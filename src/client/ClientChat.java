package client;

import GUI.ClientGUI;
import java.net.Socket;

public class ClientChat {

    private Socket socket;
    private String nick;
    private String sala;

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
