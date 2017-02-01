/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2012 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * FILE: ReleasePackage_Component_Version_Db.java
 *
 *-PURPOSE---------------------------------------------------------------------
 * ReleasePackage X Component Version class
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 06/18/2012 PS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;

public class ReleasePackage_Component_Version_Db extends TkAudit {

	private static final long serialVersionUID = -1744772555918785084L;

	/**
	 * Constants.
	 */
	public static final String TABLE_NAME = "TK.RELEASEPACKAGE_X_COMPONENT_TKVERSION";
	public static final String RELEASE_PACKAGE_ID_COL = "RELEASEPACKAGE_ID";
	public static final String COMPONENT_TKVERSION_ID_COL = "COMPONENT_TKVERSION_ID";
	public static final String ALL_COLS = RELEASE_PACKAGE_ID_COL + ","
			+ COMPONENT_TKVERSION_ID_COL;

	/**
	 * Constructor - takes EdaContext and aCompRelId, aCompTypeId ids
	 * 
	 * @param xContext
	 *            EdaContext
	 * @param aCompRelId
	 *            A Component_Release id
	 * @param aCompTypeId
	 *            A Component_Type id
	 */
	public ReleasePackage_Component_Version_Db(EdaContext xContext,
			short aCompRelId, short aCompTypeId) throws IcofException {
		setReleasePackage(xContext, aCompRelId);
		setComponentVersion(xContext, aCompTypeId);
	}

	/**
	 * Constructor - takes ReleasePackage_Db and Component_Version_Db objects.
	 * 
	 * @param aRelPackage
	 *            A ReleasePackage_Db object
	 * @param aCompVersion
	 *            A Component_Version_Db object
	 */
	public ReleasePackage_Component_Version_Db(ReleasePackage_Db aRelPackage,
			Component_Version_Db aCompVersion) {
		setReleasePackage(aRelPackage);
		setComponentVersion(aCompVersion);
	}

	/**
	 * Data Members
	 */
	private ReleasePackage_Db releasePackage;
	private Component_Version_Db compVersion;

	/**
	 * Getters
	 */
	public ReleasePackage_Db getReleasePackage() {
		return releasePackage;
	}

	public Component_Version_Db getCompVersion() {
		return compVersion;
	}

	/**
	 * Setters
	 */
	private void setReleasePackage(ReleasePackage_Db aReleasePackage) {
		releasePackage = aReleasePackage;
	}

	private void setComponentVersion(Component_Version_Db aCompVersion) {
		compVersion = aCompVersion;
	}

	private void setReleasePackage(EdaContext xContext, short anId) {
		releasePackage = new ReleasePackage_Db(anId);
	}

	private void setComponentVersion(EdaContext xContext, short anId) {
		compVersion = new Component_Version_Db(anId);
	}

	/**
	 * Create a PreparedStatement to lookup this object by ids.
	 * 
	 * @param xContext
	 *            Application context.
	 * @throws IcofException
	 */
	public void setLookupIdsStatement(EdaContext xContext) throws IcofException {

		// Define the query.
		String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
				+ RELEASE_PACKAGE_ID_COL + " = ? " + " AND "
				+ COMPONENT_TKVERSION_ID_COL + " = ? ";

		// Set and prepare the query and statement.
		setQuery(xContext, query);

	}

	/**
	 * Create a PreparedStatement to add a row.
	 * 
	 * @param xContext
	 *            Application context.
	 * @throws IcofException
	 */
	public void setAddRowStatement(EdaContext xContext) throws IcofException {

		// Define the query.
		String query = "insert into " + TABLE_NAME + " ( " + ALL_COLS + " )"
				+ " values( ?, ? )";

		// Set and prepare the query and statement.
		setQuery(xContext, query);

	}
	

	/**
	 * Create a PreparedStatement to update a row.
	 * 
	 * @param xContext
	 *            Application context.
	 * @throws IcofException
	 */
	public void setUpdateRowStatement(EdaContext xContext) throws IcofException {

		  // Define the query.
        String query = "update " + TABLE_NAME + 
                       " set " +  COMPONENT_TKVERSION_ID_COL + " = ?  " + 
                       " where " + RELEASE_PACKAGE_ID_COL + " = ? ";
		// Set and prepare the query and statement.
		setQuery(xContext, query);

	}


	/**
	 * Create a PreparedStatement to lookup by Component_Type
	 * 
	 * @param xContext
	 *            Application context.
	 * @throws IcofException
	 */

	public void setLookupReleasePackageStatement(EdaContext xContext)
			throws IcofException {

		// Define the query.
		String query = "select " + RELEASE_PACKAGE_ID_COL + " from "
				+ TABLE_NAME + " where " + COMPONENT_TKVERSION_ID_COL + " = ? ";

		// Set and prepare the query and statement.
		setQuery(xContext, query);

	}

