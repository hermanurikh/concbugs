package com.qbutton.concbugs.inspection.deadlock;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.tree.IElementType;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.StringTokenizer;

public class DeadlocksInspection extends AbstractBaseJavaLocalInspectionTool {
    private static final Logger LOG = Logger.getInstance("#com.intellij.codeInspection.DeadlocksInspection");


    @SuppressWarnings({"WeakerAccess"})
    @NonNls
    public String CHECKED_CLASSES = "java.lang.String;java.util.Date";
    @NonNls
    private static final String DESCRIPTION_TEMPLATE =
            InspectionsBundle.message("inspection.comparing.references.problem.descriptor");

    @NotNull
    public String getDisplayName() {

        return "'==' or '!=' instead of 'equals()'";
    }

    @NotNull
    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    @NotNull
    public String getShortName() {
        return "ComparingReferences";
    }

    private boolean isCheckedType(PsiType type) {
        if (!(type instanceof PsiClassType)) return false;

        StringTokenizer tokenizer = new StringTokenizer(CHECKED_CLASSES, ";");
        while (tokenizer.hasMoreTokens()) {
            String className = tokenizer.nextToken();
            if (type.equalsToText(className)) return true;
        }

        return false;
    }

    //JavaFindUsagesProvider::canFindUsagesFor
    //ReferencesSearch::search

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);

                PsiCodeBlock body = method.getBody();
                if (body != null) {
                    body.getStatements();
                    //todo
                }
            }

            @Override
            public void visitBinaryExpression(PsiBinaryExpression expression) {
                super.visitBinaryExpression(expression);
                IElementType opSign = expression.getOperationTokenType();
                if (opSign == JavaTokenType.EQEQ || opSign == JavaTokenType.NE) {
                    PsiExpression lOperand = expression.getLOperand();
                    PsiExpression rOperand = expression.getROperand();
                    if (rOperand == null || isNullLiteral(lOperand) || isNullLiteral(rOperand)) return;

                    PsiType lType = lOperand.getType();
                    PsiType rType = rOperand.getType();

                    if (isCheckedType(lType) || isCheckedType(rType)) {
                        holder.registerProblem(expression, DESCRIPTION_TEMPLATE);
                    }
                }
            }
        };
    }

    private static boolean isNullLiteral(PsiExpression expr) {
        return expr instanceof PsiLiteralExpression && "null".equals(expr.getText());
    }


    public JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JTextField checkedClasses = new JTextField(CHECKED_CLASSES);
        checkedClasses.getDocument().addDocumentListener(new DocumentAdapter() {
            public void textChanged(DocumentEvent event) {
                CHECKED_CLASSES = checkedClasses.getText();
            }
        });

        panel.add(checkedClasses);
        return panel;
    }

    public boolean isEnabledByDefault() {
        return false;
    }
}
