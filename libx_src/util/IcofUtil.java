/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2005 - 2011 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: IcofUtil.java
*
* CREATOR: Karen K. Witt
*    DEPT: AW0V
*    DATE: 12/15/2005
*
*-PURPOSE---------------------------------------------------------------------
* IcofUtil class definition file.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 12/15/2005 KKW  Initial coding.
* 12/27/2005 GFS  Added constructAesIipmdsTechVerPacketMapsDirName() and
*                 updated constructAesIipmdsTechPacketStagedDirName() methods.
* 01/10/2006 KKW  Updated constructAesIipmdsTechPacketReleasedDirName() method
*                 to use the new release-specific packetMaps dir.
* 04/06/2006 KPL  added release name into constructAsicshipDocDirName()
* 04/20/2006 AAK  Added constructAesIipmdsTechReleaseNotesDirName() method
* 06/14/2006 RAM  Added constructAsicpatch2CompactedDirName(),
* 				  		constructAimEDesignBuildDirName() methods
* 06/15/2006 RAM  Added constructBinDataFileName(),
* 						constructSharedDataDirName(),
* 						constructCommonDRTarFileName(),
* 						constructCommonTarFileName(),
* 						constructCompactedDeltaPacketListFileName(),
* 						constructCompactionStopFileName(),
* 						constructCumulativePacketFileName(),
* 						constructDeltaOrdDirName(),
* 						constructDeltaCompactedPacketXRefFileName() methods
* 				  		Added overload of constructAimIcofDKDirName()method
* 				  		for non-DB connection
* 06/16/2006 RAM  Added constructDeltaPacketListFileName(),
* 						constructDeltaReleaseListFileName(),
* 						constructDesignKitDirName(),
* 						constructExportedOrderFileName(),
* 						constructInitReleasePacketListFileName(),
* 						constructLibGroupOverriesFileName(),
* 						constructMasterPacketListFileName(),
* 						constructMkSizeFileName(),
* 						constructPacketContentFileName(),
* 						constructPackerHistoryFileName() methods
* 06/26/2006 RAM  Added constructPacketXRefFileName(),
* 						constructShippingDirName(), 
* 						constructSoftwareContactsFileName(),
* 						constructTarListFileName(), constructToolMTFileName(),
* 						constructToolsMapFileName(), removeShipitFile()
* 06/29/2006 RAM  Added round(), saveShipitCommand(),
* 						constructCustomIpDefDataFileName()
* 07/21/2006 RAM  Updated all methods with ICCPROD appMode.
* 08/14/2006 RAM  Added the AppContext to all functions for SessionLog
* 						capability.
* 08/16/2006 RAM  Removed appMode from functions because of the movement of
* 				   appMode into AppContext
* 10/06/2006 KKW  Moved isValidEmailAddress, getFirstName,
*                 constructDummyIntranetId, getLastName to IcofEmailUtil
* 12/13/2006 AAK  Added constructAesIipmdsTechRevisionAlertsDirName() method
* 01/02/2007 KKW  Removed constructAimIcofDKDirName method that took a 
*                 LibRelease object as a parameter.  This method was no  
*                 longer being used.
* 01/24/2007 GFS  Added the following new methods: 
*                 constructIipmdsStagedDipDirName(),
*                 constructIipmdsStagedFingerprintsDirName(),
*                 constructIipmdsStagedObsoleteDirName(),
*                 constructIipmdsStagedFullContentDirName(),
*                 constructIipmdsStagedRelNotesDirName(), 
*                 constructIipmdsPocketDirName() and
*                 constructIipmdsCompactedDirName().
* 02/05/2007 AAK  Updated constructAesIipmdsTechReleaseNotesDirName() method to
* 			          point to the new location ($release/relnotes)
* 02/08/2007 AAK  Updated constructAesIipmdsTechRevisionAlertsDirName() method
* 				        to point to the new location ($release/revisionAlerts)
* 02/09/2007 GFS  Updated constructIipmdsStagedDipDirName() to point to
*                 TECH/staged/dip_dev instead of TECH/packet/dip_ver.
*                 Added constructIipmdsStagedDirName.
* 02/26/2007 KKW  Updated to use new HighLvlQualifier from AppContext to
*                 allow for testing on windows platforms
* 03/15/2007 GFS  Added getDipName() and getDipVersion() methods.
* 04/02/2007 KKW  Merged changes from iipmds stream into rewrite stream
* 04/24/2007 RAM  added constructAESIipmdsReleasedObsoleteDirName() and
* 				  constructAesObsoleteFileName()
* 05/21/2007 RAM  Synchronized all public static methods
* 06/12/2007 RAM  Added roundToLong() function
* 06/27/2007 AAK  Added constructIccRsyncCmd(AppContext),
*                 constructAsicReleaseCommonDirName(AppContext),
*                 constructAimAeinfoBinInstallDKDirName(AppContext) functions
* 08/02/2007 KKW  Added constructSharedDataDirName(AppContext)
* 08/28/2007 KKW  In constructShipListName(..), added code to ensure that the
*                 parent directories exist.
* 10/09/2007 KKW  Added constructAesDirName(..)
* 01/18/2008 KKW  Added a variant of constructAesDirName(..) that allows
*                 the application mode in the appContext object to be
*                 overridden with the specified appMode.  The reason for
*                 this method is to allow a program running in one appMode
*                 to construct the path, using the specified appMode, to a 
*                 program that it will call.  Basically, this allows DEV 
*                 programs to use PROD data and vice versa.
* 01/24/2008 KKW  Fixed items identified by RSA Code Analysis tool --
*                 specifically using == and != to compare java objects.
* 01/30/2008 GFS  Added constructIccRsyncMigrationCmd() and constructRsyncCmd()
*                 methods to support the migration only rsynch command.
* 01/30/2008 KKW  Removed constructAesObsoleteFileName method, as it is
*                 no longer used anywhere.                                 
* 01/31/2008 KKW  Removed constructAesIipmdsTechPacketFingerprintsDirName
*                 since this directory is not to be used any more.
* 04/10/2008 KKW  Added constructIccRsyncDataFileName method       
* 05/07/2008 AAK  Added constructAesIipmdsTechRetractDirName,
*                 constructAesIipmdsTechReleaseLogDirName and 
*                 constructAesIipmdsTechRetractedIPRevisionsFile methods                          
* 05/08/2008 KKW  Added new appMode JUNIT and updated constructShipListDirName
*                 to use it.                           
* 05/14/2008 KKW  Added constructRevisionCustomersDirName and 
*                 constructRevisionCustomersFileName      
* 06/17/2008 GFS  Added constructIipmdsStagedDipContentDirName() and 
*                 constructIipmdsStagedDipObsoleteDirName()method. Minor clean up.    
* 08/04/2008 GFS  Updated to no longer use the *_AIM_SWADMIN constants.   
* 12/08/2008 GFS  Added constructIipmdsDipPrepDirName() method.
* 03/06/2009 KKW  Added constructDipContentFileName() method.
* 03/10/2009 KKW  Added constructObsoletePacketMapFileName() method.
* 03/30/2009 GFS  Added constructAesIipmdsTechVerAsictestConfigDirName() method.
* 04/15/2009 KKW  Added constructRetractedPacketMapFileName() method.
* 02/17/2010 KKW  Added constructShippingDirName(), 
*                 constructShippingProcessingDirName(), 
*                 constructProcessingCompactionDirName()
* 04/09/2010 KKW  Added support for ICCTEST appMode   
* 05/18/2010 AAK  Added icc.rsync.data.icctest to constructIccRsyncDataFileName
* 02/21/2011 KKW  Removed all the methods related to ICC rsync.   
* 04/19/2011 KKW  Updated validateLibGrpType to recognize new foundry lib grp
*                 types (ASPRD 792)            
*=============================================================================
* </pre>
*/

package com.ibm.stg.iipmds.icof.component.util;

import java.io.File;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofStringUtil;
import com.ibm.stg.iipmds.icof.component.clearcase.ccConstants;
import com.ibm.stg.iipmds.icof.component.modelkit.DipPacketMap;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.proddef.TechRelease;

public class IcofUtil {

