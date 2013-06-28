package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

public class ChatServer
{
    
    private DefaultListModel chat = new DefaultListModel();
    
    public static List usersList = new ArrayList();
    
    public static void main(String[] args)
    {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } 
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            System.err.println("Unable to find and load MySQL driver");
            System.exit(1);
        }
    
        ServidorChat servidorChat = new ServidorChat();
        servidorChat.start();
        
        ServidorRMI servidorRMI = new ServidorRMI();
        servidorRMI.start();
    }
}
