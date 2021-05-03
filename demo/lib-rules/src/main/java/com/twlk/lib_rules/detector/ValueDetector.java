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

import com.android.resources.ResourceFolderType;
import com.android.tools.lint.detector.api.XmlContext;
import com.twlk.lib_rules.Utils;
import com.twlk.lib_rules.UtilsKt;
import com.twlk.lib_rules.base.BaseDetector;
import com.twlk.lib_rules.NameIssue;

import org.w3c.dom.Attr;

import java.util.Arrays;
import java.util.Collection;

/**
 * 处理与Values文件使用相关规则
 *
 * @author twlk
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
