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
 * Migrate a Package to a new tool kit in the DB. 
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
import com.ibm.stg.eda.component.tk_etreedb.ReleasePackage_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class MigrateReleasePackage extends AbstractReleasePackage {

	/**
	 * Constants.
	 */
	public static final String APP_NAME = "pir.migratePkgs";
	public static final String APP_VERSION = "v1.0";

	private ToolKit aNewTk = null;

	/**
	 * Constructor
	 * 
	 * @param aContext
	 *            Application context
	 * @param aOldTk
	 *            ToolKit old Object
	 * @param aNewTk
	 *            ToolKit new Object
	 */
	public MigrateReleasePackage(EdaContext aContext, ToolKit aOldTk,
			ToolKit aNewTk) throws IcofException {

		super(aContext, APP_NAME, APP_VERSION);
		setToolKit(aOldTk);
		this.aNewTk = aNewTk;
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
	public MigrateReleasePackage(EdaContext aContext) throws IcofException {
		this(aContext, null, null);
	}

	/**
	 * Instantiate the ValidateReleaesComponent class and process the arguments.
	 * 
	 * @param argv
	 *            [] the command line arguments
	 */
	public static void main(String argv[]) {

		TkAppBase myApp = null;
		try {
			myApp = new MigrateReleasePackage(null);
			start(myApp, argv);
		}

		catch (Exception e) {
			handleExceptionInMain(e);
		} finally {
			handleInFinallyBlock(myApp);
		}

	}

	public void process(EdaContext xContext) throws IcofException {

		// Connect to the database
		connectToDB(xContext);

		migratePackages(xContext);
		commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
	}

	private void migratePackages(EdaContext xContext) throws IcofException {

		if (aNewTk.getName().equals(getToolKit().getName())) {
			throw new IcofException(APP_NAME, "migratePackages()",
					IcofException.SEVERE, "Unable to Migrate Packages.\n",
					"The tool kit to be migrated is same as the old tool kit");
		}

		Component_Version_Db compVer = new Component_Version_Db(getToolKit()
				.getToolKit());
		Vector<Component_Db> components = compVer
				.dbLookupAllComponents(xContext);

		Iterator<Component_Db> compItr = components.iterator();
		while (compItr.hasNext()) {

			Component_Db comp = compItr.next();
			System.out.println("Migration is happening for  " + comp.getName());

			setNewComponent(xContext, comp.getName());

			Component_Version_Db compVersion = setCompVersion(xContext);

			ReleasePackage_Db relDb = new ReleasePackage_Db();
			ArrayList<ReleasePackage_Db> releasePackageList = relDb
					.dbLookupComponentVersion(xContext, compVersion);
			Collections.sort(releasePackageList);

			int length = releasePackageList.size();
			if (length > 0) {
				relDb = releasePackageList.get(length - 1);
				migratePackage(xContext, relDb, comp);
				logInfo(xContext, "Migration of " + comp.getName()
						+ " is completed \n", true);
			} else {
				logInfo(xContext, "Migration  is not required  for "
						+ comp.getName() + " \n", true);
			}
		}
	}

	/**
	 * Migrates a Package.
	 * 
	 * @param xContext
	 *            Application context
	 * @param ReleasePackage_Db
	 *            ReleasePackage_Db Object *
	 * @param comp
	 *            Component_Db Object
	 * @throws IcofException
	 *             Trouble querying database.
	 */
	private void migratePackage(EdaContext xContext, ReleasePackage_Db relDb,
			Component_Db comp) throws IcofException {

		// Add the Package Name to the database.
		logInfo(xContext, "Migrating Package  name ...", verboseInd);

		Component_Version_Db compVersion = setCompVersionForNewTk(xContext,
				comp);

		try{
			boolean result = migrateReleasePackageInfo(xContext, compVersion, relDb.getId());
			if (result) {
				logInfo(xContext, "Migrated Package from " + getToolKit().getName()
						+ "/" + getComponent().getName() + " to "
						+ aNewTk.getName() + "/" + getComponent().getName(),
						true);
			} else {
				logInfo(xContext, "Unable to Migrate  Package of :"
						+ getToolKit().getName()
						+ "/" + getComponent().getName() + " to "
						+ aNewTk.getName() + "/" + getComponent().getName(),true);
			}
		}catch(IcofException ex){
			logInfo(xContext, "Unable to Migrate  Package of :"
					+ getToolKit().getName()
					+ "/" + getComponent().getName() + " to "
					+ aNewTk.getName() + "/" + getComponent().getName() +" "+ex.getMessage(),true);
		}
		
		
	}

	protected Component_Version_Db setCompVersionForNewTk(EdaContext xContext,
			Component_Db comp) throws IcofException {
		logInfo(xContext, "Looking up Component/ToolKit ...", verboseInd);

		Component_Version_Db compVersion = new Component_Version_Db(xContext,
				aNewTk.getToolKit(), comp);

		compVersion.dbLookupByCompRelVersion(xContext);
		logInfo(xContext, "Component/ToolKit found ...", verboseInd);
		return compVersion;
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
				.append("Migrating a new Release Package Info to the ETREE database. \n");
		usage.append("\n");
		usage.append("USAGE:\n");
		usage.append("------\n");
		usage.append(APP_NAME + " <-n newtoolkit> <-o oldtoolkit> \n");
		usage.append("            [-y] [-h] [db dbMode]\n");
		usage.append("\n");
		usage.append("  toolkit     = ToolKit name (14.1.0, 14.1.1 ...).\n");
		usage
				.append("  -y          = (optional) Verbose mode (echo messages to screen)\n");
		usage
				.append("  dbMode      = (optional) DEV | PROD (defaults to PROD)\n");
		usage.append("  -h          = Help (shows this information)\n");
		usage.append("\n");
		usage.append("Return Codes\n");
		usage.append("------------\n");
		usage.append(" 0 = application ran ok\n");
		usage.append(" 1 = application error\n");
		usage.append("\n");

		System.out.println(usage);

	}

	protected String getAppName() {
		return APP_NAME;
	}

	protected String getAppVersion() {
		return APP_VERSION;
	}

	protected String readParams(Hashtable<String, String> params,
			String errors, EdaContext xContext) throws IcofException {

		// Read the new ToolKit name
		if (params.containsKey("-n")) {
			setNewToolKit(xContext, params.get("-n"));
		} else {
			errors += "ToolKit (-n) is a required parameter\n";
		}

		// Read the Old ToolKit name
		if (params.containsKey("-o")) {
			setToolKit(xContext, params.get("-o"));
		} else {
			errors += "ToolKit (-o) is a required parameter\n";
		}
		return errors;

	}

	/**
	 * Set the ToolKit object from the name
	 * 
	 * @param xContext
	 *            Application context.
	 * @param aName
	 *            ToolKit name
	 * @throws IcofException
	 */
	protected void setNewToolKit(EdaContext xContext, String aName)
			throws IcofException {
		if (aNewTk == null) {
			aNewTk = new ToolKit(xContext, aName.trim());
			aNewTk.dbLookupByName(xContext);
			logInfo(xContext, aNewTk.toString(xContext), verboseInd);
		}
	}

	protected void createSwitches(Vector<String> singleSwitches,
			Vector<String> argSwitches) {
		singleSwitches.add("-y");
		singleSwitches.add("-h");
		argSwitches.add("-db");
		argSwitches.add("-n");
		argSwitches.add("-o");
	}

	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App        : " + getAppName() + "  "
				+ getAppVersion(), verboseInd);
		logInfo(xContext, "Old ToolKit    : "
				+ getToolKit().getToolKit().getDisplayName(), verboseInd);
		logInfo(xContext, "New ToolKit    : "
				+ aNewTk.getToolKit().getDisplayName(), verboseInd);
		logInfo(xContext, "DB Mode    : " + dbMode, verboseInd);
		logInfo(xContext, "Verbose    : " + getVerboseInd(xContext), verboseInd);
	}

	protected void setNewComponent(EdaContext xContext, String aName)
			throws IcofException {

		component = new Component(xContext, aName.trim());
		component.dbLookupByName(xContext);

		logInfo(xContext, getComponent().toString(xContext), false);
	}

}
