/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs;

import java.util.HashSet;
import java.util.List;
import org.openrdf.model.URI;

public interface Indexation extends Iterable<Document> {
    
    public Iterable<Document> findByIds(List<String> documentIds);
    public HashSet<Document> getDocumentByConceptURI(URI conceptURI);
    public int size();
}
