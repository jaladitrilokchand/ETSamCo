/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2000 - 2009 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *    FILE: IcofObject.java
 *
 * CREATOR: Karen K. Kellam
 *    DEPT: 5ZIA
 *    DATE: 10/15/2001
 *
 *-PURPOSE---------------------------------------------------------------------
 * IcofObject class definition file.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 10/15/2001 KKK  Initial coding.
 * 05/25/2005 KKW  Added "implements Serializable".
 * 06/30/2006 KPL  Added AppContext as first param in all methods
 * 10/06/2006 KKW  Moved audit info into this class (merged the old
 *                 IcofReadOnlyObject and IcofUpdateObject into this class)
 * 01/16/2009 KKW  Added get/set for archivedBy and archivedTmstmp                 
 *=============================================================================
 * </pre>
 */
package com.ibm.stg.iipmds.icof.component.mom;
import java.io.Serializable;
import java.util.Date;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;


public abstract class IcofObject implements Serializable {

    
    //--------------------------------------------------------------------------
    /**
     * * Constructor -- for new style objects whose audit information is kept
     *   in a separate history table, with the exception of "marked as deleted"
     *   information
     *   
     *   @param  anArchivedBy  userid that marked the object as deleted
     *   @param  anArchivedTimestamp  date/time when object was marked as 
     *                                deleted
     */   
    //--------------------------------------------------------------------------
    protected IcofObject(AppContext anAppContext
                         ,String anArchivedBy
                         ,Date anArchivedTimestamp) {
        this(anAppContext
            ,""
             ,Constants.NULL_DATE
             ,anArchivedBy
             ,anArchivedTimestamp);

    }


    //--------------------------------------------------------------------------
    // Constructor -- for "read only" objects
    //--------------------------------------------------------------------------
    protected IcofObject(AppContext anAppContext
                         ,String aCreatedBy
                         ,Date aCreationTimestamp
                         ,String aDeletedBy
                         ,Date aDeletionTimestamp) {
        this(anAppContext
             ,aCreatedBy
             ,aCreationTimestamp
             ,""
             ,Constants.NULL_DATE
             ,aDeletedBy
             ,aDeletionTimestamp);

    }

    //-----------------------------------------------------------------------------
    // Constructor
    //-----------------------------------------------------------------------------
    protected IcofObject(AppContext anAppContext
                         ,String aCreatedBy
                         ,Date aCreationTimestamp
                         ,String anUpdatedBy
                         ,Date anUpdatedTimestamp
                         ,String aDeletedBy
                         ,Date aDeletionTimestamp) {
        setCreatedBy(anAppContext, aCreatedBy);
        setCreationTimestamp(anAppContext, aCreationTimestamp);
        setUpdatedBy(anAppContext, anUpdatedBy);
        setUpdatedTimestamp(anAppContext, anUpdatedTimestamp);
        setDeletedBy(anAppContext, aDeletedBy);
        setDeletionTimestamp(anAppContext, aDeletionTimestamp);

    }


    //-----------------------------------------------------------------------------
    // Constructor -- used when child classes want to initialize audit info
    //   to default values.
    //-----------------------------------------------------------------------------
    protected IcofObject(AppContext anAppContext) {

        this(anAppContext
             ,""
             ,Constants.NULL_DATE
             ,""
             ,Constants.NULL_DATE
             ,""
             ,Constants.NULL_DATE);

    }


    //-----------------------------------------------------------------------------
    // Member "getter" functions
    //-----------------------------------------------------------------------------
    public String getCreatedBy(AppContext anAppContext) { return createdBy; }
    public Date getCreationTimestamp(AppContext anAppContext) { return creationTimestamp; }
    public String getUpdatedBy(AppContext anAppContext) { return updatedBy; }
    public Date getUpdatedTimestamp(AppContext anAppContext) { return updatedTimestamp; }
    public String getDeletedBy(AppContext anAppContext) { return deletedBy; }
    public Date getDeletionTimestamp(AppContext anAppContext) { return deletionTimestamp; }
    public String getArchivedBy(AppContext anAppContext) { return getDeletedBy(anAppContext); }
    public Date getArchivedTimestamp(AppContext anAppContext) { 
        return getDeletionTimestamp(anAppContext); 
    }
    public String getMapKey(AppContext anAppContext) { return mapKey; }


