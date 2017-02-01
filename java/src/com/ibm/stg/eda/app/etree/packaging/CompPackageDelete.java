/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2014 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *
 *-PURPOSE---------------------------------------------------------------------
 * Delete a Component Package and set any associated CRs to complete state 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/11/2014 GFS  Initial coding
 * 01/23/2015 GFS  Added -skipcrs switch to not reset CR states
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree.packaging;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.ComponentPackage_ComponentTkVersion_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ComponentPackage;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;

public class CompPackageDelete extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "compPkg.delete";
    public static final String APP_VERSION = "v1.1";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aPkg     Component package name to delete
     * @param isSkipCr If true skip CR reset process
     */
    public CompPackageDelete(EdaContext aContext, ComponentPackage aPkg,
                             boolean isSkipCrs)
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setPackage(aPkg);
	setSkipCrs(isSkipCrs);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * @exception IcofException Unable to construct ManageApplications object
     */
    public CompPackageDelete(EdaContext aContext) throws IcofException {

	this(aContext, null, false);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new CompPackageDelete(null);
	    start(myApp, argv, CompPackageDelete.class.getName(), APP_NAME);
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

	connectToDB(xContext);

	deletePackage(xContext);

	commitToDB(xContext, APP_NAME);

    }


    /**
     * Display data for each access request
     * 
     * @param xContext
     * @throws IcofException
     */
    private void deletePackage(EdaContext xContext)
    throws IcofException {

	// Determine the TKs this pkg is associated with
	ComponentPackage_ComponentTkVersion_Db cpcv = 
	new ComponentPackage_ComponentTkVersion_Db(getPackage().getDbObject(), 
	                                           null);
	Vector<Component_Version_Db> compVers = cpcv.dbLookupCompVersions(xContext);
	
	// Delete the package from the database
	String log = getPackage().dbDelete(xContext, isSkipCrs());
	logInfo(xContext, log, true);
	
	// Delete the package from AFS
	for (Component_Version_Db compVer : compVers) {
	    ToolKit aTk = new ToolKit(xContext, compVer.getVersion().getId());
	    deleteFromAFS(xContext, aTk);
	}
	
	logInfo(xContext, "\nComponent package has been deleted", true);
	
    }


    /**
     *
     *
     * @param xContext
     */
    private void deleteFromAFS(EdaContext xContext, ToolKit aTk) {

	IcofFile pkgDir = PkgUtils.getPkgPackagesDir(aTk);
	
	String pkgName = pkgDir.getAbsolutePath() + File.separator + 
	                 getPackage().getName() + ".tar.gz";
	logInfo(xContext, "Removing - " + pkgName, true);

	File pkgFile = new File(pkgName);

	if (pkgFile.exists()) {
	    pkgFile.delete();
	}
	
    }


    /**
     * Parse command line args
     * 
     * @param params Collection of command line args/switches
     * @param errors String to store any error messages
     * @param xContext Application context object
     */
    protected String readParams(Hashtable<String, String> params,
				String errors, EdaContext xContext)
    throws IcofException {

	// Read the Request name
	if (params.containsKey("-pkg")) {
	    String pkgName = (String) params.get("-pkg");
	    if (pkgName.endsWith(".tar.gz"))
		pkgName = pkgName.replace(".tar.gz",  "");
	    setPackage(xContext, pkgName);
	}
	else {
	    errors += "Component package (-pkg) is a required parameter\n";
	}

	// Read skipcrs flag
	setSkipCrs(false);
	if (params.containsKey("-skipcrs")) {
	    setSkipCrs(true);
	}
	
	return errors;

    }


    /**
     * Define application's command line switches
     * 
     * @param singleSwitches Collection of switches
     * @param argSwitches Collection switches/args
     */
    protected void createSwitches(Vector<String> singleSwitches,
				  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	singleSwitches.add("-skipcrs");
	argSwitches.add("-db");
	argSwitches.add("-pkg");

    }


    /**
     * Display application's invocation
     * 
     * @param dbMode Database model
     * @param xContext Application context object
     */
    protected void displayParameters(String dbMode, EdaContext xContext) {

	logInfo(xContext, "App     : " + APP_NAME + "  " + APP_VERSION, verboseInd);
	logInfo(xContext, "Package : " + getPackage().getName(), verboseInd);
	logInfo(xContext, "Skip Crs: " + isSkipCrs(), verboseInd);
	logInfo(xContext, "DB Mode : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose : " + getVerboseInd(xContext), verboseInd);

    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Delete the given Component Package and revert any associated. \n");
	usage.append("Change Requests back to the Complete state (CR processing\n");
	usage.append("can be skipped using -skipcrs switch).\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-pkg comp_pkg> [-skipcrs] [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  comp_pkg = Component package name (ie, pds.18016z.0000.0001.aix61_64)\n");
	usage.append("  -skipcrs = (optional) Don't reset CR state to complete\n");
	usage.append("  -y       = (optional) Quiet mode (no putput headers)\n");
	usage.append("  -y       = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode   = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h       = Help (shows this information)\n");
	usage.append("\n");
	usage.append("Return Codes\n");
	usage.append("------------\n");
	usage.append(" 0 = ok \n");
	usage.append(" 1 = application errors\n");
	usage.append("\n");

	System.out.println(usage);

    }


    /**
     * Data members
     * @formatter:off
     */
    private ComponentPackage pkg;
    private boolean skipCrsFlag = false;


    /**
     * Getters
     */
    public ComponentPackage getPackage() { return pkg; }
    public boolean isSkipCrs() { return skipCrsFlag; }
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }


    /**
     * Setters
     */
    private void setPackage(ComponentPackage aPkg) { pkg = aPkg; }
    private void setSkipCrs(boolean aFlag) { skipCrsFlag = aFlag; }
    // @formatter:on


    /**
     * Set the Package object
     * 
     * @param xContext Application context.
     * @param anId Request id
     * @throws IcofException
     */
    protected void setPackage(EdaContext xContext, String aName)
    throws IcofException {

	if (getPackage() == null) {
	    pkg = new ComponentPackage(xContext, aName);
	}
	logInfo(xContext,
		"Component Package: " + getPackage().toString(xContext), 
		verboseInd);
    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }

}
