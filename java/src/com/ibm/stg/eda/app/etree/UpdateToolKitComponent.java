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
* Update a ToolKit Component (Component_Version). 
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
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Component_Release_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.StageName;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class UpdateToolKitComponent extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "updateTkComp";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     aContext    Application context
     * @param     aVersion    ToolKit object
     * @param     aComp       Component object
     * @param     aStageName  StageName object
     *
     */
    public UpdateToolKitComponent(EdaContext aContext, ToolKit aTk, 
    		                      Component aComponent, StageName aStageName)
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);

        setToolKit(aTk);
        setComponent(aComponent);
        setStageName(aStageName);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public UpdateToolKitComponent(EdaContext aContext) throws IcofException {

        this(aContext, null, null, null);

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

			myApp = new UpdateToolKitComponent(null);

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

        // Find the Component_Verion object.
        lookupComponentRelease(xContext);
        
        // Update the Component_Version with the new stage.
        updateComponentRelease(xContext);
        
        // Set the return code to success if we get this far.
        setReturnCode(xContext, SUCCESS);
        commitToDB(xContext, APP_NAME);
        
    }

    /**
     * Look up the Component_Release_Db object.
     * 
     * @param xContext  EdaContext object
     * @throws IcofException 
     */
    private void lookupComponentRelease(EdaContext xContext) throws IcofException {
		
    	Component_Release_Db compRel = 
    		new Component_Release_Db(getToolKit().getToolKit().getRelease(), 
    				                 getComponent().getComponent());
    	compRel.dbLookupByRelComp(xContext);
    	
    	setCompRelease(compRel);
		
	}


	/**
     * Update the specified TkReleaseComponent object.
     * @param context
     * @throws IcofException 
     */
    private void updateComponentRelease(EdaContext xContext) throws IcofException {

    	// Find the current Component_Version_Db row.
    	Component_Version_Db compVersion = 
    		new Component_Version_Db(getCompRelease(), getToolKit().getToolKit());
    	compVersion.dbLookupByCompRelVersion(xContext);
    	logInfo(xContext, "Found current component_version object", false);
    	
    	// Update it with the new StageName.
    	compVersion.dbUpdate(xContext, getStageName().getStage(), getUser());
    	logInfo(xContext, "Update complete", true);
        
    }


	protected String readParams(Hashtable<String,String>  params, String errors,
			EdaContext xContext) throws IcofException {
		// Read the Tool Kit name
        if (params.containsKey("-t")) {
            setToolKit(xContext, params.get("-t"));
        }
        else {
            errors += "ToolKit (-t) is a required parameter\n";
        }
        
        // Read the StageName
        if (params.containsKey("-s")) {
            setStageName(xContext,  params.get("-s"));
        }
        else {
            errors += "StageName (-s) is a required parameter\n";
        }

        // Read the Component
        if (params.containsKey("-c")) {
            setComponent(xContext, params.get("-c"));
        }
        else {
            errors += "Component (-c) is a required parameter\n";
        }
		return errors;
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-t");
        argSwitches.add("-s");
        argSwitches.add("-c");
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
        if (getComponent() != null) {
            logInfo(xContext, "Component  : " + getComponent().getName(), verboseInd);
        }
        else {
            logInfo(xContext, "Component  : null", verboseInd);
        }
        if (getStageNameText() != null) {
            logInfo(xContext, "StageName  : " + getStageNameText(), verboseInd);
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
        usage.append("Updates the StageName for the specified ToolKit and Component.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-t tool_kit> <-s stage_name> <-c component>\n");
        usage.append("            [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  tool_kit   = ToolKit name (14.1.1, 14.1.2 ).\n");
        usage.append("  component  = Component name (ess, hdp, einstimer).\n");
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
    private Component_Release_Db compRelease;
 
    private String stageNameText;

    
    /**
     * Getters.
     */
    public String getStageNameText() { return stageNameText; }
    public StageName getStageName() { return stageName; }
    public Component_Release_Db getCompRelease() { return compRelease; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}

    
        
    /**
     * Setters.
     */
    private void setStageName(StageName aComp) { stageName = aComp;  }
    private void setCompRelease(Component_Release_Db aCompRel) { compRelease = aCompRel; }
    public void setStageNameText(String aStageNameText) {  stageNameText = aStageNameText; }

    
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
