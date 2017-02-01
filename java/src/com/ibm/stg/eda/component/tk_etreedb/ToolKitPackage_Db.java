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
 * Tool Kit Package class
 *-----------------------------------------------------------------------------
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/21/2013 GFS  Initial coding.
 * 04/07/2014 GFS  Added dbLookupByTkAndState() method
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

public class ToolKitPackage_Db extends TkAudit {

    /**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.TOOLKITPACKAGE";
    public static final String ID_COL = "TOOLKITPACKAGE_ID";
    public static final String MAINTENANCE_COL = "MAINT";
    public static final String EVENTS_ID_COL = "EVENTS_ID";
    public static final String ALL_COLS = ID_COL + "," + MAINTENANCE_COL + ","
					  + EVENTS_ID_COL;
    private static final long serialVersionUID = 1L;


    /**
     * Constructor - takes a DB id
     * 
     * @param anId A database id
     */
    public ToolKitPackage_Db(long anId) {

	setId(anId);
    }


    /**
     * Constructor - all members except DB id
     * 
     * @param aMaintName Maintenance name for TK
     * @param anEvents Events collection
     */
    public ToolKitPackage_Db(String aMaintName, Events_Db anEvents) {

	setName(aMaintName);
	setEvents(anEvents);
    }


    /**
     * Constructor - all members
     * 
     * @param anId Id of this object
     * @param aMaintName Maintenance name for TK
     * @param anEventsId Events collection
     */
    public ToolKitPackage_Db(long id, String aMaintName, long anEventsId) {

	setId(id);
	setName(aMaintName);
	setEvents(anEventsId);
    }


    /**
     * Data Members
     * @formatter:off
     */
    private long id;
    private String  name;
    private Events_Db events;
    
    
    /**
     * Getters
     */
    public long getId() { return id; }
    public String getName() { return name; }
    public Events_Db getEvents() { return events; }
    
    
    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setName(String aMaint) { name = aMaint; }
    private void setEvents(Events_Db anEvents) { events = anEvents; }
    private void setEvents(long anId) { 
    	if (getEvents() != null) 
    		events = null;
    	events = new Events_Db(anId); 
    }
    // @formatter:on


