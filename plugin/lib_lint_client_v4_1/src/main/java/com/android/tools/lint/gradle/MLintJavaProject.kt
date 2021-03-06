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

package com.android.tools.lint.gradle

import com.android.sdklib.IAndroidTarget
import com.android.tools.lint.detector.api.Project
import com.intellij.pom.java.LanguageLevel
import java.io.File

class MLintJavaProject(
        lintClient: MLintGradleClient?,
        projectDir: File?,
        dependencies: List<Project>,
        sources: List<File>,
        classes: List<File>,
        libs: List<File>,
        tests: List<File>,
        private val languageLevel: LanguageLevel?
) : Project(lintClient!!, projectDir!!, projectDir) {

    init {
        gradleProject = true
        mergeManifests = true
        directLibraries = dependencies
        javaSourceFolders = sources
        javaClassFolders = classes
        javaLibraries = libs
        testSourceFolders = tests
    }

    override fun initialize() {
        // Deliberately not calling super; that code is for ADT compatibility
    }

    override fun isLibrary(): Boolean = true

    override fun isGradleProject(): Boolean = true

    override fun isAndroidProject(): Boolean = false

    override fun getBuildTarget(): IAndroidTarget? = null

    override fun getJavaLanguageLevel(): LanguageLevel {
        return languageLevel ?: super.getJavaLanguageLevel()
    }
}
