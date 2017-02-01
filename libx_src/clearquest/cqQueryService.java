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
 * 06/10/2008 AS  Changed the FETCH_QUERY constant to point to the new query.
 * 10/30/2008 AS  Added more properties to the QUERY_PROPERTIES constant.
 *
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.clearquest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.Date;

import javax.wvcm.WvcmException;
import javax.wvcm.PropertyNameList.PropertyName;
import javax.wvcm.PropertyRequestItem.PropertyRequest;

import com.ibm.rational.wvcm.stp.cq.CqQuery;
import com.ibm.rational.wvcm.stp.cq.CqResultSet;
import com.ibm.rational.wvcm.stp.cq.CqRowData;
import com.ibm.rational.wvcm.stp.cq.CqProvider;
import com.ibm.rational.wvcm.stp.cq.CqQuery.DisplayField;
import com.ibm.rational.wvcm.stp.cq.CqQuery.FilterLeaf;
import com.ibm.rational.wvcm.stp.cq.CqQuery.Filter.Operation;
import com.ibm.rational.wvcm.stp.cq.CqQuery.FilterLeaf.TargetType;
import com.ibm.rational.wvcm.stp.cq.CqUserDb;

/**
 * @author asuren
 * 
 */
public class cqQueryService extends cqService {

    private static final String QUERY_PREFIX = "cq.query:";
    private static final String FETCH_QUERY = "Public Queries/Administration/Web Services/Find Record";

    // Properties of the selected query that we will be using.
    private static final PropertyName[] QUERY_PROPERTIES = 
	    new PropertyName[] { CqQuery.ALL_PROPERTIES,
		    CqQuery.ALL_CUSTOM_PROPERTIES, CqQuery.FILTERING,
		    CqQuery.IS_AGGREGATED, CqQuery.IS_MULTITYPE,
		    CqQuery.IS_SQL_GENERATED, CqQuery.PRIMARY_RECORD_TYPE,
		    CqQuery.QUERY_TYPE, CqQuery.DISPLAY_NAME,
		    CqQuery.SQL, CqQuery.AUTHENTICATION_REALM,
		    CqQuery.CREATOR_GROUP_NAME, CqQuery.CREATOR_LOGIN_NAME,
		    CqQuery.INVALID_PROPERTIES, CqQuery.REPOSITORY,
		    CqQuery.STABLE_LOCATION, CqQuery.COMMENT,
		    CqQuery.CONTENT_TYPE, CqQuery.CONTENT_CHARACTER_SET,
		    CqQuery.CONTENT_IDENTIFIER, CqQuery.CONTENT_LANGUAGE,
		    CqQuery.CONTENT_LENGTH, CqQuery.CREATION_DATE,
		    CqQuery.CREATOR_DISPLAY_NAME,
		    CqQuery.LAST_MODIFIED, CqQuery.PARENT_LIST,
		    CqQuery.PROVIDER_LIST, CqQuery.RESOURCE_IDENTIFIER,
		    CqQuery.WORKSPACE_FOLDER_LIST,
		    CqQuery.IS_MODIFIED, CqQuery.DISPLAY_FIELDS,
		    CqQuery.DYNAMIC_FILTERS, CqQuery.USER_FRIENDLY_LOCATION };


    private String _sDatabase = null;
    private String _sUserQuery = null;
    private String _sSchema = null;

    /**
     * For unit testing.  This method is no longer in use.  Was used by 
     * Aydin when his dev box was also iipmdsdev.
     * 
     * @param args
     */
    public static void main(String[] args) {

	String sResult = null;
	StringBuffer sbResults = null;
	StringBuffer sbXMLRecord = null;
	cqQueryService xCqQueryService = null;
	cqQueryOutput xCqQueryOutput = null;
	try {

	    xCqQueryService = new cqQueryService();

	    /*
	     * // Getting all the queries. sbXMLRecord =
	     * cqUtil.getFileContent(cqQueryService
	     * .class.getResource(cqUtil.sQUERY_XML)); sResult =
	     * xCqQueryService.getAllQueries(sbXMLRecord);
	     * System.out.println(sResult); xCqQueryOutput = new
	     * cqQueryOutput(sResult); sbResults =
	     * xCqQueryOutput.getAllQueriesAsDelimited(",", true);
	     * System.out.println("\nResult as Delimited:\n"+
	     * sbResults.toString());
	     */

	    // Execure user's query.
	    sbXMLRecord = cqUtil.getFileContent(cqQueryService.class
		    .getResource(cqUtil.sQUERY_XML2));
	    // sbXMLRecord =
	    // cqUtil.getFileContent(cqQueryService.class.getResource(cqUtil.sQUERY_XML3));
	    sResult = xCqQueryService.runQuery(sbXMLRecord);
	    System.out.println(sResult);
	    xCqQueryOutput = new cqQueryOutput(sResult);
	    sbResults = xCqQueryOutput.getQueryResultsAsDelimited(",", true);
	    System.out.println("\nResult as Delimited:\n"
		    + sbResults.toString());

	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    xCqQueryService = null;
	    xCqQueryOutput = null;
	}

    }

