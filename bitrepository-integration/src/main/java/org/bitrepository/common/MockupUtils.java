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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MockupUtils {
	
	private static final int IO_BUFFER_SIZE = 1024;
	
	public static void copyInputStreamToOutputStream(InputStream in,
			OutputStream out) throws Exception {
		if(in == null || out == null) {
			throw new NullPointerException("InputStream: " + in 
					+ ", OutputStream: " + out);
		}

		try {
			byte[] buf = new byte[IO_BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = in.read(buf)) != -1) {
				out.write(buf, 0, bytesRead);
			}
			out.flush();
		} finally {
			in.close();
		}
	}

}
