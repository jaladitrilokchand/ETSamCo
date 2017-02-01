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
 * Object representing the packaging control files 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 09/12/2013 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.io.File;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;

public class PkgControlFile extends IcofFile {

    /**
     * Constants.
     */
    private static final long serialVersionUID = -6659734532446691142L;
    public static final String FILE_customDeliver = ".custom_deliver";
    public static final String FILE_dontShip = ".dont_ship";
    public static final String FILE_dontFollow = ".symlink_dont_follow";
    public static final String FILE_prevShipList = "prev.ship.list";
    public static final String FILE_addon = ".addon";
    public static final String FILE_specialPath = ".special_path";


    /**
     * Returns the full path to a control file
     * 
     * @param topLevel Top level directory (ie, /afs/eda/LOC/COMP/REL)
     * @param aName    Control file name
     */
    public static String getFullFileName(File topLevelDir, String aName) {

	return topLevelDir.getAbsolutePath() + File.separator + aName;
	
    }


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aStageName Stage name to add
     * @throws IcofException
     */
    public PkgControlFile(EdaContext xContext, String fileName)
    throws IcofException {

	super(fileName, false);
	
    }


    /**
     * Read the file contents and strip out comment lines
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    public StringBuffer readContents(EdaContext xContext)
    throws IcofException {

	StringBuffer log = new StringBuffer();
	
	if (! exists()) {
	    log.append(" WARNING: file not found ... " + getAbsolutePath() + "\n");
	    return log;
	}

	// Read the file
	openRead();
	read();
	closeRead();

	// Remove comment lines
	removeComments();

	// Display the contents again
	log.append(" Contents of " + getFileName() + "\n");
	log.append(" ------------------------\n");
	for (Object line : getContents()) {
	    log.append(" " + line + "\n");
	}

	return log;

    }


    /**
     * Parse a line of the file
     * 
     * @param line A line of the control file
     */
    public String[] parseLine(String line) {

	return line.split("[;]+");

    }


    /**
     * Remove comments (lines starting with #) from the contents
     * 
     * @param xContext Application context
     * @throws IcofException Trouble determining deliverables
     */
    private void removeComments() {

	Vector<String> myContents = new Vector<String>();
	
	for (Object entry : getContents()) {
	    String line = (String)entry;
	    if (! line.isEmpty() && ! line.startsWith("#") ) {
		myContents.add(line);
	    }
	}

	setContents(myContents);

    }
    

}
