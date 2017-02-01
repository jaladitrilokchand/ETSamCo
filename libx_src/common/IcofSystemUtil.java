/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2005 - 2011 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *    FILE: IcofSystemUtil.java
 *
 * CREATOR: Gregg Stadtlander
 *    DEPT: 5ZIA
 *    DATE: 12/15/2005
 *
 *-PURPOSE---------------------------------------------------------------------
 * IcofSystemUtil class definition file.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 12/15/2005 GFS  Initial coding. (copied methods from Constants.java)
 * 03/01/2007 KKW  Added determineHighLvlQualifier
 * 04/27/2007 GFS  Synchronized all public static methods.
 * 05/22/2007 RAM  Synchronized added public static methods.
 * 07/12/2007 KKW  Ensured all execSystemCommand logic is in one method and
 *                 that the variants all call that method.  Added retry logic
 *                 to that method.
 * 08/10/2007 KKW  Reversed the change from 07/12 -- added the code back for the
 *                 variant of execSystemCommand(String[], StringBuffer, Vector)
 *                 so that it does not trickle down to the execSystemCommand 
 *                 variant with the retry logic.  This is because many of the
 *                 ClearCase system calls do not return any particular rc, which
 *                 was causing the retry logic to be executed the maximum
 *                 number of times (as defined by a constant).
 * 05/16/2008 GFS  Added a new version of parseCmdLine() that allows for more
 *                 complex command line arguements.
 * 03/20/2009 MJD  Fixed a bug in parseCmdLine(String[],String,Hashtable) 
 *                 that would generate an ArrayIndexOutOfBoundsException when
 *                 the last command line switch entered by the user required
 *                 an argument but none was provided.  Before attempting to 
 *                 reference a non-existent array element, it now checks to
 *                 ensure that nextArg < argv.length before proceeding. NOTE:
 *                 parseCmdLine(String[],Vector,Vector,Hashtable) may need
 *                 a fix as well.
 * 03/12/2009 AS   Added isWindowsSystem method.
 * 07/29/2009 KKW  Corrected some javadoc.                
 * 08/12/2009 KKW  Updated to use WINDOWS constant.
 * 04/23/2010 KKW  Added getHostName()  
 * 06/10/2010 KKW  Added methods to detect if afs is large file enabled.  Also
 *                 added getMaxAfsFileSize() method.
 * 08/10/2010 KKW  Commented out System.out.println calls
 * 01/14/2011 KKW  Fixed java 1.5 collection warnings.  
 *                 Added runCommand(String, String) method.
 * 02/03/2011 KKW  Included command in exception message from runCommand method.
 * 02/21/2011 KKW  Added getProperty method.                                                  
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.common;

import java.net.InetAddress;
import java.util.Hashtable;
import java.util.PropertyResourceBundle;
import java.util.Vector;

public class IcofSystemUtil implements java.io.Serializable {

    // Constants for checking afs large file enablement
    public static final double MIN_LFE_AFS_VERSION = 3.6;
    public static final double MIN_LFE_AFS_SUBVERSION = 2.64;
    public static final double MIN_LFE_OPEN_AFS_VERSION = 1.4;
    public static final String AFS_VERSION = "AFS version:";
    public static final String OPEN_AFS = "OpenAFS";
    public static final String BASE_CONFIGURATION = "Base configuration afs";


    // -----------------------------------------------------------------------------
    /**
     * Get the name of the machine this code is running on
     *  
     * @return the machine name
     * @throws IcofException unable to determine host name
     * 
     */
    // -----------------------------------------------------------------------------
    public static synchronized String getHostName() throws IcofException {

        // Get the host name
        InetAddress localMachine = null;
        try {
            localMachine = InetAddress.getLocalHost();
        }
        catch (Exception e) {
            IcofException ie = 
                new IcofException(CLASS_NAME, 
                                  "getHostName()", 
                                  IcofException.SEVERE, 
                                  e.getMessage(), "");
            throw ie;
        }

        return localMachine.getHostName();

    }
    // -----------------------------------------------------------------------------
    /**
     * Determine the high level qualifier to be used for constructing directory
     * names and file names, based on the operating system platform.
     * 
     * @param appMode
     *            application mode
     * @return the high level qualifier.
     * 
     */
    // -----------------------------------------------------------------------------
    public static synchronized String determineHighLevelQualifier(String appMode) {

        String osName = System.getProperty("os.name");
        osName = osName.toUpperCase();
        if (osName.indexOf(Constants.WINDOWS) != -1) {
            return Constants.SAMBA_BTV;
        } else if (appMode.equals(Constants.ICCPROD)) {
            return Constants.ICC_GSA_DIR;
        } else {
            return Constants.AFS_BTV_DATA;
        }

    }

