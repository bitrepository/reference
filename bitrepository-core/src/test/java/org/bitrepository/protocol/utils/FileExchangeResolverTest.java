package org.bitrepository.protocol.utils;

import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.LocalFileExchange;
import org.bitrepository.protocol.http.HttpFileExchange;
import org.bitrepository.protocol.http.HttpsFileExchange;
import org.bitrepository.settings.referencesettings.FileExchangeSettings;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.bitrepository.protocol.utils.FileExchangeResolver.getBasicFileExchangeFromURL;
import static org.bitrepository.protocol.utils.FileExchangeResolver.getFileExchange;
import static org.bitrepository.settings.referencesettings.ProtocolType.FILE;
import static org.bitrepository.settings.referencesettings.ProtocolType.HTTP;
import static org.bitrepository.settings.referencesettings.ProtocolType.HTTPS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileExchangeResolverTest {
    @Test
    public void resolveFileProtocol() {
        FileExchangeSettings settings = new FileExchangeSettings();
        settings.setProtocolType(FILE);
        FileExchange exchange = getFileExchange(settings);
        assertEquals(LocalFileExchange.class, exchange.getClass());
    }

    @Test
    public void resolveHttpProtocol() {
        FileExchangeSettings settings = new FileExchangeSettings();
        settings.setProtocolType(HTTP);
        FileExchange exchange = getFileExchange(settings);
        assertEquals(HttpFileExchange.class, exchange.getClass());
    }

    @Test
    public void resolveHttpsProtocol() {
        FileExchangeSettings settings = new FileExchangeSettings();
        settings.setProtocolType(HTTPS);
        FileExchange exchange = getFileExchange(settings);
        assertEquals(HttpsFileExchange.class, exchange.getClass());
    }

    @Test
    public void resolveFileProtocolURL() throws MalformedURLException {
        URL url = new URL("file:///home/user/Desktop/my-cool-file.txt");
        FileExchange exchange = getBasicFileExchangeFromURL(url);
        assertEquals(LocalFileExchange.class, exchange.getClass());
    }

    @Test
    public void resolveHttpProtocolURL() throws MalformedURLException {
        URL url = new URL("http://localhost:80/myfile.txt");
        FileExchange exchange = getBasicFileExchangeFromURL(url);
        assertEquals(HttpFileExchange.class, exchange.getClass());
    }

    @Test
    public void resolveHttpsProtocolURL() throws MalformedURLException {
        URL url = new URL("https://localhost:443/myfile.txt");
        FileExchange exchange = getBasicFileExchangeFromURL(url);
        assertEquals(HttpsFileExchange.class, exchange.getClass());
    }

    @Test
    public void resolveBadProtocolURL() throws MalformedURLException {
        assertThrows(IllegalArgumentException.class, () -> {
            URL badURL = new URL("ftp://some/path");
            FileExchangeResolver.getBasicFileExchangeFromURL(badURL);
        });
    }
}
