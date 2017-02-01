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
 * Show the Compenents for the specified Tk Version. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 01/17/2011 GFS  Initial coding.
 * 06/09/2011 GFS  Disabled logging.  Updated to lookup db objects on the fly.
 * 10/05/2011 GFS  Enhanced to filter the component list by Component Type.
 * 11/27/2012 GFS  Refactored to use business objects and support all flavors
 *                 of the tool kit name.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Component_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ComponentType;
import com.ibm.stg.eda.component.tk_etreeobjs.StageName;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class GetComponents extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "getComponents";
    public static final String APP_VERSION = "v1.1";

    
    /**
     * Constructor
     *
     * @param     aContext    Application context
     * @param     aToolKit    A ToolKit object
     */
    public GetComponents(EdaContext aContext, ToolKit aToolKit)
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setToolKit(aToolKit);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public GetComponents(EdaContext aContext) throws IcofException {

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
	    myApp = new GetComponents(null);
	    start(myApp, argv);
	}
	catch (Exception e) {
	    handleExceptionInMain(e);
	}
	finally {
	    handleInFinallyBlock(myApp);
	}

    }


    /**
     * Add, update, delete, or report on the specified applications.
     * 
     * @param aContext      Application Context
     * @throws              IcofException
     */
    public void process(EdaContext xContext) throws IcofException {

	connectToDB(xContext);

	setComponents(xContext);
	showResults(xContext);

	rollBackDBAndSetReturncode(xContext, APP_NAME, SUCCESS);

    }


    /**
     * Show all the valid release, version and component combinations.
     * 
     * @param  xContext  Application context
     * @throws IcofException 
     */
    private void showResults(EdaContext xContext) 
    throws IcofException {

	StringBuffer results = formatResults(xContext);
	
	// If no versions found.
	if (components.size() < 1) {
	
	    if (! getToolKit().getToolKit().isLoaded())
		getToolKit().getToolKit().getRelease().dbLookupById(xContext);
	    logInfo(xContext, "No Components found for " + 
	            getToolKit().getToolKit().getDisplayName(), true);
	    
	}
	else {
	    
	    logInfo(xContext, results.toString(), true);
	
	}
	
    }


    /**
     * Format the results based on the user wishes
     *
     * @param xContext
     * @return 
     * @throws IcofException 
     */
     StringBuffer formatResults(EdaContext xContext) throws IcofException {

	StringBuffer results = new StringBuffer();
	
	Collections.sort(getComponents());
	for (Component_Db comp : getComponents()) {
	    if (isQuietMode()) {

		if (results.length() < 1)
		    results.append(comp.getName());
		else 
		    results.append("," + comp.getName());

	    }
	    else if (isNamesOnly()) {

		results.append(comp.getName() + "\n");

	    }
	    else {
		
		if (! comp.isLoaded()) 
		    comp.dbLookupById(xContext);
		results.append(comp.toString(xContext) + "\n");

	    }

	}
	
	return results;
	
    }


    /**
     * Lookup the Components for this ToolKit and Component Types
     * 
     * @param xContext Application context
     * @throws IcofException 
     */
    private void setComponents(EdaContext xContext) throws IcofException {

	// Create a list of Component Types
	setCompTypes(xContext);

	// Lookup the Components
	Component_Version_Db compVer = 
	new Component_Version_Db(getToolKit().getToolKit());

	if ((getCompTypes() == null) && (getStageName() == null)) {

	    // Lookup all Components with no filtering
	    loadComps(compVer.dbLookupAllComponents(xContext));

	}
	else if (getCompTypes() != null) {

	    // Lookup Components filtering by Component Type
	    for (String compType : getCompTypes()) {

		ComponentType ct = new ComponentType(xContext, compType);
		ct.dbLookupByName(xContext);

		loadComps(compVer.dbLookupComponents(xContext, ct.getDbObject()));

	    }

	}
	else {
	    
	    // Lookup Components filtering by Stage Name
	    Component_Db comp = new Component_Db("");
	    loadComps(comp.dbLookupByTkStage(xContext, 
	                                     getToolKit().getToolKit(),
	                                     getStageName().getStage()));

	}

    }


    /**
     * Add Vector contents to HashMap
     *
     * @param dbLookupAllComponents
     */
    private void loadComps(Vector<Component_Db> dbLookupAllComponents) {

	if (getComponents() == null)
	    components = new Vector<Component_Db>();

	for (Component_Db comp : dbLookupAllComponents) {
	    if (! getComponents().contains(comp))
		getComponents().add(comp);
	}

    }


    /**
     * Create a list of Component Type objects from the Component Types names
     * 
     * @param xContext Application context
     * @throws IcofException 
     */
    private void setCompTypes(EdaContext xContext) throws IcofException {

	if (getCompTypeNames() != null)
	    compTypes = getCompTypeNames().split("[,]");

    }


    protected void createSwitches(Vector<String> singleSwitches, 
                                  Vector<String> argSwitches) {
	
	singleSwitches.add("-y");
	singleSwitches.add("-h");
	singleSwitches.add("-q");
	singleSwitches.add("-n");
	argSwitches.add("-db");
	argSwitches.add("-t");
	argSwitches.add("-ct");
	argSwitches.add("-s");
	
    }


    protected String readParams(Hashtable<String, String> params, 
                                String errors, EdaContext xContext) 
                                throws IcofException {
	
	// Read the RelVersion
	if (params.containsKey("-t"))
	    setToolKit(xContext,  params.get("-t"));
	else
	    errors += "ToolKit (-t) is a required parameter.";

	// Read the Component TYpes
	if (params.containsKey("-ct"))
	    setCompTypeNames(params.get("-ct"));

	// Read the Stage
	if (params.containsKey("-s"))
	    setStageName(xContext, (String) params.get("-s"));
	
	// Set quiet option
	setQuietMode(false);
	if (params.containsKey("-q")) 
	    setQuietMode(true);

	// Set names only option
	setNamesOnly(false);
	if (params.containsKey("-n")) 
	    setNamesOnly(true);

	return errors;
	
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
		
	logInfo(xContext, "App      : " + APP_NAME + "  " + APP_VERSION, verboseInd);
	if (getToolKit() != null)
	    logInfo(xContext, "ToolKit  : " + getToolKit().getToolKit().getDisplayName(), verboseInd);
	else
	    logInfo(xContext, "ToolKit  : null", verboseInd);
	if (getCompTypeNames() == null)
	    logInfo(xContext, "Comp Types: null", verboseInd);
	else
	    logInfo(xContext, "Comp Types: " + getCompTypeNames().toString(), verboseInd);
	if (getStageName() != null)
	    logInfo(xContext, "Stage     : " + getStageName().getName(), verboseInd);
	else
	    logInfo(xContext, "Stage     : null", verboseInd);
	logInfo(xContext, "DB Mode  : " + dbMode, verboseInd);
	logInfo(xContext, "Quiet    : " + isQuietMode(), verboseInd);
	logInfo(xContext, "Names    : " + isNamesOnly(), verboseInd);
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
	usage.append("Displays Components for the specified Tool Kit. \n");
	usage.append("\n");
	usage.append("Components can be filtered by Component Type (run \n");
	usage.append("compTypeShow to see valid Component Types). If multiple \n");
	usage.append("Component Types are specified then the resulting Component\n");
	usage.append("list will be a union of the results.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t toolkit> [-ct comp_types] [-s stage] [-q] [-n]\n");
	usage.append("             [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  toolkit    = ToolKit name (14.1.1, 14.1.2 ...)\n");
	usage.append("  comp_types = Comma delimited list of Component Types (DEL_EDA, 32-BIT ...)\n");
	usage.append("  stage      = Filter list by Stage Name (DEVELOPMENT, PREVIEW, PRODUCTION ...)\n");
	usage.append("  -q         = (optional) Display comma delimited list of component names\n");
	usage.append("  -n         = (optional) Display component names 1 per line\n");
	usage.append("  -y         = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  -h         = Help (shows this information)\n");
	usage.append("  dbMode     = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("\n");

	System.out.println(usage);

    }


    /**
     * Members.
     */
    private StageName stageName;
    private String compTypeNames;
    private String[] compTypes;
    private List<Component_Db> components;
    private boolean quietMode = false;
    private boolean namesOnly = false;

    /**
     * Getters.
     */
    public StageName getStageName() { return stageName; }
    public boolean isQuietMode() { return quietMode; }
    public boolean isNamesOnly() { return namesOnly; }
    public String[] getCompTypes() { return compTypes; }
    public List<Component_Db> getComponents() { return components; }
    public String getCompTypeNames() { return compTypeNames; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


    /**
     * Setters.
     */
    private void setQuietMode(boolean aFlag) { quietMode = aFlag;  }
    private void setNamesOnly(boolean aFlag) { namesOnly = aFlag;  }
    private void setCompTypeNames(String names) { compTypeNames = names;  }


    /**
     * Set the StageName object
     * @param xContext    Application context.
     * @param aName       Stage name
     * @throws IcofException 
     */
    private void setStageName(EdaContext xContext, String aName) 
    throws IcofException { 
	if (getStageName() == null) {
	    stageName = new StageName(xContext, aName.trim());
	    stageName.dbLookupByName(xContext);
	}    
	logInfo(xContext, "Stage: " + getStageName().toString(xContext),
	        getVerboseInd(xContext));
    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

}
