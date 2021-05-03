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

import com.android.SdkConstants
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.JavaContext
import com.twlk.lib_rules.*
import com.twlk.lib_rules.UsageIssue.ISSUE_ENUM
import com.twlk.lib_rules.base.BaseDetector.JavaDetector
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import java.util.*
import kotlin.collections.HashMap

/**
 * 处理与类相关规则
 *
 * @author twlk
 * @date 2018/10/10
 */
open class ClassDetector : JavaDetector() {


    companion object {
        const val MAX_LINE = 800
        var classMap: HashMap<String, String> = HashMap()

        const val CLASS_LINE_MSG = "类的行数不能超过$MAX_LINE"
    }

    init {
        if (classMap.isEmpty()) {
            classMap[SdkConstants.CLASS_ACTIVITY] = "Activity"
            classMap[SdkConstants.CLASS_FRAGMENT] = "Fragment"
            classMap["android.support.v4.app.Fragment"] = "Fragment"
            classMap[SdkConstants.CLASS_BROADCASTRECEIVER] = "Receiver"
            classMap[SdkConstants.CLASS_SERVICE] = "Service"
            classMap["android.app.Dialog"] = "Dialog"
            classMap[SdkConstants.CLASS_CONTENTPROVIDER] = "Provider"
        }
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        val list = ArrayList<Class<out UElement>>(1)
        list.add(UClass::class.java)
        return list
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? = object : UElementHandler() {
        override fun visitClass(node: UClass) {
            if (Utils.isEmpty(node.name))
                return
            checkComment(context, node)
            if (checkLines(context, node)) return
            if (checkName(context, node)) return
            if (node.isEnum) {
                context.report(ISSUE_ENUM, context.getNameLocation(node), "禁止使用枚举类型")
                return
            }

            if (node.isInterface) {
                checkInterface(context, node)
            }
        }
    }


    override fun applicableSuperClasses(): List<String>? {
        val list = ArrayList<String>(classMap.size)
        list.addAll(classMap.keys)
        return list
    }

    override fun visitClass(context: JavaContext, declaration: UClass) {
        if (!declaration.isInterface) {
            checkExtendsClass(context, declaration)
        }
    }

    private fun checkLines(context: JavaContext, node: UClass): Boolean {
        val location = context.getLocation(node as UElement)
        val nameLocation = context.getNameLocation(node)
        var classLine = location.end!!.line - nameLocation.start!!.line

        if (classLine < MAX_LINE) return false
        for (cl in node.innerClasses) {
            val childLocation = context.getLocation(cl as UElement)
            classLine = classLine - childLocation.end!!.line + childLocation.start!!.line
            if (classLine < MAX_LINE) return false
        }
        context.report(AnalysisIssue.ISSUE_CLASS_LINES, nameLocation, CLASS_LINE_MSG)
        return true
    }

    private fun checkComment(context: JavaContext, node: UClass): Boolean {
        if (node.language.id != "JAVA") return false
        if (context.file.name.substringBefore('.') != node.name) return false

        val classLocation = context.getNameLocation(node)
        if (node.comments.isEmpty()) {
            context.report(CommentIssue.ISSUE_CLASS, node, classLocation, "无注释")
            return true
        }

        val comment = node.comments.first()
        val commentLocation = context.getNameLocation(comment.javaPsi!!)
        if (commentLocation.start!!.line > classLocation.start!!.line) {
            context.report(CommentIssue.ISSUE_CLASS, node, classLocation, "需要添加注释")
            return true
        }

        val s = comment.text.toLowerCase()

        if (!s.startsWith("/**")) {
            context.report(CommentIssue.ISSUE_CLASS, node, commentLocation, "使用 /**内容*/格式")
            return true
        }
        return false

        //        if (s.contains("@author") && s.contains("@classMap")) {
        //            return false;
        //        }
        //        context.report(CommentIssue.ISSUE_CLASS, node, commentlocation, "类注释需包含创建人和创建时间");
        //        return true;
    }

    private fun checkName(context: JavaContext, node: UClass): Boolean {
        val name = node.name!!
        if (Utils.isEmpty(name)) return true
        val c = name.first()
        if (c < 'A' || c > 'Z') {
            context.report(NameIssue.ISSUE_CLASS, node, context.getNameLocation(node), "首字母大写")
            return false
        }
        val result = name.delete("[a-z]|[A-Z]")
        if (result.isEmpty()) {
            return false
        }
        val s = Arrays.toString(result.toCharArray())
        context.report(NameIssue.ISSUE_CLASS, node, context.getNameLocation(node),
                "类名不能包含\"${s.substring(1, s.length - 1)}\"")
        return false
    }

    private fun checkExtendsClass(context: JavaContext, node: UClass): Boolean {
        if (Utils.isEmpty(node.name))
            return false
        for ((key, value) in classMap) {
            if (context.evaluator.extendsClass(node, key, false)) {
                if (!node.name!!.endsWith(value)) {
                    context.report(NameIssue.ISSUE_CLASS, node, context.getNameLocation(node),
                            "类名需要以${value}结尾")
                    return true
                }
                return false
            }
        }
        return false
    }

    private fun checkInterface(context: JavaContext, node: UClass): Boolean {
        //        if (!node.getName().startsWith("I")) {
        //            context.report(ISSUE_CLASS, node, context.getNameLocation(node), "接口名需要以I开头");
        //        }
        return false
    }
}