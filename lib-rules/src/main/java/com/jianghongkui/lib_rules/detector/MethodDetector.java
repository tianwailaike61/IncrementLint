package com.jianghongkui.lib_rules.detector;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Location;
import com.jianghongkui.lib_rules.Utils;
import com.jianghongkui.lib_rules.UtilsKt;
import com.jianghongkui.lib_rules.base.BaseDetector;
import com.jianghongkui.lib_rules.AnalysisIssue;
import com.jianghongkui.lib_rules.CommentIssue;
import com.jianghongkui.lib_rules.NameIssue;

import org.jetbrains.uast.UComment;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理与函数相关规则
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
public class MethodDetector extends BaseDetector.JavaDetector {

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        List<Class<? extends UElement>> list = new ArrayList<Class<? extends UElement>>(2);
        list.add(UMethod.class);
        list.add(UParameter.class);
        return list;
    }

    @Override
    public UElementHandler createUastHandler(final JavaContext context) {
        return new UElementHandler() {

            @Override
            public void visitMethod(UMethod node) {
                if (checkMethod(context, node)) return;
                if (checkLines(context, node)) return;
                if (checkComment(context, node)) return;
            }

            @Override
            public void visitParameter(UParameter node) {
                checkParam(context, node);
            }


        };
    }

    private boolean checkComment(JavaContext context, UMethod node) {
        List<UComment> comments = node.getComments();
        int size = comments != null ? comments.size() : 0;
        if (size == 0) {
            return false;
        }
        Location methodLocation = context.getLocation(node);
        UComment comment = comments.get(0);
        Location commentLocation = context.getNameLocation(comment.getJavaPsi());
        if (commentLocation.getStart().getLine() == methodLocation.getStart().getLine() && !comment.getText().startsWith("/**")) {
            context.report(CommentIssue.ISSUE_METHOD, comment, commentLocation, "函数注释格式有误");
            return true;
        }
        return false;
    }

    private boolean checkLines(JavaContext context, UMethod node) {
        Location location = context.getLocation((UElement) node);
        int count = location.getEnd().getLine() - location.getStart().getLine();
        if (count > 50) {
            context.report(AnalysisIssue.ISSUE_METHOD_LINES, location, String.format("该方法有%1d行,超过50行", count));
            return true;
        }
        return false;
    }

    private void checkParam(JavaContext context, UParameter node) {
        checkLowerCamelCase(context, node, node.getName(), NameIssue.ISSUE_PARAMS);
    }

    private boolean checkMethod(JavaContext context, UMethod node) {
        if (node.isConstructor()) {
            return false;
        }
        return checkLowerCamelCase(context, node, node.getName(), NameIssue.ISSUE_METHOD);
    }

    private boolean checkLowerCamelCase(JavaContext context, UElement node, String name, Issue issue) {
        String result = UtilsKt.checkLowerCamelCase(name);
        if (!Utils.isEmpty(result)) {
            context.report(issue, node, context.getLocation(node), String.format("%1s%2s", name, result));
            return true;
        }
        return false;
    }
}
