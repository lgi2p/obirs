/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs;

import java.util.List;

public class RefinedObirsQuery {

    private List<String> selectedDocIds;
    private List<String> rejectedDocIds;
    private ObirsQuery query;
    
    public RefinedObirsQuery(ObirsQuery query, List<String> selectedDocIds, List<String> rejectedDocIds){
        this.query = query;
        this.selectedDocIds = selectedDocIds;
        this.rejectedDocIds = rejectedDocIds;
    }

    public List<String> getSelectedDocIds() {
        return selectedDocIds;
    }

    public void setSelectedDocIds(List<String> selectedDocIds) {
        this.selectedDocIds = selectedDocIds;
    }

    public List<String> getRejectedDocIds() {
        return rejectedDocIds;
    }

    public void setRejectedDocIds(List<String> otherDocIds) {
        this.rejectedDocIds = otherDocIds;
    }

    public ObirsQuery getQuery() {
        return query;
    }

    public void setQuery(ObirsQuery query) {
        this.query = query;
    }
}
