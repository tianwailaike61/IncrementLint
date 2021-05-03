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

package com.twlk.lib_rules.base;

import com.android.tools.lint.detector.api.Context;

import org.jetbrains.uast.UAnnotation;
import org.jetbrains.uast.UArrayAccessExpression;
import org.jetbrains.uast.UBinaryExpression;
import org.jetbrains.uast.UBinaryExpressionWithType;
import org.jetbrains.uast.UBlockExpression;
import org.jetbrains.uast.UBreakExpression;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UCallableReferenceExpression;
import org.jetbrains.uast.UCatchClause;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UClassInitializer;
import org.jetbrains.uast.UClassLiteralExpression;
import org.jetbrains.uast.UContinueExpression;
import org.jetbrains.uast.UDeclaration;
import org.jetbrains.uast.UDeclarationsExpression;
import org.jetbrains.uast.UDoWhileExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UEnumConstant;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UExpressionList;
import org.jetbrains.uast.UField;
import org.jetbrains.uast.UFile;
import org.jetbrains.uast.UForEachExpression;
import org.jetbrains.uast.UForExpression;
import org.jetbrains.uast.UIfExpression;
import org.jetbrains.uast.UImportStatement;
import org.jetbrains.uast.ULabeledExpression;
import org.jetbrains.uast.ULambdaExpression;
import org.jetbrains.uast.ULiteralExpression;
import org.jetbrains.uast.ULocalVariable;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UObjectLiteralExpression;
import org.jetbrains.uast.UParameter;
import org.jetbrains.uast.UParenthesizedExpression;
import org.jetbrains.uast.UPolyadicExpression;
import org.jetbrains.uast.UPostfixExpression;
import org.jetbrains.uast.UPrefixExpression;
import org.jetbrains.uast.UQualifiedReferenceExpression;
import org.jetbrains.uast.UReturnExpression;
import org.jetbrains.uast.USimpleNameReferenceExpression;
import org.jetbrains.uast.USuperExpression;
import org.jetbrains.uast.USwitchClauseExpression;
import org.jetbrains.uast.USwitchExpression;
import org.jetbrains.uast.UThisExpression;
import org.jetbrains.uast.UThrowExpression;
import org.jetbrains.uast.UTryExpression;
import org.jetbrains.uast.UTypeReferenceExpression;
import org.jetbrains.uast.UUnaryExpression;
import org.jetbrains.uast.UVariable;
import org.jetbrains.uast.UWhileExpression;
import org.jetbrains.uast.visitor.UastVisitor;

/**
 * @author twlk
 * @date 2018/10/10
 */
public class BaseUastVisitor<T extends Context> implements UastVisitor {

    protected T context;

    public void start(T context, UElement element) {
        this.context = context;
        element.accept(this);
    }

    @Override
    public boolean visitAnnotation(UAnnotation uAnnotation) {
        return false;
    }

    @Override
    public boolean visitArrayAccessExpression(UArrayAccessExpression uArrayAccessExpression) {
        return false;
    }

    @Override
    public boolean visitBinaryExpression(UBinaryExpression uBinaryExpression) {
        return false;
    }

    @Override
    public boolean visitBinaryExpressionWithType(UBinaryExpressionWithType uBinaryExpressionWithType) {
        return false;
    }

    @Override
    public boolean visitBlockExpression(UBlockExpression uBlockExpression) {
        return false;
    }

    @Override
    public boolean visitBreakExpression(UBreakExpression uBreakExpression) {
        return false;
    }

    @Override
    public boolean visitCallExpression(UCallExpression uCallExpression) {
        return false;
    }

    @Override
    public boolean visitCallableReferenceExpression(UCallableReferenceExpression uCallableReferenceExpression) {
        return false;
    }

    @Override
    public boolean visitCatchClause(UCatchClause uCatchClause) {
        return false;
    }

    @Override
    public boolean visitClass(UClass uClass) {
        return false;
    }

    @Override
    public boolean visitClassLiteralExpression(UClassLiteralExpression uClassLiteralExpression) {
        return false;
    }

    @Override
    public boolean visitContinueExpression(UContinueExpression uContinueExpression) {
        return false;
    }

    @Override
    public boolean visitDeclarationsExpression(UDeclarationsExpression uDeclarationsExpression) {
        return false;
    }

    @Override
    public boolean visitDoWhileExpression(UDoWhileExpression uDoWhileExpression) {
        return false;
    }

    @Override
    public boolean visitElement(UElement uElement) {
        return false;
    }

