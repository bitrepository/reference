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
package org.bitrepository.protocol.activemq;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.bitrepository.settings.repositorysettings.MessageBusConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates and configures {@link ActiveMQConnectionFactory} instances for Artemis.
 *
 * <p>The broker URL is resolved in priority order:
 * <ol>
 *   <li>Environment variable {@value #BROKER_URL_ENV}</li>
 *   <li>URL from {@link MessageBusConfiguration}</li>
 * </ol>
 *
 * <p>If the resolved URL does not already contain {@code reconnectAttempts},
 * {@code ?reconnectAttempts=-1} is appended to enable indefinite reconnect.
 */
public final class ArtemisConnectionFactoryProvider {
    private static final Logger log = LoggerFactory.getLogger(ArtemisConnectionFactoryProvider.class);

    /** Environment variable that overrides the broker URL from configuration. */
    public static final String BROKER_URL_ENV = "ARTEMIS_BROKER_URL";

    private ArtemisConnectionFactoryProvider() {}

    /**
     * Creates a new {@link ActiveMQConnectionFactory} using the resolved broker URL.
     */
    public static ActiveMQConnectionFactory create(MessageBusConfiguration config) {
        String url = resolveUrl(config);
        log.info("Creating Artemis connection factory with URL: {}", url);
        return new ActiveMQConnectionFactory(url);
    }

    /**
     * Resolves the broker URL from environment variable or configuration,
     * and ensures {@code reconnectAttempts=-1} is present.
     */
    static String resolveUrl(MessageBusConfiguration config) {
        String envUrl = System.getenv(BROKER_URL_ENV);
        String base = (envUrl != null && !envUrl.isBlank()) ? envUrl : config.getURL();
        if (!base.contains("reconnectAttempts")) {
            return base + (base.contains("?") ? "&" : "?") + "reconnectAttempts=-1";
        }
        return base;
    }
}
