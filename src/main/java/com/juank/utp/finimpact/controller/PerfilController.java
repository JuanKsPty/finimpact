package com.juank.utp.finimpact.controller;

import com.juank.utp.finimpact.model.Usuario;
import com.juank.utp.finimpact.repository.UsuarioRepository;
import com.juank.utp.finimpact.utils.PasswordUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controlador para la vista de perfil del usuario
 */
public class PerfilController {

    // Componentes de información personal
    @FXML private Label lblNombreCompleto;
    @FXML private Label lblUsuario;
    @FXML private Label lblEmail;
    @FXML private Label lblTipo;

    // Componentes de cambio de contraseña
    @FXML private PasswordField txtPasswordActual;
    @FXML private PasswordField txtPasswordNueva;
    @FXML private PasswordField txtPasswordConfirmar;

    private Usuario usuario;
    private UsuarioRepository usuarioRepository;

    public void initialize() {
        usuarioRepository = new UsuarioRepository();
    }

    /**
     * Establece el usuario cuyo perfil se va a mostrar
     */
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        cargarDatosUsuario();
    }

    /**
     * Carga los datos del usuario en la interfaz
     */
    private void cargarDatosUsuario() {
        if (usuario != null) {
            lblNombreCompleto.setText(usuario.getNombreCompleto());
            lblUsuario.setText(usuario.getEmail()); // Usar email como nombre de usuario
            lblEmail.setText(usuario.getEmail());
            lblTipo.setText(getTipoDisplayName(usuario.getRol())); // Usar getRol() en lugar de getTipo()
        }
    }

    /**
     * Maneja el evento de cambiar contraseña
     */
    @FXML
    private void cambiarPassword() {
        if (usuario == null) {
            mostrarError("Error", "No se ha cargado la información del usuario.");
            return;
        }

        // Validaciones
        String passwordActual = txtPasswordActual.getText().trim();
        String passwordNueva = txtPasswordNueva.getText().trim();
        String passwordConfirmar = txtPasswordConfirmar.getText().trim();

        if (passwordActual.isEmpty()) {
            mostrarError("Error", "Debe ingresar su contraseña actual.");
            return;
        }

        if (passwordNueva.isEmpty()) {
            mostrarError("Error", "Debe ingresar la nueva contraseña.");
            return;
        }

        if (passwordNueva.length() < 6) {
            mostrarError("Error", "La nueva contraseña debe tener al menos 6 caracteres.");
            return;
        }

        if (!passwordNueva.equals(passwordConfirmar)) {
            mostrarError("Error", "La confirmación de contraseña no coincide.");
            return;
        }

        // Verificar contraseña actual
        if (!PasswordUtils.verifyPassword(passwordActual, usuario.getPassword())) {
            mostrarError("Error", "La contraseña actual es incorrecta.");
            return;
        }

        // Cambiar contraseña
        try {
            // No hashear aquí, se hace en el repositorio
            usuario.setPassword(passwordNueva);
            usuarioRepository.update(usuario);

            mostrarInformacion("Éxito", "Contraseña cambiada correctamente.");
            limpiarCampos();

        } catch (Exception e) {
            mostrarError("Error", "Error al cambiar la contraseña: " + e.getMessage());
        }
    }

    /**
     * Limpia los campos de contraseña
     */
    @FXML
    private void limpiarCampos() {
        txtPasswordActual.clear();
        txtPasswordNueva.clear();
        txtPasswordConfirmar.clear();
    }

    /**
     * Convierte el rol técnico a un nombre más amigable
     */
    private String getTipoDisplayName(String rol) {
        switch (rol.toLowerCase()) {
            case "admin":
                return "Administrador";
            case "analista":
                return "Analista";
            case "viewer":
                return "Visualizador";
            default:
                return rol;
        }
    }

    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de información
     */
    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
