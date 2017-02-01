/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2005 - 2011 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *    FILE: IcofCollectionsUtil.java
 *
 * CREATOR: Karen K. Witt
 *    DEPT: AW0V
 *    DATE: 12/15/2005
 *
 *-PURPOSE---------------------------------------------------------------------
 * IcofCollectionsUtil class definition file.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 12/15/2005 KKW  Initial coding.
 * 04/23/2006 KPL  Added Collection diff/intersect methods
 * 04/19/2007 KKW  Changed parseString so that empty strings will still be
 *                 added to the resulting Vector if the line being parsed
 *                 contains them.
 * 04/27/2007 GFS  Synchronized all public static methods.
 * 03/03/2009 KKW  Updated diff methods to be more efficient, such that
 *                 they don't loop through the "left" collection when the
 *                 "right" is null or empty.
 * 05/22/2009 AS   Added isEmpty method.
 * 08/05/2010 APA  Overloaded the method getVectorAsString with a boolean flag
 * 				   that indicates if a trailing delimiter needs to be added or not
 * 08/09/2010 KKW  Fixed problem in getVectorAsString -- it was not including
 *                 the delimiter!
 *                 Also removed use of java.lang.String.isEmpty method, as this
 *                 method is not supported in java 1.4 or 1.5.
 * 05/20/2011 KKW  Added getVectorAsStringArray method and fixed some java 1.5
 *                 collection warnings                
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.common;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.ibm.stg.iipmds.icof.component.util.ManagerFunctions;

public class IcofCollectionsUtil {

    // -----------------------------------------------------------------------------
    /**
     * Get the contents of the String array as a Vector.
     * 
     * @param strArray
     *            the string array to be converted to a Vector.
     * 
     * @return the contents of the string array as a Vector.
     */
    // -----------------------------------------------------------------------------
    public static synchronized Vector<String> getStringArrayAsVector(String[] strArray) {

	Vector<String> vector = new Vector<String>();
	for (int i = 0; i < strArray.length; i++) {

	    vector.add(strArray[i]);

	}

	return vector;

    }

    // -----------------------------------------------------------------------------
    /**
     * Get the contents of the Vector as a string array.
     * 
     * @param Vector
     *            the vector to be converted to string array
     * 
     * @return the contents of the vector as a string array
     */
    // -----------------------------------------------------------------------------
    public static synchronized String[] getVectorAsStringArray(Vector<String> aVector) {

        String[] stringArray = new String[aVector.size()];
        aVector.toArray(stringArray);

        return stringArray;

    }

    // -----------------------------------------------------------------------------
    /**
     * Get the contents of the TreeMap as a String -- the TreeMap contains
     * String objects.
     * 
     * @param srcTreeMap
     *            the TreeMap to be converted to a delimited string.
     * @param aDelimiter
     *            the delimiter to use between elements of the TreeMap when
     *            building the string representation.
     * 
     * @return the contents of the TreeMap as a delimited string.
     */
    // -----------------------------------------------------------------------------
    public static synchronized String getTreeMapAsString(TreeMap<String, String> srcTreeMap,
	    String aDelimiter) {

	String tgtString = "";
	Iterator<String> iter = srcTreeMap.values().iterator();

	while (iter.hasNext()) {

	    String field = (String) iter.next();
	    
	    tgtString += field + aDelimiter;

	}

	return tgtString;

    }

    // -----------------------------------------------------------------------------
    /**
     * This method calls the overloaded method to get the contents of the vector 
     * as a delimited string. The addTrailingDelimiter flag is set here as true
     * inorder to include the delimiter at the end of the string.
     * 
     * @param srcVector
     *            the Vector to be converted to a delimited string.
     * @param aDelimiter
     *            the delimiter to use between elements of the Vector when
     *            building the string representation.
     * 
     * @return the contents of the Vector as a delimited string.
     */
    // -----------------------------------------------------------------------------
    public static synchronized String getVectorAsString(Vector<String> srcVector,
    	    String aDelimiter) {
    	
    	return getVectorAsString(srcVector, aDelimiter, true);

    }

