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
 * Update the name of an existing Branch. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 07/14/2011 GFS  Initial coding.
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
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Branch;
import com.ibm.stg.eda.component.tk_etreeobjs.BranchName;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class BranchUpdate extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "branchUpdate";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aTk            ToolKit for Branch
     * @param     aComponent     Component for Branch
     * @param     aBranch        Existing Branch
     * @param     aNewName       New Branch
     */
    public BranchUpdate(EdaContext aContext, ToolKit aTk, Component aComponent, 
                        BranchName aBranchName, BranchName aNewName)
                        throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setToolKit(aTk);
	setComponent(aComponent);
	setBranchName(aBranchName);
	setNewBranchName(aNewName);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public BranchUpdate(EdaContext aContext) throws IcofException {

	this(aContext, null, null, null, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new BranchUpdate(null);
	    start(myApp, argv);
	}

	catch (Exception e) {

	    handleExceptionInMain(e);
	} finally {

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

	// Determine if branch/component are for a production or development TK.
	updateBranch(xContext);
	commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);


    }


    /**
     * Determines if this branch is valid for the specified Component name
     * 
     * @param xContext  Application context
     * @return          True if branch is a prod branch, false if dev branch.
     * @throws IcofException  Trouble querying database.
     */
    private void updateBranch(EdaContext xContext) throws IcofException {

	// Add the new Branch name
	logInfo(xContext, "Adding New Branch name ...", verboseInd);
	getNewBranchName().dbAdd(xContext);
	logInfo(xContext, "Branch name added ...", verboseInd);

	// Set the Component_Version object
	logInfo(xContext, "Looking up Component/ToolKit ...", verboseInd);
	Component_Version_Db compVersion = 
	new Component_Version_Db(xContext, getToolKit().getToolKit(), 
	                         getComponent().getComponent());
	compVersion.dbLookupByCompRelVersion(xContext);
	logInfo(xContext, "Component/ToolKit found ...", verboseInd);

	// Lookup the old Branch name
	logInfo(xContext, "Looking up Old Branch name ...", verboseInd);
	getBranchName().dbLookupByName(xContext);
	logInfo(xContext, "Branch name found ...", verboseInd);

	// Update the branch name
	logInfo(xContext, "Updating Branch ...", verboseInd);
	Branch newBranch = new Branch(xContext, getNewBranchName(), compVersion);
	Branch oldBranch = new Branch(xContext, getBranchName(), compVersion);
	try {
	    // If the new branch already is associated with the comp ver then
	    // nothing to do
	    newBranch.dbLookupByNameCompVersion(xContext);
	    logInfo(xContext, "Nothihg to do .. new Branch (" + 
	            getNewBranchName().getName() + ") already associated with " + 
	            getToolKit().getName() + "/" + getComponent().getName(),
	            true);
	}
	catch(IcofException ie) {
	    // If new branch is not associated with the comp ver then update
	    // the old branch to the new branch
	    oldBranch.dbUpdate(xContext, getNewBranchName());
	    logInfo(xContext, "Updated Branch for " + getToolKit().getName() + 
	            "/" + getComponent().getName() + " from " + 
	            getBranchName().getName() + " to " + getNewBranchName().getName(),
	            true);
	}
    }


    protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-b");
	argSwitches.add("-t");
	argSwitches.add("-c");
	argSwitches.add("-n");
    }


    protected String readParams(Hashtable<String,String> params, String errors,
                                EdaContext xContext) throws IcofException {
	// Read the new Branch name
	if (params.containsKey("-n")) {
	    setNewBranchName(xContext, (String) params.get("-n"));
	}
	else {
	    errors += "New Branch name (-n) is a required parameter\n";
	}

	// Read the ToolKit name
	if (params.containsKey("-t")) {
	    setToolKit(xContext, (String) params.get("-t"));
	}
	else {
	    errors += "ToolKit (-t) is a required parameter\n";
	}

	// Read the Component name
	if (params.containsKey("-c")) {
	    setComponent(xContext, (String) params.get("-c"));
	}
	else {
	    errors += "Component (-c) is a required parameter\n";
	}

	// Read the Branch name
	if (params.containsKey("-b")) {
	    setBranchName(xContext, (String) params.get("-b"));
	}
	else {
	    errors += "Branch name (-b) is a required parameter\n";
	}
	return errors;
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	boolean verboseInd = getVerboseInd(xContext);
	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, verboseInd);
	logInfo(xContext, "ToolKit    : " + getToolKit().getToolKit().getDisplayName(), verboseInd);
	logInfo(xContext, "Component  : " + getComponent(), verboseInd);
	logInfo(xContext, "Branch name: " + getBranchName().getName(), verboseInd);
	logInfo(xContext, "New name   : " + getNewBranchName().getName(), verboseInd);
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
	usage.append("Update the name of an existing Branch. \n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t toolkit> <-c component> <-b branch> <-n new_name>\n");
	usage.append("            [-y] [-h] -db dbMode]\n");
	usage.append("\n");
	usage.append("  toolkit     = ToolKit name (14.1.0, 14.1.1 ...).\n");
	usage.append("  component   = Component name (ess, pds, model, einstimer ...).\n");
	usage.append("  branch      = Existing Branch name.\n");
	usage.append("  new_name    = New Branch name.\n");
	usage.append("  -y          = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode      = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h          = Help (shows this information)\n");
	usage.append("\n");
	usage.append("Return Codes\n");
	usage.append("------------\n");
	usage.append(" 0 = application ran ok\n");
	usage.append(" 1 = application error\n");
	usage.append("\n");

	System.out.println(usage);

    }


    /**
     * Members.
     */
    private BranchName newBranchName;

    /**
     * Getters.
     */
    public BranchName getNewBranchName()  { return newBranchName; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


    /**
     * Setters.
     */
    private void setNewBranchName(BranchName aName) { newBranchName = aName; }

    /**
     * Set the Branch object from the name
     * @param xContext  Application context.
     * @param aName     Branch name
     * @throws IcofException 
     */
    private void setNewBranchName(EdaContext xContext, String aName) 
    throws IcofException { 
	if (getNewBranchName() == null) {
	    newBranchName = new BranchName(xContext, aName.trim());
	}    
	logInfo(xContext, getNewBranchName().toString(xContext), false);
    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

}
