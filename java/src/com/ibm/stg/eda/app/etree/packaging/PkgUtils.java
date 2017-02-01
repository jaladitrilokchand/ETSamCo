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
 * FileFilter object to get only the .ship-* directories
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 09/17/2013 GFS  Initial coding.
 * 02/26/2014 GFS  Updated createMissingDirs() method to be more robust and 
 *                 efficient
 * 04/09/2014 GFS  Added support for internal installs and several 
 *                 common methods
 * 06/23/2014 GFS  Added getPkg* methods
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree.packaging;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import com.ibm.stg.eda.component.tk_etreedb.Platform_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.Location;
import com.ibm.stg.eda.component.tk_etreeobjs.PkgDeliverable;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofSystemUtil;

class PkgUtils {

    /**
     * Constants
     */
    public static final String BASE_PATH = "/afs/eda/";
    public static final String INTERNAL_DIR = 
    "/afs/btv.ibm.com/data/vlsi/eclipz/common/tools/edatools";
    public static final String EXTERNAL_DIR = "/afs/SITE.ibm.com/data/edatools/ptn";
    public static final String TK_PREVIEW = "toolkit_preview";
    public static final List<String> PLATFORMS = Arrays.asList("64-amd64_linux26_RH5",
                                                               "64-amd64_linux26_RH6",
                                                               "64-rs_aix61",
                                                               "64-rs_aix71");
    public static final List<String> PLATS = Arrays.asList("lin64b_x86",
    														"lin64b_x86",
    														"aix64b", 
    														"aix64b");
    // navechan: For 15.1, Use these list to create install links
    public static final List<String> PLATFORMS_151 = Arrays.asList("64-amd64_linux26_RH6",   
            													   "64-ppc64le_linux26_RH7");
public static final List<String> PLATS_151 = Arrays.asList("lin64b_x86",
														   "lin64b_ppc");

    public static final String MAR_INC_EXT = ".increment";
    public static final String MAR_PREVIOUS_VERSION = "Version.prev";
    public static final String MAR_CURRENT_VERSION = "Version";
    public static final String PKGS_DIR = "/afs/eda/edadist/packages/";

   

    /**
     * Finds all files
     * 
     * @param dir         Starting directory
     * @param stopOnError Throw an error if a directory doesn't exist
     * @return Collection(HashSet) of files and/or directories
     * @throws IcofException
     */
    public static Collection<File> listFileTree(File dir, boolean stopOnError)
    throws IcofException {

	return listFileTree(dir, stopOnError, false);

    }


    /**
     * Finds all files/directories
     * 
     * @param dir         Starting directory
     * @param stopOnError Throw an error if a directory doesn't exist
     * @param includeDirs If true include directory names otherwise only 
     *                    include file names
     * @return Collection(HashSet) of files and/or directories
     * @throws IcofException
     */
    public static Collection<File> listFileTree(File dir, boolean stopOnError,
                                                boolean includeDirs)
    throws IcofException {

	HashSet<File> fileTree = new HashSet<File>();

	if (! dir.exists()) {
	    fileTree.add(dir);

	    if (stopOnError) {
		throw new IcofException("PackagingUtils", "ListFileTree()",
					IcofException.SEVERE,
					"Unable to read directory or file",
					dir.getAbsolutePath());
	    }
	}
	else {
	    for (File entry : dir.listFiles()) {
		if (entry.isFile()) {
		    fileTree.add(entry);
		}
		else {
		    if (includeDirs)
			fileTree.add(entry);
		    fileTree.addAll(listFileTree(entry, stopOnError, 
		                                 includeDirs));
		}
	    }
	}

	return fileTree;

    }

    
    /**
     * Determine if a file should be exclude as a deliverable or not
     * 
     * @param xDel The file to be tested
     * @return True if file should be excluded otherwise false
     */
    public static boolean excludeDeliverable(ToolKit aTk, Component aComp,
                                             PkgDeliverable xDel) {

	// Dot files in dll-64 and bin-64
	if (xDel.getPartialDelName().startsWith("dll-64/.")  ||
	    xDel.getPartialDelName().startsWith("bin-64/."))

	    return true;


	// Handle 15.1 bin-64, dll-64 and lib-64 deliverables
	if (aTk.getToolKit().getRelease().getName().equals("15.1") && 
	    (xDel.getPartialDelName().startsWith("bin-64") ||
	     xDel.getPartialDelName().startsWith("dll-64") ||
	     xDel.getPartialDelName().startsWith("lib-64")))
	    return true;
 
	
	// Files
	String fileName = xDel.getName();
	if (fileName.endsWith("-O.dll") || 
	    fileName.endsWith("-O.exp") ||
	    fileName.endsWith("-O") || 
	    fileName.equals("prev.ship.list") ||
	    fileName.equals("prior.ship.list") || 
	    fileName.endsWith("~") ||
	    fileName.startsWith("~") || 
	    fileName.endsWith("imp") ||
	    fileName.endsWith("loadmap") || 
	    fileName.endsWith("-g") ||
	    fileName.endsWith("-pg") || 
	    (fileName.indexOf("makefile") > -1) ||
	    (fileName.indexOf("Makefile") > -1) || 
	    fileName.endsWith(".pure") ||
	    fileName.endsWith("-pure") || 
	    fileName.endsWith("_pure") ||
	    fileName.endsWith("-quaint") ||
	    fileName.endsWith("_auto_loads.tcl")) 

	    return true;

	
	return false;

    }