    /**
     * This is the method that talks to ClearQuest via Rational Team API to
     * fetch a record.
     * 
     * @param sbXMLRecord
     *            An XML input that has all the necessary information to create
     *            a ClearQuest issue. For an example of input XML, please see
     *            cqFetchExample.xml file and its corresponding XSD schema file.
     * @return XML formatted result of the action. Any errors/exception will be
     *         inside of the returned XML formatted string.
     */
    public String fetch(StringBuffer sbXMLInput) {

	String sQueryName = null;
	String sID = null;
	String sClient = null;
	boolean bFound = false;
	StringBuffer sbXMLResult = null;
	cqFetchInput xCqFetchInput = null;
	CqQuery[] teamCqQueries = null;
	CqQuery teamCqQuery = null;
	CqResultSet teamCqResultSet = null;
	DisplayField[] teamDisplayField = null;
	Object[] oCqRowData = null;
	CqProvider teamProvider = null;
	cqUsageMeasurement xCqUsageMeasurement = null;
	FilterLeaf[] dFilters = null;
	CqRowData row = null;
	
	try {

		// Parse the input request.
	    xCqFetchInput = new cqFetchInput(sbXMLInput);

	    String sUser = xCqFetchInput.getCqUser().getUsername();
	    String sPassword = xCqFetchInput.getCqUser().getPassword();
	    String sDatabase = xCqFetchInput.getCqUser().getDatabase();
	    String sSchema = xCqFetchInput.getCqUser().getSchema();
	    if (xCqFetchInput.getCqRecord() != null) {
	    	sID = xCqFetchInput.getCqRecord().getId();
	    }

	    // Check to make sure and ID is provided with the XML.
	    if (cqUtil.isEmpty(sID)) {
	    	throw new Exception("Please provide ID of an existing record.");
	    }
	    sClient = xCqFetchInput.getCqClient().getClientName();
	    this._sDatabase = sDatabase;
	    this._sSchema = sSchema;

	    // Initialize the measurements collection.
	    xCqUsageMeasurement = new cqUsageMeasurement(xCqFetchInput
		    .getCqUser(), xCqFetchInput.getCqClient(),
		    cqUtil.sFETCH_PROBLEM_SERVICE, true);

	    // Get the provider.
	    teamProvider = getProvider(sUser, sPassword);

	    // Get all the query objects.
	    teamCqQueries = getCqQueries(teamProvider);

	    // Find the user given query.
	    for (int i = 0; !bFound && i < teamCqQueries.length; i++) {
	    	teamCqQuery = teamCqQueries[i];
	    	sQueryName = teamCqQuery.toString();
	    	sQueryName = sQueryName.replaceFirst(QUERY_PREFIX, "");
	    	sQueryName = sQueryName.replace("@" + _sSchema + "/"
	    			+ _sDatabase, "");

	    	if (sQueryName.equalsIgnoreCase(FETCH_QUERY)) {
	    		bFound = true;
	    	}
	    }

	    // If the query is not found, throw an exception.
	    if (!bFound) {
	    	throw new Exception("Query is not found! Query: \""
	    			+ FETCH_QUERY + "\"");
	    }

	    // Retrieve detailed information about the selected query.
	    teamCqQuery = (CqQuery) teamCqQuery.doReadProperties(
		    new PropertyRequest(QUERY_PROPERTIES));

	    dFilters = teamCqQuery.getDynamicFilters();
	    int iFilters = dFilters.length;
	    if (iFilters > 0) {
	    	dFilters[0].setOperation(Operation.IS_EQUAL);
	    	dFilters[0].setTarget(TargetType.CONSTANT, sID);
	    	teamCqResultSet = teamCqQuery.doExecute(1, Long.MAX_VALUE, CqQuery.COUNT_ROWS, 
													dFilters);
	    } else {
	    	throw new Exception(
				"Failed to set the dynamic filter for the fetch query.");
	    }

	    if (teamCqResultSet.hasNext()) {
	    	teamDisplayField = teamCqQuery.getDisplayFields();
	    	oCqRowData = new Object[(int) teamCqResultSet.getRowCount()];
	    	while (teamCqResultSet.hasNext()) {
	    		row = (CqRowData) teamCqResultSet.next();
	    		oCqRowData[(int) row.getRowNumber() - 1] = row.getValues();
	    	}
	    }
	    teamCqResultSet.release();

	    // Update CQ database that the client just used "fetch" service.
	    
	    cqSysAppUsage xCqSysAppUsage = new cqSysAppUsage(
		    cqUtil.sFETCH_PROBLEM_SERVICE, sClient, sUser);
	    xCqSysAppUsage.recordUsageToCQ(teamProvider, sSchema, sDatabase);

	    // Release the resource.
	    if (teamProvider != null) {
	    	teamProvider.terminate();
	    }

	    // Finish the measurements collection.
	    xCqUsageMeasurement.record();

	    if (oCqRowData == null || oCqRowData.length == 0) {
	    	throw new Exception("Can not fetch any data for " + sID
	    			+ ". Check the record ID to make sure it is valid.");
	    }

	    sbXMLResult = getFetchResultAsXML(teamDisplayField, oCqRowData,
	    		null);

	    
	} catch (Exception e) {
	    String sError = null;
	    StringWriter sw = new StringWriter();
	    e.printStackTrace(new PrintWriter(sw, true));
	    try {
	    	sError = cqUtil.filterXMLReservedChars(sw.toString());
	    } catch (Exception ignore) {
	    	sError = sw.toString();
	    }
	    sbXMLResult = getFetchResultAsXML(null, null, sError);
	    e.printStackTrace();
	} finally {
	    xCqFetchInput = null;
	    teamCqQueries = null;
	    teamCqQuery = null;
	    teamCqResultSet = null;
	    teamDisplayField = null;
	    oCqRowData = null;
	    teamProvider = null;
	    xCqUsageMeasurement = null;
	}
	return sbXMLResult.toString();
    }

    
    /**
     * This is the method that talks to ClearQuest via Rational Team API to
     * fetch all the existing queries.
     * 
     * @param sbXMLRecord
     *            An XML input that has all the necessary information to create
     *            a ClearQuest issue. For an example of input XML, please see
     *            cqQueryExample.xml file and its corresponding XSD schema file.
     * @return XML formatted result of the action. Any errors/exception will be
     *         inside of the returned XML formatted string.
     */
    public String getAllQueries(StringBuffer sbXMLRecord) {

	StringBuffer sbXMLResult = null;
	cqQueryInput xCqQueryInput = null;
	String[] saQueryAsString = null;
	CqQuery[] teamStpQueries = null;
	CqProvider teamProvider = null;
	cqUsageMeasurement xCqUsageMeasurement = null;

	try {

	    // Parse the input request.
	    xCqQueryInput = new cqQueryInput(sbXMLRecord);
	    String sUser = xCqQueryInput.getCqUser().getUsername();
	    String sPassword = xCqQueryInput.getCqUser().getPassword();
	    String sDatabase = xCqQueryInput.getCqUser().getDatabase();
	    String sSchema = xCqQueryInput.getCqUser().getSchema();
	    String sClient = xCqQueryInput.getCqClient().getClientName();
	    this._sDatabase = sDatabase;
	    this._sSchema = sSchema;
	    this._sUserQuery = xCqQueryInput.getCqQuery().getQueryName();

	    // Initialize the measurements collection.
	    xCqUsageMeasurement = new cqUsageMeasurement(xCqQueryInput
		    .getCqUser(), xCqQueryInput.getCqClient(),
		    cqUtil.sQUERY_SERVICE, true);
	    // Get the provider.
	    teamProvider = getProvider(sUser, sPassword);

	    // Get the queries.
	    sbXMLResult = new StringBuffer();
	    teamStpQueries = getCqQueries(teamProvider);
	    saQueryAsString = getAllQueriesAsString(teamStpQueries);
	    sbXMLResult = getAllQueriesAsXML(saQueryAsString, null);

	    // Update CQ database that the client just used "query" service.
	    cqSysAppUsage xCqSysAppUsage = new cqSysAppUsage(
		    cqUtil.sQUERY_SERVICE, sClient, sUser);
	    xCqSysAppUsage.recordUsageToCQ(teamProvider, sSchema, sDatabase);

	    // Release the resource.
	    if (teamProvider != null) {
		teamProvider.terminate();
	    }

	    // Finish the measurements collection.
	    xCqUsageMeasurement.record();

	} catch (Exception e) {
	    String sError = null;
	    StringWriter sw = new StringWriter();
	    e.printStackTrace(new PrintWriter(sw, true));
	    try {
		sError = cqUtil.filterXMLReservedChars(sw.toString());
	    } catch (Exception ignore) {
		sError = sw.toString();
	    }
	    sbXMLResult = getAllQueriesAsXML(null, sError);
	    System.err.println("An error occured:\n");
	    e.printStackTrace();
	} finally {
	    xCqQueryInput = null;
	    saQueryAsString = null;
	    teamStpQueries = null;
	    teamProvider = null;
	    xCqUsageMeasurement = null;
	}
	return sbXMLResult.toString();
    }

