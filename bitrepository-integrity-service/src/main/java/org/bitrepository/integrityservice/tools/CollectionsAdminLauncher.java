package org.bitrepository.integrityservice.tools;

/**
 * Class to handle launching of CollectionsAdmin 
 */
public class CollectionsAdminLauncher {

    
    public static void main(String[] args) {
        if(!(args.length == 3)) {
            printUsage();
        }
        String method = args[0];
        String collectionID = args[1];
        String settingsPath = args[2];
        
        CollectionsAdmin ca = new CollectionsAdmin(method, collectionID, settingsPath);
        try {
            ca.invoke();
        } catch (UnknownCollectionException e) {
            System.out.println(e.getMessage());
        } catch (InvalidMethodException e) {
            System.out.println(e.getMessage());
        }
    }
    
    private static void printUsage() {
        System.out.println("Usage: collectionAdmin <method> <collectionID> <path-to-settings>");
        System.out.println("Supported methods: add | remove");
    }
}
