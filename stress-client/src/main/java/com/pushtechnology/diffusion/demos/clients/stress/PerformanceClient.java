/*
 * @author pwalsh -
 * Created 8 Dec 2009 : 09:22:08
 */

package com.pushtechnology.diffusion.demos.clients.stress;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.diffusion.api.APIException;
import com.pushtechnology.diffusion.api.Credentials;
import com.pushtechnology.diffusion.api.ServerConnection;
import com.pushtechnology.diffusion.api.ServerConnectionListener;
import com.pushtechnology.diffusion.api.client.ExternalClientConnection;
import com.pushtechnology.diffusion.api.config.ConfigManager;
import com.pushtechnology.diffusion.api.connection.ConnectionFactory;
import com.pushtechnology.diffusion.api.connection.ServerDetails;
import com.pushtechnology.diffusion.api.message.TopicMessage;
import com.pushtechnology.diffusion.api.threads.RunnableTask;
import com.pushtechnology.diffusion.api.threads.ThreadService;
import com.pushtechnology.diffusion.api.topic.TopicStatus;

/**
 * A test client for the Publiser demo.
 *
 * @author pwalsh
 */
public class PerformanceClient
    implements ServerConnectionListener, RunnableTask {

    private static final String URL = "dpts://localhost:8443";
    private static final Logger LOG = LoggerFactory.getLogger(PerformanceClient.class);

    private ExternalClientConnection theConnection;
    private static int theMessageCount = 0;

    /**
     * Entry point for the client.
     *
     * @param arg Command-line arguments.
     */
    //CHECKSTYLE.OFF: UncommentedMain
    public static void main(final String[] arg) {
        try {
            new PerformanceClient().test();
        }
        catch (APIException ex) {
            LOG.info("After " + theMessageCount + " messages");
            ex.printStackTrace();
        }
    }
    //CHECKSTYLE.ON: UncommentedMain

    /**
     * The main body of the client.
     *
     * @throws APIException problem with Diffusion
     */
    public void test() throws APIException {
        ConfigManager.getConfig().setMaximumMessageSize(16665);
        final ServerDetails serverDetails =
            ConnectionFactory.createServerDetails(URL);
        serverDetails.setInputBufferSize(16665);
        serverDetails.setOutputBufferSize(16665);

        theConnection =
            new ExternalClientConnection(this, ConnectionFactory
                .createConnectionDetails(serverDetails));
        theConnection.connect("F1");

        ThreadService.schedule(this, 5, 1, TimeUnit.SECONDS, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageFromServer(
        ServerConnection serverConnection,
        TopicMessage message) {
        theMessageCount++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serverTopicStatusChanged(
        ServerConnection serverConnection,
        String topicName,
        TopicStatus status) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serverConnected(ServerConnection serverConnection) {
        LOG.warn("Connected");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serverRejectedCredentials(
        ServerConnection serverConnection,
        Credentials credentials) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serverDisconnected(ServerConnection serverConnection) {
        LOG.error("Disconnected");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            final TopicMessage message = theConnection.createDeltaMessage("F1");
            message.put("Message from Client");
            theConnection.send(message);

            LOG.info(theMessageCount + " messages received");

        }
        catch (APIException ex) {
            LOG.error("Failed to send message " + ex);
        }

    }

}
