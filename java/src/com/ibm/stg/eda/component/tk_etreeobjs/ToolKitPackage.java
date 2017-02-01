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
 * Tool Kit Package business object.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/21/2013 GFS  Initial coding. 
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.ComponentPackage_Db;
import com.ibm.stg.eda.component.tk_etreedb.ComponentPackage_ToolKitPackage_Db;
import com.ibm.stg.eda.component.tk_etreedb.ToolKitPackage_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class ToolKitPackage {

    /**
     * Constructor
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    public ToolKitPackage(EdaContext xContext) throws IcofException {

    }


    /**
     * Constructor - takes a name
     * 
     * @param xContext Application context
     * @param aName A name (TK maintenance name)
     * @throws IcofException
     */
    public ToolKitPackage(EdaContext xContext, String aName)
    throws IcofException {

	setName(aName);
    }


    /**
     * Constructor - takes ID
     * 
     * @param xContext Application context
     * @param anId A ToolKit Package object id
     * @throws IcofException
     */
    public ToolKitPackage(EdaContext xContext, long anId) throws IcofException {

	dbLookupById(xContext, anId);
	populate(xContext);
    }


    /**
     * Constructor - takes a DB object
     * 
     * @param xContext Application context
     * @param aDbObject A ToolKit Package database object
     * @throws IcofException
     */
    public ToolKitPackage(EdaContext xContext, ToolKitPackage_Db aDbObject)
    throws IcofException {

	setDbObject(aDbObject);
	populate(xContext);
    }


    /**
     * Data Members
     * @formatter:off
     */
    private String name;
    private Events events;
    private ToolKitPackage_Db dbObject;

    
    /**
     * Getters
     */
    public String getName() { return name; }
    public Events getEvents() { return events; }
    public ToolKitPackage_Db getDbObject() { return dbObject; }

    
    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    private void setEvents(Events anEvents) { events = anEvents; }
    private void setDbObject(ToolKitPackage_Db anObject) { dbObject = anObject; }
    // @formatter:on


    /**
     * Lookup the object from the database id
     * 
     * @param xContext Application context.
     * @param anId A database id
     * @throws IcofException
     */
    public void dbLookupById(EdaContext xContext, long anId)
    throws IcofException {

	if (getDbObject() == null) {
	    try {
		dbObject = new ToolKitPackage_Db(anId);
		dbObject.dbLookupById(xContext);
		setName(dbObject.getName());
	    }
	    catch (IcofException trap) {
		dbObject = null;
		throw new IcofException(this.getClass().getName(),
					"dbLookupById()", IcofException.SEVERE,
					"Unable to find ToolKitPackage ("
					+ anId + ") in the database.\n",
					trap.getMessage());
	    }
	}
    }


    /**
     * Lookup the object from the database id
     * 
     * @param xContext Application context.
     * @throws IcofException
     */
    public void dbLookupById(EdaContext xContext)
    throws IcofException {

	dbLookupById(xContext, getDbObject().getId());
    }


    /**
     * Add this object to the database
     * 
     * @param xContext Application object
     * @param newEvents An Events object to associate Event changes with
     * @throws IcofException
     */
    public void dbAdd(EdaContext xContext, Events newEvents, User_Db user)
    throws IcofException {

	// If events is null create a new one
	if (newEvents == null) {
	    newEvents = new Events(xContext, null);
	    newEvents.dbAdd(xContext);
	}
	
	// Add the new object
	setEvents(newEvents);
	setDbObject(xContext, true);
	getDbObject().dbAddRow(xContext);
	
	// Set the state to pkg_new
	EventName newEvent = new EventName(xContext, "PKG_NEW");
	newEvent.dbLookupByName(xContext);
	
	Event event = new Event(xContext, newEvent, "", "", null, null);
	event.dbAdd(xContext, newEvents.getDbObject().getId(), user);
	

    }


    /**
     * Populate the database object and look it up in the DB
     * 
     * @param xContext
     * @throws IcofException
     */
    private void setDbObject(EdaContext xContext, boolean bIsNew)
    throws IcofException {

	if (getDbObject() != null)
	    dbObject = null;

	dbObject = new ToolKitPackage_Db(getName(), getEvents().getDbObject());
	if (! bIsNew)
	    getDbObject().dbLookupByAll(xContext);

    }


    /**
     * Update this object in the database
     * 
     * @param xContext Application object
     * @param newMaintName New TK maintenance name
     * @param newEvents New Events collection
     * @throws IcofException
     */
    public void dbUpdate(EdaContext xContext, String newMaintName,
			 Events newEvents)
    throws IcofException {

	try {
	    // Lookup the object in the database first.
	    setDbObject(xContext, false);
	}
	catch (IcofException trap) {
	    throw new IcofException(this.getClass().getName(), "dbUpdate()",
				    IcofException.SEVERE,
				    "Unable to find existing object ("
				    + getName() + ") in the database.\n",
				    trap.getMessage());
	}

	// Update the object
	getDbObject().dbUpdateRow(xContext, newMaintName,
				  newEvents.getDbObject());

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
	buffer.append("Tool Kit Package\n---------------\n");
	buffer.append("Name     : " + getName() + "\n");
	buffer.append("Events ID: " + getEvents().getId() + "\n");

	return buffer.toString();

    }


    /**
     * Populate the members from the database object
     * 
     * @throws IcofException
     */
    private void populate(EdaContext xContext)
    throws IcofException {

	setName(getDbObject().getName());

	if (getEvents() != null) {
	    events = null;
	}
	events = new Events(xContext, getDbObject().getEvents());

    }


    /**
     * Lookup ComponentUpdates associated with this object
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    public Vector<ComponentPackage> getComponentPackages(EdaContext xContext)
    throws IcofException {

	// Create a collection of available component packages
	Vector<ComponentPackage> allPkgs = new Vector<ComponentPackage>();

	ComponentPackage_ToolKitPackage_Db cptkp;
	cptkp = new ComponentPackage_ToolKitPackage_Db(xContext, 0,
	                                               getDbObject().getId());
	Vector<ComponentPackage_Db> compPkgs;
	compPkgs = cptkp.dbLookupCompPkgs(xContext);

	for (ComponentPackage_Db dbPkg : compPkgs) {
	    ComponentPackage pkg = new ComponentPackage(xContext, dbPkg);
	    allPkgs.add(pkg);
	}

	return allPkgs;

    }


    /**
     * Lookup ToolKitPackage which are in the given EventName state
     * 
     * @param xContext Application context.
     * @param anEvent EventName to find ARs for
     * @throws IcofException
     */
    public Vector<ToolKitPackage> dbLookupByEventName(EdaContext xContext,
						      EventName anEvent)
    throws IcofException {

	// Lookup requests
	if (getDbObject() == null) {
	    dbObject = new ToolKitPackage_Db("", null);
	}
	Vector<ToolKitPackage_Db> dbPkgs;
	dbPkgs = dbObject.dbLookupByEventName(xContext,
	                                      anEvent.getDbObject().getId());

	// Convert database objects to business objects
	Vector<ToolKitPackage> packages = new Vector<ToolKitPackage>();
	for (ToolKitPackage_Db dbPkg : dbPkgs) {
	    ToolKitPackage pkg = new ToolKitPackage(xContext, dbPkg);
	    packages.add(pkg);
	}

	return packages;

    }
    

    /**
     * Lookup latest Tool Kit Package for this Tool Kit
     * 
     * @param xContext Application context.
     * @param aTk      Tool Kit object
     * @throws IcofException
     */
    public boolean dbLookupLatest(EdaContext xContext, ToolKit aTk)
    throws IcofException {

	boolean bFound = false;
	
	// Look up the latest Tool Kit Maintenance for this tk
	if (getDbObject() == null) {
	    dbObject = new ToolKitPackage_Db((long)0);
	}
	
	Vector<ToolKitPackage_Db> tkPkgs;
	tkPkgs = getDbObject().dbLookupByToolKit(xContext, aTk.getToolKit());
	
	if (! tkPkgs.isEmpty()) {
	    setDbObject((ToolKitPackage_Db) tkPkgs.firstElement());
	    setName(getDbObject().getName());
	    bFound = true;
	}
	
	return bFound;
	
    }

    
    /**
     * Lookup latest Tool Kit Package for this Tool Kit and state
     * 
     * @param xContext Application context.
     * @param aTk      Tool Kit object
     * @throws IcofException
     */
    public boolean dbLookupLatestInState(EdaContext xContext, ToolKit aTk, 
                                         EventName aState)
    throws IcofException {

	boolean bFound = false;
	
	// Look up the latest Tool Kit Maintenance for this tk
	if (getDbObject() == null) {
	    dbObject = new ToolKitPackage_Db((long)0);
	}
	
	Vector<ToolKitPackage_Db> tkPkgs;
	tkPkgs = getDbObject().dbLookupByTkAndState(xContext, aTk.getToolKit(), 
	                                            aState.getDbObject());
	
//	System.out.println("Found " + tkPkgs.size() + " TK pkgs in " +
//		           aState.getName() + " state");
	
	if (! tkPkgs.isEmpty()) {
	    setDbObject((ToolKitPackage_Db) tkPkgs.firstElement());
	    setName(getDbObject().getName());
	    events = new Events(xContext, getDbObject().getEvents());
	    bFound = true;
	}
	
	return bFound;
	
    }
    

    /**
     * Associate the specified component package with this tool kit
     * package
     *
     * @param xContext   Application context
     * @param newPackage New component package object
     * @throws IcofException 
     */
    public void addComponentPackage(EdaContext xContext,
				    ComponentPackage newPackage) 
				    throws IcofException {

	ComponentPackage_ToolKitPackage_Db cptp;
	cptp = new ComponentPackage_ToolKitPackage_Db(newPackage.getDbObject(),
	                                              getDbObject());
	
	// Add the component package it it doesn't already exist
	try {
	    cptp.dbLookupByIds(xContext);
	}
	catch(IcofException trap) {
	    cptp.dbAddRow(xContext);
	}
	
    }


    /**
     * Log an event (ie, update the state of) this tool kit package
     *
     * @param xContext  Application context
     * @param newState  Event to log
     * @param comments  Event comments
     * @param user      User making the update
     * @throws IcofException 
     */
    public void updateState(EdaContext xContext, EventName newState,
                            String comments,  User_Db user) throws IcofException {

	// Mark the current event as expired
	Event lastEvent = new Event(xContext, null, "", "" , null, null);
	lastEvent.dbUpdateExpiredTms(xContext, getEvents());

	// Create a new event record
	Event newEvent = new Event(xContext, newState, comments, 
	                           user.getIntranetId(), null, null);
	newEvent.dbAdd(xContext, getEvents().getId(), user);
	
    }

    
    /**
     * Look up tool kit packages for a given tool kit
     *
     * @param xContext  Application context
     * @param toolKit   
     * @throws IcofException 
     */
    public Vector<ToolKitPackage_Db> dbLookupByTk(EdaContext xContext, 
                                                  ToolKit aTk) 
    throws IcofException {

	if (getDbObject() == null) {
	    dbObject = new ToolKitPackage_Db((long)0);
	}
	
	Vector<ToolKitPackage_Db> pkgs = getDbObject().dbLookupByToolKit(xContext, 
	                                                                 aTk.getToolKit());
	
	return pkgs;
	
    }

    
    /**
     * Look up this package by tool kit and maint name
     *
     * @param xContext  Application context
     * @param toolKit   
     * @throws IcofException 
     */
    public void dbLookupByName(EdaContext xContext, ToolKit aTk) 
    throws IcofException {

	if (getDbObject() == null) {
	    dbObject = new ToolKitPackage_Db((long)0);
	}
	
	getDbObject().dbLookupByToolKit(xContext, aTk.getToolKit(), getName());
	
	events = new Events(xContext, getDbObject().getEvents().getId());
	events.dbLookupById(xContext, getDbObject().getEvents().getId());
    }


}
