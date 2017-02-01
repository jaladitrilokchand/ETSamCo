/**
* <pre>
*=============================================================================
*
* Copyright: (C) IBM Corporation 2009 -- IBM Internal Use Only
*
*=============================================================================
*
* CREATOR: Gregg Stadtlander
*    DATE: 12/23/2009
*
*-PURPOSE---------------------------------------------------------------------
* Class for EDA Tool Kit LEVELHIST file data
*-----------------------------------------------------------------------------
*
*
*-CHANGE LOG------------------------------------------------------------------
* 12/23/2009 GFS  Initial coding.
* 02/18/2010 GFS  Added support for extracting tracks from CMVC
* 03/04/2010 GFS  Merged changes from bugfix and test streams. 
*=============================================================================
* </pre>
*/

package com.ibm.stg.eda.component.tk_levelhist;

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_patch.TkInjectRequest;
import com.ibm.stg.eda.component.tk_patch.TkInjectUtils;
import com.ibm.stg.eda.component.tk_patch.TkPatch;
import com.ibm.stg.eda.component.tk_patch.TkSource;
import com.ibm.stg.iipmds.common.IcofCollectionsUtil;
import com.ibm.stg.iipmds.common.IcofException;
import com.ibm.stg.iipmds.common.IcofFile;
import com.ibm.stg.iipmds.common.IcofSystemUtil;

public class LevelHist extends IcofFile {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 6679725510084833866L;
	
	/**
     * Constructor
     * 
     * @param aPath  Full path to LEVELHIST file.
     */
    public LevelHist(String aPath) {
        super(aPath, false);
    }

    
    /* 
     * Getters
     */
    public StringBuffer getNewHeader() { return newHeader; }
    public Vector<String> getNewContents() { return newContents; }
    public String getNewContentsAsString() { 
        
        // Iterate through the new contents vector.
        StringBuffer contents = new StringBuffer();
        for (int i = 0; i < getNewContents().size(); i++) {
            contents.append((String) getNewContents().get(i) + "\n");
        }
        
        return contents.toString(); 
        
    }
    
    
    /*
     * Read the file contents
     * 
     * @param force  If true reread the file anyway/
     */
    public void readFile(boolean force) throws IcofException {
        
        // If the file has already been read then return unless its a forced
        // read.
        if ((getContents() != null) && (getContents().size() > 0)) {
            if (! force)
                return;
        }

        // Read the file.
        try {
            openRead();
            read();
            closeRead();
        } catch(IcofException ex) {
            throw ex;
        }
        finally {
            closeRead();
        }

    }
    

    /*
     * Initialize/clears the header StringBuffer.
     */
    public void initializeNewHeader() {
        if (getNewHeader() != null) {
            newHeader = null;
        }

        newHeader = new StringBuffer();
        
    }


