/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2000 - 2009 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: IcofUserRole.java
*
* CREATOR: Karen Kellam
*    DEPT: 5ZIA
*    DATE: 10/25/2001
*
*-PURPOSE---------------------------------------------------------------------
* IcofUserRole class definition file.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 10/25/2001 KK  Initial coding.
* 07/29/2005 KW  Added javadoc.
* 12/15/2005 KKW  Modified due to splitting of Constants.java into several
*                 *Util classes.
* 12/05/2006 KKW  Added AppContext as first parameter of each method
* 02/05/2009 KKW  Added deleteAssociations and markAssociations which are now
*                 required for DBDelete interface.
*=============================================================================
* </pre>
*/

package com.ibm.stg.iipmds.icof.component.role;
import java.util.Date;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.db.IcofUserRoleDBInterface;
import com.ibm.stg.iipmds.icof.component.db.OrdCreatorDBInterface;
import com.ibm.stg.iipmds.icof.component.db.OrdMgrDBInterface;
import com.ibm.stg.iipmds.icof.component.db.PgmMgrDBInterface;
import com.ibm.stg.iipmds.icof.component.db.RelMgrDBInterface;
import com.ibm.stg.iipmds.icof.component.db.ShipperDBInterface;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.mom.DBAdd;
import com.ibm.stg.iipmds.icof.component.mom.DBDelete;
import com.ibm.stg.iipmds.icof.component.mom.IcofObject;


