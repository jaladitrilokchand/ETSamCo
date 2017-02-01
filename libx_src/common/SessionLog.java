/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2006 - 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *    FILE: SessionLog.java
 *
 * CREATOR: Ryan A. Morgan
 *    DEPT: AW0V
 *    DATE: 07/06/2006
 *
 *-PURPOSE---------------------------------------------------------------------
 * SessionLog should be instantiated by a class that needs to log information
 * about the currently running application.
 * 
 *-----------------------------------------------------------------------------
 *
 *
 **-CHANGE LOG-----------------------------------------------------------------
 * 07/06/2006	RAM		Initial Coding
 * 01/04/2007	RAM		Added ConsoleAppender option to account for
 * 						applications that use AppContext but don't need to make
 * 						use of this logging option.
 * 04/15/2008 KKW  Updated to use IcofException.SEVERE instead of 
 *                 Constants.SEVERE when throwing exceptions.             
 * 02/05/2009 KKW  Added support for JUNIT appMode to avoid getting warnings
 *                 about defaulting to dev for log file location.   
 * 10/20/2009 AS   Fixed an issue in "log(IcofException ie)" method.
 * 04/09/2010 KKW  Added setPath(String, String) method
 * 04/23/2010 KKW  Added call to IcofSystemUtil.getHostName() in writeHeader()
 *                 method
 * 05/27/2010 KKW  Updated so that log messages are instantly flushed to file.
 *                 Functionality does not appear to be working, though.
 * 06/14/2010 KKW  Updated so that header and trailer are written using log4j,
 *                 instead of separately.  This seemed to be interfering with
 *                 the immediate flush of the log file. Removed logFile member,
 *                 its getters/setters.  Obsoleted ILog.java and removed the
 *                 "implements ILog" clause.                                             
 *=============================================================================
 * </pre>
 *
 */

package com.ibm.stg.eda.component.common;

import java.io.IOException;
import java.util.Date;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;

