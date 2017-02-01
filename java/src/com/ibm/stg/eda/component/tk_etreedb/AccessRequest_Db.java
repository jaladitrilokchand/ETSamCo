/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2013 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*
*-PURPOSE---------------------------------------------------------------------
* Access Request class
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 08/02/2013 GFS  Initial coding.
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

public class AccessRequest_Db extends TkAudit {
	
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.ACCESS_REQUEST";
    public static final String ID_COL = "ACCESS_REQUEST_ID";
    public static final String USER_ID_COL = "USER_ID";
    public static final String COMP_REL_ID_COL = "COMPONENT_TKRELEASE_ID";
    public static final String ACCESS_TYPE_ID_COL = "ACCESS_TYPE_ID";
    public static final String EVENTS_ID_COL = "EVENTS_ID";
    public static final String REQUESTOR_COL = "REQUESTOR";
    public static final String ALL_COLS = ID_COL + "," + USER_ID_COL + "," +
                                          COMP_REL_ID_COL + "," + ACCESS_TYPE_ID_COL + "," +	
                                          EVENTS_ID_COL + "," + REQUESTOR_COL;
    private static final long serialVersionUID = 5722775620131252452L;

    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public AccessRequest_Db(long anId) {
        setId(anId);
    }

    
    /**
     * Constructor - all members except DB id
     * 
     * @param aUser        User needing access
     * @param aCompRel     Component/Release to needing access to
     * @param anAccessType Access being requested
     * @param anEvents     Events collection
     * @param aRequestor   Intranet id of person making request
     */
    public AccessRequest_Db(User_Db aUser, Component_Release_Db aCompRel, 
                            AccessType_Db anAccessType, Events_Db anEvents, 
                            String aRequestor) {
    	setUser(aUser);
        setCompRelease(aCompRel);
        setAccessType(anAccessType);
        setEvents(anEvents);
        setRequestor(aRequestor);
    }

    
    /**
     * Constructor - all members
     * 
     * @param anId           Id of this object
     * @param aUserId        User needing access
     * @param aCompRelId     Component/Release to needing access to
     * @param anAccessTypeId Access being requested
     * @param anEventsId     Events collection
     * @param aRequestorId   Intranet id of person making request
     */
    public AccessRequest_Db(long id, short aUserId, short aCompRelId, 
                            long anAccessTypeId, long anEventsId, 
                            String aRequestor) {
    	setId(id);
    	setUser(aUserId);
        setCompRelease(aCompRelId);
        setAccessType(anAccessTypeId);
        setEvents(anEventsId);
        setRequestor(aRequestor);
    }
    
    
    
