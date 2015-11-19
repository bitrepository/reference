/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityservice.tools;

/**
 * Class to handle launching of CollectionsAdmin 
 */
public class CollectionsAdminLauncher {

    
    public static void main(String[] args) {
        if(!(args.length == 3)) {
            printUsage();
            System.exit(1);
        }
        String method = args[0];
        String collectionID = args[1];
        String settingsPath = args[2];
        
        CollectionsAdmin ca = new CollectionsAdmin(collectionID, settingsPath);
        try {
            ca.invoke(method);
        } catch (UnknownCollectionException e) {
            System.out.println(e.getMessage());
        } catch (InvalidMethodException e) {
            System.out.println(e.getMessage());
        }
    }
    
    private static void printUsage() {
        System.out.println("Usage: collectionAdmin.sh <method> <collectionID> <path-to-settings>");
        System.out.println("Supported methods: add | remove");
    }
}
