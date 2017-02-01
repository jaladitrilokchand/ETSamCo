//=============================================================================
//
// Copyright: (C) IBM Corporation 2000 -- IBM Internal Use Only
//
//=============================================================================
//
//    FILE: Shipper.java
//
// CREATOR: Karen K. Kellam
//    DEPT: 5ZIA
//    DATE: 12/11/2000
//
//-PURPOSE---------------------------------------------------------------------
// Shipper class definition file.
//-----------------------------------------------------------------------------
//
//
//-CHANGE LOG------------------------------------------------------------------
// 12/11/2000 KKK  Initial coding.
// 10/24/2001 KKK  User/Role redesign.  Now inherits from IcofUser.
// 03/20/2002 KKK  Added AppContext to constructors for all objects.  Added
//                 DefaultContext and AuditUserid to all DBInterface constructors.
//                 Convert to Java 1.2.2.
// 12/15/2005 KKW  Modified due to splitting of Constants.java into several
//                 *Util classes.
// 09/14/2006 KPL  Added AppContext as firt param throughout, added exception
//                 logging
//=============================================================================

package com.ibm.stg.iipmds.icof.component.role;
import java.util.Date;
import java.util.TreeMap;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.db.ShipperDBInterface;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.proddef.TechnologyMap;


public class Shipper extends IcofUser {


  //-----------------------------------------------------------------------------
  // Constructor - used to instantiate object from application
  //
  // This constructor does an automatic database lookup to populate all fields.
  //-----------------------------------------------------------------------------
  public Shipper(AppContext anAppContext
                 ,short anID) 
      throws IcofException {

    super(anAppContext);
    setID(anAppContext, anID);
    dbLookUpById(anAppContext);

  }


  //-----------------------------------------------------------------------------
  // Constructor - used to instantiate object from application when only the
  //   afs userid is known.
  //
  // This constructor does an automatic database lookup to populate all fields.
  //-----------------------------------------------------------------------------
  public Shipper(AppContext anAppContext
                 ,String anIntranetUserid) 
      throws IcofException {

    super(anAppContext);
    setIntranetUserid(anAppContext, anIntranetUserid);
    dbLookUpByName(anAppContext);

  }


