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
 * Native Java log test
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 06/17/2014 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree.logging;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.iipmds.common.IcofException;

public class LoggerTest extends TkAppBase {

    /**
     * Constants.
     */
    public static final String APP_NAME = "logTest";
    public static final String APP_VERSION = "v1.0";
    
    
    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aTkName  Tool Kit name (14.1.8e, 18.1.8e ...)
     * @param aPkg     Tool kit package to process
     * @param aRelNotesDir Object containing path of where to write rel notes to
     * @param bWrite   If true write rel notes to file otherwise dump to screen
     */
    public LoggerTest(EdaContext aContext, boolean bShow) throws IcofException {

	super(aContext, APP_NAME, APP_VERSION);
	
    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * @exception IcofException Unable to construct ManageApplications object
     */
    public LoggerTest(EdaContext aContext) throws IcofException {

	this(aContext, false);

    }


    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     * 
     * @param argv[] the command line arguments
     * @throws IOException 
     * @throws SecurityException 
     * @throws IcofException 
     */
    public static void main(String argv[]) throws IcofException {

	TkAppBase myApp = null;
	try {
	    myApp = new LoggerTest(null);
	    start(myApp, argv, LoggerTest.class.getName(), APP_NAME);

	    // Try some logging
	    logger.log(Level.INFO, "NEW .. Hello world!");
	    logger.info("NEW .. and again");
	    logger.info("NEW .. and again");
	    logger.warning("hey .. this doesn't look right");
	    logger.severe("what happened here?");

	    logger.fine("To log file only ...");

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
     * @throws SQLException 
     * @throws IOException 
     */
    public void process(EdaContext xContext)
    throws IcofException, SQLException, IOException {

	connectToDB(xContext);

	doSomething(xContext);

	commitToDB(xContext, APP_NAME);

    }


    /**
     * Generate and display release notes
     * 
     * @param aContext Application Context
     * @throws IcofException
     * @throws SQLException 
     * @throws IOException 
     */
    public void doSomething(EdaContext xContext) throws IcofException {

	logInfo(xContext, "OLD .. Hello world!", true);
	
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
	    logInfo(xContext, "Tool Kit: " + (String)params.get("-t"), true);
	}
	else {
	    errors += "Tool kit maintenance name (-t) is a required parameter\n";
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

	StringBuffer msg = new StringBuffer();
	
	msg.append("App        : " + APP_NAME + "  " + APP_VERSION + "\n");
	msg.append("DB Mode    : " + dbMode + "\n");
	msg.append("Verbose    : " + getVerboseInd(xContext) + "\n");
	
	logInfo(xContext, msg.toString(), verboseInd);
	
    }



    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

	StringBuffer usage = new StringBuffer();
	usage.append("------------------------------------------------------\n");
	usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
	usage.append("------------------------------------------------------\n");
	usage.append("Generate release notes for the given tool kit package.\n");
	usage.append("\n");
	usage.append("USAGE:\n");
	usage.append("------\n");
	usage.append(APP_NAME + " <-t tk_pkg> [-write relNoteDir] [-y] [-h] [-db dbMode]\n");
	usage.append("\n");
	usage.append("  tk_pkg     = Tool kit Package name (ie, 14.1.6.8, 14.1.6z.1 ...)\n");
	usage.append("  relNoteDir = Location to write release notes to\n");
	usage.append("               Default is write rel notes to screen\n");
	usage.append("  -y         = (optional) Verbose mode (echo messages to screen)\n");
	usage.append("  dbMode     = (optional) DEV | PROD (defaults to PROD)\n");
	usage.append("  -h         = Help (shows this information)\n");
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
   
    
    /**
     * Getters
     */
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
