/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs.core.model;

import ema.lgi2p.obirs.Conf;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;

public class ObirsQuery {

    public enum AGGREGATOR_FLAG {

        MAX;
    }

    public enum MEASURE_FLAG {

        LIN;
    }

    private AGGREGATOR_FLAG aggregator;          // aggregator method used
    private MEASURE_FLAG similarityMeasure;   // similarity measurement used
    private Double aggregatorParameter; // -1, 0, 1, 2
    private Double scoreThreshold;      // we only want documents which have a score higher then scoreThreshold
    private Integer numberOfResults;    // how many results do we want ?
    private Map<URI, Double> concepts;

    private double weigthSum;

    public ObirsQuery(
            Map<URI, Double> concepts,
            String aggregator,
            String similarityMeasure,
            Double aggregatorParameter,
            Double scoreThreshold,
            Integer numberOfResults) {

        this.aggregator = aggregator == null ? AGGREGATOR_FLAG.MAX : AGGREGATOR_FLAG.valueOf(aggregator);
        this.similarityMeasure = similarityMeasure == null ? MEASURE_FLAG.LIN : MEASURE_FLAG.valueOf(similarityMeasure);
        this.aggregatorParameter = aggregatorParameter == null ? Conf.DEFAULT_QUERY_AGGREGATION_PARAM_VALUE : aggregatorParameter;
        this.scoreThreshold = scoreThreshold == null ? Conf.DEFAULT_QUERY_SCORE_THRESHOLD_VALUE : scoreThreshold;
        this.numberOfResults = numberOfResults == null ? Conf.DEFAULT_QUERY_NUMBER_OF_RESULT_VALUE : numberOfResults;
        this.concepts = concepts == null ? new HashMap<URI, Double>() : concepts;

        weigthSum = 0;
    }

    public ObirsQuery() {
        this(null, null, null, null, null, null);
    }

    public Map<URI, Double> getConcepts() {
        return Collections.unmodifiableMap(concepts);
    }

    public void addConcept(URI uriConcept, Double weight) {
        concepts.put(uriConcept, weight);
        weigthSum += weight;
    }

    public void removeConcept(URI uriConcept) {
        weigthSum -= concepts.get(uriConcept);
        concepts.remove(uriConcept);

    }

    public AGGREGATOR_FLAG getAggregator() {
        return aggregator;
    }

    public MEASURE_FLAG getSimilarityMeasure() {
        return similarityMeasure;
    }

    public Double getAggregatorParameter() {
        return aggregatorParameter;
    }

    public void setAggregatorParameter(Double aggregatorParameter) {
        this.aggregatorParameter = aggregatorParameter;
    }

    public Double getScoreThreshold() {
        return scoreThreshold;
    }

    public void setScoreThreshold(Double scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    public Integer getNumberOfResults() {
        return numberOfResults;
    }

    public void setNumberOfResults(Integer numberOfResults) {
        this.numberOfResults = numberOfResults;
    }

    /**
     * If the sum of weights associated to the concepts is not equals to 1 the
     * methods uses a simple cross-product to adjust the weights such as their
     * sum is equal to 1. .
     */
    public void normalizeWeight() {

        if (weigthSum == 1 || concepts.isEmpty()) {
            return;
        }
        double totalWeight = 0;
        for (URI uri : concepts.keySet()) {
            totalWeight += concepts.get(uri);
        }

        for (URI concept : concepts.keySet()) {
            double new_weight = concepts.get(concept) / totalWeight;
            concepts.put(concept, new_weight);
        }
        weigthSum = 1;
    }

    @Override
    public String toString() {

        String out = "Aggregator: " + aggregator
                + "\tAggregator parameter: " + aggregatorParameter + ""
                + "\tMeasure: " + similarityMeasure
                + "\tscore Threashold: " + scoreThreshold
                + "\tNumber of results:" + numberOfResults
                + "\tconcepts: " + concepts
                + "\tweight sum: " + weigthSum;
        return out;

    }
}
