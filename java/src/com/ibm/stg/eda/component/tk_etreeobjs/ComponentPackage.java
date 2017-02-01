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
 * Component Package business object.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/22/2013 GFS  Initial coding. 
 * 03/18/2014 GFS  Enhanced getCompPackages() to include sorting
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.ComponentPackage_ComponentTkVersion_Db;
import com.ibm.stg.eda.component.tk_etreedb.ComponentPackage_Db;
import com.ibm.stg.eda.component.tk_etreedb.ComponentPackage_ToolKitPackage_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.Deliverable_Db;
import com.ibm.stg.eda.component.tk_etreedb.Event_Db;
import com.ibm.stg.eda.component.tk_etreedb.Platform_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class ComponentPackage {

    /**
     * Constructor - takes a name
     * 
     * @param xContext Application context
     * @param aName A name (TK maintenance name)
     * @param anEvents An Events collection object
     * @throws IcofException
     */
    public ComponentPackage(EdaContext xContext, String aName,
			    Platform_Db aPlatform, int aMaintLevel,
			    int aPatchLevel, Events anEvents)
    throws IcofException {

	setName(aName);
	setPlatform(aPlatform);
	setMaintLevel(aMaintLevel);
	setPatchLevel(aPatchLevel);
	setEvents(anEvents);
    }


    /**
     * Constructor - takes ID
     * 
     * @param xContext Application context
     * @param anId A Component Package object id
     * @throws IcofException
     */
    public ComponentPackage(EdaContext xContext, long anId)
    throws IcofException {

	try {
	    dbLookupById(xContext, anId);
	}
	catch(IcofException ignore) { 
	    return; // Don't populate if not found .. for new packages
	}
	populate(xContext);
	
    }
    
    /**
     * Constructor - takes name
     * 
     * @param xContext Application context
     * @param anId     A Component Package name
     * @throws IcofException
     */
    public ComponentPackage(EdaContext xContext, String aName)
    throws IcofException {

	try {
	    setName(aName);
	    dbLookupByName(xContext);
	}
	catch(IcofException ignore) { 
	    return; // Don't populate if not found .. for new packages
	}
	populate(xContext);
	
    }


    /**
     * Constructor - takes a DB object
     * 
     * @param xContext Application context
     * @param aDbObject A Component Package database object
     * @throws IcofException
     */
    public ComponentPackage(EdaContext xContext, ComponentPackage_Db aDbObject)
    throws IcofException {

	setDbObject(aDbObject);
	populate(xContext);
    }


    /**
     * Data Members
     * @formatter:off
     */
    private String name;
    private Platform_Db platform;
    private int maintLevel;
    private int patchLevel;
    private Events events;
    private ComponentPackage_Db dbObject;

    
    /**
     * Getters
     */
    public String getName() { return name; }
    public Platform_Db getPlatform() { return platform; }
    public int getMaintLevel() { return maintLevel; }
    public int getPatchLevel() { return patchLevel; }
    public Events getEvents() { return events; }
    public ComponentPackage_Db getDbObject() { return dbObject; }

    
    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    private void setPlatform(Platform_Db aPlatform) { platform = aPlatform; }
    private void setMaintLevel(int aLevel) { maintLevel = aLevel; }
    private void setPatchLevel(int aLevel) { patchLevel = aLevel; }
    private void setEvents(Events anEvents) { events = anEvents; }
    private void setDbObject(ComponentPackage_Db anObject) { dbObject = anObject; }
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
		dbObject = new ComponentPackage_Db(anId);
		dbObject.dbLookupById(xContext);
		setName(dbObject.getName());
	    }
	    catch (IcofException trap) {
		dbObject = null;
		throw new IcofException(this.getClass().getName(),
					"dbLookupById()", IcofException.SEVERE,
					"Unable to find ComponentPackage ("
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
     * Add this object to the database (don't associate with CompVersion)
     * 
     * @param xContext Application object
     * @param newEvents An Events object to associate Event changes with
     * @throws IcofException
     */
    private void dbAdd(EdaContext xContext, Events newEvents, User_Db user)
    throws IcofException {

	// If events is null create a new one
	if (newEvents == null) {
	    newEvents = new Events(xContext, 0);
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
     * Add this object to the database
     * 
     * @param xContext Application context
     * @param newEvents An Events object to associate Event changes with
     * @param aTk Tool kit for this package
     * @param aComp Component for this package
     * @throws IcofException
     */
    public void dbAdd(EdaContext xContext, Events newEvents, ToolKit aTk,
		      Component aComp, User_Db user)
    throws IcofException {

	// Lookup this component version
	Component_Version_Db compVer = new Component_Version_Db(xContext,
								aTk.getToolKit(),
								aComp.getComponent());
	compVer.dbLookupByCompRelVersion(xContext);
	
	dbAdd(xContext, newEvents, compVer, user);

    }


    /**
     * Add this object to the database
     * 
     * @param xContext Application context
     * @param newEvents An Events object to associate Event changes with
     * @param aCompVer ComponentVersion_Db object for this package
     * @throws IcofException
     */
    public void dbAdd(EdaContext xContext, Events newEvents,
		      Component_Version_Db aCompVer, User_Db user)
    throws IcofException {

	// Add this component package if not already there
	if (! dbLookupByName(xContext)) {
	    dbAdd(xContext, newEvents, user);
	}
	
	// Associate with the tk/component if not already done
	ComponentPackage_ComponentTkVersion_Db cpcv;
	cpcv = new ComponentPackage_ComponentTkVersion_Db(getDbObject(),
							  aCompVer);
	try {
	    cpcv.dbLookupByIds(xContext);
	}
	catch(IcofException notFound) {
	    cpcv.dbAddRow(xContext);
	}

    }


    /**
     * Look up this package by name
     *
     * @param xContext  Application context
     * @return
     * @throws IcofException 
     */
    public boolean dbLookupByName(EdaContext xContext) throws IcofException {
	
	boolean bFound = false;
	
	dbObject = new ComponentPackage_Db(getPlatform(), getName(), 
	                                   getMaintLevel(), getPatchLevel(), 
	                                   null);
	try {
	    dbObject.dbLookupByName(xContext);
	    bFound = true;
	    populate(xContext);
	}
	catch(IcofException ignore) { }
	
	return bFound;
	
    }


    /**
     * Populate the database object and look it up in the DB
     * 
     * @param xContext  Application context
     * @param bIsNew    If true don't try to look up this object
     *                  otherwise lookup it up
     * @throws IcofException
     */
    private void setDbObject(EdaContext xContext, boolean bIsNew)
    throws IcofException {

	if (getDbObject() != null)
	    dbObject = null;

	dbObject = new ComponentPackage_Db(getPlatform(), getName(),
					   getMaintLevel(), getPatchLevel(),
					   getEvents().getDbObject());
	if (! bIsNew)
	    getDbObject().dbLookupByName(xContext);

    }


    /**
     * Update this object in the database
     * 
     * @param xContext Application object
     * @param newMaintName New TK maintenance name
     * @param newEvents New Events collection
     * @throws IcofException
     */
    // public void dbUpdate(EdaContext xContext, String newMaintName,
    // Events newEvents)
    // throws IcofException {
    //
    // try {
    // // Lookup the object in the database first.
    // setDbObject(xContext);
    // }
    // catch(IcofException trap) {
    // throw new IcofException(this.getClass().getName(), "dbUpdate()",
    // IcofException.SEVERE,
    // "Unable to find existing object (" +
    // getName() + ") in the database.\n",
    // trap.getMessage());
    // }
    //
    // // Update the object
    // getDbObject().dbUpdateRow(xContext, newMaintName,
    // newEvents.getDbObject());
    //
    // }


    /**
     * Delete this object from the database
     * 
     * @param xContext Application object
     * @throws IcofException
     */
    // public void dbDelete(EdaContext xContext) throws IcofException {
    // getDbObject().dbDeleteRow(xContext);
    // }


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
     * @throws IcofException 
     */
    public String toString(EdaContext xContext) throws IcofException {

	if (getPlatform() != null)
	    if (! getPlatform().isLoaded())
		getPlatform().dbLookupById(xContext);
	
	// Get the class specific data
	StringBuffer buffer = new StringBuffer();
	buffer.append("Component Package\n---------------\n");
	if (getPlatform() == null)
	    buffer.append("Platform: NULL\n");
	else
	    buffer.append("Platform: " + getPlatform().getName() + "\n");
	buffer.append("Name    : " + getName() + "\n");
	buffer.append("Spin    : " + getMaintLevel() + "\n");
	buffer.append("Patch   : " + getPatchLevel() + "\n");
	if (getEvents() == null) 
	    buffer.append("Events ID  : NULL\n");
	else 
	    buffer.append("Events ID  : " + getEvents().getId() + "\n");

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

	setPlatform(getDbObject().getPlatform());
	setMaintLevel(getDbObject().getSpinLevel());
	setPatchLevel(getDbObject().getPatchLevel());

	if (getEvents() != null) {
	    events = null;
	}
	events = new Events(xContext, getDbObject().getEvents());

    }


    /**
     * Lookup Component Packages for this component and tool kit
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    public List<ComponentPackage> getCompPackages(EdaContext xContext,
						  ToolKit aTk, Component aComp,
						  Platform_Db aPlat, 
						  boolean bReverseSort)
    throws IcofException {

	List<ComponentPackage> pkgs = new ArrayList<ComponentPackage>();

	ComponentPackage_Db myPkg = new ComponentPackage_Db((long) 0);
	List<ComponentPackage_Db> dbPkgs = myPkg.dbLookupPkgs(xContext,
							      aTk.getToolKit(),
							      aComp.getComponent(),
							      aPlat,
							      bReverseSort);

	for (ComponentPackage_Db dbPkg : dbPkgs) {
	    ComponentPackage aPkg = new ComponentPackage(xContext, dbPkg);
	    pkgs.add(aPkg);
	}

	return pkgs;

    }
    
    
    /**
     * Lookup latest Component Package for this component and tool kit
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    public ComponentPackage getLastCompPackage(EdaContext xContext,
					       ToolKit aTk, Component aComp,
					       Platform_Db aPlat)
    throws IcofException {

	List<ComponentPackage> pkgs = getCompPackages(xContext, aTk, aComp,
						      aPlat, true);

	ComponentPackage myPkg = null;
	if (! pkgs.isEmpty()) {
	    myPkg = (ComponentPackage) pkgs.get(0);
	}

	return myPkg;

    }


    /**
     * Construct the component package name
     * @throws IcofException 
     */
    public static String getComponentPkgName(Component aComp, ToolKit aTk,
					     Platform_Db aPlat, int aMaint,
					     int aPatch) throws IcofException {

	StringBuffer name = new StringBuffer();
	name.append(aComp.getName() + ".");
	name.append(aTk.getPackagingName() + ".");
	name.append(IcofStringUtil.padString(String.valueOf(aMaint), 4, "0", true) + ".");
	name.append(IcofStringUtil.padString(String.valueOf(aPatch), 4, "0", true) + ".");
	name.append(aPlat.getPackagingName());

	return name.toString();

    }


    /**
     * Associate package contents (deliverables) with this package
     *
     * @param xContext  Application context     
     * @param contents  Collection of contents
     * @throws IcofException 
     */
    public void addDeliverables(EdaContext xContext,
				Deliverables contents)
				throws IcofException {

	for (PkgDeliverable xDel : contents.getDeliverables().values()) {
		
	    // Add the file name
	    FileName file = new FileName(xContext, xDel.getPartialDelName());
	    file.dbAdd(xContext);
	    
	    // Add the deliverable
	    Deliverable del = new Deliverable(xContext, this, file, 
	                                      xDel.getCheckSum(), 
	                                      xDel.getTypeName(), 
	                                      xDel.getActionName(),
	                                      xDel.getSize(),
	                                      xDel.getTimestamp());
	    del.dbAdd(xContext);

	}

    }


    /**
     * Remove all deliverables currently associated with this package
     *
     * @param xContext
     * @throws IcofException 
     */
    public void removeAllDeliverables(EdaContext xContext) throws IcofException {

	Deliverable del = new Deliverable(xContext, this, null, 0, null, 
	                                  null, 0, 0);
	del.dbDeleteAllForCompPkg(xContext);
	
    }


    /**
     * Determine if this component package is in the specified tool kit package
     *
     * @param xContext   Application context
     * @param toolKitPkg Candidate tool kit package
     * @return True if comp pkg is a member of tk pkg otherwise false
     */
    public boolean dbLookupByTkPkg(EdaContext xContext,
				   ToolKitPackage toolKitPkg) {

	boolean found = false;
	
	try {
	    found = getDbObject().dbLookupInTkPkg(xContext, 
	                                          toolKitPkg.getDbObject());
	}
	catch(IcofException ignore) {}
	
	return found;
	
    }


    /**
     * Delete any change requests associated with this package
     *
     * @param xContext Application context
     * @param user     Person making this update
     * @throws IcofException 
     */
    public void deleteChangeRequests(EdaContext xContext, User_Db user) 
    throws IcofException {

	ChangeRequest_Db cr = new ChangeRequest_Db(0);
	cr.dbDeleteCompPackages(xContext, getDbObject().getId(), user);
	
    }


    /**
     * Add a collection of Change Requests to this package
     *
     * @param xContext       Application context
     * @param changeRequests Collection of CRs needing updates
     * @param user           Person make the updates
     * @throws IcofException 
     */
    public void addChangeRequests(EdaContext xContext,
				  ChangeRequests changeRequests,
				  User_Db user) throws IcofException {

	if (changeRequests == null)
	    return;
	
	for (ChangeRequest cr : changeRequests.getChangeRequests().values()) {
	    cr.getChangeRequest().dbUpdateCompPkg(xContext, 
	                                          getDbObject().getId(), user); 
	}
	
    }

    
    /**
     * Log an event (ie, update the state of) this component package
     *
     * @param xContext  Application context
     * @param newState  Event to log
     * @param comments  Event comments
     * @param user      User making the update
     * @throws IcofException 
     */
    public void updateState(EdaContext xContext, EventName newState,
                            String comments,  User_Db user) 
    throws IcofException {

	// Mark the current event as expired
	Event lastEvent = new Event(xContext, null, "", "" , null, null);
	lastEvent.dbUpdateExpiredTms(xContext, getEvents());

	// Create a new event record
	Event newEvent = new Event(xContext, newState, comments, 
	                           user.getIntranetId(), null, null);
	newEvent.dbAdd(xContext, getEvents().getId(), user);
	
    }
    
    
    /**
     * Log an event (ie, update the state of) this component package
     *
     * @param xContext   Application context
     * @param stateName  Event name
     * @param comments   Event comments
     * @param user       User making the update
     * @throws IcofException 
     */
    public void updateState(EdaContext xContext, String stateName,
                            String comments,  User_Db user) 
    throws IcofException {

	EventName state = new EventName(xContext, stateName);
	state.dbLookupByName(xContext);
	
	updateState(xContext, state, comments, user);
	
    }


    /**
     * Delete this object from the database
     * Delete entries from these tables in this order
     *  - Deliverables
     *  - Event
     *  - TkPkg_x_CompPkg
     *  - CompPkg_x_Comp_TkVersion
     *  - Get CRs
     *    - Update State to Complete
     *    - Update CompPkg_id to null
     *  - CompPkg
     *  - Events
     *
     * @param xContext
     * @param skipCrs  If true don't reset CR states to complete
     * @throws IcofException 
     */
    public String  dbDelete(EdaContext xContext, boolean skipCrs) 
    throws IcofException {
	
	StringBuffer log = new StringBuffer();
	
	// Delete Deliverables associated with this pkg
	Deliverable_Db del = new Deliverable_Db(0);
	Vector<Deliverable_Db> dels = del.dbLookupByCompPkgId(xContext, 
	                                                      getDbObject().getId());
	for (Deliverable_Db myDel : dels) {
	    log.append("Deleting DEL: " + myDel.getFileNameId() + "\n");
	    myDel.dbDeleteRow(xContext);
	}
	
	// Delete Event rows for this object
	Event_Db dbEvent = new Event_Db(0);
	Vector<Event_Db> dbEvents = dbEvent.dbLookupByEventsId(xContext, 
	                                                    getEvents().getId());
	for (Event_Db myEvent : dbEvents) {
	    log.append("Deleting EVT: " + myEvent.getId() + "\n");
	    myEvent.dbDeleteRow(xContext);
	}
	
	// Delete TkPkg_x_CompPkg entries
	ComponentPackage_ToolKitPackage_Db cptp = 
	  new ComponentPackage_ToolKitPackage_Db(getDbObject(), null);
	log.append("Deleting CP_TP ... \n");
	cptp.dbDeleteCompPkgs(xContext);

	// Delete CompPkg_x_Comp_Tk entries
	ComponentPackage_ComponentTkVersion_Db cpcv = 
	new ComponentPackage_ComponentTkVersion_Db(getDbObject(), null);
	log.append("Deleting CP_TK ... \n");
	cpcv.dbDeleteRowCompPkgs(xContext);

	if (! skipCrs) {
	    
	    // Look up the new CR state
	    ChangeRequestStatus crComplete = new ChangeRequestStatus(xContext, 
	                                                             "COMPLETE");
	    crComplete.dbLookupByName(xContext);

	    // Manage Change Requests
	    ChangeRequest_Db cr = new ChangeRequest_Db(0);
	    Vector<ChangeRequest_Db> crs = cr.dbLookupByCompPkg(xContext, 
	                                                        getDbObject().getId());
	    for (ChangeRequest_Db myCr : crs) {
		log.append("Updating CR: " + myCr.getCqName() + "\n");
		myCr.dbUpdateCompPkg(xContext, 0, "svnlib@us.ibm.com");
		myCr.dbUpdateRow(xContext, 
		                 myCr.getCqName(), myCr.getDescription(), 
		                 crComplete.getStatus(), 
		                 myCr.getType(), myCr.getSeverity(), 
		                 myCr.getImpactedCustomer(),
		                 myCr.getCreatedBy(), "svnlib@us.ibm.com");
	    }
	    
	}
	
	
	// Delete this object
	Events myEvents = getEvents();
	log.append("Deleting CompPkg: " + getName() + "\n");
	getDbObject().dbDeleteRow(xContext);
	
	// Delete events
	log.append("Deleting EVTs: " + events.getId() + "\n");
	events.dbDelete(xContext, myEvents.getId());
	
	return log.toString();
	
    }


    /**
     * Create a collection of pkgs for the given tool kit
     *
     * @param xContext Application context
     * @param toolKit  Tool kit in question
     * @return
     * @throws IcofException 
     */
    public Vector<ComponentPackage> dbLookupByTk(EdaContext xContext,
						 ToolKit toolKit) 
						 throws IcofException {

	List<ComponentPackage_Db> dbPkgs;
	if (getDbObject() == null)
	    dbObject = new ComponentPackage_Db(0);
	dbPkgs = getDbObject().dbLookupPackages(xContext, toolKit.getToolKit());  
	
	Vector<ComponentPackage> pkgs = new Vector<ComponentPackage>();
	for (ComponentPackage_Db dbPkg : dbPkgs) {
	    ComponentPackage pkg = new ComponentPackage(xContext, dbPkg);
	    pkgs.add(pkg);
	}
	
	return pkgs;
	
    }

    
}
