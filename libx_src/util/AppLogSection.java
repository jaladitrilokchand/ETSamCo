/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2005 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: AppLogSection.java
*
* CREATOR: Karen K. Witt
*    DEPT: AW0V
*    DATE: 05/26/2005
*
*-PURPOSE---------------------------------------------------------------------
* AppLogSection class definition file.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/26/2005 KKW  Initial coding.
* 07/19/2005 KKW  Corrected some comments.
* 10/28/2005 KKW  Removed extra ; in function getAppTimestampStr().
* 12/15/2005 KKW  Modified due to splitting of Constants.java into several
*                 *Util classes.
* 12/06/2006 KKW  Added AppContext as first parameter of each method; updated
*                 due to changes in base class.
*=============================================================================
* </pre>
*/

package com.ibm.stg.iipmds.icof.component.util;
import java.util.Date;
import java.util.Vector;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofDateUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.mom.IcofObject;


public class AppLogSection extends IcofObject {


  //-----------------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param     anAppName         the name of the application that is using
   *                              this log file (either reading or updating)
   *
   */
  //-----------------------------------------------------------------------------
  public AppLogSection(AppContext anAppContext, String anAppName) {

    super(anAppContext);
    setAppName(anAppContext, anAppName);
    setAppTimestamp(anAppContext, new Date());
    setMsgList(anAppContext, new Vector());
    setReturnCode(anAppContext, 0);
    setMapKey(anAppContext, anAppName);

  }


  //-----------------------------------------------------------------------------
  // Member "getter" functions
  //-----------------------------------------------------------------------------
  /**
   * Get value of appName
   *
   */
  public String getAppName(AppContext anAppContext) { return appName; }

  /**
   * Get value of appTimestamp
   *
   */
  public Date getAppTimestamp(AppContext anAppContext) { return appTimestamp; }

  /**
   * Get value of appTimestamp as a formatted string
   *
   */
  public String getAppTimestampStr(AppContext anAppContext) {
    return IcofDateUtil.formatDate(getAppTimestamp(anAppContext)
                                   ,IcofDateUtil.ARCHIVE_FILE_DATE_FORMAT);
  }

  /**
   * Get value of msgList
   *
   */
  public Vector getMsgList(AppContext anAppContext) { return msgList; }

  /**
   * Get value of return code
   *
   */
  public int getReturnCode(AppContext anAppContext) { return returnCode; }


  //-----------------------------------------------------------------------------
  /**
   * Get contents of AppLogSection as a string
   *
   * @return                      Contents of this object as a String
   */
  //-----------------------------------------------------------------------------
  public String asString(AppContext anAppContext) throws IcofException {

    String logData = Constants.SECTION_DIVIDER + "\n"
                   + getAppName(anAppContext) + "\n"
                   + getAppTimestampStr(anAppContext) + "\n\n"
                   + IcofCollectionsUtil.getVectorAsString(getMsgList(anAppContext), "\n")
                   + "\n" + Constants.RETURN_CODE_TAG
                   + ":" + String.valueOf(getReturnCode(anAppContext)) + "\n"
                   + Constants.SECTION_DIVIDER + "\n";

    return logData;

  }


  //-----------------------------------------------------------------------------
  /**
   * Format contents of AppLogSection as a vector, including section dividers
   *   and blank lines.  This vector could then be passed to an IcofFile
   *   write function.
   *
   * @return                      Contents of this object as a Vector
   */
  //-----------------------------------------------------------------------------
  public Vector format(AppContext anAppContext) {

    Vector contents = new Vector();
    contents.add(Constants.SECTION_DIVIDER);
    contents.add(getAppName(anAppContext));
    contents.add(getAppTimestampStr(anAppContext));
    contents.add(" ");
    contents.addAll(getMsgList(anAppContext));
    contents.add(" ");
    contents.add(Constants.RETURN_CODE_TAG
                   + ":" + String.valueOf(getReturnCode(anAppContext)));
    contents.add(Constants.SECTION_DIVIDER);

    return contents;

  }


  //-----------------------------------------------------------------------------
  /**
   * Add a message for this application's section of the log
   *
   * @param     msg               The message to be added
   *
   */
  //-----------------------------------------------------------------------------
  public void appendMsg(AppContext anAppContext, String msg) {

    getMsgList(anAppContext).add(msg);

  }


  //-----------------------------------------------------------------------------
  // Data members
  //-----------------------------------------------------------------------------
  protected String         appName;
  protected Date           appTimestamp;
  protected Vector         msgList;
  protected int            returnCode;


  //-----------------------------------------------------------------------------
  // Member "setter" functions
  //-----------------------------------------------------------------------------
  /**
   * Set value of appName
   *
   */
  protected void setAppName(AppContext anAppContext, String aString) {
    appName = aString;
  }

  /**
   * Set value of appTimestamp
   *  
   */
  public void setAppTimestamp(AppContext anAppContext, Date aDate) {
    appTimestamp = aDate;
  }

  /**
   * Set value of appTimestamp
   *  
   */
  public void setAppTimestamp(AppContext anAppContext, String aString) {
    appTimestamp = IcofDateUtil.formatDate(aString
                                           ,IcofDateUtil.ARCHIVE_FILE_DATE_FORMAT);
  }

  /**
   * Set value of msgList
   *  
   */
  public void setMsgList(AppContext anAppContext, Vector aVector) {
    msgList = aVector;
  }

  /**
   * Set value of return code
   *  
   */
  public void setReturnCode(AppContext anAppContext, int anInt) {
    returnCode = anInt;
  }

}

//==========================  END OF FILE  ====================================
