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
 * Sets a ChangeRequest's status to COMPLETE 
 *-----------------------------------------------------------------------------
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/03/2011 GFS  Initial coding.
 * 09/09/2011 GFS  Updated so help is shown if no parm specified.
 * 09/16/2011 GFS  Change -cq switch to -cr.
 * 05/22/2013 GFS  Updated to support default CRs.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Scanner;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestStatus_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestStatus;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestComplete extends AbstractChangeRequest {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.complete";
    public static final String APP_VERSION = "v1.0";



    /**
     * Constructor
     *
     * @param     xContext  Application context
     * @param     aCqId     ChangeRequest's CQ number
     */
    public ChangeRequestComplete(EdaContext xContext, String aCqId, boolean aFlag)	
    throws IcofException {

	super(xContext,aCqId, APP_NAME, APP_VERSION, aFlag);
	setChangeRequest(xContext, aCqId);

    }


    /**
     * Constructor
     *
     * @param     aContext  Application context
     * @param     aCr       ChangeRequest object
     */
    public ChangeRequestComplete(EdaContext aContext, ChangeRequest aCr, boolean aFlag)	
    throws IcofException {

	super(aContext, aCr,APP_NAME, APP_VERSION,aFlag);
	setChangeRequest(aCr);
    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ChangeRequestComplete(EdaContext aContext) throws IcofException {

	this(aContext, (ChangeRequest)null, false);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new ChangeRequestComplete(null);
	    start(myApp, argv);
	}
	catch (Exception e) {

	    handleExceptionInMain(e);
	} finally {

	    handleInFinallyBlock(myApp);
	}
    }


    /**
     * Update the state of the new ChangeRequest to APPROVED.
     * 
     * @param xContext  Application context
     * @throws Exception 
     */
    private void markComplete(EdaContext xContext) throws Exception {

	// Determine this CR's status
	ChangeRequestStatus status = 
	new ChangeRequestStatus(xContext, 
	                        getChangeRequest().getChangeRequest().getStatus().getId());
	status.dbLookupById(xContext);
	logInfo(xContext, "Status: " + status.getName(), verboseInd);

	// If this CR is already COMPLETE then don't do anything
	if (status.getName().equals(ChangeRequestStatus_Db.STATUS_COMPLETE)) {
	    logInfo(xContext, "Change Request already marked as Complete.", true);
	    return;
	}

	// Warn the user if no commits (codeUpdates) associated with this
	// change request.
	setCodeUpdates(xContext);
	if (getCodeUpdates().size() == 0) {
	    System.out.println("No SVN commits found for this Change Request.");
	    System.out.print("Continue completing this CR ('y' to continue)? ");
	    Scanner user_input = new Scanner(System.in);
	    String answer = user_input.next();
	    if (! answer.equalsIgnoreCase("y")) {
		logInfo(xContext,
		        "Operation Cancelled per user's request!",
		        true);
		return;
	    }
	}
	else {
	    logInfo(xContext,
	            "Found " + getCodeUpdates().size() + " SVN commit(s) " +
	            "associated with this Change Request",
	            true);
	}

	// If this CR is not APPROVED or ONHOLD then can't complete it.
	if (status.getName().equals(ChangeRequestStatus_Db.STATUS_ONHOLD) || 
	status.getName().equals(ChangeRequestStatus_Db.STATUS_APPROVED)) {

	    // Update the ChangeRequest's status to COMPLETE
	    getChangeRequest().dbUpdateStatus(xContext, 
	                                      ChangeRequestStatus_Db.STATUS_COMPLETE,
	                                      getUser());
	    logInfo(xContext, "Change Request marked as Complete.", true);

	}
	else {
	    logInfo(xContext, "Only ChangeRequests in the " + 
	    ChangeRequestStatus_Db.STATUS_ONHOLD + " or " + 
	    ChangeRequestStatus_Db.STATUS_APPROVED + " can be " +
	    "marked as Complete.", true);
	}
    }


    /**
     * Look up code updates associated with this change request
     * 
     * @param  xContexzt  Application context
     * @throws IcofException
     */
    private void setCodeUpdates(EdaContext xContext) 
    throws IcofException {

	CodeUpdate_ChangeRequest_Db cucr =  
	new CodeUpdate_ChangeRequest_Db(null, 
	                                getChangeRequest().getChangeRequest());
	codeUpdates = cucr.dbLookupCodeUpdates(xContext);
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Mark this ChangeRequest(or your default CR) as development complete.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-cr ChangeRequest | -default> [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  ChangeRequest = A ChangeRequest id (MDCMS######### ...).\n");
	usage.append("  -default      = Complete your default change request\n");
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


    public void doMarkOperation(EdaContext xContext) throws Exception {
	lookupChangeRequest(xContext);
	markComplete(xContext);
    }


    /*
     * Members
     */
    private Hashtable<String,CodeUpdate_Db> codeUpdates;


    /*
     * Getters
     */
    private Hashtable<String,CodeUpdate_Db> getCodeUpdates() { return codeUpdates; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

}
