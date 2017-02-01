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
* FileContent business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 08/22/2013 GFS  Initial coding. 
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.sql.Timestamp;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.FileContent_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class FileContent  {

	/**
     * Constructor - takes IDs
     * 
     * @param xContext    Application context
     * @param anId        A object id
     * @throws IcofException 
     */
    public FileContent(EdaContext xContext, long anId) 
    throws IcofException {
        dbLookupById(xContext, anId);
        populate(xContext);
    }

    
	/**
     * Constructor - takes full path to file
     * 
     * @param xContext    Application context
     * @param aName       File name 
     * @throws IcofException 
     */
    public FileContent(EdaContext xContext, String Name) 
    throws IcofException {
    	
    	
    }

    
    /**
     * Constructor - takes objects
     * 
     * @param xContext  Application context
     * @param aFileName A FileName database object
     */
    public FileContent(EdaContext xContext, FileName aFileName,
                       int aChecksum, String aContents, 
                       Timestamp aCreateTms, Timestamp aModifyTms) {
        setFileName(aFileName);
        setChecksum(aChecksum);
        setContents(aContents);
        setCreateTimestamp(aCreateTms);
        setModifyTimestamp(aModifyTms);
        
    }

    
    /**
     * Constructor - take a database object
     * 
     * @param xContext  Application context
     * @param aDbObject A FileContent database object
     * @throws IcofException 
     */
    public FileContent(EdaContext xContext, FileContent_Db aDbObject)
    throws IcofException {
        setDbObject(aDbObject);
        populate(xContext);
    }
     
    
    /**
     * Data Members
     */
    private FileName fileName;
    private int checksum;
    private String contents;
    private Timestamp createTimestamp;
    private Timestamp modifyTimestamp;
    private FileContent_Db dbObject;

    
    /**
     * Getters
     */
    public FileName getFileName() { return fileName; }
    public int getChecksum() { return checksum; }
    public String getContents() { return contents; }
    public Timestamp getCreateTimestamp() { return createTimestamp; }
    public Timestamp getModifyTimestamp() { return modifyTimestamp; }
    public FileContent_Db getDbObject() { return dbObject; }

    
    /**
     * Setters
     */
    private void setFileName(FileName aName) { fileName = aName; }
    private void setChecksum(int aChecksum) { checksum = aChecksum; }
    private void setContents(String aContents) { contents = aContents; }
    private void setCreateTimestamp(Timestamp aTms) { createTimestamp = aTms; }
    private void setModifyTimestamp(Timestamp aTms) { modifyTimestamp = aTms; }
    private void setDbObject(FileContent_Db aDbObject) { dbObject = aDbObject; }

    
    /**
     * Lookup the object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A database id
     * @throws IcofException 
     */
    public void dbLookupById(EdaContext xContext, long anId) 
    throws IcofException { 
        if (getDbObject() == null) {
        	try {
        		dbObject = new FileContent_Db(anId);
        		dbObject.dbLookupById(xContext);
        		populate(xContext); 
        	}
        	catch(IcofException trap) {
        		dbObject = null;
        		throw new IcofException(this.getClass().getName(), 
        		                        "dbLookupById()",
        		                        IcofException.SEVERE,
        		                        "Unable to find FileContent (" + anId + 
        		                        ") in the database.\n",
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
//    public void dbLookupByName(EdaContext xContext) 
//    throws IcofException {
//    	if (getDbObject() == null) {
//        	try {
//        		dbObject = new FileName_Db(getName());
//        		dbObject.dbLookupByName(xContext);
//        	}
//        	catch(IcofException trap) {
//        		throw new IcofException(this.getClass().getName(), 
//        		                        "dbLookupByName()",
//        		                        IcofException.SEVERE,
//        		                        "Unable to find FileName (" + 
//        		                        getName() + ") in the database.\n",
//        		                        trap.getMessage());
//        	}
//        }
//    }
    
    
    /**
     * Add this object to the database
     * 
     * @param xContext  Application object
     * @throws IcofException 
     * @return True if object created false object existing object.
     */
    public void dbAdd(EdaContext xContext) throws IcofException {
   	
    	setDbObject(xContext);
    	getDbObject().dbAddRow(xContext);

    }


    /**
     * Populate the DB object from the members
     * @param xContext
     */
    private void setDbObject(EdaContext xContext) {

    	if (getDbObject() != null)
    		dbObject = null;
    	dbObject = new FileContent_Db(0, getFileName().getDbObject(), 
    	                              getChecksum(), getContents(),
    	                              getCreateTimestamp(), 
    	                              getModifyTimestamp());
    	
	}

    
//    /**
//     * Update this object in the database
//     * 
//     * @param xContext    Application object
//     * @param newName     New Branch name
//     * @throws IcofException 
//     */
//    public void dbUpdate(EdaContext xContext, String newName)
//    throws IcofException {
//    	
//    	try {
//    		// Lookup the object in the database first.
//    		dbLookupByName(xContext);
//    	}
//    	catch(IcofException trap) {
//    		throw new IcofException(this.getClass().getName(), "dbUpdate()",
//	                                IcofException.SEVERE,
//	                                "Unable to find existing object (" +
//	                                 getName() + ") in the database.\n",
//	                                 trap.getMessage());
//    	}
//
//    	// Update the object
//    	getDbObject().dbUpdateRow(xContext, newName);
//    	
//    }

    
    /**
     * Delete this object from the database
     * 
     * @param xContext    Application object
     * @throws IcofException 
     */
//    public void dbDelete(EdaContext xContext) throws IcofException {
//    	getDbObject().dbDeleteRow(xContext);
//    }

    
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
        buffer.append("FileContent object\n---------------\n");
        buffer.append("File Name : " + getFileName() + "\n");
        buffer.append("Checksum  : " + getChecksum() + "\n");
        buffer.append("Create Tms: " + getCreateTimestamp().toString() + "\n");
        buffer.append("Modify Tms: " + getModifyTimestamp().toString() + "\n");        
        return buffer.toString();

    }

    
    /**
     * Populate this object from the database object
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    private void populate(EdaContext xContext) throws IcofException {
    	
    	if (getFileName() != null)
    		fileName = null;
    	fileName = new FileName(xContext, getDbObject().getFileName().getId());
    	getFileName().dbLookupById(xContext, getDbObject().getFileName().getId());
    	
    	setChecksum(getDbObject().getChecksum());
    	setContents(getDbObject().getContents());
    	setCreateTimestamp(getDbObject().getCreateTimestamp());
    	setModifyTimestamp(getDbObject().getModifyTimestamp());
    	
    }
    
}
