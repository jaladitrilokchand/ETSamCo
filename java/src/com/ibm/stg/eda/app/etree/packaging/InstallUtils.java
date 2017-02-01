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
 * Install utilities
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 04/15/2014 GFS  Initial coding
 * 04/29/2014 GFS  Added support for ICC
 * 07/28/2014 GFS  Updated to support site specific server installs
 * 05/15/2015 GFS  Changed /afs/btv/data/gnacfg to be /afs/apd/data/gnacfg
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree.packaging;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Logger;
import com.ibm.stg.eda.app.etree.PlatformShow;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.Component_Db;
import com.ibm.stg.eda.component.tk_etreedb.Platform_Db;
import com.ibm.stg.eda.component.tk_etreedb.StageName_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKitPackage;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;

class InstallUtils {

    /**
     * Constants
     */
    public static final String BASE_PATH = "/afs/eda/";
    public static final String SERVER_BTV_DIR = 
    "/afs/btv.ibm.com/data/vlsi/eclipz/common/tools/edatools";
    public static final String SERVER_EDA_DIR = 
    "/afs/eda/project/eclipz/common/tools/edatools";
    public static final String ASICS_DIR = "/afs/SITE.ibm.com/data/edatools/ptn";
    public static final String ICC_DIR = "/afs/eda.fishkill.ibm.com/tkbld/tkinstall/ptn";
    public static final String TK_PREVIEW = "toolkit_preview";
    public static final String HISTORY_DIR = "history";
  
    
    /**
     * Determine the existing the mar2* incremental installs
     *
     * @param aDir     Directory to interrogate
     * @param compName Component name
     * @return Collection of increment dir names
     */
    public static Vector<String> readIncrements(IcofFile aDir, 
                                                String compName) {
	
	String[] contents = aDir.getParentFile().list();
	
	Vector<String> increments = new Vector<String>();
	for (String dir : contents) {
	    if (dir.startsWith(compName + ".1")) {
		increments.add(dir);
	    }
	}
	
	return increments;
	
    }

    
    /**
     * Create a collection of component names from the BOM file.  The BOM
     * contains the latest collection of components so need to filter that
     * by what components are associated with the given TK
     *
     * @param xContext
     * @param aTk ToolKit object
     * @param aComp Component object
     * @throws IcofException 
     */
    public static ArrayList<String> setDeliveredComps(EdaContext xContext, 
                                                      ToolKit aTk,
                                                      String aPlatName,
                                                      Component  aComp,
                                                      boolean bExternal) 
                                                      throws IcofException {
	
	boolean bIsLinux = false;
	if (aPlatName.contains("lin"))
	    bIsLinux = true;
	
	return setDeliveredComps(xContext, aTk, bIsLinux, aComp, bExternal);
	
    }


    /**
     * Create a collection of component names from the BOM file.  The BOM
     * contains the latest collection of components so need to filter that
     * by what components are associated with the given TK
     *
     * @param xContext
     * @param aTk ToolKit object
     * @param aComp Component object
     * @throws IcofException 
     */
    public static ArrayList<String> setDeliveredComps(EdaContext xContext, 
                                                      ToolKit aTk,
                                                      Platform_Db aPlat,
                                                      Component  aComp,
                                                      boolean bExternal) 
                                                      throws IcofException {
	
	boolean bIsLinux = false;
	if (aPlat.getOs().contains("LIN"))
	    bIsLinux = true;
	
	return setDeliveredComps(xContext, aTk, bIsLinux, aComp, bExternal);
	
    }

    
    /**
     * Create a collection of component names from the BOM file.  The BOM
     * contains the latest collection of components so need to filter that
     * by what components are associated with the given TK
     *
     * @param xContext
     * @param aTk ToolKit object
     * @param bIsLinux Is true if reading linux BOM
     * @param aComp Component object
     * @param bExternal Is true if reading External BOM
     * @throws IcofException 
     */
    public static ArrayList<String> setDeliveredComps(EdaContext xContext, 
                                                      ToolKit aTk,
                                                      boolean bIsLinux,
                                                      Component aComp,
                                                      boolean bExternal) 
    throws IcofException {
	
	if (aTk.getToolKit().getRelease().getName().equals("15.1") &&
		! bIsLinux)
	    bIsLinux = true;
	
	// Get a list of components for this TK
	aTk.setComponents(xContext);
	ArrayList<String> dbNames = new ArrayList<String>();
	for (Component_Db dbComp : aTk.getComponents()) {
	    dbNames.add(dbComp.getName());
	}
	
	// Read the BOM file
	String bomName = getBomName(xContext, aTk, bExternal, bIsLinux);
	IcofFile bomFile = new IcofFile(bomName, false);
	bomFile.openRead();
	bomFile.read();
	bomFile.closeRead();

	// Create the collection
	ArrayList<String> bomNames = new ArrayList<String>();
	for (Object entry : bomFile.getContents()) {
	    String line = (String)entry;
	    if (line.startsWith(":") || line.isEmpty())
		continue;
	    String[] tokens = line.split("[.]");
	    bomNames.add(tokens[0].trim());
	}	
	
	// Create the final list by intersecting the DB and BOM lists
	ArrayList<String> comps = new ArrayList<String>();
	for (String compName : bomNames) {
	    if (dbNames.contains(compName)) {
		if (aComp == null)
		    comps.add(compName);
		else if (aComp.getName().equals(compName)) {
		    comps.add(compName);
		}
	    }
	}
	
	return comps;
	
    }

    
    /**
     * Construct the BOM file name
     *
     * @param xContext
     * @param aTk
     * @param bExternal
     * @param bIsLinux
     * @return
     * @throws IcofException 
     */
    private static String getBomName(EdaContext xContext, ToolKit aTk,
				     boolean bExternal, boolean bIsLinux) 
				     throws IcofException {

	String bomName = "/afs/eda.fishkill.ibm.com/edadist/tools/install/";
	if (bExternal) 
	    bomName += "Ext_BOM/Ext_BOM_toolkit";
	else
	    bomName += "SERVER_BOM/BOM_Server_toolkit";
	 
	
	bomName += aTk.getToolKit().getRelease().getBomName(bExternal) + "_";    
	bomName += getPlatform(xContext, aTk, bIsLinux);
		   
	System.out.println("BOM file: " + bomName);
	
	return bomName;
	
    }


