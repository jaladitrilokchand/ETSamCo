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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.iipmds.common.IcofException;


public class CodeUpdateStatusName_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = 834716383231957921L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.CODE_UPDATE_STATUS_NAME";
    public static final String ID_COL = "CODE_UPDATE_STATUS_NAME_ID";
    public static final String NAME_COL = "CODEUPDATE_STATUS_NAME";
    public static final String ALL_COLS = ID_COL + "," + NAME_COL + "," + 
                                          CREATED_BY_COL + "," + CREATED_ON_COL + "," + 
                                          UPDATED_BY_COL + "," + UPDATED_ON_COL + "," +
                                          DELETED_BY_COL + "," + DELETED_ON_COL;

    // Possible status names
    public static final String STATUS_COMMIT_ON_BRANCH = "COMMITTED_ON_BRANCH";
    public static final String STATUS_COMMIT_ON_TRUNK = "COMMITTED_ON_TRUNK";
    public static final String STATUS_EXTRACTED = "EXTRACTED";
    public static final String STATUS_BUILT = "BUILT";

    public static final String STATUS_PROMOTING_TO_DEV = "PROMOTING_TO_DEV";
    public static final String STATUS_DEV_OK = "DEV_OK";
    public static final String STATUS_DEV_ERROR = "DEV_ERROR";
    
    public static final String STATUS_PROMOTING_TO_PROD = "PROMOTING_TO_PROD";
    public static final String STATUS_PROD_OK = "PROD_OK";
    public static final String STATUS_PROD_ERROR = "PROD_ERROR";

    
    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public CodeUpdateStatusName_Db(short anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes a Code Update Status Name name
     * 
     * @param aName           Name of a CodeUpdateStatus
     */
    public CodeUpdateStatusName_Db(String aName) {
        setName(aName);
    }

    
    /**
     * Data Members
     */
    private short id;
    private String name;
    private transient PreparedStatement lookupIdStmt;
    private transient PreparedStatement lookupNameStmt;
    
    
    /**
     * Getters
     */
    public String getName() { return name; }
    public short getId() { return id; }
    public PreparedStatement getLookupIdStatement() { return lookupIdStmt; }
    public PreparedStatement getLookupNameStatement() { return lookupNameStmt; }


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

        // Return the statement if it already exists.
        if (lookupIdStmt != null) {
            return;
        }
        
        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + ID_COL + " = ? " +
                       " AND " + DELETED_ON_COL + " is NULL";
        
        // Otherwise create a statement object and return it.
        lookupIdStmt = TkDbUtils.prepStatement(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup this object by id.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupNameStatement(EdaContext xContext) throws IcofException {

        // Return the statement if it already exists.
        if (lookupNameStmt != null) {
            return;
        }
        
        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + NAME_COL + " = ? " +
                       " AND " + DELETED_ON_COL + " is NULL";
        
        // Otherwise create a statement object and return it.
        lookupNameStmt = TkDbUtils.prepStatement(xContext, query);
        
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
            getLookupIdStatement().setLong(1, getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupById()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getLookupIdStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! selectSingleRow(xContext, getLookupIdStatement())) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupById()",
                                                 IcofException.SEVERE,
                                                 "Unable to find row for query.\n",
                                                 "QUERY: " + 
                                                 getLookupIdStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;

        }
        
        // Close the PreparedStatement.
        closeStatement(xContext, getLookupIdStatement());
        
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
            getLookupNameStatement().setString(1, getName());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByName()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getLookupNameStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! selectSingleRow(xContext, getLookupNameStatement())) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByName()",
                                                 IcofException.SEVERE,
                                                 "Unable to find row for query.\n",
                                                 "QUERY: " + 
                                                 getLookupNameStatement().toString());
            xContext.getSessionLog().log(ie);
            throw ie;

        }

        // Close the PreparedStatement.
        closeStatement(xContext, getLookupNameStatement());

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
        buffer.append("CodeUpdateStatusName name: " + getName() + "\n");
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

    
}
