/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lgi2p.obirs.core.engine.utils;

import org.openrdf.model.URI;

public class ConceptSim {

    public URI matchingConcept;
    public double similarity;

    public ConceptSim(URI matchingConcept, double max_similarity) {
        this.matchingConcept = matchingConcept;
        this.similarity = max_similarity;
    }

    public URI getURI() {
        return matchingConcept;
    }


    public double getSimilarity() {
        return similarity;
    }
}
