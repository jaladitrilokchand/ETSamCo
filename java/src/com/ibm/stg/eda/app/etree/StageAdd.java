/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2013 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*
*-PURPOSE---------------------------------------------------------------------
* Add a Stage name to the DB. 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 06/13/2013 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.StageName;
import com.ibm.stg.iipmds.common.IcofException;

public class StageAdd extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "stage.add";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     aContext   Application context
     * @param     aStageName Stage name to add
     */
    public StageAdd(EdaContext aContext, StageName aStageName)
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);
        setStageName(aStageName);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public StageAdd(EdaContext aContext) throws IcofException {

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

			myApp = new StageAdd(null);
			start(myApp, argv);
		}

		catch (Exception e) {
			handleExceptionInMain(e);
		} finally {
			handleInFinallyBlock(myApp);
		}

    }

    
    /**
     * Add new stage name
     * 
     * @param aContext      Application Context
     * @throws              IcofException
     */
    public void process(EdaContext xContext) throws IcofException {

        // Connect to the database
        connectToDB(xContext);

        // Add the Stage Name
        addStage(xContext);
        commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
                
    }

    
    /**
     * Adds a new Stage Name
     * 
     * @param xContext        Application context
     * @return                True if Stage Name was added otherwise false
     * @throws IcofException  Trouble querying database.
     */
    private boolean addStage(EdaContext xContext) throws IcofException {

    	// Add the Stage Name to the database.
    	logInfo(xContext, "Adding Stage Name ...", verboseInd);
    	stageName = new StageName(xContext, getName());
    	boolean result = getStageName().dbAdd(xContext, getUser());
    	
        if (result) {
        	logInfo(xContext, 
        	        "Added Stage Name ("  + getStageName().getName() + ")", 
        	        true);
        }
        else {
        	logInfo(xContext, 
        	        "Unable to add new Stage Name (" + getStageName().getName() 
        	        + "). \n" 
        	        +  "If a Stage Name already exists and you'd like to change \n" +
        	        "the name then run stage.update instead.", 
        	        true);
        }
        
        return result;
        
	}

    
    /**
     * Define the command line switches
     * 
     * @param singleSwitches  Collection of switches that don't need args
     * @param argSwitches     Collection of switches that require arguments
     */
	protected void createSwitches(Vector<String> singleSwitches, 
	                              Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-s");
	}


	/**
	 * Process the command line arguments
	 * 
	 * @param params   Collection of command line arguments
	 * @param errors   String to hold any error messages
	 * @param xContext Application context object
	 */
	protected String readParams(Hashtable<String,String> params, 
	                            String errors, EdaContext xContext) 
	throws IcofException {
		
		// Read the Stage Name
        if (params.containsKey("-s")) {
            setName( params.get("-s"));
        }
        else {
            errors += "Stage Name (-s) is a required parameter\n";
        }

		return errors;

	}


	/**
	 * Display input data
	 * 
	 * @param dbMode   Database mode
	 * @param xContext Application context
	 */
	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, verboseInd);
		logInfo(xContext, "Stage Name : " + getName(), verboseInd);
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
        usage.append("Add a new Stage Name to the ETREE database. \n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-s stage_name> [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  stage_name = New stage name (ie, XTINCT/TK14.1.2venice ...)\n");
        usage.append("  -y         = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  dbMode     = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("  -h         = Help (shows this information)\n");
        usage.append("\n");
        usage.append("Return Codes\n");
        usage.append("------------\n");
        usage.append(" 0 = application ran ok\n");
        usage.append(" 1 = application error\n");
        usage.append("\n");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */
    private StageName stageName;
    private String name;

    
    /**
     * Getters.
     */
    public StageName getStageName()  { return stageName; }
    public String getName()  { return name; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}

        
    /**
     * Setters.
     */
    private void setStageName(StageName aStageName) { stageName = aStageName;  }
    private void setName(String aName) { name = aName;  }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}
    
    
}
