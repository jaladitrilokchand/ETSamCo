/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2013 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*
*-PURPOSE---------------------------------------------------------------------
* AccessType business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 08/02/2013 GFS  Initial coding. 
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.AccessType_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class AccessType  {

	
    /**
     * Constructor - takes a Access type name
     * 
     * @param xContext    Application context
     * @param aName       A Access Type name
     * @throws IcofException 
     */
    public AccessType(EdaContext xContext, String aName) 
    throws IcofException {
        setName(aName);
    }

    
    /**
     * Constructor - takes objects
     * 
     * @param xContext  Application context
     * @param aComp     A AccessType_Db object
     */
    public AccessType(EdaContext xContext, AccessType_Db aDbObject) {
        setDbObject(aDbObject);
        setName(getDbObject().getName());
    }

    
    /**
     * Constructor - takes IDs
     * 
     * @param xContext    Application context
     * @param anId        A EventName object id
     * @throws IcofException 
     */
    public AccessType(EdaContext xContext, long anId) 
    throws IcofException {
        dbLookupById(xContext, anId);
        setName(getDbObject().getName());
        
    }

    
    /**
     * Data Members
     */
    private String name;
    private AccessType_Db dbObject;

    
    /**
     * Getters
     */
    public String getName() { return name; }
    public AccessType_Db getDbObject() { return dbObject; }

    
    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    private void setDbObject(AccessType_Db aDbObject) { dbObject = aDbObject; }

    
    /**
     * Lookup this object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A database id
     * @throws IcofException 
     */
    public void dbLookupById(EdaContext xContext, long anId) 
    throws IcofException { 
        if (getDbObject() == null) {
        	try {
        		dbObject = new AccessType_Db(anId);
        		dbObject.dbLookupById(xContext);
        		populate(xContext);
        	}
        	catch(IcofException trap) {
        		dbObject = null;
        		throw new IcofException(this.getClass().getName(), "dbLookupById()",
        				IcofException.SEVERE,
                        "Unable to find AccessType (" + anId + ") in the database.\n",
                        trap.getMessage());
        	}
        }            
    }

    
    /**
     * Lookup the EventName object from the name
     * 
     * @param xContext   Application context.
     * @throws IcofException 
     */
    public void dbLookupByName(EdaContext xContext) 
    throws IcofException {
    	if (getDbObject() == null) {
        	try {
        		dbObject = new AccessType_Db(getName());
        		dbObject.dbLookupByName(xContext);
        		populate(xContext);
        	}
        	catch(IcofException trap) {
        		throw new IcofException(this.getClass().getName(), "dbLookupByName()",
        				IcofException.SEVERE,
                        "Unable to find AccessType (" + getName() + ") in the database.\n",
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
    		dbLookupByName(xContext);
    		return false;
    	}
    	catch(IcofException trap) {
    		// Add the new object
    		getDbObject().dbAddRow(xContext);
    		populate(xContext);	
    	}
    	return true;

    }


    /**
     * Update this object in the database
     * 
     * @param xContext    Application object
     * @param newName     New Event name
     * @throws IcofException 
     */
    public void dbUpdate(EdaContext xContext, String newName)
    throws IcofException {
    	
    	try {
    		// Lookup the object in the database first.
    		dbLookupByName(xContext);
    	}
    	catch(IcofException trap) {
    		throw new IcofException(this.getClass().getName(), "dbUpdate()",
	                                IcofException.SEVERE,
	                                "Unable to find existing object (" +
	                                 getName() + ") in the database.\n",
	                                 trap.getMessage());
    	}

    	// Update the object
    	getDbObject().dbUpdateRow(xContext, newName);
    	populate(xContext);
    	
    }

    
    /**
     * Delete this object from the database
     * 
     * @param xContext    Application object
     * @throws IcofException 
     */
    public void dbDelete(EdaContext xContext) throws IcofException {
    	getDbObject().dbDeleteRow(xContext);
    }

    
    /**
     * Lookup the EventName object from the name
     * 
     * @param xContext   Application context.
     * @throws IcofException 
     */
    public void dbLookupDbObject(EdaContext xContext) 
    throws IcofException {
    	
    	if (getDbObject() == null) {
    		throw new IcofException(this.getClass().getName(), "dbLookupDbObject()",
    		                        IcofException.SEVERE, 
    		                        "DB object is empty.\n", "");
    	}

    	try {
    		getDbObject().dbLookupById(xContext);
    		populate(xContext);
    	}
    	catch(IcofException trap) {
    		throw new IcofException(this.getClass().getName(), "dbLookupDbObject()",
    		                        IcofException.SEVERE,
    		                        "Unable to find AccessType in the database.\n",
    		                        trap.getMessage());
    	}
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
        buffer.append("AccessType object\n---------------\n");
        buffer.append("Name: " + getName() + "\n");
        
        return buffer.toString();

    }

    
    /**
     * Populate this object from the database object
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void populate(EdaContext xContext) throws IcofException {
		
		setName(getDbObject().getName());
    	
	}
    
}
