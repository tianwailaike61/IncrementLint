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

package com.twlk.lib_rules.detector

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.TextFormat
import com.intellij.psi.PsiMethod
import com.twlk.lib_rules.UsageIssue
import com.twlk.lib_rules.base.BaseDetector
import org.jetbrains.uast.*

/**
 * 处理与线程使用相关规则
 *
 * @author twlk
 * @date 2018/10/10
 */
open class ThreadUsageDetector : BaseDetector.JavaDetector() {

    companion object {
        val map = mapOf(UsageIssue.ISSUE_THREAD to "java.lang.Thread",
                UsageIssue.ISSUE_HANDLER to "android.os.Handler")
    }

    override fun getApplicableConstructorTypes(): List<String>? = map.values.toList()

    override fun visitConstructor(context: JavaContext, node: UCallExpression, constructor: PsiMethod) {
        val evaluator = context.evaluator
        for (entry in map.entries) {
            if (evaluator.isMemberInSubClassOf(constructor, entry.value, false)) {
                context.report(entry.key, node, context.getLocation(node),
                        entry.key.getBriefDescription(TextFormat.TEXT))
            }
        }
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>>? = listOf(UClass::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler = object : UElementHandler() {

        override fun visitClass(node: UClass) {
            val evaluator = context.evaluator
            for (entry in map.entries) {
                if (evaluator.extendsClass(node, entry.value, false)) {
                    context.report(entry.key, node, context.getNameLocation(node),
                            entry.key.getBriefDescription(TextFormat.TEXT))
                }
            }
        }
    }
}