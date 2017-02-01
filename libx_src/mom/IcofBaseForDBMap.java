/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *    FILE: IcofBaseMap.java
 *
 * CREATOR: Karen K. Witt
 *    DEPT: AW0V
 *    DATE: 02/04/2009
 *
 *-PURPOSE---------------------------------------------------------------------
 * IcofBaseForDBMap class definition file.  This class is a base class
 *   for "manager" classes -- those classes that manage lists of
 *   objects that are populated from db2 tables
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 02/04/2009 KKW  Initial coding.
 *=============================================================================
 * </pre>
 */
package com.ibm.stg.iipmds.icof.component.mom;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeMap;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;


public abstract class IcofBaseForDBMap extends IcofBaseMap implements Serializable {

    
    //--------------------------------------------------------------------------
    /**
     * Constructor -- Default comparator to ManagerFunctions.STRINGCOMPARE 
     * 
     * @param anAppContext  application context, includes log and db connection
     */   
    //--------------------------------------------------------------------------
    protected IcofBaseForDBMap(AppContext anAppContext) {
       super(anAppContext);

    }

    
    //--------------------------------------------------------------------------
    /**
     * Constructor 
     *  
     * @param anAppContext  application context, includes log and db connection
     * @param aComparator   comparator to be used for ordering the elements in
     *                      the member treeMap
     */   
    //--------------------------------------------------------------------------
    protected IcofBaseForDBMap(AppContext anAppContext, Comparator aComparator) {

        super(anAppContext, aComparator);
        
    }

    
    //--------------------------------------------------------------------------
    /**
     * Constructor 
     * 
     * @param anAppContext  application context, includes log and db connection
     * @param aTreeMap     treeMap to use to initialize the member TreeMap
     */   
    //--------------------------------------------------------------------------
    protected IcofBaseForDBMap(AppContext anAppContext
                               ,TreeMap aTreeMap) {
        
        super(anAppContext, aTreeMap);

    }


    //--------------------------------------------------------------------------
    /**
     * Member "getter" functions
     */
    //--------------------------------------------------------------------------


    //--------------------------------------------------------------------------
    /**
     * Data members
     */
    //--------------------------------------------------------------------------


    //--------------------------------------------------------------------------
    /**
     * Member "setter" functions
     */
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    /**
     * Required methods
     */
    //--------------------------------------------------------------------------


    //--------------------------------------------------------------------------
    /**
     * Populate the member treeMap with all records from the database
     * 
     * @param anAppContext includes log file and db connection
     * @throws IcofException
     */
    //--------------------------------------------------------------------------
    public abstract void populate(AppContext anAppContext) throws IcofException;


    //--------------------------------------------------------------------------
    /**
     * Populate the member treeMap with all "archived" (marked as deleted)
     * records from the database
     * 
     * @param anAppContext includes log file and db connection
     * @throws IcofException
     */
    //--------------------------------------------------------------------------
    public abstract void populateArchived(AppContext anAppContext) 
    throws IcofException;
}

//==========================  END OF FILE  ====================================
