package com.jianghongkui.lint;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 抽象命令类型
 *
 * @author hongkui.jiang
 * @Date 2019/3/13
 */
public abstract class AbsCmdType {

    private final static int GIT = 1;
    private final static int SVN = 2;

    abstract String getCommandStr(File checkDir);

    /**
     * 解析svn或git命令运行输出
     */
    abstract List<FileStatus> getFileStatusList(InputStream inputStream) throws IOException;

    /**
     * 将SVN 或 GIT中的文件状态表示方式统一
     */
    protected FileStatus.FStatus getIntStatus(String s) {
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

    public static AbsCmdType get(int toolType) {
        AbsCmdType type = null;
        switch (toolType) {
            case GIT:
                type = new GitType();
                break;
            case SVN:
                type = new SvnType();
                break;
            default:
        }
        return type;
    }

    /**
     * svn 命令
     */
    private static class SvnType extends AbsCmdType {
        private final static String cmd = "svn status %1s";

        @Override
        String getCommandStr(File checkDir) {
            return String.format(cmd, checkDir.getAbsolutePath());
        }

        @Override
        List<FileStatus> getFileStatusList(InputStream inputStream) throws IOException {
            if (inputStream == null) {
                return null;
            }
            InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            String str;
            List<FileStatus> list = new ArrayList<>();
            while ((str = reader.readLine()) != null) {
                if (str.length() < 8) {
                    continue;
                }
                FileStatus fileStatus = new FileStatus();
                String type = str.substring(0, 8).trim();
                String path = str.substring(8).trim().split(" ")[0];
                fileStatus.setPath(path);
                fileStatus.setStatus(getIntStatus(type));
                list.add(fileStatus);
            }
            reader.close();
            isr.close();
            return list;
        }
    }

    /**
     * git 命令
     */
    private static class GitType extends AbsCmdType {

        private final static String cmd = "git status %1s -s";

        @Override
        String getCommandStr(File checkDir) {
            return String.format(cmd, checkDir.getAbsolutePath());
        }

        @Override
        List<FileStatus> getFileStatusList(InputStream inputStream) throws IOException {
            if (inputStream == null) {
                return null;
            }
            InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            String str;
            List<FileStatus> list = new ArrayList<>();
            while ((str = reader.readLine()) != null) {
                if (str.length() < 3) {
                    continue;
                }
                FileStatus fileStatus = new FileStatus();
                String type = str.substring(0, 2).trim();
                String path = str.substring(2).trim().split(" ")[0];
                fileStatus.setPath("./" + path);
                fileStatus.setStatus(getIntStatus(type));
                list.add(fileStatus);
            }
            reader.close();
            isr.close();
            return list;
        }
    }
}
