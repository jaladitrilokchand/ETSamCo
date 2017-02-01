/**
 * <pre>
 * 
 * =============================================================================
 * 
 *  Copyright: (C) IBM Corporation 2001 - 2011 -- IBM Internal Use Only
 * 
 * =============================================================================
 * 
 *     FILE: AppContext.java
 * 
 *  CREATOR: Gregg Stadtlander (stadtlag)
 *     DEPT: 5ZIA
 *     DATE: 03/12/2002
 * 
 * -PURPOSE---------------------------------------------------------------------
 *  Manage AFS userid, intranet userid and DefaultContext(db connection) objects
 * -----------------------------------------------------------------------------
 * 
 * 
 * -CHANGE LOG------------------------------------------------------------------
 *  03/12/2002 GS  Initial coding.
 *  05/27/2004 KKW Added code to look up the intranet userid in the Icof_User
 *                 table to get the afs userid when it is not passed on the
 *                 constructor.
 *  03/04/2005 KKW Added constructor that does not require a db connection, in
 *                 order to be able to use certain classes that require an
 *                 AppContext to be passed in.  Use this constructor with care,
 *                 and use only when dealing strictly with file input and output
 *                 (ie. no database access).
 *  05/25/2005 KKW  Added &quot;implements Serializable&quot;.
 *  12/15/2005 KKW  Modified due to splitting of Constants.java into several
 *                  *Util classes.  Added Javadoc.
 *  08/10/2006 RAM Added SessionLog capability. Standardized indentation.
 *  08/17/2006 RAM Added appMode parameter to all constructors and as a member.
 *  01/02/2007 KKW Added jdbc query to lookup the intranet id to get the afs
 *                 userid
 *  02/23/2007 KKW Added highLvlQualifier, which is set based on the
 *                 operating system that the app is running on
 *  03/02/2007 KKW Added dbName, with getters and setters, and connectToDB
 *  05/31/2007 KKW  Ensured the result of all trim() functions is assigned
 *                  to a variable, as appropriate
 *  07/24/2007 KKW  Changed constructors that don't use the database to 
 *                  set the dbName to the empty string, instead of reading
 *                  the db.names file to set it.  This is because ICC does
 *                  not have that file on their system.
 *  08/30/2007 RAM  Removed unused constructor AppContext(DefaultContext,String,
 *            		String,String) and added 
 *           		AppContext(String,String,String,SessionLog)
 *  10/11/2007 RAM  Made setSessionLog() public
 *  01/22/2008 KKW  Added fields, getters, and setters for dbServerName and
 *                  dbPort.  Also changed connectToDB method to use db2
 *                  type 4 driver. 
 *  05/08/2008 KKW  Removed IcofUtil import to prevent circular reference that
 *                  was caused when a constructor was added to TechRelease
 *                  that referenced Technology and LibRelease. 
 *  05/09/2008 KKW  Added new appMode (JUNIT) and updated validateAppMode to
 *                  recognize it.    
 *  08/04/2008 GFS  Updated setDBName() to point the the aes/sys_admin directory.
 *                  Updated validateAppMode().                
 *  11/20/2008 GFS  Added setOsName() and getOsName() methods.
 *  02/05/2009 KKW  Overloaded connectToDB(AppContext) method to allow user
 *                  to specify an appMode to be used for determining the 
 *                  database to connect to, instead of using the appMode
 *                  stored in AppContext.  The purpose of this new method is
 *                  to allow the Junit testing to work without changes against
 *                  either dkdev or dkprod databases. 
 *  02/18/2009 KKW  Removed throws exception clause from asString method  
 *  03/26/2009 AS   Added createAppContext public static method.  
 *  10/05/2009 KKW  Added getDBUrl method and disconnectFromDB method.  
 *  04/09/2010 KKW  Added support for ICCTEST appMode     
 *  06/10/2010 KKW  Added maxAfsFileSize, with getters and setters.
 *  08/11/2010 KKW  Updated disconnectFromDB() to ensure the connections aren't
 *                  already closed before closing them.
 *  04/25/2011 KKW  Added a dedicated db connection for space mgmt.                                
 * =============================================================================
 *  
 * </pre>
 */

package com.ibm.stg.eda.component.mom;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import sqlj.runtime.ref.DefaultContext;

import com.ibm.db2.jcc.DB2Driver;
import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofSystemUtil;
import com.ibm.stg.iipmds.common.SessionLog;
import com.ibm.stg.iipmds.icof.component.util.IcofEmailUtil;

//=============================================================================

public class AppContext implements Serializable {

    // The application mode to use in constructing a directory name.
    private String appMode;

    // The SessionLog object used for writing messages to a "log" file.
    private SessionLog sessionLog;

    // The intranet userid that is running the environment (the
    // ApplicationContext)
    private String intranetUserid;

    // The afs user id required for access to directories... etc.
    private String afsUserid;

    // The database connection
    private Connection connection;

    // The database connection context
    private DefaultContext ctx;

    // The database connection for SpaceMgmt
    private Connection spaceMgrConnection;

