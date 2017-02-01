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
* TK Deliverable Update Status DB class with audit info
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


public class DeliverableUpdateStatus_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = 981987596549301848L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.DELIVERABLE_UPDATE_STATUS";
    public static final String ID_COL = "DELIVERABLE_UPDATE_STATUS_ID";
    public static final String STATUS_NAME_ID_COL = "DELIVERABLE_UPDATE_STATUS_NAME_ID";
    public static final String USER_ID_COL = "USER_ID";
    public static final String DELIVERABLE_UPDATE_ID_COL = "DELIVERABLE_UPDATE_ID";
    public static final String DELIVERABLE_UPDATE_TMS_COL = "DELIVERABLE_UPDATE_STATUS_TMSTMP";
    public static final String ALL_COLS = ID_COL + "," + 
                                          STATUS_NAME_ID_COL + "," + USER_ID_COL + "," +
                                          DELIVERABLE_UPDATE_ID_COL + "," + DELIVERABLE_UPDATE_TMS_COL;

    
    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public DeliverableUpdateStatus_Db(long anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes DeliverableUpdateStatusName and DeliverableUpdate objects.
     * 
     * @param aStatusName  A deliverableUpdateStatusName object
     * @param adeliverableUpdate  A deliverableUpdate object
     * @param aUserId      A user id
     * @param anUpdatedTime  An updated timestamp
     */
    public DeliverableUpdateStatus_Db(DeliverableUpdate_Db anUpdate,
                                   DeliverableUpdateStatusName_Db aName,
                                   short aUserId) {
        setDeliverableUpdate(anUpdate);
        setDeliverableUpdateStatusName(aName);
        setUserId(aUserId);
    }

    
    /**
     * Data Members
     */
    private long id;
    private DeliverableUpdate_Db deliverableUpdate;
    private DeliverableUpdateStatusName_Db statusName;
    private short userId;
    private long deliverableUpdateId;
    private Timestamp updatedTime;
    private transient PreparedStatement lookupIdStmt;
    private transient PreparedStatement lookupDelUpdateAndStatusStmt;
    private transient PreparedStatement addRowStmt;
    private transient PreparedStatement nextIdStmt;

    
    /**
     * Getters
     */
    public long getId() { return id; }
    public DeliverableUpdate_Db getDeliverableUpdate() { return deliverableUpdate; }
    public DeliverableUpdateStatusName_Db getStatusName() { return statusName; }
    public short getUserId() { return userId; }
    public long getDeliverableUpdateId() { return deliverableUpdateId; }
    public Timestamp getUpdatedTime() { return updatedTime; }
    public PreparedStatement getLookupIdStatement() { return lookupIdStmt; }
    public PreparedStatement getLookupDelUpdateAndStatusStatement() { return lookupDelUpdateAndStatusStmt; }
    public PreparedStatement getAddRowStatement() { return addRowStmt; }
    public PreparedStatement getNextIdStatement() { return nextIdStmt; }

    
    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setUserId(short anId) { userId = anId; }
    private void setUpdatedTime(Timestamp aTime) { updatedTime = aTime; }
    private void setDeliverableUpdate(DeliverableUpdate_Db anUpdate) { deliverableUpdate = anUpdate; }
    private void setDeliverableUpdateStatusName(DeliverableUpdateStatusName_Db aName) { statusName = aName; }
    private void setDeliverableUpdateId(long anId) { deliverableUpdateId = anId; } 
    private void setDeliverableUpdateStatusName(EdaContext xContext, short anId) 
    throws IcofException { 
        if (getStatusName() == null) {
            statusName = new DeliverableUpdateStatusName_Db(anId);
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
    public void setLookupDelUpdateAndStatusStatement(EdaContext xContext) 
    throws IcofException {

        // Return the statement if it already exists.
        if (lookupDelUpdateAndStatusStmt != null) {
            return;
        }
        
        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + STATUS_NAME_ID_COL + " = ? " +
                       " AND " + DELIVERABLE_UPDATE_ID_COL + " = ? ";

        
        // Otherwise create a statement object and return it.
        lookupDelUpdateAndStatusStmt = TkDbUtils.prepStatement(xContext, query);
        
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
    public void dbLookupByDelUpdateAndStatus(EdaContext xContext) 
    throws IcofException {
        
        // Create the SQL query in the PreparedStatement.
        setLookupDelUpdateAndStatusStatement(xContext);
        
        try {
            getLookupDelUpdateAndStatusStatement().setShort(1, getStatusName().getId());
            getLookupDelUpdateAndStatusStatement().setLong(2, getDeliverableUpdate().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByDelUpdateAndStatus()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getLookupDelUpdateAndStatusStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! selectSingleRow(xContext, getLookupDelUpdateAndStatusStatement())) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByDelUpdateAndStatus()",
                                                 IcofException.SEVERE,
                                                 "Unable to find row for query.\n",
                                                 "QUERY: " + 
                                                 getLookupDelUpdateAndStatusStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;

        }

        // Close the PreparedStatement.
        closeStatement(xContext, getLookupDelUpdateAndStatusStatement());
        
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
            getAddRowStatement().setLong(4, getDeliverableUpdate().getId());
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

        setId(rs.getInt(ID_COL));
        setDeliverableUpdateId(rs.getLong(DELIVERABLE_UPDATE_ID_COL));
        setDeliverableUpdateStatusName(xContext, rs.getShort(STATUS_NAME_ID_COL));
        setUserId(rs.getShort(USER_ID_COL));
        setUpdatedTime(rs.getTimestamp(DELIVERABLE_UPDATE_TMS_COL));
        
    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID: " + getId() + "\n");
        if (getDeliverableUpdate() != null) {
            buffer.append("DeliverableUpdate ID: " + getDeliverableUpdate().getId() + "\n");
        }
        else {
            buffer.append("DeliverableUpdate ID: NULL\n");
        }
        if (getStatusName() != null) {
            buffer.append("Status Name: " + getStatusName().getName() + "\n");
        }
        else {
            buffer.append("Status Name: NULL\n");
        }

        buffer.append("User ID: " + getUserId() + "\n");
        buffer.append("Updated on: " + getUpdatedTime() + "\n");
        
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

