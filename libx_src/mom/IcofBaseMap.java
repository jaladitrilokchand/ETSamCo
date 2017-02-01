/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2009 - 2011 -- IBM Internal Use Only
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
 * IcofBaseMap class definition file.  This class is a base class
 *   for "manager" classes -- those classes that manage lists of
 *   objects
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 02/04/2009 KKW  Initial coding.
 * 05/02/2011 KKW  Removed unused imports
 *=============================================================================
 * </pre>
 */
package com.ibm.stg.iipmds.icof.component.mom;
import java.io.Serializable;
import java.util.Comparator;
import java.util.TreeMap;

import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;


public abstract class IcofBaseMap implements Serializable {

    
    //--------------------------------------------------------------------------
    /**
     * Constructor -- Default comparator to ManagerFunctions.STRINGCOMPARE 
     * 
     * @param anAppContext  application context, includes log and db connection
     */   
    //--------------------------------------------------------------------------
    protected IcofBaseMap(AppContext anAppContext) {
       this(anAppContext
            ,ManagerFunctions.STRINGCOMPARE);

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
    protected IcofBaseMap(AppContext anAppContext, Comparator aComparator) {

        setComparator(anAppContext, aComparator);
        setTreeMap(anAppContext, new TreeMap(aComparator));
    }

    
    //--------------------------------------------------------------------------
    /**
     * Constructor 
     * 
     * @param anAppContext  application context, includes log and db connection
     * @param aTreeMap     treeMap to use to initialize the member TreeMap
     */   
    //--------------------------------------------------------------------------
    protected IcofBaseMap(AppContext anAppContext
                          ,TreeMap aTreeMap) {
        
        setTreeMap(anAppContext, aTreeMap);
        setComparator(anAppContext, aTreeMap.comparator());


    }


    //--------------------------------------------------------------------------
    /**
     * Member "getter" functions
     */
    //--------------------------------------------------------------------------
    protected TreeMap getTreeMap(AppContext anAppContext) { return treeMap; }
    public Comparator getComparator(AppContext anAppContext) { return comparator; }


    //--------------------------------------------------------------------------
    /**
     * Data members
     */
    //--------------------------------------------------------------------------
    protected TreeMap   treeMap;
    protected Comparator comparator;


    //--------------------------------------------------------------------------
    /**
     * Member "setter" functions
     */
    //--------------------------------------------------------------------------
    protected void setTreeMap(AppContext anAppContext, TreeMap aTreeMap) {
        treeMap = new TreeMap(aTreeMap);
    }

    protected void setComparator(AppContext anAppContext, Comparator aComparator) {
        comparator = aComparator;
    }


    //--------------------------------------------------------------------------
    /**
     * Required methods
     */
    //--------------------------------------------------------------------------
}

//==========================  END OF FILE  ====================================
