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
 * Add an existing component update to the given tk, component and location
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 01/30/2014 GFS  Initial coding.
 * 02/06/2014 GFS  Added support for using a revision instead of the 
 *                 ComponentUpdate id which most users would not know
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.sql.Timestamp;
import java.util.Hashtable;
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

public class ComponentUpdateCreate extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "compUp.create";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aTk       Tool kit object
     * @param aComp     Component object
     * @param aLoc      Location object
     * @param aMaxRevision Revision string 
     * 
     */
    public ComponentUpdateCreate(EdaContext aContext, ToolKit aTk,
                                 Component aComp, Location aLoc,
                                 int aMaxRevision) 
                                 throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setToolKit(aTk);
	setComponent(aComp);
	setLocation(aLoc);
	setMaxRevision(aMaxRevision);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * 
     * @exception IcofException Unable to construct ManageApplications object
     */
    public ComponentUpdateCreate(EdaContext aContext) throws IcofException {

	this(aContext, null, null, null, 0);
    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new ComponentUpdateCreate(null);
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

	connectToDB(xContext);

	boolean bCreated = create(xContext);

	if (bCreated)
	    commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
	else
	    rollBackDBAndSetReturncode(xContext, APP_NAME, SUCCESS);

    }


    /**
     * Read Component Updates from the DB for this tk, comp and location
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private boolean create(EdaContext xContext)
    throws IcofException {

	// Lookup the Component_TkVersion
	cv = new Component_Version_Db(xContext, getToolKit().getToolKit(),
	                              getComponent().getComponent());
	cv.dbLookupByCompRelVersion(xContext);

	
	// Look up un-extracted CodeUpdates for this TK, comp and max rev
	CodeUpdate_Db cu = new CodeUpdate_Db(getCompVersion(), "", "", "",
	                                     null, null);
	Vector<CodeUpdate_Db> codeUpdates = cu.dbLookupNotExtracted(xContext);
	logInfo(xContext, "Found " + codeUpdates.size() + " unextracted CodeUpdates", 
	        verboseInd);
	
	if (codeUpdates.size() <= 0) {
	    logInfo(xContext, "No CodeUpdates found to add to new ComponentUpdate",
	            verboseInd);
	    return false;
	}
	
	// Create the ComponentUpdate record for the extracted CodeUpdates.
	logInfo(xContext, "Creating new ComponentUpdate ...", verboseInd);
	ComponentUpdate_Db compUpdate = new ComponentUpdate_Db((long) 0);
	compUpdate.dbAddRow(xContext, getUser());
	logInfo(xContext, " ComponentUpdate: " + compUpdate.toString(xContext),
	        verboseInd);
	logInfo(xContext, " ComponentUpdate created.", verboseInd);

	
	// Update the extract data on each extracted CodeUpdate
	logInfo(xContext, "Updating extract data on CodeUpdates ...",
	        verboseInd);
	Timestamp extractTimestamp = new Timestamp(new java.util.Date().getTime());
	boolean bFound = false;
	for (CodeUpdate_Db update : codeUpdates) {
	    int updateRev = Integer.parseInt(update.getRevision());
	    if (updateRev > getMaxRevision())
		continue;
	    bFound = true;
	    update.dbUpdateExtractData(xContext, compUpdate,
	                                 extractTimestamp, getUser());
	    logInfo(xContext, " Updating CodeUpdate (" + update.getRevision()
	            + ")", verboseInd);
	}
	if (! bFound) {
	    logInfo(xContext, "No CodeUpdates <= max revision ",
	            verboseInd);
	    return false;
	}
	
	// Create the comp ver location
	logInfo(xContext, "Adding new Component_Version_Location ...",
	        verboseInd);
	Component_Version_Location_Db aCVL;
	aCVL = new Component_Version_Location_Db(cv, getLocation().getLocation());
	try {	
	    aCVL.dbLookupByIds(xContext);
	}	
	catch (IcofException ex) {
	    aCVL.dbAddRow(xContext);
	}


	// Update this location with the newly extracted CodeUpdates.
	logInfo(xContext, "Adding new ComponentUpdate to this location ...",
	        verboseInd);
	Location_ComponentUpdate_Db locUpdate = new Location_ComponentUpdate_Db(aCVL,
	                                                                        compUpdate);
	locUpdate.dbAddRow(xContext, getUser());
	logInfo(xContext, " New ComponentUpdate added", verboseInd);

	return true;
	
    }

 
    /**
     * Define valid command line options
     * 
     * @param singleSwitches Collection of single switches
     * @param argSwitches Collection of switches that take arg
     */
    protected void createSwitches(Vector<String> singleSwitches,
                                  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-t");
	argSwitches.add("-c");
	argSwitches.add("-l");
	argSwitches.add("-max");
    }


    /**
     * Load application objects from command line parameters
     * 
     * @param params Collection of command line strings
     * @param errors String to store any error messages
     * @param xContext Application context
     * @throws IcofException
     */
    protected String readParams(Hashtable<String, String> params,
                                String errors, EdaContext xContext)
                                throws IcofException {

	if (params.containsKey("-t"))
	    setToolKit(xContext, (String) params.get("-t"));
	else
	    errors += "Tool Kit (-t) is a required parameter.";

	if (params.containsKey("-c"))
	    setComponent(xContext, (String) params.get("-c"));
	else
	    errors += "Component (-c) is a required parameter.";

	if (params.containsKey("-l")) {
	    setLocation(xContext, (String) params.get("-l"));
	    getLocation().dbLookupByName(xContext);
	}
	else
	    errors += "Location (-l) is a required parameter.";

	if (params.containsKey("-max"))
	    setMaxRevision(Integer.parseInt(params.get("-max")));
	else
	    errors += "Max revision (-max) is a required parameter.";

	return errors;

    }


    /**
     * Display command line parameters
     * 
     * @param dbMode   Database mode
     * @param xContext Application context
     */
    protected void displayParameters(String dbMode, EdaContext xContext) {

	boolean verboseInd = getVerboseInd(xContext);
	logInfo(xContext, "App      : " + APP_NAME + "  " + APP_VERSION,
	        verboseInd);
	logInfo(xContext, "Tool Kit : " + getToolKit().getName(), verboseInd);
	logInfo(xContext, "Component: " + getComponent().getName(), verboseInd);
	logInfo(xContext, "Location : " + getLocation().getName(), verboseInd);
	logInfo(xContext, "Max Rev  : " + getMaxRevision(), verboseInd);
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
	usage.append("Creates a new ComponentUpdate for the specified Tool Kit,\n");
	usage.append("Component and Location from all unextracted CodeUpdates\n");
	usage.append("less than or equal to the max revision.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t toolkit> <-c component> <-l location>\n");
	usage.append("              <-max revision>\n");
	usage.append("              [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  toolkit   = A tool kit name like 14.1.6\n");
	usage.append("  component = A component name like edif\n");
	usage.append("  location  = A location like build, prod, shipb \n");
	usage.append("  revision  = Revision to add to TK, Comp and location\n");
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
    private ComponentUpdate_Db componentUpdate;
    private static boolean requestHelp = false;
    private Component_Version_Db cv;    
    private int revision;


    /**
     * Getters.
     */
    public ComponentUpdate_Db getComponentUpdate() { return componentUpdate; }
    public static boolean getRequestHelp() { return requestHelp; }
    public Component_Version_Db getCompVersion() { return cv; }
    public int getMaxRevision() { return revision; }
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }


    /**
     * Setters.
     */
    private void setMaxRevision(int aRev) { revision = aRev; }
    // @formatter:on


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {

	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }

}
