/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 12/10/2009
*
*-PURPOSE---------------------------------------------------------------------
* ToolKit injection utility class.
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 12/10/2009 GFS  Initial coding.
* 01/27/2010 GFS  Added getToolName() and updated getTopLevelDirectory() to use
*                 that method to correctly create the directory.
* 02/16/2010 GFS  Updated algorithm in getFullLocationPath() method.  Added
*                 "CMVC - extract track" constant and changed CMVC to 
*                 "CMVC - extract file"
* 04/05/2010 GFS  Added getModifyString() method.
* 03/15/2012 GFS  Updated getQueryString() to insert the userid, password and cq
*                 record into the query instead of relying on replaceAll which
*                 will not process special chars ($) in the userid/password
*                 correctly.
* 04/09/2012 GFS  Updated to support xtinct injects.
* 11/05/2013 GFS  Added  getPatchQueryString() method.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_patch;

import java.io.File;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofSystemUtil;

public class TkInjectUtils {

    
    /*
     * Constants.
     */
    public static String CLASS_NAME = "TkInjectUtils";
    public static String CMVC_FILE_EXT = "CMVC - extract file";
    public static String CMVC_TRACK_EXT = "CMVC - extract track";
    public static String OTHER = "OTHER";
    public static String LEVEL_HIST = "LEVELHIST";
    public static String AFS_EDA = "/afs/eda/";
    public static String BUILD_DIR = AFS_EDA + "build/";
    public static String DEV_DIR = AFS_EDA + "dev/";
    public static String PROD_DIR = AFS_EDA + "prod/";
    public static String SHIPB_DIR = AFS_EDA + "shipb/";
    public static String TKB_DIR = AFS_EDA + "tkb/";
    public static String RSH = "/usr/afsws/bin/rsh";
    public static String SSH = "ssh";
    public static String FORK_IT_CMD = "/afs/eda/u/einslib/bin/ET_forkit -c ";
    
    
    /**
     * Get the CQ web service query to fetch TK Patch data.
     * 
     * @param sUserid   Intranet user id
     * @param sPassword Intranet password
     * @param sRecord   CQ record to query 
     */
    public static String getQueryString(String sUserid, String sPassword,
                                        String sRecord) {
        StringBuffer query = new StringBuffer();
        
        query.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> ");
        query.append("<CqQuery> ");
        query.append("<login> ");
        query.append("<username>" + sUserid + "</username> ");
        query.append("<password>" + sPassword + "</password> ");
        query.append("<database>MDCMS</database> ");
        query.append("<schema>mdcms</schema> ");
        query.append("</login> ");
        query.append("<client> ");
        query.append("<app-name>injectBuild</app-name> ");
        query.append("</client> ");
        query.append("<query> ");
        query.append("<query-name>Public Queries/EDA Injections and Patches/Injection Patches and Requests Filter (ID)</query-name> ");
        query.append("<filters> ");
        query.append("<filter name=\"id\" operator=\"Equals\" value=\"" 
                     + sRecord + "\" /> ");
        query.append("</filters> ");
        query.append("</query> ");
        query.append("</CqQuery>");

        return query.toString();
        
    }

    
    /**
     * Get the CQ web service query to Transmit Read Patches.
     * 
     * @param sUserid   Intranet id
     * @param sPassword Intranet id's password
     */
    public static String getPatchQueryString(String sUserid, String sPassword) {
        StringBuffer query = new StringBuffer();
        
        query.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> ");
        query.append("<CqQuery> ");
        query.append("<login> ");
        query.append("<username>" + sUserid + "</username> ");
        query.append("<password>" + sPassword + "</password> ");
        query.append("<database>MDCMS</database> ");
        query.append("<schema>mdcms</schema> ");
        query.append("</login> ");
        query.append("<client> ");
        query.append("<app-name>injectBuild</app-name> ");
        query.append("</client> ");
        query.append("<query> ");
        query.append("<query-name>Public Queries/EDA Injections and Patches/Injection Patches in Process</query-name> ");
        query.append("</query> ");
        query.append("</CqQuery>");
        
        return query.toString();
        
    }

