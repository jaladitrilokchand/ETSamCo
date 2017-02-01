/**
 * <pre>
 * =============================================================================
 * 
 *  Copyright: (C) IBM Corporation 2009 - 2011 -- IBM Internal Use Only
 * 
 * =============================================================================
 * 
 *  CREATOR: Aydin Suren
 * 
 * -PURPOSE---------------------------------------------------------------------
 *  A utility class for communicating with PSO web services.
 * -----------------------------------------------------------------------------
 * 
 * 
 * -CHANGE LOG------------------------------------------------------------------
 *  03/02/2009 AS  Initial coding.
 *  02/21/2011 KKW Updated to user constant USER_HOME_PROPERTY_TAG when
 *                 retrieving user.home java property.
 * =============================================================================
 * </pre>
 */
package com.ibm.stg.eda.component.common;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.PropertyResourceBundle;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.md.mdApplicationContext;
import com.ibm.md.webservice.client.mdQueryClient;
import com.ibm.md.webservice.util.mdServiceMessage;
import com.ibm.md.webservice.util.mdServiceResponse;
import com.ibm.md.webservice.util.mdServiceResultSet;

public class IcofPSOClientUtil implements Serializable {

    private static final String APP_NAME = "PSOWebserviceClientUtil";

    /** The production PSO web server URL. */
    public static final String PSO_PROD_SERVER_URL = "https://pso.btv.ibm.com";

    /** PSO Web Service XML Date and Time format. */
    public static final String PSO_XML_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    /**
     * Authenticate the user to the PSO server.
     * 
     * @param psoServerName
     *            PSO server to connect.
     * @param appName
     *            Client application name.
     * @param appVersion
     *            Client application version.
     * @return The authenticated context or null if the login failed.
     * @exception IcofException
     *                Runtime exception.
     */
    public static mdApplicationContext authenticate(String psoServerName,
	    String appName, String appVersion) throws IcofException {

	mdApplicationContext xContext = null;

	try {
	    // Use default PSO server if it is not specified.
	    if (IcofStringUtil.isEmpty(psoServerName)) {
		psoServerName = PSO_PROD_SERVER_URL;
	    }

	    // Read the username and password information.
	    String[] credentials = readPasswordFile();

	    // Authenticate to the server.
	    if (credentials != null) {

		xContext = new mdApplicationContext(psoServerName,
			credentials[0], credentials[1], appName, appVersion);

		// Check for a login failure.
		if (!xContext.hasAuthenticated()) {
		    System.out.println("Failed: Login - "
			    + xContext.getLoginMessage());
		    xContext = null;
		}
	    }

	} catch (Throwable t) {
	    // Since we are calling an external interface, make sure to catch
	    // anything thrown.
	    throw new IcofException(APP_NAME, "authenticate",
		    IcofException.SEVERE,
		    "Failed to authenticate to PSO server " + psoServerName, t
			    .getMessage());
	}

	return xContext;
    }

    /**
     * Read the username/password from $HOME/.adb-application file. $HOME is
     * defined by the OS.<br />
     * Format of the file:<br />
     * username <intranet user id><br />
     * password <intranet password><br />
     * 
     * @return A string array with the username as the first element and
     *         password as the second.
     * @exception IcofException
     *                Trouble reading the file.
     */
    private static String[] readPasswordFile() throws IcofException {
	String[] sReturn = null;

	String username = null;
	String password = null;
	FileInputStream inputStream = null;

	try {

	    // Build the filename to read.
	    String filename = System.getProperty(Constants.USER_HOME_PROPERTY_TAG)
		    + System.getProperty("file.separator") + ".adb-application";

	    // Open the file.
	    IcofFile passwordFile = new IcofFile(filename, false);
	    if (passwordFile.exists()) {

		inputStream = new FileInputStream(passwordFile);
		PropertyResourceBundle prb = new PropertyResourceBundle(
			inputStream);
		if (prb == null) {
		    throw new IcofException("ERROR: Unable to Open "
			    + passwordFile, IcofException.SEVERE);
		}

		username = prb.getString("username");
		if (username == null) {
		    throw new IcofException(
			    "ERROR: Element 'username' Missing from "
				    + passwordFile, IcofException.SEVERE);
		} else {
		    username = username.trim();
		}

		password = prb.getString("password");
		if (password == null) {
		    throw new IcofException(
			    "ERROR: Element 'password' Missing from "
				    + passwordFile, IcofException.SEVERE);
		} else {
		    password = password.trim();
		}
	    }

	    sReturn = new String[2];
	    sReturn[0] = username;
	    sReturn[1] = password;

	} catch (Exception e) {
	    throw new IcofException(e.getMessage(), IcofException.SEVERE);
	} finally {
	    try {
		if (inputStream != null) {
		    inputStream.close();
		}
	    } catch (Exception e2) {
		throw new IcofException(
			"Failed to close InputStream in readPasswordFile "
				+ e2.getMessage(), IcofException.SEVERE);
	    }
	}

	return sReturn;
    }

    /**
     * Read the username from $HOME/.adb-application file. $HOME is defined by
     * the OS.<br />
     * Format of the file:<br />
     * username <intranet user id><br />
     * password <intranet password><br />
     * 
     * @return Username.
     * @exception IcofException
     *                Trouble reading the file.
     */
    public static String readIntranetID() throws IcofException {
	return readPasswordFile()[0];
    }

