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
* =============================================================================
*
* -CHANGE LOG------------------------------------------------------------------
* 03/01/2008 AS  Initial coding.
*
* =============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.clearquest;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author asuren
 *
 */
public abstract class cqOutput {

	/**
     * Extracts and returns the error description tag value.
     * 
	 * @param docEle   error-description tag to parse.
	 * @return
	 * @throws Exception
	 */
	protected String getError(Element docEle) throws Exception {

		String sError = null;
		Element el = null;
		NodeList nl = null;

		try {
			// Get a nodelist of <error> elements
			nl = docEle.getElementsByTagName(cqUtil.ERROR);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					el = (Element) nl.item(i);
					sError = cqUtil.getValueAsText(el, cqUtil.DESCRIPTION);
				}
			}
		} catch (Exception e) {
			throw (e);
		} finally {

		}
		return sError;
	}

	/**
     * Writes a given content (StringBuffer for efficiency) to a given file.
     * 
	 * @param sFile            Fully qualified filename.
	 * @param sbContent    Content to write.
	 * @param bDelete      Whether the fle should be deleted if exists.
	 * @throws Exception
	 */
	protected void write2File(String sFile, StringBuffer sbContent,
			boolean bDelete) throws Exception {
		cqUtil.write2File(sFile, sbContent, bDelete);
	}

}
