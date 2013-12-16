/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs;

import java.util.ArrayList;
import java.util.List;
import org.openrdf.model.URI;

public class Document {

    private String id;
    private String title;
    private String href;
    private List<URI> conceptURIs = new ArrayList<URI>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<URI> getConceptURIs() {
        return this.conceptURIs;
    }

    public void setConceptURIs(List<URI> conceptIds) {
        this.conceptURIs = conceptIds;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
