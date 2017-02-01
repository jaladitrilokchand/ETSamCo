/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *    DATE: 05/18/2010
 *
 *-PURPOSE---------------------------------------------------------------------
 * TK Component DB class with audit info
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 05/18/2010 GFS  Initial coding.
 * 07/23/2010 GFS  Converted to using PreparedStatements.
 * 08/11/2010 GFS  Added dbAddRow() method.
 * 10/05/2010 GFS  Added dbUpdateRow() and a new constructor.
 * 11/11/2010 GFS  Removed builder, transmitter and team lead role information.
 * 01/06/2011 GFS  Renamed class from RelComponent_Db to Component_Release_Db.
 * 04/14/2011 GFS  Added dbDeleteRow() method.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;

public class Component_Release_Db extends TkAudit {

    /**
     * 
     */
    private static final long serialVersionUID = -7800663703364827672L;
    /**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.COMPONENT_TKRELEASE";
    public static final String ID_COL = "COMPONENT_TKRELEASE_ID";
    public static final String COMP_ID_COL = "COMPONENT_ID";
    public static final String REL_ID_COL = "TKRELEASE_ID";
    public static final String ALL_COLS = ID_COL + "," + 
    COMP_ID_COL + "," + REL_ID_COL + "," +
    CREATED_BY_COL + "," + CREATED_ON_COL + "," + 
    UPDATED_BY_COL + "," + UPDATED_ON_COL + "," +
    DELETED_BY_COL + "," + DELETED_ON_COL;


    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public Component_Release_Db(short anId) {
	setId(anId);
    }


    /**
     * Constructor - takes TK Release and Component objects.
     * 
     * @param aRelease        A TkRelease object
     * @param aComponent      A TkComponent object
     */
    public Component_Release_Db(Release_Db aRelease, Component_Db aComponent) {
	setComponent(aComponent);
	setRelease(aRelease);
    }


    /**
     * Constructor - takes TK Release and Component objects.
     * 
     * @param anId            An id
     * @param aRelease        A TkRelease object
     * @param aComponent      A TkComponent object
     * @param aBuilderId      Id of a Builder(user) database row
     * @param aTeamLeadId     Id of a Team Lead(user) database row
     * @param aTransmitterId  Id of a Transmitter(user) database row
     */
    public Component_Release_Db(short anId, Release_Db aRelease, Component_Db aComponent) {
	setId(anId);
	setComponent(aComponent);
	setRelease(aRelease);
    }



    /**
     * Constructor - takes TK Release and Component IDs
     * 
     * @param xContext    Application context object.
     * @param releaeId    Id of a TkRelease database row
     * @param componentId Id of a Component database row
     *
     * @throws Trouble looking up the TkRelease or Component objects
     */
    public Component_Release_Db(EdaContext xContext, short releaseId, short componentId)
    throws IcofException {
	setRelease(xContext, releaseId);
	setComponent(xContext, componentId);
    }


    /**
     * Data Members
     */
    private short id;
    private Release_Db release;
    private Component_Db component;


    /**
     * Getters
     */
    public short getId() { return id; }
    public Release_Db getRelease() { return release; }
    public Component_Db getComponent() { return component; }


