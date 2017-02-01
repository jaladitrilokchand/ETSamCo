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
* Make a ChangeRequest the Default one for the given user/tool kit. 
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 07/20/2011 GFS  Initial coding.
* 09/09/2011 GFS  Fixed typo in user message. Updated so help is shown if no
*                 parm specified.
* 09/16/2011 GFS  Change -cq switch to -cr. Changed app name to cr.defaultSet.             
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestStatus_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestActivate extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.defaultSet";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     xContext  Application context
     * @param     aCqId     ChangeRequest's CQ number
     * @param     aFlag     If true unset an existing Default CR otherwise
     *                      throw an error if a Default CR already exists.
     */
    public ChangeRequestActivate(EdaContext xContext, String aCqId, boolean aFlag)	
    throws IcofException {

        super(xContext, APP_NAME, APP_VERSION);

        setChangeRequest(xContext, aCqId);
        setForce(aFlag);

    }

    
    /**
     * Constructor
     *
     * @param     aContext  Application context
     * @param     aCr       ChangeRequest object
     * @param     aFlag     If true unset an existing Default CR otherwise
     *                      throw an error if a Default CR already exists.
     */
    public ChangeRequestActivate(EdaContext aContext, ChangeRequest aCr, 
                                 boolean aFlag)	
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);
        setChangeRequest(aCr);
        setForce(aFlag);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ChangeRequestActivate(EdaContext aContext) throws IcofException {

        this(aContext, (ChangeRequest)null, false);

    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

		TkAppBase myApp = null;
		try {

			myApp = new ChangeRequestActivate(null);
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

        // Activate this request.
        activateRequest(xContext);
        commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS); 
  
    }

    
    /**
     * Make this ChangeRequest the active CR.
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void activateRequest(EdaContext xContext) throws IcofException {

    	// Verify this ChangeRequest has been APPROVED
    	changeRequest.getStatus().getStatus().dbLookupById(xContext);
    	String currentStatus = changeRequest.getStatus().getStatus().getName();
    	
    	if (! currentStatus.equals(ChangeRequestStatus_Db.STATUS_APPROVED)) {
    		logInfo(xContext, "This Change Request wasn't set to Default.\nYou " +
    		        "can't set a Change Request to Default that hasn't been APPROVED.",
    		        true);
    		return;
    	}
    	
    	// Activate this ChangeRequest
    	boolean active = false;
    	if (active) {
    		logInfo(xContext, "Change Request set to Default.", true);
    	}
    	else {
    		logInfo(xContext, "ERROR = Unable to set this ChangeRequest to Default\n" +
    		        "        since you already have a Default ChangeRequest.\n" +
    		        "If a Default Change Request already exists and you'd like\n" +
    		        "to unset it as you make this one the Default then rerun\n" +
    		        "this application with the -f switch.", true);
    	}
    	
	}



	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        singleSwitches.add("-force");
        argSwitches.add("-db");
        argSwitches.add("-cr");
        argSwitches.add("-cq");
	}


	protected String readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) throws IcofException {
		// Read the ClearQuest name
        if (params.containsKey("-cr")) {
            setChangeRequest(xContext, (String) params.get("-cr"));
        }
        else if (params.containsKey("-cq")) {
            setChangeRequest(xContext, (String) params.get("-cq"));
        }
        else {
            errors += "ChangeRequest (-cr) is a required parameter\n";
        }

        // Read the force option
        setForce(false);
        if (params.containsKey("-force")) {
            setForce(true);
        }
		return errors;
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App           : " + APP_NAME + "  " + APP_VERSION, verboseInd);
		logInfo(xContext, "ChangeRequest : " + getChangeRequest().getClearQuest(), verboseInd);
		logInfo(xContext, "Force         : " + getForce(), verboseInd);
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
        usage.append("Makes an existing ChangeRequest the Default CR.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-cr ChangeRequest> [-force] [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  ChangeRequest =  Change Request id (MDCMS######### ...).\n");
        usage.append("  -force        = (optional) Forces the existing Default CR to be unset.\n");
        usage.append("  -y            = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  dbMode        = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("  -h            = Help (shows this information)\n");
        usage.append("\n");
        usage.append("Return Codes\n");
        usage.append("------------\n");
        usage.append(" 0 = ok\n");
        usage.append(" 1 = error\n");
        usage.append("\n");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */
    private boolean force;
    
    /**
     * Getters.
     */
    public boolean getForce()  { return force; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
    
        
    /**
     * Setters.
     */
    private void setForce(boolean aFlag) { force = aFlag; }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}

}
