//=============================================================================
//
// Copyright: (C) IBM Corporation 2001 -- IBM Internal Use Only
//
//=============================================================================
//
//    FILE: ProgramMgrMap.java
//
// CREATOR: Karen K. Kellam
//    DEPT: 5ZIA
//    DATE: 02/12/2001
//
//-PURPOSE---------------------------------------------------------------------
// ProgramMgrMap class definition file.
//-----------------------------------------------------------------------------
//
//
//-CHANGE LOG------------------------------------------------------------------
// 02/12/2001 KKK  Initial coding.
// 10/28/2001 KKK  Modified to match new constructors for ProgramMgr.
// 12/05/2006 KKW  Added AppContext as first parameter of each method.
// 05/21/2007 RAM  Synchronized all public static methods
//=============================================================================

package com.ibm.stg.iipmds.icof.component.role;
import java.util.Iterator;
import java.util.TreeMap;

import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.db.PgmMgrDBInterface;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;


public class ProgramMgrMap {


  //-----------------------------------------------------------------------------
  // Create a list containing all program managers from database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createList(AppContext anAppContext)
      throws IcofException {

    TreeMap programMgrList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    PgmMgrDBInterface pmTbl =
      new PgmMgrDBInterface(anAppContext);
    pmTbl.openCursor(anAppContext);
    boolean moreData = pmTbl.fetchCursor(anAppContext);
    while (moreData) {
      ProgramMgr programMgr = new ProgramMgr(anAppContext
                                             ,pmTbl.getID(anAppContext)
                                             ,pmTbl.getFirstName(anAppContext)
                                             ,pmTbl.getLastName(anAppContext)
                                             ,pmTbl.getUserid(anAppContext)
                                             ,pmTbl.getEmailAddr(anAppContext)
                                             ,pmTbl.getPhoneNumber(anAppContext)
                                             ,pmTbl.getIntranetUserid(anAppContext)
                                             ,pmTbl.getCreatedBy(anAppContext)
                                             ,pmTbl.getCreationTmstmp(anAppContext)
                                             ,pmTbl.getUpdatedBy(anAppContext)
                                             ,pmTbl.getUpdatedTmstmp(anAppContext));

      programMgrList.put(programMgr.getMapKey(anAppContext), programMgr);

      moreData = pmTbl.fetchCursor(anAppContext);
    }

    return programMgrList;

  }


  //-----------------------------------------------------------------------------
  // Create a list containing all "marked as deleted" program managers from
  //   database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createMarkedList(AppContext anAppContext)
      throws IcofException {

    TreeMap programMgrList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    PgmMgrDBInterface pmTbl =
      new PgmMgrDBInterface(anAppContext);
    pmTbl.openCursorDeleted(anAppContext);
    boolean moreData = pmTbl.fetchCursorDeleted(anAppContext);
    while (moreData) {
      ProgramMgr programMgr = new ProgramMgr(anAppContext
                                             ,pmTbl.getID(anAppContext)
                                             ,pmTbl.getFirstName(anAppContext)
                                             ,pmTbl.getLastName(anAppContext)
                                             ,pmTbl.getUserid(anAppContext)
                                             ,pmTbl.getEmailAddr(anAppContext)
                                             ,pmTbl.getPhoneNumber(anAppContext)
                                             ,pmTbl.getIntranetUserid(anAppContext)
                                             ,pmTbl.getCreatedBy(anAppContext)
                                             ,pmTbl.getCreationTmstmp(anAppContext)
                                             ,pmTbl.getUpdatedBy(anAppContext)
                                             ,pmTbl.getUpdatedTmstmp(anAppContext)
                                             ,pmTbl.getDeletedBy(anAppContext)
                                             ,pmTbl.getDeletionTmstmp(anAppContext));

      programMgrList.put(programMgr.getMapKey(anAppContext), programMgr);

      moreData = pmTbl.fetchCursorDeleted(anAppContext);
    }

    return programMgrList;

  }


  //-----------------------------------------------------------------------------
  // Return the member whose name matches the specified name.
  //-----------------------------------------------------------------------------
  public static synchronized ProgramMgr getMember(AppContext anAppContext
                                     ,TreeMap programMgrList
                                     ,String fullName) {

    Iterator iter = programMgrList.values().iterator();
    while (iter.hasNext()) {
      ProgramMgr programMgr = (ProgramMgr) iter.next();
      if (programMgr.getFullName(anAppContext).equals(fullName)) {
        return programMgr;
      }
    }

    // if we get here, this means no member's name matched the specified name.
    return null;

  }


  //-----------------------------------------------------------------------------
  // Return the member whose id matches the specified id.
  //-----------------------------------------------------------------------------
  public static synchronized ProgramMgr getMember(AppContext anAppContext
                                     ,TreeMap programMgrList
                                     ,short id) {

    Iterator iter = programMgrList.values().iterator();
    while (iter.hasNext()) {
      ProgramMgr programMgr = (ProgramMgr) iter.next();
      if (programMgr.getID(anAppContext) == id) {
        return programMgr;
      }
    }

    // if we get here, this means no member's id matched the specified id.
    return null;

  }


  //-----------------------------------------------------------------------------
  // Data elements.
  //-----------------------------------------------------------------------------
  private static final String CLASS_NAME = "ProgramMgrMap";

}

//==========================  END OF FILE  ====================================
