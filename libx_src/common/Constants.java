/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2000 - 2011 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: Constants.java
*
* CREATOR: Karen K. Kellam
*    DEPT: 5ZIA
*    DATE: 12/11/2000
*
*-PURPOSE---------------------------------------------------------------------
* Constants class definition file.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 12/11/2000 KKK  Initial coding.
* 10/10/2001 KKK  Added booleanToString(..) and stringToBoolean(..) functions.
* 10/11/2001 KKK  Added constant for tech.acos.data file.  Added handleDBNull
*                 functions.
* 10/24/2001 KKK  Added constants for valid role names.
* 10/31/2001 KKK  Added constants for additional files, containing FSE info.
* 11/15/2001 KKK  Added PREVIOUSLY_ORDERED and REMOVED constants.
* 11/29/2001 KKK  Renamed RESTRICTED_CORE and GENERALLY_AVAILABLE_CORE
*                 constants to RESTRICTED and GENERALLY_AVAILABLE.  Added
*                 SHIPPING_BASE_ORDERABLE.
* 12/11/2001 KKK  Added readStream(..)
* 12/17/2001 KKK  Added constants for Acos Exported Order file.
* 01/04/2002 KKK  Changed strings for order status and suborder status
*                 constants.
* 01/09/2002 KKK  Added isValidEmailAddress(..), which was a member of
*                 the IcofEmail class.
* 01/24/2002 KKK  Added customerType field to entitlement table and added
*                 constants to ensure it gets written to customer.list file
* 03/05/2002 KKK  Incorporated Sandra's getFirstName() and getLastName()
*                 functions
* 03/20/2002 KKK  Converted to Java 1.2.2.
* 04/03/2002 KKK  Removed readStream() and writeStream(), because they have
*                 been incorporated into the IcofStream class.
* 04/12/2002 KKK  Added 4 role constants.
* 04/30/2002 KKK  Added 2 tool types (Platform-GA and Platform-R).
* 01/17/2003 KKK  Added constants for $aim/icof/dk.. and functions to
*                 construct that directory name.  Removed constant PM_PHONE_NBR
* 02/11/2003 KKK  Added new libGrpType, toolTypes, and customerType to
*                 support delivery of FoundryModelKit.  Added new functions,
*                 validateLibGrpType(..), validateCustomerType(..), and
*                 validateToolType(..).
* 04/07/2003 KKK  Added new constants, FOUNDRY and NON_FOUNDRY.
* 06/27/2003 KKW  Added new constants, FOUNDRY_CORES and FOUNDRY_BASE_ORDERABLE.
* 07/11/2003 KKW  Added new constants, TECH_RELEASE_DATA.
* 07/28/2003 KKW  Added new constants, IGNORE_FOUNDRY_MT_LG and
*                 IGNORE_FOUNDRY_MT_LG_APP, as well as
*                 determineIgnoreFoundryMtLG(..) function.  Also added
*                 constants for $aim/icof/log directory, including dev and
*                 test variants, and a constructAimIcofLogDirName function.
* 09/16/2003 SMP  Added new contant, TOOLS_MAP.
* 04/08/2004 KKW  Added new constants VERIFY_TECH_FILES, TECH_SWITCH,
*                 VERSION_SWITCH, and DEV_SWITCH.
* 05/27/2004 KKW  Added new constants AFS_USERID_LENGTH
* 10/13/2004 KKW  Added new constants ASICPATCH2, ASICDELTA, SYSLOG,
*                 PATCH, DELTA_RELEASES_LIST, DELTA_PACKET_LIST,
*                 PACKET_ENTRY, RELEASE_ENTRY, DELTA_DONE, DELTA_PENDING.
*                 Added new functions constructAsicpatch2DirName,
*                 constructAsicdeltaSyslogDirName.
* 01/06/2005 KKW  Added constant FOURTEEN_DAYS.
* 03/16/2005 GFS  Added execSystemCommand(String, StringBuffer, Vector) and
*                 execSystemCommand(String[], StringBuffer, Vector) methods.
* 03/23/2005 KKW  Added 4 new LibGrpTypes to support the creation of versioned
*                 lib groups for iipmds.
* 03/25/2005 KKW  Added constants for new $aes/iipmds.... directory structure
*                 and some functions to create certain directory names.
* 03/30/2005 KKW  Added constants for new BaseOrd prefixes.
* 04/19/2005 KKW  Added several constructIcc... methods for constructing
*                 directory names to which files will be staged.
* 05/04/2005 KKW  Updated execSystemCommand functions to use new StreamGobbler
*                 class which will read the stdOut/stdErr streams while
*                 the system command is executing.  This change takes care
*                 of system call hangs, due to buffers filling up.
* 05/05/2005 KKW  Added a new flavor of execSystemCommand that will execute
*                 the system command in a specified directory.
* 06/14/2005 GFS  Fixed PROD_AES_BIN constant.
* 06/17/2005 GFS  Updated execSystemCommand() method to set the errorMsg to
*                 stdErr even if the rc from the system call is good.
* 07/28/2005 KKW  Finished adding javadoc to last few methods.
* 08/04/2005 KKW  Added constants to support on-demand rsync of data to ICC.
* 09/14/2005 KPL  Added constants for App wrappers for various tools for ICC.
* 12/15/2005 KKW  Split constants into multiple *Util classes, leaving just
*                 a few methods in this class.
* 12/27/2005 GFS  Added constant for packetMaps directory.
* 03/28/2006 KPL  Added constants for new tables LibRelState and TechibState
*                 and PERCENT now a release name delimiter in customer.list
* 04/21/2006 KKW  Added constant ALL_RELEASES_DATA
* 04/24/2006 AAK  Added constant REL_NOTE_EXTENSION and IIPMDS_RELEASE_IP_REVISIONS
* 05/12/2006 KKW  Changed development edesign directory constants to point to
*                 the icctest2 (staging.int2) environment.
* 06/--/2006 RAM  Added a large number of constants that existed in our C++ 
* 				  library and did not yet exist in our java library
* 10/16/2006 KKW  Added COMPACTED_PACKET_ABBREV and a number of other constants
*                 used during compaction
* 12/13/2006 AAK  Added constant REV_ALERT and REV_ALERT_EXTENSION for revision
*                 alert work.
* 01/03/2007 RAM  Added ALL_MDL_TYPES for MajorDKOrder.java to use in read()
* 01/10/2007 RAM  Added Packet Types DEL, SRC, BOTH for MajorKit.java and
* 				  CUSTOMER_SHIP_TO_LN
* 01/24/2007 GFS  Added FULL_CONTENT, OBSOLETE and PACKET_EXTENSION, POCKET,
*                 OBSOLETE_EXTENSION, FULL_CONTENT_EXTENSION and COMPACTED
*                 constants. Added CONSUMER_TYPE_* constants.
* 02/26/2007 KKW  Added constant SAMBA_BTV and changed most path constants not
*                 to include /afs/btv/data as the top qualifier.
* 02/27/2007 GFS  Updated PROD_AES_BIN and DEV_AES_BIN.  Added AES_APPS.
* 04/02/2007 KKW  Merged iipmds and rewrite stream.
* 04/03/2007 KKW  Removed unused constants.
* 05/22/2007 KKW  Renamed LATEST_RELEASED_DATA to LATEST_RELEASE_DATA and 
*                 changed its value, too. 
* 07/12/2007 KKW  Added SAMBA_DRIVE_LETTER and SAMBA_AFS_BTV_LINK_NAME.
* 11/16/2007 GFS  Added TIMING_DATA_DIR constant.
* 01/10/2008 GFS  Added PATCH_REVISION_ID constant.
* 01/30/2008 GFS  Added EDESIGN_RSYNC_MIGRATION_FILE_SCRIPT constant.
* 04/15/2008 KKW  Removed unused constants: QUEUE, SHIPPING, REMOTE_HOST,
*                 TIMEOUT, USED_BY, NOT_APPLICABLE.  Removed INFO, WARNING,
*                 TRACE, EXCEPTION as these are also defined in the
*                 IcofException class and those are the ones that
*                 should be used.
* 04/18/2008 KKW  Added RELEASE_HISTORY     
* 05/07/2008 AAK  Added DESCRIPTION for log constant, RETRACT, RETRACTED and 
*                 IIPMDS_RETRCTED_IP_REVISIONS            
* 05/08/2008 KKW  Added new appMode (JUNIT) and JUNIT_DATA_PATH
* 05/13/2008 KKW  Added constants DB_ACCESS_ID and DB_ACCESS_CODE.
* 05/14/2008 KKW  Added constants REVISION_CUSTOMERS and ALL_TECH_RELEASES    
* 5/19/2008  AAK  Added REVISION_ZERO   
* 06/17/2008 GFS  Added DIP_CONTENT and DIP_OBSOLETE constants.   
* 08/04/2008 GFS  Added PROD_AES_SYS_ADMIN, DEV_AES_SYS_ADMIN and SYS_ADMIN.
*                 Removed the *_AIM_ICOF_SWADMIN constants.
* 11/06/2008 KKW  Renamed constants FOUNDRY and NON_FOUNDRY to 
*                 NON_ASIC and ASIC, respectively, and changed
*                 their values, likewise.  Work is in support of
*                 ASPRD 508.                 
* 12/08/2008 GFS  Added DIPPREP constant.
* 02/06/2009 AS   Added JUnit constants.
* 02/17/2009 GFS  Added IPACKETS and REVISION_MAX constants.
* 02/23/2009 AAK  Added constant for .retracted extension and for retraction 
*                 template file name
* 03/06/2009 KKW  Added constant OBSOLETE_BL and OBSOLETE_BL_EXTENSION for 
*                 identifying packets built from build lists that have since 
*                 been obsoleted. 
* 03/09/2009 KKW  Changed DEV_STAGING_DIR from icc test2 environment to
*                 iccfix environment for retraction testing.   
* 03/27/2009 GFS  Added OVERRIDES_EXTENSION, TEST_CONFIG, ASICTEST_CONFIG and
*                 LATEST_TEST_CONFIG constants.
* 05/11/2009 KKW  Changed DEV_STAGING_DIR from iccfix environment to
*                 icctest2 environment for testing their daemons.
* 05/28/2009 KKW  Changed userid for connecting to our db (DB_ACCESS_ID constant)
*                 to a new local id, in conjunction with moving our db to new
*                 Linux servers with db2 v9.5 installed.  AFS authentication is
*                 not available in db2 v9.5.
* 08/12/2009 KKW  Added constant for "WINDOWS".
* 11/09/2009 KKW  Added constant XML_EXTENSION.
* 11/12/2009 KKW  Added constant ERR_EXTENSION.
* 02/17/2010 KKW  Added constants SHIPPING_DIR and PROCESSING_DIR and USED_BY
* 02/19/2010 KKW  Changed type of MAX_FILE_SIZE from int to long in preparation
*                 for increasing the value to a number larger than 2GB.  Also 
*                 added HUNDRED_GIG constant.
* 03/23/2010 KKW  Added new application mode (ICCTEST).
* 04/07/2010 KKW  Added ONE_GB, AFS_FILE_SYSTEM, GSA_FILE_SYSTEM, LOCAL_FILE_SYSTEM
* 04/09/2010 KKW  Added directory constants in support of new ICCTEST appmode.
* 05/27/2010 KKW  Changed value for DEFAULT_NUM_TRIES to 5 (was 30) and NUM_30SEC_TRIES
*                 to 3 (was 60).
* 05/28/2010 KKW  Changed DEV_STAGING_DIR to $aes/dev/iccEnvLink/active_env,
*                 which is a link to either /afs/btv/data/edesign/edsd/staging.fix
*                 OR /afs/btv/data/edesign/edsd/staging.int2.  This will allow
*                 us to use either environment without having to recompile
*                 our applications.            
* 06/07/2010 KKW  Changed MAX_FILE_SIZE constant from 2GB to 7GB.
* 06/10/2010 KKW  Changed name of MAX_FILE_SIZE to MAX_FILE_SIZE_AFS_LFE and changed value to 64GB 
*                 as this is the maximum file size for large-file-enabled afs.
*                 Added MAX_FILE_SIZE_AFS and set it to 2GB.
* 02/21/2011 KKW  Added constant USER_HOME_PROPERTY_TAG.  Removed constants
*                 related to ICC rsync.
* 04/19/2011 KKW  Added four new lib group type constants for Foundry (ASPRD 792)
*                 LGT_FOUNDRY_CUSTOMER, LGT_FOUNDRY_CUSTOMER_VW,
*                 LGT_FOUNDRY_SHARE, LGT_FOUNDRY_SHARE_VW                                                                                         
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.common;
import java.util.Date;


