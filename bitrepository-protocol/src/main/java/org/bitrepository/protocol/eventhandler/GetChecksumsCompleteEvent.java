/*
 * #%L
 * Bitrepository Protocol
 * *
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
package org.bitrepository.protocol.eventhandler;

import org.bitrepository.bitrepositoryelements.ResultingChecksums;


/**
 * Event for successful completion of a GetChecksums operation.
 */
public class GetChecksumsCompleteEvent implements OperationEvent<ResultingChecksums> {
	private final OperationEventType type;
	private final String info;
	private final ResultingChecksums result;

	/**
	 * Constructor with result information.
	 * @param type The event type
	 * @param info Free text description of the event
	 * @param result The result of the GetChecksums operation this event relates to
	 */
	public GetChecksumsCompleteEvent(OperationEventType type, String info, ResultingChecksums result) {
		super();
		this.type = type;
		this.info = info;
		this.result = result;
	}

	@Override
	public String getInfo() {
		return info;
	}

	@Override
	public org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType getType() {
		return type;
	}

	/**
	 * Returns the result of the GetChecksums operation this event relates to.
	 */
	@Override
	public ResultingChecksums getState() {
		return result;
	}
}
