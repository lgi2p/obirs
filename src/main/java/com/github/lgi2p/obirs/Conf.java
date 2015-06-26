/*
 *  Copyright or © or Copr. Ecole des Mines d'Alès (2012-2014) 
 *  
 *  This software is a computer program whose purpose is to provide 
 *  several functionalities for the processing of semantic data 
 *  sources such as ontologies or text corpora.
 *  
 *  This software is governed by the CeCILL  license under French law and
 *  abiding by the rules of distribution of free software.  You can  use, 
 *  modify and/ or redistribute the software under the terms of the CeCILL
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info". 
 * 
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability. 

 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or 
 *  data to be ensured and,  more generally, to use and operate it in the 
 *  same conditions as regards security. 
 * 
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL license and that you accept its terms.
 */
package com.github.lgi2p.obirs;

import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Defines default configuration
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Conf {
    
    public static final String DEFAULT_URI = "http://www.mines-ales.fr/obirs";
    public static final double DEFAULT_QUERY_AGGREGATION_PARAM_VALUE = 2.0;
    public static final double DEFAULT_QUERY_SCORE_THRESHOLD_VALUE = 0.0;
    public static final int DEFAULT_QUERY_NUMBER_OF_RESULT_VALUE = 30;
    public static double DEFAULT_QUERY_FAST_SIMILARITY_THREASHOLD = 0.1;
    
    
    public static SMconf getDefaultPairwiseSimilarityMeasure() throws SLIB_Ex_Critic {
        ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SANCHEZ_2011);
        return new SMconf("Lin_icSanchez", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998, icConf);
    }

    public static SMconf getDefaultDirectGroupwiseSimilarityMeasure() throws SLIB_Ex_Critic {
        ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004);
        SMconf similarityMeasureConf = new SMconf(SMConstants.FLAG_SIM_GROUPWISE_DAG_TO, icConf);
        return similarityMeasureConf;
    }

    public static SMconf getDefaultIndirectGroupwiseSimilarityMeasure() throws SLIB_Ex_Critic {
        SMconf groupwiseMeasureConf = new SMconf(SMConstants.FLAG_SIM_GROUPWISE_BMA);
        return groupwiseMeasureConf;
    }
}
