package com.fiuba.fallas.dos.sn.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.orientechnologies.common.util.OCallable;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import com.tinkerpop.blueprints.impls.orient.OrientEdgeType;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import java.util.HashMap;
import java.util.Map;

class EdgeFromFile {
    String out;
    String in;
    String label;
}

public class SemanticNetworkMain {

    private static List<String> vertexesFromFile;
    private static List<EdgeFromFile> edgesFromFile;
    private static Map<String, Vertex> vertexes;
    private static Map<String, OrientEdge> edges;
    private static OrientGraphFactory ogf;

    public static void main(String[] args) {
        loadVertexes();
        loadEdges();
        OrientGraph og = initOg();
        initVertexes(og);
        initEdges(og);
    }

    private static void loadVertexes() {
        vertexesFromFile = new ArrayList<>();
        String fileName = "./src/main/resources/Vertexes.txt";
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach((line)->{
                vertexesFromFile.add(line.trim());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Nodos:");
        for (String vertex : vertexesFromFile) {
            System.out.println(vertex);
        }
        System.out.println();
    }

    private static void loadEdges() {
        edgesFromFile = new ArrayList<>();
        String fileName = "./src/main/resources/Edges.txt";
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach((line)->{
                String[] parsedLine = line.split("-");
                EdgeFromFile edge = new EdgeFromFile();
                edge.out = parsedLine[0].trim();
                edge.label = parsedLine[1].trim();
                edge.in = parsedLine[2].trim();
                edgesFromFile.add(edge);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Aristas:");
        for (EdgeFromFile e : edgesFromFile) {
            System.out.println(e.out + " -> " + e.label + " -> " + e.in);
        }
        System.out.println();
    }

    private static OrientGraph initOg() {
        OrientGraphFactory ogf = new OrientGraphFactory("remote:127.0.0.1:2424/fallas_db", "fallas", "fallas123");
        final OrientGraph og = ogf.getTx();
        og.executeOutsideTx(new OCallable<Object, OrientBaseGraph>() {
            @Override
            public Object call(OrientBaseGraph iArgument) {
                /* Se limpia la base y definen clases.*/
                initVertexType(og);
                initEdgeType(og);
                return null;
            }

            private void initVertexType(OrientGraph og) {
                for (String vertex : vertexesFromFile) {
                    OrientVertexType vertexType = og.getVertexType(vertex);
                    if (vertexType != null) {
                        og.dropVertexType(vertex);
                    }
                }
                List<String> addedVertexs = new ArrayList<>();
                for (String vertex : vertexesFromFile) {
                    if (!addedVertexs.contains(vertex)) {
                        og.createVertexType(vertex);
                    }
                    addedVertexs.add(vertex);
                }
            }

            private void initEdgeType(OrientGraph og) {
                for (EdgeFromFile e : edgesFromFile) {
                    OrientEdgeType edgeType = og.getEdgeType(e.label);
                    if (edgeType != null) {
                        og.dropEdgeType(e.label);
                    }
                }
                List<String> addedLabels = new ArrayList<>();
                for (EdgeFromFile e : edgesFromFile) {
                    if (!addedLabels.contains(e.label)) {
                        og.createEdgeType(e.label);
                    }
                    addedLabels.add(e.label);
                }
            }
        });
        return og;
    }

    private static void initVertexes(OrientGraph og) {
        vertexes = new HashMap<>();
        for (String vertexAsString : vertexesFromFile) {
            Vertex vertex = og.addVertex("class:" + vertexAsString);
            vertex.setProperty("value", vertexAsString);
            vertexes.put(vertex.getProperty("value").toString(), vertex);
        }
        og.commit();
    }

    private static void initEdges(OrientGraph og) {
        edges = new HashMap<>();
        for (EdgeFromFile e : edgesFromFile) {
            OrientEdge edge = og.addEdge(null, vertexes.get(e.out), vertexes.get(e.in), e.label);
            edges.put(e.out + e.in, edge);
        }
        og.commit();
    }
}
