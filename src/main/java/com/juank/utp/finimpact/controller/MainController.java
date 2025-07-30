package com.juank.utp.finimpact.controller;

import com.juank.utp.finimpact.model.Usuario;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controlador para la vista principal de la aplicación
 */
public class MainController {

    @FXML
    private Button btnLogin;

    @FXML
    private Label lblStatus;

    private Usuario usuarioLogueado;

    @FXML
    private void initialize() {
        actualizarEstadoUsuario();
    }

    /**
     * Maneja el evento del botón de login
     */
    @FXML
    private void handleLogin() {
        if (usuarioLogueado == null) {
            // Mostrar ventana de login
            mostrarVentanaLogin();
        } else {
            // Realizar logout
            realizarLogout();
        }
    }

    /**
     * Muestra la ventana de login como modal
     */
    private void mostrarVentanaLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/juank/utp/finimpact/login-view.fxml"));
            Scene scene = new Scene(loader.load());

            Stage loginStage = new Stage();
            loginStage.setTitle("Iniciar Sesión - FinImpact");
            loginStage.setScene(scene);
            loginStage.setResizable(false);
            loginStage.initModality(Modality.APPLICATION_MODAL);

            // Obtener el controlador de login y pasarle referencia a este controlador
            LoginController loginController = loader.getController();
            loginController.setMainController(this);

            // Mostrar la ventana y esperar
            loginStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error al cargar la vista de login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Realiza el logout del usuario
     */
    private void realizarLogout() {
        usuarioLogueado = null;
        actualizarEstadoUsuario();
        System.out.println("🔓 Usuario deslogueado exitosamente");
    }

    /**
     * Establece el usuario logueado (llamado desde LoginController)
     */
    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        actualizarEstadoUsuario();
        System.out.println("✅ Usuario logueado: " + usuario.getNombreCompleto() + " (" + usuario.getRol() + ")");
    }

    /**
     * Actualiza la interfaz según el estado del usuario
     */
    private void actualizarEstadoUsuario() {
        if (usuarioLogueado == null) {
            btnLogin.setText("Login");
            lblStatus.setText("No hay usuario autenticado");
            btnLogin.setStyle("-fx-background-color: #5E81AC; -fx-text-fill: white; -fx-background-radius: 5;");
        } else {
            btnLogin.setText("Logout");
            lblStatus.setText("Conectado como: " + usuarioLogueado.getNombreCompleto() + " (" +
                            getRolDisplayName(usuarioLogueado.getRol()) + ")");
            btnLogin.setStyle("-fx-background-color: #D08770; -fx-text-fill: white; -fx-background-radius: 5;");
        }
    }

    /**
     * Convierte el rol técnico a un nombre más amigable
     */
    private String getRolDisplayName(String rol) {
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
     * Obtiene el usuario actualmente logueado
     */
    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }
}
