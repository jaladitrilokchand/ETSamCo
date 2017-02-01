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
 * Validates the ChangeRequest is ready for commits 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 09/16/2011 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestStatus_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestValidate extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.isValid";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aChgRreq       ChangeRequest's CQ
     * @param     aComponent     Component to associate the CR with
     */
    public ChangeRequestValidate(EdaContext aContext, ChangeRequest aChgReq,
                                 Component aComponent)	
                                 throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setComponent(aComponent);
	setChangeRequest(aChgReq);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ChangeRequestValidate(EdaContext aContext) throws IcofException {

	this(aContext, null, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new ChangeRequestValidate(null);
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

	// Connect to the database
	connectToDB(xContext);

	// Verify the state.
	validate(xContext);

	rollBackDB(xContext, APP_NAME);
    }


    /**
     * Validate this ChangeRequest is ready for commits
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void validate(EdaContext xContext) throws IcofException {

	setReturnCode(xContext, SUCCESS);

	// If the CR is DEV then ignore the checks
	if (getChangeRequest().getClearQuest().equals("DEV"))
	    return;
	
	// Verify the CR state is APPROVED
	if (! getChangeRequest().getStatus().getStatus().isLoaded())
	    getChangeRequest().getStatus().getStatus().dbLookupById(xContext);

	String myStatusName = getChangeRequest().getStatus().getStatus().getName();
	if (! myStatusName.equals(ChangeRequestStatus_Db.STATUS_APPROVED)) {
	    logInfo(xContext, "ERROR: ChangeRequest is not in the APPROVED state.\n" +
	    "This ChangeRequest is in the " + myStatusName + " state.",
	    true);
	    setReturnCode(xContext, FAILURE);
	}


	// Verify CR is for this specified component
	getChangeRequest().setComponent(xContext);
	getChangeRequest().getComponent().getComponent().dbLookupById(xContext);
	String myCompName = getChangeRequest().getComponent().getComponent().getName();

	if (! myCompName.equals(getComponent().getComponent().getName())) {
	    logInfo(xContext, 
	            "ERROR: ChangeRequest is not assigned to the specified component.\n" +
	            "This ChangeRequest is assigned to " + myCompName, 
	            true);
	    setReturnCode(xContext, FAILURE);
	}

    }


    protected String readParams(Hashtable<String,String> params, String errors,
                                EdaContext xContext) throws IcofException {
	// Read the Component name
	if (params.containsKey("-c"))
	    setComponent(xContext,  params.get("-c"));
	else
	    errors += "Component (-c) is a required parameter\n";

	// Read the ClearQuest name
	if (params.containsKey("-cr"))
	    setChangeRequest(xContext,  params.get("-cr"));
	else
	    errors += "ChangeRequest (-cr) is a required parameter\n";
	
	return errors;
	
    }


    protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
	singleSwitches.add("-y");
	singleSwitches.add("-h");
	singleSwitches.add("-n");
	argSwitches.add("-db");
	argSwitches.add("-cr");
	argSwitches.add("-c");
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	logInfo(xContext, "App           : " + APP_NAME + "  " + APP_VERSION, verboseInd);
	logInfo(xContext, "ChangeRequest : " + getChangeRequest().getClearQuest(), verboseInd);
	logInfo(xContext, "Component     : " + getComponent().getName(), verboseInd);
	logInfo(xContext, "DB Mode       : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose       : " + getVerboseInd(xContext), verboseInd);
	logInfo(xContext, "", verboseInd);
    }


    /**
     * Display this application's usage and invocation
     */
    public void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Verifies this ChangeRequest is open for commits in this Component.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-cr ChangeRequest> <-c component> [-y] [-h] -db dbMode]\n");
	usage.append("\n");
	usage.append("  component     = Component name (ess, pds, model, einstimer ...).\n");
	usage.append("  ChangeRequest = Change Request id (MDCMS######### ...).\n");
	usage.append("  -y            = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode        = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h            = Help (shows this information)\n");
	usage.append("\n");
	usage.append("Return Codes\n");
	usage.append("------------\n");
	usage.append(" 0 = Change Request is valid\n");
	usage.append(" 1 = ChangeRequest is NOT open for commits\n");

	System.out.println(usage);

    }

    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

}
