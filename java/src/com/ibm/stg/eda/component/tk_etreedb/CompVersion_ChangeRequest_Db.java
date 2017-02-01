/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * FILE: TkFunctionalUpdate.java
 *
 *-PURPOSE---------------------------------------------------------------------
 * ComponentVersion X ChangeRequest DB class with audit info
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 03/08/2011 GFS  Initial coding.
 * 05/18/2011 GFS  Updated to use base class query and statement members. Added
 *                 dbUpdateCompVersion() method.
 * 08/06/2012 GFS  Updated Component_Tk_Version_Id from short to long.
 * 08/23/2012 GFS  Added dbDeleteRowCrs() method.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;


public class CompVersion_ChangeRequest_Db extends TkAudit {

    /**
     * 
     */
    private static final long serialVersionUID = -6719109090582585879L;
    
    
    /**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.COMPONENT_TKVERSION_X_CHANGEREQUEST";
    public static final String CHANGE_REQUEST_ID_COL = "CHANGEREQUEST_ID";
    public static final String COMPONENT_VERSION_COL = "COMPONENT_TKVERSION_ID";
    public static final String ALL_COLS = CHANGE_REQUEST_ID_COL + "," + 
                               COMPONENT_VERSION_COL;


    /**
     * Constructor - takes database ids
     * 
     * @param aChangeRequestId  A ChangeRequest database object id
     * @param aCompVerId        A ComponentVersion database object id
     */
    public CompVersion_ChangeRequest_Db(EdaContext xContext, 
                                        long aChangeRequestId, 
                                        long aCompVerId) 
                                        throws IcofException {
	setCompVersion(xContext, aCompVerId);
	setChangeRequest(xContext, aChangeRequestId);
    }


    /**
     * Constructor - takes database objects
     * 
     * @param aChangeRequest  A ChangeRequest object
     * @param aCompVersion    A Component_Version_Db object
     * @param aStatus         A ChangeRequest Status DB object
     */
    public CompVersion_ChangeRequest_Db(ChangeRequest_Db aChangeRequest,
                                        Component_Version_Db aCompVersion) {
	setCompVersion(aCompVersion);
	setChangeRequest(aChangeRequest);
    }


    /**
     * Data Members
     */
    private Component_Version_Db compVersion;
    private ChangeRequest_Db changeRequest;

    
    /**
     * Getters
     */
    public Component_Version_Db getCompVersion() { return compVersion; }
    public ChangeRequest_Db getChangeRequest() { return changeRequest; }
    

    /**
     * Setters
     */
    private void setCompVersion(Component_Version_Db anUpdate) { compVersion = anUpdate; }
    private void setChangeRequest(ChangeRequest_Db anUpdate) { changeRequest = anUpdate; }
        private void setCompVersion(EdaContext xContext, long anId) { 
	compVersion = new Component_Version_Db(anId);
    }
    private void setChangeRequest(EdaContext xContext, long anId) { 
	changeRequest = new ChangeRequest_Db(anId);
    }
 

    /**
     * Create a PreparedStatement to lookup this object by ids.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupIdsStatement(EdaContext xContext) throws IcofException {

	// Define the query.
	String query = "select " + ALL_COLS + 
	               " from " + TABLE_NAME + 
	               " where " + 
	               COMPONENT_VERSION_COL + " = ? AND " + 
	               CHANGE_REQUEST_ID_COL + " = ? ";

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
	               " values( ?, ? )";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup the ChangeRequests.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupChangeRequestsStatement(EdaContext xContext) 
    throws IcofException {

	// Define the query.
	String query = "select " + CHANGE_REQUEST_ID_COL + 
	               " from " + TABLE_NAME +
	               " where " + COMPONENT_VERSION_COL + " = ? "; 

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup the CompVersions.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupCompVersionsStatement(EdaContext xContext) 
    throws IcofException {

	// Define the query.
	String query = "select " + COMPONENT_VERSION_COL + 
	               " from " + TABLE_NAME + 
	               " where " + CHANGE_REQUEST_ID_COL + " = ? "; 

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to lookup the CompVersions.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setUpdateCompVersionStatement(EdaContext xContext) 
    throws IcofException {

	// Define the query.
	String query = "update " + TABLE_NAME +
	               "   set " + COMPONENT_VERSION_COL + " = ? " +
	               " where " + COMPONENT_VERSION_COL + " = ? " +
	               "   and " + CHANGE_REQUEST_ID_COL + " = ? "; 

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
    public void setDeleteCrsStatement(EdaContext xContext) throws IcofException {

	// Define the query.
	String query = "delete from " + TABLE_NAME + 
	               " where " + CHANGE_REQUEST_ID_COL + " = ? ";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Look up this object by id.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByIds(EdaContext xContext) throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setLookupIdsStatement(xContext);

	try {
	    getStatement().setLong(1, getCompVersion().getId());
	    getStatement().setLong(2, getChangeRequest().getId());
	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "dbLookupByIds()",
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
	                                         "dbLookupByIds()",
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

	// Create the SQL query in the PreparedStatement.
	setAddRowStatement(xContext);

	try {
	    getStatement().setLong(1, getChangeRequest().getId());
	    getStatement().setLong(2, getCompVersion().getId());

	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "dbAddRow()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap) + 
	                                         "\n" + getStatement().toString());
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
	setLoadFromDb(true);

    }


    /**
     * Create a list of ChangeRequest object for this CompVersion.
     * 
     * @param xContext  An application context object.
     * @return          Collection of ChangeRequest_Db objects for this code update.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,ChangeRequest_Db> dbLookupChangeRequests(EdaContext xContext) throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setLookupChangeRequestsStatement(xContext);

	try {
	    getStatement().setLong(1, getCompVersion().getId());
	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "dbLookupChangeRequests()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap) + 
	                                         "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Hashtable<String,ChangeRequest_Db> changeRequests = new Hashtable<String,ChangeRequest_Db>();
	try {
	    while (rs.next()) {
		long anId =  rs.getLong(CHANGE_REQUEST_ID_COL);
		ChangeRequest_Db cr = new ChangeRequest_Db(anId);
		cr.dbLookupById(xContext);

		changeRequests.put(cr.getIdKey(xContext), cr);
	    }

	}
	catch(SQLException ex) {
	    throw new IcofException(this.getClass().getName(), 
	                            "dbLookupChangeRequests()",
	                            IcofException.SEVERE, 
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return changeRequests;

    }


    /**
     * Create a list of CompVersion object for this ChangeRequest.
     * 
     * @param xContext  An application context object.
     * @return          Collection of ComponenteVersion_Db objects.
     * @throws          Trouble querying the database.
     */
    public Vector<Component_Version_Db>  dbLookupCompVersions(EdaContext xContext) throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setLookupCompVersionsStatement(xContext);

