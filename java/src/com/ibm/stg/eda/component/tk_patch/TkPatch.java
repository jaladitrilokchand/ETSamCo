/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *    DATE: 11/10/2009
 *
 *-PURPOSE---------------------------------------------------------------------
 * Class for EDA Tool Kit patch data
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 11/10/2009 GFS  Initial coding.
 * 01/08/2010 GFS  Changed the backup logic to only backup the source file once.
 *                 Added setDuplicateFiles() method.
 * 02/01/2010 GFS  Moved cmvcRelease to CMVC object.     
 * 02/09/2010 GFS  Add buildLevelHist and targetLevelHist members.   
 * 02/17/2010 GFS  Added clearTrackList() to support using shipb CMVC releases 
 *                 and extracting tracks from CMVC.       
 * 03/02/2010 GFS  Fixed a bug in the setCopyExtractActions() method.
 * 03/04/2010 GFS  Merged changes from bugfix and test streams.
 * 03/09/2010 GFS  Fixed a problem when the patch's files updated field is empty.
 *                 Updated copyHeaderFiles and compileMsgcatFiles methods.
 * 04/26/2010 GFS  Added STATE_BUILT and made setState() public.     
 * 09/28/2010 GFS  Changed backupSource so it will not try to backup a source
 *                 file that doesn't exist.   
 * 10/07/2010 GFS  Added setUpdateToBuilt() method to support writing to the 
 *                 .update file.     
 * 10/11/2010 GFS  Updated copyHeaderFiles() to copy header files to include 
 *                 directory only if the header file already exists in include 
 *                 dir.
 * 04/09/2012 GFS  Updated to support xtinct injects.
 * 05/08/2012 GFS  Updated to support xtinct venice injects.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_patch;

import java.io.File;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_levelhist.CMVC;
import com.ibm.stg.eda.component.tk_levelhist.CMVC_File;
import com.ibm.stg.eda.component.tk_levelhist.CMVC_Track;
import com.ibm.stg.eda.component.tk_levelhist.LevelHist;
import com.ibm.stg.eda.component.tk_levelhist.LevelHistUtils;
import com.ibm.stg.iipmds.common.IcofDateUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofSystemUtil;
import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;

