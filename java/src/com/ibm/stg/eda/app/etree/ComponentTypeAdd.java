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
* Add a Component Type to the DB. 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 10/04/2011 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.ComponentType;
import com.ibm.stg.iipmds.common.IcofException;

public class ComponentTypeAdd extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "compTypeAdd";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aCompType      Component Type to add
     */
    public ComponentTypeAdd(EdaContext aContext, ComponentType aCompType)
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);
        setComponentType(aCompType);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ComponentTypeAdd(EdaContext aContext) throws IcofException {

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

			myApp = new ComponentTypeAdd(null);
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

        // Add the Component Type
        addComponentType(xContext);
        commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
        
        
    }

    
    /**
     * Adds a new Component Type.
     * 
     * @param xContext  Application context
     * @return                True if Component Type was added otherwise false
     * @throws IcofException  Trouble querying database.
     */
    private boolean addComponentType(EdaContext xContext) throws IcofException {

    	// Add the Component TYpe to the database.
    	logInfo(xContext, "Adding Component Type ...", verboseInd);
    	componentType = new ComponentType(xContext, 
    	                                  getCompTypeName(), getDescription());
    	boolean result = getComponentType().dbAdd(xContext);
    	
        if (result) {
        	logInfo(xContext, "Added Component Type (" 
        	        + getComponentType().getName() + ")", 
        	        true);
        }
        else {
        	logInfo(xContext, 
        	        "Unable to add new Component Type (" 
        	        + getComponentType().getName() + "). \n" +
        	        "If a Component Type already exists and you'd like to change \n" +
        	        "the name/description then run compTypeUpdate instead of this " +
        	        "application.", 
        	        true);
        }
        
        return result;
        
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-ct");
        argSwitches.add("-d");
	}


	protected String readParams(Hashtable<String,String> params, String errors,EdaContext xContext) throws IcofException {
		// Read the Component Type
        if (params.containsKey("-ct")) {
            setCompTypeName( params.get("-ct"));
        }
        else {
            errors += "Component Type (-ct) is a required parameter\n";
        }

        // Read the Description
        if (params.containsKey("-d")) {
            setDescription( params.get("-d"));
        }
        else {
            errors += "Description (-d) is a required parameter\n";
        }
		return errors;
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App            : " + APP_NAME + "  " + APP_VERSION, verboseInd);
		logInfo(xContext, "Component Type : " + getCompTypeName(), verboseInd);
		logInfo(xContext, "Description    : " + getDescription(), verboseInd);
		logInfo(xContext, "DB Mode        : " + dbMode, verboseInd);
		logInfo(xContext, "Verbose        : " + getVerboseInd(xContext), verboseInd);
	}

    
    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

        StringBuffer usage = new StringBuffer();
        usage.append("------------------------------------------------------\n");
        usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
        usage.append("------------------------------------------------------\n");
        usage.append("Add a new Component Type to the ETREE database. \n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-ct component_type> <-d description> [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  component_type = Component Type (32-bit, build_support ...).\n");
        usage.append("  description    = Description of this Component Type (256 max chars).\n");
        usage.append("  -y             = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  dbMode         = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("  -h             = Help (shows this information)\n");
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
    private ComponentType componentType;
    private String compTypeName;
    private String description;

    
    /**
     * Getters.
     */
    public ComponentType getComponentType()  { return componentType; }
    public String getCompTypeName()  { return compTypeName; }
    public String getDescription()  { return description; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}

        

    /**
     * Setters.
     */
    private void setComponentType(ComponentType aCompType) { componentType = aCompType;  }
    private void setCompTypeName(String aName) { compTypeName = aName;  }
    private void setDescription(String aDesc) { description = aDesc;  }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}

    
    
}
