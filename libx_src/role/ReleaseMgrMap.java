//=============================================================================
//
// Copyright: (C) IBM Corporation 2001 -- IBM Internal Use Only
//
//=============================================================================
//
//    FILE: ReleaseMgrMap.java
//
// CREATOR: Karen K. Kellam
//    DEPT: 5ZIA
//    DATE: 02/12/2001
//
//-PURPOSE---------------------------------------------------------------------
// ReleaseMgrMap class definition file.
//-----------------------------------------------------------------------------
//
//
//-CHANGE LOG------------------------------------------------------------------
// 02/12/2001 KKK  Initial coding.
// 10/28/2001 KKK  Modified to match new constructors for ReleaseMgr.
// 12/05/2006 KKW  Added AppContext as first parameter of each method.
// 05/21/2007 RAM  Synchronized all public static methods
//=============================================================================

package com.ibm.stg.iipmds.icof.component.role;
import java.util.Iterator;
import java.util.TreeMap;

import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.db.RelMgrDBInterface;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;


public class ReleaseMgrMap {


  //-----------------------------------------------------------------------------
  // Create a list containing all release managers from database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createList(AppContext anAppContext)
      throws IcofException {

    TreeMap releaseMgrList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    RelMgrDBInterface rmTbl =
      new RelMgrDBInterface(anAppContext);
    rmTbl.openCursor(anAppContext);
    boolean moreData = rmTbl.fetchCursor(anAppContext);
    while (moreData) {
      ReleaseMgr releaseMgr = new ReleaseMgr(anAppContext
                                             ,rmTbl.getID(anAppContext)
                                             ,rmTbl.getFirstName(anAppContext)
                                             ,rmTbl.getLastName(anAppContext)
                                             ,rmTbl.getUserid(anAppContext)
                                             ,rmTbl.getEmailAddr(anAppContext)
                                             ,rmTbl.getPhoneNumber(anAppContext)
                                             ,rmTbl.getIntranetUserid(anAppContext)
                                             ,rmTbl.getCreatedBy(anAppContext)
                                             ,rmTbl.getCreationTmstmp(anAppContext)
                                             ,rmTbl.getUpdatedBy(anAppContext)
                                             ,rmTbl.getUpdatedTmstmp(anAppContext));

      releaseMgrList.put(releaseMgr.getMapKey(anAppContext), releaseMgr);

      moreData = rmTbl.fetchCursor(anAppContext);
    }

    return releaseMgrList;

  }


  //-----------------------------------------------------------------------------
  // Create a list containing all "marked as deleted" release managers from
  //   database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createMarkedList(AppContext anAppContext)
      throws IcofException {

    TreeMap releaseMgrList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    RelMgrDBInterface rmTbl =
      new RelMgrDBInterface(anAppContext);
    rmTbl.openCursorDeleted(anAppContext);
    boolean moreData = rmTbl.fetchCursorDeleted(anAppContext);
    while (moreData) {
      ReleaseMgr releaseMgr = new ReleaseMgr(anAppContext
                                             ,rmTbl.getID(anAppContext)
                                             ,rmTbl.getFirstName(anAppContext)
                                             ,rmTbl.getLastName(anAppContext)
                                             ,rmTbl.getUserid(anAppContext)
                                             ,rmTbl.getEmailAddr(anAppContext)
                                             ,rmTbl.getPhoneNumber(anAppContext)
                                             ,rmTbl.getIntranetUserid(anAppContext)
                                             ,rmTbl.getCreatedBy(anAppContext)
                                             ,rmTbl.getCreationTmstmp(anAppContext)
                                             ,rmTbl.getUpdatedBy(anAppContext)
                                             ,rmTbl.getUpdatedTmstmp(anAppContext)
                                             ,rmTbl.getDeletedBy(anAppContext)
                                             ,rmTbl.getDeletionTmstmp(anAppContext));

      releaseMgrList.put(releaseMgr.getMapKey(anAppContext), releaseMgr);

      moreData = rmTbl.fetchCursorDeleted(anAppContext);
    }

    return releaseMgrList;

  }


  //-----------------------------------------------------------------------------
  // Return the member whose name matches the specified name.
  //-----------------------------------------------------------------------------
  public static synchronized ReleaseMgr getMember(AppContext anAppContext
                                     ,TreeMap releaseMgrList
                                     ,String fullName) {

    Iterator iter = releaseMgrList.values().iterator();
    while (iter.hasNext()) {
      ReleaseMgr releaseMgr = (ReleaseMgr) iter.next();
      if (releaseMgr.getFullName(anAppContext).equals(fullName)) {
        return releaseMgr;
      }
    }

    // if we get here, this means no member's name matched the specified name.
    return null;

  }


  //-----------------------------------------------------------------------------
  // Return the member whose id matches the specified id.
  //-----------------------------------------------------------------------------
  public static synchronized ReleaseMgr getMember(AppContext anAppContext
                                     ,TreeMap releaseMgrList
                                     ,short id) {

    Iterator iter = releaseMgrList.values().iterator();
    while (iter.hasNext()) {
      ReleaseMgr releaseMgr = (ReleaseMgr) iter.next();
      if (releaseMgr.getID(anAppContext) == id) {
        return releaseMgr;
      }
    }

    // if we get here, this means no member's id matched the specified id.
    return null;

  }


  //-----------------------------------------------------------------------------
  // Data elements.
  //-----------------------------------------------------------------------------
  private static final String CLASS_NAME = "ReleaseMgrMap";

}

//==========================  END OF FILE  ====================================
