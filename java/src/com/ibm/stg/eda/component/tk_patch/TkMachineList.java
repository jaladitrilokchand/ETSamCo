/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2010 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 03/15/2010
*
*-PURPOSE---------------------------------------------------------------------
* ToolKit machine list class.
*-----------------------------------------------------------------------------
*
*-CHANGE LOG------------------------------------------------------------------
* 03/15/2010 GFS  Initial coding.
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_patch;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofStringUtil;
import com.ibm.stg.iipmds.common.IcofSystemUtil;


public class TkMachineList {

    /*
     * Constructor
     */
    public TkMachineList () {
        setMachineList();
    }
    
    
    /*
     * Constants.
     */
    public static final String CLASS_NAME = "TkMachineList";
    public static final String MACHINE_FILE = "/afs/eda/u/cmlib/public/machinelist";
    public static final String UPTIME_CMD = "uptime";
    public static final double MAX_LOAD = 99.0;
    

    /*
     * Members
     */
    private Hashtable<String,TkMachine> machineList;
    
    
    /*
     * Getters
     */
    private Hashtable <String,TkMachine>getMachineList() { return machineList; }
    public String getFastestMachineName(String os, int size) throws IcofException { 
        TkMachine fastestMachine = getFastestMachine(os, size);
        if (fastestMachine != null)
            return fastestMachine.getHostName(); 
        
        return "Unable to determine fastest machine ...";

    }
    public String getFastestMachineName(String fullOsName) throws IcofException {
        int size = 32;
        String os = "";
        if (fullOsName.indexOf("-") > -1) {
            String sizeText = IcofStringUtil.getField(fullOsName, 1, "-");
            os = IcofStringUtil.getField(fullOsName, 2, "-");
            size = Integer.parseInt(sizeText);
        }
        else {
            os = fullOsName;
        }

        return getFastestMachineName(os, size);
        
    }

    
    /**
     * Determine the fastest machine in the machine list or the first one with
     * an load greater than 0 and less than 2.
     * 
     * @param os  The desired os name
     * @param size  The desired machine size (32 or 64 bit)
     * @return fastestMachine
     * @throws IcofException 
     */
    private TkMachine getFastestMachine(String os, int size) throws IcofException {

        double bestTime = MAX_LOAD;
        TkMachine bestMachine = null;
        
        // Set the technology level for aix machines.
        if (os.indexOf("rs_aix") > -1) {
            if (os.indexOf("tl0") < 0) 
                os += "tl05";
        }
        
        // Set the desired machine type. If its 32 bit then it can run on a 
        // 32 or 64 bit machine.
        String type1 = os + "##" + size;
        String type2 = "";
        if (size == 32) {
            type2 = os + "##" + 64;
        }
        
        // Iterate through the machine list querying each one for its uptime
        Iterator<TkMachine> iter = getMachineList().values().iterator();
        while (iter.hasNext()) {
            TkMachine thisMach =  iter.next();
            //System.out.println("Querying machine: " + thisMach.getName());
            //System.out.println(" OS: " + thisMach.getOs());
            if (thisMach.getOs().equals(type1) || 
                (thisMach.getOs().equals(type2))) {
                double thisTime = getUptime(thisMach);
                if ((thisTime > 0.0) && (thisTime < 2.0)) {
                    return thisMach;
                }
                if (thisTime < bestTime) {
                    bestTime = thisTime;
                    bestMachine = thisMach;
                }
            }
        }
        
        return bestMachine;
        
    }
    
    
    /**
     * Determine the load average for the specified machine.
     * 
     * @param myMach
     * @return
     * @throws IcofException 
     */
    private double getUptime(TkMachine myMach) throws IcofException {
        
        // Determine if the machine is available.
        if (! pingMachine(myMach)) {
            return MAX_LOAD;
        }
        
        // Construct the command to query this machine for its load average.
        String command = TkInjectUtils.RSH + " " + myMach.getName() 
                         + " " + UPTIME_CMD;
        //System.out.println(" Running: " + command);
        
        // Run the command.
        Vector<String> results = new Vector<String>();
        StringBuffer errorMsg = new StringBuffer();
        int rc = IcofSystemUtil.execSystemCommand(command, errorMsg, results);
        //System.out.println(" Results: " + results.toString());
        
        
        // If there were problems then return the max load.
        if (rc != 0) {
            return MAX_LOAD;
        }
        
        // Parse the results.
        if (results.size() > 0) {
            String result = (String) results.firstElement();
            //System.out.println(" Uptime result: " + result);

            int index = result.indexOf("load average: ");
            String loads = result.substring(index + 14);
            //System.out.println(" Load nums: " + loads);
            Vector<String> tokens = new Vector<String> ();
            IcofCollectionsUtil.parseString(loads, ", ", tokens, true);
            //System.out.println(" Num load stats: " + tokens.size());
            if (tokens.size() >= 3) {
                double totalLoad = 0.0;
                for (int i = 0; i < tokens.size(); i++) {
                    String load = (String) tokens.get(i);
                    //System.out.println(" Load: " + load);
                    totalLoad += Double.parseDouble(load);
                }
                double avgLoad = totalLoad / tokens.size();
                //System.out.println(" Avg load: " + avgLoad);
                
                return avgLoad;
            }
        }
            
        return MAX_LOAD;
        
    }
    
    
    /**
     * @param myMach
     * @return
     * @throws IcofException 
     */
    private boolean pingMachine(TkMachine myMach) throws IcofException {

        // Construct the command to query this machine for its load average.
        String command = "ping -c 1 " + myMach.getName();

        // Run the command.
        Vector<String> results = new Vector<String>();
        StringBuffer errorMsg = new StringBuffer();
        int rc = IcofSystemUtil.execSystemCommand(command, errorMsg, results);

        if (rc != 0) {
            return false;
        }
        
        // Determine the machines full name
        Iterator<String> iter = results.iterator();
        while (iter.hasNext()) {
            String line =  iter.next();
            if (line.indexOf("PING") > -1) {
                String hostName = IcofStringUtil.getField(line, 2, " ");
                myMach.setHostName(hostName);
            }
        }
        
        return true;

    }
    
    
    /*
     * Setters
     */
    private void setMachineList() { 
        try {
            readMachineList();
        }
        catch(IcofException trap) {}
    }
    
    
    /*
     * Read the machine list file in the hash.
     */
    private void readMachineList() throws IcofException {
        
        // Initialize the machine list.
        if (machineList == null) {
            machineList = new Hashtable<String,TkMachine>();
        }
        else {
            machineList.clear();
        }
        
        // Read the machine list file.
        IcofFile machFile = new IcofFile(MACHINE_FILE, false);
        if (machFile.exists()) {
            machFile.openRead();
            machFile.read();
            machFile.closeRead();
        }
        //System.out.println("Reading: " + machFile.getAbsolutePath());
        
        
        // Parse the results.
        Iterator<String> iter = machFile.getContents().iterator();
        String platform = "";
        String machName = "";
        String numBits = "0";
        String numCpus = "0";
        while (iter.hasNext()) {
            String line = (String) iter.next();
            //System.out.println("--> " + line);
            if (line.indexOf(" => [" ) > -1) {
                platform = getValue(line, 1);
                //System.out.println(" Platform: " + platform);
            }
            else if (line.indexOf("'name'") > -1) {
                machName = getValue(line, 3);
                //System.out.println(" Name: " + machName);
            }
            else if (line.indexOf("'bits'") > -1) {
                numBits = getValue(line, 3);
                //System.out.println(" Bits: " + numBits);
            }
            else if (line.indexOf("'cpus'") > -1) {
                numCpus = getValue(line, 3);
                //System.out.println(" Cpus: " + numCpus);
                
                TkMachine myMach = new TkMachine(machName, platform, 
                                                 Integer.parseInt(numBits),
                                                 Integer.parseInt(numCpus));
                //System.out.println("Machine: " + myMach.getName() + " " 
                //                   + myMach.getOs() + " " + myMach.getNumCpus());
                machineList.put(machName, myMach);
                
                machName = "";
                numBits = "0";
                numCpus = "0";

            }
            
        }
        
    }


