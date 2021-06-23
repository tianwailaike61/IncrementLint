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

package com.twlk.lib_lint_base;

import com.android.tools.lint.gradle.api.LintExecutionRequest;
import com.twlk.lib_lint_base.extension.CommandExtension;
import com.twlk.lib_lint_base.extension.IncrementLintExtension;
import com.twlk.lib_lint_base.extension.IncrementLintOptions;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

public class LintTaskExecutor {

    private Class<?> creatorClz;
    private LintExecutionRequest request;
    private String lintClassPath;

    public LintTaskExecutor() {
    }

    public void execute(Project project, ILintRunner runner) {
        IncrementLintExtension incrementLintExtension = project.getExtensions().findByType(IncrementLintExtension.class);
        if (incrementLintExtension == null) {
            return;
        }

        CommandExtension extension = incrementLintExtension.commandExtension;
        Collection<File> checkFileList = getChangedFiles(extension.changedInfoFile, extension.versionFile);
        if (checkFileList != null && !checkFileList.isEmpty()) {
            IncrementLogger.addLog("the size of files that's need to be checked is %1d", checkFileList.size());
        } else {
            IncrementLogger.addLog("not need check");
            IncrementLogger.flush();
            return;
        }
        FileCollection lintClassPath = project.getConfigurations().getByName(this.lintClassPath);
        LintResultCollector collector = new LintResultCollector(checkFileList);
        runner.runLint(project.getGradle(), request, lintClassPath, collector, creatorClz);
        if (!collector.hasError()) {
            writeResults(extension.versionFile, extension.getLastVersion(), null);
            IncrementLogger.flush();
        } else {
            Collection<String> strings = collector.getFailedFiles();
            IncrementLogger.addLog("the problem happened, lastVersion:%1s", extension.getLastVersion());
            writeResults(extension.versionFile, extension.getLastVersion(), strings);
            IncrementLintOptions options = project.getExtensions().findByType(IncrementLintOptions.class);
            IncrementLogger.flush();
            throw new GradleException("there are " + strings.size() + " incorrect files found by Lint,you can see more info in " +
                    options.getHtmlOutput().toURI());
        }
    }

    private static Collection<File> getChangedFiles(File... dest) {
        if (dest == null || dest.length == 0) {
            return null;
        }
        HashSet<File> list = new HashSet<>();
        for (File file : dest) {
            Utils.readFile(file, line -> {
                File f = new File(line);
                if (f.isFile()) {
                    list.add(f);
                }
                return true;
            });
        }
        return list;
    }

    private static void writeResults(File dest, String version, Collection<String> cs) {
        StringBuilder builder = new StringBuilder();
        if (version != null) {
            builder.append("version:").append(version).append("\n");
        }
        if (cs != null && cs.size() > 0) {
            cs.forEach(s -> builder.append(s).append("\n"));
        }
        try {
            Utils.saveFile(builder.toString(), dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCreatorClz(Class<?> creatorClz) {
        this.creatorClz = creatorClz;
    }

    public void setRequest(LintExecutionRequest request) {
        this.request = request;
    }

    public void setLintClassPath(String lintClassPath) {
        this.lintClassPath = lintClassPath;
    }
}
