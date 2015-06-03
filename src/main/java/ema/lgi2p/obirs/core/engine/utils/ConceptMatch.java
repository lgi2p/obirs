package ema.lgi2p.obirs.core.engine.utils;

import org.openrdf.model.URI;

public class ConceptMatch {

    private final URI queryConceptURI;
    private final URI matchingConceptURI;
    private URI relationType;
    private final double score;

    public ConceptMatch(URI queryConceptURI, URI matchingConceptURI, URI relationType, double score) {
        this.queryConceptURI = queryConceptURI;
        this.matchingConceptURI = matchingConceptURI;
        this.relationType = relationType;
        this.score = score;
    }

    public ConceptMatch(URI queryConceptURI, URI matchingConceptURI, double score) {
        this.queryConceptURI = queryConceptURI;
        this.matchingConceptURI = matchingConceptURI;
        this.relationType = null;
        this.score = score;
    }

    /**
     *
     * @return The type of relationship linking the two concepts - may be null
     */
    public URI getRelationType() {
        return relationType;
    }

    public void setRelationType(URI relationType) {
        this.relationType = relationType;
    }

    public URI getQueryConceptURI() {
        return queryConceptURI;
    }

    public URI getMatchingConceptURI() {
        return matchingConceptURI;
    }

    public double getScore() {
        return score;
    }

}
