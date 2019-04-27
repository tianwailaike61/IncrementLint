package com.jianghongkui.lint;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author hongkui.jiang
 * @Date 2019-04-27
 */
public class ChangeFileCheckTask extends DefaultTask {

    private static File BUILD_DIR;

    public static void create(Project project) {
        ChangeFileCheckTask task = project.getTasks().replace("getChangedFile", ChangeFileCheckTask.class);
    }

    @TaskAction
    void check() {
        Project project = getProject();
        BUILD_DIR = project.getBuildDir();
        LintExtension extension = project.getExtensions().findByType(LintExtension.class);
        if (extension == null) {
            return;
        }
        AbsCmd cmd = AbsCmd.get(project);
        if (cmd == null) {
            MLogger.addLog("can not find the cmd,the lint will check all of the files");
            addChangedFiles(extension.getChangedInfoFile(), getFileWithoutDir(project.getProjectDir()));
            return;
        }
        File versionFile = extension.getVersionFile();
        String lastVersion = getLastVersion(versionFile, extension);
        extension.setLastVersion(lastVersion);
        String version = null;
        try {
            version = cmd.getVersion();
        } catch (Exception e) {
            MLogger.addLog("can not get version -" + e);
        }
        MLogger.addLog("last version:%1s current version:%2s", lastVersion, version);
        if (lastVersion == null || lastVersion.isEmpty()) {
            MLogger.addLog("first check");
            addChangedFiles(extension.getChangedInfoFile(), getFileWithoutDir(project.getProjectDir()));
            setLastVersion(version, extension);
            return;
        }
        HashSet<File> set = new HashSet<>();
        //获取修改文件
        Collection<File> fileList = getFileWithoutDir(filterFiles(cmd.getFileStatusList()));
        if (fileList != null) {
            set.addAll(fileList);
        }
        if (!lastVersion.equals(version)) {
            setLastVersion(version, extension);
            Collection<File> versionChangedFile = cmd.getVersionFileList(version, lastVersion);
            MLogger.addLog("the files changed by each commit will ba added to check list -" + versionChangedFile.size());
            set.addAll(getFileWithoutDir(versionChangedFile));
        }
        addChangedFiles(extension.getChangedInfoFile(), set);
    }

    String getLastVersion(File file, LintExtension extension) {
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

    void setLastVersion(String version, LintExtension extension) {
        if (version == null) {
            return;
        }
        extension.setLastVersion(version);
    }

    void addChangedFiles(File dest, Collection<File> changeFiles) {
        if (changeFiles == null || changeFiles.size() == 0) {
            return;
        }
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(dest);
            bos = new BufferedOutputStream(fos);
            for (File file : changeFiles) {
                bos.write(file.getAbsolutePath().getBytes());
                bos.write("\n".getBytes());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.closeStream(bos, fos);
        }
    }

    private Collection<File> getFileWithoutDir(Collection<File> files) {
        if (files == null || files.size() == 0) {
            return null;
        }
        HashSet<File> set = new HashSet<>(files.size());
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
                set.add(file);
            }
        });
        return set;
    }

    private Collection<File> getFileWithoutDir(File file) {
        if (file == null) {
            return null;
        }
        if (!file.isDirectory()) {
            return Collections.singletonList(file);
        }
        if (BUILD_DIR.equals(file)) {
            return null;
        }

        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        HashSet<File> set = new HashSet<>();
        for (File f : files) {
            Collection<File> c = getFileWithoutDir(f);
            if (c != null) {
                set.addAll(c);
            }
        }
        return set;
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