    //-----------------------------------------------------------------------------
    // Data members
    //-----------------------------------------------------------------------------
    private String   createdBy;
    private Date     creationTimestamp;
    private String   updatedBy;
    private Date     updatedTimestamp;
    private String   deletedBy;
    private Date     deletionTimestamp;
    private String   mapKey;
    private String   functionName;


    //-----------------------------------------------------------------------------
    // Member "setter" functions
    //-----------------------------------------------------------------------------


    //-----------------------------------------------------------------------------
    /**
     * Set the CreatedBy name.
     * 
     * @param     anAppContext      the application context 
     * @param     aUserid           the userid of the user that created the object 
     */
    //-----------------------------------------------------------------------------
    protected void setCreatedBy(AppContext anAppContext, String aUserid) {
        createdBy = aUserid;
    }


    //-----------------------------------------------------------------------------
    /**
     * Set the CreationTimestamp.
     * 
     * @param     anAppContext      the application context 
     * @param     aTimestamp        when the object was created 
     */
    //-----------------------------------------------------------------------------
    protected void setCreationTimestamp(AppContext anAppContext, Date aTimestamp) {
        creationTimestamp = aTimestamp;
    }
    //-----------------------------------------------------------------------------
    /**
     * Set the UpdatedBy name.
     * 
     * @param     anAppContext      the application context 
     * @param     aUserid           the userid of the user that updated the object 
     */
    //-----------------------------------------------------------------------------
    protected void setUpdatedBy(AppContext anAppContext, String aUserid) {
        updatedBy = aUserid;
    }


    //-----------------------------------------------------------------------------
    /**
     * Set the UpdatedTimestamp.
     * 
     * @param     anAppContext      the application context 
     * @param     aTimestamp        when the object was updated
     */
    //-----------------------------------------------------------------------------
    protected void setUpdatedTimestamp(AppContext anAppContext, Date aTimestamp) {
        updatedTimestamp = aTimestamp;
    }

    //-----------------------------------------------------------------------------
    /**
     * Set the DeletedBy name.
     * 
     * @param     anAppContext      the application context 
     * @param     aUserid           the userid of the user that deleted the object 
     */
    //-----------------------------------------------------------------------------
    protected void setDeletedBy(AppContext anAppContext, String aUserid) {
        deletedBy = aUserid;
    }


    //-----------------------------------------------------------------------------
    /**
     * Set the DeletionTimestamp.
     * 
     * @param     anAppContext      the application context 
     * @param     aTimestamp        when the object was deleted 
     */
    //-----------------------------------------------------------------------------
    protected void setDeletionTimestamp(AppContext anAppContext, Date aTimestamp) {
        deletionTimestamp = aTimestamp;
    }

    //-----------------------------------------------------------------------------
    /**
     * Set the ArchivedBy name.
     * 
     * @param     anAppContext      the application context 
     * @param     aUserid           the userid of the user that deleted the object 
     */
    //-----------------------------------------------------------------------------
    protected void setArchivedBy(AppContext anAppContext, String aUserid) {
        setDeletedBy(anAppContext, aUserid);
    }


    //-----------------------------------------------------------------------------
    /**
     * Set the ArchivedTimestamp.
     * 
     * @param     anAppContext      the application context 
     * @param     aTimestamp        when the object was deleted 
     */
    //-----------------------------------------------------------------------------
    protected void setArchivedTimestamp(AppContext anAppContext, Date aTimestamp) {
        setDeletionTimestamp(anAppContext, aTimestamp);
    }

    protected void setMapKey(AppContext anAppContext, String aString) { mapKey = aString; }
    protected void setFuncName(AppContext anAppContext, String fName)  { functionName = fName; }



    //-----------------------------------------------------------------------------
    // Get contents of IcofObject as a String
    //-----------------------------------------------------------------------------
    public abstract String asString(AppContext anAppContext) throws IcofException;

    //-----------------------------------------------------------------------------
    // Functions included for readability
    //-----------------------------------------------------------------------------
    public boolean isMarkedAsDeleted(AppContext anAppContext) {
        boolean marked = false;
        if ((!getDeletedBy(anAppContext).equals("")) &&
                        (getDeletedBy(anAppContext) != null)) {
            marked = true;
        }
        return marked;
    }

    //-----------------------------------------------------------------------------
    // Member "getter" functions
    //-----------------------------------------------------------------------------
    protected String getFuncName(AppContext anAppContext) { return functionName; }

}

//==========================  END OF FILE  ====================================
