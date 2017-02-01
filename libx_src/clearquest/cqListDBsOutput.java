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
public class cqListDBsOutput extends cqOutput {

	private String _sError = null;
	private Object[] _oDBs = null;

	/**
     * Constructor. Builds the object based on the information in the given XML.
     * 
	 * @param sXML
	 * @throws Exception
	 */
	public cqListDBsOutput(String sXML) throws Exception {
		super();
		this.unmarshall(new StringBuffer(sXML));
	}

	/**
     * Constructor. Builds the object based on the information in the given XML.
     * 
	 * @param sbXML
	 * @throws Exception
	 */
	public cqListDBsOutput(StringBuffer sbXML) throws Exception {
		super();
		this.unmarshall(sbXML);
	}

	/**
     * Returns the error.
     * 
	 * @return
	 * @throws Exception
	 */
	public String getError() throws Exception {
		return _sError;
	}

	/**
     * Returns a delimited text version of this object.
     * 
	 * @param sDelimiter
	 * @param bQuotes
	 * @return
	 * @throws Exception
	 */
	public StringBuffer getResultsAsDelimited(String sDelimiter, boolean bQuotes)
			throws Exception {
		StringBuffer sbReturn = null;
		try {
			sbReturn = new StringBuffer();

			// Add the header.
			if (bQuotes) {
				sbReturn.append("\"database\"" + sDelimiter + "\n");
			} else {
				sbReturn.append("database" + sDelimiter + "\n");
			}

			// Add the content.
			for (int i = 0; i < _oDBs.length; i++) {
				if (bQuotes) {
					sbReturn.append("\"" + (String) _oDBs[i] + "\""
							+ sDelimiter + "\n");
				} else {
					sbReturn.append((String) _oDBs[i] + sDelimiter + "\n");
				}
			}

		} catch (Exception e) {
			throw (e);
		} finally {
		}
		return sbReturn;
	}

	/**
     * Translates an XML to the object.
     * 
	 * @param sbXML
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

			// Get a nodelist of <cqDBs> elements
			nl = docEle.getElementsByTagName(cqUtil.CQDBS);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					el = (Element) nl.item(i);
					_oDBs = cqUtil.getValuesAsText(el, cqUtil.CQDB);
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
