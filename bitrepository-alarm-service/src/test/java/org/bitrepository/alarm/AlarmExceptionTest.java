/*
 * #%L
 * Bitrepository Alarm Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AlarmExceptionTest extends ExtendedTestCase {
    
    @Test(groups = {"regressiontest"})
    public void alarmExceptionTest() throws Exception {
        addDescription("Tests that AlarmExceptions can be thrown.");
        String alarmError = "The message of the alarm exception";
        try {
            throw new AlarmException(alarmError);
        } catch (AlarmException e) {
            Assert.assertEquals(e.getMessage(), alarmError);
            Assert.assertNull(e.getCause());
        }
        
        String otherError = "This is the message of the included exception";
        try {
            throw new AlarmException(alarmError, new Exception(otherError));
        } catch (AlarmException e) {
            Assert.assertEquals(e.getMessage(), alarmError);
            Assert.assertNotNull(e.getCause());
            Assert.assertEquals(e.getCause().getMessage(), otherError);
        }
    }

}
