/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *    DATE: 12/29/2009
 *
 *-PURPOSE---------------------------------------------------------------------
 * Class for EDA CMVC data.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 12/29/2009 GFS  Initial coding.
 * 01/14/2010 GFS  Added getValidReleaseName() and extractFile() methods.
 * 02/01/2010 GFS  Added release as a data member.
 * 02/17/2010 GFS  Added support for naming a shipb release. Added CMVC track 
 *                 extraction methods.
 * 03/02/2010 GFS  Update pd_level_extract command.  
 * 04/09/2012 GFS  Updated to support xtinct injects.              
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_levelhist;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_patch.TkInjectUtils;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofSystemUtil;

public class CMVC implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 62657147384917384L;


	/**
	 * Constructor
	 * 
	 * @param toolName     Tool name from TK Patch (ie, component)
	 * @param patchRelease Tool Kit release (ie, 17.1.9)
	 */
	public CMVC(String toolName, String patchRelease) {
		super();
		setActive();
		setReleases(toolName, patchRelease);
	}

	/**
	 * Constructor
	 * 
	 * @param cmvcRelease  CMVC release name
	 */
	public CMVC(String cmvcRelease) {
		super();
		setActive();
		setRelease(cmvcRelease);
	}



	/**
	 * Empty constructor
	 */
	public CMVC() {
		super();
		setActive();
	}


	/*
	 * Constants.
	 */
	private static String CLASS_NAME = "CMVC";


	/*
	 * Getters
	 */
	public boolean isActive() { return isActive; }
	public boolean hasShipbRelease() { return hasShipbRelease; }
	public int getReturnCode() { return returnCode; }
	public StringBuffer getErrorMsg() { return errorMsg; }
	public Vector<String> getResults() { return results; }
	public String getRelease() { return release; }
	public String getShipbRelease() { return shipbRelease; }
	public String getBuildRelease() { return buildRelease; }
	public String getResultsAsString() {
		StringBuffer myResults = new StringBuffer();
		for (int i = 0; i < getResults().size(); i++) {
			myResults.append((String) getResults().get(i) + "\n"); 
		}
		return myResults.toString();
	}

	/*
	 * Members
	 */
	private boolean isActive = false;
	private boolean hasShipbRelease = false;
	private int returnCode;
	private StringBuffer errorMsg;
	private Vector<String> results;
	private String release;
	private String shipbRelease;
	private String buildRelease;


	/*
	 * Command strings.
	 */
	public String getIsActiveCommand() {
		return "/afs/eda/common/contrib/bin/where Report";
	}
	public String getReleaseNameCommand(String release) {
		return "Report -g ReleaseView -w \"name='" + release + "'\" -se NAME";
	}
	public String getDefectInfoCommand(String track) {
		return "Report -raw -view DefectView -where \"Name='" + track + "'\"";
	}
	public String getFeatureInfoCommand(String track) {
		return "Report -gen FeatureView -where \"Name='" + track + "'\"";
	}
	public String getTrackInfoCommand(String release, String track) {
		return "Report -raw -view ChangeView -where \"DefectName='" + 
		track + "' and releaseName='" + release + "'\"";
	}
	public String getExtractFileCommand(String release, String file, 
	                                    String sid, String targetDir) {
		return "File -extract " + file + " -release " + release + 
		" -relative " + targetDir + " -version " + sid + 
		" -fmask 644 -verbose";
	}
	public String getFilePathCommand(String release, String file) {
		return "File -res " + file + " -release " + release + " -q";
	}

	public String getShipbReleaseCommand(String rel1, String rel2, String rel3) {
		return "Report -g ReleaseView -w \"name in ('" + rel1 + "', '" 
		+ rel2 + "', '" + rel3 + "')\" -se NAME";
	}

	public String getLevelExtractCommand(String release,  
	                                     String extractDirectory, 
	                                     String trackList) {
		return "/afs/eda/u/mannk/tools/golden/pd_level_extract.NEW" 
		+ " -r " + release 
		+ " -e " + extractDirectory
		+ " -t \"" + trackList + "\"";
	}


	/*
	 * Setters
	 */
	public void setRelease(String aRelease) { release = aRelease; }
	public void setShipbRelease(String aRelease) { shipbRelease = aRelease; }
	public void setBuildRelease(String aRelease) { buildRelease = aRelease; }
	public void setReleases(String toolName, String patchRelease) {
		buildRelease = getValidBuildReleaseName(toolName, patchRelease);
		shipbRelease = getValidShipbReleaseName(toolName, patchRelease);
		setRelease(buildRelease);
	}

	private void setActive() {

		// Run the is active command.
		runCommand(getIsActiveCommand());

		// If anything was returned then CMVC was found.
		if (getResults().size() > 0) {
			isActive = true;
		}
		else {
			isActive = false;
		}

	}


	/*
	 * Initialize the command variables.
	 */
	private void initialize() {

		// Reset the results.
		if (results == null) {
			results = new Vector<String>();
		}
		else {
			results.clear();
		}

		// Reset the error buffer.
		if (errorMsg == null) {
			errorMsg = new StringBuffer();
		}
		else {
			errorMsg.setLength(0); 
		}

		// Reset the return code.
		returnCode = -1;

	}


	/*
	 * Run the command
	 * 
	 * @param command  Command string to execute.
	 * @return If IcofException was encountered then return the message
	 *         otherwise return null.
	 */
	public String runCommand(String command) {

		String returnString = null;

		// Initialize the command variables
		initialize();

		// Run the command.
		try {
			returnCode = IcofSystemUtil.execSystemCommand(command, errorMsg, results);
		}
		catch (IcofException ex) {
			returnString = ex.getMessage();
		}

		return returnString;

	}


	/**
	 * Extract a file from CMVC.
	 * 
	 * @param fileName  Name of file to extract.
	 * @param sid       SID of file to extract.
	 * @param targetDir Target directory
	 * @throws IcofException 
	 */
	public void extractFile(String fileName, String sid, String targetDir) 
	throws IcofException {

		// Extract the specified file.
		String command = getExtractFileCommand(getRelease(), fileName, sid, targetDir);
		runCommand(command);
		if ((results != null) && results.size() > 0) {
			return;
		}

		// Must have encountered an error.
		throw new IcofException(CLASS_NAME, "extractFile", IcofException.SEVERE,
		                        "Unable to extract specified file: " + 
		                        errorMsg.toString(), 
		                        fileName);

	}


	/*
	 * Reads track data from CMVC.
	 * 
	 * @param track   CMVC track name.
	 * @return        CMVC_Track object loaded with track data 
	 *                or null if not found in CMVC.
	 * @throws IcofException 
	 */
	public CMVC_Track readTrack(String track) 
	throws IcofException {

		// Read the severity and abstract from CMVC.
		String severity = "";
		String abstractText = "";

		// Check for a defect.
		Vector<String> tokens = new Vector<String>();
		String command = getDefectInfoCommand(track);
		runCommand(command);
		if ((results != null) && results.size() > 0) {
			IcofCollectionsUtil.parseString((String)results.firstElement(), 
			                                "|", tokens, true);
			if (tokens.size() >= 8) {
				severity += (String) tokens.get(7);
				abstractText += (String) tokens.get(8);
			}
		}

		// Check for feature if defect severity not set.
		if (severity.equals("")) {
			command = getFeatureInfoCommand(track);
			runCommand(command);
			if ((results != null) && results.size() > 0) {
				tokens.clear();
				IcofCollectionsUtil.parseString((String)results.firstElement(), 
				                                "|", tokens, true);
				severity = "f";
				if (tokens.size() >= 9) {
					abstractText = (String) tokens.get(8);
				}
			}
		}

		// Read changed files for this track.
		readChangedFiles(track);

		Hashtable<String, CMVC_File> changedFiles = 
			new Hashtable<String, CMVC_File>();
		tokens.clear();
		if ((results != null) && (results.size() > 0)) {
			String sid = "";
			String path = "";
			String type = "";
			String developer = "";

			for (int i = 0; i < results.size(); i++) {
				String entry = (String) getResults().get(i);
				//System.out.println("CMVC entry = " + entry);

				tokens.clear();
				IcofCollectionsUtil.parseString(entry, "|", tokens, true);
				if (tokens.size() >= 4)
					sid = (String) tokens.get(3);
				if (tokens.size() >= 5)
					path = (String) tokens.get(4);
				if (tokens.size() >= 6)
					type = (String) tokens.get(5);
				if (tokens.size() >= 11)
					developer = (String) tokens.get(10);
				CMVC_File myFile = new CMVC_File("", path, sid, type, developer, null);
				changedFiles.put(path, myFile);

			}
		} 

		// Create the CMVC_Track object;
		CMVC_Track myTrack = new CMVC_Track("", track, abstractText, severity,
		                                    "", "", changedFiles);

		return myTrack;

	}

	/**
	 * Reads changed files for this track into the results collection.
	 * 
	 * @param track   CMVC track name.
	 * @return        True if track data found otherwise false.
	 */
	private void readChangedFiles(String track) {

		// Query CMVC for changed files.
		String command = getTrackInfoCommand(getRelease(), track);
		runCommand(command);

	}


	/**
	 * Get the valid build CMVC release name.
	 * 
	 * @param component CMVC component name.
	 * @param version   Tool Kit version (17.1.8, 17.1.9 ...)
	 * @return Valid CMVC release name or "" if not found.
	 */
	public String getValidBuildReleaseName(String component, String version) {

		// Construct the "comp-version" name first. (like pds-13.1)
		String releaseName = component + "-" + 
		                     TkInjectUtils.getCmvcVersion(version, ".");

		// Query CMVC for this release name.
		String command = getReleaseNameCommand(releaseName);
		runCommand(command);

		if ((results != null) && results.size() > 0) {
			return releaseName;
		}

		// Construct the "comp.version" name first. (like einstimer.1301)
		releaseName = component + "." + TkInjectUtils.getCmvcVersion(version, "0");

		// Query CMVC for this release name.
		command = getReleaseNameCommand(releaseName);
		runCommand(command);

		if ((results != null) && results.size() > 0) {
			return releaseName;
		}
		else {
			releaseName = "";
		}

		return releaseName;

	}


	/**
	 * Set the shipb CMVC release name.
	 * 
	 * @param component CMVC component name.
	 * @param version   Tool Kit version (17.1.8, 17.1.9 ...)
	 * @return Valid CMVC release name or "" if not found.
	 */
	public String getValidShipbReleaseName(String component, String version) {

		// Get the CMVC release name for this component/version.
		String release = getValidBuildReleaseName(component, version);

		// The shipb release can have one of 3 flavors. 
		//  <RELEASE>-shipb
		//  <RELEASE.shipb
		//  <RELEASE>s
		String rel1 = release + "-shipb";
		String rel2 = release + ".shipb";
		String rel3 = release + "s";

		// Query CMVC for these release names.
		String command = getShipbReleaseCommand(rel1, rel2, rel3);
		runCommand(command);

		String shipbReleaseName = "";
		if ((results != null) && results.size() > 0) {
			shipbReleaseName = (String) results.firstElement();
			hasShipbRelease = true;
		}
		else {
			shipbReleaseName = "";
			hasShipbRelease = false;
		}

		return shipbReleaseName.trim();

	}


}
