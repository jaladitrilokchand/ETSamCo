//=============================================================================
//
// Copyright: (C) IBM Corporation 2001 -- IBM Internal Use Only
//
//=============================================================================
//
//    FILE: OrderMgrMap.java
//
// CREATOR: Karen K. Kellam
//    DEPT: 5ZIA
//    DATE: 10/28/2001
//
//-PURPOSE---------------------------------------------------------------------
// OrderMgrMap class definition file.
//-----------------------------------------------------------------------------
//
//
//-CHANGE LOG------------------------------------------------------------------
// 10/28/2001 KKK  Initial coding.
// 12/05/2006 KKW  Added AppContext as first parameter of each method.
// 05/21/2007 RAM  Synchronized all public static methods
//=============================================================================

package com.ibm.stg.iipmds.icof.component.role;
import java.util.Iterator;
import java.util.TreeMap;

import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.db.OrdMgrDBInterface;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;


public class OrderMgrMap {


  //-----------------------------------------------------------------------------
  // Create a list containing all order managers from database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createList(AppContext anAppContext)
      throws IcofException {

    TreeMap orderMgrList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    OrdMgrDBInterface omTbl =
      new OrdMgrDBInterface(anAppContext);
    omTbl.openCursor(anAppContext);
    boolean moreData = omTbl.fetchCursor(anAppContext);
    while (moreData) {
      OrderMgr orderMgr = new OrderMgr(anAppContext
                                       ,omTbl.getID(anAppContext)
                                       ,omTbl.getFirstName(anAppContext)
                                       ,omTbl.getLastName(anAppContext)
                                       ,omTbl.getUserid(anAppContext)
                                       ,omTbl.getEmailAddr(anAppContext)
                                       ,omTbl.getPhoneNumber(anAppContext)
                                       ,omTbl.getIntranetUserid(anAppContext)
                                       ,omTbl.getCreatedBy(anAppContext)
                                       ,omTbl.getCreationTmstmp(anAppContext)
                                       ,omTbl.getUpdatedBy(anAppContext)
                                       ,omTbl.getUpdatedTmstmp(anAppContext));

      orderMgrList.put(orderMgr.getMapKey(anAppContext), orderMgr);

      moreData = omTbl.fetchCursor(anAppContext);
    }

    return orderMgrList;

  }


  //-----------------------------------------------------------------------------
  // Create a list containing all "marked as deleted" order managers from
  //   database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createMarkedList(AppContext anAppContext)
      throws IcofException {

    TreeMap orderMgrList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    OrdMgrDBInterface omTbl =
      new OrdMgrDBInterface(anAppContext);
    omTbl.openCursorDeleted(anAppContext);
    boolean moreData = omTbl.fetchCursorDeleted(anAppContext);
    while (moreData) {
      OrderMgr orderMgr = new OrderMgr(anAppContext
                                       ,omTbl.getID(anAppContext)
                                       ,omTbl.getFirstName(anAppContext)
                                       ,omTbl.getLastName(anAppContext)
                                       ,omTbl.getUserid(anAppContext)
                                       ,omTbl.getEmailAddr(anAppContext)
                                       ,omTbl.getPhoneNumber(anAppContext)
                                       ,omTbl.getIntranetUserid(anAppContext)
                                       ,omTbl.getCreatedBy(anAppContext)
                                       ,omTbl.getCreationTmstmp(anAppContext)
                                       ,omTbl.getUpdatedBy(anAppContext)
                                       ,omTbl.getUpdatedTmstmp(anAppContext)
                                       ,omTbl.getDeletedBy(anAppContext)
                                       ,omTbl.getDeletionTmstmp(anAppContext));

      orderMgrList.put(orderMgr.getMapKey(anAppContext), orderMgr);

      moreData = omTbl.fetchCursorDeleted(anAppContext);
    }

    return orderMgrList;

  }


  //-----------------------------------------------------------------------------
  // Return the member whose name matches the specified name.
  //-----------------------------------------------------------------------------
  public static synchronized OrderMgr getMember(AppContext anAppContext
                                   ,TreeMap orderMgrList
                                   ,String fullName) {

    Iterator iter = orderMgrList.values().iterator();
    while (iter.hasNext()) {
      OrderMgr orderMgr = (OrderMgr) iter.next();
      if (orderMgr.getFullName(anAppContext).equals(fullName)) {
        return orderMgr;
      }
    }

    // if we get here, this means no member's name matched the specified name.
    return null;

  }


  //-----------------------------------------------------------------------------
  // Return the member whose id matches the specified id.
  //-----------------------------------------------------------------------------
  public static synchronized OrderMgr getMember(AppContext anAppContext
                                   ,TreeMap orderMgrList
                                   ,short id) {

    Iterator iter = orderMgrList.values().iterator();
    while (iter.hasNext()) {
      OrderMgr orderMgr = (OrderMgr) iter.next();
      if (orderMgr.getID(anAppContext) == id) {
        return orderMgr;
      }
    }

    // if we get here, this means no member's id matched the specified id.
    return null;

  }


  //-----------------------------------------------------------------------------
  // Data elements.
  //-----------------------------------------------------------------------------
  private static final String CLASS_NAME = "OrderMgrMap";

}

//==========================  END OF FILE  ====================================
