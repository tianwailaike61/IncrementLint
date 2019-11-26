package com.jianghongkui.lib_rules.detector;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Position;
import com.android.tools.lint.detector.api.TextFormat;
import com.jianghongkui.lib_rules.FormatIssue;
import com.jianghongkui.lib_rules.base.BaseDetector;

import org.jetbrains.uast.UBinaryExpression;

import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UIfExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UPostfixExpression;
import org.jetbrains.uast.UPrefixExpression;
import org.jetbrains.uast.UUnaryExpression;
import org.jetbrains.uast.UastBinaryOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheng.zhang on 2018/10/15.
 * 任何二目、三目运算符的左右两边都需要加一个 空格。
 */

public class OperatorFormatDetector extends BaseDetector.JavaDetector {

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        List<Class<? extends UElement>> list = new ArrayList<Class<? extends UElement>>(4);
        list.add(UBinaryExpression.class);
        list.add(UPostfixExpression.class);
        list.add(UPrefixExpression.class);
        list.add(UIfExpression.class);
        return list;
    }

    @Override
    public UElementHandler createUastHandler(final JavaContext context) {
        return new UElementHandler() {

            @Override
            public void visitIfExpression(UIfExpression node) {
                if (node.isTernary()) {
                    checkTernaryOperator(context, node);
                }
            }

            @Override
            public void visitBinaryExpression(UBinaryExpression node) {
                UExpression leftOperand = node.getLeftOperand();
                String source = node.asSourceString();
                UExpression rightOperand = node.getRightOperand();
                UastBinaryOperator operator = node.getOperator();
                String text = operator.getText();
                Location leftLocation = context.getLocation(leftOperand);
                Location rightLocation = context.getLocation(rightOperand);
                int startOffset = leftLocation.getStart().getOffset();
                String subStr = source.substring(leftLocation.getEnd().getOffset() - startOffset,
                        rightLocation.getStart().getOffset() - startOffset);
                int length = subStr.length();
                if (subStr.contains("\n") || subStr.contains("\r\n")) return;

                int textLength = text.length();
                if (text.equals("!==") || text.equals("===")) {
                    textLength--;
                }
                if (length != textLength + 2 || !subStr.startsWith(" ") || !subStr.endsWith(" ")) {
                    context.report(FormatIssue.ISSUE_OPERATOR_RULE, node, context.getLocation(node), FormatIssue.ISSUE_OPERATOR_RULE.getBriefDescription(TextFormat.TEXT));
                }
              /*  if (i>=0){
                    String substring = substring2.substring(0,i);
                    if(!substring.matches("^\\s+.*\\s+$")){
                        context.report(FormatIssue.ISSUE_OPERATOR_RULE, node, context.getLocation(node), FormatIssue.ISSUE_OPERATOR_RULE.getBriefDescription(TextFormat.TEXT));
                    }
                }*/
            }

            @Override
            public void visitPrefixExpression(UPrefixExpression node) {
                checkUnaryOperator(context, node, -1);
            }

            @Override
            public void visitPostfixExpression(UPostfixExpression node) {
                checkUnaryOperator(context, node, 1);
            }
        };
    }

    private void checkUnaryOperator(JavaContext context, UUnaryExpression expression, int type) {
        Location nodeLocation = context.getLocation(expression);
        Location operandLocation = context.getLocation(expression.getOperand());

        String operatorStr = expression.getOperator().getText();
        boolean flag;
        String msg;
        if (type == 1) {
            flag = nodeLocation.getEnd().getColumn() - operatorStr.length() != operandLocation.getEnd().getColumn();
            msg = "%1s前不能有空格";
        } else {
            flag = nodeLocation.getStart().getColumn() + operatorStr.length() != operandLocation.getStart().getColumn();
            msg = "%1s后不能有空格";
        }
        if (nodeLocation.getEnd().getLine() == operandLocation.getEnd().getLine() && flag) {
            context.report(FormatIssue.ISSUE_UNARY_OPERATOR_RULE, expression, nodeLocation,
                    String.format(msg, operatorStr));
        }
    }

    private void checkTernaryOperator(JavaContext context, UIfExpression node) {
        String text = node.asSourceString();
        Location conditionLocation = context.getLocation(node.getCondition());
        Location thenExpressionLocation = context.getLocation(node.getThenExpression());

        int startOffset = conditionLocation.getStart().getOffset();
        String subStr1 = text.substring(conditionLocation.getEnd().getOffset() - startOffset,
                thenExpressionLocation.getStart().getOffset() - startOffset);

        if (conditionLocation.getEnd().getLine() == thenExpressionLocation.getStart().getLine()) {
            if (subStr1.length() != 3 || subStr1.charAt(0) != ' ' || subStr1.charAt(2) != ' ') {
                context.report(FormatIssue.ISSUE_TERNARY_OPERATOR_RULE, node, context.getLocation(node),
                        "?前后需要一个空格");
                return;
            }
        } else {
            if (subStr1.startsWith(" ?") || subStr1.endsWith("? "))
                return;
            context.report(FormatIssue.ISSUE_TERNARY_OPERATOR_RULE, node, context.getLocation(node),
                    "?前后需要一个空格");

        }

        Location elseExpressionLocation = context.getLocation(node.getElseExpression());
        String subStr2 = text.substring(thenExpressionLocation.getEnd().getOffset() - startOffset,
                elseExpressionLocation.getStart().getOffset() - startOffset);
        if (thenExpressionLocation.getEnd().getLine() == elseExpressionLocation.getStart().getLine()) {
            if (subStr2.length() != 3 || subStr2.charAt(0) != ' ' || subStr2.charAt(2) != ' ') {
                context.report(FormatIssue.ISSUE_TERNARY_OPERATOR_RULE, node, context.getLocation(node),
                        ":前后需要一个空格");
            }
        } else {
            if (subStr2.startsWith(" :") || subStr2.endsWith(": ")) return;
            context.report(FormatIssue.ISSUE_TERNARY_OPERATOR_RULE, node, context.getLocation(node),
                    ":前后需要一个空格");
        }
    }

}
