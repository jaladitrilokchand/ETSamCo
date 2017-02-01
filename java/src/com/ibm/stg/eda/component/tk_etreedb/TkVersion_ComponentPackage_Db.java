/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2013 -- IBM Internal Use Only
*
*=============================================================================
*
*-PURPOSE---------------------------------------------------------------------
* TkVerison X ComponentPackate DB class
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
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofException;


public class TkVersion_ComponentPackage_Db extends TkAudit {
	
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.TKVERSION_X_COMPONENTPACKAGE";
    public static final String COMPONENT_PACKAGE_ID_COL = "COMPONENTPACKAGE_ID";
    public static final String TK_VERSION_COL = "TKVERSION_ID";
    public static final String ALL_COLS = COMPONENT_PACKAGE_ID_COL + "," + 
                                          TK_VERSION_COL;
    private static final long serialVersionUID = 1L;

    
    /**
     * Constructor - takes ComponentPackage and ComponentTkVersion ids
     * 
     * @param aCompPackageId  A ComponentPackage database object id
     * @param aTkVersionId    A TkVersion database object id
     */
    public TkVersion_ComponentPackage_Db(EdaContext xContext, 
                                         long aCompPackageId, 
                                         short aTkVersionId) 
    throws IcofException {
        setTkVersion(xContext, aTkVersionId);
        setCompPackage(xContext, aCompPackageId);
    }

    
    /**
     * Constructor - takes ComponentPackage and TkVersion objects.
     * 
     * @param aTkVersion A TkVersion_Db object
     * @param aCompPackage A ComponentPackage object
     */
    public TkVersion_ComponentPackage_Db(ComponentPackage_Db aCompPackage,
                                         RelVersion_Db aTkVersion) {
        setTkVersion(aTkVersion);
        setCompPackage(aCompPackage);
    }

    
    /**
     * Data Members
     */
    private RelVersion_Db tkVersion;
    private ComponentPackage_Db compPackage;
    
    
    /**
     * Getters
     */
    public RelVersion_Db getTkVersion() { return tkVersion; }
    public ComponentPackage_Db getCompPackage() { return compPackage; }

    
    /**
     * Setters
     */
    private void setTkVersion(RelVersion_Db anUpdate) { tkVersion = anUpdate; }
    private void setCompPackage(ComponentPackage_Db anUpdate) { compPackage = anUpdate; }
    private void setTkVersion(EdaContext xContext, short anId) { 
    	tkVersion = new RelVersion_Db(anId);
    }
    private void setCompPackage(EdaContext xContext, long anId) { 
    	compPackage = new ComponentPackage_Db(anId);
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
                       TK_VERSION_COL + " = ? AND " + 
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
                       " values( ?, ? )";
        
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
    public void setLookupCompPkgsStatement(EdaContext xContext) 
    throws IcofException {

        // Define the query.
        String query = "select " + COMPONENT_PACKAGE_ID_COL + 
                       " from " + TABLE_NAME + 
                       " where " + TK_VERSION_COL + " = ? "; 
        
		 // Set and prepare the query and statement.
		 setQuery(xContext, query);
        
    }

  
    /**
     * Create a PreparedStatement to lookup the TkVersions.
     *
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupTkVersionsStatement(EdaContext xContext) 
    throws IcofException {
        
        // Define the query.
        String query = "select " + TK_VERSION_COL + 
                       " from " + TABLE_NAME + 
                       " where " + COMPONENT_PACKAGE_ID_COL + " = ? "; 
        
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
    public void setUpdateCompVersionStatement(EdaContext xContext) 
    throws IcofException {
        
        // Define the query.
        String query = "update " + TABLE_NAME +
                       "   set " + TK_VERSION_COL + " = ? " +
                       " where " + TK_VERSION_COL + " = ? " +
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
    public void setDeleteCompPkgsStatement(EdaContext xContext)
    throws IcofException {

        // Define the query.
        String query = "delete from " + TABLE_NAME + 
                       " where " + COMPONENT_PACKAGE_ID_COL + " = ? ";
        
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
            getStatement().setLong(1, getTkVersion().getId());
            getStatement().setLong(2, getCompPackage().getId());
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
            getStatement().setLong(2, getTkVersion().getId());

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
     * Create a list of CompPackge objects for this TkVersion.
     * 
     * @param xContext  An application context object.
     * @return          Collection of ComponentPackage_Db objects for this TkVersion
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,ComponentPackage_Db> dbLookupCompPkgs(EdaContext xContext) 
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupCompPkgsStatement(xContext);
        
        try {
            getStatement().setLong(1, getTkVersion().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupCompPkgs()",
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
        Hashtable<String,ComponentPackage_Db> items = 
        	new Hashtable<String,ComponentPackage_Db>();
        try {
            while (rs.next()) {
                long anId =  rs.getLong(COMPONENT_PACKAGE_ID_COL);
                ComponentPackage_Db cr = new ComponentPackage_Db(anId);
                cr.dbLookupById(xContext);
                
                items.put(cr.getIdKey(xContext), cr);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), 
            			            "dbLookupCompPkgs()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return items;
        
    }


    /**
     * Create a list of TkVersion objects for this CompPackage
     * 
     * @param xContext  An application context object.
     * @return          Collection of TkVersion_Db objects.
     * @throws          Trouble querying the database.
     */
    public Vector<RelVersion_Db>  dbLookupTkVersions(EdaContext xContext)
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupTkVersionsStatement(xContext);
        
        try {
            getStatement().setLong(1, getCompPackage().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupTkVersions()",
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
        Vector<RelVersion_Db> items = new Vector<RelVersion_Db>();
        try {
            while (rs.next()) {
                short anId =  rs.getShort(TK_VERSION_COL);
                RelVersion_Db cv = new RelVersion_Db(anId);
                cv.dbLookupById(xContext);
                
                items.add(cv);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), 
                                    "dbLookupTkVersions()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return items;
        
    }

    
    /**
     * Update the ComponentVersion for this object.
     * 
     * @param xContext   An application context object.
     * @param newTkVersion The new ComponentVersion object
     * @throws           Trouble querying the database.
     */
    public void dbUpdateTkVersion(EdaContext xContext, 
                                  RelVersion_Db newTkVersion)
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setUpdateCompVersionStatement(xContext);
        
        try {
            getStatement().setLong(1, newTkVersion.getId());
            getStatement().setLong(2, getTkVersion().getId());
            getStatement().setLong(3, getCompPackage().getId());
            
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbUpdateTkVersion()",
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
                                                 "dbUpdateTkVersion()",
                                                 IcofException.SEVERE,
                                                 "Unable to update row.\n",
                                                 "QUERY: " + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        // Update the data members.
        setTkVersion(newTkVersion);
        setLoadFromDb(true);
        
    }


    /**
     * Delete this object from the database
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbDeleteCompPkgs(EdaContext xContext)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setDeleteCompPkgsStatement(xContext);
        try {
            getStatement().setLong(1, getCompPackage().getId());

        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass().getName(),
                                                 "dbDeleteCompPkgs()",
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
                                                 "dbDeleteCompPkgs()",
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
        
        setTkVersion(xContext, rs.getShort(TK_VERSION_COL));
        setCompPackage(xContext, rs.getShort(COMPONENT_PACKAGE_ID_COL));
        setLoadFromDb(true);

    }

    
    /**
     * Return the members as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ComponentPackage ID: " + getCompPackage().getId() + "\n");
        buffer.append("TkVersion ID       : " + getTkVersion().getId() + "\n");
        
        return buffer.toString();
        
    }
    
    
    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
        return String.valueOf(getTkVersion().getIdKey(xContext) + "_" +
        		              getCompPackage().getIdKey(xContext));
    }

    
}
