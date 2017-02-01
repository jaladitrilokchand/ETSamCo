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
* Event DB class with audit info
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 07/29/2013 GFS  Initial coding.
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

public class Event_Db extends TkAudit {

    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.EVENT";
    public static final String ID_COL = "EVENT_ID";
    public static final String NAME_ID_COL = "EVENTNAME_ID";
    public static final String EVENTS_COL = "EVENTS_ID";
    public static final String COMMENT_COL = "COMMENT";
    public static final String EXPIRED_ON_COL = "EXPIRED_TMSTMP";
    public static final String ALL_COLS = ID_COL + "," + NAME_ID_COL + "," +
                                          EVENTS_COL + "," + COMMENT_COL + "," +	
                                          CREATED_BY_COL + "," + CREATED_ON_COL +
                                          "," + EXPIRED_ON_COL; 

    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public Event_Db(long anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes Event Name id, Events Id and a comment
     * 
     * @param aNameId     Id of Event Name
     * @param anEventsId  Id of Events collection
     */
    public Event_Db(long aNameId, long anEventsId, String aComment) {
    	setEventNameId(aNameId);
        setEventsId(anEventsId);
        setComments(aComment);
    }

    
    /**
     * Constructor - takes all members
     * 
     * @param id           Database id of this object
     * @param eventNameId  An Event Name object Id
     * @param eventsId     An Events object id
     * @param comment      A comment
     * @param createdBy    Creator's intranet id
     * @param createdOn    Creation timestamp
     * @param expiredOn    Expired timestamp (null for new events)
     */
    public Event_Db(long id, long eventNameId, long eventsId, 
                    String comment, String createdBy, 
                    Timestamp createdOn, Timestamp expiredOn) {
    	setId(id);
    	setEventNameId(eventNameId);
        setEventsId(eventsId);
        setComments(comment);
        setCreatedBy(createdBy);
        setCreatedOn(createdOn);
        setExpiredOn(expiredOn);
        
    }
    
   
    /**
     * Constructor - all members except id
     * 
     * @param eventNameId  An Event Name object Id
     * @param eventsId     An Events object id
     * @param comment      A comment
     * @param createdBy    Creator's intranet id
     * @param createdOn    Creation timestamp
     * @param expiredOn    Expired timestamp (null for new events)
     */
    public Event_Db(long eventNameId, long eventsId, 
                    String comment, String createdBy, 
                    Timestamp createdOn, Timestamp expiredOn) {
    	setEventNameId(eventNameId);
        setEventsId(eventsId);
        setComments(comment);
        setCreatedBy(createdBy);
        setCreatedOn(createdOn);
        setExpiredOn(expiredOn);
        
    }
    
    
    /**
     * Data Members
     */
    private long id;
    private long eventNameId;
    private long eventsId;
    private String comments;
    private Timestamp expiredOn;
    
    
    /**
     * Getters
     */
    public long getId() { return id; }
    public long getEventNameId() { return eventNameId; }
    public long getEventsId() { return eventsId; }
    public String getComments() { return comments; }
    public Timestamp getExpiredOn() { return expiredOn; }
    
    
    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setEventNameId(long anId) { eventNameId = anId; }
    private void setEventsId(long anId) { eventsId = anId; }
    private void setComments(String aComment) { comments = aComment; }
    private void setExpiredOn(Timestamp aTms) { expiredOn = aTms; }
    
    
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
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupNameStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + NAME_ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    
  
    /**
     * Create a PreparedStatement to lookup this object by Event Name and 
     * Events Id.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupByEventsIdStatement(EdaContext xContext)
    throws IcofException {

//    	select e.event_id, en.eventname_id, e.comment, e.created_by, 
//	       e.created_tmstmp. e.expired_tmstmp
//    	from tk.Event as e,
//    	     tk.EventName as en
//    	where e.events_id = ?
//    	  and e.eventname_id = en.eventname_id
//    	order by e.created_tmstmp asc
    	
        // Define the query.
        String query = "select e." + ID_COL + 
                       ", en." + EventName_Db.ID_COL + 
                       ", e." + COMMENT_COL + 
                       ", e." + CREATED_BY_COL + 
                       ", e." + CREATED_ON_COL + 
                       " ,e." + EXPIRED_ON_COL +  
                       " from " + TABLE_NAME + " as e, " +
                       EventName_Db.TABLE_NAME + " as en " +
                       " where e." + EVENTS_COL + " = ? " + 
                       " and e." + NAME_ID_COL +  " = en." + EventName_Db.ID_COL +               
                       " order by e." + CREATED_ON_COL + " asc ";

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
                       " values( ?, ?, ?, ?, ?, " + CURRENT_TMS + ", ? )";
        
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
     * Create a PreparedStatement to update this object expired timestamp
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setExpiredTmsStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "update " + TABLE_NAME + 
                       " set " + EXPIRED_ON_COL + " = " + CURRENT_TMS + 
                       " where " + EVENTS_COL + " = ? " +
                       " and " + EXPIRED_ON_COL + " is null";
        
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
                       " set " +  NAME_ID_COL + " = ? , " + 
                                  EVENTS_COL + " = ? , " +
                                  COMMENT_COL + " = ? " +
                       " where " + ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    


    /**
     * Look up the Event by id.
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
     * Look up the Events ID.
     * 
     * @param xContext  An application context object.
     * @param eventsId  Events id to find individual events for
     * @throws          Trouble querying the database.
     */
    public Vector<Event_Db> dbLookupByEventsId(EdaContext xContext, long eventsId) 
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupByEventsIdStatement(xContext);
        
        try {
            getStatement().setLong(1, eventsId);
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByEventsId()",
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
        Vector<Event_Db> events = new Vector<Event_Db>();
        try {
            while (rs.next()) {
            	// e.event_id, en.eventname, e.comment, e.created_by, e.created_tmstmp
                long anId =  rs.getLong(ID_COL);
                long anEventNameId =  rs.getLong(EventName_Db.ID_COL);
                String comment =  rs.getString(COMMENT_COL);
                String createdBy =  rs.getString(CREATED_BY_COL);
                Timestamp createdOn =  rs.getTimestamp(CREATED_ON_COL);
                Timestamp expiredOn =  rs.getTimestamp(EXPIRED_ON_COL);
                
                Event_Db event = new Event_Db(anId, anEventNameId, getEventsId(), 
                                              comment, createdBy, 
                                              createdOn, expiredOn);
                events.add(event);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), 
                                    "dbLookupByEventId()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);

        return events;
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
        setId(getNextBigIntId(xContext, getStatement()));
        closeStatement(xContext);
        
        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        try {
            getStatement().setLong(1, getId());
            getStatement().setLong(2, getEventNameId());
            getStatement().setLong(3, getEventsId());
            getStatement().setString(4, getComments());
            getStatement().setString(5, user.getIntranetId());
            getStatement().setTimestamp(6, getExpiredOn());
            
            
//            printQuery(xContext);
//            System.out.println("ID " + getId());
//            System.out.println("EventName ID " + getEventNameId());
//            System.out.println("Events ID " + getEventsId());
//            System.out.println("Comments " + getComments());
//            System.out.println("User " + user.getIntranetId());
//            System.out.println("Tms " + getExpiredOn());
            
            
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
     * @param eventsId     New Events Id
     * @throws          Trouble querying the database.
     */
    public void dbUpdateExpiredTms(EdaContext xContext, long eventsId)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setExpiredTmsStatement(xContext);
        try {
            getStatement().setLong(1, eventsId);
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass().getName(),
                                                 "dbUpdateExpiredTms()",
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
                                                 "dbUpdateExpiredTms()",
                                                 IcofException.SEVERE,
                                                 "Unable to update existing row.\n",
                                                 "\nQUERY: " + getQuery() + 
                                                 "\nID: " + eventsId);
                                                
            xContext.getSessionLog().log(ie);
            throw ie;
        }

        // Close the PreparedStatement.
        closeStatement(xContext);

        // Set the names on this object.
        setExpiredOn(expiredOn);
        setLoadFromDb(true);
        
    }
    

    /**
     * Update this object.
     * 
     * @param xContext  An application context object.
     * @param eventNameId  New event name id
     * @param eventsId     New Events Id
     * @param comments     New comments
     * @throws          Trouble querying the database.
     */
    public void dbUpdateRow(EdaContext xContext, long eventNameId,
                            long eventsId, String comments)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setUpdateStatement(xContext);
        try {
            getStatement().setLong(1, eventNameId);
            getStatement().setLong(2, eventsId);
            getStatement().setString(3, comments);
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
                                                 "Unable to update row.\n",
                                                 "QUERY: " + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }

        // Close the PreparedStatement.
        closeStatement(xContext);

        // Set the names on this object.
        setEventNameId(eventNameId);
        setEventsId(eventsId);
        setComments(comments);
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
        setEventNameId(rs.getLong(NAME_ID_COL));
        setEventsId(rs.getLong(EVENTS_COL));
        setComments(rs.getString(COMMENT_COL));
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
        buffer.append("ID: " + getId() + "\n");
        buffer.append("Event name id: " + getEventNameId() + "\n");
        buffer.append("Events id: " + getEventsId() + "\n");
        buffer.append("Comments: " + getComments() + "\n");
        buffer.append("Created by: " + getCreatedBy() + "\n");
        buffer.append("Created on: " + getCreatedOn() + "\n");
        
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
