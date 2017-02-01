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
* Sets a ChangeRequest's status to approved 
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 01/16/2012 GFS  Initial coding.
* 02/15/2012 GFS  Updated so only CCB_approver can approve Change Requests.
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
import com.ibm.stg.eda.component.tk_etreedb.EdaTkRole_Db;
import com.ibm.stg.eda.component.tk_etreedb.EdaTkRole_User_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestStatus;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestApprove extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.approve";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     xContext  Application context
     * @param     aCqId     ChangeRequest's CQ number
     */
    public ChangeRequestApprove(EdaContext xContext, String aCqId)	
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
    public ChangeRequestApprove(EdaContext aContext, ChangeRequest aCr)	
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
    public ChangeRequestApprove(EdaContext aContext) throws IcofException {

        this(aContext, (ChangeRequest)null);

    }
    
   
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

		TkAppBase myApp = null;
		try {

			myApp = new ChangeRequestApprove(null);
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

        // Mark this CR as Approved.
        boolean approved = markApproved(xContext);
        
        // Set the return code to success if we get this far.
        if (approved) {
        	commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
        }else{
        	rollBackDB(xContext, APP_NAME);
        }
    }

    
    /**
     * Update the state of the new ChangeRequest to APPROVED.
     * 
     * @param xContext  Application context
     * @throws Exception 
     */
    private boolean markApproved(EdaContext xContext) throws Exception {

    	// Validate the user is authorized to perform this action
    	boolean authorized = validateUser(xContext);
    	if (! authorized) {
    		return false;
    	}
    	
    	// Determine this CR's status
    	ChangeRequestStatus status = 
    		new ChangeRequestStatus(xContext, 
    		                        getChangeRequest().getChangeRequest().getStatus().getId());
    	status.dbLookupById(xContext);
    	logInfo(xContext, "Status: " + status.getName(), verboseInd);
    	
    	// If this CR is already approved then don't do anything
    	if (status.getName().equals(ChangeRequestStatus_Db.STATUS_APPROVED)) {
    		logInfo(xContext, "Change Request already marked as Approved.", true);
    		return true;
    	}

    	// If this CR is not Submitted/Reviewed then can't approve it.
    	if (status.getName().equals(ChangeRequestStatus_Db.STATUS_SUBMITTED) ||
    		status.getName().equals(ChangeRequestStatus_Db.STATUS_REVIEWED)) {

    		// Update the ChangeRequest's status to approved
    		getChangeRequest().dbUpdateStatus(xContext, 
    		                                  ChangeRequestStatus_Db.STATUS_APPROVED,
    		                                  getUser());
        	logInfo(xContext, "Change Request marked as Approved.", true);
        	
    	}
    	else {
    		
    		logInfo(xContext, "Only ChangeRequests in the " + 
    		        ChangeRequestStatus_Db.STATUS_SUBMITTED + " or "+
    		        ChangeRequestStatus_Db.STATUS_REVIEWED + " state can be " +
    		        "marked as Approved.", true);

    	}

    	return true;
    	
    }


    /**
     * Verify the user is authorized to perform this action.
     * @param xContext  Application context
     * @throws IcofException 
     */
    private boolean validateUser(EdaContext xContext) throws IcofException {
		
    	// Users for the authorized role
    	EdaTkRole_Db authRole = new EdaTkRole_Db("CCB_approver", "");
    	authRole.dbLookupByName(xContext);
    	
    	EdaTkRole_User_Db roleUser = new EdaTkRole_User_Db(getUser(), authRole);
    	try {
    		roleUser.dbLookupByAll(xContext);
    	}
    	catch(IcofException trap) {
    		System.out.println("You are not authorized to approve Change Requests.");
    		System.out.println("Only members of the EDA CCB team can approve CRs.");
    		return false;
    	}
		    	
    	return true;
    	
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-cq");
        argSwitches.add("-cr");
	}


	protected String readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) throws IcofException {
		// Read the ClearQuest name
        if (params.containsKey("-cr")) {
            setChangeRequest(xContext,  params.get("-cr"));
        }
        else if (params.containsKey("-cq")) {
            setChangeRequest(xContext,  params.get("-cq"));
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
        usage.append("Mark this ChangeRequest as Approved (ready for commits).\n");
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


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}
    
    
}


 
