package com.jianghongkui.lint;

import org.gradle.api.Project;

import java.io.File;

/**
 * @author hongkui.jiang
 * @Description TODO
 * @Date 2019-04-27
 */
public class LintExtension {

    private static String BASE_PATH;

    private File versionFile;
    private File changedInfoFile;
    private String lastVersion;

    private String logPath;

    public LintExtension() {

    }

    public void init(Project project) {
        BASE_PATH = new File(project.getRootDir(), "IncrementLint").getAbsolutePath();
        logPath = new File(BASE_PATH, "IncrementLint.log").getAbsolutePath();
        versionFile = new File(BASE_PATH, "CommitVersion.txt");
        changedInfoFile = new File(BASE_PATH, "changes.txt");
        IncrementLintOptions options = project.getExtensions().findByType(IncrementLintOptions.class);
        if (options != null) {
            options.htmlOutput(new File(BASE_PATH, "IncrementLint.html").getAbsolutePath());
        }
    }

    public File getVersionFile() {
        return versionFile;
    }

    public File getChangedInfoFile() {
        return changedInfoFile;
    }

    public String getLogPath() {
        return logPath;
    }

    public String getLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(String lastVersion) {
        this.lastVersion = lastVersion;
    }
}
