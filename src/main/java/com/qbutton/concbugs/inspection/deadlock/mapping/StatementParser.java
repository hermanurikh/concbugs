package com.qbutton.concbugs.inspection.deadlock.mapping;

import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiBlockStatement;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiExpressionStatement;
import com.intellij.psi.PsiIfStatement;
import com.intellij.psi.PsiLoopStatement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiSynchronizedStatement;
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl;
import com.intellij.psi.search.searches.OverridingMethodsSearch;
import com.qbutton.concbugs.algorythm.dto.MethodDeclaration;
import com.qbutton.concbugs.algorythm.dto.MethodDeclaration.Variable;
import com.qbutton.concbugs.algorythm.dto.statement.BranchStatement;
import com.qbutton.concbugs.algorythm.dto.statement.CrossAssignmentStatement;
import com.qbutton.concbugs.algorythm.dto.statement.DeclarationStatement;
import com.qbutton.concbugs.algorythm.dto.statement.InnerAssignmentStatement;
import com.qbutton.concbugs.algorythm.dto.statement.MethodStatement;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import com.qbutton.concbugs.algorythm.dto.statement.SynchronizedStatement;
import com.qbutton.concbugs.algorythm.dto.statement.WaitStatement;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressFBWarnings({"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "UC_USELESS_OBJECT"})
@RequiredArgsConstructor
public class StatementParser {

    private final StatementShrinker statementShrinker;
    private final StatementMapper statementMapper;

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

    private Statement parseStatements(PsiCodeBlock psiCodeBlock) {
        PsiStatement[] statements = psiCodeBlock.getStatements();
        List<Statement> resultStatements = new ArrayList<>();

        Arrays.stream(statements)
                .forEach(psiStatement -> {
                    Statement statement = parseStatement(psiStatement);
                    resultStatements.add(statement);
                });


        return statementShrinker.shrinkStatements(resultStatements);
    }

    private Statement parseStatement(PsiStatement statement) {
        List<Statement> resultStatements = new ArrayList<>();

        BiConsumer<PsiStatement, List<Statement>> parseFunction = statementMapper.getParser(statement);
        parseFunction.accept(statement, resultStatements);

        return statementShrinker.shrinkStatements(resultStatements);
    }

    @SuppressFBWarnings("UC_USELESS_OBJECT")
    private void parseMethodCallExpression(PsiMethodCallExpression expression, List<Statement> statements, String resultVarName) {
        PsiMethod psiMethod = expression.resolveMethod();

        int textOffset = expression.getTextOffset();

        if ("wait".equals(psiMethod.getName()) && psiMethod.getParameterList().getParameters().length == 0) {
            PsiElement firstChild = expression.getFirstChild();
            statements.add(new WaitStatement(textOffset, firstChild.getFirstChild().getText()));
        } else {
            if (isLibraryMethod(psiMethod)) {
                return;
            }

            List<String> actualParameters = getActualParameters(expression, psiMethod);

            statements.add(parseMethod(psiMethod, resultVarName, textOffset, actualParameters));
        }
    }

    private MethodStatement parseMethod(PsiMethod psiMethod, String resultVarName, Integer initialTextOffset, List<String> actualParameters) {
        List<MethodDeclaration> methodDeclarations = new ArrayList<>();
        List<PsiMethod> methodsToParse = new ArrayList<>();

        int textOffset = initialTextOffset == null
                ? psiMethod.getTextOffset()
                : initialTextOffset;

        methodsToParse.add(psiMethod);
        methodsToParse.addAll(OverridingMethodsSearch.search(psiMethod).findAll());

        methodsToParse.forEach(addMethodDeclarationIfNeeded(psiMethod.getName(), methodDeclarations));

        String returnType = psiMethod.getReturnType().getCanonicalText();

        return new MethodStatement(textOffset, resultVarName, methodDeclarations, returnType, actualParameters);
    }

    /**
     * Top-level method parsing.
     * @param psiMethod psiMethod
     * @return resulting method statement
     */
    MethodStatement parseMethod(PsiMethod psiMethod) {
        return parseMethod(psiMethod, null, null, Collections.emptyList());
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
                .map(ifBlock -> this.parseStatements(((PsiBlockStatement) ifBlock).getCodeBlock()))
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
                this.parseStatements(psiSynchronizedStatement.getBody())));
    }

    void parseLoopStatement(PsiLoopStatement psiLoopStatement, List<Statement> statements) {
        PsiStatement body = psiLoopStatement.getBody();
        if (body == null) return;

        PsiElement firstChild = body.getFirstChild();

        if (!(firstChild instanceof PsiCodeBlock)) {
            throw new RuntimeException("PsiLoopStatement body firstChild is not a PsiCodeBlock: " + firstChild);
        }

        Statement statement = this.parseStatements((PsiCodeBlock) firstChild);

        statements.add(statement);
    }

    private Consumer<PsiMethod> addMethodDeclarationIfNeeded(String methodName,
                                                             List<MethodDeclaration> methodDeclarations) {
        return method -> {
            if (isLibraryMethod(method)) {
                return;
            }

            String className = method.getContainingClass().getQualifiedName();

            String synchronizationVarName = isStatic(method)
                    ? className + ".class"
                    : "this";

            //add this as a first argument for synchronization if it is instance method
            List<Variable> variables = new ArrayList<>();
            if (!isStatic(method)) {
                variables.add(new Variable(synchronizationVarName, className));
            }

            variables.addAll(
                    Arrays.stream(method.getParameterList().getParameters())
                            .map(param -> new Variable(param.getName(), param.getType().getCanonicalText()))
                            .collect(Collectors.toList())
            );

            PsiCodeBlock actualBody = method.getBody();
            Statement methodBody = this.parseStatements(actualBody);

            int textOffset = method.getTextOffset();

            methodBody = desugarSynchronizedIfNeeded(textOffset, method, synchronizationVarName, methodBody);

            methodDeclarations.add(
                    new MethodDeclaration(methodName, variables, methodBody, textOffset)
            );
        };
    }

    private boolean isStatic(PsiMethod method) {
        return method.getModifierList().hasModifierProperty("static");
    }

    private List<String> getActualParameters(PsiMethodCallExpression expression, PsiMethod psiMethod) {
    /*when m.getSomeDate(a)
    expression.getFirstChild() - PsiReferenceExpression m.getSomedate with first child PsiReferenceExpression m (expr.fc.fc.getText())
    expression.getLastChild() - PsiExpressionListImpl .getExpressions() [0] - PsiReferenceExpression a (((PsiExpressionListImpl) expression.getLastChild()).getExpressions()[0].getText())

    when getSomeDate(a) - same with lastchild, but firstChild - psiReferenceExpression getSomeDate with first child PsiReferenceParameterList
     */
        if (!(expression.getLastChild() instanceof PsiExpressionList)) {
            throw new RuntimeException("methodCallExpression is expected to have last child of PsiExpressionList");
        }

        List<String> actualParameters = new ArrayList<>();

        if (!isStatic(psiMethod)) {
            if (expression.getFirstChild().getFirstChild() instanceof PsiReferenceExpression) {
                //a method is called on some object, add it as first param
                actualParameters.add(expression.getFirstChild().getFirstChild().getText());
            } else {
                //a method is called on this
                actualParameters.add("this");
            }
        }

        actualParameters.addAll(
                Arrays.stream(((PsiExpressionList) expression.getLastChild()).getExpressions())
                        .map(PsiElement::getText)
                        .collect(Collectors.toList())
        );
        return actualParameters;
    }

    private Statement desugarSynchronizedIfNeeded(int textOffset,
                                                  PsiMethod method,
                                                  String synchronizationVarName,
                                                  Statement methodBody) {
        if (method.getModifierList().hasModifierProperty("synchronized")) {
            methodBody = new SynchronizedStatement(
                    textOffset, synchronizationVarName, methodBody);
        }
        return methodBody;
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

    private boolean isLibraryMethod(PsiMethod psiMethod) {
        return psiMethod.getBody() == null;
    }
}
