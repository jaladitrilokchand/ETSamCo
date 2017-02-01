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
 * Displays platform details. 
 *-----------------------------------------------------------------------------
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 11/13/2013 GFS  Initial coding.
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
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class PlatformShow extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "plat.show";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aCompType Component Type to add
     */
    public PlatformShow(EdaContext aContext, ToolKit aTk) throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setToolKit(aTk);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * 
     * @exception IcofException Unable to construct ManageApplications object
     */
    public PlatformShow(EdaContext aContext) throws IcofException {

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

	    myApp = new PlatformShow(null);
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

	// Look up TK and show it's details
	setPlatforms(xContext);
	showPlatforms(xContext);

	rollBackDBAndSetReturncode(xContext, APP_NAME, SUCCESS);

    }


    /**
     * Lookup the platforms 
     *
     * @param xContext
     * @throws IcofException 
     */
    public void setPlatforms(EdaContext xContext) throws IcofException {

	// Look up the platforms for this TK
	Platform_Db platform = new Platform_Db("");
	platforms = platform.dbLookupByTk(xContext, getToolKit().getToolKit());
	
    }


    /**
     * Displays details about the existing tool kit
     * 
     * @param xContext Application context
     * @throws IcofException Trouble querying database.
     */
    private void showPlatforms(EdaContext xContext)
    throws IcofException {

	// Display the platform data
	StringBuffer msg = new StringBuffer();
	for (Platform_Db myPlat : getPlatforms()) {
	    
	    if (getShowShippingNames())
		msg.append(myPlat.getShippingName() + "\n");
	    else if (getShowPackagingNames())
		msg.append(myPlat.getPackagingName() + "\n");
	    else 
		msg.append(myPlat.toString(xContext) + "\n");

	}

	logInfo(xContext, msg.toString(), true);
	
    }


    /**
     * Define the single and param command line switches
     * 
     * @param singleSwitches Collection of single switches
     * @param argSwitches Collection of switches needing a paramter
     */
    protected void createSwitches(Vector<String> singleSwitches,
				  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	singleSwitches.add("-ship");
	singleSwitches.add("-pkg");
	argSwitches.add("-db");
	argSwitches.add("-t");

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

	// Read the Tool Kit
	if (params.containsKey("-t"))
	    setToolKit(xContext, params.get("-t"));
	else
	    errors += "Tool Kit (-t) is a required parameter\n";

	if (params.containsKey("-ship"))
	    setShowShippingNames(true);
	
	if (params.containsKey("-pkg"))
	    setShowPackagingNames(true);
	
	if (getShowPackagingNames() && getShowShippingNames()) 
	    errors += "Please don't specify -ship and -pkg in the same invocation\n";
	
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
	logInfo(xContext, "Tool Kit : " + getToolKit().getName(),
	        verboseInd);
	logInfo(xContext, "Show Ship : " + getShowShippingNames(), verboseInd);
	logInfo(xContext, "Show Pkg  : " + getShowPackagingNames(), verboseInd);
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
	usage.append(APP_NAME + " <-t tool_kit> [-ship | -pkg] [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  tool_kit  = View platforms for this Tool Kit (14.1.3 ...).\n");
	usage.append("  -ship     = (optional) Show shipping plat names only\n");
	usage.append("  -pkg      = (optional) Show packaging plat names only\n");
	usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h        = Help (shows this information)\n");
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
    Vector<Platform_Db> platforms;
    private boolean shippingNames = false;
    private boolean packagingNames = false;
    
    
    /**
     * Getters.
     */
    public Vector<Platform_Db> getPlatforms() { return platforms; }
    public boolean getShowShippingNames() { return shippingNames; }
    public boolean getShowPackagingNames() { return packagingNames; }
    public static boolean getRequestHelp() { return requestHelp; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}
        

    /**
     * Setters.
     */
   private void setShowShippingNames(boolean aFlag) { shippingNames = aFlag; }
   private void setShowPackagingNames(boolean aFlag) { packagingNames = aFlag; }

    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }

    //@formatter:off

}