/**
 * 
 * @author Ryan A. Morgan July 7, 2006
 * 
 * <p>
 * SessionLog should be instantiated by a class that wishes to create a log for
 * the running application. This class wraps the functionality of Log4j (open
 * source) for IBM use. It allows for a set of levels to be defined as currently
 * valid for logging. The minimum logging level can be changed or turned off all
 * together. This feature allows certain messages to be written to file based on
 * what "level" of importance the message has been assigned. More than one
 * SessionLog can be instantiated, each with a different output path and level
 * set if desired. Conversion patterns allow the output to be written in a
 * certain form. The documentation below describes how conversion patterns can
 * be used. This information is from the log4j PatternLayout.
 * </p>
 * <p>
 * The conversion pattern is closely related to the conversion pattern of the
 * printf function in C. A conversion pattern is composed of literal text and
 * format control expressions called <em>conversion specifiers</em>.
 * 
 * <p>
 * <i>You are free to insert any literal text within the conversion pattern.</i>
 * 
 * <p>
 * Each conversion specifier starts with a percent sign (%) and is followed by
 * optional <em>format modifiers</em> and a <em>conversion
 character</em>.
 * The conversion character specifies the type of data, e.g. category, priority,
 * date, thread name. The format modifiers control such things as field width,
 * padding, left and right justification. The following is a simple example.
 * 
 * <p>
 * Let the conversion pattern be <b>"%-5p [%t]: %m%n"</b> and assume that the
 * log4j environment was set to use a PatternLayout. Then the statements
 * 
 * <pre>
 * Category root = Category.getRoot();
 * root.debug(&quot;Message 1&quot;);
 * root.warn(&quot;Message 2&quot;);
 * </pre>
 * 
 * would yield the output
 * 
 * <pre>
 *  DEBUG [main]: Message 1
 *  WARN  [main]: Message 2
 * </pre>
 * 
 * <p>
 * Note that there is no explicit separator between text and conversion
 * specifiers. The pattern parser knows when it has reached the end of a
 * conversion specifier when it reads a conversion character. In the example
 * above the conversion specifier <b>%-5p</b> means the priority of the logging
 * event should be left justified to a width of five characters.
 * 
 * The recognized conversion characters are
 * 
 * <p>
 * <table border="1" CELLPADDING="8">
 * <th>Conversion Character</th>
 * <th>Effect</th>
 * 
 * <tr>
 * <td align=center><b>c</b></td>
 * 
 * <td>Used to output the category of the logging event. The category
 * conversion specifier can be optionally followed by
 * <em>precision specifier</em>, that is a decimal constant in brackets.
 * 
 * <p>
 * If a precision specifier is given, then only the corresponding number of
 * right most components of the category name will be printed. By default the
 * category name is printed in full.
 * 
 * <p>
 * For example, for the category name "a.b.c" the pattern <b>%c{2}</b> will
 * output "b.c".
 * 
 * </td>
 * </tr>
 * 
 * <tr>
 * <td align=center><b>C</b></td>
 * 
 * <td>Used to output the fully qualified class name of the caller issuing the
 * logging request. This conversion specifier can be optionally followed by
 * <em>precision specifier</em>, that is a decimal constant in brackets.
 * 
 * <p>
 * If a precision specifier is given, then only the corresponding number of
 * right most components of the class name will be printed. By default the class
 * name is output in fully qualified form.
 * 
 * <p>
 * For example, for the class name "org.apache.xyz.SomeClass", the pattern
 * <b>%C{1}</b> will output "SomeClass".
 * 
 * <p>
 * <b>WARNING</b> Generating the caller class information is slow. Thus, it's
 * use should be avoided unless execution speed is not an issue.
 * 
 * </td>
 * </tr>
 * 
 * <tr>
 * <td align=center><b>d</b></td>
 * <td>Used to output the date of the logging event. The date conversion
 * specifier may be followed by a <em>date format specifier</em> enclosed
 * between braces. For example, <b>%d{HH:mm:ss,SSS}</b> or
 * <b>%d{dd&nbsp;MMM&nbsp;yyyy&nbsp;HH:mm:ss,SSS}</b>. If no date format
 * specifier is given then ISO8601 format is assumed.
 * 
 * <p>
 * The date format specifier admits the same syntax as the time pattern string
 * of the java.text.SimpleDateFormat. Although part of the standard JDK, the
 * performance of <code>SimpleDateFormat</code> is quite poor.
 * 
 * <p>
 * For better results it is recommended to use the log4j date formatters. These
 * can be specified using one of the strings "ABSOLUTE", "DATE" and "ISO8601"
 * for specifying org.apache.log4j.helpers.AbsoluteTimeDateFormat,
 * org.apache.log4j.helpers.DateTimeDateFormat and respectively
 * org.apache.log4j.helpers.ISO8601DateFormat For example, <b>%d{ISO8601}</b>
 * or <b>%d{ABSOLUTE}</b>.
 * 
 * <p>
 * These dedicated date formatters perform significantly better than
 * java.text.SimpleDateFormat. </td>
 * </tr>
 * 
 * <tr>
 * <td align=center><b>F</b></td>
 * 
 * <td>Used to output the file name where the logging request was issued.
 * 
 * <p>
 * <b>WARNING</b> Generating caller location information is extremely slow.
 * It's use should be avoided unless execution speed is not an issue.
 * 
 * </tr>
 * 
 * <tr>
 * <td align=center><b>l</b></td>
 * 
 * <td>Used to output location information of the caller which generated the
 * logging event.
 * 
 * <p>
 * The location information depends on the JVM implementation but usually
 * consists of the fully qualified name of the calling method followed by the
 * callers source the file name and line number between parentheses.
 * 
 * <p>
 * The location information can be very useful. However, it's generation is
 * <em>extremely</em> slow. It's use should be avoided unless execution speed
 * is not an issue.
 * 
 * </td>
 * </tr>
 * 
 * <tr>
 * <td align=center><b>L</b></td>
 * 
 * <td>Used to output the line number from where the logging request was
 * issued.
 * 
 * <p>
 * <b>WARNING</b> Generating caller location information is extremely slow.
 * It's use should be avoided unless execution speed is not an issue.
 * 
 * </tr>
 * 
 * 
 * <tr>
 * <td align=center><b>m</b></td>
 * <td>Used to output the application supplied message associated with the
 * logging event.</td>
 * </tr>
 * 
 * <tr>
 * <td align=center><b>M</b></td>
 * 
 * <td>Used to output the method name where the logging request was issued.
 * 
 * <p>
 * <b>WARNING</b> Generating caller location information is extremely slow.
 * It's use should be avoided unless execution speed is not an issue.
 * 
 * </tr>
 * 
 * <tr>
 * <td align=center><b>n</b></td>
 * 
 * <td>Outputs the platform dependent line separator character or characters.
 * 
 * <p>
 * This conversion character offers practically the same performance as using
 * non-portable line separator strings such as "\n", or "\r\n". Thus, it is the
 * preferred way of specifying a line separator.
 * 
 * 
 * </tr>
 * 
 * <tr>
 * <td align=center><b>p</b></td>
 * <td>Used to output the priority of the logging event.</td>
 * </tr>
 * 
 * <tr>
 * 
 * <td align=center><b>r</b></td>
 * 
 * <td>Used to output the number of milliseconds elapsed since the start of the
 * application until the creation of the logging event.</td>
 * </tr>
 * 
 * 
 * <tr>
 * <td align=center><b>t</b></td>
 * 
 * <td>Used to output the name of the thread that generated the logging event.</td>
 * 
 * </tr>
 * 
 * <tr>
 * 
 * <td align=center><b>x</b></td>
 * 
 * <td>Used to output the NDC (nested diagnostic context) associated with the
 * thread that generated the logging event. </td>
 * </tr>
 * 
 * 
 * <tr>
 * <td align=center><b>X</b></td>
 * 
 * <td>
 * 
 * <p>
 * Used to output the MDC (mapped diagnostic context) associated with the thread
 * that generated the logging event. The <b>X</b> conversion character
 * <em>must</em> be followed by the key for the map placed between braces, as
 * in <b>%X{clientNumber}</b> where <code>clientNumber</code> is the key. The
 * value in the MDC corresponding to the key will be output.
 * </p>
 * 
 * <p>
 * See MDC class for more details.
 * </p>
 * 
 * </td>
 * </tr>
 * 
 * <tr>
 * 
 * <td align=center><b>%</b></td>
 * 
 * <td>The sequence %% outputs a single percent sign. </td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * By default the relevant information is output as is. However, with the aid of
 * format modifiers it is possible to change the minimum field width, the
 * maximum field width and justification.
 * 
 * <p>
 * The optional format modifier is placed between the percent sign and the
 * conversion character.
 * 
 * <p>
 * The first optional format modifier is the <em>left justification
 flag</em>
 * which is just the minus (-) character. Then comes the optional
 * <em>minimum field width</em> modifier. This is a decimal constant that
 * represents the minimum number of characters to output. If the data item
 * requires fewer characters, it is padded on either the left or the right until
 * the minimum width is reached. The default is to pad on the left (right
 * justify) but you can specify right padding with the left justification flag.
 * The padding character is space. If the data item is larger than the minimum
 * field width, the field is expanded to accommodate the data. The value is
 * never truncated.
 * 
 * <p>
 * This behavior can be changed using the <em>maximum field
 width</em>
 * modifier which is designated by a period followed by a decimal constant. If
 * the data item is longer than the maximum field, then the extra characters are
 * removed from the <em>beginning</em> of the data item and not from the end.
 * For example, it the maximum field width is eight and the data item is ten
 * characters long, then the first two characters of the data item are dropped.
 * This behavior deviates from the printf function in C where truncation is done
 * from the end.
 * 
 * <p>
 * Below are various format modifier examples for the category conversion
 * specifier.
 * 
 * <p>
 * <TABLE BORDER=1 CELLPADDING=8>
 * <th>Format modifier
 * <th>left justify
 * <th>minimum width
 * <th>maximum width
 * <th>comment
 * 
 * <tr>
 * <td align=center>%20c</td>
 * <td align=center>false</td>
 * <td align=center>20</td>
 * <td align=center>none</td>
 * 
 * <td>Left pad with spaces if the category name is less than 20 characters
 * long.
 * 
 * <tr>
 * <td align=center>%-20c</td>
 * <td align=center>true</td>
 * <td align=center>20</td>
 * <td align=center>none</td>
 * <td>Right pad with spaces if the category name is less than 20 characters
 * long.
 * 
 * <tr>
 * <td align=center>%.30c</td>
 * <td align=center>NA</td>
 * <td align=center>none</td>
 * <td align=center>30</td>
 * 
 * <td>Truncate from the beginning if the category name is longer than 30
 * characters.
 * 
 * <tr>
 * <td align=center>%20.30c</td>
 * <td align=center>false</td>
 * <td align=center>20</td>
 * <td align=center>30</td>
 * 
 * <td>Left pad with spaces if the category name is shorter than 20 characters.
 * However, if category name is longer than 30 characters, then truncate from
 * the beginning.
 * 
 * <tr>
 * <td align=center>%-20.30c</td>
 * <td align=center>true</td>
 * <td align=center>20</td>
 * <td align=center>30</td>
 * 
 * <td>Right pad with spaces if the category name is shorter than 20
 * characters. However, if category name is longer than 30 characters, then
 * truncate from the beginning.
 * 
 * </table>
 * 
 * <p>
 * Below are some examples of conversion patterns.
 * 
 * <dl>
 * 
 * <p>
 * <dt><b>%r [%t] %-5p %c %x - %m\n</b>
 * <p>
 * <dd>This is essentially the TTCC layout.
 * 
 * <p>
 * <dt><b>%-6r [%15.15t] %-5p %30.30c %x - %m\n</b>
 * 
 * <p>
 * <dd>Similar to the TTCC layout except that the relative time is right padded
 * if less than 6 digits, thread name is right padded if less than 15 characters
 * and truncated if longer and the category name is left padded if shorter than
 * 30 characters and truncated if longer.
 * 
 * </dl>
 * 
 * -CHANGE LOG------------------------------------------------------------------
 *  05/09/2008 AAK  Created new constructor so the file name can be specified
 *  
 */
