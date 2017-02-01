/**
 * <pre>
 * =============================================================================
 * 
 *  Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
 * 
 * =============================================================================
 *     FILE: IcofAppUtil.java
 * 
 *  CREATOR: Aydin Suren
 * 
 * -PURPOSE---------------------------------------------------------------------
 *  A utility class for applications (non-web).
 * -----------------------------------------------------------------------------
 * 
 * 
 * -CHANGE LOG------------------------------------------------------------------
 *  03/02/2009 AS  Initial coding.
 * =============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.common;

import java.util.HashMap;

public class IcofAppUtil {

    private static final String APP_NAME = "IcofAppUtil";

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
    public static HashMap getHashArguments(String[] args) throws IcofException {
	HashMap hmValues = new HashMap();

	String arg = "<init>";
	String lastArg = null;

	try {
	    // Iterate thru the arguments.
	    if (args != null) {
		for (int i = 0; i < args.length; i++) {
		    arg = args[i];

		    if (lastArg != null) {

			if (arg.startsWith("-")) {
			    hmValues.put(lastArg, null);
			    lastArg = arg;
			} else {
			    hmValues.put(lastArg, arg);
			    lastArg = null;
			}
		    } else {
			lastArg = arg;
		    }
		}
	    }

	    if (lastArg != null) {
		hmValues.put(lastArg, null);
	    }

	} catch (Exception e) {
	    throw new IcofException(APP_NAME
		    + " Failed to parse the arguments.\n" + "args=" + args
		    + "\narg=" + arg + "\nlastArg=" + lastArg,
		    IcofException.SEVERE);
	} finally {
	}

	return hmValues;
    }

}
