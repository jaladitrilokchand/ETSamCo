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
 * ToolKit business object.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 10/25/2010 GFS  Initial coding.
 * 04/14/2011 GFS  Renamed to ToolKit. Added addComponent, deleteComponent and
 *                 db* methods.
 * 09/08/2011 GFS  Added dbCopy() method.
 * 11/17/2011 GFS  Added dbLookupToolKit().
 * 12/20/2011 GFS  Added new variant of dbLookupToolKitByStage(Component).
 * 10/23/2012 GFS  Updated dbLookupToolKitByStage() to work for AGTS.
 * 10/30/2012 GFS  Updated to support xtinct tool kits.
 * 11/06/2012 GFS  Added support for new cqReleaseName, description and parent 
 *                 id database columns.
 * 11/27/2012 GFS  Updated to look up the TK by its ASIC, Server or CQ names.
 * 03/13/2013 GFS  Enhanced dbLookupByName() method to support transition period
 *                 between EOL and xtinct TK creation.
 * 03/20/2013 GFS  Added more info to toString() method.
 * 06/20/2013 GFS  Updated to lookup release in dbLookupById().
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreeobjs;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.Component_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Release_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.Platform_Db;
import com.ibm.stg.eda.component.tk_etreedb.RelVersion_Db;
import com.ibm.stg.eda.component.tk_etreedb.Release_Platform_Db;
import com.ibm.stg.eda.component.tk_etreedb.StageName_Db;
import com.ibm.stg.eda.component.tk_etreedb.User_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class ToolKit implements Serializable {

    /**
			 * 
			 */
    private static final long serialVersionUID = 2828470238673666647L;


    /**
     * Constructor - takes a ToolKit name (like 14.1.0) or a CQ TK name (like
     * 18.1.3 (shipb))
     * 
     * @param xContext Application context
     * @param aTkName A ToolKit name
     * @throws IcofException
     */
    public ToolKit(EdaContext xContext, String aTkName, boolean bIsCqName)
    throws IcofException {

	if (bIsCqName) {
	    setCqName(aTkName);
	}
	else {
	    setName(aTkName);
	}

    }


    /**
     * Constructor - takes a ToolKit name (14.1.0)
     * 
     * @param xContext Application context
     * @param aTkName A ToolKit name like 14.1.0, 14.1.1 ...
     * @throws IcofException
     */
    public ToolKit(EdaContext xContext, String aTkName) throws IcofException {

	setName(aTkName);
    }


    /**
     * Constructor - takes a ToolKit name (14.1.0) and stage name (NEW)
     * 
     * @param xContext Application context
     * @param aTkName A ToolKit name like 14.1.0, 14.1.1 ...
     * @param aStageName A Stage name like NEW, READY, DEVELOPMENT, ...
     * @throws IcofException
     */
    public ToolKit(EdaContext xContext, String aTkName, String aStageName)
    throws IcofException {

	setName(aTkName);
	setStage(xContext, aStageName);
    }


    /**
     * Constructor - takes all members (for adding new ToolKit)
     * 
     * @param xContext Application context
     * @param aTkName A ToolKit name like 14.1.0, 14.1.1 ...
     * @param aStageName A Stage name like NEW, READY, DEVELOPMENT, ...
     * @param aCqName ClearQuest release name like 14.1.0 (dev) ..
     * @param aDesc TK version description
     * @param aParentId This TK's parent
     * @throws IcofException
     */
    public ToolKit(EdaContext xContext, String aTkName, String aStageName,
		   String aCqName, String aDesc, short aParentId)
    throws IcofException {

	setName(aTkName);
	setStage(xContext, aStageName);
	setCqName(aCqName);
	setDescription(aDesc);
	setParentId(aParentId);
    }


    /**
     * Constructor - takes objects
     * 
     * @param xContext Application context
     * @param aVersion A TkRelease object
     */
    public ToolKit(EdaContext xContext, RelVersion_Db aVersion) {

	setToolKit(aVersion);
	setName(getToolKit().getDisplayName());
	setStage(xContext, getToolKit().getStageName());
    }


    /**
     * Constructor - takes IDs
     * 
     * @param xContext Application context
     * @param anId A TkRelease object id
     * @throws IcofException
     */
    public ToolKit(EdaContext xContext, short anId) throws IcofException {

	dbLookupById(xContext, anId);
	setName(getToolKit().getDisplayName());

    }


    /**
     * Data Members
     * @formatter:off
     */
    private String name;
    private StageName stageName;
    private String cqName;
    private String description;
    private short parentId;
    private RelVersion_Db toolKit;
    private Vector<Component_Db> components;
    private Hashtable<String,Platform_Db> platforms;


    /**
     * Getters
     */
    public String getName() { return name; }
    public String getCqName() { return cqName; }
    public StageName getStageName() { return stageName; }
    public String getDescription()  { return description; }
    public short getParentId()  { return parentId; }
    public Vector<Component_Db> getComponents() { return components; }
    public RelVersion_Db getToolKit() { return toolKit; }
    public Hashtable<String,Platform_Db> getPlatforms() { return platforms; }
    public String getPackagingName() throws IcofException { 
	return getToolKit().getPackagingName();
    }
    
    public String getReleaseName(){return getToolKit().getRelease().getName();}
    

    /**
     * Setters
     */
    private void setToolKit(RelVersion_Db aVersion) { toolKit = aVersion; }
    private void setName(String aName) { name = aName; }
    private void setCqName(String aName) { cqName = aName; }
    private void setDescription(String aDesc)  { description = aDesc; }
    private void setParentId(short anId)  { parentId = anId; }
    // @formatter:on

    

    /**
     * Set the StageName object.
     * 
     * @param xContext Application context
     * @param aName The stage name.
     * @throws IcofException
     */
    private void setStage(EdaContext xContext, String aName)
    throws IcofException {

	stageName = new StageName(xContext, aName);
	stageName.dbLookupByName(xContext);
    }


    /**
     * Set the StageName object
     * 
     * @param xContext Application context
     * @param aStage A StageName_Db object
     */
    private void setStage(EdaContext xContext, StageName_Db aStage) {

	stageName = new StageName(xContext, aStage);
    }


    /**
     * Lookup the ToolKit object from the database id
     * 
     * @param xContext Application context.
     * @param anId A RelVersion id
     * @throws IcofException
     */
    public void dbLookupById(EdaContext xContext, short anId)
    throws IcofException {

	if (getToolKit() != null)
	    toolKit = null;

	try {
	    toolKit = new RelVersion_Db(anId);
	    toolKit.dbLookupById(xContext);
	    toolKit.getRelease().dbLookupById(xContext);
	    toolKit.getStageName().dbLookupById(xContext);
	    populate(xContext);
	}
	catch (IcofException trap) {
	    toolKit = null;
	    throw new IcofException(this.getClass().getName(),
	                            "setToolKit()", IcofException.SEVERE,
	                            "Unable to find ToolKit (" + anId
	                            + ") in the database.\n",
	                            trap.getMessage());
	}
	
    }
    


    /**
     * Lookup the ToolKit object from the ASIC, Server or CQ ToolKit name
     * 
     * @param xContext Application context.
     * @throws IcofException
     */
    public void dbLookupByName(EdaContext xContext)
    throws IcofException {

	try {
	    // Query for a TK using the ASIC (14.1) or Server (18.1) name
	    toolKit = new RelVersion_Db(xContext, getName());
	    toolKit.dbLookupByName(xContext);
	}
	catch (IcofException trap) {

	    try {
		// Query for a TK using the CQ Release Name
		toolKit = null;
		toolKit = new RelVersion_Db(xContext, null, null, getName(),
					    "", (short) 0);
		toolKit.dbLookupByCqName(xContext);
	    }
	    catch (IcofException trap2) {

		throw new IcofException(this.getClass().getName(),
					"dbLookByName()", IcofException.SEVERE,
					"Unable to find ToolKit (" + name
					+ ") in the database.(3)\n",
					trap2.getMessage());
	    }
	}

	populate(xContext);

    }


    /**
     * Lookup the ToolKit object from the ClearQuest TK name
     * 
     * @param xContext Application context.
     * @throws IcofException
     */
    public void dbLookupByCqName(EdaContext xContext)
    throws IcofException {

	try {
	    toolKit = new RelVersion_Db(xContext, null, null, getCqName(), "",
					(short) 0);
	    toolKit.dbLookupByCqName(xContext);
	    populate(xContext);
	}
	catch (IcofException trap) {
	    throw new IcofException(this.getClass().getName(), "setToolKit()",
				    IcofException.SEVERE,
				    "Unable to find ToolKit (" + getCqName()
				    + ") in the database.\n", trap.getMessage());
	}
    }


    /**
     * Get the components for this ToolKit.
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    public void setComponents(EdaContext xContext) throws IcofException {

	validate(xContext);
	Component_Version_Db compVer = new Component_Version_Db(getToolKit());
	components = compVer.dbLookupAllComponents(xContext);
	
    }

    
    /**
     * Get the platforms for this ToolKit.
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    public void setPlatforms(EdaContext xContext)
    throws IcofException {

	validate(xContext);
	Release_Platform_Db relPlat = new Release_Platform_Db(
							      getToolKit().getRelease(),
							      null);
	platforms = relPlat.dbLookupPlatforms(xContext);
    }


    /**
     * Verifies the ToolKit is set.
     * 
     * @param xContext Application context
     * @throws IcofException
     */
    private void validate(EdaContext xContext)
    throws IcofException {

	if ((getToolKit() == null) && (getToolKit().getId() > 0)) {
	    throw new IcofException(
				    this.getClass().getName(),
				    "validate()",
				    IcofException.SEVERE,
				    "The ToolKit is not valid for this operation.\n",
				    "");
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
	    toolKit = null;
	    toolKit = new RelVersion_Db(xContext, getName(),
					getStageName().getStage(), getCqName(),
					getDescription(), getParentId());
	    getToolKit().dbAddRow(xContext, creator);

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
    public void dbUpdate(EdaContext xContext, String newName,
			 String newCqRelName, String newDescription,
			 User_Db editor)
    throws IcofException {

	try {
	    // Lookup the object in the database first.
	    dbLookupByName(xContext);
	    populate(xContext);
	}
	catch (IcofException trap) {
	    throw new IcofException(this.getClass().getName(), "dbUpdate()",
				    IcofException.SEVERE,
				    "Unable to find existing object ("
				    + getName() + ") in the database.\n",
				    trap.getMessage());
	}

	// Determine what's new
	if (newName == null)
	    newName = getToolKit().getName();
	if (newCqRelName == null)
	    newCqRelName = getCqName();
	if (newDescription == null)
	    newDescription = getDescription();

	// Update the object
	getToolKit().dbUpdate(xContext, newName, newCqRelName, newDescription,
			      editor);

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

	getToolKit().dbDeleteRow(xContext, editor);
    }


    /**
     * Add a component to this ToolKit
     * 
     * @param xContext Application context
     * @param compName Component name to be added
     * @param creator Person making the update
     * @throws IcofException
     */
    public void addComponent(EdaContext xContext, String compName,
			     User_Db creator)
    throws IcofException {

	// Add the component if needed
	Component newComp = new Component(xContext, compName);
	try {
	    newComp.dbLookupByName(xContext);
	}
	catch (IcofException trap) {
	    newComp.dbAdd(xContext, creator);
	}

	addComponent(xContext, newComp, creator);

    }


    /**
     * Add a component to this ToolKit
     * 
     * @param xContext Application context
     * @param newComp Component to be added
     * @param creator Person making the update
     * @throws IcofException
     */
    public void addComponent(EdaContext xContext, Component newComp,
			     User_Db creator)
    throws IcofException {

	// Add component to Tool Kit if not already there
	Component_Version_Db compVer = new Component_Version_Db(
								xContext,
								getToolKit(),
								newComp.getComponent());
	try {
	    compVer.dbLookupByAll(xContext);
	}
	catch (IcofException trap) {
	    compVer.dbAddRow(xContext, creator);
	}

	// Add component to Release if not already there
	Component_Release_Db compRel = new Component_Release_Db(
								getToolKit().getRelease(),
								newComp.getComponent());
	try {
	    compRel.dbLookupByRelComp(xContext);
	}
	catch (IcofException trap) {
	    compRel.dbAddRow(xContext, creator);
	}


    }


    /**
     * Remove a component from this ToolKit
     * 
     * @param xContext Application context
     * @param compName Component name to be removed
     * @param editor Person making the update
     * @throws IcofException
     */
    public void removeComponent(EdaContext xContext, String compName,
				User_Db editor)
    throws IcofException {

	// Look up the component name
	Component comp = new Component(xContext, compName);
	try {
	    comp.dbLookupByName(xContext);
	}
	catch (IcofException trap) {
	    throw new IcofException(
				    this.getClass().getName(),
				    "removeComponent()",
				    IcofException.SEVERE,
				    "This Component was not found in the database.\n",
				    compName);
	}

	// Remove Component from ToolKit and Release
	removeComponent(xContext, comp, editor);

    }


    /**
     * Remove component from this ToolKit
     * 
     * @param xContext Application context
     * @param comp Component to be removed
     * @param editor Person making the update
     * @throws IcofException
     */
    public void removeComponent(EdaContext xContext, Component comp,
				User_Db editor)
    throws IcofException {

	// Remove component from Tool Kit
	Component_Version_Db compVer = new Component_Version_Db(
								xContext,
								getToolKit(),
								comp.getComponent());
	try {
	    compVer.dbLookupByAll(xContext);
	}
	catch (IcofException trap) {
	    throw new IcofException(this.getClass().getName(),
				    "removeComponent()", IcofException.SEVERE,
				    "This Component is not associated with the given ToolKit in "
				    + "the database.\n",
				    "Component: " + comp.getName() + "\n"
				    + "ToolKit  : "
				    + getToolKit().getDisplayName() + "\n");
	}
	compVer.dbDeleteRow(xContext, editor);


	// Remove component from Release if not associated with any other TKs
	// for this release.
	Component_Release_Db compRel = new Component_Release_Db(
								getToolKit().getRelease(),
								comp.getComponent());
	try {
	    compRel.dbLookupByRelComp(xContext);
	}
	catch (IcofException trap) {
	    throw new IcofException(this.getClass().getName(),
				    "removeComponent()", IcofException.SEVERE,
				    "This Component is not associated with the Release in "
				    + "the database.\n",
				    "Component: " + comp.getName() + "\n"
				    + "Release  : "
				    + getToolKit().getRelease().getName()
				    + "\n");
	}
	compRel.dbDeleteRow(xContext, editor);

    }


    /**
     * Create a key from the ID.
     * 
     * @param xContext Application context object.
     * @return A Statement object.
     */
    public String getIdKey(EdaContext xContext) {

	return String.valueOf(getToolKit().getId());
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
	buffer.append("ToolKit object\n---------------\n");
	buffer.append("Name  : " + getName() + "\n");
	if (getToolKit().getStageName() != null)
	    buffer.append("Stage : " + getToolKit().getStageName().getName()
			  + "\n");
	else
	    buffer.append("Stage : null\n");
	buffer.append("CQ Rel: " + getCqName() + "\n");
	buffer.append("Parent: " + getParentId() + "\n");

	return buffer.toString();

    }


    /**
     * Copy all data from old Tool Kit to this Tool Kit.
     * 
     * @param xContext   Application context
     * @param oldTk      Existing Tool Kit
     * @param user       Person making the copy
     * @param components Collection of components to add
     * @throws IcofException
     */
    public void dbCopy(EdaContext xContext, ToolKit oldTk, User_Db user, 
                       Vector<String> components)
    throws IcofException {

	// Copy Components from old TK to this TK.
	Component_Version_Db oldCv = new Component_Version_Db(oldTk.getToolKit(),
	                                                      null);
	Vector<Component_Db> comps = oldCv.dbLookupAllComponents(xContext);
	for (Component_Db oldComp : comps) {

	    if ((components == null) || components.contains(oldComp.getName())) {
		System.out.println("Adding comp (" + getName() + ") : " + oldComp.getName());
		Component_Version_Db newCv = new Component_Version_Db(xContext,
		                                                      getToolKit(),
		                                                      oldComp,
		                                                      getStageName().getStage(),
		                                                      user);
		// Add if doesn't already exist
		try {
		    newCv.dbLookupByAll(xContext);
		}
		catch(IcofException trap) {
		    newCv.dbAddRow(xContext, user);
		}
	    }
	    else {
		System.out.println("Skipping comp: " + oldComp.getName());
	    }

	}

	// Set the TK Stage name to READY
	getToolKit().dbUpdateStageName(xContext, getStageName().getStage(),
				       user);

    }


    /**
     * Set this object to the Tool Kit in this object's stage.
     * 
     * @param xContext Application context
     */
    public void dbLookupToolKitByStage_OLD(EdaContext xContext)
    throws IcofException {

	try {
	    // Construct a RelVersion_Db object setting the initial TK
	    // to 14.1.build even though its never used.
	    toolKit = new RelVersion_Db(xContext, "14.1.build",
					getStageName().getStage());
	    Vector<RelVersion_Db> tks = toolKit.dbLookupByStageName(xContext,
								    getStageName().getStage());
	    toolKit = null;

	    // If 1 TK was found the set this object's members
	    if (tks.size() == 1) {
		setToolKit((RelVersion_Db) tks.firstElement());
		getToolKit().getRelease().dbLookupById(xContext);
		populate(xContext);
	    }
	    else if (tks.size() == 0) {
		throw new IcofException(this.getClass().getName(),
					"dbLookupToolKitByStage()",
					IcofException.SEVERE,
					"Unable to find ToolKit in the database "
					+ "for this stage.\n",
					"Stage: " + getStageName().getName());
	    }
	    else if (tks.size() > 1) {
		throw new IcofException(this.getClass().getName(),
					"dbLookupToolKitByStage()",
					IcofException.SEVERE,
					"Multiple ToolKits found in database "
					+ "for this stage.\n",
					"Stage: " + getStageName().getName());
	    }

	}
	catch (IcofException trap) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupToolKitByStage()",
				    IcofException.SEVERE,
				    "Unable to find ToolKit in database.\n",
				    trap.getMessage());
	}

    }


    /**
     * Set this object to the Tool Kit in this object's stage and Component
     * 
     * @param xContext Application context
     */
    public void dbLookupToolKitByStage_OLD(EdaContext xContext,
				       Component_Db component)
    throws IcofException {

	try {

	    Component_Version_Db cv = new Component_Version_Db(null);
	    Vector<Component_Version_Db> tks = cv.dbLookupCompVersions(xContext,
								       component,
								       getStageName().getStage());

	    // If 1 TK was found then set this object's members
	    if (tks.size() == 1) {
		cv = (Component_Version_Db) tks.firstElement();
		cv.dbLookupById(xContext);
		toolKit = new RelVersion_Db(cv.getVersion().getId());
		getToolKit().dbLookupById(xContext);
		getToolKit().getRelease().dbLookupById(xContext);
		populate(xContext);
	    }
	    else if (tks.size() == 0) {
		throw new IcofException(this.getClass().getName(),
					"dbLookupToolKitByStage()",
					IcofException.SEVERE,
					"Unable to find ToolKit in the database "
					+ "for this stage. TKs = 0.\n",
					"Stage: " + getStageName().getName());
	    }
	    else if (tks.size() > 1) {
		throw new IcofException(this.getClass().getName(),
					"dbLookupToolKitByStage()",
					IcofException.SEVERE,
					"Multiple ToolKits found in database "
					+ "for this stage. TKs = " + tks.size()
					+ "\n", "Stage: "
						+ getStageName().getName());
	    }

	}
	catch (IcofException trap) {
	    throw new IcofException(this.getClass().getName(),
				    "dbLookupToolKitByStage()",
				    IcofException.SEVERE,
				    "Unable to find ToolKit in database.\n",
				    trap.getMessage());
	}

    }


    /**
     * Populate this objects members from the DB object
     * 
     * @param xContext Application context
     */
    public void populate(EdaContext xContext)
    throws IcofException {

	if (!getToolKit().getRelease().isLoaded())
	    getToolKit().getRelease().dbLookupById(xContext);

	if (!getToolKit().getStageName().isLoaded())
	    getToolKit().getStageName().dbLookupById(xContext);

	setName(getToolKit().getDisplayName());
	setCqName(getToolKit().getCqReleaseName());
	setDescription(getToolKit().getDescription());
	setParentId(getToolKit().getParentId());
	setStage(xContext, getToolKit().getStageName());

    }


    /**
     * Verify the location is correct for the tool kit stage
     *
     * @param xContext Application context
     * @param location Location to verify
     * @throws IcofException 
     */
    public void validateLocation(EdaContext xContext, Location location)
    throws IcofException {

	boolean isError = true;
	
	switch (getStageName().getName().toUpperCase()) {
	
	case "DEVELOPMENT":
	    if (location.getName().equalsIgnoreCase("BUILD") ||
	        location.getName().equalsIgnoreCase("DEV") ||
	        location.getName().equalsIgnoreCase("PROD"))
		isError = false;
		break;
	case "PREVIEW":
	    if (location.getName().equalsIgnoreCase("SHIPB") ||
	        location.getName().equalsIgnoreCase("SHIP"))
		isError = false;
	    break;
	case "PRODUCTION":
	    if (location.getName().equalsIgnoreCase("TKB") ||
	        location.getName().equalsIgnoreCase("TK"))
		isError = false;
	    break;
	default:
	    String locName = location.getName().toUpperCase();
	    if (locName.startsWith("CUSTOMTKB") ||
	        locName.startsWith("CUSTOMTK"))
		isError = false;
	    break;
	}

	if (isError)
	    throw new IcofException("ToolKit", "validateLocation()", 
	                            IcofException.SEVERE, 
	                            "The Location (" + location.getName() + 
	                            ") is NOT correct for this tool kit (" + 
	                            getName() + ") in stage (" + 
	                            getStageName().getName().toUpperCase() + 
	                            ")!", "");
	
    }


    /**
     * Delete the component from this tool kit
     *
     * @param xContext
     * @param xComp
     * @throws IcofException 
     */
    public String dbDeleteComponent(EdaContext xContext, Component xComp,
                                  User_Db xUser) throws IcofException {
	
	StringBuffer reply = new StringBuffer();

	// Delete comp from component_tkVersion table
	Component_Version_Db compVer = new Component_Version_Db(xContext, 
	                                                        getToolKit(), 
	                                                        xComp.getComponent());
	compVer.dbLookupByCompRelVersion(xContext);
	compVer.dbDeleteRow(xContext, xUser);
	reply.append("Deleted comp from Comp_Ver table\n");
	
	
	// Delete from component_tkrelease table
	Component_Release_Db compRel;
	compRel = new Component_Release_Db(getToolKit().getRelease(),
	                                   xComp.getComponent());
	// May not be able to delete from this table since comp could be 
	// associated with other TKs for this release
	try {
	    compRel.dbDeleteRow(xContext, xUser);
	    reply.append("Deleted comp from Comp_Rel table\n");
	}
	catch(IcofException ie) {}
	
	
	// Delete from component table
//	try {
//	    xComp.getComponent().dbDeleteRow(xContext, xUser);
//	    reply.append("Deleted comp from Comp table\n");
//	}
//	catch(IcofException ie) {}
	
	return reply.toString();
		
    }

}
