package com.qbutton.concbugs.notmine;

import com.intellij.codeInspection.InspectionToolProvider;
import org.jetbrains.annotations.NotNull;

public class ConcBugsProvider implements InspectionToolProvider {
    @NotNull
    @Override
    public Class[] getInspectionClasses() {
        return new Class[0];
    }
}
