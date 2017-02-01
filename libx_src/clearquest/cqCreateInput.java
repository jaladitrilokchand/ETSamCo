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
public class cqCreateInput extends cqInput {

    private cqUser xCqUser = null;
    private cqClient xCqClient = null;
    private cqRecord xCqRecord = null;

    /**
     * Constructor.
     * 
     * @throws Exception
     */
    public cqCreateInput() throws Exception {
    }

    /**
     * Consturctor.
     * 
     * @param cqUser
     *            cqUser object.
     * @param cqClient
     *            cqClient object.
     * @param cqRecord
     *            cqRecord object.
     * @throws Exception
     */
    public cqCreateInput(cqUser xCqUser, cqClient xCqClient, cqRecord xCqRecord)
	    throws Exception {
	this.xCqUser = xCqUser;
	this.xCqClient = xCqClient;
	this.xCqRecord = xCqRecord;
    }

    /**
     * Constructor. Reads an XML input file to construct the object.
     * 
     * @param sbXMLRecord
     * @throws Exception
     */
    public cqCreateInput(StringBuffer sbXMLRecord) throws Exception {
	unmarshall(sbXMLRecord);
    }

    /**
     * Retuns cqClient object.
     * 
     * @return
     */
    public cqClient getCqClient() {
	return xCqClient;
    }

    /**
     * Returns cqRecord object
     * 
     * @return
     */
    public cqRecord getCqRecord() {
	return xCqRecord;
    }

    /**
     * Returns cqUser object
     * 
     * @return
     */
    public cqUser getCqUser() {
	return xCqUser;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.stg.iipmds.icof.component.clearquest.cqInput#marshal()
     */
    public String marshall() throws Exception {
	StringBuffer sbReturn = null;

	sbReturn = new StringBuffer();
	sbReturn.append("<CqCreate>\n");
	sbReturn.append("	<login>\n");
	sbReturn.append("		<username>" + getCqUser().getUsername()
		+ "</username>\n");
	sbReturn.append("		<password>" + getCqUser().getPassword()
		+ "</password>\n");
	sbReturn.append("		<database>" + getCqUser().getDatabase()
		+ "</database>\n");
	sbReturn.append("	</login>\n");
	sbReturn.append("	<client>\n");
	sbReturn.append("		<app-name>" + getCqClient().getClientName()
		+ "</app-name>\n");
	sbReturn.append(" </client>\n");
	sbReturn.append(getCqRecord().getXmlRepresentation() + "\n");
	sbReturn.append("</CqCreate>\n");

	return sbReturn.toString();
    }

    /**
     * Sets cqClient object.
     * 
     * @param xCqClient
     */
    private void setCqClient(cqClient xCqClient) {
	this.xCqClient = xCqClient;
    }

    /**
     * Sets cqRecord object.
     * 
     * @param xCqRecord
     */
    private void setCqRecord(cqRecord xCqRecord) {
	this.xCqRecord = xCqRecord;
    }

    /**
     * Sets cqUser object.
     * 
     * @param xCqUser
     */
    private void setCqUser(cqUser xCqUser) {
	this.xCqUser = xCqUser;
    }

    /**
     * Unmarshalls the given XML into this object.
     * 
     * @param sbXMLRecord
     *            XML to unmarshall.
     * @throws Exception
     */
    private void unmarshall(StringBuffer sbXMLRecord) throws Exception {

	// Validate the XML content first.
	cqUtil.validateXML(sbXMLRecord, cqUtil.sCREATE_XSD);

	// Get the factory
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

	// Using factory get an instance of document builder.
	DocumentBuilder db = dbf.newDocumentBuilder();

	// Parse using builder to get DOM representation of the XML file.
	Document dom = db.parse(new InputSource(new StringReader(sbXMLRecord
		.toString())));

	// Get the root element
	Element docEle = dom.getDocumentElement();

	// Get a nodelist of <login> elements
	Element el = null;
	NodeList nl = docEle.getElementsByTagName(cqUtil.LOGIN);
	if (nl != null && nl.getLength() > 0) {
	    for (int i = 0; i < nl.getLength(); i++) {

		// Set the user object.
		el = (Element) nl.item(i);
		cqUser xCqUser = new cqUser(cqUtil.getValueAsText(el,
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
		cqClient xCqClient = new cqClient(cqUtil.getValueAsText(el,
			cqUtil.APP_NAME));
		setCqClient(xCqClient);

	    }

	    // Get a nodelist of <record> elements
	    nl = docEle.getElementsByTagName(cqUtil.RECORD);
	    if (nl != null && nl.getLength() > 0) {
		for (int i = 0; i < nl.getLength(); i++) {

		    // Set the record object.
		    el = (Element) nl.item(i);
		    cqRecord xCqRecord = new cqRecord(cqUtil
			    .getNodeNameAndValues(el));
		    setCqRecord(xCqRecord);
		}
	    }
	}
    }
}
