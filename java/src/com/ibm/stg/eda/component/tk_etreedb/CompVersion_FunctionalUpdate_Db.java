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
* 01/19/2010 GFS  Initial coding.
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


public class CompVersion_FunctionalUpdate_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = 385093340085397744L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.COMPONENT_TKVERSION_X_FUNCTIONAL_UPDATE";
    public static final String FUNCTIONAL_UPDATE_ID_COL = "FUNCTIONAL_UPDATE_ID";
    public static final String COMPONENT_VERSION_COL = "COMPONENT_TKVERSION_ID";
    public static final String ALL_COLS = FUNCTIONAL_UPDATE_ID_COL + "," + 
                                          COMPONENT_VERSION_COL;

    
    /**
     * Constructor - takes FunctionalUpdate and ComponentTkVersion ids
     * 
     * @param anId  A database id
     */
    public CompVersion_FunctionalUpdate_Db(EdaContext xContext, long aFuncUpdateId, 
                                           short aCompVerId) 
    throws IcofException {
        setCompVersion(xContext, aCompVerId);
        setFuncUpdate(xContext, aFuncUpdateId);
    }

    
    /**
     * Constructor - takes FunctionalUpdate and ComponentTkVersion objects.
     * 
     * @param aCompVersion A Component_Version_Db object
     * @param aFuncUpdate  A FunctionalUpdate object
     */
    public CompVersion_FunctionalUpdate_Db(FunctionalUpdate_Db aFuncUpdate,
                                           Component_Version_Db aCompVersion) {
        setCompVersion(aCompVersion);
        setFuncUpdate(aFuncUpdate);
    }

    
    /**
     * Constructor - takes FunctionalUpdate, Component and TkVersion objects.
     * 
     * @param aFuncUpdate  A FunctionalUpdate object
     * @param aComp        A Component_Db object
     * @param aRelVersion  A Release version object
     * @throws IcofException 
     */
    public CompVersion_FunctionalUpdate_Db(EdaContext aContext, 
                                           FunctionalUpdate_Db aFuncUpdate,
                                           Component_Db aComp,
                                           RelVersion_Db aRelVersion) throws IcofException {
	
	setFuncUpdate(aFuncUpdate);
	compVersion = new Component_Version_Db(aContext, aRelVersion, aComp);
	compVersion.dbLookupByAll(aContext);
	
    }
    
    
    /**
     * Data Members
     */
    private Component_Version_Db compVersion;
    private FunctionalUpdate_Db functionalUpdate;
    
    
    /**
     * Getters
     */
    public Component_Version_Db getCompVersion() { return compVersion; }
    public FunctionalUpdate_Db getFuncUpdate() { return functionalUpdate; }

    
    /**
     * Setters
     */
    private void setCompVersion(Component_Version_Db anUpdate) { compVersion = anUpdate; }
    private void setFuncUpdate(FunctionalUpdate_Db anUpdate) { functionalUpdate = anUpdate; }
    private void setCompVersion(EdaContext xContext, short anId) { 
    	compVersion = new Component_Version_Db(anId);
    }
    private void setFuncUpdate(EdaContext xContext, long anId) { 
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
                       " where " + 
                       COMPONENT_VERSION_COL + " = ? AND " + 
                       FUNCTIONAL_UPDATE_ID_COL + " = ? ";
        
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
     * Create a PreparedStatement to lookup the FunctionalUpdates.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupFuncUpdatesStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "select cvfu." + FUNCTIONAL_UPDATE_ID_COL + 
                       " from " + TABLE_NAME + " as cvfu " +
                       " where cvfu." + COMPONENT_VERSION_COL +
                       " = ? "; 
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

  
    /**
     * Create a PreparedStatement to lookup the CompVersions.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupCompVersionsStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "select cvfu." + FUNCTIONAL_UPDATE_ID_COL + 
                       " from " + TABLE_NAME + " as cvfu " +
                       " where cvfu." + FUNCTIONAL_UPDATE_ID_COL +
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
        	getStatement().setLong(1, getCompVersion().getId());
            getStatement().setLong(2, getFuncUpdate().getId());
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
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        
        try {
        	getStatement().setLong(1, getFuncUpdate().getId());
            getStatement().setLong(2, getCompVersion().getId());

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
        setLoadFromDb(true);

    }

    
    /**
     * Create a list of FunctionalUpdate object for this CompVersion.
     * 
     * @param xContext  An application context object.
     * @return          Collection of FunctionalUpdate_Db objects for this code update.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,FunctionalUpdate_Db> dbLookupFuncUpdates(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupFuncUpdatesStatement(xContext);
        
        try {
            getStatement().setLong(1, getCompVersion().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
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
        Hashtable<String,FunctionalUpdate_Db> funcUpdates = new Hashtable<String,FunctionalUpdate_Db>();
        try {
            while (rs.next()) {
                long anId =  rs.getLong(FUNCTIONAL_UPDATE_ID_COL);
                FunctionalUpdate_Db du = new FunctionalUpdate_Db(anId);
                du.dbLookupById(xContext);
                
                funcUpdates.put(du.getIdKey(xContext), du);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), 
            			            "dbLookupFuncUpdates()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return funcUpdates;
        
    }


    /**
     * Create a list of CompVersion object for this FunctionalUpdate.
     * 
     * @param xContext  An application context object.
     * @return          Collection of CodeUpdate_Db objects.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,Component_Version_Db> dbLookupCompVersions(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupCompVersionsStatement(xContext);
        
        try {
            getStatement().setLong(1, getFuncUpdate().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupCompVersions()",
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
        Hashtable<String,Component_Version_Db> compVersions = new Hashtable<String,Component_Version_Db>();
        try {
            while (rs.next()) {
                short anId =  rs.getShort(COMPONENT_VERSION_COL);
                Component_Version_Db cv = new Component_Version_Db(anId);
                cv.dbLookupById(xContext);
                
                compVersions.put(cv.getIdKey(xContext), cv);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupCompVersion()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return compVersions;
        
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
        
        setCompVersion(xContext, rs.getShort(COMPONENT_VERSION_COL));
        setFuncUpdate(xContext, rs.getShort(FUNCTIONAL_UPDATE_ID_COL));
        setLoadFromDb(true);

    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        if (getCompVersion() != null) {
            buffer.append("Component_Version ID: " + getCompVersion().getId() + "\n");
            buffer.append(" Component ID: " + getCompVersion().getCompRelease().getId() + "\n");
            buffer.append(" RelVersion ID: " + getCompVersion().getVersion().getId() + "\n");
        }
        else {
            buffer.append("Component_Version ID: NULL\n");
        }

        if (getFuncUpdate() != null) {
            buffer.append("FunctionalUpdate ID: " + getFuncUpdate().getId() + "\n");
            buffer.append("FunctionalUpdate description: " + 
                          getFuncUpdate().getDescription() + "\n");
        }
        else {
            buffer.append("FunctionalUpdate ID: NULL\n");
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
        return String.valueOf(getCompVersion().getIdKey(xContext) + "_" +
        		              getFuncUpdate().getIdKey(xContext));
    }

    
}
