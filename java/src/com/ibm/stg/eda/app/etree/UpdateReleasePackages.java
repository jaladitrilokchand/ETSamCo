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
 * Update  a Package Info to the DB. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 27/06/2012 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

import java.util.Hashtable;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.Platform_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ReleasePackage;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class UpdateReleasePackages extends AbstractReleasePackage {

	/**
	 * Constants.
	 */
	public static final String APP_NAME = "pir.updatePkg";
	public static final String APP_VERSION = "v1.0";

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
	public UpdateReleasePackages(EdaContext aContext, ToolKit aTk,
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
	 * @exception IcofException
	 *                Unable to construct ManageApplications object
	 */
	public UpdateReleasePackages(EdaContext aContext) throws IcofException {
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
			myApp = new UpdateReleasePackages(null);
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

		updatePackage(xContext);
		commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
	}

	/**
	 * Update the Release package info.
	 * 
	 * @param xContext
	 *            Application context
	 * @throws IcofException
	 *             Trouble querying database.
	 */
	private void updatePackage(EdaContext xContext) throws IcofException {

		// Updating the Package Name to the database.
		logInfo(xContext, "Updating Package name ...", verboseInd);

		// Set the Platform ID

		Platform_Db platDb = new Platform_Db(getPlatform(),getPlatform());
		platDb.dbLookupByPackagingName(xContext);

		short platId = platDb.getId();

		// Set the Component_Version object
		Component_Version_Db compVersion = setCompVersion(xContext);

		// update eventually adds the package.
		boolean result = updateReleasePackageInfo(xContext, compVersion,
				platId, getSpinLevel(), getPatchLevel(), getRelId());

		if (result) {
			logInfo(xContext, "Updated Package by adding to "
					+ getToolKit().getName() + "/" + getComponent().getName()
					+ " Release ID :" + getRelId(), true);
		} else {
			logInfo(xContext, "Unable to Update Package with Patch level :"
					+ getPatchLevel() + " Spin level :" + getSpinLevel()
					+ " Platform :" + getPlatform() + " Release ID :"
					+ getRelId() + "). \n", true);
		}

	}

	/**
	 * Update by adding the Release package info.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compVersion
	 *            Component_Version_Db object.
	 * @param platformId
	 *            The platform id
	 * @param spinLevel
	 *            The spin level id
	 * @param patchLevel
	 *            The patch level id
	 * @param relDbId
	 *            The Relation DB id
	 * @return True if Release package was updated
	 * @throws IcofException
	 */
	public boolean updateReleasePackageInfo(EdaContext xContext,
			Component_Version_Db compVersion, short platformId,
			short spinLevel, short patchLevel, long relDbId)
			throws IcofException {

		ReleasePackage releasePackage = new ReleasePackage();
		boolean result = false;
		result = updateReleasePackageInfo(xContext, compVersion, relDbId,
				platformId, spinLevel, patchLevel, releasePackage);
		return result;
	}

	/**
	 * update the Release package Info.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compVersion
	 *            Component_Version_Db Object.
	 * @param relDbId
	 *            The Relation DB id
	 * @param platformId
	 *            The platform id
	 * @param spinLevel
	 *            The spin level id
	 * @param releasePackage
	 *            TheReleasePackage Object
	 * @return True if Release package was updated
	 * @throws IcofException
	 */
	public boolean updateReleasePackageInfo(EdaContext xContext,
			Component_Version_Db compVersion, long relDbId, short platformId,
			short spinLevel, short patchLevel, ReleasePackage releasePackage)
			throws IcofException {
		return releasePackage.updateReleasePackageInfo(xContext, compVersion,
				relDbId, platformId, spinLevel, patchLevel);
	}

	/**
	 * update the Release package Info.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compVersion
	 *            Component_Version_Db Object.
	 * @param relDbId
	 *            The Relation DB id
	 * @param platformID
	 *            The patch level id
	 * @param releasePackage
	 *            TheReleasePackage Object
	 * @return True if Release package was updated
	 * @throws IcofException
	 */
	public boolean updateReleasePackagePlatInfo(EdaContext xContext,
			Component_Version_Db compVersion, long relDbId, short platformId,
			ReleasePackage releasePackage) throws IcofException {
		return releasePackage.updateReleasePackagePlatInfo(xContext,
				compVersion, relDbId, platformId);
	}

	/**
	 * update the Release package Info.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compVersion
	 *            Component_Version_Db Object.
	 * @param relDbId
	 *            The Relation DB id
	 * @param patchLevel
	 *            The patch level id
	 * @param releasePackage
	 *            TheReleasePackage Object
	 * @return True if Release package was updated
	 * @throws IcofException
	 */
	public boolean updateReleasePackagePatchInfo(EdaContext xContext,
			Component_Version_Db compVersion, long relDbId, short patchLevel,
			ReleasePackage releasePackage) throws IcofException {
		return releasePackage.updateReleasePackagePatchInfo(xContext,
				compVersion, relDbId, patchLevel);
	}

	/**
	 * update the Component Type to this ComponentRelease.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compType
	 *            Component_Version_Db Object.
	 * @param relDbId
	 *            The Relation DB id
	 * @param spinLevel
	 *            The spin level id
	 * @param releasePackage
	 *            TheReleasePackage Object
	 * @return True if Release package was updated
	 * @throws IcofException
	 */
	public boolean updateReleasePackageSpinInfo(EdaContext xContext,
			Component_Version_Db compVersion, long relDbId, short spinLevel,
			ReleasePackage releasePackage) throws IcofException {
		return releasePackage.updateReleasePackageSpinInfo(xContext,
				compVersion, relDbId, spinLevel);
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
		usage.append("Release Package is Updated in the ETREE database. \n");
		usage.append("\n");
		usage.append("USAGE:\n");
		usage.append("------\n");
		usage
				.append(APP_NAME
						+ " <-t toolkit> <-c component> <-p patch> <-s spin> <-plat platform> \n");
		usage.append("  [-y] [-h] [db dbMode]\n");
		usage.append("\n");
		usage.append(" toolkit = ToolKit name (14.1.0, 14.1.1 ...).\n");
		usage
				.append(" component  = Component name (ess, pds, model, einstimer ...).\n");
		usage.append(" patch level = Patch level\n");
		usage.append(" spin level = Spin level\n");
		usage.append(" platform level = Platform\n");
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

	protected String getAppName() {
		return APP_NAME;
	}

	protected String getAppVersion() {
		return APP_VERSION;
	}

	protected void createSwitches(Vector<String> singleSwitches,
			Vector<String> argSwitches) {
		super.createSwitches(singleSwitches, argSwitches);
		argSwitches.add("-r");
	}

	protected String readParams(Hashtable<String, String> params,
			String errors, EdaContext xContext) throws IcofException {

		super.readParams(params, errors, xContext);
		errors = parseReleaseIdnfo(params, errors, xContext);
		return errors;
	}

}
