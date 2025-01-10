package com.csc3202.lab.project;
// ChatServer.java
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
public class chatServer implements chatService {
    private ConcurrentHashMap<String, clientNotifier> clients = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, clientProfile> userProfiles = new ConcurrentHashMap<>();
    public static void main(String[] args) {
        try {
            chatServer server = new chatServer();
            chatService stub = (chatService) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.createRegistry(5000);
            registry.rebind("ChatService", stub);
            System.out.println("Chat Server is running...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void broadcastMessage(chatMessage message) throws RemoteException {
        for (clientNotifier client : clients.values()) {
            client.receiveMessage(message);
        }
    }

    @Override
    public void registerClient(String username,clientNotifier callback) throws RemoteException {
        clients.put(username, callback);
        userProfiles.put(username, new clientProfile(username));
        broadcastMessage(new chatMessage("Server", username + " has joined the chat.", null, chatMessage.MessageType.USER_JOIN));
        updateClientLists();
    }

    @Override
    public void unregisterClient(String username) throws RemoteException {
        clients.remove(username);
        broadcastMessage(new chatMessage("Server", username + " has left the chat.", null, chatMessage.MessageType.USER_LEAVE));
        updateClientLists();
    }

    @Override
    public List<String> getConnectedUsers() throws RemoteException {
        return new ArrayList<>(clients.keySet());
    }

    @Override
    public void sendPrivateMessage(chatMessage message) throws RemoteException {
        clientNotifier recipient = clients.get(message.getRecipient());
        if (recipient != null) {
            recipient.receiveMessage(message);
            // Also send to sender
            clientNotifier sender = clients.get(message.getSender());
            if (sender != null) {
                sender.receiveMessage(message);
            }
        }
    }

    @Override
    public void updateProfile(String username, clientProfile profile) throws RemoteException {
        userProfiles.put(username, profile);
        broadcastMessage(new chatMessage("Server",
                username + " updated their profile", null, chatMessage.MessageType.SYSTEM));
    }

    @Override
    public clientProfile getUserProfile(String username) throws RemoteException {
        return userProfiles.get(username);
    }

    @Override
    public void setUserStatus(String username, String status) throws RemoteException {
        clientProfile profile = userProfiles.get(username);
        if (profile != null) {
            profile.setStatus(status);
            broadcastMessage(new chatMessage("Server",
                    username + " is now " + status, null,chatMessage.MessageType.STATUS));
        }
    }

    private void updateClientLists() throws RemoteException {
        List<String> userList = getConnectedUsers();
        for (clientNotifier client : clients.values()) {
            client.updateUserList(userList);
        }
    }
}