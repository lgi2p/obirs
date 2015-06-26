package com.github.lgi2p.obirs.core.index;

import com.github.lgi2p.obirs.core.model.Item;
import java.util.List;
import org.openrdf.model.URI;

public interface ItemCollection extends Iterable<Item> {

    /**
     * @param uris the URIs of the items you want to retrieve.
     * @return an iterable view of the items associated to the given URIs.
     */
    public Iterable<Item> findItemsByURIs(List<URI> uris);

    /**
     * Search for items annotated by the given URI.
     *
     * @param uri
     * @return an iterable view of the items annotated by the given URI. If no item is
     * annotated by this URI an empty, not null, iterable is return.
     */
    public Iterable<Item> getItemsAnnotatedByConcept(URI uri);

    /**
     * @return the number of indexed items.
     */
    public int size();
}
