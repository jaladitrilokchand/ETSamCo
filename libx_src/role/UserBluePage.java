/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2009 - 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *    FILE: UserBluePage.java
 *
 * CREATOR: Karen K. Witt
 *    DEPT: AW0V
 *    DATE: 10/14/2009
 *
 *-PURPOSE---------------------------------------------------------------------
 * UserBluePage class definition file.
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 10/14/2009 KKW  Initial coding.
 * 11/23/2009 AS   Rewrote the class to work with BluPages APIs.
 * 07/20/2010 KKW  Updated error message in retrieveAttributes so that it includes
 *                 that was being looked up when the exception occurred.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.iipmds.icof.component.role;

import com.ibm.bluepages.BPResults;
import com.ibm.bluepages.BluePages;
import com.ibm.bluepages.utilities.BPPerson;
import com.ibm.bluepages.utilities.BPWrapper;
import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.mom.IcofObject;

public class UserBluePage extends IcofObject {

    // Data members and constants
    private String intranetId = null;
    private String countryCode = null;
    private String fullName = null;
    private String phoneNumber = null;
    private String serialNumber = null;

    /**
     * Constructor
     * 
     * This constructor looks up the specified user intranet id (email address)
     * in IBM BluePages to populate the class.
     * 
     * @param anAppContext
     *            the db2 context and user information
     * @param anIntranetID
     *            the intranet id to be looked up
     * 
     * @exception IcofException
     *                user does not exist in database
     */
    public UserBluePage(AppContext anAppContext, String anIntranetID)
	    throws IcofException {

	super(anAppContext, "", Constants.NULL_DATE, "", Constants.NULL_DATE,
		"", Constants.NULL_DATE);
	setIntranetId(anIntranetID);
	setMapKey(anAppContext, anIntranetID);
	retrieveAttributes();
    }

    /**
     * Get the value of the intranet id for this user
     */
    public String getIntranetId() {
	return intranetId;
    }

    /**
     * Get the user's full name as "Lastname, Firstname"
     */
    public String getFullName() throws IcofException {
	return fullName;
    }

    /**
     * Get the user's serial number
     */
    public String getSerialNumber() throws IcofException {
	return serialNumber;
    }

    /**
     * Get the user's country code
     */
    public String getCountryCode() throws IcofException {
	return countryCode;
    }

    /**
     * Get the value of the user's phone (tie line) number
     */
    public String getPhoneNumber() throws IcofException {
	return phoneNumber;
    }

    /**
     * Get the value of the user's intranet email address
     */
    public String getEmailAddr() throws IcofException {
	return intranetId;
    }

    /**
     * Get contents of Icof User as a string
     * 
     * @return the contents of this object as a string
     * @exception IcofException
     *                Unable to create the formatted string
     */
    public String asString(AppContext anAppContext) throws IcofException {

	return ("Intranet Userid: " + intranetId + ";" + "Full Name: "
		+ fullName + ";" + "Serial Number: " + serialNumber + ";"
		+ "Country Code: " + countryCode + ";" + "Phone Number: "
		+ phoneNumber + ";" + "Map Key: " + getMapKey(anAppContext));

    }

    /**
     * Set the value of the key used for treeMaps
     */
    protected void setMapKey(AppContext anAppContext) {
	setMapKey(anAppContext, getIntranetId());
    }

    /**
     * Set the value of the user's intranet id
     */
    protected void setIntranetId(String anIntranetId) {
	intranetId = anIntranetId;
    }

    /**
     * Retrieve the bluepages attributes, using the intranet id.
     * 
     * @param anAppContext
     */
    private void retrieveAttributes() throws IcofException {

	try {

	    // Retrieve user info from BluePages using Internet address as key
	    BPResults bpResults = BluePages
		    .getPersonsByInternet(getEmailAddr());
	    BPWrapper bpWrapper = new BPWrapper(bpResults);
	    BPPerson bpPerson = bpWrapper.toBPPerson();

	    fullName = bpPerson.getName();
	    serialNumber = bpPerson.getEmpnum();
	    countryCode = bpPerson.getEmpcc();
	    phoneNumber = bpPerson.getTie();

	} catch (Exception e) {
	    IcofException ie = new IcofException(this.getClass().getName(),
		    "retrieveAttributes(AppContext)", IcofException.SEVERE, e
			    .getMessage()
			    + "\n" + IcofException.printStackTraceAsString(e),
		    getEmailAddr() + " not found in BluePages");
	    throw ie;
	}
    }

}