public class IcofUserRole extends IcofObject implements DBAdd, DBDelete {


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from application when only names
   *   are known.
   *
   * This constructor does an automatic database lookup to fully populate
   *   the object.
   *
   * @param     anIntranetUserid  the intranet id of the person associated with the
   *                              specified role name
   * @param     aRoleName         the role name
   * @param     anAppContext      the db2 context and user information
   *
   * @exception IcofException     userid/role name does not exist in database
   */
  //-----------------------------------------------------------------------------
  public IcofUserRole(AppContext anAppContext
                      ,String anIntranetUserid
                      ,String aRoleName) throws IcofException {

    super(anAppContext);
    setIntranetUserid(anAppContext, anIntranetUserid);
    setRoleName(anAppContext, aRoleName);
    dbLookUpByName(anAppContext);

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from application when only IDs
   *   are known.
   *
   * This is the constructor to use when creating an object to
   *   add to the database.
   *
   * If the object is already in the database, use dbLookUpById() to fully
   *   populate the object.
   *
   * @param     anIcofUserID      the numeric id of the person associated with
   *                              the role specified by the role id
   * @param     aRoleID           the numeric id of this role
   * @param     anAppContext      the db2 context and user information
   *
   */
  //-----------------------------------------------------------------------------
  public IcofUserRole(AppContext anAppContext
                      ,short anIcofUserID
                      ,short aRoleID) {

    this(anAppContext
         ,anIcofUserID
         ,""
         ,aRoleID
         ,""
         ,""
         ,Constants.NULL_DATE);

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from database
   *
   * @param     anIcofUserID      the numeric id of the person associated with
   *                              the role specified by the role id
   * @param     anIntranetUserid  the intranet id of the person associated with the
   *                              specified role name
   * @param     aRoleID           the numeric id of this role
   * @param     aRoleName         the role name
   * @param     aCreatedBy        audit information -- the afs userid of the
   *                              person who created this entry in the database
   * @param    aCreationTimestamp audit information -- when this entry was
   *                              created in the database
   * @param     anAppContext      the db2 context and user information
   *
   */
  //-----------------------------------------------------------------------------
  public IcofUserRole(AppContext anAppContext
                      ,short anIcofUserID
                      ,String anIntranetUserid
                      ,short aRoleID
                      ,String aRoleName
                      ,String aCreatedBy
                      ,Date aCreationTimestamp) {

    this(anAppContext
         ,anIcofUserID
         ,anIntranetUserid
         ,aRoleID
         ,aRoleName
         ,aCreatedBy
         ,aCreationTimestamp
         ,""
         ,Constants.NULL_DATE);

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate "marked as deleted" object from database
   *
   * @param     anIcofUserID      the numeric id of the person associated with
   *                              the role specified by the role id
   * @param     anIntranetUserid  the userid of the person associated with the
   *                              specified role name
   * @param     aRoleID           the numeric id of this role
   * @param     aRoleName         the role name
   * @param     aCreatedBy        audit information -- the afs userid of the
   *                              person who created this entry in the database
   * @param    aCreationTimestamp audit information -- when this entry was
   *                              created in the database
   * @param     aDeletedBy        audit information -- the afs userid of the
   *                              person who deleted this entry in the database
   * @param    aDeletionTimestamp audit information -- when this entry was
   *                              deleted in the database
   * @param     anAppContext      the db2 context and user information
   */
  //-----------------------------------------------------------------------------
  public IcofUserRole(AppContext anAppContext
                      ,short anIcofUserID
                      ,String anIntranetUserid
                      ,short aRoleID
                      ,String aRoleName
                      ,String aCreatedBy
                      ,Date aCreationTimestamp
                      ,String aDeletedBy
                      ,Date aDeletionTimestamp) {

    super(anAppContext
          ,aCreatedBy
          ,aCreationTimestamp
          ,aDeletedBy
          ,aDeletionTimestamp);

    setIcofUserID(anAppContext, anIcofUserID);
    setIntranetUserid(anAppContext, anIntranetUserid);
    setRoleID(anAppContext, aRoleID);
    setRoleName(anAppContext, aRoleName);
    setMapKey(anAppContext);

  }


  //-----------------------------------------------------------------------------
  // Member "getter" functions
  //-----------------------------------------------------------------------------
  /**
  *  Get the value of the numeric id key used for treeMaps
  */
  public String getMapIDKey(AppContext anAppContext) { return mapIDKey; }
  /**
  *  Get the value of the numeric id for the user associated with the role
  */
  public short getIcofUserID(AppContext anAppContext) { return icofUserID; }
  /**
  *  Get the value of the afs userid for the user associated with the role
  */
  public String getIntranetUserid(AppContext anAppContext) { return intranetUserid; }
  /**
  *  Get the value of the role id
  */
  public short getRoleID(AppContext anAppContext) { return roleID; }
  /**
  *  Get the value of the role name
  */
  public String getRoleName(AppContext anAppContext) { return roleName; }


  //-----------------------------------------------------------------------------
  /**
   * Get contents of IcofUserRole as a string
   *
   * @return                      the contents of this object as a string
   * @exception IcofException     Unable to create the formatted string
   */
  //-----------------------------------------------------------------------------
  public String asString(AppContext anAppContext) throws IcofException {

    return("IcofUser ID: "           + String.valueOf(getIcofUserID(anAppContext)) + "; " +
           "Intranet Userid: "       + getIntranetUserid(anAppContext) + "; " +
           "Role ID: "               + String.valueOf(getRoleID(anAppContext)) + "; " +
           "Role Name: "             + getRoleName(anAppContext) + "; " +
           "Created By: "            + getCreatedBy(anAppContext) + "; "  +
           "Creation Timestamp: "    + getCreationTimestamp(anAppContext).toString() + "; "  +
           "Deleted By: "            + getDeletedBy(anAppContext) + "; "  +
           "Deletion Timestamp: "    + getDeletionTimestamp(anAppContext).toString() + "; " +
           "App Context: "           + anAppContext.asString() + ";" +
           "Map ID Key: "            + getMapIDKey(anAppContext) + "; " +
           "Map Key: "               + getMapKey(anAppContext));

  }


  //-----------------------------------------------------------------------------
  /**
   * Lookup an IcofUserRole in the database, using the userid and
   *   role name.  Populate the data members in this object with the values from
   *   the database.
   *
   * @exception IcofException     role name and intranet userid association does
   *                              not exist in database
   */
  //-----------------------------------------------------------------------------
  public void dbLookUpByName(AppContext anAppContext) throws IcofException {

    setFuncName(anAppContext, "dbLookUpByName(AppContext)");

    IcofUserRoleDBInterface dba =
        new IcofUserRoleDBInterface(anAppContext);
    if (!dba.selectRow(anAppContext
                       ,getIntranetUserid(anAppContext)
                       ,getRoleName(anAppContext))) {
      if (!dba.selectMarkedRow(anAppContext
                               ,getIntranetUserid(anAppContext)
                               ,getRoleName(anAppContext))) {
        String msg = new String("Could not find icofUserRole in IcofUserRole table");
        IcofException ie = new IcofException(this.getClass().getName()
                                             ,getFuncName(anAppContext)
                                             ,IcofException.SEVERE
                                             ,msg
                                             ,"Intranet Userid: " 
                                             + getIntranetUserid(anAppContext)
                                             +" Role Name: " 
                                             + getRoleName(anAppContext));
        anAppContext.getSessionLog().log(ie);
        throw(ie);
      }
    }

    populate(anAppContext, dba);

  }


  //-----------------------------------------------------------------------------
  /**
   * Lookup an IcofUserRole in the database, using the userid and role
   *   id.
   *   Populate the data members in this object with the values from
   *   the database.
   *
   * @exception IcofException     role id and icofUser id association does
   *                              not exist in database
   */
  //-----------------------------------------------------------------------------
  public void dbLookUpById(AppContext anAppContext) throws IcofException {

    setFuncName(anAppContext, "dbLookUpById(AppContext)");

    IcofUserRoleDBInterface dba =
        new IcofUserRoleDBInterface(anAppContext);
    if (!dba.selectRow(anAppContext
                       ,getIcofUserID(anAppContext)
                       ,getRoleID(anAppContext))) {
      if (!dba.selectMarkedRow(anAppContext
                               ,getIcofUserID(anAppContext)
                               ,getRoleID(anAppContext))) {
        String msg = new String("Could not find icofUserRole in IcofUserRole table");
        IcofException ie = new IcofException(this.getClass().getName()
                                             ,getFuncName(anAppContext)
                                             ,IcofException.SEVERE
                                             ,msg
                                             ,"IcofUser ID: " 
                                             + String.valueOf(getIcofUserID(anAppContext))
                                             +" Role ID: " 
                                             + String.valueOf(getRoleID(anAppContext)));
        anAppContext.getSessionLog().log(ie);
        throw(ie);
      }
    }

    populate(anAppContext, dba);

  }


  //-----------------------------------------------------------------------------
  /**
   * Delete an IcofUserRole from the database
   *
   * @exception IcofException       Unable to delete row
   */
  //-----------------------------------------------------------------------------
  public void dbDelete(AppContext anAppContext) throws IcofException {

    IcofUserRoleDBInterface dba =
        new IcofUserRoleDBInterface(anAppContext);
    dba.deleteRow(anAppContext, this);

  }


  //-----------------------------------------------------------------------------
  /**
   * Mark an IcofUserRole as deleted in the database
   *
   * @exception IcofException       Unable to mark row as deleted
   */
  //-----------------------------------------------------------------------------
  public void dbMark(AppContext anAppContext) throws IcofException {

    IcofUserRoleDBInterface dba =
        new IcofUserRoleDBInterface(anAppContext);
    dba.markRow(anAppContext, this);

  }


  //-----------------------------------------------------------------------------
  /**
   * Unmark an IcofUserRole as deleted in the database
   *
   * @exception IcofException       Unable to re-activate row
   */
  //-----------------------------------------------------------------------------
  public void dbUnmark(AppContext anAppContext) throws IcofException {

    IcofUserRoleDBInterface dba =
        new IcofUserRoleDBInterface(anAppContext);
    dba.unmarkRow(anAppContext, this);

  }


  //-----------------------------------------------------------------------------
  /**
   * Add an IcofUserRole to the database -- this function is PROTECTED because
   *   all applications should use addOrReinstate.
   *
   * @exception IcofException       Unable to add row
   */
  //-----------------------------------------------------------------------------
  public void dbAdd(AppContext anAppContext) throws IcofException {

    IcofUserRoleDBInterface dba =
        new IcofUserRoleDBInterface(anAppContext);
    dba.insertRow(anAppContext, this);

  }


  //-----------------------------------------------------------------------------
  /**
   * Determine whether to add this IcofUserRole or to reinstate it (if it
   *   already exists, but has been marked as deleted)
   *
   * @return                      Constants.IS_ACTIVE, if already in database;
   *                              Constants.ADDED, if added to database;
   *                              Constants.REINSTATED, if reactivated in database.
   * @exception                   Unable to communicate with database.
   */
  //-----------------------------------------------------------------------------
  public String addOrReinstate(AppContext anAppContext) throws IcofException {

    IcofUserRoleDBInterface dba =
        new IcofUserRoleDBInterface(anAppContext);
    if (dba.selectRow(anAppContext, this)) {
      populate(anAppContext, dba);
      return Constants.IS_ACTIVE;
    }

    if (dba.selectMarkedRow(anAppContext, this)) {
      //
      // Populate the object prior to unmarking it, because the db2 host
      //   variables get reset during the unmark function.
      //
      populate(anAppContext, dba);
      dbUnmark(anAppContext);
      return Constants.REINSTATED;
    }

    //
    // If we get here, it means this IcofUserRole was not in the database,
    //   at all, so we need to add it.
    // In order to fully populate it, select the row that was added.
    //
    dbAdd(anAppContext);
    dbLookUpById(anAppContext);
    return Constants.ADDED;

  }


  //-----------------------------------------------------------------------------
  /**
   * Determine whether or not an IcofUserRole can be physically deleted,
   *   marked as deleted, or neither.
   *
   * @return                      Constants.MUST_REMAIN, if cannot be deleted;
   *                              Constants.MARKABLE, if can be marked as deleted;
   *                              Constants.DELETABLE, if can by physically deleted.
   * @exception                   Unable to communicate with database.
   */
  //-----------------------------------------------------------------------------
  public String testDeletability(AppContext anAppContext) throws IcofException {

    //
    // An IcofUserRole is deletable only if:
    //   - The associated user does not appear in the
    //     corresponding specific role table (if one exists).
    //
    // If the user is listed as active in the corresponding specific role
    //   table, the icofUserRole entry must remain.
    // Otherwise, the icofUserRole entry is to be marked as deleted.
    //


    // Determine which role this record is for and check its corresponding
    //   specific table for this user.
    //   Be sure to check for both active records and
    //   marked as deleted records.

    if (getRoleName(anAppContext).equals(Constants.ORDER_CREATOR)) {
      OrdCreatorDBInterface ocTbl =
          new OrdCreatorDBInterface(anAppContext);
      if (ocTbl.selectRow(anAppContext, getIcofUserID(anAppContext))) {
        return Constants.MUST_REMAIN;
      }
      if (ocTbl.selectMarkedRow(anAppContext, getIcofUserID(anAppContext))) {
        return Constants.MARKABLE;
      }
      return Constants.DELETABLE;
    }

    if (getRoleName(anAppContext).equals(Constants.ORDER_MANAGER)) {
      OrdMgrDBInterface omTbl =
          new OrdMgrDBInterface(anAppContext);
      if (omTbl.selectRow(anAppContext, getIcofUserID(anAppContext))) {
        return Constants.MUST_REMAIN;
      }
      if (omTbl.selectMarkedRow(anAppContext, getIcofUserID(anAppContext))) {
        return Constants.MARKABLE;
      }
      return Constants.DELETABLE;
    }

    if (getRoleName(anAppContext).equals(Constants.PROGRAM_MANAGER)) {
      PgmMgrDBInterface pmTbl =
          new PgmMgrDBInterface(anAppContext);
      if (pmTbl.selectRow(anAppContext, getIcofUserID(anAppContext))) {
        return Constants.MUST_REMAIN;
      }
      if (pmTbl.selectMarkedRow(anAppContext, getIcofUserID(anAppContext))) {
        return Constants.MARKABLE;
      }
      return Constants.DELETABLE;
    }

    if (getRoleName(anAppContext).equals(Constants.RELEASE_MANAGER)) {
      RelMgrDBInterface rmTbl =
          new RelMgrDBInterface(anAppContext);
      if (rmTbl.selectRow(anAppContext, getIcofUserID(anAppContext))) {
        return Constants.MUST_REMAIN;
      }
      if (rmTbl.selectMarkedRow(anAppContext, getIcofUserID(anAppContext))) {
        return Constants.MARKABLE;
      }
      return Constants.DELETABLE;
    }

    if (getRoleName(anAppContext).equals(Constants.SHIPPER)) {
      ShipperDBInterface smTbl =
          new ShipperDBInterface(anAppContext);
      if (smTbl.selectRow(anAppContext, getIcofUserID(anAppContext))) {
        return Constants.MUST_REMAIN;
      }
      if (smTbl.selectMarkedRow(anAppContext, getIcofUserID(anAppContext))) {
        return Constants.MARKABLE;
      }
      return Constants.DELETABLE;
    }

    // If there is not a corresponding specific table for this role, then
    //   the icofUserRole is deletable.
    return Constants.DELETABLE;


  }


  // -------------------------------------------------------------------------
  /**
   * Delete all associations in the db for this object.
   * 
   * @param anAppContext
   *            Application context (includes log and db context)
   * @param deleteRow
   *            True, if this object's row is to be deleted after all
   *            associations have been deleted; False, to keep the row
   * @throws IcofException
   *             Unable to delete associations
   */
  // -------------------------------------------------------------------------
  public void deleteAssociations(AppContext anAppContext, boolean deleteRow)
                  throws IcofException {


      if (deleteRow) {
          dbDelete(anAppContext);
      }
  }


  // -------------------------------------------------------------------------
  /**
   * Mark as deleted all associations for this object.
   * 
   * @param anAppContext
   *            Application context (includes log and db context)
   * @param markRow
   *            True, if this object's row is to be marked as deleted
   *            after all associations have been marked as deleted; False, to
   *            keep the row active
   * @throws IcofException
   *             Unable to mark associations
   */
  // -------------------------------------------------------------------------
  public void markAssociations(AppContext anAppContext, boolean markRow)
                  throws IcofException {

      if (markRow) {
          dbMark(anAppContext);
      }
  }


  //-----------------------------------------------------------------------------
  // Data members
  //-----------------------------------------------------------------------------
  private String   mapIDKey;
  private short    icofUserID;
  private String   intranetUserid;
  private short    roleID;
  private String   roleName;


  //-----------------------------------------------------------------------------
  // Member "setter" functions
  //-----------------------------------------------------------------------------
  /**
  *  Set the value of the numeric id key and the string key used for treeMaps
  */
  private void setMapKey(AppContext anAppContext) {
    setMapKey(anAppContext
              ,getIntranetUserid(anAppContext) + ";" 
              + getRoleName(anAppContext));
    mapIDKey = String.valueOf(getIcofUserID(anAppContext)) + ";"
              + String.valueOf(getRoleID(anAppContext));
  }
  /**
   * Set the value of the numeric id for the user associated with the role
   */
  protected void setIcofUserID(AppContext anAppContext, short anID) {
    icofUserID = anID;
  }

  /**
   * Set the value of the afs userid for the user associated with the role
   */
  protected void setIntranetUserid(AppContext anAppContext, String aName) {
    intranetUserid = aName;
  }

  /**
   * Set the value of the role id
   */
  protected void setRoleID(AppContext anAppContext, short anID) {
    roleID = anID;
  }

  /**
   * Set the value of the role name
   */
  protected void setRoleName(AppContext anAppContext, String aName) {
    roleName = aName;
  }


  //-----------------------------------------------------------------------------
  /**
   * Populate this object from its corresponding database object.
   *
   * @param     dba               the data from a db2 "select *" query on the
   *                              icof_user_role table
   */
  //-----------------------------------------------------------------------------
  private void populate(AppContext anAppContext, IcofUserRoleDBInterface dba) {

    setIcofUserID(anAppContext, dba.getIcofUserID(anAppContext));
    setIntranetUserid(anAppContext, dba.getIntranetUserid(anAppContext));
    setRoleID(anAppContext, dba.getRoleID(anAppContext));
    setRoleName(anAppContext, dba.getRoleName(anAppContext));
    setCreatedBy(anAppContext, dba.getCreatedBy(anAppContext));
    setCreationTimestamp(anAppContext, dba.getCreationTmstmp(anAppContext));
    setDeletedBy(anAppContext, dba.getDeletedBy(anAppContext));
    setDeletionTimestamp(anAppContext, dba.getDeletionTmstmp(anAppContext));
    setMapKey(anAppContext);

  }

}

//==========================  END OF FILE  ====================================
