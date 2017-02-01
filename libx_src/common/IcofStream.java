/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2001 -- IBM Internal Use Only
*
*=============================================================================
*
*    FILE: IcofStream.java
*
* CREATOR: Karen K. Kellam
*    DEPT: 5ZIA
*    DATE: 12/14/2001
*
*-PURPOSE---------------------------------------------------------------------
* IcofStream class definition file.
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 12/14/2001 KKK  Initial coding.
* 03/20/2002 KKK  Converted to Java 1.2.2.
* 05/25/2005 KKW  Added "implements Serializable".
* 12/15/2005 KKW  Modified due to splitting of Constants.java into several
*                 *Util classes.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.common;
import java.util.*;
import java.io.*;


public class IcofStream implements Serializable {


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from application when nothing
   *   is known, but will be set later.
   */
  //-----------------------------------------------------------------------------
  public IcofStream() {

    this(null
         ,null);

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from application when dealing
   *   with an input stream only.
   *
   * @param     anInStream        an input stream to be managed
   */
  //-----------------------------------------------------------------------------
  public IcofStream(InputStream anInStream) {

    this(anInStream
         ,null);

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from application when dealing
   *   with an output stream only.
   *
   * @param     anOutStream        an output stream to be managed
   */
  //-----------------------------------------------------------------------------
  public IcofStream(OutputStream anOutStream) {

    this(null
         ,anOutStream);

  }


  //-----------------------------------------------------------------------------
  /**
   * Constructor - used to instantiate object from application when dealing
   *   with an input stream and an output stream.
   *
   * @param     anInStream        an input stream to be managed
   * @param     anOutStream       an output stream to be managed
   */
  //-----------------------------------------------------------------------------
  public IcofStream(InputStream anInStream
                    ,OutputStream anOutStream) {

    setInStream(anInStream);
    setOutStream(anOutStream);
    setReader(null);
    setWriter(null);
    Vector tmpVector = new Vector();
    setContents(tmpVector);

  }


  //-----------------------------------------------------------------------------
  // Member "getter" functions
  //-----------------------------------------------------------------------------
  /**
   * Get value of inStream
   *
   */
  public InputStream getInStream() { return inStream; }
  /**
   * Get value of outStream
   *
   */
  public OutputStream getOutStream() { return outStream; }
  /**
   * Get contents of stream
   *
   */
  public Vector  getContents() { return contents; }
  /**
   * Get value of reader
   *
   */
  protected BufferedReader getReader() { return reader; }
  /**
   * Get value of writer
   *
   */
  protected PrintWriter getWriter() { return writer; }


  //-----------------------------------------------------------------------------
  // Member "setter" functions
  //-----------------------------------------------------------------------------
  /**
   * Set contents read from stream to to be written to stream
   *
   */
  public void setContents(Vector aVector) { contents = aVector; }


  //-----------------------------------------------------------------------------
  /**
   * Add a line to the contents Vector.
   *
   * @param     aLine             add a line to the contents of the stream
   *                              (either being read or to be written)
   * @exception IcofException     Unable to add line to vector
   */
  //-----------------------------------------------------------------------------
  public void addLine(String aLine) throws IcofException {

    String funcName = new String("addLine(String)");

    contents.add(aLine);

  }


  //-----------------------------------------------------------------------------
  /**
   * Close the stream for reading.
   *
   * @exception IcofException     Unable to close the stream being read
   */
  //-----------------------------------------------------------------------------
  public void closeRead() throws IcofException {

    String funcName = new String("closeRead()");

    // Make sure the stream is opened for reading. If it is,
    //   close it.
    try {
      if (isOpenRead()) {
        getReader().close();
        setReader(null);
      }
    }
    catch (Exception e) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,e.toString()
                                           ,"");
      throw ie;
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Close the stream for writing.
   */
  //-----------------------------------------------------------------------------
  public void closeWrite() {

    String funcName = new String("closeWrite()");

    // Make sure the stream is opened for writing. If it is,
    //   close it.
    if (isOpenWrite()) {
      getWriter().close();
      setWriter(null);
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Determine whether or not this stream is already open.
   *
   * @return                      true, if the stream is open; false, if not
   */
  //-----------------------------------------------------------------------------
  public boolean isOpen() {

    String funcName = new String("isOpen()");

    //
    // Check to see if the stream has been opened for reading or writing.
    //
    boolean open = false;
    if ((isOpenRead()) || (isOpenWrite())) {
      open = true;
    }
    return open;

  }


  //-----------------------------------------------------------------------------
  /**
   * Determine whether or not this stream is already opened for reading.
   *
   * @return                      true, if the stream is open for reading;
   *                              false, if not
   */
  //-----------------------------------------------------------------------------
  public boolean isOpenRead() {

    String funcName = new String("isOpenRead()");

    //
    // Check to see if the stream has been opened for reading.
    //
    boolean open = false;
    if (getReader() != null) {
      open = true;
    }
    return open;

  }


  //-----------------------------------------------------------------------------
  /**
   * Determine whether or not this stream is already opened for writing.
   *
   * @return                      true, if the stream is open for writing;
   *                              false, if not
   */
  //-----------------------------------------------------------------------------
  public boolean isOpenWrite() {

    String funcName = new String("isOpenWrite()");

    //
    // Check to see if the stream has been opened for writing.
    //
    boolean open = false;
    if (getWriter() != null) {
      open = true;
    }
    return open;

  }


  //-----------------------------------------------------------------------------
  /**
   * Open the stream for reading.  If the input stream object is null,
   *   throw an exception.
   *
   * @exception IcofException     Unable to open input stream for reading
   */
  //-----------------------------------------------------------------------------
  public void openRead() throws IcofException {

    String funcName = new String("openRead()");

    if (getInStream() == null) {
      String msg = new String("No input stream specified when object was constructed");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,"");
      throw ie;
    }

    // Make sure the Stream is not currently opened. If it is,
    //   throw an exception.
    if (isOpen()) {
      String msg = new String("Stream is already open.");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,"");
      throw ie;
    }

    //
    // If we get here, then the stream exists and is not open.  Open it for
    //   reading.
    //
    try {
      setReader(new BufferedReader(new InputStreamReader(getInStream())));
    }
    catch (Exception e) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,e.toString()
                                           ,"");
      throw ie;
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Open the Stream for writing.  If the output stream is null, throw
   *   an exception.
   *
   * @exception IcofException     Unable to open input stream for writing
   */
  //-----------------------------------------------------------------------------
  public void openWrite() throws IcofException {

    String funcName = new String("openWrite()");

    if (getOutStream() == null) {
      String msg = new String("No output stream specified when object was constructed");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,"");
      throw ie;
    }

    //
    // Make sure the stream is not currently opened. If it is,
    //   throw an exception.
    //
    if (isOpen()) {
      String msg = new String("Stream is already open.");
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,msg
                                           ,"");
      throw ie;
    }

    //
    // Open the file for writing.
    //
    try {
      setWriter(new PrintWriter(new OutputStreamWriter(getOutStream()), true));
    }
    catch (Exception e) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,e.toString()
                                           ,"");
      throw ie;
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Read the stream into a vector.  Return the vector.
   *
   * @return                      the contents read from the stream
   * @exception IcofException     Unable to read the input stream
   */
  //-----------------------------------------------------------------------------
  public Vector read() throws IcofException {

    String funcName = new String("read()");

    // Clear the contents vector.
    getContents().clear();

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(getInStream()));

