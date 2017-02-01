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
 * Get SVN access request in a given state. 
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/13/2013 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Component_Release_Db;
import com.ibm.stg.eda.component.tk_etreeobjs.AccessRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.EventName;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofStringUtil;

public class SvnGetRequests extends TkAppBase {

	/**
	 *  Constants.
	 */
	public static final String APP_NAME = "svn.getRequests";
	public static final String APP_VERSION = "v1.0";


	/**
	 * Constructor
	 *
	 * @param     aContext       Application context
	 * @param     anEvent        EventName to find requests for
	 */
	public SvnGetRequests(EdaContext aContext, EventName anEvent)
	throws IcofException {

		super(aContext, APP_NAME, APP_VERSION);
		setEventName(anEvent);

	}


	/**
	 * Constructor -- used when instantiating this class within its own main.
	 * 
	 * @param  aContext             the application context
	 * @exception IcofException     Unable to construct ManageApplications object
	 */
	public SvnGetRequests(EdaContext aContext) throws IcofException {

		this(aContext, null);

	}


	/**
	 * Instantiate the class and process the arguments.
	 *
	 * @param argv[] command line arguments
	 */
	public static void main(String argv[]) {

		TkAppBase myApp = null;
		try {
			myApp = new SvnGetRequests(null);
			start(myApp, argv);
		}

		catch (Exception e) {
			handleExceptionInMain(e);
		}
		finally {
			handleInFinallyBlock(myApp);
		}

	}


	/**
	 * Add, update, delete, or report on the specified applications.
	 * 
	 * @param aContext      Application Context
	 * @throws              IcofException
	 */
	public void process(EdaContext xContext) throws IcofException {

		// Connect to the database
		connectToDB(xContext);

		// Determine if branch/component are for a production or development TK.
		setRequests(xContext);
		showRequests(xContext);

		setReturnCode(xContext, SUCCESS);
		if (getRequests().isEmpty())
			setReturnCode(xContext, 2);
		rollBackDBAndSetReturncode(xContext, APP_NAME, getReturnCode(xContext));
		
	}


	/**
	 * Display data for each access request
	 * 
	 * @param xContext
	 * @throws IcofException 
	 */
	private void showRequests(EdaContext xContext) throws IcofException {

		if (getRequests().isEmpty()) {
			System.out.println("No Access Requests are currently in the " + 
			                   getEventName().getName() + " state");
			return;
		}

		StringBuffer out = new StringBuffer();
		if (! isQuiet()) {
			out.append(formatLine("Request Id", "Release", "Component", 
			                      "User", "Access", "Requester"));
			out.append(formatLine("----------", "-------", "---------", 
			                      "----", "------", "---------"));
		}

		Iterator<AccessRequest> iter = getRequests().iterator();
		while (iter.hasNext()) {			
			AccessRequest request = (AccessRequest)iter.next();
			
			Component_Release_Db compRel = 
				new Component_Release_Db(request.getDbObject().getCompRelease().getId());
			if (! compRel.isLoaded())
				compRel.dbLookupById(xContext);
			compRel.getComponent().dbLookupById(xContext);
			compRel.getRelease().dbLookupById(xContext);
									
			out.append(formatLine(String.valueOf(request.getDbObject().getId()), 
			                      compRel.getRelease().getName(),
			                      compRel.getComponent().getName(), 
			                      request.getUser().getIntranetId(),
			                      request.getAccessType().getName(),
			                      request.getRequester()));

		}

		System.out.print(out.toString());

	}


	/**
	 * Query the DB for SVN access requests
	 * 
	 * @param xContext  Application context
	 * @throws IcofException  Trouble querying database.
	 */
	private void setRequests(EdaContext xContext) throws IcofException {

		// Query the DB 
		logInfo(xContext, "Querying DB for SVN " + getEventName().getName() +
		        " requests ...", verboseInd);

		AccessRequest request = new AccessRequest(xContext, null, null, null, "");
		requests = request.dbLookupByEventName(xContext, getEventName());
		
		logInfo(xContext, "Found " + requests.size() + 
		        " SVN Access Requests in the " + getEventName().getName() + 
		        " state.", verboseInd);

	}


	/**
	 * Parse command line args
	 * 
	 * @param params    Collection of command line args/switches
	 * @param errors    String to store any error messages
	 * @param xContext  Application context object
	 */
	protected String readParams(Hashtable<String,String> params, String errors,
	                            EdaContext xContext)
	throws IcofException {

		// Read the EventName (state) name
		if (params.containsKey("-s")) {
			setEventName(xContext, (String) params.get("-s"));
		}
		else {
			errors += "Request state (-s) is a required parameter\n";
		}

		// Set the quiet flag
		setQuiet(false);
		if (params.containsKey("-q"))
			setQuiet(true);
		
		return errors;

	}


