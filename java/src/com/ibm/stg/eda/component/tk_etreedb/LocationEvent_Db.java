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
* LocationEvent DB class
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 02/02/2011 GFS  Initial coding.
* 04/17/2012 GFS  Added dbLookupAGTSEvents method.
* 05/03/2012 GFS  Added dbLookupXmitEvents method.
* 12/18/2012 GFS  Added dbLookupLastAdvance and dbLookupXtinctEvents methods.
* 06/20/2013 GFS  Updated setLookupXtinctEvents to get CUSTOMTK_ events too.
* 07/30/2013 GFS  Updated to support changing Event class to LocationEvent.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.eda.component.tk_etreeobjs.LocationEvent;
import com.ibm.stg.iipmds.common.IcofException;


public class LocationEvent_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5467916710689869575L;
	
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.LOCATIONEVENT";
    public static final String ID_COL = "LOCATIONEVENT_ID";
    public static final String PLATFORM_ID_COL = "PLATFORM_ID";
    public static final String COMP_VER_LOCATION_ID_COL = "COMPONENTTKVERSION_LOCATION_ID";
    public static final String LOCATION_EVENT_NAME_ID_COL = "LOCATIONEVENTNAME_ID";
    public static final String COMMENT_COL = "COMMENT";
    public static final String ALL_COLS = ID_COL + ", " + PLATFORM_ID_COL + ", " +
                                          COMP_VER_LOCATION_ID_COL + ", " +
                                          LOCATION_EVENT_NAME_ID_COL + ", " +
                                          COMMENT_COL + ", " +
                                          CREATED_BY_COL + ", " +
                                          CREATED_ON_COL;

    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public LocationEvent_Db(long anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes all objects
     * 
     * @param aPlatform      Platform_Db onject
     * @param aCompVerLoc    Component_Version_Location_Db object
     * @param anEventName    LocationEventName_Db object
	 * @param aComment       Event comments
     */
    public LocationEvent_Db(Platform_Db aPlatform, Component_Version_Location_Db aCompVerLoc,
    		                LocationEventName_Db anEventName, String aComment) {
        setPlatform(aPlatform);
        setCompVerLocation(aCompVerLoc);
        setEventName(anEventName);
        setComment(aComment);
    }

    
    /**
     * Data Members
     */
    private long id;
    private Platform_Db platform;
    private Component_Version_Location_Db compVerLocation;
    private LocationEventName_Db eventName;
    private String comment;
    
    
    /**
     * Getters
     */
    public long getId() { return id; }
    public Platform_Db getPlatform() { return platform; }
    public Component_Version_Location_Db getCompVerLocation() { return compVerLocation; }
    public LocationEventName_Db getEventName() { return eventName; }
    public String getComment() { return comment; }


    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setPlatform(Platform_Db aPlat) { platform = aPlat; }
    private void setCompVerLocation(Component_Version_Location_Db aLoc) { compVerLocation = aLoc; }
    private void setEventName(LocationEventName_Db aLocEvent) { eventName = aLocEvent; }
    private void setComment(String aText) { comment = aText; }
    private void setPlatform(EdaContext xContext, short anId) { 
    	platform = new Platform_Db(anId);
    }
    private void setCompVerLocation(EdaContext xContext, short anId) { 
    	compVerLocation = new Component_Version_Location_Db(anId);
    }
    private void setEventName(EdaContext xContext, short anId) { 
    	eventName = new LocationEventName_Db(anId);
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
                       " where " + ID_COL + " = ?";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup objects by Location.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupByLocationStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + COMP_VER_LOCATION_ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    

    /**
     * Create a PreparedStatement to lookup objects by Event. Reverse sorting
     * the list by created date (ie, most recent first).
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupByEventStatement(EdaContext xContext) throws IcofException {

//    	select max(LocationEvent_id) as LOCATIONEVENT_ID
//    	 from TK.LOCATIONEVENT
//    	 where COMPONENTTKVERSION_LOCATION_ID = 5
//    	  and LOCATIONEVENTNAME_ID = 8
    	
        // Define the query.
        String query = "select max(" + ID_COL + ")  as " + ID_COL + 
                       " from " + TABLE_NAME + 
                       " where " + COMP_VER_LOCATION_ID_COL + " = ? " +
                       " and " + LOCATION_EVENT_NAME_ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
 
    
    public void setLookupLastAdvanceStatement(EdaContext xContext) throws IcofException {

//    	select max(LocationEvent_id) as LOCATIONEVENT_ID
//    	  from TK.LOCATIONEVENT
//    	  where COMPONENTTKVERSION_LOCATION_ID = 3040
//    	    and LOCATIONEVENTNAME_ID IN (select LocationEventName_id 
//    	                                  from tk.LocationEventName 
//    	                                  where EventName like 'ADVANCED_%') 
    	
        // Define the query.
        String query = "select max(" + ID_COL + ")  as " + ID_COL + 
                       " from " + TABLE_NAME + 
                       " where " + COMP_VER_LOCATION_ID_COL + " = ? " +
                       " and " + LOCATION_EVENT_NAME_ID_COL + " IN " +
                       "(select " + LocationEventName_Db.ID_COL +
                       " from " + LocationEventName_Db.TABLE_NAME +
                       " where " + LocationEventName_Db.NAME_COL + " like 'ADVANCED_%')";      
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }


    /**
     * Create a PreparedStatement to lookup the most recent Timestamp of Event
     * for a given ToolKit and Component.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupLastEventStatement(EdaContext xContext) throws IcofException {

//    	select len.EventName, max(le.Created_TmStmp) as created_on  
//    	  from Tk.LocationEvent as le, 
//    	       Tk.LocationEventName as len, 
//    	       Tk.ComponentTkVersion_Location as cvl,
//    	       Tk.Component_TkVersion as cv,
//    	       Tk.Component_TkRelease as cr
//    	 where cv.TkVersion_Id = 1
//    	   and cr.Component_Id = 3
//    	   and cv.Component_TkRelease_Id = cr.Component_TkRelease_Id
//    	   and cv.Component_TkVersion_Id = cvl.Component_TkVersion_Id
//    	   and cvl.ComponentTkVersion_Location_Id = le.ComponentTkVersion_Location_Id
//    	   and le.LocationEventName_Id = len.LocationEventName_Id
//    	group by len.EventName
    	
        // Define the query.
        String query = "select len." + LocationEventName_Db.NAME_COL + ", " +
                             " max(le.Created_TmStmp) as " + CREATED_ON_COL +  
        	           "  from " + LocationEvent_Db.TABLE_NAME + " as le, " + 
        	                       LocationEventName_Db.TABLE_NAME + " as len, " +
        	                       Component_Version_Location_Db.TABLE_NAME + " as cvl, " +
        	                       Component_Version_Db.TABLE_NAME + " as cv, " +
        	                       Component_Release_Db.TABLE_NAME + " as cr " +
        	           " where cv." + Component_Version_Db.VERSION_ID_COL + " = ? " +
        	           "   and cr." + Component_Release_Db.COMP_ID_COL + " = ? " +
        	           "   and cv." + Component_Version_Db.REL_COMP_ID_COL + " = cr." + Component_Release_Db.ID_COL + 
        	           "   and cvl." + Component_Version_Location_Db.COMPONENT_VERSION_ID_COL + " = cv." + Component_Version_Db.ID_COL +
        	           "   and le." + LocationEvent_Db.COMP_VER_LOCATION_ID_COL + " = cvl." + Component_Version_Location_Db.ID_COL + 
        	           "   and le." + LocationEvent_Db.LOCATION_EVENT_NAME_ID_COL + " = len." + LocationEventName_Db.ID_COL +
        	           " group by len." + LocationEventName_Db.NAME_COL;
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    
    /**
     * Create a PreparedStatement to lookup the AGTS events for a 
     * given ToolKit and Component.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupAGTSEventsStatement(EdaContext xContext) throws IcofException {

//    	select len.EventName, le.Created_TmStmp  
//    	  from Tk.LocationEvent as le, 
//    	       Tk.LocationEventName as len, 
//    	       Tk.ComponentTkVersion_Location as cvl,
//    	       Tk.Component_TkVersion as cv,
//    	       Tk.Component_TkRelease as cr
//    	 where cv.TkVersion_Id = 1
//    	   and cr.Component_Id = 3
//    	   and cv.Component_TkRelease_Id = cr.Component_TkRelease_Id
//    	   and cv.Component_TkVersion_Id = cvl.Component_TkVersion_Id
//    	   and cvl.ComponentTkVersion_Location_Id = le.ComponentTkVersion_Location_Id
//    	   and le.LocationEventName_Id = len.LocationEventName_Id
//         and len.EventName like 'AGTS%
//    	order by le.Created_TmStmp
    	
        // Define the query.
        String query = "select len." + LocationEventName_Db.NAME_COL + ", " +
                             " le. " + CREATED_ON_COL +  
        	           "  from " + LocationEvent_Db.TABLE_NAME + " as le, " + 
        	                       LocationEventName_Db.TABLE_NAME + " as len, " +
        	                       Component_Version_Location_Db.TABLE_NAME + " as cvl, " +
        	                       Component_Version_Db.TABLE_NAME + " as cv, " +
        	                       Component_Release_Db.TABLE_NAME + " as cr " +
        	           " where cv." + Component_Version_Db.VERSION_ID_COL + " = ? " +
        	           "   and cr." + Component_Release_Db.COMP_ID_COL + " = ? " +
        	           "   and cv." + Component_Version_Db.REL_COMP_ID_COL + " = cr." + Component_Release_Db.ID_COL + 
        	           "   and cvl." + Component_Version_Location_Db.COMPONENT_VERSION_ID_COL + " = cv." + Component_Version_Db.ID_COL +
        	           "   and le." + LocationEvent_Db.COMP_VER_LOCATION_ID_COL + " = cvl." + Component_Version_Location_Db.ID_COL + 
        	           "   and le." + LocationEvent_Db.LOCATION_EVENT_NAME_ID_COL + " = len." + LocationEventName_Db.ID_COL +
        	           "   and len." + LocationEventName_Db.NAME_COL + " like 'AGTS%' " +
        	           " order by le." + CREATED_ON_COL;
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    

    /**
     * Create a PreparedStatement to lookup the XMIT events for a 
     * given ToolKit and Component.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupXmitEventsStatement(EdaContext xContext) throws IcofException {

//    	select len.EventName, le.Created_TmStmp  
//    	  from Tk.LocationEvent as le, 
//    	       Tk.LocationEventName as len, 
//    	       Tk.ComponentTkVersion_Location as cvl,
//    	       Tk.Component_TkVersion as cv,
//    	       Tk.Component_TkRelease as cr
//    	 where cv.TkVersion_Id = 1
//    	   and cr.Component_Id = 3
//    	   and cv.Component_TkRelease_Id = cr.Component_TkRelease_Id
//    	   and cv.Component_TkVersion_Id = cvl.Component_TkVersion_Id
//    	   and cvl.ComponentTkVersion_Location_Id = le.ComponentTkVersion_Location_Id
//    	   and le.LocationEventName_Id = len.LocationEventName_Id
//         and (len.EventName like 'XMIT_% or len.EventName like 'CTK_PKG_%) 
//    	order by le.Created_TmStmp
    	
        // Define the query.
        String query = "select len." + LocationEventName_Db.NAME_COL + ", " +
                             " le. " + CREATED_ON_COL +  
        	           "  from " + LocationEvent_Db.TABLE_NAME + " as le, " + 
        	                       LocationEventName_Db.TABLE_NAME + " as len, " +
        	                       Component_Version_Location_Db.TABLE_NAME + " as cvl, " +
        	                       Component_Version_Db.TABLE_NAME + " as cv, " +
        	                       Component_Release_Db.TABLE_NAME + " as cr " +
        	           " where cv." + Component_Version_Db.VERSION_ID_COL + " = ? " +
        	           "   and cr." + Component_Release_Db.COMP_ID_COL + " = ? " +
        	           "   and cv." + Component_Version_Db.REL_COMP_ID_COL + " = cr." + Component_Release_Db.ID_COL + 
        	           "   and cvl." + Component_Version_Location_Db.COMPONENT_VERSION_ID_COL + " = cv." + Component_Version_Db.ID_COL +
        	           "   and le." + LocationEvent_Db.COMP_VER_LOCATION_ID_COL + " = cvl." + Component_Version_Location_Db.ID_COL + 
        	           "   and le." + LocationEvent_Db.LOCATION_EVENT_NAME_ID_COL + " = len." + LocationEventName_Db.ID_COL +
        	           "   and (len." + LocationEventName_Db.NAME_COL + " like 'XMIT_%' " +
        	           "   or len." + LocationEventName_Db.NAME_COL + " like 'CTK_PKG_%')" +
        	           " order by le." + CREATED_ON_COL;
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
 
    
    /**
     * Create a PreparedStatement to lookup the XTINCT events for a 
     * given ToolKit and Component.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupXtinctEventsStatement(EdaContext xContext) throws IcofException {

//    	select len.EventName, le.Created_TmStmp  
//    	  from Tk.LocationEvent as le, 
//    	       Tk.LocationEventName as len, 
//    	       Tk.ComponentTkVersion_Location as cvl,
//    	       Tk.Component_TkVersion as cv,
//    	       Tk.Component_TkRelease as cr
//    	 where cv.TkVersion_Id = 1
//    	   and cr.Component_Id = 3
//    	   and cv.Component_TkRelease_Id = cr.Component_TkRelease_Id
//    	   and cv.Component_TkVersion_Id = cvl.Component_TkVersion_Id
//    	   and cvl.ComponentTkVersion_Location_Id = le.ComponentTkVersion_Location_Id
//    	   and le.LocationEventName_Id = len.LocationEventName_Id
//         and (len.EventName like 'XTINCT_% or len.EventName like 'CUSTOMTK_%)
//    	order by le.Created_TmStmp
    	
        // Define the query.
        String query = "select len." + LocationEventName_Db.NAME_COL + ", " +
                             " le. " + CREATED_ON_COL +  
        	           "  from " + LocationEvent_Db.TABLE_NAME + " as le, " + 
        	                       LocationEventName_Db.TABLE_NAME + " as len, " +
        	                       Component_Version_Location_Db.TABLE_NAME + " as cvl, " +
        	                       Component_Version_Db.TABLE_NAME + " as cv, " +
        	                       Component_Release_Db.TABLE_NAME + " as cr " +
        	           " where cv." + Component_Version_Db.VERSION_ID_COL + " = ? " +
        	           "   and cr." + Component_Release_Db.COMP_ID_COL + " = ? " +
        	           "   and cv." + Component_Version_Db.REL_COMP_ID_COL + " = cr." + Component_Release_Db.ID_COL + 
        	           "   and cvl." + Component_Version_Location_Db.COMPONENT_VERSION_ID_COL + " = cv." + Component_Version_Db.ID_COL +
        	           "   and le." + LocationEvent_Db.COMP_VER_LOCATION_ID_COL + " = cvl." + Component_Version_Location_Db.ID_COL + 
        	           "   and le." + LocationEvent_Db.LOCATION_EVENT_NAME_ID_COL + " = len." + LocationEventName_Db.ID_COL +
        	           "   and (len." + LocationEventName_Db.NAME_COL + " like 'XTINCT_%' " +
        	           "   or len." + LocationEventName_Db.NAME_COL + " like 'CUSTOMTK_%' ) " +
        	           " order by le." + CREATED_ON_COL;
        
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
                       " ( " +  ALL_COLS + " )";
        if (getPlatform() != null) {
        	query += " values( ?, ?, ?, ?, ?, ?, ? )";
        }
        else {
        	query += " values( ?, NULL, ?, ?, ?, ?, ? )";
        }

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
     * Look up this object by location.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByLocation(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupByLocationStatement(xContext);
        
        try {
            getStatement().setLong(1, getCompVerLocation().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByLocation()",
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
                                                 "dbLookupByLocation()",
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
     * Create a list of LocationEvent objects for this ComponentVersion and for 
     * the specified event. The events are reverse sorted (ie, most recent first).
     * 
     * @param xContext  An application context object.
     * @param anEvent   Event to lookup
     * @return          Collection of LocationEvent objects
     * @throws          Trouble querying the database.
     */
    public void dbLookupLastEvent(EdaContext xContext, 
                                  LocationEventName_Db anEvent)
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupByEventStatement(xContext);
        
        try {
            getStatement().setLong(1, getCompVerLocation().getId());
            getStatement().setLong(2, anEvent.getId());
            
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupLastEvent()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query. 
        // If can't find the last event then set the id to 0 and continue.
        ResultSet rs = executeQuery(xContext);
        setId(0);
        try {
            while (rs.next()) {
                setId(rs.getLong(ID_COL));
            }
        }
        catch(SQLException ignore) { }
        
        // Close the PreparedStatement.
        closeStatement(xContext);

        // Look up this event if we found one
        if (getId() > 0)
        	dbLookupById(xContext);
        
    }


    /**
     * Sets this object to the last ADVANCED_* event for this ComponentVersion.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupLastAdvance(EdaContext xContext)
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupLastAdvanceStatement(xContext);
        
        try {
            getStatement().setLong(1, getCompVerLocation().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupLastAdvance()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query. 
        // If can't find the last event then set the id to 0 and continue.
        ResultSet rs = executeQuery(xContext);
        setId(0);
        try {
            while (rs.next()) {
                setId(rs.getLong(ID_COL));
            }
        }
        catch(SQLException ignore) { }
        
        // Close the PreparedStatement.
        closeStatement(xContext);

        // Look up this event if we found one
        if (getId() > 0)
        	dbLookupById(xContext);
        
    }

    
    /**
     * Determine the latest events for the given ToolKit and Component.
     * 
     * @param xContext     An application context object.
     * @param aRelVersion  RelVersion_Db object (ToolKit)
     * @param aComponent   Component_Db object
     * @return             Collection of event objects
     * @throws             Trouble querying the database.
     */
    public Hashtable<String,LocationEvent>  dbLookupLastEvents(EdaContext xContext, 
                                                               RelVersion_Db aRelVersion,
                                                               Component_Db aComponent)
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupLastEventStatement(xContext);
        
        try {
            getStatement().setShort(1, aRelVersion.getId());
            getStatement().setShort(2, aComponent.getId());
            
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupLastEvent()",
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
        Hashtable<String,LocationEvent> events = new Hashtable<String,LocationEvent> ();
        try {
            while (rs.next()) {
                String eventName =  rs.getString(LocationEventName_Db.NAME_COL);
                Timestamp createdOn =  rs.getTimestamp(CREATED_ON_COL);
                LocationEvent event = new LocationEvent(xContext, eventName, "", createdOn);
                events.put(event.getName(), event);
            }
        	
        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupLastEvent()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }
        
        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return events;
        
    }

    
    /**
     * Determine the AGTS events for the given ToolKit and Component.
     * 
     * @param xContext     An application context object.
     * @param aRelVersion  RelVersion_Db object (ToolKit)
     * @param aComponent   Component_Db object
     * @return             Collection of event objects
     * @throws             Trouble querying the database.
     */
    public Vector<LocationEvent> dbLookupAGTSEvents(EdaContext xContext, 
                                                    RelVersion_Db aRelVersion,
                                                    Component_Db aComponent)
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupAGTSEventsStatement(xContext);
        
        try {
            getStatement().setShort(1, aRelVersion.getId());
            getStatement().setShort(2, aComponent.getId());
            
            //printQuery(xContext);
            //System.out.println("RelVersion: " + aRelVersion.getId());
            //System.out.println("Component : " + aComponent.getId());
            
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupAGTSEvents()",
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
        Vector <LocationEvent> events = new Vector<LocationEvent>();
        try {
            while (rs.next()) {
                String eventName =  rs.getString(LocationEventName_Db.NAME_COL);
                Timestamp createdOn =  rs.getTimestamp(CREATED_ON_COL);
                LocationEvent event = new LocationEvent(xContext, eventName, 
                                                        "", createdOn);
                events.add(event);
            }
        	
        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), 
                                    "dbLookupAGTSEvents()",
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
    public void dbAddRow(EdaContext xContext, User_Db creator)
    throws IcofException{

    	// Get the next id for this new row.
    	setNextIdStatement(xContext);
    	long id = getNextBigIntId(xContext);
    	closeStatement(xContext);
    	
        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        Timestamp now = new Timestamp(new java.util.Date().getTime());
        try {
        	getStatement().setLong(1, id);
            if (getPlatform() != null) {
            	getStatement().setShort(2, getPlatform().getId());
                getStatement().setLong(3, getCompVerLocation().getId());
                getStatement().setLong(4, getEventName().getId());
                getStatement().setString(5, getComment());
                getStatement().setString(6, creator.getIntranetId());
                getStatement().setTimestamp(7, now);
            }
            else {
            	getStatement().setLong(2, getCompVerLocation().getId());
            	getStatement().setLong(3, getEventName().getId());
            	getStatement().setString(4, getComment());
            	getStatement().setString(5, creator.getIntranetId());
            	getStatement().setTimestamp(6, now);
            }
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

        setId(rs.getLong(ID_COL));
        short platId = rs.getShort(PLATFORM_ID_COL);
        if (platId > 0) {
        	setPlatform(xContext, rs.getShort(PLATFORM_ID_COL));
        }
        setCompVerLocation(xContext, rs.getShort(COMP_VER_LOCATION_ID_COL));
        setEventName(xContext, rs.getShort(LOCATION_EVENT_NAME_ID_COL));
        setComment(rs.getString(COMMENT_COL));
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
        buffer.append("Location: " + getCompVerLocation().getLocation().getName() + "\n");
        buffer.append("Component: " + getCompVerLocation().getComponentVersion().getCompRelease().getComponent().getName() + "\n");
        buffer.append("ToolKit: " + getCompVerLocation().getComponentVersion().getVersion().getDisplayName() + "\n");
        if (getPlatform() != null) {
        	buffer.append("Platform: " + getPlatform().getName() + "\n");
        }
        else {
        	buffer.append("Platform: NULL\n");
        }
        buffer.append("Event: " + getEventName().getName() + "\n");
        buffer.append("Comments: " + getComment() + "\n");
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

    
    /**
     * Determine the XMIT events for the given ToolKit and Component.
     * 
     * @param xContext     An application context object.
     * @param aRelVersion  RelVersion_Db object (ToolKit)
     * @param aComponent   Component_Db object
     * @return             Collection of event objects
     * @throws             Trouble querying the database.
     */
    public Vector<LocationEvent> dbLookupXmitEvents(EdaContext xContext, 
                                                    RelVersion_Db aRelVersion,
                                                    Component_Db aComponent)
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupXmitEventsStatement(xContext);
        
        try {
            getStatement().setShort(1, aRelVersion.getId());
            getStatement().setShort(2, aComponent.getId());
            
            //printQuery(xContext);
            //System.out.println("RelVersion: " + aRelVersion.getId());
            //System.out.println("Component : " + aComponent.getId());
            
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupXmitEvents()",
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
        Vector<LocationEvent> events = new Vector<LocationEvent>();
        try {
            while (rs.next()) {
                String eventName =  rs.getString(LocationEventName_Db.NAME_COL);
                Timestamp createdOn =  rs.getTimestamp(CREATED_ON_COL);
                LocationEvent event = new LocationEvent(xContext, eventName, 
                                                        "", createdOn);
                events.add(event);
            }
        	
        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), 
                                    "dbLookupXmitEvents()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }
        
        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return events;
        
    }


    /**
     * Determine the XTINCT events for the given ToolKit and Component.
     * 
     * @param xContext     An application context object.
     * @param aRelVersion  RelVersion_Db object (ToolKit)
     * @param aComponent   Component_Db object
     * @return             Collection of event objects
     * @throws             Trouble querying the database.
     */
    public Vector<LocationEvent> dbLookupXtinctEvents(EdaContext xContext, 
                                                      RelVersion_Db aRelVersion,
                                                      Component_Db aComponent)
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupXtinctEventsStatement(xContext);
        
        try {
            getStatement().setShort(1, aRelVersion.getId());
            getStatement().setShort(2, aComponent.getId());
            
            //printQuery(xContext);
            //System.out.println("RelVersion: " + aRelVersion.getId());
            //System.out.println("Component : " + aComponent.getId());
            
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupXtinctEvents()",
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
        Vector<LocationEvent> events = new Vector<LocationEvent>();
        try {
            while (rs.next()) {
                String eventName =  rs.getString(LocationEventName_Db.NAME_COL);
                Timestamp createdOn =  rs.getTimestamp(CREATED_ON_COL);
                LocationEvent event = new LocationEvent(xContext, eventName, 
                                                        "", createdOn);
                events.add(event);
            }
        	
        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), 
                                    "dbLookupXtinctEvents()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }
        
        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return events;
        
    }

    
}
