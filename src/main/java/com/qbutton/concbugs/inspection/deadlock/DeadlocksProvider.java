package com.qbutton.concbugs.inspection.deadlock;

import com.intellij.codeInspection.InspectionToolProvider;
import org.jetbrains.annotations.NotNull;

public class DeadlocksProvider implements InspectionToolProvider {
    @NotNull
    @Override
    public Class[] getInspectionClasses() {
        return new Class[]{DeadlocksInspection.class};
    }
}
