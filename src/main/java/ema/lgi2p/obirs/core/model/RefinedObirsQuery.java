/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs.core.model;

import java.util.List;
import org.openrdf.model.URI;

public class RefinedObirsQuery {

    private List<URI> selectedItemURIs;
    private List<URI> rejectedItemURIs;
    private ObirsQuery query;
    
    public RefinedObirsQuery(ObirsQuery query, List<URI> selectedItemURIs, List<URI> rejectedItemURIs){
        this.query = query;
        this.selectedItemURIs = selectedItemURIs;
        this.rejectedItemURIs = rejectedItemURIs;
    }

    public List<URI> getSelectedItemURIs() {
        return selectedItemURIs;
    }

    public void setSelectedItemURIs(List<URI> selectedItemURIs) {
        this.selectedItemURIs = selectedItemURIs;
    }

    public List<URI> getRejectedItemURIs() {
        return rejectedItemURIs;
    }

    public void setRejectedItemURIs(List<URI> otherDocURIs) {
        this.rejectedItemURIs = otherDocURIs;
    }

    public ObirsQuery getQuery() {
        return query;
    }

    public void setQuery(ObirsQuery query) {
        this.query = query;
    }
}
