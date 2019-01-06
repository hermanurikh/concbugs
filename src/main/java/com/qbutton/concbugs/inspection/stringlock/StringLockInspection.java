package com.qbutton.concbugs.inspection.stringlock;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiSynchronizedStatement;
import com.intellij.psi.PsiType;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.StringTokenizer;

public class StringLockInspection extends AbstractBaseJavaLocalInspectionTool {

  @SuppressWarnings({"WeakerAccess"})
  @NonNls
  public String CHECKED_CLASSES = "java.lang.String";

  @NonNls
  private static final String DESCRIPTION_TEMPLATE = "String %s used as a lock";

  @NotNull
  public String getDisplayName() {
    return "String should better not be used as a lock";
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
      if (type.equalsToText(className)) return true;
    }

    return false;
  }

  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new JavaElementVisitor() {

      @Override
      public void visitSynchronizedStatement(PsiSynchronizedStatement statement) {
        super.visitSynchronizedStatement(statement);

          PsiExpression lockExpression = statement.getLockExpression();
          if (lockExpression != null) {
              PsiType type = lockExpression.getType();
              if (isCheckedType(type)) {
                  holder.registerProblem(statement,
                          String.format(DESCRIPTION_TEMPLATE, lockExpression.getText()));
              }
          }
      }
    };
  }

  @Override
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
}
