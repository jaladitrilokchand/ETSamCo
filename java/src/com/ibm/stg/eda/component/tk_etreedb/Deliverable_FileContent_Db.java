/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2013 -- IBM Internal Use Only
*
*=============================================================================
*
*-PURPOSE---------------------------------------------------------------------
* Deliverable X FileContent DB class
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
import java.util.Hashtable;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;


public class Deliverable_FileContent_Db extends TkAudit {
	
	/**
     * Constants.
     */
	private static final long serialVersionUID = 1L;
    public static final String TABLE_NAME = "TK.DELIVERABLE_X_FILECONTENT";
    public static final String COMPONENT_PACKAGE_ID_COL = "COMPONENTPACKAGE_ID";
    public static final String DELIVERABLE_ID_COL = "DELIVERABLE_ID";
    public static final String FILE_CONTENT_ID_COL = "FILECONTENT_ID";
    public static final String ALL_COLS = COMPONENT_PACKAGE_ID_COL + "," + 
                                          DELIVERABLE_ID_COL + "," +
                                          FILE_CONTENT_ID_COL;
    
    
    /**
     * Constructor - takes ids
     * 
     * @param aCompPackageId  A ComponentPackage database object id
     * @param aDeliverableId  A Deliverable database object id
     * @param aFileContentId  A FileContent database object id
     */
    public Deliverable_FileContent_Db(EdaContext xContext, 
                                      long aCompPackageId, 
                                      long aDeliverableId, 
                                      long aFileContentId) 
    throws IcofException {
    	setCompPackage(xContext, aCompPackageId);
    	setDeliverable(xContext, aDeliverableId);
    	setFileContent(xContext, aFileContentId);
        
    }

    
    /**
     * Constructor - takes objects
     * 
     * @param aCompPackage A ComponentPackage object
     * @param aDeliverable A TkVersion_Db object
     * @param aFileContent A FileContent_Db object
     */
    public Deliverable_FileContent_Db(ComponentPackage_Db aCompPackage,
                                         Deliverable_Db aDeliverable,
                                         FileContent_Db aFileContent) {
    	setCompPackage(aCompPackage);
        setDeliverable(aDeliverable);
        setFileContent(aFileContent);
    }

    
    /**
     * Data Members
     */
    private ComponentPackage_Db compPackage;
    private Deliverable_Db deliverable;
    private FileContent_Db fileContent;    
    
    
    /**
     * Getters
     */
    public ComponentPackage_Db getCompPackage() { return compPackage; }
    public Deliverable_Db getDeliverable() { return deliverable; }
    public FileContent_Db getFileContent() { return fileContent; }

    
    /**
     * Setters
     */
    private void setCompPackage(ComponentPackage_Db anUpdate) { compPackage = anUpdate; }
    private void setDeliverable(Deliverable_Db anUpdate) { deliverable = anUpdate; }
    private void setFileContent(FileContent_Db anUpdate) { fileContent = anUpdate; }
    
