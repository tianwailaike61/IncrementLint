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

package com.twlk.lib_lint_client_v3_6

import com.android.builder.model.Variant
import com.android.repository.Revision
import com.android.tools.lint.LintCliFlags
import com.android.tools.lint.Warning
import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.LintBaseline
import com.android.tools.lint.client.api.LintRequest
import com.android.tools.lint.detector.api.*
import com.android.tools.lint.gradle.KotlinSourceFoldersResolver
import com.android.tools.lint.gradle.LintGradleClient
import com.android.tools.lint.gradle.api.VariantInputs
import com.android.utils.Pair
import com.twlk.lib_lint_base.LintResultCollector
import org.gradle.api.Project
import java.io.File
import java.io.IOException

/**
 * @author hongkui.jiang
 * @Date 2019/3/12
 */
class IncrementLintClient(
    version: String?,
    registry: IssueRegistry,
    flags: LintCliFlags?,
    gradleProject: Project?,
    sdkHome: File?,
    variant: Variant?,
    variantInputs: VariantInputs?,
    buildToolInfoRevision: Revision?,
    resolver: KotlinSourceFoldersResolver?,
    isAndroid: Boolean,
    baselineVariantName: String?
) : LintGradleClient(
    version,
    registry,
    flags,
    gradleProject,
    sdkHome,
    variant,
    variantInputs,
    buildToolInfoRevision,
    resolver,
    isAndroid,
    baselineVariantName
) {
    private lateinit var collector: LintResultCollector
    private val mRegistry: IssueRegistry = registry

    override fun createLintRequest(files: List<File>): LintRequest {
        val request = super.createLintRequest(files)
        for (project in request.getProjects()!!) {
            for (f in collector.files) {
                project.addFile(f)
            }
        }
        return request
    }

    @Throws(IOException::class)
    fun run(collector: LintResultCollector): Pair<List<Warning>, LintBaseline> {
        this.collector = collector
        return super.run(this.mRegistry)
    }

    override fun report(
        context: Context,
        issue: Issue,
        severity: Severity,
        location: Location,
        message: String,
        format: TextFormat,
        fix: LintFix?
    ) {
        if (collector.addReport(issue.id, severity.isError, context.file)) {
            super.report(context, issue, severity, location, message, format, fix)
        }
    }
}