package org.bitrepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.text.SimpleDateFormat;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.NoPillarFoundException;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.exceptions.OperationTimeOutException;
import org.bitrepository.settings.collectionsettings.CollectionSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicClient {
    private PutFileClient putClient;
    private GetFileClient getClient;
    private GetChecksumsClient getChecksumClient;
    private GetFileIDsClient getFileIDsClient;
    private EventHandler eventHandler;
    private String logFile;
    private Settings settings;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private ArrayBlockingQueue<String> shortLog;

    
    public BasicClient(Settings settings) {
        log.debug("---- Basic client instanciated ----");
        logFile = "/tmp/webservice-logfile";
        changeLogFiles();
        shortLog = new ArrayBlockingQueue<String>(50);
        eventHandler = new BasicEventHandler(logFile, shortLog);
        this.settings = settings;
        putClient = ModifyComponentFactory.getInstance().retrievePutClient(settings);
        getClient = AccessComponentFactory.getInstance().createGetFileClient(settings);
        getChecksumClient = AccessComponentFactory.getInstance().createGetChecksumsClient(settings);
        getFileIDsClient = AccessComponentFactory.getInstance().createGetFileIDsClient(settings);
    }
    
    public String putFile(String fileID, long fileSize, String URLStr) {
        URL url;
        try {
            url = new URL(URLStr);
            putClient.putFileWithId(url, fileID, fileSize, eventHandler);
            return "Placing '" + fileID + "' in Bitrepository :)";
        } catch (MalformedURLException e) {
            return "The string: '" + URLStr + "' is not a valid URL!";
        }
    }
    
    public String getFile(String fileID, String URLStr) {
        URL url;
        try {
            url = new URL(URLStr);
            getClient.getFileFromFastestPillar(fileID, url, eventHandler);
            return "Fetching '" + fileID + "' from Bitrepository :)";
        } catch (MalformedURLException e) {
            return "The string: '" + URLStr + "' is not a valid URL!";
        }
    }
    
    public String getLog() {
        File logfile = new File(logFile);
        try {
            FileReader fr = new FileReader(logfile);
            BufferedReader br = new BufferedReader(fr);
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = br.readLine()) != null) {
                result.append(line + "\n");
            }
            return result.toString();
        } catch (FileNotFoundException e) {
            return "Unable find log file... '" + logfile.getAbsolutePath() + "'";
        } catch (IOException e) {
            return "Unable to read log... '" + logfile.getAbsolutePath() + "'";
        }
    }
    
    public String getHtmlLog() {
        File logfile = new File(logFile);
        try {
            FileReader fr = new FileReader(logfile);
            BufferedReader br = new BufferedReader(fr);
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = br.readLine()) != null) {
                result.append(line + "<br>");
            }
            return result.toString();
        } catch (FileNotFoundException e) {
            return "Unable find log file... '" + logfile.getAbsolutePath() + "'";
        } catch (IOException e) {
            return "Unable to read log... '" + logfile.getAbsolutePath() + "'";
        }
    }
    
    public String getShortHtmlLog() {
    	StringBuilder sb = new StringBuilder();
    	List<String> entries = new ArrayList<String>();
    	for(String entry : shortLog) {
    		entries.add(entry);
    	}
    	Collections.reverse(entries);
    	for(String entry : entries) {
    		sb.append(entry + "<br>");
    	}
    	
    	return sb.toString();
    }
    
    public String getSettingsSummary() {
        StringBuilder sb = new StringBuilder();
        CollectionSettings collectionSettings = settings.getCollectionSettings();
        sb.append("CollectionID: <i>" + collectionSettings.getCollectionID() + "</i><br>");
        sb.append("Pillar(s) in configuration: <br> <i>");
        List<String> pillarIDs = collectionSettings.getClientSettings().getPillarIDs(); 
        for(String pillarID : pillarIDs) {
        	sb.append("&nbsp;&nbsp;&nbsp; " + pillarID + "<br>");
        }
        sb.append("</i>");
        sb.append("Messagebus URL: <br> &nbsp;&nbsp;&nbsp; <i>"); 
        sb.append(collectionSettings.getProtocolSettings().getMessageBusConfiguration().getURL() + "</i><br>");
        return sb.toString();
    }
    
    public Map<String, Map<String, String>> getChecksums(String fileIDsText, String checksumType, String salt) {
    	String[] IDs = fileIDsText.split("\n");
    	Map<String, Map<String, String>> result = null;
    	ChecksumSpecTYPE checksumSpecItem = new ChecksumSpecTYPE();
    	if(salt != null || salt != "") {
    		checksumSpecItem.setChecksumSalt(salt);	
    	}
    	checksumSpecItem.setChecksumType(checksumType);
    	FileIDs fileIDs = new FileIDs();
    	for(String ID : IDs) {
    		fileIDs.getFileID().add(ID.trim());	
    	}

    	Map<String, ResultingChecksums> clientResult;
		try {
			clientResult = getChecksumClient.getChecksumsBlocking(
					settings.getCollectionSettings().getClientSettings().getPillarIDs(), 
					fileIDs, checksumSpecItem, null, eventHandler, "Deliver my checksums pirate garrh");

       	if(clientResult != null) {
    		result = new HashMap<String, Map<String, String>>();
    		Set<String> returnedPillarIDs = clientResult.keySet();
    		for(String pillarID : returnedPillarIDs) {
    			List<ChecksumDataForChecksumSpecTYPE> items = clientResult.get(pillarID).getChecksumDataItems();
    			for(ChecksumDataForChecksumSpecTYPE item : items) {
    				if(!result.containsKey(item.getFileID())) {
    					Map<String, String> value = new HashMap<String, String>();
    					value.put(pillarID, item.getChecksumValue());
    					result.put(item.getFileID(), value);
    				} else {
    					result.get(item.getFileID()).put(pillarID, item.getChecksumValue());
    				}
    			}
    		}
    	}
		} catch (NoPillarFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationTimeOutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return result;
    }
    
    public GetFileIDsResults getFileIDs(String fileIDsText) {
    	GetFileIDsResults results = new GetFileIDsResults(
    			settings.getCollectionSettings().getClientSettings().getPillarIDs());
    	GetFileIDsEventHandler handler = new GetFileIDsEventHandler(results, eventHandler);
    	
    	String[] IDs = fileIDsText.split("\n");
    	FileIDs fileIDs = new FileIDs();
    	for(String ID : IDs) {
    		fileIDs.getFileID().add(ID.trim());	
    	}
    	try {
			getFileIDsClient.getFileIDs(settings.getCollectionSettings().getClientSettings().getPillarIDs(),
					fileIDs, null, handler, "Deliver my fileIDs garrh");
		} catch (OperationFailedException e) {
			//This should not happen Jonas!
		}
    	
		try {
			while(!results.isDone() && !results.hasFailed()) {
				Thread.sleep(500);
			}
		} catch (InterruptedException e) {
			// Uhm, we got aborted, should return error..
		}
		
    	return results;
    }
    
    private void changeLogFiles() {
        File oldLogFile = new File(logFile);
        String date = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String newName = logFile + "-" + date;
        System.out.println("Moving old log file to: " + newName);
        oldLogFile.renameTo(new File(newName));
    }
    
}
