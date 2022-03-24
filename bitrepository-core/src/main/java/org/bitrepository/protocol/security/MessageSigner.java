/*
 * #%L
 * Bitrepository Protocol
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
package org.bitrepository.protocol.security;

import org.bitrepository.protocol.security.exception.MessageSigningException;

import java.security.KeyStore.PrivateKeyEntry;

/**
 * Interface for classes using CMS to sign messages.
 */
public interface MessageSigner {

    /**
     * Setter method for setting the PrivateKeyEntry needed by implementers to sign messages.
     *
     * @param privateKeyEntry the PrivateKeyEntry used for signing messages.
     */
    void setPrivateKeyEntry(PrivateKeyEntry privateKeyEntry);

    /**
     * Method to sign a message.
     *
     * @param messageData The messages in byte raw byte form
     * @return The raw signature.
     * @throws MessageSigningException if the signing fails.
     */
    byte[] signMessage(byte[] messageData) throws MessageSigningException;
}
