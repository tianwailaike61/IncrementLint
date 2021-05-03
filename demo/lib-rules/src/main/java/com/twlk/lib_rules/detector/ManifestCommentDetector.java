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

package com.twlk.lib_rules.detector;

import com.android.SdkConstants;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.XmlContext;
import com.twlk.lib_rules.CommentIssue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 处理AndroidManifest注释相关规则
 *
 * @author twlk
 * @date 2018/10/11
 */
public class ManifestCommentDetector extends Detector implements Detector.XmlScanner {

    @Override
    public void visitDocument(XmlContext context, Document document) {
        NodeList applicationNodeList = document.getElementsByTagName(SdkConstants.TAG_APPLICATION);
        if (applicationNodeList.getLength() != 1) {
            return;
        }
        int lastCommentEndLine = -1;
        NodeList nodeList = applicationNodeList.item(0).getChildNodes();
        for (int j = 0; j < nodeList.getLength(); j++) {
            Node node = nodeList.item(j);
            if (node.getNodeType() == Node.COMMENT_NODE) {
                lastCommentEndLine = context.getLocation(node).getEnd().getLine();
                continue;
            }
            if (node instanceof Element) {
                Element element = (Element) node;
                if (SdkConstants.TAG_ACTIVITY.equals(element.getTagName())) {
                    Location location = context.getLocation(element);
                    if (lastCommentEndLine + 1 == location.getStart().getLine()) continue;
                    context.report(CommentIssue.ISSUE_MANIFEST, element, location, "Activity需要添加注释");
                }
            }
        }

    }
}
