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
 * Add a Package to the DB. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 27/06/2012 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreedb.Component_Db;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.Platform_Db;
import com.ibm.stg.eda.component.tk_etreedb.ReleasePackage_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ReleasePackage;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class GetReleasePackages extends AbstractReleasePackage {

	/**
	 * Constants.
	 */
	public static final String APP_NAME = "pir.getPkgs";
	public static final String APP_VERSION = "v1.0";

	private boolean latest = false;

	/**
	 * Constructor
	 * 
	 * @param aContext
	 *            Application context
	 * @param aTk
	 *            ToolKit Object
	 * @param aComponent
	 *            Component Object
	 */
	public GetReleasePackages(EdaContext aContext, ToolKit aTk,
			Component aComponent) throws IcofException {

		super(aContext, APP_NAME, APP_VERSION);
		setToolKit(aTk);
		setComponent(aComponent);

	}

	/**
	 * Constructor -- used when instantiating this class within its own main.
	 * 
	 * @param aContext
	 *            the application context
	 * 
	 * @exception IcofException
	 *                Unable to construct ManageApplications object
	 */
	public GetReleasePackages(EdaContext aContext) throws IcofException {
		this(aContext, null, null);
	}

	/**
	 * Instantiate the class and process the arguments.
	 * 
	 * @param argv
	 *            [] the command line arguments
	 */
	public static void main(String argv[]) {

		TkAppBase myApp = null;
		try {
			myApp = new GetReleasePackages(null);
			start(myApp, argv);

		}

		catch (Exception e) {
			handleExceptionInMain(e);
		} finally {
			handleInFinallyBlock(myApp);
		}
	}

	// --------------------------------------------------------------------------
	/**
	 * Add, update, delete, or report on the specified applications.
	 * 
	 * @param aContext
	 *            Application Context
	 * @throws IcofException
	 */
	// --------------------------------------------------------------------------
	public void process(EdaContext xContext) throws IcofException {

		// Connect to the database
		connectToDB(xContext);
		getPackages(xContext);
		commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
	}

	/**
	 * Gets the package info.
	 * 
	 * @param xContext
	 *            Application context
	 * @throws IcofException
	 *             Trouble querying database.
	 */
	private void getPackages(EdaContext xContext) throws IcofException {

		// Getting the Package Name to the database.
		logInfo(xContext, "Getting Package name ...", verboseInd);

		boolean value = getVerboseInd(xContext);
		// Set the Platform ID
		short platId =0;
		if(getPlatform() != null){
			Platform_Db platDb = new Platform_Db(getPlatform(),getPlatform());
			platDb.dbLookupByPackagingName(xContext);
			platId = platDb.getId();
		}

		if(null  == getComponent()){
			Component_Version_Db compVer = new Component_Version_Db(
					getToolKit().getToolKit());
			// Lookup all Components (don't filter by Component Type)
			Vector<Component_Db> components = compVer
					.dbLookupAllComponents(xContext);
			
			Iterator<Component_Db> compItr = components.iterator();
			setVerboseInd(xContext, false);
			while (compItr.hasNext()) {
				Component_Db comp = compItr.next();
				Component_Version_Db compVersion = new Component_Version_Db(
						xContext, getToolKit().getToolKit(), comp);
				compVersion.dbLookupByCompRelVersion(xContext);
				setVerboseInd(xContext, false);
				getPackagesInfo(xContext, platId, compVersion, comp.getName());
				setVerboseInd(xContext, value);
			}

		} else {
			// Set the Component_Version object
			Component_Version_Db compVersion = setCompVersion(xContext);
			setVerboseInd(xContext, true);
			getPackagesInfo(xContext, platId, compVersion, getComponent().getName());
			setVerboseInd(xContext, value);
		}
	

	}

	private void getPackagesInfo(EdaContext xContext, short platId,
			Component_Version_Db compVersion, String compName) throws IcofException {
		ReleasePackage releasePackage = new ReleasePackage();
		// Getting the package.
		ArrayList<ReleasePackage_Db> releasePackageList = releasePackage
				.getReleasePackagesInfo(xContext, compVersion);

		if (releasePackageList.size() > 0) {
			Collections.sort(releasePackageList);
			
			if(isLatest()){
				int length = releasePackageList.size();
				showPackageInfo(releasePackageList.get(length-1), compName);
			}else{
				showPackageInfo(platId, releasePackageList, compName);
			}
		} else {
			logInfo(xContext, "Unable to get Package Info for :" + getToolKit().getName() + "/"
						+ compName + " Platform :" + getPlatform() + "). \n", getVerboseInd(xContext));
		}
	}



	private void showPackageInfo(short platId,
			ArrayList<ReleasePackage_Db> releasePackageList, String compName) {
		
		for (ReleasePackage_Db releasePackage_Db : releasePackageList) {
			if(getPlatform()==null){
				showPackageInfo(releasePackage_Db, compName);
			}else{
				if(releasePackage_Db.getPlatformId()  ==platId){
					showPackageInfo(releasePackage_Db, compName);
				}
			}
		}
	}

	private void showPackageInfo(ReleasePackage_Db releasePackage_Db, String compName) {
		
		String spinLevel = padZeroes(String.valueOf(releasePackage_Db.getSpinLevel()));
		String patchLevel = padZeroes(String.valueOf(releasePackage_Db.getPatchLevel()));
	
		String toolKitName = getToolKitName(getToolKit().getName());
		System.out.println(compName + "."
				+ toolKitName + "." + spinLevel+ "."
				+patchLevel + "." + getPlatform() );
	}
	
	private static String getToolKitName(String name) {
		
		String split[] = name.split("\\.");
		String val = split[0];
		if (split[1] != null) {
			if (split[1].length() < 2) {
				split[1] = "0" + split[1];
			}
			val = val + split[1];
		}
		return val;
	}

	private String padZeroes(String value){
		int length = value.length();
		if (length<4) {
			for(int i=0; i< 4-length; i++){
				value = "0" + value;
			}
		}
		return value;
	}

	/**
	 * Display this application's usage and invocation
	 */
	protected void showUsage() {

		StringBuffer usage = new StringBuffer();
		usage
				.append("------------------------------------------------------\n");
		usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
		usage
				.append("------------------------------------------------------\n");
		usage
				.append("Getting the Release Package Info from the ETREE database. \n");
		usage.append("\n");
		usage.append("USAGE:\n");
		usage.append("------\n");
		usage.append(APP_NAME
				+ " <-t toolkit>  \n");
		usage.append("  [-y] [-h] [db dbMode] [-plat platform] [-latest] [-c component]\n");
		usage.append("\n");
		usage.append(" toolkit = ToolKit name (14.1.0, 14.1.1 ...).\n");
		usage
				.append(" component  = Component name (ess, pds, model, einstimer ...).\n");
		usage.append(" platform  = Platform\n");
		usage.append(" latest  = Finds the latest PIR data only\n");
		usage
				.append(" -y   = (optional) Verbose mode (echo messages to screen)\n");
		usage.append(" dbMode = (optional) DEV | PROD (defaults to PROD)\n");
		usage.append(" -h   = Help (shows this information)\n");
		usage.append("\n");
		usage.append("Return Codes\n");
		usage.append("------------\n");
		usage.append(" 0 = application ran ok\n");
		usage.append(" 1 = application error\n");
		usage.append("\n");

		System.out.println(usage);

	}

	protected String readParams(Hashtable<String, String> params,
			String errors, EdaContext xContext) throws IcofException {

		errors = parseToolKitInfo(params, errors, xContext);
		errors = parseComponentInfo(params, errors, xContext);
		errors = parsePlatformName(params, errors);
		errors = parseLatest(params, errors);
		return errors;
	}
	
	
	protected String parseLatest(Hashtable<String, String> params,
			String errors) {
		// Read the platform name
		if (params.containsKey("-latest")) {
			setLatest(true);
		} 
		return errors;
	}


	protected String getAppName() {
		return APP_NAME;
	}

	protected String getAppVersion() {
		return APP_VERSION;
	}
	
	
	public boolean isLatest() {
		return latest;
	}

	public void setLatest(boolean latest) {
		this.latest = latest;
	}
	
	protected void createSwitches(Vector<String> singleSwitches,
			Vector<String> argSwitches) {
		super.createSwitches(singleSwitches, argSwitches);
		singleSwitches.add("-latest");
	}
	
	protected String parsePlatformName(Hashtable<String, String> params,
			String errors) {
		// Read the platform name
		if (params.containsKey("-plat")) {
			setPlatform(params.get("-plat"));
		}else{
			setPlatform(null);
		}
		return errors;
	}
	
	protected String parseComponentInfo(Hashtable<String, String> params,
			String errors, EdaContext xContext) throws IcofException {
		// Read the Component name
		if (params.containsKey("-c")) {
			setComponent(xContext, params.get("-c"));
		} 
		return errors;
	}
	
	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App        : " + getAppName() + "  "
				+ getAppVersion(), verboseInd);
		logInfo(xContext, "ToolKit    : "
				+ getToolKit().getToolKit().getDisplayName(), verboseInd);
		logInfo(xContext, "Patch Level: " + getPatchLevel(), verboseInd);
		logInfo(xContext, "Spin Level: " + getSpinLevel(), verboseInd);
		logInfo(xContext, "Platform: " + getPlatform(), verboseInd);
		logInfo(xContext, "DB Mode    : " + dbMode, verboseInd);
		logInfo(xContext, "Verbose    : " + getVerboseInd(xContext), verboseInd);
	}

	
}
