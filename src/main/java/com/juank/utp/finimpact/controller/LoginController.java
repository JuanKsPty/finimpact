package com.juank.utp.finimpact.controller;

import com.juank.utp.finimpact.model.Usuario;
import com.juank.utp.finimpact.repository.UsuarioRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Controlador para la vista de login
 */
public class LoginController {

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnIngresar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Label lblEmailError;

    @FXML
    private Label lblPasswordError;

    @FXML
    private Label lblLoginError;

    private MainController mainController;
    private UsuarioRepository usuarioRepository;

    @FXML
    private void initialize() {
        usuarioRepository = new UsuarioRepository();

        // Cargar el último email usado si existe
        String ultimoEmail = MainController.getUltimoEmailUsado();
        if (!ultimoEmail.isEmpty()) {
            txtEmail.setText(ultimoEmail);
            // Enfocar el campo de contraseña ya que el email está prellenado
            txtPassword.requestFocus();
        }

        // Configurar validaciones en tiempo real
        txtEmail.textProperty().addListener((observable, oldValue, newValue) -> {
            limpiarErrorEmail();
        });

        txtPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            limpiarErrorPassword();
        });

        // Permitir login con Enter
        txtPassword.setOnAction(event -> handleLogin());
    }

    /**
     * Establece la referencia al controlador principal
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Maneja el evento de login
     */
    @FXML
    private void handleLogin() {
        limpiarErrores();

        if (!validarCampos()) {
            return;
        }

        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        // Deshabilitar botón mientras se procesa
        btnIngresar.setDisable(true);
        btnIngresar.setText("Ingresando...");

        try {
            // Intentar autenticación
            Optional<Usuario> usuarioOpt = usuarioRepository.authenticate(email, password);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();

                // Verificar que el usuario esté activo (doble verificación)
                if (!usuario.isEstado()) {
                    mostrarErrorLogin("Usuario inactivo. Contacte al administrador.");
                    return;
                }

                // Guardar el email para futuras sesiones
                MainController.guardarUltimoEmail(email);

                // Login exitoso
                mainController.setUsuarioLogueado(usuario);
                cerrarVentana();

            } else {
                mostrarErrorLogin("Email o contraseña incorrectos.");
            }

        } catch (Exception e) {
            System.err.println("Error durante el login: " + e.getMessage());
            e.printStackTrace();
            mostrarErrorLogin("Error de conexión. Intente nuevamente.");
        } finally {
            // Rehabilitar botón
            btnIngresar.setDisable(false);
            btnIngresar.setText("Ingresar");
        }
    }

    /**
     * Maneja el evento de cancelar
     */
    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    /**
     * Valida que todos los campos estén llenos y correctos
     */
    private boolean validarCampos() {
        boolean esValido = true;

        // Validar email
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            mostrarErrorEmail("El email es requerido");
            esValido = false;
        } else if (!esEmailValido(email)) {
            mostrarErrorEmail("Formato de email inválido");
            esValido = false;
        }

        // Validar contraseña
        String password = txtPassword.getText();
        if (password.isEmpty()) {
            mostrarErrorPassword("La contraseña es requerida");
            esValido = false;
        } else if (password.length() < 3) {
            mostrarErrorPassword("La contraseña debe tener al menos 3 caracteres");
            esValido = false;
        }

        return esValido;
    }

    /**
     * Valida el formato del email
     */
    private boolean esEmailValido(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Muestra error en el campo email
     */
    private void mostrarErrorEmail(String mensaje) {
        lblEmailError.setText(mensaje);
        lblEmailError.setVisible(true);
        txtEmail.setStyle("-fx-border-color: #BF616A; -fx-background-radius: 5; -fx-border-radius: 5;");
    }

    /**
     * Muestra error en el campo contraseña
     */
    private void mostrarErrorPassword(String mensaje) {
        lblPasswordError.setText(mensaje);
        lblPasswordError.setVisible(true);
        txtPassword.setStyle("-fx-border-color: #BF616A; -fx-background-radius: 5; -fx-border-radius: 5;");
    }

    /**
     * Muestra error general de login
     */
    private void mostrarErrorLogin(String mensaje) {
        lblLoginError.setText(mensaje);
        lblLoginError.setVisible(true);
    }

    /**
     * Limpia el error del campo email
     */
    private void limpiarErrorEmail() {
        lblEmailError.setVisible(false);
        txtEmail.setStyle("-fx-background-radius: 5; -fx-border-radius: 5;");
    }

    /**
     * Limpia el error del campo contraseña
     */
    private void limpiarErrorPassword() {
        lblPasswordError.setVisible(false);
        txtPassword.setStyle("-fx-background-radius: 5; -fx-border-radius: 5;");
    }

    /**
     * Limpia todos los errores
     */
    private void limpiarErrores() {
        limpiarErrorEmail();
        limpiarErrorPassword();
        lblLoginError.setVisible(false);
    }

    /**
     * Cierra la ventana de login
     */
    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
