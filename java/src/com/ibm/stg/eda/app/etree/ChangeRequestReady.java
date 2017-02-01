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
 * Verifies the ChangeRequest is approved is valid for the branch. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/01/2011 GFS  Initial coding.
 * 09/09/2011 GFS  Updated so help is shown if no parm specified.
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
import com.ibm.stg.eda.component.tk_etreedb.StageName_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Branch;
import com.ibm.stg.eda.component.tk_etreeobjs.BranchName;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestReady extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "commit.ready";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aComponent     Component object
     * @param     aCr            ChangeRequest object
     * @param     aBranchName    The BranchName object
     */
    public ChangeRequestReady(EdaContext aContext, Component aComponent,
                              ChangeRequest aCr, BranchName aBranchName)	
                              throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setComponent(aComponent);
	setChangeRequest(aCr);
	setBranchName(aBranchName);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ChangeRequestReady(EdaContext aContext) throws IcofException {

	this(aContext, null, null, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new ChangeRequestReady(null);
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

	// Verify this ChangeRequest is ready for commits in the Branch
	verifyReady(xContext);

	rollBackDB(xContext, APP_NAME);

	// Set the return code 
	if (isReady()) {
	    setReturnCode(xContext, 0);
	} else {
	    setReturnCode(xContext, 1);
	}


    }


    /**
     * Verify this ChangeRequest is ready for commits in the Branch
     * @param xContext  Application context
     * @throws IcofException 
     */
    public void verifyReady(EdaContext xContext) throws IcofException {

	// Process development TK change requests
	if (getChangeRequest().getClearQuest().equals("DEV")) {
	    setBranchToolKits(xContext);
	    setReady(true);
	    for (ToolKit tk : getBranchToolKits()) {
		if (! tk.getStageName().getName().equals(StageName_Db.STAGE_DEV))
		setReady(false);
	    }
	    return;
	}

	// Verify Change Request is APPROVED
	else {
	    getChangeRequest().getStatus().dbLookupById(xContext);
	    getChangeRequest().getStatus().getStatus().dbLookupById(xContext);
	    String currentStatus = changeRequest.getStatus().getStatus().getName();

	    if (! currentStatus.equals(ChangeRequestStatus_Db.STATUS_APPROVED)) {
		logInfo(xContext, "ERROR: IR status(" + currentStatus + 
		        ") is not APPROVED", verboseInd);
		setReady(false);
		return;
	    }
	}

	// Verify this ChangeRequest's ToolKit matches the Branch's ToolKit
	Branch branch = new Branch(xContext, getBranchName());
	boolean isValid = getChangeRequest().isValidBranch(xContext, branch,
	                                                   getComponent());
	if (! isValid) {
	    logInfo(xContext, "ERROR: the branch tool kit doesn't match the " +
	    		"IR tool kit", verboseInd);
	    
	    setReady(false);
	    return;
	}

	// If all checks have passed then set verified
	setReady(true);

    }

    
    /**
     * Set the ToolKit from the Component and Branch objects
     * @param xContext   Application context.
     * @throws IcofException 
     */
    private void setBranchToolKits(EdaContext xContext) 
    throws IcofException { 
	if (getBranchToolKits() == null) {
	    Branch branch = new Branch(xContext, getBranchName());
	    branchToolKits = branch.findToolKits(xContext, getComponent());
	}    
	for (ToolKit tk : getBranchToolKits()) 
	    logInfo(xContext, "Tool Kit: " + tk.getName(), false);
    }


    protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-cq");
	argSwitches.add("-cr");
	argSwitches.add("-b");
	argSwitches.add("-c");
    }


    protected String readParams(Hashtable<String,String> params, String errors,
                                EdaContext xContext) throws IcofException {

	// Read the Branch name
	if (params.containsKey("-b"))
	    setBranchName(xContext,  params.get("-b"));
	else
	    errors += "Branch (-b) is a required parameter\n";

	// Read the Component name
	if (params.containsKey("-c"))
	    setComponent(xContext,  params.get("-c"));
	else
	    errors += "Component (-c) is a required parameter\n";

	// Read the ClearQuest name
	String crName = "";
	if (params.containsKey("-cr") || params.containsKey("-cq")) {
	    if (params.containsKey("-cr"))
		crName = params.get("-cr");
	    else
		crName = params.get("-cq");
	    
	    if (crName.equalsIgnoreCase("DEV"))
		changeRequest = new ChangeRequest(xContext, crName, 
		                                  "", null, null, null, "");
	    else
		setChangeRequest(xContext,  crName);
	}
	else
	    errors += "ChangeRequest (-cr) is a required parameter\n";


	return errors;
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	logInfo(xContext, "App           : " + APP_NAME + "  " + APP_VERSION, verboseInd);
	logInfo(xContext, "ChangeRequest : " + getChangeRequest().getClearQuest(), verboseInd);
	logInfo(xContext, "Component     : " + getComponent().getName(), verboseInd);
	logInfo(xContext, "Branch        : " + getBranchName().getName(), verboseInd);
	logInfo(xContext, "DB Mode       : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose       : " + getVerboseInd(xContext), verboseInd);
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Verifies the given ChangeRequest is valid for the given\n");
	usage.append("Component and Branch.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-cr ChangeRequest> <-c component> <-b branch>\n");
	usage.append("             [-y] [-h] -db dbMode]\n");
	usage.append("\n");
	usage.append("  ChangeRequest = A Change Request id (MDCMS######### ...).\n");
	usage.append("  component     = Component name (ess, pds, model, einstimer ...).\n");
	usage.append("  branch        = Branch name or trunk if no branch\n");
	usage.append("  -y            = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode        = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h            = Help (shows this information)\n");
	usage.append("\n");
	usage.append("Return Codes\n");
	usage.append("------------\n");
	usage.append(" 0 = ok (CR valid for branch/component)\n");
	usage.append(" 1 = ChangeRequest not in given state\n");
	usage.append("\n");

	System.out.println(usage);

    }

    /**
     * Members.
     */
    private boolean ready;
    Vector<ToolKit> branchToolKits;


    /**
     * Getters.
     */
    public boolean isReady()  { return ready; }
    public Vector<ToolKit> getBranchToolKits() { return branchToolKits; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


    /**
     * Setters.
     */
    private void setReady(boolean aFlag) { ready = aFlag; }


    /**
     * Set the BranchName object
     * @param xContext   Application context.
     * @param aName      Branch name
     * @throws IcofException 
     */
    protected void setBranchName(EdaContext xContext, String aName) 
    throws IcofException { 
	if (getBranchName() == null) {
	    branchName = new BranchName(xContext, aName);
	    branchName.dbLookupByName(xContext);
	}
	logInfo(xContext, "Branch name: " + getBranchName().toString(xContext),
	        false);
    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }


}
