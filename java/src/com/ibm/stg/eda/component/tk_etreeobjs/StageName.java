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
* StageName business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 04/18/2011 GFS  Initial coding. 
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.io.Serializable;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.StageName_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class StageName  implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 4518193689604925123L;


	/**
     * Constructor - takes a name
     * 
     * @param xContext  Application context
     * @param aName     A Stage name (READY, NEW ...)
     * @throws IcofException 
     */
    public StageName(EdaContext xContext, String aName) 
    throws IcofException {
        setName(aName);
    }

    
    /**
     * Constructor - takes objects
     * 
     * @param xContext  Application context
     * @param aStage    Database object
     */
    public StageName(EdaContext xContext, StageName_Db aStage) {
        setStage(aStage);
        setName(getStage().getName());
    }

    
    /**
     * Constructor - takes IDs
     * 
     * @param xContext    Application context
     * @param anId        A ChangeRequestStatus object id
     * @throws IcofException 
     */
    public StageName(EdaContext xContext, short anId) 
    throws IcofException {
        dbLookupById(xContext, anId);
        setName(getStage().getName());
        
    }

    
    /**
     * Data Members
     */
    private String name;
    private StageName_Db stage;

    
    /**
     * Getters
     */
    public String getName() { return name; }
    public StageName_Db getStage() { return stage; }

    
    /**
     * Setters
     */
    private void setStage(StageName_Db aStage) { stage = aStage; }
    private void setName(String aName) { name = aName; }

    
    /**
     * Lookup the object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A database id
     * @throws IcofException 
     */
    public void dbLookupById(EdaContext xContext, short anId) throws IcofException { 
        if (getStage() == null) {
        	try {
        		stage = new StageName_Db(anId);
        		stage.dbLookupById(xContext);
        	}
        	catch(IcofException trap) {
        		stage = null;
        		throw new IcofException(this.getClass().getName(), "dbLookupById()",
        				                IcofException.SEVERE,
        				                "Unable to find StageName (" +
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
    	if (getStage() == null) {
        	try {
        		stage = new StageName_Db(getName());
        		stage.dbLookupByName(xContext);
        	}
        	catch(IcofException trap) {
        		throw new IcofException(this.getClass().getName(), "dbLookupByName()",
        				                IcofException.SEVERE,
        				                "Unable to find StageName (" +
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
    		getStage().dbAddRow(xContext);
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
    	getStage().dbUpdateRow(xContext, newName, editor);
    	
    }

    
    /**
     * Delete this object from the database
     * 
     * @param xContext    Application object
     * @param editor      Person adding this object
     * @throws IcofException 
     */
    public void dbDelete(EdaContext xContext, User_Db editor) throws IcofException {
    	getStage().dbDeleteRow(xContext, editor);
    }

    
    /**
     * Create a key from the ID.
     * 
     *  @param xContext  Application context object.
     *  @return          A Statement object.
     */
    public String getIdKey(EdaContext xContext) {
        return String.valueOf(getStage().getId());
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