    /**
     * This is the method that talks to ClearQuest via Rational Team API to
     * fetch the results of an query.
     * 
     * @param sbXMLRecord
     *            An XML input that has all the necessary information to create
     *            a ClearQuest issue. For an example of input XML, please see
     *            cqQueryExample2.xml file and its corresponding XSD schema
     *            file.
     * @return XML formatted result of the action. Any errors/exception will be
     *         inside of the returned XML formatted string.
     */
    public String runQuery(StringBuffer sbXMLInput) {

	String sQueryName = null;
	String sField = null;
	String sOperator = null;
	String sValue = null;
	boolean bFound = false;
	StringBuffer sbXMLResult = null;
	cqQueryInput xCqQueryInput = null;
	CqQuery[] teamStpQueries = null;
	CqQuery teamCqQuery = null;
	CqResultSet teamCqResultSet = null;
	DisplayField[] teamDisplayField = null;
	Object[] oCqRowData = null;
	CqRowData row = null;
	CqProvider teamProvider = null;
	Vector vcFilter = null;
	Object[] oFilterDescription = null;
	cqUsageMeasurement xCqUsageMeasurement = null;
	String[] saValues = null;
	ArrayList alValues = null;
	CqQuery.FilterLeaf.TargetType[] types = null;
	FilterLeaf[] dFilters = null;
	
	try {

	    // Parse the input request.
	    xCqQueryInput = new cqQueryInput(sbXMLInput);
	    String sUser = xCqQueryInput.getCqUser().getUsername();
	    String sPassword = xCqQueryInput.getCqUser().getPassword();
	    String sDatabase = xCqQueryInput.getCqUser().getDatabase();
	    String sSchema = xCqQueryInput.getCqUser().getSchema();
	    String sClient = xCqQueryInput.getCqClient().getClientName();
	    _sDatabase = sDatabase;
	    _sSchema = sSchema;
	    _sUserQuery = xCqQueryInput.getCqQuery().getQueryName();

	    // Initialize the measurements collection.
	    xCqUsageMeasurement = new cqUsageMeasurement(xCqQueryInput
		    .getCqUser(), xCqQueryInput.getCqClient(),
		    cqUtil.sQUERY_SERVICE, true);

	    // Get the provider.
	    teamProvider = getProvider(sUser, sPassword);

	    // Get all the query objects.
	    teamStpQueries = getCqQueries(teamProvider);

	    // Find the user given query.
	    for (int i = 0; !bFound && i < teamStpQueries.length; i++) {
	    	teamCqQuery = teamStpQueries[i];
	    	sQueryName = teamCqQuery.toString();
	    	sQueryName = sQueryName.replaceFirst(QUERY_PREFIX, "");
	    	sQueryName = sQueryName.replace("@" + _sSchema + "/"
	    			+ _sDatabase, "");
	    	bFound = sQueryName.equals(_sUserQuery);
	    }

	    // If the query is not found, throw an exception.
	    if (!bFound) {
	    	throw new Exception("Query is not found! Query: \""
	    			+ _sUserQuery + "\"");
	    }

	    // Retrieve detailed information about the selected query.
	    teamCqQuery = (CqQuery) teamCqQuery.doReadProperties(
			    new PropertyRequest(QUERY_PROPERTIES));

	    dFilters = teamCqQuery.getDynamicFilters();
	    int iFilters = dFilters.length;
	    if (iFilters > 0) {

	    	vcFilter = xCqQueryInput.getCqQuery().getFilter();
	    	if (vcFilter == null || vcFilter.size() <= 0
	    			|| iFilters != vcFilter.size()) {
	    		throw new Exception("This query requires " + iFilters
				    + "dynamic filters. Please supply the filters in the order"
				    + " in which they are defined in ClearQuest Edit Query functionality.");
	    	}

	    	// Process dynamic filters.
	    	for (int j = 0; j < iFilters; j++) {

	    		String sSearchedFilter = dFilters[j].getSourceName();

	    		boolean bSet = false;
	    		for (int i = 0; i < vcFilter.size() && !bSet; i++) {
	    			oFilterDescription = (Object[]) vcFilter.get(i);
	    			sField = (String) oFilterDescription[0];
	    			sOperator = (String) oFilterDescription[1];
	    			sValue = (String) oFilterDescription[2];
	    			// Set the filter and its operation and values if it is
	    			// the right one.
	    			if (sSearchedFilter.equalsIgnoreCase(sField)) {
	    				dFilters[j].setOperation(
	    						cqUtil.getTeamOperation(sOperator));
	    				if (cqUtil.isEmpty(sValue)) {
	    					throw new Exception(
	    							"The dynamic filters cannot be blank or null. Please supply a valid value for \""
	    							+ sField + "\".");
	    				} else {
	    					if (sOperator.equalsIgnoreCase(cqUtil.sIN)
	    							|| sOperator
	    							.equalsIgnoreCase(cqUtil.sNOT_IN)
	    							|| sOperator
	    							.equalsIgnoreCase(cqUtil.sBETWEEN)
	    							|| sOperator
	    							.equalsIgnoreCase(cqUtil.sNOT_BETWEEN)) {
	    						saValues = sValue.split(",");
	    						alValues = new ArrayList(saValues.length);
	    						for (int k = 0; k < saValues.length; k++) {
	    							alValues.add(saValues[k].trim());
	    						}
	    						types = new CqQuery.FilterLeaf.TargetType[alValues
	    						                                          .size()];
	    						for (int k = 0; k < alValues.size(); k++) {
	    							types[k] = TargetType.CONSTANT;
	    						}
	    						dFilters[j].setTargets(types, alValues);

	    					} else {
	    						dFilters[j].setTarget(TargetType.CONSTANT,
	    								sValue);
	    					}
	    				}
	    				// We found the right filter to set.
	    				bSet = true;
	    			}
	    			// System.out.println("This is not the right filter to set... Moving to the next filter...");
	    			// // Debug.
	    		}

	    		if (!bSet) {
	    			throw new Exception("The dynamic filters for "
	    					+ sSearchedFilter
	    					+ " were not provided. Please supply a valid value for \""
	    					+ sSearchedFilter + "\".");
	    		}
	    	}

	    	teamCqResultSet = teamCqQuery.doExecute(1, Long.MAX_VALUE, CqQuery.COUNT_ROWS, dFilters);

	    } else {
	    	teamCqResultSet = teamCqQuery.doExecute(1, Long.MAX_VALUE, CqQuery.COUNT_ROWS, null);
	    }

	    if (teamCqResultSet.hasNext()) {
	    	teamDisplayField = teamCqQuery.getDisplayFields();
	    	oCqRowData = new Object[(int) teamCqResultSet.getRowCount()];
	    	while (teamCqResultSet.hasNext()) {
	    		row = (CqRowData) teamCqResultSet.next();
	    		oCqRowData[(int) row.getRowNumber() - 1] = row.getValues();
	    	}
	    }
	    teamCqResultSet.release();
	    

	    // Update CQ database that the client just used "query" service.
	    cqSysAppUsage xCqSysAppUsage = new cqSysAppUsage(
		    cqUtil.sQUERY_SERVICE, sClient, sUser);
	    xCqSysAppUsage.recordUsageToCQ(teamProvider, sSchema, sDatabase);

	    // Release the resource.
	    if (teamProvider != null) {
	    	teamProvider.terminate();
	    }

	    // Finish the measurements collection.
	    xCqUsageMeasurement.record();
	    
	    sbXMLResult = getQueryResultAsXML(teamDisplayField, oCqRowData,
		    null);

	} catch (Exception e) {
	    String sError = null;
	    StringWriter sw = new StringWriter();
	    e.printStackTrace(new PrintWriter(sw, true));
	    try {
		sError = cqUtil.filterXMLReservedChars(sw.toString());
	    } catch (Exception ignore) {
		sError = sw.toString();
	    }
	    if (e instanceof WvcmException) {
		WvcmException w = (WvcmException) e;
		System.err.println("Error reported by WvcmException:\n");
		System.err.println("w.getReasonCode()=" + w.getReasonCode());
		w.printStackTrace();
		sError = "ReasonCode = " + w.getReasonCode() + sError;
	    }

	    sbXMLResult = getQueryResultAsXML(null, null, sError);
	    System.err
		    .println("An error occured. See System.err stream for details.\n");
	    e.printStackTrace();
	} finally {
	    xCqQueryInput = null;
	    teamStpQueries = null;
	    teamCqQuery = null;
	    teamCqResultSet = null;
	    teamDisplayField = null;
	    oCqRowData = null;
	    teamProvider = null;
	    vcFilter = null;
	    oFilterDescription = null;
	    xCqUsageMeasurement = null;
	    saValues = null;
	    alValues = null;
	    types = null;
	    row = null;
	}
	return sbXMLResult.toString();
    }

