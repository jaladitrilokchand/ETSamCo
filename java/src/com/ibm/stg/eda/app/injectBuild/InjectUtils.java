/**
* <pre>
* =============================================================================
* 
*  IBM Internal Use Only
* 
* =============================================================================
* 
*     FILE: InjectUtils.java
* 
*  CREATOR: Gregg Stadtlander
* 
* -PURPOSE---------------------------------------------------------------------
* A utility class to hold the common constants and methods for the
* inject applications.
* -----------------------------------------------------------------------------
* 
* 
* -CHANGE LOG------------------------------------------------------------------
* 08/26/2010 GFS  Initial coding.
* =============================================================================
* </pre>
*/

package com.ibm.stg.eda.app.injectBuild;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Vector;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;

public class InjectUtils {

    // Public data members.
    public static final String sDATE_FORMAT_EXT = "dd/MM/yyyy HH:mm:ss:SSS";


    //------------------------------------------------------------------------
    /**
     * A convenient method to create a string representation of the
     * current time as in "dd/MM/yyyy HH:mm:ss:SSS" format.
     * 
     * @param     sFormat          Desired format.
     * @return                     Current timestamp formatted as string. 
     * @exception IcofException    Trouble creating the formatted string.
     */
    public static synchronized String getTimestamp(String sFormat) 
    throws IcofException {

        // Get current time and format it.
        long lTimestamp = System.currentTimeMillis();
        Date dDate = new Date(lTimestamp);

        SimpleDateFormat sdSimpleDate = new SimpleDateFormat(sFormat);
        sdSimpleDate.setTimeZone(TimeZone.getTimeZone("EST"));

        return sdSimpleDate.format(dDate).toString();

    }
 
   
    //------------------------------------------------------------------------
    /**
     * Creates a relative path string for a given file object by
     * subtracting the given root directory string. If the root
     * directory string is not part of file objects path, it will throw
     * an exception. The returned string starts with a "/".
     *
     * @param     xContext       AppContext object.
     * @param     xFile          IcofFile object.
     * @param     xRootToRemove  Directory string to remove.
     * @return                   New relative path as string.
     * @exception IcofException  Trouble creating the relative path.
     */
    public static synchronized String getRelativePath(AppContext xContext,
                                                      IcofFile xFile,
                                                      IcofFile xRootToRemove)
    throws IcofException {

        // Get remove directory name.
        String sRootToRemove = xRootToRemove.getDirectoryName();
        String sReturn = null;

        // Remove root directory. If it doesn't exist, throw an exception.
        if (xFile.getDirectoryName().indexOf(sRootToRemove) > -1) {
            sReturn 
            = xFile.getDirectoryName().substring(sRootToRemove.length())
            + IcofFile.separator + xFile.getName();
        } else {
            String msg = "Root directory is not part of given file's path.\n"
                         + "File = " + xFile.getFileName()
                         + "\nRoot directory = " + sRootToRemove + "\n";
            throw new IcofException(_sCLASS_NAME,
                                    "getRelativePath()",
                                    IcofException.SEVERE,
                                    msg, "");  
        }
            
        return sReturn;
        
    }

    
    //------------------------------------------------------------------------
    /**
     * Calculates the difference in specified times (milliseconds in
     * long) and converts to minutes. The return string is formatted as
     * \"#0.00\".
     *
     * @param     long           Start time.
     * @param     long           End time.
     * @return                   Time difference in minutes.
     * @exception IcofException  Trouble calculating the run time.
     */
    public static synchronized String getRunTime(long lStart, long lEnd) 
    throws IcofException {

        // Calculate the difference in minutes as double.
        double dResult = (double) (lEnd-lStart)/(1000 * 60);

        // Define the format.
        DecimalFormat df = new DecimalFormat("#0.00");

        // Format the result.
        return df.format(dResult);

    }

    
    //-------------------------------------------------------------------------
    /**
    * Given a TreeMap were the key is a String and the value is a Vector.  This 
    * method will add new values for the specified key.
    *
    * @param treeMapCollection TreeMap to update.
    * @param key               Key string.
    * @param newValues         Vector of new values.
    * 
    * @exception IcofException  Trouble reading content files.
    */
    public static void addValuesToTreeMap(TreeMap<String,Vector<String>> treeMapCollection, String key, 
                                           Vector<String>  newValues) 
    throws IcofException {

        Vector<String>  values = null;
        if (treeMapCollection.containsKey(key)) {
            values =  treeMapCollection.get(key);
            values.addAll(newValues);
        }
        else {
            values = new Vector<String>();
            values.addAll(newValues);
        }
        treeMapCollection.put(key, values);
        
    }


    //-------------------------------------------------------------------------
    /**
    * Given a TreeMap were the key is a String and the value is a Vector.  This 
    * method will add a new value for the specified key.
    *
    * @param treeMapCollection  TreeMap to update.
    * @param key                Key string.
    * @param vNewValues         Vector of new values.
    * 
    * @exception IcofException  Trouble reading content files.
    */
    public static void addValueToTreeMap(TreeMap<String,Vector<String>> treeMapCollection, String key, 
                                          String newValue) 
    throws IcofException {

        Vector<String>  values = null;
        if (treeMapCollection.containsKey(key)) {
            values =  treeMapCollection.get(key);
            values.add(newValue);
        }
        else {
            values = new Vector<String> ();
            values.add(newValue);
        }
        
        treeMapCollection.put(key, values);
        
    }
    
    
    //-------------------------------------------------------------------------
    /**
     * Get the the user's email address from the userid.
     * @param   sUserid  User's name.
     * 
     * @exception IcofException  Trouble setting email address.
     */
    public static String getUsersAddress(String sUserid) throws IcofException {

        // Convert the user's id to an email address.
        String sName = sUserid;
        if (sUserid.equals("cwhite")) {
            sName = "cbwhite";
        }
        else if (sUserid.equals("paulg")) {
            sName = "pgale";
        }

        return sName + Constants.AT_SIGN + "us.ibm.com";

    }

    
    //------------------------------------------------------------------------
    /**
     * For each directory in the input collection of directory objects remove
     * any empty directories between the specified directory and the topLevelDir.
     * 
     * @param directories        List of ccFile objects (directories) to remove. 
     * @exception IcofException  Trouble removing directories.
     */
    public static void removeEmptyDirs(Vector<String> directories, IcofFile topLevelDir)
    throws IcofException, Exception {

        // Remove directories only if they contain no files or
        // sub directories.
        Iterator<String> iter = directories.iterator();
        while (iter.hasNext()) {
            String dirName =  iter.next();
            IcofFile dir = new IcofFile(dirName, true);

            if (dir.exists()) {
                dir.removeEmptyDirectories(topLevelDir);
            }
            
        }
        
    }

    // Private members
    private static final String _sCLASS_NAME = "StagingUtils";


}
