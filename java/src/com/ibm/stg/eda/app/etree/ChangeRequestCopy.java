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
 * Copy an existing ChangeRequest. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 12/01/2011 GFS  Initial coding.
 * 08/03/2012 GFS  Updated to work with the new 14.1.build generic TK version.
 *                 Removed support for -t.
 * 10/16/2012 GFS  Updated to show the new CR.
 * 11/29/2012 GFS  Updated to support CR's new impacted customer attribute.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestCopy extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.copy";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     *
     * @param     aContext  Application context
     * @param     aCr       ChangeRequest object to clone
     * @param     aComp     Component to associate with cloned CR
     */
    public ChangeRequestCopy(EdaContext aContext, ChangeRequest aCr,
                             Component aComp)	
                             throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);

	setComponent(aComp);
	setChangeRequest(aCr);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ChangeRequestCopy(EdaContext aContext) throws IcofException {

	this(aContext, null, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new ChangeRequestCopy(null);
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
     * @throws IcofException 
     * Add, update, delete, or report on the specified applications.
     * 
     * @param aContext      Application Context
     * @throws  
     */
    //--------------------------------------------------------------------------
    public void process(EdaContext xContext) throws IcofException   {

	// Connect to the database
	connectToDB(xContext);

	// Create the new ChangeRequest.
	copyIt(xContext);

	// Commit the changes
	commitToDB(xContext, APP_NAME);

    }


    /**
     * Create the new (cloned) ChangeRequest from the original CR.
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void copyIt(EdaContext xContext) throws IcofException {

	// Determine the ToolKit and Component from the original CR and user's
	// input
	getChangeRequest().setCompVersions(xContext);
	if (! getChangeRequest().getCompVersions().firstElement().isLoaded())
	    getChangeRequest().getCompVersions().firstElement().dbLookupById(xContext);
	if (! getChangeRequest().getCompVersions().firstElement().getCompRelease().isLoaded())
	    getChangeRequest().getCompVersions().firstElement().getCompRelease().dbLookupById(xContext);

	logInfo(xContext, "New Component: " + getComponent().getComponent().toString(xContext), 
	        isVerbose(xContext));

	// Fully populate the original CR
	if (! getChangeRequest().getStatus().getStatus().isLoaded())
	    getChangeRequest().getStatus().getStatus().dbLookupById(xContext);
	if (! getChangeRequest().getType().getDbObject().isLoaded())
	    getChangeRequest().getType().getDbObject().dbLookupById(xContext);
	if (! getChangeRequest().getSeverity().getDbObject().isLoaded())
	    getChangeRequest().getSeverity().getDbObject().dbLookupById(xContext);

	// Create the new CR
	ChangeRequestCreateBase createApp = 
	new ChangeRequestCreateBase(xContext, 
	                            getComponent(),
	                            getChangeRequest().getDescription(),
	                            "",
	                            getChangeRequest().getStatus(),
	                            getChangeRequest().getType(),
	                            getChangeRequest().getSeverity(),
	                            getUser(), 
	                            getVerboseInd(xContext),
	                            getToolKit(),
	                            getChangeRequest().getImpactedCustomer());
	try {
	    createApp.create(xContext);
	    logInfo(xContext, "New Change Request: " + 
	    createApp.getClearQuest(), true);
	}
	catch (IcofException e) {
	    logInfo(xContext, "Unable to copy this Change Request ...", true);
	    throw e;

	}

	// Set the return code
	logInfo(xContext, "Change Request copied", true);
	setReturnCode(xContext, SUCCESS);

    }


    protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-cr");
	argSwitches.add("-c");
    }


    protected String readParams(Hashtable<String,String> params, String errors,
                                EdaContext xContext) throws IcofException {

	// Read the Component name
	if (params.containsKey("-c")) {
	    setComponent(xContext,  params.get("-c"));
	}
	else {
	    errors += "Component (-c) is a required parameter\n";
	}

	// Read the ChangeRequest
	if (params.containsKey("-cr")) {
	    setChangeRequest(xContext, params.get("-cr"));
	}
	else {
	    errors += "ChangeRequest (-cr) is a required parameter\n";
	}

	return errors;

    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	logInfo(xContext, "App           : " + APP_NAME + "  " + APP_VERSION, 
	        getVerboseInd(xContext));
	logInfo(xContext, "ChangeRequest : " + getChangeRequest().getClearQuest(), 
	        getVerboseInd(xContext));
	if (getComponent() == null) 
	    logInfo(xContext, "Component     : null", getVerboseInd(xContext));
	else 
	    logInfo(xContext, "Component     : " + getComponent().getName(), getVerboseInd(xContext));
	logInfo(xContext, "DB Mode       : " + dbMode, getVerboseInd(xContext));
	logInfo(xContext, "Verbose       : " + getVerboseInd(xContext), 
	        getVerboseInd(xContext));
	logInfo(xContext, "", getVerboseInd(xContext));
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Copies an existing Change Request into a new Change\n");
	usage.append("Request with a different Component. The new CR is NOT\n");
	usage.append("associated with the original CR (use \n");
	usage.append(ChangeRequestCloneComp.APP_NAME + " if you want the CRs associated\n");
	usage.append("with each other)\n");
	usage.append("\n");
	usage.append("If you want to update the severity, type or description\n");
	usage.append("then run cr.update on the new ChangeRequest\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-cr ChangeRequest> <-c component >\n");
	usage.append("        [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  ChangeRequest = The existing ChangeRequest id (MDCMS######### ...)\n");
	usage.append("  component     = The copy's Component name (ess, pds, model, einstimer ...).\n");
	usage.append("  -y            = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode        = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h            = Help (shows this information)\n");
	usage.append("\n");
	usage.append("Return Codes\n");
	usage.append("------------\n");
	usage.append(" 0 = ok\n");
	usage.append(" 1 = error\n");

	System.out.println(usage);

    }

    /**
     * Members.
     */


    /**
     * Getters.
     */
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}



    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }
}
