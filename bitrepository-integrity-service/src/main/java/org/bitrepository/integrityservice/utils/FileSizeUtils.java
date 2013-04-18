package org.bitrepository.integrityservice.utils;

public class FileSizeUtils {

    private static final int unitSize = 1024;
    private static final long kiloSize = unitSize;
    private static final long megaSize = kiloSize * unitSize;
    private static final long gigaSize = megaSize * unitSize;
    private static final long teraSize = gigaSize * unitSize;
    private static final long petaSize = teraSize * unitSize;
    private static final long zetaSize = petaSize * unitSize;
    
    private static final String bytePostfix = "B";
    private static final String kiloPostfix = "KB";
    private static final String megaPostfix = "MB";
    private static final String gigaPostfix = "GB";
    private static final String teraPostfix = "TB";
    private static final String petaPostfix = "PB";
    private static final String zetaPostfix = "ZB";
    
    
    public static String toHumanShort(long size) {
        if(size >= zetaSize) {
            return formatShortZeta(size);
        } else if(size >= petaSize) {
            return formatShortPeta(size);
        } else if(size >= teraSize) {
            return formatShortTera(size);
        } else if(size >= gigaSize) {
            return formatShortGiga(size);
        } else if(size >= megaSize) {
            return formatShortMega(size);
        } else if(size >= kiloSize) {
            return formatShortKilo(size);
        } else {
            return formatShortByte(size);
        }
    }
    
    private static String formatShortZeta(long size) {
        int wholeZB = (int) (size / zetaSize);
        return wholeZB + zetaPostfix;
    }
    
    private static String formatShortPeta(long size) {
        int wholePB = (int) (size / petaSize);
        return wholePB + petaPostfix;
    }
    
    private static String formatShortTera(long size) {
        int wholeTB = (int) (size / teraSize);
        return wholeTB + teraPostfix;
    }
    
    private static String formatShortGiga(long size) {
        int wholeGB = (int) (size / gigaSize);
        return wholeGB + gigaPostfix;
    }
    
    private static String formatShortMega(long size) {
        int wholeMB = (int) (size / megaSize);
        return wholeMB + megaPostfix;
    }
    
    private static String formatShortKilo(long size) {
        int wholeKB = (int) (size / kiloSize);
        return wholeKB + kiloPostfix;
    }
    
    private static String formatShortByte(long size) {
        return size + bytePostfix;
    }
}
