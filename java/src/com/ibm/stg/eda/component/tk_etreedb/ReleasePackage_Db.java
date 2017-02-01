/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2012 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Prashanth Shivaram
 *    DATE: 06/19/2012
 *
 *-PURPOSE---------------------------------------------------------------------
 * ReleasePackage_Db  class with audit info
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 06/19/2012 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;

public class ReleasePackage_Db extends TkAudit implements
		Comparable<ReleasePackage_Db> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -456394795831434008L;
	/**
	 * Constants.
	 */
	public static final String TABLE_NAME = "TK.RELEASEPACKAGE";
	public static final String ID_COL = "RELEASEPACKAGE_ID";
	public static final String PLATFORM_ID_COL = "PLATFORM_ID";
	public static final String SPINLEVEL_COL = "SPINLEVEL";
	public static final String PATCHLEVEL_COL = "PATCHLEVEL";

	public static final String ALL_COLS = ID_COL + "," + PLATFORM_ID_COL + ","
			+ SPINLEVEL_COL + "," + PATCHLEVEL_COL + "," + CREATED_BY_COL + ","
			+ CREATED_ON_COL + "," + UPDATED_BY_COL + "," + UPDATED_ON_COL
			+ "," + DELETED_BY_COL + "," + DELETED_ON_COL;

	public ReleasePackage_Db() {
		super();
	}

	
	/**
	 * Constructor - takes a DB id
	 * 
	 * @param anId
	 *            A database id
	 */
	public ReleasePackage_Db(long anId) {
		setId(anId);
	}

	/**
	 * Constructor - takes a platform id , spin level id and a patch level id.
	 * 
	 * @param platformId
	 *            The platform id
	 * @param spinLevel
	 *            The spin level id
	 * @param patchLevel
	 *            The patch level id
	 */
	public ReleasePackage_Db(short platformId, short spinLevel, short patchLevel) {
		setPlatformId(platformId);
		setSpinLevel(spinLevel);
		setPatchLevel(patchLevel);
	}

	/**
	 * Constructor - takes an release package id, platform id , spin level id
	 * and a patch level id.
	 * 
	 * @param platformId
	 *            The platform id
	 * @param spinLevel
	 *            The spin level id
	 * @param patchLevel
	 *            The patch level id
	 */
	public ReleasePackage_Db(long anId, short platformId, short spinLevel,
			short patchLevel) {
		setId(anId);
		setPlatformId(platformId);
		setSpinLevel(spinLevel);
		setPatchLevel(patchLevel);
	}

	/**
	 * Constructor - takes an release package id, platform id , spin level id
	 * patch level id, created by, created on, updated by, updated on, deleted
	 * by and deleted On info.
	 * 
	 * @param anId
	 *            The id
	 * @param platformId
	 *            The platform id
	 * @param spinLevel
	 *            The spin level id
	 * @param patchLevel
	 *            The patch level id
	 * @param createdBy
	 *            Created By User
	 * @param createdOn
	 *            Created on
	 * @param updatedBy
	 *            Updated User
	 * @param updatedOn
	 *            Updated on
	 * @param deletedBy
	 *            Deleting User
	 * @param deletedOn
	 *            Deleting on
	 */
	public ReleasePackage_Db(long anId, short platformId, short spinLevel,
			short patchLevel, String createdBy, Timestamp createdOn,
			String updatedBy, Timestamp updatedOn, String deletedBy,
			Timestamp deletedOn) {
		setId(anId);
		setPlatformId(platformId);
		setSpinLevel(spinLevel);
		setPatchLevel(patchLevel);
		setCreatedBy(createdBy);
		setCreatedOn(createdOn);
		setUpdatedBy(updatedBy);
		setUpdatedOn(updatedOn);
		setDeletedBy(deletedBy);
		setDeletedOn(deletedOn);
	}

	/**
	 * Data Members
	 */
	private long id;
	private short platformId;
	private short spinLevel;
	private short patchLevel;

	/**
	 * Getters
	 */
	public long getId() {
		return id;
	}

	public short getPlatformId() {
		return platformId;
	}

	public short getSpinLevel() {
		return spinLevel;
	}

	public short getPatchLevel() {
		return patchLevel;
	}

	/**
	 * Setters
	 */
	private void setId(long anId) {
		id = anId;
	}

	public void setPlatformId(short platformId) {
		this.platformId = platformId;
	}

	public void setSpinLevel(short spinLevel) {
		this.spinLevel = spinLevel;
	}

	public void setPatchLevel(short patchLevel) {
		this.patchLevel = patchLevel;
	}

	/**
	 * Create a PreparedStatement to lookup this object by id.
	 * 
	 * @param xContext
	 *            Application context.
	 * @return PreparedStatement
	 * @throws IcofException
	 */
	public void setLookupIdStatement(EdaContext xContext) throws IcofException {

		// Return the statement if it already exists.
		if (getLookupIdStatement() != null) {
			return;
		}

		// Define the query.
		String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
				+ ID_COL + " = ? ";

		// Otherwise create a statement object and return it.
		setLookupIdStmt(TkDbUtils.prepStatement(xContext, query));

	}
	
	/**
	 * Create a PreparedStatement to lookup this object by id.
	 * 
	 * @param xContext
	 *            Application context.
	 * @return PreparedStatement
	 * @throws IcofException
	 */
	public void setLookUpPlatSpinPatchStatement(EdaContext xContext) throws IcofException {

		// Define the query.
		String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
				+ PATCHLEVEL_COL + " = ? " + " AND " + SPINLEVEL_COL + " = ? "
				+ " AND " + PLATFORM_ID_COL + " = ?";
		setQuery(xContext, query);

	}

	/**
	 * Create a PreparedStatement to add a row.
	 * 
	 * @param xContext
	 *            Application context.
	 * @return PreparedStatement
	 * @throws IcofException
	 */

	public void setAddRowStatement(EdaContext xContext) throws IcofException {
		// Return the statement if it already exists.
		if (getAddRowStatement() != null) {
			return;
		}
		// Define the query.
		String query = "insert into "
			+ TABLE_NAME
			+ " ( "
			+ ALL_COLS
			+ " )"
			+ " values( ?, ?, ?, ?, ?, CURRENT TIMESTAMP, ?, CURRENT TIMESTAMP,NULL,NULL)";

		setAddRowStmt(TkDbUtils.prepStatement(xContext, query));
	}

	/**
	 * Create a PreparedStatement to lookup the next id for this table.
	 * 
	 * @param xContext
	 *            Application context.
	 * @return PreparedStatement
	 * @throws IcofException
	 */
	public void setNextIdStatement(EdaContext xContext) throws IcofException {

		// Return the statement if it already exists.
		if (getNextIdStatement() != null) {
			return;
		}

		// Define the query.
		String query = TkAudit.getNextIdQuery(xContext, TABLE_NAME, ID_COL);

		// Otherwise create a statement object and return it.
		setNextIdStmt(TkDbUtils.prepStatement(xContext, query));

	}

	/**
	 * Look up this object by id.
	 * 
	 * @param xContext
	 *            An application context object.
	 * @throws Trouble
	 *             querying the database.
	 */
	public void dbLookupById(EdaContext xContext) throws IcofException {

		// Create the SQL query in the PreparedStatement.
		setLookupIdStatement(xContext);

		try {
			getLookupIdStatement().setLong(1, getId());
		} catch (SQLException trap) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbLookupById()", IcofException.SEVERE,
					"Unable to prepare SQL statement.", IcofException
							.printStackTraceAsString(trap)
							+ "\n" + getLookupIdStatement().toString());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		// Run the query.
		if (!selectSingleRow(xContext, getLookupIdStatement())) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbLookupById()", IcofException.SEVERE,
					"Unable to find row for query.\n", "QUERY: "
							+ getLookupIdStatement().toString());
			xContext.getSessionLog().log(ie);
			throw ie;

		}

	}
	
	/**
	 * Look up this object by platform, spin level and patch level info.
	 * 
	 * @param xContext
	 *            An application context object.
	 * @throws Trouble
	 *             querying the database.
	 */
	public ReleasePackage_Db dbLookUpByPlatSpinPatch(EdaContext xContext ) throws IcofException {

		// Create the SQL query in the PreparedStatement.
		setLookUpPlatSpinPatchStatement(xContext);

		try {
			getStatement().setShort(1, getPatchLevel());
			getStatement().setShort(2, getSpinLevel());
			getStatement().setShort(3, getPlatformId());
		} catch (SQLException trap) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbLookUpByPlatSpinPatch()", IcofException.SEVERE,
					"Unable to prepare SQL statement.", IcofException
							.printStackTraceAsString(trap)
							+ "\n" + getLookupIdStatement().toString());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		// Run the query.
		if (!selectSingleRow(xContext, getStatement())) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbLookUpByPlatSpinPatch()", IcofException.SEVERE,
					"Unable to find row for query.\n", "QUERY: "
							+ getStatement().toString());
			xContext.getSessionLog().log(ie);
			throw ie;
		}else{
			return this;
		}
	}

	/**
	 * Insert a new row.
	 * 
	 * @param xContext
	 *            An application context object.
	 * @throws Trouble
	 *             querying the database.
	 */

	public void dbAddRow(EdaContext xContext) throws IcofException {

		// Create the SQL query in the PreparedStatement.
		setAddRowStatement(xContext);
		setNextIdStatement(xContext);

		long id = getNextBigIntId(xContext, getNextIdStatement());
		try {
			
			getAddRowStatement().setLong(1, id);
			getAddRowStatement().setShort(2, getPlatformId());
			getAddRowStatement().setShort(3, getSpinLevel());
			getAddRowStatement().setShort(4, getPatchLevel());
			getAddRowStatement().setString(5, getCreatedBy());
			if (getUpdatedBy() != null) {
				getAddRowStatement().setString(6, getUpdatedBy());
			} else {
				getAddRowStatement().setString(6, getCreatedBy());
			}
		} catch (SQLException trap) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbAddRow()", IcofException.SEVERE,
					"Unable to prepare SQL statement.", IcofException
							.printStackTraceAsString(trap)
							+ "\n" + getAddRowStatement().toString());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		// Run the query.
		if (!insertRow(xContext, getAddRowStatement())) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbAddRow()", IcofException.SEVERE,
					"Unable to insert new row.\n", "QUERY: "
							+ getAddRowStatement().toString());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		// Close the PreparedStatement.
		try {
			getAddRowStatement().close();
			getNextIdStatement().close();
		} catch (SQLException trap) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbAddRow()", IcofException.SEVERE,
					"Unable to prepare SQL statement.", IcofException
							.printStackTraceAsString(trap)
							+ "\n" + getAddRowStatement().toString());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		// Load the data for the new row.
		setId(id);
		dbLookupById(xContext);

	}

	/**
	 * Mark this object as Deleted from the database
	 * 
	 * @param xContext
	 *            An application context object.
  	 * @param deletedBy
	 *            The Deleted by user   
	 * @throws Trouble
	 *             querying the database.
	 */

	public boolean dbDeleteRow(EdaContext xContext, String deletedBy)
			throws IcofException {

		Timestamp now = new Timestamp(new java.util.Date().getTime());
		// Create the SQL query in the PreparedStatement.
		setDeleteStatement(xContext);
		try {
			getStatement().setString(1, getDeletedBy());
			getStatement().setTimestamp(2, now);
			getStatement().setShort(3, getPatchLevel());
			getStatement().setShort(4, getSpinLevel());
			getStatement().setShort(5, getPlatformId());

		} catch (SQLException trap) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbDeleteRow()", IcofException.SEVERE,
					"Unable to prepare SQL statement.", IcofException
							.printStackTraceAsString(trap)
							+ "\n" + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		boolean result = insertRow(xContext);

		// Run the query.
		if (!result) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbDeleteRow()", IcofException.SEVERE,
					"Unable to delete row.\n", "QUERY: " + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		// Close the PreparedStatement.
		closeStatement(xContext);
		
		   // Set the delete info on this object.
        setDeletedBy(deletedBy);
        setDeletedOn(now);
		return result;
	}

	/**
	 * UnMark this object as Deleted from the database
	 * 
	 * @param xContext
	 *            An application context object.
  	 * @param updatedBy
	 *            The Deleted by user   
	 * @throws Trouble
	 *             querying the database.
	 */

	public boolean dbUnMarkDeleteRow(EdaContext xContext, String updatedBy)
			throws IcofException {

		Timestamp now = new Timestamp(new java.util.Date().getTime());
		// Create the SQL query in the PreparedStatement.
		setUnmarkDeleteStatement(xContext);
		try {
			getStatement().setString(1,null);
			getStatement().setTimestamp(2, null);
			getStatement().setString(3, updatedBy);
			getStatement().setTimestamp(4, now);
			getStatement().setShort(5, getPatchLevel());
			getStatement().setShort(6, getSpinLevel());
			getStatement().setShort(7, getPlatformId());
			

		} catch (SQLException trap) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbUnMarkDeleteRow()", IcofException.SEVERE,
					"Unable to prepare SQL statement.", IcofException
							.printStackTraceAsString(trap)
							+ "\n" + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		boolean result = insertRow(xContext);

		// Run the query.
		if (!result) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbDeleteRow()", IcofException.SEVERE,
					"Unable to delete row.\n", "QUERY: " + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		// Close the PreparedStatement.
		closeStatement(xContext);
		
		   // Set the delete info on this object.
        setDeletedBy(updatedBy);
        setDeletedOn(now);
		return result;
	}

	
	/**
	 * Populate this object from the result set.
	 * 
	 * @param xContext
	 *            Application context.
	 * @param rs
	 *            A valid result set.
	 * @throws IcofException
	 * @throws Trouble
	 *             retrieving the data.
	 */
	protected void populate(EdaContext xContext, ResultSet rs)
			throws SQLException, IcofException {

		super.populate(xContext, rs);
		setId(rs.getInt(ID_COL));
		setPlatformId(rs.getShort(PLATFORM_ID_COL));
		setSpinLevel(rs.getShort(SPINLEVEL_COL));
		setPatchLevel(rs.getShort(PATCHLEVEL_COL));
		setDeletedBy(rs.getString(DELETED_BY_COL));
		setDeletedOn(rs.getTimestamp(DELETED_ON_COL));
	}

	/**
	 * Return the members as a string.
	 */
	public String toString(EdaContext xContext) {

		String audit = super.toString(xContext);
		StringBuffer buffer = new StringBuffer(audit);
		buffer.append("\n");
		buffer.append("ID: " + getId() + "\n");
		buffer.append("Platform: " + getPlatformId() + "\n");
		buffer.append("Spin Level: " + getSpinLevel() + "\n");
		buffer.append("Patch Level: " + getPatchLevel() + "\n");
		return buffer.toString();
	}

	/**
	 * Get a key from the ID.
	 * 
	 * @param xContext
	 *            Application context.
	 */
	public String getIdKey(EdaContext xContext) {
		return String.valueOf(getId());
	}

	/**
	 * Create a list of ReleasePackage_Db objects for the given ToolKit.
	 * 
	 * @param xContext
	 *            An application context object.
	 * @param compVersion
	 *            A Component_Version_Db Object
	 * @return Collection of ReleasePackage_Db objects.
	 * @throws Trouble
	 *             querying the database.
	 */
	public ArrayList<ReleasePackage_Db> dbLookupComponentVersion(
			EdaContext xContext, Component_Version_Db compVersion)
			throws IcofException {

		// Create the SQL query in the PreparedStatement.
		setLookupComponentVersion(xContext);

		try {
			getStatement().setLong(1, compVersion.getId());
		} catch (SQLException trap) {
			IcofException ie = new IcofException(this.getClass().getName(),
					"dbLookupComponentVersion()", IcofException.SEVERE,
					"Unable to prepare SQL statement.", IcofException
							.printStackTraceAsString(trap)
							+ "\n" + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}
		ArrayList<ReleasePackage_Db> releasePackageList = new ArrayList<ReleasePackage_Db>();

		// Run the query.
		ResultSet rs = executeQuery(xContext);
		ReleasePackage_Db releasePackage = null;

		// Process the results
		try {
			while (rs.next()) {
				long anId = rs.getShort(ID_COL);
				short platformId = rs.getShort(PLATFORM_ID_COL);
				short spinLevel = rs.getShort(SPINLEVEL_COL);
				short patchLevel = rs.getShort(PATCHLEVEL_COL);
				Timestamp updatedOn = rs.getTimestamp(UPDATED_ON_COL);
				Timestamp deletedOn = rs.getTimestamp(DELETED_ON_COL);
				Timestamp createdOn = rs.getTimestamp(DELETED_ON_COL);
				String createdBy = rs.getString(CREATED_BY_COL);
				String deletedBy = rs.getString(DELETED_BY_COL);
				String updatedBy = rs.getString(UPDATED_BY_COL);
				releasePackage = new ReleasePackage_Db(anId, platformId,
						spinLevel, patchLevel, createdBy, createdOn, updatedBy,
						updatedOn, deletedBy, deletedOn);
				releasePackageList.add(releasePackage);
			}

		} catch (SQLException ex) {
			throw new IcofException(this.getClass().getName(),
					"dbLookupComponentVersion()", IcofException.SEVERE,
					"Error reading DB query results.", ex.getMessage());
		}

		// Close the PreparedStatement.
		closeStatement(xContext);

		return releasePackageList;

	}

	/**
	 * Create a PreparedStatement to look based on component version
	 * 
	 * @param xContext
	 *            Application context.
	 * @return PreparedStatement
	 * @throws IcofException
	 */
	private void setLookupComponentVersion(EdaContext xContext)
			throws IcofException {

		String query = "select c." + ID_COL + ", " + "c." + PLATFORM_ID_COL  + ", " + "c." + SPINLEVEL_COL + ", "
		+ "c." + PATCHLEVEL_COL + ", " + "c." + CREATED_BY_COL + ", " + "c." + CREATED_ON_COL + ", "
		+ "c." + UPDATED_BY_COL + ", " + "c." + UPDATED_ON_COL + ", " + "c." + DELETED_BY_COL + ", "
		+ "c." + DELETED_ON_COL + " from " + TABLE_NAME + " as c, " + ReleasePackage_Component_Version_Db.TABLE_NAME
		+ " as cr  " + " where cr." + ReleasePackage_Component_Version_Db.COMPONENT_TKVERSION_ID_COL
		+ " =  ? " + " AND cr." + ReleasePackage_Component_Version_Db.RELEASE_PACKAGE_ID_COL
		+ " = c." + ID_COL +  " AND c." + DELETED_ON_COL + " is NULL";;
		
		// Set and prepare the query and statement.
		setQuery(xContext, query);

	}

	/**
	 * Create a PreparedStatement to delete this object
	 * 
	 * @param xContext
	 *            Application context.
	 * @return PreparedStatement
	 * @throws IcofException
	 */
	public void setDeleteStatement(EdaContext xContext) throws IcofException {

		// Define the query.
		String query = "update " + TABLE_NAME + " set " + DELETED_BY_COL
				+ " = ?, " + DELETED_ON_COL + " = ?"
				+ " where " + PATCHLEVEL_COL + " = ? " + " AND "
				+ SPINLEVEL_COL + " = ? " + " AND " + PLATFORM_ID_COL + " = ?";

		// Set and prepare the query and statement.
		setQuery(xContext, query);

	}
	
	/**
	 * Create a PreparedStatement to delete this object
	 * 
	 * @param xContext
	 *            Application context.
	 * @return PreparedStatement
	 * @throws IcofException
	 */
	public void setUnmarkDeleteStatement(EdaContext xContext) throws IcofException {

		// Define the query.
		String query = "update " + TABLE_NAME + " set " + DELETED_BY_COL
				+ " = ?, " + DELETED_ON_COL + " = ?,"  + UPDATED_BY_COL + " = ?, " + UPDATED_ON_COL + " = ?"
				+ " where " + PATCHLEVEL_COL + " = ? " + " AND "
				+ SPINLEVEL_COL + " = ? " + " AND " + PLATFORM_ID_COL + " = ?";

		// Set and prepare the query and statement.
		setQuery(xContext, query);

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
			short value = 0;
			getStatement().setShort(1, value);
			getStatement().setLong(2, getId());

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
	 * Create a PreparedStatement to update a row.
	 * 
	 * @param xContext
	 *            Application context.
	 * @throws IcofException
	 */
	public void setUpdateRowStatement(EdaContext xContext) throws IcofException {

		  // Define the query.
        String query = "update " + TABLE_NAME + 
                       " set " +  PATCHLEVEL_COL + " = ?  " + 
                       " where " + ID_COL + " = ? ";
		// Set and prepare the query and statement.
		setQuery(xContext, query);

	}
	
	public int compareTo(ReleasePackage_Db o) {

		// Compare the spin level
		if (spinLevel != o.spinLevel) {
			return new Short(spinLevel).compareTo(o.spinLevel);
		}//If Spin levels are equal then compare patch levels
		return new Short(patchLevel).compareTo(o.patchLevel);
	}

}