      String thisLine = in.readLine();
      while (thisLine != null) {

        addLine(thisLine);

        thisLine = in.readLine();

      }

      // Close the stream.
      in.close();
      return getContents();

    }
    catch (Exception e) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,e.toString()
                                           ,"");
      throw ie;
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Read a single line from the stream and add the line to the contents vector.
   *   If end-of-file is reached, close the file.  Return the line read.
   *
   * @return                      the line read from the stream
   * @exception IcofException     Unable to read the input stream
   */
  //-----------------------------------------------------------------------------
  public String readLine() throws IcofException {

    String funcName = new String("readLine()");

    // Make sure the stream is opened for reading.
    if (!isOpenRead()) {

      //   Clear the contents vector, since we will be reading into it.
      getContents().clear();

      openRead();
    }

    //
    // Read a line from the stream.  If end-of-file is reached, close the stream.
    //
    String thisLine = new String("");

    try {
      thisLine = reader.readLine();
    }
    catch (Exception e) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,e.toString()
                                           ,"");
      throw ie;
    }

    if (thisLine == null) {
      closeRead();
    }
    else {
      addLine(thisLine);
    }
    return thisLine;

  }


  //-----------------------------------------------------------------------------
  /**
   * Write the contents (vector) to the stream represented by this object.
   *
   * @exception IcofException     Unable to write to the output stream
   */
  //-----------------------------------------------------------------------------
  public void write() throws IcofException {

    String funcName = new String("write()");
    write(getContents());

  }


  //-----------------------------------------------------------------------------
  /**
   * Write the contents of the specified vector to the stream represented by
   *   this object.
   *
   * @param     writeVector       the data to write to the output stream
   * @exception IcofException     Unable to write to the output stream
   */
  //-----------------------------------------------------------------------------
  public void write(Vector writeVector) throws IcofException {

    String funcName = new String("write(Vector)");

    try {
      PrintWriter out = new PrintWriter(new OutputStreamWriter(getOutStream())
                                        ,true);

      Iterator iter = writeVector.iterator();
      while (iter.hasNext()) {

        String thisLine = (String) iter.next();

        out.println(thisLine);

      }

      // Close the stream.
      out.close();

    }
    catch(Exception e) {
      IcofException ie = new IcofException(this.getClass().getName()
                                           ,funcName
                                           ,IcofException.SEVERE
                                           ,e.toString()
                                           ,"");
      throw ie;
    }

  }


  //-----------------------------------------------------------------------------
  /**
   * Write a single line to the stream.
   *   If the line is null, close the stream.
   *
   * @param     thisLine          the line to write to the output stream
   * @exception IcofException     Unable to write to the output stream
   */
  //-----------------------------------------------------------------------------
  public void writeLine(String thisLine) throws IcofException {

    String funcName = new String("writeLine(String)");

    // Make sure the stream is opened for writing.
    if (!isOpenWrite()) {
      openWrite();
    }

    //
    // Write the line to the stream.  If the line is null, close the stream.
    //
    if (thisLine == null) {
      closeWrite();
    }
    else {
      getWriter().println(thisLine);
    }

  }


  //-----------------------------------------------------------------------------
  // Data members
  //-----------------------------------------------------------------------------
  private InputStream inStream;
  private OutputStream outStream;
  private Vector   contents;
  private PrintWriter writer;
  private BufferedReader reader;


  //-----------------------------------------------------------------------------
  // Member "setter" functions
  //-----------------------------------------------------------------------------
  protected void setReader(BufferedReader aReader) { reader = aReader; }
  protected void setWriter(PrintWriter aWriter) { writer = aWriter; }
  private void setInStream(InputStream aStream) { inStream = aStream; }
  private void setOutStream(OutputStream aStream) { outStream = aStream; }

}

//==========================  END OF FILE  ====================================
