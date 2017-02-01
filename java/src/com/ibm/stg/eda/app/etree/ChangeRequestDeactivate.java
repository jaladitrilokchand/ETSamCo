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
* Deactivates an existing ChangeRequest. 
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 07/20/2011 GFS  Initial coding.
* 09/09/2011 GFS  Updated so help is shown if no parm specified.
* 09/16/2011 GFS  Change -cq switch to -cr. Changed app name to cr.defaultUnset.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestDeactivate extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.defaultUnset";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     xContext  Application context
     * @param     aCqId     ChangeRequest's CQ number
     */
    public ChangeRequestDeactivate(EdaContext xContext, String aCqId)	
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
    public ChangeRequestDeactivate(EdaContext aContext, ChangeRequest aCr)	
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
    public ChangeRequestDeactivate(EdaContext aContext) throws IcofException {

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

			myApp = new ChangeRequestDeactivate(null);
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
        deactivateRequest(xContext);
        
        // Set the return code to success if we get this far.
        setReturnCode(xContext, SUCCESS);
        commitToDB(xContext, APP_NAME);
        
    }

    
    /**
     * Update the state of the new ChangeRequest to APPROVED.
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void deactivateRequest(EdaContext xContext) throws IcofException {
    	
    	boolean deactive = true;
    	
    	if (deactive) {
    		logInfo(xContext, "This Change Request is no longer the Default CR.", true);
    	}
    	else {
    		logInfo(xContext, "Unable to unset this Change Request as the Default.", true);
    	}

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
		boolean verboseInd = getVerboseInd(xContext);
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
        usage.append("Unsets the existing Default ChangeRequest.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-cr ChangeRequest> [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  ChangeRequest = An Default ChangeRequest id (MDCMS######### ...).\n");
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
