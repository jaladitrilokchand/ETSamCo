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
* Show the valid TK Release and Component combinations. 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/27/2010 GFS  Initial coding.
* 07/25/2010 GFS  Converted to using PreparedStatements.
* 06/09/2011 GFS  Disabled logging
* 11/27/2012 GFS  Refactored to use business objects and support all flavors
*                 of the tool kit name.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.eda.component.tk_etreebase.TkConstants;
import com.ibm.stg.eda.component.tk_etreebase.TkDbUtils;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Component_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Release_Db;
import com.ibm.stg.eda.component.tk_etreedb.RelVersion_Db;
import com.ibm.stg.eda.component.tk_etreedb.Release_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class GetReleaseComponents extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "getRelComps";
    public static final String APP_VERSION = "v1.1";

    // Main query
    //        select r.tkrelease_name, v.tkversion_name, c.component_name
    //        from tk.component as c,
    //             tk.component_tkrelease as cr,
    //             tk.tkrelease as r,
    //             tk.tkversion as v
    //        where c.component_id = cr.component_id
    //          and cr.tkrelease_id = r.tkrelease_id
    //          and v.tkrelease_id = r.tkrelease_id
    public static final String QUERY =
        "select r." + Release_Db.NAME_COL + 
        " ,v." + RelVersion_Db.NAME_COL + 
        " ,c." + Component_Db.NAME_COL + " " +
        "from " + Component_Db.TABLE_NAME + " as c, " +
        Component_Release_Db.TABLE_NAME + " as cr, " +
        Release_Db.TABLE_NAME + " as r, " +
        RelVersion_Db.TABLE_NAME + " as v " +
        "where c." + Component_Db.ID_COL + " = cr." + Component_Release_Db.ID_COL +
        " and cr." + Component_Release_Db.REL_ID_COL + " = r." + Release_Db.ID_COL +
        " and v." + RelVersion_Db.RELEASE_ID_COL + " = r." + Release_Db.ID_COL;
        
    /**
     * Constructor
     *
     * @param     aContext    Application context
     * @param     aTk         ToolKit object
     * @param     aComponent  Component object
     *
     */
    public GetReleaseComponents(EdaContext aContext, Release_Db aRelease,
                                ToolKit aTk, Component aComponent)
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);

        setRelease(aRelease);
        setToolKit(aTk);
        setComponent(aComponent);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public GetReleaseComponents(EdaContext aContext) throws IcofException {

        this(aContext, null, null, null);

    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

    	TkAppBase myApp = null;
		try {

			myApp = new GetReleaseComponents(null);
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

        // Look up the Release, RelVersion and/or components
        if ((showAll) || 
            ((getToolKit() == null) && (getComponent() == null))) {
            showRelComps(xContext);
        }
        else if ((getToolKit() != null) && (getComponent() == null)) {
            showComponents(xContext);
        }
        else if ((getToolKit() == null) && (getComponent() != null)) {
            showRelVersions(xContext);
        }
        else {
            showOne(xContext);
        }
        // Set the return code.
        setReturnCode(xContext, SUCCESS);
        commitToDB(xContext, APP_NAME);
        
    }

    
    /**
     * Filter the results by TkRelease, version and component.
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void showOne(EdaContext xContext) throws IcofException {

        lookupOneComponent = true;
        showResults(xContext);
        
    }


    /**
     * Filter the query by TkRelese version.
     * 
     * @param context
     * @throws IcofException 
     */
    private void showComponents(EdaContext xContext) throws IcofException {

        lookupComponents = true;
        showResults(xContext);
        
    }


    /**
     * Filter the query by component.
     * 
     * @param context   Application context
     * @throws IcofException 
     */
    private void showRelVersions(EdaContext xContext) throws IcofException {
        
        lookupRelVersions = true;
        showResults(xContext);
        
    }

    
    /**
     * Show all the valid release, version and component combinations.
     * 
     * @param  xContext  Application context
     * @throws IcofException 
     */
    private void showRelComps(EdaContext xContext) throws IcofException {
        showResults(xContext);
        
    }


    /**
     * Show all the valid release, version and component combinations.
     * 
     * @param  xContext  Application context
     * @throws IcofException 
     */
    private void showResults(EdaContext xContext) 
    throws IcofException {

        // Create the SQL query in the PreparedStatement.
        setLookupStatement(xContext);
        
        try {
            
            if (lookupRelVersions) {
                getLookupStatement().setShort(1, 
                                              getComponent().getComponent().getId());
            }
            else if (lookupComponents) {
                getLookupStatement().setShort(1, getToolKit().getToolKit().getId());
                getLookupStatement().setShort(2, getRelease().getId());
            }
            else if (lookupOneComponent) {
                getLookupStatement().setShort(1, getToolKit().getToolKit().getId());
                getLookupStatement().setShort(2, getRelease().getId());
                getLookupStatement().setShort(3, getComponent().getComponent().getId());
            }
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "showResults()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getLookupStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }

        
        // Run the query.
        //
        // TODO should move to DB layer
        //
        TkAudit db = new TkAudit();
        ResultSet rs = db.executeQuery(xContext, getLookupStatement());
        StringBuffer results = new StringBuffer();

        // Get the results
        try {
            while (rs.next()) {
                String relName = rs.getString(1);
                String verName = rs.getString(2);
                String compName = rs.getString(3);
                if (! isQuietMode()) {
                    results.append(" " + relName + "." + verName + "      " + compName + "\n");
                }
                else {
                    if (results.length() > 1) {
                        results.append(",");
                    }
                    results.append(compName);

                }
            
            }
            getLookupStatement().close();
        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "showResults()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Show the results
        if (results.length() > 0) {
            if (! isQuietMode()) { 
                System.out.println("\nTK Release  Component");
                System.out.println("----------  ---------");
            }
            System.out.println(results.toString());
        }
        else {
            System.out.println("No valid Release, version or components found");
        }
        
    }


	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        singleSwitches.add("-a");
        singleSwitches.add("-q");
        argSwitches.add("-db");
        argSwitches.add("-r");
        argSwitches.add("-c");
	}


	protected String  readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) throws IcofException {
		// Read the release name
        if (params.containsKey("-r")) {
            setRelease(xContext,  params.get("-r"));
            setToolKit(xContext, TkConstants.INTIAL_VERSION);
        }

        // Read the component name
        if (params.containsKey("-c")) {
            setComponent(xContext,  params.get("-c"));
        }

        // Read the ALL options
        setShowAll(false);
        if (params.containsKey("-a")) {
            setShowAll(true);
        }

        // Set quiet option
        setQuietMode(false);
        if (params.containsKey("-q")) {
            setQuietMode(true);
        }
        return errors;
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		boolean verboseInd = getVerboseInd(xContext);
        logInfo(xContext, "App      : " + APP_NAME + "  " + APP_VERSION, verboseInd);
        if (getRelease() != null) {
            logInfo(xContext, "Release  : " + getRelease().getName(), verboseInd);
        }
        else {
            logInfo(xContext, "Release  : null", verboseInd);
        }
        if (getComponent() != null) {
            logInfo(xContext, "Component: " + getComponent().getName(), verboseInd);
        }
        else {
            logInfo(xContext, "Component: null", verboseInd);
        }
        logInfo(xContext, "DB Mode  : " + dbMode, verboseInd);
        logInfo(xContext, "Show All : " + showAll, verboseInd);
        logInfo(xContext, "Quiet    : " + quietMode, verboseInd);
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
        usage.append("Displays valid Tk Release and components.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " [-r release] [-c component] [-a] [-q] [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  release   = TK release and version (14.1 or 1401).\n");
        usage.append("  component = TK component name (einstimer, edautls, ...)\n");
        usage.append("  -a        = (optional) Show ALL valid combinations\n");
        usage.append("  -q        = (optional) Display comma delimited list of components\n");
        usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  -h        = Help (shows this information)\n");
        usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("\n");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */
    private Release_Db release;
    private boolean showAll = false;
    private boolean quietMode = false;
    private PreparedStatement lookupStmt;
    private boolean lookupOneComponent = false;
    private boolean lookupComponents = false;
    private boolean lookupRelVersions = false;
    
    /**
     * Getters.
     */
    public Release_Db getRelease() { return release; }
    public static boolean getRequestHelp() { return requestHelp; }
    public PreparedStatement getLookupStatement() { return lookupStmt; }
    public boolean isQuietMode() { return quietMode; }
    public boolean isShowAll() { return showAll; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
    
    
    /**
     * Setters.
     */
    private void setRelease(Release_Db aRel) { release = aRel;  }
    private void setShowAll(boolean aFlag) { showAll = aFlag;  }
    private void setQuietMode(boolean aFlag) { quietMode = aFlag;  }
    
    
    /**
     * Set the TkRelease object from the release name
     * @param xContext   Application context.
     * @param aReleaesName  TK release version name like 14.1 or 1401
     * @throws IcofException 
     */
    private void setRelease(EdaContext xContext, String aReleaseName) 
    throws IcofException { 
        if (getRelease() == null) {
            release = new Release_Db(xContext, aReleaseName.trim());
            release.dbLookupByName(xContext, false);
        }    
        logInfo(xContext, "Release: " + getRelease().toString(xContext), false);
    }

    /**
     * Create a PreparedStatement to lookup this object by id.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupStatement(EdaContext xContext) throws IcofException {

        // Return the statement if it already exists.
        if (lookupStmt != null) {
            return;
        }
        
        // Define the query.
        String query = QUERY;
        if (lookupRelVersions) {
            query += " and c." + Component_Db.ID_COL + " = ? ";
        }
        else if (lookupComponents) {
            query += " and v." + RelVersion_Db.ID_COL + " = ? "+
                     " and r." + Release_Db.ID_COL + " = ? ";
        }
        else if (lookupOneComponent) {
            query += " and v." + RelVersion_Db.ID_COL + " = ? " +
                     " and r." + Release_Db.ID_COL + " = ? " +
                     " and c." + Component_Db.ID_COL + " = ? ";
        }
        
        // Otherwise create a statement object and return it.
        lookupStmt = TkDbUtils.prepStatement(xContext, query);
        
    }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}

}
