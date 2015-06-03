package ema.lgi2p.obirs;

import ema.lgi2p.obirs.core.model.ObirsQuery;
import ema.lgi2p.obirs.core.model.ObirsResult;
import ema.lgi2p.obirs.core.model.RefinedObirsQuery;
import ema.lgi2p.obirs.core.engine.ObirsMohameth2010;
import ema.lgi2p.obirs.core.engine.ObirsGroupwise;
import ema.lgi2p.obirs.core.index.ItemCollection;
import ema.lgi2p.obirs.core.index.IndexationMemory;
import ema.lgi2p.obirs.core.model.Item;
import ema.lgi2p.obirs.utils.IndexerJSON;
import ema.lgi2p.obirs.utils.JSONConverter;
import ema.lgi2p.obirs.utils.Utils;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.openrdf.model.URI;
import org.slf4j.LoggerFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;

/**
 * Class used to launch a command-line version of OBIRS
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class App {

    static org.slf4j.Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {

        Options options = new Options();

        // add option
        options.addOption("o", "ontology", true, "ontology file path (RDF/XML Format)");
        options.addOption("c", "concept index", true, "File defining the labels of the concepts defined into the given ontology");
        options.addOption("i", "collection index", true, "JSON index describing the item collection to query. This file defines the concepts associated to each items");
        options.addOption("q", "query-file", true, "query file that contains the JSON query");
        options.addOption("r", "refined-query", true, "refined query that contains the JSON query to refine");
        options.addOption("f", "fast-query", true, "use fast query");
        options.addOption("g", "groupwise", true, "use groupwise calculation");

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);

            String ontoFile = cmd.getOptionValue("o");
            String conceptIndexFile = cmd.getOptionValue("c");
            String indexFile = cmd.getOptionValue("i");
            String jsonQuery = cmd.getOptionValue("q");
            String jsonRefinedQuery = cmd.getOptionValue("r");
            String fast = cmd.getOptionValue("f");
            String groupwise = cmd.getOptionValue("g");

            logger.info("OBIRS");
            logger.info("Ontology  : " + ontoFile);
            logger.info("Concept Index     : " + conceptIndexFile);
            logger.info("Index     : " + indexFile);
            logger.info("Query file: " + jsonQuery);
            logger.info("Query(ref): " + jsonRefinedQuery);
            logger.info("fast      : " + fast);
            logger.info("groupwise : " + groupwise);

            if (conceptIndexFile != null && ontoFile != null && indexFile != null && (jsonQuery != null || jsonRefinedQuery != null)) {

                logger.info("Loading concept labels index");
                Map<URI, String> conceptLabels = Utils.loadConceptLabels(conceptIndexFile);

                logger.info("loading ontology & building engine...");
                SM_Engine engine = Utils.buildSMEngine(ontoFile);

                logger.info("loading index...");
                IndexerJSON indexer = new IndexerJSON();
                indexer.index(indexFile);

                Iterable<Item> items = indexer.getItems();
                ItemCollection index = new IndexationMemory(items);

                List<ObirsResult> results = null;
                String jsonResults = null;

                if (jsonQuery != null) {

                    jsonQuery = Utils.readFileAsString(new File(jsonQuery));
                    logger.info("Loading query: " + jsonQuery);

                    logger.info("Quering...");
                    ObirsQuery query = JSONConverter.parseObirsJSONQuery(engine, jsonQuery);

                    if (groupwise != null) {
                        if (groupwise.equals("standalone")) {
                            SMconf similarityMeasureConf = Conf.getDefaultDirectGroupwiseSimilarityMeasure();
                            results = new ObirsGroupwise(engine, index, similarityMeasureConf).query(query);
                        } else {
                            SMconf pairwiseMeasureConf = Conf.getDefaultPairwiseSimilarityMeasure();
                            SMconf groupwiseMeasureConf = Conf.getDefaultIndirectGroupwiseSimilarityMeasure();
                            results = new ObirsGroupwise(engine, index, groupwiseMeasureConf, pairwiseMeasureConf).query(query);
                        }
                    } else {
                        if (fast != null) {
                            results = new ObirsMohameth2010(engine, index).fastQuery(query);
                        } else {
                            results = new ObirsMohameth2010(engine, index).query(query);
                        }
                    }

                    jsonResults = JSONConverter.jsonifyObirsResults(results, indexer, conceptLabels);

                } else {
                    logger.info("Refining query...");
                    jsonRefinedQuery = Utils.readFileAsString(new File(jsonRefinedQuery));
                    logger.info("query:" + jsonRefinedQuery);
                    RefinedObirsQuery refinedQuery = JSONConverter.parseRefinedObirsQuery(engine, jsonRefinedQuery);
                    ObirsQuery oQuery = new ObirsMohameth2010(engine, index).refineQuery(refinedQuery);
                    jsonResults = JSONConverter.jsonifyObirsQuery(oQuery);
                }
                logger.info(jsonResults);

            } else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("obirs", options);
            }

        } catch (UnrecognizedOptionException e) {
            logger.info(e.getMessage());
            if (cmd != null) {
                logger.info(options.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Error: " + ex.getMessage());
        }
    }
}
