package com.dataiku.wt1.storage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.dataiku.wt1.ProcessingQueue;
import com.dataiku.wt1.TrackedRequest;
import com.dataiku.wt1.Utils;

public class JsonFormatWriter {

    private static DateTimeFormatter isoFormatter = ISODateTimeFormat.dateHourMinuteSecondMillis().withZone(DateTimeZone.UTC);

    private static String escape(String in) {
        if (in == null) return "null";
        return in.replace("\",", "\\\"");
    }

    public JsonFormatWriter(Set<String> inlinedVP, Set<String> inlinedSP, Set<String> inlinedEP) {
        this.inlinedVP = inlinedVP;
        this.inlinedSP = inlinedSP;
        this.inlinedEP = inlinedEP;
        this.thirdPartyCookies =  ProcessingQueue.getInstance().isThirdPartyCookies();
    }

    private boolean thirdPartyCookies;
    private Set<String> inlinedVP;
    private Set<String> inlinedSP;
    private Set<String> inlinedEP;

    /**
     * Write the line of log for one request, with optional terminating newline
     */
    public String toJson(TrackedRequest req, boolean newline) {

        StringBuilder sb = new StringBuilder();
        sb.append("{ server_ts:\"");
        sb.append(isoFormatter.print(req.serverTS));
        sb.append("\", client_ts:\"");
        sb.append(isoFormatter.print(req.clientTS));
        sb.append("\", client_addr:\"");
        sb.append(req.origAddress);
        sb.append("\", visitor_id:\"");
        sb.append(req.visitorId);
        sb.append("\", session_id:\"");
        sb.append(req.sessionId);
        sb.append("\", location:\"");
        sb.append(escape(req.page));
        sb.append("\", referer:\"");
        sb.append(escape(req.referer));
        sb.append("\", user-agent:\"");
        sb.append(escape(req.ua));
        sb.append("\", type:\"");
        sb.append(escape(req.type));
        sb.append("\", visitor_params:\"");
        sb.append(escape(req.visitorParams));
        sb.append("\", session_params:\"");
        sb.append(escape(req.sessionParams));
        sb.append("\", event_params:\"");
        sb.append(escape(req.eventParams));
        sb.append("\", browser_width:\"");
        sb.append(req.browserWidth);
        sb.append("\", browser_height:\"");
        sb.append(req.browserHeight);
        sb.append("\", screen_width:\"");
        sb.append(req.screenWidth);
        sb.append("\", screen_height:\"");
        sb.append(req.screenHeight);
        sb.append("\", browser_language:\"");
        sb.append(req.browserLanguage);
        sb.append("\", tz_offset:\"");
        sb.append(req.tzOffset);
        sb.append("\", ");
        if (thirdPartyCookies) {
            sb.append("global_visitor_id=\"");
            sb.append(req.globalVisitorId);
            sb.append("\",");
        }

        if (req.visitorParams != null && inlinedVP.size() > 0) {
            doInline(Utils.decodeQueryString(req.visitorParams), inlinedVP, sb);
        }
        if (req.sessionParams != null && inlinedSP.size() > 0) {
            doInline(Utils.decodeQueryString(req.sessionParams), inlinedSP, sb);
        }
        if (req.eventParams != null && inlinedEP.size() > 0) {
            doInline(Utils.decodeQueryString(req.eventParams), inlinedEP, sb);
        }

        sb.append("}");

        return sb.toString();
    }

    private static void doInline(Map<String, String[]> decoded, Set<String> inlineRules, StringBuilder sb) {
        for (String toInline : inlineRules) {
            String[] values = decoded.get(toInline);
            sb.append(toInline+"=\"");
            if (values != null && values.length > 0) {
                sb.append(escape(values[0]));
            } else {
                sb.append("");
            }
            sb.append("\", ");
        }
    }
}
