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
 * Displays a ChangeRequest's release note data. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/05/2011 GFS  Initial coding.
 * 08/08/2011 GFS  Changed delimiter around developer name. Updated to display 
 *                 plain release notes and release notes formatted for PI&R.
 * 09/07/2011 GFS  Added support for Change Request type and severity. 
 * 09/09/2011 GFS  Updated so help is shown if no parm specified.
 * 09/16/2011 GFS  Change -cq switch to -cr.
 * 12/10/2012 GFS  Updated to support new impacted customer data.
 * 03/04/2014 GFS  Updated to be usable/accessible by other applications
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkConstants;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestShowRelNotes extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "cr.relNotes";
    public static final String APP_VERSION = "v1.3";
    public static final String O_DELIM_OPEN = "[[[";
    public static final String O_DELIM_CLOSE = "]]]";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aCr      ChangeRequest object
     * @param bForPir  If true add additional PIR data otherwise description only
     */
    public ChangeRequestShowRelNotes(EdaContext xContext, ChangeRequest aCr, 
                                     boolean bForPir)
    throws IcofException {

	super(xContext, APP_NAME, APP_VERSION);

	setChangeRequest(aCr);
	setForPIR(bForPir);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * 
     * @exception IcofException Unable to construct ManageApplications object
     */
    public ChangeRequestShowRelNotes(EdaContext aContext) throws IcofException {

	this(aContext, null, false);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {

	    myApp = new ChangeRequestShowRelNotes(null);
	    start(myApp, argv);
	}

	catch (Exception e) {
	    handleExceptionInMain(e);
	}
	finally {
	    handleInFinallyBlock(myApp);
	}

    }


    // --------------------------------------------------------------------------
    /**
     * Add, update, delete, or report on the specified applications.
     * 
     * @param aContext Application Context
     * @throws IcofException
     */
    // --------------------------------------------------------------------------
    public void process(EdaContext xContext)
    throws IcofException {

	// Connect to the database
	connectToDB(xContext);

	// Display the release note data to stdout
	setRelNotes(xContext);
	showRelNotes(xContext);
	
	// Set the return code
	setReturnCode();
	rollBackDB(xContext, APP_NAME);

    }


    /**
     * Display the release notes
     *
     * @param xContext
     */
    public void showRelNotes(EdaContext xContext) {
	
	System.out.println(getRelNotes().toString());
	
    }


    /**
     * Set the return code based on the ChangeRequest type 0 = defect, 1 =
     * feature and 2 = no rel note data
     */
    private void setReturnCode() {

	if (getChangeRequest() == null) {
	    setReturnCode(null, TkConstants.NOTHING_TO_DO);
	}
	else if (isDefect()) {
	    setReturnCode(null, 0);
	}
	else {
	    setReturnCode(null, 1);
	}

    }


    /**
     * Display the release note data to stdout.
     * 
     * @param xContext
     * @throws IcofException
     */
    public void setRelNotes(EdaContext xContext)
    throws IcofException {

	// Skip if ChangeRequest was not found
	if (getChangeRequest() == null)
	    return;

	// Display release notes only if not for PI&R
	relNotes = new StringBuffer();
	if (! isForPIR()) {

	    relNotes.append(getChangeRequest().getDescription());

	}
	else {

	    // Output formatted for PI&R
	    relNotes.append(getChangeRequest().getClearQuest());
	    relNotes.append(getTypeText(xContext) + "\n");
	    //relNotes.append(getImpactStatement(xContext));
	    relNotes.append(getChangeRequest().getDescription());
//	    relNotes.append(" " + 
//			   O_DELIM_OPEN + 
//			   getChangeRequest().getChangeRequest().getCreatedBy() + 
//			   O_DELIM_CLOSE);

	}

    }


    /**
     * Generate a printable type (d or f) for the CR type
     * 
     * @param xContext Application context
     * @return "  (type)  "
     * @throws IcofException
     */
    private String getTypeText(EdaContext xContext)
    throws IcofException {

	getChangeRequest().getType().getDbObject().dbLookupById(xContext);
	String type = getChangeRequest().getType().getDbObject().getName();
	String letter = type.substring(0, 1);

	return "  (" + letter.toLowerCase() + ")  ";

    }


    /**
     * Generate the impacted customer
     * 
     * @param xContext Application context
     * @return "  Impacted: Server Only  "
     * @throws IcofException
     */
    private String getImpactStatement(EdaContext xContext)
    throws IcofException {

	String reply = "";
	if (getChangeRequest().getImpactedCustomer() != null) {
	    String cust = getChangeRequest().getImpactedCustomer();
	    if (cust.indexOf("Server Only") > -1) {
		cust += " (Not ASICs)";
	    }
	    reply = "Impacts: " + cust + "\n";
	}

	return reply;

    }


    protected String readParams(Hashtable<String, String> params,
				String errors, EdaContext xContext)
    throws IcofException {

	// Read the ChangeRequest name
	if (params.containsKey("-cr")) {
	    setChangeRequest(xContext, params.get("-cr"));
	}
	else if (params.containsKey("-cq")) {
	    setChangeRequest(xContext, params.get("-cq"));
	}
	else {
	    errors += "ChangeRequest (-cr) is a required parameter\n";
	}

	// Read the PIR switch
	if (params.containsKey("-pir")) {
	    setForPIR(true);
	}

	return errors;

    }


    protected void createSwitches(Vector<String> singleSwitches,
				  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	singleSwitches.add("-pir");
	argSwitches.add("-db");
	argSwitches.add("-cq");
	argSwitches.add("-cr");
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {

	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	if (getChangeRequest() != null)
	    logInfo(xContext, "ChangeRequest : "
			      + getChangeRequest().getClearQuest(), verboseInd);
	else
	    logInfo(xContext, "ChangeRequest : null", verboseInd);
	logInfo(xContext, "PIR Mode   : " + isForPIR(), verboseInd);
	logInfo(xContext, "DB Mode    : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose    : " + getVerboseInd(xContext), verboseInd);

    }


    /**
     * Display this application's usage and invocation
     */
    public void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Displays this ChangeRequest's release note data.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME
		     + " <-cr ChangeRequest> [-y] [-h] [-pir] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  ChangeRequest = A ChangeRequest id (MDCMS######### ...).\n");
	usage.append("  -y            = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  -pir          = (optional) Format output for PI&R process\n");
	usage.append("  dbMode        = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h            = Help (shows this information)\n");
	usage.append("\n");
	usage.append("Return Codes\n");
	usage.append("------------\n");
	usage.append(" 0 = ok (defect)\n");
	usage.append(" 1 = ok (feature)\n");
	usage.append(" 2 = ok (no results/CR not found)\n");
	usage.append(" 8 = error\n");
	usage.append("\n");

	System.out.println(usage);

    }


    /**
     * Members.
     * @formatter:off
     */
    private boolean defect = false;
    private boolean forPIR = false;
    private StringBuffer relNotes;

    
    /**
     * Getters.
     */
    public boolean isDefect() { return defect; }
    public boolean isForPIR() { return forPIR; }
    public StringBuffer getRelNotes() { return relNotes; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}

        
    /**
     * Setters.
     */
    private void setForPIR(boolean aFlag) { forPIR = aFlag; }
    // @formtter:on
    
    
	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}
    
}

