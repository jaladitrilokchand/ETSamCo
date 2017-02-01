/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 07/15/2010
*
*-PURPOSE---------------------------------------------------------------------
* Release business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 07/15/2010 GFS  Initial coding.
* 07/25/2010 GFS  Converted to using PreparedStatements.
* 10/25/2010 GFS  Moved prepared statements to TkRelease_TkPlatform class.
* 04/14/2011 GFS  Renamed to Release. Added dbAdd, dbUpdate and dbDelete
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.Release_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class Release  {
	

    /**
     * Constructor - takes the release names
     * 
     * @param xContext  Application context
     * @param aName     The release name (14.1, 15.1 ...)
     * @param altName   The alternate release name (18.1, 19.1 ...)
     */
    public Release(EdaContext xContext, String aName, String altName) {
    	setName(aName);
    	setAltName(altName);
    }

    
	/**
     * Constructor - takes objects
     * 
     * @param xContext  Application context
     * @param aRelease  A TkRelease object
     */
    public Release(EdaContext xContext, Release_Db aRelease) {
        setTkRelease(aRelease);
    	setName(getRelease().getName());
    	setAltName(getRelease().getAltName());
    }
    
    
    /**
     * Constructor - takes IDs
     * 
     * @param xContext    Application context
     * @param aReleaseId  A TkRelease object id
     * @throws IcofException 
     */
    public Release(EdaContext xContext, short aReleaseId) 
    throws IcofException {
        dbLookupById(xContext, aReleaseId);
    	setName(getRelease().getName());
    	setAltName(getRelease().getAltName());
    }

    
    /**
     * Data Members
     */
    private String name;
    private String altName;
    private Release_Db release;

    
    /**
     * Getters
     */
    public String getName() { return name; }
    public String getAltName() { return altName; }
    public Release_Db getRelease() { return release; }

        

    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    private void setAltName(String aName) { altName = aName; }
    private void setTkRelease(Release_Db aRelease) { release = aRelease; }
    
    
//    /**
//     * Lookup the platforms for this release
//     * @param xContext  Application Context
//     * @throws IcofException
//     */
//    private void setPlatforms(EdaContext xContext) throws IcofException {
//        Release_Platform_Db relPlats = new Release_Platform_Db(getRelease(), null);
//        platforms = relPlats.dbLookupPlatforms(xContext);
//    }
//    
//
//    /**
//     * Lookup the ToolKits for this release
//     * @param xContext  Application Context
//     * @throws IcofException
//     */
//    private void setToolKits(EdaContext xContext) throws IcofException {
//        RelVersion_Db relVer = new RelVersion_Db(getRelease(), null);
//        Hashtable vers = relVer.dbLookupByRelease(xContext);
//        
//        // Empty the toolKit collection
//        if (getToolKits(xContext) == null) {
//        	toolKits = new Hashtable();
//        }
//        else {
//        	toolKits.clear();
//        }
//        
//        // Convert the RelVersion_Db objects to ToolKits
//        Iterator iter = vers.values().iterator();
//        while (iter.hasNext()) {
//        	RelVersion_Db myVer = (RelVersion_Db) iter.next();
//        	ToolKit tk = new ToolKit(xContext, myVer);
//        	toolKits.put(tk.getIdKey(xContext), tk);
//        }
//        
//    }

    
    /**
     * Lookup the TkRelease object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       Object id
     * @throws IcofException 
     */
    public void dbLookupById(EdaContext xContext, short anId) 
    throws IcofException {
        if (getRelease() == null) {
            release = new Release_Db(anId);
            release.dbLookupById(xContext);
        }            
    }
    

    /**
     * Lookup the TkRelease object by name
     * 
     * @param xContext   Application context.
     * @throws IcofException 
     */
    public void dbLookupByName(EdaContext xContext) 
    throws IcofException {
        if (getRelease() == null) {
            release = new Release_Db(xContext, getName());
            release.dbLookupByName(xContext, false);
        }            
    }
    

    /**
     * Lookup the TkRelease object by alt name
     * 
     * @param xContext   Application context.
     * @throws IcofException 
     */
    public void dbLookupByAltName(EdaContext xContext) 
    throws IcofException {
        if (getRelease() == null) {
            release = new Release_Db(xContext, getAltName());
            release.dbLookupByName(xContext, true);
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
    public boolean dbAdd(EdaContext xContext, User_Db creator) 
    throws IcofException {
    	
    	try {
    		// Lookup the object in the database first.
    		dbLookupByName(xContext);
    		return false;
    	}
    	catch(IcofException trap) {
    		// Add the new object
    		getRelease().dbAddRow(xContext, creator);
    	}
    	return true;
    }


    /**
     * Update this object in the database
     * 
     * @param xContext    Application object
     * @param newName     New release name
     * @param newAltName  New alternate release name
     * @param editor      Person updating this object
     * @throws IcofException 
     */
    public void dbUpdate(EdaContext xContext, String newName, String newAltName,
    		             User_Db editor) throws IcofException {
    	
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
    	getRelease().dbUpdateRow(xContext, newName, newAltName, editor);

    }

    
    /**
     * Delete this object from the database
     * 
     * @param xContext    Application object
     * @param editor      Person deleting this object
     * @throws IcofException 
     */
    public void dbDelete(EdaContext xContext, User_Db editor) throws IcofException {
    	getRelease().dbDeleteRow(xContext, editor);
    }


    /**
     * Display this object as a string
     * @param xContext  Application context
     * @return    This object as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("Release object\n---------------\n");
        buffer.append("Release name: " + getName() + "\n");
        buffer.append("Alt Release name: " + getAltName() + "\n");
        
        return buffer.toString();

    }

    
    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
        return String.valueOf(getRelease().getId());
    }
        
}
