/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*
*-PURPOSE---------------------------------------------------------------------
* Execution Log business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 04/12/2012 GFS  Initial coding. 
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.sql.Timestamp;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.ExecutionLog_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class ExecutionLog  {

    /**
     * Constructor
     * 
     * @param xContext  Application context
     * @param aName     An application name
     * @param aMachine  A machine name
     * @param aComment  A comment string
     * @param aUser     A user's AFS id
     * @throws IcofException 
     */
    public ExecutionLog(EdaContext xContext, String aName, String aMachine,
                        String aComment, String aUser) {
        setName(aName);
        setMachine(aMachine);
        setComment(aComment);
        setUserId(aUser);
    }

    
    /**
     * Data Members
     */
    private String name;
    private String machine;
    private String comment;
    private String userId;
    private Timestamp startedOn;
    private Timestamp stoppedOn;
    private ExecutionLog_Db dbObject;
    private String message;
    
    /**
     * Getters
     */
    public String getName() { return name; }
    public String getMachine() { return machine; }
    public String getComment() { return comment; }
    public String getUserId() { return userId; }
    public Timestamp getStartedOn() { return startedOn; }
    public Timestamp getStoppedOn() { return stoppedOn; }
    public ExecutionLog_Db getDbObject() { return dbObject; }
    public String getMessage() { return message; }
    
    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    private void setMachine(String aName) { machine = aName; }
    private void setComment(String aName) { comment = aName; }
    private void setUserId(String aName) { userId = aName; }
    private void setStartedOn(Timestamp aTms) { startedOn = aTms; }
    private void setStoppedOn(Timestamp aTms) { stoppedOn = aTms; }
    
    
    /**
     * Query the for this application running
     * 
     * @param xContext   Application context.
     * @param bPopulate  If true populate from DB otherwise don't
     * @throws IcofException 
     */
    public boolean isRunning(EdaContext xContext) 
    throws IcofException {

    	populateDbObject(false);
    	boolean bRunning = true;
    	try {
    		getDbObject().dbLookupStillRunning(xContext);
    		populateFromDb();
    		setMessage();
    	}
    	catch(IcofException trap) { 
    		bRunning = false;
    	}
    	
    	return bRunning;
    	
    }
    
    
    /**
     * Add this object to the database
     * 
     * @param xContext  Application object
     * @throws IcofException 
     */
    public void dbAdd(EdaContext xContext)  throws IcofException {
    	
    	if (! isRunning(xContext)) {
    		getDbObject().dbAddRow(xContext);
    		populateFromDb();
    	}
    	else {
    		throw new IcofException(this.getClass().getName(), "dbAdd()",
	                                IcofException.SEVERE,
	                                "WARNING: wnable to add instance of this " +
	                                 "application (" + getName() + ") if " +
	                                 "another instance is already running.\n",
	                                 "");
    	}
    }


    /**
     * Update the end timestamp for this object
     *  
     * @param xContext    Application object
     * @throws IcofException 
     */
    public void dbUpdate(EdaContext xContext)
    throws IcofException {
    	
    	// If no running instance found then fail otherwise update the row
    	String sSaveComment = getComment();
    	if (isRunning(xContext)) {
    		
    		// Reset the comment for the update
    		setComment(sSaveComment);
    		populateDbObject(true);
    		
    		getDbObject().dbUpdateRow(xContext);
    		populateFromDb();
    	}
    	else {
    		throw new IcofException(this.getClass().getName(), "dbUpdate()",
	                                IcofException.SEVERE,
	                                "Unable to find running instance of this " +
	                                 "application (" + getName() + ").\n",
	                                 "");
    	}
    	
    }

    
    /**
     * Delete this object from the database
     * 
     * @param xContext    Application object
     * @param editor      Person adding this object
     * @throws IcofException 
     */
    public void dbDelete(EdaContext xContext)
    throws IcofException {
    	
    	populateDbObject(false);
    	getDbObject().dbDeleteRow(xContext);
    	
    }

    
    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
    	
    	String key = "";
    	if (getDbObject() != null)
    		key = String.valueOf(getDbObject().getIdKey(xContext));
    	
    	return key;
    	
    }
 
    
    /**
     * Display this object as a string
     * @param xContext  Application context
     * @return    This object as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("Execution Log\n---------------\n");
        buffer.append("Name: " + getName() + "\n");
        buffer.append("Machine: " + getMachine() + "\n");
        buffer.append("Comment: " + getComment() + "\n");
        buffer.append("User: " + getUserId() + "\n");
        if (getStartedOn() != null)
        	buffer.append("Started Timestamp: " + getStartedOn().toString() + "\n");
        else 
        	buffer.append("Started Timestamp: null\n");
        if (getStoppedOn() != null)
        	buffer.append("Stopped Timestamp: " + getStoppedOn().toString() + "\n");
        else 
        	buffer.append("Stopped Timestamp: null\n");
        
        return buffer.toString();

    }
    

    /**
     * Return the details on who's running this application
     * @param xContext  Application context
     * @return          A usage message
     */
    private void setMessage() {

    	if (getDbObject() != null) {
    		message = "The " + getName() + 
    		           " application is already running on " + getMachine() + 
    		           "\n - started by " + getUserId() +
    		           " on " + getStartedOn().toString();
    	}
    	
    }
    
    
    /**
     * Populate this objects members from the database object 
     */
    private void populateFromDb() {
    
    	if (getDbObject() != null) {
    		setName(getDbObject().getName());
    		setMachine(getDbObject().getMachineName());
    		setComment(getDbObject().getComment());
    		setUserId(getDbObject().getCreatedBy());
    		setStartedOn(getDbObject().getCreatedOn());
    		setStoppedOn(getDbObject().getEndedOn());
    	}
    	
    }
    
    
    /**
     * Instantiate the DB object
     * 
     * @param bForce  It true for the DB object to be repopulated otherwise
     *                populate only if null
     */
    private void populateDbObject(boolean bForce) {
    	
    	if ((getDbObject() == null) || (bForce)) {
    		dbObject = new ExecutionLog_Db(getName(), getMachine(),
    		                               getComment(), getUserId());
    	}
    	
    }

    
}
