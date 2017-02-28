package org.bitrepository.protocol.utils;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;

public final class MessageDataTypeValidator {

	/** Utility class, should never be instantiated */
    private MessageDataTypeValidator() {}
    
    /**
     * Check if a ChecksumDataForFileTYPE has the mandatory elements present.
     *
     * @param obj  the value to check, if null, all checks are skipped
     * @param name the name of the object to check
     * @throws IllegalArgumentException if validation fails
     */
    public static void validate(ChecksumDataForFileTYPE obj, String name) {
    	if(obj != null) {
    		if(obj.getChecksumValue() == null || obj.getChecksumValue().length < 1) {
	            throw new IllegalArgumentException("The '" + name + "' is missing the checksum value.");
	        }
	    	
	    	if(obj.getCalculationTimestamp() == null) {
	    		throw new IllegalArgumentException("The '" + name + "' is missing the calculation timestamp.");
	    	}
	    	
	    	if(obj.getChecksumSpec() == null) {
	    		throw new IllegalArgumentException("The '" + name + "' is missing the checksum spec.");
	    	}
	    	
	    	validate(obj.getChecksumSpec(), name);
    	}
    }
    
    /**
     * Check if a ChecksumSpecTYPE has the mandatory elements present.
     *
     * @param obj  the value to check, if null, all checks are skipped
     * @param name the name of the object to check
     * @throws IllegalArgumentException if validation fails
     */
    public static void validate(ChecksumSpecTYPE obj, String name) {
    	if(obj != null) {
    		if(obj.getChecksumType() == null) {
    			throw new IllegalArgumentException("The '" + name + "' is missing the checksum type.");
    		}
    	}
    }
}
