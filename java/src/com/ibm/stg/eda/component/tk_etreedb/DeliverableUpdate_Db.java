/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* FILE: DeliverableUpdate.java
*
*-PURPOSE---------------------------------------------------------------------
* DeliverableUpdate DB class with audit info
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/20/2010 GFS  Initial coding.
* 07/22/2010 GFS  Converted to using PreparedStatements.
* 08/04/2010 GFS  Converted to using storing DeliverableUpdateStatus ID instead
*                 of DeliverableUpdateStatusName for current status.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.iipmds.common.IcofException;


public class DeliverableUpdate_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8948259900584369683L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.DELIVERABLE_UPDATE";
    public static final String ID_COL = "DELIVERABLE_UPDATE_ID";
    public static final String PLATFORM_ID_COL = "PLATFORM_ID";
    public static final String CURRENT_STATUS_ID_COL = "CURRENT_STATUS_ID";
    public static final String ALL_COLS = ID_COL + "," + PLATFORM_ID_COL + "," +
                               CURRENT_STATUS_ID_COL;

    
    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public DeliverableUpdate_Db(long anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes a TkPlatform object
     * 
     * @param anId  A database id
     */
    public DeliverableUpdate_Db(Platform_Db aPlatform, 
                             DeliverableUpdateStatus_Db aStatus) {
        setPlatform(aPlatform);
        setCurrentStatus(aStatus);
    }

    
    /**
     * Data Members
     */
    private long id;
    private Platform_Db platform;
    private DeliverableUpdateStatus_Db currentStatus;
    private transient PreparedStatement lookupIdStmt;
    private transient PreparedStatement updateCurrStatusStmt;
    private transient PreparedStatement addRowStmt;
    private transient PreparedStatement nextIdStmt;

    
    /**
     * Getters
     */
    public long getId() { return id; }
    public Platform_Db getPlatform() { return platform; }
    public DeliverableUpdateStatus_Db getCurrentStatus() { return currentStatus; }
    public PreparedStatement getLookupIdStatement() { return lookupIdStmt; }
    public PreparedStatement getUpdateCurrStatusStatement() { return updateCurrStatusStmt; }
    public PreparedStatement getAddRowStatement() { return addRowStmt; }
    public PreparedStatement getNextIdStatement() { return nextIdStmt; }


    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setPlatform(Platform_Db aPlat) { platform = aPlat; }
    private void setCurrentStatus(DeliverableUpdateStatus_Db aStatus) { currentStatus = aStatus; }

    private void setPlatform(EdaContext xContext, short anId) throws IcofException { 
        if (getPlatform() == null) {
            platform = new Platform_Db(anId);
            platform.dbLookupById(xContext);
        }
    }

    private void setCurrentStatus(EdaContext xContext, long anId) throws IcofException { 
        if ((getCurrentStatus() == null) && (anId > 0)){
            currentStatus = new DeliverableUpdateStatus_Db(anId);
            currentStatus.dbLookupById(xContext);
        }
    }
    
    
    /**
     * Create a PreparedStatement to lookup this object by id.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupIdStatement(EdaContext xContext) throws IcofException {

        // Need to figure out how to test if the PreparedStatement is already closed.
//        // Return the statement if it already exists.
//        if ((lookupIdStmt != null) || (lookupIdStmt.) {
//            return;
//        }
        
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
    public void setUpdateCurrStatusStatement(EdaContext xContext) throws IcofException {

        // Return the statement if it already exists.
        if (updateCurrStatusStmt != null) {
            return;
        }
        
        // Define the query.
        String query = "update " + TABLE_NAME + 
                       " set " +  CURRENT_STATUS_ID_COL + " = ? " + 
                       " where " + ID_COL + " = ? ";
        
        // Otherwise create a statement object and return it.
        updateCurrStatusStmt = TkDbUtils.prepStatement(xContext, query);
        
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
                       " values( ?, ?, NULL )";
        
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
     * Update the current status for this object.
     * 
     * @param xContext  An application context object.
     * @param newStatus The new status for this object.
     * @throws          Trouble querying the database.
     */
    public void dbUpdateCurrentStatus(EdaContext xContext, 
                                      DeliverableUpdateStatus_Db newStatus)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setUpdateCurrStatusStatement(xContext);
        try {
            getUpdateCurrStatusStatement().setLong(1, newStatus.getId());
            getUpdateCurrStatusStatement().setLong(2, getId());

        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbUpdateCurrectStatus()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getUpdateCurrStatusStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! insertRow(xContext, getUpdateCurrStatusStatement())) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbUpdateCurrentStatus()",
                                                 IcofException.SEVERE,
                                                 "Unable to insert new row.\n",
                                                 "QUERY: " + 
                                                 getUpdateCurrStatusStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }

        // Close the PreparedStatement.
        closeStatement(xContext, getUpdateCurrStatusStatement());
        
        // Set the current status to the new status.
        setCurrentStatus(newStatus);
        
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
        try {

            getAddRowStatement().setLong(1, id);
            getAddRowStatement().setShort(2, getPlatform().getId());
            //getAddRowStatement().setShort(3, getCurrentStatus().getId());

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
        setPlatform(xContext, rs.getShort(PLATFORM_ID_COL));
        setCurrentStatus(xContext, rs.getLong(CURRENT_STATUS_ID_COL));
        
    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID: " + getId() + "\n");
        if (getPlatform() != null) {
            buffer.append("Platform ID: " + getPlatform().getId() + "\n");
            buffer.append("Platform name: " + getPlatform().getName() + "\n");
        }
        else {
            buffer.append("Platform ID: NULL" + "\n");
            buffer.append("Platform name: NULL" + "\n");
        }
        if (getCurrentStatus() != null) {
            buffer.append("Status: " + getCurrentStatus().toString(xContext) + "\n");
        }
        else {
            buffer.append("Status: NULL" + "\n");
        }
        
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
