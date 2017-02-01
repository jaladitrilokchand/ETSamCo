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
 * Class to hold a Component Package deliverable
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 09/17/2013 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Logger;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofSystemUtil;

public class PkgDeliverable extends IcofFile {

   
    /**
     * Constants 
     */
    private static final long serialVersionUID = 1L;
    public enum DelType { REAL, LINK_NUTSHELL, LINK_FOLLOW, LINK_DONT_FOLLOW }
    public enum Action { DELETE, UPDATE, NEW, MANUAL_ADD, UNKNOWN, UNCHANGED }

    /**
     * Constructor
     * 
     * @param sTopLevelDir      Top directory name /afs/eda/LOC/COMP/REL
     * @param aCompleteFileName Full path to deliverables (including top level)
     * @param aDirectoryInd
     */
    public PkgDeliverable(String sTopLevelDir, 
                          String sCompleteFileName, 
                          boolean bDirectoryInd) {

	super(sCompleteFileName, bDirectoryInd);
	loadMetaData();
	setNames(sTopLevelDir, sCompleteFileName);
	setAction(Action.UNKNOWN);
    }


    /**
     * Constructor
     * 
     * @param sTopLevelDir  Top directory name /afs/eda/LOC/COMP/REL
     * @param xFile         File object for deliverable
     */
    public PkgDeliverable(String sTopLevelDir, File xFile) {

	super(xFile.getAbsolutePath(), false);
	setNames(sTopLevelDir, xFile.getAbsolutePath());
	setAction(Action.UNKNOWN);
    }

 

    /**
     * Constructor
     * 
     * @param sTopLevelDir   Top directory name /afs/eda/LOC/COMP/REL
     * @param sDelName       Deliverable name with sub dirs (/dir/del.name)
     * @param aDirectoryInd
     * @param aCheckSum
     * @param aTimestamp
     * @param aSize
     */
    public PkgDeliverable(String sTopLevelDir, String sDelName, 
                          boolean aDirectoryInd,
			  long aCheckSum, long aTimestamp, 
			  long aSize, Action anAction) {

	super(sTopLevelDir + File.separator + sDelName, aDirectoryInd);
	setChecksum(aCheckSum);
	setTimestamp(aTimestamp);
	setSize(aSize);
	setTopLevelDirName(sTopLevelDir);
	setPartialDelName(sDelName);
	setAction(anAction);

    }


    /**
     * Members
     */
    private long checksum;
    private long timestamp;
    private long size;
    private String topLevelDirName;
    private String partialDelName;
    private DelType type;
    private Action action;


    /**
     * Getters
     * @formatter:off
     */
    public long getCheckSum() { return checksum; }
    public long getTimestamp() { return timestamp; }
    public long getSize() { return size; }
    public String getTopLevelDirName() { return topLevelDirName; }
    public String getPartialDelName() { return partialDelName; }
    public DelType getType() { return type; }
    public Action getAction() { return action; }


    /**
     * Setters
     */
    private void setChecksum(long aCheckSum) { checksum = aCheckSum; }
    private void setTimestamp(long aTimestamp) { timestamp = aTimestamp; }
    private void setSize(long aSize) { size = aSize; }
    private void setTopLevelDirName(String aName) { topLevelDirName = aName; }
    private void setPartialDelName(String aName) { partialDelName = aName; }
    public void setAction(Action aType) { action = aType; }
    public void setTimestamp() {
	long tms =  lastModified() / 1000; // Convert milliseconds to seconds
	setTimestamp(tms);
    }
    public void setChecksum() { 
	try {
	    if (getFileName().indexOf("*") == -1)
		setChecksum(getChecksum());
	    else
		// Special cksum processing for files containing * (pwrspice)
		setChecksum(getSpecialChecksum((IcofFile)this, null));
	}
	catch (IcofException e) {
	    setChecksum((long)0);
	}
    }
    public void setSize() { 
	setSize(length());
    }
    // @formatter:on


