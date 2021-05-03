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

package com.twlk.lib_lint_base;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.dsl.LintOptions;
import com.android.build.gradle.internal.scope.GlobalScope;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.tasks.LintBaseTask;
import com.twlk.lib_lint_base.extension.MLintOptions;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class IncrementLintLintBaseTask extends LintBaseTask {

    protected Class<?> realTaskClass;

    protected VariantInputs variantInputs;

    protected String variantName;

    public static <T extends IncrementLintLintBaseTask> T createTask(Project project, BaseVariant variant, String taskName, Class<T> cls) {
        Task task = project.getTasks().findByName(taskName);
        T t;
        if (task == null) {
            t = project.getTasks().create(taskName, cls);
        } else {
            t = (T) task;
        }
        t.realTaskClass = cls;
        try {
            Class<IIncrementCreationAction> c = (Class<IIncrementCreationAction>) Class.forName(cls.getName() + "$IncrementCreationAction");
            Constructor constructor = c.getDeclaredConstructor(ITaskConfigure.class);
            IIncrementCreationAction action = (IIncrementCreationAction) constructor.newInstance(t.getAction(variant));
            action.configure(t);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return t;
    }

    @Override
    protected void runLint(LintBaseTaskDescriptor descriptor) {
        FileCollection lintClassPath = getLintClassPath();
        if (lintClassPath != null) {
            Project project = getProject();
            LintTaskRunner runner = new LintTaskRunner();
            runner.setCreatorClz(realTaskClass);
            runner.setRequest(descriptor);
            runner.setLintClassPath(LINT_CLASS_PATH);
            runner.run(project, getRunner());
        }
    }

    @Override
    public FileCollection getLintClassPath() {
        return getProject().getConfigurations().getByName(LINT_CLASS_PATH);
    }

    protected abstract ITaskConfigure getAction(BaseVariant variant);

    protected abstract ILintRunner getRunner();

    public static LintOptions getOptions(MLintOptions mLintOptions, LintOptions options) {
        options.setAbortOnError(true);
        options.setTextReport(false);
        options.setXmlReport(false);
        if (!mLintOptions.disable.isEmpty()) {
            options.disable(mLintOptions.disable.toArray(new String[0]));
        }
        if (!mLintOptions.enable.isEmpty()) {
            options.enable(mLintOptions.enable.toArray(new String[0]));
        }
        if (!mLintOptions.check.isEmpty()) {
            options.check(mLintOptions.enable.toArray(new String[0]));
        }
        if (mLintOptions.htmlOutput != null) {
            options.setHtmlOutput(mLintOptions.htmlOutput);
        }
        return options;
    }

    public abstract class IncrementLintBaseTaskDescriptor extends LintBaseTask.LintBaseTaskDescriptor {

        @Nullable
        @Override
        public String getVariantName() {
            return variantName;
        }

        @Nullable
        @Override
        public VariantInputs getVariantInputs(@NonNull String variantName) {
            return variantInputs;
        }

    }


    public abstract static class BaseTaskConfigure<T extends IncrementLintLintBaseTask> implements ITaskConfigure {

        protected BaseVariant variant;

        public BaseTaskConfigure(BaseVariant variant) {
            this.variant = variant;
        }

        @NotNull
        @Override
        public GlobalScope getGlobalScope() {
            VariantScope scope = Utils.getVariantScope(variant);
            if (scope == null) {
                return null;
            }
            return scope.getGlobalScope();
        }

        public void configure(T task) {
            MLintOptions mLintOptions = getGlobalScope().getProject().getExtensions().findByType(MLintOptions.class);
            getOptions(mLintOptions, task.lintOptions);
        }
    }
}
