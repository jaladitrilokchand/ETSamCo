/**
 * <pre>
 * 
 * =============================================================================
 * 
 *  Copyright: (C) IBM Corporation 2007 - 2010 -- IBM Internal Use Only
 * 
 * =============================================================================
 * 
 *     FILE: AppMetrics.java
 * 
 *  CREATOR: Gregg Stadtlander (stadtlag)
 *     DEPT: AW0V
 *     DATE: 11/15/2007
 * 
 * -PURPOSE---------------------------------------------------------------------
 *  A class to hold application metrics.
 * -----------------------------------------------------------------------------
 * 
 * 
 * -CHANGE LOG------------------------------------------------------------------
 *  11/15/2007 GS  Initial coding.
 *  02/27/2008 AK  Fixed bug where even if running in prod mode, it wrote the log
 *                 to the dev location.
 *  11/11/2008 GS  Added DELIVERABLE_COUNT, DELIVERABLE_SIZE, 
 *                 TOTAL_DELIVERABLE_COUNT and TOTAL_DELIVERABLE_SIZE constants.
 *  04/09/2010 KW  Added support for ICCTEST appMode               
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.iipmds.icof.component.mom;

import java.lang.String;
import java.net.InetAddress;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofDateUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofStringUtil;
import com.ibm.stg.iipmds.common.IcofSystemUtil;

import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

public class AppMetrics {

	// --------------------------------------------------------------------------
	/**
	 * Constructor - appMode Used when instantiating this object from a file.
	 */
	// --------------------------------------------------------------------------
	public AppMetrics(String sAppMode) {
		setAppMode(sAppMode);
	}

	// --------------------------------------------------------------------------
	/**
	 * Constructor - appName, AppNote and appMode Used when instantiating this
	 * object from an application. The machine, user and start time will be read
	 * from the current environment.
	 */
	// --------------------------------------------------------------------------
	public AppMetrics(String sAppName, String sAppNote, String sAppMode)
	throws IcofException {
		setAppName(sAppName);
		setAppNote(sAppNote);
		setAppMode(sAppMode);
		setHostName();
		setUserid();
		setStartDate();

	}

	// --------------------------------------------------------------------------
	/**
	 * Class constants.
	 */
	// --------------------------------------------------------------------------
    public static final String DELIVERABLE_COUNT = "Deliverable_count=";
    public static final String DELIVERABLE_SIZE = "Deliverable_size=";
    public static final String TOTAL_DELIVERABLE_COUNT = "Total_deliverable_count=";
    public static final String TOTAL_DELIVERABLE_SIZE = "Total_deliverable_size=";

    private static final String F$sAppEnd = "Application end.";
	private static final String F$sApplication = "APPLICATION: ";
	private static final String F$sAppNote = "NOTE: ";
	private static final String F$sAppStart = "Application start.";
	private static final String F$sClassName = "AppMetrics";
	private static final String F$sEnd = "END: ";
	private static final String F$sEvent = "EVENT: ";
	private static final String F$sEventSeparator = " --> ";
	private static final String F$sHost = "HOST: ";
	private static final String F$sStart = "START: ";
	private static final String F$sUserid = "USERID: ";

    
	// --------------------------------------------------------------------------
	/**
	 * Validate the event name by ensuring it is not empty and contains some
	 * text and/or numbers
	 * 
	 * @param sEventName  Name of the timing mark.
	 */
	public static synchronized boolean validateEvent(String sEventName) {
		boolean bReturn = true;

		// Is the event name null?
		if (sEventName == null) {
			bReturn = false;

		} else {
			// Is the event name empty?
			if (sEventName.length() < 1) {
				bReturn = false;
			}

		}

		return bReturn;

	}

	// --------------------------------------------------------------------------
	/**
	 * Private members.
	 */
	// --------------------------------------------------------------------------
	private Date _dtEndDate = null;
	private Date _dtStartDate = null;
	private String _sAppMode = null;
	private String _sAppName = null;
	private String _sAppNote = null;
	private String _sHostName = null;
	private String _sUserid = null;
	private Vector _vcTimingMarks = null;


	// --------------------------------------------------------------------------
	/**
	 * Public getters.
	 */
	// --------------------------------------------------------------------------
	public String getAppMode() { return _sAppMode; }
	public String getAppName() { return _sAppName; }
	public String getAppNote() { return _sAppNote; }
	public Date getEndTime() { return _dtEndDate; }
	public String getHostName() { return _sHostName; }
	public Date getStartTime() { return _dtStartDate; }
	public Vector getTimes() { return _vcTimingMarks; }
	public String getUserid() { return _sUserid; }

	// --------------------------------------------------------------------------
	/**
	 * Add a timing mark to the timing mark collection using the current time
	 * and the specified event name.
	 * 
	 * @param anEventName   Name of the timing event.
	 */
	public void markEvent(String anEventName) throws IcofException {

		try {

			// Validate the event name.
			if (!validateEvent(anEventName)) {
				throw new Exception("Event name is empty or invalid."
						+ "  Event Name: " + anEventName);
			}

			// Save the event.
			addTimingMark(getCurrentTime(), anEventName);

		} catch (Exception e) {
			throw new IcofException(
					F$sClassName,
					"markEvent()",
					IcofException.SEVERE,
					"Failed to save the event in the timing marks collection.\n",
					e.getMessage());
		} finally { }

	}
	
	// --------------------------------------------------------------------------
	/**
	 * Read the metrics file and populate this class.
	 * 
	 * @param xFile           IcofFile object pointing to a metrics file.
	 * @throws IcofException  Problem reading the metrics file.
	 */
	public void readMetrics(IcofFile xFile) throws IcofException {

		String sLine = null;
		String[] saTokens = null;
		Iterator itLines = null;

		try {

			// Read the contents of the file
			xFile.read();

			// Save the contents to this object.
			itLines = xFile.getContents().iterator();
			while (itLines.hasNext()) {
				sLine = (String) itLines.next();
				saTokens = parseLine(sLine);
				
				// Found a timing event
				if (saTokens[0].equals(F$sEvent)) {
					addTimingMark(Long.valueOf(saTokens[1]), saTokens[4]);
				}

				// Found app data.
				else {
					if (saTokens[0].equals(F$sApplication)) {
						setAppName(saTokens[1]);
					}
					if (saTokens[0].equals(F$sUserid)) {
						setUserid(saTokens[1]);
					}
					if (saTokens[0].equals(F$sHost)) {
						setHostName(saTokens[1]);
					}
					if (saTokens[0].equals(F$sAppNote)) {
						String sNote = "";
						if (saTokens[1] != null) {
							sNote = saTokens[1];
						}
						setAppNote(sNote);
					}
					if (saTokens[0].equals(F$sStart)) {
						setStartDate(IcofDateUtil.formatDate(saTokens[1],
								IcofDateUtil.ACOS_REQUEST_DATE_FORMAT));
					}
					if (saTokens[0].equals(F$sEnd)) {
						setEndDate(IcofDateUtil.formatDate(saTokens[1],
								IcofDateUtil.ACOS_REQUEST_DATE_FORMAT));
					}

				}
			}

		} catch (Exception e) {
			throw new IcofException(F$sClassName, "readMetrics()",
					IcofException.SEVERE,
					"Failed to read the timing data from a file.\n",
					e.getMessage());
		} finally {
			xFile = null;
		}

	}

	// --------------------------------------------------------------------------
	/**
	 * Read the metrics file and populate this class.
	 * 
	 * @param sFile           Full path to metrics file.
	 * @throws IcofException  Problem reading the metrics file.
	 */
	public void readMetrics(String sFile) throws IcofException {

		IcofFile xFile = null;

		try {

			// Instantiate a file object and read the file.
			xFile = new IcofFile(sFile, false);
			readMetrics(xFile);

		} catch (Exception e) {
			throw new IcofException(F$sClassName, "readMetrics()",
					IcofException.SEVERE,
					"Failed to read the timing data from a file.\n",
					e.getMessage());
		} finally {
			xFile = null;
		}
	}
	
	// --------------------------------------------------------------------------
	/**
	 * Write the metrics data to a file.
	 * 
	 * @returns Name of metrics file.
	 * @throws IcofException  Problem writing metrics file.
	 */
	public String write() 
	throws IcofException {
		
		String sDir = null;
		String sFile = null;
		String sFullPath = null;
		IcofFile xDir = null;
		
		try {
		
			// Create the metrics directory.
			sDir = IcofSystemUtil.determineHighLevelQualifier(_sAppMode);
			if ((_sAppMode.equals(Constants.DEV)) || (_sAppMode.equals(Constants.ICCTEST))) {
				sDir += Constants.AES_DEV;
			}
			else {
				sDir += Constants.AES_PROD;
			}
			sDir += Constants.TIMING_DATA_DIR + IcofFile.separator + _sAppName;

			xDir = new IcofFile(sDir, true);
			xDir.validate(true);
			
			// Create the metrics file name.
			sFile = _sAppName + "_" + _sUserid + "_";
			sFile += IcofDateUtil.formatDate(_dtStartDate, 
					IcofDateUtil.ARCHIVE_FILE_DATE_FORMAT)
					+ Constants.TIME_EXTENSION;
			
			// Write this object to the timing file.
			sFullPath = sDir + IcofFile.separator + sFile;
			writeMetrics(sFullPath);
			
		} catch (Exception e) {
            throw new IcofException(F$sClassName,
                    "writeMetrics",
                    IcofException.SEVERE,
                    "Failed to write the timing event to a file.\n"
                    + " File: " + sFullPath + "\n",
                    e.getMessage());
		} finally {
			xDir = null;
		}
			
		return sFullPath;
		
	}
	
	// --------------------------------------------------------------------------
	/**
	 * Write the metrics data to a file.
	 * 
	 * @param sFile           Full path to metrics file.
	 * @throws IcofException  Problem writing metrics file.
	 */
	public void writeMetrics(String sFile) throws IcofException {

		String sNote = null;
		Long lTime = null;
		Long lStartTime = null;
		Long lLastTime = null;
		long lRunTime = 0;
		long lElapsedTime = 0;
		String sDescription = null;
		String sEntry = null;
		IcofFile xFile = null;
		Vector vcContents = null;

		try {
			
			// Set the end date.
			setEndDate();

			// Load the application data into file contents collection.
			vcContents = new Vector();
			vcContents.add(F$sApplication + getAppName());
			sNote = "";
			if (getAppNote() != null) {
				sNote = getAppNote();
			}
			vcContents.add(F$sAppNote + sNote);
			vcContents.add(F$sUserid + getUserid());
			vcContents.add(F$sHost + getHostName());
			vcContents.add(F$sStart
					+ IcofDateUtil.formatDate(getStartTime(),
							IcofDateUtil.ACOS_REQUEST_DATE_FORMAT));
			vcContents.add(F$sEnd
					+ IcofDateUtil.formatDate(getEndTime(),
							IcofDateUtil.ACOS_REQUEST_DATE_FORMAT));

			// Load the timing data into file contents collection.
			Iterator itTimes = _vcTimingMarks.iterator();
			while (itTimes.hasNext()) {
				Object[] xObject = (Object[]) itTimes.next();
				lTime = (Long) xObject[0];
				sDescription = (String) xObject[1];
				if (lStartTime == null) {
					lStartTime = lTime;
					lLastTime = lTime;
				}
				lRunTime = lTime.longValue() - lStartTime.longValue();
				lElapsedTime = lTime.longValue() - lLastTime.longValue();

				sEntry = F$sEvent + lTime + " " + lRunTime + " " + lElapsedTime
						 + F$sEventSeparator + sDescription;
				vcContents.add(sEntry);

				lLastTime = lTime;
			}

			// Write the timing data to the file
			xFile = new IcofFile(sFile, false);
			xFile.openWrite();
			xFile.write(vcContents);
			xFile.closeWrite();

		} catch (Exception e) {
			throw new IcofException(F$sClassName, "writeMetrics()",
					IcofException.SEVERE,
					"Failed to write application metrics to a file.\n",
					e.getMessage());
		} finally { 
			xFile = null;
			vcContents = null;
		}

	}
	
	// --------------------------------------------------------------------------
	/**
	 * Add a timing mark to the timing mark collection.
	 * 
	 * @param aTime        System time in mSec (Long).
	 * @param sEvent       Event description.
	 * @param anEventName  Name of the timing mark.
	 */
	private void addTimingMark(Long aTime, String sEvent) throws IcofException {

		try {
			
			// Verify or create the timimg marks TreeMap.
			if (_vcTimingMarks == null) {
				_vcTimingMarks = new Vector();
			}

			// Save the event.
			Object[] xObject = new Object[2];
			xObject[0] = aTime;
			xObject[1] = sEvent;
			_vcTimingMarks.add(xObject);

		} catch (Exception e) {
			throw new IcofException(
					F$sClassName,
					"addTimingMark()",
					IcofException.SEVERE,
					"Failed to save the event in the timing marks collection.\n",
					e.getMessage());
		} finally { }

	}
	
	// --------------------------------------------------------------------------
	/**
	 * Returns the current system time in milliseconds.
	 * 
	 * @returns Current time in mSec.
	 */
	private Long getCurrentTime() {
		Long lTime = new Long(System.currentTimeMillis());
		return lTime;

	}
	
	// --------------------------------------------------------------------------
	/**
	 * Parse a line from the metrics file into data elements.
	 * 
	 * @param sLine           Line from a metrics file to be parsed.
	 * @returns               String array
	 * @throws IcofException  Problem reading the metrics file.
	 */
	private String[] parseLine(String sLine) throws IcofException {

		int iStartIndex = 0;
		int iEndIndex = 0;
		String sKey = null;
		String[] saTokens = null;

		try {

			// Determine the type of entry.
			sKey = IcofStringUtil.getField(sLine, 1, " ");
			sKey += " ";
			iStartIndex = sLine.indexOf(" ") + 1;

			if (sKey.equals(F$sEvent)) {
				saTokens = new String[5];
				saTokens[0] = sKey;
				for (int i = 1; i <= 3; i++) {
					iEndIndex = sLine.indexOf(" ", iStartIndex);
					saTokens[i] = sLine.substring(iStartIndex, iEndIndex);
					iStartIndex = iEndIndex + 1;
				}
				iStartIndex = sLine.indexOf(F$sEventSeparator);
				saTokens[4] = sLine.substring(iStartIndex + F$sEventSeparator.length());

			} else {
				saTokens = new String[2];
				saTokens[0] = sKey;
				saTokens[1] = sLine.substring(iStartIndex);
			}

		} catch (Exception e) {
			throw new IcofException(F$sClassName, "parseLine()",
					IcofException.SEVERE,
					"Failed to parse the line inot data elememts.\n",
					e.getMessage());
		} finally {
		}

		return saTokens;

	}

	// --------------------------------------------------------------------------
	/**
	 * Private setters.
	 */
	// --------------------------------------------------------------------------
	private void setAppMode(String aMode) { _sAppMode = aMode; }
	private void setAppName(String aName) { _sAppName = aName; }
	private void setAppNote(String aNote) { _sAppNote = aNote; }
	
	// --------------------------------------------------------------------------
	/**
	 * Set the end time to the current time.
	 * 
	 */
	private void setEndDate() throws IcofException { 
		Date dtNow = new Date();

		setEndDate(dtNow);
		addTimingMark(getCurrentTime(), F$sAppEnd);

	}
	private void setEndDate(Date aTime) { _dtEndDate = aTime; }
	
	// --------------------------------------------------------------------------
	/**
	 * Set the machine name from the environment.
	 * 
	 * @throws IcofException  Problem reading the machine name.
	 */
	private void setHostName() throws IcofException {

		InetAddress addr = null;

		try {

			addr = InetAddress.getLocalHost();
			setHostName(addr.getHostName());

		} catch (Exception e) {
			throw new IcofException(F$sClassName, "setHostName()",
					IcofException.SEVERE,
					"Failed to determine host name from environment.\n", e
							.getMessage());
		} finally {
			addr = null;
		}

	}
	private void setHostName(String aMachine) { _sHostName = aMachine; }
	
	// --------------------------------------------------------------------------
	/**
	 * Set the start time to the current time.
	 * 
	 */
	private void setStartDate() 
	throws IcofException {
		Date dtNow = new Date();

		setStartDate(dtNow);
		addTimingMark(getCurrentTime(), F$sAppStart);

	}
	private void setStartDate(Date aTime) {
		_dtStartDate = aTime;
	}
	
	// --------------------------------------------------------------------------
	/**
	 * Set the user name from the environment.
	 * 
	 * @throws IcofException  Problem reading the user's name.
	 */
	private void setUserid() {
		setUserid(System.getProperty(Constants.USER_NAME_PROPERTY_TAG));
	}

	private void setUserid(String aUser) {
		_sUserid = aUser;
	}

}
