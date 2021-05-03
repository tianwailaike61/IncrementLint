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

package com.twlk.lib_rules

import com.android.SdkConstants
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author twlk
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
