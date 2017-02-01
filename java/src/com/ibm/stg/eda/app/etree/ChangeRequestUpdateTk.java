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
 * Update the Tool Kit for numerous Change Requests 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/0862012 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestActive_Db;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.CompVersion_ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.Location;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestUpdateTk extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.updateTk";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor - use default, TKs, revision and location
     *
     * @param     aContext       Application context
     * @param     aComponent     Component to associate the CR with
     */
    public ChangeRequestUpdateTk(EdaContext aContext, Component aComponent)	
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setComponent(aComponent);

    }


    /**
     * Constructor
     *
     * @param     aContext      Application context
     * @param     aComponent    Component to look up CRs for
     * @param     aMaxRevision  Location of CRs (build, dev, prod ...)
     * @param     anOldToolKit  The old ToolKit object
     * @param     aNewToolKit   The new ToolKit object

     */
    public ChangeRequestUpdateTk(EdaContext aContext, Component aComponent, 
                                 Location aLocation,
                                 ToolKit anOldToolKit,
                                 ToolKit aNewToolKit)	
                                 throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setComponent(aComponent);
	setLocation(aLocation);
	setOldToolKit(anOldToolKit);
	setNewToolKit(aNewToolKit);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ChangeRequestUpdateTk(EdaContext aContext) throws IcofException {

	this(aContext, null, null, null, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new ChangeRequestUpdateTk(null);
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
     * @throws IcofException 
     * @throws Exception 
     */
    //--------------------------------------------------------------------------
    public void process(EdaContext xContext) throws IcofException  {

	// Connect to the database
	connectToDB(xContext);

	// Update the Change Requests
	update(xContext);

	// Set the return code to success if we get this far.
	setReturnCode(xContext, SUCCESS);
	commitToDB(xContext, APP_NAME);

    }


    /**
     * Find Change Requests and update their Tool Kit from the old TK to 
     * the new TK.
     * 
     * @param xContext       Application context
     * @throws IcofException Trouble updating the ChangeRequest
     */
    public void update(EdaContext xContext) throws IcofException {

	logInfo(xContext, "Updating Change Requests ...", verboseInd);

	// Set the old and new Component_TkVersion object.
	setOldCompTk(xContext);
	setNewCompTk(xContext);

	// Locate the new Code Updates (commits) for the oldTk/component in 
	// this location
	setCodeUpdates(xContext);

	// Update the Tool Kit
	// - ActiveChangeRequest table
	// - Component_TkVersion_x_ChangeRequest table
	setChangeRequests(xContext);

	// Update the Change Requests
	deleteActiveChangeRequests(xContext);
	updateChangeRequests(xContext);

    }




    /**
     * Update the old tool kit to the new tool kit for each Change Request
     * @param xContext
     * @throws IcofException 
     */
    private void updateChangeRequests(EdaContext xContext) throws IcofException {

	logInfo(xContext, "Updating CRs ...", true);

	Iterator<Long> iter = getChangeRequests().iterator();
	while (iter.hasNext()) {

	    Long crId = (Long) iter.next();
	    CompVersion_ChangeRequest_Db cvcr = 
	    new CompVersion_ChangeRequest_Db(xContext, crId, 
	                                     getOldCompTk().getId());

	    logInfo(xContext, " Updating CR (" + crId + ") from old TK (" +
	    getOldCompTk().getId() + ") to new TK (" + getNewCompTk().getId() +")",
	    getVerboseInd(xContext));
	    cvcr.dbUpdateCompVersion(xContext, newCompTk);
	}

    }


    /**
     * Delete active Change Requests if they exist
     * @param xContext
     * @throws IcofException 
     */
    private void deleteActiveChangeRequests(EdaContext xContext) 
    throws IcofException {

	logInfo(xContext, "Deleting active CRs ...", true);

	Iterator<Long> iter = getChangeRequests().iterator();
	while (iter.hasNext()) {

	    Long crId = (Long) iter.next();
	    ChangeRequestActive_Db cr = 
	    new ChangeRequestActive_Db(xContext, (short)0, (long)0, 
	                               crId.longValue());

	    logInfo(xContext, " Deleting active CR (" + crId + ")", 
	            getVerboseInd(xContext));
	    cr.dbDeleteRowByCr(xContext);

	}

    }


    /**
     * Create a list of ChangeRequest ids for the Code Update ids
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void setChangeRequests(EdaContext xContext) throws IcofException {

	logInfo(xContext, " Querying Change Requests ...", verboseInd);

	changeRequests = new HashSet<Long>();

	Iterator<String> iter = getCodeUpdates().iterator();
	while (iter.hasNext()) {
	    String codeUpdateId = (String) iter.next();
	    Long id = Long.parseLong(codeUpdateId);
	    CodeUpdate_ChangeRequest_Db cucr = 
	    new CodeUpdate_ChangeRequest_Db(xContext, id, id);
	    Hashtable<String, ChangeRequest_Db> crs = cucr.dbLookupChangeRequests(xContext);

	    Iterator<ChangeRequest_Db> iter2 = crs.values().iterator();
	    while (iter2.hasNext()) {
		ChangeRequest_Db cr = (ChangeRequest_Db) iter2.next();
		changeRequests.add((Long)cr.getId());
		logInfo(xContext, "CU: " + id + " -> CR: " + cr.getId(), getVerboseInd(xContext));
	    }

	}

	logInfo(xContext, "Change Requests: " + getChangeRequests().size(), true);

    }


    /**
     * Determine the latest revision in the specified location for the 
     * old Tool Kit.
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void setCodeUpdates(EdaContext xContext) throws IcofException {

	// Lookup CodeUpdates (revisions) for this ComponentVersion_Location
	logInfo(xContext, " Querying for committed revisions ...", verboseInd);
	CodeUpdate_Db codeUpdate = new CodeUpdate_Db(null, "", "", "",
	                                             null, null);
	codeUpdates = codeUpdate.dbLookupIdsByCompVerLoc(xContext, 
	                                                 getOldCompTk(),
	                                                 getLocation().getLocation());

	// If no revisions then try again without a Tool Kit
	if ((codeUpdates == null) || (codeUpdates.size() < 1)) {
	    codeUpdates = codeUpdate.dbLookupRevsByCompVerLoc(xContext, 
	                                                      null,
	                                                      getComponent().getComponent(),
	                                                      getLocation().getLocation(),
	                                                      false);
	}

	logInfo(xContext, "Code Updates: " + getCodeUpdates().size(), true);

    }


    protected String readParams(Hashtable<String,String> params, String errors,
                                EdaContext xContext) throws IcofException {

	// Read the Component name
	if (params.containsKey("-c")) {
	    setComponent(xContext,  params.get("-c"));
	}
	else {
	    errors += "A Component (-c) must be specified.\n";

	}

	// Read the Location
	if (params.containsKey("-l")) {
	    setLocation(xContext, params.get("-l"));
	}
	else {
	    setLocation(xContext, "prod");
	}
	getLocation().dbLookupByName(xContext);

	// Read the old Tool Kit name
	if (params.containsKey("-o")) {
	    setOldToolKit(xContext, params.get("-o"));
	}
	else {
	    errors += "A Old Tool Kit (-o) must be specified.\n";
	}

	// Read the new Tool Kit name
	if (params.containsKey("-n")) {
	    setNewToolKit(xContext, params.get("-n"));
	}
	else {
	    errors += "A New Tool Kit (-n) must be specified.\n";
	}

	return errors;

    }


    protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-c");
	argSwitches.add("-o");
	argSwitches.add("-n");
	argSwitches.add("-l");
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	logInfo(xContext, "App         : " + APP_NAME + "  " + APP_VERSION, verboseInd);
	logInfo(xContext, "Component   : " + getComponent().getName(), verboseInd);
	logInfo(xContext, "Location    : " + getLocation().getName(), verboseInd);
	logInfo(xContext, "Old ToolKit : " + getOldToolKit().getName(), verboseInd);
	logInfo(xContext, "New ToolKit : " + getNewToolKit().getName(), verboseInd);
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
	usage.append("Updates the Tool Kit for all Change Requests assoicated\n");
	usage.append("with the specified Component, <old Tool Kit> and  \n");
	usage.append("have committed revisions less than or equal to the latest\n");
	usage.append("revision in the specified location.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-c component> <-n new_toolkit> <-o old_toolKit>\n");
	usage.append("            [-l location] [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  component     = Component name (ess, pds, model, einstimer ...).\n");
	usage.append("  new_toolkit   = New Tool Kit name (14.1.build, 15.1.build, 14.1.10, 15.1.0 ...) \n");
	usage.append("  old_toolkit   = Old Tool Kit name (14.1.build, 15.1.build, 14.1.10, 15.1.0 ...)\n");
	usage.append("  location      = Location to search for Change Requests (default = prod)\n");
	usage.append("  -y            = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode        = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h            = Help (shows this information)\n");
	usage.append("\n");
	usage.append("Return Codes\n");
	usage.append("------------\n");
	usage.append(" 0 = ok\n");
	usage.append(" 1 = error\n");
	usage.append("\n");

	System.out.println(usage);

    }


    /**
     * Members.
     */
    private String revision;
    private ToolKit newToolKit;
    private ToolKit oldToolKit;
    private Component_Version_Db oldCompTk;
    private Component_Version_Db newCompTk;
    private Vector<String> codeUpdates;
    private HashSet<Long> changeRequests;

    /**
     * Getters.
     */
    public String getRevision()  { return revision; }
    public ToolKit getNewToolKit()  { return newToolKit; }
    public ToolKit getOldToolKit()  { return oldToolKit; }
    public Component_Version_Db getOldCompTk()  { return oldCompTk; }
    public Component_Version_Db getNewCompTk()  { return newCompTk; }

    public Vector<String> getCodeUpdates()  { return codeUpdates; }
    public HashSet<Long> getChangeRequests()  { return changeRequests; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


    /**
     * Setters.
     */
    private void setNewToolKit(ToolKit aTk) { newToolKit = aTk; }
    private void setOldToolKit(ToolKit aTk) { oldToolKit = aTk; }


    /**
     * Set the new ToolKit object from the tool kit name.  If aToolKit is null
     * then the Tool Kit in the READY state will be used.
     * 
     * @param xContext    Application context.
     * @param aToolKit    ToolKit name like 14.1.1, 14.1.2 ...
     * @throws IcofException 
     */
    protected void setNewToolKit(EdaContext xContext, String aToolKit) 
    throws IcofException { 

	// User specified Tool Kit name
	if (aToolKit != null) {

	    try {
		if (getNewToolKit() == null)
		    newToolKit = new ToolKit(xContext, aToolKit.trim());
		newToolKit.dbLookupByName(xContext);
	    }
	    catch(IcofException ex) {
		logInfo(xContext, "Tool Kit (" + aToolKit + 
		        ") was not found in the database.", true);
		throw ex;
	    }

	}
	
    }


    /**
     * Set the old ToolKit object from the tool kit name.  If aToolKit is null
     * then the Tool Kit in the DEVELOPMENT state will be used.
     * 
     * @param xContext    Application context.
     * @param aToolKit    ToolKit name like 14.1.1, 14.1.2 ...
     * @throws IcofException 
     */
    protected void setOldToolKit(EdaContext xContext, String aToolKit) 
    throws IcofException { 

	// User specified Tool Kit name
	if (aToolKit != null) {

	    try {
		if (getOldToolKit() == null)
		    oldToolKit = new ToolKit(xContext, aToolKit.trim());
		oldToolKit.dbLookupByName(xContext);
	    }
	    catch(IcofException ex) {
		logInfo(xContext, "Tool Kit (" + aToolKit + 
		        ") was not found in the database.", true);
		throw ex;
	    }

	}
	
    }


    /**
     * Lookup the old Component_TkVersion object
     * @param xContext
     * @throws IcofException 
     */
    private void setOldCompTk(EdaContext xContext) throws IcofException {
	oldCompTk = new Component_Version_Db(xContext, 
	                                     getOldToolKit().getToolKit(), 
	                                     getComponent().getComponent());
	oldCompTk.dbLookupByCompRelVersion(xContext);
	logInfo(xContext, "Old CompTk: " + oldCompTk.getId(),  
	        getVerboseInd(xContext));
    }


    /**
     * Lookup the new Component_TkVersion object
     * @param xContext
     * @throws IcofException 
     */
    private void setNewCompTk(EdaContext xContext) throws IcofException {
	newCompTk = new Component_Version_Db(xContext, 
	                                     getNewToolKit().getToolKit(), 
	                                     getComponent().getComponent());
	newCompTk.dbLookupByCompRelVersion(xContext);
	logInfo(xContext, "New CompTk: " + newCompTk.getId(),  
	        getVerboseInd(xContext));
    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }


}
