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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;

public class TestLogger extends Logger {

	protected TestLogger(String name, String resourceBundleName) {
		super(name, resourceBundleName);
	}

	public static TestLogger getLog(String entity) {
		return new TestLogger("org.bitrepository.test." + entity, null);
	}
	
	public void error(String msg) {
		this.severe(msg);
	}

	public void debug(String string) {
		this.fine(string);
	}

	public void warn(String msg) {
		this.warning(msg);
	}

	public void error(String msg, JMSException ex) {
		log(Level.SEVERE, msg, ex);
	}	
}
