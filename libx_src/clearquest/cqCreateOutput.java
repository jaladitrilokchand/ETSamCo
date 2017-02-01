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
 * Class to proces the output XML.
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
public class cqCreateOutput extends cqOutput {

    private String _sError = null;
    private String _sId = null;

    /**
     * Constructor - Constructs one this object based on the given XML as
     * string.
     * 
     * @param sXML
     *            XML to read/unmarshall.
     * @throws Exception
     */
    public cqCreateOutput(String sXML) throws Exception {
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
    public cqCreateOutput(StringBuffer sbXML) throws Exception {
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
    public String getId() throws Exception {
	return _sId;
    }

    /**
     * Reads and parses an XML input.
     * 
     * @param sbXML
     *            XML inout to read.
     * @throws Exception
     */
    private void unmarshall(StringBuffer sbXML) throws Exception {

	DocumentBuilderFactory dbf = null;
	DocumentBuilder db = null;
	Document dom = null;
	Element docEle = null;
	Element el = null;
	NodeList nl = null;

	try {

	    // Get the factory
	    dbf = DocumentBuilderFactory.newInstance();

	    // Using factory get an instance of document builder.
	    db = dbf.newDocumentBuilder();

	    // Parse using builder to get DOM representation of the XML file.
	    dom = db.parse(new InputSource(new StringReader(sbXML.toString())));

	    // Get the root elememt
	    docEle = dom.getDocumentElement();

	    // Get the error.
	    _sError = getError(docEle);
	    if (!_sError.equalsIgnoreCase(cqUtil.NULL)) {
		throw new Exception(cqUtil.OUTPUT_PARSE_ERROR_MSG + _sError);
	    }

	    // Get a nodelist of <record> elements
	    nl = docEle.getElementsByTagName(cqUtil.RECORD);
	    if (nl != null && nl.getLength() > 0) {
		for (int i = 0; i < nl.getLength(); i++) {
		    el = (Element) nl.item(i);
		    _sId = cqUtil.getValueAsText(el, cqUtil.ID);
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
	    nl = null;
	}
    }

}
