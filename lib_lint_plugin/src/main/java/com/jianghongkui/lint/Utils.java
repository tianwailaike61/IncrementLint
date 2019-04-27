package com.jianghongkui.lint;

import org.gradle.api.GradleException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 工具类
 *
 * @author hongkui.jiang
 * @Date 2019/3/12
 */
public class Utils {

    private Utils() {
    }


    public static void saveFile(InputStream is, File file) throws IOException {
        if (is == null || file == null) return;
        file.deleteOnExit();

        FileOutputStream outputStream;

        file.getParentFile().mkdirs();
        file.createNewFile();
        outputStream = new FileOutputStream(file);
        byte[] buf1 = new byte[1024];
        int len;
        while ((len = is.read(buf1)) > 0) {
            outputStream.write(buf1, 0, len);
        }
        closeStream(outputStream);
    }

    public static void saveFile(String s, File file) throws IOException {
        if (s == null) return;
        byte[] bytes = s.getBytes();
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
        saveFile(byteInputStream, file);
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
        BufferedReader reader = null;
        String str;
        try {
            isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            reader = new BufferedReader(isr);
            while ((str = reader.readLine()) != null) {
                if (!callback.onRead(str)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.closeStream(reader, isr);
        }
    }

    public static void runCommand(String s, IReadLineCallback callback) {
        InputStream inputStream = null;
        try {
            inputStream = exec(s);
            readStream(inputStream, callback);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.closeStream(inputStream);
        }

    }

    public static InputStream exec(String cmdStr) throws InterruptedException, IOException, GradleException {
        Process process = Runtime.getRuntime().exec(cmdStr);
        process.waitFor();
        int exitValue = process.exitValue();
        MLogger.addLog("%s execute %d", cmdStr, exitValue);
        if (exitValue != 0) {
            MLogger.flush();
            throw new GradleException("The command execute failed,please check your config\n");
        }
        return process.getInputStream();
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
}

