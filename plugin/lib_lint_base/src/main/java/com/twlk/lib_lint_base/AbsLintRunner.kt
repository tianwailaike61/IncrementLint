/*
 * MIT License
 *
 * Copyright (c) 2020 tianwailaike61
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

package com.twlk.lib_lint_base

import com.android.tools.lint.gradle.api.DelegatingClassLoader
import com.android.tools.lint.gradle.api.LintExecutionRequest
import com.google.common.base.Throwables
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.invocation.Gradle
import org.gradle.initialization.BuildCompletionListener
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.net.URLClassLoader

abstract class AbsLintRunner : ILintRunner {

    // There should only be one Lint class loader, and it should
    // exist for the entire lifetime of the Gradle daemon.
    protected var loader: DelegatingClassLoader? = null

    private var buildCompletionListenerRegistered = false

    override fun runLint(gradle: Gradle, request: LintExecutionRequest, lintClassPath: FileCollection, collector: LintResultCollector, clientClz: Class<*>) {
        try {
            val classPaths = HashSet<File>()
            val clientJar = File(clientClz.protectionDomain.codeSource.location.file)
            classPaths.add(clientJar)
            classPaths.addAll(lintClassPath)
            val loader = getLintClassLoader(gradle, classPaths, clientJar.name)
            val cls = loader.loadClass("com.twlk.lib_lint_client.LintGradleExecution")
            val constructor = cls.getDeclaredConstructor(LintExecutionRequest::class.java, LintResultCollector::class.java)
            val obj = constructor.newInstance(request, collector)
            val analyseMethod = cls.getMethod("analyze")
            analyseMethod.invoke(obj)
        } catch (e: InvocationTargetException) {
            if (e.targetException is GradleException) {
                // Build error from lint -- pass it on
                throw e.targetException
            }
            throw wrapExceptionAsString(e)
        } catch (t: Throwable) {
            // Reflection problem
            throw wrapExceptionAsString(t)
        }
    }

    @Throws
    open fun onBuildCompleted(loader: ClassLoader) {
        val cls: Class<*> = try {
            loader.loadClass("com.android.tools.lint.UastEnvironment")
        } catch (e: ClassNotFoundException) {
            loader.loadClass("com.android.tools.lint.LintCoreApplicationEnvironment")
        }
        val disposeMethod = cls.getDeclaredMethod("disposeApplicationEnvironment")
        disposeMethod.invoke(null)
    }

    protected open fun getLintClassLoader(gradle: Gradle, lintClassPath: Set<File>, customJarName: String): ClassLoader {
        var l = loader
        if (l == null) {
            val urls = getUrls(lintClassPath, customJarName)
            l = DelegatingClassLoader(urls.toTypedArray())
            loader = l
        }

        // There can be multiple Lint tasks running in parallel, and we would like them
        // to share the same UastEnvironment (in order to share caches).
        // Thus we do not dispose the UastEnvironment until the entire
        // Gradle invocation finishes.
        if (!buildCompletionListenerRegistered) {
            buildCompletionListenerRegistered = true
            gradle.addListener(BuildCompletionListener {
                onBuildCompleted(l)
                buildCompletionListenerRegistered = false
            })
        }

        return l
    }

    protected abstract fun getUrls(lintClassPath: Set<File>, customJarName: String): List<URL>

    /**
     * Computes the exact set of URLs that we should load into our own
     * class loader. This needs to include all the classes lint depends on,
     * but NOT the classes that are already defined by the gradle plugin,
     * since we'll be passing in data (like Gradle projects, builder model
     * classes, sdklib classes like BuildInfo and so on) and these need
     * to be using the existing class loader.
     *
     * This is based on hardcoded heuristics instead of deltaing class loaders.
     */
    protected fun computeUrlsFallback(lintClassPath: Set<File>, customJarName: String): List<URL> {
        val urls = mutableListOf<URL>()
        val iterator: MutableIterator<File> = lintClassPath.iterator() as MutableIterator<File>
        while (iterator.hasNext()) {
            val file = iterator.next()
            if (file.name.startsWith(customJarName)) {
                try {
                    urls.add(file.toURI().toURL())
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                }
                iterator.remove()
            }
        }
        for (file in lintClassPath) {
            val name = file.name

            // The set of jars that lint needs that *aren't* already used/loaded by gradle-core
            if (name.startsWith("uast-") ||
                    name.startsWith("intellij-core-") ||
                    name.startsWith("kotlin-compiler-") ||
                    name.startsWith("asm-") ||
                    name.startsWith("kxml2-") ||
                    name.startsWith("trove4j-") ||
                    name.startsWith("groovy-all-") ||

                    // All the lint jars, except lint-gradle-api jar (self)
                    name.startsWith("lint-") &&
                    // Do *not* load this class in a new class loader; we need to
                    // share the same class as the one already loaded by the Gradle
                    // plugin
                    !name.startsWith("lint-gradle-api-")
            ) {
                urls.add(file.toURI().toURL())
            }
        }

        return urls
    }

    private fun wrapExceptionAsString(t: Throwable) = RuntimeException(
            "Lint infrastructure error\nCaused by: ${Throwables.getStackTraceAsString(t)}\n"
    )

    companion object {

        /**
         * Computes the class loader based on looking at the given [lintClassPath] and
         * subtracting out classes already loaded by the Gradle plugin directly.
         * This may fail if the class loader isn't a URL class loader, or if
         * after some diagnostics we discover that things aren't the way they should be.
         */
        fun computeUrlsFromClassLoaderDelta(lintClassPath: Set<File>): List<URL>? {
            // Operating on URIs rather than URLs here since URL.equals is a blocking (host name
            // resolving) operation.
            // We map to library names since sometimes the Gradle plugin and the lint class path
            // vary in where they locate things, e.g. builder-model in lintClassPath could be
            //  file:out/repo/com/<truncated>/builder-model/3.1.0-dev/builder-model-3.1.0-dev.jar
            // vs the current class loader pointing to
            //  file:~/.gradle/caches/jars-3/a6fbe15f1a0e37da0962349725f641cc/builder-3.1.0-dev.jar
            val uriMap = HashMap<String, URI>(2 * lintClassPath.size)
            lintClassPath.forEach {
                val uri = it.toURI()
                val name = getLibrary(uri) ?: return null
                uriMap[name] = uri
            }

            val gradleClassLoader = this::class.java.classLoader as? URLClassLoader ?: return null
            for (url in gradleClassLoader.urLs) {
                val uri = url.toURI()
                val name = getLibrary(uri) ?: return null
                if (name.startsWith("lib_lint_")) {
                    continue
                }
                uriMap.remove(name)
            }

            // Convert to URLs (and sanity check the result)
            val urls = ArrayList<URL>(uriMap.size)
            var seenLint = false
            for ((name, uri) in uriMap) {
                if (name.startsWith("lint-api")) {
                    seenLint = true
                } else if (name.startsWith("builder-model")) {
                    // This should never be on our class path, something is wrong
                    return null
                }
                urls.add(uri.toURL())
            }

            if (!seenLint) {
                // Something is wrong; fall back to heuristics
                return null
            }

            return urls
        }

        private fun getLibrary(uri: URI): String? {
            val path = uri.path
            val index = uri.path.lastIndexOf('/')
            if (index == -1) {
                return null
            }
            var dash = path.indexOf('-', index)
            while (dash != -1 && dash < path.length) {
                if (path[dash + 1].isDigit()) {
                    return path.substring(index + 1, dash)
                } else {
                    dash = path.indexOf('-', dash + 1)
                }
            }

            return path.substring(index + 1, if (dash != -1) dash else path.length)
        }
    }
}