    // The database connection context for SpaceMgmt
    private DefaultContext spaceMgrCtx;

    // The high level qualifier used to create directory names
    private String highLvlQualifier;

    // The name of the database, based on the appMode
    private String dbName;

    // The name of the database server, based on the appMode
    private String dbServerName;

    // The port number of the database, based on the appMode
    private String dbPort;

    // The os name for the current platform
    private String osName;

    // The maximum afs file size for the current machine we are running on
    private long maxAfsFileSize;

    /** Class name */
    private static final String APP_NAME = "AppContext";

    // Constant used to describe an intranet user id that is invalid.
    private static final String BAD_INTRANET_USERID = "Null, empty or invalid intranet userid";

    public static final String DB2_DRIVER = "com.ibm.db2.jcc.DB2Driver";

    // ---------------------------------------------------------------------------
    /**
     * Constructor - use when a database connection is not needed or will be
     * created separately.
     * 
     * @param anIntranetUserid
     *            intranetUserid of the person for which this context is being
     *            created
     * @param anAfsUserid
     *            afsUserid of the person for which this context is being
     *            created
     * @param appName
     *            The Name of the application for this appContext
     * @param appMode
     *            The app mode (ICCPROD, PROD, DEV, TEST)
     * @exception IcofException
     *                Unable to create object
     */
    // ---------------------------------------------------------------------------
    public AppContext(String anIntranetUserid, String anAfsUserid,
                      String appName, String appMode) throws IcofException {
        this(anIntranetUserid, anAfsUserid, appName, appMode, true);
    }

    // ---------------------------------------------------------------------------
    /**
     * Constructor - use when a database connection is not needed or will be
     * created separately.
     * 
     * @param anIntranetUserid
     *            intranetUserid of the person for which this context is being
     *            created
     * @param anAfsUserid
     *            afsUserid of the person for which this context is being
     *            created
     * @param appName
     *            The Name of the application for this appContext
     * @param appMode
     *            The app mode (ICCPROD, PROD, DEV, TEST)
     * @param append
     *            True if logging to fileAppender is desired, false if no
     *            logging is desired.
     * @exception IcofException
     *                Unable to create object
     */
    // ---------------------------------------------------------------------------
    public AppContext(String anIntranetUserid, String anAfsUserid,
                      String appName, String appMode, boolean append)
    throws IcofException {

        setAppMode(appMode);
        SessionLog log;
        if (append == true) {
            log = new SessionLog(appName, anAfsUserid, getAppMode(), true);
        } else {
            log = new SessionLog(appName, anAfsUserid, getAppMode(), false);
            log.setLevel(SessionLog.OFF);
        }
        setSessionLog(log);
        setHighLvlQualifier();
        setUserids(anIntranetUserid, anAfsUserid);
        setDBName("");
        setOsName();

    }

    // ---------------------------------------------------------------------------
    /**
     * Constructor - use if Intranet and AFS userid are known. This one will set
     * the logging to OFF and will set logging to go to console. Use this
     * constructor for webApplications that don't need logging.
     * 
     * @param aCtx
     *            context that has been created for this user's database
     *            connection
     * @param anIntranetUserid
     *            intranet userid of the person for which this context is being
     *            created
     * @param anAfsUserid
     *            afsUserid of the person for which this context is being
     *            created
     * @param appName
     *            the name of the application for this context
     * @param appMode
     *            The app mode (ICCPROD, PROD, DEV, TEST)
     * @exception IcofException
     *                Unable to create object
     */
    // ---------------------------------------------------------------------------
    public AppContext(DefaultContext aCtx, String anIntranetUserid,
                      String anAfsUserid, String appName, String appMode)
    throws IcofException {

        this(aCtx, anIntranetUserid, anAfsUserid, appName, appMode, false,
             SessionLog.OFF, SessionLog.DEFAULT_CONVERSION_PATTERN, "");

    }

    // -----------------------------------------------------------------------------
    /**
     * 
     * Constructor - use if Intranet and AFS userid are known
     * 
     * @param aCtx
     *            context that has been created for this user's database
     *            connection
     * @param anIntranetUserid
     *            intranet userid of the person for which this context is being
     *            created
     * @param anAfsUserid
     *            afsUserid of the person for which this context is being
     *            created
     * @param appName
     *            The application constructing this object.
     * @param appMode
     *            The app mode (ICCPROD, PROD, DEV, TEST)
     * @param fileAppender
     *            true, to log to a file; false, to log to console.
     * @param minimumLevel
     *            The lowest level that the logger will log. Use a constant from
     *            SessionLog.java.
     * @param conversionPattern
     *            The pattern in which to log statements. Two are offered by
     *            SessionLog. The user can specify their own.
     * @param path
     *            The path where the SessionLog file will be located. Insert a
     *            blank String or use a different constructor if you wish to
     *            base the path on the appMode.
     * 
     * @throws IcofException
     *             Unable to create object. Also, if the dir cannot be validated
     *             or created, if the PrintWriter contains errors, if the named
     *             file exists but is a directory rather than a regular file,
     *             does not exist but cannot be created, or cannot be opened for
     *             any other reason when creating the SessionLog object.
     * 
     */
    // -----------------------------------------------------------------------------
    public AppContext(DefaultContext aCtx, String anIntranetUserid,
                      String anAfsUserid, String appName, String appMode,
                      boolean fileAppender, String minimumLevel,
                      String conversionPattern, String path) throws IcofException {

        setAppMode(appMode);
        setSessionLog(appName, getAfsUserid(), getAppMode(), minimumLevel,
                      conversionPattern, path, fileAppender);
        setHighLvlQualifier();
        setDBName();
        setDefaultContext(aCtx);
        setUserids(anIntranetUserid, anAfsUserid);
        setOsName();

    }