    // -----------------------------------------------------------------------------
    /**
     * Determine the system command shell based on the operating system
     * platform.
     * 
     * @return vector containing the system command shell.
     * 
     */
    // -----------------------------------------------------------------------------
    public static synchronized Vector<String> determineCommandShell() {

        String osName = System.getProperty("os.name");
        osName = osName.toUpperCase();
        Vector<String> cmd = new Vector<String>();
        if (osName.indexOf(Constants.WINDOWS) != -1) {
            cmd.add("cmd");
            cmd.add("/c");
        } else {
            cmd.add("/bin/sh");
            cmd.add("-c");
        }

        return cmd;

    }


    //-------------------------------------------------------------------------
    /**
     * Execute the specified system command and throw an exception with the 
     * specified error message if the command fails.
     *    
     * Use this method only if an exception should be thrown for a non-zero
     * return code and only if no retry is desired if the command fails.
     * 
     * @param command      the command to run
     * @param errMessage   the error message to use if an exception occurs.
     *
     * @exception Exception Trouble executing command.
     */
    //-------------------------------------------------------------------------
    public static synchronized Vector<String> runCommand(String command,
                                                         StringBuffer errMessage) 
    throws Exception {

        
        // Run the command
        Vector<String> vcResults = new Vector<String>();
        StringBuffer sbErrorMsg = new StringBuffer();
        int iRc = IcofSystemUtil.execSystemCommand(command
                                                   ,sbErrorMsg
                                                   ,vcResults);
        
        
        // Throw exception if the system call didn't work correctly
        if (iRc != 0) {
            throw new Exception(errMessage.toString() 
                                + "\n-- Command: " + command
                                + "\n-- Msg: " + sbErrorMsg.toString());
        }
        
        return vcResults;
    }

    
    // -----------------------------------------------------------------------------
    /**
     * Execute system command.
     * 
     * Use this method only if the results (stdout) are not needed.
     * 
     * This method will create a String array containing three elements: (for
     * AIX) (for Windows) -- "/bin/sh" -- "cmd" -- "-c" -- "/c" -- command --
     * command
     * 
     * It will then call another execSystemCommand method, passing the String
     * array. Doing so ensures that all system calls are executed in a
     * consistent manner.
     * 
     * @param command
     *            a String containing the command to be executed
     * @param errorMsg
     *            a StringBuffer that will return the contents of stdErr and
     *            stdOut if the command produces a non-zero return code.
     * 
     * @return the return code from the command.
     * 
     * @exception IcofException
     *                Problem executing system call.
     */
    // -----------------------------------------------------------------------------
    public static synchronized int execSystemCommand(String command,
                                                     StringBuffer errorMsg) throws IcofException {

        Vector<String> shell = determineCommandShell();
        shell.add(command);
        String[] commandArray = new String[shell.size()];
        for (int i = 0; i < shell.size(); i++) {
            commandArray[i] = (String) shell.elementAt(i);
        }

        Vector<String> results = new Vector<String>();

        int rc = execSystemCommand(commandArray, errorMsg, results);

        return rc;
    }