    /**
     * Converts a fully qualified deliverable name into its partial name
     * (removes the leading /afs/eda/LOC/COMP/REL/.ship-PLAT/)
     * 
     * @param topLevelDir Top level source directory (/afs/eda/LOC/COMP/REL)
     * @param file        Deliverable name
     * @return converted deliverable name
     */
    public static String getPartialDel(File topLevelDir, File file) {

	return getPartialDel(topLevelDir.getAbsolutePath(), file);

    }
    
    
    /**
     * Converts a fully qualified deliverable name into its partial name
     * (removes the leading /afs/eda/LOC/COMP/REL/.ship-PLAT/)
     * 
     * @param topLevelDir Top level source directory (/afs/eda/LOC/COMP/REL)
     * @param file        Deliverable name
     * @return converted deliverable name
     */
    public static String getPartialDel(String topLevelDirName, File file) {

	return file.getAbsolutePath().replace(topLevelDirName, "");

    }


    /**
     * Constructs the top level directory name (removes the leading
     * /afs/eda/LOC/COMP/REL) Adds .ship-platform if platform is not null
     * 
     * @param file Deliverable to convert
     * @param platform Platform name
     * @return converted deliverable name
     */
    public static String getTopLevelShipDir(ToolKit tk, Component comp,
                                            Location loc, 
                                            Platform_Db platform) {

	String dirName = BASE_PATH + loc.getName().toLowerCase()
			 + File.separator + comp.getName() + File.separator
			 + tk.getToolKit().getRelease().getName();

	if (platform != null) {
	    dirName += File.separator + ".ship-" + platform.getShippingName();	}

	return dirName;

    }

    
    /**
     * Create any missing sub directories in the newFullPath up to the topDir
     * 
     * @param topDir      Top level directory .. must exist
     * @param partialPath New partial path which when added to topDir creates
     *                    the full path to a new file
     * @param bIncludeLast If true treat partialPath as a directory and create all
     *                     subdirs otherwise treat partialPath as a file and don't
     *                     create the last subdir because it is really a file
     * @throws IcofException
     * @return parent directory
     */
    public static IcofFile createMissingDirs(IcofFile topDir, String partialPath, 
                                             boolean includeLast) 
    throws IcofException {

	IcofFile dir = new IcofFile(topDir.getAbsolutePath() + File.separator + 
	                            partialPath, true);

	if (! includeLast) {
	    String parentDirName = dir.getParent();
	    dir = new IcofFile(parentDirName, true);
	}	    
	    
	if (! dir.exists())
	    dir.mkdirs();

	return dir;
	
    }

    
    /**
     * Recursively removes everything under this directory
     *
     *
     * @param dir
     * @return
     * @throws IcofException
     */
    public static boolean removeAll(IcofFile dir) throws IcofException {
	
	// Bail if the directory does not exist
	if (! dir.exists()) {
	    throw new IcofException("PkgUtils", "removeAll()", 
	                            IcofException.SEVERE, "Dir does not exist",
	                            dir.getAbsolutePath());
	}	
	
	// Remove the dir and all sub dirs
	String command = new String("/usr/bin/rm -fr " + dir.getAbsolutePath());
	StringBuffer errorMsg = new StringBuffer();
	Vector<String> results = new Vector<String>();
	System.out.println("Running - " + command);
	int rc = IcofSystemUtil.execSystemCommand(command, errorMsg, results);
	if (rc == 0) {
	    return true;
	}
	
	for (String line : results) {
	    System.out.println(" --> " + line);
	}
	
	return false;

    }
    
    
    /**
     * Determine the existing the mar2* incremental installs
     *
     * @param aDir     Directory to interrogate
     * @param compName Component name
     * @return Collection of increment dir names
     */
    public static Vector<String> readIncrements(File aDir, 
                                                String compName,
                                                boolean useParent) {

	System.out.println("READING incremental installs for " + 
	                   aDir.getAbsolutePath());
	
	String[] contents;
	if (useParent) 
	    contents = aDir.getParentFile().list();
	else 
	    contents = aDir.list();
	Vector<String> increments = new Vector<String>();
	if (contents != null) {
	    for (String dir : contents) {
		if (dir.startsWith(compName + ".1")) {
		    increments.add(dir);
		}
	    }
	}
	    
	return increments;
	
    }

