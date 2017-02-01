/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2011 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *    FILE: IntranetUser.java
 *
 * CREATOR: Karen K. Witt
 *    DEPT: AW0V
 *    DATE: 02/21/2010
 *
 *-PURPOSE---------------------------------------------------------------------
 * IntranetUser class definition file.  Retrieves the user's intranet id and
 *   password from the .who file in the user's afs home directory. 
 *-CHANGE LOG------------------------------------------------------------------
 * 02/21/2011 KKW  Initial coding.
 * 03/10/2011 KKW  Added constructWhoFileName method. 
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.common;

import java.util.PropertyResourceBundle;

public class IntranetUser {

    // Data members and constants
    private String intranetId;
    private String password;

    /**
     * Constructor
     * 
     * This constructor looks up afs/gsa userid for the current user.  It
     *   then looks in the home directory for that user to find the .who
     *   file.  If there is no .who file or if it contains unexpected contents
     *   an exception will be thrown.
     * 
     * @exception IcofException
     *                .who file does not exist or contains unexpected contents
     */
    public IntranetUser() throws IcofException {
        initialize();
    }

    /**
     * Get the value of the intranet id for this user
     */
    public String getIntranetId() {
        return intranetId;
    }

    /**
     * Get the value of the intranet password for this user
     */
    public String getPassword() throws IcofException {
        return password;
    }

    /**
     * Set the value of the user's intranet id
     */
    protected void setIntranetId(String anIntranetId) {
        intranetId = anIntranetId;
    }

    /**
     * Set the value of the user's intranet password
     */
    protected void setPassword(String aPassword) {
        password = aPassword;
    }

    /**
     * Construct the name of the .who file 
     */
    public static String constructWhoFileName() throws IcofException {

        // Construct the full path to the .who file
        String filename = System.getProperty(Constants.USER_HOME_PROPERTY_TAG)
                        + IcofFile.separator + ".who";
        return filename;
    }

    /**
     * Read the .who file in the user's home directory to get the user's 
     *   intranet id and password
     * 
     */
    private void initialize() throws IcofException {

        // Construct the full path to the .who file
        String filename = constructWhoFileName();

        // See if the file exists.  Throw an exception if it does not.
        IcofFile whoFile = new IcofFile(filename, false);
        whoFile.validate(false);
        whoFile.openRead();

        // Read the file as a properties file
        PropertyResourceBundle prb = null;
        try {
            prb = new PropertyResourceBundle(whoFile.getIcofStream().getInStream());
        }
        catch(Exception e) {
            IcofException ie = new IcofException(getClass().getName(), 
                                                 "initialize()", 
                                                 IcofException.SEVERE, 
                                                 IcofException.printStackTraceAsString(e),
                                                 "");
            throw ie;
        }
        
        if (prb == null) {
            StringBuffer msg = new StringBuffer("Unable to open property file ");
            msg.append(whoFile);
            IcofException ie = new IcofException(getClass().getName(), 
                                                 "initialize()", 
                                                 IcofException.SEVERE, 
                                                 msg.toString(),
                                                 "");
            throw ie;
        }

        try {
            setIntranetId(IcofSystemUtil.getProperty("username", prb));
            setPassword(IcofSystemUtil.getProperty("password", prb));
        }
        catch(IcofException ie) {
            IcofException ie2 = new IcofException(ie.getMessage() + "\nProperty File: " + filename, IcofException.SEVERE);
            throw ie2;
        }
        
        // Close the file
        whoFile.closeRead();

    }
}