    // -----------------------------------------------------------------------------
    /**
     * Execute system command without any retry logic.
     * 
     * This method will create a String array containing three elements: (for
     * AIX) (for Windows) -- "/bin/sh" -- "cmd" -- "-c" -- "/c" -- command --
     * command
     * 
     * It will then call another execSystemCommand method, passing the String
     * array. Doing so ensures that all system calls are executed in a
     * consistent manner.
     * 
     * @param command
     *            a String containing the command to be executed
     * @param errorMsg
     *            a StringBuffer that will return the contents of stdErr and
     *            stdOut if the command produces a non-zero return code.
     * @param results
     *            a Vector containing all text written to stdOut as a result of
     *            executing the command.
     * 
     * @return the return code from the command.
     * 
     * @exception IcofException
     *                Problem executing system call.
     */
    // -----------------------------------------------------------------------------
    public static synchronized int execSystemCommand(String command,
                                                     StringBuffer errorMsg, 
                                                     Vector<String> results) 
    throws IcofException {

        int rc = execSystemCommand(command, errorMsg, results, false);

        return rc;

    }

    // -----------------------------------------------------------------------------
    /**
     * Execute system command.
     * 
     * This method will create a String array containing three elements: (for
     * AIX) (for Windows) -- "/bin/sh" -- "cmd" -- "-c" -- "/c" -- command --
     * command
     * 
     * It will then call another execSystemCommand method, passing the String
     * array. Doing so ensures that all system calls are executed in a
     * consistent manner.
     * 
     * @param command
     *            a String containing the command to be executed
     * @param errorMsg
     *            a StringBuffer that will return the contents of stdErr and
     *            stdOut if the command produces a non-zero return code.
     * @param results
     *            a Vector containing all text written to stdOut as a result of
     *            executing the command.
     * @param retry
     *            true to use retry logic; false to try only once
     * 
     * @return the return code from the command.
     * 
     * @exception IcofException
     *                Problem executing system call.
     */
    // -----------------------------------------------------------------------------
    public static synchronized int execSystemCommand(String command,
                                                     StringBuffer errorMsg, 
                                                     Vector<String> results, 
                                                     boolean retry)
    throws IcofException {

        Vector<String> shell = determineCommandShell();
        shell.add(command);
        String[] commandArray = new String[shell.size()];
        for (int i = 0; i < shell.size(); i++) {
            commandArray[i] = (String) shell.elementAt(i);
        }

        int rc = 0;
        if (retry) {
            rc = execSystemCommand(commandArray, null, null, errorMsg, results);
        } else {
            rc = execSystemCommand(commandArray, errorMsg, results);

        }

        return rc;

    }

    // -----------------------------------------------------------------------------
    /**
     * Execute system command without any retry logic.
     * 
     * @param commandArray
     *            a String array containing the command to be executed. For AIX,
     *            the first element of the array should be "/bin/sh". The second
     *            element should be "-c" and the third element should be the
     *            command. For Windows, the first element should be "cmd". The
     *            second element should be "/c" and the third element should be
     *            the command.
     * @param errorMsg
     *            a StringBuffer that will return the contents of stdErr and
     *            stdOut if the command produces a non-zero return code.
     * @param results
     *            a Vector containing all text written to stdOut as a result of
     *            executing the command.
     * 
     * @return the return code from the command.
     * 
     * @exception IcofException
     *                Problem executing system call.
     */
    // -----------------------------------------------------------------------------
    public static synchronized int execSystemCommand(String[] commandArray,
                                                     StringBuffer errorMsg, 
                                                     Vector<String> results) 
    throws IcofException {

        String funcName = "execSystemCommand(String[], StringBuffer, Vector)";

        // Clear the results vector
        if (!results.isEmpty()) {
            results.clear();
        }

        // Execute the system command and capture the data written to stdout
        int rc = 0;
        try {

            Runtime runTime = Runtime.getRuntime();
            Process proc = runTime.exec(commandArray);

            // "Gobble" the stdout and stderr streams to keep them from filling
            // up buffers, resulting in a hung process.
            IcofStream stdErrStream = new IcofStream(proc.getErrorStream());
            IcofStreamGobbler stdErrGobbler = new IcofStreamGobbler(
                                                                    stdErrStream);
            IcofStream stdOutStream = new IcofStream(proc.getInputStream());
            IcofStreamGobbler stdOutGobbler = new IcofStreamGobbler(
                                                                    stdOutStream);
            stdErrGobbler.start();
            stdOutGobbler.start();

            //
            // Check the results.
            //
            int procRc = proc.waitFor();
            rc = proc.exitValue();

            // Ensure both threads reading the streams have finished
            stdErrGobbler.join();
            stdOutGobbler.join();

            String tempStdErrMsg = "From StdErr:  "
                + IcofCollectionsUtil.getVectorAsString(stdErrGobbler
                                                        .getContents(), "\n") + "\n\n";

            String tempStdOutMsg = "From StdOut:  "
                + IcofCollectionsUtil.getVectorAsString(stdOutGobbler
                                                        .getContents(), "\n") + "\n\n";

            // Return the stderr/stdout streams if process failed otherwise
            // the results of stdout
            if (procRc != 0) {
                errorMsg.append(tempStdErrMsg);
                errorMsg.append(tempStdOutMsg);
            } else {
                results.addAll(stdOutGobbler.getContents());
                errorMsg.append(tempStdErrMsg);
            }

        } catch (Exception e) {
            IcofException ie = new IcofException(CLASS_NAME, funcName,
                                                 IcofException.SEVERE, e.toString(), "");
            throw ie;
        }

        return rc;

    }

