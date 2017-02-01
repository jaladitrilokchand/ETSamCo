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
* Chanage Request Type business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 09/06/2011 GFS  Initial coding. 
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestType_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestType  {

    /**
     * Constructor - takes a Type name
     * 
     * @param xContext  Application context
     * @param aName     A Type name (FEATURE, DEFECT ...)
     * @throws IcofException 
     */
    public ChangeRequestType(EdaContext xContext, String aName) 
    throws IcofException {
        setName(aName);
    }

    
    /**
     * Constructor - takes objects
     * 
     * @param xContext  Application context
     * @param aType     Database object
     */
    public ChangeRequestType(EdaContext xContext, ChangeRequestType_Db aType) {
        setDbObject(aType);
        setName(getDbObject().getName());
    }

    
    /**
     * Constructor - takes IDs
     * 
     * @param xContext    Application context
     * @param anId        A ChangeRequestStatus object id
     * @throws IcofException 
     */
    public ChangeRequestType(EdaContext xContext, short anId) 
    throws IcofException {
        dbLookupById(xContext, anId);
        setName(getDbObject().getName());
        
    }

    
    /**
     * Data Members
     */
    private String name;
    private ChangeRequestType_Db dbObject;

    
    /**
     * Getters
     */
    public String getName() { return name; }
    public ChangeRequestType_Db getDbObject() { return dbObject; }

    
    /**
     * Setters
     */
    private void setDbObject(ChangeRequestType_Db aType) { dbObject = aType; }
    private void setName(String aName) { name = aName; }

    
    /**
     * Lookup the object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A database id
     * @throws IcofException 
     */
    public void dbLookupById(EdaContext xContext, short anId) throws IcofException { 
        if (getDbObject() == null) {
        	try {
        		dbObject = new ChangeRequestType_Db(anId);
        		dbObject.dbLookupById(xContext);
        		setName(dbObject.getName());
        	}
        	catch(IcofException trap) {
        		dbObject = null;
        		throw new IcofException(this.getClass().getName(), "dbLookupById()",
        				                IcofException.SEVERE,
        				                "Unable to find ChangeRequestType (" +
        				                anId + ") in the database.\n",
        				                trap.getMessage());
        	}
        }            
    }

    
    /**
     * Lookup the object from the database id
     * 
     * @param xContext   Application context.
     * @throws IcofException 
     */
    public void dbLookupById(EdaContext xContext) throws IcofException { 
    	dbLookupById(xContext, getDbObject().getId());
    }

    
    /**
     * Lookup the object from the name
     * 
     * @param xContext   Application context.
     * @throws IcofException 
     */
    public void dbLookupByName(EdaContext xContext) 
    throws IcofException {
    	if (getDbObject() == null) {
        	try {
        		dbObject = new ChangeRequestType_Db(getName());
        		dbObject.dbLookupByName(xContext);
        	}
        	catch(IcofException trap) {
        		dbObject = null;
        		throw new IcofException(this.getClass().getName(), "dbLookupByName()",
        				                IcofException.SEVERE,
        				                "Unable to find ChangeRequestType (" +
        				                getName() + ") in the database.\n",
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
    	}
    	return true;
    }


    /**
     * Update this object in the database
     * 
     * @param xContext    Application object
     * @param newName     New name
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
        buffer.append("ChangeRequestType\n---------------\n");
        buffer.append("Name: " + getName() + "\n");
        
        return buffer.toString();

    }

}
