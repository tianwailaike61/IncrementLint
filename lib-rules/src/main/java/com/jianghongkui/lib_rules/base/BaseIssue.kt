package com.jianghongkui.lib_rules.base

import com.android.tools.lint.detector.api.*

/**
 * @author jianghongkui
 * @date 2018/10/10
 */
open class BaseIssue {

    companion object {
        const val FN_PRIORITY = 10
        val FN_CATEGORY = Category.create("FNRules", FN_PRIORITY)
    }

    protected fun createIssue(id: String, desc: String, explanation: String, type: Int,
                              implementation: Implementation): Issue {
        return Issue.create(id, desc, explanation, FN_CATEGORY, FN_PRIORITY, Level.getSeverity(type),
                implementation)
    }

    protected fun createJavaIssue(id: String, desc: String, explanation: String, type: Int,
                                  cls: Class<out Detector>): Issue {
        return Issue.create(id, desc, explanation, FN_CATEGORY, FN_PRIORITY, Level.getSeverity(type),
                Implementation(cls, Scope.JAVA_FILE_SCOPE))
    }

    protected fun createClassIssue(id: String, desc: String, explanation: String, type: Int,
                                   cls: Class<out Detector>): Issue {
        return Issue.create(id, desc, explanation, FN_CATEGORY, FN_PRIORITY, Level.getSeverity(type),
                Implementation(cls, Scope.CLASS_FILE_SCOPE))
    }

    protected fun createXmlIssue(id: String, desc: String, explanation: String, type: Int,
                                 cls: Class<out Detector>): Issue {
        return Issue.create(id, desc, explanation, FN_CATEGORY, FN_PRIORITY, Level.getSeverity(type),
                Implementation(cls, Scope.RESOURCE_FILE_SCOPE))
    }

    protected fun createBinaryIssue(id: String, desc: String, explanation: String, type: Int,
                                 cls: Class<out Detector>): Issue {
        return Issue.create(id, desc, explanation, FN_CATEGORY, FN_PRIORITY, Level.getSeverity(type),
                Implementation(cls, Scope.BINARY_RESOURCE_FILE_SCOPE))
    }

    protected fun createGradleIssue(id: String, desc: String, explanation: String, type: Int,
                                    cls: Class<out Detector>): Issue {
        return Issue.create(id, desc, explanation, FN_CATEGORY, FN_PRIORITY, Level.getSeverity(type),
                Implementation(cls, Scope.GRADLE_SCOPE))
    }
}

object Level {
    const val HIGH = 1
    const val MIDDLE = 2
    const val LOW = 3

    fun getSeverity(type: Int): Severity = when (type) {
        HIGH -> Severity.ERROR
        MIDDLE -> Severity.WARNING
        LOW -> Severity.IGNORE
        else -> Severity.FATAL
    }
}