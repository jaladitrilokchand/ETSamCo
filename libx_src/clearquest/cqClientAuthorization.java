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
 * This class is used for authenticating the clients to see if they can use the service.
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

import java.util.Vector;

/**
 * @author asuren
 * 
 */
public class cqClientAuthorization {

    private cqUser xCqUser = null;
    private cqClient xCqClient = null;
    private String sService = null;

    protected static final String DETERMINE_AUT_QUERY = "Public Queries/Administration/Web Services/Determine Authorization";

    protected static final String SERVICE_USAGE_TODAY_QUERY = "Public Queries/Administration/Web Services/Services Usage Today - By Client and Service";

    /**
     * Constructor.
     * 
     * @param xCqClient
     *            cqClient object.
     * @param sService
     *            Service name to see if it is a valid service.
     * @throws Exception
     */
    public cqClientAuthorization(cqUser xCqUser, cqClient xCqClient,
	    String sService) throws Exception {
	super();
	this.xCqClient = xCqClient;
	this.xCqUser = xCqUser;

	// Make sure it is a valid service.
	cqUtil.validateService(sService);
	this.sService = sService;

    }

    /**
     * Checks to see if the client is authorized to use a service and number of
     * service call they can make.
     * 
     * @throws Exception
     */
    public void checkAuthorization() throws Exception {

	try {

	    // cqUtil.sCREATE_SERVICE and sUPDATE_* services need to be check
	    // for access and number of request a client can make. Others are
	    // open the public without any restrictions.
	    if (sService.equals(cqUtil.sCREATE_PROBLEM_SERVICE)
		    || sService.equals(cqUtil.sUPDATE_PROBLEM_SERVICE)
		    || sService.equals(cqUtil.sCREATE_DIP_REVISION_SERVICE)
		    || sService.equals(cqUtil.sUPDATE_DIP_REVISION_SERVICE)
		    || sService.equals(cqUtil.sUPDATE_TK_PATCH_SERVICE)
		    || sService.equals(cqUtil.sUPDATE_TK_INJREQ_SERVICE)) {

		//
		// Query the CQ database to see if the client can use "create"
		// service by using the DETERMINE_AUT_QUERY.
		//
		StringBuffer sbXML_A = new StringBuffer(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sbXML_A.append("<CqQuery><login>");
		sbXML_A.append("<username>" + xCqUser.getUsername()
			+ "</username>");
		sbXML_A.append("<password>" + xCqUser.getPassword()
			+ "</password>");
		sbXML_A.append("<database>" + xCqUser.getDatabase()
			+ "</database>");
		sbXML_A.append("<schema>" + xCqUser.getSchema() + "</schema>");
		sbXML_A.append("</login><client>");
		sbXML_A.append("<app-name>" + xCqClient.getClientName()
			+ "</app-name>");
		sbXML_A.append("</client><query>");

		StringBuffer sbXML_B = new StringBuffer("<filters>");
		sbXML_B
			.append("<filter name=\"service_requester_name\" operator=\"Equals\" value=\""
				+ xCqClient.getClientName() + "\" />");
		sbXML_B
			.append("<filter name=\"sys_cq_service\" operator=\"Equals\" value=\""
				+ sService + "\" />");
		sbXML_B.append("</filters></query></CqQuery>");

		StringBuffer sbXML = new StringBuffer();
		sbXML.append(sbXML_A);
		sbXML.append("<query-name>" + DETERMINE_AUT_QUERY
			+ "</query-name>");
		sbXML.append(sbXML_B);

		cqQueryService xCqQueryService = new cqQueryService();
		String sReturn = xCqQueryService.runQuery(sbXML);

		// Parse the result to see if the client can use the "create"
		// service and the max limit in a day.
		cqQueryOutput xCqQueryOutput = new cqQueryOutput(sReturn);
		Vector vcResults = xCqQueryOutput.getResultObjects();
		if (vcResults.size() <= 0) {
		    throw new Exception(
			    xCqClient.getClientName()
				    + " is not authorized to use "
				    + sService
				    + " service."
				    + " Please contact to ClearQuest admin for getting access.");
		}

		// Get the max limit.
		Object[] oResult = (Object[]) vcResults.firstElement();
		String sMaxLimit = (String) oResult[4];

		// Query CQ database to see how many request the client
		// made today for this service.
		sbXML = new StringBuffer();
		sbXML.append(sbXML_A);
		sbXML.append("<query-name>" + SERVICE_USAGE_TODAY_QUERY
			+ "</query-name>");
		sbXML.append(sbXML_B);

		xCqQueryService = new cqQueryService();
		sReturn = xCqQueryService.runQuery(sbXML);

		// Parse the results and compare the numbers.
		xCqQueryOutput = new cqQueryOutput(sReturn);
		vcResults = xCqQueryOutput.getResultObjects();
		int iTodaysUsage = vcResults.size();
		if (iTodaysUsage >= Integer.parseInt(sMaxLimit)) {
		    throw new Exception(
			    xCqClient.getClientName()
				    + " has reached its max allowed this service usage."
				    + " Please contact to ClearQuest admin for further descussion."
				    + " Today's Usage = " + iTodaysUsage
				    + " -- sMaxLimit = " + sMaxLimit + ".");
		}

		// Client can use the restricted service since it has not used
		// the daily quota.

	    }

	} catch (Exception e) {
	    throw (e);
	} finally {
	}

    }
}
