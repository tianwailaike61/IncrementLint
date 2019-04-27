package com.jianghongkui.lint;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 抽象命令类型
 *
 * @author hongkui.jiang
 * @Date 2019/3/13
 */
public abstract class AbsCmd {

    File projectDir;

    public final static int GIT = 1;
    public final static int SVN = 2;

    AbsCmd(File projectDir) {
        this.projectDir = projectDir;
    }

    /**
     * 解析svn或git命令运行输出
     */
    public abstract Collection<FileStatus> getFileStatusList();

    public abstract String getVersion();

    public abstract Collection<File> getVersionFileList(String startVersion, String endVersion);

    /**
     * 将SVN 或 GIT中的文件状态表示方式统一
     */
    FileStatus.FStatus getIntStatus(String s) {
        switch (s) {
            case "D":
            case "!":
            case "C":
                return FileStatus.FStatus.DELETE;
            case "A":
            case "?":
                return FileStatus.FStatus.ADD;
            case "M":
            case "??":
            case "R":
            default:
                return FileStatus.FStatus.MODIFY;
        }
    }

    public static AbsCmd get(Project project) {
        int toolType = checkToolType(project);
        MLogger.addLog("The type of the tool is " + toolType);
        if (toolType == -1) {
            return null;
        }
        AbsCmd type = null;
        switch (toolType) {
            case GIT:
                type = new Git(project.getProjectDir());
                break;
            case SVN:
                type = new Svn(project.getProjectDir());
                break;
        }
        return type;
    }

    /**
     * 检查使用的仓库管理工具
     */
    private static int checkToolType(Project project) {
        String path = project.getProjectDir().getAbsolutePath();
        if (check(String.format(Git.CHANGED_COM, path))) {
            return GIT;
        } else if (check(String.format(Svn.CHANGED_COM, path))) {
            return SVN;
        } else {
            return -1;
        }
    }

    private static boolean check(String cmd) {
        try {
            Utils.exec(cmd);
            return true;
        } catch (GradleException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * svn 命令
     */
    private static class Svn extends AbsCmd {
        private final static String CHANGED_COM = "svn status %1s";
        private final static String VERSION_COM = "svn info";
        private final static String VERSION_ChANGE_COM = "svn log -r %1s:%2s -v -q";
        private String relativeUrl = "";

        Svn(File projectDir) {
            super(projectDir);
        }

        @Override
        public Collection<FileStatus> getFileStatusList() {
            List<FileStatus> list = new ArrayList<>();
            Utils.runCommand(String.format(CHANGED_COM, projectDir.getAbsolutePath()), line -> {
                if (line.length() < 8) {
                    return true;
                }
                FileStatus fileStatus = new FileStatus();
                String type = line.substring(0, 8).trim();
                String path = line.substring(8).trim().split(" ")[0];
                fileStatus.setPath(path);
                fileStatus.setStatus(getIntStatus(type));
                list.add(fileStatus);
                return true;
            });
            return list;
        }

        @Override
        public String getVersion() {
            StringBuilder builder = new StringBuilder();
            Utils.runCommand(VERSION_COM, line -> {
                if (line.startsWith("Last Changed Rev")) {
                    builder.append(line.substring(17).trim());
                    return false;
                } else if (line.startsWith("Relative URL")) {
                    relativeUrl = line.substring(15).trim() + "/" + projectDir.getName();
                }
                return true;
            });
            return builder.toString();
        }

        @Override
        public Collection<File> getVersionFileList(String startVersion, String endVersion) {
            if (startVersion.compareTo(endVersion) <= 0) {
                return Collections.emptyList();
            }
            Set<File> set = new HashSet<>();
            int urlLength = relativeUrl.length();
            Utils.runCommand(String.format(VERSION_ChANGE_COM, startVersion, endVersion), line -> {
                if (line.length() < 8) {
                    return true;
                }
                int index = line.indexOf(relativeUrl, 5);
                if (index <= 0) {
                    return true;
                }
                String[] ss = line.substring(index + urlLength + 1).trim().split(" ");
                File file = new File(projectDir, ss[0]);
                if (file.isFile()) {
                    set.add(file);
                }
                return true;
            });
            return set;// Stream.of(set.toArray(new File[0])).collect(Collectors.toList());
        }
    }

    /**
     * git 命令
     */
    private static class Git extends AbsCmd {
        private final static String CHANGED_COM = "git status %1s -s --no-renames";
        private final static String VERSION_COM = "git log --pretty=format:%h -1";
        private final static String VERSION_ChANGE_COM = "git diff --name-only %1s %2s";

        Git(File projectDir) {
            super(projectDir);
        }

        @Override
        public Collection<FileStatus> getFileStatusList() {
            List<FileStatus> list = new ArrayList<>();
            Utils.runCommand(String.format(CHANGED_COM, projectDir.getAbsolutePath()), line -> {
                if (line.length() < 3) {
                    return true;
                }
                FileStatus fileStatus = new FileStatus();
                String type = line.substring(0, 2).trim();
                String path = line.substring(2).trim().split(" ")[0];
                fileStatus.setPath(path);
                fileStatus.setStatus(getIntStatus(type));
                list.add(fileStatus);
                return true;
            });
            return list;
        }

        @Override
        public String getVersion() {
            StringBuilder builder = new StringBuilder();
            Utils.runCommand(VERSION_COM, line -> {
                builder.append(line.trim());
                return true;
            });
            return builder.toString();
        }

        @Override
        public Collection<File> getVersionFileList(String startVersion, String endVersion) {
            List<File> list = new ArrayList<>();
            int length = projectDir.getName().length();
            Utils.runCommand(String.format(VERSION_ChANGE_COM, startVersion, endVersion), line -> {
                File file = new File(projectDir, line.substring(length).trim());
                if (file.isFile() && file.exists()) {
                    list.add(file);
                }
                return true;
            });
            return list;
        }
    }
}
