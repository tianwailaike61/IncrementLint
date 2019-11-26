package com.jianghongkui.lib_rules.detector

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.JavaContext
import com.jianghongkui.lib_rules.UsageIssue
import com.jianghongkui.lib_rules.base.BaseDetector
import com.jianghongkui.lib_rules.base.BaseUastVisitor
import org.jetbrains.uast.*
import java.util.*

/**
 * 兼容性：调用onOverScrolled函数时，
 *
 * @author jianghongkui
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