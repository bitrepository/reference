package org.bitrepository.modify;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bitrepository.bitrepositoryelements.ChecksumsTypeTYPE;

/**
 * Utility class for handling checksum calculations.
 * 
 * TODO perhaps move to Common, so it also can be used elsewhere.
 */
public final class ChecksumUtils {

    /** The magical integer '4'.*/
    private static final int MAGIC_INTEGER_4 = 4;
    /** The magical integer for the hexadecimal '0x0F'.*/
    private static final int MAGIC_INTEGER_OXOF = 0x0F;
    /** The maximal size of the byte array for digest.*/
    private static final int BYTE_ARRAY_SIZE_FOR_DIGEST = 4000;

    /** 
     * Private constructor. To prevent instantiation of this utility class.
     */
    private ChecksumUtils() { }
    
    /**
     * Calculates the checksum for a file based on the given checksums type.
     * The type of checksum is used for finding the corresponding algorithm,
     * which is used for the actual calculation of the checksum.
     * The answer is delivered in hexadecimal form.
     * 
     * @param type The type of checksum.
     * @param file The file to calculate the checksum for.
     * @return The checksum in hexadecimal form.
     */
    public static String getChecksum(ChecksumsTypeTYPE type, File file) {
        try {
            MessageDigest md;
            if(type.equals(ChecksumsTypeTYPE.MD_5)) {
                md = MessageDigest.getInstance("MD5");
            } else if(type.equals(ChecksumsTypeTYPE.SHA_3)) {
                md = MessageDigest.getInstance("SHA");
            } else {
                throw new IllegalArgumentException("The digest is not "
                        + "supported! . . . Yet at least.");
            }
            
            return generateChecksum(file, md);
        } catch (NoSuchAlgorithmException e) {
            throw new ModifyException("Could not digest checksum algorith '" 
                    + type.value() + "'.", e);
        }
    }

    /**
     * Calculates the checksum for a file based on the given checksum algorith.
     * 
     * @param file The file to calculate the checksum for.
     * @param messageDigest The digest algorithm to use for calculating the 
     * checksum of the file.
     * @return The checksum of the file in hexadecimal.
     */
    private static String generateChecksum(File file, 
            MessageDigest messageDigest) {
        byte[] bytes = new byte[BYTE_ARRAY_SIZE_FOR_DIGEST];
        int bytesRead;
        try {
            DataInputStream fis = null;
            try {
                fis = new DataInputStream(new BufferedInputStream(
                        new FileInputStream(file)));
                messageDigest.reset();
                while ((bytesRead = fis.read(bytes)) > 0) {
                    messageDigest.update(bytes, 0, bytesRead);
                }
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
        } catch (IOException e) {
            throw new ModifyException("Could not calculate the checksum with "
                    + "algorithm '" + messageDigest + "' for file '"
                    + file.getAbsolutePath() + "'.", e);
        }
        return toHex(messageDigest.digest());
    }
    
    /**
     * Method for extracting a array of bytes as hex.
     * 
     * @param byteArray The byte array to make into hex.
     * @return The hexadecimal representation of the byte array.
     */
    private static String toHex(byte[] byteArray) {
        // The list of value representations for hexadecimals.
        char[] hexdigit = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
                'd', 'e', 'f'
        };

        StringBuffer sb = new StringBuffer("");
        for (byte b : byteArray) {
            sb.append(hexdigit[(b >> MAGIC_INTEGER_4) & MAGIC_INTEGER_OXOF]);
            sb.append(hexdigit[b & MAGIC_INTEGER_OXOF]);
        }
        return sb.toString();
    }

}
