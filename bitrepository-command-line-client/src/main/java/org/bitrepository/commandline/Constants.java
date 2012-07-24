package org.bitrepository.commandline;

/**
 * Container for the constants for this package.
 */
public class Constants {
    /**
     * Private constructor to prevent instantiation.
     */
    private Constants() {}
    
    /** If an argument is required.*/
    public static final Boolean ARGUMENT_IS_REQUIRED = true;
    /** If an argument is not required.*/
    public static final Boolean ARGUMENT_IS_NOT_REQUIRED = false;
    
    /** The path to the settings.*/ 
    public static final String SETTINGS_ARG = "s";
    /** The path to the private key.*/
    public static final String PRIVATE_KEY_ARG = "k";
    
    /** The path to the file.*/
    public static final String FILE_PATH_ARG = "f";
    /** The checksum of the file.*/
    public static final String CHECKSUM_ARG = "c";
    /** The argument for the type of checksum to request.*/
    public static final String REQUEST_CHECKSUM_TYPE_ARG = "R";
    /** The argument for the salt of the checksum to request.*/
    public static final String REQUEST_CHECKSUM_SALT_ARG = "S";
}
