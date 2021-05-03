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

package com.twlk.lint_plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.twlk.lib_lint_base.FnLogger
import com.twlk.lib_lint_base.IncrementLintLintBaseTask
import com.twlk.lib_lint_base.Utils
import com.twlk.lib_lint_base.extension.IncrementLintLintExtension
import com.twlk.lib_lint_base.extension.MLintOptions
import com.twlk.lib_lint_client_v3_3.IncrementLintLintTask
import com.twlk.lint_plugin.task.CommandTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.impldep.org.eclipse.jgit.errors.NotSupportedException

class LintPlugin : Plugin<Project> {
    private lateinit var project: Project
    private lateinit var extension: IncrementLintLintExtension
    override fun apply(project: Project) {
        this.project = project
        extension = project.extensions.create(IncrementLintLintExtension.NAME, IncrementLintLintExtension::class.java)
        project.extensions.create("MLintOptions", MLintOptions::class.java)
        extension.init(project)
        FnLogger.setOutFile(extension.logPath)
        CommandTask.create(project)
        when {
            project.plugins.hasPlugin("com.android.application") -> {
                project.getAndroid<AppExtension>().let { android ->
                    project.afterEvaluate {
                        android.applicationVariants.forEach { variant ->
                            maybeLint(variant, "App")
                        }
                    }
                }
            }

            project.plugins.hasPlugin("com.android.library") -> {
                project.getAndroid<LibraryExtension>().let { lib ->
                    project.afterEvaluate {
                        lib.libraryVariants.forEach { variant ->
                            maybeLint(variant, "lib")
                        }
                    }
                }
            }

            else -> {
                return
            }
        }
        project.gradle.addListener(this)
    }

    private fun maybeLint(variant: BaseVariant, type: String) {
        val suffix = variant.name.capitalize()
        if (!suffix.endsWith("Debug")) {
            return
        }
        addDebugLintTask(suffix, variant, type)
    }

    private fun addDebugLintTask(suffix: String, variant: BaseVariant, type: String) {
        val taskName = "IncrementLint${suffix}"
        createLintTask(taskName, variant)?.let {
            dependedTask(it, "getChangedFile", "prepareLintJar", "compile${suffix}JavaWithJavac", "process${suffix}Manifest")
        }
        if (type == "App") {
            dependTask("assemble${suffix}", taskName)
        } else {
            dependTask("bundleLibRuntime${suffix}", taskName)
        }
    }

    /**
     * [3.2.0 ~ 3.3.0) lib_lint_client_v3_2
     * [3.3.0 ~ 3.6.0) lib_lint_client_v3_3
     * [3.6.0 ~ 4.0.0) lib_lint_client_v3_6
     * [4.0.0 ~ 4.1.0] lib_lint_client_v4_1
     */
    private fun createLintTask(taskName: String, variant: BaseVariant): Task? {
        //创建LintTask 任务
        val agpVersion = Utils.getAGPVersion()
        val cls: Class<out IncrementLintLintBaseTask> = if (Utils.compareVersion("3.2.0", agpVersion) > 0) {
            throw NotSupportedException("")
        } else if (Utils.compareVersion("3.3.0", agpVersion) > 0) {
            com.twlk.lib_lint_client_v3_2.IncrementLintLintTask::class.java
        } else if (Utils.compareVersion("3.6.0", agpVersion) > 0) {
            IncrementLintLintTask::class.java
        } else if (Utils.compareVersion("4.1.0", agpVersion) > 0) {
            com.twlk.lib_lint_client_v3_6.IncrementLintLintTask::class.java
        } else {
            com.twlk.lib_lint_client_v4_1.IncrementLintLintTask::class.java
        }
        val task: Task? = IncrementLintLintBaseTask.createTask(project, variant, taskName, cls)
        task?.let {
            val extension = project.extensions.findByName(IncrementLintLintExtension.NAME) as IncrementLintLintExtension
            it.inputs.files(extension.commandExtension.changedInfoFile)
        }
        return task
    }

    private fun dependTask(taskName: String, vararg dependTaskNames: String) {
        val task: Task = project.tasks.findByName(taskName) ?: return
        dependedTask(task, *dependTaskNames)
    }

    private fun dependedTask(task: Task, vararg dependTaskNames: String) {
        val tasks: MutableList<Task> = ArrayList()
        dependTaskNames.forEach { taskName ->
            project.tasks.findByName(taskName)?.let {
                tasks.add(it)
            }
        }
        task.dependsOn(tasks.toTypedArray())
    }
}