    /**
     * Construct an AppContext object using a predefined SessionLog object, a
     * known intranet userid and afs userid, and appMode. This constructor is
     * best used for defining a specific SessionLog path without being able to
     * pass a DefaultContext.
     * 
     * @param anIntranetUserid
     *            intranet userid of the person for which this context is being
     *            created
     * @param anAfsUserid
     *            afsUserid of the person for which this context is being
     *            created
     * @param appName
     *            The application constructing this object.
     * @param appMode
     *            The app mode (ICCPROD, PROD, DEV, TEST)
     * @param aSessionLog
     *            A constructed SessionLog object.
     * @throws IcofException
     *             Unable to create object
     */
    public AppContext(String anIntranetUserid, String anAfsUserid,
                      String appMode, SessionLog aSessionLog) throws IcofException {

        setAppMode(appMode);
        setHighLvlQualifier();
        setDBName();
        setUserids(anIntranetUserid, anAfsUserid);
        this.setSessionLog(aSessionLog);
        setOsName();
    }

    /**
     * Creates an application context object.
     * 
     * @param appName
     *            Application name.
     * @param appMode
     *            Application mode.
     * @param userIntranetID
     *            User Intranet ID that is running the application.
     * @param userID
     *            UserID that is running the application.
     * @param useLogFile
     *            Create a session log file if it is true.
     * @return
     * @throws IcofException
     */
    public static AppContext createAppContext(String appName, String appMode,
                                              String userIntranetID, String userID, boolean useLogFile)
    throws IcofException {

        String funcName = "createAppContext";
        AppContext anAppContext = null;

        if (useLogFile) {
            // Construct the log file name
            String logFilePath = Constants.AES_PROD;
            if ((appMode.equals(Constants.DEV)) || (appMode.equals(Constants.ICCTEST))) {
                logFilePath = Constants.AES_DEV;
            } else if (appMode.equals(Constants.TEST)) {
                logFilePath = Constants.AES_TEST;
            }
            logFilePath += "/logs/" + appName;
            String hlq = IcofSystemUtil.determineHighLevelQualifier(appMode);
            logFilePath = hlq + logFilePath;

            // Set the SessionLog.
            SessionLog aLog = new SessionLog(appName, userID, appMode, true,
                                             SessionLog.TRACE, SessionLog.DEFAULT_CONVERSION_PATTERN,
                                             logFilePath);

            // Construct the app context with session log.
            anAppContext = new AppContext(userIntranetID, userID, appMode, aLog);

        } else {
            // Construct the app context without session log.
            anAppContext = new AppContext(userIntranetID, userID, appName,
                                          appMode, false);
        }

        if (anAppContext == null) {
            throw new IcofException(APP_NAME, funcName, IcofException.SEVERE,
                                    "Failed to initialize AppContect Object.", "appName="
                                    + appName + " appMode=" + appMode + " userID="
                                    + userID);
        }

        return anAppContext;
    }

    // -------------------------------------------------------------------------//
    // MEMBER GETTER FUNCTIONS //
    // -------------------------------------------------------------------------//

    // ---------------------------------------------------------------------------
    /**
     * Get the application mode
     * 
     * @return appMode The application mode to use in constructing the directory
     *         name. (ICCPROD, PROD, DEV, TEST)
     */
    // ---------------------------------------------------------------------------
    public String getAppMode() {
        return appMode;
    }

    // ---------------------------------------------------------------------------
    /**
     * Get the high level qualifier
     * 
     * @return highLvlQualifer The top level directory qualifier, set based on
     *         the operating system.
     */
    // ---------------------------------------------------------------------------
    public String getHighLvlQualifier() {
        return highLvlQualifier;
    }

    // ---------------------------------------------------------------------------
    /**
     * Get value of afsUserid
     * 
     * @return afsUserid The afs user id
     */
    // ---------------------------------------------------------------------------
    public String getAfsUserid() {
        return afsUserid;
    }

    // ---------------------------------------------------------------------------
    /**
     * Get value of intranetUserid
     * 
     * @return intranetUserid The intranet user id
     */
    // ---------------------------------------------------------------------------
    public String getIntranetUserid() {
        return intranetUserid;
    }

    // ---------------------------------------------------------------------------
    /**
     * Get value of dbName
     * 
     * @return dbName The name of the database based on the appMode
     */
    // ---------------------------------------------------------------------------
    public String getDBName() {
        return dbName;
    }

