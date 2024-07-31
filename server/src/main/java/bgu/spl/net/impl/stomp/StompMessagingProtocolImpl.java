package bgu.spl.net.impl.stomp;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.net.api.StompMessagingProtocol;

import bgu.spl.net.impl.libraries.frame;
import bgu.spl.net.impl.libraries.ActiveConnections;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class StompMessagingProtocolImpl implements StompMessagingProtocol<String>{
    private ActiveConnections activeConnections;
    private Connections connections;
    private int connectionId;
    private String username;
    private boolean shouldTerminate;

    public StompMessagingProtocolImpl() {
        activeConnections = ActiveConnections.getInstance();
    }

    @Override
    public void start(int connectionId, Connections connections) {
        // TODO Auto-generated method stub
        this.connectionId = connectionId;
        this.connections = connections;
        shouldTerminate = false;
    }

    @Override
    public void process(String message) {
        // TODO Auto-generated method stub
        String frameCommand = getFrameCommand(message);
        LinkedHashMap<String, String> msgMap = frame.msgToMap(frameCommand, message);

        String receiptId = null;
        if (msgMap.containsKey("receipt")) receiptId = msgMap.get("receipt");

        frame reply = null;
        if (frame.isLegalFrame(frameCommand, msgMap)) {
            switch (frameCommand) {
                case "CONNECT":
                    reply = ConnectProcess(msgMap, receiptId);
                break;
                case "SEND":
                    reply = SendProcess(msgMap, receiptId);
                break;
                case "SUBSCRIBE":
                    reply = SubscribeProcess(msgMap, receiptId);
                break;
                case "UNSUBSCRIBE":
                    reply = UnsubscribeProcess(msgMap, receiptId);
                break;
                case "DISCONNECT":
                    connections.send(connectionId, frame.toString(frame.receiptFrame(receiptId)));
                    disconnectProcess(msgMap, receiptId);
                break;
            }

        } else {
            reply =  frame.errorFrame("illegal header", receiptId);
        }

        // Should convert reply to string.
        if (!frameCommand.equals("DISCONNECT")) {
            if (reply != null) { 
                connections.send(connectionId , frame.toString(reply)); 
            } else if (receiptId != null) { 
                connections.send(connectionId, frame.toString(frame.receiptFrame(receiptId))); 
            }
        }
    }

    private String getFrameCommand(String msg) {
        return msg.substring(0, msg.indexOf("\n"));
    }

    public frame ConnectProcess(HashMap<String, String> msgMap, String receiptId) {
        frame res = null;
        username = msgMap.get("login");
        String passcode = msgMap.get("passcode");
        if (activeConnections.userExists(username)) {
            if (!activeConnections.isLoggedIn(username)) {
                if (!activeConnections.login(username, passcode)) {
                    return frame.errorFrame("Wrong password", receiptId);
                } else {
                    return frame.connectedFrame(null, receiptId);
                }
            } else {
                return frame.errorFrame("User already logged in", receiptId);
            }
        } else {
            activeConnections.registerUser(activeConnections.getConnectionHandlerById(connectionId), username, passcode); // Adding user to database
            res = frame.connectedFrame(null, receiptId);
        }
        return res;
    }

    public frame SendProcess(HashMap<String, String> msgMap, String receiptId) {

        String destination = msgMap.get("destination");
        ConcurrentLinkedQueue<ConnectionHandler> subscribedConnections = activeConnections.getSubscribedConnectionHandlers(destination);
        if (subscribedConnections.contains(activeConnections.getConnectionHandlerById(connectionId))) {
            for (ConnectionHandler ch : subscribedConnections) {
                int id = activeConnections.getConnectionChannelId(destination, ch);
                frame message = frame.messageFrame(destination, msgMap.get("body") , id, receiptId);
                // Should make a "toString" method for frame class.
                connections.send(activeConnections.getConnectionId(ch), frame.toString(message));
            }
        } else { return frame.errorFrame("You are not subscribed to this channel!\n Subscribe before you send reports.", receiptId); }
        //res = null or RECEIPT, depends.
        return null;
    }

    public frame SubscribeProcess(HashMap<String, String> msgMap, String receiptId) {
        frame res = null;

        String channel = msgMap.get("destination");
        String id = msgMap.get("id");
        boolean success = activeConnections.subscribeToChannel(channel, id, activeConnections.getConnectionHandlerById(connectionId));

        if (success) { res = frame.receiptFrame(receiptId); }
        else res = frame.errorFrame("Subscription failed!", receiptId);

        return res;
    }

    public frame UnsubscribeProcess(HashMap<String, String> msgMap, String receiptId) {
        int id = Integer.parseInt(msgMap.get("id"));
        String destination = activeConnections.geChannelNameByCHId(connectionId, id); // Server receives user-chosen id.
        boolean success = activeConnections.unsubscribeOffChannel(destination, activeConnections.getConnectionHandlerById(connectionId));
        if (!success) 
            return frame.errorFrame("No such channel exists!", receiptId);
        return null;
    }

    public frame disconnectProcess(HashMap<String, String> msgMap, String receiptId) {
        // frame res = frame.receiptFrame(receiptId);
        activeConnections.logout(activeConnections.getConnectionHandlerById(connectionId), username);
        shouldTerminate = true;
        return null;
    }

    @Override
    public boolean shouldTerminate() {
        // TODO Auto-generated method stub
        return shouldTerminate;
    }

}
