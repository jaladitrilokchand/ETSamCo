/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2011 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *    FILE: IcofFileUtil.java
 *
 * CREATOR: Karen K. Witt
 *    DEPT: AW0V
 *    DATE: 05/16/2011
 *
 *-PURPOSE---------------------------------------------------------------------
 * IcofFileUtil class definition file.  A set of static methods
 *   that operate on IcofFiles or on collections of IcofFiles
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 05/16/2011 KKW  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.common;
import java.util.Vector;


public class IcofFileUtil {


    //--------------------------------------------------------------------------
    /**
     * Calculate the total size of files/directories represented by the 
     * collection of input IcofFile objects.
     *
     * @param  filesOrDirs          Collection of files/directories
     * @return                      the size in bytes.
     */
    //--------------------------------------------------------------------------
    public static synchronized double getTotalSize(Vector<IcofFile> filesOrDirs) 
    throws IcofException {
        
        double totalSize = 0.0;
        for (int i = 0; i < filesOrDirs.size(); i++) {
            IcofFile thisFileOrDir = filesOrDirs.elementAt(i);
            double usedBytes = thisFileOrDir.getUsedBytes();
            System.out.println(thisFileOrDir.getAbsolutePath() + "  " + (long) usedBytes);
            totalSize += usedBytes;
        }
        return totalSize;

    }

    //-----------------------------------------------------------------------------
    // Data elements.
    //-----------------------------------------------------------------------------
    private static final String CLASS_NAME = "IcofFileUtil";


}


//==========================  END OF FILE  ====================================
