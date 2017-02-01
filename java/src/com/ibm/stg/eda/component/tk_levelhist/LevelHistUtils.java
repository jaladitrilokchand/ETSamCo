/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 12/23/2009
*
*-PURPOSE---------------------------------------------------------------------
* Class for EDA Tool Kit LEVELHIST utility methods.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 12/23/2009 GFS  Initial coding.
* 01/08/2010 GFS  Removed the "Level:" entry from the header.
* 01/27/2010 GFS  Added an extra blank line before the header in getNewHeader().
* 03/02/2010 GFS  Removed constructLevel() method.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_levelhist;

import java.util.Vector;

import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofSystemUtil;

public class LevelHistUtils {
    
    /*
     * Construct a new date entry for today.
     */
    public static String constructDate() throws IcofException {
        
        String command = "date";
        
        // Run the command.
        StringBuffer errorMsg = new StringBuffer();
        Vector<String> results = new Vector<String>();
        int rc = IcofSystemUtil.execSystemCommand(command, errorMsg, results);
        
        String date = "";
        if (rc == 0) {
            if (results.size() > 0) {
                date =  results.firstElement();
                date = date.trim();
            }
        }
        
        return date;
        
    }

    /*
     * Construct a new date entry for today.
     */
    public static String constructUpdateDateTime() throws IcofException {
        
        String command = "date +%m/%d/%y\" \"%H:%M";
        
        // Run the command.
        StringBuffer errorMsg = new StringBuffer();
        Vector<String> results = new Vector<String>();
        int rc = IcofSystemUtil.execSystemCommand(command, errorMsg, results);
        
        String date = "";
        if (rc == 0) {
            if (results.size() > 0) {
                date =  results.firstElement();
                date = date.trim();
            }
        }
        
        return date;
        
    }

    /*
     * Construct a new header entry.
     * 
     * @param target Target location of injection (shipb|tkb)
     * @returns      Collection of Strings where each String is a line of the 
     *               LEVELHIST file.
     */
    public static Vector<String> getNewHeader(String target) throws IcofException {
        
        // Create a StringBuffer to hold the header text.
        Vector<String> header = new Vector<String>();
        
        // Add the date.
        header.add("");
        header.add(COMMENT_LINE);
        header.add(LevelHist.constructDate() + " (" + target + " INJECTION)");
        header.add(COMMENT_LINE);
        header.add("");
        
        return header;
        
    }
    

    /*
     * Constants
     */
    //private final static String CLASS_NAME = "LevelHist";
    public final static String COMMENT_LINE = "****************************";
    public final static String LEVEL = "Level: ";

}