public class SessionLog {
    // ----------CONSTANTS----------------------------------------------------------
    
    
    public static final String DIVIDER_LN = 
        "===================================================================";

    /**
     * DEFAULT_PATTERN_CONVERSION is the default conversion pattern used by
     * PatternLayout. Format: Level [thread]: message Example: DEBUG [main]:
     * Entering application
     * 
     */
    public static final String DEFAULT_CONVERSION_PATTERN = "%-5p [%t]: %m%n";
    /**
     * DETAILED_PATTERN_CONVERSION is the detailed conversion pattern used by
     * PatternLayout. Format: line# [thread] Level class_name - message Example:
     * 0 [main] INFO Foo - Entering application.
     */
    public static final String DETAILED_CONVERSION_PATTERN = "%d{HH:mm:ss} [%t] %-5p %c %x - %m%n";
    /**
     * TRACE:Log4j defined value for logging. The level(severity) order: TRACE,
     * DEBUG, INFO, WARN, ERROR, FATAL
     */
    public static final String TRACE = "TRACE";
    /**
     * DEBUG:Log4j defined value for logging. The level(severity) order: TRACE,
     * DEBUG, INFO, WARN, ERROR, FATAL
     */
    public static final String DEBUG = "DEBUG";
    /**
     * INFO:Log4j defined value for logging. The level(severity) order: TRACE,
     * DEBUG, INFO, WARN, ERROR, FATAL
     */
    public static final String INFO = "INFO";
    /**
     * WARN:Log4j defined value for logging. The level(severity) order: TRACE,
     * DEBUG, INFO, WARN, ERROR, FATAL
     */
    public static final String WARN = "WARN";
    /**
     * ERROR:Log4j defined value for logging. The level(severity) order: TRACE,
     * DEBUG, INFO, WARN, ERROR, FATAL
     */
    public static final String ERROR = "ERROR";
    /**
     * FATAL:Log4j defined value for logging. The level(severity) order: TRACE,
     * DEBUG, INFO, WARN, ERROR, FATAL
     */
    public static final String FATAL = "FATAL";
    /**
     * ALL
     * 
     * Value provided for logging all statements. This constant should only be
     * used when passing to setLevel() when needed.
     */
    public static final String ALL = "ALL";
    /**
     * OFF
     * 
     * Value provided for logging NO statements. This constant should only be
     * used when passing to setLevel() when needed.
     */
    public static final String OFF = "OFF";
    // ----------DATAMEMBERS--------------------------------------------------------
    protected Logger logger;
    protected Date timestamp;
    protected boolean activeSessionLog;
    protected boolean fileAppenderInd;
    protected String funcName;
    protected String fileName;
    protected String path;
    protected String createdBy;
    protected String appName;
    protected String conversionPattern;
    protected String nativeMinimumLevel;
    protected FileAppender fileAppender;
    protected ConsoleAppender consoleAppender;
    /**
     * ALL_LEVELS is a TreeMap (whose key is a String level) used to determine
     * whether or not a message was logged. The TreeMap allows a comparison of
     * greater than or equal to for whether or not a message was logged.
     */
    protected TreeMap ALL_LEVELS = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    // -----------CONSTRUCTS--------------------------------------------------------
    /**
     * Construct the Session Log Copy constructor copies the SessionLog object,
     * but not the file
     * 
     * @param s
     *            The SessionLog to be copied
     */
    // -----------------------------------------------------------------------------
    public SessionLog(SessionLog s) throws IcofException {
        this(s.getAppName(), s.getCreatedBy(), "", s.getFileAppenderInd(), s
             .getNativeMinimumLevel(), s.getConversionPattern(), s.getPath());
    }

    // -----------------------------------------------------------------------------
    /**
     * Construct the Session Log Empty constructor. This constructor should be
     * used only to instantiate SessionLog outside of a try, catch block (where
     * it will be reassigned). This constructor does not provide any
     * functionality and WILL cause an exception to be thrown when a log() is
     * attempted.
     */
    public SessionLog() {
    }

