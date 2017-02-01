package com.ibm.stg.iipmds.icof.component.util;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
* <pre>
* =============================================================================
*
*  Copyright: (C) IBM Corporation 2001 -- IBM Internal Use Only
*
* =============================================================================
*
*     FILE: Debugger.java
*
*  CREATOR: Aydin Suren (asuren)
*     DEPT: 5ZIA
*     DATE: 12/04/2001
*
* -PURPOSE---------------------------------------------------------------------
*  Used for debugging the classes and servlets.
*  - Simply dumps/append the supplied content to a given file
* -----------------------------------------------------------------------------
*
*
* -CHANGE LOG------------------------------------------------------------------
*  12/04/2001 AS  Initial coding.
*  04/07/2005 AS  Updated java documentation.
*  05/21/2007 RAM  Synchronized all public static methods
* =============================================================================
* </pre>
*/

public class Debugger {

    //----------------------------------------------------------------------
    /**
     * Constructor which takes a class name that is calling this class
     * and a file name that will be dumped to. It sets the private
     * class name and file name variables.
     *
     * @param clsname  Classname that is being debugged.
     * @param fname    A file to use to dump the debug statements.
     *                 It must include the entire path.
     */
    public Debugger(String clsname, String fname) {

        setClassName(clsname);
        setFilename(fname);
    }

    //----------------------------------------------------------------------
    /**
    * Prints a given string to the initialized (by constructor) file.
    *
    * @param s  A string to print.
    */

    public void print(String s) {
        FileOutputStream fos = null;
        PrintWriter out = null;

        try {
            long timestamp = System.currentTimeMillis();
            Date date = new Date(timestamp);
            SimpleDateFormat simpleDate =
                new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss:SSS");
            simpleDate.setTimeZone(TimeZone.getTimeZone("EST"));

            s = simpleDate.format(date).toString() + " *** "
                + className + " *** " + s;
            
            fos = new FileOutputStream(fileName, true);
            out = new PrintWriter(fos);
        }
        catch (Exception e) {
            System.out.println(e.toString());
        } finally {
            if (out != null){
                out.println(s);
                out.flush();
                out.close();
            }
        }
    }

    //----------------------------------------------------------------------
    /**
    * @see Debugger#print(String s)
    */
    public void print(int i) {
        print(String.valueOf(i));
    }

    //----------------------------------------------------------------------
    /**
    * @see Debugger#print(String s)
    */
    public void print(boolean i) {
        print(String.valueOf(i));
    }

    //----------------------------------------------------------------------
    /**
    * @see Debugger#print(String s)
    */
    public void print(char i) {
        print(String.valueOf(i));
    }

    //----------------------------------------------------------------------
    /**
    * @see Debugger#print(String s)
    */
    public void print(double i) {
        print(String.valueOf(i));
    }

    //----------------------------------------------------------------------
    /**
    * @see Debugger#print(String s)
    */
    public void print(float i) {
        print(String.valueOf(i));
    }

    //----------------------------------------------------------------------
    /**
    * @see Debugger#print(String s)
    */
    public void print(long i) {
        print(String.valueOf(i));
    }

    //----------------------------------------------------------------------
    /**
    * @see Debugger#print(String s)
    */
    public void print(short i) {
        print(String.valueOf(i));
    }

    //----------------------------------------------------------------------
    /**
    * @see Debugger#print(String s)
    */
    public void print(Object i) {
        print(String.valueOf(i));
    }

    //----------------------------------------------------------------------
    /**
     * Main: Used only for testing purpose from the command line.
     *
     * @param args The command line arguments.
     */
    public static synchronized void main(String args[]) {
        Debugger debugger = new Debugger("Debugger"
                ,"/afs/btv/data/aim/icof/JavaApps/acosDev/Acos/debug.txt");

        debugger.print("Test");
        debugger.print(debugger);
    }

    //---------------------------------------------------------------------
    // Members - private
    //---------------------------------------------------------------------

    /**
     * The file that will be used for writing into.
     */
    private String fileName;

    /**
     * Caller class name
     */
    private String className;

    //---------------------------------------------------------------------
    // Methods - private setters
    //---------------------------------------------------------------------

    private void setFilename(String aFilename) {

        fileName = aFilename;
    }

    private void setClassName(String aClassName) {

        className = aClassName;
    }

}
