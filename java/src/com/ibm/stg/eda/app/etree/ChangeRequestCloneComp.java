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
 * Clone an existing ChangeRequest to a new Component. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 09/30/2011 GFS  Initial coding.
 * 10/16/2012 GFS  Updated to show the new CR.
 * 12/06/2012 GFS  Added support for new impacted customer field.
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
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestRelationship;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestCloneComp extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.cloneComp";
    public static final String APP_VERSION = "v1.0";
    public static final String RELATIONSHIP = "Cloned Component";


    /**
     * Constructor
     *
     * @param     aContext  Application context
     * @param     aCr       ChangeRequest object to clone
     * @param     aComp     Component to associate with cloned CR
     */
    public ChangeRequestCloneComp(EdaContext aContext, ChangeRequest aCr,
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
    public ChangeRequestCloneComp(EdaContext aContext) throws IcofException {

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

	    myApp = new ChangeRequestCloneComp(null);
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
	cloneIt(xContext);
	commitToDB(xContext, APP_NAME);
    }


    /**
     * Create the new (cloned) ChangeRequest from the original CR.
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void cloneIt(EdaContext xContext) throws IcofException {

	// Determine the Component from the user's input
	getChangeRequest().setCompVersions(xContext);
	if (! getChangeRequest().getCompVersions().firstElement().isLoaded())
	    getChangeRequest().getCompVersions().firstElement().dbLookupById(xContext);
	if (! getChangeRequest().getCompVersions().firstElement().getCompRelease().isLoaded())
	    getChangeRequest().getCompVersions().firstElement().getCompRelease().dbLookupById(xContext);

	if (getComponent() == null) {
	    component =  new Component(xContext, getChangeRequest().getCompVersions().firstElement().getCompRelease().getComponent());
	    getComponent().dbLookupById(xContext, 
	                                getComponent().getComponent().getId());
	}
	logInfo(xContext, "Clone's Component: " + getComponent().getComponent().toString(xContext), 
	        isVerbose(xContext));

	if (getComponent().getComponent().getId() == 
	getChangeRequest().getCompVersions().firstElement().getCompRelease().getComponent().getId()) {
	    IcofException ie = new IcofException(APP_NAME,  "cloneIt()",
	                                         IcofException.SEVERE,
	                                         "User specified Component is " +
	                                         "the same as the original " +
	                                         "Change Request's Component.",
	                                         "\nComponent: " + 
	                                         getComponent().getName());
	    throw ie;
	}


	// Determine the ToolKit from the original CR
	if (! getChangeRequest().getCompVersions().firstElement().getVersion().isLoaded())
	    getChangeRequest().getCompVersions().firstElement().getVersion().dbLookupById(xContext);
	toolKit = new ToolKit(xContext, getChangeRequest().getCompVersions().firstElement().getVersion());
	getToolKit().dbLookupById(xContext, getToolKit().getToolKit().getId());
	logInfo(xContext, "New ToolKit: " + getToolKit().getToolKit().toString(xContext), 
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
	createApp.create(xContext);
	logInfo(xContext, "New Change Request: " + 
	createApp.getClearQuest(), true);

	// Create the relationship
	ChangeRequestRelationship crRelate = 
	new ChangeRequestRelationship(xContext, getChangeRequest(),
	                              createApp.getMyChangeRequest(),
	                              RELATIONSHIP);
	try {
	    crRelate.dbAdd(xContext);
	}
	catch (IcofException e) {
	    logInfo(xContext, "Unable to clone this Change Request ...", true);
	}

	// Set the return code
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
	    setChangeRequest(xContext,  params.get("-cr"));
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
	usage.append("Clones an existing Change Request into a new CR for a \n");
	usage.append("different Component. The original and new CRs are associated\n");
	usage.append("with each other which can be useful to determine if changes\n");
	usage.append("made in 1 component were also made in the coreq component(s).\n");
	usage.append("\n");
	usage.append("If you want to update the severity, type or description\n");
	usage.append("then run cr.update on the cloned ChangeRequest\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-cr ChangeRequest> <-c component>\n");
	usage.append("             [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  ChangeRequest = The existing ChangeRequest id (MDCMS######### ...)\n");
	usage.append("  component     = Clone's Component name (ess, pds, model, einstimer ...).\n");
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
     * Getters.
     */
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles( EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }


}
