/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* FILE: CodeUpdate.java
*
*-PURPOSE---------------------------------------------------------------------
* CodeUpdate DB class with audit info
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/19/2010 GFS  Initial coding.
* 07/22/2010 GFS  Converted to using PreparedStatements.
* 08/04/2010 GFS  Converted to using storing CodeUpdateStatus ID instead of 
*                 CodeUpdateStatusName for current status.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.iipmds.common.IcofException;


public class ComponentVersion_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1079991486058194331L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.CODE_UPDATE";
    public static final String ID_COL = "CODE_UPDATE_ID";
    public static final String REVISION_COL = "SVNREVISION";
    public static final String BRANCH_COL = "BRANCH_NAME";
    public static final String COMP_VERSION_ID_COL = "COMPONENT_TKVERSION_ID";
    public static final String CURRENT_STATUS_ID_COL = "CURRENT_STATUS_ID";
    public static final String ALL_COLS = ID_COL + "," + COMP_VERSION_ID_COL + "," +
                                          REVISION_COL + "," + BRANCH_COL + "," +
                                          CURRENT_STATUS_ID_COL;

    
    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public ComponentVersion_Db(long anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes a TkComponent object, SVN revision and a 
     * branch name.
     * 
     * @param aCompVer    Component_TkVersion_Db object
     * @param aRevision   The SVN revision number
     * @param aBranch     The branch name
     * @param aStatus     The current status of this update
     */
    public ComponentVersion_Db(Component_Version_Db aCompVer, 
    			         String aRevision,  String aBranch, 
    			         CodeUpdateStatus_Db aStatus) {
        setCompVersion(aCompVer);
        setRevision(aRevision);
        setBranch(aBranch);
        setCurrentStatus(aStatus);
    }

    
    /**
     * Data Members
     */
    private long id;
    private String revision;
    private String branch;
    private Component_Version_Db compVersion;
    private CodeUpdateStatus_Db currentStatus;
    private transient PreparedStatement lookupIdStmt;
    private transient PreparedStatement lookupCompRevStmt;
    private transient PreparedStatement updateCurrStatusStmt;
    private transient PreparedStatement addRowStmt;
    private transient PreparedStatement nextIdStmt;

    
    
    /**
     * Getters
     */
    public long getId() { return id; }
    public String getRevision() { return revision; }
    public String getBranch() { return branch; }
    public Component_Version_Db getCompVersion() { return compVersion; }
    public CodeUpdateStatus_Db getCurrentStatus() { return currentStatus; }
    public PreparedStatement getLookupIdStatement() { return lookupIdStmt; }
    public PreparedStatement getLookupCompRevStatement() { return lookupCompRevStmt; }
    public PreparedStatement getUpdateCurrStatusStatement() { return updateCurrStatusStmt; }
    public PreparedStatement getAddRowStatement() { return addRowStmt; }
    public PreparedStatement getNextIdStatement() { return nextIdStmt; }


    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setRevision(String aRev) { revision = aRev; }
    private void setBranch(String aBranch) { branch = aBranch; }
    private void setCompVersion(Component_Version_Db aCompVer) { compVersion = aCompVer; }
    private void setCurrentStatus(CodeUpdateStatus_Db aStatus) { 
        currentStatus = aStatus; }

    private void setCompVersion(EdaContext xContext, short compId)
    throws IcofException { 
        if (getCompVersion() == null) {
            compVersion = new Component_Version_Db(compId);
            compVersion.dbLookupById(xContext);
        }
    }
    
    private void setCurrentStatus(EdaContext xContext, long anId)
    throws IcofException { 
        if ((getCurrentStatus() == null) && (anId > 0)) {
            currentStatus = new CodeUpdateStatus_Db(anId);
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
     * Create a PreparedStatement to lookup this object by Component and Revision.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupCompRevStatement(EdaContext xContext) throws IcofException {

        // Return the statement if it already exists.
        if (lookupCompRevStmt != null) {
            return;
        }
        
        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + COMP_VERSION_ID_COL + " = ? " +
                       " AND " + REVISION_COL + " = ? ";
        
        // Otherwise create a statement object and return it.
        lookupCompRevStmt = TkDbUtils.prepStatement(xContext, query);
        
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
                       " values( ?, ?, ?, ?, NULL )";
        
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
        
    }

    /**
     * Look up this object by Component_Version and Revision
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByCompRev(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupCompRevStatement(xContext);
        
        try {
            getLookupCompRevStatement().setLong(1, getCompVersion().getId());
            getLookupCompRevStatement().setString(2, getRevision());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByCompRev()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getLookupCompRevStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! selectSingleRow(xContext, getLookupCompRevStatement())) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByCompRev()",
                                                 IcofException.SEVERE,
                                                 "Unable to find row for query.\n",
                                                 "QUERY: " + 
                                                 getLookupCompRevStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;

        }
        
    }

    
    /**
     * Update the current status for this object.
     * 
     * @param xContext  An application context object.
     * @param newStatus The new status for this object.
     * @throws          Trouble querying the database.
     */
    public void dbUpdateCurrentStatus(EdaContext xContext,
                                      CodeUpdateStatus_Db newStatus)
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
            getAddRowStatement().setLong(2, getCompVersion().getId());
            getAddRowStatement().setString(3, getRevision());
            getAddRowStatement().setString(4, getBranch());
            //getAddRowStatement().setShort(5, getCurrentStatus().getId());

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
        setRevision(rs.getString(REVISION_COL));
        setBranch(rs.getString(BRANCH_COL));
        setCompVersion(xContext, rs.getShort(COMP_VERSION_ID_COL));
        setCurrentStatus(xContext, rs.getLong(CURRENT_STATUS_ID_COL));
        
    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID: " + getId() + "\n");
        if (getCompVersion() != null) {
            buffer.append("Component Version ID: " + getCompVersion().getId() + "\n");
        }
        else {
            buffer.append("Component Version ID: NULL" + "\n");
        }

        buffer.append("Revision: " + getRevision() + "\n");
        buffer.append("Branch: " + getBranch() + "\n");

        if (getCurrentStatus() != null) {
            buffer.append("Status ID: " + getCurrentStatus().getId() + "\n");
            buffer.append("Status: " + getCurrentStatus().toString(xContext) + "\n");
        }
        else {
            buffer.append("Status ID: NULL" + "\n");
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
