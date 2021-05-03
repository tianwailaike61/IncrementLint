/*
 * MIT License
 *
 * Copyright (c) 2020 tianwailaike61
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

package com.twlk.lib_lint_client_v4_1;

import com.android.annotations.NonNull;
import com.android.build.api.component.impl.ComponentPropertiesImpl;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.LoggerWrapper;
import com.android.build.gradle.internal.SdkComponentsBuildService;
import com.android.build.gradle.internal.SdkComponentsKt;
import com.android.build.gradle.internal.api.BaseVariantImpl;
import com.android.build.gradle.internal.scope.GlobalScope;
import com.android.build.gradle.tasks.LintBaseTask;
import com.android.builder.errors.DefaultIssueReporter;
import com.android.repository.Revision;
import com.twlk.lib_lint_base.IncrementLintLintBaseTask;
import com.twlk.lib_lint_base.IIncrementCreationAction;
import com.twlk.lib_lint_base.ILintRunner;
import com.twlk.lib_lint_base.ITaskConfigure;

import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;

public class IncrementLintLintTask extends IncrementLintLintBaseTask {

    @Override
    public Property<SdkComponentsBuildService> getSdkBuildService() {
        return null;
    }

    @TaskAction
    public void lint() {
        runLint(new LintTaskDescriptor());
    }

    @Override
    protected ITaskConfigure getAction(BaseVariant variant) {
        return new TaskConfigure(variant);
    }

    @Override
    protected ILintRunner getRunner() {
        return new LintRunner();
    }


    private class LintTaskDescriptor extends IncrementLintBaseTaskDescriptor {
        @NonNull
        @Override
        public Set<String> getVariantNames() {
            return Collections.singleton(variantName);
        }

        @Override
        public Revision getBuildToolsRevision() {
            return Revision.NOT_SPECIFIED;
        }
    }

    static class TaskConfigure extends BaseTaskConfigure<IncrementLintLintTask> {

        private ComponentPropertiesImpl componentProperties;

        public TaskConfigure(BaseVariant variant) {
            super(variant);
            Class<BaseVariantImpl> cls = BaseVariantImpl.class;
            ComponentPropertiesImpl componentProperties = null;
            try {
                Field field = cls.getDeclaredField("componentProperties");
                field.setAccessible(true);
                componentProperties = (ComponentPropertiesImpl) field.get(variant);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            if (componentProperties != null) {
                this.componentProperties = componentProperties;
            }
        }

        @NotNull
        @Override
        public GlobalScope getGlobalScope() {
            if (componentProperties == null) {
                return null;
            }
            return componentProperties.getGlobalScope();
        }

        @Override
        public void configure(IncrementLintLintTask task) {
            super.configure(task);
            task.variantName = variant.getName();
            task.variantInputs = new VariantInputs(componentProperties);
        }
    }

    public static class IncrementCreationAction extends LintBaseTask.BaseCreationAction<IncrementLintLintTask> implements IIncrementCreationAction<IncrementLintLintTask> {
        private final ITaskConfigure configure;

        public IncrementCreationAction(ITaskConfigure configure) {
            super(configure.getGlobalScope());
            this.configure = configure;
        }

        @NotNull
        @Override
        public String getName() {
            return "IncrementLint";
        }

        @NotNull
        @Override
        public Class<IncrementLintLintTask> getType() {
            return IncrementLintLintTask.class;
        }

        @Override
        public void configure(IncrementLintLintTask lintTask) {
            lintTask.setGroup(JavaBasePlugin.VERIFICATION_GROUP);
            lintTask.lintOptions = globalScope.getExtension().getLintOptions();
            lintTask.sdkHome =
                    SdkComponentsKt.getSdkDir(
                            lintTask.getProject().getRootDir(),
                            new DefaultIssueReporter(LoggerWrapper.getLogger(LintBaseTask.class)));
            lintTask.toolingRegistry = globalScope.getToolingRegistry();
            lintTask.reportsDir = globalScope.getReportsDir();
            if (configure instanceof BaseTaskConfigure) {
                ((BaseTaskConfigure) configure).configure(lintTask);
            }
        }
    }
}
