package com.csc3202.lab.project;
import java.rmi.RemoteException;
import java.rmi.Remote; 
import java.util.List;
public interface clientNotifier extends Remote {
    void receiveMessage(chatMessage message) throws RemoteException;
    void updateUserList(List<String> users) throws RemoteException;
}

