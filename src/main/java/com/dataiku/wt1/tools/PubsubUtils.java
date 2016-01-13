package com.dataiku.wt1.tools;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.util.Utils;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.PubsubScopes;
import com.google.api.services.pubsub.model.ListTopicsResponse;
import com.google.api.services.pubsub.model.PublishRequest;
import com.google.api.services.pubsub.model.PublishResponse;
import com.google.api.services.pubsub.model.PubsubMessage;
import com.google.api.services.pubsub.model.Topic;

import com.google.common.collect.ImmutableList;

import com.google.common.base.Preconditions;
//import sun.security.rsa.RSAPrivateCrtKeyImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for this sample application.
 */
public final class PubsubUtils {

    /**
     * The application name will be attached to the API requests.
     */
    private static final String APP_NAME = "wt1-Pubsub-processor/1.0";

    /**
     * Prevents instantiation.
     */
    private PubsubUtils() {
    }

    /**
     * Enum representing a resource type.
     */
    public enum ResourceType {
        /**
         * Represents topics.
         */
        TOPIC("topics"),
        /**
         * Represents subscriptions.
         */
        SUBSCRIPTION("subscriptions");
        /**
         * A path representation for the resource.
         */
        private String collectionName;
        /**
         * A constructor.
         *
         * @param collectionName String representation of the resource.
         */
        private ResourceType(final String collectionName) {
            this.collectionName = collectionName;
        }
        /**
         * Returns its collection name.
         *
         * @return the collection name.
         */
        public String getCollectionName() {
            return this.collectionName;
        }
    }

    /**
     * Returns the fully qualified resource name for Pub/Sub.
     *
     * @param resourceType ResourceType.
     * @param project A project id.
     * @param resource topic name or subscription name.
     * @return A string in a form of PROJECT_NAME/RESOURCE_NAME
     */
    public static String getFullyQualifiedResourceName(
            final ResourceType resourceType, final String project,
            final String resource) {
        return String.format("projects/%s/%s/%s", project,
                resourceType.getCollectionName(), resource);
    }

    /**
     * Builds a new Pubsub client with default HttpTransport and
     * JsonFactory and returns it.
     *
     * @return Pubsub client.
     * @throws IOException when we can not get the default credentials.
     */
    public static Pubsub getClient(String secretPath) throws IOException {
        return getClient(Utils.getDefaultTransport(),
                Utils.getDefaultJsonFactory(),
                secretPath);
    }

    /** Authorizes the installed application to access user's protected data. */
/*    private static GoogleCredential authorize() throws Exception {
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(HelloGooglePubSub.class.getResourceAsStream("/client_secrets.json")));

        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets,
                Collections.singleton(CalendarScopes.CALENDAR)).setDataStoreFactory(dataStoreFactory)
                .build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }
*/

    /**
     * Builds a new Pubsub client and returns it.
     *
     * @param httpTransport HttpTransport for Pubsub client.
     * @param jsonFactory JsonFactory for Pubsub client.
     * @return Pubsub client.
     * @throws IOException when we can not get the default credentials.
     */
    public static Pubsub getClient(final HttpTransport httpTransport,
                                   final JsonFactory jsonFactory,
                                   final String secretPath)
            throws IOException {
        Preconditions.checkNotNull(httpTransport);
        Preconditions.checkNotNull(jsonFactory);

        // Build service account credential.
        /*
        // this line works, but i want secret file to be in project resource
        GoogleCredential credential =
                GoogleCredential.getApplicationDefault(httpTransport, jsonFactory);
        */

        // http://stackoverflow.com/questions/31209974/connecting-to-google-compute-engine-using-oauth
        // InputStream in = HelloGooglePubSub.class.getResourceAsStream("/client_secrets.json");
        InputStream in = new FileInputStream(secretPath);
        GoogleCredential credential = GoogleCredential.fromStream(in);

        // I keep this version in case, i need custom option later...
        // this require more dependencies :
        // <dependency>
        // <groupId>com.google.oauth-client</groupId>
        // <artifactId>google-oauth-client-jetty</artifactId>
        // <version>${project.oauth.version}</version>
        // </dependency>
        /*
        GoogleClientSecrets clientSecrets2 =
                GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        httpTransport, jsonFactory, clientSecrets2, PubsubScopes.all())
                        .setAccessType("online").setApprovalPrompt("auto")
                        .build();
        GoogleCredential credential2 = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");

         */


        if (credential.createScopedRequired()) {
            credential = credential.createScoped(PubsubScopes.all());
        }

        // Please use custom HttpRequestInitializer for automatic
        // retry upon failures.
        HttpRequestInitializer initializer =
                new RetryHttpInitializerWrapper(credential);

        return new Pubsub.Builder(httpTransport, jsonFactory, initializer)
                .setApplicationName(APP_NAME)
                .build();
    }

    /**
     * Publishes the given message to the given topic.
     *
     * @param client Cloud Pub/Sub client.
     * @param args Command line arguments.
     * @throws IOException when Cloud Pub/Sub API calls fail.
     */
    public static void publishMessage(Pubsub client, String projectName, String topicName, String message)
            throws IOException {

        String topic = PubsubUtils.getFullyQualifiedResourceName(
                PubsubUtils.ResourceType.TOPIC, projectName, topicName);

        PubsubMessage pubsubMessage = new PubsubMessage()
                .encodeData(message.getBytes("UTF-8"));
        List<PubsubMessage> messages = ImmutableList.of(pubsubMessage);
        PublishRequest publishRequest = new PublishRequest();
        publishRequest.setMessages(messages);
        PublishResponse publishResponse = client.projects().topics()
                .publish(topic, publishRequest)
                .execute();
        List<String> messageIds = publishResponse.getMessageIds();
        if (messageIds != null) {
            for (String messageId : messageIds) {
                System.out.println("Published with a message id: " + messageId);
            }
        }
    }

}