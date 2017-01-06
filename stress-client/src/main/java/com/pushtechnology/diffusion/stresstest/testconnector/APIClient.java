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

/**
 * Created on 13 May 2009 by dhudson
 */
package com.pushtechnology.diffusion.stresstest.testconnector;

import com.pushtechnology.diffusion.DiffusionException;
import com.pushtechnology.diffusion.api.Credentials;
import com.pushtechnology.diffusion.api.ServerConnection;
import com.pushtechnology.diffusion.api.ServerConnectionListener;
import com.pushtechnology.diffusion.api.client.ExternalClientConnection;
import com.pushtechnology.diffusion.api.message.TopicMessage;
import com.pushtechnology.diffusion.api.topic.TopicSet;
import com.pushtechnology.diffusion.api.topic.TopicStatus;
import com.pushtechnology.diffusion.stresstest.StressTest.CompletionCallback;
import com.pushtechnology.diffusion.stresstest.StressTestProperties;

/**
 * API client.
 *
 * @author Push Technology Limited
 */
@SuppressWarnings("deprecation")
public abstract class APIClient extends BaseClient
    implements ServerConnectionListener {

    private final CompletionCallback completionCallback;

    /**
     * The External Client API object.
     */
    protected ExternalClientConnection theClient;

    /**
     * Constructor.
     *
     * @param completionCallback
     *
     * @param host ..
     * @param port ..
     * @param threadNumber ..
     * @param maxMessages ..
     */
    public APIClient(CompletionCallback completionCallback, String host,
        int port, int threadNumber, long maxMessages)
        throws DiffusionException {
        super(host, port, threadNumber, maxMessages);
        this.completionCallback = completionCallback;
    }

    /**
     * @see com.pushtechnology.diffusion.stresstest.testconnector.BaseClient#connect()
     */
    @Override
    public void connect() throws DiffusionException {
        theClient =
            new ExternalClientConnection(
                this,
                "dpt://" + getHost() + ":" + getPort());
        TopicSet topics = new TopicSet(StressTestProperties.getTopic());
        setClientID(theClient.connect(topics));
    }

    /**
     * @see com.pushtechnology.diffusion.stresstest.testconnector.BaseClient#close()
     */
    @Override
    public void close() throws DiffusionException {
        if (theClient != null) {
            theClient.close();
            completionCallback.clientFinished(this);
        }
    }

    /**
     * @see com.pushtechnology.diffusion.stresstest.testconnector.BaseClient#send(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void send(String data, String topic) throws DiffusionException {
        if (theClient != null) {
            TopicMessage message = theClient.createDeltaMessage(topic);
            message.put(data);
            theClient.send(message);
        }
    }

    @Override
    public void sendSubscribe(TopicSet topics) throws DiffusionException {
        if (theClient != null) {
            theClient.subscribe(topics);
        }
    }

    @Override
    public void sendUnsubscribe(TopicSet topics) throws DiffusionException {
        if (theClient != null) {
            theClient.unsubscribe(topics);
        }
    }

    @Override
    public void serverConnected(ServerConnection serverConnection) {
        System.out.println(serverConnection + " connected");
    }

    @Override
    public void serverRejectedCredentials(
        ServerConnection serverConnection,
        Credentials credentials) {
        System.out.println(serverConnection + " credentials rejected");
    }

    @Override
    public final void serverDisconnected(ServerConnection serverConnection) {
        System.out.println(serverConnection + " disconnected");
        completionCallback.clientFinished(this);
    }

    @Override
    public void messageFromServer(
        ServerConnection serverConnection,
        TopicMessage message) {

        if (hasMessageHandler()) {
            callMessageHandler(message);
        }

        theNoOfMessages++;
        if (theNoOfMessages == 2) {
            // The 1st message would have been a Topic Load, the 2nd
            // message is actually the first delta message
            theFirstDeltaMessageTime = System.currentTimeMillis();
        }

        doAdditionalPollTests();
        theLastDeltaMessageTime = System.currentTimeMillis();

        if (theMaxMessages != 0 &&
            theNoOfMessages > theMaxMessages) {
            displayAverageStats();
            try {
                close();
            }
            catch (Exception ignore) {
                // ignore
            }
        }
    }

    @Override
    public void serverTopicStatusChanged(
        ServerConnection serverConnection,
        String topicName,
        TopicStatus status) {

    }

}