    /**
     * Converts and formats CqQuery[] objects into String[].
     * 
     * @param teamStpQueries
     *            Collection of the CqQuery team API objects.
     * @return
     * @throws Exception
     */
    private String[] getAllQueriesAsString(CqQuery[] teamStpQueries)
	    throws Exception {

	String[] saReturn = null;
	CqQuery teamCqQuery = null;
	String sQueryName = null;
	int iTeamStpQueries = 0;

	try {

	    iTeamStpQueries = teamStpQueries.length;
	    saReturn = new String[iTeamStpQueries];
	    for (int i = 0; i < iTeamStpQueries; i++) {
	    	teamCqQuery = teamStpQueries[i];
	    	sQueryName = teamCqQuery.toString();
	    	sQueryName = sQueryName.replaceFirst(QUERY_PREFIX, "");
	    	sQueryName = sQueryName.replace("@" + _sSchema + "/"
	    			+ _sDatabase, "");
	    	saReturn[i] = sQueryName;
	    }

	    // Sort the results.
	    Arrays.sort(saReturn, String.CASE_INSENSITIVE_ORDER);

	} catch (Exception e) {
	    throw (e);
	} finally {
	    teamCqQuery = null;
	}
	return saReturn;
    }

    /**
     * Prepares the XML formatted return string for all queries action.
     * 
     * @param saQuery
     *            Collection of the queries to write.
     * @param sError
     *            The error that needs to be written to return string if the
     *            operation/action was not successful, the error/exception
     * @return
     */
    private StringBuffer getAllQueriesAsXML(String[] saQuery, String sError) {

	StringBuffer sbReturnXML = null;

	try {
	    sbReturnXML = new StringBuffer();
	    sbReturnXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	    sbReturnXML.append("<CqQuery>\n");

	    if (saQuery != null && saQuery.length > 0) {
		sbReturnXML.append(" <queries>\n");
		for (int i = 0; i < saQuery.length; i++) {
		    sbReturnXML.append("  <query>"
			    + cqUtil.filterXMLReservedChars(saQuery[i])
			    + "</query>\n");
		}
		sbReturnXML.append(" </queries>\n");
	    }

	    sbReturnXML.append(" <error>\n");
	    sbReturnXML.append("  <description>"
		    + cqUtil.filterXMLReservedChars(sError)
		    + "</description>\n");
	    sbReturnXML.append(" </error>\n");
	    sbReturnXML.append("</CqQuery>\n");

	} catch (Exception e) {
	    sbReturnXML = new StringBuffer();
	    sbReturnXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	    sbReturnXML.append("<CqQuery>\n");
	    sbReturnXML.append(" <queries>\n");
	    sbReturnXML.append(" </queries>\n");
	    sbReturnXML.append(" <error>\n");
	    sbReturnXML.append("  <description>" + e.getMessage()
		    + "</description>\n");
	    sbReturnXML.append(" </error>\n");
	    sbReturnXML.append("</CqQuery>\n");
	} finally {
	}
	return sbReturnXML;
    }

