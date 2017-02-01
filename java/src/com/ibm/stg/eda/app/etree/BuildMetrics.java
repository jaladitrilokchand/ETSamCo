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
* Generate a report of build metrics 
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 01/22/2013 GFS  Initial coding.
* 01/25/2013 GFS  Updated some headings.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.etree;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.Report_build_stats;
import com.ibm.stg.eda.component.tk_etreeobjs.Component;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;

public class BuildMetrics extends TkAppBase {

    /**
     *  Constants.
     */
    public static final String APP_NAME = "buildStats";
    public static final String APP_VERSION = "v1.1";
    public static final String TMS_START = "2012-01-01 00:00:00";
    public static final String TMS_END = "2012-12-31 23:59:59";
    public static final String DELIM = "#";
    
    
    /**
     * Constructor
     *
     * @param     aContext    Application context
     * @param     aRoleName   Role name
     * @param     aRoleDesc   Description of role name
     * @param     aRoleTable  Role table (EdaTkRole, CompTkRelRole)
     *
     */
    public BuildMetrics(EdaContext aContext, Component aComp, 
                        Timestamp aStart, Timestamp anEnd)
    throws IcofException {

        super(aContext, APP_NAME, APP_VERSION);

        setComponent(aComp);
        setStartTime(aStart);
        setEndTime(anEnd);

    }


    /**
     * Constructor -- used when instantiating this class within its own main.
     * 
     * @param  aContext             the application context
     *
     * @exception IcofException     Unable to construct ManageApplications object
     */
    public BuildMetrics(EdaContext aContext) throws IcofException {
        this(aContext, null, null, null);
    }
    
    
    /**
     * Instantiate the ValidateReleaesComponent class and process the arguments.
     *
     * @param     argv[]            the command line arguments
     */
    public static void main(String argv[]) {

		TkAppBase myApp = null;
		try {

			myApp = new BuildMetrics(null);
			start(myApp, argv);
		}

		catch (Exception e) {

			handleExceptionInMain(e);
		} finally {

			handleInFinallyBlock(myApp);
		}

	}

    
    //--------------------------------------------------------------------------
    /**
     * Add, update, delete, or report on the specified applications.
     * 
     * @param aContext      Application Context
     * @throws              IcofException
     * @throws SQLException 
     * @throws ParseException 
     */
    //--------------------------------------------------------------------------
    public void process(EdaContext xContext) 
    throws IcofException, SQLException, ParseException {

        // Connect to the database
        connectToDB(xContext);
        
        setResults(xContext);
        summarizeResults(xContext);
        showResults(xContext);
        
        rollBackDBAndSetReturncode(xContext, APP_NAME, SUCCESS);
        disconnectFromDB(xContext);
        
    }

  
    /**
     * Display the summarized results
     * 
     * @param xContext  Application context
     */
    private void showResults(EdaContext xContext) {

    	logInfo(xContext, "Component: " + getComponent().getName(), true);
    	logInfo(xContext, "Start: " + getStartTime().toString(), true);
    	logInfo(xContext, "End  : " + getEndTime().toString(), true);
    	logInfo(xContext, "", true);
    	
    	showCounts(xContext);
    	logInfo(xContext, "\n", true);
    	showRunTimes(xContext);
    	
	}