    /**
     * Display contents of this object
     */
    public String toString() {

	return getAbsolutePath() + ";" + getSize() + ";" + getCheckSum() + ";"
	       + getTimestamp() + ";" + getTypeName() + ";" + getActionName();
    }

    
    /**
     * Return the type as a string
     * 
     * @return Name of deliverable type
     */
    public String getTypeName() {

	if (getType() == null) {
	    return "";
	}
	
	switch (getType()) {
	case REAL:
	    return "REAL";
	case LINK_NUTSHELL:
	    return "LINK_NUTSHELL";
	case LINK_DONT_FOLLOW:
	    return "LINK_DONT_FOLLOW";
	case LINK_FOLLOW:
	    return "LINK_FOLLOW";
	}

	return "";

    }
    
    /**
     * Return the type string as a type enum
     * 
     * @return Deliverable type enum
     */
    public static DelType getTypeEnum(String aType) {
	
	if (aType.toUpperCase().equals("REAL"))
	    return DelType.REAL;
	else if (aType.toUpperCase().equals("LINK_NUTSHELL"))
	    return DelType.LINK_NUTSHELL;
	else if (aType.toUpperCase().equals("LINK_DONT_FOLLOW"))
	    return DelType.LINK_DONT_FOLLOW;
	else if (aType.toUpperCase().equals("LINK_FOLLOW"))
	    return DelType.LINK_FOLLOW;

	return null;

    }

    
    /**
     * Return the action string as an action enum
     * 
     * @return Deliverable action enum
     */
    public static Action getActionEnum(String aType) {
	
	if (aType.toUpperCase().equals("NEW"))
	    return Action.NEW;
	else if (aType.toUpperCase().equals("UPDATE"))
	    return Action.UPDATE;
	else if (aType.toUpperCase().equals("DELETE"))
	    return Action.DELETE;
	else if (aType.toUpperCase().equals("MANUAL_ADD"))
	    return Action.MANUAL_ADD;
	else if (aType.toUpperCase().equals("UNKNOWN"))
	    return Action.UNKNOWN;
	else if (aType.toUpperCase().equals("UNCHANGED"))
	    return Action.UNCHANGED;
	
	return null;

    }

    
    /**
     * Return the action as a string
     * 
     * @return Name of action type
     */
    public String getActionName() {

	if (getAction() == null) {
	    return "";
	}
	
	switch (getAction()) {
	case NEW:
	    return "NEW";
	case UPDATE:
	    return "UPDATE";
	case DELETE:
	    return "DELETE";
	case MANUAL_ADD:
	    return "MANUAL_ADD";
	case UNKNOWN:
	    return "UNKNOWN";
	case UNCHANGED:
	    return "UNCHANGED";
	}

	return "";

    }

    
    /**
     * @formatter:off
     * Examine each deliverable to determine what type it is and what packaging
     * action needs to happen with it Types 
     *  - realFile = del is an actual file 
     *  - linkNutshell = del is a sym link to nutshell
     *  - linkFollow = del is a symlink and it should be kept as a symlink
     *  - linkDontFollow = del is a symlink and it should be converted to 
     *    the target (copy src link to this dir and rename to this del name)
     * 
     * @param xContext Application context
     * @throws IOException
     * @throws IcofException
     * @formatter:on
     */
    public void setType(EdaContext xContext, String sTopLevel,
			PkgControlFile notFollow)
    throws IcofException {

	type = DelType.REAL;
	if (isSymbolic(xContext, this)) {
	    
	    if (! getComponent().equals("nutshell")) {
		String linkTgt = readLink(xContext, this);
		if (linkTgt.indexOf("/nutsh") > -1) {
		    type = DelType.LINK_NUTSHELL;
		}
		else if (notFollow.getContents().contains(getPartialDelName())) {
		    type = DelType.LINK_DONT_FOLLOW;
		}
		else {
		    type = DelType.LINK_FOLLOW;
		}
	    }
	    else if (notFollow.getContents().contains(getPartialDelName())) {
		type = DelType.LINK_DONT_FOLLOW;
	    }
	    else {
		type = DelType.LINK_FOLLOW;
	    }
	    
	}
	
    }