    /**
     * Get the CQ web service query to Transmit Read Patches.
     * 
     * @param sUserid     Intranet id
     * @param sPassword   Intranet id's password
     * @param sQueryName  Name of the CQ inject request query to run
     */
    public static String getInjectQueryString(String sUserid, 
                                              String sPassword,
                                              String sQueryName) {
        StringBuffer query = new StringBuffer();
        
        query.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> ");
        query.append("<CqQuery> ");
        query.append("<login> ");
        query.append("<username>" + sUserid + "</username> ");
        query.append("<password>" + sPassword + "</password> ");
        query.append("<database>MDCMS</database> ");
        query.append("<schema>mdcms</schema> ");
        query.append("</login> ");
        query.append("<client> ");
        query.append("<app-name>injectBuild</app-name> ");
        query.append("</client> ");
        query.append("<query> ");
        query.append("<query-name>Public Queries/EDA Injections and Patches/" +
                     sQueryName + "</query-name> ");
        query.append("</query> ");
        query.append("</CqQuery>");
        
        return query.toString();
        
    }
    
    
    /**
     * Get the CQ web service query to fetch TK Patch data.
     */
    public static String getModifyString() {
        StringBuffer update = new StringBuffer();
        
        update.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        update.append("<CqUpdate>");
        update.append("<login>");
        update.append("<username>##USERID##</username> ");
        update.append("<password>##PASSWORD##</password> ");
        update.append("<database>MDCMS</database>");
        update.append("<schema>mdcms</schema>");
        update.append("</login>");
        update.append("<client>");
        update.append("<app-name>injectBuild</app-name>");
        update.append("</client>");
        update.append("<update>");
        update.append("<record_type>tk_patch</record_type>");
        update.append("<record_id>##RECORD##</record_id>");
        update.append("<action>BuildComplete</action>");
        update.append("</update>");
        update.append("</CqUpdate>");
        
        return update.toString();
        
    }
    
    
    /**
     * Get the CQ web service query to fetch TK Patch data.
     */
    public static String getUpdateString() {
	
	return getUpdateString("tk_patch");
        
    }
    
    
    /**
     * Get the CQ web service query to fetch TK Patch data.
     */
    public static String getUpdateString(String recordType) {
        StringBuffer update = new StringBuffer();
        
        String application = "injectBuild";
        if (recordType.equals("tk_injectionrequest"))
            application = "parseCQ";
        
        update.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        update.append("<CqUpdate>");
        update.append("<login>");
        update.append("<username>##USERID##</username> ");
        update.append("<password>##PASSWORD##</password> ");
        update.append("<database>MDCMS</database>");
        update.append("<schema>mdcms</schema>");
        update.append("</login>");
        update.append("<client>");
        update.append("<app-name>" + application + "</app-name>");
        update.append("</client>");
        update.append("<update>");
        update.append("<record_type>" + recordType + "</record_type>");
        update.append("<record_id>##RECORD##</record_id>");
        update.append("<action>##ACTION##</action>");
        update.append("</update>");
        update.append("</CqUpdate>");
        
        return update.toString();
        
    }
    

    /**
     * Get the CQ web service query to fetch TK Patch data.
     */
    public static String getModifyString2() {
        StringBuffer update = new StringBuffer();
        
        update.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        update.append("<CqUpdate>");
        update.append("<login>");
        update.append("<username>##USERID##</username> ");
        update.append("<password>##PASSWORD##</password> ");
        update.append("<database>MDCMS</database>");
        update.append("<schema>mdcms</schema>");
        update.append("</login>");
        update.append("<client>");
        update.append("<app-name>injectBuild</app-name>");
        update.append("</client>");
        update.append("<update>");
        update.append("<record_type>tk_patch</record_type>");
        update.append("<record_id>##RECORD##</record_id>");
        update.append("<action>##ACTION##</action>");
        update.append("</update>");
        update.append("</CqUpdate>");
        
        return update.toString();
        
    }


