/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *
 *-PURPOSE---------------------------------------------------------------------
 * Displays ChangeRequests in a given state for the specified TK and Component. 
 *-----------------------------------------------------------------------------
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/04/2011 GFS  Initial coding.
 * 09/06/2011 GFS  Added support for Change Request type and severity.
 * 09/09/2011 GFS  Updated so help is shown if no parm specified.
 * 01/11/2012 GFS  Added optional revision paramater that will further limit the
 *                 query of Change Requests.
 * 01/16/2012 GFS  Added optional minimum revision to setChangeRequests.
 * 11/27/2012 GFS  Refactored to use business objects and support all flavors
 *                 of the tool kit name.
 * 11/29/2012 GFS  Updated to support CR's new impacted customer attribute.
 * 09/25/2013 GFS  Updated to be callable from other Java apps
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkConstants;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestStatus;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestShowByStatus extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "changeReqShowByStatus";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     * 
     * @param xContext Application context
     * @param aTk The requested Tool Kit
     * @param aComp The requested Component
     * @param aStatus The requested CR Status
     * @param sMaxRev Null or the maximum revision
     * @param aMinRev Null or the minimum revision
     */
    public ChangeRequestShowByStatus(EdaContext xContext, ToolKit aTk,
				     Component aComp,
				     ChangeRequestStatus aStatus,
				     String aMaxRev, String aMinRev)
    throws IcofException {

	super(xContext, APP_NAME, APP_VERSION);

	setToolKit(aTk);
	setComponent(aComp);
	setStatus(aStatus);
	setMaxRevision(aMaxRev);
	setMinRevision(aMinRev);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * 
     * @exception IcofException Unable to construct ManageApplications object
     */
    public ChangeRequestShowByStatus(EdaContext aContext) throws IcofException {

	this(aContext, null, null, null, "", "");

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new ChangeRequestShowByStatus(null);
	    start(myApp, argv);
	}

	catch (Exception e) {

	    handleExceptionInMain(e);
	}
	finally {

	    handleInFinallyBlock(myApp);
	}

    }


    /**
     * Add, update, delete, or report on the specified applications.
     * 
     * @param aContext Application Context
     * @throws IcofException
     */
    public void process(EdaContext xContext)
    throws IcofException {

	// Connect to the database
	connectToDB(xContext);

	// Determine the CRs which match the criteria then display them.
	setChangeRequests(xContext);
	showChangeRequests(xContext);

	// Set the return code to success if we get this far.
	setReturnCode(xContext, SUCCESS);
	if ((getChangeRequests() == null) || (getChangeRequests().size() < 1))
	    setReturnCode(xContext, TkConstants.NOTHING_TO_DO);

	rollBackDB(xContext, APP_NAME);

    }


    /**
     * Display the ChangeRequests to stdout.
     * 
     * @param xContext Application context
     */
    private void showChangeRequests(EdaContext xContext) {

	StringBuffer results = new StringBuffer();
	results.append("");

	if (getChangeRequests() != null) {
	    results.append(IcofCollectionsUtil.getVectorAsString(getChangeRequests(),
								 ";"));
	}

	System.out.println(results.toString());

    }


    /**
     * Update the state of the new ChangeRequest to APPROVED.
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    public void setChangeRequests(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "Looking up CRs ...", verboseInd);

	// Look up the Component_TkVersion
	Component_Version_Db compVer = 
	   new Component_Version_Db(xContext,
	                            getToolKit().getToolKit(),
	                            getComponent().getComponent());
	compVer.dbLookupByCompRelVersion(xContext);

	// Look up CRs
	ChangeRequest cr = new ChangeRequest(xContext, "", "", getStatus(),
					     null, null, null);
	changeRequests = cr.getAllWithStatus(xContext, compVer, getStatus(),
					     getMaxRevision(), getMinRevision());

    }


    protected String readParams(Hashtable<String, String> params,
				String errors, EdaContext xContext)
    throws IcofException {

	// Read the ToolKit name
	if (params.containsKey("-t")) {
	    setToolKit(xContext, params.get("-t"));
	}
	else {
	    errors += "ToolKit (-t) is a required parameter\n";
	}

	// Read the Component name
	if (params.containsKey("-c")) {
	    setComponent(xContext, params.get("-c"));
	}
	else {
	    errors += "Component (-c) is a required parameter\n";
	}

	// Read the CR Status name
	if (params.containsKey("-s")) {
	    setStatus(xContext, params.get("-s"));
	}
	else {
	    errors += "State (-s) is a required parameter\n";
	}

	// Read the maximum revision
	setMaxRevision(null);
	if (params.containsKey("-max")) {
	    setMaxRevision(params.get("-max"));
	}

	// Read the minimum revision
	setMinRevision(null);
	if (params.containsKey("-min")) {
	    setMinRevision(params.get("-min"));
	}
	return errors;
    }


    protected void createSwitches(Vector<String> singleSwitches,
				  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-c");
	argSwitches.add("-t");
	argSwitches.add("-s");
	argSwitches.add("-max");
	argSwitches.add("-min");
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {

	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	logInfo(xContext, "Tool Kit   : " + getToolKit().getName(), verboseInd);
	logInfo(xContext, "Component  : " + getComponent().getName(),
		verboseInd);
	logInfo(xContext, "State      : " + getStatus().getName(), verboseInd);
	logInfo(xContext, "Max Rev    : " + getMaxRevision(), verboseInd);
	logInfo(xContext, "Min Rev    : " + getMinRevision(), verboseInd);
	logInfo(xContext, "DB Mode    : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose    : " + getVerboseInd(xContext), verboseInd);
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Displays a semi-colon delimited list of ChangeRequests \n");
	usage.append("in the given state for the specified ToolKit and Component.\n");
	usage.append("You can set the min/max revision to reduce the results.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t tool_kit> <-c component> <-s state>\n");
	usage.append("                      [-max max_revision] [-min min_revision]\n");
	usage.append("                      [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  tool_kit    = ToolKit number (14.1.0, 14.1.1 ...).\n");
	usage.append("  component   = Component name (edautils, hdp, model ...).\n");
	usage.append("  state       = The desired state (complete, approved ...).\n");
	usage.append("  max         = Don't show CRs for revisions above this revision.\n");
	usage.append("  max         = Don't show CRs for revionss below this revision.\n");
	usage.append("  -y          = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode      = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h          = Help (shows this information)\n");
	usage.append("\n");
	usage.append("Return Codes\n");
	usage.append("------------\n");
	usage.append(" 0 = ok\n");
	usage.append(" 1 = error\n");
	usage.append(" 2 = no CR match criteria\n");
	usage.append("\n");

	System.out.println(usage);

    }


    /**
     * Members.
     */
    private ChangeRequestStatus status;
    private String maxRevision;
    private String minRevision;
    private Vector<String> changeRequests;


    /**
     * Getters.
     * @formatter:off
     */
    public ChangeRequestStatus getStatus() { return status; }
    public String getMaxRevision() { return maxRevision; }
    public String getMinRevision() { return minRevision; }
    public Vector<String> getChangeRequests() {	return changeRequests; }
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }


    /**
     * Setters.
     */
    private void setStatus(ChangeRequestStatus aStatus) { status = aStatus; }
    private void setMaxRevision(String aRev) { maxRevision = aRev; }
    private void setMinRevision(String aRev) { minRevision = aRev; }
    // @formatter:on


    /**
     * Set the ChangeRequestStatus_Db object from the status name
     * 
     * @param xContext Application context.
     * @param aName Version name like dev_complete ...
     * @throws IcofException
     */
    private void setStatus(EdaContext xContext, String aName)
    throws IcofException {

	if (getStatus() == null) {
	    status = new ChangeRequestStatus(xContext, aName.trim()
							    .toUpperCase());
	    status.dbLookupByName(xContext);
	}
	logInfo(xContext, "Status: " + getStatus().toString(xContext), false);
    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {

	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }


}
