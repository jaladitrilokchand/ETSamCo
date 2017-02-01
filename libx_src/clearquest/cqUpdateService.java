/**
 * <pre>
 * =============================================================================
 *
 *  IBM Internal Use Only
 *
 * =============================================================================
 *
 *  CREATOR: Aydin Suren
 *     DEPT: AW0V
 *     
 *-PURPOSE----------------------------------------------------------------------------------------
 * Service class definition.
 *------------------------------------------------------------------------------------------------------
 *
 * =============================================================================
 *
 * -CHANGE LOG------------------------------------------------------------------
 * 06/15/2009 AS  Initial coding.
 *
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.clearquest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Vector;

import javax.wvcm.PropertyNameList.PropertyName;
import javax.wvcm.PropertyRequestItem;
import javax.wvcm.PropertyRequestItem.PropertyRequest;

import com.ibm.rational.wvcm.stp.StpLocation;
import com.ibm.rational.wvcm.stp.StpProperty;
import com.ibm.rational.wvcm.stp.StpResource;
// used with obsolete getCqLocation method
//import com.ibm.rational.wvcm.stp.StpLocation.Namespace;
//import com.ibm.rational.wvcm.stp.StpProvider.Domain;
import com.ibm.rational.wvcm.stp.cq.CqAction;
import com.ibm.rational.wvcm.stp.cq.CqAttachment;
import com.ibm.rational.wvcm.stp.cq.CqAttachmentFolder;
import com.ibm.rational.wvcm.stp.cq.CqFieldValue;
import com.ibm.rational.wvcm.stp.cq.CqProvider;
import com.ibm.rational.wvcm.stp.cq.CqRecord;
import com.ibm.rational.wvcm.stp.cq.CqRecordType;

public class cqUpdateService extends cqService {

    // Debugging the service in dev mode, turn it to true.
    private static final boolean debug = false;

    /** Generally useful field meta-properties. **/
    private static final PropertyRequestItem[] FIELD_METAPROPERTIES = 
    	new PropertyRequestItem[] { CqFieldValue.VALUE, CqFieldValue.NAME,
		    CqFieldValue.TYPE, CqFieldValue.REQUIREDNESS };

    /** Property request for retrieving the user-friendly name of a resource */
    private static final PropertyRequestItem[] RECORD_REQUEST = 
	    new PropertyRequestItem[] {
		    StpResource.USER_FRIENDLY_LOCATION,
		    CqRecord.ALL_FIELD_VALUES
			    .nest(new PropertyRequestItem[] { StpProperty.VALUE
				    .nest(FIELD_METAPROPERTIES) }),
		    CqRecord.STATE_NAME, CqRecord.DEFAULT_ACTION,
		    CqRecord.LEGAL_ACTIONS, CqRecord.RECORD_CLASS, };

    /**
     * Properties to be requested from each record field value, including
     * specific additional information for attachments
     */
    private static final PropertyRequestItem[] VALUE_PROPERTIES = new PropertyRequestItem[] {
    		    StpResource.USER_FRIENDLY_LOCATION,
    		    CqAttachmentFolder.ATTACHMENT_LIST.nest(new PropertyName[] {
    			    CqAttachment.DISPLAY_NAME, CqAttachment.FILE_NAME,
    			    CqAttachment.FILE_SIZE, CqAttachment.DESCRIPTION }) };

    /** The record properties read prior to editing the record */
    private static final PropertyRequestItem[] RECORD_PROPERTIES = new PropertyRequestItem[] {
		    CqRecord.USER_FRIENDLY_LOCATION,
		    CqRecord.STABLE_LOCATION,
		    CqRecord.LEGAL_ACTIONS.nest(new PropertyRequestItem[] {
		    	CqAction.USER_FRIENDLY_LOCATION,
		    	CqAction.STABLE_LOCATION,		    	
			    CqAction.DISPLAY_NAME, CqAction.TYPE }),
		    CqRecord.ALL_FIELD_VALUES
			    .nest(new PropertyRequestItem[] { StpProperty.VALUE
				    .nest(new PropertyRequestItem[] {
					    CqFieldValue.NAME,
					    CqFieldValue.REQUIREDNESS,
					    CqFieldValue.TYPE,
					    CqFieldValue.VALUE
						    .nest(VALUE_PROPERTIES) }) }) };

    /**
     * Prepares a StpLocation team API object based on the action type.
     * 
     * @param provider
     *            CqProvider team API object.
     * @param Namespace
     *            The namespace to use in the CQ Team API location
     * @param sSchema
     *            CQ schema that user is trying to connect.
     * @param sDatabase
     *            CQ database that user is trying to connect.
     * @param sRecordType
     *            CQ Record type
     * @param sAction
     *            CQ action
     * @return
     * @throws Exception
     */
    /* Replaced with new 7.1 API methodology.  Keep it around, however, 
     * in case of problems.  This builds a known location more manually.
    private StpLocation getCqLocation(CqProvider provider,
	    Namespace stpNamespace, String sSchema, String sDatabase,
	    String sRecordType, String sAction) throws Exception {

	StpLocation stpLocation = provider.stpLocation(sRecordType + "/"
		+ sAction);
	stpLocation = stpLocation.recomposeWithDomain(Domain.CLEAR_QUEST);
	if (stpLocation.getNamespace() == Namespace.NONE) {
	    stpLocation = stpLocation.recomposeWithNamespace(stpNamespace);
	}
	stpLocation = stpLocation.recomposeWithRepo(sSchema + "/" + sDatabase);

	return stpLocation;
    }
     */
    
    /**
     * Helper main method for unit testing from command line. This can only
     * tested in the server where CQ client installed and configured.
     * 
     * @param args
     */
    public static void main(String[] args) {

	try {

	    StringBuffer sbXMLRecord = cqUtil
		    .getFileContent(cqUpdateService.class
			    .getResource(cqUtil.sUPDATE_XML));

	    cqUpdateService xCqUpdateService = new cqUpdateService();
	    String sResult = xCqUpdateService.update(sbXMLRecord);
	    System.out.println(sResult);

	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	}
    }

    /**
     * This is the method that talks to ClearQuest via Rational Team API to
     * update a record.
     * 
     * @param sbXMLRecord
     *            An XML input that has all the necessary information to update
     *            a ClearQuest issue. For an example of input XML, please see
     *            cqUpdateExample1.xml file and its corresponding XSD schema
     *            file.
     * @return XML formatted result of the action. Any errors/exception will be
     *         inside of the returned XML formatted string.
     */
    /**
     * @param sbXMLInput
     * @return
     */
    public String update(StringBuffer sbXMLInput) {

	String sError = null;
	String sMessage = null;
	CqProvider provider = null;
	CqAction tmpAction = null;
	CqAction action = null;
	PropertyRequest feedback = null;
	CqRecordType teamRecordType = null;
	StpLocation srcStpLocation = null;
	CqRecord teamCqRecord = null;
	Iterator itActions = null;
	Vector vcLegalAction = null;
	Vector vcField = null;
	cqField xCqField = null;
	
	try {
	    // Process the data in XML.
	    cqUpdateInput xCqUpdateInput = new cqUpdateInput(sbXMLInput);

	    String sUser = xCqUpdateInput.getCqUser().getUsername();
	    String sPassword = xCqUpdateInput.getCqUser().getPassword();
	    String sDatabase = xCqUpdateInput.getCqUser().getDatabase();
	    String sSchema = xCqUpdateInput.getCqUser().getSchema();
	    String sClient = xCqUpdateInput.getCqClient().getClientName();
	    String sRecordType = xCqUpdateInput.getCqUpdate().getRecordType();
	    String sRecordID = xCqUpdateInput.getCqUpdate().getRecordID();
	    String sAction = xCqUpdateInput.getCqUpdate().getAction();
	    
	    if (sUser == null) {
	    	throw new Exception("User is null.");
	    }
	    if (sPassword == null) {
	    	throw new Exception("Password is null.");
	    }
	    if (sDatabase == null) {
	    	throw new Exception("Database is null.");
	    }
	    if (sSchema == null) {
	    	throw new Exception("Schema is null.");
	    }
	    if (sClient == null) {
	    	throw new Exception("Client is null.");
	    }
	    if (sRecordType == null) {
	    	throw new Exception("Record type is null.");
	    }
	    if (sRecordID == null) {
	    	throw new Exception("Record ID is null.");
	    }
	    if (sAction == null) {
	    	throw new Exception("Action is null.");
	    }
	    
	    if (debug) {
		System.out
			.println("sUser=" + sUser + " - sDatabase=" + sDatabase
				+ " - sSchema=" + sSchema + " - sClient="
				+ sClient + " - sRecordType=" + sRecordType
				+ " - sRecordID=" + sRecordID + " - sAction="
				+ sAction);
	    }

	    // Construct the service name and validate.
	    String sService = cqUtil.UPDATE + "-" + sRecordType;
	    cqUtil.validateService(sService);

	    // Initialize the measurements collection.
	    cqUsageMeasurement xCqUsageMeasurement = new cqUsageMeasurement(
		    xCqUpdateInput.getCqUser(), xCqUpdateInput.getCqClient(),
		    sService, true);

	    // Check authorization for the service.
	    cqClientAuthorization xCqClientAuthorization = new cqClientAuthorization(
		    xCqUpdateInput.getCqUser(), xCqUpdateInput.getCqClient(),
		    sService);
	    xCqClientAuthorization.checkAuthorization();

	    // Initialize the CqRecord object.
	    provider = getProvider(sUser, sPassword);
	    	    
	    // Get the requested CqRecordType from the requested database proxy.
	    teamRecordType = getCqRecordType(provider, sSchema, sDatabase, sRecordType);
	    
	    // get the proxy CqRecord
	    srcStpLocation = (StpLocation) teamRecordType.getStableLocation();
	    teamCqRecord = provider.cqRecord((StpLocation) srcStpLocation
	    		.child(sRecordID));

	    // Determine the legal actions
	    teamCqRecord = (CqRecord) teamCqRecord
		    .doReadProperties(new PropertyRequest(RECORD_PROPERTIES));
	    itActions = teamCqRecord.getLegalActions().iterator();
	    vcLegalAction = new Vector();
	    while (itActions.hasNext()) {
	    	tmpAction = (CqAction) itActions.next();
	    	String sActionName = tmpAction.getDisplayName();
	    	vcLegalAction.add(sActionName);
	    	// this captures "action" as the CqAction object we want.
	    	if (sAction.equals(sActionName)) {
	    		action = tmpAction;
	    	}
	    	if (debug) {
	    		System.out.println("action.getDisplayName()="
	    				+ action.getDisplayName() + " --- "
	    				+ action.toString());
	    	}
	    }

	    // Check to see if the action is a legal action.
	    if (!vcLegalAction.contains(sAction)) {
	    	sError = "\"" + sAction + "\" is not a legal action for "
				+ sRecordID;
	    	sError += ". The valid actions are: "
	    		+ vcLegalAction.toString();
	    	throw new Exception(sError);
	    }

	    if (sRecordType.equals("dip_revision")) {
	    	// First field indicates webservice, needed by dip_revision types 
	    	// to turn off read-only, which only happens for webservices
	    	teamCqRecord.setField("process_type", "webservice");
	    }
	    
	    // Set the new field values if given.
	    vcField = xCqUpdateInput.getCqUpdate().getFields();
	    if (vcField != null && vcField.size() > 0) {
	    	for (int i = 0; i < vcField.size(); i++) {
	    		xCqField = (cqField) vcField.get(i);
	    		teamCqRecord.setField(xCqField.getName(), xCqField.getValue());
	    	}
	    }

	    // Print the fields to be set in debug.
	    if (debug) {
	    	PropertyName[] updated = teamCqRecord.updatedPropertyNameList()
	    		.getPropertyNames();

	    	for (int i = 0; i < updated.length; ++i) {
	    		System.out.println(updated[i].getName() + " => "
	    				+ teamCqRecord.getProperty(updated[i]));
	    	}
	    }

	    // Set target location for taking the action.
	    /* Needed only if using getCqLocation manual method.
	    StpLocation targetStpLocation = getCqLocation(provider,
		    Namespace.ACTION, sSchema, sDatabase, sRecordType, sAction);

	    if (targetStpLocation.getNameSegmentCount() == 1) {
	    	targetStpLocation = (StpLocation) targetStpLocation.parent()
	    		.child(srcStpLocation.parent().lastSegment()).child(
	    				targetStpLocation.lastSegment());
	    }
        */
	    
	    //action = provider.cqAction(targetStpLocation);
	    feedback = new PropertyRequest(RECORD_REQUEST);
	    // returned object is not being used
	    teamCqRecord = (CqRecord) teamCqRecord.setAction(action)
	    	.doWriteProperties(feedback, CqProvider.DELIVER_ALL);

	    // Prepare the return message to the client.
	    sMessage = action.location().lastSegment()
		    + " action performed for " + sRecordType + "/" + sRecordID;

	    // Update CQ database that the client just used "update"
	    // service.
	    cqSysAppUsage xCqSysAppUsage = new cqSysAppUsage(sService, sClient,
		    sUser);
	    xCqSysAppUsage.recordUsageToCQ(provider, sSchema, sDatabase);

	    // Finish the measurements collection.
	    xCqUsageMeasurement.record();

	} catch (Throwable t) {
	    StringWriter sw = new StringWriter();
	    t.printStackTrace(new PrintWriter(sw, true));
	    try {
		sError = cqUtil.filterXMLReservedChars(sw.toString());
	    } catch (Exception ignore) {
		sError = sw.toString();
	    }
	} finally {

	    // Release the resource.
	    if (provider != null) {
	    	try {
	    		provider.terminate();
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    }
	    provider = null;
		tmpAction = null;
		action = null;
		feedback = null;
		teamRecordType = null;
		srcStpLocation = null;
		teamCqRecord = null;
		itActions = null;
		vcLegalAction = null;
		vcField = null;
		xCqField = null;
	}
	return getCreateResponse(sMessage, sError).toString();
    }

    /**
     * Prepares the XML formatted return string.
     * 
     * @param sMessage
     *            Action message result
     * @param sError
     *            The error that needs to be written to return string if the
     *            operation/action was not successful, the error/exception
     * @return
     */
    private StringBuffer getCreateResponse(String sMessage, String sError) {

	StringBuffer sbReturnXML = new StringBuffer();
	sbReturnXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	sbReturnXML.append("<CqUpdate>\n");

	sbReturnXML.append("  <update>\n");
	if (cqUtil.isEmpty(sMessage)) {
	    sbReturnXML.append("    <message></message>\n");
	} else {
	    sbReturnXML.append("    <message>" + sMessage + "</message>\n");
	}
	sbReturnXML.append("  </update>\n");

	sbReturnXML.append("  <error>\n");
	sbReturnXML.append("    <description>" + sError + "</description>\n");
	sbReturnXML.append("  </error>\n");
	sbReturnXML.append("</CqUpdate>\n");

	return sbReturnXML;
    }

}
