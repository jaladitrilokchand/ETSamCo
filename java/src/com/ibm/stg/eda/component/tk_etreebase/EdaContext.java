/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *
 *-PURPOSE---------------------------------------------------------------------
 * EdaContext object
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 05/17/2010 GFS  Initial coding.
 * 03/10/2011 GFS  Added support for greenjiffy DB server.
 * 04/10/2011 GFS  Added support for EDA_TEST, EDA_GDEV, EDA_GPROD and EDA_GTEST
 *                 application modes/datebases.
 * 05/10/2011 GFS  Added support for EDA_BDEV, EDA_BPROD and EDA_BTEST
 *                 application modes/datebases.
 * 05/13/2011 GFS  Changed default database server to be greenjiffy.                
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreebase;

import java.sql.Statement;

import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.SessionLog;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;

public class EdaContext extends AppContext {

    /**
     * 
     */
    private static final long serialVersionUID = -333679349483011152L;
    
    /**
     * Constants.
     */
    //    public static final String DB_NAME_BPROD = "etreedb";
    //    public static final String DB_NAME_BTEST = "tktestdb";
    //    public static final String DB_NAME_BDEV = "tkdevdb";
    //    public static final String DB_PORT_BPROD = "52000";
    //    public static final String DB_PORT_BTEST = "52000";
    //    public static final String DB_PORT_BDEV = "52000";
    //    public static final String DB_SERVER_BPROD = "bluejiffy.btv.ibm.com";
    //    public static final String DB_SERVER_BTEST = "bluejiffy.btv.ibm.com";
    //    public static final String DB_SERVER_BDEV = "bluejiffy.btv.ibm.com";
    //    public static final String DB_ACCESS_ID_B = "etreeadm"; 
    //    public static final String DB_ACCESS_PW_B = "etreedb2";
    //public static final String DB_CURRENT_TMS = "current timestamp";
    public static final String DB_NAME_GPROD = "etreedb";
    public static final String DB_NAME_GTEST = "tktestdb";
    public static final String DB_NAME_GDEV = "tkdevdb";
    public static final String DB_PORT_GPROD = "60004";
    public static final String DB_PORT_GTEST = "60008";
    public static final String DB_PORT_GDEV = "60000";
    public static final String DB_SERVER_GPROD = "greenjiffy.fishkill.ibm.com";
    public static final String DB_SERVER_GTEST = "greenjiffy.fishkill.ibm.com";
    public static final String DB_SERVER_GDEV = "greenjiffy.fishkill.ibm.com";
    public static final String DB_ACCESS_ID_G = "etreeadm"; 
    public static final String DB_ACCESS_PW_G = "etreedb2";


    /**
     * Member
     */
    //private Statement statement = null;


    /**
     * Create the JDBC statement object from the DB connection.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     *  @throws          Trouble creating the statement.
     */
    public Statement getStatement() throws IcofException {

	// Otherwise create a statement object and return it.
	try {
	    Statement statement = getConnection().createStatement();
	    return statement;
	}
	catch (Exception e) {
	    IcofException ie = new IcofException("EdaContext",
	                                         "getStatement()",
	                                         IcofException.SEVERE,
	                                         e.getMessage(), "");
	    getSessionLog().log(ie);
	    throw ie;
	}

    }


    /**
     * Construct an EdaContext object using a predefined SessionLog object, a
     * known intranet userid and afs userid, and appMode. This constructor is
     * best used for defining a specific SessionLog path without being able to
     * pass a DefaultContext.
     * 
     * @param anIntranetUserid   Intranet userid of the person for which this 
     *                           context is being created
     * @param anAfsUserid        afsUserid of the person for which this context
     *                           is being created
     * @param appName            The application constructing this object.
     * @param appMode            The app mode (PROD, DEV, TEST, GPROD, GDEV, GTEST)
     * @param aSessionLog        A constructed SessionLog object.
     * 
     * @throws IcofException     Unable to create object
     */
    public EdaContext(String anIntranetUserid, String anAfsUserid,
                      String appMode, SessionLog aSessionLog)
                      throws IcofException {

	super(anIntranetUserid, anAfsUserid, appMode, aSessionLog);

    }


    /**
     * Constructor - use when a database connection is not needed or will be
     * created separately.
     * 
     * @param anIntranetUserid  IntranetUserid of the person for which this 
     *                          context is being created
     * @param anAfsUserid  afsUserid of the person for which this context is 
     *                     being created
     * @param appName   The Name of the application for this appContext
     * @param appMode   The app mode (PROD, DEV, TEST)
     * @param append    True if logging to fileAppender is desired, false if no
     *                  logging is desired.
     * @exception IcofException  Unable to create object
     */
    public EdaContext(String anIntranetUserid, String anAfsUserid,
                      String appName, String appMode, boolean append)
                      throws IcofException {

	super(anIntranetUserid, anAfsUserid, appName, appMode, append);

    }


    /**
     * Validate the application mode.
     * 
     * @param anAppMode  the application mode to be validated
     * 
     * @exception IcofException  invalid application mode
     */
    protected void validateAppMode(String anAppMode) throws IcofException {

	String funcName = "validateAppMode(String)";

	// Make sure application mode is valid.
	if ((! anAppMode.equals(TkConstants.EDA_DEV)) && 
	(! anAppMode.equals(TkConstants.EDA_TEST)) &&
	(! anAppMode.equals(TkConstants.EDA_PROD)) &&

	(! anAppMode.equals(TkConstants.EDA_GDEV)) &&
	(! anAppMode.equals(TkConstants.EDA_GTEST)) &&
	(! anAppMode.equals(TkConstants.EDA_GPROD))) {


	    IcofException ie = new IcofException(getClass().getName(),
	                                         funcName, IcofException.SEVERE,
	                                         "Invalid application mode",
	                                         anAppMode);
	    if (getSessionLog() != null) {
		getSessionLog().log(ie);
	    }
	    throw (ie);
	}
    }


    /**
     * Determine the db name, db server name and db port. Defaults to PROD.
     * 
     * @param anAppMode the appMode to use for determining the database name
     */
    protected void setDBNameFromAppMode(String anAppMode) {

	if (anAppMode.equals(TkConstants.EDA_DEV)) {
	    setDBName(DB_NAME_GDEV);
	    setDBServerName(DB_SERVER_GDEV);
	    setDBPort(DB_PORT_GDEV);
	}
	else if (anAppMode.equals(TkConstants.EDA_TEST)) {
	    setDBName(DB_NAME_GTEST);
	    setDBServerName(DB_SERVER_GTEST);
	    setDBPort(DB_PORT_GTEST);
	}

	else  if (anAppMode.equals(TkConstants.EDA_GDEV)) {
	    setDBName(DB_NAME_GDEV);
	    setDBServerName(DB_SERVER_GDEV);
	    setDBPort(DB_PORT_GDEV);
	}
	else  if (anAppMode.equals(TkConstants.EDA_GTEST)) {
	    setDBName(DB_NAME_GTEST);
	    setDBServerName(DB_SERVER_GTEST);
	    setDBPort(DB_PORT_GTEST);
	}
	else  if (anAppMode.equals(TkConstants.EDA_GPROD)) {
	    setDBName(DB_NAME_GPROD);
	    setDBServerName(DB_SERVER_GPROD);
	    setDBPort(DB_PORT_GPROD);
	}

	else {
	    // Default is greenjiffy PROD database.
	    setDBName(DB_NAME_GPROD);
	    setDBServerName(DB_SERVER_GPROD);
	    setDBPort(DB_PORT_GPROD);
	}

    }

}
