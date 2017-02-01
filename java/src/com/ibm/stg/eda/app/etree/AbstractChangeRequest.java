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
 * Abstract Change Request class
 *-----------------------------------------------------------------------------
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 07/28/2012 PS  Initial coding.
 * 11/29/2012 GFS  Updated to support CR's new impacted customer attribute.
 * 05/22/2013 GFS  Updated to support default CRs.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.app.etree;

import java.util.Hashtable;
import java.util.Vector;
import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreeobjs.ChangeRequest;
import com.ibm.stg.iipmds.common.IcofException;

public abstract class AbstractChangeRequest extends TkAppBase {

    private boolean defaultCr;

    /**
     * Constructor
     *
     * @param     xContext  Application context
     * @param     aCqId     ChangeRequest's CQ number
     */
    public AbstractChangeRequest(EdaContext xContext, String aCqId, 
                                 String appName, String appVersion, 
                                 boolean aFlag)	
                                 throws IcofException {

	super(xContext, appName, appVersion);
	this.appName = appName;
	this.appVersion = appVersion;
	setChangeRequest(xContext, aCqId);
	setDefault(aFlag);
    }


    /**
     * Constructor
     *
     * @param     aContext  Application context
     * @param     aCr       ChangeRequest object
     */
    public AbstractChangeRequest(EdaContext aContext, 
                                 ChangeRequest aCr,
                                 String appName, String appVersion,  
                                 boolean aFlag)	
                                 throws IcofException {

	super(aContext, appName, appVersion);
	this.appName = appName;
	this.appVersion = appVersion;
	setChangeRequest(aCr);
	setDefault(aFlag);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public AbstractChangeRequest(EdaContext aContext, String appName, 
                                 String appVersion, boolean aFlag) 
                                 throws IcofException {

	this(aContext, (ChangeRequest)null, appName, appVersion, aFlag);

    }

    public abstract void doMarkOperation(EdaContext xContext) throws  Exception ;


    /**
     * Add, update, delete, or report on the specified applications.
     * 
     * @param aContext      Application Context
     * @throws Exception 
     */
    public void process(EdaContext xContext) throws Exception {

	// Connect to the database
	connectToDB(xContext);

	// Mark this CR 
	doMarkOperation(xContext);

	// Set the return code to success if we get this far.
	setReturnCode(xContext, SUCCESS);
	commitToDB(xContext, appName);

    }



    protected String readParams(Hashtable<String,String> params, String errors,
                                EdaContext xContext) 
                                throws IcofException {

	// Read the default CR switch
	setDefault(false);
	if (params.containsKey("-default")) {
	    setDefault(true);
	}

	// Read the ClearQuest name
	if (params.containsKey("-cr")) {
	    setChangeRequest(xContext, params.get("-cr"));
	}
	else if (params.containsKey("-cq")) {
	    setChangeRequest(xContext,  params.get("-cq"));
	} 

	// Verify only 1 required parameter was set.
	if ((getChangeRequest() == null) && (useDefault() == false)) {
	    errors += "Please specify a ChangeRequest id or the " +
	    "-default parameter!\n";        	
	}
	else if ((getChangeRequest() != null) && useDefault()) {
	    errors += "Please specify either a ChangeRequest id or the " +
	    "-default parameter but not both!\n";        	
	}

	return errors;

    }


    protected void createSwitches(Vector<String> singleSwitches, 
                                  Vector<String> argSwitches) {
	singleSwitches.add("-y");
	singleSwitches.add("-h");
	singleSwitches.add("-default");
	argSwitches.add("-db");
	argSwitches.add("-cq");
	argSwitches.add("-cr");
    }


    protected void displayParameters(String dbMode, EdaContext xContext) {
	logInfo(xContext, "App           : " + appName + " " + appVersion, verboseInd);
	if (getChangeRequest() != null){ 
	    logInfo(xContext, "ChangeRequest : " + getChangeRequest().getClearQuest(),
	            verboseInd);
	}
	else {
	    logInfo(xContext, "ChangeRequest : null", verboseInd);
	}
	logInfo(xContext, "Default    : " + useDefault(), verboseInd);
	logInfo(xContext, "DB Mode    : " + dbMode, verboseInd);
	logInfo(xContext, "Verbose    : " + getVerboseInd(xContext), verboseInd);
	logInfo(xContext, "Default    : " + useDefault(), verboseInd);
    }

    public boolean useDefault()  { return defaultCr; }
    private void setDefault(boolean aFlag) { defaultCr = aFlag; }


    /**
     * Lookup the specified CR in the database or determine the user's active
     * CR if requested.
     * 
     * @param xContext  Application context
     * @throws IcofException 
     */
    protected void lookupChangeRequest(EdaContext xContext) throws IcofException {

	setChangeRequest(xContext, getChangeRequest().getClearQuest());
	logInfo(xContext, "Change Request: " + getChangeRequest().getClearQuest(),
	        verboseInd);

    }


}