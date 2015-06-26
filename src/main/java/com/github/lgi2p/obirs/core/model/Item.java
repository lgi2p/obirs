package com.github.lgi2p.obirs.core.model;

import java.util.Set;
import org.openrdf.model.URI;

/**
 * An Item is a simple object (1) unambiguously identified by a URI and (2)
 * associated (indexed) to a set of URIs. An item can be used to represent a
 * document/gene/object annotated by a set of concepts defined into an ontology.
 */
public class Item {

    private final URI uri;
    private final Set<URI> annotations;

    /**
     * Build an item considering the given item as the item URI. This URI will
     * therefore be used to unambiguously (uniquely) identify this item.
     *
     * @param uri the identifier of the item
     * @param annotations the set of annotations associated to the item
     */
    public Item(URI uri, Set<URI> annotations) {
        this.uri = uri;
        this.annotations = annotations;
    }

    /**
     * @return the URI of this item
     */
    public URI getURI() {
        return uri;
    }

    /**
     * @return the final set of annotations associated to the item
     */
    public Set<URI> getAnnotations() {
        return annotations;
    }
}
