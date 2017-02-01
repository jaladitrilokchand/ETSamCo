/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2005 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: IipmdsReleaseLog.java
*
* CREATOR: Karen K. Witt
*    DEPT: AW0V
*    DATE: 05/26/2005
*
*-PURPOSE---------------------------------------------------------------------
* IipmdsReleaseLog class definition file.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/26/2005 KKW  Initial coding.
* 07/19/2005 KKW  Corrected logIt and logReturnCode to handle the case where
*                 the specified appName was found in the list of
*                 appLogSections.
* 12/15/2005 KKW  Modified due to splitting of Constants.java into several
*                 *Util classes.
* 12/06/2006 KKW  Updated due to changes to base class.  Added AppContext as
*                 first parameter of each method.
* 05/31/2007 KKW  Ensured the result of all trim() functions is assigned
*                 to a variable, as appropriate 
*=============================================================================
* </pre>
*/

package com.ibm.stg.iipmds.icof.component.util;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofStringUtil;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.mom.IcofObject;
import com.ibm.stg.iipmds.icof.component.proddef.TechRelease;


public class IipmdsReleaseLog extends IcofObject {

  // Constants for log file
  public static final String RELEASE_TAG = "RELEASE";
  public static final String TECHNOLOGY_TAG = "TECHNOLOGY";
  public static final String VERSION_TAG = "VERSION";
  public static final String RELEASE_PROD_DEFN_TAG = "RELEASE_PROD_DEFN";
  public static final String PREVIEW_KIT_BUILT_TAG = "PREVIEW_KIT_BUILT";
  public static final String DIP_VER_REV_TAG = "DIP_VER_REV";


  //-----------------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param     anAppContext      application context; contains appMode 
   *                              (ex: "PROD" | "DEV", see Constants.java)
   * @param     aTechRelease      the TechnologyRelease to which this log file
   *                              applies.
   * @param     aReleaseID        an identifier, created by the releaseDip
   *                              application, used to keep track of all
   *                              activity being done for a single run of
   *                              the releaseDip application
   *
   * @exception IcofException     Unable to construct object
   */
  //-----------------------------------------------------------------------------
  public IipmdsReleaseLog(AppContext anAppContext
                          ,TechRelease aTechRelease
                          ,String aReleaseID)
      throws IcofException {

    super(anAppContext);
    setReleaseID(anAppContext, aReleaseID);
    setTechRelease(anAppContext, aTechRelease);
    setDipVerRevList(anAppContext, new Vector());
    setAppSpecificData(anAppContext, new HashMap());

    // Create an IcofFile instance, representing the log file.  Note that
    //   this does not mean the file actually exists.
    String fileName = 
        IcofUtil.constructAesIipmdsTechVerReleaseDirName(anAppContext
                                                         ,getTechName(anAppContext)
                                                         ,getVersion(anAppContext));
    IcofFile logDir = new IcofFile(fileName, true);
    logDir.validate(true);

    fileName += "/release." + aReleaseID + ".log";
    setLogFile(anAppContext, new IcofFile(fileName, false));

  }


  //-----------------------------------------------------------------------------
  // Member "getter" functions
  //-----------------------------------------------------------------------------
  /**
   * Get value of releaseID
   *
   */
  public String getReleaseID(AppContext anAppContext) { return releaseID; }

  /**
   * Get value of techRelease
   *
   */
  public TechRelease getTechRelease(AppContext anAppContext) { return techRelease; }

  /**
   * Get value of techName
   *
   */
  public String getTechName(AppContext anAppContext) {
    if (getTechRelease(anAppContext) == null) {
      return "";
    }
    else {
      return getTechRelease(anAppContext).getName(anAppContext);
    }
  }

  /**
   * Get value of tech version
   *
   */
  public String getVersion(AppContext anAppContext) {
    if (getTechRelease(anAppContext) == null) {
      return "";
    }
    else {
      return getTechRelease(anAppContext).getVersion(anAppContext);
    }
  }

  /**
   * Get value of prodDefnSelections
   *
   */
  
  
  //-----------------------------------------------------------------------------
  /**
   * @param anAppContext
   * @return
   */
  //-----------------------------------------------------------------------------
  public String getProdDefnSelections(AppContext anAppContext) {
    return prodDefnSelections;
  }