public class TkPatch implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6799641081348055673L;


    /**
     * Constructor
     * 
     * @param labels
     * @param results
     * @throws IcofException 
     */
    public TkPatch(Vector<String[]> labels, Vector<String[]> results)  
    throws IcofException {
	setMembers(labels, results);
	setBuildLevelHist();
    }


    /**
     * Constructor
     * 
     * @param component
     * @param id
     * @param injectRequests
     * @param injector
     * @param release
     * @param state
     * @param transmitReady
     */
    public TkPatch(String component,
                   String id,
                   TreeMap<String,TkInjectRequest> injectRequests,
                   String injector,
                   String release,
                   String state,
                   String buildReady,
                   String transmitReady) {
	setComponent(component);
	setToolName();
	setId(id);
	setInjectRequests(injectRequests);
	setInjector(injector);
	setRelease(release);
	setState(state);
	setTransmitReady(transmitReady);
	setBuildReady(buildReady);
	setToolVersion();
	setBuildLevelHist();

    }


    /**
     * Clear the trackList.
     */
    public void initializeTrackList() {

	// Initialize the track collection.
	if (cmvcTracks == null) {
	    cmvcTracks = new Hashtable<String, CMVC_Track>();
	}
	else {
	    cmvcTracks.clear();
	}

    }


    /**
     * Clear the missing CMVC track collection.
     */
    public void initializeMissingCmvcTracks() {

	// Initialize the track collection.
	if (missingCmvcTracks == null) {
	    missingCmvcTracks = new StringBuffer();
	}
	else {
	    missingCmvcTracks.setLength(0);
	}

    }


    /**
     * Clear the missing build track collection.
     */
    public void initializeMissingBuildTracks() {

	// Initialize the track collection.
	if (missingBuildTracks == null) {
	    missingBuildTracks = new StringBuffer();
	}
	else {
	    missingBuildTracks.setLength(0);
	}

    }


    /**
     * Clear the selected requests, tracks and files collection.
     */
    public void initializeSelectedItems() {

	if (selectedRequests == null) {
	    selectedRequests = new HashSet<TkInjectRequest>();
	}
	else {
	    selectedRequests.clear();
	}

	if (selectedTracks == null) {
	    selectedTracks = new HashSet<String>();
	}
	else {
	    selectedTracks.clear();
	}

	if (selectedFiles == null) {
	    selectedFiles = new HashSet<TkSource>();
	}
	else {
	    selectedFiles.clear();
	}


    }



    /**
     * Loads the members with data from the input collections.
     * 
     * @param inLabels
     * @param inResults
     * @throws IcofException 
     */
    private void setMembers(Vector<String[]> inLabels, Vector<String[]> inResults)
    throws IcofException {

	track2request = new Hashtable<String, String>();

	// Return if the input collections are null.
	if ((inLabels == null) || (inResults == null)) {
	    throw new IcofException("TkPatch", "setMembers()",
	                            IcofException.SEVERE,
	                            "Inject request lables/results are empty.",
	    "");
	}

	// Parse the labels.
	TreeMap<String, String> myLabels = new TreeMap(ManagerFunctions.STRINGCOMPARE);
	for (int i = 0; i < inLabels.size(); i++) {
	    String[] labels = (String[]) inLabels.get(i);
	    myLabels.put(labels[0], Integer.toString(i));
	}

	// Parse the content.
	injectRequests = new TreeMap(ManagerFunctions.STRINGCOMPARE);
	for (int i = 0; i < inResults.size(); i++) {
	    Object[] oValues = (Object[]) inResults.get(i);

	    // Collect the patch data
	    if (i == 0) {

		// Set the ID.
		String index = (String) myLabels.get(ID);
		setId((String) oValues[Integer.parseInt(index)]);

		// Set the Component and Tool name.
		index = (String) myLabels.get(COMPONENT);
		setComponent((String) oValues[Integer.parseInt(index)]);
		setToolName();

		// Set the Injector.
		index = (String) myLabels.get(INJECTOR);
		setInjector((String) oValues[Integer.parseInt(index)]);

		// Set the Release.
		index = (String) myLabels.get(RELEASE);
		setRelease((String) oValues[Integer.parseInt(index)]);

		// Set the State.
		index = (String) myLabels.get(STATE);
		setState((String) oValues[Integer.parseInt(index)]);

		// Set the Transmit Ready.
		index = (String) myLabels.get(BUILD_READY);
		if (index != null) {
		    String ready = (String) oValues[Integer.parseInt(index)];
		    setBuildReady(ready);
		}
		
		// Set the Transmit Ready.
		index = (String) myLabels.get(TRANSMIT_READY);
		if (index != null) {
		    String ready = (String) oValues[Integer.parseInt(index)];
		    setTransmitReady(ready);
		}
		
		
	    }

	    // Load the Inject Request data.
	    String index = (String) myLabels.get(INJECT_REQUESTS);
	    String requestId = (String) oValues[Integer.parseInt(index)];

	    index = (String) myLabels.get(CMVC_PRIMARY);
	    String trackPrimary = (String) oValues[Integer.parseInt(index)];

	    index = (String) myLabels.get(CMVC_SECONDARY);
	    String tracksSecondary = (String) oValues[Integer.parseInt(index)];

	    index = (String) myLabels.get(FILES_UPDATED);
	    String filesUpdated = (String) oValues[Integer.parseInt(index)];

	    index = (String) myLabels.get(DEVELOPER);
	    String developer = (String) oValues[Integer.parseInt(index)];

	    TkInjectRequest request = new TkInjectRequest(requestId,
	                                                  trackPrimary,
	                                                  tracksSecondary,
	                                                  filesUpdated,
	                                                  developer);

	    injectRequests.put(requestId, request);
	    updateTrack2Request(request);

	}

	// Set the tool version.
	setToolVersion();
	setTargetLocation("");

    }


    /**
     * Adds this request to the track2request collection.
     * 
     * @param request A TkInjectRequest object
     */
    private void updateTrack2Request(TkInjectRequest request) {

	// Get the tracks for this request.
	Iterator<String> iter = request.getAllTracks().iterator();
	while (iter.hasNext()) {
	    String track = (String) iter.next();
	    track2request.put(track, request.getId());
	}

    }


    /*
     * Constants
     */
    public static String CLASS_NAME = "TkPatch";
    public static String DB_ID = "dbid";
    public static String ID = "id";
    public static String COMPONENT = "tk_component";
    public static String STATE = "State";
    public static String BUILD_READY = "buildready";
    public static String TRANSMIT_READY = "transmit_ready";
    public static String INJECTOR = "injector";
    public static String TRANSMITTOE = "transmitter";
    public static String INJECT_REQUESTS = "tk_injectionrequests";
    public static String TESTERS = "testers_additional_all";
    public static String COMMENTS = "comment_log";
    public static String HISTORY = "history";
    public static String RELEASE = "tk_release_num";
    public static String IS_DUPLICATE = "is_duplicate";
    public static String CMVC_PRIMARY = "tk_injectionrequests.cmvc_primary";
    public static String CMVC_SECONDARY = "tk_injectionrequests.cmvc_secondary";
    public static String FILES_UPDATED = "tk_injectionrequests.files_updated";
    public static String DEVELOPER = "tk_injectionrequests.developer";
    public static String STATE_BUILT = "Built";


    /*
     * Getters
     */
    public String getId() { return id; }
    public String getComponent() { return component;  }
    public String getState() { return state; }
    public String getBuildReady() { return buildReady; }
    public String getTransmitReady() { return transmitReady; }
    public String getInjector() { return injector; }
    public TreeMap<String,TkInjectRequest> getInjectRequests() { return injectRequests; }
    public String getRelease() { return release; }
    public String getToolVersion() { return toolVersion; }
    public Hashtable<String, CMVC_Track> getCmvcTracks() { return cmvcTracks; }
    public String getTargetLocation() { return targetLocation; }
    public Hashtable<String, String> getBackupActions() { return backupActions; }
    public Hashtable<String, String> getCopyActions() { return copyActions; }
    public Hashtable<String, CMVC_File> getExtractFileActions() { return extractFileActions; }
    public Hashtable<String, String> getTrackExtractList() { return extractTrackList; }
    public StringBuffer getMissingCmvcTracks() { return missingCmvcTracks; }
    public StringBuffer getMissingBuildTracks() { return missingBuildTracks; }
    public Hashtable<String, String> getTrack2RequestLookup() { return track2request; }
    public Hashtable<String, String> getDuplicateFiles() { return duplicateFiles; }
    public IcofFile getLogFile() { return logFile; }
    public String getToolName() { return toolName; }
    public String getTargetDirectory() { return targetDirectory; }
    public LevelHist getBuildLevelHist() { return buildLevelHist; }
    public LevelHist getTargetLevelHist() { return targetLevelHist; }
    public String getTrackExtractCommand() { return trackExtractCommand; }
    public HashSet<TkInjectRequest> getSelectedRequests() { return selectedRequests; }
    public HashSet<String> getSelectedTracks() { return selectedTracks; }
    public HashSet<TkSource> getSelectedFiles() { return selectedFiles; }


    /*
     * Setters
     */
    private void setId(String anId) { id = anId; }
    private void setComponent(String aComp) { component = aComp;  }
    private void setToolName() { toolName = TkInjectUtils.getToolName(component);  }
    public void setState(String aState) { state = aState; }
    private void setBuildReady(String aName) { buildReady = aName; }
    private void setTransmitReady(String aName) { transmitReady = aName; }
    private void setInjector(String aName) { injector = aName; }
    private void setInjectRequests(TreeMap<String,TkInjectRequest> aReq) { injectRequests = aReq; }
    private void setToolVersion() {
	toolVersion = TkInjectUtils.getTkVersion(getRelease());
    }
    public void setTargetLocation(String aLocation) { 
	targetLocation = aLocation;
	setTargetDirectory();
    }
    public void setRelease(String aRelease) { release = aRelease; }
    public void addCmvcTrack(CMVC_Track track) {
	cmvcTracks.put(track.getName(), track);
    }
    public void setTrackExtractCommand(String command) { trackExtractCommand = command; }


    /*
     * Members
     */
    private String id;
    private String component;
    private String state;
    private String transmitReady;
    private String buildReady;
    private String injector;
    private TreeMap<String, TkInjectRequest> injectRequests;
    private String release;
    private String toolVersion;
    private String toolName;
    private Hashtable<String, CMVC_Track> cmvcTracks;
    private String targetLocation;
    private Hashtable<String, String> backupActions;
    private Hashtable<String, String> copyActions;
    private Hashtable<String, CMVC_File> extractFileActions;
    private Hashtable<String, String> extractTrackList;
    private StringBuffer missingCmvcTracks;
    private StringBuffer missingBuildTracks;
    private Hashtable<String, String> track2request;
    private Hashtable<String, String> duplicateFiles;
    private IcofFile logFile;
    private String targetDirectory;
    private LevelHist buildLevelHist;
    private LevelHist targetLevelHist;
    private String trackExtractCommand;
    private HashSet<TkInjectRequest> selectedRequests;
    private HashSet<String> selectedTracks;
    private HashSet<TkSource> selectedFiles;


    /**
     * Create a collection of source files to be backed up.
     * 
     */
    public void setBackupActions() {

	// Reset the backup actions.
	if (backupActions != null) {
	    backupActions.clear();
	}
	else {
	    backupActions = new Hashtable<String, String>();
	}

	// Create a list of files to be injected.
	Iterator<TkInjectRequest> requests = selectedRequests.iterator();
	while (requests.hasNext()) {
	    TkInjectRequest request = (TkInjectRequest) requests.next();

	    Iterator<TkSource> sources = request.getSourceFiles().values().iterator();
	    while (sources.hasNext()) {
		TkSource srcFile = (TkSource) sources.next();

		if (srcFile.isActive()) {
		    String src = getTargetDirectory() + File.separator 
		    + srcFile.getName();
		    String tgt = src + "." + request.getTrackPrimary();
		    backupActions.put(tgt, src);
		}

	    }

	}

    }


    /**
     * Set the target directory.
     */
    private void setTargetDirectory() {

	String toolName = TkInjectUtils.getToolName(getComponent());

	targetDirectory = "/afs/eda/"; 
	if (getTargetLocation().equals("xtinct")) {
	    targetDirectory += getTargetLocation() + File.separator +
	    "tk" + getRelease().substring(0, 7);       
	}
	else if (getTargetLocation().equals("xtinct2")) {
	    targetDirectory += "xtinct" + File.separator +
	    "tk" + getRelease().substring(0, 7) + "venice";  
	}
	else {
	    targetDirectory += getTargetLocation();
	}
	targetDirectory += File.separator + toolName + 
	File.separator + getToolVersion();

    }

    /**
     * Create a collection of files to copy or extract.
     */
    public void setCopyExtractActions() {

	// Reset the copy actions.
	if (copyActions != null) {
	    copyActions.clear();
	}
	else {
	    copyActions = new Hashtable<String, String>();
	}

	// Reset the extract actions.
	if (extractFileActions != null) {
	    extractFileActions.clear();
	}
	else {
	    extractFileActions = new Hashtable<String, CMVC_File>();
	}

	// Reset the extract tracks list.
	if (extractTrackList != null) {
	    extractTrackList.clear();
	}
	else {
	    extractTrackList = new Hashtable<String, String>();
	}


	// Create the target directory.
	String targetDir = getTargetDirectory();

	// Create a list of files to be injected.
	Iterator<TkInjectRequest> requests = selectedRequests.iterator();
	while (requests.hasNext()) {
	    TkInjectRequest request = (TkInjectRequest) requests.next();
	    request.getFilesUpdated();

	    Iterator<TkSource> sources = request.getSourceFiles().values().iterator();
	    while (sources.hasNext()) {
		TkSource srcFile = (TkSource) sources.next();

		if (srcFile.isActive()) {

		    // Handle the CMVC file extractions.
		    if (srcFile.getFullPath().equals(TkInjectUtils.CMVC_FILE_EXT)) {
			CMVC_File cmvcFile = getCmvcFile(request, srcFile.getName());
			extractFileActions.put(srcFile.getName(), cmvcFile);

		    }
		    // Handle the CMVC track extractions.
		    else if (srcFile.getFullPath().equals(TkInjectUtils.CMVC_TRACK_EXT)) {
			for (int i = 0; i < request.getAllTracks().size(); i++) {
			    String track = (String) request.getAllTracks().get(i);
			    extractTrackList.put(track, track);
			}


		    }
		    // Handle the file copies.
		    else {
			String src = srcFile.getFullPath();
			String tgt = targetDir + File.separator + srcFile.getName();
			copyActions.put(tgt, src);

		    }

		}

	    }

	}

    }


    /**
     * Finds the CMVC_File object for this request and fileName.
     * 
     * @param request
     * @param src
     * @return
     */
    private CMVC_File getCmvcFile(TkInjectRequest request, String fileName) {

	CMVC_File cmvcFile = null;
	boolean found = false;

	// Iterate through the CMVC_Track objects.
	Iterator<String> tracks = request.getAllTracks().iterator();
	while (tracks.hasNext() && !found) {
	    String track = (String) tracks.next();
	    CMVC_Track cmvcTrack = (CMVC_Track) getCmvcTracks().get(track);

	    // Iterate through the CMVC_File objects for this CMVC_Track object.
	    Iterator<CMVC_File> files = cmvcTrack.getFiles().values().iterator();
	    while (files.hasNext() && !found) {
		cmvcFile = (CMVC_File) files.next();
		if (cmvcFile.getPath().equals(fileName)) {
		    found = true;
		}
	    }
	}

	// If found then return the object otherwise return false.
	if (! found) {
	    cmvcFile = null;
	}

	return cmvcFile;

    }




    /**
     * Write to the log file.
     * 
     * @param text   Text to write to log file/screen
     * @param bEcho  If true echo the message to the screen.
     * @throws IcofException 
     */
    public void logIt(String text, boolean bEcho) throws IcofException {

	// Display message to the screen if echo is true.
	if (bEcho)
	    System.out.println(text);

	// Get the log file object.
	if (logFile == null) {
	    String logFileName = TkInjectUtils.getLogFileName(getId(), 
	                                                      getComponent(), 
	                                                      null, true);
	    logFile = new IcofFile(logFileName, false);
	    writeLogHeader();
	    logFile.validate(true);
	}

	// Open the log file and write the message to it
	try {
	    logFile.openAppend();
	    logFile.writeLine(text, bEcho);
	}
	finally {
	    logFile.closeAppend();
	}

    }


    /**
     * Write a header to the log file. 
     * @throws IcofException 
     */
    private void writeLogHeader() throws IcofException {

	StringBuffer header = new StringBuffer();

	// Get the host name
	String host  = "unknown";
	try {
	    InetAddress localMachine =  InetAddress.getLocalHost();
	    host = localMachine.getHostName();
	}
	catch (Exception ex) {}

	// Get the time.
	Calendar cal = Calendar.getInstance();
	Date now = cal.getTime();
	String time = IcofDateUtil.formatDate(now,
	                                      IcofDateUtil.ACOS_REQUEST_DATE_FORMAT);

	// Add patch info.
	header.append("============================================\n");
	header.append("User: " + System.getProperty("user.name") + "\n");
	header.append("Host: " + host + "\n");
	header.append("TK Patch: " + getId() + "\n");
	header.append("State: " + getState() + "\n");
	header.append("Time: " +  time + "\n");
	header.append("============================================\n");

	logIt(header.toString(), false);

    }


    /**
     * Backup any source files which will be injected.
     * 
     * @throws IcofException 
     */
    public void backupSource(boolean debug) throws IcofException {

	logIt("Backing up source files\n--------------------", false);

	// For each backup source file copy the source file to the tgt file.
	Iterator<String> iter = getBackupActions().keySet().iterator();
	while (iter.hasNext()) {
	    String tgt = (String) iter.next();
	    String src = (String) getBackupActions().get(tgt);

	    // Create the IcofFile object.
	    IcofFile srcFile = new IcofFile(src, false);
	    IcofFile tgtFile = new IcofFile(tgt, false);

	    // If source file doesn't exist then nothing to backup
	    if (! srcFile.exists()) {
		logIt("Unable to backup source that doesn't exist ...\n" +
		"  File not found " + srcFile.getAbsolutePath() +  "\n",
		false);
	    }
	    else {
		// Backup the original source file. If the file has already been 
		// backed then don't do it again.
		if (! tgtFile.exists()) {
		    if (! debug) {
			logIt(" Copying " + srcFile.getAbsolutePath() +  "\n" +
			"   to " + tgtFile.getAbsolutePath(), false);
			srcFile.copyPreserve(tgtFile, true);            
		    }
		    else {
			logIt("DEBUG: would copy " + srcFile + "\n  to " + tgtFile,
			      true);
		    }
		}
	    }
	}

    }


    /**
     * Copy the injected source files.
     * 
     * @throws IcofException 
     */
    public void copySource(boolean debug) throws IcofException {

	if (getCopyActions().size() > 0) {
	    logIt("\nInjecting source files\n--------------------", false);
	}

	// For each copy source file copy the source file to the tgt file.
	Iterator<String> iter = getCopyActions().keySet().iterator();
	while (iter.hasNext()) {
	    String tgt = (String) iter.next();
	    String src = (String) getCopyActions().get(tgt);

	    // Create the IcofFile object.
	    IcofFile srcFile = new IcofFile(src, false);
	    IcofFile tgtFile = new IcofFile(tgt, false);
	    if (! debug) {
		logIt(" Copying " + srcFile.getAbsolutePath() +  "\n" +
		"   to " + tgtFile.getAbsolutePath(), false);
		srcFile.copy(tgtFile, true);
	    }
	    else {
		logIt("DEBUG: would copy " + srcFile + "\n  to " + tgtFile, 
		      true);
	    }

	}

    }


    /**
     * Extract the injected source files from CMVC.
     * 
     * @param cmvc      CMVC object
     * @param debug     If true don't extract files.
     * @throws IcofException 
     */
    public void extractSource(CMVC cmvc, boolean debug)
    throws IcofException {

	if (getExtractFileActions().size() > 0) {
	    logIt("\nExtracting source files from CMVC\n--------------------",
	          false);
	}

	// For each extracted source file extract the source file from CMVC 
	// into the tgt directory.
	Iterator<String> iter = getExtractFileActions().keySet().iterator();
	while (iter.hasNext()) {
	    String fileName = (String) iter.next();
	    CMVC_File cmvcFile = (CMVC_File) getExtractFileActions().get(fileName);

	    // Extract the source file
	    if (! debug) {
		logIt(" Extracting " + fileName + " (" + cmvcFile.getSid() + 
		      ") from " + cmvc.getRelease() + "\n  to " + 
		      getTargetDirectory() + "", false);
		cmvc.extractFile(fileName, cmvcFile.getSid(), getTargetDirectory());
	    }
	    else {
		logIt("Would extract " + fileName + " (" + cmvcFile.getSid() 
		      + ") from " + cmvc.getRelease() + " into " + 
		      getTargetDirectory(), true);
	    }

	}

    }


    /**
     * Update the .update file in the source directory with the current time 
     * @throws IcofException
     */
    public void setUpdateToBuilt(CMVC cmvc, boolean debug) throws IcofException {

	logIt("Updating .update file with build info\n--------------------", false);

	// Create an .update file object.
	IcofFile updateFile = new IcofFile(getTargetDirectory() + 
	                                   IcofFile.separator + ".update", false);

	// Read the existing .update file.
	if (updateFile.exists()) {
	    try {
		updateFile.openRead();
		updateFile.read();
	    }
	    catch(IcofException ex) {
		throw new IcofException(CLASS_NAME, "setUpdateToBuilt()",
		                        IcofException.SEVERE, 
		                        "Unable to open .update file. \n",
		                        "File: " + updateFile.getAbsolutePath());
	    }
	    finally {
		if (updateFile.isOpenRead()) {
		    try {  
			updateFile.closeRead();
		    }
		    catch(IcofException ignore) {}
		}
	    }
	}

	// Construct new build line (einstimer.1301 09/28/10 12:04 build)
	String newEntry = cmvc.getRelease() + " " + 
	LevelHistUtils.constructUpdateDateTime() + " build"; 

	// Replace the old build entry with a current one or add a new entry.
	boolean found = false;
	Vector<String> newContents = new Vector<String>();
	if (updateFile.exists()) {
	    for (int i = 0; i < updateFile.getContents().size(); i++) {
		String line = (String) updateFile.getContents().get(i);
		if (line.indexOf("build") > -1) {
		    line = newEntry;
		    found = true;
		}
		newContents.add(line);
	    }
	}

	if (! found) {
	    newContents.add(newEntry);
	}

	// Write the new file
	try {
	    updateFile.openWrite();
	    updateFile.write(newContents);
	}
	catch(IcofException ex) {
	    throw new IcofException(CLASS_NAME, "setUpdateToBuilt()",
	                            IcofException.SEVERE, 
	                            "Unable to write new contents to .update file. \n",
	                            "File: " + updateFile.getAbsolutePath());
	}
	finally {
	    if (updateFile.isOpenWrite()) {
		updateFile.closeWrite();
	    }
	}

    }


    /**
     * Extract tracks from CMVC.
     * 
     * @param cmvc      CMVC object
     * @param debug     If true don't extract files.
     * @throws IcofException 
     */
    public void setTrackExtractCommand(CMVC cmvc, boolean debug)  
    throws IcofException {

	if (getTrackExtractList().size() > 0) {
	    logIt("\nExtracting tracks from CMVC\n--------------------",
	          false);
	}

	// For each track to be extracted create a space delimited list of 
	// track names.
	String trackList = new String();
	Iterator<String> iter = getTrackExtractList().keySet().iterator();
	while (iter.hasNext()) {
	    String track = (String) iter.next();
	    if (! trackList.equals("")) {
		trackList += " ";
	    }
	    trackList += track;

	}


	// Construct the extract command.
	trackExtractCommand = cmvc.getLevelExtractCommand(getRelease(), 
	                                                  getTargetDirectory(), 
	                                                  trackList);

    }


    /**
     * Create a collection of changed files that appear in more than 1 
     * injection request.
     */
    public void setDuplicateFiles() {   

	// Initialize the dupFiles collection.
	if (duplicateFiles == null) {
	    duplicateFiles = new Hashtable<String, String>();
	}
	else {
	    duplicateFiles.clear();
	}

	// Iterate through all tracks and read the track/file info from CMVC.
	Hashtable<String, String> foundFiles = new Hashtable<String, String>();
	Iterator<TkInjectRequest> requests = getInjectRequests().values().iterator();
	while (requests.hasNext()) {
	    TkInjectRequest request = (TkInjectRequest) requests.next();

	    Hashtable<String, TkSource> changedFiles = 
	    (Hashtable<String, TkSource>) request.getSourceFiles();
	    Iterator<String> iter = changedFiles.keySet().iterator();
	    while (iter.hasNext()) {
		String path = (String) iter.next();

		if (! foundFiles.containsKey(path)) {
		    foundFiles.put(path, request.getId());
		}
		else {
		    if (! duplicateFiles.containsKey(path)) {
			String otherRequest = (String) foundFiles.get(path);
			duplicateFiles.put(path, 
			                   request.getId() + ", " + otherRequest);
		    }
		    else {
			String otherRequests = (String) duplicateFiles.get(path);
			duplicateFiles.put(path, 
			                   request.getId() + ", " + otherRequests);

		    }
		}
	    }

	}
    }


    /**
     * Set the input LEVELHIST file.  First try build and if there's no 
     * LEVEHIST file there then try dev.
     */
    private void setBuildLevelHist() {

	// Create the LevelHist object. First look for a LH file in build if
	// one is not present then look for one in dev.
	String lhPath = "/afs/eda/build/" + getComponent() + File.separator + 
	TkInjectUtils.getTkVersion(getRelease());
	buildLevelHist = new LevelHist(lhPath + File.separator +
	                               TkInjectUtils.LEVEL_HIST);

	if (! buildLevelHist.exists()) {
	    lhPath = "/afs/eda/dev/" + getComponent() + File.separator +
	    TkInjectUtils.getTkVersion(getRelease());
	    buildLevelHist = new LevelHist(lhPath + File.separator + 
	                                   TkInjectUtils.LEVEL_HIST);
	}

    }


    /**
     * Set the target LEVELHIST file.
     */
    public void setTargetLevelHist() {
	targetLevelHist = new LevelHist(getTargetDirectory() + File.separator +
	                                TkInjectUtils.LEVEL_HIST);
    }


    /**
     * Set the missing track collection.
     * 
     * @throws IcofException 
     */
    public void setMissingTracks() throws IcofException {

	// Initialize the missing build track list.
	initializeMissingBuildTracks();

	// Create a collection of tracks to search for.
	Vector<String> searchFor = new Vector<String>();
	Iterator<String> iter = getTrack2RequestLookup().keySet().iterator();
	while (iter.hasNext()) {
	    String track = (String) iter.next();
	    searchFor.add(track);
	}


	// Read the LEVELHIST file if it exists.
	if (buildLevelHist.exists()) {
	    buildLevelHist.readFile(false);
	}
	else {
	    return;
	}

	// Iterate backwards through the LH contents searching for tracks.
	boolean allTracksFound = false; 
	for (int i = buildLevelHist.getContents().size() - 1; (i > -1) && ! allTracksFound; i--) {
	    String line = (String) buildLevelHist.getContents().get(i);

	    // Does this line contain one of the searchFor tracks?
	    boolean foundTrack = false;
	    String track = "";
	    for (int j = 0; ((j < searchFor.size()) && !foundTrack); j++) {
		track = (String) searchFor.get(j);
		if (line.indexOf(track) > -1) {
		    foundTrack = true;
		}
	    }

	    // If a track was found remove it from the list.
	    if (foundTrack) {
		searchFor.remove(track);
	    }

	}

	// Any tracks not found add them to the missing build track collection.
	for (int i = 0; i < searchFor.size(); i++) {
	    String track = (String) searchFor.get(i);
	    String request = (String) getTrack2RequestLookup().get(track);
	    String entry = "  * Track " + track + " (" + request + ")\n";
	    getMissingBuildTracks().append(entry);
	}

    }

    /**
     * Clear the CMVC track collection.
     */
    public void clearTrackList() {

	// Remove all the CMVC tracks.
	if (cmvcTracks != null)
	    cmvcTracks.clear();

	// Clean up CMVC data from each inject request.
	Iterator<TkInjectRequest> iter = getInjectRequests().values().iterator();
	while (iter.hasNext()) {
	    TkInjectRequest request = (TkInjectRequest) iter.next();
	    if (request.getSourceFiles() != null)
		request.getSourceFiles().clear();
	    if (request.getMissingTracks() != null)
		request.getMissingTracks().clear();
	}



    }


    /**
     * Copy the specified header files.
     * 
     * @param headerFiles
     * @throws IcofException 
     */
    public void copyHeaderFiles(HashSet<String> headerFileNames) 
    throws IcofException {

	// Copy any header files.
	if (headerFileNames.size() > 0) {
	    logIt("\nCopying header files ...", false);
	}
	else {
	    return;
	}

	// Define the private and include directory.
	String privateDirName = getTargetDirectory() + IcofFile.separator + "private";
	String includeDirName = getTargetDirectory() + IcofFile.separator + "include";

	// Copy the header files.
	Iterator<String> iter = headerFileNames.iterator();
	while (iter.hasNext()) {
	    String headerPath = (String) iter.next();
	    String headerFile = headerPath.substring(headerPath.lastIndexOf("/") + 1,
	                                             headerPath.length());
	    String srcFile = getTargetDirectory() + IcofFile.separator + headerPath;
	    String privateFile = privateDirName + IcofFile.separator + headerFile;
	    String includeFile = includeDirName + IcofFile.separator + headerFile;

	    // Copy to private directory  (if file is already there).
	    IcofFile src = new IcofFile(srcFile, false);
	    IcofFile tgtPrivate = new IcofFile(privateFile, false);
	    if (tgtPrivate.exists()) {
		logIt("Copying " + src.getAbsolutePath() + " to " 
		+ tgtPrivate.getAbsolutePath(), false);
		src.copy(tgtPrivate, true); 
	    }

	    // Copy to the include directory (if file is already there)
	    IcofFile tgtInclude = new IcofFile(includeFile, false);
	    if (tgtInclude.exists()) {
		logIt("Copying " + src.getAbsolutePath() + "\n to " 
		+ tgtInclude.getAbsolutePath(), false);
		src.copy(tgtInclude, true);
	    }

	}

    }


    /**
     * Locate and copy selected header files.
     * 
     * @throws IcofException 
     */
    public void copyHeaderFiles() throws IcofException {

	HashSet<String> headerFileNames = new HashSet<String>();

	// Scan the selected files looking for *.h or *.H files.
	Iterator<TkSource> iter = getSelectedFiles().iterator();
	while (iter.hasNext()) {
	    TkSource sourceFile = (TkSource) iter.next();
	    if (sourceFile.getName().endsWith(".h") ||
	    sourceFile.getName().endsWith(".H")) {
		headerFileNames.add(sourceFile.getName());
	    }
	}

	// Copy any header files.
	copyHeaderFiles(headerFileNames);

    }


    /**
     * @throws IcofException 
     * 
     */
    public void compileMsgcatFiles() throws IcofException {

	logIt("\nCompiling msgcat files ...", false);
	HashSet<String> msgcatFileNames = new HashSet<String>();

	// Scan the selected files looking for *.h or *.H files.
	Iterator<TkSource> iter = getSelectedFiles().iterator();
	while (iter.hasNext()) {
	    TkSource sourceFile = (TkSource) iter.next();
	    if (sourceFile.getName().indexOf(".msgcat") > -1) {
		msgcatFileNames.add(sourceFile.getName());
	    }
	}

	if (msgcatFileNames.size() <= 0) {
	    logIt(" No msgcat files to compile ...", false);
	    return;
	}

	// Compile any msgcat files.
	Iterator<String> iter2 = msgcatFileNames.iterator();
	StringBuffer errorMsg = new StringBuffer();
	Vector<String> results = new Vector<String>();
	while (iter.hasNext()) {
	    String msgcatPath = (String) iter2.next();
	    String command = "/afs/eda/u/einslib/bin/CompMsgcat" + 
	    " -t " + getTargetDirectory() +
	    " -m " + msgcatPath;
	    //+ " -g ";
	    int returnCode = 
	    IcofSystemUtil.execSystemCommand(command, errorMsg, results);

	    logIt(" Command: " + command, false);
	    logIt(" rc: " + returnCode, false);
	    logIt(" results: " + results.toString(), false);
	    logIt(" error msg: " + errorMsg.toString(), false);

	    if (returnCode != 0) {
		throw new IcofException(CLASS_NAME, "compileMsgcatFiles()",
		                        IcofException.SEVERE, 
		                        "Error trying to compile msgcat files. \n"
		                        + errorMsg.toString(), 
		                        "Command: " + command);

	    }

	}

    }


    /**
     * Set the selection* collections based on the specified collection of 
     * TkRequest objects.
     * 
     * @param requests
     */
    public void setSelectedItems(HashSet<TkInjectRequest> requests) {

	// Initialize the selected* collections.
	initializeSelectedItems();

	// Set the selectedRequests collection.
	selectedRequests.addAll(requests);

	// Set the selectedTracks and selectedFiles collections.
	Iterator<TkInjectRequest> iter = requests.iterator();
	while (iter.hasNext()) {
	    TkInjectRequest request = (TkInjectRequest) iter.next();

	    // Add selected tracks.
	    for (int i = 0; i < request.getAllTracks().size(); i ++) {
		String track = (String) request.getAllTracks().get(i);
		if (! selectedTracks.contains(track)) {
		    selectedTracks.add(track);
		}
	    }

	    // Add selected files.
	    Iterator<String> iter2 = request.getSourceFiles().keySet().iterator();
	    while (iter2.hasNext()) {
		String filePath = (String) iter2.next();
		TkSource sourceFile = (TkSource) request.getSourceFiles().get(filePath);
		if (sourceFile.isActive() && (! selectedFiles.contains(filePath))) {
		    selectedFiles.add(sourceFile);
		}
	    }

	}

    }

}

