package com.jianghongkui.lib_rules.detector;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.JavaContext;
import com.jianghongkui.lib_rules.base.BaseDetector;

import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UField;
import org.jetbrains.uast.ULocalVariable;
import org.jetbrains.uast.UTypeReferenceExpression;

import java.util.ArrayList;
import java.util.List;

import static com.jianghongkui.lib_rules.UsageIssue.ISSUE_SIMPLE_DATE_FORMAT;

/**
 * 处理与SimpleDateFormat使用相关规则
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
public class SimpleDateFormatUsageDetector extends BaseDetector.JavaDetector {

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        List<Class<? extends UElement>> types = new ArrayList<Class<? extends UElement>>(2);
        types.add(UField.class);
        types.add(ULocalVariable.class);
        return types;
    }

    @Override
    public UElementHandler createUastHandler(final JavaContext context) {
        return new UElementHandler() {

            @Override
            public void visitLocalVariable(ULocalVariable node) {
                UTypeReferenceExpression typeReferenceExpression = node.getTypeReference();
                if (typeReferenceExpression == null)
                    return;
                if ("java.text.SimpleDateFormat".equals(typeReferenceExpression.toString()) && node.isStatic()) {
                    context.report(ISSUE_SIMPLE_DATE_FORMAT, (UElement) node, context.getLocation((UElement) node), "SimpleDateFormatUsage不能定义为static");
                }
            }

            @Override
            public void visitField(UField node) {
                UTypeReferenceExpression typeReferenceExpression = node.getTypeReference();
                if (typeReferenceExpression == null)
                    return;
                if ("java.text.SimpleDateFormat".equals(typeReferenceExpression.toString()) && node.isStatic()) {
                    context.report(ISSUE_SIMPLE_DATE_FORMAT, node, context.getLocation(node), "SimpleDateFormatUsage不能定义为static");
                }
            }
        };
    }
}
