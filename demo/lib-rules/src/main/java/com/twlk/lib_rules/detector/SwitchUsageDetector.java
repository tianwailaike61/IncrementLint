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

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.JavaContext;
import com.twlk.lib_rules.Utils;
import com.twlk.lib_rules.base.BaseDetector;

import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.USwitchClauseExpressionWithBody;
import org.jetbrains.uast.USwitchExpression;

import java.util.ArrayList;
import java.util.List;

import static com.twlk.lib_rules.UsageIssue.ISSUE_BREAK;
import static com.twlk.lib_rules.UsageIssue.ISSUE_DEFAULT;

/**
 * 处理与Switch使用相关规则
 *
 * @author twlk
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