    /**
     * Read a symlink and return what it's pointing to
     * 
     * @param xContext Application context
     * @param aFile File object to test
     * @throws IcofException
     */
    public String readLink(EdaContext xContext, File aFile)
    throws IcofException {

	Vector<String> results = new Vector<String>();
	StringBuffer errorMsg = new StringBuffer();
	String command = "ls -l " + aFile.getAbsolutePath();

	String link = null;
	int rc = IcofSystemUtil.execSystemCommand(command, errorMsg, results);
	if (rc == 0) {
	    String answer = results.firstElement();
	    Vector<String> tokens = new Vector<String>();
	    IcofCollectionsUtil.parseString(answer, "->", tokens, false);
	    link = (String) tokens.get(1);
	    link = link.trim();
	}

	return link;

    }


    /**
     * Determines if file is a symlink
     * 
     * @param xContext Application context
     * @param aFile File object to test
     * @throws IcofException
     */
    public boolean isSymbolic(EdaContext xContext, File aFile)
    throws IcofException {

	Vector<String> results = new Vector<String>();
	StringBuffer errorMsg = new StringBuffer();
	String command = "test -h " + aFile.getAbsolutePath()
			 + " && echo 'symlink' || echo 'not symlink'";

	IcofSystemUtil.execSystemCommand(command, errorMsg, results);
	String answer = results.firstElement();

	if (answer.equals("symlink"))
	    return true;

	return false;

    }


    /**
     * Get the component name from the deliverable path
     * 
     * @return component name or ""
     * @throws IcofException
     */
    private String getComponent()
    throws IcofException {

	Vector<String> tokens = new Vector<String>();
	IcofCollectionsUtil.parseString(getAbsolutePath(), "/", tokens, false);
	if (tokens.size() > 4) {
	    return (String) tokens.get(3);
	}
	return "";
    }


    /**
     * Set the topLevel and delName members
     *
     * @param sTopLevel
     * @param sFullDelPath
     */
    private void setNames(String sTopLevel, String sFullDelPath) {
	
	setTopLevelDirName(sTopLevel);
	setPartialDelName(sFullDelPath.replace(getTopLevelDirName() + File.separator, ""));
	
    }
    
    
    /**
     * Set the size, checksum and timestamp from the actual file
     */
    public void loadMetaData() {
	
	setChecksum();
	setTimestamp();
	setSize();
	
    }
    
    
    /**
     * Determine the cksum for this file.  Used for files that have a * in the
     * name (ie, pwrspice deliverables)
     *
     * @param del File in question
     * @return
     * @throws IcofException 
     */
    private long getSpecialChecksum(IcofFile del, Logger logger) 
    throws IcofException {

	if (logger != null) {
	    logger.info("Running SPECIAL checksum processing .. ");
	    logger.info(" For deliverable: " + del.getName());
	}
	
	String command = "/usr/bin/cksum " + del.getAbsolutePath();
	StringBuffer errors = new StringBuffer();
	Vector<String> results = new Vector<String>();
	if (IcofSystemUtil.execSystemCommand(command, errors, results) != 0) {
	    return 0;
	}
	
	long cksum = 0;
	for (String entry : results) {
	    if (logger != null)
		logger.info(" Entry: " + entry);
	    if (entry.indexOf(del.getName()) > -1) {
		if (logger != null)
		    logger.info("  Matches del name: " + del.getName());
		String[] tokens = entry.split("[ ]");
		return Long.parseLong(tokens[0]);
	    }
	}
	
	return cksum;
	
    }

    
}
