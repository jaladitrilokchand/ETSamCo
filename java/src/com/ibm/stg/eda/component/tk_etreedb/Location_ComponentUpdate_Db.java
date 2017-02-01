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
* Location X ComponentUpdate DB class
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 02/01/2011 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;


public class Location_ComponentUpdate_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7806764823228372801L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.LOCATION_COMPONENTUPDATE";
    public static final String COMPONENT_VERSION_LOCATION_ID_COL = "COMPONENTTKVERSION_LOCATION_ID";
    public static final String COMPONENT_UPDATE_ID_COL = "COMPONENTUPDATE_ID";
    public static final String ALL_COLS = COMPONENT_VERSION_LOCATION_ID_COL + ", " + 
                                          COMPONENT_UPDATE_ID_COL + ", " +
                                          CREATED_BY_COL + ", " +
                                          CREATED_ON_COL;

    
    /**
     * Constructor - takes ComponentVersionLocation and ComponentUpdate ids
     * 
     */
    public Location_ComponentUpdate_Db(EdaContext xContext, short aCompVerLocId, 
                                       long aCompUpdateId, 
                                       String aCreatedBy, Timestamp aCreatedOn) 
    throws IcofException {
        setCompVerLocation(xContext, aCompVerLocId);
        setComponentUpdate(xContext, aCompUpdateId);
        setCreatedBy(aCreatedBy);
        setCreatedOn(aCreatedOn);
    }

    
    /**
     * Constructor - takes Component_Version_Location and ComponentUpdate objects
     * 
     */
    public Location_ComponentUpdate_Db(Component_Version_Location_Db aCompVerLoc,
                                       ComponentUpdate_Db aCompUpdate) {
    	setCompVerLocation(aCompVerLoc);
        setComponentUpdate(aCompUpdate);
    }

    
    /**
     * Data Members
     */
    private Component_Version_Location_Db compVerLocation;
    private ComponentUpdate_Db componentUpdate;
    
    
    /**
     * Getters
     */
    public Component_Version_Location_Db getCompVerLocation() { return compVerLocation; }
    public ComponentUpdate_Db getComponentUpdate() { return componentUpdate; }

    
    /**
     * Setters
     */
    private void setCompVerLocation(Component_Version_Location_Db aLocation) { compVerLocation = aLocation; }
    private void setComponentUpdate(ComponentUpdate_Db anUpdate) { componentUpdate = anUpdate; }
    private void setCompVerLocation(EdaContext xContext, short anId) { 
    	compVerLocation = new Component_Version_Location_Db(anId);
    }
    private void setComponentUpdate(EdaContext xContext, long anId) { 
            componentUpdate = new ComponentUpdate_Db(anId);
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
                       " where " + COMPONENT_UPDATE_ID_COL + " = ? AND " + 
                       COMPONENT_VERSION_LOCATION_ID_COL + " = ? ";
        
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
                       " values( ?, ?, ?, ? )";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }


    /**
     * Create a PreparedStatement to lookup objects created since a 
     * given Timestamp
     *
     * @param xContext    Application context.
     * @param aTimestamp  Timestamp criteria (may be null)
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupCreatedSinceStatement(EdaContext xContext, 
    		                                   Timestamp aTimestamp) 
    throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME +
                       " where " + COMPONENT_VERSION_LOCATION_ID_COL + " = ? ";
        
        if (aTimestamp != null) {
        	query += " and "  + CREATED_ON_COL + " > ? ";
        }
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

  
    /**
     * Create a PreparedStatement to lookup the DeliverableUpdates.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupLocationsStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "select " + COMPONENT_VERSION_LOCATION_ID_COL + 
                       " from " + TABLE_NAME +
                       " where " + COMPONENT_UPDATE_ID_COL + " = ? "; 
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

  
    /**
     * Create a PreparedStatement to lookup the CodeUpdates.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupComponentUpdatesStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "select " + COMPONENT_UPDATE_ID_COL + 
                       " from " + TABLE_NAME + 
                       " where " + COMPONENT_VERSION_LOCATION_ID_COL + " = ? " +
                       " order by " + COMPONENT_UPDATE_ID_COL; 
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
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
        	getStatement().setLong(1, getComponentUpdate().getId());
            getStatement().setLong(2, getCompVerLocation().getId());
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
     * @param creator   Person making this update
     * 
     * @throws          Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext, User_Db creator)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        Timestamp now = new Timestamp(new java.util.Date().getTime());
        try {
            getStatement().setLong(1, getCompVerLocation().getId());
        	getStatement().setLong(2, getComponentUpdate().getId());
            getStatement().setString(3, creator.getIntranetId());
            getStatement().setTimestamp(4, now);

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

        // Set data.
        setCreatedBy(creator.getIntranetId());
        setCreatedOn(now);
        setLoadFromDb(true);

        
    }

    
    /**
     * Create a list of Location objects for this ComponentUpdate.
     * 
     * @param xContext  An application context object.
     * @return          Collection of location_Db objects
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,Component_Version_Location_Db> dbLookupLocations(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupLocationsStatement(xContext);
        
        try {
            getStatement().setLong(1, getComponentUpdate().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
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
        Hashtable<String,Component_Version_Location_Db> locations = new Hashtable<String,Component_Version_Location_Db>();
        try {
            while (rs.next()) {
                long anId =  rs.getLong(COMPONENT_VERSION_LOCATION_ID_COL);
                Component_Version_Location_Db cvl = new Component_Version_Location_Db(anId);
                cvl.dbLookupById(xContext);
                
                locations.put(cvl.getIdKey(xContext), cvl);
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
     * Create a list of ComponentUpdate objects for this Location.
     * 
     * @param xContext  An application context object.
     * @return          Collection of ComponentUpdate_Db objects.
     * @throws          Trouble querying the database.
     */
    public List<ComponentUpdate_Db> dbLookupComponentUpdates(EdaContext xContext) 
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupComponentUpdatesStatement(xContext);
        
        try {
            getStatement().setLong(1, getCompVerLocation().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupComponentUpdates()",
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
        List<ComponentUpdate_Db> compUpdates = new ArrayList<ComponentUpdate_Db>();
        try {
            while (rs.next()) {
                long anId =  rs.getLong(COMPONENT_UPDATE_ID_COL);
                ComponentUpdate_Db cu = new ComponentUpdate_Db(anId);
                cu.dbLookupById(xContext);
                
                compUpdates.add(cu);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupComponentUpdates()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);

        return compUpdates;
        
    }


    /**
     * Create a list of objects created after the specified timestamp.
     * 
     * @param xContext   An application context object.
     * @param aTimestamp Timestamp to query for
     * @return           Collection of Location_ComponentUpdate_Db objects
     * @throws           Trouble querying the database.
     */
    public Vector<Location_ComponentUpdate_Db> dbLookupCreatedSince(EdaContext xContext, Timestamp aTimestamp) 
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupCreatedSinceStatement(xContext, aTimestamp);
        
        try {
            getStatement().setLong(1, getCompVerLocation().getId());
            if (aTimestamp != null) {
            	getStatement().setTimestamp(2, aTimestamp);
            }
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupCreatedSince()",
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
        Vector<Location_ComponentUpdate_Db> objects = new Vector<Location_ComponentUpdate_Db>();
        try {
            while (rs.next()) {
            	short cvlId =  rs.getShort(COMPONENT_VERSION_LOCATION_ID_COL);
            	long cuId =  rs.getLong(COMPONENT_UPDATE_ID_COL);
            	String aCreatedBy =  rs.getString(CREATED_BY_COL);
            	Timestamp aCreatedOn =  rs.getTimestamp(CREATED_ON_COL);
                Location_ComponentUpdate_Db object = 
                	new Location_ComponentUpdate_Db(xContext, 
                	                                cvlId, cuId,
                	                                aCreatedBy, aCreatedOn);
                setLoadFromDb(true);
                objects.add(object);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupCreatedSince()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);

        return objects;
        
    }



    /**
     * Populate this object from the result set.
     * 
     * @param xContext  Application context.
     * @param rs        A valid result set.
     * @throws          Trouble retrieving the data.
     */
    protected void populate(EdaContext xContext, ResultSet rs) 
    throws SQLException, IcofException {
        
        setCompVerLocation(xContext, rs.getShort(COMPONENT_UPDATE_ID_COL));
        setComponentUpdate(xContext, rs.getShort(COMPONENT_VERSION_LOCATION_ID_COL));
        setCreatedBy(rs.getString(CREATED_BY_COL));

        // TODO Change to use audit info.
        setCreatedOn(rs.getTimestamp(CREATED_ON_COL));

        setLoadFromDb(true);

    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {
    	
        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        
        if (getCompVerLocation() != null) {
            buffer.append("Location: " + getCompVerLocation().getLocation().getName() + "\n");
            buffer.append("Component: " + getCompVerLocation().getComponentVersion().getCompRelease().getComponent().getName() + "\n");
            buffer.append("ToolKit: " + getCompVerLocation().getComponentVersion().getVersion().getDisplayName() + "\n");
        }
        else {
            buffer.append("Location: NULL\n");
            buffer.append("Component: NULL\n");
            buffer.append("ToolKit: NULL\n");
        }

        if (getComponentUpdate() != null) {
            buffer.append("ComponentUpdate ID: " + getComponentUpdate().getId() + "\n");
        }
        else {
            buffer.append("ComponentUpdate ID: NULL\n");
        }

        buffer.append("Created by: " + getCreatedBy() + "\n");
        if (getCreatedOn() == null) {
            buffer.append("Created on: null\n");
        }
        else {
            buffer.append("Created on: " + getCreatedOn().toString() + "\n");
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
        return String.valueOf(getComponentUpdate().getIdKey(xContext) + "_" +
        		              getCompVerLocation().getIdKey(xContext));
    }


}