    // -----------------------------------------------------------------------------
    /**
     * Gets the contents of the vector as a delimited string.
     * 
     * @param srcVector
     *            the Vector to be converted to a delimited string.
     * @param aDelimiter
     *            the delimiter to use between elements of the Vector when
     *            building the string representation.
     * @param addTrailingDelimiter 
     * 			  the flag that indicates if the delimiter has to be included
     * 			  at the end of the string or not.
     * 
     * @return the contents of the Vector as a delimited string.
     */
    // -----------------------------------------------------------------------------
    public static synchronized String getVectorAsString(Vector<String> srcVector,
    	    String aDelimiter, boolean addTrailingDelimiter) {

    	String tgtString = "";
    	
    	Iterator<String> iter = srcVector.iterator();

    	while (iter.hasNext()) {

   	    	if (!tgtString.equals("")) {
   	    	    tgtString += aDelimiter;
    	    }
    		
    	    String field = iter.next();
    	    tgtString += field;
    	}

        if (addTrailingDelimiter) { 
            if (!tgtString.equals("")) {
                tgtString += aDelimiter;
            }
        }

    	return tgtString;

    }
    // -----------------------------------------------------------------------------
    /**
     * Get a Vector of Strings as a TreeMap whose searchKey is the Vector's
     * element (a String element) and whose value is also the Vector's String
     * element
     * 
     * @param srcVector
     *            Vector of String objects
     * @return TreeMap whose elements have keys that are the Vector's String
     *         elements and values that match their corresponding keys.
     */
    // -----------------------------------------------------------------------------
    public static synchronized TreeMap<String, String> getVectorAsTreeMap(Vector<String> srcVector) {

	TreeMap<String, String> vectorAsTreeMap = new TreeMap<String, String>(ManagerFunctions.STRINGCOMPARE);

	Iterator<String> iter = srcVector.iterator();
	while (iter.hasNext()) {

	    String thisString = (String) iter.next();
	    vectorAsTreeMap.put(thisString, thisString);

	}

	return vectorAsTreeMap;
    }

    // -----------------------------------------------------------------------------
    /**
     * Parse a delimited string apart and add each element to the specified
     * Vector. If there is no value between two delimiters (ex ;;), then an
     * empty string will be placed in the Vector for that value. The idea is to
     * return a Vector containing each field from the input line in the same
     * sequence as the input line's fields.
     * 
     * @param srcString
     *            the string to be parsed
     * @param aDelimiter
     *            the character on which to split the string
     * @param tgtVector
     *            the vector to receive the "tokens"
     * @param trimInd
     *            Indicates whether or not to use String.trim()
     * @exception IcofException
     *                Unable to parse string
     */
    // -----------------------------------------------------------------------------
    public static synchronized void parseString(String srcString,
	    String aDelimiter, Vector<String> tgtVector, boolean trimInd)
	    throws IcofException {

	String funcName = new String("parseString(String, String, Vector)");

	try {

	    String srcStringCopy = new String(srcString);

	    if (srcStringCopy.equals("")) {
		return;
	    }

	    // Make sure the string is terminated with a delimiter.
	    if (srcStringCopy.lastIndexOf(aDelimiter) != srcStringCopy.length() - 1) {
		srcStringCopy += aDelimiter;
	    }

	    int count = IcofStringUtil.occurrencesOf(srcStringCopy, aDelimiter);
	    for (int i = 0; i < count; i++) {
		String field = new String(IcofStringUtil.getField(
			srcStringCopy, i + 1, aDelimiter));
		if (trimInd) {
		    field = field.trim();
		}
		tgtVector.addElement(field);
	    }
	} catch (Exception e) {
	    IcofException ie = new IcofException(CLASS_NAME, funcName,
		    IcofException.SEVERE, "Error parsing string " + srcString
			    + "  delimited by " + aDelimiter, "");
	    throw (ie);
	}

    }

