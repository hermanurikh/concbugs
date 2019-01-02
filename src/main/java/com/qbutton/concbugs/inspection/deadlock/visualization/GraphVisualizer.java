package com.qbutton.concbugs.inspection.deadlock.visualization;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

public class GraphVisualizer {
    private static final String CSS_PATH = "css/graph.css";

    public static void visualizeGraph(com.qbutton.concbugs.algorythm.dto.Graph graph) {
        Graph visualizedGraph = new MultiGraph("Deadlocks graph");
        visualizedGraph.setStrict(false);

        visualizedGraph.addAttribute("ui.stylesheet", "url('" + CSS_PATH + "')");

        graph.getNeighbors().forEach((originalFrom, originalTo) -> {
            String fromName = originalFrom.toString();
            Node from = visualizedGraph.addNode(fromName);

            setLabel(from, extractSimpleClassName(originalFrom.getClazz()));

            originalTo.forEach(ho -> {
                String toName = ho.toString();
                Node to = visualizedGraph.addNode(toName);
                setLabel(to, extractSimpleClassName(ho.getClazz()));

                Edge edge = visualizedGraph.addEdge(getName(fromName, toName), from, to, true);
                setLabel(edge, getName(originalFrom.getClazz(), ho.getClazz()));
            });

        });

        visualizedGraph.display();
    }

    private static void setLabel(Element element, String label) {
        element.addAttribute("ui.label", label);
    }

    private static String getName(String fromName, String toName) {
        return fromName + " ------> " + toName;
    }

    private static String extractSimpleClassName(String fullClassName) {
        return fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
    }
}
