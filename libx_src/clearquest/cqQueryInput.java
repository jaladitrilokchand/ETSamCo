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
 * Class definition to process the XML input.
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

import java.io.StringReader;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author asuren
 * 
 */
public class cqQueryInput extends cqInput {

    private cqUser xCqUser = null;
    private cqClient xCqClient = null;
    private cqQuery xCqQuery = null;

    /**
     * Constructor.
     * 
     * @param xCqUser
     * @param xCqClient
     * @param xCqQuery
     * @throws Exception
     */
    public cqQueryInput(cqUser xCqUser, cqClient xCqClient, cqQuery xCqQuery)
	    throws Exception {
	this.xCqUser = xCqUser;
	this.xCqClient = xCqClient;
	this.xCqQuery = xCqQuery;
    }

    /**
     * Constructor. Builds an object based on the given information in the XML.
     * 
     * @param sbXMLRecord
     * @throws Exception
     */
    public cqQueryInput(StringBuffer sbXMLRecord) throws Exception {
	unmarshall(sbXMLRecord);
    }

    /**
     * Returns the object.
     * 
     * @return
     */
    public cqClient getCqClient() {
	return xCqClient;
    }

    /**
     * Returns the object.
     * 
     * @return
     */
    public cqQuery getCqQuery() {
	return xCqQuery;
    }

    /**
     * Returns the object.
     * 
     * @return
     */
    public cqUser getCqUser() {
	return xCqUser;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.stg.iipmds.icof.component.clearquest.cqInput#marshall()
     */
    public String marshall() throws Exception {
	StringBuffer sbReturn = null;

	try {
	    sbReturn = new StringBuffer();
	    sbReturn.append("<CqQuery>\n");
	    sbReturn.append("	<login>\n");
	    sbReturn.append("		<username>" + getCqUser().getUsername()
		    + "</username>\n");
	    sbReturn.append("		<password>" + getCqUser().getPassword()
		    + "</password>\n");
	    sbReturn.append("		<database>" + getCqUser().getDatabase()
		    + "</database>\n");
	    sbReturn.append("		<schema>" + getCqUser().getSchema()
		    + "</schema>\n");
	    sbReturn.append("	</login>\n");
	    sbReturn.append("	<client>\n");
	    sbReturn.append("		<app-name>" + getCqClient().getClientName()
		    + "</app-name>\n");
	    sbReturn.append(" </client>\n");
	    sbReturn.append(" <query>\n");
	    sbReturn
		    .append("    <query-name>"
			    + cqUtil.filterXMLReservedChars(getCqQuery()
				    .getQueryName()) + "</query-name>\n");
	    sbReturn.append(" </query>\n");
	    sbReturn.append("</CqQuery>\n");
	} catch (Exception e) {
	    throw (e);
	} finally {
	}

	return sbReturn.toString();
    }

    /**
     * Sets the local variable.
     * 
     * @param xCqClient
     */
    private void setCqClient(cqClient xCqClient) {
	this.xCqClient = xCqClient;
    }

    /**
     * Sets the local variable.
     * 
     * @param xCqQuery
     */
    private void setCqQuery(cqQuery xCqQuery) {
	this.xCqQuery = xCqQuery;
    }

    /**
     * Sets the local variable.
     * 
     * @param xCqUser
     */
    private void setCqUser(cqUser xCqUser) {
	this.xCqUser = xCqUser;
    }

    /**
     * Translates an XML to the object.
     * 
     * @param sbXMLRecord
     * @throws Exception
     */
    private void unmarshall(StringBuffer sbXMLRecord) throws Exception {

	DocumentBuilderFactory dbf = null;
	DocumentBuilder db = null;
	Document dom = null;
	Element docEle = null;
	Element el = null;
	Element elFilters = null;
	NodeList nl = null;
	cqUser xCqUser = null;
	cqClient xCqClient = null;
	cqQuery xCqQuery = null;
	Vector vcFilter = null;
	String[] saFilterAttributeNames = null;

	try {

	    // Validate the XML content first.
	    cqUtil.validateXML(sbXMLRecord, cqUtil.sQUERY_XSD);

	    // Get the factory
	    dbf = DocumentBuilderFactory.newInstance();

	    // Using factory get an instance of document builder.
	    db = dbf.newDocumentBuilder();

	    // Parse using builder to get DOM representation of the XML file.
	    dom = db.parse(new InputSource(new StringReader(sbXMLRecord
		    .toString())));

	    // Get the root elememt
	    docEle = dom.getDocumentElement();

	    // Get a nodelist of <login> elements
	    nl = docEle.getElementsByTagName(cqUtil.LOGIN);
	    if (nl != null && nl.getLength() > 0) {
		for (int i = 0; i < nl.getLength(); i++) {

		    // Set the user object.
		    el = (Element) nl.item(i);
		    xCqUser = new cqUser(cqUtil.getValueAsText(el,
			    cqUtil.USERNAME), cqUtil.getValueAsText(el,
			    cqUtil.PASSWORD), cqUtil.getValueAsText(el,
			    cqUtil.DATABASE), cqUtil.getValueAsText(el,
			    cqUtil.SCHEMA));
		    setCqUser(xCqUser);

		}
	    }

	    // Get a nodelist of <client> elements
	    nl = docEle.getElementsByTagName(cqUtil.CLIENT);
	    if (nl != null && nl.getLength() > 0) {
		for (int i = 0; i < nl.getLength(); i++) {

		    // Set the client object.
		    el = (Element) nl.item(i);
		    xCqClient = new cqClient(cqUtil.getValueAsText(el,
			    cqUtil.APP_NAME));
		    setCqClient(xCqClient);

		}
	    }

	    // Get a nodelist of <query> elements
	    String sQueryName = null;
	    nl = docEle.getElementsByTagName(cqUtil.QUERY);
	    if (nl != null && nl.getLength() > 0) {
		saFilterAttributeNames = new String[3];
		saFilterAttributeNames[0] = cqUtil.NAME;
		saFilterAttributeNames[1] = cqUtil.OPERATOR;
		saFilterAttributeNames[2] = cqUtil.VALUE;

		for (int i = 0; i < nl.getLength(); i++) {

		    // Set the client object.
		    el = (Element) nl.item(i);
		    sQueryName = cqUtil.getValueAsText(el, cqUtil.QUERY_NAME);

		    // Get the dynamic filters.
		    elFilters = (Element) el.getElementsByTagName(
			    cqUtil.FILTERS).item(0);
		    vcFilter = cqUtil.getAttributesAsText(elFilters,
			    cqUtil.FILTER, saFilterAttributeNames);
		    /*
		     * For ddebug. for (int j=0; j < vcFilter.size(); j++) {
		     * saFilterAttributeValues = (String[]) vcFilter.get(j);
		     * System
		     * .out.println("sFilterName="+saFilterAttributeValues[0]);
		     * System
		     * .out.println("sFilterOperator="+saFilterAttributeValues
		     * [1]);
		     * System.out.println("sFilterValue="+saFilterAttributeValues
		     * [2]); }
		     */
		    xCqQuery = new cqQuery(sQueryName, vcFilter);
		    setCqQuery(xCqQuery);

		}
	    }

	} catch (Exception e) {
	    throw (e);
	} finally {
	    dbf = null;
	    db = null;
	    dom = null;
	    docEle = null;
	    el = null;
	    elFilters = null;
	    nl = null;
	    xCqUser = null;
	    xCqClient = null;
	    vcFilter = null;
	    saFilterAttributeNames = null;
	}
    }
}
