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
 *     DATE: 03/01/2008
 *     
 *-PURPOSE----------------------------------------------------------------------------------------
 * Service class definition.
 *------------------------------------------------------------------------------------------------------
 *
 * =============================================================================
 *
 * -CHANGE LOG------------------------------------------------------------------
 * 03/01/2008 AS  Initial coding.
 *
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.clearquest;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.wvcm.PropertyNameList;
import javax.wvcm.PropertyRequestItem;
import javax.wvcm.Feedback;

import com.ibm.rational.wvcm.stp.StpLocation;
import com.ibm.rational.wvcm.stp.cq.CqProvider;
import com.ibm.rational.wvcm.stp.cq.CqRecord;
import com.ibm.rational.wvcm.stp.cq.CqRecordType;

/**
 * @author asuren
 * 
 */
public class cqCreateService extends cqService {

    /**
     * For unit testing. Doesn't work for Tom, because Tom is not Aydin; his 
     * machine is not iipmdsdev.
     * 
     * @param args
     */
    public static void main(String[] args) {

	String sResult = null;
	cqCreateService xCreateService = null;

	try {

	    xCreateService = new cqCreateService();
	    sResult = xCreateService.create(cqUtil
		    .getFileContent(cqCreateService.class
			    .getResource(cqUtil.sCREATE_XML)));
	    System.out.println("\nResult:\n" + sResult);

	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    xCreateService = null;
	}

    }

