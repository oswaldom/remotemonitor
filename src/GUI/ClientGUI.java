/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import client.ClientChat;
import client.ControlClient;
import dominio.IRemoto;
import dominio.Proceso;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import server.ServidorChat;
import server.ServidorRMI;

/**
 *
 * @author xubuntu
 */
public class ClientGUI extends javax.swing.JFrame {

    private Socket socket = null;
    private static int PORT = 5000;
    private static String ipServidorActual;
    private static String ipServidor;
    private static String ipLocal;
    private static String nick;
    private static String sala;   
    
    private static ArrayList<String> listaClientesConectados;
    private static ArrayList<Integer> listaUltimoOcteto;
    
    private boolean servidor;
    
    Registry registro = null;
    DefaultListModel listModel = null;
    
    IRemoto listaNodo = null;    
    
    int nodoGlobal = 0;
    RefreshStats refresh;
    ControlClient control;
    ClientChat client;
    UpdateIpList updateIpList;
    
    /**
     * Creates new form ClientGUI
     */
    
    public ClientGUI() {
        initComponents();
        
        this.setLocationRelativeTo(null);
        this.disposeServerResources();
        this.jButton1.setEnabled(false);
        this.jButtonDisconnectChat.setEnabled(false);
        
        jComboBoxUsers.addItem("Todos");
        jTextAreaChat.setEditable(false);
        jComboBoxUsers.setEnabled(false);
        jLabelNick.setEnabled(false);
        jLabelSala.setEnabled(false);
        jTextAreaChat.setEnabled(false);
        jTextFieldChat.setEnabled(false);
        
        this.setResizable(false);
        
        //Inicializando las variables.
        listaClientesConectados = new ArrayList();
        listaUltimoOcteto = new ArrayList();
        servidor = false;
        
        ipLocal = "";
        updateIpList = new UpdateIpList();
        refresh = new RefreshStats();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                try {
                    ControlClient.removeUser();
                    listaNodo.removeListaIps(ipLocal);
                    if (socket != null)
                        socket.close();
                    System.exit(0);
                } catch (IOException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        try {
            ipLocal = getFirstNonLoopbackAddress();
            System.out.println("Ip Local: " + ipLocal);    
            
            
        } catch (SocketException ex) {
            System.out.println("No se pudo determinar la ipLocal");
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }       
        
        if (!"".equals(ipServidor)) {
            this.jButton1.setEnabled(true);
            this.textfieldIpNodo.setEnabled(true);

            this.jComboBox1.setEnabled(true);
            this.jTextAreaProcesos.setVisible(true);
            
        }
        
        if (connectionRMI() == false) {
           this.grandulon();
        }
        
    }
    
    private void grandulon() {           
        
        if (listaClientesConectados.isEmpty()){
            System.out.println("LISTA DE IP'S ESTA VACIA");
            this.levantarServidor();
        }
        else {
          
            String ip_actual;

            //Calcular lista con los ultimos octetos de cada IP de la lista.
            for(int i = 0;i<listaClientesConectados.size();i++) {
                ip_actual = listaClientesConectados.get(i);            
                String[] octetos = ip_actual.split("\\.");           

                int ultimo_octeto = Integer.parseInt(octetos[3]);
                //Porque va de 0 a 3, el 4to octeto es el que se agarra

                listaUltimoOcteto.add(ultimo_octeto);
            }

            //Algoritmo para calcular el octeto mayor de la lista de octetos.
            Integer octeto_mayor = listaUltimoOcteto.get(0);

            for(int i = 0;i<listaUltimoOcteto.size();i++) {
                if(listaUltimoOcteto.get(i) > octeto_mayor) 
                    octeto_mayor = listaUltimoOcteto.get(i);
            }

            //Extrayendo la IP con el octeto mayor
            for(int i = 0;i<listaClientesConectados.size();i++) {
                ip_actual = listaClientesConectados.get(i);
                if(ip_actual.endsWith(octeto_mayor.toString())) {
                    ipServidor = listaClientesConectados.get(i);
                    System.out.println("IP ACTUAL: " + ip_actual + " Octeto mayor: " + octeto_mayor);
                    break;
                }
            } 
            
            if (ipServidor.equals(ipLocal)) {
                this.levantarServidor();
            }
            else {
                
                while (this.connectionRMI() == false){
                    try {
                        Thread.sleep(10000);
                        System.out.println("Intentando conectar a RMI...");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
            }
            
            if (socket != null){
                try {
                    socket.close();
                    socket = new Socket(ipServidor, PORT);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                
                control = new ControlClient(socket, this, nick, sala);
                control.setNick(nick);
                
            }

            System.out.println("Ip Nuevo Servidor: " + ipServidor);
        }
    }
    
    private static String getFirstNonLoopbackAddress() throws SocketException {
    Enumeration en = NetworkInterface.getNetworkInterfaces();
    
    while (en.hasMoreElements()) {
        NetworkInterface i = (NetworkInterface) en.nextElement();
        for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements();) {
            InetAddress addr = (InetAddress) en2.nextElement();
            if (!addr.isLoopbackAddress()) {
                if (addr instanceof Inet4Address) {
                    return addr.toString().substring(1);
                    }
                }
            }
        }
        return null;
    }
    
    public void levantarServidor(){
        try {
            //JOptionPane.showMessageDialog(this, "Host inalcanzable. Iniciando servidor...");
            System.out.println("Host inalcanzable. Iniciando servidor...");
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
            } 
            catch (ClassNotFoundException e) {
                System.err.println("Unable to find and load MySQL driver");
                System.exit(1);
            } catch (InstantiationException ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println("Encendiendo Servidor...");
            ServidorChat servidorChat = new ServidorChat();
            servidorChat.start();

            ServidorChat.sleep(1000);

            System.setProperty("java.rmi.server.hostname", ipLocal);
            ServidorRMI servidorRMI = new ServidorRMI();
            servidorRMI.start();

            ServidorRMI.sleep(1000);

            System.out.println("Servidor encendido en: " + ipLocal);
            
         
            listaClientesConectados.remove(ipLocal);
            System.out.println("DELETED");
            this.showServerResources();
            
            servidor = true;
            
            this.connectionRMI();
            

            //registro = LocateRegistry.getRegistry(ipLocal, 1099);
            //listaNodo = (IRemoto) registro.lookup("Nodos");
                //Se obtiene el arreglo global de las IP's de los clientes
                //conectados.
                //Se actualiza la lista global de todos los clientes.
                //listaClientesConectados.remove(ipLocal);
                //listaClientesConectados = listaNodo.getListaIps();
                //listaNodo.setListaIps(listaClientesConectados);              
            
                            
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    
    //metodo usado por la clase ciente_nodo para refrescar la interfaz
    private boolean connectionRMI() {
        //Se utiliza REGEX para validar si es una IP o si es localhost.
        if (ipServidor.matches("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b") || 
                ipServidor.equals("localhost")) {
            try {
                registro = LocateRegistry.getRegistry(ipServidor, 1099);
                listaNodo = (IRemoto) registro.lookup("Nodos");       

                this.showChatResources();
                listaClientesConectados = listaNodo.getListaIps();
                if (servidor == false) {
                    listaClientesConectados.add(ipLocal);
                }
                listaNodo.setListaIps(listaClientesConectados);
                
                if (!refresh.isAlive() && !updateIpList.isAlive()){
                    this.refresh.start();
                    this.updateIpList.start();
                }
                System.out.println(servidor);
                
            } catch (NotBoundException ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                
                //this.disposeChatResources();
                //this.grandulon();
                
                return false;
            }
        }
        
        else {
            JOptionPane.showMessageDialog(this, "Introduzca una IP valida");
        }
        
        return true;
        
    }
    
    public void disposeChatResources() {
        Component[] com = this.jPanelChat.getComponents();  
        //Inside you action event where you want to disable everything  
        //Do the following  
        for (int a = 0; a < com.length; a++) {  
             com[a].setEnabled(false);  
        } 
        this.jTextAreaChat.setText("Servidor caido, restaurando chat...");
        
    }
    
    public void showChatResources() {
        Component[] com = this.jPanelChat.getComponents();  
        //Inside you action event where you want to disable everything  
        //Do the following  
        for (int a = 0; a < com.length; a++) {  
             com[a].setEnabled(true);  
        }
        this.jTextAreaChat.setText("");
    }
    
    public class UpdateIpList extends Thread {
       
        protected boolean loop = true;
        @Override
        public void run() {
            
            while(loop){
                try {
                    listaClientesConectados = listaNodo.getListaIps();
                    System.out.println("Lista de clientes actualizada...");
                    updateIpList.sleep(10000);
                } catch (RemoteException ex) {
                    grandulon();
//                    if (connectionRMI() == false){
//                        try {
//                            
//                            updateIpList.sleep(10000);
//                            System.out.println("WHILIEANDO");
//                            grandulon();
//                        } catch (InterruptedException ex1) {
//                            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex1);
//                        }
//                     
//                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    //metodo usado por la clase clientGUI para refrescar la interfaz
    public class RefreshStats extends Thread {

        String procesos = "";
        protected int loop = 1;

        @Override
        public void run() {

            while (loop == 1) {
                try {
                  
                    if (listaNodo.getListaNodos().get(nodoGlobal) != null) {
                        
                        mostrarInformacionNodo();
                        fillComboBoxNodos();

                    }
                        //Sleep 1 minuto.
                        refresh.sleep(1000 * 60);

                } catch (RemoteException ex) {
                    grandulon();
//                    if (connectionRMI() == false){
//                        try {
//                            //grandulon();
//                            refresh.sleep(10000);
//                            System.out.println("WHILIEANDO");
//                            grandulon();
//                        } catch (InterruptedException ex1) {
//                            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex1);
//                        }
//                     
//                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

        public void stop_loop() {
            loop = 0;
        }
    }

    
    public void disconnectRMI(String ip_nodo) throws RemoteException, NotBoundException {
         
        registro.unbind("Nodos");
        UnicastRemoteObject.unexportObject((Remote) listaNodo, true);

    }
    
    public void fillComboBoxNodos() {
        try {
            this.jComboBox1.removeAllItems();
            for (int i = 0; i < listaNodo.getListaNodos().size(); i++) {
                this.jComboBox1.addItem(listaNodo.getListaNodos().get(i).getIp());
            }
        } catch (RemoteException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void mostrarInformacionNodo() throws RemoteException {
        if (listaNodo.getListaNodos().get(nodoGlobal) != null) {
        
            this.jTextFieldCPUNodo.setText(listaNodo.getListaNodos().get(nodoGlobal).getCpu());
            this.jTextFieldRAMTotalNodo.setText(listaNodo.getListaNodos().get(nodoGlobal).getMemTotal());
            this.jTextFieldRAMUsadaNodo.setText(listaNodo.getListaNodos().get(nodoGlobal).getMemUsada());
            this.jTextFieldRAMLibreNodo.setText(listaNodo.getListaNodos().get(nodoGlobal).getMemLibre());
            
            ArrayList<Proceso> listaProc = listaNodo.getListaNodos().get(nodoGlobal).getListaProcesos();
               

                String procesos = "";
                for (int i = 0; i < listaProc.size() && i != 300; i++) {


                    procesos = procesos
                            + "(ID): " + listaProc.get(i).getPid()
                            + " --- CPU: " + listaProc.get(i).getCpu()
                            + " --- RAM: " + listaProc.get(i).getRam()
                            + " --- Estado: " + listaProc.get(i).getState()
                            + "\n";
                }
                
                this.jTextAreaProcesos.setText(procesos);
           
        }
    }
    
    /**
     * Add the text to show in the chat
     *
     * @param text Text to add
     */
    public void addText(String text) {
        jTextAreaChat.append(text);
        if (text.equals("kickUser")){
            try {
                this.socket.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void setSala(String sala){
        this.jLabelSala.setText(sala);
    }

    public void fillCombo(String user) {
        jComboBoxUsers.addItem(user);
    }

    /**
     * Return the text present in the textField and erase it
     *
     * @return TextField text
     */
    public String getTexto() {
        String texto = jTextFieldChat.getText();
        jTextFieldChat.setText("");
        return texto;
    }
    
    /*
     * Return the item focused on the comboBox
     */
    public String getSelectedUser() {
        return (String) jComboBoxUsers.getSelectedItem();
    }
    
    /*
     * Remove a user from the comboBox
     */
    public void removeUser(String nick){
        
        for (int i = 0; i < jComboBoxUsers.getItemCount(); i++) {
            if (jComboBoxUsers.getItemAt(i).equals(nick)){
                jComboBoxUsers.removeItemAt(i);
                break;
            }
        }
        
    }
    
    public void showServerResources() {
        servidor = true;
        this.jLabelServer.setVisible(true);
        this.jButtonAdminConsole.setVisible(true);
    }
    
    private void disposeServerResources() {
        servidor = false;
        this.jLabelServer.setVisible(false);
        this.jButtonAdminConsole.setVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelChat = new javax.swing.JPanel();
        jLabelSala = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxUsers = new javax.swing.JComboBox();
        jButtonDisconnectChat = new javax.swing.JButton();
        jTextFieldChat = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaChat = new javax.swing.JTextArea();
        jLabelNick = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListSalas = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();
        jButtonConnectChat = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButtonAdminConsole = new javax.swing.JButton();
        jPanelMonitor = new javax.swing.JPanel();
        jLabelServer = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        textfieldIpNodo = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldRAMLibreNodo = new javax.swing.JTextField();
        jTextFieldRAMUsadaNodo = new javax.swing.JTextField();
        jTextFieldRAMTotalNodo = new javax.swing.JTextField();
        jTextFieldCPUNodo = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextAreaProcesos = new javax.swing.JTextArea();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanelChat.setBorder(javax.swing.BorderFactory.createTitledBorder("Chat"));

        jLabelSala.setText("Sala");

        jLabel1.setText("Usuarios conectados:");

        jComboBoxUsers.setModel(new javax.swing.DefaultComboBoxModel(new String[] { }));
        jComboBoxUsers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxUsersActionPerformed(evt);
            }
        });

        jButtonDisconnectChat.setText("Disconnect");
        jButtonDisconnectChat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDisconnectChatActionPerformed(evt);
            }
        });

        jTextAreaChat.setColumns(20);
        jTextAreaChat.setRows(5);
        jScrollPane1.setViewportView(jTextAreaChat);

        jLabelNick.setText("jLabel2");

        jListSalas.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Administradores SO", "Administradores BD", "Personal monitoreo" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jListSalas);

        jLabel6.setText("Salas:");

        jButtonConnectChat.setText("Connect");
        jButtonConnectChat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConnectChatActionPerformed(evt);
            }
        });

        jButton1.setText("Send");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButtonAdminConsole.setText("Admin console");
        jButtonAdminConsole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAdminConsoleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelChatLayout = new javax.swing.GroupLayout(jPanelChat);
        jPanelChat.setLayout(jPanelChatLayout);
        jPanelChatLayout.setHorizontalGroup(
            jPanelChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelChatLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelChatLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(jPanelChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelChatLayout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanelChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelChatLayout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelChatLayout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addGroup(jPanelChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(jPanelChatLayout.createSequentialGroup()
                                                .addComponent(jButtonConnectChat)
                                                .addGap(18, 18, 18)
                                                .addComponent(jButtonDisconnectChat))
                                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(jPanelChatLayout.createSequentialGroup()
                                .addComponent(jTextFieldChat, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(jPanelChatLayout.createSequentialGroup()
                        .addGroup(jPanelChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelChatLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBoxUsers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanelChatLayout.createSequentialGroup()
                                .addComponent(jLabelSala)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabelNick)
                                .addGap(157, 157, 157)))
                        .addComponent(jButtonAdminConsole)))
                .addContainerGap())
        );
        jPanelChatLayout.setVerticalGroup(
            jPanelChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelChatLayout.createSequentialGroup()
                .addGroup(jPanelChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelChatLayout.createSequentialGroup()
                        .addGroup(jPanelChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelSala)
                            .addComponent(jLabelNick))
                        .addGroup(jPanelChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelChatLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel1)
                                    .addComponent(jComboBoxUsers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanelChatLayout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(jLabel6))))
                    .addComponent(jButtonAdminConsole))
                .addGap(18, 18, 18)
                .addGroup(jPanelChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelChatLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonConnectChat)
                            .addComponent(jButtonDisconnectChat))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldChat, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(0, 18, Short.MAX_VALUE))
        );

        jPanelMonitor.setBorder(javax.swing.BorderFactory.createTitledBorder("Informacion nodos"));

        jLabelServer.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        jLabelServer.setForeground(java.awt.Color.darkGray);
        jLabelServer.setText("YOU ARE THE SERVER.");
        jLabelServer.setVisible(false);

        jLabel2.setText("Ingrese la direccion IP del nodo:");

        jButton2.setText("Agregar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton4.setText("Resfrescar");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox1ItemStateChanged(evt);
            }
        });
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel7.setText("Nodos agregados:");

        jLabel3.setText("CPU (%):");

        jLabel4.setText("RAM Total (%):");

        jLabel5.setText("RAM Usada (%):");

        jLabel8.setText("RAM Disponible (%):");

        jTextFieldRAMLibreNodo.setEditable(false);

        jTextFieldRAMUsadaNodo.setEditable(false);

        jTextFieldRAMTotalNodo.setEditable(false);

        jTextFieldCPUNodo.setEditable(false);

        jTextAreaProcesos.setEditable(false);
        jTextAreaProcesos.setColumns(20);
        jTextAreaProcesos.setRows(5);
        jScrollPane3.setViewportView(jTextAreaProcesos);

        jButton3.setText("Imprimir ips");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelMonitorLayout = new javax.swing.GroupLayout(jPanelMonitor);
        jPanelMonitor.setLayout(jPanelMonitorLayout);
        jPanelMonitorLayout.setHorizontalGroup(
            jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMonitorLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addGap(237, 237, 237))
            .addGroup(jPanelMonitorLayout.createSequentialGroup()
                .addGroup(jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelMonitorLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelMonitorLayout.createSequentialGroup()
                                .addGroup(jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanelMonitorLayout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addGap(29, 29, 29)
                                        .addComponent(jTextFieldRAMLibreNodo, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel2)
                                    .addGroup(jPanelMonitorLayout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextFieldRAMUsadaNodo, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanelMonitorLayout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextFieldRAMTotalNodo, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jTextFieldCPUNodo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelMonitorLayout.createSequentialGroup()
                                .addGroup(jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelMonitorLayout.createSequentialGroup()
                        .addGap(263, 263, 263)
                        .addGroup(jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabelServer)
                            .addGroup(jPanelMonitorLayout.createSequentialGroup()
                                .addComponent(textfieldIpNodo, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton2)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelMonitorLayout.setVerticalGroup(
            jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMonitorLayout.createSequentialGroup()
                .addComponent(jLabelServer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(textfieldIpNodo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addGroup(jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelMonitorLayout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelMonitorLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jTextFieldCPUNodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jTextFieldRAMUsadaNodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(jTextFieldRAMLibreNodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jTextFieldRAMTotalNodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(16, 16, 16)
                .addGroup(jPanelMonitorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(jButton3))
                .addGap(13, 13, 13))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelChat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelMonitor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelMonitor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelChat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonDisconnectChatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDisconnectChatActionPerformed
        ControlClient.removeUser();
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.jButton1.setEnabled(false);
        this.jComboBoxUsers.setEnabled(false);
        this.jButtonConnectChat.setEnabled(true);
        this.jTextFieldChat.setEnabled(false);
        this.jTextFieldChat.setEnabled(false);
        this.jButtonDisconnectChat.setEnabled(false);
        this.jComboBoxUsers.removeAllItems();
        this.jComboBoxUsers.addItem("Todos");
        this.jListSalas.setVisible(true);
    }//GEN-LAST:event_jButtonDisconnectChatActionPerformed

    private void jButtonConnectChatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConnectChatActionPerformed
    
            if (jListSalas.isSelectionEmpty()){
                JOptionPane.showMessageDialog(this, "Debes seleccionar una sala.");
            }
            else {
                sala = jListSalas.getSelectedValue().toString();
                
                if (sala.equals("Administradores SO")){
                    PORT = 5000;
                }
                else if (sala.equals("Administradores BD")){
                    PORT = 5001;
                }
                else if (sala.equals("Personal Monitoreo")){
                    PORT = 5002;
                }
            
                nick = JOptionPane.showInputDialog("Introduzca un nickname:");
                
                client = new ClientChat(PORT, ipServidor, nick, sala, this);
                socket = client.getSocket();          
                
                this.jLabelNick.setText(nick);
                jComboBoxUsers.setEnabled(true);
                jLabelNick.setEnabled(true);
                jLabelSala.setEnabled(true);
                jTextAreaChat.setEnabled(true);
                jTextFieldChat.setEnabled(true);
                jButton1.setEnabled(true);
                this.jButtonConnectChat.setEnabled(false);
                this.jButtonDisconnectChat.setEnabled(true);
                this.jListSalas.setVisible(false);
                
            }
                
       
    }//GEN-LAST:event_jButtonConnectChatActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        //Se utiliza REGEX para validar si es una IP o si es localhost.
        if (textfieldIpNodo.getText().matches("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b") || textfieldIpNodo.getText().equals("localhost")) {
            try {
                listaNodo.shellCommandExecute("/home/xubuntu/shellScripts/instalador.sh  " + this.textfieldIpNodo.getText());
                JOptionPane.showMessageDialog(this, "Nodo agregado. Se reflejara en pantalla "
                    + "en 5 minutos.");

                this.textfieldIpNodo.setText(null);

                this.fillComboBoxNodos();

            } catch (RemoteException ex) {
                this.levantarServidor();
                this.connectionRMI();
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else {
            JOptionPane.showMessageDialog(this, "Introduzca una direccion IP valida.");
            this.textfieldIpNodo.setText("");
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        try {
            this.mostrarInformacionNodo();
        } catch (RemoteException ex) {
            
            //this.grandulon();
            //this.levantarServidor();
            //this.connectionRMI();
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged

    }//GEN-LAST:event_jComboBox1ItemStateChanged

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        if(this.jComboBox1.getSelectedIndex() >= 0){
            nodoGlobal = this.jComboBox1.getSelectedIndex();

            try {
                mostrarInformacionNodo();
            } catch (RemoteException ex) {
                this.levantarServidor();
                this.connectionRMI();
                //Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ControlClient.actionInterface();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jComboBoxUsersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxUsersActionPerformed
         if((this.jComboBoxUsers.getSelectedIndex() >= 0) && (servidor == true)){
             this.jButtonAdminConsole.setVisible(true);
         }
    }//GEN-LAST:event_jComboBoxUsersActionPerformed

    private void jButtonAdminConsoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAdminConsoleActionPerformed
        new AdminConsole(this,true).setVisible(true);
    }//GEN-LAST:event_jButtonAdminConsoleActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        for(int i = 0;i<listaClientesConectados.size();i++){
            System.out.println("Lista de Ip: Ip "+i+" "+listaClientesConectados.get(i));
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("GTK+".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
//                try {
//                    nick = InetAddress.getLocalHost().getHostName();
//                } catch (UnknownHostException ex) {
//                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
//                }
                ipServidor = JOptionPane.showInputDialog("Introduzca IP del servidor:", "localhost");
                ipServidorActual = ipServidor;
                new ClientGUI().setVisible(true);
                
                
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButtonAdminConsole;
    private javax.swing.JButton jButtonConnectChat;
    private javax.swing.JButton jButtonDisconnectChat;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBoxUsers;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelNick;
    private javax.swing.JLabel jLabelSala;
    private javax.swing.JLabel jLabelServer;
    private javax.swing.JList jListSalas;
    private javax.swing.JPanel jPanelChat;
    private javax.swing.JPanel jPanelMonitor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea jTextAreaChat;
    private javax.swing.JTextArea jTextAreaProcesos;
    private javax.swing.JTextField jTextFieldCPUNodo;
    private javax.swing.JTextField jTextFieldChat;
    private javax.swing.JTextField jTextFieldRAMLibreNodo;
    private javax.swing.JTextField jTextFieldRAMTotalNodo;
    private javax.swing.JTextField jTextFieldRAMUsadaNodo;
    private javax.swing.JTextField textfieldIpNodo;
    // End of variables declaration//GEN-END:variables
}
