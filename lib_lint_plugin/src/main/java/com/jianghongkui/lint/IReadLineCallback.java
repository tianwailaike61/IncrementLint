package com.jianghongkui.lint;

/**
 * @author hongkui.jiang
 * @Date 2019-04-23
 */
public interface IReadLineCallback {
    /**
     * @return false:打断读取 true:继续读取
     */
    boolean onRead(String line);
}
