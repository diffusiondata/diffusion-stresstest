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

import static com.pushtechnology.diffusion.utils.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.pushtechnology.diffusion.DiffusionException;
import com.pushtechnology.diffusion.api.APIException;
import com.pushtechnology.diffusion.api.Utils;
import com.pushtechnology.diffusion.api.client.ExternalClientConnection;
import com.pushtechnology.diffusion.api.connection.ConnectionFactory;
import com.pushtechnology.diffusion.api.connection.ServerDetails;
import com.pushtechnology.diffusion.api.topic.TopicSet;
import com.pushtechnology.diffusion.stresstest.StressTest.CompletionCallback;
import com.pushtechnology.diffusion.stresstest.StressTestProperties;
import com.pushtechnology.diffusion.utils.JVMSupport;

/**
 * @author Push Technology Limited
 */
@SuppressWarnings("deprecation")
public class SSLClient extends APIClient {

    private volatile SSLContext theSSLContext = null;

    /**
     * Constructor.
     *
     * @param completionCallback
     *
     * @param host
     * @param port
     * @param threadNumber
     * @param maxMessages
     */
    public SSLClient(CompletionCallback completionCallback, String host,
        int port, int threadNumber, long maxMessages)
        throws DiffusionException {
        super(completionCallback, host, port, threadNumber, maxMessages);
    }

    @Override
    public void connect() throws DiffusionException {
        ServerDetails serverDetails =
            ConnectionFactory.createServerDetails(
                "dpts://" + getHost() + ":" + getPort());

        serverDetails.setInputBufferSize(65536);

        // details.setSSLContext(getSSLContext());
        theClient = new ExternalClientConnection(this, serverDetails);

        TopicSet topics = new TopicSet(StressTestProperties.getTopic());
        setClientID(theClient.connect(topics));
    }

    @Override
    public String getClientType() {
        return "SSL Client";
    }

    @SuppressWarnings("unused")
    private SSLContext getSSLContext() throws GeneralSecurityException, APIException, IOException {

        if (theSSLContext != null) {
            return theSSLContext;
        }
        synchronized (SSLClient.class) {
            if (theSSLContext != null) {
                return theSSLContext;
            }

            // Get the most preferred implementation
            KeyStore ks = KeyStore.getInstance("JKS");

            // This is the default password for the keystore
            char[] password = "password".toCharArray();

            InputStream keyStream = null;
            try {
                keyStream = Utils.getResourceAsStream("sample.keystore");
                ks.load(keyStream, password);
            }
            finally {
                closeQuietly(keyStream);
            }

            theSSLContext = SSLContext.getInstance("TLS");
            KeyManagerFactory kmf = JVMSupport.getKeyManagerFactory();
            kmf.init(ks, password);

            TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory
                    .getDefaultAlgorithm());

            tmf.init(ks);
            theSSLContext
                .init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            return theSSLContext;
        }
    }
}