public class Constants {

  // General constants
  public static final short ONE_K = 1024;
  public static final long ONE_GB = 1024000000;
  public static final long MAX_FILE_SIZE_AFS_LFE = 64000000000L;
  public static final long MAX_FILE_SIZE_AFS =2000000000L;
  public static final double EIGHT_GIG = 8000000000.0;  // approx 8.0 GB
  public static final double HUNDRED_GIG = 100000000000.0;  // approx 100.0 GB
  public static final int DEFAULT_NUM_TRIES =  5;
  public static final int BLOCK_SIZE = 512;
  public static final int BYTES_PER_TAR_RECORD = 10240;
  public static final double COMPACTION_DIR_SIZE = EIGHT_GIG;
//  public static final double COMPACTION_DIR_SIZE = HUNDRED_GIG;
  public static final int NUM_30SEC_TRIES = 3;
  public static final double MIN_BIN_PCT_ADDER = 1.20;
  public static final String SMTP_SERVER ="mailrelay.btv.ibm.com";
  
  // Operating Systems
  public static final String WINDOWS = "WINDOWS";

  // Application Modes
  public static final String DEV = "DEV";
  public static final String TEST = "TEST";
  public static final String PROD = "PROD";
  public static final String ICCPROD = "ICCPROD";
  public static final String JUNIT = "JUNIT";
  public static final String ICCTEST = "ICCTEST";
  
