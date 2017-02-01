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
 * Log ToolKit events. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 02/03/2011 GFS  Initial coding.
 * 02/10/2011 GFS  Set CLEAN_DIRS event to not require a platform.
 * 02/11/2011 GFS  Reworked logic for testing if platform is required.  Now
 *                 leveraging new RequiresPlatform column in LocationEventName table.
 * 03/03/2011 GFS  Fixed bug in processAdvance().
 * 05/10/2011 GFS  Updated to support new create/update CodeUpdate columns. 
 * 06/10/2011 GFS  Disabled logging and reworked all DB queries.
 * 10/13/2011 GFS  Updated help to display a list of possible events. Updated
 *                 so event name is case insensitive.
 * 12/21/2011 GFS  Made the Tool Kit switch not required and will set the 
 *                 Tool Kit based on the location. Also added force_tk (-ft)
 *                 switch in case we need to override the default TK.
 * 08/16/2012 GFS  Updated to support the 14.1.build TK in build/dev/prod. This
 *                 changes means the TK changes from prod to shipb and the 
 *                 target TK must be computed for prod->shipb advances.
 * 09/07/2012 GFS  Fixed a bug where the ComponentUpdates were not getting added
 *                 to the advance target location.
 * 10/23/2012 GFS  Updated to look up the target TK without using the component.
 * 11/27/2012 GFS  Refactored to use business objects and support all flavors
 *                 of the tool kit name. 
 * 12/17/2012 GFS  Updated to support xtinct tool kits.
 * 04/09/2013 GFS  Added support for custom TKs.  Added Javadoc. Added support 
 *                 to specify the source or target TK via the command line.
 * 06/21/2013 GFS  Fixed a bug in setTgtToolKit() method.
 * 08/13/2013 GFS  Renamed Event objects to LocationEvent.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkConstants;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.ComponentUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Location_Db;
import com.ibm.stg.eda.component.tk_etreedb.LocationEventName_Db;
import com.ibm.stg.eda.component.tk_etreedb.LocationEvent_Db;
import com.ibm.stg.eda.component.tk_etreedb.Location_ComponentUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.Location_Db;
import com.ibm.stg.eda.component.tk_etreedb.Platform_Db;
import com.ibm.stg.eda.component.tk_etreedb.StageName_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.Location;
import com.ibm.stg.eda.component.tk_etreeobjs.LocationEvent;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofSystemUtil;

