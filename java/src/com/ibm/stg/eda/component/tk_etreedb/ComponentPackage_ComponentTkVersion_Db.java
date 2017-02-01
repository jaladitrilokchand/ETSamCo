/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2013 -- IBM Internal Use Only
*
*=============================================================================
*
*-PURPOSE---------------------------------------------------------------------
* ComponentPackate X ComponentVersion DB class
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


public class ComponentPackage_ComponentTkVersion_Db extends TkAudit {

    /**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.COMPONENTPACKAGE_X_COMPONENT_TKVERSION";
    public static final String COMPONENT_PACKAGE_ID_COL = "COMPONENTPACKAGE_ID";
    public static final String COMPONENT_VERSION_COL = "COMPONENT_TKVERSION_ID";
    public static final String ALL_COLS = COMPONENT_PACKAGE_ID_COL + ", " + 
                                          COMPONENT_VERSION_COL;
    private static final long serialVersionUID = 1L;

    
    /**
     * Constructor - takes ComponentPackage and ComponentTkVersion ids
     * 
     * @param aCompPackageId  A ComponentPackage database object id
     * @param aCompVerId      A ComponentVersion database object id
     */
    public ComponentPackage_ComponentTkVersion_Db(EdaContext xContext, 
                                                  long aCompPackageId, 
                                                  long aCompVerId) 
    throws IcofException {
        setCompVersion(xContext, aCompVerId);
        setCompPackage(xContext, aCompPackageId);
    }

    
    /**
     * Constructor - takes ComponentPackage and ComponentTkVersion objects.
     * 
     * @param aCompVersion A Component_Version_Db object
     * @param aCompPackage A ComponentPackage object
     */
    public ComponentPackage_ComponentTkVersion_Db(ComponentPackage_Db aCompPackage,
                                                  Component_Version_Db aCompVersion) {
        setCompVersion(aCompVersion);
        setCompPackage(aCompPackage);
    }

    
    /**
     * Data Members
     */
    private Component_Version_Db compVersion;
    private ComponentPackage_Db compPackage;
    
    
    /**
     * Getters
     */
    public Component_Version_Db getCompVersion() { return compVersion; }
    public ComponentPackage_Db getCompPackage() { return compPackage; }

    
    /**
     * Setters
     */
    private void setCompVersion(Component_Version_Db anUpdate) { compVersion = anUpdate; }
    private void setCompPackage(ComponentPackage_Db anUpdate) { compPackage = anUpdate; }
    private void setCompVersion(EdaContext xContext, long anId) { 
    	compVersion = new Component_Version_Db(anId);
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
                       COMPONENT_VERSION_COL + " = ? AND " + 
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
                       " where " + COMPONENT_VERSION_COL + " = ? "; 
        
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
    public void setLookupCompVersionsStatement(EdaContext xContext) 
    throws IcofException {
        
        // Define the query.
        String query = "select " + COMPONENT_VERSION_COL + 
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
                       "   set " + COMPONENT_VERSION_COL + " = ? " +
                       " where " + COMPONENT_VERSION_COL + " = ? " +
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
    public void setDeleteCompPkgsStatement(EdaContext xContext) throws IcofException {

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
            getStatement().setLong(1, getCompVersion().getId());
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
            getStatement().setLong(2, getCompVersion().getId());

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
     * Create a list of CompPackge objects for this CompVersion.
     * 
     * @param xContext  An application context object.
     * @return          Collection of ComponentPackage_Db objects for this CompVersion
     * @throws          Trouble querying the database.
     */
    public Hashtable<String,ComponentPackage_Db> dbLookupCompPkgs(EdaContext xContext)
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupCompPkgsStatement(xContext);
        
        try {
            getStatement().setLong(1, getCompVersion().getId());
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
     * Create a list of CompVersion objects for this CompPackage
     * 
     * @param xContext  An application context object.
     * @return          Collection of ComponenteVersion_Db objects.
     * @throws          Trouble querying the database.
     */
    public Vector<Component_Version_Db>  dbLookupCompVersions(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupCompVersionsStatement(xContext);
        
        try {
            getStatement().setLong(1, getCompPackage().getId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupCompVersions()",
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
        Vector<Component_Version_Db> compVersions = new Vector<Component_Version_Db>();
        try {
            while (rs.next()) {
                short anId =  rs.getShort(COMPONENT_VERSION_COL);
                Component_Version_Db cv = new Component_Version_Db(anId);
                cv.dbLookupById(xContext);
                
                compVersions.add(cv);
            }

        }
        catch(SQLException ex) {
            throw new IcofException(this.getClass().getName(), 
                                    "dbLookupCompVersion()",
                                    IcofException.SEVERE, 
                                    "Error reading DB query results.",
                                    ex.getMessage());
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        return compVersions;
        
    }

    
    /**
     * Update the ComponentVersion for this object.
     * 
     * @param xContext   An application context object.
     * @param newCompVer The new ComponentVersion object
     * @throws           Trouble querying the database.
     */
    public void dbUpdateCompVersion(EdaContext xContext, 
                                    Component_Version_Db newCompVer)
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setUpdateCompVersionStatement(xContext);
        
        try {
            getStatement().setLong(1, newCompVer.getId());
            getStatement().setLong(2, getCompVersion().getId());
            getStatement().setLong(3, getCompPackage().getId());
            
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbUpdateCompVersion()",
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
                                                 "dbUpdateCompVersion()",
                                                 IcofException.SEVERE,
                                                 "Unable to update row.\n",
                                                 "QUERY: " + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }

        // Close the PreparedStatement.
        closeStatement(xContext);
        
        // Update the data members.
        setCompVersion(newCompVer);
        setLoadFromDb(true);
        
    }


    /**
     * Delete this object from the database
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbDeleteRowCompPkgs(EdaContext xContext)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setDeleteCompPkgsStatement(xContext);
        try {
            getStatement().setLong(1, getCompPackage().getId());

        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass().getName(),
                                                 "dbDeleteRowCompPkgs()",
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
                                                 "dbDeleteRowCompPkgs()",
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
        
        setCompVersion(xContext, rs.getShort(COMPONENT_VERSION_COL));
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
        buffer.append("Component_TkVerion ID: " + getCompVersion().getId() + "\n");
        
        return buffer.toString();
        
    }
    
    
    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
        return String.valueOf(getCompVersion().getIdKey(xContext) + "_" +
        		              getCompPackage().getIdKey(xContext));
    }

    
}
