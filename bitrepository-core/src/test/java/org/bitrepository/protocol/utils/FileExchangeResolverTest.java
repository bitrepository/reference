package org.bitrepository.protocol.utils;

import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.LocalFileExchange;
import org.bitrepository.protocol.http.HttpFileExchange;
import org.bitrepository.protocol.http.HttpsFileExchange;
import org.bitrepository.settings.referencesettings.FileExchangeSettings;
import org.bitrepository.settings.referencesettings.ProtocolType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


class FileExchangeResolverTest {
    @Test
    void resolveFileProtocol() {
        FileExchangeSettings settings = new FileExchangeSettings();
        settings.setProtocolType(ProtocolType.FILE);
        FileExchange exchange = FileExchangeResolver.getFileExchange(settings);
        Assertions.assertEquals(LocalFileExchange.class, exchange.getClass());
    }

    @Test
    void resolveHttpProtocol() {
        FileExchangeSettings settings = new FileExchangeSettings();
        settings.setProtocolType(ProtocolType.HTTP);
        FileExchange exchange = FileExchangeResolver.getFileExchange(settings);
        Assertions.assertEquals(HttpFileExchange.class, exchange.getClass());
    }

    @Test
    void resolveHttpsProtocol() {
        FileExchangeSettings settings = new FileExchangeSettings();
        settings.setProtocolType(ProtocolType.HTTPS);
        FileExchange exchange = FileExchangeResolver.getFileExchange(settings);
        Assertions.assertEquals(HttpsFileExchange.class, exchange.getClass());
    }

    @Test
    void resolveFileProtocolURL() throws MalformedURLException, URISyntaxException {
        URL url = new URI("file:///home/user/Desktop/my-cool-file.txt").toURL();
        FileExchange exchange = FileExchangeResolver.getBasicFileExchangeFromURL(url);
        Assertions.assertEquals(LocalFileExchange.class, exchange.getClass());
    }

    @Test
    void resolveHttpProtocolURL() throws MalformedURLException, URISyntaxException {
        URL url = new URI("http://localhost:80/myfile.txt").toURL();
        FileExchange exchange = FileExchangeResolver.getBasicFileExchangeFromURL(url);
        Assertions.assertEquals(HttpFileExchange.class, exchange.getClass());
    }

    @Test
    void resolveHttpsProtocolURL() throws MalformedURLException, URISyntaxException {
        URL url = new URI("https://localhost:443/myfile.txt").toURL();
        FileExchange exchange = FileExchangeResolver.getBasicFileExchangeFromURL(url);
        Assertions.assertEquals(HttpsFileExchange.class, exchange.getClass());
    }

    @Test
    void resolveBadProtocolURL() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            URL badURL = new URI("ftp://some/path").toURL();
            FileExchangeResolver.getBasicFileExchangeFromURL(badURL);
        });
    }
}