    /**
     * Determine the existing the mar2* incremental installs
     *
     * @param aDir     Directory to interrogate
     * @param compName Component name
     * @return Collection of increment dir names
     */
    public static Vector<String> readIncrements(String aDirName, 
                                                String compName) {
	
	File dir = new File(aDirName);

	return readIncrements(dir, compName, false);
	
    }

    
    
    /**
     * Run the command in the specified directory
     * 
     * @param xContext Application context
     * @param command  Command string in an array
     * @param IcofFile Directory to run command
     * @param bDryRun  If true don't actually run the command
     * @param reply    Returns any comments/responses
     * @return         Return code from command
     * @throws IcofException
     */
    public static int runCommandInDir(String[] command, IcofFile aDir, 
                                      boolean bDryRun, Logger logger)
    throws IcofException {

	int rc = 0;
	String cmdText = "";
	for (String s : command) {
	    cmdText += s + " ";
	}

	if (logger != null)
	    logger.info(" Running '" + cmdText + "' in " + 
	                aDir.getAbsolutePath() + "\n");

	StringBuffer errors = new StringBuffer();
	Vector<String> results = new Vector<String>();
	if (bDryRun) {
	    if (logger != null)
		logger.info("DRYRUN - files not really copied\n");
	}
	else {
	    rc = IcofSystemUtil.execSystemCommand(command, null, aDir, 
	                                          errors, results);

	    if (rc != 0) {
		String resultText = "";
		for (Object line : results) {
		    resultText += (String) line;
		}
		throw new IcofException("PkgUtils",
		                        "runCommandInDir()", IcofException.SEVERE,
		                        "Error running " + cmdText + " in "
		                        + aDir.getAbsolutePath(),
		                        "Results: " + resultText + "\n"
		                        + "Errors: " + errors.toString() + "\n");
	    }
	}

	return rc;

    }


    /**
     * Run the given command in the specified directory
     *
     * @param xContext Application context
     * @param command  Command to run
     * @param aDir     Dir to run command in
     * @param bDryRun  If true don't actually run the command
     * @param reply    Returns any comments/responses
     * @return         Return code from command
     * @throws IcofException 
     */
    public static int runCommandInDir(String command, IcofFile aDir,
                                      boolean bDryRun, Logger logger) 
                                      throws IcofException {

	String[] cmdList = command.split("[ ]+");

	return runCommandInDir(cmdList, aDir, bDryRun, logger);

    }
   
    
    /**
     * Construct the packages directory
     * 
     * @param aTk
     */
    public static IcofFile getPkgPackagesDir(ToolKit aTk) {
	String dirName =  PKGS_DIR + "tk" + aTk.getName() + File.separator + 
	                 "packages";
	IcofFile dir = new IcofFile(dirName, true);
	
	return dir;
    }

   
    /**
     * Construct the packages directory
     * 
     * @param aTk
     */
    public static IcofFile getPkgWorkDir(ToolKit aTk) {
	String dirName =  PKGS_DIR + "tk" + aTk.getName() + File.separator + 
                          "work";
	IcofFile dir = new IcofFile(dirName, true);

	return dir;
    }

    
    /**
     * Determine the cksum for this file.  Used for files that have a * in the
     * name (ie, pwrspice deliverables)
     *
     * @param del File in question
     * @return
     * @throws IcofException 
     */
    public static long getSpecialChecksum(IcofFile del, Logger logger) 
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

