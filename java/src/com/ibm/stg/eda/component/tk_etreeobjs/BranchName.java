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
 * BranchName business object.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 07/15/2011 GFS  Initial coding. 
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreeobjs;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.BranchName_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class BranchName  {

    /**
     * Constructor - takes a Branch name
     * 
     * @param xContext    Application context
     * @param aName       A Branch name (trunk ...)
     * @throws IcofException 
     */
    public BranchName(EdaContext xContext, String aName) 
    throws IcofException {
	setName(aName);
    }


    /**
     * Constructor - takes objects
     * 
     * @param xContext  Application context
     * @param aComp     A BranchName_Db object
     */
    public BranchName(EdaContext xContext, BranchName_Db aBranchName) {
	setDbObject(aBranchName);
	setName(getDbObject().getName());
    }


    /**
     * Constructor - takes IDs
     * 
     * @param xContext    Application context
     * @param anId        A BranchName object id
     * @throws IcofException 
     */
    public BranchName(EdaContext xContext, int anId) 
    throws IcofException {
	dbLookupById(xContext, anId);
	setName(getDbObject().getName());

    }


    /**
     * Data Members
     */
    private String name;
    private BranchName_Db branchName;


    /**
     * Getters
     */
    public String getName() { return name; }
    public BranchName_Db getDbObject() { return branchName; }


    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    private void setDbObject(BranchName_Db aBranchName) { branchName = aBranchName; }


    /**
     * Lookup the BranchName object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A Component id
     * @throws IcofException 
     */
    public void dbLookupById(EdaContext xContext, int anId) throws IcofException { 
	if (getDbObject() == null) {
	    try {
		branchName = new BranchName_Db(anId);
		branchName.dbLookupById(xContext);
		name = branchName.getName();
	    }
	    catch(IcofException trap) {
		branchName = null;
		throw new IcofException(this.getClass().getName(), "dbLookupById()",
		                        IcofException.SEVERE,
		                        "Unable to find BranchName (" + anId + ") in the database.\n",
		                        trap.getMessage());
	    }
	}            
    }


    /**
     * Lookup the BranchName object from the name
     * 
     * @param xContext   Application context.
     * @throws IcofException 
     */
    public void dbLookupByName(EdaContext xContext) 
    throws IcofException {
	if (getDbObject() == null) {
	    try {
		branchName = new BranchName_Db(getName());
		branchName.dbLookupByName(xContext);
	    }
	    catch(IcofException trap) {
		throw new IcofException(this.getClass().getName(), "dbLookupByName()",
		                        IcofException.SEVERE,
		                        "Unable to find BranchName (" + getName() + ") in the database.\n",
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
     * @param newName     New Branch name
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
	buffer.append("BranchName object\n---------------\n");
	buffer.append("Name: " + getName() + "\n");

	return buffer.toString();

    }

}
