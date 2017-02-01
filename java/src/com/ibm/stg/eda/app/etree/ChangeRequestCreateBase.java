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
 * 03/11/2011 GFS  Initial coding.
 * 03/23/2011 GFS  Added auto approve if TK in development. Added auto generation
 *                 of CR number (for prototype only)
 * 04/18/2011 GFS  Updated to use business objects.
 * 06/23/2011 GFS  Updated to allow creating "reserved" Change Requests.
 * 08/01/2011 GFS  Made auto generate be the default behavior it can be overridden
 *                 by the -cq switch.
 * 08/08/2011 GFS  Added -f switch so description can be specified via a file.
 * 09/06/2011 GFS  Added support for defect/feature and severity.  Updated so 
 *                 help is shown if no parm specified.
 * 09/19/2011 GFS  Changed -active to -default.
 * 09/30/2011 GFS  Create the create() method.
 * 10/19/2011 GFS  Removed -auto as an application parameter.
 * 11/17/2011 GFS  Updated to support interim shipb injection process where we'll
 *                 approve CRs for preview (shipb) tool kit. Made -t switch 
 *                 optional and will default tool kit to tool kit in development.
 *                 Renamed active to default in help app help text.
 * 12/20/2011 GFS  Updated setDefaultToolKit() to lookup the tool kit by 
 *                 Component and StageName.
 * 02/14/2012 GFS  Turned auto-approval off for preview tool kits.
 * 08/01/2012 GFS  Updated so this application only create Change Requests.
 *                 Removed -t switch.
 * 09/20/2012 GFS  Refactored to support new user and cq classes.
 * 12/06/2012 GFS  Updated to support new customer impacted field.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.CompVersion_ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestSeverity;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestStatus;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestType;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;

public class ChangeRequestCreateBase {

    /**
     * Constructor - TK developers to create new CRs
     *
     * @param xContext       Application context
     * @param aComponent     Component to associate the CR with
     * @param aRecord        ClearQuest record name (MDCMS...)
     * @param aDescription   ChangeRequest's description
     * @param aStatus        ChangeRequest's status
     * @param aType          ChangeRequest's type
     * @param aSeverity      ChangeRequest's severity
     * @param bDefault       Set this CR as the default CR or not
     * @param bVerbose       Message status back to caller or not
     * @param aTk            ToolKit to create CR for (ignored if status is reserved)
     * @param aCustomer   Impacted customer
     */
    public ChangeRequestCreateBase(EdaContext xContext, 
                                   Component aComponent,
                                   String aDescriptionText,
                                   String aDescriptionFile, 
                                   ChangeRequestStatus aStatus,
                                   ChangeRequestType aType,
                                   ChangeRequestSeverity aSeverity,
                                   User_Db aCreator,
                                   boolean bVerbose,
                                   ToolKit aTk,
                                   String aCustomer)
                                   throws IcofException {

	setComponent(aComponent);
	setClearQuest(null);
	setDescriptionText(aDescriptionText);
	setDescriptionFile(aDescriptionFile);
	setStatus(aStatus);
	setType(aType);
	setSeverity(aSeverity);
	setUser(aCreator);
	setVerbose(bVerbose);
	setToolKit(aTk);
	setDeveloperRequest(true);
	setImpactedCustomer(aCustomer);

	showInputs(xContext);

    }


    /**
     * Constructor - used by CQ scripts to create change request records in the
     *               ETREE DB based on existing CQ records for reserved CRs
     *               or inject requests.
     *
     * @param xContext    Application context
     * @param aComponent  Component to associate the CR with
     * @param aDescText   ChangeRequest's description text
     * @param aDescFile   File containing ChangeRequest's description
     * @param aRecord     ClearQuest record name (like MDCMS00123456)
     * @param aStatus     ChangeRequest's status
     * @param aType       ChangeRequest's type
     * @param aSeverity   ChangeRequest's severity
     * @param aCreator    ChangeRequest's creator
     * @param bVerbose    Message status back to caller or not
     * @param aTk         ToolKit to create CR for (ignored if status is reserved)
     * @param aCustomer   Impacted customer
     */
    public ChangeRequestCreateBase(EdaContext xContext, 
                                   Component aComponent,
                                   String aDescText, 
                                   String aDescFile,
                                   String aRecord,
                                   ChangeRequestStatus aStatus,
                                   ChangeRequestType aType,
                                   ChangeRequestSeverity aSeverity,
                                   User_Db aCreator,
                                   boolean bVerbose,
                                   ToolKit aTk,
                                   String aCustomer)
                                   throws IcofException {

	setComponent(aComponent);
	setDescriptionText(aDescText);
	setDescriptionFile(aDescFile);
	setClearQuest(aRecord);
	setStatus(aStatus);
	setType(aType);
	setSeverity(aSeverity);
	setUser(aCreator);
	setVerbose(bVerbose);
	setToolKit(aTk);
	setImpactedCustomer(aCustomer);
	setDeveloperRequest(false);

	showInputs(xContext);

    }


