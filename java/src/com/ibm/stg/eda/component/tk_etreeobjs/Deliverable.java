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
 * Deliverable business object.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 07/30/2013 GFS  Initial coding 
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreeobjs;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.Deliverable_Db;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;

public class Deliverable {

    /**
     * Constructor - takes an id
     * 
     * @param xContext Application context
     * @param anId Deliverable id
     * @throws IcofException
     */
    public Deliverable(EdaContext xContext, long anId) throws IcofException {

	setId(anId);
    }


    /**
     * Constructor - takes a db object
     * 
     * @param xContext Application context
     * @param aDbObject A database object
     * @throws IcofException
     */
    public Deliverable(EdaContext xContext, Deliverable_Db aDbObject)
    throws IcofException {

	setDbObject(aDbObject);
	populate(xContext);
    }


    /**
     * Constructor - takes a deliverable info
     * 
     * @param xContext Application context
     * @param aName A name
     * @throws IcofException
     */
    public Deliverable(EdaContext xContext, ComponentPackage aCompPkg,
		       FileName aFileName, long aChecksum, String aType,
		       String anAction, long aSize, long aTms) throws IcofException {

	setCompPackage(aCompPkg);
	setFileName(aFileName);
	setChecksum(aChecksum);
	setType(aType);
	setAction(anAction);
	setSize(aSize);
	setTimestamp(aTms);
    }


    /**
     * Constructor - takes a deliverable info
     * 
     * @param xContext Application context
     * @param aName A name
     * @throws IcofException
     */
    public Deliverable(PkgDeliverable xPkgDel) throws IcofException {

	setChecksum(xPkgDel.getCheckSum());
	setType(xPkgDel.getTypeName());
	setTimestamp(xPkgDel.getTimestamp());
	setSize(xPkgDel.getSize());
	setAction(xPkgDel.getActionName());
	setRealBaseDir(xPkgDel.getTopLevelDirName());
	setRealDelName(xPkgDel.getPartialDelName());
	fileName = new FileName(null, getRealDelName());
	
    }


    /**
     * Data Members
     * 
     * @formatter:off
     */
    private long id;
    private ComponentPackage compPackage;
    private FileName fileName;
    private long checksum;
    private String type;
    private String action;
    private long size;    
    private Deliverable_Db dbObject;
    private String realBaseDir;
    private String realDelName;
    private IcofFile realDelFile;
    private long timestamp;

    
    /**
     * Getters
     */
    public long getId() { return id; }
    public ComponentPackage getCompPackage() { return compPackage; }
    public FileName getFileName() { return fileName; }
    public long getChecksum() { return checksum; }
    public String getType() { return type; }
    public String getAction() { return action; }
    public long getTimestamp() { return timestamp; }
    public long getSize() { return size; }
    public Deliverable_Db getDbObject() { return dbObject; }
    public String getRealBaseDir() { return realBaseDir; }
    public String getRealDelName() { return realDelName; }
    public IcofFile getRealDelFile() { return realDelFile; }

    
    /**
     * Setters
     */
    private void setId(long anId) { id = anId; }
    private void setCompPackage(ComponentPackage aCompPkg) { compPackage = aCompPkg; }
    private void setFileName(FileName aName) { fileName = aName; }
    private void setChecksum(long aChecksum) { checksum = aChecksum; }
    private void setType(String aType) { type = aType; }
    private void setAction(String anAction) { action = anAction; }
    private void setTimestamp(long aTms) { timestamp = aTms; }
    private void setSize(long aSize) { size = aSize; }
    private void setRealBaseDir(String aDir) { realBaseDir = aDir; }
    private void setRealDelName(String aName) { realDelName = aName; }
    private void setDbObject(Deliverable_Db aDbObject) { dbObject = aDbObject; }
    // @formatter:on


    /**
     * Add this object to the database
     * 
     * @param xContext Application object
     * @param eventsId Events id
     * @param user Person adding this row
     * @throws IcofException
     * @return True if object created false object existing object.
     */
    public boolean dbAdd(EdaContext xContext)
    throws IcofException {

	setDbObject(xContext);
	getDbObject().dbAddRow(xContext);

	return true;

    }