    // -----------------------------------------------------------------------------
    /**
     * Execute the system command in the specified directory, using the
     * specified environment. If the environment is null, then the current
     * environment is used. If the directory is null, then the current directory
     * is used.
     * 
     * This method will retry the command repeatedly if it returns a non-zero
     * return code. The number of retries is controlled by the DEFAULT_NUM_TRIES
     * constant.
     * 
     * @param commandArray
     *            a String array containing the command to be executed. For AIX,
     *            the first element of the array should be "/bin/sh". The second
     *            element should be "-c" and the third element should be the
     *            command. For Windows, the first element should be "cmd". The
     *            second element should be "/c" and the third element should be
     *            the command.
     * @param envArray
     *            a String array containing the environment variable settings in
     *            the format name=value. Use null if no special environment
     *            settings are needed.
     * @param directory
     *            an IcofFile object representing the directory in which to
     *            execute the command. Use null if the command doesn't need to
     *            be executed in any particular directory.
     * @param errorMsg
     *            a StringBuffer that will return the contents of stdErr and
     *            stdOut if the command produces a non-zero return code.
     * @param results
     *            a Vector containing all text written to stdOut as a result of
     *            executing the command.
     * 
     * @return the return code from the command.
     * 
     * @exception IcofException
     *                Problem executing system call.
     */
    // -----------------------------------------------------------------------------
    public static synchronized int execSystemCommand(String[] commandArray,
                                                     String[] envArray, 
                                                     IcofFile directory, 
                                                     StringBuffer errorMsg,
                                                     Vector<String> results) 
    throws IcofException {

        String funcName = "execSystemCommand(String[], String[], IcofFile, StringBuffer, Vector)";

        // Clear the results vector
        if (!results.isEmpty()) {
            results.clear();
        }

        // Execute the system command and capture the data written to stdout
        int rc = 1;
        try {

            Runtime runTime = Runtime.getRuntime();
            for (int i = 0; i < Constants.DEFAULT_NUM_TRIES && rc != 0; i++) {
                Process proc = runTime.exec(commandArray, envArray, directory);

                // "Gobble" the stdout and stderr streams to keep them from
                // filling
                // up buffers, resulting in a hung process.
                IcofStream stdErrStream = new IcofStream(proc.getErrorStream());
                IcofStreamGobbler stdErrGobbler = new IcofStreamGobbler(
                                                                        stdErrStream);
                IcofStream stdOutStream = new IcofStream(proc.getInputStream());
                IcofStreamGobbler stdOutGobbler = new IcofStreamGobbler(
                                                                        stdOutStream);
                stdErrGobbler.start();
                stdOutGobbler.start();

                //
                // Check the results.
                //
                int procRc = proc.waitFor();
                rc = proc.exitValue();

                // Ensure both threads reading the streams have finished
                stdErrGobbler.join();
                stdOutGobbler.join();

                String tempStdErrMsg = "From StdErr:  "
                    + IcofCollectionsUtil.getVectorAsString(stdErrGobbler
                                                            .getContents(), "\n") + "\n\n";

                String tempStdOutMsg = "From StdOut:  "
                    + IcofCollectionsUtil.getVectorAsString(stdOutGobbler
                                                            .getContents(), "\n") + "\n\n";


                // Return the stderr/stdout streams if process failed otherwise
                // the results of stdout
                if (procRc != 0) {
                    errorMsg.append(tempStdErrMsg);
                    errorMsg.append(tempStdOutMsg);
                    // Sleep for 5 seconds before trying again.
                    Thread.sleep(5000);
                    System.out.println("Just slept for 5 seconds");
                } else {
                    results.addAll(stdOutGobbler.getContents());
                }
            }

            // If the process completed successfully, remove any error messages
            // that
            // may have been generated from previous attempts.
            if (rc == 0) {
                errorMsg = new StringBuffer("");
            }

        } catch (Exception e) {
            IcofException ie = new IcofException(CLASS_NAME, funcName,
                                                 IcofException.SEVERE, e.toString(), "");
            throw ie;
        }

        return rc;

    }

