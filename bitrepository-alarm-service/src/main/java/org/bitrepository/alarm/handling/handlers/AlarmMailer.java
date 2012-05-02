/*
 * #%L
 * bitrepository-access-client
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
package org.bitrepository.alarm.handling.handlers;

import java.net.InetAddress;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.bitrepository.alarm.AlarmException;
import org.bitrepository.alarm.handling.AlarmHandler;
//import org.bitrepository.alarm_service.alarmconfiguration.AlarmConfiguration;
//import org.bitrepository.alarm_service.alarmconfiguration.AlarmConfiguration.MailingConfiguration;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.settings.referencesettings.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A quite simple AlarmHandler, which sends an mail with the Alarm.
 * TODO have a setting for how often, a mail should be send.
 */
public class AlarmMailer implements AlarmHandler {
    
    /** The logger to log the Alarms.*/
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    /** The message receiver.*/
    private final String messageReceiver;
    /** The message sender.*/
    private final String messageSender;
    /** The mail server.*/
    private final String mailServer;
    
    /** The key for the MAIL_FROM_PROPERTY.*/
    private static final String MAIL_FROM_PROPERTY_KEY = "mail.from";
    /** The key for the MAIL_HOST_PROPERTY.*/
    private static final String MAIL_HOST_PROPERTY_KEY = "mail.host";
    /** The default mimetype of a mail.*/
    private static final String MIMETYPE = "text/plain";
    
    /**
     * Constructor.
     */
    public AlarmMailer(AlarmServiceSettings settings) {
        MailingConfiguration config = settings.getMailingConfiguration();
        this.messageReceiver = config.getMailReceiver();
        this.messageSender = config.getMailSender();
        this.mailServer = config.getMailServer();
        log.debug("Instantiating the alarmhandler '" + this.getClass().getCanonicalName() + "'");
    }
    
    @Override
    public void handleAlarm(AlarmMessage msg) {
        String subject = "Received alarm '" + msg.getAlarm() + "'";
        log.info(subject + ":\n{}", msg.toString());
        sendMail(subject, msg.toString());
    }
    
    @Override
    public void handleOther(Object msg) {
        String subject = "Received unexpected object of type '" + msg.getClass() + "'";
        log.info(subject + ":\n{}", msg.toString());
        sendMail(subject, msg.toString());
    }
    
    /**
     * Method for sending a mail.
     * 
     * TODO: put into utility class?
     * 
     * @param subject The subject of the mail.
     * @param content The content of the mail.
     */
    private void sendMail(String subject, String content) {
        Properties props = makeMailProperties();
        Session session = Session.getDefaultInstance(props);
        Message msg = new MimeMessage(session);
        
        addMailBody(content, msg);
        addMailHeader(subject, msg);
        sendMessage(msg);
    }
    
    /**
     * Method for generating the properties of the mail.
     * @return The properties for the mail.
     */
    private Properties makeMailProperties() {
        Properties props = new Properties();
        props.put(MAIL_FROM_PROPERTY_KEY, messageSender);
        props.put(MAIL_HOST_PROPERTY_KEY, mailServer);
        
        return props;
    }
    
    /**
     * Method for generating the body of the mail.
     * @param content The content of the body.
     * @param msg The message which should have the body.
     * @return The body of the mail.
     */
    private void addMailBody(String content, Message msg) {
        StringBuffer body = new StringBuffer();
        try {
            // Make the body of the mail.
            body.append("Host: " + InetAddress.getLocalHost().getCanonicalHostName() + "\n");
            body.append("Date: " + new Date().toString() + "\n");
            body.append(content);
            body.append("\n");
            msg.setContent(body.toString(), MIMETYPE);
        } catch (Exception e) {
            throw new AlarmException("Could not create mail body.", e);
        }
    }
    
    /**
     * Method for generating and adding the header to the mail. 
     * @param subject The subject of 
     * @param msg
     */
    private void addMailHeader(String subject, Message msg) {
        try {
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            msg.setFrom(new InternetAddress(messageSender));
            
            addReceiversToMessage(msg);
        } catch (MessagingException e) {
            throw new AlarmException("Could not create header.", e);
        }
    }
    
    /**
     * Method for adding the message receivers to the message.
     * @param msg The message which should be have the receivers added.
     */
    private void addReceiversToMessage(Message msg) {
        // to might contain more than one e-mail address
        for (String toAddressS : messageReceiver.split(",")) {
            try {
                InternetAddress toAddress = new InternetAddress(toAddressS.trim());
                msg.addRecipient(Message.RecipientType.TO, toAddress);
            } catch (Exception e) {
                throw new AlarmException("To address '" + toAddressS + "' is not a valid email address", e);
            }
        }
        
        try {
            if (msg.getAllRecipients().length == 0) {
                throw new AlarmException("No valid recipients in '" + messageReceiver + "'");
            }
        } catch (MessagingException e) {
            throw new AlarmException("Cannot handle recipients of the message '" + msg + "'", e);
        }
    }
    
    /**
     * Method for sending the message.
     * @param msg The message to send.
     */
    private void sendMessage(Message msg) {
        try {
            Transport.send(msg);
        } catch (MessagingException e) {
            throw new AlarmException("Could not send email: " + msg, e);
        }
    }
    
    @Override
    public void close() {
        log.debug("Closing the alarmhandler '" + this.getClass().getCanonicalName() + "'");
    }
}
