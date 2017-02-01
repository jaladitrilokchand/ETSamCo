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

public class cqFetchService extends cqService {

    /**
     * For unit testing.
     * 
     * @param args
     */
    public static void main(String[] args) {

	String sResult = null;
	StringBuffer sbResults = null;
	StringBuffer sbXMLRecord = null;
	cqFetchService xCqFetchService = null;
	cqFetchOutput xCqFetchOutput = null;
	try {

	    sbXMLRecord = cqUtil.getFileContent(cqFetchService.class
		    .getResource(cqUtil.sFETCH_XML));

	    xCqFetchService = new cqFetchService();
	    sResult = xCqFetchService.fetch(sbXMLRecord);
	    System.out.println(sResult);
	    xCqFetchOutput = new cqFetchOutput(sResult);
	    sbResults = xCqFetchOutput.getResultsAsDelimited(",", true);
	    System.out.println("\nResult as Delimited:\n"
		    + sbResults.toString());
	    // cqUtil.write2File("/tmp/test.csv", sbResults, true);

	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    xCqFetchService = null;
	    xCqFetchOutput = null;
	}
    }

    /**
     * This is the method that talks to ClearQuest via Rational Team API to
     * fetch a record. This implementation fetch relies on execution of a
     * predefined query.
     * 
     * @param sbXMLRecord
     *            An XML input that has all the necessary information to create
     *            a ClearQuest issue. For an example of input XML, please see
     *            cqFetchExample.xml file and its corresponding XSD schema file.
     * @return XML formatted result of the action. Any errors/exception will be
     *         inside of the returned XML formatted string.
     */
    public String fetch(StringBuffer sbXMLInput) {
	cqQueryService xCqQueryService = new cqQueryService();
	return xCqQueryService.fetch(sbXMLInput);
    }
}
