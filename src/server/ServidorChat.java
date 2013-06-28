package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;

/**
 *
 * @author xubuntu
 */
public class ServidorChat extends Thread {
    
    /** List in which will be stored all the chat conversation*/
    private DefaultListModel chat = new DefaultListModel();
    /*List of all users*/
    public static List usersList = new ArrayList();
    
    //Numero de puerto por defecto (Sala Administradores SO)
    private static int puerto = 5000;   

    public ServidorChat(int puerto) {
        this.puerto = puerto;
    }
    
    public ServidorChat() {
    }
    
    @Override
    public void run() {
        try {
            ServerSocket socketServidor = new ServerSocket(puerto);
            System.out.println("Servidor Chat encendido y esperando conexiones de clientes por el puerto " + puerto);
            
            while(true) {
                Socket client = socketServidor.accept();
                Runnable nuevoCliente = new ClientThread(chat, client);
                Thread thread = new Thread(nuevoCliente);
                thread.start();
            }
        } 
        catch (Exception e) {
            e.getMessage();
        }
    }
}