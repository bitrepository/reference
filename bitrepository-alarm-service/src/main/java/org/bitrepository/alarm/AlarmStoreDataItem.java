/*
 * #%L
 * Bitrepository Alarm Service
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.alarm;
import java.util.StringTokenizer;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositoryelements.Alarm;



/** 
 * Class to contain the data of a received alarm. Contains methods to serialize/deserialize the object 
 * so it can be persisted in a flat file.  
 */
public class AlarmStoreDataItem {

	/** Representation of the items date and time */
	private XMLGregorianCalendar date;
	/** Identifier of the component which raised the alarm */
	private String raiserID;
	/** The alarm code of the item */
	private AlarmCode alarmCode;
	/** The human readable text message of the alarm */
	private String alarmText;

	/**
	 * Private constructor to be used when creating a AlarmStoreDataItem by deserializing from a string. 
	 */
	private AlarmStoreDataItem(XMLGregorianCalendar date, String raiser, AlarmCode alarmCode,
			String alarmText) {
		this.date = date;
		this.raiserID = raiser;
		this.alarmCode = alarmCode;
		this.alarmText = alarmText;
	}

	/**
	 * Publicly accessible  constructor.
	 * @param Alarm Alarm obbject to build the AlarmStoreDataItem from.
	 * @return The fresh AlarmStoreDataItem object. 
	 */
	public AlarmStoreDataItem(AlarmMessage message) {
		Alarm alarm = message.getAlarm();
		date = alarm.getOrigDateTime();
		raiserID = alarm.getAlarmRaiser();
		alarmCode = alarm.getAlarmCode();
		alarmText = alarm.getAlarmText();
	}

	/**
	 * toString method to deliver a HTML representation of the alarm. 
	 * @return A string containing a HTML table row presenting the alarm.  
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<tr><td>");
		sb.append(date.toString());
		sb.append("</td><td>");
		sb.append(raiserID);
		sb.append("</td><td>");
		sb.append(alarmCode.toString());
		sb.append("</td><td>");
		sb.append(alarmText);
		sb.append("</td></tr>");
		return sb.toString();
	}

	/**
	 * Method for serializing the object to a string representation so it can be persisted to disk.
	 * @return The string representation of the object.   
	 */
	public String serialize() {
		return date.toString() + " #!# " + raiserID + " #!# " + alarmCode.toString() + " #!# " + alarmText;
	}

	/**
	 * Method for deserializing the string representation of a AlarmStoreDataItem object to a java object. 
	 * @param String String representation of a AlarmStoreDataItem
	 * @return The AlarmStoreDataItem object corresponding to the data. 
	 */
	public static AlarmStoreDataItem deserialize(String data) throws IllegalArgumentException {
		XMLGregorianCalendar date;
		String raiser;
		AlarmCode alarmCode;
		String alarmText;
		String dateStr;

		StringTokenizer st = new StringTokenizer(data, "#!#");
		if(st.countTokens() != 4) {
			throw new IllegalArgumentException("The input string did not contain excatly 4 tokens");
		}
		try {
			dateStr = st.nextToken();
			date = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateStr.trim());
			raiser = st.nextToken();
			alarmCode = AlarmCode.valueOf(st.nextToken().trim());
			alarmText = st.nextToken();
			return new AlarmStoreDataItem(date, raiser, alarmCode, alarmText);
		} catch (DatatypeConfigurationException e) {
			throw new IllegalArgumentException("The date token is invalid");
		}
	}

}
