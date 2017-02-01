/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2014 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Gregg Stadtlander
 *
 *-PURPOSE---------------------------------------------------------------------
 * Class for EDA Tool Kit inject request data
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 11/05/2013 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_patch;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;
import java.util.Vector;
import com.ibm.stg.iipmds.common.IcofDateUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;

public class CqInjectRequests implements java.io.Serializable {

    /**
     * Constructor
     * 
     * @param labels
     * @param results
     * @throws IcofException
     */
    public CqInjectRequests(Vector<String[]> labels, Vector<String[]> results)
    throws IcofException {
	setMembers(labels, results);
    }


    /**
     * Loads the members with data from the input collections.
     * 
     * @param inLabels Collection of CQ query labels
     * @param inResults Collection of CQ data
     * @throws IcofException
     */
    private void setMembers(Vector<String[]> inLabels,
			    Vector<String[]> inResults)
    throws IcofException {

	injects = new TreeMap<String, CqInjectRequest>();
	
	// Return if the input collections are null.
	if ((inLabels == null) || (inResults == null))
	    return;

	// Parse the content.
	for (int i = 0; i < inResults.size(); i++) {
	    
	    Object[] oValues = (Object[]) inResults.get(i);
	    
	    // Gather the desired data
	    String id = (String) oValues[1];
	    String developer = (String) oValues[3];
	    String component = (String) oValues[4];
	    String toolkit = (String) oValues[5];
	    
	    //String state = (String) oValues[STATE];
	    String state = "";
	    
	    String emerStr = (String) oValues[8];
	    boolean emergency = false;
	    if (emerStr.equalsIgnoreCase("yes"))
		emergency = true;
	    
	    developer = developer.substring(developer.indexOf("/") + 1, 
	                                    developer.indexOf("@"));
	    
	    CqInjectRequest inject = new CqInjectRequest(id, toolkit, 
	                                                 component, developer, 
	                                                 state, emergency);
	    injects.put(id, inject);

	}

    }


    /*
     * Constants
     */
    public static String CLASS_NAME = "TkXmittal";
    public static int STATE = 0;
    public static int CQID = 1;
    public static int DEVELOPER = 3;
    public static int COMPONENT = 4;
    public static int RELEASE = 5;
    public static int EMERGENCY = 8;
    

    /*
     * Getters
     * @formatter:off
     */
    public TreeMap<String, CqInjectRequest> getInjects() { return injects; }


    /*
     * Members
     */
    private TreeMap<String, CqInjectRequest> injects;
    // @formatter:on 


}