  // System Properties constants
  public static final String USER_NAME_PROPERTY_TAG = "user.name";
  public static final String USER_HOME_PROPERTY_TAG = "user.home";
  

  // Database access information
  public static final String DB_ACCESS_ID = "frodo";
  public static final String DB_ACCESS_CODE = "best4you";
  
  // Default values
  public static final short EMPTY = -1;
  public static final String COLON = ":";
  public static final String SEMI_COLON = ";";
  public static final String COMMA = ",";
  public static final String DASH = "-";
  public static final String BLANK = " ";
  public static final String UNDERSCORE = "_";
  public static final String POUND = "#";
  public static final String PERIOD = ".";
  public static final String AT_SIGN = "@";
  public static final String PERCENT = "%";
  public static final Date NULL_DATE = new Date(0);
  public static final String DB_TRUE = "Y";
  public static final String DB_FALSE = "N";
  public static final String RESTRICTED = "R";
  public static final String GENERALLY_AVAILABLE = "GA";
  public static final String YES = "yes";
  public static final String NO = "no";
  public static final String NONE = "none";
  public static final int AFS_USERID_LENGTH = 8;
  public static final String WRITING = "writing";
  public static final String NO_TECH = "noTech";
  public static final String NO_VERSION = "noVersion";
  public static final String ALL_TECH_RELEASES = "ALL";
  public static final String REVISION_ZERO = "r00000";
  public static final String REVISION_MAX = "r99999";

  // DIP version consumer type constants.
  public static final String CONSUMER_TYPE_ALL = "all";
  public static final String CONSUMER_TYPE_INTERNAL = "internal";
  public static final String CONSUMER_TYPE_DEV = "dev";
  
  // Lock constants
  public static final String LOCK = "Lock";
  public static final String NOT_LOCKED = "Not locked";
  public static final String UNMATCHED_LOCK = "Lock does not match";
  public static final String REMOVED_LOCK = "Removed lock";
  public static final String COULD_NOT_GET_LOCK = "Could not obtain lock";
  public static final long CHECK_INTERVAL = 5000; // 5000 millisecs = 5 seconds
  public static final long NUM_LOCK_CHECKS = 25; // approx 2 minutes (5 second * 25)

  // Logging constants
  public static final String SECTION_DIVIDER =
      "------------------------------------------------------------------------";
  public static final String RETURN_CODE_TAG = "Return code";
  public static final String DESCRIPTION = "DESCRIPTION";

  // Results from database actions.
  public static final String DELETABLE = "deletable";
  public static final String MARKABLE = "markable";
  public static final String MUST_REMAIN = "must remain";
  public static final String ADDED = "added";
  public static final String REINSTATED = "reinstated";
  public static final String IS_ACTIVE = "is active";
  public static final String PREVIOUSLY_ORDERED = "previously ordered";
  public static final String REMOVED = "removed";

  // Roles
  public static final String CORE_PROGRAM_MANAGER = "Core Program Manager";
  public static final String CUSTOMER_INTERFACE = "Customer Interface";
  public static final String CUST_INTERFACE_ADMIN = "Customer Interface Admin";
  public static final String ICOF_ADMIN = "Icof Admin";
  public static final String ORDER_CREATOR = "Order Creator";
  public static final String ORDER_MANAGER = "Order Manager";
  public static final String PROGRAM_MANAGER = "Program Manager";
  public static final String RELEASE_MANAGER = "Release Manager";
  public static final String SHIPPER = "Shipper";
  public static final String SERVER_ADMIN = "Server Admin";
  public static final String GUEST_ACOS = "Acos Guest";
  public static final String GUEST_EDITORS = "Editors Guest";
  public static final String GUEST_DELTA_LOG = "Delta Log Guest";

  // Acos Request ID generation constants
  public static final String GROWABLE = "G";
  public static final int ORDER_NUM_LENGTH = 3;
  public static final String ORDER_NUM_PAD_CHAR = "0";

  // Acos SubOrder Expected Completion Date constants
  public static final int FOURTEEN_DAYS = 14;

  // Acos Order States
  public static final String DRAFT = "Draft";
  public static final String AWAITING_APPROVAL = "Awaiting Approval";
  public static final String AWAITING_COMPLETION = "Awaiting Completion";
  public static final String IN_PROCESS = "In Process";
  public static final String COMPLETED = "Completed";
  public static final String DENIED = "Denied";
  public static final String CANCELLED = "Cancelled";
  public static final String PERS_FILE_CHECKING = "Pers File Checking";

  // Acos Rule Types
  public static final String ROM = "rom";
  public static final String LLF = "llf";
  public static final String DEFAULT_RULE_TYPE = "default";

  // Base Ord Prefixes (for IIPMDS tech releases)
  public static final String BMK_PREFIX = "BMK_";
  public static final String PK_PREFIX = "PK_";
  public static final String CUST_PREFIX = "CG_";
  public static final String BO_PREFIX = "BaseOrd_";

  // Customer Types
  public static final String REGULAR_CUSTOMER = "REGULAR";
  public static final String TOOL_VENDOR_CUSTOMER = "TOOL_VENDOR";
  public static final String FOUNDRY_CUSTOMER = "FOUNDRY";
  
  // Delta States
  public static final String DELTA_DONE = "DONE";
  public static final String DELTA_PENDING = "PENDING";

  // Tech Lib States
  public static final String TL_PRELIMINARY = "Preliminary";
  public static final String TL_ACTIVE = "Active";
  public static final String TL_INACTIVE = "Inactive";

  // Lib Rel States
  public static final String LR_PRELIMINARY = "Preliminary";
  public static final String LR_ACTIVE = "Active";
  public static final String LR_INACTIVE = "Inactive";

