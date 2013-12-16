/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;


public class ObirsMohameth2010 extends Obirs {

    public ObirsMohameth2010(SM_Engine engine, String indexFilePath, SMconf similarityConfiguration) throws SLIB_Ex_Critic, ParseException {
        ontologyEngine = new OntologyEngine(engine, similarityConfiguration);
        indexation = new IndexationFile(indexFilePath);
    }

    // MeSH constructor
    public ObirsMohameth2010(String meshFilePath, String indexFilePath, SMconf similarityConfiguration) throws SLIB_Ex_Critic, ParseException {
        ontologyEngine = new OntologyEngine(meshFilePath, similarityConfiguration);
        indexation = new IndexationFile(indexFilePath);
    }
    
    // MeSH constructor
    public ObirsMohameth2010(String meshFilePath, String indexFilePath) throws SLIB_Ex_Critic, ParseException {
        ontologyEngine = new OntologyEngine(meshFilePath);
        indexation = new IndexationFile(indexFilePath);
    }
    

    
    public String query(String jsonQuery) throws ParseException, Exception {
        ObirsQuery query = parseObirsQuery(jsonQuery);
        List<ObirsResult> results = query(query);
        return jsonifyObirsResults(results);
    }

    public String fastQuery(String jsonQuery) throws ParseException, Exception{
        return fastQuery(jsonQuery, 0.1);
    }

    public String fastQuery(String jsonQuery, double similarityThreshold) throws ParseException, Exception {
        ObirsQuery query = parseObirsQuery(jsonQuery);
        List<ObirsResult> results = fastQuery(query, similarityThreshold);
        return jsonifyObirsResults(results);
    }

    public List<ObirsResult> query(ObirsQuery query) {
        query.normalizeWeight();
        List<ObirsResult> results = new ArrayList<ObirsResult>();
        if (query.getConcepts().isEmpty()) {
            return results;
        }

//        Set<URI> documentConceptIds = new HashSet<URI>();

        for (Document document : indexation) {
            ArrayList<ResultSet> resultSet = new ArrayList<ResultSet>();
            for (Map.Entry<URI, Double> queryConcept : query.getConcepts().entrySet()) {
                URI queryConceptUri = queryConcept.getKey();
                Double queryConceptWeight = queryConcept.getValue();
                BestConcept bestConcept = searchBestConcept(queryConceptUri, document.getConceptURIs());
                ResultSet resultConcept = new ResultSet();
                if (bestConcept.getSimilarity() >= queryConceptWeight) {
                    resultConcept.setScore(bestConcept.getSimilarity());
                } else {
                    resultConcept.setScore(0.0);
                }
                resultConcept.setQueryConceptURI(queryConceptUri);
                resultConcept.setMatchingConceptURI(bestConcept.getURI());
                resultSet.add(resultConcept);
                // reference concept ids so we can fetch their title later
//                documentConceptIds.add(queryConceptUri);
//                documentConceptIds.add(bestConcept.getURI());
            }
            double score = aggregateElementaryScore(resultSet, query);
            if (score > query.getScoreThreshold()) {
                ObirsResult result = new ObirsResult();
                result.setDocId(document.getId());
                result.setDocTitle(document.getTitle());
                result.setHref(document.getHref());
                result.setScore(score);
                result.setConcepts(resultSet);
                results.add(result);
            }
        }
        Collections.sort(results);
        if (results.size() > query.getNumberOfResults()) {
            results = results.subList(0, query.getNumberOfResults());
        }

        // fill relation types and concept titles
        String relationType;
        for (ObirsResult docResult : results) {
            for (ResultSet resultSet : docResult.getConcepts()) {
                if (resultSet.getQueryConceptURI().equals(resultSet.getMatchingConceptURI())) {
                    relationType = "EXACT";
                } else {
                    Set<URI> conceptAncestors = ontologyEngine.getAncestors(resultSet.getQueryConceptURI());
                    if (conceptAncestors.contains(resultSet.getMatchingConceptURI())) {
                        relationType = "DESC";
                    } else {
                        Set<URI> conceptDescendants = ontologyEngine.getDescendants(resultSet.getQueryConceptURI());
                        if (conceptDescendants.contains(resultSet.getMatchingConceptURI())) {
                            relationType = "ASC";
                        } else {
                            relationType = "OTHER";
                        }
                    }
                }
                resultSet.setRelationType(relationType);
            }
        }
        return results;
    }
    
