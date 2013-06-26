/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

//import Chat.ClienteGUI;
import client.ClientChat;
import dominio.IRemoto;
import dominio.Proceso;
import server.ServidorChat;
import server.ServidorRMI;
import java.awt.Component;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

/**
 *
 * @author oswaldomaestra
 */
public class InterfazUsuario extends javax.swing.JFrame {

    Registry registro = null;
    DefaultListModel listModel = null;
    IRemoto listaNodo = null;
    int nodoGlobal = 0;
    RefreshStats refresh;
    //ClienteGUI panelChat = null;
    private static int PORT;
    private String ipServidor;
    
    /**
     * Creates new form InterfazUsuario
     */
    
    public InterfazUsuario() {
        initComponents();
                
        //iniciamos el panel del chat en el puerto 1500
        //panelChat = new ClienteGUI("localhost", 1500);
        //jPanelChat.add(panelChat);
        //salasGUI = new MenuGUI("nick");
        //this.jPanelChat.add(salasGUI);
        
    }
    
    public void levantarServidor(String ip_nodo){
        try {
            JOptionPane.showMessageDialog(this, "Host inalcanzable. Iniciando servidor...");
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
            } 
            catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                System.err.println("Unable to find and load MySQL driver");
                System.exit(1);
            }

            System.out.println("Encendiendo Servidor...");
            ServidorChat servidorChat = new ServidorChat();
            servidorChat.start();

            ServidorChat.sleep(1500);

            ServidorRMI servidorRMI = new ServidorRMI();
            servidorRMI.start();

            ServidorRMI.sleep(1500);

            System.out.println("Servidor encendido en: " + ipServidor);

            registro = LocateRegistry.getRegistry(ip_nodo, 1099);
            listaNodo = (IRemoto) registro.lookup("Nodos");
            
