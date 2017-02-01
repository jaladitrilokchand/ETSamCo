package com.ibm.stg.eda.app.etree;

/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2012 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Prashanth Shivaram
 *
 *-PURPOSE---------------------------------------------------------------------
 * Abstract class for the  Release Package
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 06/27/2012 PS   Initial coding.
 * 11/27/2012 GFS  Refactored to use business objects.
 *=============================================================================
 * </pre>
 */

import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ReleasePackage;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public abstract class AbstractReleasePackage extends TkAppBase {

	private String platform = "";
	private short patchLevel = 0;
	private short spinLevel = 0;
	private long relId = 0l;

	/**
	 * Constructor
	 * 
	 * @param aContext
	 *            Application context
	 * @param aTk
	 *            A ToolKit Object
	 * @param anAppName
	 *            the application name
	 * @param anAppVersion
	 *            the application version
	 * @param aComponent
	 *            A Component object
	 */
	public AbstractReleasePackage(EdaContext aContext, ToolKit aTk,
			Component aComponent, String appName, String appVersion)
			throws IcofException {

		super(aContext, appName, appVersion);
		setToolKit(aTk);
		setComponent(aComponent);
	}

	/**
	 * Constructor -- used when instantiating this class within its own main.
	 * 
	 * @param aContext
	 *            the application context
	 * @param anAppName
	 *            the application name
	 * @param anAppVersion
	 *            the application version
	 * @exception IcofException
	 *                Unable to construct ManageApplications object
	 */
	public AbstractReleasePackage(EdaContext aContext, String appName,
	                              String appVersion)
	throws IcofException {
		this(aContext, null, null, appName, appVersion);
	}

	protected String readParams(Hashtable<String, String> params,
	                            String errors, EdaContext xContext)
	throws IcofException {

		errors = parseToolKitInfo(params, errors, xContext);
		errors = parseComponentInfo(params, errors, xContext);
		errors = parsePatchLevelInfo(params, errors);
		errors = parseSpinLevelInfo(params, errors);
		errors = parsePlatformName(params, errors);

		return errors;
	}

	protected String parseReleaseIdnfo(Hashtable<String, String> params,
	                                   String errors, EdaContext xContext)
	throws IcofException {
	
		// Read the Release ID info
		if (params.containsKey("-r")) {
			long value = Long.valueOf(params.get("-r"));
			setRelId(value);
		} else {
			errors += "Release Id (-r) is a required parameter\n";
		}
		return errors;
		
	}

	protected String parseComponentInfo(Hashtable<String, String> params,
	                                    String errors, EdaContext xContext)
	throws IcofException {
	
		// Read the Component name
		if (params.containsKey("-c")) {
			setComponent(xContext, params.get("-c"));
		} else {
			errors += "Component (-c) is a required parameter\n";
		}
		return errors;
	}

	protected String parseToolKitInfo(Hashtable<String, String> params,
	                                  String errors, EdaContext xContext) 
	throws IcofException {
	
		// Read the ToolKit name
		if (params.containsKey("-t")) {
			setToolKit(xContext, params.get("-t"));
		} else {
			errors += "ToolKit (-t) is a required parameter\n";
		}
		return errors;
		
	}

	
	protected String parsePatchLevelInfo(Hashtable<String, String> params,
	                                     String errors) {

		// Read the patch id
		if (params.containsKey("-p")) {
			setPatchLevel(Short.valueOf(params.get("-p")));
		} else {
			errors += "Patch Level (-p) is a required parameter\n";
		}
		return errors;
		
	}

	
	protected String parseSpinLevelInfo(Hashtable<String, String> params,
	                                    String errors) {
		
		// Read the spin level
		if (params.containsKey("-s")) {
			setSpinLevel(Short.valueOf(params.get("-s")));
		} else {
			errors += "Spin Level (-s) is a required parameter\n";
		}
		return errors;
		
	}

	
	protected String parsePlatformName(Hashtable<String, String> params,
	                                   String errors) {
		
		// Read the platform name
		if (params.containsKey("-plat")) {
			setPlatform(params.get("-plat"));
		} else {
			errors += "Packaging platform name (-plat) is a required parameter\n";
		}
		return errors;
		
	}

	
	protected void createSwitches(Vector<String> singleSwitches,
	                              Vector<String> argSwitches) {
		
		singleSwitches.add("-y");
		singleSwitches.add("-h");
		argSwitches.add("-db");
		argSwitches.add("-p");
		argSwitches.add("-plat");
		argSwitches.add("-s");
		argSwitches.add("-t");
		argSwitches.add("-c");
		
	}

	
	protected void displayParameters(String dbMode, EdaContext xContext) {
		
		logInfo(xContext, "App        : " + getAppName() + "  "
				+ getAppVersion(), verboseInd);
		logInfo(xContext, "ToolKit    : "
				+ getToolKit().getToolKit().getDisplayName(), verboseInd);
		logInfo(xContext, "Component  : " + getComponent().getName(),
				verboseInd);
		logInfo(xContext, "Patch Level: " + getPatchLevel(), verboseInd);
		logInfo(xContext, "Spin Level: " + getSpinLevel(), verboseInd);
		logInfo(xContext, "Platform: " + getPlatform(), verboseInd);
		logInfo(xContext, "DB Mode    : " + dbMode, verboseInd);
		logInfo(xContext, "Verbose    : " + getVerboseInd(xContext), verboseInd);

	}

	
	public String getPlatform() {
		return platform;
	}

	public short getPatchLevel() {
		return patchLevel;
	}

	public short getSpinLevel() {
		return spinLevel;
	}

	public long getRelId() {
		return relId;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public void setPatchLevel(short patchLevel) {
		this.patchLevel = patchLevel;
	}

	public void setSpinLevel(short spinLevel) {
		this.spinLevel = spinLevel;
	}

	public void setRelId(long relId) {
		this.relId = relId;
	}


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
	}

	protected Component_Version_Db setCompVersion(EdaContext xContext)
	throws IcofException {
	
		logInfo(xContext, "Looking up Component/ToolKit ...", verboseInd);
		Component_Version_Db compVersion = new Component_Version_Db(xContext,
				getToolKit().getToolKit(), getComponent().getComponent());
		compVersion.dbLookupByCompRelVersion(xContext);
		logInfo(xContext, "Component/ToolKit found ...", verboseInd);
		
		return compVersion;
		
	}

	
	/**
	 * Add the Release Package Info.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compVersion
	 *            Component Type to add.
	 * @param platformId
	 *            The platform id
	 * @param spinLevel
	 *            The spin level id
	 * @param patchLevel
	 *            The patch level id
	 * @param createdBy
	 *            Created By User
	 * @param createdOn
	 *            Created on
	 * @param updatedBy
	 *            Updated User
	 * @param updatedOn
	 *            Updated on
	 * @param deletedBy
	 *            Deleting User
	 * @param deletedOn
	 *            Deleting on
	 * @return True if Release package was added
	 * @throws IcofException
	 */
	public boolean addReleasePackageInfo(EdaContext xContext,
	                                     Component_Version_Db compVersion, 
	                                     short platformId,
	                                     short spinLevel, short patchLevel, 
	                                     String createdBy, Timestamp createdOn, 
	                                     String updatedBy, Timestamp updatedOn,
	                                     String deletedBy, Timestamp deletedOn) 
	throws IcofException {

		ReleasePackage releasePackage = new ReleasePackage();
		return releasePackage.addReleasePackageInfo(xContext, compVersion,
		                                            platformId, spinLevel, 
		                                            patchLevel, 
		                                            createdBy, createdOn,
		                                            updatedBy, updatedOn, 
		                                            deletedBy, deletedOn);
	}

	
	/**
	 * Add the Release Package Info.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compVersion
	 *            Component Type to add.
	 * @param platformId
	 *            The platform id
	 * @param spinLevel
	 *            The spin level id
	 * @param patchLevel
	 *            The patch level id
	 * @param createdBy
	 *            By The Created By users
	 * @return True if Release package was added
	 * @throws IcofException
	 */
	public boolean addReleasePackageInfo(EdaContext xContext,
	                                     Component_Version_Db compVersion, 
	                                     short platformId,
	                                     short spinLevel, short patchLevel, 
	                                     String createdBy)
	throws IcofException {

		ReleasePackage releasePackage = new ReleasePackage();
		return releasePackage.addReleasePackageInfo(xContext, compVersion,
		                                            platformId, spinLevel, 
		                                            patchLevel, 
		                                            createdBy, null, 
		                                            createdBy, null,
		                                            null, null);
		
	}
	
	
	public boolean migrateReleasePackageInfo(EdaContext xContext,
	                                         Component_Version_Db compVersion, 
	                                         long relDbId)
	throws IcofException {

		ReleasePackage releasePackage = new ReleasePackage();
		return releasePackage.migrateReleasePackageInfo(xContext, compVersion, 
		                                                relDbId);
	}

}
