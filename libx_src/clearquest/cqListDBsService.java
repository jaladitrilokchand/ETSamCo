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
 * Service class definition.
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Vector;

import javax.wvcm.ResourceList;

import com.ibm.rational.wvcm.stp.cq.CqProvider;

/**
 * @author asuren
 * 
 */
public class cqListDBsService extends cqService {

    private static final String DATABASE_PREFIX = "cq.userdb:";

    /**
     * For unit testing.
     * 
     * @param args
     */
    public static void main(String[] args) {

	String sResult = null;
	StringBuffer sbResults = null;
	cqListDBsService xCqListDBsService = null;
	cqListDBsOutput xCqListDBsOutput = null;

	try {

	    xCqListDBsService = new cqListDBsService();
	    sResult = xCqListDBsService.listDBs(cqUtil
		    .getFileContent(cqListDBsService.class
			    .getResource(cqUtil.sLIST_DBS_XML)));
	    System.out.println("Result:\n" + sResult);
	    xCqListDBsOutput = new cqListDBsOutput(sResult);
	    sbResults = xCqListDBsOutput.getResultsAsDelimited(",", true);
	    System.out.println("\nResult as Delimited:\n"
		    + sbResults.toString());

	} catch (Throwable t) {
	    t.printStackTrace();
	} finally {
	    xCqListDBsService = null;
	    xCqListDBsOutput = null;
	    sbResults = null;
	}

    }

    /**
     * This is the method that talks to ClearQuest via Rational Team API to get
     * the listing of the configured database and schemas.
     * 
     * @param sbXMLRecord
     *            An XML input that has all the necessary information to create
     *            a ClearQuest issue. For an example of input XML, please see
     *            cqListDBsExample.xml file and its corresponding XSD schema
     *            file.
     * @return XML formatted result of the action. Any errors/exception will be
     *         inside of the returned XML formatted string.
     */
    public String listDBs(StringBuffer sbXMLRecord) {

	String sFound = null;
	String sUser = null;
	String sPassword = null;
	String sError = null;
	CqProvider provider = null;
	Vector vcNames = null;
	cqListDBsInput xCqListDBsInput = null;
	cqUsageMeasurement xCqUsageMeasurement = null;
	cqClientAuthorization xCqClientAuthorization = null;

	StringBuffer sbResult = new StringBuffer();
	sbResult.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	sbResult.append("<CqListDBs>\n");

	try {

	    // Process the data in XML.
	    xCqListDBsInput = new cqListDBsInput(sbXMLRecord);
	    sUser = xCqListDBsInput.getCqUser().getUsername();
	    sPassword = xCqListDBsInput.getCqUser().getPassword();

	    // Initialize the measurements collection.
	    xCqUsageMeasurement = new cqUsageMeasurement(xCqListDBsInput
		    .getCqUser(), xCqListDBsInput.getCqClient(),
		    cqUtil.sLIST_SERVICE, true);

	    // Process.
	    provider = getProvider(sUser, sPassword);
	    vcNames = getDbNames(provider);

	    sbResult.append("  <cqDBs>\n");
	    for (int i = 0; i < vcNames.size(); i++) {
		sFound = (String) vcNames.get(i);
		sFound = sFound.replace(DATABASE_PREFIX, "");
		sbResult.append("    <cqDB>" + sFound + "</cqDB>\n");
	    }
	    sbResult.append("  </cqDBs>\n");

	    // Release the resource.
	    if (provider != null) {
		provider.terminate();
	    }

	    // Finish the measurements collection.
	    xCqUsageMeasurement.record();

	} catch (Throwable t) {
	    StringWriter sw = new StringWriter();
	    t.printStackTrace(new PrintWriter(sw, true));
	    try {
		sError = cqUtil.filterXMLReservedChars(sw.toString());
	    } catch (Exception ignore) {
		sError = sw.toString();
	    }
	} finally {
	    provider = null;
	    vcNames = null;
	    xCqUsageMeasurement = null;
	    xCqClientAuthorization = null;
	}
	sbResult.append("  <error>\n");
	sbResult.append("    <description>" + sError + "</description>\n");
	sbResult.append("  </error>\n");
	sbResult.append("</CqListDBs>\n");
	return sbResult.toString();
    }

    /**
     * Returns the collection of the database/schema names as string.
     * 
     * @param provider
     *            StpTeamProvider Team API object.
     * @return
     * @throws Throwable
     */
    private Vector getDbNames(CqProvider provider) throws Throwable {

	Vector vcNames = null;
	ResourceList databases = null;
	Iterator iter = null;
	String sName = null;

	try {
	    databases = provider.doGetDbSetList(null);
	    vcNames = new Vector();
	    iter = databases.iterator();

	    while (iter.hasNext()) {
		sName = iter.next().toString();
		vcNames.add(sName);
	    }

	} catch (Throwable t) {
	    throw (t);
	} finally {
	    databases = null;
	    iter = null;
	}
	return vcNames;
    }

}
