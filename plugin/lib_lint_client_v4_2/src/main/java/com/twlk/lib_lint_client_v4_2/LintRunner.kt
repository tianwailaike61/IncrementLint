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

package com.twlk.lib_lint_client_v4_2

import com.android.build.gradle.internal.services.LintClassLoaderBuildService
import com.twlk.lib_lint_base.AbsLintRunner
import org.gradle.api.GradleException
import org.gradle.api.invocation.Gradle
import java.io.File
import java.net.URL

internal class LintRunner : AbsLintRunner() {

    var service: LintClassLoaderBuildService? = null

    override fun getUrls(lintClassPath: Set<File>, customJarName: String): List<URL> {
        return computeUrlsFromClassLoaderDelta(lintClassPath)
            ?: computeUrlsFallback(lintClassPath, customJarName)
    }

    override fun getLintClassLoader(
        gradle: Gradle,
        lintClassPath: Set<File>,
        customJarName: String
    ): ClassLoader {
        return if (service == null) {
            super.getLintClassLoader(gradle, lintClassPath, customJarName)
        } else {
            val fileSet: MutableSet<File> = HashSet()
            var customJar: File? = null
            lintClassPath.forEach {
                if (it.name == customJarName) {
                    customJar = it
                } else {
                    fileSet.add(it)
                }
            }
            if (customJar == null) {
                throw GradleException("can not find custom jar")
            }
            val urls: Array<URL> = arrayOf(customJar!!.toURI().toURL())
            DelegatingClassLoader(urls, service!!.getClassLoader(fileSet))
        }
    }

}