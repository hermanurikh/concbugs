package com.qbutton.concbugs.inspection.deadlock;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.DocumentAdapter;
import com.qbutton.concbugs.algorythm.AlgorythmFacade;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.statement.MethodStatement;
import com.qbutton.concbugs.di.BeanFactory;
import com.qbutton.concbugs.inspection.deadlock.mapping.PsiToAlgorythmFacade;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@SuppressFBWarnings({"DLS_DEAD_LOCAL_STORE", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"})
public class DeadlocksInspection extends AbstractBaseJavaLocalInspectionTool {
    private static final Logger LOG = Logger.getInstance("#com.intellij.codeInspection.DeadlocksInspection");


    @SuppressWarnings({"WeakerAccess"})
    @NonNls
    public String CHECKED_METHODS = "all public methods of selected scope";

    @NotNull
    public String getDisplayName() {

        return "build lock-order graph";
    }

    @NotNull
    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    //JavaFindUsagesProvider::canFindUsagesFor
    //ReferencesSearch::search

    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            private final List<MethodStatement> methodStatements = new ArrayList<>();

            private final PsiToAlgorythmFacade parsingFacade =  BeanFactory.getBean(PsiToAlgorythmFacade.class);
            private final AlgorythmFacade algorythmFacade =  BeanFactory.getBean(AlgorythmFacade.class);

            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);

                if (!isLibraryMethod(method) || isOnTheFly) {
                    //only public library methods are analyzed and through analyze action
                    return;
                }

                MethodStatement methodStatement = parsingFacade.parseMethod(method);
                methodStatements.add(methodStatement);
            }

            @Override
            public void visitClass(PsiClass aClass) {
                super.visitClass(aClass);

                if (isOnTheFly) {
                    return;
                }

                Graph graph = algorythmFacade.visitLibrary(methodStatements);

                System.out.println(graph);

                methodStatements.clear();
            }
        };
    }

    private boolean isLibraryMethod(PsiMethod method) {
        return method.getModifierList().hasModifierProperty("public") &&
                method.getContainingClass().getModifierList().hasModifierProperty("public");
    }

    public JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JTextField checkedClasses = new JTextField(CHECKED_METHODS);
        checkedClasses.getDocument().addDocumentListener(new DocumentAdapter() {
            public void textChanged(DocumentEvent event) {
                CHECKED_METHODS = checkedClasses.getText();
            }
        });

        panel.add(checkedClasses);
        return panel;
    }

    public boolean isEnabledByDefault() {
        return false;
    }
}