    /**
     * Display start, success fail counts
     * 
     * @param xContext Application context
     */
    private void showCounts(EdaContext xContext) {
		
    	aixStarts = new Hashtable<String, Integer>();
    	linuxStarts = new Hashtable<String, Integer>();
    	aixFails = new Hashtable<String, Integer>();
    	linuxFails = new Hashtable<String, Integer>();
    	aixSuccess = new Hashtable<String, Integer>();
    	linuxSuccess = new Hashtable<String, Integer>();
    	
    	Iterator<String> iter = startCount.keySet().iterator();
    	while (iter.hasNext()) {
    		String key = (String) iter.next();
    		Integer countStart = (Integer)startCount.get(key);
    		Integer countFail = 0;
    		if (failCount.containsKey(key)) 
    			countFail = (Integer)failCount.get(key);
    		
    		Integer countSuccess = 0;
    		if (successCount.containsKey(key)) 
    			countSuccess = (Integer)successCount.get(key);
    		//logInfo(xContext, "Key: " + key + "  Value: " + startCount, verboseInd);
    		
    		//String location = getLocation(key);
    		String os = getOs(key);
    		String date = getDate(key);
    		
    		if (os.indexOf("AIX") > -1) {
    			aixStarts.put(date, countStart);
    			aixFails.put(date, countFail);
    			aixSuccess.put(date, countSuccess);
    		}
    		else {
    			linuxStarts.put(date, countStart);
    			linuxFails.put(date, countFail);
    			linuxSuccess.put(date, countSuccess);

    		}
    	}
    	
    	Collections.sort(dates);

    	// Display the results
    	String[] headers = {"", "AIX Build Counts", "Linux Build Counts"};
    	Integer[] widths = {10, 40, 40};

    	String[] headers2 = {"Date", "Starts", "Success", "Fail", "Unknown*",
    			           "Starts", "Success", "Fail", "Unknown*"};
    	Integer[] widths2 = {10, 10, 10, 10, 10, 10, 10, 10, 10};

    	
    	System.out.println(getReport().getHeader(headers, widths));
    	System.out.println(getReport().getHeader(headers2, widths2));

    	Iterator<String> iter2 = dates.iterator();
    	while (iter2.hasNext()) {
    		String date = (String) iter2.next();
    		
    		Integer aStartCount = 0;
    		Integer aSuccessCount = 0;
    		Integer aFailCount = 0; 
    		if (aixStarts.containsKey(date)) 
    			aStartCount = (Integer)aixStarts.get(date);
    		if (aixSuccess.containsKey(date)) 
    			aSuccessCount = (Integer)aixSuccess.get(date);
    		if (aixFails.containsKey(date)) 
    			aFailCount = (Integer)aixFails.get(date);
    		Integer aUserCount = 0;
    		if (aStartCount > (aSuccessCount + aFailCount)) {
    			aUserCount = aStartCount - aSuccessCount - aFailCount;
    		}
    		
    		Integer lStartCount = 0;
    		Integer lSuccessCount = 0;
    		Integer lFailCount = 0;
    		if (linuxStarts.containsKey(date)) 
    			lStartCount = (Integer)linuxStarts.get(date);
    		if (linuxSuccess.containsKey(date)) 
    			lSuccessCount = (Integer)linuxSuccess.get(date);
    		if (linuxFails.containsKey(date)) 
    			lFailCount = (Integer)linuxFails.get(date);
    		Integer lUserCount = 0;
    		if (lStartCount > (lSuccessCount + lFailCount)) {
    			lUserCount = lStartCount - lSuccessCount - lFailCount;
    		}

    		date = getPrintableDate(date);
    		String[] data = {date, 
    				         String.valueOf(aStartCount), 
    				         String.valueOf(aSuccessCount),
    				         String.valueOf(aFailCount),
    				         String.valueOf(aUserCount),
    				         String.valueOf(lStartCount),
    				         String.valueOf(lSuccessCount),
    				         String.valueOf(lFailCount),
    				         String.valueOf(lUserCount)};
    		System.out.print(getReport().formatLine(data, widths2));
    	}

		System.out.println("\n* success/fail event not logged most " +
		                   "likely due to user exit or system error.");
    	
	}

    
    /**
     * Display total and average run times
     * 
     * @param xContext Application context
     */
    private void showRunTimes(EdaContext xContext) {
		
    	aixRunTotal = new Hashtable<String, Long>();
    	linuxRunTotal = new Hashtable<String, Long>();
    	
    	Iterator<String> iter = elapsedTimes.keySet().iterator();
    	while (iter.hasNext()) {
    		String key = (String) iter.next();
    		Long elapsedTime = (Long)elapsedTimes.get(key);
    		elapsedTime = elapsedTime/1000; // Convert to seconds

    		logInfo(xContext, "Key: " + key + "  Value: " + elapsedTime, verboseInd);
    		
    		//String location = getLocation(key);
    		String os = getOs(key);
    		String date = getDate(key);
    		logInfo(xContext, "Date: " + date, verboseInd);
    		
    		if (os.indexOf("AIX") > -1) {
    			if (! aixRunTotal.containsKey(date)) {
    				aixRunTotal.put(date, elapsedTime);
    			}
    			else {
    				Long totalTime = (Long)aixRunTotal.get(date) + elapsedTime;
    				aixRunTotal.put(date, totalTime);
    			}
    		}
    		else {
    			if (! linuxRunTotal.containsKey(date)) {
    				linuxRunTotal.put(date, elapsedTime);
    			}
    			else {
    				Long totalTime = (Long)linuxRunTotal.get(date) + elapsedTime;
    				linuxRunTotal.put(date, totalTime);
    			}
    		}
    	}
    	
    	Collections.sort(dates);

    	// Display the results
    	String[] headers = {"", "AIX Build Times (seconds)", "Linux Build Times (seconds)"};
    	Integer[] widths = {10, 30, 30};

    	String[] headers2 = {"Date", "Total", "Average", "Total", "Average"};
    	Integer[] widths2 = {10, 15, 15, 15, 15};

    	System.out.println(getReport().getHeader(headers, widths));
    	System.out.println(getReport().getHeader(headers2, widths2));

    	Iterator<String> iter2 = dates.iterator();
    	while (iter2.hasNext()) {
    		String date = (String) iter2.next();
    		
    		Long aTotal = Long.valueOf(0);
    		Integer aCount = 0;
    		if (aixRunTotal.containsKey(date)) {
    			aTotal = (Long)aixRunTotal.get(date);
    			aCount = (Integer)aixSuccess.get(date) + (Integer)aixFails.get(date);
    		}
    		Long aAverage = Long.valueOf(0);
    		if (aTotal > 0)
    			aAverage = aTotal / aCount;

    		Long lTotal = Long.valueOf(0);
    		Integer lCount = 0;
    		if (linuxRunTotal.containsKey(date)) {
    			lTotal = (Long)linuxRunTotal.get(date);
    			lCount = (Integer)linuxSuccess.get(date) + (Integer)linuxFails.get(date);
    		}
    		Long lAverage = Long.valueOf(0);
    		if (lTotal > 0)
    			lAverage = lTotal / lCount;

    		date = getPrintableDate(date);
    		String[] data = {date, 
    				         String.valueOf(aTotal), 
    				         String.valueOf(aAverage),
    				         String.valueOf(lTotal),
    				         String.valueOf(lAverage)};
    		System.out.print(getReport().formatLine(data, widths2));
    	}

	}

    
    /**
     * Convert the date from yyyy/mm to mm/yyyy
     * 
     * @param date  Date as yyyy/mm for sorting
     * @return Date as mm/yyyy
     */
    private String getPrintableDate(String date) {
		int index = date.indexOf("/");
		return date.substring(index + 1) + "/" + date.substring(0, index);
	}


