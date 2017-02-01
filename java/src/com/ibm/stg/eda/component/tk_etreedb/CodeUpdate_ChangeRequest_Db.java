/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* FILE: CodeUpdate_ChangeRequest.java
*
*-PURPOSE---------------------------------------------------------------------
* Code Update X Change Request DB class with audit info
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 03/08/2011 GFS  Initial coding.
* 05/18/2011 GFS  Updated to use base class query and statement members.
* 12/08/2011 GFS  Fixed a bug in dbLookupChangeRequests.
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


public class CodeUpdate_ChangeRequest_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8964217659475434888L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.CODE_UPDATE_X_CHANGEREQUEST";
    public static final String CODE_UPDATE_ID_COL = "CODE_UPDATE_ID";
    public static final String CHANGE_REQUEST_ID_COL = "CHANGEREQUEST_ID";
    public static final String ALL_COLS = CODE_UPDATE_ID_COL + "," + 
                                          CHANGE_REQUEST_ID_COL;

    
    /**
     * Constructor - takes CodeUpdate and ChangeRequest ids
     * 
     * @param aCodeUpdateId     A CodeUpdate database object id
     * @param aChangeRequestId  A ChangeRequest database object id
     */
    public CodeUpdate_ChangeRequest_Db(EdaContext xContext, 
                                       long aCodeUpdateId, long aChangeRequestId) 
    throws IcofException {
        setCodeUpdate(xContext, aCodeUpdateId);
        setChangeRequest(xContext, aChangeRequestId);
    }

    
    /**
     * Constructor - takes CodeUpdate and ChangeRequest objects.
     * 
     * @param aCodeUpdate  A CodeUpdate object
     * @param aChangeRequest  A ChangeRequest object
     */
    public CodeUpdate_ChangeRequest_Db(CodeUpdate_Db anUpdate, 
                                       ChangeRequest_Db aChangeRequest) {
        setCodeUpdate(anUpdate);
        setChangeRequest(aChangeRequest);
    }

    
    /**
     * Data Members
     */
    private CodeUpdate_Db codeUpdate;
    private ChangeRequest_Db changeRequest;

    
    /**
     * Getters
     */
    public CodeUpdate_Db getCodeUpdate() { return codeUpdate; }
    public ChangeRequest_Db getChangeRequest() { return changeRequest; }
    
    
    /**
     * Setters
     */
    private void setCodeUpdate(CodeUpdate_Db anUpdate) { codeUpdate = anUpdate; }
    private void setChangeRequest(ChangeRequest_Db anUpdate) { changeRequest = anUpdate; }
    private void setCodeUpdate(EdaContext xContext, long anId) { 
    	codeUpdate = new CodeUpdate_Db(anId);
    }
    private void setChangeRequest(EdaContext xContext, long anId) { 
    	changeRequest = new ChangeRequest_Db(anId);
    }
    
    
    /**
     * Create a PreparedStatement to lookup this object by ids.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupIdsStatement(EdaContext xContext) throws IcofException {
        
        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + CODE_UPDATE_ID_COL + " = ? AND " + 
                       CHANGE_REQUEST_ID_COL + " = ? ";
        
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
                       " values( ?, ? )";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
       
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

        // Define the query.
        String query = "select " + CODE_UPDATE_ID_COL + 
                       " from " + TABLE_NAME + 
                       " where " + CHANGE_REQUEST_ID_COL + " = ? "; 
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup the ChangeRequest.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupChangeRequestStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "select cufu." + CHANGE_REQUEST_ID_COL + 
                       " from " + TABLE_NAME + " as cufu " +
                       " where cufu." + CODE_UPDATE_ID_COL +
                       " = ? "; 
        
        // Set and prepare the query and st<atement.
        setQuery(xContext, query);
        
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
            getStatement().setLong(1, getCodeUpdate().getId());
            getStatement().setLong(2, getChangeRequest().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByIds()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getQuery().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! selectSingleRow(xContext)) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByIds()",
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
    throws IcofException {

        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        
        try {
            getStatement().setLong(1, getCodeUpdate().getId());
            getStatement().setLong(2, getChangeRequest().getId());

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

    }

    
    /**
     * Create a list of CodeUpdate object for this ChangeRequest.
     * 
     * @param xContext  An application context object.
     * @return          Collection of CodeUpdate_Db objects.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,CodeUpdate_Db> dbLookupCodeUpdates(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupCodeUpdatesStatement(xContext);
        
        try {
            getStatement().setLong(1, getChangeRequest().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupCodeUpdates()",
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
        Hashtable<String, CodeUpdate_Db> codeUpdates = 
        	new Hashtable<String, CodeUpdate_Db>();
        try {
            while (rs.next()) {
                long anId =  rs.getLong(CODE_UPDATE_ID_COL);
                CodeUpdate_Db cu = new CodeUpdate_Db(anId);
                cu.dbLookupById(xContext);
                
                codeUpdates.put(cu.getIdKey(xContext), cu);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), 
                                    "dbLookupCodeUpdates()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return codeUpdates;
        
    }


    /**
     * Create a list of ChangeRequest objects for this CodeUpdate.
     * 
     * @param xContext  An application context object.
     * @return          Collection of ChangeRequest_Db objects.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,ChangeRequest_Db> dbLookupChangeRequests(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupChangeRequestStatement(xContext);
        
        try {
            getStatement().setLong(1, getCodeUpdate().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass().getName(),
                                                 "dbLookupChangeRequests()",
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
        Hashtable<String,ChangeRequest_Db> changeRequests = new Hashtable<String,ChangeRequest_Db>();
        try {
            while (rs.next()) {
                long anId =  rs.getLong(CHANGE_REQUEST_ID_COL);
                ChangeRequest_Db cr = new ChangeRequest_Db(anId);
                cr.dbLookupById(xContext);
                
                changeRequests.put(cr.getIdKey(xContext), cr);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupChangeRequests()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return changeRequests;
        
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
        setChangeRequest(xContext, rs.getShort(CHANGE_REQUEST_ID_COL));
        setLoadFromDb(true);

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

        if (getChangeRequest() != null) {
            buffer.append("ChangeRequests ID: " + getChangeRequest().getId() + "\n");
            buffer.append("ChangeRequests description: " + 
                          getChangeRequest().getDescription() + "\n");
        }
        else {
            buffer.append("ChangeRequests ID: NULL\n");
            buffer.append("ChangeRequests revision: NULL\n");
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
        return String.valueOf(getCodeUpdate().getIdKey(xContext) + "_" +
        		              getChangeRequest().getIdKey(xContext));
    }
    
}