  // Lib Group Types
  public static final String BASE = "base";
  public static final String CORE = "core";
  public static final String CUSTOMER = "customer";
  public static final String BASE_ORD = "base_ord";
  public static final String BASE_FOUNDRY = "base_foundry";
  public static final String BASE_VW = "base_vw";
  public static final String CORE_VW = "core_vw";
  public static final String CUSTOMER_VW = "customer_vw";
  public static final String BASE_ORD_VW = "base_ord_vw";
  public static final String LGT_FOUNDRY_CUSTOMER = "foundry_customer";
  public static final String LGT_FOUNDRY_CUSTOMER_VW = "foundry_customer_vw";
  public static final String LGT_FOUNDRY_SHARE = "foundry_share";
  public static final String LGT_FOUNDRY_SHARE_VW = "foundry_share_vw";

  // Packet Types
  public static final String DEL = "DEL";
  public static final String BOTH = "BOTH";
  public static final String SRC = "SRC";
  
  // Model Type Constants
  public static final String ALL_MDL_TYPES = "ALL";
  
  // ShipList Types
  public static final String SL_MASTER = "master";
  public static final String SL_DELIVERY = "delivery";

  // Tool Types
  public static final String STANDARD = "Standard";
  public static final String NON_STANDARD_R = "Non-standard-R";
  public static final String NON_STANDARD_GA = "Non-standard-GA";
  public static final String PREVIEW_KIT = "Preview Kit";
  public static final String PLATFORM_GA = "Platform-GA";
  public static final String PLATFORM_R = "Platform-R";
  public static final String FOUNDRY_STANDARD = "Foundry-Standard";
  public static final String FOUNDRY_PREVIEW_KIT = "Foundry-Preview-Kit";

  // IP Category Types
  public static final String IP_BASE = "base";
  public static final String IP_BASE_ORD = "baseOrd";
  public static final String IP_CORE = "core";

  // ICC Project Name constants
  public static final String BLANK_LOCATION = "BLANK";

  // Product Definition constants
  public static final String FIRST_PD_REVISION_ID = "r00000";
  public static final int NUM_REVISION_DIGITS = 5;

  // Product Definition Revision states
  public static final String PDR_IN_PROCESS = "In-Process";
  public static final String PDR_COMPLETED = "Completed";
  public static final String PDR_FAILED = "Failed";


  // Directory and File constants
  public static final String AE_NAMES_DIRECTORY = "AE.names.directory";
  public static final String ALL_RELEASES_DATA = "all.releases.data";
  public static final String COMMON_TAR = "common.tar";
  public static final String CUSTOMER_LIST = "customer.list";
  public static final String DAVEND_NAMES_DIRECTORY = "DAVend.names.directory";
  public static final String DB_NAMES = "db.names";
  public static final String DELTA_PACKET_LIST = "delta_packet_list";
  public static final String DELTA_RELEASES_README = "DeltaReleases.html";
  public static final String DELTA_RELEASES_LIST = "delta_releases_list";
  public static final String DELTA_SUBGROUP_MATRIX = "delta.sys.subgroup.matrix";
  public static final String DELTA_SUPPORT_MATRIX = "delta.sys.support.matrix";
  public static final String DESIGN_KIT_README = "DesignKit.html";
  public static final String GDK_TAR = "gdk.tar";
  public static final String IIPMDS_DIP_LG_DATA = "iipmds_dip_lg_data";
  public static final String IIPMDS_IP_DEF_DATA = "iipmds_ipDef_data";
  public static final String IIPMDS_CUSTOMER_IP_ENTITLEMENTS = "iipmds_customer_ip_entitlements";
  public static final String IIPMDS_RELEASE_IP_REVISIONS = "released.ip.revisions";
  public static final String IIPMDS_RETRCTED_IP_REVISIONS = "retracted.ip.revisions";
  public static final String IGNORE_FOUNDRY_MT_LG = "ignoreFoundry.mt.lg";
  public static final String INSTALL_SCRIPT = "install_dk";
  public static final String LAST_RELEASE_DATA = "last.release.data";
  public static final String LAST_REVBUILT_DATA = "last.revbuilt.data.";
  public static final String LATEST_RELEASE_DATA = "latest.release.data";
  public static final String LIB_GROUP_OVERRIDES = "libGroup.overrides";
  public static final String MASTER_PACKET_LIST = "master_packet_list";
  public static final String FOUNDRY_BASE_ORDERABLE = "foundry.base_orderable.data";
  public static final String FOUNDRY_CORES = "foundry.cores.data";
  public static final String PACKET_ENTRY = "packet.entry";
  public static final String PATCH = "Patch";
  public static final String RETRACT = "retract";
  public static final String PATCH_REVISION_ID = "patch.revisionID";
  public static final String PREVIEW_KIT_README = "PreviewKit.html";
  public static final String RELEASE_ENTRY = "release.entry";
  public static final String RELEASE_HISTORY = "release.history";
  public static final String RELEASE_DIPS_DATA = "release.dips.data";
  public static final String RETRACTED_REL_NOTE_TEMPLATE = "retracted_relnote.template";
  public static final String REVISION_HISTORY = "revision.history";
  public static final String README = "README";
  public static final String SHIPPING_CUSTOMERS = "shipping.customers.data";
  public static final String SHIPPING_BASE_ORDERABLE = "shipping.base_orderable.data";
  public static final String SHIPPING_CORES = "shipping.cores.data";
  public static final String SHIPPING_TOOLS = "shipping.tools.data";
  public static final String SHIPPING_COMPONENTS = "shipping.orderable.components";
  public static final String SHIPPING_LIB_GROUPS = "shipping.libgroups.data";
  public static final String SOFTWARE_CONTACTS = "software.contacts"; //added
  public static final String TECH_RELEASE_DATA = "tech.release.data";
  public static final String TECH_ACOS_DATA = "tech.acos.data";
  public static final String TOOL_KIT_README = "tkinst.html";
  public static final String TOOLS_MAP = "tools.map";
  public static final String TPK_TAR = "tpk.tar";
  public static final String TPK_ORDER_NUMBER = "TPK"; //added
  public static final String USED_BY = "used_by";
  public static final String ADMIN = "/admin";
  public static final String AEINFO = "/aeinfo";
  public static final String AES = "/aes";
  public static final String SAMBA_DRIVE_LETTER = "X:";
  public static final String SAMBA_AFS_BTV_LINK_NAME = "AFS_BTV_DATA";
  public static final String SAMBA_BTV = SAMBA_DRIVE_LETTER + SAMBA_AFS_BTV_LINK_NAME;
  public static final String AFS_BTV_DATA = "/afs/btv/data";
  public static final String JUNIT_DATA_PATH = AES + "/apps/test/iipmds/junitData";
  public static final String AIM = "/aim";
  public static final String AIMTOOLS = "/aimtools";
  public static final String ASICDELTA = "/asicdelta";
  public static final String ASICPATCH2 = "/asicpatch2";
  public static final String ASICS = "/asics";
  public static final String ASICSHIP = "/asicship";
  public static final String ASICTEST_CONFIG = "/asictestConfig";
  public static final String BIN = "/bin";
  public static final String BUILD = "/build";
  public static final String CHECKSUMS = "/checksums";
  public static final String COMMON = "/common";
  public static final String COMPACTED_DIR = "/compacted";
  public static final String COMPACTION_DIR = "/compaction";
  public static final String DELIVERABLES = "/deliverables";
  public static final String DIPPREP = "/dipPrep";
  public static final String DK = "/dk";
  public static final String DOC_DIR = "/doc";
  public static final String EDESIGN_DIR = "/edesign"; //moved
  public static final String EDGE_DIR = "/edge";
  public static final String EMAILS  = "/emails";
  public static final String FINGERPRINTS = "/fingerprints";
  public static final String FULL_CONTENT = "/fullContent";
  public static final String DIP_CONTENT = "/dipContent";
  public static final String DIP_OBSOLETE = "/dipObsolete";
  public static final String GLOBAL = "/global";
  public static final String GDK = "/gdk";
  public static final String ICOF = "/icof";
  public static final String IIPMDS = "/iipmds";
  public static final String IPACKETS = "/ipackets";
  public static final String LOG = "/log";
  public static final String OBSOLETE = "/obsolete";
  public static final String OBSOLETE_BL = "/obsoleteBL";
  public static final String PACKET = "/packet";
  public static final String PACKET_MAPS = "/packetMaps";
  public static final String POCKET = "/pocket";
  public static final String PROCESSING_DIR = "/Processing";
  public static final String PRODUCT_DEFN = "/productDefn";
  public static final String RELEASE = "/release";
  public static final String RELEASED = "/released";
  public static final String RETRACTED = "/retracted";
  public static final String REL_NOTES = "/relnotes";
  public static final String REL_NOTE_MAP = "/relnoteMaps";
  public static final String REV_ALERT = "/revisionAlerts";
  public static final String REVISION_CUSTOMERS = "/revisionCustomers";
  public static final String SHARED_DATA = "/sharedData";
  public static final String SHIPLISTS = "/shipLists";
  public static final String SHIPPING_DIR = "/Shipping";
  public static final String STAGED = "/staged";
  public static final String SWADMIN = "/swAdmin";
  public static final String SYS_ADMIN = "/sys_admin";
  public static final String SYSLOG = "/syslog";
  public static final String TEMPLATES = "/templates";
  public static final String COMPACTION_WORK_DIR = "/tmp_compaction";
  public static final String TPK = "/tpk";
  public static final String TIMING_DATA_DIR = "/timingData";