    /**
     * Create the ChangeRequest
     * 
     * @param xContext  Application context
     * @throws IcofException
     */
    public void create(EdaContext xContext) throws IcofException {

	// Create the ChangeRequest.
	setDescription(xContext);
	setCompVersion(xContext);
	setMyChangeRequest(xContext);
	linkThem(xContext);

    }


    /**
     * Associate the ChangeRequest and ComponentVersion objects.
     * 
     * @param xContext Application context.
     * @throws IcofException 
     */
    private void linkThem(EdaContext xContext) throws IcofException {

	log("Assigning new ChangeRequest to ToolKit ...", isVerbose());

	// If this is a "reserved" CR then don't link it to a Compontnt_Version.
	if (isReserved()) {
	    log("Not assigning \"reserved\" ChangeRequest to ToolKit ...", 
	        isVerbose());
	    return;
	}

	CompVersion_ChangeRequest_Db cvcr = 
	new CompVersion_ChangeRequest_Db(getMyChangeRequest().getChangeRequest(),
	                                 getCompVersion());
	cvcr.dbAddRow(xContext);

	log("Assignment complete", isVerbose());

    }


    /**
     * Lookup the Component_TkVersion object.
     * @param xContext       Application context
     * @throws IcofException Trouble updating the ChangeRequest
     */
    private void setCompVersion(EdaContext xContext) throws IcofException {

	log("Looking up ComponentVersion ...", isVerbose());

	// If this is a "reserved" CR  then  don't look up the 
	// Compontnt_Version row
	if (isReserved()) {
	    log("Not looking up ComponentVersion for \"reserved\" ChangeRequest ...", 
	        isVerbose());
	    return;
	}

	// Construct the new ChangeRequest and add it to the DB
	compVersion  = new Component_Version_Db(xContext, 
	                                        getToolKit().getToolKit(),
	                                        getComponent().getComponent());
	compVersion.dbLookupByCompRelVersion(xContext);

	log("ComponentVersion found", isVerbose());

    }


    /**
     * Create the new ChangeRequest object.
     * @param xContext       Application context
     * @throws IcofException Trouble updating the ChangeRequest
     */
    private void setMyChangeRequest(EdaContext xContext) throws IcofException {

	log("Creating new ChangeRequest ...", isVerbose());

	// Construct the new ChangeRequest and add it to the DB
	myChangeRequest = new ChangeRequest(xContext,
	                                    getClearQuest(), 
	                                    getDescriptionText(),
	                                    getStatus(),
	                                    getType(),
	                                    getSeverity(),
	                                    getImpactedCustomer());
	boolean success = myChangeRequest.dbAdd(xContext, getUser(), 
	                                        isDeveloperRequest());

	if (! success) {
	    String msg;
	    if (isDeveloperRequest()) {
		msg = "\nUnable to automatically generate a ChangeRequest since \n" +
		"NO reserved ClearQuest/CRs are available.";
	    }
	    else {
		msg = "\nYou are trying to add a ChangeRequest that already exists.\n" +
		"If you want to update this ChangeRequest then run changeReqUpdate.\n";
	    }
	    throw new IcofException(this.getClass().getName(), 
	                            "setChangeRequest()",
	                            IcofException.SEVERE, msg, "");

	}

	// Set the members 
	if (isDeveloperRequest()) {
	    setClearQuest(getMyChangeRequest().getChangeRequest().getCqName());
	}

	log("ChangeRequest added", isVerbose());

    }


