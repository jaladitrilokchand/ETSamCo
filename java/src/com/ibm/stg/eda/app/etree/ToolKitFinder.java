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
 * Determines the ToolKit for the given Branch and Component. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 07/18/2011 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.Branch;
import com.ibm.stg.eda.component.tk_etreeobjs.BranchName;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class ToolKitFinder extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "toolKitFinder";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aComponent     Component to associate the CR with
     * @param     aBranch        Branch in question
     */
    public ToolKitFinder(EdaContext aContext,  Component aComponent, 
                         BranchName aBranchName)
                         throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setComponent(aComponent);
	setBranchName(aBranchName);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ToolKitFinder(EdaContext aContext) throws IcofException {

	this(aContext, null, null);

    }


    /**
     * Instantiate the class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new ToolKitFinder(null);
	    start(myApp, argv);
	}
	catch (Exception e) {
	    handleExceptionInMain(e);
	} finally {
	    handleInFinallyBlock(myApp);
	}

    }


    //--------------------------------------------------------------------------
    /**
     * Add, update, delete, or report on the specified applications.
     * 
     * @param aContext      Application Context
     * @throws              IcofException
     */
    //--------------------------------------------------------------------------
    public void process(EdaContext xContext) throws IcofException {

	// Connect to the database
	connectToDB(xContext);

	// Determine if branch/component are for a production or development TK.
	findToolKit(xContext);
	rollBackDBAndSetReturncode(xContext, APP_NAME, SUCCESS);

    }


    /**
     * Determines if this branch is valid for the specified Component name
     * 
     * @param xContext  Application context
     * @return          True if branch is a prod branch, false if dev branch.
     * @throws IcofException  Trouble querying database.
     */
    public void findToolKit(EdaContext xContext) throws IcofException {

	// Lookup this branch name in the branch name DB table.  If not found 
	// then not a production branch.
	logInfo(xContext, "Looking up Branch name ...", verboseInd);
	BranchName myBranchName = new BranchName(xContext, getName());
	try {
	    myBranchName.dbLookupByName(xContext);
	    logInfo(xContext, "Found Branch name ...", verboseInd);
	}
	catch(IcofException trap) {
	    logInfo(xContext, "Unable to find ToolKit - Branch name not found.", true);
	    return;
	}
	setBranchName(myBranchName);

	// Now determine if there's a tool kit for this Branch/Component pair.
	// If so then this is a production branch.
	logInfo(xContext, "Looking up ToolKit ...", verboseInd);
	Branch myBranch = new Branch(xContext, getBranchName(), null);
	Vector<ToolKit> tks = myBranch.findToolKits(xContext, getComponent());

	if (! tks.isEmpty()) {
	    for (ToolKit tk : tks) {
		tk.getToolKit().getRelease().dbLookupById(xContext);
		logInfo(xContext, tk.getToolKit().getDisplayName(), true);
	    }
	}
	else
	    logInfo(xContext, "Unable to find ToolKit - Component/Branch pair " + 
	    "not found in database.", true);

    }


    protected String readParams(Hashtable<String,String> params, String errors,
                                EdaContext xContext) throws IcofException {
	// Read the Component name
	if (params.containsKey("-c")) {
	    setComponent(xContext, params.get("-c"));
	}
	else {
	    errors += "Component (-c) is a required parameter\n";
	}

	// Read the Branch name
	if (params.containsKey("-b")) {
	    setName((String) params.get("-b"));
	}
	else {
	    errors += "Branch name (-n) is a required parameter\n";
	}
	return errors;
    }


    protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-b");
	argSwitches.add("-c");
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	boolean verboseInd = getVerboseInd(xContext);
	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, verboseInd);
	logInfo(xContext, "Component  : " + getComponent().getName(), verboseInd);
	logInfo(xContext, "Branch name: " + getName(), verboseInd);
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
	usage.append("Prints the Tool Kit name for the given Branch and \n");
	usage.append("Component.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-c component> <-b branch> [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  component   = Component name (ess, pds, model, einstimer ...).\n");
	usage.append("  branch      = Branch name.\n");
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
    private String name;
    /**
     * Getters.
     */
    public String getName()  { return name; }
    public static boolean getRequestHelp() { return requestHelp; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


    /**
     * Setters.
     */
    private void setName(String aName) { name = aName;  }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }


}
