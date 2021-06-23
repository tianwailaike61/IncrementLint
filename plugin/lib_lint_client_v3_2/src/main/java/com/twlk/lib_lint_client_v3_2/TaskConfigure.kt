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

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.scope.GlobalScope
import com.android.build.gradle.tasks.LintBaseTask
import com.twlk.lib_lint_base.AbsTaskConfigure
import com.twlk.lib_lint_base.ILintRunner
import com.twlk.lib_lint_base.Utils

class TaskConfigure(name: String, private val variant: BaseVariant) :
    AbsTaskConfigure(name, variant) {

    override fun getRunner(): ILintRunner = LintRunner()

    override var variantInputs: LintBaseTask.VariantInputs? = null

    init {
        variantInputs = LintBaseTask.VariantInputs(Utils.getVariantScope(variant))
    }

    override fun getGlobalScope(): GlobalScope? {
        val scope = Utils.getVariantScope(variant) ?: return null
        return scope.globalScope
    }
}