  public static final String APPS = "/apps";
  public static final String AES_APPS = AES + APPS;
  
  public static final String ICC_GSA_DIR = "/web/server_root/datapersist/technologyconnect/sd"; /*added*/

  // Prod Directory Constants
  public static final String AES_PROD = AES + "/prod";
  public static final String PROD_AES_IIPMDS = AES + IIPMDS;
  public static final String PROD_AES_BIN = AES_APPS + "/prod" + IIPMDS + BIN;
  public static final String PROD_AES_SYS_ADMIN = AES_APPS + "/prod" + IIPMDS + SYS_ADMIN;
  public static final String TEST_AES_SYS_ADMIN = AES_APPS + "/test" + IIPMDS + SYS_ADMIN;
  public static final String PROD_AIM_ADMIN = AIM + ADMIN;
  public static final String PROD_AEINFO_BIN = AIM + AEINFO + BIN;
  public static final String PROD_AEINFO_TEMPLATES = AIM + AEINFO + TEMPLATES;
  public static final String PROD_AIMTOOLS_TEMPLATES = AIM + AIMTOOLS + TEMPLATES;
  public static final String PROD_AIMTOOLS_BIN = AIM + AIMTOOLS + BIN;
  public static final String PROD_ASICDELTA = ASICDELTA;
  public static final String PROD_ASICPATCH2 = ASICPATCH2;
  public static final String PROD_ASICPATCH2_COMPACTED = PROD_ASICPATCH2 + COMPACTED_DIR; //added
  public static final String PROD_ASICS = ASICS;
  public static final String PROD_ASICS_RELEASE = PROD_ASICS + RELEASE;
  public static final String PROD_ASICSHIP = ASICSHIP; //added
  public static final String PROD_AIM_ICOF =  AIM + ICOF;
  public static final String PROD_AIM_ICOF_DK =  PROD_AIM_ICOF + DK;
  public static final String PROD_AIM_ICOF_DK_GLOBAL =  PROD_AIM_ICOF_DK + GLOBAL;
  public static final String PROD_AIM_ICOF_LOG =  PROD_AIM_ICOF + LOG;
  public static final String PROD_ICOF_EDESIGN_SHARED_DATA_DIR = PROD_AIM_ICOF + EDESIGN_DIR + SHARED_DATA;//added
  public static final String PROD_AIM_EDESIGN_DIR = AIM + EDESIGN_DIR; //added
  public static final String PROD_AIM_EDESIGN_BUILD_DIR = PROD_AIM_EDESIGN_DIR + BUILD; //added
  public static final String PROD_TPK_OUTPUT_DIR = PROD_ASICS_RELEASE + "/tpk";