    /**
     * Determine the description.
     * @param xContext
     * @return Description or null if no new description
     * @throws IcofException 
     */
    private void setDescription(EdaContext xContext) throws IcofException {

	// Read the text description
	if (getDescriptionText() != null) {
	    return;
	}

	// Read the file description
	else if (getDescriptionFile() != null) {
	    IcofFile file = new IcofFile(getDescriptionFile(), false);
	    file.openRead();
	    file.read();
	    file.closeRead();

	    if (file.getContents() != null) {
		descriptionText = IcofCollectionsUtil.getVectorAsString(file.getContents(),
		                                                        "\n");
	    }
	}

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
    private boolean developerRequest = true;
    private boolean reservedFlag = false;
    private User_Db user;
    private Component component;
    private boolean verbose = false;
    private ToolKit toolKit = null;
    private ChangeRequest myChangeRequest;


    /**
     * Getters.
     */
    public Component getComponent()  { return component; }
    public String getDescriptionText()  { return descriptionText; }
    public String getDescriptionFile()  { return descriptionFile; }
    public String getClearQuest()  { return clearQuest; }
    public String getImpactedCustomer()  { return impactedCustomer; }
    public ChangeRequest getMyChangeRequest()  { return myChangeRequest; }
    public ChangeRequestStatus getStatus()  { return status; }
    public ChangeRequestType getType()  { return type; }
    public ChangeRequestSeverity getSeverity()  { return severity; }
    public Component_Version_Db getCompVersion()  { return compVersion; }
    public boolean isDeveloperRequest()  { return developerRequest; }
    public boolean isReserved()  { return reservedFlag; }
    public User_Db getUser()  { return user; }
    public boolean isVerbose()  { return verbose; }
    public ToolKit getToolKit()  { return toolKit; }


    /**
     * Setters.
     */
    private void setDescriptionText(String aText) { descriptionText = aText; }
    private void setDescriptionFile(String aFile) { descriptionFile = aFile; }
    private void setClearQuest(String aRecord) { clearQuest = aRecord; }
    private void setImpactedCustomer(String aCust) { impactedCustomer = aCust; }
    private void setStatus(ChangeRequestStatus aStatus) { status = aStatus; }
    private void setType(ChangeRequestType aType) { type = aType; }
    private void setSeverity(ChangeRequestSeverity aSev) { severity = aSev; }
    private void setUser(User_Db aUser) { user = aUser; }
    private void setVerbose(boolean aFlag) { verbose = aFlag; }
    private void setToolKit(ToolKit aTk)  { toolKit = aTk; }
    private void setDeveloperRequest(boolean aFlag)  { developerRequest = aFlag; }
    private void setComponent(Component aComp)  { 
	component = aComp;
	if (getComponent() == null) {
	    reservedFlag = true;
	}
    }


    /**
     * Display messages (mimics logInfo)
     */
    private void log(String aMsg, boolean showIt) {

	if (showIt) {
	    System.out.println(aMsg);
	}

    }


    /**
     * Display the class inputs
     * @throws Exception 
     */
    private void showInputs(EdaContext xContext) throws IcofException {

	setDescription(xContext);

	if (getComponent() == null) {
	    log("Component : null", isVerbose());
	}
	else {
	    log("Component : " + getComponent().getName(), isVerbose());
	}
	log("Change req: " + getClearQuest(), isVerbose());    	
	log("Desc text : " + getDescriptionText(), isVerbose());
	log("Desc file : " + getDescriptionFile(), isVerbose());
	log("Status    : " + getStatus().getName(), isVerbose());
	log("Type      : " + getType().getName(), isVerbose());
	log("Severity  : " + getSeverity().getName(), isVerbose());
	log("User      : " + getUser().getIntranetId(), isVerbose());
	log("Verbose?  : " + isVerbose(), isVerbose());
	if (getToolKit() != null) {
	    log("Tool kit  : " + getToolKit().getName(), isVerbose());
	}
	else {
	    log("Tool kit  : null", isVerbose());
	}
	log("Dev req?  : " + isDeveloperRequest(), isVerbose());
	log("Customer  : " + getImpactedCustomer(), isVerbose());

	// Uncomment for testing
	//throw new IcofException("USER EXIT", IcofException.SEVERE);

    }


}
