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
* Event business object.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/03/2011 GFS  Initial coding - just stubbed out the members. 
* 10/13/2011 GFS  Added getEvents(), setEventsAll() and getFormattedEvents().
* 07/30/2013 GFS  Renamed to LocationEvent
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.LocationEventName_Db;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class LocationEvent  {

	/**
	 *  Constants
	 */
	public static final int GAP = 25;
	
	
    /**
     * Constructor - takes a name
     * 
     * @param xContext  Application context
     * @param aName     A name
     * @throws IcofException 
     */
    public LocationEvent(EdaContext xContext, String aName) 
    throws IcofException {
        setName(aName);
    }

    
    /**
     * Constructor
     * 
     * @param xContext  Application context
     * @param aName       An event name
     * @param aCreatedBy  The Created By intranet id
     * @param aCreatedOn  The Created On timestamp
     * @throws IcofException 
     */
    public LocationEvent(EdaContext xContext, String aName, String aCreatedBy, 
                         Timestamp aCreatedOn) 
    throws IcofException {
        setName(aName);
        setCreatedBy(aCreatedBy);
        setCreatedOn(aCreatedOn);
    }
    
    
    /**
     * Data Members
     */
    private String name;
    private String createdBy;
    private Timestamp createdOn;
    private Vector<LocationEventName_Db> events;
    private LocationEventName_Db dbObject;

    
    /**
     * Getters
     */
    public String getName() { return name; }
    public String getCreatedBy() { return createdBy; }
    public Timestamp getCreatedOn() { return createdOn; }
    public Vector<LocationEventName_Db> getEvents() { return events; }
    public LocationEventName_Db getDbObject() { return dbObject; }

    
    /**
     * Setters
     */
    private void setName(String aName) { name = aName; }
    private void setCreatedBy(String aName) { createdBy = aName; }
    private void setCreatedOn(Timestamp aTms) { createdOn = aTms; }   

    
    /**
     * Display this object as a string
     * @param xContext  Application context
     * @return    This object as a string.
     */
    public String toString(EdaContext xContext) {

        // Get the class specific data
        StringBuffer buffer = new StringBuffer();
        buffer.append("Event: " + getName() + "\n");
        buffer.append("Created by: " + getCreatedBy() + "\n");
        buffer.append("Created on: " + getCreatedOn() + "\n");
        
        return buffer.toString();

    }

    
    /**
     * Lookup all event names.
     * 
     * @param xContext  Application context
     * @throws IcofException
     */
    public void setEventsAll(EdaContext xContext) throws IcofException {
    	
    	if (getDbObject() == null)
    		dbObject = new LocationEventName_Db("");
    	events = getDbObject().dbLookupAll(xContext);
    	
    }

    /**
     * Return a user friendly version of the Events
     * @param xContext  Application context
     * @return  Returns a string of human readable events
     */
    public String getFormattedEvents(EdaContext xContext) {
    	
    	StringBuffer list = new StringBuffer();
    	
    	if (getEvents().size() > 0) {
    		list.append(IcofStringUtil.leftJustify("Event name", " ", GAP) +
    		            IcofStringUtil.center("Platform Required", " ", GAP) +
    		            "\n");
    		list.append(IcofStringUtil.leftJustify("-----------", " ", GAP) +
    		            IcofStringUtil.center("-----------------", " ", GAP) + 
    		            "\n");
    	
    		Iterator<LocationEventName_Db> iter = getEvents().iterator();
    		while (iter.hasNext()) {
    			LocationEventName_Db event = iter.next();
    			String reqString = "no";
    			if (event.getRequiresPlat() == true) 
    				reqString = "yes";
    			list.append(IcofStringUtil.leftJustify(event.getName(), " ", GAP) +
    			            IcofStringUtil.center(reqString, " ", GAP) + 
                           	"\n");
    		}
    	}

    	return list.toString();

    }
    
}