    // ---------------------------------------------------------------------------
    /**
     * Get value of dbServerName
     * 
     * @return dbName The name of the database server based on the appMode
     */
    // ---------------------------------------------------------------------------
    public String getDBServerName() {
        return dbServerName;
    }

    // ---------------------------------------------------------------------------
    /**
     * Get value of dbPort
     * 
     * @return dbPort The number of the database port based on the appMode
     */
    // ---------------------------------------------------------------------------
    public String getDBPort() {
        return dbPort;
    }


    // ---------------------------------------------------------------------------
    /**
     * Get the database URL
     * 
     * @return The database URL
     */
    // ---------------------------------------------------------------------------
    public String getDBUrl() {
        String dbURL = "jdbc:db2://" + getDBServerName() + ":" + getDBPort()
        + "/" + getDBName();
        return dbURL;
    }


    // ---------------------------------------------------------------------------
    /**
     * Get the database URL, using the specified appMode
     * 
     * @apAppMode  the appMode for which to construct the DB URL.
     * @return The database URL
     */
    // ---------------------------------------------------------------------------
    public String getDBUrl(String anAppMode) throws IcofException {

        // Ensure the dbname has been set.
        if ((getDBName() == null) || (getDBName().equals(""))) {
            setDBNameFromAppMode(anAppMode);
        }

        return getDBUrl();
    }

    // ---------------------------------------------------------------------------
    /**
     * Get value of osName
     * 
     * @return osName The name of the current platform
     */
    // ---------------------------------------------------------------------------
    public String getOsName() {
        return osName;
    }

    // ---------------------------------------------------------------------------
    /**
     * Get value of maxAfsFileSize
     * 
     * @return maxAfsFileSize 
     */
    // ---------------------------------------------------------------------------
    public long getMaxAfsFileSize() throws IcofException {
        if (maxAfsFileSize == 0L) {
            setMaxAfsFileSize();
        }
        return maxAfsFileSize;
    }

    // ---------------------------------------------------------------------------
    /**
     * Get value of ctx
     * 
     * @return ctx The default context
     */
    // ---------------------------------------------------------------------------
    public DefaultContext getDefaultContext() {
        return ctx;
    }

    // ---------------------------------------------------------------------------
    /**
     * Get value of connection
     * 
     * @return connection The db connection
     */
    // ---------------------------------------------------------------------------
    public Connection getConnection() {
        return connection;
    }

    // ---------------------------------------------------------------------------
    /**
     * Get value of spaceMgrCtx
     * 
     * @return spaceMgrCtx The spaceMgr default context
     */
    // ---------------------------------------------------------------------------
    public DefaultContext getSpaceMgrContext() {
        return spaceMgrCtx;
    }

    // ---------------------------------------------------------------------------
    /**
     * Get value of spaceMgrConnection
     * 
     * @return spaceMgrConnection The db connection for space management
     */
    // ---------------------------------------------------------------------------
    public Connection getSpaceMgrConnection() {
        return spaceMgrConnection;
    }

    // ---------------------------------------------------------------------------
    /**
     * Get the SessionLog
     * 
     * @return sessionLog The logging object
     */
    // ---------------------------------------------------------------------------
    public SessionLog getSessionLog() {
        return sessionLog;
    }

    // ---------------------------------------------------------------------------
    /**
     * Get the content of the AppContext as a String
     * 
     * @return String The content of the SessionLog object
     */
    // ---------------------------------------------------------------------------
    public String asString() {

        String tmpDefaultContext = "null";
        if (getDefaultContext() != null) {
            tmpDefaultContext = getDefaultContext().toString();
        }
        return ("AFS user id: " + getAfsUserid() + ";" + "Application Mode: "
                        + getAppMode() + ";" + "High Level Qualifier "
                        + getHighLvlQualifier() + ";" + "DB Name " + getDBName() + ";"
                        + "DefaultContext: " + tmpDefaultContext + ";"
                        + "Intranet user id: " + getIntranetUserid() + ";"
                        + "Session Log: " + getSessionLog());
    }

    // -------------------------------------------------------------------------//
    // MEMBER SETTER FUNCTIONS //
    // -------------------------------------------------------------------------//

    // ---------------------------------------------------------------------------
    /**
     * Set the application mode. If an invalid appMode is passed, the app mode
     * is not changed and the exception is logged via getSessionLog().log(ie)
     * 
     * @param anAppMode
     *            An app mode (ICCPROD, PROD, DEV, TEST). AppMode constants are
     *            found in Constants.java
     * @throws IcofException
     *             Invalid Application mode
     */
    // ---------------------------------------------------------------------------
    protected void setAppMode(String anAppMode) throws IcofException {
        validateAppMode(anAppMode);
        this.appMode = anAppMode;
    }

    // ---------------------------------------------------------------------------
    /**
     * Set the high level qualifier.
     * 
     */
    // ---------------------------------------------------------------------------
    protected void setHighLvlQualifier(String hlq) {
        highLvlQualifier = hlq;
    }

    // ---------------------------------------------------------------------------
    /**
     * Set the dbName.
     * 
     */
    // ---------------------------------------------------------------------------
    protected void setDBName(String name) {
        dbName = name;
    }

