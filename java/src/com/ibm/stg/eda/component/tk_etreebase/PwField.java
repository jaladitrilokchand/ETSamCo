/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *    FILE: PwEraserThread.java
 *
 *-PURPOSE---------------------------------------------------------------------
 * Erase the password as the user types it on the command line
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 01/17/2012 GFS  Initial Coding
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreebase;

import java.io.*;
import java.util.*;

/**
 * This class prompts the user for a password and attempts to mask input with "*"
 */

public class PwField {

    /**
     *@param input stream to be used (e.g. System.in)
     *@param prompt The prompt to display to the user.
     *@return The password as entered by the user.
     */
    public static final char[] getPassword(InputStream in, String prompt) throws IOException {
	PwMaskingThread maskingthread = new PwMaskingThread(prompt);
	Thread thread = new Thread(maskingthread);
	thread.start();

	char[] lineBuffer;
	char[] buf;

	buf = lineBuffer = new char[128];

	int room = buf.length;
	int offset = 0;
	int c;

	loop:   while (true) {
	    switch (c = in.read()) {
	    case -1:
	    case '\n':
		break loop;

	    case '\r':
		int c2 = in.read();
		if ((c2 != '\n') && (c2 != -1)) {
		    if (!(in instanceof PushbackInputStream)) {
			in = new PushbackInputStream(in);
		    }
		    ((PushbackInputStream)in).unread(c2);
		} else {
		    break loop;
		}

	    default:
		if (--room < 0) {
		    buf = new char[offset + 128];
		    room = buf.length - offset - 1;
		    System.arraycopy(lineBuffer, 0, buf, 0, offset);
		    Arrays.fill(lineBuffer, ' ');
		    lineBuffer = buf;
		}
		buf[offset++] = (char) c;
		break;
	    }
	}
	maskingthread.stopMasking();
	if (offset == 0) {
	    return null;
	}
	char[] ret = new char[offset];
	System.arraycopy(buf, 0, ret, 0, offset);
	Arrays.fill(buf, ' ');
	return ret;
    }

}