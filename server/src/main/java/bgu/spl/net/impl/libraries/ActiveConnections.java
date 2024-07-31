package bgu.spl.net.impl.libraries;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.net.srv.ConnectionHandler;

public class ActiveConnections<T> {
    ConcurrentHashMap<Integer, ConnectionHandler<T>> activeConnections; // Saves active connections.
    ConcurrentHashMap<ConnectionHandler<T>, Integer> ConnectionHandlerToId; 
    ConcurrentHashMap<ConnectionHandler<T>, ConcurrentHashMap<String, Integer>> ConnectionHandlerToChannelId; // Mapping user to forum + user chosen id.
    ConcurrentHashMap<ConnectionHandler<T>, ConcurrentHashMap<Integer, String>> ConnectionHandlerToIdToChannel; // Mapping Connection Handler to map of user chosen id to channel name.
    ConcurrentHashMap<String, ConcurrentLinkedQueue<ConnectionHandler<T>>> channelToActiveConnections; // Mapping channel to subscribed users.
    ConcurrentHashMap<String, String> usernamePasscode; // Users information
    ConcurrentHashMap<String, Boolean> loggedIn; // Saves information about who is logged in.
    AtomicInteger uniqueId;

    private static class ActiveConnectionsHolder {
        private static ActiveConnections instance = new ActiveConnections<>(); ;
    }

    private ActiveConnections() {
        activeConnections = new ConcurrentHashMap<>();
        ConnectionHandlerToId = new ConcurrentHashMap<>();
        ConnectionHandlerToChannelId = new ConcurrentHashMap<>();
        ConnectionHandlerToIdToChannel = new ConcurrentHashMap<>();
        channelToActiveConnections = new ConcurrentHashMap<>();
        usernamePasscode = new ConcurrentHashMap<>();
        loggedIn = new ConcurrentHashMap<>();
        uniqueId = new AtomicInteger(0);
    }

    public static ActiveConnections getInstance() {
        return ActiveConnectionsHolder.instance;
    }

    public void addActiveConnection(ConnectionHandler ch) {
        int id;
        do {
            id = uniqueId.get();
        }
        while (!uniqueId.compareAndSet(id, id + 1));
        activeConnections.put(id, ch);
        ConnectionHandlerToId.put(ch, id);
    }

    public boolean userExists(String username) {
        return usernamePasscode.containsKey(username);
    }

    public void registerUser(ConnectionHandler ch, String username, String passcode) {
        usernamePasscode.put(username, passcode);
        loggedIn.put(username, false);
        login(username, passcode);
        ConnectionHandlerToChannelId.put(ch, new ConcurrentHashMap<>());
        ConnectionHandlerToIdToChannel.put(ch, new ConcurrentHashMap<>());
    }

    public boolean isLoggedIn(String username) {
        return loggedIn.get(username);
    }

    public boolean login(String username, String passcode) {
        if (usernamePasscode.get(username).equals(passcode)) {
            loggedIn.replace(username, true);
            return true;
        }
        return false;
    } 

    public void logout(ConnectionHandler ch, String username) {
        loggedIn.replace(username, false);
        Integer chID = ConnectionHandlerToId.get(ch);
        ConnectionHandlerToId.remove(ch, chID);
        activeConnections.remove(chID, ch);
        deleteConnectionFromChannels(ch);
    }

    private void deleteConnectionFromChannels(ConnectionHandler ch) {
        ConcurrentHashMap ConnectionHandlerChannels = ConnectionHandlerToChannelId.get(ch);
        Set<String> channelNames = ConnectionHandlerChannels.keySet();
        for (String key : channelNames) {
            channelToActiveConnections.get(key).remove(ch);
        }
    } 

    public boolean unsubscribeOffChannel(String channel, ConnectionHandler ch) {
        if (!channelToActiveConnections.containsKey(channel)) { return false; }

        int id = ConnectionHandlerToChannelId.get(ch).get(channel);
        return channelToActiveConnections.get(channel).remove(ch) && 
        ConnectionHandlerToChannelId.get(ch).remove(channel, id) && 
        ConnectionHandlerToIdToChannel.get(ch).remove(id, channel);
    }

    public boolean subscribeToChannel(String channel, String id, ConnectionHandler ch) {
        if (!channelToActiveConnections.containsKey(channel)) 
            channelToActiveConnections.put(channel, new ConcurrentLinkedQueue<>());
            
        channelToActiveConnections.get(channel).add(ch);
        ConnectionHandlerToChannelId.get(ch).put(channel, Integer.parseInt(id));
        ConnectionHandlerToIdToChannel.get(ch).put(Integer.parseInt(id), channel);
        return true;
    }

    public String geChannelNameByCHId(int chId, int userChannelId) {
        ConnectionHandler ch = getConnectionHandlerById(chId);
        String channelName = ConnectionHandlerToIdToChannel.get(ch).get(userChannelId);
        return channelName;
    }

    public ConcurrentLinkedQueue<ConnectionHandler<T>> getSubscribedConnectionHandlers(String channel) {
        return channelToActiveConnections.get(channel);
    }

    public int getConnectionChannelId(String channel, ConnectionHandler<T> ch) {
        return ConnectionHandlerToChannelId.get(ch).get(channel);
    }

    public int getConnectionId(ConnectionHandler<T> ch) {
        return ConnectionHandlerToId.get(ch);
    }

    public boolean idToConnectionHandler(int id) {
        return activeConnections.containsKey(id);
    }

    public ConnectionHandler<T> getConnectionHandlerById(int id) {
        return activeConnections.get(id);
    }

    /*
     * Ch to delete and it's id.
     */
    public void disconnect(ConnectionHandler<T> ch, Integer connectionId) {
        deleteConnectionFromChannels(ch);
        activeConnections.remove(connectionId, ch);
        ConnectionHandlerToId.remove(ch, connectionId);
        ConnectionHandlerToChannelId.remove(ch);
    }

}
