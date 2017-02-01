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
 * Component_Version class with audit info
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 01/06/2011 GFS  Initial coding.
 * 04/11/2011 GFS  Changed id from short to long.
 * 04/14/2011 GFS  Added dbDeleteRow() method.
 * 06/09/2011 GFS  Updated dbLookupComponents to be more efficient.
 * 07/25/2011 GFS  Updated the query in setLookupComponentsStatement() to sort
 *                 the results.
 * 10/05/2011 GFS  Added dbLookupAllComponents and updated dbLookupComponents
 *                 to filter by Component Type.
 * 12/20/2011 GFS  Updated dbLookupCompToolKits() to include a stage name filter.
 *=============================================================================
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

public class Component_Version_Db extends TkAudit {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1083302408934977878L;
    /**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.COMPONENT_TKVERSION";
    public static final String ID_COL = "COMPONENT_TKVERSION_ID";
    public static final String REL_COMP_ID_COL = "COMPONENT_TKRELEASE_ID";
    public static final String VERSION_ID_COL = "TKVERSION_ID";
    public static final String STAGE_NAME_ID_COL = "STAGENAME_ID";
    public static final String ALL_COLS = ID_COL + "," + REL_COMP_ID_COL + ","
					  + VERSION_ID_COL + ","
					  + STAGE_NAME_ID_COL + ","
					  + CREATED_BY_COL + ","
					  + CREATED_ON_COL + ","
					  + UPDATED_BY_COL + ","
					  + UPDATED_ON_COL + ","
					  + DELETED_BY_COL + ","
					  + DELETED_ON_COL;


    /**
     * Constructor - takes a DB id
     * 
     * @param anId A database id
     */
    public Component_Version_Db(long anId) {

	setId(anId);
    }


    /**
     * Constructor - takes TK Version and Component_Release objects.
     * 
     * @param aVersion A RelVersion object
     * @param aReleaseComponent A Component_Release object
     */
    public Component_Version_Db(RelVersion_Db aVersion,
				Component_Release_Db aRelComp) {

	setCompRelease(aRelComp);
	setVersion(aVersion);

    }
    

    /**
     * Constructor - takes TK Release and Component objects.
     * 
     * @param anId An id
     * @param aVersion A TkRelease object
     * @param aComponent A TkComponent object
     * @param aStageName A StageName object
     */
    public Component_Version_Db(short anId, RelVersion_Db aVersion,
				Component_Release_Db aRelComp,
				StageName_Db aStageName) {

	setId(anId);
	setCompRelease(aRelComp);
	setVersion(aVersion);
	setStageName(aStageName);
    }


    /**
     * Constructor - takes TK Release and Component IDs
     * 
     * @param xContext Application context object.
     * @param aVersionId Id of a RelVersion database row
     * @param aRelCompId Id of a RelComponent database row
     * @param aStageName Id of a StageName database row
     * 
     * @throws Trouble looking up the TkRelease or Component objects
     */
    public Component_Version_Db(EdaContext xContext, short aVersionId,
				short aRelCompId, short aStageNameId)
    throws IcofException {

	setVersion(xContext, aVersionId);
	setCompRelease(xContext, aRelCompId);
	setStageName(xContext, aStageNameId);
    }


    /**
     * Constructor - takes TK Version.
     * 
     * @param anId An id
     * @param aVersion A TkRelease object
     */
    public Component_Version_Db(RelVersion_Db aVersion) {

	setVersion(aVersion);
    }


    /**
     * Constructor - takes Component_Release_Db and RelVersion_Db objects.
     * 
     * @param aCompRelease A Component_Release_Db object.
     * @param aVersion A RelVersion_Db object
     */
    public Component_Version_Db(Component_Release_Db aCompRelease,
				RelVersion_Db aVersion) {

	setVersion(aVersion);
	setCompRelease(aCompRelease);
    }


    /**
     * Constuctor - takes RelVersion and Component
     * 
     * @param xContext EdaContext object
     * @param aVersion ToolKit version
     * @param aComponent Component
     * @throws IcofException
     */
    public Component_Version_Db(EdaContext xContext, RelVersion_Db aVersion,
				Component_Db aComponent) throws IcofException {

	// Lookup the ComponentRelease
	Component_Release_Db compRel;
	compRel = new Component_Release_Db(aVersion.getRelease(), aComponent);
	compRel.dbLookupByRelComp(xContext);

	setCompRelease(compRel);
	setVersion(aVersion);
	setStageName(aVersion.getStageName());

    }


