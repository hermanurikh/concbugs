package com.qbutton.concbugs.inspection.oldcollections;

import com.intellij.codeInspection.InspectionToolProvider;

public class OldCollectionsProvider implements InspectionToolProvider {
  public Class[] getInspectionClasses() {
    return new Class[]{OldCollectionsInspection.class};
  }
}