	try {
	    getStatement().setLong(1, getChangeRequest().getId());
	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "dbLookupCompVersions()",
	                                         IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.",
	                                         IcofException.printStackTraceAsString(trap) + 
	                                         "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	ResultSet rs = executeQuery(xContext);

	// Process the results
	Vector<Component_Version_Db> compVersions = new Vector<Component_Version_Db>();
	try {
	    while (rs.next()) {
		short anId =  rs.getShort(COMPONENT_VERSION_COL);
		Component_Version_Db cv = new Component_Version_Db(anId);
		cv.dbLookupById(xContext);

		compVersions.add(cv);
	    }

	}
	catch(SQLException ex) {
	    throw new IcofException(this.getClass().getName(), "dbLookupCompVersion()",
	                            IcofException.SEVERE, 
	                            "Error reading DB query results.",
	                            ex.getMessage());
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	return compVersions;

    }


    /**
     * Update the ComponentVersion for this object.
     * 
     * @param xContext   An application context object.
     * @param newCompVer The new ComponentVersion object
     * @throws           Trouble querying the database.
     */
    public void dbUpdateCompVersion(EdaContext xContext, 
                                    Component_Version_Db newCompVer)
                                    throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setUpdateCompVersionStatement(xContext);

	try {
	    getStatement().setLong(1, newCompVer.getId());
	    getStatement().setLong(2, getCompVersion().getId());
	    getStatement().setLong(3, getChangeRequest().getId());

	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass() .getName(),
	                                         "dbUpdateCompVersion()",
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
	                                         "dbUpdateCompVersion()",
	                                         IcofException.SEVERE,
	                                         "Unable to update row.\n",
	                                         "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Close the PreparedStatement.
	closeStatement(xContext);

	// Update the data members.
	setCompVersion(newCompVer);
	setLoadFromDb(true);

    }
 

    /**
     * Delete this object from the database
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbDeleteRowCrs(EdaContext xContext)
    throws IcofException{

	// Create the SQL query in the PreparedStatement.
	setDeleteCrsStatement(xContext);
	try {
	    getStatement().setLong(1, getChangeRequest().getId());

	}
	catch(SQLException trap) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "dbDeleteRowCrs()",
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
	                                         "dbDeleteRowCrs()",
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
     * @param xContext  Application context.
     * @param rs        A valid result set.
     * @throws IcofException 
     * @throws IcofException 
     * @throws          Trouble retrieving the data.
     */
    protected void populate(EdaContext xContext, ResultSet rs) 
    throws SQLException, IcofException {

	setCompVersion(xContext, rs.getLong(COMPONENT_VERSION_COL));
	setChangeRequest(xContext, rs.getLong(CHANGE_REQUEST_ID_COL));

	setLoadFromDb(true);

    }


    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

	// Get the class specific data
	StringBuffer buffer = new StringBuffer();
	if (getCompVersion() != null) {
	    buffer.append("Component_Version ID: " + getCompVersion().getId() + "\n");
	    buffer.append(" Component ID: " + getCompVersion().getCompRelease().getId() + "\n");
	    buffer.append(" RelVersion ID: " + getCompVersion().getVersion().getId() + "\n");
	}
	else {
	    buffer.append("Component_Version ID: NULL\n");
	}

	if (getChangeRequest() != null) {
	    buffer.append("ChangeRequest ID: " + getChangeRequest().getId() + "\n");
	    buffer.append("ChangeRequest description: " + 
	    getChangeRequest().getDescription() + "\n");
	}
	else {
	    buffer.append("ChangeRequest ID: NULL\n");
	}
	
	return buffer.toString();

    }


    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
	return String.valueOf(getCompVersion().getIdKey(xContext) + "_" +
	                      getChangeRequest().getIdKey(xContext));
    }


}