  // Dev Directory Constants
  public static final String AES_DEV = AES + "/dev";
  public static final String AIM_DEV = AIM + "/dev";
  public static final String DEV_AES_IIPMDS = AES_DEV + IIPMDS;
  public static final String DEV_AES_BIN = AES_APPS + "/test" + IIPMDS + BIN;
  public static final String DEV_AES_SYS_ADMIN = AES_APPS + "/test" + IIPMDS + SYS_ADMIN;
  public static final String DEV_AIM_ADMIN = AIM_DEV + ADMIN;
  public static final String DEV_AEINFO_BIN = AIM_DEV + AEINFO + BIN;
  public static final String DEV_AEINFO_TEMPLATES = AIM_DEV + AEINFO + TEMPLATES;
  public static final String DEV_AIMTOOLS_TEMPLATES = AIM_DEV + AIMTOOLS +  TEMPLATES;
  public static final String DEV_AIMTOOLS_BIN = AIM_DEV + AIMTOOLS +  BIN;
  public static final String DEV_ASICDELTA =  AIM_DEV + ASICDELTA;
  public static final String DEV_ASICPATCH2 =  AIM_DEV + ASICPATCH2;
  public static final String DEV_ASICPATCH2_COMPACTED = DEV_ASICPATCH2 + COMPACTED_DIR; //added
  public static final String DEV_ASICS =  AIM_DEV + ASICS;
  public static final String DEV_ASICS_RELEASE =  DEV_ASICS + RELEASE;
  public static final String DEV_ASICSHIP =  AIM_DEV + ASICSHIP;
  public static final String DEV_AIM_ICOF =  AIM_DEV + ICOF;
  public static final String DEV_AIM_ICOF_DK =  DEV_AIM_ICOF + DK;
  public static final String DEV_AIM_ICOF_DK_GLOBAL =  DEV_AIM_ICOF_DK + GLOBAL;
  public static final String DEV_AIM_ICOF_LOG =  DEV_AIM_ICOF + LOG;
  public static final String DEV_ICOF_EDESIGN_SHARED_DATA_DIR = DEV_AIM_ICOF + EDESIGN_DIR + SHARED_DATA;//added
  public static final String DEV_AIM_EDESIGN_DIR = AIM_DEV + EDESIGN_DIR; //added
  public static final String DEV_AIM_EDESIGN_BUILD_DIR = DEV_AIM_EDESIGN_DIR + BUILD; //added
  public static final String DEV_TPK_OUTPUT_DIR = DEV_ASICS_RELEASE + "/tpk";

  // Test Directory Constants (no current file system structure to support these)
  public static final String AES_TEST = AES + "/test";
  public static final String AIM_TEST = AIM + "/test";
  public static final String TEST_AES_IIPMDS = AES_TEST + IIPMDS;
  public static final String TEST_AES_BIN = AES_TEST + BIN;
  public static final String TEST_AIM_ADMIN = AIM_TEST + ADMIN;
  public static final String TEST_AEINFO_BIN = AIM_TEST + AEINFO + BIN;
  public static final String TEST_AEINFO_TEMPLATES = AIM_TEST + AEINFO + TEMPLATES;
  public static final String TEST_AIMTOOLS_TEMPLATES = AIM_TEST + AIMTOOLS +  TEMPLATES;
  public static final String TEST_AIMTOOLS_BIN = AIM_TEST + AIMTOOLS +  BIN;
  public static final String TEST_ASICDELTA =  AIM_TEST + ASICDELTA;
  public static final String TEST_ASICPATCH2 =  AIM_TEST + ASICPATCH2;
  public static final String TEST_ASICPATCH2_COMPACTED = TEST_ASICPATCH2 + COMPACTED_DIR; //added
  public static final String TEST_ASICS =  AIM_TEST + ASICS;
  public static final String TEST_ASICS_RELEASE =  TEST_ASICS + RELEASE;
  public static final String TEST_ASICSHIP =  AIM_TEST + ASICSHIP;
  public static final String TEST_AIM_ICOF =  AIM_TEST + ICOF;
  public static final String TEST_AIM_ICOF_DK =  TEST_AIM_ICOF + DK;
  public static final String TEST_AIM_ICOF_DK_GLOBAL =  TEST_AIM_ICOF_DK + GLOBAL;
  public static final String TEST_AIM_ICOF_LOG =  TEST_AIM_ICOF + LOG;
  public static final String TEST_ICOF_ESDESIGN_SHARED_DATA_DIR = TEST_AIM_ICOF + EDESIGN_DIR + SHARED_DATA; //added
  public static final String TEST_AIM_ESDESIGN_DIR = AIM_TEST + EDESIGN_DIR; //added
  public static final String TEST_AIM_EDESIGN_BUILD_DIR = TEST_AIM_ESDESIGN_DIR + BUILD; //added
  

  // ICCTEST Directory Constants
  public static final String AES_ICCTEST = AES + "/icctest";
  public static final String AIM_ICCTEST = AIM + "/icctest";
  public static final String ICCTEST_AES_IIPMDS = AES_ICCTEST + IIPMDS;
  public static final String ICCTEST_AES_BIN = AES_APPS + "/test" + IIPMDS + BIN;
  public static final String ICCTEST_AES_SYS_ADMIN = AES_APPS + "/icctest" + IIPMDS + SYS_ADMIN;
  public static final String ICCTEST_AIM_ADMIN = AIM_ICCTEST + ADMIN;
  public static final String ICCTEST_AEINFO_BIN = AIM_ICCTEST + AEINFO + BIN;
  public static final String ICCTEST_AEINFO_TEMPLATES = AIM_ICCTEST + AEINFO + TEMPLATES;
  public static final String ICCTEST_AIMTOOLS_TEMPLATES = AIM_ICCTEST + AIMTOOLS +  TEMPLATES;
  public static final String ICCTEST_AIMTOOLS_BIN = AIM_ICCTEST + AIMTOOLS +  BIN;
  public static final String ICCTEST_ASICDELTA =  AIM_ICCTEST + ASICDELTA;
  public static final String ICCTEST_ASICPATCH2 =  AIM_ICCTEST + ASICPATCH2;
  public static final String ICCTEST_ASICPATCH2_COMPACTED = ICCTEST_ASICPATCH2 + COMPACTED_DIR; //added
  public static final String ICCTEST_ASICS =  AIM_ICCTEST + ASICS;
  public static final String ICCTEST_ASICS_RELEASE =  ICCTEST_ASICS + RELEASE;
  public static final String ICCTEST_ASICSHIP =  AIM_ICCTEST + ASICSHIP;
  public static final String ICCTEST_AIM_ICOF =  AIM_ICCTEST + ICOF;
  public static final String ICCTEST_AIM_ICOF_DK =  ICCTEST_AIM_ICOF + DK;
  public static final String ICCTEST_AIM_ICOF_DK_GLOBAL =  ICCTEST_AIM_ICOF_DK + GLOBAL;
  public static final String ICCTEST_AIM_ICOF_LOG =  ICCTEST_AIM_ICOF + LOG;
  public static final String ICCTEST_ICOF_EDESIGN_SHARED_DATA_DIR = ICCTEST_AIM_ICOF + EDESIGN_DIR + SHARED_DATA;//added
  public static final String ICCTEST_AIM_EDESIGN_DIR = AIM_ICCTEST + EDESIGN_DIR; //added
  public static final String ICCTEST_AIM_EDESIGN_BUILD_DIR = ICCTEST_AIM_EDESIGN_DIR + BUILD; //added
  public static final String ICCTEST_TPK_OUTPUT_DIR = ICCTEST_ASICS_RELEASE + "/tpk";
  