  /**
   * Get value of previewKitBuiltInd
   *  
   */
  public boolean getPreviewKitBuiltInd(AppContext anAppContext) {
    return previewKitBuiltInd;
  }

  /**
   * Get value of dipVerRevList
   *  
   */
  public Vector getDipVerRevList(AppContext anAppContext) {
    return dipVerRevList;
  }

  /**
   * Get value of appSpecificData
   *  
   */
  public HashMap getAppSpecificData(AppContext anAppContext) {
    return appSpecificData;
  }

  /**
   * Get value of logFile
   *
   */
  protected IcofFile getLogFile(AppContext anAppContext) { return logFile; }

  /**
   * Get log file name
   *
   */
  public String getLogFileName(AppContext anAppContext) { 
    return getLogFile(anAppContext).getAbsolutePath(); 
  }


  //-----------------------------------------------------------------------------
  /**
   * Included for readability -- same as getPreviewKitBuiltInd()
   *
   */
  //-----------------------------------------------------------------------------
  public boolean builtPreviewKit(AppContext anAppContext) {
    return previewKitBuiltInd;
  }


  //-----------------------------------------------------------------------------
  /**
   * Get contents of IipmdsReleaseLog as a string
   *
   * @return                      Contents of this object as a String
   */
  //-----------------------------------------------------------------------------
  public String asString(AppContext anAppContext) throws IcofException {

    String logData = getUserSelectionsAsString(anAppContext);
    logData += getAppSpecificDataAsString(anAppContext);

    return logData;

  }


  //-----------------------------------------------------------------------------
  /**
   * Format user selection part of log
   *
   * @return                      formatted string
   */
  //-----------------------------------------------------------------------------
  public String getUserSelectionsAsString(AppContext anAppContext) {

    String pkBuiltIndStr = "No";
    if (builtPreviewKit(anAppContext)) {
      pkBuiltIndStr = "Yes";
    }

    String logData = RELEASE_TAG + Constants.SEMI_COLON
                   + getReleaseID(anAppContext) + Constants.SEMI_COLON + "\n"
                   + TECHNOLOGY_TAG + Constants.SEMI_COLON
                   + getTechName(anAppContext) + Constants.SEMI_COLON + "\n"
                   + VERSION_TAG + Constants.SEMI_COLON
                   + getVersion(anAppContext) + Constants.SEMI_COLON + "\n"
                   + RELEASE_PROD_DEFN_TAG + Constants.SEMI_COLON
                   + getProdDefnSelections(anAppContext) + Constants.SEMI_COLON + "\n"
                   + PREVIEW_KIT_BUILT_TAG + Constants.SEMI_COLON
                   + pkBuiltIndStr + Constants.SEMI_COLON + "\n";

    Iterator iter = getDipVerRevList(anAppContext).iterator();
    while (iter.hasNext()) {
      String dipVerRev = (String) iter.next();
      logData += DIP_VER_REV_TAG + Constants.SEMI_COLON
               + dipVerRev + Constants.SEMI_COLON + "\n";
    }

    return logData;

  }


  //-----------------------------------------------------------------------------
  /**
   * Format application specific part of log
   *
   * @return                      formatted string
   */
  //-----------------------------------------------------------------------------
  public String getAppSpecificDataAsString(AppContext anAppContext) 
      throws IcofException {

    String logData = "";
    Iterator iter = getAppSpecificData(anAppContext).values().iterator();
    while (iter.hasNext()) {
      AppLogSection appSection = (AppLogSection) iter.next();
      logData += appSection.asString(anAppContext);
    }

    return logData;

  }


  //-----------------------------------------------------------------------------
  /**
   * Format contents of IipmdsReleaseLog as a vector, including tags
   *   and application sections.  This vector can be passed to an
   *   IcofFile write function.
   *
   * @return                      Contents of this object as a Vector
   */
  //-----------------------------------------------------------------------------
  public Vector format(AppContext anAppContext) {

    Vector contents = formatUserSelections(anAppContext);
    contents.addAll(formatAppSpecificData(anAppContext));

    return contents;

  }


