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
 * Component business object.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 04/14/2011 GFS  Initial coding. 
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.io.Serializable;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.Component_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class Component implements Serializable {

    private static final long serialVersionUID = -5305081707186556580L;


    /**
     * Constructor - takes a Component name
     * 
     * @param xContext Application context
     * @param aName A Component name (hdp, ess, vss ...)
     * @throws IcofException
     */
    public Component(EdaContext xContext, String aName) throws IcofException {

	setName(aName);
    }


    /**
     * Constructor - takes objects
     * 
     * @param xContext Application context
     * @param aComp A Component_Db object
     */
    public Component(EdaContext xContext, Component_Db aComp) {

	setComponent(aComp);
	setName(getComponent().getName());
    }


    /**
     * Constructor - takes IDs
     * 
     * @param xContext Application context
     * @param anId A Component object id
     * @throws IcofException
     */
    public Component(EdaContext xContext, short anId) throws IcofException {

	dbLookupById(xContext, anId);
	setName(getComponent().getName());

    }


    /**
     * Data Members
     * @formatter:off
     */
    private String name;
    private Component_Db component;
    private String stageName;

    
    /**
     * Getters
     */
    public String getName() { return name; }
    public Component_Db getComponent() { return component; }
    public String getStageName() { return stageName; }
    
    
    /**
     * Setters
     */
    private void setComponent(Component_Db aComp) { component = aComp; }
    private void setName(String aName) { name = aName; }
    // @formatter:on


    /**
     * Lookup the Component object from the database id
     * 
     * @param xContext Application context.
     * @param anId A Component id
     * @throws IcofException
     */
    public void dbLookupById(EdaContext xContext, short anId)
    throws IcofException {

	if (getComponent() == null) {
	    try {
		component = new Component_Db(anId);
		component.dbLookupById(xContext);
	    }
	    catch (IcofException trap) {
		component = null;
		throw new IcofException(this.getClass().getName(),
					"dbLookupById()", IcofException.SEVERE,
					"Unable to find Component (" + anId
					+ ") in the database.\n",
					trap.getMessage());
	    }
	}
    }


    /**
     * Lookup the Component object from the Component name
     * 
     * @param xContext Application context.
     * @throws IcofException
     */
    public void dbLookupByName(EdaContext xContext)
    throws IcofException {

	if (getComponent() == null) {
	    try {
		component = new Component_Db(getName());
		component.dbLookupByName(xContext);
	    }
	    catch (IcofException trap) {
		component = null;
		throw new IcofException(this.getClass().getName(),
					"dbLookupByName()",
					IcofException.SEVERE,
					"Unable to find Component ("
					+ getName() + ") in the database.\n",
					trap.getMessage());
	    }
	}
    }


    /**
     * Add this object to the database
     * 
     * @param xContext Application object
     * @param creator Person adding this object
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
	catch (IcofException trap) {
	    // Add the new object
	    getComponent().dbAddRow(xContext, creator);
	}
	return true;

    }


    /**
     * Update this object in the database
     * 
     * @param xContext Application object
     * @param newName New tool kit name
     * @param editor Person updating this object
     * @throws IcofException
     */
    public void dbUpdate(EdaContext xContext, String newName, User_Db editor)
    throws IcofException {

	try {
	    // Lookup the object in the database first.
	    dbLookupByName(xContext);
	}
	catch (IcofException trap) {
	    throw new IcofException(this.getClass().getName(), "dbUpdate()",
				    IcofException.SEVERE,
				    "Unable to find existing object ("
				    + getName() + ") in the database.\n",
				    trap.getMessage());
	}

	// Update the object
	getComponent().dbUpdateRow(xContext, newName, editor);
    }


    /**
     * Delete this object from the database
     * 
     * @param xContext Application object
     * @param editor Person adding this object
     * @throws IcofException
     */
    public void dbDelete(EdaContext xContext, User_Db editor)
    throws IcofException {

	getComponent().dbDeleteRow(xContext, editor);
    }


    /**
     * Create a key from the ID.
     * 
     * @param xContext Application context object.
     * @return A Statement object.
     */
    public String getIdKey(EdaContext xContext) {

	return String.valueOf(getComponent().getId());
    }


    /**
     * Display this object as a string
     * 
     * @param xContext Application context
     * @return This object as a string.
     */
    public String toString(EdaContext xContext) {

	// Get the class specific data
	StringBuffer buffer = new StringBuffer();
	buffer.append("Component object\n---------------\n");
	buffer.append("Name: " + getName() + "\n");

	return buffer.toString();

    }


    /**
     * Look up this Component's details for the ToolKit
     * 
     * @param xContext
     * @param toolKit
     * @throws IcofException
     */
    public void dbLookupTkDetails(EdaContext xContext, ToolKit toolKit)
    throws IcofException {

	Component_Version_Db cv = new Component_Version_Db(
							   xContext,
							   toolKit.getToolKit(),
							   getComponent());
	cv.dbLookupByCompRelVersion(xContext);
	cv.getStageName().dbLookupById(xContext);

	stageName = cv.getStageName().getName();

    }
    
    
    /**
     * Look up the ComponentTkVersion id for this Component and the 
     * specified Tool Kit
     *
     * @param xContext  Application context
     * @param aTk       Tool Kit object
     * @return  
     * @throws IcofException 
     */
    public Component_Version_Db getComponentTkVersionId(EdaContext xContext,
							ToolKit aTk)
    throws IcofException {

	Component_Version_Db compVer = new Component_Version_Db(xContext,
								aTk.getToolKit(),
								getComponent());
	compVer.dbLookupByAll(xContext);

	return compVer;

    }

    
}
