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
import com.pushtechnology.diffusion.api.client.ExternalClientConnection;
import com.pushtechnology.diffusion.api.connection.ConnectionFactory;
import com.pushtechnology.diffusion.api.connection.ServerDetails;
import com.pushtechnology.diffusion.api.topic.TopicSet;
import com.pushtechnology.diffusion.stresstest.StressTest.CompletionCallback;
import com.pushtechnology.diffusion.stresstest.StressTestProperties;

/**
 * @author Push Technology Limited
 */
@SuppressWarnings("deprecation")
public class WebSocketClient extends APIClient {

    /**
     * Constructor.
     *
     * @param completionCallback
     * @param host
     * @param port
     * @param threadNumber
     * @param maxMessages
     * @throws DiffusionException
     */
    public WebSocketClient(CompletionCallback completionCallback, String host,
        int port, int threadNumber, long maxMessages)
        throws DiffusionException {
        super(completionCallback, host, port, threadNumber, maxMessages);
    }

    @Override
    public void connect() throws DiffusionException {
        final ServerDetails serverDetails =
            ConnectionFactory.createServerDetails(
                "ws://" + getHost() + ":" + getPort());
        // 64K Input buffer
        serverDetails.setInputBufferSize(65536);

        theClient = new ExternalClientConnection(this, serverDetails);

        final TopicSet topics = new TopicSet(StressTestProperties.getTopic());
        setClientID(theClient.connect(topics));
    }

    @Override
    public String getClientType() {
        return "WebSocket Client";
    }

}
