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
import com.twlk.lib_rules.UsageIssue
import com.twlk.lib_rules.base.BaseDetector
import com.twlk.lib_rules.base.BaseUastVisitor
import org.jetbrains.uast.*
import java.util.*

/**
 * 兼容性：调用onOverScrolled函数时，
 *
 * @author twlk
 * @date 2018/10/22
 */
open class ScrollViewUsageDetector : BaseDetector.JavaDetector() {

    var visitor: OnOverScrolledMethodVisitor = OnOverScrolledMethodVisitor()

    override fun getApplicableUastTypes(): List<Class<out UElement>>? = Arrays.asList(UMethod::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler? = object : UElementHandler() {
        override fun visitMethod(node: UMethod) {
            if (node.name == "onOverScrolled") {
                visitor.paramName = node.uastParameters[3].name!!
                visitor.start(context, node.uastBody)
            }
        }
    }

    class OnOverScrolledMethodVisitor : BaseUastVisitor<JavaContext>() {
        var paramName: String = "clampedY"

        override fun visitIfExpression(node: UIfExpression): Boolean {
            val expression = node.condition
            if (expression is USimpleNameReferenceExpression) {
                if (expression.identifier == paramName) {
                    context.report(UsageIssue.ISSUE_SCROLLVIEW, expression, context.getLocation(expression),
                            String.format("%1s在锤子手机上永远是false", paramName))
                    return true
                }
            } else if (expression is UPrefixExpression) {
                val expression1 = expression.operand
                if (expression1 is USimpleNameReferenceExpression && expression1.identifier == paramName) {
                    context.report(UsageIssue.ISSUE_SCROLLVIEW, expression, context.getLocation(expression),
                            String.format("%1s在锤子手机上永远是false", paramName))
                    return true
                }
            }
            return false
        }

    }

}