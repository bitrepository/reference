package org.bitrepository.dashboard;

import java.util.ArrayList;
import java.util.HashMap;

import org.bitrepository.common.webobjects.StatisticsCollectionSize;
import org.bitrepository.common.webobjects.StatisticsDataSize;
import org.bitrepository.common.webobjects.StatisticsPillarSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardDataCache {
	private final static Logger log = LoggerFactory.getLogger(DashboardDataCache.class);
	public static HashMap<String,String> collectionId2NameMap = new HashMap<String,String>();
    public static HashMap<String, ArrayList<StatisticsDataSize>> statisticsIdDataSizeMap = new HashMap<String, ArrayList<StatisticsDataSize>>();
    public static HashMap<String,GetCollectionInformation> getCollectionInformationMap = new HashMap<String,GetCollectionInformation>();
    public static HashMap<String, ArrayList<StatisticsDataSize>> statisticsIdDataGrowthMap = new HashMap<String, ArrayList<StatisticsDataSize>>();
	public static HashMap<String, ArrayList<GetWorkflowSetup>> getWorkflowSetupMap = new HashMap<String, ArrayList<GetWorkflowSetup>>();
	public static HashMap<String, ArrayList<GetIntegrityStatus>> getIntegrityStatusMap = new HashMap<String, ArrayList<GetIntegrityStatus>>();
	
	private static ArrayList<StatisticsCollectionSize> collectionDataSize;
	private static ArrayList<StatisticsPillarSize> latestPillarDataSize;
    private static long lastReload = 0;
	private static long reloadTimer = 5*60*1000L; // 5min    
    private static long MILIS_PR_DAY = 86400*1000L;
	
	static{
		try{
			reload(); 
		    
		}
		catch(Exception e){
		   log.error("Unable to load dashboard data",e);
		   e.printStackTrace();
		}	
	}
		
	
	public static void reload(){
	    if ((System.currentTimeMillis() - lastReload) < reloadTimer){ //Dont reload
	    	return;
	    }
	    lastReload=System.currentTimeMillis();
	    
		log.debug("reloading dashboard data cache");
	    loadDataMaps();
	    collectionDataSize = IntegrityClient.getCollectionDataSize();
		latestPillarDataSize =IntegrityClient.getLatestPillarDataSize();
	    		
	}

	public static ArrayList<StatisticsCollectionSize> getCollectionDataSize() {
		reload(); 
		return collectionDataSize;
	}
	
	public static ArrayList<StatisticsPillarSize> getLatestPillarDataSize() {
		reload(); 
		return latestPillarDataSize;
	}
	
	public  static void loadDataMaps(){ 
		ArrayList<String> ids=  IntegrityClient.getPillarIds();
		for (String current : ids){
			 collectionId2NameMap.put(current, IntegrityClient.getPillarName(current));			 			 
			 ArrayList<StatisticsDataSize> dataSizeHistory = IntegrityClient.getDataSizeHistory(current);
			 statisticsIdDataSizeMap.put(current,dataSizeHistory);
		     getCollectionInformationMap.put(current, IntegrityClient.getCollectionInformation(current));
			 getWorkflowSetupMap.put(current, IntegrityClient.getWorkflowSetup(current));
			 getIntegrityStatusMap.put(current, IntegrityClient.getIntegrityStatus(current));			 
			 statisticsIdDataGrowthMap.put(current,  calculateGrowthRate(dataSizeHistory));
			 
		}			
	}

	private static ArrayList<StatisticsDataSize> calculateGrowthRate(ArrayList<StatisticsDataSize> data){
		ArrayList<StatisticsDataSize> growth = new ArrayList<StatisticsDataSize>(); 
		if (data.size() <= 1){
			return growth;
		}
	
		//first set the dates;
		for (int i =0;i<data.size();i++){
			StatisticsDataSize currentDataSize= data.get(i);			
			StatisticsDataSize growthRate = new StatisticsDataSize();
			growthRate.setDateMillis(currentDataSize.getDateMillis());
			if (i == 0){
				growthRate.setDataSize(0L);
			}
			else{
				long deltaBytes= currentDataSize.getDataSize()- data.get(i-1).getDataSize();
                long deltaTime = currentDataSize.getDateMillis()-data.get(i-1).getDateMillis();
				float growthInBytesPrDay = 1f*MILIS_PR_DAY*deltaBytes/(deltaTime); 
				growthRate.setDataSize((long)growthInBytesPrDay);				
			}
			
			growth.add(growthRate);			
		}
		
	   return growth;
	}
	
}
