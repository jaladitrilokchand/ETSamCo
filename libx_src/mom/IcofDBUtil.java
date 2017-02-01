/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2005 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: IcofDBUtil.java
*
* CREATOR: Karen K. Witt
*    DEPT: AW0V
*    DATE: 12/15/2005
*
*-PURPOSE---------------------------------------------------------------------
* IcofDBUtil class definition file.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 12/15/2005 KKW  Initial coding.
* 02/16/2006 KKW  Moved constants LIKE_ESC_CHAR, LIKE_STRING,
*                 NOT_FOUND, SQL_SUCCESS to DBObject.java.
* 01/08/2007 KKW  Added AppContext as first parameter of most methods
* 03/02/2007 KKW  Moved getDBName method to AppContext class.
* 05/21/2007 RAM  Synchronized all public static methods
* 01/20/2009 KKW  Added handleDBNull methods to handle Integers
* 06/02/2010 KKW  Added DUP_KEY_SQLCODE constant
*=============================================================================
* </pre>
*/

package com.ibm.stg.iipmds.icof.component.mom;
import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofStringUtil;
import com.ibm.stg.iipmds.icof.component.db.DBObject;


public class IcofDBUtil {

  public static final String DUP_KEY_SQLCODE = "SQLCODE: -803";
    

  //-----------------------------------------------------------------------------
  /**
   * Convert a database null value to a short.  This function is
   *   typically used when selecting null numeric fields from a DB2 table.
   *
   * @param  aShort               the value of a nullable numeric field selected
   *                              from a DB2 table (null if the field was null).
   *
   * @return                      the value of the field as a short (-1 if the
   *                              field was null).
   */
  //-----------------------------------------------------------------------------
  public static synchronized short handleDBNull(AppContext anAppContext, Short aShort) {

    if (aShort == null) {
      return Constants.EMPTY;
    }
    else {
      return aShort.shortValue();
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Convert a short into a database null value.  This function is
   *   typically used when inserting null numeric fields into a DB2 table.
   *
   * @param  aNumber              the number to be converted
   *
   * @return                      the value of the field as a Short (null if
   *                              aNumber was -1).
   */
  //-----------------------------------------------------------------------------
  public static synchronized Short handleDBNull(AppContext anAppContext, short aNumber) {

    if (aNumber == Constants.EMPTY) {
      return null;
    }
    else {
      return new Short(aNumber);
    }

  }



  //-----------------------------------------------------------------------------
  /**
   * Convert a database null value to an integer.  This function is
   *   typically used when selecting null numeric fields from a DB2 table.
   *
   * @param  anInteger            the value of a nullable numeric field selected
   *                              from a DB2 table (null if the field was null).
   *
   * @return                      the value of the field as a short (-1 if the
   *                              field was null).
   */
  //-----------------------------------------------------------------------------
  public static synchronized int handleDBNull(AppContext anAppContext, Integer anInteger) {

    if (anInteger == null) {
      return Constants.EMPTY;
    }
    else {
      return anInteger.intValue();
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Convert an integer into a database null value.  This function is
   *   typically used when inserting null numeric fields into a DB2 table.
   *
   * @param  aNumber              the number to be converted
   *
   * @return                      the value of the field as an Integer (null if
   *                              aNumber was -1).
   */
  //-----------------------------------------------------------------------------
  public static synchronized Integer handleDBNull(AppContext anAppContext, int aNumber) {

    if (aNumber == (int) Constants.EMPTY) {
      return null;
    }
    else {
      return new Integer(aNumber);
    }

  }

  //-----------------------------------------------------------------------------
  /**
   * Insert LIKE_ESC_CHARs into searchString to be used in a DB2 LIKE clause.
   *
   * @param   searchString        the string to be used in a DB2 LIKE clause
   * @return                      modified searchString, containing the
   *                              LIKE_ESC_CHAR, plus the original string.
   */
  //-----------------------------------------------------------------------------
  public static synchronized String insertLikeEscChars(AppContext anAppContext, String searchString) {

    String replaceWith = new String(DBObject.LIKE_ESC_CHAR + DBObject.LIKE_STRING);
    return IcofStringUtil.replaceString(searchString, DBObject.LIKE_STRING, replaceWith);

  }


  //-----------------------------------------------------------------------------
  // Data elements.
  //-----------------------------------------------------------------------------
  private static final String CLASS_NAME = "IcofDBUtil";


}


//==========================  END OF FILE  ====================================
