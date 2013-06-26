/**
 * 25 March 2013 
 * Chat Server
 */
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import javax.swing.DefaultListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Thread to attend clients
 *
 * @author xubuntu
 */
public class ClientThread implements Runnable, ListDataListener {

    /**
     * List where the conversation will be stored
     */
    private DefaultListModel chat;
    
    /**
     * Socket where client is connected
     */
    private Socket socket;
    
    /**
     * Input Channel
     */
    private DataInputStream dataInput;
    
    /**
     * Output Channel
     */    
    private DataOutputStream dataOutput;

    /**
     * Create an instance of the chat
     *
     * @param chat All the conversation in the chat
     * @param socket Socket with the client.
     */
    public ClientThread(DefaultListModel chat, Socket socket) {
        
        this.chat = chat;
        this.socket = socket;
        try {
            
            dataInput = new DataInputStream(socket.getInputStream());
            dataOutput = new DataOutputStream(socket.getOutputStream());
            chat.addListDataListener(this);
            
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * Manages the conversation. Takes care of everything that happens.
     */
    @Override
    public void run() {
        try {
            while (true) {

                String response = "";
                /* Read everything that comes from the client*/ 
                String texto = dataInput.readUTF(); 
                
                /*Split the message that comes from the user and puts ir in an
                 array*/
                String[] result = texto.split("&");
                
                String action = result[0]; //Action
                System.out.println(action);

                if (action.equals("addUser")) {
                    String nick = result[1];
                    ServidorChat.usersList.add(nick);
                    response = "addUser&" + nick;
                } else if (action.equals("getUserList")) {
                    String nick = result[1];
                    response = "getUserList&" + nick;
                    for (int i = 0; i < ServidorChat.usersList.size(); i++) {
                        if (!nick.equals(ServidorChat.usersList.get(i))) {
                            response += "&" + ServidorChat.usersList.get(i);
                        }
                    }
                } else if (action.equals("publicMessage")) {
                    response = "publicMessage&" + result[1] + "&" + result[2];
                } else if (action.equals("privateMessage")) {
                    response = "privateMessage&" + result[1] + "&" + result[2] 
                            + "&" + result[3];
                } else if (action.equals("removeUser")){
                    response = "removeUser&" + result[1];
                    for (int i = 0; i < ServidorChat.usersList.size(); i++) {
                        if(ServidorChat.usersList.get(i).equals(result[1])){
                            ServidorChat.usersList.remove(i);
                            break;
                        }
                    }
                } else if (action.equals("kickUser")){
                    response = "kickUser&" + result[1];
                    for (int i = 0; i < ServidorChat.usersList.size(); i++) {
                        if(ServidorChat.usersList.get(i).equals(result[1])){
                            ServidorChat.usersList.remove(i);
                            break;
                        }
                    }
                }

                synchronized (chat) {
                    chat.addElement(response);
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

       
    /*
     * Send the last message of the conversation through the socket.
     * This method is calle each time the chat is updated with messages
     */
    @Override
    public void intervalAdded(ListDataEvent e) {
        String texto = (String) chat.getElementAt(e.getIndex0());
        try {
            dataOutput.writeUTF(texto);
            dataOutput.flush();
        } catch (Exception excepcion) {
            excepcion.getMessage();
        }
    }

    /**
     * Nothing
     */
    @Override
    public void intervalRemoved(ListDataEvent e) {
    }

    /**
     * Nothing
     */
    @Override
    public void contentsChanged(ListDataEvent e) {
    }
}
