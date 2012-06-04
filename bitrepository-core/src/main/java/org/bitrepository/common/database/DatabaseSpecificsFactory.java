package org.bitrepository.common.database;

/**
 * Factory for instantating the specifics for a database connection.
 */
public class DatabaseSpecificsFactory {
    /**
     * Retrieves the specified class with the database connection specifics.
     * @param className The name of the database specifics.
     * @return The requested database specifics class.
     */
    @SuppressWarnings("unchecked")
    public static DBSpecifics retrieveDBSpecifics(String className) {
        try {
            Class<DBSpecifics> dbSpecs = (Class<DBSpecifics>) Class.forName(className);
            
            return (DBSpecifics) dbSpecs.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not instantiate the specifics for the database", e);
        }
    }
}