    // ---------------------------------------------------------------------------
    /**
     * Set the dbServerName.
     * 
     */
    // ---------------------------------------------------------------------------
    protected void setDBServerName(String name) {
        dbServerName = name;
    }

    // ---------------------------------------------------------------------------
    /**
     * Set the dbPort.
     * 
     */
    // ---------------------------------------------------------------------------
    protected void setDBPort(String name) {
        dbPort = name;
    }

    // ---------------------------------------------------------------------------
    /**
     * Set the high level qualifier.
     * 
     */
    // ---------------------------------------------------------------------------
    protected void setHighLvlQualifier() {

        setHighLvlQualifier(IcofSystemUtil
                            .determineHighLevelQualifier(getAppMode()));

    }

    // ---------------------------------------------------------------------------
    /**
     * Set the osName.
     * 
     */
    // ---------------------------------------------------------------------------
    protected void setOsName() {
        osName = System.getProperty("os.name");
    }

    // ---------------------------------------------------------------------------
    /**
     * Set the max afs file size.
     * 
     */
    // ---------------------------------------------------------------------------
    protected void setMaxAfsFileSize() throws IcofException {

        maxAfsFileSize = IcofSystemUtil.getMaxAfsFileSize();

    }

    // ---------------------------------------------------------------------------
    /**
     * Validate and set the userids
     * 
     * @param anIntranetUserid
     *            the intranet userid to be validated
     * @param anAfsUserid
     *            afsUserid to be validated
     * 
     * @exception IcofException
     *                unable to set the userids
     */
    // ---------------------------------------------------------------------------
    protected void setUserids(String anIntranetUserid, String anAfsUserid)
    throws IcofException {

        // Validate and set intranet userid
        setIntranetUserid(anIntranetUserid);

        // Validate/build and set AFS userid
        setAfsUserid(anAfsUserid);

    }

    // ---------------------------------------------------------------------------
    /**
     * Validate and set the intranet userid
     * 
     * @param anIntranetUserid
     *            the intranet userid to be validated
     * 
     * @exception IcofException
     *                unable to set the userid
     */
    // ---------------------------------------------------------------------------
    protected void setIntranetUserid(String anIntranetUserid)
    throws IcofException {

        // Validate and set intranet userid
        if (!verifyIntranetUserid(anIntranetUserid)) {
            IcofException ie = new IcofException(this.getClass().getName(),
                                                 "setIntranetUserid()", IcofException.SEVERE,
                                                 BAD_INTRANET_USERID, "");
            getSessionLog().log(ie);
            throw ie;
        }
        intranetUserid = anIntranetUserid;

    }

    // ---------------------------------------------------------------------------
    /**
     * Validate and set the afs userid
     * 
     * @param anAfsUserid
     *            afsUserid to be validated
     * 
     * @exception IcofException
     *                unable to set the userid
     */
    // ---------------------------------------------------------------------------
    protected void setAfsUserid(String anAfsUserid) throws IcofException {

        // Validate/build and set AFS userid
        if ((anAfsUserid == null) || (anAfsUserid.equals(""))) {

            // Begin by creating the afs userid -- this is made using the
            // intranet
            // userid. Set the afsUserid in the class to this "made-up" value
            // in case there is not an afsUserid in the database for this
            // intranet userid
            afsUserid = createAfsUserid(intranetUserid);

            // Attempt to look up the intranet userid in the database to
            // get the afs userid, only if the default context has a value
            if ((ctx != null) && (ctx.getConnection() != null)) {

                // Use a jdbc query to avoid having a circ reference between
                // IcofUserDBInterface and AppContext
                Connection con = ctx.getConnection();
                try {
                    Statement stmt = con.createStatement();
                    String query = "Select userid from icof.icof_user where LOWER("
                        + "intranet_userid) = '"
                        + getIntranetUserid().toLowerCase()
                        + "' and deletion_tmstmp is null";
                    ResultSet rs = stmt.executeQuery(query);
                    boolean found = rs.next();
                    if (found) {
                        String dbAfsUserid = rs.getString(1);
                        if (!dbAfsUserid.equals("")) {
                            afsUserid = dbAfsUserid;
                        }
                    }
                }

                catch (Exception e) {
                    IcofException ie = new IcofException(this.getClass()
                                                         .getName(), "setAfsUserid(String)",
                                                         IcofException.SEVERE, e.getMessage(), anAfsUserid);
                    getSessionLog().log(ie);
                    throw ie;
                }
            }
        } else
            afsUserid = anAfsUserid;

    }

    // ---------------------------------------------------------------------------
    /**
     * Validate and set the Default Context
     * 
     * @param aCtx
     *            context to be validated
     * 
     * @exception IcofException
     *                unable to set the default context
     */
    // ---------------------------------------------------------------------------
    protected void setDefaultContext(DefaultContext aCtx) throws IcofException {

        // Verify default context is not null and ctx contains a valid
        // database connection
        if ((aCtx == null) || (aCtx.getConnection() == null)) {
            String msg = "DefaultContext object is null or contains ";
            msg += "a null database connection.";
            IcofException ie = new IcofException(this.getClass().getName(),
                                                 "setDefaultContext()", IcofException.SEVERE, msg, "");
            getSessionLog().log(ie);
            throw ie;
        }

        // Set the DefaultContext
        ctx = aCtx;

    }

