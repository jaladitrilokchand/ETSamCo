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
 * Class for EDA Tool Kit xmittal data
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

public class TkXmittal implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Constructor
     * 
     * @param labels
     * @param results
     * @throws IcofException
     */
    public TkXmittal(Vector<String[]> labels, Vector<String[]> results)
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

	// Return if the input collections are null.
	if ((inLabels == null) || (inResults == null)) {
	    throw new IcofException(this.getClass().getSimpleName(),
				    "setMembers()", IcofException.SEVERE,
				    "CQ lables/results are empty.", "");
	}

	// Parse the labels.
	TreeMap<String, String> myLabels = 
	  new TreeMap(ManagerFunctions.STRINGCOMPARE);
	for (int i = 0; i < inLabels.size(); i++) {
	    String[] labels = (String[]) inLabels.get(i);
	    myLabels.put(labels[0], Integer.toString(i));
	}

	// Parse the content.
	patches = new TreeMap(ManagerFunctions.STRINGCOMPARE);
	for (int i = 0; i < inResults.size(); i++) {
	    Object[] oValues = (Object[]) inResults.get(i);

	    // Set the ID.
	    String index = (String) myLabels.get(ID);
	    setId((String) oValues[Integer.parseInt(index)]);

	    // Set the Component and Tool name.
	    index = (String) myLabels.get(COMPONENT);
	    setComponent((String) oValues[Integer.parseInt(index)]);
	    
	    // Set the Release.
	    index = (String) myLabels.get(RELEASE);
	    setRelease((String) oValues[Integer.parseInt(index)]);

	    // Set the State.
	    index = (String) myLabels.get(STATE);
	    setState((String) oValues[Integer.parseInt(index)]);

	    // Set BuildReady 
	    index = (String) myLabels.get(BUILD_READY);
	    setBuildReady((String) oValues[Integer.parseInt(index)]);

	    // Set TransmitReady 
	    index = (String) myLabels.get(TRANSMIT_READY);
	    setTransmitReady((String) oValues[Integer.parseInt(index)]);
	    
	    // Set the Transmitter.
	    index = (String) myLabels.get(TRANSMITTER);
	    setTransmitter((String) oValues[Integer.parseInt(index)]);

	    TkPatch patch = new TkPatch(getComponent(), getId(), null,
					getTransmitter(), getRelease(),
					getState(), getBuildReady(), 
					getTransmitReady());

	    patches.put(getId(), patch);

	}

    }


    /*
     * Constants
     */
    public static String CLASS_NAME = "TkXmittal";
    public static String ID = "id";
    public static String COMPONENT = "tk_component";
    public static String STATE = "State";
    public static String TRANSMITTER = "transmitter.name";
    public static String RELEASE = "tk_release_num";
    public static String BUILD_READY = "buildready";
    public static String TRANSMIT_READY = "transmit_ready";
    

    /*
     * Getters
     * @formatter:off
     */
    public String getId() { return id; }
    public String getComponent() { return component; }
    public String getState() { return state; }
    public String getTransmitter() { return transmitter; }
    public String getRelease() { return release; }
    public String getBuildReady() { return buildReady; }
    public String getTransmitReady() { return transmitReady; }
    
    public IcofFile getLogFile() { return logFile; }
    public TreeMap<String, TkPatch> getPatches() { return patches; }


    /*
     * Setters
     */
    private void setId(String anId) { id = anId; }
    private void setComponent(String aComp) { component = aComp; }
    public void setState(String aState) { state = aState; }
    private void setTransmitter(String aName) { transmitter = aName; }
    public void setRelease(String aRelease) { release = aRelease; }
    public void setBuildReady(String aName) { buildReady = aName; }
    public void setTransmitReady(String aName) { transmitReady = aName; }


    /*
     * Members
     */
    private String id;
    private String component;
    private String release;
    private String state;
    private String transmitter;
    private String transmitReady;
    private String buildReady;
    
    private IcofFile logFile;
    private TreeMap<String, TkPatch> patches;
    // @formatter:on 

    /**
     * Write to the log file.
     * 
     * @param text Text to write to log file/screen
     * @param bEcho If true echo the message to the screen.
     * @throws IcofException
     */
    public void logIt(String text, boolean bEcho)
    throws IcofException {

	// Display message to the screen if echo is true.
	if (bEcho)
	    System.out.println(text);

	// Get the log file object.
	if (logFile == null) {
	    String logFileName = TkInjectUtils.getLogFileName(getId(),
							      getComponent(),
							      null, true);
	    logFile = new IcofFile(logFileName, false);
	    writeLogHeader();
	    logFile.validate(true);
	}

	// Open the log file and write the message to it
	try {
	    logFile.openAppend();
	    logFile.writeLine(text, bEcho);
	}
	finally {
	    logFile.closeAppend();
	}

    }


    /**
     * Write a header to the log file.
     * 
     * @throws IcofException
     */
    private void writeLogHeader()
    throws IcofException {

	StringBuffer header = new StringBuffer();

	// Get the host name
	String host = "unknown";
	try {
	    InetAddress localMachine = InetAddress.getLocalHost();
	    host = localMachine.getHostName();
	}
	catch (Exception ex) {
	}

	// Get the time.
	Calendar cal = Calendar.getInstance();
	Date now = cal.getTime();
	String time = IcofDateUtil.formatDate(now,
					      IcofDateUtil.ACOS_REQUEST_DATE_FORMAT);

	// Add patch info.
	header.append("============================================\n");
	header.append("User: " + System.getProperty("user.name") + "\n");
	header.append("Host: " + host + "\n");
	header.append("TK Patch: " + getId() + "\n");
	header.append("State: " + getState() + "\n");
	header.append("Time: " + time + "\n");
	header.append("============================================\n");

	logIt(header.toString(), false);

    }


}
