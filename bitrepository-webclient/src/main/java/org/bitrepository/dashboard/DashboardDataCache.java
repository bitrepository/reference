/*
 * #%L
 * Bitrepository Webclient
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.dashboard;

import java.util.ArrayList;
import java.util.HashMap;

import org.bitrepository.common.webobjects.StatisticsCollectionSize;
import org.bitrepository.common.webobjects.StatisticsDataSize;
import org.bitrepository.common.webobjects.StatisticsPillarSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Cache class for the JSP page dashboard.jsp.
 * Change reload timer:  reloadTimer = 5*60*1000L; // 5min     
 */
public class DashboardDataCache {
	private final static Logger log = LoggerFactory.getLogger(DashboardDataCache.class);
	private static HashMap<String, String> collectionId2NameMap;
	private static HashMap<String, ArrayList<StatisticsDataSize>> statisticsIdDataSizeMap;
	private static HashMap<String, GetCollectionInformation> getCollectionInformationMap;
	private static HashMap<String, ArrayList<StatisticsDataSize>> statisticsIdDataGrowthMap;
	private static HashMap<String, ArrayList<GetWorkflowSetup>> getWorkflowSetupMap;
	private static HashMap<String, ArrayList<GetIntegrityStatus>> getIntegrityStatusMap;
	private static ArrayList<StatisticsCollectionSize> collectionDataSize;
	private static ArrayList<StatisticsPillarSize> latestPillarDataSize;
	private static long lastReload = 0;
	private static long reloadTimer = 5 * 60 * 1000L; // 5min
	private static long MILIS_PR_DAY = 86400 * 1000L;

	static {
		try {
			reload();
		} catch (Exception e) {
			log.error("Unable to load dashboard data", e);
			e.printStackTrace();
		}
	}

	public static HashMap<String, ArrayList<StatisticsDataSize>> getStatisticsIdDataSizeMap() {
		reload();
		return statisticsIdDataSizeMap;
	}

	public static HashMap<String, GetCollectionInformation> getCollectionInformationMap() {
		reload();
		return getCollectionInformationMap;
	}

	public static HashMap<String, ArrayList<StatisticsDataSize>> getStatisticsIdDataGrowthMap() {
		reload();
		return statisticsIdDataGrowthMap;
	}

	public static HashMap<String, ArrayList<GetWorkflowSetup>> getWorkflowSetupMap() {
		reload();
		return getWorkflowSetupMap;
	}

	public static HashMap<String, ArrayList<GetIntegrityStatus>> getIntegrityStatusMap() {
		reload();
		return getIntegrityStatusMap;
	}

	public static HashMap<String, String> getCollectionId2NameMap() {
		reload();
		return collectionId2NameMap;
	}

	public static ArrayList<StatisticsCollectionSize> getCollectionDataSize() {
		reload();
		return collectionDataSize;
	}

	public static ArrayList<StatisticsPillarSize> getLatestPillarDataSize() {
		reload();
		return latestPillarDataSize;
	}

	/*
	 * Calculate the delta (data growth rate over time) of the StatisticsDataSize
	 */
	private static ArrayList<StatisticsDataSize> calculateGrowthRate(ArrayList<StatisticsDataSize> data) {
		ArrayList<StatisticsDataSize> growth = new ArrayList<StatisticsDataSize>();
		if (data.size() <= 1) {
			return growth;
		}

		// first set the dates;
		for (int i = 0; i < data.size(); i++) {
			StatisticsDataSize currentDataSize = data.get(i);
			StatisticsDataSize growthRate = new StatisticsDataSize();
			growthRate.setDateMillis(currentDataSize.getDateMillis());
			if (i == 0) {
				growthRate.setDataSize(0L);
			} else {
				long deltaBytes = currentDataSize.getDataSize() - data.get(i - 1).getDataSize();
				long deltaTime = currentDataSize.getDateMillis() - data.get(i - 1).getDateMillis();
				float growthInBytesPrDay = 1f * MILIS_PR_DAY * deltaBytes / (deltaTime);
				growthRate.setDataSize((long) growthInBytesPrDay);
			}

			growth.add(growthRate);
		}

		return growth;
	}

	// only called from the reload() method. This method arrange all the data in nice organized Maps.
	private static void loadDataMaps() {
		ArrayList<String> ids = IntegrityClient.getPillarIds();
		for (String current : ids) {
			collectionId2NameMap.put(current, IntegrityClient.getPillarName(current));
			ArrayList<StatisticsDataSize> dataSizeHistory = IntegrityClient.getDataSizeHistory(current);
			statisticsIdDataSizeMap.put(current, dataSizeHistory);
			getCollectionInformationMap.put(current, IntegrityClient.getCollectionInformation(current));
			getWorkflowSetupMap.put(current, IntegrityClient.getWorkflowSetup(current));
			getIntegrityStatusMap.put(current, IntegrityClient.getIntegrityStatus(current));
			statisticsIdDataGrowthMap.put(current, calculateGrowthRate(dataSizeHistory));
		}
	}

	/*
	 * Make all WS-calls and create struktured data. The reloadTime variable determines how often this reload method is called.
	 */
	private static void reload() {
		if ((System.currentTimeMillis() - lastReload) < reloadTimer) { // Dont reload
			return;
		}
		lastReload = System.currentTimeMillis();

		log.debug("reloading dashboard data cache");
		collectionId2NameMap = new HashMap<String, String>();
		statisticsIdDataSizeMap = new HashMap<String, ArrayList<StatisticsDataSize>>();
		getCollectionInformationMap = new HashMap<String, GetCollectionInformation>();
		statisticsIdDataGrowthMap = new HashMap<String, ArrayList<StatisticsDataSize>>();
		getWorkflowSetupMap = new HashMap<String, ArrayList<GetWorkflowSetup>>();
		getIntegrityStatusMap = new HashMap<String, ArrayList<GetIntegrityStatus>>();
		loadDataMaps();
		collectionDataSize = IntegrityClient.getCollectionDataSize();
		latestPillarDataSize = IntegrityClient.getLatestPillarDataSize();
	}

}