//=============================================================================
//
// Copyright: (C) IBM Corporation 2000 -- IBM Internal Use Only
//
//=============================================================================
//
//    FILE: DBAdd (was DBReadOnly and before that IcofReadOnlyObject.java)
//
// CREATOR: Karen K. Kellam
//    DEPT: 5ZIA
//    DATE: 10/15/2001
//
//-PURPOSE---------------------------------------------------------------------
// IcofReadOnlyObject class definition file.
//-----------------------------------------------------------------------------
//
//
//-CHANGE LOG------------------------------------------------------------------
// 10/15/2001 KKK  Initial coding.
// 08/30/2006 KPL  Changed from class to interface (removed data members and 
//                 method bodies), renamed from IcofReadOnlyObject to 
//                 DBAdd
// 10/06/2006 KKW  Moved audit info getters/setters to IcofObject class; split
//                 into DBAdd and DBDelete interfaces
//=============================================================================

package com.ibm.stg.iipmds.icof.component.mom;

import com.ibm.stg.iipmds.common.IcofException;

public interface DBAdd {


  //-----------------------------------------------------------------------------
  // Required functions for children classes
  //-----------------------------------------------------------------------------
  public void dbAdd(AppContext anAppContext) throws IcofException;
  public String addOrReinstate(AppContext anAppContext) throws IcofException;

}

//==========================  END OF FILE  ====================================
