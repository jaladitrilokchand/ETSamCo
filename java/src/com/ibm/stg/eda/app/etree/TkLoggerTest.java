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
* Tests the TkLogger (easy testcase) 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 06/10/2011 GFS  Initial coding.
* 12/21/2011 GFS  Updated to new TkLogger usage.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.LocationEventName_Db;
import com.ibm.stg.eda.component.tk_etreedb.Location_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.iipmds.common.IcofException;

public class TkLoggerTest extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "svnLog.test";
    public static final String APP_VERSION = "v1.0";
    public static final int DEFAULT_FREQ = 5;     // minutes
    public static final int DEFAULT_TEST_COUNT = 120;


    /**
     * Constructor
     *
     * @param     aContext    Application context
     * @param     aDbMode     Desired database mode (DEV, TEST, PROD)
     *
     */
    public TkLoggerTest(EdaContext aContext, String aDbMode)
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);
        
        //setDbMode(aDbMode);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public TkLoggerTest(EdaContext aContext) throws IcofException {

        this(aContext, null);

    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

    	TkAppBase myApp = null;
		try {

			myApp = new TkLoggerTest(null);
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
     * @throws InterruptedException 
     */
    //--------------------------------------------------------------------------
    public void process(EdaContext xContext) 
    throws IcofException, InterruptedException {

        // Connect to the database
        connectToDB(xContext);
        
        testIt(xContext);
        
        commitToDB(xContext, APP_NAME);
        
    }

    
    /**
     * Run the svnLog test.
     * 
     * @param xContext Application context
     * @throws IcofException 
     * @throws InterruptedException 
     */
    private void testIt(EdaContext xContext) 
    throws IcofException, InterruptedException {
		
    	// Set the sleep time in milli-seconds
    	int sleepTime = getFrequency() * 60 * 1000;
    	    	
    	// Start tests
    	System.out.println("Event: " + getEventName());
    	System.out.println("Testing (sleeps " + getFrequency() + " mins between tests)");
    	for (int i = 1; i <= getTestCount(); i++) {
    		
    		// Get the start time (in milli-sec)
    		long startTime = System.currentTimeMillis();

    		// --------------------------------------------
    		// Test start
    		// --------------------------------------------
    		
        	// These object would be read from the database if running from 
    		// the command line so we need to add these as part of the test.
        	setComponent(xContext, "einstimer");
        	setEventName(xContext, getEventName());
        	setLocation(xContext, "build");
        	getLocation().dbLookupByName(xContext);
        	setComment("Testing svnLog");
    		
        	// Log the event
        	TkLogger logger = new TkLogger(xContext,  null, 
        	                               getComponent(), getEvent(), 
        	                               getLocation(), 
        	                               null, getComment(),
        	                               getUser(), 
        	                               getVerboseInd(xContext));
    		logger.logIt(xContext);
    		
    		// Rollback changes
            try {
                xContext.getConnection().rollback();
            }
            catch(SQLException ignore) {} // do nothing  

    		// --------------------------------------------
    		// Test end
    		// --------------------------------------------
    		
    		// Get the end time (in milli-sec)
    		long endTime = System.currentTimeMillis();
    		
    		// Compute the run time (in seconds)
    		long elapsedTime = (endTime - startTime);
    		float elTimeSec = (float)elapsedTime / (float)1000.0;
    		System.out.println(i + ") Run time: " + elTimeSec + " (sec)");
    		
    		// Sleep if not last test
    		if (i < getTestCount()) {
    			Thread.sleep(sleepTime);
    		}
    		
    	}
		
	}


	protected String readParams(Hashtable<String,String> params, String errors, EdaContext context) throws IcofException {
		// Read the event name
        setEventName("EXTRACT_START");
        if (params.containsKey("-e")) {
            setEventName( params.get("-e"));
        }
        
        // Read the frequency parameter
        setFrequency(DEFAULT_FREQ);
        if (params.containsKey("-f")) {
        	String text =  params.get("-f");
            setFrequency(Integer.parseInt(text));
        }

        // Read the test_count parameter
        setTestCount(DEFAULT_TEST_COUNT);
        if (params.containsKey("-c")) {
        	String text =  params.get("-c");
            setTestCount(Integer.parseInt(text));
        }
        return errors;
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-f");
        argSwitches.add("-c");
        argSwitches.add("-e");
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		boolean verboseInd = getVerboseInd(xContext);
        logInfo(xContext, "App       : " + APP_NAME + "  " + APP_VERSION, verboseInd);
        logInfo(xContext, "DB Mode   : " + dbMode, verboseInd);
        logInfo(xContext, "Frequency : " + getFrequency(), verboseInd);
        logInfo(xContext, "Test count: " + getTestCount(), verboseInd);
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
        usage.append("Runs svnLog (easy testcase) for einstimer against\n");
        usage.append("the specified database.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-db dbMode> [-e event] \n");
        usage.append("            [-f frequency] [-c test_count] [-y] [-h] \n");
        usage.append("\n");
        usage.append("  dbMode     = Database mode [ DEV | TEST | PROD ]\n");
        usage.append("  event      = Event to log ( EXTRACT_START | EXTRACTED | ADVANCED_TO_DEV )\n");
        usage.append("  frequency  = Test frequency in minutes [ default is 5 minutes ]\n");
        usage.append("  test_count = Number of tests to run [ default is 120 ]\n");
        usage.append("  -y         = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  -h         = Help (shows this information)\n");

        usage.append("\n");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */
    private LocationEventName_Db event;
    private String comment;
    private int frequency;
    private int testCount;
    private String eventName;
    
    
    /**
     * Getters.
     */
    public LocationEventName_Db getEvent() { return event; }
    public String getComment() { return comment; }
    public int getFrequency() { return frequency; }
    public int getTestCount() { return testCount; }
    public String getEventName() { return eventName; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
    
    
    /**
     * Setters.
     */
    private void setComment(String aNote) { comment = aNote; }
    private void setFrequency(int aNum) { frequency = aNum; }
    private void setTestCount(int aNum) { testCount = aNum; }
    private void setEventName(String aName) { eventName = aName; }

    
    /**
     * Set the Component_db object from the component name.
     * @param xContext   Application context.
     * @param aName      Component name
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
    	catch(IcofException ex) {
    		logInfo(xContext, "Component (" + aName + ") was not found in the database.", true);
    		throw ex;
    	}

    }
    
   
    /**
     * Set the LocationEventName_db object from the event name.
     * @param xContext   Application context.
     * @param aName      Event name
     * @throws IcofException 
     */
    private void setEventName(EdaContext xContext, String aName) 
    throws IcofException { 
    	try {
    		if (getEvent() == null) {
    			event = new LocationEventName_Db(aName.trim());
    			event.dbLookupByName(xContext);
    		}    
    		logInfo(xContext, "Event: " + getEvent().toString(xContext), false);
    	}
    	catch(IcofException ex) {
    		logInfo(xContext, "Event (" + aName + ") was not found in the database.", true);
    		throw ex;
    	}
    }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}


    
}