    /**
     * Data Members
     */
    private long id;
    private User_Db user;
    private Component_Release_Db compRel;
    private AccessType_Db accessType;
    private Events_Db events;
    private String requestor;
    
    
    /**
     * Getters
     */
    public long getId() { return id; }
    public User_Db getUser() { return user; }
    public Component_Release_Db getCompRelease() { return compRel; }
    public AccessType_Db getAccessType() { return accessType; }
    public Events_Db getEvents() { return events; }
    public String getRequester() { return requestor; }
    
    
    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setUser(User_Db aUser) { user = aUser; }
    private void setCompRelease(Component_Release_Db aCompRel) { compRel = aCompRel; }
    private void setAccessType(AccessType_Db aType) { accessType = aType; }
    private void setEvents(Events_Db anEvents) { events = anEvents; }
    private void setRequestor(String aName) { requestor = aName; }
    private void setUser(short anId) { 
    	if (getUser() != null) 
    		user = null;
    	user = new User_Db(anId);
    }
    private void setCompRelease(short anId) { 
    	if (getCompRelease() != null) 
    		compRel = null;
    	compRel = new Component_Release_Db(anId);
    }
    private void setAccessType(long anId) {
    	if (getAccessType() != null) 
    		accessType = null;
    	accessType = new AccessType_Db(anId); 
    }
    private void setEvents(long anId) { 
    	if (getEvents() != null) 
    		events = null;
    	events = new Events_Db(anId); 
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
                       " where " + ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    /**
     * Create a PreparedStatement to lookup this object.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupAllStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + USER_ID_COL + " = ? " +
                       " and " + COMP_REL_ID_COL + " = ? " +
                       " and " + ACCESS_TYPE_ID_COL + " = ? " +
                       " and " + EVENTS_ID_COL + " = ? " +
                       " and " + REQUESTOR_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    
    /**
     * Create a PreparedStatement to lookup this object by requester.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupByRequestorStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + REQUESTOR_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup objects that are in a certain 
     * event state.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupByEventNameStatement(EdaContext xContext) throws IcofException {

//    	select * 
//    	from tk.Event as e,
//    	     tk.Access_Request as ar
//    	where e.EventName_id = 2
//    	  and ar.Events_id = e.Events_id
//    	  and e.expired_tmstmp is null
    	
        // Define the query.
        String query = "select " + ID_COL + "," + USER_ID_COL + "," + 
                         COMP_REL_ID_COL + "," + ACCESS_TYPE_ID_COL + 
                         ",ar." + EVENTS_ID_COL + "," + REQUESTOR_COL + 
                        " from " + TABLE_NAME + " as ar, " +
                         Event_Db.TABLE_NAME + " as e " +
                        " where e." + Event_Db.NAME_ID_COL + " = ? " +
                        " and ar." + EVENTS_ID_COL + " = e." + Event_Db.EVENTS_COL +
                        " and e." + Event_Db.EXPIRED_ON_COL + " is null";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    
    
    /**
     * Create a PreparedStatement to lookup objects where that have the latest
     * state equal to the given state.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupByUserStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + USER_ID_COL + " = ? ";
        
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
                       " values( ?, ?, ?, ?, ?, ? )";
        
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
                       " set " +  USER_ID_COL + " = ? , " + 
                                  COMP_REL_ID_COL + " = ? , " +
                                  ACCESS_TYPE_ID_COL + " = ? " +
                                  EVENTS_ID_COL + " = ? " +
                                  REQUESTOR_COL + " = ? " +
                       " where " + ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    

    /**
     * Look up by the id.
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
     * Look up by all data members
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByAll(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupAllStatement(xContext);
         
        try {
            getStatement().setShort(1, getUser().getId());
            getStatement().setShort(2, getCompRelease().getId());
            getStatement().setLong(3, getAccessType().getId());
            getStatement().setLong(4, getEvents().getId());
            getStatement().setString(5, getRequester());
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
        if (! selectSingleRow(xContext, getStatement())) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByAll()",
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
     * Look up by Requestor
     * 
     * @param xContext  An application context object.
     * @param eventsId  Events id to find individual events for
     * @throws          Trouble querying the database.
     */
    public Vector<AccessRequest_Db> dbLookupByRequestor(EdaContext xContext,
                                                        String aRequestor) 
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupByRequestorStatement(xContext);
        
        try {
            getStatement().setString(1, aRequestor);
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByRequestor()",
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
        Vector<AccessRequest_Db> requests = new Vector<AccessRequest_Db>();
        try {
            while (rs.next()) {
                long anId =  rs.getLong(ID_COL);
                short aUserId =  rs.getShort(USER_ID_COL);
                short aCompRelId =  rs.getShort(COMP_REL_ID_COL);
                long anAccessTypeId =  rs.getLong(ACCESS_TYPE_ID_COL);
                long anEventsId =  rs.getLong(EVENTS_ID_COL);
                
                AccessRequest_Db event = new AccessRequest_Db(anId, aUserId, 
                                                              aCompRelId, 
                                                              anAccessTypeId, 
                                                              anEventsId, 
                                                              aRequestor);
                requests.add(event);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), 
                                    "dbLookupByRequestor()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);

        return requests;
        
    }
    
 
    /**
     * Look up by Event Name
     * 
     * @param xContext     An application context object.
     * @param eventNameId  EventName id to find events for
     * @throws          Trouble querying the database.
     */
    public Vector<AccessRequest_Db> dbLookupByEventName(EdaContext xContext,
                                                        long eventNameId) 
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupByEventNameStatement(xContext);
        
        try {
            getStatement().setLong(1, eventNameId);
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByEventName()",
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
        Vector<AccessRequest_Db> requests = new Vector<AccessRequest_Db>();
        try {
            while (rs.next()) {
                long anId =  rs.getLong(ID_COL);
                short aUserId =  rs.getShort(USER_ID_COL);
                short aCompRelId =  rs.getShort(COMP_REL_ID_COL);
                long anAccessTypeId =  rs.getLong(ACCESS_TYPE_ID_COL);
                long anEventsId =  rs.getLong(EVENTS_ID_COL);
                String aRequester =  rs.getString(REQUESTOR_COL);
                
                AccessRequest_Db event = new AccessRequest_Db(anId, aUserId, 
                                                              aCompRelId, 
                                                              anAccessTypeId, 
                                                              anEventsId, 
                                                              aRequester);
                requests.add(event);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), 
                                    "dbLookupByEventName()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);

        return requests;
        
    }

    
    /**
     * Insert a new row.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext)
    throws IcofException{

        // Create the next id for this new row
        setNextIdStatement(xContext);
        setId(getNextBigIntId(xContext, getStatement()));
        closeStatement(xContext);
        
        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        try {
            getStatement().setLong(1, getId());
            getStatement().setShort(2, getUser().getId());
            getStatement().setShort(3, getCompRelease().getId());
            getStatement().setLong(4, getAccessType().getId());
            getStatement().setLong(5, getEvents().getId());
            getStatement().setString(6, getRequester());
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
     * @param xContext        An application context object.
     * @param aUserId         New user id
     * @param aCompRelId      New ComponentTkRelease id
     * @param anAccessTypeId  New AccessType id
     * @param anEventsId      New Events id
     * @param aRequestor      New Requester's intranet id
     * @throws          Trouble querying the database.
     */
    public void dbUpdateRow(EdaContext xContext, User_Db newUser,
                            Component_Release_Db newCompRel,
                            AccessType_Db newAccessType,
                            Events_Db newEvents, 
                            String newRequestor)
    throws IcofException{
    	
        // Create the SQL query in the PreparedStatement.
        setUpdateStatement(xContext);
        try {
            getStatement().setShort(1, newUser.getId());
            getStatement().setShort(2, newCompRel.getId());
            getStatement().setLong(3, newAccessType.getId());
            getStatement().setLong(4, getEvents().getId());
            getStatement().setString(5, newRequestor);
            getStatement().setLong(6, getId());

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
        setUser(newUser);
        setCompRelease(newCompRel);
        setAccessType(newAccessType);
        setEvents(newEvents);
        setRequestor(newRequestor);
        setLoadFromDb(true);

        
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
     * Populate this object from the result set.
     * 
     * @param xContext  Application context.
     * @param rs        A valid result set.
     * @throws          Trouble retrieving the data.
     */
    protected void populate(EdaContext xContext, ResultSet rs) 
    throws SQLException, IcofException {

        setId(rs.getLong(ID_COL));
        setUser(rs.getShort(USER_ID_COL));
        setCompRelease(rs.getShort(COMP_REL_ID_COL));
        setAccessType(rs.getLong(ACCESS_TYPE_ID_COL));
        setEvents(rs.getLong(EVENTS_ID_COL));
        setRequestor(rs.getString(REQUESTOR_COL));
        setLoadFromDb(true);

    }
    
    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID            : " + getId() + "\n");
        buffer.append("User id       : " + getUser().getId() + "\n");
        buffer.append("Comp Rel id   : " + getCompRelease().getId() + "\n");
        buffer.append("Access Type id: " + getAccessType().getId() + "\n");
        buffer.append("Events id     : " + getEvents().getId() + "\n");
        buffer.append("Requestor     : " + getRequester() + "\n");
        
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
