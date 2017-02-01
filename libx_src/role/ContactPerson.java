/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2006 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *    FILE: ContactPerson.java
 *
 * CREATOR: Ryan A. Morgan
 *    DEPT: AW0V
 *    DATE: December 1, 2006
 *
 *-PURPOSE---------------------------------------------------------------------
 * ContactPerson class file.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 12/01/06	RAM		Initial Coding based on ContactPerson.C
 * 12/05/06 KKW   Modified to extend IcofObject.
 * 01/24/08 KKW   Fixed items identified by RSA Code Analysis tool --
 *                specifically using == and != to compare java objects.
 *=============================================================================
 * </pre>
 */
package com.ibm.stg.iipmds.icof.component.role;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.mom.IcofObject;

public class ContactPerson extends IcofObject {
  // Members
  private String name;
  private String addressLine1;
  private String addressLine2;
  private String city;
  private String state;
  private String zip;
  private String phoneNumber;
  private String faxNumber;
  private String emailAddr;
  private String location;
  private String userid;
  
  
  //Constructors
  //-----------------------------------------------------------------------------
  /**
   * Default Constructor - All attributes set to "" (empty)
   * 
   * @param	anAppContext	Application Context
   */
  //-----------------------------------------------------------------------------
  public ContactPerson(AppContext anAppContext) {
    this(anAppContext
        ,""
        ,""
        ,""
        ,""
        ,""
        ,""
        ,""
        ,""
        ,""
        ,""
        ,"");
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct the object with all information provided
   * 
   * @param anAppContext  Application Context
   * @param aName     A person's name
   * @param anAddressLine1  Line 1 of an address
   * @param anAddressLine2  Line 2 of an address
   * @param aCity     A city
   * @param aState      A state
   * @param aPhoneNumber  A phone number
   * @param aFaxNumber    A fax num
   * @param anEmailAddr   An e-mail address
   * @param aLocation   A location
   * @param aUserid     A userid
   */
  //-----------------------------------------------------------------------------
  public ContactPerson(AppContext anAppContext
                       ,String aName
                       ,String anAddressLine1
                       ,String anAddressLine2
                       ,String aCity
                       ,String aState
                       ,String aZip
                       ,String aPhoneNumber
                       ,String aFaxNumber
                       ,String anEmailAddr)  {
    this(anAppContext
         ,aName
         ,anAddressLine1
         ,anAddressLine2
         ,aCity
         ,aState
         ,aZip
         ,aPhoneNumber
         ,aFaxNumber
         ,anEmailAddr
         ,""
         ,"");
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Construct the object with all information provided
   * 
   * @param	anAppContext	Application Context
   * @param	aName			A person's name
   * @param anAddressLine1	Line 1 of an address
   * @param anAddressLine2	Line 2 of an address
   * @param	aCity			A city
   * @param	aState			A state
   * @param aPhoneNumber	A phone number
   * @param aFaxNumber		A fax num
   * @param anEmailAddr		An e-mail address
   * @param	aLocation		A location
   * @param aUserid			A userid
   */
  //-----------------------------------------------------------------------------
  public ContactPerson(AppContext anAppContext
      				         ,String aName
      				         ,String anAddressLine1
      				         ,String anAddressLine2
      				         ,String aCity
      				         ,String aState
      				         ,String aZip
      				         ,String aPhoneNumber
      				         ,String aFaxNumber
      				         ,String anEmailAddr
      				         ,String aLocation
      				         ,String aUserid)  {
    super(anAppContext);
    setName(anAppContext, aName);
    setAddressLine1(anAppContext,anAddressLine1);
    setAddressLine2(anAppContext,anAddressLine2);
    setCity(anAppContext, aCity);
    setState(anAppContext,aState);
    setZip(anAppContext,aZip);
    setPhoneNumber(anAppContext,aPhoneNumber);
    setFaxNumber(anAppContext,aFaxNumber);
    setEmailAddr(anAppContext,anEmailAddr);
    setLocation(anAppContext,aLocation);
    setUserid(anAppContext,aUserid);
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Construct the object when the address is not needed
   * 
   * @param	anAppContext	Application Context
   * @param	aName			A person's name
   * @param anEmailAddr		An e-mail address
   * @param aUserid			A userid
   */
  //-----------------------------------------------------------------------------
  public ContactPerson(AppContext anAppContext
      					       ,String aName
      				     	   ,String anEmailAddr
      					       ,String aUserid) {
    this(anAppContext
        ,aName
        ,""
        ,""
        ,""
        ,""
        ,""
        ,""
        ,""
        ,anEmailAddr
        ,""
        ,aUserid);
     
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Copy constructor
   * 
   * @param	anAppContext	Application Context
   * @param	aPerson			A ContactPerson object to be copied
   */
  //-----------------------------------------------------------------------------
  public ContactPerson(AppContext anAppContext
      				         ,ContactPerson aPerson) {
    super(anAppContext);
    setName(anAppContext, aPerson.getName(anAppContext));
    setAddressLine1(anAppContext,aPerson.getAddressLine1(anAppContext));
    setAddressLine2(anAppContext,aPerson.getAddressLine2(anAppContext));
    setCity(anAppContext, aPerson.getCity(anAppContext));
    setState(anAppContext,aPerson.getState(anAppContext));
    setZip(anAppContext,aPerson.getZip(anAppContext));
    setPhoneNumber(anAppContext,aPerson.getPhoneNumber(anAppContext));
    setFaxNumber(anAppContext,aPerson.getFaxNumber(anAppContext));
    setEmailAddr(anAppContext,aPerson.getEmailAddr(anAppContext));
    setLocation(anAppContext,aPerson.getLocation(anAppContext));
    setUserid(anAppContext,aPerson.getUserid(anAppContext));
  }
  
  
  // Member "setter" functions
  //-----------------------------------------------------------------------------
  /**
   * Set the person's name
   * 
   * @param anAppContext
   *          Application Context
   * @param aName
   *          A person's name
   */
  //-----------------------------------------------------------------------------
  public void setName(AppContext anAppContext, String aName) {
    name = aName;
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Set the person's address (line 1)
   * 
   * @param anAppContext
   *          Application Context
   * @param anAddressLine
   *          A person's address (line 1)
   */
  //-----------------------------------------------------------------------------
  public void setAddressLine1(AppContext anAppContext, String anAddressLine) {
    addressLine1 = anAddressLine;
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Set the person's address (line 2)
   * 
   * @param anAppContext
   *          Application Context
   * @param anAddressLine
   *          A person's addres (line 2)
   */
  //-----------------------------------------------------------------------------
  public void setAddressLine2(AppContext anAppContext, String anAddressLine) {
    addressLine2 = anAddressLine;
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Set the person's city
   * 
   * @param anAppContext
   *          Application Context
   * @param aCity
   *          A person's city
   */
  //-----------------------------------------------------------------------------
  public void setCity(AppContext anAppContext, String aCity) {
    city = aCity;
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Set the person's state
   * 
   * @param anAppContext
   *          Application Context
   * @param state
   *          A person's state
   */
  //-----------------------------------------------------------------------------
  public void setState(AppContext anAppContext, String aState) {
    state = aState;
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Set the person's zip
   * 
   * @param anAppContext
   *          Application Context
   * @param aZip
   *          A person's zip
   */
  //-----------------------------------------------------------------------------
  public void setZip(AppContext anAppContext, String aZip) {
    zip = aZip;
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Set the person's phone number
   * 
   * @param anAppContext
   *          Application Context
   * @param aPhoneNumber
   *          A person's phone number
   */
  //-----------------------------------------------------------------------------
  public void setPhoneNumber(AppContext anAppContext, String aPhoneNumber) {
    phoneNumber = aPhoneNumber;
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Set the person's fax number
   * 
   * @param anAppContext
   *          Application Context
   * @param aFaxNumber
   *          A person's fax number
   */
  //-----------------------------------------------------------------------------
  public void setFaxNumber(AppContext anAppContext, String aFaxNumber) {
    faxNumber = aFaxNumber;
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Set the person's e-mail address
   * 
   * @param anAppContext
   *          Application Context
   * @param anEmailAddr
   *          A person's e-mail address
   */
  //-----------------------------------------------------------------------------
  public void setEmailAddr(AppContext anAppContext, String anEmailAddr) {
    emailAddr = anEmailAddr;
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Set the person's location
   * 
   * @param anAppContext
   *          Application Context
   * @param aLocation
   *          A person's location
   */
  //-----------------------------------------------------------------------------
  public void setLocation(AppContext anAppContext, String aLocation) {
    location = aLocation;
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Set the person's user id
   * 
   * @param anAppContext
   *          Application Context
   * @param auserid
   *          A person's user id
   */
  //-----------------------------------------------------------------------------
  public void setUserid(AppContext anAppContext, String aUserid) {
    userid = aUserid;
  }
  
  
  // Member "getter" functions
  //-----------------------------------------------------------------------------
  /**
   * Get the person's name
   * 
   * @param	anAppContext	Application Context
   * @return name	The person's name
   */
  //-----------------------------------------------------------------------------
  public String getName(AppContext anAppContext) { return name; }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Get the person's address (line 1)
   * 
   * @param	anAppContext	Application Context
   * @return addressLine1	The address line 1
   */
  //-----------------------------------------------------------------------------
  public String getAddressLine1(AppContext anAppContext) { return addressLine1; }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Get the person's address (line 2)
   * 
   * @param	anAppContext	Application Context
   * @return addressLine2	The address line 2
   */
  //-----------------------------------------------------------------------------
  public String getAddressLine2(AppContext anAppContext) { return addressLine2; }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Get the person's city
   * 
   * @param	anAppContext	Application Context
   * @return city	The city
   */
  //-----------------------------------------------------------------------------
  public String getCity(AppContext anAppContext) { return city; }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Get the person's state
   * 
   * @param	anAppContext	Application Context
   * @return state	The state
   */
  //-----------------------------------------------------------------------------
  public String getState(AppContext anAppContext) { return state; }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Get the zip code
   * 
   * @param	anAppContext	Application Context
   * @return zip	The person's zip code
   */
  //-----------------------------------------------------------------------------
  public String getZip(AppContext anAppContext) { return zip; }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Get the complete three line address (Two lines only if no address 2nd line)
   * 
   * @param	anAppContext	Application Context
   * @return compAdd	The address
   */
  //-----------------------------------------------------------------------------
  public String getCompleteAddress(AppContext anAppContext) {
    String completeAddress = getAddressLine1(anAppContext) + "\n";

    if (!getAddressLine2(anAppContext).equals( "" )) {
      completeAddress += getAddressLine2(anAppContext) + "\n";
    }
    
    completeAddress += getCity(anAppContext) + ", "  + getState(anAppContext) 
    				+ "  " + getZip(anAppContext) + "\n";

    return completeAddress;
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Get the phone number
   * 
   * @param	anAppContext	Application Context
   * @return phoneNumber	The phone number
   */
  //-----------------------------------------------------------------------------
  public String getPhoneNumber(AppContext anAppContext) { return phoneNumber; }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Get the fax number
   * 
   * @param	anAppContext	Application Context
   * @return faxNumber	The fax number
   */
  //-----------------------------------------------------------------------------
  public String getFaxNumber(AppContext anAppContext) { return faxNumber; }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Get the e-mail address
   * 
   * @param	anAppContext	Application Context
   * @return emailAddr	The e-mail address
   */
  //-----------------------------------------------------------------------------
  public String getEmailAddr(AppContext anAppContext) { return emailAddr; }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Get the location
   * 
   * @param	anAppContext	Application Context
   * @return location	The location
   */
  //-----------------------------------------------------------------------------
  public String getLocation(AppContext anAppContext) { return location; }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Get the user id
   * 
   * @param	anAppContext	Application Context
   * @return userid	The user id
   */
  //-----------------------------------------------------------------------------
  public String getUserid(AppContext anAppContext) { return userid; }
  
  
  //Member Functions
  //-----------------------------------------------------------------------------
  /**
   * Get the object as a String
   * 
   * @param	anAppContext	Application Context
   * @return The object as a String
   */
  //-----------------------------------------------------------------------------
  public String asString(AppContext anAppContext) throws IcofException {
    return("Name: "          + getName(anAppContext) + "\n" +
           "AddressLine1: "  + getAddressLine1(anAppContext) + "\n" +
           "AddressLine2: "  + getAddressLine2(anAppContext) + "\n" +
           "City: "          + getCity(anAppContext) + "\n"  +
           "State: "         + getState(anAppContext) + "\n" +
           "Zip: "           + getZip(anAppContext) + "\n" +
           "Phone Number: "  + getPhoneNumber(anAppContext) + "\n" +
           "Fax Number: "    + getFaxNumber(anAppContext) + "\n" +
           "Email addr: "    + getEmailAddr(anAppContext) + "\n" +
           "Userid: "        + getUserid(anAppContext) + "\n" +
           "Location: "      + getLocation(anAppContext));
  }
  
  
}