  // File name extensions
  public static final String COMPACTED_EXTENSION = ".packets.compacted";
  public static final String SHIPLIST_EXTENSION = "shiplist";
  public static final String PDF_EXTENSION = "pdf";
  public static final String OVERRIDES_EXTENSION = ".overrides";
  public static final String FINGERPRINT_EXTENSION = "fingerprint";
  public static final String REL_NOTE_EXTENSION = "relnote";
  public static final String RETRACTED_EXTENSION = ".retracted";
  public static final String REL_NOTE_STAGED_EXTENSION = "staged";
  public static final String REL_NOTE_DESCRIPTION = "revision.description";
  public static final String REL_NOTE_NAME = "revision.notes";
  public static final String REV_ALERT_EXTENSION = "revision_alert";
  public static final String REV_NOTE_TEMPLATE = "DIP_Release_Note_Template.txt";
  public static final String PACKET_EXTENSION = ".packet";
  public static final String PACKETS_EXTENSION = ".packets";
  public static final String OBSOLETE_EXTENSION = ".obsolete";
  public static final String OBSOLETE_BL_EXTENSION = ".obsoleteBL";
  public static final String FULL_CONTENT_EXTENSION = ".content";
  public static final String FILTERED_EXTENSION = "filtered"; //added all below
  public static final String TOOL_MTS_EXTENSION = "toolMTs";
  public static final String BINS_EXTENSION = "bins";
  public static final String COMMON_DR = "common_dr";
  public static final String COMPACTION_STOP = "compaction.stop";
  public static final String COMPACTED_DELTA_PACKET_LIST = "compactedDeltaPacketList";
  public static final String CUMULATIVE_PACKETS = "cumulativePackets";
  public static final String INIT_RELEASE_PACKET_LIST = "init_release_packet_list";
  public static final String LOG_EXTENSION = "log";
  public static final String TAR_EXTENSION = "tar";
  public static final String TMP_EXTENSION = "tmp";
  public static final String GZ_EXTENSION = "gz";
  public static final String SIZE_EXTENSION = "size";
  public static final String TARLIST_EXTENSION = "tarList";
  public static final String PACKET_XREF_EXTENSION = "packetXRef";
  public static final String PACKET_HISTORY_EXTENSION = "packetHistory";
  public static final String COMPACTED = "compacted";
  public static final String XML_EXTENSION = "xml";
  public static final String ERR_EXTENSION = "err";
  public static final String COMMON_DR_TAR = COMMON_DR + "." +  TAR_EXTENSION;
  public static final String ON_SHELF = "onShelf"; 
  public static final String TIME_EXTENSION = ".time";
  public static final String LATEST_TEST_CONFIG = "latest_test.config";
  public static final String TEST_CONFIG = "test.config.";
  public static final String REL_NOTE_MAPPING = ".releasenote.mapping";
  
  // E-design constants
  public static final String ASICTECH_DIR = "/ASICTECH";
  public static final String GENERIC_DIR = "/generic";
  public static final String TECH_INFO_DIR = "/TechInfo";
  public static final String ORDER_LOG_DIR = "/OrderLog";
  public static final String STAGING_DIR = EDESIGN_DIR + "/edsd/staging";
//  public static final String DEV_STAGING_DIR = EDESIGN_DIR + "/edsd/staging.fix";
  public static final String DEV_STAGING_DIR = AES_DEV + "/iccEnvLink/active_env";
  public static final String PROD_EDESIGN_DIR = STAGING_DIR;
  public static final String DEV_EDESIGN_DIR = DEV_STAGING_DIR;
  public static final String PROD_E_TECH_DIR = PROD_EDESIGN_DIR + ASICTECH_DIR;
  public static final String DEV_E_TECH_DIR = DEV_EDESIGN_DIR + ASICTECH_DIR;
  public static final String PROD_E_INFO_DIR = PROD_EDESIGN_DIR + TECH_INFO_DIR;
  public static final String DEV_E_INFO_DIR = DEV_EDESIGN_DIR + TECH_INFO_DIR;
  public static final String PROD_E_ORDER_DIR = PROD_EDESIGN_DIR + ORDER_LOG_DIR;
  public static final String DEV_E_ORDER_DIR = DEV_EDESIGN_DIR + ORDER_LOG_DIR;
  public static final String PRODUCT_DEFN_DIR = "/ProductDefinition";
  public static final String DELTA_REL_DIR = "/DeltaReleases";
  public static final String PREVIEW_KIT_DIR = "/PreviewKit";
  public static final String MODEL_KIT_DIR = "/ModelKit";
  public static final String DK_RELNOTES_DIR = "/DKReleaseNote";
  public static final String PK_RELNOTES_DIR = "/PKReleaseNote";
  public static final String READMES_DIR = "/Readmes";
  public static final String EDESIGN_REMOVE_FILE_SCRIPT = "edrm_techf";
  public static final String EDESIGN_CHECKIN_TECHINFO_SCRIPT = "edci_techInfo";
  public static final String EDESIGN_CHECKIN_FILE_SCRIPT = "edci_techf";
  public static final String EDESIGN_STAGING_SCRIPT_DIR = PROD_EDESIGN_DIR + "/bin";
  public static final String EDESIGN_DEV_STAGING_SCRIPT_DIR = DEV_EDESIGN_DIR + "/bin";
  public static final String REV_ALERT_DIR = "/Revisions";
  public static final String ICC_BMK_TAR = "BaseModelKit.tar";
  public static final String ICC_TPK_TAR = "PreviewKit.tar";
  public static final String ICC_MODEL_KIT = "ModelKit";
  public static final String ICC_DELTA = "Delta"; //added
  public static final String DEFAULT_SHIP_TO_LABEL = "customer_ShipTo_01";//added
  public static final String MAJOR_REL_TYPE = "MAJOR"; //added
  public static final String DELTA_REL_TYPE = "DELTA";//added
  public static final String GENERIC_ORDER_NUMBER = "GENERIC"; //added
 
  // last.release.data constants
  public static final String NEW = "NEW";
  public static final String EDGE = "EDGE";
  public static final String NON_EDGE = "NON_EDGE";
  public static final String NON_ASIC = "NON_ASIC";
  public static final String ASIC = "ASIC";
  public static final String IIPMDS_IND = "IIPMDS";
  public static final String NON_IIPMDS_IND = "NON_IIPMDS";


  // tech.acos.data constants
  public static final String ACOS_ENABLED = "ACOS";
  public static final String NOT_ACOS_ENABLED = "NON_ACOS";


  // customer.list constants
  public static final String CL_ABBREV_5 = "5";
  public static final String CL_CMOS = "CMOS";
  public static final String CL_COMPANY = "Company";
  public static final String CL_DIVISION = "Location/DIV";
  public static final String CL_PROJECT = "Project";
  public static final String CL_CODE_NAME = "Code Name";
  public static final String CL_CUSTOMER_TYPE = "Customer Type";
  public static final String CL_TECH_LIST = "Tech_List";
  public static final String CL_NA = "N/A";


