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
 * Prepare the install area for the next patch level install 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 03/04/2014 GFS  Initial coding.
 * 03/16/2014 GFS  Updated nutshell process to copy files and tgsupport to 
 *                 handle alternate theguide_support name.
 * 04/11/2014 GFS  Updated to support ASIC installs
 * 06/19/2014 GFS  Updated to use native java logging
 * 07/28/2014 GFS  Updated to support site specific server installs
 * 12/29/2014 GFS  Updated to support patch level install of preview TKs
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree.packaging;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.app.etree.PlatformShow;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Platform_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKitPackage;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofStringUtil;
import com.ibm.stg.iipmds.common.IcofSystemUtil;

public class InstallPrep extends TkAppBase {

    /**
     * Constants
     */
    public static final String APP_NAME = "pkg.installPrep";
    public static final String APP_VERSION = "v1.2";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aTk  Tool kit to prep for install
     */
    public InstallPrep(EdaContext aContext, ToolKit aTk, ToolKitPackage aTkPkg,
                       String aSiteName, Component aComp, boolean bExternal)
                       throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setToolKit(aTk);
	setToolKitPackage(aTkPkg);
	setSiteName(aSiteName);
	setComponent(aComp);
	setAsicsInstall(bExternal);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * @exception IcofException Unable to construct ManageApplications object
     */
    public InstallPrep(EdaContext aContext) throws IcofException {

	this(aContext, null, null, null, null, false);

    }


    /**
     * Instantiate class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new InstallPrep(null);
	    start(myApp, argv, InstallPrep.class.getName(), APP_NAME);
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
    throws IcofException, IOException {

	connectToDB(xContext);

	prepIt(xContext);

	commitToDB(xContext, APP_NAME);

    }


    /**
     * Run the install process
     * 
     * @param aContext Application Context
     * @throws IcofException
     * @throws IOException 
     * @throws SQLException 
     */
    public void prepIt(EdaContext xContext) throws IcofException, IOException {

	// Process each platform
	setPlatforms(xContext);
	for (Platform_Db installPlat : getPlatforms()) {
	    
	    String installPlatName = installPlat.getShortInstallName();
	    logInfo(xContext, "Platform: " + installPlatName, true);

	    // Create a list of components in the tool kit, platform and 
	    // install type
	    componentNames = InstallUtils.setDeliveredComps(xContext, 
	                                                    getToolKit(), 
	                                                    installPlat, 
	                                                    getComponent(), 
	                                                    isAsicsInstall());
	    logInfo(xContext, "Component count: " + componentNames.size(), true);

	    setTopLevelDir(xContext, installPlatName);

	    
	    // For each component prepare the new install location
	    for (String compName : getComponentNames()) {
		logInfo(xContext, "\n\nComponent: " + compName, true);

		IcofFile prevDir = setInstallDirs(xContext, compName,
		                                  installPlatName, null, true);
		IcofFile newDir = setInstallDirs(xContext, compName,
		                                 installPlatName, null, false);

		// Process mar2* incremental installs only for internal installs
		if (! isAsicsInstall() && compName.startsWith("mar2")) {

		    logInfo(xContext, "\nIncremental install detected .. ", 
		            true);

		    // Determine existing incremental installs
		    Vector<String> increments;
		    increments = PkgUtils.readIncrements(prevDir, compName, true);

		    // Process each incremental install
		    for (String increment : increments) {
			logInfo(xContext, " PLI incremental copy of " + increment, 
			        verboseInd);
			prevDir = setInstallDirs(xContext, compName, installPlatName, 
			                         increment, true);
			newDir = setInstallDirs(xContext, compName, installPlatName, 
			                        increment, false);
			HashSet<File> files= readInstall(xContext, prevDir, 
			                                 compName);
			logInfo(xContext, " File/link/dir count: " + files.size() + "\n", 
			        true);
			createLinks(xContext, files, prevDir, newDir, compName);
			createIncrementNutshellLink(xContext, compName, 
			                            increment);
		    }

		    // Create the 14.1 sym link to the last/latest increment
		    createIncLink(xContext, installPlatName, compName, 
		                  increments.lastElement());

		}
		else {

		    HashSet<File> files= readInstall(xContext, prevDir, 
		                                     compName);
		    logInfo(xContext, " File/link/dir count: " + files.size() + "\n", 
		            true);
		    if (files.size() > 0 || compName.equalsIgnoreCase("nutshell"))
			createLinks(xContext, files, prevDir, newDir, compName);
		    else
			logInfo(xContext, " Skipping no prev files ...", true);

		}

	    }
		
	    // Create the symlinks for readme and others
	    String reply = "";
	    InstallUtils.createPetLinks(xContext, getInstallDir(), getToolKit(), 
	                                isDryRun(), getLogger());
	    logInfo(xContext, reply, verboseInd);
	    InstallUtils.createVersionFile(getInstallDir(), getToolKit(), 
	                                   installPlat.getPackagingName(), 
	                                   isAsicsInstall());
	    InstallUtils.createReadmeLinks(xContext, getInstallDir(), 
	                                   getToolKit(), 
	                                   installPlatName, 
	                                   isAsicsInstall(), isDryRun(), 
	                                   getLogger());
	    logInfo(xContext, reply, verboseInd);
	    
	}
	

	logInfo(xContext, "\nInstall preparations completed successfully!\n",
	        true);

    }




 

