package com.qbutton.concbugs.inspection.deadlock.visualization;

import com.qbutton.concbugs.algorythm.dto.VisualisationNode;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import java.util.Map;
import java.util.Set;

public class GraphVisualizer {
    private static final String CSS_PATH = "css/graph.css";

    public static void visualizeGraph(Map<VisualisationNode, Set<VisualisationNode>> graph) {
        Graph visualizedGraph = new MultiGraph("Deadlocks graph");
        visualizedGraph.setStrict(false);

        visualizedGraph.addAttribute("ui.stylesheet", "url('" + CSS_PATH + "')");

        graph.forEach((originalFrom, originalTo) -> {
            String fromName = originalFrom.getClazz();
            Node from = visualizedGraph.addNode(fromName);

            setLabel(from, extractSimpleClassName(originalFrom.getClazz()));

            originalTo.forEach(ho -> {
                String toName = ho.getClazz();
                Node to = visualizedGraph.addNode(toName);
                setLabel(to, extractSimpleClassName(ho.getClazz()));

                String edgeDisplayedName = getName(originalFrom, ho);
                Edge edge = visualizedGraph.addEdge(edgeDisplayedName, from, to, true);

                setLabel(edge, edgeDisplayedName);
            });

        });

        visualizedGraph.display();
    }

    private static void setLabel(Element element, String label) {
        element.addAttribute("ui.label", label);
    }

    private static String getName(VisualisationNode from, VisualisationNode to) {
        return from.getEnclosingMethod() + "=>" + from.getVarName()
                + " ------> "
                + to.getEnclosingMethod() + "=>" + to.getVarName();
    }

    private static String extractSimpleClassName(String fullClassName) {
        return fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
    }
}
