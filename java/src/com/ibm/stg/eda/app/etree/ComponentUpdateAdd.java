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

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.ComponentUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Location_Db;
import com.ibm.stg.eda.component.tk_etreedb.LocationEventName_Db;
import com.ibm.stg.eda.component.tk_etreedb.LocationEvent_Db;
import com.ibm.stg.eda.component.tk_etreedb.Location_ComponentUpdate_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.Location;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class ComponentUpdateAdd extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "compUp.add";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aTk Tool kit object
     * @param aComp Component object
     * @param aLoc Location object
     * @param anUpdate ComponentUpdate object to add
     * 
     */
    public ComponentUpdateAdd(EdaContext aContext, ToolKit aTk, ToolKit aSrcTk,
			      Component aComp, Location aLoc,
			      ComponentUpdate_Db anUpdate) throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setToolKit(aTk);
	setSrcToolKit(aSrcTk);
	setComponent(aComp);
	setLocation(aLoc);
	setComponentUpdate(anUpdate);

    }
    

    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aTk       Tool kit object
     * @param aComp     Component object
     * @param aLoc      Location object
     * @param aRevision Revision string 
     * 
     */
    public ComponentUpdateAdd(EdaContext aContext, ToolKit aTk,
			      Component aComp, Location aLoc,
			      String aRevision) 
			      throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setToolKit(aTk);
	setComponent(aComp);
	setLocation(aLoc);
	setRevision(aRevision);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * 
     * @exception IcofException Unable to construct ManageApplications object
     */
    public ComponentUpdateAdd(EdaContext aContext) throws IcofException {

	this(aContext, null, null, null, "");
    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new ComponentUpdateAdd(null);
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

	addComponentUpdate(xContext);

	commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
	//rollBackDBAndSetReturncode(xContext, APP_NAME, SUCCESS);

    }


    /**
     * Read Component Updates from the DB for this tk, comp and location
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void addComponentUpdate(EdaContext xContext)
    throws IcofException {

	// Lookup the Component_TkVersion
	cv = new Component_Version_Db(xContext, getToolKit().getToolKit(),
				      getComponent().getComponent());
	cv.dbLookupByCompRelVersion(xContext);

	// Lookup the new Component_TkVersion_Location (add to DB if not found)
	Component_Version_Location_Db cvl;
	cvl = new Component_Version_Location_Db(cv, getLocation().getLocation());
	logInfo(xContext, "CVL --> " + cvl.toString(xContext), verboseInd);
	try {
	    cvl.dbLookupByIds(xContext);
	}
	catch(IcofException trap) {
	    cvl.dbAddRow(xContext);
	}

	// If no ComponentUpdate passed in then look it up from the revision
	if (getComponentUpdate() == null)
	    setComponentUpdateFromRevision(xContext);
	
	// Add the ComponentUpdate to Component_Version_Location
	Location_ComponentUpdate_Db lcu;
	lcu = new Location_ComponentUpdate_Db(cvl, getComponentUpdate());
	try {
	    lcu.dbAddRow(xContext, getUser());
	}
	catch(IcofException trap) {
	    lcu.dbLookupByIds(xContext);
	    logInfo(xContext, "ComponentUpdate already associated with this " +
	    		"TK, Comp and Location", true);
	}
	
	// Add the advance event
	String locName = getLocation().getName().toUpperCase();
	if (locName.indexOf(File.separator) > -1)
	    locName = locName.substring(0, locName.indexOf(File.separator));
	
	LocationEventName_Db eventName;
	eventName = new LocationEventName_Db("ADVANCED_TO_" + locName);
	eventName.dbLookupByName(xContext);
	
	LocationEvent_Db event = new LocationEvent_Db(null, cvl, eventName, "");
	try {
	    event.dbAddRow(xContext, getUser());
	}
	catch(IcofException trap) {
	    logInfo(xContext, "Advance event already logged", true);
	    
	}

	logInfo(xContext, "ComponentUpdate added/verified", true);
	
    }


   
    /**
     * Look up the Component Update for the Component and revision
     *
     * @param xContext    Application context
     * @param compVersion Component_TkVersion
     * @throws IcofException 
     */
    private void setComponentUpdateFromRevision(EdaContext xContext) 
    throws IcofException {

	Component_Version_Db prevCompVer;
	prevCompVer = new Component_Version_Db(xContext, 
	                                       getSrcToolKit().getToolKit(), 
	                                       getComponent().getComponent());
	prevCompVer.dbLookupByCompRelVersion(xContext);
	
	// Look up the code update for the specified revision
	CodeUpdate_Db codeUp = new CodeUpdate_Db(null, null, 
	                                         "", "", null, null, 
	                                         null, null, null, null);
	boolean bFound = false;
	if (getToolKit().getToolKit().getRelease().getId() == 
	    getSrcToolKit().getToolKit().getRelease().getId())
	    bFound = codeUp.dbLookupByCompRev(xContext, 
	                                      getToolKit().getToolKit().getRelease(),
	                                      getComponent().getComponent(),
	                                      getRevision());
	else
	    bFound = codeUp.dbLookupByCompRev(xContext, 
	                                      prevCompVer, 
	                                      getRevision());
	
	
	logInfo(xContext, "CodeUpdate found for revision: " + bFound, verboseInd);
	logInfo(xContext, "CodeUpdate --> " + codeUp.toString(xContext), verboseInd);
	
	// Get the component update from the code update
	long myId = codeUp.getComponentUpdate().getId();
	componentUpdate = new ComponentUpdate_Db(myId);
	
	logInfo(xContext, "Found ComponentUpdate ID: " + myId, verboseInd);
	
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
	argSwitches.add("-cu");
	argSwitches.add("-l");
	argSwitches.add("-r");
	argSwitches.add("-srctk");
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
	
	if (params.containsKey("-srctk"))
	    setSrcToolKit(xContext, (String) params.get("-srctk"));
	else
	    errors += "Source Tool Kit (-srctk) is a required parameter.";

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
	
	if (params.containsKey("-cu"))
	    setComponentUpdate(xContext, (String) params.get("-cu"));
	
	if (params.containsKey("-r"))
	    setRevision((String) params.get("-r"));
	
	// Usage error checking
	if (getComponentUpdate() == null && getRevision() == null) {
	    errors += "ComponentUpdate (-cu) or Revision (-r) must be specified.";
	}
	if (getComponentUpdate() != null && getRevision() != null) {
	    errors += "Either ComponentUpdate (-cu) or Revision (-r) must be " +
	    "specified but not both.";
	}

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
	logInfo(xContext, "App          : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	logInfo(xContext, "Tool Kit     : " + getToolKit().getName(), verboseInd);
	logInfo(xContext, "Src Tool Kit : " + getSrcToolKit().getName(), verboseInd);
	logInfo(xContext, "Component    : " + getComponent().getName(), verboseInd);
	logInfo(xContext, "Location     : " + getLocation().getName(), verboseInd);
	if (getComponentUpdate() != null)
	    logInfo(xContext, "CompUp ID    : " + getComponentUpdate().getId(),
	            verboseInd);
	else
	    logInfo(xContext, "CompUp ID    : NULL", verboseInd);
	logInfo(xContext, "Revision     : " + getRevision(), verboseInd);
	logInfo(xContext, "Mode         : " + dbMode, verboseInd);
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
	usage.append("Adds a ComponentUpdate to the specified Tool Kit,\n");
	usage.append("Component and Location. User can specify a specific\n");
	usage.append("ComponentUpdate via its database id or specify a revision\n");
	usage.append("to look up the ComponentUpdate to be added.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t toolkit> <-c component> <-l location>\n");
	usage.append("           <-cu comp_up_id | -r revision>\n");
	usage.append("           <-srctk srcToolKit>\n");
	usage.append("           [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  toolkit   = A tool kit name like 14.1.6\n");
	usage.append("  component = A component name like edif\n");
	usage.append("  location  = A location like build, prod, shipb \n");
	usage.append("  comp_up_id= Id of ComponentUpdate row to add\n");
	usage.append("  revision  = Revision to add to TK, Comp and location\n");
	usage.append("  srcToolKit= Tool Kit that contains the CompUp/Rev being added\n");
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
    private List<ComponentUpdate_Db> cus;
    private Component_Version_Db cv;    
    private String revision;
    private ToolKit srcToolKit;
    
    
    /**
     * Getters.
     */
    public ComponentUpdate_Db getComponentUpdate() { return componentUpdate; }
    public static boolean getRequestHelp() { return requestHelp; }
    public List<ComponentUpdate_Db> getCompUpdates() { return cus; }
    public Component_Version_Db getCompVersion() { return cv; }
    public String getRevision() { return revision; }
    public ToolKit getSrcToolKit() { return srcToolKit; }
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }

    
    /**
     * Setters.
     */
    private void setSrcToolKit(ToolKit aTk) { srcToolKit = aTk; }
    private void setComponentUpdate(ComponentUpdate_Db aCu) { componentUpdate = aCu;  }
    private void setRevision(String aRev) { revision = aRev; }
    // @formatter:on


    /**
     * Set the ComponentUpdate object
     * 
     * @param xContext Application context.
     * @param anId ComponentUpdate id
     * @throws IcofException
     */
    protected void setComponentUpdate(EdaContext xContext, String anId)
    throws IcofException {

	if (getComponentUpdate() == null) {
	    componentUpdate = new ComponentUpdate_Db(Long.parseLong(anId));
	    componentUpdate.dbLookupById(xContext);
	}
	logInfo(xContext,
		"ComponentUpdate: " + getComponentUpdate().toString(xContext),
		false);
    }


    /**
     * Set the RelVersion_db object from the tool kit name
     * 
     * @param xContext Application context.
     * @param aToolKit Kit name like 14.1.1, 14.1.2 ...
     * @throws IcofException
     */
    protected void setSrcToolKit(EdaContext xContext, String aName)
    throws IcofException {

	try {
	    if (getSrcToolKit() == null) {
		srcToolKit = new ToolKit(xContext, aName.trim());
		srcToolKit.dbLookupByName(xContext);
		srcToolKit.getToolKit().getStageName().dbLookupById(xContext);
	    }
	    logInfo(xContext, "Src Tool Kit: " + getSrcToolKit().toString(xContext),
		    isVerbose(xContext));
	}
	catch (IcofException ex) {
	    logInfo(xContext, "Source ToolKit (" + aName
			      + ") was not found in the database.", false);
	    throw ex;
	}

    }

    
    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {

	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }

}
