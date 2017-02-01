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
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class AddReleasePackage extends AbstractReleasePackage {

	/**
	 * Constants.
	 */
	public static final String APP_NAME = "pir.addPkg";
	public static final String APP_VERSION = "v1.0";

	/**
	 * Constructor
	 * 
	 * @param aContext
	 *            Application context
	 * @param aTk
	 *            ToolKit
	 * @param aComponent
	 *            A Component Object
	 */
	public AddReleasePackage(EdaContext aContext, ToolKit aTk,
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
	public AddReleasePackage(EdaContext aContext) throws IcofException {
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
			myApp = new AddReleasePackage(null);
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

		addPackage(xContext);
		commitToDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
	}

	/**
	 * Adds a new Package.
	 * 
	 * @param xContext
	 *            Application context
	 * @return True if Release package was added otherwise false
	 * @throws IcofException
	 *             Trouble querying database.
	 */
	private void addPackage(EdaContext xContext) throws IcofException {

		// Add the Package Name to the database.
		logInfo(xContext, "Adding Package  name ...", verboseInd);

		// Set the Platform ID

		Platform_Db platDb = new Platform_Db(getPlatform(),getPlatform());
		platDb.dbLookupByPackagingName(xContext);

		short platId = platDb.getId();

		// Set the Component_Version object
		Component_Version_Db compVersion = setCompVersion(xContext);

		// Add the package.
		boolean result = addReleasePackageInfo(xContext, compVersion, platId,
				getSpinLevel(), getPatchLevel(), getUser().getIntranetId());

		if (result) {
			logInfo(xContext, "Added Package ( Patch level :" + getPatchLevel()
					+ " Spin level :" + getSpinLevel() + " Platform :"
					+ getPlatform() + ") to " + getToolKit().getName() + "/"
					+ getComponent().getName(), true);
		} else {
			logInfo(xContext, "Unable to add new Package with  Patch level :"
					+ getPatchLevel() + " Spin level :" + getSpinLevel()
					+ " Platform :" + getPlatform() + "). \n", true);
			logInfo(xContext, "Already the PIR information exists ", true);
		}
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
				.append("Add a new Release Package Info to the ETREE database. \n");
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
