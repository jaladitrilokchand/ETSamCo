/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* FILE: Component_Version_Location_Db.java
*
*-PURPOSE---------------------------------------------------------------------
* Component_Version X Location DB class
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 02/02/2011 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;


public class Component_Version_Location_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2406921556057544680L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.COMPONENTTKVERSION_LOCATION";
    public static final String ID_COL = "COMPONENTTKVERSION_LOCATION_ID";
    public static final String COMPONENT_VERSION_ID_COL = "COMPONENT_TKVERSION_ID";
    public static final String LOCATION_ID_COL = "LOCATION_ID";
    public static final String ALL_COLS = ID_COL + ", " +
                                          COMPONENT_VERSION_ID_COL + ", " + 
                                          LOCATION_ID_COL;

    
    /**
     * Constructor - take a row id
     * 
     * @param anId  A database id
     */
    public Component_Version_Location_Db(long anId) {
        setId(anId);
    }

   
    /**
     * Constructor - takes ComponentVersion and Location ids
     * 
     * @param anId  A database id
     */
    public Component_Version_Location_Db(EdaContext xContext, 
                                         short aCompVersionId, short aLocationId) 
    throws IcofException {
        setComponentVersion(xContext, aCompVersionId);
        setLocation(xContext, aLocationId);
    }

    
    /**
     * Constructor - takes ComponentVersion and Location objects.
     * 
     * @param aComponentVersion  A ComponentVersion_Db object
     * @param aLocation          A Location_Db object
     */
    public Component_Version_Location_Db(Component_Version_Db anUpdate, 
    		                             Location_Db aLocation) {
        setComponentVersion(anUpdate);
        setLocation(aLocation);
    }

    
    /**
     * Data Members
     */
    private long id;
    private Component_Version_Db compVersion;
    private Location_Db location;

    
    /**
     * Getters
     */
    public long getId() { return id; }
    public Component_Version_Db getComponentVersion() { return compVersion; }
    public Location_Db getLocation() { return location; }
    
    
    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setComponentVersion(Component_Version_Db aCompVer) { compVersion = aCompVer; }
    private void setLocation(Location_Db aLoc) { location = aLoc; }
    private void setComponentVersion(EdaContext xContext, short anId) { 
    	compVersion = new Component_Version_Db(anId);
    }
    private void setLocation(EdaContext xContext, short anId) { 
    	location = new Location_Db(anId);
    }
    
    
    /**
     * Create a PreparedStatement to lookup this object by id.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupByIdStatement(EdaContext xContext) throws IcofException {
        
        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
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
                       " where " + COMPONENT_VERSION_ID_COL + " = ? AND " + 
                       LOCATION_ID_COL + " = ? ";
        
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
     * Create a PreparedStatement to lookup the ComponentVersion.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupComponentVersionsStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "select " + ID_COL + 
                       " from " + TABLE_NAME + 
                       " where " + LOCATION_ID_COL + " = ? "; 
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup the Locations.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupLocationsStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "select " + ID_COL + 
                       " from " + TABLE_NAME + 
                       " where " + COMPONENT_VERSION_ID_COL + " = ? "; 
        
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
     * Look up this object by id.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupById(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupByIdStatement(xContext);
        
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
     * Look up this object by ids.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByIds(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupIdsStatement(xContext);
        
        try {
            getStatement().setLong(1, getComponentVersion().getId());
            getStatement().setShort(2, getLocation().getId());
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
    throws IcofException {

    	// Get the next id for this new row.
    	setNextIdStatement(xContext);
    	long id = getNextBigIntId(xContext);
    	closeStatement(xContext);
    	
        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        try {
        	getStatement().setLong(1, id);
        	getStatement().setLong(2, getComponentVersion().getId());
        	getStatement().setShort(3, getLocation().getId());

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
        setId(id);
        dbLookupById(xContext); 
        
    }

    
    /**
     * Create a list of objects for this Location
     * 
     * @param xContext  An application context object.
     * @return          Collection of Component_Version_Location_Db objects.
     * @throws          Trouble querying the database.
     */
    public Hashtable <String,Component_Version_Location_Db>dbLookupComponentVersions(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupComponentVersionsStatement(xContext);
        
        try {
            getStatement().setShort(1, getLocation().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupComponentVersions()",
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
        Hashtable<String,Component_Version_Location_Db> compVerLocs = new Hashtable<String,Component_Version_Location_Db>();
        try {
            while (rs.next()) {
                short anId =  rs.getShort(COMPONENT_VERSION_ID_COL);
                Component_Version_Location_Db cvl = new Component_Version_Location_Db(anId);
                cvl.dbLookupById(xContext);
                
                compVerLocs.put(cvl.getIdKey(xContext), cvl);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupComponentVersions()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return compVerLocs;
        
    }


    /**
     * Create a list of objects for this ComponentVersion.
     * 
     * @param xContext  An application context object.
     * @return          Collection of Component_Version_Location_Db objects.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,Location_Db> dbLookupLocations(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupLocationsStatement(xContext);
        
        try {
            getStatement().setShort(1, getLocation().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass().getName(),
                                                 "dbLookupLocations()",
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
        Hashtable<String,Location_Db> locations = new Hashtable<String,Location_Db>();
        try {
            while (rs.next()) {
                short anId =  rs.getShort(LOCATION_ID_COL);
                Location_Db loc = new Location_Db(anId);
                loc.dbLookupById(xContext);
                
                locations.put(loc.getIdKey(xContext), loc);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupLocations()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return locations;
        
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
        
    	setId(rs.getLong(ID_COL));
        setComponentVersion(xContext, rs.getShort(COMPONENT_VERSION_ID_COL));
        setLocation(xContext, rs.getShort(LOCATION_ID_COL));
        setLoadFromDb(true);

    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID: " + getId() + "\n");
        if (getComponentVersion() != null) {
            buffer.append("ComponentVersion ID: " + getComponentVersion().getId() + "\n");
        }
        else {
            buffer.append("ComponentVersion ID: NULL\n");
        }

        if (getLocation() != null) {
            buffer.append("Location ID: " + getLocation().getId() + "\n");
            buffer.append("Location: " + getLocation().getName() + "\n");
        }
        else {
            buffer.append("Location ID: NULL\n");
            buffer.append("Location: NULL\n");
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
        return String.valueOf(getComponentVersion().getIdKey(xContext) + "_" +
        		              getLocation().getIdKey(xContext));
    }

    
}

