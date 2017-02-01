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
* Determine the maximum revision for the Change Requests in the TK Patch 
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 01/17/2011 GFS  Initial coding.
* 02/28/2012 GFS  Changed the version and made minor updates.  
* 03/13/2012 GFS  Added debug statements to track a password issue.  Added
*                 Release and Component to reported output.      
* 03/15/2012 GFS  Updated to use new version of TkInjectUtils.getQueryString()
*                 which does a better job of handling special chars in id/pw.
*                 Added developer name to the output.
* 03/21/2012 GFS  Updated quiet mode only show CRs. Updated usage.
* 04/11/2013 GFS  Added new -newstate switch to set the state of all CRs
*                 associated with the patch to the new state.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.PwField;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestStatus;
import com.ibm.stg.eda.component.tk_patch.TkInjectRequest;
import com.ibm.stg.eda.component.tk_patch.TkInjectUtils;
import com.ibm.stg.eda.component.tk_patch.TkPatchBase;
import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;
import com.ibm.stg.iipmds.icof.component.clearquest.cqFetchOutput;
import com.ibm.stg.iipmds.icof.webclient.clearquest.CqClientUtil;
import com.ibm.stg.iipmds.icof.webclient.clearquest.CqService_PortType;
import com.ibm.stg.iipmds.icof.webclient.clearquest.CqService_PortTypeProxy;

