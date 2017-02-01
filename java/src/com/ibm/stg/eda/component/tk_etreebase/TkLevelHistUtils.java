/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 06/08/2010
*
*-PURPOSE---------------------------------------------------------------------
* Class for EDA Tool Kit LEVELHIST utility methods.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 06/08/2010 GFS  Initial coding.
* 09/01/2011 GFS  Added getNewHeader(Timestamp aTms) method.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_etreebase;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofSystemUtil;

public class TkLevelHistUtils {
    
    /**
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

    
    /**
     * Construct a new header entry.
     * 
     * @returns      Collection of Strings where each String is a line of the 
     *               LEVELHIST file.
     */
    public static Vector<String>  getNewHeader() throws IcofException {
        
        return getNewHeader(null);
        
    }
    

    /**
     * Construct a new header entry.
     * 
     * @param aTms  Timestamp to use in the header or null in which case
     *              the current date/time is used. 
     * @returns     Collection of Strings where each String is a line of the 
     *              LEVELHIST file.
     */
    public static Vector<String> getNewHeader(Timestamp aTms) throws IcofException {
        
        // Create a StringBuffer to hold the header text.
        Vector<String> header = new Vector<String> ();
        
        // Construct the unix date/time string (Thu Sep  1 09:29:24 EDT 2011)
        // from the DB timestamp.
        Date tms = null;
        if (aTms == null)
        	tms = new Date();
        else 
        	tms = new Date(aTms.getTime());
        
        // Add the date.
        header.add("");
        header.add(COMMENT_LINE);
        header.add(tms.toString());
        header.add(COMMENT_LINE);
        header.add("");
        
        return header;
        
    }
    
    /**
     * Constants
     */
    public final static String COMMENT_LINE = "****************************";
    public final static int MAX_COMMENT_WIDTH = 62;

}