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
* Update an existing Component Type in the DB. 
*-----------------------------------------------------------------------------
*
*
*-ctHANGE LOG------------------------------------------------------------------
* 10/04/2011 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.ComponentType;
import com.ibm.stg.iipmds.common.IcofException;

public class ComponentTypeUpdate extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "compTypeUpdate";
    public static final String APP_VERSION = "v1.0";

        
    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aCompType      Component Type to add
     */
    public ComponentTypeUpdate(EdaContext aContext, ComponentType aCompType,
                               String aName, String aDesc)
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);
        setComponentType(aCompType);
        setNewName(aName);
        setNewDescription(aDesc);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ComponentTypeUpdate(EdaContext aContext) throws IcofException {

        this(aContext, null, "", "");

    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

    	TkAppBase myApp = null;
		try {

			myApp = new ComponentTypeUpdate(null);
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
        updateComponentType(xContext);
                
        // Disconnect from the database
        try {
            xContext.getConnection().commit();
        }
        catch(SQLException se) {
            throw new IcofException(APP_NAME, "proces()", IcofException.SEVERE,
                                    "Unable to commit DB transactions.\n",
                                    se.getMessage());
        }finally{
        	disconnectFromDB(xContext);
        }
        
        // Set the return code.
    	setReturnCode(xContext, SUCCESS);
        
        
    }

    
    /**
     * Updates this Component Type.
     * 
     * @param xContext  Application context
     * @throws IcofException  Trouble querying database.
     */
    private void updateComponentType(EdaContext xContext) throws IcofException {

    	// Delete this Component Type.
    	logInfo(xContext, "Updating Component Type ...", verboseInd);
    	
    	String myName = getComponentType().getName();
    	if ((getNewName() != null) && (! getNewName().equals("")))
    		myName = getNewName();

    	String myDesc = getComponentType().getDescription();
    	if ((getNewDescription() != null)  && (! getNewDescription().equals("")))
    		myDesc = getNewDescription();
    	
    	getComponentType().dbUpdate(xContext, myName, myDesc);
    	logInfo(xContext, "Component Type updated ...", verboseInd);
    	
    	logInfo(xContext, "Updated Component Type (" + getComponentType().getName() + ")", 
    	        true);
        
	}


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-ct");
        argSwitches.add("-n");
        argSwitches.add("-d");
	}


	protected String readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) throws IcofException {
		// Read the Component Type
        if (params.containsKey("-ct")) {
            setComponentType(xContext,  params.get("-ct"));
        }
        else {
            errors += "Component Type (-ct) is a required parameter\n";
        }

        // Read the New Description
        if (params.containsKey("-d")) {
        	setNewDescription( params.get("-d"));
        }

        // Read the New Name
        if (params.containsKey("-n")) {
            setNewName( params.get("-n"));
        }
        
        if ((getNewName() == null) && (getNewDescription() == null))
        	errors += "You must specify a new Name (-n) or Description (-d).\n";
		return errors;
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App             : " + APP_NAME + "  " + APP_VERSION, verboseInd);
		logInfo(xContext, "Component Type  : " + getComponentType().getName(), verboseInd);
		logInfo(xContext, "New name        : " + getNewName(), verboseInd);
		logInfo(xContext, "New description : " + getNewDescription(), verboseInd);
		logInfo(xContext, "DB Mode         : " + dbMode, verboseInd);
		logInfo(xContext, "Verbose         : " + getVerboseInd(xContext), verboseInd);
	}

    
    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

        StringBuffer usage = new StringBuffer();
        usage.append("------------------------------------------------------\n");
        usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
        usage.append("------------------------------------------------------\n");
        usage.append("Update the name and/or description of an existing \n");
        usage.append("Component Type in the ETREE database. \n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-ct component_type> <-n name | -d description>\n");
        usage.append("               [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  component_type = Component Type (32-bit, build_support ...).\n");
        usage.append("  name           = New Component Type name \n");
        usage.append("  description    = New Component Type description (256 chars max)\n");
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
    private String newName;
    private String newDescription;


    
    /**
     * Getters.
     */
    public ComponentType getComponentType()  { return componentType; }
    public String getNewName()  { return newName; }
    public String getNewDescription()  { return newDescription; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}

        

    /**
     * Setters.
     */
    private void setComponentType(ComponentType aCompType) { componentType = aCompType;  }
    private void setNewName(String aName) { newName = aName;  }
    private void setNewDescription(String aDesc) { newDescription = aDesc;  }
   
   

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
