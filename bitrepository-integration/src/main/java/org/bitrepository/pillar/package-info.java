/*
 * #%L
 * Bitrepository Integration
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
/**
 * This package provides a reference pillar
 * <p/>
 * The class {@link org.bitrepository.pillar.PillarAPI} provides the basic interface for the reference pillar towards
 * the {@link org.bitrepository.pillar.PillarMessageListener}.
 * This gives the opportunity for different reference pillar implementations.
 * <p/>
 * The {@link org.bitrepository.pillar.PillarMessageListener} provides the basic interface for receiving the different
 * message handled by the reference pillar.
 * <p/>
 * The {@link org.bitrepository.pillar.ReferenceArchive} handles the deposit of the files. When retrieving a file, it 
 * is downloaded to the 'tmp' directory, and when the files is completed, then it is moved to a directory for the given
 * SLA.
 * <p/>
 * The actual implementation of the reference pillar is in the {@link org.bitrepository.pillar.ReferencePillar} class.
 * <p/>
 * Automatical creation of the response message for the reference pillar is done in the 
 * {@link org.bitrepository.pillar.ReferencePillarMessageFactory} class.
 * 
 * <h3>Exceptions</h3>
 * All methods may throw {@link IllegalArgumentException} if parameters are null
 * or empty strings, and the documentation does not explicitly allow for this.
 * Also, parameter prerequisites described in documentation may result in an
 * {@link IllegalArgumentException} without this being declared.
 */
package org.bitrepository.pillar;