    /**
     * Creates the CqUserDb team API to obtain the configured database/schemas.
     * 
     * @param provider
     *            Rational CqProvider object that holds the connection.
     * @param sDatabase
     *            CQ database name (for example: TRYIT).
     * @param sSchema
     *            CQ schema name (for example: mdcms).
     * @return
     * @throws Exception
     */
    /* Obsolete.  Use cqService.getCqUserDb() method.
    private CqUserDb getCqUserDb(CqProvider provider, String sDatabase,
	    String sSchema) throws Exception {

	String sLocation = null;
	StpLocation stpLocation = null;
	CqUserDb teamCqUserDb = null;

	try {
	    sLocation = "cq.userdb:" + sSchema + "/" + sDatabase;
	    stpLocation = (StpLocation) provider.location(sLocation);
	    teamCqUserDb = provider.cqUserDb(stpLocation);
	} catch (Exception e) {
	    throw (e);
	} finally {
	}

	return teamCqUserDb;
    }
*/
    
    /**
     * Parses the strange, undocumented mixed Object's value to extract the
     * display value. The string representation of this undocumented objects are
     * like this, for example:
     * 
     * <pre>
     *          cq.record:users/the-user-name@mdcms/TRYIT
     *          cq.record:ratl_replicas/&lt;local&gt;@mdcms/TRYIT
     *          and some is like:
     *          cq.record:Problem@mdcms/TRYIT
     * </pre>
     * 
     * @param sInput
     * @return
     * @throws Exception
     */
    /* Obsolete with CQ 7.1
    private String getDisplayValueOfStrangeMixedUndocumentedCqObject(
	    String sInput) throws Exception {
	String sReturn = null;

	try {

	    // Find first location of "/"
	    int iSlashLocation = sInput.indexOf("/");

	    // Find the first "@"
	    int iAtLocation = sInput.indexOf("@");

	    // Get the string in between.
	    if (iSlashLocation > iAtLocation) {
		int iColumnLocation = sInput.indexOf(":");
		sReturn = sInput.substring(iColumnLocation + 1, iAtLocation);
	    } else {
		sReturn = sInput.substring(iSlashLocation + 1, iAtLocation);
	    }

	} catch (Exception e) {
	    throw new Exception("Failed to parse " + sInput
		    + " to determine the display name: " + e.toString());
	} finally {
	}

	return sReturn;
    }
    */