    /**
     * Loads the DB object
     * 
     * @param xContext Application context
     * @param eventsId Events id
     */
    private void setDbObject(EdaContext xContext) {

	dbObject = new Deliverable_Db(getCompPackage().getDbObject().getId(),
				      getFileName().getDbObject().getId(),
				      getChecksum(), 
				      getType(), 
				      getAction(),
				      getSize(),
				      getTimestamp());
	
    }


    /**
     * Update this object in the database
     * 
     * @param xContext Application object
     * @param id Event id to update
     * @param newEvent New Event object (contains new data)
     * @throws IcofException
     */
    public void dbUpdate(EdaContext xContext, long id, Deliverable newDel)
    throws IcofException {

	try {
	    // Lookup the object in the database first.
	    dbLookupById(xContext, id);
	}
	catch (IcofException trap) {
	    throw new IcofException(this.getClass().getName(), "dbUpdate()",
				    IcofException.SEVERE,
				    "Unable to find existing object (" + id
				    + ") in the database.\n", trap.getMessage());
	}

	// Update the object
	getDbObject().dbUpdateRow(xContext,
				  newDel.getFileName().getDbObject().getId(),
				  newDel.getChecksum(), newDel.getType(),
				  newDel.getAction(), newDel.getSize(), 
				  newDel.getTimestamp());

    }


    /**
     * Delete this object from the database
     * 
     * @param xContext Application object
     * @param id Event id to delete
     * @throws IcofException
     */
    public void dbDelete(EdaContext xContext, long id)
    throws IcofException {

	try {
	    // Lookup the object in the database first.
	    dbLookupById(xContext, id);
	}
	catch (IcofException trap) {
	    throw new IcofException(this.getClass().getName(), "dbDelete()",
				    IcofException.SEVERE,
				    "Unable to find existing object (" + id
				    + ") in the database.\n", trap.getMessage());
	}

	getDbObject().dbDeleteRow(xContext);
    }


    /**
     * Lookup the object from the id
     * 
     * @param xContext Application context.
     * @throws IcofException
     */
    public void dbLookupById(EdaContext xContext, long id)
    throws IcofException {

	if (getDbObject() == null) {
	    try {
		dbObject = new Deliverable_Db(id);
		dbObject.dbLookupById(xContext);
		populate(xContext);
	    }
	    catch (IcofException trap) {
		throw new IcofException(this.getClass().getName(),
					"dbLookupById()", IcofException.SEVERE,
					"Unable to find Event (" + id
					+ ") in the database.\n",
					trap.getMessage());
	    }
	}
    }


    /**
     * Create a key from the ID.
     * 
     * @param xContext Application context object.
     * @return A Statement object.
     */
    public String getIdKey(EdaContext xContext) {

	return String.valueOf(getDbObject().getId());
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
	buffer.append("Comp Pkg : " + getCompPackage().getName() + "\n");
	buffer.append("File Name: " + getFileName().getName() + "\n");
	buffer.append("Checksum : " + getChecksum() + "\n");
	buffer.append("Type     : " + getType() + "\n");
	buffer.append("Action   : " + getAction() + "\n");
	buffer.append("Size     : " + getSize() + "\n");
	buffer.append("Last Mod : " + getTimestamp() + "\n");

	return buffer.toString();

    }


    /**
     * Populate this object from the database object
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void populate(EdaContext xContext)
    throws IcofException {

	setChecksum(getDbObject().getChecksum());
	setType(getDbObject().getType());
	setAction(getDbObject().getAction());
	fileName = new FileName(xContext, getDbObject().getFileNameId());
	getFileName().dbLookupById(xContext, getDbObject().getFileNameId());
	compPackage = new ComponentPackage(xContext,
					   getDbObject().getCompPackageId());
	getCompPackage().dbLookupById(xContext,
				      getDbObject().getCompPackageId());
	setSize(getDbObject().getSize());
	setTimestamp(getDbObject().getLastModified());

    }


    /**
     * Remove all deliverables associate with the component package
     *
     * @param xContext  Application context
     * @throws IcofException 
     */
    public void dbDeleteAllForCompPkg(EdaContext xContext)
    throws IcofException {

	if (getDbObject() == null) 
	    dbObject = new Deliverable_Db(0);
	
	dbObject.dbDeleteAllCompPkg(xContext, 
	                            getCompPackage().getDbObject().getId());
	
    }
 
}
