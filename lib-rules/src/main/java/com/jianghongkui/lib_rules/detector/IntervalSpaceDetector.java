package com.jianghongkui.lib_rules.detector;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.TextFormat;
import com.jianghongkui.lib_rules.FormatIssue;
import com.jianghongkui.lib_rules.base.BaseDetector;

import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UClassInitializer;
import org.jetbrains.uast.UComment;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UField;
import org.jetbrains.uast.UMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cheng.zhang on 2018/10/15.
 * 方法与类成员之间间隔
 */
public class IntervalSpaceDetector extends BaseDetector.JavaDetector {

    private Node currentNode, root;

    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        List<Class<? extends UElement>> list = new ArrayList<Class<? extends UElement>>(1);
        list.add(UClass.class);
        return list;
    }

    @Override
    public UElementHandler createUastHandler(final JavaContext context) {
        return new UElementHandler() {
            @Override
            public void visitClass(UClass node) {
                root = new Node();
                currentNode = root;

                putAll(node.getFields(), context);
                putAll(node.getInnerClasses(), context);
                putAll(node.getMethods(), context);
                putAll(node.getInitializers(), context);
                List<UComment> comments = node.getComments();
                if (comments.size() > 0) {
                    UComment uComment = comments.get(0);
                    Location commentLocation = context.getLocation(uComment);
                    Location classLocation = context.getNameLocation(node);
                    if (commentLocation.getEnd().getLine() < classLocation.getStart().getLine()) {
                        comments.remove(0);
                    }
                    putAll(comments, context);
                }
                currentNode = root.next;
                while (currentNode != null && currentNode.next != null) {
                    UElement element = currentNode.next.element;
                    if (currentNode.next.startLine - 2 != currentNode.endLine) {
                        context.report(FormatIssue.ISSUE_LINE_RULE, element, context.getLocation(element),
                                FormatIssue.ISSUE_LINE_RULE.getBriefDescription(TextFormat.TEXT));
                    }
                    currentNode = currentNode.next;
                }
            }
        };
    }

    private void putAll(UElement[] elements, JavaContext context) {
        for (UElement element : elements) {
            put(new Node(element, context));
        }
    }

    private void putAll(List<? extends UElement> elements, JavaContext context) {
        for (UElement element : elements) {
            put(new Node(element, context));
        }
    }

    private void put(Node node) {
        put(node, false);
    }

    private void put(Node node, boolean flag) {
        currentNode = find(node);
        if (currentNode == root) {
            node.next = currentNode.next;
            currentNode.next = node;
            currentNode = node;
            return;
        }

        if (node.canUpMerge(currentNode)) {
            currentNode.addNode(node);
            return;
        }
        Node nextNode = currentNode.next;
        if (nextNode != null && node.canMerge(nextNode)) {
            node.next = nextNode.next;
            currentNode.next = node;
            node.addNode(nextNode);
            return;
        }

        node.next = currentNode.next;
        currentNode.next = node;
        currentNode = node;
    }

    private Node find(Node node) {
        Node temp, tempNext;
        if (node.type == currentNode.type) {
            temp = currentNode;
        } else {
            temp = root;
        }
        tempNext = temp.next;
        while (tempNext != null && tempNext.endLine < node.startLine) {
            if (tempNext.next == null) {
                return tempNext;
            }
            temp = tempNext;
            tempNext = temp.next;
        }
        return temp;

    }

    private void print(Node root) {
        Node node = root.next;
        System.out.println("----------------");
        while (node != null) {
            System.out.print(node.toString());
            node = node.next;
        }
        System.out.println();
        System.out.println("----------------");
    }

    private class Node {
        private int startLine;
        private int endLine;
        private UElement element;
        private int type;   //1 Field 2 Comment 3 Method 4 Class 5 Initializer
        private int mergeType;

        private Node next;

        private Node() {
            this.element = null;
            type = mergeType = -1;

            startLine = 0;
            endLine = 0;
            next = null;
        }

        public Node(UElement element, JavaContext context) {
            this.element = element;
            type = getElementType(element);
            mergeType = type;
            Location location = context.getLocation(element);
            startLine = location.getStart().getLine();
            endLine = location.getEnd().getLine();
            next = null;
        }

        public void addNode(Node node) {
            endLine = node.endLine;
            mergeType = node.type;
            Node temp = next;
            if (temp != null && canMerge(temp)) {
                next = temp.next;
                addNode(temp);
            }
        }

        public boolean canMerge(Node node) {
            if (endLine + 2 == node.startLine)
                return true;
            if (mergeType > 0 && mergeType < 3 && mergeType == node.type && endLine + 1 >= node.startLine) {
                return true;
            }
            return false;
        }

        public boolean canUpMerge(Node node) {
            if (node.endLine + 2 == startLine)
                return true;
            if (type > 0 && type < 3 && node.mergeType == type && node.endLine + 1 >= startLine) {
                return true;
            }
            return false;
        }

        private int getElementType(UElement element) {
            if (element instanceof UField) return 1;
            if (element instanceof UComment) return 2;
            if (element instanceof UMethod) return 3;
            if (element instanceof UClass) return 4;
            if (element instanceof UClassInitializer) return 5;
            return -1;
        }


        @Override
        public String toString() {
            return "Node{start:" + startLine +
                    " end:" + endLine +
                    (element == null ? "" : " element:" + element.asRenderString()) +
                    "}\n";
        }
    }

}
