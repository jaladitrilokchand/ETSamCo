/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2001 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: IcofEmail.java
*
* CREATOR: Aydin Suren (asuren)
*    DEPT: 5ZIA
*    DATE: 11/21/2001
*
*-PURPOSE---------------------------------------------------------------------
* Send e-mail
* This class uses the following jar files:
* - activation.jar
* - mail.jar
* - mailapi.jar
* - smtp.jar
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 11/21/2001 AS  Initial coding.
* 02/12/2002 AS  Updated mailTo & mailCC to accept comma delimeted email
*                addresses.
* 03/20/2002 KK  Converted to Java 1.2.2.
* 05/25/2005 KKW  Added "implements Serializable".
* 12/15/2005 KKW  Modified due to splitting of Constants.java into several
*                 *Util classes.
* 12/19/2005 KKW  Added MAIL_HOST constant from the old Constants class.
* 07/31/2006 KPL  Reworked package and import delarations, added javadoc,
*                 tweaked some parameter names for readability, added 
*                 AppContext as first param throughout, added session logging
* 05/31/2007 KKW  Ensured the result of all trim() functions is assigned
*                 to a variable, as appropriate 
* 11/16/2009 KKW  Added constant for BTV_SMTP_SERVER                
*=============================================================================
* </pre>
*/

package com.ibm.stg.iipmds.icof.component.util;
import java.io.Serializable;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.ibm.stg.iipmds.common.Constants;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.icof.component.mom.AppContext;
import com.ibm.stg.iipmds.icof.component.util.IcofEmailUtil;

public class IcofEmail implements Serializable {

    public static final String MAIL_HOST = "mail.smtp.host";
    public static final String BTV_SMTP_SERVER = "mailrelay.btv.ibm.com";

    //-----------------------------------------------------------------------------
    /**
     * Constructor - used to instantiate object from an application
     *               where only server name and mail header are known.
     *
     * @param  server             the name of the email server.
     * @param  titleHeader        the email header.
     * @exception                 IcofException unable to set server or header.
     */
    //-----------------------------------------------------------------------------
    public IcofEmail(AppContext anAppContext, String server, String titleHeader) 
        throws IcofException {

        //
        // Initialize Outgoing SMTP mail server
        //
        setSmtpServer(anAppContext, server);
        setHeader(anAppContext, titleHeader);
    }

    //-----------------------------------------------------------------------------
    /**
     * Constructor - used to instantiate object from an application when
     *               all the fields are known. This contructor will
     *               automatically set them.
     *
     * @param  server             the name of the email server.
     * @param  titleHeader        the email header.
     * @param  mailTo             To (comma delimited) list of email addresses to 
     *                            recieve the email  
     * @param  mailFrom           From email addresses from which the email shall
     *                            appear to have been sent.
     * @param  mailCC             carbon copy (comma delimited) list of other email 
     *                            addresses to recieve the email. If there is no 
     *                            mailCC, pass "" for it, otherwise it will be 
     *                            validated and fail.
     * @param  subject            Subject line of the email.
     * @param  body               Body (content) of the email.
     * @exception                 IcofException unable to set server or header.
     */
    //-----------------------------------------------------------------------------
    public IcofEmail(AppContext anAppContext
    		         ,String server
                     ,String titleHeader
                     ,String mailTo
                     ,String mailFrom
                     ,String mailCC
                     ,String subject
                     ,String body) throws IcofException {

        setSmtpServer(anAppContext, server);
        setHeader(anAppContext, titleHeader);
        setMailFrom(anAppContext, mailFrom);
        setSubject(anAppContext, subject);
        setBody(anAppContext, body);

        //
        // Parse mailTo and mailCC into vectors first,
        //  then call setMailTo & setMailCC.
        //
        parseMailToNames(anAppContext, mailTo);
        parseMailCCNames(anAppContext, mailCC);

        setMailTo(anAppContext);
        setMailCC(anAppContext);

    }

