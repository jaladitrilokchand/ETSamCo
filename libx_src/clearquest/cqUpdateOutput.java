/**
 * <pre>
 * =============================================================================
 *
 *  IBM Internal Use Only
 *
 * =============================================================================
 *
 *  CREATOR: Aydin Suren *-PURPOSE----------------------------------------------------------------------------------------
 * Class to process/read the output XML.
 *------------------------------------------------------------------------------------------------------
 *
 * =============================================================================
 *
 * -CHANGE LOG------------------------------------------------------------------
 * 06/26/2009 AS  Initial coding.
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
public class cqUpdateOutput extends cqOutput {

    private String _sError = null;
    private String _sMessage = null;

    /**
     * Constructor - Constructs one this object based on the given XML as
     * string.
     * 
     * @param sXML
     *            XML to read/unmarshall.
     * @throws Exception
     */
    public cqUpdateOutput(String sXML) throws Exception {
	super();
	this.unmarshall(new StringBuffer(sXML));
    }

    /**
     * Constructor - Constructs one this object based on the given XML as
     * StringBuffer.
     * 
     * @param sbXML
     * @throws Exception
     */
    public cqUpdateOutput(StringBuffer sbXML) throws Exception {
	super();
	this.unmarshall(sbXML);
    }

    /**
     * Returns the error string.
     * 
     * @return
     * @throws Exception
     */
    public String getError() throws Exception {
	return _sError;
    }

    /**
     * Returns the ID.
     * 
     * @return
     * @throws Exception
     */
    public String getMessage() throws Exception {
	return _sMessage;
    }

    /**
     * Reads and parses an XML input.
     * 
     * @param sbXML
     *            XML inout to read.
     * @throws Exception
     */
    private void unmarshall(StringBuffer sbXML) throws Exception {

	// Get the factory
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

	// Using factory get an instance of document builder.
	DocumentBuilder db = dbf.newDocumentBuilder();

	// Parse using builder to get DOM representation of the XML file.
	Document dom = db.parse(new InputSource(new StringReader(sbXML
		.toString())));

	// Get the root element
	Element docEle = dom.getDocumentElement();

	// Get the error.
	_sError = getError(docEle);
	if (!_sError.equalsIgnoreCase(cqUtil.NULL)) {
	    throw new Exception(cqUtil.OUTPUT_PARSE_ERROR_MSG + _sError);
	}

	// Get a nodelist of <record> elements
	NodeList nl = docEle.getElementsByTagName(cqUtil.UPDATE);
	if (nl != null && nl.getLength() > 0) {
	    for (int i = 0; i < nl.getLength(); i++) {
		Element el = (Element) nl.item(i);
		_sMessage = cqUtil.getValueAsText(el, "message");
	    }
	}

    }

}
