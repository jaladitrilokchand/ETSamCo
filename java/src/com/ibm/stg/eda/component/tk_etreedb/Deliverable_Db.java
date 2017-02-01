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
* Deliverable DB class
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 07/30/2013 GFS  Initial coding.
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

public class Deliverable_Db extends TkAudit {

    /**
     * Constants.
     */
    private static final long serialVersionUID = -8320187177759345752L;

    public static final String TABLE_NAME = "TK.DELIVERABLE";
    public static final String ID_COL = "DELIVERABLE_ID";
    public static final String COMP_PKG_ID_COL = "COMPONENTPACKAGE_ID";
    public static final String FILENAME_ID_COL = "FILENAME_ID";
    public static final String CHECKSUM_COL = "CHECKSUM";
    public static final String TYPE_COL = "TYPE";
    public static final String ACTION_COL = "ACTION";
    public static final String SIZE_COL = "SIZE";
    public static final String LAST_MODIFIED = "LAST_MODIFIED";
    public static final String ALL_COLS = ID_COL + "," + COMP_PKG_ID_COL + "," +
                                          FILENAME_ID_COL + "," + 
                                          CHECKSUM_COL + "," +	
                                          TYPE_COL + "," + ACTION_COL + "," + 
                                          SIZE_COL + "," + LAST_MODIFIED; 

