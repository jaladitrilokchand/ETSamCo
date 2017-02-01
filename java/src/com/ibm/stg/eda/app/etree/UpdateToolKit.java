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
* Update a ToolKit (RelVersion). 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 01/19/2010 GFS  Initial coding.
* 06/08/2011 GFS  Disabled logging.
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
import com.ibm.stg.eda.component.tk_etreebase.TkConstants;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.StageName;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;

public class UpdateToolKit extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "updateToolKit";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     aContext    Application context
     * @param     aVersion    ToolKit object
     * @param     aStageName  StageName object
     *
     */
    public UpdateToolKit(EdaContext aContext, ToolKit aVersion, 
    		             StageName aStageName)
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);

        setToolKit(aVersion);
        setStageName(aStageName);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public UpdateToolKit(EdaContext aContext) throws IcofException {

        this(aContext, null, null);

    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

    	TkAppBase myApp = null;
		EdaContext aContext = null;

		try {
			myApp = new UpdateToolKit(null);
			// Read and verify input parameters and get a database connection.
			start(myApp, aContext, argv);
		}

		catch (Exception e) {

			handleExceptionInMain(e, aContext);
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

        // Update the ToolKit with the new stage.
        updateToolKit(xContext);
        
        // Set the return code to success if we get this far.
        setReturnCode(xContext, SUCCESS);
        commitToDB(xContext, APP_NAME);
        
    }

    
    /**
     * Update the specified TkReleaseComponent object.
     * @param context
     * @throws IcofException 
     */
    private void updateToolKit(EdaContext xContext) throws IcofException {

    	getToolKit().getToolKit().dbUpdateStageName(xContext, 
    	                                            getStageName().getStage(), 
    	                                            getUser());
        
    	logInfo(xContext, "Update complete", true);
        
    }


    /**
     * Read command line arguments and verify application invocation
     * 
     * @param argv
     *        [] the command line arguments
     * 
     * @return the application context
     * @exception IcofException
     *            unable to process arguments
     */
    protected EdaContext initializeApp(String argv[]) throws IcofException {

        // Get the userid
        String userid = System.getProperty(Constants.USER_NAME_PROPERTY_TAG);

        // Parse the command line args
        Hashtable<String,String>  params = new Hashtable<String,String> ();
        Vector<String> singleSwitches = new Vector<String>();
        Vector<String> argSwitches = new Vector<String>();
        createSwitches(singleSwitches, argSwitches);
        
        
        // Parse the command line parameters.
        String errors = "";
        String invocationMsg = "";
        String dbMode = TkConstants.EDA_PROD;
        
        EdaContext xContext =  parseAndConnectToDb(argv, params, singleSwitches, 
                                                   argSwitches, userid, 
                                                   errors, invocationMsg,
                                                   dbMode, APP_NAME);
        if(null ==xContext){
        	return null;
        }
        // Set the user object.
        setUser(xContext, userid);
        
        errors = readParams(params, errors, xContext);

        logAndDisplay(params,errors, invocationMsg, dbMode, xContext, APP_NAME, APP_VERSION);
        return xContext;

    }


	protected String readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) throws IcofException {
		// Read the Tool Kit name
        if (params.containsKey("-t")) {
            setToolKit(xContext, (String) params.get("-t"));
        }
        else {
            errors += "ToolKit (-t) is a required parameter\n";
        }
        
        // Read the StageName
        if (params.containsKey("-s")) {
            setStageName(xContext, (String) params.get("-s"));
        }
        else {
            errors += "StageName (-s) is a required parameter\n";
        }
		return errors;
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-t");
        argSwitches.add("-s");
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		boolean verboseInd = getVerboseInd(xContext);
        logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, verboseInd);
        if (getToolKit() != null) {
            logInfo(xContext, "ToolKit    : " + getToolKit().getToolKit().getName(), verboseInd);
        }
        else {
            logInfo(xContext, "ToolKit    : null", verboseInd);
        }
        if (getStageName() != null) {
            logInfo(xContext, "StageName  : " + getStageName().getName(), verboseInd);
        }
        else {
            logInfo(xContext, "StageName  : null", verboseInd);
        }
        logInfo(xContext, "DB Mode    : " + dbMode, verboseInd);
        logInfo(xContext, "Verbose    : " + getVerboseInd(xContext), verboseInd);
	}

    
    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

        StringBuffer usage = new StringBuffer();
        usage.append("------------------------------------------------------\n");
        usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
        usage.append("------------------------------------------------------\n");
        usage.append("Updates the specified ToolKit with the given StageName.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-t tool_kit> <-s stage_name>\n");
        usage.append("            [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  tool_kit   = ToolKit name (14.1.1, 14.1.2 ).\n");
        usage.append("  stage_name = StageName (development, ship, ready, tk ...)\n");
        usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  -h        = Help (shows this information)\n");
        usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
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
    private StageName stageName;

    
    /**
     * Getters.
     */
    public StageName getStageName() { return stageName; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}

        
    /**
     * Setters.
     */
    private void setStageName(StageName aComp) { stageName = aComp;  }

    
    /**
     * Set the StageNane_db object from the Stage Name
     * @param xContext    Application context.
     * @param aToolKit    StageName like ready, new, development ...
     * @throws IcofException 
     */
    private void setStageName(EdaContext xContext, String aName) 
    throws IcofException { 
        if (getStageName() == null) {
            stageName = new StageName(xContext, aName.trim());
            stageName.dbLookupByName(xContext);
        }    
        logInfo(xContext, "StageName: " + getStageName().toString(xContext), false);
    }


 	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}


    
    
}
