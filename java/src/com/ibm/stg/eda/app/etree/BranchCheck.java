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
 * Determines if a branch is production or development. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 04/28/2011 GFS  Initial coding.
 * 05/10/2011 GFS  Updated to temporarily bypass the checking and always
 *                 return 0.
 * 06/10/2011 GFS  Disabled logging. Updated to lookup DB objects on the fly.
 * 07/13/2011 GFS  Undid the updates from 5/10 and enabled branch checking.
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
import com.ibm.stg.eda.component.tk_etreeobjs.Branch;
import com.ibm.stg.eda.component.tk_etreeobjs.BranchName;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.iipmds.common.IcofException;

public class BranchCheck extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "branchCheck";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aComponent Component to associate the CR with
     * @param aBranch Branch in question
     */
    public BranchCheck(EdaContext aContext, Component aComponent,
		       BranchName aBranchName) throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setComponent(aComponent);
	setBranchName(aBranchName);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * 
     * @exception IcofException Unable to construct ManageApplications object
     */
    public BranchCheck(EdaContext aContext) throws IcofException {

	this(aContext, null, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new BranchCheck(null);
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

	branchName = new BranchName(xContext, getName());

	// Determine if branch/component are for a production or development TK.
	checkBranch(xContext, true);
	rollBackDBAndSetReturncode(xContext, APP_NAME, SUCCESS);


    }


    /**
     * Determines if this branch is valid for the specified Component name
     * 
     * @param xContext Application context
     * @param showResult If true print result to stdout otherwise don't
     * @return True if branch is a prod branch, false if dev branch.
     * @throws IcofException Trouble querying database.
     */
    public boolean checkBranch(EdaContext xContext, boolean showResult)
    throws IcofException {

	// Lookup this branch name in the branch name DB table. If not found
	// then not a production branch.
	logInfo(xContext, "Looking up Branch name ...", verboseInd);
	try {
	    getBranchName().dbLookupByName(xContext);
	    logInfo(xContext, "Found Branch name ...", verboseInd);
	}
	catch (IcofException trap) {
	    logInfo(xContext, "Branch name not found ...", verboseInd);
	    displayResult(xContext, false, showResult);
	    return false;
	}

	// Now determine if there's a tool kit for this Branch/Component pair.
	// If so then this is a production branch.
	logInfo(xContext, "Looking up ToolKit ...", verboseInd);
	boolean isProd = false;
	Branch myBranch = new Branch(xContext, getBranchName(), null);
	if (! myBranch.findToolKits(xContext, getComponent()).isEmpty())
	    isProd = true;
	    
	// Display the result for user.
	displayResult(xContext, isProd, showResult);

	return isProd;

    }


    /**
     * Displays the result.
     * 
     * @param xContext Application context
     * @param isProd Production flag
     * @param showResult If true print result to stdout otherwise don't
     */
    private void displayResult(EdaContext xContext, boolean isProd,
			       boolean showResult) {

	if (!showResult)
	    return;

	// Display the result.
	if (isProd) {
	    System.out.println("1");
	}
	else {
	    System.out.println("0");
	}

    }


    protected void createSwitches(Vector<String> singleSwitches,
				  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-b");
	argSwitches.add("-c");
	
    }


    protected String readParams(Hashtable<String, String> params,
				String errors, EdaContext xContext)
    throws IcofException {

	// Read the Component name
	if (params.containsKey("-c")) {
	    setComponent(xContext, (String) params.get("-c"));
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


    protected void displayParameters(String dbMode, EdaContext xContext) {

	boolean verboseInd = getVerboseInd(xContext);
	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	logInfo(xContext, "Component  : " + getComponent().getName(),
		verboseInd);
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
	usage.append("Determines if the branch requires Change Management. Writes\n");
	usage.append("a 1 to stdout if the branch requires change management \n");
	usage.append("otherwise writes a 0.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME
		     + " <-c component> <-b branch> [-y] [-h] [-db dbMode]\n");
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
     * Members
     * @formatter:off
     */
    private String name;

    /**
     * Getters.
     */
    public String getName()  { return name; }
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

    // @formatter:off
	
}