    // -----------------------------------------------------------------------------
    /**
     * Parse command line arguments stored in argv[] using the definition
     * string. The definition string uses a perl getopts like syntax where any
     * alpha char is the switch name. If the switch is followed by a colon(:)
     * then the switch requires an additional. Example, command = <-f file> [-d]
     * results in a definition of "f:d" or "df:" (order does not matter). The
     * hashtable is used to return the parse results where each key is the
     * switch (including the leading "-") and the values are either the
     * switches' additional argument or Constants.YES for single switches.
     * 
     * @param argv
     *            [] Array of command line arguments
     * @param definition
     *            a String representing the valid "switches" and parameters
     * @param parms
     *            a table of switches and their values, keyed by the switches
     *            (including the "-"). This table is build by this function.
     * @return error messages; null, if none.
     */
    // -----------------------------------------------------------------------------
    public static synchronized String parseCmdLine(String argv[],
                                                   String definition, 
                                                   Hashtable<String, String> params) 
    throws IcofException {

        Vector<String> singleSwitches = new Vector<String>();
        Vector<String> argSwitches = new Vector<String>();
        String errors = "";

        // Make sure the params hashtable is empty
        params.clear();

        // Parse the definition to determine valid switches and types
        for (int i = 0; i < definition.length(); i++) {

            String arg = definition.substring(i, i + 1);

            String nextArg = null;
            if (i + 2 <= definition.length()) {
                nextArg = definition.substring(i + 1, i + 2);
            }

            if ((nextArg == null) || (!nextArg.equals(Constants.COLON))) {
                singleSwitches.add(Constants.DASH + arg);
            } else {
                argSwitches.add(Constants.DASH + arg);
                i++;
            }

        }

        // Parse the command line args
        for (int j = 0; j < argv.length; j++) {

            if (singleSwitches.contains(argv[j])) {
                params.put(argv[j], Constants.YES);
                continue;
            }

            if (argSwitches.contains(argv[j])) {

                int nextArg = j + 1;

                if (nextArg == argv.length
                                || singleSwitches.contains(argv[nextArg])
                                || argSwitches.contains(argv[nextArg])) {
                    errors += " * Found switch but missing required argument ("
                        + argv[j] + ")\n";
                    continue;
                }

                params.put(argv[j], argv[nextArg]);
                j++;
                continue;

            }

            errors += " * Invalid command line argument (" + argv[j] + ")\n";

        }

        // Return null if found args which matched the definition string
        // otherwise
        // return any errors
        if (errors.equals("")) {
            return null;
        }

        return errors;

    }

