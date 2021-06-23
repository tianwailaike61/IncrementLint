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

package com.twlk.lib_lint_base;

import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.api.BaseVariantImpl;
import com.android.build.gradle.internal.dsl.LintOptions;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.twlk.lib_lint_base.extension.IncrementLintOptions;

import org.gradle.api.GradleException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author hongkui.jiang
 * @Date 2019/3/12
 */
public class Utils {

    public static ExecutorService service = Executors.newFixedThreadPool(2);

    private Utils() {
    }

//    void request(String urlStr, File file) {
//        HttpURLConnection http;
//        InputStream inputStream = null;
//        URL urlGet;
//        try {
//            urlGet = new URL(urlStr);
//            http = (HttpURLConnection) urlGet.openConnection();
//            http.setRequestMethod("GET");
//            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            http.setDoInput(true);
//            http.setDoOutput(true);
//            System.setProperty("sun.net.client.defaultConnectTimeout", "30000");
//            System.setProperty("sun.net.client.defaultReadTimeout", "30000");
//            http.connect();
//            inputStream = http.getInputStream();
//            saveFile(inputStream, file);
//            http.disconnect();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//            FnLogger.addLog("request-" + e.toString());
//        } catch (ProtocolException e) {
//            e.printStackTrace();
//            FnLogger.addLog("request-" + e.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//            FnLogger.addLog("request-" + e.toString());
//        } finally {
//            closeStream(inputStream);
//        }
//    }

    public static void saveFile(InputStream is, File file) throws IOException {
        saveFile(is, file, false);
    }

    public static void saveFile(InputStream is, File file, boolean isAppend) throws IOException {
        if (is == null || file == null) return;

        FileOutputStream outputStream;

        file.getParentFile().mkdirs();
        file.createNewFile();
        outputStream = new FileOutputStream(file, isAppend);
        byte[] buf1 = new byte[1024];
        int len;
        while ((len = is.read(buf1)) > 0) {
            outputStream.write(buf1, 0, len);
        }
        closeStream(outputStream);
    }

    public static void saveFile(String s, File file) throws IOException {
        saveFile(s, file, false);
    }

    public static void saveFile(String s, File file, boolean isAppend) throws IOException {
        if (s == null) return;
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        byte[] bytes = s.getBytes();
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
        saveFile(byteInputStream, file, isAppend);
        closeStream(byteInputStream);
    }

    public static String readFile(File file) {
        StringBuilder builder = new StringBuilder();
        readFile(file, line -> {
            builder.append(line);
            builder.append("\n");
            return true;
        });
        return builder.toString();
    }

    public static void readFile(File file, IReadLineCallback callback) {
        if (file == null || !file.exists()) {
            return;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            readStream(fis, callback);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.closeStream(fis);
        }
    }

