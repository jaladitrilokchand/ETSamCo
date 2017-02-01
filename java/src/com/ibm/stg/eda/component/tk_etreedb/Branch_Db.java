/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* FILE: Branch_Db.java
*
*-PURPOSE---------------------------------------------------------------------
* Branch DB class
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 06/30/2011 GFS  Initial coding.
* 07/13/2011 GFS  Added dbAdd, dbUpdate and dbDelete methods. Updated to use 
*                 new BranchName_Db object.
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

public class Branch_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3351527742476086779L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.BRANCH";
    public static final String ID_COL = "BRANCH_ID";
    public static final String BRANCH_NAME_ID_COL = "BRANCHNAME_ID";
    public static final String COMP_VERSION_ID_COL = "COMPONENT_TKVERSION_ID";
    public static final String ALL_COLS = ID_COL + "," + 
                                          BRANCH_NAME_ID_COL + "," +
                                          COMP_VERSION_ID_COL;

    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public Branch_Db(short anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes a Name
     * 
     * @param aBranchName  Branch Name object
     */
    public Branch_Db(BranchName_Db aBranchName) {
        setBranchName(aBranchName);
    }

    
    /**
     * Constructor - takes BranchName and Component_TkVersion objects
     * 
     * @param aBranchName  Branch Name object
     * @param aCompVer     Component_TkVersion object
     */
    public Branch_Db(BranchName_Db aBranchName, Component_Version_Db aCompVer) {
        setBranchName(aBranchName);
        setCompVersion(aCompVer);
    }
    
    
    /**
     * Data Members
     */
    private short id;
    private BranchName_Db branchName;
    private Component_Version_Db compVersion;
    
    
    /**
     * Getters
     */
    public short getId() { return id; }
    public BranchName_Db getBranchName() { return branchName; }
    public Component_Version_Db getCompVersion() { return compVersion; }
    private void setBranchName(EdaContext xContext, int anId) { 
    	branchName = new BranchName_Db(anId);
    }
    private void setCompVersion(EdaContext xContext, short compId) { 
    	compVersion = new Component_Version_Db(compId);
    }
    

    /**
     * Setters
     */
    private void setId(short anId) { id = anId; }
    private void setBranchName(BranchName_Db aBranchName) { branchName = aBranchName; }
    private void setCompVersion(Component_Version_Db aCompVer) { compVersion = aCompVer; }
    
    
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
     * Create a PreparedStatement to lookup Branches by Component_Version ID.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupByCompVersionStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "select " + ID_COL + 
                       " from " + TABLE_NAME + 
                       " where " + COMP_VERSION_ID_COL + " = ? ";
        
        // Set and prepare the query and statement.
		 setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to lookup this object by name and 
     * Component_Version id.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupNameCompVerStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + BRANCH_NAME_ID_COL + " = ? " +
                       " AND " + COMP_VERSION_ID_COL + " = ? ";
        
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
     * Create a PreparedStatement to update this object
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setUpdateBranchNameStatement(EdaContext xContext) throws IcofException {
        
        // Define the query.
        String query = "update " + TABLE_NAME + 
                       " set " +  BRANCH_NAME_ID_COL + " = ? " + 
                       " where " + ID_COL + " = ? ";
        
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
     * Look up this object by name and Component_Version id.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByNameCompVer(EdaContext xContext)
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupNameCompVerStatement(xContext);
        
        try {
            getStatement().setInt(1, getBranchName().getId());
            getStatement().setLong(2, getCompVersion().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByNameCompVer()",
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
                                                 "dbLookupByNameCompVer()",
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
     * Create a list of Branch objects for this Component_Version.
     * 
     * @param xContext  An application context object.
     * @return          Collection of Branch objects
     * @throws IcofException 
     * @throws          Trouble querying the database.
     */
	public Vector<Branch_Db> dbLookupByCompVersion(EdaContext xContext)
	throws IcofException {
        
        // Create the SQL query in the PreparedStatement.
        setLookupByCompVersionStatement(xContext);
        
        try {
            getStatement().setLong(1, getCompVersion().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByCompVersion()",
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
        Vector<Branch_Db> branches = new Vector<Branch_Db>();
        try {
            while (rs.next()) {
                short anId =  rs.getShort(ID_COL);
                Branch_Db branch = new Branch_Db(anId);
                branches.add(branch);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupByCompVersion()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return branches;
	
	}


    /**
     * Insert a new row.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext)
    throws IcofException{

    	// Lookup the next id
    	setNextIdStatement(xContext);
    	setId(getNextSmallIntId(xContext, getStatement()));
    	closeStatement(xContext);
    	
        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        try {
            getStatement().setLong(1, getId());
            getStatement().setLong(2, getBranchName().getId());
            getStatement().setLong(3, getCompVersion().getId());
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
     * @param xContext  An application context object.
     * @param aName     New component name
     * @throws          Trouble querying the database.
     */
    public void dbUpdateRow(EdaContext xContext, BranchName_Db newBranch)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setUpdateBranchNameStatement(xContext);
        try {
            getStatement().setLong(1, newBranch.getId());
            getStatement().setLong(2, getId());

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
        setBranchName(newBranch);
        
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
     * @param xContext       Application context.
     * @param rs             A valid result set.
     * @throws IcofException 
     * @throws SQLException 
     */
    protected void populate(EdaContext xContext, ResultSet rs) 
    throws SQLException, IcofException  {
        
        setId(rs.getShort(ID_COL));
        setBranchName(xContext, rs.getInt(BRANCH_NAME_ID_COL));
        setCompVersion(xContext, rs.getShort(COMP_VERSION_ID_COL));
        
    }
  

    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID: " + getId() + "\n");
        buffer.append("Branch Name ID: " + getBranchName().getId() + "\n");
        buffer.append("Component TkVersion ID: " + getCompVersion().getId() + "\n");
        
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
