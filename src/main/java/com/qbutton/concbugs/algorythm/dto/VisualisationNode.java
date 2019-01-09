package com.qbutton.concbugs.algorythm.dto;

import lombok.Data;

@Data
public class VisualisationNode {
    private final String clazz;
    private final String enclosingMethod;
    private final String varName;
}
