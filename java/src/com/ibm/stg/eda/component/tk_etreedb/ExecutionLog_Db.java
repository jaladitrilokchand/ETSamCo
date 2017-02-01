/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2012 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *
 *-PURPOSE---------------------------------------------------------------------
 * ExecutionLog DB class
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 04/12/2012 GFS  Initial coding.
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
import com.ibm.stg.iipmds.common.IcofException;

public class ExecutionLog_Db extends TkAudit {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1561050385281292911L;
	/**
	 * Constants.
	 */
	public static final String TABLE_NAME = "TK.EXECUTIONLOG";
	public static final String NAME_COL = "NAME";
	public static final String MACHINE_COL = "MACHINE_NAME";
	public static final String COMMENT_COL = "COMMENT";
	public static final String ENDED_ON_COL = "END_TMSTMP";
	public static final String ALL_COLS = NAME_COL + "," +
	                           MACHINE_COL + ", " + 
	                           CREATED_BY_COL + ", " + 
	                           COMMENT_COL + ", " +  
	                           CREATED_ON_COL + "," + 
	                           ENDED_ON_COL;

	/**
	 * Constructor - takes a Component name
	 * 
	 * @param aName  Name of a TK Component (ess, edautils, einstimer ...)
	 */
	public ExecutionLog_Db(String aName, String aMachine,
	                       String aComment, String aUser) {
		setName(aName);
		setMachineName(aMachine);
		setComment(aComment);
		setCreatedBy(aUser);
	}


	/**
	 * Constructor - takes a Component name
	 * 
	 * @param aName  Name of a TK Component (ess, edautils, einstimer ...)
	 */
	public ExecutionLog_Db(String aName, String aMachine,
	                       String aComment, String aUser,
	                       Timestamp aCreatedOn, Timestamp aEndedOn) {
		setName(aName);
		setMachineName(aMachine);
		setComment(aComment);
		setCreatedBy(aUser);
		setCreatedOn(aCreatedOn);
		setEndedOn(aEndedOn);
	}



	/**
	 * Data Members
	 */
	private String name;
	private String machineName;
	private String comment;
	private Timestamp endedOn;


	/**
	 * Getters
	 */
	public String getName() { return name; }
	public String getMachineName() { return machineName; }
	public String getComment() { return comment; }
	public Timestamp getEndedOn() { return endedOn; }


	/**
	 * Setters
	 */
	private void setName(String aName) { name = aName; }
	private void setMachineName(String aName) { machineName = aName; }
	private void setComment(String aStr) { comment = aStr; }
	private void setEndedOn(Timestamp aTms) { endedOn = aTms; }


	/**
	 * Create a PreparedStatement to lookup this object by id.
	 * @param xContext  Application context.
	 * @return PreparedStatement
	 * @throws IcofException 
	 */
	public void setLookupNameStatement(EdaContext xContext) 
	throws IcofException {

		// Define the query.
		String query = "select " + ALL_COLS + 
		               " from " + TABLE_NAME + 
		               " where " + NAME_COL + " = ? ";

		// Set and prepare the query and statement.
		setQuery(xContext, query);

	}


