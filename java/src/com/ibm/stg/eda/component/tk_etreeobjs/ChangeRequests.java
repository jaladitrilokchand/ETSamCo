/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2013 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *
 *-PURPOSE---------------------------------------------------------------------
 * Create and manage lists of Change Requests 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 11/20/2013 GFS  Initial coding.
 * 05/13/2014 GFS  Updated to support a comma delimited list of CRs.
 *=============================================================================
 * </pre>
 */
package com.ibm.stg.eda.component.tk_etreeobjs;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import com.ibm.stg.eda.app.etree.ChangeRequestShowByStatus;
import com.ibm.stg.eda.app.etree.GetRevisionsTk;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_patch.TkInjectUtils;
import com.ibm.stg.eda.component.tk_patch.TkPatch;
import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.SessionLog;
import com.ibm.stg.iipmds.icof.component.clearquest.cqFetchOutput;
import com.ibm.stg.iipmds.icof.webclient.clearquest.CqClientUtil;
import com.ibm.stg.iipmds.icof.webclient.clearquest.CqService_PortType;
import com.ibm.stg.iipmds.icof.webclient.clearquest.CqService_PortTypeProxy;


public class ChangeRequests {

        
    /**
     * Constants
     */
    public static final String AUTHORIZED_ID = "svnlib@us.ibm.com";

    
    /**
     * Constructor
     *
     * @param aTk   A Tool Kit object
     * @param aComp A Component object
     */
    public ChangeRequests(ToolKit aTk, Component aComp) {
	setToolKit(aTk);
	setComponent(aComp);
    }

    
    /**
     * Determine change requests
     * 
     * @param xContext Application context
     * @param xChgRequest Comma delimited list of CRs if CR mode
     * @param sPatchName  Patch name for Patch mode
     * @param bAgts       If true then in AGTS mode
     * @param bCtk        If true then in CTK mode
     * @param bVerbose    If true display debug messages
     * @throws Exception
     */
    public void setChangeRequests(EdaContext xContext, 
                                  String changeReqText, String sPatchName,
                                  boolean bAgts, boolean bCtk, boolean bVerbose)
    throws Exception {

	/*
	 * There are 4 possible Change Request scenarios to deal with ... 
	 *  1) CR mode - Comma delimited list of CRs passed on command line 
	 *     with -z switch 
	 *  2) Patch mode - patch name passed on command line and need to 
	 *     determine CRs from patch 
	 *  3) AGTS mode - New TK being created and CRs must be determined 
	 *     from build (really /afs/eda/prod) environment
	 *  4) CTK mode - no CRs to process
	 */
	logInfo(xContext, "Finding Change Requests ...", true);
	changeRequests = new HashMap<String, ChangeRequest>();

	// Option #1 .. single CR passed on command line
	if (changeReqText != null && ! changeReqText.isEmpty()) {
	    logInfo(xContext,
		    " CR mode - found Change Request(s) passed on command line ...",
		    true);
	    setChgReqsFromList(xContext, changeReqText, bVerbose);
	}

	// Option #2 .. Patch or patches passed on command line
	else if (sPatchName != null && !sPatchName.isEmpty()) {
	    logInfo(xContext, " Patch mode - looking up Change Requests from TK Patch ...",
		    true);
	    setChgReqsFromPatch(xContext, sPatchName, bVerbose);
	}
	
	// Option #3 .. Get completed CRs from development
	else if (bAgts) {
	    logInfo(xContext,
		    " AGTS mode - looking up completed development Change Requests ...",
		    true);
	    setCrsFromDevelopment(xContext, bVerbose);
	}
	
	// Option #4 .. ignore CR processing
	else {
	    logInfo(xContext, " CTK mode - ignoring Change Requests ...",
	            true);
	    return;
	}
	
	// Display/log change requests
	if (changeRequests.isEmpty()) {
	    throw new IcofException("ChangeRequests", "setChangeRequests()", 
	                            IcofException.SEVERE,
	                            "No change requests found", "");
	}
	logInfo(xContext, "\nChange Request(s)\n------------------", bVerbose);
	for (String crName : changeRequests.keySet()) {
	    logInfo(xContext, crName, bVerbose);
	}
	
    }


    /**
     * Create a collection of change request objects from a comma delimited 
     * list of CR names.
     *
     * @param xContext  Application context
     * @param changeReqText  CDL of CR names
     * @param bVerbose If true show debug messages
     * @throws IcofException 
     */
    private void setChgReqsFromList(EdaContext xContext, String changeReqText,
				    boolean bVerbose) throws IcofException {

	// Query each patch in CQ
	String[] tokens = changeReqText.split("[,]+");
	for (String crName : tokens) {
	   crName = crName.trim();
	    
	   ChangeRequest myCr = new ChangeRequest(xContext, crName);
	   myCr.dbLookupByCq(xContext);
	   changeRequests.put(myCr.getClearQuest() , myCr);

	}
	
    }


