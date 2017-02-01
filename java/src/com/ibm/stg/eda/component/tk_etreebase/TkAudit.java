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
 * Base DB class with audit info
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 05/18/2010 GFS  Initial coding.
 * 07/21/2010 GFS  Changed Date objects to Timestamp objects.
 * 07/23/2010 GFS  Converted to using PreparedStatements.
 * 02/09/2011 GFS  Added closeStatement() method.
 * 05/04/2011 GFS  Added updateRows() method.
 * 05/17/2011 GFS  Added query and preparedStatement members.
 * 06/06/2010 GFS  Added loadedFromDb member, setter and getter.
 * 07/18/2011 GFS  Added printQuery().
 * 10/19/2011 GFS  Added formatForInsert()
 * 03/06/2012 GFS  Added CURRENT_TMS constant
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreebase;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class TkAudit implements Serializable {

    private static final long serialVersionUID = 5353474251184605117L;

    /**
     * Constants.
     */
    public static final String CREATED_BY_COL = "CREATED_BY";
    public static final String CREATED_ON_COL = "CREATED_TMSTMP";
    public static final String UPDATED_BY_COL = "UPDATED_BY";
    public static final String UPDATED_ON_COL = "UPDATED_TMSTMP";
    public static final String DELETED_BY_COL = "DELETED_BY";
    public static final String DELETED_ON_COL = "DELETED_TMSTMP";
    public static final String CURRENT_TMS = "CURRENT TIMESTAMP";


    /**
     * Constructor - empty
     */
    public TkAudit() { }


    /**
     * Constructor - takes all members
     * 
     * @param aCreatedBy
     * @param aCreatedOn
     * @param anUpdatedBy
     * @param anUpdatedOn
     * @param aDeletedBy
     * @param aDeletedOn
     * @param aQuery
     * @param aStatement
     */
    public TkAudit(String aCreatedBy,
                   Timestamp aCreatedOn,
                   String anUpdatedBy,
                   Timestamp anUpdatedOn,
                   String aDeletedBy,
                   Timestamp aDeletedOn,
                   String aQuery,
                   PreparedStatement aStatement) {
	setCreatedBy(aCreatedBy);
	setCreatedOn(aCreatedOn);
	setUpdatedBy(anUpdatedBy);
	setUpdatedOn(anUpdatedOn);
	setDeletedBy(aDeletedBy);
	setDeletedOn(aDeletedOn);
	setQuery(aQuery);
	setStatement(aStatement);
    }


    /**
     * Constructor - takes audit members
     * 
     * @param aCreatedBy
     * @param aCreatedOn
     * @param anUpdatedBy
     * @param anUpdatedOn
     * @param aDeletedBy
     * @param aDeletedOn
     */
    public TkAudit(String aCreatedBy,
                   Timestamp aCreatedOn,
                   String anUpdatedBy,
                   Timestamp anUpdatedOn,
                   String aDeletedBy,
                   Timestamp aDeletedOn) {
	setCreatedBy(aCreatedBy);
	setCreatedOn(aCreatedOn);
	setUpdatedBy(anUpdatedBy);
	setUpdatedOn(anUpdatedOn);
	setDeletedBy(aDeletedBy);
	setDeletedOn(aDeletedOn);
    }


    /**
     * Data Members
     */
    private boolean loadedFromDb = false;
    private String createdBy;
    private Timestamp createdOn;
    private String updatedBy;
    private Timestamp updatedOn;
    private String deletedBy;
    private Timestamp deletedOn;
    private String query;
    private transient PreparedStatement statement;
    private transient PreparedStatement lookupIdStmt;
    private transient PreparedStatement addRowStmt;
    private transient PreparedStatement nextIdStmt;

    /**
     * Getters
     */
    public boolean isLoaded() { return loadedFromDb; }
    public String getCreatedBy() { return createdBy; }
    public Timestamp getCreatedOn() { return createdOn; }
    public String getUpdatedBy() { return updatedBy; }
    public Timestamp getUpdatedOn() { return updatedOn; }
    public String getDeletedBy() { return deletedBy; }
    public Timestamp getDeletedOn() { return deletedOn; }
    public String getQuery() { return query; }
    public PreparedStatement getStatement() { return statement; }
    public PreparedStatement getLookupIdStatement() { return lookupIdStmt; }
    public PreparedStatement getAddRowStatement() { return addRowStmt; }
    public PreparedStatement getNextIdStatement() { return nextIdStmt; }

    /**
     * Setters
     */
    protected void setLoadFromDb(boolean aFlag) { loadedFromDb = aFlag; }
    protected void setCreatedBy(String aCreatedBy) { createdBy = aCreatedBy; }
    protected void setCreatedOn(Timestamp aCreatedOn) { createdOn = aCreatedOn; }
    protected void setUpdatedBy(String anUpdatedBy) { updatedBy = anUpdatedBy; }
    protected void setUpdatedOn(Timestamp anUpdatedOn) { updatedOn = anUpdatedOn; }
    protected void setDeletedBy(String aDeletedBy) { deletedBy = aDeletedBy; }
    protected void setDeletedOn(Timestamp aDeletedOn) { deletedOn = aDeletedOn; }
    protected void setQuery(String aQuery) { query = aQuery; }
    protected void setStatement(PreparedStatement aStmt) { statement = aStmt; }
    protected void setQuery(EdaContext xContext, String aQuery) throws IcofException {
	query = aQuery; 
	setStatement(TkDbUtils.prepStatement(xContext, getQuery()));
    }

    public void setLookupIdStmt(PreparedStatement aStmt) {lookupIdStmt = aStmt;}
    public void setAddRowStmt(PreparedStatement aStmt) { addRowStmt = aStmt;}
    public void setNextIdStmt(PreparedStatement aStmt) { nextIdStmt = aStmt;}


    /**
     * Print the query.
     * 
     *  @param xContext  Application context object.
     */
    public void printQuery(EdaContext xContext) {
	System.out.println("Query: " + getQuery());
    }


    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
	return String.valueOf(TkDbUtils.EMPTY);
    }


    /**
     * Select a specific row that matches the query. 
     * 
     * @param xContext   Application context
     * @return           True if row found otherwise false.
     */
    protected boolean selectSingleRow(EdaContext xContext) 
    throws IcofException {
	return selectSingleRow(xContext, getStatement());
    }


    /**
     * Select a specific row that matches the query. 
     * 
     * @param xContext   Application context
     * @param aStatement A prepared DB2 query.
     * @return           True if row found otherwise false.
     */
    protected boolean selectSingleRow(EdaContext xContext, PreparedStatement aStatement) 
    throws IcofException {

	// Run the query.
	try {
	    ResultSet rs = executeQuery(xContext, aStatement);
	    boolean found = rs.next();

	    if (found)
		populate(xContext, rs);
	  
	    // Close this PreparedStatement and release DB resources.
	    aStatement.close();

	    return found;

	}
	catch(Exception e) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "selectSingleRow()",
	                                         IcofException.SEVERE,
	                                         e.getMessage(), 
	                                         aStatement.toString());
	    xContext.getSessionLog().log(ie);
	    throw ie;

	}

    }


    /**
     * Populate this object from the result set.
     * 
     * @param xContext  Application context.
     * @param rs        A valid result set.
     * @throws          Trouble retrieving the data.
     */
    protected void populate(EdaContext xContext, ResultSet rs) 
    throws SQLException, IcofException {
	setCreatedBy(rs.getString(CREATED_BY_COL));
	setCreatedOn(rs.getTimestamp(CREATED_ON_COL));
	setUpdatedBy(rs.getString(UPDATED_BY_COL));
	setUpdatedOn(rs.getTimestamp(UPDATED_ON_COL));
	setDeletedBy(rs.getString(DELETED_BY_COL));
	setDeletedOn(rs.getTimestamp(DELETED_ON_COL));

    }


    /**
     * Return the members as a string.
     */
    protected String toString(EdaContext xContex) {

	StringBuffer buffer = new StringBuffer();
	buffer.append("Created by: " + getCreatedBy() + "\n");
	if (getCreatedOn() == null)
	    buffer.append("Created on: null\n");
	else
	    buffer.append("Created on: " + getCreatedOn().toString() + "\n");
	buffer.append("Updated by: " + getUpdatedBy() + "\n");
	if (getUpdatedOn() == null)
	    buffer.append("Updated on: null\n");
	else
	    buffer.append("Updated on: " + getUpdatedOn().toString() + "\n");
	buffer.append("Deleted by: " + getDeletedBy() + "\n");
	if (getDeletedOn() == null)
	    buffer.append("Deleted on: null\n");
	else
	    buffer.append("Deleted on: " + getDeletedOn().toString() + "\n");

	return buffer.toString();

    }


    /**
     * Insert a new row based. 
     * 
     * @param xContext   Application context
     * @return           True if row added otherwise false.
     */
    protected boolean insertRow(EdaContext xContext) 
    throws IcofException {
	return insertRow(xContext, getStatement());
    }


    /**
     * Insert a new row based on a PreparedStatement. 
     * 
     * @param xContext   Application context
     * @param aStatement A prepared DB2 query.
     * @return           True if row added otherwise false.
     */
    protected boolean insertRow(EdaContext xContext, PreparedStatement aStatement) 
    throws IcofException {

	// Run the query.
	try {
	    int numUpdates = executeUpdate(xContext, aStatement);
	    if (numUpdates != 1)
		return false;

	    return true;

	}
	catch(Exception e) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "insertRow()",
	                                         IcofException.SEVERE,
	                                         e.getMessage(), aStatement.toString());
	    xContext.getSessionLog().log(ie);
	    throw ie;

	}

    }


    /**
     * Update rows. 
     * 
     * @param xContext   Application context
     * @return           True if row added otherwise false.
     */
    protected boolean updateRows(EdaContext xContext) 
    throws IcofException {
	return updateRows(xContext, getStatement());
    }


    /**
     * Update rows based on a PreparedStatement. 
     * 
     * @param xContext   Application context
     * @param aStatement A prepared DB2 query.
     * @return           True if row added otherwise false.
     */
    protected boolean updateRows(EdaContext xContext, PreparedStatement aStatement) 
    throws IcofException {

	// Run the query.
	try {
	    int numUpdates = executeUpdate(xContext, aStatement);
	    if (numUpdates < 1)
		return false;

	    return true;

	}
	catch(Exception e) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "insertRow()",
	                                         IcofException.SEVERE,
	                                         e.getMessage(), aStatement.toString());
	    xContext.getSessionLog().log(ie);
	    throw ie;

	}

    }


    /**
     * Determine the next BIGINT ID. 
     * 
     * @param xContext    Application context
     * @return            Next available ID.
     */
    public long getNextBigIntId(EdaContext xContext) 
    throws IcofException {
	return getNextBigIntId(xContext, getStatement());
    }


    /**
     * Determine the next BIGINT ID for the PreparedStatement. 
     * 
     * @param xContext    Application context
     * @param aStatement  A PreparedStatement to run
     * @return            Next available ID.
     */
    public long getNextBigIntId(EdaContext xContext, PreparedStatement aStatement) 
    throws IcofException {

	ResultSet rs = null;
	// Run the query.
	try {
	    rs = aStatement.executeQuery();
	    boolean found = rs.next();
	    if (found) {
		long lastId = rs.getLong(1) + 1;
		return lastId;
	    }

	    return TkDbUtils.START_ID;

	}
	catch(Exception e) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "getNextBigIntId()",
	                                         IcofException.SEVERE,
	                                         e.getMessage(), 
	                                         aStatement.toString());
	    xContext.getSessionLog().log(ie);
	    throw ie;

	}
	finally{
	    try {
		rs.close();
	    }
	    catch (SQLException e) {
		e.printStackTrace();
	    }
	}

    }


    /**
     * Determine the next INT ID. 
     * 
     * @param xContext    Application context
     * @return            Next available ID.
     */
    public int getNextIntId(EdaContext xContext) 
    throws IcofException {
	return getNextIntId(xContext, getStatement());
    }


    /**
     * Determine the next INT ID for the PreparedStatement. 
     * 
     * @param xContext    Application context
     * @param aStatement  A PreparedStatement to run
     * @return            Next available ID.
     */
    public int getNextIntId(EdaContext xContext, PreparedStatement aStatement) 
    throws IcofException {

	ResultSet rs = null;
	// Run the query.
	try {
	    rs = aStatement.executeQuery();
	    boolean found = rs.next();
	    if (found) {
		int lastId = rs.getInt(1) + 1;
		return lastId;
	    }

	    return TkDbUtils.START_ID;

	}
	catch(Exception e) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "getNextIntId()",
	                                         IcofException.SEVERE,
	                                         e.getMessage(), 
	                                         aStatement.toString());
	    xContext.getSessionLog().log(ie);
	    throw ie;

	} 
	finally{
	    try {
		rs.close();
	    }
	    catch (SQLException e) {
		e.printStackTrace();
	    }
	}

    }


    /**
     * Determine the next SMALLINT ID. 
     * 
     * @param xContext    Application context
     * @return            Next available ID.
     */
    public short getNextSmallIntId(EdaContext xContext) 
    throws IcofException {
	return getNextSmallIntId(xContext, getStatement());
    }


    /**
     * Determine the next SMALLINT ID for the PreparedStatement. 
     * 
     * @param xContext    Application context
     * @param aStatement  A PreparedStatement to run
     * @return            Next available ID.
     */
    public short getNextSmallIntId(EdaContext xContext, PreparedStatement aStatement) 
    throws IcofException {

	ResultSet rs = null;
	// Run the query.
	try {
	    rs = aStatement.executeQuery();
	    boolean found = rs.next();
	    if (found) {
		short lastId = (short) (rs.getShort(1) + 1);
		return lastId;
	    }

	    return TkDbUtils.START_ID;

	}
	catch(Exception e) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "getNextSmallIntId()",
	                                         IcofException.SEVERE,
	                                         e.getMessage(), 
	                                         aStatement.toString());
	    xContext.getSessionLog().log(ie);
	    throw ie;

	}
	finally{
	    try {
		rs.close();
	    }
	    catch (SQLException e) {
		e.printStackTrace();
	    }
	}

    }


    /**
     * Set the next DB id query.
     * 
     * @param xContext  Application context
     * @param table     DB table name
     * @param idColumn  DB ID column name
     * @return  DB query
     */
    public void setNextIdQuery(EdaContext xContext, String table, String idColumn) {
	query = "select max(" + idColumn + ") from " + table;
    }


    /**
     * Construct the get next DB id query.
     * 
     * @param xContext  Application context
     * @param table     DB table name
     * @param idColumn  DB ID column name
     * @return  DB query
     */
    public static String getNextIdQuery(EdaContext xContext, String table,
                                        String idColumn) {
	return "select max(" + idColumn + ") from " + table;
    }


    /**
     * Closes a prepared statement.
     * 
     *  @param xContext    Application context
     *  @throws  IcofException
     */
    public void closeStatement(EdaContext xContext)
    throws IcofException {
	closeStatement(xContext, getStatement());
    }


    /**
     * Closes the prepared statement.
     * 
     *  @param xContext    Application context
     *  @param aStatement  Statement to close
     *  @throws  IcofException
     */
    public void closeStatement(EdaContext xContext, PreparedStatement aStatement)
    throws IcofException {

	// Close the PreparedStatement.
	try {
	    aStatement.close();
	} 
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "closeStatement()", 
	                                         IcofException.SEVERE,
	                                         "Unable to close the SQL statement.",
	                                         IcofException.printStackTraceAsString(trap) + 
	                                         "\n" + aStatement.toString());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

    }


    /**
     * Execute the specified SQL query.
     * 
     *  @param xContext   Application context object.
     *  @return           A ResultSet containing the query results
     *  @throws           IcofException
     */
    public ResultSet executeQuery(EdaContext xContext)
    throws IcofException {
	return executeQuery(xContext, getStatement());
    }


    /**
     * Execute the specified SQL update request.
     * 
     *  @param xContext   Application context object.
     *  @param aStatement A prepared db2 query.
     *  @return           Number of rows updated.
     *  @throws           IcofException
     */
    public int executeUpdate(EdaContext xContext) throws IcofException {
	return executeUpdate(xContext, getStatement());
    }


    /**
     * Execute the specified SQL update request.
     * 
     *  @param xContext   Application context object.
     *  @param aStatement A prepared db2 query.
     *  @return           Number of rows updated.
     *  @throws           IcofException
     */
    public int executeUpdate(EdaContext xContext, 
                             PreparedStatement aStatement)
                             throws IcofException {

	// Log the query
	//xContext.getSessionLog().log(SessionLog.INFO, aStatement.toString());

	// Execute the query.
	try {
	    int numRowsUpdated = aStatement.executeUpdate();
	    aStatement.close();
	    return numRowsUpdated;

	}
	catch (Exception e) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "executeUpdate()",
	                                         IcofException.SEVERE,
	                                         e.getMessage(), aStatement.toString());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

    }


    /**
     * Execute the specified SQL query.
     * 
     *  @param xContext   Application context object.
     *  @param aStatement A prepared SQL query.
     *  @return           A ResultSet containing the query results
     *  @throws           IcofException
     */
    public ResultSet executeQuery(EdaContext xContext,
                                  PreparedStatement aStatement)
                                  throws IcofException {

	// Execute the query.
	ResultSet results = null;
	try {
	    results = aStatement.executeQuery();
	}
	catch (Exception e) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "executeQuery()",
	                                         IcofException.SEVERE,
	                                         e.getMessage(), aStatement.toString());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	return results;

    }

    /**
     * Format the given string so it can be added to the db2 without errors.
     * (ie, if string includes single quotes then set the correct escape char
     * for a for a single quote.)
     * 
     * @param xContext  Application context
     * @param text      Text to be formated for insertion
     * @return  String
     * @throws IcofException 
     */
    public static String formatForInsert(EdaContext xContext, String text) 
    throws IcofException {

	String copy = text;

	// Determine if text is already in correct format for DB insert
	if ((IcofStringUtil.occurrencesOf(text, "''") == 1) &&
	    (IcofStringUtil.occurrencesOf(text, "'''") == 0))
	    return copy;
	else if (IcofStringUtil.occurrencesOf(text, "''") == 0)
	    copy = text.replaceAll("'", "''");
	else {

	    IcofException ie = 
	    new IcofException("TkAudit",
	                      "formateText()", 
	                      IcofException.SEVERE,
	                      "The string contains 3 or more consecutive single quotes.", 
	                      text);
	    xContext.getSessionLog().log(ie);
	    throw ie;

	}

	return copy;

    }

}
