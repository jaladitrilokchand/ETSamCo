/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*
*-PURPOSE---------------------------------------------------------------------
* Eda Tk Roles DB class
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 10/21/2010 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;


public class EdaTkRole_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3061353325189139400L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.EDATK_ROLES";
    public static final String ID_COL = "EDATK_ROLES_ID";
    public static final String NAME_COL = "NAME";
    public static final String DESCRIPTION_COL = "DESCRIPTION";
    public static final String ALL_COLS = ID_COL + "," + 
                                          NAME_COL + "," +
                                          DESCRIPTION_COL ;

    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public EdaTkRole_Db(long anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes a name and description
     * 
     * @param aName           Role name
     * @param aDesc           Role description
     */
    public EdaTkRole_Db(String aName, String aDesc) {
        setName(aName);
        setDescription(aDesc);
    }

    
    
    /**
     * Data Members
     */
    private long id;
    private String name;
    private String description;
    
    
    /**
     * Getters
     */
    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }


    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    private void setDescription(String aDesc) { description = aDesc; }
    public void setId(long anId) { id = anId; }
    

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
                       " values( ?, ?, ? )";
        
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
        if (! selectSingleRow(xContext, getStatement())) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupById()",
                                                 IcofException.SEVERE,
                                                 "Unable to find row for query.\n",
                                                 "QUERY: " +  getQuery());
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
        long id = getNextBigIntId(xContext, getStatement());
        closeStatement(xContext);
        
        // Create the SQL query in the PreparedStatement.
    	setAddRowStatement(xContext);
        try {
            getStatement().setLong(1, id);
            getStatement().setString(2, getName());
            getStatement().setString(3, getDescription());

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
                                                 "QUERY: " +  getQuery());
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
        setDescription(rs.getString(DESCRIPTION_COL));
        setLoadFromDb(true);

    }
  

    /**
     * Return the members as a string.
     */
    protected String toString(EdaContext xContext) {

        
        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID: " + getId() + "\n");
        buffer.append("File name: " + getName() + "\n");
        buffer.append("Description: " + getDescription() + "\n");
        
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