  //-----------------------------------------------------------------------------
  /**
   * Format user selection part of log as a vector, including tags.
   *
   * @return                      Contents of user selections as a Vector
   */
  //-----------------------------------------------------------------------------
  public Vector formatUserSelections(AppContext anAppContext) {

    Vector contents = new Vector();
    contents.add(RELEASE_TAG + Constants.SEMI_COLON
                 + getReleaseID(anAppContext) + Constants.SEMI_COLON);
    contents.add(TECHNOLOGY_TAG + Constants.SEMI_COLON
                 + getTechName(anAppContext) + Constants.SEMI_COLON);
    contents.add(VERSION_TAG + Constants.SEMI_COLON
                 + getVersion(anAppContext) + Constants.SEMI_COLON);
    contents.add(RELEASE_PROD_DEFN_TAG + Constants.SEMI_COLON
                 + getProdDefnSelections(anAppContext) + Constants.SEMI_COLON);
    contents.add(PREVIEW_KIT_BUILT_TAG + Constants.SEMI_COLON
                 + getPreviewKitBuiltInd(anAppContext) + Constants.SEMI_COLON);

    Iterator iter = getDipVerRevList(anAppContext).iterator();
    while (iter.hasNext()) {
      String dipVerRev = (String) iter.next();
      contents.add(DIP_VER_REV_TAG + Constants.SEMI_COLON
                   + dipVerRev + Constants.SEMI_COLON);
    }
    contents.add(" ");

    return contents;

  }


  //-----------------------------------------------------------------------------
  /**
   * Format application specific part of log as a vector
   *
   * @return                      Contents of application log sections in a vector
   */
  //-----------------------------------------------------------------------------
  public Vector formatAppSpecificData(AppContext anAppContext) {

    Vector contents = new Vector();
    Iterator iter = getAppSpecificData(anAppContext).values().iterator();
    while (iter.hasNext()) {
      AppLogSection appSection = (AppLogSection) iter.next();
      contents.add(appSection.format(anAppContext));
    }

    return contents;

  }


  //-----------------------------------------------------------------------------
  /**
   * Add an application section to the log
   *
   * @param     anAppLogSection   the application log section to add
   */
  //-----------------------------------------------------------------------------
  public void addAppLogSection(AppContext anAppContext
                               ,AppLogSection anAppLogSection) {

    getAppSpecificData(anAppContext).put(anAppLogSection.getMapKey(anAppContext)
                                         ,anAppLogSection);

  }


