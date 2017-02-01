/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2014 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *-PURPOSE---------------------------------------------------------------------
 * ETREE log file format
 *-----------------------------------------------------------------------------
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 06/18/2014 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

package com.ibm.stg.eda.component.tk_etreebase;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class EtreeLogFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {

	String reply;
	if (record.getLevel().equals(Level.INFO) ||
	    record.getLevel().equals(Level.FINE)) {
	    reply = record.getMessage();
	}
	else {
	    reply = "\n" + record.getLevel() + ": " + record.getMessage() + "\n";
	    reply += "==> logged in .. " + record.getSourceClassName() + 
	    "@" + record.getSourceMethodName();
	}

	return reply + "\n";

    }


}