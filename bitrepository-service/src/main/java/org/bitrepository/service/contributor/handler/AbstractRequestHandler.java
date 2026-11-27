/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.service.contributor.handler;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.protocol.ProtocolVersionLoader;
import org.bitrepository.protocol.utils.FileExchangeResolver;
import org.bitrepository.service.contributor.ContributorContext;
import org.bitrepository.service.exception.InvalidMessageException;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * The interface for the request handlers.
 *
 * @param <T> The request class for the specific type of requests to be handled by this request handler.
 */
public abstract class AbstractRequestHandler<T extends MessageRequest> implements RequestHandler<T> {
    protected static final BigInteger VERSION = ProtocolVersionLoader.loadProtocolVersion().getVersion();
    protected static final BigInteger MIN_VERSION = ProtocolVersionLoader.loadProtocolVersion().getMinVersion();
    protected static final String XSD_CLASSPATH = "xsd/";
    protected static final String XSD_BR_DATA = "BitRepositoryData.xsd";
    private final ContributorContext context;

    /**
     * @param context The context for this contributor.
     */
    protected AbstractRequestHandler(ContributorContext context) {
        ArgumentValidator.checkNotNull(context, "ContributorContext context");
        this.context = context;
    }

    /**
     * @return The handler context as defined in the concrete classes.
     */
    protected ContributorContext getContext() {
        return context;
    }

    /**
     * Delegates to the response dispatchers dispatchResponse method.
     *
     * @param request  the request
     * @param response the response
     */
    protected void dispatchResponse(MessageResponse response, T request) {
        context.getResponseDispatcher().dispatchResponse(response, request);
    }

    /**
     * Validates that the collectionID has been set.
     *
     * @param request The request to check the collectionID for.
     */
    protected void validateCollectionID(T request) {
        if (!request.isSetCollectionID()) {
            throw new IllegalArgumentException(request.getClass().getSimpleName() +
                    "'s requires a CollectionID");
        }
    }

    /**
     * Parses an address to upload data to, and checks that a file exchange supports its protocol, so an invalid
     * address can be rejected before the operation is accepted.
     *
     * @param address The address from the request.
     * @return The address as a URL.
     * @throws InvalidMessageException If the address is not an absolute URL with a supported protocol.
     */
    protected static URL toUploadUrl(String address) throws InvalidMessageException {
        try {
            URL uploadUrl = new URI(address).toURL();
            FileExchangeResolver.getBasicFileExchangeFromURL(uploadUrl);
            return uploadUrl;
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
            throw new InvalidMessageException(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE,
                    "Invalid address '" + address + "', cause: " + e.getMessage(), e);
        }
    }
}
