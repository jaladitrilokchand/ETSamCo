/**
 * <pre>
 * =============================================================================
 * 
 *  Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
 * 
 * =============================================================================
 *     FILE: IcofXmlUtil.java
 * 
 *  CREATOR: Aydin Suren
 * 
 * -PURPOSE---------------------------------------------------------------------
 *  A utility class for XML documents.
 * -----------------------------------------------------------------------------
 * 
 * 
 * -CHANGE LOG------------------------------------------------------------------
 *  03/02/2009 AS  Initial coding.
 *  11/09/2009 AS  Added reverseXMLReservedChars method.
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.common;

import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;

public class IcofXmlUtil {

    /**
     * Writes the XML to given OutputStream.
     * 
     * @param doc
     *            XML document object.
     * @param out
     *            Output stream to write to. Ex: System.out
     * 
     * @throws Exception
     *             Trouble executing the method.
     */
    public static void serialize(Document doc, OutputStream out)
	    throws IcofException {

	try {
	    OutputFormat format = new OutputFormat(doc);
	    format.setLineWidth(80);
	    format.setIndenting(true);
	    format.setIndent(2);
	    XMLSerializer serializer = new XMLSerializer(out, format);
	    serializer.serialize(doc);
	} catch (Exception e) {
	    throw new IcofException(e.getMessage(), IcofException.SEVERE);
	}
    }

    /**
     * Formats give n the XML document.
     * 
     * @param doc
     *            XML document object.
     * 
     * @return Formatted string version of the XML document as StringBuffer.
     * @throws IcofException
     *             Trouble executing the method.
     */
    public static StringBuffer formatXML(Document doc) throws IcofException {

	StringBuffer sbReturn = null;
	try {

	    Source source = new DOMSource(doc.getFirstChild());
	    StringWriter stringWriter = new StringWriter();
	    Result result = new StreamResult(stringWriter);
	    TransformerFactory factory = TransformerFactory.newInstance();
	    Transformer transformer = factory.newTransformer();
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(
		    "{http://xml.apache.org/xslt}indent-amount", "2");
	    transformer.transform(source, result);
	    sbReturn = stringWriter.getBuffer();

	} catch (Exception e) {
	    throw new IcofException(e.getMessage(), IcofException.SEVERE);
	}
	return sbReturn;
    }

    /**
     * Reverses the XML reserved chars to their original chars from a given
     * string. For example, "&amp;" to "&".
     * 
     * @param sInput
     *            Value
     * @return String New string
     * 
     */
    public static String reverseXMLReservedChars(String sInput)
	    throws Exception {

	if (IcofStringUtil.isEmpty(sInput)) {
	    return sInput;
	}

	// Replace the "&" first so that it won't be affected by
	// others.
	sInput = sInput.replace("&amp;", "&");
	sInput = sInput.replace("&lt;", "<");
	sInput = sInput.replace("&gt;", ">");
	sInput = sInput.replace("&apos;", "\'");
	sInput = sInput.replace("&quot;", "\"");

	return sInput;
    }

}
