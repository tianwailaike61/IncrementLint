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

package com.twlk.lib_lint_client_v4_2

import com.android.build.api.component.impl.ComponentImpl
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.BaseVariantImpl
import com.android.build.gradle.internal.scope.GlobalScope
import com.android.build.gradle.tasks.LintBaseTask
import com.android.tools.lint.gradle.api.LintExecutionRequest
import com.twlk.lib_lint_base.AbsTaskConfigure
import com.twlk.lib_lint_base.ILintRunner

internal class TaskConfigure(name: String, variant: BaseVariant) : AbsTaskConfigure(name, variant) {
    private var component: ComponentImpl? = null

    override var variantInputs: LintBaseTask.VariantInputs? = null

    private val runner: LintRunner = LintRunner()

    init {
        val cls = BaseVariantImpl::class.java
        try {
            val field = cls.getDeclaredField("component")
            field.isAccessible = true
            component = field[variant] as ComponentImpl
            variantInputs = LintBaseTask.VariantInputs(component)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    override fun runLint(task: LintBaseTask, request: LintExecutionRequest) {
        runner.service = task.lintClassLoader.get()
        super.runLint(task, request)
    }

    override fun getRunner(): ILintRunner = runner

    override fun getGlobalScope(): GlobalScope? {
        return component?.globalScope
    }
}