    public static void readStream(InputStream inputStream, IReadLineCallback callback) {
        InputStreamReader isr = null;
        String str;
        try {
            isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);

            while ((str = reader.readLine()) != null) {
                if (!callback.onRead(str)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.closeStream(isr);
        }
    }

    public static void runCommand(String s, File dir, IReadLineCallback callback) {
        try {
            exec(s, dir, new IExecListener() {
                @Override
                public void error(String s) {

                }

                @Override
                public void success(String s) {
                    callback.onRead(s);
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String readInputStream(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        String s = null;
        try {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            s = new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static boolean exec(String cmdStr) throws InterruptedException, IOException, GradleException {
        return exec(cmdStr, null);
    }

    public static boolean exec(String cmdStr, File dir) throws InterruptedException, IOException, GradleException {
        return exec(cmdStr, dir, null);
    }

    public static boolean exec(String cmdStr, File dir, IExecListener listener) throws GradleException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        if (dir != null) {
            builder.directory(dir);
        }
        List<String> list = new ArrayList<>(Arrays.asList(cmdStr.split(" ")));
        builder.command(list);
        Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            throw new GradleException("start error", e);
        }
        Future<String> future = service.submit(() -> {
            StringBuffer buffer = new StringBuffer();
            readStream(process.getInputStream(), line -> {
                buffer.append(line).append("\n");
                return true;
            });
            return buffer.toString();
        });
        AtomicBoolean isError = new AtomicBoolean(false);
        Future<String> errFuture = service.submit(() -> {
            StringBuffer buffer = new StringBuffer();
            readStream(process.getErrorStream(), line -> {
                buffer.append(line).append("\n");
                isError.compareAndSet(false, true);
                return true;
            });
            return buffer.toString();
        });
        int ret = process.waitFor();
        IncrementLogger.addLog("%s execute ret:%d isError:%b", cmdStr, ret, isError.get());
        if (ret != 0 || isError.get()) {
            try {
                IncrementLogger.addLog("error result: %s", errFuture.get());
            } catch (ExecutionException e) {
                throw new GradleException("fail to get error info", e);
            }
            IncrementLogger.flush();
            throw new GradleException("The command execute failed,please check your config\n");
        }
        String result;
        try {
            result = future.get();
        } catch (ExecutionException e) {
            throw new GradleException("fail to get info", e);
        }
        if (result == null || result.isEmpty()) {
            return true;
        }
        if (listener != null) {
            for (String s : result.split("\n")) {
                listener.success(s);
            }
        }
        return true;
    }

    public static String printException(Throwable throwable) {
        ByteArrayOutputStream bos = null;
        String s = null;
        try {
            bos = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(bos);
            throwable.printStackTrace(printStream);
            printStream.flush();
            s = bos.toString();
            bos.close();
            printStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStream(bos);
        }
        return s;
    }

    public static void closeStream(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static int compareVersion(String v1, String v2) {
        if (v1.equals(v2)) {
            return 0;
        }

        String[] version1 = v1.split("-");
        String[] version2 = v2.split("-");
        String[] version1Array = version1[0].split("[._]");
        String[] version2Array = version2[0].split("[._]");

        String preRelease1 = "";
        String preRelease2 = "";
        if (version1.length > 1) {
            preRelease1 = version1[1];
        }
        if (version2.length > 1) {
            preRelease2 = version2[1];
        }

        int index = 0;
        int minLen = Math.min(version1Array.length, version2Array.length);
        long diff = 0;

        while (index < minLen
                && (diff = Long.parseLong(version1Array[index])
                - Long.parseLong(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            for (int i = index; i < version1Array.length; i++) {
                if (Long.parseLong(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Long.parseLong(version2Array[i]) > 0) {
                    return -1;
                }
            }
            //compare pre-release
            if (!preRelease1.isEmpty() && preRelease2.isEmpty()) {
                return -1;
            } else if (preRelease1.isEmpty() && !preRelease2.isEmpty()) {
                return 1;
            } else if (!preRelease1.isEmpty()) {
                int preReleaseDiff = preRelease1.compareTo(preRelease2);
                if (preReleaseDiff > 0) {
                    return 1;
                } else if (preReleaseDiff < 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }

    public static String getAGPVersion() {
        // AGP 3.6+
        try {
            Class<?> aClass = Class.forName("com.android.Version");
            Field version = aClass.getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION");
            version.setAccessible(true);
            return (String) version.get(aClass);
        } catch (Throwable ignore) {
            try {
                Class<?> aClass = Class.forName("com.android.builder.model.Version");
                Field version = aClass.getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION");
                version.setAccessible(true);
                return (String) version.get(aClass);
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return "0";
    }

    public static BaseVariantData getVariantData(BaseVariant variant) {
        try {
            Method method = BaseVariantImpl.class.getDeclaredMethod("getVariantData");
            method.setAccessible(true);
            return (BaseVariantData) method.invoke(variant);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static VariantScope getVariantScope(BaseVariant variant) {
        BaseVariantData data = getVariantData(variant);
        if (data == null) {
            return null;
        }
        return data.getScope();
    }

    public static LintOptions getOptions(IncrementLintOptions incrementLintOptions, LintOptions options) {
        options.setAbortOnError(true);
        options.setTextReport(false);
        options.setXmlReport(false);
        if (!incrementLintOptions.disable.isEmpty()) {
            options.disable(incrementLintOptions.disable.toArray(new String[0]));
        }
        if (!incrementLintOptions.enable.isEmpty()) {
            options.enable(incrementLintOptions.enable.toArray(new String[0]));
        }
        if (!incrementLintOptions.check.isEmpty()) {
            options.check(incrementLintOptions.enable.toArray(new String[0]));
        }
        if (incrementLintOptions.htmlOutput != null) {
            options.setHtmlOutput(incrementLintOptions.htmlOutput);
        }
        if (incrementLintOptions.lintXmlPath != null) {
            options.setLintConfig(incrementLintOptions.lintXmlPath);
        }
        return options;
    }

}