  //-----------------------------------------------------------------------------
  /**
   * Add a message to the log section for the specified application
   *
   * @param     msg               the message to add to the log
   * @param     appName           the application that the message is from
   */
  //-----------------------------------------------------------------------------
  public void logIt(AppContext anAppContext, String msg, String appName) {

    AppLogSection thisAppLogSection = 
        (AppLogSection) getAppSpecificData(anAppContext).get(appName);
    if (thisAppLogSection == null) {
      AppLogSection newSection = new AppLogSection(anAppContext, appName);
      newSection.appendMsg(anAppContext, msg);
      getAppSpecificData(anAppContext).put(newSection.getMapKey(anAppContext)
                                           ,newSection);
    }
    else {
      thisAppLogSection.appendMsg(anAppContext, msg);
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Log the return code for the specified application
   *
   * @param     rc                the return code
   * @param     appName           the application that the message is from
   */
  //-----------------------------------------------------------------------------
  public void logReturnCode(AppContext anAppContext, int rc, String appName) {

    AppLogSection thisAppLogSection = 
        (AppLogSection) getAppSpecificData(anAppContext).get(appName);
    if (thisAppLogSection == null) {
      AppLogSection newSection = new AppLogSection(anAppContext, appName);
      newSection.setReturnCode(anAppContext, rc);
      getAppSpecificData(anAppContext).put(newSection.getMapKey(anAppContext)
                                           ,newSection);
    }
    else {
      thisAppLogSection.setReturnCode(anAppContext, rc);
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Read the log file
   *
   * @exception IcofException     Unable to read the log file.
   */
  //-----------------------------------------------------------------------------
  public void read(AppContext anAppContext) throws IcofException {

    String funcName = "read()";
    logFile.openRead();
    logFile.read();
    logFile.closeRead();

    boolean inAppSection = false;
    String curAppName = "";
    String prevLine = "";
    Iterator iter = logFile.getContents().iterator();
    while (iter.hasNext()) {
      String thisLine = (String) iter.next();
      thisLine = thisLine.trim();
      if (thisLine.equals("")) {
        prevLine = thisLine;
        continue;
      }

      if (thisLine.startsWith(RELEASE_TAG + Constants.SEMI_COLON)) {
        inAppSection = false;
        // Verify that the release id in the log file matches the one used
        //   to construct this object.
        String fileReleaseID = IcofStringUtil.getField(thisLine, 2, Constants.SEMI_COLON);
        if (!fileReleaseID.equals(getReleaseID(anAppContext))) {
          IcofException ie = new IcofException(this.getClass().getName()
                                               ,funcName
                                               ,IcofException.SEVERE
                                               ,"ReleaseID in file, "
                                               + fileReleaseID
                                               +", does not match"
                                               +" release ID used to construct "
                                               +" this class"
                                               ,getReleaseID(anAppContext));
          anAppContext.getSessionLog().log(ie);
          throw ie;
        }
        prevLine = thisLine;
        continue;
      }

      if (thisLine.startsWith(TECHNOLOGY_TAG + Constants.SEMI_COLON)) {
        inAppSection = false;
        // Verify that the tech name in the log file matches the one used
        //   to construct this object.
        String fileTechName = IcofStringUtil.getField(thisLine, 2, Constants.SEMI_COLON);
        if (!fileTechName.equals(getTechName(anAppContext))) {
          IcofException ie = new IcofException(this.getClass().getName()
                                               ,funcName
                                               ,IcofException.SEVERE
                                               ,"Tech Name in file, "
                                               + fileTechName
                                               +", does not match"
                                               +" tech name used to construct "
                                               +" this class"
                                               ,getTechName(anAppContext));
          anAppContext.getSessionLog().log(ie);
          throw ie;
        }
        prevLine = thisLine;
        continue;
      }

      if (thisLine.startsWith(VERSION_TAG + Constants.SEMI_COLON)) {
        inAppSection = false;
        // Verify that the tech version in the log file matches the one used
        //   to construct this object.
        String fileVersion = IcofStringUtil.getField(thisLine, 2, Constants.SEMI_COLON);
        if (!fileVersion.equals(getVersion(anAppContext))) {
          IcofException ie = new IcofException(this.getClass().getName()
                                               ,funcName
                                               ,IcofException.SEVERE
                                               ,"Version in file, "
                                               + fileVersion
                                               +", does not match"
                                               +" version used to construct "
                                               +" this class"
                                               ,getVersion(anAppContext));
          anAppContext.getSessionLog().log(ie);
          throw ie;
        }
        prevLine = thisLine;
        continue;
      }

      if (thisLine.startsWith(RELEASE_PROD_DEFN_TAG + Constants.SEMI_COLON)) {
        inAppSection = false;
        setProdDefnSelections(anAppContext
                              ,IcofStringUtil.getField(thisLine, 2, Constants.SEMI_COLON));
        prevLine = thisLine;
        continue;
      }

      if (thisLine.startsWith(PREVIEW_KIT_BUILT_TAG + Constants.SEMI_COLON)) {
        inAppSection = false;
        String indStr = IcofStringUtil.getField(thisLine, 2, Constants.SEMI_COLON);
        setPreviewKitBuiltInd(anAppContext, IcofStringUtil.stringToBoolean(indStr));
        prevLine = thisLine;
        continue;
      }

      if (thisLine.startsWith(DIP_VER_REV_TAG + Constants.SEMI_COLON)) {
        inAppSection = false;
        String dipStr = IcofStringUtil.getField(thisLine, 2, Constants.SEMI_COLON);
        getDipVerRevList(anAppContext).add(dipStr);
        prevLine = thisLine;
        continue;
      }

      if (thisLine.equals(Constants.SECTION_DIVIDER)) {

        if (prevLine.startsWith(Constants.RETURN_CODE_TAG)) {
          inAppSection = false;
          curAppName = "";
        }
        else {
          inAppSection = true;
        }
        prevLine = thisLine;
        continue;
      }

      if (inAppSection) {
        if (prevLine.equals(Constants.SECTION_DIVIDER)) {
          curAppName = thisLine;
          AppLogSection thisAppSection = new AppLogSection(anAppContext, curAppName);
          getAppSpecificData(anAppContext).put(thisAppSection.getMapKey(anAppContext)
                                               ,thisAppSection);
          prevLine = thisLine;
          continue;
        }

        if (prevLine.equals(curAppName)) {
          AppLogSection thisAppSection = 
              (AppLogSection) getAppSpecificData(anAppContext).get(curAppName);
          thisAppSection.setAppTimestamp(anAppContext, thisLine);
          prevLine = thisLine;
          continue;
        }

        if (thisLine.startsWith(Constants.RETURN_CODE_TAG)) {
          String rcStr = thisLine.substring(thisLine.indexOf(":") + 1);
          AppLogSection thisAppSection = 
              (AppLogSection) getAppSpecificData(anAppContext).get(curAppName);
          thisAppSection.setReturnCode(anAppContext, Integer.parseInt(rcStr));
          prevLine = thisLine;
          continue;
        }

        AppLogSection thisAppSection = 
            (AppLogSection) getAppSpecificData(anAppContext).get(curAppName);
        thisAppSection.appendMsg(anAppContext, thisLine);
        prevLine = thisLine;
        continue;
      }
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Append the specified appLogSection to the log file
   *
   * @exception IcofException     Unable to append to the log file.
   */
  //-----------------------------------------------------------------------------
  public void append(AppContext anAppContext
                     ,AppLogSection thisAppLogSection) throws IcofException {

    logFile.openAppend();
    logFile.write(thisAppLogSection.format(anAppContext), true);
    logFile.closeAppend();

  }


  //-----------------------------------------------------------------------------
  /**
   * Write the log file
   *
   * @exception IcofException     Unable to write the log file.
   */
  //-----------------------------------------------------------------------------
  public void write(AppContext anAppContext) throws IcofException {

    logFile.openWrite();
    logFile.writeLine(asString(anAppContext));
    logFile.closeWrite();

  }


  //-----------------------------------------------------------------------------
  // Data members
  //-----------------------------------------------------------------------------
  protected String         releaseID;
  protected TechRelease    techRelease;
  protected String         prodDefnSelections;
  protected boolean        previewKitBuiltInd;
  protected Vector         dipVerRevList;
  protected HashMap        appSpecificData;
  protected IcofFile       logFile;


  //-----------------------------------------------------------------------------
  // Member "setter" functions
  //-----------------------------------------------------------------------------
  /**
   * Set value of releaseID
   *
   */
  protected void setReleaseID(AppContext anAppContext, String aString) {
    releaseID = aString;
  }

  /**
   * Set value of techRelease
   *  
   */
  protected void setTechRelease(AppContext anAppContext
                                ,TechRelease aTechRelease) {
    techRelease = aTechRelease;
  }

  /**
   * Set value of prodDefnSelections
   *  
   */
  protected void setProdDefnSelections(AppContext anAppContext, String aString) {
    prodDefnSelections = aString;
  }

  /**
   * Set value of previewKitBuiltInd
   *  
   */
  public void setPreviewKitBuiltInd(AppContext anAppContext, boolean aBoolean) {
    previewKitBuiltInd = aBoolean;
  }

  /**
   * Set value of dipVerRevList
   *  
   */
  protected void setDipVerRevList(AppContext anAppContext, Vector aVector) {
    dipVerRevList = aVector;
  }

  /**
   * Set value of appSpecificData
   *  
   */
  protected void setAppSpecificData(AppContext anAppContext, HashMap aHashMap) {
    appSpecificData = aHashMap;
  }

  /**
   * Set value of logFile
   *  
   */
  protected void setLogFile(AppContext anAppContext, IcofFile anIcofFile) {
    logFile = anIcofFile;
  }
}

//==========================  END OF FILE  ====================================