    // -----------------------------------------------------------------------------
    /**
     * Parse a delimited string apart and add each element to the specified
     * TreeMap. Also return a vector of any duplicate strings that were in the
     * input string.
     * 
     * @param srcString
     *            the string to be parsed
     * @param aDelimiter
     *            the character on which to split the string
     * @param tgtMap
     *            the treeMap to receive the "tokens"
     * @param trimInd
     *            Indicates whether or not to use String.trim()
     * @return vector of duplicate strings
     * @exception IcofException
     *                Unable to parse string
     */
    // -----------------------------------------------------------------------------
    public static synchronized Vector<String> parseString(String srcString,
	    String aDelimiter, TreeMap<String, String> tgtMap, boolean trimInd)
	    throws IcofException {

	String funcName = new String("parseString(String, String, TreeMap)");

	try {

	    Vector<String> duplicates = new Vector<String>();
	    String srcStringCopy = new String(srcString);

	    if (srcStringCopy.equals("")) {
		return duplicates;
	    }

	    // Make sure the string is terminated with a delimiter.
	    if (srcStringCopy.lastIndexOf(aDelimiter) != srcStringCopy.length() - 1) {
		srcStringCopy += aDelimiter;
	    }

	    int count = IcofStringUtil.occurrencesOf(srcStringCopy, aDelimiter);
	    for (int i = 0; i < count; i++) {
		String field = new String(IcofStringUtil.getField(
			srcStringCopy, i + 1, aDelimiter));
		if (trimInd) {
		    if (!field.equals("")) {
			field = field.trim();
		    }
		}

		if (tgtMap.containsKey(field)) {
		    duplicates.add(field);
		}
		tgtMap.put(field, field);
	    }
	    return duplicates;
	} catch (Exception e) {
	    IcofException ie = new IcofException(CLASS_NAME, funcName,
		    IcofException.SEVERE, "Error parsing string " + srcString
			    + "  delimited by " + aDelimiter, "");
	    throw (ie);
	}

    }

    // -----------------------------------------------------------------------------
    /**
     * Returns a new TreeMap with all elements of TreeMap left after subtracting
     * all elements of TreeMap right. TreeMap keys and values are both
     * considered in comparing elements, if either or both are unequal the
     * element is considered different.
     * 
     * @param left
     *            a TreeMap with elements to be retained
     * @param right
     *            another TreeMap with elements to be removed from the result
     * @exception IcofException
     *                Unable to create TreeMap diff
     */
    // -----------------------------------------------------------------------------
    public static synchronized TreeMap diff(TreeMap left, TreeMap right)
	    throws IcofException {

	String funcName = new String("diff(TreeMap, TreeMap)");

	TreeMap aDiff = null;

	try {

	    aDiff = diff(left, right, false);

	} catch (Exception e) {
	    IcofException ie = new IcofException(CLASS_NAME, funcName,
		    IcofException.SEVERE,
		    "Error creating difference of TreeMaps ", "");
	    throw (ie);
	}

	return aDiff;
    }

    // -----------------------------------------------------------------------------
    /**
     * Returns a new TreeMap with all elements of TreeMap left after subtracting
     * all elements of TreeMap right. TreeMap keys and values are both
     * considered in comparing elements if keysOnly is false, when true only
     * keys are considered if either or both are unequal the element is
     * considered different.
     * 
     * @param left
     *            a TreeMap with elements to be retained
     * @param right
     *            another TreeMap with elements to be removed from the result
     * @param keysOnly
     *            true if only the TreeMap keys are considered, false if both
     *            keys and values are to be compared
     * @exception IcofException
     *                Unable to create TreeMap diff
     */
    // -----------------------------------------------------------------------------
    public static synchronized TreeMap diff(TreeMap left, TreeMap right,
	    boolean keysOnly) throws IcofException {

	String funcName = new String("diff(TreeMap, TreeMap, boolean)");

	// If the "right" TreeMap is null or empty, just return the "left"
	// TreeMap.
	// There's no point in cycling through the entire "left" TreeMap, in
	// this
	// case.
	if ((right == null) || (right.isEmpty())) {
	    return left;
	}

	TreeMap aDiff = new TreeMap();

	try {

	    Set keySetLeft = left.keySet();
	    Iterator lIter = keySetLeft.iterator();

	    while (lIter.hasNext()) {

		Object keyLeft = (Object) lIter.next();
		Object valueLeft = (Object) left.get(keyLeft);

		if (!right.containsKey(keyLeft)) {
		    aDiff.put(keyLeft, left.get(keyLeft));
		} else {
		    // the right TreeMap does have a matching key...
		    if (!keysOnly) {
			Object valueRight = (Object) right.get(keyLeft);

			// the keys AND values both must match for the diff
			if (!valueLeft.equals(valueRight)) {
			    aDiff.put(keyLeft, left.get(keyLeft));
			}
		    }
		}
	    }
	} catch (Exception e) {
	    IcofException ie = new IcofException(CLASS_NAME, funcName,
		    IcofException.SEVERE,
		    "Error creating difference of TreeMaps ", "");
	    throw (ie);
	}

	return aDiff;
    }

