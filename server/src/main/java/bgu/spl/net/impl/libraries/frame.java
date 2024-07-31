package bgu.spl.net.impl.libraries;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class frame {
    final static String[] CONNECTHeaders = {"accept-version", "host", "login", "passcode"};
    final static String[] SENDHeaders = {"destination"};
    final static String[] SUBSCRIBEHeaders = {"destination", "id"};
    final static String[] UNSUBSCRIBEHeaders = {"id"};
    final static String[] DISCONNECTHeaders = {"receipt"};

    static AtomicInteger messageId = new AtomicInteger(0);

    String command;
    LinkedHashMap<String, String> headers;
    String body;

    public static boolean isLegalFrame(String frameCommand, LinkedHashMap<String, String> frameHeaders) {
        boolean res = true; // default

        switch(frameCommand) {
            case "CONNECT":
                for (String header : CONNECTHeaders)
                    if (!frameHeaders.containsKey(header)) res = false;
                break;

            case "SEND":
                for (String header : SENDHeaders)
                    if (!frameHeaders.containsKey(header)) res = false;
                break;

            case "SUBSCRIBE":
                for (String header : SUBSCRIBEHeaders)
                    if (!frameHeaders.containsKey(header)) res = false;
                break;

            case "UNSUBSCRIBE":
                for (String header : UNSUBSCRIBEHeaders)
                    if (!frameHeaders.containsKey(header)) res = false;
                break;
            
            case "DISCONNECT":
                for (String header : DISCONNECTHeaders)
                    if (!frameHeaders.containsKey(header)) res = false;
                break;    
        }
        
        return res;
    }

    public static LinkedHashMap<String, String> msgToMap(String frameCommand, String msg) {
        String[] msgLines = msg.split("\n"); // Split message by lines.
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        String body = "";
        // Saving headers information 
        if (!frameCommand.equals("SEND")) {
            for (int i = 1; i < msgLines.length; i++) {
                String[] header = msgLines[i].split(":");
                if (header.length == 2)
                    headers.put(header[0], header[1]);
            }
        } else {

            int k = 0;
            // We want to know if there is a receipt
            if (msg.contains("receipt:")) {
                k++;
            }

            for (int i = 1; i < 2 + k; i++) { // 2 headers
                String[] header = msgLines[i].split(":");
                headers.put(header[0], header[1]);
            }

            for (int i = 2 + k; i < msgLines.length; i++) {
                body = body + msgLines[i] + "\n";
            }
            //     for (int i = 1; i < 3 + k; i++) { // 2 headers
            //         String[] header = msgLines[i].split(":");
            //         headers.put(header[0], header[1]);
            //     }

            //     for (int i = 3 + k; i < msgLines.length; i++) {
            //         body = body + msgLines[i];
            //     }
            // } else {

            //     // 1 header
            //     String[] header = msgLines[1].split(":");
            //     headers.put(header[0], header[1]);

            //     for (int i = 3; i < msgLines.length; i++) {
            //         body = body + msgLines[i];
            //     }

            //     
            // }
        }
        headers.put("body", body);

        return headers;
    }

    public frame(String command, LinkedHashMap<String, String> headers, String body) {
        this.command = command;
        this.headers = headers;
        this.body = body;
    }

    public static frame connectedFrame(String msg, String receiptId) {
        LinkedHashMap<String, String> connectedHeaders = new LinkedHashMap<>();
        connectedHeaders.put("version", "1.2");
        if (receiptId != null) { connectedHeaders.put("receipt-id", receiptId); }
        return new frame("CONNECTED", connectedHeaders, null);
    }

    public static frame messageFrame(String channel, String msg, Integer userId, String receiptId) {
        LinkedHashMap<String, String> messageHeaders = new LinkedHashMap<>();
        messageHeaders.put("destination", channel);
        messageHeaders.put("message-id", getMessageId().toString());
        messageHeaders.put("subscription", userId.toString());

        if (receiptId != null) { messageHeaders.put("receipt-id", receiptId); }
        return new frame("MESSAGE", messageHeaders, msg);
    }

    public static frame errorFrame(String msg, String receiptId) {
        LinkedHashMap<String, String> errorHeaders = new LinkedHashMap<>();
        errorHeaders.put("message", msg);
        if (receiptId != null) { errorHeaders.put("receipt-id", receiptId); }
        return new frame("ERROR", errorHeaders, null);
    }

    public static frame receiptFrame(String receiptId) {
        LinkedHashMap<String, String> receiptHeaders = new LinkedHashMap<>();
        receiptHeaders.put("receipt-id", receiptId);
        return new frame("RECEIPT", receiptHeaders, null);
    }

    private static Integer getMessageId() {
        int id;
        do {
            id = messageId.get();
        } while (!messageId.compareAndSet(id, id + 1));
        return id;
    }

    public static String toString(frame typeFrame) {
        // Should implement.
        String frameString = typeFrame.command.toString() + "\n";
        for (String header : typeFrame.headers.keySet()) {
            frameString = frameString + header + ":" + typeFrame.headers.get(header) + "\n";
        }

        if (typeFrame.body != null) { frameString = frameString + typeFrame.body; }
        else { frameString = frameString; }
        return frameString;
    }
}