    /**
     * Constuctor - takes RelVersion, Component and StageName
     * 
     * @param xContext EdaContext object
     * @param aVersion ToolKit version
     * @param aComponent A component
     * @param aStageName A StageName
     * @throws IcofException
     */
    public Component_Version_Db(EdaContext xContext, RelVersion_Db aVersion,
				Component_Db aComponent, StageName_Db aStageName)
    throws IcofException {

	// Lookup the ComponentRelease
	Component_Release_Db compRel = new Component_Release_Db(aVersion.getRelease(),
								aComponent);
	compRel.dbLookupByRelComp(xContext);
	
	setCompRelease(compRel);
	setVersion(aVersion);
	setStageName(aStageName);

    }

    /**
     * Constuctor - takes RelVersion, Component and StageName
     * 
     * @param xContext EdaContext object
     * @param aVersion ToolKit version
     * @param aComponent A component
     * @param aStageName A StageName
     * @throws IcofException
     */
    public Component_Version_Db(EdaContext xContext, RelVersion_Db aVersion,
				Component_Db aComponent, StageName_Db aStageName,
				User_Db user)
    throws IcofException {

	// Lookup the ComponentRelease
	Component_Release_Db compRel = new Component_Release_Db(aVersion.getRelease(),
								aComponent);
	try {
	    compRel.dbLookupByRelComp(xContext);
	}
	catch(IcofException ie) {
	    compRel.dbAddRow(xContext, user);
	}
	
	setCompRelease(compRel);
	setVersion(aVersion);
	setStageName(aStageName);

    }


    /**
     * Data Members
     * @formatter:off
     */
    private long id;
    private RelVersion_Db version;
    private Component_Release_Db compRel;
    private StageName_Db stageName;

    
    /**
     * Getters
     */
    public long getId() { return id; }
    public RelVersion_Db getVersion() { return version; }
    public Component_Release_Db getCompRelease() { return compRel; }
    public StageName_Db getStageName() { return stageName; }


    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setVersion(RelVersion_Db aVer) { version = aVer; }
    private void setCompRelease(Component_Release_Db aCompRel) { compRel = aCompRel; }
    private void setStageName(StageName_Db aStageName) { stageName = aStageName; }
    private void setVersion(EdaContext xContext, short anId) {
    	version = new RelVersion_Db(anId);
    }
    private void setCompRelease(EdaContext xContext, short anId) {
    	compRel = new Component_Release_Db(anId);
    }
    private void setStageName(EdaContext xContext, short anId) {
    	stageName = new StageName_Db(anId);
    }
    // @formatter:on

