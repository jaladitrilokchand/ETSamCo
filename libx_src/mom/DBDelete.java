/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2000 - 2009 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: DBDelete.java
*
* CREATOR: Karen K. Witt
*    DEPT: AW0V
*    DATE: 10/06/2006
*
*-PURPOSE---------------------------------------------------------------------
* DBDelete interface definition file.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 10/06/2006 KKW  Initial coding. Split out from DBAdd.
* 02/05/2009 KKW  Added testDeletability, deleteAssociations,
*                 markAssociations methods
*=============================================================================
* </pre>
*/

package com.ibm.stg.iipmds.icof.component.mom;

import com.ibm.stg.iipmds.common.IcofException;

public interface DBDelete {


  //-----------------------------------------------------------------------------
  // Required functions for children classes
  //-----------------------------------------------------------------------------
  public void dbDelete(AppContext anAppContext) throws IcofException;
  public void dbMark(AppContext anAppContext) throws IcofException;
  public void dbUnmark(AppContext anAppContext) throws IcofException;
  public String testDeletability(AppContext anAppContext) throws IcofException;
  public void deleteAssociations(AppContext anAppContext, boolean deleteRow) throws IcofException;
  public void markAssociations(AppContext anAppContext, boolean markRow) throws IcofException;
}

//==========================  END OF FILE  ====================================