public class TkLogger extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "svnLog";
    public static final String APP_VERSION = "v2.2";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aComponent Object representing the component
     * @param anEvent Object representing the event to log
     * @param aLocation Object representing the event location (build, dev, prod
     *            ..)
     * @param aPlatform Object representing the platform (optional)
     * @param aUser Person logging the event
     * @param verbose If true show progress otherwise run quietly.
     * @param aComment Event comments
     */
    public TkLogger(EdaContext aContext, Component aComponent,
		    LocationEventName_Db anEvent, Location aLocation,
		    Platform_Db aPlatform, String aComment, User_Db aUser,
		    boolean verbose) throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setComponent(aComponent);
	setEventName(anEvent);
	setPlatform(aPlatform);
	setComments(aComment);
	setUser(aUser);
	setVerboseInd(aContext, verbose);
    }


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aTk Object representing the FORCE tool kit (14.1.0 ...)
     * @param aComponent Object representing the component
     * @param anEvent Object representing the event to log
     * @param aLocation Object representing the event location (build, dev, prod
     *            ..)
     * @param aPlatform Object representing the platform (optional)
     * @param aUser Person logging the event
     * @param verbose If true show progress otherwise run quietly.
     * @param aComment Event comments
     */
    public TkLogger(EdaContext aContext, ToolKit aTk, Component aComponent,
		    LocationEventName_Db anEvent, Location aLocation,
		    Platform_Db aPlatform, String aComment, User_Db aUser,
		    boolean verbose) throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setToolKit(aTk);
	setComponent(aComponent);
	setEventName(anEvent);
	setLocation(aLocation);
	setPlatform(aPlatform);
	setComments(aComment);
	setUser(aUser);
	setVerboseInd(aContext, verbose);
    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * 
     * @exception IcofException Unable to construct ManageApplications object
     */
    public TkLogger(EdaContext aContext) throws IcofException {

	this(aContext, null, null, null, null, "", null, false);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new TkLogger(null);
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

	// Log the requested event
	logIt(xContext);

	commitToDB(xContext, APP_NAME);

    }


    /**
     * Log the requested event.
     * 
     * @param aContext Application Context
     * @throws IcofException
     */
    public void logIt(EdaContext xContext)
    throws IcofException {

	// Lookup the required objects
	setExtractTimestamp(xContext);
	setCompVersion(xContext);
	setCompVerLocation(xContext, getLocation().getLocation());

	// Determine if event name is required.
	String eventName = getEventName().getName().toUpperCase();
	confirmPlatform(xContext);

	// Process the extract/advance event.
	boolean revsExtracted = false;
	if (eventName.indexOf(TkConstants.EXTRACTED) > -1)
	    revsExtracted = processExtracted(xContext);
	else if (eventName.indexOf(TkConstants.ADVANCED_TO) > -1)
	    processAdvance(xContext);

	// Log the event.
	logEvent(xContext);

	// Set the return code to success if we get this far.
	if ((eventName.indexOf(TkConstants.EXTRACTED) > -1) && !revsExtracted) {
	    setReturnCode(xContext, TkConstants.NOTHING_TO_DO);
	}
	else {
	    if (getEventName().getRequiresPlat()) {
		logInfo(xContext, "Logged event(" + getEventName().getName()
				  + ") for " + getToolKit().getName() + "/"
				  + getComponent().getName() + " on "
				  + getPlatform().getName(), true);
	    }
	    else {
		logInfo(xContext, "Logged event(" + getEventName().getName()
				  + ") for " + getToolKit().getName() + "/"
				  + getComponent().getName(), true);
	    }
	    setReturnCode(xContext, SUCCESS);
	}

    }


    /**
     * Based on the event name determine if the platform is required.
     * 
     * @param xContext Application context
     * @param anEventName An event name.
     * @throws IcofException
     */
    private void confirmPlatform(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, " Testing if Platform is required ...", verboseInd);

	// Throw exception if platform required but not present.
	if (getEventName().getRequiresPlat() && (getPlatform() == null)) {
	    throw new IcofException(APP_NAME, "confirmPlatform()",
				    IcofException.SEVERE,
				    "This event requires a Platform. "
				    + "Please rerun and specify the platform.",
				    "Event: " + getEventName().getName());
	}

    }


    /**
     * Set the extracted timestamp
     * 
     * @param xContext EdaContext object.
     */
    private void setExtractTimestamp(EdaContext xContext) {

	extractTimestamp = new Timestamp(new java.util.Date().getTime());
    }


    /**
     * Lookup the ComponentVersion row for this event.
     * 
     * @param xContext
     * @throws IcofException
     */
    private void setCompVersion(EdaContext xContext)
    throws IcofException {

	// Lookup the Component_Version_Db row for this event.
	logInfo(xContext, "\nSetting SRC ComponentVersion ...", verboseInd);
	logInfo(xContext, "getToolKit() =  "
			  + getToolKit().getToolKit().getDisplayName(),
		getVerboseInd(xContext));
	logInfo(xContext, "getComponent() =  "
			  + getComponent().getComponent().getName(),
		getVerboseInd(xContext));

	compVersion = new Component_Version_Db(xContext,
					       getToolKit().getToolKit(),
					       getComponent().getComponent());
	compVersion.dbLookupByCompRelVersion(xContext);
	logInfo(xContext,
		"ComponentVersion is " + getCompVersion().toString(xContext),
		verboseInd);

    }


    /**
     * Lookup the target ComponentVersion row for this event.
     * 
     * @param xContext
     * @throws IcofException
     */
    private void setTgtCompVersion(EdaContext xContext)
    throws IcofException {

		// Lookup the Component_Version_Db row for this event.
	logInfo(xContext, "\nSetting TARGET ComponentVersion ...", verboseInd);
	logInfo(xContext, "getTgtToolKit() =  "
			  + getTgtToolKit().getToolKit().getDisplayName(),
		getVerboseInd(xContext));
	logInfo(xContext, "getComponent() =  "
			  + getComponent().getComponent().getName(),
		getVerboseInd(xContext));

	tgtCompVersion = new Component_Version_Db(xContext,
						  getTgtToolKit().getToolKit(),
						  getComponent().getComponent());
	tgtCompVersion.dbLookupByCompRelVersion(xContext);
	logInfo(xContext, "TARGET ComponentVersion is "
			  + getTgtCompVersion().toString(xContext), verboseInd);

    }


    /**
     * Lookup the ComponentVersionLocation. Add the row if it doesn't exist.
     * 
     * @param xContext EdaContext object
     * @param aLocation Location to look up
     * @throws IcofException
     */
    private void setCompVerLocation(EdaContext xContext, Location_Db aLocation)
    throws IcofException {

	logInfo(xContext, "\nSetting SRC ComponentVersionLocation ...",
		verboseInd);
	compVerLocaton = createCompVerLocation(xContext, getCompVersion(),
					       aLocation);
	logInfo(xContext, "ComponentVersionLocation is "
			  + getCompVerLocation().toString(xContext), verboseInd);

    }


    /**
     * Lookup the ComponentVersionLocation. Add the row if it doesn't exist.
     * 
     * @param xContext EdaContext object
     * @param aLocation Location to look up
     * @throws IcofException
     */
    private Component_Version_Location_Db createCompVerLocation(EdaContext xContext,
								Component_Version_Db aCompVer,
								Location_Db aLocation)
    throws IcofException {
	
	// Look for an existing row
	Component_Version_Location_Db aCVL;
	aCVL = new Component_Version_Location_Db(aCompVer, aLocation);
	try {
	    aCVL.dbLookupByIds(xContext);
	}
	catch (IcofException ex) {
	    // Add the row if wasn't found
	    aCVL.dbAddRow(xContext);
	    aCVL.toString(xContext);
	}

	return aCVL;

    }


    /**
     * Log this event in the database
     * 
     * @param xContext EdaContext object
     * @throws IcofException
     */
    private void logEvent(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "\nIn logEvent ...", verboseInd);

	// Add the row which logs this event
	logInfo(xContext, " Logging event ...", verboseInd);
	LocationEvent_Db locEvent = new LocationEvent_Db(getPlatform(),
							 getCompVerLocation(),
							 getEventName(),
							 getComments());
	locEvent.dbAddRow(xContext, getUser());
	logInfo(xContext, " Event has been logged", verboseInd);

    }


    /**
     * For an advance we need to update the destination location with the
     * ComponentUpdates which are being advanced.
     * 
     * @param xContext EdaContext object
     * @throws IcofException
     */
    private void processAdvance(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "\nIn processAdvance ...", verboseInd);

	// If target TK (defaults to source TK)
	setTgtToolKit(xContext);
					
	// Set the target TK, CompVersion and CompVersion_location
	setTgtCompVersion(xContext);
	setAdvanceLocation(xContext);
	setTgtCompVerLocation(xContext);

	// Get the new component updates
	Vector<Location_ComponentUpdate_Db> newCompUpdates = getCompUpdates(xContext);
	
	// Lookup/Add rows to the ComponentUpdate_Location table with the
	// advance destination as the location.
	logInfo(xContext, "Creating new rows for ComponentUpdates in advance "
			  + "to location ...", verboseInd);
	Iterator<Location_ComponentUpdate_Db> iter = newCompUpdates.iterator();
	while (iter.hasNext()) {
	    Location_ComponentUpdate_Db oldLocUpdate = iter.next();
	    Location_ComponentUpdate_Db newLocUpdate;
	    newLocUpdate = new Location_ComponentUpdate_Db(getTgtCompVerLocation(),
	                                                   oldLocUpdate.getComponentUpdate());

	    try {
		newLocUpdate.dbLookupByIds(xContext);
		logInfo(xContext, " Found row for CompUpdate("
				  + newLocUpdate.getComponentUpdate().getId()
				  + ") and CompVerLocatioln("
				  + newLocUpdate.getCompVerLocation().getId()
				  + ")", verboseInd);

	    }
	    catch (Exception e) {
		newLocUpdate.dbAddRow(xContext, getUser());
		logInfo(xContext, " Added row for CompUpdate("
				  + newLocUpdate.getComponentUpdate().getId()
				  + ") and CompVerLocation("
				  + newLocUpdate.getCompVerLocation().getId()
				  + ")", verboseInd);
	    }

	}

    }


    /**
     * Determine the target tool kit
     *
     * @param xContext
     */
    private void setTgtToolKit(EdaContext xContext) {

	// TODO .. this part needs some work
	if (getTgtToolKit() == null) 
	    tgtToolKit = getToolKit();
	
    }


    /**
     * Get the Component Updates associated with the revision or location
     *
     * @param xContext Application context
     * @throws IcofException 
     */
    private Vector<Location_ComponentUpdate_Db> getCompUpdates(EdaContext xContext) 
    throws IcofException {

	Vector<Location_ComponentUpdate_Db> newCompUpdates;


	logInfo(xContext, "Finding component updates from last " +
	"ADVANCED_* event ...", verboseInd);

	// Find the last advance event for the source Component Version Location
	LocationEvent_Db lastAdvEvent;
	lastAdvEvent = new LocationEvent_Db(null, getCompVerLocation(),
	                                    getEventName(), "");
	lastAdvEvent.dbLookupLastAdvance(xContext);
	logInfo(xContext, " Last event id: " + lastAdvEvent.getId() +
	        " (" + lastAdvEvent.getCreatedBy() + ")", verboseInd);
	
	// Find all ComponentUpdate Location rows (for the source location)
	// created after the last advance.
	logInfo(xContext, "Locating ComponentUpdates since last advance ...",
	        verboseInd);
	Location_ComponentUpdate_Db locCompUpdate;
	locCompUpdate = new Location_ComponentUpdate_Db(getCompVerLocation(),
	                                                null);
	newCompUpdates = new Vector<Location_ComponentUpdate_Db>();
	if (lastAdvEvent.getId() > 0) {
	    newCompUpdates = locCompUpdate.dbLookupCreatedSince(xContext,
	                                                        lastAdvEvent.getCreatedOn());
	}
	else {
	    newCompUpdates = locCompUpdate.dbLookupCreatedSince(xContext, null);
	}
	
	logInfo(xContext,
		" New ComponentUpdates found: " + newCompUpdates.size(),
		verboseInd);
	
	return newCompUpdates;
	
    }


    /**
     * Lookup or create the target ComponentVersionLocation
     * 
     * @param xContext
     * @throws IcofException
     */
    private void setTgtCompVerLocation(EdaContext xContext)
    throws IcofException {

	tgtCompVerLocaton = createCompVerLocation(xContext,
						  getTgtCompVersion(),
						  getAdvLocation());

    }


    /**
     * Set the advance location. Parse the location from the event name.
     * 
     * @throws IcofException Trouble reading from database.
     */
    private void setAdvanceLocation(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "Setting advance to location ...", verboseInd);

	// Construct the location if xtinct or customtk
	String eName = getEventName().getName();
	String dest = eName.substring(eName.lastIndexOf("_") + 1,
				      eName.length());

	if (eName.indexOf("XTINCT") > -1) {
	    dest = "XTINCT/TK" + getTgtToolKit().getName();
	}
	else if (eName.indexOf("CUSTOMTKB") > -1) {
	    dest = "CUSTOMTKB/TK" + getTgtToolKit().getName();
	}

	else if (eName.indexOf("CUSTOMTK") > -1) {
	    dest = "CUSTOMTK/TK" + getTgtToolKit().getName();
	}

	// Lookup the location.
	advLocation = new Location_Db(dest);
	advLocation.dbLookupByName(xContext);

	logInfo(xContext,
		" Advance to location: " + getAdvLocation().getName(),
		verboseInd);

    }


    /**
     * For an extract event we need to add a ComponentUpdate row for all
     * CodeUpdates which have not been extracted.
     * 
     * @param xContext EdaContext object
     * @return True if CodeUpdates (revision) need found which have not been
     *         extracted otherwise false
     * @throws IcofException
     */
    private boolean processExtracted(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "\nIn processExtracted() ...", verboseInd);

	// Look for CodeUpdates for this Component_Version/branch which
	// have a null extractedOn Timestamp. If none then return false.
	logInfo(xContext, "Finding newly extracted CodeUpdates ...", verboseInd);
	CodeUpdate_Db cu = new CodeUpdate_Db(getCompVersion(), "", "", "",
					     null, null);

	// Look up CodeUpdates not extracted for this ComponentVersion object.
	Vector<CodeUpdate_Db> codeUpdates = cu.dbLookupNotExtracted(xContext);
	logInfo(xContext, " New CodeUpdates found: " + codeUpdates.size(),
		verboseInd);
	if (codeUpdates.size() < 1) {
	    logInfo(xContext, " Returning - nothing to do", verboseInd);
	    return false;
	}

	// Create the ComponentUpdate record for the extract CodeUpdates.
	logInfo(xContext, "Creating new ComponentUpdate ...", verboseInd);
	ComponentUpdate_Db compUpdate = new ComponentUpdate_Db((long) 0);
	compUpdate.dbAddRow(xContext, getUser());
	logInfo(xContext, " ComponentUpdate: " + compUpdate.toString(xContext),
		verboseInd);
	logInfo(xContext, " ComponentUpdate created.", verboseInd);

	// Update the extract data on each extracted CodeUpdate
	logInfo(xContext, "Updating extract data on CodeUpdates ...",
		verboseInd);
	Iterator<CodeUpdate_Db> iter = codeUpdates.iterator();
	while (iter.hasNext()) {
	    CodeUpdate_Db revision = iter.next();
	    revision.dbUpdateExtractData(xContext, compUpdate,
					 getExtractTimestamp(), getUser());
	    logInfo(xContext, " Updating CodeUpdate (" + revision.getRevision()
			      + ")", verboseInd);
	}

	// Update this location with the newly extracted CodeUpdates.
	logInfo(xContext, "Adding new ComponentUpdate to this location ...",
		verboseInd);
	Location_ComponentUpdate_Db locUpdate = new Location_ComponentUpdate_Db(
										getCompVerLocation(),
										compUpdate);
	locUpdate.dbAddRow(xContext, getUser());
	logInfo(xContext, " New ComponentUpdate added", verboseInd);

	return true;

    }


    /**
     * Parse the command line arguments
     * 
     * @param params Collection of command line parameters
     * @param errors Error messages
     * @param xContext Application context
     */
    protected String readParams(Hashtable<String, String> params,
				String errors, EdaContext xContext)
    throws IcofException {

	// Read the Component
	if (params.containsKey("-c"))
	    setComponent(xContext, (String) params.get("-c"));
	else
	    errors += "Component (-c) is a required parameter\n";

	// Read the Location
	if (params.containsKey("-l")) {
	    setLocation(xContext, (String) params.get("-l"));
	    getLocation().dbLookupByName(xContext);
	}
	else
	    errors += "Location (-l) is a required parameter\n";

	// Read the Event
	if (params.containsKey("-e"))
	    setEventName(xContext, (String) params.get("-e"));
	else
	    errors += "Event name (-e) is a required parameter\n";

	// Read the Platform
	if (params.containsKey("-p")) 
	    setPlatform(xContext, (String) params.get("-p"));

	// Read the Comments
	if (params.containsKey("-m"))
	    setComments((String) params.get("-m"));

	// Read the Tool Kit name
	if (params.containsKey("-t"))
	    setToolKit(xContext, (String) params.get("-t"));
	else if (params.containsKey("-srctk"))
	    setToolKit(xContext, (String) params.get("-srctk")); 
	else 
	    errors += "Tool Kit (-t) is a required parameter\n";
	
	if (params.containsKey("-tgttk"))
	    setTgtToolKit(xContext, (String) params.get("-tgttk"));

	// Validate required params were used
	errors += validateParams(xContext);
	
	return errors;

    }


    /**
     * Determines if the tgtTk was passed for certain events
     *
     * @param xContext Application context
     * @return "" if no errors otherwise list of errors
     */
    private String validateParams(EdaContext xContext) {

	String errors = "";
	String event = getEventName().getName();
	if (event.equals(LocationEventName_Db.EVENT_ADV_TO_CUSTOM) ||
	    event.equals(LocationEventName_Db.EVENT_ADV_TO_SHIPB) ||
	    event.equals(LocationEventName_Db.EVENT_ADV_TO_TKB)) {
	    if (getTgtToolKit() == null) 
		errors = "Target Tool Kit (-tgttk) is required when advancing"
		         + " from one tool kit to another tool kit.";
	}
	
	return errors;
	
    }


    /**
     * Define the command line inputs
     * 
     * @param singleSwitches Collection of single command line options
     * @param argSwitches Collection of single command line options requiring a
     *            value
     */
    protected void createSwitches(Vector<String> singleSwitches,
				  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-c");
	argSwitches.add("-db");
	argSwitches.add("-e");
	argSwitches.add("-l");
	argSwitches.add("-m");
	argSwitches.add("-p");
	argSwitches.add("-t");
	argSwitches.add("-srctk");
	argSwitches.add("-tgttk");

    }


    /**
     * Display the command line arguments
     * 
     * @param dbMode Database mode
     * @param xContext Application context
     */
    protected void displayParameters(String dbMode, EdaContext xContext) {

	boolean verboseInd = getVerboseInd(xContext);
	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	
	if (getToolKit() != null)
	    logInfo(xContext, "Src ToolKit: " + getToolKit().getName(),
		    verboseInd);
	else
	    logInfo(xContext, "Src ToolKit: null", verboseInd);

	if (getTgtToolKit() != null)
	    logInfo(xContext, "Tgt ToolKit: " + getTgtToolKit().getName(),
		    verboseInd);
	else
	    logInfo(xContext, "Tgt ToolKit: null", verboseInd);

	if (getComponent() != null)
	    logInfo(xContext, "Component  : " + getComponent().getName(),
		    verboseInd);
	else
	    logInfo(xContext, "Component  : null", verboseInd);

	if (getLocation() != null)
	    logInfo(xContext, "Location   : " + getLocation().getName(),
		    verboseInd);
	else
	    logInfo(xContext, "Location   : null", verboseInd);
	
	if (getEventName() != null)
	    logInfo(xContext, "Event      : " + getEventName().getName(),
		    verboseInd);
	else
	    logInfo(xContext, "Event      : null", verboseInd);
	
	if (getPlatform() != null)
	    logInfo(xContext, "Platform   : " + getPlatform().getName(),
		    verboseInd);
	else
	    logInfo(xContext, "Platform   : null", verboseInd);
	
	logInfo(xContext, "Comments   : " + comments, verboseInd);
	logInfo(xContext, "DB Mode    : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose    : " + getVerboseInd(xContext), verboseInd);

    }


    /**
     * Process the command line options and connect to the DB
     * 
     * @param argv Command line arguments
     * @param params Collection of parameters
     * @param singleSwitch Collection of single switches
     * @param argSwithes Collection of argument switches
     * @param userid User id
     * @param errors Error messages
     * @param invocationMsg Command line invocation
     * @param dbMode Database mode
     * @param appName Application name
     */
    protected EdaContext parseAndConnectToDb(String[] argv,
					     Hashtable<String, String> params,
					     Vector<String> singleSwitches,
					     Vector<String> argSwitches,
					     String userid, String errors,
					     String invocationMsg,
					     String dbMode, String appName)
    throws IcofException {

	// Parse the command line parameters.
	String msg = IcofSystemUtil.parseCmdLine(argv, singleSwitches,
						 argSwitches, params);

	invocationMsg = getInvocation(argv);

	createErrorMsg(argv, params, errors, invocationMsg, msg);
	EdaContext xContext = createConnection(params, userid, dbMode, appName,
					       false);

	// See if help was requested
	if (params.containsKey("-h")) {
	    requestHelp = true;
	    setEvents(xContext);
	    showUsage();
	    return null;
	}

	return xContext;

    }


    /**
     * Lookup all event names from the DB
     * 
     * @param xContext
     * @throws IcofException
     */
    private void setEvents(EdaContext xContext)
    throws IcofException {

	LocationEvent event = new LocationEvent(xContext, "");
	event.setEventsAll(xContext);
	events = event.getFormattedEvents(xContext);

    }


    public String getEvents() {

	return events;
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Log an event for the specified component and location.\n");
	usage.append("\n");
	usage.append("The event is applied to the Tool Kit in the specified\n");
	usage.append("location. For example, if location = build then event\n");
	usage.append("is applied to TK under development in build/dev/prod.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t toolkit> <-c component> <-l location> <-e event> "
		     + "[-p platform] [-m message]\n");
	usage.append("       [-y] [-h] [-db dbMode] [-srctk src_tk] [-tgttk tgt_tk]\n");
	usage.append("\n");
	usage.append("  toolkit    = Tool Kit name (14.1.build, 15.1.build, 14.1.10, 15.1.0 ...)\n");
	usage.append("  component  = Tool Kit component name (hdp, model, einstimer ...)\n");
	usage.append("  location   = Location of event (build, dev, prod, shipb, tkb, xtinct/tk14.1.2 ...)\n");
	usage.append("  event      = Event name (see list below)\n");
	usage.append("  platform   = (optional) Platform name (64-rs_aix61, 64-linux50 ...)\n");
	usage.append("  message    = (optional) Event description (128 chars max)\n");
	usage.append("  -y         = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  -h         = Help (shows this information)\n");
	usage.append("  dbMode     = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  src_tk     = Force logging to use this ToolKit name as\n");
	usage.append("               the SOURCE tool kit (ie, 14.1.0, 14.1.1 ... ).\n");
	usage.append("  tgt_tk     = Force logging to use this ToolKit name as\n");
	usage.append("               the TARGET tool kit (ie, 14.1.0, 14.1.1 ... ).\n");
	usage.append("\n");
	usage.append("Return Codes\n------------\n");
	usage.append(" 0 = ok\n");
	usage.append(" 1 = error\n");
	usage.append("\n");
	usage.append("Examples\n------------\n");
	usage.append("Log EXTRACTED to BUILD (/afs/eda/build/hdp/14.1) event (if not using svnExtract)\n");
	usage.append("  svnLog -t 14.1.build -c hdp -l build -e EXTRACTED\n");
	usage.append("Log EXTRACTED to BUILD (/afs/eda/build/hdp/15.1) event (if not using svnExtract)\n");
	usage.append("  svnLog -t 15.1.build -c hdp -l build -e EXTRACTED\n");
	usage.append("Log ADVANCE build->dev event for 14.1 (if not using svnAdvance)\n");
	usage.append("  svnLog -t 14.1.build -c hdp -l build -e ADVANCED_TO_DEV\n");
	usage.append("Log ADVANCE build->dev event for 15.1 (if not using svnAdvance)\n");
	usage.append("  svnLog -t 15.1.build -c hdp -l build -e ADVANCED_TO_DEV\n");
	usage.append("Log ADVANCE dev->prod event for 15.1 (if not using svnAdvance)\n");
	usage.append("  svnLog -t 15.1.build -c hdp -l dev -e ADVANCED_TO_PROD\n");
	usage.append("\n");
	usage.append(getEvents());

	System.out.println(usage);

    }


    /**
     * Members.
     * @formatter:off
     */
    private Platform_Db platform;
    private Location_Db advLocation;
    private LocationEventName_Db eventName;
    private String comments;
    private ToolKit tgtToolKit;
    private Component_Version_Db compVersion;
    private Component_Version_Db tgtCompVersion;
    private Component_Version_Location_Db compVerLocaton;
    private Component_Version_Location_Db tgtCompVerLocaton;
    private Timestamp extractTimestamp;
    private String events;


    /**
     * Getters.
     */
    public Platform_Db getPlatform() { return platform; }
    public Location_Db getAdvLocation() { return advLocation; }
    public LocationEventName_Db getEventName() { return eventName; }
    public String getComments() { return comments; }
    public ToolKit getTgtToolKit() { return tgtToolKit; }
    public Component_Version_Db getCompVersion() { return compVersion; }
    public Component_Version_Db getTgtCompVersion() { return tgtCompVersion; }
    public Component_Version_Location_Db getCompVerLocation() { return compVerLocaton; }
    public Component_Version_Location_Db getTgtCompVerLocation() { return tgtCompVerLocaton; }
    public Timestamp getExtractTimestamp() { return extractTimestamp; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


    /**
     * Setters.
     */
    private void setPlatform(Platform_Db aPlat) { platform = aPlat;  }
    private void setEventName(LocationEventName_Db aName) { eventName = aName;  }
    private void setComments(String aMsg) { comments = aMsg;  }
    //@formatter:on


    /**
     * Set the Component_db object from the component name.
     * 
     * @param xContext Application context.
     * @param aName Component name
     * @throws IcofException
     */
    protected void setComponent(EdaContext xContext, String aName)
    throws IcofException {

	try {
	    if (getComponent() == null) {
		component = new Component(xContext, aName.trim());
		component.dbLookupByName(xContext);
	    }
	    logInfo(xContext, "Component: " + getComponent().getName(), false);
	}
	catch (IcofException ex) {
	    logInfo(xContext, "Component (" + aName
			      + ") was not found in the database.", true);
	    throw ex;
	}

    }


    /**
     * Set the Platform_db object from the platform name.
     * 
     * @param xContext Application context.
     * @param aName Platform name
     * @throws IcofException
     */
    private void setPlatform(EdaContext xContext, String aName)
    throws IcofException {

	try {
	    if (getPlatform() == null) {
		platform = new Platform_Db(aName.trim());
		platform.dbLookupByName(xContext);
	    }
	    logInfo(xContext, "Platform: " + getPlatform().toString(xContext),
		    false);
	}
	catch (IcofException ex) {
	    logInfo(xContext, "Platform (" + aName
			      + ") was not found in the database.", true);
	    throw ex;
	}

    }

 
    /**
     * Set the LocationEventName_db object from the event name.
     * 
     * @param xContext Application context.
     * @param aName Event name
     * @throws IcofException
     */
    private void setEventName(EdaContext xContext, String aName)
    throws IcofException {

	try {
	    if (getEventName() == null) {
		eventName = new LocationEventName_Db(aName.trim().toUpperCase());
		eventName.dbLookupByName(xContext);
	    }
	    logInfo(xContext, "Event: " + getEventName().toString(xContext),
		    false);
	}
	catch (IcofException ex) {
	    logInfo(xContext, "Event (" + aName
			      + ") was not found in the database.", true);
	    throw ex;
	}
    }


    /**
     * Look up the target Tool Kit (from command line)
     * 
     * @param xContext Application Context
     * @param tkName Tool Kit name
     * @throws IcofException
     */
    private void setTgtToolKit(EdaContext xContext, String tkName)
    throws IcofException {

	tgtToolKit = new ToolKit(xContext, tkName);
	tgtToolKit.dbLookupByName(xContext);

	logInfo(xContext,
		"Target Tool Kit: " + getTgtToolKit().toString(xContext),
		getVerboseInd(xContext));

    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {

	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }

}