    // -----------------------------------------------------------------------------
    /**
     * Construct the Session Log This constructor sets the logging level to
     * SessionLog.WARN and uses the default ConversionPattern for logging
     * messages. The logging path, if using a fileAppender, will be determined
     * by the appMode paramter.
     * 
     * @param appName
     *            The name of the application creating the .log file
     * @param userID
     * @param appMode
     *            The application mode (i.e. ICC, PROD, DEV...)
     * @param aFileAppenderInd
     *            True if logging to FileAppender, false if logging to
     *            ConsoleAppender
     * 
     * @throws IcofException
     *             if the dir cannot be validated or created, if the PrintWriter
     *             contains errors, if the named file exists but is a directory
     *             rather than a regular file, does not exist but cannot be
     *             created, or cannot be opened for any other reason
     */
    public SessionLog(String appName, String userID, String appMode,
                      boolean aFileAppenderInd) throws IcofException {
        this(appName, userID, appMode, aFileAppenderInd, TRACE);
    }

    // -----------------------------------------------------------------------------
    /**
     * Construct the Session Log Fully paramterized constructor.
     * 
     * @param appName
     *            The name of the application creating the .log file
     * @param userID
     * @param appMode
     *            The application mode (i.e. ICC, PROD, DEV...)
     * @param aFileAppenderInd
     *            True if logging to FileAppender, false if logging to
     *            ConsoleAppender
     * @param minimumLevel
     *            The lowest level that the logger will log. Use a constant from
     *            this class for efficiency.
     * @throws IcofException
     *             if the dir cannot be validated or created, if the PrintWriter
     *             contains errors, if the named file exists but is a directory
     *             rather than a regular file, does not exist but cannot be
     *             created, or cannot be opened for any other reason.
     */
    // -----------------------------------------------------------------------------
    public SessionLog(String appName, String userID, String appMode,
                      boolean aFileAppenderInd, String minimumLevel) throws IcofException {
        this(appName, userID, appMode, aFileAppenderInd, minimumLevel,
             DEFAULT_CONVERSION_PATTERN);

    }

    // -----------------------------------------------------------------------------
    /**
     * Construct the Session Log Fully paramterized with the added feature of
     * setting the conversionPattern String. An extra String is required to
     * allow this constructor to exist since. Default minimum logging level is
     * WARN - it will be set. The same functionality can be achieved by calling
     * a different constructor and then setting the conversion pattern via
     * setConversionPattern().
     * 
     * @param appName
     *            The name of the application creating the .log file
     * @param userID
     * @param appMode
     *            The application mode (i.e. ICC, PROD, DEV...)
     * @param aFileAppenderInd
     *            True if logging to FileAppender, false if logging to
     *            ConsoleAppender
     * @param minimumLevel
     *            The lowest level that the logger will log. Use a constant from
     *            this class for efficiency.
     * @param conversionPattern
     *            The pattern in which to log statements. Two are defined in
     *            this class or the user can specify one.
     * @throws IcofException
     *             if the dir cannot be validated or created, if the PrintWriter
     *             contains errors, if the named file exists but is a directory
     *             rather than a regular file, does not exist but cannot be
     *             created, or cannot be opened for any other reason.
     */
    // -----------------------------------------------------------------------------
    public SessionLog(String appName, String userID, String appMode,
                      boolean aFileAppenderInd, String minimumLevel,
                      String conversionPattern) throws IcofException {
        this(appName, userID, appMode, aFileAppenderInd, minimumLevel,
             conversionPattern, "");

    }

    // -----------------------------------------------------------------------------
    /**
     * Construct the Session Log Fully paramterized with the added feature of
     * setting the conversionPattern String. Default minimum logging level is
     * WARN - it will be set. The same functionality can be achieved by calling
     * a different constructor and then setting the conversion pattern via
     * setConversionPattern().
     * 
     * @param appName
     *            The name of the application creating the .log file
     * @param userID
     * @param appMode
     *            The application mode (i.e. ICC, PROD, DEV...)
     * @param aFileAppenderInd
     *            True if logging to FileAppender, false if logging to
     *            ConsoleAppender
     * @param minimumLevel
     *            The lowest level that the logger will log. Use a constant from
     *            this class for efficiency.
     * @param conversionPattern
     *            The pattern in which to log statements. Two are offered by
     *            this class. The user can specify their own.
     * @param path
     *            The path where the SessionLog file will be located. Insert a
     *            blank String or use a different constructor if you wish to
     *            base the path on the appMode.
     * @param fileName
     *            The name of the log file
     * @throws IcofException
     *             if the dir cannot be validated or created, if the PrintWriter
     *             contains errors, if the named file exists but is a directory
     *             rather than a regular file, does not exist but cannot be
     *             created, or cannot be opened for any other reason.
     */
    // -----------------------------------------------------------------------------
    public SessionLog(String appName, String userID, String appMode,
                      boolean aFileAppenderInd, String minimumLevel,
                      String conversionPattern, String path, String afileName)
    throws IcofException {

        // Set Application Name. Default to NONAME
        if (appName == null) {
            appName = "NONAME";
        } else if (appName.equals("")) {
            appName = "NONAME";
        }
        this.setAppName(appName);

        // Set createdBy default to user running the instance of SessionLog
        if (userID == null) {
            this.setCreatedBy(System
                              .getProperty(Constants.USER_NAME_PROPERTY_TAG));
        } else if (userID.equals("")) {
            this.setCreatedBy(System
                              .getProperty(Constants.USER_NAME_PROPERTY_TAG));
        } else {
            this.setCreatedBy(userID);
        }

        // Set timestamp as current date and time
        this.setTimestamp(new Date());

        // Determine path to create .log file. If defaulted to DEV path set
        // defaultedToDev true so that message can be used later
        boolean defaultedToDev = false;
        if (path != null && !path.equals("")) {
            this.setPath(path);
        } else {
            defaultedToDev = setPath(appMode, appName);
        }

        // Set the filename if it was passed in
        if (!afileName.equals("") && afileName != null) {
            setFileName(afileName);
        }

        // Create Logger instance with unique name to avoid Loggers with same
        // address
        this.logger = Logger.getLogger("SessionLog"
                                       + (int) (Math.random() * 999999));

        // Set the FileAppenderIndicator so that the SessionLog can be activated
        // properly
        this.setFileAppenderInd(aFileAppenderInd);

        // Activate the SessionLog instance
        this.activateSessionLog();

        // Set the level to the parameter passed to the construtor
        this.setLevel(minimumLevel);

        // Set conversion pattern for logging
        setConversionPattern(conversionPattern);
        
        // Write the header to the log file
        if (getFileAppenderInd()) {
            this.writeHeader();
        }

        // Log message indicating reason for defaulted location of SessionLog
        // file
        // and print message to console if necessary
        if (defaultedToDev && getFileAppenderInd()) {
            String tempLevel = getNativeMinimumLevel();

            String msg = "Invalid application mode passed to SessionLog. " +
            "Logging is defaulting to: " + Constants.DEV_AIM_ICOF_LOG;

            log(SessionLog.INFO, msg, SessionLog.ALL);
            setLevel(tempLevel);
            System.out.println(msg);
        }
    }

