package com.jianghongkui.lint

import org.gradle.api.GradleException
import org.gradle.api.Project

import java.util.concurrent.TimeUnit;

/**
 * 命令执行器
 * @author hongkui.jiang
 * @Date 2019/3/13
 */
class Commder {

    private Commder() {}

    private static List<FileStatus> getResult(File checkDir, AbsCmdType type) {
        String commandStr = type.getCommandStr(checkDir);
        Process process = commandStr.execute();
        process.waitFor(5, TimeUnit.SECONDS);
        int exitValue = process.exitValue();
        MLogger.addLog(commandStr + " execute " + exitValue);
        if (exitValue != 0) {
            throw new GradleException("The command execute faild,please check your config\n" + process.text);
        }
        InputStream inputStream = process.inputStream;
        List<FileStatus> list = type.getFileStatusList(inputStream);
        inputStream.close();
        return list;
    }

    static List<FileStatus> run(File file, int toolType) {
        AbsCmdType type = AbsCmdType.get(toolType)
        if (type == null) return null
        return getResult(file, type)
    }

    /**
     * 检查使用的仓库管理工具
     */
    static int checkToolType(Project project) {
        File rootDir = project.getProjectDir().getParentFile();

        File gitFile = new File(rootDir, ".git");
        if (gitFile.exists())
            return AbsCmdType.GIT;
        File svnFile = new File(rootDir, ".svn");
        if (svnFile.exists())
            return AbsCmdType.SVN;
        return -1;
    }
}
