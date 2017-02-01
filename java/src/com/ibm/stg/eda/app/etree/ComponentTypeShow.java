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
* Displays existing Component Types. 
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
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.ComponentType;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class ComponentTypeShow extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "compTypeShow";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aCompType      Component Type to add
     */
    public ComponentTypeShow(EdaContext aContext, ComponentType aCompType)
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
    public ComponentTypeShow(EdaContext aContext) throws IcofException {

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

			myApp = new ComponentTypeShow(null);
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
        showComponentTypes(xContext);
        rollBackDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
        
    }

    
    /**
     * Displays details about existing or user specified Component Types.
     * 
     * @param xContext  Application context
     * @throws IcofException  Trouble querying database.
     */
    private void showComponentTypes(EdaContext xContext) throws IcofException {

    	// Create a list of Component Types to display
    	setComponentTypes(xContext);
    	
    	// Gather and display each Component Type's details
    	int sizeName = 20;
    	if ((getComponentType() != null) && 
    		(getComponentType().getComponentTypes()!= null) && 
    		(getComponentType().getComponentTypes().size() > 0)) {
    		logInfo(xContext, "", true);
    		logInfo(xContext, 
    		        IcofStringUtil.leftJustify("Component Type", " ", sizeName) +
    		        "Description", true);
    		logInfo(xContext, 
    		        IcofStringUtil.leftJustify("--------------", " ", sizeName) + 
    		        "-----------", true);
    	}
    	else {
    		logInfo(xContext, "No Component Types found.", true);
    		return;
    	}
    	
    	Iterator<ComponentType> iter = getComponentType().getComponentTypes().iterator();
    	while (iter.hasNext()) {
    		ComponentType compType =  iter.next();
    		logInfo(xContext, 
    		        IcofStringUtil.leftJustify(compType.getName(), " ", sizeName) + 
    		        compType.getDescription(), true);

    	}
        
	}


    /**
     * Create a list of Component Types to process
     * @param xContext
     * @throws IcofException 
     */
	private void setComponentTypes(EdaContext xContext) throws IcofException {
		
		if (getComponentType() != null) {
			getComponentType().setComponentTypes(xContext, getComponentType());
		}
		else {
			componentType = new ComponentType(xContext, "");
			getComponentType().setComponentTypes(xContext);
		}
		
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-ct");
	}


	protected String readParams(Hashtable<String,String> params, String errors ,EdaContext xContext)
			throws IcofException {
		// Read the Component Type
        if (params.containsKey("-ct")) {
            setComponentType(xContext,  params.get("-ct"));
        }
        return errors;
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App            : " + APP_NAME + "  " + APP_VERSION, verboseInd);
		if (getComponentType() != null)
			logInfo(xContext, "Component Type : " + getComponentType().getName(), verboseInd);
		else
			logInfo(xContext, "Component Type : null", verboseInd);
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
        usage.append("Show details about existing Component Types in the ETREE database. \n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " [-ct component_type] [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  component_type = View detail for this Component Type (32-bit, build_support ...).\n");
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
    public static boolean getRequestHelp() { return requestHelp; }
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
            componentType = new ComponentType(xContext, aName.trim().toUpperCase());
            componentType.dbLookupByName(xContext);
        }    
        logInfo(xContext, "Component Type: " + getComponentType().toString(xContext), false);
    }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}


}
