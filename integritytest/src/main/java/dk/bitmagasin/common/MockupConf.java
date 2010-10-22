package dk.bitmagasin.common;
/** Bit Repository Standard Header Open Source License */

import org.apache.activemq.ActiveMQConnection;

/**
 * Configuration MockUp
 * @author bam
 * @since 2010-10-01 */
public class MockupConf {
    public static final String SLAID = "SLA8";
    public static final String user = ActiveMQConnection.DEFAULT_USER;
    public static final String password = ActiveMQConnection.DEFAULT_PASSWORD;
    public static final String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    public static final String pillarId = "MockUpPillar1";
    public static final String accessClientId = "MockUpClientA";
}
