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
 * TK Release DB class with audit info
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 05/18/2010 GFS  Initial coding.
 * 07/22/2010 GFS  Converted to using PreparedStatements.
 * 04/14/2011 GFS  Added dbUpdateName and dbDelete methods.
 * 04/28/2011 GFS  Added partial support for branch name.
 * 11/17/2011 GFS  Added getAltDisplayName().
 * 10/30/2012 GFS  Updated to support xtinct tool kits.  Added support for new
 *                 DB columns - cq_relname, description and parent.
 * 11/27/2012 GFS  Updated constructors to work with all TK variants.
 * 02/19/2013 GFS  Updated dbLookupByStagename to include new CQ data.
 * 06/14/2013 GFS  Updated so CQ release name is compared in uppercase.
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class RelVersion_Db extends TkAudit {

    /**
     * 
     */
    private static final long serialVersionUID = 9149377440324160963L;
    /**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.TKVERSION";
    public static final String ID_COL = "TKVERSION_ID";
    public static final String RELEASE_ID_COL = "TKRELEASE_ID";
    public static final String NAME_COL = "TKVERSION_NAME";
    public static final String BRANCH_COL = "BRANCH_NAME";
    public static final String STAGE_NAME_ID_COL = "STAGENAME_ID";
    public static final String CQ_RELEASE_NAME_COL = "CQ_RELNAME";
    public static final String DESCRIPTION_COL = "DESCRIPTION";
    public static final String PARENT_TKVERSION_ID_COL = "PARENT_TKVERSION";
    public static final String ALL_COLS = ID_COL + "," + NAME_COL + "," +
    RELEASE_ID_COL + "," + 
    STAGE_NAME_ID_COL + "," +
    CQ_RELEASE_NAME_COL + "," + 
    DESCRIPTION_COL + "," +
    PARENT_TKVERSION_ID_COL + "," + 
    CREATED_BY_COL + "," + CREATED_ON_COL + "," + 
    UPDATED_BY_COL + "," + UPDATED_ON_COL + "," +
    DELETED_BY_COL + "," + DELETED_ON_COL;


    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public RelVersion_Db(short anId) {
	setId(anId);
    }


    /**
     * Constructor - takes a Server or ASIC ToolKit name
     * 
     * @param xContext Application context
     * @param aName    ToolKit (like 14.1.1, 14.1.2, 14.1.3 ...)
     * @throws IcofException 
     */
    public RelVersion_Db(EdaContext xContext, String aName)
    throws IcofException {

	verifyTkString(xContext, aName);
	setReleaseFromString(xContext, aName);
	setVersionFromString(xContext, aName);

    }


    /**
     * Constructor - takes a Server or ASIC ToolKit name and StageName
     * 
     * @param xContext   Application context
     * @param aName      ToolKit (like 14.1.1, 14.1.2, 14.1.3 ...)
     * @param aStageName A StageName_db object
     * @throws IcofException 
     */
    public RelVersion_Db(EdaContext xContext, String aName, 
                         StageName_Db aStageName)
                         throws IcofException {

	verifyTkString(xContext, aName);
	setReleaseFromString(xContext, aName);
	setVersionFromString(xContext, aName);
	setStageName(aStageName);

    }


    /**
     * Constructor - takes all data members
     * 
     * @param xContext   Application context
     * @param aName      ToolKit like 14.1.1, 14.1.2, 14.1.3 ...
     * @param aStageName A StageName_db object
     * @param aCqName    ClearQuest release name like 14.1.1 (dev)
     * @param aDesc      TK version description
     * @param aParentId  This version's parent id
     * @throws IcofException 
     */
    public RelVersion_Db(EdaContext xContext, String aName, 
                         StageName_Db aStageName, String aCqName,
                         String aDesc, short aParentId)
                         throws IcofException {

	String tkName = aName;
	if (aName == null || aName.equals("")) {
	    int index = aCqName.indexOf(" ");
	    if (index > -1) {
		tkName = aCqName.substring(0, index);
	    }
	    else {
		tkName = aCqName;
	    }
	}

	verifyTkString(xContext, tkName);
	setReleaseFromString(xContext, tkName);
	setVersionFromString(xContext, tkName);
	setStageName(aStageName);
	setCqReleaseName(aCqName);
	setDescription(aDesc);
	setParentId(aParentId);

    }


    /**
     * Set the Version from the ToolKit string
     * 
     * @param xContext Application context
     * @param aName    RelVersoin string (14.1.0 ...)
     * @throws IcofException 
     */
    private void setVersionFromString(EdaContext xContext, String aName) {
	
	String[] tokens = aName.split("[.]");
	if (tokens.length == 3) {
	    setName(tokens[2]);
	}
	else {
	    setName(tokens[2] + "." + tokens[3]);
	}

    }


    /**
     * Set the Release from the ToolKit string
     * 
     * @param xContext Application context
     * @param aName    RelVersoin string (14.1.0 ...)
     * @throws IcofException 
     */
    private void setReleaseFromString(EdaContext xContext, String aName)
    throws IcofException {

	// Set release.
	String[] tokens = aName.split("[.]");
	String releaseName = tokens[0] + "." + tokens[1];

	release = new Release_Db(xContext, releaseName, releaseName);
	try {
	    // Query for the ASIC TK release name
	    release.dbLookupByName(xContext, false);
	}
	catch(IcofException ie) {
	    // Query for the Server TK release name
	    release.dbLookupByName(xContext, true);
	}

    }


    /**
     * Verify format of ToolKit string
     * 
     * @param xContext Application context
     * @param aName    RelVersoin string (14.1.0 ...)
     * @throws IcofException 
     */
    private void verifyTkString(EdaContext xContext, String aName) 
    throws IcofException {
	
	int numDots = IcofStringUtil.occurrencesOf(aName, ".");

	if ((numDots < 2) || (numDots > 3)) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "Constructor - RelVersion_Db",
	                                         IcofException.SEVERE,
	                                         "Incorrect ToolKit name (not x.y.z or x.y.z.<patch>)", aName);
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

    }


    /**
     * Constructor - takes a TKRelease object and a TK Version name
     * 
     * @param aRelease   A TkRelease object
     * @param aName      Name of a TK Version (1, 2, 3 ...)
     */
    public RelVersion_Db(Release_Db aRelease, String aName) {
	setName(aName);
	setRelease(aRelease);
    }


    /**
     * Constructor - takes a TKRelease object, a TK Version name and a Stage name.
     * 
     * @param aRelease   A TkRelease object
     * @param aName      Name of a TK Version (1, 2, 3 ...)
     * @param aStageName A StageName_db object
     */
    public RelVersion_Db(Release_Db aRelease, String aName, 
                         StageName_Db aStageName) {
	setName(aName);
	setRelease(aRelease);
	setStageName(aStageName);
    }


    /**
     * Constructor - takes a TKRelease id a TK Version name and a StageName id.
     * 
     * @param xContext  Application context object.
     * @param aVerId    A TkVersion db id
     * @param aRelId    A TkRelease db id
     * @param aName     Name of a TK Version (1, 2, 3 ...)
     * @param aStageId  A StageName db id
     */
    public RelVersion_Db(EdaContext xContext, short aVerId, short aRelId, 
                         String aName, short aStageId, String cqRelName,
                         String aDescription, short aParentId) {
	setId(aVerId);
	setName(aName);
	setRelease(xContext, aRelId);
	setStageName(xContext, aStageId);
	setCqReleaseName(cqRelName);
	setDescription(aDescription);
	setParentId(aParentId);
    }



    /**
     * Data Members
     */
    private short id;
    private String name;
    private Release_Db release;
    private StageName_Db stageName;
    private String cqReleaseName;
    private String description;
    private short parentId;


    /**
     * Getters
     */
    public String getName() { return name; }
    public short getId() { return id; }
    public Release_Db getRelease() { return release; }
    public StageName_Db getStageName() { return stageName; }
    public String getCqReleaseName() { return cqReleaseName; }
    public String getDescription() { return description; }
    public Short getParentId() { return parentId; }


    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    private void setId(short anId) { id = anId; }
    private void setRelease(Release_Db aRel) { release = aRel; }
    private void setStageName(StageName_Db aName) { stageName = aName; }
    private void setRelease(EdaContext xContext, short anId) {
	if (release == null)
	    release = new Release_Db(anId);
    }
    private void setStageName(EdaContext xContext, short anId) {
	if (stageName == null)
	    stageName = new StageName_Db(anId);
    }
    private void setParentId(short anId) { parentId = anId; }
    private void setCqReleaseName(String aName) { cqReleaseName = aName; }
    private void setDescription(String aDesc) { description = aDesc; }


    /**
     * Create a PreparedStatement to delete this object
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setDeleteStatement(EdaContext xContext) throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME + 
	" set " + 
	DELETED_BY_COL + " = ? , " +
	DELETED_ON_COL + " = ? " +
	" where " + ID_COL + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by id.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupIdStatement(EdaContext xContext) throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + 
	" from " + TABLE_NAME + 
	" where " + ID_COL + " = ? " +
	" AND " + DELETED_ON_COL + " is NULL";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by name.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupNameStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + 
	" from " + TABLE_NAME + 
	" where UCASE(" + NAME_COL + ") = ? " +
	" AND " + RELEASE_ID_COL + " = ? " +
	" AND " + DELETED_ON_COL + " is NULL";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by ClearQuest 
     * release name.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupCqNameStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + 
	" from " + TABLE_NAME + 
	" where UCASE(" + CQ_RELEASE_NAME_COL + ") = ? " +
	" AND " + DELETED_ON_COL + " is NULL";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by branch name.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupBranchStatement(EdaContext xContext) 
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + 
	" from " + TABLE_NAME + 
	" where " + BRANCH_COL + " = ? " +
	" AND " + DELETED_ON_COL + " is NULL";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by Release.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupByReleaseStatement(EdaContext xContext) 
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + 
	" from " + TABLE_NAME + 
	" where " + RELEASE_ID_COL + " =  ?" + 
	" AND " + DELETED_ON_COL + " is NULL";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by StageName.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupByStageNameStatement(EdaContext xContext) 
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + 
	" from " + TABLE_NAME + 
	" where " + STAGE_NAME_ID_COL + " = ? " +
	" AND " + RELEASE_ID_COL + " =  ? " + 
	" AND " + DELETED_ON_COL + " is NULL";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by Release and StageName.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupByReleaseStageNameStatement(EdaContext xContext) 
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + 
	" from " + TABLE_NAME + 
	" where " + STAGE_NAME_ID_COL + " = ? " +
	" AND " + RELEASE_ID_COL + " = ? " +
	" AND " + DELETED_ON_COL + " is NULL";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to update the Name.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setUpdateNameStatement(EdaContext xContext) 
    throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME + 
	" set " + NAME_COL + " = ?, " +
	CQ_RELEASE_NAME_COL + " = ?, " +
	DESCRIPTION_COL + " = ?, " +
	UPDATED_BY_COL + " = ?, " +
	UPDATED_ON_COL + " = ? " +
	" where " + ID_COL + " = ?";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to update the StageName.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setUpdateStageNameStatement(EdaContext xContext) 
    throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME + 
	" set " + STAGE_NAME_ID_COL + " = ?, " +
	UPDATED_BY_COL + " = ?, " +
	UPDATED_ON_COL + " = ? " +
	" where " + ID_COL + " = ?";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup the next id for this table.

     * @param xContext  Application context.
     * @return PreparedStatement
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
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setAddRowStatement(EdaContext xContext) throws IcofException {

	// Define the query.
	String query = "insert into " + TABLE_NAME + 
	" ( " +  ALL_COLS + " )" + 
	" values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Look up the next id for this table.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupNextId(EdaContext xContext) throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setNextIdStatement(xContext);

    }


    /**
     * Look up this object by id.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupById(EdaContext xContext) throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setLookupIdStatement(xContext);

	try {
	    getStatement().setLong(1, getId());
	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "dbLookupById()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap) + 
	                                         "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (! selectSingleRow(xContext)) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
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
     * Look up this object by name.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByName(EdaContext xContext) throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setLookupNameStatement(xContext);

	try {
	    getStatement().setString(1, getName().toUpperCase());
	    getStatement().setShort(2, getRelease().getId());

	    //printQuery(xContext);
	    //System.out.println("Name: " + getName());
	    //System.out.println("Rel id: " + getRelease().getId());

	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "dbLookupByName()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap) + 
	                                         "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (! selectSingleRow(xContext)) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
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
     * Look up this object by ClearQuest release name.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByCqName(EdaContext xContext) throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setLookupCqNameStatement(xContext);

	//printQuery(xContext);
	//System.out.println("CQ Name: " + getCqReleaseName().toUpperCase());
	//System.out.println("Rel id: " + getRelease().getId());


	try {
	    getStatement().setString(1, getCqReleaseName().toUpperCase());
	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "dbLookupByCqName()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap) + 
	                                         "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (! selectSingleRow(xContext)) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "dbLookupByCqName()",
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
     * Insert a new row.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
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
	    getStatement().setString(2, getName());
	    getStatement().setShort(3, getRelease().getId());
	    getStatement().setShort(4, getStageName().getId());
	    getStatement().setString(5, getCqReleaseName());
	    getStatement().setString(6, getDescription());
	    getStatement().setShort(7, getParentId());
	    getStatement().setString(8, user.getIntranetId());
	    getStatement().setTimestamp(9, now);
	    getStatement().setString(10, user.getIntranetId());
	    getStatement().setTimestamp(11, now);
	    getStatement().setString(12, null);
	    getStatement().setString(13, null);

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

	//  Load the data for the new row.
	dbLookupById(xContext); 

    }


    /**
     * Create a list of RelVersion_Db objects for the given stage name.
     * 
     * @param xContext   An application context object.
     * @param aStageName A StageName_Db object.
     * @return           Collection of RelVersion_Db objects.
     * @throws           Trouble querying the database.
     */
    public Vector<RelVersion_Db> dbLookupByStageName(EdaContext xContext, StageName_Db stageName)
    throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setLookupByStageNameStatement(xContext);

	try {
	    getStatement().setLong(1, stageName.getId());
	    getStatement().setShort(2, getRelease().getId());

	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "dbLookupByStageName()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap) + 
	                                         "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Vector<RelVersion_Db> versions = new Vector<RelVersion_Db>();
	try {
	    while (rs.next()) {
		short anId =  rs.getShort(ID_COL);
		String aName = rs.getString(NAME_COL);
		short relId = rs.getShort(RELEASE_ID_COL);
		short stageId = rs.getShort(STAGE_NAME_ID_COL);
		String cqName = rs.getString(CQ_RELEASE_NAME_COL);                                             
		String desc = rs.getString(DESCRIPTION_COL);
		short parent = rs.getShort(PARENT_TKVERSION_ID_COL);

		RelVersion_Db version = new RelVersion_Db(xContext, anId, relId,
		                                          aName, stageId, cqName,
		                                          desc, parent);
		versions.add(version);
	    }

	}
	catch(SQLException ex) {
	    throw new IcofException(this.getClass().getName(), "dbLookupByStageName()",
	                            IcofException.SEVERE, 
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return versions;

    }


    /**
     * Create a list of RelVersion_Db objects for the given Release.
     * 
     * @param xContext   An application context object.
     * @return           Collection of RelVersion_Db objects.
     * @throws           Trouble querying the database.
     */
    public Hashtable<String,RelVersion_Db> dbLookupByRelease(EdaContext xContext)
    throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setLookupByReleaseStatement(xContext);

	try {
	    getStatement().setShort(2, getRelease().getId());
	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "dbLookupByRelease()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap) + 
	                                         "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Hashtable<String,RelVersion_Db> tks = new Hashtable<String,RelVersion_Db>();
	try {
	    while (rs.next()) {
		short anId =  rs.getShort(ID_COL);
		RelVersion_Db tk = new RelVersion_Db(anId);
		tk.dbLookupById(xContext);

		tks.put(tk.getIdKey(xContext), tk);
	    }

	}
	catch(SQLException ex) {
	    throw new IcofException(this.getClass().getName(), "dbLookupByRelease()",
	                            IcofException.SEVERE, 
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return tks;

    }


    /**
     * Create a list of RelVersion_Db objects for the given release and stage name.
     * 
     * @param xContext   An application context object.
     * @return           Collection of RelVersion_Db objects.
     * @throws           Trouble querying the database.
     */
    public Hashtable<String,RelVersion_Db> dbLookupByReleaseStageName(EdaContext xContext)
    throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setLookupByReleaseStageNameStatement(xContext);

	try {
	    getStatement().setLong(1, getStageName().getId());
	    getStatement().setShort(2, getRelease().getId());
	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "dbLookupByReleaseStageName()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap) + 
	                                         "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Hashtable<String,RelVersion_Db> versions = new Hashtable<String,RelVersion_Db>();
	try {
	    while (rs.next()) {
		short anId =  rs.getShort(ID_COL);
		RelVersion_Db version = new RelVersion_Db(anId);
		version.dbLookupById(xContext);

		versions.put(version.getIdKey(xContext), version);
	    }

	}
	catch(SQLException ex) {
	    throw new IcofException(this.getClass().getName(), 
	                            "dbLookupByReleaseStageName()",
	                            IcofException.SEVERE, 
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return versions;

    }


    /**
     * Update the name for this object.
     * 
     * @param xContext   An application context object.
     * @param aStageName A StageName_Db object.
     * @param editor     User updating this object.
     * @throws IcofException  Trouble querying the database.
     */
    public void dbUpdate(EdaContext xContext, String newName, String newCqRelName,
                         String newDescription, User_Db aUser) 
                         throws IcofException {

	// Create the SQL query in the PreparedStatement.
	Timestamp now = new Timestamp(new java.util.Date().getTime());
	setUpdateNameStatement(xContext);
	try {
	    getStatement().setString(1, newName);
	    getStatement().setString(2, newCqRelName);
	    getStatement().setString(3, newDescription);
	    getStatement().setString(4, aUser.getIntranetId());
	    getStatement().setTimestamp(5, now);
	    getStatement().setShort(6, getId());

	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "dbUpdateName()",
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
	                                         "dbUpdateName()",
	                                         IcofException.SEVERE,
	                                         "Unable to update selected row.\n",
	                                         "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	// Set the current status to the new status.
	setName(newName);

    }


    /**
     * Update the stage name for this object.
     * 
     * @param xContext   An application context object.
     * @param aStageName A StageName_Db object.
     * @param aUser      A User_Db object.
     * @throws IcofException 
     * @throws           Trouble querying the database.
     */
    public void dbUpdateStageName(EdaContext xContext, StageName_Db stageName, 
                                  User_Db aUser) 
                                  throws IcofException {

	// Create the SQL query in the PreparedStatement.
	Timestamp now = new Timestamp(new java.util.Date().getTime());
	setUpdateStageNameStatement(xContext);
	try {
	    getStatement().setShort(1, stageName.getId());
	    getStatement().setString(2, aUser.getEmailAddress());
	    getStatement().setTimestamp(3, now);
	    getStatement().setShort(4, getId());

	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "dbUpdateStageName()",
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
	                                         "dbUpdateStageName()",
	                                         IcofException.SEVERE,
	                                         "Unable to update selected row.\n",
	                                         "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	// Set the current status to the new status.
	setStageName(stageName);
	setUpdatedBy(aUser.getIntranetId());
	setUpdatedOn(now);
	setLoadFromDb(true);

    }


    /**
     * Delete (mark as deleted) this object in the database
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbDeleteRow(EdaContext xContext, User_Db editor)
    throws IcofException{

	// Create the SQL query in the PreparedStatement.
	Timestamp now = new Timestamp(new java.util.Date().getTime());
	setDeleteStatement(xContext);
	try {
	    getStatement().setString(1, editor.getIntranetId());
	    getStatement().setTimestamp(2, now);
	    getStatement().setLong(3, getId());

	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "dbDeleteRow()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap) + 
	                                         "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (! insertRow(xContext)) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "dbDeleteRow()",
	                                         IcofException.SEVERE,
	                                         "Unable to delete row.\n",
	                                         "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	// Set the delete info on this object.
	setDeletedBy(editor.getIntranetId());
	setDeletedOn(now);

    }


    /**
     * Populate this object from the result set.
     * 
     * @param xContext  Application context.
     * @param rs        A valid result set.
     * @throws IcofException 
     * @throws IcofException 
     * @throws          Trouble retrieving the data.
     */
    protected void populate(EdaContext xContext, ResultSet rs) 
    throws SQLException, IcofException {

	super.populate(xContext, rs);
	setId(rs.getShort(ID_COL));
	setName(rs.getString(NAME_COL));
	setRelease(xContext, rs.getShort(RELEASE_ID_COL));
	setStageName(xContext, rs.getShort(STAGE_NAME_ID_COL));
	setCqReleaseName(rs.getString(CQ_RELEASE_NAME_COL));
	setDescription(rs.getString(DESCRIPTION_COL));
	setParentId(rs.getShort(PARENT_TKVERSION_ID_COL));
	setLoadFromDb(true);

    }


    /**
     * Return the members as a string.
     * @throws IcofException 
     */
    public String toString(EdaContext xContext) {

	// Get the audit data
	String audit = super.toString(xContext);

	// Get the class specific data
	StringBuffer buffer = new StringBuffer();
	buffer.append("ID: " + getId() + "\n");
	if (getRelease() != null) {
	    buffer.append("Release ID: " + getRelease().getId() + "\n");
	    try {
		if (! getRelease().isLoaded())
		    getRelease().dbLookupById(xContext);
	    }
	    catch(IcofException ignore) {}
	    buffer.append("Release name: " + getRelease().getName() + "\n");
	    buffer.append("Alt release name: " + getRelease().getAltName() + "\n");
	}
	else {
	    buffer.append("Release ID: NULL" + "\n");
	    buffer.append("Release name: NULL" + "\n");
	    buffer.append("Alt release name: NULL" + "\n");
	}
	buffer.append("Minor version: " + getName() + "\n");
	if (getStageName() != null) {
	    buffer.append("StageName ID: " + getStageName().getId() + "\n");
	    try {
		if (! getStageName().isLoaded())
		    getStageName().dbLookupById(xContext);
	    }
	    catch(IcofException ignore) {}
	    buffer.append("StageName   : " + getStageName().getName() + "\n");
	}
	else {
	    buffer.append("StageName ID: NULL" + "\n");
	    buffer.append("StageName   : NULL" + "\n");
	}
	if (getCqReleaseName() != null) {
	    buffer.append("CQ Release Name: " + getCqReleaseName() + "\n");
	}
	else {
	    buffer.append("CQ Release Name: NULL" + "\n");
	}
	if (getDescription() != null) {
	    buffer.append("Description    : " + getDescription() + "\n");
	}
	else {
	    buffer.append("Description    : NULL" + "\n");
	}
	if (getParentId() != null) {
	    buffer.append("Parent TK id   : " + getParentId() + "\n");
	}
	else {
	    buffer.append("Parent Tk id   : NULL" + "\n");
	}

	buffer.append(audit);

	return buffer.toString();

    }


    /**
     * Format the Release and version for display.
     * 
     * @param xContext  An application context object.
     * @throws IcofException 
     * @throws          Trouble querying the database.
     */
    public String getDisplayName() {
	return getRelease().getName() + "." + getName();
    }


    /**
     * Format the Release and version for display.
     * 
     * @param xContext  An application context object.
     * @throws IcofException 
     * @throws          Trouble querying the database.
     */
    public String getPackagingName() throws IcofException {

	int padSize = getName().length();

	String tkNameAsNum = "";
	for (int i = 0; i < getName().length(); i++) {
	    if ((getName().charAt(i) >= '0') && (getName().charAt(i) <= '9')) {
		tkNameAsNum += getName().charAt(i);
	    }
	}
	if (Integer.parseInt(tkNameAsNum) < 10)
	    padSize++;

	String relName = getRelease().getPackagingName() +
	IcofStringUtil.padString(getName(), padSize, "0", true);

	return relName;
    }



    /**
     * Format the Alternate Release and version for display.
     * 
     * @param xContext  An application context object.
     * @throws IcofException 
     * @throws          Trouble querying the database.
     */
    public String getAltDisplayName() {
	return getRelease().getAltName() + "." + getName();
    }


    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
	return String.valueOf(getId());
    }


}