    /**
     * Determine if the file can be found at the location.
     * 
     * @param dirFile   Directory/fileName
     * @param location  Path to a directory or file
     * @return True if fileName can be found at location.
     */
    public static String getFullLocationPath(String dirFile, String location) {

        // Return null if inputs are empty.
        if (dirFile.equals("") || location.equals("")) {
            return null; 
        }
        
        // Return location if location is CMVC.
        if (location.equals(TkInjectUtils.CMVC_FILE_EXT) || 
            location.equals(TkInjectUtils.CMVC_TRACK_EXT)) {
            return location;
        }
        
        // It the source file is not in CMVC then it is in build, dev, prod,
        // shipb or a sandbox. If build, dev, prod or shipb then 
        // location + dirFile = fullPath and  must exist.
        // If a sandbox then there are 3 possibilities:
        //  1. location + dirFile = full path
        //  2. location = full path
        //  3. location + (dirFile - dir) = full path
        // In each case full path must exist and must contain file.

        // Set path name of the location as a directory.
        String fileName = dirFile.substring(dirFile.lastIndexOf("/") + 1);
        
        // Test for condition #1.
        File temp;
        String tempName = location + File.separator + dirFile;
        temp = new File(tempName);
        if (temp.exists() && temp.isFile() && temp.getPath().indexOf(fileName) > -1) {
            return temp.getAbsolutePath();
        }

        // Test for condition #2.
        tempName = location;
        temp = new File(tempName);
        if (temp.exists() && temp.isFile() && temp.getPath().indexOf(fileName) > -1) {
            return temp.getAbsolutePath();
        }

        // Test for condition #3.
        tempName = location + File.separator + fileName;
        temp = new File(tempName);
        if (temp.exists() && temp.isFile() && temp.getPath().indexOf(fileName) > -1) {
            return temp.getAbsolutePath();
        }

        return null;
        
    }
 

    /**
     * Convert a TK release (17.1.8) to the TK version (13.1).
     * 
     *  @param tkRelease  Tool Kit release name (17.1.8)
     *  @return tkVersion Tool Kit version name (13.1)
     */
    public static String getTkVersion(String tkRelease) {

        // Parse the TK release.
        Vector<String> tokens = new Vector<String>(); 
        try {
            IcofCollectionsUtil.parseString(tkRelease, ".", tokens, true);
        } catch(IcofException trap) {}
        
        String tkVer = "";
        if (tokens.size() > 2) {
        	String sVer = (String) tokens.get(0);
        	int version = Integer.parseInt(sVer);
            if (version > 13)
            	version -= 4;	
            tkVer = version + "." + (String) tokens.get(1);
        }
            
        return tkVer;
        
    }   


    /**
     * Convert a TK release (17.1.8) to the CMVC version (1301).
     * 
     *  @param tkRelease  Tool Kit release name (17.1.8)
     *  @param delimiter  Char between major and minor release.
     *  @return           CMVC version name (1301 or 13.1)
     */
    public static String getCmvcVersion(String tkRelease, String delimiter) {

        // Parse the TK release.
        Vector<String> tokens = new Vector<String>(); 
        try {
            IcofCollectionsUtil.parseString(tkRelease, ".", tokens, true);
        } catch(IcofException trap) {}
        
        String cmvcVer = "";
        if (tokens.size() > 2) {
            String sVer = (String) tokens.get(0);
            int version = Integer.parseInt(sVer);
            if (version > 13)
            	version -= 4;	
            cmvcVer = String.valueOf(version) + delimiter + (String) tokens.get(1);
        }
            
        return cmvcVer;
        
    }   

