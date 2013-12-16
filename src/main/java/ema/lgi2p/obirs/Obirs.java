/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import slib.utils.ex.SLIB_Ex_Critic;



public class Obirs {
    
    protected OntologyEngine ontologyEngine;
    protected Indexation indexation;
    

//     [{
//         "docId":"21924535","docTitle":"...","score":0.6,"href":"http://www.ncbi.nlm.nih.gov/pubmed/21924535",
//         "concepts":[{
//            "queryConceptURI":{"namespace":"http://obirs/","localName":"D006701"},
//            "matchingConceptURI":{"namespace":"http://obirs/","localName":"D058005"},
//            "relationType":"OTHER",
//            "score":0.5134594895514066
//          }]
//     }]
    protected String jsonifyObirsResults(List<ObirsResult> results) throws IOException{
        JSONArray jsonResults = new JSONArray();
        StringWriter json = new StringWriter();       
        for(ObirsResult result: results){
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("docId", result.getDocId());
            jsonResult.put("docTitle", result.getDocTitle());
            jsonResult.put("score", result.getScore());
            jsonResult.put("href", result.getHref());
            JSONArray jsonConcepts = new JSONArray();
            if (result.getConcepts() != null){
                for(ResultSet concept: result.getConcepts()){
                    JSONObject jsonConcept = new JSONObject();
                    jsonConcept.put("queryConceptURI", concept.getQueryConceptURI().toString());
                    jsonConcept.put("matchingConceptURI", concept.getMatchingConceptURI().toString());
                    jsonConcept.put("relationType", concept.getRelationType());
                    jsonConcept.put("score", concept.getScore());
                    jsonConcepts.add(jsonConcept);
                }
            }
            jsonResult.put("concepts", jsonConcepts);
            jsonResults.add(jsonResult);
        }
        jsonResults.writeJSONString(json);
        return json.toString();
    }
    

    
    
    protected String jsonifyObirsQuery(ObirsQuery query) throws IOException{
        JSONObject jsonObject = new JSONObject();
        
        jsonObject.put("defaultNameSpace", query.getDefaultNameSpace());
        jsonObject.put("aggregator", query.getAggregator());
        jsonObject.put("aggregatorParameter", query.getAggregatorParameter());
        jsonObject.put("similarityMeasure", query.getSimilarityMeasure());
        jsonObject.put("scoreThreshold", query.getScoreThreshold());
        jsonObject.put("numberOfResults", query.getNumberOfResults());
        
        StringWriter jsonQuery = new StringWriter();       
        
        
        JSONArray jsonConcepts = new JSONArray();
        for (Map.Entry<URI, Double> queryConcept : query.getConcepts().entrySet()) {
            JSONObject jsonConcept = new JSONObject();
            URI conceptURI = queryConcept.getKey();
            jsonConcept.put("id", conceptURI.getNamespace()+conceptURI.getLocalName());
            jsonConcept.put("weight", queryConcept.getValue());
            jsonConcepts.add(jsonConcept);
        }
        jsonObject.put("concepts", jsonConcepts);
        
        jsonObject.writeJSONString(jsonQuery);
        return jsonQuery.toString();
    }
    
    protected ObirsQuery parseObirsQuery(String jsonQuery) throws ParseException, Exception {
        
        jsonQuery = jsonQuery.replace("\n", "");
        jsonQuery = jsonQuery.replace("\r", "");
        JSONParser parser = new JSONParser();
        
        Object obj = parser.parse(jsonQuery);
        JSONObject jsonObject = (JSONObject) obj;
        String defaultNameSpace = (String) jsonObject.get("defaultNameSpace");
        String aggregator = (String) jsonObject.get("aggregator");
        String similarityMeasure = (String) jsonObject.get("similarityMeasure");
        Double aggregatorParameter;
        if(jsonObject.get("aggregatorParameter") != null){
            aggregatorParameter = Double.parseDouble(jsonObject.get("aggregatorParameter").toString());
        }
        else {
            aggregatorParameter = 2.0;
        }
        Double scoreThreshold = (Double) jsonObject.get("scoreThreshold");
        Integer numberOfResults = (Integer) jsonObject.get("numberOfResults");

        JSONArray concepts = (JSONArray) jsonObject.get("concepts");

        Map<URI, Double> conceptsMap = new HashMap<URI, Double>();

        ObirsQuery query = new ObirsQuery(defaultNameSpace, conceptsMap, aggregator, similarityMeasure, aggregatorParameter, scoreThreshold, numberOfResults);
        
        if (concepts != null) {
            Iterator<JSONObject> iterator = concepts.iterator();
            URI uri;
            while (iterator.hasNext()) {
                JSONObject concept = iterator.next();
                Double weight =  (Double) concept.get("weight");
                String id = concept.get("id").toString();
                if(id.startsWith("http://")){
                    uri = new URIImpl(id);
                }
                else {
                    if(defaultNameSpace == null){
                        throw new Exception("defaultNameSpace not found");
                    }
                    uri = new URIImpl(defaultNameSpace + "/" + id);
                }
                if (ontologyEngine.hasVertex(uri)){
                    query.addConcept(uri, weight);
                }
            }
        }
        return query;
    }
    
}
