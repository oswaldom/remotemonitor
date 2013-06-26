package dominio;

import client.ClientChat;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 *
 * @author oswaldomaestra
 *  
 */
public interface IRemoto extends Remote{

    public ArrayList<Nodo> getListaNodos() throws RemoteException;
    public void setListaNodos(ArrayList<Nodo> listaNodo) throws RemoteException;
    
    public ArrayList<String> getListaIps() throws RemoteException;
    public void setListaIps(String ip_cliente) throws RemoteException;
    public void removeListaIps(String ip_remover) throws RemoteException;

    public void shellCommandExecute(String comando) throws RemoteException;
}
