package com.jianghongkui.lib_rules.detector;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.JavaContext;
import com.jianghongkui.lib_rules.base.BaseDetector;

import org.jetbrains.uast.UBlockExpression;
import org.jetbrains.uast.UElement;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理与代码格式相关规则
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
public class FormatDetector extends BaseDetector implements BaseDetector.JavaScanner, BaseDetector.XmlScanner {
    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        List<Class<? extends UElement>> list = new ArrayList<Class<? extends UElement>>(2);
        list.add(UElement.class);
        return list;
    }

    @Override
    public UElementHandler createUastHandler(JavaContext context) {
        return new UElementHandler() {
            @Override
            public void visitElement(UElement node) {

            }

            @Override
            public void visitBlockExpression(UBlockExpression node) {
            }
        };
    }
}