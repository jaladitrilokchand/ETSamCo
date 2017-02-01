//=============================================================================
//
// Copyright: (C) IBM Corporation 2000 -- IBM Internal Use Only
//
//=============================================================================
//
//    FILE: DBUpdate.java
//
// CREATOR: Karen K. Kellam
//    DEPT: 5ZIA
//    DATE: 10/15/2001
//
//-PURPOSE---------------------------------------------------------------------
// DBUpdate interface definition file.
//-----------------------------------------------------------------------------
//
//
//-CHANGE LOG------------------------------------------------------------------
// 10/15/2001 KKK  Initial coding.
// 08/30/2006 KPL  Changed from class to interface (removed data members and 
//                 method bodies), renamed from IcofUpdateObject to DBUpdate
// 10/06/2006 KKW  Moved audit info getters/setters to IcofObject class
//=============================================================================

package com.ibm.stg.iipmds.icof.component.mom;

import com.ibm.stg.iipmds.common.IcofException;


public interface DBUpdate {

	
  //-----------------------------------------------------------------------------
  // Required functions for children classes
  //-----------------------------------------------------------------------------
  public void dbUpdate(AppContext anAppContext) throws IcofException;

}

//==========================  END OF FILE  ====================================