    /**
     * Setters
     */
    private void setId(short anId) { id = anId; }
    private void setRelease(Release_Db aRel) { release = aRel; }
    private void setComponent(Component_Db aComp) { component = aComp; }
    private void setRelease(EdaContext xContext, short anId) {
	release = new Release_Db(anId);
    }
    private void setComponent(EdaContext xContext, short anId) {
	component = new Component_Db(anId);
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
	" where " + ID_COL + " = ? " +
	" AND " + DELETED_ON_COL + " is NULL";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup this object by Release and Component.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupRelCompStatement(EdaContext xContext) 
    throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + 
	" from " + TABLE_NAME + 
	" where " + COMP_ID_COL + " = ? " +
	" AND " + REL_ID_COL + " = ? " +
	" AND " + DELETED_ON_COL + " is NULL";

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
	" values( ?, ?, ?, ?, ?, ?, ?, ?, ? )";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to update a row.
     * 
     * @param  xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setUpdateRowStatement(EdaContext xContext) throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME + 
	" set " +  UPDATED_BY_COL + " = ?, " +
	UPDATED_ON_COL + " = ?" +
	" where " + ID_COL + " = ? ";

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
    public void setDeleteRowStatement(EdaContext xContext) throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME + 
	" set " + 
	DELETED_BY_COL + " = ? , " +
	DELETED_ON_COL + " = ? " +
	" where " + ID_COL + " = ? ";

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
     * Look up the Component by id.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupById(EdaContext xContext) throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setLookupIdStatement(xContext);

	try {
	    getStatement().setShort(1, getId());
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
     * Look up the Component by name.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByRelComp(EdaContext xContext) throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setLookupRelCompStatement(xContext);

	try {
	    getStatement().setShort(1, getComponent().getId());
	    getStatement().setShort(2, getRelease().getId());
	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "dbLookupByRelComp()",
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
	                                         "dbLookupByRelComp()",
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
    public void dbAddRow(EdaContext xContext, User_Db user)
    throws IcofException{

	// Get the next id for this new row.
	setNextIdStatement(xContext);
	setId(getNextSmallIntId(xContext));
	closeStatement(xContext);

	// Create the SQL query in the PreparedStatement.
	setAddRowStatement(xContext);
	Timestamp now = new Timestamp(new java.util.Date().getTime());
	try {
	    getStatement().setLong(1, getId());
	    getStatement().setShort(2, getComponent().getId());
	    getStatement().setShort(3, getRelease().getId());
	    getStatement().setString(4, user.getIntranetId());
	    getStatement().setTimestamp(5, now);
	    getStatement().setString(6, user.getIntranetId());
	    getStatement().setTimestamp(7, now);
	    getStatement().setString(8, null);
	    getStatement().setString(9, null);
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

	// Load the data for the new row.
	dbLookupById(xContext); 

    }


    /**
     * Update the builder, team lead or transmitter ids this object.
     * 
     * @param xContext  An application context object.
     * @param editor    A User object for the person making the update.
     * @throws          Trouble querying the database.
     */
    public void dbUpdateRow(EdaContext xContext, User_Db editor)
    throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setUpdateRowStatement(xContext);
	Timestamp now = new Timestamp(new java.util.Date().getTime());
	try {
	    getStatement().setString(1, editor.getIntranetId());
	    getStatement().setTimestamp(2, now);
	    getStatement().setShort(6, getId());

	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "dbUpdateRow()",
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
	                                         "dbUpdateRow()",
	                                         IcofException.SEVERE,
	                                         "Unable to update selected row.\n",
	                                         "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

    }


    /**
     * Delete (mark as deleted) this object in the database
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbDeleteRow(EdaContext xContext, User_Db editor)
    throws IcofException{

	// Create the SQL query in the PreparedStatement.
	Timestamp now = new Timestamp(new java.util.Date().getTime());
	setDeleteRowStatement(xContext);
	try {
	    getStatement().setString(1, editor.getIntranetId());
	    getStatement().setTimestamp(2, now);
	    getStatement().setLong(3, getId());

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

	// Set the delete info on this object.
	setDeletedBy(editor.getIntranetId());
	setDeletedOn(now);
	setLoadFromDb(true);

    }


    /**
     * Populate this object from the result set.
     * 
     * @param xContext  Application context.
     * @param rs        A valid result set.
     * @throws IcofException 
     * @throws SQLException
     */
    protected void populate(EdaContext xContext, ResultSet rs) 
    throws SQLException, IcofException {

	super.populate(xContext, rs);
	setId(rs.getShort(ID_COL));

	setComponent(xContext, rs.getShort(COMP_ID_COL));
	setRelease(xContext, rs.getShort(REL_ID_COL));
	setLoadFromDb(true);

    }


    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

	// Get the audit data
	String audit = super.toString(xContext);

	// Get the class specific data
	StringBuffer buffer = new StringBuffer();
	buffer.append("ID: " + getId() + "\n");
	if (getComponent() != null) {
	    buffer.append("Component ID: " + getComponent().getId() + "\n");
	    buffer.append("Component name: " + getComponent().getName() + "\n");
	}
	else {
	    buffer.append("Component ID: NULL\n");
	    buffer.append("Component name: NULL\n");
	}
	if (getRelease() != null) {
	    buffer.append("Release ID: " + getRelease().getId() + "\n");
	    buffer.append("Release name: " + getRelease().getName() + "\n");
	}
	else {
	    buffer.append("Release ID: NULL\n");
	    buffer.append("Relasse name: NULL\n");
	}
	buffer.append(audit);

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


}