	/**
	 * Create a PreparedStatement to lookup by Release Package
	 * 
	 * @param xContext
	 *            Application context.
	 * @throws IcofException
	 */
	public void setLookupComponentVersionStatement(EdaContext xContext)
			throws IcofException {

		// Define the query.
		String query = "select " + COMPONENT_TKVERSION_ID_COL + " from "
				+ TABLE_NAME + " where " + RELEASE_PACKAGE_ID_COL + " = ? ";

		// Set and prepare the query and statement.
		setQuery(xContext, query);

	}

	/**
	 * Create a PreparedStatement to delete this object
	 * 
	 * @param xContext
	 *            Application context.
	 * @throws IcofException
	 */
	public void setDeleteStatement(EdaContext xContext) throws IcofException {

		// Define the query.
		String query = "delete from " + TABLE_NAME + " where "
				+ RELEASE_PACKAGE_ID_COL + " = ? " + " and "
				+ COMPONENT_TKVERSION_ID_COL + " = ? ";

		// Set and prepare the query and statement.
		setQuery(xContext, query);

	}

	/**
	 * Look up this object by id.
	 * 
	 * @param xContext
	 *            An application context object.
	 * @throws Trouble
	 *             querying the database.
	 */
	public void dbLookupByIds(EdaContext xContext) throws IcofException {

		// Create the SQL query in the PreparedStatement.
		setLookupIdsStatement(xContext);

		try {
			getStatement().setLong(1, getReleasePackage().getId());
			getStatement().setLong(2, getCompVersion().getId());
		} catch (SQLException trap) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbLookupByIds()", IcofException.SEVERE,
					"Unable to prepare SQL statement.", IcofException
							.printStackTraceAsString(trap)
							+ "\n" + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		// Run the query.
		if (!selectSingleRow(xContext)) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbLookupByIds()", IcofException.SEVERE,
					"Unable to find row for query.\n", "QUERY: " + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;

		}

