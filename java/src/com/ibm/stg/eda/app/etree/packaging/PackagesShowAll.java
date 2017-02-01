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
 * Update a Tool Kit Package a given state. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 01/13/2014 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree.packaging;

import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.ToolKitPackage_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Event;
import com.ibm.stg.eda.component.tk_etreeobjs.Events;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKitPackage;
import com.ibm.stg.iipmds.common.IcofException;

public class PackagesShowAll extends TkAppBase {
    

    /**
     * Constants.
     */
    public static final String APP_NAME = "pkg.showAll";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aTk Tool kit to view packages for
     */
    public PackagesShowAll(EdaContext aContext, ToolKit aTk)
    throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setToolKit(aTk);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * @exception IcofException Unable to construct ManageApplications object
     */
    public PackagesShowAll(EdaContext aContext) throws IcofException {

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
	    myApp = new PackagesShowAll(null);
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

	connectToDB(xContext);

	showPackages(xContext);

	commitToDB(xContext, APP_NAME);

    }


    /**
     * Display packages for this tool kit
     * 
     * @param xContext
     * @throws IcofException
     */
    private void showPackages(EdaContext xContext)
    throws IcofException {

	// Query for tk packages for this tk
	ToolKitPackage tkPkg = new ToolKitPackage(xContext, "");
	pkgs = tkPkg.dbLookupByTk(xContext, getToolKit());
	
	// Show the results
	for (ToolKitPackage_Db pkg : getPackages()) {
	    Events event = new Events(xContext, pkg.getEvents());
	    Event lastEvent = event.getLastEvent(xContext);
	    System.out.println(getToolKit().getName() + "." + 
	                       pkg.getName() + " (" + 
	                       lastEvent.getEventName().getName() + ")" );
	    
	}
	
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

	// Read the Tool Kit
	if (params.containsKey("-t"))
	    setToolKit(xContext, (String) params.get("-t"));
	else
	    errors += "Tool Kit (-t) is a required parameter\n";

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

	logInfo(xContext, "App          : " + APP_NAME + "  " + APP_VERSION,
		verboseInd);
	logInfo(xContext, "Tool Kit   :" + getToolKit().getName(), verboseInd);
	logInfo(xContext, "DB Mode      : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose      : " + getVerboseInd(xContext), verboseInd);
    }


    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Display packages for the given Tool Kit. \n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t toolkit> [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  toolkit  = Tool kit name including maint (14.1.6.0 ...)\n");
	usage.append("  -y       = (optional) Quiet mode (no putput headers)\n");
	usage.append("  -y       = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode   = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h       = Help (shows this information)\n");
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
    private Vector<ToolKitPackage_Db> pkgs;
    

    /**
     * Getters
     */
    public Vector<ToolKitPackage_Db> getPackages() { return pkgs; }
    protected String getAppName() { return APP_NAME; }
    protected String getAppVersion() { return APP_VERSION; }


    /**
     * Setters
     */
    // @formatter:on


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
    }

}
