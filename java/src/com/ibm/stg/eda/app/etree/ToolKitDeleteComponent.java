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
 * Displays tool kit details. 
 *-----------------------------------------------------------------------------
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 03/20/2013 GFS  Initial coding.
 * 06/20/2013 GFS  Updated to display parent TK's name.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class ToolKitDeleteComponent extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "tk.deleteComp";
    public static final String APP_VERSION = "v1.0";


    /**
     * Constructor
     *
     * @param     aContext       Application context
     * @param     aCompType      Component Type to add
     */
    public ToolKitDeleteComponent(EdaContext aContext, ToolKit aTk, 
                                  Component aComp)
                                  throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	setToolKit(aTk);
	setComponent(aComp);
    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public ToolKitDeleteComponent(EdaContext aContext) throws IcofException {

	this(aContext, null, null);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

	TkAppBase myApp = null;
	try {
	    myApp = new ToolKitDeleteComponent(null);
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
     * @param aContext      Application Context
     * @throws              IcofException
     */
    public void process(EdaContext xContext) throws IcofException {

	connectToDB(xContext);

	// Look up TK and show it's details
	delete(xContext);

	commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);

    }


    /**
     * Displays details about the existing tool kit
     * 
     * @param xContext  Application context
     * @throws IcofException  Trouble querying database.
     */
    private void delete(EdaContext xContext) throws IcofException {

	String reply = getToolKit().dbDeleteComponent(xContext, 
	                                              getComponent(), getUser());
	
	logInfo(xContext, reply, true);
	
    }


    /**	
     * Define the single and param command line switches
     * 
     * @param singleSwitches  Collection of single switches	
     * @param argSwitches     Collection of switches needing a paramter
     */
    protected void createSwitches(Vector<String> singleSwitches, 
                                  Vector<String> argSwitches) {

	singleSwitches.add("-y");
	singleSwitches.add("-h");
	argSwitches.add("-db");
	argSwitches.add("-c");
	argSwitches.add("-t");

    }


    /**
     * Parse the command line arguments
     * 
     * @param params    Collection of command line arguments
     * @param errors    Error messages
     * @param xContext  Application context
     */
    protected String readParams(Hashtable<String, String> params, 
                                String errors, EdaContext xContext)
                                throws IcofException {

	// Read the Tool Kit
	if (params.containsKey("-t")) {
	    setToolKit(xContext,  params.get("-t"));
	}
	else {
	    errors += "Tool Kit (-t) is a required parameter\n";
	}

	// Read the Component
	if (params.containsKey("-c")) {
	    setComponent(xContext,  params.get("-c"));
	}
	else {
	    errors += "Component (-c) is a required parameter\n";
	}
	
	return errors;

    }


    /**
     * Show the input parameters
     * 
     * @param dbMode    Database mode
     * @param xContext  Application context
     */
    protected void displayParameters(String dbMode, EdaContext xContext) {

	logInfo(xContext, "App       : " + APP_NAME + "  " + APP_VERSION,
	        verboseInd);
	logInfo(xContext, "Tool Kit : " + getToolKit().getName(), verboseInd);
	logInfo(xContext, "Component: " + getComponent().getName(), verboseInd);
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
	usage.append("Delete this component from the specified tool kit. \n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t tool_kit> <-c component> [y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  tool_kit  = Tool Kit (14.1.3 ...).\n");
	usage.append("  component = Component to delete (ess, hdp, oa ...).\n");
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
     */



    /**
     * Getters.
     */
    public static boolean getRequestHelp() { return requestHelp; }
    protected String getAppName() { return APP_NAME;}
    protected String getAppVersion() { 	return APP_VERSION;}


    /**
     * Setters.
     */


    @Override
    protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
	return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
    }


}
