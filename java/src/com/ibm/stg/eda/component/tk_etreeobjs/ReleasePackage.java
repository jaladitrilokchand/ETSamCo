package com.ibm.stg.eda.component.tk_etreeobjs;

/**
 * <pre>
 *=============================================================================
 *
 * Copyright: (C) IBM Corporation 2012 -- IBM Internal Use Only
 *
 *=============================================================================
 *
 * CREATOR: Prashanth Shivaram
 *
 *-PURPOSE---------------------------------------------------------------------
 * Wrapper Class for all the Release Package Db activities
 *-----------------------------------------------------------------------------
 *
 *
 *-CHANGE LOG------------------------------------------------------------------
 * 08/07/2012 GFS  Initial coding.
 *=============================================================================
 * </pre>
 */

import java.sql.Timestamp;
import java.util.ArrayList;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreedb.Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.ReleasePackage_Component_Version_Db;
import com.ibm.stg.eda.component.tk_etreedb.ReleasePackage_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class ReleasePackage {

	public ReleasePackage() {

	}

	/**
	 * Add the Release Package to the DB.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compVersion
	 *            Component_Version_Db Object.
	 * @param platformId
	 *            The platform id
	 * @param spinLevel
	 *            The spin level id
	 * @param patchLevel
	 *            The patch level id
	 * @param createdBy
	 *            Created By User
	 * @param createdOn
	 *            Created on
	 * @param updatedBy
	 *            Updated User
	 * @param updatedOn
	 *            Updated on
	 * @param deletedBy
	 *            Deleting User
	 * @param deletedOn
	 *            Deleting on
	 * @return True if Release package was added
	 * @throws IcofException
	 */
	public boolean addReleasePackageInfo(EdaContext xContext,
			Component_Version_Db compVersion, short platformId,
			short spinLevel, short patchLevel, String createdBy,
			Timestamp createdOn, String updatedBy, Timestamp updatedOn,
			String deletedBy, Timestamp deletedOn) throws IcofException {

		ReleasePackage_Db relDb = new ReleasePackage_Db(0, platformId,
				spinLevel, patchLevel, createdBy, createdOn, updatedBy,
				updatedOn, deletedBy, deletedOn);
		boolean result = false;
		try {
    		// Lookup the object in the database first.
			relDb = relDb.dbLookUpByPlatSpinPatch(xContext);
			
			if(null != relDb && null != relDb.getDeletedBy()){
				 result = relDb.dbUnMarkDeleteRow(xContext, updatedBy);
				 if(result){
					System.out.println("Unmarked the delete info for Package ( Patch level :" + relDb.getPatchLevel()
								+ " Spin level :" + relDb.getSpinLevel() + " Platform :");
				 }
				 
			}
			return result;
    	}
    	catch(IcofException trap) {
    		// Add the new object
    		relDb.dbAddRow(xContext);
    		result = addToReleasePackageCompVersionDb(xContext,
    				compVersion, relDb);
    	}
		return result;
	}
	
	
	/**
	 * Migrate the Release Package to the DB.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compVersion
	 *            Component_Version_Db Object.
	 * @param relDbId 
	 * @return True if Release package was added
	 * @throws IcofException
	 */
	public boolean migrateReleasePackageInfo(EdaContext xContext,
			Component_Version_Db compVersion, long relDbId)
			throws IcofException {

		ReleasePackage_Db relDb = new ReleasePackage_Db(relDbId);
		ArrayList<ReleasePackage_Db> releasePackageList = relDb
				.dbLookupComponentVersion(xContext, compVersion);
		if (releasePackageList.size() == 0) {
			boolean result = migrateToReleasePackageCompVersionDb(xContext,
					compVersion, relDb);
			return result;
		}else{
			throw new IcofException("", "migratePackages()",
					IcofException.WARNING, "Unable to Migrate Packages.\n",
			"The tool kit component migration is not required");
		}

	}


	/**
	 * Update the Release Package Info.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compVersion
	 *            Component_Version_Db Object.
	 * @param relDbId
	 *            a unique Releation_DB Id.
	 * @param platformId
	 *            The platform id
	 * @param spinLevel
	 *            The spin level id
	 * @param patchLevel
	 *            The patch level id
	 * @return True if Release package was updated
	 * @throws IcofException
	 */
	public boolean updateReleasePackageInfo(EdaContext xContext,
			Component_Version_Db compVersion, long relDbId, short platformId,
			short spinLevel, short patchLevel) throws IcofException {

		ReleasePackage_Db relDb = new ReleasePackage_Db(relDbId);
		relDb.dbLookupById(xContext);
		
		ReleasePackage_Db updateDb = new ReleasePackage_Db(0, platformId,
				spinLevel, patchLevel, relDb.getCreatedBy(), null, null,
				null, null, null);
		updateDb.dbAddRow(xContext);

		boolean result = addToReleasePackageCompVersionDb(xContext,
				compVersion, updateDb);

		return result;

	}

	/**
	 * Update the Release package patch info.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compVersion
	 *            Component_Version_Db Object.
	 * @param relDbId
	 *            a unique Releation_DB Id.
	 * @param patchLevel
	 *            The patch level id
	 * @return True if Release package was updated
	 * @throws IcofException
	 */
	public boolean updateReleasePackagePatchInfo(EdaContext xContext,
			Component_Version_Db compVersion, long relDbId, short patchLevel)
			throws IcofException {

		ReleasePackage_Db relDb = new ReleasePackage_Db(relDbId);
		relDb.dbLookupById(xContext);
		
		ReleasePackage_Db updateDb = new ReleasePackage_Db(0, relDb.getPlatformId(),
				relDb.getSpinLevel(), patchLevel, relDb.getCreatedBy(), null, null,
				null, null, null);
		updateDb.dbAddRow(xContext);

		boolean result = addToReleasePackageCompVersionDb(xContext,
				compVersion, updateDb);

		return result;

	}

	/**
	 * Update the Release package Platform info.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compVersion
	 *            Component_Version_Db Object.
	 * @param relDbId
	 *            a unique Releation_DB Id.
	 * @param platformId
	 *            The platform id
	 * @return True if Release package was updated
	 * @throws IcofException
	 */
	public boolean updateReleasePackagePlatInfo(EdaContext xContext,
			Component_Version_Db compVersion, long relDbId, short platformId)
			throws IcofException {

		ReleasePackage_Db relDb = new ReleasePackage_Db(relDbId);
		relDb.dbLookupById(xContext);
		
		ReleasePackage_Db updateDb = new ReleasePackage_Db(0, platformId,
				relDb.getSpinLevel(), relDb.getPatchLevel(), relDb.getCreatedBy(), null, null,
				null, null, null);
		updateDb.dbAddRow(xContext);

		boolean result = addToReleasePackageCompVersionDb(xContext,
				compVersion, updateDb);

		return result;

	}

	/**
	 * Update the Release package Spin info.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compVersion
	 *            Component_Version_Db Object.
	 * @param relDbId
	 *            a unique Releation_DB Id.
	 * @param spinLevel
	 *            The spin level id
	 * @return True if Release package was updated
	 * @throws IcofException
	 */
	public boolean updateReleasePackageSpinInfo(EdaContext xContext,
			Component_Version_Db compVersion, long relDbId, short spinLevel)
			throws IcofException {

		ReleasePackage_Db relDb = new ReleasePackage_Db(relDbId);
		relDb.dbLookupById(xContext);
		
		ReleasePackage_Db updateDb = new ReleasePackage_Db(0, relDb.getPlatformId(),
				spinLevel, relDb.getPatchLevel(), relDb.getCreatedBy(), null, null,
				null, null, null);
		updateDb.dbAddRow(xContext);

		boolean result = addToReleasePackageCompVersionDb(xContext,
				compVersion, updateDb);

		return result;

	}

	private boolean migrateToReleasePackageCompVersionDb(EdaContext xContext,
			Component_Version_Db compVersion, ReleasePackage_Db updateDb)
			throws IcofException {
		
		ReleasePackage_Component_Version_Db rPackageCompVerDb = new ReleasePackage_Component_Version_Db(
				updateDb, compVersion);
		boolean result = rPackageCompVerDb.dbUpdateRow(xContext);
		
//		if(result){
//			updateDb.dbUpdateRow(xContext);
//		}
		return result;
	}
	
	private boolean addToReleasePackageCompVersionDb(EdaContext xContext,
			Component_Version_Db compVersion, ReleasePackage_Db updateDb)
			throws IcofException {
		ReleasePackage_Component_Version_Db rPackageCompVerDb = new ReleasePackage_Component_Version_Db(
				updateDb, compVersion);
		boolean result = rPackageCompVerDb.dbAddRow(xContext);
		return result;
	}

	/**
	 * Gets the Release package Info for a given component version.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compVersion
	 *            Component_Version_Db Object.
	 * @return ArrayList of ReleasePackage_Db objects
	 * @throws IcofException
	 */
	public ArrayList<ReleasePackage_Db> getReleasePackagesInfo(
			EdaContext xContext, Component_Version_Db compVersion) throws IcofException {

		ReleasePackage_Db relDb = new ReleasePackage_Db();
		ArrayList<ReleasePackage_Db> releasePackageList = relDb
				.dbLookupComponentVersion(xContext, compVersion);
		return releasePackageList;
	}

	/**
	 * Mark the Release package info as deleted.
	 * 
	 * @param xContext
	 *            Application context
	 * @param compVersion
	 *            Component_Version_Db Object.
	 * @param platformId
	 *            The platform id
	 * @param spinLevel
	 *            The spin level id
	 * @param patchLevel
	 *            The patch level id
  	 * @param deletedBy
	 *            The Deleted by user        
	 * @return True if Release package was marked as removed
	 * @throws IcofException
	 */
	public boolean removeReleasePackageInfo(EdaContext xContext,
			Component_Version_Db compVersion, short platformId,
			short spinLevel, short patchLevel, String deletedBy)
			throws IcofException {


		ReleasePackage_Db relDb = new ReleasePackage_Db(0, platformId,
				spinLevel, patchLevel, null, null, null,
				null, deletedBy, null);
		boolean result = relDb.dbDeleteRow(xContext, deletedBy);

		return result;
	}

}
