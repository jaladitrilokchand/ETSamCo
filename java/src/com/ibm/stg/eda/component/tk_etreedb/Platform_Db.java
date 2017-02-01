/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *    DATE: 05/18/2010
 *
 *-PURPOSE---------------------------------------------------------------------
 * TK Platform DB class with audit info
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 05/18/2010 GFS  Initial coding.
 * 07/23/2010 GFS  Converted to using PreparedStatements.
 * 11/13/2013 GFS  Added dbLookupByTk() and getShippingName() methods.
 * 07/16/2015 GFS  Added dbAddRow()
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;


public class Platform_Db extends TkAudit {

    /**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.PLATFORM";
    public static final String ID_COL = "PLATFORM_ID";
    public static final String OS_COL = "OS";
    public static final String PACKAGING_COL = "PACKAGING_NAME";
    public static final String SYSTEM_COL = "SYSTEM_NAME";
    public static final String INSTALL_SHORT_NAME_COL = "INSTALL_SHORT_NAME";
    public static final String SIZE_COL = "SIZE";
    public static final String TECHLEVEL_COL = "TECHLEVEL";
    public static final String VERSION_COL = "VERSION";    
    public static final String ALL_COLS = ID_COL + "," +
	    OS_COL + "," +
	    SIZE_COL + "," + 
	    TECHLEVEL_COL + "," +
	    VERSION_COL + "," + 
	    PACKAGING_COL + "," +
	    SYSTEM_COL+ "," +
	    INSTALL_SHORT_NAME_COL+ "," +
	    CREATED_BY_COL + "," + 
	    CREATED_ON_COL + "," + 
	    UPDATED_BY_COL + "," + 
	    UPDATED_ON_COL + "," + 
	    DELETED_BY_COL + "," + 
	    DELETED_ON_COL;
    private static final long serialVersionUID = 6392549772170893770L;


    /**
     * Constructor - takes a DB id
     * 
     * @param anId A database id
     */
    public Platform_Db(short anId) {
	setId(anId);
    }


    /**
     * Constructor - takes an OS, size, techlevel and version.
     * 
     * @param anOs Name of operating system (linux, aix)
     * @param aSize Size in bits of os (32 or 64)
     * @param aTechLevel Name of AIX tech level (use null for linux) (tl07)
     * @param aVersion Name of operating system version (5.0, 6.1)
     */
    public Platform_Db(String anOs, short aSize, String aTechLevel,
	    String aVersion, String packagingName, String systemName, String installShortName) {

	setOs(anOs);
	setSize(aSize);
	setTechLevel(aTechLevel);
	setVersion(aVersion);
	setPackagingName(packagingName);
	setSystemName(systemName);
	setInstallShortName(installShortName);
    }


    /**
     * Constructor - takes a platform name in the common form (64-linux50)
     * 
     * @param aPlatformName Name of platform
     */
    public Platform_Db(String aPlatformName) {

	setMembers(aPlatformName);
    }


    /**
     * Constructor - takes a platform name in the common form (64-linux50)
     * 
     * @param aPlatformName Name of platform
     * @param packagingName Name of packaging
     */
    public Platform_Db(String aPlatformName, String packagingName) {

	setMembers(aPlatformName);
	this.packagingName = packagingName;
    }


    /**
     * Parse the common platform name (64-linux50. 64-rs_aix61) and set the data
     * members.
     * 
     * @param aName TkRelease and version name (14.1 or 1401)
     */
    private void setMembers(String aName) {

	// Get the size. (assumes on "64-" means 32 bit)
	String size = "32";
	if (aName.indexOf("-") > -1) {
	    size = aName.substring(0, aName.indexOf("-"));
	    aName = aName.substring(aName.indexOf("-") + 1, aName.length());
	}
	setSize(Short.parseShort(size));

	// Get the version. (assumes os names are aix or linux)
	int index = aName.lastIndexOf("x");
	if (index > -1) {
	    setVersion(aName.substring(index + 1, aName.length()));
	    aName = aName.substring(0, index + 1);
	}
	else {
	    setVersion("");
	}

	// Get the os
	setOs(aName);

	// Get the tech level (hard coded for AIX 6.1)
	String myTechLevel = "NA";
	if (getOs().indexOf("aix") > -1) {
	    myTechLevel = "TL07";
	}
	setTechLevel(myTechLevel);

    }


