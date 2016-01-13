This is a fork of the wt1 tracker from dataiku.

Feature added :
- Google Cloud PubSub storage implementation :
    It publishes the trackedRequest in a json message to your google cloud pubsub server.
     
Requirement :
    You need to configure project name, topic name and the secret json filepath in config.properties
    You also have to get you secret.json from you google cloud console