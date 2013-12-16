/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs;

import org.openrdf.model.URI;

public class BestConcept {

    public URI uri;
    public double similarity;

    public URI getURI() {
        return uri;
    }

    public void setURI(URI uri) {
        this.uri = uri;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }
}
