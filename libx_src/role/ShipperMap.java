//=============================================================================
//
// Copyright: (C) IBM Corporation 2001 -- IBM Internal Use Only
//
//=============================================================================
//
//    FILE: ShipperMap.java
//
// CREATOR: Karen K. Kellam
//    DEPT: 5ZIA
//    DATE: 02/12/2001
//
//-PURPOSE---------------------------------------------------------------------
// ShipperMap class definition file.
//-----------------------------------------------------------------------------
//
//
//-CHANGE LOG------------------------------------------------------------------
// 02/12/2001 KKK  Initial coding.
// 10/28/2001 KKK  Modified to match new constructors for Shipper.
// 09/14/2006 KPL  Added AppContext as firt param throughout, added exception
//                 logging
// 05/21/2007 RAM  Synchronized all public static methods
//=============================================================================

package com.ibm.stg.iipmds.icof.component.role;
import java.util.Iterator;
import java.util.TreeMap;

import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.db.ShipperDBInterface;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;


public class ShipperMap {


  //-----------------------------------------------------------------------------
  // Create a list containing all shippers from database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createList(AppContext anAppContext)
      throws IcofException {

    TreeMap shipperList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    ShipperDBInterface smTbl =
      new ShipperDBInterface(anAppContext);
    smTbl.openCursor(anAppContext);
    boolean moreData = smTbl.fetchCursor(anAppContext);
    while (moreData) {
      Shipper shipper = new Shipper(anAppContext
                                    ,smTbl.getID(anAppContext)
                                    ,smTbl.getFirstName(anAppContext)
                                    ,smTbl.getLastName(anAppContext)
                                    ,smTbl.getUserid(anAppContext)
                                    ,smTbl.getEmailAddr(anAppContext)
                                    ,smTbl.getPhoneNumber(anAppContext)
                                    ,smTbl.getIntranetUserid(anAppContext)
                                    ,smTbl.getCreatedBy(anAppContext)
                                    ,smTbl.getCreationTmstmp(anAppContext)
                                    ,smTbl.getUpdatedBy(anAppContext)
                                    ,smTbl.getUpdatedTmstmp(anAppContext));

      shipperList.put(shipper.getMapKey(anAppContext), shipper);

      moreData = smTbl.fetchCursor(anAppContext);
    }

    return shipperList;

  }


  //-----------------------------------------------------------------------------
  // Create a list containing all "marked as deleted" shippers from
  //   database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createMarkedList(AppContext anAppContext)
      throws IcofException {

    TreeMap shipperList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    ShipperDBInterface smTbl =
      new ShipperDBInterface(anAppContext);
    smTbl.openCursorDeleted(anAppContext);
    boolean moreData = smTbl.fetchCursorDeleted(anAppContext);
    while (moreData) {
      Shipper shipper = new Shipper(anAppContext
                                    ,smTbl.getID(anAppContext)
                                    ,smTbl.getFirstName(anAppContext)
                                    ,smTbl.getLastName(anAppContext)
                                    ,smTbl.getUserid(anAppContext)
                                    ,smTbl.getEmailAddr(anAppContext)
                                    ,smTbl.getPhoneNumber(anAppContext)
                                    ,smTbl.getIntranetUserid(anAppContext)
                                    ,smTbl.getCreatedBy(anAppContext)
                                    ,smTbl.getCreationTmstmp(anAppContext)
                                    ,smTbl.getUpdatedBy(anAppContext)
                                    ,smTbl.getUpdatedTmstmp(anAppContext)
                                    ,smTbl.getDeletedBy(anAppContext)
                                    ,smTbl.getDeletionTmstmp(anAppContext));

      shipperList.put(shipper.getMapKey(anAppContext), shipper);

      moreData = smTbl.fetchCursorDeleted(anAppContext);
    }

    return shipperList;

  }


  //-----------------------------------------------------------------------------
  // Return the member whose name matches the specified name.
  //-----------------------------------------------------------------------------
  public static synchronized Shipper getMember(AppContext anAppContext
                                  			  ,TreeMap shipperList
                                  			  ,String fullName) {

    Iterator iter = shipperList.values().iterator();
    while (iter.hasNext()) {
      Shipper shipper = (Shipper) iter.next();
      if (shipper.getFullName(anAppContext).equals(fullName)) {
        return shipper;
      }
    }

    // if we get here, this means no member's name matched the specified name.
    return null;

  }


  //-----------------------------------------------------------------------------
  // Return the member whose id matches the specified id.
  //-----------------------------------------------------------------------------
  public static synchronized Shipper getMember(AppContext anAppContext
                                  			  ,TreeMap shipperList
                                  			  ,short id) {

    Iterator iter = shipperList.values().iterator();
    while (iter.hasNext()) {
      Shipper shipper = (Shipper) iter.next();
      if (shipper.getID(anAppContext) == id) {
        return shipper;
      }
    }

    // if we get here, this means no member's id matched the specified id.
    return null;

  }


  //-----------------------------------------------------------------------------
  // Data elements.
  //-----------------------------------------------------------------------------
  private static final String CLASS_NAME = "ShipperMap";

}

//==========================  END OF FILE  ====================================
