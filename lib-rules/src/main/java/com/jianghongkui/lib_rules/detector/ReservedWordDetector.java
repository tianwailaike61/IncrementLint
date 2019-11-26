package com.jianghongkui.lib_rules.detector;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.TextFormat;
import com.jianghongkui.lib_rules.FormatIssue;
import com.jianghongkui.lib_rules.base.BaseDetector;

import org.jetbrains.uast.UDoWhileExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UForEachExpression;
import org.jetbrains.uast.UForExpression;
import org.jetbrains.uast.UIfExpression;
import org.jetbrains.uast.ULoopExpression;

import org.jetbrains.uast.USwitchExpression;
import org.jetbrains.uast.UWhileExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheng.zhang on 2018/10/15.
 */

public class ReservedWordDetector extends BaseDetector.JavaDetector {

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        List<Class<? extends UElement>> list = new ArrayList<Class<? extends UElement>>(10);
        list.add(UForEachExpression.class);
        list.add(UForExpression.class);
        list.add(ULoopExpression.class);
        list.add(UWhileExpression.class);
        list.add(UDoWhileExpression.class);
        list.add(UIfExpression.class);
        list.add(USwitchExpression.class);
        return list;
    }

    @Override
    public UElementHandler createUastHandler(final JavaContext context) {
        return new UElementHandler() {

            @Override
            public void visitSwitchExpression(USwitchExpression node) {
                String source = node.asSourceString();
                Location ifLocation = context.getLocation(node.getSwitchIdentifier());
                int startOffset = ifLocation.getStart().getOffset();
                UExpression condition = node.getExpression();
                Location conditonLocation = context.getLocation(condition);
                String subStr = source.substring(ifLocation.getEnd().getOffset() - startOffset,
                        conditonLocation.getStart().getOffset() - startOffset);
                if (subStr.length() < 2 || !subStr.startsWith(" ")) {
                    context.report(FormatIssue.ISSUE_RESERVED_WORD, node, context.getLocation(node.getSwitchIdentifier()), FormatIssue.ISSUE_RESERVED_WORD.getBriefDescription(TextFormat.TEXT));
                }
            }




            @Override
            public void visitWhileExpression(UWhileExpression node) {
                String source = node.asSourceString();
                Location ifLocation = context.getLocation(node.getWhileIdentifier());
                int startOffset = ifLocation.getStart().getOffset();
                UExpression condition = node.getCondition();
                Location conditonLocation = context.getLocation(condition);
                String subStr = source.substring(ifLocation.getEnd().getOffset() - startOffset,
                        conditonLocation.getStart().getOffset() - startOffset);
                if (subStr.length() < 2 || !subStr.startsWith(" ")) {
                    context.report(FormatIssue.ISSUE_RESERVED_WORD, node, context.getLocation(node.getWhileIdentifier()), FormatIssue.ISSUE_RESERVED_WORD.getBriefDescription(TextFormat.TEXT));
                }
            }

            @Override
            public void visitDoWhileExpression(UDoWhileExpression node) {
                String source = node.asSourceString();
                Location ifLocation = context.getLocation(node.getDoIdentifier());
                int startOffset = ifLocation.getStart().getOffset();
                UExpression condition = node.getCondition();
                Location conditonLocation = context.getLocation(condition);
                String subStr = source.substring(ifLocation.getEnd().getOffset() - startOffset,
                        conditonLocation.getStart().getOffset() - startOffset);
                if (subStr.length() < 2 || !subStr.startsWith(" ")) {
                    context.report(FormatIssue.ISSUE_RESERVED_WORD, node, context.getLocation(node.getDoIdentifier()), FormatIssue.ISSUE_RESERVED_WORD.getBriefDescription(TextFormat.TEXT));
                }
            }

            @Override
            public void visitIfExpression(UIfExpression node) {
                if (node.isTernary()) return;
                String source = node.asSourceString();
                Location ifLocation = context.getLocation(node.getIfIdentifier());
                int startOffset = ifLocation.getStart().getOffset();
                UExpression condition = node.getCondition();
                Location conditonLocation = context.getLocation(condition);
                String subStr = source.substring(ifLocation.getEnd().getOffset() - startOffset,
                        conditonLocation.getStart().getOffset() - startOffset);
                if (subStr.length() < 2 || !subStr.startsWith(" ")) {
                    context.report(FormatIssue.ISSUE_RESERVED_WORD, node, context.getLocation(node.getIfIdentifier()), FormatIssue.ISSUE_RESERVED_WORD.getBriefDescription(TextFormat.TEXT));
                }

            }

            @Override
            public void visitForExpression(UForExpression node) {
                String source = node.asSourceString();
                Location ifLocation = context.getLocation(node.getForIdentifier());
                int startOffset = ifLocation.getStart().getOffset();
                UExpression condition = node.getCondition();
                Location conditonLocation = context.getLocation(condition);
                String subStr = source.substring(ifLocation.getEnd().getOffset() - startOffset,
                        conditonLocation.getStart().getOffset() - startOffset);
                if (subStr.length() < 2 || !subStr.startsWith(" ")) {
                    context.report(FormatIssue.ISSUE_RESERVED_WORD, node, context.getLocation(node.getForIdentifier()), FormatIssue.ISSUE_RESERVED_WORD.getBriefDescription(TextFormat.TEXT));
                }
            }

            @Override
            public void visitForEachExpression(UForEachExpression node) {
                String source = node.asSourceString();
                Location ifLocation = context.getLocation(node.getForIdentifier());
                int startOffset = ifLocation.getStart().getOffset();
                UExpression condition = node.getBody();
                Location conditonLocation = context.getLocation(condition);
                String subStr = source.substring(ifLocation.getEnd().getOffset() - startOffset,
                        conditonLocation.getStart().getOffset() - startOffset);
                if (subStr.length() < 2 || !subStr.startsWith(" ")) {
                    context.report(FormatIssue.ISSUE_RESERVED_WORD, node, context.getLocation(node.getForIdentifier()), FormatIssue.ISSUE_RESERVED_WORD.getBriefDescription(TextFormat.TEXT));
                }
            }
        };
    }
}
