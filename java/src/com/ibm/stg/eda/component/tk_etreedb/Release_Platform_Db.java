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
* TkRelease X TkPlatform DB class with audit info
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 07/15/2010 GFS  Initial coding.
* 07/22/2010 GFS  Converted to using PreparedStatements.
* 10/25/2010 GFS  Added dbLookupPlatforms() and dbLookupReleases()
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


public class Release_Platform_Db extends TkAudit {

    /**
     * 
     */
    private static final long serialVersionUID = 1673006816187108101L;
    
    
    /**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.TKRELEASE_X_PLATFORM";
    public static final String RELEASE_ID_COL = "TKRELEASE_ID";
    public static final String PLATFORM_ID_COL = "PLATFORM_ID";
    public static final String ALL_COLS = RELEASE_ID_COL + "," + 
                                          PLATFORM_ID_COL;

    
    /**
     * Constructor - takes TkRelease and Platform objects.
     * 
     * @param aCodeUpdate  A CodeUpdate object
     * @param aFuncUpdate  A FunctionalUpdate object
     */
    public Release_Platform_Db(Release_Db aRelease, 
                                Platform_Db aPlatform) {
        setRelease(aRelease);
        setPlatform(aPlatform);
    }

    
    /**
     * Data Members
     */
    private Release_Db release;
    private Platform_Db platform;
  
    
    /**
     * Getters
     */
    public Release_Db getRelease() { return release; }
    public Platform_Db getPlatform() { return platform; }

    
    /**
     * Setters
     */
    private void setRelease(Release_Db aRelease) { release = aRelease; }
    private void setPlatform(Platform_Db aPlatform) { platform = aPlatform; }
    private void setRelease(EdaContext xContext, short anId) { 
    	release = new Release_Db(anId);
    }
    private void setPlatform(EdaContext xContext, short anId) { 
    	platform = new Platform_Db(anId);
    }

    
    /**
     * Create a PreparedStatement to lookup this object by id.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupIdsStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + RELEASE_ID_COL + " =  ? AND " +  
                       PLATFORM_ID_COL + " = ? ";
        
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
    public void setLookupReleasesStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + PLATFORM_ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    

    /**
     * Create a PreparedStatement to lookup objects by Platform.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupPlatformsStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + RELEASE_ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);

    }


    /**
     * Create a PreparedStatement to add a row.
     * 
     * @param xContext  Application context.
     * @throws IcofException
     */
    public void setAddRowStatement(EdaContext xContext) throws IcofException {

	// Define the query.
	String query = "insert into " + TABLE_NAME + " ( " + ALL_COLS + " )"
	+ " values( ?, ? )";

	// Set and prepare the query and statement.
	setQuery(xContext, query);

    }


    /**
     * Insert a new row.
     * 
     * @param xContext  An application context object.
     * @throws Trouble querying the database.
     */
    public boolean dbAddRow(EdaContext xContext) throws IcofException {

	// Create the SQL query in the PreparedStatement.
	setAddRowStatement(xContext);

	try {
	    getStatement().setLong(1, getRelease().getId());
	    getStatement().setLong(2, getPlatform().getId());

	} 
	catch (SQLException trap) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "dbAddRow()", IcofException.SEVERE,
	                                         "Unable to prepare SQL statement.", IcofException
	                                         .printStackTraceAsString(trap)
	                                         + "\n" + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Run the query.
	boolean value = insertRow(xContext);
	if (!value) {
	    IcofException ie = new IcofException(this.getClass().getName(),
	                                         "dbAddRow()", IcofException.SEVERE,
	                                         "Unable to insert new row.\n", "QUERY: " + getQuery());
	    xContext.getSessionLog().log(ie);
	    throw ie;
	}

	// Close the PreparedStatement.
	closeStatement(xContext);
	
	return value;
	
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
        	getStatement().setShort(1, getRelease().getId());
            getStatement().setShort(2, getPlatform().getId());
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
     * Create a list of platforms for this release.
     * 
     * @param xContext  An application context object.
     * @return          Collection of TkPlatform objects for this release.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,Platform_Db> dbLookupPlatforms(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupPlatformsStatement(xContext);
        
        try {
            getStatement().setShort(1, getRelease().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupPlatforms()",
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
        Hashtable<String,Platform_Db> platforms = new Hashtable<String,Platform_Db>();
        try {
            while (rs.next()) {
                short anId = rs.getShort(PLATFORM_ID_COL);
                Platform_Db platform = new Platform_Db(anId);
                platform.dbLookupById(xContext);
                platforms.put(platform.getIdKey(xContext), platform);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupPlatforms()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);

        return platforms;
        
    }


    /**
     * Create a list of releases for this platform.
     * 
     * @param xContext  An application context object.
     * @return          Collection of TkPlatform objects for this release.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,Release_Db>  dbLookupReleases(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupReleasesStatement(xContext);
        
        try {
            getStatement().setShort(1, getPlatform().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupReleases()",
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
        Hashtable<String,Release_Db> releases = new Hashtable<String,Release_Db> ();
        try {
            while (rs.next()) {
                short anId = rs.getShort(1);
                Release_Db release = new Release_Db(anId);
                release.dbLookupById(xContext);
                releases.put(release.getIdKey(xContext), release);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupReleases()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);

        return releases;
        
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
        
        setRelease(xContext, rs.getShort(RELEASE_ID_COL));
        setPlatform(xContext, rs.getShort(PLATFORM_ID_COL));
        setLoadFromDb(true);

    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        if (getRelease() != null) {
            buffer.append("Release ID: " + getRelease().getId() + "\n");
            buffer.append("Release name: " + getRelease().getName() + "\n");
        }
        else {
            buffer.append("Release ID: NULL\n");
            buffer.append("Release name: NULL\n");
        }

        if (getPlatform() != null) {
            buffer.append("Platform ID: " + getPlatform().getId() + "\n");
            buffer.append("Platform name: " + getPlatform().getName() + "\n");
        }
        else {
            buffer.append("Platform ID: NULL\n");
            buffer.append("Platform name: NULL\n");
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
        return String.valueOf(getRelease().getIdKey(xContext) + "_" +
        		              getPlatform().getIdKey(xContext));
    }

    
 }