    /**
     * Prepares the XML formatted return string for fetch results.
     * 
     * @param teamDisplayField
     *            Fields as collection of DisplayField objects.
     * @param oCqRowData
     *            Row data s collection of CqRowData objects.
     * @param sError
     *            The error that needs to be written to return string if the
     *            operation/action was not successful, the error/exception
     * @return
     */
    private StringBuffer getFetchResultAsXML(DisplayField[] teamDisplayField,
	    Object[] oCqRowData, String sError) {

	StringBuffer sbReturnXML = null;
	String sLabel = null;
	String sValue = null;
	Object oValue = null;
	
	try {

		sbReturnXML = new StringBuffer();
	    sbReturnXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	    sbReturnXML.append("<CqFetch>\n");
	    sbReturnXML.append(" <result>\n");

	    // Add the results tag.
	    if (teamDisplayField != null && oCqRowData != null) {
	    	int iFieldCount = teamDisplayField.length;
	    	int iRowCount = oCqRowData.length;
	    	for (int i = 0; i < iRowCount; i++) {
	    		for (int j = 0; j < iFieldCount; j++) {
	    			sLabel = teamDisplayField[j].getLabel();
	    			sbReturnXML.append("    <field name=\""
	    					+ cqUtil.filterXMLReservedChars(sLabel)
	    					+ "\">");
	    			oValue = ((Object[]) oCqRowData[i])[j];
	    			if (oValue == null) {
	    				sbReturnXML.append("");
	    			} else {
	    				if (oValue instanceof Date) {
	    					sValue = dateToString((Date) oValue);
	    				} else {
	    					sValue = oValue.toString();
	    				}
	    				sbReturnXML.append(cqUtil.filterXMLReservedChars(sValue));
	    			}
	    			sbReturnXML.append("</field>\n");
	    		}
	    	}
	    }
	    
	    sbReturnXML.append(" </result>\n");
	    // Add the error tag.
	    sbReturnXML.append(" <error>\n");
	    sbReturnXML.append("  <description>"
		    + cqUtil.filterXMLReservedChars(sError)
		    + "</description>\n");
	    sbReturnXML.append(" </error>\n");

	    // Add the finishing tag.
	    sbReturnXML.append("</CqFetch>\n");

	} catch (Exception e) {
	    sbReturnXML = new StringBuffer();
	    sbReturnXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	    sbReturnXML.append("<CqFetch>\n");
	    sbReturnXML.append(" <result>\n");
	    sbReturnXML.append(" </result>\n");
	    sbReturnXML.append(" <error>\n");
	    sbReturnXML.append("  <description>" + e.toString()
		    + "</description>\n");
	    sbReturnXML.append(" </error>\n");
	    sbReturnXML.append("</CqFetch>\n");
	}
	return sbReturnXML;
    }

