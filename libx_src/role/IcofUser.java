/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2001 - 2009 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: IcofUser.java
*
* CREATOR: Karen K. Kellam
*    DEPT: 5ZIA
*    DATE: 10/24/2001
*
*-PURPOSE---------------------------------------------------------------------
* IcofUser class definition file.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 10/24/2001 KKK  Initial coding.
* 07/29/2005 KKW  Added javadoc.
* 12/15/2005 KKW  Modified due to splitting of Constants.java into several
*                 *Util classes.
* 12/05/2006 KKW  Added AppContext as first parameter of each method
* 02/05/2009 KKW  Added deleteAssociations and markAssociations which are now
*                 required for DBDelete interface.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.role;
import java.util.Date;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.db.IcofUserDBInterface;
import com.ibm.stg.iipmds.icof.component.db.IcofUserRoleDBInterface;
import com.ibm.stg.iipmds.icof.component.db.OrdCreatorDBInterface;
import com.ibm.stg.iipmds.icof.component.db.OrdMgrDBInterface;
import com.ibm.stg.iipmds.icof.component.db.PgmMgrDBInterface;
import com.ibm.stg.iipmds.icof.component.db.RelMgrDBInterface;
import com.ibm.stg.iipmds.icof.component.db.ShipperDBInterface;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.mom.DBAdd;
import com.ibm.stg.iipmds.icof.component.mom.DBDelete;
import com.ibm.stg.iipmds.icof.component.mom.DBUpdate;
import com.ibm.stg.iipmds.icof.component.mom.IcofObject;


