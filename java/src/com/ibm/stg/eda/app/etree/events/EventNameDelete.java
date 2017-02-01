/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2013 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *
 *-PURPOSE---------------------------------------------------------------------
 * Removes an existing event from the Event Name table 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 12/05/2013 GFS  Initial coding.
 *=============================================================================
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

public class EventNameDelete extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "event.delete";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aName    An event name
     * 
     */
    public EventNameDelete(EdaContext aContext, String aName) throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setName(aName);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * 
     * @exception IcofException Unable to construct ManageApplications object
     */
    public EventNameDelete(EdaContext aContext) throws IcofException {

	this(aContext, "");
    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new EventNameDelete(null);
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

	removeEvent(xContext);

	commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);

    }


    /**
     * Remove the event
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void removeEvent(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "In removeEvent() .. \n", true);
	
	// Remove the Event
	if (isLocationEvent()) {
	    
	    logInfo(xContext, "Deleting LocationEventName event .. \n", true);
	    
	    LocationEventName_Db event = new LocationEventName_Db(getName());
	    try {
		event.dbLookupByName(xContext);
	    }
	    catch(IcofException trap) { 
		logInfo(xContext, "Event (" + getName() +
		            ") not found in Location Event Name table", true);
		return;
		}
	    event.dbDeleteRow(xContext);

	    logInfo(xContext, "Event (" + getName() +
	            ") deleted from Location Event Name table", true);
	    
	}
	else {
	    
	    logInfo(xContext, "Deleting EventName event .. \n", true);
	    
	    EventName event = new EventName(xContext, getName());
	    try {
		event.dbLookupByName(xContext);
	    }
	    catch(IcofException trap) { 
		logInfo(xContext, "Event (" + getName() +
		        ") not found in Event Name table", true);
		return;
	    }
	    event.dbDelete(xContext);

	    logInfo(xContext, "Event (" + getName() +
	            ") deleted from the Event Name table", true);
	    
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
	singleSwitches.add("-l");
	argSwitches.add("-db");
	argSwitches.add("-e");
    }


    /**
     * Set the application parameters from command line args
     * 
     * @param params A collection of command line tokens
     * @param error Placeholder for error messages
     * @param xContext Application context
     */
    protected String readParams(Hashtable<String, String> params,
				String errors, EdaContext xContext) {

	// Read the name
	if (params.containsKey("-e"))
	    setName((String) params.get("-e"));
	else
	    errors += "Event name (-e) is a required parameter.";

	// Read the location table flag
	setLocationEventFlag(false);
	if (params.containsKey("-l"))
	    setLocationEventFlag(true);
	
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
	logInfo(xContext, "App       : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	logInfo(xContext, "Event name: " + getName(), verboseInd);
	logInfo(xContext, "Mode      : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose   : " + getVerboseInd(xContext), verboseInd);
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
	usage.append("  -l         = (optional) Delete event from tk.locationeventname\n");
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
     * @formatter:off
     */
    private String sName;
    private boolean bLocationEvent = false;
    private static boolean requestHelp = false;
    
    
    /**
     * Getters.
     */
    public String getName() { return sName; }
    private boolean isLocationEvent() { return bLocationEvent; }
    public static boolean getRequestHelp() { return requestHelp; }
    
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { 	return APP_VERSION; }

    
    /**
     * Setters.
     */
    private void setName(String aName) { sName = aName;  }
    private void setLocationEventFlag(boolean aFlag) { bLocationEvent = aFlag; }
    //@formatter:off


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

    
}
