/*
 * #%L
 * Bitmagasin integrationstest
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
package org.bitrepository.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLogger {	
    private Logger log;

    public TestLogger(Class<?> logHandle) {
        log = LoggerFactory.getLogger(logHandle);
    }

    public void error(String msg) {
        log.error(msg);
    }

    public void debug(String string) {
        log.debug(string);
    }

    public void warn(String msg) {
        log.warn(msg);
    }

    public void info(String msg) {
        log.info(msg);
    }	
}
