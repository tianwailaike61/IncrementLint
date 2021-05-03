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

package com.twlk.lib_lint_base.extension;


import org.gradle.api.Project;

import java.io.File;

/**
 * @author twlk
 * @Date 2019-04-23
 */
public class IncrementLintLintExtension {

    public static final String NAME = "IncrementLint";

    private static String BASE_PATH;

    public com.twlk.lib_lint_base.extension.CommandExtension commandExtension;

    private String logPath;
    private File lintConfigFile;

    public IncrementLintLintExtension() {
    }

    public void init(Project project) {
        BASE_PATH = new File(project.getRootDir(), "incrementLint").getAbsolutePath();
        logPath = new File(BASE_PATH, "lint.log").getAbsolutePath();
        commandExtension = new CommandExtension(BASE_PATH);
        MLintOptions options = project.getExtensions().findByType(MLintOptions.class);
        lintConfigFile = new File(BASE_PATH, "lint.config");
        if (options != null) {
            options.htmlOutput(new File(BASE_PATH, "lint.html"));
        }
    }

    public File getLintConfigFile() {
        return lintConfigFile;
    }

    public String getLogPath() {
        return logPath;
    }
}
