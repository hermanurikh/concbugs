package com.qbutton.concbugs.inspection.stringlock;

import com.intellij.codeInspection.InspectionToolProvider;

public class StringLockProvider implements InspectionToolProvider {
  public Class[] getInspectionClasses() {
    return new Class[]{StringLockInspection.class};
  }
}
