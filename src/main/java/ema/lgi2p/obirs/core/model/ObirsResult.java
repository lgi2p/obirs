/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ema.lgi2p.obirs.core.model;

import ema.lgi2p.obirs.core.engine.utils.ConceptMatch;
import java.util.ArrayList;
import java.util.List;
import org.openrdf.model.URI;

/**
 * Representation of a result produced by the IRS. It is unambiguously
 * identified by a URI and is associated to a score and a list of matching
 * concepts.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class ObirsResult implements Comparable<ObirsResult> {

    private final URI itemURI;
    private final double score;
    private final List<ConceptMatch> concepts;

    public ObirsResult(URI uri, double score) {
        this(uri, score, null);
    }

    public ObirsResult(URI uri, double score, List<ConceptMatch> concepts) {
        this.itemURI = uri;
        this.score = score;
        this.concepts = concepts;
    }

    @Override
    public int compareTo(ObirsResult o) {
        if (o.getScore() > score) {
            return 1;
        } else if (o.getScore() < score) {
            return -1;
        } else {
            return 0;
        }
    }

    public List<ConceptMatch> getConcepts() {
        return concepts;
    }

    public Double getScore() {
        return score;
    }

    public URI getItemURI() {
        return itemURI;
    }
}
