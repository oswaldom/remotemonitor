/**
 * March 2013
 * Chat Server
 */
package client;

import GUI.ClientGUI;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Class that attend the socket and request from the user. the socket result
 * is shown in the textArea panel, so that the user types in the panel sends
 * it to the socket.
 * 
 * @author xubuntu
 *
 */
public class ControlClient implements Runnable {

    /**
     * To read data from the socket
     */
    private static DataInputStream dataInput;
    /**
     * To write data in the socket
     */
    private static DataOutputStream dataOutput;
    /**
     * Panel with the controls for the user
     */
    private static ClientGUI panel;
    /**
     * Nick for shown in the window
     */
    private static String nick;
    private static String sala;

    /**
     * Launches a thread to serve the socket.
     *
     * @param socket El socket
     * @param panel El panel del usuario
     */
    public ControlClient(Socket socket, ClientGUI panel, String nick, String sala) {
        this.panel = panel;
        this.nick = nick;
        this.sala = sala;
        try {
            dataInput = new DataInputStream(socket.getInputStream());
            dataOutput = new DataOutputStream(socket.getOutputStream());
            sendNick();
            getUsersFromServer();
            Thread hilo = new Thread(this);
            hilo.start();

        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * Method for send nick to the server
     */
    public void sendNick() {
        try {
            dataOutput.writeUTF("addUser&" + nick);
            dataOutput.flush();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * Method for get the user list from the user
     */
    public static void getUsersFromServer() {
        try {
            dataOutput.writeUTF("getUserList&" + nick);
            dataOutput.flush();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * Method for remove a user from the server
     */
    public static void removeUser() {
        try {
            dataOutput.writeUTF("removeUser&" + nick);
            dataOutput.flush();
        } catch (Exception e) {
            e.getMessage();
        }
    }
    
    public static void kickUser(String nick) {
        try {
            dataOutput.writeUTF("kickUser&" + nick);
            dataOutput.flush();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * Method for send public or private messages depending of the combobox
     */
    public static void actionInterface() {
        try {
            if (panel.getSelectedUser().equals("Todos")) {
                dataOutput.writeUTF("publicMessage&" + nick + "&" +
                        panel.getTexto());
            } else {
                dataOutput.writeUTF("privateMessage&" + nick + "&" +
                        panel.getSelectedUser() + "&" + panel.getTexto());
            }            
            dataOutput.flush();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * Method run for attend the socket. Everything that comes through the
     * socket is written to the panel.
     */
    @Override
    public void run() {
        try {
            while (true) {
                String texto = dataInput.readUTF();
                String[] result = texto.split("&");
                String action = result[0];
                String chat;
                
                panel.setSala(sala);
                
                if (action.equals("addUser")) {
                    String nickResponse = result[1];
                    if (!nickResponse.equals(nick)) {
                        chat = nickResponse + " se ha conectado a la sala.";
                        panel.addText(chat);
                        panel.addText("\n");
                        panel.fillCombo(nickResponse);
                    }
                } else if (action.equals("getUserList") && result[1].equals(nick)) {
                    for (int i = 2; i < result.length; i++) {
                        String nickResponse = result[i];
                        if (!nick.equals(nickResponse)) {
                            panel.fillCombo(nickResponse);
                        }
                    }
                } else if (action.equals("publicMessage")) {
                    panel.addText(result[1] + " dice: " + result[2]);
                    panel.addText("\n");
                } else if (action.equals("privateMessage")) {
                    if (result[1].equals(nick) || result[2].equals(nick)) {
                        panel.addText(result[1] + " dice a " + result[2]
                                + " : " + result[3]);
                        panel.addText("\n");
                    }
                } else if (action.equals("removeUser")) {
                    panel.removeUser(result[1]);
                    panel.addText(result[1] + " se ha desconectado");
                    panel.addText("\n");
                } else if (action.equals("kickUser")) {
                    panel.removeUser(result[1]);
                    panel.addText(result[1] + " ha sido expulsado por el servidor");
                    panel.addText("\n");
                }
                
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * @return the nick
     */
    public String getNick() {
        return nick;
    }

    /**
     * @param nick the nick to set
     */
    public void setNick(String nick) {
        this.nick = nick;
    }
}
