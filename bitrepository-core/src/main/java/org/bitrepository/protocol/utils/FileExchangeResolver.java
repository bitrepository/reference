package org.bitrepository.protocol.utils;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.LocalFileExchange;
import org.bitrepository.protocol.http.HttpFileExchange;
import org.bitrepository.protocol.http.HttpsFileExchange;
import org.bitrepository.settings.referencesettings.FileExchangeSettings;
import org.bitrepository.settings.referencesettings.ProtocolType;

import java.net.URL;
import java.util.Locale;

/**
 * Util class for resolving what kind of file exchange to use.
 */
public class FileExchangeResolver {
    /**
     * Get an unconfigured {@link FileExchange} matching the given {@link URL}'s protocol.
     *
     * This should only be used when wanting a dynamically instantiated {@link FileExchange} to make get/put-operations
     * to a specific URL. To get a {@link FileExchange} matching the statically configured {@link FileExchangeSettings}
     * see {@link org.bitrepository.protocol.ProtocolComponentFactory#getFileExchange(Settings)}.
     *
     * @param url The {@link URL} to get a {@link FileExchange} from.
     * @return A new {@link FileExchange} matching url.
     */
    public static FileExchange getBasicFileExchangeFromURL(URL url) {
        ProtocolType protocolType = protocolNameToProtocolType(url.getProtocol());
        FileExchangeSettings basicSettings = new FileExchangeSettings();
        basicSettings.setProtocolType(protocolType);
        return getFileExchange(basicSettings);
    }

    /**
     * Get a {@link FileExchange} corresponding to the given {@link FileExchangeSettings}
     * @param exchangeSettings Settings to get exchange from.
     * @return New {@link FileExchange}.
     */
    public static FileExchange getFileExchange(FileExchangeSettings exchangeSettings) {
        switch (exchangeSettings.getProtocolType()) {
            case FILE:
                return new LocalFileExchange(exchangeSettings);
            case HTTP:
                return new HttpFileExchange(exchangeSettings);
            case HTTPS:
                return new HttpsFileExchange(exchangeSettings);
            default:
                throw new IllegalStateException("Can't resolve protocol type " + exchangeSettings);
        }
    }

    /**
     * Gets a {@link ProtocolType} matching the given protocol name.
     * Throws an {@link IllegalArgumentException} if an invalid/unsupported protocol is provided.
     * @param protocol Protocol name (e.g. 'file' or 'https')
     * @return The matching {@link ProtocolType}.
     */
    private static ProtocolType protocolNameToProtocolType(String protocol) {
        return ProtocolType.fromValue(protocol.toUpperCase(Locale.ROOT));
    }
}
