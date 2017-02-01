/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *    FILE: tkAppBase.java
 *
 *-PURPOSE---------------------------------------------------------------------
 * tkAppBase class definition file.
 *   This class serves as a base class for the Tool Kit applications,
 *   because they have much of the same data and constants.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 05/17/2010 GFS  Initial Coding
 * 05/10/2011 GFS  Updated connectToDB() to support greenjiffy db server.
 * 06/13/2012 GFS  Updated to not print Program Return Code ... when each 
 *                 application completes.
 * 11/06/2012 GFS  Added setCqToolKit() method.
 * 11/27/2012 GFS  Removed setCqToolKit() and updated setToolKit() to work for
 *                 all tool kit variants.
 * 05/28/2013 GFS  Updated to db/app mode is correctly displayed.
 * 06/14/2013 GFS  Updated SetToolKit so the TK name is always lower case which
 *                 is how the name and CQ relnames are stored in the DB
 * 12/23/2013 GFS  Updated to support application logging
 * 06/04/2014 GFS  Added Location object
 * 06/18/2014 GFS  Added support for native Java logging
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreebase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.BranchName;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.Location;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofDateUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofSystemUtil;
import com.ibm.stg.iipmds.common.SessionLog;

public abstract class TkAppBase {

    // Constants
    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;
    public static final int INVALID_PARMS = 2;
    public static final int APPCONTEXT_ERR = 4;
    public static String PROP_FILE = "/afs/eda/data/edainfra/tools/enablement/dev/properties/etree_log.properties";
    public static String LOG_DIR = "/afs/eda/data/edainfra/tools/logs";
    // Trilok changes - Add new dir path to log pkg create logs
    public static String PKG_CREATE_LOG_DIR = "/afs/eda/data/edainfra/logs" ;
    public static String PKG_CREATE = "pkg.create" ;
    public static String LOG_EXT = ".log";

    
    /**
     * Constructor
     * 
     * @param aContext Application context
     * @param aTechRelease Technology release that contains the specified delta
     *            or dip
     * @param aVerboseInd true, to echo messages to screen; false, otherwise
     * @param anAppName the application name
     * @param anAppVersion the application version
     * 
     */
    protected TkAppBase(EdaContext aContext, boolean aVerboseInd,
			String anAppName, String anAppVersion) {

	setVerboseInd(aContext, aVerboseInd);
	setReturnCode(aContext, FAILURE);
	setAppModeForCalledApps(aContext, "");
	if (aContext != null) {
	    setAppModeForCalledApps(aContext, aContext.getAppMode());
	}
	setAppName(aContext, anAppName);
	setAppVersion(aContext, anAppVersion);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param aContext the application context
     * @param anAppName the application name
     * @param anAppVersion the application version
     * 
     */
    protected TkAppBase(EdaContext aContext, String anAppName,
			String anAppVersion) {

	this(aContext, false, anAppName, anAppVersion);

    }

    
    /**
     * Set the User object from the user'd id
     * 
     * @param xContext Application context.
     * @param aUserName AFS user id
     * @throws IcofException
     */
    protected void setUser(EdaContext xContext, String aUserid)
    throws IcofException {

	if (getUser() == null) {
	    user = new User_Db(aUserid, false);
	    try {
		user.dbLookupByAfs(xContext);
	    }
	    catch (IcofException ie) {
		// Not found so populate from AFS and add to DB
		user.populateFromAfs(xContext);
		user.dbAddRow(xContext);
	    }
	}
	logInfo(xContext, "User: " + getUser().toString(xContext), false);
    }


    /**
     * Set the ChangeRequest object
     * 
     * @param xContext Application context.
     * @param aName Change Request CQ name
     * @throws IcofException
     */
    protected void setChangeRequest(EdaContext xContext, String aName)
    throws IcofException {

	if (getChangeRequest() == null) {
	    changeRequest = new ChangeRequest(xContext, aName);
	    if (! aName.equalsIgnoreCase("DEV"))
		changeRequest.dbLookupByCq(xContext);
	}
	logInfo(xContext,
		"ChangeRequest: " + getChangeRequest().toString(xContext),
		false);
    }


    /**
     * Set the Branch object from the name
     * 
     * @param xContext Application context.
     * @param aName Branch name
     * @throws IcofException
     */
    protected void setBranchName(EdaContext xContext, String aName)
    throws IcofException {

	if (getBranchName() == null) {
	    branchName = new BranchName(xContext, aName.trim());
	}
	logInfo(xContext, getBranchName().toString(xContext), false);
    }

    
    /**
     * Set the Location object from the name
     * 
     * @param xContext Application context.
     * @param aName Branch name
     * @throws IcofException
     */
    protected void setLocation(EdaContext xContext, String aName)
    throws IcofException {

	if (getLocation() == null) {
	    location = new Location(xContext, aName.trim());
	}
	logInfo(xContext, getLocation().toString(xContext), false);
    }


    /**
     * Set the Component_Db object from the component name
     * 
     * @param xContext Application context.
     * @param aName Component name like ess, edautils, pds ...
     * @throws IcofException
     */
    protected void setComponent(EdaContext xContext, String aName)
    throws IcofException {

	if (getComponent() == null) {
	    component = new Component(xContext, aName.trim());
	    component.dbLookupByName(xContext);
	}
	logInfo(xContext, getComponent().toString(xContext), false);
    }


    /**
     * Set the RelVersion_db object from the tool kit name
     * 
     * @param xContext Application context.
     * @param aToolKit Kit name like 14.1.1, 14.1.2 ...
     * @throws IcofException
     */
    protected void setToolKit(EdaContext xContext, String aName)
    throws IcofException {

	try {
	    if (getToolKit() == null) {
		toolKit = new ToolKit(xContext, aName.trim());
		toolKit.dbLookupByName(xContext);
		toolKit.getToolKit().getStageName().dbLookupById(xContext);
	    }
	    logInfo(xContext, "Tool Kit: " + getToolKit().toString(xContext),
		    isVerbose(xContext));
	}
	catch (IcofException ex) {
	    logInfo(xContext, "ToolKit (" + aName
			      + ") was not found in the database.", false);
	    throw ex;
	}

    }


    /**
     * Return the application invocation
     * 
     * @param argv [] the command line arguments
     * @return the application invocation
     */
    protected String getInvocation(String argv[]) {

	// Construct the application invocation
	StringBuffer invocation = new StringBuffer(appName + " ");

	for (String arg : argv) {
	    invocation.append(arg + " ");
	}
	invocation.append("\n");

	String invocationMsg = "INVOCATION: " + invocation.toString();
	return invocationMsg;
    }


    // Included for readability
    public boolean isVerbose(EdaContext aContext) { return verboseInd; }


    /*
     *  Members
     * @formatter:off
     */
    protected boolean verboseInd;
    protected int returnCode;
    protected String appModeForCalledApps;
    protected String appName;
    protected String appVersion;
    protected User_Db user;
    protected static boolean requestHelp = false;
    protected ToolKit toolKit;
    protected ChangeRequest changeRequest;
    protected Component component;
    protected BranchName branchName;
    protected Location location;
    protected static Logger logger;
    protected static String logName;

    
    /*
     *  Setters
     */
    protected void setVerboseInd(EdaContext aContext, boolean aBoolean) { verboseInd = aBoolean; }
    protected void setReturnCode(EdaContext aContext, int anInt) { returnCode = anInt; }
    protected void setAppModeForCalledApps(EdaContext aContext, String anAppMode) { 
	appModeForCalledApps = anAppMode; }
    protected void setAppName(EdaContext aContext, String aString) { appName = aString; }
    protected void setAppVersion(EdaContext aContext, String aString) {	appVersion = aString; }
    protected void setUser(User_Db aUser) { user = aUser; }
    protected void setToolKit(ToolKit aTk) { toolKit = aTk; }
    protected void setRequestHelp(boolean aFlag) { requestHelp = aFlag; }
    protected void setChangeRequest(ChangeRequest aCr) { changeRequest = aCr; }
    protected void setComponent(Component aComp) { component = aComp; }
    protected void setLogName(String aName) { logName = aName; }
    protected void setBranchName(BranchName aBranchName) { branchName = aBranchName; }
    protected void setLocation(Location aLocation) { location = aLocation; }


    /*
     * Getters
     */
    public boolean getVerboseInd(EdaContext aContext) { return verboseInd; }
    public int getReturnCode(EdaContext aContext) { return returnCode; }
    public String getAppModeForCalledApps(EdaContext aContext) { 
	return appModeForCalledApps; }
    public String getAppName(EdaContext aContext) { return appName; }
    public String getAppVersion(EdaContext aContext) { return appVersion; }
    public User_Db getUser() { return user; }
    public static boolean getRequestHelp() { return requestHelp; }
    public ToolKit getToolKit() { return toolKit; }
    public ChangeRequest getChangeRequest() { return changeRequest; }
    public Component getComponent() { return component; }
    public BranchName getBranchName() { return branchName; }
    public Location getLocation() { return location; }
    public Logger getLogger() { return logger; }
    public static String getLogName() { return logName; }
    // @formatter:on

    
    /**
     * Read command line arguments and verify application invocation
     * 
     * @param argv [] the command line arguments
     * 
     * @return the application context
     * @exception IcofException unable to process arguments
     */
    protected EdaContext initializeApp(String argv[], String className, 
                                       String appName, String logId)
    throws IcofException {

	// Get the userid
	String userid = System.getProperty(Constants.USER_NAME_PROPERTY_TAG);

	// Parse the command line args
	Hashtable<String, String> params = new Hashtable<String, String>();
	Vector<String> singleSwitches = new Vector<String>();
	Vector<String> argSwitches = new Vector<String>();
	createSwitches(singleSwitches, argSwitches);

	// Parse the command line parameters.
	String errors = "";
	String invocationMsg = getInvocation(argv);
	
	System.out.println("Trilok::Debug--> invocationMsg -->" + invocationMsg);
	//INVOCATION: pkg.create -full -t 15.1.1 -c wss -ctk -y
	// Trilok changes - Parse invocationMsg and get the toolkit Name and Component Name
	String toolkitName = "" ;
	String compName = "" ;
	
	boolean pkgLogCreate = false ;
	System.out.println("Trilok::Debug--> appName -->" + appName);
	if(appName != null)
	{
		if(appName.equalsIgnoreCase("pkg.create") || appName.equalsIgnoreCase("pkg.install") || 
				appName.equalsIgnoreCase("pkg.installPrep")){
			pkgLogCreate = true ;
			String argus[] = invocationMsg.split(" ") ;
			for(int i=0; i<argus.length; i++){
				if(argus[i].equalsIgnoreCase("-t")){
					toolkitName = argus[i+1] ;
				}else if (argus[i].equalsIgnoreCase("-c")){
					compName = argus[i+1] ;
				}
			}
		}
	}

	
	
	// End changes
	
	EdaContext xContext = parseAndConnectToDb(argv, params, singleSwitches,
						  argSwitches, userid, errors,
						  invocationMsg,
						  TkConstants.EDA_PROD,
						  appName);

	if (null == xContext) {
	    return null;
	}
	
	if (className != null){
		// Trilok changes - if appName is Pkg.create, concat appName, tkName, compName with #
		if(pkgLogCreate == true){
			System.out.println("Trilok::Debug--> concat string is  -->" + appName+"#"+toolkitName+"#"+compName);
			initializeLogging(className, appName+"#"+toolkitName+"#"+compName, logId);
		}else{
			initializeLogging(className, appName, logId);
		}
		
	}
	    

	// Set the user object.
	setUser(xContext, userid);

	errors = readParams(params, errors, xContext);

	logAndDisplay(params, errors, invocationMsg, xContext.getAppMode(),
		      xContext, appName, getAppVersion());

	validateAndProceed(xContext);
	return xContext;

    }


    protected void validateAndProceed(EdaContext xContext)
    throws IcofException {

	if (!validateUserRole(xContext)) {
	    StringBuffer sbErrorMessage = new StringBuffer(
							   "You are not authorized to approve  "
							   + getAppName());
	    sbErrorMessage.append("\n");
	    sbErrorMessage.append("Only members of the EDA ");

	    TkUserRoleConstants[] roles = getAuthorisedRoles(xContext);

	    for (TkUserRoleConstants role : roles) {
		sbErrorMessage.append(",");
		sbErrorMessage.append(role.getUserRole() + " ");
	    }
	    sbErrorMessage.append(" are authorised for the same");

	    throw new IcofException(sbErrorMessage.toString(),
				    IcofException.SEVERE);

	}
    }


    /**
     * Display error messages to the user
     * 
     * @param msg Text to display
     * @param isError True if this is an error message
     */
    protected void alert(String msg, boolean isError) {

	String error = ">>>>>>>>>>>>>>>>>>>>  ERROR  <<<<<<<<<<<<<<<<<<<<";
	String line = "-------------------------------------------------";
	String extra = line + "\n" + "                  COMMIT FAILED" + "\n"
		       + line;
	String top = line;
	String bottom = line;
	if (isError) {
	    top = error + "\n" + extra;
	    bottom = error;
	}

	if ((msg != null) && (msg.length() > 0))
	    System.out.println(top + "\n" + msg + "\n" + bottom + "\n");
	else
	    System.out.println(top + "\n" + bottom + "\n");

    }


    /**
     * Display this application's usage and invocation
     */
    protected abstract void showUsage();
    protected abstract void createSwitches(Vector<String> singleSwitches,
					   Vector<String> argSwitches);
    protected abstract String readParams(Hashtable<String, String> params,
					 String errors, EdaContext xContext)
					 throws IcofException;
    protected abstract String getAppName();
    protected abstract String getAppVersion();


    /**
     * Do the initial check of command line parameters and return any errors
     * found
     * 
     * @param argv [] the command line arguments
     * @param syntax the expected switches
     * @param params will be populated with the switches and arguments
     * 
     * @return error messages
     * @exception IcofException unable to process arguments
     */
    protected String parseCmdLine(String argv[], String syntax,
				  Hashtable<String, String> params)
    throws IcofException {

	// Parse input parameters
	String msg = IcofSystemUtil.parseCmdLine(argv, syntax, params);
	String errors = initialParmCheck(params, msg, argv);
	return errors;
    }


    /**
     * Do the initial check of command line parameters and return any errors
     * found
     * 
     * @param argv [] the command line arguments
     * @param noArgSwitches Vector containing the switches for the application
     *            that do not require an argument
     * @param argSwitches Vector containing the switches for the application
     *            that do require an argument
     * @param params will be populated with the switches and arguments
     * 
     * @return error messages
     * @exception IcofException unable to process arguments
     */
    protected String parseCmdLine(String argv[], Vector<String> noArgSwitches,
				  Vector<String> argSwitches,
				  Hashtable<String, String> params)
    throws IcofException {

	// Parse input parameters
	String msg = IcofSystemUtil.parseCmdLine(argv, noArgSwitches,
						 argSwitches, params);
	String errors = initialParmCheck(params, msg, argv);
	return errors;
    }


    /**
     * Do the initial check of command line parameters and return any errors
     * found
     * 
     * @param params The switches and corresponding arguments
     * @param msg the message that was returned by parseCmdLine
     * @param argv The original command line parameters
     * 
     * @return error messages
     * @exception IcofException unable to process arguments
     */
    private String initialParmCheck(Hashtable<String, String> params,
				    String msg, String[] argv) {

	String errors = "";

	// Incorrect usage if no parameters returned or msg contains some
	// error messages from the
	// parseCmdLine method indicating incorrect command line syntax.
	if (params.isEmpty() || ((msg != null) && (!msg.equals("")))) {

	    // Add application invocation if present
	    if (argv.length == 0) {
		errors += " * No parameters were specified\n";
	    }

	    // Add error messages from parseCmdLine method
	    if (msg != null) {
		errors += msg;

	    }

	}

	return errors;

    }


    /**
     * Log the message and echo it to the screen, if the verbose indicator is
     * true
     * 
     * @param aContext the application context (contains log)
     * @param msg the message to log
     * @param level the level of the message, from the constants defined in
     *            SessionLog
     */
    protected void logInfo(EdaContext aContext, String msg, boolean bEcho) {

	if (logger != null) {
	    if (bEcho)
		logger.info(msg);
	    else
		logger.fine(msg);
	    return;
	}
	
	aContext.getSessionLog().log(SessionLog.INFO, msg);
	if (isVerbose(aContext) || bEcho) {
	    System.out.println(msg);
	}

    }
    
    
    /**
     * Set up the log handlers if needed
     * 
     * @param className  Class name to identify log file
     * @param appName    Used in log file name (<appName>.<timestamp>.log)
     * @throws IcofException 
     */
    protected static String initializeLogging(String className, String appName) 
                                              throws IcofException {

	return initializeLogging(className, appName, "");
	
    }
    
    
    /**
     * Set up the log handlers if needed
     * 
     * @param className  Class name to identify log file
     * @param appName    Used in log file name (<appName>.<timestamp>.log)
     * @param logFileId  Used in log file name (<appName>_<id>.<timestamp>.log)
     * @throws IcofException 
     */
    protected static String initializeLogging(String className, String appName, 
                                              String logFileIdentifier) 
                                              throws IcofException {

	logger = Logger.getLogger(className);

	// LOGGING
	try {
	    LogManager.getLogManager().readConfiguration(new FileInputStream(PROP_FILE));
	}
	catch (SecurityException | FileNotFoundException e) {
	    throw new IcofException("TkAppBase", "initializeLogging()", 
	                            IcofException.SEVERE, 
	                            "Unable to read LOGGING properties file", 
	                            "File: " + PROP_FILE);
	}
	catch (IOException e) {
	    throw new IcofException("TkAppBase", "initializeLogging()", 
	                            IcofException.SEVERE, 
	                            "IO .. unable to read LOGGING properties file", 
	                            "File: " + PROP_FILE);
	}

	// Trilok changes - if Pkg.create, split the appName and fetch the tkName and component Name
	// LOGGING - set File message handler
	String apiName ="";
	String tkName = "" ; 
	String cName = "" ;
	boolean pkgLogDir = false ;
	File dir = null ;
	System.out.println("Trilok::Debug--> initializeLogging appName is -->" + appName);
	if(appName != null && (appName.contains("pkg.create") || 
			appName.contains("pkg.install")  || appName.contains("pkg.installPrep"))){
		String splArrp[] = appName.split("#") ;
		apiName = splArrp[0] ;
		tkName = splArrp[1] ;
		if(splArrp.length > 2)
			cName = splArrp[2] ;
		pkgLogDir = true ;
		System.out.println("appName selected is-->" + appName);
		if(appName.contains("pkg.installPrep")){
			// /afs/eda/data/edainfra/logs/<TOOLKIT>/install.prep/<here patch name>
			int counter = 0;
			for( int i=0; i<tkName.length(); i++ ) {
			    if( tkName.charAt(i) == '.' ) {
			        counter++;
			    } 
			}
			if(counter > 2){
				String tn = "" ;
				tn = tkName.substring(0,tkName.lastIndexOf(".")) ;
				dir = new File(PKG_CREATE_LOG_DIR+File.separator+tn+File.separator+apiName+File.separator+tkName) ;
			}else{
				dir = new File(PKG_CREATE_LOG_DIR+File.separator+tkName+File.separator+apiName) ;
			}
			
			//dir = new File(PKG_CREATE_LOG_DIR+File.separator+tkName+File.separator+apiName+File.separator) ; 
		}else{
			dir = new File(PKG_CREATE_LOG_DIR+File.separator+tkName+File.separator+apiName) ; ///afs/eda/data/edainfra/logs/15.1.1/pkg.create
		}
		System.out.println("Trilok::Debug--> initializeLogging log Dir is -->" + dir);	
	}else{
		dir = new File(LOG_DIR);
	}
	
	
	if (! dir.exists())
	    dir.mkdirs();

	SimpleDateFormat sdf;
	sdf = new SimpleDateFormat(IcofDateUtil.ARCHIVE_FILE_DATE_FORMAT);

	// Trilok changes Starts
	if(pkgLogDir){
		logName = dir.getAbsolutePath() + File.separator + apiName;
	}else{
		logName = dir.getAbsolutePath() + File.separator + appName;
	}
	
	// end
	
	if (logFileIdentifier != null && ! logFileIdentifier.isEmpty())
	    logName += "_" + logFileIdentifier;
	
	// Trilok changes Starts
	if(pkgLogDir){
		if(cName != null && cName.length() > 0){
			logName +="_"+cName+"_" + sdf.format(new Date()) + LOG_EXT;
		}else{
			logName +="_" + sdf.format(new Date()) + LOG_EXT;
		}
	}else{
		logName += "_" + sdf.format(new Date()) + LOG_EXT;
	}

	// end
	
	Handler fileHandler;
	try {
	    System.out.println("Initializing log file .. " + logName);
	    fileHandler = new FileHandler(logName);
	}
	catch (SecurityException | IOException e) {
	    throw new IcofException("TkAppBase", "initializeLogging()", 
	                            IcofException.SEVERE, 
	                            "Security .. unable to create log file\n" +  
	                            "Log: " + logName,
	                            e.getMessage());
	}

	logger.addHandler(fileHandler);

	return logName;

    }

    
    /**
     * Write the contents of the input vector to the log file. If requested,
     * override the verboseIndicator and do not print vector contents to the
     * screen. This method is useful when an application invokes another
     * application and wishes to log the output of that application, without
     * displaying it on screen a second time.
     * 
     * @param aContext application context
     * @param aVector the vector containing the data to write to the log file.
     * @param level the level of the message, from the constants defined in
     *            SessionLog
     * @param overrideVerbose true, to ignore the verbose indicator and not
     *            display on screen; false, to use the verbose indicator to
     *            determine whether to display vector contents on screen
     * 
     */
    protected void logVectorContents(EdaContext aContext,
				     Vector<String> aVector, String level,
				     boolean overrideVerbose) {

	boolean origVerboseInd = getVerboseInd(aContext);

	for (int i = 0; i < aVector.size(); i++) {
	    String msg = aVector.elementAt(i);
	    logInfo(aContext, msg, verboseInd);
	}

	setVerboseInd(aContext, origVerboseInd);

    }


    /**
     * Connect to the database, if not already connected
     * 
     * @param aContext Application context
     * 
     * @throws IcofException
     * 
     */
    protected void connectToDB(EdaContext aContext)
    throws IcofException {

	if (aContext != null) {
	    if (aContext.getConnection() == null) {
		EdaContext.getDB2DriverInstance();
		if (aContext.getAppMode().equals(TkConstants.EDA_DEV) ||
		    aContext.getAppMode().equals(TkConstants.EDA_TEST) ||
		    aContext.getAppMode().equals(TkConstants.EDA_PROD)) {
		    aContext.connectToDB(EdaContext.DB_ACCESS_ID_G,
		                         EdaContext.DB_ACCESS_PW_G);
		}
		else if (aContext.getAppMode().equals(TkConstants.EDA_GDEV) ||
			 aContext.getAppMode().equals(TkConstants.EDA_GTEST) ||
			 aContext.getAppMode().equals(TkConstants.EDA_GPROD)) {
		    aContext.connectToDB(EdaContext.DB_ACCESS_ID_G,
					 EdaContext.DB_ACCESS_PW_G);
		}

	    }

	}

    }


    /**
     * Disconnect from the database, if connected
     * 
     * @param anEdaContext
     * 
     * @throws IcofException
     * 
     */
    protected void disconnectFromDB(EdaContext aContext)
    throws IcofException {

	aContext.disconnectFromDB();
    }


    public abstract void process(EdaContext xContext)
    throws IcofException, Exception;


    protected void wrongApplicationUsage(String errors, String appName,
					 EdaContext xContext)
    throws IcofException {

	// Throw an exception if any application usage was incorrect

	// Add the application usage
	showUsage();
	setReturnCode(xContext, INVALID_PARMS);

	IcofException ie = new IcofException(appName, "initializeApp()",
					     IcofException.SEVERE,
					     "Invalid application invocation or "
					     + "verification errors.", "\n"
								       + errors);
	xContext.getSessionLog().log(ie);
	xContext.getSessionLog().log(SessionLog.INFO,
				     "Program Return Code = "
				     + getReturnCode(xContext));
	throw ie;
    }


    protected void logHeaderAndInvocationMsg(String invocationMsg,
					     EdaContext xContext,
					     String appName, String appVersion) {

	// Log the Application invocation and application version
	String header = "\n";
	header += "---------------------------------------------------------------\n";
	header += "Application: " + appName + " " + appVersion + "\n";
	try {
	    header += "Host: " + InetAddress.getLocalHost().getHostName() + "\n";
	}
	catch (UnknownHostException e) {
	    e.printStackTrace();
	}
	header += "---------------------------------------------------------------\n";

	logInfo(xContext, header, verboseInd);
	logInfo(xContext, invocationMsg, verboseInd);

    }


    protected EdaContext parseAndConnectToDb(String[] argv,
					     Hashtable<String, String> params,
					     Vector<String> singleSwitches,
					     Vector<String> argSwitches,
					     String userid, String errors,
					     String invocationMsg,
					     String dbMode, String appName)
    throws IcofException {

	// Parse the command line parameters.
	String msg = IcofSystemUtil.parseCmdLine(argv, singleSwitches,
						 argSwitches, params);

	// See if help was requested
	if (params.containsKey("-h")) {
	    requestHelp = true;
	    showUsage();
	    return null;
	}

	invocationMsg = getInvocation(argv);

	createErrorMsg(argv, params, errors, invocationMsg, msg);
	EdaContext xContext = createConnection(params, userid, dbMode, 
	                                       appName, false);

	return xContext;

    }


    protected EdaContext createConnection(Hashtable<String, String> params,
					  String userid, String dbMode,
					  String appName, boolean bEnableLog)
    throws IcofException {

	// Read the application mode parameter first because it is needed in
	// order to create the log file

	if (params.containsKey("-db")) {
	    dbMode = params.get("-db");
	}

	// Create the SessionLog and AppContext, now that the appMode is known.
	String intranetId = userid + "@us.ibm.com";
	EdaContext xContext;
	if (bEnableLog) {
	    SessionLog xLog = new SessionLog(appName, userid, dbMode, 
	                                     true, SessionLog.INFO, "", 
	                                     TkConstants.createEtreeLogDir(dbMode),
	                                     TkConstants.createLogName(appName, userid));
	    
	    xContext = new EdaContext(intranetId, userid, dbMode, xLog);
	}
	else {
	    xContext = new EdaContext(intranetId, userid, appName,
					     dbMode, false);
	}
	

	// Configure the context for database connections.
	EdaContext.getDB2DriverInstance();
	connectToDB(xContext);
	return xContext;
    }


    protected void createErrorMsg(String[] argv,
				  Hashtable<String, String> params,
				  String errors, String invocationMsg,
				  String msg) {

	// Incorrect usage if no parameters returned or msg contains some
	// error messages from the parseCmdLine method indicating incorrect
	// command line syntax.
	if (params.isEmpty() || ((msg != null) && (!msg.equals("")))) {

	    // Add application invocation if present
	    if (argv.length > 0) {

		errors += "\n";
		errors += invocationMsg;
	    }
	    else {
		errors += "No parameters were specified\n";
	    }

	    // Add error messages from parseCmdLine method
	    if (msg != null) {

		errors += "\n";
		errors += "ERRORS\n";
		errors += "-----------------------------------\n";
		errors += msg;
	    }
	}

	if ((params.size() == 1) && (params.containsKey("-db"))) {
	    errors += " * Additional parameters (besides -db are required\n";
	}
    }


    public static void handleExceptionInMain(Exception e, EdaContext aContext) {

	if (aContext != null) {
	    // Log the exception
	    aContext.getSessionLog().log(e.getMessage(), SessionLog.FATAL);
	}

	handleExceptionInMain(e);
    }


    public static void handleExceptionInMain(Exception e) {

	System.out.println("");
	System.out.println("====================================================");
	System.out.println("... Caught ERROR in main() ...");
	System.out.println("----------------------------------------------------");
	System.out.println(" - Message -");
	System.out.println(e.toString());
	System.out.println("----------------------------------------------------");
	System.out.println(" -- Debug --");
	e.printStackTrace();
	System.out.println("====================================================");
	System.out.println("");

    }


    public static void handleInFinallyBlock(TkAppBase myApp) {

	// Notify user where to find log file 
	if (logger != null) {
	    System.err.println("\nLog: " + getLogName() + "\n");
	    for(Handler h : logger.getHandlers())
	        h.close();
	}
	
	
	// Get the application return code
	int rc = FAILURE;
	if (myApp != null) {
	    rc = myApp.getReturnCode(null);
	}

	// System.out.println("Program Return Code = " + rc);
	System.exit(rc);

    }


    /**
     * Use if no logging is needed
     * 
     * @param myApp Object being executed
     * @param argv  Collection of command line arguments
     */
    public static void start(TkAppBase myApp, String argv[])
    throws IcofException, Exception {

	// Read and verify input parameters and get a database connection.
	EdaContext aContext = myApp.initializeApp(argv, null, null, null);
	if (getRequestHelp()) {
	    System.exit(SUCCESS);
	}
	myApp.process(aContext);
    }
    

    /**
     * Use if logging is needed
     * 
     * @param myApp     Object being executed
     * @param argv      Collection of command line arguments
     * @param className Name of class owning the logger (<class>.class.getName())
     * @param appName   Application name used in the log file name
     */
    public static void start(TkAppBase myApp, String argv[], 
                             String className, String appName)
    throws IcofException, Exception {

	// Read and verify input parameters and get a database connection.
	EdaContext aContext = myApp.initializeApp(argv, className, appName, "");
	if (getRequestHelp()) {
	    System.exit(SUCCESS);
	}

	myApp.process(aContext);

    }
    
    /**
     * Use if logging is needed
     * 
     * @param myApp     Object being executed
     * @param argv      Collection of command line arguments
     * @param className Name of class owning the logger (<class>.class.getName())
     * @param appName    Used in log file name (<appName>.<timestamp>.log)
     * @param logFileId  Used in log file name (<appName>_<id>.<timestamp>.log)
     *      */
    public static void start(TkAppBase myApp, String argv[], 
                             String className, String appName, 
                             String logFileIdentifier)
    throws IcofException, Exception {

	// Read and verify input parameters and get a database connection.
	EdaContext aContext = myApp.initializeApp(argv, className, appName, 
	                                          logFileIdentifier);
	if (getRequestHelp()) {
	    System.exit(SUCCESS);
	}

	myApp.process(aContext);

    }

    
    /**
     * Use if Context is already instantiated
     * 
     * @param myApp     Object being executed
     * @param aContex   Application context
     * @param argv      Collection of command line arguments
     */
    public static void start(TkAppBase myApp, EdaContext aContext,
			     String argv[])
    throws IcofException, Exception {

	// Read and verify input parameters and get a database connection.
	aContext = myApp.initializeApp(argv, null, null, null);
	if (getRequestHelp()) {
	    System.exit(SUCCESS);
	}
	myApp.process(aContext);
	
    }


    public void commitToDBAndSetReturncode(EdaContext xContext, String appName,
					   int returnCode)
    throws IcofException {

	commitToDB(xContext, appName);
	setReturnCode(xContext, returnCode);

    }


    public void commitToDB(EdaContext xContext, String appName)
    throws IcofException {

	try {
	    xContext.getConnection().commit();
	}
	catch (SQLException se) {
	    throw new IcofException(appName, "proces()", IcofException.SEVERE,
				    "Unable to commit DB transactions.\n",
				    se.getMessage());
	}
	finally {
	    // Disconnect from the database
	    disconnectFromDB(xContext);
	}
    }


    public void rollBackDBAndSetReturncode(EdaContext xContext, String appName,
					   int returnCode)
    throws IcofException {

	rollBackDB(xContext, appName);
	setReturnCode(xContext, returnCode);

    }


    public void rollBackDB(EdaContext xContext, String appName)
    throws IcofException {

	try {
	    xContext.getConnection().rollback();
	}
	catch (SQLException se) {
	    throw new IcofException(appName, "proces()", IcofException.SEVERE,
				    "Unable to rollback DB transactions.\n",
				    se.getMessage());
	}
	finally {
	    // Disconnect from the database
	    disconnectFromDB(xContext);
	}
    }


    protected void logAndDisplay(Hashtable<String, String> params,
				 String errors, String invocationMsg,
				 String dbMode, EdaContext xContext,
				 String appName, String appVersion)
    throws IcofException {

	setVerbose(params, xContext);
	// Log the Application invocation and application version
	logHeaderAndInvocationMsg(invocationMsg, xContext, appName, appVersion);

	// Display parameters if no errors
	if (errors.equals("")) {
	    displayParameters(dbMode, xContext);
	}
	else {
	    // Throw an exception if any application usage was incorrect
	    wrongApplicationUsage(errors, appName, xContext);
	}
    }


    protected void setVerbose(Hashtable<String, String> params,
			      EdaContext xContext) {

	// Read the verbose indicator parameter next, because it is needed for
	// logging
	setVerboseInd(xContext, false);
	if (params.containsKey("-y")) {
	    setVerboseInd(xContext, true);
	}

    }

    
    /**
     * Set up the log handlers if needed
     * 
     * @param className  Class name to identify log file
     * @param appName    Used in log file name (<appName>.<timestamp>.log)
     * @param logFileId  Used in log file name (<appName>_<id>.<timestamp>.log)
     * @throws IcofException 
     */
//    protected static String initializeLogging(String className, String appName, 
//                                           String logFileIdentifier) 
//    throws IcofException {
//
//	 logger = Logger.getLogger(className);
//	
//	// LOGGING
//	try {
//	    LogManager.getLogManager().readConfiguration(new FileInputStream(PROP_FILE));
//	}
//	catch (SecurityException e1) {
//	    throw new IcofException("TkAppBase", "initializeLogging()", 
//	                            IcofException.SEVERE, 
//	                            "Security .. unable to read properties file", 
//	                            "File: " + PROP_FILE);
//	}
//	catch (FileNotFoundException e1) {
//	    throw new IcofException("TkAppBase", "initializeLogging()", 
//	                            IcofException.SEVERE, 
//	                            "Missing .. unable to read properties file", 
//	                            "File: " + PROP_FILE);
//	}
//	catch (IOException e1) {
//	    throw new IcofException("TkAppBase", "initializeLogging()", 
//	                            IcofException.SEVERE, 
//	                            "IO .. unable to read properties file", 
//	                            "File: " + PROP_FILE);
//	}
//
//	// LOGGING - set File message handler
//	File dir = new File(LOG_DIR);
//	if (! dir.exists())
//	    dir.mkdirs();
//	
//	SimpleDateFormat sdf;
//	sdf = new SimpleDateFormat(IcofDateUtil.ARCHIVE_FILE_DATE_FORMAT);
//
//	logName = dir.getAbsolutePath() + File.separator + appName;
//	if (logFileIdentifier != null && ! logFileIdentifier.isEmpty())
//	    logName += "_" + logFileIdentifier;
//	logName += "." + sdf.format(new Date()) + LOG_EXT;
//		
//	Handler fileHandler;
//	try {
//	    System.out.println("debug log: " + logName);
//	    fileHandler = new FileHandler(logName);
//	}
//	catch (SecurityException e) {
//	    throw new IcofException("TkAppBase", "initializeLogging()", 
//	                            IcofException.SEVERE, 
//	                            "Security .. unable to create log file\n" +  
//	                            "Log: " + logName,
//	                            e.getMessage());
//	}
//	catch (IOException e) {
//	    e.printStackTrace();
//	    throw new IcofException("TkAppBase", "initializeLogging()", 
//	                            IcofException.SEVERE, 
//	                            "IO .. unable to create log file\n", 
//	                            "Log: " + logName);
//	}
//	
//	logger.addHandler(fileHandler);
//
//	return logName;
//	
//    }
//
//    /**
//     * Set up the log handlers if needed
//     *
//     * @param className  Class name to identify log file
//     * @param appName    Used in log file name (<appName>.<timestamp>.log)
//     * @throws IcofException 
//     */
//    protected static String initializeLogging(String className, String appName) 
//    throws IcofException {
//	
//	return initializeLogging(className, appName, "");
//	
//    }

    
    /**
     * Verify the user is authorized to perform this action.
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private boolean validateUserRole(EdaContext xContext)
    throws IcofException {

	// // Users for the authorized role
	// TkUserRoleConstants[] roles = getAuthorisedRoles(xContext);
	// try {
	// for (TkUserRoleConstants role : roles) {
	// EdaTkRole_Db authRole = new EdaTkRole_Db(role.getUserRole(), "");
	// authRole.dbLookupByName(xContext);
	//
	// EdaTkRole_User_Db roleUser = new EdaTkRole_User_Db(getUser(),
	// authRole);
	// roleUser.dbLookupByAll(xContext);
	return true;
	// }
	//
	// } catch (IcofException trap) {
	// System.out.println(trap.printStackTraceAsString());
	// }

	// return false;
    }


    protected abstract void displayParameters(String dbMode, EdaContext xContext);


    protected abstract TkUserRoleConstants[] getAuthorisedRoles(EdaContext xContext);

}
