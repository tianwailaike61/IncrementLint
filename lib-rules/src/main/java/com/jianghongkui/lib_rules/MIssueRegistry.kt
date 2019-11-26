package com.jianghongkui.lib_rules

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue
import java.util.*

/**
 * @author jianghongkui
 * @date 2018/10/10
 */
open class MIssueRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() {
            val list = ArrayList<Issue>()
            list.addAll(NameIssue.getAllIssue())
            list.addAll(NoChineseIssue.getAllIssue())
            list.addAll(UsageIssue.getAllIssue())
            list.addAll(ValueIssue.getAllIssue())
            list.addAll(AnalysisIssue.getAllIssue())
            list.addAll(CommentIssue.getAllIssue())
            list.addAll(FormatIssue.getAllIssue())
            return list
        }

    override val api: Int
        get() =  com.android.tools.lint.detector.api.CURRENT_API
}