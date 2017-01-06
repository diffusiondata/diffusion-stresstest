/*******************************************************************************
 * Copyright (C) 2016, 2017 Push Technology Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
* limitations under the License.
 *******************************************************************************/

package com.pushtechnology.diffusion.stresstest.testconnector;

import com.pushtechnology.diffusion.DiffusionException;
import com.pushtechnology.diffusion.api.message.TopicMessage;
import com.pushtechnology.diffusion.api.topic.TopicSet;

/**
 * @author Push Technology Limited
 */
public abstract class BaseClient {

    private final String theHost;

    private final int thePort;

    private final int theThreadNumber;

    protected final long theMaxMessages;

    protected long theNoOfMessages = 0;

    protected long theLastDeltaMessageTime = 0;

    protected long theFirstDeltaMessageTime = 0;

    private MessageHandler theMessageHandler = null;

    private String theClientID;

    /**
     * Constructor.
     *
     * @param host ..
     * @param port ..
     * @param threadNumber ..
     * @param maxMessages ..
     */
    public BaseClient(String host, int port, int threadNumber, long maxMessages) {
        theHost = host;
        thePort = port;
        theThreadNumber = threadNumber;
        theMaxMessages = maxMessages;
    }

    /**
     * Calls a configurable message handler. This allows customers to write
     * their own bespoke message handlers for stress testing callMessageHandler
     *
     * @param message the message.
     */
    protected final void callMessageHandler(TopicMessage message) {
        if (theMessageHandler != null) {
            theMessageHandler.handleMessage(message);
        }
    }

    /**
     * @return true if there's a message handler
     */
    public final boolean hasMessageHandler() {
        return theMessageHandler != null;
    }

    /**
     * displayAverageStats.
     */
    public final void displayAverageStats() {
        final long totalTime = theLastDeltaMessageTime - theFirstDeltaMessageTime;
        final long avgTime = totalTime / theMaxMessages;
        System.out.println(getClientType() + " T" + theThreadNumber + " " +
            theClientID + " : "
            + theMaxMessages + " in " + totalTime + " Avg: " + avgTime);
    }

    /**
     * setMessageHandler.
     *
     * @param handler the handler.
     */
    public final void setMessageHandler(MessageHandler handler) {
        theMessageHandler = handler;
    }

    /**
     * @return the thread number.
     */
    public final long getThreadNumber() {
        return theThreadNumber;
    }

    /**
     * @return the maximum number of messages.
     */
    public final long getMaxMessages() {
        return theMaxMessages;
    }

    /**
     * @return the client id.
     */
    public final String getClientID() {
        return theClientID;
    }

    /**
     * @return the host.
     */
    public final String getHost() {
        return theHost;
    }

    /**
     * @return the port.
     */
    public final int getPort() {
        return thePort;
    }

    /**
     * setClientID.
     *
     * @param id
     */
    public final void setClientID(String id) {
        theClientID = id;
    }

    /**
     * Method which can be overridden by subclasses to run more tests. This
     * method is called after a message has been read. This method is not
     * abstract as some subclasses will not wish to do anything
     * doAdditionalPollTests
     */
    protected void doAdditionalPollTests() {
    }

    /**
     * Connect.
     *
     * @throws DiffusionException
     */
    public abstract void connect() throws DiffusionException;

    /**
     * Send Subscribe.
     *
     * @param topics
     * @throws DiffusionException
     */
    public abstract void sendSubscribe(TopicSet topics)
        throws DiffusionException;

    /**
     * Send Unsubscribe.
     *
     * @param topics
     * @throws DiffusionException
     */
    public abstract void sendUnsubscribe(TopicSet topics)
        throws DiffusionException;

    /**
     * Close.
     *
     * @throws DiffusionException
     */
    public abstract void close() throws DiffusionException;

    /**
     * Send.
     *
     * @param data
     * @param topic
     * @throws DiffusionException
     */
    public abstract void send(String data, String topic)
        throws DiffusionException;

    /**
     * getClientType.
     * @return the client type
     */
    public abstract String getClientType();

}