    // -----------------------------------------------------------------------------
    /**
     * Construct the Session Log Fully parameterized with the added feature of
     * setting the conversionPattern String. Default minimum logging level is
     * WARN - it will be set. The same functionality can be achieved by calling
     * a different constructor and then setting the conversion pattern via
     * setConversionPattern().
     * 
     * @param appName
     *            The name of the application creating the .log file
     * @param userID
     * @param appMode
     *            The application mode (i.e. ICC, PROD, DEV...)
     * @param aFileAppenderInd
     *            True if logging to FileAppender, false if logging to
     *            ConsoleAppender
     * @param minimumLevel
     *            The lowest level that the logger will log. Use a constant from
     *            this class for efficiency.
     * @param conversionPattern
     *            The pattern in which to log statements. Two are offered by
     *            this class. The user can specify their own.
     * @param path
     *            The path where the SessionLog file will be located. Insert a
     *            blank String or use a different constructor if you wish to
     *            base the path on the appMode.
     * @throws IcofException
     *             if the dir cannot be validated or created, if the PrintWriter
     *             contains errors, if the named file exists but is a directory
     *             rather than a regular file, does not exist but cannot be
     *             created, or cannot be opened for any other reason.
     */
    // -----------------------------------------------------------------------------
    public SessionLog(String appName, String userID, String appMode,
                      boolean aFileAppenderInd, String minimumLevel,
                      String conversionPattern, String path) throws IcofException {

        this(appName, userID, appMode, aFileAppenderInd, minimumLevel,
             conversionPattern, path, "");
    }

    // -----------GETTERS-----------------------------------------------------------
    /**
     * Returns the name of the application that instaniated SessionLog
     * 
     * @return appName the application name
     * 
     */
    // -----------------------------------------------------------------------------
    public String getAppName() {
        return appName;
    }

    // -----------------------------------------------------------------------------
    /**
     * Returns the name of the caller of this instantiated SessionLog
     * 
     * @return createdBy the creator
     * 
     */
    public String getCreatedBy() {
        return createdBy;
    }

    // -----------------------------------------------------------------------------
    /**
     * Returns the conversion pattern for the PatternLayout used in the Log4j
     * package
     * 
     * @return conversionPattern the pattern used by PatternLayout.java
     */
    // -----------------------------------------------------------------------------
    public String getConversionPattern() {
        return conversionPattern;
    }

    // -----------------------------------------------------------------------------
    /**
     * Returns the date held by the timestamp attribute
     * 
     * @return timestamp the date
     * 
     */
    // -----------------------------------------------------------------------------
    public Date getTimestamp() {
        return timestamp;
    }

    // -----------------------------------------------------------------------------
    /**
     * Returns the path as a String
     * 
     * @return path the path (location of .log file)
     * 
     */
    // -----------------------------------------------------------------------------
    public String getPath() {
        return path;
    }

    // -----------------------------------------------------------------------------
    /**
     * Returns the name of the .log file
     * 
     * @return fileName .log file name
     * 
     */
    // -----------------------------------------------------------------------------
    public String getFileName() {
        return fileName;
    }

    // -----------------------------------------------------------------------------
    /**
     * Returns the full path, including the file name
     * 
     * @return fullPath the full path containing the location of the .log file
     */
    // -----------------------------------------------------------------------------
    public String getFullPath() {
        return (path + "/" + fileName);
    }

    // -----------------------------------------------------------------------------
    /**
     * Get the current minimum log level for internal use. The native attribute
     * nativeMinimumLevel allows for boolean returns on log() to indicate
     * whether or not the log() call logged to file or not (based on a
     * comparison of severity level and currentMinimumLevel)
     * 
     * @return nativeMinimumLevel The current level set for minimum logging. It
     *         is synonymous with log4j's current Level set
     */
    // -----------------------------------------------------------------------------
    public String getNativeMinimumLevel() {
        return this.nativeMinimumLevel;
    }

    // -----------------------------------------------------------------------------
    /**
     * Returns the name of the last executed function (method)
     * 
     * @return funcName the name of the function
     * 
     */
    // -----------------------------------------------------------------------------
    public String getFuncName() {
        return funcName;
    }

    // -----------------------------------------------------------------------------
    /**
     * Returns the name of this class
     * 
     * @return "SessionLog" the name of this class
     * 
     */
    // -----------------------------------------------------------------------------
    public String getClassName() {
        return "SessionLog";
    }

    // -----------------------------------------------------------------------------
    /**
     * Returns the fileAppenderIndicator If the indicator is true the SessionLog
     * is using a FileAppender, if it is false then the SessionLog is using a
     * ConsoleAppender
     * 
     * @return fileAppenderInd FileAppender indicator
     * 
     */
    // -----------------------------------------------------------------------------
    public boolean getFileAppenderInd() {
        return fileAppenderInd;
    }

    // --------------SETTERS--------------------------------------------------------
    /**
     * Sets the name of the application that instantiated this class
     * 
     * @param appName
     *            the application name
     * 
     */
    // -----------------------------------------------------------------------------
    protected void setAppName(String appName) {
        this.appName = appName;
    }

