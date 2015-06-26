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

import com.github.lgi2p.obirs.Conf;
import com.github.lgi2p.obirs.core.index.Indexer;
import com.github.lgi2p.obirs.core.model.Item;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class IndexerJSON implements Indexer {

     static org.slf4j.Logger logger = LoggerFactory.getLogger(IndexerJSON.class);
    List<Item> items;
    Map<URI, ItemMetadata> metadata;
    
    URIFactory URIFactory = URIFactoryMemory.getSingleton();

    public void index(String indexFilePath) throws SLIB_Ex_Critic {

        items = new ArrayList();
        metadata = new HashMap();
        
        logger.info("Loading items");

        JSONParser parser = new JSONParser();
        BufferedReader br = null;
        String line = null;
        try {
            br = new BufferedReader(new FileReader(indexFilePath));

            while ((line = br.readLine()) != null) {
                JSONObject jsonObject = (JSONObject) parser.parse(line);
                indexItem(jsonObject.toJSONString());
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new SLIB_Ex_Critic("error processing line: " + line + "\n" + e.getMessage());
        }
        logger.info("indexation done (" + items.size() + " items)");
    }

    // "uri":"http://id/14622599","label":"XXXXX","annots":["http://obirs/D008382",...],"href":"http://www..."}
    public void indexItem(String json) throws IOException, ParseException {

        JSONParser parser = new JSONParser();

        Object obj;
        obj = parser.parse(json);
        JSONObject jsonObject = (JSONObject) obj;
        String uri = (String) jsonObject.get("uri").toString();
        String label = (String) jsonObject.get("label");
        String href = (String) jsonObject.get("href");

        URI itemUri = URIFactoryMemory.getSingleton().getURI(uri);
        Set<URI> itemAnnotations = new HashSet();
        

        ItemMetadata meta = new ItemMetadata(uri, label, href);

        JSONArray conceptIds = (JSONArray) jsonObject.get("annots");

        for (int i = 0; i < conceptIds.size(); i++) {
            String conceptURI = (String) conceptIds.get(i);
            URI uriConcept = URIFactory.getURI(conceptURI);
            itemAnnotations.add(uriConcept);
        }

        items.add(new Item(itemUri, itemAnnotations));
        metadata.put(itemUri, meta);
    }

    public Iterable<Item> getItems() {
        return items;
    }
    
    public ItemMetadata getMetadata(URI itemURI) {
        return metadata.get(itemURI);
    }

    public class ItemMetadata {

        String idLiteral, title, href;

        public ItemMetadata(String idLiteral, String title, String href) {
            this.idLiteral = idLiteral;
            this.title = title;
            this.href = href;
        }

        public String getIdLiteral() {
            return idLiteral;
        }

        public String getTitle() {
            return title;
        }

        public String getHref() {
            return href;
        }

    }

}
