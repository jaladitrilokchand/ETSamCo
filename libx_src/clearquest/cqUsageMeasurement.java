/**
 * <pre>
 * =============================================================================
 *
 *  IBM Internal Use Only
 *
 * =============================================================================
 *
 *  CREATOR: Aydin Suren
 *     DEPT: AW0V
 *     DATE: 03/01/2008
 *     
 *-PURPOSE----------------------------------------------------------------------------------------
 * Class definition for the CQ service usage measurements.
 *------------------------------------------------------------------------------------------------------
 *
 * =============================================================================
 *
 * -CHANGE LOG------------------------------------------------------------------
 * 03/01/2008 AS  Initial coding.
 *
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.clearquest;

import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.util.Date;

/**
 * @author asuren
 * 
 */
public class cqUsageMeasurement {

    private cqUser xCqUser = null;
    private cqClient xCqClient = null;
    private Date dtStart = null;
    private Date dtEnd = null;
    private String sService = null;
    private final String sUSAGE_FILE_DIR = "/afs/btv/data/aes/prod/logs/CQService/usage";
    private final String sUSAGE_FILE = sUSAGE_FILE_DIR + "/cqServiceUsage";

    /**
     * Constructor.
     * 
     * @param xCqUser
     * @param xCqClient
     * @param sService
     * @throws Exception
     */
    public cqUsageMeasurement(cqUser xCqUser, cqClient xCqClient,
	    String sService) throws Exception {

	super();
	this.xCqUser = xCqUser;
	this.xCqClient = xCqClient;

	// Make sure it is a valid service.
	cqUtil.validateService(sService);
	this.sService = sService;

    }

    /**
     * Constructor.
     * 
     * @param xCqUser
     * @param xCqClient
     * @param sService
     * @param bStart
     * @throws Exception
     */
    public cqUsageMeasurement(cqUser xCqUser, cqClient xCqClient,
	    String sService, boolean bStart) throws Exception {
	this(xCqUser, xCqClient, sService);
	startTimer();
    }

    /**
     * Returns the name of the filename that is used for recording the
     * measurements data.
     * 
     * @return
     * @throws Exception
     */
    public String getMeasurementsFilename() throws Exception {

	// Check to make sure the directory exist. Create it if it doesn't.
	File fDir = new File(sUSAGE_FILE_DIR);
	if (!fDir.exists()) {
	    fDir.mkdirs();
	}

	return sUSAGE_FILE + "-" + cqUtil.getDate(cqUtil.sUSAGE_FILE_SUFFIX);
    }

    /**
     * Appends the measurements data to the file.
     * 
     * @throws Exception
     */
    public void record() throws Exception {

	String sDatabase = ""; // Some services may not have this value.
	String sSchema = ""; // Some services may not have this value.

	// If the start has not been defined yet, throw exception.
	if (dtStart == null) {
	    throw new Exception(
		    "Start timer is not initialized in measurement collection class.");
	}

	// Stop the timer if it was not defined yet.
	if (dtEnd == null) {
	    stopTimer();
	}

	// Get the hostname
	InetAddress addr = InetAddress.getLocalHost();
	String hostname = addr.getHostName();

	// Log the usage in logs directory. This log contains more
	// information then what's being logged in CQ.
	String sUser = xCqUser.getUsername();
	String sClient = xCqClient.getClientName();
	if (xCqUser.getDatabase() != null) {
	    sDatabase = xCqUser.getDatabase();
	}
	if (xCqUser.getSchema() != null) {
	    sSchema = xCqUser.getSchema();
	}

	String sRecord = sClient + ";" + sUser + ";" + sService + ";"
		+ sDatabase + ";" + sSchema + ";" + dtStart.toString() + ";"
		+ dtEnd.toString() + ";" + getDuration() + ";" + hostname
		+ ";\n";

	// Append to the file.
	append(sRecord);
    }

    /**
     * Starts the timer.
     * 
     * @throws Exception
     */
    public void startTimer() throws Exception {
	if (dtStart == null) {
	    this.dtStart = new Date();
	} else {
	    throw new Exception(
		    "The timer is already started. You can reset it.");
	}
    }

    /**
     * Stops the timer.
     * 
     * @throws Exception
     */
    public void stopTimer() throws Exception {
	this.dtEnd = new Date();
    }

    /**
     * Appends a given string to the measurements file.
     * 
     * @param sAppend
     * @throws Exception
     */
    private void append(String sAppend) throws Exception {

	String sFilename = null;
	FileWriter fw = null;

	try {
	    // Define the filename.
	    sFilename = getMeasurementsFilename();

	    // Append.
	    fw = new FileWriter(sFilename, true);
	    fw.write(sAppend);
	    fw.close();

	} catch (Exception e) {

	    // try writing to the /tmp file system.
	    FileWriter fw2 = null;
	    try {
		sFilename = "/tmp/cqServiceUsage";
		fw2 = new FileWriter(sFilename, true);
		fw2.write(sAppend);
		fw2.close();
	    } catch (Exception e2) {
		throw (e2);
	    } finally {
		fw2 = null;
	    }
	} finally {
	    fw = null;
	}
    }

    /**
     * Calculates the duration by substracting the end time from the start time.
     * 
     * @return
     * @throws Exception
     */
    private long getDuration() throws Exception {
	return dtEnd.getTime() - dtStart.getTime();
    }

}