    // -----------------------------------------------------------------------------
    /**
     * Returns a new Vector with all elements of Vector left after subtracting
     * all elements of Vector right.
     * 
     * @param left
     *            a Vector to be comapred
     * @param right
     *            a different Vector to be comapred
     * @exception IcofException
     *                Unable to create Vector diff
     */
    // -----------------------------------------------------------------------------
    public static synchronized Vector diff(Vector left, Vector right)
	    throws IcofException {

	String funcName = new String("diff(Vector, Vector)");

	// If the "right" Vector is null or empty, just return the "left"
	// Vector.
	// There's no point in cycling through the entire "left" Vector, in this
	// case.
	if ((right == null) || (right.isEmpty())) {
	    return left;
	}

	Vector aDiff = new Vector();

	try {

	    for (int i = 0; i < left.size(); i++) {
		Object o = (Object) left.elementAt(i);
		if (!right.contains(o)) {
		    aDiff.add(o);
		}
	    }

	    aDiff.trimToSize();
	} catch (Exception e) {
	    IcofException ie = new IcofException(CLASS_NAME, funcName,
		    IcofException.SEVERE,
		    "Error creating difference of Vectors ", "");
	    throw (ie);
	}

	return aDiff;
    }

    // -----------------------------------------------------------------------------
    /**
     * Returns a new TreeMap with all elements of TreeMap left that are also in
     * TreeMap right. TreeMap keys and values are both considered in comparing
     * elements, if either or both are unequal the element is considered
     * different.
     * 
     * @param left
     *            a TreeMap to be intersected
     * @param right
     *            another TreeMap to be intersected
     * @exception IcofException
     *                Unable to create TreeMap intersection
     */
    // -----------------------------------------------------------------------------
    public static synchronized TreeMap intersect(TreeMap left, TreeMap right)
	    throws IcofException {

	String funcName = new String("intersect(TreeMap, TreeMap)");

	TreeMap intersection = new TreeMap();

	try {

	    // the third parm 'false' means that not only are keys but olso
	    // values are
	    // compared to determine sameness for inclusion in the intersection.
	    intersection = intersect(left, right, false);

	} catch (Exception e) {
	    IcofException ie = new IcofException(CLASS_NAME, funcName,
		    IcofException.SEVERE,
		    "Error creating intersection of TreeMaps ", "");
	    throw (ie);
	}

	return intersection;
    }

    // -----------------------------------------------------------------------------
    /**
     * Intersect two TreeMaps returning a new TreeMap with only elements common
     * to both input maps.
     * 
     * @param left
     *            a TreeMap to be intersected
     * @param right
     *            another TreeMap to be intersected
     * @param keysOnly
     *            true if only the TreeMap keys are considered, false if both
     *            keys and values are to be compared
     * @exception IcofException
     *                Unable to create TreeMap intersection
     */
    // -----------------------------------------------------------------------------
    public static synchronized TreeMap intersect(TreeMap left, TreeMap right,
	    boolean keysOnly) throws IcofException {

	String funcName = new String("Intersect(TreeMap, TreeMap, boolean)");

	TreeMap intersection = new TreeMap();

	try {

	    Set keySetLeft = left.keySet();
	    Iterator lIter = keySetLeft.iterator();

	    while (lIter.hasNext()) {

		Object keyLeft = (Object) lIter.next();
		Object valueLeft = (Object) left.get(keyLeft);

		if (right.containsKey(keyLeft)) {
		    Object valueRight = (Object) right.get(keyLeft);

		    if (keysOnly) {
			intersection.put(keyLeft, left.get(keyLeft));
		    } else {
			// the keys AND values both must match for the
			// intersection
			if (valueLeft.equals(valueRight))
			    intersection.put(keyLeft, left.get(keyLeft));
		    }
		}
	    }

	} catch (Exception e) {
	    IcofException ie = new IcofException(CLASS_NAME, funcName,
		    IcofException.SEVERE,
		    "Error creating intersection of TreeMaps ", "");
	    throw (ie);
	}

	return intersection;
    }

