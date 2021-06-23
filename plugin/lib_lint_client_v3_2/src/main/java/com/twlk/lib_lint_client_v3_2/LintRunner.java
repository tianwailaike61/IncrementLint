/*
 * MIT License
 *
 * Copyright (c) 2021 tianwailaike61
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

package com.twlk.lib_lint_client_v3_2;

import com.twlk.lib_lint_base.AbsLintRunner;

import org.gradle.initialization.BuildCompletionListener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * @author hongkui.jiang
 * @Date 2019-04-26
 */
public class LintRunner extends AbsLintRunner {

    @NotNull
    @Override
    protected List<URL> getUrls(@NotNull Set<? extends File> lintClassPath, @NotNull String customJarName) {
        return computeUrlsFallback(lintClassPath, customJarName);
    }

    @Override
    public void onBuildCompleted(@NotNull ClassLoader loader) {
        Class<?> cls;
        try {
            cls = loader.loadClass("com.android.tools.lint.LintCoreApplicationEnvironment");
            Method disposeMethod = cls.getDeclaredMethod("disposeApplicationEnvironment");
            disposeMethod.invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}
