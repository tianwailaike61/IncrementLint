package com.jianghongkui.lib_rules.detector;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.JavaContext;
import com.jianghongkui.lib_rules.Utils;
import com.jianghongkui.lib_rules.base.BaseDetector;

import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.USwitchClauseExpressionWithBody;
import org.jetbrains.uast.USwitchExpression;

import java.util.ArrayList;
import java.util.List;

import static com.jianghongkui.lib_rules.UsageIssue.ISSUE_BREAK;
import static com.jianghongkui.lib_rules.UsageIssue.ISSUE_DEFAULT;

/**
 * 处理与Switch使用相关规则
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
public class SwitchUsageDetector extends BaseDetector.JavaDetector {

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        List<Class<? extends UElement>> list = new ArrayList<Class<? extends UElement>>(1);
        list.add(USwitchExpression.class);
        return list;
    }

    @Override
    public UElementHandler createUastHandler(final JavaContext context) {
        return new UElementHandler() {

            @Override
            public void visitSwitchExpression(USwitchExpression node) {
                String s;
                List<UExpression> expressionList = node.getBody().getExpressions();
                int count = expressionList.size();
                UExpression expression;
                USwitchClauseExpressionWithBody expressionWithBody;
                for (int i = count - 1; i >= 0; i--) {
                    expression = expressionList.get(i);
                    if (expression instanceof USwitchClauseExpressionWithBody) {
                        expressionWithBody = (USwitchClauseExpressionWithBody) expression;
                        if (i == count - 1) {
                            boolean isContainElse = false;
                            for (UExpression uExpression : expressionWithBody.getCaseValues()) {
                                if ("else".equals(uExpression.asRenderString())) {
                                    isContainElse = true;
                                    break;
                                }
                            }
                            if (isContainElse) continue;
                            context.report(ISSUE_DEFAULT, node, context.getNameLocation(node),
                                    "Switch语句必须包含default");
                        }
                        s = expressionWithBody.getBody().asRenderString();
                        if (Utils.isEmpty(s) || (!s.contains("break")
                                && !s.contains("return"))) {
                            context.report(ISSUE_BREAK, expression, context.getLocation(expression),
                                    "Switch语句必须包含break或return");
                            continue;
                        }
                        return;
                    }
                }
            }

        };
    }
}
