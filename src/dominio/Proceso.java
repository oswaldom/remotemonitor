package dominio;

import java.io.Serializable;

/**
 *
 * @author oswaldomaestra
 */
public class Proceso implements Serializable{

    private String id = "";
    private String pid = "";
    private String cpu = "";
    private String ram = "";
    private String state = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    
    
}
