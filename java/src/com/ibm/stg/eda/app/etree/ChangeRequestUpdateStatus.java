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
 * Update a ChangeRequest's requests status. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/08/2011 GFS  Initial coding.
 * 09/06/2011 GFS  Added support for Change Request type and severity.
 * 09/09/2011 GFS  Updated so help is shown if no parm specified.
 * 09/16/2011 GFS  Change -cq switch to -cr.
 * 11/30/2012 GFS  Updated to support CR's new impacted customer attribute.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestStatus;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestUpdateStatus extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "cr.updateStatus";
    public static final String APP_VERSION = "v1.1";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aCqId ChangeRequest's CQ number
     * @param aStatus ChangeRequest's status
     */
    public ChangeRequestUpdateStatus(EdaContext aContext, String aCqId,
				     ChangeRequestStatus aStatus, User_Db aUser)
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setChangeRequest(aContext, aCqId);
	setNewStatus(aStatus);
	setUser(aUser);

    }


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aCr ChangeRequest object to update
     * @param aStatus ChangeRequest's status
     */
    public ChangeRequestUpdateStatus(EdaContext aContext, ChangeRequest aCr,
				     ChangeRequestStatus aStatus, User_Db aUser)
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setChangeRequest(aCr);
	setNewStatus(aStatus);
	setUser(aUser);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * 
     * @exception IcofException Unable to construct ManageApplications object
     */
    public ChangeRequestUpdateStatus(EdaContext aContext) throws IcofException {

	this(aContext, (ChangeRequest) null, null, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new ChangeRequestUpdateStatus(null);
	    start(myApp, argv);
	}

	catch (Exception e) {

	    handleExceptionInMain(e);
	}
	finally {

	    handleInFinallyBlock(myApp);
	}

    }


    // --------------------------------------------------------------------------
    /**
     * Add, update, delete, or report on the specified applications.
     * 
     * @param aContext Application Context
     * @throws Exception
     */
    // --------------------------------------------------------------------------
    public void process(EdaContext xContext)
    throws Exception {

	// Connect to the database
	connectToDB(xContext);

	// Update the request
	updateChangeRequest(xContext);

	// Set the return code to success if we get this far.
	setReturnCode(xContext, SUCCESS);
	commitToDB(xContext, APP_NAME);

    }


    /**
     * Update the specified ChangeRequest object.
     * 
     * @param xContext Application context
     * @throws IcofException
     * @throws Exception
     */
    public void updateChangeRequest(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "Updating ChangeRequest ...", verboseInd);

	// Set the status
	ChangeRequestStatus crStatus;
	crStatus = new ChangeRequestStatus(xContext,
	                                   getChangeRequest().getChangeRequest()
	                                   .getStatus()
	                                   .getId());

	// Update this ChangeRequest
	getChangeRequest().dbUpdate(xContext,
				    getChangeRequest().getClearQuest(),
				    getChangeRequest().getDescription(),
				    crStatus, 
				    getChangeRequest().getType(),
				    getChangeRequest().getSeverity(),
				    getChangeRequest().getImpactedCustomer(),
				    getUser());

	logInfo(xContext, "Update complete", true);

    }


    protected String readParams(Hashtable<String, String> params,
				String errors, EdaContext xContext)
    throws IcofException {

	// Read the ClearQuest name
	if (params.containsKey("-cr")) {
	    setChangeRequest(xContext, params.get("-cr"));
	}
	else if (params.containsKey("-cq")) {
	    setChangeRequest(xContext, params.get("-cq"));
	}
	else {
	    errors += "ChangeRequest (-cr) is a required parameter\n";
	}

	// Read the status
	if (params.containsKey("-s")) {
	    setStatus(xContext, params.get("-s"));
	}
	else {
	    errors += "A new status (-s) must be specified.\n";
	}
	return errors;
    }


    protected void createSwitches(Vector<String> singleSwitches,
				  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-cq");
	argSwitches.add("-cr");
	argSwitches.add("-s");
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {

	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	logInfo(xContext, "ChangeRequest : "
			  + getChangeRequest().getClearQuest(), verboseInd);
	if (getNewStatus() != null)
	    logInfo(xContext, "Status     : " + getNewStatus().getName(),
		    verboseInd);
	else
	    logInfo(xContext, "Status     : null", verboseInd);
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
	usage.append("Update this ChangeRequest's status. Changing the status\n");
	usage.append("from APPROVED to another state will deactivate this CR\n");
	usage.append("if it's active.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-cr ChangeRequest> <-s new_state>\n");
	usage.append("                [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  ChangeRequest = A ChangeRequest id (MDCMS######### ...).\n");
	usage.append("  new_state     = New ChangeRequest state (new, ready, dev_complete ...)\n");
	usage.append("  -y            = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode        = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h            = Help (shows this information)\n");
	usage.append("\n");
	usage.append("Return Codes\n");
	usage.append("------------\n");
	usage.append(" 0 = ok\n");
	usage.append(" 1 = error\n");
	usage.append("\n");

	System.out.println(usage);

    }


    /**
     * Members
     * @formatter:off
     */
    private ChangeRequestStatus newStatus;

    
    /**
     * Getters
     */
    public ChangeRequestStatus getNewStatus()  { return newStatus; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}

        
    /**
     * Setters
     */
    private void setNewStatus(ChangeRequestStatus aStatus) { newStatus = aStatus; }
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

	if (getNewStatus() == null) {
	    newStatus = new ChangeRequestStatus(xContext, aName.toUpperCase());
	    newStatus.dbLookupByName(xContext);
	}
	logInfo(xContext, "Status: " + getNewStatus().toString(xContext),
		verboseInd);
    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {

	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }


}
