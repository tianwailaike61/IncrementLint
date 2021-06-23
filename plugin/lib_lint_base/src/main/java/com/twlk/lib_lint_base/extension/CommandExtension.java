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

/**
 * @author hongkui.jiang
 * @Date 2019-04-23
 */
public class CommandExtension {

    private final String basePath;

    public File versionFile;
    public File changedInfoFile;
    private String lastVersion;

    public CommandExtension(String basePath) {
        this.basePath = basePath;
        versionFile = new File(basePath, "CommitVersion.txt");
        changedInfoFile = new File(basePath, "changes.txt");
    }

    public String getLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(String lastVersion) {
        this.lastVersion = lastVersion;
    }


//    public void addCheckFile(File file) {
//        if (checkFileList == null) {
//            checkFileList = new HashSet<>();
//        }
//        checkFileList.add(file);
//    }
//
//    public void addCheckFiles(Collection<File> files) {
//        if (files == null || files.size() == 0) {
//            return;
//        }
//        if (checkFileList == null) {
//            checkFileList = new HashSet<>(files.size());
//        }
//        checkFileList.addAll(files);
//    }
//
//    public void clearCheckFiles() {
//        if (checkFileList != null) {
//            checkFileList.clear();
//        }
//    }
}
