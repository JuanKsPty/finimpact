package com.juank.utp.finimpact.controller;

import com.juank.utp.finimpact.model.Iniciativa;
import com.juank.utp.finimpact.model.Usuario;
import com.juank.utp.finimpact.repository.IniciativaRepository;
import com.juank.utp.finimpact.repository.UsuarioRepository;
import com.juank.utp.finimpact.utils.UserSession;
import com.juank.utp.finimpact.utils.AsyncTaskManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador para la gestión de iniciativas
 */
public class IniciativaController implements Initializable {

    // Componentes de la vista
    @FXML private ComboBox<String> cbEstado;
    @FXML private ComboBox<String> cbRiesgo;
    @FXML private TextField txtFiltroNombre;
    @FXML private Button btnCrearIniciativa; // Botón para crear nueva iniciativa
    @FXML private TableView<Iniciativa> tableIniciativas;
    @FXML private TableColumn<Iniciativa, String> colId;
    @FXML private TableColumn<Iniciativa, String> colNombre;
    @FXML private TableColumn<Iniciativa, String> colDescripcion;
    @FXML private TableColumn<Iniciativa, String> colTipo;
    @FXML private TableColumn<Iniciativa, String> colEstado;
    @FXML private TableColumn<Iniciativa, String> colRiesgo;
    @FXML private TableColumn<Iniciativa, String> colFechaInicio;
    @FXML private TableColumn<Iniciativa, String> colFechaFin;
    @FXML private TableColumn<Iniciativa, Void> colAcciones;

    // Repositorios
    private IniciativaRepository iniciativaRepository;
    private UsuarioRepository usuarioRepository;

    // Lista de datos
    private final ObservableList<Iniciativa> iniciativasList = FXCollections.observableArrayList();
    private final ObservableList<Iniciativa> iniciativasFiltradasList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        iniciativaRepository = new IniciativaRepository();
        usuarioRepository = new UsuarioRepository();

