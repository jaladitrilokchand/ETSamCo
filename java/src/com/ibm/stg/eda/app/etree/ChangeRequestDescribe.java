/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2011 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*
*-PURPOSE---------------------------------------------------------------------
* Displays all details of the specified ChangeRequest 
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 10/17/2011 GFS  Initial coding.
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

public class ChangeRequestDescribe extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.describe";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     xContext  Application context
     * @param     aCqId     ChangeRequest's CQ number
     */
    public ChangeRequestDescribe(EdaContext xContext, String aCqId)	
    throws IcofException {

        super(xContext, APP_NAME, APP_VERSION);

        setChangeRequest(xContext, aCqId);

    }

    
    /**
     * Constructor
     *
     * @param     aContext  Application context
     * @param     aCr       ChangeRequest object
     */
    public ChangeRequestDescribe(EdaContext aContext, ChangeRequest aCr)	
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);

        setChangeRequest(aCr);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ChangeRequestDescribe(EdaContext aContext) throws IcofException {

        this(aContext, (ChangeRequest)null);

    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

    	TkAppBase myApp = null;
		try {

			myApp = new ChangeRequestDescribe(null);
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
     * @throws Exception 
     */
    //--------------------------------------------------------------------------
    public void process(EdaContext xContext) throws Exception {

        // Connect to the database
        connectToDB(xContext);

        // Lookup and display the details for this CR
        setDetails(xContext);
        printDetails(xContext);
        
        // Set the return code to success if we get this far.
        setReturnCode(xContext, SUCCESS);
        rollBackDB(xContext, APP_NAME);
        
        
    }

    
    private void printDetails(EdaContext xContext) {
		// TODO Auto-generated method stub
		
	}


	/**
     * Lookup details for this Change Request.
     * 
     * @param xContext  Application context
     * @throws Exception 
     */
    private void setDetails(EdaContext xContext) throws Exception {

    	// TODO lookup the details ...
    	
    	// Determine this CR's status
    	getChangeRequest().getStatus().getStatus().dbLookupById(xContext);
    	logInfo(xContext, "Status: " + 
    	        getChangeRequest().getStatus().getStatus().getName(), verboseInd);
    	
    	// Lookup
    	
    	
    		logInfo(xContext, "Only ChangeRequests in the " + 
    		        ChangeRequestStatus_Db.STATUS_ONHOLD + " or " + 
    		        ChangeRequestStatus_Db.STATUS_APPROVED + " can be " +
    		        "marked as Complete.", true);


    }


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-cr");
	}


	protected String readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) throws IcofException {
		// Read the ClearQuest name
        if (params.containsKey("-cr")) {
            setChangeRequest(xContext,  params.get("-cr"));
        }
        else {
            errors += "ChangeRequest (-cr) is a required parameter\n";
        }
		return errors;
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App           : " + APP_NAME + "  " + APP_VERSION, verboseInd);
		logInfo(xContext, "ChangeRequest : " + getChangeRequest().getClearQuest(), verboseInd);
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
        usage.append("Display details about this ChangeRequest.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-cr ChangeRequest> [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  ChangeRequest = A ChangeRequest id (MDCMS######### ...).\n");
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
    
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}
    
}
