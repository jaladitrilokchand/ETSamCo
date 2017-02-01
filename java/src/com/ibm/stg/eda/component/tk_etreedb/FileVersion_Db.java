/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* FILE: FileVersion.java
*
*-PURPOSE---------------------------------------------------------------------
* FileVersion DB class with audit info
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/19/2010 GFS  Initial coding.
* 07/22/2010 GFS  Converted to using PreparedStatements.
* 10/25/2010 GFS  Added dbLookupFiles() method.
* 06/10/2011 GFS  Updated dbLookupFiles to improve performance.
* 06/27/2011 GFS  Updated dbLookupFiles to fix a bug when more than 1 source
*                 files is associated with the revision.
* 09/23/2013 GFS  Fixed a bug where the CodeUpdate ID was a short and should 
*                 be a long.
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

public class FileVersion_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5649383859649681885L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.FILE_VERSION";
    public static final String ID_COL = "FILE_VERSION_ID";
    public static final String CODE_UPDATE_ID_COL = "CODE_UPDATE_ID";
    public static final String FILENAME_ID_COL = "FILENAME_ID";
    public static final String FILE_ACTION_NAME_ID_COL = "FILE_ACTION_NAME_ID";
    public static final String ALL_COLS = ID_COL + "," + 
                                          CODE_UPDATE_ID_COL + "," + 
                                          FILENAME_ID_COL + "," + 
                                          FILE_ACTION_NAME_ID_COL;

    
    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public FileVersion_Db(long anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes CodeUpdate and FileName objects.
     * 
     * @param aCodeUpdate  A CodeUpdate object
     * @param aFileName    A FileName object 
     * @param anAction     A FileActionName object
     */
    public FileVersion_Db(CodeUpdate_Db anUpdate, FileName_Db aFileName, 
                       FileActionName_Db anAction) {
        setCodeUpdate(anUpdate);
        setFileName(aFileName);
        setFileAction(anAction);
    }

    
    /**
     * Data Members
     */
    private long id;
    private CodeUpdate_Db codeUpdate;
    private FileName_Db fileName;
    private FileActionName_Db fileAction;

    
    /**
     * Getters
     */
    public long getId() { return id; }
    public CodeUpdate_Db getCodeUpdate() { return codeUpdate; }
    public FileName_Db getFileName() { return fileName; }
    public FileActionName_Db getFileAction() { return fileAction; }
    
    
    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setCodeUpdate(CodeUpdate_Db anUpdate) { codeUpdate = anUpdate; }
    private void setFileName(FileName_Db aFile) { fileName = aFile; }
    private void setFileAction(FileActionName_Db anAction) { fileAction = anAction; }
    private void setCodeUpdate(EdaContext xContext, long anId) { 
    	codeUpdate = new CodeUpdate_Db(anId);
    }
    private void setFileName(EdaContext xContext, long anId) { 
    	fileName = new FileName_Db(anId);
    }
    private void setFileAction(EdaContext xContext, short anId) { 
    	fileAction = new FileActionName_Db(anId);
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
     * Create a PreparedStatement to lookup this object by id.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupNameStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + FILENAME_ID_COL + " =  ? " +
                       " AND " + CODE_UPDATE_ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    

    /**
     * Create a PreparedStatement to lookup this row all by ids.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupAllStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + CODE_UPDATE_ID_COL + " = ? " +
                       " AND " + FILENAME_ID_COL + " =  ? " +
                       " AND " + FILE_ACTION_NAME_ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup this object's source files.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupFilesStatement(EdaContext xContext) 
    throws IcofException {

//    	select fn.filename, fan.svn_action_name
//    	 from tk.file_version as fv,
//    	      tk.fileName as fn,
//    	      tk.file_action_name as fan
//    	 where fv.code_update_id = 2
//    	   and fv.filename_id = fn.filename_id
//    	   and fv.file_action_name_id = fan.file_action_name_id
    	
        // Define the query.
        String query = "select fn." + FileName_Db.NAME_COL + ", " + 
                              "fan." + FileActionName_Db.SVN_NAME_COL +
                       " from " + TABLE_NAME + " as fv, " +
                                  FileName_Db.TABLE_NAME + " as fn, " +
                                  FileActionName_Db.TABLE_NAME + " as fan " +
                       " where fv." + CODE_UPDATE_ID_COL + " = ? " +
                         " and fv." + FILENAME_ID_COL + " = fn." + FileName_Db.ID_COL +
                         " and fv." + FILE_ACTION_NAME_ID_COL + " = fan." + FileActionName_Db.ID_COL;
        
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
     * Look up this object by name.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByCodeUpdateAndFile(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupNameStatement(xContext);
        
        try {
            getStatement().setLong(1, getFileName().getId());
            getStatement().setLong(2, getCodeUpdate().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByCodeUpdateAndFile()",
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
                                                 "dbLookupByCodeUpdateAndFile()",
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
     * Look up this object by name by all ids.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByCodeUpdateFileAndAction(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupAllStatement(xContext);
        
        try {
            getStatement().setLong(1, getCodeUpdate().getId());
            getStatement().setLong(2, getFileName().getId());
            getStatement().setShort(3, getFileAction().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByCodeUpdateFileAndAction()",
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
                                                 "dbLookupByCodeUpdateFileAndAction()",
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
     * Create a list of FileVersion object for this CodeUpdate.
     * 
     * @param xContext  An application context object.
     * @return          Collection of FileVersion_Db objects for this code update.
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,FileVersion_Db> dbLookupFiles(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupFilesStatement(xContext);
        
        try {
        	getStatement().setLong(1, getCodeUpdate().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupFiles()",
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
        Hashtable<String,FileVersion_Db> files = new Hashtable<String,FileVersion_Db>();
        try {
            while (rs.next()) {
                String aName =  rs.getString(FileName_Db.NAME_COL);
                FileName_Db fileName = new FileName_Db(aName);
                
                String action = rs.getString(FileActionName_Db.SVN_NAME_COL);
                FileActionName_Db fileAction = new FileActionName_Db("", action);;
                
                FileVersion_Db file = new FileVersion_Db(getCodeUpdate(), fileName, fileAction);
                files.put(aName + ":" + action, file);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupFiles()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return files;
        
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
            getStatement().setLong(2, getCodeUpdate().getId());
            getStatement().setLong(3, getFileName().getId());
            getStatement().setLong(4, getFileAction().getId());
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

        setId(rs.getLong(ID_COL));
        setCodeUpdate(xContext, rs.getLong(CODE_UPDATE_ID_COL));
        setFileName(xContext, rs.getLong(FILENAME_ID_COL));
        setFileAction(xContext, rs.getShort(FILE_ACTION_NAME_ID_COL));
        setLoadFromDb(true);

    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID: " + getId() + "\n");
        if (getCodeUpdate() != null) {
            buffer.append("CodeUpdate ID: " + getCodeUpdate().getId() + "\n");
            buffer.append("CodeUpdate revision: " + getCodeUpdate().getRevision() + "\n");
        }
        else {
            buffer.append("CodeUpdate ID: NULL\n");
            buffer.append("CodeUpdate revision: NULL\n");
        }
        if (getFileName() != null) {
            buffer.append("FileName ID: " + getFileName().getId() + "\n");
            buffer.append("FileName name: " + getFileName().getName() + "\n");
        }
        else {
            buffer.append("FileName ID: NULL\n");
            buffer.append("FileName name: NULL\n");
        }
        if (getFileAction() != null) {
            buffer.append("FileAction ID: " + getFileAction().getId() + "\n");
            buffer.append("FileAction name: " + getFileAction().getName() + "\n");
        }
        else {
            buffer.append("FileAction ID: NULL\n");
            buffer.append("FileAction name: NULL\n");
        }

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

