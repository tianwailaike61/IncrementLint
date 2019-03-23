package com.jianghongkui.lint;

/**
 * @author hongkui.jiang
 * @Date 2019/3/11
 */
class FileStatus {

    private String path; //文件路径
    private FStatus status; //文件状态

    String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    FStatus getStatus() {
        return status;
    }

    void setStatus(FStatus status) {
        this.status = status;
    }

    public enum FStatus {
        MODIFY, ADD, DELETE
    }

    @Override
    public String toString() {
        return "path:" + path + " status:" + status;
    }
}
