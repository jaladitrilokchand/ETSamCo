/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2011 -- IBM Internal Use Only
*
*=============================================================================
*
* FILE: ComponentType_ComponentRelease.java
*
*-PURPOSE---------------------------------------------------------------------
* Component Type X Component Release class
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 09/12/2010 GFS  Initial coding.
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


public class ComponentType_ComponentRelease_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3082767335003628280L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.COMPONENT_TYPE_X_COMPONENT_TKRELEASE";
    public static final String COMPONENT_RELEASE_ID_COL = "COMPONENT_TKRELEASE_ID";
    public static final String COMPONENT_TYPE_ID_COL = "COMPONENT_TYPE_ID";
    public static final String ALL_COLS = COMPONENT_RELEASE_ID_COL + "," + 
                                          COMPONENT_TYPE_ID_COL;

    
    /**
     * Constructor - takes Component_Release and Component_Type ids
     * 
     * @param aCompRelId  A Component_Release id
     * @param aCompTypeId  A Component_Type id
     */
    public ComponentType_ComponentRelease_Db(EdaContext xContext, 
                                             short aCompRelId, 
                                             short aCompTypeId) 
    throws IcofException {
        setComponentRelease(xContext, aCompRelId);
        setComponentType(xContext, aCompTypeId);
    }

    
    /**
     * Constructor - takes Component_Release_Db and Component_Type_Db objects.
     * 
     * @param aCompRel   A ComponentRelease_Db object
     * @param aCompType  A ComponentType_Db object
     */
    public ComponentType_ComponentRelease_Db(Component_Release_Db aCompRel, 
                                          ComponentType_Db aCompType) {
        setComponentRelease(aCompRel);
        setComponentType(aCompType);
    }

    
    /**
     * Data Members
     */
    private Component_Release_Db compRelease;
    private ComponentType_Db compType;

    
    /**
     * Getters
     */
    public Component_Release_Db getCompRelease() { return compRelease; }
    public ComponentType_Db getCompType() { return compType; }
    
    
    /**
     * Setters
     */
    private void setComponentRelease(Component_Release_Db aCompRel) { compRelease = aCompRel; }
    private void setComponentType(ComponentType_Db aCompType) { compType = aCompType; }
    private void setComponentRelease(EdaContext xContext, short anId)  { 
    	compRelease = new Component_Release_Db(anId);
    }
    private void setComponentType(EdaContext xContext, short anId) { 
    	compType = new ComponentType_Db(anId);
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
                       " where " + COMPONENT_RELEASE_ID_COL + " = ? AND " + 
                       COMPONENT_TYPE_ID_COL + " = ? ";
        
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
     * Create a PreparedStatement to lookup by Component_Type
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupComponentReleaseStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "select " + COMPONENT_RELEASE_ID_COL + 
                       " from " + TABLE_NAME +
                       " where " + COMPONENT_TYPE_ID_COL + " = ? "; 
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup by Component_Release.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupComponentTypesStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "select " + COMPONENT_TYPE_ID_COL + 
                       " from " + TABLE_NAME +
                       " where " + COMPONENT_RELEASE_ID_COL + " = ? "; 
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to delete this object
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setDeleteStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "delete from " + TABLE_NAME + 
                       " where " + COMPONENT_RELEASE_ID_COL + " = ? " +
                       "   and " + COMPONENT_TYPE_ID_COL + " = ? ";
        
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
        	getStatement().setLong(1, getCompRelease().getId());
            getStatement().setLong(2, getCompType().getId());
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
            getStatement().setLong(1, getCompRelease().getId());
            getStatement().setLong(2, getCompType().getId());

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
     * Create a list of Component_Release objects for this Component_Type
     * 
     * @param xContext  An application context object.
     * @return          Collection of ComponentReleae_Db objects.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,Component_Release_Db> dbLookupComponentReleases(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupComponentReleaseStatement(xContext);
        
        try {
            getStatement().setLong(1, getCompType().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupComponentReleases()",
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
        Hashtable<String,Component_Release_Db> compReleases = new Hashtable<String,Component_Release_Db>();
        try {
            while (rs.next()) {
                short anId =  rs.getShort(COMPONENT_RELEASE_ID_COL);
                Component_Release_Db cr = new Component_Release_Db(anId);
                cr.dbLookupById(xContext);
                
                compReleases.put(cr.getIdKey(xContext), cr);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupComponentReleases()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return compReleases;
        
    }


    /**
     * Create a list of Component_Type objects for this Component_Release.
     * 
     * @param xContext  An application context object.
     * @return          Collection of ComponentType_Db objects.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,ComponentType_Db>  dbLookupComponentTypes(EdaContext xContext) 
    throws IcofException {
        
        // Create the SQL query in the PreparedStatement.
        setLookupComponentTypesStatement(xContext);
        
        try {
            getStatement().setShort(1, getCompRelease().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass().getName(),
                                                 "dbLookupComponentTypes()",
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
        Hashtable<String,ComponentType_Db> compTypes = new Hashtable<String,ComponentType_Db> ();
        try {
            while (rs.next()) {
                short anId =  rs.getShort(COMPONENT_TYPE_ID_COL);
                ComponentType_Db ct = new ComponentType_Db(anId);
                ct.dbLookupById(xContext);
                
                compTypes.put(ct.getIdKey(xContext), ct);
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
        
        return compTypes;
        
    }

    
    /**
     * Delete this object from the database
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbDeleteRow(EdaContext xContext)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setDeleteStatement(xContext);
        try {
            getStatement().setShort(1, getCompRelease().getId());
            getStatement().setLong(2, getCompType().getId());

        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass().getName(),
                                                 "dbDeleteRow()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! insertRow(xContext)) {
            IcofException ie = new IcofException(this.getClass().getName(),
                                                 "dbDeleteRow()",
                                                 IcofException.SEVERE,
                                                 "Unable to delete row.\n",
                                                 "QUERY: " + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }

        // Close the PreparedStatement.
        closeStatement(xContext);

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
        
        setComponentRelease(xContext, rs.getShort(COMPONENT_RELEASE_ID_COL));
        setComponentType(xContext, rs.getShort(COMPONENT_TYPE_ID_COL));
        setLoadFromDb(true);

    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        if (getCompRelease() != null) {
            buffer.append("Component Release ID: " + getCompRelease().getId() + "\n");
        }
        else {
            buffer.append("Component Release ID: NULL\n");
        }

        if (getCompType() != null) {
            buffer.append("Component Type ID: " + getCompType().getId() + "\n");
        }
        else {
            buffer.append("Component Type ID: NULL\n");
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
        return String.valueOf(getCompRelease().getIdKey(xContext) + "_" +
        		              getCompType().getIdKey(xContext));
    }
    
}

