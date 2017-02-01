//=============================================================================
//
// Copyright: (C) IBM Corporation 2001 -- IBM Internal Use Only
//
//=============================================================================
//
//    FILE: IcofUserMap.java
//
// CREATOR: Karen K. Kellam
//    DEPT: 5ZIA
//    DATE: 10/28/2001
//
//-PURPOSE---------------------------------------------------------------------
// IcofUserMap class definition file.
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
import com.ibm.stg.iipmds.icof.component.db.IcofUserDBInterface;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;

public class IcofUserMap {


  //-----------------------------------------------------------------------------
  // Create a list containing all icof users from database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createList(AppContext anAppContext)
    throws IcofException {

    TreeMap icofUserList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    IcofUserDBInterface iuTbl =
      new IcofUserDBInterface(anAppContext);
    iuTbl.openCursor(anAppContext);
    boolean moreData = iuTbl.fetchCursor(anAppContext);
    while (moreData) {
      IcofUser icofUser = new IcofUser(anAppContext
                                       ,iuTbl.getID(anAppContext)
                                       ,iuTbl.getFirstName(anAppContext)
                                       ,iuTbl.getLastName(anAppContext)
                                       ,iuTbl.getUserid(anAppContext)
                                       ,iuTbl.getEmailAddr(anAppContext)
                                       ,iuTbl.getPhoneNumber(anAppContext)
                                       ,iuTbl.getIntranetUserid(anAppContext)
                                       ,iuTbl.getCreatedBy(anAppContext)
                                       ,iuTbl.getCreationTmstmp(anAppContext)
                                       ,iuTbl.getUpdatedBy(anAppContext)
                                       ,iuTbl.getUpdatedTmstmp(anAppContext));

      icofUserList.put(icofUser.getMapKey(anAppContext), icofUser);

      moreData = iuTbl.fetchCursor(anAppContext);
    }

    return icofUserList;

  }


  //-----------------------------------------------------------------------------
  // Create a list containing all "marked as deleted" icof users from
  //   database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createMarkedList(AppContext anAppContext)
    throws IcofException {

    TreeMap icofUserList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    IcofUserDBInterface iuTbl =
      new IcofUserDBInterface(anAppContext);
    iuTbl.openCursorDeleted(anAppContext);
    boolean moreData = iuTbl.fetchCursorDeleted(anAppContext);
    while (moreData) {
      IcofUser icofUser = new IcofUser(anAppContext
                                       ,iuTbl.getID(anAppContext)
                                       ,iuTbl.getFirstName(anAppContext)
                                       ,iuTbl.getLastName(anAppContext)
                                       ,iuTbl.getUserid(anAppContext)
                                       ,iuTbl.getEmailAddr(anAppContext)
                                       ,iuTbl.getPhoneNumber(anAppContext)
                                       ,iuTbl.getIntranetUserid(anAppContext)
                                       ,iuTbl.getCreatedBy(anAppContext)
                                       ,iuTbl.getCreationTmstmp(anAppContext)
                                       ,iuTbl.getUpdatedBy(anAppContext)
                                       ,iuTbl.getUpdatedTmstmp(anAppContext)
                                       ,iuTbl.getDeletedBy(anAppContext)
                                       ,iuTbl.getDeletionTmstmp(anAppContext));

      icofUserList.put(icofUser.getMapKey(anAppContext), icofUser);

      moreData = iuTbl.fetchCursorDeleted(anAppContext);
    }

    return icofUserList;

  }


  //-----------------------------------------------------------------------------
  // Return the member whose name matches the specified name.
  //-----------------------------------------------------------------------------
  public static synchronized IcofUser getMember(AppContext anAppContext
                                   ,TreeMap icofUserList
                                   ,String fullName) {

    Iterator iter = icofUserList.values().iterator();
    while (iter.hasNext()) {
      IcofUser icofUser = (IcofUser) iter.next();
      if (icofUser.getFullName(anAppContext).equals(fullName)) {
        return icofUser;
      }
    }

    // if we get here, this means no member's name matched the specified name.
    return null;

  }


  //-----------------------------------------------------------------------------
  // Return the member whose id matches the specified id.
  //-----------------------------------------------------------------------------
  public static synchronized IcofUser getMember(AppContext anAppContext
                                   ,TreeMap icofUserList
                                   ,short id) {

    Iterator iter = icofUserList.values().iterator();
    while (iter.hasNext()) {
      IcofUser icofUser = (IcofUser) iter.next();
      if (icofUser.getID(anAppContext) == id) {
        return icofUser;
      }
    }

    // if we get here, this means no member's id matched the specified id.
    return null;

  }


  //-----------------------------------------------------------------------------
  // Data elements.
  //-----------------------------------------------------------------------------
  private static final String CLASS_NAME = "IcofUserMap";

}


//==========================  END OF FILE  ====================================
