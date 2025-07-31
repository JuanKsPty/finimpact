package com.juank.utp.finimpact.controller;

import com.juank.utp.finimpact.model.Usuario;
import com.juank.utp.finimpact.repository.UsuarioRepository;
import com.juank.utp.finimpact.utils.PasswordUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador para la gestión de usuarios (solo admin)
 */
public class UsuarioController implements Initializable {

    // Componentes de la vista
    @FXML private ComboBox<String> cbTipo;
    @FXML private TextField txtFiltroNombre;
    @FXML private TableView<Usuario> tableUsuarios;
    @FXML private TableColumn<Usuario, String> colId;
    @FXML private TableColumn<Usuario, String> colNombreCompleto;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colTipo;
    @FXML private TableColumn<Usuario, Void> colAcciones;

    // Repositorio
    private UsuarioRepository usuarioRepository;

    // Lista de datos
    private final ObservableList<Usuario> usuariosList = FXCollections.observableArrayList();
    private final ObservableList<Usuario> usuariosFiltradosList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usuarioRepository = new UsuarioRepository();

        configurarTabla();
        cargarUsuarios();
        configurarFiltros();
        configurarFiltrosAutomaticos();
    }

    private void configurarTabla() {
        // Configurar columnas
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        colNombreCompleto.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombreCompleto()));
        colUsuario.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail())); // Usando email como usuario
        colEmail.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        colTipo.setCellValueFactory(cellData -> new SimpleStringProperty(getTipoDisplayName(cellData.getValue().getRol())));

        // Configurar columna de acciones
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox pane = new HBox(8);

            {
                // Hacer los botones más grandes
                btnEditar.setPrefWidth(80);
                btnEditar.setPrefHeight(35);
                btnEliminar.setPrefWidth(80);
                btnEliminar.setPrefHeight(35);

                btnEditar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 12px; -fx-font-weight: bold;");
                btnEliminar.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 12px; -fx-font-weight: bold;");

                pane.getChildren().addAll(btnEditar, btnEliminar);
                pane.setAlignment(javafx.geometry.Pos.CENTER);

                btnEditar.setOnAction(event -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    editarUsuario(usuario);
                });

                btnEliminar.setOnAction(event -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    eliminarUsuario(usuario);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        tableUsuarios.setItems(usuariosFiltradosList);
    }

    private void configurarFiltros() {
        cbTipo.setValue("Todos");
    }

    private void configurarFiltrosAutomaticos() {
        // Configurar filtros automáticos
        cbTipo.valueProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
        txtFiltroNombre.textProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
    }

    @FXML
    private void mostrarFormularioUsuario() {
        mostrarFormulario(null);
    }

    @FXML
    private void aplicarFiltros() {
        String tipoFiltro = cbTipo.getValue();
        String nombreFiltro = txtFiltroNombre.getText().toLowerCase();

        usuariosFiltradosList.clear();

        for (Usuario usuario : usuariosList) {
            boolean cumpleTipo = "Todos".equals(tipoFiltro) || usuario.getRol().equals(tipoFiltro);
            boolean cumpleNombre = nombreFiltro.isEmpty() ||
                    usuario.getNombreCompleto().toLowerCase().contains(nombreFiltro) ||
                    usuario.getEmail().toLowerCase().contains(nombreFiltro);

            if (cumpleTipo && cumpleNombre) {
                usuariosFiltradosList.add(usuario);
            }
        }
    }

    @FXML
    private void limpiarFiltros() {
        cbTipo.setValue("Todos");
        txtFiltroNombre.clear();
        usuariosFiltradosList.setAll(usuariosList);
    }

    private void cargarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            usuariosList.setAll(usuarios);
            usuariosFiltradosList.setAll(usuarios);
        } catch (Exception e) {
            mostrarError("Error al cargar usuarios", e.getMessage());
        }
    }

    private void editarUsuario(Usuario usuario) {
        mostrarFormulario(usuario);
    }

    private void eliminarUsuario(Usuario usuario) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar este usuario?");
        alert.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                usuarioRepository.delete(usuario.getId());
                cargarUsuarios();
                mostrarInformacion("Éxito", "Usuario eliminado correctamente.");
            } catch (Exception e) {
                mostrarError("Error al eliminar usuario", e.getMessage());
            }
        }
    }

    private void mostrarFormulario(Usuario usuario) {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(usuario == null ? "Nuevo Usuario" : "Editar Usuario");

            // Forzar el tamaño del stage
            stage.setWidth(650);
            stage.setHeight(500);
            stage.setMinWidth(650);
            stage.setMinHeight(500);
            stage.setResizable(true);

            GridPane grid = new GridPane();
            grid.setHgap(20);
            grid.setVgap(20);
            grid.setPadding(new Insets(40));

            // Campos del formulario - HACERLOS MÁS GRANDES
            TextField txtNombreCompleto = new TextField(usuario != null ? usuario.getNombreCompleto() : "");
            txtNombreCompleto.setPrefWidth(350);
            txtNombreCompleto.setPrefHeight(35);

            TextField txtEmail = new TextField(usuario != null ? usuario.getEmail() : "");
            txtEmail.setPrefWidth(350);
            txtEmail.setPrefHeight(35);

            ComboBox<String> cbRolForm = new ComboBox<>();
            cbRolForm.getItems().addAll("admin", "analista", "viewer");
            cbRolForm.setValue(usuario != null ? usuario.getRol() : "viewer");
            cbRolForm.setPrefWidth(350);
            cbRolForm.setPrefHeight(35);

            PasswordField txtPassword = new PasswordField();
            txtPassword.setPromptText(usuario != null ? "Dejar vacío para mantener contraseña actual" : "Contraseña");
            txtPassword.setPrefWidth(350);
            txtPassword.setPrefHeight(35);

            // Hacer las etiquetas más grandes
            Label lblNombre = new Label("Nombre Completo:");
            lblNombre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            Label lblEmail = new Label("Email:");
            lblEmail.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            Label lblRol = new Label("Rol:");
            lblRol.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            Label lblPassword = new Label("Contraseña:");
            lblPassword.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            // Agregar campos al grid
            grid.add(lblNombre, 0, 0);
            grid.add(txtNombreCompleto, 1, 0);
            grid.add(lblEmail, 0, 1);
            grid.add(txtEmail, 1, 1);
            grid.add(lblRol, 0, 2);
            grid.add(cbRolForm, 1, 2);
            grid.add(lblPassword, 0, 3);
            grid.add(txtPassword, 1, 3);

            // Botones más grandes
            HBox buttonBox = new HBox(15);
            Button btnGuardar = new Button("Guardar");
            Button btnCancelar = new Button("Cancelar");

            btnGuardar.setPrefWidth(120);
            btnGuardar.setPrefHeight(40);
            btnGuardar.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            btnCancelar.setPrefWidth(120);
            btnCancelar.setPrefHeight(40);
            btnCancelar.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            buttonBox.getChildren().addAll(btnGuardar, btnCancelar);
            grid.add(buttonBox, 1, 4);

            btnGuardar.setOnAction(e -> {
                try {
                    if (txtNombreCompleto.getText().trim().isEmpty()) {
                        mostrarError("Error", "El nombre completo es obligatorio");
                        return;
                    }
                    if (txtEmail.getText().trim().isEmpty()) {
                        mostrarError("Error", "El email es obligatorio");
                        return;
                    }

                    Usuario nuevoUsuario = usuario != null ? usuario : new Usuario();
                    nuevoUsuario.setNombreCompleto(txtNombreCompleto.getText().trim());
                    nuevoUsuario.setEmail(txtEmail.getText().trim());
                    nuevoUsuario.setRol(cbRolForm.getValue());
                    nuevoUsuario.setEstado(true); // Usuario activo por defecto

                    // Solo cambiar contraseña si se ingresó una nueva
                    if (!txtPassword.getText().trim().isEmpty()) {
                        String hashedPassword = PasswordUtils.hashPassword(txtPassword.getText().trim());
                        nuevoUsuario.setPassword(hashedPassword);
                    }

                    if (usuario == null) {
                        // Nuevo usuario, la contraseña es obligatoria
                        if (txtPassword.getText().trim().isEmpty()) {
                            mostrarError("Error", "La contraseña es obligatoria para nuevos usuarios");
                            return;
                        }
                        usuarioRepository.save(nuevoUsuario);
                        mostrarInformacion("Éxito", "Usuario creado correctamente.");
                    } else {
                        usuarioRepository.update(nuevoUsuario);
                        mostrarInformacion("Éxito", "Usuario actualizado correctamente.");
                    }

                    cargarUsuarios();
                    stage.close();
                } catch (Exception ex) {
                    mostrarError("Error al guardar usuario", ex.getMessage());
                }
            });

            btnCancelar.setOnAction(e -> stage.close());

            Scene scene = new Scene(grid, 650, 500);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.showAndWait();

        } catch (Exception e) {
            mostrarError("Error al abrir formulario", e.getMessage());
        }
    }

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

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
