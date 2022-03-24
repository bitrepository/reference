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
 * <p>
 * This package implements a reference pillar.
 * </p><p>
 * The class @see org.bitrepository.pillar.PillarAPI provides the basic interface for the reference pillar towards
 * the @see org.bitrepository.pillar.PillarMessageListener.
 * This gives the opportunity for different reference pillar implementations.
 * </p><p>
 * The @see org.bitrepository.pillar.PillarMessageListener provides the basic interface for receiving the different
 * message handled by the reference pillar.
 * </p><p>
 * The @see org.bitrepository.pillar.store.filearchive.ReferenceArchive handles the deposit of the files.
 * When retrieving a file, it is downloaded to the 'tmp' directory, and when the files is completed, then it is moved 
 * to the file directory.
 * If it at some later point is requested to be removed, then it is moved to an 'retain' directory.
 * </p><p>
 * The actual implementation of the reference pillar is in the 
 *  org.bitrepository.pillar.referencepillar.ReferencePillar class.
 * </p><p>
 * Automatic creation of the response message for the reference pillar is done in the
 *  org.bitrepository.pillar.ReferencePillarMessageFactory class.
 * </p><p>
 * The @see org.bitrepository.pillar.checksumpillar.ChecksumPillar is the implementation of the checksum pillar.
 * It does not store the files, only their checksums, and it will therefore not be able to handle some parts of the 
 * org.bitrepository.org.bitrepository.protocol.
 * </p>
 * 
 * <h3>Exceptions</h3>
 * All methods may throw IllegalArgumentException if parameters are null
 * or empty strings, and the documentation does not explicitly allow for this.
 * Also, parameter prerequisites described in documentation may result in an
 * IllegalArgumentException without this being declared.
 */
package org.bitrepository.pillar;
