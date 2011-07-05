/*
 * #%L
 * bitrepository-common
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.protocol.bitrepositorycollection;


/**
 * Defines the configuration parameters for a single Service Level Agreement.
 */
public interface ClientSettings extends BitRepositoryCollectionSettings {
    /**
	 * Return the ID for the topic used for messages with are broadcasted to all participants for a given SLA. 
	 * 
	 * See <a href="https://sbforge.org/display/BITMAG/Queues+and+topics#Queuesandtopics-TheperSLAtopic">The per-SLA topic,/a> for details.
	 * @return
	 */
	public String getBitRepositoryCollectionTopicID();
	
	/**
	 * Return the Id for the topic used for messages with are broadcasted to all clients for a given SLA. 
	 * 
	 * See <a href="https://sbforge.org/display/BITMAG/Queues+and+topics#Queuesandtopics-Thededicatedclientpersistenttopic%3A">The per-SLA topic,/a> for details.
	 */
	public String getClientTopicID();

	/**
	 * Returns the number of pillar participating in this SLA. The number can be used to evaluate whether all pillars
	 * has responded to a broadcast.
	 */
	public String[] getPillarIDs();
	
    /**
     * The location of the directory where the files which are downloaded/uploaded are placed. The location corresponds
     *  to a url exposed by the http server. This might be null in case of no shared file system.
     */
    public String getLocalFileStorage();
    
    /**
     * The general timeout to use for conversations. Concrete timeouts are normally defined for the different phases of 
     * an operation, but this general 'last-ditch' timeout is used if the internal conversation somehow timeout 
     * consistency breaks and the conversation freezes.
     * 
     * @Return The conversation timeout in milliseconds.
     */
    public long getConversationTimeout();

    /**
     * The general timeout to use for identifying pillars.
     * 
     * @Return The time to wait for identify responses before continuing.
     */
	public long getIdentifyPillarsTimeout();

	/**
	 * The period of time between each cleaning of obsolete conversations by the conversation mediators.
	 * @return
	 */
    public long getMediatorCleanInterval();
}
