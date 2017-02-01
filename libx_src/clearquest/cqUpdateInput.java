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
 *     
 *-PURPOSE----------------------------------------------------------------------------------------
 * Class definition to process the XML input.
 *------------------------------------------------------------------------------------------------------
 *
 * =============================================================================
 *
 * -CHANGE LOG------------------------------------------------------------------
 * 06/17/2009 AS  Initial coding.
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
public class cqUpdateInput extends cqInput {

    private cqUser xCqUser = null;
    private cqClient xCqClient = null;
    private cqUpdate xCqUpdate = null;

    /**
     * Constructor.
     * 
     * @throws Exception
     */
    public cqUpdateInput() throws Exception {
    }

    /**
     * Constructor.
     * 
     * @param cqUser
     *            cqUser object.
     * @param cqClient
     *            cqClient object.
     * @param cqRecord
     *            cqRecord object.
     * @throws Exception
     */
    public cqUpdateInput(cqUser xCqUser, cqClient xCqClient, cqUpdate xCqUpdate)
	    throws Exception {
	this.xCqUser = xCqUser;
	this.xCqClient = xCqClient;
	this.xCqUpdate = xCqUpdate;
    }

    /**
     * Constructor. Reads an XML input file to construct the object.
     * 
     * @param sbXMLRecord
     * @throws Exception
     */
    public cqUpdateInput(StringBuffer sbXMLRecord) throws Exception {
	unmarshall(sbXMLRecord);
    }

    /**
     * Returns cqClient object.
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
    public cqUpdate getCqUpdate() {
	return xCqUpdate;
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
     * Turns the object to XML format
     * 
     * @see com.ibm.stg.iipmds.icof.component.clearquest.cqInput#marshal()
     */
    public String marshall() throws Exception {
	StringBuffer sbReturn = null;

	try {
	    sbReturn = new StringBuffer();
	    sbReturn.append("<CqUpdate>\n");
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

	    sbReturn.append(" <update>\n");
	    sbReturn.append("   <record_type>\n"
		    + getCqUpdate().getRecordType());
	    sbReturn.append("   </record_type>\n");
	    sbReturn.append("   <record_id>\n" + getCqUpdate().getRecordID());
	    sbReturn.append("   </record_id>\n");
	    sbReturn.append("   <action>\n" + getCqUpdate().getAction());
	    sbReturn.append("   </action>\n");

	    sbReturn.append("   <fields>\n");
	    Vector vcField = getCqUpdate().getFields();
	    if (vcField != null && vcField.size() > 0) {
		for (int i = 0; i < vcField.size(); i++) {
		    cqField xField = (cqField) vcField.get(i);
		    sbReturn.append("     <field name=\"" + xField.getName()
			    + "\" value=\"" + xField.getValue() + "\" />\n");
		}
	    }
	    sbReturn.append("   </fields>\n");
	    sbReturn.append(" </update>\n");
	    sbReturn.append("</CqUpdate>\n");

	} catch (Exception e) {
	    throw (e);
	} finally {
	}

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
     * @param xCqUpdate
     */
    private void setCqUpdate(cqUpdate xCqUpdate) {
	this.xCqUpdate = xCqUpdate;
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
	cqUtil.validateXML(sbXMLRecord, cqUtil.sUPDATE_XSD);

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
		xCqClient = new cqClient(cqUtil.getValueAsText(el,
			cqUtil.APP_NAME));
		setCqClient(xCqClient);

	    }

	    // Get a nodelist of <update> elements
	    String sRecordType = null;
	    String sRecordID = null;
	    String sAction = null;
	    nl = docEle.getElementsByTagName(cqUtil.UPDATE);
	    if (nl != null && nl.getLength() > 0) {
		for (int i = 0; i < nl.getLength(); i++) {

		    // Set the update object.
		    el = (Element) nl.item(i);
		    sRecordType = cqUtil.getValueAsText(el, cqUtil.RECORD_TYPE);
		    sRecordID = cqUtil.getValueAsText(el, cqUtil.RECORD_ID);
		    sAction = cqUtil.getValueAsText(el, cqUtil.ACTION);

		    // Get the fields.
		    Vector vcField = null;
		    Element elFields = (Element) el.getElementsByTagName(
			    cqUtil.FIELDS).item(0);
		    if (elFields != null && elFields.hasChildNodes()) {
			NodeList nlField = elFields
				.getElementsByTagName(cqUtil.FIELD);
			if (nlField != null && nlField.getLength() > 0) {
			    vcField = new Vector();
			    for (int j = 0; j < nlField.getLength(); j++) {
				Element elInner = (Element) nlField.item(j);
				String name = elInner.getAttribute(cqUtil.NAME);
				String value = elInner
					.getAttribute(cqUtil.VALUE);
				vcField.add(new cqField(name, value));
			    }
			}
		    }
		    xCqUpdate = new cqUpdate(sRecordType, sRecordID, sAction,
			    vcField);
		    setCqUpdate(xCqUpdate);

		}
	    }
	}
    }
}
