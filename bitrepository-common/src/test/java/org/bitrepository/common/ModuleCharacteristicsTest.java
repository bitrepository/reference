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
