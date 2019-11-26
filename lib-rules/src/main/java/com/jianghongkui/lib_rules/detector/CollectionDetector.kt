package com.jianghongkui.lib_rules.detector

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiMethod
import com.jianghongkui.lib_rules.UsageIssue
import com.jianghongkui.lib_rules.base.BaseDetector
import org.jetbrains.uast.*
import java.util.*

/**
 * 处理与集合相关规则
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
open class CollectionDetector : BaseDetector.JavaDetector() {

    override fun getApplicableMethodNames(): List<String>? = Arrays.asList("keySet")


    override fun visitMethod(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        when (method.name) {
            "keySet" -> checkSet(context, node, method)
        }
    }

    private fun checkSet(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (context.evaluator.isMemberInSubClassOf(method, "java.util.Set", false)) {
            context.report(UsageIssue.ISSUE_SET, node, context.getLocation(node),
                    "使用 entrySet 遍历 Map 类集合 KV,而不是 keySet 方式进行遍历")
        }
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        val list = ArrayList<Class<out UElement>>(1)
        list.add(UCallExpression::class.java)
        return list
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {

            override fun visitCallExpression(node: UCallExpression) {
                val method = node.resolve()
                if (method != null && method.isConstructor && node.valueArguments.isEmpty() &&
                        context.evaluator.isMemberInSubClassOf(method, "java.util.Collection", false)
                        && isHaveIntConstructor(node)) {
                    context.report(UsageIssue.ISSUE_COLLECTION_INIT, node, context.getLocation(node), "指定集合初始值大小")
                }
            }
        }
    }

    private fun isHaveIntConstructor(node: UCallExpression): Boolean {
        val uClass = node.resolveToUElement()!!.uastParent
        if (uClass is UClass) {
            val iterator = uClass.constructors.iterator()
            while (iterator.hasNext()) {
                val element = iterator.next().toUElement(UMethod::class.java)
                if (element != null && element.uastParameters.size == 1) {
                    val param = element.uastParameters.single().typeReference.toString()
                    if (param == "int") return true
                }

            }
        }
        return false
    }

}