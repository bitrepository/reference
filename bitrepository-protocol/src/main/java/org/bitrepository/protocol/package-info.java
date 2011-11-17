/*
 * #%L
 * Bitrepository Protocol
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
 * This package provides functionality for communicating with the protocol
 * of the bit repository.
 * <p/>
 * The classes {@link org.bitrepository.protocol.messagebus.MessageBus}
 * and {@link org.bitrepository.protocol.messagebus.MessageListener} provide the
 * primary interface for sending asynchronous messages in the protocol.
 * {@link org.bitrepository.protocol.messagebus.AbstractMessageListener} provides a
 * default implementation that throws exceptions in all instances.
 * <p/>
 * The {@link org.bitrepository.protocol.FileExchange} interface defines the interface for transporting binary blobs of
 * data.
 * <p/>
 * The class {@link org.bitrepository.protocol.ProtocolComponentFactory} provides the
 * interface for initiating concrete asynchronous communication in the protocol.
 * <p/>
 * The subpackage {@link org.bitrepository.protocol.activemq} contains an
 * implementation of a message connection in ActiveMQ.
 * <p/>
 * The subpackage {@link org.bitrepository.protocol.http} contains the protocol
 * for synchronous communication of blobs of binary data implemented in HTTP.
 *
 * <h3>Exceptions</h3>
 * All methods may throw {@link IllegalArgumentException} if parameters are null
 * or empty strings, and the documentation does not explicitly allow for this.
 * Also, parameter prerequisites described in documentation may result in an
 * {@link IllegalArgumentException} without this being declared.
 *
 * All methods may throw
 * {@link org.bitrepository.protocol.CoordinationLayerException}
 * in case of faults.
 *
 * Implementations of the interfaces may throw {@link UnsupportedOperationException} in case they do not implement a
 * non-required method.
 */
package org.bitrepository.protocol;