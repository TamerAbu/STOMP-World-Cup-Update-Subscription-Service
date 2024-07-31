package bgu.spl.net.srv;

import java.sql.Connection;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.net.impl.libraries.ActiveConnections;

public class ConnectionsImpl<T> implements Connections<T>{

    ActiveConnections currentConnections;

    public ConnectionsImpl() {
        currentConnections = ActiveConnections.getInstance();
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if (currentConnections.idToConnectionHandler(connectionId)) {
            currentConnections.getConnectionHandlerById(connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void send(String channel, T msg) {
        // Assumes channel exists
        ConcurrentLinkedQueue<ConnectionHandler<T>> subscribed = currentConnections.getSubscribedConnectionHandlers(channel);
        for (ConnectionHandler<T> ch : subscribed) {
            ch.send(msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        // TODO Auto-generated method stub
        if (currentConnections.idToConnectionHandler(connectionId)) {
            ConnectionHandler<T> disconnecting = currentConnections.getConnectionHandlerById(connectionId);
            currentConnections.disconnect(disconnecting, connectionId);
        }

    }
    
}