    // -----------------------------------------------------------------------------
    /**
     * Parse command line arguments stored in argv[] using the definition array.
     * The hashtable is used to return the parse results where each key is the
     * switch (including the leading "-") and the values are either the
     * switches' additional argument or Constants.YES for single switches.
     * 
     * @param argv
     *            [] Array of command line arguments
     * @param vcSingleSwitches
     *            Vector of switches that take no arguments.
     * @param vcArgSwitches
     *            Vector of switches that take arguments.
     * @param parms
     *            A table of switches and their values, keyed by the switches
     *            (including the "-"). This table is build by this function.
     * @return error messages; null, if none.
     * @throws IcofException
     *             Trouble parsing the command line args.
     */
    // -----------------------------------------------------------------------------
    public static String parseCmdLine(String argv[], Vector<String> vcSingleSwitches,
                                      Vector<String> vcArgSwitches, 
                                      Hashtable<String, String> htParams) 
    throws IcofException {

        String errors = "";

        try {

            // Make sure the params hashtable is empty.
            if (htParams == null) {
                htParams = new Hashtable<String, String>();
            } else {
                htParams.clear();
            }

            // Parse the command line args.
            for (int j = 0; j < argv.length; j++) {

                if (vcSingleSwitches.contains(argv[j])) {
                    htParams.put(argv[j], Constants.YES);
                    continue;
                }

                if (vcArgSwitches.contains(argv[j])) {

                    int nextArg = j + 1;

                    if (vcSingleSwitches.contains(argv[nextArg])
                                    || vcArgSwitches.contains(argv[nextArg])) {
                        errors += " * Found switch but missing required"
                            + " argument (" + argv[j] + ")\n";
                        continue;
                    }

                    htParams.put(argv[j], argv[nextArg]);
                    j++;
                    continue;

                }

                errors += " * Invalid command line argument (" + argv[j]
                                                                      + ")\n";

            }

            // Return null if found args which matched the definition
            // string otherwise return any errors.
            if (errors.equals("")) {
                return null;
            }

        } catch (Exception e) {
            throw new IcofException(CLASS_NAME, "parseCmdLine()",
                                    IcofException.SEVERE, "Unable to parse the command line"
                                    + " parameters.\n", e.getMessage());
        } finally {
        }

        return errors;

    }

    /**
     * Check the operation system name for windows.
     * 
     * @return true if it is Windows operating system.
     */
    public static boolean isWindowsSystem() {
        boolean windowsIndicator = false;
        String osName = System.getProperty("os.name");
        osName = osName.toUpperCase();
        if (osName.indexOf(Constants.WINDOWS) != -1) {
            windowsIndicator = true;
        }
        return windowsIndicator;
    }

    /**
     * Check the operating system name for aix.
     * 
     * @return true if it is aix operating system.
     */
    public static boolean isAixSystem() {
        String osName = System.getProperty("os.name");
        osName = osName.toUpperCase();
        boolean aixOsInd = false;
        if (osName.indexOf("AIX") >= 0) {
            aixOsInd = true;
        }
        return aixOsInd;
    }


    //-----------------------------------------------------------------------------
    /**
     * Determine the max afs file size for the system we are running on
     *
     * @return                      the max file size in bytes
     * @throws IcofException  Unable to determine max afs file size                             
     */
    //-----------------------------------------------------------------------------
    public static synchronized long getMaxAfsFileSize() throws IcofException {

        long maxSize = Constants.MAX_FILE_SIZE_AFS;
        if (isAfsLfe()) {
            maxSize = Constants.MAX_FILE_SIZE_AFS_LFE;
        }

        return maxSize;
    }

