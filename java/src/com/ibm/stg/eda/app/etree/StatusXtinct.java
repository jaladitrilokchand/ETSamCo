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
 * Show the xtinct status for a components in Tool Kit 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 12/18/2012 GFS  Initial coding
 * 06/20/2013 GFS  Updated to support custom TKs.
 * 08/13/2013 GFS  Renamed Event objects to LocationEvent.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.CompTkRelRole_User_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Release_Db;
import com.ibm.stg.eda.component.tk_etreedb.LocationEvent_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.LocationEvent;
import com.ibm.stg.eda.component.tk_etreeobjs.TkUserRelCompRole;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;
import com.ibm.stg.iipmds.common.SessionLog;

public class StatusXtinct extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "statusXtinct";
    public static final String APP_VERSION = "v1.1";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aToolKit A ToolKit object
     * @param comps A collection of Component objects
     */
    public StatusXtinct(EdaContext aContext, ToolKit aToolKit,
			Vector<Component_Db> comps) throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setToolKit(aToolKit);
	setComponents(comps);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * 
     * @exception IcofException Unable to construct ManageApplications object
     */
    public StatusXtinct(EdaContext aContext) throws IcofException {

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
	    myApp = new StatusXtinct(null);
	    start(myApp, argv);
	}

	catch (Exception e) {
	    handleExceptionInMain(e);

	}
	finally {
	    handleInFinallyBlock(myApp);
	}

    }


    // --------------------------------------------------------------------------
    /**
     * Add, update, delete, or report on the specified applications.
     * 
     * @param aContext Application Context
     * @throws IcofException
     */
    // --------------------------------------------------------------------------
    public void process(EdaContext xContext)
    throws IcofException {

	// Connect to the database
	connectToDB(xContext);

	// Determine the Components to find status for
	setComponents(xContext);

	// Look up the status for each Component
	Iterator<Component_Db> iter = getComponents().iterator();
	List<String> entries = new ArrayList<String>();
	while (iter.hasNext()) {
	    setComponent(iter.next());
	    setComponentEvents(xContext);
	    entries.add(formatComponentEvents(xContext));
	}
	displayEvents(xContext, entries);

	// Set the return code.
	setReturnCode(xContext, SUCCESS);
	rollBackDB(xContext, APP_NAME);
    }


    /**
     * Display the status for a single component.
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private String formatComponentEvents(EdaContext xContext)
    throws IcofException {

	// Process the AGTS event data
	String owner = getOwner(xContext);
	String status = getXtinctStatus(xContext);
	String completeTms = getCompleteDate(xContext);
	String attemptCount = getAttempts(xContext);

	// Format the event times
	String componentName = getComponentDb().getName();

	// Format the event times
	String entry = formatLine(componentName, owner, status, completeTms,
				  attemptCount);

	return entry;

    }


    /**
     * Determine the number of xtinct start events
     * 
     * @param xContext
     * @return
     */
    private String getAttempts(EdaContext xContext) {

	int attemptCount = 0;

	Iterator<LocationEvent> iter = getEvents().iterator();
	while (iter.hasNext()) {
	    LocationEvent event = iter.next();
	    if (event.getName().equals("XTINCT_START")) {
		attemptCount++;
	    }
	}

	return String.valueOf(attemptCount);
    }


    /**
     * Determine the time of the last event
     * 
     * @param xContext
     * @return status name
     */
    private String getCompleteDate(EdaContext xContext) {

	// Determine the last event
	String completeTms = "---";
	if ((getEvents() != null) && (getEvents().size() > 0)) {
	    LocationEvent event = (LocationEvent) getEvents().lastElement();
	    Timestamp tms = event.getCreatedOn();
	    completeTms = tms.toString();

	}

	return completeTms;

    }


    /**
     * Determine the current status based on the events
     * 
     * @param xContext
     * @return status name
     */
    private String getXtinctStatus(EdaContext xContext) {

	// Determine the last event
	String lastEvent = "---";
	if ((getEvents() != null) && (getEvents().size() > 0)) {
	    LocationEvent event = (LocationEvent) getEvents().lastElement();
	    if (event.getName().indexOf("_START") > -1) {
		lastEvent = "WIP";
	    }
	    else if (event.getName().indexOf("_SUCCESS") > -1) {
		lastEvent = "Pass";
	    }
	    else {
		lastEvent = "Fail";
	    }
	}

	return lastEvent;

    }


    /**
     * Determine the component owner
     * 
     * @return owners intranet id
     * @throws IcofException
     */
    private String getOwner(EdaContext xContext)
    throws IcofException {

	// Lookup the Component_Release object for this ToolKit and Component
	Component_Release_Db compRel = new Component_Release_Db(
								getToolKit().getToolKit()
									    .getRelease(),
								getComponentDb());
	compRel.dbLookupByRelComp(xContext);

	// Lookup all the Users and Roles for this RelComponent object.
	CompTkRelRole_User_Db roleTable = new CompTkRelRole_User_Db(compRel,
								    null, null);
	Hashtable<String, TkUserRelCompRole> myRoles = roleTable.dbLookupByRelComp(xContext);

	// Display the roles and user.
	String owner = "---";
	boolean found = false;
	Iterator<TkUserRelCompRole> iter = myRoles.values().iterator();
	while (iter.hasNext() && !found) {
	    TkUserRelCompRole role = iter.next();
	    if (role.getRole().getName().equals("OWNER")) {
		owner = role.getUser().getIntranetId();
		found = true;
	    }
	}

	return owner;

    }


    /**
     * Display the status for a single component.
     * 
     * @param xContext Application context
     * @param events Collection of event lines
     */
    private void displayEvents(EdaContext xContext, List<String> events) {

	// Display the headers rows
	Timestamp now = new Timestamp(new java.util.Date().getTime());
	String sNow = getTimeAsString(now);
	System.out.println("\nGenerated: " + sNow + "\n");

	String line = formatLine("Component", "Owner", "Status",
				 "Complete Date", "Attempts");
	System.out.println(line);

	line = formatLine("----------", "------", "-------", "--------------",
			  "---------");
	System.out.println(line);

	// Display the component rows
	Collections.sort(events);
	for (int i = 0; i < events.size(); i++) {
	    System.out.println(events.get(i));
	}

	System.out.println("\nComponent count: " + events.size());

    }


    /**
     * Format a line
     * 
     * @param compText Component name
     * @param ownerText Owner email address
     * @param statusText Status
     * @param completeText Complete timestamp
     * @param attemtpsText Attempt count
     * @return Formatted line
     */
    private String formatLine(String compText, String ownerText,
			      String statusText, String completeText,
			      String attemtpsText) {

	String entry = IcofStringUtil.leftJustify(compText, " ", 18)
		       + IcofStringUtil.center(ownerText, 22)
		       + IcofStringUtil.center(statusText, 12)
		       + IcofStringUtil.center(completeText, 19)
		       + IcofStringUtil.center(attemtpsText, 10);

	return entry;

    }


    /**
     * Return the Timestamp as a string
     * 
     * @param createdOn Timestamp object
     * @return
     */
    private String getTimeAsString(Timestamp createdOn) {

	String time = "---";

	if (createdOn != null) {
	    time = new SimpleDateFormat("MM/dd/yy HH:mm:ss").format(createdOn);
	    // time = createdOn.toString();
	}

	return time;
    }


    /**
     * Lookup the build, dev and prod status for this Component.
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void setComponentEvents(EdaContext xContext)
    throws IcofException {

	LocationEvent_Db locEvent = new LocationEvent_Db(null, null, null, "");

	// Lookup the latest events
	events = locEvent.dbLookupXtinctEvents(xContext,
					       getToolKit().getToolKit(),
					       getComponentDb());

    }


    /**
     * Create the collection of Component objects to work with.
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void setComponents(EdaContext xContext)
    throws IcofException {

	// Lookup Components for the ToolKit if no Components were specified.
	if (getCompList() == null) {
	    logInfo(xContext, "Looking up Components from database", true);
	    getToolKit().setComponents(xContext);
	    components = getToolKit().getComponents();
	}

	// Otherwise look up the user specified Components.
	else {
	    logInfo(xContext, "Parsing user defined Component list", true);
	    if (getComponents() == null) {
		components = new Vector<Component_Db>();
	    }
	    else {
		components.clear();
	    }

	    Vector<String> myComps = new Vector<String>();
	    IcofCollectionsUtil.parseString(getCompList(), ",", myComps, true);
	    Iterator<String> iter = myComps.iterator();
	    while (iter.hasNext()) {
		String compName = (String) iter.next();
		Component myComp = lookupComponent(xContext, compName);
		components.add(myComp.getComponent());
	    }

	}

	logInfo(xContext, "Components to process", true);
	if (getComponents() != null) {
	    Iterator<Component_Db> iter = getComponents().iterator();
	    while (iter.hasNext()) {
		Component_Db comp = iter.next();
		logInfo(xContext, comp.getName(), true);
	    }
	}
	else {
	    logInfo(xContext, " No components found ...", true);
	}

    }


    protected void displayParameters(String dbMode, EdaContext xContext) {

	boolean verboseInd = getVerboseInd(xContext);
	logInfo(xContext, "App      : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	if (getToolKit() != null) {
	    logInfo(xContext, "ToolKit  : " + getToolKit().getName(),
		    verboseInd);
	}
	else {
	    logInfo(xContext, "ToolKit  : null", verboseInd);
	}
	if (getCompList() != null) {
	    logInfo(xContext, "Components: " + getCompList(), verboseInd);
	}
	else {
	    logInfo(xContext, "Components: null", verboseInd);
	}

	logInfo(xContext, "DB Mode  : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose  : " + getVerboseInd(xContext), verboseInd);
    }


    protected String readParams(Hashtable<String, String> params,
				String errors, EdaContext xContext)
    throws IcofException {

	// Read the RelVersion
	if (params.containsKey("-t")) {
	    setToolKit(xContext, (String) params.get("-t"));
	}
	else {
	    errors += "Tool Kit (-t) is a required parameter.";
	}

	// Read the Component
	if (params.containsKey("-c")) {
	    setCompList((String) params.get("-c"));
	}
	return errors;
    }


    protected void createSwitches(Vector<String> singleSwitches,
				  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-t");
	argSwitches.add("-c");
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Displays XTINCT status for each component in the Tool Kit.\n");
	usage.append("If a comma delimited list of components is specified\n");
	usage.append("then only those components will be displayed.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME
		     + " <-t toolkit> [-c components> [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  toolkit    = ToolKit name (14.1.1, 14.1.2 ...)\n");
	usage.append("  components = Comma delimited list of Component names (einstimer,model ...)\n");
	usage.append("  -y         = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  -h         = Help (shows this information)\n");
	usage.append("  dbMode     = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("\n");

	System.out.println(usage);

    }


    /**
     * @formatter:off
     * Members.
     */
    private String compList;
    private Vector<Component_Db> components;
    private Vector<LocationEvent> events;
    private Component_Db component;
    
    /**
     * Getters.
     */
    public String getCompList() { return compList; }
    public Vector<Component_Db> getComponents() { return components; }
    public Vector<LocationEvent> getEvents() { return events; }
    public Component_Db getComponentDb() { return component; }
    
    /**
     * Setters.
     */
    private void setCompList(String aList) { compList = aList;  }
    private void setComponents(Vector<Component_Db> comps) { components = comps; }
    private void setComponent(Component_Db aComp) { component = aComp;  }

    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }
    
    // @formatter:on

    /**
     * Lookup the Component object from the Component name
     * 
     * @param xContext Application context.
     * @param aName Component name like ess, model, idme
     * @throws IcofException
     */
    private Component lookupComponent(EdaContext xContext, String aName)
    throws IcofException {

	// Lookup the new Component
	Component comp = new Component(xContext, aName.trim());
	comp.dbLookupByName(xContext);

	logInfo(xContext, "Component: " + comp.toString(xContext), false);

	return comp;
    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }


}
