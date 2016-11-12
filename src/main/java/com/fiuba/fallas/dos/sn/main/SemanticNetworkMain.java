package com.fiuba.fallas.dos.sn.main;

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

public class SemanticNetworkMain {

    private static Map<String, Vertex> vertexes;
    private static Map<String, OrientEdge> edges;
    private static OrientGraphFactory ogf;

    public static void main(String[] args) {
        OrientGraph og = initOg();
        System.out.println("Cant vertices inicio: " + og.countVertices());
        initVertexes(og);
        System.out.println("Cant vertices desp agregar vertices: " + og.countVertices());
        System.out.println("Cant aristas antes agregar aristas: " + og.countEdges());
        initEdges(og);
        System.out.println("Cant aristas desp agregar aristas: " + og.countEdges());

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
                OrientVertexType vertexTypePersona = og.getVertexType("Persona");
                OrientVertexType vertexTypeCuerpo = og.getVertexType("Cuerpo");
                OrientVertexType vertexTypeRopa = og.getVertexType("Ropa");
                if (vertexTypePersona != null) {
                    og.dropVertexType("Persona");
                }
                if (vertexTypeCuerpo != null) {
                    og.dropVertexType("Cuerpo");
                }
                if (vertexTypeRopa != null) {
                    og.dropVertexType("Ropa");
                }

                og.createVertexType("Persona");
                og.createVertexType("Cuerpo");
                og.createVertexType("Ropa");
            }

            private void initEdgeType(OrientGraph og) {
                OrientEdgeType edgeTypeRelacion = og.getEdgeType("Relacion");
                if (edgeTypeRelacion != null) {
                    og.dropEdgeType("Relacion");
                }
                og.createEdgeType("Relacion");
            }
        });
        return og;
    }

    private static void initVertexes(OrientGraph og) {
        vertexes = new HashMap<>();
        Vertex vPersona = og.addVertex("class:Persona");
        vPersona.setProperty("value", "Persona");
        Vertex vCuerpo = og.addVertex("class:Cuerpo");
        vCuerpo.setProperty("value", "Cuerpo");
        Vertex vRopa = og.addVertex("class:Ropa");
        vRopa.setProperty("value", "Ropa");
        Vertex vSombrero = og.addVertex("class:Ropa");
        vSombrero.setProperty("value", "Sombrero");
        vertexes.put(vPersona.getProperty("value").toString(), vPersona);
        vertexes.put(vCuerpo.getProperty("value").toString(), vCuerpo);
        vertexes.put(vRopa.getProperty("value").toString(), vRopa);
        vertexes.put(vSombrero.getProperty("value").toString(), vSombrero);
        og.commit();
    }

    private static void initEdges(OrientGraph og) {
        edges = new HashMap<>();
        OrientEdge ePersonaCuerpo = og.addEdge(null, vertexes.get("Persona"), vertexes.get("Cuerpo"), "Relacion");
        ePersonaCuerpo.setProperty("value", "tiene-un");
        OrientEdge ePersonaRopa = og.addEdge(null, vertexes.get("Persona"), vertexes.get("Ropa"), "Relacion");
        ePersonaRopa.setProperty("value", "viste");
        OrientEdge eRopaSombrero = og.addEdge(null, vertexes.get("Ropa"), vertexes.get("Sombrero"), "Relacion");
        eRopaSombrero.setProperty("value", "es-un");

        edges.put("PersonaCuerpo", ePersonaCuerpo);
        edges.put("PersonaRopa", ePersonaRopa);
        edges.put("RopaSombrero", eRopaSombrero);
        og.commit();
    }

}
