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
* User DB class with audit info
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/18/2010 GFS  Initial coding.
* 07/23/2010 GFS  Converted to using PreparedStatements.
* 09/14/2010 GFS  Changed populateFromBluePages() method to set the AFS id to
*                 UNKNOWN instead of trying to construct it from intranet id.
* 02/16/2011 GFS  Added dbLookupByNames() and support methods.
* 03/30/2011 GFS  Added dbUpdateRow() method.  Changed User(String, String) 
*                 constructor which was not used to take the AFS/Intranet ids.
* 05/29/2013 GFS  Updated to use bpfinger to lookup a new user when only the 
*                 AFS id is known.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreedb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAudit;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofSystemUtil;
import com.ibm.stg.iipmds.icof.component.role.UserBluePage;


public class User_Db extends TkAudit {

    /**
	 * 
	 */
	private static final long serialVersionUID = -924266897733454652L;
	/**
     * Constants.
     */
    public static final String TABLE_NAME = "TK.USER";
    public static final String ID_COL = "USER_ID";
    public static final String LAST_NAME_COL = "LASTNAME";
    public static final String FIRST_NAME_COL = "FIRSTNAME";
    public static final String EMAIL_COL = "EMAIL";
    public static final String AFSID_COL = "AFSID";
    public static final String INTRANETID_COL = "INTRANETID";
    public static final String ALL_COLS = ID_COL + "," + 
                                          LAST_NAME_COL + "," + FIRST_NAME_COL + "," +
                                          EMAIL_COL + "," +
                                          AFSID_COL + "," + INTRANETID_COL + "," +
                                          CREATED_BY_COL + "," + CREATED_ON_COL + "," + 
                                          UPDATED_BY_COL + "," + UPDATED_ON_COL + "," +
                                          DELETED_BY_COL + "," + DELETED_ON_COL;
    public static final String FINGER = "/afs/btv/data/linux64/amd64_linux24/bin/bpfinger";

    
    /**
     * Constructor - takes a DB id
     * 
     * @param anId  A database id
     */
    public User_Db(short anId) {
        setId(anId);
    }

    
    /**
     * Constructor - takes an AFS or Intranet id
     * 
     * @param aName       An AFS or Intranet ID
     * @param isIntranet  If true the ID is an intranet id otherwise its 
     *                    the AFS id
     */
    public User_Db(String anId, boolean isIntranet) {
        if (isIntranet) {
            setIntranetId(anId);
        }
        else {
            setAfsId(anId);
        }
    }


    /**
     * Constructor - takes AFS/intranet ids
     * 
     * @param xContext      Application context
     * @param anAfsId       An AFS id
     * @param anIntranetId  An intranet id (user@domain)
     * @throws IcofException 
     */
    public User_Db(EdaContext xContext, String anAfsId, String anIntranetId) 
    throws IcofException {
    	setAfsId(anAfsId);
    	setIntranetId(anIntranetId);
    	populateFromBluePages(xContext);
    }


    /**
     * Constructor - takes all members.
     * 
     * @param anId         An object id
     * @param fName        A first name
     * @param lName        A last name
     * @param eMail        An email address
     * @param anAfsId      An AFS id
     * @param anIntranetId An intrabet id
     */
    public User_Db(short anId, String fName, String lName, String eMail, 
    		       String anAfsId, String anIntranetId) {
    	setId(anId);
    	setFirstName(fName);
    	setLastName(lName);
    	setEmailAddress(eMail);
    	setAfsId(anAfsId);
    	setIntranetId(anIntranetId);
    }
    
