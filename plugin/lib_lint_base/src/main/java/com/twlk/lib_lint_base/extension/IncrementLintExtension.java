/*
 * MIT License
 *
 * Copyright (c) 2021 tianwailaike61
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

package com.twlk.lib_lint_base.extension;


import org.gradle.api.Project;

import java.io.File;

/**
 * @author hongkui.jiang
 * @Date 2019-04-23
 */
public class IncrementLintExtension {

    private static String BASE_PATH;

    public CommandExtension commandExtension;

    private String logPath;
    private String lintXmlPath;
    private File lintConfigFile;
    public boolean increment = true;

    public IncrementLintExtension() {
    }

    public void init(Project project, String prefix) {
        BASE_PATH = new File(project.getRootDir(), prefix).getAbsolutePath();
        logPath = new File(BASE_PATH, prefix + "Lint.log").getAbsolutePath();
        commandExtension = new CommandExtension(BASE_PATH);
        IncrementLintOptions options = project.getExtensions().findByType(IncrementLintOptions.class);
        File xmlFile = new File(BASE_PATH, "lint.xml");
        lintXmlPath = xmlFile.getAbsolutePath();
        lintConfigFile = new File(BASE_PATH, "lint.config");
        if (options != null) {
            options.lintXml(xmlFile);
            options.htmlOutput(new File(BASE_PATH, prefix + "Lint.html"));
        }
    }

    public File getLintConfigFile() {
        return lintConfigFile;
    }

    public String getLogPath() {
        return logPath;
    }

    public String getLintXmlPath() {
        return lintXmlPath;
    }

    public void setIncrement(boolean flag) {
        increment = flag;
    }

    public boolean isIncrement() {
        return increment;
    }
}
