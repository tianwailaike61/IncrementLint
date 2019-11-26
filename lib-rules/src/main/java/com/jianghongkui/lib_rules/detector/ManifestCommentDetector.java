package com.jianghongkui.lib_rules.detector;

import com.android.SdkConstants;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.XmlContext;
import com.jianghongkui.lib_rules.CommentIssue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 处理AndroidManifest注释相关规则
 *
 * @author jianghongkui
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