    /**
     * Parses the line and returns the specified token.  
     * @param aLine
     * @param position
     * @param delimiter
     * @param removeChar
     * @return
     * @throws IcofException 
     */
    private String getValue(String aLine, int position) throws IcofException {

        // Trim any trailing or leading whitespace.
        aLine = aLine.trim();
        
        // Parse the line.
        String result = IcofStringUtil.getField(aLine, position, " ");
        
        // Remove the removeChar;
        int start = result.indexOf("'");
        int end = result.lastIndexOf("'");
        if ((start != -1) && (end != -1)) {
            result = result.substring(start + 1, end);
        }
        else {
            end = result.lastIndexOf(",");
            if (end != -1) {
                result = result.substring(0, end);
            }
        }
        
        return result;
    }

    
    // Helper class to store the machine data.
    private static class TkMachine {
        private String myName;
        private String osName;
        private String hostName;
        private int numBits;
        private int numCpus;

        public TkMachine (String aName, String os, int bits, int cpus) {
            myName = aName;
            numBits = bits;
            numCpus = cpus;
            osName = os;
        }
        public String getName() { return myName; }
        public String getHostName() { return hostName; }
        public String getOs() { return osName + "##" + String.valueOf(numBits); }
        public void setHostName(String aName) { hostName = aName; }
    }
    
}

