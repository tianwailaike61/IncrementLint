/*
 * MIT License
 *
 * Copyright (c) 2021 tianwailaike61
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.android.tools.lint.gradle

import com.android.tools.lint.detector.api.*
import com.android.tools.lint.model.*
import com.intellij.pom.java.LanguageLevel
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import java.io.File

class MProjectSearch {
    private val libraryProjects = mutableMapOf<LintModelAndroidLibrary, Project>()
    private val libraryProjectsByCoordinate = mutableMapOf<LintModelMavenName, LintModelModuleLibraryProject>()
    private val namedProjects = mutableMapOf<String, Project>()
    private val javaLibraryProjects = mutableMapOf<LintModelJavaLibrary, Project>()
    private val javaLibraryProjectsByCoordinate = mutableMapOf<LintModelMavenName, LintModelModuleLibraryProject>()
    private val appProjects = mutableMapOf<org.gradle.api.Project, Project>()
    private val gradleProjects = mutableMapOf<org.gradle.api.Project, LintModelModule>()

    private fun getBuildModule(
            lintClient: MLintGradleClient,
            gradleProject: org.gradle.api.Project
    ): LintModelModule? {
        return gradleProjects[gradleProject] ?: run {
            val newModel = createLintBuildModel(lintClient, gradleProject)
            if (newModel != null) {
                gradleProjects[gradleProject] = newModel
            }
            newModel
        }
    }

    /**
     * Given a Gradle project, compute the builder model and lint models and then
     * call getProject with those
     */
    fun getProject(
            lintClient: MLintGradleClient,
            gradleProject: org.gradle.api.Project,
            variantName: String?
    ): Project? {
        val module = getBuildModule(lintClient, gradleProject)
        if (module != null && variantName != null) {
            val variant = module.findVariant(variantName)
            if (variant != null) {
                return getProject(lintClient, variant, gradleProject)
            }

            // Just use the default variant.
            // TODO: Use DSL to designate the default variants for this (not
            // yet available, but planned.)
            module.defaultVariant()?.let { defaultVariant ->
                return getProject(lintClient, defaultVariant, gradleProject)
            }
        }

        return createNonAgpProject(gradleProject, lintClient, variantName)
    }

    private fun createNonAgpProject(
            gradleProject: org.gradle.api.Project,
            lintClient: MLintGradleClient,
            variantName: String?
    ): MLintJavaProject? {
        // Make plain vanilla project; this is what happens for Java projects (which
        // don't have a Gradle model)
        val convention = gradleProject.convention.findPlugin(
                JavaPluginConvention::class.java
        ) ?: return null

        // Language level: Currently not needed. The way to get it is via
        //   convention.getSourceCompatibility()
        val language = LanguageLevel.parse(convention.sourceCompatibility.name)

        // Sources
        val sourceSets = convention.sourceSets
        val sources: MutableList<File> = mutableListOf()
        val classes: MutableList<File> = mutableListOf()
        val libs: MutableList<File> = mutableListOf()
        val tests: MutableList<File> = mutableListOf()
        for (sourceSet in sourceSets) {
            if (sourceSet.name == SourceSet.TEST_SOURCE_SET_NAME) {
                // We don't model the full test source set yet (e.g. its dependencies),
                // only its source files
                val javaSrc = sourceSet.java
                for (dir in javaSrc.srcDirs) {
                    if (dir.exists()) {
                        tests.add(dir)
                    }
                }
                continue
            }
            val javaSrc = sourceSet.java
            // There are also resource directories, in case we want to
            // model those here eventually
            for (dir in javaSrc.srcDirs) {
                if (dir.exists()) {
                    sources.add(dir)
                }
            }
            for (file in sourceSet.output.classesDirs) {
                if (file.exists()) {
                    classes.add(file)
                }
            }
            for (file in sourceSet.compileClasspath.files) {
                if (file.exists()) {
                    libs.add(file)
                }
            }

            // TODO: Boot classpath? We don't have access to that here, so for
            // now the LintCliClient just falls back to the running Gradle JVM and looks
            // up its class path.
        }
        val projectDir = gradleProject.projectDir
        val dependencies: MutableList<Project> = mutableListOf()
        val project = MLintJavaProject(
                lintClient, projectDir, dependencies, sources, classes, libs, tests, language
        )

        // Dependencies
        val configurations = gradleProject.configurations
        val compileConfiguration = configurations.getByName("compileClasspath")
        for (dependency in compileConfiguration.allDependencies) {
            if (dependency is ProjectDependency) {
                val p = dependency.dependencyProject
                val lintProject = getProject(lintClient, p.path, p, variantName)
                        ?: continue
                dependencies.add(lintProject)
            } else if (dependency is ExternalDependency) {
                val name = dependency.getName()
                // group or version null: this will be the case for example with
                //    repositories { flatDir { dirs 'myjars' } }
                //    dependencies { compile name: 'guava-18.0' }
                val group = dependency.getGroup() ?: continue
                val version = dependency.getVersion() ?: continue
                val coordinates = DefaultLintModelMavenName(group, name, version)
                val javaLib = javaLibraryProjectsByCoordinate[coordinates]
                        ?: continue // Create wrapper? Unfortunately we don't have the actual .jar file
                javaLib.isExternalLibrary = true
                dependencies.add(javaLib)
            } else if (dependency is FileCollectionDependency) {
                val files = dependency.resolve()
                libs.addAll(files)
            }
        }
        return project
    }

    private fun getProject(
            client: MLintGradleClient,
            variant: LintModelVariant,
            gradleProject: org.gradle.api.Project
    ): Project {
        val cached = appProjects[gradleProject]
        if (cached != null) {
            return cached
        }
        val dir = gradleProject.projectDir
        val manifest = client.mergedManifest
        val lintProject = LintModelModuleProject(client, dir, dir, variant, manifest)
        appProjects[gradleProject] = lintProject
        lintProject.gradleProject = true

        // DELIBERATELY calling getDependencies here (and Dependencies#getProjects() below) :
        // the new hierarchical model is not working yet.
        val dependencies = variant.mainArtifact.dependencies.compileDependencies
        for (item in dependencies.roots) {
            val library = item.findLibrary() ?: continue // local project dependency: handled below
            if (library is LintModelAndroidLibrary) {
                lintProject.addDirectLibrary(getLibrary(client, item, library, gradleProject, variant))
            }
        }

        // Dependencies.getProjects() no longer passes project names in all cases, so
        // look up from Gradle project directly
        var processedProjects: MutableList<String?>? = null
        val configurations = gradleProject.configurations
        val compileConfiguration =
                configurations.getByName(variant.name + "CompileClasspath")
        for (dependency in compileConfiguration.allDependencies) {
            if (dependency is ProjectDependency) {
                val p = dependency.dependencyProject
                // Libraries don't have to use the same variant name as the
                // consuming app. In fact they're typically not: libraries generally
                // use the release variant. We can look up the variant name
                // in AndroidBundle#getProjectVariant, though it's always null
                // at the moment. So as a fallback, search for existing
                // code.
                val depProject = getProject(client, p, variant.name)
                if (depProject != null) {
                    if (processedProjects == null) {
                        processedProjects = mutableListOf()
                    }
                    processedProjects.add(p.path)
                    lintProject.addDirectLibrary(depProject)
                }
            }
        }
        for (libraryItem in dependencies.roots) {
            val library = libraryItem.findLibrary()
            if (library is LintModelJavaLibrary) {
                val projectName = library.getProjectPath()
                if (projectName != null) {
                    if (processedProjects != null && processedProjects.contains(projectName)) {
                        continue
                    }
                    val libLintProject =
                            getProject(client, projectName, gradleProject, variant.name)
                    if (libLintProject != null) {
                        lintProject.addDirectLibrary(libLintProject)
                        continue
                    }
                }
                lintProject.addDirectLibrary(getLibrary(client, libraryItem, library))
            }
        }
        return lintProject
    }

    private fun getProject(
            client: MLintGradleClient,
            path: String,
            gradleProject: org.gradle.api.Project,
            variantName: String?
    ): Project? {
        val cached = namedProjects[path]
        if (cached != null) {
            return cached
        }
        val namedProject = gradleProject.findProject(path)
        if (namedProject != null) {
            val project = getProject(client, namedProject, variantName)
            if (project != null) {
                namedProjects[path] = project
                return project
            }
        }
        return null
    }

    private fun getLibrary(
            client: MLintGradleClient,
            libraryItem: LintModelDependency,
            library: LintModelAndroidLibrary,
            gradleProject: org.gradle.api.Project,
            variant: LintModelVariant
    ): Project {
        var cached = libraryProjects[library]
        if (cached != null) {
            return cached
        }
        val coordinates = library.resolvedCoordinates
        cached = libraryProjectsByCoordinate[coordinates]
        if (cached != null) {
            return cached
        }
        if (library.getProjectPath() != null) {
            val project =
                    getProject(client, library.getProjectPath()!!, gradleProject, variant.name)
            if (project != null) {
                libraryProjects[library] = project
                return project
            }
        }
        val dir = library.folder
        val project = LintModelModuleAndroidLibraryProject(client, dir, dir, libraryItem, library)
        project.setMavenCoordinates(coordinates)
        if (library.getProjectPath() == null) {
            project.isExternalLibrary = true
        }
        libraryProjects[library] = project
        libraryProjectsByCoordinate[coordinates] = project
        for (dependentItem in libraryItem.dependencies) {
            val dependent = dependentItem.findLibrary() ?: continue
            if (dependent is LintModelAndroidLibrary) {
                project.addDirectLibrary(
                        getLibrary(client, dependentItem, dependent, gradleProject, variant)
                )
            } else {
                // TODO What do we do here? Do we create a wrapper JavaLibrary project?
            }
        }
        return project
    }

    private fun getLibrary(
            client: MLintGradleClient,
            libraryItem: LintModelDependency,
            library: LintModelJavaLibrary
    ): Project {
        var cached = javaLibraryProjects[library]
        if (cached != null) {
            return cached
        }
        val coordinates = library.resolvedCoordinates
        cached = javaLibraryProjectsByCoordinate[coordinates]
        if (cached != null) {
            return cached
        }
        val dir = library.jarFiles.first()
        val project = LintModelModuleJavaLibraryProject(client, dir, dir, libraryItem, library)
        project.setMavenCoordinates(coordinates)
        project.isExternalLibrary = true
        javaLibraryProjects[library] = project
        javaLibraryProjectsByCoordinate[coordinates] = project
        for (dependentItem in libraryItem.dependencies) {
            val dependent = dependentItem.findLibrary() ?: continue
            // just a sanity check; Java libraries cannot depend on Android libraries
            if (dependent is LintModelJavaLibrary) {
                project.addDirectLibrary(
                        getLibrary(client, dependentItem, dependent)
                )
            }
        }
        return project
    }

    private fun createLintBuildModel(
            client: MLintGradleClient,
            gradleProject: org.gradle.api.Project
    ): LintModelModule? {
        val pluginContainer = gradleProject.plugins
        for (p in pluginContainer) {
            val provider = p as? LintModelModuleLoaderProvider ?: continue
            val factory = LintModelFactory()
            // This should not be necessary, but here until AGP includes all the folders
            // necessary via the builder-model (or better yet, via XML serialization)
            factory.kotlinSourceFolderLookup = { variantName ->
                client.getKotlinSourceFolders(gradleProject, variantName)
                        // It also includes default Java source providers which are redundant
                        .filter { it.name != "java" }
            }

            return provider.getModuleLoader().getModule(gradleProject.path, factory)
        }
        return null
    }
}