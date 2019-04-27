package com.jianghongkui.lint;

import com.android.tools.lint.gradle.api.DelegatingClassLoader;
import com.android.tools.lint.gradle.api.LintExecutionRequest;
import com.jianghongkui.lib_client.ClientCreator;

import org.gradle.api.invocation.Gradle;
import org.gradle.initialization.BuildCompletionListener;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author hongkui.jiang
 * @Date 2019-04-26
 */
public class LintRunner {

    private static DelegatingClassLoader loader = null;
    private static boolean buildCompletionListenerRegistered = false;

    public static Collection<String> runLint(
            Gradle gradle,
            LintExecutionRequest request,
            Set<File> lintClassPath,
            Collection<File> checkFiles
    ) {
        try {
            Class clientClz = ClientCreator.class;
            File clientJar = new File(clientClz.getProtectionDomain().getCodeSource().getLocation().getFile());
            lintClassPath.add(clientJar);
            ClassLoader loader = getLintClassLoader(gradle, lintClassPath, clientJar.getName());
            Class cls = loader.loadClass(clientClz.getName());
            Method create = cls.getMethod("create", LintExecutionRequest.class);
            Object obj = create.invoke(null, request);
            if (obj != null && run(obj, checkFiles) > 0) {
                return getCheckFailedFiles(obj);
            }
        } catch (InvocationTargetException e) {
            throw wrapExceptionAsString(e);
        } catch (Throwable t) {
            // Reflection problem
            throw wrapExceptionAsString(t);
        }
        return Collections.emptyList();
    }

    private static int run(Object obj, Collection<File> checkFiles) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method runMethod = obj.getClass().getMethod("run", Collection.class);
        Object countObj = runMethod.invoke(obj, checkFiles);
        if (countObj instanceof Integer) {
            return (int) countObj;
        }
        return -1;
    }

    private static Collection<String> getCheckFailedFiles(Object obj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = obj.getClass().getMethod("getResult");
        Object resultObj = method.invoke(obj);
        if (resultObj instanceof Collection) {
            return (Collection<String>) resultObj;
        }
        return Collections.emptyList();
    }

    private static ClassLoader getLintClassLoader(Gradle gradle, Set<File> lintClassPath, String customJarName) {
        DelegatingClassLoader l = loader;
        if (l == null) {
            List<URL> urls = computeUrlsFallback(lintClassPath, customJarName);
            l = new DelegatingClassLoader(urls.toArray(new URL[0]));
            loader = l;
        }

        if (!buildCompletionListenerRegistered) {
            buildCompletionListenerRegistered = true;
            DelegatingClassLoader finalL = l;
            gradle.addListener((BuildCompletionListener) () -> {
                try {
                    Class cls = finalL.loadClass("com.android.tools.lint.LintCoreApplicationEnvironment");
                    Method disposeMethod = cls.getDeclaredMethod("disposeApplicationEnvironment");
                    disposeMethod.invoke(null);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } finally {
                    buildCompletionListenerRegistered = false;
                }
            });
        }

        return l;
    }

    private static List<URL> computeUrlsFallback(Set<File> lintClassPath, String customJarName) {
        List<URL> urls = new ArrayList<>();
        lintClassPath.forEach(file -> {
            String name = file.getName();
            if (name.startsWith("uast-") ||
                    name.startsWith("intellij-core-") ||
                    name.startsWith("kotlin-compiler-") ||
                    name.startsWith("asm-") ||
                    name.startsWith("kxml2-") ||
                    name.startsWith("trove4j-") ||
                    name.startsWith("groovy-all-") ||
                    name.startsWith(customJarName) ||

                    // All the lint jars, except lint-gradle-api jar (self)
                    name.startsWith("lint-") &&
                            // Do *not* load this class in a new class loader; we need to
                            // share the same class as the one already loaded by the Gradle
                            // plugin
                            !name.startsWith("lint-gradle-api-")
            ) {
                try {
                    urls.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });

        return urls;
    }

    private static RuntimeException wrapExceptionAsString(Throwable t) {
        return new RuntimeException(t);
    }
}
