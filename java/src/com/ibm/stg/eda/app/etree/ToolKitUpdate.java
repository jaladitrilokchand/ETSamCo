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
* Allows the user to update the CQ release name or description. 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 11//09/2012 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class ToolKitUpdate extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "toolKitUpdate";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aTk            Tool Kit object to be updated
     * @param     aCqRelName     New CQ release name or null if not to be updated
     * @param     aDesc          New description or null if not to be updated
     */
    public ToolKitUpdate(EdaContext aContext,  ToolKit aTk, 
    		             String aCqRelName, String aDesc)
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);
        setToolKit(aTk);
        setCqRelName(aCqRelName);
        setDescription(aDesc);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ToolKitUpdate(EdaContext aContext) throws IcofException {

        this(aContext, null, null, null);

    }
    
    
    /**
     * Instantiate the class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

    	TkAppBase myApp = null;
		try {

			myApp = new ToolKitUpdate(null);
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

        // Update the specified tool kit
        updateToolKit(xContext);
        
        commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
        
    }

    
    /**
     * Update the CQ release name and/or description of this Tool Kit
     * 
     * @param xContext  Application context
     * @throws IcofException  Trouble querying database.
     */
    public void updateToolKit(EdaContext xContext) throws IcofException {

    	getToolKit().dbUpdate(xContext, null, getCqRelName(), getDescription(),
    	                      getUser());
        
    	logInfo(xContext, "Tool Kit update is complete", true);
    	
	}


	protected String readParams(Hashtable<String,String> params, String errors,
	                            EdaContext xContext) 
	throws IcofException {
	
		// Read the Tool Kit name
        if (params.containsKey("-t")) {
            setToolKit(xContext, params.get("-t"));
        }
        else {
            errors += "Tool Kit(-t) is a required parameter\n";
        }

        // Read the new CQ Tool Kit name
        if (params.containsKey("-cqtk")) {
            setCqRelName((String) params.get("-cqtk"));
        }
        
        // Read the new description
        if (params.containsKey("-d")) {
            setDescription((String) params.get("-d"));
        }

        if ((getCqRelName() == null) && (getDescription() == null)) {
            errors += "A new CQ Tool Kit name(-cqtk) or description(-d) " +
                      "must be specified\n";
        }
	
		return errors;
		
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-t");
        argSwitches.add("-cqtk");
        argSwitches.add("-d");
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		boolean verboseInd = getVerboseInd(xContext);
        logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, verboseInd);
        logInfo(xContext, "Tool Kit   : " + getToolKit().getName(), verboseInd);
        logInfo(xContext, "New CQ TK  : " + getCqRelName(), verboseInd);
        logInfo(xContext, "Description: " + getDescription(), verboseInd);
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
        usage.append("Updates the CQ release name and/or description of the \n");
        usage.append("specified Tool Kit. Either a new CQ tool kit or \n");
        usage.append("description must be specified.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-t toolkit> [-cqtk cq_tk_name> [-d description]\n");
        usage.append("              [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  toolkit     = Tool Kit to be updated like 14.1.0, 14.1.1 ...).\n");
        usage.append("  cq_tk_name  = New CQ Tool Kit name like 14.1.0 (shipb)\n");
        usage.append("  description = New description of this TK\n");
        usage.append("  -y          = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  dbMode      = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("  -h          = Help (shows this information)\n");
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
    private String cqRelName;
    private String description;


    /**
     * Getters.
     */
    public String getCqRelName()  { return cqRelName; }
    public String getDescription()  { return description; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
        

    /**
     * Setters.
     */
    private void setCqRelName(String aName) { cqRelName = aName;  }
    private void setDescription(String aDesc) { description = aDesc;  }
    

	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}
   

}
