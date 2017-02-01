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
* Location business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 06/13/2013 GFS  Initial coding. 
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.io.Serializable;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.Location_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class Location  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 4518193689604925123L;


	/**
     * Constructor - takes a name
     * 
     * @param xContext  Application context
     * @param aName     A Location name (BUILD, DEV, ... )
     * @throws IcofException 
     */
    public Location(EdaContext xContext, String aName) 
    throws IcofException {
        setName(aName);
    }

    
    /**
     * Constructor - takes objects
     * 
     * @param xContext   Application context
     * @param aLocation  Database object
     */
    public Location(EdaContext xContext, Location_Db aLocation) {
        setLocation(aLocation);
        setName(getLocation().getName());
    }

    
    /**
     * Constructor - takes IDs
     * 
     * @param xContext    Application context
     * @param anId        A database id
     * @throws IcofException 
     */
    public Location(EdaContext xContext, short anId) 
    throws IcofException {
        dbLookupById(xContext, anId);
        setName(getLocation().getName());
        
    }

    
    /**
     * Data Members
     */
    private String name;
    private Location_Db location;

    
    /**
     * Getters
     */
    public String getName() { return name; }
    public Location_Db getLocation() { return location; }

    
    /**
     * Setters
     */
    private void setLocation(Location_Db aLocation) { location = aLocation; }
    private void setName(String aName) { name = aName; }

    
    /**
     * Lookup the object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A database id
     * @throws IcofException 
     */
    public void dbLookupById(EdaContext xContext, short anId) 
    throws IcofException { 
    
    	if (getLocation() == null) {
        	try {
        		location = new Location_Db(anId);
        		location.dbLookupById(xContext);
        	}
        	catch(IcofException trap) {
        		location = null;
        		throw new IcofException(this.getClass().getName(), "dbLookupById()",
        				                IcofException.SEVERE,
        				                "Unable to find Location (" +
        				                anId + ") in the database.\n",
        				                trap.getMessage());
        	}
        }            
    }

    
    /**
     * Lookup the object from the name
     * 
     * @param xContext   Application context.
     * @throws IcofException 
     */
    public void dbLookupByName(EdaContext xContext) 
    throws IcofException {
    	if (getLocation() == null) {
        	try {
        		location = new Location_Db(getName());
        		location.dbLookupByName(xContext);
        	}
        	catch(IcofException trap) {
        		throw new IcofException(this.getClass().getName(), "dbLookupByName()",
        				                IcofException.SEVERE,
        				                "Unable to find Location (" +
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
    		getLocation().dbAddRow(xContext);
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
    	getLocation().dbUpdateRow(xContext, newName, editor);
    	
    }

    
    /**
     * Delete this object from the database
     * 
     * @param xContext    Application object
     * @param editor      Person adding this object
     * @throws IcofException 
     */
    public void dbDelete(EdaContext xContext, User_Db editor) throws IcofException {
    	getLocation().dbDeleteRow(xContext, editor);
    }

    
    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
        return String.valueOf(getLocation().getId());
    }
    
 
    /**
     * Display this object as a string
     * @param xContext  Application context
     * @return    This object as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("StageName\n---------------\n");
        buffer.append("Name: " + getName() + "\n");
        
        return buffer.toString();

    }

}