    @Override
    public boolean visitEnumConstant(UEnumConstant uEnumConstant) {
        return false;
    }

    @Override
    public boolean visitExpressionList(UExpressionList uExpressionList) {
        return false;
    }

    @Override
    public boolean visitField(UField uField) {
        return false;
    }

    @Override
    public boolean visitFile(UFile uFile) {
        return false;
    }

    @Override
    public boolean visitForEachExpression(UForEachExpression uForEachExpression) {
        return false;
    }

    @Override
    public boolean visitForExpression(UForExpression uForExpression) {
        return false;
    }

    @Override
    public boolean visitIfExpression(UIfExpression uIfExpression) {
        return false;
    }

    @Override
    public boolean visitImportStatement(UImportStatement uImportStatement) {
        return false;
    }

    @Override
    public boolean visitInitializer(UClassInitializer uClassInitializer) {
        return false;
    }

    @Override
    public boolean visitLabeledExpression(ULabeledExpression uLabeledExpression) {
        return false;
    }

    @Override
    public boolean visitLambdaExpression(ULambdaExpression uLambdaExpression) {
        return false;
    }

    @Override
    public boolean visitLiteralExpression(ULiteralExpression uLiteralExpression) {
        return false;
    }

    @Override
    public boolean visitLocalVariable(ULocalVariable uLocalVariable) {
        return false;
    }

    @Override
    public boolean visitMethod(UMethod uMethod) {
        return false;
    }

    @Override
    public boolean visitObjectLiteralExpression(UObjectLiteralExpression uObjectLiteralExpression) {
        return false;
    }

    @Override
    public boolean visitParameter(UParameter uParameter) {
        return false;
    }

    @Override
    public boolean visitParenthesizedExpression(UParenthesizedExpression uParenthesizedExpression) {
        return false;
    }

    @Override
    public boolean visitPolyadicExpression(UPolyadicExpression uPolyadicExpression) {
        return false;
    }

    @Override
    public boolean visitPostfixExpression(UPostfixExpression uPostfixExpression) {
        return false;
    }

    @Override
    public boolean visitPrefixExpression(UPrefixExpression uPrefixExpression) {
        return false;
    }

    @Override
    public boolean visitQualifiedReferenceExpression(UQualifiedReferenceExpression uQualifiedReferenceExpression) {
        return false;
    }

    @Override
    public boolean visitReturnExpression(UReturnExpression uReturnExpression) {
        return false;
    }

    @Override
    public boolean visitSimpleNameReferenceExpression(USimpleNameReferenceExpression uSimpleNameReferenceExpression) {
        return false;
    }

    @Override
    public boolean visitSuperExpression(USuperExpression uSuperExpression) {
        return false;
    }

    @Override
    public boolean visitSwitchClauseExpression(USwitchClauseExpression uSwitchClauseExpression) {
        return false;
    }

    @Override
    public boolean visitSwitchExpression(USwitchExpression uSwitchExpression) {
        return false;
    }

    @Override
    public boolean visitThisExpression(UThisExpression uThisExpression) {
        return false;
    }

    @Override
    public boolean visitThrowExpression(UThrowExpression uThrowExpression) {
        return false;
    }

    @Override
    public boolean visitTryExpression(UTryExpression uTryExpression) {
        return false;
    }

    @Override
    public boolean visitTypeReferenceExpression(UTypeReferenceExpression uTypeReferenceExpression) {
        return false;
    }

    @Override
    public boolean visitUnaryExpression(UUnaryExpression uUnaryExpression) {
        return false;
    }

    @Override
    public boolean visitVariable(UVariable uVariable) {
        return false;
    }

    @Override
    public boolean visitWhileExpression(UWhileExpression uWhileExpression) {
        return false;
    }

    @Override
    public boolean visitDeclaration(UDeclaration uDeclaration) {
        return false;
    }

    @Override
    public boolean visitExpression(UExpression uExpression) {
        return false;
    }

    @Override
    public void afterVisitAnnotation(UAnnotation uAnnotation) {

    }

    @Override
    public void afterVisitArrayAccessExpression(UArrayAccessExpression uArrayAccessExpression) {

    }

    @Override
    public void afterVisitBinaryExpression(UBinaryExpression uBinaryExpression) {

    }

    @Override
    public void afterVisitBinaryExpressionWithType(UBinaryExpressionWithType uBinaryExpressionWithType) {

    }

    @Override
    public void afterVisitBlockExpression(UBlockExpression uBlockExpression) {

    }

    @Override
    public void afterVisitBreakExpression(UBreakExpression uBreakExpression) {

    }

