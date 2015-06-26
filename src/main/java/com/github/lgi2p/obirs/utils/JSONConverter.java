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

import com.github.lgi2p.obirs.core.model.ObirsQuery;
import com.github.lgi2p.obirs.core.model.ObirsResult;
import com.github.lgi2p.obirs.core.model.RefinedObirsQuery;
import com.github.lgi2p.obirs.core.engine.utils.ConceptMatch;
import com.github.lgi2p.obirs.utils.IndexerJSON.ItemMetadata;
import com.github.lgi2p.obirs.utils.autocomplete.Autocompletion_Trie;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
import org.slf4j.LoggerFactory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * Utility class used to convert JSON format outputs by the annotator into the
 * JSON format expected to load OBIRS index. The annotator generates a result
 * file for each item. The input file expected by Obirs is:
 * {"id":"14622599","title":"XXXXX","conceptIds":["http://obirs/D008382",...],"href":"http://www..."}
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class JSONConverter {

    static org.slf4j.Logger logger = LoggerFactory.getLogger(JSONConverter.class);

    
    // {"concepts": [{"uri": "http://www.cea.fr/ontotoxnuc#AnalyseStatistique", "weight": 0.5},{"uri":"http://www.cea.fr/ontotoxnuc#Uranium", "weight": 0.5}]}
    public static ObirsQuery parseObirsJSONQuery(SM_Engine engine, String jsonQuery) throws ParseException, Exception {

        logger.info("parsing query: " + jsonQuery);
        URIFactory factory = URIFactoryMemory.getSingleton();

        jsonQuery = jsonQuery.replace("\n", "");
        jsonQuery = jsonQuery.replace("\r", "");
        JSONParser parser = new JSONParser();

        Object obj = parser.parse(jsonQuery);
        JSONObject jsonObject = (JSONObject) obj;
        String aggregator = (String) jsonObject.get("aggregator");
        String similarityMeasure = (String) jsonObject.get("similarityMeasure");
        Double aggregatorParameter;
        if (jsonObject.get("aggregatorParameter") != null) {
            aggregatorParameter = Double.parseDouble(jsonObject.get("aggregatorParameter").toString());
        } else {
            aggregatorParameter = 2.0;
        }
        Double scoreThreshold = (Double) jsonObject.get("scoreThreshold");
        Integer numberOfResults = (Integer) jsonObject.get("numberOfResults");

        JSONArray concepts = (JSONArray) jsonObject.get("concepts");

        Map<URI, Double> conceptsMap = new HashMap<URI, Double>();

        ObirsQuery query = new ObirsQuery(conceptsMap, aggregator, similarityMeasure, aggregatorParameter, scoreThreshold, numberOfResults);

        if (concepts != null) {
            Iterator<JSONObject> iterator = concepts.iterator();
            while (iterator.hasNext()) {
                JSONObject concept = iterator.next();
                Double weight = Double.parseDouble(concept.get("weight").toString());
                Object uriString = concept.get("uri");

                if (uriString == null) {
                    throw new SLIB_Ex_Critic("Error a URI must be specified for each query concept");
                }
                URI uri = factory.getURI(uriString.toString());

                if (engine.getGraph().containsVertex(uri)) {
                    query.addConcept(uri, weight);
                } else {
                    throw new Exception("concept associated to URI '" + uri + "' does not exists...");
                }
            }
        }
        return query;
    }

    //{
    //	"selectedItemURIs":["http://www.mines-ales.fr/obirs/items/1132","http://www.mines-ales.fr/obirs/items/1133"],
    //	"rejectedItemURIs":["http://www.mines-ales.fr/obirs/items/1131"],
    //	"query": {
    //		"concepts": [
    //			{"uri": "http://www.cea.fr/ontotoxnuc#AnalyseStatistique", "weight": 0.5},
    //			{"uri":"http://www.cea.fr/ontotoxnuc#Uranium", "weight": 0.5}
    //			]
    //	}
    //}
    public static RefinedObirsQuery parseRefinedObirsQuery(SM_Engine engine, String jsonQuery) throws ParseException, IOException, Exception {
        
        logger.info("parsing json refined query");
        logger.info("query: "+jsonQuery);
        jsonQuery = jsonQuery.replace("\n", "");
        jsonQuery = jsonQuery.replace("\r", "");

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(jsonQuery);
        JSONObject jsonObject = (JSONObject) obj;

        JSONObject _query = (JSONObject) jsonObject.get("query");
        StringWriter jsQuery = new StringWriter();
        _query.writeJSONString(jsQuery);
        ObirsQuery obirsQuery = parseObirsJSONQuery(engine, jsQuery.toString());

        List<URI> selectedItemURIs = toURIs((List<String>) jsonObject.get("selectedItemURIs"));
        List<URI> rejectedItemURIs = toURIs((List<String>) jsonObject.get("rejectedItemURIs"));
        
        return new RefinedObirsQuery(obirsQuery, selectedItemURIs, rejectedItemURIs);
    }


    
    //    {
    //	"results":[
    //		{ 
    //			"itemTitle":"f835.json",
    //			"score":1.0,
    //			"itemId":"919",
    //			"itemURI":"ns0:919",
    //			"concepts":[
    //				{
    //					"relationType":"ns2:EXACT",
    //					"score":1.0,
    //					"queryConceptURI":"ns1:AnalyseStatistique",
    //					"matchingConceptURI":"ns1:AnalyseStatistique"
    //				},
    //				{
    //					"relationType":"ns2:EXACT",
    //					"score":1.0,
    //					"queryConceptURI":"ns1:Uranium",
    //					"matchingConceptURI":"ns1:Uranium"
    //				}
    //			],
    //			"href":"\/data\/toxnuc\/toxnuc_annots_5_11_14\/annots\/f835.json"
    //		},
    //		...
    //	],
    //	"infoConcepts":[
    //		{
    //			"label":"statistical analysis",
    //			"uri":"ns1:AnalyseStatistique"
    //		},
    //		{
    //			"label":"actinide",
    //			"uri":"ns1:Actinide"
    //		},
    //		...
    //	],
    //	"prefixes":[
    //		{"ns":"http:\/\/www.cea.fr\/ontotoxnuc#","prefix":"ns1"},
    //		{"ns":"http:\/\/www.mines-ales.fr\/obirs\/match_type\/","prefix":"ns2"},
    //		...
    //	]
    //}
    public static String jsonifyObirsResults(List<ObirsResult> results, IndexerJSON indexer, Map<URI, String> indexURI2Label) throws IOException {

        Map<String, String> namespace2prefix = new HashMap<String, String>();

        JSONArray jsonResults = new JSONArray();

        Set<URI> infoConcepts = new HashSet();

        StringWriter json = new StringWriter();

        for (ObirsResult result : results) {

            JSONObject jsonResult = new JSONObject();
            URI itemResultURI = result.getItemURI();

            ItemMetadata metadata = indexer.getMetadata(itemResultURI);

            jsonResult.put("itemTitle", metadata.title);
            jsonResult.put("href", metadata.href);
            jsonResult.put("itemId", metadata.idLiteral);
            jsonResult.put("itemURI", buildShortURI(result.getItemURI(), namespace2prefix));
            jsonResult.put("score", result.getScore());

            JSONArray jsonConcepts = new JSONArray();

            if (result.getConcepts() != null) {

                for (ConceptMatch concept : result.getConcepts()) {

                    JSONObject jsonConcept = new JSONObject();

                    jsonConcept.put("queryConceptURI", buildShortURI(concept.getQueryConceptURI(), namespace2prefix));
                    jsonConcept.put("matchingConceptURI", buildShortURI(concept.getMatchingConceptURI(), namespace2prefix));

                    infoConcepts.add(concept.getQueryConceptURI());
                    infoConcepts.add(concept.getMatchingConceptURI());

                    jsonConcept.put("relationType", buildShortURI(concept.getRelationType(),namespace2prefix));
                    jsonConcept.put("score", concept.getScore());
                    jsonConcepts.add(jsonConcept);
                }
            }
            jsonResult.put("concepts", jsonConcepts);
            jsonResults.add(jsonResult);
        }

        JSONArray jsonInfoConcepts = new JSONArray();
        for (URI uri : infoConcepts) {

            JSONObject jsonQueryConcept = new JSONObject();
            jsonQueryConcept.put("uri", buildShortURI(uri, namespace2prefix));
            jsonQueryConcept.put("label", indexURI2Label.get(uri));

            jsonInfoConcepts.add(jsonQueryConcept);
        }

        JSONArray jsonInfoNamespace = new JSONArray();
        for (Map.Entry<String,String> e : namespace2prefix.entrySet()) {

            JSONObject jsonQueryConcept = new JSONObject();
            jsonQueryConcept.put("ns", e.getKey());
            jsonQueryConcept.put("prefix", e.getValue());

            jsonInfoNamespace.add(jsonQueryConcept);
        }

        JSONObject finalJSONresult = new JSONObject();
        finalJSONresult.put("prefixes", jsonInfoNamespace);
        finalJSONresult.put("results", jsonResults);
        finalJSONresult.put("infoConcepts", jsonInfoConcepts);
        finalJSONresult.writeJSONString(json);
        return json.toString();
    }

    /**
     * Convert a given ObirsQuery object into a JSON format
     *
     * @param query
     * @return
     * @throws IOException
     */
    public static String jsonifyObirsQuery(ObirsQuery query) throws IOException {
        JSONObject jsonObject = new JSONObject();

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
            jsonConcept.put("uri", conceptURI.stringValue());
            jsonConcept.put("weight", queryConcept.getValue());
            jsonConcepts.add(jsonConcept);
        }
        jsonObject.put("concepts", jsonConcepts);

        jsonObject.writeJSONString(jsonQuery);
        return jsonQuery.toString();
    }

    /**
     * {"log": "", "annotations" : [{ "text": "uranium", "startpos": 723,
     * "endpos":730, "uris":["http://www.cea.fr/ontotoxnuc#Uranium"] },{ "text":
     * "protein", "startpos": 1837, "endpos":1845,
     * "uris":["http://www.cea.fr/ontotoxnuc#Proteine"] },{ "text": "plant",
     * "startpos": 4661, "endpos":4666,
     * "uris":["http://www.cea.fr/ontotoxnuc#Plante"] }]}
     *
     * @param jsonQuery
     * @return
     * @throws ParseException
     * @throws Exception
     */
    protected static String toJSONindexFormat(int id, String title, String content, String href) throws ParseException, Exception {

        URIFactory f = URIFactoryMemory.getSingleton();

        content = content.replace("\n", "").replace("\r", "");
        Object obj = new JSONParser().parse(content);
        JSONObject jsonObject = (JSONObject) obj;
        JSONArray annotations = (JSONArray) jsonObject.get("annotations");

        Set<URI> concepts = new HashSet<URI>();

        if (annotations != null) {
            Iterator<JSONObject> iterator = annotations.iterator();
            while (iterator.hasNext()) {
                JSONObject concept = iterator.next();
                JSONArray uris = (JSONArray) concept.get("uris");
                for (int i = 0; i < uris.size(); i++) {
                    concepts.add(f.getURI((String) uris.get(i)));
                }
            }

        }

        String urisAsString = "";
        for (URI u : concepts) {
            if (!urisAsString.isEmpty()) {
                urisAsString += ",";
            }
            urisAsString += "\"" + u.stringValue() + "\"";
        }
        return "{\"id\":\"" + id + "\",\"title\":\"" + title + "\",\"conceptIds\":[" + urisAsString + "],\"href\":\"" + href + "\"}";
    }

    

    private static List<URI> toURIs(List<String> list) {
        
        List<URI> uris = new ArrayList<URI>(list.size());

        URIFactory f = URIFactoryMemory.getSingleton();

        for (String s : list) {
            uris.add(f.getURI(s));
        }
        return uris;
    }

    private static String buildShortURI(URI itemURI, Map<String, String> namespace2prefix) {

        String ns, prefix, localname;
        ns = itemURI.getNamespace();
        localname = itemURI.getLocalName();

        if (!namespace2prefix.containsKey(ns)) {
            prefix = "ns" + namespace2prefix.size();
            namespace2prefix.put(ns, prefix);
        }
        else{
            prefix = namespace2prefix.get(ns);
        }

        return prefix + ":" + localname;

    }

    public static String buildJSONString(Set<Autocompletion_Trie.AutocompleteElement> autocomplete) throws IOException {
        
        JSONArray jsonResults = new JSONArray();
        StringWriter json = new StringWriter();

        for (Autocompletion_Trie.AutocompleteElement result : autocomplete) {

            JSONObject jsonResult = new JSONObject();

            jsonResult.put("uri", result.id);
            jsonResult.put("label", result.label);

            jsonResults.add(jsonResult);
        }

        
        JSONObject finalJSONresult = new JSONObject();
        finalJSONresult.put("results", jsonResults);
        finalJSONresult.writeJSONString(json);
        return json.toString();
    }

    public static void main(String[] args) throws Exception {

        String annotdir = "/data/toxnuc/toxnuc_annots_5_11_14/annots";
        String annotsIndex = "/data/toxnuc/toxnuc_annots_5_11_14.json";

        File folder = new File(annotdir);
        File[] listOfFiles = folder.listFiles();

        PrintWriter printWriter = new PrintWriter(annotsIndex);

        int i = 0;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                System.out.println(file.getPath());
                String title = file.getName();
                String href = file.getPath();
                String json = toJSONindexFormat(i, title, Utils.readFileAsString(file), href);
                i++;

                printWriter.println(json);
            }
        }
        printWriter.close();
        System.out.println("consult: " + annotsIndex);
    }
}
