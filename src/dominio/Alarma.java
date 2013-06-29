/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dominio;

/**
 *
 * @author xubuntu
 */
public class Alarma {
    
    private Double cpuMaximaPermitida;
    private Double ramMaximaPermitida;

    public Alarma() {
        this.cpuMaximaPermitida = null;
        this.ramMaximaPermitida = null;
    }

    public Double getCpuMaximaPermitida() {
        return cpuMaximaPermitida;
    }

    public void setCpuMaximaPermitida(Double cpuMaximaPermitida) {
        this.cpuMaximaPermitida = cpuMaximaPermitida;
    }

    public Double getRamMaximaPermitida() {
        return ramMaximaPermitida;
    }

    public void setRamMaximaPermitida(Double ramMaximaPermitida) {
        this.ramMaximaPermitida = ramMaximaPermitida;
    }

}
