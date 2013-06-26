/**
 * 25 March 2013
 * Chat Server
 */
package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

/**
 * Chat Server
 * Accepts connections of clients, create threads for each user and wait
 * for more connections
 * @author xubuntu
 *
 */
public class ChatServer
{
    
    /** List in which will be stored all the chat conversation*/
    private DefaultListModel chat = new DefaultListModel();
    
    /*List of all users*/
    public static List usersList = new ArrayList();
    
    /*Port Number*/
    private static int PORT = 5000;
    
    
    /**
     * Instantiate this class
     * @param args
     */
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
   
    /*
     * It starts an infinite while to attend clients, it launches a thread
     * for each user
     */
//    public ChatServer()
//    {
//        try
//        {
//            ServerSocket socketServidor = new ServerSocket(PORT);
//            while (true)
//            {
//                Socket client = socketServidor.accept();
//                Runnable nuevoCliente = new ClientThread(chat, client);
//                Thread thread = new Thread(nuevoCliente);
//                thread.start();
//            }
//        } catch (Exception e)
//        {
//            e.getMessage();
//        }
//    }

}
