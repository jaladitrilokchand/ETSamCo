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
 * Show the Revisions for a given location/toolkit. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 03/09/2011 GFS  Initial coding.
 * 06/09/2011 GFS  Disabled logging. Updated to lookup db objects on the fly.
 * 11/15/2011 GFS  Updated to retrieve a list of SVN revisions from the DB 
 *                 instead of a list of CodeUpdate objects.
 * 01/03/2012 GFS  Updated to use the previous Tool Kit if there are no revisions
 *                 for the current Tool Kit in a location.
 * 02/14/2012 GFS  Updated query to account for static components that are not
 *                 changing from TK to TK.
 * 10/23/2012 GFS  Updated setToolKit to work during AGTS.
 * 11/27/2012 GFS  Refactored to use business objects and support all flavors
 *                 of the tool kit name.
 * 12/18/2012 GFS  Added initial support for xtinct tks.
 * 09/25/2013 GFS  Updated to be run from another Java app.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.Location;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class GetRevisionsTk extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "getRevisionsTk";
    public static final String APP_VERSION = "v1.1";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aComponent A Component object
     * @param aLocation A Location_Db object
     * @param latest If true get latest revision only
     */
    public GetRevisionsTk(EdaContext aContext, ToolKit aTk, 
                          Component aComponent,
			  Location aLocation, boolean lastest)
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setToolKit(aTk);
	setComponent(aComponent);
	setLocation(aLocation);
	setShowLatest(lastest);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * 
     * @exception IcofException Unable to construct ManageApplications object
     */
    public GetRevisionsTk(EdaContext aContext) throws IcofException {

	this(aContext, null, null, null, false);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new GetRevisionsTk(null);
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

	// Look up the Revisions
	lookupResults(xContext, true);

	// Show the results.
	showRevisions(xContext);

	// Set the return code.
	setReturnCode(xContext, SUCCESS);

	rollBackDB(xContext, APP_NAME);

    }


    /**
     * Lookup revisions for this component and location
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    public void lookupResults(EdaContext xContext, boolean useCurrentTk)
    throws IcofException {

	if (! getForce())
	    getToolKit().validateLocation(xContext, getLocation());
	
	setRevisions(xContext);

    }


    /**
     * Determine the revisions for this ToolKit, component and location
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void setRevisions(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "Reading revisions from DB ...", verboseInd);

	// Lookup CodeUpdates (revisions) for this ComponentVersion_Location
	logInfo(xContext, " Querying DB for revisions ...", verboseInd);
	logInfo(xContext, "  Trying .. " + getToolKit().getName(), verboseInd);
	CodeUpdate_Db codeUpdate = new CodeUpdate_Db(null, "", "", "", null,
						     null);
	codeUpdates = codeUpdate.dbLookupRevsByCompVerLoc(xContext,
							  getToolKit().getToolKit(),
							  getComponent().getComponent(),
							  getLocation().getLocation(),
							  getShowLatest());


	// If no revisions then try again without a Tool Kit
	if ((codeUpdates == null) || (codeUpdates.size() < 1)) {
	    logInfo(xContext, "  Trying .. null", verboseInd);
	    codeUpdates = codeUpdate.dbLookupRevsByCompVerLoc(xContext,
							      null,
							      getComponent().getComponent(),
							      getLocation().getLocation(),
							      getShowLatest());
	}

    }


    /**
     * Show all the valid release, version and component combinations.
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void showRevisions(EdaContext xContext)
    throws IcofException {

	// Show the results
	String results = "";
	Iterator<String> iter = getRevisions().iterator();
	while (iter.hasNext()) {
	    String revision = iter.next();
	    if (results.length() < 1)
		results = revision;
	    else
		results += "," + revision;
	}

	// If no revisions found.
	if (getRevisions().size() < 1) {
	    System.out.println("No revisions found for "
			       + getToolKit().getToolKit().getDisplayName()
			       + " " + getComponent().getName() + " in "
			       + getLocation().getName());
	}
	else {
	    System.out.println(results);
	}

    }


    protected void createSwitches(Vector<String> singleSwitches,
				  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	singleSwitches.add("-latest");
	singleSwitches.add("-force");
	argSwitches.add("-db");
	argSwitches.add("-c");
	argSwitches.add("-l");
	argSwitches.add("-t");
    }


    protected String readParams(Hashtable<String, String> params,
				String errors, EdaContext xContext)
    throws IcofException {

	// Read the Tool kit
	if (params.containsKey("-t")) {
	    setToolKit(xContext, params.get("-t"));
	}
	else {
	    errors += "Tool Kit (-t) is a required parameter.";
	}

	// Read the Component
	if (params.containsKey("-c")) {
	    setComponent(xContext, params.get("-c"));
	}
	else {
	    errors += "Component (-c) is a required parameter.";
	}

	// Read the Location
	if (params.containsKey("-l")) {
	    String myLoc = params.get("-l");
	    setLocation(xContext, myLoc.toUpperCase());
	    getLocation().dbLookupByName(xContext);
	}
	else {
	    errors += "Location (-l) is a required parameter.";
	}

	// Read the latest switch.
	setShowLatest(false);
	if (params.containsKey("-latest")) {
	    setShowLatest(true);
	}
	
	// Read the force switch.
	setForce(false);
	if (params.containsKey("-force"))
	    setForce(true);

	return errors;
	
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {

	boolean verboseInd = getVerboseInd(xContext);
	
	logInfo(xContext, "App      : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	
	if (getToolKit() != null)
	    logInfo(xContext, "Tool Kit : " + getToolKit().getName(),
		    verboseInd);
	else
	    logInfo(xContext, "Component: null", verboseInd);

	if (getComponent() != null)
	    logInfo(xContext, "Component: " + getComponent().getName(),
		    verboseInd);
	else
	    logInfo(xContext, "Component: null", verboseInd);

	if (getLocation() != null)
	    logInfo(xContext, "Location : " + getLocation().getName(),
		    verboseInd);
	else
	    logInfo(xContext, "Location : null", verboseInd);
	
	logInfo(xContext, "Latest   : " + getShowLatest(), verboseInd);
	logInfo(xContext, "Force    : " + getForce(), verboseInd);
	logInfo(xContext, "DB Mode  : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose  : " + getVerboseInd(xContext), verboseInd);

    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Displays the revisions for the specified component and\n");
	usage.append("location. To see a single version try -latest swtich.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t toolkit> <-c component> <-l location>\n");
	usage.append("               [-latest] [-force] [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  toolkit   = Tool kit name (14.1.build, 15.1.build, 14.1.10 ...)\n");
	usage.append("  component = Component name (einstimer, model ...)\n");
	usage.append("  location  = Location name (build, dev, prod, shipb, tkb, xtinct/tk14.1.2 ...)\n");
	usage.append("  -latest   = (optional) shows the latest revision only\n");
	usage.append("  -force    = (optional) ignores the location TK stage validation\n");
	usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  -h        = Help (shows this information)\n");
	usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("\n");

	System.out.println(usage);

    }


    /**
     * Members.
     */
    private boolean showLatest = false;
    private boolean force = false;
    private Vector<String> codeUpdates;


    /**
     * Getters.
     */
    public boolean getShowLatest() { return showLatest; }
    public boolean getForce() { return force; }
    public Vector<String> getRevisions() { return codeUpdates; }
    protected String getAppName() { return APP_NAME;  }
    protected String getAppVersion() { return APP_VERSION; }


    /**
     * Setters.
     */
    private void setShowLatest(boolean aFlag) { showLatest = aFlag; }
    private void setForce(boolean aFlag) { force = aFlag; }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {

	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }

}
