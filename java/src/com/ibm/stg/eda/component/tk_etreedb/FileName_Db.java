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
* FileName DB class with audit info
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/18/2010 GFS  Initial coding.
* 07/22/2010 GFS  Converted to using PreparedStatements.
* 03/25/2011 GFS  FIxed a bug in populate() - using setShort instead of setLong.
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


public class FileName_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2438805343209435149L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.FILENAME";
    public static final String ID_COL = "FILENAME_ID";
    public static final String NAME_COL = "FILENAME";
    public static final String ALL_COLS = ID_COL + "," + NAME_COL;

    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public FileName_Db(long anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes a File name
     * 
     * @param aName           Name of a file (below tree top)
     */
    public FileName_Db(String aName) {
        setName(aName);
    }

    
    /**
     * Data Members
     */
    private long id;
    private String name;
    
    
    /**
     * Getters
     */
    public String getName() { return name; }
    public long getId() { return id; }


    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    public void setId(long anId) { id = anId; }
    

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
     * Create a PreparedStatement to lookup this object by CQ.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupCQStatement(EdaContext xContext) throws IcofException {

    	// Define the query.
		String query = "select f." + NAME_COL + " from "
				+ CodeUpdate_ChangeRequest_Db.TABLE_NAME + " as cur, "
				+ ChangeRequest_Db.TABLE_NAME + " as CQ, "
				+ FileVersion_Db.TABLE_NAME + " as fv, " + TABLE_NAME + " as f "
				+ " where cq." + ChangeRequest_Db.CQ_COL + " = ? " + " and cq."
				+ ChangeRequest_Db.ID_COL + " = cur."
				+ CodeUpdate_ChangeRequest_Db.CHANGE_REQUEST_ID_COL
				+ " and cur." + CodeUpdate_ChangeRequest_Db.CODE_UPDATE_ID_COL
				+ " = fv." + FileVersion_Db.CODE_UPDATE_ID_COL + " and fv."
				+ FileVersion_Db.FILENAME_ID_COL + " = f." + ID_COL
				+ " order by f." + ID_COL;
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
                       " where " + NAME_COL + " = ? ";
        
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
                       " values( ?, ? )";
        
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
    public void dbLookupByName(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupNameStatement(xContext);
        
        try {
            getStatement().setString(1, getName());
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
     * Look up this object by CQ .
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public Vector<String>  dbLookupByCQ(EdaContext xContext, String cqId) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupCQStatement(xContext);
        
        try {
            getStatement().setString(1, cqId);
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByCQ()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        ResultSet rs = executeQuery(xContext);
        Vector<String> fileNameList = new  Vector<String>();
        
        try {
            while (rs.next()) {
            	String cqName = rs.getString(NAME_COL);
            	if(cqName !=null){
            		fileNameList.add(cqName);
            	}
            }
        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), "dbLookupRevsByCompVerLoc()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }
        
        // Close the PreparedStatement.
        closeStatement(xContext);
        return fileNameList;
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
            getStatement().setString(2, getName());

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

        //  Load the data for the new row.
        setId(id);
        dbLookupById(xContext); 
        
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
        
        setId(rs.getLong(ID_COL));
        setName(rs.getString(NAME_COL));
        setLoadFromDb(true);

    }
  

    /**
     * Return the members as a string.
     */
    protected String toString(EdaContext xContext) {

        
        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID: " + getId() + "\n");
        buffer.append("File name: " + getName() + "\n");
        
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
