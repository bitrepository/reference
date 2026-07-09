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

import org.bitrepository.settings.repositorysettings.MessageBusConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("regressiontest")
class ArtemisConnectionFactoryProviderTest {

    private static MessageBusConfiguration configWithUrl(String url) {
        MessageBusConfiguration config = new MessageBusConfiguration();
        config.setURL(url);
        return config;
    }

    @Test
    void resolveUrlAppendsReconnectAttemptsWhenAbsent() {
        String result = ArtemisConnectionFactoryProvider.resolveUrl(
                configWithUrl("tcp://localhost:61616"));
        assertTrue(result.contains("reconnectAttempts=-1"),
                "reconnectAttempts must be added when missing: " + result);
    }

    @Test
    void resolveUrlDoesNotDuplicateReconnectAttempts() {
        String url = "tcp://localhost:61616?reconnectAttempts=-1";
        String result = ArtemisConnectionFactoryProvider.resolveUrl(configWithUrl(url));
        assertTrue(result.indexOf("reconnectAttempts") == result.lastIndexOf("reconnectAttempts"),
                "reconnectAttempts must appear exactly once: " + result);
    }

    @Test
    void resolveUrlAppendsWithAmpersandWhenQueryParamAlreadyPresent() {
        String result = ArtemisConnectionFactoryProvider.resolveUrl(
                configWithUrl("tcp://localhost:61616?foo=bar"));
        assertTrue(result.contains("&reconnectAttempts=-1"),
                "Must use & separator when query already has params: " + result);
    }

    @Test
    void resolveUrlUsesConfigUrlWhenEnvVarAbsent() {
        String configUrl = "tcp://broker.example.com:61616";
        String result = ArtemisConnectionFactoryProvider.resolveUrl(configWithUrl(configUrl));
        assertTrue(result.startsWith(configUrl),
                "URL must start with config URL when env var is absent: " + result);
    }

    @Test
    void createReturnsNonNullFactory() {
        assertNotNull(ArtemisConnectionFactoryProvider.create(configWithUrl("tcp://localhost:61616")));
    }
}
