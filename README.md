This is a fork of the wt1 tracker from dataiku.

Feature added :
- Google Cloud PubSub storage implementation :
    It publishes the trackedRequest in a json message to your google cloud pubsub server.
     
Requirement :
    You need to configure project name, topic name and the secret json filepath in config.properties
    You also have to get you secret.json from you google cloud console
    
Traced request result :
{ server_ts:"2016-01-13T13:43:22.477", client_ts:"1970-01-01T00:00:00.000", client_addr:"127.0.0.1", visitor_id:"Zf119292037874c5daa2470acee30b9d7", session_id:"Za507b88a19ee4c79b60d179abc3cf2f4", location:"null", referer:"null", user-agent:"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36", type:"null", visitor_params:"null", session_params:"null", event_params:"", browser_width:"0", browser_height:"0", screen_width:"0", screen_height:"0", browser_language:"null", tz_offset:"null" }