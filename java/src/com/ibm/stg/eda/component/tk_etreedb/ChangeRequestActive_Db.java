/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2011 -- IBM Internal Use Only
*
*=============================================================================
*
* FILE: ChangeRequestActive_Db.java
*
*-PURPOSE---------------------------------------------------------------------
* Active ChangeRequest DB class
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 04/11/2011 GFS  Initial coding.
* 05/18/2011 GFS  Updated to use base class query and statement members.
* 08/06/2012 GFS  Added dbDeleteRowByCr() method.
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

public class ChangeRequestActive_Db extends TkAudit {

 	private static final long serialVersionUID = 4130271866385841117L;
	
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.ACTIVECHANGEREQUEST";
    public static final String USER_ID_COL = "USER_ID";
    public static final String COMPONENT_VERSION_ID_COL = "COMPONENT_TKVERSION_ID";
    public static final String CHANGE_REQUEST_ID_COL = "CHANGEREQUEST_ID";
    public static final String ALL_COLS = USER_ID_COL + ", " +
                                          COMPONENT_VERSION_ID_COL + ", " + 
                                          CHANGE_REQUEST_ID_COL + ", " +
                                          CREATED_BY_COL + ", " +
                                          CREATED_ON_COL;

    
    /**
     * Constructor - takes User, Component_TkVersion and ChangeRequest ids
     *  @param xContext   Application context
     *  @param aUserId    User object id
     *  @param aComVerId  Component_TkVersion object id
     *  @param aChangeRequestId  ChangeRequest object id 
     */
    public ChangeRequestActive_Db(EdaContext xContext, short aUserId, long aCompVerId, 
                                       long aChangeRequestId) 
    throws IcofException {
    	setUser(xContext, aUserId);
        setCompVersion(xContext, aCompVerId);
        setChangeRequest(xContext, aChangeRequestId);
    }

    
    /**
     * Constructor - takes Component_Version_Location and ComponentUpdate objects
     * 
     */
    public ChangeRequestActive_Db(User_Db aUser, Component_Version_Db aCompVersion,
                                       ChangeRequest_Db aChangeRequest) {
    	setUser(aUser);
    	setCompVersion(aCompVersion);
        setChangeRequest(aChangeRequest);
    }

    
    /**
     * Data Members
     */
    private User_Db user;
    private Component_Version_Db compVersion;
    private ChangeRequest_Db changeRequest;
    
    
    /**
     * Getters
     */
    public User_Db getUser() { return user; }
    public Component_Version_Db getCompVersion() { return compVersion; }
    public ChangeRequest_Db getChangeRequest() { return changeRequest; }

    
    /**
     * Setters
     */
    private void setUser(User_Db aUser) { user = aUser; }
    private void setCompVersion(Component_Version_Db aCompVer) { compVersion = aCompVer; }
    private void setChangeRequest(ChangeRequest_Db aChgReq) { changeRequest = aChgReq; }
    private void setCompVersion(EdaContext xContext, long anId) { 
    	compVersion = new Component_Version_Db(anId);
    }
    private void setChangeRequest(EdaContext xContext, long anId) 
    throws IcofException { 
    	changeRequest = new ChangeRequest_Db(anId);
    }
    private void setUser(EdaContext xContext, short anId) { 
    	user = new User_Db(anId);
    }

    
    /**
     * Create a PreparedStatement to lookup this object by ids.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupIdsStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + USER_ID_COL + " = ? and " +
                       COMPONENT_VERSION_ID_COL + " = ?  and " +
                       CHANGE_REQUEST_ID_COL + " = ?";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup this object by User.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupUserStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + USER_ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup this object by User and CompVersion ids.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupUserCompVerStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + USER_ID_COL + " = ? and " +
                       COMPONENT_VERSION_ID_COL + " = ?";

        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    

    /**
     * Create a PreparedStatement to lookup this object by CompVersion ids.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupCompVerStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + COMPONENT_VERSION_ID_COL + " = ?";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup this object by ChangeRequest id.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupChangeReqStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + CHANGE_REQUEST_ID_COL + " = ?";
        
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
    private void setAddRowStatement(EdaContext xContext) throws IcofException {
        
        // Define the query.
        String query = "insert into " + TABLE_NAME + 
                       " ( " +  ALL_COLS + " )" + 
                       " values( ?, ?, ?, ?, ? )";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to delete a row.
     * 
     * @param  xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setDeleteRowStatement(EdaContext xContext) throws IcofException {
        
        // Define the query.
        String query = "delete from " + TABLE_NAME + 
                       " where " + USER_ID_COL + " = ? " +  
                       " and " + COMPONENT_VERSION_ID_COL + " = ? " +
                       " and " + CHANGE_REQUEST_ID_COL + " = ?";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to delete a row.
     * 
     * @param  xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setDeleteRowByCrStatement(EdaContext xContext)
    throws IcofException {
        
        // Define the query.
        String query = "delete from " + TABLE_NAME + 
                       " where " + CHANGE_REQUEST_ID_COL + " = ?";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    
    /**
     * Look up this object by ids.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByIds(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupIdsStatement(xContext);
        
        try {
            getStatement().setShort(1, getUser().getId());
            getStatement().setLong(2, getCompVersion().getId());
            getStatement().setLong(3, getChangeRequest().getId());
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
     * Look up this object by User and Component Version ids.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByUserCompVersion(EdaContext xContext) 
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupUserCompVerStatement(xContext);
        
        try {
            getStatement().setShort(1, getUser().getId());
            getStatement().setLong(2, getCompVersion().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByUserCompVersion()",
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
                                                 "dbLookupByUserCompVersion()",
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
     * Look up this object by User id.
     * 
     * @param xContext  An application context object.
     * @return          Collection of ChangeRequestActive_Db objects.
     * @throws          Trouble querying the database.
     */
    public Vector<ChangeRequestActive_Db> dbLookupByUser(EdaContext xContext) throws IcofException{
        
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
        ResultSet rs = executeQuery(xContext);
        
        // Process the results
        Vector<ChangeRequestActive_Db> changeRequests = new Vector<ChangeRequestActive_Db>();
        try {
            while (rs.next()) {
                short userId =  rs.getShort(USER_ID_COL);
                long compVerId =  rs.getLong(COMPONENT_VERSION_ID_COL);
                long chgReqId =  rs.getLong(CHANGE_REQUEST_ID_COL);
                ChangeRequestActive_Db cra = 
                	new ChangeRequestActive_Db(xContext, userId, compVerId, chgReqId);
                changeRequests.add(cra);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupByUserLoc()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        
        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return changeRequests;
        
    }


    /**
     * Look up this object by Component Version id.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public Vector<ChangeRequestActive_Db> dbLookupByCompVersion(EdaContext xContext) 
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupCompVerStatement(xContext);
        
        try {
            getStatement().setLong(1, getCompVersion().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByCompVer()",
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
        Vector<ChangeRequestActive_Db> changeRequests = new Vector<ChangeRequestActive_Db>();
        try {
            while (rs.next()) {
                short userId =  rs.getShort(USER_ID_COL);
                long compVerId =  rs.getLong(COMPONENT_VERSION_ID_COL);
                long chgReqId =  rs.getLong(CHANGE_REQUEST_ID_COL);
                ChangeRequestActive_Db cra = 
                	new ChangeRequestActive_Db(xContext, userId, compVerId, chgReqId);
                changeRequests.add(cra);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupByCompVerLoc()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        
        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return changeRequests;
        
    }


    /**
     * Look up this object by Change Request id.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public Vector<ChangeRequestActive_Db> dbLookupByChangeReq(EdaContext xContext) 
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupChangeReqStatement(xContext);
        
        try {
            getStatement().setLong(1, getChangeRequest().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByChangeReq()",
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
        Vector<ChangeRequestActive_Db> changeRequests = new Vector<ChangeRequestActive_Db>();
        try {
            while (rs.next()) {
                short userId =  rs.getShort(USER_ID_COL);
                long compVerId =  rs.getLong(COMPONENT_VERSION_ID_COL);
                long chgReqId =  rs.getLong(CHANGE_REQUEST_ID_COL);
                ChangeRequestActive_Db cra = 
                	new ChangeRequestActive_Db(xContext, userId, compVerId, chgReqId);
                changeRequests.add(cra);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupByChangeReq()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        
        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return changeRequests;
        
    }

    
    /**
     * Insert a new row.
     * 
     * @param xContext  An application context object.
     * @param creator   Person making this update
     * 
     * @throws          Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext, User_Db creator)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        Timestamp now = new Timestamp(new java.util.Date().getTime());
        try {
            getStatement().setShort(1, getUser().getId());
            getStatement().setLong(2, getCompVersion().getId());
        	getStatement().setLong(3, getChangeRequest().getId());
            getStatement().setString(4, creator.getIntranetId());
            getStatement().setTimestamp(5, now);

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
     * Delete a row.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbDeleteRow(EdaContext xContext) throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setDeleteRowStatement(xContext);

        try {
            getStatement().setShort(1, getUser().getId());
            getStatement().setLong(2, getCompVersion().getId());
        	getStatement().setLong(3, getChangeRequest().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbDeleteRow()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getQuery().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! insertRow(xContext)) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbDeleteRow()",
                                                 IcofException.SEVERE,
                                                 "Unable to delete existing row.\n",
                                                 "QUERY: " + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Close the PreparedStatement.
        closeStatement(xContext);

    }


    /**
     * Delete a row by the Change Request id
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbDeleteRowByCr(EdaContext xContext) throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setDeleteRowByCrStatement(xContext);

        try {
        	getStatement().setLong(1, getChangeRequest().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbDeleteRowByCr()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getQuery().toString());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query - don't care if insert returns true or false.
        insertRow(xContext);
        
        // Close the PreparedStatement.
        closeStatement(xContext);

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
        
    	setUser(xContext, rs.getShort(USER_ID_COL));
        setCompVersion(xContext, rs.getLong(COMPONENT_VERSION_ID_COL));
        setChangeRequest(xContext, rs.getLong(CHANGE_REQUEST_ID_COL));
        setCreatedBy(rs.getString(CREATED_BY_COL));
        setCreatedOn(rs.getTimestamp(CREATED_ON_COL));
        setLoadFromDb(true);
        
    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {
    	
        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        
        if (getCompVersion() != null) {
            buffer.append("Component: " + getCompVersion().getCompRelease().getComponent().getName() + "\n");
            buffer.append("ToolKit: " + getCompVersion().getVersion().getDisplayName() + "\n");
        }
        else {
            buffer.append("Component: NULL\n");
            buffer.append("ToolKit: NULL\n");
        }

        if (getChangeRequest() != null) {
            buffer.append("ChangeRequest ID: " + getChangeRequest().getId() + "\n");
        }
        else {
            buffer.append("ChangeRequest ID: NULL\n");
        }

        buffer.append("Created by: " + getCreatedBy() + "\n");
        if (getCreatedOn() == null) {
            buffer.append("Created on: null\n");
        }
        else {
            buffer.append("Created on: " + getCreatedOn().toString() + "\n");
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
        return String.valueOf(getUser().getIdKey(xContext) + "_" +
        		              getChangeRequest().getIdKey(xContext) + "_" +
        		              getCompVersion().getIdKey(xContext));
    }


}

