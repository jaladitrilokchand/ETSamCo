/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 06/10/2010
*
*-PURPOSE---------------------------------------------------------------------
* EDA TK constants class.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 06/10/2010 GFS  Initial coding.
* 07/13/2010 GFS  Added support for the development and prod log directories.
* 01/16/2011 GFS  Added TRUNK and STAGE_* constants.
* 03/03/2011 GFS  Added SVN constants.
* 03/10/2011 GFS  Added GDEV and GPROD constants,
* 04/10/2011 GFS  Added EDA_TEST and EDA-GTEST constants.
* 05/02/2011 GFS  Added location constants - LOC_*.  Added some event constants.
* 05/10/2011 GFS  Added BDEV, BTEST and BPROD constants.
* 05/11/2011 GFS  Changed ENABLEMENT_DIR, ADMIN_DIR and USER_DIR to new 
*                 /afs/eda/data/edainfra location.
* 09/19/2011 GFS  Added EVENT_* constants.               
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreebase;

import java.io.File;
import java.util.Date;
import java.util.regex.Pattern;

import com.ibm.stg.iipmds.common.IcofDateUtil;

public class TkConstants {

    /**
     * Constants.
     */
    public static final String EDA_DEV = "DEV";
    public static final String EDA_TEST = "TEST";
    public static final String EDA_PROD = "PROD";
    
    public static final String EDA_GDEV = "GDEV";
    public static final String EDA_GTEST = "GTEST";
    public static final String EDA_GPROD = "GPROD";
    
    public static final String INTIAL_VERSION = "0";

    // Log constants
    public static final String ETREE_DIR = "/afs/eda/data/edainfra";
    public static final String ENABLEMENT_DIR = 
        "/afs/eda/data/edainfra/tools/enablement";
    public static final String USER_DIR = 
        "/afs/eda/data/edainfra/tools/user";
    public static final String ADMIN_DIR = 
        "/afs/eda/data/edainfra/tools/admin";
    public static final String LOG_EXT = ".log";
    public static final String LOGS_DIR = "logs";
    
    // Status constants
    public static final String STATUS_OK = "OK";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_START_AUTO = "START_AUTO";
    public static final String STATUS_START_MANUAL = "START_MANUAL";
    public static final String STATUS_START = "START";

    // Return code constants
    public static final int NOTHING_TO_DO = 2;

    // Text constants
    public static final String DB_ID = "DB_ID";
    public static final String CQ_DUMMY = "MDCMS????????";
    public static final String LH_DELIM = "  ";
    public static final String EXTRACTED = "EXTRACTED";
    public static final String ADVANCED_TO = "ADVANCED_TO_";
    
    // SVN constants
    public static final String TAGS = "tags";
    public static final String TRUNK = "trunk";
    public static final String BRANCHES = "branches";
    
    // ToolKit Stage Names
    public static final String STAGE_NEW = "NEW";
    public static final String STAGE_READY = "READY";
    public static final String STAGE_DEV = "DEVELOPMENT";
    public static final String STAGE_SHIP = "SHIP";
    public static final String STAGE_TK = "TK";
    public static final String STAGE_XTINCT = "XTINCT";
    public static final String STAGE_OBSOLETE = "OBSOLETE";

    // ToolKit Locations
    public static final String LOC_BUILD = "build";
    public static final String LOC_DEV = "dev";
    public static final String LOC_PROD = "prod";
    public static final String LOC_SHIPB = "shipb";
    public static final String LOC_SHIP = "ship";
    public static final String LOC_TKB = "tkb";
    public static final String LOC_TK = "tl";

    // Events
    public static final String EVENT_EXTRACTED = "EXTRACTED";
    public static final String EVENT_BUILD_OK = "BUILD_SUCCESS";
    public static final String EVENT_ADV_TO_DEV = "ADVANCED_TO_DEV";
    public static final String EVENT_ADV_TO_PROD = "ADVANCED_TO_PROD";
    public static final String EVENT_ADV_TO_SHIPB = "ADVANCED_TO_SHIPB";
    public static final String EVENT_ADV_TO_SHIP = "ADVANCED_TO_SHIP";
    public static final String EVENT_ADV_TO_TKB = "ADVANCED_TO_TKB";
    public static final String EVENT_ADV_TO_TK = "ADVANCED_TO_TK";
    

    // Misc
    public static final int MAX_CQ_SIZE = 13;
    
    /**
     * Set the log file name.
     * 
     * @param anAppMode  The application mode
     */
    public static String createEnablementLogDir(String anAppMode) {
        return createLogDir(ENABLEMENT_DIR, anAppMode);
    }
    public static String createAdminLogDir(String anAppMode) {
        return createLogDir(ADMIN_DIR, anAppMode);
    }
    public static String createUserLogDir(String anAppMode) {
        return createLogDir(USER_DIR, anAppMode);
    }
    public static String createEtreeLogDir(String anAppMode) {
        return createLogDir(ETREE_DIR, anAppMode);
    }
    public static String createLogDir(String location, String anAppMode) {
        return location + File.separator + LOGS_DIR + File.separator + 
               anAppMode.toLowerCase();
    }
   

    /**
     * Set the log file name.
     * 
     * @param appName  The application name
     * @param userId   The AFS userid
     */
    public static String createLogName(String appName, String userId) {

        String file =  appName;
        
        Date now = new Date();
        file += "." + 
                IcofDateUtil.formatDate(now, 
                                        IcofDateUtil.ARCHIVE_FILE_DATE_FORMAT) +
                "." + userId + LOG_EXT;    
        
        return file;

    }

    
    /**
     * A String matching method to determine if a String contains letters 
     **/
    public static final Pattern alpha = Pattern.compile("[A-Za-z]+");
    public static boolean containsAlphabet(String s) {
    	return alpha.matcher(s).find();
    } 
    

}
