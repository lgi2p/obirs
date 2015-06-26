/*
 *  Copyright or © or Copr. Ecole des Mines d'Alès (2012-2014) 
 *  
 *  This software is a computer program whose purpose is to provide 
 *  several functionalities for the processing of semantic data 
 *  sources such as ontologies or text corpora.
 *  
 *  This software is governed by the CeCILL  license under French law and
 *  abiding by the rules of distribution of free software.  You can  use, 
 *  modify and/ or redistribute the software under the terms of the CeCILL
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info". 
 * 
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability. 

 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or 
 *  data to be ensured and,  more generally, to use and operate it in the 
 *  same conditions as regards security. 
 * 
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL license and that you accept its terms.
 */
package com.github.lgi2p.obirs.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.LoggerFactory;
import slib.graph.algo.extraction.utils.GAction;
import slib.graph.algo.extraction.utils.GActionType;
import slib.graph.algo.extraction.utils.GraphActionExecutor;
import slib.graph.algo.validator.dag.ValidatorDAG;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.GraphLoaderGeneric;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Utils {

    static org.slf4j.Logger logger = LoggerFactory.getLogger(Utils.class);

    public static SM_Engine buildSMEngine(String ontologyRDFXML) throws SLIB_Exception {

        String basename = "http://obirs";
        G onto = new GraphMemory(URIFactoryMemory.getSingleton().getURI(basename));
        GDataConf owlconf = new GDataConf(GFormat.RDF_XML, ontologyRDFXML);
        GraphLoaderGeneric.populate(owlconf, onto);

        // remove inner cycle if required
        logger.info("Removing inner cylces if required");
        Set<E> toRemove = new HashSet<E>();
        for (E e : onto.getE()) {
            if (e.getSource().equals(e.getTarget())) {
                toRemove.add(e);
            }
        }
        logger.info(toRemove.size() + " inner cycles removed");
        onto.removeE(toRemove);

        logger.info("Rerooting ontology if required");

        // reroot if required
        if (new ValidatorDAG().getTaxonomicRoots(onto).size() != 1) {
            GAction rooting = new GAction(GActionType.REROOTING);
            GraphActionExecutor.applyAction(rooting, onto);
        }

        return new SM_Engine(onto);
    }

    public static Set<URI> findNeighboringConcepts(SM_Engine engine, SMconf pairwiseMeasure, URI startingConceptURI, double similarityThreshold) throws SLIB_Ex_Critic {

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
            Set<URI> neighborURIs = engine.getGraph().getV(currentConceptURI, RDFS.SUBCLASSOF, Direction.BOTH);
            for (URI neighborURI : neighborURIs) {
                if (!processed.contains(neighborURI)) {
                    currentSimilarity = engine.compare(pairwiseMeasure, startingConceptURI, neighborURI);
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

    /**
     * Return a String representation of the given file
     *
     * @param f the file to process
     * @return the string representation
     * @throws IOException
     */
    public static String readFileAsString(File f) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(f.getPath()));
        return new String(encoded, Charset.defaultCharset());
    }

    public static Map<URI, String> loadConceptLabels(String conceptIndexFile) throws IOException {
        FileReader fileReader = new FileReader(conceptIndexFile);
        BufferedReader br = new BufferedReader(fileReader);
        
        Map<URI,String> mapURI2Labels = new HashMap();
        URIFactory f = URIFactoryMemory.getSingleton();

        String line = null;
        while ((line = br.readLine()) != null) {
            String[] data = line.split("\t");
            if(data.length == 2){
                mapURI2Labels.put(f.getURI(data[0]), data[1]);
            }
        }
        return mapURI2Labels;
    }

}