    /**
     * Look up the user from the AFS id using bpfinger
     * 
     * @param  afsId  An AFS user id (like stadtlag)
     * @throws IcofException 
     */
    private void fingerAfsId() throws IcofException {
    	
    	// Set to defaults
    	setIntranetId(getAfsId() + "@us.ibm.com");
    	setEmailAddress(getIntranetId());
        setFirstName("");
        setLastName("");
        
    	// Run the bpfinger command on the current user
        String command = FINGER + " " + afsId;
        Vector<String> results = new Vector<String>();
        StringBuffer errorMsg = new StringBuffer();
        try {
        	int rc = IcofSystemUtil.execSystemCommand(command, errorMsg, results);
        }
        catch(IcofException ignore) { return; }
        
        // If no results or incorrect number results then return
        if (results.isEmpty() || results.size() < 3) {
        	return;
        }
        
        // Parse the intranet id, first name and last name from the result
        Vector<String> tokens = new Vector<String>();
        Iterator<String> iter = results.iterator();
        while (iter.hasNext()) {
        	String line = iter.next();
        	if (line.indexOf("=") < 0)   // Skip lines without a = 
        		continue;  
        	tokens.clear();
        	IcofCollectionsUtil.parseString(line, "=", tokens, true);
        	String key = tokens.get(0).trim();
        	String value = tokens.get(1).trim();
        	        	
        	if (key.equalsIgnoreCase("NAME")) {
        		tokens.clear();
        		IcofCollectionsUtil.parseString(value, "\"", tokens, true);
        		String fullName = tokens.get(1);
        		if (tokens.size() >= 2) {
        			tokens.clear();
        			IcofCollectionsUtil.parseString(fullName, ",", tokens, true);
        			setLastName(tokens.get(0));
        			setFirstName(tokens.get(1));
        		}
        	}
        	else if (key.equalsIgnoreCase("INTERNET")) {
        		tokens.clear();
        		IcofCollectionsUtil.parseString(value, "\"", tokens, true);
        		setIntranetId(tokens.get(1));
        		setEmailAddress(getIntranetId());
        	}
        	
        }
        
    }

    
    /**
     * Data Members
     */
    private short id;
    private String lastName;
    private String firstName;
    private String emailAddress;
    private String afsId;
    private String intranetId;

    
    /**
     * Getters
     */
    public short getId() { return id; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getEmailAddress() { return emailAddress; }
    public String getAfsId() { return afsId; }
    public String getIntranetId() { return intranetId; }


    /**
     * Setters
     */
    private void setId(short anId) { id = anId; }
    private void setLastName(String aName) { lastName = aName; }
    private void setFirstName(String aName) { firstName = aName; }
    private void setEmailAddress(String anAddress) { emailAddress = anAddress; }
    private void setAfsId(String aName) { afsId = aName; }
    private void setIntranetId(String aName) { intranetId = aName; }
    
    
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
    public void setLookupAfsStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + AFSID_COL + " =  ? " +
                       " AND " + DELETED_ON_COL + " is NULL";
        
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
    public void setLookupIntranetStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where " + INTRANETID_COL + " = ? " +
                       " AND " + DELETED_ON_COL + " is NULL";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }
    