public class IcofUser extends IcofObject implements DBAdd, DBDelete, DBUpdate {


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from application
   *
   * This constructor does an automatic database lookup to populate all fields.
   *
   * @param     anID              the numeric id of the user
   * @param     anAppContext      the db2 context and user information
   *
   * @exception IcofException     user does not exist in database
   */
  //-----------------------------------------------------------------------------
  public IcofUser(AppContext anAppContext
                  ,short anID) throws IcofException {

    super(anAppContext);
    setID(anAppContext, anID);
    dbLookUpById(anAppContext);

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from application when only the
   *   intranet userid is known.
   *
   * This constructor does an automatic database lookup to populate all fields.
   *
   * @param     anIntranetUserid  the userid of the person associated with the
   *                              specified role name
   * @param     anAppContext      the db2 context and user information
   *
   * @exception IcofException     userid/role name does not exist in database
   */
  //-----------------------------------------------------------------------------
  public IcofUser(AppContext anAppContext
                  ,String anIntranetUserid) throws IcofException {

    super(anAppContext);
    setIntranetUserid(anAppContext, anIntranetUserid);
    dbLookUpByName(anAppContext);

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from application
   *
   * This is the constructor to use when creating an object to add to the
   *   database.
   *
   * @param     aFirstName        the user's first name, which can include
   *                              their middle initial
   * @param     aLastName         the user's last name
   * @param     aUserid           the user's afs userid
   * @param     anEmailAddr       the user's email address
   * @param     aPhoneNumber      the user's phone number
   * @param     anIntranetUserid  the user's intranet id
   * @param     anAppContext      the db2 context and user information
   */
  //-----------------------------------------------------------------------------
  public IcofUser(AppContext anAppContext
                  ,String aFirstName
                  ,String aLastName
                  ,String aUserid
                  ,String anEmailAddr
                  ,String aPhoneNumber
                  ,String anIntranetUserid) {

    this(anAppContext
         ,Constants.EMPTY
         ,aFirstName
         ,aLastName
         ,aUserid
         ,anEmailAddr
         ,aPhoneNumber
         ,anIntranetUserid
         ,""
         ,Constants.NULL_DATE
         ,""
         ,Constants.NULL_DATE);

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from database
   *
   * @param     aFirstName        the user's first name, which can include
   *                              their middle initial
   * @param     aLastName         the user's last name
   * @param     aUserid           the user's afs userid
   * @param     anEmailAddr       the user's email address
   * @param     aPhoneNumber      the user's phone number
   * @param     anIntranetUserid  the user's intranet id
   * @param     aCreatedBy        audit information -- the afs userid of the
   *                              person who created this entry in the database
   * @param    aCreationTimestamp audit information -- when this entry was
   *                              created in the database
   * @param     anUpdatedBy       audit information -- the afs userid of the
   *                              person who last updated this entry in the database
   * @param    anUpdatedTimestamp audit information -- when this entry was
   *                              last updated in the database
   * @param     anAppContext      the db2 context and user information
   */
  //-----------------------------------------------------------------------------
  public IcofUser(AppContext anAppContext
                  ,short anID
                  ,String aFirstName
                  ,String aLastName
                  ,String aUserid
                  ,String anEmailAddr
                  ,String aPhoneNumber
                  ,String anIntranetUserid
                  ,String aCreatedBy
                  ,Date aCreationTimestamp
                  ,String anUpdatedBy
                  ,Date anUpdatedTimestamp) {

    this(anAppContext
         ,anID
         ,aFirstName
         ,aLastName
         ,aUserid
         ,anEmailAddr
         ,aPhoneNumber
         ,anIntranetUserid
         ,aCreatedBy
         ,aCreationTimestamp
         ,anUpdatedBy
         ,anUpdatedTimestamp
         ,""
         ,Constants.NULL_DATE);

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate "marked as deleted" object from database
   *
   * @param     aFirstName        the user's first name, which can include
   *                              their middle initial
   * @param     aLastName         the user's last name
   * @param     aUserid           the user's afs userid
   * @param     anEmailAddr       the user's email address
   * @param     aPhoneNumber      the user's phone number
   * @param     anIntranetUserid  the user's intranet id
   * @param     aCreatedBy        audit information -- the afs userid of the
   *                              person who created this entry in the database
   * @param    aCreationTimestamp audit information -- when this entry was
   *                              created in the database
   * @param     anUpdatedBy       audit information -- the afs userid of the
   *                              person who last updated this entry in the database
   * @param    anUpdatedTimestamp audit information -- when this entry was
   *                              last updated in the database
   * @param     aDeletedBy        audit information -- the afs userid of the
   *                              person who deleted this entry in the database
   * @param    aDeletionTimestamp audit information -- when this entry was
   *                              deleted in the database
   * @param     anAppContext      the db2 context and user information
   */
  //-----------------------------------------------------------------------------
  public IcofUser(AppContext anAppContext
                  ,short anID
                  ,String aFirstName
                  ,String aLastName
                  ,String aUserid
                  ,String anEmailAddr
                  ,String aPhoneNumber
                  ,String anIntranetUserid
                  ,String aCreatedBy
                  ,Date aCreationTimestamp
                  ,String anUpdatedBy
                  ,Date anUpdatedTimestamp
                  ,String aDeletedBy
                  ,Date aDeletionTimestamp) {

    super(anAppContext
          ,aCreatedBy
          ,aCreationTimestamp
          ,anUpdatedBy
          ,anUpdatedTimestamp
          ,aDeletedBy
          ,aDeletionTimestamp);

    setID(anAppContext, anID);
    setFirstName(anAppContext, aFirstName);
    setLastName(anAppContext, aLastName);
    setUserid(anAppContext, aUserid);
    setEmailAddr(anAppContext, anEmailAddr);
    setPhoneNumber(anAppContext, aPhoneNumber);
    setIntranetUserid(anAppContext, anIntranetUserid);
    setMapKey(anAppContext);

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor -- used when child classes want to initialize audit info
   *   to default values.
   *
   * @param     anAppContext      the db2 context and user information
   */
  //-----------------------------------------------------------------------------
  protected IcofUser(AppContext anAppContext) {

    super(anAppContext
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
  /**
  *  Get the value of the numeric id for this user
  */
  public short getID(AppContext anAppContext) { return id; }
  /**
  *  Get the value of the user's first name
  */
  public String getFirstName(AppContext anAppContext) { return firstName; }
  /**
  *  Get the value of the user's last name
  */
  public String getLastName(AppContext anAppContext) { return lastName; }
  /**
  *  Get the value of the user's full name
  */
  public String getFullName(AppContext anAppContext) { return firstName + " " + lastName; }
  /**
  *  Get the value of the user's afs userid
  */
  public String getUserid(AppContext anAppContext) { return userid; }
  /**
  *  Get the value of the user's email address
  */
  public String getEmailAddr(AppContext anAppContext) { return emailAddr; }
  /**
  *  Get the value of the user's phone number
  */
  public String getPhoneNumber(AppContext anAppContext) { return phoneNumber; }
  /**
  *  Get the value of the user's intranet userid
  */
  public String getIntranetUserid(AppContext anAppContext) { return intranetUserid; }


  //-----------------------------------------------------------------------------
  /**
   * Get contents of Icof User as a string
   *
   * @return                      the contents of this object as a string
   * @exception IcofException     Unable to create the formatted string
   */
  //-----------------------------------------------------------------------------
  public String asString(AppContext anAppContext) throws IcofException {

    return("ID: "                    + String.valueOf(getID(anAppContext)) + ";" +
           "First name: "            + getFirstName(anAppContext) + ";" +
           "Last Name: "             + getLastName(anAppContext) + ";" +
           "Userid: "                + getUserid(anAppContext) + ";"  +
           "Email addr: "            + getEmailAddr(anAppContext) + ";" +
           "Phone number: "          + getPhoneNumber(anAppContext) + ";" +
           "Intranet Userid: "       + getIntranetUserid(anAppContext) + ";" +
           "Created By: "            + getCreatedBy(anAppContext) + ";"  +
           "Creation Timestamp: "    + getCreationTimestamp(anAppContext).toString() + ";"  +
           "Updated By: "            + getUpdatedBy(anAppContext) + ";"  +
           "Updated Timestamp: "     + getUpdatedTimestamp(anAppContext).toString() + ";"  +
           "Deleted By: "            + getDeletedBy(anAppContext) + ";"  +
           "Deletion Timestamp: "    + getDeletionTimestamp(anAppContext).toString() + ";" +
           "App Context: "           + anAppContext.asString() + ";" +
           "Map Key: "               + getMapKey(anAppContext));

  }


  //-----------------------------------------------------------------------------
  /**
   * Lookup an Icof User in the database, using the intranet id.
   *   Populate the data members in this object with the values from
   *   the database.
   *
   * @exception IcofException     intranet id does not exist in database
   */
  //-----------------------------------------------------------------------------
  public void dbLookUpByName(AppContext anAppContext) throws IcofException {

    setFuncName(anAppContext, "dbLookUpByName(AppContext)");

    IcofUserDBInterface dba = new IcofUserDBInterface(anAppContext);
    if (!dba.selectRow(anAppContext, getIntranetUserid(anAppContext))) {
      if (!dba.selectMarkedRow(anAppContext, getIntranetUserid(anAppContext))) {
        String msg = new String("Could not find intranet userid in " +
                                "IcofUser table");
        IcofException ie = new IcofException(this.getClass().getName()
                                             ,getFuncName(anAppContext)
                                             ,IcofException.SEVERE
                                             ,msg
                                             ,getIntranetUserid(anAppContext));
        anAppContext.getSessionLog().log(ie);
        throw(ie);
      }
    }

    populate(anAppContext, dba);

  }


  //-----------------------------------------------------------------------------
  /**
   * Lookup an Icof User in the database, using the ID.
   *   Populate the data members in this object with the values from
   *   the database.
   *
   * @exception IcofException     user does not exist in database
   */
  //-----------------------------------------------------------------------------
  public void dbLookUpById(AppContext anAppContext) throws IcofException {

    setFuncName(anAppContext, "dbLookUpById(AppContext)");

    IcofUserDBInterface dba = new IcofUserDBInterface(anAppContext);
    if (!dba.selectRow(anAppContext, getID(anAppContext))) {
      if (!dba.selectMarkedRow(anAppContext, getID(anAppContext))) {
        String msg = new String("Could not find icof user ID in IcofUser table");
        IcofException ie = new IcofException(this.getClass().getName()
                                             ,getFuncName(anAppContext)
                                             ,IcofException.SEVERE
                                             ,msg
                                             ,String.valueOf(getID(anAppContext)));
        anAppContext.getSessionLog().log(ie);
        throw(ie);
      }
    }

    populate(anAppContext, dba);

  }


  //-----------------------------------------------------------------------------
  /*
   * Delete an Icof User from the database
   *
   * @exception IcofException       Unable to delete row
   */
  //-----------------------------------------------------------------------------
  public void dbDelete(AppContext anAppContext) throws IcofException {

    IcofUserDBInterface dba = new IcofUserDBInterface(anAppContext);
    dba.deleteRow(anAppContext, this);

  }


  //-----------------------------------------------------------------------------
  /**
   * Mark an Icof User as deleted in the database
   *
   * @exception IcofException       Unable to mark row as deleted
   */
  //-----------------------------------------------------------------------------
  public void dbMark(AppContext anAppContext) throws IcofException {

    IcofUserDBInterface dba = new IcofUserDBInterface(anAppContext);
    dba.markRow(anAppContext, this);

  }


  //-----------------------------------------------------------------------------
  /**
   * Unmark an Icof User as deleted in the database
   *
   * @exception IcofException       Unable to re-activate row
   */
  //-----------------------------------------------------------------------------
  public void dbUnmark(AppContext anAppContext) throws IcofException {

    IcofUserDBInterface dba = new IcofUserDBInterface(anAppContext);
    dba.unmarkRow(anAppContext, this);

  }


  //-----------------------------------------------------------------------------
  /**
   * Update an Icof User's info in the database
   *
   * @exception IcofException       Unable to update row
   */
  //-----------------------------------------------------------------------------
  public void dbUpdate(AppContext anAppContext) throws IcofException {

    IcofUserDBInterface dba = new IcofUserDBInterface(anAppContext);
    dba.updateRow(anAppContext, this);

  }


  //-----------------------------------------------------------------------------
  /**
   * Add an Icof User to the database -- this function is PROTECTED because
   *   all applications should use addOrReinstate()
   *
   * @exception IcofException       Unable to add row
   */
  //-----------------------------------------------------------------------------
  public void dbAdd(AppContext anAppContext) throws IcofException {

    IcofUserDBInterface dba = new IcofUserDBInterface(anAppContext);
    setID(anAppContext, dba.getNextID(anAppContext));
    dba.insertRow(anAppContext, this);

  }



  //-----------------------------------------------------------------------------
  /**
   * Determine whether to add this Icof User or to reinstate it
   *   (if it already exists, but has been marked as deleted)
   *
   * @return                      Constants.IS_ACTIVE, if already in database;
   *                              Constants.ADDED, if added to database;
   *                              Constants.REINSTATED, if reactivated in database.
   * @exception                   Unable to communicate with database.
   */
  //-----------------------------------------------------------------------------
  public String addOrReinstate(AppContext anAppContext) throws IcofException {

    IcofUserDBInterface dba = new IcofUserDBInterface(anAppContext);
    if (dba.selectRow(anAppContext, getIntranetUserid(anAppContext))) {
      populate(anAppContext, dba);
      return Constants.IS_ACTIVE;
    }

    if (dba.selectMarkedRow(anAppContext, getIntranetUserid(anAppContext))) {
      //
      // Populate the object prior to unmarking it, because the db2 host
      //   variables get reset during the unmark function.
      //
      populate(anAppContext, dba);
      dbUnmark(anAppContext);
      return Constants.REINSTATED;
    }

    //
    // If we get here, it means this Icof User was not in the
    //   database, at all, so we need to add it.
    // In order to fully populate it, select the row that was added.
    //
    dbAdd(anAppContext);
    dbLookUpById(anAppContext);
    return Constants.ADDED;

  }


  //-----------------------------------------------------------------------------
  /**
   * Determine whether or not an Icof User can be physically deleted from
   *   the database
   *
   * @return                      Constants.MUST_REMAIN, if cannot be deleted;
   *                              Constants.MARKABLE, if can be marked as deleted;
   *                              Constants.DELETABLE, if can by physically deleted.
   * @exception                   Unable to communicate with database.
   */
  //-----------------------------------------------------------------------------
  public String testDeletability(AppContext anAppContext) throws IcofException {

    //
    // An Icof User is deletable only if:
    //   -- there are no active roles associated with him
    //   -- he does not appear as an active member of any individual role table
    //
    // If there are active roles, then the Icof User
    //   must remain.
    //
    // If there are only inactive roles, then the Icof User
    //   can be marked as deleted.
    //

    IcofUserRoleDBInterface iurTbl =
        new IcofUserRoleDBInterface(anAppContext);
    if (iurTbl.exists(anAppContext, this)) {
      return(Constants.MUST_REMAIN);
    }

    PgmMgrDBInterface pmTbl =
        new PgmMgrDBInterface(anAppContext);
    if (pmTbl.exists(anAppContext, this)) {
      return(Constants.MUST_REMAIN);
    }

    RelMgrDBInterface rmTbl =
        new RelMgrDBInterface(anAppContext);
    if (rmTbl.exists(anAppContext, this)) {
      return(Constants.MUST_REMAIN);
    }

    ShipperDBInterface smTbl =
        new ShipperDBInterface(anAppContext);
    if (smTbl.exists(anAppContext, this)) {
      return(Constants.MUST_REMAIN);
    }

    OrdCreatorDBInterface ocTbl =
        new OrdCreatorDBInterface(anAppContext);
    if (ocTbl.exists(anAppContext, this)) {
      return(Constants.MUST_REMAIN);
    }

    OrdMgrDBInterface omTbl =
        new OrdMgrDBInterface(anAppContext);
    if (omTbl.exists(anAppContext, this)) {
      return(Constants.MUST_REMAIN);
    }

    if (iurTbl.existsMarked(anAppContext, this)) {
      return(Constants.MARKABLE);
    }

    if (pmTbl.existsMarked(anAppContext, this)) {
      return(Constants.MARKABLE);
    }

    if (rmTbl.existsMarked(anAppContext, this)) {
      return(Constants.MARKABLE);
    }

    if (smTbl.existsMarked(anAppContext, this)) {
      return(Constants.MARKABLE);
    }

    if (ocTbl.existsMarked(anAppContext, this)) {
      return(Constants.MARKABLE);
    }

    if (omTbl.existsMarked(anAppContext, this)) {
      return(Constants.MARKABLE);
    }

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
  private short    id;
  private String   firstName;
  private String   lastName;
  private String   userid;
  private String   emailAddr;
  private String   phoneNumber;
  private String   intranetUserid;


  //-----------------------------------------------------------------------------
  // Member "setter" functions
  //-----------------------------------------------------------------------------
  /**
  *  Set the value of the key used for treeMaps
  */
  protected void setMapKey(AppContext anAppContext) {
    setMapKey(anAppContext, getIntranetUserid(anAppContext));
  }

  /**
   * Set the value of the numeric id for the user
   */
  protected void setID(AppContext anAppContext, short anID) {
    id = anID;
  }

  /**
   * Set the value of the user's first name
   */
  protected void setFirstName(AppContext anAppContext, String aName) {
    firstName = aName;
  }

  /**
   * Set the value of the user's last name
   */
  protected void setLastName(AppContext anAppContext, String aName) {
    lastName = aName;
  }

  /**
   * Set the value of the user's afs userid
   */
  protected void setUserid(AppContext anAppContext, String aUserid) {
    userid = aUserid;
  }

  /**
   * Set the value of the user's email address
   */
  protected void setEmailAddr(AppContext anAppContext, String anEmailAddr) {
    emailAddr = anEmailAddr;
  }

  /**
   * Set the value of the user's phone number
   */
  protected void setPhoneNumber(AppContext anAppContext, String aPhoneNumber) {
    phoneNumber = aPhoneNumber;
  }

  /**
   * Set the value of the user's intranet id
   */
  protected void setIntranetUserid(AppContext anAppContext,
      String anIntranetUserid) {
    intranetUserid = anIntranetUserid;
  }


  //-----------------------------------------------------------------------------
  /**
   * Populate this object from its corresponding database object.
   *
   * @param     dba               the data from a db2 "select *" query on the
   *                              icof_user table
   */
  //-----------------------------------------------------------------------------
  private void populate(AppContext anAppContext, IcofUserDBInterface dba) {

    setID(anAppContext, dba.getID(anAppContext));
    setFirstName(anAppContext, dba.getFirstName(anAppContext));
    setLastName(anAppContext, dba.getLastName(anAppContext));
    setUserid(anAppContext, dba.getUserid(anAppContext));
    setEmailAddr(anAppContext, dba.getEmailAddr(anAppContext));
    setPhoneNumber(anAppContext, dba.getPhoneNumber(anAppContext));
    setIntranetUserid(anAppContext, dba.getIntranetUserid(anAppContext));
    setCreatedBy(anAppContext, dba.getCreatedBy(anAppContext));
    setCreationTimestamp(anAppContext, dba.getCreationTmstmp(anAppContext));
    setUpdatedBy(anAppContext, dba.getUpdatedBy(anAppContext));
    setUpdatedTimestamp(anAppContext, dba.getUpdatedTmstmp(anAppContext));
    setDeletedBy(anAppContext, dba.getDeletedBy(anAppContext));
    setDeletionTimestamp(anAppContext, dba.getDeletionTmstmp(anAppContext));
    setMapKey(anAppContext);

  }

}

//==========================  END OF FILE  ====================================
