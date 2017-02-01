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
* TK Release DB class with audit info
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/18/2010 GFS  Initial coding.
* 07/22/2010 GFS  Converted to using PreparedStatements.
* 04/14/2011 GFS  Added dbAddRow and dbDeleteRow methods.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;


public class Release_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3491336729270905500L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.TKRELEASE";
    public static final String ID_COL = "TKRELEASE_ID";
    public static final String NAME_COL = "TKRELEASE_NAME";
    public static final String ALT_NAME_COL = "ALT_TKRELEASE_NAME";
    public static final String ALL_COLS = ID_COL + "," + NAME_COL + "," + ALT_NAME_COL + "," +
                                          CREATED_BY_COL + "," + CREATED_ON_COL + "," + 
                                          UPDATED_BY_COL + "," + UPDATED_ON_COL + "," +
                                          DELETED_BY_COL + "," + DELETED_ON_COL;

    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public Release_Db(short anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes a TK Release name (14.1 or 1401)
     * 
     * @param xContext  Application context object.
     * @param aName     Name of a TK Release Version (14.1, 14.2 ...)
     * 
     */
    public Release_Db(EdaContext xContext, String aName) throws IcofException {
        setName(getValidReleaseName(aName));
    }

    
    /**
     * Constructor - takes a TK Release name (14.1 or 1401) and an 
     *               alternate release name (18.1, 19.1 ...)
     * 
     * @param xContext  Application context object.
     * @param aName     TK Release name
     * @param anAltName TK Release alternate name
     * 
     */
    public Release_Db(EdaContext context, String aName, String anAltName) {
        setName(getValidReleaseName(aName));
        setAltName(getValidReleaseName(anAltName));
    }


    /**
     * Return a valid release name where a valid release is #.#.  
     * (ie convert 1401 to 14.1)
     * 
     * @param aName  TkRelease and version name (14.1 or 1401)
     * @return       Valid TkRelease name
     */
    public static String getValidReleaseName(String aName) {

        if (aName.indexOf(".") > -1) {
            return aName;
        }
        else if (aName.indexOf("0") == 2) {
            return aName.substring(0, aName.indexOf("0")) + 
                   "." + aName.substring(aName.indexOf("0") + 1);
        }

        return aName;
    }

     /**
     * Data Members
     */
    private short id;
    private String name;
    private String altName;
    
    
    /**
     * Getters
     */
    public String getName() { return name; }
    public String getAltName() { return altName; }
    public short getId() { return id; }


    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    private void setAltName(String aName) { altName = aName; }
    private void setId(short anId) { id = anId; }
    
    
    /**
     * Format the Release for packaging
     * 
     * @param xContext  An application context object.
     * @throws IcofException 
     * @throws          Trouble querying the database.
     */
    public String getPackagingName() throws IcofException {
	
	Vector<String> tokens = new Vector<String>();
	IcofCollectionsUtil.parseString(getName(), ".", tokens, false);
	
	String name = (String)tokens.get(0) + 
	              IcofStringUtil.padString((String)tokens.get(1), 2,  "0", true);
	
        return name;
        
    }
    
    
    /**
     * Format the Release for BOM file
     * 
     * @param xContext  An application context object.
     * @throws IcofException 
     * @throws          Trouble querying the database.
     */
    public String getBomName(boolean bExternal) throws IcofException {
	
	Vector<String> tokens = new Vector<String>();
	if (bExternal)
	    IcofCollectionsUtil.parseString(getAltName(), ".", tokens, false);
	else
	    IcofCollectionsUtil.parseString(getName(), ".", tokens, false);   
	return (String)tokens.get(0) + (String)tokens.get(1);
	
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
     * Create a PreparedStatement to lookup this object by id.
     * 
     * @param xContext   Application context.
     * @param useAltName If true use the alt name otherwise use name
     * @throws IcofException 
     */
    public void setLookupNameStatement(EdaContext xContext, boolean useAltName)
    throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME;
        if (useAltName) {
            query += " where " + ALT_NAME_COL + " = ? ";
        }
        else {
        	query += " where " + NAME_COL + " = ? ";
        }
        query += "AND " + DELETED_ON_COL + " is NULL";
        
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
     * Create a PreparedStatement to delete this object
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setDeleteStatement(EdaContext xContext) throws IcofException {

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
     * Create a PreparedStatement to update this object
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setUpdateStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "update " + TABLE_NAME + 
                       " set " +  NAME_COL + " = ? , " + 
                                  ALT_NAME_COL + " = ? , " +
                                  UPDATED_BY_COL + " = ? , " +
                                  UPDATED_ON_COL + " = ? " +
                       " where " + ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    

    /**
     * Look up the next id for this table.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupNextId(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setNextIdStatement(xContext);
        
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
    	setAddRowStatement(xContext);
    	setId(getNextSmallIntId(xContext));
    	closeStatement(xContext);
    	
        // Create the SQL query in the PreparedStatement.
        setNextIdStatement(xContext);
        Timestamp now = new Timestamp(new java.util.Date().getTime());
        try {
        	getStatement().setLong(1, getId());
            getStatement().setString(2, getName());
            getStatement().setString(3, getAltName());
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
     * Look up this object by id.
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
     * Look up this object by name.
     * 
     * @param xContext   An application context object.
     * @param useAltName If true use the alt name otherwise use name
     * @throws           Trouble querying the database.
     */
    public void dbLookupByName(EdaContext xContext, boolean useAltName)
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupNameStatement(xContext, useAltName);
        
        try {
        	if (useAltName) {
        		getStatement().setString(1, getAltName());
        	}
        	else {
        		getStatement().setString(1, getName());
        	}

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
     * Update the names for this object.
     * 
     * @param xContext  An application context object.
     * @param aName     New release name
     * @param anAltName New alternate release name
     * @throws          Trouble querying the database.
     */
    public void dbUpdateRow(EdaContext xContext, String aName,
    			                    String anAltName, User_Db editor)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
    	Timestamp now = new Timestamp(new java.util.Date().getTime());
        setUpdateStatement(xContext);
        try {
            getStatement().setString(1, aName);
            getStatement().setString(2, anAltName);
            getStatement().setString(3, editor.getIntranetId());
            getStatement().setTimestamp(4, now);
            getStatement().setLong(5, getId());

        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass().getName(),
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
                                                 "Unable to insert new row.\n",
                                                 "QUERY: " + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }

        // Close the PreparedStatement.
        closeStatement(xContext);

        // Set the names on this object.
        setName(aName);
        setAltName(anAltName);
        setUpdatedBy(editor.getIntranetId());
        setUpdatedOn(now);
        setLoadFromDb(true);

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
        setDeleteStatement(xContext);
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
        
        super.populate(xContext, rs);
        setId(rs.getShort(ID_COL));
        setName(rs.getString(NAME_COL));
        setAltName(rs.getString(ALT_NAME_COL));
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
        buffer.append("Release name: " + getName() + "\n");
        buffer.append("Alt Release name: " + getAltName() + "\n");
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