    // ---------------------------------------------------------------------------
    /**
     * Set the SessionLog attribute.
     * 
     * @param aSessionLog
     *            An instantiated SessionLog object
     */
    // ---------------------------------------------------------------------------
    public void setSessionLog(SessionLog aSessionLog) {
        this.sessionLog = aSessionLog;
    }

    // ---------------------------------------------------------------------------
    /**
     * Change the email address
     * 
     * @param anIntranetUserid
     *            the intranet userid to be validated
     * 
     * @exception IcofException
     *                unable to set the userid
     */
    // ---------------------------------------------------------------------------
    public void updateEmail(String anIntranetUserid) throws IcofException {
        // Validate and set intranet userid
        setIntranetUserid(anIntranetUserid);
    }

    // ---------------------------------------------------------------------------
    /**
     * Set the SessionLog attribute.
     * 
     * @param appName
     *            The application constructing this object.
     * @param userID
     *            The userID (creator)
     * @param appMode
     *            The app mode (ICCPROD, PROD, DEV, TEST)
     * @param minimumLevel
     *            The lowest level that the logger will log. Use a constant from
     *            SessionLog.java.
     * @param conversionPattern
     *            The pattern in which to log statements. Two are offered by
     *            SessionLog. The user can specify their own.
     * @param path
     *            The path where the SessionLog file will be located. Insert a
     *            blank String or use a different constructor if you wish to
     *            base the path on the appMode.
     * @param fileAppender
     *            true, to log to file; false, to log to console
     * @throws IcofException
     *             if the dir cannot be validated or created, if the PrintWriter
     *             contains errors, if the named file exists but is a directory
     *             rather than a regular file, does not exist but cannot be
     *             created, or cannot be opened for any other reason.
     */
    // ---------------------------------------------------------------------------
    protected void setSessionLog(String appName, String userID, String appMode,
                                 String minimumLevel, String conversionPattern, String path,
                                 boolean fileAppender) throws IcofException {
        String funcName;
        funcName = "setSessionLog(String, String, String, String, String, String, boolean)";
        String msg = "SessionLog instantiation error: \n";
        try {
            sessionLog = new SessionLog(appName, userID, appMode, fileAppender,
                                        minimumLevel, conversionPattern, path);
        } catch (IcofException ie) {
            IcofException e = new IcofException(this.getClass().getName(),
                                                funcName, IcofException.SEVERE, msg + ie.getMessage(), "");
            throw e;
        }
    }// end setSessionLog

    // -------------------------------------------------------------------------//
    // CLASS FUNCTIONS //
    // -------------------------------------------------------------------------//

    // -----------------------------------------------------------------------------
    /**
     * Validate the application mode.
     * 
     * @param anAppMode
     *            the application mode to be validated
     * 
     * @exception IcofException
     *                invalid application mode
     */
    // -----------------------------------------------------------------------------
    protected void validateAppMode(String anAppMode) throws IcofException {

        String funcName = new String("validateAppMode(String)");

        // Make sure application mode is valid.
        if ((!anAppMode.equals(Constants.DEV))
                        && (!anAppMode.equals(Constants.TEST))
                        && (!anAppMode.equals(Constants.PROD))
                        && (!anAppMode.equals(Constants.ICCTEST))
                        && (!anAppMode.equals(Constants.ICCPROD))
                        && (!anAppMode.equals(Constants.JUNIT))) {

            IcofException ie = new IcofException(getClass().getName(),
                                                 funcName, IcofException.SEVERE, "Invalid application mode",
                                                 anAppMode);
            if (getSessionLog() != null) {
                getSessionLog().log(ie);
            }
            throw (ie);
        }
    }

    // ---------------------------------------------------------------------------
    /**
     * Build an AFS userid from the Intranet userid
     * 
     * @param anIntranetUserid
     *            the intranetUserid from which to construct an afsUserid
     * @return the constructed afsUserid
     * @exception IcofException
     *                unable to construct afsUserid
     */
    // ---------------------------------------------------------------------------
    public String createAfsUserid(String anIntranetUserid) throws IcofException {

        String id = "";

        // Validate intranet userid
        if (!verifyIntranetUserid(anIntranetUserid)) {
            if (anIntranetUserid != null)
                id = anIntranetUserid;
            IcofException ie = new IcofException("AppContext",
                                                 "createAfsUserid()", IcofException.SEVERE,
                                                 BAD_INTRANET_USERID, id);
            getSessionLog().log(ie);
            throw ie;
        }

        // Set the AFS userid to the username portion of the intranet which is
        // formatted as user_name@domain_name
        id = anIntranetUserid.substring(0, anIntranetUserid.indexOf("@"));

        // Truncate the userid to the max size of an afs userids
        if (id.length() > Constants.AFS_USERID_LENGTH) {
            id = id.substring(0, Constants.AFS_USERID_LENGTH);
        }

        return (id);

    }

