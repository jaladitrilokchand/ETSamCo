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
* AccessRequest business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 08/08/2013 GFS  Initial coding. 
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.AccessRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.AccessType_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Release_Db;
import com.ibm.stg.eda.component.tk_etreedb.Events_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class AccessRequest  {
	
	/**
     * Constructor - takes objects
     * 
     * @param xContext  Application context
     * @param aComp     A EventName_Db object
     */
    public AccessRequest(EdaContext xContext, User_Db aUser, 
                         Component_Release_Db aCompRel, 
                         AccessType aType, String aRequester) {
        setUser(aUser);
        setCompRelease(aCompRel);
        setAccessType(aType);
        setRequester(aRequester);
    }

    
	/**
     * Constructor - takes objects
     * 
     * @param xContext  Application context
     * @param aComp     A EventName_Db object
     */
    public AccessRequest(EdaContext xContext, User_Db aUser, 
                         Component_Release_Db aCompRel, 
                         AccessType aType, Events anEvents, 
                         String aRequester) {
        setUser(aUser);
        setCompRelease(aCompRel);
        setAccessType(aType);
        setEvents(anEvents);
        setRequester(aRequester);
    }

    
    /**
     * Constructor - takes IDs
     * 
     * @param xContext    Application context
     * @param anId        A EventName object id
     * @throws IcofException 
     */
    public AccessRequest(EdaContext xContext, long anId) 
    throws IcofException {
        dbLookupById(xContext, anId);
        setRequester(getDbObject().getRequester());
    }

    /**
     * Constructor - takes a db object
     * 
     * @param xContext    Application context
     * @param anObject    A AccessRequest_Db object
     * @throws IcofException 
     */
    public AccessRequest(EdaContext xContext, AccessRequest_Db anObject) 
    throws IcofException {
        setDbObject(anObject);
        populate(xContext);
    }
    
    
    /**
     * Data Members
     */
    User_Db user;
    Component_Release_Db compRelease;
    AccessType type;
    Events events;
    private String requester;
    private AccessRequest_Db dbObject;

    
    /**
     * Getters
     */
    public User_Db getUser() { return user; }
    public Component_Release_Db getCompRelease() { return compRelease; };
    public AccessType getAccessType() { return type; }
    public Events getEvents() { return events; }
    public String getRequester() { return requester; }
    public AccessRequest_Db getDbObject() { return dbObject; }

    
    /**
     * Setters
     */
    private void setUser(User_Db aUser) { user = aUser; }
    private void setCompRelease(Component_Release_Db aCompRel) { compRelease = aCompRel; }
    private void setAccessType(AccessType aType) { type = aType; }
    private void setEvents(Events anEvents) { events = anEvents; }
    private void setRequester(String aName) { requester = aName; }
    private void setDbObject(AccessRequest_Db aRequest) { dbObject = aRequest; }

    
    /**
     * Lookup the EventName object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A Component id
     * @throws IcofException 
     */
    public void dbLookupById(EdaContext xContext, long anId) 
    throws IcofException { 
        if (getDbObject() == null) {
        	try {
        		dbObject = new AccessRequest_Db(anId);
        		dbObject.dbLookupById(xContext);
        		populate(xContext);
        	}
        	catch(IcofException trap) {
        		dbObject = null;
        		throw new IcofException(this.getClass().getName(), "dbLookupById()",
        				IcofException.SEVERE,
                        "Unable to find AccessRequest (" + anId + ") in the database.\n",
                        trap.getMessage());
        	}
        }            
    }

    /**
     * Lookup Access Requests which are in the given EventName state
     * 
     * @param xContext   Application context.
     * @param anEvent    EventName to find ARs for
     * @throws IcofException 
     */
    public Vector<AccessRequest> dbLookupByEventName(EdaContext xContext, EventName anEvent) 
    throws IcofException { 

    	// Lookup requests 
    	if (getDbObject() == null) {
    		dbObject = new AccessRequest_Db(null, null, null, null, "");
    	}
    	Vector<AccessRequest_Db> dbRequests = 
    		dbObject.dbLookupByEventName(xContext, anEvent.getDbObject().getId());            
    	
    	// Convert AccessRequest_Db objects to AccessRequest objects
    	Vector<AccessRequest> requests = new Vector<AccessRequest>();
    	Iterator<AccessRequest_Db> iter = dbRequests.iterator();
    	while (iter.hasNext()) {
    		AccessRequest_Db dbRequest = (AccessRequest_Db) iter.next();
    		AccessRequest request = new AccessRequest(xContext, dbRequest);
    		requests.add(request);
    	}
    	
    	return requests;
    	
    }

    
    /**
     * Add this object to the database
     * 
     * @param xContext  Application object
     * @throws IcofException 
     * @return True if object created false object existing object.
     */
    public boolean dbAdd(EdaContext xContext) throws IcofException {

	try {
	    // Lookup the object in the database first.
	    dbLookup(xContext);
	    return false;
	}
	catch(IcofException trap) {
	    // Add the new object
	    getDbObject().dbAddRow(xContext);
	    populate(xContext);	
	}
	return true;

    }


    /**
     * Update this object in the database
     * 
     * @param xContext    Application object
     * @param newName     New Event name
     * @throws IcofException 
     */
    public void dbUpdate(EdaContext xContext, User_Db newUser,
                         Component_Release_Db newCompRel, AccessType_Db newAccessType,
                         Events_Db newEvents, String newRequester)
    throws IcofException {
    	
    	try {
    		// Lookup the object in the database first.
    		dbLookup(xContext);
    	}
    	catch(IcofException trap) {
    		throw new IcofException(this.getClass().getName(), "dbUpdate()",
	                                IcofException.SEVERE,
	                                "Unable to find existing object in " +
	                                "the database.\n",
	                                 trap.getMessage());
    	}

    	// Update the object
    	getDbObject().dbUpdateRow(xContext, newUser, newCompRel, 
    	                          newAccessType, newEvents, newRequester);
    	populate(xContext);
    	
    }

    
    /**
     * Delete this object from the database
     * 
     * @param xContext    Application object
     * @throws IcofException 
     */
    public void dbDelete(EdaContext xContext) throws IcofException {
    	getDbObject().dbDeleteRow(xContext);
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
        buffer.append("AccessRequest object\n---------------\n");
        buffer.append("User       : " + getUser().getIntranetId() + "\n");
        buffer.append("CompRel id : " + getCompRelease().getId() + "\n");
        buffer.append("Access Type: " + getAccessType().getName() + "\n");
        buffer.append("Events id  : " + getEvents().getId() + "\n");
        
        return buffer.toString();

    }

    
    /**
     * Populate this object from the database object
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void populate(EdaContext xContext) throws IcofException {
    	
    	// Set the requester
    	setRequester(getDbObject().getRequester());
    	
    	// Set the access type
    	if (getAccessType() != null)
    		type = null;
    	AccessType_Db dbType = getDbObject().getAccessType();
    	dbType.dbLookupById(xContext);
    	type = new AccessType(xContext, dbType);
    
    	// Set the user
    	if (getUser() != null)
    		user = null;
    	user = getDbObject().getUser();
    	user.dbLookupById(xContext);
    
    	// Set the component release
    	if (getCompRelease() != null)
    		compRelease = null;
    	compRelease = getDbObject().getCompRelease();
    	
    	// Set the events object
    	if (getEvents() != null)
    		events = null;
    	events = new Events(xContext, getDbObject().getEvents());
    	
    }
    
    
    /**
     * Lookup this object in the database
     * 
     * @param xContext  Application object
     * @throws IcofException if object not found
     */
    private boolean dbLookup(EdaContext xContext) throws IcofException {

    	try {
    		setDbObject();
    		getDbObject().dbLookupByAll(xContext);
    	}
    	catch(IcofException notFound) {
    		return false;
    	}
    	return true;

    }


    /**
     * Set the DbObject to this object members
     */
	private void setDbObject() {
		if (getDbObject() == null) {
			dbObject = new AccessRequest_Db(getUser(), 
			                                getCompRelease(), 
			                                getAccessType().getDbObject(), 
			                                getEvents().getDbObject(), 
			                                getRequester());
		}
		
	}
    
	/**
	 * Update the state of this request.
	 * Mark the previous event as expired and add a new event
	 * 
	 * @param newState  The new event state
	 * @throws IcofException 
	 */
	public void updateState(EdaContext xContext, EventName newState, 
	                        User_Db creator) 
	throws IcofException {
		
		// Mark the current event as expired
		Event lastEvent = new Event(xContext, null, "", "" , null, null);
		lastEvent.dbUpdateExpiredTms(xContext, getEvents());
						
		// Create a new event record
		Event newEvent = new Event(xContext, newState, "", 
		                           creator.getIntranetId(), null, null);
		newEvent.dbAdd(xContext, getEvents().getId(), creator);
		
	}
	
}
