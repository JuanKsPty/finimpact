package com.juank.utp.finimpact.controller;

import com.juank.utp.finimpact.model.Impacto;
import com.juank.utp.finimpact.model.Iniciativa;
import com.juank.utp.finimpact.repository.ImpactoRepository;
import com.juank.utp.finimpact.repository.IniciativaRepository;
import com.juank.utp.finimpact.utils.UserSession;
import com.juank.utp.finimpact.utils.AsyncTaskManager;
import com.juank.utp.finimpact.model.Usuario;
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

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador para la gestión de impactos financieros
 */
public class ImpactoController implements Initializable {

    // Componentes de la vista
    @FXML private ComboBox<String> cbTipo;
    @FXML private ComboBox<String> cbSeveridad;
    @FXML private TextField txtFiltroDescripcion;
    @FXML private Button btnCrearImpacto; // Botón para crear nuevo impacto
    @FXML private TableView<Impacto> tableImpactos;
    @FXML private TableColumn<Impacto, String> colId;
    @FXML private TableColumn<Impacto, String> colDescripcion;
    @FXML private TableColumn<Impacto, String> colTipo;
    @FXML private TableColumn<Impacto, String> colSeveridad;
    @FXML private TableColumn<Impacto, String> colValorMonetario;
    @FXML private TableColumn<Impacto, String> colFechaDeteccion;
    @FXML private TableColumn<Impacto, String> colIniciativaId;
    @FXML private TableColumn<Impacto, Void> colAcciones;

    // Repositorios
    private ImpactoRepository impactoRepository;
    private IniciativaRepository iniciativaRepository;

    // Lista de datos
    private ObservableList<Impacto> impactosList = FXCollections.observableArrayList();
    private ObservableList<Impacto> impactosFiltradosList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        impactoRepository = new ImpactoRepository();
        iniciativaRepository = new IniciativaRepository();

        configurarTabla();
        cargarImpactos();
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

        System.out.println("🔐 Configurando permisos de impactos para usuario: " +
                          (usuarioActual != null ? usuarioActual.getRol() : "sin usuario") +
                          " (Es viewer: " + esViewer + ")");

        // Si es viewer, ocultar/deshabilitar botón de crear
        if (btnCrearImpacto != null) {
            if (esViewer) {
                btnCrearImpacto.setVisible(false);
                btnCrearImpacto.setManaged(false);
                System.out.println("🚫 Botón crear impacto ocultado para viewer");
            } else {
                btnCrearImpacto.setVisible(true);
                btnCrearImpacto.setManaged(true);
                System.out.println("✅ Botón crear impacto visible para " + (usuarioActual != null ? usuarioActual.getRol() : "usuario"));
            }
        }

