package com.ibm.stg.eda.app.etree;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.stg.eda.component.tk_etreebase.EdaContext;
import com.ibm.stg.eda.component.tk_etreebase.TkAppBase;
import com.ibm.stg.eda.component.tk_etreebase.TkUserRoleConstants;
import com.ibm.stg.eda.component.tk_etreedb.CodeUpdate_Db;
import com.ibm.stg.iipmds.common.IcofException;

public class CreateCodeDBUpdator extends TkAppBase {

	/**
	 * Constants.
	 */
	public static final String APP_NAME = "cr.updatecreatedby";
	public static final String APP_VERSION = "v1.2";
	private String date ="";
	private String createdBy ="";

	/**
	 * Constructor
	 * 
	 * @param aContext
	 *            Application context
	 */
	public CreateCodeDBUpdator(EdaContext aContext)
			throws IcofException {

		super(aContext, APP_NAME, APP_VERSION);
	}

	@Override
	protected String getAppName() {
		return APP_NAME;
	}

	@Override
	protected String getAppVersion() {
		return APP_VERSION;
	}

	protected void createSwitches(Vector<String> singleSwitches,
			Vector<String> argSwitches) {
		singleSwitches.add("-y");
		singleSwitches.add("-h");
		argSwitches.add("-db");
		argSwitches.add("-dt");
		argSwitches.add("-createdby");
		
	}

	protected String readParams(Hashtable<String, String> params,
			String errors, EdaContext xContext) throws IcofException {
		
		if (params.containsKey("-dt")) {
        	date = params.get("-dt");
        }
		if (params.containsKey("-createdby")) {
			createdBy = params.get("-createdby");
        }
		
		return errors;
	}

	protected void displayParameters(String dbMode, EdaContext xContext) {
		logInfo(xContext, "App           : " + APP_NAME + "  " + APP_VERSION,
				verboseInd);
		logInfo(xContext, "DB Mode       : " + dbMode, verboseInd);
		logInfo(xContext, "Verbose       : " + getVerboseInd(xContext),
				verboseInd);
	}

	/**
	 * Display this application's usage and invocation
	 */
	protected void showUsage() {

		StringBuffer usage = new StringBuffer();
		usage
				.append("------------------------------------------------------\n");
		usage.append(" " + APP_NAME + " " + APP_VERSION + "\n");
		usage
				.append("------------------------------------------------------\n");
		usage.append("USAGE:\n");
		usage.append("------\n");
		usage.append(APP_NAME + " [-y] [-h] [-db dbMode]\n");
		usage.append("\n");
		usage
				.append("  -y            = (optional) Verbose mode (echo messages to screen)\n");
		usage
				.append("  dbMode        = (optional) DEV | PROD (defaults to PROD)\n");
		usage.append("  -h            = Help (shows this information)\n");
		usage.append("\n");
		usage.append("Return Codes\n");
		usage.append("------------\n");
		usage.append(" 0 = ok\n");
		usage.append(" 1 = error\n");
		usage.append("\n");

		System.out.println(usage);

	}

	@Override
	protected TkUserRoleConstants[] getAuthorisedRoles(EdaContext context) {
		return new TkUserRoleConstants[] { TkUserRoleConstants.CCB_APPROVER };
	}

	/**
	 * Instantiate the ValidateReleaesComponent class and process the arguments.
	 * 
	 * @param argv
	 *            [] the command line arguments
	 */
	public static void main(String argv[]) {

		TkAppBase myApp = null;
		try {

			myApp = new CreateCodeDBUpdator(null);
			start(myApp, argv);

		}

		catch (Exception e) {

			handleExceptionInMain(e);
		} finally {

			handleInFinallyBlock(myApp);
		}

	}

	public void process(EdaContext xContext) throws IcofException,
			ParseException {

		connectToDB(xContext);
		updateCreatedBy(xContext);

	}

	private void updateCreatedBy(EdaContext xContext) throws ParseException {

		HashMap<Long, String> createdByMap = buildCreatedByMap();
		
		System.out.println("The Map size is " +createdByMap.size());

		CodeUpdate_Db updateDb = new CodeUpdate_Db();
		try {
			Vector<CodeUpdate_Db> resultList = updateDb.dbLookupCreatedBy(
					xContext, createdBy+"%");
			
 		System.out.println("The size of the population to be changed "+resultList.size());
			
			Iterator<CodeUpdate_Db> itr = resultList.iterator();
			
			while (itr.hasNext()) {
				CodeUpdate_Db codeUpdate = itr.next();
				long dbId = codeUpdate.getId();
				if (createdByMap.containsKey(dbId)) {
					String userid = createdByMap.get(codeUpdate.getId());
					codeUpdate.dbUpdateCreatedByData(xContext, userid, dbId);
				}
			}

		} catch (IcofException e) {
			e.printStackTrace();
		}

	}

	private HashMap<Long, String> buildCreatedByMap() throws ParseException {

		File[] files = new File("/afs/eda/data/edainfra/tools/user/logs")
				.listFiles();
		ArrayList<File> filteredFiles = new ArrayList<File>();

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		Date dt = formatter.parse(date);

		for (File file : files) {
			long time = file.lastModified();
			if (time >= dt.getTime()) {

				if (file.getName().startsWith("post_commit")
						&& !file.getName().endsWith("log")) {
					filteredFiles.add(file);
				}

			}
		}

		HashMap<Long, String> createdByMap = new HashMap<Long, String>();
		Iterator<File> fileItr = filteredFiles.iterator();
		while (fileItr.hasNext()) {
			File file = fileItr.next();
			parseFile(createdByMap, file);
		}

		return createdByMap;
	}

	private void parseFile(HashMap<Long, String> createdByMap, File file) {

		try {

			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			String user = "";
			Long dbId = 0l;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {

				if (strLine.startsWith("USER")) {
					String[] matches = strLine.split("\\|");
					user = matches[1].trim();
				} else if (strLine.startsWith("DB_ID ")) {
					String[] matches = strLine.split("\\|");
					dbId = Long.valueOf(matches[1].trim());
				}
			}
			createdByMap.put(dbId, user);

			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

	}

	protected void validateAndProceed(EdaContext xContext) throws IcofException {
	}

}
