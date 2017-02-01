//=============================================================================
//
// Copyright: (C) IBM Corporation 2001 - 2010 -- IBM Internal Use Only
//
//=============================================================================
//
//    FILE: ManagerFunctions.java
//
// CREATOR: Regina Yang
//          Karen Kellam
//    DEPT: 5ZIA
//    DATE: 01/23/2001
//
//-PURPOSE---------------------------------------------------------------------
// ManagerFunctions class definition file.
//-----------------------------------------------------------------------------
//
//
//-CHANGE LOG------------------------------------------------------------------
// 01/23/2001 RY  Initial coding.
// 02/28/2001 KK  Added getPage(..)
// 01/22/2002 KK  Corrected problem in LIBRELSTRINGCOMPARE -- to properly
//                handle the .xx portion of the version.
// 02/06/2002 EH  Added REVSTRINGCOMPARE
// 05/01/2002 KK  Added VERSIONSTRINGCOMPARE
// 06/13/2005 AS  Removed VERSIONSTRINGCOMPARE method and updated
//                LIBRELSTRINGCOMPARE method to work without keying on the
//                version prefix.
// 11/16/2007 GS  Added LONGCOMPARE.
// 11/15/2010 KKW Fixed java 1.5 warnings
//=============================================================================


package com.ibm.stg.iipmds.icof.component.util;
import java.util.Comparator;

import com.ibm.stg.iipmds.common.Constants;

public class ManagerFunctions {


    //============================================================================
    //  This function will be used to provide a compareTo() method for LibRelease
    //    keys (Strings).  Use of the Java collections requires this function.
    //============================================================================
    public static final Comparator<String> LIBRELSTRINGCOMPARE = new Comparator<String>() {

        public int compare(String s1, String s2) {

            // LibRelease key = techName;version
            String techName1 =  s1.substring(0, s1.indexOf(Constants.SEMI_COLON));
            String techName2 =  s2.substring(0, s2.indexOf(Constants.SEMI_COLON));

            // If the tech names are different, indicate which one should come
            //   first.
            if (techName1.compareTo(techName2) < 0) {
                return -1;
            }

            if (techName1.compareTo(techName2) > 0) {
                return 1;
            }

            // If the tech names are the same, compare the versions.

            // Get version.
            char c;
            String s1VersionField = null;
            String s2VersionField = null;
            s1VersionField = s1.substring(s1.indexOf(Constants.SEMI_COLON) + 1);
            s2VersionField = s2.substring(s2.indexOf(Constants.SEMI_COLON) + 1);

            // Reform the s1 string (number portion only).
            s1 = "";
            for (int i = 0; i < s1VersionField.length(); ++i ) {
                c = s1VersionField.charAt(i);

                // Ignore "." if we haven't found a digit yet.
                if ((c == '.') && s1.equals("")) {
                    continue;
                }

                // Grab the numbers and fraction dot.
                if ((c >= '0' && c <= '9') || (c == '.')) {
                    s1 += String.valueOf(c);
                }
            }

            // Reform the s2 string (number portion only).
            s2 = "";
            for (int i = 0; i < s2VersionField.length(); ++i ) {
                c = s2VersionField.charAt(i);

                // Ignore "." if we haven't found a digit yet.
                if ((c == '.') && s2.equals("")) {
                    continue;
                }

                // Grab the numbers and fraction dot.
                if ((c >= '0' && c <= '9') || (c == '.')) {
                    s2 += String.valueOf(c);
                }
            }

            // If either release name did not contain digits, then compare the
            //   release names as strings.
            if ((s1.equals("")) || (s2.equals(""))) {
                if (s1VersionField.compareTo(s2VersionField) < 0) {
                    return -1;
                }

                if (s1VersionField.compareTo(s2VersionField) > 0) {
                    return 1;
                }

            }
            else {
                // If both release names contained digits, then convert the version 
                //  numbers to float for comparison.
                float f1 = new Float(s1).floatValue();
                float f2 = new Float(s2).floatValue();

                if (f1 < f2) {
                    return -1;
                }

                if (f1 > f2) {
                    return 1;
                }
            }
            return 0;
        }
    };


    //============================================================================
    //  This function will be used to provide a compareTo() method for String
    //    objects.  Use of the Java collections requires this function.
    //============================================================================
    public static final Comparator<String> STRINGCOMPARE = new Comparator<String>() {
        public int compare(String s1, String s2) {
            int len1 = s1.length();
            int len2 = s2.length();
            for (int i = 0, n = Math.min(len1, len2); i < n; i++ ) {
                char c1 = s1.charAt(i);
                char c2 = s2.charAt(i);
                if (c1 != c2)
                    return c1 - c2;
            }
            return len1 - len2;
        }
    };


    //============================================================================
    //  This function will be used to provide a compareTo() method for String
    //    objects.  It will do a reverse sort.
    //    Use of the Java collections requires this function.
    //============================================================================
    public static final Comparator<String> REVSTRINGCOMPARE = new Comparator<String>() {
        public int compare(String s1, String s2) {
            int len1 = s1.length();
            int len2 = s2.length();
            for (int i = 0, n = Math.min(len1, len2); i < n; i++ ) {
                char c1 = s1.charAt(i);
                char c2 = s2.charAt(i);
                if (c1 != c2)
                    return c2 - c1;
            }
            return len2 - len1;
        }
    };


    //============================================================================
    //  This function will be used to provide a compareTo() method for Integer
    //    objects.  Use of the Java collections requires this function.
    //============================================================================
    public static final Comparator<Integer> INTEGERCOMPARE = new Comparator<Integer>() {
        public int compare(Integer i1, Integer i2) {
            if (i1.intValue() < i2.intValue())
                return( -1 );
            else if (i1.intValue() > i2.intValue())
                return(1);
            else
                return(0);
        }
    };

    //============================================================================
    //  This function will be used to provide a compareTo() method for Long
    //    objects.  Use of the Java collections requires this function.
    //============================================================================
    public static final Comparator<Long> LONGCOMPARE = new Comparator<Long>() {
        public int compare(Long l1, Long l2) {

            if (l1.intValue() < l2.intValue())
                return( -1 );
            else if (l1.intValue() > l2.intValue())
                return(1);
            else
                return(0);
        }
    };


    //============================================================================
    //  This function will be used to provide a compareTo() method for Double
    //    objects.  Use of the Java collections requires this function.
    //============================================================================
    public static final Comparator<Double> DOUBLECOMPARE = new Comparator<Double>() {
        public int compare(Double d1, Double d2) {
            if (d1.doubleValue() < d2.doubleValue())
                return(-1);
            else if (d1.doubleValue() > d2.doubleValue())
                return(1);
            else
                return(0);
        }
    };

    //============================================================================
    //  This function will be used to provide a compareTo() method for Double
    //    objects. The objects will be ordred largest to smallest. 
    //  Use of the Java collections requires this function.
    //============================================================================
    public static final Comparator<Double> REVDOUBLECOMPARE = new Comparator<Double>() {
        public int compare(Double d1, Double d2) {

            if (d1.doubleValue() > d2.doubleValue())
                return(-1);
            else if (d1.doubleValue() < d2.doubleValue())
                return(1);
            else
                return(0);
        }
    };

    //-----------------------------------------------------------------------------
    // Data elements.
    //-----------------------------------------------------------------------------
    private static final String CLASS_NAME = "ManagerFunctions";

}







