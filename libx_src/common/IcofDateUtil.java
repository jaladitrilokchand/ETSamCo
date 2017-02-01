/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2005 - 2011 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: IcofDateUtil.java
*
* CREATOR: Karen K. Witt
*    DEPT: 5ZIA
*    DATE: 12/15/2005
*
*-PURPOSE---------------------------------------------------------------------
* IcofDateUtil class definition file.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 12/15/2005 KKW  Initial coding.
* 05/22/2007 RAM  Synchronized all public static methods
* 03/10/2010 KKW  Added addHours method
* 05/12/2011 KKW  Added YYYY_DATE_FORMAT constant.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.common;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class IcofDateUtil {

  // Date format strings
  public static final String DEFAULT_TIMEZONE = "EST";
  public static final String DATE_FORMAT_YY = "MM/dd/yy";
  public static final String DATE_FORMAT_YYYY = "MM/dd/yyyy";
  public static final String TIMESTAMP_FORMAT_PACKET_LIST = "yyyyMMdd HH:mm";
  public static final String TIMESTAMP_FORMAT_LOCK_FILE = "yyyyMMdd HH:mm:ss";
  public static final String ACOS_REQ_ID_DATE_FORMAT = "yyMMdd";
  public static final String ACOS_EXPORT_FILE_DATE_FORMAT = "yyMMddHHmm";
  public static final String ACOS_REQUEST_DATE_FORMAT = "MM/dd/yyyy hh:mm:ss a";
  public static final String ARCHIVE_FILE_DATE_FORMAT = "yyyyMMdd.HHmmss";
  public static final String YYYY_DATE_FORMAT = "yyyyMMdd";


  //-----------------------------------------------------------------------------
  /**
   * Convert a Date object to a string, formatted according to the input string.
   *
   * @param  aDate                a date to be formatted.
   * @param  formatString         a String specifying the format to use.
   *
   * @return                      the formatted date as a String.
   */
  //-----------------------------------------------------------------------------
  public static synchronized String formatDate(Date aDate
                                  ,String formatString) {

    SimpleDateFormat dateFormatter = new SimpleDateFormat(formatString);
    dateFormatter.setTimeZone(TimeZone.getDefault());
    String dateString = dateFormatter.format(aDate);

    return dateString;

  }


  //-----------------------------------------------------------------------------
  /**
   * Convert a string to a Date Object, formatted according to the input string.
   *
   * @param  aStr                 a formatted date string.
   * @param  formatString         a String specifying the format of the date
   *                              string.
   *
   * @return                      the date as a Date object.
   */
  //-----------------------------------------------------------------------------
  public static synchronized Date formatDate(String aStr
                                ,String formatString) {

    Date newDate = new Date();

    SimpleDateFormat simpleDate = new SimpleDateFormat(formatString);
    ParsePosition pos = new ParsePosition(0);

    newDate = (Date)simpleDate.parse(aStr, pos);

    return newDate;

  }


  //-----------------------------------------------------------------------------
  /**
   * Add the specified number of hours to the specified date and return the
   * resulting date.
   * 
   * @param  origDate             the date to which to add the hours
   * @param  hours                the number of hours to add
   *                         
   *
   * @return                      the resulting date.
   */
  //-----------------------------------------------------------------------------
  public static synchronized Date addHours(Date origDate, short hours) {
      
      Calendar resultingDate = Calendar.getInstance();
      resultingDate.setTime(origDate);
      resultingDate.add(Calendar.HOUR, hours);
      return resultingDate.getTime();

  }

  //-----------------------------------------------------------------------------
  // Data elements.
  //-----------------------------------------------------------------------------
  private static final String CLASS_NAME = "IcofDateUtil";


}


//==========================  END OF FILE  ====================================