	/**
	 * Create a PreparedStatement to lookup this object by TK and Stage.
	 * @param xContext  Application context.
	 * @return PreparedStatement
	 * @throws IcofException 
	 */
	public void setLookupStillRunningStatement(EdaContext xContext)
	throws IcofException {

		// Define the query.
		String query = "select * " +  
		               " from " + TABLE_NAME + 
		               " where " + NAME_COL + " = ? " +
		               " and " + ENDED_ON_COL + " is null";

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
		               " ( " + 
		               NAME_COL + ", " +  
		               MACHINE_COL + ", " +
		               COMMENT_COL + ", " +
		               CREATED_BY_COL + ", " +
		               CREATED_ON_COL + 
		               " )" +
		               " values( ?, ?, ?, ?, " + CURRENT_TMS + " )";

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
		               " where " + NAME_COL + " = ? " + 
		               " and " + MACHINE_COL + " = ? " +
		               " and " + CREATED_ON_COL + " = ? ";

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
	public void setUpdateStatement(EdaContext xContext)
	throws IcofException {

		// Define the query.
		String query = "update " + TABLE_NAME + 
		               " set " +  ENDED_ON_COL + " = " + CURRENT_TMS;
		if ((getComment() != null) && (! getComment().equals(""))) {
			query += ", " + COMMENT_COL + " = ? ";
		}
		query += " where " + NAME_COL + " = ? " + 
		         " and " + ENDED_ON_COL + " is null ";

		// Set and prepare the query and statement.
		setQuery(xContext, query);

	}


	/**
	 * Look up the ExecutionLog rows by Application Name
	 * 
	 * @param xContext  An application context object.
	 * @throws          Trouble querying the database.
	 */
	public Vector<ExecutionLog_Db> dbLookupByName(EdaContext xContext)
	throws IcofException{

		// Create the SQL query in the PreparedStatement.
		setLookupNameStatement(xContext);

		try {
			getStatement().setString(1, getName());
		}
		catch(SQLException trap) {
			IcofException ie = 
				new IcofException(this.getClass() .getName(),
				                  "dbLookupByName()",
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
		Vector<ExecutionLog_Db> rows = new Vector<ExecutionLog_Db>();
		try {
			while (rs.next()) {
				String aName =  rs.getString(NAME_COL);
				String aMachineName =  rs.getString(MACHINE_COL);
				String aComment =  rs.getString(COMMENT_COL);
				String aCreatedBy =  rs.getString(CREATED_BY_COL);
				Timestamp aCreatedOn =  rs.getTimestamp(CREATED_ON_COL);
				Timestamp aEndedOn =  rs.getTimestamp(ENDED_ON_COL);
				ExecutionLog_Db comp = new ExecutionLog_Db(aName, aMachineName, 
				                                           aComment, aCreatedBy,
				                                           aCreatedOn, aEndedOn);
				rows.add(comp);
			}

		}
		catch(SQLException ex) {
			throw new IcofException(this.getClass().getName(), 
			                        "dbLookupByName()",
			                        IcofException.SEVERE, 
			                        "Error reading DB query results.",
			                        ex.getMessage());
		}

		// Close the PreparedStatement.
		closeStatement(xContext);

		return rows;

	}

	
    /**
     * Look up the row for a running instance of this application.
     * 
     * @param xContext  An application context object.
     * @throws          Trouble querying the database.
     */
    public void dbLookupStillRunning(EdaContext xContext)
    throws IcofException{
        
        // Create the SQL query in the PreparedStatement.
        setLookupStillRunningStatement(xContext);
        
        try {
            getStatement().setString(1, getName());
        }
        catch(SQLException trap) {
            IcofException ie = 
            	new IcofException(this.getClass() .getName(),
            	                  "dbLookupStillRunning()",
            	                  IcofException.SEVERE,
            	                  "Unable to prepare SQL statement.",
            	                  IcofException.printStackTraceAsString(trap) + 
            	                  "\n" + getQuery());
            xContext.getSessionLog().log(ie);
            throw ie;
        }
        
        // Run the query.
        if (! selectSingleRow(xContext)) {
            IcofException ie = 
            	new IcofException(this.getClass() .getName(),
            	                  "dbLookupStillRunning()",
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
			getStatement().setString(1, getName());
			getStatement().setString(2, getMachineName());
			getStatement().setString(3, "[START]-" + getComment());
			getStatement().setString(4, getCreatedBy());
			
//			printQuery(xContext);
//			System.out.println("Name: " + getName());
//			System.out.println("Machine: " + getMachineName());
//			System.out.println("Comment: " + getComment());
//			System.out.println("CreatedBy: " + getCreatedBy());
			
		}
		catch(SQLException trap) {
			IcofException ie = 
				new IcofException(this.getClass() .getName(),
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

	}


	/**
	 * Update the EndedOn Timestamp row.
	 * 
	 * @param xContext  An application context object.
	 * @param aName     New component name
	 * @throws          Trouble querying the database.
	 */
	public void dbUpdateRow(EdaContext xContext)
	throws IcofException{
		
		// If there are new commenst then append new comments to the
		// existing comments
		if ((getComment() != null) && (! getComment().equals(""))) {
			String newComment = getComment();
			dbLookupStillRunning(xContext);
			if ((getComment() == null) || getComment().equals("")) {
				setComment("[END]-" + newComment);
			}
			else {
				setComment(getComment() + ";[END]-" + newComment);
			}
		}
		
		// Create the SQL query in the PreparedStatement.
		setUpdateStatement(xContext);
		try {
			if ((getComment() != null) && (! getComment().equals(""))) {
				getStatement().setString(1, getComment());
				getStatement().setString(2, getName());
			}
			else {
				getStatement().setString(1, getName());
			}
		}
		catch(SQLException trap) {
			IcofException ie = 
				new IcofException(this.getClass().getName(),
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
			getStatement().setString(1, getName());
			getStatement().setString(2, getMachineName());
			getStatement().setTimestamp(3, getCreatedOn());

		}
		catch(SQLException trap) {
			IcofException ie = 
				new IcofException(this.getClass().getName(),
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

		setName(rs.getString(NAME_COL));
		setMachineName(rs.getString(MACHINE_COL));
		setComment(rs.getString(COMMENT_COL));
		setCreatedBy(rs.getString(CREATED_BY_COL));
		setCreatedOn(rs.getTimestamp(CREATED_ON_COL));
		setEndedOn(rs.getTimestamp(ENDED_ON_COL));
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
		buffer.append("Name: " + getName() + "\n");
		buffer.append("Machine: " + getMachineName() + "\n");
		buffer.append("Comment: " + getComment() + "\n");
		buffer.append("Ended on: " + getEndedOn() + "\n");
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
		return getName() + "#" + 
		       getMachineName() + "#" +
		       getCreatedBy() + "#" + 
		       getCreatedOn().toString();
	}


}
