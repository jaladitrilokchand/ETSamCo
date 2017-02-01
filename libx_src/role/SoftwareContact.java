/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2006 - 2011 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: SoftwareContact.java
*
* CREATOR: Keith Loring
*    DEPT: AW0V
*    DATE: 09/04/2006
*
*-PURPOSE---------------------------------------------------------------------
* SoftwareContact class definition file.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 09/04/2006 KPL  Initial coding.
* 04/19/2007 KKW  Fixed constructor to initialize the vector of emailAddresses
* 02/21/2011 KKW  Updated to use validateAddresses method in IcofEmailUtil.
*                 Fixed java 1.5 collection warnings.  Corrected some javadoc
*=============================================================================
* </pre>
*/

package com.ibm.stg.iipmds.icof.component.role;

import java.util.Vector;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.mom.IcofObject;
import com.ibm.stg.iipmds.icof.component.util.IcofEmailUtil;

public class SoftwareContact extends IcofObject {

  //-----------------------------------------------------------------------------
  /**
   * Construct a SoftwareContact 
   *
   * @param  anAppContext     the Application Context
   * @param  appName          the Application name
   * @param  emailAddressList the String of email addresses for the application
   *
   */
  //-----------------------------------------------------------------------------
  public SoftwareContact(AppContext anAppContext
                         ,String appName
                         ,String emailAddressList) 
      throws IcofException {
  	
  	super(anAppContext);
  	setAppName(anAppContext, appName);
    setEmailAddresses(anAppContext, new Vector<String>());
  	parseEmailAddressString(anAppContext, emailAddressList);
  	setMapKey(anAppContext);
  }

	  
  //-----------------------------------------------------------------------------
  /**
   * Construct a SoftwareContact 
   *
   * @param  anAppContext     the Application Context
   * @param  appName          the Application name
   * @param  emailAddressList the Vector of email addresses for the application
   *
   */
  //-----------------------------------------------------------------------------
  public SoftwareContact(AppContext anAppContext
                         ,String appName
                         ,Vector<String> emailAddressList) 
    throws IcofException {
  	
  	super(anAppContext);
  	setAppName(anAppContext, appName);
  	setEmailAddresses(anAppContext, emailAddressList);
  	setMapKey(anAppContext);
  }

	  
  //-----------------------------------------------------------------------------
  /**
   * Get the SoftwareContact functionName attibute
   *
   * @param  anAppContext         the Application Context
   *
   * @return                      the name of the application
   */
  //-----------------------------------------------------------------------------
  public String getAppName(AppContext anAppContext) {
    return appName;
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Get the emailAddresses
   *
   * @param  anAppContext         the Application Context
   *
   * @return                      the vector of email addresses
   */
  //-----------------------------------------------------------------------------
  public Vector<String> getEmailAddresses(AppContext anAppContext) {
    return emailAddresses;
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Get the emailAddresses
   *
   * @param  anAppContext         the Application Context
   *
   * @return                      a comma separated String of all email addresses
   *                              from the Vector attribute
   */
  //-----------------------------------------------------------------------------
  public String getEmailAddressString(AppContext anAppContext) {
  	
    return IcofCollectionsUtil.getVectorAsString(emailAddresses
    		                                         ,Constants.COMMA);
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Return the content of this SoftwareContact as a String
   *
   * @param  anAppContext         the Application Context
   *
   * @return                      the content of the SoftwareConact as a String.
   */
  //-----------------------------------------------------------------------------
  public String asString(AppContext anAppContext) throws IcofException {

    return("AppName:        "  + getAppName(anAppContext) + ";" +
	       "emailAddresses: "  + getEmailAddresses(anAppContext));
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Parse the String of comma separated email addresses adding each to the 
   *   Vector of email adresses
   *
   * @param  anAppContext         the Application Context
   *
   * @return                      the contrent of the SoftwareConact as a String.
   */
  //-----------------------------------------------------------------------------
  public void parseEmailAddressString(AppContext anAppContext
  		                              ,String addresses) 
      throws IcofException {
  	
    IcofCollectionsUtil.parseString(addresses
                                    ,Constants.COMMA
			                              ,emailAddresses
                                    ,false);
  	
  }
  

  //-----------------------------------------------------------------------------
  // Data members
  //-----------------------------------------------------------------------------
  private String appName;
  private Vector<String> emailAddresses;


  //-----------------------------------------------------------------------------
  /**
   * Set the mapKey
   *
   * @param  anAppContext         the Application Context
   * @param  anAppName            the appName 
   *
   */
  //-----------------------------------------------------------------------------
  private void setMapKey(AppContext anAppContext) 
  { 
    setMapKey(anAppContext, getAppName(anAppContext)); 
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Set the appName
   *
   * @param  anAppContext         the Application Context
   * @param  anAppName            the appName 
   *
   */
  //-----------------------------------------------------------------------------
  public void setAppName(AppContext anAppContext, String anAppName) {
    appName = anAppName;
  }

  
  //-----------------------------------------------------------------------------
  /**
   * Set the functionName
   *
   * @param  anAppContext         the Application Context
   * @param  aFunctionName        the name 
   */
  //-----------------------------------------------------------------------------
  public void setFunctionName(AppContext anAppContext, String aFunctionName) {
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Set the emailAddresses (Vector of Strings)
   *
   * @param  anAppContext         the Application Context
   * @param  emailAddressList     the vector of email addresses 
   */
  //-----------------------------------------------------------------------------
  public void setEmailAddresses(AppContext anAppContext, Vector<String> emailAddressList) 
    throws IcofException {
    
    emailAddresses = emailAddressList;
    validateAddresses(anAppContext);
    
  }


  //-----------------------------------------------------------------------------
  /**
   * Append an email Address the the emailAddresses Vector 
   *
   * @param  anAppContext         the Application Context
   * @param  address              the email address to add 
   */
  //-----------------------------------------------------------------------------
  public void appendAddress(AppContext anAppContext, String address) 
    throws IcofException {

    if (!emailAddresses.contains(address)) {
    	emailAddresses.add(address);
    }
    validateAddresses(anAppContext);
    
  }
  
  
  //-----------------------------------------------------------------------------
  /**
   * Validates the Vector of email addresses 
   *
   * @param  anAppContext         the Application Context
   */
  //-----------------------------------------------------------------------------
  private void validateAddresses(AppContext anAppContext) 
    throws IcofException {

    IcofEmailUtil.validateAddresses(anAppContext, getEmailAddresses(anAppContext));
  }  
}
