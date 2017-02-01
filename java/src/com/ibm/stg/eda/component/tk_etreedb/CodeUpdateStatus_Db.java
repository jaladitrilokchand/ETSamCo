/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* FILE: TkFunctionalUpdate.java
*
*-PURPOSE---------------------------------------------------------------------
* TK Code Update Status DB class with audit info
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/19/2010 GFS  Initial coding.
* 07/22/2010 GFS  Converted to using PreparedStatements.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.iipmds.common.IcofException;


public class CodeUpdateStatus_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3629255906054255509L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.CODE_UPDATE_STATUS";
    public static final String ID_COL = "CODE_UPDATE_STATUS_ID";
    public static final String STATUS_NAME_ID_COL = "CODE_UPDATE_STATUS_NAME_ID";
    public static final String USER_ID_COL = "USER_ID";
    public static final String CODE_UPDATE_ID_COL = "CODE_UPDATE_ID";
    public static final String CODE_UPDATE_TMS_COL = "CODE_UPDATE_STATUS_TMSTMP";
    public static final String ALL_COLS = ID_COL + "," + 
                                          STATUS_NAME_ID_COL + "," + USER_ID_COL + "," +
                                          CODE_UPDATE_ID_COL + "," + CODE_UPDATE_TMS_COL;

    
    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public CodeUpdateStatus_Db(long anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes CodeUpdateStatusName and CodeUpdate objects.
     * 
     * @param aCodeUpdate  A CodeUpdate object
     * @param aStatusName  A CodeUpdateStatusName object
     * @param aUserId      A user id
     */
    public CodeUpdateStatus_Db(CodeUpdate_Db anUpdate, CodeUpdateStatusName_Db aStatusName,
                            short aUserId) {
        setCodeUpdate(anUpdate);
        setCodeUpdateStatusName(aStatusName);
        setUserId(aUserId);
    }

    
    /**
     * Data Members
     */
    private long id;
    private CodeUpdate_Db codeUpdate;
    private long codeUpdateId;
    private CodeUpdateStatusName_Db statusName;
    private short userId;
    private Timestamp updatedTimestamp;
    private transient PreparedStatement lookupIdStmt;
    private transient PreparedStatement lookupCodeUpdateAndStatusStmt;
    private transient PreparedStatement addRowStmt;
    private transient PreparedStatement nextIdStmt;
    
    
    /**
     * Getters
     */
    public long getId() { return id; }
    public CodeUpdate_Db getCodeUpdate() { return codeUpdate; }
    public CodeUpdateStatusName_Db getStatusName() { return statusName; }
    public short getUserId() { return userId; }
    public long getCodeUpdateId() { return codeUpdateId; }
    public Timestamp getUpdatedTimestamp() { return updatedTimestamp; }
    public PreparedStatement getLookupIdStatement() { return lookupIdStmt; }
    public PreparedStatement getLookupCodeUpdateAndStatusStatement() { return lookupCodeUpdateAndStatusStmt; }
    public PreparedStatement getAddRowStatement() { return addRowStmt; }
    public PreparedStatement getNextIdStatement() { return nextIdStmt; }

    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setUserId(short anId) { userId = anId; }
    private void setUpdatedTimestamp(Timestamp aTms) { updatedTimestamp = aTms; }
    private void setCodeUpdate(CodeUpdate_Db anUpdate) { codeUpdate = anUpdate; }
    private void setCodeUpdateStatusName(CodeUpdateStatusName_Db aName) { statusName = aName; }
    private void setCodeUpdateId(long anId) { codeUpdateId = anId; } 
    private void setCodeUpdateStatusName(EdaContext xContext, short anId) 
    throws IcofException { 
        if (getStatusName() == null) {
            statusName = new CodeUpdateStatusName_Db(anId);
            statusName.dbLookupById(xContext);
        }
    }
    
    
    /**
     * Create a PreparedStatement to lookup this object by id.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupIdStatement(EdaContext xContext) throws IcofException {

        // Return the statement if it already exists.
        if (lookupIdStmt != null) {
            return;
        }
        
        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + ID_COL + " = ? ";
        
        // Otherwise create a statement object and return it.
        lookupIdStmt = TkDbUtils.prepStatement(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup this object by id.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupCodeUpdateAndStatusStatement(EdaContext xContext) 
    throws IcofException {

        // Return the statement if it already exists.
        if (lookupCodeUpdateAndStatusStmt != null) {
            return;
        }
        
        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + STATUS_NAME_ID_COL + " = ? " +
                       " AND " + CODE_UPDATE_ID_COL + " = ? ";

        
        // Otherwise create a statement object and return it.
        lookupCodeUpdateAndStatusStmt = TkDbUtils.prepStatement(xContext, query);
        
    }
    

    /**
     * Create a PreparedStatement to add a row.
     * 
     * @param  xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setAddRowStatement(EdaContext xContext) throws IcofException {

        // Return the statement if it already exists.
        if (addRowStmt != null) {
            return;
        }
        
        // Define the query.
        String query = "insert into " + TABLE_NAME + 
                       " ( " +  ALL_COLS + " )" + 
                       " values( ?, ?, ?, ?, ? )";
        
        addRowStmt = TkDbUtils.prepStatement(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup the next id for this table.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setNextIdStatement(EdaContext xContext) throws IcofException {

        // Return the statement if it already exists.
        if (nextIdStmt != null) {
            return;
        }
        
        // Define the query.
        String query =  TkAudit.getNextIdQuery(xContext, TABLE_NAME, ID_COL);
        
        // Otherwise create a statement object and return it.
        nextIdStmt = TkDbUtils.prepStatement(xContext, query);
        
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
            getLookupIdStatement().setLong(1, getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupById()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getLookupIdStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! selectSingleRow(xContext, getLookupIdStatement())) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupById()",
                                                 IcofException.SEVERE,
                                                 "Unable to find row for query.\n",
                                                 "QUERY: " + 
                                                 getLookupIdStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;

        }
        
        // Close the PreparedStatement.
        closeStatement(xContext, getLookupIdStatement());

    }
    

    /**
     * Look up this object by name.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByCodeUpdateAndStatus(EdaContext xContext) 
    throws IcofException {
        
        // Create the SQL query in the PreparedStatement.
        setLookupCodeUpdateAndStatusStatement(xContext);
        
        try {
            getLookupCodeUpdateAndStatusStatement().setShort(1, getStatusName().getId());
            getLookupCodeUpdateAndStatusStatement().setLong(2, getCodeUpdate().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByDelUpdateAndStatus()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getLookupCodeUpdateAndStatusStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! selectSingleRow(xContext, getLookupCodeUpdateAndStatusStatement())) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByDelUpdateAndStatus()",
                                                 IcofException.SEVERE,
                                                 "Unable to find row for query.\n",
                                                 "QUERY: " + 
                                                 getLookupCodeUpdateAndStatusStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;

        }
        
        // Close the PreparedStatement.
        closeStatement(xContext, getLookupCodeUpdateAndStatusStatement());

    }

  
    /**
     * Insert a new row.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        setNextIdStatement(xContext);
        
        long id = getNextBigIntId(xContext, getNextIdStatement());
        Timestamp now = new Timestamp(new java.util.Date().getTime());
        try {
            getAddRowStatement().setLong(1, id);
            getAddRowStatement().setShort(2, getStatusName().getId());
            getAddRowStatement().setShort(3, getUserId());
            getAddRowStatement().setLong(4, getCodeUpdate().getId());
            getAddRowStatement().setTimestamp(5, now);
            
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbAddRow()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getAddRowStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! insertRow(xContext, getAddRowStatement())) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbAddRow()",
                                                 IcofException.SEVERE,
                                                 "Unable to insert new row.\n",
                                                 "QUERY: " + 
                                                 getAddRowStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }

        // Close the PreparedStatement.
        closeStatement(xContext, getAddRowStatement());
        closeStatement(xContext, getNextIdStatement());

        // Load the data for the new row.
        setId(id);
        dbLookupById(xContext); 
        
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

        //super.populate(xContext, rs);
        setId(rs.getInt(ID_COL));
        setCodeUpdateId(rs.getLong(CODE_UPDATE_ID_COL));
        setCodeUpdateStatusName(xContext, rs.getShort(STATUS_NAME_ID_COL));
        setUserId(rs.getShort(USER_ID_COL));
        setUpdatedTimestamp(rs.getTimestamp(CODE_UPDATE_TMS_COL));
    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID: " + getId() + "\n");
        if (getCodeUpdate() != null) {
            buffer.append("CodeUpdate ID: " + getCodeUpdate().getId() + "\n");
            buffer.append("CodeUpdate revision: " + getCodeUpdate().getRevision() + "\n");
        }
        else {
            buffer.append("CodeUpdate ID: NULL\n");
            buffer.append("CodeUpdate revision: NULL\n");
        }
        buffer.append("User ID: " + getUserId() + "\n");
        buffer.append("Updated on: " + getUpdatedTimestamp().toString() + "\n");
        
        return buffer.toString();
        
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

