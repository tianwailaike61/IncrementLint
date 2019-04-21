package com.jianghongkui.lint

import com.android.build.gradle.internal.api.BaseVariantImpl
import com.android.build.gradle.internal.scope.GlobalScope
import com.android.builder.model.AndroidProject
import com.android.builder.model.Variant
import com.android.builder.sdk.TargetInfo
import com.android.sdklib.BuildToolInfo
import com.google.common.base.Preconditions
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.provider.model.ToolingModelBuilder
import org.jetbrains.kotlin.resolve.diagnostics.DiagnosticSuppressor

import java.lang.reflect.Method

/**
 *
 * lint增量任务
 *
 * @author hongkui.jiang* @Date 2019/3/12
 */
class IncrementLintTask extends DefaultTask {

    private BaseVariantImpl applicationVariant;
    private GlobalScope globalScope;

    @TaskAction
    void lint() {
        MLogger.addLog(getName() + " start")
        if (globalScope == null) {
            MLogger.flush()
            throw new GradleException("the global scope not allow to be null when creating android project");
        }
        int toolType = Commder.checkToolType(getProject());

        MLogger.addLog("check %s", getProject().getProjectDir());
        MLogger.addLog("The type of the tool is %d", toolType);
        //获取修改文件
        List<FileStatus> fileStatusList = Commder.run(getProject().getProjectDir(), toolType);

        List<File> fileList = filterFiles(fileStatusList);
        if (toolType == -1 && fileList == null) {
            fileList = new ArrayList<>();
            fileList.add(getProject().getProjectDir());
        }

        if (fileList == null || fileList.isEmpty()) {
            MLogger.addLog("IncrementLintTask no checked file")
            return;
        }
        MLogger.addLog("The changed files is %d", fileList.size());
        for (File file : fileList) {
            MLogger.addLog("- %s", file.getPath());
        }
        IncrementLintClient client = new IncrementLintClient(getProject(), globalScope,
                applicationVariant.getVariantData().getScope(), getBuildTools(globalScope), getVariant());
        client.syncConfigOptions();
        try {
            client.run(fileList);
        } catch (Exception e) {
            for (Method m : clz.getMethods()) {
                println("jhk-11--method " + m.name + " " + m.accessible)
            }
            println("jhk--e==" + e.toString())
        }

        int count = client.getResult();
        if (count > 0) {
            MLogger.flush()
            String suffix = variant.name.capitalize()
            File resultFile = new File(globalScope.getReportsDir(), "lint_${suffix}_result.html");
            throw new GradleException("there are " + count + " errors found by FnLint,you can see more info in " +
                    resultFile.toURI());
        }
        MLogger.flush()
    }

    /**
     * 过滤需要检查的文件
     * @param fileStatusList
     * @return
     */
    private List<File> filterFiles(List<FileStatus> fileStatusList) {
        if (fileStatusList == null || fileStatusList.size() == 0) return null;
        List<File> fileList = new ArrayList<>(fileStatusList.size());
        for (FileStatus status : fileStatusList) {
            File file = new File(status.getPath());
            if (status.getStatus() != FileStatus.FStatus.DELETE && file.exists()) {
                fileList.add(file);
            }
        }
        return fileList;
    }

    void setVariant(BaseVariantImpl variant) {
        if (variant == null) return;
        applicationVariant = variant;
        globalScope = variant.getVariantData().getScope().getGlobalScope();
    }

    private BuildToolInfo getBuildTools(GlobalScope globalScope) {
        TargetInfo targetInfo = globalScope.getAndroidBuilder().getTargetInfo();
        Preconditions.checkState(targetInfo != null, "androidBuilder.targetInfo required for task '%s'.", this.getName());
        return targetInfo.getBuildTools();
    }

    /**
     * 创建AndroidProject
     *
     * 注：如补加ext.set(****)，gradle执行该任务时会报错
     *
     * @param globalScope
     * @return
     */
    private AndroidProject createAndroidProject(GlobalScope globalScope) {
        String modelName = AndroidProject.class.getName();
        ToolingModelBuilder modelBuilder = globalScope.getToolingRegistry().getBuilder(modelName);
        assert modelBuilder.canBuild(modelName): modelName;
        ExtraPropertiesExtension ext = getProject().getExtensions().getExtraProperties();
        synchronized (ext) {
            ext.set("android.injected.build.model.only.versioned", Integer.toString(3));
            ext.set("android.injected.build.model.disable.src.download", true);
            AndroidProject androidProject;
            try {
                androidProject = (AndroidProject) modelBuilder.buildAll(modelName, getProject());
            } finally {
                ext.set("android.injected.build.model.only.versioned", null);
                ext.set("android.injected.build.model.disable.src.download", null);
            }
            return androidProject;
        }
    }

    /**
     * 获取变种
     */
    private Variant getVariant() {
        AndroidProject androidProject = createAndroidProject(globalScope);
        String variantImplName = applicationVariant.getName();
        Collection<Variant> variantList = androidProject.getVariants();
        if (variantList != null && !variantList.isEmpty()) {
            for (Variant v : variantList) {
                if (v.getName().equals(variantImplName)) {
                    return v;
                }
            }
        }
        return null;
    }

}