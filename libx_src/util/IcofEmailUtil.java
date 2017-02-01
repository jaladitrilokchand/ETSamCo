/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2005 - 2011 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *    FILE: IcofEmailUtil.java
 *
 * CREATOR: Karen K. Witt
 *    DEPT: AW0V
 *    DATE: 10/06/2005
 *
 *-PURPOSE---------------------------------------------------------------------
 * IcofEmailUtil class definition file.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 10/06/2006 KKW  Initial coding.
 * 05/21/2007 RAM  Synchronized all public static methods
 * 05/22/2008 KKW  Added method, getEmailAddress(StrMYSQL_DB_SERVERing)
 *                 to get a ClearCase user's email address using
 *                 his/her afs userid.
 * 02/09/2009 AS   Updated MYSQL_DB_SERVER to point to w3iipmds.
 * 10/07/2009 KKW  Corrected some javadoc.
 * 02/21/2011 KKW  Added validateAddresses method
=============================================================================
 * </pre>
 */

package com.ibm.stg.iipmds.icof.component.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;

public class IcofEmailUtil {

    public static final String TITLE = "JR Jr jr SR Sr sr II ii III iii IV iv";
    public static final String BTV_IBM_COM = "btv.ibm.com";

    // MySQL database constants
    public static final String MYSQL_DB_SERVER = "w3iipmds.btv.ibm.com";
    public static final String MYSQL_DB_NAME = "clearcase";

    // -----------------------------------------------------------------------------
    /**
     * Construct dummy intranet id, from the specified string.
     * 
     * @param userid a userid from which to construct a dummy intranet id.
     * 
     * @return the dummy intranet id (userid@btv.ibm.com)
     */
    // -----------------------------------------------------------------------------
    public static synchronized String constructDummyIntranetID(String userid) {

        // First, make sure that the input id is not already a valid intranet
        // id.
        if (isValidEmailAddress(userid)) {
            return userid;
        }

        // Add "@btv.ibm.com" to the input userid and return it.
        String dummyID = userid;
        dummyID += Constants.AT_SIGN + BTV_IBM_COM;
        return dummyID;
    }

    // --------------------------------------------------------------------------
    /**
     * Evaluate given fullName string to pull out firstName. Check to see if the
     * name contains a title and remove both the lastName and title
     * 
     * @param fullName the full name of a person in the form of
     *            "firstname lastname title".
     * 
     * @return the first name.
     */
    // --------------------------------------------------------------------------
    public static synchronized String getFirstName(String fullName) {

        // Remove any excess spaces from the name
        fullName = fullName.trim();

        // Choose the last word in the name
        String tail = fullName.substring(fullName.lastIndexOf(" ") + 1);

        // Check to see if the last word is considered a title
        if (TITLE.indexOf(tail) > 0) {

            // When the last word is a title, return everything but the last two
            // words in the name as the firstName
            String removeTitle = fullName.substring(0,
                                                    fullName.lastIndexOf(" "));
            return removeTitle.substring(0, removeTitle.lastIndexOf(" "));

        } else {

            // Otherwise return everything but the last word in the name as the
            // firstName
            return fullName.substring(0, fullName.lastIndexOf(" "));

        }

    }

    // --------------------------------------------------------------------------
    /**
     * Evaluate given fullName string to pull out lastName. Check to see if the
     * name contains a title and return both the lastName and title
     * 
     * @param fullName the full name of a person in the form of
     *            "firstname lastname title".
     * 
     * @return the last name.
     */
    // --------------------------------------------------------------------------
    public static synchronized String getLastName(String fullName) {

        // Remove any excess spaces from the name
        fullName = fullName.trim();

        // Choose the last word in the name
        String tail = fullName.substring(fullName.lastIndexOf(" ") + 1);

        // Check to see if last word is considered a title
        if (TITLE.indexOf(tail) > 0) {

            // When the last word is a title, return the last two words in the
            // name
            // as the lastName. First the name has the title removed and is
            // stored in removeTitle. Then the last word is found in removeTitle
            // and the index is used to determine what is the lastName in
            // fullName
            String removeTitle = fullName.substring(0,
                                                    fullName.lastIndexOf(" "));
            return fullName.substring(removeTitle.lastIndexOf(" ") + 1);

        } else {

            // Otherwise return the last word in the name as the lastName
            return tail;

        }

    }

