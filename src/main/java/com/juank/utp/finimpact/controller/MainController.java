package com.juank.utp.finimpact.controller;

import com.juank.utp.finimpact.model.Usuario;
import com.juank.utp.finimpact.utils.UserSession;
import com.juank.utp.finimpact.utils.AsyncTaskManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.IOException;

/**
 * Controlador para la vista principal de la aplicación
 */
public class MainController {

    @FXML private Button btnLogin;
    @FXML private Button btnLogout;
    @FXML private Button btnUserAction;
    @FXML private Label lblStatus;
    @FXML private VBox welcomeView;  // Vista central de bienvenida
    @FXML private VBox loginView;    // Vista del header para no autenticados
    @FXML private VBox userView;     // Vista del header para autenticados
    @FXML private TabPane mainTabPane;

    // Referencias a los controladores incluidos
    @FXML private DashboardController dashboardIncludeController;
    @FXML private IniciativaController iniciativaIncludeController;
    @FXML private ImpactoController impactoIncludeController;

    private Usuario usuarioLogueado;
    private static String ultimoEmailUsado = ""; // Variable estática para recordar el último email

    @FXML
    private void initialize() {
        actualizarEstadoUsuario();
    }

    /**
     * Maneja el evento del botón de login
     */
    @FXML
    private void handleLogin() {
        mostrarVentanaLogin();
    }

    /**
     * Maneja el evento del botón de logout
     */
    @FXML
    private void handleLogout() {
        realizarLogout();
    }

