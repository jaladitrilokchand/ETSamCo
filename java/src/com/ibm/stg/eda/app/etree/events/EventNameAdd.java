/**
 * <pre>
 * =============================================================================
 *
 * Copyright: (C) IBM Corporation 2013 -- IBM Internal Use Only
 *
 * =============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *
 * -PURPOSE---------------------------------------------------------------------
 * Add a new events Events table
 * -----------------------------------------------------------------------------
 *
 *
 * -CHANGE LOG------------------------------------------------------------------
 * 12/05/2013 GFS  Initial coding.
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree.events;

import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.LocationEventName_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.EventName;
import com.ibm.stg.iipmds.common.IcofException;

public class EventNameAdd extends TkAppBase {

    /**
     * Constants.
     */
    private static final String APP_NAME = "event.add";
    private static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     *
     * @param aContext Application context
     * @param aName               New event name
     * @param bAddToLocationTable If true add new event to Location Event Name table
     *                            otherwise add new event to Event Name table
     */
    private EventNameAdd(EdaContext aContext, String aName, 
                         boolean bAddToLocationTable) {

	super(aContext, APP_NAME, APP_VERSION);

	setName(aName);
	setLocationEventFlag(bAddToLocationTable);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     *
     * @param aContext the application context
     */
    private EventNameAdd(EdaContext aContext) {

	this(aContext, "", false);
    }


    /**
     * Instantiate the ValidateRelease Component class and process the arguments.
     *
     * @param argv the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new EventNameAdd(null);
	    start(myApp, argv);
	} catch (Exception e) {

	    handleExceptionInMain(e);
	} finally {

	    handleInFinallyBlock(myApp);
	}

    }


    /**
     * Add, update, delete, or report on the specified applications.
     *
     * @param xContext Application Context
     * @throws IcofException
     */
    public void process(EdaContext xContext)
    throws IcofException {

	// Connect to the database
	connectToDB(xContext);

	addEvent(xContext);

	commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);

    }


    /**
     * Add the event
     *
     * @param xContext Application context
     * @throws IcofException
     */
    private void addEvent(EdaContext xContext)
    throws IcofException {

	// Create the new database object
	if (isLocationEvent()) {
	    LocationEventName_Db newEvent = new LocationEventName_Db(getName(), 
	                                                             isPlatformRequired());
	    newEvent.dbAddRow(xContext);
	    logInfo(xContext, "Event (" + getName() +
	            ") added to the Location Event Name table", true);
	}
	else {
	    EventName newEvent = new EventName(xContext, getName().toUpperCase());
	    newEvent.dbAdd(xContext);
	    logInfo(xContext, "Event (" + getName() +
	            ") added to the Event Name table", true);
	}

    }


    /**
     * Define command line switches
     *
     * @param singleSwitches A collection of no arg switches
     * @param argSwitches A collection of switches requiring an arg
     */
    protected void createSwitches(Vector<String> singleSwitches,
                                  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-l");
	argSwitches.add("-db");
	argSwitches.add("-e");

    }


    /**
     * Set the application parameters from command line args
     *
     * @param params A collection of command line tokens
     * @param errors Placeholder for error messages
     * @param xContext Application context
     */
    protected String readParams(Hashtable<String, String> params,
                                String errors, EdaContext xContext) {

	if (params.containsKey("-e"))
	    setName(params.get("-e"));
	else
	    errors += "Event name (-e) is a required parameter.";

	setLocationEventFlag(false);
	setPlatformRequired(false);
	if (params.containsKey("-l")) {
	    setLocationEventFlag(true);
	    String reqPlat = params.get("-l");
	    if (reqPlat.equalsIgnoreCase("y") || reqPlat.equalsIgnoreCase("yes")) 
		setPlatformRequired(true);
	}

	return errors;

    }


    /**
     * Display application parameters
     *
     * @param dbMode Database mode
     * @param xContext Application context
     */
    protected void displayParameters(String dbMode, EdaContext xContext) {

	boolean verboseInd = getVerboseInd(xContext);
	logInfo(xContext, "App           : " + APP_NAME + "  " + APP_VERSION,
	        verboseInd);
	logInfo(xContext, "Event name    : " + getName(), verboseInd);
	logInfo(xContext, "Location event: " + isLocationEvent(), verboseInd);
	logInfo(xContext, "Platform req'd: " + isPlatformRequired(), verboseInd);
	logInfo(xContext, "Mode          : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose       : " + getVerboseInd(xContext), verboseInd);
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Add a new Event name to the Event Name.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-e event_name> [-l] [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  event_name = An event name (START, BUILT, ...)\n");
	usage.append("  -l         = (optional) Add event to tk.locationeventname\n");
	usage.append("               instead of tk.eventname DB table\n");
	usage.append("  -y         = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  -h         = Help (shows this information)\n");
	usage.append("  dbMode     = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("\n");
	usage.append("------------------------------------------------------\n");

	System.out.println(usage);

    }


    /**
     * Members
     */
    // @formatter:off
    private String sName;
    private boolean bLocationEvent = false;
    private boolean bRequiresPlatform = false;


    /**
     * Getters.
     */
    private String getName() { return sName; }
    private boolean isLocationEvent() { return bLocationEvent; }
    private boolean isPlatformRequired() { return bRequiresPlatform; }
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }


    /**
     * Setters.
     */
    private void setName(String aName) { sName = aName; }
    private void setLocationEventFlag(boolean aFlag) { bLocationEvent = aFlag; }
    private void setPlatformRequired(boolean aFlag) { bRequiresPlatform = aFlag; }
    //@formatter:on


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }


}