    @Override
    public void afterVisitCallExpression(UCallExpression uCallExpression) {

    }

    @Override
    public void afterVisitCallableReferenceExpression(UCallableReferenceExpression uCallableReferenceExpression) {

    }

    @Override
    public void afterVisitCatchClause(UCatchClause uCatchClause) {

    }

    @Override
    public void afterVisitClass(UClass uClass) {

    }

    @Override
    public void afterVisitClassLiteralExpression(UClassLiteralExpression uClassLiteralExpression) {

    }

    @Override
    public void afterVisitContinueExpression(UContinueExpression uContinueExpression) {

    }

    @Override
    public void afterVisitDeclaration(UDeclaration uDeclaration) {

    }

    @Override
    public void afterVisitDeclarationsExpression(UDeclarationsExpression uDeclarationsExpression) {

    }

    @Override
    public void afterVisitDoWhileExpression(UDoWhileExpression uDoWhileExpression) {

    }

    @Override
    public void afterVisitElement(UElement uElement) {

    }

    @Override
    public void afterVisitEnumConstant(UEnumConstant uEnumConstant) {

    }

    @Override
    public void afterVisitExpression(UExpression uExpression) {

    }

    @Override
    public void afterVisitExpressionList(UExpressionList uExpressionList) {

    }

    @Override
    public void afterVisitField(UField uField) {

    }

    @Override
    public void afterVisitFile(UFile uFile) {

    }

    @Override
    public void afterVisitForEachExpression(UForEachExpression uForEachExpression) {

    }

    @Override
    public void afterVisitForExpression(UForExpression uForExpression) {

    }

    @Override
    public void afterVisitIfExpression(UIfExpression uIfExpression) {

    }

    @Override
    public void afterVisitImportStatement(UImportStatement uImportStatement) {

    }

    @Override
    public void afterVisitInitializer(UClassInitializer uClassInitializer) {

    }

    @Override
    public void afterVisitLabeledExpression(ULabeledExpression uLabeledExpression) {

    }

    @Override
    public void afterVisitLambdaExpression(ULambdaExpression uLambdaExpression) {

    }

    @Override
    public void afterVisitLiteralExpression(ULiteralExpression uLiteralExpression) {

    }

    @Override
    public void afterVisitLocalVariable(ULocalVariable uLocalVariable) {

    }

    @Override
    public void afterVisitMethod(UMethod uMethod) {

    }

    @Override
    public void afterVisitObjectLiteralExpression(UObjectLiteralExpression uObjectLiteralExpression) {

    }

    @Override
    public void afterVisitParameter(UParameter uParameter) {

    }

    @Override
    public void afterVisitParenthesizedExpression(UParenthesizedExpression uParenthesizedExpression) {

    }

    @Override
    public void afterVisitPolyadicExpression(UPolyadicExpression uPolyadicExpression) {

    }

    @Override
    public void afterVisitPostfixExpression(UPostfixExpression uPostfixExpression) {

    }

    @Override
    public void afterVisitPrefixExpression(UPrefixExpression uPrefixExpression) {

    }

    @Override
    public void afterVisitQualifiedReferenceExpression(UQualifiedReferenceExpression uQualifiedReferenceExpression) {

    }

    @Override
    public void afterVisitReturnExpression(UReturnExpression uReturnExpression) {

    }

    @Override
    public void afterVisitSimpleNameReferenceExpression(USimpleNameReferenceExpression uSimpleNameReferenceExpression) {

    }

    @Override
    public void afterVisitSuperExpression(USuperExpression uSuperExpression) {

    }

    @Override
    public void afterVisitSwitchClauseExpression(USwitchClauseExpression uSwitchClauseExpression) {

    }

    @Override
    public void afterVisitSwitchExpression(USwitchExpression uSwitchExpression) {

    }

    @Override
    public void afterVisitThisExpression(UThisExpression uThisExpression) {

    }

    @Override
    public void afterVisitThrowExpression(UThrowExpression uThrowExpression) {

    }

    @Override
    public void afterVisitTryExpression(UTryExpression uTryExpression) {

    }

    @Override
    public void afterVisitTypeReferenceExpression(UTypeReferenceExpression uTypeReferenceExpression) {

    }

    @Override
    public void afterVisitUnaryExpression(UUnaryExpression uUnaryExpression) {

    }

    @Override
    public void afterVisitVariable(UVariable uVariable) {

    }

    @Override
    public void afterVisitWhileExpression(UWhileExpression uWhileExpression) {

    }
}
