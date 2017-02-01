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
 * Install component packages in server and ASICs install locations
 *-----------------------------------------------------------------------------
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 02/13/2014 GFS  Initial coding
 * 03/25/2014 GFS  Disabled support for incremental installs
 * 04/22/2014 GFS  Updated to support ASIC installs
 * 04/29/2014 GFS  Added support for ICC
 * 06/19/2014 GFS  Updated to use native java logging
 * 07/28/2014 GFS  Updated to support site specific server installs
 * 12/29/2014 GFS  Added support for patch level preview(beta) installs
 * 03/05/2015 GFS  Fixed problem with initial incremental install for mar2*.
 *                 Updated getVolumeName() to work with initial installs. 
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree.packaging;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import com.ibm.stg.eda.app.etree.PlatformShow;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Platform_Db;
import com.ibm.stg.eda.component.tk_etreedb.StageName_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ComponentPackage;
import com.ibm.stg.eda.component.tk_etreeobjs.EventName;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKitPackage;
import com.ibm.stg.iipmds.common.IcofDateUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofStringUtil;
import com.ibm.stg.iipmds.common.IcofSystemUtil;

public class PackageInstall extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "pkg.install";
    public static final String APP_VERSION = "v1.3";
    public static final String ERROR_EXT = ".error";
    public static final String INSTALLED_EXT = ".installed";
    

    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aDir Test install directory
     */
    public PackageInstall(EdaContext aContext, IcofFile aDir)
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setInstallDir(aDir);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * @exception IcofException Unable to construct ManageApplications object
     */
    public PackageInstall(EdaContext aContext) throws IcofException {

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
	    myApp = new PackageInstall(null);
	    start(myApp, argv, PackageInstall.class.getName(), APP_NAME);
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

	installAll(xContext);

	commitToDB(xContext, APP_NAME);

    }


    /**
     * Run the install process
     * 
     * @param aContext Application Context
     * @throws IcofException
     * @throws SQLException 
     * @throws IOException 
     */
    public void installAll(EdaContext xContext)
    throws IcofException, SQLException, IOException {

	// Verify user is authorized to run this application
	try {
	    getUser().validateUser(xContext, "edaadmin");
	}
	catch(IcofException trap) {
	    getUser().validateUser(xContext, "integ");
	}

	// Set the location where to find the packages
	setPackageDirs(xContext);
	setPlatforms(xContext);

	// Determine if any tool kit packages need to be installed
	// Create PLI links if new tool kit package
	setToolKitPackage(xContext);

	// Create the PLI links for this TK pkg if requested
	if (isPrepInstall())
	    createPliLinks(xContext);
	
	// Install
	installTkPackage(xContext);

    }


    /**
     * Install all component packages associated with the tool kit package into
     * the install directory
     * 
     * @param xContext Application context
     * @throws IcofException
     * @throws SQLException 
     */
    private void installTkPackage(EdaContext xContext)
    throws IcofException, SQLException {

	// If no TK package then skip
	if (getToolKitPkg() == null)
	    return;
	
	// Look up all component packages associated with this tk package 
	// and install type
	logInfo(xContext, "Querying TK Package for Component Packages ...", 
	        true);
	Vector<ComponentPackage> compPkgs;
	compPkgs = getComponentPackages(xContext);
	logInfo(xContext, " Found " + compPkgs.size() + " Component Packages", 
	        true);

	List<String> goodPkgs = new ArrayList<String>();
	List<String> badPkgs = new ArrayList<String>();
	List<String> skippedPkgs = new ArrayList<String>();
	
	// Install each component package
	int totalCount = compPkgs.size();
	int counter = 1;
	for (ComponentPackage compPkg : compPkgs) {
	    
	    logInfo(xContext, "\nPkg: " + compPkg.getName(), verboseInd);
	    setComponentName(xContext, compPkg);
	    
	    logInfo(xContext, "\nUnpacking - " + compPkg.getName() + " (" + 
                    counter++ + " of " + totalCount + ")", true);
	    
	    try {
		
		// Process component package
		setInstallDirs(xContext, compPkg);
		
		if (isInstalled(xContext, compPkg) && ! isForceInstall()) {
		    logInfo(xContext, "SKIPPING .. this package has already " +
		    "been installed at this site", true);
		    skippedPkgs.add(compPkg.getName());
		    continue;
		}
		
		cleanInstall(xContext);
		unpackCompPackage(xContext, compPkg);
		verifyUnpack(xContext, compPkg);
		postInstall(xContext, compPkg);
		setCompPkgEvent(xContext, compPkg, "PKG_UNPACKED", "");
		logInstallData(xContext, compPkg);
		goodPkgs.add(compPkg.getName());
		
		// Commit the comp pkg event to the database so it will not 
		// be reprocessed on a retry
		xContext.getConnection().commit();

		logInfo(xContext, "Installed " + compPkg.getName(), true);
	
	    }
	    catch(IcofException ignore) {
		logInfo(xContext, "FAILED to install " + compPkg.getName(), 
		        true);
		logInfo(xContext, ignore.getMessage(), true);
		logInfo(xContext, ignore.printStackTraceAsString(), true);
		logErrorData(xContext, compPkg, ignore.getMessage());
		
		badPkgs.add(compPkg.getName());
		
	    }
	    
	}
	
	// Update the TK package state and create install sym links if no errors
	if ((badPkgs.size() == 0) && (goodPkgs.size() > 0)) {
	    //setTkPkgEvent(xContext, "PKG_UNPACKED", "All component pkgs unpacked");
	    createInstallSymlinks(xContext);
	}
	
	// Display the results
	displayResults(xContext, totalCount, goodPkgs, badPkgs, skippedPkgs);

    }


    /**
     * 
     *
     * @param xContext
     */
    private void cleanInstall(EdaContext xContext) {

	
    }


    /**
     * Determine if this package has already been installed
     *
     * @param xContext
     * @param compPkg
     * @return
     */
    private boolean isInstalled(EdaContext xContext, ComponentPackage compPkg) {

	boolean bInstalled = false;
	
	IcofFile installedFile;
	installedFile = getInstalledFile(xContext, getHistoryDir(), 
	                                 compPkg);
	
	logInfo(xContext, "Checking existence of installed file ...", true);
	logInfo(xContext, " -> " + installedFile.getAbsolutePath(), true);
	
	if (installedFile.exists()) 
	    bInstalled = true;
	
	return bInstalled;
	
    }

    
    /**
     * Create a collection of Component Packages to install
     * 
     * @param xContext Application context
     */
    private Vector<ComponentPackage> getComponentPackages(EdaContext xContext) 
    throws IcofException {
    
	return getCompPkgsForProd(xContext);
	
    }
	
    
    /**
     * Create a collection of Component Pkgs to install
     *
     * @param xContext
     * @return
     * @throws IcofException 
     */
    private Vector<ComponentPackage> getCompPkgsForProd(EdaContext xContext) 
    throws IcofException {
	
	// Create a collection of all available component packages for this
	// tool kit package
	Vector<ComponentPackage> allPkgs;
	allPkgs = getToolKitPkg().getComponentPackages(xContext);
	
	// Create a list of components for this tk and install type
	ArrayList<String> componentNames;
	componentNames = InstallUtils.setDeliveredComps(xContext, 
	                                                getToolKit(), 
	                                                "aix", 
	                                                getComponent(), 
	                                                isAsicsInstall());
	
	// Filter the comp pkg collection so it only contains components for
	// tk and install
	Vector<ComponentPackage> myPkgs = new Vector<ComponentPackage>();
	for (ComponentPackage pkg : allPkgs) {
	    String[] tokens = pkg.getName().split("[.]");
	    if (componentNames.contains(tokens[0])) {
		myPkgs.add(pkg);
	    }
	    else {
		logInfo(xContext, "Not installing " + pkg.getName(),true);
	    }
	}

	return myPkgs;
	
    }
    

    /**
     * Determine if this install and package require an incremental install
     *
     * @param xContext Application context
     * @param compPkg  Package to be installed
     * @throws IcofException 
     */
    private String setIncrementalInstall(EdaContext xContext,
				       ComponentPackage compPkg) 
				       throws IcofException {

	logInfo(xContext, " Looking for incremental install marker ..", true);
	
	String nextInc = null;
	setIncrementalInstall(false);
	
	// Incremental installs not required for asic installs
	if (isAsicsInstall()) {
	    logInfo(xContext, "  Skipping .. not a server install", true);
	    return nextInc;
	}
	
	// Only mar2 mar2_2243 support incremental installs
	if (getCompName().startsWith("mar2")) {
	    
		// Always do incremental install for first patch level.
	    if (getToolKitPkg().getName().equals("0")) {
		setIncrementalInstall(true);
		logInfo(xContext, "  Initial incremental install", true);
	    }
	    else {
		// If this pkg has a corresponding <pkg>.increment file then
		// this pkg will require an incremental install
		String fileName = getPackageDir() + File.separator + 
		compPkg.getName() + ".tar.gz" + PkgUtils.MAR_INC_EXT;
		logInfo(xContext, "  Looking for " + fileName, true);
		File file = new File(fileName);
		if (file.exists()) {
		    setIncrementalInstall(true);
		    logInfo(xContext, "  Found marker file", true);
		}
		else {
		    logInfo(xContext, "  Skipping .. incremental install marker not found", true);
		}
	    }
	}
	else {
	    logInfo(xContext, "  Skipping .. not a mar2* component", true);	    
	}
	
	
	if (isIncrementalInstall()) 
	    nextInc = createNextIncrement(xContext);
	
	return nextInc;
	
    }


    /**
     * Create the next mar2* incremental install directory
     *
     * @param xContext Application context
     * @throws IcofException 
     */
    private String createNextIncrement(EdaContext xContext) throws IcofException {

	if (! isIncrementalInstall())
	    return null;
	
	logInfo(xContext, " CREATING NEW INCREMENTAL INSTALL ...", true);
	
	// Create a list of the current incremental installs
	String mar2Dir = getInstallDir().getAbsolutePath() + 
                        File.separator + getCompName();
	Vector<String> increments;
	increments = PkgUtils.readIncrements(mar2Dir, getCompName());

	// Get the current and next incremental install
	if (increments.isEmpty()) {
		
		// Get Toolkit release name and convert '.' with '0'(Ex: 14.1 => 1401)
		//String incRelName = getToolKit().getToolKit().getRelease().getName();
		String incRelName = getToolKit().getReleaseName();
		incRelName = incRelName.replace('.', '0'); 
				
	    String nextInc = getCompName() + "."+incRelName+".0";
	    logInfo(xContext, "  Initial incremental install: " + nextInc, true);
	    String existingFile = nextInc + File.separator + 
                                  getCompName() + File.separator + 
                                  getToolKit().getToolKit().getRelease().getName();
	    String newFile = getToolKit().getToolKit().getRelease().getName();
	    IcofFile marDir = new IcofFile(mar2Dir, true);
	    marDir.mkdir();
	    makeSymlink(xContext, existingFile, newFile, marDir);
	    	    
	    return nextInc;
	}

	String currentInc = increments.lastElement();
	String[] toks = currentInc.split("[.]+", 3);
	int version = Integer.parseInt(toks[2]) + 1;
	String nextInc = toks[0] + "." + toks[1] + "." + version;
	
	logInfo(xContext, "  Current incremental install: " + currentInc, true);
	logInfo(xContext, "  Next incremental install   : " + nextInc, true);
	
	// Copy the current inc install to the future
	logInfo(xContext, "  Copying " + currentInc + " to " + nextInc + " ...", true);
	IcofFile currIncDir = new IcofFile(mar2Dir + File.separator + currentInc, 
	                                   true);
	IcofFile nextIncDir = new IcofFile(mar2Dir + File.separator + nextInc, 
	                                   true);
	logInfo(xContext, "   from: " + currIncDir.getAbsolutePath(), true);
	logInfo(xContext, "   to  : " + nextIncDir.getAbsolutePath(), true);
	
	if (isDryRun())
	    logInfo(xContext, "  DRYRUN .. copy not actually executed", true);
	else 
	    currIncDir.copyPreserve(nextIncDir, true);
	
	// Update the release symlink (14.1 -> mar2.1401.1/mar2/14.1)
	String existingFile = nextInc + File.separator + 
	                      getCompName() + File.separator + 
	                      getToolKit().getToolKit().getRelease().getName();
	String newFile = getToolKit().getToolKit().getRelease().getName();
	IcofFile marDir = new IcofFile(mar2Dir, true);
	makeSymlink(xContext, existingFile, newFile, marDir);
	
	return nextInc;
	
    }


    /**
     * Set the component name from the component package
     *
     * @param xContext
     * @param compPkg
     */
    private void setComponentName(EdaContext xContext, 
                                  ComponentPackage compPkg) {

	String[] tokens = compPkg.getName().split("[.]");
	componentName = tokens[0];
	
	setIncrementPath("");
	
    }


    /**
     * Show the install results
     *
     * @param xContext  Application context
     * @param total 
     * @param installedPkgs Collection of pkgs that installed ok
     * @param failedPkgs    Collection of pkgs that did not install
     * @param skippedPkgs   Collection of pkgs that had already been installed
     */
    private void displayResults(EdaContext xContext, int total, 
                                List<String> installedPkgs, 
                                List<String> failedPkgs,
                                List<String> skippedPkgs) {

	StringBuffer msg = new StringBuffer();
	msg.append("\n");
	msg.append("Install results\n");
	msg.append("---------------\n");
	msg.append("Installed: " + installedPkgs.size() + "\n");
	msg.append("Failed   : " + failedPkgs.size() + "\n");
	msg.append("Skipped  : " + skippedPkgs.size() + "\n");
	msg.append("TOTAL    : " + total + "\n");
	msg.append("\n");
	if (! failedPkgs.isEmpty()) {
	    msg.append("Failed Installs\n");
	    for (String pkgName : failedPkgs) {
		msg.append(pkgName + "\n");
	    }
	    msg.append("\n");
	}
	logInfo(xContext, msg.toString(), true);

    }


     /**
     * Track the installed components in the install dir
     *
     * @param xContext
     * @param compPkg
     * @throws IcofException 
     */
    private void logInstallData(EdaContext xContext, ComponentPackage compPkg)
    throws IcofException {

	SimpleDateFormat sim;
	sim = new SimpleDateFormat(IcofDateUtil.TIMESTAMP_FORMAT_LOCK_FILE);
	
	// Create the file contents
	Vector<String> msg = new Vector<String>();
	msg.add("Successful package install");
	msg.add("--------------------------");
	msg.add("Package: " + compPkg.getName());
	msg.add("Patch level: " + getToolKitPkg().getName());
	msg.add("Install on: " + sim.format(new Date()));

	msg.add("");
	msg.add("Deliverables");
	msg.add("-------------");
	for (Object entry : getChecksumFile().getContents()) {
	    String line = (String)entry;
	    String[] tokens = line.split("[;]");
	    msg.add(tokens[0]);
	}

	msg.add("");
	msg.add("Deliverables");
	msg.add("-------------");
	for (Object entry : getChecksumFile().getContents()) {
	    String line = (String)entry;
	    String[] tokens = line.split("[;]");
	    msg.add(tokens[0]);
	}

	// Write the file
	IcofFile installedFile = getInstalledFile(xContext, getHistoryDir(), 
	                                          compPkg);
	if (isDryRun()) {
	    logInfo(xContext, "[DRYRUN] would have written this \n" + 
	    installedFile.getAbsolutePath(), true);
	    logInfo(xContext, msg.toString(), true);
	    return;
	}

	installedFile.openWrite();
	installedFile.write(msg);
	installedFile.closeWrite();
	logInfo(xContext, "Wrote " + installedFile.getAbsolutePath(), true);

	// Remove the .error file if one exists
	IcofFile errorFile = getErrorFile(xContext, getHistoryDir(), compPkg);
	if (errorFile.exists()) 
	    errorFile.delete();
	
	// If preview install then also log the install for the volume
	if (isPreviewInstall()) {
	    installedFile = getInstalledFile(xContext, getHistoryDir(), compPkg);
	    installedFile.openWrite();
	    installedFile.write(msg);
	    installedFile.closeWrite();
	    logInfo(xContext, "Wrote " + installedFile.getAbsolutePath(), true);

	    errorFile = getErrorFile(xContext, getHistoryDir(), compPkg);
	    if (errorFile.exists()) 
		errorFile.delete();

	}
	
    }

    
    /**
     * Construct the Error file name
     *
     * @param xContext
     * @param compPkg
     * @return
     */
    private IcofFile getErrorFile(EdaContext xContext, IcofFile histDir,
                                  ComponentPackage compPkg) {
	
	String fileName = compPkg.getName() + ERROR_EXT;
	IcofFile file = new IcofFile(histDir.getAbsolutePath() + 
	                             File.separator + fileName, false);
	
	return file;
	
	
    }


    /**
     * Construct the Installed file path
     *
     * @param xContext
     * @param compPkg
     * @return
     */
    private IcofFile getInstalledFile(EdaContext xContext, IcofFile histDir,
				      ComponentPackage compPkg) {

	String fileName = compPkg.getName() + INSTALLED_EXT;
	IcofFile file = new IcofFile(histDir.getAbsolutePath() + 
	                             File.separator + fileName, false);

	return file;
	
    }


    /**
     * Track any errors installing a component package
     *
     * @param xContext
     * @param compPkg
     * @throws IcofException 
     */
    private void logErrorData(EdaContext xContext, ComponentPackage compPkg,
                              String exceptioMsg)
    throws IcofException {

	SimpleDateFormat sim;
	sim = new SimpleDateFormat(IcofDateUtil.TIMESTAMP_FORMAT_LOCK_FILE);
	
	// Create the file contents
	Vector<String> msg = new Vector<String>();
	msg.add("FAILED package install");
	msg.add("-----------------------");
	msg.add("Package: " + compPkg.getName());
	msg.add("Patch level: " + getToolKitPkg().getName());
	msg.add("Install on: " + sim.format(new Date()));
	msg.add("");
	msg.add("Error message");
	msg.add(exceptioMsg);
	msg.add("");
	msg.add("See log file at " + getLogName());


	// Write the file
	IcofFile errorFile = getErrorFile(xContext, getHistoryDir(), compPkg);
	if (isDryRun()) {
	    logInfo(xContext, "[DRYRUN] would have written this \n" + 
	    errorFile.getAbsolutePath(), true);
	    logInfo(xContext, msg.toString(), true);
	    return;
	}
	
	errorFile.openWrite();
	errorFile.write(msg);
	errorFile.closeWrite();
	logInfo(xContext, "Wrote " + errorFile.getAbsolutePath(), true);
	
	// If preview install then also log the install for the volume
	if (isPreviewInstall()) {
	    errorFile = getErrorFile(xContext, getHistoryDir(), compPkg);
	    errorFile.openWrite();
	    errorFile.write(msg);
	    errorFile.closeWrite();
	    logInfo(xContext, "Wrote " + errorFile.getAbsolutePath(), true);
	}
	
    }

    
    /**
     * Verify install contents against the checksum.data file contents
     *
     * @param xContext
     * @param compPkg
     * @throws IcofException 
     */
    private void verifyUnpack(EdaContext xContext, ComponentPackage compPkg)
    throws IcofException {

	logInfo(xContext, "Starting install verification .. ", true);
		
	// Read the checksum.data file
	readChecksumFile(xContext);
	
	// Verify each deliverable can be found and has correct checksum
	String myInstallDir = getDotPackageDir().getParent();
	if (compPkg.getName().startsWith("tgsupport.")) {
	    myInstallDir = getInstallDir().getAbsolutePath();
	}
	for (Object entry : getChecksumFile().getContents()) {
	    
	    String line = (String)entry;
	    logInfo(xContext, " checking .. " + line, verboseInd);
	    
	    String[] tokens = line.split("[;]+");
	    String file = tokens[0];
	    long checksum = Long.parseLong(tokens[1]);
	    String type = tokens[2];
	    
	    IcofFile del = new IcofFile(myInstallDir + File.separator + file, 
	                                false);
	    if (del.exists()) {
		
		long actualChecksum = 0;
		if (del.getFileName().indexOf("*") == -1)
		    actualChecksum = del.getChecksum();
		else
		    // Special cksum processing for files containing * (pwrspice)
		    actualChecksum = PkgUtils.getSpecialChecksum(del, getLogger());
		if (! type.equals("LINK_NUTSHELL") &&
		    actualChecksum != checksum) {
		    throw new IcofException(this.getClass().getName(), "verifyUnpack()",
			                        IcofException.SEVERE, 
			                        "Checksum mismatch - packaged " +
			                        "checksum for this deliverable " +
			                        "does not match the installed " +
			                        "checksum.",
			                        "File: " + del.getAbsolutePath());
		}
		
	    }
	    else {
		
		// If its a nutsh link then it will not exist so only
		// throw an error if it's not a nutshell link.
		if (! type.equals("LINK_NUTSHELL")) {
		    throw new IcofException(this.getClass().getName(), "verifyUnpack()",
		                            IcofException.SEVERE, 
		                            "Unable to find deliverable in install",
		                            "File: " + del.getAbsolutePath());
		}
		
	    }
	    
	}
	
	logInfo(xContext, " Verification completed successfully", true);
	
    }




    /**
     * Read the .packaging/checksum.data file in the specified directory
     *
     * @param xContext Application context
     * @param aDir     Directory that should contain .packaging or _packaging dir
     * @throws IcofException 
     */
    private void readChecksumFile(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, " Reading checksum file ... ", verboseInd);
	
	checksumFile = new IcofFile(getDotPackageDir().getAbsolutePath() + 
	                            File.separator + "checksum.data", false);
	
	checksumFile.openRead();
	checksumFile.read();
	checksumFile.closeRead();
	
    }


    /**
     * Create the tk14.1.6z.0 symlinks at the top install level
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void createInstallSymlinks(EdaContext xContext)
    throws IcofException {

	// Do nothing if user specified -nolinks
	if (isNoTopLinks()) {
	    logInfo(xContext, "No top level TK links created", true);
	    return;
	}
	
	if (isAsicsInstall())
	    createInstallSymlinks_asics(xContext);
	else
	    createInstallSymlinks_server(xContext);

    }


    /**
     * Create the tk14.1.6z.0 symlinks at the top install level
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void createInstallSymlinks_server(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "\nCreating install PLI links ...", true);
	logInfo(xContext, " Tool Kit Pkg: |" + getToolKitPkg().getName() + "|", true);

	String pliTk = getPliTkName(xContext, false);
	String tkName = "tk" + getToolKit().getName();
	
	// Create .. tk14.1.6z.0 -> .tk14.1.6z.0-64-@sys
	String existingFile = "." + pliTk + "-64-@sys";
	String newFile = pliTk;
	makeSymlink(xContext, existingFile, newFile, getTopLevelDir());
	
	
	// Create .tk14.1.6z.0-64-rs_aix61 ->
	// aix64b/tk14.1.6z/tk14.1.6z.0_aix64b
	// for all platforms
    /*	
	for (int i = 0; i < PkgUtils.PLATFORMS.size(); i++) {
	    existingFile = PkgUtils.PLATS.get(i) + File.separator + 
	                   tkName + File.separator + 
		           pliTk + "_" + PkgUtils.PLATS.get(i);
	    newFile = "." + pliTk + "-" + PkgUtils.PLATFORMS.get(i);
	    makeSymlink(xContext, existingFile, newFile, getTopLevelDir());
	}
	*/
	
	//navechan:
	// Create original links for 14.1
	// Create only linux7.1 and plinx links for 15.1
	if(getToolKit().getName().startsWith("14.1")){
		// create links for 14.1 install
		logInfo(xContext, "\nToolkit release is 14.1, Creating Linux(RH5 & RH6) and AIX Links", true);
		for (int i = 0; i < PkgUtils.PLATFORMS.size(); i++) {
		    existingFile = PkgUtils.PLATS.get(i) + File.separator + 
		                   tkName + File.separator + 
			           pliTk + "_" + PkgUtils.PLATS.get(i);
		    newFile = "." + pliTk + "-" + PkgUtils.PLATFORMS.get(i);
		    makeSymlink(xContext, existingFile, newFile, getTopLevelDir());
		}
	}else
	if(getToolKit().getName().startsWith("15.1")){
		logInfo(xContext, "\nToolkit release is 15.1, Creating Linux(RH6) and P-LINUX(RH7) Links", true);
		//create links for 15.1 install
		for (int i = 0; i < PkgUtils.PLATFORMS_151.size(); i++) {
		    existingFile = PkgUtils.PLATS_151.get(i) + File.separator + 
		                   tkName + File.separator + 
			           pliTk + "_" + PkgUtils.PLATS_151.get(i);
		    newFile = "." + pliTk + "-" + PkgUtils.PLATFORMS_151.get(i);
		    makeSymlink(xContext, existingFile, newFile, getTopLevelDir());
		}
	}
		
	// Create tk14.1.6zlatest -> tk14.1.6z.0
	String tkLatestDir = "tk" + getToolKit().getName() + "latest";
	IcofFile latest = new IcofFile(getTopLevelDir() + File.separator
				       + tkLatestDir, true);

	// Remove latest if it exists
	if (latest.exists()) {
	    latest.remove(false);
	}

	// Create the new latest link
	existingFile = pliTk;
	newFile = tkLatestDir;
	makeSymlink(xContext, existingFile, newFile, getTopLevelDir());

    }
    
    
    /**
     * Create the external install links
     *	- /afs/btv/data/edatools/tool_preview/tk18.1.8e.0_aix64b ..
     *  - /afs/btv/data/edatools/tk18.1.8e.0_aix64b ..
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void createInstallSymlinks_asics(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "\nCreating install PLI links ...", true);
	logInfo(xContext, " Tool Kit Pkg: |" + getToolKitPkg().getName() + "|", true);
	
	// Skip if ICC install
	if (getSiteName().equalsIgnoreCase("icc")) {
	    logInfo(xContext, "Ignoring PLI link creation for ICC install", true);
	    return;
	}
	
	// Set the real top level dir
	IcofFile trueTopDir;
	trueTopDir = new IcofFile(getTopLevelDir().getAbsolutePath() + "/../../..", 
	                           true);

	// Set the tool_preview dir
	IcofFile toolPreviewDir;
	toolPreviewDir = new IcofFile(trueTopDir.getAbsolutePath() + 
	                              File.separator + "toolkit_preview", 
	                              true);

	
	// Create the PLI links 
	String pliTk = getPliTkName(xContext, false);
	String tkName = "tk" + getToolKit().getToolKit().getAltDisplayName();

	// Create the links
	for (Platform_Db plat : getPlatforms()) {
	    String existingFile = "ptn" + File.separator +
	                           plat.getShortInstallName() + File.separator +
	                           tkName + File.separator;
	    // Create link to volA only if patch == 0
	    existingFile += pliTk + "_";
	    existingFile += plat.getShortInstallName();	
	    String newFile = pliTk + "_" + plat.getShortInstallName();
	    makeSymlink(xContext, existingFile, newFile, trueTopDir);
	    existingFile = ".." + File.separator + existingFile;
	    makeSymlink(xContext, existingFile, newFile, toolPreviewDir);
	}

    }
    
    
    /**
     * Construct the tk14.1.8.0 name
     * 
     * @param xContext Application context
     * @return
     */
    private String getPliTkName(EdaContext xContext, boolean bPrevToolkit) {

	int pliVersion = Integer.parseInt(getToolKitPkg().getName());
	if (bPrevToolkit && (pliVersion != -1))
	    pliVersion--;
	
	String name = "tk";
	if (isAsicsInstall())
	    name += getToolKit().getToolKit().getAltDisplayName();
	else
	    name += getToolKit().getName();
	name += "." + pliVersion;
	
	return name;
	
    }


    /**
     * Create the symlink (newFile) that points to existingFile in aDir
     * 
     * @param xContext Application context
     * @param existingFile Existing file name
     * @param newFile New file name (to be a link to existing)
     * @param installDir2 Directory to create this sym link
     * @throws IcofException
     */
    private void makeSymlink(EdaContext xContext, String existingFile,
			     String newFile, IcofFile aDir)
    throws IcofException {

	// Don't create if already exists (internal installs only)
	File file = new File(getTopLevelDir() + File.separator + newFile);
	if (file.exists() && ! isAsicsInstall())
	    return;

	String[] command = new String[4];
	command[0] = "ln";
	command[1] = "-sf";
	command[2] = existingFile;
	command[3] = newFile;

	int rc = PkgUtils.runCommandInDir(command, aDir, isDryRun(), getLogger());
	if (rc == 0) {
	    logInfo(xContext, " Symlink created " + newFile + " -> "
			      + existingFile, verboseInd);
	}

    }


    /**
     * Run the component level post install process
     * 
     * @param xContext Application context
     * @param compPkg
     * @throws IcofException 
     * @throws SQLException 
     * @throws IOException 
     */
    private void postInstall(EdaContext xContext, ComponentPackage compPkg) 
    throws IcofException {

	logInfo(xContext, " Post install .. ", true);
	
	generateRelNotes(xContext, compPkg);
	
	if (getCompName().equals("tgsupport")) {
	    logInfo(xContext, "Updating Gna Init Tcl file in " + 
	            getInstallCompRelDir(), verboseInd);
	    String msg = InstallUtils.updateGnaInitTcl(getInstallCompRelDir(), 
	                                               getToolKit(), 
	                                               getToolKitPkg(),
	                                               isAsicsInstall(), 
	                                               isDryRun());
	    logInfo(xContext, msg, verboseInd);
	}
	
	else if (getCompName().equalsIgnoreCase("theguide")) {
	    logInfo(xContext, "Udating Gna Install in " + getInstallCompRelDir(), 
	            verboseInd);
	    String msg = InstallUtils.updateGnaInstall(getInstallCompRelDir(), 
	                                               getSiteName(),
	                                               isAsicsInstall(), isDryRun(),
	                                               getLogger());
	    logInfo(xContext, msg, verboseInd);
	}

	else if (getCompName().startsWith("mar2")) {
	    createIncrementalLinks(xContext, compPkg);
	}

    }


    /**
     * Unpack the specified component package
     * 
     * @param xContext Application context
     * @param compPkg Comp package to unpack
     * @throws IcofException
     */
    private void unpackCompPackage(EdaContext xContext, ComponentPackage compPkg)
    throws IcofException {

	// Create the new install dirs if they don't exist
	getInstallCompRelDir().mkdirs();
	
	// Define the package names
	String gzPkgName = compPkg.getName() + ".tar.gz";
	String tarPkgName = compPkg.getName() + ".tar";
	
	
	// Uncompress the compressed package in the package dir
	IcofFile gzPkg = new IcofFile(getPackageDir().getAbsolutePath() + 
	                              File.separator + gzPkgName, false);
	IcofFile tarPkg = new IcofFile(getPackageSiteDir().getAbsolutePath() + 
	                               File.separator + tarPkgName, false);
	logInfo(xContext, " Uncompressing \n  " + gzPkg.getAbsolutePath() + 
	        "\n  into " + 
	        tarPkg.getAbsolutePath(), true);
	if (isDryRun())
	    logInfo(xContext, "[DRYRUN] uncompress not actually run", true);
	else
	    gzPkg.unCompress(tarPkg);

	
	// Unpack the tar file
	String[] command = new String[3];
	command[0] = "tar";
	command[1] = "-xvf";
	command[2] = tarPkg.getAbsolutePath();
	String reply = "";
	int rc = PkgUtils.runCommandInDir(command, getInstallDir(), 
	                                  isDryRun(), getLogger());
	logInfo(xContext, reply, verboseInd);
	if (rc == 0) {
	    logInfo(xContext, " Extraction complete", verboseInd);
	}

	// Set the location of the .package directory
	setDotPackageDir(xContext, tarPkg);
	
	// Remove the tar file
	tarPkg.delete();
	
    }


    /**
     * Define the location of the .package directory
     *
     * @param xContext
     * @param tarPkg
     * @throws IcofException 
     */
    private void setDotPackageDir(EdaContext xContext, IcofFile tarPkg) 
    throws IcofException {

	// Look in the default location
	dotPackageDir = new IcofFile(getInstallCompRelDir().getAbsolutePath() + 
	                             File.separator + ".package", 
	                             true);
	if (dotPackageDir.exists())
	    return;

	// If not in default location then read it from contents of tar file
	String command = "tar -tf " + tarPkg.getAbsolutePath();
	StringBuffer errors = new StringBuffer();
	Vector<String> results = new Vector<String>();
	int rc = IcofSystemUtil.execSystemCommand(command, errors, results);
	if (rc != 0) {
	    throw new IcofException(this.getClass().getName(), 
	                            "setDotPackageDir()", IcofException.SEVERE, 
	                            "Unable to get package contents with tar -tf", 
	                            "Package: " + tarPkg.getAbsolutePath());
	}
	
	String dotPackageDirName = "";
	for (String line : results) {
	    if (line.endsWith(".package/")) {
		dotPackageDirName = line;
		break;
	    }
	}
	
	if (dotPackageDirName.equals("")) {
	    throw new IcofException(this.getClass().getName(), 
	                            "setDotPackageDir()", IcofException.SEVERE, 
	                            "Unable to locate .package in tar package", 
	                            "Package: " + tarPkg.getAbsolutePath());
	}

	dotPackageDir = new IcofFile(getInstallDir() + File.separator + 
	                             dotPackageDirName, true);
	
    }


    /**
     * Set the install tool kit platform directory
     * /afs/btv/data/vlsi/eclipz/common/tools/edatools/tk14.1.6/
     *  aix64b/tk14.1.6z/tk14.1.6z.0_aix64b 
     *  - or -
     * $topLevelDir/aix64b/tk14.1.6z/tk14.1.6z.0_aix64b
     *  - or -
     * $topLevelDir/aix64b/tk14.1.6z/tk14.1.6z.0_aix64b/mar2/mar2.1401.0/mar2/14.1
     * 
     * @param xContext Application context
     * @param aPkg Component package to be installed in this dir
     * @throws IcofException 
     */
    private void setInstallDirs(EdaContext xContext, ComponentPackage aPkg)
    throws IcofException {
	
	// Get the install dir platform
	String installPlat = "aix64b";
	aPkg.getPlatform().dbLookupById(xContext);
	if (aPkg.getPlatform().getOs().equalsIgnoreCase("LINUX")) {
		logInfo(xContext, "OS from Db is 'LINUX'",true);
	    installPlat = "lin64b_x86";
	}else // navechan: added to support p-linux
	if (aPkg.getPlatform().getOs().equalsIgnoreCase("PLINUX")) {
		logInfo(xContext, "OS from Db is 'PLINUX'",true);
		installPlat = "lin64b_ppc";
	}
	logInfo(xContext, "Install Platform: "+installPlat,true);
	

	// Set the top level directory
	// for example ... /afs/eda.fishkill.ibm.com/data/edatools/ptn/aix64b/tk18.1.8e
	setTopLevelDir(xContext, installPlat);
	setHistoryDir(xContext);
	
	// Get the tk sub dir
	String tkName ="tk" + getToolKit().getName();
	if (isAsicsInstall())
	    tkName = "tk" + getToolKit().getToolKit().getAltDisplayName();


	String installDirName = getTopLevelDir().getAbsolutePath() + 
	                        File.separator;

	if (! isAsicsInstall()) {
	    installDirName += installPlat + File.separator + 
	    tkName + File.separator;
	}
	installDirName += getPliTkName(xContext, false) + "_" + installPlat;
	

	// Determine if we are dealing with an incremental mar2* install
	installDir = new IcofFile(installDirName, true);
	logInfo(xContext, " Install dir (initial): " + getInstallDir().getAbsolutePath(),
		true);
	String incrementName = setIncrementalInstall(xContext, aPkg);
	setIncrementPath(incrementName);
	
	// Define the install and comp/rel directories
	if (isIncrementalInstall()) {
	    installDirName += File.separator + 
	                      getCompName() + File.separator + 
	                      incrementName;
	}
	String compRelDir = installDirName + File.separator;
	if (getCompName().equalsIgnoreCase("tgsupport")) {
	    compRelDir += "theguide_support";
	}
	else {
	    compRelDir += getCompName() + File.separator;
	    compRelDir += getToolKit().getToolKit().getRelease().getName();
	}
	
	// Create the install directory
	installDir = new IcofFile(installDirName, true);
	getInstallDir().mkdirs();
	logInfo(xContext, " Install dir (final): " + getInstallDir().getAbsolutePath(),
		true);

	installCompRelDir = new IcofFile(compRelDir, true);
	logInfo(xContext, " Comp/Rel dir: " + compRelDir, true);
	
    }
    

    /**
     * Creates the history directory for this tool kit package
     *
     * @param xContext Application context
     */
    private void setHistoryDir(EdaContext xContext) {

	if (getHistoryDir() != null)
	    return;

	String dirName = InstallUtils.getHistoryDirName(getTopLevelDir(), 
	                                                getToolKitPkg(),
	                                                getToolKit());
	historyDir = new IcofFile(dirName, true);
	
	if (! getHistoryDir().exists())
	    getHistoryDir().mkdirs();
	
	logInfo(xContext, " History dir: " + 
	        getHistoryDir().getAbsolutePath(), true);
	
    }


     
    /**
     * Set the install top level directory
     * /afs/btv/data/vlsi/eclipz/common/tools/edatools/tk14.1.6z
     * 
     * @param xContext
     */
    private void setTopLevelDir(EdaContext xContext, String installPlatform) {

	topLevelDir = InstallUtils.constructTopInstallDir(xContext, getToolKit(), 
	                                                  isAsicsInstall(),
	                                                  getSiteName(), 
	                                                  installPlatform);

	logInfo(xContext, " Top Level dir: " +
                getTopLevelDir().getAbsolutePath(), true);

    }


    /**
     * Set the location to find the packages
     * 
     * @param xContext
     * @throws IcofException
     */
    private void setPackageDirs(EdaContext xContext)
    throws IcofException {

	// Set the package dir
	packageDir = PkgUtils.getPkgPackagesDir(getToolKit());
	getPackageDir().validate(false);
	
	logInfo(xContext, "Package dir: " + getPackageDir().getAbsolutePath(), 
	        verboseInd);
	
	// Set the type_site specific package dir for uncompressing files
	String pkgSiteDirName = packageDir.getAbsolutePath();
	pkgSiteDirName += File.separator;
	if (isAsicsInstall())
	    pkgSiteDirName += "asics_";
	else 
	    pkgSiteDirName += "server_";
	pkgSiteDirName += getSiteName(); 
	
	packageSiteDir = new IcofFile(pkgSiteDirName, true);
	if (! packageSiteDir.exists())
	    packageSiteDir.mkdirs();
	
	
	logInfo(xContext, "Package site dir: " + getPackageSiteDir().getAbsolutePath(), 
	        verboseInd);

    }


    /**
     * Find a tool kit to install
     * 
     * @param xContext
     * @throws IcofException
     * @throws IOException 
     * @throws SQLException 
     */
    private void setToolKitPackage(EdaContext xContext)
    throws IcofException, IOException, SQLException {
	
	// Return if TK package set from command line
	if (getToolKitPkg() != null) {
	    setPreviewInstall(xContext);
	    return;
	}

	logInfo(xContext, "Querying for install ready TK Package ...", true);
	
	// Look for unfinished installs
	boolean bFound = setTkPkgInState(xContext, "PKG_INSTALL_READY");

	// If no unfinished installs then look for a new one
	if (! bFound) {
	    bFound = setTkPkgInState(xContext, "PKG_NEW");
	    if (bFound) {
		//createPliLinks(xContext);
		setTkPkgEvent(xContext, "PKG_INSTALL_READY", 
		              "Initial PLI install");
	    }
	}
	else {
	    logInfo(xContext, "REINSTALL detected .. skipping patch level " +
	            "link creation", true);
	}

	// No package found
	if (getToolKitPkg() == null) {
	    logInfo(xContext, "\nNo Tool Kit Packages found to install\n", 
	            true);
	}
	else {
	    logInfo(xContext, "Tool Kit Package: " + getToolKitPkg().getName(), 
	            true);
	}
	
	setPreviewInstall(xContext);
	
    }


    /**
     * Determine if this install is for a preview tool kit
     *
     * @param xContext
     * @throws IcofException 
     */
    private void setPreviewInstall(EdaContext xContext) throws IcofException {

	setPreviewInstall(false);
	
	logInfo(xContext, "Tool Kit Package: " + getToolKitPkg().getName(), 
	            true);
	
	if (getToolKit().getStageName().getName().equals(StageName_Db.STAGE_PREVIEW)) {
	    logInfo(xContext, "Preview Tool Kit package .. detected", true);
	    setPreviewInstall(true);
	    setNoTopLinks(true);
	    
	    // Set the top links for EDA site only
	    if (getSiteName().equalsIgnoreCase("eda"))
		setNoTopLinks(false);
	 
	    logInfo(xContext, " No top links: " + isNoTopLinks(), true);
	    
	}
	
    }


    /**
     * Create the PLI symlinks in the new tool kit version
     *
     * @param xContext Application context
     * @throws IcofException 
     * @throws IOException 
     */
    private void createPliLinks(EdaContext xContext) 
    throws IcofException, IOException {

	logInfo(xContext, "Creating PLI links ... please be patient", true);
	InstallPrep prep = new InstallPrep(xContext, getToolKit(), 
	                                   getToolKitPkg(), getSiteName(), 
	                                   getComponent(), isAsicsInstall());
	prep.prepIt(xContext);
	
    }

    
    /**
     * Generate release notes
     *
     * @param xContext Application context
     * @param compPkg  Component plg being installed
     * @throws IcofException 
     */
    private void generateRelNotes(EdaContext xContext, ComponentPackage compPkg) 
    throws IcofException {

	logInfo(xContext, "Generating release notes ... please be patient", true);
	
	// Determine if rel notes need to be generated for this platform
	if (getGenRelNotes().contains(compPkg.getPlatform().getName())) {
	    logInfo(xContext, " Already generated for " + 
	            compPkg.getPlatform().getName(), true);
	    return;
	}
	
	// Get the correct TK name based on server vs asics install
	String tkName = getToolKit().getName();
	if (isAsicsInstall())
	    tkName = getToolKit().getToolKit().getAltDisplayName();
	
	// Generate the rel notes
	PackageShowRelNotes app = new PackageShowRelNotes(xContext, 
	                                                  tkName, 
	                                                  getToolKitPkg(),
	                                                  getInstallDir(),
	                                                  true);
	String readmeFile = app.generate(xContext);
	logInfo(xContext, " README file: " + readmeFile, true);
	
	// Update the generated rel notes list so we don't create rel notes 
	// for this platform again
	generatedRelNotes.add(compPkg.getPlatform().getName());
	logInfo(xContext, " Added " + compPkg.getPlatform().getName() +
	        " to generated rel notes collection", true);
	
    }


    /**
     * Set the TK Package Event
     * 
     * @param xContext Application context
     * @param state New event state
     * @param comment Event comments
     * @throws IcofException
     */
    private void setTkPkgEvent(EdaContext xContext, String state, String comment)
    throws IcofException {

	// Skip if test run
	if (isDryRun()) {
	    logInfo(xContext, " State not updated - " + getToolKitPkg().getName(), 
	            verboseInd);
	    return;
	}
	
	EventName unpackState = new EventName(xContext, state);
	unpackState.dbLookupByName(xContext);
	
	getToolKitPkg().updateState(xContext, unpackState, 
	                            comment, getUser());

	logInfo(xContext, "\nState updated to " + state + " " +
			"for this Tool Kit Package", verboseInd);
	
    }
    
    
    /**
     * Determine the valid platforms for this tool kit
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void setPlatforms(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "\nDetermining Platforms ...", true);

	// Look up the supported platforms for this tool kit to determine
	// .ship-<platform> directories
	
	PlatformShow allPlats = new PlatformShow(xContext, getToolKit());
	allPlats.setPlatforms(xContext);
	for (Platform_Db plat : allPlats.getPlatforms()) {
	    getPlatforms().add(plat);
	    logInfo(xContext, " Ship platform: " + plat.getShippingName(), 
	            getVerboseInd(xContext));
	}
	
    }
        

    /**
     * Set the Component Package Event
     * 
     * @param xContext Application context
     * @param pkg ComponentPackage to update
     * @param state New event state
     * @param comment Event comments
     * @throws IcofException
     */
    private void setCompPkgEvent(EdaContext xContext, ComponentPackage pkg,
				 String state, String comment)
    throws IcofException {

	// Skip if test run
	if (isDryRun()) {
	    logInfo(xContext, " State not updated - " + pkg.getName(), 
	            verboseInd);
	    return;
	}
	
	// Set the event aka update the state
	EventName unpackState = new EventName(xContext, state);
	unpackState.dbLookupByName(xContext);

	pkg.updateState(xContext, unpackState, comment, getUser());

	logInfo(xContext, " State updated to " + state + " - " + pkg.getName(), 
	        verboseInd);

    }


    /**
     * Look for a tool kit package in the given state
     * 
     * @param xContext Application context
     * @param aState Desired event state
     * @return
     * @throws IcofException
     */
    private boolean setTkPkgInState(EdaContext xContext, String aState)
    throws IcofException {

	EventName tkPkgEvent = new EventName(xContext, aState);
	tkPkgEvent.dbLookupByName(xContext);

	if (getToolKitPkg() == null)
	    toolKitPkg = new ToolKitPackage(xContext);
	boolean bFound = getToolKitPkg().dbLookupLatestInState(xContext, 
	                                                       getToolKit(), 
	                                                       tkPkgEvent);

	if (! bFound) {
	    logInfo(xContext, "No TK Package found in " + aState + " state", 
	            verboseInd);
	}
	else {
	    logInfo(xContext, "Found TK Package in " + aState + " state", 
	            verboseInd);
	    logInfo(xContext, " TK Package: " + getToolKitPkg().getName(), 
	            verboseInd);
	}
	
	return bFound;

    }


    /**
     * Create the new 14.1 symlink to the latest increment mar2* install
     *
     * @param xContext Application context
     * @param platform Platform being processed 
     * @param compName Component being processed
     * @param lastInc  Latest incremental install version
     * @throws IcofException 
     */
    private void createIncrementalLinks(EdaContext xContext, ComponentPackage aPkg) 
			       throws IcofException {

	// Skip if not an incremental install
	if (! isIncrementalInstall())
	    return;

	logInfo(xContext, "  Creating incremental links", true);
	

	
	// Need to create a nutshell link in the new incremental install dir
	// (in /afs/btv.ibm.com/data/vlsi/eclipz/common/tools/edatools/tk14.1.8e/
	// lin64b_x86/tk14.1.8e/tk14.1.8e.1_lin64b_x86/mar2/mar2.1401.0)
	String command = "";

	// Run only if incremental install or if initial inc install
	if (isIncrementalInstall() || getIncrementPath().indexOf(".0") > -1) {
	    logInfo(xContext, "  Creating nutshell link ...", true);
	    command = "/bin/ln -sf ../../nutshell";
	    PkgUtils.runCommandInDir(command, getInstallDir(), isDryRun(), 
	                             getLogger());
	}	
	
	// The install dir is ...
	// $topLevelDir/lin64b_x86/tk14.1.6z/tk14.1.6z.3_lin64b_x86/mar2/mar2.1401.3
	//
	// Here's where the new 14.1 link needs to be created
	// $topLevelDir/lin64b_x86/tk14.1.6z/tk14.1.6z.0_lin64b_x86/mar2
	if (isIncrementalInstall() || getIncrementPath().indexOf(".0") > -1) {
	    logInfo(xContext, "  Creating " + 
	            getToolKit().getToolKit().getRelease().getName() + 
	            " link ...", true);
	    IcofFile parent = new IcofFile(getInstallDir().getParent(), true);

	    String existing = getIncrementPath() + File.separator +
	    getCompName() + File.separator +
	    getToolKit().getToolKit().getRelease().getName();

	    command = "/bin/ln -sf " + existing;
	    PkgUtils.runCommandInDir(command, parent, isDryRun(), getLogger());
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

	if (params.containsKey("-t")) {
	    String pkgName = (String) params.get("-t");
	    if (IcofStringUtil.occurrencesOf(pkgName, ".") == 3) {
		setToolKitPackage(xContext, pkgName);
	    }
	    else if (IcofStringUtil.occurrencesOf(pkgName, ".") == 2) {
		setToolKit(xContext, pkgName);
	    }
	    else {
		errors += "Tool kit (-t) swithc name must be specify a \n" +
		          "Tool Kit (x.y.z) or Tool Kit maintenance (x.y.z.a).\n";
	    }
	}
	else {
	    errors += "Tool kit/maintenance name (-t) is a required parameter\n";
	}
	
	// Read the component
	if (params.containsKey("-c")) {
	    setComponent(xContext, (String) params.get("-c"));
	}
	
	// Read the force action
	setForceInstall(false);
	if (params.containsKey("-force")) {
	    setForceInstall(true);
	}

	// Read the dryrun action
	setDryRun(false);
	if (params.containsKey("-dryrun")) {
	    setDryRun(true);
	}

	// Read the prep action
	setPrepInstall(false);
	if (params.containsKey("-prep")) {
	    setPrepInstall(true);
	}

	// Read the nolinks action
	setNoTopLinks(false);
	if (params.containsKey("-nolinks")) {
	    setNoTopLinks(true);
	}
	
	// Read the internal/external switch
	if (params.containsKey("-asics")) {
	    setAsicsInstall(true);
	    setSiteName((String)params.get("-asics"));
	    setSiteName(getSiteName().toLowerCase());
	}
	else if (params.containsKey("-server")) {
	    setAsicsInstall(false);
	    setSiteName((String)params.get("-server"));
	    setSiteName(getSiteName().toLowerCase());
	}
	else {
	    errors += "Install type (-server or -asics) is a required parameter\n";
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
	String tkName = aName.substring(0, aName.lastIndexOf("."));
	setToolKit(xContext, tkName);

	logInfo(xContext,
	        "Tool Kit: " + getToolKit().getName(), true);

	// Set the maintenance
	String maintName = aName.substring(aName.lastIndexOf(".") + 1);

	if (getToolKitPkg() == null) {
	    toolKitPkg = new ToolKitPackage(xContext, maintName);
	    try {
		// TK Pkg may not exist in the DB
		getToolKitPkg().dbLookupByName(xContext, getToolKit());
	    }
	    catch(IcofException ignore) {}
	}

	logInfo(xContext, "Tool Kit Package: " + getToolKitPkg().getName(), 
	        true);
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
	singleSwitches.add("-force");
	singleSwitches.add("-dryrun");
	singleSwitches.add("-prep");
	singleSwitches.add("-nolinks");
	argSwitches.add("-server");
	argSwitches.add("-c");
	argSwitches.add("-db");
	argSwitches.add("-asics");
	argSwitches.add("-t");

	
    }


    /**
     * Display application's invocation
     * 
     * @param dbMode Database model
     * @param xContext Application context object
     */
    protected void displayParameters(String dbMode, EdaContext xContext) {

	logInfo(xContext, "App       : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	logInfo(xContext, "Tool Kit  : " + getToolKit().getName(), verboseInd);
	if (getComponent() != null)
	    logInfo(xContext, "Component : " + getComponent().getName(), 
	            verboseInd);
	logInfo(xContext, "Force     : " + isForceInstall(), verboseInd);
	logInfo(xContext, "Dry Run   : " + isDryRun(), verboseInd);	
	logInfo(xContext, "Prep mode : " + isPrepInstall(), verboseInd);
	logInfo(xContext, "No top links: " + isNoTopLinks(), verboseInd);
	logInfo(xContext, "ASICs inst: " + isAsicsInstall(), verboseInd);
	if (getSiteName() != null)
	    logInfo(xContext, "Site      : " + getSiteName(), verboseInd);
	logInfo(xContext, "DB Mode   : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose   : " + getVerboseInd(xContext),
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
	usage.append("Install the NEW Tool Kit Packages as patch level installs.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t toolkit | tk maint> <-server site | -asics site>\n");
	usage.append("            [-c component] [-force] [-prep] [-nolinks]\n");
	usage.append("            [-y] [-h] [-dryrun]\n");
	usage.append("\n");
	usage.append("  toolkit   = Tool kit or tk main name (ie, 14.1.8e or 14.1.8e.1 ...)\n");
	usage.append("  -server   = Specifies this install is for server customers \n");
	usage.append("  -asics    = Specifies this install is for ASICs customers\n");
	usage.append("  site      = Which Server/ASICs customer site (BTV, EDA, ICC, POK ...)\n");
	usage.append("  component = Install pkgs for component only\n");
	usage.append("  -prep     = Create the PLI symlinks (ie, runs pkg.installPrep)\n");
	usage.append("  -nolinks  = Do NOT create the top tool kit symlinks\n");
	usage.append("  -force    = Install/reinstall all components in the currect " +
			            "tool kit package even if previsously installed\n");
	usage.append("              Default is to only install pkgs not already installed\n");
	usage.append("  -dryrun   = Don't actuall install or update anything\n");
	usage.append("  -y        = Verbose mode (echo messages to screen)\n");
	usage.append("  -h        = Help (shows this information)\n");
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
    private IcofFile topLevelDir;
    private IcofFile installDir;
    private IcofFile packageDir;
    private IcofFile packageSiteDir;
    private ToolKitPackage toolKitPkg;
    private IcofFile checksumFile;
    private IcofFile installCompRelDir;
    private IcofFile dotPackageDir;
    private IcofFile historyDir;
    private boolean forceInstall = false;
    private String componentName;
    private String incrementPath;
    private String siteName;
    private boolean asicsInstall = false;
    private boolean serverInstall = false;
    private boolean dryRun = false;
    private boolean previewInstall = false;
    private Vector<String> generatedRelNotes = new Vector<String>();
    private boolean prepInstall = false;
    private boolean noTopLinks = false;
    private ArrayList<Platform_Db> platforms = new ArrayList<Platform_Db>();
    

    /**
     * Getters
     */
    public IcofFile getTopLevelDir() { return topLevelDir; }
    public IcofFile getInstallDir() { return installDir; }
    public IcofFile getPackageDir() { return packageDir; }
    public IcofFile getPackageSiteDir() { return packageSiteDir; }
    public ToolKitPackage getToolKitPkg() { return toolKitPkg; }
    public IcofFile getChecksumFile() { return checksumFile; }
    public IcofFile getInstallCompRelDir() { return installCompRelDir; }
    public IcofFile getDotPackageDir() { return dotPackageDir; }
    public IcofFile getHistoryDir() { return historyDir; }
    public boolean isForceInstall() { return forceInstall; }
    public String getCompName() { return componentName; }
    public String getIncrementPath() { return incrementPath; }
    public String getSiteName() { return siteName; }
    public boolean isIncrementalInstall() { return serverInstall; }
    public boolean isAsicsInstall() { return asicsInstall; }
    public boolean isDryRun() { return dryRun; }
    public boolean isPrepInstall() { return prepInstall; }
    public boolean isPreviewInstall() { return previewInstall; }
    public boolean isNoTopLinks() { return noTopLinks; }
    public Vector<String> getGenRelNotes() { return generatedRelNotes; }
    public ArrayList<Platform_Db> getPlatforms()  { return platforms; }
    

    /**
     * Setters
     */
    private void setInstallDir(IcofFile aDir) { installDir = aDir; }
    private void setForceInstall(boolean aFlag) { forceInstall = aFlag; }
    private void setIncrementPath(String aPath) { incrementPath = aPath; }
    private void setSiteName(String aName) { siteName = aName; }
    private void setIncrementalInstall(boolean aFlag) { serverInstall = aFlag; }
    private void setAsicsInstall(boolean aFlag) { asicsInstall = aFlag; }
    private void setDryRun(boolean aFlag) { dryRun = aFlag; }
    private void setPrepInstall(boolean aFlag) { prepInstall = aFlag; }
    private void setPreviewInstall(boolean aFlag) { previewInstall = aFlag; }
    private void setNoTopLinks(boolean aFlag) { noTopLinks = aFlag; }
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }
    // @formatter:on


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {

	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }

}
