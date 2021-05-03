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
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Position;
import com.android.tools.lint.detector.api.TextFormat;
import com.intellij.psi.PsiElement;
import com.twlk.lib_rules.FormatIssue;
import com.twlk.lib_rules.base.BaseDetector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UParameter;
import org.jetbrains.uast.UTypeReferenceExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheng.zhang on 2018/10/16.
 * 处理构造函数规则
 */

public class ConstructorDetector extends BaseDetector.JavaDetector {

    ArrayList<UMethod> uMethods = new ArrayList<>();

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        List<Class<? extends UElement>> list = new ArrayList<Class<? extends UElement>>(2);
        list.add(UClass.class);
        return list;
    }

    @Override
    public UElementHandler createUastHandler(final JavaContext context) {
        return new UElementHandler() {

            @Override
            public void visitClass(@NotNull UClass node) {
                uMethods.clear();
                UMethod[] methods = node.getMethods();
                for (UMethod method : methods) {
                    boolean isNotIgnore = isNotIgnore(method);
                    if (method.isConstructor() && isNotIgnore) {
                        uMethods.add(method);
                    }
                }
                if (methods != null && methods.length > 0 && uMethods != null && uMethods.size() > 0) {
                    UMethod method = methods[0];
                    UMethod uMethod = uMethods.get(0);

                    if (!method.isConstructor()) {
                        context.report(FormatIssue.ISSUE_CONSTRUCTOR_SCHEDULE_RULE, node, context.getLocation(uMethod), FormatIssue.ISSUE_CONSTRUCTOR_SCHEDULE_RULE.getBriefDescription(TextFormat.TEXT));
                        return;
                    }

                }

                for (int a = 0; a < uMethods.size(); a++) {
                    UMethod uMethod = uMethods.get(a);
                    List<UParameter> uastParameters = uMethod.getUastParameters();

                    if (uMethods.size() > 1 && uastParameters != null && a > 0 && uastParameters.size() < uMethods.get(a - 1).getUastParameters().size()) {
                        context.report(FormatIssue.ISSUE_CONSTRUCTOR_ORDER_RULE, node, context.getLocation(uMethod), FormatIssue.ISSUE_CONSTRUCTOR_ORDER_RULE.getBriefDescription(TextFormat.TEXT));
                        return;
                    }

                    if (uMethods.size() > 1 && a > 0) {
                        UMethod beforeMethod = uMethods.get(a - 1);
                        Location beforeLocation = context.getLocation(beforeMethod);
                        Position beforeEnd = beforeLocation.getEnd();
                        int beforeLine = beforeEnd.getLine();
                        Location location = context.getLocation(uMethod);
                        Position start = location.getStart();
                        int line = start.getLine();
                        if (line - beforeLine > 5) {
                            context.report(FormatIssue.ISSUE_CONSTRUCTOR_SCHEDULE_RULE, node, context.getLocation(uMethod), FormatIssue.ISSUE_CONSTRUCTOR_SCHEDULE_RULE.getBriefDescription(TextFormat.TEXT));
                        }
                    }

                }

            }

            private boolean isNotIgnore(UMethod uMethod) {
                List<UParameter> uastParameters = uMethod.getUastParameters();
                boolean isNotIgnore = true;
                if (uastParameters != null && uastParameters.size() == 1) {
                    UParameter uParameter = uastParameters.get(0);
                    UTypeReferenceExpression typeReference = uParameter.getTypeReference();
                    PsiElement javaPsi = typeReference.getJavaPsi();
                    String text1 = javaPsi.getText();
                    if ("Parcel".equals(text1)) {
                        isNotIgnore = false;
                    }
                }
                return isNotIgnore;
            }

        }
                ;
    }

}
