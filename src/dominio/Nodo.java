package dominio;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author oswaldomaestra
 */
public class Nodo implements Serializable{

    private String id = "";
    private String ip = "";
    private String cpu = "";
    private String memTotal = "";
    private String memUsada = "";
    private String memLibre = "";
    private String fileSystem = "";
    private ArrayList<Proceso> listaProcesos = null;
    private Alarma alarma;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMemTotal() {
        return memTotal;
    }

    public void setMemTotal(String memTotal) {
        this.memTotal = memTotal;
    }

    public String getMemUsada() {
        return memUsada;
    }

    public void setMemUsada(String memUsada) {
        this.memUsada = memUsada;
    }

    public String getMemLibre() {
        return memLibre;
    }

    public void setMemLibre(String memLibre) {
        this.memLibre = memLibre;
    }

    public String getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(String fileSystem) {
        this.fileSystem = fileSystem;
    }

    public ArrayList<Proceso> getListaProcesos() {
        return listaProcesos;
    }

    public void setListaProcesos(ArrayList<Proceso> listaProcesos) {
        this.listaProcesos = listaProcesos;
    }

    public Alarma getAlarma() {
        return alarma;
    }

    public void setAlarma(Alarma alarma) {
        this.alarma = alarma;
    }
    
}
