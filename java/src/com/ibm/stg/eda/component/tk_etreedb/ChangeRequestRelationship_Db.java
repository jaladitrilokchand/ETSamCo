/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * FILE: ChangeRequestRelationship_Db.java
 *
 *-PURPOSE---------------------------------------------------------------------
 * ChangeRequestRelationship DB class.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 12/01/2010 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestRelationship_Db extends TkAudit {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2770405189130484334L;
	/**
	 * Constants.
	 */
	public static final String TABLE_NAME = "TK.ChangeRequest_Relationship";
	public static final String ID_COL = "ChangeRequest_Relationship_ID";
	public static final String FROM_ID_COL = "From_ID";
	public static final String TO_ID_COL = "To_ID";
	public static final String RELATIONSHIP_COL = "Relationship";
	public static final String ALL_COLS = ID_COL + "," + 
	                                      FROM_ID_COL + "," + 
	                                      TO_ID_COL + "," + 
	                                      RELATIONSHIP_COL;


	/**
	 * Constructor - takes a DB id
	 * 
	 * @param anId  A database id
	 */
	public ChangeRequestRelationship_Db(long anId) {
		setId(anId);
	}


	/**
	 * Constructor - takes objects.
	 * 
	 * @param aFromChangeReq  The FROM ChangeRequest object object
	 * @param aToChangeReq    The TO ChangeRequest object object 
	 * @param aRelationship   The relationship name
	 */
	public ChangeRequestRelationship_Db(ChangeRequest_Db aFromChangeReq, 
	                                    ChangeRequest_Db aToChangeReq, 
	                                    String aRelationship) {
		setFromChangeReq(aFromChangeReq);
		setToChangeReq(aToChangeReq);
		setRelationship(aRelationship);
	}


	/**
	 * Data Members
	 */
	private long id;
	private ChangeRequest_Db fromChangeRequest;
	private ChangeRequest_Db toChangeRequest;
	private String relationship;


	/**
	 * Getters
	 */
	public long getId() { return id; }
	public ChangeRequest_Db getFromChangeReq() { return fromChangeRequest; }
	public ChangeRequest_Db getToChangeReq() { return toChangeRequest; }
	public String getRelationshipb() { return relationship; }


	/**
	 * Setters
	 */
	private void setId(long anId) { id = anId; }
	private void setFromChangeReq(ChangeRequest_Db aCr) { fromChangeRequest = aCr; }
	private void setToChangeReq(ChangeRequest_Db aCr) { toChangeRequest = aCr; }
	private void setRelationship(String aName) { relationship= aName; }
	private void setFromChangeReq(EdaContext xContext, long anId) { 
		fromChangeRequest = new ChangeRequest_Db(anId);
	}
	private void setToChangeReq(EdaContext xContext, long anId) { 
		toChangeRequest = new ChangeRequest_Db(anId);
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
		               " where " + ID_COL + " = ? ";

		// Set and prepare the query and statement.
		setQuery(xContext, query);

	}


	/**
	 * Create a PreparedStatement to lookup this row all by ids.
	 * 
	 * @param xContext  Application context.
	 * @return PreparedStatement
	 * @throws IcofException 
	 */
	public void setLookupByToFromStatement(EdaContext xContext) throws IcofException {

		// Define the query.
		String query = "select " + ALL_COLS + 
		               " from " + TABLE_NAME + 
		               " where " + FROM_ID_COL + " = ? " +
		               " and " + TO_ID_COL + " =  ? ";

		// Set and prepare the query and statement.
		setQuery(xContext, query);

	}

	/**
	 * Create a PreparedStatement to lookup this row to, from and relationship.
	 * 
	 * @param xContext  Application context.
	 * @return PreparedStatement
	 * @throws IcofException 
	 */
	public void setLookupByAllStatement(EdaContext xContext) throws IcofException {

		// Define the query.
		String query = "select " + ALL_COLS + 
		               " from " + TABLE_NAME + 
		               " where " + FROM_ID_COL + " = ? " +
		               " and " + TO_ID_COL + " =  ? " +
		               " and " + RELATIONSHIP_COL + " =  ? ";

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
		               " values( ?, ?, ?, ? )";

		// Set and prepare the query and statement.
		setQuery(xContext, query);

	}


	/**
	 * Create a PreparedStatement to delete this object
	 * 
	 * @param xContext  Application context.
	 * @return PreparedStatement
	 * @throws IcofException 
	 */
	public void setDeleteStatement(EdaContext xContext) throws IcofException {

		// Define the query.
		String query = "delete from " + TABLE_NAME + 
		               " where " + ID_COL + " = ? ";

		// Set and prepare the query and statement.
		setQuery(xContext, query);

	}


	/**
	 * Create a PreparedStatement to update this object
	 * 
	 * @param xContext  Application context.
	 * @return PreparedStatement
	 * @throws IcofException 
	 */
	public void setUpdateStatement(EdaContext xContext) 
	throws IcofException {

		// Define the query.
		String query = "update " + TABLE_NAME + 
		               " set " +  RELATIONSHIP_COL + " = ? " + 
		               " where " + ID_COL + " = ? ";

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
	 * Insert a new row.
	 * 
	 * @param xContext  An application context object.
	 * @throws          Trouble querying the database.
	 */
	public void dbAddRow(EdaContext xContext)
	throws IcofException{

		// Get the next id for this new row.
		setNextIdStatement(xContext);
		long id = getNextBigIntId(xContext);
		closeStatement(xContext);

		// Create the SQL query in the PreparedStatement.
		setAddRowStatement(xContext);
		try {
			getStatement().setLong(1, id);
			getStatement().setLong(2, getFromChangeReq().getId());
			getStatement().setLong(3, getToChangeReq().getId());
			getStatement().setString(4, getRelationshipb());
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
		setId(id);
		dbLookupById(xContext); 

	}


	/**
	 * Delete (mark as deleted) this object in the database
	 * 
	 * @param xContext  An application context object.
	 * @throws          Trouble querying the database.
	 */
	public void dbDeleteRow(EdaContext xContext)
	throws IcofException{

		// Create the SQL query in the PreparedStatement.
		setDeleteStatement(xContext);
		try {
			getStatement().setLong(1, getId());

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

	}


	/**
	 * Update the name for this object.
	 * 
	 * @param xContext         An application context object.
	 * @param newRelationship  New relationship name
	 * @throws          Trouble querying the database.
	 */
	public void dbUpdateRow(EdaContext xContext, String newRelationship)
	throws IcofException{

		// Create the SQL query in the PreparedStatement.
		setUpdateStatement(xContext);
		try {
			getStatement().setString(1, newRelationship);
			getStatement().setLong(2, getId());

		}
		catch(SQLException trap) {
			IcofException ie = new IcofException(this.getClass().getName(),
			                                     "dbUpdateRow()",
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
			                                     "dbUpdateRow()",
			                                     IcofException.SEVERE,
			                                     "Unable to insert new row.\n",
			                                     "QUERY: " + getQuery());
			xContext.getSessionLog().log(ie);
			throw ie;
		}

		// Close the PreparedStatement.
		closeStatement(xContext);

		// Set the names on this object.
		setRelationship(newRelationship);

	}


	/**
	 * Lookup this object using the from CR, to CR and relationship names
	 * 
	 * @param xContext  An application context object.
	 * @throws IcofException 
	 */
	public void dbLookupByAll(EdaContext xContext)
	throws IcofException {

		// Create the SQL query in the PreparedStatement.
		setLookupByAllStatement(xContext);

		try {
			getStatement().setLong(1, getFromChangeReq().getId());
			getStatement().setLong(2, getToChangeReq().getId());
		}
		catch(SQLException trap) {
			IcofException ie = new IcofException(this.getClass() .getName(),
			                                     "dbLookupByAll()",
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
			                                     "dbLookupByAll()",
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
	 * Create a list of ChangeRequestRelationsship objects for the FROM/TO ids.
	 * 
	 * @param xContext  An application context object.
	 * @return          Collection of Branch objects
	 * @throws IcofException 
	 * @throws          Trouble querying the database.
	 */
	public Vector<ChangeRequestRelationship_Db> dbLookupByToFrom(EdaContext xContext)
	throws IcofException {

		// Create the SQL query in the PreparedStatement.
		setLookupByToFromStatement(xContext);

		try {
			getStatement().setLong(1, getFromChangeReq().getId());
			getStatement().setLong(2, getToChangeReq().getId());
		}
		catch(SQLException trap) {
			IcofException ie = new IcofException(this.getClass() .getName(),
			                                     "dbLookupByToFrom()",
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
		Vector<ChangeRequestRelationship_Db> objects = new Vector<ChangeRequestRelationship_Db>();
		try {
			while (rs.next()) {
				long anId =  rs.getLong(ID_COL);
				ChangeRequestRelationship_Db crr = new ChangeRequestRelationship_Db(anId);
				objects.add(crr);
			}

		}
		catch(SQLException ex) {
			throw new IcofException(this.getClass().getName(), "dbLookupByToFrom()",
			                        IcofException.SEVERE, 
			                        "Error reading DB query results.",
			                        ex.getMessage());
		}

		// Close the PreparedStatement.
		closeStatement(xContext);

		return objects;

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

		setId(rs.getLong(ID_COL));
		setFromChangeReq(xContext, rs.getLong(FROM_ID_COL));
		setToChangeReq(xContext, rs.getLong(TO_ID_COL));
		setRelationship(rs.getString(RELATIONSHIP_COL));
		setLoadFromDb(true);

	}


	/**
	 * Return the members as a string.
	 */
	public String toString(EdaContext xContext) {

		// Get the class specific data
		StringBuffer buffer = new StringBuffer();
		buffer.append("ID: " + getId() + "\n");
		if (getFromChangeReq() != null) {
			buffer.append("From CR ID: " + getFromChangeReq().getId() + "\n");
		}
		else {
			buffer.append("From CR ID: NULL\n");
		}
		if (getToChangeReq() != null) {
			buffer.append("To CR ID: " + getToChangeReq().getId() + "\n");
		}
		else {
			buffer.append("To CR ID: NULL\n");
		}
		buffer.append("Relationship: " + getRelationshipb() + "\n");

		return buffer.toString();

	}

	/**
	 * Get a key from the ID.
	 * 
	 * @param xContext  Application context.
	 */
	public String getIdKey(EdaContext xContext) {
		return String.valueOf(getId());

	}

}