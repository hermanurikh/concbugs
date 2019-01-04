package com.qbutton.concbugs.inspection.oldcollections;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiFieldImpl;
import com.intellij.psi.impl.source.tree.ChildRole;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.IncorrectOperationException;
import com.qbutton.concbugs.algorythm.exception.IdeaIntegrationException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringTokenizer;

@SuppressFBWarnings({"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "NP_NULL_ON_SOME_PATH", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
public class OldCollectionsInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final Logger LOG = Logger.getInstance("#com.intellij.codeInspection.OldCollectionsInspection");

    private final LocalQuickFix localQuickFix = new OldCollectionsFix();

    @SuppressWarnings({"WeakerAccess"})
    @NonNls
    public String CHECKED_CLASSES = "java.util.Hashtable";

    @NonNls
    private static final String DESCRIPTION_TEMPLATE = "java.util.Hashtable may better be replaced by java.util.concurrent.ConcurrentHashMap";

    @NotNull
    public String getDisplayName() {

        return "java.util.Hashtable used instead of java.util.concurrent.ConcurrentHashMap";
    }

    @NotNull
    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    private boolean isCheckedType(PsiType type) {
        if (!(type instanceof PsiClassType)) return false;

        StringTokenizer tokenizer = new StringTokenizer(CHECKED_CLASSES, ";");
        while (tokenizer.hasMoreTokens()) {
            String className = tokenizer.nextToken();
            Project openProject = ProjectManager.getInstance().getOpenProjects()[0];
            if (type.isAssignableFrom(
                    PsiType.getTypeByName(className, openProject, GlobalSearchScope.allScope(openProject)))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitAssignmentExpression(PsiAssignmentExpression expression) {
                if (expression.getRExpression() instanceof PsiNewExpression) {
                    PsiNewExpression newExpression = (PsiNewExpression) expression.getRExpression();

                    checkNewExpression(expression, newExpression);
                }
            }

            @Override
            public void visitField(PsiField field) {
                PsiElement psiElement = ((PsiFieldImpl) field).getNode().findChildByRoleAsPsiElement(ChildRole.INITIALIZER);

                if (psiElement != null) {
                    PsiNewExpression newExpression = (PsiNewExpression) psiElement;

                    checkNewExpression(field, newExpression);
                }
            }

            private void checkNewExpression(PsiElement element, PsiNewExpression newExpression) {
                if (isCheckedType(newExpression.getType())) {
                    holder.registerProblem(element, DESCRIPTION_TEMPLATE, localQuickFix);
                }
            }
        };
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
        return true;
    }

    private static class OldCollectionsFix implements LocalQuickFix {
        @NotNull
        public String getName() {
            // The test (see the TestThisPlugin class) uses this string to identify the quick fix action.
            return "Use java.util.concurrent.ConcurrentHashMap";
        }


        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            try {
                PsiNewExpression psiNewExpression = getPsiNewExpression(descriptor);

                JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
                PsiElementFactory factory = psiFacade.getElementFactory();

                addImportIfNecessary(project, psiNewExpression, psiFacade, factory);

                replaceNewExpression(psiNewExpression, factory);

            } catch (IncorrectOperationException e) {
                LOG.error(e);
            }
        }

        private PsiNewExpression getPsiNewExpression(@NotNull ProblemDescriptor descriptor) {
            PsiNewExpression psiNewExpression;
            PsiElement descriptorElement = descriptor.getPsiElement();
            if (descriptorElement instanceof PsiField) {
                PsiFieldImpl fieldDescriptor = (PsiFieldImpl) descriptorElement;
                psiNewExpression = (PsiNewExpression) fieldDescriptor.getNode().findChildByRoleAsPsiElement(ChildRole.INITIALIZER);
            } else if (descriptorElement instanceof PsiAssignmentExpression) {
                PsiAssignmentExpression assignmentDescriptor = (PsiAssignmentExpression) descriptorElement;
                psiNewExpression = (PsiNewExpression) assignmentDescriptor.getRExpression();
            } else {
                throw new IllegalArgumentException("unknown problem descriptor: " + descriptor);
            }
            return psiNewExpression;
        }

        private void addImportIfNecessary(@NotNull Project project,
                                          PsiNewExpression psiNewExpression,
                                          JavaPsiFacade psiFacade,
                                          PsiElementFactory factory) {
            PsiClass psiClass = psiFacade.findClass("java.util.concurrent.ConcurrentHashMap", GlobalSearchScope.allScope(project));

            if (psiClass == null) {
                //this should never happen
                throw new IdeaIntegrationException("class java.util.concurrent.ConcurrentHashMap could not be found");
            }

            PsiImportStatement importStatement = factory.createImportStatement(psiClass);

            final PsiImportList importList = ((PsiJavaFile) PsiUtil.getTopLevelClass(psiNewExpression).getParent()).getImportList();
            if (Arrays.stream(importList.getImportStatements())
                    .noneMatch(stmt -> Objects.equals(stmt.getQualifiedName(), importStatement.getQualifiedName()))) {

                importList.add(importStatement);
            }
        }

        private void replaceNewExpression(PsiNewExpression psiNewExpression, PsiElementFactory factory) {
            PsiExpression expressionFromText
                    = factory.createExpressionFromText("new ConcurrentHashMap<>()", null);

            psiNewExpression.replace(expressionFromText);
        }

        @NotNull
        public String getFamilyName() {
            return getName();
        }
    }
}
