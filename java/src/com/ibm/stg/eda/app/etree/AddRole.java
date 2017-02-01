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
* Add a new role to the EdaTkRole or ComTkRelRole table. 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 11/15/2010 GFS  Initial coding.
* 06/09/2011 GFS  Updated to disable logging.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.CompTkRelRole_Db;
import com.ibm.stg.eda.component.tk_etreedb.EdaTkRole_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class AddRole extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "addRole";
    public static final String APP_VERSION = "v1.0";
    public static final String EDA_TK_ROLE = "EDATKROLE";
    public static final String COMP_TK_REL_ROLE = "COMPTKRELROLE";
    public static final String ALL_ROLES = EDA_TK_ROLE + " or " + COMP_TK_REL_ROLE;  

    
    /**
     * Constructor
     *
     * @param     aContext    Application context
     * @param     aRoleName   Role name
     * @param     aRoleDesc   Description of role name
     * @param     aRoleTable  Role table (EdaTkRole, CompTkRelRole)
     *
     */
    public AddRole(EdaContext aContext, String aRoleName, 
                   String aRoleDesc, String aRoleTable) throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);

        setRoleName(aRoleName);
        setRoleDescription(aRoleDesc);
        setRoleTable(aRoleTable);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public AddRole(EdaContext aContext) throws IcofException {
        this(aContext, "", "", "");
    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

		TkAppBase myApp = null;
		try {

			myApp = new AddRole(null);
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

        if (getRoleTable().toUpperCase().equals(EDA_TK_ROLE)) {            
            addEdaTkRole(xContext);
        }
        else  if (getRoleTable().toUpperCase().equals(COMP_TK_REL_ROLE)) {
            addCompTkRelRole(xContext);
        }
        else {
            throw new IcofException(APP_NAME, "proces()", IcofException.SEVERE,
                                    "Unknown Role Table name (" + getRoleTable() + ").",
                                    "Valid Role Table names are " + ALL_ROLES);
        }
        setReturnCode(xContext, SUCCESS);
        commitToDB(xContext, APP_NAME);
        
    }

    
    /**
     * Add the role to the the CompTkRelRole DB table.
     * 
     * @param xCcontext  Application context
     * @throws IcofException 
     */
    private void addCompTkRelRole(EdaContext xContext) throws IcofException {

        // Create the new database object
        CompTkRelRole_Db dbObj = new CompTkRelRole_Db(getRoleName(),
                                                      getRoleDescription());
        
        // See if it exists in the database.
        try {
            dbObj.dbLookupByName(xContext);
        }
        catch (IcofException e) {
                    
            // Add it to the database.
            try {
                dbObj.dbAddRow(xContext);
                logInfo(xContext, "Role added", true);
                return;
            }
            catch (IcofException e2) {
                throw new IcofException(APP_NAME, "addCompTkRelRole()",
                                        IcofException.SEVERE,
                                        "Unable to add new Role to CompTkRelRole database table.",
                                        e2.getMessage());
            }
               
        }

        logInfo(xContext, "Role (" + getRoleName() + ") already exists in CompTkRelRole table", true);
            
    }


    /**
     * Add the role to the the CompTkRelRole DB table.
     *      
     * @param xContext
     * @throws IcofException 
     */
    private void addEdaTkRole(EdaContext xContext) throws IcofException {

        // Create the new database object
        EdaTkRole_Db dbObj = new EdaTkRole_Db(getRoleName(), getRoleDescription());

        // See if it exists in the database.
        try {
            dbObj.dbLookupByName(xContext);
        }
        catch (IcofException e) {
            
            // Add it to the database.
            try {
                dbObj.dbAddRow(xContext);
                logInfo(xContext, "Role added", true);
                return;
            }
            catch (IcofException e2) {
                throw new IcofException(APP_NAME, "addEdaTkRole()",
                                        IcofException.SEVERE,
                                        "Unable to add new Role to EdaTkRole database table.",
                                        e2.getMessage());

            }

        }

        logInfo(xContext, "Role (" + getRoleName() + ") already exists in EdaTkRole table", true);

    }


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-r");
        argSwitches.add("-t");
        argSwitches.add("-d");
	}


	protected String readParams(Hashtable<String,String> params, String errors,EdaContext xContext) {
		// Read the role name
        if (params.containsKey("-r")) {
            setRoleName((String) params.get("-r"));
        }
        else {
            errors += "Role name (-r) is a required parameter.";
        }

        // Read the role description
        if (params.containsKey("-d")) {
            setRoleDescription((String) params.get("-d"));
        }
        else {
            errors += "Role description (-d) is a required parameter.";
        }

        // Read the role table
        if (params.containsKey("-t")) {
            setRoleTable((String) params.get("-t"));
        }
        else {
            errors += "Role table (-t) is a required parameter.";
        }
		return errors;
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		boolean verboseInd = getVerboseInd(xContext);
        logInfo(xContext, "App      : " + APP_NAME + "  " + APP_VERSION, verboseInd);
        logInfo(xContext, "Role     : " + getRoleName(), verboseInd);
        logInfo(xContext, "Description: " + getRoleDescription(), verboseInd);
        logInfo(xContext, "Table    : " + getRoleTable(), verboseInd);
        logInfo(xContext, "Role name: " + getRoleName(), verboseInd);

        logInfo(xContext, "Mode     : " + dbMode, verboseInd);
        logInfo(xContext, "Verbose  : " + getVerboseInd(xContext), verboseInd);
	}

    
    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

        StringBuffer usage = new StringBuffer();
        usage.append("------------------------------------------------------\n");
        usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
        usage.append("------------------------------------------------------\n");
        usage.append("Add a new role to the specified DB table.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-r role> <-d description> <-t table>\n");
        usage.append("        [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  role      = A role name (BUILDER, TEAM LEAD ...)\n");
        usage.append("  description = Short description of role )\n");
        usage.append("  table     = A role table (EdaTkRole, CompTkRelRole ...)\n");
        usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  -h        = Help (shows this information)\n");
        usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("\n");
        usage.append("------------------------------------------------------\n");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */
    private String sRoleName;
    private String sRoleDescription;
    private String sRoleTable;
    private static boolean requestHelp = false;
    
    
    /**
     * Getters.
     */
    public String getRoleName() { return sRoleName; }
    public String getRoleDescription() { return sRoleDescription; }
    public String getRoleTable() { return sRoleTable; }
    public static boolean getRequestHelp() { return requestHelp; }
    protected String getAppName() {
		return APP_NAME;
	}

	protected String getAppVersion() {
		return APP_VERSION;
	}


    
    /**
     * Setters.
     */
    private void setRoleName(String aName) { sRoleName = aName;  }
    private void setRoleDescription(String aDesc) { sRoleDescription = aDesc;  }
    private void setRoleTable(String aTable) { sRoleTable = aTable;  }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}

    
}
