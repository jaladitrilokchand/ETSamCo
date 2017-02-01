/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Prashanth Shivaram
 *
 *-PURPOSE---------------------------------------------------------------------
 * Show the Changes record  which have advanced to next for a given location/toolkit based on from and to date. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/07/2012 GFS  Initial coding.
 * 11/27/2012 GFS  Refactored to use business objects and support all flavors
 *                 of the tool kit name.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkConstants;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.ChangeRequest_Db;
import com.ibm.stg.eda.component.tk_etreedb.Location_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.eda.component.tk_etreeobjs.Location;
import com.ibm.stg.eda.component.tk_etreeobjs.ToolKit;
import com.ibm.stg.iipmds.common.IcofException;

public class GetChangesRecord extends TkAppBase {

	/**
	 * Constants.
	 */
	public static final String APP_NAME = "getChangesRecord";
	public static final String APP_VERSION = "v1.0";

	/**
	 * Constructor
	 * 
	 * @param aContext
	 *            Application context
	 * @param aComponent
	 *            A Component_Db object
	 * @param aLocation
	 *            A Location_Db object
	 */
	public GetChangesRecord(EdaContext aContext, Component aComponent,
			Location aLocation, ToolKit aToolKit) throws IcofException {

		super(aContext, APP_NAME, APP_VERSION);
		setComponent(aComponent);
		setToolKit(aToolKit);
		setLocation(aLocation);
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
	public GetChangesRecord(EdaContext aContext) throws IcofException {

		this(aContext, null, null, null);

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

			myApp = new GetChangesRecord(null);
			start(myApp, argv);
		} catch (Exception e) {
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
		setChangeRequests(xContext);

		// Show the Changes.
		showChangeRequests(xContext);

		// Set the return code.
		setReturnCode(xContext, SUCCESS);

		rollBackDB(xContext, APP_NAME);

	}

	/**
	 * Determine the Change record for this ToolKit, component and location
	 * 
	 * @param xContext
	 *            Application context
	 * @throws IcofException
	 */
	private void setChangeRequests(EdaContext xContext) throws IcofException {

		logInfo(xContext, "Reading Changed record from DB ...", verboseInd);

		// Lookup CodeUpdates (revisions) for this ComponentVersion_Location
		logInfo(xContext, " Querying DB for Changes record ...", verboseInd);
		ChangeRequest_Db changeRequest_Db = new ChangeRequest_Db(null);
		changeRequests = 
			changeRequest_Db.dbLookupRevsByCompVerLoc(xContext,
			                                          getToolKit().getToolKit(),
			                                          getComponent().getComponent(),
			                                          getLocationEventName(), 
			                                          getFromDate(), getToDate());
	}

	/**
	 * Show Change request info.
	 * 
	 * @param xContext
	 *            Application context
	 * @throws IcofException
	 */
	private void showChangeRequests(EdaContext xContext) throws IcofException {

		// Show the results
		int length = getChangeRequests().size();
		if(length<1){
			System.out.println("No Change Records were  found for "
					+ getToolKit().getToolKit().getDisplayName() + " "
					+ getComponent().getName() + " in "
					+ getLocation().getName() + " From: " +getFromDate() +" To: " +getToDate());
			
		}else{
			System.out.println("---------------------------------------------------------------------------");
			System.out.println("Change info for the toolkit: "+ getToolKit().getToolKit().getDisplayName() + " Component : "
					+ getComponent().getName() + " in "
					+ getLocation().getName() + " From : " +getFromDate() +" To: " +getToDate());
			
			System.out.println("---------------------------------------------------------------------------");
			Iterator<ChangeRequest_Db> iter = getChangeRequests().iterator();
			while (iter.hasNext()) {
				ChangeRequest_Db changeRequest_Db = iter.next();
				System.out.println(" Change Request ID : " +changeRequest_Db.getCqName());
				System.out.println(" Description : " +changeRequest_Db.getDescription());
				System.out.println(" Severity: " +changeRequest_Db.getSeverity().getId());
				System.out.println(" Created By : " +changeRequest_Db.getCreatedBy());
				System.out.println(" Created On : " +changeRequest_Db.getCreatedOn());
				
				
				System.out.println("---------------------------------------------------------------------------");
			}			
			
			}
		}


	protected void createSwitches(Vector<String> singleSwitches,
			Vector<String> argSwitches) {
		singleSwitches.add("-y");
		singleSwitches.add("-h");
		argSwitches.add("-db");
		argSwitches.add("-from");
		argSwitches.add("-to");
		argSwitches.add("-c");
		argSwitches.add("-t");
		argSwitches.add("-l");
	}

	protected String readParams(Hashtable<String, String> params,
			String errors, EdaContext xContext) throws IcofException {
		// Read the Component
		if (params.containsKey("-c")) {
			setComponent(xContext, params.get("-c"));
		} else {
			errors += "Component (-c) is a required parameter.";
		}

		// Read the Location
		if (params.containsKey("-l")) {
		    String myLoc = params.get("-l");
		    setLocation(xContext, myLoc.toUpperCase());
		    getLocation().dbLookupByName(xContext);
		} else {
		    errors += "Location (-l) is a required parameter.";
		}

		// Read the RelVersion
		if (params.containsKey("-t")) {
			setToolKit(xContext, params.get("-t"));
		} else {
			errors += "Tool Kit (-t) is a required parameter.";
		}

		// Read the from date
		if (params.containsKey("-from")) {
			setFromDate(params.get("-from"));
		} else {
			errors += "From date (-from) is a required parameter.";
		}

		// Read the to date
		if (params.containsKey("-to")) {
			setToDate(params.get("-to"));
		} else {
			errors += "To date (-to) is a required parameter.";
		}

		return errors;
	}

	protected void displayParameters(String dbMode, EdaContext xContext) {
		boolean verboseInd = getVerboseInd(xContext);
		logInfo(xContext, "App      : " + APP_NAME + "  " + APP_VERSION,
				verboseInd);
		if (getComponent() != null) {
			logInfo(xContext, "Component: " + getComponent().getName(),
					verboseInd);
		} else {
			logInfo(xContext, "Component: null", verboseInd);
		}
		if (getLocation() != null) {
			logInfo(xContext, "Location : " + getLocation().getName(),
					verboseInd);
		} else {
			logInfo(xContext, "Location : null", verboseInd);
		}
		if (getToolKit() != null) {
			logInfo(xContext, "ToolKit  : " + getToolKit().getName(),
					verboseInd);
		} else {
			logInfo(xContext, "ToolKit  : null", verboseInd);
		}

		logInfo(xContext, "From Date   : " + getFromDate(), verboseInd);
		logInfo(xContext, "To Date   : " + getToDate(), verboseInd);
		logInfo(xContext, "DB Mode  : " + dbMode, verboseInd);
		logInfo(xContext, "Verbose  : " + getVerboseInd(xContext), verboseInd);
	}

	private String getLocationEventName() {

		String eventName = "";
		String location = getLocation().getName();
		if (location.equals(Location_Db.LOC_PROD.toUpperCase())) {
			eventName = TkConstants.EVENT_ADV_TO_PROD;
		} else if (location.equals(Location_Db.LOC_BUILD.toUpperCase())
				|| location.equals(Location_Db.LOC_DEV.toUpperCase())) {
			eventName = TkConstants.EVENT_ADV_TO_DEV;
		} else if (location.equals(Location_Db.LOC_SHIPB.toUpperCase())
				|| location.equals(Location_Db.LOC_SHIP.toUpperCase())) {
			eventName = TkConstants.EVENT_ADV_TO_SHIPB;
		} else if (location.equals(Location_Db.LOC_TKB.toUpperCase())
				|| location.equals(Location_Db.LOC_TK.toUpperCase())) {
			eventName = TkConstants.EVENT_ADV_TO_TKB;
		}

		return eventName;
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
				.append("Displays the CQ info of those which are advanced to nest stage for the specified component and location based on from and to dates\n");
		usage.append("\n");
		usage.append("USAGE:\n");
		usage.append("------\n");
		usage.append(APP_NAME + " <-c component> <-l location>  <-t toolkit>\n");
		usage.append("                 <-from from_date> <-to to_date>\n");
		usage.append("                 [-y] [-h] [-db dbMode]\n");
		usage.append("\n");
		usage.append("  component = Component name (einstimer, model ...)\n");
		usage.append("  location  = Location name (build, dev, prod, shipb ...)\n");
		usage.append("  -latest   = (optional) shows the latest revision only\n");
		usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
		usage.append("  -h        = Help (shows this information)\n");
		usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
		usage.append("  toolkit   = ToolKit name (14.1.1, 14.1.2 ...)\n");
		usage.append("  from_date = From date (2011-01-01)\n");
		usage.append("  to_date   = To date (2012-01-01)\n");
		usage.append("\n");

		System.out.println(usage);

	}

	/**
	 * Members.
	 */
	private String fromDate;
	private String toDate;
	private Collection<ChangeRequest_Db> changeRequests;

	
	/**
	 * Getters.
	 */
	public Collection<ChangeRequest_Db> getChangeRequests() {
		return changeRequests;
	}

	protected String getAppName() {
		return APP_NAME;
	}

	protected String getAppVersion() {
		return APP_VERSION;
	}

	private String getFromDate() {
		return fromDate;
	}

	private String getToDate() {
		return toDate;
	}

	/**
	 * Setters.
	 */
	private void setFromDate(String date) {
		fromDate = date;
	}

	private void setToDate(String date) {
		toDate = date;
	}



	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
	}

}