    /*
     * Construct a new date entry for today.
     */
    public static String constructDate() throws IcofException {
        
        String command = "date";
        
        // Run the command.
        StringBuffer errorMsg = new StringBuffer();
        Vector<String> results = new Vector<String>();
        int rc = IcofSystemUtil.execSystemCommand(command, errorMsg, results);
        
        String date = "";
        if (rc == 0) {
            if (results.size() > 0) {
                date = (String) results.firstElement();
                date = date.trim();
            }
        }
        
        return date;
        
    }
  
    
    /**
     * Set the new LEVELHIST contents.
     * 
     * @param selectedRequests Collection of TkInjectRequest objects.
     * @param injectLocation   Name of injection location (shipb | tkb)
     * @throws IcofException 
     */
    public void setNewContents(HashSet<TkInjectRequest> selectedRequests,  
                                String injectLocation, TkPatch patch,
                                boolean extractTracksFromCmvc) 
    throws IcofException {
        
        // Use this message if all tracks are extracted from CMVC.
        if (extractTracksFromCmvc) {
            if (newContents == null) {
                newContents = new Vector<String>();
            }
            else {
                newContents.clear();
            }
            newContents.add("LEVELHIST entries will be created when");
            newContents.add("pd_level_extract.NEW command is run.");
            return;
        }
        
        // Set inject file SID, type and developer based on the file's
        // source location.
        Hashtable<String, CMVC_File> newFiles = 
        	getNewFileDetails(selectedRequests, patch);
        //System.out.println("File count: " + newFiles.size());
        
        // Add the LEVELHIST header for this injection.
        newContents = LevelHistUtils.getNewHeader(injectLocation);
        
        // Add entries for each request and track.
        Iterator<TkInjectRequest> requests = selectedRequests.iterator();
        while (requests.hasNext()) {
            TkInjectRequest myRequest = (TkInjectRequest) requests.next();
            
            Iterator<String> iter2 = myRequest.getAllTracks().iterator();
            while (iter2.hasNext()) {
                String track = (String) iter2.next();
                
                // Find the matching CMVC track.
                CMVC_Track myTrack = (CMVC_Track) patch.getCmvcTracks().get(track);
                
                // Add the track line.
                String trackLine = track + " (" + myTrack.getChangeType() + ") "
                                   + myTrack.getAbstractText();
                newContents.add(trackLine);
                
                // Add the requester line.
                String reqLine = "Requesting Developer: " + myRequest.getDeveloper();
                newContents.add(reqLine);
                
                // Add the file line(s).
                Hashtable<String, String> copyLines = new Hashtable<String, String>();
                Vector<String> fileLines = new Vector<String>();

                Iterator<CMVC_File> files = myTrack.getFiles().values().iterator();
                while (files.hasNext()) {
                    CMVC_File cmvcFile = (CMVC_File) files.next();
                    TkSource srcFile = (TkSource) myRequest.getSourceFiles().get(cmvcFile.getPath());
                    
                    // Save this source file if it is active.
                    if (srcFile.isActive()) {
                        String copyLine = "Copied from: " + srcFile.getFullPath();  
                        copyLines.put(srcFile.getFullPath(), copyLine);
                        
                        // Determine the updated SID, type and developer injected file.
                        String fileLine = "";
                        if (newFiles.containsKey(cmvcFile.getPath())) {
                            CMVC_File newFile = (CMVC_File) newFiles.get(cmvcFile.getPath());
                            fileLine = newFile.getSid() + "  " + 
                            cmvcFile.getPath() + "  " + 
                            newFile.getType() + "  " + 
                            newFile.getDeveloper();
                        }
                        else {
                            fileLine = cmvcFile.getSid() + "  " + 
                            cmvcFile.getPath() + "  " + 
                            cmvcFile.getType() + "  " + 
                            cmvcFile.getDeveloper();
                        }
                    
                        // Save this source file if it is active.
                        fileLines.add(fileLine);
                        
                    }
                    
                }
                
                // Add the copy and file lines.
                Iterator<String> iter = copyLines.values().iterator();
                while (iter.hasNext()) {
                    String line = (String) iter.next();
                    newContents.add(line);
                }
                newContents.addAll(fileLines);
                
                // Add a blank line.
                newContents.add("");
                
            }
            
        }
        
    }


