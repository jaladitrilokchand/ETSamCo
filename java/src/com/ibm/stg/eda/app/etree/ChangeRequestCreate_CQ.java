/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2012 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *
 *-PURPOSE---------------------------------------------------------------------
 * Create a new ChangeRequest. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 09/20/2012 GFS  Initial coding. Refactored existing classes.
 * 11/27/2012 GFS  Refactored to use business objects and support all flavors
 *                 of the tool kit name.
 * 11/29/2012 GFS  Updated to support CR's new impacted customer attribute.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkConstants;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestStatus_Db;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestType_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.StageName_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestSeverity;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestStatus;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestType;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestCreate_CQ extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "changeReqCreate";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aToolKit       ToolKit to associate this CR with
     * @param     aComponent     Component to associate the CR with
     * @param     aDescription   ChangeRequest's description
     * @param     aCqId          ChangeRequest's CQ number
     * @param     aStatus        ChangeRequest's status
     * @param     aType          ChangeRequest's type
     * @param     aSeverity      ChangeRequest's severity
     * @param     aCreator       ChangeRequest's creator
     * @param     aCustomer      Customer impacted by this change request
     */
    public ChangeRequestCreate_CQ(EdaContext aContext, 
                                  ToolKit aToolKit, Component aComponent,
                                  String aDescription, String aCqId,
                                  ChangeRequestStatus aStatus,
                                  ChangeRequestType aType,
                                  ChangeRequestSeverity aSeverity,
                                  User_Db aCreator,
                                  String aCustomer)	
                                  throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setToolKit(aToolKit);
	setComponent(aComponent);
	setDescriptionText(aDescription);
	setClearQuest(aCqId);
	setStatus(aStatus);
	setType(aType);
	setSeverity(aSeverity);
	setUser(aCreator);
	setImpactedCustomer(aCustomer);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ChangeRequestCreate_CQ(EdaContext aContext) throws IcofException {

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
	    myApp = new ChangeRequestCreate_CQ(null);
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
     * @throws IcofException 
     * Add, update, delete, or report on the specified applications.
     * 
     * @param aContext      Application Context
     * @throws  
     */
    public void process(EdaContext xContext) throws IcofException   {

	// Connect to the database
	connectToDB(xContext);

	// Create the CR
	create(xContext);

	// Commit the changes
	//rollBackDB(xContext, APP_NAME);
	commitToDB(xContext, APP_NAME);

    }


    /**
     * Create the ChangeRequest
     * 
     * @param xContext  Application context
     * @throws IcofException
     */
    public void create(EdaContext xContext) throws IcofException {

	// Create the new ChangeRequest.
	setStatus(xContext);

	// Create the new ChangeRequest.
	ChangeRequestCreateBase cr = new ChangeRequestCreateBase(xContext, 
	                                                         getComponent(), 
	                                                         getDescriptionText(), 
	                                                         getDescriptionFile(),
	                                                         getClearQuest(),
	                                                         getStatus(), 
	                                                         getType(), 
	                                                         getSeverity(), 
	                                                         getUser(), 
	                                                         getVerboseInd(xContext),
	                                                         getToolKit(),
	                                                         getImpactedCustomer());
	cr.create(xContext);

	// Set the return code to success if we get this far.
	logInfo(xContext, "Change Request created in " +
	        getStatus().getName() + " state", true);

	setReturnCode(xContext, SUCCESS);

    }


    /**
     * Define the command line switches
     */
    protected void createSwitches(Vector<String> singleSwitches, 
                                  Vector<String> argSwitches) {
	singleSwitches.add("-y");
	singleSwitches.add("-h");
	singleSwitches.add("-feature");
	singleSwitches.add("-defect");
	argSwitches.add("-db");
	argSwitches.add("-cq");
	argSwitches.add("-d");
	argSwitches.add("-c");
	argSwitches.add("-cust");
	argSwitches.add("-t");
	argSwitches.add("-f");
	argSwitches.add("-sev");
	argSwitches.add("-t");
	argSwitches.add("-u");
    }


    protected String readParams(Hashtable<String,String> params, String errors,
                                EdaContext xContext) throws IcofException {

	// Read the Component name and set revered flag
	if (params.containsKey("-c")) {
	    setComponent(xContext,  params.get("-c"));
	}
	else {
	    errors += "Component (-c) is a required parameter\n";
	}

	// Read the ToolKit name
	if (! isReserved() && params.containsKey("-t")) {
	    setToolKit(xContext,  params.get("-t"));
	}
	else {
	    if (! isReserved()) {
		errors += "ToolKit (-t) is a required parameter\n";
	    }
	}

	// Set the Type
	if (isReserved()) {
	    setType(xContext, ChangeRequestType_Db.RESERVED);
	}
	else {
	    if (params.containsKey("-defect")) {
		setType(xContext, ChangeRequestType_Db.DEFECT);
	    }
	    else if (params.containsKey("-feature")) {
		setType(xContext, ChangeRequestType_Db.FEATURE);
	    }
	    else {
		errors += "Either a defect (-defect) or feature (-feature) must " +
		"be specified.\n";
	    }
	    if (params.containsKey("-defect") && params.containsKey("-feature")) {
		errors += "Please specify either a defect (-defect) or feature " + 
		"(-feature) but not both.\n";
	    }
	}

	// Set the Severity
	if (isReserved()) {
	    setSeverity(xContext, "4");
	}
	else {
	    if (params.containsKey("-sev")) {
		setSeverity(xContext,  params.get("-sev"));
	    }
	    else {
		errors += "Severity (-sev) is a required parameter\n";
	    }
	}

	// Read the ClearQuest name
	if (params.containsKey("-cq")) {
	    setClearQuest((String) params.get("-cq"));
	    if (getClearQuest().length() > TkConstants.MAX_CQ_SIZE) {
		errors += "A ClearQuest record can't be longer than " +
		TkConstants.MAX_CQ_SIZE + " characters\n";
	    }
	}

	// Read the developer's intranet id
	if ((! isReserved()) && params.containsKey("-u")) {
	    setDeveloper(xContext, (String)params.get("-u"));
	}

	// Validate the usage.
	if ((getClearQuest() == null)) {
	    errors += "You must specify the ChangeRequest (-cq) or -auto parameter\n";
	}

	// Read the description text
	if (params.containsKey("-d")) {
	    setDescriptionText( params.get("-d"));
	}

	// Read the description file
	if (params.containsKey("-f")) {
	    setDescriptionFile(params.get("-f"));
	}
	// Verify both description file and text were not specified.
	if ((getDescriptionText() != null) && (getDescriptionFile() != null)) {
	    errors += "Description can be specified via a file (-f) or text (-d) but NOT both.\n";
	}
	else if ((getDescriptionText() == null) && (getDescriptionFile() == null)) {
	    errors += "Description must be specified via a file (-f) or text (-d).\n";
	}

	// Set the Impacated Customer
	if (params.containsKey("-cust")) {
	    setImpactedCustomer(params.get("-cust"));
	}

	return errors;
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, 
	        getVerboseInd(xContext));
	logInfo(xContext, "ClearQuest : " + getClearQuest(), 
	        getVerboseInd(xContext));
	if (getDescriptionText() != null)
	    logInfo(xContext, "Description: " + getDescriptionText(), 
	            getVerboseInd(xContext));
	if (getDescriptionFile() != null)
	    logInfo(xContext, "Description: " + getDescriptionFile(), 
	            getVerboseInd(xContext));
	if (getType() != null)
	    logInfo(xContext, "Type       : " + getType().getName(),
	            getVerboseInd(xContext));
	else 
	    logInfo(xContext, "Type       : null", getVerboseInd(xContext));
	if (getSeverity() != null)
	    logInfo(xContext, "Severity   : " + getSeverity().getName(), 
	            getVerboseInd(xContext));
	else 
	    logInfo(xContext, "Severity   : null", getVerboseInd(xContext));
	logInfo(xContext, "Customer   : " + getImpactedCustomer(), getVerboseInd(xContext));
	logInfo(xContext, "DB Mode    : " + dbMode, getVerboseInd(xContext));
	logInfo(xContext, "Verbose    : " + getVerboseInd(xContext), 
	        getVerboseInd(xContext));
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Creates a new ChangeRequest record based on an existing CQ\n");
	usage.append("record.  This app is used to create entries for reserved\n");
	usage.append("change requests and injection requests.\n");
	usage.append("\n");
	usage.append("For reserved CRs the component (and tool kit) must be NULL.\n");
	usage.append("\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " [-t toolkit] <-c component> \n");
	usage.append("                <-d description | -f description_file> \n");
	usage.append("                <-feature | -defect> <-sev severity> \n");
	usage.append("                [-u user] [-cq ChangeRequest id]\n");
	usage.append("                [-cust customer]\n");
	usage.append("                [-y] [-h] [-db dbMode] \n");
	usage.append("\n");
	usage.append("  toolkit          = ClearQuest ToolKit name (NULL, 14.1.0 (dev) ...).\n");
	usage.append("                     [If no tool kit then create as reserved CR]\n");
	usage.append("  component        = Component name (ess, pds, model, einstimer ...).\n");
	usage.append("  description      = Customer facing description (release note) of this\n");
	usage.append("                     change (3000 chars max)\n");
	usage.append("  description_file = Full path to file containing customer facing description\n");
	usage.append("                     (releaes note) of this change (3000 chars max)\n");
	usage.append("  -defect          = Specify if this ChangeRequest is a defect.\n");
	usage.append("  -feature         = Specify if this ChangeRequest is a feature.\n");
	usage.append("  severity         = Defect/feature impact/priority (1, 2, 3, 4)\n");
	usage.append("                     where 1 means \"extreme impact/resolve immediately\" down to\n");
	usage.append("                           4 means \"very little impact/work into future release\"\n");
	usage.append("  ChangeRequest    = ClearQuest id (MDCMS######### ...) - used by admins only.\n");
	usage.append("  user             = developers intranet id {defaults to user}\n");
	usage.append("  customer         = Customer impacted by this change request\n");
	usage.append("  -y               = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode           = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h               = Help (shows this information)\n");
	usage.append("\n");
	usage.append("Return Codes\n");
	usage.append("------------\n");
	usage.append(" 0 = ok\n");
	usage.append(" 1 = error\n");
	usage.append("\n");
	usage.append("Notes\n");
	usage.append("------\n");
	usage.append(" If toolkit and component are \"null\" a reserved CR \n");
	usage.append(" will be created. Reserved CRs are not usable by EDA developers.\n");
	usage.append("\n");
	usage.append("Examples\n");
	usage.append("------------\n");
	usage.append(" To create a Change Request from a CQ inject request\n");
	usage.append("  --> changeReqCreate -t \"14.1.0 (dev)\" -c ess -cq MDCMS00000000 -defect -sev 1 -d \"description of change here\" \n");
	usage.append(" To create RESERVED Change Request\n");
	usage.append("  --> changeReqCreate -t NULL -c NULL -cq MDCMS00000000 -defect -sev 4 -d \"Reserved\"\n");

	System.out.println(usage);

    }


    /**
     * Members.
     */
    private String descriptionText;
    private String descriptionFile;
    private String clearQuest;
    private String impactedCustomer; 
    private ChangeRequestType type;
    private ChangeRequestSeverity severity;
    private ChangeRequestStatus status;
    private Component_Version_Db compVersion;
    private boolean reservedFlag = false;


    /**
     * Getters.
     */
    public Component getComponentDb()  { return component; }
    public String getDescriptionText()  { return descriptionText; }
    public String getDescriptionFile()  { return descriptionFile; }
    public String getClearQuest()  { return clearQuest; }
    public String getImpactedCustomer()  { return impactedCustomer; }
    public ChangeRequestStatus getStatus()  { return status; }
    public ChangeRequestType getType()  { return type; }
    public ChangeRequestSeverity getSeverity()  { return severity; }
    public Component_Version_Db getCompVersion() { return compVersion; }
    public static boolean getRequestHelp() { return requestHelp; }
    public boolean isReserved() { return reservedFlag; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


    /**
     * Setters.
     */
    private void setDescriptionText(String aDesc) { descriptionText = aDesc; }
    private void setDescriptionFile(String aFile) { descriptionFile = aFile; }
    private void setClearQuest(String aRecord) { clearQuest = aRecord; }
    private void setStatus(ChangeRequestStatus aStatus) { status = aStatus; }
    private void setType(ChangeRequestType aType) { type = aType; }
    private void setSeverity(ChangeRequestSeverity aSev) { severity = aSev; }
    private void setReserved(boolean aFlag) { reservedFlag = aFlag;  }
    private void setImpactedCustomer(String aCust)  { impactedCustomer = aCust; }


    /**
     * Set the Component_Db object from the component name
     * @param xContext  Application context.
     * @param aName     Component name like ess, edautils, pds ...
     * @throws IcofException 
     */
    protected void setComponent(EdaContext xContext, String aName) 
    throws IcofException { 
	if (aName.toUpperCase().equals("NULL")) {
	    setReserved(true);
	    return;
	}
	if (getComponentDb() == null) {
	    component = new Component(xContext, aName.trim());
	    component.dbLookupByName(xContext);
	}    
	logInfo(xContext, "Component: " + getComponentDb().toString(xContext), 
	        getVerboseInd(xContext));
    }


    /**
     * Set the ChangeRequestStatus_Db object from the status name
     * @param xContext  Application context.
     * @throws IcofException 
     */
    private void setStatus(EdaContext xContext) 
    throws IcofException { 

	// Set the Status Name - if no ToolKit and Component then use RESERVED.
	String aName;
	if (isReserved()) {
	    aName = ChangeRequestStatus_Db.STATUS_RESERVED;
	}
	else {
	    String stage = getToolKit().getToolKit().getStageName().getName();
	    logInfo(xContext, "Stage Name: " + stage, getVerboseInd(xContext));

	    // Set status to reviewed for shipb injects
	    if (stage.equals(StageName_Db.STAGE_PREVIEW)) {
		aName = ChangeRequestStatus_Db.STATUS_REVIEWED;
	    }
	    else {
		aName = ChangeRequestStatus_Db.STATUS_SUBMITTED;
	    }
	}

	status = new ChangeRequestStatus(xContext, aName.trim().toUpperCase());
	status.dbLookupByName(xContext);
	logInfo(xContext, "Status: " + getStatus().toString(xContext), 
	        getVerboseInd(xContext));

    }


    /**
     * Set the ChangeRequestType_Db object from the type name
     * 
     * @param xContext  Application context.
     * @param aName     Type name.
     * @throws IcofException 
     */
    private void setType(EdaContext xContext, String aName) 
    throws IcofException { 

	if (getType() == null) {
	    type = new ChangeRequestType(xContext, aName.trim().toUpperCase());
	    type.dbLookupByName(xContext);
	}    
	logInfo(xContext, "Type: " + getType().toString(xContext), 
	        getVerboseInd(xContext));

    }


    /**
     * Set the ChangeRequestSeverity_Db object from the severity name
     * 
     * @param xContext  Application context.
     * @param aName     Severity name.
     * @throws IcofException 
     */
    private void setSeverity(EdaContext xContext, String aName) 
    throws IcofException { 

	if (getSeverity() == null) {
	    severity = new ChangeRequestSeverity(xContext, aName.trim().toUpperCase());
	    severity.dbLookupByName(xContext);
	}    
	logInfo(xContext, "Severity: " + getSeverity().toString(xContext), 
	        getVerboseInd(xContext));

    }

    /**
     * Reset the user from the caller to the developer
     * 
     * @param xContext  Application context
     * @param sIntranet  The developer's intranet id
     * @throws IcofException 
     */
    private void setDeveloper(EdaContext xContext, String sIntranet)
    throws IcofException {


	// For now just set the user object's intranet id.  Once the 
	// intranet ids are correct then we can lookup the User object
	user = null;
	user = new User_Db(sIntranet, true);
	//user.dbLookupByIntranet(xContext);

    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

}
