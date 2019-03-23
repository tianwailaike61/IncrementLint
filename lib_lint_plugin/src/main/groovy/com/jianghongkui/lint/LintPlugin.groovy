package com.jianghongkui.lint

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author hongkui.jiang
 * @Date 2019/3/12
 */
class LintPlugin implements Plugin<Project> {

    private Project project

    @Override
    void apply(Project project) {
        this.project = project
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw new GradleException("Lint Plugin,Android Application plugin required")
        }

        MLogger.setOutFile(new File(project.buildDir, "intermediates/LintRun.log").getAbsolutePath())

        project.android.applicationVariants.all { variant ->
            String suffix = variant.name.capitalize()
            def assembleTask = project.tasks["assemble${suffix}"]
            def compileSources = project.tasks["compile${suffix}Sources"]

            //过滤debug版本编译
            if (suffix.endsWith("Debug")) {

                //创建FnLintTask 任务
                IncrementLintTask fnLintTask = project.tasks.replace("IncrementLint${suffix}Task", IncrementLintTask)
                fnLintTask.setApplicationVariant(variant)
                fnLintTask.dependsOn(compileSources)

                assembleTask.dependsOn(fnLintTask)

            }
        }
    }
}