    //-----------------------------------------------------------------------------
    /**
     * Sends the email.
     *
     * @exception  IcofException  unable to send email.
     */
    //-----------------------------------------------------------------------------
    public void send(AppContext anAppContext) throws IcofException {

        try{
            Properties props = System.getProperties();

            props.put(IcofEmail.MAIL_HOST, getSmtpServer(anAppContext));
            Session session = Session.getDefaultInstance(props, null);

            //
            // Create a new (empty) message
            //
            Message msg = new MimeMessage(session);

            //
            // Set the FROM field
            //
            msg.setFrom(new InternetAddress(mailFrom));

            //
            // Set the TO recipients
            // (This method accepts comma delimited email addreses)
            //
            msg.setRecipients(Message.RecipientType.TO,
                              InternetAddress.parse(mailTo, false));

            //
            // Include CC recipients if there is any
            // (This method accepts comma delimited email addreses)
            //
            if (mailCC != null)
                msg.setRecipients(Message.RecipientType.CC
                                  ,InternetAddress.parse(mailCC, false));

            //
            // Set the subject and body text
            //
            msg.setSubject(subject);
            msg.setText(body);

            //
            // Set some other header information
            //
            msg.setHeader("X-Mailer", getHeader(anAppContext));
            msg.setSentDate(new Date());

            //
            // Send the message
            //
            Transport.send(msg);
        }
        catch (Exception e) {
            String funcName = new String("send(AppContext)");

            IcofException ie = new IcofException(this.getClass().getName()
                                                 ,funcName
                                                 ,IcofException.SEVERE
                                                 ,e.toString()
                                                 ,"");
            anAppContext.getSessionLog().log(ie);
            throw ie;
        }

    }

    //-----------------------------------------------------------------------------
    /**
     * Sets the "To:" list of email addresses.
     *
     * @exception  IcofException  Invalid email address in "To:" list.
     */
    //-----------------------------------------------------------------------------
    public void setMailTo(AppContext anAppContext) throws IcofException {

        //
        // Validate each email address in the mailToNames vector
        //
        for (int i = 0; i < mailToNames.size(); i++) {
            validateEmailAddress(anAppContext, (String)mailToNames.elementAt(i));
        }

        //
        // Assign the comma delimited values of mailToNames to mailTo
        //
        mailTo = IcofCollectionsUtil.getVectorAsString(mailToNames, Constants.COMMA);
    }

    //-----------------------------------------------------------------------------
    /**
     * Sets the "From:" email address.
     *
     * @param      fromAddress    The address the email is to be sent from.
     * @exception  IcofException  Invalid email address in "From:".
     */
    //-----------------------------------------------------------------------------
    public void setMailFrom(AppContext anAppContext, String fromAddress) throws IcofException {

        mailFrom = validateEmailAddress(anAppContext, fromAddress);
    }

    //-----------------------------------------------------------------------------
    /**
     * Sets the "CC:" (carbon copy) list of email addresses.
     *
     * @exception  IcofException  Invalid email address in "CC:" list.
     */
    //-----------------------------------------------------------------------------
    public void setMailCC(AppContext anAppContext) throws IcofException {

        //
        // Validate each email address in the mailCCNames vector
        //
        for (int i = 0; i < mailCCNames.size(); i++) {
            validateEmailAddress(anAppContext, (String)mailCCNames.elementAt(i));
        }

        //
        // Assign the comma delimited values of mailCCNames to mailCC.
        // If "" was passes in constructor, it will generate an empty
        //   vector and the following call will return "" for an empty
        //   vector.
        //
        mailCC = IcofCollectionsUtil.getVectorAsString(mailCCNames, Constants.COMMA);

    }

    //-----------------------------------------------------------------------------
    /**
     * Sets the subject of the email
     *
     * @param      aSubject       the subject of the email
     * @exception  IcofException  aSubject string is null
     */
    //-----------------------------------------------------------------------------
    public void setSubject(AppContext anAppContext, String aSubject) 
        throws IcofException {

        if (aSubject == null) {
            String funcName = new String("setSubject(String aSubject)");
            String msg = new String("Subject can not be empty");

            IcofException ie = new IcofException(this.getClass().getName()
                                                 ,funcName
                                                 ,IcofException.SEVERE
                                                 ,msg
                                                 ,"");
            anAppContext.getSessionLog().log(ie);
            throw ie;
        }

        subject = aSubject;
    }

