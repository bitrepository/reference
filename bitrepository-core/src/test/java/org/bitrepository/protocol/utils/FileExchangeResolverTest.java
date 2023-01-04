package org.bitrepository.protocol.utils;

import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.LocalFileExchange;
import org.bitrepository.protocol.http.HttpFileExchange;
import org.bitrepository.protocol.http.HttpsFileExchange;
import org.bitrepository.settings.referencesettings.FileExchangeSettings;
import org.bitrepository.settings.referencesettings.ProtocolType;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.testng.Assert.assertEquals;

public class FileExchangeResolverTest {
    @Test
    public void resolveFileProtocol() {
        FileExchangeSettings settings = new FileExchangeSettings();
        settings.setProtocolType(ProtocolType.FILE);
        FileExchange exchange = FileExchangeResolver.getFileExchange(settings);
        assertEquals(exchange.getClass(), LocalFileExchange.class);
    }

    @Test
    public void resolveHttpProtocol() {
        FileExchangeSettings settings = new FileExchangeSettings();
        settings.setProtocolType(ProtocolType.HTTP);
        FileExchange exchange = FileExchangeResolver.getFileExchange(settings);
        assertEquals(exchange.getClass(), HttpFileExchange.class);
    }

    @Test
    public void resolveHttpsProtocol() {
        FileExchangeSettings settings = new FileExchangeSettings();
        settings.setProtocolType(ProtocolType.HTTPS);
        FileExchange exchange = FileExchangeResolver.getFileExchange(settings);
        assertEquals(exchange.getClass(), HttpsFileExchange.class);
    }

    @Test
    public void resolveFileProtocolURL() throws MalformedURLException {
        URL url = new URL("file:///home/user/Desktop/my-cool-file.txt");
        FileExchange exchange = FileExchangeResolver.getBasicFileExchangeFromURL(url);
        assertEquals(exchange.getClass(), LocalFileExchange.class);
    }

    @Test
    public void resolveHttpProtocolURL() throws MalformedURLException {
        URL url = new URL("http://localhost:80/myfile.txt");
        FileExchange exchange = FileExchangeResolver.getBasicFileExchangeFromURL(url);
        assertEquals(exchange.getClass(), HttpFileExchange.class);
    }

    @Test
    public void resolveHttpsProtocolURL() throws MalformedURLException {
        URL url = new URL("https://localhost:443/myfile.txt");
        FileExchange exchange = FileExchangeResolver.getBasicFileExchangeFromURL(url);
        assertEquals(exchange.getClass(), HttpsFileExchange.class);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void resolveBadProtocolURL() throws MalformedURLException {
        URL badURL = new URL("ftp://some/path");
        FileExchangeResolver.getBasicFileExchangeFromURL(badURL);
    }
}
