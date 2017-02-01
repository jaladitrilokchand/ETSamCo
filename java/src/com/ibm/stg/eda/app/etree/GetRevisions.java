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
* Show the Revisions for a given location/toolkit. 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 12/20/2011 GFS  Initial coding.
* 11/27/2012 GFS  Refactored to use business objects and support all flavors
*                 of the tool kit name.
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
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Location_Db;
import com.ibm.stg.eda.component.tk_etreedb.Location_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.Location;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class GetRevisions extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "getRevisions";
    public static final String APP_VERSION = "v1.0";

    /**
     * Constructor
     *
     * @param     aContext    Application context
     * @param     aToolKit    A ToolKit object
     * @param     aComponent  A Component object
     * @param     aLocation   A Location_Db object
     */
    public GetRevisions(EdaContext aContext, ToolKit aToolKit, 
    		            Component aComponent, Location aLocation)
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);
        setToolKit(aToolKit);
        setComponent(aComponent);
        setLocation(aLocation);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public GetRevisions(EdaContext aContext) throws IcofException {

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

			myApp = new GetRevisions(null);
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

        // Look up the Revisions
        getToolKit().validateLocation(xContext, getLocation());
        setCompVersion(xContext);
        setCompVerLocation(xContext);
        setRevisions(xContext);
        
        // Show the results.
        showRevisions(xContext);
        
        // Set the return code.
        setReturnCode(xContext, SUCCESS);
        
        rollBackDB(xContext, APP_NAME);
        
    }

    /**
     * Determine the revisions for this ToolKit, component and location
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void setRevisions(EdaContext xContext) throws IcofException {
    	
        logInfo(xContext, "Reading revisions from DB ...", verboseInd);
    	
    	// Lookup CodeUpdates (revisions) for this ComponentVersion_Location
        logInfo(xContext, " Querying DB for revisions ...", verboseInd);
    	CodeUpdate_Db codeUpdate = new CodeUpdate_Db(getCompVersion(), 
    			                                     "", "", "", null, null);
    	codeUpdates = 
    		codeUpdate.dbLookupRevsByCompVerLoc_old(xContext, 
    		                                        getCompVerLocation(),
    		                                        getShowLatest());
    	
	}

    
    /**
     * Lookup the ComponentVersion.
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void setCompVersion(EdaContext xContext) throws IcofException {

        logInfo(xContext, "Reading ComponentVersion from DB ...", verboseInd);
    	
    	// Lookup the ComponentVersion_Loocation row.
        compVersion = new Component_Version_Db(xContext, getToolKit().getToolKit(), 
        		                               getComponent().getComponent());

    	try {
    		compVersion.dbLookupByCompRelVersion(xContext);
    	}
    	catch(IcofException trap) {
    		xContext.getSessionLog().log(trap);
    		String msg = "Component (" + getComponent().getName() + ") is not " +
    		             "a member this ToolKit (" + getToolKit().getToolKit().getDisplayName() + ").";
    		throw new IcofException(this.getClass().getName(), "setRevisions()", 
    				                IcofException.SEVERE, msg, "");
    	}
    	
        logInfo(xContext, " Found ComponentVersion: " + compVersion.getId(), verboseInd);
    	
	}

    
    /**
     * Lookup the ComponentVersionLocation.
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void setCompVerLocation(EdaContext xContext) throws IcofException {

        logInfo(xContext, "Reading ComponentVersionLocation from DB ...", verboseInd);
    	
    	// Lookup the ComponentVersion_Loocation row.
        compVerLocation = new Component_Version_Location_Db(getCompVersion(), 
                                                            getLocation().getLocation());
    	try {
    		compVerLocation.dbLookupByIds(xContext);
    	}
    	catch(IcofException trap) {
    		xContext.getSessionLog().log(trap);
    		String msg = "Component (" + getComponent().getName() + ") and " +
    		             "ToolKit (" + getToolKit().getToolKit().getDisplayName() + ") were not " +
    		             "found in this Location (" + getLocation().getName() + ").";
    		throw new IcofException(this.getClass().getName(), "setRevisions()", 
    				                IcofException.SEVERE, msg, "");
    	}

    	logInfo(xContext, " Found ComponentVersionLocation: " + 
    			compVerLocation.getId(), verboseInd);
    	
	}


	/**
     * Show all the valid release, version and component combinations.
     * 
     * @param  xContext  Application context
     * @throws IcofException 
     */
    private void showRevisions(EdaContext xContext) 
    throws IcofException {

        // Show the results
    	String results = "";
    	Iterator<String>  iter = getRevisions().iterator();
    	while (iter.hasNext()) {
    		String revision =  iter.next();
    		if (results.length() < 1)
    			results = revision;
    		else 
    			results += "," + revision;
    	}
    	
        // If no revisions found.
        if (getRevisions().size() < 1) {
        	System.out.println("No revisions found for " + 
        	                   getToolKit().getToolKit().getDisplayName() +
        			           " " + getComponent().getName() + " in " +
        			           getLocation().getName());
        }
        else {
        	System.out.println(results);
        }
        
    }

	protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        singleSwitches.add("-latest");
        argSwitches.add("-db");
        argSwitches.add("-t");
        argSwitches.add("-c");
        argSwitches.add("-l");
	}


	protected String readParams(Hashtable<String,String> params, String errors,
			EdaContext xContext) throws IcofException{
		// Read the ToolKit 
        if (params.containsKey("-t")) {
            setToolKit(xContext, params.get("-t"));
        }
        else {
        	errors += "ToolKit (-t) is a required parameter.";
        }

        // Read the Component
        if (params.containsKey("-c")) {
            setComponent(xContext,  params.get("-c"));
        }
        else {
        	errors += "Component (-c) is a required parameter.";
        }

        // Read the Location
        if (params.containsKey("-l")) {
        	String myLoc =  params.get("-l");
            setLocation(xContext, myLoc.toUpperCase());
            getLocation().dbLookupByName(xContext);
        }
        else {
        	errors += "Location (-l) is a required parameter.";
        }
        
        // Read the latest switch.
        setShowLatest(false);
        if (params.containsKey("-latest")) {
        	setShowLatest(true);
        }
		return errors;
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		boolean verboseInd = getVerboseInd(xContext);
        logInfo(xContext, "App      : " + APP_NAME + "  " + APP_VERSION, verboseInd);
        if (getToolKit() != null) {
            logInfo(xContext, "Version  : " + getToolKit().getName(), verboseInd);
        }
        else {
            logInfo(xContext, "Version  : null", verboseInd);
        }
        if (getComponent() != null) {
            logInfo(xContext, "Component: " + getComponent().getName(), verboseInd);
        }
        else {
            logInfo(xContext, "Component: null", verboseInd);
        }
        if (getLocation() != null) {
            logInfo(xContext, "Location : " + getLocation().getName(), verboseInd);
        }
        else {
            logInfo(xContext, "Location : null", verboseInd);
        }

        logInfo(xContext, "Latest   : " + getShowLatest(), verboseInd);
        logInfo(xContext, "DB Mode  : " + dbMode, verboseInd);
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
        usage.append("Displays the revisions for the specified Tool Kit, component\n");
        usage.append("and location. To see a single version try -last (latest).\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-t toolkit> <-c component> <-l location>\n");
        usage.append("             [-latest] [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  toolkit   = ToolKit name (14.1.1, 14.1.2 ...)\n");
        usage.append("  component = Component name (einstimer, model ...)\n");
        usage.append("  location  = Location name (build, dev, prod, shipb, tkb, xtinct/tk14.1.2 ...)\n");
        usage.append("  -latest   = (optional) shows the latest revision only\n");
        usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  -h        = Help (shows this information)\n");
        usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("\n");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */
    private Component_Db component;
    private Component_Version_Db compVersion;
    private Component_Version_Location_Db compVerLocation;
    private boolean showLatest = false;
    private Vector<String>  codeUpdates;
    
    /**
     * Getters.
     */
    public Component_Version_Db getCompVersion() { return compVersion; }
    public Component_Version_Location_Db getCompVerLocation() { return compVerLocation; }
    public boolean getShowLatest() { return showLatest; }
    public Vector<String>  getRevisions() { return codeUpdates; }
    public static boolean getRequestHelp() { return requestHelp; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
    
    /**
     * Setters.
     */
    private void setShowLatest(boolean aFlag) { showLatest = aFlag;  }
    


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}
    


}