    // -----------------------------------------------------------------------------
    /**
     * Sets the name of the creator of this class instance
     * 
     * @param createdBy
     *            the creator
     * 
     */
    // -----------------------------------------------------------------------------
    protected void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    // -----------------------------------------------------------------------------
    /**
     * Sets the conversion pattern for the PatternLayout used in the Log4j
     * package. If the pattern is null or "", the default will be used. It is up
     * to the user to determine a conversion pattern that works if neither of
     * the constants defined in this class are used.
     * 
     * @param conversionPattern
     *            the pattern used by PatternLayout.java
     */
    // -----------------------------------------------------------------------------
    public void setConversionPattern(String conversionPattern) {
        if (conversionPattern == null) {
            conversionPattern = "INVALIDPATTERN";
        }
        if (conversionPattern.equals("INVALIDPATTERN")
                        || conversionPattern.equals("")) {
            log(
                SessionLog.TRACE,
            "Invalid conversion pattern submitted to setConversionPattern(). No change in pattern.");
        } else {
            this.conversionPattern = conversionPattern;
            if (getFileAppenderInd()) {
                fileAppender.setLayout(new PatternLayout(conversionPattern));
            } else {
                consoleAppender.setLayout(new PatternLayout(conversionPattern));
            }
        }
    }

    // -----------------------------------------------------------------------------
    /**
     * Sets the date (used at to mark the start and finish of the .log file's
     * creation)
     * 
     * @param timestamp
     *            the date and time
     * 
     */
    // -----------------------------------------------------------------------------
    protected void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // -----------------------------------------------------------------------------
    /**
     * Sets the path that the .log file will be written to
     * 
     * @param path
     *            the path to write the .log file to
     * 
     */
    // -----------------------------------------------------------------------------
    protected void setPath(String path) {
        this.path = path;
    }

    // -----------------------------------------------------------------------------
    /**
     * Construct the path for the SessionLog file, based on the specified appMode
     *   and appName
     * 
     * @param appMode
     * @param appName
     * @return true if logging is defaulted to the dev location; false, if
     *         not
     * @throws IcofException
     *             
     */
    // -----------------------------------------------------------------------------
    private boolean setPath(String appMode, String appName) throws IcofException {

        // Determine path to create .log file. If defaulted to DEV path set
        // defaultedToDev true.
        boolean defaultedToDev = false;
        if (appMode == null) {
            appMode = "INVALIDAPPMODE";
        }

        String logPath = IcofSystemUtil.determineHighLevelQualifier(appMode);
        if ((appMode.equals(Constants.PROD)) || (appMode.equals(Constants.ICCPROD))) {
            logPath += "/" + Constants.PROD_AIM_ICOF_LOG;
        } 
        else if ((appMode.equals(Constants.DEV)) || (appMode.equals(Constants.JUNIT))
                        || (appMode.equals(Constants.ICCTEST))) {
            logPath += "/" + Constants.DEV_AIM_ICOF_LOG;
        } 
        else {
            logPath += "/" + Constants.DEV_AIM_ICOF_LOG;
            defaultedToDev = true;
        }
        logPath += "/" + appName;
        setPath(logPath);
        return defaultedToDev;
    }

    // -----------------------------------------------------------------------------
    /**
     * Sets the .log file name
     * 
     * @param fileName
     *            name of the .log file
     * 
     */
    // -----------------------------------------------------------------------------
    protected void setFileName(String fileName) {
        this.fileName = fileName;
    }

    // -----------------------------------------------------------------------------
    /**
     * Sets the name of the function last executed
     * 
     * @param funcName
     *            name of the function
     * 
     */
    // -----------------------------------------------------------------------------
    protected void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    // -----------------------------------------------------------------------------
    /**
     * Set the FileAppender Indicator.
     * 
     * @param anInd
     *            true if using a FileAppender; false if using a ConsoleAppender
     * 
     */
    // -----------------------------------------------------------------------------
    protected void setFileAppenderInd(boolean anInd) {
        this.fileAppenderInd = anInd;
    }

    // -----------------------------------------------------------------------------
    /**
     * Change the level that is necessary for SessionLog to write to file. The
     * order is as follows: ALL TRACE DEBUG INFO WARN ERROR FATAL OFF. If the
     * level is set at WARN, then WARN, ERROR, and FATAL messages will be
     * logged. If an invalid value is passed, the logging level will be set to
     * WARN.
     * 
     * @param newLevel
     *            New logging level (i.e. logging set to the INFO level will be
     *            changed to the newLevel to yield different appending to the
     *            file).
     */
    // -----------------------------------------------------------------------------
    public void setLevel(String newLevel) {
        if (newLevel == null) {
            newLevel = "INVALIDLEVEL";
        }

        if (newLevel.equals(OFF)) {
            logger.setLevel(Level.OFF);
            this.setNativeMinimumLevel(OFF);
        } else if (newLevel.equals(TRACE)) {
            logger.setLevel(Level.TRACE);
            this.setNativeMinimumLevel(TRACE);
        } else if (newLevel.equals(DEBUG)) {
            logger.setLevel(Level.DEBUG);
            this.setNativeMinimumLevel(DEBUG);
        } else if (newLevel.equals(INFO)) {
            logger.setLevel(Level.INFO);
            this.setNativeMinimumLevel(INFO);
        } else if (newLevel.equals(WARN)) {
            logger.setLevel(Level.WARN);
            this.setNativeMinimumLevel(WARN);
        } else if (newLevel.equals(ERROR)) {
            logger.setLevel(Level.ERROR);
            this.setNativeMinimumLevel(ERROR);
        } else if (newLevel.equals(FATAL)) {
            logger.setLevel(Level.FATAL);
            this.setNativeMinimumLevel(FATAL);
        } else if (newLevel.equals(ALL)) {
            logger.setLevel(Level.ALL);
            this.setNativeMinimumLevel(ALL);
        } else {
            // Default to warn and log message
            log(WARN,
                "Invalid minimum log level set: default to WARN. All loggings "
                + "below level WARN will not be logged to file.",
                WARN);
        }
    }

