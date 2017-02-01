//=============================================================================
//
// Copyright: (C) IBM Corporation 2001 -- IBM Internal Use Only
//
//=============================================================================
//
//    FILE: RoleMap.java
//
// CREATOR: Karen K. Kellam
//    DEPT: 5ZIA
//    DATE: 10/28/2001
//
//-PURPOSE---------------------------------------------------------------------
// RoleMap class definition file.
//-----------------------------------------------------------------------------
//
//
//-CHANGE LOG------------------------------------------------------------------
// 10/28/2001 KKK  Initial coding.
// 12/05/2006 KKW  Added AppContext as first parameter of each method
// 05/21/2007 RAM  Synchronized all public static methods
//=============================================================================

package com.ibm.stg.iipmds.icof.component.role;
import java.util.Iterator;
import java.util.TreeMap;

import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.db.RoleDBInterface;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;


public class RoleMap {


  //-----------------------------------------------------------------------------
  // Create a list containing all roles from database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createList(AppContext anAppContext)
      throws IcofException {

    TreeMap roleList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    RoleDBInterface rTbl = new RoleDBInterface(anAppContext);
    rTbl.openCursor(anAppContext);
    boolean moreData = rTbl.fetchCursor(anAppContext);
    while (moreData) {
      Role role = new Role(anAppContext
                           ,rTbl.getID(anAppContext)
                           ,rTbl.getRoleName(anAppContext));

      roleList.put(role.getMapKey(anAppContext), role);

      moreData = rTbl.fetchCursor(anAppContext);
    }

    return roleList;

  }


  //-----------------------------------------------------------------------------
  // Return the member whose id matches the specified id.
  //-----------------------------------------------------------------------------
  public static synchronized Role getMember(AppContext anAppContext
                               				,TreeMap roleList
                               				,short id) {

    Iterator iter = roleList.values().iterator();
    while (iter.hasNext()) {
      Role role = (Role) iter.next();
      if (role.getID(anAppContext) == id) {
        return role;
      }
    }

    // if we get here, this means no member's id matched the specified id.
    return null;

  }


  //-----------------------------------------------------------------------------
  // Data elements.
  //-----------------------------------------------------------------------------
  private static final String CLASS_NAME = "RoleMap";

}


//==========================  END OF FILE  ====================================
