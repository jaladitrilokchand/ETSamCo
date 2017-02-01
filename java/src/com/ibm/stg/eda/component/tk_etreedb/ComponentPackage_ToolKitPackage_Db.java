/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2013 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *-PURPOSE---------------------------------------------------------------------
 * ComponentPackage_x_ToolKitPackage DB class
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 10/02/2013 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeSet;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;


public class ComponentPackage_ToolKitPackage_Db extends TkAudit {

    /**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.COMPONENTPACKAGE_X_TOOLKITPACKAGE";
    public static final String COMPONENT_PACKAGE_ID_COL = "COMPONENTPACKAGE_ID";
    public static final String TOOLKITPACKAGE_ID_COL = "TOOLKITPACKAGE_ID";
    public static final String ALL_COLS = COMPONENT_PACKAGE_ID_COL + ","
					  + TOOLKITPACKAGE_ID_COL;
    private static final long serialVersionUID = 1L;


    /**
     * Constructor - takes ComponentPackage and ComponentTkVersion ids
     * 
     * @param aCompPackageId A ComponentPackage database object id
     * @param aTkPackageId A ToolKitPackage database object id
     */
    public ComponentPackage_ToolKitPackage_Db(EdaContext xContext,
					      long aCompPackageId,
					      long aTkPackageId)
    throws IcofException {

	setTkPackage(xContext, aTkPackageId);
	setCompPackage(xContext, aCompPackageId);
    }


    /**
     * Constructor - takes ComponentPackage and TkVersion objects.
     * 
     * @param aTkPackage   A ToolKitPackage_Db object
     * @param aCompPackage A ComponentPackage object
     */
    public ComponentPackage_ToolKitPackage_Db(ComponentPackage_Db aCompPackage,
					      ToolKitPackage_Db aTkPackage) {

	setTkPackage(aTkPackage);
	setCompPackage(aCompPackage);
    }


    /**
     * Data Members
     */
    private ToolKitPackage_Db tkPackage;
    private ComponentPackage_Db compPackage;


    /**
     * Getters
     * @formatter:off
     */
    public ToolKitPackage_Db getTkPackage() { return tkPackage; }
    public ComponentPackage_Db getCompPackage() { return compPackage; }


    /**
     * Setters
     */
    private void setTkPackage(ToolKitPackage_Db anUpdate) { tkPackage = anUpdate; }
    private void setCompPackage(ComponentPackage_Db anUpdate) {	compPackage = anUpdate; }
    private void setTkPackage(EdaContext xContext, long anId) { 
	tkPackage = new ToolKitPackage_Db(anId);
    }
    private void setCompPackage(EdaContext xContext, long anId) {
	compPackage = new ComponentPackage_Db(anId);
    }
    // @formatter:on

