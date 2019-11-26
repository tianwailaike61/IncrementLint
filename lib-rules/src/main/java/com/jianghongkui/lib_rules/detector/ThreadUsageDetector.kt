package com.jianghongkui.lib_rules.detector

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.TextFormat
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNewExpression
import com.jianghongkui.lib_rules.UsageIssue
import com.jianghongkui.lib_rules.base.BaseDetector
import org.jetbrains.uast.*
import org.jetbrains.uast.util.isConstructorCall

/**
 * 处理与线程使用相关规则
 *
 * @author jianghongkui
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