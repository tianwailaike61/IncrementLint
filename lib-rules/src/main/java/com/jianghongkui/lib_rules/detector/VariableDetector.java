package com.jianghongkui.lib_rules.detector;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiJavaToken;
import com.jianghongkui.lib_rules.Utils;
import com.jianghongkui.lib_rules.base.BaseDetector;
import com.jianghongkui.lib_rules.CommentIssue;
import com.jianghongkui.lib_rules.NoChineseIssue;

import org.jetbrains.uast.UComment;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UField;
import org.jetbrains.uast.ULiteralExpression;
import org.jetbrains.uast.ULocalVariable;
import org.jetbrains.uast.UTypeReferenceExpression;
import org.jetbrains.uast.UastLiteralUtils;

import com.jianghongkui.lib_rules.UtilsKt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jianghongkui.lib_rules.NameIssue.ISSUE_ARRAY;
import static com.jianghongkui.lib_rules.NameIssue.ISSUE_VAL;
import static com.jianghongkui.lib_rules.NameIssue.ISSUE_VAR;

/**
 * 处理常量、变量相关规则
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
public class VariableDetector extends BaseDetector.JavaDetector {

    private List<String> normalTyps = Arrays.asList("int", "long", "double", "float", "short",
            "char", "boolean", "byte", "java.lang.Integer", "java.lang.Long", "java.lang.Float",
            "java.lang.Short", "java.lang.Character", "java.lang.Boolean", "java.lang.Byte",
            "java.lang.String");

    @Override
    public void beforeCheckFile(Context context) {
        super.beforeCheckFile(context);
    }

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        List<Class<? extends UElement>> list = new ArrayList<Class<? extends UElement>>(3);
        list.add(ULocalVariable.class);
        list.add(UField.class);
        list.add(ULiteralExpression.class);
        return list;
    }

    @Override
    public UElementHandler createUastHandler(final JavaContext context) {
        return new UElementHandler() {

            @Override
            public void visitField(UField node) {
                if (node.isFinal() && node.isStatic()) {
                    UTypeReferenceExpression expression = node.getTypeReference();
                    if ((node.getPsi() instanceof PsiEnumConstant) || (expression != null
                            && normalTyps.contains(expression.toString().trim()))) {
                        checkVal(context, node);
                    }
                    return;
                }
                if (checkVar(context, node)) return;
                if (checkFieldComment(context, node)) return;
                if (checkFieldLocation(context, node)) return;
                if (checkArrayName(context, node)) return;
            }

            @Override
            public void visitLocalVariable(ULocalVariable node) {
                checkVar(context, node);
            }

            @Override
            public void visitLiteralExpression(ULiteralExpression expression) {
                String string = UastLiteralUtils.getValueIfStringLiteral(expression);
                if (Utils.isEmpty(string)) {
                    return;
                }

                if (UtilsKt.isChinese(string)) {
                    context.report(NoChineseIssue.ISSUE_JAVA, expression, context.getLocation(expression),
                            "禁止在java文件中直接使用中文");
                }

            }
        };
    }

    private boolean checkFieldLocation(JavaContext context, UField node) {
//        if (firstMethodStartLine) {
//            context.report(FormatIssue.ISSUE_FIELD, node, context.getLocation(node), "成员变量需要放在函数前");
//        }
        return false;
    }

    private boolean checkArrayName(JavaContext context, UField node) {
        if (!node.asSourceString().contains("[]")) return false;
        if (node.isPsiValid()) {
            boolean isId = false;
            for (PsiElement element : node.getChildren()) {
                if (element instanceof PsiIdentifier) {
                    isId = true;
                    continue;
                }
                if (element instanceof PsiJavaToken && isId &&
                        ((PsiJavaToken) element).getTokenType() == JavaTokenType.LBRACKET) {
                    context.report(ISSUE_ARRAY, node, context.getLocation(node), "申明数组需将[]放在变量名左侧");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkVar(JavaContext context, UField node) {
        return checkLowerCamelCase(context, node, node.getName(), ISSUE_VAR);
    }

    private boolean checkVar(JavaContext context, ULocalVariable node) {
        return checkLowerCamelCase(context, node, node.getName(), ISSUE_VAR);
    }

    private boolean checkVal(JavaContext context, UField node) {
        String name = node.getName();
        if ("serialVersionUID".equals(name)) {
            return false;
        }
        String s = UtilsKt.delete(name, "\\d+|[A-Z]|_");
        if (!Utils.isEmpty(s)) {
            StringBuilder builder = new StringBuilder("常量不能包含");
            String strWithoutLowerCase = UtilsKt.delete(s, "[a-z]");
            if (s.length() > strWithoutLowerCase.length()) {
                builder.append("小写字母");
            }
            String result = UtilsKt.getResultStr(strWithoutLowerCase);
            if (!Utils.isEmpty(result)) {
                if (s.length() > strWithoutLowerCase.length())
                    builder.append("和");
                builder.append(result);
            }
            context.report(ISSUE_VAL, (UElement) node, context.getLocation((UElement) node),
                    builder.toString());
            return true;
        }
        if (name.startsWith("_") || name.endsWith("_")) {
            context.report(ISSUE_VAL, (UElement) node, context.getLocation((UElement) node),
                    "常量不能以下划线开头或结尾");
            return true;
        }
        return false;
    }

    private boolean checkFieldComment(JavaContext context, UField node) {
        List<UComment> comments = node.getComments();
        int size = comments != null ? comments.size() : 0;
        if (size == 0) {
            return false;
        } else if (size == 1) {
            UComment comment = comments.get(0);
            String s = comment.getText();
            if (getCommentLines(s) > 1 || !s.startsWith("//")) {
                context.report(CommentIssue.ISSUE_VAL, context.getLocation(comment), "成员变量必须使用格式为'//内容'的单行注释");
                return true;
            }
        } else {
            context.report(CommentIssue.ISSUE_VAL, context.getLocation(node), "存在多条注释");
            return true;
        }
        return false;
    }

    private int getCommentLines(String commentStr) {
        if (Utils.isEmpty(commentStr))
            return -1;
        return commentStr.split("\n").length;
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
