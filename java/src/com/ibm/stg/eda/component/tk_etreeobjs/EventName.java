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
 * EventName business object.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 07/29/2013 GFS  Initial coding. 
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.util.ArrayList;
import java.util.List;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.EventName_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class EventName {


    /**
     * Constructor - takes a Event name
     * 
     * @param xContext Application context
     * @param aName A Event name (trunk ...)
     * @throws IcofException
     */
    public EventName(EdaContext xContext, String aName) throws IcofException {

	setName(aName.toUpperCase());
	dbObject = new EventName_Db(aName);
	try {
	    dbObject.dbLookupByName(xContext);
	}
	catch(IcofException ignore) {}
    }


    /**
     * Constructor - takes objects
     * 
     * @param xContext Application context
     * @param aComp A EventName_Db object
     */
    public EventName(EdaContext xContext, EventName_Db aEventName) {

	setDbObject(aEventName);
	setName(getDbObject().getName());
    }


    /**
     * Constructor - takes IDs
     * 
     * @param xContext Application context
     * @param anId A EventName object id
     * @throws IcofException
     */
    public EventName(EdaContext xContext, long anId) throws IcofException {

	dbLookupById(xContext, anId);
	setName(getDbObject().getName());

    }


    /**
     * Data Members
     * @formatter:off
     */
    private String name;
    private EventName_Db dbObject;

    
    /**
     * Getters
     */
    public String getName() { return name; }
    public EventName_Db getDbObject() { return dbObject; }

    
    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    private void setDbObject(EventName_Db aEventName) { dbObject = aEventName; }
    // @formatter:on


    /**
     * Lookup the EventName object from the database id
     * 
     * @param xContext Application context.
     * @param anId A Component id
     * @throws IcofException
     */
    public void dbLookupById(EdaContext xContext, long anId)
    throws IcofException {

	if (getDbObject() == null) {
	    try {
		dbObject = new EventName_Db(anId);
		dbObject.dbLookupById(xContext);
		populate(xContext);
	    }
	    catch (IcofException trap) {
		dbObject = null;
		throw new IcofException(this.getClass().getName(),
					"dbLookupById()", IcofException.SEVERE,
					"Unable to find EventName (" + anId
					+ ") in the database.\n",
					trap.getMessage());
	    }
	}
    }


    /**
     * Lookup the EventName object from the name
     * 
     * @param xContext Application context.
     * @throws IcofException
     */
    public void dbLookupByName(EdaContext xContext)
    throws IcofException {

	try {
	    dbObject = new EventName_Db(getName());
	    dbObject.dbLookupByName(xContext);
	    populate(xContext);
	}
	catch (IcofException trap) {
	    throw new IcofException(this.getClass().getName(),
	                            "dbLookupByName()",
	                            IcofException.SEVERE,
	                            "Unable to find EventName ("
	                            + getName() + ") in the database.\n",
	                            trap.getMessage());
	}
    }


    /**
     * Add this object to the database
     * 
     * @param xContext Application object
     * @throws IcofException
     * @return True if object created false object existing object.
     */
    public boolean dbAdd(EdaContext xContext)
    throws IcofException {

	try {
	    // Lookup the object in the database first.
	    dbLookupByName(xContext);
	    return false;
	}
	catch (IcofException trap) {
	    // Add the new object
	    getDbObject().dbAddRow(xContext);
	    populate(xContext);
	}
	return true;

    }


    /**
     * Update this object in the database
     * 
     * @param xContext Application object
     * @param newName New Event name
     * @throws IcofException
     */
    public void dbUpdate(EdaContext xContext, String newName)
    throws IcofException {

	try {
	    // Lookup the object in the database first.
	    dbLookupByName(xContext);
	}
	catch (IcofException trap) {
	    throw new IcofException(this.getClass().getName(), "dbUpdate()",
				    IcofException.SEVERE,
				    "Unable to find existing object ("
				    + getName() + ") in the database.\n",
				    trap.getMessage());
	}

	// Update the object
	getDbObject().dbUpdateRow(xContext, newName);
	populate(xContext);

    }


    /**
     * Delete this object from the database
     * 
     * @param xContext Application object
     * @throws IcofException
     */
    public void dbDelete(EdaContext xContext)
    throws IcofException {

	getDbObject().dbDeleteRow(xContext);
    }


    /**
     * Create a key from the ID.
     * 
     * @param xContext Application context object.
     * @return A Statement object.
     */
    public String getIdKey(EdaContext xContext) {

	return String.valueOf(getDbObject().getId());
    }


    /**
     * Display this object as a string
     * 
     * @param xContext Application context
     * @return This object as a string.
     */
    public String toString(EdaContext xContext) {

	// Get the class specific data
	StringBuffer buffer = new StringBuffer();
	buffer.append("EventName object\n---------------\n");
	buffer.append("Name: " + getName() + "\n");

	return buffer.toString();

    }


    /**
     * Populate this object from the database object
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void populate(EdaContext xContext)
    throws IcofException {

	setName(getDbObject().getName());

    }


    /**
     * Create a collection of all Event Names or those containing the filter
     * if filter is not null or empty
     *
     *
     * @param xContext Application context
     * @param sFilter  Name filter text
     * 
     * @return List of EventName objects
     * @throws IcofException 
     */
    public List<EventName> getEventNames(EdaContext xContext, String sFilter) 
    throws IcofException {
	
	if (getDbObject() == null) {
	    dbObject = new EventName_Db("");
	}
	
	List<EventName_Db> dbObjects = getDbObject().dbLookupNames(xContext, 
	                                                           sFilter);
	List<EventName> objects = new ArrayList<EventName>();
	
	for(EventName_Db event : dbObjects) {
	    EventName myEvent = new EventName(xContext, event);
	    objects.add(myEvent);
	}
	
	return objects;
	
    }
    
}
