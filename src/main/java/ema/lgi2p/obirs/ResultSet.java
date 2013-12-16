/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs;

import org.openrdf.model.URI;

public class ResultSet {

    private URI queryConceptURI;
    private URI matchingConceptURI;
//    private String queryConceptTitle;
//    private String matchingConceptTitle;
    private String relationType;
    private double score;

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public URI getQueryConceptURI() {
        return queryConceptURI;
    }

    public void setQueryConceptURI(URI queryConceptURI) {
        this.queryConceptURI = queryConceptURI;
    }

    public URI getMatchingConceptURI() {
        return matchingConceptURI;
    }

    public void setMatchingConceptURI(URI matchingConceptURI) {
        this.matchingConceptURI = matchingConceptURI;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

//    public void setQueryConceptTitle(String title) {
//        queryConceptTitle = title;
//    }
//
//    public String getQueryConceptTitle() {
//        return queryConceptTitle;
//    }
//
//    public void setMatchingConceptTitle(String title) {
//        matchingConceptTitle = title;
//    }
//
//    public String getMatchingConceptTitle() {
//        return matchingConceptTitle;
//    }
}
