package com.jianghongkui.lib_rules.detector;

import com.android.resources.ResourceFolderType;
import com.android.tools.lint.detector.api.XmlContext;
import com.jianghongkui.lib_rules.Utils;
import com.jianghongkui.lib_rules.UtilsKt;
import com.jianghongkui.lib_rules.base.BaseDetector;
import com.jianghongkui.lib_rules.NameIssue;

import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 处理与Values文件使用相关规则
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
public class ValueDetector extends BaseDetector.XmlDetector {

    @Override
    public boolean appliesTo(ResourceFolderType folderType) {
        return ResourceFolderType.VALUES == folderType;
    }

    @Override
    public Collection<String> getApplicableAttributes() {
        return Arrays.asList("name");
    }

    @Override
    public void visitAttribute(XmlContext context, Attr attribute) {
        super.visitAttribute(context, attribute);
        String value = attribute.getValue();
        if (Utils.isEmpty(value)) {
            return;
        }

        String msg = null;
        String result = UtilsKt.delete(value, "\\d+|[a-z]|_");
        if (Utils.isEmpty(result)) {
            if (value.startsWith("_") || value.endsWith("_")) {
                msg = "不能以下划线开头或结尾";
            }
            //数字开头已有
        } else {
            if (UtilsKt.delete(result, "[A-Z]").length() != result.length()) {
                msg = "不能包含大写字母";
            } else {
                msg = String.format("不能包含%1s", UtilsKt.getResultStr(result));
            }
        }
        if (!Utils.isEmpty(msg))
            context.report(NameIssue.ISSUE_NAME,
                    attribute, context.getLocation(attribute), msg);
    }
}