    /**
     * Prepares the XML formatted return string for a query results.
     * 
     * @param teamDisplayField
     *            Fields as collection of DisplayField objects.
     * @param oCqRowData
     *            Row data s collection of CqRowData objects.
     * @param sError
     *            The error that needs to be written to return string if the
     *            operation/action was not successful, the error/exception
     * @return
     */
    private StringBuffer getQueryResultAsXML(DisplayField[] teamDisplayField,
	    Object[] oCqRowData, String sError) {

	StringBuffer sbReturnXML = null;
	String sLabel = null;
	String sValue = null;
	Object oValue = null;

	try {
	    sbReturnXML = new StringBuffer();
	    sbReturnXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	    sbReturnXML.append("<CqQuery>\n");

	    // Add the query tag.
	    sbReturnXML.append(" <query>\n");
	    sbReturnXML.append("  <query-name>"
		    + cqUtil.filterXMLReservedChars(_sUserQuery)
		    + "</query-name>\n");
	    sbReturnXML.append(" </query>\n");

	    // Add the results tag.
	    if (teamDisplayField != null && oCqRowData != null) {
	    	int iFieldCount = teamDisplayField.length;
	    	int iRowCount = oCqRowData.length;
	    	sbReturnXML.append(" <results>\n");
	    	for (int i = 0; i < iRowCount; i++) {
	    		if (oCqRowData[i] != null) {
	    			sbReturnXML.append("  <result>\n");
	    			for (int j = 0; j < iFieldCount; j++) {
	    				sLabel = teamDisplayField[j].getLabel();
	    				sbReturnXML
	    				.append("    <field name=\""
							+ cqUtil.filterXMLReservedChars(sLabel)
							+ "\">");
		    			oValue = ((Object[]) oCqRowData[i])[j];
		    			if (oValue == null) {
		    				sbReturnXML.append("");
		    			} else {
		    				if (oValue instanceof Date) {
		    					sValue = dateToString((Date) oValue);
		    				} else {
		    					sValue = oValue.toString();
		    				}
	    					sbReturnXML.append(cqUtil
	    							.filterXMLReservedChars(sValue));
	    				}
	    				sbReturnXML.append("</field>\n");
	    			}
	    			sbReturnXML.append("  </result>\n");
	    		}
	    	}
	    	sbReturnXML.append(" </results>\n");
	    }
	    // Add the error tag.
	    sbReturnXML.append(" <error>\n");
	    sbReturnXML.append("  <description>"
	    		+ cqUtil.filterXMLReservedChars(sError)
	    		+ "</description>\n");
	    sbReturnXML.append(" </error>\n");
	    
	    // Add the finishing tag.
	    sbReturnXML.append("</CqQuery>\n");

	} catch (Exception e) {
		
	    sbReturnXML = new StringBuffer();
	    sbReturnXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	    sbReturnXML.append("<CqQuery>\n");
	    sbReturnXML.append(" <query>\n");
	    sbReturnXML.append("  <query-name>" + _sUserQuery
		    + "</query-name>\n");
	    sbReturnXML.append(" </query>\n");
	    sbReturnXML.append(" <error>\n");
	    sbReturnXML.append("  <description>" + e.toString()
		    + "</description>\n");
	    sbReturnXML.append(" </error>\n");
	    sbReturnXML.append("</CqQuery>\n");
	}
	return sbReturnXML;
    }

