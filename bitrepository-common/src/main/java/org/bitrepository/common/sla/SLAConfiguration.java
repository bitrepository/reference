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
package org.bitrepository.common.sla;


/**
 * Defines the configuration parameters for a single Service Level Agreement.
 */
public interface SLAConfiguration {
    /**
     * Returns the ID for this SLA
     */
    public String getSlaId();

	/**
	 * Return the ID for the topic used for messages with are broadcasted to all participants for a given SLA. 
	 * 
	 * See <a href="https://sbforge.org/display/BITMAG/Queues+and+topics#Queuesandtopics-TheperSLAtopic">The per-SLA topic,/a> for details.
	 * @return
	 */
	public String getSlaTopicId();
	
	/**
	 * Return the Id for the topic used for messages with are broadcasted to all clients for a given SLA. 
	 * 
	 * See <a href="https://sbforge.org/display/BITMAG/Queues+and+topics#Queuesandtopics-Thededicatedclientpersistenttopic%3A">The per-SLA topic,/a> for details.
	 */
	public String getClientTopicId();

	/**
	 * Returns the number of pillar participating in this SLA. The number can be used to evaluate whether all pillars
	 * has responded to a broadcast.
	 */
	public int getNumberOfPillars();
	
    /**
     * The location of the directory where the files which are downloaded/uploaded are placed. The location corresponds
     *  to a url exposed by the http server.
     */
    public String getLocalFileStorage();
}
