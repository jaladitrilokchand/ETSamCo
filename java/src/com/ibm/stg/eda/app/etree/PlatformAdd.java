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
 * Add new platform to release. 
 *-----------------------------------------------------------------------------
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 07/16/2015 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Platform_Db;
import com.ibm.stg.eda.component.tk_etreedb.Release_Platform_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Release;
import com.ibm.stg.iipmds.common.IcofException;

public class PlatformAdd extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "plat.add";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aCompType Component Type to add
     */
    public PlatformAdd(EdaContext aContext, Release aRel) throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setRelease(aRel);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * 
     * @exception IcofException Unable to construct ManageApplications object
     */
    public PlatformAdd(EdaContext aContext) throws IcofException {

	this(aContext, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new PlatformAdd(null);
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
     */
    public void process(EdaContext xContext)
    throws IcofException {

	// Connect to the database
	connectToDB(xContext);

	// Add the new platform
	addPlatform(xContext);
	
	logInfo(xContext, "\nSuccessfully added the platform... ", true);

	commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);

    }


    /**
     * Add a platform 
     *
     * @param xContext  Application context
     * @throws IcofException 
     */
    public void addPlatform(EdaContext xContext) throws IcofException {

	// Lookup the platform or add it to db
	setPlatform(xContext);
	
	// Associate the platform with release
	linkReleasePlatform(xContext);
	
    }


    /**
     * Assigns the platform to the release
     *
     * @param xContext
     * @throws IcofException 
     */
    private void linkReleasePlatform(EdaContext xContext) throws IcofException {

	Release_Platform_Db relPlat;
	relPlat = new Release_Platform_Db(getRelease().getRelease(), 
	                                  getPlatform());
	
	try {
	    relPlat.dbLookupByIds(xContext);
	}
	catch(IcofException ie) {
	    relPlat.dbAddRow(xContext);
	}
	
    }


    /**
     * Lookup platform in db or add if doesn't exist
     *
     * @param xContext
     * @throws IcofException 
     */
    private void setPlatform(EdaContext xContext) throws IcofException {

	platform = new Platform_Db(getOs(), getSize(), getTechLevel(), 
	                           getVersion(), getPkgName(), getSystemName(), getInstallShortName());
	try {
	    platform.dbLookupByName(xContext);
	}
	catch(IcofException ie) {
	    platform.dbAddRow(xContext, getUser());
	}
	
    }


    /**
     * Define the single and param command line switches
     * 
     * @param singleSwitches Collection of single switches
     * @param argSwitches Collection of switches needing a parameter
     */
    protected void createSwitches(Vector<String> singleSwitches,
				  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-r");
	argSwitches.add("-o");
	argSwitches.add("-s");
	argSwitches.add("-l");
	argSwitches.add("-v");
	argSwitches.add("-pkg");
	argSwitches.add("-sys");
	argSwitches.add("-is");
	
    }


    /**
     * Parse the command line arguments
     * 
     * @param params Collection of command line arguments
     * @param errors Error messages
     * @param xContext Application context
     */
    protected String readParams(Hashtable<String, String> params,
				String errors, EdaContext xContext)
    throws IcofException {

	// Read the release
	if (params.containsKey("-r")) {
	    setRelease(xContext, params.get("-r"));
	}
	else {
	    errors += "Release (-r) is a required parameter\n";
	}

	// Read the operating sys
	if (params.containsKey("-o"))
	    setOs(params.get("-o"));
	else
	    errors += "OS name (-o) is a required parameter\n";

	// Read the operating sys size
	if (params.containsKey("-s"))
	    setSize(Short.parseShort(params.get("-s")));
	else
	    errors += "OS size (-s) is a required parameter\n";

	// Read the operating sys tech level
	if (params.containsKey("-l"))
	    setTechLevel(params.get("-l"));
	else
	    errors += "OS tech level (-l) is a required parameter\n";

	// Read the operating sys version
	if (params.containsKey("-v"))
	    setVersion(params.get("-v"));
	else
	    errors += "OS version (-v) is a required parameter\n";
	
	// Read the packaging name
	if (params.containsKey("-pkg"))
	    setPkgName(params.get("-pkg"));
	else
	    errors += "Packaging name (-pkg) is a required parameter\n";
	
	// Read the @system name
	if (params.containsKey("-sys"))
	    setSystemName(params.get("-sys"));
	else
	    errors += "System name (-sys) is a required parameter\n";
	
	// Read the Install Short Name
	if (params.containsKey("-is"))
	    setInstallShortName(params.get("-is"));
	else
	    errors += "Install Short Name (-is) is a required parameter\n";

	return errors;

    }


    /**
     * Show the input parameters
     * 
     * @param dbMode Database mode
     * @param xContext Application context
     */
    protected void displayParameters(String dbMode, EdaContext xContext) {

	logInfo(xContext, "App       : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	logInfo(xContext, "Release   : " + getRelease().getName(),
	        verboseInd);
	logInfo(xContext, "OS        : " + getOs(), verboseInd);
	logInfo(xContext, "Size      : " + getSize(), verboseInd);
	logInfo(xContext, "Tech Lvl  : " + getTechLevel(), verboseInd);
	logInfo(xContext, "Version   : " + getVersion(), verboseInd);
	logInfo(xContext, "Sys name  : " + getSystemName(), verboseInd);
	logInfo(xContext, "Install Short Name : " + getInstallShortName(), verboseInd);
	logInfo(xContext, "Pkg name  : " + getPkgName(), verboseInd);
	logInfo(xContext, "DB Mode   : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose   : " + getVerboseInd(xContext), verboseInd);
	
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Show details about platforms associated with the specified Tool Kit. \n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-r release> <-o os> \n");
	usage.append("         <-s size> <-l tech_level> <-v version>\n");
	usage.append("         <-pkg pkg_name> <-sys system_name> <-is install_short_name> [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  release    = TK release to add platform to (ie, 14.1, 15.1 ...).\n");
	usage.append("  os         = Operating sys name { LINUX | AIX }\n");
	usage.append("  size       = Bit size { 32 | 64}\n");
	usage.append("  tech_level = for AIX only { NA, TL07 ... }\n");
	usage.append("  version    = OS version (ie, 50, 51, 61 ...)\n");
	usage.append("  pkg_name   = Plat name used for packages (aix61_64, lnx26_64_rh5 ...)\n");
	usage.append("  system_name= System Name returned by executing 'sys' command\n");
	usage.append("  install_short_name= Platform specific install location(ex: aix64b or lin64b_x86 or lin64b_ppc)\n");
	usage.append("  -y         = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode     = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h         = Help (shows this information)\n");
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
     * @formatter:off
     */
    Release release;
    Platform_Db platform;
    String os;
    short size;
    String techLevel;
    String version;
    String pkgName;
    String systemName;
    String installShortName;
    
    
    /**
     * Getters.
     */
    public Release getRelease()  { return release; }
    public Platform_Db getPlatform()  { return platform; }
    public String getOs()  { return os; }
    public short getSize()  { return size; }
    public String getTechLevel()  { return techLevel; }
    public String getVersion()  { return version; }
    public String getPkgName()  { return pkgName; }
    public static boolean getRequestHelp() { return requestHelp; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
    public String getSystemName()  { return systemName; }
    public String getInstallShortName()  { return installShortName; }
        

    /**
     * Setters.
     */
    private void setRelease(Release aRel)  { release = aRel; }
    private void setOs(String aName)  { os = aName; }
    private void setSize(short aNum)  { size = aNum; }
    private void setTechLevel(String aName)  { techLevel = aName; }
    private void setVersion(String aName)  { version = aName; }
    private void setPkgName(String aName)  { pkgName = aName; }
    private void setSystemName(String aName)  { systemName = aName; }
    private void setInstallShortName(String aName)  { installShortName = aName; }
    
    
    /**
     * Set the new Release object
     * @param xContext       Application context.
     * @param aName          Release name 
     * @throws IcofException 
     */
    private void setRelease(EdaContext xContext, String aName) 
    throws IcofException { 

	logInfo(xContext, "Looking up new release ...", verboseInd);
	
	// Return if nothing to look up
	if ((aName == null) || aName.isEmpty())
	    return;

	release = new Release(xContext, aName, "");
	release.dbLookupByName(xContext);
	logInfo(xContext, "New Release: " + getRelease().toString(xContext), 
	        verboseInd);
	
    }


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

    //@formatter:off

}