    /**
     * Create a PreparedStatement to lookup this object by ids.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupIdsStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
		       + TOOLKITPACKAGE_ID_COL + " = ? AND "
		       + COMPONENT_PACKAGE_ID_COL + " = ? ";

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
		       + " values( ?, ? )";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup the ComponentPackages
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupCompPkgsStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select cptp." + COMPONENT_PACKAGE_ID_COL + ", " +
	               "cp." + ComponentPackage_Db.PACKAGE_NAME_COL + 
	               " from " +
		       TABLE_NAME + " as cptp, " +
	               ComponentPackage_Db.TABLE_NAME + " as cp" +
		       " where cptp." + TOOLKITPACKAGE_ID_COL + " = ? " +
		       " and cptp." + COMPONENT_PACKAGE_ID_COL + " = cp." +
		       ComponentPackage_Db.ID_COL + 
		       " order by cp." + ComponentPackage_Db.PACKAGE_NAME_COL;

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup the TkVersions.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupTkPackagesStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + TOOLKITPACKAGE_ID_COL + " from " + TABLE_NAME
		       + " where " + COMPONENT_PACKAGE_ID_COL + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup the CompVersions.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setUpdateCompVersionStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME + "   set " + TOOLKITPACKAGE_ID_COL
		       + " = ? " + " where " + TOOLKITPACKAGE_ID_COL + " = ? "
		       + "   and " + COMPONENT_PACKAGE_ID_COL + " = ? ";

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
    public void setDeleteCompPkgsStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "delete from " + TABLE_NAME + " where "
		       + COMPONENT_PACKAGE_ID_COL + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Look up this object by id.
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbLookupByIds(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupIdsStatement(xContext);

	try {
	    getStatement().setLong(1, getTkPackage().getId());
	    getStatement().setLong(2, getCompPackage().getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupByIds()",
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
						 "dbLookupByIds()",
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
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setAddRowStatement(xContext);

	try {
	    getStatement().setLong(1, getCompPackage().getId());
	    getStatement().setLong(2, getTkPackage().getId());

	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbAddRow()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n"
						 + getStatement().toString());
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
	setLoadFromDb(true);

    }


    /**
     * Create a list of CompPackage objects for this tool kit package
     * 
     * @param xContext An application context object.
     * @return Collection of ComponentPackage_Db objects
     * @throws Trouble querying the database.
     */
    public Vector<ComponentPackage_Db> dbLookupCompPkgs(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupCompPkgsStatement(xContext);

	try {
	    getStatement().setLong(1, getTkPackage().getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupCompPkgs()",
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
	Vector<ComponentPackage_Db> items = new Vector<ComponentPackage_Db>();
	try {
	    while (rs.next()) {
		long anId = rs.getLong(COMPONENT_PACKAGE_ID_COL);
		ComponentPackage_Db cr = new ComponentPackage_Db(anId);
		cr.dbLookupById(xContext);

		items.add(cr);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupCompPkgs()", IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return items;

    }


    /**
     * Create a list of ToolKitPackage objects for this CompPackage
     * 
     * @param xContext     An application context object
     * @return Collection of ToolKitPackage_Db objects
     * @throws Trouble querying the database.
     */
    public Vector<ToolKitPackage_Db> dbLookupTkPackages(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupTkPackagesStatement(xContext);

	try {
	    getStatement().setLong(1, getCompPackage().getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupTkPackages()",
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
	Vector<ToolKitPackage_Db> items = new Vector<ToolKitPackage_Db>();
	try {
	    while (rs.next()) {
		short anId = rs.getShort(TOOLKITPACKAGE_ID_COL);
		ToolKitPackage_Db pkg = new ToolKitPackage_Db(anId);
		pkg.dbLookupById(xContext);

		items.add(pkg);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupTkPackages()",
				    IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return items;

    }


    /**
     * Update the TkPackage for this object.
     * 
     * @param xContext     Application context object.
     * @param newTkPackage The new TkPackage object
     * @throws Trouble querying the database.
     */
    public void dbUpdateTkVersion(EdaContext xContext,
				  ToolKitPackage_Db newTkPackage)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setUpdateCompVersionStatement(xContext);

	try {
	    getStatement().setLong(1, newTkPackage.getId());
	    getStatement().setLong(2, getTkPackage().getId());
	    getStatement().setLong(3, getCompPackage().getId());

	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbUpdateTkPackage()",
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
						 "dbUpdateTkPackage()",
						 IcofException.SEVERE,
						 "Unable to update row.\n",
						 "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	// Update the data members.
	setTkPackage(newTkPackage);
	setLoadFromDb(true);

    }


    /**
     * Delete this object from the database
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbDeleteCompPkgs(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setDeleteCompPkgsStatement(xContext);
	try {
	    getStatement().setLong(1, getCompPackage().getId());

	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbDeleteCompPkgs()",
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
						 "dbDeleteCompPkgs()",
						 IcofException.SEVERE,
						 "Unable to delete row.\n",
						 "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

    }


    /**
     * Populate this object from the result set.
     * 
     * @param xContext Application context.
     * @param rs A valid result set.
     * @throws IcofException
     * @throws IcofException
     * @throws Trouble retrieving the data.
     */
    protected void populate(EdaContext xContext, ResultSet rs)
    throws SQLException, IcofException {

	setTkPackage(xContext, rs.getLong(TOOLKITPACKAGE_ID_COL));
	setCompPackage(xContext, rs.getLong(COMPONENT_PACKAGE_ID_COL));
	setLoadFromDb(true);

    }


    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

	// Get the class specific data
	StringBuffer buffer = new StringBuffer();
	buffer.append("ComponentPackage ID: " + getCompPackage().getId() + "\n");
	buffer.append("TkPackage  ID      : " + getTkPackage().getId() + "\n");

	return buffer.toString();

    }


    /**
     * Create a key from the ID.
     * 
     * @param xContext Application context object.
     * @return A Statement object.
     */
    public String getIdKey(EdaContext xContext) {

	return String.valueOf(getTkPackage().getIdKey(xContext) + "_"
			      + getCompPackage().getIdKey(xContext));
    }


}