    /**
     * Maneja el evento del botón de acción del usuario (Mi Perfil / Gestión Usuarios)
     */
    @FXML
    private void handleUserAction() {
        if (usuarioLogueado == null) {
            return;
        }

        if ("admin".equals(usuarioLogueado.getRol())) {
            // Si es admin, mostrar gestión de usuarios
            mostrarGestionUsuarios();
        } else {
            // Si es usuario normal, mostrar mi perfil
            mostrarMiPerfil();
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
        // Limpiar la sesión global
        UserSession.limpiarSesion();
        actualizarEstadoUsuario();
        System.out.println("🔓 Usuario deslogueado exitosamente");
    }

    /**
     * Establece el usuario logueado (llamado desde LoginController)
     */
    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        // Establecer usuario en la sesión global para que otros controladores puedan accederlo
        UserSession.setUsuarioActual(usuario);
        actualizarEstadoUsuario();

        // Mostrar mensaje de carga
        lblStatus.setText("Configurando aplicación para " + usuario.getNombreCompleto() + "...");

        // Reconfigurar de forma asíncrona para evitar bloqueos
        AsyncTaskManager.executeAsyncWithMessage(
            () -> {
                // Simular configuración (esto se ejecuta en background)
                try {
                    Thread.sleep(500); // Pequeña pausa para mostrar el indicador
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Configuración completada";
            },
            (result) -> {
                // Este código se ejecuta en el UI thread después del éxito
                reconfigurarDashboard();
                lblStatus.setText("Conectado como: " + usuario.getNombreCompleto() + " (" + getRolDisplayName(usuario.getRol()) + ")");
            },
            (error) -> {
                // En caso de error
                System.err.println("Error configurando usuario: " + error.getMessage());
                lblStatus.setText("Error al configurar usuario");
            },
            null, // No hay ProgressIndicator en este caso
            lblStatus,
            "Configurando aplicación para " + usuario.getNombreCompleto() + "...",
            "Configuración completada"
        );

        System.out.println("✅ Usuario logueado: " + usuario.getNombreCompleto() + " (" + usuario.getRol() + ")");
    }

    /**
     * Reconfigura el dashboard cuando cambia el usuario
     */
    private void reconfigurarDashboard() {
        // Forzar recarga del dashboard
        System.out.println("🔄 Reconfigurando dashboard para el nuevo usuario...");

        // Configurar controladores de forma secuencial y asíncrona
        Platform.runLater(() -> {
            configurarControladores();
        });
    }

    /**
     * Configura los controladores de forma asíncrona y secuencial
     */
    private void configurarControladores() {
        // Configurar dashboard
        if (dashboardIncludeController != null && usuarioLogueado != null) {
            System.out.println("📊 Estableciendo usuario en DashboardController...");
            dashboardIncludeController.setUsuarioLogueado(usuarioLogueado);
        }

        // Configurar iniciativas de forma asíncrona
        if (iniciativaIncludeController != null && usuarioLogueado != null) {
            System.out.println("📋 Reconfigurando IniciativaController para usuario: " + usuarioLogueado.getRol());

            AsyncTaskManager.executeAsync(
                () -> {
                    // Configurar permisos en background
                    return "Iniciativas configuradas";
                },
                (result) -> {
                    // Configurar en UI thread
                    try {
                        iniciativaIncludeController.configurarPermisosSegunUsuario();
                        // Cargar datos de forma asíncrona
                        iniciativaIncludeController.cargarIniciativasAsync();
                    } catch (Exception e) {
                        System.err.println("Error configurando IniciativaController: " + e.getMessage());
                    }
                },
                (error) -> {
                    System.err.println("Error al reconfigurar IniciativaController: " + error.getMessage());
                }
            );
        }

        // Configurar impactos de forma asíncrona
        if (impactoIncludeController != null && usuarioLogueado != null) {
            System.out.println("💰 Reconfigurando ImpactoController para usuario: " + usuarioLogueado.getRol());

            AsyncTaskManager.executeAsync(
                () -> {
                    // Configurar permisos en background
                    return "Impactos configurados";
                },
                (result) -> {
                    // Configurar en UI thread
                    try {
                        impactoIncludeController.configurarPermisosSegunUsuario();
                        // Cargar datos de forma asíncrona
                        impactoIncludeController.cargarImpactosAsync();
                    } catch (Exception e) {
                        System.err.println("Error configurando ImpactoController: " + e.getMessage());
                    }
                },
                (error) -> {
                    System.err.println("Error al reconfigurar ImpactoController: " + error.getMessage());
                }
            );
        }

        System.out.println("✅ Controladores configurados para recargar con nuevo usuario");
    }

    /**
     * Actualiza la interfaz según el estado del usuario
     */
    private void actualizarEstadoUsuario() {
        if (usuarioLogueado == null) {
            // Usuario NO logueado - mostrar solo el botón de login
            lblStatus.setText("No hay usuario autenticado");

            // En el header: mostrar loginView, ocultar userView completamente
            loginView.setVisible(true);
            loginView.setManaged(true);
            userView.setVisible(false);
            userView.setManaged(false);

            // En el centro: mostrar welcomeView, ocultar TabPane
            welcomeView.setVisible(true);
            welcomeView.setManaged(true);
            mainTabPane.setVisible(false);
            mainTabPane.setManaged(false);
        } else {
            // Usuario logueado - mostrar botones de usuario
            lblStatus.setText("Conectado como: " + usuarioLogueado.getNombreCompleto() + " (" +
                            getRolDisplayName(usuarioLogueado.getRol()) + ")");

            // Configurar el botón de acción según el tipo de usuario
            if ("admin".equals(usuarioLogueado.getRol())) {
                btnUserAction.setText("Gestión Usuarios");
            } else {
                btnUserAction.setText("Mi Perfil");
            }

            // En el header: ocultar loginView completamente, mostrar userView
            loginView.setVisible(false);
            loginView.setManaged(false);
            userView.setVisible(true);
            userView.setManaged(true);

            // En el centro: ocultar welcomeView, mostrar TabPane
            welcomeView.setVisible(false);
            welcomeView.setManaged(false);
            mainTabPane.setVisible(true);
            mainTabPane.setManaged(true);
        }
    }

    /**
     * Convierte el tipo técnico a un nombre más amigable
     */
    private String getRolDisplayName(String tipo) {
        switch (tipo.toLowerCase()) {
            case "admin":
                return "Administrador";
            case "analista":
                return "Analista";
            case "viewer":
                return "Visualizador";
            default:
                return tipo;
        }
    }

    /**
     * Obtiene el usuario actualmente logueado
     */
    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    /**
     * Muestra la vista de gestión de usuarios (para admin)
     */
    private void mostrarGestionUsuarios() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/juank/utp/finimpact/usuarios-view.fxml"));
            Scene scene = new Scene(loader.load());

            Stage usuariosStage = new Stage();
            usuariosStage.setTitle("Gestión de Usuarios - FinImpact");
            usuariosStage.setScene(scene);
            usuariosStage.initModality(Modality.APPLICATION_MODAL);
            usuariosStage.setWidth(900);
            usuariosStage.setHeight(700);
            usuariosStage.setMinWidth(900);
            usuariosStage.setMinHeight(700);
            usuariosStage.centerOnScreen();
            usuariosStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error al cargar la vista de usuarios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Muestra la vista de mi perfil (para usuarios normales)
     */
    private void mostrarMiPerfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/juank/utp/finimpact/perfil-view.fxml"));
            Scene scene = new Scene(loader.load());

            Stage perfilStage = new Stage();
            perfilStage.setTitle("Mi Perfil - FinImpact");
            perfilStage.setScene(scene);
            perfilStage.initModality(Modality.APPLICATION_MODAL);
            perfilStage.setWidth(600);
            perfilStage.setHeight(500);
            perfilStage.setMinWidth(600);
            perfilStage.setMinHeight(500);
            perfilStage.centerOnScreen();

            // Pasar el usuario actual al controlador del perfil
            PerfilController perfilController = loader.getController();
            perfilController.setUsuario(usuarioLogueado);

            perfilStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error al cargar la vista de perfil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Guarda el email del último usuario que se conectó
     */
    public static void guardarUltimoEmail(String email) {
        ultimoEmailUsado = email;
    }

    /**
     * Obtiene el email del último usuario que se conectó
     */
    public static String getUltimoEmailUsado() {
        return ultimoEmailUsado;
    }
}
