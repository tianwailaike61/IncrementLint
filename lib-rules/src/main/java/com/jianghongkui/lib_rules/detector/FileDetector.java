//package com.jianghongkui.lib_rules.detector;
//
//import com.android.tools.lint.client.api.UElementHandler;
//import com.android.tools.lint.detector.api.JavaContext;
//import com.android.tools.lint.detector.api.Location;
//import com.jianghongkui.lib_rules.AnalysisIssue;
//import com.jianghongkui.lib_rules.base.BaseDetector;
//
//import org.jetbrains.uast.UElement;
//import org.jetbrains.uast.UFile;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 处理与文件相关规则
// *
// * @author jianghongkui
// * @date 2018/10/10
// */
//public class FileDetector extends BaseDetector.JavaDetector {
//
//    @Override
//    public List<Class<? extends UElement>> getApplicableUastTypes() {
//        List<Class<? extends UElement>> list = new ArrayList<Class<? extends UElement>>(1);
//        list.add(UFile.class);
//        return list;
//    }
//
//    @Override
//    public UElementHandler createUastHandler(final JavaContext context) {
//        return new UElementHandler() {
//            @Override
//            public void visitFile(UFile node) {
//                if ("".equals(node.getPackageName()))
//                    return;
//                Location location = context.getLocation(node);
//                int count = location.getEnd().getLine();
//                if (600 < count) {
//                    context.report(AnalysisIssue.ISSUE_FILE_LINES, node, location,
//                            String.format("%1s有%2d行", context.file.getName(), count));
//                }
//            }
//        };
//    }
//}
