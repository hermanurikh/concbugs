package com.qbutton.concbugs.inspection.deadlock.mapping;

import com.google.common.collect.ImmutableList;
import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiBlockStatement;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiExpressionStatement;
import com.intellij.psi.PsiIfStatement;
import com.intellij.psi.PsiLoopStatement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiSynchronizedStatement;
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl;
import com.intellij.psi.search.searches.OverridingMethodsSearch;
import com.intellij.util.Query;
import com.qbutton.concbugs.algorythm.dto.MethodDeclaration;
import com.qbutton.concbugs.algorythm.dto.statement.BranchStatement;
import com.qbutton.concbugs.algorythm.dto.statement.CrossAssignmentStatement;
import com.qbutton.concbugs.algorythm.dto.statement.DeclarationStatement;
import com.qbutton.concbugs.algorythm.dto.statement.InnerAssignmentStatement;
import com.qbutton.concbugs.algorythm.dto.statement.MethodStatement;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import com.qbutton.concbugs.algorythm.dto.statement.SynchronizedStatement;
import com.qbutton.concbugs.algorythm.dto.statement.WaitStatement;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
public class StatementParser {

    private PsiToAlgorythmFacade psiToAlgorythmFacade;

    private void parseAssignmentExpression(PsiAssignmentExpression expression, List<Statement> statements) {
        if (!(expression.getFirstChild() instanceof PsiReferenceExpression)) {
            throw new RuntimeException("expression first child should be a PsiReferenceExpression, but it is " + expression.getFirstChild());
        }

        PsiReferenceExpression left = (PsiReferenceExpression) expression.getFirstChild();

        if (expression.getLastChild() instanceof PsiAssignmentExpression) {
            PsiAssignmentExpression lastChild = (PsiAssignmentExpression) expression.getLastChild();
            parseAssignmentExpression(lastChild, statements);

            CrossAssignmentStatement statement = new CrossAssignmentStatement(
                    left.getTextOffset(), left.getText(), lastChild.getFirstChild().getText());

            statements.add(statement);
        } else if (expression.getLastChild() instanceof PsiNewExpression) {
            DeclarationStatement statement = new DeclarationStatement(
                    left.getTextOffset(), left.getText(), getVarClass((PsiNewExpression) expression.getLastChild()));
            statements.add(statement);
        } else if (expression.getLastChild() instanceof PsiReferenceExpression) {
            PsiReferenceExpression right = (PsiReferenceExpression) expression.getLastChild();
            if (right.getLastChild().getText().equals(right.getText())) {
                //simple cross assignment
                CrossAssignmentStatement statement = new CrossAssignmentStatement(
                        left.getTextOffset(), left.getText(), right.getText()
                );
                statements.add(statement);
            } else {
                //inner assignment statement
                InnerAssignmentStatement statement = new InnerAssignmentStatement(
                        left.getTextOffset(), left.getText(), getVarClass(right)
                );
                statements.add(statement);
            }
        } else if (expression.getLastChild() instanceof PsiMethodCallExpression) {
            parseMethodCallExpression((PsiMethodCallExpression) expression.getLastChild(), statements, left.getText());
        }
    }

    private void parseMethodCallExpression(PsiMethodCallExpression expression, List<Statement> statements, String resultVarName) {
        PsiMethod psiMethod = expression.resolveMethod();

        String methodName = psiMethod.getName();
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();

        int textOffset = expression.getTextOffset();

        if ("wait".equals(methodName) && parameters.length == 0) {
            PsiElement firstChild = expression.getFirstChild();
            statements.add(new WaitStatement(textOffset, firstChild.getFirstChild().getText()));
        } else {
            String className = psiMethod.getContainingClass().getQualifiedName();
            PsiCodeBlock actualBody = psiMethod.getBody();
            if (actualBody == null) {
                //this is a library, don't analyze it
                return;
            }
            Statement methodBody = psiToAlgorythmFacade.parseStatements(actualBody);

            String synchronizationVarName;

            if (psiMethod.getModifierList().hasModifierProperty("static")) {
                synchronizationVarName = className + ".class";
            } else {
                synchronizationVarName = "this";
            }

            //add this or class variable as a first argument
            MethodDeclaration.Variable firstVariable = new MethodDeclaration.Variable(synchronizationVarName, className);

            if (psiMethod.getModifierList().hasModifierProperty("synchronized")) {
                //desugaring
                methodBody = new SynchronizedStatement(
                        textOffset, synchronizationVarName, methodBody);
            }

            List<MethodDeclaration.Variable> variables = new ArrayList<>();
            variables.add(firstVariable);

            variables.addAll(
                    Arrays.stream(parameters)
                            .map(param -> new MethodDeclaration.Variable(param.getName(), param.getType().getCanonicalText()))
                            .collect(Collectors.toList())
            );

            Query<PsiMethod> psiMethods = OverridingMethodsSearch.search(psiMethod);
            psiMethods.findAll();

            MethodDeclaration methodDeclaration = new MethodDeclaration(
                    methodName, variables, methodBody
            );

            String returnType = psiMethod.getReturnType().getCanonicalText();

            statements.add(new MethodStatement(
                    textOffset, resultVarName, ImmutableList.of(methodDeclaration), returnType)
            );
        }
    }

