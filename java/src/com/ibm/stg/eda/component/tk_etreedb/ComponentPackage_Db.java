/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2013 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *-PURPOSE---------------------------------------------------------------------
 * Component_Package DB class
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/21/2013 GFS  Initial coding.
 * 03/18/2014 GFS  Enhanced dbLookupPackages() to include ascending/descending
 *                 sorting 
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;


public class ComponentPackage_Db extends TkAudit {

    /**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.COMPONENTPACKAGE";
    public static final String ID_COL = "COMPONENTPACKAGE_ID";
    public static final String PLATFORM_ID_COL = "PLATFORM_ID";
    public static final String PACKAGE_NAME_COL = "PACKAGENAME";
    public static final String SPIN_LEVEL_COL = "SPINLEVEL";
    public static final String PATCH_LEVEL_COL = "PATCHLEVEL";
    public static final String EVENTS_ID_COL = "EVENTS_ID";
    public static final String ALL_COLS = ID_COL + "," + PLATFORM_ID_COL + ","
					  + PACKAGE_NAME_COL + ","
					  + SPIN_LEVEL_COL + ","
					  + PATCH_LEVEL_COL + ","
					  + EVENTS_ID_COL;
    private static final long serialVersionUID = 1L;


    /**
     * Constructor - takes a DB id
     * 
     * @param anId A database id
     */
    public ComponentPackage_Db(long anId) {

	setId(anId);
    }


    /**
     * Constructor - used to create new row branch name.
     * 
     * @param aPlatform Platform package built for
     * @param aName Package name
     * @param aSpinLevel The maintenance level
     * @param aPatchLevel The urgent level
     * @param anEvents The Events collection id
     */
    public ComponentPackage_Db(Platform_Db aPlatform, String aName,
			       int aSpinLevel, int aPatchLevel,
			       Events_Db anEvents) {

	setPlatform(aPlatform);
	setName(aName);
	setSpinLevel(aSpinLevel);
	setPatchLevel(aPatchLevel);
	setEvents(anEvents);
    }


    /**
     * Constructor - takes all members
     * 
     * @param anId Object id
     * @param aPlatform Platform package built for
     * @param aName Package name
     * @param aSpinLevel The maintenance level
     * @param aPatchLevel The urgent level
     * @param anEvents The Events object
     */
    public ComponentPackage_Db(long anId, Platform_Db aPlatform, String aName,
			       int aSpinLevel, int aPatchLevel,
			       Events_Db anEvents) {

	setId(anId);
	setPlatform(aPlatform);
	setName(aName);
	setSpinLevel(aSpinLevel);
	setPatchLevel(aPatchLevel);
	setEvents(anEvents);
    }


    /**
     * Constructor - takes all members
     * 
     * @param anId Object id
     * @param aPlatform Platform package built for
     * @param aName Package name
     * @param aSpinLevel The maintenance level
     * @param aPatchLevel The urgent level
     * @param anEventsId The Events id
     */
    public ComponentPackage_Db(long anId, Platform_Db aPlatform, String aName,
			       int aSpinLevel, int aPatchLevel, long anEventsId) {

	setId(anId);
	setPlatform(aPlatform);
	setName(aName);
	setSpinLevel(aSpinLevel);
	setPatchLevel(aPatchLevel);
	setEvents(anEventsId);
    }