    /**
     * Reads the child element values and returns the text node values in a
     * hashmap.
     * 
     * @param eleParent
     *            The parent element to read.
     * @return A hashmap containing the child element values.
     * @exception IcofException
     *                Runtime exception.
     */
    public static HashMap hashElementChildValues(Element eleParent)
	    throws IcofException {

	HashMap hmReturn = new HashMap();

	String nodeName = "<init>";
	try {

	    NodeList nlChildren = eleParent.getChildNodes();
	    for (int i = 0; i < nlChildren.getLength(); i++) {
		Node nChild = nlChildren.item(i);

		// Only parse out nested element nodes.
		if (nChild.getNodeType() == Node.ELEMENT_NODE) {
		    nodeName = nChild.getNodeName();
		    int iSplit = nodeName.indexOf(":");
		    if (iSplit > -1) {
			nodeName = nodeName.substring(iSplit + 1);
		    }

		    // Save the text node.
		    Node nText = nChild.getFirstChild();
		    if (nText != null) {

			if (nText.getNodeType() == Node.TEXT_NODE) {
			    hmReturn.put(nodeName, nText.getNodeValue());
			}
		    }
		}
	    }

	} catch (Throwable t) {
	    throw new IcofException(t.getMessage() + "\nnodeName=" + nodeName,
		    IcofException.SEVERE);
	} finally {
	}

	return hmReturn;
    }

    /**
     * Reads the error message from an mdServiceResponse object.
     * 
     * @param xContext
     *            An mdApplicationContext context.
     * @param xResponse
     *            mdServiceResponse object to read.
     * 
     * @return Error message as StringBuffer.
     */
    public static StringBuffer getPSOQueryErrorMsg(
	    mdApplicationContext xContext, mdServiceResponse xResponse) {

	StringBuffer sbError = new StringBuffer();

	try {
	    Vector vcProblem = xResponse.getMessageSet(xContext);
	    for (int i = 0; i < vcProblem.size(); i++) {
		mdServiceMessage xServiceMessage = (mdServiceMessage) vcProblem
			.get(i);
		sbError.append(xServiceMessage.getCode(xContext) + " - "
			+ xServiceMessage.getText(xContext) + "\n");
	    }
	} catch (Exception e) {
	    sbError
		    .append("Falied to get the PSO error message in getPSOQueryErrorMsg");
	}
	return sbError;
    }

    /**
     * Runs the query against to PSO and then returns the response as collection
     * of PSO objects.
     * 
     * @param xContext
     *            The users context.
     * @param psoObjectType
     *            The PSO object that will be fetched.
     * @param lastUpdate
     *            If given, it will be used for getting the objects that are
     *            changed since then. The format is like
     *            "2001-01-01T01:00:00.000".
     * @param appName
     *            Calling application name.
     * @param appVersion
     *            Calling application version.
     * @param debug
     *            It will print object XML data to System.out.
     * @return The webservice response. Elements are raw XMl data as Document
     *         objects.
     * @exception IcofException
     *                Trouble accessing the query webservice.
     */
    public static Vector queryObject(mdApplicationContext xContext,
	    String psoObjectType, String lastUpdate, String appName,
	    String appVersion, boolean debug) throws IcofException {

	// Read each object.
	Vector vcObjects = new Vector();
	try {
	    // Create a query client.
	    mdServiceResponse xResponse = null;
	    mdQueryClient xQuery = new mdQueryClient();
	    xQuery.setApplicationName(xContext, appName);
	    xQuery.setApplicationVersion(xContext, appVersion);
	    xQuery.setQueryType(xContext, psoObjectType);
	    xQuery.setFetchLimit(xContext, 0);

	    // Add last update restriction if given.
	    if (!IcofStringUtil.isEmpty(lastUpdate)) {
		xQuery.addQueryAttribute(xContext,
			"mdBusinessObject_LastUpdate", "GE", lastUpdate);
	    }

	    // Send the query to the server.
	    xResponse = xQuery.execute(xContext);
	    if (!xResponse.isSuccessful(xContext)) {
		xResponse.printMessages(xContext);
		throw new Exception("PSO Query Service Failed");
	    }

	    // Process returned objects.
	    mdServiceResultSet xResultSet = xResponse.getResultSet(xContext);
	    Vector vcRows = xResultSet.getRows(xContext);
	    if (debug) {
		xResultSet.output(xContext, System.out, true);
	    }

	    boolean bError = false;
	    for (int i = 0; i < vcRows.size() && !bError; i++) {
		String[] oRow = (String[]) vcRows.elementAt(i);
		String objID = oRow[0];

		// Query for this object.
		xQuery = new mdQueryClient();
		xQuery.setApplicationName(xContext, appName);
		xQuery.setApplicationVersion(xContext, appVersion);
		xQuery.setFetchObjects(xContext, true);
		xQuery.addQueryObjectID(xContext, objID);
		xResponse = xQuery.execute(xContext);

		// Process the response.
		if (!xResponse.isSuccessful(xContext)) {
		    throw new Exception("Querying PSO for object ID = " + objID
			    + " failed:\n"
			    + getPSOQueryErrorMsg(xContext, xResponse));
		}

		// Parse the XML document and into the collection of raw
		// data.
		Vector vcObjectSet = xResponse.getObjectSet(xContext);
		if (vcObjectSet == null || vcObjectSet.size() == 0) {
		    throw new IcofException(
			    "No data is returned from PSO for object ID = "
				    + objID, IcofException.SEVERE);
		}
		vcObjects.add((Document) vcObjectSet.get(0));
	    }

	} catch (Throwable t) {
	    // Since we are calling an external interface, make sure to catch
	    // anything thrown.
	    throw new IcofException(t.getMessage(), IcofException.SEVERE);
	}
	return vcObjects;
    }

}
