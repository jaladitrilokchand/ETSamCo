/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* FILE: CodeUpdateStatusName.java
*
*-PURPOSE---------------------------------------------------------------------
* CodeStatusUpdateName DB class with audit info
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/18/2010 GFS  Initial coding.
* 07/22/2010 GFS  Converted to using PreparedStatements.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;


public class FileActionName_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1149899612844835376L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.FILE_ACTION_NAME";
    public static final String ID_COL = "FILE_ACTION_NAME_ID";
    public static final String NAME_COL = "FILE_ACTION_NAME";
    public static final String SVN_NAME_COL = "SVN_ACTION_NAME";
    public static final String ALL_COLS = ID_COL + "," + NAME_COL + "," + SVN_NAME_COL + "," +
                                          CREATED_BY_COL + "," + CREATED_ON_COL + "," + 
                                          UPDATED_BY_COL + "," + UPDATED_ON_COL + "," +
                                          DELETED_BY_COL + "," + DELETED_ON_COL;
    public static final String ACTION_ADD = "ADD";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";

    
    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public FileActionName_Db(short anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes a Code Update Status Name name
     * 
     * @param aName           Name of a CodeUpdateStatus
     */
    public FileActionName_Db(String aName) {
        setName(aName);
    }

    /**
     * Constructor - takes a Name and SVN name
     * 
     * @param aName      Action name
     * @param aSvnName   SVN action name
     */
    public FileActionName_Db(String aName, String svnName) {
        setName(aName);
        setSvnName(svnName);
    }

    
    /**
     * Data Members
     */
    private short id;
    private String name;
    private String svnName;

    
    /**
     * Getters
     */
    public String getName() { return name; }
    public String getSvnName() { return svnName; }
    public short getId() { return id; }


    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    private void setSvnName(String aName) { svnName = aName; }
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
     * 
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
     * Create a PreparedStatement to lookup this object by id.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupSvnNameStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + SVN_NAME_COL + " = ? " +
                       " AND " + DELETED_ON_COL + " is NULL";
        
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
     * Look up this object by name.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupBySvnName(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupSvnNameStatement(xContext);
        
        try {
            getStatement().setString(1, getSvnName());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupBySvnName()",
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
     * Populate this object from the result set.
     * 
     * @param xContext       Application context.
     * @param rs             A valid result set.
     * @throws IcofException 
     * @throws SQLException 
     */
    protected void populate(EdaContext xContext, ResultSet rs) 
    throws SQLException, IcofException  {
        
        super.populate(xContext, rs);
        setId(rs.getShort(ID_COL));
        setName(rs.getString(NAME_COL));
        setSvnName(rs.getString(SVN_NAME_COL));
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
        buffer.append("FileActionName: " + getName() + "\n");
        buffer.append("SvnActionName: " + getSvnName() + "\n");
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