    /**
     * Data Members
     * @formatter:off
     */
    private long id;
    private Platform_Db platform;
    private String name;
    private int spinLevel;
    private int patchLevel;
    private Events_Db events;
    
    
    /**
     * Getters
     */
    public long getId() { return id; }
    public Platform_Db getPlatform() { return platform; }
    public String getName() { return name; }
    public int getSpinLevel() { return spinLevel; }
    public int getPatchLevel() { return patchLevel; }
    public Events_Db getEvents() { return events; }


    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setPlatform(Platform_Db aPlat) { platform = aPlat; }
    private void setName(String aName) { name = aName; }
    private void setSpinLevel(int aLvl) { spinLevel = aLvl; }
    private void setPatchLevel(int aLvl) { patchLevel = aLvl; }
    private void setEvents(Events_Db anEvents) { events = anEvents; }
    private void setPlatform(EdaContext xContext, short anId) { 
    	platform = new Platform_Db(anId);
    }
    private void setEvents(long anId) { 
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
     * Create a PreparedStatement to lookup this object by name
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupNameStatement(EdaContext xContext)
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + " from " + TABLE_NAME + " where "
		       + PACKAGE_NAME_COL + " = ? ";

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
	String query = "insert into " + TABLE_NAME + " ( " + ALL_COLS + " ) "
		       + " values( ?, ?, ?, ?, ?, ? )";

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
	// tk.ComponentPackage as x
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
     * Create a PreparedStatement to lookup objects for a tk, component and plat
     * state.
     * 
     * @param xContext Application context.
     * @param bReverse Reverse sort the results
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupPackagesStatement(EdaContext xContext, boolean bReverse)
    throws IcofException {

//	select cp.componentpackage_id, cp.packagename
//	  from tk.Component_TkVersion as cv,
//	       tk.ComponentPackage_x_Component_TkVersion as cpcv,
//	       tk.ComponentPackage as cp
//	 where cv.component_tkversion_id = 6
//         and cp.platform_id = 1
//	   and cv.component_tkversion_id = cpcv.component_tkversion_id
//	   and cpcv.componentpackage_id = cp.componentpackage_id
//	   order by cp.packagename

	// Define the query.
	String query = "select cp." + ID_COL +  
	               " from " + Component_Version_Db.TABLE_NAME + " as cv, " +
		       ComponentPackage_ComponentTkVersion_Db.TABLE_NAME + " as cpcv, " +
		       ComponentPackage_Db.TABLE_NAME + " as cp " +
	               " where cv." + Component_Version_Db.ID_COL + " = ? " +
		       " and cp." + PLATFORM_ID_COL + " = ? " + 
	               " and cv." + Component_Version_Db.ID_COL + " = cpcv." + 
	               ComponentPackage_ComponentTkVersion_Db.COMPONENT_VERSION_COL +
	               " and cpcv." + ComponentPackage_ComponentTkVersion_Db.COMPONENT_PACKAGE_ID_COL + " = cp." +
	               ComponentPackage_Db.ID_COL + 
	               " order by cp." + ComponentPackage_Db.PACKAGE_NAME_COL;
	
	if (bReverse)
	    query += " DESC";
	else
	    query += " ASC";
	
	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }
    
    
    /**
     * Create a PreparedStatement to determine if this objects is associated
     * with a tk pkg object
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupInTkPkgStatement(EdaContext xContext)
    throws IcofException {

	// select ComponentPackage_id
	//   from tk.ComponentPackage_x_ToolKitPackage
	//  where ToolKitPackage_id = ?
	//    and ComponentPackage_id = ?

	// Define the query.
	String query = "select " + ID_COL + 
	               " from " + ComponentPackage_ToolKitPackage_Db.TABLE_NAME + 
		        " where " + 
		        ComponentPackage_ToolKitPackage_Db.TOOLKITPACKAGE_ID_COL +
		        " = ? and " +
		        ComponentPackage_ToolKitPackage_Db.COMPONENT_PACKAGE_ID_COL +
		        " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }
    
    
    /**
     * Create a PreparedStatement to look up the pkgs associated with a TK
     * 
     * @param xContext Application context.
     * @return PreparedStatement
     * @throws IcofException
     */
    public void setLookupByTkStatement(EdaContext xContext)
    throws IcofException {

	// select cp.ComponentPackage_id
	//   from tk.ComponentPackage_x_Component_TkVersion as cpcv,
	//        tk.Component_TkVersion as cv
	//        tk.ComponentPackage as cp
	//  where cv.TkVersion_Id = ?
	//    and cv.Component_TkVersion_Id = cpcv.Component_TkVersion_Id
	//    and cpcv.ComponentPackage_Id = cp.ComponentPackage_Id
	//    order by cp.PackageName

	// Define the query.
	String query = "select cp.ComponentPackage_id" + 
	               " from tk.ComponentPackage_x_Component_TkVersion as cpcv," +
	               " tk.Component_TkVersion as cv," +
	               " tk.ComponentPackage as cp" +
	               " where cv.TkVersion_Id = ?" +
	               " and cv.Component_TkVersion_Id = cpcv.Component_TkVersion_Id" +
	               " and cpcv.ComponentPackage_Id = cp.ComponentPackage_Id" +
	               " order by cp.PackageName";

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
     * Look up this object by id.
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
	if (!selectSingleRow(xContext)) {
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
     * Look up this object by name
     * 
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbLookupByName(EdaContext xContext)
    throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupNameStatement(xContext);

	try {
	    getStatement().setString(1, getName());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(this.getClass().getName(),
						 "dbLookupByName()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	if (! selectSingleRow(xContext)) {
	    IcofException ie = new IcofException(this.getClass().getName(),
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
     * @param xContext An application context object.
     * @throws Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext)
    throws IcofException {

	// Get the next id for this new row.
	setNextIdStatement(xContext);
	long id = getNextBigIntId(xContext);
	closeStatement(xContext);

	// Create the SQL query in the PreparedStatement.
	setAddRowStatement(xContext);
	try {
	    getStatement().setLong(1, id);
	    getStatement().setShort(2, getPlatform().getId());
	    getStatement().setString(3, getName());
	    getStatement().setInt(4, getSpinLevel());
	    getStatement().setInt(5, getPatchLevel());
	    getStatement().setLong(6, getEvents().getId());

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
	setId(id);
	dbLookupById(xContext);

    }


    /**
     * Populate this object from the result set.
     * 
     * @param xContext Application context.
     * @param rs A valid result set.
     * @throws IcofException
     * @throws IcofException
     * @throws Trouble retrieving the data.
     */
    protected void populate(EdaContext xContext, ResultSet rs)
    throws SQLException, IcofException {

	setId(rs.getInt(ID_COL));
	setPlatform(xContext, rs.getShort(PLATFORM_ID_COL));
	setName(rs.getString(PACKAGE_NAME_COL));
	setSpinLevel(rs.getInt(SPIN_LEVEL_COL));
	setPatchLevel(rs.getInt(PATCH_LEVEL_COL));
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
	buffer.append("Platform ID : " + getPlatform().getId() + "\n");
	buffer.append("Package Name: " + getName() + "\n");
	buffer.append("Spin Level  : " + getSpinLevel() + "\n");
	buffer.append("Patch Level : " + getPatchLevel() + "\n");
	buffer.append("Events ID   : " + getEvents().getId() + "\n");

	return buffer.toString();

    }


    /**
     * Get a key from the ID.
     * 
     * @param xContext Application context.
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
    public Vector<ComponentPackage_Db> dbLookupByEventName(EdaContext xContext,
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
	Vector<ComponentPackage_Db> packages = new Vector<ComponentPackage_Db>();
	try {
	    while (rs.next()) {
		long anId = rs.getLong(ID_COL);
		Platform_Db platform = new Platform_Db(
						       rs.getShort(PLATFORM_ID_COL));
		String name = rs.getString(PACKAGE_NAME_COL);
		int spinLevel = rs.getInt(SPIN_LEVEL_COL);
		int patchLevel = rs.getInt(PATCH_LEVEL_COL);
		long eventsId = rs.getLong(EVENTS_ID_COL);

		ComponentPackage_Db pkg = new ComponentPackage_Db(anId,
								  platform,
								  name,
								  spinLevel,
								  patchLevel,
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
     * Create a collection of ComponentPackage_Db objects
     * 
     * @param xContext   Application context
     * @param aToolKit   Tool kit to look up
     * @param aComponent Component to look up
     * @param aPlat      Platform to look up
     * @param bReverse   Reverse sort the results
     * @return Collection of ComponentPackage_Db objects
     * @throws IcofException
     */
    public List<ComponentPackage_Db> dbLookupPkgs(EdaContext xContext,
                                                  RelVersion_Db aToolKit,
                                                  Component_Db aComponent,
                                                  Platform_Db aPlat,
                                                  boolean bReverse)
    throws IcofException {

	// Look up the Component_Version_Db for this tk and component
	Component_Version_Db aCompVer = new Component_Version_Db(xContext,
								 aToolKit,
								 aComponent);
	aCompVer.dbLookupByCompRelVersion(xContext);
	
	return dbLookupPackages(xContext, aCompVer, aPlat, bReverse);

    }


    /**
     * Create a collection of ComponentPackage_Db objects
     * 
     * @param xContext Application context
     * @param aCompVer Component_Version_Db to look up
     * @param aPlat    Platform_Db to lookup
     * @param bReverse Reverse sort the results
     * @return Collection of ComponentPackate_Db objects
     * @throws IcofException 
     */
    public List<ComponentPackage_Db> dbLookupPackages(EdaContext xContext,
                                                      Component_Version_Db aCompVer,
                                                      Platform_Db aPlat,
                                                      boolean bReverse) 
                                                      throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupPackagesStatement(xContext, bReverse);
	
	try {
	    getStatement().setLong(1, aCompVer.getId());
	    getStatement().setShort(2, aPlat.getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupPackages()",
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
	List<ComponentPackage_Db> packages = new ArrayList<ComponentPackage_Db>();
	try {
	    while (rs.next()) {
		long anId = rs.getLong(ID_COL);
		ComponentPackage_Db pkg = new ComponentPackage_Db(anId);
		pkg.dbLookupById(xContext);
		packages.add(pkg);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupPackages()", IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return packages;

    }


    /**
     * Determine if this component pkg is a member of the specified tk pkg
     *
     * @param xContext Application context
     * @param tkPkg    Candidate tk pkg
     * @throws IcofException 
     */
    public boolean dbLookupInTkPkg(EdaContext xContext, 
                                   ToolKitPackage_Db tkPkg) 
                                  throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setLookupInTkPkgStatement(xContext);

	try {
	    getStatement().setLong(1, tkPkg.getId());
	    getStatement().setLong(2, getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
						 this.getClass().getName(),
						 "dbLookupInTkPkg()",
						 IcofException.SEVERE,
						 "Unable to prepare SQL statement.",
						 IcofException.printStackTraceAsString(trap)
						 + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query
	boolean found = false;
	ResultSet rs = executeQuery(xContext);

	// Process the results
	long count = 0;
	try {
	    while (rs.next()) {
		long id = rs.getLong(ID_COL);
		count = id;
	    }
	    if (count > 0) 
		found = true;
	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupInTkPkg()",
				    IcofException.SEVERE,
				    "Error reading DB query results.",
				    ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);
	
	//System.out.println("Found Comp Pkg for TK Pkg: " + found);
	
	return found;
	
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
     * Lookup packages by tool kit
     *
     * @param xContext Application context
     * @param toolKit  Tool kit in question
     * @return
     * @throws IcofException 
     */
    public List<ComponentPackage_Db> dbLookupPackages(EdaContext xContext,
                                                      RelVersion_Db toolKit) 
                                                      throws IcofException {
	
	// Create the SQL query in the PreparedStatement.
	setLookupByTkStatement(xContext);

	try {
	    getStatement().setLong(1, toolKit.getId());
	}
	catch (SQLException trap) {
	    IcofException ie = new IcofException(
	                                         this.getClass().getName(),
	                                         "dbLookupPackages()",
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
	List<ComponentPackage_Db> packages = new ArrayList<ComponentPackage_Db>();
	try {
	    while (rs.next()) {
		long anId = rs.getLong(ID_COL);
		ComponentPackage_Db pkg = new ComponentPackage_Db(anId);
		pkg.dbLookupById(xContext);
		packages.add(pkg);
	    }

	}
	catch (SQLException ex) {
	    throw new IcofException(this.getClass().getName(),
	                            "dbLookupPackages()", IcofException.SEVERE,
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return packages;

    }


}
