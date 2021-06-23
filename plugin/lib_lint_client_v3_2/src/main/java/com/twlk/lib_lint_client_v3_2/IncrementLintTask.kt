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

package com.twlk.lib_lint_client_v3_2

import com.android.build.gradle.tasks.LintBaseTask
import com.twlk.lib_lint_base.IIncrementLintTask
import com.twlk.lib_lint_base.IVariantConfigure
import com.twlk.lib_lint_base.Utils
import com.twlk.lib_lint_base.extension.IncrementLintOptions
import org.gradle.api.tasks.TaskAction

abstract class IncrementLintTask : LintBaseTask(), IIncrementLintTask {
    private lateinit var configure: IVariantConfigure

    @TaskAction
    fun lint() {
        configure.runLint(this, LintTaskDescriptor())
    }

    override fun setConfigure(configure: IVariantConfigure) {
        this.configure = configure
        lintOptions = Utils.getOptions(
            project.extensions.getByType(IncrementLintOptions::class.java),
            lintOptions
        )

        variantName = configure.variantName
    }

    private inner class LintTaskDescriptor : LintBaseTaskDescriptor() {

        override val variantName: String
            get() = configure.variantName

        override fun getVariantInputs(variantName: String): VariantInputs? {
            return configure.variantInputs
        }
    }
}