/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 *    FILE: PsEraserThread.java
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

/**
 * This class attempts to erase characters echoed to the console.
 */
class PwMaskingThread extends Thread {


    private volatile boolean stop;
    private char echochar = '*';


    /**
     *@param prompt The prompt displayed to the user
     */
    public PwMaskingThread(String prompt) {
	System.out.print(prompt);
    }


    /**
     * Begin masking until asked to stop.
     */
    public void run() {

	int priority = Thread.currentThread().getPriority();
	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

	try {
	    stop = true;
	    while(stop) {
		System.out.print("\010" + echochar);
		try {
		    // attempt masking at this rate
		    Thread.sleep(1);
		}catch (InterruptedException iex) {
		    Thread.currentThread().interrupt();
		    return;
		}
	    }
	} finally { // restore the original priority
	    Thread.currentThread().setPriority(priority);
	}
    }


    /**
     * Instruct the thread to stop masking.
     */
    public void stopMasking() {
	this.stop = false;
    }

}