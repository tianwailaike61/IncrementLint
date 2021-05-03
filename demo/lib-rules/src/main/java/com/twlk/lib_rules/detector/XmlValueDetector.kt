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
import com.android.resources.ResourceFolderType
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.XmlContext
import com.twlk.lib_rules.*
import com.twlk.lib_rules.base.BaseDetector
import org.w3c.dom.Attr

/**
 *
 * id 、size 、color等命令和使用相关规则
 *
 * @author twlk
 * @date 2018/10/23
 */
open class XmlValueDetector : BaseDetector.XmlDetector() {

    override fun appliesTo(folderType: ResourceFolderType): Boolean =
            ResourceFolderType.LAYOUT == folderType || ResourceFolderType.DRAWABLE == folderType

    override fun getApplicableAttributes(): Collection<String>? =
            listOf(SdkConstants.ATTR_ID, SdkConstants.ATTR_TEXT, SdkConstants.ATTR_HINT, SdkConstants.ATTR_TEXT_SIZE, SdkConstants.ATTR_TEXT_COLOR, SdkConstants.ATTR_COLOR, SdkConstants.ATTR_BACKGROUND)

    override fun visitAttribute(context: XmlContext, attribute: Attr) {
        when (attribute.name) {
            "android:id" -> checkIdName(context, attribute)
            "android:text", "android:hint" -> checkChineseUsage(context, attribute)
            "android:textSize" -> checkTextSizeValue(context, attribute)
            "android:background", "android:textColor", "android:color" -> checkColorValue(context, attribute)
        }
    }

    private fun checkDirectlyUse(context: XmlContext, attribute: Attr, issue: Issue) {
        val value = attribute.value
        if (value.startsWith("@")) return
        if (value.startsWith("?attr")) return
        val name = attribute.name
        context.report(issue, attribute,
                context.getLocation(attribute), String.format("%1s 不能直接设置成 %2s", name, value))
    }

    private fun checkTextSizeValue(context: XmlContext, attribute: Attr) {
        checkDirectlyUse(context, attribute, ValueIssue.ISSUE_FONT)
    }

    private fun checkColorValue(context: XmlContext, attribute: Attr) {
        checkDirectlyUse(context, attribute, ValueIssue.ISSUE_COLOR)
    }

    private fun checkChineseUsage(context: XmlContext, attribute: Attr) {
        val string = attribute.value
        if (Utils.isEmpty(string))
            return
        if (string.isChinese()) {
            context.report(NoChineseIssue.ISSUE_XML, attribute, context.getLocation(attribute), "禁止在布局文件中直接使用中文")
        }
    }

    private fun checkIdName(context: XmlContext, attribute: Attr) {
        var value = attribute.value
        if (Utils.isEmpty(value)) {
            return
        }
        if ("android:id" == attribute.name) {
            if (value.startsWith("@android:id")) return
            var index = value.indexOf('/')
            if (index < 0) {
                index = 0
            } else {
                index++
            }
            value = value.substring(index, value.length)
        }

        var msg: String? = null
        val result = value.delete("\\d+|[a-z]|_".toRegex())
        if (result.isEmpty()) {
            if (value.startsWith("_") || value.endsWith("_")) {
                msg = "不能以下划线开头或结尾"
            }
            //数字开头已有
        } else {
            msg = if (result.delete("[a-z]".toRegex()).length != result.length) {
                "不能包含大写字母"
            } else {
                "不能包含 ${result.getResultStr()}"
            }
        }
        if (!Utils.isEmpty(msg))
            context.report(NameIssue.ISSUE_ID, attribute, context.getLocation(attribute), msg!!)
    }
}