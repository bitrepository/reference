/*
 * #%L
 * bitrepository-common
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
package org.bitrepository.common;

import junit.framework.Assert;

import org.testng.annotations.Test;

public class ModuleCharacteristicsTest {

	/**
	 * Verifies that the <code>ModuleCharacteristics</code> class implements the naming conversion rules correctly
	 */
	@Test(groups = { "regressiontest" })
	public void moduleNamingTest() {
		String upperCamelCaseName = "MonitoringService";
		String lowerCamelCaseName = "monitoringService";
		String lowerCaseName = "monitoringservice"; 
		String lowerCaseNameWithHyphen = "monitoring-service"; 

		ModuleCharacteristics moduleCharacteristics = new ModuleCharacteristics(lowerCaseNameWithHyphen);
		Assert.assertEquals("Upper camel casing format invalid", 
				upperCamelCaseName, moduleCharacteristics.getUpperCamelCaseName());

		Assert.assertEquals("Lower camel casing format invalid", 
				lowerCamelCaseName, moduleCharacteristics.getLowerCamelCaseName());		

		Assert.assertEquals("Lower casing with hyphen format invalid", 
				lowerCaseName, moduleCharacteristics.getLowerCaseName());
		
		Assert.assertEquals("Lower casing with hyphen format invalid", 
				lowerCaseNameWithHyphen, moduleCharacteristics.getLowerCaseNameWithHyphen());
	}

	/**
	 * The constructor needs to check for invalid arguments
	 */
	@Test(groups = { "regressiontest" })
	public void moduleConstructorTest() {
		String upperCamelCaseName = "MonitoringService";
		String lowerCamelCaseName = "monitoringService";
		try {
			new ModuleCharacteristics(upperCamelCaseName);
			Assert.fail("Shouldn't accept upper camel case name");
		} catch (IllegalArgumentException e) {
			// Works as it should
		}

		try {
			new ModuleCharacteristics(lowerCamelCaseName);
			Assert.fail("Shouldn't accept lower camel case name");
		} catch (IllegalArgumentException e) {
			// Works as it should
		}
	}
}
