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

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Component_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Branch;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.Subversion;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class BranchValidate extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "branchValidate";
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
    public BranchValidate(EdaContext aContext, ToolKit aTk, 
                          Component aComponent)
                          throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setToolKit(aTk);
	setComponent(aComponent);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public BranchValidate(EdaContext aContext) throws IcofException {

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

	    myApp = new BranchValidate(null);
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

	// Determine if branch/component are for a production or development TK.
	validate(xContext);

	rollBackDBAndSetReturncode(xContext, APP_NAME, SUCCESS);


    }


    /**
     * Determines if this branch is valid for the specified Component name
     * 
     * @param xContext  Application context
     * @return          True if branch is a prod branch, false if dev branch.
     * @throws IcofException  Trouble querying database.
     */
    private void validate(EdaContext xContext) throws IcofException {

	setComponents(xContext);
	getToolKit().getToolKit().getRelease().dbLookupById(xContext);
	
	for (Component_Db comp : getComponents()) {
	    
	    if (comp.getName().startsWith("etreetest"))
		continue;
	    if (comp.getName().startsWith("svntest"))
		continue;
	    
	    logInfo(xContext, "Component: " + comp.getName(), true);
	    String wcBranch = getWorkingCopyBranch(xContext, comp);
	    String dbBranch = getDatabaseBranch(xContext, comp);
	    
	    if ((wcBranch == null) || ! wcBranch.equals(dbBranch))
		logInfo(xContext, " >> Branches DON'T match <<", true);

	    else 
		logInfo(xContext, " .. Branches match", true);
	   
	    logInfo(xContext, " -> WC " + wcBranch, true);
	    logInfo(xContext, " -> DB " + dbBranch, true);

	}
    }


    /**
     * Query the SVN working copy for the branch name
     *
     * @param xContext
     * @param comp
     * @return
     * @throws IcofException 
     */
    private String getWorkingCopyBranch(EdaContext xContext, Component_Db comp) 
    throws IcofException {

	String workingCopy = getWorkingCopy(xContext, comp);
	
	File wcDir = new File(workingCopy);
	if (! wcDir.exists())
	    return "ERROR no directory at " + workingCopy;
	
	Subversion svn = new Subversion(xContext);
	svn.svnInfo(xContext, workingCopy);
	
	String[] tokens = svn.getUrl().split("[/]+");
	boolean bFound = false;
	for (String token : tokens) {
	    if (! bFound && token.equals(comp.getName())) {
		bFound = true;
		continue;
	    }
	    if (! bFound)
		continue;
	    if (token.startsWith("branch"))
	     	continue;
	    return token;
	}
	
	return null;
	
    }


    /**
     * Query the database for the branch for this TK and component
     *
     * @param xContext
     * @param xComp
     * @return
     * @throws IcofException 
     */
    private String getDatabaseBranch(EdaContext xContext, Component_Db xComp) 
    throws IcofException {

	Component_Version_Db compVer;
	compVer = new Component_Version_Db(xContext, getToolKit().getToolKit(), 
	                                   xComp);
	compVer.dbLookupByCompRelVersion(xContext);
	
	Branch branch = new Branch(xContext, null, compVer);
	branch.dbLookupByCompVersion(xContext);
	branch.getDbObject().getBranchName().dbLookupById(xContext);
	
	return branch.getDbObject().getBranchName().getName();
	
    }


    /**
     * Set the SVN working copy path
     *
     * @param xContext
     * @param comp
     */
    private String getWorkingCopy(EdaContext xContext, Component_Db comp) {

	return "/afs/eda/build/" + comp.getName() + File.separator + 
	        getToolKit().getToolKit().getRelease().getName();	
	
    }


    /**
     * Create a collection of components to process
     *
     * @param xContext
     * @throws IcofException 
     */
    private void setComponents(EdaContext xContext) throws IcofException {

	if (getComponent() == null) {
	    getToolKit().setComponents(xContext);
	    components = getToolKit().getComponents();
	}
	else {
	    components = new Vector<Component_Db>();
	    components.add(getComponent().getComponent());
	}
	
    }


    protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-t");
	argSwitches.add("-c");

    }


    protected String readParams(Hashtable<String,String> params, String errors,
                                EdaContext xContext) throws IcofException {

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

	return errors;

    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	boolean verboseInd = getVerboseInd(xContext);
	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, verboseInd);
	logInfo(xContext, "ToolKit    : " + getToolKit().getToolKit().getDisplayName(), verboseInd);
	if (getComponent() != null)
	    logInfo(xContext, "Component  : " + getComponent(), verboseInd);
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
	usage.append("Verifies the branch associated with the SVN working copy\n");
	usage.append("matches the branch registered in the database.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t toolkit> [-c component] \n");
	usage.append("               [-y] [-h] -db dbMode]\n");
	usage.append("\n");
	usage.append("  toolkit     = ToolKit name (14.1.0, 14.1.1 ...).\n");
	usage.append("  component   = Process just this Component name (ess, pds, model, einstimer ...).\n");
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
    Vector<Component_Db> components;
    
    
    /**
     * Getters.
     */
    public Vector<Component_Db> getComponents() {return components; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


    /**
     * Setters.
     */



    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

}