    /**
     * Data Members
     */
    // @formatter:off
    private short id;
    private String os;
    private short size;
    private String techLevel;
    private String version;
    private String packagingName;
    private String systemName;
    private String installShortName;


    /**
     * Getters
     */
    public short getId() { return id; }
    public String getOs() { return os; }
    public short getSize() { return size; }
    public String getTechLevel() { return techLevel; }
    public String getVersion() { return version; }
    public String getPackagingName() {return packagingName; }
    public String getSystemName() {return systemName; }
    public String getInstallShortName() {return installShortName; }


    /**
     * Setters
     */
    private void setId(short anId)  { id = anId; }
    private void setOs(String anOs)  { os = anOs; }
    private void setSize(short aSize)  { size = aSize; }
    private void setTechLevel(String aLevel)  { techLevel = aLevel; }
    private void setVersion(String aVersion)  { version = aVersion; }
    public void setPackagingName(String aName) { packagingName = aName; }
    public void setSystemName(String aName) { systemName = aName; }
    public void setInstallShortName(String aName) { installShortName = aName; }
    // @formatter:on


    /**
     * Return the version without the "." separator.
     * 
     * @return Version name
     */
    public String getVersionShort() {

	String shortVer = getVersion();

	if (getVersion() != null) {
	    int index = getVersion().indexOf(".");
	    if (index > -1) {
		shortVer = getVersion().substring(0, index - 1);
		shortVer += getVersion().substring(index, getVersion().length());
	    }
	}

	return shortVer;

    }


    /**
     * Get the platform name in the common form (64-rs_aix61).
     */
    public String getName() {

	String name = "";
	if (getSize() != 32) {
	    name = String.valueOf(getSize()) + "-";
	}
	name += getOs();
	name += getVersionShort();

	return name;
    }


    /**
     * Get the platform name in the shipping name format (ie,
     * 64-amd64_linux26_RH5 and 64-rs_aix61). This name is added
     * to .ship- to find the deliverables in the ship, tk or customtk
     * directories.
     */
    public String getShippingName() {

	String name = "";
	if (getSize() != 32) {
	    name += getSize() + "-";
	}

	return name + getSystemName();

    }


    /**
     * Get the platform name in the short install name format (ie,
     * lin64b-x86 and aix64b)
     */
    public String getShortInstallName() {

	return installShortName;

    }