    private void setCompPackage(EdaContext xContext, long anId) { 
    	compPackage = new ComponentPackage_Db(anId);
    }
    private void setDeliverable(EdaContext xContext, long anId) { 
    	deliverable = new Deliverable_Db(anId);
    }
    private void setFileContent(EdaContext xContext, long anId) { 
    	fileContent = new FileContent_Db(anId);
    }
    
    
    /**
     * Create a PreparedStatement to lookup this object by ids.

     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupIdsStatement(EdaContext xContext) 
    throws IcofException {
        
        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + 
                       DELIVERABLE_ID_COL + " = ? AND " + 
                       FILE_CONTENT_ID_COL + " = ? AND " +
                       COMPONENT_PACKAGE_ID_COL + " = ? ";
        
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
                       " ( " +  ALL_COLS + " )" + 
                       " values( ?, ?, ? )";
        
		 // Set and prepare the query and statement.
		 setQuery(xContext, query);
        
    }


    /**
     * Create a PreparedStatement to lookup the ComponentPackages
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupFileContentsStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "select " + FILE_CONTENT_ID_COL + 
                       " from " + TABLE_NAME + 
                       " where " + DELIVERABLE_ID_COL + " = ? AND " +
                       COMPONENT_PACKAGE_ID_COL + " = ? "; 
        
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
    public void setUpdateFileContentStatement(EdaContext xContext) 
    throws IcofException {
        
        // Define the query.
        String query = "update " + TABLE_NAME +
                       "   set " + FILE_CONTENT_ID_COL + " = ? " +
                       " where " + DELIVERABLE_ID_COL + " = ? " +
                       "   and " + COMPONENT_PACKAGE_ID_COL + " = ? "; 
        
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
    public void setDeleteStatement(EdaContext xContext)
    throws IcofException {

        // Define the query.
        String query = "delete from " + TABLE_NAME + 
                       " where " + COMPONENT_PACKAGE_ID_COL + " = ? " +
                       " AND " + DELIVERABLE_ID_COL + " = ? " +
                       " AND " + FILE_CONTENT_ID_COL + " = ? ";
        
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
            getStatement().setLong(1, getDeliverable().getId());
            getStatement().setLong(2, getFileContent().getId());
            getStatement().setLong(3, getCompPackage().getId());
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
            getStatement().setLong(1, getCompPackage().getId());
            getStatement().setLong(2, getDeliverable().getId());
            getStatement().setLong(2, getFileContent().getId());

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
     * Create a list of FileContent objects for this CompPkg and Deliverable
     * 
     * @param xContext  An application context object.
     * @return          Collection of FileContent_Db objects
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,FileContent_Db> dbLookupFileContents(EdaContext xContext) 
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupFileContentsStatement(xContext);
        
        try {
            getStatement().setLong(1, getDeliverable().getId());
            getStatement().setLong(2, getCompPackage().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupFileContents()",
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
        Hashtable<String,FileContent_Db> items = 
        	new Hashtable<String,FileContent_Db>();
        try {
            while (rs.next()) {
                long anId =  rs.getLong(FILE_CONTENT_ID_COL);
                FileContent_Db item = new FileContent_Db(anId);
                item.dbLookupById(xContext);
                
                items.put(item.getIdKey(xContext), item);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), 
            			            "dbLookupFileContents()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return items;
        
    }

    
    /**
     * Update the FileContent for this object.
     * 
     * @param xContext       An application context object.
     * @param newFileContent The new FileContent object
     * @throws               Trouble querying the database.
     */
    public void dbUpdateFileContent(EdaContext xContext, 
                                    FileContent_Db newFileContent)
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setUpdateFileContentStatement(xContext);
        
        try {
            getStatement().setLong(1, newFileContent.getId());
            getStatement().setLong(2, getDeliverable().getId());
            getStatement().setLong(3, getCompPackage().getId());
            
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbUpdateFileContent()",
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
                                                 "dbUpdateFileContent()",
                                                 IcofException.SEVERE,
                                                 "Unable to update row.\n",
                                                 "QUERY: " + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        // Update the data members.
        setFileContent(newFileContent);
        setLoadFromDb(true);
        
    }


    /**
     * Delete this object from the database
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbDelete(EdaContext xContext)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setDeleteStatement(xContext);
        try {
            getStatement().setLong(1, getCompPackage().getId());
            getStatement().setLong(2, getDeliverable().getId());
            getStatement().setLong(3, getFileContent().getId());

        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass().getName(),
                                                 "dbDelete()",
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
                                                 "dbDelete()",
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
        
    	setCompPackage(xContext, rs.getLong(COMPONENT_PACKAGE_ID_COL));
        setDeliverable(xContext, rs.getLong(DELIVERABLE_ID_COL));
        setFileContent(xContext, rs.getLong(FILE_CONTENT_ID_COL));
        setLoadFromDb(true);

    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ComponentPackage ID: " + getCompPackage().getId() + "\n");
        buffer.append("Deliverable ID     : " + getDeliverable().getId() + "\n");
        buffer.append("FileContent ID     : " + getFileContent().getId() + "\n");
        
        return buffer.toString();
        
    }
    
    
    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
        return String.valueOf(getCompPackage().getIdKey(xContext) + "_" +
                              getDeliverable().getIdKey(xContext) + "_" +
        		              getFileContent().getIdKey(xContext));
    }

    
}
