package com.jianghongkui.lib_rules.base

import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.LayoutDetector

/**
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
open class BaseDetector : Detector() {
    open class JavaDetector : Detector(), Detector.UastScanner

    open class XmlDetector : LayoutDetector()

    open class GradleDetector : Detector(), Detector.GradleScanner

    open class ClassDetector : Detector(), Detector.ClassScanner

    interface JavaScanner : Detector.UastScanner
}