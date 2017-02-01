/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2000 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: IcofException.java
*
* CREATOR: Karen K. Kellam
*    DEPT: 5ZIA
*    DATE: 12/18/2000
*
*-PURPOSE---------------------------------------------------------------------
* IcofException Class.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 12/18/2000 KKK  Initial coding.
* 05/26/2005 KKW  Added printStackTraceAsString(..) methods
* 09/09/2005 KKW  Added javadoc comments
* 12/19/2005 KKW  Added exception constants from the old Constants class.
* 05/22/2007 RAM  Synchronized all public static methods
* 09/17/2008 KKW  Added ieMsg field and getter
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.common;
import java.io.PrintWriter;
import java.io.StringWriter;


public class IcofException extends Exception {

  public static final String INFO = "INFORMATIONAL";
  public static final String WARNING = "WARNING";
  public static final String SEVERE = "SEVERE";
  public static final String TRACE = "TRACE";

  public static final String sep = " -- ";


  //-----------------------------------------------------------------------------
  /**
   * Constructor -- Use this constructor for miscellaneous exceptions that
   *                do not seem to fit either of the other constructors
   *
   * @param     m                 message
   * @param     s                 severity
   *
   */
  //-----------------------------------------------------------------------------
  public IcofException(String m, String s) {

    super(m);
    severity = s;

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor -- Use this constructor for exceptions resulting from
   *                database interactions
   *
   * @param     classname         the name of the class in which the exception
   *                              occurred
   * @param     function          the name of the function in which the
   *                              exception occurred
   * @param     aSeverity         the severity (SEVERE | WARNING | INFO)
   * @param     aMessage          the error message
   * @param     element           the data in which the exception was detected
   * @param     dbTable           the name of the database table being accessed
   *                              when the exception occurred
   *
   */
  //-----------------------------------------------------------------------------
  public IcofException(String className
                       ,String function
                       ,String aSeverity
                       ,String aMessage
                       ,String element
                       ,String dbTable) {

    super("Severity: "       + aSeverity + "\n" +
          "Message: "        + aMessage   + "\n" +
          "Class: "          + className + sep +
          "Function: "       + function  + "\n" +
          "Database Table: " + dbTable   + "\n" +
          "Element: "        + element   + "\n");
    severity = aSeverity;
    ieMsg = aMessage;

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor -- Use this constructor for exceptions that did not
   *                involve database interactions
   *
   * @param     classname         the name of the class in which the exception
   *                              occurred
   * @param     function          the name of the function in which the
   *                              exception occurred
   * @param     aSeverity         the severity (SEVERE | WARNING | INFO)
   * @param     aMessage          the error message
   * @param     element           the data in which the exception was detected
   *
   */
  //-----------------------------------------------------------------------------
  public IcofException(String className
                       ,String function
                       ,String aSeverity
                       ,String aMessage
                       ,String element) {

    super("Severity: "       + aSeverity + "\n" +
          "Message: "        + aMessage   + "\n" +
          "Class: "          + className + sep +
          "Function: "       + function  + "\n" +
          "Element: "        + element   + "\n");
    severity = aSeverity;
    ieMsg = aMessage;

  }


  //-----------------------------------------------------------------------------
  // Member getter functions
  //-----------------------------------------------------------------------------
  /**
   * Get value of severity
   *
   */
  public String getSeverity() { return severity; }
  
  /**
   * Get value of ieMsg
   *
   */
  public String getIeMsg() { 
      if (ieMsg.equals("")) {
          return getMessage();
      }
      else {
          return ieMsg;
      }
  }


  //-----------------------------------------------------------------------------
  /**
   * Capture the output of printStackTrace as a string.
   *
   * @return                      contents of stack trace as a string
   */
  //-----------------------------------------------------------------------------
  public String printStackTraceAsString() {
    StringWriter sw = new StringWriter();

    printStackTrace(new PrintWriter(sw, true));

    String sExc = sw.toString();
    return sExc;
  }


  //-----------------------------------------------------------------------------
  /**
   * Capture the output of printStackTrace as a string.
   *
   * @param     e                 exception that generated the stack trace
   * @return                      contents of stack trace as a string
   */
  //-----------------------------------------------------------------------------
  public static synchronized String printStackTraceAsString(Exception e) {
    StringWriter sw = new StringWriter();

    e.printStackTrace(new PrintWriter(sw, true));

    String sExc = sw.toString();
    return sExc;
  }


  //-----------------------------------------------------------------------------
  // Data Members
  //-----------------------------------------------------------------------------
  private String severity;
  private String ieMsg;

};
//==========================  END OF FILE  ====================================
