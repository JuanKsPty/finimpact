package com.juank.utp.finimpact.utils;

import com.juank.utp.finimpact.model.Usuario;

/**
 * Clase para manejar la sesión del usuario actual
 */
public class UserSession {
    private static Usuario usuarioActual;

    /**
     * Establece el usuario actual de la sesión
     */
    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    /**
     * Obtiene el usuario actual de la sesión
     */
    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Verifica si hay un usuario logueado
     */
    public static boolean hayUsuarioLogueado() {
        return usuarioActual != null;
    }

    /**
     * Verifica si el usuario actual es analista
     */
    public static boolean esAnalista() {
        return usuarioActual != null && "analista".equals(usuarioActual.getRol());
    }

    /**
     * Verifica si el usuario actual es admin
     */
    public static boolean esAdmin() {
        return usuarioActual != null && "admin".equals(usuarioActual.getRol());
    }

    /**
     * Limpia la sesión del usuario
     */
    public static void limpiarSesion() {
        usuarioActual = null;
    }
}