    /**
     * This is the method that talks to ClearQuest via Rational Team API to
     * create a record.
     * 
     * @param sbXMLRecord
     *            An XML input that has all the necessary information to create
     *            a ClearQuest issue. For an example of input XML, please see
     *            cqCreateExample.xml file and its corresponding XSD schema
     *            file.
     * @return XML formatted result of the action. Any errors/exception will be
     *         inside of the returned XML formatted string.
     */
    public String create(StringBuffer sbXMLRecord) {

	String sUser = null;
	String sPassword = null;
	String sDatabase = null;
	String sSchema = null;
	String sRecordType = null;
	String sDisplayName = null;
	String sError = null;
	String sClient = null;
	CqProvider provider = null;
	CqRecord newTeamCqRecord = null;
	CqRecord createdTeamCqRecord = null;
	cqCreateInput xCqCreateInput = null;
	Feedback feedback = null;
	cqUsageMeasurement xCqUsageMeasurement = null;
	cqClientAuthorization xCqClientAuthorization = null;
	CqRecordType xRecordType = null;
	
	try {

		// Process the data in XML.
	    xCqCreateInput = new cqCreateInput(sbXMLRecord);
	    sUser = xCqCreateInput.getCqUser().getUsername();
	    sPassword = xCqCreateInput.getCqUser().getPassword();
	    sDatabase = xCqCreateInput.getCqUser().getDatabase();
	    sSchema = xCqCreateInput.getCqUser().getSchema();
	    sClient = xCqCreateInput.getCqClient().getClientName();
	    sRecordType = xCqCreateInput.getCqRecord().getRecordType();

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

	    // Construct the service name and validate.
	    String sService = cqUtil.CREATE + "-" + sRecordType;
	    cqUtil.validateService(sService);

	    // Initialize the measurements collection.
	    xCqUsageMeasurement = new cqUsageMeasurement(xCqCreateInput
		    .getCqUser(), xCqCreateInput.getCqClient(), sService, true);

	    // Check authorization for the service.
	    xCqClientAuthorization = new cqClientAuthorization(xCqCreateInput
		    .getCqUser(), xCqCreateInput.getCqClient(), sService);
	    xCqClientAuthorization.checkAuthorization();

	    // Initialize the CqRecord object.
	    provider = getProvider(sUser, sPassword);

	    // Get the requested CqRecordType from the requested database proxy.
	    xRecordType = getCqRecordType(provider, sSchema, sDatabase, sRecordType);

	    // get a new proxy CqRecord with a user friendly "suggested" location 
	    // of the requested type.  Name has not been set yet.
	    newTeamCqRecord = provider.cqRecord((StpLocation) xRecordType
	    		.getUserFriendlyLocation().child("new"));

	    feedback = new PropertyRequestItem.PropertyRequest(
			    new PropertyNameList.PropertyName[] {CqRecord.DISPLAY_NAME,
				    CqRecord.ALL_FIELD_VALUES, CqRecord.STABLE_LOCATION});

	    // Creating the record with the requested properties, but not 
	    // delivering it yet, because required fields are yet to be set
	    // this will establish a stable location, however (I think).
	    createdTeamCqRecord = (CqRecord) newTeamCqRecord
    		.doCreateRecord(feedback, CqProvider.HOLD);

	    // Refresh proxy. Location may have changed from the user friendly 
	    // location
	    // this worked before, but seems to lose the feedback now and the 
	    // entity appears missing in the db, at least for dip_revisions
	    //createdTeamCqRecord = xRecordType.cqProvider()
    	//.cqRecord(createdTeamCqRecord.getStableLocation());
    
	    // Set the fields of the CqRequest.
	    
	    if (sRecordType.equals("dip_revision")) {
	    	// First field indicates webservice, needed by dip_revision types 
	    	// to turn off read-only, which only happens for webservices
	    	createdTeamCqRecord.setField("process_type", "webservice");
	    }
	    
	    xCqCreateInput.getCqRecord().populateFields(createdTeamCqRecord);
	    // Commit the record.
	    createdTeamCqRecord = (CqRecord) createdTeamCqRecord
	    	.doWriteProperties(feedback, CqProvider.DELIVER);

	    // Display name as the id "<DBNAME>000nnnnn" format.
	    sDisplayName = createdTeamCqRecord.getDisplayName();

	    // Update CQ database that the client just used "create"
	    // service.
	    cqSysAppUsage xCqSysAppUsage = new cqSysAppUsage(sService, sClient,
		    sUser);
	    xCqSysAppUsage.recordUsageToCQ(provider, sSchema, sDatabase);

	    // Release the resource.
	    if (provider != null) {
		provider.terminate();
	    }

	    // Finish the measurements collection.
	    xCqUsageMeasurement.record();

	} catch (Exception e) {
	    StringWriter sw = new StringWriter();
	    e.printStackTrace(new PrintWriter(sw, true));
	    try {
		sError = cqUtil.filterXMLReservedChars(sw.toString());
	    } catch (Exception ignore) {
		sError = sw.toString();
	    }
	} finally {
	    provider = null;
	    newTeamCqRecord = null;
	    createdTeamCqRecord = null;
	    xCqCreateInput = null;
	    xCqUsageMeasurement = null;
	    xCqClientAuthorization = null;
	}
	return getCreateResponse(sDisplayName, sError).toString();
    }

    /**
     * Prepares the XML formatted return string.
     * 
     * @param sDisplayName
     *            CQ Issue display name (for example, MDCMS0000012345)
     * @param sError
     *            The error that needs to be written to return string if the
     *            operation/action was not successful, the error/exception
     * @return
     */
    private StringBuffer getCreateResponse(String sDisplayName, String sError) {

	StringBuffer sbReturnXML = new StringBuffer();
	sbReturnXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	sbReturnXML.append("<CqProblem>\n");

	sbReturnXML.append("	<record>\n");
	if (cqUtil.isEmpty(sDisplayName)) {
	    sbReturnXML.append("		<id></id>\n");
	} else {
	    sbReturnXML.append("		<id>" + sDisplayName + "</id>\n");
	}
	sbReturnXML.append("	</record>\n");

	sbReturnXML.append("	<error>\n");
	sbReturnXML.append("		<description>" + sError + "</description>\n");
	sbReturnXML.append("	</error>\n");
	sbReturnXML.append("</CqProblem>\n");

	return sbReturnXML;
    }

}