  //-----------------------------------------------------------------------------
  // Constructor - used to instantiate object from application
  //
  // This is the constructor to use when creating an object to add to the
  //   database.
  //-----------------------------------------------------------------------------
  public Shipper(AppContext anAppContext
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
  // Constructor - used to instantiate object from database
  //-----------------------------------------------------------------------------
  public Shipper(AppContext anAppContext
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
  // Constructor - used to instantiate "marked as deleted" object from database
  //-----------------------------------------------------------------------------
  public Shipper(AppContext anAppContext
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
          ,aDeletedBy
          ,aDeletionTimestamp);

  }


  //-----------------------------------------------------------------------------
  // Lookup a Shipper in the database, using the afs userid.
  //-----------------------------------------------------------------------------
  public void dbLookUpByName(AppContext anAppContext) 
      throws IcofException {

    setFuncName(anAppContext, "dbLookUpByName(AppContext)");

    ShipperDBInterface dba =
        new ShipperDBInterface(anAppContext);
    if (!dba.selectRow(anAppContext, getIntranetUserid(anAppContext))) {
      if (!dba.selectMarkedRow(anAppContext, getIntranetUserid(anAppContext))) {
        String msg = new String("Could not find shipper name in Shipper table");
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
  // Lookup a Shipper in the database, using the ID.
  //-----------------------------------------------------------------------------
  public void dbLookUpById(AppContext anAppContext) 
      throws IcofException {

    setFuncName(anAppContext, "dbLookUpById(anAppContext)");

    ShipperDBInterface dba =
        new ShipperDBInterface(anAppContext);
    if (!dba.selectRow(anAppContext, getID(anAppContext))) {
      if (!dba.selectMarkedRow(anAppContext, getID(anAppContext))) {
        String msg = new String("Could not find shipper ID in Shipper table");
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
  // Delete a Shipper from the database
  //-----------------------------------------------------------------------------
  public void dbDelete(AppContext anAppContext) 
      throws IcofException {

    ShipperDBInterface dba =
        new ShipperDBInterface(anAppContext);
    dba.deleteRow(anAppContext, this);

    // Now, delete from the IcofUserRole table.
    Role thisRole = new Role(anAppContext
                             ,Constants.SHIPPER);
    IcofUserRole thisUserRole =
        new IcofUserRole(anAppContext
                         ,getID(anAppContext)
                         ,thisRole.getID(anAppContext));
    thisUserRole.dbDelete(anAppContext);

  }


  //-----------------------------------------------------------------------------
  // Mark a Shipper as deleted in the database
  //-----------------------------------------------------------------------------
  public void dbMark(AppContext anAppContext) 
      throws IcofException {

    ShipperDBInterface dba =
        new ShipperDBInterface(anAppContext);
    dba.markRow(anAppContext, this);

    // Now, mark this Shipper in the IcofUserRole table.
    Role thisRole = new Role(anAppContext
                             ,Constants.SHIPPER);
    IcofUserRole thisUserRole =
        new IcofUserRole(anAppContext
                         ,getID(anAppContext)
                         ,thisRole.getID(anAppContext));
    thisUserRole.dbMark(anAppContext);

  }


  //-----------------------------------------------------------------------------
  // Unmark a Shipper as deleted in the database
  //-----------------------------------------------------------------------------
  public void dbUnmark(AppContext anAppContext) 
      throws IcofException {

    ShipperDBInterface dba =
        new ShipperDBInterface(anAppContext);
    dba.unmarkRow(anAppContext, this);

    // Now, unmark this Shipper in the IcofUserRole table.
    Role thisRole = new Role(anAppContext
                             ,Constants.SHIPPER);
    IcofUserRole thisUserRole =
        new IcofUserRole(anAppContext
                         ,getID(anAppContext)
                         ,thisRole.getID(anAppContext));
    thisUserRole.dbUnmark(anAppContext);

  }


  //-----------------------------------------------------------------------------
  // Add a Shipper to the database -- this function is PRIVATE because all
  //   applications should use addOrReinstate()
  //-----------------------------------------------------------------------------
  public void dbAdd(AppContext anAppContext) throws IcofException {

    ShipperDBInterface dba =
        new ShipperDBInterface(anAppContext);
    dba.insertRow(anAppContext, this);

  }


  //-----------------------------------------------------------------------------
  // Determine whether to add this shipper or to reinstate it
  //  (if it already exists, but has been marked as deleted)
  // Note -- there is no "mark as deleted" capability for shipper
  //   but this function is included for consistency.
  //-----------------------------------------------------------------------------
  public String addOrReinstate(AppContext anAppContext) throws IcofException {

    // First, make sure this person is in the IcofUser table.
    //   Note that the ID field will be guaranteed to have the correct
    //   value after this step.

    //
    // Because IcofUser is a parent class, we cannot just call super.addOrReinstate().
    //   The reason is that any public method called by super.addOrReinstate()
    //   will actually invoke the child's method.  Because most addOrReinstate()
    //   methods call dbLookUpById() (a public method), incorrect results occur
    //   if super.addOrReinstate() is called.  Therefore, fake it by
    //   instantiating a separate instance of IcofUser, so that its own
    //   methods will be called and not those of the child.
    //
    IcofUser thisUser = new IcofUser(anAppContext
                                     ,getFirstName(anAppContext)
                                     ,getLastName(anAppContext)
                                     ,getUserid(anAppContext)
                                     ,getEmailAddr(anAppContext)
                                     ,getPhoneNumber(anAppContext)
                                     ,getIntranetUserid(anAppContext));
    String parentResult = thisUser.addOrReinstate(anAppContext);
    setID(anAppContext, thisUser.getID(anAppContext));

    // Next, make sure this person is in the IcofUserRole table.
    Role thisRole = new Role(anAppContext
                             ,Constants.SHIPPER);
    IcofUserRole thisUserRole =
        new IcofUserRole(anAppContext
                         ,getID(anAppContext)
                         ,thisRole.getID(anAppContext));
    String userRoleResult = thisUserRole.addOrReinstate(anAppContext);

    // Finally, make sure this person is in the Shipper table.
    ShipperDBInterface dba =
        new ShipperDBInterface(anAppContext);
    if (dba.selectRow(anAppContext, getID(anAppContext))) {
      populate(anAppContext, dba);
      return Constants.IS_ACTIVE;
    }

    if (dba.selectMarkedRow(anAppContext, getID(anAppContext))) {
      //
      // Populate the object prior to unmarking it, because the db2 host
      //   variables get reset during the unmark function.
      //
      populate(anAppContext, dba);
      dbUnmark(anAppContext);
      return Constants.REINSTATED;
    }

    //
    // If we get here, it means this shipper was not in the
    //   database, at all, so we need to add it.
    // In order to fully populate it, select the row that was added.
    //
    dbAdd(anAppContext);
    dbLookUpById(anAppContext);
    return Constants.ADDED;

  }


  //-----------------------------------------------------------------------------
  // Determine whether or not a Shipper can be physically deleted from
  //   the database.  Also, return the list of technologies associated with
  //   the Shipper.
  //-----------------------------------------------------------------------------
  public String testDeletability(AppContext anAppContext
  		                         ,TreeMap assocTechs) 
      throws IcofException {

    //
    // A Shipper is deletable only if:
    //   -- it is not associated with any technologies
    //
    // If associated only with "marked as deleted" technologies,
    //   the Shipper can be "marked as deleted".
    //

    // Get the list of technologies associated with this Shipper.
    TreeMap activeTechs = TechnologyMap.createList(anAppContext, this);
    TreeMap markedTechs = TechnologyMap.createMarkedList(anAppContext, this);

    // Clear out the input list and set it to the list of technologies associated
    //   with this Shipper.
    assocTechs.clear();
    assocTechs.putAll(activeTechs);

    if (!activeTechs.isEmpty()) {
      return Constants.MUST_REMAIN;
    }

    if (!markedTechs.isEmpty()) {
      return Constants.MARKABLE;
    }

    return Constants.DELETABLE;

  }


  //-----------------------------------------------------------------------------
  // Populate this object from its corresponding database object.
  //-----------------------------------------------------------------------------
  private void populate(AppContext anAppContext
  		                ,ShipperDBInterface dba) {

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
