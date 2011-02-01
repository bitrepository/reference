/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id: MessageBusTest.java 49 2011-01-03 08:48:13Z mikis $
 * $HeadURL: https://gforge.statsbiblioteket.dk/svn/bitmagasin/trunk/bitrepository-integration/src/test/java/org/bitrepository/bus/MessageBusTest.java $
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
package org.bitrepository.protocol;

import java.util.Collection;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.bitrepository.protocol.ConnectionProperty;
import org.jaccept.structure.ExtendedTestCase;

public class ConnectionPropertyTest extends ExtendedTestCase {

	/**
	 * Tests the basic functionality of the ConnecitonProperty class:
	 * Whether it automatically is initiated and able to give the expected 
	 * amount of connections (2: one local and one distributed). 
	 * Also that it is possible to retrieve a given connection based on the Id 
	 * for that connection, and that if a wrong Id is given a null is returned.
	 */
	@Test(groups = { "testfirst", "functest" })
	public void initializationTest() {
		addDescription("Tests the initialization of ConnectionProperty, "
				+ "and ensures that only one connection is known per default.");
		Collection<ConnectionProperty> connections 
		        = ConnectionProperty.getAllConnections();
		Assert.assertEquals(connections.size(), 2, "There should be two "
				+ "connection properties.");
		Assert.assertNull(ConnectionProperty.getProperty("BAD_ID"), 
				"No connection for this url. Thus return null!");
		
		ConnectionProperty con = connections.iterator().next();
		ConnectionProperty con2 = ConnectionProperty.getProperty(con.getId());
		Assert.assertEquals(con2, con, "It should be the same ConnectionProperty");
	}
}
