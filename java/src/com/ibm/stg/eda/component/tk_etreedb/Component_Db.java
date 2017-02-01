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
* Component DB class with audit info
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/18/2010 GFS  Initial coding.
* 07/23/2010 GFS  Converted to using PreparedStatements.
* 08/11/2010 GFS  Added dbAddRow() method.
* 06/09/2011 GFS  Added new constructor.
* 04/11/2011 GFS  Added dbLookupByTkStage() method.
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
import com.ibm.stg.iipmds.common.IcofException;

public class Component_Db extends TkAudit implements Comparable<Component_Db> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8862866127946666698L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.COMPONENT";
    public static final String ID_COL = "COMPONENT_ID";
    public static final String NAME_COL = "COMPONENT_NAME";
    public static final String ALL_COLS = ID_COL + "," + NAME_COL + "," + 
                                          CREATED_BY_COL + "," + CREATED_ON_COL + "," + 
                                          UPDATED_BY_COL + "," + UPDATED_ON_COL + "," +
                                          DELETED_BY_COL + "," + DELETED_ON_COL;

    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public Component_Db(short anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes a Component name
     * 
     * @param aName  Name of a TK Component (ess, edautils, einstimer ...)
     */
    public Component_Db(String aName) {
        setName(aName);
    }

    
    /**
     * Constructor - takes an id and name
     * 
     * @param anId   A database id
     * @param aName  Name of a TK Component (ess, edautils, einstimer ...)
     */
    public Component_Db(short anId, String aName) {
    	setId(anId);
        setName(aName);
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
    private void setId(short anId) { id = anId; }
    
    
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
                       " where " + ID_COL + " = ? " +
                       " AND " + DELETED_ON_COL + " is NULL";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup this object by id.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupNameStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + NAME_COL + " = ? " +
                       " AND " + DELETED_ON_COL + " is NULL";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    
  
    /**
     * Create a PreparedStatement to lookup this object by TK and Stage.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupByTkStageStatement(EdaContext xContext)
    throws IcofException {

//    	select c.component_id, c.component_name
//    	from tk.Component as c,
//    	     tk.Component_TkVersion as cv,
//    	     tk.Component_TkRelease as cr
//    	where cv.StageName_id = 3
//    	  and cv.TkVersion_id = 2
//    	  and cv.Component_TkRelease_id = cr.Component_TkRelease_id
//    	  and cr.Component_id = c.Component_id
//    	order by c.component_name asc

    	
        // Define the query.
        String query = "select c." + ID_COL + ", c." + NAME_COL +  
                       " from " + TABLE_NAME + " as c, " +
                       Component_Version_Db.TABLE_NAME + " as cv, " +
                       Component_Release_Db.TABLE_NAME + " as cr " +
                       " where cv." + Component_Version_Db.STAGE_NAME_ID_COL + 
                       " = ? " +
                       " and cv." + Component_Version_Db.VERSION_ID_COL + 
                       " = ? " +
                       " and cv." + Component_Version_Db.REL_COMP_ID_COL + 
                       " = cr." + Component_Release_Db.ID_COL + 
                       " and cr." + Component_Release_Db.COMP_ID_COL + 
                       " = c." + ID_COL +
                       " order by c." + NAME_COL + " asc ";
        
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
     * Create a PreparedStatement to delete this object
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setDeleteStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "update " + TABLE_NAME + 
                       " set " + 
                       DELETED_BY_COL + " = ? , " +
                       DELETED_ON_COL + " = ? " +
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
                       " set " +  NAME_COL + " = ? , " + 
                                  UPDATED_BY_COL + " = ? , " +
                                  UPDATED_ON_COL + " = ? " +
                       " where " + ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    


    /**
     * Look up the Component by id.
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
     * Look up the Component by name.
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
     * Create a list of Component_Db objects for the given ToolKit
     * and Stage Name
     * 
     * @param xContext   An application context object.
     * @param xTk        A RelVersion_Db object
     * @param xStage     A StageName_Db object
     * @return           Collection of Component_Db objects.
     * @throws           Trouble querying the database.
     */
    public Vector<Component_Db> dbLookupByTkStage(EdaContext xContext, 
                                        RelVersion_Db xTk,
                                        StageName_Db xStage)
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupByTkStageStatement(xContext);
        
        try {
        	getStatement().setShort(1, xStage.getId());
        	getStatement().setShort(2, xTk.getId());
         }
        catch(SQLException trap) {
            IcofException ie = 
            	new IcofException(this.getClass().getName(),
            	                  "dbLookupByTkStage()",
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
        Vector<Component_Db> components = new Vector<Component_Db>();
        try {
            while (rs.next()) {
                short anId =  rs.getShort(Component_Db.ID_COL);
                String aName =  rs.getString(Component_Db.NAME_COL);
                Component_Db comp = new Component_Db(anId, aName);
                components.add(comp);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), 
                                    "dbLookupByTkStage()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);

        return components;
        
    }
    

    /**
     * Insert a new row.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext, User_Db user)
    throws IcofException{

        // Create the next id for this new row
        setNextIdStatement(xContext);
        setId(getNextSmallIntId(xContext, getStatement()));
        closeStatement(xContext);
        
        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        Timestamp now = new Timestamp(new java.util.Date().getTime());
        try {
            getStatement().setLong(1, getId());
            getStatement().setString(2, getName());
            getStatement().setString(3, user.getIntranetId());
            getStatement().setTimestamp(4, now);
            getStatement().setString(5, user.getIntranetId());
            getStatement().setTimestamp(6, now);
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
    	Timestamp now = new Timestamp(new java.util.Date().getTime());
        setUpdateStatement(xContext);
        try {
            getStatement().setString(1, aName);
            getStatement().setString(2, editor.getIntranetId());
            getStatement().setTimestamp(3, now);
            getStatement().setLong(4, getId());

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
        setUpdatedBy(editor.getIntranetId());
        setUpdatedOn(now);
        setLoadFromDb(true);

        
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
            getStatement().setString(1, editor.getIntranetId());
            getStatement().setTimestamp(2, now);
            getStatement().setLong(3, getId());

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
     * @param xContext  Application context.
     * @param rs        A valid result set.
     * @throws          Trouble retrieving the data.
     */
    protected void populate(EdaContext xContext, ResultSet rs) 
    throws SQLException, IcofException {

        super.populate(xContext, rs);
        
        setId(rs.getShort(ID_COL));
        setName(rs.getString(NAME_COL));
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
        buffer.append("Component name: " + getName() + "\n");
        buffer.append(audit);
        
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


    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Component_Db that) {

	return getName().compareToIgnoreCase(that.getName());
	
    }
        
    
}
