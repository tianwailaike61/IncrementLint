/*
 * MIT License
 *
 * Copyright (c) 2020 tianwailaike61
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

package com.twlk.lib_rules.base

import com.android.tools.lint.detector.api.*

/**
 * @author twlk
 * @date 2018/10/10
 */
open class BaseIssue {

    companion object {
        const val CUSTOM_PRIORITY = 10
        val CUSTOM_CATEGORY = Category.create("CustomRules", CUSTOM_PRIORITY)
    }

    protected fun createIssue(id: String, desc: String, explanation: String, type: Int,
                              implementation: Implementation): Issue {
        return Issue.create(id, desc, explanation, CUSTOM_CATEGORY, CUSTOM_PRIORITY, Level.getSeverity(type),
                implementation)
    }

    protected fun createJavaIssue(id: String, desc: String, explanation: String, type: Int,
                                  cls: Class<out Detector>): Issue {
        return Issue.create(id, desc, explanation, CUSTOM_CATEGORY, CUSTOM_PRIORITY, Level.getSeverity(type),
                Implementation(cls, Scope.JAVA_FILE_SCOPE))
    }

    protected fun createClassIssue(id: String, desc: String, explanation: String, type: Int,
                                   cls: Class<out Detector>): Issue {
        return Issue.create(id, desc, explanation, CUSTOM_CATEGORY, CUSTOM_PRIORITY, Level.getSeverity(type),
                Implementation(cls, Scope.CLASS_FILE_SCOPE))
    }

    protected fun createXmlIssue(id: String, desc: String, explanation: String, type: Int,
                                 cls: Class<out Detector>): Issue {
        return Issue.create(id, desc, explanation, CUSTOM_CATEGORY, CUSTOM_PRIORITY, Level.getSeverity(type),
                Implementation(cls, Scope.RESOURCE_FILE_SCOPE))
    }

    protected fun createBinaryIssue(id: String, desc: String, explanation: String, type: Int,
                                 cls: Class<out Detector>): Issue {
        return Issue.create(id, desc, explanation, CUSTOM_CATEGORY, CUSTOM_PRIORITY, Level.getSeverity(type),
                Implementation(cls, Scope.BINARY_RESOURCE_FILE_SCOPE))
    }

    protected fun createGradleIssue(id: String, desc: String, explanation: String, type: Int,
                                    cls: Class<out Detector>): Issue {
        return Issue.create(id, desc, explanation, CUSTOM_CATEGORY, CUSTOM_PRIORITY, Level.getSeverity(type),
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