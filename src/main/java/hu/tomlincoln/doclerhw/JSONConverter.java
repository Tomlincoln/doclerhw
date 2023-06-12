package hu.tomlincoln.doclerhw;

import java.util.Map;

public class JSONConverter {

    public static String toJSON(String host, Map<MonitoringType, MonitoringResult> results) {
        return "{" +
                quote("host") + ":" + quote(host) +
                ", " +
                quote("icmp ping") + ":" + quote(results.get(MonitoringType.ICMP).getLastResult()) +
                ", " +
                quote("tcp ping") + ":" + quote(results.get(MonitoringType.TCP).getLastResult()) +
                ", " +
                quote("trace") + ":" + quote(results.get(MonitoringType.TRACE).getLastResult()) +
                "}";
    }

    private static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }
        char c;
        int i;
        int len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String t;
        sb.append('"');
        for (i = 0; i < len; i += 1) {
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                case '/':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }

}
