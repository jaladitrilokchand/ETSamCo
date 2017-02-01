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
 * Add a new role to the EdaTkRole or ComTkRelRole table. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 01/30/2014 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.ComponentUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Location_Db;
import com.ibm.stg.eda.component.tk_etreedb.Location_ComponentUpdate_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.Location;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class ComponentUpdateShow extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "compUp.show";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aTk   Tool kit object
     * @param aComp Component object
     * @param aLoc  Location object
     * 
     */
    public ComponentUpdateShow(EdaContext aContext, ToolKit aTk,
				Component aComp, Location aLoc)
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setToolKit(aTk);
	setComponent(aComp);
	setLocation(aLoc);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * 
     * @exception IcofException Unable to construct ManageApplications object
     */
    public ComponentUpdateShow(EdaContext aContext) throws IcofException {

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

	    myApp = new ComponentUpdateShow(null);
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
     * Lookup Component updates
     * 
     * @param aContext Application Context
     * @throws IcofException
     */
    public void process(EdaContext xContext)
    throws IcofException {

	// Connect to the database
	connectToDB(xContext);

	readFromDb(xContext);
	show(xContext);

	setReturnCode(xContext, SUCCESS);
	commitToDB(xContext, APP_NAME);

    }


    /**
     * Display Component Updates 
     *
     * @param xContext Application context
     * @throws IcofException 
     */
    private void show(EdaContext xContext) throws IcofException {
	
	// Display the header
	if (getCompUpdates().isEmpty()) {
	    logInfo(xContext, "No component updates/revisions found", true);
	    return;
	}

	logInfo(xContext, "", true);
	String line = formatLine(xContext, "Component", "");
	logInfo(xContext, line, true);

	line = formatLine(xContext, "Update Id", "Revisions");
	logInfo(xContext, line, true);

	line = formatLine(xContext, "---------", "---------");
	logInfo(xContext, line, true);
	
	// Display component update data
	Iterator<ComponentUpdate_Db> iter = getCompUpdates().iterator();
	while (iter.hasNext()) {
	    
	    ComponentUpdate_Db cu = (ComponentUpdate_Db)iter.next();

	    // Skip process if latestFlag and not the last element
	    if (isLatest() && iter.hasNext()) {
		continue;
	    }
	    
	    CodeUpdate_Db update = new CodeUpdate_Db(getCompVersion(), 
	                                             "", "", "", cu, null, 
	                                             "", null, "", null);
	    Vector<CodeUpdate_Db> updates = update.dbLookupByCompUpdate(xContext);
	    
	    String revisions = getRevisons(xContext, updates);

	    line = formatLine(xContext, String.valueOf(cu.getId()), revisions);
	    
	    if (isLatest()) {
		if (! iter.hasNext()) {
		    logInfo(xContext,  line, true);
		}
	    }
	    else {
		logInfo(xContext,  line, true);
	    }
	}
	
    }


    /**
     * Create a list of revisions for the code updates
     *
     * @param xContext  Application context
     * @param updates   Collection of code updates
     * @return
     */
    private String getRevisons(EdaContext xContext,
                               Vector<CodeUpdate_Db> updates) {

	String revisions = "";

	Iterator<CodeUpdate_Db> iter2 = updates.iterator();
	while (iter2.hasNext()) {
	    CodeUpdate_Db codeUpdate = (CodeUpdate_Db)iter2.next();
	    if (revisions != "")
		revisions += ", ";
	    revisions += codeUpdate.getRevision();
	}

	return revisions;

    }


    /**
     * Constructs a nicely formatted line for display
     *
     * @param xContext
     * @param col1 Text for 1st column
     * @param col2 Text for 2nd column
     * @return
     */
    private String formatLine(EdaContext xContext, String col1, String col2) {
	
	return IcofStringUtil.padString(col1, 12, " ") + col2;
	
    }


    /**
     * Read Component Updates from the DB for this tk, comp and location
     *
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void readFromDb(EdaContext xContext) throws IcofException {

	// Lookup the Component_TkVersion
	cv = new Component_Version_Db(xContext, getToolKit().getToolKit(), 
	                              getComponent().getComponent());
	cv.dbLookupByCompRelVersion(xContext);
	logInfo(xContext, "Component Version ID: " + cv.getId(), verboseInd);
		
	// Lookup the Component_TkVersion_Location
	Component_Version_Location_Db cvl;
	cvl = new Component_Version_Location_Db(cv, 
	                                        getLocation().getLocation());
	cvl.dbLookupByIds(xContext);
	
	// Lookup the ComponentUpdates
	Location_ComponentUpdate_Db lcu;
	lcu = new Location_ComponentUpdate_Db(cvl, null);
	
	cus = lcu.dbLookupComponentUpdates(xContext);

    }


    /**
     * Define valid command line options
     * 
     * @param singleSwitches Collection of single switches
     * @param argSwitches    Collection of switches that take arg
     */
    protected void createSwitches(Vector<String> singleSwitches,
				  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	singleSwitches.add("-latest");
	argSwitches.add("-db");
	argSwitches.add("-t");
	argSwitches.add("-c");
	argSwitches.add("-l");
    }


    /**
     * Load application objects from command line parameters
     * 
     * @param params   Collection of command line strings
     * @param errors   String to store any error messages
     * @param xContext Application context
     * @throws IcofException 
     */
    protected String readParams(Hashtable<String, String> params,
                                String errors, EdaContext xContext) 
                                throws IcofException {

	if (params.containsKey("-t")) {
	    setToolKit(xContext, (String) params.get("-t"));
	}
	else {
	    errors += "Tool Kit (-t) is a required parameter.";
	}

	if (params.containsKey("-c")) {
	    setComponent(xContext, (String) params.get("-c"));
	}
	else {
	    errors += "Component (-c) is a required parameter.";
	}

	if (params.containsKey("-l")) {
	    setLocation(xContext, (String) params.get("-l"));
	}
	else {
	    errors += "Location (-l) is a required parameter.";
	}
	
	setLatest(false);
	if (params.containsKey("-latest")) {
	    setLatest(true);
	}
	
	return errors;
	
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {

	boolean verboseInd = getVerboseInd(xContext);
	logInfo(xContext, "App      : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	logInfo(xContext, "Tool Kit : " + getToolKit().getName(), verboseInd);
	logInfo(xContext, "Component: " + getComponent().getName(), verboseInd);
	logInfo(xContext, "Location : " + getLocation().getName(), verboseInd);
	logInfo(xContext, "Latest   : " + isLatest(), verboseInd);
	logInfo(xContext, "Mode     : " + dbMode, verboseInd);
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
	usage.append("Displays Component Updates and revisions for the given\n");
	usage.append("Tool Kit, Component and Location.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t toolkit> <-c component> <-l location>\n");
	usage.append("            [-latest] [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  toolkit   = A tool kit name like 14.1.6\n");
	usage.append("  component = A component name like edif\n");
	usage.append("  location  = A location like build, prod, shipb \n");
	usage.append("  -latest   = Display the latest component update only\n");
	usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  -h        = Help (shows this information)\n");
	usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("\n");
	usage.append("------------------------------------------------------\n");

	System.out.println(usage);

    }


    /**
     * Members
     * @formatter:off
     */
    private boolean latestFlag;
    private static boolean requestHelp = false;
    private List<ComponentUpdate_Db> cus;
    private Component_Version_Db cv;    
    
    /**
     * Getters.
     */
    public boolean isLatest() { return latestFlag; }
    public static boolean getRequestHelp() { return requestHelp; }
    public List<ComponentUpdate_Db> getCompUpdates() { return cus; }
    public Component_Version_Db getCompVersion() { return cv; }
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }

    
    /**
     * Setters.
     */
    private void setLatest(boolean aFlag) { latestFlag = aFlag;  }
    // @formatter:on


    /**
     * Set the Location object
     * 
     * @param xContext Application context.
     * @param aName Location name
     * @throws IcofException
     */
    protected void setLocation(EdaContext xContext, String aName)
    throws IcofException {

	if (getLocation() == null) {
	    location = new Location(xContext, aName.toUpperCase());
	    location.dbLookupByName(xContext);
	}
	logInfo(xContext,
		"Location: " + getLocation().getName(), false);
    }



    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }

}
