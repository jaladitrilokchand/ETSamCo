/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2014 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *
 *-PURPOSE---------------------------------------------------------------------
 * Update a Tool Kit Package a given state. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 01/13/2014 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree.packaging;

import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequests;
import com.ibm.stg.eda.component.tk_etreeobjs.ComponentPackage;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKitPackage;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class TkPackageAddChangeReqs extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "pkg.addCrs";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aPkg Tool k package to update
     * @param anEvent New EventName (state)
     */
    public TkPackageAddChangeReqs(EdaContext aContext, ToolKitPackage aPkg,
                                  ChangeRequests changeReqs)
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setTkPackage(aPkg);
	setChangeReqs(changeReqs);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * @exception IcofException Unable to construct ManageApplications object
     */
    public TkPackageAddChangeReqs(EdaContext aContext) throws IcofException {

	this(aContext, null, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new TkPackageAddChangeReqs(null);
	    start(myApp, argv);
	}

	catch (Exception e) {
	    handleExceptionInMain(e);
	}
	finally {
	    handleInFinallyBlock(myApp);
	}

    }


    /**
     * Add, update, delete, or report on the specified applications.
     * 
     * @param aContext Application Context
     * @throws IcofException
     */
    public void process(EdaContext xContext)
    throws IcofException {

	connectToDB(xContext);

	updatePackage(xContext);

	commitToDB(xContext, APP_NAME);

    }


    /**
     * Display data for each access request
     * 
     * @param xContext
     * @throws IcofException
     */
    private void updatePackage(EdaContext xContext)
    throws IcofException {
	
	for (ComponentPackage compPkg : getTkPackage().getComponentPackages(xContext)) {
	    String[] tokens = compPkg.getName().split("[.]");
	    String compPkgCompName = tokens[0];
	    if (getComponent().getName().equals(compPkgCompName)) {
		logInfo(xContext, 
		        "Updating DB .. " + compPkg.getName() + " <--> " + getChangeReqText(), 
		        true);
		compPkg.addChangeRequests(xContext, getChangeReqs(), getUser());
	    }
	}
    }	



    /**
     * Parse command line args
     * 
     * @param params Collection of command line args/switches
     * @param errors String to store any error messages
     * @param xContext Application context object
     */
    protected String readParams(Hashtable<String, String> params,
				String errors, EdaContext xContext)
    throws IcofException {

	// Read the Request name
	if (params.containsKey("-t")) {
	    String pkgName = (String) params.get("-t");
	    if (IcofStringUtil.occurrencesOf(pkgName, ".") != 3) {
		errors += "Tool kit maintenance name must be in " +
				"x.y.z.a (14.1.6.0) format.\n";
	    }
	    else {
		setToolKitPackage(xContext, (String) params.get("-t"));
	    }
	}
	else
	    errors += "Tool kit maintenance name (-t) is a required parameter\n";

	// Read the comments
	if (params.containsKey("-cr"))
	    setChangeReqs(xContext, (String) params.get("-cr"));
	else
	    errors += "Change Reqests (-cr) is a required parameter\n";

	
	// Read the component
	if (params.containsKey("-c"))
	    setComponent(xContext, (String) params.get("-c"));
	else	
	    errors += "Component name (-c) is a required parameter\n";


	validateComps(xContext);
	
	return errors;

    }


    /**
     * Verify the CR and given component are the same
     *
     * @param xContext
     * @throws IcofException 
     */
    private void validateComps(EdaContext xContext) throws IcofException {
	
	for (ChangeRequest cr : getChangeReqs().getChangeRequests().values()) {
	    logInfo(xContext, "CHANGE REQ: " + cr.getClearQuest(), true);
	    cr.setComponent(xContext);
	    logInfo(xContext, cr.toString(xContext), true);
	    logInfo(xContext, "CR comp: " + cr.getComponent().getName(), true);
	    logInfo(xContext, "Comp   : " + getComponent().getName(), true);
	    if (! cr.getComponent().getName().equals(getComponent().getName()))
		throw new IcofException(this.getClass().getName(), 
		                        "setChangeReqs()", 
		                        IcofException.SEVERE, 
		                        "Change Request(" + cr.getClearQuest() + 
		                        ") is not associated with " +
		                        "the given component(" + 
		                        getComponent().getName() + ")", 
		                        "ChangeReq Comp: " + 
		                        cr.getComponent().getName());
	}
	
    }


    /**
     * Create a collection of ChangeRequest objects from the comma delimited
     * String of CR numbers
     *
     * @param xContext
     * @param input
     * @throws Exception 
     */
    private void setChangeReqs(EdaContext xContext, String input) 
    throws IcofException {
	
	setChangeReqText(input);

	try {
	    changeReqs = new ChangeRequests(getToolKit(), getComponent());
	    changeReqs.setChangeRequests(xContext, input, "", false, 
	                                 false, getVerboseInd(xContext));
	}
	catch(Exception trap) {
	    throw new IcofException(this.getClass().getName(), "setChangeReqs()",
	                            IcofException.SEVERE, 
	                            trap.getMessage(),
	                            "CR list: " + input);
	}
	
    }


    /**
     * Define application's command line switches
     * 
     * @param singleSwitches Collection of switches
     * @param argSwitches Collection switches/args
     */
    protected void createSwitches(Vector<String> singleSwitches,
				  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	singleSwitches.add("-q");
	argSwitches.add("-db");
	argSwitches.add("-t");
	argSwitches.add("-cr");
	argSwitches.add("-c");
    }	


    /**
     * Display application's invocation
     * 
     * @param dbMode Database model
     * @param xContext Application context object
     */
    protected void displayParameters(String dbMode, EdaContext xContext) {

	logInfo(xContext, "App          : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	logInfo(xContext, "TK Package ID: " + getTkPackage().getDbObject().getId(),
		verboseInd);
	logInfo(xContext, "Change reqs  : " + getChangeReqs().toString(), verboseInd);
	logInfo(xContext, "DB Mode      : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose      : " + getVerboseInd(xContext), verboseInd);
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Add the Change Requests to the given Tool Kit patch. \n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t toolkit> <-c component> <-cr change_reqs>\n");
	usage.append("                [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  toolkit  = Tool kit name including maint (14.1.6.0 ...)\n");
	usage.append("  component   = Component name (hdp, einstimer, pds ...)\n");
	usage.append("  change_reqs = Comma delimited list of crs to add\n");
	usage.append("  -y       = (optional) Quiet mode (no putput headers)\n");
	usage.append("  -y       = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode   = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h       = Help (shows this information)\n");
	usage.append("\n");
	usage.append("Return Codes\n");
	usage.append("------------\n");
	usage.append(" 0 = ok \n");
	usage.append(" 1 = application errors\n");
	usage.append("\n");

	System.out.println(usage);

    }


    /**
     * Data members
     * @formatter:off
     */
    private ToolKitPackage pkg;
    private ChangeRequests changeReqs;
    private String changeReqText;


    /**
     * Getters
     */
    public ToolKitPackage getTkPackage() { return pkg; }
    public ChangeRequests getChangeReqs() { return changeReqs; }
    public String getChangeReqText() { return changeReqText; }
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }


    /**
     * Setters
     */
    private void setTkPackage(ToolKitPackage aPkg) { pkg = aPkg; }
    private void setChangeReqs(ChangeRequests crs) { changeReqs = crs; }
    private void setChangeReqText(String aText) { changeReqText = aText; }


    // @formatter:on

    
     /**
     * Set the TK package object
     * 
     * @param xContext Application context.
     * @param anId Request id
     * @throws IcofException
     */
    protected void setToolKitPackage(EdaContext xContext, String aName)
    throws IcofException {

	// Strip off the tool kit name and lookup the tool kit
	String tkName = aName.substring(0, aName.lastIndexOf("."));
	setToolKit(xContext, tkName);
	
	logInfo(xContext,
		"Tool Kit: " + getToolKit().toString(xContext),
		false);
	
	// Set the maintenance
	String maintName = aName.substring(aName.lastIndexOf(".") + 1);
	
	if (getTkPackage() == null) {
	    pkg = new ToolKitPackage(xContext, maintName);
	    getTkPackage().dbLookupByName(xContext, getToolKit());
	}
	logInfo(xContext,
		"Tool Kit Package: " + getTkPackage().toString(xContext),
		false);
    }



    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {

	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }

}