    /**
     * Create a PreparedStatement to lookup this object by id.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupIdStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
		       + ID_COL + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupAllStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
		       + MAINTENANCE_COL + " = ? " + " and " + EVENTS_ID_COL
		       + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by maintenance.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupByMaintStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
		       + MAINTENANCE_COL + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to add a row.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setAddRowStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "insert into " + TABLE_NAME + " ( " + ALL_COLS + " )"
		       + " values( ?, ?, ? )";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup the next id for this table.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setNextIdStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = TkAudit.getNextIdQuery(xContext, TABLE_NAME, ID_COL);

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to delete this object
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setDeleteStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "delete from " + TABLE_NAME + " where " + ID_COL
		       + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to update this object
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setUpdateStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME + " set " + MAINTENANCE_COL
		       + " = ? , " + EVENTS_ID_COL + " = ? " + " where "
		       + ID_COL + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup objects that are in a certain event
     * state.
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupByEventNameStatement(EdaContext xContext)
    throws IcofException {

	// select *
	// from tk.Event as e,
	// tk.ToolKitPackage as x
	// where e.EventName_id = 2
	// and x.Events_id = e.Events_id
	// and e.expired_tmstmp is null

	// Define the query.
	String query = "select * " + " from " + TABLE_NAME + " as x, "
		       + Event_Db.TABLE_NAME + " as e " + " where e."
		       + Event_Db.NAME_ID_COL + " = ? " + " and x."
		       + EVENTS_ID_COL + " = e." + Event_Db.EVENTS_COL
		       + " and e." + Event_Db.EXPIRED_ON_COL + " is null";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup objects that are associated with a
     * given tool kit
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupByTKStatement(EdaContext xContext, String aMaint)
    throws IcofException {

	// select distinct tp.toolkitpackage_id, tp.maint, tp.events_id
	// from tk.Component_TkVersion as cv,
	// tk.ComponentPackage_x_Component_TkVersion as cpcv,
	// tk.ComponentPackage as cp,
	// tk.ComponentPackage_x_ToolKitPackage as cptp,
	// tk.ToolKitPackage as tp
	// where cv.tkversion_id = 6
	// and cv.component_tkversion_id = cpcv.component_tkversion_id
	// and cpcv.componentpackage_id = cp.componentpackage_id
	// and cp.componentpackage_id = cptp.componentpackage_id
	// and cptp.toolkitpackage_id = tp.toolkitpackage_id
	
	// --> Add if maint == null
	// order by tp.maint desc

	// >>> Add if maint != null
	// and tp.maint = ?
	String query = "select distinct tp." + ID_COL + ", tp." 
	               + MAINTENANCE_COL + ", tp." + EVENTS_ID_COL   
	               + " from "
		       + Component_Version_Db.TABLE_NAME
		       + " as cv, "
		       + ComponentPackage_ComponentTkVersion_Db.TABLE_NAME
		       + " as cpcv, "
		       + ComponentPackage_Db.TABLE_NAME
		       + " as cp, "
		       + ComponentPackage_ToolKitPackage_Db.TABLE_NAME
		       + " as cptp, "
		       + ToolKitPackage_Db.TABLE_NAME
		       + " as tp "
		       + " where cv."
		       + Component_Version_Db.VERSION_ID_COL
		       + " = ? "
		       + " and cv."
		       + Component_Version_Db.ID_COL
		       + " = cpcv."
		       + ComponentPackage_ComponentTkVersion_Db.COMPONENT_VERSION_COL
		       + " and cpcv."
		       + ComponentPackage_ComponentTkVersion_Db.COMPONENT_PACKAGE_ID_COL
		       + " = cp."
		       + ComponentPackage_Db.ID_COL
		       + " and cp."
		       + ComponentPackage_Db.ID_COL
		       + " = cptp."
		       + ComponentPackage_ToolKitPackage_Db.COMPONENT_PACKAGE_ID_COL
		       + " and cptp."
		       + ComponentPackage_ToolKitPackage_Db.TOOLKITPACKAGE_ID_COL
		       + " = tp." + ToolKitPackage_Db.ID_COL;
	
	if (aMaint == null) 
	    query += " order by tp." + ToolKitPackage_Db.MAINTENANCE_COL + " desc";
	else 
	    query += " and tp." + ToolKitPackage_Db.MAINTENANCE_COL + " = ?";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }

    /**
     * Create a PreparedStatement to lookup objects that are associated with a
     * given tool kit and in a certain state
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupByTkAndStateStatement(EdaContext xContext)
    throws IcofException {

	// select distinct tp.toolkitpackage_id, tp.maint, tp.events_id
	// from tk.Component_TkVersion as cv,
	//      tk.ComponentPackage_x_Component_TkVersion as cpcv,
	//      tk.ComponentPackage as cp,
	//      tk.ComponentPackage_x_ToolKitPackage as cptp,
	//      tk.ToolKitPackage as tp,
	//      tk.Event as e
	// where cv.tkversion_id = 6
	//   and e.eventname_id = ?
	//   and e.expired_tmstmp is null
	//   and tp.events_id = e.events_id
	//   and cv.component_tkversion_id = cpcv.component_tkversion_id
	//   and cpcv.componentpackage_id = cp.componentpackage_id
	//   and cp.componentpackage_id = cptp.componentpackage_id
	//   and cptp.toolkitpackage_id = tp.toolkitpackage_id
	// order by tp.maint desc

	String query = "select distinct tp." + ID_COL + ", tp." 
	               + MAINTENANCE_COL + ", tp." + EVENTS_ID_COL   
	               + " from "
		       + Component_Version_Db.TABLE_NAME
		       + " as cv, "
		       + ComponentPackage_ComponentTkVersion_Db.TABLE_NAME
		       + " as cpcv, "
		       + ComponentPackage_Db.TABLE_NAME
		       + " as cp, "
		       + ComponentPackage_ToolKitPackage_Db.TABLE_NAME
		       + " as cptp, "
		       + ToolKitPackage_Db.TABLE_NAME
		       + " as tp, "
		       + Event_Db.TABLE_NAME
		       + " as e "
		       + " where cv."
		       + Component_Version_Db.VERSION_ID_COL
		       + " = ? "
		       + " and e."
		       + Event_Db.NAME_ID_COL + " = ? "
		       + " and e."
		       + Event_Db.EXPIRED_ON_COL + " is null "
		       + " and tp." + ToolKitPackage_Db.EVENTS_ID_COL + " = e." 
		       + Event_Db.EVENTS_COL
		       + " and cv."
		       + Component_Version_Db.ID_COL
		       + " = cpcv."
		       + ComponentPackage_ComponentTkVersion_Db.COMPONENT_VERSION_COL
		       + " and cpcv."
		       + ComponentPackage_ComponentTkVersion_Db.COMPONENT_PACKAGE_ID_COL
		       + " = cp."
		       + ComponentPackage_Db.ID_COL
		       + " and cp."
		       + ComponentPackage_Db.ID_COL
		       + " = cptp."
		       + ComponentPackage_ToolKitPackage_Db.COMPONENT_PACKAGE_ID_COL
		       + " and cptp."
		       + ComponentPackage_ToolKitPackage_Db.TOOLKITPACKAGE_ID_COL
		       + " = tp." + ToolKitPackage_Db.ID_COL 
		       + " order by tp." + ToolKitPackage_Db.MAINTENANCE_COL + " desc";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }

    
    /**
     * Look up by the id.
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbLookupById(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupIdStatement(xContext);

	try {
	    getStatement().setLong(1, getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupById()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (!selectSingleRow(xContext, getStatement())) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
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
     * Look up by all data members
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbLookupByAll(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupAllStatement(xContext);

	try {
	    getStatement().setString(1, getName());
	    getStatement().setLong(2, getEvents().getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupByAll()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (!selectSingleRow(xContext, getStatement())) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupByAll()",
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
     * Look up by all tool kit and maint
     * 
     * @param xContext An application context object.
     * @param aTkId    Tool kit this package is associated with
     * @param aMaint   Maintenance name
     * @throws Trouble querying the database.
     */
    public void dbLookupByToolKit(EdaContext xContext, RelVersion_Db aTk, 
                                  String aMaint)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupByTKStatement(xContext, aMaint);

