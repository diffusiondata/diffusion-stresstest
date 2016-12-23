package com.pushtechnology.diffusion.demos.publishers.stress;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.diffusion.api.APIException;
import com.pushtechnology.diffusion.api.message.Encoding;
import com.pushtechnology.diffusion.api.message.TopicMessage;
import com.pushtechnology.diffusion.api.publisher.Client;
import com.pushtechnology.diffusion.api.publisher.Publisher;
import com.pushtechnology.diffusion.api.threads.RunnableTask;
import com.pushtechnology.diffusion.api.threads.ThreadService;
import com.pushtechnology.diffusion.api.topic.CachedTopicLoader;
import com.pushtechnology.diffusion.api.topic.SimpleTopicLoader;
import com.pushtechnology.diffusion.api.topic.Topic;

/**
 * Send some real world data and send it frequently. This publisher is to be
 * used with the stress test tool
 * @author pwalsh
 */
@SuppressWarnings("deprecation")
public final class StressPublisher extends Publisher implements RunnableTask {

    private static final Logger LOG = LoggerFactory.getLogger(StressPublisher.class);

    private static final String DEFAULT_SAMPLE_DATA =
        "1009|0 Mins|8|102:17978|null[9.6,540,1069[9.8,98,1069[10,684,1069[10.2,58,1069[11,2,1069|11,1245,1070" +
        "[11.2,45,1070[11.4,70,1070[11.6,54,1070[11.8,168,1070[12,45,1070|null[1.1,3256,1071[1.18,300,1071" +
        "[1.2,2995,1071[1.25,158,1071[1.3,254,1071|null[1.3,700,1072[1.34,41,1072[1.35,836,1072[1.4,22,1072" +
        "[1.8,2472,1072|null[5.2,12,1073[5.3,650,1073[5.4,784,1073[5.5,65,1073[5.8,462,1073|null[6.1,100,1074" +
        "[6.2,600,1074[6.3,14,1074[6.4,680,1074[6.5,500,1074|null[30,10,1075[34,30,1075[36,11,1075[40,5,1075" +
        "[46,12,1075";

    private static final String TOPIC = "Stress";

    private Topic theTopic;

    private ScheduledFuture<?> theFuture = null;

    private static final String DELAY_PROPERTY = "frequency";

    private static final String ENCODING_PROPERTY = "encoding-mode";

    private static final String MESSAGE_SIZE = "message-size";

    private byte theMessageEncoding = Encoding.NONE.byteValue();

    private int theFrequency = 1000;

    private String theMessage;

    private int theMaximumMessageSize = 0;

    @Override
    protected void initialLoad() throws APIException {
        // get message encoding
        try {
            theMessageEncoding = (byte) getIntegerProperty(ENCODING_PROPERTY);
            LOG.info("Using encoding of {}", theMessageEncoding);
        }
        catch (APIException ignore) {
            LOG.info("Invalid message encoding, using default of NONE");
        }

        // Get frequency
        try {
            theFrequency = getIntegerProperty(DELAY_PROPERTY);
        }
        catch (APIException ignore) {
            LOG.info("Invalid frequency using {}", theFrequency);
        }

        int messageSize = 0;
        // Message size
        try {
            messageSize = getIntegerProperty(MESSAGE_SIZE);
            if (messageSize == 0) {
                theMessage = DEFAULT_SAMPLE_DATA;
            }
            else {
                if (messageSize < DEFAULT_SAMPLE_DATA.length()) {
                    theMessage = DEFAULT_SAMPLE_DATA.substring(0, messageSize);
                }
                else {
                    String tmpMessage = DEFAULT_SAMPLE_DATA;
                    while (tmpMessage.length() <= messageSize) {
                        // Add another message block
                        tmpMessage += DEFAULT_SAMPLE_DATA;
                    }
                    theMessage = tmpMessage.substring(0, messageSize);
                }
            }
        }
        catch (APIException ignore) {
            LOG.info(
                "Stress Test.  Using default message size {}", DEFAULT_SAMPLE_DATA.length());
            theMessage = DEFAULT_SAMPLE_DATA;
        }

        theTopic = getTopic(TOPIC);
        if (theTopic == null) {
            theTopic = addTopic(TOPIC);
        }

        // Set up the topic loader
        addTopicLoader(new StressTopicLoader(), TOPIC);

        // The 100 bytes extra is for encrypted buffers
        theMaximumMessageSize = theMessage.length() + TOPIC.length() + 100;

        LOG.info("Using message size {}", theMaximumMessageSize);
    }

    @Override
    protected void publisherStarted() throws APIException {
        LOG.info("Stress Test started.  Message Size {} bytes every {} ms", theMessage.length() , theFrequency);

        if (theFrequency != 0) {
            theFuture =
                ThreadService.schedule(
                    this,
                    theFrequency,
                    theFrequency,
                    TimeUnit.MILLISECONDS,
                    true);
        }
    }

    @Override
    protected void publisherStopped() {
        if (theFuture != null) {
            theFuture.cancel(false);
        }
    }

    @Override
    protected void messageFromClient(TopicMessage message, Client client) {
        // Broadcast the message to all clients, except the client that sent
        // the message
        try {
            publishExclusiveMessage(message, client);
        }
        catch (APIException ex) {
            LOG.warn("Unable to process message from client", ex);
        }
    }

    @Override
    public void run() {
        try {
            // If some one is out there..
            if (theTopic.hasSubscribers()) {
                // Generate a new message every time, this simulates deltas
                final TopicMessage message =
                    theTopic.createLoadMessage(theMaximumMessageSize);
                message.put(theMessage);
                message.setEncoding(Encoding.parse(theMessageEncoding));
                publishMessage(message);
            }
        }
        catch (APIException ex) {
            LOG.error("Run failure", ex);
            try {
                theFuture.cancel(true);
                stopPublisher();
            }
            catch (APIException ignore) {
                LOG.error("Unable to stop");
            }
        }
    }

    @Override
    protected boolean isStoppable() {
        return true;
    }

    /**
     * Have a cached topic loader as the message doesn't change.
     */
    private class StressTopicLoader extends CachedTopicLoader {

        /**
         * @see SimpleTopicLoader#populateMessage(TopicMessage)
         */
        @Override
        protected void populateMessage(TopicMessage message)
            throws APIException {
            message.put(theMessage);
        }
    }
}
