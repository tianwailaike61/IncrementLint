package com.jianghongkui.lib_rules

import com.android.SdkConstants
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author jianghongkui
 * @date 2018/10/24
 */
open class LayoutLevelChecker(private var listener: IActionListener) {

    private var maxLevel: Int = 0
    private var isStartByMerge: Boolean = false

    fun start(file: File): Boolean {
        if (!file.isFile || !file.name.endsWith(".xml")) return false

        val dbf = DocumentBuilderFactory.newInstance()
        val db = dbf.newDocumentBuilder()
        return this.start(db.parse(file))
    }

    fun start(document: Document): Boolean {
        if (document.lastChild.nodeType != Node.ELEMENT_NODE) return false
        maxLevel = 0
        listener.beforeCheck()
        parseXml(document.lastChild as Element, 1)
        listener.afterCheck(maxLevel,isStartByMerge)
        return true
    }

    private fun parseXml(element: Element, level: Int) {
        if (element.tagName == SdkConstants.VIEW_MERGE) {
            isStartByMerge = true
        }
        val hasChild = element.hasChildNodes()
        if (level > maxLevel) maxLevel = level
        if (listener.onVisitElement(element, level, hasChild, isStartByMerge)) return
        if (hasChild) {
            var i = 0
            while (i < element.childNodes.length) {
                val childNode = element.childNodes.item(i)
                i++
                if (childNode.nodeType != Node.ELEMENT_NODE) continue
                parseXml(childNode as Element, level + 1)

            }
        }
    }

    interface IActionListener {
        fun beforeCheck() {
        }

        fun onVisitElement(element: Element, level: Int, hasChild: Boolean, isStartByMerge: Boolean): Boolean = false

        fun afterCheck(maxLevel: Int, isStartByMerge: Boolean) {

        }
    }
}
