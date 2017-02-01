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
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.eda.component.tk_etreeobjs.TkUserRelCompRole;
import com.ibm.stg.iipmds.common.IcofException;

public class CompTkRelRole_User_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6705512832770135863L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.COMPTKREL_USERROLE";
    public static final String ID_COL = "COMPTKREL_USERROLE_ID";
    public static final String USER_ID_COL = "USER_ID";
    public static final String COMPTKREL_ID_COL = "COMPONENT_TKRELEASE_ID";
    public static final String COMPTKREL_ROLES_ID_COL = "COMPTKREL_ROLES_ID";
    public static final String ALL_COLS = ID_COL + "," + USER_ID_COL + "," +
                                          COMPTKREL_ROLES_ID_COL + "," +
                                          COMPTKREL_ID_COL + "," +
                                          CREATED_BY_COL + "," + CREATED_ON_COL + "," + 
                                          UPDATED_BY_COL + "," + UPDATED_ON_COL + "," +
                                          DELETED_BY_COL + "," + DELETED_ON_COL;

    
    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public CompTkRelRole_User_Db(long anId) {
        setId(anId);
    }


    /**
     * Constructor - takes User and CompTkRelRoles objects.
     * 
     * @param aRelComp     A TkRelComponent object
     * @param aUser        A User object
     * @param aRole        A CompTkRelRoles object
     */
    public CompTkRelRole_User_Db(Component_Release_Db aRelComp, User_Db aUser, 
                                 CompTkRelRole_Db aRole) {
        setRelComp(aRelComp);
        setUser(aUser);
        setRole(aRole);
    }


    /**
     * Constructor - takes TK Release and Component objects.
     * 
     * @param anId       An id
     * @param aRelComp   A TkRelComponent object
     * @param aUser      A User object
     * @param aRole      A CompTkRelRoles object
     */
    public CompTkRelRole_User_Db(short anId, Component_Release_Db aRelComp, User_Db aUser,
                              CompTkRelRole_Db aRole) {
        setId(anId);
        setRelComp(aRelComp);
        setUser(aUser);
        setRole(aRole);
    }

    
    
    /**
     * Constructor - takes User and CompTkRelRoles IDs
     * 
     * @param xContext  Application context object.
     * @param relCompId Id of a TkRelComponent database row
     * @param userId    Id of a User database row
     * @param roleId    Id of a CompTkRelRoles database row
     *
     * @throws Trouble looking up the User or CompTkRelRoles objects
     */
    public CompTkRelRole_User_Db(EdaContext xContext, short relCompId, 
                              short userId, long roleId)
    throws IcofException {
        setRelComp(xContext, relCompId);
        setUser(xContext, userId);
        setRole(xContext, roleId);

    }

    
    /**
     * Data Members
     */
    private long id;
    private Component_Release_Db relComp;
    private User_Db user;
    private CompTkRelRole_Db role;

    
    /**
     * Getters
     */
    public long getId() { return id; }
    public Component_Release_Db getRelComp() { return relComp; }
    public User_Db getUser() { return user; }
    public CompTkRelRole_Db getRole() { return role; }


    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setRole(CompTkRelRole_Db aRole) { role = aRole; }
    private void setUser(User_Db aUser) { user = aUser; }
    private void setRelComp(Component_Release_Db aRelComp) { relComp = aRelComp; }
    private void setUser(EdaContext xContext, short anId) {
    	user = new User_Db(anId);
    }
    private void setRole(EdaContext xContext, long anId) {
    	role = new CompTkRelRole_Db(anId);
    }
    private void setRelComp(EdaContext xContext, short anId) {
    	relComp = new Component_Release_Db(anId);
    }
    

    /**
     * Create a PreparedStatement to lookup this object by id.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    private void setLookupIdStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + ID_COL + " = ? " +
                       " AND " + DELETED_ON_COL + " is NULL";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    /**
     * Create a PreparedStatement to lookup this object by All ids (TkRelComponent,
     * User and Role)
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    private void setLookupAllStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + COMPTKREL_ID_COL + " =  ? AND " +
                                   USER_ID_COL + " =  ? AND " +
                                   COMPTKREL_ROLES_ID_COL + " =  ? AND " +
                                   DELETED_ON_COL + " is NULL";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    
    
    /**
     * Create a PreparedStatement to lookup this object by TkRelComponent.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    private void setLookupRelCompStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + COMPTKREL_ID_COL + " =  ? "+
                       " AND " + DELETED_ON_COL + " is NULL" + 
                       " ORDER BY " + COMPTKREL_ROLES_ID_COL + " ASC";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }


    /**
     * Create a PreparedStatement to lookup this object by user.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    private void setLookupUserStatement(EdaContext xContext) throws IcofException {
        
        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + USER_ID_COL + " =  ? "+
                       " AND " + DELETED_ON_COL + " is NULL";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    

//    /**
//     * Create a PreparedStatement to lookup this object by Role.
//     * @param xContext  Application context.
//     * @return PreparedStatement
//     * @throws IcofException 
//     */
//    private void setLookupRoleStatement(EdaContext xContext) throws IcofException {
//
//        // Define the query.
//        String query = "select " + ALL_COLS + 
//                       " from " + TABLE_NAME + 
//                       " where " + COMPTKREL_ROLES_ID_COL + " =  ? "+
//                       " AND " + DELETED_ON_COL + " is NULL";
//        
//        // Set and prepare the query and statement.
//        setQuery(xContext, query);
//        
//    }


    /**
     * Create a PreparedStatement to add a row.
     * 
     * @param  xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    private void setAddRowStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "insert into " + TABLE_NAME + 
                       " ( " +  ALL_COLS + " )" + 
                       " values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup the next id for this table.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    private void setNextIdStatement(EdaContext xContext) throws IcofException {

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
     * Look up by All ids.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByAll(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupAllStatement(xContext);
        
        try {
            getStatement().setShort(1, getRelComp().getId());
            getStatement().setShort(2, getUser().getId());
            getStatement().setLong(3, getRole().getId());
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
     * Look up by RelComponent.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,TkUserRelCompRole>  dbLookupByRelComp(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupRelCompStatement(xContext);

        try {
            getStatement().setShort(1, getRelComp().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByRelComp()",
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
        Hashtable<String,TkUserRelCompRole> userRoles = new Hashtable<String,TkUserRelCompRole> ();
        try {
            while (rs.next()) {
                short userId =  rs.getShort(USER_ID_COL);
                long roleId =  rs.getLong(COMPTKREL_ROLES_ID_COL);
                TkUserRelCompRole userRole = new TkUserRelCompRole(xContext, userId, roleId);
                userRoles.put(userRole.getIdKey(xContext), userRole);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupByRelComp()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return userRoles;
        
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
                       DELETED_ON_COL + " = " + CURRENT_TMS +
                       " where " + ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
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
     * @param creator   User adding this row
     * @throws          Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext, User_Db creator)
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
            getStatement().setLong(4, getRelComp().getId());
            getStatement().setString(5, creator.getIntranetId());
            getStatement().setTimestamp(6, now);
            getStatement().setString(7, creator.getIntranetId());
            getStatement().setTimestamp(8, now);
            getStatement().setString(9, null);
            getStatement().setString(10, null);
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
     * Delete (mark as deleted) this object in the database
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbDeleteRow(EdaContext xContext, User_Db editor)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setDeleteStatement(xContext);
        try {
            getStatement().setString(1, editor.getIntranetId());
            getStatement().setLong(2, getId());

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

        setRelComp(xContext, rs.getShort(COMPTKREL_ID_COL));
        setRole(xContext, rs.getShort(USER_ID_COL));
        setUser(xContext, rs.getShort(COMPTKREL_ROLES_ID_COL));
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
        if (getRelComp() != null) {
            buffer.append("RelComp ID: " + getRelComp().getId() + "\n");
            buffer.append("RelComp name: " + getRelComp().getRelease().getName() + "\n");
        }
        else {
            buffer.append("RelComp ID: NULL\n");
            buffer.append("RelComp name: NULL\n");
        }

        if (getUser() != null) {
            buffer.append("User ID: " + getUser().getId() + "\n");
            buffer.append("User name: " + getUser().getIntranetId() + "\n");
        }
        else {
            buffer.append("User ID: NULL\n");
            buffer.append("User name: NULL\n");
        }
        if (getRole() != null) {
            buffer.append("CompTkRel Role ID: " + getRole().getId() + "\n");
            buffer.append("CompTkRel Role name: " + getRole().getName() + "\n");
        }
        else {
            buffer.append("CompTkRel Role ID: NULL\n");
            buffer.append("CompTkRel Role name: NULL\n");
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
