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
* Fetch the file list based on ChangeRequest. 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 08/10/2012   Initial coding.

*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.FileName_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.iipmds.common.IcofException;

public class GetFileListBasedOnCQ extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "getFileListBasedOnCQ";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     aContext  Application context
     * @param     aCr       ChangeRequest object to clone
     */
    public GetFileListBasedOnCQ(EdaContext aContext, ChangeRequest aCr)	
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
    public GetFileListBasedOnCQ(EdaContext aContext) throws IcofException {

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

			myApp = new GetFileListBasedOnCQ(null);
			start(myApp, argv);
		}catch (Exception e) {
			handleExceptionInMain(e);
		} finally {
			handleInFinallyBlock(myApp);
		}

    }

    
    //--------------------------------------------------------------------------
    /**
     * @throws IcofException 
     * Add, update, delete, or report on the specified applications.
     * 
     * @param aContext      Application Context
     * @throws  
     */
    //--------------------------------------------------------------------------
    public void process(EdaContext xContext) throws IcofException   {

        // Connect to the database
        connectToDB(xContext);

        // Create the new ChangeRequest.
        showInfo(xContext);
        commitToDB(xContext, APP_NAME);
        
    }

    
    /**
	 * Show the file list for ChangeRequest 
	 * 
	 * @param xContext
	 *            Application context
	 * @throws IcofException
	 */
	private void showInfo(EdaContext xContext) throws IcofException {

		
		FileName_Db fileName_Db = new FileName_Db(null);
		Vector<String> fileNameList = fileName_Db.dbLookupByCQ(xContext, getChangeRequest().getClearQuest());
		
		if(fileNameList.size()>0){
			logInfo(xContext, "File list for the CQ : " ,true);
			showFileInfo(fileNameList);
			
		}else{
			logInfo(xContext, "Unable to Fetch the file list for the Change Request ...", true);
		}
	}

	private void showFileInfo(Vector<String> fileNameList ){
		
		System.out.println("------------------------------------------------------------------");
		for(String fileName:fileNameList){
			System.out.println(fileName);
		}
		System.out.println("------------------------------------------------------------------");
		
	}
	
	
	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-cr");
        argSwitches.add("-c");
	}


	protected String readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) throws IcofException {

       
        // Read the ChangeRequest
        if (params.containsKey("-cr")) {
            setChangeRequest(xContext, params.get("-cr"));
        }
        else {
        	errors += "ChangeRequest (-cr) is a required parameter\n";
        }

        return errors;
	
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App           : " + APP_NAME + "  " + APP_VERSION, 
		        getVerboseInd(xContext));
		logInfo(xContext, "ChangeRequest : " + getChangeRequest().getClearQuest(), 
		        getVerboseInd(xContext));
		if (getComponent() == null) 
			logInfo(xContext, "Component     : null", getVerboseInd(xContext));
		else 
			logInfo(xContext, "Component     : " + getComponent().getName(), getVerboseInd(xContext));
		logInfo(xContext, "DB Mode       : " + dbMode, getVerboseInd(xContext));
		logInfo(xContext, "Verbose       : " + getVerboseInd(xContext), 
		        getVerboseInd(xContext));
		logInfo(xContext, "", getVerboseInd(xContext));
	}

    
    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {
    	
        StringBuffer usage = new StringBuffer();
        usage.append("------------------------------------------------------\n");
        usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
        usage.append("------------------------------------------------------\n");
        usage.append("Get the Changed files based on Change Request \n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-cr ChangeRequest> <-c component >\n");
        usage.append("        [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  ChangeRequest = A ChangeRequest id (MDCMS######### ...)\n");
        usage.append("  component     = The Component name (ess, pds, model, einstimer ...).\n");
        usage.append("  -y            = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  dbMode        = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("  -h            = Help (shows this information)\n");
        usage.append("\n");
        usage.append("Return Codes\n");
        usage.append("------------\n");
        usage.append(" 0 = ok\n");
        usage.append(" 1 = error\n");

        System.out.println(usage);

    }
    
    /**
     * Members.
     */
    
    
    /**
     * Getters.
     */
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
    
    

	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}
}
