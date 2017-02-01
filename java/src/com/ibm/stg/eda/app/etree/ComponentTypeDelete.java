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
* Delete an existing Component Type from the DB. 
*-----------------------------------------------------------------------------
*
*
*-ctHANGE LOG------------------------------------------------------------------
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

public class ComponentTypeDelete extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "compTypeDelete";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aCompType      Component Type to add
     */
    public ComponentTypeDelete(EdaContext aContext, ComponentType aCompType)
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
    public ComponentTypeDelete(EdaContext aContext) throws IcofException {

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

			myApp = new ComponentTypeDelete(null);
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
        deleteComponentType(xContext);
        commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
    }

    
    /**
     * Deletes this Component Type.
     * 
     * @param xContext  Application context
     * @throws IcofException  Trouble querying database.
     */
    private void deleteComponentType(EdaContext xContext) throws IcofException {

    	// Delete this Component Type.
    	logInfo(xContext, "Deleting Component Type ...", verboseInd);
    	getComponentType().dbDelete(xContext);
    	logInfo(xContext, "Component Type deleted ...", verboseInd);
    	
    	logInfo(xContext, "Deleted Component Type (" + getComponentType().getName() + ")", 
    	        true);
        
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-ct");
	}


	protected String readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) throws IcofException {
		// Read the Component Type
        if (params.containsKey("-ct")) {
            setComponentType(xContext, params.get("-ct"));
        }
        else {
            errors += "Component Type (-ct) is a required parameter\n";
        }
		return errors;
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App            : " + APP_NAME + "  " + APP_VERSION, verboseInd);
		logInfo(xContext, "Component Type : " + getComponentType().getName(), verboseInd);
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
        usage.append("Delete an existing Component Type from the ETREE database. \n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-ct component_type> [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  component_type = Component Type (32-bit, build_support ...).\n");
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

    
    /**
     * Getters.
     */
    public ComponentType getComponentType()  { return componentType; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}

        

    /**
     * Setters.
     */
    private void setComponentType(ComponentType aCompType) { componentType = aCompType;  }

   

    /**
     * Set the ComponentType object from the component type name
     * @param xContext       Application context.
     * @param aName          Component Type name
     * @throws IcofException 
     */
    private void setComponentType(EdaContext xContext, String aName) 
    throws IcofException { 
        if (getComponentType() == null) {
            componentType = new ComponentType(xContext, aName.trim());
            componentType.dbLookupByName(xContext);
        }    
        logInfo(xContext, "Component Type: " + getComponentType().toString(xContext), false);
    }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}

	
	

}