    /**
     * Ontains the existing queries in ClearQuest as an array of CqQuery
     * objects.
     * 
     * @param userDb
     *            CQ data base object to talk to.
     * @return
     * @throws Exception
     */
    private CqQuery[] getCqQueries(CqUserDb userDb) throws Exception {

	CqQuery[] teamCqQueries = null;

	try {

	    // Obtain a list of all queries from the database.
	    userDb = (CqUserDb) userDb.doReadProperties(
			    new PropertyRequest(new PropertyName[] { CqUserDb.ALL_QUERIES }));
	    teamCqQueries = (CqQuery[]) userDb.getAllQueries().toArray(
		    new CqQuery[] {});

	} catch (Exception e) {
	    throw (e);
	} finally {
	}
	return teamCqQueries;
    }

    private CqQuery[] getCqQueries(CqProvider teamProvider) throws Exception {

	CqUserDb teamCqUserDb = null;
	CqQuery[] teamCqQueries = null;

	try {

	    teamCqUserDb = getCqUserDb(teamProvider, _sSchema, _sDatabase);
	    teamCqQueries = getCqQueries(teamCqUserDb);

	} catch (Exception e) {
	    throw (e);
	} finally {
	    teamCqUserDb = null;
	}
	return teamCqQueries;
    }

}
