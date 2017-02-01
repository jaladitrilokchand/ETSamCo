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
 * Update a Tool Kit Package a given state. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 01/13/2014 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree.packaging;

import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.ComponentPackage;
import com.ibm.stg.eda.component.tk_etreeobjs.EventName;
import com.ibm.stg.iipmds.common.IcofException;

public class CompPackageUpdate extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "compPkg.update";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aPkg Tool k package to update
     * @param anEvent New EventName (state)
     */
    public CompPackageUpdate(EdaContext aContext, ComponentPackage aPkg,
			     EventName anEvent, String sComments)
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setPackage(aPkg);
	setComments(sComments);
	setEventName(anEvent);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * @exception IcofException Unable to construct ManageApplications object
     */
    public CompPackageUpdate(EdaContext aContext) throws IcofException {

	this(aContext, null, null, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new CompPackageUpdate(null);
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

	connectToDB(xContext);

	updatePackage(xContext);
	

	commitToDB(xContext, APP_NAME);

    }


    /**
     * Display data for each access request
     * 
     * @param xContext
     * @throws IcofException
     */
    private void updatePackage(EdaContext xContext)
    throws IcofException {

	getPackage().updateState(xContext, getEventName(), getComments(),
				 getUser());
	logInfo(xContext, "Component package state updated to "
			  + getEventName().getName(), true);
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

	// Read the EventName (state) name
	if (params.containsKey("-s")) {
	    setEventName(xContext, (String) params.get("-s"));
	}
	else {
	    errors += "New state (-s) is a required parameter\n";
	}

	// Read the Request name
	if (params.containsKey("-pkg")) {
	    setPackage(xContext, (String) params.get("-pkg"));
	}
	else {
	    errors += "Component package (-pkg) is a required parameter\n";
	}

	// Read the comments
	if (params.containsKey("-c")) {
	    setComments((String) params.get("-c"));
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
	argSwitches.add("-db");
	argSwitches.add("-pkg");
	argSwitches.add("-s");
	argSwitches.add("-c");
    }


    /**
     * Display application's invocation
     * 
     * @param dbMode Database model
     * @param xContext Application context object
     */
    protected void displayParameters(String dbMode, EdaContext xContext) {

	logInfo(xContext, "App         : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	logInfo(xContext, "Event name  :" + getEventName().getName(),
		verboseInd);
	logInfo(xContext,
		"Package ID  : " + getPackage().getDbObject().getId(),
		verboseInd);
	logInfo(xContext, "Comments    : " + getComments(), verboseInd);
	logInfo(xContext, "DB Mode     : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose     : " + getVerboseInd(xContext),
		verboseInd);
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Update the given Component Package to the new state. \n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-pkg comp_pkg> <-s state> [-c comments]\n");
	usage.append("                  [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  comp_pkg = Component package name (ie, pds.18016z.0000.0001.aix61_64)\n");
	usage.append("  state    = New package state (pkg_new, pkg_built, pkg_unpacked, \n");
	usage.append("             pkg_linked, pkg_installed, pkg_construction, pkg_delivered\n");
	usage.append("  comments = event comments (128 char max)\n");
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
    private String comments;
    private EventName eventName;
    private ComponentPackage pkg;


    /**
     * Getters
     */
    public String getComments() { return comments; }
    public EventName getEventName()  { return eventName; }
    public ComponentPackage getPackage() { return pkg; }


    /**
     * Setters
     */
    private void setComments(String aName) { comments = aName; }
    private void setEventName(EventName aName) { eventName = aName; }
    private void setPackage(ComponentPackage aPkg) { pkg = aPkg; }
    // @formatter:on

    protected String getAppName() {

	return APP_NAME;
    }


    protected String getAppVersion() {

	return APP_VERSION;
    }


    /**
     * Set the EventName object
     * 
     * @param xContext Application context.
     * @param aName Event name
     * @throws IcofException
     */
    protected void setEventName(EdaContext xContext, String aName)
    throws IcofException {

	if (getEventName() == null) {
	    eventName = new EventName(xContext, aName.toUpperCase());
	    eventName.dbLookupByName(xContext);
	}
	logInfo(xContext, "EventName: " + getEventName().toString(xContext),
		verboseInd);
    }


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