    /**
     * Creates a collection of CMVC_File objects for each to be injected file.
     * These objects have the corrected SID, file type and developer names.
     * 
     * @param selectedRequests
     * @param patch
     * @return Collection of CMVC_File objects.
     * @throws IcofException 
     * 
     */
    private Hashtable<String, CMVC_File> getNewFileDetails(HashSet<TkInjectRequest> selectedRequests,
                                        TkPatch patch)
    throws IcofException {
        
        Hashtable<String, Vector<CMVC_File>> sortedFiles = 
        	new Hashtable<String, Vector<CMVC_File>>();
        Hashtable<String, CMVC_File> newFiles = new Hashtable<String, CMVC_File>();
        
        // Convert the request collection into a collection of to be injected
        // files.  Sort the files by source location.
        Iterator<TkInjectRequest> requests = selectedRequests.iterator();
        while (requests.hasNext()) {
            TkInjectRequest request = (TkInjectRequest) requests.next();
            //System.out.println("Request: " + request.getId());
            
            Iterator<String> tracks = request.getAllTracks().iterator();
            while (tracks.hasNext()) {
                String trackName = (String) tracks.next();
                //System.out.println("Track: " + trackName);
                CMVC_Track track = (CMVC_Track) patch.getCmvcTracks().get(trackName);
                //System.out.println(" File count(track): " + track.getFiles().size());
                
                Iterator<CMVC_File> iter = track.getFiles().values().iterator();
                while (iter.hasNext()) {
                    CMVC_File cmvcFile = (CMVC_File) iter.next();
                    TkSource srcFile = (TkSource) request.getSourceFiles().get(cmvcFile.getPath());
                    
                    if (srcFile.isActive()) {
                        String repository = srcFile.getRepository();
                        
                        // Add this file to the sorted collection
                        Vector<CMVC_File> files;
                        if (sortedFiles.containsKey(repository)) {
                            files = (Vector<CMVC_File>) sortedFiles.get(repository);
                        }
                        else {
                            files = new Vector<CMVC_File>();
                        }
                        files.add(cmvcFile);
                        sortedFiles.put(repository, files);
                    }

                }
                
            }
            
        }
        
        // Process each repository specific file collection.
        Iterator<String> iter = sortedFiles.keySet().iterator();
        while (iter.hasNext()) {
            String repository = (String) iter.next();
            Vector<CMVC_File> files = (Vector<CMVC_File>) sortedFiles.get(repository);
            //System.out.println("Repository: " + repository);
            //System.out.println(" Sorted file count: " + files.size());
            
            Hashtable<String, CMVC_File> updatedFiles = 
            	updateFileDetails(repository, files);
            newFiles.putAll(updatedFiles);
            
        }
        
        return newFiles;
        
    }


    /**
     * Update the CMVC_File objects in the files collection based on the 
     * repository.  If repository is build/dev/prod/shipb then read that 
     * LEVELHIST file and locate the last entry of the file.  Use that data to
     * update the CMVC_File object. If repository is OTHER then do the same 
     * thing but use the target LEVELHIST contents. If repository is CMVC then
     * do nothing.
     * 
     * @param repository
     * @param files
     * @throws IcofException 
     */
    private Hashtable<String, CMVC_File> updateFileDetails(String repository, 
                                                           Vector<CMVC_File> files)
    throws IcofException {

        Hashtable<String, CMVC_File> newFiles = new Hashtable<String, CMVC_File>();
        boolean addPlus = false;

        // Set the LEVELHIST file to the correct repository.
        LevelHist lhFile;
        if (repository.equals(TkInjectUtils.CMVC_FILE_EXT)) {
            return newFiles;
        }
        if (repository.equals(TkInjectUtils.CMVC_TRACK_EXT)) {
            return newFiles;
        }
        else if (repository.equals(TkInjectUtils.OTHER)) {
            lhFile = this;
            addPlus = true;
        }
        // Handle build/dev/prod/shipb.
        else {
            lhFile = new LevelHist(repository + File.separator + 
                                   TkInjectUtils.LEVEL_HIST);
        }
        
        // Create a collection of path names to look for.
        Hashtable<String, String> searchFor = new Hashtable<String, String>();
        Iterator<CMVC_File> iter = files.iterator();
        while (iter.hasNext()) {
            CMVC_File file = (CMVC_File) iter.next();
            searchFor.put(file.getPath(), "");
        }
        
        // Read the LEVELHIST file.
        lhFile.readFile(false);
                
        // Search the LEVELHIST file for the paths from the bottom to the top.
        for (int i = lhFile.getContents().size()-1; (i >= 0) && (searchFor.size() > 0); i--) {
            String line = (String) lhFile.getContents().get(i);
            Iterator<String> iter2 = searchFor.keySet().iterator();
            boolean foundPath = false;
            String path = "";
            while (iter2.hasNext() && !foundPath) {
                path = (String) iter2.next();
                if (line.indexOf(path) > -1) {
                    CMVC_File newFile = parseLine(line.trim(), path, addPlus);
                    newFiles.put(path, newFile);
                    foundPath = true;
                }
            }
            if (foundPath) {
                searchFor.remove(path);
            }

            // If path not found then use default values.
            else {
                CMVC_File newFile = new CMVC_File("", path, "1.1+", "create",
                                                  "requester", null);
                newFiles.put(path, newFile);
            }
            
        }
        
        return newFiles;
        
    }


