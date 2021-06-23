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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author hongkui.jiang
 * @Date 2019/3/12
 */
public class IncrementLogger {

    private static StringBuilder builder;
    private static String logFilePath;

    private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss -", Locale.CHINESE);

    private IncrementLogger() {
    }

    public static void setOutFile(String filePath) {
        logFilePath = filePath;
    }

    public static void addLog(String msg) {

//        System.out.println(msg);

        if (builder == null) {
            builder = new StringBuilder();
        }
        builder.append(format.format(new Date()));
        builder.append(msg);
        builder.append("\n");
        if (builder.length() > 100) {
            flush();
        }
    }

    public static void addLog(String fmt, Object... objects) {
        addLog(String.format(fmt, objects));
    }

    public static void flush() {
        if (builder == null || builder.length() == 0) {
            return;
        }
        String s = builder.toString();
        builder.delete(0, builder.length());
        writeToFile(s);
    }

    static void writeToFile(String s) {
        if (logFilePath == null || logFilePath.trim().length() == 0) {
            return;
        }
        if (s == null || s.length() == 0) return;
        File file = new File(logFilePath);

        try {
            Utils.saveFile(s, file, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