    /**
     * Create a PreparedStatement to lookup this object by id.
     * 
     * @param xContext Application context.
     * @throws IcofException
     */
    public void setLookupIdStatement(EdaContext xContext)
	    throws IcofException {

	// Define the query.
	String query = "select * from " + TABLE_NAME + " where "
		+ ID_COL + " = ? " + " AND " + DELETED_ON_COL
		+ " is NULL";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by id.
     * 
     * @param xContext Application context.
     * @throws IcofException
     */
    public void setLookupNameStatement(EdaContext xContext)
	    throws IcofException {

	// Define the query.
//	String query = "select * from " + TABLE_NAME + " where "
//		+ OS_COL + " =  ? " + " AND " + SIZE_COL + " =  ? "
//		+ " AND " + TECHLEVEL_COL + " = ? " + " AND "
//		+ VERSION_COL + " = ? " + " AND " + DELETED_ON_COL
//		+ " is NULL";

    	// Updated query to not to use tech_level (navechan) 
    	// Gregg's update: tech level for the linux plats from NA to x86 or ppc is creating problem
    	String query = "select * from " + TABLE_NAME + " where "
    			+ OS_COL + " =  ? " + " AND " + SIZE_COL + " =  ? "
    			 + " AND " + VERSION_COL + " = ? " + " AND " + DELETED_ON_COL
    			+ " is NULL";
    	
	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by Packaging Name.
     * 
     * @param xContext Application context.
     * @throws IcofException
     */
    public void setLookupPackagingNameStatement(EdaContext xContext)
	    throws IcofException {

	// Define the query.
	String query = "select * from " + TABLE_NAME + " where "
		+ PACKAGING_COL + " =  ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup platforms by tk.
     * 
     * @param xContext Application context.
     * @throws IcofException
     */
    public void setLookupByTkStatement(EdaContext xContext)
	    throws IcofException {

	// Define the query.
	String query = "select p." + ID_COL + " from " + TABLE_NAME + " as p, "
		+ Release_Platform_Db.TABLE_NAME + " as rp"
		+ " where p." + DELETED_ON_COL + " is NULL"
		+ " and rp." + Release_Platform_Db.RELEASE_ID_COL
		+ " = ?" + " and rp."
		+ Release_Platform_Db.PLATFORM_ID_COL + " = p." + ID_COL;

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup the next id for this table.

     * @param xContext  Application context.
     * @throws IcofException
     */
    public void setNextIdStatement(EdaContext xContext) throws IcofException {

	// Define the query.
	String query =  TkAudit.getNextIdQuery(xContext, TABLE_NAME, ID_COL);

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to add a row.
     * 
     * @param  xContext  Application context.
     * @throws IcofException
     */
    public void setAddRowStatement(EdaContext xContext) throws IcofException {

	// Define the query.
	String query = "insert into " + 
		TABLE_NAME + 
		" ( " +  ALL_COLS + " )" + 
		" values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }



    /**
     * Insert a new row.
     * 
     * @param xContext  An application context object.
     * @throws IcofException Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext, User_Db user)
	    throws IcofException{

	// Get the next id for this new row.
	setNextIdStatement(xContext);
	setId(getNextSmallIntId(xContext));
	closeStatement(xContext);

	// Create the SQL query in the PreparedStatement.
	setAddRowStatement(xContext);
	Timestamp now = new Timestamp(new java.util.Date().getTime());
	try {
	    getStatement().setLong(1, getId());
	    getStatement().setString(2, getOs());
	    getStatement().setShort(3, getSize());
	    getStatement().setString(4, getTechLevel());
	    getStatement().setString(5, getVersion());
	    getStatement().setString(6, getPackagingName());
	    getStatement().setString(7, getSystemName());
	    getStatement().setString(8, getInstallShortName());
	    getStatement().setString(9, user.getIntranetId());
	    getStatement().setTimestamp(10, now);
	    getStatement().setString(11, user.getIntranetId());
	    getStatement().setTimestamp(12, now);
	    getStatement().setString(13, null);
	    getStatement().setString(14, null);

	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
		    "dbAddRow()",
		    IcofException.SEVERE,
		    "Unable to prepare SQL statement.",
		    IcofException.printStackTraceAsString(trap) + 
		    "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (! insertRow(xContext)) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
		    "dbAddRow()",
		    IcofException.SEVERE,
		    "Unable to insert new row.\n",
		    "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}


	// Close the PreparedStatement.
	closeStatement(xContext);

	// Load the data for the new row.
	dbLookupById(xContext); 

    }


    /**
     * Look up the Component by id.
     * 
     * @param xContext An application context object.
     * @throws IcofException Trouble querying the database.
     */
    public void dbLookupById(EdaContext xContext)
	    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupIdStatement(xContext);

	try {
	    getStatement().setShort(1, getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
		    this.getClass().getName(),
		    "dbLookupById()",
		    IcofException.SEVERE,
		    "Unable to prepare SQL statement.",
		    IcofException.printStackTraceAsString(trap)
		    + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (!selectSingleRow(xContext)) {
	    IcofException ie = new IcofException(
		    this.getClass().getName(),
		    "dbLookupById()",
		    IcofException.SEVERE,
		    "Unable to find row for query.\n",
		    "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;

	}

	// Close the PreparedStatement.
	closeStatement(xContext);

    }


    /**
     * Look up the Component by name.
     * 
     * @param xContext An application context object.
     * @throws IcofException Trouble querying the database.
     */
    public void dbLookupByName(EdaContext xContext)
	    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupNameStatement(xContext);

	try {
	    getStatement().setString(1, getOs().toUpperCase());
	    getStatement().setShort(2, getSize());
	  //getStatement().setString(3, getTechLevel().toUpperCase());  // updated to not to use tech_level(navechan)
	    getStatement().setString(3, getVersion().toUpperCase());

	}

	catch (SQLException trap) {
	    IcofException ie = new IcofException(
		    this.getClass().getName(),
		    "dbLookupByName()",
		    IcofException.SEVERE,
		    "Unable to prepare SQL statement.",
		    IcofException.printStackTraceAsString(trap)
		    + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (!selectSingleRow(xContext)) {
	    IcofException ie = new IcofException(
		    this.getClass().getName(),
		    "dbLookupByName()",
		    IcofException.SEVERE,
		    "Unable to find row for query.\n",
		    "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;

	}

	// Close the PreparedStatement.
	closeStatement(xContext);

    }


    /**
     * Look up the Component by packaging name.
     * 
     * @param xContext An application context object.
     * @throws IcofException Trouble querying the database.
     */
    public void dbLookupByPackagingName(EdaContext xContext)
	    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupPackagingNameStatement(xContext);

	try {
	    getStatement().setString(1, getPackagingName());
	}

	catch (SQLException trap) {
	    IcofException ie = new IcofException(
		    this.getClass().getName(),
		    "dbLookupByPackagingName()",
		    IcofException.SEVERE,
		    "Unable to prepare SQL statement.",
		    IcofException.printStackTraceAsString(trap)
		    + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (!selectSingleRow(xContext)) {
	    IcofException ie = new IcofException(
		    this.getClass().getName(),
		    "dbLookupByPackagingName()",
		    IcofException.SEVERE,
		    "Unable to find row for query.\n",
		    "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;

	}

	// Close the PreparedStatement.
	closeStatement(xContext);

    }


    /**
     * Create a list of Platform_Db objects for the given ToolKit
     * 
     * @param xContext An application context object.
     * @param xTk A RelVersion_Db object
     * @return Collection of Component_Db objects.
     * @throws IcofException Trouble querying the database.
     */
    public Vector<Platform_Db> dbLookupByTk(EdaContext xContext,
	    RelVersion_Db xTk)
		    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupByTkStatement(xContext);
	//printQuery(xContext);
	//System.out.println("Release ID "+xTk.getRelease().getId());

	try {
	    getStatement().setShort(1, xTk.getRelease().getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
		    this.getClass().getName(),
		    "dbLookupByTk()",
		    IcofException.SEVERE,
		    "Unable to prepare SQL statement.",
		    IcofException.printStackTraceAsString(trap)
		    + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Vector<Platform_Db> platforms = new Vector<Platform_Db>();
	try {
	    while (rs.next()) {
		Platform_Db plat = new Platform_Db(rs.getShort(Platform_Db.ID_COL));
		plat.dbLookupById(xContext);

		// Only 15.1.0 does not have plinux platform.
		if(xTk.getDisplayName().equals("15.1.0")){
		    if(! plat.getOs().equalsIgnoreCase("plinux")){
			platforms.add(plat);
		    }			
		}else{
		    platforms.add(plat);
		}		
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
		    "dbLookupByTk()", IcofException.SEVERE,
		    "Error reading DB query results.",
		    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return platforms;

    }


    /**
     * Populate this object from the result set.
     * 
     * @param xContext Application context.
     * @param rs A valid result set.
     * @throws IcofException
     * @throws SQLException
     */
    protected void populate(EdaContext xContext, ResultSet rs)
	    throws SQLException, IcofException {

	super.populate(xContext, rs);
	setId(rs.getShort(ID_COL));
	setOs(rs.getString(OS_COL));
	setSize(rs.getShort(SIZE_COL));
	setTechLevel(rs.getString(TECHLEVEL_COL));
	setVersion(rs.getString(VERSION_COL));
	setPackagingName(rs.getString(PACKAGING_COL));
	setSystemName(rs.getString(SYSTEM_COL));
	setInstallShortName(rs.getString(INSTALL_SHORT_NAME_COL));
	setLoadFromDb(true);

    }


    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

	// Get the audit data
	String audit = super.toString(xContext);

	// Get the class specific data
	String buffer = "";
	buffer += "ID: " + getId() + "\n";
	buffer += "OS: " + getOs() + "\n";
	buffer += "Size: " + getSize() + "\n";
	buffer += "Tech level: " + getTechLevel() + "\n";
	buffer += "Version: " + getVersion() + "\n";
	buffer += "Packaging Name: " + getPackagingName() + "\n";
	buffer += "Shipping Name: " + getShippingName() + "\n";
	buffer += "System Name: " + getSystemName() + "\n";
	buffer += "Install Short Name: " + getInstallShortName() + "\n";

	buffer += audit;

	return buffer;

    }


    /**
     * Create a key from the ID.
     * 
     * @param xContext Application context object.
     * @return A Statement object.
     */
    public String getIdKey(EdaContext xContext) {

	return String.valueOf(getId());
    }

}