    /**
     * Creates the nutshell link in INSTALL/mar2/mar2.1401.0
     *
     * @param xContext  Application context
     * @param compName  Component name
     * @param increment Increment name
     * @throws IcofException 
     */
    private void createIncrementNutshellLink(EdaContext xContext,
                                             String compName, 
                                             String increment) 
                                             throws IcofException {

	String dirName = getInstallDir().getAbsolutePath() + File.separator +
	compName + File.separator +
	increment;
	IcofFile incDir = new IcofFile(dirName, true);

	String[] command = new String[3];
	command[0] = "ln";
	command[1] = "-sf";
	command[2] = "../../nutshell";

	PkgUtils.runCommandInDir(command, incDir, isDryRun(), getLogger());

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
    private void createIncLink(EdaContext xContext, String platform,
                               String compName, String lastInc) 
                               throws IcofException {

	// This is the install plat from comp/rel directory
	//  $topLevelDir/aix64b/tk14.1.6z/tk14.1.6z.0_aix64b/mar2/14.1
	IcofFile newDir = setInstallDirs(xContext, compName, platform, 
	                                 null, false);

	// Here's where the new 14.1 link needs to be created
	// $topLevelDir/aix64b/tk14.1.6z/tk14.1.6z.0_aix64b/mar2
	IcofFile parent = new IcofFile(newDir.getParent(), true);

	String existing = lastInc + File.separator +
	compName + File.separator +
	getToolKit().getToolKit().getRelease().getName();

	String[] command = new String[3];
	command[0] = "ln";
	command[1] = "-sf";
	command[2] = existing;
	
	PkgUtils.runCommandInDir(command, parent, isDryRun(), getLogger());

    }


    /**
     * Create the new sym links based on the previous install
     *
     * @param xContext Application context
     * @param files    Collection of files/links
     * @param prevDir  Previous install directory
     * @param newDir   New install directory
     * @param compName Component name
     * @throws IOException 
     * @throws IcofException 
     */
    private void createLinks(EdaContext xContext, HashSet<File> files,
                             IcofFile prevDir, IcofFile newDir, String compName) 
                             throws IOException, IcofException {

	logInfo(xContext, "Creating PLI links ..", true);

	// Handle nutshell component different .. must be real files
	if (compName.equalsIgnoreCase("nutshell")) {
	    logInfo(xContext, " Starting nutshell special processing", true);
	    copyAllFiles(xContext, prevDir, newDir);
	    return;
	}

	if (! isAsicsInstall() && compName.startsWith("mar2")) {
	    logInfo(xContext, " Starting mar2* special processing", true);
	    copyAllFiles(xContext, prevDir, newDir);
	    return;
	}
	
	if (compName.equalsIgnoreCase("tgsupport")) {
	    logInfo(xContext, " Starting tgsupport special processing", true);
	    copyAllFiles(xContext, prevDir, newDir);
	    String msg = InstallUtils.updateGnaInitTcl(newDir, getToolKit(), 
	                                               getToolKitPkg(),
	                                               isAsicsInstall(), isDryRun());
	    logInfo(xContext, msg, verboseInd);
	    return;
	}

	// Create the new links for all other components
	for (File xFile : files) {
	    logInfo(xContext, "ABS: " + xFile.getAbsolutePath(), verboseInd);
	    //logInfo(xContext, "CAN: " + xFile.getCanonicalPath(), verboseInd);

	    if (skip64BitDirs(xContext, compName, xFile)) {
		logInfo(xContext, " SKIPPING .. is *-64 dir", verboseInd);
		continue;
	    }
	    
	    if (xFile.isDirectory()) {
		logInfo(xContext, " .. is DIR", verboseInd);
		createDirectory(xContext, xFile, prevDir, newDir);
	    }
	    else if (isSymlink(xFile)) {
		logInfo(xContext, " .. is LINK", verboseInd);
		copyLink(xContext, xFile, prevDir, newDir);
	    }
	    else {
		logInfo(xContext, " .. is FILE", verboseInd);
		createLink(xContext, xFile, prevDir, newDir);
	    }

	}

	// Special processing for theguide
	if (compName.equalsIgnoreCase("theguide")) {
	    String msg = InstallUtils.updateGnaInstall(newDir, getSiteName(), 
	                                               isAsicsInstall(), isDryRun(),
	                                               getLogger());
	    logInfo(xContext, msg, verboseInd);
	}

	// For 15.1 add any bin-64, dll-64 or lib-64 sym links
	String msg = "";
	if (isBin64Found())
	    msg = InstallUtils.makeSymlink("bin", "bin-64", newDir, 
	                             isDryRun(), getLogger());
	if (isDll64Found())
	    msg += InstallUtils.makeSymlink("dll", "dll-64", newDir, 
	                                    isDryRun(), getLogger());
	if (isLib64Found())
	    msg += InstallUtils.makeSymlink("lib", "lib-64", newDir, 
	                             isDryRun(), getLogger());
	logInfo(xContext, msg, verboseInd);
	
    }
 

    /**
     * Return true if this file should not be processed.  You wouldn't
     * process bin-64, dll-64 or lib-64 files if those dirs were sym links
     *
     * @param xContext
     * @param compName 
     * @param xFile
     * @return
     */
    private boolean skip64BitDirs(EdaContext xContext, String compName, File xFile) {

	if (isBin64Found() || isDll64Found() || isLib64Found()) {
	
	    String testStr = compName + File.separator +
	                     getToolKit().getToolKit().getRelease().getName() +
	                     File.separator;

	    if (isBin64Found() && 
	    xFile.getAbsolutePath().indexOf(testStr + "bin-64") > -1)
		return true;

	    if (isDll64Found() && 
	    xFile.getAbsolutePath().indexOf(testStr + "dll-64") > -1)
		return true;

	    if (isLib64Found() && 
	    xFile.getAbsolutePath().indexOf(testStr + "lib-64") > -1)
		return true;

	}
	
	return false;
	
    }


    /**
     * Determine if a file is a symlink
     *
     * @param xFile
     * @return
     * @throws IcofException 
     */
    private boolean isSymlink(File xFile) throws IcofException {

	boolean bIsLink = false;
	
	try {
	    Process p;
	    p = Runtime.getRuntime().exec(new String[]{"test", "-h", xFile.getAbsolutePath()});
	    p.waitFor();
	    if (p.exitValue() == 0) 
		bIsLink = true;
	}
	catch (IOException e) {
	    throw new IcofException(this.getClass().getName(), "isSymlink()", 
	                            IcofException.SEVERE, 
	                            "ERROR: unable to test file", 
	                            "File: " + xFile.getAbsolutePath());
	}
	catch (InterruptedException e) {
	    throw new IcofException(this.getClass().getName(), "isSymlink()", 
	                            IcofException.SEVERE, 
	                            "ERROR: unable to test file", 
	                            "File: " + xFile.getAbsolutePath());
	}
	
	return bIsLink;
	
    }


    /**
     * Copy all files so they are real not sym links
     *
     * @param xContext Application context
     * @param oldDir   Existing PLI directory
     * @param newDir   New PLI directory
     * @throws IcofException 
     */
    private void copyAllFiles(EdaContext xContext, IcofFile oldDir,
                              IcofFile newDir) throws IcofException {

	String command = "cp -pPr " + oldDir.getAbsolutePath() + " .";
	
	IcofFile parent = new IcofFile(newDir.getParent(), true);
	parent.mkdirs();

	logInfo(xContext, "  DIR: " + parent.getAbsolutePath(), verboseInd);
	logInfo(xContext, "  COMMAND: " + command, verboseInd);
	
	PkgUtils.runCommandInDir(command, parent, isDryRun(), getLogger());

	logInfo(xContext, "  COPY complete ", verboseInd);
	
    }


    /**
     * Creates a new patch level link 
     *
     * @param xContext Application context
     * @param xFile    Link to be created
     * @param oldDir   Old top level directory  
     * @param newDir   New top level directory
     * @throws IcofException 
     */
    private void createLink(EdaContext xContext, File xFile, IcofFile oldDir,
                            IcofFile newDir) throws IcofException {

	// Create the new directory path
	String newPath = getNewPath(xContext, xFile, oldDir, newDir);
	IcofFile newLink = new IcofFile(newPath, false);

	// Create relative link path
	String relPath = createRelativePath(xContext, xFile, oldDir);
	IcofFile parentDir = new IcofFile(newLink.getParent(), true);

	if (isDryRun())
	    logInfo(xContext, "DRYRUN - link not created\n  > " + 
	    newLink.getName() + "\n  > " + relPath + 
	    "\n  > " + parentDir.getAbsolutePath(), true);
	else {
	    String msg = InstallUtils.makeSymlink(relPath, xFile.getName(), 
	                                          parentDir,  isDryRun(), getLogger());
	    logInfo(xContext, msg, verboseInd);
	}


    }


    /**
     * Create the relative path to xFile
     *
     * @param xContext Application context
     * @param xFile    File to create relative path to
     * @param oldDir   Old top level directory
     * @return  relative path to PLI version of xFile
     */
    private String createRelativePath(EdaContext xContext, File xFile,
                                      IcofFile oldDir) {

	String pliDir = getInstallDir().getParent() + File.separator;
	//logInfo(xContext, " >> PLI dir: " + pliDir, verboseInd);

	String partialPath = xFile.getAbsolutePath().replace(pliDir, "");
	//logInfo(xContext, " >> Partial path: " + partialPath, verboseInd);

	String[] tokens = partialPath.split("[/]");
	String relPath = "";
	for (int i = 0; i < tokens.length - 1; i++) {
	    relPath += ".." + File.separator;
	}
	relPath += partialPath;
	//logInfo(xContext, " >> Relative path: " + relPath, verboseInd);

	return relPath;

    }


    /**
     * Copies the link(xFile) from prevDir to newDir
     *
     * @param xContext Application context
     * @param xFile    Link to be copied
     * @param oldDir   Old top level directory
     * @param newDir   New top level directory
     * @throws IcofException 
     */
    private void copyLink(EdaContext xContext, File xFile, IcofFile oldDir,
                          IcofFile newDir) throws IcofException {

	// Create the new directory path
	String newPath = getNewPath(xContext, xFile, oldDir, newDir);

	IcofFile oldLink = new IcofFile(xFile.getAbsolutePath(), false);
	IcofFile newLink = new IcofFile(newPath, false);
	if (! isDryRun())
	    newLink.getParentFile().mkdirs();

	if (isDryRun()) {
	    logInfo(xContext, "DRYRUN - link not copied\n  " + 
	    newLink.getAbsolutePath(), true);
	}
	else {
	    // Run special copy if copy deliverable contains * in the name
	    if (xFile.getAbsolutePath().contains("*")) {
		IcofFile parent = new IcofFile(newLink.getParent(), true);
		runCopySpecial(xContext, oldLink, parent, xFile);
	    }
	    else {
		oldLink.copy(newLink, true, "-pP");
	    }
	}

    }


    /**
     * Run a special copy if the file contains "*" in the name (pwrspice)
     *
     * @param xContext   Application context
     * @param actualDel  Full path to existing deliverable
     * @param workDelDir Full path to target
     * @param file       Deliverable object
     * @throws IcofException 
     */
    private void runCopySpecial(EdaContext xContext, IcofFile actualDel,
                                IcofFile workDelDir, File file) 
                                throws IcofException {

	// Remove the target file if it exists
	IcofFile tgtFile = new IcofFile(workDelDir.getAbsolutePath() + 
	                                File.separator + file.getName(), 
	                                false);
	if (tgtFile.exists())
	    tgtFile.delete();

	String command = "cp -pP " + actualDel.getAbsolutePath() + 
	" " + workDelDir.getAbsolutePath();

	logInfo(xContext, " SPECIAL CHAR file ", true);
	logInfo(xContext, "Running " + command, true);

	StringBuffer errors = new StringBuffer();
	Vector<String> results = new Vector<String>();
	if (! isDryRun()) {
	    int rc = IcofSystemUtil.execSystemCommand(command, errors, results);
	    if (rc != 0) {
		throw new IcofException(this.getClass().getName(), "runCopySpecial()", 
		                        IcofException.SEVERE, 
		                        "ERROR: unable to copy special " +
		                        "char file " + file.getName(), 
		                        errors.toString());
	    }
	}

    }


    /**
     * Create the new directory
     *
     * @param xContext Application context
     * @param xFile    Old directory that needs to be created in newDir
     * @param oldDir   Old top level directory
     * @param newDir   New top level directory
     */
    private void createDirectory(EdaContext xContext, File xFile,
                                 IcofFile oldDir, IcofFile newDir) {

	// Create the new directory path
	String newPath = getNewPath(xContext, xFile, oldDir, newDir);

	IcofFile dir = new IcofFile(newPath, true);
	if (isDryRun())
	    logInfo(xContext, "DRYRUN - dir not really created\n  " + 
	    dir.getAbsolutePath(), true);
	else
	    dir.mkdirs();	

    }


    /**
     * Create the new path name by substituting oldDir for newDir
     *
     * @param xContext Application context
     * @param xFile    File to work on
     * @param oldDir   Old directory name
     * @param newDir   New directory name
     * @return new xFile path
     */
    private String getNewPath(EdaContext xContext, File xFile, IcofFile oldDir,
                              IcofFile newDir) {

	String newPath = xFile.getAbsolutePath();
	return newPath.replace(oldDir.getAbsolutePath(), 
	                       newDir.getAbsolutePath());

    }


    /**
     * Read all files and links from the specified directory
     *
     * @param xContext Application context
     * @param aDir     Directory to read contents from
     * @param compName 
     * @return
     * @throws IcofException 
     */
    private HashSet<File> readInstall(EdaContext xContext, IcofFile aDir, 
                                      String compName) 
                                      throws IcofException {

	HashSet<File> files = new HashSet<File>();

	// Skip if nutshell .. gets special processing
	if (compName.equalsIgnoreCase("nutshell")) {
	    return files;
	}
	if (! aDir.exists()) {
	    return files;
	}
	
	files = (HashSet<File>)PkgUtils.listFileTree(aDir, false, true);

	// If 15.1 test if bin-64, dll-64 or lib-64 are sym links
	if (! getToolKit().getName().startsWith("14.1"))
	    set64bitLinks(xContext, aDir);
	
	return files;

    }


    /**
     * Test if bin-64, dll-64 or lib-64 are sym links
     *
     * @param xContext Application context
     * @param aDir     Prev install directory
     * @throws IcofException 
     */
    private void set64bitLinks(EdaContext xContext, IcofFile aDir) throws IcofException {

	File dir64Bit = new File(aDir.getAbsoluteFile() + File.separator + "bin-64");
	if (isSymlink(dir64Bit))
	    setBin64Found(true);
	
	dir64Bit = new File(aDir.getAbsoluteFile() + File.separator + "dll-64");
	if (isSymlink(dir64Bit))
	    setDll64Found(true);
	
	dir64Bit = new File(aDir.getAbsoluteFile() + File.separator + "lib-64");
	if (isSymlink(dir64Bit))
	    setLib64Found(true);
	
    }


    /**
     * Construct the tk14.1.6z.1 name or tk14.1.6z.beta1
     * 
     * @param xContext Application context
     * @return
     */
    private String constructPliTkName(EdaContext xContext, boolean bPrevToolkit) {

	int pliVersion = Integer.parseInt(getToolKitPkg().getName());
	
	if (bPrevToolkit && ! getToolKitPkg().getName().equals("0"))
	    pliVersion--;
	
	String tkName = getToolKit().getName();
	if (isAsicsInstall())
	    tkName = getToolKit().getToolKit().getAltDisplayName();
	
	String pliTk = "tk" + tkName + "." + pliVersion;

	return pliTk;
	
    }




    /**
     * Set the install tool kit platform directory
     * /afs/btv/data/vlsi/eclipz/common/tools/edatools/tk14.1.6/
     *   aix64b/tk14.1.6z/tk14.1.6z.0_aix64b/abc/14.1
     * - or -
     * $topLevelDir/aix64b/tk14.1.6z/tk14.1.6z.0_aix64b/abc/14.1
     *  - or -
     * $topLevelDir/aix64b/tk14.1.6z/tk14.1.6z.0_aix64b/mar2/mar2.1401.0/mar2/14.1
     *  - or -
     *  $topLevelDir/aix64b/tk14.1.6z/tk14.1.6z.0_aix64b/theguide_support (tgsupport)
     * 
     * @param xContext  Application context
     * @param aPkg      Component package to be installed in this dir
     * @param increment Incremental install dir or null
     * @param bPrev     If true get the previous PLI directory otherwise the 
     *                  current PLI directory
     * @throws IcofException 
     */
    private IcofFile setInstallDirs(EdaContext xContext, String compName, 
                                    String platform, String increment, 
                                    boolean bPrev)
                                    throws IcofException {

	if (bPrev)
	    logInfo(xContext, "\n Directories for LAST tool kit PLI", true);
	else
	    logInfo(xContext, "\n Directories for NEW tool kit PLI", true);

	// Get the tk sub dir
	String tkDir = "tk" + getToolKit().getName();
	String dirName = getTopLevelDir().getAbsolutePath() + File.separator;
	if (! isAsicsInstall())
	    dirName += platform + File.separator + 
	               tkDir + File.separator + 
	               constructPliTkName(xContext, bPrev) + "_" + platform;
	else
	    dirName += constructPliTkName(xContext, bPrev) + "_" + platform;
	
	// Create the install directory
	installDir = null;
	installDir = new IcofFile(dirName, true);
	logInfo(xContext, " Install dir: " + installDir.getAbsolutePath(),
	        true);

	// Define the install directory
	//  - install/COMP/REL or
	//  - install/COMP/INCREMENT/COMP/REL or
	//  - install/theguide_support
	String compRelDir = installDir.getAbsolutePath() + File.separator;
	if ((increment != null) && (! increment.isEmpty())) {
	    compRelDir += compName + File.separator + 
	    increment + File.separator +
	    compName + File.separator +
	    getToolKit().getToolKit().getRelease().getName();
	}
	else if (compName.equalsIgnoreCase("tgsupport")) {
	    compRelDir += "theguide_support";
	}
	else {
	    compRelDir += compName + File.separator + 
	    getToolKit().getToolKit().getRelease().getName();
	}
	IcofFile installCompRelDir = new IcofFile(compRelDir, true);
	logInfo(xContext, "  Comp Rel dir: " + compRelDir, true);

	return installCompRelDir;

    }


    /**
     * Set the install top level directory
     * /afs/btv/data/vlsi/eclipz/common/tools/edatools/tk14.1.6z
     * 
     * @param xContext
     */
    private void setTopLevelDir(EdaContext xContext, String installPlatform) {

	topLevelDir = InstallUtils.constructTopInstallDir(xContext, 
	                                                  getToolKit(), 
	                                                  isAsicsInstall(), 
	                                                  getSiteName(),
	                                                  installPlatform);

	logInfo(xContext, 
	        "Top Level dir: " + getTopLevelDir().getAbsolutePath() + "\n", 
	        true);

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
	    errors += "Tool kit package name (-t) is a required parameter\n";
	}

	// Read the dry run flag
	setDryRun(false);
	if (params.containsKey("-dryrun")) {
	    setDryRun(true);
	}

	// Read the component
	if (params.containsKey("-c")) {
	    setComponent(xContext, (String) params.get("-c"));
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
	    errors += "Install type and site (-server or -asics) is a required parameter\n";
	}

	return errors;

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
	singleSwitches.add("-dryrun");
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

	logInfo(xContext, "App         : " + APP_NAME + "  " + APP_VERSION,
	        verboseInd);
	logInfo(xContext, "Tool Kit    : " + getToolKit().getName(), verboseInd);
	logInfo(xContext, "Tool Kit Pkg: " + getToolKitPkg().getName(), verboseInd);
	if (getComponent() != null)
	    logInfo(xContext, "Component  : " + getComponent().getName(), 
	            verboseInd);
	logInfo(xContext, "Dryrun Mode : " + isDryRun(), verboseInd);
	logInfo(xContext, "DB Mode     : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose     : " + getVerboseInd(xContext),
	        verboseInd);
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
	        "Tool Kit: " + getToolKit().toString(xContext),
	        verboseInd);

