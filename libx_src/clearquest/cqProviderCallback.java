/**
 * <pre>
 * =============================================================================
 *
 *  IBM Internal Use Only
 *
 * =============================================================================
 *
 *  CREATOR: Aydin Suren
 *     DEPT: AW0V
 *     DATE: 03/01/2008
 *     
 *-PURPOSE----------------------------------------------------------------------------------------
 * Class definition for crating a connection to CQ.
 *------------------------------------------------------------------------------------------------------
 *
 * =============================================================================
 *
 * -CHANGE LOG------------------------------------------------------------------
 * 03/01/2008 AS  Initial coding.
 *
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.clearquest;

import javax.wvcm.ProviderFactory;

/**
 * @author asuren
 * 
 */
public class cqProviderCallback implements ProviderFactory.Callback {

    private Authentication m_unpw;
    private String sUsername = null;
    private String sPassword = null;


    /**
     * Creates a connection to the ClearQuest via Rational Team API for a given
     * username and password.
     * 
     * @param sUN
     *        Username of the ClearQuest user.
     * @param sPW
     *        User's ClearQuest password.
     */
    public cqProviderCallback(String sUN, String sPW) {
        sUsername = sUN;
        sPassword = sPW;
    }


    /*
     * (non-Javadoc)
     * 
     * @see javax.wvcm.ProviderFactory$Callback#getAuthentication(java.lang.String,
     *      int)
     */
    public Authentication getAuthentication(String realm, int retryCount) {

        // Try to reuse last credentials on each new repository
        if (m_unpw != null && retryCount == 0) {
            return m_unpw;
        }
        if (retryCount > 2) {
            return null;
        }

        return m_unpw = new Authentication() {
            public String loginName() {
                return sUsername;
            }


            public String password() {
                return sPassword;
            }
        };

    }
}