    // -----------------------------------------------------------------------------
    /**
     * Activates the sessionlog by creating the PrintWriter and file name
     * 
     * @throws IcofException
     *             if the dir cannot be validated or created, if the PrintWriter
     *             contains errors, if the named file exists but is a directory
     *             rather than a regular file, does not exist but cannot be
     *             created, or cannot be opened for any other reason
     */
    // -----------------------------------------------------------------------------
    protected void activateSessionLog() throws IcofException {

        this.fillALL_LEVELS();
        BasicConfigurator.configure();
        if (getFileAppenderInd()) {
            IcofFile logDir = new IcofFile(getPath(), true);
            logDir.validate(true);

            // If the file name is not set, then create it.
            if (getFileName() == null || getFileName().equals("")) {
                this.createFileName();
            }
            

            // Open the file with the Log4j Logger
            try {
                fileAppender = new FileAppender(new PatternLayout(DEFAULT_CONVERSION_PATTERN), this.getFullPath());
                fileAppender.setImmediateFlush(true);
                fileAppender.activateOptions();
                logger.addAppender(fileAppender);
                logger.setAdditivity(false);

            } catch (IOException e) {
                IcofException ie = new IcofException(getClassName(),
                                                     getFuncName(), IcofException.SEVERE,
                                                     "Unable to open session log file via Log4j"
                                                     + e.getMessage(), getFullPath());

                throw ie;
            }
        } else {
            consoleAppender = new ConsoleAppender(new PatternLayout(
                                                                    DEFAULT_CONVERSION_PATTERN));
            consoleAppender.setImmediateFlush(true);
            consoleAppender.activateOptions();
            logger.addAppender(consoleAppender);
            logger.setAdditivity(false);
        }
    }

    // -----------------------------------------------------------------------------
    /**
     * Fills the local TreeMap with the String constants for severity levels.
     * Each constant has an Integer number associated with it as a value.
     */
    // -----------------------------------------------------------------------------
    private void fillALL_LEVELS() {
        ALL_LEVELS.put(OFF, new Integer(0));
        ALL_LEVELS.put(TRACE, new Integer(1));
        ALL_LEVELS.put(DEBUG, new Integer(2));
        ALL_LEVELS.put(INFO, new Integer(3));
        ALL_LEVELS.put(WARN, new Integer(4));
        ALL_LEVELS.put(ERROR, new Integer(5));
        ALL_LEVELS.put(FATAL, new Integer(6));
        ALL_LEVELS.put(ALL, new Integer(7));
    }

    // -----------------------------------------------------------------------------
    /**
     * Determines if the call of the log() method resulted in a write to file.
     * Returns true or false;
     * 
     * @param severityLevel
     *            The severity level for a log attempt
     * @return wasLogged Wether or not the message was logged
     */
    // -----------------------------------------------------------------------------
    private boolean wasLogged(String severityLevel) {
        Integer int1 = (Integer) ALL_LEVELS.get(severityLevel);
        Integer int2 = (Integer) ALL_LEVELS.get(getNativeMinimumLevel());
        if (int1 == null || int2 == null) {
            // To ensure that the TreeMap of levels is filled and to eliminate
            // infinite looping.
            if (int2 == null) {
                this.fillALL_LEVELS();
                if (int1 == null && int2 == null) {
                    System.out
                    .println("There is an error with the logging levels with logger:"
                             + " whose fileName is:"
                             + this.getFileName()
                             + ". The comparator"
                             + " levels were likely to not have been set. To avoid this problem being"
                             + " repeated, they have been set. To avoid an infinite loop, an error message"
                             + " logging this information has been ommitted. If this message is repeated"
                             + " for the same logger contact the manager of this logging system.");
                    return false;
                }
            }
            String temp = getNativeMinimumLevel();
            log(
                WARN,
                "The severity level was not found in the TreeMap or the TreeMap containing the "
                + " information for comparing severity levels contains one or more null values. The "
                + " indicator of whether or not a message was logged may yield an inaccurate return.",
                WARN);
            setLevel(temp);
            return true;
        }
        if (int1.compareTo(int2) < 0) {
            return false;
        } else {
            return true;
        }
    }

    // -----------------------------------------------------------------------------
    /**
     * Create the name of the .log file based on the instance's attribute values
     * following the file naming convention
     */
    // -----------------------------------------------------------------------------
    protected void createFileName() {
        int randomNum = (int) (Math.random() * 999999);
        fileName = this.getAppName() + "_";
        String fileName2 = IcofDateUtil.formatDate(timestamp,
                                                   IcofDateUtil.ARCHIVE_FILE_DATE_FORMAT);
        int separator = fileName2.indexOf(".");
        // appends fileName with yyyyMMdd_
        fileName += fileName2.substring(0, separator) + "_";
        // appends filename with hhMMss_
        fileName += fileName2.substring(separator + 1, fileName2.length())
        + "_";
        fileName += this.getCreatedBy() + "_" + randomNum + "."
        + Constants.LOG_EXTENSION;
    }

    // -----------------------------------------------------------------------------
    /**
     * Write the header of the .log file to the path
     */
    // -----------------------------------------------------------------------------
    protected void writeHeader() throws IcofException {
    
      StringBuffer header = new StringBuffer("\n");
      header.append(DIVIDER_LN);
      header.append("\n");
      header.append("Application: ");
      header.append(this.getAppName());
      header.append("\n");
      header.append("Host name: ");
      header.append(IcofSystemUtil.getHostName());
      header.append("\n");
      header.append("Creator: ");
      header.append(this.getCreatedBy());
      header.append("\n");
      header.append("Started ");
      header.append(IcofDateUtil.formatDate(this.getTimestamp(),
                                                IcofDateUtil.ACOS_REQUEST_DATE_FORMAT));
      header.append("\n");
      header.append(DIVIDER_LN);
      header.append("\n");
      log(INFO, header.toString());
    }

    // -----------------------------------------------------------------------------
    /**
     * Write the trailer of the .log file to the path
     * 
     */
    // -----------------------------------------------------------------------------
    protected void writeTrailer() {
        
        this.setTimestamp(new Date());
        
        StringBuffer trailer = new StringBuffer("\n");
        trailer.append(DIVIDER_LN);
        trailer.append("\n");
        trailer.append("Ended ");
        trailer.append(IcofDateUtil.formatDate(this.getTimestamp(),
                                                  IcofDateUtil.ACOS_REQUEST_DATE_FORMAT));
        trailer.append("\n");
        trailer.append(DIVIDER_LN);
        trailer.append("\n");
        log(INFO, trailer.toString());
    }

