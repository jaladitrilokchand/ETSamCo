/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2011 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*
*-PURPOSE---------------------------------------------------------------------
* Component Type business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 09/13/2011 GFS  Initial coding. 
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.ComponentType_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class ComponentType  {

    /**
     * Constructor - takes a name
     * 
     * @param xContext  Application context
     * @param aName     A name (deliverable, 32-bit, ...)
     * @throws IcofException 
     */
    public ComponentType(EdaContext xContext, String aName) 
    throws IcofException {
        setName(aName);
    }

    
    /**
     * Constructor - takes a name and description
     * 
     * @param xContext  Application context
     * @param aName     A name (deliverable, 32-bit, ...)
     * @param aDesc     Description of this type
     * @throws IcofException 
     */
    public ComponentType(EdaContext xContext, String aName, String aDesc) 
    throws IcofException {
        setName(aName);
        setDescription(aDesc);
    }

    
    /**
     * Constructor - takes objects
     * 
     * @param xContext  Application context
     * @param aType     Database object
     */
    public ComponentType(EdaContext xContext, ComponentType_Db aType) {
        setDbObject(aType);
        setName(getDbObject().getName());
        setDescription(getDbObject().getDescription());
    }

    
    /**
     * Constructor - takes IDs
     * 
     * @param xContext    Application context
     * @param anId        A ChangeRequestStatus object id
     * @throws IcofException 
     */
    public ComponentType(EdaContext xContext, long anId) 
    throws IcofException {
        dbLookupById(xContext, anId);
        setName(getDbObject().getName());
        setDescription(getDbObject().getDescription());
        
    }

    
    /**
     * Data Members
     */
    private String name;
    private String description;
    private Vector<ComponentType> componentTypes;
    private ComponentType_Db dbObject;

    
    /**
     * Getters
     */
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Vector<ComponentType> getComponentTypes() { return componentTypes; }
    public ComponentType_Db getDbObject() { return dbObject; }

    
    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    private void setDescription(String aDesc) { description = aDesc; }
    private void setDbObject(ComponentType_Db anObject) { dbObject = anObject; }
    

    
    /**
     * Lookup the object from the database id
     * 
     * @param xContext   Application context.
     * @param anId       A database id
     * @throws IcofException 
     */
    public void dbLookupById(EdaContext xContext, long anId) throws IcofException { 
        if (getDbObject() == null) {
        	try {
        		dbObject = new ComponentType_Db(anId);
        		dbObject.dbLookupById(xContext);
        		setName(dbObject.getName());
        	}
        	catch(IcofException trap) {
        		dbObject = null;
        		throw new IcofException(this.getClass().getName(), "dbLookupById()",
        				                IcofException.SEVERE,
        				                "Unable to find ComponentType (" +
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
        		dbObject = new ComponentType_Db(getName(), getDescription());
        		dbObject.dbLookupByName(xContext);
        		setDescription(dbObject.getDescription());
        	}
        	catch(IcofException trap) {
        		throw new IcofException(this.getClass().getName(), "dbLookupByName()",
        				                IcofException.SEVERE,
        				                "Unable to find ComponentType (" +
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
     * @param newName     New description
     * @throws IcofException 
     */
    public void dbUpdate(EdaContext xContext, String newName, 
                         String newDescription)
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
    	getDbObject().dbUpdateRow(xContext, newName, newDescription);
    	
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
        buffer.append("ComponentType\n---------------\n");
        buffer.append("Name: " + getName() + "\n");
        buffer.append("Description: " + getDescription() + "\n");
        
        return buffer.toString();

    }
    
    
    /**
     * Create a list of all ComponentType objects.
     * @param xContext  Application context
     * @throws IcofException 
     */
    public void setComponentTypes(EdaContext xContext) throws IcofException {
    	
    	if (dbObject == null)
    		dbObject = new ComponentType_Db("");
    	Vector<ComponentType_Db> dbObjects = getDbObject().dbLookupAll(xContext);
    	
    	// Convert the ComponentType_Db objects to ComponentType objects
    	componentTypes = new Vector<ComponentType>();
    	Iterator<ComponentType_Db> iter = dbObjects.iterator();
    	while (iter.hasNext()) {
    		ComponentType_Db compTypeDb =  iter.next();
    		ComponentType compType = new ComponentType(xContext, compTypeDb);
    		componentTypes.add(compType);
    		
    	}
    	
    }


    /**
     * Create a list of ComponentType objects and seed the list with the 
     * specified ComponentType
     * @param xContext  Application context
     * @param anObject  A Component Type object
     * @throws IcofException 
     */
	public void setComponentTypes(EdaContext xContext, ComponentType anObject) {

    	componentTypes = new Vector<ComponentType>();
    	componentTypes.add(anObject);
		
	}
    
}
