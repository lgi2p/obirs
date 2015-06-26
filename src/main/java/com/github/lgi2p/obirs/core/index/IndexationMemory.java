/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lgi2p.obirs.core.index;

import com.github.lgi2p.obirs.core.model.Item;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.parser.ParseException;
import org.openrdf.model.URI;
import org.slf4j.LoggerFactory;

public class IndexationMemory implements ItemCollection {

    private final Set<Item> indexation;
    private final Map<URI, Set<Item>> concept2documents;
    static org.slf4j.Logger logger = LoggerFactory.getLogger(IndexationMemory.class);

    public IndexationMemory(Iterable<Item> collection) throws ParseException {

        indexation = new HashSet<Item>();
        concept2documents = new HashMap();

        for (Item item : collection) {
            
            indexation.add(item);
            
            for (URI conceptURI : item.getAnnotations()) {
                if (!concept2documents.containsKey(conceptURI)) {
                    concept2documents.put(conceptURI, new HashSet<Item>());
                }
                concept2documents.get(conceptURI).add(item);
            }
        }
        logger.info("advanced indexation done (" + indexation.size() + " items)");
    }

    public Iterator<Item> iterator() {
        return indexation.iterator();
    }

    public int size() {
        return indexation.size();
    }

    @Override
    public Iterable<Item> findItemsByURIs(List<URI> itemURIs) {
        ArrayList<Item> results = new ArrayList<Item>();
        for (Item item : indexation) {
            for (URI itemURI : itemURIs) {
                if (item.getURI().equals(itemURI)) {
                    results.add(item);
                }
            }
        }
        return results;
    }

    @Override
    public Iterable<Item> getItemsAnnotatedByConcept(URI conceptURI) {
        if (concept2documents.containsKey(conceptURI)) {
            return concept2documents.get(conceptURI);
        }
        return new HashSet<Item>();
    }

}
