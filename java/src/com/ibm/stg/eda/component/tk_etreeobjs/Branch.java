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
 * Branch business object.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 04/28/2011 GFS  Initial coding. 
 * 07/13/2011 GFS  Added db methods.
 * 11/28/2011 GFS  Added dbLookupByCompVersion() method.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.Branch_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.RelVersion_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class Branch  {

    /**
     * Constructor - takes a BranchName
     * 
     * @param xContext     Application context
     * @param aBranchName  A BranchName object
     * @throws IcofException 
     */
    public Branch(EdaContext xContext, BranchName aBranchName) 
    throws IcofException {
	setBranchName(aBranchName);
    }


    /**
     * Constructor - takes a Branch name and Component_Version
     * 
     * @param xContext     Application context
     * @param aBranchName  A BranchName object
     * @param aCompVer     A Component_Version_Db object
     * @throws IcofException 
     */
    public Branch(EdaContext xContext, BranchName aBranchName,
                  Component_Version_Db aCompVer) 
                  throws IcofException {
	setBranchName(aBranchName);
	setCompTks(aCompVer);
    }


    /**
     * Data Members
     */
    private BranchName branchName;
    private Branch_Db branch;
    private Vector<Component_Version_Db> compVers;


    /**
     * Getters
     */
    public BranchName getBranchName() { return branchName; }
    public Branch_Db getDbObject() { return branch; }
    public Vector<Component_Version_Db> getCompTks() { return compVers; }


    /**
     * Setters
     */
    private void setBranchName(BranchName aBranchName) { branchName = aBranchName; }
    private void setDbObject(Branch_Db aBranch) { branch = aBranch; }
    private void setCompTks(Component_Version_Db aCompVer) { 
	if (getCompTks() == null)
	    compVers = new Vector<Component_Version_Db>();
	compVers.add(aCompVer);
    }


    /**
8     * Lookup the object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A database id
     * @throws IcofException 
     */
    public void dbLookupById(EdaContext xContext, short anId) 
    throws IcofException { 
	if (getDbObject() == null) {
	    try {
		branch = new Branch_Db(anId);
		branch.dbLookupById(xContext);
	    }
	    catch(IcofException trap) {
		branch = null;
		throw new IcofException(this.getClass().getName(), "dbLookupById()",
		                        IcofException.SEVERE,
		                        "Unable to find Branch (" +
		                        anId + ") in the database.\n",
		                        trap.getMessage());
	    }
	}            
    }


    /**
     * Lookup the object from the name/CompVersion
     * 
     * @param xContext   Application context.
     * @throws IcofException 
     */
    public void dbLookupByNameCompVersion(EdaContext xContext) 
    throws IcofException {

	if (getDbObject() == null) {
	    try {
		branch = new Branch_Db(getBranchName().getDbObject(), 
		                       getCompTks().firstElement());
		branch.dbLookupByNameCompVer(xContext);
	    }
	    catch(IcofException trap) {
		throw new IcofException(this.getClass().getName(), "dbLookupByNameComVersion()",
		                        IcofException.SEVERE,
		                        "Unable to find BranchName (" +
		                        getBranchName().getName() + ") for CompVersion in database.\n",
		                        trap.getMessage());
	    }
	}

    }


    /**
     * Lookup the object from the name/CompVersion
     * 
     * @param xContext  Application context.
     * @throws IcofException 
     */
    public void dbLookupByCompVersion(EdaContext xContext) 
    throws IcofException {

	if (getDbObject() == null) {
	    try {
		branch = new Branch_Db(null, getCompTks().firstElement());
		Vector<Branch_Db> branches = branch.dbLookupByCompVersion(xContext);

		if (branches.size() == 1) {
		    branch = (Branch_Db) branches.firstElement();
		    branch.dbLookupById(xContext);
		    
		}
		else {
		    throw new IcofException(this.getClass().getName(), "dbLookupByComVersion()",
		                            IcofException.SEVERE,
		                            "Found multiple Branches for this " +
		                            "Component/ToolKit in the database.\n",
		    "");
		}

	    }
	    catch(IcofException trap) {
		throw new IcofException(this.getClass().getName(), "dbLookupByComVersion()",
		                        IcofException.SEVERE,
		                        "Unable to find BranchName (" +
		                        getBranchName().getName() + ") for CompVersion in database.\n",
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
	    dbLookupByNameCompVersion(xContext);
	    return false;
	}
	catch(IcofException trap) {

	    // Verify this Component_Version doesn't have a branch
	    Vector<Branch_Db> branches = getDbObject().dbLookupByCompVersion(xContext);

	    if (branches.size() < 1) {
		// Add the new object
		getDbObject().dbAddRow(xContext);
	    }
	    else {
		return false;
	    }
	}

	return true;

    }


    /**
     * Update this object in the database
     * 
     * @param xContext    Application object
     * @param newName     New BranchName object
     * @throws IcofException 
     */
    public void dbUpdate(EdaContext xContext, BranchName newName)
    throws IcofException {

	try {
	    // Lookup the object in the database first.
	    dbLookupByNameCompVersion(xContext);
	}
	catch(IcofException trap) {
	    throw new IcofException(this.getClass().getName(), "dbUpdate()",
	                            IcofException.SEVERE,
	                            "Unable to find existing object (" +
	                            getBranchName().getName() + ") in the database.\n",
	                            trap.getMessage());
	}

	// Update the object
	getDbObject().dbUpdateRow(xContext, newName.getDbObject());

    }


    /**
     * Delete this object from the database
     * 
     * @param xContext    Application object
     * @param editor      Person adding this object
     * @throws IcofException 
     */
    public void dbDelete(EdaContext xContext) throws IcofException {

	try {
	    // Lookup the object in the database first.
	    dbLookupByNameCompVersion(xContext);
	}
	catch(IcofException trap) {
	    throw new IcofException(this.getClass().getName(), "dbDelete()",
	                            IcofException.SEVERE,
	                            "Unable to find existing object (" +
	                            getBranchName().getName() + ") in the database.\n",
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
	buffer.append("BranchName ID: " + getBranchName().getDbObject().getId() + "\n");
	for (Component_Version_Db cv : getCompTks())
	    buffer.append("Comp_Verion ID: " + cv.getId() + "\n");
	
	return buffer.toString();

    }


    /**
     * Determines the RelVersion for this Branch name and Component.
     * 
     * @param xContext  Application context
     * @return          True if branch is a prod branch, false if dev branch.
     * @throws IcofException  Trouble querying database.
     */
    public Vector<ToolKit> findToolKits(EdaContext xContext, Component component)
    throws IcofException {

	// Get the Comp_RelVersions for this Component/Branch.
	Vector<Component_Version_Db> compVers = setCompVersions(xContext, component);

	// Return the ToolKit
	Vector<ToolKit> tks = new Vector<ToolKit>();
	for (Component_Version_Db compVer : compVers) {
	    compVer.dbLookupById(xContext);
	    RelVersion_Db relVer = new RelVersion_Db(compVer.getVersion().getId());
	    relVer.dbLookupById(xContext);
	    ToolKit tk = new ToolKit(xContext, relVer);
	    tk.dbLookupById(xContext, relVer.getId());
	    tks.add(tk);
	}

	return tks;

    }


    /**
     * Determines the Component_TkVersion for this Branch name and Component.
     * 
     * @param xContext  Application context
     * @return          ComponentVersion
     * @throws IcofException  Trouble querying database.
     */
    public Vector<Component_Version_Db> setCompVersions(EdaContext xContext, 
                                                        Component component)
                                                        throws IcofException {

	// Get the Comp_RelVersions for this Component.
	Component_Version_Db compVer = new Component_Version_Db((long) 0);
	compVers = compVer.dbLookupCompVerionss(xContext, component.getComponent());

	// Look up the Branch name/Component/Comp_Tks to determine which is the 
	// valid TK for this Branch/Component.
	Vector<Component_Version_Db> matchingTks = new Vector<Component_Version_Db>();
	for (Component_Version_Db myCompVer : compVers) {
	    Branch myBranch = new Branch(xContext, getBranchName(), myCompVer);
	    try {
		myBranch.setDbObject(null);
		myBranch.dbLookupByNameCompVersion(xContext);
		matchingTks.add(myCompVer);
	    }
	    catch(IcofException ignore) {}

	}

	return matchingTks;

    }

}
