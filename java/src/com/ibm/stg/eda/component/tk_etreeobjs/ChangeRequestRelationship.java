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
* ChangeRequest Relationship business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 12/01/2011 GFS  Initial coding. 
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestRelationship_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestRelationship  {

    /**
     * Constructor - takes an ID
     * 
     * @param xContext  Application context
     * @param anId      From ChangeRequest object
     * @throws IcofException 
     */
    public ChangeRequestRelationship(EdaContext xContext, long anId) 
    throws IcofException {
    	dbLookupById(xContext, anId);
    }

    
	/**
     * Constructor - takes a BranchName
     * 
     * @param xContext       Application context
     * @param aFromCr        From ChangeRequest object
     * @param aToCr          From ChangeRequest object
     * @param aRelationship  Relationship name
     * @throws IcofException 
     */
    public ChangeRequestRelationship(EdaContext xContext, ChangeRequest aFromCr,
                                     ChangeRequest aToCr, String aRelationship) 
    throws IcofException {
        setFromChangeReq(aFromCr);
        setToChangeReq(aToCr);
        setRelationship(aRelationship);
    }


    /**
     * Data Members
     */
    private ChangeRequest fromChangeRequest;
    private ChangeRequest toChangeRequest;
    private String relationship;
    private ChangeRequestRelationship_Db dbObject;

    
    /**
     * Getters
     */
    public ChangeRequest getFromChangeReq() { return fromChangeRequest; }
    public ChangeRequest getToChangeReq() { return toChangeRequest; }
    public String getRelationship() { return relationship; }
    public ChangeRequestRelationship_Db getDbObject() { return dbObject; }

    
    /**
     * Setters
     */
    private void setFromChangeReq(ChangeRequest aCr) { fromChangeRequest = aCr; }
    private void setToChangeReq(ChangeRequest aCr) { toChangeRequest = aCr; }
    private void setRelationship(String aName) { relationship = aName; }
    

    /**
8     * Lookup the object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A database id
     * @throws IcofException 
     */
    public void dbLookupById(EdaContext xContext, long anId) 
    throws IcofException { 
        if (getDbObject() == null) {
        	try {
        		dbObject = new ChangeRequestRelationship_Db(anId);
        		dbObject.dbLookupById(xContext);
        	}
        	catch(IcofException trap) {
        		dbObject = null;
        		throw new IcofException(this.getClass().getName(), "dbLookupById()",
        				                IcofException.SEVERE,
        				                "Unable to find ChangeRequestRelationship (" +
        				                anId + ") in the database.\n",
        				                trap.getMessage());
        	}
        }            
    }

    
    /**
     * Lookup the object from the from CR, to CR and relationship names
     * 
     * @param xContext   Application context.
     * @throws IcofException 
     */
    public void dbLookupByAll(EdaContext xContext) 
    throws IcofException {
    	
    	if (getDbObject() == null) {
        	try {
        		dbObject = 
        			new ChangeRequestRelationship_Db(getFromChangeReq().getChangeRequest(), 
        			                                 getToChangeReq().getChangeRequest(),
        			                                 getRelationship());
        		dbObject.dbLookupByAll(xContext);
        	}
        	catch(IcofException trap) {
        		throw new IcofException(this.getClass().getName(), "dbLookupByNameComVersion()",
        				                IcofException.SEVERE,
        				                "Unable to find ChangeRequestRelationship " +
        				                "in the database.\n",
        				                trap.getMessage());
        	}
        }
    	
    }
    

    /**
     * Add this object to the database
     * 
     * @param xContext  Application object
     * @throws IcofException
     * @return True if object created false object existing object. 
     */
    public boolean dbAdd(EdaContext xContext) throws IcofException {
    	
    	try {
    		// Lookup the object in the database first.
    		dbLookupByAll(xContext);
    		return false;
    	}
    	catch(IcofException trap) {
    		getDbObject().dbAddRow(xContext);
    	}
    	
    	return true;
    	
    }


    /**
     * Update this object in the database
     * 
     * @param xContext         Application object
     * @param newRelationship  New relationship name
     * @throws IcofException 
     */
    public void dbUpdate(EdaContext xContext, String newRelationship)
    throws IcofException {

    	try {
    		// Lookup the object in the database first.
    		dbLookupByAll(xContext);
    	}
    	catch(IcofException trap) {
    		throw new IcofException(this.getClass().getName(), "dbUpdate()",
	                                IcofException.SEVERE,
	                                "Unable to find existing object in the " +
	                                "database.\n",
	                                 trap.getMessage());
    	}

    	// Update the object
    	getDbObject().dbUpdateRow(xContext, newRelationship);
    	
    }

    
    /**
     * Delete this object from the database
     * 
     * @param xContext    Application object
     * @throws IcofException 
     */
    public void dbDelete(EdaContext xContext) throws IcofException {
    	
    	try {
    		// Lookup the object in the database first.
    		dbLookupByAll(xContext);
    	}
    	catch(IcofException trap) {
    		throw new IcofException(this.getClass().getName(), "dbDelete()",
	                                IcofException.SEVERE,
	                                "Unable to find existing object in the" +
	                                " database.\n",
	                                 trap.getMessage());
    	}
    	
    	getDbObject().dbDeleteRow(xContext);

    }

    
    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
        return String.valueOf(getDbObject().getId());
    }
    
 
    /**
     * Display this object as a string
     * @param xContext  Application context
     * @return    This object as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("From CR: " + getFromChangeReq().getClearQuest() + "\n");
        buffer.append("To CR: " + getToChangeReq().getClearQuest() + "\n");
        buffer.append("Relationship " + getRelationship() + "\n");
        return buffer.toString();

    }
    
}
