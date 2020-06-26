/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.common.utils;

import java.util.Locale;

/**
 * Util class for handling formatting of datasizes. 
 */
public class FileSizeUtils {

    private static final int unitSize = 1000;
    private static final long byteSize = 1;
    private static final long kiloSize = byteSize * unitSize;
    private static final long megaSize = kiloSize * unitSize;
    private static final long gigaSize = megaSize * unitSize;
    private static final long teraSize = gigaSize * unitSize;
    private static final long petaSize = teraSize * unitSize;
    private static final long exaSize = petaSize * unitSize;

    private static final String bytePostfix = " B";
    private static final String kiloPostfix = " KB";
    private static final String megaPostfix = " MB";
    private static final String gigaPostfix = " GB";
    private static final String teraPostfix = " TB";
    private static final String petaPostfix = " PB";
    private static final String exaPostfix = " EB";

    private static final String decimalFormat = "%.2f";

    /*
     * Returns the appropiate unit for a given byte size. Ie. MB or GB
     * 
     */	
    public static String toHumanUnit(Long size) {
        if (size == null) {
            return bytePostfix;
        }
        if (size >= exaSize) {
            return exaPostfix;
        } else if (size >= petaSize) {
            return petaPostfix;
        } else if (size >= teraSize) {
            return teraPostfix;
        } else if (size >= gigaSize) {
            return gigaPostfix;
        } else if (size >= megaSize) {
            return megaPostfix;
        } else if (size >= kiloSize) {
            return kiloPostfix;
        } else {
            return bytePostfix;
        }
    }


    /*	 
     *  Returns the number of bytes for a given unit (ie. MB etc)
     */	
    public static long getByteSize(String unit) {

        if (exaPostfix.equals(unit)) {
            return exaSize;
        } else if (petaPostfix.equals(unit)) {
            return petaSize;
        } else if (teraPostfix.equals(unit)) {
            return teraSize;
        } else if (petaPostfix.equals(unit)) {
            return petaSize;
        } else if (gigaPostfix.equals(unit)) {
            return gigaSize;
        } else if (megaPostfix.equals(unit)) {
            return megaSize;
        } else if (kiloPostfix.equals(unit)) {
            return kiloSize;
        } else if (bytePostfix.equals(unit)) {
            return byteSize;
        } else {
            return byteSize;
        }

    }

    /*
     * Formats bytes to standards. 
     * Ie 1024 bytes -> 1KB 
     */	
    public static String toHumanShort(Long size) {
        if (size == null) {
            return "0 B";
        }
        if (size >= exaSize) {
            return formatShortExa(size);
        } else if (size >= petaSize) {
            return formatShortPeta(size);
        } else if (size >= teraSize) {
            return formatShortTera(size);
        } else if (size >= gigaSize) {
            return formatShortGiga(size);
        } else if (size >= megaSize) {
            return formatShortMega(size);
        } else if (size >= kiloSize) {
            return formatShortKilo(size);
        } else {
            return formatShortByte(size);
        }
    }

    /**
     * Formats bytes to standards
     * i.e. 2283 bytes becomes 2.23 KB
     * @param size the number of bytes, as a long
     * @return the byte size as a human readable value
     */
    public static String toHumanShortDecimal(Long size) {
        if (size == null) {
            return "0 B";
        }
        if (size >= exaSize) {
            return formatShortDecimalExa(size);
        } else if (size >= petaSize) {
            return formatShortDecimalPeta(size);
        } else if (size >= teraSize) {
            return formatShortDecimalTera(size);
        } else if (size >= gigaSize) {
            return formatShortDecimalGiga(size);
        } else if (size >= megaSize) {
            return formatShortDecimalMega(size);
        } else if (size >= kiloSize) {
            return formatShortDecimalKilo(size);
        } else {
            return formatShortByte(size);
        }
    }

    private static String formatShortExa(long size) {
        int wholeEB = (int) (size / exaSize);
        return wholeEB + exaPostfix;
    }

    private static String formatShortDecimalExa(long size) {
        double EB = ((double) size / exaSize);
        return String.format(Locale.ROOT, decimalFormat, EB) + exaPostfix;
    }

    private static String formatShortPeta(long size) {
        int wholePB = (int) (size / petaSize);
        return wholePB + petaPostfix;
    }

    private static String formatShortDecimalPeta(long size) {
        double PB = ((double) size / petaSize);
        return String.format(Locale.ROOT, decimalFormat, PB) + petaPostfix;
    }

    private static String formatShortTera(long size) {
        int wholeTB = (int) (size / teraSize);
        return wholeTB + teraPostfix;
    }

    private static String formatShortDecimalTera(long size) {
        double TB = ((double) size / teraSize);
        return String.format(Locale.ROOT, decimalFormat, TB) + teraPostfix;
    }	

    private static String formatShortGiga(long size) {
        int wholeGB = (int) (size / gigaSize);
        return wholeGB + gigaPostfix;
    }

    private static String formatShortDecimalGiga(long size) {
        double GB = ((double) size / gigaSize);
        return String.format(Locale.ROOT, decimalFormat, GB) + gigaPostfix;
    }

    private static String formatShortMega(long size) {
        int wholeMB = (int) (size / megaSize);
        return wholeMB + megaPostfix;
    }

    private static String formatShortDecimalMega(long size) {
        double MB = ((double) size / megaSize);
        return String.format(Locale.ROOT, decimalFormat, MB) + megaPostfix;
    }

    private static String formatShortKilo(long size) {
        int wholeKB = (int) (size / kiloSize);
        return wholeKB + kiloPostfix;
    }

    private static String formatShortDecimalKilo(long size) {
        double KB = ((double) size / kiloSize);
        return String.format(Locale.ROOT, decimalFormat, KB) + kiloPostfix;
    }

    private static String formatShortByte(long size) {
        return size + bytePostfix;
    }
}
