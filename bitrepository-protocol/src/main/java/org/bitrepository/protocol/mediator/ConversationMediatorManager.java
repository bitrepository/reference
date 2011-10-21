package org.bitrepository.protocol.mediator;

import java.util.HashMap;
import java.util.Map;

import org.bitrepository.common.settings.Settings;

/** 
 * Get your <code>ConversationMediator</code> here.
 */
public class ConversationMediatorManager {
    /** Map of the loaded mediators */
    private static final Map<String,ConversationMediator> mediatorMap = new HashMap<String,ConversationMediator>();
    
    /**
     * Will return a the <code>ConversationMediator</code> for the collection indicated in the settings. 
     * If the mediator does doesn't exist, it will be created.
     * @param collectionID The collectionID to find settings for.
     * @return The settings for the indicated CollectionID
     */
    public static synchronized ConversationMediator getConversationMediator(Settings settings) {
        String collectionID = settings.getCollectionID();
        if (!mediatorMap.containsKey(collectionID)) {
            ConversationMediator mediator = new CollectionBasedConversationMediator(settings);
            mediatorMap.put(collectionID, mediator);
        }
        return mediatorMap.get(collectionID);
    }
}
