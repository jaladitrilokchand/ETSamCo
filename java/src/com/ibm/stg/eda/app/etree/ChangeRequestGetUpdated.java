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
 * Returns ChangeRequest updated between startdate and enddata.  
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 04/20/2011 GFS  Initial coding.
 * 06/09/2011 GFS  Disabled logging. Updated to lookup database objects on the fly. 
 * 06/29/2011 GFS  Added updated_by to XML file.
 * 09/07/2011 GFS  Added support for Change Request type and severity.
 * 09/09/2011 GFS  Updated so help is shown if no parm specified.
 * 11/17/2011 GFS  Updated to add <problem_introduction> and 
 *                 <communication_method> records to XML file.
 * 11/08/2012 GFS  Added CQ release name to XML file.
 * 11/30/2012 GFS  Added Impacted Customer to XML file.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.io.File;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Iterator;
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
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.StageName_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestGetUpdated extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.getUpdated";
    public static final String APP_VERSION = "v1.1";


    /**
     * Constructor
     *
     * @param     aContext    Application context
     * @param     aFileName   Full path to output file
     * @param     aStartTms   Starting timestamp
     * @param     aEndTms     Ending timestamp
     * @param     bDetails    If true show ChangeRequest details otherwise list
     *                        ChangeRequest names only.
     */
    public ChangeRequestGetUpdated(EdaContext xContext,
                                   String aFileName, Timestamp aStartTms,
                                   Timestamp aEndTms, boolean bDetails)
                                   throws IcofException {

	super(xContext, APP_NAME, APP_VERSION);

	setFileName(aFileName);
	setStartTimestamp(aStartTms);
	setEndTimestamp(aEndTms);
	setShowDetails(bDetails);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ChangeRequestGetUpdated(EdaContext aContext) throws IcofException {

	this(aContext, null, null, null, false);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new ChangeRequestGetUpdated(null);
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
	setChangeRequests(xContext);
	logInfo(xContext, "Found " + requests.size() + " Change Requests",
	        getVerboseInd(xContext));

	// Create the XML object and write it to the output file
	if (showDetails()) {
	    setXmlDetails(xContext);
	}
	else {
	    setXml(xContext);
	}
	writeXml(xContext);

	// Set the return code
	setReturnCode(xContext, SUCCESS);	
	rollBackDB(xContext, APP_NAME);

    }


    /**
     * Read the change request updated between start and end.
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void setChangeRequests(EdaContext xContext) throws IcofException {

	ChangeRequest_Db request = new ChangeRequest_Db(null);
	requests = request.dbLookupUpdated(xContext, getStartTimestamp(), 
	                                   getEndTimestamp()); 

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

	    /* For TESTING un-comment these 3 lines */
	    //StreamResult result =  new StreamResult(System.out);
	    //System.out.println("---------");
	    transformer.transform(source, result);
	    //System.out.println("---------");

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

	// If no requests then don't generate XML.
	if (getRequests() == null) {
	    return;
	}

	// Generate the XML object.
	try {

	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

	    // Add the ChangeRequest element.
	    document = docBuilder.newDocument();
	    Element rootElement = getDocument().createElement("ChangeRequest");
	    getDocument().appendChild(rootElement);

	    // Process each change request.
	    Iterator<String>  iter = getRequests().values().iterator();
	    while (iter.hasNext()) {
		String request =  iter.next();

		// Add to XML object
		Element record = getDocument().createElement("record");
		record.setAttribute("clearquest_id", request);
		rootElement.appendChild(record);    			

	    }

	}
	catch(Exception trap) {
	    throw new IcofException(APP_NAME, "setXml()", IcofException.SEVERE,
	                            "Unable to create XML object from database object.\n",
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
    private void setXmlDetails(EdaContext xContext) throws IcofException {

	// If no requests then don't generate XML.
	if (getRequests() == null) {
	    logInfo(xContext, "Skipping ... no CRs", getVerboseInd(xContext));
	    return;
	}

	// Generate the XML object.
	try {

	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

	    // Add the ChangeRequest element.
	    document = docBuilder.newDocument();
	    Element rootElement = getDocument().createElement("ChangeRequest");
	    getDocument().appendChild(rootElement);

	    // Process each change request.
	    Iterator<String> iter = getRequests().values().iterator();
	    while (iter.hasNext()) {
		String request =  iter.next();
		logInfo(xContext, "Request: " + request, getVerboseInd(xContext));

		// Lookup details for this ChangeRequest
		logInfo(xContext, "Looking up details ...",
		        getVerboseInd(xContext));
		ChangeRequest cr = new ChangeRequest(xContext, request);
		cr.dbLookupByCq(xContext);
		cr.setComponent(xContext);
		logInfo(xContext, " Found component ...", getVerboseInd(xContext));
		cr.setCodeUpdates(xContext);
		logInfo(xContext, " Found commits ...", getVerboseInd(xContext));

		// Add to XML object
		Element record = getDocument().createElement("record");
		record.setAttribute("clearquest_id", request);
		rootElement.appendChild(record);    			

		// Component
		logInfo(xContext, " Adding component ...", getVerboseInd(xContext));
		Element component = getDocument().createElement("component");
		record.appendChild(component);
		cr.getComponent().getComponent().dbLookupById(xContext);
		component.setAttribute("name", cr.getComponent().getComponent().getName());

		// Component->codeupdate_count 
		logInfo(xContext, " Adding CU count ...", getVerboseInd(xContext));
		Element updateCount = getDocument().createElement("codeupdate_count");
		int count = cr.countCodeUpdatesSince(xContext, null);
		updateCount.appendChild(getDocument().createTextNode(String.valueOf(count)));
		component.appendChild(updateCount);

		// Component->codeupdate_count_since
		logInfo(xContext, " Adding CU count since ...", getVerboseInd(xContext));
		Element updateCountSince = getDocument().createElement("codeupdate_count_since");
		count = cr.countCodeUpdatesSince(xContext, getStartTimestamp());
		updateCountSince.appendChild(getDocument().createTextNode(String.valueOf(count)));
		component.appendChild(updateCountSince);

		// Release  
		logInfo(xContext, " Adding release ...", getVerboseInd(xContext));
		cr.setCompVersions(xContext);
		if (! cr.getCompVersions().firstElement().isLoaded())
		    cr.getCompVersions().firstElement().dbLookupById(xContext);
		if (! cr.getCompVersions().firstElement().getVersion().isLoaded())
		    cr.getCompVersions().firstElement().getVersion().dbLookupById(xContext);
		if (! cr.getCompVersions().firstElement().getVersion().getRelease().isLoaded())
		    cr.getCompVersions().firstElement().getVersion().getRelease().dbLookupById(xContext);
		Element release = getDocument().createElement("release");
		release.setAttribute("alt_releasename", 
		                     cr.getCompVersions().firstElement().getVersion().getAltDisplayName());
		release.setAttribute("releasename", 
		                     cr.getCompVersions().firstElement().getVersion().getDisplayName());
		release.setAttribute("cq_releasename", 
		                     cr.getCompVersions().firstElement().getVersion().getCqReleaseName());
		record.appendChild(release);

		// Customer Impacted
		logInfo(xContext, " Adding Impacted Customer ...", getVerboseInd(xContext));
		Element impactedCust = getDocument().createElement("impacted_customer");
		String cust = "";
		if (cr.getImpactedCustomer() != null)
		    cust = cr.getImpactedCustomer();
		impactedCust.appendChild(getDocument().createTextNode(cust));
		record.appendChild(impactedCust);   	  

		// Updated by  
		logInfo(xContext, " Adding updated_by ...", getVerboseInd(xContext));
		Element updatedBy = getDocument().createElement("updated_by");
		String updateBy = cr.getChangeRequest().getUpdatedBy();
		updatedBy.appendChild(getDocument().createTextNode(updateBy));
		record.appendChild(updatedBy);   	  

		// Update timestamp  
		logInfo(xContext, " Adding updated_tms ...", getVerboseInd(xContext));
		Element updated = getDocument().createElement("updated_timestamp");
		String updateTms = cr.getChangeRequest().getUpdatedOn().toString();
		updated.appendChild(getDocument().createTextNode(updateTms));
		record.appendChild(updated);   	  

		// Description  
		logInfo(xContext, " Adding description ...", getVerboseInd(xContext));
		Element description = getDocument().createElement("description");
		description.appendChild(getDocument().createTextNode(cr.getDescription()));
		record.appendChild(description);  	  

		// State  
		logInfo(xContext, " Adding state ...", getVerboseInd(xContext));
		Element state = getDocument().createElement("state");
		cr.getStatus().getStatus().dbLookupById(xContext);
		state.appendChild(getDocument().createTextNode(cr.getStatus().getStatus().getName()));
		record.appendChild(state);  	  

		// Type (defect/feature)  
		logInfo(xContext, " Adding type ...", getVerboseInd(xContext));
		Element type = getDocument().createElement("type");
		cr.getType().getDbObject().dbLookupById(xContext);
		type.appendChild(getDocument().createTextNode(cr.getType().getDbObject().getName()));
		record.appendChild(type);  	  

		// Severity  
		logInfo(xContext, " Adding severity ...", getVerboseInd(xContext));
		Element severity = getDocument().createElement("severity");
		cr.getSeverity().getDbObject().dbLookupById(xContext);
		severity.appendChild(getDocument().createTextNode(cr.getSeverity().getDbObject().getName()));
		record.appendChild(severity);  	  

		// If this CR is for the Tool Kit in preview (shipb) then add
		// problem introduction and communication method fields.
		logInfo(xContext, " Adding misc ...", getVerboseInd(xContext));
		cr.setStageName(xContext);
		if (! cr.getStageName().isLoaded())
		    cr.getStageName().dbLookupById(xContext);
		if (cr.getStageName().getName().equals(StageName_Db.STAGE_PREVIEW)) {

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

	}
	catch(Exception trap) {
	    throw new IcofException(APP_NAME, "setXmlDetails()", IcofException.SEVERE,
	                            "Unable to create XML object from database object.\n",
	                            trap.getMessage());
	}

    }


    protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
	singleSwitches.add("-y");
	singleSwitches.add("-h");
	singleSwitches.add("-detail");
	argSwitches.add("-db");
	argSwitches.add("-output");
	argSwitches.add("-startdate");
	argSwitches.add("-enddate");
    }


    protected String readParams(Hashtable<String,String> params, String errors,EdaContext xContext) {
	// Read the XML file
	if (params.containsKey("-output")) {
	    setFileName((String) params.get("-output"));
	}
	else {
	    errors += "Output file (-output) is a required parameter\n";
	}

	// Read the Start Timestamp
	if (params.containsKey("-startdate")) {
	    setStartTimestamp((String) params.get("-startdate"));
	}
	else {
	    errors += "Start date (-startdate) is a required parameter\n";
	}

	// Read the End Timestamp
	if (params.containsKey("-enddate")) {
	    setEndTimestamp((String) params.get("-enddate"));
	}
	else {
	    errors += "End date (-enddate) is a required parameter\n";
	}

	// See if detail  was requested
	setShowDetails(false);
	if (params.containsKey("-detail")) {
	    setShowDetails(true);
	}
	return errors;
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, verboseInd);
	logInfo(xContext, "XML file   : " + getFileName(), verboseInd);
	logInfo(xContext, "Start Tms  : " + getStartTimestamp(), verboseInd);
	logInfo(xContext, "End Tms    : " + getEndTimestamp(), verboseInd);
	logInfo(xContext, "Detail     : " + showDetails(), verboseInd);
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
	usage.append("Writes data for ChangeRequests updated between startdate  \n");
	usage.append("and enddate into the XML file. \n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-startdate start_tms> <-enddate start_tms> \n");
	usage.append("              <-output file> [-detail] [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  start_tms   = Timestamp (like 2011-02-17 10:14:26)\n");
	usage.append("  end_tms     = Timestamp (like 2011-02-17 10:14:26)\n");
	usage.append("  file        = Full path to XML file\n");
	usage.append("  -detail     = If true show CR details otherwise CR # only\n");
	usage.append("  -y          = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode      = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h          = Help (shows this information)\n");
	usage.append("\n");
	usage.append("Return Codes\n");
	usage.append("------------\n");
	usage.append(" 0 = ok\n");
	usage.append(" 1 = error\n");
	usage.append("\n");
	usage.append("Example \n");
	usage.append("--------\n");
	usage.append("To see Change Requests updated between 1/1/2011 and 6/1/2011 ...\n");
	usage.append(" cr.getUpdated -startdate '2011-01-01 00:00:00' -enddate '2011-06-01 00:00:00' -output test2.xml\n");
	usage.append("\n");

	System.out.println(usage);

    }


    /**
     * Members.
     */
    private String fileName;
    private Document document;
    private Timestamp startTms;
    private Timestamp endTms;
    private boolean showDetails = false;
    private Hashtable<String,String> requests;

    /**
     * Getters.
     */
    public String getFileName()  { return fileName; }
    public Timestamp getStartTimestamp() { return startTms; }
    public Timestamp getEndTimestamp() { return endTms; }
    public Document getDocument() { return document; }
    public boolean showDetails() { return showDetails; }
    public Hashtable<String,String> getRequests() { return requests; }
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
    private void setEndTimestamp(Timestamp aTms) { endTms = aTms; }
    private void setEndTimestamp(String aTms) {
	if (aTms.indexOf(".") < 0)
	    aTms += ".0";
	endTms = Timestamp.valueOf(aTms); }
    private void setShowDetails(boolean aFlag) { showDetails = aFlag; }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

}