    /**
     * Construct the tool name from the Patch component name.
     * 
     *  @param patchComponent  TK patch component name (einstimer, hdp ...)
     *  @return                Tool name
     */
    public static String getToolName(String patchComponent) {

        // Convert any *.tcl[z] components to *.
        String toolName = patchComponent;
        int index = patchComponent.indexOf("_tcl[z]");
        if (index > -1) {
            toolName = patchComponent.substring(0, index);
        }
            
        return toolName;
        
    }
    
 
    /**
     * Determine the best machine for the specified platform.
     * 
     * @param platform
     * @throws IcofException 
     */
    public static String getBestMachine(String platform) throws IcofException {
        
        // Construct command.
        String command = "/afs/eda/u/cmlib/bin/CM_getbestmach -sp " + platform +
                         " -m 2.0 -c 15 -l";
        
        // Run the command.
        StringBuffer errorMsg = new StringBuffer();
        Vector<String> results = new Vector<String>();
        System.out.println("Running: " + command);
        int rc = IcofSystemUtil.execSystemCommand(command, errorMsg, results);
        System.out.println("Return code: " + rc);
        System.out.println("Result count: " + results.size());
        System.out.println("Error msg: " + errorMsg.toString());
        
        // Check the return code.
        if (rc != 0) {
            throw new IcofException("TkInjectUtils", "getBestMachine()", 
                                    IcofException.SEVERE, 
                                    "Error: unable to determine best machine",
                                    "Message: " + errorMsg.toString());
        }
    
        // Parse the results to determine the best machine.
        String bestMachine = "";
        String bestTime = "100";
        Iterator<String> entries = results.iterator();
        while (entries.hasNext()) {
            String entry = entries.next();
            System.out.println("Entry: " + entry);
            Vector<String> tokens = new Vector<String>();
            IcofCollectionsUtil.parseString(entry, ":", tokens, true);
            if (tokens.size() > 2) {
                String myMachine = (String) tokens.get(0);
                String myUptime = (String) tokens.get(1);
                System.out.println("Machine: " + myMachine);
                System.out.println("Uptime: " + myUptime + "\n");
                if (! myUptime.equals("0.00")) {
                    if (Integer.parseInt(myUptime) < Integer.parseInt(bestTime)) {
                        bestMachine = myMachine;
                        bestTime = myUptime;
                    }
                }
            }
        }

        return bestMachine;
        
    }

    
    /**
     * Construct a log file object.
     * 
     * @param patchId     The TK patch id
     * @param component   The TK component (tool) name
     * @param descriptor  A descriptor if needed.
     * @param addLogExtension  If true adds .log
     * @throws IcofException 
     */
    public static String getLogFileName(String patchId, String component, 
                                        String descriptor, boolean addLogExtenstion) {

        // Get the user id.
        String userid = System.getProperty("user.name");
        
        // Get the current date.
        String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul",
                           "aug", "sep", "oct", "nov", "dec"}; 
        Calendar cal = Calendar.getInstance();
        String month = months[cal.get(Calendar.MONTH)];
        int day = cal.get(Calendar.DAY_OF_MONTH); 
        
        // Construct the log file name
        String logName = month + day + "." + "injectBuild" 
                         + "." + component + "." + patchId;
        if ((descriptor != null) && (! descriptor.equals(""))) {
            logName += "_" + descriptor;
        }
        if (addLogExtenstion)
            logName += ".log";

        // Construct the log directory.
        StringBuffer dirName = new StringBuffer("/afs/eda/u/" + userid);
        dirName.append(IcofFile.separator + "logs");
        dirName.append(IcofFile.separator + month);
        IcofFile logDir = new IcofFile(dirName.toString(), true);
        try {
            logDir.validate(true);
        }
        catch(IcofException trap) {}
        
        // Construct the full log file name.
        String fileName = logDir.getAbsolutePath() + IcofFile.separator + logName;
        
        return fileName;
        
    }

    /**
     * Construct a log file object.
     * 
     * @param patchId     The TK patch id
     * @param component   The TK component (tool) name
     * @param descriptor  A descriptor if needed.
     * @param addLogExtension  If true adds .log
     * @throws IcofException 
     */
    public static String getOptionsFileName(String component) {

        // Get the user id.
        String userid = System.getProperty("user.name");
        
        // Construct the log file name
        String fileName = component + ".options";

        // Construct the log directory.
        StringBuffer dirName = new StringBuffer("/afs/eda/u/" + userid 
                                                + "/.injectBuild");
        IcofFile logDir = new IcofFile(dirName.toString(), true);
        try {
            logDir.validate(true);
        }
        catch(IcofException trap) {}
        
        // Construct the full log file name.
        return logDir.getAbsolutePath() + IcofFile.separator + fileName;
        
    }

    
}


