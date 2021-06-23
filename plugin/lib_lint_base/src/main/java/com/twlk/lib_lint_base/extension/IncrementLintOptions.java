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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author hongkui.jiang
 * @Date 2019-04-26
 */
public class IncrementLintOptions {

    public File htmlOutput;
    public File lintXmlPath;
    public final Set<String> disable = new HashSet<>();
    public final Set<String> enable = new HashSet<>();
    public final Set<String> check = new HashSet<>();

    public IncrementLintOptions() {
    }

    /**
     * Adds the id to the set of issues to check.
     */
    public void check(String id) {
        check.add(id);
    }

    /**
     * Adds the ids to the set of issues to check.
     */
    public void check(String... ids) {
        for (String id : ids) {
            check(id);
        }
    }

    /**
     * Adds the id to the set of issues to enable.
     */
    public void enable(String id) {
        enable.add(id);
    }

    /**
     * Adds the ids to the set of issues to enable.
     */
    public void enable(String... ids) {
        for (String id : ids) {
            enable(id);
        }
    }

    /**
     * Adds the id to the set of issues to enable.
     */
    public void disable(String id) {
        disable.add(id);
    }

    /**
     * Adds the ids to the set of issues to enable.
     */
    public void disable(String... ids) {
        for (String id : ids) {
            disable(id);
        }
    }

    public void htmlOutput(File file) {
        htmlOutput = file;
    }

    public void lintXml(File file) {
        this.lintXmlPath = file;
    }

    public File getHtmlOutput() {
        return htmlOutput;
    }
}
