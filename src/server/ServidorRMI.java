package server;


import dominio.Nodo;
import dominio.RemotoImpl;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

/**
 *
 * @author oswaldomaestra
 */
public class ServidorRMI extends Thread{

    //informacion de los nodos
    private ArrayList<Nodo> listaNodo = null;
    private Registry registro = null;

    @Override
    public void run(){
        try {
            registro = LocateRegistry.createRegistry(1099);
            //se crea el servicio
            RemotoImpl remoto = new RemotoImpl();
            remoto.setListaNodos(listaNodo);
            registro.rebind("Nodos", remoto);
            System.out.println("Servidor RMI encendido y esperando "
                    + "conexiones de clientes por el puerto 1099");

        } catch (RemoteException ex) {

        }
    }



}