            this.jLabelServer.setVisible(true);
                    
        } catch (RemoteException ex) {
            Logger.getLogger(InterfazUsuario.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            Logger.getLogger(InterfazUsuario.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(InterfazUsuario.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    
    //metodo usado por la clase ciente_nodo para refrescar la interfaz
    public String connectionRMI(String ip_nodo) {
        try {
            ipServidor = ip_nodo;
            registro = LocateRegistry.getRegistry(ip_nodo, 1099);
            listaNodo = (IRemoto) registro.lookup("Nodos");

        } catch (NotBoundException ex) {
            Logger.getLogger(InterfazUsuario.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            
            this.levantarServidor(ip_nodo);
            
        }
        
        //Se utiliza REGEX para validar si es una IP o si es localhost.
        if (ip_nodo.matches("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b") || ip_nodo.equals("localhost"))
            return ip_nodo;
        
        return "";
    }
    
    //metodo usado por la clase ciente_nodo para refrescar la interfaz
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
                        //Sleep 5 minutos.
                        RefreshStats.sleep(5000 * 60);

                } catch (RemoteException | InterruptedException ex) {
                    Logger.getLogger(InterfazUsuario.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(InterfazUsuario.class.getName()).log(Level.SEVERE, null, ex);
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
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        textfieldIpNodo = new javax.swing.JTextField();
        jButtonConnect = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldRAMLibreNodo = new javax.swing.JTextField();
        jTextFieldRAMUsadaNodo = new javax.swing.JTextField();
        jTextFieldCPUNodo = new javax.swing.JTextField();
        jTextFieldRAMTotalNodo = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaProcesos = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jPanelChat = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListSalas = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldNick = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jButtonChatConnect = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabelServer = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Ingrese la direccion IP del nodo:");

        jButton1.setText("Agregar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButtonConnect.setText("Connect");
        jButtonConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConnectActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), "Informacion Nodo"));

        jLabel2.setText("CPU (%):");

        jLabel3.setText("RAM Total (%):");

        jLabel4.setText("RAM Usada (%):");

        jLabel5.setText("RAM Disponible (%):");

        jTextFieldRAMLibreNodo.setEditable(false);

        jTextFieldRAMUsadaNodo.setEditable(false);

        jTextFieldCPUNodo.setEditable(false);

        jTextFieldRAMTotalNodo.setEditable(false);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Procesos"));

        jTextAreaProcesos.setEditable(false);
        jTextAreaProcesos.setColumns(20);
        jTextAreaProcesos.setRows(5);
        jScrollPane2.setViewportView(jTextAreaProcesos);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2)
                .add(27, 27, 27))
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel5)
                    .add(jLabel4)
                    .add(jLabel2)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jTextFieldRAMTotalNodo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                    .add(jTextFieldRAMUsadaNodo)
                    .add(jTextFieldCPUNodo)
                    .add(jTextFieldRAMLibreNodo))
                .add(18, 18, 18)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jTextFieldCPUNodo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jTextFieldRAMTotalNodo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jTextFieldRAMUsadaNodo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(jTextFieldRAMLibreNodo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(57, Short.MAX_VALUE))
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel7.setText("Nodos agregados:");

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

        jPanelChat.setBorder(javax.swing.BorderFactory.createTitledBorder("Chat administrativo"));
        jPanelChat.setPreferredSize(new java.awt.Dimension(10, 250));

        jListSalas.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Administradores SO", "Administradores BD", "Personal monitoreo" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jListSalas);

        jLabel6.setText("Salas:");

        jLabel8.setText("nickname");

        jButtonChatConnect.setText("Connect to Chat");
        jButtonChatConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChatConnectActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanelChatLayout = new org.jdesktop.layout.GroupLayout(jPanelChat);
        jPanelChat.setLayout(jPanelChatLayout);
        jPanelChatLayout.setHorizontalGroup(
            jPanelChatLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelChatLayout.createSequentialGroup()
                .add(jPanelChatLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanelChatLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanelChatLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanelChatLayout.createSequentialGroup()
                                .add(jLabel8)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jTextFieldNick, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 171, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanelChatLayout.createSequentialGroup()
                                .add(jLabel6)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(jPanelChatLayout.createSequentialGroup()
                        .add(202, 202, 202)
                        .add(jButtonChatConnect)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanelChatLayout.setVerticalGroup(
            jPanelChatLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelChatLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelChatLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldNick, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8))
                .add(18, 18, 18)
                .add(jPanelChatLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel6)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jButtonChatConnect)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton2.setText("Resfrescar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabelServer.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        jLabelServer.setForeground(java.awt.Color.darkGray);
        jLabelServer.setText("YOU ARE THE SERVER.");
        jLabelServer.setVisible(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel7)
                                        .add(18, 18, 18)
                                        .add(jComboBox1, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel1)
                                        .add(18, 18, 18)
                                        .add(textfieldIpNodo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 201, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jButton1)
                                    .add(jButton2))
                                .add(0, 0, Short.MAX_VALUE))
                            .add(jPanelChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabelServer)
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(240, 240, 240)
                .add(jButtonConnect)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabelServer)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(textfieldIpNodo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton1))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButton2))
                .add(18, 18, 18)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanelChat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 256, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonConnect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 33, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(29, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConnectActionPerformed
 
        ipServidor = connectionRMI(JOptionPane.showInputDialog(this, "Ingrese IP del Servidor"));
   
        if (!"".equals(ipServidor)) {
            this.jButton1.setEnabled(true);
            this.textfieldIpNodo.setEnabled(true);
            this.jButtonConnect.setText("Conectado a '" + ipServidor + "'");
            this.jButtonConnect.setEnabled(false);
            this.jComboBox1.setEnabled(true);
            this.jTextAreaProcesos.setVisible(true);
            
            Component[] com2 = this.jPanel2.getComponents();
            for (int a = 0; a < com2.length; a++) {  
                com2[a].setVisible(true); 
            }
            //Habilita todos los componentes del Panel seleccionado.
            Component[] com = this.jPanel1.getComponents();
            for (int a = 0; a < com.length; a++) {  
                com[a].setEnabled(true); 
            }
            
            Component[] com3 = this.jPanelChat.getComponents();
            for (int a = 0; a < com3.length; a++) {  
                com3[a].setVisible(true); 
            }
            
            this.jButtonChatConnect.setVisible(true);
            
            refresh = new RefreshStats();
            refresh.start();
            
        }
    }//GEN-LAST:event_jButtonConnectActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //Se utiliza REGEX para validar si es una IP o si es localhost.
        if (textfieldIpNodo.getText().matches("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b") || textfieldIpNodo.getText().equals("localhost")) {
            try {
                listaNodo.shellCommandExecute("/home/xubuntu/shellScripts/instalador.sh  " + this.textfieldIpNodo.getText());
                JOptionPane.showMessageDialog(this, "Nodo agregado. Se reflejara en pantalla "
                        + "en 5 minutos.");            

                this.textfieldIpNodo.setText(null);

                this.fillComboBoxNodos();

            } catch (RemoteException ex) {
                this.levantarServidor(ipServidor);
                this.connectionRMI(ipServidor);
                Logger.getLogger(InterfazUsuario.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else {
            JOptionPane.showMessageDialog(this, "Introduzca una direccion IP valida.");
            this.textfieldIpNodo.setText("");
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        if(this.jComboBox1.getSelectedIndex() >= 0){
            nodoGlobal = this.jComboBox1.getSelectedIndex();

            try {
                mostrarInformacionNodo();
            } catch (RemoteException ex) {
                this.levantarServidor(ipServidor);
                this.connectionRMI(ipServidor);
                //Logger.getLogger(InterfazUsuario.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
        
    }//GEN-LAST:event_jComboBox1ItemStateChanged

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            this.mostrarInformacionNodo();
        } catch (RemoteException ex) {
            this.levantarServidor(ipServidor);
            this.connectionRMI(ipServidor);
            //Logger.getLogger(InterfazUsuario.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButtonChatConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChatConnectActionPerformed
        Component[] com3 = this.jPanelChat.getComponents();

        for (int a = 0; a < com3.length; a++) {
            com3[a].setVisible(true);
        }

        if (jListSalas.isSelectionEmpty()){
            JOptionPane.showMessageDialog(this, "Debes seleccionar una sala.");
        }
        else {

            if (jListSalas.getSelectedValue().equals("Administradores SO")){
                PORT = 5000;
            }
            else if (jListSalas.getSelectedValue().equals("Administradores BD")){
                PORT = 5001;
            }
            else if (jListSalas.getSelectedValue().equals("Personal Monitoreo")){
                PORT = 5002;
            }

            //ClientChat clientChat = new ClientChat(PORT, ipServidor, this.jTextFieldNick.getText(), jListSalas.getSelectedValue().toString());

            //this.jTextFieldNick.setEditable(false);
            //this.jButtonChatConnect.setVisible(false);
        }

    }//GEN-LAST:event_jButtonChatConnectActionPerformed

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
                if ("MacOS".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(InterfazUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(InterfazUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(InterfazUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(InterfazUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
            
                InterfazUsuario iUsuario = new InterfazUsuario();
                iUsuario.setVisible(true);
                iUsuario.setLocationRelativeTo(null);
                iUsuario.jButton1.setEnabled(false);
                iUsuario.textfieldIpNodo.setEnabled(false);
                iUsuario.jComboBox1.setEnabled(false);
                
                Component[] com = iUsuario.jPanel1.getComponents();
                Component[] com2 = iUsuario.jPanel2.getComponents();
                Component[] com3 = iUsuario.jPanelChat.getComponents();
 
                //Disable all jPanel1 components.
                for (int a = 0; a < com2.length; a++) {
                    com2[a].setVisible(false);
                }
                //Disable al jPanel2 components.
                for (int a = 0; a < com.length; a++) {  
                    com[a].setEnabled(false);
                }
                //Disable al jPanel2 components.
                for (int a = 0; a < com3.length; a++) {  
                    com3[a].setVisible(false);
                }
         
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButtonChatConnect;
    private javax.swing.JButton jButtonConnect;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelServer;
    private javax.swing.JList jListSalas;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelChat;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextAreaProcesos;
    private javax.swing.JTextField jTextFieldCPUNodo;
    private javax.swing.JTextField jTextFieldNick;
    private javax.swing.JTextField jTextFieldRAMLibreNodo;
    private javax.swing.JTextField jTextFieldRAMTotalNodo;
    private javax.swing.JTextField jTextFieldRAMUsadaNodo;
    private javax.swing.JTextField textfieldIpNodo;
    // End of variables declaration//GEN-END:variables
}