    /**
     * Create a list of development Change Requests which are in the COMPLETE
     * state
     * 
     * @param xContext Application context
     * @return Collection of Change Request objects
     * @throws IcofException
     */
    private void setCrsFromDevelopment(EdaContext xContext, boolean bVerbose)
    throws IcofException {

	// Look up the ship location
	Location shipLocation = new Location(xContext, 
	                                     "customtkb/tk" + getToolKit().getName());
	shipLocation.dbLookupByName(xContext);

	// Look up the development tool kit
	ToolKit devTk = new ToolKit(xContext, getToolKit().getToolKit()
							  .getRelease()
							  .getName()
							  + ".build");
	devTk.dbLookupByName(xContext);

	// Determine the latest revision in ship
	GetRevisionsTk revApp = new GetRevisionsTk(xContext, getToolKit(),
	                                           getComponent(),
						   shipLocation,
						   true);
	revApp.lookupResults(xContext, true);
	if (revApp.getRevisions().isEmpty()) {
	    throw new IcofException(this.getClass().getSimpleName(),
				    "setCrsFromDevelopment",
				    IcofException.SEVERE,
				    "unable to determine latest SVN revision",
				    "Location: " + shipLocation.getName());
	}
	String latestShipRev = revApp.getRevisions().firstElement();
	logInfo(xContext, "  Latest revision in " + shipLocation.getName()
			  + " is " + latestShipRev, bVerbose);

	// Find completed development CRs for revisions less than or equal 
	// to the latest ship revision
	ChangeRequestStatus completeStatus = new ChangeRequestStatus(xContext,
							       "COMPLETE");
	completeStatus.dbLookupByName(xContext);

	ChangeRequestShowByStatus crApp;
	crApp = new ChangeRequestShowByStatus(xContext, devTk, getComponent(),
	                                      completeStatus, latestShipRev, "0");
	crApp.setChangeRequests(xContext);

	for (String crName : crApp.getChangeRequests()) {
	    ChangeRequest cr = new ChangeRequest(xContext, crName);
	    cr.dbLookupByCq(xContext);
	    changeRequests.put(cr.getClearQuest(), cr);
	}

    }


    /**
     * Query ClearQuest for the inject requests (CRs) associated with this TK
     * patch
     * 
     * @param xContext  Application context
     * @param patches   Patch text from command line (may be a comma delimited 
     *                  list of patch names
     * @param bVerbose  If true show progress
     * @return Collection of Change Request objects
     * @throws Exception
     */
    private void setChgReqsFromPatch(EdaContext xContext, String patches, 
                                     boolean bVerbose)
    throws Exception {

	// Query each patch in CQ
	String[] tokens = patches.split("[,]+");
	for (String patchName : tokens) {
	   patchName = patchName.trim();
	    
	    // Query CQ for the patch info
	    setPatch(xContext, patchName, bVerbose);

	    // Add CRs to collection
	    for (String crName : getPatch().getInjectRequests().keySet()) {
		ChangeRequest myCr = new ChangeRequest(xContext, crName);
		myCr.dbLookupByCq(xContext);
		changeRequests.put(myCr.getClearQuest() , myCr);
	    }

	}
	    
    }


    /**
     * Read patch data from CQ
     * 
     * @param xContext   Application context
     * @param sPatchName Patch to read from CQ
     * @param bVerbose   If true show progress
     * @throws Exception
     */
    private void setPatch(EdaContext xContext, String sPatchName,
                               boolean bVerbose)
    throws Exception {

	// Prepare the XML file
	String genericPassword = getAuthIdPassword();
	String sQuery = TkInjectUtils.getQueryString(AUTHORIZED_ID,
						     genericPassword,
						     sPatchName);
	
	// Set the SSL properties.
	CqClientUtil.setSSLProperties();

	// Call the service.
	CqService_PortTypeProxy proxy = new CqService_PortTypeProxy();
	CqService_PortType client = proxy.getCqService_PortType();
	proxy.setEndpoint(CqClientUtil.getServiceAddress(Constants.PROD));
	String sResult = client.runQuery(sQuery);

	// Parse the response.
	cqFetchOutput xCqFetchOutput = new cqFetchOutput(sResult);
	sResult = xCqFetchOutput.getResultsAsDelimited("##", true).toString();

	Vector<String[]> objects = xCqFetchOutput.getResultObjects();
	Vector<String[]> labels = xCqFetchOutput.getFieldLabels();

	// Populate the patch object
	patch = new TkPatch(labels, objects);

    }
  
    
    /**
     * Members
     * @formatter:off
     */
    private ToolKit toolKit;
    private Component component;
    private List<TkPatch> patches;
    private TkPatch patch = null;
    private HashMap<String, ChangeRequest> changeRequests = null;
    
    /**
     * Getters
     */
    public ToolKit getToolKit() { return toolKit; }
    public Component getComponent() { return component; }
    public HashMap<String, ChangeRequest> getChangeRequests() { return changeRequests; }
    public List<TkPatch> getPatches() { return patches; }
    public TkPatch getPatch() { return patch; }
    
    
    /**
     * Setters
     */
    private void setToolKit(ToolKit aTk) { toolKit = aTk; }
    private void setComponent(Component aComp) { component = aComp; }  
        
    
    /**
     * Log the message and echo it to the screen, if the verbose indicator is
     * true
     * 
     * @param aContext the application context (contains log)
     * @param msg the message to log
     */
    private void logInfo(EdaContext xContext, String msg, boolean bEcho) {

	xContext.getSessionLog().log(SessionLog.INFO, msg);
	if (bEcho) {
	    System.out.println(msg);
	}

    }
    
    
    /**
     * Read the svnlib id AFS password from a secure file
     * 
     * @return password
     * @throws IcofException
     */
    private String getAuthIdPassword()
    throws IcofException {

	String path = "/afs/eda/u/svnlib/private/svnlib.funcid";
	IcofFile pwFile = new IcofFile(path, false);
	pwFile.openRead();
	pwFile.read();
	pwFile.closeRead();

	if (pwFile.getContents().isEmpty()) {
	    throw new IcofException("PackagingUtils", "getAuthIdPassword()",
				    IcofException.SEVERE,
				    "unable to read intranet password from "
				    + "secure file for "
				    + AUTHORIZED_ID, "File: " + path);
	}

	String pw = (String) pwFile.getContents().firstElement();
	return pw.trim();

    }

}
