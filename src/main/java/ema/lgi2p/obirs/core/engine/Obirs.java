package ema.lgi2p.obirs.core.engine;

import ema.lgi2p.obirs.core.model.ObirsQuery;
import ema.lgi2p.obirs.core.model.ObirsResult;
import java.util.List;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Interface defining the basic methods any Ontology-Based Information Retrieval
 * System (OBIRS) must implements.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public interface Obirs {

    /**
     * Queries the underlying collection considering the given query parameters.
     * Return a list of results ordered according to their score (the first
     * element has the better score).
     *
     * @param query the query considered
     * @return a list of results
     * @throws SLIB_Ex_Critic
     */
    List<ObirsResult> query(ObirsQuery query) throws SLIB_Ex_Critic;

}