		// Close the PreparedStatement.
		closeStatement(xContext);

	}

	/**
	 * Insert a new row.
	 * 
	 * @param xContext
	 *            An application context object.
	 * @throws Trouble
	 *             querying the database.
	 */
	public boolean dbAddRow(EdaContext xContext) throws IcofException {

		// Create the SQL query in the PreparedStatement.
		setAddRowStatement(xContext);

		try {
			getStatement().setLong(1, getReleasePackage().getId());
			getStatement().setLong(2, getCompVersion().getId());

		} catch (SQLException trap) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbAddRow()", IcofException.SEVERE,
					"Unable to prepare SQL statement.", IcofException
							.printStackTraceAsString(trap)
							+ "\n" + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		boolean value = insertRow(xContext);
		// Run the query.
		if (!value) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbAddRow()", IcofException.SEVERE,
					"Unable to insert new row.\n", "QUERY: " + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		// Close the PreparedStatement.
		closeStatement(xContext);
		return value;
	}

	/**
	 * Updates a  row.
	 * 
	 * @param xContext
	 *            An application context object.
	 * @throws Trouble
	 *             querying the database.
	 */
	public boolean dbUpdateRow(EdaContext xContext) throws IcofException {

		// Create the SQL query in the PreparedStatement.
		setUpdateRowStatement(xContext);

		try {
			getStatement().setLong(1, getCompVersion().getId());
			getStatement().setLong(2, getReleasePackage().getId());

		} catch (SQLException trap) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbUpdateRow()", IcofException.SEVERE,
					"Unable to prepare SQL statement.", IcofException
							.printStackTraceAsString(trap)
							+ "\n" + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		boolean value = insertRow(xContext);
		// Run the query.
		if (!value) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbUpdateRow()", IcofException.SEVERE,
					"Unable to update row.\n", "QUERY: " + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		// Close the PreparedStatement.
		closeStatement(xContext);
		return value;
	}

	
	/**
	 * Create a list of ReleasePackage_Db objects
	 * 
	 * @param xContext
	 *            An application context object.
	 * @return Collection of ReleasePackage_Db objects.
	 * @throws Trouble
	 *             querying the database.
	 */
	public Hashtable<String, ReleasePackage_Db> dbLookupReleasePackage(
			EdaContext xContext) throws IcofException {

		// Create the SQL query in the PreparedStatement.
		setLookupReleasePackageStatement(xContext);

		try {
			getStatement().setLong(1, getCompVersion().getId());
		} catch (SQLException trap) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbLookupReleasePackage()", IcofException.SEVERE,
					"Unable to prepare SQL statement.", IcofException
							.printStackTraceAsString(trap)
							+ "\n" + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		// Run the query.
		ResultSet rs = executeQuery(xContext);

		// Process the results
		Hashtable<String, ReleasePackage_Db> compReleases = new Hashtable<String, ReleasePackage_Db>();
		try {
			while (rs.next()) {
				long anId = rs.getLong(RELEASE_PACKAGE_ID_COL);
				ReleasePackage_Db rp = new ReleasePackage_Db(anId);
				rp.dbLookupById(xContext);

				compReleases.put(rp.getIdKey(xContext), rp);
			}

		} catch (SQLException ex) {
			throw new IcofException(this.getClass().getName(),
					"dbLookupReleasePackage()", IcofException.SEVERE,
					"Error reading DB query results.", ex.getMessage());
		}

		// Close the PreparedStatement.
		closeStatement(xContext);

		return compReleases;

	}

	/**
	 * Create a list of Component_Version_Db objects for the Release Package
	 * 
	 * @param xContext
	 *            An application context object.
	 * @return Collection of Component_Version_Db objects.
	 * @throws Trouble
	 *             querying the database.
	 */
	public Hashtable<String, Component_Version_Db> dbLookupComponentVersion(
			EdaContext xContext) throws IcofException {

		// Create the SQL query in the PreparedStatement.
		setLookupComponentVersionStatement(xContext);

		try {
			getStatement().setLong(1, getReleasePackage().getId());
		} catch (SQLException trap) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbLookupComponentVersion()", IcofException.SEVERE,
					"Unable to prepare SQL statement.", IcofException
							.printStackTraceAsString(trap)
							+ "\n" + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		// Run the query.
		ResultSet rs = executeQuery(xContext);

		// Process the results
		Hashtable<String, Component_Version_Db> compTypes = new Hashtable<String, Component_Version_Db>();
		try {
			while (rs.next()) {
				long anId = rs.getLong(COMPONENT_TKVERSION_ID_COL);
				Component_Version_Db ct = new Component_Version_Db(anId);
				ct.dbLookupById(xContext);

				compTypes.put(ct.getIdKey(xContext), ct);
			}

		} catch (SQLException ex) {
			throw new IcofException(this.getClass().getName(),
					"dbLookupComponentVersion()", IcofException.SEVERE,
					"Error reading DB query results.", ex.getMessage());
		}

		// Close the PreparedStatement.
		closeStatement(xContext);

		return compTypes;

	}

	/**
	 * Delete this object from the database
	 * 
	 * @param xContext
	 *            An application context object.
	 * @throws Trouble
	 *             querying the database.
	 */
	public void dbDeleteRow(EdaContext xContext) throws IcofException {

		// Create the SQL query in the PreparedStatement.
		setDeleteStatement(xContext);
		try {
			getStatement().setLong(1, getReleasePackage().getId());
			getStatement().setLong(2, getCompVersion().getId());

		} catch (SQLException trap) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbDeleteRow()", IcofException.SEVERE,
					"Unable to prepare SQL statement.", IcofException
							.printStackTraceAsString(trap)
							+ "\n" + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		// Run the query.
		if (!insertRow(xContext)) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbDeleteRow()", IcofException.SEVERE,
					"Unable to delete row.\n", "QUERY: " + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		// Close the PreparedStatement.
		closeStatement(xContext);

	}

	/**
	 * Populate this object from the result set.
	 * 
	 * @param xContext
	 *            Application context.
	 * @param rs
	 *            A valid result set.
	 * @throws IcofException
	 * @throws IcofException
	 * @throws Trouble
	 *             retrieving the data.
	 */
	public void populate(EdaContext xContext, ResultSet rs)
			throws SQLException, IcofException {

		setReleasePackage(xContext, rs.getShort(RELEASE_PACKAGE_ID_COL));
		setComponentVersion(xContext, rs.getShort(COMPONENT_TKVERSION_ID_COL));
		setLoadFromDb(true);

	}

	/**
	 * Return the members as a string.
	 */
	public String toString(EdaContext xContext) {

		// Get the class specific data
		StringBuffer buffer = new StringBuffer();
		if (getReleasePackage() != null) {
			buffer.append("Release Package ID: " + getReleasePackage().getId()
					+ "\n");
		} else {
			buffer.append("Release Package ID: NULL\n");
		}

		if (getCompVersion() != null) {
			buffer.append("Component Version  ID: " + getCompVersion().getId()
					+ "\n");
		} else {
			buffer.append("Component Version ID: NULL\n");
		}

		return buffer.toString();

	}

	/**
	 * Create a key from the ID.
	 * 
	 * @param xContext
	 *            Application context object.
	 * @return A Statement object.
	 */
	public String getIdKey(EdaContext xContext) {
		return String.valueOf(getReleasePackage().getIdKey(xContext) + "_"
				+ getCompVersion().getIdKey(xContext));
	}

}
