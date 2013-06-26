/**
 * March 2013
 * Chat Server
 */
package client;

import GUI.ClientGUI;
import GUI.PanelClientGUI;
import java.net.Socket;

/**
 * Client Chat Class. Create the window, establishes the
 * connection and do the controller instance.
 *
 * @author xubuntu
 *
 */
public class ClientChat {

    /**
     * Server Socket Chat
     */
    private Socket socket;
    private String nick;
    private String sala;

    /**
     * Create the window, establishes the connection and
     * do the controller instance.
     */
    public ClientChat(int port, String ip, String nick, String sala, ClientGUI clientgui) {
        try {
            this.nick = nick;
            this.sala = sala;
            this.socket = new Socket(ip, port);
            
            ControlClient control = new ControlClient(socket, clientgui, nick, sala);
            control.setNick(nick);

        } catch (Exception e) {
            e.getMessage();
        }
        
        
    }

    public Socket getSocket() {
        return socket;
    }
}
