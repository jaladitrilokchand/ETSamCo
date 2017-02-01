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
 * Update an SVN access request to a given state. 
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
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreeobjs.AccessRequest;
import com.ibm.stg.eda.component.tk_etreeobjs.EventName;
import com.ibm.stg.iipmds.common.IcofException;

public class SvnUpdateRequest extends TkAppBase {

	/**
	 *  Constants.
	 */
	public static final String APP_NAME = "svn.updateRequest";
	public static final String APP_VERSION = "v1.0";


	/**
	 * Constructor
	 *
	 * @param aContext  Application context
	 * @param aRequest  Access Request to update
	 * @param anEvent   New EventName (state)
	 */
	public SvnUpdateRequest(EdaContext aContext, AccessRequest aRequest,
	                        EventName anEvent)
	throws IcofException {

		super(aContext, APP_NAME, APP_VERSION);
		setAccessRequest(aRequest);
		setEventName(anEvent);

	}


	/**
	 * Constructor -- used when instantiating this class within its own main.
	 * 
	 * @param  aContext             the application context
	 * @exception IcofException     Unable to construct ManageApplications object
	 */
	public SvnUpdateRequest(EdaContext aContext) throws IcofException {

		this(aContext, null, null);

	}


	/**
	 * Instantiate the ValidateReleaesComponent class and process the arguments.
	 *
	 * @param     argv[]            the command line arguments
	 */
	public static void main(String argv[]) {

		TkAppBase myApp = null;
		try {
			myApp = new SvnUpdateRequest(null);
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

		connectToDB(xContext);

		updateRequest(xContext);

		commitToDB(xContext, APP_NAME);
		
	}


	/**
	 * Display data for each access request
	 * 
	 * @param xContext
	 * @throws IcofException 
	 */
	private void updateRequest(EdaContext xContext) throws IcofException {

		getAccessRequest().updateState(xContext, getEventName(), getUser());
		logInfo(xContext, "SVN Access Request state updated to " + 
		        getEventName().getName(), true);
	}


	/**
	 * Parse command line args
	 * 
	 * @param params    Collection of command line args/switches
	 * @param errors    String to store any error messages
	 * @param xContext  Application context object
	 */
	protected String readParams(Hashtable<String, String> params, 
	                            String errors, EdaContext xContext)
	throws IcofException {

		// Read the EventName (state) name
		if (params.containsKey("-s")) {
			setEventName(xContext, (String) params.get("-s"));
		}
		else {
			errors += "Request state (-s) is a required parameter\n";
		}

		// Read the Request name
		if (params.containsKey("-r"))
			setAccessRequest(xContext, (String)params.get("-r"));
		else {
			errors += "Request id (-r) is a required parameter\n";
		}
		
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
		argSwitches.add("-r");
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
		logInfo(xContext, "Request ID: " + getAccessRequest().getDbObject().getId(), verboseInd);
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
		usage.append("Update the given SVN Access Request to the new state. \n");
		usage.append("\n");
		usage.append("USAGE:\n");
		usage.append("------\n");
		usage.append(APP_NAME + " <-r request> <-s state> [-y] [-h] [-db dbMode]\n");
		usage.append("\n");
		usage.append("  request  = ID of existing SVN access request (1, 2, 3, ... )\n");
		usage.append("  state    = New SVN access request state (approved, complete ... )\n");
		usage.append("  -y       = (optional) Quiet mode (no putput headers)\n");
		usage.append("  -y       = (optional) Verbose mode (echo messages to screen)\n");
		usage.append("  dbMode   = (optional) DEV | PROD (defaults to PROD)\n");
		usage.append("  -h       = Help (shows this information)\n");
		usage.append("\n");
		usage.append("Return Codes\n");
		usage.append("------------\n");
		usage.append(" 0 = ok \n");
		usage.append(" 1 = application errors\n");
		usage.append("\n");

		System.out.println(usage);

	}

	
	/**
	 * Data members
	 */
	private EventName eventName;
	private AccessRequest request;
	

	/**
	 * Getters
	 */
	public EventName getEventName()  { return eventName; }
	public AccessRequest getAccessRequest() { return request; }


	/**
	 * Setters
	 */
	private void setEventName(EventName aName) { eventName = aName; }
	private void setAccessRequest(AccessRequest aRequest) { request = aRequest; }


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

	
	/**
	 * Set the Request object
	 * 
	 * @param xContext  Application context.
	 * @param anId      Request id
	 * @throws IcofException
	 */
	protected void setAccessRequest(EdaContext xContext, String anId)
	throws IcofException {
		if (getAccessRequest() == null) {
			request = new AccessRequest(xContext, Long.parseLong(anId));
			getAccessRequest().dbLookupById(xContext, Long.parseLong(anId));
		}
		logInfo(xContext,
		        "Access Request: " + getAccessRequest().toString(xContext),
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

}
