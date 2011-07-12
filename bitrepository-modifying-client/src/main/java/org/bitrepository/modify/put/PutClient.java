/*
 * #%L
 * Bitmagasin modify client
 * 
 * $Id: PutClientExternalAPI.java 143 2011-04-08 09:15:13Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-modifying-client/src/main/java/org/bitrepository/modify/PutClientExternalAPI.java $
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
package org.bitrepository.modify.put;

import java.net.URL;

/**
 * Interface for the put client.
 */
public interface PutClient {
	/**
	 * Method for performing the put operation.
	 * 
	 * @param url The URL where the file to be put is located.
	 * @param fileId The id of the file.
	 * @param collectionId The BitRespositoryCollectionID where the file belongs.
	 */
    void putFileWithId(URL url, String fileId, String collectionId);
}
