package com.jianghongkui.lint

import com.android.build.gradle.internal.dsl.LintOptions
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.tasks.LintBaseTask
import com.android.builder.model.Version
import com.android.builder.sdk.TargetInfo
import com.android.sdklib.BuildToolInfo
import com.android.tools.lint.gradle.api.LintExecutionRequest
import com.google.common.base.Preconditions
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry
import org.jetbrains.annotations.NotNull

/**
 *
 * lint增量任务
 *
 * @author hongkui.jiang* @Date 2019/3/12
 */
class IncrementLintTask extends LintBaseTask {
    private LintBaseTask.VariantInputs variantInputs

    private String variantName

    static IncrementLintTask create(VariantScope scope, String taskName) {
        Project project = scope.getGlobalScope().project
        //创建FnLintTask 任务
        IncrementLintTask lintTask = project.tasks.replace(taskName, IncrementLintTask)
        new IncrementCreationAction(scope).execute(lintTask)
        return lintTask
    }

    @TaskAction
    void lint() {
        Project project = getProject()
        LintExtension extension = project.getExtensions().findByType(LintExtension.class)
        if (extension == null) {
            return
        }

        Collection<File> checkFileList = getChangedFiles(extension.getChangedInfoFile(), extension.getVersionFile())
        if (checkFileList != null && !checkFileList.isEmpty()) {
            MLogger.addLog("the size of files that's need to be checked is %1d", checkFileList.size())
        } else {
            MLogger.addLog("not need check")
            MLogger.flush()
            return
        }
        Collection<String> strings = runLint(checkFileList)
        if (strings == null || strings.size() == 0) {
            writeResults(extension.versionFile, extension.getLastVersion(), null)
            MLogger.flush()
        } else {
            MLogger.addLog("the problem happened, lastVersion:%1s", extension.getLastVersion())
            writeResults(extension.versionFile, extension.getLastVersion(), strings)
            IncrementLintOptions options = project.getExtensions().findByType(IncrementLintOptions.class)
            File resultFile = new File(options.getHtmlOutput())
            MLogger.flush()
            throw new GradleException("there are " + strings.size() + " errors found by FnLint,you can see more info in " +
                    resultFile.toURI())
        }
    }

    private Collection<String> runLint(Collection<File> checkFiles) {
        FileCollection lintClassPath = getProject().getConfigurations().getByName(LINT_CLASS_PATH)
        return LintRunner.runLint(getProject().getGradle(), new TaskDescriptor(), lintClassPath.getFiles(), checkFiles)
    }

    String getVariantName() {
        return variantName
    }

    void setVariantName(String variantName) {
        this.variantName = variantName
    }

    LintBaseTask.VariantInputs getVariantInputs() {
        return variantInputs
    }

    void setVariantInputs(LintBaseTask.VariantInputs variantInputs) {
        this.variantInputs = variantInputs
    }

    private static Collection<File> getChangedFiles(File... dest) {
        if (dest == null || dest.length == 0) {
            return null
        }
        HashSet<File> list = new HashSet<>()
        for (File file : dest) {
            Utils.readFile(file, new IReadLineCallback() {
                @Override
                boolean onRead(String line) {
                    File f = new File(line)
                    if (f.isFile()) {
                        list.add(f)
                    }
                    return true
                }
            })
        }
        return list
    }

    private static void writeResults(File dest, String version, Collection<String> cs) {
        FileOutputStream fos = null
        try {
            fos = new FileOutputStream(dest)
            if (version != null) {
                fos.write(("version:" + version + "\n").getBytes())
            }
            if (cs != null && cs.size() > 0) {
                FileOutputStream finalFos = fos

                cs.each { s ->
                    try {
                        finalFos.write((s + "\n").getBytes())
                    } catch (IOException e) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace()
        } catch (IOException e) {
            e.printStackTrace()
        } finally {
            Utils.closeStream(fos)
        }
    }

    private class TaskDescriptor extends LintExecutionRequest {

        @Override
        String getVariantName() {
            return IncrementLintTask.this.variantName
        }

        @Override
        LintBaseTask.VariantInputs getVariantInputs(String variantName) {
            assert variantName == getVariantName()
            return variantInputs
        }

        @Override
        LintOptions getLintOptions() {
            IncrementLintOptions options = getProject().getExtensions().findByType(IncrementLintOptions.class)
            if (options == null) {
                return super.getLintOptions()
            }
            return options.getOptions()
        }

        @Override
        BuildToolInfo getBuildTools() {
            TargetInfo targetInfo = IncrementLintTask.this.androidBuilder.getTargetInfo()
            Preconditions.checkState(
                    targetInfo != null, "androidBuilder.targetInfo required for task '%s'.", getName())
            return targetInfo.getBuildTools()
        }

        @Override
        String getGradlePluginVersion() {
            return Version.ANDROID_GRADLE_PLUGIN_VERSION
        }

        @Override
        Project getProject() {
            return IncrementLintTask.this.getProject()
        }

        @Override
        File getReportsDir() {
            return IncrementLintTask.this.reportsDir
        }

        @Override
        File getSdkHome() {
            return IncrementLintTask.this.sdkHome
        }

        @Override
        ToolingModelBuilderRegistry getToolingRegistry() {
            return IncrementLintTask.this.toolingRegistry
        }

        @Override
        void warn(@NotNull String s, @NotNull Object... objects) {

        }
    }

    static class IncrementCreationAction extends LintBaseTask.BaseConfigAction<IncrementLintTask> {
        private final VariantScope scope

        IncrementCreationAction(VariantScope scope) {
            super(scope.getGlobalScope())
            this.scope = scope
        }

        @Override
        String getName() {
            return "IncrementLint"
        }

        @Override
        Class<IncrementLintTask> getType() {
            return IncrementLintTask.class
        }

        @Override
        void execute(IncrementLintTask lintTask) {
            super.execute(lintTask)
            lintTask.setVariantName(scope.getFullVariantName())

            lintTask.setVariantInputs(new LintBaseTask.VariantInputs(scope))
        }
    }

}