  public static final String TITLE = "JR Jr jr SR Sr sr II ii III iii IV iv";
  public static final String BTV_IBM_COM = "btv.ibm.com";


  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/{appmode} directory name.
   *
   * @param   anAppContext  Application Context
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesDirName(AppContext anAppContext) {

    return constructAesDirName(anAppContext, anAppContext.getAppMode());

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/{appmode} directory name.  The reason for
   *   this method is to allow a program running in one appMode
   *   to construct the path, using the specified appMode, to a 
   *   program that it will call.  Basically, this allows DEV 
   *   programs to use PROD data and vice versa.
   *
   * @param   anAppContext  Application Context
   * @param   overrideAppMode  the appMode to use instead of the one inside
   *                           the application context
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesDirName(AppContext anAppContext
                                                        ,String overrideAppMode) {

    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if ((overrideAppMode.equals(Constants.PROD)) ||
        (overrideAppMode.equals(Constants.ICCPROD))) {
      dirName += Constants.AES_PROD;
    }
    else if ((overrideAppMode.equals(Constants.DEV)) || 
              (overrideAppMode.equals(Constants.ICCTEST))) {
      dirName += Constants.AES_DEV;
    }
    else {
      dirName += Constants.AES_TEST;
    }
    
    return dirName;

  }  

  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/bin directory name.
   *
   * @param		anAppContext	Application Context
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesBinDirName(AppContext anAppContext) {

    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.PROD)) ||
        (anAppContext.getAppMode().equals(Constants.ICCPROD))) {
      dirName += Constants.PROD_AES_BIN;
    }
    else if ((anAppContext.getAppMode().equals(Constants.DEV)) || 
                    (anAppContext.getAppMode().equals(Constants.ICCTEST))) {
      dirName += Constants.DEV_AES_BIN;
    }
    else {
      dirName += Constants.TEST_AES_BIN;
    }
    
    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH> directory name.
   *
   * @param		anAppContext	Application Context
   * @param  	techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechDirName(AppContext anAppContext
                                                     ,String techName) {

    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) ||
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_AES_IIPMDS;
    }
    else if (anAppContext.getAppMode().equals(Constants.DEV)) {
        dirName += Constants.DEV_AES_IIPMDS;
    }
    else if (anAppContext.getAppMode().equals(Constants.ICCTEST)) {
        dirName += Constants.ICCTEST_AES_IIPMDS;
    }
    else {
        dirName += Constants.TEST_AES_IIPMDS;
    } 

    dirName += "/" + techName;

    return dirName;

  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/TECH/REL/relNotes directory name.
   *
   * @param		anAppContext	Application Context
   * @param  	techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param  releaseName      release for which to construct the directory name    
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechReleaseNotesDirName(AppContext anAppContext
                                                                 ,String techName
    																                             ,String releaseName) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechDirName(anAppContext, techName);
    dirName += "/" + releaseName + Constants.REL_NOTES;

    return dirName;

  }
  // -----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/TECH/REL/release directory name.
   * 
   * @param anAppContext
   *          Application Context
   * @param techName
   *          the uppercase technology name (ex. CU11, CU08, CU65LP,...)
   * @param releaseName
   *          release for which to construct the directory name
   * 
   * @return the directory name
   */
  // -----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechReleaseLogDirName(AppContext anAppContext,
                                                                            String techName,
                                                                            String releaseName) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechDirName(anAppContext, techName);
    dirName += "/" + releaseName;

    return dirName;

  }
  // -----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/retracted directory name.
   *
   * @param  anAppContext         the application context
   * @param  techName             the uppercase technology name
   *                              (ex. CU11, CU08, CU65LP,...)
   * @param  releaseName          the release for which to construct the directory
   *                              name
   * @return                      the directory name
   *
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechRetractDirName(AppContext anAppContext
                                                                   ,String techName) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechDirName(anAppContext, techName);
    dirName += Constants.RETRACTED;

    return dirName;

  }  
  // -----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/<REL>/retract directory name.
   *
   * @param  anAppContext         the application context
   * @param  techName             the uppercase technology name
   *                              (ex. CU11, CU08, CU65LP,...)
   * @param  releaseName          the release for which to construct the directory
   *                              name
   * @return                      the directory name
   *
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechRetractDirName(AppContext anAppContext
                                                                   ,String techName
                                                                   ,String releaseName) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechDirName(anAppContext, techName);
    dirName += "/" + releaseName +  "/" + Constants.RETRACT;

    return dirName;

  }  
  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/<REL>/revisionAlerts directory name.
   *
   * @param  anAppContext         the application context
   * @param  techName             the uppercase technology name
   *                              (ex. CU11, CU08, CU65LP,...)
   * @param  releaseName          the release for which to construct the directory
   *                              name
   *
   * @return                      the directory name
   *
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechRevisionAlertsDirName(AppContext anAppContext
                                                                                ,String techName
                                                                                ,String releaseName) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechDirName(anAppContext, techName);
    dirName += "/" + releaseName + "/" + Constants.REV_ALERT;

    return dirName;

  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/<REL>/released.ip.revisions file name.
   *
   * @param		anAppContext	Application Context
   * @param  	techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param  	relName         the lowercase technology release name
   *                            (ex. rel1.0, rel2.0,...)
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechReleasedIPRevisionsFile(AppContext anAppContext
                                                                     ,String techName
                                                                     ,String releaseName) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechDirName(anAppContext, techName);
    dirName += "/" + releaseName + Constants.RELEASE + "/";
    dirName += Constants.IIPMDS_RELEASE_IP_REVISIONS;

    return dirName;

  }
  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/<REL>/retracted.ip.revisions file name.
   *
   * @param     anAppContext    Application Context
   * @param     techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param     relName         the lowercase technology release name
   *                            (ex. rel1.0, rel2.0,...)
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechRetractedIPRevisionsFile(AppContext anAppContext
                                                                     ,String techName
                                                                     ,String releaseName) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechDirName(anAppContext, techName);
    dirName += "/" + releaseName +  "/" + Constants.RETRACT + "/";
    dirName += Constants.IIPMDS_RETRCTED_IP_REVISIONS;

    return dirName;

  }

  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/admin directory name.
   *
   * @param		anAppContext	Application Context
   * @param  	techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechAdminDirName(AppContext anAppContext
                                                          ,String techName) {
    // Construct directory name.
    String dirName = constructAesIipmdsTechDirName(anAppContext, techName);
    dirName += Constants.ADMIN;

    return dirName;
  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/packet directory name.
   *
   * @param		anAppContext	Application Context
   * @param  	techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechPacketDirName(AppContext anAppContext
                                                           ,String techName) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechDirName(anAppContext, techName);
    dirName += Constants.PACKET;

    return dirName;

  }

  
  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/staged directory name.
   *
   * @param  anAppContext         the application context
   * @param  techName             the uppercase technology name
   *                              (ex. CU11, CU08, CU65LP,...)
   *
   * @return                      the directory name
   *
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsStagedDirName(AppContext anAppContext
                                                    ,String techName) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechDirName(anAppContext, techName);
    dirName +=  Constants.STAGED;

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/dipPrep/<dip>_<version>_<userid> 
   * directory name.
   *
   * @param  anAppContext         the application context
   * @param  techName             the uppercase technology name
   *                              (ex. CU11, CU08, CU65LP,...)
   * @param  dipName              the dip name (ex. ra, rom1, sc_hvt ...)
   * @param  dipVersion           the dip version name (ex. v001, v002 ...)
   *
   * @return                      the directory name
   *
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsDipPrepDirName(AppContext anAppContext
                                                    ,String techName
                                                    ,String dipName
                                                    ,String dipVersion) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechDirName(anAppContext, techName);
    dirName +=  Constants.DIPPREP + IcofFile.separator;
    dirName += dipName + "_" + dipVersion + "_" + anAppContext.getAfsUserid();

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/staged/<dip>_<ver> directory name.
   *
   * @param  anAppContext         the application context 
   * @param  techName             the uppercase technology name
   *                              (ex. CU11, CU08, CU65LP,...)
   * @param  dipName              the dip name (ex. ra, rom1, sc_hvt ...)
   * @param  dipVersion           the dip version name (ex. v001, v002 ...)
   *
   * @return                      the directory name
   *
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsStagedDipDirName(AppContext anAppContext
                                                       ,String techName
                                                       ,String dipName
                                                       ,String dipVersion) {

    // Construct directory name.
    String dirName = constructIipmdsStagedDirName(anAppContext, techName);
    dirName +=  File.separator + dipName + "_" + dipVersion;

    return dirName;

  }

  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/<REL>/packetMaps directory name.
   *
   * @param		anAppContext	Application Context
   * @param  	techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param  	relName         the lowercase technology release name
   *                            (ex. rel1.0, rel2.0,...)
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechVerPacketMapsDirName(AppContext anAppContext
                                                                  ,String techName
                                                                  ,String releaseName) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechDirName(anAppContext, techName);
    dirName += File.separator + releaseName;
    dirName += Constants.PACKET_MAPS;

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/<REL>/packetMaps/released directory name.
   *
   * @param		anAppContext	Application Context
   * @param  	techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param  	relName         the lowercase technology release name
   *                            (ex. rel1.0, rel2.0,...)
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechPacketReleasedDirName(AppContext anAppContext
                                                                   ,String techName
                                                                   ,String relName) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechVerPacketMapsDirName(anAppContext
                                                                ,techName
                                                                ,relName);
    dirName += Constants.RELEASED;
    return dirName;

  }

  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/<REL>/packet/retracted directory name.
   *
   * @param     anAppContext    Application Context
   * @param     techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param     relName         the lowercase technology release name
   *                            (ex. rel1.0, rel2.0,...)
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechPacketRetractedDirName(AppContext anAppContext
                                                                   ,String techName
                                                                   ,String relName) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechVerPacketMapsDirName(anAppContext
                                                                ,techName
                                                                ,relName);
    dirName += Constants.RETRACTED;
    return dirName;

  }
  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/packet/compacted<n> directory name.
   *
   * @param		anAppContext	Application Context
   * @param  	techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param  compactedNumber      the compacted number (ex. 1, 2, 3,...)
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsCompactedDirName(AppContext anAppContext
                                                       ,String techName
                                                       ,String compactedNumber) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechPacketDirName(anAppContext
                                                         ,techName);
    dirName += IcofFile.separator + Constants.COMPACTED + compactedNumber;
    return dirName;

  }

  
 //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/staged/<dip>_<ver>/relnotes directory name.
   *
   * @param  anAppContext         the application context
   * @param  techName             the uppercase technology name
   *                              (ex. CU11, CU08, CU65LP,...)
   * @param  dipName              the dip name (ex. ra, rom1, sc_hvt ...)
   * @param  dipVersion           the dip version name (ex. v001, v002 ...)
   *
   * @return                      the directory name
   *
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsStagedRelNotesDirName(AppContext anAppContext
                                                            ,String techName
                                                            ,String dipName
                                                            ,String dipVersion)
    throws IcofException {

    // Construct directory name.
    String dirName = constructIipmdsStagedDipDirName(anAppContext
                                                     ,techName
                                                     ,dipName
                                                     ,dipVersion);
    dirName += Constants.REL_NOTES;

    return dirName;

  }

  
  //------------------------------------------------------------------------
  /**
   * Creates a packet name in tar format. The format is
   * TECH.paddedVersion.paddedRevision.ModelType.LibGroup.tar
   * 
   * @param     anAppContext   the application context
   * @param     sTech          Technology name (TECH).
   * @param     sBuildList     Build list name. It will be used for getting
   *                           the model type and lib group names.
   * @param     iDipVersion    Deliverable IP version.
   * @param     iDipRevision   Deliverable IP revision.
   * @param     bCompacted     If true construct compacted packet name
   *                           otherwise construct raw packet name.
   * @return                   Formatted packet name.
   * @exception IcofException  Trouble creating the packet name.
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsTarPacketName(AppContext anAppContext
                                                    ,String sTech
                                                    ,String sBuildList
                                                    ,int iDipVersion
                                                    ,int iDipRevision
                                                    ,boolean bCompacted) 
      throws IcofException {

      String sReturn = null;
      String sModelType = null;
      String sLibGroup = null;

      try {

          // Parse build list name to get model type name and lib group name.
          sModelType = IcofStringUtil.getField(sBuildList, 2, ".");
          sLibGroup = IcofStringUtil.getField(sBuildList, 3, ".");

          // Construct the packet name.
          // Example: CU65LP.v006.r00001.srule.OSC.tar.gz
          // Example: CU65LP.v006.r00001_cmp.srule.OSC.tar.gz (compacted)
          sReturn = sTech + "."
              + ccConstants.convertDipVersion(iDipVersion, true) + "."
              + ccConstants.convertDipRevision(iDipRevision, true);
          
          if (bCompacted) {
              sReturn += "_cmp";
          }
          sReturn += "." + sModelType + "." + sLibGroup + ".tar";

      } catch (Exception e) {
          IcofException ie = new IcofException(CLASS_NAME
                                  ,"constructIipmdsTarPacketName()"
                                  ,IcofException.SEVERE
                                  ,"Failed to create tar packet name.\n"
                                  ,e.getMessage());
          anAppContext.getSessionLog().log(ie);
          throw ie;
      } finally {
      }

      return sReturn;
  }


  //------------------------------------------------------------------------
  /**
   * Creates a packet name in tar format. The format is
   * TECH.paddedVersion.paddedRevision.ModelType.LibGroup.tar
   * 
   * @param     anAppContext   application context
   * @param     sTech          Technology name (TECH).
   * @param     sBuildList     Build list name. It will be used for getting
   *                           the model type and lib group names.
   * @param     iDipVersion    Deliverable IP version.
   * @param     sDipRevision   Deliverable IP revision.
   * @param     bCompacted     If true construct compacted packet name
   *                           otherwise construct raw packet name.
   * @return                   Formatted packet name.
   * @exception IcofException  Trouble creating the packet name.
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsTarPacketName(AppContext anAppContext
                                                    ,String sTech
                                                    ,String sBuildList
                                                    ,int iDipVersion
                                                    ,String sDipRevision
                                                    ,boolean bCompacted) 
      throws IcofException {

      String sReturn = null;
      String sModelType = null;
      String sLibGroup = null;

      try {

          // Parse build list name to get model type name and lib group name.
          sModelType = IcofStringUtil.getField(sBuildList, 2, ".");
          sLibGroup = IcofStringUtil.getField(sBuildList, 3, ".");

          // Construct the packet name.
          // Example: CU65LP.v006.r00001.srule.OSC.tar.gz
          // Example: CU65LP.v006.r00001_cmp.srule.OSC.tar.gz (compacted)
          sReturn = sTech + "."
              + ccConstants.convertDipVersion(iDipVersion, true) + "."
              + sDipRevision;
          
          if (bCompacted) {
              sReturn += "_cmp";
          }
          sReturn += "." + sModelType + "." + sLibGroup + ".tar";

      } catch (Exception e) {
          IcofException ie = new IcofException(CLASS_NAME
                                  ,"constructIipmdsTarPacketName()"
                                  ,IcofException.SEVERE
                                  ,"Failed to create tar packet name.\n"
                                  ,e.getMessage());
          anAppContext.getSessionLog().log(ie);
          throw ie;
      } finally {
      }

      return sReturn;
  }

  //------------------------------------------------------------------------
  /**
   * Creates a packet name in tar format. The format is
   * TECH.paddedVersion.paddedRevision.ModelType.LibGroup.tar
   *
   * @param     anAppContext   the application context 
   * @param     sTech          Technology name (TECH).
   * @param     sBuildList     Build list name. It will be used for getting
   *                           the model type and lib group names.
   * @param     sDipVersion    Deliverable IP version.
   * @param     sDipRevision   Deliverable IP revision.
   * @param     bCompacted     If true construct compacted packet name
   *                           otherwise construct raw packet name.
   * @return                   Formatted packet name.
   * @exception IcofException  Trouble creating the packet name.
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsTarPacketName(AppContext anAppContext
                                                    ,String sTech
                                                    ,String sBuildList
                                                    ,String sDipVersion
                                                    ,String sDipRevision
                                                    ,boolean bCompacted) 
      throws IcofException {

      String sReturn = null;
      String sModelType = null;
      String sLibGroup = null;

      try {

          // Parse build list name to get model type name and lib group name.
          sModelType = IcofStringUtil.getField(sBuildList, 2, ".");
          sLibGroup = IcofStringUtil.getField(sBuildList, 3, ".");

          // Construct the packet name.
          // Example: CU65LP.v006.r00001.srule.OSC.tar.gz
          // Example: CU65LP.v006.r00001_cmp.srule.OSC.tar.gz (compacted)
          sReturn = sTech + "."
              + sDipVersion + "."
              + sDipRevision;
          
          if (bCompacted) {
              sReturn += "_cmp";
          }
          sReturn += "." + sModelType + "." + sLibGroup + ".tar";

      } catch (Exception e) {
          IcofException ie = new IcofException(CLASS_NAME
                                  ,"constructIipmdsTarPacketName()"
                                  ,IcofException.SEVERE
                                  ,"Failed to create tar packet name.\n"
                                  ,e.getMessage());
          anAppContext.getSessionLog().log(ie);
          throw ie;
      } finally {
      }

      return sReturn;
  }


  //------------------------------------------------------------------------
  /**
   * Creates a packet name in compressed format. The format is
   * TECH.paddedVersion.paddedRevision.ModelType.LibGroup.tar.gz
   *
   * @param     anAppContext   application context 
   * @param     sTech          Technology name (TECH).
   * @param     sBuildList     Build list name. It will be used for getting
   *                           the model type and lib group names.
   * @param     iDipVersion    Deliverable IP version.
   * @param     iDipRevision   Deliverable IP revision.
   * @param     bCompacted     If true construct compacted packet name
   *                           otherwise construct raw packet name.
   * @return                   Formatted packet name.
   * @exception IcofException  Trouble creating the packet name.
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsPacketName(AppContext anAppContext
                                                 ,String sTech
                                                 ,String sBuildList
                                                 ,int iDipVersion
                                                 ,int iDipRevision
                                                 ,boolean bCompacted) 
      throws IcofException {

      String sFuncName 
          = "constructIipmdsPacketName(String, String, int, int)";
      String sReturn = null;

      try {

          sReturn = constructIipmdsTarPacketName(anAppContext
                                                 ,sTech
                                                 ,sBuildList
                                                 ,iDipVersion
                                                 ,iDipRevision
                                                 ,bCompacted);

          // Add ".gz" extention.
          sReturn += ".gz";
          
      } catch (Exception e) {
          IcofException ie = new IcofException(CLASS_NAME
                                  ,sFuncName
                                  ,IcofException.SEVERE
                                  ,"Failed to create packet name.\n"
                                  ,e.getMessage());
          anAppContext.getSessionLog().log(ie);
          throw ie;
      } finally {}

      return sReturn;
  }
  
  
  //------------------------------------------------------------------------
  /**
   * Creates a packet name in compressed format. The format is
   * TECH.paddedVersion.paddedRevision.ModelType.LibGroup.tar.gz
   * 
   * @param     anAppContext   application context
   * @param     sTech          Technology name (TECH).
   * @param     sBuildList     Build list name. It will be used for getting
   *                           the model type and lib group names.
   * @param     iDipVersion    Deliverable IP version.
   * @param     sDipRevision   Deliverable IP revision.
   * @param     bCompacted     If true construct compacted packet name
   *                           otherwise construct raw packet name.
   * @return                   Formatted packet name.
   * @exception IcofException  Trouble creating the packet name.
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsPacketName(AppContext anAppContext
                                                 ,String sTech
                                                 ,String sBuildList
                                                 ,int iDipVersion
                                                 ,String sDipRevision
                                                 ,boolean bCompacted) 
      throws IcofException {

      String sReturn = null;

      try {

          sReturn = constructIipmdsTarPacketName(anAppContext
                                                 ,sTech
                                                 ,sBuildList
                                                 ,iDipVersion
                                                 ,sDipRevision
                                                 ,bCompacted);

          // Add ".gz" extention.
          sReturn += ".gz";
          
      } catch (Exception e) {
          IcofException ie = new IcofException(CLASS_NAME
                                  ,"constructIipmdsPacketName()"
                                  ,IcofException.SEVERE
                                  ,"Failed to create packet name.\n"
                                  ,e.getMessage());
          anAppContext.getSessionLog().log(ie);
          throw ie;
      } finally {}

      return sReturn;
  }
  
  //------------------------------------------------------------------------
  /**
   * Creates a packet name in compressed format. The format is
   * TECH.paddedVersion.paddedRevision.ModelType.LibGroup.tar.gz
   *
   * @param     anAppContext   application context 
   * @param     sTech          Technology name (TECH).
   * @param     sBuildList     Build list name. It will be used for getting
   *                           the model type and lib group names.
   * @param     sDipVersion    Deliverable IP version.
   * @param     sDipRevision   Deliverable IP revision.
   * @param     bCompacted     If true construct compacted packet name
   *                           otherwise construct raw packet name.
   * @return                   Formatted packet name.
   * @exception IcofException  Trouble creating the packet name.
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsPacketName(AppContext anAppContext
                                                 ,String sTech
                                                 ,String sBuildList
                                                 ,String sDipVersion
                                                 ,String sDipRevision
                                                 ,boolean bCompacted) 
      throws IcofException {

      String sReturn = null;

      try {

          sReturn = constructIipmdsTarPacketName(anAppContext
                                                 ,sTech
                                                 ,sBuildList
                                                 ,sDipVersion
                                                 ,sDipRevision
                                                 ,bCompacted);

          // Add ".gz" extention.
          sReturn += ".gz";
          
      } catch (Exception e) {
          IcofException ie = new IcofException(CLASS_NAME
                                  ,"constructIipmdsPacketName()"
                                  ,IcofException.SEVERE
                                  ,"Failed to create packet name.\n"
                                  ,e.getMessage());
          anAppContext.getSessionLog().log(ie);
          throw ie;
      } finally {}

      return sReturn;
  }
  
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/staged/<dip>_<ver>/packetMasp directory
   * name.
   *
   * @param  anAppContext         the application context
   * @param  techName             the uppercase technology name
   *                              (ex. CU11, CU08, CU65LP,...)
   * @param  dipName              the dip name (ex. ra, rom1, sc_hvt ...)
   * @param  dipVersion           the dip version name (ex. v001, v002 ...)
   *
   * @return                      the directory name
   *
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsStagedPacketMapsDirName(AppContext anAppContext
                                                              ,String techName
                                                              ,String dipName
                                                              ,String dipVersion) {

    // Construct directory name.
    String dirName = constructIipmdsStagedDipDirName(anAppContext
                                                     ,techName
                                                     ,dipName
                                                     ,dipVersion);
    
    dirName +=  Constants.PACKET_MAPS;

    return dirName;

  }

  
  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/staged/<dip>_<ver>/fullContent directory
   * name.
   *
   * @param  anAppContext         the application context
   * @param  techName             the uppercase technology name
   *                              (ex. CU11, CU08, CU65LP,...)
   * @param  dipName              the dip name (ex. ra, rom1, sc_hvt ...)
   * @param  dipVersion           the dip version name (ex. v001, v002 ...)
   *
   * @return                      the directory name
   *
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsStagedFullContentDirName(AppContext anAppContext
                                                               ,String techName
                                                               ,String dipName
                                                               ,String dipVersion) {
    // Construct directory name.
    String dirName = constructIipmdsStagedDipDirName(anAppContext
                                                     ,techName
                                                     ,dipName
                                                     ,dipVersion);
    dirName += Constants.FULL_CONTENT;

    return dirName;

  }

  
  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/staged/<dip>_<ver>/dipContent directory
   * name.
   *
   * @param  anAppContext         the application context
   * @param  techName             the uppercase technology name
   *                              (ex. CU11, CU08, CU65LP,...)
   * @param  dipName              the dip name (ex. ra, rom1, sc_hvt ...)
   * @param  dipVersion           the dip version name (ex. v001, v002 ...)
   *
   * @return                      the directory name
   *
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsStagedDipContentDirName(AppContext anAppContext
                                                               ,String techName
                                                               ,String dipName
                                                               ,String dipVersion) {
    // Construct directory name.
    String dirName = constructIipmdsStagedDipDirName(anAppContext
                                                     ,techName
                                                     ,dipName
                                                     ,dipVersion);
    dirName += Constants.DIP_CONTENT;

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/staged/<dip>_<ver>/dipObsolete directory
   * name.
   *
   * @param  anAppContext         the application context
   * @param  techName             the uppercase technology name
   *                              (ex. CU11, CU08, CU65LP,...)
   * @param  dipName              the dip name (ex. ra, rom1, sc_hvt ...)
   * @param  dipVersion           the dip version name (ex. v001, v002 ...)
   *
   * @return                      the directory name
   *
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsStagedDipObsoleteDirName(AppContext anAppContext
                                                               ,String techName
                                                               ,String dipName
                                                               ,String dipVersion) {
    // Construct directory name.
    String dirName = constructIipmdsStagedDipDirName(anAppContext
                                                     ,techName
                                                     ,dipName
                                                     ,dipVersion);
    dirName += Constants.DIP_OBSOLETE;

    return dirName;

  }

  
  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/staged/<dip>_<ver>/obsolete directory name.
   *
   * @param  anAppContext         the application context
   * @param  techName             the uppercase technology name
   *                              (ex. CU11, CU08, CU65LP,...)
   * @param  dipName              the dip name (ex. ra, rom1, sc_hvt ...)
   * @param  dipVersion           the dip version name (ex. v001, v002 ...)
   *
   * @return                      the directory name
   *
   * @exception IcofException     Invalid application mode
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsStagedObsoleteDirName(AppContext anAppContext
                                                           ,String techName
                                                           ,String dipName
                                                           ,String dipVersion) {
    // Construct directory name.
    String dirName = constructIipmdsStagedDipDirName(anAppContext
                                                     ,techName
                                                     ,dipName
                                                     ,dipVersion);
    dirName += Constants.OBSOLETE;

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/packet/pocket<n> directory name.
   *
   * @param  anAppContext         the application context
   * @param  techName             the uppercase technology name
   *                              (ex. CU11, CU08, CU65LP,...)
   * @param  pocketNumber         the pcet number (ex. 1, 2, 3,...)
   *
   * @return                      the directory name
   *
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsPocketDirName(AppContext anAppContext
                                                    ,String techName
                                                    ,String pocketNumber) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechPacketDirName(anAppContext, techName);
    dirName += Constants.POCKET + pocketNumber;

    return dirName;

  }
  

  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/staged/<dip>_<ver>/fingerprints directory
   * name.
   *
   * @param  anAppContext         the application context
   * @param  techName             the uppercase technology name
   *                              (ex. CU11, CU08, CU65LP,...)
   * @param  dipName              the dip name (ex. ra, rom1, sc_hvt ...)
   * @param  dipVersion           the dip version name (ex. v001, v002 ...)
   *
   * @return                      the directory name
   *
   * @exception IcofException     Invalid application mode
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIipmdsStagedFingerprintsDirName(AppContext anAppContext
                                                                ,String techName
                                                                ,String dipName
                                                                ,String dipVersion) {

    // Construct directory name.
    String dirName = constructIipmdsStagedDipDirName(anAppContext
                                                     ,techName
                                                     ,dipName
                                                     ,dipVersion);
    dirName += Constants.FINGERPRINTS;

    return dirName;

  }
  


  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/<version> directory name.
   *
   * @param		anAppContext	Application Context
   * @param  	techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param  	version         the ModelKit version of the technology
   *                            (ex. v13.0...)
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechVerDirName(AppContext anAppContext
                                                        ,String techName
                                                        ,String version) {    

    // Construct directory name.
    String dirName = constructAesIipmdsTechDirName(anAppContext
        										   ,techName);
    dirName += "/" + version;

    return dirName;

  }

  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/<version>/asictestConfig directory name.
   *
   * @param     anAppContext    Application Context
   * @param     techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param     version         the ModelKit version of the technology
   *                            (ex. v13.0...)
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechVerAsictestConfigDirName(AppContext anAppContext
                                                                               ,String techName
                                                                               ,String version) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechVerDirName(anAppContext
                                                      ,techName
                                                      ,version);
    dirName += Constants.ASICTEST_CONFIG;

    return dirName;

  }



  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/<version>/deliverables directory name.
   *
   * @param		anAppContext	Application Context
   * @param  	techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param  	version         the ModelKit version of the technology
   *                            (ex. v13.0...)
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechVerDeliverablesDirName(AppContext anAppContext
                                                                    ,String techName
                                                                    ,String version) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechVerDirName(anAppContext
        											  ,techName
        											  ,version);
    dirName += Constants.DELIVERABLES;

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/<version>/productDefn directory name.
   *
   * @param		anAppContext	Application Context
   * @param  	techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param  	version         the ModelKit version of the technology
   *                            (ex. v13.0...)
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechVerProdDefnDirName(AppContext anAppContext
                                                                ,String techName
                                                                ,String version) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechVerDirName(anAppContext
        											  ,techName
        											  ,version);
    dirName += Constants.PRODUCT_DEFN;

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/<version>/release directory name.
   *
   * @param		anAppContext	Application Context
   * @param  	techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param  	version         the ModelKit version of the technology
   *                            (ex. v13.0...)
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsTechVerReleaseDirName(AppContext anAppContext
                                                               ,String techName
                                                               ,String version) {

    // Construct directory name.
    String dirTechName = techName;
    if (dirTechName.equals("")) {
      dirTechName = Constants.NO_TECH;
    }
    String dirVersion = version;
    if (dirVersion.equals("")) {
      dirVersion = Constants.NO_VERSION;
    }
    String dirName = constructAesIipmdsTechVerDirName(anAppContext
        											  ,dirTechName
        											  ,dirVersion);
    dirName += Constants.RELEASE;

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aes/iipmds/<TECH>/<version>/productDefn/<revision> directory name.
   *
   * @param		anAppContext	Application Context
   * @param  	techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param  	version         the ModelKit version of the technology
   *                            (ex. v13.0...)
   * @param  	revision        the Product Definition revision for the
   *                            technology version (ex. r00000)
   *
   * @return                    the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAesIipmdsProdDefnRevDirName(AppContext anAppContext
                                                            ,String techName
                                                            ,String version
                                                            ,String revision) {

    // Construct directory name.
    String dirName = constructAesIipmdsTechVerProdDefnDirName(anAppContext
        													  ,techName
        													  ,version);
    dirName += "/" + revision;

    return dirName;

  }

  
  //-----------------------------------------------------------------------------
  /**
   * Construct the aim/admin directory name, without requiring a database
   *   connection
   * @param	 anAppContext		  Application Context
   * @param  techNickname         the lowercase "nickname" for the technology
   *                              (ex. catamount, cu11, cu65lp)
   * @param  version              the ModelKit version of the technology
   *                              (ex. v13.0...)
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAimAdminDirName(AppContext anAppContext
                                                ,String techNickname
                                                ,String version) {

    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) || 
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_AIM_ADMIN;
    }
    else if (anAppContext.getAppMode().equals(Constants.DEV)) {
      dirName += Constants.DEV_AIM_ADMIN;
    }
    else if (anAppContext.getAppMode().equals(Constants.ICCTEST)) {
        dirName += Constants.ICCTEST_AIM_ADMIN;
      }
    else {
      dirName += Constants.TEST_AIM_ADMIN;
    }
    

    dirName += "/" + techNickname + "/" + version;

    return dirName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct the aim/edesign/build directory name.
   *
   * @param		anAppContext	Application Context
   * @param  	techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param  	version         the ModelKit version of the technology
   *                            (ex. v13.0...)
   * 
   * @return                    the directory name
   */
  
  //-----------------------------------------------------------------------------
  public static synchronized String constructAimEdesignBuildDirName(AppContext anAppContext
  														,String techName
														,String version) {
    
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) ||
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_AIM_EDESIGN_BUILD_DIR;
    }
    else if (anAppContext.getAppMode().equals(Constants.DEV)) {
      dirName += Constants.DEV_AIM_EDESIGN_BUILD_DIR;
    }
    else if (anAppContext.getAppMode().equals(Constants.ICCTEST)) {
        dirName += Constants.ICCTEST_AIM_EDESIGN_BUILD_DIR;
      }
    else {
      dirName += Constants.TEST_AIM_EDESIGN_BUILD_DIR;
    }
    
    
    dirName += "/" + techName + "/" + version;
    
    return dirName;
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Construct the aim/aeinfo/templates directory name.
   * 
   * @param		anAppContext		Application Context
   *
   * @return                      	the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAimAeinfoTemplateDirName(AppContext anAppContext) {

    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) ||
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_AEINFO_TEMPLATES;
    }
    else if (anAppContext.getAppMode().equals(Constants.DEV)) {
      dirName += Constants.DEV_AEINFO_TEMPLATES;
    }
    else if (anAppContext.getAppMode().equals(Constants.ICCTEST)) {
        dirName += Constants.ICCTEST_AEINFO_TEMPLATES;
      }
    else {
      dirName += Constants.TEST_AEINFO_TEMPLATES;
    }
    
    return dirName;

  }

  //-----------------------------------------------------------------------------
  /**
   * Construct the aim/aeinfo/bin/install_dk directory name.
   * 
   * @param   anAppContext    Application Context
   *
   * @return                        the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAimAeinfoBinInstallDKDirName(AppContext anAppContext) {

    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();

    if (anAppContext.getAppMode().equals(Constants.PROD)) {
      dirName += Constants.PROD_AEINFO_BIN;
    }
    else {
      if ((anAppContext.getAppMode().equals(Constants.DEV)) ||
                      (anAppContext.getAppMode().equals(Constants.ICCTEST))) {
        dirName += Constants.DEV_AEINFO_BIN;
      }
      else {
        dirName += Constants.TEST_AEINFO_BIN;
      }
    }
    
    return dirName;

  }
  //-----------------------------------------------------------------------------
  /**
   * Construct the aim/aimtools/bin directory name.
   *
   * @param		anAppContext		Application Context
   * 
   * @return                      	the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAimAimtoolsBinDirName(AppContext anAppContext) {
    
    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) ||
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_AIMTOOLS_BIN;
    }
    else if ((anAppContext.getAppMode().equals(Constants.DEV)) ||
                    (anAppContext.getAppMode().equals(Constants.ICCTEST))) {
      dirName += Constants.DEV_AIMTOOLS_BIN;
    }
    else {
      dirName += Constants.TEST_AIMTOOLS_BIN;
    }
    
    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aim/aimtools/templates directory name.
   *
   * @param		anAppContext		Application Context
   *
   * @return                      	the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAimAimtoolsTemplateDirName(AppContext anAppContext) {

    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if (anAppContext.getAppMode().equals(Constants.JUNIT)) {
      dirName += Constants.JUNIT_DATA_PATH;
    }
    else if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) ||
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_AIMTOOLS_TEMPLATES;
    }
    else if (anAppContext.getAppMode().equals(Constants.DEV)) {
      dirName += Constants.DEV_AIMTOOLS_TEMPLATES;
    }
    else if (anAppContext.getAppMode().equals(Constants.ICCTEST)) {
        dirName += Constants.ICCTEST_AIMTOOLS_TEMPLATES;
      }
    else {
      dirName += Constants.TEST_AIMTOOLS_TEMPLATES;
    }

    return dirName;

  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct the aim/icof/dk/<tech>/<ver>/ directory name.
   * 
   * Use this method as an alternative to the database connective "sister"-method
   * 
   * @param  	anAppContext	Application Context
   * @param 	techName		the technology name
   * @param 	version			the ModelKit version of the technology
   *                        	(ex. v13.0...)
   * @return 	dirName			directory name 
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAimIcofDKDirName(AppContext anAppContext
  												 ,String techName
												 ,String version) {
    
    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) ||
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_AIM_ICOF_DK;
    } 
    else if (anAppContext.getAppMode().equals(Constants.DEV)) {
      dirName += Constants.DEV_AIM_ICOF_DK;
    }  
    else if (anAppContext.getAppMode().equals(Constants.ICCTEST)) {
        dirName += Constants.ICCTEST_AIM_ICOF_DK;
      }  
    else {
      dirName += Constants.TEST_AIM_ICOF_DK;
    }
    
    dirName += "/" + techName + "/" + version;
    
    return dirName;
  }

  
  //-----------------------------------------------------------------------------
  /**
   *  Construct the aim/icof/dk/global directory name.
   *
   * @param		anAppContext		Application Context
   *
   * @return                      	the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAimIcofDKGlobalDirName(AppContext anAppContext) {
    
    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) ||
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_AIM_ICOF_DK_GLOBAL;
    }
    else if (anAppContext.getAppMode().equals(Constants.DEV)) {
      dirName += Constants.DEV_AIM_ICOF_DK_GLOBAL;
    }
    else if (anAppContext.getAppMode().equals(Constants.ICCTEST)) {
        dirName += Constants.ICCTEST_AIM_ICOF_DK_GLOBAL;
      }
    else {
      dirName += Constants.TEST_AIM_ICOF_DK_GLOBAL;
    }
    
    return dirName;
  }

 
  //-----------------------------------------------------------------------------
  /**
   * Construct the aim/icof/log directory name.
   *
   * @param		anAppContext		Application Context
   *
   * @return                      	the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAimIcofLogDirName(AppContext anAppContext) {
   
    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) ||
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_AIM_ICOF_LOG;
    }
    else if ((anAppContext.getAppMode().equals(Constants.DEV)) || 
             (anAppContext.getAppMode().equals(Constants.ICCTEST))) {
      dirName += Constants.DEV_AIM_ICOF_LOG;
    }
    else {
      dirName += Constants.TEST_AIM_ICOF_LOG;
    }

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aim/icof/swAdmin directory name.
   *
   * @param		anAppContext		Application Context
   *
   * @return                      	the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAimIcofSwadminDirName(AppContext anAppContext) {

    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) ||
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_AES_SYS_ADMIN;
    }
    else if (anAppContext.getAppMode().equals(Constants.DEV)) {
      dirName += Constants.DEV_AES_SYS_ADMIN;
    }
    else if (anAppContext.getAppMode().equals(Constants.ICCTEST)) {
        dirName += Constants.ICCTEST_AES_SYS_ADMIN;
      }
    else {
      dirName += Constants.DEV_AES_SYS_ADMIN;
    }

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the asicdelta/<tech>/admin directory name.
   *
   * @param	 anAppContext		  Application Context
   * @param  techNickname         a technology nickname fo which to construct
   *                              the directory name (ex. catamount, cu11)
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAsicdeltaTechAdminDirName(AppContext anAppContext
                                                          ,String techNickname) {
    
    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) ||
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_ASICDELTA;
    }
    else if (anAppContext.getAppMode().equals(Constants.DEV)) {
      dirName += Constants.DEV_ASICDELTA;
    }
    else if (anAppContext.getAppMode().equals(Constants.ICCTEST)) {
        dirName += Constants.ICCTEST_ASICDELTA;
      }
    else {
      dirName += Constants.TEST_ASICDELTA;
    }

    dirName += "/" + techNickname + Constants.ADMIN;

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the asicdelta/<tech>/admin/syslog directory name.
   *
   *
   * @param  anAppContext		  Application Context
   * @param  techNickname     technology nickname (ex. cu08, catamount)
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAsicdeltaSyslogDirName(AppContext anAppContext
                                                       ,String techNickname) {

    String dirName = constructAsicdeltaTechAdminDirName(anAppContext
        												,techNickname);

    dirName += Constants.SYSLOG;

    return dirName;

  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct the asicpatch2/<tech>/<ver>Patch/compacted directory name.
   *
   * Use this function when you do not have a database connection.
   * 
   * @param  anAppContext		  Application Context
   * @param  techNickname         the lowercase nickname for the technology
   *                              for which to construct the directory name.
   *                              (ex. catamount, cu11...)
   * @param  version              the technology version for which to construct
   *                              the directory name.
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAsicpatch2CompactedDirName(AppContext anAppContext
  														   ,String techNickname
														   ,String version) {
    
    //  Construct name of directory that contains the delta release data.
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) ||
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_ASICPATCH2_COMPACTED;
    }
    else if (anAppContext.getAppMode().equals(Constants.DEV)) {
      dirName += Constants.DEV_ASICPATCH2_COMPACTED;
    }
    else if (anAppContext.getAppMode().equals(Constants.ICCTEST)) {
        dirName += Constants.ICCTEST_ASICPATCH2_COMPACTED;
      }
    else {
      dirName += Constants.TEST_ASICPATCH2_COMPACTED;
    }
    
    
    dirName += "/" + techNickname + "/" + version + Constants.PATCH;
    
    return dirName;	
  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the asicpatch2/<tech>/<ver>Patch/ directory name.
   *
   * Use this function when you do not have a database connection.
   * 
   * @param  anAppContext		  Application Context
   * @param  techNickname         the lowercase nickname for the technology
   *                              for which to construct the directory name.
   *                              (ex. catamount, cu11...)
   * @param  version              the technology version for which to construct
   *                              the directory name.
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAsicpatch2DirName(AppContext anAppContext
                                                  ,String techNickname
                                                  ,String version) {
    
    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) ||
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_ASICPATCH2;
    } 
    else if (anAppContext.getAppMode().equals(Constants.DEV)) {
      dirName += Constants.DEV_ASICPATCH2;
    }
    else if (anAppContext.getAppMode().equals(Constants.ICCTEST)) {
        dirName += Constants.ICCTEST_ASICPATCH2;
    }
    else {
      dirName += Constants.TEST_ASICPATCH2;
    }
    
    
    dirName += "/" + techNickname + "/" + version + Constants.PATCH;
    
    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the asicship directory name.
   *
   * @param  anAppContext     Application Context
   * @param  techNickname     a Technology nickname (ex. cu11, catamount)
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAsicshipDirName(AppContext anAppContext
                                                ,String techNickname) {

    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) ||
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_ASICSHIP;
    }
    else if (anAppContext.getAppMode().equals(Constants.DEV)) {
      dirName += Constants.DEV_ASICSHIP;
    }
    else if (anAppContext.getAppMode().equals(Constants.ICCTEST)) {
        dirName += Constants.ICCTEST_ASICSHIP;
      }
    else {
      dirName += Constants.TEST_ASICSHIP;
    }

    dirName += "/" + techNickname;
    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the asicship/<tech>/packet directory name.
   *
   * Note:  in order to construct the Technology object to pass to this
   *   function, it is necessary to have a database connection.
   *
   * @param  anAppContext     Application Context
   * @param  techNickname     a Technology nickname (ex. cu11, catamount)
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAsicshipPacketDirName(AppContext anAppContext
                                                      ,String techNickname) {

    // Construct directory name.
    String dirName = constructAsicshipDirName(anAppContext, techNickname);
 
    dirName += Constants.PACKET;

    return dirName;

  }

  //-----------------------------------------------------------------------------
  /**
   * Construct the /aim/asics/release/common/<tech>/<rel>/ dir name.
   *
   * @param  anAppContext     Application Context
   * @param  techNickname     a Technology nickname (ex. cu11, catamount)
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAsicReleaseCommonDirName
                              (AppContext anAppContext) {

    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();;
    if (anAppContext.getAppMode().equals(Constants.PROD)) {
      dirName += Constants.PROD_ASICS_RELEASE;
    }
    else {
      if (anAppContext.getAppMode().equals(Constants.DEV)) {
        dirName += Constants.DEV_ASICS_RELEASE;
      }
      if (anAppContext.getAppMode().equals(Constants.ICCTEST)) {
          dirName += Constants.ICCTEST_ASICS_RELEASE;
        }
      else {
        dirName += Constants.TEST_ASICS_RELEASE;
      }
    }

    return dirName;

  }
  //-----------------------------------------------------------------------------
  /**
   * Construct the asicship/<tech>/deliverables directory name.
   *
   * @param  anAppContext		  Application Context
   * @param  techNickname         the lowercase nickname of the technology
   *                              for which to construct the directory name
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAsicshipDeliverablesDirName(AppContext anAppContext
                                                      ,String techNickname) {

    // Construct directory name.
    String dirName = constructAsicshipDirName(anAppContext, techNickname);
 
    dirName += Constants.DELIVERABLES;

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the asicship/<tech>/doc directory name.
   * 
   * @param  anAppContext		  Application Context
   * @param  techNickname         the lowercase nickname of the technology
   *                              for which to construct the directory name
   * @param  versionName		  the version of the technology
   * 
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructAsicshipDocDirName(AppContext anAppContext
                                                   ,String techNickname
                                                   ,String versionName) {

    
    // Construct directory name.
    String dirName = constructAsicshipDirName(anAppContext, techNickname);
    dirName += "/" + versionName + Constants.DOC_DIR;

    return dirName;

  }
  
  
  //-----------------------------------------------------------------------------
  /**
   *  Construct the the aim/icof/edesign/sharedData/
   * 
   * @param  	anAppContext	Application Context
   * @param 	techName		the technology name
   * @param 	version			the ModelKit version of the technology
   *                        	(ex. v13.0...)
   * 
   * @return 	dirName			the directoy name that was constructed
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructSharedDataDirName(AppContext anAppContext)
  			throws IcofException {
    
    // Construct directory name
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) ||
       (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_ICOF_EDESIGN_SHARED_DATA_DIR;
    } 
    else if ((anAppContext.getAppMode().equals(Constants.DEV)) || 
              (anAppContext.getAppMode().equals(Constants.JUNIT))) {
      dirName += Constants.DEV_ICOF_EDESIGN_SHARED_DATA_DIR;
    } 
    else if (anAppContext.getAppMode().equals(Constants.ICCTEST)) {
            dirName += Constants.ICCTEST_ICOF_EDESIGN_SHARED_DATA_DIR;
          } 
    else {
      dirName += Constants.TEST_ICOF_ESDESIGN_SHARED_DATA_DIR;
    }
    
    return dirName;
  	
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   *  Construct the the aim/icof/edesign/sharedData/<tech>/<ver>/
   * 
   * @param   anAppContext  Application Context
   * @param   techName    the technology name
   * @param   version     the ModelKit version of the technology
   *                          (ex. v13.0...)
   * 
   * @return  dirName     the directoy name that was constructed
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructSharedDataDirName(AppContext anAppContext
                            ,String techName
                          ,String version) 
        throws IcofException {
    
    // Construct directory name
    String dirName = constructSharedDataDirName(anAppContext);
    
    dirName += "/" + techName + "/" + version;
    
    //validate directory exists -- create it if it doesn't
    IcofFile temp = new IcofFile(dirName, true); //pass true for directory type
    temp.validate(true);
    
    return dirName;
    
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct the the .bins aim/icof/edesign/sharedData/<tech>/<ver>/ filename
   * 
   * @param anAppContext	Application Context
   * @param techName		the technology name
   * @param version			the ModelKit version of the technology
   *                        (ex. v13.0...)
   * @param orderID			the ID for the order
   * @param shipToID		the numeric portion of a filename used for
   * 						shipping
   * @param isMajor			indication of Major Release type
   * 
   * 
   * @return				file name for a .bins order file
   * 
   */
  //----------------------------------------------------------------------------
  public static synchronized String constructBinDataFileName(AppContext anAppContext
  												,String techName
												,String version
												,String orderID
												,short shipToID
												,boolean isMajor) 
  		throws IcofException {
      
      String fileName = constructSharedDataDirName(anAppContext
                                                   ,techName
                                                   ,version);
      if (isMajor) {
          fileName += "/" + Constants.ICC_MODEL_KIT;
      }
      else {
          fileName += "/" + Constants.ICC_DELTA;
      }

      fileName += "." + orderID + "." + shipToID + "." + Constants.BINS_EXTENSION;

      return fileName;

  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct common_dr.tar file name.
   * 
   * @param anAppContext		Application Context
   * @param techNickname		the lowercase nickname of the technology
   *                        	for which to construct the directory name
   * @param version				the ModelKit version of the technology
   *                       		(ex. v13.0...)
   * 
   * @return fileName			common_dr.tar file name
   * 
   * @exception IcofException   Invalid directory, it doesn't exist and has been
   * 							coded to throw an exception and not create it
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructCommonDRTarFileName(AppContext anAppContext
      												,String techNickname
      												,String version) 
  		throws IcofException {
    
    String dirName = constructAsicpatch2DirName(anAppContext
        										,techNickname
        										,version);
    
    //validate directory exists -- create if it doesn't
    IcofFile temp = new IcofFile(dirName, true); //true if directory, false if file
    temp.validate(true);
    
    //create file name
    String fileName = dirName + "/" + Constants.COMMON_DR_TAR;
    
    return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct the compacted delta packet list file name
   * @param anAppContext		Application Context
   * @param techNickname		the lowercase nickname of the technology
   *                        	for which to construct the directory name
   * @param version				the ModelKit version of the technology
   *                       		(ex. v13.0...)
   * 
   * @return filename			the file name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructCompactedDeltaPacketListFileName(AppContext anAppContext
  																,String techNickname
																,String version) {
 
    String fileName = constructAsicpatch2CompactedDirName(anAppContext
  	    												  ,techNickname
  	    												  ,version)
  	 				+ "/" + Constants.COMPACTED_DELTA_PACKET_LIST;
  	
  	return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct delta.compacted.packetXRef file name.
   * 
   * @param	anAppContext		Application Context
   * @param	techName			the technology name
   * @param version				the ModelKit version of the technology
   *                        	(ex. v13.0...)
   * 
   * @return fileName			the file name
   * 
   * @exception IcofException	Invalid directory, it doesn't exist and has been
   * 							coded to throw an exception and not create it
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructDeltaCompactedPacketXRefFileName(AppContext anAppContext
  																 ,String techName
																 ,String version)
  			throws IcofException {
  	
  	String dirName = constructAimIcofDKDirName(anAppContext
  	    									   ,techName
  	    									   ,version)
				   + Constants.COMPACTION_DIR + Constants.EDGE_DIR;
  	
  	//  Create the directory if it does not exist.
  	IcofFile temp = new IcofFile(dirName, true); //true if Directory
  	temp.validate(true);
  	
  	//  Finish constructing the file name.
    String fileName = dirName + "/" + Constants.ICC_DELTA + "." 
    				+ Constants.COMPACTED + "."
              		+ Constants.PACKET_XREF_EXTENSION;
  	
  	return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct the compacted.stop file name
   * 
   * @param anAppContext		Application Context
   * @param techNickname		the lowercase nickname of the technology
   *                        	for which to construct the directory name
   * @param version				the ModelKit version of the technology
   *                       		(ex. v13.0...)
   * 
   * @return filename			the file name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructCompactionStopFileName(AppContext anAppContext
  														,String techNickname
														,String version) {
  	
  	String fileName = constructAsicpatch2CompactedDirName(anAppContext
  	    												  ,techNickname
  	    												  ,version)
  	 				  + "/" + Constants.COMPACTION_STOP;
  	
  	return fileName;
  }
 
  
  //-----------------------------------------------------------------------------
  /**
   * Construct custom IP definition data file name
   * 
   * @param	 anAppContext		  Application Context
   * @param  techName			  the technology name
   * @param  version              the ModelKit version of the technology
   *                              (ex. v13.0...)
   * @param  orderID			  the ID for the order
   * @param	 shipToLabel		  shipping label
   * 
   * @return fileName			  the name of the construct file
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructCustomIpDefDataFileName(AppContext anAppContext
  														,String techName
														,String version
														,String orderID
														,short shipToID) 
  		throws IcofException {
    
  	String fileName = constructSharedDataDirName(anAppContext
  	    										 ,techName
  	    										 ,version);
  	
  	fileName += "/" + Constants.ICC_MODEL_KIT  + "." + orderID + "." + shipToID
  			 + "." + Constants.IIPMDS_IP_DEF_DATA;
  	
  	return fileName;
  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the ICC technology/version directory in which to check
   *   in release-specific files
   * 
   * @param	 anAppContext		  Application Context
   * @param  aTechRelease         a TechRelease object representing the technology
   *                              release for which to construct the directory name
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIccAsicTechDirName(AppContext anAppContext
                                                   ,TechRelease aTechRelease) {

    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if (anAppContext.getAppMode().equals(Constants.PROD)) {
      dirName += Constants.PROD_E_TECH_DIR;
    }
    else {
      dirName += Constants.DEV_E_TECH_DIR;
    }

    dirName += "/" + aTechRelease.getName(anAppContext) + "/" 
    		+  aTechRelease.getVersion(anAppContext);

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the ICC technology/version directory in which to check
   *   in product definition files
   * 
   * @param	 anAppContext		  Application Context
   * @param  aTechRelease         a TechRelease object representing the technology
   *                              release for which to construct the directory name
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIccProdDefnDirName(AppContext anAppContext
                                                   ,TechRelease aTechRelease) {

    String dirName = constructIccAsicTechDirName(anAppContext
        										 ,aTechRelease);

    dirName += Constants.PRODUCT_DEFN_DIR;

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the ICC technology/version directory in which to check
   *   in Design Kit release notes
   *
   * @param anAppContext		  Application Context
   * @param  aTechRelease         a TechRelease object representing the technology
   *                              release for which to construct the directory name
   *
   * @return                      the directory name
   *
   * @exception IcofException     Invalid application mode
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIccDKRelNotesDirName(AppContext anAppContext
                                                     ,TechRelease aTechRelease) {

    String dirName = constructIccAsicTechDirName(anAppContext
        										 ,aTechRelease);

    dirName += Constants.DK_RELNOTES_DIR;

    return dirName;

  }

  //-----------------------------------------------------------------------------
  /**
   * Construct the ICC technology/version directory in which to check
   *   in delta_releases_list and delta_packet_list
   *
   * @param	 anAppContext		  Application Context
   * @param  aTechRelease         a TechRelease object representing the technology
   *                              release for which to construct the directory name
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIccDeltaReleasesDirName(AppContext anAppContext
                                                        ,TechRelease aTechRelease) {

    String dirName = constructIccAsicTechDirName(anAppContext
        										 ,aTechRelease);

    dirName += Constants.DELTA_REL_DIR;

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the ICC technology/version directory in which to check
   *   in the baseModelKit
   *
   * @param	 anAppContext		  Application Context
   * @param  aTechRelease         a TechRelease object representing the technology
   *                              release for which to construct the directory name
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIccModelKitDirName(AppContext anAppContext
                                                   ,TechRelease aTechRelease) {

    String dirName = constructIccAsicTechDirName(anAppContext
        										 ,aTechRelease);

    dirName += Constants.MODEL_KIT_DIR;

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the ICC technology/version directory in which to check
   *   in the previewKit
   *
   * @param	 anAppContext		  Application Context
   * @param  aTechRelease         a TechRelease object representing the technology
   *                              release for which to construct the directory name
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIccPreviewKitDirName(AppContext anAppContext
                                                     ,TechRelease aTechRelease) {

    String dirName = constructIccAsicTechDirName(anAppContext
        										 ,aTechRelease);

    dirName += Constants.PREVIEW_KIT_DIR;

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the ICC technology/version directory in which to check
   *   in Preview Kit release notes
   *
   * @param	 anAppContext		  Application Context
   * @param  aTechRelease         a TechRelease object representing the technology
   *                              release for which to construct the directory name
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIccPKRelNotesDirName(AppContext anAppContext
      												 ,TechRelease aTechRelease) {

    String dirName = constructIccAsicTechDirName(anAppContext
        										 ,aTechRelease);

    dirName += Constants.PK_RELNOTES_DIR;

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the ICC technology/version directory in which to check
   *   in README files
   *
   * @param	 anAppContext		  Application Context
   * @param  aTechRelease         a TechRelease object representing the technology
   *                              release for which to construct the directory name
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIccReadMeDirName(AppContext anAppContext
                                                 ,TechRelease aTechRelease) {

    String dirName = constructIccAsicTechDirName(anAppContext
        										 ,aTechRelease);

    dirName += Constants.READMES_DIR;

    return dirName;

  }
 

  //-----------------------------------------------------------------------------
  /**
   * Construct the ICC TechInfo directory name in which to check
   *   in release-independent files
   * 
   * @param	 anAppContext		  Application Context
   * 
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIccTechInfoDirName(AppContext anAppContext) {

    // Construct directory name.
    String dirName = anAppContext.getHighLvlQualifier();
    if (anAppContext.getAppMode().equals(Constants.PROD)) {
      dirName += Constants.PROD_E_INFO_DIR;
    }
    else {
      dirName += Constants.DEV_E_INFO_DIR;
    }

    return dirName;

  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct initial_release_packet_list file name.
   * 
   * @param	 anAppContext		  	Application Context
   * @param	 techName				the technology name
   * @param  version				the ModelKit version of the technology
   *                        		(ex. v13.0...)
   * 
   * @return fileName				the file name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructInitReleasePacketListFileName(AppContext anAppContext
      														  ,String techName
      														  ,String version) { 
    String fileName = constructAimIcofDKDirName(anAppContext
        										,techName
        										,version) 
					+ "/" + Constants.INIT_RELEASE_PACKET_LIST;
    return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct lib group overrides file name.  This file contains overrides 
   * for a tech release for the delta.sys.subgroup.matrix
   *
   * @param	 anAppContext		Application Context
   * @param techNickname		the lowercase nickname of the technology
   *                        	for which to construct the directory name
   * @param version				the ModelKit version of the technology
   *                       		(ex. v13.0...)
   * 
   * @return filename			the file name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructLibGroupOverridesFileName(AppContext anAppContext
  														 ,String techNickname
														 ,String version) {
  	
  	String fileName = constructAimAdminDirName(anAppContext
  	    									   ,techNickname
  	    									   ,version)
					+ "/" + Constants.LIB_GROUP_OVERRIDES;
  	return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct master packet list file name.
   * 
   * @param	 anAppContext		Application Context
   * @param  techNickname		the lowercase nickname of the technology
   *                        	for which to construct the directory name
   * @param  useAsics			indicates use of asics directory
   * 
   * @return filename			the file name
   * 
   * @exception IcofException   Directory doesn't exist. In order for an exception
   * 							to be thrown, the code must be set to not create
   * 							the dir if it doesn't exist. 
   * 							See IcofFile.validate(boolean createIt)
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructMasterPacketListFileName(AppContext anAppContext
  														 ,String techNickname
														 ,boolean useAsics)
				throws IcofException {
    String dirName = anAppContext.getHighLvlQualifier();
    
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) || 
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      if (useAsics){
        dirName += Constants.PROD_ASICS;
      } 
      else {
        dirName += Constants.PROD_ASICSHIP;
      }
    } else if (anAppContext.getAppMode().equals(Constants.DEV)){
      if (useAsics){
        dirName += Constants.DEV_ASICS;
      } else {
        dirName += Constants.DEV_ASICSHIP;
      }
    } else if (anAppContext.getAppMode().equals(Constants.ICCTEST)){
        if (useAsics){
          dirName += Constants.ICCTEST_ASICS;
        } else {
          dirName += Constants.ICCTEST_ASICSHIP;
        }
    } else {
      if (useAsics){
        dirName += Constants.TEST_ASICS;
      } else {
        dirName += Constants.TEST_ASICSHIP;
      }
    }
    
    dirName += "/" + techNickname + Constants.PACKET;
    
    IcofFile dataDir = new IcofFile(dirName, true);
    dataDir.validate(true);
    
    String fileName = dirName + "/" + Constants.MASTER_PACKET_LIST;
    
    return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct mkSize file name.
   * 
   * @param	 anAppContext		Application Context
   * @param  techName			the technology name
   * @param  version			the ModelKit version of the technology
   *                        	(ex. v13.0...)
   * @param  orderID			the ID for the order
   * @param  shipToID			the numeric portion of a filename used for
   * 							shipping
   * @param  isMajor			indication of Major Release type 
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructMkSizeFileName(AppContext anAppContext
      										   ,String techName
      										   ,String version
      										   ,String orderID
      										   ,short shipToID
      										   ,boolean isMajor) 
  		throws IcofException {
    
    String fileName = constructSharedDataDirName(anAppContext
        										 ,techName
        										 ,version);
    
    if (isMajor) {
      fileName += "/" + Constants.ICC_MODEL_KIT;
    } else {
      fileName += "/" + Constants.ICC_DELTA;
    }
    
    fileName += "." + orderID + "." + shipToID + "." + Constants.SIZE_EXTENSION;
    
    return fileName;
    
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct packet contents file name.
   * 
   * @param	 anAppContext		    Application Context
   * @param	 techName				the technology name
   * @param  version				the ModelKit version of the technology
   *                        		(ex. v13.0...)
   * @param  packetName				name of the packet
   * @param includeExtension		true if extension should be included in
   * 								filename 
   * @return fileName				the file name
   * 
   * @exception	IcofException		Directory doesn't exist. In order for an exception
   * 								to be thrown, the code must be set to not create
   * 								the dir if it doesn't exist. 
   * 								See IcofFile.validate(boolean createIt)
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructLegacyFingerprintFileName(AppContext anAppContext
  														  ,String techName
  														  ,String version
  														  ,String packetName
  														  ,boolean includeExtension)
  			throws IcofException {
    
  	String fileName = constructAimIcofDKDirName(anAppContext
  	    										,techName
  	    										,version);
  	
  	fileName += Constants.CHECKSUMS;
  	
  	//Check for directory existance -- create if it doesn't exist
  	IcofFile dir = new IcofFile(fileName, true);
  	dir.validate(true);
  	
  	//  Finish constructing the file name.
    fileName += "/" + packetName;
  	
    //  Add the extension, if the caller requested it to be added.
    if (includeExtension) {
      fileName += "." + Constants.FINGERPRINT_EXTENSION;
    }
  	return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct packet history file name
   * 
   * @param	 anAppContext		    Application Context
   * @param	 techName				the technology name
   * @param  version				the ModelKit version of the technology
   *                        		(ex. v13.0...)
   * @param  orderID				the order ID
   * @param	 isIipmdsOrder			true is order is IIPMDS, false if Legacy
   * @return fileName				the file name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructPacketHistoryFileName(AppContext anAppContext
  													  ,String techName
  													  ,String version
  													  ,String orderID
  													  ,boolean isIipmdsOrder) 
  		throws IcofException {
  	
    String fileName = constructSharedDataDirName(anAppContext
        										 ,techName
        										 ,version);
    
    if (isIipmdsOrder) {
      fileName += "/" + Constants.ICC_MODEL_KIT;
    }
    else {
      fileName += "/" + Constants.ICC_DELTA;
    }
    fileName +=  "." + orderID + "." + Constants.PACKET_HISTORY_EXTENSION;
    
    return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct packetXRef file name for AsicConnect orders
   * 
   * @param	 anAppContext		Application Context
   * @param techName			the technology name
   * @param version				the ModelKit version of the technology
   *                        	(ex. v13.0...)
   * @param orderID				the ID for the order
   * @param shipToID			the numeric portion of a filename used for
   * 							shipping
   * @param orderType			order type 
   * 	
   * @return fileName			the name of the file
   * 
   * @exception IcofException	Directory doesn't exist. In order for an exception
   * 							to be thrown, the code must be set to not create
   * 							the dir if it doesn't exist. 
   * 							See IcofFile.validate(boolean createIt)
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructPacketXRefFileName(AppContext anAppContext
      											   ,String techName
      											   ,String version
      											   ,String orderID
      											   ,short shipToID
      											   ,String orderType
      											   ,boolean onShelf)
  		throws IcofException{
  	
    String fileName = constructAimIcofDKDirName(anAppContext
        										,techName
        										,version);
    fileName += Constants.COMPACTION_DIR + Constants.EDGE_DIR;
    
    //  Create the directory if it does not exist.
    IcofFile dir = new IcofFile(fileName, true); //true for directory type
    dir.validate(true);//true to create dir if non-existant
    
    //  Finish constructing the file name.
    fileName += "/" + orderType + "." + orderID + "."
    		 + shipToID;
    
    if (onShelf) {
      fileName += "." + Constants.ON_SHELF;
    }
    
    fileName += "." + Constants.PACKET_XREF_EXTENSION;
    
    return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct the ICC subdirectory name in which to check
   *   in release-dependent files
   *
   * @param	 anAppContext		  Application Context
   * @param  techRelease          a TechRelease object representing the technology
   *                              release for which to construct the directory name
   * @param  subDirName           the subDirectory underneath the techRelease
   *                              directory
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructIccSubDirName(AppContext anAppContext
      										  ,TechRelease techRelease
                                              ,String subDirName) {

    // Construct subdirectory name.
    String iccSubDirName = new String(techRelease.getName(anAppContext) + "/"
                                      + techRelease.getVersion(anAppContext));
    if (!iccSubDirName.equals("")) {
      iccSubDirName += subDirName;
    }

    return iccSubDirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aim/admin/(tech")/(ver)/Shipping/ directory
   *   name
   * 
   * @param  anAppContext         Application Context
   * @param  techNickname         the lowercase "nickname" for the technology
   *                              (ex. catamount, cu11, cu65lp)
   * @param  version              the ModelKit version of the technology
   *                              (ex. v13.0...)
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructShippingDirName(AppContext anAppContext
                                                ,String techNickname
                                                ,String version) {

    // Construct directory name.
    String dirName = "";
    if (anAppContext.getAppMode().equals(Constants.JUNIT)) {
      dirName = anAppContext.getHighLvlQualifier() + Constants.JUNIT_DATA_PATH;
    }
    else {
      dirName = constructAimAdminDirName(anAppContext
                                         ,techNickname
                                         ,version);
      dirName += Constants.SHIPPING_DIR;
    }

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aim/admin/(tech")/(ver)/Shipping/Processing/ directory
   *   name
   * 
   * @param  anAppContext         Application Context
   * @param  techNickname         the lowercase "nickname" for the technology
   *                              (ex. catamount, cu11, cu65lp)
   * @param  version              the ModelKit version of the technology
   *                              (ex. v13.0...)
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructShippingProcessingDirName(AppContext anAppContext
                                                ,String techNickname
                                                ,String version) {

    // Construct directory name.
    String dirName = constructShippingDirName(anAppContext, techNickname, version);
    dirName += Constants.PROCESSING_DIR;

    return dirName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the aim/admin/(tech")/(ver)/Shipping/Processing/compaction directory
   *   name
   * 
   * @param  anAppContext         Application Context
   * @param  techNickname         the lowercase "nickname" for the technology
   *                              (ex. catamount, cu11, cu65lp)
   * @param  version              the ModelKit version of the technology
   *                              (ex. v13.0...)
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructProcessingCompactionDirName(AppContext anAppContext
                                                ,String techNickname
                                                ,String version) {

    // Construct directory name.
    String dirName = constructShippingProcessingDirName(anAppContext, techNickname, version);
    dirName += Constants.COMPACTION_DIR;

    return dirName;

  }
  
  //-----------------------------------------------------------------------------
  /**
   * Construct the aim/admin/(tech")/(ver)/shipLists/(shipListType)/ directory
   *   name
   * 
   * @param	 anAppContext		  Application Context
   * @param  techNickname         the lowercase "nickname" for the technology
   *                              (ex. catamount, cu11, cu65lp)
   * @param  version              the ModelKit version of the technology
   *                              (ex. v13.0...)
   * @param  shipListType         "master" | "delivery", depending on whether
   *                              you are interested in what has actually been
   *                              delivered or what were candidates for delivery
   *
   * @return                      the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructShipListDirName(AppContext anAppContext
                                                ,String techNickname
                                                ,String version
                                                ,String shipListType) {

    // Construct directory name.
    String dirName = "";
    if (anAppContext.getAppMode().equals(Constants.JUNIT)) {
      dirName = anAppContext.getHighLvlQualifier() + Constants.JUNIT_DATA_PATH;
    }
    else {
      dirName = constructAimAdminDirName(anAppContext
       									 ,techNickname
        								 ,version);
      dirName += Constants.SHIPLISTS + "/" + shipListType;
    }

    return dirName;

  }
  
  //-----------------------------------------------------------------------------
  /**
   * Construct the aim/admin/(tech")/(ver)/shipLists/(shipListType)/(fileName)
   *  full path file name
   * 
   * @param	anAppContext	Application Context
   * @param aTechRelease	Technology Release object
   * @param orderNumber		order number as a string
   * @param shipListType	Ship List type as a string
   * @param shipToLabl		Ship To label if one exists (else use "")
   * @param isMajor			boolean to indicate if the ship list is for a major relase
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructShipListFileName(AppContext anAppContext
      											 ,TechRelease aTechRelease
      											 ,String orderNumber
      											 ,String shipListType
      											 ,String shipToLabel
      											 ,boolean isMajor) throws IcofException {
    String dirName = 
      IcofUtil.constructShipListDirName(anAppContext
               ,aTechRelease.getNickname(anAppContext)
               ,aTechRelease.getVersion(anAppContext)
               ,shipListType);
    
    // Ensure directory exists
    IcofFile directory = new IcofFile(dirName, true);
    if (!directory.exists()) {
      directory.create();
    }

    String fileName =  dirName + File.separator;
    if (isMajor) {
      fileName += Constants.ICC_MODEL_KIT;
    }
    else {
      fileName += Constants.ICC_DELTA;
    }

    fileName += "." + orderNumber;

    if ((!orderNumber.equals(Constants.TPK_ORDER_NUMBER)) &&
        (!orderNumber.equals(Constants.GENERIC_ORDER_NUMBER))) {

      if (!shipToLabel.equals( "" )) {

        fileName += "." + shipToLabel;
      }
    }

    fileName += "." + Constants.SHIPLIST_EXTENSION;

    return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct software.contacts file name
   *
   * @param	 anAppContext		  Application Context
   * @return fileName 			  the file name
   * 
   * @exception IcofException	Directory doesn't exist. In order for an 
   * 							exception to be thrown, the code must be set to 
   * 							not create the dir if it doesn't exist. 
   * 							See IcofFile.validate(boolean createIt)
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructSoftwareContactsFileName(AppContext anAppContext)
  		throws IcofException {
    
    String dirName = anAppContext.getHighLvlQualifier();
    if ((anAppContext.getAppMode().equals(Constants.ICCPROD)) ||
        (anAppContext.getAppMode().equals(Constants.PROD))) {
      dirName += Constants.PROD_AES_SYS_ADMIN;
    } 
    else if (anAppContext.getAppMode().equals(Constants.DEV)) {
      dirName += Constants.DEV_AES_SYS_ADMIN;
    } 
    else if (anAppContext.getAppMode().equals(Constants.ICCTEST)) {
        dirName += Constants.ICCTEST_AES_SYS_ADMIN;
      } 
    else {
      dirName += Constants.DEV_AES_SYS_ADMIN;
    }
    
    IcofFile dir = new IcofFile(dirName, true); //true for dir, false for file
    dir.validate(true); //true to create if non-existant
    
    String fileName = dirName + "/" + Constants.SOFTWARE_CONTACTS;
    
    return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Constuct tarList file name
   *
   * @param	anAppContext		Application Context
   * @param techName			the technology name
   * @param version				the ModelKit version of the technology
   *                       	 	(ex. v13.0...)
   * @param orderID				the ID for the order
   * @param shipToID			the numeric portion of a filename used for
   * 							shipping
   * @param isMajor				indication of Major Release type
   * 
   * @return					file name for a .tarList file Name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructTarListFileName(AppContext anAppContext
												,String techName
												,String version
												,String orderID
												,short shipToID
												,boolean isMajor) 
  		throws IcofException {
    
  	String fileName = constructSharedDataDirName(anAppContext
  	    										 ,techName
  	    										 ,version);
  	
  	if (isMajor) {
  	  fileName += "/" + Constants.ICC_MODEL_KIT;
  	}
  	else {
  	  fileName += "/" + Constants.ICC_DELTA;
  	}
  	fileName += "." + orderID + "." + shipToID + "." + Constants.TARLIST_EXTENSION;
  	
  	return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct .toolMTs file name
   * @param	anAppContext		Application Context
   * @param techName			the technology name
   * @param version				the ModelKit version of the technology
   *                       	 	(ex. v13.0...)
   * @param orderNumber			the order number
   * @param isMajor				indication of Major Release type
   * 
   * @return fileName			the ship list file name 
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructToolMTFileName(AppContext anAppContext
											   ,String techName
											   ,String version
											   ,String orderNumber
											   ,boolean isMajor) 
  			throws IcofException {
    
    String fileName = constructSharedDataDirName(anAppContext
		   										 ,techName
		   										 ,version);
    
    if (isMajor) {
      fileName += "/" + Constants.ICC_MODEL_KIT;
    } 
    else {
      fileName += "/" + Constants.ICC_DELTA;
    }
    
    fileName += "." + orderNumber + "." + Constants.TOOL_MTS_EXTENSION;
    
    return fileName;
  	
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct tools.map file name
   * 
   * @param	anAppContext		  Application Context
   * @param  techNickname         the lowercase "nickname" for the technology
   *                              (ex. catamount, cu11, cu65lp)
   * @param  version              the ModelKit version of the technology
   *                              (ex. v13.0...)
   * 
   * @return fileName             the directory name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructToolsMapFileName(AppContext anAppContext
	   											 ,String techNickname
	   											 ,String version) {
  	
  	String fileName = constructAimAdminDirName(anAppContext
			 								   ,techNickname
			 								   ,version);
  	
  	fileName += "/" + Constants.TOOLS_MAP;
  	
    return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct delta_packet_list file name.
   * 
   * @param	anAppContext		Application Context
   * @param techNickname		the lowercase nickname of the technology
   *                        	for which to construct the directory name
   * @param version				the ModelKit version of the technology
   *                       		(ex. v13.0...)
   * 
   * @return filename			the file name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructDeltaPacketListFileName(AppContext anAppContext
  														,String techNickname
														,String version) {
  	
  	String fileName = constructAsicpatch2DirName(anAppContext
  	    										 ,techNickname
  	    										 ,version); 
  	fileName += "/" + Constants.DELTA_PACKET_LIST;
  	
  	return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct delta_releases_list file name.
   * 
   * @param	anAppContext		Application Context
   * @param techNickname		the lowercase nickname of the technology
   *                        	for which to construct the directory name
   * @param version				the ModelKit version of the technology
   *                       		(ex. v13.0...)
   * 
   * @return filename			the file name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructDeltaReleasesListFileName(AppContext anAppContext
  														  ,String techNickname
  														  ,String version) {
  	
  	String fileName = constructAsicpatch2DirName(anAppContext
  	    										 ,techNickname
  	    										 ,version); 
  	fileName += "/" + Constants.DELTA_RELEASES_LIST;
  	return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct exported order file name.
   * 
   * @param	anAppContext	Application Context 
   * @param techName		the technology name
   * @param version			the ModelKit version of the technology
   *                        (ex. v13.0...)
   * @param orderNumber		the number for the order
   * @param isFinalOrder 	indicates whether the order is final:
   * 						determines the need for .filtered extension
   * @param isMajor			indication of Major Release type
   * 
   * 
   * @return				file name for order file
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructExportedOrderFileName(AppContext anAppContext
      												  ,String techName
      												  ,String version
      												  ,String orderNumber
      												  ,boolean isFinalOrder
      												  ,boolean isMajor) 
  		throws IcofException {
    
    String fileName = constructSharedDataDirName(anAppContext
		  										 ,techName
		  										 ,version);
    
    if (isMajor) {
      fileName += "/" + Constants.ICC_MODEL_KIT;
    } 
    else {
      fileName += "/" + Constants.ICC_DELTA;
    }
    
    fileName += "." + orderNumber;
    
    if (isFinalOrder) {
      fileName += "." + Constants.FILTERED_EXTENSION;
    }
    
    return fileName;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   *  Construct the 
   *  aes/<appMode>/revisionCustomers/<tech>  directory name.
   * 
   * @param   anAppContext  Application Context
   * @param   techName    the technology name
   * 
   * @return  the directory name that was constructed
   */
  //-----------------------------------------------------------------------------
  public static synchronized 
  String constructRevisionCustomersDirName(AppContext anAppContext,
                                           String techName)
        throws IcofException {
    
    // Construct directory name
    String dirName = constructAesDirName(anAppContext) + Constants.REVISION_CUSTOMERS
                   + IcofFile.separator + techName;
    
    //validate directory exists -- create it if it doesn't
    IcofFile temp = new IcofFile(dirName, true); //pass true for directory type
    temp.validate(true);
    
    return dirName;
    
  }  
  
  
  //-----------------------------------------------------------------------------
  /**
   *  Construct the name of the file containing the build lists that were used
   *    to build the specified dip revision
   * 
   * @param   anAppContext  Application Context
   * @param   techName    the technology name
   * @param   dipName     the dip name
   * @param   dipVersion  the dip version
   * @param   dipRevision the dip revision
   * 
   * @return  the file name that was constructed
   */
  //-----------------------------------------------------------------------------
  public static synchronized 
  String constructDipContentFileName(AppContext anAppContext,
                                     String techName,
                                     String dipName,
                                     String dipVersion,
                                     String dipRevision)
        throws IcofException {
    
    // Construct file name
    String fileName = constructIipmdsStagedDipContentDirName(anAppContext, techName, dipName, dipVersion)
                    + File.separator + dipName + "_" + dipVersion + "_" + dipRevision
                    + Constants.FULL_CONTENT_EXTENSION;

    // 
    return fileName;
    
  }  
 
  
  //-----------------------------------------------------------------------------
  /**
   * Construct the name of the file of obsolete packets for the specified 
   * techRelease and dip. 
   *   
   * @param anAppContext  Application Context
   * @param aTechName     the name of the technology (ex. CU65HP)
   * @param aRelease      the name of the release (ex. rel4.0)
   * @param aDipNameVer   the dip version, formatted as dipName_dipVersion 
   *                      (ex. sc_lvt_v004)
   * @param isCompacted   true, to construct name of file containing obsolete 
   *                      compacted packets; 
   *                      false, to construct name of file containing obsolete 
   *                      regular packets
   * @return              Fully-pathed file name                     
   * @throws IcofException
   */
  //-----------------------------------------------------------------------------
  public static synchronized String constructObsoletePacketMapFileName(AppContext anAppContext, 
                                                                       String aTechName,
                                                                       String aRelease,
                                                                       String aDipNameVer, 
                                                                       boolean isCompacted) 
  throws IcofException {

  
      // Construct file name
      String dirName = 
          IcofUtil.constructAesIipmdsTechVerPacketMapsDirName(anAppContext, 
                                                              aTechName, 
                                                              aRelease);
      dirName += Constants.OBSOLETE_BL;
      
      IcofFile directory = new IcofFile(dirName, true);
      directory.validate(true);
      
      String fileName = dirName + IcofFile.separator + 
                        aDipNameVer;
      
      if (isCompacted) {
          fileName += Constants.COMPACTED_EXTENSION;
      }
      else {
          fileName += Constants.PACKETS_EXTENSION;

      }
      
      fileName += Constants.OBSOLETE_BL_EXTENSION;
      
      return fileName;
  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the name of a released packetMap file for the specified
   * tech, release, and dip version.
   *
   * @param     anAppContext    Application Context
   * @param     techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param     relName         the lowercase technology release name
   *                            (ex. rel1.0, rel2.0,...)
   * @param     dipName         the dip name (ex. sc_lvt)
   * @param     dipVersion      the dip version (ex v003)
   * @param     isCompacted     true, to construct the compacted packetMap file
   *                            name; false, to construct the regular packetMap
   *                            file name                           
   *
   * @return                    the file name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String 
  constructReleasedDipPacketMapFileName(AppContext anAppContext,
                                        String techName,
                                        String relName,
                                        String dipName,
                                        String dipVersion,
                                        boolean isCompacted) 
  throws IcofException {

    // Construct directory name.
    String dirName = constructAesIipmdsTechPacketReleasedDirName(anAppContext,
                                                                  techName,
                                                                  relName);
    // Check to see if the directory exists.
    IcofFile packetMapDir = new IcofFile(dirName, true);
    packetMapDir.validate(true);
    
    // Finish constructing file name.
    String fileName = dirName + IcofFile.separator 
    + DipPacketMap.constructReleasedPacketMapName(anAppContext, dipName, dipVersion);
    if (isCompacted) { 
        fileName += "." + Constants.COMPACTED;
    }
    
    return fileName;

  }


  //-----------------------------------------------------------------------------
  /**
   * Construct the name of a retracted packetMap file for the specified
   * tech, release, and dip version.
   *
   * @param     anAppContext    Application Context
   * @param     techName        the uppercase technology name
   *                            (ex. CU11, CU08, CU65LP,...)
   * @param     relName         the lowercase technology release name
   *                            (ex. rel1.0, rel2.0,...)
   * @param     dipName         the dip name (ex. sc_lvt)
   * @param     dipVersion      the dip version (ex v003)
   * @param     isCompacted     true, to construct the compacted packetMap file
   *                            name; false, to construct the regular packetMap
   *                            file name                           
   *
   * @return                    the file name
   */
  //-----------------------------------------------------------------------------
  public static synchronized String 
  constructRetractedPacketMapFileName(AppContext anAppContext,
                                      String techName,
                                      String relName,
                                      String dipName,
                                      String dipVersion,
                                      boolean isCompacted) 
  throws IcofException {

    // Construct directory name.
    String dirName = constructAesIipmdsTechPacketRetractedDirName(anAppContext,
                                                                  techName,
                                                                  relName);
    // Check to see if the directory exists.
    IcofFile packetMapDir = new IcofFile(dirName, true);
    packetMapDir.validate(true);
    
    // Finish constructing file name.
    String fileName = dirName + IcofFile.separator 
    + DipPacketMap.constructReleasedPacketMapName(anAppContext, dipName, dipVersion);
    if (isCompacted) { 
        fileName += "." + Constants.COMPACTED;
    }
    fileName += "." + Constants.RETRACT;
    
    return fileName;

  }
  
  
  //-----------------------------------------------------------------------------
  /**
   *  Construct the 
   *  aes/<appMode>/revisionCustomers/<tech>/<dipRev>.customers.<techVer>
   *    file name.
   * 
   * @param   anAppContext  Application Context
   * @param   techName    the technology name
   * @param   version     the ModelKit version of the technology;
   *                      if an empty string is passed, the word
   *                      "ALL" will be used in place of the version
   *                      (ex. rel4.0)
   * @param   dipRevision a dip revision specified as dipName_dipVer_dipRevision
   *                      (ex. sc_rvt_v003_r00005)                     
   * 
   * @return  the file name that was constructed
   */
  //-----------------------------------------------------------------------------
  public static synchronized 
  String constructRevisionCustomersFileName(AppContext anAppContext,
                                            String techName,
                                            String version,
                                            String dipRevision) 
        throws IcofException {
    
    // Construct file name
    String fileName = constructRevisionCustomersDirName(anAppContext, techName)
                    + IcofFile.separator + dipRevision + ".customers.";
    if (version.equals("")) {
        version = Constants.ALL_TECH_RELEASES;
    }
    
    fileName += version;
    
    return fileName;
    
  }


  //-----------------------------------------------------------------------------
  /**
   * Remove a file from the edesign staging area.
   *
   * @param	 anAppContext		  Application Context
   * @param     fileName          The name of the file, with no path, to be
   *                              removed
   * @param     edesignDirName    The partial directory name from which to
   *                              remove the file
   * @exception IcofException     Bad return code from edesign script
   */
  //-----------------------------------------------------------------------------
  public static synchronized void removeEdesignFile(AppContext anAppContext
									   ,String fileName
                                       ,String edesignDirName)  
  		throws IcofException {

    String funcName = new String("removeEdesignFile(AppContext, String, String)");

    String stagingRemoveCmd = new String(Constants.EDESIGN_STAGING_SCRIPT_DIR + "/" +
                                         Constants.EDESIGN_REMOVE_FILE_SCRIPT +
                                         " " +
                                         fileName +
                                         " " +
                                         edesignDirName);

    try {
      Process removeIt = Runtime.getRuntime().exec(stagingRemoveCmd);

      int removeItRc = removeIt.waitFor();

      if (removeItRc != 0) {
        IcofException ie = new IcofException(CLASS_NAME
                                             , funcName
                                             , IcofException.SEVERE
                                             , "Bad return code, "
                                             + String.valueOf(removeItRc)
                                             + ", from "
                                             + Constants.EDESIGN_REMOVE_FILE_SCRIPT
                                             + " when removing "
                                             + fileName
                                             + " from "
                                             + edesignDirName
                                             , stagingRemoveCmd);
        anAppContext.getSessionLog().log(ie);
        throw(ie);
      }
    }
    catch(Exception e) {
      IcofException ie = new IcofException(CLASS_NAME
                                           ,IcofException.SEVERE
                                           ,funcName
                                           ,e.toString()
                                           ,"");
      anAppContext.getSessionLog().log(ie);
      throw ie;

    }
  }

  //-----------------------------------------------------------------------------
  /**
   * Validate the customer type.
   *
   * @param	 	anAppContext	  Application Context
   * @param     customerType      the customer type to be validated
   * 
   * @exception IcofException     invalid customer type
   */
  //-----------------------------------------------------------------------------
  public static synchronized void validateCustomerType(AppContext anAppContext
      									  ,String customerType)
      throws IcofException {

    String funcName = new String("validateCustomerType(AppContext, String)");

    // Make sure customerType is valid.
    if ((!customerType.equals(Constants.REGULAR_CUSTOMER)) &&
        (!customerType.equals(Constants.TOOL_VENDOR_CUSTOMER)) &&
        (!customerType.equals(Constants.FOUNDRY_CUSTOMER))) {

      IcofException ie = new IcofException(CLASS_NAME
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Invalid customer type"
                                           ,customerType);
      anAppContext.getSessionLog().log(ie);
      throw(ie);
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Validate the IP Category.
   *
   * @param	    anAppContext	  Application Context
   * @param     ipCategory        the IP category to be validated
   * 
   * @exception IcofException     invalid IP category
   */
  //-----------------------------------------------------------------------------
  public static synchronized void validateIPCategory(AppContext anAppContext
		  							    ,String ipCategory)
      throws IcofException {

    String funcName = new String("validateIPCategory(AppContext, String)");

    // Make sure ip category name is valid.
    if ((!ipCategory.equals(Constants.IP_BASE)) &&
        (!ipCategory.equals(Constants.IP_BASE_ORD)) &&
        (!ipCategory.equals(Constants.IP_CORE))) {

      IcofException ie = new IcofException(CLASS_NAME
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Invalid IP Category"
                                           ,ipCategory);
      anAppContext.getSessionLog().log(ie);
      throw(ie);
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Validate the lib group type.
   *
   * @param	    anAppContext	  Application Context
   * @param     libGrpType        the lib group type to be validated
   * 
   * @exception IcofException     invalid lib group type
   */
  //-----------------------------------------------------------------------------
  public static synchronized void validateLibGrpType(AppContext anAppContext
	    								,String libGrpType)
      throws IcofException {

    String funcName = new String("validateLibGrpType(AppContext, String)");

    // Make sure lib grp type name is valid.
    if ((!libGrpType.equals(Constants.BASE)) &&
        (!libGrpType.equals(Constants.BASE_ORD)) &&
        (!libGrpType.equals(Constants.CORE)) &&
        (!libGrpType.equals(Constants.CUSTOMER)) &&
        (!libGrpType.equals(Constants.BASE_FOUNDRY)) &&
        (!libGrpType.equals(Constants.BASE_VW)) &&
        (!libGrpType.equals(Constants.BASE_ORD_VW)) &&
        (!libGrpType.equals(Constants.CORE_VW)) &&
        (!libGrpType.equals(Constants.CUSTOMER_VW)) &&
        (!libGrpType.equals(Constants.LGT_FOUNDRY_CUSTOMER)) &&
        (!libGrpType.equals(Constants.LGT_FOUNDRY_CUSTOMER_VW)) &&
        (!libGrpType.equals(Constants.LGT_FOUNDRY_SHARE)) &&
        (!libGrpType.equals(Constants.LGT_FOUNDRY_SHARE_VW))) {

      IcofException ie = new IcofException(CLASS_NAME
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Invalid lib group type"
                                           ,libGrpType);
      anAppContext.getSessionLog().log(ie);
      throw(ie);
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Validate the tool type.
   *
   * @param	 anAppContext		  Application Context
   * @param     toolType          the tool type to be validated
   * @exception IcofException     invalid tool type
   */
  //-----------------------------------------------------------------------------
  public static synchronized void validateToolType(AppContext anAppContext
									  ,String toolType)
      throws IcofException {

    String funcName = new String("validateToolType(AppContext, String)");

    // Make sure tool type name is valid.
    if ((!toolType.equals(Constants.STANDARD)) &&
        (!toolType.equals(Constants.NON_STANDARD_GA)) &&
        (!toolType.equals(Constants.NON_STANDARD_R)) &&
        (!toolType.equals(Constants.PREVIEW_KIT)) &&
        (!toolType.equals(Constants.PLATFORM_GA)) &&
        (!toolType.equals(Constants.PLATFORM_R)) &&
        (!toolType.equals(Constants.FOUNDRY_STANDARD)) &&
        (!toolType.equals(Constants.FOUNDRY_PREVIEW_KIT))) {

      IcofException ie = new IcofException(CLASS_NAME
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Invalid tool type"
                                           ,toolType);
      anAppContext.getSessionLog().log(ie);
      throw(ie);
    }

  }
  
  /**
   * Round the input num up to a mathematical integer if it has a
   * decimal of .5 or greater. Round the input num down to a
   * mathematical integer if it has a decimal of less than .5.
   * 
   * @param num	The double to be rounded
   * @return The rounded input num as a Java type long.
   */
  public static synchronized long roundToLong(double num) {
    double diff = num - (long)num;
    if (diff >= .5) {
      num += .5;
    }
    return (long)num;
  }

  //-----------------------------------------------------------------------------
  // Data elements.
  //-----------------------------------------------------------------------------
  private static final String CLASS_NAME = "IcofUtil";


}


//==========================  END OF FILE  ====================================
