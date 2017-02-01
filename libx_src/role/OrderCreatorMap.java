//=============================================================================
//
// Copyright: (C) IBM Corporation 2001 -- IBM Internal Use Only
//
//=============================================================================
//
//    FILE: OrderCreatorMap.java
//
// CREATOR: Karen K. Kellam
//    DEPT: 5ZIA
//    DATE: 10/28/2001
//
//-PURPOSE---------------------------------------------------------------------
// OrderCreatorMap class definition file.
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
import com.ibm.stg.iipmds.icof.component.db.OrdCreatorDBInterface;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;


public class OrderCreatorMap {


  //-----------------------------------------------------------------------------
  // Create a list containing all order managers from database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createList(AppContext anAppContext)
      throws IcofException {

    TreeMap orderCreatorList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    OrdCreatorDBInterface ocTbl =
      new OrdCreatorDBInterface(anAppContext);
    ocTbl.openCursor(anAppContext);
    boolean moreData = ocTbl.fetchCursor(anAppContext);
    while (moreData) {
      OrderCreator orderCreator = new OrderCreator(anAppContext
                                                   ,ocTbl.getID(anAppContext)
                                                   ,ocTbl.getFirstName(anAppContext)
                                                   ,ocTbl.getLastName(anAppContext)
                                                   ,ocTbl.getUserid(anAppContext)
                                                   ,ocTbl.getEmailAddr(anAppContext)
                                                   ,ocTbl.getPhoneNumber(anAppContext)
                                                   ,ocTbl.getIntranetUserid(anAppContext)
                                                   ,ocTbl.getCreatedBy(anAppContext)
                                                   ,ocTbl.getCreationTmstmp(anAppContext)
                                                   ,ocTbl.getUpdatedBy(anAppContext)
                                                   ,ocTbl.getUpdatedTmstmp(anAppContext));

      orderCreatorList.put(orderCreator.getMapKey(anAppContext), orderCreator);

      moreData = ocTbl.fetchCursor(anAppContext);
    }

    return orderCreatorList;

  }


  //-----------------------------------------------------------------------------
  // Create a list containing all "marked as deleted" order managers from
  //   database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createMarkedList(AppContext anAppContext)
      throws IcofException {

    TreeMap orderCreatorList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    OrdCreatorDBInterface ocTbl =
      new OrdCreatorDBInterface(anAppContext);
    ocTbl.openCursorDeleted(anAppContext);
    boolean moreData = ocTbl.fetchCursorDeleted(anAppContext);
    while (moreData) {
      OrderCreator orderCreator = new OrderCreator(anAppContext
                                                   ,ocTbl.getID(anAppContext)
                                                   ,ocTbl.getFirstName(anAppContext)
                                                   ,ocTbl.getLastName(anAppContext)
                                                   ,ocTbl.getUserid(anAppContext)
                                                   ,ocTbl.getEmailAddr(anAppContext)
                                                   ,ocTbl.getPhoneNumber(anAppContext)
                                                   ,ocTbl.getIntranetUserid(anAppContext)
                                                   ,ocTbl.getCreatedBy(anAppContext)
                                                   ,ocTbl.getCreationTmstmp(anAppContext)
                                                   ,ocTbl.getUpdatedBy(anAppContext)
                                                   ,ocTbl.getUpdatedTmstmp(anAppContext)
                                                   ,ocTbl.getDeletedBy(anAppContext)
                                                   ,ocTbl.getDeletionTmstmp(anAppContext));

      orderCreatorList.put(orderCreator.getMapKey(anAppContext), orderCreator);

      moreData = ocTbl.fetchCursorDeleted(anAppContext);
    }

    return orderCreatorList;

  }


  //-----------------------------------------------------------------------------
  // Return the member whose name matches the specified name.
  //-----------------------------------------------------------------------------
  public static synchronized OrderCreator getMember(AppContext anAppContext
                                       ,TreeMap orderCreatorList
                                       ,String fullName) {

    Iterator iter = orderCreatorList.values().iterator();
    while (iter.hasNext()) {
      OrderCreator orderCreator = (OrderCreator) iter.next();
      if (orderCreator.getFullName(anAppContext).equals(fullName)) {
        return orderCreator;
      }
    }

    // if we get here, this means no member's name matched the specified name.
    return null;

  }


  //-----------------------------------------------------------------------------
  // Return the member whose id matches the specified id.
  //-----------------------------------------------------------------------------
  public static synchronized OrderCreator getMember(AppContext anAppContext
                                       ,TreeMap orderCreatorList
                                       ,short id) {

    Iterator iter = orderCreatorList.values().iterator();
    while (iter.hasNext()) {
      OrderCreator orderCreator = (OrderCreator) iter.next();
      if (orderCreator.getID(anAppContext) == id) {
        return orderCreator;
      }
    }

    // if we get here, this means no member's id matched the specified id.
    return null;

  }


  //-----------------------------------------------------------------------------
  // Data elements.
  //-----------------------------------------------------------------------------
  private static final String CLASS_NAME = "OrderCreatorMap";

}

//==========================  END OF FILE  ====================================
