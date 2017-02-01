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
* Chanage Request Status business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 04/15/2011 GFS  Initial coding. 
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.io.Serializable;
import java.util.Hashtable;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequestStatus_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class ChangeRequestStatus  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 5192632683176968188L;


	/**
     * Constructor - takes a Status name
     * 
     * @param xContext  Application context
     * @param aName     A Status name (READY, NEW ...)
     * @throws IcofException 
     */
    public ChangeRequestStatus(EdaContext xContext, String aName) 
    throws IcofException {
        setName(aName);
    }

    
    /**
     * Constructor - takes objects
     * 
     * @param xContext  Application context
     * @param aStatus   Database object
     */
    public ChangeRequestStatus(EdaContext xContext, ChangeRequestStatus_Db aStatus) {
        setStatus(aStatus);
        setName(getStatus().getName());
    }

    
    /**
     * Constructor - takes IDs
     * 
     * @param xContext    Application context
     * @param anId        A ChangeRequestStatus object id
     * @throws IcofException 
     */
    public ChangeRequestStatus(EdaContext xContext, short anId) 
    throws IcofException {
        dbLookupById(xContext, anId);
        setName(getStatus().getName());
        
    }

    
    /**
     * Data Members
     */
    private String name;
    private ChangeRequestStatus_Db status;

    
    /**
     * Getters
     */
    public String getName() { return name; }
    public ChangeRequestStatus_Db getStatus() { return status; }

    
    /**
     * Setters
     */
    private void setStatus(ChangeRequestStatus_Db aStatus) { status = aStatus; }
    private void setName(String aName) { name = aName; }

    
    /**
     * Lookup the object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A database id
     * @throws IcofException 
     */
    public void dbLookupById(EdaContext xContext, short anId) throws IcofException { 
        if (getStatus() == null) {
        	try {
        		status = new ChangeRequestStatus_Db(anId);
        		status.dbLookupById(xContext);
        		setName(status.getName());
        	}
        	catch(IcofException trap) {
        		status = null;
        		throw new IcofException(this.getClass().getName(), "dbLookupById()",
        				                IcofException.SEVERE,
        				                "Unable to find ChangeRequestStatus (" +
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
    	dbLookupById(xContext, getStatus().getId());
    }

    
    /**
     * Lookup the Component object from the name
     * 
     * @param xContext   Application context.
     * @throws IcofException 
     */
    public void dbLookupByName(EdaContext xContext) 
    throws IcofException {
    	if (getStatus() == null) {
        	try {
        		status = new ChangeRequestStatus_Db(getName());
        		status.dbLookupByName(xContext);
        	}
        	catch(IcofException trap) {
        		status = null;
        		throw new IcofException(this.getClass().getName(), "dbLookupByName()",
        				                IcofException.SEVERE,
        				                "Unable to find ChangeRequestStatus (" +
        				                getName() + ") in the database.\n",
        				                trap.getMessage());
        	}
        }
    }
    

    /**
     * Add this object to the database
     * 
     * @param xContext  Application object
     * @param creator   Person adding this object
     * @throws IcofException
     * @return True if object created false object existing object. 
     */
    public boolean dbAdd(EdaContext xContext, User_Db creator) throws IcofException {
    	
    	try {
    		// Lookup the object in the database first.
    		dbLookupByName(xContext);
    		return false;
    	}
    	catch(IcofException trap) {
    		// Add the new object
    		getStatus().dbAddRow(xContext, creator);
    	}
    	return true;
    }


    /**
     * Update this object in the database
     * 
     * @param xContext    Application object
     * @param newName     New name
     * @param editor      Person updating this object
     * @throws IcofException 
     */
    public void dbUpdate(EdaContext xContext, String newName, User_Db editor)
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
    	getStatus().dbUpdateRow(xContext, newName, editor);
    	
    }

    
    /**
     * Delete this object from the database
     * 
     * @param xContext    Application object
     * @param editor      Person adding this object
     * @throws IcofException 
     */
    public void dbDelete(EdaContext xContext, User_Db editor) throws IcofException {
    	getStatus().dbDeleteRow(xContext, editor);
    }

    
    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
        return String.valueOf(getStatus().getId());
    }
 
    /**
     * Display this object as a string
     * @param xContext  Application context
     * @return    This object as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("ChangeRequestStatus\n---------------\n");
        buffer.append("Name: " + getName() + "\n");
        
        return buffer.toString();

    }

}
