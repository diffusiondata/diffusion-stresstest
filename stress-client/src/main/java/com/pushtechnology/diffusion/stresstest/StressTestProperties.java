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

import com.pushtechnology.diffusion.DiffusionException;

/**
 * Singleton class used for accessing properties used by the stress test
 * application.
 *
 * @author Push Technology Limited
 */
public final class StressTestProperties {

    private static final String TESTING_KEY_PREFIX = "testing.";

    private static final String NOOFCLIENTS_KEY =
        TESTING_KEY_PREFIX + "numberofClients";

    private static final String TRANSPORT_TYPE_KEY =
        TESTING_KEY_PREFIX + "transportType";

    private static final String SERVER_HOST_KEY =
        TESTING_KEY_PREFIX + "server.host";

    private static final String HTTP_PORT_KEY = TESTING_KEY_PREFIX +
        "http.port";

    private static final String SSLCLIENT_PORT_KEY =
        TESTING_KEY_PREFIX + "ssl.port";

    private static final String EXTERNALCLIENT_PORT_KEY =
        TESTING_KEY_PREFIX + "netclient.port";

    private static final String NOOFMESSAGES_KEY =
        TESTING_KEY_PREFIX + "numberofDeltaMessages";

    private static final String TOPIC_KEY = "topic";

    private static final String MESSAGE_HANDLER_CLASS =
        TESTING_KEY_PREFIX + "messageHandler.class";

    private static final String CREATION_DELAY =
        TESTING_KEY_PREFIX + "delayAfterClientCreation";

    private static final String INBOUND_THREADPOOL_QUEUE_SIZE =
        TESTING_KEY_PREFIX + "inboundThreadPool.queueSize";
    private static final String INBOUND_THREADPOOL_CORE_SIZE =
        TESTING_KEY_PREFIX + "inboundThreadPool.coreThreads";
    private static final String INBOUND_THREADPOOL_MAX_SIZE =
        TESTING_KEY_PREFIX + "inboundThreadPool.maxThreads";

    private static StressTestPropertiesImpl theInstance =
        new StressTestPropertiesImpl();

    /**
     * Constructor.
     */
    private StressTestProperties() {

    }

    /**
     * @return the size for the queue of inbound messages for a client
     * @throws DiffusionException on error.
     */
    public static int getInboundThreadPoolCoreSize() throws DiffusionException {
        return theInstance.getIntegerProperty(INBOUND_THREADPOOL_CORE_SIZE);
    }

    /**
     * @return the size for the queue of inbound messages for a client
     * @throws DiffusionException on error.
     */
    public static int getInboundThreadPoolMaxSize() throws DiffusionException {
        return theInstance.getIntegerProperty(INBOUND_THREADPOOL_MAX_SIZE);
    }

    /**
     * @return the size for the queue of inbound messages for a client
     * @throws DiffusionException on error.
     */
    public static int getInboundThreadPoolQueueSize() throws DiffusionException {
        return theInstance.getIntegerProperty(INBOUND_THREADPOOL_QUEUE_SIZE);
    }

    /**
     * @return the number of clients to be used in the stress test
     * @throws DiffusionException on error.
     */
    public static int getNoOfClients() throws DiffusionException {
        return theInstance.getIntegerProperty(NOOFCLIENTS_KEY);
    }

    /**
     * @return the transport type to use in the stress test. This can be set to
     *         mixed which indicates to use multiple types
     */
    public static String getTransportType() {
        return theInstance.getProperty(TRANSPORT_TYPE_KEY);
    }

    /**
     * @return the creation delay.
     */
    public static boolean getCreationDelay() {
        return theInstance.getBooleanProperty(CREATION_DELAY);
    }

    /**
     * @return the server name that the diffusion server is running on, which
     *         the stress test will connect to
     */
    public static String getServerHost() {
        return theInstance.getProperty(SERVER_HOST_KEY);
    }

    /**
     * @return the port that is used for http connections on the diffusion
     *         server
     * @throws DiffusionException on error.
     */
    public static int getHttpPort() throws DiffusionException {
        return theInstance.getIntegerProperty(HTTP_PORT_KEY);
    }

    /**
     * @return the port that is used for the external client connections on the
     *         Diffusion server
     * @throws DiffusionException on error
     */
    public static int getNetClientPort() throws DiffusionException {
        return theInstance.getIntegerProperty(EXTERNALCLIENT_PORT_KEY);
    }

    /**
     * @return the ssl client port
     * @throws DiffusionException on error
     */
    public static int getSSLClientPort() throws DiffusionException {
        return theInstance.getIntegerProperty(SSLCLIENT_PORT_KEY);
    }

    /**
     * @return the topic name
     * @throws DiffusionException on error
     */
    public static String getTopic() throws DiffusionException {
        return theInstance.getProperty(TOPIC_KEY);
    }

    /**
     * @return the number of messages that each client in the stress test will
     *         receive before closing A setting of zero will mean the stress
     *         test runs until manual termination
     * @throws DiffusionException on error
     */
    public static int getNoOfMessages() throws DiffusionException {
        return theInstance.getIntegerProperty(NOOFMESSAGES_KEY);
    }

    /**
     * @return the name of the message handler class
     */
    public static String getMessageHandlerClass() {
        return theInstance.getProperty(MESSAGE_HANDLER_CLASS);
    }
}
