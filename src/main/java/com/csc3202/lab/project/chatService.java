package com.csc3202.lab.project;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface chatService extends Remote {
    void broadcastMessage(chatMessage message) throws RemoteException;
    void registerClient(String username, clientNotifier callback) throws RemoteException;
    void unregisterClient(String username) throws RemoteException;
    List<String> getConnectedUsers() throws RemoteException;
    void sendPrivateMessage(chatMessage message) throws RemoteException;
    void updateProfile(String username, clientProfile profile) throws RemoteException;
    clientProfile getUserProfile(String username) throws RemoteException;
    void setUserStatus(String username, String status) throws RemoteException;
}