    // -----------------------------------------------------------------------------
    /**
     * Verify that the input string is a valid email address format.
     * 
     * @param address the email address to be verified
     * @return true, if format is valid; false, if not
     */
    // -----------------------------------------------------------------------------
    public static synchronized boolean isValidEmailAddress(String address) {

        if (address == null)
            return false;

        // Check for '@'
        int i = address.indexOf("@");
        if (i < 0) {
            return false;
        }

        // Get the domain name
        String domain = address.substring(i + 1, address.length());

        // Domain name should have at least one '.'
        if (domain.indexOf(".") < 0) {
            return false;
        }

        // Check address for any " " (empty string)
        if (address.indexOf(" ") != -1) {
            return false;
        }

        // Check for non-allowed chars in address
        // Valid chars: 'a-Z', '@', '-', '_', '.', '+'
        char c;
        for (int j = 0; j < address.length(); j++) {
            c = address.charAt(j);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                            || (c >= '0' && c <= '9')
                            || (c == '@')
                            || (c == '-')
                            || (c == '_')
                            || (c == '.') || (c == '+'))) {
                return false;
            }
        }

        return true;
    }

    // --------------------------------------------------------------------------
    /**
     * Given a Clearcase user's afs userid, get his/her email address from a
     * mySql database.
     * 
     * @param afsId afs userid to be looked up.
     * 
     * @return the email address.
     */
    // --------------------------------------------------------------------------
    public static synchronized String getEmailAddress(String afsId)
    throws IcofException {

        String funcName = "getEmailAddress(String)";
        boolean found = false;
        try {

            String emailAddr = "";

            // Begin by connecting to the mySql database
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String dbURL = "jdbc:mysql://" + MYSQL_DB_SERVER
            + "/"
            + MYSQL_DB_NAME;
            Connection con = DriverManager.getConnection(dbURL,
                                                         Constants.DB_ACCESS_ID,
            "");

            // Create and run the query
            Statement stmt = con.createStatement();
            String query = "Select email from users where afsid = '" + afsId
            + "'";
            ResultSet result = stmt.executeQuery(query);

            // Get the result of the query
            found = result.next();
            if (found) {
                emailAddr = result.getString(1);
                return emailAddr;
            } else {
                String msg = "Unable to locate afsid, " + afsId
                + ", in clearcase "
                + "MySQL database.";
                IcofException ie = new IcofException(CLASS_NAME,
                                                     funcName,
                                                     IcofException.SEVERE,
                                                     msg,
                "");
                throw ie;

            }
        } catch (IcofException ie) {
            throw ie;
        } catch (Exception e) {
            String msg = "Error occurred accessing clearcase MySQL database.\n" + e.getMessage();
            IcofException ie = new IcofException(CLASS_NAME,
                                                 funcName,
                                                 IcofException.SEVERE,
                                                 msg,
                                                 IcofException.printStackTraceAsString(e));
            throw ie;

        }

    }


    //-----------------------------------------------------------------------------
    /**
     * Validates the Vector of email addresses 
     *
     * @param  anAppContext         the Application Context
     * @param  addresses            the vector of email addresses 
     */
    //-----------------------------------------------------------------------------
    public static void validateAddresses(AppContext anAppContext, 
                                         Vector<String> addresses) 
    throws IcofException {

        Vector<String> invalidEmailAddresses = new Vector<String>();
        for (int i = 0; i < addresses.size(); i++) {
            String emailAddr = addresses.elementAt(i);
            if (!IcofEmailUtil.isValidEmailAddress(emailAddr)) {
                invalidEmailAddresses.add(emailAddr);
            }
        }

        // If there were any invalid email addresses in the aclList, throw an
        //   exception
        if (!invalidEmailAddresses.isEmpty()) {
            StringBuffer msg = new StringBuffer("The input Vector contains strings that are not formatted as email addresses: ");
            msg.append(IcofCollectionsUtil.getVectorAsString(invalidEmailAddresses, ", "));
            IcofException ie = new IcofException(CLASS_NAME, 
                                                 "validateAddresses(AppContext, Vector<String>)", 
                                                 IcofException.SEVERE, 
                                                 msg.toString(), "");
            anAppContext.getSessionLog().log(ie);
            throw ie;
        }
    }
    // -----------------------------------------------------------------------------
    // Data elements.
    // -----------------------------------------------------------------------------
    private static final String CLASS_NAME = "IcofEmailUtil";

}

// ========================== END OF FILE ====================================
