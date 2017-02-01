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
 * Clones a new ToolKit from an existing ToolKit. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/17/2011 GFS  Initial coding.
 * 09/08/2011 GFS  Updated copyToolKit() to work with new methods in ToolKit.
 * 11/07/2012 GFS  Updated to support creating xtinct TKs.
 * 11/27/2012 GFS  Refactored to use business objects and support all flavors
 *                 of the tool kit name.
 * 07/14/2015 GFS  Updated to support creating the release and making component
 *                 cloning selectable.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.StageName_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Release;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class ToolKitDuplicator extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "toolKitClone";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     *
     * @param aContext       Application context
     * @param aToolKit       Existing TK to be cloned
     * @param newName        New ToolKit name
     * @param aUser          Person cloning the Tool Kit
     * @param bClone         If true clone the comps from old tk to new tk 
     *                       otherwise create the rel/tk only
     */
    public ToolKitDuplicator(EdaContext aContext,  ToolKit aToolKit, 
                             String newName, User_Db aUser, boolean bClone)
                             throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setToolKit(aToolKit);
	setNewName(newName);
	setUser(aUser);
	setCloneFlag(bClone);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ToolKitDuplicator(EdaContext aContext) throws IcofException {

	this(aContext, null, "", null, false);

    }


    /**
     * Instantiate the class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new ToolKitDuplicator(null);
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

	// Copy the existing Tool Kit into the new Tool Kit
	copyToolKit(xContext);
	commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
    }


    /**
     * Copy the existing to the new Tool Kit
     * @param xContext
     * @throws IcofException 
     */
    private void copyToolKit(EdaContext xContext) throws IcofException {

	// Set the new release and tool kit
	setNewRelease(xContext);
	setNewToolKit(xContext);

	// Copy data from the existing ToolKit to new ToolKit
	if (cloneFlag) 
	    newToolKit.dbCopy(xContext, getToolKit(), getUser(), null);
	else 
	    logInfo(xContext, "Warning: no components not cloned to new TK!", true);

    }


    /**
     * Create the new Tool Kit
     *
     * @param xContext Application context
     * @throws IcofException 
     */
    private void setNewToolKit(EdaContext xContext) throws IcofException {

	// If the new ToolKit exists then exit with errors
	newToolKit = new ToolKit(xContext, getNewName(),
	                         StageName_Db.STAGE_READY,
	                         getCqName(), getDescription(),
	                         getToolKit().getToolKit().getId());
	try {
	    getToolKit().dbLookupByName(xContext);
	    logInfo(xContext, "Warning: new ToolKit already exists.", true);
	    return;
	}
	catch(IcofException ie) {
	    logInfo(xContext, "New TK name not found in DB - proceeding ...", 
	            verboseInd);

	    // Add new Tool Kit to database.
	    boolean addedTk = getToolKit().dbAdd(xContext, getUser());
	    if (! addedTk) {
		IcofException ie2 = new IcofException(this.getClass().getName(),
		                                     "copyToolKit()",
		                                     IcofException.SEVERE,
		                                     "Unable to add new ToolKit",
		                                     getNewName());
		throw ie2;

	    }
	}
	
    }


    /**
     * Lookup the new tool kit release or create a new one
     *
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void setNewRelease(EdaContext xContext) 
    throws IcofException {

	// Create the new Release name
	String releaseName = getNewName().substring(0, 
	                                            getNewName().lastIndexOf("."));
	String majorName = releaseName.substring(0, releaseName.indexOf("."));
	int majName = Integer.valueOf(majorName);
	String altRelName = String.valueOf(majName + 4);
	altRelName += "." + releaseName.substring(releaseName.lastIndexOf("."));
	newRelease = new Release(xContext, releaseName,
	                                 altRelName);
	try {
	    newRelease.dbLookupByName(xContext);
	    logInfo(xContext, "The TK Release already exists - " + 
	    	releaseName + ".\n", true);
	}
	catch(IcofException ie) {
	    try {
		logInfo(xContext, "Creating TK Release: " + releaseName, true);
		newRelease.dbAdd(xContext, getUser());
		
	    }
	    catch(IcofException ie2) {
		logInfo(xContext, "Unable to create new TK Release", true);
		throw ie2;
	    }

	}
	
    }


    protected String readParams(Hashtable<String,String> params, String errors,
                                EdaContext xContext) throws IcofException {
	// Read the Component name
	if (params.containsKey("-t")) {
	    setToolKit(xContext, params.get("-t"));
	}
	else {
	    errors += "Tool Kit (-t) is a required parameter\n";
	}

	// Read the new Tool Kit name
	if (params.containsKey("-n")) {
	    setNewName(params.get("-n"));
	}
	else {
	    errors += "New Tool Kit name (-n) is a required parameter\n";
	}

	// Read the ClearQuest Tool Kit name
	if (params.containsKey("-cqtk")) {
	    setCqName(params.get("-cqtk"));
	} 
	else {
	    errors += "ClearQuest Tool Kit name (-cqtk) is a required parameter\n";
	}

	// Read the new Tool Kit description
	if (params.containsKey("-d")) {
	    setDescription(params.get("-d"));
	}
	else {
	    errors += "Description (-d) is a required parameter\n";
	}
	
	// Read the no clone switch
	setCloneFlag(true);
	if (params.containsKey("-noclone")) {
	    setCloneFlag(false);
	}

	return errors;
    }


    protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
	singleSwitches.add("-y");
	singleSwitches.add("-noclone");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-t");
	argSwitches.add("-n");
	argSwitches.add("-cqtk");
	argSwitches.add("-d");
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	boolean verboseInd = getVerboseInd(xContext);
	logInfo(xContext, "App         : " + APP_NAME + "  " + APP_VERSION, verboseInd);
	logInfo(xContext, "ToolKit     : " + getToolKit().getName(), verboseInd);
	logInfo(xContext, "New Tool Kit: " + getNewName(), verboseInd);
	logInfo(xContext, "New CQ name : " + getCqName(), verboseInd);
	logInfo(xContext, "Description : " + getDescription(), verboseInd);
	logInfo(xContext, "Parent ID   : " + getToolKit().getToolKit().getId(), verboseInd);
	logInfo(xContext, "Clone TK    : " + getCloneFlag(), verboseInd);
	logInfo(xContext, "DB Mode     : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose     : " + getVerboseInd(xContext), verboseInd);
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Clones a new Tool Kit from an existing Tool Kit.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t tool_kit> <-n new_tk_name> \n");
	usage.append("             <-cqtk cq_rel_name> <-d description>\n");
	usage.append("             [-noclone] [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  tool_kit    = Existing Tool Kit (14.1.0, 14.1.1 ...).\n");
	usage.append("  new_tk_name = Name of new TKt (14.1.1, 14.1.2 ...)\n");
	usage.append("  cq_rel_name = ClearQuest name of new TK (14.1.2 (dev) ...)\n");
	usage.append("  description = Description for new TK\n");
	usage.append("  -y          = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode      = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -noclone    = (optional) Create the tk but don't clone the comps\n");
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
    private String newName;
    private String cqName;
    private String description;
    private Release newRelease;
    private ToolKit newToolKit;
    private boolean cloneFlag = true;


    /**
     * Getters.
     */
    public String getNewName() { return newName; }
    public String getCqName() { return cqName; }
    public String getDescription() { return description; }
    public Release getRelease() { return newRelease; }
    public ToolKit getToolKit() { return newToolKit; } 
    public boolean getCloneFlag() { return cloneFlag; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}



    /**
     * Setters.
     */
    private void setNewName(String aName) { newName = aName;  }
    private void setCqName(String aName) { cqName = aName;  }
    private void setCloneFlag(boolean aFlag) { cloneFlag = aFlag;  }
    private void setDescription(String aDesc) { description = aDesc;  }



    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }


}
