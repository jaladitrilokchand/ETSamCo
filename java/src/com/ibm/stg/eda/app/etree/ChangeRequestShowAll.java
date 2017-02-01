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
 * Displays the ChangeRequests for the User.  This list can be filtered by TK
 * Component, state and active. 
 *-----------------------------------------------------------------------------
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 07/22/2011 GFS  Initial coding.
 * 09/19/2011 GFS  Changed -active to -default.
 * 11/27/2012 GFS  Refactored to use business objects and support all flavors
 *                 of the tool kit name.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequest_Report;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestStatus;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestShowAll extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.showAll";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     *
     * @param     xContext  Application context
     * @param     aCqId     ChangeRequest's CQ number
     */
    public ChangeRequestShowAll(EdaContext xContext)	
    throws IcofException {
	super(xContext, APP_NAME, APP_VERSION);
    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new ChangeRequestShowAll(null);
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
     * @param aContext      Application Context
     * @throws              IcofException
     */
    public void process(EdaContext xContext) throws IcofException {

	connectToDB(xContext);

	showRequests(xContext);

	setReturnCode(xContext, SUCCESS);
	rollBackDB(xContext, APP_NAME);

    }


    /**
     * Look up the desired ChangeRequests and display them.
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void showRequests(EdaContext xContext) throws IcofException {

	// Run the ChangeRequest report
	ChangeRequest_Report report = 
	new ChangeRequest_Report(xContext, getUser(),
	                         getToolKit(), 
	                         getComponent(), 
	                         getState(),
	                         getShowAll());
	report.setContent(xContext);

	// Print the results
	if ((report.getContent() != null) && (report.getContent().length() > 0)) {
	    System.out.print(report.getHeader());
	    System.out.println(report.getContent());
	}
	else {
	    System.out.println("No Change Requests found ...");
	    if (! getShowAll())
		System.out.println("If you want to view DEV Change Requests " +
				"please rerun and add -all");
	}
    }


    protected String readParams(Hashtable<String, String> params,
                                String errors, EdaContext xContext)
    throws IcofException {
	// Read the Tool Kit parameter
	if (params.containsKey("-t"))
	    setToolKit(xContext, params.get("-t"));

	// Read the Component parameter
	if (params.containsKey("-c"))
	    setComponent(xContext, params.get("-c"));

	// Read the State parameter
	if (params.containsKey("-s"))
	    setState(xContext, params.get("-s"));

	// Read the default parameter
	setShowAll(false);
	if (params.containsKey("-all"))
	    setShowAll(true);

	return errors;
    }


    protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
	singleSwitches.add("-y");
	singleSwitches.add("-h");
	singleSwitches.add("-all");
	argSwitches.add("-db");
	argSwitches.add("-t");
	argSwitches.add("-c");
	argSwitches.add("-s");
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	boolean verboseInd = getVerboseInd(xContext);
	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, verboseInd);
	logInfo(xContext, "User       : " + getUser().getAfsId(), verboseInd);
	if (getToolKit() != null)
	    logInfo(xContext, "ToolKit    : " + getToolKit().getName(), verboseInd);
	else 
	    logInfo(xContext, "ToolKit    : null", verboseInd);
	if (getComponent() != null)
	    logInfo(xContext, "Component  : " + getComponent().getName(), verboseInd);
	else 
	    logInfo(xContext, "Component  : null", verboseInd);
	logInfo(xContext, "Show all: " + getShowAll(), verboseInd);
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
	usage.append("Displays ChangeRequests.  You can filter this list by \n");
	usage.append("specifying a Tool Kit, Component and/or ChangeRequest state.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " [-t tool_kit] [-c component] [-s state] [-all]\n");
	usage.append("           [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  -default    = Filter to show only default Change Requests\n");
	usage.append("  tool_kit    = Filter by a specific Tool Kit (14.1.0 ...)\n");
	usage.append("  component   = Filter by a specific Component (hpd, mar, ess ...)\n");
	usage.append("  state       = Filter by a specific CR state (submitted, approved, complete, released)\n");
	usage.append("  -all        = (optional) Show all CRs including DEV (default is false)\n");
	usage.append("  -y          = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode      = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h          = Help (shows this information)\n");
	usage.append("\n");
	usage.append("Return Codes\n");
	usage.append("------------\n");
	usage.append(" 0 = ok\n");
	usage.append(" 1 = error\n");
	usage.append("\n");

	System.out.println(usage);

    }


    /**
     * Members.
     */
    private ChangeRequestStatus state;
    private boolean showAll = false;


    /**
     * Getters.
     */

    public ChangeRequestStatus getState()  { return state; }
    public boolean getShowAll() { return showAll; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}



    /**
     * Setters.
     */
    private void setShowAll(boolean aFlag) { showAll = aFlag; }


    /**
     * Set the Component_Db object from the component name
     * @param xContext  Application context.
     * @param aName     Component name like ess, edautils, pds ...
     * @throws IcofException 
     */
    protected void setComponent(EdaContext xContext, String aName) 
    throws IcofException { 
	try {
	    if (getComponent() == null) {
		component = new Component(xContext, aName.trim());
		component.dbLookupByName(xContext);
	    }    
	    logInfo(xContext, "Component: " + getComponent().toString(xContext), verboseInd);
	}
	catch(IcofException ex) {
	    logInfo(xContext, "Component (" + aName + ") was not found in the database.", true);
	    throw ex;
	}

    }


    /**
     * Set the ChangeRequestStatus_Db object from the status name
     * @param xContext  Application context.
     * @throws IcofException 
     */
    private void setState(EdaContext xContext, String aName) 
    throws IcofException { 
	try {
	    if (getState() == null) {
		state = new ChangeRequestStatus(xContext, aName.trim().toUpperCase());
		state.dbLookupByName(xContext);
	    }    
	    logInfo(xContext, "Status: " + getState().toString(xContext), verboseInd);
	}
	catch(IcofException ex) {
	    logInfo(xContext, "State (" + aName + ") was not found in the database.", true);
	    throw ex;
	}
    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

}