	/**
     * Get the location from the collection key
     * 
     * @param key  
     * @return location name
     */
	private String getLocation(String key) {
		return key.substring(0, key.indexOf(DELIM));
	}
	
	
	/**
	 * Get the date from the collection key
	 * 
	 * @param key
	 * @return date
	 */
	private String getDate(String key) {
		return key.substring(key.lastIndexOf(DELIM) + 1);
	}
	
	
	/**
	 * Get the OS from the collection key
	 * 
	 * @param key
	 * @return OS name
	 */
	private String getOs(String key) {
		return key.substring(key.indexOf(DELIM) + 1, key.lastIndexOf(DELIM));
	}
	

	/**
     * Summarize the results by date, success and failures.
     * 
     * @param xContext  Application context
     * @throws IcofException 
     * @throws ParseException 
     */
	private void summarizeResults(EdaContext xContext) 
	throws IcofException, ParseException {
		
		// Initialize the collections
		startCount = new Hashtable<String, Integer>();
		failCount = new Hashtable<String, Integer>();
		successCount = new Hashtable<String, Integer>();
		startTimes = new Hashtable<String, Date>();
		elapsedTimes = new Hashtable<String, Long>();

    	dates = new ArrayList<String>();
		Iterator<String> iter = getReport().getRawContent().iterator();
		while (iter.hasNext()) {
			String line = (String)iter.next();
			logInfo(xContext, "Line: " + line, verboseInd);
			Vector<String> elements = new Vector<String>();
			IcofCollectionsUtil.parseString(line, " ", elements, true);
			
			String event = (String)elements.get(0);
			String os = (String)elements.get(1);
			String location = (String)elements.get(2);
			String date = (String)elements.get(3);
			String time = (String)elements.get(4);
			String tms = date + " " + time;
			if (tms.indexOf(".") == (tms.length()-1))
				tms += "0";

			if (event.equals("BUILD_START") || event.equals("BUILD_MANUAL_START")) {
				setStartEvent(xContext, os, location, tms);
			}
			else if (event.equals("BUILD_SUCCESS")) {
				setSuccessEvent(xContext, os, location, tms);
			} 
			else if (event.equals("BUILD_FAIL")) {
				setFailEvent(xContext, os, location, tms);
			}
			else {
				throw new IcofException(this.getClass().getName(), 
				                        "summarizeResults()",
				                        IcofException.SEVERE, 
				                        "Unknown event name found ... ",
				                        event);
			}
		}
		
	}


