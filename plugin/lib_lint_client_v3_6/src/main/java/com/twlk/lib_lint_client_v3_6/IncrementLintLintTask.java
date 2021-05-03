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

package com.twlk.lib_lint_client_v3_6;

import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.tasks.LintBaseTask;
import com.android.repository.Revision;
import com.twlk.lib_lint_base.IncrementLintLintBaseTask;
import com.twlk.lib_lint_base.IIncrementCreationAction;
import com.twlk.lib_lint_base.ILintRunner;
import com.twlk.lib_lint_base.ITaskConfigure;
import com.twlk.lib_lint_base.Utils;

import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;

public class IncrementLintLintTask extends IncrementLintLintBaseTask {

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
        public LintTaskDescriptor() {
        }

        @Override
        public Revision getBuildToolsRevision() {
            return Revision.NOT_SPECIFIED;
        }
    }

    static class TaskConfigure extends BaseTaskConfigure<IncrementLintLintTask> {

        public TaskConfigure(BaseVariant variant) {
            super(variant);
        }

        @Override
        public void configure(IncrementLintLintTask task) {
            super.configure(task);
            task.variantName = variant.getName();
            task.variantInputs = new VariantInputs(Utils.getVariantScope(variant));
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
            super.configure(lintTask);
            if (configure instanceof BaseTaskConfigure) {
                ((BaseTaskConfigure) configure).configure(lintTask);
            }

        }
    }
}
