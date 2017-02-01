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
* Log application start/stop events and determine if app is running.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 04/12/2012 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.ExecutionLog;
import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;

public class ApplicationLogger extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "appLog";
    public static final String APP_VERSION = "v1.0";
    public static final String EVENT_START = "START";
    public static final String EVENT_END = "END";

    
    /**
     * Constructor
     *
     * @param     aContext   Application context
     * @param     aAppName   Application name
     * @param     aMachine   Machine name
     * @param     aComment   Event comment
     * @param     aName      User logging this event
     *
     */
    public ApplicationLogger(EdaContext aContext, String aAppName, 
                            String aMachine, String aComment, String aName) 
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);

        setAppName(aAppName);
        setMachine(aMachine);
        setComment(aComment);
        setUserName(aName);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ApplicationLogger(EdaContext aContext) throws IcofException {
        this(aContext, "", "", "", "");
    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

    	TkAppBase myApp = null;

        try {
            myApp = new ApplicationLogger(null);
			start(myApp, argv);
        }
        catch (Exception e) {
    		handleExceptionInMain(e);
        }
        finally {
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

        // Process the desired event
        if (getEvent().toUpperCase().equals(EVENT_START)) {
        	logStartEvent(xContext);
        }
        else if (getEvent().toUpperCase().equals(EVENT_END)) {
        	logStopEvent(xContext);
        }
        
        commitToDB(xContext, APP_NAME);
        
    }

    
    /**
     * Log the application stop event
     * @throws IcofException 
     */
    private void logStopEvent(EdaContext xContext) throws IcofException {
    	
    	xAppLog = new ExecutionLog(xContext, getName(), getMachine(),
		                           getComment(), getUserName());
		
		// Try to log the stop event
    	getAppLog().dbUpdate(xContext);
        setReturnCode(xContext, SUCCESS);
		
	}


    /**
     * Log the application start event
     * @throws IcofException 
     */
	private void logStartEvent(EdaContext xContext) throws IcofException {
		
		xAppLog = new ExecutionLog(xContext, getName(), getMachine(),
		                           getComment(), getUserName());
		
		// Try to log the start event
		try {
			getAppLog().dbAdd(xContext);
	        setReturnCode(xContext, SUCCESS);
		}
		catch (IcofException trap) {
			if (! getAppLog().getMessage().equals("")) {
				logInfo(xContext, getAppLog().getMessage(),  true);
			}
			else {
				throw trap;
			}

		}
		
	}

	protected String readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) {
		// Read the application name
        if (params.containsKey("-a")) {
            setAppName((String) params.get("-a"));
        }
        else {
            errors += "Application name (-a) is a required parameter.";
        }

        // Read the comment
        if (params.containsKey("-c")) {
            setComment((String) params.get("-c"));
        }

        // Read the event
        if (params.containsKey("-start") && params.containsKey("-end")) {
        	errors += "Please do not specify -start and -stop at the same time.";
        }
        else if (! params.containsKey("-start") && ! params.containsKey("-end")) {
        	errors += "You must specify -start or -stop but not both.";
        }
        else {
        	if (params.containsKey("-start")) {
        		setEvent(EVENT_START);
        	}
        	else if (params.containsKey("-end")) {
        		setEvent(EVENT_END);
        	}
        }
        // Set the machine and user if not already set
        setMachine(xContext);
		return errors;
	}


	protected void displayParameters(String dbMode,EdaContext xContext) {
		boolean verboseInd = getVerboseInd(xContext);
        logInfo(xContext, "App      : " + APP_NAME + "  " + APP_VERSION, verboseInd);
        logInfo(xContext, "App name : " + getName(), verboseInd);
        logInfo(xContext, "Machine  : " + getMachine(), verboseInd);
        logInfo(xContext, "Comment  : " + getComment(), verboseInd);
        logInfo(xContext, "User     : " + getUserName(), verboseInd);
        logInfo(xContext, "Event    : " + getEvent(), verboseInd);
        logInfo(xContext, "Verbose  : " + getVerboseInd(xContext), verboseInd);
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        singleSwitches.add("-start");
        singleSwitches.add("-end");
        argSwitches.add("-db");
        argSwitches.add("-a");
        argSwitches.add("-c");
	}

    
    /**
     * Get the user's AFS id
     * @param xContext  Application context
     */
    protected void setUser(EdaContext xContext,String aUserid) {

    	if ((getUserName() == null) || (getUserName().equals(""))) {
    		sUserName = System.getProperty(Constants.USER_NAME_PROPERTY_TAG);
    	}
    	
	}


    /**
     * Get the machine name
     * @param xContext  Application context
     */
	private void setMachine(EdaContext xContext) {
		
		if ((getMachine() == null) || (getMachine().equals(""))) {
			
			try {
				setMachine(InetAddress.getLocalHost().getHostName());
			} catch (UnknownHostException trap) {
				setMachine("unknown");
			}
			
		}
		
	}


	/**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

        StringBuffer usage = new StringBuffer();
        usage.append("------------------------------------------------------\n");
        usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
        usage.append("------------------------------------------------------\n");
        usage.append("Logs an application's start/end events. Also reports \n");
        usage.append("if this application is already running.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-a app_name> <-start|-end> [-c comment]\n");
        usage.append("       [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  app_name  = Name of application to track\n");
        usage.append("  -start    = Log that this app has started running\n");
        usage.append("  -end      = Log that this app has stopped running\n");
        usage.append("  comments  = (Optional) Event comments\n");
        usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  -h        = Help (shows this information)\n");
        usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("\n");
        usage.append("------------------------------------------------------\n");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */
    private String sAppName;
    private String sMachine;
    private String sComment;
    private String sUserName;
    private ExecutionLog xAppLog;
    private String sEvent;
    
    
    /**
     * Getters.
     */
    public String getName() { return sAppName; }
    public String getMachine() { return sMachine; }
    public String getComment() { return sComment; }
    public String getUserName() { return sUserName; }
    public String getEvent() { return sEvent; }
    public ExecutionLog getAppLog() { return xAppLog; }


    
    /**
     * Setters.
     */
    private void setAppName(String aName) { sAppName = aName;  }
    private void setMachine(String aName) { sMachine = aName;  }
    private void setComment(String aText) { sComment = aText;  }
    private void setUserName(String aText) { sUserName = aText;  }
    private void setEvent(String aEvent) { sEvent = aEvent;  }



	@Override
	protected String getAppName() {
		return APP_NAME;
	}


	@Override
	protected String getAppVersion() {
		return APP_VERSION;
	}


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}

  
    
}