  // Acos exported order constants
  public static final String AEO_ACOS_SYSTEM = "ASIC Compilable Ordering System";
  public static final String AEO_REQUEST_ID = "Request_ID";
  public static final String AEO_REQUESTED_BY = "Requested_By";
  public static final String AEO_REQUESTER_EMAIL = "Requester_Email";
  public static final String AEO_STATUS = "Status";
  public static final String AEO_APPROVER = "Approver";
  public static final String AEO_MAIL_ADDRESS = "Mail_Address";
  public static final String AEO_REQUEST_DATE = "Request_Date";
  public static final String AEO_CUSTOMER_ID = "Customer_ID";
  public static final String AEO_DATE_NEEDED = "Date_Needed";
  public static final String AEO_PRODUCT_NAME = "Product_Name";
  public static final String AEO_RELEASE_TYPE = "Release_Type";
  public static final String AEO_DELTA = "Delta";
  public static final String AEO_MAJOR_LEVEL = "CMVC_Major_Level";
  public static final String AEO_DELTA_LEVEL = "CMVC_Delta_Level";
  public static final String AEO_NON_STD_DEL = "Non-Standard_Deliverables";
  public static final String AEO_INSTANCE_ADDITIONS = "Instance_Additions";
  public static final String AEO_INSTANCE_DELETIONS = "Instance_Deletions";
  public static final String AEO_PERSONALITY_FILE = "Personality File";
  public static final int AEO_LINE_LENGTH = 75;


  // delta.sys.subgroup.matrix constants
  public static final short LIB_GRP_TYPE_COLUMN = 52;


  // System call constants
  public static final String WRITE_ALL_RELEASES_DATA = "writeAllReleasesData";
  public static final String WRITE_DELTA_PACKET_LIST = "writeDeltaPacketList";  
  public static final String WRITE_TECH_RELEASE_DATA = "writeTechReleaseData";
  public static final String ADD_DELTA_2_DB = "addDelta2DB";
  public static final String REMOVE_DELTA_DB = "removeDeltaDB";
  public static final String SYNC_CUST_LIST = "syncCustList";
  public static final String SYNC_ORD_COMPONENTS = "syncOrdComponents";
  public static final String IGNORE_FOUNDRY_MT_LG_APP = "determineIgnoreFoundryMtLg";
  public static final String VERIFY_TECH_FILES = "verifyTechFiles";
  public static final String TECH_SWITCH = "-t ";
  public static final String VERSION_SWITCH = "-v ";
  public static final String DEV_SWITCH = "-d ";

  // ShipTo Label - an order file name segment
  public static final String SHIP_TO_LABEL = "_ShipTo_";
  
  // TDOF Line Types 
  public static final String TDOF_LINE_TYPE_HEAD_LN =  "HEAD";
  public static final String TDOF_LINE_TYPE_CUST_LN = "CUST";
  public static final String TDOF_LINE_TYPE_NSTD_LN = "NSTD";
  public static final String TDOF_LINE_TYPE_CORE_LN = "CORE";
  public static final String TDOF_LINE_TYPE_BASE_ORD_LN = "BASE_ORD";
  public static final String TDOF_LINE_TYPE_SHIP_LN = "SHIP";
  public static final String TDOF_LINE_TYPE_PRI_CI_LN = "PRIMARY_CI";
  public static final String TDOF_LINE_TYPE_SEC_CI_LN = "BACKUP_CI";
  public static final String TDOF_LINE_TYPE_COMMENTS_LN = "COMMENTS";
  public static final String TDOF_LINE_TYPE_BASE_KIT_LN = "BASE_MODEL_KIT";
  public static final String TDOF_LINE_TYPE_TOOL_KIT_ONLY_LN = "TOOL_KIT_ONLY";
  public static final String TDOF_LINE_TYPE_MODEL_TYPE_LN = "MODEL_TYPES";
  public static final String TDOF_LINE_TYPE_DELTA_LN = "DELTAS";
  public static final String TDOF_LINE_TYPE_PREV_NSTD_LN = "PREV_NSTD";
  public static final String TDOF_LINE_TYPE_PREV_CORE_LN = "PREV_CORE";
  public static final String TDOF_LINE_TYPE_PREV_BASE_ORD_LN = "PREV_BASE_ORD";
  public static final String TDOF_LINE_TYPE_MAJOR_ORDER_LN = "MAJOR_RELEASE_ORDER";
  public static final String TDOF_LINE_TYPE_CATCH_ME_UP_LN = "CATCH_ME_UP";
  public static final String TDOF_LINE_TYPE_TEST_LN = "TEST";
  public static final String TDOF_LINE_TYPE_PROFILE_ORDER_LN = "PROFILE_ORDER";

  // ShipTo Types
  public static final String SHIP_TO_CUSTOMER = "customer";
  public static final String SHIP_TO_FDC = "fdc";
  public static final String SHIP_TO_OTHER = "other";
  public static final String SHIP_TO_OTHER_1 = "other_1";
  public static final String SHIP_TO_OTHER_2 = "other_2";
  public static final String SHIP_TO_OTHER_3 = "other_3";
  
  //Other Order Constants
  public static final short CUSTOMER_SHIP_TO_LN = 1;

  // Model Kit Media Types
  public static final String MDL_KIT_MEDIA_TYPE_CD = "CD"; 
  public static final String MDL_KIT_MEDIA_TYPE_FILE = "FILE"; 
  public static final String MDL_KIT_MEDIA_TYPE_DOWNLOAD = "DOWNLOAD"; 
  public static final String MDL_KIT_MEDIA_TYPE_NONE = ""; 

  // File System Types
  public static final String AFS_FILE_SYSTEM = "afs";
  public static final String GSA_FILE_SYSTEM = "gsa";
  public static final String LOCAL_FILE_SYSTEM = "local";
  
  // Misc constants
  public static final String COMPACTED_PACKET_ABBREV = "cmp";
  
  // JUnit common constants.
  public static final String JUNIT_OBJECT_NAME = "JUnit_obj_name";
  public static final String JUNIT_OBJECT_DESCRIPTION = "JUnit_obj_desc";
  public static final String JUNIT_INVALID_NAME = "JUnit_invalid_name";
  public static final String JUNIT_INVALID_DESCRIPTION = "JUnit_invalid_desc";
  
  
  //-----------------------------------------------------------------------------
  // Data elements.
  //-----------------------------------------------------------------------------
  private static final String CLASS_NAME = "Constants";


}


//==========================  END OF FILE  ====================================
