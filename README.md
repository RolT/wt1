This is a fork of the wt1 tracker from dataiku.

Feature added :
- Google Cloud PubSub storage implementation :
    It publishes the trackedRequest in a json message to your google cloud pubsub server.
     
Requirement :
    You need to configure project name, topic name and the secret json filepath in config.properties
    You also have to get you secret.json from you google cloud console
    
Traced request result :
{ server_ts:"2016-01-13T13:35:49.188" client_ts:"1970-01-01T00:00:00.000" client_addr:"127.0.0.1" visitor_id:"Z9b847e7e346e499e91b62bb101e20210" session_id:"Ze12e472b1cfb42d698ffc83480d32d15" location:"null" referer:"null" user-agent:"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36" type:"null" visitor_params:"null" session_params:"null" event_params:"" browser_width:"0" browser_height:"0" screen_width:"0" screen_height:"0" browser_language:"null" tz_offset:"null" }