    void parseDeclarationStatement(PsiDeclarationStatement psiDeclarationStatement, List<Statement> statements) {
        PsiElement[] declaredElements = psiDeclarationStatement.getDeclaredElements();

        for (PsiElement declaredElement : declaredElements) {
            if (!(declaredElement instanceof PsiLocalVariableImpl)) {
                throw new RuntimeException("psiElement should be a local variable, but it is " + declaredElement);
            }

            PsiLocalVariableImpl localVar = (PsiLocalVariableImpl) declaredElement;
            PsiExpression initializer = localVar.getInitializer();


            if (initializer instanceof PsiReferenceExpression) {
                statements.add(new InnerAssignmentStatement(
                        localVar.getTextOffset(), localVar.getName(), getVarClass((PsiReferenceExpression) initializer)));
            } else {
                statements.add(new DeclarationStatement(
                        localVar.getTextOffset(), localVar.getName(), getVarClass(localVar)));
                if (initializer instanceof PsiMethodCallExpression) {
                    parseMethodCallExpression((PsiMethodCallExpression) initializer, statements, localVar.getName());
                }
            }
        }
    }

    void parseExpressionStatement(PsiExpressionStatement psiExpressionStatement, List<Statement> statements) {
        PsiElement expression = psiExpressionStatement.getFirstChild();
        if (expression instanceof PsiAssignmentExpression) {
            parseAssignmentExpression((PsiAssignmentExpression) expression, statements);
        } else if (expression instanceof PsiMethodCallExpression) {
            parseMethodCallExpression((PsiMethodCallExpression) expression, statements, null);
        }
    }

    void parseIfStatement(PsiIfStatement psiIfStatement, List<Statement> statements) {
        List<Statement> bodies = Arrays.stream(psiIfStatement.getChildren())
                .filter(child -> child instanceof PsiBlockStatement)
                .map(ifBlock -> psiToAlgorythmFacade.parseStatements(((PsiBlockStatement) ifBlock).getCodeBlock()))
                .collect(Collectors.toList());

        Statement ifStatement = null;
        Statement elseStatement = null;

        if (!bodies.isEmpty()) {
            ifStatement = bodies.get(0);
            if (bodies.size() > 1) {
                if (bodies.size() == 2) {
                    elseStatement = bodies.get(1);
                } else {
                    throw new RuntimeException("if statement contains more than 2 bodies - " + bodies);
                }
            }
        }

        statements.add(new BranchStatement(psiIfStatement.getTextOffset(), null, ifStatement, elseStatement));
    }

    void parseSynchronizedStatement(PsiSynchronizedStatement psiSynchronizedStatement, List<Statement> statements) {
        statements.add(new SynchronizedStatement(
                psiSynchronizedStatement.getTextOffset(),
                psiSynchronizedStatement.getLockExpression().getText(),
                psiToAlgorythmFacade.parseStatements(psiSynchronizedStatement.getBody())));
    }

    void parseLoopStatement(PsiLoopStatement psiLoopStatement, List<Statement> statements) {
        PsiStatement body = psiLoopStatement.getBody();
        if (body == null) return;

        PsiElement firstChild = body.getFirstChild();

        if (!(firstChild instanceof PsiCodeBlock)) {
            throw new RuntimeException("PsiLoopStatement body firstChild is not a PsiCodeBlock: " + firstChild);
        }

        Statement statement = psiToAlgorythmFacade.parseStatements((PsiCodeBlock) firstChild);

        statements.add(statement);
    }

    private String getVarClass(PsiLocalVariableImpl localVariable) {
        return localVariable.getType().getCanonicalText();
    }

    private String getVarClass(PsiReferenceExpression expression) {
        return doGetVarClass(expression);
    }

    private String getVarClass(PsiNewExpression expression) {
        return doGetVarClass(expression);
    }

    private String doGetVarClass(PsiExpression expression) {
        return expression.getType().getCanonicalText();
    }

    public void setPsiToAlgorythmFacade(PsiToAlgorythmFacade psiToAlgorythmFacade) {
        this.psiToAlgorythmFacade = psiToAlgorythmFacade;
    }
}
