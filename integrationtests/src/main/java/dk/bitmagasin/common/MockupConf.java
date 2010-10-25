/*
 * #%L
 * Bitmagasin integritetstest
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package dk.bitmagasin.common;

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