    //-----------------------------------------------------------------------------
    /**
     * Sets the body of the email
     *
     * @param      aBody          the body of the email
     * @exception  IcofException  aBody string is null
     */
    //-----------------------------------------------------------------------------
    public void setBody(AppContext anAppContext, String aBody) throws IcofException {

        if (aBody == null) {
            String funcName = new String("setBody(String aBody)");
            String msg = new String("E-Mail can not be empty");

            IcofException ie = new IcofException(this.getClass().getName()
                                                 ,funcName
                                                 ,IcofException.SEVERE
                                                 ,msg
                                                 ,"");
            anAppContext.getSessionLog().log(ie);
            throw ie;
        }

        body = aBody;
    }

    //-----------------------------------------------------------------------------
    /**
     * Gets the "To:" list of email addresses
     *
     * @return                    comma delimited String of "To:" email addresses
     * @exception  IcofException  an error occured
     */
    //-----------------------------------------------------------------------------
    public String getMailTo(AppContext anAppContext) throws IcofException {

        return mailTo;
    }

    //-----------------------------------------------------------------------------
    /**
     * Gets the "From:" email address
     *
     * @return                    a "From:" email address
     * @exception  IcofException  an error occured
     */
    //-----------------------------------------------------------------------------
    public String getMailFrom(AppContext anAppContext) throws IcofException {

        return mailFrom;
    }

    //-----------------------------------------------------------------------------
    /**
     * Gets the "CC:" list of email addresses
     *
     * @return                    comma delimited String of "CC:" email addresses
     * @exception  IcofException  an error occured
     */
    //-----------------------------------------------------------------------------
    public String getMailCC(AppContext anAppContext) throws IcofException {

        if (mailCC == null)
            mailCC = "";

        return mailCC;
    }

    //-----------------------------------------------------------------------------
    /**
     * Gets the subject of the email
     *
     * @return                    the email subject
     * @exception  IcofException  an error occured
     */
    //-----------------------------------------------------------------------------
    public String getSubject(AppContext anAppContext) throws IcofException {

        if (subject == null)
            subject = "";

        return subject;
    }

    //-----------------------------------------------------------------------------
    /**
     * Gets the body (content) of the email
     *
     * @return                    the email body
     * @exception  IcofException  an error occured
     */
    //-----------------------------------------------------------------------------
    public String getBody(AppContext anAppContext) throws IcofException {

        if (body == null)
            body = "";

        return body;
    }

    //-----------------------------------------------------------------------------
    /**
     * Parses the mailToNameString as comma delimited list of email addresses
     * and stores the result in a Vector data member of the IcofEmail object
     *
     * @param      mailToNameString     comma delimited string of email addresses
     * @exception  IcofException        error parsing adresses or empty string
     */
    //-----------------------------------------------------------------------------
    public void parseMailToNames(AppContext anAppContext, String mailToNameString)
        throws IcofException {

        String funcName = new String("parseMailToNames(String)");

        IcofCollectionsUtil.parseString(mailToNameString
                                        ,Constants.COMMA
                                        ,mailToNames
                                        ,false);
        if ((mailToNames.isEmpty()) &&
            (!mailToNameString.equals(""))) {
            IcofException ie = new IcofException(this.getClass().getName()
                                                 ,funcName
                                                 ,IcofException.SEVERE
                                                 ,"Error parsing MailTo names"
                                                 ,"");
            anAppContext.getSessionLog().log(ie);
            throw(ie);
        }

    }

    //-----------------------------------------------------------------------------
    /**
     * Parses the mailCCNameString as comma delimited list of email addresses
     * and stores the result in a Vector data member of the IcofEmail object
     *
     * @param      mailCCNameString     comma delimited string of email addresses
     * @exception  IcofException        error parsing adresses or empty string
     */
    //-----------------------------------------------------------------------------
    public void parseMailCCNames(AppContext anAppContext, String mailCCNameString)
        throws IcofException {

        String funcName = new String("parseMailCCNames(AppContext, String)");

        IcofCollectionsUtil.parseString(mailCCNameString
                                        ,Constants.COMMA
                                        ,mailCCNames
                                        ,false);
        if ((mailCCNames.isEmpty()) &&
            (!mailCCNameString.equals(""))) {
            IcofException ie = new IcofException(this.getClass().getName()
                                                 ,funcName
                                                 ,IcofException.SEVERE
                                                 ,"Error parsing MailCC names"
                                                 ,"");
            anAppContext.getSessionLog().log(ie);
            throw(ie);
        }

    }

