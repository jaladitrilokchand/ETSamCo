/**
* <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2013 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *
 *-PURPOSE---------------------------------------------------------------------
 * Create a package of deliverables for a given component/tool kit 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 09/12/2013 GFS  Initial coding.
 * 02/26/2014 GFS  Fixed a .dont_ship file processing bug. Updated to handle
 *                 Deliverables with * in the name.  Updated tar command to 
 *                 deal with @longLink issues on Linux.
 * 03/02/2014 GFS  Fixed a bug in the repackage logic
 * 03/18/2014 GFS  Updated incremental processing
 * 03/24/2014 GFS  Disabled full mode for mar2 components.  Will have to 
 *                 revisit mar* incremental packaging.
 * 05/13/2014 GFS  Updated to support a comma delimited list of CRs with -z.
 * 05/20/2014 GFS  Updated mar2* auto increment detection
 * 07/15/2014 GFS  Updated to support preview tool kits.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree.packaging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import com.ibm.stg.eda.app.etree.ChangeRequestUpdateStatus;
import com.ibm.stg.eda.app.etree.PlatformShow;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Platform_Db;
import com.ibm.stg.eda.component.tk_etreedb.StageName_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequestStatus;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequests;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ComponentPackage;
import com.ibm.stg.eda.component.tk_etreeobjs.Deliverables;
import com.ibm.stg.eda.component.tk_etreeobjs.EventName;
import com.ibm.stg.eda.component.tk_etreeobjs.Location;
import com.ibm.stg.eda.component.tk_etreeobjs.PkgControlFile;
import com.ibm.stg.eda.component.tk_etreeobjs.PkgDeliverable;
import com.ibm.stg.eda.component.tk_etreeobjs.PkgDeliverable.Action;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKitPackage;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofStringUtil;
import com.ibm.stg.iipmds.common.IcofSystemUtil;

public class PackageDeliverables extends TkAppBase {

    /**
     * Constants
     */
    public static final String APP_NAME = "pkg.create";
    public static final String APP_VERSION = "v1.1";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aStageName Stage name to add
     */
    public PackageDeliverables(EdaContext aContext, ToolKit aTk, Component aComp)
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setToolKit(aTk);
	setComponent(aComp);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * @exception IcofException Unable to construct ManageApplications object
     */
    public PackageDeliverables(EdaContext aContext) throws IcofException {

	this(aContext, null, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv [] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new PackageDeliverables(null);
	    start(myApp, argv, PackageDeliverables.class.getName(), APP_NAME);
	}

	catch (Exception e) {
	    handleExceptionInMain(e);
	}
	finally {
	    handleInFinallyBlock(myApp);
	}

    }


    /**
     * Create Component packages for the new deliverables
     * 
     * @param aContext Application Context
     * @throws Exception
     */
    public void process(EdaContext xContext)
    throws Exception {

	// Connect to the database
	connectToDB(xContext);

	// Verify user is authorized to run this application
	getUser().validateUser(xContext, "svnlib");
	setOsName(System.getProperty("os.name").toLowerCase());
	
	// Process deliverables
	setChangeRequests(xContext);
	setPlatforms(xContext);
	setAllPackages(xContext);

	setDeliverables(xContext);
	packageDeliverables(xContext);
	updateChangeRequests(xContext);
	
	logInfo(xContext, "\nPackaging completed successfully", true);
	
	// Commit updates and return
	commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);

    }


    /**
     * Update the inject/change requests to transmitted
     *
     * @param xContext Application context
     * @throws IcofException 
     */
    private void updateChangeRequests(EdaContext xContext) throws IcofException {

	if (isDryRun()) {
	    logInfo(xContext, "[DRYRUN] skipping CR update ...", true); 
	    return;
	}
	
	// Set the new state
	ChangeRequestStatus crXmit = new ChangeRequestStatus(xContext, 
	                                                     "TRANSMITTED");
	crXmit.dbLookupByName(xContext);
	
	// Update all the change requests to transmitted
	for (ChangeRequest cr : getChangeRequests().getChangeRequests().values()) {
	 
	    ChangeRequestUpdateStatus crus;
	    crus = new ChangeRequestUpdateStatus(xContext, cr, 
	                                         crXmit, getUser());
	    crus.updateChangeRequest(xContext);
	    logInfo(xContext, "Updated CR (" + cr.getClearQuest() + ") to " + 
	            crXmit.getName(), verboseInd);
	    
	}
	
    }
  

    /**
     * Determine the Change Requests
     *
     * @param xContext  Application context
     * @throws Exception 
     */
    private void setChangeRequests(EdaContext xContext) throws Exception {

	logInfo(xContext, "Gathering change information ...", true);
	
	changeReqs = new ChangeRequests(getToolKit(), getComponent());
	changeReqs.setChangeRequests(xContext, getChangeReqText(),
	                             getPatchName(), 
	                             isAgts(), isCustomToolKit(), verboseInd);

    }


    /**
     * Find new/updated deliverables
     * 
     * @param xContext Application context
     * @throws IcofException Trouble determining deliverables
     * @throws IOException 
     * @throws InterruptedException 
     */
    private void setDeliverables(EdaContext xContext)
    throws IcofException, IOException {

	setLocation(xContext);
	setWorkingDirectories(xContext);
	setControlFiles(xContext);
	setIncrementalInstall(xContext);
	setAllDeliverables(xContext);
	filterOutUnwantedDels(xContext);
	setNewDeliverables(xContext);
	
    }


    /**
     * Update the allDeliverables collection so it only contains new/updated
     * deliverables and create a list of unchanged deliverables
     * 
     * @param xContext Application context
     * @throws IcofException
     * @throws IOException 
     * @throws InterruptedException 
     */
    private void setNewDeliverables(EdaContext xContext)
    throws IcofException, IOException {

	logInfo(xContext, "\nRemoving UNCHANGED deliverables ... ",
		getVerboseInd(xContext));
	changedCount = 0;

	deletedDeliverables = new HashMap<Platform_Db, Deliverables>();
	unchangedDeliverables = new HashMap<Platform_Db, Deliverables>();
	newDeliverables = new HashMap<Platform_Db, Deliverables>();
	
	// Remove unchanged deliverables
	for (Platform_Db platform : allDeliverables.keySet()) {
	    logInfo(xContext, "Platform: " + platform.getShippingName(), verboseInd);
	    
	    // Define the top level directory for this platform
	    String basePath = getSourceDir() + File.separator + 
 	                      platform.getShippingName() + File.separator;
	    
	    Deliverables keepers = new Deliverables(basePath);
	    Deliverables unchanged = new Deliverables(basePath);
	    Deliverables deleted = new Deliverables(basePath);

	    // Read the prev.ship.list file to determine which deliverables 
	    // were packaged the last time
	    if (isIncrementalPackage()) { 
		setPreviousDeliverables(xContext, platform);
	    }
	    else {
		logInfo(xContext, ">>> Full package mode requested .. " +
				  "previous deliverables not set from previous package <<<", 
				  true);
	    }
	    	    
	    // Remove unchanged deliverables
	    for (PkgDeliverable newDel : allDeliverables.get(platform).getDeliverables().values()) {
		newDel.loadMetaData();
		
		// If this deliverable is a link and what the link points to 
		// doesn't exist then skip processing this deliverable
		if (! newDel.getCanonicalFile().exists()) {
		    logInfo(xContext, "DELIVERABLE LINK SOURCE MISSING .. ", true);
		    logInfo(xContext, " Del: " + newDel.getAbsolutePath(), true);
		    logInfo(xContext, " Link src: " + newDel.getCanonicalPath(), true);
		    continue;
		}
		
		if ((getPrevDels() != null) &&
  		    getPrevDels().getDeliverables().containsKey(newDel.getPartialDelName())) {
		    
		    PkgDeliverable prevDel;
		    prevDel = getPrevDels().getDeliverables().get(newDel.getPartialDelName());
		    if ((prevDel.getTimestamp() != newDel.getTimestamp()) || 
		        (prevDel.getSize() != newDel.getSize())) {
			logInfo(xContext, "updated .. ", true);
//			logInfo(xContext, " SIZE old:" + prevDel.getSize() + 
//			        "  new:" + newDel.getSize(), true);
//			logInfo(xContext, " TMS old:" + prevDel.getTimestamp() + 
//			        "  new:" + newDel.getTimestamp(), true);
//			logInfo(xContext, " CKSUM old:" + prevDel.getCheckSum() +  
//			        "  new:" + newDel.getCheckSum(), true);
			
			newDel.setType(xContext, basePath, 
			               getSymlinkNotFollowFile());
			newDel.setAction(Action.UPDATE);
			keepers.addDeliverable(newDel);
		    }
		    else {
			newDel.setAction(Action.UNCHANGED);
			unchanged.addDeliverable(newDel);
			logInfo(xContext, "unchanged .. " + newDel.getPartialDelName(),
			        verboseInd);
		    }
		}
		else {
		    newDel.setAction(Action.NEW);
		    newDel.setType(xContext, basePath, getSymlinkNotFollowFile());
		    keepers.addDeliverable(newDel);
		}

	    }
	    
	    // Find deleted deliverables
	    boolean found = false;
	    if (getPrevDels() != null) {
		for (PkgDeliverable prevDel : getPrevDels().getDeliverables().values()) {

		    for (PkgDeliverable myDel : 
			allDeliverables.get(platform).getDeliverables().values()) {
			if (myDel.getPartialDelName().equals(prevDel.getPartialDelName())) {
			    found = true;
			    break;
			}
		    }
		    if (! found) {
			logInfo(xContext, "Deleted .. " + 
			        prevDel.getPartialDelName(), verboseInd);
			prevDel.setAction(Action.DELETE);
			deleted.addDeliverable(prevDel);
			found = false;
		    }

		}
	    }

	    deletedDeliverables.put(platform, deleted);
	    unchangedDeliverables.put(platform, unchanged);
	    newDeliverables.put(platform, keepers);
	    changedCount += keepers.getDeliverables().size();
	    
	    if (getVerboseInd(xContext)) {
		logInfo(xContext, "FINAL deleted deliverables ...", true);
		logInfo(xContext, deleted.toString(false) + "\n", true);
		
		logInfo(xContext, "FINAL unchanged deliverables for "
				  + platform.getShippingName(), true);
		logInfo(xContext, unchanged.toString(false) + "\n", true);
		
		logInfo(xContext, "FINAL new/changed deliverables for " + 
		        platform.getShippingName(), true);
		logInfo(xContext, keepers.toString(false) + "\n", true);

	    }
	    
	}
	
	// Report if no unchanged deliverables to process
	if (getChangedCount() == 0) {
	    logInfo(xContext, "\nWARNING: no unchanged deliverables to package " +
	    		"for ALL platforms!", true);
	}

    }


    /**
     * Reads the previously packaged deliverables from the prev.ship.list file
     *
     * @param xContext  Application context
     * @param platform Platform to read file from
     * @return Collection of deliverables
     * @throws IcofException 
     * @throws InterruptedException 
     */
    private void setPreviousDeliverables(EdaContext xContext, 
                                                  Platform_Db platform)
    throws IcofException {

	String basePath = getSourceDir() + File.separator + 
                          ".ship-" + platform.getShippingName();

	prevDels = new Deliverables(basePath);
	
	if ((getPreviousCompPkgs() != null) && 
	     (getPreviousCompPkgs().get(platform) != null)) {

	    logInfo(xContext, "Querying DB for previous deliverables .. " + 
	            getPreviousCompPkgs().get(platform).getName(), verboseInd);
	    prevDels.readDels(xContext, getToolKit(), getComponent(), platform,
	                      getPreviousCompPkgs().get(platform));

	}
	else {

	    logInfo(xContext, "No previous deliverables detected", verboseInd);
	    
	}
	
    }


    /**
     * Update the allDeliverables collection so it only contains desired
     * (non-excluded) deliverables and create a list of excluded deliverables
     * 
     * @param xContext Application context
     */
    private void filterOutUnwantedDels(EdaContext xContext) {

	logInfo(xContext, "\nRemoving UNWANTED deliverables ... ", true);

	excludedDeliverables = new HashMap<Platform_Db, Deliverables>();

	for (Platform_Db platform : allDeliverables.keySet()) {
	    String sTopDir = allDeliverables.get(platform).getTopLevelDir();
	    Deliverables keepers = new Deliverables(sTopDir);
	    Deliverables excluded = new Deliverables(sTopDir);
	    for (PkgDeliverable xDel : allDeliverables.get(platform).getDeliverables().values()) {
		logInfo(xContext, " Testing for exclusion: " + xDel.getPartialDelName(), verboseInd);
		if (PkgUtils.excludeDeliverable(getToolKit(), getComponent(), xDel)) {
		    excluded.addDeliverable(xDel);
		    logInfo(xContext, "UNWANTED .. " + xDel.getPartialDelName(),
			    verboseInd);
		    if (xDel.getPartialDelName().startsWith("bin-64"))
			foundBin64 = true;
		    else if (xDel.getPartialDelName().startsWith("dll-64"))
			foundDll64 = true;
		    else if (xDel.getPartialDelName().startsWith("lib-64"))
			foundLib64 = true;
		}
		else if (getDontShipFile().getContents().contains(xDel.getPartialDelName())) {
		    excluded.addDeliverable(xDel);
		    logInfo(xContext, "DONT_SHIP .. " + xDel.getPartialDelName(),
			    verboseInd);
		}
		else {
		    keepers.addDeliverable(xDel);
		}
	    }
	    allDeliverables.put(platform, keepers);
	    excludedDeliverables.put(platform, excluded);
	    
	}

	// Display the new collections
	for (Platform_Db platform : allDeliverables.keySet()) {
	    if (getVerboseInd(xContext)) {
		logInfo(xContext, "ALL Deliverables (after exclude) for "
				  + platform.getShippingName(), true);
		logInfo(xContext, 
		        allDeliverables.get(platform).toString(false) + "\n", 
		        true);

		logInfo(xContext, "Excluded Deliverables for " + 
		        platform.getShippingName(), true);
		logInfo(xContext, 
		        excludedDeliverables.get(platform).toString(false) + "\n", 
		        true);	    }

	}

    }


    /**
     * Find all deliverables in the source directory. Need to do this for each
     * .ship-platform directory.
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void setAllDeliverables(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "\nSetting Deliverables ... ", true);

	// Find deliverables in .ship-<platform> directories
	allDeliverables = new HashMap<Platform_Db, Deliverables>();
	for (Platform_Db platform : getPlatforms()) {
	    String shipDirName = PkgUtils.getTopLevelShipDir(getToolKit(),
	                                                       getComponent(), 
	                                                       getLocation(), 
	                                                       platform);
	    File shipDir = new File(shipDirName);
	  
	    // Get all files under the .ship directory
	    logInfo(xContext, " Scanning " + shipDirName, verboseInd);
	    HashSet<File> allFiles;
	    allFiles = (HashSet<File>)PkgUtils.listFileTree(shipDir, false);
	    logInfo(xContext, " -> Found " + allFiles.size() + 
	            " possible deliverables in directory", verboseInd);
	   	    
	    // Convert the files into deliverables
	    Deliverables platDeliverables = new Deliverables(shipDirName, allFiles);
	    allDeliverables.put(platform, platDeliverables);

	    logInfo(xContext, "\nALL Deliverables for " + platform.getShippingName(),
		    getVerboseInd(xContext));
	    logInfo(xContext, platDeliverables.toString(false), verboseInd);
	    
	}

    }


    /**
     * Define the top and working directories
     * 
     * @param xContext Application context
     * @throws IcofException 
     */
    private void setWorkingDirectories(EdaContext xContext) throws IcofException {

	logInfo(xContext, "\nSetting directories ... ", true);

	// Where to look for deliverables
	String topDir = PkgUtils.getTopLevelShipDir(getToolKit(),
	                                            getComponent(),
	                                            getLocation(), null);
	sourceDirectory = new IcofFile(topDir, true);
	logInfo(xContext, " Source Dir: " + sourceDirectory.getAbsolutePath(),
		getVerboseInd(xContext));

	// Where to copy new/update deliverables to
	tgtWorkDirectory = PkgUtils.getPkgWorkDir(getToolKit());
	logInfo(xContext,
		" Target Working Dir: " + tgtWorkDirectory.getAbsolutePath(),
		getVerboseInd(xContext));

	if (! tgtWorkDirectory.exists())
	    tgtWorkDirectory.mkdir();	
	
	// Where to store component packages
	tgtPkgDirectory = PkgUtils.getPkgPackagesDir(getToolKit());
	logInfo(xContext,
		" Target Package Dir: " + tgtPkgDirectory.getAbsolutePath(),
		getVerboseInd(xContext));

	if (! tgtPkgDirectory.exists()) {
	    tgtPkgDirectory.mkdir();	
	}
	
    }


    /**
     * Set the location based on the Tool Kit stage
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void setLocation(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "\nDetermining Location ...", true);

	String stage = getToolKit().getStageName().getName();
	if (stage.equalsIgnoreCase("PREVIEW")) {
	    location = new Location(xContext, "SHIP");
	}
	else if (stage.equalsIgnoreCase("PRODUCTION")) {
	    location = new Location(xContext, "TK");
	}
	else if (stage.contains("CUSTOMTK")) {
	    String locName = stage.toUpperCase();
	    locName = locName.replace("CUSTOMTKB", "CUSTOMTK");
	    location = new Location(xContext, locName);
	}
	else {
	    throw new IcofException("PackageDeliverables", "setLocation()", 
	                            IcofException.SEVERE, 
	                            "Unable to determine suitable location " +
	                            "from Tool Kit stage.", 
	                            getToolKit().getName() + " is in " + 
	                            stage + " stage");
	}
	
	logInfo(xContext, " Location: " + getLocation().getName(),
		getVerboseInd(xContext));

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
     * Define and read the control files
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void setControlFiles(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, "\nReading control files ...", true);
	StringBuffer myLog;
	
	// .addons file
	String fileName = PkgControlFile.getFullFileName(getSourceDir(),
							 PkgControlFile.FILE_addon);
	addonFile = new PkgControlFile(xContext, fileName);
	if (addonFile.exists()) {
	    myLog = addonFile.readContents(xContext);
	    logInfo(xContext, myLog.toString(), verboseInd);
	}
	else 
	    logInfo(xContext, " " + fileName + " not found ... skipping", 
	            verboseInd);
	
	// .symlink_not_follow file
	fileName = PkgControlFile.getFullFileName(getSourceDir(),
						  PkgControlFile.FILE_dontFollow);
	symlinkNotFollowFile = new PkgControlFile(xContext, fileName);
	if (symlinkNotFollowFile.exists()) {
	    myLog = symlinkNotFollowFile.readContents(xContext);
	    logInfo(xContext, myLog.toString(), verboseInd);
	}
	else 
	    logInfo(xContext, " " + fileName + " not found ... skipping", 
	            verboseInd);

	// .custom_deliver file
	fileName = PkgControlFile.getFullFileName(getSourceDir(),
						  PkgControlFile.FILE_customDeliver);
	customDeliverFile = new PkgControlFile(xContext, fileName);
	if (customDeliverFile.exists()) {
	    myLog = customDeliverFile.readContents(xContext);
	    logInfo(xContext, myLog.toString(), verboseInd);
	}
	else 
	    logInfo(xContext, " " + fileName + " not found ... skipping", 
	            verboseInd);
	
	// .special_path file
	fileName = PkgControlFile.getFullFileName(getSourceDir(),
						  PkgControlFile.FILE_specialPath);
	specialPathFile = new PkgControlFile(xContext, fileName);
	if (specialPathFile.exists()) {
	    myLog = specialPathFile.readContents(xContext);
	    logInfo(xContext, myLog.toString(), verboseInd);
	}
	else
	    logInfo(xContext, " " + fileName + " not found ... skipping", 
	            verboseInd);
	
	// .dont_ship file
	fileName = PkgControlFile.getFullFileName(getSourceDir(),
						  PkgControlFile.FILE_dontShip);
	dontShipFile = new PkgControlFile(xContext, fileName);
	if (dontShipFile.exists()) {
	    myLog = dontShipFile.readContents(xContext);
	    logInfo(xContext, myLog.toString(), verboseInd);
	}
	else 
	    logInfo(xContext, " " + fileName + " not found ... skipping", 
	            verboseInd);

    }


    /**
     * Package new/updated deliverables into a Component Package (tar file)
     * 
     * @param xContext Application context
     * @throws IOException
     * @throws IcofException
     */
    private void packageDeliverables(EdaContext xContext)
    throws IOException, IcofException {

	logInfo(xContext, "\nStarting packaging", true);
	
	if (getChangedCount() <= 0) {
	    logInfo(xContext, "No deliverables to package .. quitting", true);
	    return;
	}
	
	// Setup the AFS environment so the packages can be generated
	configureEnvironment(xContext);	
	
	for (Platform_Db platform : allDeliverables.keySet()) {
	    
	    setWorkingPlatformDirs(xContext, platform);
	    copyNewDeliverables(xContext, platform);
	    copyControlFiles(xContext, platform);
	    createPackage(xContext, platform);
	    logPackage(xContext, platform);
	    
	}

    }


    /**
     * Create the platform specific working directories. 
     * workPlatDir = top level platform specific directory
     *               for example /afs/.../work/14.1.6
     * workCompPlatDir = top level plat for
     *               for example /afs/.../work/14.1.6/edif/14.1
     *                        or /afs/.../work/14.1.6/theguide_support 
     *
     * @param xContext Application context
     * @param platform Platform
     * @throws IcofException 
     */
    private void setWorkingPlatformDirs(EdaContext xContext,
                                        Platform_Db platform) throws IcofException {

	// Read and parse the .special_path file to determine if there are any
	// special paths for this component
	readSpecialPath(xContext);
	
	// Define the new dir names
	String workPlatDirName = platform.getShippingName();
	logInfo(xContext, "Work platform dir: " + workPlatDirName, verboseInd);
	
	String workPlatCompDirName = workPlatDirName + File.separator;
	if (! getSpecialPathFile().exists() || 
	    getSpecialPathFile().getContents().isEmpty()) {
	    workPlatCompDirName += getComponent().getName() + File.separator + 
	                           getToolKit().getToolKit().getRelease().getName();
	}
	else {
	    workPlatCompDirName += getSpecialPath();
	}
	logInfo(xContext, "Work platform component dir: " + workPlatCompDirName, 
	        verboseInd);

	// Create any missing dirs if they don't exist
	PkgUtils.createMissingDirs(getTgtWorkDir(), workPlatCompDirName, true);
		
	// Instantiate the objects
	workingPlatformDir = new IcofFile(getTgtWorkDir().getAbsolutePath() + 
	                                  File.separator + workPlatDirName, true);
	workingPlatformComponentDir = new IcofFile(getTgtWorkDir().getAbsolutePath() + 
	 	                                  File.separator + workPlatCompDirName, true);
	
    }


    /**
     * Parse the .special_path contents
     *
     * @param xContext Application context
     */
    private void readSpecialPath(EdaContext xContext) {

	// If no .special_path file then skip
	setSpecialPath("");
	setSpecialPathPkgDir("");
	if (! getSpecialPathFile().exists() || 
	    getSpecialPathFile().getContents().isEmpty()) {
	    return;
	}

	// Process the .special_path file contents
	String line = (String)getSpecialPathFile().getContents().firstElement();
	String[] tokens = getSpecialPathFile().parseLine(line);	
	
	if (! tokens[0].equalsIgnoreCase("empty")) {
	    setSpecialPath(tokens[0]);
	}
	logInfo(xContext, " SPECIAL path: " + getSpecialPath(), verboseInd);
	
	setSpecialPathPkgDir(tokens[1]);
	
    }


    /**
     * Copy control files so they packaged with the deliverables
     *
     * @param xContext Application context
     * @param platform Platform to be processed
     * @throws IcofException 
     */
    private void copyControlFiles(EdaContext xContext, Platform_Db platform)
    throws IcofException {

	logInfo(xContext, "\nCopying control files ..", true);
	
	setControlDir(xContext, platform);
	writeChangeRequestFile(xContext);
	writeDeletedFile(xContext, platform);
	writeCustomDeliverFile(xContext);
	writeChecksumFile(xContext, platform);
	
    }


    /**
     * Create the deliverables checksum file
     *
     * @param xContext
     * @throws IcofException 
     */
    private void writeChecksumFile(EdaContext xContext, Platform_Db platform)
    throws IcofException {
	
	logInfo(xContext, " Writing ... checksum.data file", verboseInd);
	
	String fileName = getControlDir().getAbsolutePath() + File.separator +
                          "checksum.data";
	IcofFile file = new IcofFile(fileName, false);

	for (PkgDeliverable xDel : newDeliverables.get(platform).getDeliverables().values()) {
	    file.addLine(xDel.getPartialDelName() + ";" + 
	                 xDel.getCheckSum() + ";" + 
	                 xDel.getTypeName());
	}
	

	if (isDryRun()) {
	    logInfo(xContext, "\n Checksums:", true);
	    for (Object line : file.getContents()) {
		logInfo(xContext, " " + (String)line, true);
	    }
	}
	else {
	    file.openWrite();
	    file.write();
	    file.closeWrite();
	}
	
    }


    /**
     * Write the Custom Deliver file (copy original file)
     *
     * @param xContext
     * @throws IcofException 
     */
    private void writeCustomDeliverFile(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, " Writing ... custom_deliver.data file", verboseInd);
	
	String fileName = getControlDir().getAbsolutePath() + File.separator +
	                  "custom_deliver.data";
	IcofFile file = new IcofFile(fileName, false);

	if (isDryRun()) {
	    logInfo(xContext, "\n The following components have " +
	    		"different deliverables depending on the customer:", 
	    		true);
	    for (Object line : getCustomDeliverFile().getContents()) {
		logInfo(xContext, " " + (String)line, true);
	    }
	}
	else {
	    if (getCustomDeliverFile().exists())
		getCustomDeliverFile().copy(file, true);
	    else
		logInfo(xContext, " -> no file found to copy", verboseInd);
	}
	
    }

    
    /**
     * Write the Deleted file control file
     *
     * @param xContext
     * @throws IcofException 
     */
    private void writeDeletedFile(EdaContext xContext, Platform_Db platform)
    throws IcofException {

	logInfo(xContext, " Writing ... delete.data file", verboseInd);
	
	if (deletedDeliverables.get(platform).getDeliverables().isEmpty()) {
	    logInfo(xContext, " -> no deleted deliverables", verboseInd);
	    return;
	}
	
	
	String fileName = getControlDir().getAbsolutePath() + File.separator +
	                  "delete.data";
	IcofFile file = new IcofFile(fileName, false);

	for (PkgDeliverable xDel : deletedDeliverables.get(platform).getDeliverables().values()) {
	    file.addLine(xDel.getPartialDelName() + ";" + xDel.getCheckSum());
	}
	
	if (isDryRun()) {
	    logInfo(xContext, "\n The following files/dirs will be deleted " +
	    		"from the installation:", true);
	    for (Object line : file.getContents()) {
		logInfo(xContext, " " + (String)line, true);
	    }
	}
	else {
	    file.openWrite();
	    file.write();
	    file.closeWrite();
	}
	
    }


    /**
     * Set the control directory which will hold the control files
     *
     * @param xContext
     * @throws IcofException 
     */
    private void setControlDir(EdaContext xContext, Platform_Db platform) 
    throws IcofException {

	// Create the control directory
	String dirName = getWorkPlatCompDir().getAbsolutePath() + 
	                 File.separator;
	if (getSpecialPathPkgDir() != null)
	    dirName += getSpecialPathPkgDir() + File.separator;
	dirName += ".package";
	
	tgtPkgControlDir = new IcofFile(dirName, true);
	
	if (! tgtPkgControlDir.exists()) {
	    tgtPkgControlDir.create();
	}
	
	logInfo(xContext, " Control dir: " + dirName, verboseInd);
	
    }


    /**
     * Creates the CQ control file
     *
     * @param xContext
     * @throws IcofException 
     */
    private void writeChangeRequestFile(EdaContext xContext)
    throws IcofException {

	logInfo(xContext, " Writing ... CQ.data file", verboseInd);
	
	String fileName = getControlDir().getAbsolutePath() + File.separator +
	                  "CQ.data";
	IcofFile file = new IcofFile(fileName, false);
	
	// Create the file contents
	// - comma delimited list of Change Requests
	StringBuffer crs = new StringBuffer();
	if ((getChangeRequests() != null) &&
	    (! getChangeRequests().getChangeRequests().isEmpty())) {
	    for (String crName : getChangeRequests().getChangeRequests().keySet()) {
		if (crs.length() > 0)
		    crs.append(",");
		crs.append(crName);
	    }
	}
	if (crs.length() > 0)
	    file.addLine(crs.toString());
	
	if (isDryRun()) {
	    logInfo(xContext, "\n The following Change Requests are " +
	    		"associated with this patch:", true);
	    for (Object line : file.getContents()) {
		logInfo(xContext, " " + (String)line, true);
	    }
	}
	else {
	    file.openWrite();
	    file.write();
	    file.closeWrite();
	}
    }


    /**
     * Create the new component and tool kit package names
     *
     * @param xContext
     * @throws IcofException 
     */
    private void setAllPackages(EdaContext xContext) 
    throws IcofException {

	logInfo(xContext, "\nGathering packaging data ... ", true);
	
	// Lookup the Tool Kit package
	setToolKitPackage(xContext);

	previousComponentPkgs = new HashMap<Platform_Db, ComponentPackage>();
	newComponentPkgs = new HashMap<Platform_Db, ComponentPackage>();
	
	// Create a list of previous and new component package names 
	for (Platform_Db plat : getPlatforms()) {
	
	    // Lookup the previous Component package
	    setPreviousCompPackage(xContext, plat);

	    // Determine new patch and spin levels from previous patch
	    int maintLevel = calculatePatchLevel(xContext, 
	                                         getPreviousCompPkgs().get(plat));
	    int patchLevel = calculateSpinLevel(xContext, 
	                                        getPreviousCompPkgs().get(plat));
	    logInfo(xContext, " New maint level: " + maintLevel, verboseInd);
	    logInfo(xContext, " New patch level : " + patchLevel, verboseInd);

	    // Package names are component.tk.maint.patch.platform
	    String pkgName = ComponentPackage.getComponentPkgName(getComponent(), 
	                                                          getToolKit(), 
	                                                          plat, 
	                                                          maintLevel, 
	                                                          patchLevel);

	    ComponentPackage newPkg = new ComponentPackage(xContext, pkgName, 
	                                                   plat, patchLevel, 
	                                                   maintLevel, null);
	    getNewCompPkgs().put(plat, newPkg);

	    logInfo(xContext, " New component package: " + newPkg.getName(), 
	            verboseInd);
	
	}

    }
    
    
    /**
     * Determine the patch level from the previous package and tool kit stage
     *
     * @param xContext Application context
     * @return
     * @throws IcofException 
     */
    private int calculatePatchLevel(EdaContext xContext, 
                                    ComponentPackage prevPkg) 
       throws IcofException {

	int level = 0;

	// If no previous package then keep the level at 0
	if (prevPkg == null)
	    return level;
	
	// If there was a previous package then set the patch to the previous
	// package's patch level
	level = prevPkg.getPatchLevel();
	
	// If this is a preview TK then increment the level
	if (getToolKit().getStageName().getName().equals(StageName_Db.STAGE_PREVIEW))
	    level++;
	
	return level;
	
    }

    
    /**
     * Determine the spin level from the previous package and tool kit stage
     *
     * @param xContext Application context
     * @return
     * @throws IcofException 
     */
    private int calculateSpinLevel(EdaContext xContext, 
                                   ComponentPackage prevPkg) 
                                   throws IcofException {

	int level = 0;
	
	// If no previous package then then keep the level at 0
	if (prevPkg == null) {
	    if (getToolKit().getName().equals("14.1.7"))
		level = Integer.parseInt(getToolKitPkg().getName());
	    return level;
	}
	
	// If there was a previous package then set the spin to the previous
	// package's spin level
	level = prevPkg.getMaintLevel();

	// If this is a production, custom or xtinct TK then increment the spin
	if (getToolKit().getStageName().getName().equals(StageName_Db.STAGE_PRODUCTION) ||
	    (getToolKit().getStageName().getName().indexOf("XTINCT") > -1) ||
	    (getToolKit().getStageName().getName().indexOf("CUSTOM") > -1 ))
	    level++;
	
	return level;
	
    }


    /**
     * Look up the previous component package for this tool kit
     *
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void setPreviousCompPackage(EdaContext xContext, Platform_Db aPlat)
    throws IcofException {

	// Lookup all component pkgs for this component and tk
	ComponentPackage prevPkg = new ComponentPackage(xContext, (long)0);
	List<ComponentPackage> allCompPkgs = prevPkg.getCompPackages(xContext, 
	                                                            getToolKit(), 
	                                                            getComponent(),
	                                                            aPlat, true);
	logInfo(xContext, "\n Found " + allCompPkgs.size() + " existing Comp Pkgs " +
			"for this TK and Component", verboseInd);
	
	if ((allCompPkgs == null) || (allCompPkgs.size() == 0)) {
	    logInfo(xContext, " No previous Component Package found", verboseInd);
	    prevPkg = null;
	    getPreviousCompPkgs().put(aPlat, prevPkg);
	    return;
	}
	
	// Set previous pkg to latest
	prevPkg = null;
	if (allCompPkgs.size() > 0)
	    prevPkg = allCompPkgs.get(0);
	
	// If the last package is in this tk package then we are in a 
	// repackage situation and we'll need to reset to the pkg before the 
	// previous .. this will set the previous deliverables correctly
	if (prevPkg != null)
	    repackage = prevPkg.dbLookupByTkPkg(xContext, getToolKitPkg());
	
	if (repackage) {
	    logInfo(xContext, " REPACKAGE mode detected\n", verboseInd);
	    prevPkg = null;
	    if (allCompPkgs.size() > 1)
		prevPkg = allCompPkgs.get(1);
	}
	getPreviousCompPkgs().put(aPlat, prevPkg);
	
	if (prevPkg == null)
	    logInfo(xContext, " Previous Component Package: NULL", verboseInd);
	else 
	    logInfo(xContext, " Previous Component Package: " + 
	            prevPkg.getName(), verboseInd);
	
    }


    /**
     * Lookup the Tool Kit Package for this Component Package
     *
     * @param xContext
     * @throws IcofException 
     */
    private void setToolKitPackage(EdaContext xContext) throws IcofException {

	// Skip if already set .. only needs to be determined once
	if (getToolKitPkg() != null) {
	    return;
	}
	
	// Find the Tool Kit package in the new state 
	EventName newTkPkgEvent = new EventName(xContext, "PKG_NEW");
	newTkPkgEvent.dbLookupByName(xContext);
	
	toolKitPkg = new ToolKitPackage(xContext);
	boolean bFound = toolKitPkg.dbLookupLatestInState(xContext, getToolKit(), 
	                                                  newTkPkgEvent);
	
	if (bFound)
	    logInfo(xContext, " Found existing TK Pkg in NEW state.", verboseInd);
	else
	    logInfo(xContext, " Didn't find a TK Pkg in NEW state.", verboseInd);

	
	// If not found then create a new tool kit package
	if (! bFound) {
	    
	    logInfo(xContext, " Creating new tool kit maintenance ... ", 
		    verboseInd);
	    
	    // To determine the new maintenance version lookup the
	    // latest tool kit package for this tool kit
	    bFound = toolKitPkg.dbLookupLatest(xContext, getToolKit());
	    logInfo(xContext, "  Found: " + bFound, verboseInd);
	    logInfo(xContext, "  Last TK Maint: " + toolKitPkg.getName(), verboseInd);
	    
	    // Set the initial TK package name to 0 for preview, production 
	    // or customtk
	    long maintName = 0;
	    if (getToolKit().getName().equals("14.1.7"))
		maintName = 12;
	    logInfo(xContext, "  Starting maint: " + maintName, verboseInd);
	    logInfo(xContext, "  TK Stage: " + getToolKit().getStageName().getName(), 
	            verboseInd);
	    
	    if (bFound) {
		maintName = Long.parseLong(toolKitPkg.getName()) + 1;
	    }
	   
	    logInfo(xContext, "  My maint: " + maintName, verboseInd);
	    toolKitPkg = null;
	    toolKitPkg = new ToolKitPackage(xContext, String.valueOf(maintName));	                                    
	    toolKitPkg.dbAdd(xContext, null, getUser());

	}
	else {
	    logInfo(xContext, " Using existing tool kit package", verboseInd);
	}
	
	logInfo(xContext, " -> Tool kit maint: " + 
	        getToolKit().getToolKit().getDisplayName() + "." + 
	        getToolKitPkg().getName(), 
	        verboseInd);
	
	
	// TODO debug
//	if (true)
//	    throw new IcofException("TESTING", "Debugging setToolKitPackage() ...");
	
	
    }

    
    /**
     * Log the new package in the ETREE database
     *
     * @param xContext  Application context
     * @param platform  Platform object
     * @throws IcofException 
     */
    private void logPackage(EdaContext xContext, Platform_Db platform) 
    throws IcofException {

	logInfo(xContext, "\nLogging package meta data in database", true);
	if (isDryRun()) {
	    logInfo(xContext, " DRYRUN: package not actually logged in DB", true);
	    return;
	}

	// Add the new package to the database and associate with this tk 
	// and component
	logInfo(xContext, " Adding new component package", verboseInd);
	getNewCompPkgs().get(platform).dbAdd(xContext, null, getToolKit(), 
	                                     getComponent(), getUser());
	logInfo(xContext, " Component package ID: " + 
	getNewCompPkgs().get(platform).getDbObject().getId(), verboseInd);
	
	// If repackaging remove all previous deliverables and update state
	if (isRepackage()) {
	    logInfo(xContext, " Repackage mode detected - deleting any " +
	    		"existing deliverables and change requests", 
	    		verboseInd);
	    getNewCompPkgs().get(platform).dbLookupByName(xContext);
	    getNewCompPkgs().get(platform).removeAllDeliverables(xContext);
	    
	    // TODO for a repackage with 2nd patch we don't want to remove 
	    //      the old CRs (so uncomment the delete call)
	    //getNewCompPkgs().get(platform).deleteChangeRequests(xContext, 
	    //                                                    getUser());
	    getNewCompPkgs().get(platform).updateState(xContext, "PKG_NEW", 
	                                               "Repackaging", getUser());
	}

	// Associate Change Requests with new package
	logInfo(xContext, " Adding change requests to component package", 
	        verboseInd);
	getNewCompPkgs().get(platform).addChangeRequests(xContext, 
	                                                 getChangeRequests(), 
	                                                 getUser());
	
	// Associate package contents with component package
	logInfo(xContext, " Logging new/updated deliverables", 
	        verboseInd);
	getNewCompPkgs().get(platform).addDeliverables(xContext, 
	                                               getNewDeliverables().get(platform));

	logInfo(xContext, " Logging deleted deliverables", 
	        verboseInd);
	getNewCompPkgs().get(platform).addDeliverables(xContext, 
	                                               deletedDeliverables.get(platform));

	// Associate this component package with the tool kit package
	logInfo(xContext, " Adding component package to tool kit package", 
	        verboseInd);
	getToolKitPkg().addComponentPackage(xContext, 
	                                    getNewCompPkgs().get(platform));

    }


    /**
     * Creates the component package file from the working directory
     * 
     * @param xContext Application context
     * @param platform Name of platform being processed
     * @throws IcofException
     */
    private void createPackage(EdaContext xContext, Platform_Db platform)
    throws IcofException {

	logInfo(xContext, "\nCreating component package", true);
	
	// Define the package names/object
	String tarPkgName = getNewCompPkgs().get(platform).getName() + ".tar";
	String gzPkgName = tarPkgName + ".gz";
	
	IcofFile tarPkg = new IcofFile(getTgtPkgDir() + File.separator + 
	                               tarPkgName, false);
	IcofFile gzPkg = new IcofFile(getTgtPkgDir() + File.separator + 
	                              gzPkgName, false);
	
	// Delete existing packages if repackage
	if (isRepackage()) {
	    if (tarPkg.exists())
		tarPkg.delete();
	    if (gzPkg.exists())
		gzPkg.delete();
	}
	
	// Determine the comp/rel directory 
	String compRel = getComponent().getName() + File.separator + 
                         getToolKit().getToolKit().getRelease().getName();
	if (! getSpecialPathPkgDir().isEmpty())
	    compRel = getSpecialPathPkgDir();
	
	// Create the package tar (packageName.tar)
	String tarExec = "tar";
	if (getOsName().contains("nix")) {
	    // Additional param to prevent @LongLink issues when run on Linux
	    tarExec += " --format=ustar ";
	}
	
	String command = tarExec + " -cvf " + tarPkg.getAbsolutePath() + 
	                 " " + compRel;
	String reply = "";
	int rc = PkgUtils.runCommandInDir(command, getWorkPlatDir(), isDryRun(), 
	                                  getLogger());
	logInfo(xContext, reply, verboseInd);
	if (rc == 0 && ! isDryRun()) {
	    logInfo(xContext, " Tar file created " + tarPkgName, verboseInd);
	}

	// Verify component package contains all deliverables
	verifyPackageContents(xContext, tarPkg, platform);
	
	// Compress the package (packageName.tar.gz)
	if (! isDryRun()) {
	    if (gzPkg.exists())
		gzPkg.delete();
	    tarPkg.compress(gzPkg);
	}
	else {
	    logInfo(xContext, " [DRYRUN] skipping package compression ", true);
	}
	logInfo(xContext, " Compressed package name: " + gzPkg.getFileName(), 
	        verboseInd);
	
	// Remove the tar file .. just want to keep the tar.gz version
	tarPkg.delete();
	
	// Clean up the working directory
	PkgUtils.removeAll(getControlDir());
	PkgUtils.removeAll(getWorkPlatDir());

	// Detect and set incremental marker for this package if mar 
	// version changed
	createIncrementalInstallMarker(xContext, gzPkg);
	
    }

    
    /**
     * Write the incremental marker file for this package if incremental
     * install detected
     *
     * @param xContext Application context
     * @param gzPkg Package being processed
     * @throws IcofException 
     */
    private void createIncrementalInstallMarker(EdaContext xContext, 
                                                IcofFile gzPkg) 
    throws IcofException {

	logInfo(xContext, "Setting incremental marker if needed ...", true);
	if (! isIncrementalInstall()) {
	    logInfo(xContext, " Skipping .. no incremental install detected", true);
	    return;
	}
	
	IcofFile marker = new IcofFile(gzPkg.getAbsolutePath() + 
	                               PkgUtils.MAR_INC_EXT, 
	                               false);
	marker.openWrite();
	marker.write(getCurrMarVerFile().getContents());
	marker.closeWrite();
	
	logInfo(xContext, " Writing incremental install marker file ...", true);
	logInfo(xContext, " " + marker.getAbsolutePath(), true);

    }


    /**
     * Write a pkg.increment file if the mar version changed
     *
     * @param xContext Application context
     * @param gzPkg Package file
     * @throws IcofException 
     */
    private void setIncrementalInstall(EdaContext xContext) 
    throws IcofException {

	logInfo(xContext, "Detecting incremental mar2* install required .. ", 
	        true);
	
	setIncrementalInstall(false);
	
	// Only mar2 and mar2_2243 support incremental installs so if
	// not one of those components then return
	if (! getComponent().getName().startsWith("mar2")) {
	    logInfo(xContext, " Not a mar2* component .. not an incremental install", true);
	    return;
	}
	
	// Compare the Version files to see if the mar version has changed
	// A difference will trigger an incremental install
	currMarVerFile = new IcofFile(getSourceDir().getAbsolutePath() + 
	                              File.separator + 
	                              PkgUtils.MAR_CURRENT_VERSION, false);
	prevMarVerFile = new IcofFile(getSourceDir().getAbsolutePath() + 
	                              File.separator + 
	                              PkgUtils.MAR_PREVIOUS_VERSION, false);
	
	// Read the currVerFile
	if (currMarVerFile.exists()) {
	    currMarVerFile.openRead();
	    currMarVerFile.read();
	    currMarVerFile.closeRead();
	}
	else {
	    setIncrementalInstall(false);
	    logInfo(xContext, " No Version file found .. not an " +
	    		"incremental install", true);
	    return;
	}
	
	// If no prev Version file then not an incremental install
	if (! prevMarVerFile.exists()) {
	    setIncrementalInstall(false);
	    logInfo(xContext, " No Version.prev file found .. not an " +
		"incremental install", true);

	}
	else {

	    prevMarVerFile.openRead();
	    prevMarVerFile.read();
	    prevMarVerFile.closeRead();
	    
	    // Compare the current and previous file contents
	    String currMarVer = (String) currMarVerFile.getContents().firstElement();
	    currMarVer = currMarVer.trim();
	    logInfo(xContext, " Current mar ver: " + currMarVer, true);

	    String prevMarVer = (String) prevMarVerFile.getContents().firstElement();
	    prevMarVer = prevMarVer.trim();
	    logInfo(xContext, " Previous mar ver: " + prevMarVer, true);

	    
	    if (currMarVer.equals(prevMarVer)) {

		logInfo(xContext, " Current and prev mar versions match .. not an " +
	        "incremental install", true);

	    }
	    else {

		setIncrementalInstall(true);
		setIncrementalPackage(false);
		logInfo(xContext, " Mar versions don't match .. incremental " +
		        "install detected .. ", true);
		logInfo(xContext, " OVERRIDE .. setting incremental package " +
				"mode to false", true);
		
	    }
	    
	}
	    
	// Write the Version.prev file
	if (! isDryRun()) {
	    prevMarVerFile.openWrite();
	    prevMarVerFile.write(currMarVerFile.getContents());
	    prevMarVerFile.closeWrite();
	}
	logInfo(xContext, " Updating Version.prev file ...", true);
	
    }


    /**
     * Verify the package contains all the deliverables
     *
     * @param xContext Application context
     * @param tarPkg   Component package
     * @throws IcofException 
     */
    private void verifyPackageContents(EdaContext xContext, IcofFile tarPkg, 
                                       Platform_Db platform)
    throws IcofException {

	logInfo(xContext, "Verify package contents - " + tarPkg.getName(), true);
	if (isDryRun()) {
	    logInfo(xContext, "[DRYRUN] skipping verify package contents", true);
	    return;
	}
	
	// Get the package contents
	String command = "tar -tf " + tarPkg.getAbsolutePath();
	StringBuffer errors = new StringBuffer();
	Vector<String> results = new Vector<String>();
	int rc = IcofSystemUtil.execSystemCommand(command, errors, results);
	if (rc != 0) {
	    throw new IcofException(this.getClass().getName(), 
	                            "verifyPackageContents()", IcofException.SEVERE, 
	                            "Unable to get package contents with tar -tf", 
	                            "Package: " + tarPkg.getAbsolutePath());
	}
	logInfo(xContext, "Tar ball contents ...", verboseInd);
	Vector<String> contents = new Vector<String>();
	for (String line : results) {
	    if (line.indexOf(" ") > -1) {
		String[] tokens = line.split("[ ]+");
		contents.add(tokens[0]);
	    }
	    else
		contents.add(line);
	}
	logInfo(xContext, "", verboseInd);
	
	// Package contents start with comp/14.1 so figure out what that text is
	// so it can be used during the compares .. some comps use a special path
	// which is not comp/14.1
	int dirLen = getWorkPlatDir().getAbsolutePath().length() + 1;
	String workPlatDir = getWorkPlatCompDir().getAbsolutePath();
	
	String compRel = "";
	if (workPlatDir.length() > dirLen)
	    compRel = workPlatDir.substring(dirLen) + File.separator;
	logInfo(xContext, "  CompRel = " + compRel, verboseInd);
	
	
	// Compare the contents
	for (PkgDeliverable file : newDeliverables.get(platform).getDeliverables().values()) {
	    logInfo(xContext, " Pkg check .. " + file.getPartialDelName(), 
	            verboseInd);
	    if (! contents.contains(compRel + file.getPartialDelName())) {
		throw new IcofException(this.getClass().getName(), 
		                        "verifyPackageContents()", IcofException.SEVERE, 
		                        "Deliverable not found in package contents", 
		                        "File: " + compRel + file.getPartialDelName());
	    }
	}
    }

 
    
    /**
     * Copy deliverables from ship location to working directory
     * 
     * @param xContext Application context
     * @param platform Name of platform to be processed
     * @throws IcofException
     * @throws IOException
     */
    private void copyNewDeliverables(EdaContext xContext, Platform_Db platform)
    throws IcofException, IOException {

	logInfo(xContext, "\nCopying new deliverables", true);
	
	for (PkgDeliverable file : getNewDeliverables().get(platform).getDeliverables().values()) {

	    // Get the type so we know what action to take
	    logInfo(xContext, "\nDel: " + file.getAbsolutePath(), 
	            verboseInd);
	    logInfo(xContext, " Type: " + file.getTypeName(), verboseInd);

	    switch (file.getType()) {
	    case REAL:
		copyFile(xContext, platform, file);
		break;
	    case LINK_NUTSHELL:
		copyNutshell(xContext, platform, file);
		break;
	    case LINK_DONT_FOLLOW:
		copyLink(xContext, platform, file, false);
		break;
	    case LINK_FOLLOW:
		copyLink(xContext, platform, file, true);
		break;
	    default:
		throw new IcofException(this.getClass().getSimpleName(),
					"copyDeliverables()",
					IcofException.SEVERE,
					"Unknown deliverable action type", "");
	    }

	}
	
	// Add any extra files listed in the .addon file
	copyAddons(xContext, platform);

	// Add any 15.1 bin-64, dll-64 or lib-64 links
	if (getToolKit().getToolKit().getRelease().getName().equals("15.1")) {
	    if (foundBin64) {
		logInfo(xContext, "Adding NEW bin-64 -> bin links", true);
		String command = "ln -sf bin bin-64";
		PkgUtils.runCommandInDir(command, getWorkPlatCompDir(), 
		                         isDryRun(), null);
	    }
	    if (foundDll64) {
		logInfo(xContext, "Adding NEW dll-64 -> dll links", true);
		String command = "ln -sf dll dll-64";
		PkgUtils.runCommandInDir(command, getWorkPlatCompDir(), 
		                         isDryRun(), null);
	    }
	    if (foundLib64) {
		logInfo(xContext, "Adding NEW lib-64 -> lib links", true);
		String command = "ln -sf lib lib-64";
		PkgUtils.runCommandInDir(command, getWorkPlatCompDir(), 
		                         isDryRun(), null);
	    }

	}
	
	
//	throw new IcofException(this.getClass().getSimpleName(),
//	                        "copyPkgDeliverables()", IcofException.SEVERE,
//	                        "TESTING", "Check results in " + 
//	                        getWorkPlatDir().getAbsolutePath());
	
	
    }


    /**
     * Create any new symlinks defined in the .addon file
     *
     * @param xContext  Application context
     * @param workDir   Target platform specific directory
     * @throws IcofException 
     */
    private void copyAddons(EdaContext xContext, Platform_Db platform) 
    throws IcofException {

	logInfo(xContext, "Processing .addon contents ...", verboseInd);

	
	// Return if nothing to do
	if (getAddonFile().getContents().isEmpty()) {
	    logInfo(xContext, " file is empty ... nothing to do", verboseInd);
	    return;
	}
	
	// Process the addon links
	for (Object aLine : getAddonFile().getContents()) {
	    
	    logInfo(xContext, " .addon - " + (String)aLine, verboseInd);
	    
	    if (IcofStringUtil.occurrencesOf((String)aLine, ";") != 2) {
		throw new IcofException(this.getClass().getName(), 
		                        "copyAddons()",
		                        IcofException.SEVERE, 
		                        "Incorrect format of .addon line", 
		                        (String)aLine);
	    }
	    
	    String[] tokens = getAddonFile().parseLine((String)aLine);
	    String tgtDir = tokens[0];
	    String newLinkName = tokens[1];
	    String existingFileName = tokens[2];

	    // Define the existing directory to create the link in
	    String existingPath = getWorkPlatCompDir().getAbsolutePath() + 
	    			  File.separator + tgtDir;
	    IcofFile existingDir = new IcofFile(existingPath, true);

	    // If the existing file doesn't exist then don't create the link
	    IcofFile existingFile = new IcofFile(existingPath + File.separator + 
	                                         existingFileName, false);
	    if (existingFile.exists()) {
		String command = "ln -sf " + existingFileName + " " + newLinkName;
		PkgUtils.runCommandInDir(command, existingDir, isDryRun(), 
		                         getLogger());
		logInfo(xContext, " Link created", verboseInd);
		
		// Add this deliverable to the new deliverables collection
		File newFile = new File(existingDir.getAbsoluteFile() + 
		                        File.separator + newLinkName);
		PkgDeliverable del = new PkgDeliverable(getWorkPlatCompDir().getAbsolutePath(), 
		                                        newFile);
		del.setAction(Action.NEW);
		del.setType(xContext, getTopLevelDir(), getSymlinkNotFollowFile());
		del.loadMetaData();
		getNewDeliverables().get(platform).addDeliverable(del);
		logInfo(xContext, " Added .addon entry " + newLinkName + 
		        " to new dels collection", verboseInd);
		
	    }
	    else {
		logInfo(xContext, " Link NOT created .. original file " +
		        "doesn't exist\n Original File:" + 
		        existingFile.getAbsolutePath(), verboseInd);
	    }
	    
	}
	
    }


    /**
     * Copies the deliverable (which is a symlink) to the work directory
     * 
     * @param platform Name of platform being processed
     * @param file Full path to deliverable name (in ship dir)
     * @param followLink If true follow the symlink (replace sym link with
     *            actual file) if false keep the symlink
     * @throws IcofException
     * @throws IOException 
     */
    private void copyLink(EdaContext xContext, Platform_Db platform, 
                          PkgDeliverable file, boolean followLink)
    throws IcofException {
	
	String msg = "";
	if (followLink)
	    msg = " Want to link (follow) " + file.getAbsolutePath() + 
	          "\n  into " + getWorkPlatCompDir();
	else
	    msg = " Want to link (not follow) " + file.getAbsolutePath() + 
	          "\n  into " +  getWorkPlatCompDir();
	logInfo(xContext, msg, true);
	
	// Create any new sub directories in the working dir
	IcofFile workDelDir = createWorkingSubDirs(xContext, 
	                                           file.getPartialDelName());

	// Create the symlink or copy the file
	IcofFile actualDel = new IcofFile(file.getAbsolutePath(), false);
	if (followLink) {
	    if (isDryRun())
		logInfo(xContext, " DRYRUN: file not actually copied", true);
	    else {
		actualDel.copyPreserve(workDelDir, true);
		logInfo(xContext, " Link complete", verboseInd);

		IcofFile target = new IcofFile(getWorkPlatCompDir() + File.separator +
		                               file.getPartialDelName(), 
		                               false);
		validateDeliverables(xContext, file, target);
	    }
	}
	else {
	    if (isDryRun())
		logInfo(xContext, " DRYRUN: file not actually copied", true);
	    else {
		actualDel.copy(workDelDir, true, "-pP");
		logInfo(xContext, " Link complete", verboseInd);

//		IcofFile target = new IcofFile(getWorkPlatCompDir() + File.separator +
//		                               file.getPartialDelName(), 
//		                               false);
//	    	validateDeliverables(xContext, file, target);
	    }
	}

//	if (! followLink)
//	    throw new IcofException(this.getClass().getSimpleName(),
//				    "copyFile()", IcofException.SEVERE,
//				    "TESTING", "TESTING");

    }


    /**
     * Copy the deliverable from ship dir to work dir
     * 
     * @param xContext Application context
     * @param platform Name of platform being processed
     * @param file Full path to deliverable name (in ship dir)
     * 
     * @throws IcofException
     * @throws IOException
     */
    private void copyNutshell(EdaContext xContext, Platform_Db platform, 
                              PkgDeliverable file)
    throws IcofException, IOException {

	if (isDryRun() || getVerboseInd(xContext)) {
	    logInfo(xContext,
	            " Want to copy nutshell link ... " + file.getAbsolutePath(),
	            true);
	}
	
	// Create any new sub directories in the working dir
	IcofFile workDelDir = createWorkingSubDirs(xContext, 
	                                           file.getPartialDelName());

	// Create the nutshell link
	logInfo(xContext, " Creating nutshell link " + file.getAbsolutePath()
			  + "\n  in " + workDelDir.getAbsolutePath(),
		true);
	
	if (isDryRun())
	    logInfo(xContext, " DRYRUN: file not actually linked", true);
	else {
	    // Define the link command	
	    String command = "ln -sf ../../../nutshell/" + 
	    	             getToolKit().getToolKit().getRelease().getName() + 
	    	             "/bin-64/nutsh ";
	    if (getToolKit().getToolKit().getRelease().getName().equals("15.1")) {
		command = "ln -sf ../../../nutshell/" + 
		          getToolKit().getToolKit().getRelease().getName() + 
		          "/bin/nutsh ";
	    }
	    command += file.getFileName();
	    
	    PkgUtils.runCommandInDir(command, workDelDir, isDryRun(), 
	                             getLogger());
	    logInfo(xContext, " Nutshell link complete", verboseInd);
	}

    }


    /**
     * Create any missing sub directories in the work dir.
     * 
     * @param xContext Application context
     * @param partialName partial path of deliverable directory to create
     * @return Full path to work dir to copy file into
     * @throws IcofException
     */
    private IcofFile createWorkingSubDirs(EdaContext xContext,
					  String partialName)
    throws IcofException {

	return (PkgUtils.createMissingDirs(getWorkPlatCompDir(), partialName, 
	                                   false));
	
    }


    /**
     * Copy the deliverable from ship dir to work dir
     * 
     * @param xContext Application context
     * @param platform Name of platform being processed
     * @param file Full path to deliverable name (in ship dir)
     * 
     * @throws IcofException
     * @throws IOException
     */
    private void copyFile(EdaContext xContext, Platform_Db platform, 
                          PkgDeliverable file)
    throws IcofException, IOException {

	logInfo(xContext, " Want to copy " + file.getAbsolutePath() + 
	                  "\n  into " +
			  getWorkPlatCompDir().getAbsolutePath(), true);

	// Create any new sub directories in the working dir
	IcofFile workDelDir = createWorkingSubDirs(xContext, 
	                                           file.getPartialDelName());

	// Copy the file to working dir
	IcofFile actualDel = new IcofFile(file.getAbsolutePath(),
					  file.getCanonicalFile().isDirectory());

	if (isDryRun() || getVerboseInd(xContext)) {
	    logInfo(xContext, " Copying " + actualDel.getAbsolutePath() + "\n" +
	    		" to " + workDelDir.getAbsolutePath(), true);
	}

	if (isDryRun())
	    logInfo(xContext, " DRYRUN: file not actually copied", true);
	else {
	    // Run special copy if copy deliverable contains * in the name
	    if (file.getPartialDelName().contains("*")) {
		runCopySpecial(xContext, actualDel, workDelDir, file);
	    }
	    else {
		actualDel.copyPreserve(workDelDir, true);
		logInfo(xContext, " Copy complete", verboseInd);
	    }
	}
	
	// Verify the copy is correct by checking the target's checksum 
	// against the source checksum
	IcofFile target = new IcofFile(getWorkPlatCompDir() + File.separator +
	                               file.getPartialDelName(), 
	                               false);
	validateDeliverables(xContext, file, target);

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
				IcofFile workDelDir, PkgDeliverable file) 
    throws IcofException {

	String command = "cp -p -H " + actualDel.getAbsolutePath() + 
	                 " " + workDelDir.getAbsolutePath() + 
	                 File.separator + ".";

	logInfo(xContext, " SPECIAL CHAR file ", true);
	logInfo(xContext, "Running " + command, true);
	
	StringBuffer errors = new StringBuffer();
	Vector<String> results = new Vector<String>();
	int rc = IcofSystemUtil.execSystemCommand(command, errors, results);
	if (rc != 0) {
	    throw new IcofException(this.getClass().getName(), "runCopySpecial()", 
	                            IcofException.SEVERE, 
	                            "ERROR: unable to copy special " +
	                            "char file " + file.getName(), 
	                            errors.toString());
	}
	
    }


    /**
     * Validates the source and target deliverable are the same
     *
     * @param xContext  Application context
     * @param file      Source deliverable
     * @param target    Target deliverable
     * @throws IcofException 
     */
    private void validateDeliverables(EdaContext xContext, PkgDeliverable file,
				      IcofFile target) throws IcofException {

	if (isDryRun()) {
	    //logInfo(xContext, " DRYRUN: skipping checksum validation", true);
	    return;
	}
	
	// Compare checksums .. source vs target
	if (target.getChecksum() == file.getChecksum()) {
	    logInfo(xContext, " Copy validated .. (" + target.getChecksum() + 
	            " vs " + file.getCheckSum() + ")", verboseInd);
	}
	else {
	    String msg = "Copy validation FAILED";
	    logInfo(xContext, " " + msg, verboseInd);
	    throw new IcofException("PackageDeliverables", "copyFile()", 
	                            IcofException.SEVERE, msg, 
	                            "SOURCE: " + file.getAbsolutePath() +
	                            "(" + file.getCheckSum() + ")\n" + 
	                            "TARGET: " + target.getAbsolutePath() +
	                            "(" + target.getChecksum() + ")");
	}
	
    }


    /**
     * Setup the working directories to receive the deliverables
     * 
     * @param xContext Application context
     * @param platform Name of platform to be processed
     * @throws IcofException 
     */
    private void configureEnvironment(EdaContext xContext) throws IcofException {

	logInfo(xContext, "\nInitializing packaging environment ...", true);
	
	if (! getTgtPkgDir().exists()) {
	    getTgtPkgDir().create();
	}

	if (! getTgtWorkDir().exists()) {
	    getTgtWorkDir().create();
	}

    }
 

    /**
     * Define the command line switches
     * 
     * @param singleSwitches Collection of switches that don't need args
     * @param argSwitches Collection of switches that require arguments
     */
    protected void createSwitches(Vector<String> singleSwitches,
				  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	singleSwitches.add("-dryrun");
	singleSwitches.add("-full");
	singleSwitches.add("-incremental");
	singleSwitches.add("-dont_xlate_links");
	singleSwitches.add("-agts");
	singleSwitches.add("-ctk");
	argSwitches.add("-db");
	argSwitches.add("-t");
	argSwitches.add("-c");
	argSwitches.add("-T");
	argSwitches.add("-z");
    }


    /**
     * Process the command line arguments
     * 
     * @param params Collection of command line arguments
     * @param errors String to hold any error messages
     * @param xContext Application context object
     */
    protected String readParams(Hashtable<String, String> params,
				String errors, EdaContext xContext)
    throws IcofException {

	// Read the Tool Kit option
	if (params.containsKey("-t")) {
	    setToolKit(xContext, params.get("-t"));
	}
	else {
	    errors += "ToolKit (-t) is a required parameter\n";
	}

	// Read the Component option
	if (params.containsKey("-c")) {
	    setComponent(xContext, params.get("-c"));
	}
	else {
	    errors += "Component (-c) is a required parameter\n";
	}

	// Read the dryrun switch
	setDryRun(false);
	if (params.containsKey("-dryrun")) {
	    setDryRun(true);
	}

	// Read the incremental mode switch
	setIncrementalPackage(true);
	if (params.containsKey("-full"))
	    setIncrementalPackage(false);
	
	// Read the dont_xlate switch
	setDontTranslate(false);
	if (params.containsKey("-dont_xlate")) {
	    setDontTranslate(true);
	}

	// Read the Change Request switch
	boolean crFound = false;
	if (params.containsKey("-z")) {
	    setChangeReqText(params.get("-z"));
	    setAgts(false);
	    crFound = true;
	}

	// Read the TK Patch switch
	if (params.containsKey("-T")) {
	    if (crFound) {
		errors += "Can't specific -z and -T switches in same invocation";
	    }
	    else { 
		setPatchName((String) params.get("-T"));
		setAgts(false);	
		crFound = true;
	    }
	}

	// Read the AGTS switch
	setAgts(false);
	if (params.containsKey("-agts")) {
	    if (crFound)
		errors += "Can't specific -z, -T or -agts switches in same invocation";
	    else {
		setAgts(true);
		crFound = true;
	    }
	}

	// Read the CTK switch
	setCustomTk(false);
	if (params.containsKey("-ctk")) {
	    if (crFound)
		errors += "Can't specific -z, -T, -agts or -ctk switches in same invocation";
	    else 
		setCustomTk(true);
	}

	
	return errors;

    }


    /**
     * Display input data
     * 
     * @param dbMode Database mode
     * @param xContext Application context
     */
    protected void displayParameters(String dbMode, EdaContext xContext) {

	logInfo(xContext, "App           : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	logInfo(xContext, "Tool Kit      : " + getToolKit().getName(),
		verboseInd);
	logInfo(xContext, "Component     : " + getComponent().getName(),
		verboseInd);
	if (getChangeRequest() != null)
	    logInfo(xContext, "Change Request: "
			      + getChangeRequest().getClearQuest(), verboseInd);
	else
	    logInfo(xContext, "Change Request: NULL", verboseInd);
	if (getPatchName() != null)
	    logInfo(xContext, "Patch         : " + getPatchName(), verboseInd);
	else
	    logInfo(xContext, "Patch         : NULL", verboseInd);
	logInfo(xContext, "AGTS mode     : " + isAgts(), verboseInd);
	logInfo(xContext, "CTK mode      : " + isCustomToolKit(), verboseInd);
	logInfo(xContext, "Dry Run       : " + isDryRun(), verboseInd);
	logInfo(xContext, "Incremental   : " + isIncrementalPackage(), verboseInd);
	logInfo(xContext, "Dont translate: " + isDontTranslate(), verboseInd);
	logInfo(xContext, "DB mode       : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose       : " + verboseInd, verboseInd);
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Find and package deliverables for the given tool kit\n");
	usage.append("and component.  The default behavior is to package\n");
	usage.append("only deliverables that have changed.  If you need to\n");
	usage.append("package all deliverables then use the -full switch.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t tool_kit> <-c component> \n");
	usage.append("           <-T patch | -z change_req | -agts | -ctk>\n");
	usage.append("           [-full] [-dryrun] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  tool_kit    = Tool kit name (ie, 14.1.5, 14.1.6 ... )\n");
	usage.append("  component   = Component name (ie, ess, pds ... )\n");
	usage.append("  patch       = Name of TK Patch to be packaged (ie, MDCMS00123456)\n");
	usage.append("  change_req  = Comma delimited list of Change Request to be packaged\n");
	usage.append("  -agts       = Determine change requests from whats new \n");
	usage.append("                in this TK (use for initial Preview TK only)\n");
	usage.append("  -ctk        = Used for initial CTK packages only ... \n");
	usage.append("                does not do anything with change requests\n");
	usage.append("  -full       = Add all deliverables to package\n");
	usage.append("  -y          = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode      = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -dryrun     = Do NOT package anything just see what would get pkg'd\n");
	usage.append("  -h          = Help (shows this information)\n");
	usage.append("\n");
	usage.append("Return Codes\n");
	usage.append("------------\n");
	usage.append(" 0 = application ran ok\n");
	usage.append(" 1 = application error\n");
	usage.append("\n");

	System.out.println(usage);

    }


    /**
     * Members.
     */
    int changedCount = 0;
    private boolean incrementalPackage = false;
    private boolean incrementalInstall = false;
    private boolean dontTranslateFlag = false;
    private boolean dryRunFlag = false;
    private Location location;
    private PkgControlFile addonFile;
    private PkgControlFile dontShipFile;
    private PkgControlFile symlinkNotFollowFile;
    private PkgControlFile customDeliverFile;
    private PkgControlFile specialPathFile;
    private PkgControlFile prevDeliverablesFile;
    private String patchName;
    private String topLevelDirectory;
    private IcofFile sourceDirectory;
    private IcofFile tgtWorkDirectory;
    private IcofFile tgtPkgDirectory;
    private IcofFile tgtPkgControlDir;
    private ArrayList<Platform_Db> platforms = new ArrayList<Platform_Db>();
    private HashMap<Platform_Db, Deliverables> allDeliverables;
    private HashMap<Platform_Db, Deliverables> newDeliverables;
    private HashMap<Platform_Db, Deliverables> excludedDeliverables;
    private HashMap<Platform_Db, Deliverables> unchangedDeliverables;
    private HashMap<Platform_Db, Deliverables> deletedDeliverables;
    private HashMap<Platform_Db, ComponentPackage> previousComponentPkgs;
    private HashMap<Platform_Db, ComponentPackage> newComponentPkgs;
    private ChangeRequests changeReqs;
    private ToolKitPackage toolKitPkg;
    private boolean repackage = false;
    private boolean agtsFlag = false;
    private boolean customTkFlag = false;
    private Deliverables prevDels;
    private IcofFile workingPlatformDir;
    private IcofFile workingPlatformComponentDir;
    private String specialPath;
    private String specialPathPkgDir;
    private String osName;
    private String changeReqText;
    private IcofFile currMarVerFile;
    private IcofFile prevMarVerFile;
    private boolean foundBin64 = false;
    private boolean foundDll64 = false;
    private boolean foundLib64 = false;
        
    
    /**
     * Getters. 
     * @formatter:off
     */
    public int getChangedCount()  { return changedCount; }
    public boolean isIncrementalPackage() { return incrementalPackage; }
    public boolean isIncrementalInstall() { return incrementalInstall; }
    public boolean isDontTranslate() { return dontTranslateFlag; }
    public boolean isDryRun() {	return dryRunFlag; }
    public boolean isCustomToolKit() {	return customTkFlag; }    
    public Location getLocation() { return location; }
    public PkgControlFile getAddonFile() { return addonFile; }
    public PkgControlFile getDontShipFile() { return dontShipFile; }
    public PkgControlFile getSymlinkNotFollowFile() { return symlinkNotFollowFile; }
    public PkgControlFile getCustomDeliverFile() { return customDeliverFile; }
    public PkgControlFile getSpecialPathFile() { return specialPathFile; }
    public PkgControlFile getPrevDeliverablesFile() { return prevDeliverablesFile; }
    public IcofFile getControlDir() { return tgtPkgControlDir; }
    public String getTopLevelDir() {return topLevelDirectory; }
    public IcofFile getSourceDir() { return sourceDirectory; }
    public IcofFile getTgtWorkDir() { return tgtWorkDirectory; }
    public IcofFile getTgtPkgDir() { return tgtPkgDirectory; }
    public HashMap<Platform_Db, Deliverables> getNewDeliverables() { return newDeliverables; }
    public ChangeRequests getChangeRequests() { return changeReqs; }
    public ToolKitPackage getToolKitPkg() { return toolKitPkg; }
    public String getPatchName()  { return patchName; }
    public ArrayList<Platform_Db> getPlatforms()  { return platforms; }
    public boolean isRepackage() { return repackage; }
    public HashMap<Platform_Db, ComponentPackage> getPreviousCompPkgs() {return previousComponentPkgs; }
    public HashMap<Platform_Db, ComponentPackage> getNewCompPkgs() {return newComponentPkgs; }
    public Deliverables getPrevDels() {return prevDels; }
    public boolean isAgts() { return agtsFlag; }
    public IcofFile getWorkPlatDir() { return workingPlatformDir; }
    public IcofFile getWorkPlatCompDir() { return workingPlatformComponentDir; }
    public String getSpecialPath() { return specialPath; }
    public String getSpecialPathPkgDir() { return specialPathPkgDir; }
    public String getOsName() { return osName; }
    public String getChangeReqText() { return changeReqText; }
    public IcofFile getCurrMarVerFile() { return currMarVerFile; }
    public IcofFile getPrevMarVerFile() { return prevMarVerFile; }

    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }

    

    /**
     * Setters.
     */
    private void setIncrementalPackage(boolean aFlag) { incrementalPackage = aFlag; }
    private void setIncrementalInstall(boolean aFlag) { incrementalInstall = aFlag; }
    private void setAgts(boolean aFlag) { agtsFlag = aFlag; }
    private void setCustomTk(boolean aFlag) { customTkFlag = aFlag; }
    private void setDontTranslate(boolean aFlag) { dontTranslateFlag = aFlag; }
    private void setDryRun(boolean aFlag) { dryRunFlag = aFlag; }
    private void setPatchName(String aName) { patchName = aName; }
    private void setSpecialPath(String aName) { specialPath = aName; }
    private void setSpecialPathPkgDir(String aName) { specialPathPkgDir = aName; }
    private void setOsName(String aName) { osName = aName; }
    private void setChangeReqText(String aName) { changeReqText = aName; }
    // @formatter:on(non-Javadoc)


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {

	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };

    }


}
