package server;

import java.io.*;

public class Mensaje_Chat implements Serializable {

    protected static final long serialVersionUID = 1112122200L;

    public static final int MENSAJE = 1, LOGOUT = 2;
    public int tipo;
    public  String mensaje;
	
    public Mensaje_Chat(int type, String message) {
	this.tipo = type;
	this.mensaje = message;
    }
	
    public int getTipo() {
	return tipo;
    }
    
    public String getMensaje() {
    	return mensaje;
    }
}


