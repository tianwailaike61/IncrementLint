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
import com.twlk.lib_rules.base.BaseDetector;

import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UField;
import org.jetbrains.uast.ULocalVariable;
import org.jetbrains.uast.UTypeReferenceExpression;

import java.util.ArrayList;
import java.util.List;

import static com.twlk.lib_rules.UsageIssue.ISSUE_SIMPLE_DATE_FORMAT;

/**
 * 处理与SimpleDateFormat使用相关规则
 *
 * @author twlk
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