    // -----------------------------------------------------------------------------
    /**
     * Intersect two Vectors returning a new Vector with only elements common to
     * both input vectors.
     * 
     * @param left
     *            the string to be parsed
     * @param right
     *            the character on which to split the string
     * @exception IcofException
     *                Unable to create Vector intersection
     */
    // -----------------------------------------------------------------------------
    public static synchronized Vector intersect(Vector left, Vector right)
	    throws IcofException {

	String funcName = new String("Intersect(Vector, Vector)");

	Vector intersection = new Vector();
	Object leftObj = null;

	try {
	    for (Enumeration e = left.elements(); e.hasMoreElements();) {

		leftObj = e.nextElement();

		if (right.contains(leftObj)) {
		    intersection.add(leftObj);
		}
	    }

	    intersection.trimToSize();
	} catch (Exception e) {
	    IcofException ie = new IcofException(CLASS_NAME, funcName,
		    IcofException.SEVERE,
		    "Error creating intersection of Vectors ", "");
	    throw (ie);
	}

	return intersection;
    }

    // -----------------------------------------------------------------------------
    /**
     * Create a new TreeMap which is the union of TreeMap left and TreeMap right
     * containing only one of each unique element from both TreeMaps.
     * 
     * @param left
     *            a TreeMap to be combined
     * @param right
     *            another TreeMap to be combined
     * @exception IcofException
     *                Unable to create TreeMap union
     */
    // -----------------------------------------------------------------------------
    public static synchronized TreeMap union(TreeMap left, TreeMap right)
	    throws IcofException {

	String funcName = new String("union(TreeMap, TreeMap)");

	TreeMap union = new TreeMap(left);

	try {

	    union.putAll(right);

	} catch (Exception e) {
	    IcofException ie = new IcofException(CLASS_NAME, funcName,
		    IcofException.SEVERE, "Error creating union of TreeMaps ",
		    "");
	    throw (ie);
	}

	return union;
    }

    // -----------------------------------------------------------------------------
    /**
     * Create a new Vector which is the union of Vector left and Vector right
     * containing only one of each unique element from both Vectors.
     * 
     * @param left
     *            a Vector to be combined
     * @param right
     *            another Vector to be combined
     * @exception IcofException
     *                Unable to create Vector union
     */
    // -----------------------------------------------------------------------------
    public static synchronized Vector union(Vector left, Vector right)
	    throws IcofException {

	String funcName = new String("union(Vector, Vector)");

	Vector aUnion = new Vector();

	try {

	    for (int i = 0; i < left.size(); i++) {
		if (!left.elementAt(i).toString().equals("")) {
		    if (!aUnion.contains(left.elementAt(i))) {
			aUnion.add(left.elementAt(i));
		    }
		}
	    }

	    for (int j = 0; j < right.size(); j++) {
		if (!right.elementAt(j).toString().equals("")) {
		    if (!aUnion.contains(right.elementAt(j))) {
			aUnion.add(right.elementAt(j));
		    }
		}
	    }

	    aUnion.trimToSize();
	} catch (Exception e) {
	    IcofException ie = new IcofException(CLASS_NAME, funcName,
		    IcofException.SEVERE, "Error creating union of Vectors ",
		    "");
	    throw (ie);
	}

	return aUnion;
    }

    /**
     * A convenient method to see if a vector has any elemets.
     * 
     * @param aVector
     *            Vector to check.
     * @return true if it is null and/or has no elements.
     * @throws IcofException
     */
    public static boolean isEmpty(Vector aVector) throws IcofException {
	boolean bReturn = false;
	if (aVector == null || aVector.size() <= 0) {
	    bReturn = true;
	}
	return bReturn;
    }

    // -----------------------------------------------------------------------------
    // Data elements.
    // -----------------------------------------------------------------------------
    private static final String CLASS_NAME = "IcofCollectionsUtil";

}

// ========================== END OF FILE ====================================
