/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs;

import org.openrdf.model.URI;

public class Concept {

    private URI uri;
    private String title;

    public Concept(URI uri) {
        this.uri = uri;
    }
    
    public URI getURI() {
        return uri;
    }

    public void setURI(URI uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
