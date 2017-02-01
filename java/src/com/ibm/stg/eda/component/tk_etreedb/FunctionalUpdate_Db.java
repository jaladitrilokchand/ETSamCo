/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * FILE: FunctionalUpdate.java
 *
 *-PURPOSE---------------------------------------------------------------------
 * Functional Update DB class with audit info
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
import java.sql.Timestamp;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.eda.component.tk_etreeobjs.TkFuncUpdate;
import com.ibm.stg.iipmds.common.IcofException;
	
	public class FunctionalUpdate_Db extends TkAudit {
	
	    /**
		 * 
		 */
		private static final long serialVersionUID = -2620659994734869668L;
		/**
	     * Constants.
	     */
	    public static final String TABLE_NAME = "TK.FUNCTIONAL_UPDATE";
	    public static final String ID_COL = "FUNCTIONAL_UPDATE_ID";
	    public static final String DESC_COL = "DESCRIPTION";
	    public static final String ALL_COLS = ID_COL + "," + 
	                                          DESC_COL + "," +
	                                          CREATED_BY_COL + "," + CREATED_ON_COL + "," + 
	                                          UPDATED_BY_COL + "," + UPDATED_ON_COL + "," +
	                                          DELETED_BY_COL + "," + DELETED_ON_COL;
	
	    
	    /**
	     * Constructor - takes a DB id
	     * 
	     * @param anId  A database id
	     */
	    public FunctionalUpdate_Db(long anId) {
	        setId(anId);
	    }
	
	    
	    /**
	     * Constructor - takes a TKRelease object and a TK Version name
	     * 
	     * @param aDescription Description of functional update
	     */
	    public FunctionalUpdate_Db(String aDescription) {
	        setDescription(aDescription);
	    }
	
	    
	    /**
	     * Data Members
	     */
	    private long id;
	    private String description;
	    
	    
	    /**
	     * Getters
	     */
	    public long getId() { return id; }
	    public String getDescription() { return description; }
	
	
	    /**
	     * Setters
	     */
	    private void setId(long anId) { id = anId; }
	    private void setDescription(String aDesc) { description = aDesc; }
	   
	    
	    /**
	     * Create a PreparedStatement to lookup this object by id.
	
	     * @param xContext  Application context.
	     * @return PreparedStatement
	     * @throws IcofException 
	     */
	    public void setLookupIdStatement(EdaContext xContext) throws IcofException {
	
	        // Define the query.
	        String query = "select " + ALL_COLS + 
	                       " from " + TABLE_NAME + 
	                       " where " + ID_COL + " =  ? " +
	                       " AND " + DELETED_ON_COL + " is NULL";
	        
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
	                       " ( " + ALL_COLS + " )" + 
	                       " values( ?, ?, ?, ?, ?, ?, ?, ? )";
	        
	        // Set and prepare the query and statement.
	        setQuery(xContext, query);
	        
	    }
	
	    
	    /**
	     * Create a PreparedStatement to lookup the next id for this table.
	
	     * @param xContext  Application context.
	     * @return PreparedStatement
	     * @throws IcofException 
	     */
	    public void setNextIdStatement(EdaContext xContext) throws IcofException {
	
	        // Define the query.
	        String query =  TkAudit.getNextIdQuery(xContext, TABLE_NAME, ID_COL);
	        
	        // Set and prepare the query and statement.
	        setQuery(xContext, query);
	        
	    }
	
	    
	    /**
	     * Create a PreparedStatement to lookup FunctionalUpdates by TkRelease, 
	     * component and optionally revision.
	     *
	     * @param xContext     Application context.
	     * @param compVersion  Component version
	     * @param revision     A revision number 
	     * @throws IcofException 
	     */
	    public void setLookupFuncUpdatesStatement(EdaContext xContext, 
	                                              Component_Version_Db compVersion,
	                                              String revision) 
	    throws IcofException {

	        // Define the query.
	        String revFilter = "";
	        if (revision != null) {
	            revFilter = " and cu.svnrevision = ? ";
	        }
	        String query = "select fu.functional_update_id " +
	                       "from tk.functional_update as fu, " + 
	                       " tk.code_update as cu, " + 
	                       " tk.code_update_x_functional_update as cufu " + 
	                       "where cu.component_tkversion_id = ? " + 
	                       " and cufu.code_update_id = cu.code_update_id " + 
	                       " and cufu.functional_update_id = fu.functional_update_id " +
	                       revFilter +
	                       " order by cu.svnrevision asc"; 
	        
			 // Set and prepare the query and statement.
			 setQuery(xContext, query);
	        
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
	        	getStatement().setLong(1, getId());
	        }
	        catch(SQLException trap) {
	            IcofException ie = new IcofException(this.getClass() .getName(),
	                                                 "dbLookupById()",
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
	                                                 "dbLookupById()",
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
	    public void dbAddRow(EdaContext xContext, String createdBy, 
	                         Timestamp createdOn)
	    throws IcofException{
	
	    	// Get the next id for this new row.
	    	setNextIdStatement(xContext);
	    	long id = getNextBigIntId(xContext);
	    	closeStatement(xContext);
	    	
	        // Create the SQL query in the PreparedStatement.
	        setAddRowStatement(xContext);
	        try {
	            getStatement().setLong(1, id);
	            getStatement().setString(2, getDescription());
	            getStatement().setString(3, createdBy);
	            getStatement().setTimestamp(4, createdOn);
	            getStatement().setString(5, createdBy);
	            getStatement().setTimestamp(6, createdOn);
	            getStatement().setString(7, null);
	            getStatement().setString(8, null);
	        
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
	
	        // Load the data for the new row.
	        setId(id);
	        dbLookupById(xContext); 
	        
	    }
	
	
		/**
	     * Lookup the Functional Updates for this component and revision.
	     * 
	     * @param  xContext  Application context
	     * @throws IcofException 
	     */
	    public Vector<TkFuncUpdate> dbLookupFuncUpdates(EdaContext xContext, 
	                                       Component_Version_Db compVersion,
	                                       String revision)
	    throws IcofException {

	        // Create the SQL query in the PreparedStatement.
	        setLookupFuncUpdatesStatement(xContext, compVersion, revision);

	        try {
	            getStatement().setLong(1, compVersion.getId());
	            if (revision != null) {
	                getStatement().setString(2, revision);
	            }
	        }
	        catch(SQLException trap) {
	            IcofException ie = new IcofException(this.getClass() .getName(),
	                                                 "dbLookupFunctUpdates()",
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
	        Vector<TkFuncUpdate> funcUpdates = new Vector<TkFuncUpdate>();
	        try {
	            while (rs.next()) {
	                short funcUpdateId = rs.getShort(1);
	                TkFuncUpdate func = new TkFuncUpdate(xContext, funcUpdateId);
	                funcUpdates.add(func);
	            }
	            
	            // Close the PreparedStatement.
	            closeStatement(xContext);
	            
	        }
	        catch(SQLException ex) {
	            throw new IcofException(this.getClass().getName(), "dbLookupFuncUpdates()",
	                                    IcofException.SEVERE, 
	                                    "Error reading DB query results.",
	                                    ex.getMessage());
	        }

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
	    
	        super.populate(xContext, rs);
	        setId(rs.getInt(ID_COL));
	        setDescription(rs.getString(DESC_COL));
	        setLoadFromDb(true);

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
	        buffer.append("Description: " + getDescription() + "\n");
	        buffer.append(audit);
	        
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
