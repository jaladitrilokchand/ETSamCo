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
public class cqQueryOutput extends cqOutput {

	private String _sError = null;
	private String _sQuery = null;
	private Object[] _oQuery = null;
	private Vector _vcFieldLabels = null;
	private Vector _vcResults = null;

	/**
     * Constructor. Builds the object based on the information in the given XML.
     * 
	 * @param sXML
	 * @throws Exception
	 */
	public cqQueryOutput(String sXML) throws Exception {
		super();
		this.unmarshall(new StringBuffer(sXML));
	}

	/**
     * Constructor. Builds the object based on the information in the given XML.
     * 
	 * @param sbXML
	 * @throws Exception
	 */
	public cqQueryOutput(StringBuffer sbXML) throws Exception {
		super();
		this.unmarshall(sbXML);
	}

	/**
     * Returns a delimited text version of the all gueries results.
     * 
     * @param sDelimiter        Delimiter to use.
     * @param bQuotes           true if the fields need to be in quotes.
     * @return
     * @throws Exception
     */
	public StringBuffer getAllQueriesAsDelimited(String sDelimiter,
			boolean bQuotes) throws Exception {
		StringBuffer sbReturn = new StringBuffer();
		try {

			if (_oQuery != null) {

				// Add the header.
				if (bQuotes) {
					sbReturn.append("\"query\"" + sDelimiter + "\n");
				} else {
					sbReturn.append("query" + sDelimiter + "\n");
				}

				// Add the content.
				for (int i = 0; i < _oQuery.length; i++) {
					if (bQuotes) {
						sbReturn.append("\"" + _oQuery[i] + "\"" + sDelimiter
								+ "\n");
					} else {
						sbReturn.append(_oQuery[i] + sDelimiter + "\n");
					}
				}
			}

		} catch (Exception e) {
			throw (e);
		} finally {
		}
		return sbReturn;
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
     * Reurns the collection of the field labels.
     * 
     * @return
     */
	public Vector getFieldLabels() {
		return _vcFieldLabels;
	}

    /**
     * Reurns the collection of the queries.
     * 
     * @return
     */
	public Object[] getQueries() throws Exception {
		return _oQuery;
	}

    /**
     * Reurns the query.
     * 
     * @return
     */
	public String getQuery() throws Exception {
		return _sQuery;
	}

     /**
     * Returns a delimited text version of the query results.
     * 
     * @param sDelimiter        Delimiter to use.
     * @param bQuotes           true if the fields need to be in quotes.
     * @return
     * @throws Exception
     */
	public StringBuffer getQueryResultsAsDelimited(String sDelimiter,
			boolean bQuotes) throws Exception {
		StringBuffer sbReturn = new StringBuffer();
		Object[] oValues = null;
		String[] saLabels = null;
		try {

			if (_vcFieldLabels != null && _vcResults != null) {

				// Add the header.
				for (int i = 0; i < _vcFieldLabels.size(); i++) {
					saLabels = (String[]) _vcFieldLabels.get(i);
					if (bQuotes) {
						sbReturn.append("\"" + saLabels[0] + "\"" + sDelimiter);
					} else {
						sbReturn.append(saLabels[0] + sDelimiter);
					}
				}
				sbReturn.append("\n");

				// Add the content.
				for (int i = 0; i < _vcResults.size(); i++) {
					oValues = (Object[]) _vcResults.get(i);
					for (int j = 0; j < oValues.length; j++) {
						if (bQuotes) {
							sbReturn.append("\"" + (String) oValues[j] + "\""
									+ sDelimiter);
						} else {
							sbReturn.append((String) oValues[j] + sDelimiter);
						}
					}
					sbReturn.append("\n");
				}
			}

		} catch (Exception e) {
			throw (e);
		} finally {
			oValues = null;
			saLabels = null;
		}
		return sbReturn;
	}

    /**
     * Returns the collection of the result array objects. The array object
     * contains the values of an XML tag.
     * 
     * @return
     */
	public Vector getResultObjects() {
		return _vcResults;
	}

    /**
     * Translates an XML to the object.
     * 
     * @param sbXML     XML input to parse.
     * @throws Exception
     */
	private void unmarshall(StringBuffer sbXML) throws Exception {

		DocumentBuilderFactory dbf = null;
		DocumentBuilder db = null;
		Document dom = null;
		Element docEle = null;
		Element el = null;
		Element el2 = null;
		NodeList nl = null;
		NodeList nl2 = null;
		String[] saAttribute = null;

		try {

			// Get the factory
			dbf = DocumentBuilderFactory.newInstance();

			// Using factory get an instance of document builder.
			db = dbf.newDocumentBuilder();

			// Parse using builder to get DOM representation of the XML file.
			dom = db.parse(new InputSource(new StringReader(sbXML.toString())));

			// Get the root element
			docEle = dom.getDocumentElement();

			// Get the error.
			_sError = getError(docEle);
			if (_sError != null && !_sError.equalsIgnoreCase(cqUtil.NULL)) {
				throw new Exception(cqUtil.OUTPUT_PARSE_ERROR_MSG + _sError);
			}

			// Get a nodelist of <queries> elements (Used with the output of the
			// all queries)
			nl = docEle.getElementsByTagName(cqUtil.QUERIES);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					el = (Element) nl.item(i);
					_oQuery = cqUtil.getValuesAsText(el, cqUtil.QUERY);
				}
			}

			// Get a nodelist of <query> elements
			nl = docEle.getElementsByTagName(cqUtil.QUERY);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					el = (Element) nl.item(i);
					_sQuery = cqUtil.getValueAsText(el, cqUtil.QUERY_NAME);
				}
			}

			// Get a nodelist of <results> elements (Used with the output of a
			// single query execution)
			_vcResults = new Vector();
			nl = docEle.getElementsByTagName(cqUtil.RESULTS);
			boolean bGotFieldLabels = false;
			if (nl != null && nl.getLength() > 0) {
				saAttribute = new String[1];
				saAttribute[0] = cqUtil.NAME;
				for (int i = 0; i < nl.getLength(); i++) {

					// Get each result.
					el = (Element) nl.item(i);
					nl2 = el.getElementsByTagName(cqUtil.RESULT);
					if (nl2 != null && nl2.getLength() > 0) {
						for (int j = 0; j < nl2.getLength(); j++) {
							el2 = (Element) nl2.item(j);
							_vcResults
								.add(cqUtil.getValuesAsText(el2, cqUtil.FIELD));

							_oQuery = cqUtil.getValuesAsText(el, cqUtil.FIELD);

							if (!bGotFieldLabels) {
								_vcFieldLabels = cqUtil.getAttributesAsText(
										el2, "field", saAttribute);
								bGotFieldLabels = true;
							}
						}
					}
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
			el2 = null;
			nl = null;
			nl2 = null;
			saAttribute = null;
		}
	}
}
