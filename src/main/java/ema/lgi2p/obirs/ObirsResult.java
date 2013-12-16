/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs;

import java.util.ArrayList;
import java.util.List;

public class ObirsResult implements Comparable<ObirsResult> {

    private String docId;
    private String docTitle;
    private Double score;
    private String href;
    private List<ResultSet> concepts;

    @Override
    public int compareTo(ObirsResult o) {
        if (o.getScore() > score) {
            return 1;
        } else if (o.getScore() < score) {
            return -1;
        } else {
            return 0;
        }
//		return (int)(o.getScore() - score);
    }

    public void addConcept(ResultSet concept) {
        if (this.concepts == null) {
            this.concepts = new ArrayList<ResultSet>();
        }
        this.concepts.add(concept);
    }

    public List<ResultSet> getConcepts() {
        return concepts;
    }

    public void setConcepts(List<ResultSet> concepts) {
        this.concepts = concepts;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getScore() {
        return score;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getDocId() {
        return docId;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}