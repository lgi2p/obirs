/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;

import slib.sglib.algo.graph.validator.dag.ValidatorDAG;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.sglib.model.repo.URIFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;


import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;



public class OntologyEngine {

    private G ontologyGraph;
    private SM_Engine engine;
    private URI uri;
    private SMconf similarityMeasureConf;
    private URIFactory factory = URIFactoryMemory.getSingleton();
    private String BASENAME;

    public OntologyEngine(SM_Engine engine, SMconf similarityMeasureConf) throws SLIB_Ex_Critic {
        this.engine = engine;
        this.ontologyGraph = engine.getGraph();
        this.uri = ontologyGraph.getURI();
        this.BASENAME = uri.stringValue();
        this.similarityMeasureConf = similarityMeasureConf;
    }

    public OntologyEngine(String meshFilePath, SMconf similarityMeasureConf) throws SLIB_Ex_Critic {
        this.BASENAME = "http://obirs";
        this.uri = factory.createURI(BASENAME);
        this.ontologyGraph = new GraphMemory(factory.createURI(BASENAME));
        GDataConf dataMeshXML = new GDataConf(GFormat.MESH_XML, meshFilePath); // the DTD must be located in the same directory
        dataMeshXML.addParameter("prefix", BASENAME + "/");
        try {
            GraphLoaderGeneric.populate(dataMeshXML, ontologyGraph);
        } catch (SLIB_Exception e) {
            e.printStackTrace();
        }
        this.removeMeshCycles(ontologyGraph);

        this.engine = new SM_Engine(ontologyGraph);
        this.similarityMeasureConf = similarityMeasureConf;
    }

    public OntologyEngine(String meshFilePath) throws SLIB_Ex_Critic {
        this(meshFilePath, getDefaultSimilarityMeasureConf());
    }
    
    public void setSimilarityMeasureConf(SMconf similarityMeasureConf){
        this.similarityMeasureConf = similarityMeasureConf;
    }
    
    public SMconf getSimilarityMeasureConf(){
        return similarityMeasureConf;
    }

    private static SMconf getDefaultSimilarityMeasureConf() throws SLIB_Ex_Critic {
        // similarity measure
        ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SANCHEZ_2011_a);
        return new SMconf("Lin_icSanchez", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998, icConf);
    }

    public double computePairwiseSimilarity(URI queryConceptURI, URI documentConceptURI) throws SLIB_Ex_Critic {
        return engine.computePairwiseSim(similarityMeasureConf, queryConceptURI, documentConceptURI);
    }

    public double computeGroupwiseStandaloneSimilarity(Set<URI> queryConceptURIs, Set<URI> documentConceptURIs) throws SLIB_Ex_Critic{
        return engine.computeGroupwiseStandaloneSim(similarityMeasureConf, queryConceptURIs, documentConceptURIs);
    }
    
    public double computeGroupwiseAddOnSimilarity(SMconf similarityPairwiseConf, Set<URI> queryConceptURIs, Set<URI> documentConceptURIs) throws SLIB_Ex_Critic{
        return engine.computeGroupwiseAddOnSim(similarityMeasureConf, similarityPairwiseConf, queryConceptURIs, documentConceptURIs);
    }
    
    public Set<URI> findNeighboringConcepts(URI startingConceptURI, double similarityThreshold) throws SLIB_Ex_Critic {

        Set<URI> results = new HashSet<URI>();
        results.add(startingConceptURI);
        Set<URI> processed = new HashSet<URI>();
        processed.add(startingConceptURI);
        Set<URI> toBeProcessed = new HashSet<URI>();
        toBeProcessed.add(startingConceptURI);

        URI currentConceptURI;
        double currentSimilarity;

        while (!toBeProcessed.isEmpty()) {
            currentConceptURI = toBeProcessed.iterator().next();
            toBeProcessed.remove(currentConceptURI);
            Set<URI> neighborURIs = ontologyGraph.getV(currentConceptURI, RDFS.SUBCLASSOF, Direction.BOTH);
            for (URI neighborURI : neighborURIs) {
                if (!processed.contains(neighborURI)) {
                    currentSimilarity = engine.computePairwiseSim(similarityMeasureConf, startingConceptURI, neighborURI);
                    if (currentSimilarity > similarityThreshold) {
                        toBeProcessed.add(neighborURI);
                        results.add(neighborURI);
                    }
                }
            }
            processed.add(currentConceptURI);
        }
        return results;
    }

    public boolean isConceptExist(String concept) {
        URI conceptURI = factory.createURI(uri.stringValue() + "/" + concept);
        if (ontologyGraph.containsVertex(conceptURI)) {
            return true;
        }
        return false;
    }

    public Set<URI> getAncestors(URI conceptURI) {
        return engine.getAncestorsInc(conceptURI);
    }

    public Set<URI> getDescendants(URI conceptURI) {
        return engine.getDescendantsInc(conceptURI);
    }

    public boolean hasVertex(URI vertexURI){
        return getEngine().getGraph().containsVertex(vertexURI);
    }
    
    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public SM_Engine getEngine() {
        return engine;
    }

    public void setEngine(SM_Engine engine) {
        this.engine = engine;
    }

    /*
     * We remove the cycles of the graph in order to obtain 
     * a rooted directed acyclic graph (DAG) and therefore be able to 
     * use most of semantic similarity measures.
     * see http://semantic-measures-library.org/sml/index.php?q=doc&page=mesh
     */
    private void removeMeshCycles(G meshGraph) throws SLIB_Ex_Critic {

        // We remove the edges creating cycles
        URI ethicsURI = factory.createURI(uri.stringValue() + "/D004989");
        URI moralsURI = factory.createURI(uri.stringValue() + "/D009014");

        // We retrieve the direct subsumers of the concept (D009014)
        Set<E> moralsEdges = meshGraph.getE(RDFS.SUBCLASSOF, moralsURI, Direction.OUT);
        for (E e : moralsEdges) {

            System.out.println("\t" + e);
            if (e.getTarget().equals(ethicsURI)) {
                System.out.println("\t*** Removing edge " + e);
                meshGraph.removeE(e);
            }
        }

        ValidatorDAG validatorDAG = new ValidatorDAG();
        boolean isDAG = validatorDAG.containsTaxonomicDag(meshGraph);

        System.out.println("MeSH Graph is a DAG: " + isDAG);

        // We remove the edges creating cycles
        // see http://semantic-measures-library.org/sml/index.php?q=doc&page=mesh

        URI hydroxybutyratesURI = factory.createURI(uri.stringValue() + "/D006885");
        URI hydroxybutyricAcidURI = factory.createURI(uri.stringValue() + "/D020155");

        // We retrieve the direct subsumers of the concept (D009014)
        Set<E> hydroxybutyricAcidEdges = meshGraph.getE(RDFS.SUBCLASSOF, hydroxybutyricAcidURI, Direction.OUT);
        for (E e : hydroxybutyricAcidEdges) {

            System.out.println("\t" + e);
            if (e.getTarget().equals(hydroxybutyratesURI)) {
                System.out.println("\t*** Removing edge " + e);
                meshGraph.removeE(e);
            }
        }
    }
}
