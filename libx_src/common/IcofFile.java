/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2001 - 2011 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: IcofFile.java
*
* CREATOR: Karen K. Kellam
*    DEPT: 5ZIA
*    DATE: 12/10/2001
*
*-PURPOSE---------------------------------------------------------------------
* IcofFile class definition file.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 12/10/2001 KKK  Initial coding.
* 03/20/2002 KKK  Converted to Java 1.2.2.
* 01/17/2003 KKK  Added link function and archive function.
* 04/01/2005 GFS  Added methods - isDifferent, getChecksum and listRecursive.
* 05/05/2005 KKW  Added methods - tar, untar, compress, uncompress,
*                 tarResolveLinks, setPermissions
* 05/12/2005 KKW  Added methods - getFreeSpace(), getUsedSpace(), tarAppend(..)
* 07/28/2005 KKW  Finished adding javadoc to last few methods
* 08/05/2005 KKW  Added rsyncToIcc, rsyncFromIcc, rsyncWithIcc,
*                 rsyncStagedSrcFile methods.  Added appMode as a parameter
*                 to stageToIcc functions.  It is needed for the rsync
*                 calls that are inside stageToIcc.
* 10/18/2005 KKW  Updated stageToIcc functions so that they actually call
*                 a test version of edci_techf and edci_techInfo in DEV mode.
* 12/07/2005 KKW  Added getDirSize() & copyLink() function, as part of the
*                 rewrite of C++ delivery applications in Java.  Added copy
*                 function that allows one to pass in the parameters for the
*                 copy command.  Changed other copy functions to call this
*                 new one.
* 12/15/2005 KKW  Modified due to splitting of Constants.java into several
*                 *Util classes.  Moved rsync* and stage* methods to a
*                 new subClass, called IccFile.
* 12/20/2005 GFS  Added compareDirectories() method.
* 02/01/2007 GFS  Added link(String, boolean, String) method.
* 03/14/2007 KKW  Added osName and made methods operating system independent.
* 04/26/2007 KKW  Updated copyLink to use -H, insteand of -h for windows.
* 09/06/2007 KKW  For getDirSize, modified it to call one of the 
*                 execSystemCommands that has retry logic.
* 10/24/2007 GFS  Changed the setPermissions() method to not require the
*                 changeLinkedFiles boolean since the chmod -h option is not
*                 supported on linux and phasing out on AIX. Minor code clean up.
* 01/22/2008 GFS  Added getEmptyDirectories() method.
* 04/15/2008 KKW  Updated to use IcofException.SEVERE instead of 
*                 IcofException.SEVERE when throwing exceptions.      
* 05/14/2008 GFS  Updated the tar command to work the same on Linux as AIX.
* 05/23/2008 KKW  Updated link command, because when the link is in afs, 
*                 pointing to a file/dir in clearcase, the "exists()" method
*                 was not returning the correct answer.  This was causing
*                 the "force" boolean to be basically ignored.   
* 10/10/2008 GFS  Renamed method getFreeSpace to getFreeAfsSpace and 
*                 getUsedSpace to getUsedAfsSpace.   
* 05/13/2009 GFS  Added createCommonRepository() (See ASPRD620).
* 08/12/2009 KKW  Updated to use WINDOWS constant.
* 09/02/2009 KKW  Fixed BEAM errors.
* 04/07/2010 KKW  Added new attribute, fileSysName and getters/setters for it,
*                 as well as support for gsa file system in getQuotaInfo() method. 
*                 Changed name of getDirSize to getUsedBytes.  Added 
*                 isMountpoint(), getQuotaFree(), getQuotaSize(), getQuotaUsed(),
*                 getAfsQuotaInfo(), getGsaQuotaInfo(), getLocalQuotaInfo(),
*                 parseDfResults(), parseFsLqResults(), parseQuotaListResults().
*                 Changed getFreeAfsSpace() and getUsedAfsSpace() to call
*                 getQuotaFree() and getQuotaUsed(), respectively.
* 04/15/2010 KKW  Added resolvesToDirectory()
* 04/27/2010 KKW  Fixed logic error in isMountPoint() method
* 05/11/2010 KKW  Changed getFreeAfsSpace and getUsedAfsSpace so that they
*                 will work even if the directory is not a mount point. (They
*                 don't check if it is a mount point).  This is to fix a problem
*                 in PreBuiltModelKit.java. (MDCMS00088845)
* 05/13/2010 KKW  Added getOwner() method 
* 11/16/2010 KKW  Fixed java 1.5 collection warnings. Added tarWithOptions
*                 method and changed other tar methods to call it.
* 02/17/2011 KKW  Updated logic in isMountPoint method so that it doesn't
*                 throw an exception when a directory is not a mountpoint.
* 03/08/2011 KKW  Fixed some java doc
* 05/09/2011 KKW  Updated getCksum() method to work from Windows (if cygwin is
*                 installed)
* 05/20/2011 KKW  Added getTarContents() method.                                                       
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.common;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;


public class IcofFile extends File {

  // Constants for file/directory permissions
  public static final String READ_ONLY_ALL = "0444";
  public static final String READ_EXEC_ALL = "0555";
  public static final String READ_WRITE_ALL = "0666";
  public static final String READ_WRITE_EXEC_ALL = "0777";
  
  // Constants for mount point checking
  public static final String IS_MOUNT_POINT = "is a mount point for volume";
  public static final String IS_NOT_MOUNT_POINT = "is not a mount point";

  
  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from application using the
   *   full filename, which includes the full path.  The boolean indicates
   *   whether this object represents a directory or file (true = directory,
   *   false = file).
   *
   * @param     aCompleteFileName the complete name (including path) of the
   *                              file or directory to instantiate.
   * @param     aDirectoryInd     true, if aCompleteFileName represents a
   *                              directory name; false, if it represents a file.
   */
  //-----------------------------------------------------------------------------
  public IcofFile(String aCompleteFileName
                  ,boolean aDirectoryInd) {

    super(aCompleteFileName);
    initialize(aDirectoryInd);

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from application using the
   *   complete path name and the file name. The boolean indicates
   *   whether this object represents a directory or file (true = directory,
   *   false = file).
   *
   * @param     aPathName         the path name of the
   *                              file or directory to instantiate.
   * @param     aFileName         the name of a file (without a path) to
   *                              instantiate.
   * @param     aDirectoryInd     true, if aCompleteFileName represents a
   *                              directory name; false, if it represents a file.
   */
  //-----------------------------------------------------------------------------
  public IcofFile(String aPathName
                  ,String aFileName
                  ,boolean aDirectoryInd) {

    super(aPathName, aFileName);
    initialize(aDirectoryInd);

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from application using the
   *   full filename, which includes the full path.  The boolean indicates
   *   whether this object represents a directory or file (true = directory,
   *   false = file).  In this constructor, the fileContents are to be passed
   *   as a vector, making this constructor useful for writing files.
   *
   * @param     aCompleteFileName the complete name (including path) of the
   *                              file or directory to instantiate.
   * @param     aDirectoryInd     true, if aCompleteFileName represents a
   *                              directory name; false, if it represents a file.
   * @param     aContentsVector   a vector representing the contents of this
   *                              file or directory.
   */
  //-----------------------------------------------------------------------------
  public IcofFile(String aCompleteFileName
                  ,boolean aDirectoryInd
                  ,Vector<String> aContentsVector) {

    super(aCompleteFileName);
    initialize(aDirectoryInd);
    setContents(aContentsVector);

  }


  //-----------------------------------------------------------------------------
  // Member "getter" functions
  //-----------------------------------------------------------------------------
  /**
   * Get value of directoryName
   *
   */
  public String getDirectoryName() { return directoryName; }
  /**
   * Get value of fileName (does not include the path)
   *
   */
  public String getFileName() { return fileName; }
  /**
   * Get value of directory indicator
   *
   */
  public boolean getDirectoryInd() { return directoryInd; }
  /**
   * Get value of contents vector
   *
   */
  public Vector<String>  getContents() { return contents; }
  /**
   * Get the IcofStream associated with this file or directory
   *
   */
  protected IcofStream getIcofStream() { return icofStream; }
  /**
   * Get the os name
   * 
   */
  protected String getOsName() { return osName; }
  /**
   * Get the fileSystem name
   * 
   */
  protected String getFileSysName() { return fileSysName; }


  //-----------------------------------------------------------------------------
  // Included for readability.
  //-----------------------------------------------------------------------------
  /**
   * Get value of directory indicator -- included for readability
   *
   */
  public boolean representsDirectory() { return directoryInd; }
  /**
   * Get the windows os indicator
   * 
   */
  public boolean isWindowsOs() { return windowsOsInd; }
  /**
   * Get the Linux os indicator
   * 
   */
  public boolean isLinuxOs() { return linuxOsInd; }
  /**
   * Get the AIX os indicator
   * 
   */
  public boolean isAixOs() { return aixOsInd; }


  //-----------------------------------------------------------------------------
  /**
   * Get contents of IcofFile as a string
   *
   * @return                      Contents of this object as a String
   */
  //-----------------------------------------------------------------------------
  public String asString() {

    String directoryIndStr = new String("false");
    if (representsDirectory()) {
      directoryIndStr = "true";
    }

    return("Directory Name: "        + getDirectoryName() + ";" +
           "File name: "             + getFileName() + ";" +
           "Directory Ind: "         + directoryIndStr + ";" +
           "Windows OS: "            + windowsOsInd);

  }


  //-----------------------------------------------------------------------------
  /**
   * Add a line to the contents Vector.
   *
   * @param     aLine             represents a line of text in a file or
   *                              directory listing.
   *
   * @exception IcofException     Unable to add aLine to contents Vector
   */
  //-----------------------------------------------------------------------------
  public void addLine(String aLine) throws IcofException {

    getContents().add(aLine);

  }


  //-----------------------------------------------------------------------------
  /**
   * Add a line to the contents Vector.
   *
   * @param     aLine             represents a line of text in a file or
   *                              directory listing.
   * @param     forceToScreen     True, to write msg to screen
   * @exception IcofException     Unable to add aLine to contents Vector
   */
  //-----------------------------------------------------------------------------
  public void addLine(String aLine, boolean forceToScreen) throws IcofException {

    //TODO who added this and do we need it?  Didn't find it used anywhere.
    //  Add the line to the contents Vector
    getContents().add(aLine);

    // Write the line to the screen if requested
    if (forceToScreen) {
      System.out.println(aLine);
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Archive the source file to the specified target file name.  The
   *   source file will be copied to the target file name, with the current date
   *   and time appended to the target file name.  A link, using the specified
   *   target file name, will be created to the newly copied file, if the
   *   boolean parameter is true.
   *
   * @param     targetName        name of the archive file
   * @param     createLink        true, if a link to the archive file is to
   *                              be created; false, if not
   * @exception IcofException     Unable to create archive file and/or link
   */
  //-----------------------------------------------------------------------------
  public void archive(String targetName
                      ,boolean createLink) throws IcofException {

    String linkName = new String(targetName);

    Date now = new Date();
    targetName += "." +
                  IcofDateUtil.formatDate(now
                                          ,IcofDateUtil.ARCHIVE_FILE_DATE_FORMAT);

    IcofFile target = new IcofFile(targetName, false);

    // Copy the file to the archive location.  The true parameter indicates
    //   that the target file will be overwritten if it already exists.
    copy(target, true);

    // Create a link to the target file, if requested by the caller.  The true
    //   parameter indicates that the link will be removed and recreated if
    //   it already exists.
    if (createLink) {
      IcofFile linkFile = new IcofFile(linkName, false);
      target.link(linkFile, true);
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Close the file for appending.
   *
   * @exception IcofException     Unable to close this file which was open for
   *                              appending
   */
  //-----------------------------------------------------------------------------
  public void closeAppend() throws IcofException {

    getIcofStream().closeWrite();

  }


  //-----------------------------------------------------------------------------
  /**
   * Close the file for reading.
   *
   * @exception IcofException     Unable to close this file which was open for
   *                              reading
   */
  //-----------------------------------------------------------------------------
  public void closeRead() throws IcofException {

    getIcofStream().closeRead();

  }


  //-----------------------------------------------------------------------------
  /**
   * Close the file for writing.
   *
   * @exception IcofException     Unable to close this file which was open for
   *                              writing
   */
  //-----------------------------------------------------------------------------
  public void closeWrite() {

    getIcofStream().closeWrite();

  }

  
  //-----------------------------------------------------------------------------
  /**
   * Given a collection of IcofFile objects representing directories create a
   * common repository containing symbolic links to all contents of the source
   * directories. Remove any symbolic links in the common directory which point
   * to source files that no longer exist.
   *
   * @param sourceDirs  Collection of source directories (IcofFile objects)
   * @param ignoreDups  If true ignore duplicate source files otherwise throw an 
   *                    exception if the same file exists in more than 1 source
   *                    directory. 
   * 
   * @exception IcofException  Unable to create the common directory.
   */
  //-----------------------------------------------------------------------------
  public void createCommonRepository(Vector<IcofFile> sourceDirs, boolean ignoreDups)
  throws IcofException {

      String funcName = "createCommonRepository()";
      
      // Verify that this object is a directory and that it exists.
      if (! isDirectory()) {
          throw new IcofException(this.getClass().getName(), funcName,
                                  IcofException.SEVERE,
                                  "Common repository element is not a directory.",
                                  "Common repository: " + getPath());
      }
      validate(true);

      
      // Return if the source directory collection is empty.
      if ((sourceDirs == null) || (sourceDirs.size() < 1)) {
          return;
      }
      
      // Create a collection of source files in each input source directory.
      Hashtable<String, String> sourceContents = new Hashtable<String, String>();
      Iterator<IcofFile> iter = sourceDirs.iterator();
      while (iter.hasNext()) {
          IcofFile xSourceDir = iter.next();
          //System.out.println(" Src dir: " + xSourceDir.getPath());
          
          // If source directory is not a directory then throw an exception.
          if (! xSourceDir.isDirectory()) {
              throw new IcofException(this.getClass().getName(), funcName,
                                      IcofException.SEVERE,
                                      "Specified source directory is not a directory.",
                                      "Source dir: " + xSourceDir.getPath());
          }
          
          // Read contents of source directory and catalog results.
          Vector<String> contents = xSourceDir.listRecursive();
          //System.out.println("  -> found: " + contents.size() + " files");
          
          if (contents != null) {
              Iterator<String> iter2 = contents.iterator();
              while (iter2.hasNext()) {
                  String fullPath = iter2.next();
                  String element = fullPath.substring(xSourceDir.getAbsolutePath().length() + 1);

                  // If the element is already in the list then don't add it 
                  // again.  If ignoreDups is true then throw an exception.
                  if (sourceContents.containsKey(element)) {
                      
                      IcofFile xFullPath = new IcofFile(fullPath, false);
                      if ((! xFullPath.isDirectory()) && (! ignoreDups)) {
                          throw new IcofException(this.getClass().getName(), funcName,
                                                  IcofException.SEVERE,
                                                  "Element found in multiple " +
                                                  "source directories. ",
                                                  "Element: " + element);
                      }
                  }
                  else {
                      sourceContents.put(element, xSourceDir.getAbsolutePath());
                      
                  }
              }
          }
          
          //System.out.println(" Source content count: " + sourceContents.size());
          
      }

      
      // Remove symbolic links from the common repository which point to  
      // source files that no longer exist or that no longer point to the correct
      // source file.
      Vector<String> commonContents = this.listRecursive();
      Vector<IcofFile> commonDirs = new Vector<IcofFile>();
      
      if (commonContents != null) {
          Iterator<String> iter2 = commonContents.iterator();
          while (iter2.hasNext()) {
              String fullPath = iter2.next();
              String element = fullPath.substring(this.getAbsolutePath().length() + 1);

              IcofFile xLink = new IcofFile(fullPath, false);
              
              // Save any directories in a collection which will be processed 
              // later to remove any now empty directories.
              if (xLink.isDirectory()) {
                  if (! commonDirs.contains(xLink)) {
                      commonDirs.add(xLink);
                  }
              }
              else {
                  // If we have an element in the common directory that is not 
                  // in one of the source directories then it's a now dangling link.
                  if (! sourceContents.containsKey(element)) { 
                      // Note - dangling links appear as empty files and thus 
                      // can't be deleted with IcofFile.remove() method.
                      xLink.delete();
                  }
                  else {
                      // Does the link still point to the correct source file? 
                      // Source files may have moved from one source directory
                      // to a different source directory.
                      String sourceDir = (String) sourceContents.get(element);
                      IcofFile source = new IcofFile(sourceDir 
                                                     + IcofFile.separator + element,
                                                     false);
                      // Remove the link if it points to a different source file
                      // than is should.
                      if (! xLink.getAbsoluteFile().equals(source.getAbsoluteFile())) {
                          xLink.delete();
                      }

                  }
              }
          }
      }

      
      // Remove any empty directories from the common directory after removing 
      // the dangling or incorrect links.
      iter = commonDirs.iterator();
      while (iter.hasNext()) {
          IcofFile xCommonDir = (IcofFile) iter.next();
          //System.out.println("CALLING - removeEmptyDirs(" + xCommonDir.getPath() + ") ...");
          if (xCommonDir.exists()) {
              xCommonDir.removeEmptyDirectories(this);
          }
      }
      
      
      // Load the common directory with links to the source elements.
      Iterator<String> iter1 = sourceContents.keySet().iterator();
      while (iter1.hasNext()) {
          String element = iter1.next();

          // Create the path to the actual file in source dir. 
          String sourceDir = (String) sourceContents.get(element);
          String sourceFileName = sourceDir + File.separator + element;
          IcofFile sourceFile = new IcofFile(sourceFileName, true);
          
          // Create the link to source files only - ignore directories.
          if (! sourceFile.isDirectory()) {
          
              // Create the link name in the common repository.
              String newLinkName = getAbsolutePath() + File.separator + element;
              IcofFile newLink = new IcofFile(newLinkName, false);

              // Create/verify the link's parent directory.
              IcofFile parent = new IcofFile(newLink.getParent(), true);
              parent.validate(true);

              // Create the symbolic link.
              sourceFile.link(newLink, true);

          }
              
      }
      
  }
  

  //---------------------------------------------------------------------------
  /**
   * Compares the srcDir to the current directory and returns a Vector of
   * file/dir names which are in the srcDir and not in the current directory.
   *
   * @param     xSrcDir           Name of directory to compare against the
   *                              current directory.
   * @param     bIncludeSourceDir True if directory pointed to by srcDir should
   *                              also be found in the current directory. For
   *                              example, if srcDir is ~/test and this is true
   *                              then the test dir should be in the current
   *                              directory. If false then any file/dir under
   *                              ~/test should also be in current directory.
   * @param     bFineCompare      True if source files found in current
   *                              directory should be further matched by
   *                              checksums.
   *
   * @exception IcofException     Unable to compress the file
   */
  //---------------------------------------------------------------------------
  public Vector<String> compareDirectories(IcofFile xSrcDir
                                   ,boolean bIncludeSourceDir
                                   ,boolean bFineCompare) throws IcofException {

    String funcName = new String("compareDirectories(IcofFile, boolean)");

    // Ensure both objects are directories
    if ((! isDirectory()) || (! exists()))
        throw new IcofException(this.getClass().getName()
                                ,funcName
                                ,IcofException.SEVERE
                                ,"Can not compare directories if the current "
                                +" element does not exist or is not a "
                                +" directory.  "
                                ,"Current element = "
                                + getPath());

    if ((! xSrcDir.isDirectory()) || (! xSrcDir.exists()))
        throw new IcofException(this.getClass().getName()
                                ,funcName
                                ,IcofException.SEVERE
                                ,"Can not compare directories if the source "
                                +" element does not exist or is not a "
                                +" directory.  "
                                ,"Source element = "
                                + xSrcDir.getPath());


    // Get the contents of the source directories
    Vector<String> vcSourceDirContents = xSrcDir.listRecursive();


    // Compare the contents of the source directory agains the current
    // directory.  Any source file not found in the current directory is added
    // to the missing file vector.  If desired any source file found in the
    // current directory is further compared by verifying the checksum
    // against its current directory counterpart and if not the same is
    // added to the missing file vector.
    Vector<String> vcMissingFiles = new Vector<String>();
    String sSrcDirName = xSrcDir.getPath();
    int iIndex = sSrcDirName.lastIndexOf(separator);
    if (! bIncludeSourceDir)
        iIndex = sSrcDirName.length();

    for (int i = 0; i < vcSourceDirContents.size(); i++) {

        // Locate the source file element
        String sSrcFileName = (String) vcSourceDirContents.get(i);
        String sSrcFilePartialName
            = sSrcFileName.substring(iIndex);

        // Create the corresponding current directory file
        String sCurrentFileName = getPath() + sSrcFilePartialName;
        IcofFile xCurrentFile = new IcofFile(sCurrentFileName, false);

//         System.out.println("------------------------------------------");
//         System.out.println("Src Dir Name    = " + sSrcDirName);
//         System.out.println("Src File Name   = " + sSrcFileName);
//         System.out.println("Index           = " + iIndex);
//         System.out.println("Src File (part) = " + sSrcFilePartialName);
//         System.out.println("Current File    = " + sCurrentFileName);

        // Determine if current directory contains the matching source file
        if (! xCurrentFile.exists()) {
            vcMissingFiles.add(sSrcFileName + "(" + sCurrentFileName + ")");
            continue;
        }

        // Skip fine compare if the element is a directory
        if (xCurrentFile.isDirectory())
            continue;

        // Perform the fine compare
        if (bFineCompare) {

            IcofFile xSrcFile = new IcofFile(sSrcFileName, false);
            long lSrcChecksum = xSrcFile.getChecksum();

            long lCurrentChecksum = xCurrentFile.getChecksum();

            if (lSrcChecksum != lCurrentChecksum)
                vcMissingFiles.add(sSrcFileName + "(" + sCurrentFileName + ")");

        }

    }

    return vcMissingFiles;

  }


  //-----------------------------------------------------------------------------
  /**
   * Compress this file.  If this object is a directory, throw an exception.
   *   The resulting file will overlay the original file and will be named
   *   with the original file's name + ".gz".
   *
   * Because the underlying File class does not allow a file name to be
   *   changed, this method cannot update the file name (ie. cannot add
   *   the .gz extension).  It simply returns the name of the compressed
   *   file.
   *
   * @return                      the name of the compressed file
   * @exception IcofException     Unable to compress the file
   */
  //-----------------------------------------------------------------------------
  public String compress() throws IcofException {

    String funcName = new String("compress()");

    if (isDirectory()) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Attempting to compress a directory"
                                           ,getAbsolutePath());
      throw ie;
    }

    String compressedFileName = new String(getAbsolutePath() + ".gz");
    IcofFile compressedFile = new IcofFile(compressedFileName,false);
    if (compressedFile.exists()) {
      compressedFile.delete();
    }

    // **** HERE **** update for windows
    String command = new String("gzip " + getAbsolutePath());

    StringBuffer commandOutput = new StringBuffer();
    int rc = IcofSystemUtil.execSystemCommand(command, commandOutput);

    if (rc == 1) {
      IcofException ie = new IcofException(getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Compress of file "
                                           + getAbsolutePath()
                                           + " failed."
                                           ,command + "\n" + commandOutput.toString());
      throw ie;
    }
    else if (rc == 2) {
      IcofException ie = new IcofException(getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Compress of file "
                                           + getAbsolutePath()
                                           + " produced warning(s)."
                                           ,command + "\n" + commandOutput.toString());
      throw ie;
    }
    else if (rc != 0) {
      IcofException ie = new IcofException(getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Compress of file "
                                           + getAbsolutePath()
                                           + " produced errors(s)."
                                           ,command + "\n" + commandOutput.toString());
      throw ie;
    }

    return(compressedFileName);

  }


  //-----------------------------------------------------------------------------
  /**
   * Compress this file into the specified file.
   *   If this object is a directory, throw an exception.
   *
   * @param     tgtFile           represents the compressed file to be created
   *
   * @exception IcofException     Unable to compress the file
   */
  //-----------------------------------------------------------------------------
  public void compress(IcofFile tgtFile) throws IcofException {

    String funcName = new String("compress(IcofFile)");

    // Ensure the source file (represented by this object) is not a directory
    //   and that the target file represents a file, not a directory.  If
    //   either are directories, then throw an exception
    if ((isDirectory()) || (tgtFile.representsDirectory())) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Attempting to compress a directory "
                                           +" or attempting to compress a file into "
                                           +" a directory"
                                           ,"Source = "
                                           +getAbsolutePath()
                                           +"\nTarget = "
                                           +tgtFile.asString());
      throw ie;
    }

    if (tgtFile.exists()) {
      tgtFile.delete();
    }

    // **** HERE **** update for windows
    String command = new String("gzip -c "+ getAbsolutePath() + " > "
                                + tgtFile.getAbsolutePath());

    StringBuffer commandOutput = new StringBuffer();
    int rc = IcofSystemUtil.execSystemCommand(command, commandOutput);

    if (rc == 1) {
      IcofException ie = new IcofException(getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Compress of file "
                                           + getAbsolutePath()
                                           + " into "
                                           + tgtFile.getAbsolutePath()
                                           + " failed."
                                           ,command + "\n"
                                           + commandOutput.toString());
      throw ie;
    }

    if (rc == 2) {
      IcofException ie = new IcofException(getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Compress of file "
                                           + getAbsolutePath()
                                           + " into "
                                           + tgtFile.getAbsolutePath()
                                           + " produced warning(s)."
                                           ,command + "\n"
                                           + commandOutput.toString());
      throw ie;
    }

    if (rc != 0) {
      IcofException ie = new IcofException(getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Compress of file "
                                           + getAbsolutePath()
                                           + " into "
                                           + tgtFile.getAbsolutePath()
                                           + " produced errors(s)."
                                           ,command + "\n"
                                           + commandOutput.toString());
      throw ie;
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Copy this file or directory to the specified file or directory.  The
   *   boolean parameter determines what to do when the target already
   *   exists (if parm is false, throw exception, else overwrite target).
   *
   * @param     target            represents the file or directory to
   *                              copy this file or directory to
   * @param     force             true to overwrite the target file if it exists;
   *                              false, to throw exception if target exists
   * @exception IcofException     Unable to copy file or directory
   */
  //-----------------------------------------------------------------------------
  public void copy(IcofFile target, boolean force) throws IcofException {

    copy(target, force, "-R");

  }


  //-----------------------------------------------------------------------------
  /**
   * Copy this file or directory to the specified file or directory.  The
   *   boolean parameter determines what to do when the target already
   *   exists (if parm is false, throw exception, else overwrite target).
   *
   * @param     target            represents the file or directory to
   *                              copy this file or directory to
   * @param     force             true to overwrite the target file if it exists;
   *                              false, to throw exception if target exists
   * @param     parameters        a string containing the parameters for the
   *                              copy command
   * @exception IcofException     Unable to copy file or directory
   */
  //-----------------------------------------------------------------------------
  public void copy(IcofFile target, boolean force, String parameters)
      throws IcofException {

    String funcName = new String("copy(IcofFile, boolean, String");

    //
    // First, verify that the source file exists.  Throw an exception if it
    //   does not.
    //
    validate(false);
    
    // Next, verify that the source and target are not identical.  If they are, 
    //  just return.  There is nothing to copy and if we continue,
    //   the copy command will fail.
    if (getAbsolutePath().equals(target.getAbsolutePath())) {
        return;
    }
    

    //
    // Make sure that we are not attempting to copy a directory to a file.
    //
    if ((resolvesToDirectory()) && (!target.representsDirectory())) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Attempting to copy a directory to a file"
                                           ,getAbsolutePath());
      throw ie;
    }

    //
    // If copying a file to a directory, construct the complete target file name.
    //
    String fullPathName = new String("");
    if ((isFile()) && (target.representsDirectory())) {
      fullPathName = target.getDirectoryName() + separatorChar + getFileName();
    }
    else {
      fullPathName = target.getAbsolutePath();
    }

    // Construct the target object, using the fullPathName
    IcofFile actualTarget = new IcofFile(fullPathName, resolvesToDirectory());

    //
    // Check to see if the file/dir exists.  If so and we do NOT want to
    //   overwrite it, then throw an exception.  Otherwise, delete it.
    //
    if (actualTarget.exists()) {
      if (!force) {
        //
        //   Throw an exception if the file/dir exists.
        //
        IcofException ie = new IcofException(this.getClass().getName()
                                             ,funcName
                                             ,IcofException.SEVERE
                                             ,"Target file or dir already exists"
                                             ,actualTarget.getAbsolutePath());
        throw ie;
      }
      else {
        actualTarget.remove(true);
      }
    }
    
    //
    // Construct copy command.
    //
    // Ignore the parameters for windows os as the parameters for AIX copy are
    //   not supported on windows.
    String command = new String("cp " 
                                + parameters 
                                + " "
                                + getAbsolutePath()
                                + " "
                                + actualTarget.getAbsolutePath());


    //
    // Execute the system command.
    //
    StringBuffer errorMsg = new StringBuffer();
    int rc = IcofSystemUtil.execSystemCommand(command, errorMsg);

    if (rc != 0) {
      String msg = new String("Copy command, "
                              + command
                              + " failed.  RC = "
                              + rc
                              + "\n \n System message = "
                              + errorMsg.toString()
                              + "\n\n");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,command);
      throw ie;
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Copy this file to the specified target directory.  If rootDir is not null
   *   or empty then ensure any sub directories of this file after rootDir will
   *   be retained under the target directory.
   *   The force boolean parameter determines what to do when the target already
   *   exists (if parm is false, throw exception, else overwrite target).
   *   The preserve boolean indicates whether to preserve this file's tds
   *   data or not.
   *
   * @param     targetDir         represents the directory to copy this file to
   * @param     force             true to overwrite the target file if it exists;
   *                              false, to throw exception if target exists
   * @param     rootDir           the ?????
   * @param     preserve          true to preserve original file/dir timestamp
   *                              on the copied file
   * @exception IcofException     Unable to copy file or directory
   */
  //-----------------------------------------------------------------------------
  public void copy(IcofFile targetDir
                   ,boolean force
                   ,String rootDir
                   ,boolean preserve)
    throws IcofException {

    String funcName = new String("copy(IcofFile, boolean, String, boolean)");

    //
    // First, verify that the source file exists.  Throw an exception if it
    //   does not.
    //
    validate(false);


    //
    // Make sure that we are not attempting to copy a directory to a file.
    //
    if ((resolvesToDirectory()) && (! targetDir.representsDirectory())) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Attempting to copy a directory to a file"
                                           ,getAbsolutePath());
      throw ie;
    }


    //
    // Verify the target directory exists.  Throw an exception if it does not.
    //
    targetDir.validate(false);


    //
    // Determine this file's sub dirs under the rootDir by subtracting
    //   the rootDir from this file path
    //
    String subDir = null;
    if ((rootDir != null) && (! rootDir.equals(""))) {

      if (getDirectoryName().indexOf(rootDir) > -1) {
        subDir = new String();
        subDir = getDirectoryName().substring(rootDir.length() + 1);
      }
    }


    //
    // Verify the sub dirs exist in the targetDir or create them
    //
    String tgtSubDirName = targetDir.getDirectoryName();
    if (subDir != null)
      tgtSubDirName += separator + subDir;

    IcofFile tgtSubDir = new IcofFile(tgtSubDirName, true);
    tgtSubDir.validate(true);


    //
    // Create the tgtFile and copy this file to it
    //
    String tgtFileName = tgtSubDir.getDirectoryName() + separator + getName();
    IcofFile tgtFile = new IcofFile(tgtFileName, false);

    if (preserve)
      this.copyPreserve(tgtFile, force);
    else
      this.copy(tgtFile, force);

  }


  //-----------------------------------------------------------------------------
  /**
   * Copy this file or directory link to the specified file or directory,
   *   resolving the link, so that the result of the copy is an actual
   *   file or directory. The
   *   boolean parameter determines what to do when the target already
   *   exists (if parm is false, throw exception, else overwrite target).
   *
   * @param     target            represents the file or directory to
   *                              copy this file or directory to
   * @param     force             true to overwrite the target file if it exists;
   *                              false, to throw exception if target exists
   * @exception IcofException     Unable to copy file or directory link
   */
  //-----------------------------------------------------------------------------
  public void copyLink(IcofFile target, boolean force) throws IcofException {

    String parms = "-R ";
    if (isWindowsOs()) {
      parms += " -H";
    }
    else {
      parms += "-h";
    }
    copy(target, force, parms);

  }


  //-----------------------------------------------------------------------------
  /*
   * Copy this file or directory to the specified file or directory, preserving
   *   the timestamp and userid from the original file.  The
   *   boolean parameter determines what to do when the target already
   *   exists (if parm is false, throw exception, else overwrite target).
   *   
   * If this object is a link the -H options should ensure the actual contents
   *   of the file get copied to the target location on AIX and Linux clients.
   *
   * @param     target            the target file or directory
   * @param     force             true = overwrite the file if it already exists;
   *                              false =  do not overwrite the file it it exists
   *                              (throws an exception instead).
   * @exception IcofException     Unable to copy the file
   */
  //-----------------------------------------------------------------------------
  public void copyPreserve(IcofFile target, boolean force) throws IcofException {

    copy(target, force, "-R -p -H");

  }


  //-----------------------------------------------------------------------------
  /**
   * Create the file or directory represented by this object.
   *
   * @exception IcofException     Unable to create the file or directory
   */
  //-----------------------------------------------------------------------------
  public void create() throws IcofException {

    String funcName = new String("create()");

    if (exists()) {
      return;
    }

    //
    // If we get here, then the directory or file did not exist, so create it.
    //

    //
    // If this object represents a directory, create the directory and any
    //   missing parent directories.
    //
    if (representsDirectory()) {
      mkdirs();
      return;
    }

    // If it is a file, create an empty file.
    // **** HERE **** update for windows
    String command = new String("touch " + getAbsolutePath());
    StringBuffer errorMsg = new StringBuffer();
    int rc = IcofSystemUtil.execSystemCommand(command, errorMsg);
    if (rc != 0) {
      String msg = new String("Creation of file, "
                              + getAbsolutePath()
                              + " failed.  RC = "
                              + rc
                              + "\n \n System message = "
                              + errorMsg.toString()
                              + "\n\n");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,command);
      throw ie;
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Determine the checksum for this object
   *
   * @return                      the checksum
   * @exception IcofException     Unable to get the checksum
   */
  //-----------------------------------------------------------------------------
  public long getChecksum() throws IcofException {

    String funcName = new String("getChecksum()");

    //
    // Verify the file exists
    //
    this.validate(false);

    //
    // Create the checksum system call
    //
    String command = "/usr/bin/";
    if (isWindowsOs()) {
        command = "";
    }
    command += "cksum " + getAbsolutePath();

    //
    // Execute the system call with retry logic.
    //
    Vector<String> results = new Vector<String>();
    StringBuffer errorMsg = new StringBuffer();
    int rc = IcofSystemUtil.execSystemCommand(command, errorMsg, results, true);

    //
    // Throw an exception if the command failed to execute
    //
    if (rc != 0) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Unable to run cksum on file ("
                                           + getPath() + ")."
                                           ,"Errors = " + errorMsg.toString()
                                           + "\nResults = " + results.toString()
                                           + "\nRc = " + rc + ".");
      throw ie;
    }

    //
    // Read the checksum number
    //
    String checksum = new String("0");
    if (results.size() > 0) {
        String result = results.firstElement();
        checksum = IcofStringUtil.getField(result, 1, " ");
    }
    else {
        throw new IcofException(this.getClass().getName()
                        ,funcName
                        ,IcofException.SEVERE
                        ,"Unable to determine cksum on file ("
                        + getPath() + ")."
                        ,"No results returned from command.");
    }
    
    return Long.parseLong(checksum.trim());

  }

  
  //-----------------------------------------------------------------------------
  /**
   * Determines the OS specific tar parameter
   *
   * @return                      Additional tar parameter
   * @exception IcofException     Unable to determine the os
   */
  //-----------------------------------------------------------------------------
  private String getSystemTarParameter() throws IcofException {

    String tarSysParam = new String("");
    
    if (isLinuxOs()) {
        // Required to ensure the tarball is built the same as on AIX when it 
        // contains files that have more than 100 chars.
        tarSysParam = "--format=ustar ";
    }
    else if (isWindowsOs()) {
        tarSysParam = "--force-local ";
    }
    
    return tarSysParam;

  }

  
  //-----------------------------------------------------------------------------
  /**
   * Determine the approximate size of the directory (or file) represented by
   *   this object.  In the case of a directory, it is actually the space used
   *   by the files in the directory.
   *   Note that this function is written for a Unix-based platform and uses
   *   the "du" system command to get the size.  "du" returns an approximate
   *   size to the nearest 1K (due to the parameters used).
   *
   * @return                      the approximate size of the directory or file
   *                              in bytes (the result from "du" command * 1024)
   * @exception IcofException     Unable to determine the size
   */
  //-----------------------------------------------------------------------------
  public double getUsedBytes() throws IcofException {

    String funcName = new String("getUsedBytes()");

    //
    // Verify the directory or file exists
    //
    validate(false);

    //
    // Create the checksum system call
    //
    // **** HERE **** update for windows
    String command = "du -k -s " + getAbsolutePath();

    // Note that it is possible to have a successful return code and still
    //   get an error message from the du command.  It returns an error
    //   message if it cannot read a file or directory.  Therefore, if we
    //   get a non-numeric result, we will try executing the command again.
    boolean nonNumericResult = true;
    String dirSizeStr = new String("0");
    Vector<String> results = new Vector<String>();
    StringBuffer errorMsg = new StringBuffer();
    for (int i = 0; i < Constants.DEFAULT_NUM_TRIES && nonNumericResult; i++) {

      //
      // Execute the system call
      //
      int rc = IcofSystemUtil.execSystemCommand(command, errorMsg, results, true);

      //
      // Throw an exception if the command failed to execute
      //
      if (rc != 0) {
        IcofException ie = new IcofException(this.getClass().getName()
                                             ,funcName
                                             ,IcofException.SEVERE
                                             ,"Unable to determine size of ("
                                             + getAbsolutePath() + ")."
                                             ,"Errors = " + errorMsg.toString()
                                             + "\nResults = " + results.toString()
                                             + "\nRc = " + rc + ".");
        throw ie;
      }
      
      // If nothing was returned, throw an exception
      if (results.isEmpty()) {
        IcofException ie = new IcofException(this.getClass().getName()
                                             ,funcName
                                             ,IcofException.SEVERE
                                             ,"Command " + command +
                                             " returned nothing for ("
                                             + getAbsolutePath() + ")."
                                             ,"Errors = " + errorMsg.toString()
                                             + "\nRc = " + rc + ".");
          throw ie;
      }
      
      // Get the first word returned from the command -- this should be
      //   the size of the directory or file
      dirSizeStr = results.firstElement();
      dirSizeStr.trim();
      dirSizeStr = IcofStringUtil.word(dirSizeStr);
      if (IcofStringUtil.isDigits(dirSizeStr)) {
        nonNumericResult = false;
      }

    }

    if (nonNumericResult) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Non-numeric result for directory size, "
                                           +dirSizeStr
                                           +"\nCommand output = "
                                           +results.toString()
                                           ,"");
      throw(ie);
    }

    double dirSize = Double.parseDouble(dirSizeStr);
    dirSize = dirSize * Constants.ONE_K;

    return dirSize;

  }
 
  
  //-----------------------------------------------------------------------------
  /**
   * Removes all directories that are empty from the current directory to the
   * specified top directory.
   *
   * @param     xTopDir           IcofFile object of the top level directory
   *                              (if no top dir then use null).
   * @exception IcofException     Unable to read directories or contents.
   */
  //-----------------------------------------------------------------------------
  public void removeEmptyDirectories(IcofFile xTopDir)
  throws IcofException {

    String funcName = new String("removeEmptyDirectories()");
    IcofFile xParent = null;
    
    try {
    
        //System.out.println("In " + funcName + " ...");
        //System.out.println(" Top level dir: :" + xTopDir.getPath() + " ...");
        //System.out.println(" Testing: :" + getPath() + " ...");
        
    	// Verify this object exists and it is a directory.
    	if (isDirectory()) {
    		validate(false);
    	}
    	else {
    		throw new IcofException(getClass().getName(),
    				funcName, IcofException.SEVERE,
    				"Current directory is really not a directory.\n"
    				+ "Current directory: " + getAbsolutePath(), "");
    	}

    	
    	// Verify the top level directory is part of this directory's path.
    	if ((xTopDir != null) 
    	     && ! getAbsolutePath().startsWith(xTopDir.getAbsolutePath())) {
    		throw new IcofException(this.getClass().getName(),
    				funcName, IcofException.SEVERE,
    				"Current directory is not a child of the top directory.\n"
    				+ "Top directory: " + xTopDir.getAbsolutePath() 
    				+ "Current directory: " + getAbsolutePath(), "");
    	}

    	
    	// Verify the top directory is actually a directory.
    	if ((xTopDir != null) && ! xTopDir.isDirectory()) {
    		throw new IcofException(getClass().getName(),
    				funcName, IcofException.SEVERE,
    				"Top directory is really not a directory.\n"
    				+ "Top directory: " + xTopDir.getAbsolutePath(), "");
    	}

    	
    	// Return if we have reached the recursion stop conditions which are
    	//  1. this directory matches the top directory.
    	//  2. this directory is null (no more parents)
    	if (xTopDir != null) {
    		if (getAbsolutePath().equals(xTopDir.getAbsolutePath())) {
    			return;
    		}
    	}
    	else {
    		if (this == null) {
    			return;
    		}
    	}

    	// If this directory is empty then remove it and call this method on 
    	// its parent.  If it is not empty then we are done,
    	xParent = new IcofFile(getParent(), true);
    	if (isEmpty()) {
    		
    		// Remove the empty directory.
    		delete();
    		
    		// Check the parent directory
    		xParent.removeEmptyDirectories(xTopDir);
    		
    	}
    	
    } catch (Exception e) {
        throw new IcofException(this.getClass().getName(),
        		funcName, IcofException.SEVERE,
        		"Unable to read directory or its contents.\n",
        		e.getMessage());       	
    } finally {}

  }


  //-----------------------------------------------------------------------------
  /**
   * Determine the freespace (in bytes) for this directory
   *   Note that this function uses the "fs lq" command and will work only
   *   in an afs file system.
   *
   * @return                      the free space in bytes
   * @exception IcofException     Unable to determine the free space
   */
  //-----------------------------------------------------------------------------
  public long getFreeAfsSpace() throws IcofException {

      Vector<String> quotaInfo = getAfsQuotaInfo();

      //
      // The vector contains two entries. The first one is the size in bytes
      //   and the second one is the used bytes.
      //
      String totalSize = (String) quotaInfo.elementAt(0);
      String usedBytes = (String) quotaInfo.elementAt(1);
             
      return Long.parseLong(totalSize) - Long.parseLong(usedBytes);
  }


  //-----------------------------------------------------------------------------
  /**
   * Get the "quota" information for the mount point represented by this
   *   object.
   *
   * @return                      a Vector where the first entry is the 
   *                              size in bytes of the mount point and the second 
   *                              entry is the used bytes in the mount point
   * @exception IcofException     Unable to determine the quota info
   */
  //-----------------------------------------------------------------------------
  public Vector<String> getQuotaInfo() throws IcofException {

    String funcName = new String("getQuotaInfo()");

    //
    // Verify the directory is a mount point
    //
    if (!isMountpoint()) {
        IcofException ie = new IcofException(this.getClass().getName()
                                             ,funcName
                                             ,IcofException.SEVERE
                                             ,"Directory does not represent a " +
                                             		"mount point so quota info will " +
                                             		"not indicate size or used space " +
                                             		"of specified directory: "
                                             + getAbsolutePath()
                                             ,"");
        throw ie;

    }

    if (fileSysName.equals(Constants.AFS_FILE_SYSTEM)) {
        return getAfsQuotaInfo();
    }
    else if (fileSysName.equals(Constants.GSA_FILE_SYSTEM)) {
        return getGsaQuotaInfo();
    }
    else {
        return getLocalQuotaInfo();
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Get the "quota" information for an afs mountpoint using fs lq command
   *
   * @return                      a Vector where the first entry is the 
   *                              size in bytes of the mount point and the second 
   *                              entry is the used bytes in the mount point
   * @exception IcofException     Unable to determine the quota info
   */
  //-----------------------------------------------------------------------------
  protected Vector<String> getAfsQuotaInfo() throws IcofException {

    String funcName = new String("getAfsQuotaInfo()");

    //
    // Create the system call
    //
    String command = "/usr/bin/fs lq " + getAbsolutePath();

    //
    // Execute the system call
    //
    Vector<String> results = new Vector<String>();
    StringBuffer errorMsg = new StringBuffer();
    int rc = IcofSystemUtil.execSystemCommand(command, errorMsg, results);

    //
    // Throw an exception if the command failed to execute
    //
    if (rc != 0) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Unable to determine quota info for "
                                           + getAbsolutePath()
                                           ,errorMsg.toString());
      throw ie;
    }

    Vector<String> parsedResults = parseFsLqResults(results);
    return parsedResults;

  }


  //-----------------------------------------------------------------------------
  /**
   * Get the "quota" information for the gsa mount point using quota list command
   *
   * @return                      a Vector where the first entry is the 
   *                              size in bytes of the mount point and the second 
   *                              entry is the used bytes in the mount point
   * @exception IcofException     Unable to determine the quota info
   */
  //-----------------------------------------------------------------------------
  protected Vector<String> getGsaQuotaInfo() throws IcofException {

    String funcName = new String("getGsaQuotaInfo()");
    String command = "/usr/bin/gsa quota " + getAbsolutePath(); 

    //
    // Execute the system call
    //
    Vector<String> results = new Vector<String>();
    StringBuffer errorMsg = new StringBuffer();
    int rc = IcofSystemUtil.execSystemCommand(command, errorMsg, results);

    //
    // Throw an exception if the command failed to execute
    //
    if (rc != 0) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Unable to determine quota info for "
                                           + getAbsolutePath()
                                           ,errorMsg.toString());
      throw ie;
    }

    Vector<String> parsedResults = parseQuotaListResults(results);
    return parsedResults;

  }


  //-----------------------------------------------------------------------------
  /**
   * Get the "quota" information for the local mount point represented by this
   *   object.
   *
   * @return                      a Vector where the first entry is the 
   *                              size in bytes of the mount point and the second 
   *                              entry is the used bytes in the mount point
   * @exception IcofException     Unable to determine the quota info
   */
  //-----------------------------------------------------------------------------
  protected Vector<String> getLocalQuotaInfo() throws IcofException {

    String funcName = new String("getLocalQuotaInfo()");
    String command = "df -k ";
    if (isAixOs()) {
        command += "-I ";
    }
    command += getAbsolutePath();        

    //
    // Execute the system call
    //
    Vector<String> results = new Vector<String>();
    StringBuffer errorMsg = new StringBuffer();
    int rc = IcofSystemUtil.execSystemCommand(command, errorMsg, results);

    //
    // Throw an exception if the command failed to execute
    //
    if (rc != 0) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Unable to determine quota info for "
                                           + getAbsolutePath()
                                           ,errorMsg.toString());
      throw ie;
    }

    Vector<String> parsedResults = parseDfResults(results);
    return parsedResults;

  }


  //-----------------------------------------------------------------------------
  /**
   * Parse the results of the fs lq afs command. 
   *
   * @return                      a Vector where the first entry is the 
   *                              size in bytes of the mount point and the second 
   *                              entry is the used bytes in the mount point
   * @exception IcofException     Unable to parse the results
   */
  //-----------------------------------------------------------------------------
  protected Vector<String> parseFsLqResults(Vector<String> rawResults) throws IcofException {

    String funcName = new String("parseFsLqResults(Vector)");

    Vector<String> results = new Vector<String>();

    // Verify that the input vector contains the expected number of entries.
    if ((rawResults == null) || (rawResults.size() != 2)) {
        String errResults = "null";
        if (rawResults != null) {
            errResults = IcofCollectionsUtil.getVectorAsString(rawResults, "\n");
        }
        IcofException ie = new IcofException(this.getClass().getName()
                                             ,funcName
                                             ,IcofException.SEVERE
                                             ,"Unexpected input in Vector.  It " +
                                             		"does not seem to be the " +
                                             		"output of an fs lq command: "
                                             + errResults
                                             ,getAbsolutePath());
        throw ie;
        
    }
    
    //
    // Parse the results.  Ignore the first element (element(0)), as it contains
    //   header information
    //
    String result = rawResults.elementAt(1);
    result = IcofStringUtil.trimInterior(result, " ");
    String size = IcofStringUtil.getField(result, 2, " ");
    size = size.trim();
    String used = IcofStringUtil.getField(result, 3, " ");
    used = used.trim();

    long sizeBytes = Long.parseLong(size) * (long) Constants.ONE_K;
    long usedBytes = Long.parseLong(used) * (long) Constants.ONE_K;
    
    results.add(String.valueOf(sizeBytes));
    results.add(String.valueOf(usedBytes));
    return results;
  }


  //-----------------------------------------------------------------------------
  /**
   * Parse the results of the quota list gsa command. 
   *
   * @return                      a Vector where the first entry is the 
   *                              size in bytes of the mount point and the second 
   *                              entry is the used bytes in the mount point
   * @exception IcofException     Unable to parse the results
   */
  //-----------------------------------------------------------------------------
  protected Vector<String> parseQuotaListResults(Vector<String> rawResults) throws IcofException {

    String funcName = new String("parseQuotaListResults(Vector)");

    Vector<String> results = new Vector<String>();

    // Verify that the input vector contains the expected number of entries.
    if ((rawResults == null) || (rawResults.size() < 9)) {
        String errResults = "null";
        if (rawResults != null) {
            errResults = IcofCollectionsUtil.getVectorAsString(rawResults, "\n");
        }
        IcofException ie = new IcofException(this.getClass().getName()
                                             ,funcName
                                             ,IcofException.SEVERE
                                             ,"Unexpected input in Vector.  It " +
                                                    "does not seem to be the " +
                                                    "output of a quota list command: "
                                             + errResults
                                             ,getAbsolutePath());
        throw ie;
        
    }
    
    //
    // Parse the results.  
    //
    String sizeResult = rawResults.elementAt(6);
    sizeResult = IcofStringUtil.trimInterior(sizeResult, " ");
    String size = IcofStringUtil.getField(sizeResult, 2, ":");
    size = size.trim();
    String usedResult = rawResults.elementAt(7);
    usedResult = IcofStringUtil.trimInterior(usedResult, " ");
    String used = IcofStringUtil.getField(usedResult, 2, ":");
    used = used.trim();

    long sizeBytes = Long.parseLong(size) * (long) Constants.ONE_GB;
    long usedBytes = Long.parseLong(used) * (long) Constants.ONE_GB;
    
    results.add(String.valueOf(sizeBytes));
    results.add(String.valueOf(usedBytes));
    return results;
  }


  //-----------------------------------------------------------------------------
  /**
   * Parse the results of the df -k command. 
   *
   * @return                      a Vector where the first entry is the 
   *                              size in bytes of the mount point and the second 
   *                              entry is the used bytes in the mount point
   * @exception IcofException     Unable to parse the results
   */
  //-----------------------------------------------------------------------------
  protected Vector<String> parseDfResults(Vector<String> rawResults) throws IcofException {

    String funcName = new String("parseDfResults(Vector)");

    Vector<String> results = new Vector<String>();

    // Verify that the input vector contains the expected number of entries.
    if ((rawResults == null) || (rawResults.size() < 2)) {
        String errResults = "null";
        if (rawResults != null) {
            errResults = IcofCollectionsUtil.getVectorAsString(rawResults, "\n");
        }
        IcofException ie = new IcofException(this.getClass().getName()
                                             ,funcName
                                             ,IcofException.SEVERE
                                             ,"Unexpected input in Vector.  It " +
                                                    "does not seem to be the " +
                                                    "output of a df -k command: "
                                             + errResults
                                             ,getAbsolutePath());
        throw ie;
        
    }
    
    //
    // Parse the results.  Ignore the first element (element(0)), as it contains
    //   header information
    //
    String result = rawResults.elementAt(1);
    result = IcofStringUtil.trimInterior(result, " ");
    String size = IcofStringUtil.getField(result, 2, " ");
    size = size.trim();
    String used = IcofStringUtil.getField(result, 3, " ");
    used = used.trim();

    long sizeBytes = Long.parseLong(size) * (long) Constants.ONE_K;
    long usedBytes = Long.parseLong(used) * (long) Constants.ONE_K;
    
    results.add(String.valueOf(sizeBytes));
    results.add(String.valueOf(usedBytes));
    return results;
  }


  //-----------------------------------------------------------------------------
  /**
   * Get the "quota" (size) for this directory (Must be a mount point)
   *
   * @return                      the size in bytes of the directory
   * @exception IcofException     Unable to determine the quota info
   */
  //-----------------------------------------------------------------------------
  public long getQuotaSize() throws IcofException {

    Vector<String> quotaInfo = getQuotaInfo();

    //
    // The vector contains two entries. The first one is the size in bytes
    //   and the second one is the used bytes.
    //
    String sizeInBytes = quotaInfo.elementAt(0);
       
    return Long.parseLong(sizeInBytes);
  }


  //-----------------------------------------------------------------------------
  /**
   * Get the used quota for this directory (must be a mount point)
   *
   * @return                      the used bytes of the directory
   * @exception IcofException     Unable to determine the quota info
   */
  //-----------------------------------------------------------------------------
  public long getQuotaUsed() throws IcofException {

    Vector<String> quotaInfo = getQuotaInfo();

    //
    // The vector contains two entries. The first one is the size in bytes
    //   and the second one is the used bytes.
    //
    String usedBytes = quotaInfo.elementAt(1);
       
    return Long.parseLong(usedBytes);
  }


  //-----------------------------------------------------------------------------
  /**
   * Get the available quota for this directory (must be a mount point)
   *
   * @return                      the available bytes of the directory
   * @exception IcofException     Unable to determine the quota info
   */
  //-----------------------------------------------------------------------------
  public long getQuotaFree() throws IcofException {

    Vector<String> quotaInfo = getQuotaInfo();

    //
    // The vector contains two entries. The first one is the size in bytes
    //   and the second one is the used bytes.
    //
    String totalSize = quotaInfo.elementAt(0);
    String usedBytes = quotaInfo.elementAt(1);
           
    return Long.parseLong(totalSize) - Long.parseLong(usedBytes);
  }

  //-----------------------------------------------------------------------------
  /**
   * Determine the used space (in bytes) for this directory
   *   Note that this function uses the "fs lq" command and will work only
   *   in an afs file system.
   *
   * @return                      the used space in bytes
   * @exception IcofException     Unable to determine the used space
   */
  //-----------------------------------------------------------------------------
  public long getUsedAfsSpace() throws IcofException {


      Vector<String> quotaInfo = getAfsQuotaInfo();

      //
      // The vector contains two entries. The first one is the size in bytes
      //   and the second one is the used bytes.
      //
      String usedBytes = quotaInfo.elementAt(1);
             
      return Long.parseLong(usedBytes);
  }


  //-----------------------------------------------------------------------------
  /**
   * Get the userid that owns this file or directory.
   *
   * @exception IcofException     Unable to create the tar file
   */
  //-----------------------------------------------------------------------------
  public String getOwner() throws IcofException {

    String funcName = "getOwner()";

    // Construct the command
    String command = "stat -c %U " + getAbsolutePath();
    if (isAixOs()) {
        command = "istat " + getAbsolutePath() + " | grep Owner:";
    }
    StringBuffer errMsg = new StringBuffer();
    Vector<String> stdOutVector = new Vector<String>();

    // Create a tar file containing everything in the directory.
    int rc = IcofSystemUtil.execSystemCommand(command
                                              ,errMsg
                                              ,stdOutVector);

    if ((rc != 0) || (stdOutVector.isEmpty())) {

      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Command to get file ower failed.  Command = "
                                           +command
                                           ,errMsg.toString());
      throw(ie);
    }
    
    
    // Parse the results
    String owner = stdOutVector.firstElement();
    
    if (isAixOs()) {
        int lParen = owner.indexOf("(");
        int rParen = owner.indexOf(")");
        owner = owner.substring(lParen + 1, rParen);
    }
    
    return owner;

  }

  //-----------------------------------------------------------------------------
  /**
   * Determine whether or not this directory or file is different from the
   *   specified file or directory (aFile).  The files or directories are
   *   different if they have different checksum values.
   * @param     aFile             the file or directory to compare against
   *
   * @return                      true, if the files/directories are different;
   *                              false, if they have the same checksum
   * @exception IcofException     Unable to determine checksums
   */
  //-----------------------------------------------------------------------------
  public boolean isDifferent(IcofFile aFile) throws IcofException {

    //
    // Verify the files exist
    //
    if (! exists())
      return true;

    if (! aFile.exists())
      return true;


    //
    // Determine files' checksum values
    //
    long myChecksum = getChecksum();
    long yourChecksum = aFile.getChecksum();

    //
    // Return the result
    //
    if (myChecksum == yourChecksum)
      return false;

    return true;

  }


  //-----------------------------------------------------------------------------
  /**
   * Determine whether or not this IcofFile instance is really a directory.
   * @return                      true, if it is a directory;
   *                              false, if it is a file.
   * @exception IcofException     Unable to determine checksums
   */
  //-----------------------------------------------------------------------------
  public boolean resolvesToDirectory() throws IcofException {

      String funcName = "resolvesToDirectory()";
      boolean isDirectory = false;
      try {
          isDirectory = getCanonicalFile().isDirectory();
      }
      catch(IOException e) {
          IcofException ie = 
              new IcofException(getClass().getName(), 
                                funcName, IcofException.SEVERE, 
                                "Unable to determine if element, " + 
                                getAbsolutePath() + 
                                " is a directory.", 
                                "Message: " + e.getMessage());
          throw ie;
      }
      return isDirectory;
  }
  

  //-----------------------------------------------------------------------------
  /**
   * Determine whether or not this directory or file is a mount point 
   *
   * @return                      true, if the file/directory is a mount point;
   *                              false, if not
   * @exception IcofException     Unable to determine checksums
   */
  //-----------------------------------------------------------------------------
  public boolean isMountpoint() throws IcofException {

      //
      // Create the system call
      //
      String command = "";
      if (fileSysName.equals(Constants.AFS_FILE_SYSTEM)) {
          command = "/usr/bin/fs lsm " + getAbsolutePath();
      }
      else if (fileSysName.equals(Constants.GSA_FILE_SYSTEM)) {
//TODO
          command = "/usr/bin/gsa quota " + getAbsolutePath(); 
      }
      else {
//TODO
          command = "df -k " + getAbsolutePath();        
      }

      //
      // Execute the system call
      //
      Vector<String> results = new Vector<String>();
      StringBuffer errorMsg = new StringBuffer();
      int rc = IcofSystemUtil.execSystemCommand(command, errorMsg, results);
      
      String resultStr = "";
      if (!results.isEmpty()) {
          resultStr = results.firstElement();
          
          if (resultStr.indexOf(IS_MOUNT_POINT) >= 0) {
              return true;
          }
      }
      else {
          
          // Inspect the error string to see if it contains a message
          //   that the directory is not a mountpoint.
          if (errorMsg.toString().indexOf(IS_NOT_MOUNT_POINT) >= 0) {
              return false;
          }
          
          // If there were no results returned, then we don't know if it's a
          //   mountpoint or not.
          IcofException ie = new IcofException(this.getClass().getName()
                                               ,"isMountPoint()"
                                               ,IcofException.SEVERE
                                               ,"Unable to determine if directory " +
                                                      "is a mount point"
                                               + getAbsolutePath()
                                               ,errorMsg.toString());
          throw ie;
          
      }
      
      // Regardless of the return code, if the directory wasn't determined to
      //   be a mountpoint in the above if statement, check the command results
      //   to see if it is not a mount point.  If the command results don't 
      //   contain that phrase, then we got unexpected results.
      if ((resultStr.indexOf(IS_NOT_MOUNT_POINT) < 0)) {
          IcofException ie = new IcofException(this.getClass().getName()
                                               ,"isMountPoint()"
                                               ,IcofException.SEVERE
                                               ,"Unexpected results from command: " +
                                               command
                                               ,resultStr);
          throw ie;
      
      }
      
      return false;

  }


  //-----------------------------------------------------------------------------
  /**
   * Determine whether or not this directory or file is empty.
   *
   * @return                      true if the directory or file is empty; false,
   *                              if it is not.
   * @exception IcofException     File or directory does not exist
   */
  //-----------------------------------------------------------------------------
  public boolean isEmpty() throws IcofException {

    validate(false);

    //
    // If we get here, then the directory or file exists.  Check to see if
    //   it is empty.
    //
    boolean empty = false;
    if (isDirectory()) {
      String[] dirContents = list();
      if ((dirContents == null) || (dirContents.length == 0)) {
        empty = true;
      }
      return empty;
    }

    //
    // If we get here, then we are dealing with a file.  Check to see if
    //   it is empty.
    //
    if (length() == 0) {
      empty = true;
    }
    return empty;

  }


  //-----------------------------------------------------------------------------
  /**
   * Determine whether or not this file is already open.
   *
   * @return                      true, if the file is open; false, if not
   */
  //-----------------------------------------------------------------------------
  public boolean isOpen() {

    //
    // Check to see if the file has been opened for reading or writing.
    //
    return getIcofStream().isOpen();

  }


  //-----------------------------------------------------------------------------
  /**
   * Determine whether or not this file is already opened for appending.
   *
   * @return                      true, if the file is open in append mode;
   *                              false, if not.
   */
  //-----------------------------------------------------------------------------
  public boolean isOpenAppend() {

    //
    // Check to see if the file has been opened for writing/appending.
    //
    return getIcofStream().isOpenWrite();

  }


  //-----------------------------------------------------------------------------
  /**
   * Determine whether or not this file is already opened for reading.
   *
   * @return                      true, if the file is open in read mode;
   *                              false, if not.
   */
  //-----------------------------------------------------------------------------
  public boolean isOpenRead() {

    //
    // Check to see if the file has been opened for reading.
    //
    return getIcofStream().isOpenRead();

  }


  //-----------------------------------------------------------------------------
  /**
   * Determine whether or not this file is already opened for writing.
   *
   * @return                      true, if the file is open in write mode;
   *                              false, if not.
   */
  //-----------------------------------------------------------------------------
  public boolean isOpenWrite() {

    //
    // Check to see if the file has been opened for writing.
    //
    return getIcofStream().isOpenWrite();

  }


  //-----------------------------------------------------------------------------
  /**
   * Link this file or directory to the specified file or directory.  The
   *   boolean parameter determines what to do when the target already
   *   exists (if parm is false, throw exception, else overwrite target).
   *
   * @param     target            the name of the link to create
   * @param     force             true, to create link even if target already
   *                              exists; false if an exception should be
   *                              thrown if target already exists
   *
   * @exception IcofException     target exists or unable to create link
   */
  //-----------------------------------------------------------------------------
  public void link(IcofFile target, boolean force) throws IcofException {

    String funcName = new String("link(IcofFile, boolean)");

    //
    // First, verify that the source file exists.  Throw an exception if it
    //   does not.L536
    
    //
    validate(false);

    //
    // Make sure that we are not attempting to link a directory to a file.
    //
    if ((resolvesToDirectory()) && (!target.representsDirectory())) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Attempting to link a directory to a file"
                                           ,getAbsolutePath());
      throw ie;
    }

    //
    // If linking a file to a directory, construct the complete target file name.
    //
    String fullPathName = new String("");
    if ((isFile()) && (target.representsDirectory())) {
      fullPathName = target.getDirectoryName() + separatorChar + getFileName();
    }
    else {
      fullPathName = target.getAbsolutePath();
    }

    // Construct the target object, using the fullPathName
    IcofFile actualTarget = new IcofFile(fullPathName, resolvesToDirectory());

    // Construct the command.
    String command = "ln ";
    if (force) {
      command += "-f ";
    }
    command += "-s " + getAbsolutePath() + " " + actualTarget.getAbsolutePath();

    //
    // Execute the system command.
    //
    StringBuffer errorMsg = new StringBuffer();
    int rc = IcofSystemUtil.execSystemCommand(command, errorMsg);

    if (rc != 0) {
      String msg = new String("Link command, "
                              + command
                              + " failed.  RC = "
                              + rc
                              + "\n \n System message = "
                              + errorMsg.toString()
                              + "\n\n");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,command);
      throw ie;
    }

  }
  
  
 //-----------------------------------------------------------------------------
  /**
   * Create a symbolic link from this file or directory to the specified 
   *   target directory.  If rootDir is not null or empty then ensure any
   *   sub directories of this file after rootDir will be retained under
   *   the target directory. The boolean parameter determines what to do
   *   when the target link already exists (if parm is false, throw exception,
   *   else overwrite target).
   * 
   * Example 1.  This = $HOME/dir1/dir2/file.
   *   Invoking as link($HOME/DIR1, true, null)
   *     results in creating $HOME/DIR1/file as a sym link to orig file.
   *   Invoking as link($HOME/DIR1, true, dir2)
   *     results in creating $HOME/DIR1/dir2/file as a sym link to orig file.
   * Example 2.  This = $HOME/dir1/dir2/dir3.
   *   Invoking as link($HOME/DIR1, true, null)
   *     results in creating $HOME/DIR1/dir3 as a sym link to orig direcotry.
   *   Invoking as link($HOME/DIR1, true, dir2)
   *     results in creating $HOME/DIR1/dir2/dir3 as a sym link to orig dir.
   * 
   * @param     targetDir         location of target directory
   * @param     force             true, to create link even if target already
   *                              exists; false if an exception should be
   *                              thrown if target already exists
   * @param     rootDir           sub directory under source object to preserve
   *                              under targetDir or null/empty.
   *
   * @exception IcofException     target exists or unable to create link
   */
  //-----------------------------------------------------------------------------
  public void link(IcofFile targetDir, boolean force, String rootDir)
  throws IcofException {

    String funcName = new String("link(IcofFile, boolean, String)");

    //
    // Verify that the source dir/file exists.  Throw an exception if it
    //   does not.
    //
    validate(false);
 
    //System.out.println("Link - This  = " + getPath());
    //System.out.println("Link - Tgt   = " + targetDir.getPath());  
    //System.out.println("Link - Force = " + force);
    //System.out.println("Link - Root  = " + rootDir);
    
    //
    // Verify the target directory exists and it is a directory. Throw an
    // exception if not.
    //
    targetDir.validate(false);
    if (! targetDir.representsDirectory()) {
        IcofException ie = new IcofException(this.getClass().getName()
                                             ,funcName
                                             ,IcofException.SEVERE
                                             ,"Target object must be a directory"
                                             ,"Dir = " + targetDir.getPath());
        throw ie;   
    }

    //
    // Verify the targetDir + rootDir directory exists or create it.
    //
    String tgtSubDirName = targetDir.getDirectoryName();
    if (rootDir != null)
        tgtSubDirName += separator + rootDir;

    IcofFile tgtSubDir = new IcofFile(tgtSubDirName, true);
    tgtSubDir.validate(true);

    
    //
    // Construct the target name.
    //
    IcofFile actualTarget = new IcofFile(tgtSubDirName + separator + getName()
                                         ,false);
    
    link(actualTarget, force);

  }


  //-----------------------------------------------------------------------------
  /**
   * Recursively list the directory contents and return the full path of each
   *   file or directory in a vector.
   *
   * @return                      the contents of the directory and each
   *                              of its subdirectories (recursive)
   * @exception IcofException     Unable to obtain directory contents
   */
  //-----------------------------------------------------------------------------
  public Vector<String> listRecursive() throws IcofException {

    String funcName = new String("listRecursive()");

    //
    // Verify that this is a directory and that is exists
    //
    validate(false);

    if (! this.resolvesToDirectory()) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"This method only operates on "
                                           + "directories."
                                           ,getDirectoryName()
                                           + IcofFile.separator + getFileName());
      throw ie;
    }

    //
    // Read contents of the directory into a vector (ie. "ls").
    //
    String[] dirContents = list();


    //
    // If this directory contains sub-directories then recursively read them
    //
    Vector<String> allContents = new Vector<String>();
    for (int i = 0; i < dirContents.length; i++) {

      String element = dirContents[i];
      String dirOrFileName = getDirectoryName() + IcofFile.separator + element;
      IcofFile dirOrFile = new IcofFile(dirOrFileName, true);

      if (dirOrFile.resolvesToDirectory()) {

        Vector<String> myContents = dirOrFile.listRecursive();

        Iterator<String> iter = myContents.iterator();
        while (iter.hasNext()) {
          String subElement = iter.next();
          allContents.add(subElement);
        }

      }

      allContents.add(dirOrFileName);

    }

    return allContents;


  }


  //-----------------------------------------------------------------------------
  /**
   * Open the file for appending.  If this object is a directory,
   *   throw an exception.
   *
   * @exception IcofException     Attempting to open a directory for writing,
   *                              file is already open, or file could not be
   *                              opened.
   */
  //-----------------------------------------------------------------------------
  public void openAppend() throws IcofException {

    String funcName = new String("openAppend()");

    if (representsDirectory()) {
      String msg = new String("Cannot open a directory object for appending");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,getAbsolutePath());
      throw ie;
    }

    //
    // Make sure the file is not currently opened. If it is,
    //   throw an exception.
    //
    if (getIcofStream().isOpen()) {
      String msg = new String("File is already open.");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,getAbsolutePath());
      throw ie;
    }

    //
    // Open the file for appending, as indicated by the true boolean.
    //
    try {
      FileOutputStream fos = new FileOutputStream(getAbsolutePath(), true);
      setIcofStream(new IcofStream(fos));
    }
    catch (Exception e) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,e.toString()
                                           ,"");
      throw ie;
    }
    getIcofStream().openWrite();

  }


  //-----------------------------------------------------------------------------
  /**
   * Open the file for reading.  If this object is a directory,
   *   throw an exception.
   *
   * @exception IcofException     Attempting to open a directory for reading,
   *                              file is already open, or file could not be
   *                              opened.
   */
  //-----------------------------------------------------------------------------
  public void openRead() throws IcofException {

    String funcName = new String("openRead()");

    validate(false);

    if (representsDirectory()) {
      String msg = new String("Cannot open a directory object for line-by-line reading");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,getAbsolutePath());
      throw ie;
    }

    // Make sure the file is not currently opened. If it is,
    //   throw an exception.
    if (getIcofStream().isOpen()) {
      String msg = new String("File is already open.");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,getAbsolutePath());
      throw ie;
    }

    //
    // If we get here, then the file exists and is not open.  Open it for
    //   reading.
    //
    try {
      FileInputStream fis = new FileInputStream(this);
      setIcofStream(new IcofStream(fis));
    }
    catch (Exception e) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,e.toString()
                                           ,"");
      throw ie;
    }
    getIcofStream().openRead();

  }


  //-----------------------------------------------------------------------------
  /**
   * Open the file for writing.  If this object is a directory,
   *   throw an exception.
   *
   * @exception IcofException     Attempting to open a directory for writing,
   *                              file is already open, or file could not be
   *                              opened.
   */
  //-----------------------------------------------------------------------------
  public void openWrite() throws IcofException {

    String funcName = new String("openWrite()");

    if (representsDirectory()) {
      String msg = new String("Cannot open a directory object for writing");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,getAbsolutePath());
      throw ie;
    }

    //
    // Make sure the file is not currently opened. If it is,
    //   throw an exception.
    //
    if (getIcofStream().isOpen()) {
      String msg = new String("File is already open.");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,getAbsolutePath());
      throw ie;
    }

    //
    // Open the file for writing.
    //
    try {
      FileOutputStream fos = new FileOutputStream(this);
      setIcofStream(new IcofStream(fos));
    }
    catch (Exception e) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,e.toString()
                                           ,"");
      throw ie;
    }
    getIcofStream().openWrite();

  }


  //-----------------------------------------------------------------------------
  /*
   * Read the file/dir into a vector.  If this object is a directory,
   *   read the contents of the directory (ie. "ls") into the FileContents
   *   vector that is a member of this class.
   *
   * @exception IcofException     File/directory does not exist or cannot be
   *                              read
   */
  //-----------------------------------------------------------------------------
  public void read() throws IcofException {

    String funcName = new String("read()");

    validate(false);

    //
    // If we get here, then the directory or file exists.  If it is a directory
    //   read the contents of the directory into a vector (ie. "ls").
    //
    if (resolvesToDirectory()) {
      String[] dirContents = list();
      setContents(IcofCollectionsUtil.getStringArrayAsVector(dirContents));
      return;
    }

    //
    // If we get here, then we are dealing with a file.  Create the
    //   file input stream.
    //
    try {
      FileInputStream fis = new FileInputStream(this);
      setIcofStream(new IcofStream(fis));
    }
    catch (Exception e) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,e.toString()
                                           ,"");
      throw ie;
    }
    setContents(getIcofStream().read());

  }


  //-----------------------------------------------------------------------------
  /**
   * Read a single line of the file.  If end-of-file is reached, close the
   *   file.  Return the line read.
   *
   * @return                      the line of the file that was just read
   * @exception                   Unable to read the line in the file
   */
  //-----------------------------------------------------------------------------
  public String readLine() throws IcofException {

    // Make sure the file is opened for reading.
    if (!isOpenRead()) {
      openRead();
    }

    String thisLine = getIcofStream().readLine();

    if (thisLine != null) {
      addLine(thisLine);
    }

    return thisLine;

  }



  //-----------------------------------------------------------------------------
  /**
   * Remove a file or directory.  The boolean parameter is used when removing
   *   directories to indicate whether or not to do a recursive remove.  The
   *   function returns a boolean to indicate whether the file/dir was
   *   successfully removed.
   *
   * @param     recursive         true, to recursively remove files/directories;
   *                              false, if a recursive remove is not desired
   * @return                      true, if the file/directory is successfully
   *                              removed; false, if not.
   * @exception IcofException     Bad return code from directory removal
   *                              system call.
   */
  //-----------------------------------------------------------------------------
  public boolean remove(boolean recursive) throws IcofException {

    String funcName = new String("remove(recursive)");

    //
    // Determine whether or not the file/dir exists.  If it doesn't, return
    //   true.
    //
    if (!exists()) {
      return true;
    }

    //
    // If the object is a file, or if it is not to be a recursive remove,
    //   just delete the file/dir with the java delete command.
    //
    if ((!isDirectory()) || (!recursive)) {
      return delete();
    }

    //
    // If we get this far, we are doing a recursive remove of a directory.
    //   Create the system command.
    //
    // **** HERE **** update for windows
    String command = new String("rm -fr " + getAbsolutePath());

    //
    // Execute the system command.
    //
    StringBuffer errorMsg = new StringBuffer();
    int rc = IcofSystemUtil.execSystemCommand(command, errorMsg);
    if (rc == 0) {
      return true;
    }

    //
    // If we get this far, the system call failed.  Throw an exception.
    //

    String msg = new String("Remove command, "
                            + command
                            + " failed.  RC = "
                            + rc
                            + "\n \n System message = "
                            + errorMsg.toString()
                            + "\n\n");
    IcofException ie = new IcofException(this.getClass().getName()
                                         ,funcName
                                         ,IcofException.SEVERE
                                         ,msg
                                         ,command);
    throw ie;

  }


  //-----------------------------------------------------------------------------
  /**
   * Set the permissions on this file or directory object
   *
   * @param     permissionMode    a string representing the permissions to
   *                              set on this file or directory. (ex. "777")
   * @exception IcofException     Unable to change the permission mode
   */
  //-----------------------------------------------------------------------------
  public void setPermissions(String permissionMode) throws IcofException {

    setPermissions(permissionMode, "", false);

  }


  //-----------------------------------------------------------------------------
  /**
   * Set the permissions on this file or directory object
   *
   * @param     permissionMode    a string representing the permissions to
   *                              set on this file or directory. (ex. "777")
   * @param     pattern           relevant only when changing permissions on
   *                              a directory.  Specifies which files should
   *                              be affected, by using a "pattern".  For
   *                              example, to set the permissions on all
   *                              files whose names start with "shipping", the
   *                              pattern would be "shipping*".
   * @param     isRecursive       true, if permissions are to be set for
   *                              all subdirectories (recursively); false, if
   *                              not.  Ignored if this object represents a
   *                              file.
   * @exception IcofException     Unable to change the permission mode
   */
  //-----------------------------------------------------------------------------
  public void setPermissions(String permissionMode
                             ,String pattern
                             ,boolean isRecursive)
  throws IcofException {

    String funcName = new String("setPermissions(String, String, boolean)");

     // Construct the command.
    String command = new String("chmod ");

    // A recursive permission change is meaningless if this is a file object.
    if (isDirectory()) {
      if (isRecursive) {
        command += "-R ";
      }
    }
    command += permissionMode + " " + getAbsolutePath();

    // The pattern contains a string to be matched, when changing permissions.
    //   For example, if you were trying to change permissions on all files
    //   whose names began with "shipping", then the pattern would be
    //   "shipping*".  Obviously, the pattern is only relevant when setting
    //   permissions on a directory object.
    if (isDirectory()) {
      command += separatorChar + pattern;
    }


    StringBuffer commandOutput = new StringBuffer();
    int rc = IcofSystemUtil.execSystemCommand(command, commandOutput);

    if (rc != 0) {
      IcofException ie = new IcofException(getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Change mode of "
                                           + getAbsolutePath()
                                           +" failed. Command = "
                                           +command
                                           ,commandOutput.toString());
      throw ie;
    }
  }


  //-----------------------------------------------------------------------------
  /**
   * Tar up the contents of this directory into the specified fileName.
   *
   * @param     tarFileName       the name of the tar file to be created
   * @exception IcofException     Unable to create the tar file
   */
  //-----------------------------------------------------------------------------
  public void tar(String tarFileName) throws IcofException {

    tarWithOptions("-cf", tarFileName, "*", true);

  }


  //-----------------------------------------------------------------------------
  /**
   * Tar up the contents of this directory into the specified fileName,
   *   resolving any linked files, so that the actual files are included
   *   in the tar file, not just the links.
   *
   * @param     tarFileName       the name of the tar file to be created
   * @exception IcofException     Unable to create the tar file
   */
  //-----------------------------------------------------------------------------
  public void tarResolveLinks(String tarFileName) throws IcofException {

    tarWithOptions("-chf", tarFileName, "*", true);

  }


  //-----------------------------------------------------------------------------
  /**
   * Tar up the contents of this directory into the specified fileName, using
   *   the specified tar options.  If included in the tarOptions string,
   *   "-f" must be the last option.
   *
   * @param     tarFileName       the name of the tar file to be created
   * @param     tarOptions        String containing the options for the tar
   *                              command ("-f" must the the last option in the
   *                              string)
   * @param     filesToInclude    If an empty string or null is specified, then
   *                              "." will be used.                       
   * @param     removeIfExists    true, to remove the tar file if it already
   *                              exists; false, to preserve it if there (as when
   *                              appending to an existing tar file)                                     
   * @exception IcofException     Unable to create the tar file
   */
  //-----------------------------------------------------------------------------
  public void tarWithOptions(String tarOptions, String tarFileName, 
                             String filesToInclude, boolean removeIfExists) 
  throws IcofException {

    String funcName = new String("tarWithOptions(String, String)");

    // Ensure that this object truly is a directory.  If not, then throw an
    //   exception.
    if (!isDirectory()) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Object to tar must be a directory  "
                                           ,getAbsolutePath());
      throw ie;
    }

    // Make sure this directory really exists
    validate(false);

    //
    // If tar file exists, remove it.  The false boolean indicates that we
    //   are working with a file, not a directory.
    //
    IcofFile tarFile= new IcofFile(tarFileName, false);
    if (removeIfExists) {
        if (tarFile.exists()) {
            tarFile.delete();
        }
    }

    // Ensure the last option is the -f parameter.  If not there, add it.
    tarOptions = tarOptions.trim();
    if (!tarOptions.endsWith("f")) {
        tarOptions += "f";
    }
    
    // Ensure filesToInclude has a value
    if (filesToInclude == null || filesToInclude.equals("")) {
        filesToInclude = ".";
    }
    
    // Construct the tar command
    String tarCommand = new String("tar " + getSystemTarParameter() + tarOptions 
                                   + " " + tarFile.getAbsolutePath() + " " + filesToInclude); 
    String[] tarCommandArray = new String[] {"/bin/sh", "-c", tarCommand};
    if (isWindowsOs()) {
      tarCommandArray = new String[] {"cmd", "/c", tarCommand};
    }

    
    StringBuffer errMsg = new StringBuffer();
    Vector<String> stdOutVector = new Vector<String>();

    // Create a tar file containing everything in the directory.
    int rc = IcofSystemUtil.execSystemCommand(tarCommandArray
                                              ,null
                                              ,this
                                              ,errMsg
                                              ,stdOutVector);

    if (rc != 0) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Tar command failed.  Command = "
                                           +tarCommand
                                           ,errMsg.toString());
      throw(ie);
    }
  }


  //-----------------------------------------------------------------------------
  /**
   * Tar up the contents of specified subdirectory into the specified fileName.
   *
   * @param     subDirectory      the subdirectory to be tarred
   * @param     tarFileName       the name of the tar file to be created
   * @exception IcofException     Unable to create the tar file
   */
  //-----------------------------------------------------------------------------
  public void tar(String subDirectory, String tarFileName) throws IcofException {

    //
    // Check to make sure the subdirectory exists.
    //
    IcofFile subDirFile = new IcofFile(this.getDirectoryName() + separatorChar + subDirectory
                                       ,true);
    subDirFile.validate(false);
    
    tarWithOptions("-cf", tarFileName, subDirectory, true);

  }


  //-----------------------------------------------------------------------------
  /**
   * Append the contents of the directory (or a single file in this directory)
   *   represented by this object to the specified tar file.
   *   (tar -rf <tarFileName>  <fileName or "">
   *
   * @param     fileName          "" to include all files in this directory
   *                              OR the name of a file in the directory to
   *                              be appended ot the tar file
   * @param     tarFileName       the name of the tar file to which to append
   *
   * @exception IcofException     Unable to append to tar file
   */
  //-----------------------------------------------------------------------------
  public void tarAppend(String fileName, String tarFileName) throws IcofException {

    String funcName = new String("tarAppend(String, String)");

    // If no fileName was specified, then include all files in this directory
    if (fileName.equals("")) {
      fileName = ".";
    }
    else {
      fileName = "./" + fileName;
    }

    //
    // Check to make sure the tar file exists.  If not, then we will
    //   create it.
    //
    String tarParms = "-r";
    IcofFile tarFile = new IcofFile(tarFileName, false);
    if (!tarFile.exists()) {
      tarParms = "-c";
    }
    tarParms += "f";

    tarWithOptions(tarParms, tarFileName, fileName, false);

  }


  //-----------------------------------------------------------------------------
  /**
   * Uncompress this file.  If this object is a directory, throw an exception.
   *   The resulting file will overlay the original file and will be named
   *   with the original file's name - ".gz".
   *
   * Because the underlying File class does not allow a file name to be
   *   changed, this method cannot update the file name (ie. cannot remove
   *   the .gz extension).  It simply returns the name of the uncompressed
   *   file.
   *
   * @return                      the name of the uncompressed file
   * @exception IcofException     Unable to uncompress file
   */
  //-----------------------------------------------------------------------------
  public String unCompress() throws IcofException {

    String funcName = new String("uncompress()");

    if (isDirectory()) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Attempting to uncompress a directory"
                                           ,getAbsolutePath());
      throw ie;
    }

    String uncompressedFileName = new String(getAbsolutePath());
    int index = uncompressedFileName.lastIndexOf(".gz");
    uncompressedFileName = uncompressedFileName.substring(0, index);
    IcofFile uncompressedFile = new IcofFile(uncompressedFileName,false);
    if (uncompressedFile.exists()) {
      uncompressedFile.delete();
    }

    String command = new String("gunzip " + getAbsolutePath());

    StringBuffer commandOutput = new StringBuffer();
    int rc = IcofSystemUtil.execSystemCommand(command, commandOutput);

    if (rc == 1) {
      IcofException ie = new IcofException(getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Uncompress of file "
                                           + getAbsolutePath()
                                           + " failed."
                                           ,command + "\n" + commandOutput.toString());
      throw ie;
    }

    if (rc == 2) {
      IcofException ie = new IcofException(getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Uncompress of file "
                                           + getAbsolutePath()
                                           + " produced warning(s)."
                                           ,command + "\n" + commandOutput.toString());
      throw ie;
    }

    if (rc != 0) {
      IcofException ie = new IcofException(getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Uncompress of file "
                                           + getAbsolutePath()
                                           + " produced errors(s)."
                                           ,command + "\n" + commandOutput.toString());
      throw ie;
    }

    return(uncompressedFileName);

  }


  //-----------------------------------------------------------------------------
  /**
   * Uncompress this file into the specified file.
   *   If this object is a directory, throw an exception.
   *
   * @param     tgtFile           the uncompressed file
   * @exception IcofException     Unable to uncompress file
   */
  //-----------------------------------------------------------------------------
  public void unCompress(IcofFile tgtFile) throws IcofException {

    String funcName = new String("unCompress(IcofFile)");

    // Ensure the source file (represented by this object) is not a directory
    //   and that the target file represents a file, not a directory.  If
    //   either are directories, then throw an exception
    if ((isDirectory()) || (tgtFile.representsDirectory())) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Attempting to uncompress a directory "
                                           +" or attempting to uncompress a file into "
                                           +" a directory"
                                           ,"Source = "
                                           +getAbsolutePath()
                                           +"\nTarget = "
                                           +tgtFile.asString());
      throw ie;
    }

    if (tgtFile.exists()) {
      tgtFile.delete();
    }

    String command = new String("gunzip -c "+ getAbsolutePath() + " > "
                                + tgtFile.getAbsolutePath());

    StringBuffer commandOutput = new StringBuffer();
    int rc = IcofSystemUtil.execSystemCommand(command, commandOutput);

    if (rc == 1) {
      IcofException ie = new IcofException(getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Uncompress of file "
                                           + getAbsolutePath()
                                           + " into "
                                           + tgtFile.getAbsolutePath()
                                           + " failed."
                                           ,command + "\n"
                                           + commandOutput.toString());
      throw ie;
    }

    if (rc == 2) {
      IcofException ie = new IcofException(getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Uncompress of file "
                                           + getAbsolutePath()
                                           + " into "
                                           + tgtFile.getAbsolutePath()
                                           + " produced warning(s)."
                                           ,command + "\n"
                                           + commandOutput.toString());
      throw ie;
    }

    if (rc != 0) {
      IcofException ie = new IcofException(getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Uncompress of file "
                                           + getAbsolutePath()
                                           + " into "
                                           + tgtFile.getAbsolutePath()
                                           + " produced errors(s)."
                                           ,command + "\n"
                                           + commandOutput.toString());
      throw ie;
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Untar the specified fileName into this directory.
   *
   * @param     tarFileName       the name of the tar file to be "untarred"
   * @exception IcofException     Unable to untar the file
   */
  //-----------------------------------------------------------------------------
  public void untar(String tarFileName) throws IcofException {

    IcofFile tarFile = new IcofFile(tarFileName,false);
    untar(tarFile);

  }


  //-----------------------------------------------------------------------------
  /**
   * Untar the specified file into this directory.
   *
   * @param     tarFile           the tar file to be "untarred"
   * @exception IcofException     Unable to untar the file
   */
  //-----------------------------------------------------------------------------
  public void untar(IcofFile tarFile) throws IcofException {

    String funcName = new String("untar(IcofFile)");

    // Ensure that this object truly is a directory.  If not, then throw an
    //   exception.
    if (!isDirectory()) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Object to untar into must be a directory  "
                                           ,getAbsolutePath());
      throw ie;
    }

    //
    // Verify existence of the tar file.  It's an error if the file does not exist.
    //
    tarFile.validate(false);

    // Construct the tar command
    String tarCommand = new String("tar -xf " + tarFile.getAbsolutePath());
    String[] tarCommandArray = new String[] {"/bin/sh", "-c", tarCommand};
    if (isWindowsOs()) {
      tarCommand = "tar --force-local -xf " + tarFile.getAbsolutePath();
      tarCommandArray = new String[] {"cmd", "/c", tarCommand};
    }

    StringBuffer errMsg = new StringBuffer();
    Vector<String> stdOutVector = new Vector<String>();

    // Create a tar file containing everything in the directory.
    int rc = IcofSystemUtil.execSystemCommand(tarCommandArray
                                         ,null
                                         ,this
                                         ,errMsg
                                         ,stdOutVector);

    if (rc != 0) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Untar command failed.  Command = "
                                           +tarCommand
                                           ,errMsg.toString());
      throw(ie);
    }
  }


  //-----------------------------------------------------------------------------
  /**
   * Get the contents of this tar file and 
   *
   * @exception IcofException     Unable to get the contents of the tar file
   */
  //-----------------------------------------------------------------------------
  public Vector<String> getTarContents() throws IcofException {

    String funcName = new String("getTarContents()");

    // Ensure that this object truly is a file.  If not, then throw an
    //   exception.
    if (!isFile()) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Object to get tar contents for must be a file  "
                                           ,getAbsolutePath());
      throw ie;
    }

    //
    // Verify existence of the tar file.  It's an error if the file does not exist.
    //
    validate(false);

    // Construct the tar command
    String tarCommand = new String("tar -tvf " + getAbsolutePath());
    String[] tarCommandArray = new String[] {"/bin/sh", "-c", tarCommand};
    if (isWindowsOs()) {
      tarCommand = "tar --force-local -tf " + getAbsolutePath();
      tarCommandArray = new String[] {"cmd", "/c", tarCommand};
    }

    StringBuffer errMsg = new StringBuffer();
    Vector<String> stdOutVector = new Vector<String>();

    // Create a tar file containing everything in the directory.
    int rc = IcofSystemUtil.execSystemCommand(tarCommandArray
                                              ,errMsg
                                              ,stdOutVector);

    if (rc != 0) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,"Tar command failed.  Command = "
                                           +tarCommand
                                           ,errMsg.toString());
      throw(ie);
    }
    
    return stdOutVector;
  }


  //-----------------------------------------------------------------------------
  /**
   * Determine whether or not a directory or file exists.  If not, create it
   *   if the input parameter is true.  Otherwise, throw an exception.
   *
   * @param     createIt          true, to create the file/directory if it does
   *                              not exist; false, to throw an exception if
   *                              file/directory does not exist.
   * @exception IcofException     file/directory does not exist
   */
  //-----------------------------------------------------------------------------
  public void validate(boolean createIt) throws IcofException {

    String funcName = new String("validate(boolean)");

    if (exists()) {
      return;
    }

    //
    // If we get here, then the directory or file did not exist.  Check to
    //   see if we should create it.  If not, throw an exception.
    //
    if (!createIt) {
      String msg = new String("Directory or file does not exist");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,getAbsolutePath());
      throw ie;
    }

    //
    // If this object represents a directory, create the directory and any
    //   missing parent directories.  If it is a file, create an empty file.
    //
    create();

  }


  //-----------------------------------------------------------------------------
  /**
   * Write the contents (vector) to the file represented by this object.  If
   *   this object is a directory, throw an exception.
   *
   * @exception IcofException     Attempting to write a directory, not a file,
   *                              or unable to write file for some other reason
   */
  //-----------------------------------------------------------------------------
  public void write() throws IcofException {

    write(getContents());

  }


  //-----------------------------------------------------------------------------
  /**
   * Write the contents of the specified vector to the file represented by this
   *   object.  If this object is a directory, throw an exception.
   *
   * @param    writeVector        the contents to be written to this file
   * @exception IcofException     Attempting to write a directory, not a file,
   *                              or unable to write file for some other reason
   */
  //-----------------------------------------------------------------------------
  public void write(Vector<String> writeVector) throws IcofException {

    String funcName = new String("write(Vector)");

    if (representsDirectory()) {
      String msg = new String("Cannot write a directory object");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,getAbsolutePath());
      throw ie;
    }

    //
    // If we get here, then we are dealing with a file.  Create the
    //   file output stream.
    //
    try {
      FileOutputStream fos = new FileOutputStream(this);
      setIcofStream(new IcofStream(fos));
    }
    catch (Exception e) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,e.toString()
                                           ,"");
      throw ie;
    }
    getIcofStream().write(writeVector);

  }


  //-----------------------------------------------------------------------------
  /**
   * Write or append the contents of the specified vector to the file represented
   *   by this object.
   *   If this object is a directory, throw an exception.
   *
   * @param    writeVector        the contents to be written or appended to this
   *                              file
   * @param     append            true, to append to file; false, to overwrite
   *                              file
   * @exception IcofException     Attempting to write or append to a directory,
   *                              or unable to write or append to file for some
   *                              other reason
   */
  //-----------------------------------------------------------------------------
  public void write(Vector<String> writeVector, boolean append) throws IcofException {

    String funcName = new String("write(Vector, boolean)");

    if (representsDirectory()) {
      String msg = new String("Cannot write a directory object");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,getAbsolutePath());
      throw ie;
    }

    //
    // If we get here, then we are dealing with a file.  Create the
    //   file output stream.  If append is true, then all writes will
    //   append to the stream, instead of overwriting it.
    //
    try {
      FileOutputStream fos = new FileOutputStream(getAbsolutePath(), append);
      setIcofStream(new IcofStream(fos));
    }
    catch (Exception e) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,e.toString()
                                           ,"");
      throw ie;
    }
    getIcofStream().write(writeVector);

  }


  //-----------------------------------------------------------------------------
  /**
   * Append the contents (vector) to the file represented by this object.  If
   *   this object is a directory, throw an exception.
   *
   * @exception IcofException     Attempting to append to a directory,
   *                              or unable to append to file for some
   *                              other reason
   */
  //-----------------------------------------------------------------------------
  public void writeAppend() throws IcofException {

    write(getContents(), true);

  }


  //-----------------------------------------------------------------------------
  /**
   * Write a single line to the file.
   *   If the line is null, close the file.
   *
   * @param     thisLine          the line to be written to the file
   * @exception IcofException     Unable to write to file
   */
  //-----------------------------------------------------------------------------
  public void writeLine(String thisLine) throws IcofException {

    // Make sure the stream is opened for writing.
    if (!isOpenWrite()) {
      openWrite();
    }

    getIcofStream().writeLine(thisLine);

  }


  //-----------------------------------------------------------------------------
  /**
   * Write or append a single line to the file.
   *   If the line is null, close the file.
   *
   * @param     thisLine          the line to be written to the file
   * @param     append            true, to append to file; false, to overwrite
   *                              file
   * @exception IcofException     Unable to write to file
   */
  //-----------------------------------------------------------------------------
  public void writeLine(String thisLine, boolean append) throws IcofException {

    // Make sure the stream is opened for appending.
    if (append) {
      if (!isOpenAppend()) {
        openAppend();
      }
    }
    else {
      if (!isOpenWrite()) {
        openWrite();
      }
    }

    getIcofStream().writeLine(thisLine);

  }


  //-----------------------------------------------------------------------------
  // Data members
  //-----------------------------------------------------------------------------
  private String   directoryName;
  private String   fileName;
  private Vector<String> contents;
  private boolean  directoryInd;
  private IcofStream icofStream;
  private String   osName;
  private boolean  windowsOsInd;
  private boolean  linuxOsInd;
  private boolean  aixOsInd;
  private String   fileSysName;


  //-----------------------------------------------------------------------------
  // Member "setter" functions
  //-----------------------------------------------------------------------------
  /**
   * Set directory name and file name
   *
   */
  protected void setNames() {

    String absolutePath = getAbsolutePath();
    if (representsDirectory()) {
      setDirectoryName(absolutePath);
      setFileName("");
    }
    else {
      setDirectoryName(absolutePath.substring(0, absolutePath.lastIndexOf(separatorChar)));
      setFileName(getName());
    }
  }
  /**
   * Set value of directory name
   *
   */
  protected void setDirectoryName(String aName) { directoryName = aName; }
  /**
   * Set value of file name
   *
   */
  protected void setFileName(String aName) { fileName = aName; }
  /**
   * Set value of directory indicator
   *
   */
  protected void setDirectoryInd(boolean aBoolean) { directoryInd = aBoolean; }
  /**
   * Set value of contents
   *
   */
  protected void setContents(Vector<String> aVector) { contents = aVector; }
  /**
   * Set value of icofStream
   *
   */
  protected void setIcofStream(IcofStream aStream) { icofStream = aStream; }
  /**
   * Set value of osName
   *
   */
  protected void setOsName() { 
      String os = (String) System.getProperty("os.name");
      osName = os.toUpperCase();
  }
  /**
   * Set value of windowsOsInd
   *
   */
  protected void setWindowsOsInd() { 
      windowsOsInd = false;
      if (getOsName().indexOf(Constants.WINDOWS) >= 0) {
          windowsOsInd = true;
      }
  }
  /**
   * Set value of linuxOsInd
   *
   */
  protected void setLinuxOsInd() { 
      linuxOsInd = false;
      if (getOsName().indexOf("LINUX") >= 0) {
          linuxOsInd = true;
      }
  }
  /**
   * Set value of aixOsInd
   *
   */
  protected void setAixOsInd() { 
      aixOsInd = false;
      if (getOsName().indexOf("AIX") >= 0) {
          aixOsInd = true;
      }
  }
  /**
   * Set value of fileSysName
   *
   */
  protected void setFileSysName() { 

      if (getDirectoryName().startsWith("/" + Constants.AFS_FILE_SYSTEM)) {
          fileSysName = Constants.AFS_FILE_SYSTEM;
      }
      else if (getDirectoryName().startsWith("/" + Constants.GSA_FILE_SYSTEM)) {
          fileSysName = Constants.GSA_FILE_SYSTEM;
      }
      else {
          fileSysName = Constants.LOCAL_FILE_SYSTEM;
      }
  }


  //-----------------------------------------------------------------------------
  /**
   * Initialization function, called by constructors.
   *
   * @param    aDirectoryInd      true, if this object represents a directory;
   *                              false, if it represents a file
   */
  //-----------------------------------------------------------------------------
  private void initialize(boolean aDirectoryInd) {

    setDirectoryInd(aDirectoryInd);
    setNames();
    Vector<String> tmpVector = new Vector<String>();
    setContents(tmpVector);
    setIcofStream(new IcofStream());
    setOsName();
    setWindowsOsInd();
    setAixOsInd();
    setLinuxOsInd();
    setFileSysName();

  }

}

//==========================  END OF FILE  ====================================