    // ---------------------------------------------------------------------------
    /**
     * Verify the Intranet userid is not null, not empty and formatted like an
     * email address.
     * 
     * @param anIntranetUserid
     *            the intranet userid to be validated
     * 
     * @return true if valid; false, if not
     */
    // ---------------------------------------------------------------------------
    public boolean verifyIntranetUserid(String anIntranetUserid) {

        // Verify intranet userid is not null or empty
        if ((anIntranetUserid == null) || (anIntranetUserid.equals("")))
            return false;

        // Verify intranet user is formatted like an email address
        return (IcofEmailUtil.isValidEmailAddress(anIntranetUserid));

    }

    // ---------------------------------------------------------------------------
    /**
     * Get a database driver instance
     * 
     * @return a database driver instance
     * @throws IcofException
     *             unable to get the database driver instance
     */
    // ---------------------------------------------------------------------------
    public static synchronized DB2Driver getDB2DriverInstance()
    throws IcofException {
        try {
            return (DB2Driver) Class.forName(DB2_DRIVER).newInstance();
        } catch (Exception e) {
            IcofException ie = new IcofException("AppContext",
                                                 "getDB2DriverInstance", IcofException.SEVERE,
                                                 "Unable to get a db2Driver instance; Msg = ", e
                                                 .getMessage());
            throw ie;
        }
    }

    
    // ---------------------------------------------------------------------------
    /**
     * Get a database connection for use by SpaceMgr, and use the appMode to 
     * determine which database to connect to. The result of this method is to 
     * set the spaceMgrCtx data member in this class.
     * 
     * @param userid
     *            the userid to connect to the database
     * @param passwd
     *            the password to connect with
     * 
     * @throws IcofException
     *             unable to get the database connection
     */
    // ---------------------------------------------------------------------------
    public void setSpaceMgrDBConnection() throws IcofException {

        String dbURL = getDBUrl(appMode);

        try {
            
            // Connect to the database
            spaceMgrConnection = DriverManager.getConnection(dbURL, 
                                                             Constants.DB_ACCESS_ID, 
                                                             Constants.DB_ACCESS_CODE);
            spaceMgrConnection.setAutoCommit(false);

            spaceMgrCtx = new DefaultContext(spaceMgrConnection);

        } 
        catch (Exception e) {
            IcofException ie = new IcofException(getClass().getName(),
                                                 "connectToDB", IcofException.SEVERE,
                                                 "Unable to connect to Database URL " + 
                                                 dbURL + "; Msg = ",
                                                 e.getMessage());
            getSessionLog().log(ie);
            throw ie;
        }

    }
    
    
    // ---------------------------------------------------------------------------
    /**
     * Get a database connection, and use the appMode to determine which
     * database to connect to. The result of this method is to set the default
     * context data member in this class.
     * 
     * @param userid
     *            the userid to connect to the database
     * @param passwd
     *            the password to connect with
     * 
     * @throws IcofException
     *             unable to get the database connection
     */
    // ---------------------------------------------------------------------------
    public void connectToDB(String userid, String passwd) throws IcofException {

        connectToDB(userid, passwd, getAppMode());

    }

    
    // ---------------------------------------------------------------------------
    /**
     * Get a database connection, and use the specified appMode (instead of the
     * appMode that is a member of this class) to determine which database to
     * connect to. The result of this method is to set the default context data
     * member in this class.
     * 
     * NOTE: This method should only be used from a JUNIT test. It exists to
     * allow the JUNIT testcases to be able to work with dkdev or dkprod
     * databases so that they work without changes when building dev, test,
     * bugfix, or prod streams.
     * 
     * @param userid
     *            the userid to connect to the database
     * @param passwd
     *            the password to connect with
     * @param anAppMode
     *            the appMode to use for determining the database name
     * 
     * @throws IcofException
     *             unable to get the database connection
     */
    // ---------------------------------------------------------------------------
    public void connectToDB(String userid, String passwd, String anAppMode)
    throws IcofException {

        String dbURL = getDBUrl(anAppMode);

        try {
            // Connect to the database
            connection = DriverManager.getConnection(dbURL, userid, passwd);
            connection.setAutoCommit(false);

            ctx = new DefaultContext(connection);

        } catch (Exception e) {
            IcofException ie = new IcofException(getClass().getName(),
                                                 "connectToDB", IcofException.SEVERE,
                                                 "Unable to connect to Database URL " + 
                                                 dbURL + "; Msg = ",
                                                 e.getMessage());
            getSessionLog().log(ie);
            throw ie;
        }

    }

    
    // -------------------------------------------------------------------------
    /**
     * Disconnect from the database, if  connected 
     * 
     * 
     * @throws IcofException
     * 
     */
    // -------------------------------------------------------------------------
    public void disconnectFromDB() throws IcofException {

        try {
            //  Close the database connection, if connected.
            if (getDefaultContext() != null) {
                if (!getDefaultContext().isClosed()) {
                    getDefaultContext().close();
                }
                if (!getConnection().isClosed()) {
                    getConnection().close();
                }
            }
            connection = null;
            ctx = null;
        }
        catch(SQLException se) {
            IcofException ie = new IcofException(this.getClass().getName(), 
                                                 "disconnectFromDB()", 
                                                 IcofException.SEVERE, 
                                                 "Failed to disconnect from database: " + 
                                                 getDBUrl() + 
                                                 "  Message = " + se.getErrorCode() + 
                                                 "  " + se.getMessage(),
            "");
            throw ie;
        }
    }

    
    // -------------------------------------------------------------------------
    /**
     * Close the spaceMgr db2 connection, if it exists. 
     * 
     * 
     * @throws IcofException
     * 
     */
    // -------------------------------------------------------------------------
    public void closeSpaceMgrConnection() throws IcofException {

        try {
            //  Close the database connection, if connected.
            if (spaceMgrCtx != null) {
                if (!spaceMgrCtx.isClosed()) {
                    spaceMgrCtx.close();
                }
                if (!spaceMgrConnection.isClosed()) {
                    spaceMgrConnection.close();
                }
            }
            spaceMgrConnection = null;
            spaceMgrCtx = null;
        }
        catch(SQLException se) {
            IcofException ie = new IcofException(this.getClass().getName(), 
                                                 "closeSpaceMgrConnection()", 
                                                 IcofException.SEVERE, 
                                                 "Failed to disconnect space manager from database: " + 
                                                 getDBUrl() + 
                                                 "  Message = " + se.getErrorCode() + 
                                                 "  " + se.getMessage(),
                                                 "");
            throw ie;
        }
    }

    
    // -----------------------------------------------------------------------------
    /**
     * Determine the database name, based on the appMode (inside this object)
     * 
     * @exception IcofException
     *                Unable to determine database name.
     */
    // -----------------------------------------------------------------------------
    protected void setDBName() throws IcofException {

        setDBNameFromAppMode(getAppMode());

    }

