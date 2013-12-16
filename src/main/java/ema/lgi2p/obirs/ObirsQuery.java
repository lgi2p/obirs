/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;


public class ObirsQuery {

    /* query options */
    private String aggregator;          // aggregator method used
    private String similarityMeasure;   // similarity measurement used
    private Double aggregatorParameter; // -1, 0, 1, 2
    private Double scoreThreshold;      // we only want documents which have a score higher then scoreThreshold
    private Integer numberOfResults;    // how many results do we want ?
    private String defaultNameSpace;
    private Map<URI,Double> concepts;

    public ObirsQuery(String defaultNameSpace,
            Map<URI,Double> concepts, 
            String aggregator, 
            String similarityMeasure, 
            Double aggregatorParameter,
            Double scoreThreshold,
            Integer numberOfResults) {
        this.defaultNameSpace = defaultNameSpace;
        
        this.aggregator = aggregator;
        if(this.aggregator == null){
            this.aggregator = "max";
        }
        
        // FLAG_ICI_SANCHEZ_2011_a,FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998::k=0.5;
        this.similarityMeasure = similarityMeasure; 
        if(this.similarityMeasure == null){
            this.similarityMeasure = "Lin";
        }
        
        this.aggregatorParameter = aggregatorParameter;
        if(this.aggregatorParameter == null){
            this.aggregatorParameter = 2.0;
        }
        
        this.scoreThreshold = scoreThreshold;
        if(this.scoreThreshold == null){
            this.scoreThreshold = 0.0;
        }
        
        this.numberOfResults = numberOfResults;
        if(this.numberOfResults == null){
            this.numberOfResults = 30;
        }
        
        this.concepts = concepts;
        if(this.concepts == null){
            this.concepts = new HashMap<URI, Double>();    
        }
    }

    public ObirsQuery() {
        
        this.aggregator = "max";
        this.similarityMeasure = "Lin";
        this.aggregatorParameter = 2.0;
        this.scoreThreshold = 0.0;
        this.numberOfResults = 30;
        concepts = new HashMap<URI, Double>();
    }
    
    
    public  Map<URI,Double> getConcepts() {
        return concepts;
    }


    public void addConcept(URI uriConcept, Double weight) {
        concepts.put(uriConcept,weight);
//        updateConceptsMap(); // TODO
    }

    public void removeConcept(URI uriConcept) {
        concepts.remove(uriConcept);
//        updateConceptsMap(); // TODO
    }


    public Set<URI> getConceptIds() {
        return new HashSet(Collections.unmodifiableCollection(concepts.keySet()));
    }

    public String getAggregator() {
        return aggregator;
    }

    public void setAggregator(String aggregator) {
        this.aggregator = aggregator;
    }

    public String getSimilarityMeasure() {
        return similarityMeasure;
    }

    public void setSimilarityMeasure(String similarityMeasure) {
        this.similarityMeasure = similarityMeasure;
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

    public void adjustWeight() {
        double nbConcepts = getConcepts().size();
        if (nbConcepts == 0) {
            return;
        }
        for (URI concept : getConcepts().keySet()) {
            concepts.put(concept,1.0 / nbConcepts);
        }
    }
    
    public void normalizeWeight() {
        double totalWeight = 0;
        for (URI concept : getConcepts().keySet()) {
            totalWeight += concepts.get(concept);
        }
        if (totalWeight != 1){
            adjustWeight();
        }
    }


    public String getDefaultNameSpace() {
        return defaultNameSpace;
    }
    
    public void setDefaultNameSpace(String defaultNameSpace) {
        this.defaultNameSpace = defaultNameSpace;
    }
    
    
}
