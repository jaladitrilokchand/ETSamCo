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
 * Chanage Request business object.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 04/15/2011 GFS  Initial coding. 
 * 06/24/2011 GFS  Reworked dbAdd to use new "reserved" ChanageRequests.
 * 09/06/2011 GFS  Added support for Change Request type and severity.
 * 09/19/2011 GFS  Changed active to default.
 * 11/17/2011 GFS  Added StageName_Db member and getter/setter.
 * 01/11/2012 GFS  Updated getAllWithStatus() to limit query by max revision.
 * 01/16/2012 GFS  Updated getAllWithStatus() to limit query by min revision.
 * 08/23/2012 GFS  Added exists(), hasCommits() and dbDelete() methods.
 * 11/29/2012 GFS  Updated to support CR's new impacted customer attribute.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkConstants;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestActive_Db;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestSeverity_Db;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestStatus_Db;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestType_Db;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.CompVersion_ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.StageName_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequest {


    // Constants
    public static final int MAX_DESC = 3000;


    /**
     * Constructor - takes a ClearQuest name
     * 
     * @param xContext Application context
     * @param aCqId A ClearQuest id (MDCMS######## ...)
     * @throws IcofException
     */
    public ChangeRequest(EdaContext xContext, String aName)
    throws IcofException {

	setClearQuest(aName);
	//dbLookupByCq(xContext);
    }


    /**
     * Constructor - takes a ClearQuest name, description and status
     * 
     * @param xContext Application context
     * @param aCqId A ClearQuest id (MDCMS######## ...)
     * @param aDesc Description of change
     * @param aStatus ChangeRequest status
     * @param aType ChangeRequest type
     * @param aSeverity ChangeRequest severity
     * @param axCustomer Customer impacted by this change request
     * @throws IcofException
     */
    public ChangeRequest(EdaContext xContext, String aName, String aDesc,
                         ChangeRequestStatus aStatus, ChangeRequestType aType,
                         ChangeRequestSeverity aSeverity, String aCustomer)
                         throws IcofException {

	setClearQuest(aName);
	setDescription(aDesc);
	setStatus(aStatus);
	setType(aType);
	setSeverity(aSeverity);
	setImpactedCustomer(aCustomer);
    }


    /**
     * Constructor - takes objects
     * 
     * @param xContext Application context
     * @param aRequest A database object
     */
    public ChangeRequest(EdaContext xContext, ChangeRequest_Db aRequest) {

	setChangeRequest(aRequest);
	populate(xContext);
    }


    /**
     * Constructor - takes IDs
     * 
     * @param xContext Application context
     * @param anId A ChangeRequestStatus object id
     * @throws IcofException
     */
    public ChangeRequest(EdaContext xContext, long anId) throws IcofException {

	dbLookupById(xContext, anId);
	populate(xContext);
    }


    /**
     * Data Members
     * @formatter:off
     */
    private String description;
    private String clearQuest;
    private String impactedCustomer;
    private ChangeRequest_Db changeRequest;
    private ChangeRequestStatus status;
    private ChangeRequestType type;
    private ChangeRequestSeverity severity;
    private Vector<Component_Version_Db> compVersions;
    private StageName_Db stageName;
    private Component component;
    private Hashtable<String, ToolKit> toolKits;
    private Hashtable<String,CodeUpdate_Db> codeUpdates;


    /**
     * Getters
     */
    public String getDescription() { return description; }
    public String getClearQuest() { return clearQuest; }
    public String getImpactedCustomer() { return impactedCustomer; }
    public ChangeRequest_Db getChangeRequest() { return changeRequest; }
    public ChangeRequestStatus getStatus() { return status; }
    public ChangeRequestType getType() { return type; }
    public ChangeRequestSeverity getSeverity() { return severity; }
    public Component getComponent() { return component; }
    public Vector<Component_Version_Db> getCompVersions() { return compVersions; }
    public StageName_Db getStageName() { return stageName; }
    public Hashtable<String, CodeUpdate_Db> getCodeUpdates() { return codeUpdates; }
    public Hashtable<String, ToolKit> getToolKits() { return toolKits; }


    /**
     * Setters
     */
    private void setChangeRequest(ChangeRequest_Db aRequest) { changeRequest = aRequest; }
    private void setDescription(String aDesc) { description = aDesc; }
    private void setClearQuest(String anId) { clearQuest = anId; }
    private void setStatus(ChangeRequestStatus aStatus) { status = aStatus; }
    private void setType(ChangeRequestType aType) { type = aType; }
    private void setSeverity(ChangeRequestSeverity aSeverity) { severity = aSeverity; }
    private void setImpactedCustomer(String aCust)  { impactedCustomer = aCust; }
    // @formatter:on

    /**
     * Lookup the Component_Version_Db object for this ChangeRequest
     * 
     * @param xContext Application object
     * @throws IcofException
     */
    public void setCompVersions(EdaContext xContext) throws IcofException {

	if (getCompVersions() == null) {

	    // Lookup the CompVers for this Change Request.
	    CompVersion_ChangeRequest_Db cvcr;
	    cvcr = new CompVersion_ChangeRequest_Db(getChangeRequest(),null);
	    compVersions = cvcr.dbLookupCompVersions(xContext);

	}

    }


    /**
     * Read the component for this Change Request
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    public void setComponent(EdaContext xContext)
    throws IcofException {

	CompVersion_ChangeRequest_Db cvcr;
	cvcr = new CompVersion_ChangeRequest_Db(getChangeRequest(), null);
	Vector<Component_Version_Db> compVers = cvcr.dbLookupCompVersions(xContext);

	// Assume there is only 1 ComponentTkVersion for each ChangeRequest
	// so return after we find the first one.
	for (Component_Version_Db compVer : compVers ) {
	    compVer.getCompRelease().dbLookupById(xContext);
	    compVer.getCompRelease().getComponent().dbLookupById(xContext);
	    component = new Component(xContext, 
	                              compVer.getCompRelease().getComponent());
	    component.dbLookupByName(xContext);

	    return;
	}

    }


    /**
     * Read the CodeUpdates for this Change Request
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    public void setCodeUpdates(EdaContext xContext)
    throws IcofException {

	CodeUpdate_ChangeRequest_Db cucr;
	cucr = new CodeUpdate_ChangeRequest_Db(null, getChangeRequest());
	codeUpdates = cucr.dbLookupCodeUpdates(xContext);

    }


    /**
     * Return the count of CodeUpdates updated since the sinceTms.
     * 
     * @param xContext Application Context
     * @param sinceTms A Timestamp object
     */
    public int countCodeUpdatesSince(EdaContext xContext, Timestamp sinceTms) {

	int count = 0;

	if (getCodeUpdates() != null) {

	    if (sinceTms == null) {
		count = getCodeUpdates().size();
	    }
	    else {
		Iterator<CodeUpdate_Db> iter = getCodeUpdates().values()
		.iterator();
		while (iter.hasNext()) {
		    CodeUpdate_Db cu = iter.next();
		    if (cu.getUpdatedOn().after(sinceTms))
			count++;
		}
	    }
	}

	return count;

    }


    /**
     * Set the status from the status database object.
     * 
     * @param xContext Application context
     * @param aStatus Database object
     */
    private void setStatus(EdaContext xContext, ChangeRequestStatus_Db aStatus) {

	status = new ChangeRequestStatus(xContext, aStatus);
    }


    /**
     * Set the type from the Type database object.
     * 
     * @param xContext Application context
     * @param aType Database object
     */
    private void setType(EdaContext xContext, ChangeRequestType_Db aType) {

	type = new ChangeRequestType(xContext, aType);
    }


    /**
     * Set the severity from the severity database object.
     * 
     * @param xContext Application context
     * @param aStatus Database object
     */
    private void setSeverity(EdaContext xContext,
                             ChangeRequestSeverity_Db aSeverity) {

	severity = new ChangeRequestSeverity(xContext, aSeverity);
    }


    /**
     * Lookup the object from the database id
     * 
     * @param xContext Application context.
     * @param anId A database id
     * @throws IcofException
     */
    public void dbLookupById(EdaContext xContext, long anId)
    throws IcofException {

	if (getChangeRequest() == null) {
	    try {
		changeRequest = new ChangeRequest_Db(anId);
		changeRequest.dbLookupById(xContext);
		setClearQuest(changeRequest.getCqName());
		setDescription(changeRequest.getDescription());
	    }
	    catch (IcofException trap) {
		changeRequest = null;
		throw new IcofException(this.getClass().getName(),
		                        "dbLookupById()", IcofException.SEVERE,
		                        "Unable to find ChangeRequest (" + anId
		                        + ") in the database.\n",
		                        trap.getMessage());
	    }
	}
    }


    /**
     * Lookup the object from the ClearQuestId
     * 
     * @param xContext Application context.
     * @throws IcofException
     */
    public void dbLookupByCq(EdaContext xContext)
    throws IcofException {

	if (getChangeRequest() == null) {
	    try {
		changeRequest = new ChangeRequest_Db(getClearQuest());
		changeRequest.dbLookupByName(xContext);
		populate(xContext);

	    }
	    catch (IcofException trap) {
		changeRequest = null;
		throw new IcofException(this.getClass().getName(),
		                        "dbLookupByCq()", IcofException.SEVERE,
		                        "Unable to find ChangeRequest ("
		                        + getClearQuest()
		                        + ") in the database.\n",
		                        trap.getMessage());
	    }
	}
    }


    /**
     * Add this object to the database
     * 
     * @param xContext Application object
     * @param creator Person adding this object
     * @param newRequest If true get the next reserved CR record.
     * @throws IcofException
     * @return True if object created false object existing object.
     */
    public boolean dbAdd(EdaContext xContext, User_Db creator,
                         boolean newRequest)
                         throws IcofException {

	// Validate the description.
	String errors = verifyDescription(xContext, getDescription());
	if ((errors != null) && (! errors.equals(""))) {
	    IcofException ie = new IcofException("ChangeRequest", "dbAdd",
	                                         IcofException.SEVERE, errors,
	                                         getDescription());
	    throw ie;
	}

	// If autoGen then find the next "reserved" CR and update it with the
	// current description and status.
	if (newRequest) {
	    ChangeRequest_Db nextCr = new ChangeRequest_Db(getClearQuest(),
	                                                   getDescription(),
	                                                   getStatus().getStatus(),
	                                                   getType().getDbObject(),
	                                                   getSeverity().getDbObject(),
	                                                   getImpactedCustomer());
	    try {
		nextCr.dbLookupNextReserved(xContext);
	    }
	    catch (IcofException trap) {
		return false;
	    }

	    nextCr.dbUpdateRow(xContext, nextCr.getCqName(), getDescription(),
	                       getStatus().getStatus(),
	                       getType().getDbObject(),
	                       getSeverity().getDbObject(),
	                       getImpactedCustomer(), creator, creator);

	    setChangeRequest(nextCr);

	    return true;
	}
	
	// if ClearQuest id is DEV then always create new ChangeRequest
	else if  (getClearQuest().equalsIgnoreCase("DEV")) {
	    
	    changeRequest = new ChangeRequest_Db(getClearQuest(),
	                                         getDescription(),
	                                         getStatus().getStatus(),
	                                         getType().getDbObject(),
	                                         getSeverity().getDbObject(),
	                                         getImpactedCustomer());
	    changeRequest.dbAddRow(xContext, creator);

	    return true;
	    
	}

	// If we get here then we have an MDCMS change request
	else {
	    try {

		// Lookup the object in the database first.
		dbLookupByCq(xContext);
		return false;

	    }
	    catch (IcofException trap) {
		// Add the new object
		changeRequest = new ChangeRequest_Db(getClearQuest(),
		                                     getDescription(),
		                                     getStatus().getStatus(),
		                                     getType().getDbObject(),
		                                     getSeverity().getDbObject(),
		                                     getImpactedCustomer());
		getChangeRequest().dbAddRow(xContext, creator);
	    }

	    return true;
	}

    }


    /**
     * Update this object in the database
     * 
     * @param xContext Application object
     * @param newClearQuest New ClearQuest name
     * @param newDesc New description
     * @param newStatus New ChangeRequestStatus
     * @param newType New ChangeRequestType
     * @param newSeverity New ChangeRequestSeverity
     * @param newCustomer New customer impacted by the change request
     * @param editor Person updating this object
     * @throws IcofException
     * @throws Exception
     */
    public void dbUpdate(EdaContext xContext, 
                         String newClearQuest,
                         String newDesc, 
                         ChangeRequestStatus newStatus,
                         ChangeRequestType newType,
                         ChangeRequestSeverity newSeverity, 
                         String newCustomer,
                         User_Db editor)
                         throws IcofException {

	// Validate the description.
	String errors = verifyDescription(xContext, newDesc);
	if ((errors != null) && (!errors.equals(""))) {
	    IcofException ie = new IcofException("ChangeRequest", "dbUpdate",
	                                         IcofException.SEVERE, errors,
	                                         newDesc);
	    throw ie;
	}

	try {
	    // Lookup the object in the database first.
	    dbLookupByCq(xContext);
	}
	catch (IcofException trap) {
	    throw new IcofException(this.getClass().getName(), "dbUpdate()",
	                            IcofException.SEVERE,
	                            "Unable to find existing object ("
	                            + getClearQuest() + ") in the database.\n",
	                            trap.getMessage());
	}
	
	// Update the object
	getChangeRequest().dbUpdateRow(xContext, 
	                               newClearQuest, 
	                               newDesc,
	                               newStatus.getStatus(),
	                               newType.getDbObject(),
	                               newSeverity.getDbObject(), 
	                               newCustomer, 
	                               null, 
	                               editor);
	populate(xContext);

    }


    /**
     * Create a key from the ID.
     * 
     * @param xContext Application context object.
     * @return A Statement object.
     */
    public String getIdKey(EdaContext xContext) {

	return String.valueOf(getChangeRequest().getId());
    }


    /**
     * Display this object as a string
     * 
     * @param xContext Application context
     * @return This object as a string.
     */
    public String toString(EdaContext xContext) {

	// Get the class specific data
	StringBuffer buffer = new StringBuffer();
	buffer.append("ChangeRequest\n---------------\n");
	buffer.append("Clear Quest: " + getClearQuest() + "\n");
	if (getStatus() != null)
	    buffer.append("Status ID: " + getStatus().getStatus().getId() + "\n");
	if (getType() != null)
	    buffer.append("Type ID: " + getType().getDbObject().getId() + "\n");
	if (getSeverity() != null)
	    buffer.append("Severity ID: " + getSeverity().getDbObject().getId()
	              + "\n");
	buffer.append("Description: " + getDescription() + "\n");
	buffer.append("Impacted Customer: " + getImpactedCustomer() + "\n");
	if (getComponent() == null) 
	    buffer.append("Component  : not set yet\n");
	else 
	    buffer.append("Component  : " + getComponent().getName() + "\n");
	return buffer.toString();

    }


    /**
     * Delete a CR
     * 
     * @param aUser User_Db object
     * 
     * @return True if default CR deleted otherwise false.
     * @throws IcofException
     */
    public boolean dbDelete(EdaContext xContext, User_Db user)
    throws IcofException {

	// Does the CR exist in the DB
	if (!exists(xContext)) {
	    System.out.println("No Change Request found (" + getClearQuest()
	                       + ").");
	    return false;
	}

	// Does it have commits?
	if (hasCommits(xContext)) {
	    System.out.println("Unable to delete this Change Request due to "
	    + "existing SVN commits for this CR.");
	    return false;
	}

	// Delete it from Component_TkVersion and Change Request tables
	CompVersion_ChangeRequest_Db cvcr;
	cvcr = new CompVersion_ChangeRequest_Db(getChangeRequest(), null);
	cvcr.dbDeleteRowCrs(xContext);

	getChangeRequest().dbDeleteRow(xContext, user);

	return true;

    }


    /**
     * Determine if this Change Request has commits associated with it
     * 
     * @param xContext Application context
     * @return
     * @throws IcofException
     */
    private boolean hasCommits(EdaContext xContext)
    throws IcofException {

	CodeUpdate_ChangeRequest_Db cucr;
	cucr = new CodeUpdate_ChangeRequest_Db(null, getChangeRequest());
	Hashtable<String, CodeUpdate_Db> cus = cucr.dbLookupCodeUpdates(xContext);

	boolean hasCommits = false;
	if (cus.size() > 0) {
	    Iterator<String> iter = cus.keySet().iterator();
	    while (iter.hasNext()) {
		String key = (String) iter.next();
		CodeUpdate_Db cu = (CodeUpdate_Db) cus.get(key);
		System.out.println("Revision: " + cu.getRevision());
	    }

	    hasCommits = true;
	}

	return hasCommits;

    }


    /**
     * Determines if the Change Request exists in the DB
     * 
     * @param xContext Application context
     * @return
     */
    private boolean exists(EdaContext xContext) {

	try {
	    dbLookupByCq(xContext);
	}
	catch (IcofException trap) {
	    return false;
	}

	return true;

    }


    /**
     * Create a list of Default CRs for this user.
     * 
     * @param xContext Application object
     * @param user User_Db object
     */
    public Vector<ChangeRequestActive_Db> getAllDefaultUser(EdaContext xContext,
                                                            User_Db user)
                                                            throws IcofException {

	Vector<ChangeRequestActive_Db> crs = new Vector<ChangeRequestActive_Db>();

	// Look up the default CRs for this user
	ChangeRequestActive_Db activeCr = new ChangeRequestActive_Db(user,
	                                                             null, null);
	crs = activeCr.dbLookupByUser(xContext);

	return crs;

    }


    /**
     * Determine which ToolKit (RelVersion) this CR is associated with.
     * 
     * @param xContext Application object
     */
    public ToolKit getToolKit(EdaContext xContext)
    throws IcofException {

	// Lookup the ComponentVersion.
	setCompVersions(xContext);

	ToolKit tk = new ToolKit(xContext, getCompVersions().firstElement().getVersion()
	                         .getId());
	tk.dbLookupById(xContext, getCompVersions().firstElement().getVersion().getId());

	return tk;

    }


    /**
     * Verify this Change Request could be applied to the specified Branch
     * 
     * @throws IcofException
     * 
     */
    public boolean isValidBranch(EdaContext xContext, Branch aBranch,
                                 Component aComp)
                                 throws IcofException {

	// Get the Branch's Component_TkVersion
	Vector<ToolKit> brTks = aBranch.findToolKits(xContext, aComp);

	// Get the ChangeRequest's ComponentTkVersion
	setCompVersions(xContext);

	// If they are equal then Branch is valid for this ChangeRequest
	boolean match = false;
	for (ToolKit tk : brTks) {
	    for (Component_Version_Db compVer : getCompVersions()) {
		if (tk.getToolKit().getId() == compVer.getVersion().getId())
		    match = true;
	    }
	}

	return match;

    }


    /**
     * Updates this CR status to the new status.
     * 
     * @param xContext Application context
     * @param newStatusText New status name
     * @param editor Person making this update
     * @throws Exception
     */
    public void dbUpdateStatus(EdaContext xContext, String newStatusTxt,
                               User_Db editor)
                               throws Exception {

	// Lookup the new status object
	ChangeRequestStatus newStatus = new ChangeRequestStatus(xContext,
	                                                        newStatusTxt);
	newStatus.dbLookupByName(xContext);

	// Update the status
	dbUpdateStatus(xContext, newStatus, editor);

    }


    /**
     * Updates this CR status to the new status.
     * 
     * @param xContext Application context
     * @param newStatus New ChangeRequestStatus object
     * @param editor Person making this update
     * @throws Exception
     */
    public void dbUpdateStatus(EdaContext xContext,
                               ChangeRequestStatus newStatus, User_Db editor)
                               throws Exception {

	// Update the status
	dbUpdate(xContext, getClearQuest(), getDescription(), newStatus,
	         getType(), getSeverity(), getImpactedCustomer(), editor);

    }


    /**
     * Create a list of CRs for this ComponentVersion and state
     * 
     * @param xContext Application object
     * @param compVer ComponentVersion search criteria
     * @param status ChangeRequestStatus search criteria
     * @param maxRev Limit query so CRs don't exceed this revision
     * @param minRev Limit query so CRs aren't less than this revision
     */
    public Vector<String> getAllWithStatus(EdaContext xContext,
                                           Component_Version_Db compVer,
                                           ChangeRequestStatus status,
                                           String maxRev, String minRev)
                                           throws IcofException {

	Vector<String> crs = new Vector<String>();

	// Look up the default CRs for this user
	ChangeRequest_Db cr = new ChangeRequest_Db("");
	crs = cr.dbLookupByCompVerStatus(xContext, compVer, status.getStatus(),
	                                 maxRev, minRev);

	return crs;

    }


    /**
     * Verify the description contains some characters.
     * 
     * @param xContext Application object
     * @param String Change request description
     */
    public String verifyDescription(EdaContext xContext, String aDesc) {

	String myErrors = "";

	// Is description null or empty
	if ((aDesc == null) || (aDesc.length() < 1)) {
	    myErrors += "Description is empty!\n";
	}

	// Is it too long
	else if (aDesc.length() > MAX_DESC) {
	    myErrors += "Description is too long!\n" + "Your description is "
	    + aDesc.length() + " characters and max length is "
	    + MAX_DESC + ".\n";
	}

	// Does it contain alphanumerics
	else {
	    if (!TkConstants.containsAlphabet(aDesc))
		myErrors += "Description did not contain any letters [A-Za-z]!\n";
	}

	return myErrors;

    }


    /**
     * Determine the Stage of the Tool Kit this Change Request is associated
     * with.
     * 
     * @param xContext
     * @throws IcofException
     */
    public void setStageName(EdaContext xContext)
    throws IcofException {

	// Lookup this CR's component/version
	if (getCompVersions() == null) {
	    setCompVersions(xContext);
	}

	// Lookup the stage name for this CR's Tool Kit
	if (!getCompVersions().firstElement().getVersion().isLoaded()) {
	    getCompVersions().firstElement().getVersion().dbLookupById(xContext);
	}
	if (!getCompVersions().firstElement().getVersion().getStageName().isLoaded()) {
	    getCompVersions().firstElement().getVersion().getStageName().dbLookupById(xContext);
	}

	stageName = getCompVersions().firstElement().getVersion().getStageName();

    }


    /**
     * Populates the local members from the database object
     * 
     * @param xContext Application comtext
     */
    void populate(EdaContext xContext) {

	setClearQuest(getChangeRequest().getCqName());
	setDescription(getChangeRequest().getDescription());
	setStatus(xContext, getChangeRequest().getStatus());
	setType(xContext, getChangeRequest().getType());
	setSeverity(xContext, getChangeRequest().getSeverity());
	setImpactedCustomer(getChangeRequest().getImpactedCustomer());
    }


    /**
     * Update the component package id for this object
     * 
     * @param xContext Application context
     * @param pkgId New comp pkg id
     * @param user Person making this update
     */
    public void dbUpdateCompPkg(EdaContext xContext, long id, User_Db user) {



    }


    /**
     * Create a list of Change Requests for this component package
     *
     * @param xContext Application context
     * @param compPkg  Component package to look up
     * @return Collection of Change Requests
     * @throws IcofException 
     */
    public Vector<ChangeRequest> dbLookupByCompPkg(EdaContext xContext,
                                                   ComponentPackage compPkg)
                                                   throws IcofException {

	Vector<ChangeRequest_Db> dbCrs;
	if (getChangeRequest() == null)
	    changeRequest = new ChangeRequest_Db(0);
	dbCrs = getChangeRequest().dbLookupByCompPkg(xContext, 
	                                             compPkg.getDbObject().getId());

	Vector<ChangeRequest> crs = new Vector<ChangeRequest>();
	for (ChangeRequest_Db dbCr : dbCrs) {
	    ChangeRequest cr = new ChangeRequest(xContext, dbCr);
	    crs.add(cr);
	}

	return crs;

    }
}
