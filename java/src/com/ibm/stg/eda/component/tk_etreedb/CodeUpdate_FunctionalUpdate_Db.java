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
* Code Update X Functional Update DB class with audit info
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;


public class CodeUpdate_FunctionalUpdate_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6253793217156718254L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.CODE_UPDATE_X_FUNCTIONAL_UPDATE";
    public static final String CODE_UPDATE_ID_COL = "CODE_UPDATE_ID";
    public static final String FUNC_UPDATE_ID_COL = "FUNCTIONAL_UPDATE_ID";
    public static final String ALL_COLS = CODE_UPDATE_ID_COL + "," + 
                                          FUNC_UPDATE_ID_COL;

    
    /**
     * Constructor - takes CodeUpdate and FunctionalUpdate ids
     * 
     * @param anId  A database id
     */
    public CodeUpdate_FunctionalUpdate_Db(EdaContext xContext, 
                                          long aCodeUpdateId, long aFuncUpdateId) 
    throws IcofException {
        setCodeUpdate(xContext, aCodeUpdateId);
        setFunctionalUpdate(xContext, aFuncUpdateId);
    }

    
    /**
     * Constructor - takes CodeUpdate and FunctionalUpdate objects.
     * 
     * @param aCodeUpdate  A CodeUpdate object
     * @param aFuncUpdate  A FunctionalUpdate object
     */
    public CodeUpdate_FunctionalUpdate_Db(CodeUpdate_Db anUpdate, 
                                          FunctionalUpdate_Db aFuncUpdate) {
        setCodeUpdate(anUpdate);
        setFunctionalUpdate(aFuncUpdate);
    }

    
    /**
     * Data Members
     */
    private CodeUpdate_Db codeUpdate;
    private FunctionalUpdate_Db functionalUpdate;

    
    /**
     * Getters
     */
    public CodeUpdate_Db getCodeUpdate() { return codeUpdate; }
    public FunctionalUpdate_Db getFunctionalUpdate() { return functionalUpdate; }
    
    
    /**
     * Setters
     */
    private void setCodeUpdate(CodeUpdate_Db anUpdate) { codeUpdate = anUpdate; }
    private void setFunctionalUpdate(FunctionalUpdate_Db anUpdate) { functionalUpdate = anUpdate; }
    private void setCodeUpdate(EdaContext xContext, long anId)  { 
    	codeUpdate = new CodeUpdate_Db(anId);
    }
    private void setFunctionalUpdate(EdaContext xContext, long anId) { 
    	functionalUpdate = new FunctionalUpdate_Db(anId);
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
                       FUNC_UPDATE_ID_COL + " = ? ";
        
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
        String query = "select cufu." + CODE_UPDATE_ID_COL + 
                       " from " + TABLE_NAME + " as cufu " +
                       " where cufu." + FUNC_UPDATE_ID_COL +
                       " = ? "; 
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup the FuncUpdates.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupFuncUpdatesStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "select cufu." + FUNC_UPDATE_ID_COL + 
                       " from " + TABLE_NAME + " as cufu " +
                       " where cufu." + CODE_UPDATE_ID_COL +
                       " = ? "; 
        
        // Set and prepare the query and statement.
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
            getStatement().setLong(2, getFunctionalUpdate().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByIds()",
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
            getStatement().setLong(2, getFunctionalUpdate().getId());

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
     * Create a list of CodeUpdate object for this FunctionalUpdate.
     * 
     * @param xContext  An application context object.
     * @return          Collection of CodeUpdate_Db objects.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,CodeUpdate_Db> dbLookupCodeUpdates(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupCodeUpdatesStatement(xContext);
        
        try {
            getStatement().setLong(1, getFunctionalUpdate().getId());
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
        Hashtable<String,CodeUpdate_Db> codeUpdates = new Hashtable<String,CodeUpdate_Db>();
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
        closeStatement(xContext);
        
        return codeUpdates;
        
    }


    /**
     * Create a list of FunctionalUpdate objects for this CodeUpdate.
     * 
     * @param xContext  An application context object.
     * @return          Collection of FunctionalUpdate_Db objects.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,FunctionalUpdate_Db> dbLookupFuncUpdates(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupFuncUpdatesStatement(xContext);
        
        try {
            getStatement().setLong(1, getCodeUpdate().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass().getName(),
                                                 "dbLookupFuncUpdates()",
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
        Hashtable<String,FunctionalUpdate_Db>  funcUpdates = new Hashtable<String,FunctionalUpdate_Db>();
        try {
            while (rs.next()) {
                long anId =  rs.getLong(FUNC_UPDATE_ID_COL);
                FunctionalUpdate_Db fu = new FunctionalUpdate_Db(anId);
                fu.dbLookupById(xContext);
                
                funcUpdates.put(fu.getIdKey(xContext), fu);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupFuncUpdates()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return funcUpdates;
        
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
        setFunctionalUpdate(xContext, rs.getShort(FUNC_UPDATE_ID_COL));
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

        if (getFunctionalUpdate() != null) {
            buffer.append("FunctionalUpdate ID: " + getFunctionalUpdate().getId() + "\n");
            buffer.append("FunctionalUpdate description: " + 
                          getFunctionalUpdate().getDescription() + "\n");
        }
        else {
            buffer.append("FunctionalUpdate ID: NULL\n");
            buffer.append("FunctionalUpdate revision: NULL\n");
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
        		              getFunctionalUpdate().getIdKey(xContext));
    }
    
}