	/**
	 * Define application's command line switches
	 * 
	 * @param singleSwitches  Collection of switches
	 * @param argSwitches     Collection switches/args
	 */
	protected void createSwitches(Vector<String> singleSwitches, 
	                              Vector<String> argSwitches) {
		singleSwitches.add("-y");
		singleSwitches.add("-h");
		singleSwitches.add("-q");
		argSwitches.add("-db");
		argSwitches.add("-s");
	}


	/**
	 * Display application's invocation
	 * 
	 * @param dbMode    Database model
	 * @param xContext  Application context object 
	 */
	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App       : " + APP_NAME + "  " + APP_VERSION, verboseInd);
		logInfo(xContext, "EventName : " + getEventName().getName(), verboseInd);
		logInfo(xContext, "Quiet Mode: " + isQuiet(), verboseInd);
		logInfo(xContext, "DB Mode   : " + dbMode, verboseInd);
		logInfo(xContext, "Verbose   : " + getVerboseInd(xContext), verboseInd);
	}


	/**
	 * Display this application's usage and invocation
	 */
	protected void showUsage() {

		StringBuffer usage = new StringBuffer();
		usage.append("------------------------------------------------------\n");
		usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
		usage.append("------------------------------------------------------\n");
		usage.append("Return SVN Access Request data for each request in the \n");
		usage.append("given state.\n");
		usage.append("\n");
		usage.append("USAGE:\n");
		usage.append("------\n");
		usage.append(APP_NAME + " <-s state> [-q] [-y] [-h] [-db dbMode]\n");
		usage.append("\n");
		usage.append("  state       = SVN access request state (approved, complete ... )\n");
		usage.append("  -y          = (optional) Quiet mode (no putput headers)\n");
		usage.append("  -y          = (optional) Verbose mode (echo messages to screen)\n");
		usage.append("  dbMode      = (optional) DEV | PROD (defaults to PROD)\n");
		usage.append("  -h          = Help (shows this information)\n");
		usage.append("\n");
		usage.append("Return Codes\n");
		usage.append("------------\n");
		usage.append(" 0 = ok and requests found\n");
		usage.append(" 1 = application errors\n");
		usage.append(" 2 = ok and no requests found\n");
		usage.append("\n");

		System.out.println(usage);

	}

	/**
	 * Data members
	 */
	private EventName eventName;
	private boolean quiet = false;
	private Vector<AccessRequest> requests;


	/**
	 * Getters
	 */
	public EventName getEventName()  { return eventName; }
	public boolean isQuiet() { return quiet; }
	public Vector<AccessRequest> getRequests() { return requests; }


	/**
	 * Setters
	 */
	private void setEventName(EventName aName) { eventName = aName; }
	private void setQuiet(boolean aFlag) { quiet = aFlag; }


	/**
	 * Set the EventName object
	 * 
	 * @param xContext  Application context.
	 * @param aName     Event name
	 * @throws IcofException
	 */
	protected void setEventName(EdaContext xContext, String aName)
	throws IcofException {
		if (getEventName() == null) {
			eventName = new EventName(xContext, aName.toUpperCase());
			eventName.dbLookupByName(xContext);
		}
		logInfo(xContext,
		        "EventName: " + getEventName().toString(xContext),
		        getVerboseInd(xContext));
	}




	protected String getAppName() {
		return APP_NAME;
	}

	protected String getAppVersion() {
		return APP_VERSION;
	}


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}


	/**
	 * Format the display line
	 * @param id        Request id text
	 * @param release   Release name
	 * @param component Component name
	 * @param user      Intranet id
	 * @param access    Access Type
	 * @param requester Requester's name
	 * @return String formatted for display
	 */
	private String formatLine(String id, String release, String component,
	                          String user,  String access, String requester) {

		int idWidth = 12;
		int relWidth = 10;
		int compWidth = 15;
		int accessWidth = 12;
		int userWidth = 20;

		// Construct the line
		String line = IcofStringUtil.leftJustify(id, " ", idWidth) + 
		IcofStringUtil.leftJustify(release, " ", relWidth) +
		IcofStringUtil.leftJustify(component, " ", compWidth) +
		IcofStringUtil.leftJustify(access, " ", accessWidth) +
		IcofStringUtil.leftJustify(user, " ", userWidth) +
		IcofStringUtil.leftJustify(requester, " ", userWidth) +
		"\n";

		return line;

	}


}
