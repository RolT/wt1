package com.dataiku.wt1.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.dataiku.wt1.ConfigConstants;
import com.dataiku.wt1.ProcessingQueue;
import com.dataiku.wt1.ProcessingQueue.Stats;
import com.dataiku.wt1.TrackedRequest;
import com.dataiku.wt1.TrackingRequestProcessor;
import com.dataiku.wt1.Utils;
import com.dataiku.wt1.tools.PubsubUtils;


import com.google.api.services.pubsub.Pubsub;

/**
 * Google Cloud PubSUb implementation of the tracked request processor.
 * It stores data in the Google Cloud PubSub feature.
 * Message are sent as it comes and on the fly
 */
public class GCPubSubStorageProcessor implements TrackingRequestProcessor{
    private long startDate;

    private int writtenBeforeGZ;
    private int writtenEvents;

    public static final String PROJECT_PARAM = "projectName";
    public static final String TOPIC_PARAM = "topicName";
    public static final String SECRET_PARAM = "secretPath";

    private JsonFormatWriter jsonWriter;


    private String projectName;
    private String topicName;
    private String secretPath;
    private Pubsub client;

    @Override
    public void init(Map<String, String> params) throws IOException {
        projectName = params.get(PROJECT_PARAM);
        if (projectName == null) {
            logger.error("Missing configuration parameter " + PROJECT_PARAM);
            throw new IllegalArgumentException(PROJECT_PARAM);
        }
        topicName = params.get(TOPIC_PARAM);
        if (topicName == null) {
            logger.error("Missing configuration parameter " + TOPIC_PARAM);
            throw new IllegalArgumentException(TOPIC_PARAM);
        }
        secretPath = params.get(SECRET_PARAM);
        if (secretPath == null) {
            logger.error("Missing configuration parameter " + SECRET_PARAM);
            throw new IllegalArgumentException(SECRET_PARAM);
        }

        jsonWriter = new JsonFormatWriter(
                Utils.parseCSVToSet(params.get(ConfigConstants.INLINED_VISITOR_PARAMS)),
                Utils.parseCSVToSet(params.get(ConfigConstants.INLINED_SESSION_PARAMS)),
                Utils.parseCSVToSet(params.get(ConfigConstants.INLINED_EVENT_PARAMS)));

        System.out.println(getServletContext().getRealPath("/strs.properties") );


        //Set PubSub Client
        client = PubsubUtils.getClient(secretPath);
    }



    @Override
    public void process(TrackedRequest req) throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("Processing request, curFile=" + writtenBeforeGZ);
        }

        String line = jsonWriter.toJson(req, true);

        byte[] data = line.getBytes("utf8");
        writtenBeforeGZ += data.length;
        writtenEvents++;

        PubsubUtils.publishMessage(client, projectName, topicName, line);

        if (logger.isTraceEnabled()) {
            logger.trace("Written " + writtenBeforeGZ );
        }

    }

    @Override
    public void shutdown() throws IOException {
        // closeClient(false);
    }

    @Override
    public void flush() throws IOException {
        // flushBuffer(true);
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
    }

    private static final Logger logger = Logger.getLogger("wt1.processor.gcpubsub");
}