	/**
	 * Process the success event
	 * 
	 * @param xContext Application context
	 * @param os       Event OS
	 * @param location Event location name
	 * @param tms      Event timestamp
	 * @throws ParseException 
	 */
	private void setSuccessEvent(EdaContext xContext, String os, 
	                             String location, String tms) throws ParseException {

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");  
    	Date dDate = df.parse(tms);  
		
		// Set the hash keys
		String key = getKey(location, os);
		String dateKey = getDatedKey(location, os, dDate);
		
		// Log the elapsed time
		long diff = 0;
		if (startTimes.containsKey(key)) {
			Date start = (Date)startTimes.get(key);
			diff = Math.abs(dDate.getTime() - start.getTime());
			elapsedTimes.put(dateKey, Long.valueOf(diff));
		}
		
		// Log the success
		Integer count = 1;
		if (successCount.containsKey(dateKey)) {
			count = (Integer)successCount.get(dateKey) + 1;
		}
		successCount.put(dateKey, count);
		
		logInfo(xContext, "Success " + count + " " + diff, verboseInd);

	}


	/**
	 * Generate the date specific hash key
	 * 
	 * @param location  Event location
	 * @param os        Event OS
	 * @param tms       Event timestamp
	 * @return   key
	 */
	private String getDatedKey(String location, String os, Date tms) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM");
		String date = sdf.format(tms);
		
		if (! dates.contains(date)) 
			dates.add(date);