public class PatchRequests extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "patch.reqs";
    public static final String APP_VERSION = "v1.1";

        
    /**
     * Constructor
     *
     * @param     xContext  Application context
     * @param     aPatch    ClearQuest patch number
     * @param     aId       User's ClearQuest id if not not their intranet id
     * @param     aPw       User's ClearQuest pw (if null user promoted)
     */
    public PatchRequests(EdaContext xContext, String aPatch, String aId,
                         String aPw)	
    throws IcofException {

        super(xContext, APP_NAME, APP_VERSION);

        setPatchName(aPatch);
        setCqUserId(aId);
        setCqUserPw(aPw);

    }

    
    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public PatchRequests(EdaContext aContext) throws IcofException {

        this(aContext, null, null, null);

    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

    	TkAppBase myApp = null;
		try {

			myApp = new PatchRequests(null);
			start(myApp, argv);
		}
		catch (Exception e) {
			handleExceptionInMain(e);
		} finally {
			handleInFinallyBlock(myApp);
		}

    }

    
    /**
     * Run this application's process
     * 
     * @param aContext      Application Context
     * @throws IcofException 
     * @throws Exception 
     */
    public void process(EdaContext xContext) throws IcofException {

        // Connect to the database
        connectToDB(xContext);

        // Read Change Requests from CQ
        setChangeRequests(xContext);
             
        // Display the Change Requests
        showChangeRequests(xContext);

        // Set the return code to success if we get this far.
        setReturnCode(xContext, SUCCESS);
        commitToDB(xContext, APP_NAME);
        
    }

	/**
     * Look up the Change Requests from CQ for this TK Patch
     * 
     * @param xContext  Application context
     * @throws IcofException 
     * @throws Exception 
     */
    private void setChangeRequests(EdaContext xContext) throws IcofException{

    	// Get the user's Clear Quest id
    	if ((getCqUserId() == null) || (getCqUserId().equals(""))) {
    		setCqUserId(user.getIntranetId());
    	}
    	
    	// Get the user's CQ password
    	setCqUserPw();
    	
    	// Query CQ
    	if (! isQuietMode()) {
    		System.out.println("Fetching patch data from ClearQuest ... " +
    		                   "please be patient");
    	}
    	setPatch(xContext);
    	
    	// Look up the Change Requests for this CR
    	setCrEntries(xContext);
    	
	}

    
    /**
     * Look up the Change Request data
     * @param xContext
     * @throws IcofException 
     */
    private void setCrEntries(EdaContext xContext) throws IcofException {
    	
    	crEntries = new Vector<String>();
    	
    	int relLen = 9;
    	int compLen = 15;
    	int crLen = 15; 
    	int stateLen = 10;
    	int revLen = 10;
    	int devLen = 20;
    	
    	StringBuffer header = new StringBuffer();
    	if (! isQuietMode()) {
    		header.append("\n");
    		header.append(IcofStringUtil.padString("Release", relLen, " "));
    		header.append(IcofStringUtil.padString("Component", compLen, " "));
    		header.append(IcofStringUtil.padString("ChangeRequest", crLen, " "));
    		header.append(IcofStringUtil.padString("CR State", stateLen, " ")); 
    		header.append(IcofStringUtil.padString("Revision", revLen, " "));
    		header.append(IcofStringUtil.padString("Developer", devLen, " "));
    		
    		header.append("\n");
    		header.append(IcofStringUtil.padString("--------", relLen, " "));
    		header.append(IcofStringUtil.padString("----------", compLen, " "));
    		header.append(IcofStringUtil.padString("--------------", crLen, " "));
    		header.append(IcofStringUtil.padString("---------", stateLen, " "));
    		header.append(IcofStringUtil.padString("---------", revLen, " "));
    		header.append(IcofStringUtil.padString("----------", devLen, " "));
    	}
    	
    	Iterator<TkInjectRequest> iter = patch.getInjectRequests().values().iterator();
    	while (	iter.hasNext()) {
    		TkInjectRequest request =  iter.next();
    		ChangeRequest cr = new ChangeRequest(xContext, request.getId());
    		cr.dbLookupByCq(xContext);
    		
    		// Update the IR's state if requested
    		if (getNewState() != null) {
    			try {	
    				cr.dbUpdateStatus(xContext, getNewState(), user);
    			}
    			catch(Exception trap) {
    				throw new IcofException(APP_NAME, "updateChangeRequest()", 	
    				                        IcofException.SEVERE,
    				                        "Unable to update CR to requested state (" + 
    				                        getNewState() + ").", cr.getClearQuest());
    			}
    		}
    		cr.getChangeRequest().getStatus().dbLookupById(xContext);
    		
    		CodeUpdate_ChangeRequest_Db cucr = 
    			new CodeUpdate_ChangeRequest_Db(null, cr.getChangeRequest());
    		Hashtable<String,CodeUpdate_Db> codeUpdates = cucr.dbLookupCodeUpdates(xContext);
    		
    		StringBuffer entry = new StringBuffer(); 
    		
    		if (! isQuietMode()) {
    			entry.append(IcofStringUtil.padString(getPatch().getRelease(), relLen, " "));
    			entry.append(IcofStringUtil.padString(getPatch().getComponent(), compLen, " "));
    		}
    		entry.append(IcofStringUtil.padString(cr.getClearQuest(), crLen, " "));
    		if (! isQuietMode()) {
    			entry.append(IcofStringUtil.padString(cr.getChangeRequest().getStatus().getName(), stateLen, " "));
    			entry.append(IcofStringUtil.center("no commit", revLen));
    			entry.append(IcofStringUtil.padString(request.getDeveloper(), devLen, " "));
    		}
            
            Iterator<CodeUpdate_Db> iter2 = codeUpdates.values().iterator();
    		while (iter2.hasNext()) {
    			CodeUpdate_Db cu =  iter2.next();
    			int thisRev = Integer.parseInt(cu.getRevision());
    			if (thisRev > getMaxRevision()) {
    				setMaxRevision(thisRev);
    			}
    			entry.setLength(0);
    			if (! isQuietMode()) {
    				entry.append(IcofStringUtil.padString(getPatch().getRelease(), relLen, " "));
    				entry.append(IcofStringUtil.padString(getPatch().getComponent(), compLen, " "));
    			}
    			entry.append(IcofStringUtil.padString(request.getId(), crLen, " "));
    			if (! isQuietMode()) {
    				entry.append(IcofStringUtil.padString(cr.getChangeRequest().getStatus().getName(), stateLen, " "));
        			entry.append(IcofStringUtil.center(cu.getRevision(), revLen));
    				entry.append(IcofStringUtil.padString(request.getDeveloper(), devLen, " "));
    			}
    		}
    		if ((crEntries.size() < 1) && (! isQuietMode())) {
    			crEntries.add(header.toString());
    		}
    		crEntries.add(entry.toString());
    	}

	}


	/**
     * Get the user's password (echo * to mask the pw)
     * @throws IcofException 
     */
    private void setCqUserPw() throws IcofException {
    	
    	// Skip if password already set
    	if (getCqUserPw() != null) {
    		return;
    	}
    	
    	// Ask the user for their CQ password
        try {
        	cqUserPw = PwField.getPassword(System.in, 
        	                               "ClearQuest password(" +  getCqUserId() + "): ");
        } catch(IOException ioe) {
           ioe.printStackTrace();
        }
        	
        if (cqUserPw == null) {
        	throw new IcofException(APP_NAME, "setCqUserPw()", 
        	                        IcofException.SEVERE,
        	                        "No password entered ...\n", "");
        }
        
//        System.out.println("[DEBUG] pw contains " + cqUserPw.length +
//                           " characters");
		
	}


	/**
     * Parses and verifies the command line arguments.
     * 
     * @param argv              The command line arguments.
     * @throws IcofException 
     * @throws Exception 
     */
    private void setPatch(EdaContext xContext) throws IcofException {

    	try {

    		// Prepare the XML file
    		String sQuery = TkInjectUtils.getQueryString(getCqUserId(), 
    		                                             getCqUserPw(),
    		                                             getPatchName());

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

    		Vector objects = xCqFetchOutput.getResultObjects();
    		Vector labels = xCqFetchOutput.getFieldLabels();

    		patch = new TkPatchBase(labels, objects);

    	}
    	catch(Exception trap) {
    		throw new IcofException(APP_NAME, "setPatch()", IcofException.SEVERE,
    		                        "Unable to query ClearQuest for patch data.\n",
    		                        trap.getMessage());
    	}
        
    }
    
    
    /**
     * Display the Change Requests
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void showChangeRequests(EdaContext xContext) throws IcofException {

    	 Iterator<String> iter = getCrEntries().iterator();
         while (iter.hasNext()) {
             String entry =  iter.next();
             System.out.println(entry);
         }

        if (! isQuietMode()) {
        	if (getPatch().getState().toUpperCase().equals("TRANSMITTED")) {
        		System.out.println("\nPatch has already been Transmitted!");
        	}
        	else {
        		System.out.print("\nPatch is Transmit Ready: ");
        		if (getPatch().isTransmitReady()) {
        			System.out.println("YES");
        		}
        		else {
        			System.out.println("NO");
        		}
        	}
        }
        
	}


	protected String readParams(Hashtable<String,String> params, 
	                            String errors, EdaContext xContext) 
	throws IcofException {
		// Read the TK Patch
        if (params.containsKey("-p")) {
            setPatchName( params.get("-p"));
        }
        else {
            errors += "TK Patch (-p) is a required parameter\n";
        }

        // Read the optional CQ user id
        if (params.containsKey("-u")) {
            setCqUserId( params.get("-u"));
        }

        // Read the optional CQ user pw
        if (params.containsKey("-pw")) {
            setCqUserPw(params.get("-pw"));
        }
        
        // Read the optional New State
        if (params.containsKey("-newstate")) {
            setNewState(xContext, (String)params.get("-newstate"));
        }
        
        // Read the optional quiet mode switch
        setQuietMode(false);
        if (params.containsKey("-q")) {
            setQuietMode(true);
        }
		return errors;
	}


	protected void createSwitches(Vector<String>  singleSwitches, Vector<String>  argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        singleSwitches.add("-q");
        argSwitches.add("-db");
        argSwitches.add("-p");
        argSwitches.add("-u");
        argSwitches.add("-pw");
        argSwitches.add("-newstate");
        
	}


	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App           : " + APP_NAME + "  " + APP_VERSION, verboseInd);
		logInfo(xContext, "TK Patch      : " + getPatchName(), verboseInd);
		if (getCqUserId() != null)
			logInfo(xContext, "CQ user id    : " + getCqUserId(), verboseInd);
		logInfo(xContext, "Quiet Mode    : " + isQuietMode(), verboseInd);
		if (newState != null) {
			logInfo(xContext, "New State     : " + getNewState().getName(), verboseInd);
		}
		logInfo(xContext, "DB Mode       : " + dbMode, verboseInd);
		logInfo(xContext, "Verbose       : " + getVerboseInd(xContext), verboseInd);
	}

    
    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

        StringBuffer usage = new StringBuffer();
        usage.append("------------------------------------------------------\n");
        usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
        usage.append("------------------------------------------------------\n");
        usage.append("Look up Change Requests for the specified TK Patch.\n");
        usage.append("If -newstate is specified then all Change Requests \n");
        usage.append("associated with this patch will have their state changed\n");
        usage.append("to the new state (used for xtinct patches).\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-p patch> [-q] [-newstate state] [-y]\n");
        usage.append("           [-u cq_id] [-pw cq_pw] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  patch   = ClearQuest patch number (MDCMS######### ...).\n");
        usage.append("  state   = An Change Request state (transmitted, complete ...).\n");
        usage.append("  cq_id   = (optional) User's CQ user id\n");
        usage.append("  cq_pw   = (optional) User's CQ password\n");
        usage.append("  -y      = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  dbMode  = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("  -h      = Help (shows this information)\n");
        usage.append("  -q      = Quiet mode - show CR names only\n");
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
    private String  patchName;
    private TkPatchBase patch;
    private String  cqUserId;
    private char[]  cqUserPw = null;
    private int maxRevision = 0;
    private Vector<String> crEntries;
    private boolean quiet = false;
    private ChangeRequestStatus newState;

    
    /**
     * Getters.
     */
    public String getPatchName()  { return patchName; }
    public TkPatchBase getPatch()  { return patch; }
    public String getCqUserId()  { return cqUserId; }
    public String getCqUserPw() {
    	if (cqUserPw == null) {
    		return null;
    	}
    	return String.valueOf(cqUserPw); 
    }
    public int getMaxRevision()  { return maxRevision; }
    public Vector<String> getCrEntries()  { return crEntries; }
    public boolean isQuietMode()  { return quiet; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
    public ChangeRequestStatus getNewState() { return newState; }
    
        
    /**
     * Setters.
     */
    private void setPatchName(String aPatch) { patchName = aPatch; }
    private void setCqUserId(String anId) { cqUserId = anId; }
    private void setCqUserPw(String aPw) { 
    	if (aPw != null) {
    		cqUserPw = aPw.toCharArray(); 
    	}
    }

    private void setMaxRevision(int aRev) { maxRevision = aRev; }
    private void setQuietMode(boolean aFlag) { quiet = aFlag; }

    
    /**
     * Set the ChangeRequestStatus_Db object from the status name
     * @param xContext  Application context.
     * @throws IcofException 
     */
    private void setNewState(EdaContext xContext, String aName) 
    throws IcofException { 
    	try {
    		if (getNewState() == null) {
    			newState = new ChangeRequestStatus(xContext, aName.trim().toUpperCase());
    			newState.dbLookupByName(xContext);
    		}    
    		logInfo(xContext, "New State: " + getNewState().toString(xContext),
    		        verboseInd);
    	}
    	catch(IcofException ex) {
    		logInfo(xContext, 
    		        "New State (" + aName + ") was not found in the database.", 
    		        true);
    		throw ex;
    	}
    }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}
        
}
