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
 * Generate releasse notes for a Tool Kit Package 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 03/05/2014 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree.packaging;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.app.etree.ChangeRequestShowRelNotes;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.ComponentPackage;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKitPackage;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class PackageShowRelNotes extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "pkg.showRelNotes";
    public static final String APP_VERSION = "v1.0";

    
    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aTkName  Tool Kit name (14.1.8e, 18.1.8e ...)
     * @param aPkg     Tool kit package to process
     * @param aRelNotesDir Object containing path of where to write rel notes to
     * @param bWrite   If true write rel notes to file otherwise dump to screen
     */
    public PackageShowRelNotes(EdaContext aContext, String aTkName, 
                               ToolKitPackage aPkg, IcofFile aRelNotesDir,
                               boolean bWrite)
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setToolKitName(aTkName);
	setTkPackage(aPkg);
	setRelNotesDir(aRelNotesDir);
	setWriteFlag(bWrite);
	
    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * @exception IcofException Unable to construct ManageApplications object
     */
    public PackageShowRelNotes(EdaContext aContext) throws IcofException {

	this(aContext, null, null, null, true);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new PackageShowRelNotes(null);
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
     * @throws SQLException 
     * @throws IOException 
     */
    public void process(EdaContext xContext)
    throws IcofException, SQLException, IOException {

	connectToDB(xContext);

	generate(xContext);

	commitToDB(xContext, APP_NAME);

    }


    /**
     * Generate and display release notes
     * 
     * @param aContext Application Context
     * @throws IcofException
     * @throws SQLException 
     * @throws IOException 
     */
    public String generate(EdaContext xContext)
    throws IcofException {

	// Look up all component packages associated with this tk package
	logInfo(xContext, "Querying TK Package for Component Packages ...", 
	        verboseInd);
	Vector<ComponentPackage> compPkgs;
	compPkgs = getTkPackage().getComponentPackages(xContext);
	logInfo(xContext, " Found " + compPkgs.size() + " Component Packages", 
	        verboseInd);

	// Initialize the string buffer to hold release notes
	if (getRelNotes() == null)
	    relNotes = new StringBuffer();
	
	// Write the header
	Date now = new Date();
	SimpleDateFormat dt = new SimpleDateFormat("MMM dd, yyyy");

	String pliName = getToolKitName();
	pliName += "." + getTkPackage().getName();
	relNotes.append("Release notes for " + pliName + "\n"); 
	relNotes.append(dt.format(now) + "\n\n\n");
	
	// Get release notes for each component
	for (ComponentPackage compPkg : compPkgs) {
	    setReleaseNotes(xContext, compPkg);
	}
	
	// Write release notes to a file
	return displayResults(xContext);
	
    }

    
    /**
     * Generate the release notes for associated with this package
     *
     * @param xContext Application context
     * @param compPkg  Component package to find rel notes for
     * @throws IcofException 
     */
    private void setReleaseNotes(EdaContext xContext, ComponentPackage compPkg) 
    throws IcofException {

	// Create a list of Change Requests for this package
	ChangeRequest myCr = new ChangeRequest(xContext, "", "", null, null, 
	                                       null, "");
	Vector<ChangeRequest> crs = myCr.dbLookupByCompPkg(xContext, compPkg);
	
	
	// For each CR get the release note text
	String name = compPkg.getName().substring(0, compPkg.getName().lastIndexOf("."));
	String header = name + "\n----------------------------\n";
	boolean bHeader = true;
	for (ChangeRequest cr : crs) {

	    ChangeRequestShowRelNotes app;
	    app = new ChangeRequestShowRelNotes(xContext, cr, true);
	    app.setRelNotes(xContext);

	    if (bHeader) {
		getRelNotes().append("\n" + header);
		bHeader = false;
	    }
	    getRelNotes().append(app.getRelNotes() + "\n\n\n");
	    
	}
	
    }


    /**
     * Show the install results
     *
     * @param xContext  Application context
     * @param total 
     * @param goodPkgs  Collection of pkgs that installed ok
     * @param badPkgs   Collection of pkgs that did not install
     * @throws IcofException 
     */
    private String displayResults(EdaContext xContext) throws IcofException {

	if (! isWrite()) {
	    System.out.println("\n\n");
	    System.out.println(getRelNotes().toString());
	    return null;
	}

	// Write the README file
	String relNoteFileName = getRelNotesDir().getAbsolutePath() + File.separator + 
	                         "README" + "_" + 
	                         getPliTkName(xContext, false) + ".txt";
	IcofFile relNoteFile = new IcofFile(relNoteFileName, false);
	relNoteFile.getParentFile().mkdirs();
	
	relNoteFile.openWrite();
	relNoteFile.writeLine(getRelNotes().toString());
	relNoteFile.closeWrite();
		
	logInfo(xContext, "\nREADME file written to " + relNoteFileName, true);
	
	return relNoteFileName;
	
    }


    /**
     * Construct the tk14.1.6z.0 name
     * 
     * @param xContext Application context
     * @return
     */
    private String getPliTkName(EdaContext xContext, boolean bPrevToolkit) {

	int pliVersion = Integer.parseInt(getTkPackage().getName());
	if (bPrevToolkit && ! getTkPackage().getName().equals("0"))
	    pliVersion--;
	return "tk" + getToolKitName() + "." + pliVersion;
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

	// Read the tool kit and tk package names
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
	else {
	    errors += "Tool kit maintenance name (-t) is a required parameter\n";
	}

	// Read the saved flag
	setWriteFlag(false);
	if (params.containsKey("-write")) {
	    setWriteFlag(true);
	    IcofFile dir = new IcofFile((String) params.get("-write"), true);
	    setRelNotesDir(dir);
	}
		
	return errors;

    }

    
    /**
     * Set the Request object
     * 
     * @param xContext Application context.
     * @param anId Request id
     * @throws IcofException
     */
    protected void setToolKitPackage(EdaContext xContext, String aName)
    throws IcofException {

	// Strip off the tool kit name and lookup the tool kit
	setToolKitName(aName.substring(0, aName.lastIndexOf(".")));
	logInfo(xContext, "Tool Kit: " + getToolKitName(), verboseInd);
	
	// Set the maintenance
	String maintName = aName.substring(aName.lastIndexOf(".") + 1);
	toolKitPkg = new ToolKitPackage(xContext, maintName);
	setToolKit(xContext, getToolKitName());
	getTkPackage().dbLookupByName(xContext, getToolKit());
	
	logInfo(xContext,
		"Tool Kit Package: " + getTkPackage().toString(xContext),
		verboseInd);
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
	argSwitches.add("-write");
	argSwitches.add("-db");
	argSwitches.add("-t");

    }


    /**
     * Display application's invocation
     * 
     * @param dbMode Database model
     * @param xContext Application context object
     */
    protected void displayParameters(String dbMode, EdaContext xContext) {

	logInfo(xContext, "App        : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	logInfo(xContext, "Tool Kit   : " + getToolKitName(), verboseInd);
	logInfo(xContext, "TK Pkg     : " + getTkPackage().getName(), verboseInd);
	logInfo(xContext, "Write mode : " + isWrite(), verboseInd);
	if (isWrite())
	    logInfo(xContext, "RelNotesDir: Write mode: " + 
	            getRelNotesDir().getAbsolutePath(), verboseInd);
	logInfo(xContext, "DB Mode    : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose    : " + getVerboseInd(xContext),
		verboseInd);
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Generate release notes for the given tool kit package.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t tk_pkg> [-write relNoteDir] [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  tk_pkg     = Tool kit Package name (ie, 14.1.6.8, 14.1.6z.1 ...)\n");
	usage.append("  relNoteDir = Location to write release notes to\n");
	usage.append("               Default is write rel notes to screen\n");
	usage.append("  -y         = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode     = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h         = Help (shows this information)\n");
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
    private ToolKitPackage toolKitPkg;
    private StringBuffer relNotes;
    private boolean writeFlag = false;
    private IcofFile relNotesDirectory;
    private String toolKitName;

    
    /**
     * Getters
     */
    public ToolKitPackage getTkPackage() { return toolKitPkg; }
    public StringBuffer getRelNotes() { return relNotes; }
    public IcofFile getRelNotesDir() { return relNotesDirectory; }
    public String getToolKitName() { return toolKitName; }
    public boolean isWrite() { return writeFlag; }
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }

    
    /**
     * Setters
     */
    private void setTkPackage(ToolKitPackage aPkg) { toolKitPkg = aPkg; }
    private void setWriteFlag(boolean aFlag) { writeFlag = aFlag; }
    private void setRelNotesDir(IcofFile aDir) { relNotesDirectory = aDir; }
    private void setToolKitName(String aName) { toolKitName = aName; }
    // @formatter:on


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }

}
