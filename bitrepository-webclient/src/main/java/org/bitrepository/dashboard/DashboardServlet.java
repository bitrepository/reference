package org.bitrepository.dashboard;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bitrepository.common.webobjects.StatisticsDataSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardServlet extends HttpServlet {
	private static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm:ss yyyy");
	private static final long serialVersionUID = 1L;
	private final Logger log = LoggerFactory.getLogger(getClass());
	public static final String DATA_SIZE_HISTORY_ATTRIBUTE="DATA_SIZE_HISTORY_ATTRIBUTE"; 
	public static final String DATA_SIZE_HISTORY_NAMES_ATTRIBUTE="DATA_SIZE_HISTORY_NAMES_ATTRIBUTE";
	
	
	//Happens first time page is access, or when reloading
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {		
		response.setContentType("text/html; charset=UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		try {  
			ArrayList<String> collectionIdsSelected = new ArrayList<String>();
            ArrayList<ArrayList<StatisticsDataSize>> dataSet = new ArrayList<ArrayList<StatisticsDataSize>>();  
            ArrayList<String> dataSetNames = new ArrayList<String>();
            for (String id : DashboardDataCache.collectionId2NameMap.keySet()){

			  ArrayList<StatisticsDataSize> data = DashboardDataCache.statisticsIdDataSizeMap.get(id); 
			  dataSet.add(data);					  					  
			  dataSetNames.add(DashboardDataCache.collectionId2NameMap.get(id));
			  collectionIdsSelected.add(id);
		      request.setAttribute(id, "on"); 
			}	        
            request.setAttribute(DATA_SIZE_HISTORY_ATTRIBUTE, dataSet);
            request.setAttribute(DATA_SIZE_HISTORY_NAMES_ATTRIBUTE, dataSetNames);
			returnFormPage(request, response);
			return;

		} catch (Exception e) {//various server errors
			log.error("unexpected error", e);			
			returnFormPage(request, response);
			return;
		}

	}
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {
		
		response.setContentType("text/html; charset=UTF-8");
		request.setCharacterEncoding("UTF-8");

		try {  
			ArrayList<String> collectionIdsSelected = new ArrayList<String>();
            ArrayList<ArrayList<StatisticsDataSize>> dataSet = new ArrayList<ArrayList<StatisticsDataSize>>();  
            ArrayList<String> dataSetNames = new ArrayList<String>();
            for (String id : DashboardDataCache.collectionId2NameMap.keySet()){
				if (request.getParameter(id) != null){
					ArrayList<StatisticsDataSize> data = DashboardDataCache.statisticsIdDataSizeMap.get(id); 
					dataSet.add(data);					  					  
					dataSetNames.add(DashboardDataCache.collectionId2NameMap.get(id));
					collectionIdsSelected.add(id);
					request.setAttribute(id, "on"); 					
				}
			}	        
            request.setAttribute(DATA_SIZE_HISTORY_ATTRIBUTE, dataSet);
            request.setAttribute(DATA_SIZE_HISTORY_NAMES_ATTRIBUTE, dataSetNames);
			returnFormPage(request, response);
			return;

		} catch (Exception e) {//various server errors
			log.error("unexpected error", e);			
			returnFormPage(request, response);
			return;
		}

	}

	public static String formatDate(long time){
		return sdf.format(new Date(time));
	}
	
	public static float bytes2TB(Long bytes){
	  if (bytes == null){
 		 return 0.0f;
      }		
	  float bytes_f= (float) bytes;
	  return bytes_f/1099511627776f;		
	}

	private void returnFormPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher dispatcher = request.getRequestDispatcher("dashboard.jsp");
		dispatcher.forward(request, response);
		return;
	}

	public static int countCheckSumErrors(ArrayList<GetIntegrityStatus> status){
		
		int checkSumErrors = 0;
		for (GetIntegrityStatus current : status){
			checkSumErrors += current.getChecksumErrorCount();
		}		
		return checkSumErrors;		
	}
	
    public static int countMissingFiles(ArrayList<GetIntegrityStatus> status){    	
		int missingFiles = 0;
		for (GetIntegrityStatus current : status){
			missingFiles += current.getMissingFilesCount();
		}		
		return missingFiles;		
	}
    
    public static long getMaximumByteSize(ArrayList<ArrayList<StatisticsDataSize>> data){
    	long max = 0;
    	
    	for (ArrayList<StatisticsDataSize> list : data){
    		for (StatisticsDataSize currentDataSet : list){    			
    			if (currentDataSet.getDataSize() > max){
    				max =currentDataSet.getDataSize();
    			}
    		}    		
    	}    			
    	return max;
    }
        
}
