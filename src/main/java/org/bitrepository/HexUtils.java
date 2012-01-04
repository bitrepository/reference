package org.bitrepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HexUtils {
    private static final Logger log = LoggerFactory.getLogger(HexUtils.class);

    
    public static String byteArrayToString(byte[] data) {
        StringBuffer sb = new StringBuffer(data.length * 2);
        for (int i = 0; i < data.length; i++){
          int v = data[i] & 0xff;
          if (v < 16) {
            sb.append('0');
          }
          sb.append(Integer.toHexString(v));
        }
        log.info("Converted byte array to string, string: " + sb.toString());
        return sb.toString();    
    }
    
    public static byte[] stringToByteArray(String hexString) {
        int len = hexString.length();
        log.info("Trying to convert '" + hexString + "' to byte array");
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                 + Character.digit(hexString.charAt(i+1), 16));
        }
        log.info("Converted string to byte array, string: " + hexString);
        return data;

    }
    
}
