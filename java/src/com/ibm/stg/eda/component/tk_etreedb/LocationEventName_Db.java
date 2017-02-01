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
 * LocationEventName DB class
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 02/02/2011 GFS  Initial coding.
 * 02/11/2011 GFS  Added support for new RequiresPlatform column.
 * 10/13/2011 GFS  Added dbLookupAll() method.
 * 12/18/2012 GFS  Added support for advance to xtinct.
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


public class LocationEventName_Db extends TkAudit {

    /**
     * 
     */
    private static final long serialVersionUID = 2819951048344679850L;
    /**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.LOCATIONEVENTNAME";
    public static final String ID_COL = "LOCATIONEVENTNAME_ID";
    public static final String NAME_COL = "EVENTNAME";
    public static final String REQ_PLATFORM_COL = "REQUIRESPLATFORM";
    public static final String ALL_COLS = ID_COL + "," +
    NAME_COL  + "," +
    REQ_PLATFORM_COL;
    public static final String EVENT_ADV_TO_SHIPB = "ADVANCED_TO_SHIPB";
    public static final String EVENT_ADV_TO_XTINCT = "ADVANCED_TO_XTINCT";
    public static final String EVENT_ADV_TO_CUSTOM = "ADVANCED_TO_CUSTOMTKB";
    public static final String EVENT_ADV_TO_TKB = "ADVANCED_TO_TKB";


    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public LocationEventName_Db(short anId) {
	setId(anId);
    }


    /**
     * Constructor - takes an event name
     * 
     * @param aName      Name of an event
     */
    public LocationEventName_Db(String aName) {
	setName(aName.toUpperCase());
    }


    /**
     * Constructor - takes an event name
     * 
     * @param aName      Name of an event
     * @param aReqPlat   Boolean for if this name requires a platform.
     */
    public LocationEventName_Db(String aName, boolean aReqPlat) {
	setName(aName.toUpperCase());
	setReqPlatform(aReqPlat);
    }


    /**
     * Data Members
     */
    private short id;
    private String name;
    private boolean reqPlatform;


    /**
     * Getters
     */
    public String getName() { return name; }
    public boolean getRequiresPlat() { return reqPlatform; }
    public short getId() { return id; }


    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    private void setReqPlatform(boolean aFlag) { reqPlatform = aFlag; }
    public void setId(short anId) { id = anId; }


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
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupNameStatement(EdaContext xContext) throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + 
	" from " + TABLE_NAME + 
	" where " + NAME_COL + " = ? ";

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
	" values( ?, ?, ? )";

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
	String query = "delete " + TABLE_NAME + " where " + ID_COL + " = ? ";

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
     * Create a PreparedStatement to all rows in this table
     * @param xContext  Application context.
     * @throws IcofException 
     */
    public void setLookupAllStatement(EdaContext xContext) throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + 
	" from " + TABLE_NAME +
	" order by " + NAME_COL + " asc";

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
     * Look up this object by name.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByName(EdaContext xContext) throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setLookupNameStatement(xContext);

	try {
	    getStatement().setString(1, getName());
	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "dbLookupByName()",
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
	                                         "dbLookupByName()",
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
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext)
    throws IcofException{

	// Get the next id for this new row.
	setNextIdStatement(xContext);
	short id = getNextSmallIntId(xContext);
	closeStatement(xContext);

	// Create the SQL query in the PreparedStatement.
	setAddRowStatement(xContext);
	try {
	    getStatement().setLong(1, id);
	    getStatement().setString(2, getName());
	    if (getRequiresPlat())
		getStatement().setString(3, "Y");
	    else 
		getStatement().setString(3, "N");

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
	                                         "QUERY: " + getStatement().toString());
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
     * @param xContext       Application context.
     * @param rs             A valid result set.
     * @throws IcofException 
     * @throws SQLException 
     */
    protected void populate(EdaContext xContext, ResultSet rs) 
    throws SQLException, IcofException  {

	setId(rs.getShort(ID_COL));
	setName(rs.getString(NAME_COL));
	if (rs.getString(REQ_PLATFORM_COL).equals("Y")) {
	    setReqPlatform(true);
	}
	else {
	    setReqPlatform(false);
	}
	setLoadFromDb(true);

    }


    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

	// Get the class specific data
	StringBuffer buffer = new StringBuffer();
	buffer.append("ID: " + getId() + "\n");
	buffer.append("Event name: " + getName() + "\n");
	buffer.append("Requires Platform: " + getRequiresPlat() + "\n");

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
     * Look up all event names.
     * 
     * @param xContext  An application context object.
     * @return  Returns  A collection of LocationEventName_Db objects 
     * @throws          Trouble querying the database.
     */
    public Vector<LocationEventName_Db> dbLookupAll(EdaContext xContext) throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setLookupAllStatement(xContext);

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Vector <LocationEventName_Db> events = new Vector<LocationEventName_Db>();
	try {
	    while (rs.next()) {
		String aName =  rs.getString(NAME_COL);
		String required =  rs.getString(REQ_PLATFORM_COL);
		boolean platReq = false;
		if (required.equals("Y")) 
		    platReq = true;
		LocationEventName_Db event = 
		new LocationEventName_Db(aName, platReq);
		events.add(event);
	    }

	}
	catch(SQLException ex) {
	    throw new IcofException(this.getClass().getName(), 
	                            "dbLookupAll()",
	                            IcofException.SEVERE, 
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return events;

    }

}
