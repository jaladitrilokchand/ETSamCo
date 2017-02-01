/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2006 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: SoftwareContactMap.java
*
* CREATOR: Keith Loring
*    DEPT: 5ZIA
*    DATE: 09/12/2006
*
*-PURPOSE---------------------------------------------------------------------
* SoftwareContactMap class definition file.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 09/12/2006 KPL  Initial coding.
* 04/19/2007 KKW  Fixed readSpecifiedSoftwareContacts method to skip over
*                 the comment line at the top of the file.
* 05/21/2007 RAM  Synchronized all public static methods
*=============================================================================
* </pre>
**/

package com.ibm.stg.iipmds.icof.component.role;

import java.util.Iterator;
import java.util.TreeMap;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofStringUtil;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.util.IcofUtil;
import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;


public class SoftwareContactMap {


  //-----------------------------------------------------------------------------
  /**
   * Read the software.contacts file from its regular production or
   *   development location.  This function will read the copy from
   *   $aim/icof/swAdmin.
   *
   * @param     anAppContext      the application context
   * @return                      treeMap of SoftwareContact objects
   * @exception IcofException     Unable to read file
   */
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap readSoftwareContacts(AppContext anAppContext)
      throws IcofException {

    String funcName = new String("readSoftwareContacts(anAppContext)");

    // Construct the aim/icof/swAdmin directory name.
    String dirName = IcofUtil.constructAimIcofSwadminDirName(anAppContext);

    // Check to see if the directory exists.
    IcofFile aimIcofSwadminDir = new IcofFile(dirName, true);
    if (!aimIcofSwadminDir.exists()) {
      IcofException ie = new IcofException(CLASS_NAME
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Directory does not exist"
                                           ,dirName);
      anAppContext.getSessionLog().log(ie);
      throw ie;
    }

    // Finish constructing file name.
    String fileName = new String(dirName + "/" + Constants.SOFTWARE_CONTACTS);

    TreeMap rsyncData = readSpecifiedSoftwareContacts(anAppContext, fileName);

    return rsyncData;

  }


  //-----------------------------------------------------------------------------
  /**
   * Read the software.contacts file from the specified fileName.
   *
   * @param     anAppContext      the application context
   * @param     fileName          the fully-pathed name of the software.contacts
   *                              file to be read.
   * @return                      treeMap of SoftwareContact objects
   * @exception IcofException     Unable to read file
   */
  //-----------------------------------------------------------------------------
  public static synchronized TreeMap readSpecifiedSoftwareContacts(AppContext anAppContext
  		                                                		   ,String fileName)
      throws IcofException {

    String funcName = new String("readSpecifiedSoftwareContacts(AppContext, String)");

    TreeMap softwareContacts = new TreeMap(ManagerFunctions.STRINGCOMPARE);

    // The "false" boolean indicates that this is a file, not a directory
    //   instance.
    IcofFile srcFile = new IcofFile(fileName,false);

    // Test for existence.
    if (!srcFile.exists()) {
      IcofException ie = new IcofException(CLASS_NAME
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"File does not exist"
                                           ,fileName);
      anAppContext.getSessionLog().log(ie);
      throw ie;
    }

    // Open the file for reading.
    srcFile.openRead();

    // Read the file.
    srcFile.read();

    // Close the file
    srcFile.closeRead();

    // Process the contents of the file.
    Iterator iter = srcFile.getContents().iterator();
    while (iter.hasNext()) {

      String thisLine = (String) iter.next();
      
      // Skip over comment line
      if (thisLine.startsWith(Constants.UNDERSCORE)) {
        continue;
      }

      String appName = IcofStringUtil.getField(thisLine, 1, Constants.SEMI_COLON);
      appName = appName.trim();

      String addresses = IcofStringUtil.getField(thisLine, 2, Constants.SEMI_COLON);
      addresses = addresses.trim();
      
      SoftwareContact aSoftwareContact = new SoftwareContact(anAppContext
      		                                                 ,appName
                                                             ,addresses);

      // Add the SoftwareContact to the treeMap.
      softwareContacts.put(aSoftwareContact.getMapKey(anAppContext), aSoftwareContact);
    }

    return softwareContacts;

  }


  //-----------------------------------------------------------------------------
  // Data elements.
  //-----------------------------------------------------------------------------
  private static final String CLASS_NAME = "SoftwareContactMap";

}

//==========================  END OF FILE  ====================================



