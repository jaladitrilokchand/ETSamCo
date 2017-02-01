/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* FILE: ConponentType_Db.java
*
*-PURPOSE---------------------------------------------------------------------
* Component Type DB class
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 09/12/2011 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;

public class ComponentType_Db extends TkAudit {

 	/**
	 * 
	 */
	private static final long serialVersionUID = 7387614688012783427L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.COMPONENT_TYPE";
    public static final String ID_COL = "COMPONENT_TYPE_ID";
    public static final String NAME_COL = "COMPONENT_TYPE_NAME";
    public static final String DESC_COL = "COMPONENT_TYPE_DESC";
    public static final String ALL_COLS = ID_COL + ", " + NAME_COL + ", " +
                                          DESC_COL;

    // Possible type names
    public static final String TYPE_DELIVERABLE = "DELIVERABLE";
    public static final String TYPE_32BIT = "32-BIT";
    public static final String TYPE_BUILD_SUPPORT = "BUILD_SUPPORT";

    
    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public ComponentType_Db(long anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes a Component TYpe name
     * 
     * @param aName  Type name
     */
    public ComponentType_Db(String aName) {
        setName(aName);
    }


    /**
     * Constructor - takes a type name and description
     * 
     * @param aName  Type name
     * @param aDesc  Type description
     */
    public ComponentType_Db(String aName, String aDesc) {
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
    public String getName() { return name; }
    public String getDescription() { return description; }
    public long getId() { return id; }


    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    private void setDescription(String aDesc) { description = aDesc; }
    private void setId(long anId) { id = anId; }
    
    
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
                       " set " +  NAME_COL + " = ?,  " + 
                                  DESC_COL + " = ? " +
                       " where " + ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to look up all rows.
     * 
     * @param  xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupAllStatement(EdaContext xContext) throws IcofException {
        
        // Define the query.
        String query = "select " +  ALL_COLS + 
                       " from " + TABLE_NAME;
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Insert a new row.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext)
    throws IcofException{

    	// Lookup the next id
    	setNextIdStatement(xContext);
    	setId(getNextSmallIntId(xContext, getStatement()));
    	closeStatement(xContext);
    	
        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        
        try {
            getStatement().setLong(1, getId());
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
                                                 "QUERY: " + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }


        // Close the PreparedStatement.
        closeStatement(xContext);
        
        // Load the data for the new row.
        dbLookupById(xContext); 
        
    }


    /**
     * Update the name for this object.
     * 
     * @param xContext  An application context object.
     * @param aName     New component type name
     * @param aName     New component type description
     * @throws          Trouble querying the database.
     */
    public void dbUpdateRow(EdaContext xContext, String aName, String aDesc)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setUpdateStatement(xContext);
        try {
            getStatement().setString(1, aName);
            getStatement().setString(2, aDesc);
            getStatement().setLong(3, getId());
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
    public void dbDeleteRow(EdaContext xContext)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
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
	  * Create a list of all Component Type objects.
	  * 
	  * @param xContext  An application context object.
	  * @return          Collection of ComponentType_Db objects.
	  * @throws          Trouble querying the database.
	  */
	 public Vector<ComponentType_Db> dbLookupAll(EdaContext xContext)
	 throws IcofException{

		 // Create the SQL query in the PreparedStatement.
		 setLookupAllStatement(xContext);
		 
		 // Run the query.
		 ResultSet rs = executeQuery(xContext);

		 // Process the results
		 Vector<ComponentType_Db> compTypes = new Vector<ComponentType_Db>();
		 try {
			 while (rs.next()) {
				 String aName =  rs.getString(NAME_COL);
				 String aDesc =  rs.getString(DESC_COL);
				 ComponentType_Db compType = new ComponentType_Db(aName, aDesc);
				 compTypes.add(compType);
			 }

		 }
		 catch(SQLException ex) {
			 throw new IcofException(this.getClass().getName(), 
			                         "dbLookupAll()",
					                 IcofException.SEVERE, 
					                 "Error reading DB query results.",
					                 ex.getMessage());
		 }

		 // Close the PreparedStatement.
		 closeStatement(xContext);

		 return compTypes;

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
        setDescription(rs.getString(DESC_COL));
        setLoadFromDb(true);

    }
  

    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID: " + getId() + "\n");
        buffer.append("Component type: " + getName() + "\n");
        buffer.append("Component type descripotion: " + getDescription() + "\n");
        
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
