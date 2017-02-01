/**
 * <pre>
 * =============================================================================
 *
 *  IBM Internal Use Only
 *
 * =============================================================================
 *
 *  CREATOR: Aydin Suren
 *     
 *-PURPOSE----------------------------------------------------------------------------------------
 * Class definition for the CQ clients.
 *------------------------------------------------------------------------------------------------------
 *
 * -CHANGE LOG------------------------------------------------------------------
 * 03/01/2008 AS  Initial coding.
 *
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.clearquest;

/**
 * @author asuren
 * 
 */
public class cqClient {

    private String clientName = null;

    /**
     * Sets client name.
     * 
     * @param clientName
     */
    public cqClient(String clientName) {
	this.clientName = clientName;
    }

    /**
     * Returns client name.
     * 
     * @return
     */
    public String getClientName() {
	return clientName;
    }

}