    //-----------------------------------------------------------------------------
    /**
     * Determine if the afs client on the machine is large file-enabled.
     *
     * @return                      true, if afs client is large file-enabled;
     *                              false if not.
     * @throws IcofException  Unable to determine if afs large file enabled.                             
     */
    //-----------------------------------------------------------------------------
    public static synchronized boolean isAfsLfe() throws IcofException {

        String funcName = "isAfsLfe()";

        // Construct the command
        String command = "/usr/sbin/";
        if (isAixSystem()) {
            command = "/usr/afsws/etc/";
        }
        command += "rxdebug " 
            + IcofSystemUtil.getHostName() + " 7001 -vers";


//        System.out.println("Executing system command: " + command);

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
            IcofException ie = new IcofException(CLASS_NAME
                                                 ,funcName
                                                 ,IcofException.SEVERE
                                                 ,"Unable to determine if afs is large file enabled.  Command = "
                                                 + command
                                                 ,"Errors = " + errorMsg.toString()
                                                 + "\nResults = " + results.toString()
                                                 + "\nRc = " + rc + ".");
            throw ie;
        }

        //
        // Read the results
        //
        boolean isLfe = false;
        boolean found = false;
        StringBuffer resultsString = new StringBuffer();
        for (int i = 0; i < results.size() && !found; i++) {
            String thisLine = (String) results.elementAt(i);
            resultsString.append(thisLine);
            resultsString.append("\n");
//            System.out.println("thisLine: " + thisLine);

            thisLine = thisLine.trim();
            if (thisLine.startsWith(AFS_VERSION)) {
                found = true;
                if (thisLine.indexOf(OPEN_AFS) >= 0) {
                    isLfe = isOpenAfsLfe(thisLine);
                }
                else if (thisLine.indexOf(BASE_CONFIGURATION) >= 0) {
                    isLfe = isAfsLfe(thisLine);
                }
                else {
                    throw new IcofException(CLASS_NAME
                                            ,funcName
                                            ,IcofException.SEVERE
                                            ,"Unable to extract afs version from results: "
                                            + thisLine
                                            ,"");

                }
            }
        }
        
        // If we went through all the results from the command and never found
        //   a line for the afs version, throw an exception.  This means we
        //   had unexpected output from the command.
        if (!found) {
            throw new IcofException(CLASS_NAME
                                    ,funcName
                                    ,IcofException.SEVERE
                                    ,"Results did not contain any afs version information: "
                                    + resultsString.toString()
                                    ,"");
        }
        
