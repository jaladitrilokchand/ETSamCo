/**
 * <pre>
 * =============================================================================
 *
 *  IBM Internal Use Only
 *
 * =============================================================================
 *
 *  CREATOR: Aydin Suren
 *     DEPT: AW0V
 *     DATE: 03/01/2008
 *     
 *-PURPOSE----------------------------------------------------------------------------------------
 * Utility class.
 *------------------------------------------------------------------------------------------------------
 *
 * =============================================================================
 *
 * -CHANGE LOG------------------------------------------------------------------
 * 03/01/2008 AS  Initial coding.
 *
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.clearquest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.rational.wvcm.stp.cq.CqQuery.Filter.Operation;

public class cqUtil {

    /** W3 schema location. */
    public static final String sXML_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

    /** Schema locations. */
    public static final String sCREATE_XSD = "/com/ibm/stg/iipmds/icof/component/clearquest/cqCreate.xsd";
    public static final String sUPDATE_XSD = "/com/ibm/stg/iipmds/icof/component/clearquest/cqUpdate.xsd";
    public static final String sQUERY_XSD = "/com/ibm/stg/iipmds/icof/component/clearquest/cqQuery.xsd";
    public static final String sLIST_DBS_XSD = "/com/ibm/stg/iipmds/icof/component/clearquest/cqListDBs.xsd";
    public static final String sFETCH_XSD = "/com/ibm/stg/iipmds/icof/component/clearquest/cqFetch.xsd";

    /** Example XML input files for unit testing. */
    public static final String sCREATE_XML = "/com/ibm/stg/iipmds/icof/component/clearquest/cqCreateExample.xml";
    public static final String sCREATE_XML2 = "/com/ibm/stg/iipmds/icof/component/clearquest/cqCreateExample2.xml";
    public static final String sQUERY_XML = "/com/ibm/stg/iipmds/icof/component/clearquest/cqQueryExample.xml";
    public static final String sQUERY_XML2 = "/com/ibm/stg/iipmds/icof/component/clearquest/cqQueryExample2.xml";
    public static final String sQUERY_XML3 = "/com/ibm/stg/iipmds/icof/component/clearquest/cqQueryExample3.xml";
    public static final String sLIST_DBS_XML = "/com/ibm/stg/iipmds/icof/component/clearquest/cqListDBsExample.xml";
    public static final String sFETCH_XML = "/com/ibm/stg/iipmds/icof/component/clearquest/cqFetchExample.xml";
    public static final String sUPDATE_XML = "/com/ibm/stg/iipmds/icof/component/clearquest/cqUpdateExample1.xml";
    public static final String sUPDATE_XML2 = "/com/ibm/stg/iipmds/icof/component/clearquest/cqUpdateExample2.xml";
    public static final String sUPDATE_XML3 = "/com/ibm/stg/iipmds/icof/component/clearquest/cqUpdateExample3.xml";

    /** Valid filter operation constants. */
    public static final String sEQUALS = "Equals";
    public static final String sDOES_NOT_EQUAL = "Does Not Equal";
    public static final String sLESS_THAN = "Less Than";
    public static final String sLESS_THANOrEqualTo = "Less Than Or Equal To";
    public static final String sGREATER_THAN = "Greater Than";
    public static final String sGREATER_THAN_OR_EQUAL_TO = "Greater Than Or Equal To";
    public static final String sCONTAINS = "Contains";
    public static final String sDOES_NOT_CONTAIN = "Does Not Contain";
    public static final String sIS_NULL = "Is Null";
    public static final String sIS_NOT_NULL = "Is Not Null";
    public static final String sIN = "In";
    public static final String sNOT_IN = "Not In";
    public static final String sNOT_BETWEEN = "Not Between";
    public static final String sBETWEEN = "Between";

    /** XML tags */
    public static final String LOGIN = "login";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String DATABASE = "database";
    public static final String SCHEMA = "schema";
    public static final String CLIENT = "client";
    public static final String APP_NAME = "app-name";
    public static final String RECORD = "record";
    public static final String TITLE = "title";
    public static final String CUSTOMER_ID = "customerid";
    public static final String PROBLEM_CLASS = "problem_class";
    public static final String SEVERITY = "severity";
    public static final String TECH = "technology";
    public static final String LIB_VERSION = "library_version";
    public static final String OS = "operating_system";
    public static final String PROB_DESC = "problem_description";
    public static final String PROB_TYPE = "problem_type";
    public static final String SUB_A = "subtype_a";
    public static final String SUB_B = "subtype_b";
    public static final String SUB_C = "subtype_c";
    public static final String SUB_D = "subtype_d";
    public static final String SUBMITTER_FULLNAME = "submitter_fullname";
    public static final String RECORD_TYPE = "record_type";
    public static final String RECORD_ID = "record_id";
    public static final String ACTION = "action";
    public static final String DEV_ONLY = "development_only";
    public static final String NULL = "null";
    public static final String ID = "id";
    public static final String RESULT = "result";
    public static final String RESULTS = "results";
    public static final String NAME = "name";
    public static final String FIELD = "field";
    public static final String FIELDS = "fields";
    public static final String CQDB = "cqDB";
    public static final String CQDBS = "cqDBs";
    public static final String ERROR = "error";
    public static final String DESCRIPTION = "description";
    public static final String QUERY = "query";
    public static final String OPERATOR = "operator";
    public static final String VALUE = "value";
    public static final String QUERY_NAME = "query-name";
    public static final String FILTER = "filter";
    public static final String FILTERS = "filters";
    public static final String QUERIES = "queries";
    public static final String SYS_CQ_SERVICE = "sys_cq_service";
    public static final String SERVICE_REQUESTER_NAME = "service_requester_name";
    public static final String USERLOGIN = "userlogin";
    public static final String BEGIN_DATE = "begin_date";
    public static final String END_DATE = "end_date";
    public static final String CREATE = "create";
    public static final String UPDATE = "update";

    /** Valid services. */
    public static final String sCREATE_PROBLEM_SERVICE = "create-Problem";
    public static final String sFETCH_PROBLEM_SERVICE = "fetch-Problem";
    public static final String sUPDATE_PROBLEM_SERVICE = "update-Problem";
    public static final String sUPDATE_TK_PATCH_SERVICE = "update-tk_patch";
    public static final String sUPDATE_TK_INJREQ_SERVICE = "update-tk_injectionrequest";
    public static final String sLIST_SERVICE = "listDBs";
    public static final String sQUERY_SERVICE = "query";
    public static final String sCREATE_DIP_REVISION_SERVICE = "create-dip_revision";
    public static final String sUPDATE_DIP_REVISION_SERVICE = "update-dip_revision";
    public static final String[] saSERVICES = { sCREATE_PROBLEM_SERVICE,
	    sFETCH_PROBLEM_SERVICE, sUPDATE_PROBLEM_SERVICE,
	    sUPDATE_TK_PATCH_SERVICE, sUPDATE_TK_INJREQ_SERVICE, sLIST_SERVICE,
	    sQUERY_SERVICE, sCREATE_DIP_REVISION_SERVICE, sUPDATE_DIP_REVISION_SERVICE};

    /** Output parse error message. */
    protected static final String OUTPUT_PARSE_ERROR_MSG = "Cannot parse the output since it has an error. Error = ";
    /** Date formats. */
    protected static final String yyyyMMddhhmmssaazz = "EEEE yyyy/MM/dd hh:mm:ss aa zz";
    protected static final String sUSAGE_FILE_SUFFIX = "yyyyMM";

    /**
     * Replaces XML reserved chars from a given string.
     * 
     * @param sInput
     *            Value
     * @return String New string
     * 
     */
    public static String filterXMLReservedChars(String sInput) throws Exception {

	if (isEmpty(sInput)) {
	    return sInput;
	}

	// Replace the "&" first so that it won't be affected by
	// others.
	sInput = sInput.replace("&", "&amp;");
	sInput = sInput.replace("<", "&lt;");
	sInput = sInput.replace(">", "&gt;");
	sInput = sInput.replace("\'", "&apos;");
	sInput = sInput.replace("\"", "&quot;");

	return sInput;
    }

    /**
     * Formats a given java.util.Date object to given format.
     * 
     * @param dtDate
     *            java.util.Date to format.
     * @param sFormat
     *            Format to apply.
     * @return Formatted date as String.
     * @throws Exception
     */
    public static String formatDate(Date dtDate, String sFormat)
	    throws Exception {

	SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
	sdf.setTimeZone(java.util.TimeZone.getDefault());

	return sdf.format(dtDate);
    }

    /**
     * Reads all the given attributes values (saAttribute) of a given tag
     * (sTagName) in a given element (ele) structure.
     * 
     * <pre>
     *             Ex:
     *               &lt;result&gt;
     *               &lt;field name=&quot;dbid&quot;&gt;123456&lt;/field&gt;
     *               &lt;field name=&quot;Headline&quot;&gt;Example&lt;/field&gt;
     *               &lt;field name=&quot;id&quot;&gt;ASPRD00000001&lt;/field&gt;
     *               &lt;field name=&quot;owner_fullname&quot;&gt;Lastname, Firstname&lt;/field&gt;
     *               &lt;field name=&quot;requestor_fullname&quot;&gt;Lastname, Firstname&lt;/field&gt;
     *               &lt;field name=&quot;analyst_fullname&quot;&gt;Lastname, Firstname&lt;/field&gt;
     *              &lt;/result&gt;
     * </pre>
     * 
     * @param ele
     *            The element structure to parse.
     * @param sTagName
     *            The tag names to look for.
     * @param saAttribute
     *            The list of the attributes to search in the given tag.
     * @return
     * @throws Exception
     */
    public static Vector getAttributesAsText(Element ele, String sTagName,
	    String[] saAttribute) throws Exception {

	String[] saValue = null;
	NodeList nl = null;
	Vector vcReturn = null;
	int iAttributeSize = 0;

	if (ele != null) {
	    nl = ele.getElementsByTagName(sTagName);
	    iAttributeSize = saAttribute.length;
	    if (nl != null && nl.getLength() > 0) {
		vcReturn = new Vector();
		for (int i = 0; i < nl.getLength(); i++) {
		    Element el = (Element) nl.item(i);
		    saValue = new String[iAttributeSize];
		    for (int j = 0; j < saAttribute.length; j++) {
			saValue[j] = el.getAttribute(saAttribute[j]);
		    }
		    vcReturn.add(saValue);
		}
	    }
	}

	return vcReturn;
    }

    /**
     * Returns the data in given format.
     * 
     * @param sFormat
     *            Example: "EEEE yyyy/MM/dd hh:mm:ss aa zz"
     * @return Formatted current date as String.
     * @throws Exception
     */
    public static String getDate(String sFormat) throws Exception {
	Date dtNow = new Date();
	return formatDate(dtNow, sFormat);
    }

    /**
     * Reads a given file and returns its content as StringBuffer.
     * 
     * @param sFile
     *            Fully qualified filename.
     * @return
     * @throws Exception
     */
    public static StringBuffer getFileContent(String sFile) throws Exception {

	StringBuffer sbReturn = new StringBuffer();

	FileReader input = new FileReader(sFile);
	BufferedReader bufRead = new BufferedReader(input);

	String sLine = bufRead.readLine();
	while (sLine != null) {
	    sbReturn.append(sLine);
	    sLine = bufRead.readLine();
	}
	bufRead.close();

	return sbReturn;
    }

    /**
     * Reads a given URL and returns its content as StringBuffer.
     * 
     * @param urlFile
     *            URL to read.
     * @return
     * @throws Exception
     */
    public static StringBuffer getFileContent(URL urlFile) throws Exception {

	StringBuffer sbReturn = new StringBuffer();

	InputStream isInput = urlFile.openStream();
	BufferedReader bufRead = new BufferedReader(new InputStreamReader(
		isInput));

	String sLine = bufRead.readLine();
	while (sLine != null) {
	    sbReturn.append(sLine);
	    sLine = bufRead.readLine();
	}
	bufRead.close();

	return sbReturn;
    }

    /**
     * Parses the given command line arguments into hash key/value pairs. For
     * example, if "... -a -b something" will return ("-a", null) and ("-b",
     * "something").
     * 
     * @param args
     *            Arguments to process
     * @return The key/value pairs of the arguments.
     * @throws Exception
     *             Problem parsing the arguments
     */
    public static HashMap getHashArguments(String[] args) throws Exception {
	HashMap hmValues = new HashMap();

	String sArg = "<init>";
	String sLastArg = null;

	try {
	    // Iterate thru the arguments.
	    if (args != null) {
		for (int i = 0; i < args.length; i++) {
		    sArg = args[i];

		    if (sLastArg != null) {

			if (sArg.startsWith("-")) {
			    hmValues.put(sLastArg, null);
			    sLastArg = sArg;
			} else {
			    hmValues.put(sLastArg, sArg);
			    sLastArg = null;
			}
		    } else {
			sLastArg = sArg;
		    }
		}
	    }

	    if (sLastArg != null) {
		hmValues.put(sLastArg, null);
	    }

	} catch (Exception e) {
	    throw new Exception("Failed to parse the arguments.\n" + "args="
		    + args + "\nsArg=" + sArg + "\nsLastArg=" + sLastArg);
	} finally {
	}

	return hmValues;
    }

    /**
     * Converts a given operation name to Rational Operation team API object.
     * 
     * @param sUserDefinedOperation
     * @return
     * @throws Exception
     */
    public static Operation getTeamOperation(String sUserDefinedOperation)
	    throws Exception {

	Operation teamOperation = null;

	if (isEmpty(sUserDefinedOperation)) {
	    throw new Exception("User defined operation (\""
		    + sUserDefinedOperation + "\") can not be null or empty.");
	} else if (sUserDefinedOperation.equalsIgnoreCase(sEQUALS)) {
	    teamOperation = Operation.IS_EQUAL;
	} else if (sUserDefinedOperation.equalsIgnoreCase(sDOES_NOT_EQUAL)) {
	    teamOperation = Operation.IS_NOT_EQUAL;
	} else if (sUserDefinedOperation.equalsIgnoreCase(sLESS_THAN)) {
	    teamOperation = Operation.IS_LESS_THAN;
	} else if (sUserDefinedOperation.equalsIgnoreCase(sLESS_THANOrEqualTo)) {
	    teamOperation = Operation.IS_LESS_THAN_OR_EQUAL;
	} else if (sUserDefinedOperation.equalsIgnoreCase(sGREATER_THAN)) {
	    teamOperation = Operation.IS_GREATER_THAN;
	} else if (sUserDefinedOperation
		.equalsIgnoreCase(sGREATER_THAN_OR_EQUAL_TO)) {
	    teamOperation = Operation.IS_GREATER_THAN_OR_EQUAL;
	} else if (sUserDefinedOperation.equalsIgnoreCase(sCONTAINS)) {
	    teamOperation = Operation.HAS_SUBSTRING;
	} else if (sUserDefinedOperation.equalsIgnoreCase(sDOES_NOT_CONTAIN)) {
	    teamOperation = Operation.HAS_NO_SUBSTRING;
	} else if (sUserDefinedOperation.equalsIgnoreCase(sIS_NULL)) {
	    teamOperation = Operation.IS_NULL;
	} else if (sUserDefinedOperation.equalsIgnoreCase(sIS_NOT_NULL)) {
	    teamOperation = Operation.IS_NOT_NULL;
	} else if (sUserDefinedOperation.equalsIgnoreCase(sIN)) {
	    teamOperation = Operation.IS_IN_SET;
	} else if (sUserDefinedOperation.equalsIgnoreCase(sNOT_IN)) {
	    teamOperation = Operation.IS_NOT_IN_SET;
	} else if (sUserDefinedOperation.equalsIgnoreCase(sBETWEEN)) {
	    teamOperation = Operation.IS_BETWEEN;
	} else if (sUserDefinedOperation.equalsIgnoreCase(sNOT_BETWEEN)) {
	    teamOperation = Operation.IS_NOT_BETWEEN;
	} else {
	    throw new Exception(
		    "Can not translate the user defined operation (\""
			    + sUserDefinedOperation
			    + "\"). Uknown operation is used.");
	}

	return teamOperation;
    }

    /**
     * Returns the value of a given tag name as integer.
     * 
     * @param ele
     *            XML Element to operate on.
     * @param sTagName
     *            Tag name to read for its value.
     * @return
     * @throws Exception
     */
    public static int getValueAsInt(Element ele, String sTagName)
	    throws Exception {
	return Integer.parseInt(getValueAsText(ele, sTagName));
    }

    /**
     * @param ele
     *            An XML Element object.
     * @param sTagName
     * @return The Value. If the tag doesn't have any value, "null" will be
     *         returned.
     * @throws Exception
     * 
     *             Example input:
     * 
     *             <pre>
     *              &lt;query&gt;
     *                &lt;query-name&gt;Some text to fetch&lt;/query-name&gt;
     *             &lt;/query&gt;
     * </pre>
     */
    public static String getValueAsText(Element ele, String sTagName)
	    throws Exception {

	String sReturn = null;
	NodeList nl = ele.getElementsByTagName(sTagName);
	if (nl != null && nl.getLength() > 0) {
	    Element el = (Element) nl.item(0);
	    if (el == null) {
	    System.err.println("el is null in getValueAsText");
	    } else {
	    	Node node = el.getFirstChild();
	    	if (node != null) {
	    		sReturn = node.getNodeValue();
	    	}
	    }
	}

	return sReturn;
    }

    /**
     * Reads thru a given XML element to get the values of a given tag name.
     * 
     * @param ele
     * @param sTagName
     * @return
     * @throws Exception
     */
    public static Object[] getValuesAsText(Element ele, String sTagName)
	    throws Exception {

	Object[] oReturn = null;
	NodeList nl = ele.getElementsByTagName(sTagName);
	if (nl != null && nl.getLength() > 0) {
	    oReturn = new Object[nl.getLength()];
	    for (int i = 0; i < nl.getLength(); i++) {
		Element el = (Element) nl.item(i);
		Node node = el.getFirstChild();
		if (node != null) {
		    String textVal = node.getNodeValue();
		    oReturn[i] = textVal;
		}
	    }
	}

	return oReturn;
    }

    /**
     * Reads thru a given XML element to get the names and values of the child
     * nodes.
     * 
     * @param ele
     * @return
     * @throws Exception
     */
    public static Vector getNodeNameAndValues(Element ele) throws Exception {

	Vector vcReturn = null;

	NodeList nl = ele.getChildNodes();
	if (nl != null && nl.getLength() > 0) {
	    vcReturn = new Vector();
	    for (int i = 0; i < nl.getLength(); i++) {
		Node nNode = nl.item(i);

		// Only parse the nested element nodes.
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		    Node nText = nNode.getFirstChild();
		    String name = nNode.getNodeName();
		    String value = nText.getNodeValue();
		    vcReturn.add(new cqField(name, value));
		}
	    }
	}
	return vcReturn;
    }

    /**
     * Return a boolean indicating if the string is empty or null.
     * 
     * @return A boolean.
     */
    public static boolean isEmpty(String sValue) {
	return (sValue == null || (sValue.trim()).length() == 0);
    }

    /**
     * Validates that a service is a valid service.
     * 
     * @param sService
     *            Service name to validate.
     * @throws Exception
     */
    public static void validateService(String sService) throws Exception {

	Arrays.sort(cqUtil.saSERVICES);
	int iIndex = Arrays.binarySearch(cqUtil.saSERVICES, sService);
	if (iIndex < 0) {
	    String sValidServices = "";
	    for (int i = 0; i < cqUtil.saSERVICES.length; i++) {
		sValidServices += " \"" + cqUtil.saSERVICES[i] + "\"";
	    }
	    throw new Exception(
		    "\""
			    + sService
			    + "\" is not a valid service. Please use one of valid services:"
			    + sValidServices + " -- iIndex=" + iIndex);
	}
    }

    /**
     * Validates an XML content to a given schema.
     * 
     * @param source
     *            XML content.
     * @param sXSD
     *            Schema filename.
     * @throws Exception
     */
    public static void validateXML(Source source, String sXSD) throws Exception {

	// Lookup a factory for the W3C XML Schema language
	SchemaFactory factory = SchemaFactory.newInstance(sXML_LANGUAGE);

	// Compile the schema.
	Schema schema = factory.newSchema(cqUtil.class.getResource(sXSD));

	// Get a validator from the schema.
	Validator validator = schema.newValidator();

	// Validate the document.
	validator.validate(source);
	// System.out.println("The XML content/file is valid.");
    }

    /**
     * Validates an XML content to a given schema.
     * 
     * @param sXMLFilename
     *            XML content.
     * @param sXSD
     *            Schema filename.
     * @throws Exception
     */
    public static void validateXML(String sXMLFilename, String sXSD)
	    throws Exception {

	// Parse the document you want to check.
	Source source = new StreamSource(sXMLFilename);

	// Validate the document.
	validateXML(source, sXSD);
    }

    /**
     * Validates an XML content to a given schema.
     * 
     * @param sXMLContent
     *            XML content.
     * @param sXSD
     *            Schema filename.
     * @throws Exception
     */
    public static void validateXML(StringBuffer sXMLContent, String sXSD)
	    throws Exception {

	if (sXMLContent == null || sXMLContent.length() == 0) {
	    throw new Exception(sXMLContent + " is either null or blank.");
	}

	// Parse the document you want to check.
	Source source = new StreamSource(new StringReader(sXMLContent
		.toString()));

	// Validate the document.
	validateXML(source, sXSD);
    }

    /**
     * Writes a given content a given file.
     * 
     * @param sFile
     *            Fully qualified filename.
     * @param sContent
     *            Content to write.
     * @param bDelete
     *            true to delete the existing file. If false is given and the
     *            file exist, it will throw an exception.
     * @throws Exception
     */
    public static void write2File(String sFile, String sContent, boolean bDelete)
	    throws Exception {
	write2File(sFile, new StringBuffer(sContent), bDelete);
    }

    /**
     * Writes a given content a given file.
     * 
     * @param sFile
     *            Fully qualified filename.
     * @param sContent
     *            Content to write.
     * @param bDelete
     *            true to delete the existing file. If false is given and the
     *            file exist, it will throw an exception.
     * @throws Exception
     */
    public static void write2File(String sFile, StringBuffer sbContent,
	    boolean bDelete) throws Exception {

	File fFile = new File(sFile);

	// Remove if the file exist.
	if (bDelete) {
	    if (fFile.exists()) {
		fFile.delete();
	    }
	} else {
	    throw new Exception(sFile + " exist!");
	}

	// Write the given content.
	FileWriter fstream = new FileWriter(sFile);
	BufferedWriter bwOut = new BufferedWriter(fstream);
	bwOut.write(sbContent.toString());
	bwOut.close();

    }

}