        configurarTabla();
        cargarIniciativas();
        configurarFiltros();
        configurarFiltrosAutomaticos();
        configurarPermisosSegunUsuario(); // Configurar permisos según el rol del usuario
    }

    /**
     * Configura los permisos de la interfaz según el rol del usuario logueado
     */
    public void configurarPermisosSegunUsuario() {
        Usuario usuarioActual = UserSession.getUsuarioActual();
        boolean esViewer = usuarioActual != null && "viewer".equals(usuarioActual.getRol());

        System.out.println("🔐 Configurando permisos para usuario: " +
                          (usuarioActual != null ? usuarioActual.getRol() : "sin usuario") +
                          " (Es viewer: " + esViewer + ")");

        // Si es viewer, ocultar/deshabilitar botón de crear
        if (btnCrearIniciativa != null) {
            if (esViewer) {
                btnCrearIniciativa.setVisible(false);
                btnCrearIniciativa.setManaged(false);
                System.out.println("🚫 Botón crear iniciativa ocultado para viewer");
            } else {
                btnCrearIniciativa.setVisible(true);
                btnCrearIniciativa.setManaged(true);
                System.out.println("✅ Botón crear iniciativa visible para " + (usuarioActual != null ? usuarioActual.getRol() : "usuario"));
            }
        }

        // Reconfigurar la tabla para mostrar/ocultar botones de acción
        configurarTabla();
    }

    private void configurarTabla() {
        // Configurar columnas
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdIniciativa())));
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colDescripcion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescripcion()));
        colTipo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipo()));
        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado()));
        colRiesgo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRiesgo()));

        colFechaInicio.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaInicio();
            return new SimpleStringProperty(fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        });

        colFechaFin.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaFin();
            return new SimpleStringProperty(fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        });

        // Configurar columna de acciones
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox pane = new HBox(8);

            {
                // Verificar permisos del usuario actual
                Usuario usuarioActual = UserSession.getUsuarioActual();
                boolean esViewer = usuarioActual != null && "viewer".equals(usuarioActual.getRol());

                // Hacer los botones más grandes
                btnEditar.setPrefWidth(80);
                btnEditar.setPrefHeight(35);
                btnEliminar.setPrefWidth(80);
                btnEliminar.setPrefHeight(35);

                btnEditar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 12px; -fx-font-weight: bold;");
                btnEliminar.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 12px; -fx-font-weight: bold;");

                // Si es viewer, ocultar los botones
                if (esViewer) {
                    btnEditar.setVisible(false);
                    btnEliminar.setVisible(false);
                    System.out.println("🚫 Botones de acción ocultos para viewer");
                } else {
                    pane.getChildren().addAll(btnEditar, btnEliminar);
                    pane.setAlignment(javafx.geometry.Pos.CENTER);

                    btnEditar.setOnAction(event -> {
                        Iniciativa iniciativa = getTableView().getItems().get(getIndex());
                        editarIniciativa(iniciativa);
                    });

                    btnEliminar.setOnAction(event -> {
                        Iniciativa iniciativa = getTableView().getItems().get(getIndex());
                        eliminarIniciativa(iniciativa);
                    });
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                // Solo mostrar el panel si no es viewer y no está vacío
                Usuario usuarioActual = UserSession.getUsuarioActual();
                boolean esViewer = usuarioActual != null && "viewer".equals(usuarioActual.getRol());
                setGraphic((empty || esViewer) ? null : pane);
            }
        });

        tableIniciativas.setItems(iniciativasFiltradasList);
    }

    private void configurarFiltros() {
        cbEstado.setValue("Todos");
        cbRiesgo.setValue("Todos");
    }

    private void configurarFiltrosAutomaticos() {
        // Configurar filtros automáticos
        cbEstado.valueProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
        cbRiesgo.valueProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
        txtFiltroNombre.textProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
    }

    @FXML
    private void mostrarFormularioIniciativa() {
        // Verificar permisos antes de mostrar el formulario
        Usuario usuarioActual = UserSession.getUsuarioActual();
        if (usuarioActual != null && "viewer".equals(usuarioActual.getRol())) {
            mostrarError("Acceso Denegado", "Los usuarios con rol 'Viewer' no pueden crear iniciativas.");
            return;
        }
        mostrarFormulario(null);
    }

    @FXML
    private void aplicarFiltros() {
        String estadoFiltro = cbEstado.getValue();
        String riesgoFiltro = cbRiesgo.getValue();
        String nombreFiltro = txtFiltroNombre.getText().toLowerCase();

        iniciativasFiltradasList.clear();

        for (Iniciativa iniciativa : iniciativasList) {
            boolean cumpleEstado = "Todos".equals(estadoFiltro) || iniciativa.getEstado().equals(estadoFiltro);
            boolean cumpleRiesgo = "Todos".equals(riesgoFiltro) || iniciativa.getRiesgo().equals(riesgoFiltro);
            boolean cumpleNombre = nombreFiltro.isEmpty() ||
                    iniciativa.getNombre().toLowerCase().contains(nombreFiltro);

            if (cumpleEstado && cumpleRiesgo && cumpleNombre) {
                iniciativasFiltradasList.add(iniciativa);
            }
        }
    }

    @FXML
    private void limpiarFiltros() {
        cbEstado.setValue("Todos");
        cbRiesgo.setValue("Todos");
        txtFiltroNombre.clear();
        iniciativasFiltradasList.setAll(iniciativasList);
    }

    /**
     * Método público para recargar iniciativas según el usuario actual
     */
    public void cargarIniciativas() {
        try {
            Usuario usuarioActual = UserSession.getUsuarioActual();
            List<Iniciativa> iniciativas;

            if (usuarioActual != null && "analista".equals(usuarioActual.getRol())) {
                // Para analistas: solo cargar sus iniciativas asignadas
                iniciativas = iniciativaRepository.findByOwner(usuarioActual.getIdUsuario());
                System.out.println("🔍 Cargando iniciativas del analista: " + usuarioActual.getNombreCompleto() + " (" + iniciativas.size() + " iniciativas)");
            } else {
                // Para admin y viewer: cargar todas las iniciativas
                iniciativas = iniciativaRepository.findAll();
                System.out.println("🔍 Cargando todas las iniciativas para " + (usuarioActual != null ? usuarioActual.getRol() : "usuario") + " (" + iniciativas.size() + " iniciativas)");
            }

            iniciativasList.setAll(iniciativas);
            iniciativasFiltradasList.setAll(iniciativas);
        } catch (Exception e) {
            mostrarError("Error al cargar iniciativas", e.getMessage());
        }
    }

    /**
     * Método asíncrono para recargar iniciativas según el usuario actual
     */
    public void cargarIniciativasAsync() {
        // Mostrar indicador de carga en la tabla
        Platform.runLater(() -> {
            tableIniciativas.setPlaceholder(new Label("Cargando iniciativas..."));
        });

        AsyncTaskManager.executeAsyncWithMessage(
            () -> {
                // Esta operación se ejecuta en background thread
                Usuario usuarioActual = UserSession.getUsuarioActual();
                List<Iniciativa> iniciativas;

                if (usuarioActual != null && "analista".equals(usuarioActual.getRol())) {
                    // Para analistas: solo cargar sus iniciativas asignadas
                    iniciativas = iniciativaRepository.findByOwner(usuarioActual.getIdUsuario());
                    System.out.println("🔍 Cargando iniciativas del analista: " + usuarioActual.getNombreCompleto() + " (" + iniciativas.size() + " iniciativas)");
                } else {
                    // Para admin y viewer: cargar todas las iniciativas
                    iniciativas = iniciativaRepository.findAll();
                    System.out.println("🔍 Cargando todas las iniciativas para " + (usuarioActual != null ? usuarioActual.getRol() : "usuario") + " (" + iniciativas.size() + " iniciativas)");
                }

                return iniciativas;
            },
            (iniciativas) -> {
                // Este código se ejecuta en el UI thread después del éxito
                iniciativasList.setAll(iniciativas);
                iniciativasFiltradasList.setAll(iniciativas);
                tableIniciativas.setPlaceholder(new Label("No hay iniciativas disponibles"));
                System.out.println("✅ Iniciativas cargadas correctamente de forma asíncrona");
            },
            (error) -> {
                // En caso de error
                System.err.println("❌ Error al cargar iniciativas: " + error.getMessage());
                Platform.runLater(() -> {
                    tableIniciativas.setPlaceholder(new Label("Error al cargar iniciativas"));
                    mostrarError("Error al cargar iniciativas", error.getMessage());
                });
            },
            null, // No hay ProgressIndicator específico
            null, // No hay Label de status específico
            "Cargando iniciativas...",
            "Iniciativas cargadas correctamente"
        );
    }

    private void editarIniciativa(Iniciativa iniciativa) {
        mostrarFormulario(iniciativa);
    }

    private void eliminarIniciativa(Iniciativa iniciativa) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar esta iniciativa?");
        alert.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                iniciativaRepository.delete(iniciativa.getIdIniciativa());
                cargarIniciativas();
                mostrarInformacion("Éxito", "Iniciativa eliminada correctamente.");
            } catch (Exception e) {
                mostrarError("Error al eliminar iniciativa", e.getMessage());
            }
        }
    }

    private void mostrarFormulario(Iniciativa iniciativa) {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(iniciativa == null ? "Nueva Iniciativa" : "Editar Iniciativa");

            // Forzar el tamaño del stage
            stage.setWidth(750);
            stage.setHeight(700);
            stage.setMinWidth(750);
            stage.setMinHeight(700);
            stage.setResizable(true);

            GridPane grid = new GridPane();
            grid.setHgap(20);
            grid.setVgap(20);
            grid.setPadding(new Insets(40));

            // Campos del formulario - HACERLOS MÁS GRANDES
            TextField txtNombre = new TextField(iniciativa != null ? iniciativa.getNombre() : "");
            txtNombre.setPrefWidth(350);
            txtNombre.setPrefHeight(35);

            TextArea txtDescripcion = new TextArea(iniciativa != null ? iniciativa.getDescripcion() : "");
            txtDescripcion.setPrefRowCount(4);
            txtDescripcion.setPrefWidth(350);
            txtDescripcion.setPrefHeight(100);

            TextField txtTipo = new TextField(iniciativa != null ? iniciativa.getTipo() : "");
            txtTipo.setPrefWidth(350);
            txtTipo.setPrefHeight(35);

            ComboBox<String> cbEstadoForm = new ComboBox<>();
            cbEstadoForm.getItems().addAll("planeado", "en curso", "finalizado", "cancelado");
            cbEstadoForm.setValue(iniciativa != null ? iniciativa.getEstado() : "planeado");
            cbEstadoForm.setPrefWidth(350);
            cbEstadoForm.setPrefHeight(35);

            ComboBox<String> cbRiesgoForm = new ComboBox<>();
            cbRiesgoForm.getItems().addAll("alto", "medio", "bajo");
            cbRiesgoForm.setValue(iniciativa != null ? iniciativa.getRiesgo() : "medio");
            cbRiesgoForm.setPrefWidth(350);
            cbRiesgoForm.setPrefHeight(35);

            DatePicker dpFechaInicio = new DatePicker(iniciativa != null ? iniciativa.getFechaInicio() : LocalDate.now());
            dpFechaInicio.setPrefWidth(350);
            dpFechaInicio.setPrefHeight(35);

            DatePicker dpFechaFin = new DatePicker(iniciativa != null ? iniciativa.getFechaFin() : null);
            dpFechaFin.setPrefWidth(350);
            dpFechaFin.setPrefHeight(35);

            ComboBox<Usuario> cbUsuario = new ComboBox<>();
            cbUsuario.setPrefWidth(350);
            cbUsuario.setPrefHeight(35);

            try {
                List<Usuario> usuarios = usuarioRepository.findAll();
                cbUsuario.getItems().setAll(usuarios);
                if (iniciativa != null && iniciativa.getUsuarioId() != 0) {
                    Usuario usuarioSeleccionado = usuarios.stream()
                            .filter(u -> u.getId() == iniciativa.getUsuarioId())
                            .findFirst().orElse(null);
                    cbUsuario.setValue(usuarioSeleccionado);
                }
            } catch (Exception e) {
                mostrarError("Error", "No se pudieron cargar los usuarios");
            }

            // Hacer las etiquetas más grandes
            Label lblNombre = new Label("Nombre:");
            lblNombre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            Label lblDescripcion = new Label("Descripción:");
            lblDescripcion.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            Label lblTipo = new Label("Tipo:");
            lblTipo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            Label lblEstado = new Label("Estado:");
            lblEstado.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            Label lblRiesgo = new Label("Riesgo:");
            lblRiesgo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            Label lblFechaInicio = new Label("Fecha Inicio:");
            lblFechaInicio.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            Label lblFechaFin = new Label("Fecha Fin:");
            lblFechaFin.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            Label lblUsuario = new Label("Usuario:");
            lblUsuario.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            // Agregar campos al grid
            grid.add(lblNombre, 0, 0);
            grid.add(txtNombre, 1, 0);
            grid.add(lblDescripcion, 0, 1);
            grid.add(txtDescripcion, 1, 1);
            grid.add(lblTipo, 0, 2);
            grid.add(txtTipo, 1, 2);
            grid.add(lblEstado, 0, 3);
            grid.add(cbEstadoForm, 1, 3);
            grid.add(lblRiesgo, 0, 4);
            grid.add(cbRiesgoForm, 1, 4);
            grid.add(lblFechaInicio, 0, 5);
            grid.add(dpFechaInicio, 1, 5);
            grid.add(lblFechaFin, 0, 6);
            grid.add(dpFechaFin, 1, 6);
            grid.add(lblUsuario, 0, 7);
            grid.add(cbUsuario, 1, 7);

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
            grid.add(buttonBox, 1, 8);

            btnGuardar.setOnAction(e -> {
                try {
                    if (txtNombre.getText().trim().isEmpty()) {
                        mostrarError("Error", "El nombre es obligatorio");
                        return;
                    }

                    Iniciativa nuevaIniciativa = iniciativa != null ? iniciativa : new Iniciativa();
                    nuevaIniciativa.setNombre(txtNombre.getText().trim());
                    nuevaIniciativa.setDescripcion(txtDescripcion.getText().trim());
                    nuevaIniciativa.setTipo(txtTipo.getText().trim());
                    nuevaIniciativa.setEstado(cbEstadoForm.getValue());
                    nuevaIniciativa.setRiesgo(cbRiesgoForm.getValue());
                    nuevaIniciativa.setFechaInicio(dpFechaInicio.getValue());
                    nuevaIniciativa.setFechaFin(dpFechaFin.getValue());

                    if (cbUsuario.getValue() != null) {
                        nuevaIniciativa.setUsuarioId(cbUsuario.getValue().getId());
                    }

                    if (iniciativa == null) {
                        iniciativaRepository.save(nuevaIniciativa);
                        mostrarInformacion("Éxito", "Iniciativa creada correctamente.");
                    } else {
                        iniciativaRepository.update(nuevaIniciativa);
                        mostrarInformacion("Éxito", "Iniciativa actualizada correctamente.");
                    }

                    cargarIniciativas();
                    stage.close();
                } catch (Exception ex) {
                    mostrarError("Error al guardar iniciativa", ex.getMessage());
                }
            });

            btnCancelar.setOnAction(e -> stage.close());

            Scene scene = new Scene(grid, 750, 700);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.showAndWait();

        } catch (Exception e) {
            mostrarError("Error al abrir formulario", e.getMessage());
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
