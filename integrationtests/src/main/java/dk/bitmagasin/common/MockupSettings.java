package dk.bitmagasin.common;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MockupSettings {
	/** The log.*/
	private static Log log = LogFactory.getLog(MockupSettings.class);
	
	/** The identifier name for the environment for this setup.*/
	private String environmentName = "TEST";
	/** The url for the connection to the broker. */
	private String connectionUrl = "failover://tcp://sandkasse-01.kb.dk:61616";
	/** The machine name for the http-server.*/
	private String httpUrl = "sandkasse-01.kb.dk";
	/** The port for the http-server.*/
	private Integer httpPort = 80;
	/** The path at the http-server.*/
	private String httpPath = "/dav";
	/** The default data id (birth date of Ada Lovelace).*/
	private String dataId = "10121815";
	private String queueName = "MyQueue";
	private String token = "https://example.dk/token#1";
	private String clientId = "CLIENT1";
	private String slaId = "SLA8";
	
    private Long timeoutMeasure = 1L;
    private TimeUnits timeoutUnit = TimeUnits.SECONDS;
    private Integer errorCode = 0;
    private String errorMessage = "error?";
    private String pillarId = "MockUpClientA";
    private File dataDir = new File(".");

	public static MockupSettings instance;
	
	public static MockupSettings getInstance(String... args) {
		if(instance == null) {
			instance = new MockupSettings(args);
		} else {
			System.out.println("Settings has already been initialized! \n"
					+ "args ignored!");
		}
		return instance;
	}
	
	protected MockupSettings(String... args) {
		System.out.println("Arguments as settings (with default):");
//		System.out.println("environmentName=(" + environmentName + ")");
//		System.out.println("connectionUrl=(" + connectionUrl + ")");
//		System.out.println("httpUrl=(" + httpUrl + ")");
//		System.out.println("httpPort=(" + httpPort + ")");
//		System.out.println("httpPath=(" + httpPath + ")");
//		System.out.println("slaId=(" + slaId + ")");
//		System.out.println("dataId=(" + dataId + ")");
//		System.out.println("queueName=(" + queueName + ")");
//		System.out.println("token=(" + token + ")");
//		System.out.println("clientId=(" + clientId + ")");
//		System.out.println("pillarId=(" + pillarId + ")");
//		System.out.println("errorMessage=(" + errorMessage + ")");
//		System.out.println("errorCode=(" + errorCode + ")");
//		System.out.println("timeoutUnit=(" + timeoutUnit + ")");
//		System.out.println("timeoutMeasure=(" + timeoutMeasure + ")");
//		System.out.println("dataDir=(" + dataDir.getAbsolutePath() + ")");
		
		for(String arg : args) {
			addSetting(arg);
		}
	}
	
	public void addSetting(String arg) {
		if(arg.startsWith("environmentName=")) {
			environmentName = arg.replace("environmentName=", "");
		} else if(arg.startsWith("connectionUrl=")) {
			connectionUrl = arg.replace("connectionUrl=", "");
		} else if(arg.startsWith("httpUrl=")) {
			httpUrl = arg.replace("httpUrl=", "");
		} else if(arg.startsWith("httpPort=")) {
			httpPort = Integer.valueOf(arg.replace("httpPort=", ""));
		} else if(arg.startsWith("httpPath=")) {
			httpPath = arg.replace("httpPath=", "");
		} else if(arg.startsWith("slaId=")) {
			slaId = arg.replace("slaId=", "");
		} else if(arg.startsWith("dataId=")) {
			dataId = arg.replace("dataId=", "");
		} else if(arg.startsWith("queueName=")) {
			queueName = arg.replace("queueName=", "");
		} else if(arg.startsWith("token=")) {
			token = arg.replace("token=", "");
		} else if(arg.startsWith("clientId=")) {
			clientId = arg.replace("clientId=", "");
		} else if(arg.startsWith("pillarId=")) {
			pillarId = arg.replace("pillarId=", "");
		} else if(arg.startsWith("errorMessage=")) {
			errorMessage = arg.replace("errorMessage=", "");
			if(errorCode == 0) {
				errorCode = -1;
			}
		} else if(arg.startsWith("errorCode=")) {
			errorCode = Integer.valueOf(arg.replace("errorCode=", ""));
		} else if(arg.startsWith("timeoutUnit=")) {
			timeoutUnit = TimeUnits.valueOf(arg.replace("timeoutUnit=", ""));
		} else if(arg.startsWith("timeoutMeasure=")) {
			timeoutMeasure = Long.valueOf(arg.replace("timeoutMeasure=", ""));
		} else if(arg.startsWith("dataDir=")) {
			dataDir = new File(arg.replace("dataDir=", ""));
			if(dataDir.isFile()) {
				throw new IllegalArgumentException("The data directory is a "
						+ "file.");
			}
			if(!(dataDir.exists() && dataDir.isDirectory())) {
				log.info("creating directory: " + dataDir.getAbsolutePath());
				if(!dataDir.mkdir()) {
					throw new IllegalArgumentException("Cannot create the data "
							+ "directory.");
				}
			}
		} else if(arg.startsWith("action=")) {
			// IGNORE! is handled locally (client only!).
		} else {
			System.err.println("Bad argument: " + arg);
		}
	}
	
	public String getEnvironmentName() {
		return environmentName;
	}
	
	public String getConnectionUrl() {
		return connectionUrl;
	}
	
	public String getHttpUrl() {
		return httpUrl;
	}
	
	public Integer getHttpPort() {
		return httpPort;
	}
	
	public String getHttpPath() {
		return httpPath;
	}
	
	public String getSlaId() {
		return slaId;
	}
	
	public String getSlaTopicId() {
		return environmentName + "_" + slaId;
	}
	
	public String getDataId() {
		return dataId;
	}

	public String getQueue() {
		return environmentName + "_" + queueName;
	}
	
	public String getToken() {
		return token;
	}
	
	public String getClientId() {
		return environmentName + "_" + clientId;
	}
	
    public Long getTimeoutMeasure() {
    	return timeoutMeasure;
    }
    
    public TimeUnits getTimeoutUnit() {
    	return timeoutUnit;
    }
    
    public Integer getErrorCode() {
    	return errorCode;
    }
    
    public String getErrorMessage() {
    	return errorMessage;
    }
    
    public String getPillarId() {
    	return pillarId;
    }
    
    public File getDataDir() {
    	return dataDir;
    }
}
