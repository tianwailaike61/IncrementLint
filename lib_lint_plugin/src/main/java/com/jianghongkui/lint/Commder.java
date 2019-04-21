package com.jianghongkui.lint;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * @author hongkui.jiang
 * @Date 2019-04-19
 */
public class Commder {
    private static List<FileStatus> getResult(File checkDir, AbsCmdType type) throws IOException, InterruptedException {
        if (type == null) {
            return emptyList();
        }
        String commandStr = type.getCommandStr(checkDir);
        Process process = Runtime.getRuntime().exec(commandStr);
        process.waitFor();
        int exitValue = process.exitValue();
        MLogger.addLog("%s execute %d", commandStr, exitValue);
        if (exitValue != 0) {
            throw new GradleException("The command execute faild,please check your config\n");
        }
        InputStream inputStream = process.getInputStream();
        List<FileStatus> list = type.getFileStatusList(inputStream);
        inputStream.close();
        return list;
    }

   public static List<FileStatus> run(File checkDir, int checkType) {
        AbsCmdType type = AbsCmdType.get(checkType);
        try {
            return getResult(checkDir, type);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 检查使用的仓库管理工具
     */
   public static int checkToolType(Project project) {
        File rootDir = project.getRootDir();

        File gitFile = new File(rootDir, ".git");
        if (gitFile.exists())
            return AbsCmdType.GIT;
        File svnFile = new File(rootDir, ".svn");
        if (svnFile.exists())
            return AbsCmdType.SVN;
        return -1;
    }
}