    // -----------------------------------------------------------------------------
    /**
     * Returns the class instance as a String
     * 
     * @return asString the class as a String
     * 
     */
    // -----------------------------------------------------------------------------
    public String toString() {
        String asString = "App Name: " + getAppName() + ";" + "Creator: "
        + getCreatedBy() + ";" + "Timestamp: "
        + getTimestamp().toString() + ";" + "Path: " + getPath() + ";"
        + "File: " + getFileName() + ";" + "Full Path: "
        + getFullPath() + ";";
        return asString;
    }

    // -----------------------------------------------------------------------------
    /**
     * Set the current minimum log level for internal use. The native attribute
     * currentMinimumLevel allows for boolean returns on log() to indicate
     * whether or not the log() call logged to file or not (based on a
     * comparison of severity level and currentMinimumLevel)
     * 
     * @param newLevel
     *            The current level set for minimum logging. It is synonymous
     *            with log4j's current Level set
     */
    // -----------------------------------------------------------------------------
    protected void setNativeMinimumLevel(String newLevel) {
        if (newLevel.equals(OFF)) {
            this.nativeMinimumLevel = OFF;
        } else if (newLevel.equals(TRACE)) {
            this.nativeMinimumLevel = TRACE;
        } else if (newLevel.equals(DEBUG)) {
            this.nativeMinimumLevel = DEBUG;
        } else if (newLevel.equals(INFO)) {
            this.nativeMinimumLevel = INFO;
        } else if (newLevel.equals(WARN)) {
            this.nativeMinimumLevel = WARN;
        } else if (newLevel.equals(ERROR)) {
            this.nativeMinimumLevel = ERROR;
        } else if (newLevel.equals(FATAL)) {
            this.nativeMinimumLevel = FATAL;
        } else if (newLevel.equals(ALL)) {
            this.nativeMinimumLevel = ALL;
        } else {
            this
            .log(
                 WARN,
                 "An error has occurred setting the SessionLog native "
                 + "currentMinimumLevel. The minimum log level will be set to WARN "
                 + "and messages will be logged accordingly.",
                 WARN);
        }
    }

    // -----------------------------------------------------------------------------
    /**
     * Used to log to the open .log file. Information gathered from the
     * IcofException will be extracted from its bundled format. The stack trace
     * will be logged if the severity level yeilds logging. The IcofException
     * also holds a message that will be logged. The default level will be ERROR
     * 
     * @param ie
     *            IcofException instance containing logging information
     * @return wasLogged Indicates whether or not the message was writen to file
     */
    // -----------------------------------------------------------------------------
    public boolean log(IcofException ie) {
        String severity = ie.getSeverity();
        String message = ie.getMessage() + "\n";
        if (severity.equals(IcofException.INFO)) {
            logger.info(message, ie);
            severity = INFO;
        } else if (severity.equals(IcofException.WARNING)) {
            logger.warn(message, ie);
            severity = WARN;
        } else if (severity.equals(IcofException.SEVERE)) {
            logger.error(message, ie);
            severity = ERROR;
        } else if (severity.equals(IcofException.TRACE)) {
            logger.trace(message, ie);
            severity = TRACE;
        } else {
            String temp = getNativeMinimumLevel();
            setLevel(WARN);
            logger
            .warn(
                  "(Invalid severity level passed: the message to be logged will be logged as WARN).\n",
                  ie);
            setLevel(temp);
            severity = TRACE;
        }
        return wasLogged(severity);
    }

    // -----------------------------------------------------------------------------
    /**
     * Used to log to the open .log file. The information necessary for the log
     * is passed separately rather than as a "bundle" in the form of an
     * IcofException. There are five levels of built in severity, which are
     * ordered: DEBUG INFO WARN ERROR FATAL. Use the constants defined in this
     * class to determine the level desired.
     * 
     * @param severity
     *            String containing the severity of the log. This MUST be a
     *            constant found in Constants.java to be compatible for logging.
     *            If it isn't, the default level will be FATAL to ensure that an
     *            important messaging will not be lost.
     * @param message
     *            String containing the message to be logged
     * @return wasLogged Indicates whether or not the message was writen to file
     */
    // -----------------------------------------------------------------------------
    public boolean log(String severity, String message) {
        if (severity.equals(TRACE)) {
            logger.trace(message);
        } else if (severity.equals(DEBUG)) {
            logger.debug(message);
        } else if (severity.equals(INFO)) {
            logger.info(message);
        } else if (severity.equals(WARN)) {
            logger.warn(message);
        } else if (severity.equals(ERROR)) {
            logger.error(message);
        } else if (severity.equals(FATAL)) {
            logger.fatal(message);
        } else {
            String temp = getNativeMinimumLevel();
            setLevel(WARN);
            logger
            .warn("(Invalid severity level passed: the message to be logged will be logged as WARN below).\n"
                  + message);
            setLevel(temp);
            severity = TRACE;
        }
        return wasLogged(severity);
    }

    // -----------------------------------------------------------------------------
    /**
     *Used to log to the open .log file. This method provides the convenience
     * of changing the logging level before a message is logged. It is to be
     * used a shortcut alternative to calling setLevel(newlevel) and then
     * SessionLogInstance.log(severity, message).
     * 
     *@param severity
     *            String containing the severity of the log. This MUST be a
     *            constant found in Constants.java to be compatible for logging.
     *            If it isn't, the default level will be FATAL to ensure that an
     *            important messaging will not be lost.
     *@param message
     *            String containing the message to be logged.
     *@param newLevel
     *            New logging level (i.e. logging set to the INFO level will be
     *            changed to the newLevel to yield different appending to the
     *            file).
     * 
     */
    // -----------------------------------------------------------------------------
    public boolean log(String severity, String message, String newLevel) {
        setLevel(newLevel);
        return log(severity, message);
    }

    // -----------------------------------------------------------------------------
    /**
     *Close the SessionLog. This method does not need to be called if the
     * Appender being used is a ConsoleAppender and not a FileAppender.
     */
    public void close() {
        if (getFileAppenderInd()) {
            writeTrailer();
        }
    }
} // End of Class.

// ========================== END OF FILE ====================================//