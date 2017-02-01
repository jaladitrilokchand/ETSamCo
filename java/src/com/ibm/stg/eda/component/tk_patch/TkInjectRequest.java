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
 * Class for EDA Tool Kit Injection Request data
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 11/10/2009 GFS  Initial coding.
 * 01/10/2010 GFS  Updated setSourceLocation() to truncate the source location
 *                 string when it sees a "\n" or a " ". Reworked the save logic 
 *                 in update*() methods.
 * 03/09/2010 GFS  Updated setLocationGuess() to detect if the file updated 
 *                 field is empty and set the location guess to "".
 * 03/30/2010 GFS  Updated setLocationGuess() to use Java Regex to improve
 *                 the location guess results.
 * 04/19/2010 GFS  Update setLocationGuess() to ensure the guess is a directory.
 * 01/17/2012 GFS  Updated setAllTracks() to work correctly if no tracks found               
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_patch;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.ibm.stg.eda.component.tk_levelhist.CMVC_File;
import com.ibm.stg.eda.component.tk_levelhist.CMVC_Track;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;


public class TkInjectRequest implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5012771023784436621L;


    /**
     * Constructor
     * 
     * @param cqId
     * @param trackPrimary
     * @param tracksSecondary
     * @param filesUpdated
     * @param developer
     * @throws IcofException
     */
    public TkInjectRequest(String cqId, String trackPrimary,
			   String tracksSecondary, String filesUpdated,
			   String developer) throws IcofException {

	setId(cqId);
	setTrackPrimary(trackPrimary);
	setTracksSecondary(tracksSecondary);
	setAllTracks();
	setFilesUpdated(filesUpdated);
	setDeveloper(developer);
	setLocationGuess();

    }


    /**
     * @throws IcofException
     * 
     */
    private void setAllTracks()
    throws IcofException {

	allTracks = new Vector<String>();
	if (getAllTracksAsString() != null) {
	    IcofCollectionsUtil.parseString(getAllTracksAsString(), " ",
					    allTracks, true);
	}
    }


    /*
     * Set the source file hash table.
     * 
     * @param trackList Collection of CMVC_Track objects
     */
    public void setSourceFiles(Hashtable<String, CMVC_Track> trackList)
    throws IcofException {

	// Initialize the collection of source files.
	if (sourceFiles == null) {
	    sourceFiles = new Hashtable<String, TkSource>();
	}

	// Create the collection of missing tracks if not already created.
	if (missingTracks == null) {
	    missingTracks = new Vector<String>();
	}

	// Find the source files for each track for this request.
	Iterator<String> tracks = getAllTracks().iterator();
	while (tracks.hasNext()) {
	    String track = (String) tracks.next();

	    boolean status = setTracksSourceFiles(track, trackList);
	    if (status == false) {
		missingTracks.add(track);
	    }
	}

    }


    /*
     * Set the source file hash table.
     * 
     * @param track CMVC track name/number
     * 
     * @param trackList Collection of CMVC_Track objects
     * 
     * @return True if track found otherwise false.
     */
    public boolean setTracksSourceFiles(String track,
					Hashtable<String, CMVC_Track> trackList)
    throws IcofException {

	// Locate this track in the collection of tracks from CMVC.
	boolean foundTrack = false;
	CMVC_Track trackObj = null;
	if (trackList.containsKey(track)) {
	    foundTrack = true;
	    trackObj = trackList.get(track);
	}

	// If this track found then find the source files.
	if (foundTrack) {

	    // Iterate through the collection of CMVC_File objects.
	    Iterator<CMVC_File> iter = trackObj.getFiles().values().iterator();
	    while (iter.hasNext()) {
		CMVC_File file = iter.next();
		TkSource source = new TkSource(file.getPath(), "", true);
		sourceFiles.put(file.getPath(), source);
	    }
	}

	return foundTrack;

    }


    /*
     * Update the source file location of the specified source file
     */
    public void updateSourceLocation(String file, String location)
    throws IcofException {

	// Locate this TkSource object.
	TkSource source = (TkSource) getSourceFiles().get(file);

	// Update the source file location.
	if (source != null) {
	    String fullPath = TkInjectUtils.getFullLocationPath(file, location);

	    source.setIsPathValid(true);
	    if (fullPath == null) {
		fullPath = location;
		source.setIsPathValid(false);
	    }
	    source.setFullPath(fullPath);
	    getSourceFiles().put(file, source);
	}

    }


    /*
     * Initialize the source file location from the files updated comments.
     */
    private void setLocationGuess() {

	// Return if the files updated is null. This scenario could happen if
	// this field on the inject request is blank.
	if (getFilesUpdated() == null) {
	    sourceLocationGuess = "";
	    return;
	}

	// Search for a string that contain /afs or ~.
	Matcher matcher1 = afsPatt1.matcher(getFilesUpdated());
	Matcher matcher2 = tildePatt1.matcher(getFilesUpdated());

	if (matcher1.find()) {
	    sourceLocationGuess = matcher1.group(0);
	}
	else if (matcher2.find()) {
	    sourceLocationGuess = matcher2.group(0);
	}
	else {
	    sourceLocationGuess = "";
	}

	// Remove a trailing )
	int index = sourceLocationGuess.indexOf(")");
	if (index > -1) {
	    sourceLocationGuess = sourceLocationGuess.substring(0, index);
	}

	// Remove everything after a space
	index = sourceLocationGuess.indexOf(" ");
	if (index > -1) {
	    sourceLocationGuess = sourceLocationGuess.substring(0, index);
	}

	// Ensure this path points to a directory not a file.
	index = sourceLocationGuess.lastIndexOf("/");
	if (index > -1) {

	    // If the guess is already a directory then return.
	    IcofFile test1 = new IcofFile(sourceLocationGuess, false);
	    if (test1.isDirectory()) {
		return;
	    }

	    // Remove any trailing stuff and test if it's a directory.
	    IcofFile test2 = new IcofFile(sourceLocationGuess.substring(1,
									index),
					  false);
	    if (test2.isDirectory()) {
		sourceLocationGuess = sourceLocationGuess.substring(0, index);
		return;
	    }

	}

    }


    /*
     * Constants
     */
    protected Pattern afsPatt1 = Pattern.compile("/afs/.*");
    protected Pattern tildePatt1 = Pattern.compile("~.*");


    /*
     * Getters
     */
    public String getId() {

	return cqId;
    }


    public String getAllTracksAsString() {

	String tracks = getTrackPrimary();
	if (!getTracksSecondary().equals("")) {
	    tracks += " " + getTracksSecondary();
	}
	return tracks;
    }


    public String getTrackPrimary() {

	return trackPrimary;
    }


    public String getTracksSecondary() {

	return tracksSecondary;
    }


    public String getFilesUpdated() {

	return filesUpdated;
    }


    public String getDeveloper() {

	return developer;
    }


    public Hashtable<String, TkSource> getSourceFiles() {

	return sourceFiles;
    }


    public Vector<String> getAllTracks() {

	return allTracks;
    }


    public Vector<String> getMissingTracks() {

	return missingTracks;
    }


    public String getLocationGuess() {

	return sourceLocationGuess;
    }


    /*
     * Setters
     */
    private void setId(String id) {

	this.cqId = id;
    }


    private void setTrackPrimary(String track) {

	trackPrimary = track;
    }


    private void setTracksSecondary(String tracks) {

	if ((tracks != null) && (!tracks.equals(""))) {
	    tracks = tracks.replaceAll("\n", " ");
	}
	else {
	    tracks = "";
	}
	tracksSecondary = tracks;
    }


    private void setFilesUpdated(String filesUpdated) {

	this.filesUpdated = filesUpdated;
    }


    private void setDeveloper(String developer) {

	this.developer = developer;
    }


    public void setLocationGuess(String location) {

	sourceLocationGuess = location;
    }


    /*
     * Members
     */
    private String cqId;
    private String trackPrimary;
    private String tracksSecondary;
    private String filesUpdated;
    private String developer;
    private Hashtable<String, TkSource> sourceFiles;
    private Vector<String> allTracks;
    private Vector<String> missingTracks;
    private String sourceLocationGuess;

}