	try {
	    getStatement().setShort(1, aTk.getId());
	    getStatement().setShort(2, Short.parseShort(aMaint));
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupByToolKit()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (! selectSingleRow(xContext, getStatement())) {
	    IcofException ie = new IcofException(this.getClass().getName(),
						 "dbLookupByToolKit()",
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
     * Insert a new row.
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext)
    throws IcofException {

	// Create the next id for this new row
	setNextIdStatement(xContext);
	setId(getNextBigIntId(xContext, getStatement()));
	closeStatement(xContext);

	// Create the SQL query in the PreparedStatement.
	setAddRowStatement(xContext);
	try {
	    getStatement().setLong(1, getId());
	    getStatement().setString(2, getName());
	    getStatement().setLong(3, getEvents().getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbAddRow()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (!insertRow(xContext)) {
	    IcofException ie = new IcofException(this.getClass().getName(),
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
     * @param xContext An application context object.
     * @param newMaint New maintenance name
     * @param anEventsId New Events id
     * @throws Trouble querying the database.
     */
    public void dbUpdateRow(EdaContext xContext, String newMaint,
			    Events_Db newEvents)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setUpdateStatement(xContext);
	try {
	    getStatement().setString(1, newMaint);
	    getStatement().setLong(2, getEvents().getId());
	    getStatement().setLong(3, getId());

	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbUpdateRow()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (!insertRow(xContext)) {
	    IcofException ie = new IcofException(this.getClass().getName(),
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
	setName(newMaint);
	setEvents(newEvents);
	setLoadFromDb(true);


    }


    /**
     * Delete (mark as deleted) this object in the database
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbDeleteRow(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setDeleteStatement(xContext);
	try {
	    getStatement().setLong(1, getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbDeleteRow()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (!insertRow(xContext)) {
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
     * @param xContext Application context.
     * @param rs A valid result set.
     * @throws Trouble retrieving the data.
     */
    protected void populate(EdaContext xContext, ResultSet rs)
    throws SQLException, IcofException {

	setId(rs.getLong(ID_COL));
	setName(rs.getString(MAINTENANCE_COL));
	setEvents(rs.getLong(EVENTS_ID_COL));
	setLoadFromDb(true);

    }


    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

	// Get the class specific data
	StringBuffer buffer = new StringBuffer();
	buffer.append("ID          : " + getId() + "\n");
	buffer.append("Maintenance : " + getName() + "\n");
	buffer.append("Events id   : " + getEvents().getId() + "\n");

	return buffer.toString();

    }


    /**
     * Create a key from the ID.
     * 
     * @param xContext Application context object.
     * @return A Statement object.
     */
    public String getIdKey(EdaContext xContext) {

	return String.valueOf(getId());
    }


    /**
     * Look up by Event Name
     * 
     * @param xContext An application context object.
     * @param eventNameId EventName id to find events for
     * @throws Trouble querying the database.
     */
    public Vector<ToolKitPackage_Db> dbLookupByEventName(EdaContext xContext,
							 long eventNameId)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupByEventNameStatement(xContext);

	try {
	    getStatement().setLong(1, eventNameId);
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupByEventName()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Vector<ToolKitPackage_Db> packages = new Vector<ToolKitPackage_Db>();
	try {
	    while (rs.next()) {
		long anId = rs.getLong(ID_COL);
		String name = rs.getString(MAINTENANCE_COL);
		long eventsId = rs.getLong(EVENTS_ID_COL);

		ToolKitPackage_Db pkg = new ToolKitPackage_Db(anId, name,
							      eventsId);
		packages.add(pkg);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupByEventName()",
				    IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return packages;

    }


    /**
     * Look up by Tool Kit
     * 
     * @param xContext An application context object.
     * @param eventNameId EventName id to find events for
     * @throws Trouble querying the database.
     */
    public Vector<ToolKitPackage_Db> dbLookupByToolKit(EdaContext xContext,
						       RelVersion_Db aTk)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupByTKStatement(xContext, null);

	try {
	    getStatement().setLong(1, aTk.getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupByToolKit()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Vector<ToolKitPackage_Db> packages = new Vector<ToolKitPackage_Db>();
	try {
	    while (rs.next()) {
		long anId = rs.getLong(ID_COL);
		short maint = rs.getShort(MAINTENANCE_COL);
		long eventsId = rs.getShort(EVENTS_ID_COL);
		ToolKitPackage_Db pkg = new ToolKitPackage_Db(anId, 
		                                              String.valueOf(maint), 
		                                              eventsId);
		packages.add(pkg);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupByToolKit()",
				    IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return packages;

    }

    /**
     * Look up by Tool Kit
     * 
     * @param xContext An application context object.
     * @param eventNameId EventName id to find events for
     * @throws Trouble querying the database.
     */
    public Vector<ToolKitPackage_Db> dbLookupByTkAndState(EdaContext xContext,
                                                          RelVersion_Db aTk,
                                                          EventName_Db aState)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupByTkAndStateStatement(xContext);
	

	// TODO
	//printQuery(xContext);
	//System.out.println("TK id: " + aTk.getId());
	//System.out.println("State id: " + aState.getId());
	
	
	try {
	    getStatement().setLong(1, aTk.getId());
	    getStatement().setLong(2, aState.getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupByTkAndState()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Vector<ToolKitPackage_Db> packages = new Vector<ToolKitPackage_Db>();
	try {
	    while (rs.next()) {
		long anId = rs.getLong(ID_COL);
		short maint = rs.getShort(MAINTENANCE_COL);
		long eventsId = rs.getShort(EVENTS_ID_COL);
		ToolKitPackage_Db pkg = new ToolKitPackage_Db(anId, 
		                                              String.valueOf(maint), 
		                                              eventsId);
		packages.add(pkg);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupByTkAndState()",
				    IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return packages;

    }


}
