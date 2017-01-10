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
package com.pushtechnology.diffusion.stresstest;

import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import com.pushtechnology.diffusion.DiffusionException;
import com.pushtechnology.diffusion.stresstest.testconnector.BaseClient;
import com.pushtechnology.diffusion.stresstest.testconnector.MessageHandler;
import com.pushtechnology.diffusion.stresstest.testconnector.NetClient;
import com.pushtechnology.diffusion.stresstest.testconnector.SSLClient;
import com.pushtechnology.diffusion.stresstest.testconnector.WebSocketClient;

/**
 * Stress test tool.
 *
 * @author Push Technology Limited
 */
public final class StressTest {

    private static final String TRANSPORT_NET = "client";
    private static final String TRANSPORT_SSL = "ssl";
    private static final String TRANSPORT_WS = "ws";
    private static final String TRANSPORT_MIXED = "mixed";

    private final int theNumberOfClients;

    private final String theHost;
    private final int theHttpPort;
    private final int theNetClientPort;
    private final int theSSLPort;

    private final int theNumberOfMessages;

    private final boolean hasCreationDelay;

    private final String theTransportType;

    private final CountDownLatch theCountDownLatch;

    private Class<?> theMessageHandlerClass = null;

    private List<BaseClient> theClients = null;

    private final Random random = new Random();

    private final CompletionCallback completionCallback =
        new CompletionCallback() {
        @Override
        public void clientFinished(BaseClient client) {
            if (theClients.remove(client)) {
                theCountDownLatch.countDown();
            }
        }
    };

    /**
     * Constructor.
     *
     * @throws DiffusionException On error.
     */
    public StressTest() throws DiffusionException {

        theNumberOfClients = StressTestProperties.getNoOfClients();

        theHost = StressTestProperties.getServerHost();

        theHttpPort = StressTestProperties.getHttpPort();

        theNetClientPort = StressTestProperties.getNetClientPort();

        theSSLPort = StressTestProperties.getSSLClientPort();

        theTransportType = StressTestProperties.getTransportType();

        theNumberOfMessages = StressTestProperties.getNoOfMessages();

        theCountDownLatch = new CountDownLatch(theNumberOfClients);

        try {
            final String className =
                StressTestProperties.getMessageHandlerClass();
            if (className != null && !"".equals(className)) {
                theMessageHandlerClass = Class.forName(className);
            }
        }
        catch (ClassNotFoundException ex) {
            System.err.println(
                "Unable to load message handler class: " +
                    ex.getLocalizedMessage());
        }
        hasCreationDelay = StressTestProperties.getCreationDelay();

        final Thread shutDown = new Thread() {
            @Override
            public void run() {
                System.err.println("StressTest caught closedown");
                if (theClients != null) {
                    for (BaseClient client : theClients) {
                        try {
                            client.close();
                        }
                        catch (DiffusionException ignore) {
                            ignore.printStackTrace();
                        }
                    }
                }
            }
        };

        InboundPoolConfigManager.configInboundPool(); // Necessary to avoid
                                                      // a SEVERE about
                                                      // inbound
                                                      // pool queue size

        // Add the shutdown hook
        Runtime.getRuntime().addShutdownHook(shutDown);
        System.out.println("Starting stress test ...");
    }

    private void setUpConnections() {
        theClients = new Vector<BaseClient>();
        for (int i = 0; i < theNumberOfClients; i++) {
            // try..catch inside loop so that test carries on if a
            // connection fails.
            try {
                System.out.println("Starting .. " + i);
                final BaseClient client = createClient(i);

                if (theMessageHandlerClass != null) {
                    try {
                        client
                            .setMessageHandler((MessageHandler) theMessageHandlerClass
                                .newInstance());
                    }
                    catch (InstantiationException e) {
                        throw new DiffusionException(
                            "failed to create message handler", e);
                    }
                    catch (IllegalAccessException e) {
                        throw new DiffusionException(
                            "failed to create message handler", e);
                    }
                }

                client.connect();

                theClients.add(client);

                if (hasCreationDelay && i % 10 == 0) {
                    // Give the server chance to breath...
                    try {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e) {
                        // ignore.
                        e.printStackTrace();
                    }
                }
            }
            catch (DiffusionException e) {
                e.printStackTrace();
                theCountDownLatch.countDown();
            }
        }
        System.out.println("Finished creating clients ");
    }

    private void waitToFinish() throws InterruptedException {
        theCountDownLatch.await();
    }

    /**
     * Callback used by a client to notify of completion.
     */
    public interface CompletionCallback {
        void clientFinished(BaseClient client);
    }

    private BaseClient createClient(int clientNumber) throws DiffusionException {

        if (theTransportType.equalsIgnoreCase(TRANSPORT_MIXED)) {
            final int randomNumber = random.nextInt() % 3;

            switch(randomNumber) {
            case 0:
                return createNetClient(clientNumber);
            case 1:
                return createWSClient(clientNumber);
            case 2:
                return createSSLClient(clientNumber);
            }
        }
        else if (theTransportType.equalsIgnoreCase(TRANSPORT_NET)) {
            return createNetClient(clientNumber);
        }
        else if (theTransportType.equalsIgnoreCase(TRANSPORT_SSL)) {
            return createSSLClient(clientNumber);
        }
        else if (theTransportType.equalsIgnoreCase(TRANSPORT_WS)) {
            return createWSClient(clientNumber);
        }

        throw new IllegalArgumentException(
            "A valid transport type was not submitted");
    }

    private NetClient createNetClient(int clientNumber)
        throws DiffusionException {
        return new NetClient(
            completionCallback,
            theHost,
            theNetClientPort,
            clientNumber,
            theNumberOfMessages);
    }

    private SSLClient createSSLClient(int clientNumber)
        throws DiffusionException {
        return new SSLClient(
            completionCallback,
            theHost,
            theSSLPort,
            clientNumber,
            theNumberOfMessages);
    }

    private WebSocketClient createWSClient(int clientNumber)
        throws DiffusionException {
        return new WebSocketClient(
            completionCallback,
            theHost,
            theHttpPort,
            clientNumber,
            theNumberOfMessages);
    }

    /**
     * Entry point.
     *
     * @param args command line arguments
     * @throws InterruptedException On error
     * @throws DiffusionException On error
     */
    // CHECKSTYLE.OFF: UncommentedMain
    public static void main(String[] args)
        throws InterruptedException, DiffusionException {
        final StressTest st = new StressTest();
        st.setUpConnections();
        st.waitToFinish();
    }
    // CHECKSTYLE.ON: UncommentedMain
}
