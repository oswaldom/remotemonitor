package dominio;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author oswaldomaestra
 */
public class RemotoImpl extends UnicastRemoteObject implements IRemoto{

    private NodoBD nodoBD = new NodoBD();   
    private ArrayList<Nodo> listaNodo = new ArrayList<Nodo>();
    
    private ArrayList<String> lista_ips;

    public RemotoImpl() throws RemoteException{
        listaNodo = null;
        lista_ips = new ArrayList<String>();
    }

    @Override
    public ArrayList<Nodo> getListaNodos() throws RemoteException{
        nodoBD.conectarBD();
        listaNodo = nodoBD.executeSQL();

        return listaNodo;
    }
    @Override
    public void setListaNodos(ArrayList<Nodo> listaNodo) throws RemoteException{
        this.listaNodo = listaNodo;
    }
    
    @Override
    public ArrayList<String> getListaIps() throws RemoteException {
        return lista_ips;
    }

    @Override
    public void setListaIps(String ip_cliente) throws RemoteException {        
        this.lista_ips.add(ip_cliente);
    }

    @Override
    public void removeListaIps(String ip_remover) throws RemoteException {
        this.lista_ips.remove(ip_remover);
    }

    @Override
    public void shellCommandExecute(String comando) throws RemoteException{
        try {

            
            String command = comando;
            System.out.println(comando);
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(command);


        } catch (IOException ex) {
            Logger.getLogger(RemotoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}