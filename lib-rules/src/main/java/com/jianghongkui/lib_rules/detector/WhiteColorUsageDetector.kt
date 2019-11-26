package com.jianghongkui.lib_rules.detector

import com.android.SdkConstants
import com.android.resources.ResourceFolderType
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.TextFormat
import com.android.tools.lint.detector.api.XmlContext
import com.jianghongkui.lib_rules.UsageIssue
import com.jianghongkui.lib_rules.base.BaseDetector
import org.w3c.dom.Attr
import org.w3c.dom.Element
import java.util.*
import kotlin.collections.HashMap

/**
 *
 * 兼容性问题  设置纯白色背景在部分机型上为透明
 * @author jianghongkui
 * @date 2018/10/23
 */
open class WhiteColorUsageDetector : BaseDetector.XmlDetector() {
    var data: HashMap<Location, String> = HashMap()

    companion object {
        val WHITESTRS: List<String> = Arrays.asList("#FFF", "#FFFFFF", "#FFFFFFFF")
    }

    override fun appliesTo(folderType: ResourceFolderType): Boolean =
            ResourceFolderType.LAYOUT == folderType || ResourceFolderType.VALUES == folderType

    override fun getApplicableElements(): Collection<String>? = Arrays.asList(SdkConstants.ATTR_COLOR)

    override fun visitElement(context: XmlContext, element: Element) {
        if (context.isEnabled(UsageIssue.ISSUE_WHITE_BACKGROUND) && context.file.name == "colors.xml" &&
                WHITESTRS.contains(element.firstChild.nodeValue)) {
            val whiteColor = element.getAttribute(SdkConstants.ATTR_NAME)
            for ((key, value) in data) {
                if (value == whiteColor) {
                    context.report(UsageIssue.ISSUE_WHITE_BACKGROUND, key,
                            UsageIssue.ISSUE_WHITE_BACKGROUND.getBriefDescription(TextFormat.TEXT))
                }
            }
        }
    }

    override fun getApplicableAttributes(): Collection<String>? =
            Arrays.asList(SdkConstants.ATTR_BACKGROUND)

    override fun visitAttribute(context: XmlContext, attribute: Attr) {
        if (attribute.name == "android:background" &&
                attribute.value.startsWith("@color/")) {
            val valueIndex = attribute.value.substringAfter('/')
            data[context.getValueLocation(attribute)] = valueIndex
        }
    }
}