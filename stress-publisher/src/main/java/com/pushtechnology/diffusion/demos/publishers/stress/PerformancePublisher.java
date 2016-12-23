package com.pushtechnology.diffusion.demos.publishers.stress;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.diffusion.api.APIException;
import com.pushtechnology.diffusion.api.message.Record;
import com.pushtechnology.diffusion.api.message.TopicMessage;
import com.pushtechnology.diffusion.api.publisher.Client;
import com.pushtechnology.diffusion.api.publisher.Publisher;
import com.pushtechnology.diffusion.api.threads.RunnableTask;
import com.pushtechnology.diffusion.api.threads.ThreadService;
import com.pushtechnology.diffusion.api.topic.Topic;

/**
 * @author dhudson - Created 26 Sep 2009 : 09:44:42 Modified by jcousins, 20
 *         October 2011 : 06:12:36
 */
@SuppressWarnings("deprecation")
public final class PerformancePublisher extends Publisher implements
    RunnableTask {

    private static final Logger LOG = LoggerFactory.getLogger(PerformancePublisher.class);

    private static final String MESSAGE_COUNT_KEY = "messages";
    private static final int MAX_MESSAGE_SIZE = 2048;
    private ScheduledFuture<?> theFuture = null;
    private static TopicMessage theMessage = null;
    private static boolean isF2 = false;
    private static int theMessageCount = 0;
    private static int theNumberOfMessages = 0;
    private static Client theClient = null;
    private static Topic theTopic = null;
    private static Record theRecord = null;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void subscription(Client client, Topic topic, boolean loaded)
        throws APIException {
        theClient = client;
        theTopic = topic;

        // Read the number of messages from the property file.
        // Do this every time so that you can change the number of messages
        // without re-starting the server
        theNumberOfMessages = getIntegerProperty(MESSAGE_COUNT_KEY);

        final TopicMessage message = topic.createLoadMessage(MAX_MESSAGE_SIZE);
        message.setHeaders(
            Integer.toString(theMessageCount),
            Integer.toString(theNumberOfMessages));

        theRecord = new Record();
        // theRecord.addFields(
        // "123456789012345678901234567890123456789012345678901234567890" +
        // "123456789012345678901234567890");
        theRecord.addFields("1234567890123456789012345678901234567980");
        message.putRecords(theRecord);

        isF2 = "F2".equals(topic.getName());

        // Set the client queue to the number of messages, to stop the queue
        // popping
        client.setMaximumQueueSize(theNumberOfMessages);

        client.send(message);

        if (isF2) {
            theFuture =
                ThreadService.schedule(this, 1, 100, TimeUnit.MILLISECONDS,
                    true);
        }
        else {
            // The 'F1' publisher is 'all guns blazing', so we can't put this in
            // a finite timed scenario
            int i = 0;

            for (; i < theNumberOfMessages; i++) {
                doSendMessage(i + 1);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void unsubscription(Client client, Topic topic) {
        reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void publisherStopped() throws APIException {
        reset();
    }

    private void reset() {
        if (theFuture != null) {
            theFuture.cancel(false);
        }
        theMessageCount = 0;
        theNumberOfMessages = 0;
    }

    private void doSendMessage(int count) {
        try {
            if (theClient.isConnected()) {
                if (count <= theNumberOfMessages) {
                    theMessage = theTopic.createDeltaMessage(MAX_MESSAGE_SIZE);
                    theMessage.setHeaders(Integer.toString(count));
                    theMessage.putRecords(theRecord);
                    theClient.send(theMessage);
                }
                else {
                    LOG.info(
                        theTopic.getName() + " .. Biggest queue size " + theClient.getLargestQueueSize());

                    reset();
                }
            }
            else {
                reset();
            }
        }
        catch (APIException ex) {
            LOG.warn("Failed to send message", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        doSendMessage(++theMessageCount);
    }
}