    /**
     * Determine the platform name for this TK
     *
     * @param xContext
     * @param aTk
     * @param bIsLinux
     * @return
     * @throws IcofException 
     */
    private static String getPlatform(EdaContext xContext, ToolKit aTk,
				      boolean bIsLinux) throws IcofException {

	String platName = "";
	
	PlatformShow plats = new PlatformShow(xContext, aTk);
	plats.setPlatforms(xContext);
	for (Platform_Db plat : plats.getPlatforms()) {
	    if (bIsLinux && plat.getOs().equalsIgnoreCase("LINUX")) {
		platName = plat.getPackagingName();
		break;
	    }
	    if (! bIsLinux && plat.getOs().equalsIgnoreCase("RS_AIX")) {
		platName = plat.getPackagingName();
		break;
	    }

	}

	return platName;
	
    }


    /**
     * Set the install top level install directory
     *  - /afs/btv/data/vlsi/eclipz/common/tools/edatools/tk14.1.6z (INTERNAL)
     *  - /afs/btv/data/edatools/ptn/tk14.1.6z (EXTERNAL - btv)
     *  - /afs/eda/data/edatools/ptn/tk14.1.6z (EXTERNAL - eda)
     *  - /afs/eda/tkbld/tkinstall/ptn/aix64b/tk18.1.8e (EXTERNAL - ICC)
     * 
     * @param xContext
     */
    public static IcofFile constructTopInstallDir(EdaContext xContext, 
                                                  ToolKit aTk,
                                                  boolean bIsExternal, 
                                                  String aSite,
                                                  String installPlatform) {

	String dirName = "";
	if (bIsExternal) {
	    if (aSite.equalsIgnoreCase("ICC")) {
		dirName = ICC_DIR;
	    }
	    else {
		dirName = ASICS_DIR;
		dirName = dirName.replace("SITE",  getAfsSite(aSite));
	    }
	    dirName += File.separator + installPlatform;
	    dirName += File.separator + "tk" + aTk.getToolKit().getAltDisplayName();
	}
	else {
	    if (aSite.equalsIgnoreCase("btv")) {
		dirName = SERVER_BTV_DIR;
	    }
	    else if (aSite.equalsIgnoreCase("eda")) {
		dirName = SERVER_EDA_DIR;
	    }
	    else {
		dirName = "ERROR ... unknown site";
	    }
	    dirName += File.separator + "tk" + aTk.getToolKit().getDisplayName();
	}

	IcofFile topLevelDir = new IcofFile(dirName, true);
	
	if (! topLevelDir.exists())
	    topLevelDir.mkdir();

	return topLevelDir;
	
    }


    /**
     * Returns the AFS site name for the given site
     *
     * @param aSite
     * @return
     */
    private static String getAfsSite(String aSite) {

	String afsName = "";
	if (aSite.equalsIgnoreCase("eda"))
	    afsName = "eda.fishkill";
	else
	    afsName = aSite;
	
	return afsName;
	
    }

