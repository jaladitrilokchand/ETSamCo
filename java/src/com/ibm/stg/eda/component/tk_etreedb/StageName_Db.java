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
* FileName DB class with audit info
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 01/06/2011 GFS  Initial coding.
* 03/23/2011 GFS  Added STATE_* constants.
* 04/18/2011 GFS  Added dbDeleteRow and dbUpdateRow.
* 08/17/2011 GFS  Added STATE_READY constant.
* 11/17/2011 GFS  Updated STAGE_* constants.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;


public class StageName_Db extends TkAudit {

    /**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.STAGENAME";
    public static final String ID_COL = "STAGENAME_ID";
    public static final String NAME_COL = "STAGE_NAME";
    public static final String ALL_COLS = ID_COL + "," + NAME_COL;
    
    public static final String STAGE_READY = "READY";
    public static final String STAGE_DEV = "DEVELOPMENT";
    public static final String STAGE_PREVIEW = "PREVIEW";
    public static final String STAGE_PRODUCTION = "PRODUCTION";
    public static final String STAGE_XTINCT = "XTINCT";
    public static final String STAGE_OBSOLETE = "OBSOLETE";
    private static final long serialVersionUID = 6438272997509242780L;

    
    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public StageName_Db(short anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes a StageName name
     * 
     * @param aName           Name of the object
     */
    public StageName_Db(String aName) {
        setName(aName.toUpperCase());
    }

    
    /**
     * Data Members
     */
    private short id;
    private String name;

    
    /**
     * Getters
     */
    public String getName() { return name; }
    public short getId() { return id; }


    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    public void setId(short anId) { id = anId; }
    

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
                       " where " + ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup this object by id.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupNameStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + NAME_COL + " = ? ";
        
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
     * Create a PreparedStatement to delete this object
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setDeleteStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "delete from " + TABLE_NAME + 
                       " where " + ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    
    
    /**
     * Create a PreparedStatement to update this object
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setUpdateStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "update " + TABLE_NAME + 
                       " set " +  NAME_COL + " = ? " + 
                       " where " + ID_COL + " = ? ";
        
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
     * Look up this object by id.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupById(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupIdStatement(xContext);
        
        try {
            getStatement().setShort(1, getId());
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
     * Look up this object by name.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByName(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupNameStatement(xContext);
        
        try {
            getStatement().setString(1, getName());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByName()",
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
                                                 "dbLookupByName()",
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

    	// Get the next id for this new row.
    	setNextIdStatement(xContext);
    	short id = getNextSmallIntId(xContext);
    	closeStatement(xContext);
    	
        // Create the SQL query in the PreparedStatement.
    	setAddRowStatement(xContext);
        try {
            getStatement().setLong(1, id);
            getStatement().setString(2, getName());

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

        //  Load the data for the new row.
        setId(id);
        dbLookupById(xContext); 
        
    }

    
    /**
     * Update the name for this object.
     * 
     * @param xContext  An application context object.
     * @param aName     New component name
     * @throws          Trouble querying the database.
     */
    public void dbUpdateRow(EdaContext xContext, String aName, User_Db editor)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setUpdateStatement(xContext);
        try {
            getStatement().setString(1, aName);
            getStatement().setLong(2, getId());

        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass().getName(),
                                                 "dbUpdateRow()",
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
                                                 "dbUpdateRow()",
                                                 IcofException.SEVERE,
                                                 "Unable to insert new row.\n",
                                                 "QUERY: " + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }

        // Close the PreparedStatement.
        closeStatement(xContext);

        // Set the names on this object.
        setName(aName);
        
    }
    
    
    /**
     * Delete (mark as deleted) this object in the database
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbDeleteRow(EdaContext xContext, User_Db editor)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
    	Timestamp now = new Timestamp(new java.util.Date().getTime());
        setDeleteStatement(xContext);
        try {
            getStatement().setLong(1, getId());

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

        // Set the delete info on this object.
        setDeletedBy(editor.getIntranetId());
        setDeletedOn(now);
        
    }


    /**
     * Populate this object from the result set.
     * 
     * @param xContext       Application context.
     * @param rs             A valid result set.
     * @throws IcofException 
     * @throws SQLException 
     */
    protected void populate(EdaContext xContext, ResultSet rs) 
    throws SQLException, IcofException  {
        
        setId(rs.getShort(ID_COL));
        setName(rs.getString(NAME_COL));
        setLoadFromDb(true);

    }
  

    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {
        
        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID: " + getId() + "\n");
        buffer.append("Name: " + getName() + "\n");
        
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