    /**
     * Parse a LEVELHIST line into a CMVC_File object. This method works for
     * einstimer, hdp and pds LEVELHIST files.
     * 
     * @param line
     * @param path
     * @param addPlus
     * @return
     * @throws IcofException 
     */
    private CMVC_File parseLine(String line, String path, boolean addPlus) throws IcofException {
        
        //System.out.println("Line: " + line);
        
        // Determine the spacing (1 space or 2).
        String delimiter = "  ";
        boolean pdsStyle = false;
        if (line.indexOf("[") > -1) {
            delimiter = " ";
            pdsStyle = true;
        }
        
        // Parse the line.
        Vector<String> tokens = new Vector<String>();
        IcofCollectionsUtil.parseString(line, delimiter, tokens, true);
        
        // Get the sid, type and developer.
        String sid = "";
        String type = "";
        String developer = "";
        if (pdsStyle) {
            if (tokens.size() >= 2)
                sid = (String) tokens.get(2);
            if (sid.indexOf(")") > -1) {
                sid = sid.substring(0, sid.indexOf(")"));
            }
            if (tokens.size() >= 4)
                type = (String) tokens.get(4);
            for (int i = 5; i < tokens.size(); i++) {
                developer += (String) tokens.get(i);
                if (i < tokens.size()-1) {
                    developer += " ";
                }   
            }
        }
        else {
            String firstToken = "";
            if (tokens.size() > 0) 
                firstToken = (String) tokens.firstElement();
            if  (firstToken.equals(path) && tokens.size() >= 2) {
                sid = (String) tokens.get(1);
            }
            else {
                if (tokens.size() > 0)
                    sid = (String) tokens.get(0);
            }
            if (tokens.size() >= 3) {
                type = (String) tokens.get(2);
            }
            if (tokens.size() >= 4) {
                developer = (String) tokens.get(3);
            }
        }
        
        // Add the "+" to SID if requested.
        if (addPlus) {
            sid += "+";
        }
        
        // Create the new CMVC_File object.
        CMVC_File newFile = new CMVC_File(line, path, sid, type, developer, null);
        
        return newFile;
    }

    
    /**
     * Add the new contents to the current file contents and write the 
     * LEVELHIST file. 
     * 
     * @param patch  The TkPatch object for this injection
     * @param debug  If true don't update the real level hist file.
     * @throws IcofException 
     */
    public void appendNewContents(TkPatch patch, boolean debug) throws IcofException {
        
        patch.logIt("\nNew LEVELHIST contents\n--------------------", false);
        
        // Add the new contents to the existing LEVELHIST file.
        if (! debug) {
            try {
                openAppend();
                write(getNewContents(), true);
                patch.logIt(getNewContentsAsString(), false);
            } catch(IcofException ex) {
                throw ex;
            }
            finally {
                closeAppend();
            }

        }
        else {
            
            // Copy the current LH file to LH.debug.
            IcofFile otherLh = new IcofFile(this.getAbsolutePath() + ".debug",
                                            false);
            copy(otherLh, true);
            
            // Append the contents to LH.debug
            LevelHist otherLevelHist = new LevelHist(otherLh.getAbsolutePath());
            try {
                otherLevelHist.openAppend();
                otherLevelHist.write(getNewContents(), true);
                patch.logIt(getNewContentsAsString(), false);
            } catch(IcofException ex) {
                throw ex;
            }
            finally {
                otherLevelHist.closeAppend();
            }

            patch.logIt("DEBUG: Updates made to LEVELHIST: " 
                        + otherLh.getAbsolutePath() + "\n",
                        true);
            
        }
            
    }
        

    /*
     * Members
     */
    private StringBuffer newHeader;
    private Vector<String> newContents;

    
    /*
     * Constants
     */
    private final static String CLASS_NAME = "LevelHist";
    public final static String COMMENT_LINE = "****************************";
    public final static String LEVEL = "Level: ";
    

}