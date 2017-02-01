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

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.Platform_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.ReleasePackage;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class RemoveReleasePackage extends AbstractReleasePackage {

	/**
	 * Constants.
	 */
	public static final String APP_NAME = "pir.delPkg";
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
	public RemoveReleasePackage(EdaContext aContext, ToolKit aTk,
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
	public RemoveReleasePackage(EdaContext aContext) throws IcofException {
		this(aContext, null, null);
	}

	/**
	 * Instantiate the ValidateReleaseComponent class and process the arguments.
	 * 
	 * @param argv
	 *            [] the command line arguments
	 */
	public static void main(String argv[]) {

		TkAppBase myApp = null;
		try {
			myApp = new RemoveReleasePackage(null);
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

		removePackage(xContext);
		commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
	}

	/**
	 * Marks a package as deleted.
	 * 
	 * @param xContext
	 *            Application context
	 * @throws IcofException
	 *             Trouble querying database.
	 */
	private void removePackage(EdaContext xContext) throws IcofException {

		// Remove the Package Name to the database.
		logInfo(xContext, "Removing Package  name ...", verboseInd);

		// Set the Platform ID

		Platform_Db platDb = new Platform_Db(getPlatform(),getPlatform());
		platDb.dbLookupByPackagingName(xContext);

		short platId = platDb.getId();

		// Set the Component_Version object
		Component_Version_Db compVersion = setCompVersion(xContext);

		// Add the package.
		try {
			boolean result = removeReleasePackageInfo(xContext, compVersion,
					platId, getSpinLevel(), getPatchLevel(), getUser()
							.getIntranetId());
			if (result) {
				logInfo(xContext, "Marked the Package as deleted.( Patch level :"
						+ getPatchLevel() + " Spin level :" + getSpinLevel()
						+ " Platform :" + getPlatform() + ") in "
						+ getToolKit().getName() + "/" + getComponent().getName(),
						true);
			} 
		} catch (IcofException e) {
				logInfo(xContext, e.printStackTraceAsString(), true);
				logInfo(xContext,
						"Unable to Mark the Package as deleted ( Patch level :"
								+ getPatchLevel() + " Spin level :"
								+ getSpinLevel() + " Platform :" + getPlatform()
								+ "). \n", true);
				logInfo(xContext,
						"The Specified combination of  patch level, spin level, platform does not exists", true);
		}
	}

	/**
	 * Mark the Release package info as deleted.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compVersion
	 *            Component_Version_Db Object.
	 * @param platformId
	 *            The platform id
	 * @param spinLevel
	 *            The spin level id
	 * @param patchLevel
	 *            The patch level id
	 * @param deletedBy
	 *            Deleted By User
	 * @return True if Release package was marked as deleted otherwise false
	 * @throws IcofException
	 */
	public boolean removeReleasePackageInfo(EdaContext xContext,
			Component_Version_Db compVersion, short platformId,
			short spinLevel, short patchLevel, String deletedBy)
			throws IcofException {

		ReleasePackage relDb = new ReleasePackage();
		return relDb.removeReleasePackageInfo(xContext, compVersion,
				platformId, spinLevel, patchLevel, deletedBy);
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
				.append("Marking the Release Package as deleted in the ETREE database. \n");
		usage.append("\n");
		usage.append("USAGE:\n");
		usage.append("------\n");
		usage
				.append(APP_NAME
						+ " <-t toolkit> <-c component> <-p patch> <-s spin> <-plat platform>\n");
		usage.append("            [-y] [-h] [db dbMode]\n");
		usage.append("\n");
		usage.append("  toolkit     = ToolKit name (14.1.0, 14.1.1 ...).\n");
		usage
				.append("  component   = Component name (ess, pds, model, einstimer ...).\n");
		usage.append("  patch level      = Patch level\n");
		usage.append("  spin level      = Spin level\n");
		usage.append("  platform level      = Platform\n");
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

}
