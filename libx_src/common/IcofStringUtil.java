/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2005 - 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *    FILE: IcofStringUtil.java
 *
 * CREATOR: Keith P. Loring
 *    DEPT: AW0V
 *    DATE: 11/16/2005
 *
 *-PURPOSE---------------------------------------------------------------------
 * Funtions that manipulate String objects.
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 11/16/2005 KPL  Initial coding.
 * 04/18/2007 KKW  Rewrote isDigits and isAlphanumeric to work correctly.
 * 04/20/2007 KKW  Fixed two errors in rightJustify and some comments.
 * 05/22/2007 RAM  Synchronized all public static methods.
 * 06/21/2007 GFS  Updated trimInterior() to work correctly
 * 01/24/2008 KKW  Fixed items identified by RSA Code Analysis tool --
 *                 specifically using == and != to compare java objects.
 * 06/10/2008 KKW  Corrected java doc that was swapped for two methods
 * 03/03/2009 AS   Added isEmpty method
 * 04/08/2009 KKW  Added removeTrailingNewLine method
 * 02/02/2010 KKW  Added formatNumber(double, boolean) method
 * 04/15/2010 KKW  Fixed bug in replaceString -- the while condition was > 0
 *                 and should have been >= 0.
 * 05/13/2010 KKW  Added formatNumber(long, boolean)
 * 07/13/2010 KKW  Added removeTrailingChar method and changed removeTrailingNewLine
 *                 method to call it.                
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.common;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IcofStringUtil {

    // -----------------------------------------------------------------------------
    /**
     * Convert a boolean to a one-char ("Y" or "N") string. This function is
     * typically used when inserting boolean values into a DB2 table.
     * 
     * @param aBoolean
     *            the boolean to be converted to a one-char string.
     * 
     * @return the one-char representation of the boolean passed in.
     */
    // -----------------------------------------------------------------------------
    public static synchronized String booleanToString(boolean aBoolean) {

        if (aBoolean == true) {
            return Constants.DB_TRUE;
        }
        else {
            return Constants.DB_FALSE;
        }
    }


    // -----------------------------------------------------------------------------
    /**
     * Convert a boolean to a ("Yes" or "No") string.
     * 
     * @param aBoolean
     *            the boolean to be converted to a "Yes"/"No" string.
     * 
     * @return the string representation of the boolean passed in.
     */
    // -----------------------------------------------------------------------------
    public static synchronized String booleanToFullString(boolean aBoolean) {

        if (aBoolean == true) {
            return "Yes";
        }
        else {
            return "No";
        }
    }


    // -----------------------------------------------------------------------------
    /**
     * Generates a string of specified length with the specified source centered
     * in it padded on both sides with blanks (spaces).
     * 
     * @param source
     *            the substring to be centered in the result string.
     * @param paddedLength
     *            the total length of the final result string.
     * @return the result String.
     */
    // -----------------------------------------------------------------------------
    public static synchronized String center(String source, int paddedLength) {
        return (center(source, " ", paddedLength));
    }


    // -----------------------------------------------------------------------------
    /**
     * Generates a string of specified length with the specified source centered
     * in it padded on both sides as specified.
     * 
     * @param source
     *            the substring to be centered in the result string.
     * @param padString
     *            the string which is repeated to form padding
     * @param paddedLength
     *            the total length of the final result string.
     * @return the result String.
     */
    // -----------------------------------------------------------------------------
    public static synchronized String center(String source,
                                             String padString,
                                             int paddedLength) {

        String generatedString = new String("");

        if (paddedLength < 1) {
            // makes no sense, do nothing, return the empty string
        }
        else if (source.length() > paddedLength) {
            // caller is asking for a padded string smaller than even the
            // centered
            // source which doesn't make a lot of sense. Rather than error out
            // let's fit what we can by trimming the source
            generatedString = source.substring(0, paddedLength);
        }
        else if (source.length() == paddedLength || padString.length() < 1) {
            // caller left no room to pad or nothing to pad with, so just return
            // the
            // 'centered' string with no padding
            generatedString = source;
        }
        else { // the source is smaller that the paddedLength - the 'normal'
            // case

            int leftPadLength = ((paddedLength - source.length()) / 2);

            // pad the left side
            while (generatedString.length() < leftPadLength) {
                generatedString = generatedString.concat(padString);
            }

            // trim off any excess padding on the last concat() above.
            generatedString = generatedString.substring(0, leftPadLength);

            // now add the 'center' string
            generatedString = generatedString.concat(source);

            // pad the right side
            while (generatedString.length() < paddedLength) {
                generatedString = generatedString.concat(padString);
            }

            // trim off any excess padding on the last concat() above.
            generatedString = generatedString.substring(0, paddedLength);
        }

        return generatedString;
    }


    // -----------------------------------------------------------------------------
    /**
     * Changes specified number of occurences of a specified pattern to a
     * specified replacement string.
     * 
     * 
     * @param source
     *            the source string to be changed.
     * @param pattern
     *            the pattern to be replaced
     * @param replacement
     *            the string which replaces 'pattern'
     * @return the generated String.
     */
    // -----------------------------------------------------------------------------
    public static synchronized String change(String source,
                                             String pattern,
                                             String replacement) {

        return (change(source, pattern, replacement, 0));
    }


    // -----------------------------------------------------------------------------
    /**
     * Changes specified number of occurences of a specified pattern to a
     * specified replacement string.
     * 
     * @param source
     *            the source string to be changed.
     * @param pattern
     *            the pattern to be replaced
     * @param replacement
     *            the string which replaces 'pattern'
     * @param startPos
     *            the character position (leftmost=1) to start searching
     * @return the generated String.
     */
    // -----------------------------------------------------------------------------
    public static synchronized String change(String source,
                                             String pattern,
                                             String replacement,
                                             int startPos) {

        return (change(source,
                       pattern,
                       replacement,
                       startPos,
                       Integer.MAX_VALUE));
    }


    // -----------------------------------------------------------------------------
    /**
     * Changes specified number of occurences of a specified pattern to a
     * specified replacement string.
     * 
     * @param source
     *            the source string to be changed.
     * @param pattern
     *            the pattern to be replaced
     * @param replacement
     *            the string which replaces 'pattern'
     * @param startPos
     *            the character position (leftmost=1) to start searching
     * @param numChanges
     *            mumber of instance to be replaced
     * @return the generated String.
     */
    // -----------------------------------------------------------------------------
    public static synchronized String change(String source,
                                             String pattern,
                                             String replacement,
                                             int startPos,
                                             int numChanges) {

        boolean done = false;
        int beginIndex = startPos;
        String generatedString = new String(source);

        // nicely handle all the funky cases
        if (source.equals("") || pattern.equals("")
            || numChanges < 1
            || startPos < 0) {
            done = true;
        }

        // loop through aString
        while (!done) {
            // locate left-most instance of the pattern
            beginIndex = source.indexOf(pattern, beginIndex);

            if (beginIndex > -1 && numChanges > 0) {
                // append any non-matching portion of the input string (left of
                // the match)
                // to the output String
                generatedString = source.substring(0, beginIndex);
                // append the replacement string to the output String
                generatedString = generatedString.concat(replacement);
                // append the remaining portion of aString which followed the
                // matched pattern
                generatedString = generatedString.concat(source.substring(beginIndex + 
                                                                          pattern.length()));

                source = generatedString;
                --numChanges;
            }
            else {
                // pattern not found in aString
                done = true;
            }
        }
        return generatedString;
    }


    // -----------------------------------------------------------------------------
    /**
     * Return the field, specified by fieldNum (first field is 1), from the
     * input line delimited by the specified delimiter.
     * 
     * @param aLine
     *            the String from which to return the field.
     * @param fieldNum
     *            the field to be returned (first field is 1).
     * @param aDelimiter
     *            the delimiter separating the fields in aLine.
     * 
     * @return the specified field.
     * 
     * @exception IcofException
     *                Unable to get the specified field.
     */
    // -----------------------------------------------------------------------------
    public static synchronized String getField(String aLine,
                                               int fieldNum,
                                               String aDelimiter)
                    throws IcofException {

        String funcName = new String("getField(String, short, String)");

        if ((aLine.equals("")) || (fieldNum == 0)) {
            return ("");
        }

        // Ensure that the last field is terminated with the delimiter
        if (aLine.lastIndexOf(aDelimiter) != aLine.length() - 1) {
            aLine += aDelimiter;
        }

        if (fieldNum > occurrencesOf(aLine, aDelimiter)) {
            IcofException ie = 
                new IcofException(CLASS_NAME,
                                  funcName,
                                  IcofException.SEVERE,
                                  "String contains fewer fields than the requested field" + 
                                  "Requested field is: " + 
                                  String.valueOf(fieldNum),
                                  aLine);
            throw (ie);
        }

        String field = new String("");
        for (short i = 0; i < fieldNum; i++) {
            //
            // Handle a null field, which is designated as two consecutive
            // delimiters,
            // which means the delimiter is in position 1. In that case, the
            // field is
            // a null string.
            //
            if (aLine.indexOf(aDelimiter) == 0) {
                field = "";
            }
            else {
                field = aLine.substring(0, aLine.indexOf(aDelimiter));
            }

            // Remove this field from the input line.
            aLine = aLine.substring(aLine.indexOf(aDelimiter) + 1);
        }

        return field;

    }


    // -----------------------------------------------------------------------------
    /**
     * Determines if all characters in a String are alphanumeric
     * 
     * @param source
     *            the string to examine.
     * @return true only if all characters are alphanumeric ('A'-'Z', 'a'-'z',
     *         or '0'-'9').
     */
    // -----------------------------------------------------------------------------
    public static synchronized boolean isAlphanumeric(String source) {

        // boolean isAlpha = true; // until we see a non-alpha character in
        // aString
        // int index = 0; // check each char in aString starting at the left
        // char aChar;
        //
        // // loop through aString until we find a non-alphanumeric char
        // while ( !isAlpha ) {
        // aChar = source.charAt(index++);
        // if ((aChar >= 'A') && (aChar <= 'Z') ||
        // (aChar >= 'a') && (aChar <= 'z') ||
        // (aChar >= '0') && (aChar <= '9')) {
        // // aChar IS alphanumeric
        // } else {
        // isAlpha = false;
        // }
        // }

        // Test to see if the string contains any non-alphanumeric characters
        boolean isAlpha = true;
        Pattern nonAlpha = Pattern.compile("\\W");
        Matcher matcher = nonAlpha.matcher(source);
        if (source.length() == 0 || matcher.find()) {
            isAlpha = false;
        }

        return isAlpha;
    }


    // -----------------------------------------------------------------------------
    /**
     * Gets the first "word" of the input string -- all characters up to the
     * first whitespace character
     * 
     * @param aString
     *            the string to examine.
     * @return the first word
     */
    // -----------------------------------------------------------------------------
    public static synchronized String word(String aString) {

        if (aString.length() == 0) {
            return aString;
        }

        Pattern whiteSpace = Pattern.compile("\\s");
        Matcher matcher = whiteSpace.matcher(aString);
        if (matcher.find()) {
            int index = matcher.start();
            String firstWord = new String(aString.substring(0, index));
            return firstWord;
        }
        else {
            return aString;
        }
    }


    // -----------------------------------------------------------------------------
    /**
     * Determines if all characters in a String are digits
     * 
     * @param aString
     *            the boolean to be converted to a one-char string.
     * @return true only if all aString characters are digits ('0'-'9').
     */
    // -----------------------------------------------------------------------------
    public static synchronized boolean isDigits(String aString) {

        // boolean isDigits = true; // until we see a non-alpha character in
        // aString
        // int index = 0; // check each char in aString starting at the left
        // char aChar;
        //
        // // loop through aString until we find a non-numeric char
        // while ( !isDigits ) {
        // aChar = aString.charAt(index++);
        // if ((aChar >= '0') && (aChar <= '9')) {
        // // aChar IS digits
        // } else {
        // isDigits = false;
        // }
        // }

        // Test to see if the string contains any non-numeric characters
        boolean isDigits = true;
        Pattern nonDigit = Pattern.compile("\\D");
        Matcher matcher = nonDigit.matcher(aString);
        if (aString.length() == 0 || matcher.find()) {
            isDigits = false;
        }
        return isDigits;
    }


    // -----------------------------------------------------------------------------
    /**
     * Return a boolean indicating if the string is empty or null.
     * 
     * @return A boolean.
     */
    // -----------------------------------------------------------------------------
    public static boolean isEmpty(String sValue) {
        return (sValue == null || (sValue.trim()).length() == 0);
    }


    // -----------------------------------------------------------------------------
    /**
     * Generates a String with a left justified substring and padded to the
     * right for a specified total length
     * 
     * @param subString
     *            string to be left justified in the result
     * @param padString
     *            string used to pad the result
     * @param paddedLength
     *            length of the returned padded String
     * @return new left justified right padded string
     */
    // -----------------------------------------------------------------------------
    public static synchronized String leftJustify(String subString,
                                                  String padString,
                                                  int paddedLength) {
        String generatedString = new String("");

        if (subString.length() > paddedLength) {
            // caller is asking for a padded string smaller than even the
            // centered
            // substring which doesn't make a lot of sense. Rather than error
            // out
            // let's fit what we can by trimming the substring
            generatedString = subString.substring(0, paddedLength);
        }
        else if (subString.length() == paddedLength || padString.length() < 1) {
            // caller left no room to pad or nothing to pad with, so just return
            // the
            // left justified string with no padding
            generatedString = subString;
        }
        else { // the subString is smaller that the paddedLength - the
            // 'normal' case
            // copy the left justified string
            generatedString = subString;

            // pad the right side
            while (generatedString.length() < paddedLength) {
                generatedString = generatedString.concat(padString);
            }

            // trim off any excess padding on the last concat() above.
            generatedString = generatedString.substring(0, paddedLength);
        }

        return generatedString;
    }


    // -----------------------------------------------------------------------------
    /**
     * Return the number of occurrences of the specified delimiter in the
     * srcString.
     * 
     * @param srcString
     *            string to be searched
     * @param aDelimiter
     *            the delimiter to be counted in the srcString
     * @return number of occurrences of aDelimiter in srcString
     */
    // -----------------------------------------------------------------------------
    public static synchronized int occurrencesOf(String srcString,
                                                 String aDelimiter) {

        return occurrencesOf(srcString, aDelimiter, 0);

    }


    // -----------------------------------------------------------------------------
    /**
     * Return the number of occurrences of the specified delimiter in the
     * srcString.
     * 
     * @param srcString
     *            string to be searched
     * @param aDelimiter
     *            the delimiter to be counted in the srcString
     * @param startPos
     *            index to start searching
     * @return number of occurrences of aDelimiter in srcString
     */
    // -----------------------------------------------------------------------------
    public static synchronized int occurrencesOf(String srcString,
                                                 String aDelimiter,
                                                 int startPos) {

        if ((srcString.equals("")) || (aDelimiter.equals(""))) {
            return 0;
        }

        String srcStringCopy = srcString;

        // Count the number of times the delimiter occurs in the string.
        int count = 0;
        int index = srcStringCopy.indexOf(aDelimiter, startPos);
        while (index != -1) {
            count++;
            srcStringCopy = srcStringCopy.substring(index + 1);
            index = srcStringCopy.indexOf(aDelimiter);
        }

        return count;

    }


    // -----------------------------------------------------------------------------
    /**
     * Generates a string
     * 
     * @param aString
     *            the substring to be centered in the result string.
     * @param anOverlay
     *            the string which is repeated to form padding
     * @param padString
     *            the string which is repeated to form padding
     * @return the result String.
     */
    // -----------------------------------------------------------------------------
    public static synchronized String overlayWith(String aString,
                                                  String anOverlay,
                                                  String padString) {

        return overlayWith(aString, anOverlay, padString, 1);
    }


    // -----------------------------------------------------------------------------
    /**
     * Generates a string
     * 
     * @param aString
     *            the substring to be centered in the result string.
     * @param anOverlay
     *            the string which is repeated to form padding
     * @param padString
     *            the string which is repeated to form padding
     * @param startIndex
     *            the total length of the final result string.
     * @return the result String.
     */
    // -----------------------------------------------------------------------------
    public static synchronized String overlayWith(String aString,
                                                  String anOverlay,
                                                  String padString,
                                                  int startIndex) {

        String generatedString = new String(aString);

        if (startIndex < 0) {
            startIndex = 0; // startIndex is silly so force it to 0
        }
        if (padString.equals("")) {
            padString = " "; // padString is silly so force it to blank
        }

        // pad past the end of the aString up to startIndex
        while (generatedString.length() < startIndex - 1) {
            generatedString = generatedString.concat(padString);
        }

        // trim off any excess padding on the last concat() above.
        generatedString = generatedString.substring(0, startIndex - 1);

        generatedString = generatedString.concat(anOverlay);

        return generatedString;
    }


    // ---------------------------------------------------------------------------
    /**
     * Pad the source string with the specified pad character so that the length
     * of the resulting string is equal to the specified new length. This
     * function will pad the front of the string or the end of the string as
     * indicated by the boolean parameter.
     * 
     * @param srcString
     *            the string to be padded
     * @param newLength
     *            the desired length of the resulting string
     * @param padChar
     *            the character to pad with
     * @param leftPad
     *            true, if string is to be padded on the left; false, to pad on
     *            the right
     * @return the padded string
     */
    // -----------------------------------------------------------------------------
    public static synchronized String padString(String srcString,
                                                int newLength,
                                                String padChar,
                                                boolean leftPad) {

        if (!leftPad) {
            return padString(srcString, newLength, padChar);
        }

        if (srcString.length() >= newLength) {
            return srcString;
        }

        int numOfPadChars = newLength - srcString.length();

        StringBuffer srcStringBuf = new StringBuffer();
        for (int i = 0; i < numOfPadChars; i++) {
            srcStringBuf.append(padChar);
        }

        return (srcStringBuf.toString() + srcString);
    }


    // ---------------------------------------------------------------------------
    /**
     * Pad the source string with the specified pad character so that the length
     * of the resulting string is equal to the specified new length. This
     * function will pad the end of the source string.
     * 
     * @param srcString
     *            the string to be padded
     * @param newLength
     *            the desired length of the resulting string
     * @param padChar
     *            the character to pad with
     * @return the padded string
     */
    // -----------------------------------------------------------------------------
    public static synchronized String padString(String srcString,
                                                int newLength,
                                                String padChar) {

        if (srcString.length() >= newLength) {
            return srcString;
        }

        StringBuffer srcStringBuf = new StringBuffer(srcString);
        for (int i = srcStringBuf.length(); i < newLength; i++) {
            srcStringBuf.append(padChar);
        }

        return srcStringBuf.toString();
    }


    // -----------------------------------------------------------------------------
    /**
     * Replaces all occurrences of a given substring with another given
     * substring.
     * 
     * @param sourceStr
     *            the string to be modified
     * @param toReplace
     *            the string to be replaced
     * @param replaceWith
     *            the string to put in place of the toReplace string
     * @return the updated string
     */
    // -----------------------------------------------------------------------------
    public static synchronized String replaceString(String sourceStr,
                                                    String toReplace,
                                                    String replaceWith) {

        int startFrom = 0;
        String returnString = new String();
        int index = sourceStr.indexOf(toReplace, startFrom);
        if (index == -1)
            return sourceStr;

        while (index >= 0) {
            returnString += sourceStr.substring(startFrom, index) + replaceWith;
            startFrom = toReplace.length() + index;
            index = sourceStr.indexOf(toReplace, startFrom);
        }
        returnString += sourceStr.substring(startFrom);

        return returnString;
    }


    // -----------------------------------------------------------------------------
    /**
     * Generates a String with a right justified substring and padded to the
     * left for a specified total length
     * 
     * @param subString
     *            string to be right justified in the result
     * @param padString
     *            string used to pad the result
     * @param paddedLength
     *            length of the returned padded String
     * @return a new left padded, right justified string
     */
    // -----------------------------------------------------------------------------
    public static synchronized String rightJustify(String subString,
                                                   String padString,
                                                   int paddedLength) {
        String generatedString = new String("");

        if (subString.length() > paddedLength) {
            // caller is asking for a padded string smaller than even the
            // centered
            // substring which doesn't make a lot of sense. Rather than error
            // out
            // let's fit what we can by trimming the substring
            generatedString = subString.substring(0, paddedLength);
        }
        else if (subString.length() == paddedLength || padString.length() < 1) {
            // caller left no room to pad or nothing to pad with, so just return
            // the
            // right justified string with no padding
            generatedString = subString;
        }
        else { // the subString is smaller that the paddedLength - the
            // 'normal' case

            int rightPadLength = (paddedLength - subString.length());

            // pad the left side
            while (generatedString.length() < rightPadLength) {
                generatedString = generatedString.concat(padString);
            }

            // copy the right justified string
            generatedString += subString;
        }

        return generatedString;
    }


    // -----------------------------------------------------------------------------
    /**
     * Convert a string to a boolean. The string can be any representation of
     * "yes" or "no" (even "true" or "false") and case is ignored. In the event
     * that something other than "yes" or "no" is passed in, this function will
     * return false.
     * 
     * @param aString
     *            the string to be converted
     * @return true if string represents "yes"; false, if not
     */
    // -----------------------------------------------------------------------------
    public static synchronized boolean stringToBoolean(String aString) {

        boolean aBoolean = false;
        if ((aString.equalsIgnoreCase(Constants.DB_TRUE)) || 
                        (aString.equalsIgnoreCase(Constants.YES)) || 
                        (aString.equalsIgnoreCase("true"))) {
            aBoolean = true;
        }
        return aBoolean;
    }


    // -----------------------------------------------------------------------------
    /**
     * Removes duplicate, consecutive occurrences of the specified delimiter
     * from the interior of a string, as well as removing white space from the
     * front and back of the string.
     * 
     * This function is useful when the result of a system call, such as fs lq
     * returns information separated by multiple spaces, for example.
     * 
     * @param aLine
     *            the String from which to return the field.
     * @param aDelimiter
     *            the delimiter for the string.
     * @return the String with duplicate delimiters removed.
     * @exception IcofException
     *                Unable to remove duplicate delimiters
     */
    // -----------------------------------------------------------------------------
    public static synchronized String trimInterior(String aLine,
                                                   String aDelimiter)
    throws IcofException {

        if (aLine.equals("")) {
            return ("");
        }

        String result = new String("");
        aLine = aLine.trim();

        // Ensure that the last field is terminated with the delimiter
        if (aLine.lastIndexOf(aDelimiter) != aLine.length() - 1) {
            aLine += aDelimiter;
        }

        Vector tokens = new Vector();
        IcofCollectionsUtil.parseString(aLine, aDelimiter, tokens, false);
        Iterator iter = tokens.iterator();
        while (iter.hasNext()) {
            String token = (String) iter.next();
            if (token.equals(aDelimiter) || token.equals("")) {
                continue;
            }
            if (result.equals("")) {
                result += token;
            }
            else {
                result += aDelimiter + token;
            }
        }

        return result;

    }

    
    //-----------------------------------------------------------------------------
    /**
     * Remove the specified character from the end of the specified string if
     *   it is there
     * 
     * @param aString      the string to remove the specified character from
     * @param aChar        the character to remove from the end of the string
     * @return             the string minus the specified ending character
     */
    //-----------------------------------------------------------------------------
    public static synchronized String removeTrailingChar(String aString, String aChar) {

        if (aString.endsWith(aChar)) {
            aString = aString.substring(0, aString.length() - 1);
        }

        return aString;

    }

    
    //-----------------------------------------------------------------------------
    /**
     * Remove the new line character from the end of the specified string
     * 
     * @param aString      the string to remove the new line character from
     * @return             the string minus the ending new line character
     */
    //-----------------------------------------------------------------------------
    public static synchronized String removeTrailingNewLine(String aString) {

        return removeTrailingChar(aString, "\n");

    }

    
    //-----------------------------------------------------------------------------
    /**
     * Format a double as a string.  If specified by the boolean parameter, use
     *   digit grouping (ex. 1,000,000.0) with commas after every three digits
     * 
     * @param aDouble      the number to format
     * @param grouping     true, to group digits into sets of three separated by
     *                     commas; otherwise, false.
     * @return             the number as a string
     */
    //-----------------------------------------------------------------------------
    public static synchronized String formatNumber(double aDouble, boolean grouping) {

        NumberFormat numformat = NumberFormat.getInstance();
        numformat.setGroupingUsed(grouping);
        String sizeValue = numformat.format(aDouble);
        
        return sizeValue;

    }

    
    //-----------------------------------------------------------------------------
    /**
     * Format a long as a string.  If specified by the boolean parameter, use
     *   digit grouping (ex. 1,000,000) with commas after every three digits
     * 
     * @param aLong      the number to format
     * @param grouping     true, to group digits into sets of three separated by
     *                     commas; otherwise, false.
     * @return             the number as a string
     */
    //-----------------------------------------------------------------------------
    public static synchronized String formatNumber(long aLong, boolean grouping) {

        NumberFormat numformat = NumberFormat.getInstance();
        numformat.setGroupingUsed(grouping);
        String sizeValue = numformat.format(aLong);
        
        return sizeValue;

    }

    
    // -----------------------------------------------------------------------------
    // Data elements.
    // -----------------------------------------------------------------------------
    private static final String CLASS_NAME = "IcofStringUtil";
}

// ========================== END OF FILE ====================================