    /**
     * Constructor - takes a DB id
     * 
     * @param anId  Deliverable database id
     */
    public Deliverable_Db(long anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes Event Name id, Events Id and a comment
     * 
     * @param aCompPkgId  Id of ComponentPackage object
     * @param aFileNameId Id of FileName object
     * @param aChecksum   Deliverable's checksum
     * @param aType       Deliverable's type
     * @param anAction    Deliverable's action
     * @param aSize       Deliverable's size (bytes)
     * @param aTms        Deliverable's last modified timestamp
     */
    public Deliverable_Db(long aCompPkgId, long aFileNameId, long aChecksum,
                          String aType, String anAction, long aSize, long aTms) {
    	setCompPackageId(aCompPkgId);
        setFileNameId(aFileNameId);
        setChecksum(aChecksum);
        setType(aType);
        setAction(anAction);
        setSize(aSize);
        setLastModified(aTms);
    }

    
    /**
     * Constructor - takes all members
     * 
     * @param anId        Deliverable database id
     * @param aCompPkgId  Id of ComponentPackage object
     * @param aFileNameId Id of FileName object
     * @param aChecksum   Deliverable's checksum
     * @param aType       Deliverable's type
     * @param anAction    Deliverable's action
     * @param aSize       Deliverable's size
     * @param aTms        Deliverable's last modified timestamp
     */
    public Deliverable_Db(long anId, long aCompPkgId, long aFileNameId, 
                          long aChecksum, String aType, String anAction,
                          long aSize, long aTms) {
    	setId(anId);
    	setCompPackageId(aCompPkgId);
        setFileNameId(aFileNameId);
        setChecksum(aChecksum);
        setType(aType);
        setAction(anAction);
        setSize(aSize);
        setLastModified(aTms);
    }
    
    
    /**
     * Data Members
     */
    private long id;
    private long compPackageId;
    private long fileNameId;
    private long checksum;
    private String type;
    private String action;
    private long size;
    private long lastModified;
    
    
    /**
     * Getters
     */
    public long getId() { return id; }
    public long getCompPackageId() { return compPackageId; }
    public long getFileNameId() { return fileNameId; }
    public long getChecksum() { return checksum; }
    public String getType() { return type; }
    public String getAction() { return action; }
    public long getSize() { return size; }
    public long getLastModified() { return lastModified; }
    
    
    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setCompPackageId(long anId) { compPackageId = anId; }
    private void setFileNameId(long anId) { fileNameId = anId; }
    private void setChecksum(long aChecksum) { checksum = aChecksum; }
    private void setType(String aType) { type = aType; }
    private void setAction(String anAction) { action = anAction; }
    private void setSize(long aSize) { size = aSize; }
    private void setLastModified(long aTms) { lastModified = aTms; }
    
    
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
     * Create a PreparedStatement to lookup this object by Component Pkg Id
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupByCompPackageIdStatement(EdaContext xContext)
    throws IcofException {

    	
        // Define the query.
        String query = "select " + ALL_COLS +
                       " from " + TABLE_NAME +
                       " where " + COMP_PKG_ID_COL + " = ? "; 
        
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
     * Create a PreparedStatement to delete all rows for a given comp pkg
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setDeleteAllCompPkgStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "delete from " + TABLE_NAME + 
                       " where " + COMP_PKG_ID_COL + " = ? ";
        
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
                       " set " +  FILENAME_ID_COL + " = ? , " + 
                                  CHECKSUM_COL + " = ? , " +
                                  TYPE_COL + " = ? " +
                                  ACTION_COL + " = ? " +
                                  SIZE_COL + " = ? " +
                                  LAST_MODIFIED + " = ? " +
                       " where " + ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    

    /**
     * Look up the Deliverable by id.
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
        if (! selectSingleRow(xContext, getStatement())) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupById()",
                                                 IcofException.SEVERE,
                                                 "Unable to find row for query.\n",
                                                 "QUERY: " +  getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;

        }
        
        // Close the PreparedStatement.
        closeStatement(xContext);

    }
    

    /**
     * Look up the Component Package ID.
     * 
     * @param xContext  An application context object.
     * @param eventsId  Events id to find individual events for
     * @throws          Trouble querying the database.
     */
    public Vector<Deliverable_Db> dbLookupByCompPkgId(EdaContext xContext, 
                                                      long compPkgId) 
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupByCompPackageIdStatement(xContext);
        
        try {
            getStatement().setLong(1, compPkgId);
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByCompPkgId()",
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
        Vector<Deliverable_Db> deliverables = new Vector<Deliverable_Db>();
        try {
            while (rs.next()) {
                long anId = rs.getLong(ID_COL);
                long aCompPkgId = rs.getLong(COMP_PKG_ID_COL);
                long aFileNameId = rs.getLong(FILENAME_ID_COL);
                long aChecksum = rs.getLong(CHECKSUM_COL);
                String aType = rs.getString(TYPE_COL);
                String anAction = rs.getString(ACTION_COL); 
                long aSize = rs.getLong(SIZE_COL);
                long aTms = rs.getLong(LAST_MODIFIED);

                
                Deliverable_Db del = new Deliverable_Db(anId, aCompPkgId, 
                                                        aFileNameId, 
                                                        aChecksum, 
                                                        aType, anAction,
                                                        aSize, aTms);
                deliverables.add(del);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), 
                                    "dbLookupByEventId()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);

        return deliverables;
    }
    
    
    /**
     * Insert a new row.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext)
    throws IcofException{

        // Create the next id for this new row
        setNextIdStatement(xContext);
        setId(getNextBigIntId(xContext, getStatement()));
        closeStatement(xContext);
        
        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        try {
            getStatement().setLong(1, getId());
            getStatement().setLong(2, getCompPackageId());
            getStatement().setLong(3, getFileNameId());
            getStatement().setLong(4, getChecksum());
            getStatement().setString(5, getType());
            getStatement().setString(6, getAction());
            getStatement().setLong(7, getSize());
            getStatement().setLong(8, getLastModified());
            
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
     * Update the name for this object.
     * 
     * @param xContext     An application context object.
     * @param aFileNameId  New FileName id
     * @param aChecksum    New Checksum size
     * @param aType        New Type name
     * @param anAction     New Action name
     * @param aSize        New Size (in bytes)
     * @param aTms         New last modified timestamp
     * 
     * @throws             Trouble querying the database.
     */
    public void dbUpdateRow(EdaContext xContext, long aFileNameId,
                            long aChecksum, String aType, 
                            String anAction, long aSize, long aTms)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setUpdateStatement(xContext);
        
        try {
            getStatement().setLong(1, aFileNameId);
            getStatement().setLong(2, aChecksum);
            getStatement().setString(3, aType);
            getStatement().setString(4, anAction);
            getStatement().setLong(5, aSize);
            getStatement().setLong(6, aTms);
            getStatement().setLong(7, getId());

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
        setFileNameId(aFileNameId);
        setChecksum(aChecksum);
        setType(aType);
        setAction(anAction);
        setSize(aSize);
        setLastModified(aTms);
        setLoadFromDb(true);
                
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
     * Populate this object from the result set.
     * 
     * @param xContext  Application context.
     * @param rs        A valid result set.
     * @throws          Trouble retrieving the data.
     */
    protected void populate(EdaContext xContext, ResultSet rs) 
    throws SQLException, IcofException {

        setId(rs.getLong(ID_COL));
        setCompPackageId(rs.getLong(COMP_PKG_ID_COL));
        setFileNameId(rs.getLong(FILENAME_ID_COL));
        setChecksum(rs.getLong(CHECKSUM_COL));
        setType(rs.getString(TYPE_COL));
        setAction(rs.getString(ACTION_COL));
        setSize(rs.getLong(SIZE_COL));
        setLastModified(rs.getLong(LAST_MODIFIED));
        setLoadFromDb(true);

    }
    
    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID          : " + getId() + "\n");
        buffer.append("Comp Pkg id : " + getCompPackageId() + "\n");
        buffer.append("File Name id: " + getFileNameId() + "\n");
        buffer.append("Checksum    : " + getChecksum() + "\n");
        buffer.append("Type        : " + getType() + "\n");
        buffer.append("Action      : " + getAction() + "\n");
        buffer.append("Size        : " + getSize() + "\n");
        buffer.append("Last Mod    : " + getLastModified() + "\n");
        
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
     * Delete all deliverables associated with this given Component package id
     *
     * @param xContext   Application context
     * @param compPkgIid Component package id      
     * @throws IcofException 
     */
    public void dbDeleteAllCompPkg(EdaContext xContext, long compPkgId) 
    throws IcofException {

	// Look for deliverables already associated with this comp pkg
	Vector<Deliverable_Db> dels = dbLookupByCompPkgId(xContext, compPkgId);
	if (dels.isEmpty()) {
	    return; // return if nothing to delete
	}
	
        // Create the SQL query in the PreparedStatement.
        setDeleteAllCompPkgStatement(xContext);
        try {
            getStatement().setLong(1, compPkgId);
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass().getName(),
                                                 "dbDeleteAllCompPkg()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query -- had trouble with insertRow() not returning correct result
        insertRow(xContext);

        // Close the PreparedStatement.
        closeStatement(xContext);

    }
  
}
