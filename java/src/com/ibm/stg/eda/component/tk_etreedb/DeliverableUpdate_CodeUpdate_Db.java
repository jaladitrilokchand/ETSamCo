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
* DeliverableUpdate X CodeUpdate DB class with audit info
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/19/2010 GFS  Initial coding.
* 07/22/2010 GFS  Converted to using PreparedStatements.
* 10/28/2010 GFS  Added dbLookupCodeUpdates and dbLookupoDelUpdates.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.iipmds.common.IcofException;


public class DeliverableUpdate_CodeUpdate_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3144850354408606158L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.DELIVERABLE_UPDATE_X_CODE_UPDATE";
    public static final String DELIVERABLE_UPDATE_ID_COL = "DELIVERABLE_UPDATE_ID";
    public static final String CODE_UPDATE_ID_COL = "CODE_UPDATE_ID";
    public static final String ALL_COLS = DELIVERABLE_UPDATE_ID_COL + "," + 
                                          CODE_UPDATE_ID_COL;

    
    /**
     * Constructor - takes DeliverableUpdate and CodeUpdate ids
     * 
     * @param anId  A database id
     */
    public DeliverableUpdate_CodeUpdate_Db(EdaContext xContext, long aDelUpdateId, 
                                       long aCodeUpdateId) 
    throws IcofException {
        setCodeUpdate(xContext, aCodeUpdateId);
        setDeliverableUpdate(xContext, aDelUpdateId);
    }

    
    /**
     * Constructor - takes DeliverableUpdate and CodeUpdateobjects.
     * 
     * @param aCodeUpdate  A CodeUpdate object
     * @param aFuncUpdate  A FunctionalUpdate object
     */
    public DeliverableUpdate_CodeUpdate_Db(DeliverableUpdate_Db aDelUpdate,
                                        CodeUpdate_Db anUpdate) {
        setCodeUpdate(anUpdate);
        setDeliverableUpdate(aDelUpdate);
    }

    
    /**
     * Data Members
     */
    private CodeUpdate_Db codeUpdate;
    private DeliverableUpdate_Db deliverableUpdate;
    private transient PreparedStatement lookupIdsStmt;
    private transient PreparedStatement addRowStmt;
    private transient PreparedStatement lookupDelUpdatesStmt;
    private transient PreparedStatement lookupCodeUpdatesStmt;
    
    
    /**
     * Getters
     */
    public CodeUpdate_Db getCodeUpdate() { return codeUpdate; }
    public DeliverableUpdate_Db getDeliverableUpdate() { return deliverableUpdate; }
    public PreparedStatement getLookupIdsStatement() { return lookupIdsStmt; }
    public PreparedStatement getAddRowStatement() { return addRowStmt; }
    public PreparedStatement getLookupDelUpdatesStatement() { return lookupDelUpdatesStmt; }
    public PreparedStatement getLookupCodeUpdatesStatement() { return lookupCodeUpdatesStmt; }

    
    /**
     * Setters
     */
    private void setCodeUpdate(CodeUpdate_Db anUpdate) { codeUpdate = anUpdate; }
    private void setDeliverableUpdate(DeliverableUpdate_Db anUpdate) { deliverableUpdate = anUpdate; }
    private void setCodeUpdate(EdaContext xContext, long anId) throws IcofException { 
        if (getCodeUpdate() == null) {
            codeUpdate = new CodeUpdate_Db(anId);
            codeUpdate.dbLookupById(xContext);
        }
    }
    private void setDeliverableUpdate(EdaContext xContext, long anId) 
    throws IcofException { 
        if (getDeliverableUpdate() == null) {
            deliverableUpdate = new DeliverableUpdate_Db(anId);
            deliverableUpdate.dbLookupById(xContext);
        }
    }

    
    /**
     * Create a PreparedStatement to lookup this object by ids.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupIdsStatement(EdaContext xContext) throws IcofException {

        // Return the statement if it already exists.
        if (lookupIdsStmt != null) {
            return;
        }
        
        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + CODE_UPDATE_ID_COL + " = ? AND " + 
                       DELIVERABLE_UPDATE_ID_COL + " = ? ";
        
        // Otherwise create a statement object and return it.
        lookupIdsStmt = TkDbUtils.prepStatement(xContext, query);
        
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
                       " values( ?, ? )";
        
        addRowStmt = TkDbUtils.prepStatement(xContext, query);
        
    }


    /**
     * Create a PreparedStatement to lookup the DeliverableUpdates.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupDelUpdatesStatement(EdaContext xContext) 
    throws IcofException {

        // Return the statement if it already exists.
        if (lookupDelUpdatesStmt != null) {
            return;
        }
        
        // Define the query.
        String query = "select ducu." + DELIVERABLE_UPDATE_ID_COL + 
                       " from " + TABLE_NAME + " as ducu " +
                       " where ducu." + CODE_UPDATE_ID_COL +
                       " = ? "; 
        
        // Otherwise create a statement object and return it.
        lookupDelUpdatesStmt = TkDbUtils.prepStatement(xContext, query);
        
    }

  
    /**
     * Create a PreparedStatement to lookup the CodeUpdates.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupCodeUpdatesStatement(EdaContext xContext) 
    throws IcofException {

        // Return the statement if it already exists.
        if (lookupCodeUpdatesStmt != null) {
            return;
        }
        
        // Define the query.
        String query = "select ducu." + DELIVERABLE_UPDATE_ID_COL + 
                       " from " + TABLE_NAME + " as ducu " +
                       " where ducu." + DELIVERABLE_UPDATE_ID_COL +
                       " = ? "; 
        
        // Otherwise create a statement object and return it.
        lookupCodeUpdatesStmt = TkDbUtils.prepStatement(xContext, query);
        
    }

  
    
    /**
     * Look up this object by id.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByIds(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupIdsStatement(xContext);
        
        try {
            getLookupIdsStatement().setLong(1, getCodeUpdate().getId());
            getLookupIdsStatement().setLong(2, getDeliverableUpdate().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByIds()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getLookupIdsStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! selectSingleRow(xContext, getLookupIdsStatement())) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByIds()",
                                                 IcofException.SEVERE,
                                                 "Unable to find row for query.\n",
                                                 "QUERY: " + 
                                                 getLookupIdsStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;

        }
        
        // Close the PreparedStatement.
        closeStatement(xContext, getLookupIdsStatement());

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
        
        try {
            getAddRowStatement().setLong(1, getDeliverableUpdate().getId());
            getAddRowStatement().setLong(2, getCodeUpdate().getId());

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

    }

    
    /**
     * Create a list of DeliverableUpdate object for this CodeUpdate.
     * 
     * @param xContext  An application context object.
     * @return          Collection of FileVersion_Db objects for this code update.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String, DeliverableUpdate_Db> dbLookupDelUpdates(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupDelUpdatesStatement(xContext);
        
        try {
            getLookupDelUpdatesStatement().setLong(1, getCodeUpdate().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupDelUpdates()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getLookupDelUpdatesStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        ResultSet rs = executeQuery(xContext, getLookupDelUpdatesStatement());
        
        // Process the results
        Hashtable<String, DeliverableUpdate_Db>  delUpdates = new Hashtable<String, DeliverableUpdate_Db>();
        try {
            while (rs.next()) {
                long anId =  rs.getLong(DELIVERABLE_UPDATE_ID_COL);
                DeliverableUpdate_Db du = new DeliverableUpdate_Db(anId);
                du.dbLookupById(xContext);
                
                delUpdates.put(du.getIdKey(xContext), du);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupDelUpdates()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext, getLookupDelUpdatesStatement());

        return delUpdates;
        
    }


    /**
     * Create a list of CodeUpdate object for this DeliverableUpdate.
     * 
     * @param xContext  An application context object.
     * @return          Collection of CodeUpdate_Db objects.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String, CodeUpdate_Db>  dbLookupCodeUpdates(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupCodeUpdatesStatement(xContext);
        
        try {
            getLookupCodeUpdatesStatement().setLong(1, getDeliverableUpdate().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupCodeUpdates()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getLookupCodeUpdatesStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        ResultSet rs = executeQuery(xContext, getLookupCodeUpdatesStatement());

        // Process the results
        Hashtable<String, CodeUpdate_Db> codeUpdates = new Hashtable<String, CodeUpdate_Db> ();
        try {
            while (rs.next()) {
                long anId =  rs.getLong(CODE_UPDATE_ID_COL);
                CodeUpdate_Db cu = new CodeUpdate_Db(anId);
                cu.dbLookupById(xContext);
                
                codeUpdates.put(cu.getIdKey(xContext), cu);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupCodeUpdates()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext, getLookupCodeUpdatesStatement());

        return codeUpdates;
        
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
        
        setCodeUpdate(xContext, rs.getShort(CODE_UPDATE_ID_COL));
        setDeliverableUpdate(xContext, rs.getShort(DELIVERABLE_UPDATE_ID_COL));
        
    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        if (getCodeUpdate() != null) {
            buffer.append("CodeUpdate ID: " + getCodeUpdate().getId() + "\n");
            buffer.append("CodeUpdate revision: " + getCodeUpdate().getRevision() + "\n");
        }
        else {
            buffer.append("CodeUpdate ID: NULL\n");
            buffer.append("CodeUpdate revision: NULL\n");
        }

        if (getDeliverableUpdate() != null) {
            buffer.append("DeliverableUpdate ID: " + getDeliverableUpdate().getId() + "\n");
            buffer.append("DeliverableUpdate description: " + 
                          getDeliverableUpdate().getPlatform() + "\n");
        }
        else {
            buffer.append("DeliverableUpdate ID: NULL\n");
            buffer.append("DeliverableUpdate revision: NULL\n");
        }
        
        return buffer.toString();
        
    }
    

    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
        return String.valueOf(getDeliverableUpdate().getIdKey(xContext) + "_" +
        		              getCodeUpdate().getIdKey(xContext));
    }

    
}

