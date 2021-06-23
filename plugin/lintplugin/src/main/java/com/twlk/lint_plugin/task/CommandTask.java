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

package com.twlk.lint_plugin.task;

import com.twlk.lib_lint_base.IncrementLogger;
import com.twlk.lib_lint_base.Utils;
import com.twlk.lib_lint_base.extension.CommandExtension;
import com.twlk.lib_lint_base.extension.IncrementLintExtension;
import com.twlk.lint_plugin.AbsCmd;
import com.twlk.lint_plugin.FileStatus;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author twlk
 * @Date 2019-04-23
 */
public class CommandTask extends DefaultTask {

    private static File BUILD_DIR;

    public static void create(Project project) {
        Task task = project.getTasks().findByName("getChangedFile");
        if (task == null) {
            task = project.getTasks().create("getChangedFile", CommandTask.class);
        }
    }

    @TaskAction
    void run() {
        try {
            Project project = getProject();
            BUILD_DIR = project.getBuildDir();
            IncrementLintExtension incrementLintLintExtension = project.getExtensions().findByType(IncrementLintExtension.class);
            if (incrementLintLintExtension == null) {
                return;
            }
            CommandExtension extension = incrementLintLintExtension.commandExtension;
            AbsCmd cmd = AbsCmd.get(project);
            if (cmd == null) {
                IncrementLogger.addLog("can not find the cmd,the lint will check all of the files");
                addChangedFiles(extension.changedInfoFile, getFileWithoutDir(project.getProjectDir()));
                return;
            }
            File versionFile = extension.versionFile;
            String lastVersion = getLastVersion(versionFile, extension);
            extension.setLastVersion(lastVersion);
            String version = null;
            try {
                version = cmd.getVersion();
            } catch (Exception e) {
                IncrementLogger.addLog("can not get version -" + e);
            }
            IncrementLogger.addLog("last version:%1s current version:%2s", lastVersion, version);
            if (lastVersion == null || lastVersion.isEmpty()) {
                IncrementLogger.addLog("first check");
                Collection<String> collections = getFileWithoutDir(project.getProjectDir());
                addChangedFiles(extension.changedInfoFile, collections);
                setLastVersion(version, extension);
                return;
            }

            HashSet<String> set = new HashSet<>();
            //获取修改文件
            Collection<String> fileList = getFileWithoutDir(filterFiles(cmd.getFileStatusList()));
            if (fileList != null) {
                set.addAll(fileList);
            }
            if (!lastVersion.equals(version)) {
                setLastVersion(version, extension);
                Collection<File> versionChangedFile = cmd.getVersionFileList(version, lastVersion);
                IncrementLogger.addLog("the files changed by each commit will ba added to check list -" + versionChangedFile.size());

                Collection<String> cs = getFileWithoutDir(versionChangedFile);
                if (cs != null && !cs.isEmpty()) {
                    set.addAll(cs);
                }
            }
            addChangedFiles(extension.changedInfoFile, set);
        } catch (RuntimeException e) {
            IncrementLogger.addLog("CommandTask Exception--" + Utils.printException(e));
            throw e;
        } finally {
            IncrementLogger.flush();
        }
    }

    String getLastVersion(File file, CommandExtension extension) {
        if (extension.getLastVersion() != null) {
            return extension.getLastVersion();
        }
        StringBuilder builder = new StringBuilder();
        Utils.readFile(file, line -> {
            if (line != null && line.startsWith("version")) {
                builder.append(line.substring(8).trim());
                return false;
            }
            return true;
        });
        if (builder.length() == 0) {
            return null;
        }
        return builder.toString();
    }

    void setLastVersion(String version, CommandExtension extension) {
        if (version == null) {
            return;
        }
        extension.setLastVersion(version);
    }

    void addChangedFiles(File dest, Collection<String> changeFiles) {
        if (changeFiles == null || changeFiles.size() == 0) {
            try {
                Utils.saveFile(" ", dest);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        StringBuilder builder = new StringBuilder();
        changeFiles.forEach(file -> builder.append(file).append("\n"));
        try {
            Utils.saveFile(builder.toString(), dest);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Collection<String> getFileWithoutDir(Collection<File> files) {
        if (files == null || files.size() == 0) {
            return Collections.EMPTY_SET;
        }
        HashSet<String> set = new HashSet<>(files.size());
        files.forEach(file -> {
            if (!file.exists()) {
                return;
            }
            if (file.isDirectory()) {
                if (BUILD_DIR.equals(file)) {
                    return;
                }
                set.addAll(getFileWithoutDir(file));
            } else {
                set.add(file.getAbsolutePath());
            }
        });
        return set;
    }

    private Collection<String> getFileWithoutDir(File file) {
        if (file == null) {
            return Collections.emptyList();
        }

        if (file.isDirectory()) {
            if (BUILD_DIR.equals(file)) {
                return Collections.emptyList();
            }
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                return Collections.emptyList();
            }
            return getFileWithoutDir(Arrays.asList(files));
        }

        return Collections.singletonList(file.getAbsolutePath());

    }

    /**
     * 过滤需要检查的文件
     */
    private List<File> filterFiles(Collection<FileStatus> fileStatusList) {
        if (fileStatusList == null || fileStatusList.isEmpty()) {
            return null;
        }
        List<File> fileList = new ArrayList<>(fileStatusList.size());
        for (FileStatus status : fileStatusList) {
            File file = new File(status.getPath());
            if (status.getStatus() != FileStatus.FStatus.DELETE && file.exists()) {
                fileList.add(file);
            }
        }
        return fileList;
    }
}
