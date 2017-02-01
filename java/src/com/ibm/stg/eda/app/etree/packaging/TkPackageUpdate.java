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
import com.ibm.stg.eda.component.tk_etreeobjs.EventName;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKitPackage;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class TkPackageUpdate extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "pkg.updateTkPkg";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aPkg Tool k package to update
     * @param anEvent New EventName (state)
     */
    public TkPackageUpdate(EdaContext aContext, ToolKitPackage aPkg,
			   EventName anEvent, String sComments)
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setTkPackage(aPkg);
	setComments(sComments);
	setEventName(anEvent);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * @exception IcofException Unable to construct ManageApplications object
     */
    public TkPackageUpdate(EdaContext aContext) throws IcofException {

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
	    myApp = new TkPackageUpdate(null);
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

	getTkPackage().updateState(xContext, getEventName(), getComments(), 
	                           getUser());
	logInfo(xContext, "Tool kit package state updated to "
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
	if (params.containsKey("-t")) {
	    String pkgName = (String) params.get("-t");
	    if (IcofStringUtil.occurrencesOf(pkgName, ".") != 3) {
		errors += "Tool kit maintenance name must be in " +
				"x.y.z.a (14.1.6.0) format.\n";
	    }
	    else {
		setToolKitPackage(xContext, (String) params.get("-t"));
	    }
	}
	else {
	    errors += "Tool kit maintenance name (-t) is a required parameter\n";
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
	singleSwitches.add("-q");
	argSwitches.add("-db");
	argSwitches.add("-t");
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

	logInfo(xContext, "App          : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	logInfo(xContext, "Event name   :" + getEventName().getName(), verboseInd);
	logInfo(xContext, "TK Package ID: " + getTkPackage().getDbObject().getId(),
		verboseInd);
	logInfo(xContext, "Comments     : " + getComments(), verboseInd);
	logInfo(xContext, "DB Mode      : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose      : " + getVerboseInd(xContext), verboseInd);
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Update the given Tool Kit maintenance to the new state. \n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t toolkit> <-s state> [-c comments]\n");
	usage.append("                [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  toolkit  = Tool kit name including maint (14.1.6.0 ...)\n");
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
    private ToolKitPackage pkg;


    /**
     * Getters
     */
    public String getComments() { return comments; }
    public EventName getEventName()  { return eventName; }
    public ToolKitPackage getTkPackage() { return pkg; }


    /**
     * Setters
     */
    private void setComments(String aName) { comments = aName; }
    private void setEventName(EventName aName) { eventName = aName; }
    private void setTkPackage(ToolKitPackage aPkg) { pkg = aPkg; }
    // @formatter:on

    
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
		true);
    }


    /**
     * Set the Request object
     * 
     * @param xContext Application context.
     * @param anId Request id
     * @throws IcofException
     */
    protected void setToolKitPackage(EdaContext xContext, String aName)
    throws IcofException {

	// Strip off the tool kit name and lookup the tool kit
	String tkName = aName.substring(0, aName.lastIndexOf("."));
	setToolKit(xContext, tkName);
	
	logInfo(xContext,
		"Tool Kit: " + getToolKit().toString(xContext),
		true);
	
	// Set the maintenance
	String maintName = aName.substring(aName.lastIndexOf(".") + 1);
	
	if (getTkPackage() == null) {
	    pkg = new ToolKitPackage(xContext, maintName);
	    getTkPackage().dbLookupByName(xContext, getToolKit());
	}
	logInfo(xContext,
		"Tool Kit Package: " + getTkPackage().toString(xContext),
		true);
    }


    protected String getAppName() {

	return APP_NAME;
    }


    protected String getAppVersion() {

	return APP_VERSION;
    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {

	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }

}
