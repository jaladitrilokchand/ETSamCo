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
* Event business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 07/30/2013 GFS  Initial coding 
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.sql.Timestamp;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.Event_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class Event  {

    /**
     * Constructor - takes an id
     * 
     * @param xContext  Application context
     * @param anId      Event id
     * @throws IcofException 
     */
    public Event(EdaContext xContext, long anId) 
    throws IcofException {
        setId(anId);
    }

    
	/**
     * Constructor - takes a db object
     * 
     * @param xContext  Application context
     * @param aDbObject A database object 
     * @throws IcofException 
     */
    public Event(EdaContext xContext, Event_Db aDbObject) 
    throws IcofException {
        setDbObject(aDbObject);
        populate(xContext);
    }

    
	/**
     * Constructor - takes a name
     * 
     * @param xContext  Application context
     * @param aName     A name
     * @throws IcofException 
     */
    public Event(EdaContext xContext, EventName name, String comment, 
                 String aCreatedBy, Timestamp aCreatedOn, Timestamp anExpiredOn) 
    throws IcofException {
        setEventName(name);
        setComments(comment);
        setCreatedBy(aCreatedBy);
        setCreatedOn(aCreatedOn);
        setExpiredOn(anExpiredOn);
    }

    
    /**
     * Data Members
     */
    private long id;
    private EventName eventName;
    private String comments;
    private String createdBy;
    private Timestamp createdOn;
    private Event_Db dbObject;
    private Timestamp expiredOn;
    
    
    /**
     * Getters
     */
    public long getId() { return id; }
    public EventName getEventName() { return eventName; }
    public String getComments() { return comments; }
    public String getCreatedBy() { return createdBy; }
    public Timestamp getCreatedOn() { return createdOn; }
    public Event_Db getDbObject() { return dbObject; }
    public Timestamp getExpiredOn() { return expiredOn; }

    
    /**
     * Setters
     */
    private void setId(long anId)  { id = anId; }
    private void setEventName(EventName aName) { eventName = aName; }
    private void setComments(String aComment) { comments = aComment; }
    private void setCreatedBy(String aName) { createdBy = aName; }
    private void setCreatedOn(Timestamp aTms) { createdOn = aTms; }   
    private void setDbObject(Event_Db aDbObject) { dbObject = aDbObject; }
    private void setExpiredOn(Timestamp aTms) { expiredOn = aTms; }

    
    /**
     * Add this object to the database
     * 
     * @param xContext  Application object
     * @param eventsId  Events id
     * @param user      Person adding this row
     * @throws IcofException 
     * @return True if object created false object existing object.
     */
    public boolean dbAdd(EdaContext xContext, long eventsId, User_Db user) 
    throws IcofException {

    	setDbObject(xContext, eventsId);
    	getDbObject().dbAddRow(xContext, user);
    	
    	return true;

    }

    
    /**
     * Loads the DB object 
     * 
     * @param xContext    Application context
     * @param eventsId    Events id 
     */
    private void setDbObject(EdaContext xContext, long eventsId) {
    	dbObject = new Event_Db(getEventName().getDbObject().getId(), 
    	                        eventsId, 
    	                        getComments(), getCreatedBy(), 
    	                        getCreatedOn(), getExpiredOn());
    }

    
	/**
     * Update this object's expired timestamp
     * 
     * @param xContext    Application object
     * @param events      Events id to update
     * @throws IcofException 
     */
    public void dbUpdateExpiredTms(EdaContext xContext, Events events)
    throws IcofException {
    	
    	// Update the object
    	if (getDbObject() == null)
    		dbObject = new Event_Db(0);
    	getDbObject().dbUpdateExpiredTms(xContext, events.getId());
    	
    }
    
    
	/**
     * Update this object in the database
     * 
     * @param xContext    Application object
     * @param id          Event id to update
     * @param newEvent    New Event object (contains new data)
     * @throws IcofException 
     */
    public void dbUpdate(EdaContext xContext, long id, Event newEvent)
    throws IcofException {
    	
    	try {
    		// Lookup the object in the database first.
    		dbLookupById(xContext, id);
    	}
    	catch(IcofException trap) {
    		throw new IcofException(this.getClass().getName(), "dbUpdate()",
	                                IcofException.SEVERE,
	                                "Unable to find existing object (" +
	                                 id + ") in the database.\n",
	                                 trap.getMessage());
    	}

    	// Update the object
    	getDbObject().dbUpdateRow(xContext, 
    	                          newEvent.getEventName().getDbObject().getId(),
    	                          id, newEvent.getComments());
    	
    }

    
    /**
     * Delete this object from the database
     * 
     * @param xContext    Application object
     * @param id          Event id to delete
     * @throws IcofException 
     */
    public void dbDelete(EdaContext xContext, long id) throws IcofException {
    	
    	try {
    		// Lookup the object in the database first.
    		dbLookupById(xContext, id);
    	}
    	catch(IcofException trap) {
    		throw new IcofException(this.getClass().getName(), "dbDelete()",
	                                IcofException.SEVERE,
	                                "Unable to find existing object (" +
	                                 id + ") in the database.\n",
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
        		dbObject = new Event_Db(id);
        		dbObject.dbLookupById(xContext);
        		populate(xContext);
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
        buffer.append("Event name: " + getEventName().getName() + "\n");
        buffer.append("Comments  : " + getComments() + "\n");
        buffer.append("Created by: " + getCreatedBy() + "\n");
        buffer.append("Created on: " + getCreatedOn() + "\n");
        if (getExpiredOn() != null) {
        	buffer.append("Expired on: " + getExpiredOn() + "\n");
        }
        else {
        	buffer.append("Expired on: NULL\n");
        }
        
        return buffer.toString();

    }
  
    
    /**
     * Populate this object from the database object
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void populate(EdaContext xContext) throws IcofException {
		
		setComments(getDbObject().getComments());
		setCreatedBy(getDbObject().getCreatedBy());
		setCreatedOn(getDbObject().getCreatedOn());
		eventName = new EventName(xContext, getDbObject().getEventNameId());
		getEventName().dbLookupById(xContext, getDbObject().getEventNameId());
    	
	}

    
}
