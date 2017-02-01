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
 * Creates a Code Update based on data in the input file. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 03/21/2011 GFS  Initial coding.
 * 05/10/2011 GFS  Updated to create a FunctionalUpdate as well.  This change 
 *                 will be removed once Change Management is fully enabled.
 * 06/10/2011 GFS  Disabled logging.
 * 06/30/2011 GFS  Updated to support new description column.
 * 07/19/2011 GFS  Removed support for FunctionalUpdates since they are replaced
 *                 with ChangeRequests. Removed support for TOOL_KIT in the 
 *                 input file.
 * 08/15/2011 GFS  Updated setBranchName to lookup the branch name only if 
 *                 CM is required. Updated setToolKit to lookup the TK only
 *                 if CM is required.
 * 10/19/2011 GFS  Fixed a bug when single quote is found in the comments.
 * 11/23/2011 GFS  Updated setChangedFiles() to not add duplicate files to the
 *                 added/deleted files collections.  Update setChangedFiles()
 *                 to fix a bug where the branch is not set correctly. Updated
 *                 setMembers() to read new mark up language style abstract.
 * 06/13/2012 GFS  Fixed bugs in setToolKit() and setUser().
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkConstants;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.CompVersion_ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.FileActionName_Db;
import com.ibm.stg.eda.component.tk_etreedb.FileName_Db;
import com.ibm.stg.eda.component.tk_etreedb.FileVersion_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Branch;
import com.ibm.stg.eda.component.tk_etreeobjs.BranchName;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestSeverity;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestStatus;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestType;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class CreateCodeUpdate extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "createCodeUpdate";
    public static final String APP_VERSION = "v2.0";
    public static final String HOOK_VER = "HOOKREL";
    public static final String REPOSITORY = "REPOS";
    public static final String COMPONENT = "COMPONENT";
    public static final String CHANGE_REQUEST = "CHANGE_REQ";
    public static final String REVISION = "REVISION";
    public static final String ABSTRACT = "ABSTRACT";
    public static final String ABSTRACT_NEW_START = "<ABSTRACT>";
    public static final String ABSTRACT_NEW_END = "</ABSTRACT>";
    public static final String USER = "USER";
    public static final String COMMIT_DATE = "COMMIT_DATE";
    public static final String COMMIT_TIME = "COMMIT_TIME";
    public static final String CHANGED = "CHANGED";
    public static final String DB_ID = "DB_ID";
    public static final String DELIM = "|";
    public static final String N_A = "N/A";
    public static final String NEW_ABSTRACT_HOOKREL = "3.0";

    public static final String DATE_FORMAT_SVN = "yyyy-MM-dd";
    public static final String TIME_FORMAT_SVN = "hh:mm:ss";


    /**
     * Constructor
     * 
     * @param aContext the application context
     * @param aReleaseName the TK release name
     * @param aComponentName the TK component name
     * 
     */
    public CreateCodeUpdate(EdaContext aContext, IcofFile inputFile)
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setDataFile(inputFile);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * 
     * @exception IcofException Unable to construct ManageApplications object
     */
    public CreateCodeUpdate(EdaContext aContext) throws IcofException {

	this(aContext, null);
	
    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new CreateCodeUpdate(null);
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

	// Connect to the database
	connectToDB(xContext);

	// Parse the data file and set the data members
	logInfo(xContext, "Reading input file ...", verboseInd);
	setMembers(xContext);
	logInfo(xContext, "Read complete", verboseInd);
	if (isAlreadyProcessed()) {
	    rollBackDBAndSetReturncode(xContext, getAppName(), TkConstants.NOTHING_TO_DO);
	    return;
	}
	
	// Set the Tool Kit object based on committed file paths
	setToolKits(xContext);

	// Lookup or create Change Requests (1 per TK)
	setChangeRequests(xContext);

	// Create CodeUpdate object (1 per TK)
	loadCodeUpdates(xContext);
	logInfo(xContext, "CodeUpdate created ...", true);

	// Write CodeUpdate IDs into the data file.
	updateDataFile(xContext);
	logInfo(xContext, "Data file updated with DB ids ...", verboseInd);

	// Commit the changes and disconnect from the database
	commitToDBAndSetReturncode(xContext, getAppName(), SUCCESS);
	//rollBackDBAndSetReturncode(xContext, getAppName(), SUCCESS);

    }


    /**
     * Set the ToolKits from the Component and Branch data.
     * 
     * @param xContext Application Context
     * @throws IcofException
     */
    private void setToolKits(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "Setting ToolKit ...", verboseInd);

	// Convert the Component_Db to a Component object.
	Component comp = new Component(xContext, getComponent().getComponent());

	// Look up the Tool Kit
	Branch myBranch = new Branch(xContext, getBranchName(), null);
	toolKits = myBranch.findToolKits(xContext, comp);

	if (getToolKits().isEmpty()) {
	    throw new IcofException(APP_NAME,
	                            "setToolKit()",
	                            IcofException.SEVERE,
	                            "Unable to determine Tool Kit for this branch/trunk.\n",
	                            getBranchName().getName());
	}

	for (ToolKit tk : getToolKits()) {
	    logInfo(xContext, "Found ToolKit ... " + tk.getName(), verboseInd);
	    logInfo(xContext, " --> Contents\n" + tk.toString(xContext), verboseInd);
	}
	
    }
  

    /**
     * Writes the CodeUpdate ID back into the data file to be read by the SVN
     * commit hook.
     * 
     * @param context Application context
     * @throws IcofException
     */
    private void updateDataFile(EdaContext context)
    throws IcofException {

	// Update the file contents to include the CodeUpdate DB id.
	String dbIdEntry = "";
	for (ToolKit tk : getToolKits()) 
	    dbIdEntry += DB_ID + " " + DELIM + " " + 
	                 getCodeUpdates().get(tk.getName()).getId() + " " + 
	                 DELIM + "\n";

	// Write the new data file.
	try {
	    dataFile.openAppend();
	    dataFile.writeLine(dbIdEntry, true);
	}
	catch (IcofException ie) {
	    if (dataFile.isOpenAppend()) {
		dataFile.closeAppend();
	    }
	    throw ie;
	}

    }


    /**
     * Creates FileName objects for each add/updated source file and adds them
     * to the database.
     * 
     * @param context Application context
     * @throws IcofException
     */
    private void loadSourceFiles(EdaContext xContext, CodeUpdate_Db codeUpdate)
    throws IcofException {

	// Add the new file objects.
	addFiles(xContext, codeUpdate, getAddedFiles(), 
	         FileActionName_Db.ACTION_ADD);

	// Add the updated file objects.
	addFiles(xContext, codeUpdate, getUpdatedFiles(), 
	         FileActionName_Db.ACTION_UPDATE);

	// Add the deleted file objects.
	addFiles(xContext, codeUpdate, getDeletedFiles(), 
	         FileActionName_Db.ACTION_DELETE);
	
	logInfo(xContext, "Source files created ...", verboseInd);

    }


    /**
     * Add files in the fileList to the database and associate with this
     * CodeUpdate.
     * 
     * @param context Application context
     * @param fileList Collection of file names
     * @param action Name of the file action taken by SVN [ADD, UPDATE, DELETE]
     * @throws IcofException
     */
    private void addFiles(EdaContext xContext, CodeUpdate_Db codeUpdate, 
                          Vector<String> fileList, String action)
                          throws IcofException {

	// Create the FileActionName object for the desired action.
	FileActionName_Db fileAction = new FileActionName_Db(action);
	fileAction.dbLookupByName(xContext);

	// Iterate through the file list. Lookup the file or add it to the
	// database. Associate the file with this CodeUpdate.
	Iterator<String> iter = fileList.iterator();
	while (iter.hasNext()) {
	    String fileName = iter.next();
	    logInfo(xContext, "Adding file: " + fileName, verboseInd);

	    // Lookup the file and/or add the file.
	    FileName_Db file = new FileName_Db(fileName);
	    try {
		file.dbLookupByName(xContext);
		logInfo(xContext, " Found it in DB ... ", verboseInd);
	    }
	    catch (IcofException ie) {
		file.dbAddRow(xContext);
		logInfo(xContext, " Added it to DB ... ", verboseInd);
	    }

	    // Associate it with this CodeUpdate.
	    FileVersion_Db fileVer = new FileVersion_Db(codeUpdate, file,
	                                                fileAction);
	    fileVer.dbAddRow(xContext);

	}

    }


    /**
     * Creates the CodeUpdate object and associates it to the ChangeRequest
     * in the database.
     * 
     * @param xCcontext Application context
     * @throws IcofException
     */
    private void loadCodeUpdates(EdaContext xContext)
    throws IcofException {

	codeUpdates = new Hashtable<String, CodeUpdate_Db>();
	for (ToolKit tk : getToolKits()) {
	
	    CodeUpdate_Db codeUpdate = createCodeUpdate(xContext, tk);
	    getCodeUpdates().put(tk.getName(), codeUpdate);
	    
	    loadSourceFiles(xContext, codeUpdate);
	    		
	    // Associate this Code Update with the ChangeRequest for the 
	    // corresponding tool kit
	    if (getChangeRequests().containsKey(tk.getName())) {

		linkChangeRequest(xContext, codeUpdate,
		                  getChangeRequests().get(tk.getName()));
		
	    }
	    else {

		logInfo(xContext, "CodeUpdate NOT associated with ChangeRequest",
		        getVerboseInd(xContext));

	    }

	}
    }


    /**
     * Link this code update to the change request
     *
     * @param xContext Application context
     * @param codeUpdate       Code update object to link to change request
     * @param changeRequest_Db Change request object to link to
     * @throws IcofException 
     */
    private void linkChangeRequest(EdaContext xContext, 
                                   CodeUpdate_Db codeUpdate,
				   ChangeRequest_Db changeReq) throws IcofException {

	
	CodeUpdate_ChangeRequest_Db update;
	update = new CodeUpdate_ChangeRequest_Db(codeUpdate, changeReq);
	update.dbAddRow(xContext);
	
	logInfo(xContext, "CodeUpdate associated with ChangeRequest - "
	        + changeReq.getId(), 
	        getVerboseInd(xContext));
	
    }


    /**
     * Creates the Code Update object in the database
     *
     * @param xContext Application context
     * @param tk       Tool kit files for code updated were committed to
     * @throws IcofException 
     */
    private CodeUpdate_Db createCodeUpdate(EdaContext xContext, ToolKit tk) 
    throws IcofException {

	// Add the code update to the database
	Timestamp now = new Timestamp(new java.util.Date().getTime());
	CodeUpdate_Db codeUpdate = new CodeUpdate_Db(xContext, 
	                                             tk.getToolKit(), 
	                                             getComponent().getComponent(),
	                                             getRevision(),
	                                             getBranchName().getName(),
	                                             getDescription(), 
	                                             null, null,
	                                             getUser().getIntranetId(),
	                                             now, 
	                                             null,
	                                             null);

	codeUpdate.dbAddRow(xContext);
	logInfo(xContext, "CodeUpdate created ... " + codeUpdate.getId(), verboseInd);

	return codeUpdate;
	
    }


    /**
     * Parses the data file and sets data members based on the name/value pairs.
     * 
     * @param xContext Application context
     */
    private void setMembers(EdaContext xContext)
    throws IcofException {

	// Read the data file.
	try {
	    dataFile.openRead();
	    dataFile.read();
	}
	catch (IcofException ie) {
	    throw ie;
	}
	finally {
	    if (dataFile.isOpenRead())
		dataFile.closeRead();
	}

	// Cycle through the contents looking for keywords.
	setAlreadyProcessed(false);
	boolean oldStyleAbstract = false;
	boolean abstractStarted = false;
	Vector<String> tokens = new Vector<String>();
	setDescription("");
	Iterator<String> iter = dataFile.getContents().iterator();
	while (iter.hasNext()) {
	    String line = iter.next();
	    logInfo(xContext, "setMembers() Line: " + line,
	            getVerboseInd(xContext));

	    tokens.clear();
	    IcofCollectionsUtil.parseString(line, DELIM, tokens, true);

	    if (line.indexOf(HOOK_VER) > -1) {
		if (tokens.size() > 1) {
		    setHookVersion(tokens.get(1));
		    if (getHookVersion().compareToIgnoreCase(NEW_ABSTRACT_HOOKREL) < 0) {
			oldStyleAbstract = true;
		    }
		}
		logInfo(xContext, "Hook Ver: " + getHookVersion(),
		        getVerboseInd(xContext));
		logInfo(xContext, " -> old style? " + oldStyleAbstract,
		        getVerboseInd(xContext));
	    }
	    else if (line.indexOf(REPOSITORY) > -1) {
		if (tokens.size() > 1) {
		    setRepository(tokens.get(1));
		}
	    }
	    else if (line.indexOf(COMPONENT) > -1) {
		if (tokens.size() > 1) {
		    setComponent(xContext, tokens.get(1));
		}
	    }
	    else if (line.indexOf(CHANGE_REQUEST) > -1) {
		if (tokens.size() > 1) {
		    setChangeRequestName(tokens.get(1).trim());
		}
	    }
	    else if (line.indexOf(REVISION) > -1) {
		if (tokens.size() > 1) {
		    setRevision(tokens.get(1));
		}
	    }
	    else if (line.indexOf(USER) > -1) {
		if (tokens.size() > 1) {
		    resetUser(xContext, tokens.get(1));
		}
	    }
	    else if (line.indexOf(COMMIT_DATE) > -1) {
		if (tokens.size() > 1) {
		    setCommitDateString(tokens.get(1));
		}
	    }
	    else if (line.indexOf(COMMIT_TIME) > -1) {
		if (tokens.size() > 1) {
		    setCommitTimeString(tokens.get(1));
		}
	    }
	    else if (line.indexOf(CHANGED) > -1) {
		if (tokens.size() > 2) {
		    setChangedFiles(xContext, tokens.get(1), tokens.get(2));
		}
	    }
	    else if (line.indexOf(TkConstants.DB_ID) > -1) {
		setAlreadyProcessed(true);
	    }
	    else if (oldStyleAbstract && line.indexOf(ABSTRACT) > -1) {
		if (tokens.size() > 1) {
		    setDescription(tokens.get(1));
		}
	    }
	    else if (line.indexOf(ABSTRACT_NEW_START) > -1) {
		abstractStarted = true;
	    }
	    else if (line.indexOf(ABSTRACT_NEW_END) > -1) {
		abstractStarted = false;
	    }
	    else {
		if (!oldStyleAbstract && abstractStarted) {
		    if (getDescription().equals(""))
			setDescription(line);
		    else
			setDescription(getDescription() + "\n" + line);
		}
		else if (oldStyleAbstract) {
		    if (tokens.size() < 1) {
			setDescription(getDescription() + "\n" + line);
		    }
		    else {
			setDescription(getDescription() + "\n" + tokens.get(0));
		    }
		}

		// Ignore non-abstract lines

	    }

	}

	logInfo(xContext, "ABSTRACT: " + getDescription(),
	        getVerboseInd(xContext));

	// Set the commit timestamp.
	if ((getCommitDateString() != null) && (getCommitTimeString() != null)) {
	    setCommitTimestamp(Timestamp.valueOf(getCommitDateString() + " "
	    + getCommitTimeString()
	    + ".000"));
	    logInfo(xContext, " Commit timestamp: "
	    + getCommitTimestamp().toString(), verboseInd);
	}

	// Set the branch name from the collection of changed files
	parseChangedFilesForBranch(xContext);

	// Show changed files if verbose on
	if (getVerboseInd(xContext)) {
	    String results = IcofCollectionsUtil.getVectorAsString(getAddedFiles(),
	    "\n");
	    logInfo(xContext, "Added files\n------------\n" + results, true);

	    results = IcofCollectionsUtil.getVectorAsString(getDeletedFiles(),
	    "\n");
	    logInfo(xContext, "Deleted files\n------------\n" + results, true);

	    results = IcofCollectionsUtil.getVectorAsString(getUpdatedFiles(),
	    "\n");
	    logInfo(xContext, "Updated files\n------------\n" + results, true);

	    logInfo(xContext, "\n", true);
	}


    }


    /**
     * Read all the changed files to determine the branch name which could be
     * trunk or a branch name.
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void parseChangedFilesForBranch(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "In parseChangedFilesForBranch() ...", verboseInd);

	String myBranchName = "";
	String bestBranch = "";

	setOnTrunk(false);
	boolean stopSearch = false;

	// The tags/branches directory is not preceded by trunk so assume its
	// for the trunk if branch has not been set
	Iterator<String> iter = getChangedFiles().iterator();
	while (iter.hasNext() && !stopSearch) {
	    String fileName = iter.next();
	    logInfo(xContext, " file: " + fileName, verboseInd);

	    String thisBranch = IcofStringUtil.getField(fileName, 1, "/");

	    logInfo(xContext, " branch: " + thisBranch, verboseInd);

	    // If the "branch" is the trunk then set the branchName to trunk
	    // and set the "on trunk" flag to true.
	    if (thisBranch.equals(TkConstants.TRUNK)) {
		myBranchName = thisBranch;
		setOnTrunk(true);
		stopSearch = true;
	    }

	    // If the "branch" is a branch then set the branchName to the 2nd
	    // qualifier (branches/branch_name).
	    else if (thisBranch.equals(TkConstants.BRANCHES)) {
		if (IcofStringUtil.occurrencesOf(fileName, "/") > 1) {
		    bestBranch = IcofStringUtil.getField(fileName, 2, "/");
		}
		else if (myBranchName.equals("")) {
		    myBranchName = TkConstants.BRANCHES;
		}
	    }

	    // If file path does not start with trunk or branches then the
	    // developer is probably not creating branches under the branches
	    // directory so use the first qualifier as the branch name
	    else {
		myBranchName = thisBranch;
	    }

	    logInfo(xContext, "  Branch     : " + myBranchName, verboseInd);
	    logInfo(xContext, "  Found Trunk: " + stopSearch, verboseInd);

	}

	// Set the branch name
	if (isOnTrunk() || bestBranch.equals("")) {
	    setBranchName(xContext, myBranchName);
	}
	else {
	    setBranchName(xContext, bestBranch);
	}

    }


    /**
     * Add this changed file to one of the changed file collections
     * 
     * @param changeType Change type [ A, U, D ]
     * @param fileName Changed file name
     */
    private void setChangedFiles(EdaContext xContext, String changeType,
                                 String fileName)
                                 throws IcofException {

	// Ensure the collections have been created.
	if (getUpdatedFiles() == null) {
	    updatedFiles = new Vector<String>();
	}
	if (getDeletedFiles() == null) {
	    deletedFiles = new Vector<String>();
	}
	if (getAddedFiles() == null) {
	    addedFiles = new Vector<String>();
	}
	if (getChangedFiles() == null) {
	    changedFiles = new Vector<String>();
	}

	// Remove any leading/trailing whitespace
	changeType = changeType.trim();
	fileName = fileName.trim();
	logInfo(xContext, " File name  : " + fileName, verboseInd);
	logInfo(xContext, " Change type: " + changeType, verboseInd);

	changedFiles.add(fileName);

	// Determine the file name (fileName - branch)
	String file = fileName.substring(fileName.indexOf("/") + 1);
	logInfo(xContext, " File name: " + fileName, verboseInd);

	// Add this file to the correct collection.
	if ((changeType.equalsIgnoreCase("U")) ||
	    (changeType.equalsIgnoreCase("_U")) ||
	    (changeType.equalsIgnoreCase("UU")) ||
	    (changeType.equalsIgnoreCase("R"))) {
	    updatedFiles.add(file);
	}
	else if (changeType.equalsIgnoreCase("D")) {
	    int index = addedFiles.indexOf(file);
	    if (index > -1) {
		logInfo(xContext,
		        " -> Remove from addedFiles and add to deletedFiles .. ",
		        verboseInd);
		addedFiles.removeElementAt(index);
		deletedFiles.add(file);
	    }
	    else {
		logInfo(xContext, " -> Add to deletedFiles .. ", verboseInd);
		deletedFiles.add(file);
	    }
	}
	else if (changeType.equalsIgnoreCase("A")) {
	    int index = deletedFiles.indexOf(file);
	    if (index > -1) {
		logInfo(xContext,
		        " -> Remove from deletedFiles and add to addedFiles .. ",
		        verboseInd);
		deletedFiles.removeElementAt(index);
		addedFiles.add(file);
	    }
	    else {
		logInfo(xContext, " -> Add to addedFiles .. ", verboseInd);
		addedFiles.add(file);
	    }
	}
	else {
	    throw new IcofException(APP_NAME, "setChangedFiles()",
	                            IcofException.SEVERE,
	                            "Unrecognized file change type. ",
	                            changeType);
	}

    }


    /**
     * Print the object's values to the screen.
     */
    public String toString() {

	StringBuffer buffer = new StringBuffer();

	buffer.append("Data file: " + getDataFile());
	buffer.append("------------------------");
	buffer.append("Repository: " + getRepository());
	buffer.append("Branch: " + getBranchName().getName());
	buffer.append("ChangeReq: " + getChangeRequestName());
	buffer.append("Component: " + getComponent().getName());
	buffer.append("Revision: " + getRevision());
	buffer.append("Abstract: " + getDescription());
	buffer.append("User: " + getUser().toString());
	buffer.append("Commit time: " + getCommitTimeString());
	buffer.append("Commit date: " + getCommitDateString());

	if (getAddedFiles() != null) {
	    Iterator<String> iter = getAddedFiles().iterator();
	    while (iter.hasNext()) {
		String file = iter.next();
		buffer.append("File: A " + file);
	    }
	}

	if (getUpdatedFiles() != null) {
	    Iterator<String> iter = getUpdatedFiles().iterator();
	    while (iter.hasNext()) {
		String file = iter.next();
		buffer.append("File: U " + file);
	    }
	}

	if (getDeletedFiles() != null) {
	    Iterator<String> iter = getDeletedFiles().iterator();
	    while (iter.hasNext()) {
		String file = iter.next();
		buffer.append("File: D " + file);
	    }
	}

	return buffer.toString();

    }


    protected void createSwitches(Vector<String> singleSwitches,
                                  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-f");
    }


    protected String readParams(Hashtable<String, String> params,
                                String errors, EdaContext xContext)
                                throws IcofException {

	// Read the input file name
	if (params.containsKey("-f")) {
	    setDataFile(xContext, params.get("-f"));
	}
	else {
	    errors += "The data file (-f) is a required parameter.";
	}
	return errors;
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {

	boolean verboseInd = getVerboseInd(xContext);
	logInfo(xContext, "App      : " + APP_NAME + "  " + APP_VERSION,
	        verboseInd);
	logInfo(xContext, "Data File: " + getDataFile().getAbsolutePath(),
	        verboseInd);
	logInfo(xContext, "DB Mode  : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose  : " + getVerboseInd(xContext), verboseInd);
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Creates a CodeUpdate entry in the ETREE database \n");
	usage.append("based on data in the input file.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-f file> [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  file      = Full path to input file.\n");
	usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  -h        = Help (shows this information)\n");
	usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("\n");
	usage.append("Return Codes\n");
	usage.append("------------\n");
	usage.append(" 0 = ok\n");
	usage.append(" 1 = error\n");
	usage.append("\n\n");
	usage.append("------------------------------------------------------\n");

	System.out.println(usage);

    }


    /**
     * Members.
     * @formatter:off
     */
    private IcofFile dataFile;
    private BranchName branchName;
    private String hookVersion;
    private String revision;
    private String description;
    private String repository;
    private String commitDateString;
    private String commitTimeString;
    private Vector <String>addedFiles;
    private Vector<String> updatedFiles;
    private Vector<String> deletedFiles;
    private Vector<String> changedFiles;
    private boolean onTrunk;
    private boolean alreadyProcessed;
    private Timestamp commitTimestamp;
    private Hashtable<String, CodeUpdate_Db> codeUpdates;
    private Vector<ToolKit> toolKits;
    private String changeRequestName;
    private Hashtable<String, ChangeRequest_Db> changeRequests;

    /**
     * Getters.
     */
    public Hashtable<String, CodeUpdate_Db> getCodeUpdates() { return codeUpdates; }
    public IcofFile getDataFile() { return dataFile; }
    public BranchName getBranchName() {return branchName; }
    public String getHookVersion() {return hookVersion; }
    public String getRevision() {return revision; }
    public String getDescription() {return description; }
    public String getRepository() {return repository; }
    public String getCommitDateString() {return commitDateString; }
    public String getCommitTimeString() {return commitTimeString; }
    public Vector<String> getAddedFiles() {return addedFiles; }
    public Vector<String> getDeletedFiles() {return deletedFiles; }
    public Vector<String> getChangedFiles() {return changedFiles; }
    public Vector<String> getUpdatedFiles() {return updatedFiles; }
    public static boolean getRequestHelp() { return requestHelp; }
    public boolean isOnTrunk() {return onTrunk; }
    public Timestamp getCommitTimestamp() {return commitTimestamp; }
    public boolean isAlreadyProcessed() {return alreadyProcessed; }
    public Vector<ToolKit> getToolKits() { return toolKits; }
    public String getChangeRequestName() { return changeRequestName; }
    public Hashtable<String, ChangeRequest_Db> getChangeRequests() { return changeRequests; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}

    /**
     * Setters.
     */
    private void setHookVersion(String aVer) { hookVersion = aVer; }
    private void setRevision(String aRev) { revision = aRev; }
    private void setRepository(String aRepos) { repository = aRepos.trim(); }
    private void setCommitDateString(String aDateStr) {commitDateString = aDateStr.trim(); }
    private void setCommitTimeString(String aTimeStr) { commitTimeString = aTimeStr.trim(); }
    private void setDataFile(IcofFile aFile) { dataFile = aFile; }
    private void setOnTrunk(boolean aFlag) { onTrunk = aFlag; }
    private void setCommitTimestamp(Timestamp aTms) { commitTimestamp = aTms; }
    private void setAlreadyProcessed(boolean aFlag) { alreadyProcessed = aFlag; }
    private void setDescription(String aDesc) { description = aDesc; }
    private void setChangeRequestName(String aName) { changeRequestName = aName; }
    // @formatter:on


    /**
     * Set the IcofFile object from the data file name.
     * 
     * @param xContext Application context.
     * @param aFileName Full path to data file
     * @throws IcofException
     */
    private void setDataFile(EdaContext xContext, String aFileName)
    throws IcofException {

	if (getDataFile() == null)
	    dataFile = new IcofFile(aFileName.trim(), false);

    }


    /**
     * Set the ChangeRequest_Db object
     * 
     * @param xContext Application context.
     * @param aName Name like MDCMS00012345
     * @throws IcofException
     */
    protected void setChangeRequests(EdaContext xContext)
    throws IcofException {

	changeRequests = new Hashtable<String, ChangeRequest_Db>();
	
	// If change request name == "n/a" don't look it up
	if ((getChangeRequestName() != null) && 
	    (getChangeRequestName().toUpperCase().equals("N/A")))
	    logInfo(xContext, "ChangeRequest: null", verboseInd);
	
	// If change request name == "DEV" don't look it up .. create new one
	else if ((getChangeRequestName() != null) && 
	         (getChangeRequestName().toUpperCase().equals("DEV"))) 
	    createDevChangeRequest(xContext, getChangeRequestName());

	// Otherwise lookup the CQ based change request
	else {
	    
	    ChangeRequest_Db changeRequestDb;
	    changeRequestDb = new ChangeRequest_Db(getChangeRequestName());
	    changeRequestDb.dbLookupByName(xContext);
	    getChangeRequests().put(getToolKits().firstElement().getName(), 
	                            changeRequestDb);
	    logInfo(xContext,
	            "ChangeRequest: " + changeRequestDb.toString(xContext),
	            verboseInd);
	}

    }


    /**
     * Create a new change request if DEV was used for the CR
     *
     * @param xContext
     * @param aName
     * @throws IcofException 
     */
    private void createDevChangeRequest(EdaContext xContext, String aName) 
    throws IcofException {

	ChangeRequestStatus approved = new ChangeRequestStatus(xContext, 
	                                                       "APPROVED");
	approved.dbLookupByName(xContext);
	
	ChangeRequestType feature= new ChangeRequestType(xContext, "FEATURE");
	feature.dbLookupByName(xContext);

	
	ChangeRequestSeverity low = new ChangeRequestSeverity(xContext, "4");
	low.dbLookupByName(xContext);
	
	// Create a CR for each tool kit and associate with TK/Comp
	for (ToolKit tk : getToolKits()) {
	    
	    ChangeRequest cr = new ChangeRequest(xContext, getChangeRequestName(), 
	                                         getDescription(), approved, 
	                                         feature, low, "");
	    cr.dbAdd(xContext, getUser(), false);
	    
	    getChangeRequests().put(tk.getName(), cr.getChangeRequest());
	    logInfo(xContext, "Created DEV CR: " + cr.getChangeRequest().getId(), verboseInd);
	    
	    // Associate this component TK with the CR for this TK
	    Component_Version_Db cv = new Component_Version_Db(xContext, 
	                                                       tk.getToolKit(), 
	                                                       getComponent().getComponent());
	    cv.dbLookupByAll(xContext);
	    
	    CompVersion_ChangeRequest_Db cvcr;
	    cvcr = new CompVersion_ChangeRequest_Db(cr.getChangeRequest(), cv);
	    cvcr.dbAddRow(xContext);
	    
	}
	
    }


    /**
     * Set the BranchName object
     * 
     * @param xContext Application context.
     * @param aName Branch name
     * @throws IcofException
     */
    protected void setBranchName(EdaContext xContext, String aName)
    throws IcofException {

	if (getBranchName() == null) {
	    branchName = new BranchName(xContext, aName.trim());

	    // Look up the branch name only if ChangeManagement is required
	    //	    if (getChangeRequestDb() != null) {
	    //		branchName.dbLookupByName(xContext);
	    //	    }
	    branchName.dbLookupByName(xContext);
	}
	logInfo(xContext, "BranchName: " + getBranchName().toString(xContext),
	        getVerboseInd(xContext));

    }


    /**
     * Set the User object from the user'd id
     * 
     * @param xContext Application context.
     * @param aUserName AFS user id
     * @throws IcofException
     */
    private void resetUser(EdaContext xContext, String aUserid)
    throws IcofException {

	user = new User_Db(aUserid.trim(), true);
	try {
	    user.dbLookupByIntranet(xContext);
	}
	catch (IcofException ie) {
	    // Not found so populate from Blue Pages and add to DB
	    user.populateFromBluePages(xContext);
	    user.dbAddRow(xContext);
	}

	logInfo(xContext, "User: " + getUser().toString(xContext),
	        getVerboseInd(xContext));
    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {

	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }

}
