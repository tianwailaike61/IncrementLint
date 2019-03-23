package com.jianghongkui.lint;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * 抽象命令类型
 *
 * @author hongkui.jiang
 * @Date 2019/3/13
 */
public abstract class AbsCmdType {

    private final static int GIT = 1;
    private final static int SVN = 2;

    abstract String getCommandStr(File checkDir);

    /**
     * 解析svn或git命令运行输出
     */
    abstract List<FileStatus> getFileStatusList(InputStream inputStream) throws IOException;

    /**
     * 将SVN 或 GIT中的文件状态表示方式统一
     */
    protected FileStatus.FStatus getIntStatus(String s) {
        switch (s) {
            case "D":
            case "deleted":
            case "missing":
                return FileStatus.FStatus.DELETE;
            case "A":
            case "added":
            case "unversioned":
                return FileStatus.FStatus.ADD;
            case "M":
            case "??":
            case "modified":
            case "replaced":
            default:
                return FileStatus.FStatus.MODIFY;
        }
    }

    public static AbsCmdType get(int toolType) {
        AbsCmdType type = null;
        switch (toolType) {
            case GIT:
                type = new GitType();
                break;
            case SVN:
                type = new SvnType();
                break;
        }
        return type;
    }

    /**
     * svn 命令
     */
    private static class SvnType extends AbsCmdType {
        private final static String cmd = "svn status %1s --xml";

        @Override
        String getCommandStr(File checkDir) {
            return String.format(cmd, checkDir.getAbsolutePath());
        }

        @Override
        List<FileStatus> getFileStatusList(InputStream inputStream) throws IOException {
            if (inputStream == null) return null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            DocumentBuilder db = null;
            Document document = null;
            try {
                db = dbf.newDocumentBuilder();
                document = db.parse(inputStream);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                return null;
            } catch (SAXException e) {
                e.printStackTrace();
                return null;
            }
            org.w3c.dom.NodeList nodeList = document.getElementsByTagName("entry");
            if (nodeList == null || nodeList.getLength() == 0) {
                return null;
            }
            List<FileStatus> list = new ArrayList<>(nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                FileStatus fileStatus = new FileStatus();

                fileStatus.setPath(node.getAttributes().getNamedItem("path").getTextContent());
                org.w3c.dom.NodeList childNodeList = node.getChildNodes();
                for (int j = 0; j < childNodeList.getLength(); j++) {
                    Node childNode = childNodeList.item(j);
                    if ("wc-status".equals(childNode.getNodeName())) {
                        fileStatus.setStatus(getIntStatus(childNode.getAttributes().getNamedItem("item").getTextContent()));
                        break;
                    }
                }

                list.add(fileStatus);
            }
            return list;
        }
    }

    /**
     * git 命令
     */
    private static class GitType extends AbsCmdType {

        private final static String cmd = "git status %1s -s";

        @Override
        String getCommandStr(File checkDir) {
            return String.format(cmd, checkDir.getAbsolutePath());
        }

        @Override
        List<FileStatus> getFileStatusList(InputStream inputStream) throws IOException {
            if (inputStream == null) {
                return null;
            }
            InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            String str;
            List<FileStatus> list = new ArrayList<>();
            while ((str = reader.readLine()) != null) {
                if (str.length() < 3) {
                    continue;
                }
                FileStatus fileStatus = new FileStatus();
                String type = str.substring(0, 2).trim();
                fileStatus.setPath("./" + str.substring(2).trim());
                fileStatus.setStatus(getIntStatus(type));
                list.add(fileStatus);
            }
            reader.close();
            isr.close();
            return list;
        }
    }
}
