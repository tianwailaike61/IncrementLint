package com.jianghongkui.lint;

import com.android.build.gradle.internal.dsl.LintOptions;
import com.google.common.collect.Sets;

import java.io.File;
import java.util.Set;

/**
 * @author hongkui.jiang
 * @Date 2019-04-27
 */
public class IncrementLintOptions {
    private String htmlOutput;
    private Set<String> disable = Sets.newHashSet();
    private Set<String> enable = Sets.newHashSet();
    private Set<String> check = Sets.newHashSet();

    public IncrementLintOptions() {
    }

    public LintOptions getOptions() {
        LintOptions options = new LintOptions();
        options.setAbortOnError(true);
        options.setTextReport(false);
        options.setXmlReport(false);
        if (!disable.isEmpty()) {
            options.disable(disable.toArray(new String[0]));
        }
        if (!enable.isEmpty()) {
            options.enable(enable.toArray(new String[0]));
        }
        if (!check.isEmpty()) {
            options.check(enable.toArray(new String[0]));
        }
        if (htmlOutput != null) {
            options.setHtmlOutput(new File(htmlOutput));
        }
        return options;
    }

    /**
     * Adds the id to the set of issues to check.
     */
    public void check(String id) {
        check.add(id);
    }

    /**
     * Adds the ids to the set of issues to check.
     */
    public void check(String... ids) {
        for (String id : ids) {
            check(id);
        }
    }

    /**
     * Adds the id to the set of issues to enable.
     */
    public void enable(String id) {
        enable.add(id);
    }

    /**
     * Adds the ids to the set of issues to enable.
     */
    public void enable(String... ids) {
        for (String id : ids) {
            enable(id);
        }
    }

    /**
     * Adds the id to the set of issues to enable.
     */
    public void disable(String id) {
        disable.add(id);
    }

    /**
     * Adds the ids to the set of issues to enable.
     */
    public void disable(String... ids) {
        for (String id : ids) {
            disable(id);
        }
    }

    public void htmlOutput(String filePath) {
        htmlOutput = filePath;
    }

    public String getHtmlOutput() {
        return htmlOutput;
    }
}
