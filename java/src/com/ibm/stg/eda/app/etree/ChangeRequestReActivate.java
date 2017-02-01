/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2012 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Prashanth Shivaram
*
*-PURPOSE---------------------------------------------------------------------
* Sets a ChangeRequest's status to approved from the completed status
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 06/11/2012 PS  Initial coding.
* 09/25/2012 GS  Fixed a typo in the iipmds2 name and reworked the user tests.
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
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestStatus;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestReActivate extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.reactivate";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     xContext  Application context
     * @param     aCqId     ChangeRequest's CQ number
     */
    public ChangeRequestReActivate(EdaContext xContext, String aCqId)	
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
    public ChangeRequestReActivate(EdaContext aContext, ChangeRequest aCr)	
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
    public ChangeRequestReActivate(EdaContext aContext) throws IcofException {

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

			myApp = new ChangeRequestReActivate(null);
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
	 * @param xContext
	 *            Application context
	 * @throws Exception
	 */
	private boolean markApproved(EdaContext xContext) throws Exception {

		// Determine this CR's status
		ChangeRequestStatus status = new ChangeRequestStatus(xContext,
				getChangeRequest().getChangeRequest().getStatus().getId());
		status.dbLookupById(xContext);
		logInfo(xContext, "Status: " + status.getName(), verboseInd);

		// If this CR is already approved then don't do anything
		if (status.getName().equals(ChangeRequestStatus_Db.STATUS_APPROVED)) {
			logInfo(xContext, "Change Request already marked as Approved.",
					true);
			return true;
		}

		// If this CR is already completed then only it can be re-activated.
		if (status.getName().equals(ChangeRequestStatus_Db.STATUS_COMPLETE)) {

			String userid = getChangeRequest().getChangeRequest().getCreatedBy();
			String iipmdsUser = "iipmds2@us.ibm.com";

			User_Db user = getUser();
			if (userid.equals(iipmdsUser) || userid.equals(user.getIntranetId())) {
				// Update the ChangeRequest's status to approved
				getChangeRequest().dbUpdateStatus(xContext,
				                                  ChangeRequestStatus_Db.STATUS_APPROVED,
				                                  getUser());
				logInfo(xContext, "Change Request marked as Approved.", true);
			} else {
				logInfo(
						xContext,
						"Only ChangeRequests  created by the same user can be re-activated to Approved status",
						true);
			}

		} else {

			logInfo(xContext, "Only ChangeRequests in the "
					+ ChangeRequestStatus_Db.STATUS_COMPLETE + " state can be "
					+ "re-activated to Approved status.", true);

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
        usage.append("Change the state of a COMPLETE ChangeRequest back to\n");
        usage.append("the APPROVED state so it can be re-used for commits.\n");
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


 
