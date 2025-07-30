package com.juank.utp.finimpact.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo que representa una iniciativa empresarial
 */
public class Iniciativa {
    private int idIniciativa;
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String tipo;
    private String estado; // planeado, en curso, finalizado, cancelado
    private String riesgo; // alto, medio, bajo
    private int idOwner;
    private LocalDateTime fechaRegistro;

    // Constructor vacío
    public Iniciativa() {}

    // Constructor completo
    public Iniciativa(int idIniciativa, String nombre, String descripcion, LocalDate fechaInicio,
                     LocalDate fechaFin, String tipo, String estado, String riesgo, int idOwner,
                     LocalDateTime fechaRegistro) {
        this.idIniciativa = idIniciativa;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipo = tipo;
        this.estado = estado;
        this.riesgo = riesgo;
        this.idOwner = idOwner;
        this.fechaRegistro = fechaRegistro;
    }

    // Constructor sin ID (para inserción)
    public Iniciativa(String nombre, String descripcion, LocalDate fechaInicio, LocalDate fechaFin,
                     String tipo, String estado, String riesgo, int idOwner) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipo = tipo;
        this.estado = estado;
        this.riesgo = riesgo;
        this.idOwner = idOwner;
    }

    // Getters y Setters
    public int getIdIniciativa() {
        return idIniciativa;
    }

    public void setIdIniciativa(int idIniciativa) {
        this.idIniciativa = idIniciativa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRiesgo() {
        return riesgo;
    }

    public void setRiesgo(String riesgo) {
        this.riesgo = riesgo;
    }

    public int getIdOwner() {
        return idOwner;
    }

    public void setIdOwner(int idOwner) {
        this.idOwner = idOwner;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return "Iniciativa{" +
                "idIniciativa=" + idIniciativa +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", tipo='" + tipo + '\'' +
                ", estado='" + estado + '\'' +
                ", riesgo='" + riesgo + '\'' +
                ", idOwner=" + idOwner +
                ", fechaRegistro=" + fechaRegistro +
                '}';
    }
}
