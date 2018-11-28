package com.qbutton.concbugs.notmine;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * @author max
 */
public class ComparingReferencesProvider implements InspectionToolProvider {
  public Class[] getInspectionClasses() {
    return new Class[]{ComparingReferencesInspection.class};
  }
}
