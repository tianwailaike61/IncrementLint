//package com.twlk.lib_rules.detector;
//
//import com.android.tools.lint.client.api.UElementHandler;
//import com.android.tools.lint.detector.api.JavaContext;
//import com.intellij.psi.PsiMethod;
//import com.twlk.lib_rules.UsageIssue;
//import com.twlk.lib_rules.base.BaseDetector;
//
//import org.jetbrains.uast.UCallExpression;
//import org.jetbrains.uast.UElement;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * 处理与集合相关规则
// *
// * @author twlk
// * @date 2018/10/10
// */
//public class CollectionDetector extends BaseDetector.JavaDetector {
//
////    private HashMap<String, Integer> map;
////    private SparseArray<String> normalTyps;
//
//    public CollectionDetector() {
////        map = new HashMap<String, Integer>(8);
////        map.put("Hashtable", 11);
////        map.put("ConcurrentHashMap", 11);
////        map.put("TreeMap", 10);
////        map.put("HashMap", 00);
////
////        normalTyps = new SparseArray<>(8);
////        normalTyps.append(0, "int");
////        normalTyps.append(1, "long");
////        normalTyps.append(2, "double");
////        normalTyps.append(3, "float");
////        normalTyps.append(4, "short");
////        normalTyps.append(5, "char");
////        normalTyps.append(6, "boolean");
////        normalTyps.append(7, "byte");
//    }
//
//    @Override
//    public List<String> getApplicableMethodNames() {
//        return Arrays.asList("keySet"/*, "put"*/);
//    }
//
//    @Override
//    public void visitMethod(JavaContext context, UCallExpression node, PsiMethod method) {
//        switch (method.getName()) {
//            case "keySet":
//                checkSet(context, node, method);
//                break;
////            case "put":
////                checkMap(context, node, method);
////                break;
//            default:
//        }
//    }
//
//    @Override
//    public List<Class<? extends UElement>> getApplicableUastTypes() {
//        List<Class<? extends UElement>> map = new ArrayList<Class<? extends UElement>>(1);
//        map.add(UCallExpression.class);
//        return map;
//    }
//
//    @Override
//    public UElementHandler createUastHandler(final JavaContext context) {
//        return new UElementHandler() {
//
//            @Override
//            public void visitCallExpression(UCallExpression node) {
//                PsiMethod method = node.resolve();
//                if ("constructor_call".equals(node.getKind().getName()) && method != null &&
//                        context.getEvaluator().isMemberInSubClassOf(method, "java.util.Collection", false) &&
//                        node.getValueArguments().size() == 0) {
//                    context.report(UsageIssue.ISSUE_COLLECTION_INIT, node, context.getLocation(node), "指定集合初始值大小");
//                }
//            }
//        };
//    }
//
//    private void checkSet(JavaContext context, UCallExpression node, PsiMethod method) {
//        if (context.getEvaluator().isMemberInSubClassOf(method, "java.util.Set", false)) {
//            context.report(UsageIssue.ISSUE_SET, node, context.getLocation(node), "");
//        }
//    }
//
////    private void checkMap(JavaContext context, UCallExpression node, PsiMethod method) {
////        if (context.getEvaluator().isMemberInSubClassOf(method, "java.util.Map", false)) {
////            int i = 0;
////            for (UExpression expression : node.getValueArguments()) {
////                i <<= 1;
////                if (expression instanceof ULiteralExpression && ((ULiteralExpression) expression).isNull()) {
////                    i += 1;
////                } else if (expression instanceof USimpleNameReferenceExpression) {
//////                    PsiReferenceExpression psiReferenceExpression = (PsiReferenceExpression) expression.getPsi();
//////                    psiReferenceExpression.resolve().getParent().ac
//////                    psiReferenceExpression.resolve().toString();
////                } else if (expression instanceof UCallExpression &&
////                        !isNormalType(((UCallExpression) expression).getReturnType().getPresentableText())) {
////                    i += 1;
////                }
////            }
////            int value;
////            String type = node.getReceiverType().getPresentableText().toString();
////            for (Map.Entry<String, Integer> entry : map.entrySet()) {
////                if (type.startsWith(entry.getKey())) {
////                    value = i & entry.getValue();
////                    if (value == 0) {
////                        return;
////                    }
////                    context.report(UsageIssue.ISSUE_MAP, node, context.getLocation(node), entry.getKey() + getMsgByValue(value));
////                }
////            }
////        }
////    }
////
////    private boolean isNormalType(String type) {
////        return normalTyps.indexOfValue(type) > 0;
////    }
////
////    private String getMsgByValue(int value) {
////        String msg = null;
////        switch (value) {
////            case 1:
////                msg = "的value不能为空";
////                break;
////            case 2:
////                msg = "的key不能为空";
////                break;
////            case 3:
////                msg = "的key和value不能为空";
////                break;
////            default:
////        }
////        return msg;
////    }
//}
