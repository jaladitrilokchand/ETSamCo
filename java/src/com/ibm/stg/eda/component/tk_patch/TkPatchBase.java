/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *
 *-PURPOSE---------------------------------------------------------------------
 * Class for basic EDA Tool Kit patch data
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 01/17/2012 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_patch;

import java.util.TreeMap;
import java.util.Vector;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;

public class TkPatchBase implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7069354150111343340L;


    /**
     * Constructor
     * 
     * @param labels
     * @param results
     * @throws IcofException
     */
    public TkPatchBase(Vector<String[]> labels, Vector<Object[]> results)
    throws IcofException {

	setMembers(labels, results);
    }


    /**
     * Loads the members with data from the input collections.
     * 
     * @param inLabels
     * @param inResults
     * @throws IcofException
     */
    private void setMembers(Vector<String[]> inLabels,
			    Vector<Object[]> inResults)
    throws IcofException {

	// Return if the input collections are null.
	if ((inLabels == null) || (inResults == null)) {
	    throw new IcofException("TkPatch", "setMembers()",
				    IcofException.SEVERE,
				    "Inject request lables/results are empty.",
				    "");
	}

	// Parse the labels.
	TreeMap<String, String> myLabels = new TreeMap<String, String>(
								       ManagerFunctions.STRINGCOMPARE);
	for (int i = 0; i < inLabels.size(); i++) {
	    String[] labels = inLabels.get(i);
	    myLabels.put(labels[0], Integer.toString(i));
	}

	// Parse the content.
	injectRequests = new TreeMap<String, TkInjectRequest>(
							      ManagerFunctions.STRINGCOMPARE);
	for (int i = 0; i < inResults.size(); i++) {
	    Object[] oValues = inResults.get(i);

	    // Collect the patch data
	    if (i == 0) {

		// Set the ID.
		String index = myLabels.get(ID);
		setId((String) oValues[Integer.parseInt(index)]);

		// Set the Component and Tool name.
		index = myLabels.get(COMPONENT);
		setComponent((String) oValues[Integer.parseInt(index)]);
		setToolName();

		// Set the Injector.
		index = myLabels.get(INJECTOR);
		setInjector((String) oValues[Integer.parseInt(index)]);

		// Set the Release.
		index = myLabels.get(RELEASE);
		setRelease((String) oValues[Integer.parseInt(index)]);

		// Set the State.
		index = myLabels.get(STATE);
		setState((String) oValues[Integer.parseInt(index)]);

		// Set the Transmit Ready.
		index = myLabels.get(TRANSMIT_READY);
		String ready = (String) oValues[Integer.parseInt(index)];
		setTransmitReady(false);
		if ((ready != null) && ready.equals("Yes")) {
		    setTransmitReady(true);
		}

	    }

	    // Load the Inject Request data.
	    String index = myLabels.get(INJECT_REQUESTS);
	    String requestId = (String) oValues[Integer.parseInt(index)];

	    index = myLabels.get(CMVC_PRIMARY);
	    String trackPrimary = (String) oValues[Integer.parseInt(index)];

	    index = myLabels.get(CMVC_SECONDARY);
	    String tracksSecondary = (String) oValues[Integer.parseInt(index)];

	    index = myLabels.get(FILES_UPDATED);
	    String filesUpdated = (String) oValues[Integer.parseInt(index)];

	    index = myLabels.get(DEVELOPER);
	    String developer = (String) oValues[Integer.parseInt(index)];

	    TkInjectRequest request = new TkInjectRequest(requestId,
							  trackPrimary,
							  tracksSecondary,
							  filesUpdated,
							  developer);

	    injectRequests.put(requestId, request);

	}

    }


    /*
     * Constants
     */
    public static final String CLASS_NAME = "TkPatch";
    public static final String DB_ID = "dbid";
    public static final String ID = "id";
    public static final String COMPONENT = "tk_component";
    public static final String STATE = "State";
    public static final String TRANSMIT_READY = "transmit_ready";
    public static final String INJECTOR = "injector";
    public static final String TRANSMITTOE = "transmitter";
    public static final String INJECT_REQUESTS = "tk_injectionrequests";
    public static final String TESTERS = "testers_additional_all";
    public static final String COMMENTS = "comment_log";
    public static final String HISTORY = "history";
    public static final String RELEASE = "tk_release_num";
    public static final String IS_DUPLICATE = "is_duplicate";
    public static final String CMVC_PRIMARY = "tk_injectionrequests.cmvc_primary";
    public static final String CMVC_SECONDARY = "tk_injectionrequests.cmvc_secondary";
    public static final String FILES_UPDATED = "tk_injectionrequests.files_updated";
    public static final String DEVELOPER = "tk_injectionrequests.developer";
    public static final String STATE_BUILT = "Built";


    /*
     * Getters
     */
    public String getId() {

	return id;
    }


    public String getComponent() {

	return component;
    }


    public String getState() {

	return state;
    }


    public boolean isTransmitReady() {

	return transmitReady;
    }


    public String getInjector() {

	return injector;
    }


    public TreeMap<String, TkInjectRequest> getInjectRequests() {

	return injectRequests;
    }


    public String getRelease() {

	return release;
    }


    public String getToolName() {

	return toolName;
    }


    /*
     * Setters
     */
    private void setId(String id) {

	this.id = id;
    }


    private void setComponent(String component) {

	this.component = component;
    }


    private void setToolName() {

	toolName = TkInjectUtils.getToolName(component);
    }


    public void setState(String state) {

	this.state = state;
    }


    private void setTransmitReady(boolean transmitReady) {

	this.transmitReady = transmitReady;
    }


    private void setInjector(String injector) {

	this.injector = injector;
    }


    public void setRelease(String release) {

	this.release = release;
    }


    /*
     * Members
     */
    private String id;
    private String component;
    private String state;
    private boolean transmitReady;
    private String injector;
    private TreeMap<String, TkInjectRequest> injectRequests;
    private String release;
    private String toolName;


}
