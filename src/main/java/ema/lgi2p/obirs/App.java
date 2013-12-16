package ema.lgi2p.obirs;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import slib.indexer.mesh.Indexer_MESH_XML;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) throws Exception {
        
        // create Options object
        Options options = new Options();

        // add t option
        options.addOption("o", "ontology", true, "ontology file path");
        options.addOption("i", "index", true, "index file path");
        options.addOption("q", "query", true, "query");
        options.addOption("r", "refined-query", true, "refined query");
        options.addOption("f", "fast-query", true, "use fast query");
        options.addOption("g", "groupwise", true, "use groupwise calculation");

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

        String ontologyFilePath = cmd.getOptionValue("o");
        String indexFilePath = cmd.getOptionValue("i");
        String query = cmd.getOptionValue("q");
        String refinedQuery = cmd.getOptionValue("r");
        String fast = cmd.getOptionValue("f");
        String groupwise = cmd.getOptionValue("g");
        
        
        if (ontologyFilePath != null && indexFilePath != null && (query != null || refinedQuery != null)) {
            try {
                String results = null;
                if (query != null) {
                    System.out.println("Quering...");
                    if (groupwise != null){
                        if (groupwise.equals("standalone")){
                            ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004); // IC
                            SMconf similarityMeasureConf = new SMconf("", SMConstants.FLAG_SIM_GROUPWISE_DAG_TO, icConf); 
                            results = new ObirsGroupwise(ontologyFilePath, indexFilePath, similarityMeasureConf).query(query);
                        }
                        else {
                            ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004); // IC
                            SMconf pairwiseMeasureConf = new SMconf("", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998, icConf);
                            SMconf groupwiseMeasureConf = new SMconf("", SMConstants.FLAG_SIM_GROUPWISE_BMA, icConf); 
                            results = new ObirsGroupwise(ontologyFilePath, indexFilePath, groupwiseMeasureConf, pairwiseMeasureConf).query(query);
                        }
                    }
                    else {
                        if(fast != null){
                            results = new ObirsMohameth2010(ontologyFilePath, indexFilePath).fastQuery(query);
                        } else {
                            try {   
                                results = new ObirsMohameth2010(ontologyFilePath, indexFilePath).query(query);
                            } catch (Exception ex) {
                                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
                else {
                    System.out.println("Refining query...");
                    results = new ObirsMohameth2010(ontologyFilePath, indexFilePath).refineQuery(refinedQuery);
                }
                System.out.println(results);
            } catch (SLIB_Ex_Critic ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            } catch (org.json.simple.parser.ParseException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("obirs", options);
        }
    }
}