    // -----------------------------------------------------------------------------
    /**
     * Determine the database name, based on the specified appMode
     * 
     * @param anAppMode
     *            the appMode to use for determining the database name
     * 
     * @exception IcofException
     *                Unable to determine database name.
     */
    // -----------------------------------------------------------------------------
    protected void setDBNameFromAppMode(String anAppMode) throws IcofException {

        String funcName = new String("setDBNameFromAppMode()");

        // Construct directory name, containing file that relates appModes to
        // database names
        // Don't use the method in IcofUtil, because other methods in that
        // class use TechRelease, which uses two "db" classes (Technology
        // and LibRelease). These cause a "circular reference" when building
        // the IcofDB.jar file. It references AppContext, which references
        // IcofUtil,
        // which references TechRelease, which references Technology, which
        // references TechLibDBInterface (and others) which is one of the
        // classes
        // that is to be compiled for the IcofDB.jar file.
        // String dirName = IcofUtil.constructAimIcofSwadminDirName(this);
        String dirName = getHighLvlQualifier();
        if ((anAppMode.equals(Constants.ICCPROD))
                        || (anAppMode.equals(Constants.PROD))) {
            dirName += Constants.PROD_AES_SYS_ADMIN;
        } else if ((anAppMode.equals(Constants.DEV))
                        || (anAppMode.equals(Constants.JUNIT))) {
            dirName += Constants.DEV_AES_SYS_ADMIN;
        } else if (anAppMode.equals(Constants.ICCTEST)) {
            dirName += Constants.ICCTEST_AES_SYS_ADMIN;
        } else {
            dirName += Constants.DEV_AES_SYS_ADMIN;
        }
        String fileName = new String(dirName + "/" + Constants.DB_NAMES);

        IcofFile srcFile = new IcofFile(fileName, false);

        // Test for existence.
        if (!srcFile.exists()) {
            IcofException ie = new IcofException(getClass().getName(),
                                                 funcName, IcofException.SEVERE, "File does not exist",
                                                 fileName);
            getSessionLog().log(ie);
            throw ie;
        }

        // Open the file for reading.
        srcFile.openRead();

        // Read the file.
        boolean found = false;
        String thisLine = srcFile.readLine();
        while ((thisLine != null) && (!found)) {

            thisLine = thisLine.trim();

            Vector fields = new Vector();
            IcofCollectionsUtil.parseString(thisLine, ";", fields, true);
            String fileAppMode = (String) fields.elementAt(0);

            if (fileAppMode.equals(anAppMode)) {
                setDBName((String) fields.elementAt(1));
                setDBServerName((String) fields.elementAt(2));
                setDBPort((String) fields.elementAt(3));
                found = true;
            }

            thisLine = srcFile.readLine();
        }

        if (dbName.equals("")) {
            IcofException ie = new IcofException(getClass().getName(),
                                                 funcName, IcofException.SEVERE,
                                                 "Unable to determine database name for appMode",
                                                 getAppMode());
            getSessionLog().log(ie);
            throw ie;
        }

        srcFile.closeRead();

        setDBName(dbName);

    }

}

// ============================ END OF FILE ==================================
