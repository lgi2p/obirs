/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;


public class IndexationFile implements Indexation {

    private List<Document> indexation;
    private HashMap<URI, HashSet<Document>> concept2documents;

    public IndexationFile(String indexFilePath) throws ParseException {
        indexation = new ArrayList<Document>();
        concept2documents = new HashMap<URI, HashSet<Document>>();
        JSONParser parser = new JSONParser();
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader(indexFilePath));

            while ((line = br.readLine()) != null) {
                JSONObject jsonObject = (JSONObject) parser.parse(line);
                indexDocument(jsonObject.toJSONString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("indexation done ("+indexation.size()+" documents)");
    }

    public final void indexDocument(String json) throws IOException {

        Document doc = buildDocObject(json);


        for (URI conceptURI : doc.getConceptURIs()) {
            if (!concept2documents.containsKey(conceptURI)) {
                concept2documents.put(conceptURI, new HashSet<Document>());
            }
            concept2documents.get(conceptURI).add(doc);
        }
        indexation.add(doc);
    }

    public Iterator<Document> iterator() {
        return indexation.iterator();
    }

    public int size(){
        return indexation.size();
    }
    
    public Iterable<Document> findByIds(List<String> documentIds) {
        ArrayList<Document> results = new ArrayList<Document>();
        for (Document document : indexation) {
            for (String docId : documentIds) {
                if (document.getId().equals(docId)) {
                    results.add(document);
                }
            }
        }
        return results;
    }

    public HashSet<Document> getDocumentByConceptURI(URI conceptURI) {
        if (concept2documents.containsKey(conceptURI)) {
            return concept2documents.get(conceptURI);
        }
        return new HashSet<Document>();
    }

    /**
     * {"id":"14622599","title":"XXXXX","conceptIds":["http://obirs/D008382",...],"href":"http://www..."}
     *
     * @param jsonString
     * @return
     */
    Document buildDocObject(String jsonString) {

        JSONParser parser = new JSONParser();
        Document doc = new Document();

        Object obj;
        try {
            obj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject) obj;
            String id = (String) jsonObject.get("id").toString();
            String title = (String) jsonObject.get("title");
            String href = (String) jsonObject.get("href");

            doc.setId(id);
            doc.setTitle(title);
            doc.setHref(href);

            JSONArray conceptIds = (JSONArray) jsonObject.get("conceptIds");
            for (int i = 0; i < conceptIds.size(); i++) {
                String conceptURI = (String) conceptIds.get(i);
                URI uri = new URIImpl(conceptURI);
                doc.getConceptURIs().add(uri);
            }
        } catch (ParseException ex) {
            Logger.getLogger(IndexationFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return doc;
    }

    private class DocObject {
    }
}
