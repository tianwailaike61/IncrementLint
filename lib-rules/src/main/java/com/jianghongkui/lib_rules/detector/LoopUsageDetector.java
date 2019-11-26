package com.jianghongkui.lib_rules.detector;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.JavaContext;
import com.intellij.psi.PsiMethod;
import com.jianghongkui.lib_rules.base.BaseDetector;
import com.jianghongkui.lib_rules.base.BaseUastVisitor;
import com.jianghongkui.lib_rules.base.Position;

import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UDeclaration;
import org.jetbrains.uast.UDeclarationsExpression;
import org.jetbrains.uast.UDoWhileExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UForEachExpression;
import org.jetbrains.uast.UForExpression;
import org.jetbrains.uast.ULoopExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UTryExpression;
import org.jetbrains.uast.UWhileExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jianghongkui.lib_rules.UsageIssue.ISSUE_FOREACH;
import static com.jianghongkui.lib_rules.UsageIssue.ISSUE_LOOP;

/**
 * 处理与循环使用相关规则
 *
 * @author jianghongkui
 * @date 2018/10/10
 */
public class LoopUsageDetector extends BaseDetector.JavaDetector {

    private LoopCheckVisitor visitor;

    private boolean isNeedCheckForEach;

    private final static String FOREACH_MSG = "foreach中不能对当前集合进行增删操作";

    public LoopUsageDetector() {
        visitor = new LoopCheckVisitor();
    }

    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList("add", "remove", "addAll", "removeAll");
    }

    @Override
    public void visitMethod(JavaContext context, UCallExpression node, PsiMethod method) {
        if (!isNeedCheckForEach)
            return;
        if (!context.getEvaluator().isMemberInClass(method, "java.util.List")) {
            return;
        }
        if (checkMethodForEach(node, node.getReceiver().toString()))
            context.report(ISSUE_FOREACH, node, context.getLocation(node), FOREACH_MSG);
    }

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        List<Class<? extends UElement>> list = new ArrayList<Class<? extends UElement>>(5);
        list.add(UForEachExpression.class);
        list.add(UForExpression.class);
        list.add(UWhileExpression.class);
        list.add(UDoWhileExpression.class);
        list.add(UMethod.class);
        return list;
    }

    @Override
    public UElementHandler createUastHandler(final JavaContext context) {
        return new UElementHandler() {

            @Override
            public void visitMethod(UMethod node) {
                isNeedCheckForEach = false;
            }

            @Override
            public void visitWhileExpression(UWhileExpression node) {
                visitor.start(context, node.getBody());
            }

            @Override
            public void visitDoWhileExpression(UDoWhileExpression node) {
                visitor.start(context, node.getBody());
            }

            @Override
            public void visitForExpression(UForExpression node) {
                visitor.start(context, node.getBody());
            }

            @Override
            public void visitForEachExpression(UForEachExpression node) {
                isNeedCheckForEach = true;
                visitor.start(context, node.getBody());
            }
        };
    }

    private boolean checkMethodForEach(UElement node, String receiver) {
        if (node instanceof UMethod) {
            return false;
        }
        if (node instanceof UForEachExpression) {
            String loopName = ((UForEachExpression) node).getIteratedValue().toString();
            if (loopName.equals(receiver)) {
                return true;
            }
        }
        return checkMethodForEach(node.getUastParent(), receiver);

    }

    private class LoopCheckVisitor extends BaseUastVisitor<JavaContext> {
        private boolean isChildLoop = false;
        private int lockCode = -1;

       // private static final String DECLARATION_MSG = "请将变量%1s声明提取到循环体外";
        private static final String TRY_MSG = "请将try/catch语句提取到循环体外";

//        @Override
//        public boolean visitDeclarationsExpression(UDeclarationsExpression uDeclarationsExpression) {
//            if (isChildLoop) return true;
//            StringBuilder sb = new StringBuilder();
//            for (UDeclaration declaration : uDeclarationsExpression.getDeclarations()) {
//                sb.append(declaration.getText());
//                sb.append(",");
//            }
//            sb.deleteCharAt(sb.length() - 1);
//            context.report(ISSUE_LOOP, uDeclarationsExpression, context.getLocation(uDeclarationsExpression),
//                    String.format(DECLARATION_MSG, sb.toString()));
//            return true;
//        }

        @Override
        public boolean visitTryExpression(UTryExpression uTryExpression) {
            if (isChildLoop) return true;
            context.report(ISSUE_LOOP, uTryExpression, context.getLocation(uTryExpression), TRY_MSG);
            return true;
        }

        @Override
        public boolean visitForExpression(UForExpression uForExpression) {
            lock(uForExpression.hashCode());
            return super.visitForExpression(uForExpression);
        }

        @Override
        public void afterVisitForExpression(UForExpression uForExpression) {
            super.afterVisitForExpression(uForExpression);
            unLock(uForExpression.hashCode());
        }

        @Override
        public boolean visitForEachExpression(UForEachExpression uForEachExpression) {
            lock(uForEachExpression.hashCode());
            return super.visitForEachExpression(uForEachExpression);
        }

        @Override
        public void afterVisitForEachExpression(UForEachExpression uForEachExpression) {
            super.afterVisitForEachExpression(uForEachExpression);
            unLock(uForEachExpression.hashCode());
        }

        @Override
        public boolean visitWhileExpression(UWhileExpression uWhileExpression) {
            lock(uWhileExpression.hashCode());
            return super.visitWhileExpression(uWhileExpression);
        }

        @Override
        public void afterVisitWhileExpression(UWhileExpression uWhileExpression) {
            super.afterVisitWhileExpression(uWhileExpression);
            unLock(uWhileExpression.hashCode());
        }

        @Override
        public boolean visitDoWhileExpression(UDoWhileExpression uDoWhileExpression) {
            lock(uDoWhileExpression.hashCode());
            return super.visitDoWhileExpression(uDoWhileExpression);
        }

        @Override
        public void afterVisitDoWhileExpression(UDoWhileExpression uDoWhileExpression) {
            super.afterVisitDoWhileExpression(uDoWhileExpression);
            unLock(uDoWhileExpression.hashCode());
        }

        private void lock(int hashCode) {
            if (lockCode > 0) return;
            isChildLoop = true;
            lockCode = hashCode;
        }

        private void unLock(int hashCode) {
            if (lockCode == hashCode) {
                isChildLoop = false;
                lockCode = -1;
            }
        }

    }
}
