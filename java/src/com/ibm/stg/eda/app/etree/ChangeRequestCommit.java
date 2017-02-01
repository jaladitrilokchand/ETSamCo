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
 * Commits changes against an active or specified CR. 
 *-----------------------------------------------------------------------------
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 07/28/2011 GFS  Initial coding.
 * 09/06/2011 GFS  Added support for Change Request type and severity.
 * 09/09/2011 GFS  Updated so help is shown if no parm specified.
 * 09/16/2011 GFS  Change -cq switch to -cr. Changed -active to -default.
 *                 Updated updateArgs() method to remove -active and -cr from
 *                 svn commit command.
 * 11/29/2012 GFS  Updated to support CR's new impacted customer attribute.                
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestStatus_Db;
import com.ibm.stg.eda.component.tk_etreedb.StageName_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Branch;
import com.ibm.stg.eda.component.tk_etreeobjs.BranchName;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.Subversion;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;

public class ChangeRequestCommit extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "cr.commit";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     *
     * @param     xContext  Application context
     * @param     aCqId     ChangeRequest's CQ number
     * @param     aActive   If true use the user's activate CR
     * @param     aUser     User committing the updates
     */
    public ChangeRequestCommit(EdaContext xContext, 
                               String aCqId, 
                               User_Db aUser)	
                               throws IcofException {

	super(xContext, APP_NAME, APP_VERSION);

	setChangeRequest(xContext, aCqId);
	setUser(aUser);

    }


    /**
     * Constructor
     *
     * @param     aContext  Application context
     * @param     aCr       ChangeRequest object
     * @param     aFlag     If true de-activate an existing active CR otherwise
     *                      throw an error if an active CR already exists.
     */
    public ChangeRequestCommit(EdaContext aContext, ChangeRequest aCr, 
                               boolean aFlag)	
                               throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setChangeRequest(aCr);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ChangeRequestCommit(EdaContext aContext) throws IcofException {

	this(aContext, (ChangeRequest)null, false);

    }


    /**
     * Instantiate this class and process the arguments.
     * 
     * @param argv
     *            [] Command line arguments
     */
    public static void main(String argv[]) {

	ChangeRequestCommit myApp = null;
	EdaContext aContext = null;

	try {

	    myApp = new ChangeRequestCommit(null);

	    // Read and verify input parameters and get a database connection.
	    aContext = myApp.initializeApp(argv,  null,  null, null);
	    if (getRequestHelp()) {
		System.exit(SUCCESS);
	    }
	    setArgs(argv);
	    myApp.process(aContext);
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
     * @param aContext      Application Context
     * @throws              IcofException
     */
    public void process(EdaContext xContext) throws IcofException {

	// Connect to the database
	connectToDB(xContext);

	// Commit changes and set return code only if successful.
	commit(xContext);
	commitToDB(xContext, APP_NAME);


    }


    /**
     * Make this ChangeRequest the active CR.
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void commit(EdaContext xContext) throws IcofException {

	// Read the SVN info - set Component and Branch names
	svn = new Subversion(xContext);
	boolean validSvn = getSvn().svnInfo(xContext, getPwd());
	if (! validSvn) {
	    alert("Unable to determine SVN information ...\n" + 
	    "Please re-run this command in a SVN working copy.", true);
	    return;
	}

	logInfo(xContext, "URL        : " + getSvn().getUrl(), verboseInd);
	logInfo(xContext, "Component  : " + getSvn().getComponent(), verboseInd);
	if (getSvn().getTrunk() != null)
	    logInfo(xContext, "Trunk      : " + getSvn().getTrunk(), verboseInd);
	if (getSvn().getBranch() != null)
	    logInfo(xContext, "Branch     : " + getSvn().getBranch(), verboseInd);


	// Determine if CR is required for this Commit
	setComponent(xContext, getSvn().getComponent());
	setBranchName(xContext);
	setCrRequired(xContext);
	logInfo(xContext, "CR Required: " + isCrRequired(), verboseInd);

	// If CR is required then lookup or validate the specified CR and
	// add the CR data to the message.
	if (isCrRequired()) {

	    // Lookup/get CR from database
	    setBranchToolKits(xContext);
	    lookupChangeRequest(xContext);

	    // Verify this ChangeRequest has been APPROVED

	    if (! validateChangeRequest(xContext))
		return;

	    // Update the commit message
	    updateMessage(xContext);

	}    	

	// Commit the updates
	if (commitUpdates(xContext)) {
	    alert("Commit complete", false);
	    setReturnCode(xContext, SUCCESS);
	}
	else 
	    alert("", true);

    }


    /**
     * Update the commit message to contain the CR record.
     * 
     * @param xContext Application context
     * @throws IcofException 
     */
    private void updateMessage(EdaContext xContext) throws IcofException {

	// Update contents of log message - text
	if (getMessageText() != null) {

	    String newText = getChangeRequest().getClearQuest() + ": ";
	    newText += getMessageText();
	    setMessageText(newText);

	}

	// Update contents of log message - file
	else if (getMessageFile() != null) {

	    try {

		// Read the specified input message file
		IcofFile log = new IcofFile(getMessageFile(), false);
		log.openRead();
		log.read();
		log.closeRead();
		String contents = 
		IcofCollectionsUtil.getVectorAsString(log.getContents(), "\n");
		logInfo(xContext, "Input file read ...", verboseInd);
		logInfo(xContext, " --> " + contents, verboseInd);

		// Create temp file and delete temp file when program exits. 
		tempFile = File.createTempFile(APP_NAME + "_", null);
		tempFile.deleteOnExit();
		logInfo(xContext, "Temp file created ... " + 
		tempFile.getAbsolutePath(), verboseInd);

		// Write to temp file
		BufferedWriter out = new BufferedWriter(new FileWriter(tempFile));
		out.write(getChangeRequest().getClearQuest() + ": " + contents);
		out.close();
		logInfo(xContext, "Temp file updated ... ", verboseInd);

	    }
	    catch (IOException ex) {
		alert("There was a problem creating/writing to the temp file", true);
		ex.printStackTrace();
	    }
	}
	else {

	    alert("When adding your change description via the editor \n" +
	    "please prefix your description with " +
	    getChangeRequest().getClearQuest() + ": .\n" +
	    "Otherwise the SVN commit hook will not detect your \n" +
	    "ChangeRequest record.  Thank you.\n\n" +
	    "For example, \"" + getChangeRequest().getClearQuest() + 
	    ": my change description ...\"\n", false);

	}

    }


    /**
     * Run svn commit
     * @param xContext  Application context
     * @throws IcofException 
     */
    private boolean commitUpdates(EdaContext xContext) throws IcofException {

	// Re-contruct the args list- replace -m or -F
	String args = updateArgs(xContext);

	boolean success = getSvn().svnCommit(xContext, args);

	return success;

    }


    /**
     * Re-build the arg list replacing parms as necessary
     * @param xContext  Application context
     * @return
     */
    private String updateArgs(EdaContext xContext) {

	String argList = "";
	Iterator<String> iter = getArgs().iterator();

	while (iter.hasNext()) {
	    String arg =  iter.next();
	    if (arg.equals("-m") || arg.equals("--message")) {
		argList += " " + arg + " \"" + getMessageText() + "\"";
		if (iter.hasNext())
		    iter.next(); 
	    }
	    else if (arg.equals("-F") || arg.equals("--file")) {
		if (isCrRequired()) 
		    argList += " " + arg + " " + getTempFile().getAbsolutePath();
		else 
		    argList += " " + arg + " " + getMessageFile();
		if (iter.hasNext())
		    iter.next(); 
	    }
	    else if (arg.equals("-default")) {
		// Don't add to arg list
	    }
	    else if (arg.equals("-cq") || arg.equals("-cr")) {
		// Don't add to arg list
		if (iter.hasNext()) 
		    iter.next();
	    }
	    else if (arg.equals("-y")) {
		// Don't add to arg list
	    }
	    else if (arg.equals("-db")) {
		// Don't add to arg list
		if (iter.hasNext()) 
		    iter.next();
	    }
	    else {
		argList += " " + arg;
	    }

	}

	return argList;

    }

    /**
     * Vaidate the change request are correct for this TK
     * 
     * @param xContext Application context
     * @throws IcofException 
     */
    private boolean validateChangeRequest(EdaContext xContext) 
    throws IcofException {

	boolean bReply = false;
	if (getChangeRequest().getClearQuest().equalsIgnoreCase("DEV"))
	    bReply = validateDevChangeRequest(xContext);
	else
	    bReply = validateProdChangeRequest(xContext);

	return bReply;

    }


    /**
     * Verify the DEV change is correct for this development branch/trunk
     *
     * @param xContext
     * @return
     */
    private boolean validateDevChangeRequest(EdaContext xContext) {

	boolean bMatch = true;
	for (ToolKit tk : getBranchToolKits()) {
	    if (! tk.getStageName().equals(StageName_Db.STAGE_DEV)) {
		bMatch = false;
		alert("Changes to this branch/trunk (" + getBranchName().getName() + 
		      ") must be made to a DEVELOPMENT tool kit.\n" +
		      "However Tool Kit (" + tk.getName() +
		      ") is associated with this branch/trunk and is in the " + 
		      tk.getStageName().getName() + "stage.", true);
	    }

	}

	return bMatch;

    }


    /**
     * Verify this Change Request is in the APPROVED state
     * @param xContext  Application context
     * @return          True if CR is valid otherwise false.
     * @throws IcofException
     */
    private boolean validateProdChangeRequest(EdaContext xContext) 
    throws IcofException {

	// Verify Change Request is APPROVED
	getChangeRequest().getStatus().dbLookupById(xContext);
	getChangeRequest().getStatus().getStatus().dbLookupById(xContext);
	String currentStatus = changeRequest.getStatus().getStatus().getName();

	if (! currentStatus.equals(ChangeRequestStatus_Db.STATUS_APPROVED)) {
	    alert("This Change Request in not in the APPROVED state.", true);
	    return false;
	}
	logInfo(xContext, "ChangeRequest is " + ChangeRequestStatus_Db.STATUS_APPROVED,
	        verboseInd);


	Branch branch = new Branch(xContext, getBranchName());
	boolean isValid = getChangeRequest().isValidBranch(xContext, branch,
	                                                   getComponent());
	if (! isValid) {
	    alert("This ChangeRequest is associated with a different\n" +
	    "ToolKit and/or Component than this SVN working copy.", true);
	    return false;
	}

	logInfo(xContext, "ChangeRequest and working copy check passed",
	        verboseInd);

	return true;

    }


    /**
     * Lookup the specified CR in the database
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void lookupChangeRequest(EdaContext xContext) throws IcofException {

	setChangeRequest(xContext, getCrName());
	logInfo(xContext, "Change Request: " + getChangeRequest().getClearQuest(),
	        verboseInd);

    }


    /**
     * Determine if Change Management is enabled on this Component and Branch
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void setCrRequired(EdaContext xContext) throws IcofException {

	BranchCheck checker = new BranchCheck(xContext, getComponent(),
	                                      getBranchName());
	crRequired = checker.checkBranch(xContext, false);

    }


    protected void createSwitches(Vector<String> singleSwitches, Vector<String> argSwitches) {
	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-cr");
	argSwitches.add("-m");
	argSwitches.add("--message");
	argSwitches.add("-F");
	argSwitches.add("--file");
	argSwitches.add("-cl");
	argSwitches.add("--changelist");
    }


    protected String readParams(Hashtable<String,String> params, String errors, EdaContext xContext) {

	// Read the ClearQuest name
	if (params.containsKey("-cr")) {
	    setCrName((String) params.get("-cr"));
	}

	// Read the message text
	if (params.containsKey("-m")) {
	    setMessageText((String) params.get("-m"));
	}
	if (params.containsKey("--message")) {
	    setMessageText((String) params.get("--message"));
	}

	// Read the message file
	if (params.containsKey("-F")) {
	    setMessageFile((String) params.get("-F"));
	}
	if (params.containsKey("--file")) {
	    setMessageFile((String) params.get("--file"));
	}

	// Read the change list
	if (params.containsKey("-cl")) {
	    setChangeList((String) params.get("-cl"));
	}
	if (params.containsKey("--changelist")) {
	    setChangeList((String) params.get("--changelist"));
	}

	// Set the current working directory
	setPwd(System.getProperty("user.dir"));


	// Verify only 1 required parameter was set.
	if (getCrName() == null) {
	    errors += "Please specify ChangeRequest id (-cr)!\n";        	
	}

	return errors;

    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION, verboseInd);
	if (getChangeRequest() != null) 
	    logInfo(xContext, "ChangeRequest : " + getChangeRequest().getClearQuest(), verboseInd);
	else 
	    logInfo(xContext, "ChangeRequest : null", verboseInd);
	logInfo(xContext, "PWD        : " + getPwd(), verboseInd);
	logInfo(xContext, "DB Mode    : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose    : " + getVerboseInd(xContext), verboseInd);
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Commits updates to SVN using a Default or the specified ChangeRequest.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-cr ChangeRequest | -default> [all svn commit switches]\n");
	usage.append("          [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  ChangeRequest = A Change Request id (MDCMS######### ...).\n");
	usage.append("  -default      = Uses your default ChangeRequest\n");
	usage.append("  -y            = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode        = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h            = Help (shows this information)\n");
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
    private boolean crRequired;
    private String crName;
    private String messageText;
    private String messageFile;
    private String changeList;
    private String pwd;
    private static Vector<String> myArgs;
    private Subversion svn;
    private File tempFile;
    private BranchName branchName;
    private Vector<ToolKit> branchToolKits;



    /**
     * Getters.
     */
    public boolean isCrRequired()  { return crRequired; }
    public String useCrName()  { return crName; }
    public String getMessageText()  { return messageText; }
    public String getMessageFile()  { return messageFile; }
    public String getChangeList()  { return changeList; }
    public String getCrName()  { return crName; }
    public String getPwd()  { return pwd; }
    public Vector<String> getArgs()  { return myArgs; }
    public Subversion getSvn()  { return svn; }
    public File getTempFile()  { return tempFile; }


    public BranchName getBranchName()  { return branchName; }
    public Vector<ToolKit> getBranchToolKits()  { return branchToolKits; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


    /**
     * Setters.
     */
    private void setCrName(String aName) { crName = aName; }
    private void setMessageText(String aStr) { messageText = aStr; }
    private void setMessageFile(String aStr) { messageFile = aStr; }
    private void setChangeList(String aStr) { changeList = aStr; }
    private void setPwd(String aDirName) { pwd = aDirName; }


    /**
     * Set the BranchName object
     * @param xContext   Application context.
     * @throws IcofException 
     */
    private void setBranchName(EdaContext xContext) 
    throws IcofException { 
	if (getBranchName() == null) {

	    // Determine the branch name
	    String name = getSvn().getBranch();
	    if (getSvn().getBranch() == null) 
		name = getSvn().getTrunk();

	    branchName = new BranchName(xContext, name);
	    branchName.dbLookupByName(xContext);

	}
	logInfo(xContext, "Branch name: " + getBranchName().toString(xContext),
	        false);
    }


    /**
     * Set the ToolKit from the Component and Branch objects
     * @param xContext   Application context.
     * @throws IcofException 
     */
    private void setBranchToolKits(EdaContext xContext) 
    throws IcofException { 
	if (getBranchToolKits() == null) {
	    Branch branch = new Branch(xContext, getBranchName());
	    branchToolKits = branch.findToolKits(xContext, getComponent());
	}    
	for (ToolKit tk : getBranchToolKits()) 
	    logInfo(xContext, "Tool Kit: " + tk.getName(), false);
    }


    /**
     * Save the command line args
     * @param argv
     */
    private static void setArgs(String[] argv) {

	myArgs = new Vector<String>();
	for (int i = 0; i < argv.length; i++) {
	    myArgs.add( argv[i]);
	}

    }

    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

}
