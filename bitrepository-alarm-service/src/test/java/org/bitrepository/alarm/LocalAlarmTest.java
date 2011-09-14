/*
 * #%L
 * Bitrepository Alarm Service
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.alarm;

import java.util.Date;

import org.bitrepository.bitrepositoryelements.AlarmDescription;
import org.bitrepository.bitrepositoryelements.AlarmcodeType;
import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.collection.settings.standardsettings.MessageBusConfigurationTYPE;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusFactory;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

public class LocalAlarmTest extends ExtendedTestCase {

    @Test(groups={"regressiontest"})
    public void testAlarm() throws Exception {
        
        MessageBusConfigurationTYPE conf = new MessageBusConfigurationTYPE();
        conf.setName("distribueret-test-messagebus");
        conf.setPassword("");
        conf.setLogin("");
        conf.setURL("failover:(ssl://sandkasse-01.kb.dk:60011)");
        
        Alarm alarm = new Alarm();//ExampleMessageFactory.createMessage(Alarm.class);
        alarm.setTo("JOLF-TEST-ALARM");
        alarm.setReplyTo("JOLF-TESTING-TOPIC-ALARM");
        alarm.setCorrelationID("JOLF-TESTING-TOPIC-ALARM" + new Date().getTime());
        alarm.setBitRepositoryCollectionID("JOLF-TEST");
        
        AlarmDescription description = new AlarmDescription();
        description.setAlarmCode(AlarmcodeType.GENERAL);
        description.setAlarmText("I refuse to send this alarm!!!");
        alarm.setAlarmDescription(description);
        
        MessageBus bus = MessageBusFactory.createMessageBus(conf);
        bus.sendMessage(alarm);

    }
}
