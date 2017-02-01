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
* TK Component DB class with audit info
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/18/2010 GFS  Initial coding.
* 07/23/2010 GFS  Converted to using PreparedStatements.
* 08/11/2010 GFS  Added dbAddRow() method.
* 10/05/2010 GFS  Added dbUpdateRow() and a new constructor.
* 11/11/2010 GFS  Removed builder, transmitter and team lead role information.
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

public class RelComponent_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7600319493941423168L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.COMPONENT_TKRELEASE";
    public static final String ID_COL = "COMPONENT_TKRELEASE_ID";
    public static final String COMP_ID_COL = "COMPONENT_ID";
    public static final String REL_ID_COL = "TKRELEASE_ID";
    public static final String ALL_COLS = ID_COL + "," + 
                                          COMP_ID_COL + "," + REL_ID_COL + "," +
                                          CREATED_BY_COL + "," + CREATED_ON_COL + "," + 
                                          UPDATED_BY_COL + "," + UPDATED_ON_COL + "," +
                                          DELETED_BY_COL + "," + DELETED_ON_COL;

    
    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public RelComponent_Db(short anId) {
        setId(anId);
    }


    /**
     * Constructor - takes TK Release and Component objects.
     * 
     * @param aRelease        A TkRelease object
     * @param aComponent      A TkComponent object
     */
    public RelComponent_Db(Release_Db aRelease, Component_Db aComponent) {
        setComponent(aComponent);
        setRelease(aRelease);
    }


    /**
     * Constructor - takes TK Release and Component objects.
     * 
     * @param anId            An id
     * @param aRelease        A TkRelease object
     * @param aComponent      A TkComponent object
     * @param aBuilderId      Id of a Builder(user) database row
     * @param aTeamLeadId     Id of a Team Lead(user) database row
     * @param aTransmitterId  Id of a Transmitter(user) database row
     */
    public RelComponent_Db(short anId, Release_Db aRelease, Component_Db aComponent) {
        setId(anId);
        setComponent(aComponent);
        setRelease(aRelease);
    }

    
    
    /**
     * Constructor - takes TK Release and Component IDs
     * 
     * @param xContext    Application context object.
     * @param releaeId    Id of a TkRelease database row
     * @param componentId Id of a Component database row
     *
     * @throws Trouble looking up the TkRelease or Component objects
     */
    public RelComponent_Db(EdaContext xContext, short releaseId, short componentId)
    throws IcofException {
        setRelease(xContext, releaseId);
        setComponent(xContext, componentId);
    }

    
    /**
     * Data Members
     */
    private short id;
    private Release_Db release;
    private Component_Db component;
    private transient PreparedStatement lookupIdStmt;
    private transient PreparedStatement lookupRelCompStmt;
    private  transient PreparedStatement addRowStmt;
    private  transient PreparedStatement updateRowStmt;
    private  transient PreparedStatement nextIdStmt;

    
    /**
     * Getters
     */
    public short getId() { return id; }
    public Release_Db getRelease() { return release; }
    public Component_Db getComponent() { return component; }
    public PreparedStatement getLookupIdStatement() { return lookupIdStmt; }
    public PreparedStatement getLookupRelCompStatement() { return lookupRelCompStmt; }
    public PreparedStatement getAddRowStatement() { return addRowStmt; }
    public PreparedStatement getUpdateRowStatement() { return updateRowStmt; }
    public PreparedStatement getNextIdStatement() { return nextIdStmt; }


    /**
     * Setters
     */
    private void setId(short anId) { id = anId; }
    private void setRelease(Release_Db aRel) { release = aRel; }
    private void setComponent(Component_Db aComp) { component = aComp; }
    private void setRelease(EdaContext xContext, short anId) throws IcofException {
        if (release == null) {
            release = new Release_Db(anId);
            release.dbLookupById(xContext);
        }
    }
    private void setComponent(EdaContext xContext, short anId) throws IcofException {
        if (component == null) {
            component = new Component_Db(anId);
            component.dbLookupById(xContext);
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
                       " where " + ID_COL + " = ? " +
                       " AND " + DELETED_ON_COL + " is NULL";
        
        // Otherwise create a statement object and return it.
        lookupIdStmt = TkDbUtils.prepStatement(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup this object by id.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupRelCompStatement(EdaContext xContext) throws IcofException {

        // Return the statement if it already exists.
        if (lookupRelCompStmt != null) {
            return;
        }
        
        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + COMP_ID_COL + " =  ? "+
                       " AND " + REL_ID_COL + " =  ? " +
                       " AND " + DELETED_ON_COL + " is NULL";
        
        // Otherwise create a statement object and return it.
        lookupRelCompStmt = TkDbUtils.prepStatement(xContext, query);
        
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
                       " values( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        
        addRowStmt = TkDbUtils.prepStatement(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to update a row.
     * 
     * @param  xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setUpdateRowStatement(EdaContext xContext) throws IcofException {

        // Return the statement if it already exists.
        if (updateRowStmt != null) {
            return;
        }
        
        // Define the query.
        String query = "update " + TABLE_NAME + 
                       " set " +  UPDATED_BY_COL + " = ?, " +
                       UPDATED_ON_COL + " = ?" +
                       " where " + ID_COL + " = ? ";
        //System.out.println("Update query: " + query);
        
        updateRowStmt = TkDbUtils.prepStatement(xContext, query);
        
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
     * Look up the Component by id.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupById(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupIdStatement(xContext);
        
        try {
            getLookupIdStatement().setShort(1, getId());
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
        
    }
    

    /**
     * Look up the Component by name.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByRelComp(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupRelCompStatement(xContext);
        
        try {
            getLookupRelCompStatement().setShort(1, getComponent().getId());
            getLookupRelCompStatement().setShort(2, getRelease().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByName()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getLookupRelCompStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! selectSingleRow(xContext, getLookupRelCompStatement())) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByName()",
                                                 IcofException.SEVERE,
                                                 "Unable to find row for query.\n",
                                                 "QUERY: " + 
                                                 getLookupRelCompStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;

        }
        
    }

    
    /**
     * Insert a new row.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext, User_Db user)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        setNextIdStatement(xContext);
        
        setId(getNextSmallIntId(xContext, getNextIdStatement()));
        Timestamp now = new Timestamp(new java.util.Date().getTime());
        try {
            getAddRowStatement().setLong(1, getId());
            getAddRowStatement().setShort(2, getComponent().getId());
            getAddRowStatement().setShort(3, getRelease().getId());
            getAddRowStatement().setString(4, user.getIntranetId());
            getAddRowStatement().setTimestamp(5, now);
            getAddRowStatement().setString(6, user.getIntranetId());
            getAddRowStatement().setTimestamp(7, now);
            getAddRowStatement().setString(8, null);
            getAddRowStatement().setString(9, null);
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
        try {
           getAddRowStatement().close();
           getNextIdStatement().close();
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

        
        // Load the data for the new row.
        dbLookupById(xContext); 
        
    }

    
    /**
     * Update the builder, team lead or transmitter ids this object.
     * 
     * @param xContext  An application context object.
     * @param editor    A User object for the person making the update.
     * @throws          Trouble querying the database.
     */
    public void dbUpdateRow(EdaContext xContext, User_Db editor)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setUpdateRowStatement(xContext);
        Timestamp now = new Timestamp(new java.util.Date().getTime());
        try {
            getUpdateRowStatement().setString(1, editor.getIntranetId());
            //System.out.println("Updated by (1): " + editor.getIntranetId());
            
            getUpdateRowStatement().setTimestamp(2, now);
            //System.out.println("Updated on (2): " + now.toString());
            
            getUpdateRowStatement().setShort(6, getId());
            //System.out.println("TkRelComp id (3): " + getId());

        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbUpdateRow()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getUpdateRowStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! insertRow(xContext, getUpdateRowStatement())) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbUpdateRow()",
                                                 IcofException.SEVERE,
                                                 "Unable to update selected row.\n",
                                                 "QUERY: " + 
                                                 getUpdateRowStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
     
    }


    /**
     * Populate this object from the result set.
     * 
     * @param xContext  Application context.
     * @param rs        A valid result set.
     * @throws IcofException 
     * @throws SQLException
     */
    protected void populate(EdaContext xContext, ResultSet rs) 
    throws SQLException, IcofException {
        
        super.populate(xContext, rs);
        setId(rs.getShort(ID_COL));

        setComponent(xContext, rs.getShort(COMP_ID_COL));
        setRelease(xContext, rs.getShort(REL_ID_COL));

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
        if (getComponent() != null) {
            buffer.append("Component ID: " + getComponent().getId() + "\n");
            buffer.append("Component name: " + getComponent().getName() + "\n");
        }
        else {
            buffer.append("Component ID: NULL\n");
            buffer.append("Component name: NULL\n");
        }
        if (getRelease() != null) {
            buffer.append("Release ID: " + getRelease().getId() + "\n");
            buffer.append("Release name: " + getRelease().getName() + "\n");
        }
        else {
            buffer.append("Release ID: NULL\n");
            buffer.append("Relasse name: NULL\n");
        }
        buffer.append(audit);
        
        return buffer.toString();
        
    }
    
}
