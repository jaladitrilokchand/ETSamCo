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
* Events business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 07/30/2013 GFS  Initial coding 
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.Event_Db;
import com.ibm.stg.eda.component.tk_etreedb.Events_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class Events  {

    /**
     * Constructor - takes an id
     * 
     * @param xContext  Application context
     * @param anId      Events id
     * @throws IcofException 
     */
    public Events(EdaContext xContext, long id) 
    throws IcofException {
        setId(id);
    }

    
    /**
     * Constructor - takes a DB object
     * 
     * @param xContext  Application context
     * @param aDbObject Events_Db object
     * @throws IcofException 
     */
    public Events(EdaContext xContext, Events_Db aDbObject) 
    throws IcofException {
        setDbObject(aDbObject);
        if (getDbObject() != null)
            setId(getDbObject().getId());
    }
    
    
    /**
     * Data Members
     */
    private long id;
    private Events_Db dbObject;
    private Vector<Event> events;
    
    /**
     * Getters
     */
    public long getId() { return id; }
    public Events_Db getDbObject() { return dbObject; }
    private Vector<Event> getEvents() { return events; }
    
    
    /**
     * Setters
     */
    private void setId(long anId)  { id = anId; }
    private void setDbObject(Events_Db anObj) { dbObject = anObj; }

    
    /**
     * Add an event
     * 
     * @param xContext  Application object
     * @param anId      Events id to add event to
     * @param anEvent   Event object to add
     * @param user      Person logging the event
     * @throws IcofException 
     */
    public void addEvent(EdaContext xContext, 
                         long anId, Event event, User_Db user) 
    throws IcofException {

	// Add the Events object (unless already in DB)
	try {
	    dbLookupById(xContext, anId);
	}
	catch(IcofException trap) {
	    dbAdd(xContext);
	}

	// Add the Event
	event.dbAdd(xContext, anId, user);

    }
    

    /**
     * Query the DB for events for this Event collection
     * 
     * @param xContext  Application object
     * @param anId      Events id
     * @throws IcofException 
     */
    public void readEvents(EdaContext xContext, long anId) 
    throws IcofException {

	// Clear the collection first
	if (getEvents() != null)
	    getEvents().clear();
	else 
	    events = new Vector<Event>();

	// Run the DB query 
	Event_Db dbEvent = new Event_Db((long)0);
	Vector<Event_Db> dbEvents = dbEvent.dbLookupByEventsId(xContext, anId);

	// Build the collection
	Iterator<Event_Db> iter = dbEvents.iterator();
	while (iter.hasNext()) {
	    Event_Db myDbEvent = (Event_Db)iter.next();
	    Event myEvent = new Event(xContext, myDbEvent);
	    getEvents().add(myEvent);
	}

    }

    
    /**
     * Query the DB for last Event of this Event collection
     * 
     * @param xContext  Application object
     * @param anId      Events id
     * @throws IcofException 
     */
    public Event getLastEvent(EdaContext xContext, long anId) 
    throws IcofException {

    	// Read the events first
    	if (getEvents().isEmpty()) {
    	    readEvents(xContext, anId);
    	}

    	// Return the latest event
    	return (Event)getEvents().lastElement();
    	
    }
    
    
    /**
     * Query the DB for last Event of this Event collection
     * 
     * @param xContext  Application object
     * @throws IcofException 
     */
    public Event getLastEvent(EdaContext xContext) throws IcofException {

	// Read the events first
	if ((getEvents() == null) || getEvents().isEmpty()) {
	    readEvents(xContext, getId());
	}

	// Return the latest event
	return (Event)getEvents().lastElement();
    	
    }
    
    /**
     * Add this object to the database
     * 
     * @param xContext  Application object
     * @param anId      Events id
     * @throws IcofException 
     * @return True if object created false object existing object.
     */
    public boolean dbAdd(EdaContext xContext) 
    throws IcofException {

	setDbObject(xContext, 0);
    	getDbObject().dbAddRow(xContext);
    	
    	return true;

    }

    
    /**
     * Loads the DB object
     * 
     * @param xContext Application context
     * @param anId     Events id 
     */
    private void setDbObject(EdaContext xContext, long anId) {
    	dbObject = new Events_Db(anId);
    }


    /**
     * Delete this object from the database
     * 
     * @param xContext    Application object
     * @param anId        Events id to delete
     * @throws IcofException 
     */
    public void dbDelete(EdaContext xContext, long anId) throws IcofException {
    	
    	try {
    		// Lookup the object in the database first.
    		dbLookupById(xContext, anId);
    	}
    	catch(IcofException trap) {
    		throw new IcofException(this.getClass().getName(), "dbDelete()",
	                                IcofException.SEVERE,
	                                "Unable to find existing object (" +
	                                 anId + ") in the database.\n",
	                                 trap.getMessage());
    	}
    	
    	getDbObject().dbDeleteRow(xContext);
    	
    }
    
    
    /**
     * Lookup the Event object from the id
     * 
     * @param xContext   Application context.
     * @throws IcofException 
     */
    public void dbLookupById(EdaContext xContext, long id) 
    throws IcofException {
    	if (getDbObject() == null) {
        	try {
        		dbObject = new Events_Db(id);
        		dbObject.dbLookupById(xContext);
        	}
        	catch(IcofException trap) {
        		throw new IcofException(this.getClass().getName(), "dbLookupById()",
        				IcofException.SEVERE,
                        "Unable to find Event (" + id + ") in the database.\n",
                        trap.getMessage());
        	}
        }
    }
    

    
    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
        return String.valueOf(getDbObject().getId());
    }
 
    
    /**
     * Display this object as a string
     * @param xContext  Application context
     * @return    This object as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID: " + getId() + "\n");
        
        return buffer.toString();

    }
    
    
}
