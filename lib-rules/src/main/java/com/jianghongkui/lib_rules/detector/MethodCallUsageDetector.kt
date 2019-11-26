package com.jianghongkui.lib_rules.detector

import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiMethod
import com.jianghongkui.lib_rules.UsageIssue
import com.jianghongkui.lib_rules.base.BaseDetector
import org.jetbrains.kotlin.incremental.isJavaFile
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.UQualifiedReferenceExpression

/**
 *
 * 函数调用相关检查
 *
 * @author jianghongkui
 * @date 2018/10/22
 */
open class MethodCallUsageDetector : BaseDetector.JavaDetector() {

    private var isLogEnable: Boolean = true

    override fun getApplicableMethodNames(): List<String>? = listOf("v", "d", "i", "w", "e", "setAlpha", "equals")

    override fun beforeCheckFile(context: Context) {
        super.beforeCheckFile(context)
        isLogEnable = !(context.file.isJavaFile() && "ExLogUtil.java" == context.file.name)
    }

    override fun visitMethod(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        when (node.methodName) {
            "v", "d", "i", "w", "e" -> {
                if (isLogEnable && context.isEnabled(UsageIssue.ISSUE_LOG) &&
                        context.evaluator.isMemberInClass(method, "android.util.Log")) {
                    context.report(UsageIssue.ISSUE_LOG, node, context.getLocation(node), String.format("Log.%1s不允许使用", method.name))
                }
            }
            "setAlpha" -> {
                if (!context.isEnabled(UsageIssue.ISSUE_SET_ALPHA)) return
                if (context.evaluator.isMemberInClass(method, "android.graphics.drawable") &&
                        !node.receiver!!.asSourceString().endsWith("mutate()")) {
                    context.report(UsageIssue.ISSUE_SET_ALPHA, node, context.getLocation(node), "不能直接使用setAlpha")
                }
            }
            "equals" -> {
                if (!context.isEnabled(UsageIssue.ISSUE_EQUALS)) return
                if (!context.evaluator.isMemberInSubClassOf(method, "java.lang.Object", false))
                    return

                if (node.valueArgumentCount != 1)
                    return
                val expression = node.valueArguments[0]
                if (expression is ULiteralExpression) {
                    context.report(UsageIssue.ISSUE_EQUALS, node, context.getLocation(node), "请将常量写在equals前面")
                }
            }
        }
    }
}