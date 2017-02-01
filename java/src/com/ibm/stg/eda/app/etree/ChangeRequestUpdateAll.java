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
 * Update all fields of a ChangeRequest. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/08/2011 GFS  Initial coding - from old ChangeRequestUpdateAll.java
 * 08/17/2011 GFS  Update to allow the Tool Kit to be changed.
 * 09/06/2011 GFS  Updated to support new ChangeRequest defect/feature and severity.
 * 09/09/2011 GFS  Updated so help is shown if no parm specified.
 * 09/16/2011 GFS  Change -cq switch to -cr.
 * 11/09/2012 GFS  Updated to support -cqtk switch which will look up the
 *                 tool kit by the CQ release name.
 * 11/30/2012 GFS  Updated to support CR's new impacted customer attribute.
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
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestType_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.CompVersion_ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestSeverity;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestStatus;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestType;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestUpdateAll extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "changeReqUpdateAll";
    public static final String APP_VERSION = "v1.1";


    /**
     * Constructor
     *
     * @param     aContext      Application context
     * @param     aComponent    New Component to be associate the CR with
     * @param     aDescription  ChangeRequest's description
     * @param     aCqId         ChangeRequest's CQ number
     * @param     aStatus       New ChangeRequest's status
     * @param     newTk         New ToolKit object
     * @param     newType       ChangeRequest's type
     * @param     newSeverity   ChangeRequest's severity
     * @param     newCustomer   Impacted customer
     * @param     newUser       Person making the update
     */
    public ChangeRequestUpdateAll(EdaContext aContext, Component aComponent,
                                  String aDescription, String aCqId,
                                  ChangeRequestStatus newStatus, 
                                  ToolKit newTk,
                                  ChangeRequestType newType,
                                  ChangeRequestSeverity newSeverity,	
                                  String newCustomer,
                                  User_Db aUser)	
                                  throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setNewComponent(aComponent);
	setNewDescription(aDescription);
	setClearQuest(aCqId);
	setNewStatus(newStatus);
	setNewToolKit(newTk);
	setNewType(newType);
	setNewSeverity(newSeverity);
	setNewCustomer(newCustomer);
	setUser(aUser);

	setChangeRequest(aContext);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ChangeRequestUpdateAll(EdaContext aContext) throws IcofException {

	this(aContext, null, null, null, null, null, null, null, null, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new ChangeRequestUpdateAll(null);
	    start(myApp, argv);
	}

	catch (Exception e) {
	    handleExceptionInMain(e);
	}
	finally {
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

	// Lookup existing ChangeRequest.
	setChangeRequest(xContext);

	// Update the ToolKit, Component and/or Change Request if requested
	updateToolKit(xContext);
	updateComponent(xContext);
	updateChangeRequest(xContext);

	// Set the return code to success if we get this far.
	setReturnCode(xContext, SUCCESS);
	commitToDB(xContext, APP_NAME);

    }


    /**
     * Update the ToolKit for specified ChangeRequest object.
     * @param xContext       Application context
     * @throws IcofException Trouble updating the ChangeRequest
     */
    public void updateToolKit(EdaContext xContext) throws IcofException {

	logInfo(xContext, "Updating ToolKit ...", verboseInd);

	// Do nothing if Component wasn't specified by the user.
	if (getNewToolKit() == null) {
	    return;
	}

	// Determine if there are any CodeUpdates for this ChangeRequest.
	// If so throw an exception otherwise allow the updating the Component.
	if (hasExistingUpdates(xContext)) {
	    logInfo(xContext, " Can't update ToolKit ...", true);
	    IcofException ie =
	    new IcofException(APP_NAME,  "updateToolKit()",
	                      IcofException.SEVERE,
	                      "Can't update the ToolKit for this " + 
	                      "ChangeRequest since \nchanges " + 
	                      "have already been committed against this ChangeRequest.",
	    "");
	    throw ie;
	}

	// Lookup the ComponentTkVersion objects for this ChangeRequest
	CompVersion_ChangeRequest_Db cvcr;
	cvcr = new CompVersion_ChangeRequest_Db(getChangeRequest().getChangeRequest(),
	                                        null);
	Vector<Component_Version_Db>  compVers = cvcr.dbLookupCompVersions(xContext);

	// Update the ToolKit (minor version only) for the existing 
	// ComponentTkVersion_x_ChangeRequest
	Iterator<Component_Version_Db> iter = compVers.iterator();
	while (iter.hasNext()) {

	    Component_Version_Db oldCv =   iter.next();

	    // Lookup the new Component Version row
	    Component_Version_Db newCv = 
	    new Component_Version_Db(oldCv.getCompRelease(), 
	                             getNewToolKit().getToolKit());
	    newCv.dbLookupByCompRelVersion(xContext);

	    // Find the existing CompVersion_x_ChangeRequest row and update it
	    cvcr = null;
	    cvcr = new CompVersion_ChangeRequest_Db(getChangeRequest().getChangeRequest(),
	                                            oldCv);
	    cvcr.dbUpdateCompVersion(xContext, newCv);

	}

	logInfo(xContext, "ToolKit update complete", true);

    }


    /**
     * Determine if there are CodeUpdates (commits) against this ChangeRequest.
     * @param xContext  Application context
     * @return          True if this CR has commits against it otherwise false
     * @throws IcofException
     */
    private boolean hasExistingUpdates(EdaContext xContext) throws IcofException {

	// Determine if there are any CodeUpdates for this ChangeRequest.
	// If so throw an exception otherwise allow the updating the Component.
	getChangeRequest().setCodeUpdates(xContext);
	Hashtable<String,CodeUpdate_Db> codeUpdates = getChangeRequest().getCodeUpdates();
	if ((codeUpdates != null) && (codeUpdates.size() > 0)) {
	    return true;
	}

	return false;

    }


    /**
     * Lookup the current ChangeRequest.
     * @param xContext  Application context.
     * @throws IcofException 
     */
    protected void setChangeRequest(EdaContext xContext) throws IcofException {

	// Return if the CQ number is not set
	if (getClearQuest() == null) {
	    return;
	}

	logInfo(xContext, "Reading ChangeRequest from DB ...", verboseInd);

	changeRequest = new ChangeRequest(xContext, getClearQuest());
	changeRequest.dbLookupByCq(xContext);
	changeRequest.getChangeRequest().getStatus().dbLookupById(xContext);

	logInfo(xContext, "ChangeRequest: " + changeRequest.toString(xContext), verboseInd);
	logInfo(xContext, "ChangeRequest found", verboseInd);
    }


    /**
     * Update the Component for specified ChangeRequest object.
     * @param xContext       Application context
     * @throws IcofException Trouble updating the ChangeRequest
     */
    public void updateComponent(EdaContext xContext) throws IcofException {

	logInfo(xContext, "Updating Component ...", verboseInd);

	// Do nothing if Component wasn't specified by the user.
	if (getNewComponent() == null) {
	    return;
	}

	// Determine if there are any CodeUpdates for this ChangeRequest.
	// If so throw an exception otherwise allow the updating the Component.
	if (hasExistingUpdates(xContext)) {
	    logInfo(xContext, " Can't update Component ...", true);
	    IcofException ie =
	    new IcofException(APP_NAME,  "updateComponent()",
	                      IcofException.SEVERE,
	                      "Can't update the Component for this " + 
	                      "ChangeRequest since \nchanges " + 
	                      "have already been committed against this ChangeRequest.",
	    "");
	    throw ie;
	}

	// Lookup the ToolKit for this ChangeRequest.
	ToolKit toolKit = getChangeRequest().getToolKit(xContext);

	// Lookup the new ComponentTkVersion
	Component_Version_Db newCv = 
	new Component_Version_Db(xContext, toolKit.getToolKit(),
	                         getNewComponent().getComponent());
	newCv.dbLookupByCompRelVersion(xContext);

	// Lookup the ComponentTkVersion objects for this ChangeRequest object
	CompVersion_ChangeRequest_Db cvcr;
	cvcr =new CompVersion_ChangeRequest_Db(getChangeRequest().getChangeRequest(),
	                                       null);
	Vector<Component_Version_Db>  compVers = cvcr.dbLookupCompVersions(xContext);

	// Update the ComponentTkVersion for the existing 
	// ComponentTkVersion_x_ChangeRequest object to be the new ComponentTkVersion.
	if ((compVers != null) && (compVers.size() == 1)) {

	    Component_Version_Db oldCv;
	    oldCv = (Component_Version_Db) compVers.firstElement();

	    cvcr = new CompVersion_ChangeRequest_Db(getChangeRequest().getChangeRequest(),
	                                            oldCv);
	    cvcr.dbUpdateCompVersion(xContext, newCv);

	    logInfo(xContext, "Component update complete", true);

	}
	else {

	    logInfo(xContext, " More than 1 Component is associated with this ChangeRequest...", 
	            true);
	    IcofException ie = new IcofException(APP_NAME,  "updateComponent()",
	                                         IcofException.SEVERE,
	                                         "Can't update Component for this " + 
	                                         "ChangeRequest since it already " +
	                                         "has CodeUpdates associated with it.",
	    "");
	    throw ie;

	}
    }


    /**
     * Update the specified ChangeRequest object.
     * @param xContext       Application context
     * @throws IcofException Trouble updating the ChangeRequest
     */
    public void updateChangeRequest(EdaContext xContext) throws IcofException {

	logInfo(xContext, "Updating ChangeRequest ...", verboseInd);

	// Set the description
	String myCustomer = getChangeRequest().getImpactedCustomer();
	if (getNewCustomer() != null) {
	    myCustomer = getNewCustomer();
	    logInfo(xContext, " Using new Impacted Customer ...", verboseInd);
	}

	// Set the description
	String myDescription = getChangeRequest().getDescription();
	if (getNewDescription() != null) {
	    myDescription = getNewDescription();
	    logInfo(xContext, " Using new description ...", verboseInd);
	}

	// Set the type
	ChangeRequestType myType = getChangeRequest().getType();
	if (getNewType() != null) {
	    myType = getNewType();
	    logInfo(xContext, " Using new type ...", verboseInd);
	}

	// Set the severity
	ChangeRequestSeverity mySeverity = getChangeRequest().getSeverity();
	if (getNewSeverity() != null) {
	    mySeverity = getNewSeverity();
	    logInfo(xContext, " Using new severity ...", verboseInd);
	}

	// Set the status
	ChangeRequestStatus myStatus = getChangeRequest().getStatus();
	if (getNewStatus() != null) {
	    myStatus = getNewStatus();
	    logInfo(xContext, " Using new status ...", verboseInd);
	}


	// Update this ChangeRequest
	getChangeRequest().dbUpdate(xContext, 
	                            getChangeRequest().getClearQuest(), 
	                            myDescription, 
	                            myStatus, 
	                            myType, 
	                            mySeverity, 
	                            myCustomer, 
	                            getUser());

	logInfo(xContext, "Update complete", true);

    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, verboseInd);
	logInfo(xContext, "ClearQuest : " + getClearQuest(), verboseInd);
	if (getNewComponent() != null)
	    logInfo(xContext, "Component  : " + getNewComponent().getName(), verboseInd);
	else 
	    logInfo(xContext, "Component  : null", verboseInd);
	if (getNewDescription() != null)
	    logInfo(xContext, "Description: " + getNewDescription(), verboseInd);
	else 
	    logInfo(xContext, "Description: null", verboseInd);
	if (getNewStatus() != null)
	    logInfo(xContext, "Status     : " + getNewStatus().getName(), verboseInd);
	else 
	    logInfo(xContext, "Status     : null", verboseInd);
	if (getNewType() != null)
	    logInfo(xContext, "Type       : " + getNewType().getName(), verboseInd);
	else 
	    logInfo(xContext, "Type       : null", verboseInd);
	if (getNewSeverity() != null)
	    logInfo(xContext, "Severity   : " + getNewSeverity().getName(), verboseInd);
	else 
	    logInfo(xContext, "Severity   : null", verboseInd);
	if (getNewToolKit() != null)
	    logInfo(xContext, "ToolKit    : " + getNewToolKit().getName(), verboseInd);
	else 
	    logInfo(xContext, "ToolKit    : null", verboseInd);
	if (getNewCustomer() != null)
	    logInfo(xContext, "Customer   : " + getNewCustomer(), verboseInd);
	else 
	    logInfo(xContext, "Customer   : null", verboseInd);

	logInfo(xContext, "DB Mode    : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose    : " + getVerboseInd(xContext), verboseInd);
    }


    protected String readParams(Hashtable<String,String> params, String errors,
                                EdaContext xContext) throws IcofException {
	// Read the Component name
	if (params.containsKey("-c")) {
	    setNewComponent(xContext, (String) params.get("-c"));
	}

	// Read the ClearQuest name
	if (params.containsKey("-cr")) {
	    setClearQuest((String) params.get("-cr"));
	}
	else if (params.containsKey("-cq")) {
	    setClearQuest((String) params.get("-cq"));
	}
	else {
	    errors += "Change Request(-cr) is a required parameter\n";
	}

	// Read the impacted customer
	if (params.containsKey("-cust")) {
	    setNewCustomer((String) params.get("-cust"));
	}

	// Read the status
	if (params.containsKey("-s")) {
	    setNewStatus(xContext, (String) params.get("-s"));
	}

	// Read the ToolKit
	if (params.containsKey("-t")) {
	    setNewToolKit(xContext, (String) params.get("-t"));
	}

	// Read the description
	if (params.containsKey("-d")) {
	    setNewDescription((String) params.get("-d"));
	}

	// Set the new Type flag
	if (params.containsKey("-defect")) {
	    setNewType(xContext, ChangeRequestType_Db.DEFECT);
	}
	if (params.containsKey("-feature")) {
	    setNewType(xContext, ChangeRequestType_Db.FEATURE);
	}
	if (params.containsKey("-defect") && params.containsKey("-feature")) {
	    errors += "Please specify either a defect (-defect) or feature " + 
	    "(-feature) but not both.\n";
	}

	// Set the Severity
	if (params.containsKey("-sev")) {
	    setNewSeverity(xContext, (String) params.get("-sev"));
	}

	// Verify status, component, description, feature/defect or severity 
	// was set
	if ((getNewDescription() == null) && 
	(getNewStatus() == null) && 
	(getNewComponent() == null) &&
	(getNewType() == null) &&
	(getNewSeverity() == null) &&
	(getNewToolKit() == null) &&
	(getNewCustomer() == null)) {
	    errors += "A new description(-d), component(-c), status(-s), " + 
	    "defect/feature(-defect/-feature), severity(-sev), "+
	    "tool kit(-t) or impacted customer(-cust) must be specified.\n";
	}
	return errors;
    }


    protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
	singleSwitches.add("-y");
	singleSwitches.add("-h");
	singleSwitches.add("-defect");
	singleSwitches.add("-feature");
	argSwitches.add("-db");
	argSwitches.add("-cq");
	argSwitches.add("-cust");
	argSwitches.add("-cr");
	argSwitches.add("-s");
	argSwitches.add("-d");
	argSwitches.add("-c");
	argSwitches.add("-t");
	argSwitches.add("-sev");
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Updates 1 or more attributes of the specified ChangeRequest.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-cr ChangeRequest> \n");
	usage.append("                   <-s status | -d description | -c component | \n");
	usage.append("                    -t tool_kit | -cust customer |\n");
	usage.append("                    -feature | -defect | -sev severity>\n");
	usage.append("                   [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  ChangeRequest = A Change Request id (MDCMS######### ...).\n");
	usage.append("  status        = New ChangeRequest state (new, ready, dev_complete ...)\n");
	usage.append("  description   = New customer facing description of this change (release note)\n");
	usage.append("                  which will replace existing change description (3000 chars max)\n");
	usage.append("  component     = New Component name (ess, pds, model, einstimer ...).\n");
	usage.append("  tool_Kit      = New ToolKit name (14.1.0, 14.1.1 ...).\n");
	usage.append("  -defect       = Specify if this ChangeRequest is a defect.\n");
	usage.append("  -feature      = Specify if this ChangeRequest is a feature.\n");
	usage.append("  severity      = Defect/feature impact/priority (1, 2, 3, 4)\n");
	usage.append("                  where 1 means \"extreme impact/resolve immediately\" down to\n");
	usage.append("                        4 means \"very little impact/work into future release\"\n");
	usage.append("  customer      = Customer impacted by this change request\n");
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
    private Component newComponent;
    private String newDescription;
    private String clearQuest;
    private String newCustomer;
    private ChangeRequestStatus newStatus;
    private ChangeRequestType newType;
    private ChangeRequestSeverity newSeverity;
    private ToolKit newToolKit;

    /**
     * Getters.
     */
    public Component getNewComponent()  { return newComponent; }
    public String getNewDescription()  { return newDescription; }
    public String getClearQuest()  { return clearQuest; }
    public String getNewCustomer()  { return newCustomer; }
    public ChangeRequestStatus getNewStatus()  { return newStatus; }
    public ChangeRequestType getNewType()  { return newType; }
    public ChangeRequestSeverity getNewSeverity()  { return newSeverity; }
    public ToolKit getNewToolKit()  { return newToolKit; }
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }


    /**
     * Setters.
     */
    private void setNewComponent(Component aComp) { newComponent = aComp;  }
    private void setNewDescription(String aDesc) { newDescription = aDesc; }
    private void setClearQuest(String aRecord) { clearQuest = aRecord; }
    private void setNewStatus(ChangeRequestStatus aStatus) { newStatus = aStatus; }
    private void setNewType(ChangeRequestType aType) { newType = aType; }
    private void setNewSeverity(ChangeRequestSeverity aSev) { newSeverity = aSev; }
    private void setNewToolKit(ToolKit aTk) { newToolKit = aTk; };
    private void setNewCustomer(String aCust) { newCustomer = aCust; };


    /**
     * Set the Component object from the component name
     * @param xContext  Application context.
     * @param aName     Component name like ess, edautils, pds ...
     * @throws IcofException 
     */
    private void setNewComponent(EdaContext xContext, String aName) 
    throws IcofException { 
	if (getNewComponent() == null) {
	    newComponent = new Component(xContext, aName.trim());
	    newComponent.dbLookupByName(xContext);
	}    
	logInfo(xContext, "Component: " + getNewComponent().toString(xContext), false);
    }


    /**
     * Set the ToolKit object from the tool_kit name
     * @param xContext  Application context.
     * @param aName     Tool Kit name like 14.1.0, 14.1.1 ...
     * @param bIsCqName True if aName is the CQ name otherwise false
     * @throws IcofException 
     */
    private void setNewToolKit(EdaContext xContext, String aName) 
    throws IcofException { 

	if (getNewToolKit() == null) {
	    newToolKit = new ToolKit(xContext, aName.trim());
	    newToolKit.dbLookupByName(xContext);
	}    
	logInfo(xContext, "ToolKit: " + getNewToolKit().toString(xContext), false);
    }


    /**
     * Set the ChangeRequestStatus_Db object from the status name
     * @param xContext  Application context.
     * @param aName     Version name like dev_complete ...
     * @throws IcofException 
     */
    private void setNewStatus(EdaContext xContext, String aName) 
    throws IcofException { 
	if (getNewStatus() == null) {
	    newStatus = new ChangeRequestStatus(xContext, aName.trim().toUpperCase());
	    newStatus.dbLookupByName(xContext);
	}    
	logInfo(xContext, "Status: " + getNewStatus().toString(xContext), false);
    }

    /**
     * Set the ChangeRequestType_Db object from the type name
     * 
     * @param xContext  Application context.
     * @param aName     Type name.
     * @throws IcofException 
     */
    private void setNewType(EdaContext xContext, String aName) 
    throws IcofException { 

	if (getNewType() == null) {
	    newType = new ChangeRequestType(xContext, aName.trim().toUpperCase());
	    newType.dbLookupByName(xContext);
	}    
	logInfo(xContext, "Type: " + getNewType().toString(xContext), 
	        getVerboseInd(xContext));

    }


    /**
     * Set the ChangeRequestSeverity_Db object from the severity name
     * 
     * @param xContext  Application context.
     * @param aName     Severity name.
     * @throws IcofException 
     */
    private void setNewSeverity(EdaContext xContext, String aName) 
    throws IcofException { 

	if (getNewSeverity() == null) {
	    newSeverity = new ChangeRequestSeverity(xContext, aName.trim().toUpperCase());
	    newSeverity.dbLookupByName(xContext);
	}    
	logInfo(xContext, "Severity: " + getNewSeverity().toString(xContext), 
	        getVerboseInd(xContext));

    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

}