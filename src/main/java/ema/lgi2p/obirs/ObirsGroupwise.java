/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import java.util.List;
import java.util.Set;
import org.json.simple.parser.ParseException;
import org.openrdf.model.URI;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;


public class ObirsGroupwise extends Obirs {

    private SMconf similarityPairwiseConf;

    public ObirsGroupwise(SM_Engine engine, String indexFilePath, SMconf similarityGroupwiseConf) throws SLIB_Ex_Critic, ParseException {
        ontologyEngine = new OntologyEngine(engine, similarityGroupwiseConf);
        indexation = new IndexationFile(indexFilePath);
    }

    public ObirsGroupwise(SM_Engine engine, String indexFilePath, SMconf similarityGroupwiseConf, SMconf similarityPairwiseConf) throws SLIB_Ex_Critic, ParseException {
        ontologyEngine = new OntologyEngine(engine, similarityGroupwiseConf);
        this.similarityPairwiseConf = similarityPairwiseConf;
        indexation = new IndexationFile(indexFilePath);
    }
    
    public ObirsGroupwise(String meshFilePath, String indexFilePath, SMconf similarityGroupwiseConf) throws SLIB_Ex_Critic, ParseException {
        ontologyEngine = new OntologyEngine(meshFilePath, similarityGroupwiseConf);
        indexation = new IndexationFile(indexFilePath);
    }
    
    public ObirsGroupwise(String meshFilePath, String indexFilePath, SMconf similarityGroupwiseConf, SMconf similarityPairwiseConf) throws SLIB_Ex_Critic, ParseException {
        ontologyEngine = new OntologyEngine(meshFilePath, similarityGroupwiseConf);
        this.similarityPairwiseConf = similarityPairwiseConf;
        indexation = new IndexationFile(indexFilePath);
    }
    
    public ObirsGroupwise(String meshFilePath, String indexFilePath) throws SLIB_Ex_Critic, ParseException {
        ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004); // IC
        SMconf similarityMeasureConf = new SMconf("", SMConstants.FLAG_SIM_GROUPWISE_DAG_TO, icConf);
        ontologyEngine = new OntologyEngine(meshFilePath, similarityMeasureConf);
        indexation = new IndexationFile(indexFilePath);
    }
    
    public String query(String jsonQuery) throws ParseException, Exception {
        // Build query from JSON
        ObirsQuery query = parseObirsQuery(jsonQuery);
        List<ObirsResult> results = query(query);
        // convert the results into json
        return jsonifyObirsResults(results);
    }

    public List<ObirsResult> query(ObirsQuery query) throws SLIB_Ex_Critic {
        query.normalizeWeight();
        List<ObirsResult> results = new ArrayList<ObirsResult>();
        if (query.getConcepts().isEmpty()) {
            return results;
        }
        
        Set<URI> queryConceptURIs = new HashSet<URI>();
        
        for(URI queryConceptURI: query.getConceptIds()){
            if (ontologyEngine.hasVertex(queryConceptURI)){
                queryConceptURIs.add(queryConceptURI);
            }
        }
        
        for (Document document : indexation) {
            Set<URI> documentConceptURIs = new HashSet<URI>();
            for(URI docConceptURI: document.getConceptURIs()){
                if (ontologyEngine.hasVertex(docConceptURI)){
                    documentConceptURIs.add(docConceptURI);
                }
            }
            if(!documentConceptURIs.isEmpty()){
                double score;
                if(similarityPairwiseConf != null){
                    score = ontologyEngine.computeGroupwiseAddOnSimilarity(similarityPairwiseConf, queryConceptURIs, documentConceptURIs);
                }
                else {
                    score = ontologyEngine.computeGroupwiseStandaloneSimilarity(queryConceptURIs, documentConceptURIs);
                }
                if (score > query.getScoreThreshold()) {
                    ObirsResult result = new ObirsResult();
                    result.setDocId(document.getId());
                    result.setDocTitle(document.getTitle());
                    result.setHref(document.getHref());
                    result.setScore(score);
                    results.add(result);
                }
            }
        }
        Collections.sort(results);
        if (results.size() > query.getNumberOfResults()) {
            results = results.subList(0, query.getNumberOfResults());
        }
        return results;
    }
    

    
    
}
