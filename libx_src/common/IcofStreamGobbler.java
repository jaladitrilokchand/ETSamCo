/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2005 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: IcofStreamGobbler.java
*
* CREATOR: Karen K. Witt
*    DEPT: 5ZIA
*    DATE: 05/04/2005
*
*-PURPOSE---------------------------------------------------------------------
* IcofStreamGobbler class definition file.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 05/04/2005 KKW  Initial coding.
* 12/15/2005 KKW  Changed class name from StreamGobbler to IcofStreamGobbler
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.common;
import java.util.Vector;


public class IcofStreamGobbler extends Thread {


  //-----------------------------------------------------------------------------
  /* Constructor
   *
   * @param     IcofStream    the stream that you want to "gobble", which
   *                          means that you want this stream to be read
   *                          while some process is writing to it.
   *                          Normally, the stream should represent stdout
   *                          or stderr.
   *
   */
  //-----------------------------------------------------------------------------
  public IcofStreamGobbler(IcofStream aStream) {

    setStream(aStream);

  }


  //-----------------------------------------------------------------------------
  // Member "getter" functions
  //-----------------------------------------------------------------------------
  /*
   * Get value of stream
   *
   */
  public IcofStream getStream() { return stream; }
  /*
   * Get contents of stream
   *
   */
  public Vector  getContents() { return stream.getContents(); }


  //-----------------------------------------------------------------------------
  /*
   * "Gobble" the stream -- ie. read the stream into a vector.  Since this
   * class extends the Thread class, when the thread is started, the run
   * method begins executing.
   */
  //-----------------------------------------------------------------------------
  public void run() {

    try {
      getStream().openRead();
      getStream().read();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }


  //-----------------------------------------------------------------------------
  // Data members
  //-----------------------------------------------------------------------------
  private IcofStream stream;


  //-----------------------------------------------------------------------------
  // Member "setter" functions
  //-----------------------------------------------------------------------------
  private void setStream(IcofStream aStream) { stream = aStream; }

}

//==========================  END OF FILE  ====================================
