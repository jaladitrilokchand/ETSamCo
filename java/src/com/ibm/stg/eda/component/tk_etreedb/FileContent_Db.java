/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2013 -- IBM Internal Use Only
*
*=============================================================================
*
*-PURPOSE---------------------------------------------------------------------
* FileContent DB class
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 08/21/2013 GFS  Initial coding.
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


public class FileContent_Db extends TkAudit {
	
	/**
     * Constants.
     */
	private static final long serialVersionUID = 1L;
    public static final String TABLE_NAME = "TK.FILECONTENT";
    public static final String ID_COL = "FILECONTENT_ID";
    public static final String FILE_NAME_ID_COL = "FILENAME_ID";
    public static final String CHECKSUM_COL = "CHECKSUM";
    public static final String CONTENTS_COL = "CONTENTS";
    public static final String CREATED_ON_COL = "FILECREATEDATE";
    public static final String MODIFIED_ON_COL = "FILEMODIFYDATE";
    public static final String ALL_COLS = ID_COL + "," + 
                                          FILE_NAME_ID_COL + "," +
    	                                  CHECKSUM_COL + "," +
    	                                  CONTENTS_COL + "," + 
    	                                  CREATED_ON_COL + "," +
                                          MODIFIED_ON_COL;
        
    
    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public FileContent_Db(long anId) {
        setId(anId);
    }

    
    /**
     * Constructor - used to create new row 
     * 
     * @param aFileName   FileName object
     * @param aChecksum   Checksum
     * @param aContent    The contents (a file/blob)
     */
    public FileContent_Db(FileName_Db aFileName, int aChecksum,
                          String aContent) {
        setFileName(aFileName);
        setChecksum(aChecksum);
        setContent(aContent);
    }

    
    /**
     * Constructor - takes all members 
     * branch name.
     * 
     * @param anId        Object id
     * @param aFileName   FileName object
     * @param aChecksum   File's checksum
     * @param aContent    File's contents
     * @param aCreateTms  File's creation timestamp
     * @param aModifyTms  File's modified timestamp
     */
    public FileContent_Db(long anId,
                          FileName_Db aFileName, int aChecksum,
                          String aContent, Timestamp aCreateTms,
                          Timestamp aModifyTms) {
        setId(anId);
    	setFileName(aFileName);
        setChecksum(aChecksum);
        setContent(aContent);
        setCreateTms(aCreateTms);
        setModifyTms(aModifyTms);
    }

    
    /**
     * Data Members
     */
    private long id;
    private FileName_Db fileName;
    private int checksum;
    private String contents;
    private Timestamp createTimestamp;
    private Timestamp modifyTimestamp;
    
    
    /**
     * Getters
     */
    public long getId() { return id; }
    public FileName_Db getFileName() { return fileName; }
    public int getChecksum() { return checksum; }
    public String getContents() { return contents; }
    public Timestamp getCreateTimestamp() { return createTimestamp; }
    public Timestamp getModifyTimestamp() { return modifyTimestamp; }


    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setFileName(FileName_Db aFile) { fileName = aFile; }
    private void setChecksum(int aName) { checksum = aName; }
    private void setContent(String stuff) { contents = stuff; }
    private void setCreateTms(Timestamp aTms) { createTimestamp = aTms; }
    private void setModifyTms(Timestamp aTms) { modifyTimestamp = aTms; }
    private void setFileName(EdaContext xContext, long anId) { 
    	fileName = new FileName_Db(anId);
    }

    
    /**
     * Create a PreparedStatement to lookup this object by id.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupIdStatement(EdaContext xContext) 
    throws IcofException {

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
    public void setAddRowStatement(EdaContext xContext) 
    throws IcofException {
    	
        // Define the query.
        String query = "insert into " + TABLE_NAME + 
                       " ( " +  ALL_COLS + " ) " +
                       " values( ?, ?, ?, ?, ?, ? )";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup the next id for this table.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setNextIdStatement(EdaContext xContext) 
    throws IcofException {

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
            getStatement().setLong(2, getFileName().getId());
            getStatement().setInt(3, getChecksum());
            getStatement().setString(4, getContents()); 
            getStatement().setTimestamp(5, getCreateTimestamp());
            getStatement().setTimestamp(6, getModifyTimestamp());

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
        setFileName(xContext, rs.getLong(FILE_NAME_ID_COL));
        setChecksum(rs.getInt(CHECKSUM_COL));
        setContent(rs.getString(CONTENTS_COL));
        setCreateTms(rs.getTimestamp(CREATED_ON_COL));
        setModifyTms(rs.getTimestamp(MODIFIED_ON_COL));
        setLoadFromDb(true);

    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID          : " + getId() + "\n");
        buffer.append("FileName ID : " + getFileName().getId() + "\n");
        buffer.append("Checksum    : " + getChecksum() + "\n");
        buffer.append("Contents    : " + getContents() + "\n");
        buffer.append("Created on  : " + getCreateTimestamp().toString() + "\n");
        buffer.append("Modified on : " + getModifyTimestamp().toString() + "\n");

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
