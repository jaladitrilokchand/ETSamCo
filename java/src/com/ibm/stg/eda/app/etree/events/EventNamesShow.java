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
 * Displays all possible event names 
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
import java.util.List;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.EventName;
import com.ibm.stg.iipmds.common.IcofException;

public class EventNamesShow extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "events.show";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aName Role name
     * 
     */
    public EventNamesShow(EdaContext aContext, String aName) throws IcofException {

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
    public EventNamesShow(EdaContext aContext) throws IcofException {

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

	    myApp = new EventNamesShow(null);
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

	setEvents(xContext);
	showEvents(xContext);
	setReturnCode(xContext, SUCCESS);
	rollBackDB(xContext, APP_NAME);

    }


    /**
     * Displays the event name collection
     *
     * @param xContext
     */
    private void showEvents(EdaContext xContext) {

	logInfo(xContext, "In showEvents() ... ", verboseInd);
	
	if (getEvents() == null || getEvents().isEmpty()) {
	    System.out.println("\nNo events found");
	}
	else {
	    System.out.println("\nEvent Name(s)\n--------------");
	}
	
	for (EventName event : getEvents()) {
	    System.out.println(event.getName());
	}
	
    }


    /**
     * Read the events from database
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void setEvents(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "In setEvents() ... ", verboseInd);
	
	EventName myEvent = new EventName(xContext, "");
	events = myEvent.getEventNames(xContext, getName());
	
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
	if (params.containsKey("-e")) {
	    setName((String) params.get("-e"));
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
	usage.append("Displays all Event names or those names containing the event_name text.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " [-e event_name] [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  event_name = Show on event names containing this text\n");
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
    private List<EventName> events; 
    private static boolean requestHelp = false;
    
    
    /**
     * Getters.
     */
    public String getName() { return sName; }
    public List<EventName> getEvents() { return events; }
    public static boolean getRequestHelp() { return requestHelp; }
    
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { 	return APP_VERSION; }

    
    /**
     * Setters.
     */
    private void setName(String aName) { sName = aName;  }
    //@formatter:off


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

    
}
