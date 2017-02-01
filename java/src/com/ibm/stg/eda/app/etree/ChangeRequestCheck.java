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
* Verifies the ChangeRequest is in the given state. 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 03/14/2011 GFS  Initial coding.
* 09/09/2011 GFS  Updated so help is shown if no parm specified.
* 09/16/2011 GFS  Change -cq switch to -cr.
* 11/27/2012 GFS  Refactored to use business objects and support all flavors
*                 of the tool kit name.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestStatus;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestCheck extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "changeReqCheck";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aToolKit       ToolKit to associate this CR with
     * @param     aComponent     Component to associate the CR with
     * @param     aDescription   ChangeRequest's description
     * @param     aCqId          ChangeRequest's CQ number
     * @param     aStatus        ChangeRequest's status
     */
    public ChangeRequestCheck(EdaContext aContext, String aCqId,
    		                  ToolKit aToolKit, Component aComponent,
    		                  ChangeRequestStatus aStatus)	
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);

        setToolKit(aToolKit);
        setComponent(aComponent);
        setClearQuest(aCqId);
        setStatus(aStatus);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ChangeRequestCheck(EdaContext aContext) throws IcofException {

        this(aContext, null, null, null, null);

    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

    	TkAppBase myApp = null;
		try {

			myApp = new ChangeRequestCheck(null);
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

        // Lookup the ChangeRequest.
        setChangeRequest(xContext);
        
        // Verify the state.
        setReturnCode(xContext, FAILURE);
        if (getChangeRequestDb().getStatus().getId() == getStatus().getStatus().getId()) {
        	logInfo(xContext, "Check is good", true);
        	setReturnCode(xContext, SUCCESS);	
        }
        else {
        	logInfo(xContext, "Check is BAD", true);
        }
        rollBackDB(xContext, APP_NAME);
        
    }

    
    /**
     * Lookup the current ChangeRequest.
     * @param xContext  Application context.
     * @throws IcofException 
     */
	private void setChangeRequest(EdaContext xContext) throws IcofException {

		logInfo(xContext, "Reading ChangeRequest from DB ...", verboseInd);
		
		changeRequest = new ChangeRequest_Db(getClearQuest());
		changeRequest.dbLookupByName(xContext);
		
		logInfo(xContext, "ChangeRequest: " + changeRequest.toString(xContext), verboseInd);
		logInfo(xContext, "ChangeRequest found", verboseInd);
	}



	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        singleSwitches.add("-n");
        argSwitches.add("-db");
        argSwitches.add("-cq");
        argSwitches.add("-cr");
        argSwitches.add("-s");
        argSwitches.add("-c");
        argSwitches.add("-t");
	}


	protected String readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) throws IcofException {
		// Read the ToolKit name
        if (params.containsKey("-t")) {
            setToolKit(xContext,  params.get("-t"));
        }
        else {
            errors += "ToolKit (-t) is a required parameter\n";
        }

        // Read the Component name
        if (params.containsKey("-c")) {
            setComponent(xContext,  params.get("-c"));
        }
        else {
            errors += "Component (-c) is a required parameter\n";
        }

        // Read the ClearQuest name
        if (params.containsKey("-cr")) {
            setClearQuest( params.get("-cr"));
        }
        else if (params.containsKey("-cq")) {
            setClearQuest( params.get("-cq"));
        }
        else {
            errors += "ChangeRequest (-cr) is a required parameter\n";
        }

        // Read the status
        if (params.containsKey("-s")) {
            setStatus(xContext,  params.get("-s"));
        }
        else {
            errors += "State (-s) is a required parameter\n";
        }
		return errors;
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App           : " + APP_NAME + "  " + APP_VERSION, verboseInd);
		logInfo(xContext, "ChangeRequest : " + getClearQuest(), verboseInd);
		logInfo(xContext, "Status        : " + getStatus().getName(), verboseInd);
		logInfo(xContext, "DB Mode       : " + dbMode, verboseInd);
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
        usage.append("Verifies the given ChangeRequest is in the given state.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-cr ChangeRequest> <-t toolkit> <-c component> <-s state>\n");
        usage.append("                    [-y] [-h] -db dbMode]\n");
        usage.append("\n");
        usage.append("  toolkit       = ToolKit name (14.1.0, 14.1.1 ...).\n");
        usage.append("  component     = Component name (ess, pds, model, einstimer ...).\n");
        usage.append("  ChangeRequest = Change Request id (MDCMS######### ...).\n");
        usage.append("  state         = ChangeRequest state (submitted, approved, dev_complete, released ...)\n");
        usage.append("  -y            = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  dbMode        = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("  -h            = Help (shows this information)\n");
        usage.append("\n");
        usage.append("Return Codes\n");
        usage.append("------------\n");
        usage.append(" 0 = ok\n");
        usage.append(" 1 = ChangeRequest not in given state\n");
        usage.append("\n");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */

    private String clearQuest;
    private ChangeRequest_Db changeRequest;
    private ChangeRequestStatus status;
    
    /**
     * Getters.
     */
    public Component getComponentDb()  { return component; }
    public String getClearQuest()  { return clearQuest; }
    public ChangeRequest_Db getChangeRequestDb()  { return changeRequest; }
    public ChangeRequestStatus getStatus()  { return status; }
    public static boolean getRequestHelp() { return requestHelp; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
        
    /**
     * Setters.
     */


    private void setClearQuest(String aName) { clearQuest = aName; }
    private void setStatus(ChangeRequestStatus aStatus) { status = aStatus; }
    


    /**
     * Set the ChangeRequestStatus_Db object from the status name
     * @param xContext  Application context.
     * @param aName     Version name like dev_complete ...
     * @throws IcofException 
     */
    private void setStatus(EdaContext xContext, String aName) 
    throws IcofException { 
        if (getStatus() == null) {
            status = new ChangeRequestStatus(xContext, aName.trim().toUpperCase());
            status.dbLookupByName(xContext);
        }    
        logInfo(xContext, "Status: " + getStatus().toString(xContext), false);
    }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}
   
  
    
}