    /**
     * Create a PreparedStatement to lookup this object by id.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupIdStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
		       + ID_COL + " = ? " + " AND " + DELETED_ON_COL
		       + " is NULL";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by ids.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupAllStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
		       + REL_COMP_ID_COL + " =  ? " + " AND " + VERSION_ID_COL
		       + " =  ? " + " AND " + STAGE_NAME_ID_COL + " =  ? "
		       + " AND " + DELETED_ON_COL + " is NULL";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by ids.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupCompRelVersionStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + 
	               " from " + TABLE_NAME + 
	               " where " + REL_COMP_ID_COL + " =  ? " + 
	               " AND " + VERSION_ID_COL + " =  ? " + 
	               " AND " + DELETED_ON_COL + " is NULL";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by id.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupCompRelStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
		       + REL_COMP_ID_COL + " =  ? " + " AND " + DELETED_ON_COL
		       + " is NULL";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by id.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupVersionStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
		       + VERSION_ID_COL + " =  ? " + " AND " + DELETED_ON_COL
		       + " is NULL";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by RelVersion id.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupAllComponentsStatement(EdaContext xContext)
    throws IcofException {

	// select cr.component_id, c.component_name
	// from tk.component_tkversion as cv,
	// tk.component_tkrelease as cr,
	// tk.component as c
	// where cv.TKVERSION_ID = 1
	// AND cv.DELETED_BY is NULL
	// AND cv.component_tkrelease_id = cr.component_tkrelease_id
	// AND cr.component_id = c.component_id
	// order by c.component_name

	// Define the query.
	String query = "select c." + Component_Db.ID_COL + ", " + "c."
		       + Component_Db.NAME_COL + " from " + TABLE_NAME
		       + " as cv, " + Component_Release_Db.TABLE_NAME
		       + " as cr, " + Component_Db.TABLE_NAME + " as c "
		       + " where cv." + VERSION_ID_COL + " =  ? " + " AND cv."
		       + DELETED_BY_COL + " is NULL" + " AND cv."
		       + REL_COMP_ID_COL + " = cr."
		       + Component_Release_Db.ID_COL + " AND cv."
		       + DELETED_BY_COL + " is NULL" + " AND cr."
		       + Component_Release_Db.COMP_ID_COL + " = c."
		       + Component_Db.ID_COL + " order by c."
		       + Component_Db.NAME_COL + " asc";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by RelVersion id and
     * filter by the Component Type.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupComponentsStatement(EdaContext xContext)
    throws IcofException {

	// select cr.component_id, c.component_name
	// from tk.component_tkversion as cv,
	// tk.component_tkrelease as cr,
	// tk.component as c,
	// tk.component_type_x_component_tkrelease as ctcr
	// where cv.TKVERSION_ID = 1
	// AND cv.DELETED_BY is NULL
	// AND cv.component_tkrelease_id = cr.component_tkrelease_id
	// AND cr.component_id = c.component_id
	// AND ctcr.component_type_id = 1
	// AND ctcr.component_tkrelease_id = cr.component_tkrelease_id
	// order by c.component_name

	// Define the query.
	String query = "select c."
		       + Component_Db.ID_COL
		       + ", "
		       + "c."
		       + Component_Db.NAME_COL
		       + " from "
		       + TABLE_NAME
		       + " as cv, "
		       + Component_Release_Db.TABLE_NAME
		       + " as cr, "
		       + Component_Db.TABLE_NAME
		       + " as c, "
		       + ComponentType_ComponentRelease_Db.TABLE_NAME
		       + " as ctcr "
		       + " where cv."
		       + VERSION_ID_COL
		       + " =  ? "
		       + " AND cv."
		       + DELETED_BY_COL
		       + " is NULL"
		       + " AND cv."
		       + REL_COMP_ID_COL
		       + " = cr."
		       + Component_Release_Db.ID_COL
		       + " AND cv."
		       + DELETED_BY_COL
		       + " is NULL"
		       + " AND cr."
		       + Component_Release_Db.COMP_ID_COL
		       + " = c."
		       + Component_Db.ID_COL
		       + " AND ctcr."
		       + ComponentType_ComponentRelease_Db.COMPONENT_TYPE_ID_COL
		       + " = ? "
		       + " AND ctcr."
		       + ComponentType_ComponentRelease_Db.COMPONENT_RELEASE_ID_COL
		       + " = cr." + Component_Release_Db.ID_COL
		       + " order by c." + Component_Db.NAME_COL + " asc";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup objects for this Component id.
     * 
     * @param xContext Application context.
     * @param stageFilter If true filter by stage also
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupCompVersionsStatement(EdaContext xContext,
					       boolean stageFilter)
    throws IcofException {

	// select cv.component_tkversion_id
	// from tk.component_tkversion as cv,
	// tk.component_tkrelease as cr
	// where cr.component_tkrelease_id = cv.component_tkrelease_id
	// and cr.component_id = 2
	// and cv.DELETED_BY is null
	// Stage filter
	// and cv.stagename_id = 4

	// Define the query.
	String query = "select cv." + ID_COL + " from " + TABLE_NAME
		       + " as cv, " + Component_Release_Db.TABLE_NAME
		       + " as cr " + " where cr." + Component_Release_Db.ID_COL
		       + " =  cv." + Component_Version_Db.REL_COMP_ID_COL
		       + " and cr." + Component_Release_Db.COMP_ID_COL
		       + " = ? " + " and cv." + DELETED_BY_COL + " is null";

	if (stageFilter) {
	    query += " and cv." + Component_Version_Db.STAGE_NAME_ID_COL
		     + " = ? ";
	}

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to update the StageName.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setUpdateStageNameStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME + " set " + STAGE_NAME_ID_COL
		       + " = ?, " + UPDATED_BY_COL + " = ?, " + UPDATED_ON_COL
		       + " = ? " + " where " + ID_COL + " = ?";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to add a row.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setAddRowStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "insert into " + TABLE_NAME + " ( " + ALL_COLS + " )"
		       + " values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to delete this object
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setDeleteRowStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME + " set " + DELETED_BY_COL
		       + " = ? , " + DELETED_ON_COL + " = ? " + " where "
		       + ID_COL + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup the next id for this table.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setNextIdStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = TkAudit.getNextIdQuery(xContext, TABLE_NAME, ID_COL);

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Look up the Component by id.
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbLookupById(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupIdStatement(xContext);

	try {
	    getStatement().setLong(1, getId());
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
     * @throws Trouble querying the database.
     */
    public void dbLookupByAll(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupAllStatement(xContext);

	try {
	    getStatement().setShort(1, getCompRelease().getId());
	    getStatement().setShort(2, getVersion().getId());
	    getStatement().setShort(3, getStageName().getId());

	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupByAll()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap) + "\n" + 
						 getQuery() + "\n");
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (!selectSingleRow(xContext)) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupByAll()",
						 IcofException.SEVERE,
						 "Unable to find row for query.\n",
						 "QUERY: " + getQuery() + "\n" +
						 "Comp Rel id  : " + getCompRelease().getId() + "\n" +
						 "Version id   : " + getVersion().getId() + "\n" +
						 "Stage name id: " + getStageName().getId() + "\n");
	    xContext.getSessionLog().log(ie);
	    throw ie;

	}

	// Close the PreparedStatement.
	closeStatement(xContext);

    }


    /**
     * Look up Component_Release and RelVersion.
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbLookupByCompRelVersion(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupCompRelVersionStatement(xContext);

	try {
	    getStatement().setShort(1, getCompRelease().getId());
	    getStatement().setShort(2, getVersion().getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupByCompRelVersion()",
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
						 "dbLookupByCompRelVersion()",
						 IcofException.SEVERE,
						 "Unable to find row for query.\n",
						 "QUERY: " + getQuery() + "\n" + 
						 "Comp Rel id: " + getCompRelease().getId() + "\n" +
						 "TK id: " + getVersion().getId());
	    	
	    xContext.getSessionLog().log(ie);
	    throw ie;

	}

	// Close the PreparedStatement.
	closeStatement(xContext);

    }


    /**
     * Look up the Component_Version objects by ReleaseComponent.
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public Hashtable<String, Component_Version_Db> dbLookupByCompRel(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupCompRelStatement(xContext);

	try {
	    getStatement().setShort(1, getCompRelease().getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupByCompRel()",
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
	Hashtable<String, Component_Version_Db> results = new Hashtable<String, Component_Version_Db>();
	try {
	    while (rs.next()) {
		short aStageNameId = rs.getShort(STAGE_NAME_ID_COL);
		StageName_Db stageName = new StageName_Db(aStageNameId);
		stageName.dbLookupById(xContext);

		short aVersionId = rs.getShort(VERSION_ID_COL);
		RelVersion_Db version = new RelVersion_Db(aVersionId);
		version.dbLookupById(xContext);

		Component_Version_Db myCompVer = new Component_Version_Db(
									  xContext,
									  aVersionId,
									  getCompRelease().getId(),
									  aStageNameId);

		results.put(myCompVer.getIdKey(xContext), myCompVer);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupByCompRel()",
				    IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return results;

    }


    /**
     * Look up the Component by name.
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public Hashtable<String, Component_Version_Db> dbLookupByVersion(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupVersionStatement(xContext);

	try {
	    getStatement().setShort(1, getVersion().getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupByVersion()",
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
	Hashtable<String, Component_Version_Db> results = new Hashtable<String, Component_Version_Db>();
	try {
	    while (rs.next()) {
		short aStageNameId = rs.getShort(STAGE_NAME_ID_COL);
		StageName_Db stageName = new StageName_Db(aStageNameId);
		stageName.dbLookupById(xContext);

		short aCompRelId = rs.getShort(REL_COMP_ID_COL);
		Component_Release_Db release = new Component_Release_Db(
									aCompRelId);
		release.dbLookupById(xContext);

		Component_Version_Db myCompVer = new Component_Version_Db(
									  xContext,
									  getVersion().getId(),
									  aCompRelId,
									  aStageNameId);

		results.put(myCompVer.getIdKey(xContext), myCompVer);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupByVersion()",
				    IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return results;

    }


    /**
     * Create a list of Component_Db objects for the given ToolKit.
     * 
     * @param xContext An application context object.
     * @return Collection of Component_Db objects.
     * @throws Trouble querying the database.
     */
    public Vector<Component_Db> dbLookupAllComponents(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupAllComponentsStatement(xContext);

	try {
	    getStatement().setShort(1, getVersion().getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupComponents()",
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
	Vector<Component_Db> components = new Vector<Component_Db>();
	try {
	    while (rs.next()) {
		short anId = rs.getShort(Component_Db.ID_COL);
		String aName = rs.getString(Component_Db.NAME_COL);
		Component_Db comp = new Component_Db(anId, aName);
		components.add(comp);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupComponents()",
				    IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return components;

    }


    /**
     * Create a list of Component_Db objects for the given ToolKit.
     * 
     * @param xContext An application context object.
     * @param compType A Component Type to filter the Component list
     * @return Collection of Component_Db objects.
     * @throws Trouble querying the database.
     */
    public Vector<Component_Db> dbLookupComponents(EdaContext xContext,
						   ComponentType_Db compType)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupComponentsStatement(xContext);

	try {
	    getStatement().setShort(1, getVersion().getId());
	    getStatement().setLong(2, compType.getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupComponents()",
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
	Vector<Component_Db> components = new Vector<Component_Db>();
	try {
	    while (rs.next()) {
		short anId = rs.getShort(Component_Db.ID_COL);
		String aName = rs.getString(Component_Db.NAME_COL);
		Component_Db comp = new Component_Db(anId, aName);
		components.add(comp);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupComponents()",
				    IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return components;

    }


    /**
     * Create a list of RelVersion_Db objects for the given Component and/or
     * StageName
     * 
     * @param xContext An application context object.
     * @param aComp A Component_Db object
     * @param aStage A StageName_Db object
     * @return Collection of RelVersion_Db objects.
     * @throws Trouble querying the database.
     */
    public Vector<Component_Version_Db> dbLookupCompVerionss(EdaContext xContext,
							     Component_Db aComp)
    throws IcofException {

	return dbLookupCompVersions(xContext, aComp, null);

    }


    /**
     * Create a list of RelVersion_Db objects for the given Component and/or
     * StageName
     * 
     * @param xContext An application context object.
     * @param aComp A Component_Db object
     * @param aStage A StageName_Db object
     * @return Collection of RelVersion_Db objects.
     * @throws Trouble querying the database.
     */
    public Vector<Component_Version_Db> dbLookupCompVersions(EdaContext xContext,
							     Component_Db aComp,
							     StageName_Db aStage)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	if (aStage == null)
	    setLookupCompVersionsStatement(xContext, false);
	else
	    setLookupCompVersionsStatement(xContext, true);
	
	try {
	    getStatement().setShort(1, aComp.getId());
	    if (aStage != null)
		getStatement().setShort(2, aStage.getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(this.getClass().getName(),
						 "dbLookupCompToolKits()",
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
	Vector<Component_Version_Db> compVers = new Vector<Component_Version_Db>();
	try {
	    while (rs.next()) {
		short anId = rs.getShort(Component_Version_Db.ID_COL);
		Component_Version_Db tk = new Component_Version_Db(anId);
		compVers.add(tk);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupCompToolKits()",
				    IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return compVers;

    }


    /**
     * Insert a new row.
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext, User_Db user)
    throws IcofException {

	// Get the next id for this new row.
	setNextIdStatement(xContext);
	setId(getNextSmallIntId(xContext));
	closeStatement(xContext);

	// Create the SQL query in the PreparedStatement.
	setAddRowStatement(xContext);
	Timestamp now = new Timestamp(new java.util.Date().getTime());
	try {
	    getStatement().setLong(1, getId());
	    getStatement().setShort(2, getCompRelease().getId());
	    getStatement().setShort(3, getVersion().getId());
	    getStatement().setShort(4, getStageName().getId());
	    getStatement().setString(5, user.getIntranetId());
	    getStatement().setTimestamp(6, now);
	    getStatement().setString(7, user.getIntranetId());
	    getStatement().setTimestamp(8, now);
	    getStatement().setString(9, null);
	    getStatement().setString(10, null);

	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbAddRow()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (!insertRow(xContext)) {
	    IcofException ie = new IcofException(this.getClass().getName(),
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
	setId(id);
	dbLookupById(xContext);

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

	setCompRelease(xContext, rs.getShort(REL_COMP_ID_COL));
	setVersion(xContext, rs.getShort(VERSION_ID_COL));
	setStageName(xContext, rs.getShort(STAGE_NAME_ID_COL));
	setLoadFromDb(true);

    }


    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

	// Get the audit data
	String audit = super.toString(xContext);

	// Get the class specific data
	StringBuffer buffer = new StringBuffer();
	buffer.append("ID: " + getId() + "\n");
	if (getCompRelease() != null) {
	    buffer.append("ComponentRelease ID: " + getCompRelease().getId()
			  + "\n");
	    if (getCompRelease().getRelease() != null)
		buffer.append("ComponentRelease release: "
			      + getCompRelease().getRelease().getName() + "\n");
	    if (getCompRelease().getComponent() != null)
		buffer.append("ComponentRelease component: "
			      + getCompRelease().getComponent().getName()
			      + "\n");
	}
	else {
	    buffer.append("ComponentRelease ID: NULL\n");
	    buffer.append("ComponentRelease release: NULL\n");
	    buffer.append("ComponentRelease component: NULL\n");
	}
	if (getVersion() != null) {
	    buffer.append("Version ID: " + getVersion().getId() + "\n");
	    if (getVersion().getRelease() != null)
		buffer.append("Version release: "
			      + getVersion().getRelease().getName() + "\n");
	    buffer.append("Version name: " + getVersion().getName() + "\n");
	}
	else {
	    buffer.append("Version ID: NULL\n");
	    buffer.append("Version release: NULL \n");
	    buffer.append("Version name: \n");
	}
	if (getStageName() != null) {
	    buffer.append("StageName ID: " + getStageName().getId() + "\n");
	    buffer.append("StageName name: " + getStageName().getName() + "\n");
	}
	else {
	    buffer.append("StageName ID: NULL\n");
	    buffer.append("StageName name: NULL\n");
	}

	buffer.append(audit);

	return buffer.toString();

    }


    /**
     * Update the stage name for this object.
     * 
     * @param xContext An application context object.
     * @param aStageName A StageName_Db object.
     * @throws IcofException
     * @throws Trouble querying the database.
     */
    public void dbUpdate(EdaContext xContext, StageName_Db aStageName,
			 User_Db aUser)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setUpdateStageNameStatement(xContext);

	Timestamp now = new Timestamp(new java.util.Date().getTime());
	try {
	    getStatement().setShort(1, aStageName.getId());
	    getStatement().setString(2, aUser.getEmailAddress());
	    getStatement().setTimestamp(3, now);
	    getStatement().setLong(4, getId());

	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbUpdate()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (!insertRow(xContext)) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbUpdate()",
						 IcofException.SEVERE,
						 "Unable to update selected row.\n",
						 "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	// Set the current status to the new status.
	setStageName(aStageName);
	setUpdatedBy(aUser.getIntranetId());
	setUpdatedOn(now);
	setLoadFromDb(true);


    }


    /**
     * Delete (mark as deleted) this object in the database
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbDeleteRow(EdaContext xContext, User_Db editor)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	Timestamp now = new Timestamp(new java.util.Date().getTime());
	setDeleteRowStatement(xContext);
	try {
	    getStatement().setString(1, editor.getIntranetId());
	    getStatement().setTimestamp(2, now);
	    getStatement().setLong(3, getId());

	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbDeleteRow()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (!insertRow(xContext)) {
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
     * Create a key from the ID.
     * 
     * @param xContext Application context object.
     * @return A Statement object.
     */
    public String getIdKey(EdaContext xContext) {

	return String.valueOf(getId());
    }

}
