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
* EdaTkRoles to User DB class with audit info
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 10/21/2010 GFS  Initial coding.
* 02/15/2012 GFS  Added dbLookupByAll() method.
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

public class EdaTkRole_User_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4327927959004583661L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.EDATK_USERROLE";
    public static final String ID_COL = "EDATK_USERROLE_ID";
    public static final String USER_ID_COL = "USER_ID";
    public static final String EDATK_ROLES_ID_COL = "EDATK_ROLES_ID";
    public static final String ALL_COLS = ID_COL + "," + 
                                          USER_ID_COL + "," + EDATK_ROLES_ID_COL + "," +
                                          CREATED_BY_COL + "," + CREATED_ON_COL + "," + 
                                          UPDATED_BY_COL + "," + UPDATED_ON_COL + "," +
                                          DELETED_BY_COL + "," + DELETED_ON_COL;

    
    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public EdaTkRole_User_Db(long anId) {
        setId(anId);
    }


    /**
     * Constructor - takes User and EdaTkRoles objects.
     * 
     * @param aUser        A User object
     * @param aRole        A EdaTkRoles object
     */
    public EdaTkRole_User_Db(User_Db aUser, EdaTkRole_Db aRole) {
        setUser(aUser);
        setRole(aRole);
    }


    /**
     * Constructor - takes TK Release and Component objects.
     * 
     * @param anId       An id
     * @param aUser      A User object
     * @param aRole      A EdaTkRoles object
     */
    public EdaTkRole_User_Db(short anId, User_Db aUser, EdaTkRole_Db aRole) {
        setId(anId);
        setUser(aUser);
        setRole(aRole);
    }

    
    
    /**
     * Constructor - takes User and EdaTkRoles IDs
     * 
     * @param xContext  Application context object.
     * @param userId    Id of a User database row
     * @param roleId    Id of a EdaTkRoles database row
     *
     * @throws Trouble looking up the User or EdaTkRoles objects
     */
    public EdaTkRole_User_Db(EdaContext xContext, short userId, long roleId)
    throws IcofException {
        setUser(xContext, userId);
        setRole(xContext, roleId);

    }

    
    /**
     * Data Members
     */
    private long id;
    private User_Db user;
    private EdaTkRole_Db role;

    
    /**
     * Getters
     */
    public long getId() { return id; }
    public User_Db getUser() { return user; }
    public EdaTkRole_Db getRole() { return role; }


    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setRole(EdaTkRole_Db aRole) { role = aRole; }
    private void setUser(User_Db aUser) { user = aUser; }
    private void setUser(EdaContext xContext, short anId) {
    	user = new User_Db(anId);
    }
    private void setRole(EdaContext xContext, long anId) throws IcofException {
    	role = new EdaTkRole_Db(anId);
    }
    

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
     * Create a PreparedStatement to lookup this object by user/role.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupAllStatement(EdaContext xContext) throws IcofException {
        
        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + USER_ID_COL + " =  ? " +
                       " AND " + EDATK_ROLES_ID_COL + " = ? " +
                       " AND " + DELETED_ON_COL + " is NULL";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup this object by user.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupUserStatement(EdaContext xContext) throws IcofException {
        
        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + USER_ID_COL + " =  ? "+
                       " AND " + DELETED_ON_COL + " is NULL";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    

    /**
     * Create a PreparedStatement to lookup this object by Role.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupRoleStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + EDATK_ROLES_ID_COL + " =  ? "+
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
                       " ( " +  ALL_COLS + " )" + 
                       " values( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        
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
     * Look up the Component by id.
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
     * Look up by User and Role.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByAll(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupAllStatement(xContext);
        
        try {
            getStatement().setShort(1, getUser().getId());
            getStatement().setLong(2, getRole().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByAll()",
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
                                                 "dbLookupByAll()",
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
     * Look up by User.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByUser(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupUserStatement(xContext);
        
        try {
            getStatement().setShort(1, getUser().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByUser()",
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
                                                 "dbLookupByUser()",
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
     * Look up by Role.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByRole(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupUserStatement(xContext);
        
        try {
            getStatement().setLong(1, getRole().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByRole()",
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
                                                 "dbLookupByRole()",
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
    public void dbAddRow(EdaContext xContext, User_Db user)
    throws IcofException{

    	// Get the next id for this new row.
    	setNextIdStatement(xContext);
    	setId(getNextSmallIntId(xContext));
    	closeStatement(xContext);
    	
        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        Timestamp now = new Timestamp(new java.util.Date().getTime());
        try {
        	getStatement().setLong(1, getId());
            getStatement().setShort(2, getUser().getId());
            getStatement().setLong(3, getRole().getId());
            getStatement().setString(4, user.getIntranetId());
            getStatement().setTimestamp(5, now);
            getStatement().setString(6, user.getIntranetId());
            getStatement().setTimestamp(7, now);
            getStatement().setString(8, null);
            getStatement().setString(9, null);
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
     * Populate this object from the result set.
     * 
     * @param xContext  Application context.
     * @param rs        A valid result set.
     * @throws IcofException 
     * @throws SQLException
     */
    protected void populate(EdaContext xContext, ResultSet rs) 
    throws SQLException, IcofException {
        
        super.populate(xContext, rs);
        setId(rs.getShort(ID_COL));

        setRole(xContext, rs.getShort(USER_ID_COL));
        setUser(xContext, rs.getShort(EDATK_ROLES_ID_COL));
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
        if (getUser() != null) {
            buffer.append("User ID: " + getUser().getId() + "\n");
            buffer.append("User name: " + getUser().getIntranetId() + "\n");
        }
        else {
            buffer.append("User ID: NULL\n");
            buffer.append("User name: NULL\n");
        }
        if (getRole() != null) {
            buffer.append("EdaTk Role ID: " + getRole().getId() + "\n");
            buffer.append("EdaTk Role name: " + getRole().getName() + "\n");
        }
        else {
            buffer.append("EdaTk Role ID: NULL\n");
            buffer.append("EdaTk Role name: NULL\n");
        }
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
