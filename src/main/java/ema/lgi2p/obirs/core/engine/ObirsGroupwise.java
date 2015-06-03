package ema.lgi2p.obirs.core.engine;

import ema.lgi2p.obirs.core.model.ObirsQuery;
import ema.lgi2p.obirs.core.model.ObirsResult;
import ema.lgi2p.obirs.core.index.ItemCollection;
import ema.lgi2p.obirs.core.model.Item;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import java.util.List;
import java.util.Set;
import org.json.simple.parser.ParseException;
import org.openrdf.model.URI;
import org.slf4j.LoggerFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * Basic implementation of the OBIRS interface based on semantic similarity
 * measures for distinguishing relevant items in the collection with regard to a
 * query.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class ObirsGroupwise implements Obirs {

    SM_Engine engine;
    ItemCollection index;
    SMconf similarityGroupwiseConf;
    SMconf similarityPairwiseConf;

    static org.slf4j.Logger logger = LoggerFactory.getLogger(ObirsGroupwise.class);

    public ObirsGroupwise(SM_Engine engine, ItemCollection index, SMconf similarityGroupwiseConf) throws SLIB_Exception, ParseException {
        this.engine = engine;
        this.similarityGroupwiseConf = similarityGroupwiseConf;
        this.index = index;
    }

    public ObirsGroupwise(SM_Engine engine, ItemCollection index, SMconf similarityGroupwiseConf, SMconf similarityPairwiseConf) throws SLIB_Exception, ParseException {
        this(engine, index, similarityGroupwiseConf);
        this.similarityPairwiseConf = similarityPairwiseConf;
    }

    public List<ObirsResult> query(ObirsQuery query) throws SLIB_Ex_Critic {

        logger.info("Query: " + this.getClass().getName());
        logger.info(query.toString());

        List<ObirsResult> results = new ArrayList<ObirsResult>();

        query.normalizeWeight();

        if (query.getConcepts().isEmpty()) {
            return results;
        }

        Set<URI> queryConceptURIs = new HashSet<URI>();

        for (URI queryConceptURI : query.getConcepts().keySet()) {
            if (engine.getGraph().containsVertex(queryConceptURI)) {
                queryConceptURIs.add(queryConceptURI);
            }
        }
        
        double threashold = query.getScoreThreshold();

        for (Item item : index) {

            Set<URI> itemAnnotations = item.getAnnotations();
            double score = -1;

            if (!itemAnnotations.isEmpty()) {
                if (similarityPairwiseConf != null) {
                    score = engine.compare(similarityGroupwiseConf, similarityPairwiseConf, queryConceptURIs, itemAnnotations);
                } else {
                    score = engine.compare(similarityGroupwiseConf, queryConceptURIs, itemAnnotations);
                }
            }
            if (score > threashold) {
                results.add(new ObirsResult(item.getURI(),score));
            }
        }
        Collections.sort(results);
        if (results.size() > query.getNumberOfResults()) {
            results = results.subList(0, query.getNumberOfResults());
        }
        return results;
    }
}