        return isLfe;

    }


    //-----------------------------------------------------------------------------
    /**
     * Parse the results of the rxdebug command to determine if the afs client 
     * on the machine is large file-enabled.
     * The line being parsed should look like this:
     *   AFS version: Base configuration afs3.6 2.64;Navinesh-ID71117-afs3.6-AIX-large
     *
     *
     * @return                      true, if afs client is large file-enabled;
     *                              false if not.
     * @throws IcofException  Unable to determine if afs large file enabled.                             
     */
    //-----------------------------------------------------------------------------
    private static synchronized boolean isAfsLfe(String thisLine) throws IcofException {

        String funcName = "isAfsLfe(String)";

        // The line being parsed should look like this:
        //    AFS version: Base configuration afs3.6 2.64;Navinesh-ID71117-afs3.6-AIX-large

        boolean isLfe = false;
        int index = thisLine.indexOf(BASE_CONFIGURATION);
        if (index < 0) {
            throw new IcofException(CLASS_NAME
                                    ,funcName
                                    ,IcofException.SEVERE
                                    ,"Unable to extract afs version from results: "
                                    + thisLine
                                    ,"");

        }

        // Remove everything up to the version.  Should end up with
        // 3.6 2.64;Navinesh-ID71117-afs3.6-AIX-large
        int startPos = index + BASE_CONFIGURATION.length();
        thisLine = thisLine.substring(startPos);
        thisLine = thisLine.trim();

        // Now extract the version number and sub version number
        // 3.6 and 2.64
        int firstSpace = thisLine.indexOf(" ");
        String versionStr = thisLine.substring(0, firstSpace);
        double version = (new Double(versionStr)).doubleValue();
//        System.out.println("version: " + version);
        if (version > MIN_LFE_AFS_VERSION) {
            isLfe = true;
        }
        else if (version == MIN_LFE_AFS_VERSION) {
            index = thisLine.indexOf(";");
            if (index >= 0) {
                thisLine = thisLine.substring(0, index);
            }
            String subversionStr = thisLine.substring(firstSpace);
            double subversion = (new Double(subversionStr)).doubleValue();
//            System.out.println("subversion: " + subversion);
            if (subversion >= MIN_LFE_AFS_SUBVERSION) {
                isLfe = true;
            }
        }

        return isLfe;


    }


    //-----------------------------------------------------------------------------
    /**
     * Parse the results of the rxdebug command to determine if the afs client 
     * on the machine is large file-enabled.
     * The line being parsed should look like this:
     *   AFS version:  OpenAFS 1.4.8 built  2009-10-06
     *
     * @return                      true, if afs client is large file-enabled;
     *                              false if not.
     * @throws IcofException  Unable to determine if afs large file enabled.                             
     */
    //-----------------------------------------------------------------------------
    private static synchronized boolean isOpenAfsLfe(String thisLine) throws IcofException {

        String funcName = "isOpenAfsLfe(String)";
//        System.out.println("thisLine: " + thisLine);

        // The line being parsed should look like this:
        //    AFS version:  OpenAFS 1.4.8 built  2009-10-06

        boolean isLfe = false;
        int index = thisLine.indexOf(OPEN_AFS);
        if (index < 0) {
            throw new IcofException(CLASS_NAME
                                    ,funcName
                                    ,IcofException.SEVERE
                                    ,"Unable to extract afs version from results: "
                                    + thisLine
                                    ,"");

        }

        // Remove everything up to the version.  Should end up with
        // 1.4.8 built  2009-10-06
        int startPos = index + OPEN_AFS.length();
        thisLine = thisLine.substring(startPos);
        thisLine = thisLine.trim();

        // Now extract the version number (just the first two numbers will be
        //   sufficient -- ie 1.4 in the example)
        // 1.4
        int firstSpace = thisLine.indexOf(" ");
        thisLine = thisLine.substring(0, firstSpace);
        thisLine = thisLine.trim();
        Vector<String> digits = new Vector<String>();
        IcofCollectionsUtil.parseString(thisLine, ".", digits, true);
        if (digits.size() < 2) {
            throw new IcofException(CLASS_NAME
                                    ,funcName
                                    ,IcofException.SEVERE
                                    ,"Unable to extract afs version from results -- expected number such as 1.4.8: "
                                    + thisLine
                                    ,"");

        }

        // Convert these to a number and compare with the minimum version that 
        //   supports large files.
        String versionStr = digits.elementAt(0) + "." + digits.elementAt(1);
        double version = (new Double(versionStr)).doubleValue();
//        System.out.println("version: " + version);
        if (version >= MIN_LFE_OPEN_AFS_VERSION) {
            isLfe = true;
        }

        return isLfe;


    }


    /**
     * Retrieve the specified property from the specified properties file 
     * 
     * @param propertyName  the name of the property to retrieve
     * @param propertyFile  the propertyFile from which to retrieve it
     * 
     * @throws IcofException if the property does not exist in the file
     * 
     */
    public static String getProperty(String propertyName, PropertyResourceBundle prb) 
    throws IcofException {

        String propertyValue = null;
        try {
            propertyValue = prb.getString(propertyName);
        }
        catch(Exception e) {
            throw new IcofException(e.getMessage(), IcofException.SEVERE);
        }
        
        if (propertyValue == null) {
            StringBuffer msg = new StringBuffer("No value found for property \"");
            msg.append(propertyName);
            msg.append("\"");
            msg.append(" in resource bundle ");
            IcofException ie = new IcofException(CLASS_NAME, 
                                                 "getProperty(String, PropertyResourceBundle)", 
                                                 IcofException.SEVERE, 
                                                 msg.toString(),
                                                 "");
            throw ie;
        } 
        else {
            return propertyValue.trim();
        }
    }

    // -------------------------------------------------------------------------
    // Member "getter" functions
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Data members
    // -------------------------------------------------------------------------
    private static final String CLASS_NAME = "IcofSystemUtil";

    // -------------------------------------------------------------------------
    // Member "setter" functions
    // -------------------------------------------------------------------------

}

// ========================== END OF FILE ====================================
