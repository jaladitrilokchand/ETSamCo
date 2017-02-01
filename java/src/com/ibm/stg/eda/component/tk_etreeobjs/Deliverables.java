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
 * Create a collection of Deliverables 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 11/20/2013 GFS  Initial coding.
 * 03/18/2014 GFS  Updated readDels to include all previous deliverables
 *=============================================================================
 * </pre>
 */
package com.ibm.stg.eda.component.tk_etreeobjs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.Deliverable_Db;
import com.ibm.stg.eda.component.tk_etreedb.FileName_Db;
import com.ibm.stg.eda.component.tk_etreedb.Platform_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.PkgDeliverable.Action;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;


public class Deliverables {
    
    /**
     * Constructor - for new empty collections
     *
     */
    public Deliverables(String sTopLevelDir) {

	setTopLevelDir(sTopLevelDir);
	deliverables = new HashMap<String, PkgDeliverable>();

    }
    
    
    /**
     * Constructor - converts files to PkgDeliverable objects
     *
     * @param sTopLevelDir  Name of Top level directory
     * @param contents      List of all possible deliverables
     */
    public Deliverables(String sTopLevelDir, HashSet<File> contents) {

	setTopLevelDir(sTopLevelDir);
	deliverables = new HashMap<String, PkgDeliverable>();
	
	for (File file : contents) {
	    PkgDeliverable xDel = new PkgDeliverable(getTopLevelDir(), file);
	    addDeliverable(xDel);
	}
	
    }


    /**
     * Read the prev.ship.list file into a collection of Deliverables
     * 
     * @throws IcofException
     * 
     */
    public String readShipList(IcofFile xPrevShipListFile) 
    throws IcofException {

	deliverables = new HashMap<String, PkgDeliverable>();
	StringBuffer log = new StringBuffer();
	
	// Read the file
	if (! xPrevShipListFile.exists()) {
	    log.append("WARNING: " + xPrevShipListFile.getAbsolutePath() + 
	               " does not exist\n");
	    return log.toString();
	}
	xPrevShipListFile.openRead();
	xPrevShipListFile.read();
	xPrevShipListFile.closeRead();

	// Convert the list into Deliverable objects
	for (Object entry : xPrevShipListFile.getContents()) {
	    String line = (String) entry;
	    log.append(" PSL line: " + line + "\n");
	    String[] tokens = line.split(" +");
	    String delSize = tokens[1].trim();
	    String delTime = tokens[3].trim();
	    String delName = tokens[8].trim();
	    log.append(" PSL tokens: size:" + delSize + " time:" + delTime + 
	               " name:" + delName + "\n");
	    String fullDelPath = getTopLevelDir() + File.separator + delName;
	    PkgDeliverable xDel = new PkgDeliverable(getTopLevelDir(), fullDelPath, 
	                                            false, 
	                                            (long)0, 
	                                            Long.parseLong(delTime), 
	                                            Long.parseLong(delSize),
	                                            Action.UNKNOWN);

	    addDeliverable(xDel);

	}
	
	return log.toString();

    }

    
    /**
     * Adds a new deliverable to the collection
     *
     * @param xDel  New deliverable object
     */
    public void addDeliverable(PkgDeliverable xDel) {
	
	if (! getDeliverables().keySet().contains(xDel.getPartialDelName())) {
	    deliverables.put(xDel.getPartialDelName(), xDel);
	}
	
    }
    
    
    /**
     * Removes a deliverable from the collection
     *
     * @param xDel  Existing deliverable object
     */
    public void removeDeliverable(PkgDeliverable xDel) {
	if (getDeliverables().keySet().contains(xDel.getPartialDelName())) {
	    deliverables.remove(xDel).getPartialDelName();
	}
    }
    
    
    /**
     * Dumps the collection contents into a string
     * 
     * @param bFullPath  If true display the full path name otherwise 
     *                   show the partial path name
     */
    public String toString(boolean bFullPath) {
	
	// Create the list of deliverables
	List<String> myList = new ArrayList<String>();
	for (PkgDeliverable del : getDeliverables().values()) {
	    String name;
	    if (bFullPath)
		name = del.getAbsolutePath();
	    else
		name = del.getPartialDelName();
	    myList.add(name);
	}
	
	// Sort the list
	Collections.sort(myList);
	
	// Convert it to a string
	StringBuffer log = new StringBuffer();
	for (String name : myList) {
	    log.append(name + "\n");
	}
	
	return log.toString();
	
    }
    
    
    /**
     * Members
     * @formatter:off
     */
    String topLevelDir;
    HashMap<String, PkgDeliverable> deliverables;
    
    
    /**
     * Getters
     */
    public HashMap<String, PkgDeliverable> getDeliverables() { return deliverables; }
    public String getTopLevelDir() { return topLevelDir; }
    
    
    /**
     * Setters
     */
    private void setTopLevelDir(String aName) { topLevelDir = aName; }


    /**
     * Read the deliverables from the database for the specified comp pkg
     *
     * @param xContext  Application context
     * @param compPkg   Package to get deliverables for
     * @throws IcofException 
     * @throws InterruptedException 
     */
    public void readDels(EdaContext xContext, ToolKit aTk, Component aComp, 
                         Platform_Db aPlat, ComponentPackage compPkg) 
    throws IcofException {

	// If no component package then return
	if (compPkg == null) {
	    System.out.println("Skipping previous deliverable query .. " +
	    		       "no previous comp pkg");
	    return;
	}
	
	// Create a collection of ALL packages created before the current pkg
	ComponentPackage aPkg = new ComponentPackage(xContext, (long)0);
	List<ComponentPackage> allPkgs = aPkg.getCompPackages(xContext, 
	                                                      aTk, aComp, 
	                                                      aPlat, true);
	
	// Create a collection of all deliverables for this component/tk 
	// such that it contains only the latest deliverable
	getDeliverables().clear();
	ArrayList<Long> fileIds = new ArrayList<Long>(); 
	for (ComponentPackage myPkg : allPkgs) {
	    
	    if ((myPkg.getMaintLevel() <= compPkg.getMaintLevel()) &&
	        (myPkg.getPatchLevel() <= compPkg.getPatchLevel())) {
		
		Deliverable_Db del = new Deliverable_Db(0);
		Vector<Deliverable_Db> dels;
		dels = del.dbLookupByCompPkgId(xContext, 
		                               myPkg.getDbObject().getId());
		
		// Convert the Deliverable_Db objects into PkgDeliverables
		for (Deliverable_Db dbDel : dels) {

		    // If the deliverable is not in the list then add it
		    Long myDelId = Long.valueOf(dbDel.getFileNameId());
		    if (! fileIds.contains(myDelId)) {

			FileName_Db file = new FileName_Db(dbDel.getFileNameId());
			file.dbLookupById(xContext);
			
			PkgDeliverable xDel;
			xDel = new PkgDeliverable(getTopLevelDir(), 
			                          file.getName(), 
			                          false, 
			                          dbDel.getChecksum(), 
			                          dbDel.getLastModified(),
			                          dbDel.getSize(),
			                          PkgDeliverable.getActionEnum(dbDel.getAction()));

			addDeliverable(xDel);
			fileIds.add(Long.valueOf(dbDel.getFileNameId()));
		    }

		}
		
	    }
	    
	}
	
    }
    
}