    /**
     * Create the VERSION file for this TK install
     *
     * @param xContext    Application context
     * @param installDir        Install directory
     * @param toolKit     Tool Kit object
     * @param installPlat Platform name
     * @param bExternal   If true install is for external customers
     * @throws IcofException 
     */
    public static void createVersionFile(IcofFile installDir,
                                         ToolKit toolKit, String installPlat, 
                                         boolean bExternal) throws IcofException {

	// EDA Server Toolkit 14.1.7 aix61_64 or EDA Server Toolkit 14.1.7 lnx26_64_rh5
	// ASIC Toolkit 14.1.7 aix61_64 or ASIC Toolkit 14.1.7 lnx26_64_rh5
	String line = "";
	if (bExternal) {
	    line = "ASIC ToolKit " + toolKit.getToolKit().getAltDisplayName();
	}
	else {
	    line = "EDA Server ToolKit " + toolKit.getName();
	}
	line += " " + installPlat;
	
	String fileName = installDir.getAbsolutePath() + File.separator + "VERSION";
	IcofFile file = new IcofFile(fileName, false);
	
	file.openWrite();
	file.writeLine(line);
	file.closeWrite();
	
    }

    
    /**
     * Create links back to .0 pli for readme files
     *
     * @param xContext    Application context
     * @param aDir        Install directory
     * @param toolKit     Tool Kit object
     * @param installPlat Platform name
     * @param bExternal   If true install is for external customers
     * @throws IcofException 
     */
    public static void createReadmeLinks(EdaContext xContext, IcofFile installDir,
                                         ToolKit toolKit, String installPlat, 
                                         boolean bExternal, boolean bDryRun,
                                         Logger logger) throws IcofException {

	String linkPath = ".." + File.separator + "tk";
	if (bExternal) 
	    linkPath += toolKit.getToolKit().getAltDisplayName();
	else 
	    linkPath += toolKit.getName();
	linkPath += ".0_" + installPlat + File.separator; 
	
	String[] readmes = new String[]{"readme.eda_platforminfo1401",  
	                                "readme.eda_releasenotes1401.html",
	                                "readme.flexnet1110",
	                                "EDAManuals.html",
	                                "html"};
	for (String rFile : readmes) {
	    // Skip flexnet readme if internal install
	    if (rFile.contains("flex") && ! bExternal)
		continue;
	    String command = "ln -sf " + linkPath + rFile + " " + rFile;

	    PkgUtils.runCommandInDir(command, installDir, bDryRun, logger);
	}
	
    }

    
    /**
     * Create the PET and PET_conf symlinks
     *
     * @param xContext  Application context
     * @param installDir       Directory to create links in
     * @throws IcofException 
     */
    public static void createPetLinks(EdaContext xContext, IcofFile installDir, 
                                      ToolKit aTk, boolean bDryRun, 
                                      Logger logger) 
    throws IcofException {

	// Create the PET link
	String release = aTk.getToolKit().getRelease().getName();
	String command = "ln -sf pet/" + release + "/pet PET";
	PkgUtils.runCommandInDir(command, installDir, bDryRun, logger);


	// Create the PET link
	command = "ln -sf pet/" + release + "/PET_eda.conf PET_eda.conf";
	PkgUtils.runCommandInDir(command, installDir, bDryRun, logger);
	
    }

    
    /**
     * Special processing for tgsupport component
     * - update the gna_toolkit_init.tcl file to include the new patch level 
     *
     * @param xContext
     * @param theGuideSupportDir
     * @throws IcofException 
     */
    public static String updateGnaInitTcl(IcofFile theGuideSupportDir,
                                          ToolKit aTk, ToolKitPackage aTkPkg,
                                          boolean bExternal, boolean bDryRun) 
    throws IcofException {

	String msg = "Updating gna_toolkit_init.tcl file in " + 
	              theGuideSupportDir.getAbsolutePath() + "\n";
	
	// Construct the new TK PLI specific line
	String newLine = "namespace eval {::gna::toolkits::";
	if (bExternal)
	    newLine += aTk.getToolKit().getAltDisplayName();
	else
	    newLine += aTk.getName();
	newLine += ".";
	
	if (aTkPkg.getName().contains("-")) {
	    int pkgNum = Integer.parseInt(aTkPkg.getName());
	    newLine += "beta" + Math.abs(pkgNum); 
	}
	else 
	    newLine +=  aTkPkg.getName();
	newLine += "} {";

	if (bDryRun) {
	    msg += " DRYRUN: would have updated " +
	            "gna_toolkit_init.tcl file with " + newLine;
	    return msg;
	}
	msg += " New Line: " + newLine;
	
	// Read the existing file
	String newTclDirName = theGuideSupportDir.getAbsolutePath() + 
	                       File.separator + "tcl/always_source" + 
	                       File.separator;
	IcofFile file = new IcofFile(newTclDirName + "gna_toolkit_init.tcl", 
	                             false);
 
	file.openRead();
	file.read();
	file.closeRead();

	
	// Replace the line
	Vector<String> newContents = new Vector<String>();
	for (Object entry : file.getContents()) {
	    String line = (String) entry;
	    if (line.contains("namespace eval"))
		newContents.add(newLine);
	    else
		newContents.add(line);
	}

	// Write the updated file
	file.openWrite();	
	file.write(newContents);	
	file.closeWrite();

	return msg;
	
    }

    
    /**
     * Special processing for theGuide component
     * - replace the gna_install.tcl file
     * @param xContext
     * @param theGuideSupportDir
     * @throws IcofException 
     */
    public static String updateGnaInstall(IcofFile theGuideSupportDir, 
                                          String siteName,
                                          boolean bExternal, boolean bDryRun,
                                          Logger logger) 
    throws IcofException {

	String msg = "Updating *_gna_install.tcl file in " + 
	              theGuideSupportDir.getAbsolutePath() + "\n";
	
	String newTclDirName = theGuideSupportDir.getAbsolutePath() + File.separator + "tcl";
	IcofFile newTclDir = new IcofFile(newTclDirName, true);

	String cfgDirName = "/afs/apd/data/gnacfg";

	// Update the gna_install.tcl file
	if (bExternal) {
	    if (! siteName.equalsIgnoreCase("icc")) {
		String cfgName = cfgDirName + File.separator + "toolkit_gna_install.tcl";
		String newName = "gna_install.tcl";
		InstallUtils.makeSymlink(cfgName, newName, newTclDir, 
		                         bDryRun, logger);
	    }
	    else {
		String command = "ln -sf ../../../../gna/gna_install_pli.tcl gna_install.tcl";
		PkgUtils.runCommandInDir(command, newTclDir, bDryRun, logger);
	    }
	}
	else {
	    String cfgName = cfgDirName + File.separator + "server_gna_install.tcl";
	    String newName = newTclDirName + File.separator + "gna_install.tcl";
	    IcofFile existingFile = new IcofFile(cfgName, false);
	    IcofFile newFile = new IcofFile(newName, false);
	    if (bDryRun) 
		msg += " DRYRUN: would have copied " + cfgName + " to " + newName;
	    else {
		existingFile.copy(newFile, true);
		msg += " Copying " + newFile.getAbsolutePath() + " to " + 
		         existingFile.getAbsolutePath() + "\n";
	    }
	}

	return msg;
	
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
    public static String makeSymlink(String existingFile,
                                     String newFile, IcofFile aDir,
                                     boolean bDryRun, Logger logger)
                                     throws IcofException {

	String msg = "";
	String[] command = new String[4];
	command[0] = "ln";
	command[1] = "-sf";
	command[2] = existingFile;
	command[3] = newFile;

	aDir.mkdirs();
	
	int rc = PkgUtils.runCommandInDir(command, aDir, bDryRun, logger);
	if (rc == 0)
	    msg += " Symlink created " + newFile + " -> " + existingFile;

	return msg;
	
    }

    
    /**
     * Construct the history directory name
     * 
     * @param topDir Top level install directory
     * @param aTkPkg Tool kit package to make history dir for
     */
    public static String getHistoryDirName(IcofFile topDir, 
                                           ToolKitPackage aTkPkg,
                                           ToolKit aTk) {
	
	String tkPkgName = "";
	
	if (aTk.getToolKit().getRelease().getName().equals("14.1")) {
	    tkPkgName += aTkPkg.getName();
	    if (aTk.getStageName().getName().equals(StageName_Db.STAGE_PREVIEW)) {
		tkPkgName += "_beta";
	    }
	    else {
		tkPkgName += "_prod";
	    }
	}
	else {
	    tkPkgName = aTk.getName() + "." + aTkPkg.getName();
	}
	
	String dirName = topDir.getAbsolutePath();
	dirName += File.separator + HISTORY_DIR;
	dirName += File.separator + tkPkgName;
	
	return dirName;
	
    }
    
    
    /**
     * Construct the history directory name for the preview volume
     * 
     * @param topDir Top level install directory
     * @param aTkPkg Tool kit package to make history dir for
     */
    public static String getVolHistoryDirName(IcofFile topDir,
                                              String volName) {
	
	String dirName = topDir.getAbsolutePath();
	dirName += File.separator + HISTORY_DIR;
	dirName += File.separator + volName;
	
	return dirName;
	
    }
    
}

