//=============================================================================
//
// Copyright: (C) IBM Corporation 2001 -- IBM Internal Use Only
//
//=============================================================================
//
//    FILE: IcofUserRoleMap.java
//
// CREATOR: Karen K. Kellam
//    DEPT: 5ZIA
//    DATE: 10/29/2001
//
//-PURPOSE---------------------------------------------------------------------
// IcofUserRoleMap class definition file.
//-----------------------------------------------------------------------------
//
//
//-CHANGE LOG------------------------------------------------------------------
// 10/29/2001 KKK  Initial coding.
// 12/05/2006 KKW  Added AppContext as first parameter of each method
//=============================================================================

package com.ibm.stg.iipmds.icof.component.role;
import java.util.Iterator;
import java.util.TreeMap;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.db.IcofUserRoleDBInterface;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;


public class IcofUserRoleMap {


  //-----------------------------------------------------------------------------
  // Create a list containing all icof user roles from database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createList(AppContext anAppContext)
      throws IcofException {

    TreeMap icofUserRoleList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    IcofUserRoleDBInterface iurTbl =
      new IcofUserRoleDBInterface(anAppContext);
    iurTbl.openCursor(anAppContext);
    boolean moreData = iurTbl.fetchCursor(anAppContext);
    while (moreData) {
      IcofUserRole icofUserRole =
          new IcofUserRole(anAppContext
                           ,iurTbl.getIcofUserID(anAppContext)
                           ,iurTbl.getIntranetUserid(anAppContext)
                           ,iurTbl.getRoleID(anAppContext)
                           ,iurTbl.getRoleName(anAppContext)
                           ,iurTbl.getCreatedBy(anAppContext)
                           ,iurTbl.getCreationTmstmp(anAppContext));
      icofUserRoleList.put(icofUserRole.getMapKey(anAppContext), icofUserRole);
      moreData = iurTbl.fetchCursor(anAppContext);
    }

    return icofUserRoleList;

  }


  //-----------------------------------------------------------------------------
  // Create a list containing all icof user roles for the specified user
  //   from database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createList(AppContext anAppContext
                                                ,IcofUser anIcofUser)
      throws IcofException {

    TreeMap icofUserRoleList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    IcofUserRoleDBInterface iurTbl =
      new IcofUserRoleDBInterface(anAppContext);
    iurTbl.openCursor(anAppContext, anIcofUser);
    boolean moreData = iurTbl.fetchCursorIcofUser(anAppContext);
    while (moreData) {
      IcofUserRole icofUserRole =
          new IcofUserRole(anAppContext
                           ,iurTbl.getIcofUserID(anAppContext)
                           ,iurTbl.getIntranetUserid(anAppContext)
                           ,iurTbl.getRoleID(anAppContext)
                           ,iurTbl.getRoleName(anAppContext)
                           ,iurTbl.getCreatedBy(anAppContext)
                           ,iurTbl.getCreationTmstmp(anAppContext));
      icofUserRoleList.put(icofUserRole.getMapKey(anAppContext), icofUserRole);
      moreData = iurTbl.fetchCursorIcofUser(anAppContext);
    }

    return icofUserRoleList;

  }


  //-----------------------------------------------------------------------------
  // Create a list containing all icof user roles for the specified role
  //   from database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createList(AppContext anAppContext
                                                ,Role aRole)
      throws IcofException {

    TreeMap icofUserRoleList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    IcofUserRoleDBInterface iurTbl =
      new IcofUserRoleDBInterface(anAppContext);
    iurTbl.openCursor(anAppContext, aRole);
    boolean moreData = iurTbl.fetchCursorRole(anAppContext);
    while (moreData) {
      IcofUserRole icofUserRole =
          new IcofUserRole(anAppContext
                           ,iurTbl.getIcofUserID(anAppContext)
                           ,iurTbl.getIntranetUserid(anAppContext)
                           ,iurTbl.getRoleID(anAppContext)
                           ,iurTbl.getRoleName(anAppContext)
                           ,iurTbl.getCreatedBy(anAppContext)
                           ,iurTbl.getCreationTmstmp(anAppContext));
      icofUserRoleList.put(icofUserRole.getMapKey(anAppContext), icofUserRole);
      moreData = iurTbl.fetchCursorRole(anAppContext);
    }

    return icofUserRoleList;

  }


  //-----------------------------------------------------------------------------
  // Create a list containing all "marked as deleted" icof user roles from
  //   database
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap createMarkedList(AppContext anAppContext)
     throws IcofException {

    TreeMap icofUserRoleList = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    IcofUserRoleDBInterface iurTbl =
      new IcofUserRoleDBInterface(anAppContext);
    iurTbl.openCursorDeleted(anAppContext);
    boolean moreData = iurTbl.fetchCursorDeleted(anAppContext);
    while (moreData) {
      IcofUserRole icofUserRole =
          new IcofUserRole(anAppContext
                           ,iurTbl.getIcofUserID(anAppContext)
                           ,iurTbl.getIntranetUserid(anAppContext)
                           ,iurTbl.getRoleID(anAppContext)
                           ,iurTbl.getRoleName(anAppContext)
                           ,iurTbl.getCreatedBy(anAppContext)
                           ,iurTbl.getCreationTmstmp(anAppContext)
                           ,iurTbl.getDeletedBy(anAppContext)
                           ,iurTbl.getDeletionTmstmp(anAppContext));
      icofUserRoleList.put(icofUserRole.getMapKey(anAppContext), icofUserRole);
      moreData = iurTbl.fetchCursorDeleted(anAppContext);
    }

    return icofUserRoleList;

  }

  
  //--------------------------------------------------------------------------
  /**
   * Construct the to list containing the icof admins.  Requires a database
   *   connection.
   * 
   * @param anAppContext  application context -- includes db connection and
   *                      log file
   * @return              a comma-delimited list (string) of the email
   *                      addresses to send the email to                  
   * @exception IcofException                     
   */
  //--------------------------------------------------------------------------
  public static synchronized String constructIcofAdminToList(AppContext anAppContext)
  throws IcofException {
      
      Role icofAdminRole = new Role(anAppContext, Constants.ICOF_ADMIN);
      return constructToListForRole(anAppContext, icofAdminRole);
  }

  
  //--------------------------------------------------------------------------
  /**
   * Construct a comma-delimited list containing the email addresses for all 
   *   users of the specified role.  
   *   Requires a database connection.
   * 
   * @param anAppContext  application context -- includes db connection and
   *                      log file
   * @param aRole         the role for which to construct an email to-list.                     
   * @return              a comma-delimited list (string) of the email
   *                      addresses to send the email to                  
   * @exception IcofException                     
   */
  //--------------------------------------------------------------------------
  public static synchronized String constructToListForRole(AppContext anAppContext, 
                                                           Role aRole)
  throws IcofException {
      
      // Get the users that match the specified role
      TreeMap roleMembers = IcofUserRoleMap.createList(anAppContext, aRole);
      
      // Now construct the "to" list
      String toList = "";
      Iterator iter = roleMembers.values().iterator();
      while (iter.hasNext()) {
          IcofUserRole user = (IcofUserRole) iter.next();
          toList += user.getIntranetUserid(anAppContext) + ",";
      }
      return toList;
  }


  //-----------------------------------------------------------------------------
  // Data elements.
  //-----------------------------------------------------------------------------
  private static final String CLASS_NAME = "IcofUserRoleMap";

}

//==========================  END OF FILE  ====================================