	// Set the maintenance
	String maintName = aName.substring(aName.lastIndexOf(".") + 1);

	if (getToolKitPkg() == null) {
	    toolKitPkg = new ToolKitPackage(xContext, maintName);
	    try {
		// TK Pkg may not exist in the DB
		getToolKitPkg().dbLookupByName(xContext, getToolKit());
	    }
	    catch(IcofException ignore) {}
	    // TODO probably want to verify the pkg state if existing pkg	
	}

	logInfo(xContext, "Tool Kit Package: " + getToolKitPkg().getName(), 
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
	usage.append("Prep the install area for the next patch level install.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t tk_pkg> <-server site | -asics site> \n");
	usage.append("                [-c component] [-y] [-dryrun] [-h]\n");
	usage.append("\n");
	usage.append("  tk_pkg    = Tool kit patch level to be prepped (ie, 14.1.6z.1 ...)\n");
	usage.append("  -server   = Specifies this install is for server customers\n");
	usage.append("  -asics    = Specifies this install is for ASICs customers \n");
	usage.append("  site      = Which Server/ASICs customer site (BTV, EDA, ICC, POK ...)\n");
	usage.append("  component = Install pkgs for component only\n");
	usage.append("  -dryrun   = Don't create any new links\n");
	usage.append("  -y        = Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode    = DEV | PROD (defaults to PROD)\n");
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
    private boolean dryRunFlag;
    private boolean asicsInstall;
    private String siteName;
    private IcofFile topLevelDir;
    private IcofFile installDir;
    private ToolKitPackage toolKitPkg;
    private ArrayList<String> componentNames;
    private ArrayList<Platform_Db> platforms = new ArrayList<Platform_Db>();
    private boolean bin64Found = false;
    private boolean dll64Found = false;
    private boolean lib64Found = false;

    
    /**
     * Getters
     */
    public boolean isDryRun() { return dryRunFlag; }
    public boolean isAsicsInstall() { return asicsInstall; }
    public String getSiteName() { return siteName; }
    public IcofFile getTopLevelDir() { return topLevelDir; }
    public IcofFile getInstallDir() { return installDir; }
    public ToolKitPackage getToolKitPkg() { return toolKitPkg; }
    public ArrayList<String> getComponentNames() { return componentNames; }
    public ArrayList<Platform_Db> getPlatforms()  { return platforms; }
    public boolean isBin64Found() { return bin64Found; }
    public boolean isDll64Found() { return dll64Found; }
    public boolean isLib64Found() { return lib64Found; }

    
    /**
     * Setters
     */
    private void setDryRun(boolean aFlag) { dryRunFlag = aFlag; } 
    private void setAsicsInstall(boolean aFlag) { asicsInstall = aFlag; }
    private void setSiteName(String aName) { siteName = aName; }
    private void setToolKitPackage(ToolKitPackage aPkg) { toolKitPkg = aPkg; }
    private void setBin64Found(boolean aFlag) { bin64Found = aFlag; } 
    private void setDll64Found(boolean aFlag) { dll64Found = aFlag; }
    private void setLib64Found(boolean aFlag) { lib64Found = aFlag; }
    
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }
    // @formatter:on


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }

}
