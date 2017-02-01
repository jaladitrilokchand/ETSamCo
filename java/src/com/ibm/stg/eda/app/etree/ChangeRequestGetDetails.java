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
 * Returns data on a given ChangeRequest. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 04/20/2011 GFS  Initial coding.
 * 06/09/2011 GFS  Disabled logging and updated to lookup objects on the fly.
 * 09/07/2011 GFS  Added support for Change Request type and severity.
 * 09/09/2011 GFS  Updated so help is shown if no parm specified.
 * 11/17/2011 GFS  Updated to add <problem_introduction> and 
 *                 <communication_method> records to XML file.
 * 06/08/2012 GFS  Updated to generate details for reserved CRs.
 * 11/08/2012 GFS  Added CQ release name to XML file.
 * 11/30/2012 GFS  Added Impacted Customer to XML file.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.io.File;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.StageName_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestGetDetails extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.getByCQ";
    public static final String APP_VERSION = "v1.2";


    /**
     * Constructor
     *
     * @param     aContext    Application context
     * @param     aCqId       ChangeRequest's CQ number
     * @param     aStartTms   Starting timestamp (maybe null)
     * @param     aFileName   Full path to output file
     */
    public ChangeRequestGetDetails(EdaContext xContext, String aCqId,
                                   String aFileName, Timestamp aStartTms)	
                                   throws IcofException {

	super(xContext, APP_NAME, APP_VERSION);

	setChangeRequest(xContext, aCqId);
	setFileName(aFileName);
	setStartTimestamp(aStartTms);

    }


    /**
     * Constructor
     *
     * @param     aContext    Application context
     * @param     aCq         ChangeRequest object
     * @param     aFileName   Full path to output file
     * @param     aStartTms   Starting timestamp (maybe null)
     */
    public ChangeRequestGetDetails(EdaContext xContext, ChangeRequest aCq,
                                   String aFileName, Timestamp aStartTms)	
                                   throws IcofException {

	super(xContext, APP_NAME, APP_VERSION);

	setChangeRequest(aCq);
	setFileName(aFileName);
	setStartTimestamp(aStartTms);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ChangeRequestGetDetails(EdaContext aContext) throws IcofException {

	this(aContext, (ChangeRequest)null, null, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new ChangeRequestGetDetails(null);
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

	// Read the database for CodeUpdates and Component information
	if (getChangeRequest() != null) {
	    getChangeRequest().dbLookupByCq(xContext);
	    getChangeRequest().setComponent(xContext);
	    getChangeRequest().setCodeUpdates(xContext);

	    logInfo(xContext, 
	            "CR data\n-----------\n" + getChangeRequest().toString(xContext),
	            isVerbose(xContext));
	}
	else {
	    System.out.println("CR not found in DB ...");
	}

	// Create the XML object and write it to the output file
	setXml(xContext);
	writeXml(xContext);

	// Set the return code
	setReturnCode(xContext, SUCCESS);
	rollBackDB(xContext, APP_NAME);

    }


    /**
     * Write the XML to the output file
     * 
     * @param xContext Application context
     * @throws IcofException 
     */
    private void writeXml(EdaContext xContext) throws IcofException {

	try {

	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();

	    DOMSource source = new DOMSource(getDocument());

	    StreamResult result =  new StreamResult(new File(getFileName()));
	    transformer.transform(source, result);

	}
	catch(Exception trap) {
	    throw new IcofException(APP_NAME, "writeXml()", IcofException.SEVERE,
	                            "Unable to write XML file.\n",
	                            trap.getMessage());
	}

    }


    /**
     * Create the XML object from the database object
     * 
     * @param xContext  Application context
     * @throws IcofException 
     * @throws ParserConfigurationException 
     */
    private void setXml(EdaContext xContext) throws IcofException {

	try {

	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

	    // ChangeRequest element
	    document = docBuilder.newDocument();
	    Element rootElement = getDocument().createElement("ChangeRequest");
	    getDocument().appendChild(rootElement);

	    // Stop here if ChangeRequest not found - this will result in an 
	    // empty XML file. 
	    if (getChangeRequest() == null) {
		System.out.println("Output file is empty ...  no CR found");
		return;
	    }

	    if (getChangeRequest().getComponent() == null) {
		setReservedXml(xContext, rootElement);
		return;
	    }

	    // Record element
	    Element record = getDocument().createElement("record");
	    record.setAttribute("clearquest_id", getChangeRequest().getClearQuest());
	    rootElement.appendChild(record);

	    // Component
	    Element component = getDocument().createElement("component");
	    getChangeRequest().getComponent().getComponent().dbLookupById(xContext);
	    component.setAttribute("name", 
	                           getChangeRequest().getComponent().getComponent().getName());
	    record.appendChild(component);

	    // Component->codeupdate_count 
	    Element updateCount = getDocument().createElement("codeupdate_count");
	    int count = getChangeRequest().countCodeUpdatesSince(xContext, null);
	    updateCount.appendChild(getDocument().createTextNode(String.valueOf(count)));
	    component.appendChild(updateCount);

	    // Component->codeupdate_count_since 
	    Element updateCountSince = getDocument().createElement("codeupdate_count_since");
	    count = getChangeRequest().countCodeUpdatesSince(xContext, getStartTimestamp());
	    updateCountSince.appendChild(getDocument().createTextNode(String.valueOf(count)));
	    component.appendChild(updateCountSince);

	    // Release  
	    getChangeRequest().setCompVersions(xContext);
	    if (! getChangeRequest().getCompVersions().firstElement().isLoaded())
		getChangeRequest().getCompVersions().firstElement().dbLookupById(xContext);
	    if (! getChangeRequest().getCompVersions().firstElement().getVersion().isLoaded())
		getChangeRequest().getCompVersions().firstElement().getVersion().dbLookupById(xContext);
	    if (! getChangeRequest().getCompVersions().firstElement().getVersion().getRelease().isLoaded())
		getChangeRequest().getCompVersions().firstElement().getVersion().getRelease().dbLookupById(xContext);

	    Element release = getDocument().createElement("release");
	    release.setAttribute("alt_releasename", 
	                         getChangeRequest().getCompVersions().firstElement().getVersion().getAltDisplayName());
	    release.setAttribute("releasename", 
	                         getChangeRequest().getCompVersions().firstElement().getVersion().getDisplayName());
	    release.setAttribute("cq_releasename", 
	                         getChangeRequest().getCompVersions().firstElement().getVersion().getCqReleaseName());
	    record.appendChild(release);

	    // Customer Impacted
	    logInfo(xContext, " Adding Impacted Customer ...", getVerboseInd(xContext));
	    Element impactedCust = getDocument().createElement("impacted_customer");
	    String cust = "";
	    if (getChangeRequest().getImpactedCustomer() != null)
		cust = getChangeRequest().getImpactedCustomer();
	    impactedCust.appendChild(getDocument().createTextNode(cust));
	    record.appendChild(impactedCust);   

	    // Update timestamp  
	    Element updated = getDocument().createElement("updated_timestamp");
	    String updateTms = getChangeRequest().getChangeRequest().getUpdatedOn().toString();
	    updated.appendChild(getDocument().createTextNode(updateTms));
	    record.appendChild(updated);   	  

	    // Description  
	    Element description = getDocument().createElement("description");
	    description.appendChild(getDocument().createTextNode(getChangeRequest().getDescription()));
	    record.appendChild(description);  	  

	    // State  
	    Element state = getDocument().createElement("state");
	    getChangeRequest().getStatus().getStatus().dbLookupById(xContext);
	    state.appendChild(getDocument().createTextNode(getChangeRequest().getStatus().getStatus().getName()));
	    record.appendChild(state);  	  

	    // Type (defect/feature)  
	    Element type = getDocument().createElement("type");
	    getChangeRequest().getType().getDbObject().dbLookupById(xContext);
	    type.appendChild(getDocument().createTextNode(getChangeRequest().getType().getDbObject().getName()));
	    record.appendChild(type);  	  

	    // Severity  
	    Element severity = getDocument().createElement("severity");
	    getChangeRequest().getSeverity().getDbObject().dbLookupById(xContext);
	    severity.appendChild(getDocument().createTextNode(getChangeRequest().getSeverity().getDbObject().getName()));
	    record.appendChild(severity);  	  

	    // If this CR is for the Tool Kit in preview (shipb) then add
	    // problem introduction and communication method fields.
	    getChangeRequest().setStageName(xContext);
	    if (! getChangeRequest().getStageName().isLoaded())
		getChangeRequest().getStageName().dbLookupById(xContext);
	    String stage = getChangeRequest().getStageName().getName();

	    if (stage.equals(StageName_Db.STAGE_PREVIEW)) {

		// Problem introduction  
		Element probIntro = getDocument().createElement("problem_introduction");
		probIntro.appendChild(getDocument().createTextNode("New functionality written < 6 months ago"));
		record.appendChild(probIntro);  	  

		// Communication method  
		Element commMethod = getDocument().createElement("communication_method");
		commMethod.appendChild(getDocument().createTextNode("Release notes"));
		record.appendChild(commMethod);

	    }

	}
	catch(Exception trap) {
	    throw new IcofException(APP_NAME, "setXml()", IcofException.SEVERE,
	                            "Unable to create XML object from database object.\n",
	                            trap.getMessage());
	}

    }


    /**
     * Create XML for reserved CRs
     * 
     * @param xContext     Application context
     * @param rootElement  XML root
     * @throws IcofException 
     */
    private void setReservedXml(EdaContext xContext, Element rootElement) 
    throws IcofException {

	//System.out.println("2b");

	// Record element
	Element record = getDocument().createElement("record");
	record.setAttribute("clearquest_id", getChangeRequest().getClearQuest());
	rootElement.appendChild(record);

	//System.out.println("3b");

	// Component
	Element component = getDocument().createElement("component");
	component.setAttribute("name", "NULL");
	record.appendChild(component);

	//System.out.println("4b");

	// Component->codeupdate_count 
	Element updateCount = getDocument().createElement("codeupdate_count");
	int count = 0;
	updateCount.appendChild(getDocument().createTextNode(String.valueOf(count)));
	component.appendChild(updateCount);

	//System.out.println("5b");

	// Component->codeupdate_count_since 
	Element updateCountSince = getDocument().createElement("codeupdate_count_since");
	count = 0;
	updateCountSince.appendChild(getDocument().createTextNode(String.valueOf(count)));
	component.appendChild(updateCountSince);

	//System.out.println("6b");

	// Release  

	//System.out.println("7b");

	Element release = getDocument().createElement("release");
	release.setAttribute("alt_releasename", "NULL");
	release.setAttribute("releasename", "NULL");
	release.setAttribute("cq_releasename", "NULL");
	record.appendChild(release);

	//System.out.println("8b");

	// Update timestamp  
	Element updated = getDocument().createElement("updated_timestamp");
	String updateTms = getChangeRequest().getChangeRequest().getUpdatedOn().toString();
	updated.appendChild(getDocument().createTextNode(updateTms));
	record.appendChild(updated);   	  

	//System.out.println("9b");

	// Description  
	Element description = getDocument().createElement("description");
	description.appendChild(getDocument().createTextNode(getChangeRequest().getDescription()));
	record.appendChild(description);  	  

	//System.out.println("10b");

	// State  
	Element state = getDocument().createElement("state");
	getChangeRequest().getStatus().getStatus().dbLookupById(xContext);
	state.appendChild(getDocument().createTextNode(getChangeRequest().getStatus().getStatus().getName()));
	record.appendChild(state);  	  

	//System.out.println("11b");

	// Type (defect/feature)  
	Element type = getDocument().createElement("type");
	getChangeRequest().getType().getDbObject().dbLookupById(xContext);
	type.appendChild(getDocument().createTextNode(getChangeRequest().getType().getDbObject().getName()));
	record.appendChild(type);  	  

	//System.out.println("12b");

	// Severity  
	Element severity = getDocument().createElement("severity");
	getChangeRequest().getSeverity().getDbObject().dbLookupById(xContext);
	severity.appendChild(getDocument().createTextNode(getChangeRequest().getSeverity().getDbObject().getName()));
	record.appendChild(severity);  	  

	//System.out.println("done .. b");

    }


    protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-cqid");
	argSwitches.add("-output");
	argSwitches.add("-startdate");
    }


    protected String readParams(Hashtable<String,String> params, String errors,
                                EdaContext xContext) throws IcofException {
	// Read the ClearQuest name
	if (params.containsKey("-cqid")) {
	    setChangeRequest(xContext,  params.get("-cqid"));
	}
	else {
	    errors += "ClearQuest (-cqid) is a required parameter\n";
	}

	// Read the XML file
	if (params.containsKey("-output")) {
	    setFileName( params.get("-output"));
	}
	else {
	    errors += "Output file (-output) is a required parameter\n";
	}

	// Read the Timestamp
	if (params.containsKey("-startdate")) {
	    setStartTimestamp( params.get("-startdate"));
	}
	return errors;
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, verboseInd);
	if (getChangeRequest() == null) {
	    logInfo(xContext, "ClearQuest : null", verboseInd);
	}
	else {
	    logInfo(xContext, "ClearQuest : " + getChangeRequest().getClearQuest(), verboseInd);
	}
	logInfo(xContext, "XML file   : " + getFileName(), verboseInd);
	if (getStartTimestamp() != null)
	    logInfo(xContext, "Timestamp  : " + getStartTimestamp(), verboseInd);
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
	usage.append("Writes data for the given ChangeRequest into the specified \n");
	usage.append("XML file. \n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-cqid ClearQuest> <-output file> [-startdate timestamp]\n");
	usage.append("                    [-y] [-h] -db dbMode]\n");
	usage.append("\n");
	usage.append("  ClearQuest  = ClearQuest id (MDCMS######### ...).\n");
	usage.append("  file        = Full path to XML file\n");
	usage.append("  timestamp   = Timestamp (like 2011-02-17 10:14:26)\n");
	usage.append("  -y          = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode      = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h          = Help (shows this information)\n");
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
    private String fileName;
    private Document document;
    private Timestamp startTms;


    /**
     * Getters.
     */
    public String getFileName()  { return fileName; }

    public Timestamp getStartTimestamp() { return startTms; }
    public Document getDocument() { return document; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


    /**
     * Setters.
     */

    private void setFileName(String aName) { fileName = aName; }
    private void setStartTimestamp(Timestamp aTms) { startTms = aTms; }
    private void setStartTimestamp(String aTms) {
	if (aTms.indexOf(".") < 0)
	    aTms += ".0";
	startTms = Timestamp.valueOf(aTms); }

    /**
     * Set the ChangeRequest object from the CQ name
     * @param xContext  Application context.
     * @param aName     ChangeRequest name
     * @throws IcofException 
     */
    protected void setChangeRequest(EdaContext xContext, String aName) 
    throws IcofException {

	if (aName == null)
	    return;

	if (getChangeRequest() == null) {
	    try {
		changeRequest = new ChangeRequest(xContext, aName.trim().toUpperCase());
		changeRequest.dbLookupByCq(xContext);
	    }
	    catch(IcofException trap) {
		changeRequest = null;
	    }
	}    
	if (getChangeRequest() == null)
	    logInfo(xContext, "ChangeRequest: null", false);
	else
	    logInfo(xContext, "ChangeRequest: " + getChangeRequest().toString(xContext), false);

    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

}
