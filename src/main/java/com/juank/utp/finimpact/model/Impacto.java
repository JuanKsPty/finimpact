package com.juank.utp.finimpact.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo que representa un impacto financiero de una iniciativa
 */
public class Impacto {
    private int idImpacto;
    private int idIniciativa;
    private LocalDate fechaCreacion;
    private String tipoImpacto; // Maquinaria, Generación, Optimización, Transformación
    private int multiplicador; // 1 (positivo) o -1 (negativo)
    private String atributoImpacto; // Planeado, Estimado, Real
    private LocalDate fechaImpacto;
    private BigDecimal impacto;

    // Constructor vacío
    public Impacto() {}

    // Constructor completo
    public Impacto(int idImpacto, int idIniciativa, LocalDate fechaCreacion, String tipoImpacto,
                  int multiplicador, String atributoImpacto, LocalDate fechaImpacto, BigDecimal impacto) {
        this.idImpacto = idImpacto;
        this.idIniciativa = idIniciativa;
        this.fechaCreacion = fechaCreacion;
        this.tipoImpacto = tipoImpacto;
        this.multiplicador = multiplicador;
        this.atributoImpacto = atributoImpacto;
        this.fechaImpacto = fechaImpacto;
        this.impacto = impacto;
    }

    // Constructor sin ID (para inserción)
    public Impacto(int idIniciativa, LocalDate fechaCreacion, String tipoImpacto, int multiplicador,
                  String atributoImpacto, LocalDate fechaImpacto, BigDecimal impacto) {
        this.idIniciativa = idIniciativa;
        this.fechaCreacion = fechaCreacion;
        this.tipoImpacto = tipoImpacto;
        this.multiplicador = multiplicador;
        this.atributoImpacto = atributoImpacto;
        this.fechaImpacto = fechaImpacto;
        this.impacto = impacto;
    }

    // Getters y Setters
    public int getIdImpacto() {
        return idImpacto;
    }

    public void setIdImpacto(int idImpacto) {
        this.idImpacto = idImpacto;
    }

    public int getIdIniciativa() {
        return idIniciativa;
    }

    public void setIdIniciativa(int idIniciativa) {
        this.idIniciativa = idIniciativa;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getTipoImpacto() {
        return tipoImpacto;
    }

    public void setTipoImpacto(String tipoImpacto) {
        this.tipoImpacto = tipoImpacto;
    }

    public int getMultiplicador() {
        return multiplicador;
    }

    public void setMultiplicador(int multiplicador) {
        this.multiplicador = multiplicador;
    }

    public String getAtributoImpacto() {
        return atributoImpacto;
    }

    public void setAtributoImpacto(String atributoImpacto) {
        this.atributoImpacto = atributoImpacto;
    }

    public LocalDate getFechaImpacto() {
        return fechaImpacto;
    }

    public void setFechaImpacto(LocalDate fechaImpacto) {
        this.fechaImpacto = fechaImpacto;
    }

    public BigDecimal getImpacto() {
        return impacto;
    }

    public void setImpacto(BigDecimal impacto) {
        this.impacto = impacto;
    }

    /**
     * Calcula el impacto real aplicando el multiplicador
     */
    public BigDecimal getImpactoCalculado() {
        if (impacto == null) return BigDecimal.ZERO;
        return impacto.multiply(BigDecimal.valueOf(multiplicador));
    }

    @Override
    public String toString() {
        return "Impacto{" +
                "idImpacto=" + idImpacto +
                ", idIniciativa=" + idIniciativa +
                ", fechaCreacion=" + fechaCreacion +
                ", tipoImpacto='" + tipoImpacto + '\'' +
                ", multiplicador=" + multiplicador +
                ", atributoImpacto='" + atributoImpacto + '\'' +
                ", fechaImpacto=" + fechaImpacto +
                ", impacto=" + impacto +
                '}';
    }
}
