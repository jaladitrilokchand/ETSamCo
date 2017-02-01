/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* FILE: CodeUpdate.java
*
*-PURPOSE---------------------------------------------------------------------
* Component_TkPackage DB class
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 06/30/2010 GFS  Initial coding.
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


public class Component_TkPackage_Db extends TkAudit {
	
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.COMPONENT_TKPACKAGE";
    public static final String ID_COL = "COMPONENT_TKPACKAGE_ID";
    public static final String MAINT_LEVEL_COL = "MAINTLEVEL";
    public static final String URGENT_LEVEL_COL = "URGENTLEVEL";
    public static final String COMP_VERSION_ID_COL = "COMPONENT_TKVERSION_ID";
    public static final String PLATFORM_ID_COL = "PLATFORM_ID";
    public static final String DESTINATION_ID_COL = "DESTINATION_ID";
    public static final String PACKAGED_BY_COL = "PACKAGED_BY";
    public static final String PACKAGED_ON_COL = "PACKAGED_TMSTMP";

    public static final String ALL_COLS = ID_COL + "," + 
                                          MAINT_LEVEL_COL + "," + URGENT_LEVEL_COL + "," +
                                          COMP_VERSION_ID_COL + "," +
                                          DESTINATION_ID_COL + "," +
                                          PLATFORM_ID_COL + "," +
                                          PACKAGED_BY_COL + "," +
                                          PACKAGED_ON_COL;
    private static final long serialVersionUID = -8046234252072364933L;

    
    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public Component_TkPackage_Db(long anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes a TkComponent object, SVN revision and a 
     * branch name.
     * 
     * @param aCompVer     Component_TkVersion_Db object
     * @param aPlatform    Platform package built for
     * @param aDestionation The package destination
     * @param aMaintLevel  The maintenance level
     * @param aUrgentLevel The urgent level
     */
    public Component_TkPackage_Db(Component_Version_Db aCompVer,
                                  Platform_Db aPlatform,
                                  DestinationName_Db aDestination,
                                  int aMaintLevel,  
                                  int aUrgentLevel) {
        setCompVersion(aCompVer);
        setPlatform(aPlatform);
        setDestination(aDestination);
        setMaintLevel(aMaintLevel);
        setUrgentLevel(aUrgentLevel);
    }

    
    /**
     * Constructor - takes all members.
     * 
     * @param aCompVer      Component_TkVersion_Db object
     * @param aPlatform     Platform package built for
     * @param aDestionation The package destination
     * @param aMaintLevel   The maintenance level
     * @param aUrgentLevel  The urgent level
     * @param packedBy      Name of user packaging this update
     * @param packedOn      Timestamp of package creation
     */
    public Component_TkPackage_Db(Component_Version_Db aCompVer,
                                  Platform_Db aPlatform,
                                  DestinationName_Db aDestination,
                                  int aMaintLevel,  
                                  int aUrgentLevel,
                                  String packedBy,
                                  Timestamp packedOn) {
        setCompVersion(aCompVer);
        setPlatform(aPlatform);
        setDestination(aDestination);
        setMaintLevel(aMaintLevel);
        setUrgentLevel(aUrgentLevel);
        setPackagedBy(packedBy);
        setPackagedOn(packedOn);
    }

    
    /**
     * Data Members
     */
    private long id;
    private int maintLevel;
    private int urgentLevel;
    private Platform_Db platform;
    private DestinationName_Db destination;
    private Component_Version_Db compVersion;
    private String packagedBy;
    private Timestamp packagedOn;
    
    
    /**
     * Getters
     */
    public long getId() { return id; }
    public int getMaintLevel() { return maintLevel; }
    public int getUrgentLevel() { return urgentLevel; }
    public Platform_Db getPlatform() { return platform; }
    public DestinationName_Db getDestination() { return destination; }
    public Component_Version_Db getCompVersion() { return compVersion; }
    public String getPackagedBy() { return packagedBy; }
    public Timestamp getPackagedOn() { return packagedOn; }


    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setMaintLevel(int aLvl) { maintLevel = aLvl; }
    private void setUrgentLevel(int aLvl) { urgentLevel = aLvl; }
    private void setPlatform(Platform_Db aPlat) { platform = aPlat; }
    private void setDestination(DestinationName_Db aDest) { destination = aDest; }
    private void setCompVersion(Component_Version_Db aCompVer) { compVersion = aCompVer; }
    private void setPackagedBy(String aUser) { packagedBy = aUser; }
    private void setPackagedOn(Timestamp aTms) { packagedOn = aTms; }
    private void setCompVersion(EdaContext xContext, short compId) { 
    	compVersion = new Component_Version_Db(compId);
    }
    private void setPlatform(EdaContext xContext, short anId) { 
    	platform = new Platform_Db(anId);
    }
    private void setDestination(EdaContext xContext, int anId) { 
    	destination = new DestinationName_Db(anId);
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
                       " where " + ID_COL + " = ? ";
        
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
                       " ( " +  ALL_COLS + " ) " +
                       " values( ?, ?, ?, ?, ?, ?, ?, ? )";
        
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
     * Insert a new row.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext)
    throws IcofException{

    	// Get the next id for this new row.
    	setNextIdStatement(xContext);
    	long id = getNextBigIntId(xContext);
    	closeStatement(xContext);
    	
    	// Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        try {
            getStatement().setLong(1, id);
            getStatement().setInt(2, getMaintLevel());
            getStatement().setInt(3, getUrgentLevel());
            getStatement().setLong(4, getCompVersion().getId());
            getStatement().setInt(5, getDestination().getId());
            getStatement().setShort(6, getPlatform().getId());
            getStatement().setString(7, getPackagedBy());
            getStatement().setTimestamp(8, getPackagedOn());   

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
    
        setId(rs.getInt(ID_COL));
        setMaintLevel(rs.getInt(MAINT_LEVEL_COL));
        setUrgentLevel(rs.getInt(URGENT_LEVEL_COL));
        setCompVersion(xContext, rs.getShort(COMP_VERSION_ID_COL));
        setPlatform(xContext, rs.getShort(PLATFORM_ID_COL));
        setDestination(xContext, rs.getInt(DESTINATION_ID_COL));
        setPackagedBy(rs.getString(PACKAGED_BY_COL));
        setPackagedOn(rs.getTimestamp(PACKAGED_ON_COL));
        setLoadFromDb(true);

    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID: " + getId() + "\n");
        buffer.append("Component Version ID: " + getCompVersion().getId() + "\n");
        buffer.append("Platform ID: " + getPlatform().getId() + "\n");
        buffer.append("Destination ID: " + getDestination().getId() + "\n");
        buffer.append("Maint Level: " + getMaintLevel() + "\n");
        buffer.append("Urgent Level: " + getUrgentLevel() + "\n");
        buffer.append("Packaged by; " + getPackagedBy() + "\n");
        buffer.append("Packaged on; " + getPackagedOn() + "\n");

        return buffer.toString();
        
    }

    
    /**
     * Get a key from the ID.
     * 
     * @param xContext  Application context.
     */
    public String getIdKey(EdaContext xContext) {
        return String.valueOf(getId());
        
    }


}
