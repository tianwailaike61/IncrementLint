package com.jianghongkui.lint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 工具类
 *
 * @author hongkui.jiang
 * @Date 2019/3/12
 */
public class Utils {

    private Utils() {
    }

    /**
     * 下载文件
     */
    public static void request(String urlStr, File file) throws Exception {
        HttpURLConnection http;
        InputStream is;
        URL urlGet = new URL(urlStr);
        http = (HttpURLConnection) urlGet.openConnection();

        http.setRequestMethod("GET");
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        http.setDoOutput(true);
        http.setDoInput(true);
        System.setProperty("sun.net.client.defaultConnectTimeout", "30000");
        System.setProperty("sun.net.client.defaultReadTimeout", "30000");
        http.connect();
        is = http.getInputStream();

        saveFile(is, file);

        http.disconnect();
        try {
            if (null != is) is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 保存文件
     */
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
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