		return getKey(location, os) + DELIM + date;
	}


	/**
	 * Generate the event hash key
	 * 
	 * @param location  Event location
	 * @param os        Event OS
	 * @return  key
	 */
	private String getKey(String location, String os) {
		return location + DELIM + os;
	}


	/**
	 * Process the fail event
	 * 
	 * @param xContext Appplication context 
	 * @param os       Event OS
	 * @param location Event location name
	 * @param tms      Event timestamp
	 * @throws ParseException 
	 */
	private void setFailEvent(EdaContext xContext, String os, String location,
	                          String tms) throws ParseException {
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");  
    	Date dDate = df.parse(tms);  

		// Set the hash keys
		String key = getKey(location, os);
		String Key = getDatedKey(location, os, dDate);
		
		// Log the elapsed time
		long diff = 0;
		if (startTimes.containsKey(key)) {
			Date start = (Date)startTimes.get(key);
			diff = Math.abs(dDate.getTime() - start.getTime());
			elapsedTimes.put(Key, Long.valueOf(diff));
		}

		// Log the fail
		Integer count = 1;
		if (failCount.containsKey(Key)) {
			count = (Integer)failCount.get(Key) + 1;
		}
		failCount.put(Key, count);
		
		logInfo(xContext, "FailSuccess " + count + " " + diff, verboseInd);
		
	}
	

	/**
	 * Process the start event
	 * 
	 * @param xContext Application context
	 * @param os       Event OS
	 * @param location Event location name
	 * @param tms      Event timestamp
	 * @throws ParseException 
	 */
	private void setStartEvent(EdaContext xContext, String os, String location,
	                           String tms) throws ParseException {

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");  
    	Date dDate = df.parse(tms);  
		
		// Set the hash keys
		String key = getKey(location, os);
		String Key = getDatedKey(location, os, dDate);

		startTimes.put(key, dDate);
		
		Integer count = 1;
		if (startCount.containsKey(Key)) {
			count = (Integer)startCount.get(Key) + 1;
		}
		startCount.put(Key, count);
		
		logInfo(xContext, "Start " + count, verboseInd);
		
	}


	/**
     * Query the DB for build metrics
     *      
     * @param xContext  Application context
     * @throws IcofException 
	 * @throws SQLException 
     */
    private void setResults(EdaContext xContext) throws IcofException, SQLException {

    	String q = "";
    	q += "select len.Eventname, p.os, l.name, le.Created_TmStmp " +
    	"from tk.Component as c," +
    	" tk.Component_TkRelease as cr," +
    	" tk.Component_TkVersion as cv," +
    	" tk.LocationEventName as len," +
    	" tk.LocationEvent as le, " +
    	" tk.ComponentTkVersion_Location as cvl," +
    	" tk.Platform as p," +
    	" tk.Location as l " +
    	"where c.Component_Name = '" + getComponent().getName() + "'" +
    	" and c.Component_id = cr.Component_id" +
    	" and cr.Component_TkRelease_id = cv.Component_TkRelease_id" +
    	" and cvl.Component_TkVersion_id = cv.Component_TkVersion_id" +
    	" and le.ComponentTkVersion_Location_id = cvl.ComponentTkVersion_Location_id" +
    	" and le.LocationEventName_id = len.LocationEventName_id" +
    	" and le.Platform_id = p.Platform_id" +
    	" and cvl.Location_id = l.Location_id" +
    	" and le.Created_TmStmp between '" + getStartTime().toString() + "'" +
    	" and '" + getEndTime().toString() + "'" +
    	" and len.EventName like 'BUILD%' " +
    	"order by Created_TmStmp";
    	
    	String[] headers = {"Event", "OS", "Location", "Timestamp"};
    	Integer[] widths = {15, 10, 10, 20};

    	xReport = new Report_build_stats(q, headers, widths);
    	xReport.setContent(xContext);

    	logInfo(xContext, xReport.getHeader(), verboseInd);
    	logInfo(xContext, xReport.getContent().toString(), verboseInd);

    }


    /**
     * Define command line switches to parse
     * 
     * @param singleSwitches  Collection of single switches 
     * @param argSwitches     Collection of switches needing args
     */
	protected void createSwitches(Vector<String> singleSwitches, 
	                              Vector<String> argSwitches) {
		singleSwitches.add("-y");
        singleSwitches.add("-h");
        argSwitches.add("-db");
        argSwitches.add("-c");
        argSwitches.add("-s");
        argSwitches.add("-e");
	}


	/**
	 *  Parse the command line switches
	 *  
	 *  @param params    Collection of command line parameters
	 *  @param errors    Error messages
	 *  @param xContext  Application context
	 * @throws IcofException 
	 */
	protected String readParams(Hashtable<String,String> params,
	                            String errors, EdaContext xContext) 
	throws IcofException {

		// Read the component name
        if (params.containsKey("-c")) {
            setComponent(xContext, (String) params.get("-c"));
        }
        else {
            errors += "Component name (-c) is a required parameter.";
        }

        // Read the start timestamp
        if (params.containsKey("-s")) {
            setStartTime(Timestamp.valueOf((String) params.get("-s")));
        }
        else {
        	setStartTime(Timestamp.valueOf(TMS_START));
        }

        // Read the end timestamp
        if (params.containsKey("-e")) {
            setEndTime(Timestamp.valueOf((String) params.get("-e")));
        }
        else {
        	setEndTime(Timestamp.valueOf(TMS_END));
        }

		return errors;

	}


	/**
	 * Display parameters
	 */
	protected void displayParameters(String dbMode, EdaContext xContext) {
		
		boolean verboseInd = getVerboseInd(xContext);
        logInfo(xContext, "App      : " + APP_NAME + "  " + APP_VERSION, verboseInd);
        logInfo(xContext, "Component: " + getComponent().getName(), verboseInd);
        logInfo(xContext, "Start    : " + getStartTime().toString(), verboseInd);
        logInfo(xContext, "End      : " + getEndTime().toString(), verboseInd);
        logInfo(xContext, "DB mode  : " + dbMode, verboseInd);
        logInfo(xContext, "Verbose  : " + getVerboseInd(xContext), verboseInd);
        
	}

    
    /**
     * Display this application's usage and invocation
     */
    protected void showUsage() {

        StringBuffer usage = new StringBuffer();
        usage.append("------------------------------------------------------\n");
        usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
        usage.append("------------------------------------------------------\n");
        usage.append("Display build metrics.\n");
        usage.append("\n");
        usage.append("USAGE:\n");
        usage.append("------\n");
        usage.append(APP_NAME + " <-c component> [-s start] [-e end] \n");
        usage.append("           [-y] [-h] [-db dbMode]\n");
        usage.append("\n");
        usage.append("  component = A tool kit component name (ess, hdp, mar ...)\n");
        usage.append("  start     = (optional) Start of query range (default = 2012-01-01 00:00:00)\n");
        usage.append("  end       = (optional) End of query range (default = 2012-12-31 23:59:59)\n");
        usage.append("  -y        = (optional) Verbose mode (echo messages to screen)\n");
        usage.append("  dbMode    = (optional) DEV | PROD (defaults to PROD)\n");
        usage.append("  -h        = Help (shows this information)\n");
        usage.append("\n");
        usage.append("------------------------------------------------------\n");

        System.out.println(usage);

    }
    
    
    /**
     * Members.
     */
    private Timestamp tStartTms;
    private Timestamp tEndTms;
	private Report_build_stats xReport;
	private List<String> dates;
	private Hashtable<String, Date> startTimes;
	private Hashtable<String, Long> elapsedTimes;
	private Hashtable<String, Integer> startCount;
	private Hashtable<String, Integer> failCount;
	private Hashtable<String, Integer> successCount;
	private Hashtable<String, Long> aixRunTotal;
	private Hashtable<String, Long> linuxRunTotal;
	private Hashtable<String, Integer> aixStarts;
	private Hashtable<String, Integer> linuxStarts;
	private Hashtable<String, Integer> aixFails;
	private Hashtable<String, Integer> linuxFails;
	private Hashtable<String, Integer> aixSuccess;
	private Hashtable<String, Integer> linuxSuccess;


	private static boolean requestHelp = false;
    
    
    /**
     * Getters.
     */
    public Timestamp getStartTime() { return tStartTms; }
    public Timestamp getEndTime() { return tEndTms; }
    public Report_build_stats getReport() { return xReport; }
    public List<String> getDates() { return dates; }
    public static boolean getRequestHelp() { return requestHelp; }
    protected String getAppName() { return APP_NAME; }
	protected String getAppVersion() { return APP_VERSION; }

    
    /**
     * Setters.
     */
    private void setStartTime(Timestamp aStart) { tStartTms = aStart;  }
    private void setEndTime(Timestamp anEnd) { tEndTms = anEnd;  }


	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[]{TkUserRoleConstants.CCB_APPROVER};
	}
    
}
