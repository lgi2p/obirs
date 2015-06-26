package com.github.lgi2p.obirs.core.engine;

import com.github.lgi2p.obirs.Conf;
import com.github.lgi2p.obirs.core.model.ObirsQuery;
import com.github.lgi2p.obirs.core.model.ObirsResult;
import com.github.lgi2p.obirs.core.model.RefinedObirsQuery;
import com.github.lgi2p.obirs.core.engine.utils.ConceptMatch;
import com.github.lgi2p.obirs.core.index.ItemCollection;
import com.github.lgi2p.obirs.core.engine.utils.ConceptSim;
import com.github.lgi2p.obirs.core.model.Item;
import com.github.lgi2p.obirs.utils.JSONConverter;
import com.github.lgi2p.obirs.utils.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.parser.ParseException;
import org.openrdf.model.URI;
import org.slf4j.LoggerFactory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * Implementation of the OBIRS interface based on the following publication.
 * User Centered and Ontology Based Information Retrieval System for Life
 * Sciences Mohameth-François Sy, Sylvie Ranwez, Jacky Montmain, Armelle
 * Regnault, Michel Crampes, Vincent Ranwez. In BMC Bioinformatics, 13(Suppl
 * 1):S4, 2012
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class ObirsMohameth2010 implements Obirs {

    SM_Engine engine;
    ItemCollection index;

    private SMconf similarityConf;
    static org.slf4j.Logger logger = LoggerFactory.getLogger(ObirsMohameth2010.class);

    public ObirsMohameth2010(SM_Engine engine, ItemCollection index, SMconf similarityConf) throws SLIB_Exception, ParseException {
        this.engine = engine;
        this.index = index;
        this.similarityConf = similarityConf;
    }

    public ObirsMohameth2010(SM_Engine engine, ItemCollection index) throws SLIB_Exception, ParseException {
        this(engine, index, Conf.getDefaultPairwiseSimilarityMeasure());
    }

    public List<ObirsResult> query(ObirsQuery query) throws SLIB_Ex_Critic {

        logger.info("Query: " + this.getClass().getName());
        logger.info(query.toString());

        query.normalizeWeight();

        List<ObirsResult> results = new ArrayList<ObirsResult>();
        if (query.getConcepts().isEmpty()) {
            return results;
        }

        for (Item item : index) {

            List<ConceptMatch> conceptMatches = new ArrayList<ConceptMatch>();
            for (Map.Entry<URI, Double> queryConcept : query.getConcepts().entrySet()) {

                URI queryConceptUri = queryConcept.getKey();
                Double queryConceptWeight = queryConcept.getValue();
                ConceptSim bestConcept = searchBestConceptSim(queryConceptUri, item.getAnnotations());

                double score = 0.0;

                if (bestConcept.getSimilarity() >= queryConceptWeight) {
                    score = bestConcept.getSimilarity();
                }

                ConceptMatch match = new ConceptMatch(queryConceptUri, bestConcept.getURI(), score);

                conceptMatches.add(match);
                // reference concept ids so we can fetch their title later
//                documentConceptURIs.add(queryConceptUri);
//                documentConceptURIs.add(bestConcept.getURI());
            }
            double score = aggregateElementaryScore(conceptMatches, query);
            if (score > query.getScoreThreshold()) {
                ObirsResult result = new ObirsResult(item.getURI(), score, conceptMatches);
                results.add(result);
            }
        }
        Collections.sort(results);
        if (results.size() > query.getNumberOfResults()) {
            results = results.subList(0, query.getNumberOfResults());
        }

        detectMatchSemantics(results);
        return results;
    }

    /**
     * Perform a fast query considering default threshold.
     *
     * @param query the query to perform
     * @return
     * @throws ParseException
     * @throws Exception
     */
    public List<ObirsResult> fastQuery(ObirsQuery query) throws ParseException, Exception {
        return fastQuery(query, Conf.DEFAULT_QUERY_FAST_SIMILARITY_THREASHOLD);
    }

    public List<ObirsResult> fastQuery(ObirsQuery query, double similarityThreshold) throws SLIB_Ex_Critic { // works only with LIN similarities
        
        query.normalizeWeight();
        
        List<ObirsResult> results = new ArrayList<ObirsResult>();
        if (query.getConcepts().isEmpty()) {
            return results;
        }

        Set<URI> neighbors = new HashSet<URI>();

        for (Map.Entry<URI, Double> queryConcept : query.getConcepts().entrySet()) {

            URI queryConceptUri = queryConcept.getKey();
            neighbors.addAll(Utils.findNeighboringConcepts(engine, similarityConf, queryConceptUri, similarityThreshold));
        }

        Set<Item> documentsToProcessed = new HashSet<Item>();
        for (URI conceptURI : neighbors) {
            for (Item item : index.getItemsAnnotatedByConcept(conceptURI)) {
                documentsToProcessed.add(item);
            }
        }

        for (Item item : documentsToProcessed) {

            List<ConceptMatch> conceptMatches = new ArrayList<ConceptMatch>();
            for (Map.Entry<URI, Double> queryConcept : query.getConcepts().entrySet()) {

                URI queryConceptURI = queryConcept.getKey();
                Double queryConceptWeight = queryConcept.getValue();

                ConceptSim bestConceptSim = searchBestConceptSim(queryConceptURI, item.getAnnotations());
                double score = 0.0;

                if (bestConceptSim.getSimilarity() >= queryConceptWeight) {
                    score = bestConceptSim.getSimilarity();
                }

                ConceptMatch match = new ConceptMatch(queryConceptURI, bestConceptSim.getURI(), score);

                conceptMatches.add(match);
            }
            double score = aggregateElementaryScore(conceptMatches, query);
            if (score > query.getScoreThreshold()) {
                ObirsResult result = new ObirsResult(item.getURI(), score, conceptMatches);
                results.add(result);
            }
        }
        Collections.sort(results);
        if (results.size() > query.getNumberOfResults()) {
            results = results.subList(0, query.getNumberOfResults());
        }

        detectMatchSemantics(results);
        return results;
    }

    public String refineQuery(String refinedJsonQuery) throws ParseException, IOException, Exception {
        RefinedObirsQuery refinedQuery = JSONConverter.parseRefinedObirsQuery(engine, refinedJsonQuery);
        ObirsQuery query = refineQuery(refinedQuery);
        return JSONConverter.jsonifyObirsQuery(query);
    }

    /**
     * Considering a given query URI and a set of URIs this methods returns the
     * best similarity and associated concept that has been obtained comparing
     * the given URI and one of the concepts contained into the given set. If
     * multiple concepts from the set have the best similarity with the target
     * concept only the first one that has been found is return. If the given
     * query concept is not found by the underlying ontology engine or is the
     * set of concepts is empty/does not contain URIs defined into the ontology
     * this methods returns null;
     *
     * @param queryConcept
     * @param concepts
     * @return a ConceptSim object containing the result explained above.
     * @throws slib.utils.ex.SLIB_Ex_Critic
     */
    protected ConceptSim searchBestConceptSim(URI queryConcept, Set<URI> concepts) throws SLIB_Ex_Critic {

        ConceptSim bestConceptSim = null;
        URI matchingConcept = null;
        double max_similarity = -1;
        double similarity;

        if (engine.getGraph().containsVertex(queryConcept)) {

            for (URI concept : concepts) {

                if (!engine.getGraph().containsVertex(concept)) {
                    continue;
                }

                try {
                    similarity = engine.compare(similarityConf, queryConcept, concept);
                } catch (SLIB_Ex_Critic e) {
                    e.printStackTrace();
                    throw new SLIB_Ex_Critic("error while calculate similarity between: " + queryConcept + " and " + concept + "\ndetails" + e.getMessage());
                }
                if (similarity > max_similarity || matchingConcept == null) {
                    max_similarity = similarity;
                    matchingConcept = concept;
                }
            }
            bestConceptSim = new ConceptSim(matchingConcept, max_similarity);
        }
        return bestConceptSim;
    }

    public double aggregateElementaryScore(List<ConceptMatch> resultSet, ObirsQuery query) {
        
        Double docScore = null;
        if (query.getAggregatorParameter() != 0) {
            docScore = 0.0;
            for (ConceptMatch res : resultSet) {
                double weight = query.getConcepts().get(res.getQueryConceptURI());
                docScore += Math.pow(res.getScore(),
                        query.getAggregatorParameter()) * weight;
            }
            docScore = Math.pow(docScore, 1 / query.getAggregatorParameter());
        } else {
            double _score;
            for (ConceptMatch res : resultSet) {
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


    private double getRSV(Item item, ObirsQuery query) throws SLIB_Ex_Critic {

        ArrayList<ConceptMatch> conceptMatches = new ArrayList<ConceptMatch>();

        for (Map.Entry<URI, Double> queryConcept : query.getConcepts().entrySet()) {

            URI queryConceptURI = queryConcept.getKey();
            double queryConceptWeight = queryConcept.getValue();

            ConceptSim bestConceptSim = searchBestConceptSim(queryConceptURI, item.getAnnotations());

            double score = 0.0;
            if (bestConceptSim.getSimilarity() >= queryConceptWeight) { // ? wtf
                score = bestConceptSim.getSimilarity();
            }

            ConceptMatch match = new ConceptMatch(queryConceptURI, bestConceptSim.getURI(), score);

            conceptMatches.add(match);
        }
        return aggregateElementaryScore(conceptMatches, query);
    }

    private double buildInd(ObirsQuery query, List<Item> selectedItems, List<Item> otherItems) throws SLIB_Ex_Critic {
        double selectedScore = 0;
        for (Item document : selectedItems) {
            selectedScore += getRSV(document, query);
        }

        double otherScore = 0;
        for (Item document : otherItems) {
            otherScore += getRSV(document, query);
        }

        if (otherItems.size() > 0) {
            return selectedScore / selectedItems.size() - otherScore / otherItems.size();
        } else {
            return selectedScore / selectedItems.size();
        }
    }

    public ObirsQuery refineQuery(RefinedObirsQuery refinedQuery) throws SLIB_Ex_Critic {
        double epsilone = 0.85;
        ObirsQuery query = refinedQuery.getQuery();

        if (refinedQuery.getSelectedItemURIs().isEmpty()) {
            return query;
        }

        Iterable<Item> selectedItemIterator = index.findItemsByURIs(refinedQuery.getSelectedItemURIs());
        ArrayList<Item> selectedItems = new ArrayList<Item>();
        for (Item document : selectedItemIterator) {
            selectedItems.add(document);
        }

        ArrayList<Item> rejectedItems = new ArrayList<Item>();
        if (refinedQuery.getRejectedItemURIs().size() > 0) {
            Iterable<Item> rejectedItemIterator = index.findItemsByURIs(refinedQuery.getRejectedItemURIs());
            for (Item document : rejectedItemIterator) {
                rejectedItems.add(document);
            }
        }

        Set<URI> selectedItemsURIs = new HashSet<URI>();
        for (Item item : selectedItems) {
            for (URI conceptId : item.getAnnotations()) {
                selectedItemsURIs.add(conceptId);
            }
        }

        Set<URI> conceptURIs = new HashSet<URI>();
        double similarity = 0.0;
        for (URI conceptURI : selectedItemsURIs) {

            if (!engine.getGraph().containsVertex(conceptURI)) {
                continue;
            }

            Set<URI> conceptAncestors = engine.getAncestorsInc(conceptURI);

            for (URI ancestorId : conceptAncestors) {
                similarity = engine.compare(similarityConf, conceptURI, ancestorId);

                if (similarity > epsilone) {
                    conceptURIs.add(ancestorId);
                }
            }
        }

        // copy the query
        ObirsQuery newQuery = new ObirsQuery(
                new HashMap(query.getConcepts()),
                query.getAggregator().toString(),
                query.getSimilarityMeasure().toString(),
                query.getAggregatorParameter(),
                query.getScoreThreshold(),
                query.getNumberOfResults());

        double ind = buildInd(query, selectedItems, rejectedItems);
        double bestInd = ind;
        boolean improve = true;
        URI bestConcept = null;

        while (improve) {
            improve = false;
            for (URI conceptURI : conceptURIs) {

                newQuery.addConcept(conceptURI, 1.0);
                newQuery.normalizeWeight();
                double newInd = buildInd(newQuery, selectedItems, rejectedItems);
                if (newInd > bestInd) {
                    bestConcept = conceptURI;
                    bestInd = newInd;
                }
                newQuery.removeConcept(conceptURI);
            }
            if (bestInd > ind && bestInd - ind > 0.01) {
                if (!newQuery.getConcepts().containsKey(bestConcept)) {
                    newQuery.addConcept(bestConcept, 1.0);
                }
                conceptURIs.remove(bestConcept);
                ind = bestInd;
                improve = true;
            }
        }
        newQuery.normalizeWeight();
        return newQuery;
    }

    private void detectMatchSemantics(List<ObirsResult> results) {

        // fill relation types and concept titles
        String relationType;
        for (ObirsResult r : results) {
            for (ConceptMatch cm : r.getConcepts()) {
                if (cm.getQueryConceptURI().equals(cm.getMatchingConceptURI())) {
                    relationType = "EXACT";
                } else {
                    Set<URI> conceptAncestors = engine.getAncestorsInc(cm.getQueryConceptURI());
                    if (conceptAncestors.contains(cm.getMatchingConceptURI())) {
                        relationType = "DESC";
                    } else {
                        Set<URI> conceptDescendants = engine.getDescendantsInc(cm.getQueryConceptURI());
                        if (conceptDescendants.contains(cm.getMatchingConceptURI())) {
                            relationType = "ASC";
                        } else {
                            relationType = "OTHER";
                        }
                    }
                }
                cm.setRelationType(URIFactoryMemory.getSingleton().getURI(Conf.DEFAULT_URI + "/match_type/" + relationType));
            }
        }

    }
}