    //-----------------------------------------------------------------------------
    /**
     * Parses the aMailTo as comma delimited list of email addresses and appends
     * and appends the result to the Vector data member of the IcofEmail object
     *
     * @param      aMailTo              comma delimited string of email addresses
     * @exception  IcofException        error parsing adresses or empty string
     */
    //-----------------------------------------------------------------------------
    public void appendMailTo(AppContext anAppContext, String aMailTo) throws IcofException {

        if (!mailToNames.contains(aMailTo)) {

            //
            // Parse the given string and add into mailToNames vector
            //
            parseMailToNames(anAppContext, aMailTo);

            //
            // Call setMailTo
            //
            setMailTo(anAppContext);
        }

    }

    //-----------------------------------------------------------------------------
    /**
     * Parses the aMailCC as comma delimited list of email addresses and appends
     * and appends the result to the Vector data member of the IcofEmail object
     *
     * @param      aMailCC              comma delimited string of email addresses
     * @exception  IcofException        error parsing adresses or empty string
     */
    //-----------------------------------------------------------------------------
    public void appendMailCC(AppContext anAppContext, String aMailCC) 
        throws IcofException {

        if (!mailCCNames.contains(aMailCC)) {

            //
            // Parse the given string and add into mailCCNames vector
            //
            parseMailCCNames(anAppContext, aMailCC);

            //
            // Call setMailCC
            //
            setMailCC(anAppContext);
        }

    }


    //----------------------------------------------------------------------
    // Private members
    //----------------------------------------------------------------------
    private String smtpServer;
    private String header;
    private String mailTo;
    private String mailFrom;
    private String mailCC;
    private String subject;
    private String body;

    private Vector mailToNames = new Vector();
    private Vector mailCCNames = new Vector();

    //-----------------------------------------------------------------------------
    /**
     * Sets the SMTP server.
     *
     * @param      aServer        the name of the SMTP server
     */
    //-----------------------------------------------------------------------------
    private void setSmtpServer(AppContext anAppContext, String aServer) {
        smtpServer = aServer;
    }

    //-----------------------------------------------------------------------------
    /**
     * Sets the email header.
     *
     * @param      aHeader        the email header
     */
    //-----------------------------------------------------------------------------
    private void setHeader(AppContext anAppContext, String aHeader) {
        header = aHeader;
    }

    //-----------------------------------------------------------------------------
    /**
     * Gets the SMTP server name.
     *
     * @return                    The name of the SMTP server as a String.
     */
    //-----------------------------------------------------------------------------
    private String getSmtpServer(AppContext anAppContext) {
        return smtpServer;
    }

    //-----------------------------------------------------------------------------
    /**
     * Gets the email header.
     *
     * @return                    The email header as a String.
     */
    //-----------------------------------------------------------------------------
    private String getHeader(AppContext anAppContext) {
        return header;
    }

    //-----------------------------------------------------------------------------
    /**
     * Validates a single email address.
     *
     * @param      address        An email address
     * @return                    Trimmed, validated email address as a String.
     * @exception  IcofException  Invalid email address
     */
    //-----------------------------------------------------------------------------
    private String validateEmailAddress(AppContext anAppContext
    		                                ,String address) throws IcofException {

        address = address.trim();

        if (! IcofEmailUtil.isValidEmailAddress(address)) {

            String funcName = new String("validateEmailAddress(String address)");
            String msg = new String("Invalid E-mail address");

            IcofException ie = new IcofException(this.getClass().getName()
                                                 ,funcName
                                                 ,IcofException.SEVERE
                                                 ,msg
                                                 ,"");

            anAppContext.getSessionLog().log(ie);
            throw ie;
        }

        return address;
    }
}
