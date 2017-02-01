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
 * Gather tool kit size data 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 06/04/2014 GFS  Initial coding
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Component_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class ToolKitSize extends TkAppBase {

    /**
     * Constants
     */
    public static final String APP_NAME = "tk.size";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aTk  Tool kit to prep for install
     */
    public ToolKitSize(EdaContext aContext, ToolKit aTk, boolean bAsics) 
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setToolKit(aTk);
	setAsicsToolKit(bAsics);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * @exception IcofException Unable to construct ManageApplications object
     */
    public ToolKitSize(EdaContext aContext) throws IcofException {

	this(aContext, null, false);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new ToolKitSize(null);
	    start(myApp, argv);
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
     * @throws IOException 
     */
    public void process(EdaContext xContext)
    throws IcofException, IOException {

	connectToDB(xContext);

	sizeIt(xContext);

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
    public void sizeIt(EdaContext xContext) throws IcofException, IOException {

	// Determine components in Tool Kit and sort by TK type (server/asics)
	setComponents(xContext);
	
	// Determine the size of each component
	for(String sCompName : getAllComponents()) {
	    setSizes(xContext, sCompName);
	}
	
	// Display the size info
	displayData(xContext);

    }


    /**
     * Display the data
     *
     * @param xContext
     */
    private void displayData(EdaContext xContext) {

	String line = formatLine("Component", 
	                        "File size",
	                        "Num Files",
	                        "ASICs TK",
	                        "Server TK");
	System.out.println("\n\nSummary\n\n" + line);

	line = formatLine("---------", 
	                 "---------",
	                 "---------",
	                 "---------",
	                 "---------");
	System.out.println(line);
	
	
	long asicsCounts = 0;
	long asicsSizes = 0;
	long serverCounts = 0;
	long serverSizes = 0;
	
	for (String compName : allComponents) {
	    line = formatLine(compName, 
	                     getComponentSizes().get(compName),
	                     getComponentCounts().get(compName),
	                     getAsicsComponents().contains(compName),
	                     getServerComponents().contains(compName));
	    System.out.println(line);
	    
	    if (getAsicsComponents().contains(compName)) {
		asicsCounts += getComponentCounts().get(compName);
		asicsSizes += getComponentSizes().get(compName);
	    }
	    if (getServerComponents().contains(compName)) {
		serverCounts += getComponentCounts().get(compName);
		serverSizes += getComponentSizes().get(compName);
	    }

	    // If not in either BOM then add to both .. worst case scenario
	    if (! getAsicsComponents().contains(compName) &&
	        ! getServerComponents().contains(compName)) {
		asicsCounts += getComponentCounts().get(compName);
		asicsSizes += getComponentSizes().get(compName);
		serverCounts += getComponentCounts().get(compName);
		serverSizes += getComponentSizes().get(compName);
	    }
	    
	}
	
	System.out.println("");

	line = formatLine("TOTALS", "-------->", "Size",
	                 String.valueOf(asicsSizes),
	                 String.valueOf(serverSizes));
	System.out.println(line);
	
	line = formatLine("TOTALS", "-------->", "Count",
	                 String.valueOf(asicsCounts),
	                 String.valueOf(serverCounts));
	System.out.println(line);
	
	
    }


    /**
     *
     *
     * @param compName
     * @param string
     * @param string2
     * @param valueOf
     * @param valueOf2
     * @return
     */
    private String formatLine(String compName, String size, String count,
			     String isAsics, String isServer) {

	String line = IcofStringUtil.padString(compName, 15, " ");
	line += IcofStringUtil.padString(size, 15, " ");
	line += IcofStringUtil.padString(count, 15, " ");
	line += IcofStringUtil.padString(isAsics, 15, " ");
	line += IcofStringUtil.padString(isServer, 15, " ");
	
	return line;
	
    }


    /**
     * Format the data for display
     *
     * @param compName
     * @param size
     * @param count
     * @param isAsics
     * @param isServer
     * @return
     */
    private String formatLine(String compName, Long size, Long count,
			     boolean isAsics, boolean isServer) {

		

	return formatLine(compName, 
	                  Long.toString(size), 
	                  Long.toString(count), 
	                  Boolean.toString(isAsics), 
	                  Boolean.toString(isServer));

    }


    /**
     * Determine all files for this component and their size
     *
     * @param xContext
     * @param sCompName
     * @throws IcofException 
     */
    private void setSizes(EdaContext xContext, String sCompName) 
    throws IcofException {

	System.out.println(" " + sCompName + " reading TK dir ...");
	
	// Find all the files
	Collection<File> myFiles = new HashSet<File>();;
	//if (! sCompName.startsWith("oa")) {
	    setToolKitDir(xContext, sCompName);	
	    myFiles = listFileTree(tkDirectory, false, true);
//	}
//	else {
//	    System.out.println(" Skiping OA component");
//	}
	
	// Determine their size
	long totalSize = 0;
	for (File file : myFiles) {
	    totalSize += file.length();
	}
		
	// Save the results
	if (getComponentCounts() == null)
	    compCounts = new HashMap<String, Long>();
	getComponentCounts().put(sCompName, Long.valueOf((long)myFiles.size()));

	if (getComponentSizes() == null)
	    compSizes = new HashMap<String, Long>();
	getComponentSizes().put(sCompName, Long.valueOf(totalSize));
	
	System.out.println("  File count: " + myFiles.size());
	System.out.println("  File size : " + totalSize);
	
    }


    /**
     * Determine the components for this Tool Kit and sort by ASIC or SERVER 
     *
     * @param xContext
     * @throws IcofException 
     */
    private void setComponents(EdaContext xContext) throws IcofException {

	System.out.println("Looking up components for this tool kit ...");
	
	// Create a collection of all components
	allComponents = new ArrayList<String>();
	if (getComponent() == null) {
	    getToolKit().setComponents(xContext);
	    for (Component_Db comp : getToolKit().getComponents()) {
		allComponents.add(comp.getName());
	    }
	}
	else {
	    allComponents.add(getComponent().getName());
	}
	
	// Read the ASIC/server BOM files
	asicComponents = setDeliveredComps(xContext, getToolKit(), 
	                                   false, getComponent(), true);

	serverComponents = setDeliveredComps(xContext, getToolKit(), 
	                                     false, getComponent(), false);
	
	System.out.println(" Comps (all)   : " + getAllComponents().size());
	System.out.println(" Comps (ASICs) : " + getAsicsComponents().size());
	System.out.println(" Comps (server): " + getServerComponents().size());
	
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
    private Collection<File> listFileTree(File dir, boolean stopOnError,
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
		    //System.out.println("File: " + entry.getAbsolutePath() + " - " + entry.length());
		}
		else {
		    if (includeDirs) {
			fileTree.add(entry);
			//System.out.println("Dir: " + entry.getAbsolutePath() + " - n/a");
		    }
		    if (! entry.getAbsolutePath().contains("/oaroot"))
			fileTree.addAll(listFileTree(entry, stopOnError, 
			                             includeDirs));
		}
	    }
	}

	return fileTree;

    }


   
    /**
     * Set the TK top level directory .. /afs/eda/<location>/<component>/<release>
     * 
     * @param xContext
     * @param sCompName 
     */
    private void setToolKitDir(EdaContext xContext, String sCompName) {

	String stage = getToolKit().getStageName().getName();
	String loc = "build";
	if (stage.equalsIgnoreCase("preview"))
	    loc = "shipb";
	else if (stage.equalsIgnoreCase("production"))
	    loc = "tkb";
	else 
	    loc = stage.toLowerCase();
	
	String dirName = "/afs/eda/" + loc + File.separator + 
	                 sCompName + File.separator + 
	                 getToolKit().getToolKit().getRelease().getName();
	
	if (getTkDir() != null)
	    tkDirectory = null;
	tkDirectory = new File(dirName);
	
	System.out.println("  TK dir: " + getTkDir().getAbsolutePath());

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
	
	// Get a list of components for this TK
	aTk.setComponents(xContext);
	ArrayList<String> dbNames = new ArrayList<String>();
	for (Component_Db dbComp : aTk.getComponents()) {
	    dbNames.add(dbComp.getName());
	}
	
	// Read the BOM file
	String bomName = "/afs/eda.fishkill.ibm.com/edadist/tools/install/";
	if (bExternal) 
	    bomName += "Ext_BOM/Ext_BOM_toolkit181_";
	else
	    bomName += "SERVER_BOM/BOM_Server_toolkit141_";
	    
	if (bIsLinux)
	    bomName += "lnx26_64_rh5";
	else
	    bomName += "aix61_64";

	System.out.println("BOM file: " + bomName);
	
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
	
	// Create the final list  by intersecting the DB and BOM lists
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
	    setToolKit(xContext, (String) params.get("-t"));
	}
	else {
	    errors += "Tool kit name (-t) is a required parameter\n";
	}

	// Read the component
	if (params.containsKey("-c")) {
	    setComponent(xContext, (String) params.get("-c"));
	}

	// Read the server/asics switch
	if (params.containsKey("-asics")) {
	    setAsicsToolKit(true);
	}
	else if (params.containsKey("-server")) {
	    setServerToolKit(true);
	}
	else {
	    setAsicsToolKit(true);
	    setServerToolKit(true);
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
	singleSwitches.add("-asics");
	singleSwitches.add("-server");
	argSwitches.add("-c");
	argSwitches.add("-db");
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
	if (getComponent() != null)
	    logInfo(xContext, "Component  : " + getComponent().getName(), 
	            verboseInd);
	logInfo(xContext, "ASICs TK    : " + isAsicsToolKit(), verboseInd);
	logInfo(xContext, "Server TK   : " + isServerToolKit(), verboseInd);
	logInfo(xContext, "DB Mode     : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose     : " + getVerboseInd(xContext),
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
	usage.append("Scan the AFS directory structure for the given tool kit\n");
	usage.append("to determine the space used/file count of component.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t tool_kit> [-server | -asics] \n");
	usage.append("                [-c component] [-y] [-h]\n");
	usage.append("\n");
	usage.append("  tool_kit  = Tool kit name (ie, 14.1.7, 14.1.6z ...)\n");
	usage.append("  -server   = Examine components in the server TK only\n");
	usage.append("  -asics    = Examine components in the ASICs TK only\n");
	usage.append("  component = Install pkgs for component only\n");
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
    private boolean asicsInstall = false;
    private boolean serverInstall = false;
    private File tkDirectory;
    private ArrayList<String> allComponents;
    private ArrayList<String> asicComponents;
    private ArrayList<String> serverComponents;
    private HashMap<String, Long> compSizes;
    private HashMap<String, Long> compCounts;
    
    
    /**
     * Getters
     */
    public boolean isAsicsToolKit() { return asicsInstall; }
    public boolean isServerToolKit() { return serverInstall; }
    public File getTkDir() { return tkDirectory; }
    public ArrayList<String> getAllComponents() { return allComponents; }
    public ArrayList<String> getAsicsComponents() { return asicComponents; }
    public ArrayList<String> getServerComponents() { return serverComponents; }
    public HashMap<String, Long> getComponentSizes() { return compSizes; }
    public HashMap<String, Long> getComponentCounts() { return compCounts; }
    

    /**
     * Setters
     */
    private void setAsicsToolKit(boolean aFlag) { asicsInstall = aFlag; }
    private void setServerToolKit(boolean aFlag) { serverInstall = aFlag; }
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }
    // @formatter:on


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }

}
