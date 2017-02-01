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
* Show the Advance Status for Components in ship/tk 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 09/19/2011 GFS  Initial coding
* 11/27/2012 GFS  Refactored to use business objects and support all flavors
*                 of the tool kit name.
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
import com.ibm.stg.eda.component.tk_etreebase.TkConstants;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Component_Db;
import com.ibm.stg.eda.component.tk_etreedb.LocationEvent_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.LocationEvent;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;
import com.ibm.stg.iipmds.common.SessionLog;

public class ComponentShowStatus_shiptk extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "advStatus";
    public static final String APP_VERSION = "v1.0";

    /**
     * Constructor
     *
     * @param     aContext    Application context
     * @param     aToolKit    A ToolKit object
     * @param     comps       A collection of Component objects
     */
    public ComponentShowStatus_shiptk(EdaContext aContext, ToolKit aToolKit, 
    		                   Vector<Component_Db> comps)
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);
        setToolKit(aToolKit);
        setComponents(comps);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ComponentShowStatus_shiptk(EdaContext aContext) throws IcofException {

        this(aContext, null, null);

    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

    	TkAppBase myApp = null;
		try {

			myApp = new ComponentShowStatus_shiptk(null);
			start(myApp, argv);
		}

		catch (Exception e) {

			handleExceptionInMain(e);
		} finally {

			handleInFinallyBlock(myApp);
		}

    }

    
    //--------------------------------------------------------------------------
    /**
     * Add, update, delete, or report on the specified applications.
     * 
     * @param aContext      Application Context
     * @throws              IcofException
     */
    //--------------------------------------------------------------------------
    public void process(EdaContext xContext) throws IcofException {

        // Connect to the database
        connectToDB(xContext);

        // Determine the Components to find status for
        setComponents(xContext);
        
        // Look up the status for each Component
        Iterator<Component_Db> iter = getComponents().iterator();
    	List<String> entries = new ArrayList<String>();
        while (iter.hasNext()) {
        	Component_Db comp =  iter.next();
        	setEvents(xContext, comp);
        	entries.add(formatEvents(xContext, comp));
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
     * @param comp     Component onject
     * @throws IcofException 
     */
    private String formatEvents(EdaContext xContext, Component_Db comp) 
    throws IcofException {

    	String compState = "---";
    	String advToShipbTime = "---";
    	String advToShipTime = "---";
    	String advToTkbTime = "---";
    	String advToTkTime = "---";
    	
    	// Lookup the state of the Component for this TK.
    	Component myComp = new Component(xContext, comp.getName());
    	myComp.dbLookupByName(xContext);
    	myComp.dbLookupTkDetails(xContext, getToolKit());
    	compState = myComp.getStageName();
    	
    	// Pull the extract, build and advance times from the event objects.
    	if (getEvents().containsKey(TkConstants.EVENT_ADV_TO_SHIPB)) {
    		LocationEvent event = 
    			(LocationEvent) getEvents().get(TkConstants.EVENT_ADV_TO_SHIPB);
    		advToShipbTime = getTimeAsString(event.getCreatedOn());
    	}

    	if (getEvents().containsKey(TkConstants.EVENT_ADV_TO_SHIP)) {
    		LocationEvent event = 
    			(LocationEvent) getEvents().get(TkConstants.EVENT_ADV_TO_SHIP);
    		advToShipTime = getTimeAsString(event.getCreatedOn());
    	}

    	if (getEvents().containsKey(TkConstants.EVENT_ADV_TO_TKB)) {
    		LocationEvent event = 
    			(LocationEvent) getEvents().get(TkConstants.EVENT_ADV_TO_TKB);
    		advToTkbTime = getTimeAsString(event.getCreatedOn());
    	}

    	if (getEvents().containsKey(TkConstants.EVENT_ADV_TO_TK)) {
    		LocationEvent event = 
    			(LocationEvent) getEvents().get(TkConstants.EVENT_ADV_TO_TK);
    		advToTkTime = getTimeAsString(event.getCreatedOn());
    	}

    	// Format the event times
    	String entry = formatLine(comp.getName(), compState,  
    	                          advToShipbTime, advToShipTime,
    	                          advToTkbTime, advToTkTime);
    	
    	return entry;
    	
	}

    
    /**
     * Display the status for a single component.
     * 
     * @param xContext Application context
     * @param events   Collection of event lines
     */
    private void displayEvents(EdaContext xContext, List<String> events) {

    	// Display the headers rows
    	Timestamp now = new Timestamp(new java.util.Date().getTime());
    	String sNow = getTimeAsString(now);
    	System.out.println("\nGenerated: " + sNow + "\n");
    	
    	String line = formatLine("Component", "State",  
                                 "Adv to shipb", "Adv. to ship",
                                 "Adv to tkb", "Adv. to tk");
    	System.out.println(line);

    	line = formatLine("----------", "------",   
                          "-------------", "--------------",
                          "-------------", "--------------");
    	System.out.println(line);
    	
    	// Display the component rows
    	Collections.sort(events);
    	for (int i = 0; i < events.size(); i++) {
    		System.out.println((String) events.get(i));		
    	}
    	
    	System.out.println("\nComponent count: " + events.size());
    	
	}

    
    /**
     * Format a line 
     * @param name           Component name
     * @param compStatus     Component status
     * @param advToShipbTime Adv to shipb tms
     * @param advToShipTime  Adv to ship tms
     * @param advToTkbTime   Adv to tkb tms
     * @param advToTkTime    Adv to tk tms

     * @return Formatted line
     * 
     */
    private String formatLine(String name, String compState,
                              String advToShipbTime, String advToShipTime, 
			                  String advToTkbTime, String advToTkTime) {

    	String entry = IcofStringUtil.leftJustify(name, " ", 15) + 
                       IcofStringUtil.center(compState, 12) + 
                       IcofStringUtil.center(advToShipbTime, 20) +
                       IcofStringUtil.center(advToShipTime, 20) + 
                       IcofStringUtil.center(advToTkbTime, 20) +
                       IcofStringUtil.center(advToTkTime, 20);

    	return entry;
    	
	}


	/**
     * Return the Timestamp as a string
     * @param createdOn  Timestamp object
     * @return
     */
	private String getTimeAsString(Timestamp createdOn) {
		
		String time = "---";
		
		if (createdOn != null) {
			time = new SimpleDateFormat("MM/dd/yy HH:mm:ss").format(createdOn);
			//time = createdOn.toString();
		}
		
		return time;
	}


	/**
     * Lookup the build, dev and prod status for this Component.
     * 
     * @param xContext Application context
     * @param comp     Component to lookup
	 * @throws IcofException 
     */
    private void setEvents(EdaContext xContext, Component_Db comp)
    throws IcofException {

    	LocationEvent_Db locEvent = new LocationEvent_Db(null, null, null, "");
    	
    	// Lookup the latest events
    	events = locEvent.dbLookupLastEvents(xContext, 
    			                             getToolKit().getToolKit(), comp);
		
	}


	/**
     * Create the collection of Component objects to work with.
     * 
     * @param xContext Application context
     * @throws IcofException 
     */
    private void setComponents(EdaContext xContext) throws IcofException {

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
    		
    		Vector<String> myComps = new Vector<String> ();
    		IcofCollectionsUtil.parseString(getCompList(), ",", myComps, true);
    		Iterator<String>  iter = myComps.iterator();
    		while (iter.hasNext()) {
    			String compName =  iter.next();
    			Component myComp = lookupComponent(xContext, compName);
    			components.add(myComp.getComponent());
    		}
    		
    	}
    	
    	logInfo(xContext, "Components to process", true);
    	if (getComponents() != null) {
    		Iterator<Component_Db> iter = getComponents().iterator();
    		while (iter.hasNext()) {
    			Component_Db comp =  iter.next();
    			logInfo(xContext, comp.getName(), true);
    		}
    	}
    	else {
			logInfo(xContext, " No components found ...", true);
    	}
    	
	}
  

	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-t");
        argSwitches.add("-c");
	}


	protected String readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) throws IcofException {
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


	protected void displayParameters(String dbMode, EdaContext xContext) {
		boolean verboseInd = getVerboseInd(xContext);
        logInfo(xContext, "App      : " + APP_NAME + "  " + APP_VERSION, verboseInd);
        if (getToolKit() != null) {
            logInfo(xContext, "ToolKit  : " + getToolKit().getName(), verboseInd);
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

    
    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

        StringBuffer usage = new StringBuffer();
        usage.append("------------------------------------------------------\n");
        usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
        usage.append("------------------------------------------------------\n");
        usage.append("Displays advance status for shipb/ship and tkb/tk for\n");
        usage.append("each component in the ToolKit. If a comma delimited list\n");
        usage.append("of components is specified then only those components\n");
        usage.append("will be displayed.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-t toolkit> [-c components> [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  toolkit    = ToolKit name (14.1.1, 14.1.2 ...)\n");
        usage.append("  components = Comma delimited list of Component names (einstimer,model ...)\n");
        usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  -h        = Help (shows this information)\n");
        usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("\n");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */

    private String compList;
    private Vector<Component_Db> components;
    private Hashtable<String,LocationEvent> events;

    
    /**
     * Getters.
     */

    public String getCompList() { return compList; }
    public Vector<Component_Db> getComponents() { return components; }
    public Hashtable<String,LocationEvent> getEvents() { return events; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}

    
    /**
     * Setters.
     */
    private void setCompList(String aList) { compList = aList;  }
    private void setComponents(Vector<Component_Db> comps) { components = comps;  }
    
    
   /**
     * Lookup the Component object from the Component name
     * @param xContext    Application context.
     * @param aName       Component name like ess, model, idme
     * @throws IcofException 
     */
    private Component lookupComponent(EdaContext xContext, String aName) 
    throws IcofException {
    	
    	// Lookup the new Component
    	Component comp = new Component(xContext, aName.trim());
    	comp.dbLookupByName(xContext);

    	logInfo(xContext, "Component: " + comp.toString(xContext), 
    	        isVerbose(xContext));
    	
    	return comp;
    }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}

    
}