    /**
     * Create a PreparedStatement to lookup this object by first/last name.
     * 
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setLookupByNamesStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "select " + ALL_COLS + 
                       " from " + TABLE_NAME + 
                       " where upper(" + LAST_NAME_COL + ") like  ? " +
                       " and upper(" + FIRST_NAME_COL + ") like ? " +
                       " and " + DELETED_BY_COL + " is null";
        
        // Set and prepare the query and statement.
        setQuery(xContext, query);
        
    }

    
    /**
     * Create a PreparedStatement to update this object.
     * @param xContext  Application context.
     * @return PreparedStatement
     * @throws IcofException 
     */
    public void setUpdateRowStatement(EdaContext xContext) throws IcofException {

        // Define the query.
        String query = "update " + TABLE_NAME + 
                       " set " +  AFSID_COL + " = ? " + ", " +
                                  INTRANETID_COL + " = ? " + ", " +
                                  UPDATED_BY_COL + " = ? " + ", " +
                                  UPDATED_ON_COL + " = ? " +
                       " where " + ID_COL + " =  ? ";
        
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
                       " ( " + ALL_COLS + " )" + 
                       " values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        
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
     * Look up the User by id.
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
     * Look up this object by intranet id.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByIntranet(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupIntranetStatement(xContext);
        
        try {
            getStatement().setString(1, getIntranetId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByIntranet()",
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
                                                 "dbLookupByIntranet()",
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
     * Look up this object by last/first name.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByNames(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupByNamesStatement(xContext);
        
        try {
            getStatement().setString(1, getFirstName() + "%");
            getStatement().setString(2, getLastName()  + "%");
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByNames()",
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
                                                 "dbLookupByNames()",
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
     * Look up this object by AFS id.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupByAfs(EdaContext xContext) throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupAfsStatement(xContext);
        
        try {
            getStatement().setString(1, getAfsId());
        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbLookupByAfs()",
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
                                                 "dbLookupByAfs()",
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

    	dbAddRow(xContext, null);
        
    }


    /**
     * Insert a new row.
     * 
     * @param editor    Person adding this row.  If null use the User info.
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbAddRow(EdaContext xContext, User_Db editor)
    throws IcofException{

    	// Set the creator string
    	String creator = getIntranetId();
    	if (editor != null) {
    		creator = editor.getIntranetId();
    	}
    	
    	// Get the next id for this new row.
    	setNextIdStatement(xContext);
    	setId(getNextSmallIntId(xContext));
    	closeStatement(xContext);
    	
        // Create the SQL query in the PreparedStatement.
        setAddRowStatement(xContext);
        Timestamp now = new Timestamp(new java.util.Date().getTime());
        try {
            getStatement().setLong(1, getId());
            getStatement().setString(2, getLastName());
            getStatement().setString(3, getFirstName());
            getStatement().setString(4, getEmailAddress());
            getStatement().setString(5, getAfsId());
            getStatement().setString(6, getIntranetId());
            getStatement().setString(7, creator);
            getStatement().setTimestamp(8, now);
            getStatement().setString(9, creator);
            getStatement().setTimestamp(10, now);
            getStatement().setString(11, null);
            getStatement().setString(12, null);
            
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
     * Update the AFS and/or Intranet ids for this object.
     * 
     * @param xContext     An application context object.
     * @param editor       Person making this update.
     * @throws          Trouble querying the database.
     */
    public void dbUpdateRow(EdaContext xContext, User_Db editor)
    throws IcofException{

        // Create the SQL query in the PreparedStatement.
        setUpdateRowStatement(xContext);

        Timestamp now = new Timestamp(new java.util.Date().getTime());
        try {
        	
            getStatement().setString(1, getAfsId());
            getStatement().setString(2, getIntranetId());
            getStatement().setString(3, editor.getIntranetId());
            getStatement().setTimestamp(4, now);
            getStatement().setLong(5, getId());

        }
        catch(SQLException trap) {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "dbUpdateRow()",
                                                 IcofException.SEVERE,
                                                 "Unable to prepare SQL statement.",
                                                 IcofException.printStackTraceAsString(trap) + 
                                                 "\n" + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! updateRows(xContext)) {
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
     
        // Set updated data.
        setUpdatedBy(editor.getIntranetId());
        setUpdatedOn(now);
        setLoadFromDb(true);

        
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
        setLastName(rs.getString(LAST_NAME_COL));
        setFirstName(rs.getString(FIRST_NAME_COL));
        setEmailAddress(rs.getString(EMAIL_COL));
        setAfsId(rs.getString(AFSID_COL));
        setIntranetId(rs.getString(INTRANETID_COL));
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
        buffer.append("Last name: " + getLastName() + "\n");
        buffer.append("First name: " + getFirstName() + "\n");
        buffer.append("Email address: " + getEmailAddress() + "\n");
        buffer.append("AFS id: " + getAfsId() + "\n");
        buffer.append("Intranet id: " + getIntranetId() + "\n");
        buffer.append(audit);
        
        return buffer.toString();
        
    }

    
    /**
     * Populate based on the Intranet id.
     * @throws IcofException 
     */
    public void populateFromBluePages(EdaContext xContext) throws IcofException {
        
        UserBluePage bluePage = new UserBluePage(xContext, getIntranetId());
        String fullName = bluePage.getFullName();
        
        if (getAfsId() == null) {
        	setAfsId("UNKNOWN");
        }
        int index = fullName.indexOf(",");
        if (index > -1) {
            setFirstName(fullName.substring(index + 1));
            setFirstName(getFirstName().trim());
            setLastName(fullName.substring(0, index));
            setLastName(getLastName().trim());
        }
        else {
            setFirstName("FUNCTIONAL");
            setLastName("ID");
        }
        
        setEmailAddress(getIntranetId());
    }


    /**
     * Populate the user's information from AFS
     * 
     * @param xContext
     */
    public void populateFromAfs(EdaContext xContext) throws IcofException {
        
        if (getAfsId() != null) {
        	fingerAfsId();
        }
        else {
            IcofException ie = new IcofException(this.getClass() .getName(),
                                                 "populateFromAfs()",
                                                 IcofException.SEVERE,
                                                 "The AFS id is empty.\n",
                                                 getAfsId());
            xContext.getSessionLog().log(ie);
            throw ie;
            
        }
        
    }
    
    
    /**
     * Verify the AFS is the specified id
     *
     * @param xContext       Application context
     * @param String userId  The desired AFS id
     * @throws IcofException 
     */
    public boolean validateUser(EdaContext xContext, String userId) 
    throws IcofException {

	boolean result = false;
	
	String afsId = getAfsId().trim();
	if (afsId.equals(userId)) {
	    result = true;
	}
	else {
	    throw new IcofException(this.getClass().getName(), "validateUser()", 
	                            IcofException.SEVERE, 
	                            "User is not authorized to run this application\n", 
	                            " User's AFS id: " + afsId + "\n" + 
	                            " Auth AFS id is " + userId + "\n");
	}
	
	return result;
	
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