    public List<ObirsResult> fastQuery(ObirsQuery query, double similarityThreshold) { // works only with LIN similarities
        query.normalizeWeight();
        List<ObirsResult> results = new ArrayList<ObirsResult>();
        if (query.getConcepts().isEmpty()) {
            return results;
        }

//        Set<URI> documentConceptURIs = new HashSet<URI>();

        Set<URI> neighbors = new HashSet<URI>();
        for (Map.Entry<URI, Double> queryConcept : query.getConcepts().entrySet()) {

            URI queryConceptUri = queryConcept.getKey();

            try {
                neighbors.addAll(ontologyEngine.findNeighboringConcepts(queryConceptUri, similarityThreshold)); // TODO parametrable
            } catch (SLIB_Ex_Critic ex) {
                Logger.getLogger(ObirsMohameth2010.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Set<Document> documentsToProcessed = new HashSet<Document>();
        for (URI conceptURI : neighbors) {
            documentsToProcessed.addAll(indexation.getDocumentByConceptURI(conceptURI));
        }

        for (Document document : documentsToProcessed) {
            ArrayList<ResultSet> resultSet = new ArrayList<ResultSet>();
            for (Map.Entry<URI, Double> queryConcept : query.getConcepts().entrySet()) {

                URI queryConceptURI = queryConcept.getKey();
                Double queryConceptWeight = queryConcept.getValue();


                BestConcept bestConcept = searchBestConcept(queryConceptURI, document.getConceptURIs());
                ResultSet resultConcept = new ResultSet();
                if (bestConcept.getSimilarity() >= queryConceptWeight) {
                    resultConcept.setScore(bestConcept.getSimilarity());
                } else {
                    resultConcept.setScore(0.0);
                }
                resultConcept.setQueryConceptURI(queryConceptURI);
                resultConcept.setMatchingConceptURI(bestConcept.getURI());
                resultSet.add(resultConcept);
                // reference concept ids so we can fetch their title later
//                documentConceptURIs.add(queryConceptURI);
//                documentConceptURIs.add(bestConcept.getURI());
            }
            double score = aggregateElementaryScore(resultSet, query);
            if (score > query.getScoreThreshold()) {
                ObirsResult result = new ObirsResult();
                result.setDocId(document.getId());
                result.setDocTitle(document.getTitle());
                result.setHref(document.getHref());
                result.setScore(score);
                result.setConcepts(resultSet);
                results.add(result);
            }
        }
        Collections.sort(results);
        if (results.size() > query.getNumberOfResults()) {
            results = results.subList(0, query.getNumberOfResults());
        }

        // fill relation types and concept titles
        String relationType;
        for (ObirsResult docResult : results) {
            for (ResultSet resultSet : docResult.getConcepts()) {
                if (resultSet.getQueryConceptURI().equals(resultSet.getMatchingConceptURI())) {
                    relationType = "EXACT";
                } else {
                    Set<URI> conceptAncestors = ontologyEngine.getAncestors(resultSet.getQueryConceptURI());
                    if (conceptAncestors.contains(resultSet.getMatchingConceptURI())) {
                        relationType = "DESC";
                    } else {
                        Set<URI> conceptDescendants = ontologyEngine.getDescendants(resultSet.getQueryConceptURI());
                        if (conceptDescendants.contains(resultSet.getMatchingConceptURI())) {
                            relationType = "ASC";
                        } else {
                            relationType = "OTHER";
                        }
                    }
                }
                resultSet.setRelationType(relationType);
            }
        }
        return results;
    }
    
    public String refineQuery(String refinedJsonQuery) throws ParseException, IOException, Exception {
        RefinedObirsQuery refinedQuery = parseRefinedObirsQuery(refinedJsonQuery);
        ObirsQuery query = refineQuery(refinedQuery);
        // convert the results into json
        return jsonifyObirsQuery(query);
    }
    
    //    {
    //        "query": {
    //            "concepts": [
    //                {"id": "D015373", "weight": 0.5},
    //                {"id":"D006801", "weight": 0.5}
    //            ]
    //        },
    //        "selectedDocIds": ["42172", "42697", "42719"],
    //        "rejectedDocIds": ["42759"]
    //    }
    protected RefinedObirsQuery parseRefinedObirsQuery(String jsonQuery) throws ParseException, IOException, Exception{
        jsonQuery = jsonQuery.replace("\n", "");
        jsonQuery = jsonQuery.replace("\r", "");
        
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(jsonQuery);
        JSONObject jsonObject = (JSONObject) obj;
        
        JSONObject _query = (JSONObject) jsonObject.get("query");
        StringWriter jsQuery = new StringWriter();
        _query.writeJSONString(jsQuery);
        ObirsQuery obirsQuery = parseObirsQuery(jsQuery.toString());
        
        List<String> selectedDocIds = (List<String>) jsonObject.get("selectedDocIds");
        List<String> rejectedDocIds = (List<String>) jsonObject.get("rejectedDocIds");
        return new RefinedObirsQuery(obirsQuery, selectedDocIds, rejectedDocIds);
    }
    
    protected BestConcept searchBestConcept(URI queryConceptUri, List<URI> conceptIds) {
        URI matchingConcept = null;
        double max_similarity = 0.0;
        double similarity;
        
        if (ontologyEngine.hasVertex(queryConceptUri)) {
            for (URI docConceptURI : conceptIds) {

                if (!ontologyEngine.hasVertex(docConceptURI)){
                    continue;
                }

                try {
                    similarity = ontologyEngine.computePairwiseSimilarity(queryConceptUri, docConceptURI);
                } 
                catch (SLIB_Ex_Critic e) {
                    // TODO Auto-generated catch block
                    System.out.println("error while calculate similarity");
                    e.printStackTrace();
                    continue;
                }
                if (similarity > max_similarity || matchingConcept == null) {
                    max_similarity = similarity;
                    matchingConcept = docConceptURI;
                }
            }
        }
        BestConcept bestConcept = new BestConcept();
        bestConcept.setURI(matchingConcept);
        bestConcept.setSimilarity(max_similarity);
        return bestConcept;
    }

    public double aggregateElementaryScore(List<ResultSet> resultSet, ObirsQuery query) {
        Double docScore = null;
        if (query.getAggregatorParameter() != 0) {
            docScore = 0.0;
            for (ResultSet res : resultSet) {
                double weight = query.getConcepts().get(res.getQueryConceptURI());
                docScore += Math.pow(res.getScore(),
                        query.getAggregatorParameter()) * weight;
            }
            docScore = Math.pow(docScore, 1 / query.getAggregatorParameter());
        } else {
            double _score;
            for (ResultSet res : resultSet) {
                double weight = query.getConcepts().get(res.getQueryConceptURI());
                _score = Math.pow(res.getScore(), weight);
                if (docScore == null) {
                    docScore = _score;
                } else {
                    docScore = docScore * _score;
                }
            }
            docScore = Math.pow(docScore, resultSet.size());
        }
        return docScore;
    }

    private double getRSV(Document document, ObirsQuery query) {
        ArrayList<ResultSet> resultSet = new ArrayList<ResultSet>();

        for (Map.Entry<URI, Double> queryConcept : query.getConcepts().entrySet()) {

            URI queryConceptURI = queryConcept.getKey();
            Double queryConceptWeight = queryConcept.getValue();

            BestConcept bestConcept = searchBestConcept(queryConceptURI, document.getConceptURIs());
            ResultSet resultConcept = new ResultSet();
            if (bestConcept.getSimilarity() >= queryConceptWeight) {
                resultConcept.setScore(bestConcept.getSimilarity());
            } else {
                resultConcept.setScore(0.0);
            }
            resultConcept.setQueryConceptURI(queryConceptURI);
            resultConcept.setMatchingConceptURI(bestConcept.getURI());
            resultSet.add(resultConcept);
        }
        return aggregateElementaryScore(resultSet, query);
    }

    private double buildInd(ObirsQuery query, List<Document> selectedDocuments, List<Document> otherDocuments) {
        double selectedScore = 0;
        for (Document document : selectedDocuments) {
            selectedScore += getRSV(document, query);
        }

        double otherScore = 0;
        for (Document document : otherDocuments) {
            otherScore += getRSV(document, query);
        }

        if (otherDocuments.size() > 0) {
            return selectedScore / selectedDocuments.size() - otherScore / otherDocuments.size();
        } else {
            return selectedScore / selectedDocuments.size();
        }
    }

    
    
    public ObirsQuery refineQuery(RefinedObirsQuery refinedQuery) {
        double epsilone = 0.85;
        ObirsQuery query = refinedQuery.getQuery();

        if (refinedQuery.getSelectedDocIds().isEmpty()) {
            return query;
        }

        Iterable<Document> selectedDocumentsIterator = indexation.findByIds(refinedQuery.getSelectedDocIds());
        ArrayList<Document> selectedDocuments = new ArrayList<Document>();
        for (Document document : selectedDocumentsIterator) {
            selectedDocuments.add(document);
        }

        ArrayList<Document> rejectedDocuments = new ArrayList<Document>();
        if (refinedQuery.getRejectedDocIds().size() > 0) {
            Iterable<Document> rejectedDocumentsIterator = indexation.findByIds(refinedQuery.getRejectedDocIds());
            for (Document document : rejectedDocumentsIterator) {
                rejectedDocuments.add(document);
            }
        }

        Set<URI> documentConceptURIs = new HashSet<URI>();
        for (Document document : selectedDocuments) {
            for (URI conceptId : document.getConceptURIs()) {
                documentConceptURIs.add(conceptId);
            }
        }

        Set<URI> conceptURIs = new HashSet<URI>();
        double similarity = 0.0;
        for (URI conceptURI : documentConceptURIs) {
            
            if(!ontologyEngine.hasVertex(conceptURI)){
                continue;
            }
            
            Set<URI> conceptAncestors = ontologyEngine.getAncestors(conceptURI);
            
            for (URI ancestorId : conceptAncestors) {
                try {
                    similarity = ontologyEngine.computePairwiseSimilarity(conceptURI, ancestorId);
                } catch (SLIB_Ex_Critic e) {
                    // TODO Auto-generated catch block
                    System.out.println("error while calculate similarity");
                    e.printStackTrace();
                }
                if (similarity > epsilone) {
                    conceptURIs.add(ancestorId);
                }
            }
        }

        // copy the query
        ObirsQuery newQuery = new ObirsQuery(
                null,
                new HashMap(query.getConcepts()),
                query.getAggregator(),
                query.getSimilarityMeasure(),
                query.getAggregatorParameter(),
                query.getScoreThreshold(),
                query.getNumberOfResults());

        double ind = buildInd(query, selectedDocuments, rejectedDocuments);
        double bestInd = ind;
        boolean improve = true;
        URI bestConcept = null;

        while (improve) {
            improve = false;
            for (URI conceptURI : conceptURIs) {
                
                newQuery.addConcept(conceptURI,1.0);
                newQuery.adjustWeight();
                double newInd = buildInd(newQuery, selectedDocuments, rejectedDocuments);
                if (newInd > bestInd) {
                    bestConcept = conceptURI;
                    bestInd = newInd;
                }
                newQuery.removeConcept(conceptURI);
            }
            if (bestInd > ind && bestInd - ind > 0.01) {
                if (!newQuery.getConcepts().containsKey(bestConcept)) {
                    newQuery.addConcept(bestConcept,null);
                }
                conceptURIs.remove(bestConcept);
                ind = bestInd;
                improve = true;
            }
        }
        newQuery.adjustWeight();
        return newQuery;
    }
}
