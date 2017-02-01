//=============================================================================
//
// Copyright: (C) IBM Corporation 2000 -- IBM Internal Use Only
//
//=============================================================================
//
//    FILE: OrderCreator.java
//
// CREATOR: Karen K. Kellam
//    DEPT: 5ZIA
//    DATE: 10/25/2001
//
//-PURPOSE---------------------------------------------------------------------
// OrderCreator class definition file.
//-----------------------------------------------------------------------------
//
//
//-CHANGE LOG------------------------------------------------------------------
// 10/25/2001 KKK  Initial coding.
// 12/15/2005 KKW  Modified due to splitting of Constants.java into several
//                 *Util classes.
// 12/05/2006 KKW  Added AppContext as first parameter of each method.
//=============================================================================

package com.ibm.stg.iipmds.icof.component.role;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.db.AcosOrderDBInterface;
import com.ibm.stg.iipmds.icof.component.db.OrdCreatorDBInterface;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.acossupport.AcosOrder;
import com.ibm.stg.iipmds.icof.component.acossupport.AcosOrderMap;


public class OrderCreator extends IcofUser {


  //-----------------------------------------------------------------------------
  // Constructor - used to instantiate object from application
  //
  // This constructor does an automatic database lookup to populate all fields.
  //-----------------------------------------------------------------------------
  public OrderCreator(AppContext anAppContext
                      ,short anID) throws IcofException {

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
  public OrderCreator(AppContext anAppContext
                      ,String anIntranetUserid) throws IcofException {

    super(anAppContext);
    setIntranetUserid(anAppContext, anIntranetUserid);
    dbLookUpByName(anAppContext);

  }


  //----------------------------------------------------------------------------
  // Constructor - used to instantiate object from application
  //
  // This is the constructor to use when creating an object to add to the
  //   database.
  //----------------------------------------------------------------------------
  public OrderCreator(AppContext anAppContext
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


  //----------------------------------------------------------------------------
  // Constructor - used to instantiate object from database
  //----------------------------------------------------------------------------
  public OrderCreator(AppContext anAppContext
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
  public OrderCreator(AppContext anAppContext
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
  // Lookup an Order Creator in the database, using the afs userid.
  //-----------------------------------------------------------------------------
  public void dbLookUpByName(AppContext anAppContext) throws IcofException {

    setFuncName(anAppContext, "dbLookUpByName(AppContext)");

    OrdCreatorDBInterface dba =
        new OrdCreatorDBInterface(anAppContext);
    if (!dba.selectRow(anAppContext, getIntranetUserid(anAppContext))) {
      if (!dba.selectMarkedRow(anAppContext, getIntranetUserid(anAppContext))) {
        String msg = new String("Could not find order creator name in OrdCreator table");
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
  // Lookup an OrderCreator in the database, using the ID.
  //-----------------------------------------------------------------------------
  public void dbLookUpById(AppContext anAppContext) throws IcofException {

    setFuncName(anAppContext, "dbLookUpById(AppContext)");

    OrdCreatorDBInterface dba =
        new OrdCreatorDBInterface(anAppContext);
    if (!dba.selectRow(anAppContext, getID(anAppContext))) {
      if (!dba.selectMarkedRow(anAppContext, getID(anAppContext))) {
        String msg = new String("Could not find order creator ID in OrdCreator table");
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


  //----------------------------------------------------------------------------
  // Delete an OrderCreator from the database
  //----------------------------------------------------------------------------
  public void dbDelete(AppContext anAppContext) throws IcofException {

    OrdCreatorDBInterface dba =
        new OrdCreatorDBInterface(anAppContext);
    dba.deleteRow(anAppContext, this);

    // Now, delete from the IcofUserRole table.
    Role thisRole = new Role(anAppContext
                             ,Constants.ORDER_CREATOR);
    IcofUserRole thisUserRole =
        new IcofUserRole(anAppContext
                         ,getID(anAppContext)
                         ,thisRole.getID(anAppContext));
    thisUserRole.dbDelete(anAppContext);

  }


  //-----------------------------------------------------------------------------
  // Mark an Order Creator as deleted in the database
  //-----------------------------------------------------------------------------
  public void dbMark(AppContext anAppContext) throws IcofException {

    OrdCreatorDBInterface dba =
        new OrdCreatorDBInterface(anAppContext);
    dba.markRow(anAppContext, this);

    // Now, mark this OrderCreator in the IcofUserRole table.
    Role thisRole = new Role(anAppContext
                             ,Constants.ORDER_CREATOR);
    IcofUserRole thisUserRole =
        new IcofUserRole(anAppContext
                         ,getID(anAppContext)
                         ,thisRole.getID(anAppContext));
    thisUserRole.dbMark(anAppContext);

  }


  //-----------------------------------------------------------------------------
  // Unmark an Order Creator as deleted in the database
  //-----------------------------------------------------------------------------
  public void dbUnmark(AppContext anAppContext) throws IcofException {

    OrdCreatorDBInterface dba =
        new OrdCreatorDBInterface(anAppContext);
    dba.unmarkRow(anAppContext, this);

    // Now, unmark this OrderCreator in the IcofUserRole table.
    Role thisRole = new Role(anAppContext
                             ,Constants.ORDER_CREATOR);
    IcofUserRole thisUserRole =
        new IcofUserRole(anAppContext
                         ,getID(anAppContext)
                         ,thisRole.getID(anAppContext));
    thisUserRole.dbUnmark(anAppContext);

  }


  //----------------------------------------------------------------------------
  // Add an OrderCreator to the database -- this function is PRIVATE because all
  //   applications should use addOrReinstate()
  //----------------------------------------------------------------------------
  public void dbAdd(AppContext anAppContext) throws IcofException {

    OrdCreatorDBInterface dba =
        new OrdCreatorDBInterface(anAppContext);
    dba.insertRow(anAppContext, this);

  }


  //-----------------------------------------------------------------------------
  // Determine whether to add this order creator or to reinstate it
  //  (if it already exists, but has been marked as deleted)
  // Note -- there is no "mark as deleted" capability for order
  //   creators, but this function is included for consistency.
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
                             ,Constants.ORDER_CREATOR);
    IcofUserRole thisUserRole = new IcofUserRole(anAppContext
                                                 ,getID(anAppContext)
                                                 ,thisRole.getID(anAppContext));
    String userRoleResult = thisUserRole.addOrReinstate(anAppContext);

    // Finally, make sure this person is in the Order Creator table.
    OrdCreatorDBInterface dba =
        new OrdCreatorDBInterface(anAppContext);
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
    // If we get here, it means this order creator was not in the
    //   database, at all, so we need to add it.
    // In order to fully populate it, select the row that was added.
    //
    dbAdd(anAppContext);
    dbLookUpById(anAppContext);
    return Constants.ADDED;

  }


  //-----------------------------------------------------------------------------
  // Determine whether or not an OrderCreator can be physically deleted from
  //   the database.
  //-----------------------------------------------------------------------------
  public String testDeletability(AppContext anAppContext) throws IcofException {

    //
    // An OrderCreator is deletable only if:
    //   -- it is not associated with any acos orders/suborders
    //
    // An OrderCreator is markable only if:
    //   -- it is only associated with completed or denied acos orders
    //
    // Otherwise, it must remain.
    //


    //
    // Get all the active (not marked as deleted) orders associated with
    //   this orderCreator.  If there are any, see if any of the orders are
    //   still being processed (status is not complete and is not denied).
    //
    TreeMap activeOrders = AcosOrderMap.createList(anAppContext, this);

    boolean inProcessOrder = false;
    Iterator iter = activeOrders.values().iterator();
    while (iter.hasNext()) {

      AcosOrder thisOrder = (AcosOrder) iter.next();
      if ((!thisOrder.getOrderState(anAppContext).getName(anAppContext).equals(Constants.COMPLETED)) &&
          (!thisOrder.getOrderState(anAppContext).getName(anAppContext).equals(Constants.DENIED))) {
        inProcessOrder = true;
        break;
      }

    }

    if (inProcessOrder) {
      return Constants.MUST_REMAIN;
    }

    //
    // If we get this far, there were no orders in process, so check to see
    //   if there are any marked as deleted orders for this orderCreator.  If
    //   so, then the orderCreator can be marked as deleted.  If there were no
    //   marked as deleted orders, but there were only completed/denied orders,
    //   then the orderCreator can be marked as deleted.
    //
    AcosOrderDBInterface aoTbl =
        new AcosOrderDBInterface(anAppContext);
    if ((aoTbl.existsMarked(anAppContext, this)) ||
        (activeOrders.size() > 0)) {
      return Constants.MARKABLE;
    }

    //
    // If we get this far, there were no orders at all for this orderCreator,
    //   so it can be deleted.
    //
    return Constants.DELETABLE;

  }


  //-----------------------------------------------------------------------------
  // Populate this object from its corresponding database object.
  //-----------------------------------------------------------------------------
  private void populate(AppContext anAppContext, OrdCreatorDBInterface dba) {

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