        // Reconfigurar la tabla para mostrar/ocultar botones de acción
        configurarTabla();
    }

    private void configurarTabla() {
        // Configurar columnas
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdImpacto())));
        colDescripcion.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipoImpacto()));
        colTipo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipoImpacto()));
        colSeveridad.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAtributoImpacto()));

        colValorMonetario.setCellValueFactory(cellData -> {
            BigDecimal valor = cellData.getValue().getImpacto();
            if (valor != null) {
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
                return new SimpleStringProperty(formatter.format(valor));
            }
            return new SimpleStringProperty("$0");
        });

        colFechaDeteccion.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaImpacto();
            return new SimpleStringProperty(fecha != null ? fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        });

        colIniciativaId.setCellValueFactory(cellData -> {
            int iniciativaId = cellData.getValue().getIdIniciativa();
            if (iniciativaId > 0) {
                try {
                    Optional<Iniciativa> iniciativaOpt = iniciativaRepository.findById(iniciativaId);
                    return new SimpleStringProperty(iniciativaOpt.map(Iniciativa::getNombre).orElse("N/A"));
                } catch (Exception e) {
                    return new SimpleStringProperty("Error");
                }
            }
            return new SimpleStringProperty("Sin asignar");
        });

        // Configurar columna de acciones
        colAcciones.setCellFactory(param -> new TableCell<Impacto, Void>() {
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
                    System.out.println("🚫 Botones de acción de impactos ocultos para viewer");
                } else {
                    pane.getChildren().addAll(btnEditar, btnEliminar);
                    pane.setAlignment(javafx.geometry.Pos.CENTER);

                    btnEditar.setOnAction(event -> {
                        Impacto impacto = getTableView().getItems().get(getIndex());
                        editarImpacto(impacto);
                    });

                    btnEliminar.setOnAction(event -> {
                        Impacto impacto = getTableView().getItems().get(getIndex());
                        eliminarImpacto(impacto);
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

        tableImpactos.setItems(impactosFiltradosList);
    }

    private void configurarFiltros() {
        cbTipo.setValue("Todos");
        cbSeveridad.setValue("Todos");
    }

    private void configurarFiltrosAutomaticos() {
        // Configurar filtros automáticos
        cbTipo.valueProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
        cbSeveridad.valueProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
        txtFiltroDescripcion.textProperty().addListener((observable, oldValue, newValue) -> aplicarFiltros());
    }

    @FXML
    private void mostrarFormularioImpacto() {
        // Verificar permisos antes de mostrar el formulario
        Usuario usuarioActual = UserSession.getUsuarioActual();
        if (usuarioActual != null && "viewer".equals(usuarioActual.getRol())) {
            mostrarError("Acceso Denegado", "Los usuarios con rol 'Viewer' no pueden crear impactos.");
            return;
        }
        mostrarFormulario(null);
    }

    @FXML
    private void aplicarFiltros() {
        String tipoFiltro = cbTipo.getValue();
        String severidadFiltro = cbSeveridad.getValue();
        String descripcionFiltro = txtFiltroDescripcion.getText().toLowerCase();

        impactosFiltradosList.clear();

        for (Impacto impacto : impactosList) {
            boolean cumpleTipo = "Todos".equals(tipoFiltro) || impacto.getTipoImpacto().equals(tipoFiltro);
            boolean cumpleSeveridad = "Todos".equals(severidadFiltro) || impacto.getAtributoImpacto().equals(severidadFiltro);
            boolean cumpleDescripcion = descripcionFiltro.isEmpty() ||
                    impacto.getTipoImpacto().toLowerCase().contains(descripcionFiltro);

            if (cumpleTipo && cumpleSeveridad && cumpleDescripcion) {
                impactosFiltradosList.add(impacto);
            }
        }
    }

    @FXML
    private void limpiarFiltros() {
        cbTipo.setValue("Todos");
        cbSeveridad.setValue("Todos");
        txtFiltroDescripcion.clear();
        impactosFiltradosList.setAll(impactosList);
    }

    /**
     * Método público para recargar impactos según el usuario actual
     */
    public void cargarImpactos() {
        try {
            Usuario usuarioActual = UserSession.getUsuarioActual();
            List<Impacto> impactos;

            if (usuarioActual != null && "analista".equals(usuarioActual.getRol())) {
                // Para analistas: solo cargar impactos relacionados a sus iniciativas
                impactos = impactoRepository.findByOwner(usuarioActual.getIdUsuario());
                System.out.println("🔍 Cargando impactos del analista: " + usuarioActual.getNombreCompleto() + " (" + impactos.size() + " impactos)");
            } else {
                // Para admin y viewer: cargar todos los impactos
                impactos = impactoRepository.findAll();
                System.out.println("🔍 Cargando todos los impactos para " + (usuarioActual != null ? usuarioActual.getRol() : "usuario") + " (" + impactos.size() + " impactos)");
            }

            impactosList.setAll(impactos);
            impactosFiltradosList.setAll(impactos);
        } catch (Exception e) {
            mostrarError("Error al cargar impactos", e.getMessage());
        }
    }

    /**
     * Método asíncrono para recargar impactos según el usuario actual
     */
    public void cargarImpactosAsync() {
        // Mostrar indicador de carga en la tabla
        Platform.runLater(() -> {
            tableImpactos.setPlaceholder(new Label("Cargando impactos..."));
        });

        AsyncTaskManager.executeAsyncWithMessage(
            () -> {
                // Esta operación se ejecuta en background thread
                Usuario usuarioActual = UserSession.getUsuarioActual();
                List<Impacto> impactos;

                if (usuarioActual != null && "analista".equals(usuarioActual.getRol())) {
                    // Para analistas: solo cargar impactos relacionados a sus iniciativas
                    impactos = impactoRepository.findByOwner(usuarioActual.getIdUsuario());
                    System.out.println("🔍 Cargando impactos del analista: " + usuarioActual.getNombreCompleto() + " (" + impactos.size() + " impactos)");
                } else {
                    // Para admin y viewer: cargar todos los impactos
                    impactos = impactoRepository.findAll();
                    System.out.println("🔍 Cargando todos los impactos para " + (usuarioActual != null ? usuarioActual.getRol() : "usuario") + " (" + impactos.size() + " impactos)");
                }

                return impactos;
            },
            (impactos) -> {
                // Este código se ejecuta en el UI thread después del éxito
                impactosList.setAll(impactos);
                impactosFiltradosList.setAll(impactos);
                tableImpactos.setPlaceholder(new Label("No hay impactos disponibles"));
                System.out.println("✅ Impactos cargados correctamente de forma asíncrona");
            },
            (error) -> {
                // En caso de error
                System.err.println("❌ Error al cargar impactos: " + error.getMessage());
                Platform.runLater(() -> {
                    tableImpactos.setPlaceholder(new Label("Error al cargar impactos"));
                    mostrarError("Error al cargar impactos", error.getMessage());
                });
            },
            null, // No hay ProgressIndicator específico
            null, // No hay Label de status específico
            "Cargando impactos...",
            "Impactos cargados correctamente"
        );
    }

    private void editarImpacto(Impacto impacto) {
        mostrarFormulario(impacto);
    }

    private void eliminarImpacto(Impacto impacto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar este impacto?");
        alert.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                impactoRepository.delete(impacto.getIdImpacto());
                cargarImpactos();
                mostrarInformacion("Éxito", "Impacto eliminado correctamente.");
            } catch (Exception e) {
                mostrarError("Error al eliminar impacto", e.getMessage());
            }
        }
    }

    private void mostrarFormulario(Impacto impacto) {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(impacto == null ? "Nuevo Impacto" : "Editar Impacto");

            // Forzar el tamaño del stage
            stage.setWidth(700);
            stage.setHeight(600);
            stage.setMinWidth(700);
            stage.setMinHeight(600);
            stage.setResizable(true);

            GridPane grid = new GridPane();
            grid.setHgap(20);
            grid.setVgap(20);
            grid.setPadding(new Insets(40));

            // Campos del formulario basados en el modelo Impacto real - HACERLOS MÁS GRANDES
            ComboBox<String> cbTipoForm = new ComboBox<>();
            cbTipoForm.getItems().addAll("Maquinaria", "Generación", "Optimización", "Transformación");
            cbTipoForm.setValue(impacto != null ? impacto.getTipoImpacto() : "Maquinaria");
            cbTipoForm.setPrefWidth(300);
            cbTipoForm.setPrefHeight(35);

            ComboBox<String> cbAtributoForm = new ComboBox<>();
            cbAtributoForm.getItems().addAll("Planeado", "Estimado", "Real");
            cbAtributoForm.setValue(impacto != null ? impacto.getAtributoImpacto() : "Planeado");
            cbAtributoForm.setPrefWidth(300);
            cbAtributoForm.setPrefHeight(35);

            ComboBox<Integer> cbMultiplicadorForm = new ComboBox<>();
            cbMultiplicadorForm.getItems().addAll(1, -1);
            cbMultiplicadorForm.setValue(impacto != null ? impacto.getMultiplicador() : 1);
            cbMultiplicadorForm.setPrefWidth(300);
            cbMultiplicadorForm.setPrefHeight(35);

            TextField txtValorImpacto = new TextField();
            if (impacto != null && impacto.getImpacto() != null) {
                txtValorImpacto.setText(impacto.getImpacto().toString());
            }
            txtValorImpacto.setPrefWidth(300);
            txtValorImpacto.setPrefHeight(35);

            DatePicker dpFechaImpacto = new DatePicker(impacto != null ? impacto.getFechaImpacto() : LocalDate.now());
            dpFechaImpacto.setPrefWidth(300);
            dpFechaImpacto.setPrefHeight(35);

            ComboBox<Iniciativa> cbIniciativa = new ComboBox<>();
            cbIniciativa.setPrefWidth(300);
            cbIniciativa.setPrefHeight(35);

            try {
                Usuario usuarioActual = UserSession.getUsuarioActual();
                List<Iniciativa> iniciativas;

                if (usuarioActual != null && "analista".equals(usuarioActual.getRol())) {
                    // Para analistas: solo mostrar sus iniciativas en el formulario
                    iniciativas = iniciativaRepository.findByOwner(usuarioActual.getIdUsuario());
                    System.out.println("📋 Cargando iniciativas del analista para formulario: " + iniciativas.size());
                } else {
                    // Para admin y viewer: mostrar todas las iniciativas
                    iniciativas = iniciativaRepository.findAll();
                    System.out.println("📋 Cargando todas las iniciativas para formulario: " + iniciativas.size());
                }

                cbIniciativa.getItems().setAll(iniciativas);
                if (impacto != null && impacto.getIdIniciativa() != 0) {
                    Iniciativa iniciativaSeleccionada = iniciativas.stream()
                            .filter(i -> i.getIdIniciativa() == impacto.getIdIniciativa())
                            .findFirst().orElse(null);
                    cbIniciativa.setValue(iniciativaSeleccionada);
                }
            } catch (Exception e) {
                mostrarError("Error", "No se pudieron cargar las iniciativas");
            }

            // Hacer las etiquetas más grandes
            Label lblTipo = new Label("Tipo de Impacto:");
            lblTipo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            Label lblAtributo = new Label("Atributo:");
            lblAtributo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            Label lblMultiplicador = new Label("Multiplicador:");
            lblMultiplicador.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            Label lblValor = new Label("Valor del Impacto:");
            lblValor.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            Label lblFecha = new Label("Fecha del Impacto:");
            lblFecha.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            Label lblIniciativa = new Label("Iniciativa:");
            lblIniciativa.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            // Agregar campos al grid
            grid.add(lblTipo, 0, 0);
            grid.add(cbTipoForm, 1, 0);
            grid.add(lblAtributo, 0, 1);
            grid.add(cbAtributoForm, 1, 1);
            grid.add(lblMultiplicador, 0, 2);
            grid.add(cbMultiplicadorForm, 1, 2);
            grid.add(lblValor, 0, 3);
            grid.add(txtValorImpacto, 1, 3);
            grid.add(lblFecha, 0, 4);
            grid.add(dpFechaImpacto, 1, 4);
            grid.add(lblIniciativa, 0, 5);
            grid.add(cbIniciativa, 1, 5);

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
            grid.add(buttonBox, 1, 6);

            btnGuardar.setOnAction(e -> {
                try {
                    if (cbTipoForm.getValue() == null) {
                        mostrarError("Error", "El tipo de impacto es obligatorio");
                        return;
                    }

                    Impacto nuevoImpacto = impacto != null ? impacto : new Impacto();
                    nuevoImpacto.setTipoImpacto(cbTipoForm.getValue());
                    nuevoImpacto.setAtributoImpacto(cbAtributoForm.getValue());
                    nuevoImpacto.setMultiplicador(cbMultiplicadorForm.getValue());
                    nuevoImpacto.setFechaImpacto(dpFechaImpacto.getValue());
                    nuevoImpacto.setFechaCreacion(LocalDate.now());

                    if (!txtValorImpacto.getText().trim().isEmpty()) {
                        try {
                            BigDecimal valor = new BigDecimal(txtValorImpacto.getText().trim());
                            nuevoImpacto.setImpacto(valor);
                        } catch (NumberFormatException ex) {
                            mostrarError("Error", "El valor del impacto debe ser un número válido");
                            return;
                        }
                    }

                    if (cbIniciativa.getValue() != null) {
                        nuevoImpacto.setIdIniciativa(cbIniciativa.getValue().getIdIniciativa());
                    }

                    if (impacto == null) {
                        impactoRepository.save(nuevoImpacto);
                        mostrarInformacion("Éxito", "Impacto creado correctamente.");
                    } else {
                        impactoRepository.update(nuevoImpacto);
                        mostrarInformacion("Éxito", "Impacto actualizado correctamente.");
                    }

                    cargarImpactos();
                    stage.close();
                } catch (Exception ex) {
                    mostrarError("Error al guardar impacto", ex.getMessage());
                }
            });

            btnCancelar.setOnAction(e -> stage.close());

            Scene scene = new Scene(grid, 700, 600);
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
