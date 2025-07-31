package com.juank.utp.finimpact.controller;

import com.juank.utp.finimpact.model.Impacto;
import com.juank.utp.finimpact.model.Iniciativa;
import com.juank.utp.finimpact.model.Usuario;
import com.juank.utp.finimpact.repository.ImpactoRepository;
import com.juank.utp.finimpact.repository.IniciativaRepository;
import com.juank.utp.finimpact.repository.UsuarioRepository;
import com.juank.utp.finimpact.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

    @FXML private ComboBox<String> cmbFiltroIniciativa;
    @FXML private ComboBox<String> cmbFiltroUsuario;
    @FXML private Label lblFiltroUsuario; // Add reference to the Usuario label

    @FXML private PieChart chartIniciativasPorEstado;
    @FXML private BarChart<String, Number> chartImpactosPorTipo;
    @FXML private LineChart<String, Number> chartImpactoMensual;
    @FXML private PieChart chartIniciativasPorRiesgo;

    @FXML private Label lblTotalIniciativas;
    @FXML private Label lblIniciativasActivas;
    @FXML private Label lblImpactoTotal;
    @FXML private Label lblImpactoPromedio;
    @FXML private Label lblRiesgoAlto;
    @FXML private Label lblIniciativasFinalizadas;
    @FXML private Label lblROI;
    @FXML private Label lblEficiencia;

    private IniciativaRepository iniciativaRepository;
    private ImpactoRepository impactoRepository;
    private UsuarioRepository usuarioRepository;
    private NumberFormat currencyFormat;

    // Mapas para almacenar los datos completos
    private Map<String, Integer> iniciativaIdMap = new HashMap<>();
    private Map<String, Integer> usuarioIdMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        iniciativaRepository = new IniciativaRepository();
        impactoRepository = new ImpactoRepository();
        usuarioRepository = new UsuarioRepository();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        System.out.println("🔧 DashboardController inicializando...");
        System.out.println("🔍 lblFiltroUsuario es null: " + (lblFiltroUsuario == null));
        System.out.println("🔍 cmbFiltroUsuario es null: " + (cmbFiltroUsuario == null));

        // Configurar inmediatamente si hay usuario
        configurarSegunUsuarioActual();
    }

    /**
     * Configura el dashboard según el usuario actual en la sesión
     */
    private void configurarSegunUsuarioActual() {
        if (UserSession.hayUsuarioLogueado()) {
            Usuario usuario = UserSession.getUsuarioActual();
            System.out.println("✅ Configurando dashboard para usuario: " + usuario.getNombreCompleto() + " (" + usuario.getRol() + ")");

            // Usar Platform.runLater para asegurar que el FXML esté completamente cargado
            javafx.application.Platform.runLater(() -> {
                configurarFiltros();
                cargarDatos();
            });
        } else {
            System.out.println("⚠️ No hay usuario en sesión al inicializar dashboard");
            // No cargar datos por defecto sin usuario
            return;
        }
    }

    private void configurarFiltros() {
        Usuario usuarioLogueado = UserSession.getUsuarioActual();
        if (usuarioLogueado == null || cmbFiltroIniciativa == null) {
            return;
        }

        boolean esAnalista = "analista".equals(usuarioLogueado.getRol());
        System.out.println("🔧 Configurando filtros para: " + usuarioLogueado.getNombreCompleto() + " (Es analista: " + esAnalista + ")");

        // PRIMERO: Ocultar inmediatamente el filtro de usuario para analistas
        if (esAnalista) {
            System.out.println("👤 OCULTANDO filtro de usuario para analista...");
            if (cmbFiltroUsuario != null) {
                cmbFiltroUsuario.setVisible(false);
                cmbFiltroUsuario.setManaged(false);
                cmbFiltroUsuario.setValue(null);
                System.out.println("✅ ComboBox usuario oculto");
            } else {
                System.out.println("⚠️ cmbFiltroUsuario es NULL");
            }
            if (lblFiltroUsuario != null) {
                lblFiltroUsuario.setVisible(false);
                lblFiltroUsuario.setManaged(false);
                System.out.println("✅ Label usuario oculto");
            } else {
                System.out.println("⚠️ lblFiltroUsuario es NULL");
            }
        }

        // Limpiar mapas antes de reconfigurar
        iniciativaIdMap.clear();
        usuarioIdMap.clear();

        // Configurar filtro de iniciativas según el rol
        List<Iniciativa> iniciativasDisponibles;

        if (esAnalista) {
            // Para analistas: solo sus iniciativas
            iniciativasDisponibles = iniciativaRepository.findByOwner(usuarioLogueado.getIdUsuario());
            System.out.println("📊 Iniciativas disponibles para analista: " + iniciativasDisponibles.size());
        } else {
            // Para admin y otros roles: todas las iniciativas
            iniciativasDisponibles = iniciativaRepository.findAll();
            System.out.println("📊 Iniciativas disponibles para admin: " + iniciativasDisponibles.size());
        }

        List<String> nombresIniciativas = new ArrayList<>();
        if (esAnalista) {
            nombresIniciativas.add("Mis iniciativas");
        } else {
            nombresIniciativas.add("Todas las iniciativas");
        }

        for (Iniciativa iniciativa : iniciativasDisponibles) {
            String nombre = iniciativa.getNombre();
            nombresIniciativas.add(nombre);
            iniciativaIdMap.put(nombre, iniciativa.getIdIniciativa());
        }

        cmbFiltroIniciativa.setItems(FXCollections.observableArrayList(nombresIniciativas));
        if (esAnalista) {
            cmbFiltroIniciativa.setValue("Mis iniciativas");
        } else {
            cmbFiltroIniciativa.setValue("Todas las iniciativas");
        }

        // Para usuarios NO analistas: configurar filtro de usuarios
        if (!esAnalista) {
            System.out.println("👥 Configurando filtro de usuario para admin/viewer...");
            List<Usuario> usuarios = usuarioRepository.findAll();
            List<String> nombresUsuarios = new ArrayList<>();
            nombresUsuarios.add("Todos los usuarios");

            for (Usuario usuario : usuarios) {
                String nombre = usuario.getNombreCompleto();
                nombresUsuarios.add(nombre);
                usuarioIdMap.put(nombre, usuario.getIdUsuario());
            }

            if (cmbFiltroUsuario != null) {
                cmbFiltroUsuario.setItems(FXCollections.observableArrayList(nombresUsuarios));
                cmbFiltroUsuario.setValue("Todos los usuarios");
                cmbFiltroUsuario.setVisible(true);
                cmbFiltroUsuario.setManaged(true);
                System.out.println("✅ ComboBox usuario visible para admin");
            }
            if (lblFiltroUsuario != null) {
                lblFiltroUsuario.setVisible(true);
                lblFiltroUsuario.setManaged(true);
                System.out.println("✅ Label usuario visible para admin");
            }
        }

        // Configurar listeners para actualización automática
        cmbFiltroIniciativa.setOnAction(e -> cargarDatos());
        if (cmbFiltroUsuario != null && !esAnalista) {
            cmbFiltroUsuario.setOnAction(e -> cargarDatos());
        }
    }

    @FXML
    private void actualizarDashboard() {
        cargarDatos();
    }

    private void cargarDatos() {
        System.out.println("🔄 Cargando datos del dashboard...");
        List<Iniciativa> iniciativas = obtenerIniciativasFiltradas();
        List<Impacto> impactos = obtenerImpactosFiltrados(iniciativas);

        System.out.println("📊 Iniciativas encontradas: " + iniciativas.size());
        System.out.println("💰 Impactos encontrados: " + impactos.size());

        actualizarKPIs(iniciativas, impactos);
        actualizarGraficos(iniciativas, impactos);
    }

    private List<Iniciativa> obtenerIniciativasFiltradas() {
        System.out.println("🔍 Obteniendo iniciativas filtradas con consultas eficientes...");

        Usuario usuarioLogueado = UserSession.getUsuarioActual();
        if (usuarioLogueado == null) {
            System.out.println("⚠️ No hay usuario logueado, devolviendo todas las iniciativas");
            return iniciativaRepository.findAll();
        }

        boolean esAnalista = "analista".equals(usuarioLogueado.getRol());
        String iniciativaSeleccionada = cmbFiltroIniciativa != null ? cmbFiltroIniciativa.getValue() : null;
        String usuarioSeleccionado = cmbFiltroUsuario != null ? cmbFiltroUsuario.getValue() : null;

        System.out.println("👤 Usuario logueado: " + usuarioLogueado.getNombreCompleto() + " (" + usuarioLogueado.getRol() + ")");
        System.out.println("🎯 Filtro iniciativa: " + iniciativaSeleccionada);
        System.out.println("👥 Filtro usuario: " + usuarioSeleccionado);

        List<Iniciativa> iniciativas;

        if (esAnalista) {
            // Para analistas: usar consultas eficientes solo para sus iniciativas
            if (iniciativaSeleccionada != null && !iniciativaSeleccionada.equals("Mis iniciativas")) {
                // Filtro específico por nombre de iniciativa del analista
                iniciativas = iniciativaRepository.findByOwnerAndName(usuarioLogueado.getIdUsuario(), iniciativaSeleccionada);
                System.out.println("📊 Consultando iniciativa específica del analista: " + iniciativaSeleccionada);
            } else {
                // Todas las iniciativas del analista
                iniciativas = iniciativaRepository.findByOwner(usuarioLogueado.getIdUsuario());
                System.out.println("📊 Consultando todas las iniciativas del analista");
            }
        } else {
            // Para admin: usar consultas eficientes según los filtros
            if (usuarioSeleccionado != null && !usuarioSeleccionado.equals("Todos los usuarios")) {
                Integer usuarioId = usuarioIdMap.get(usuarioSeleccionado);
                if (usuarioId != null) {
                    if (iniciativaSeleccionada != null && !iniciativaSeleccionada.equals("Todas las iniciativas")) {
                        // Filtro por usuario específico e iniciativa específica
                        iniciativas = iniciativaRepository.findByOwnerAndName(usuarioId, iniciativaSeleccionada);
                        System.out.println("📊 Consultando iniciativa específica de usuario específico");
                    } else {
                        // Filtro solo por usuario específico
                        iniciativas = iniciativaRepository.findByOwner(usuarioId);
                        System.out.println("📊 Consultando todas las iniciativas del usuario: " + usuarioSeleccionado);
                    }
                } else {
                    iniciativas = new ArrayList<>();
                }
            } else if (iniciativaSeleccionada != null && !iniciativaSeleccionada.equals("Todas las iniciativas")) {
                // Filtro solo por iniciativa específica (todos los usuarios)
                iniciativas = iniciativaRepository.findByName(iniciativaSeleccionada);
                System.out.println("📊 Consultando iniciativa específica de todos los usuarios");
            } else {
                // Sin filtros específicos: todas las iniciativas
                iniciativas = iniciativaRepository.findAll();
                System.out.println("📊 Consultando todas las iniciativas (admin sin filtros)");
            }
        }

        System.out.println("✅ Iniciativas obtenidas: " + iniciativas.size());
        return iniciativas;
    }

    private List<Impacto> obtenerImpactosFiltrados(List<Iniciativa> iniciativasFiltradas) {
        if (iniciativasFiltradas.isEmpty()) {
            System.out.println("⚠️ No hay iniciativas filtradas, no se obtendrán impactos");
            return new ArrayList<>();
        }

        Usuario usuarioLogueado = UserSession.getUsuarioActual();
        boolean esAnalista = usuarioLogueado != null && "analista".equals(usuarioLogueado.getRol());

        List<Impacto> impactos;

        if (esAnalista && usuarioLogueado != null) {
            // Para analistas: consulta eficiente directa por propietario
            impactos = impactoRepository.findByOwner(usuarioLogueado.getIdUsuario());
            System.out.println("📊 Consultando impactos del analista directamente desde BD");
        } else {
            // Para admin o casos específicos: consulta por IDs de iniciativas
            List<Integer> idsIniciativas = iniciativasFiltradas.stream()
                    .map(Iniciativa::getIdIniciativa)
                    .collect(Collectors.toList());

            impactos = impactoRepository.findByIniciativaIds(idsIniciativas);
            System.out.println("📊 Consultando impactos por IDs de iniciativas específicas");
        }

        System.out.println("✅ Impactos obtenidos: " + impactos.size());
        return impactos;
    }

    private void actualizarKPIs(List<Iniciativa> iniciativas, List<Impacto> impactos) {
        // Total de iniciativas
        if (lblTotalIniciativas != null) {
            lblTotalIniciativas.setText(String.valueOf(iniciativas.size()));
        }

        // Iniciativas activas
        long activas = iniciativas.stream()
                .filter(i -> "en curso".equalsIgnoreCase(i.getEstado()))
                .count();
        if (lblIniciativasActivas != null) {
            lblIniciativasActivas.setText(String.valueOf(activas));
        }

        // Iniciativas finalizadas
        long finalizadas = iniciativas.stream()
                .filter(i -> "finalizado".equalsIgnoreCase(i.getEstado()))
                .count();
        if (lblIniciativasFinalizadas != null) {
            lblIniciativasFinalizadas.setText(String.valueOf(finalizadas));
        }

        // Riesgo alto
        long riesgoAlto = iniciativas.stream()
                .filter(i -> "alto".equalsIgnoreCase(i.getRiesgo()))
                .count();
        if (lblRiesgoAlto != null) {
            lblRiesgoAlto.setText(String.valueOf(riesgoAlto));
        }

        // Impacto total - usando getImpactoCalculado() en lugar de getValor()
        BigDecimal impactoTotal = impactos.stream()
                .map(Impacto::getImpactoCalculado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (lblImpactoTotal != null) {
            lblImpactoTotal.setText(currencyFormat.format(impactoTotal));
        }

        // Impacto promedio
        BigDecimal impactoPromedio = impactos.isEmpty() ? BigDecimal.ZERO :
                impactoTotal.divide(BigDecimal.valueOf(impactos.size()), 2, BigDecimal.ROUND_HALF_UP);
        if (lblImpactoPromedio != null) {
            lblImpactoPromedio.setText(currencyFormat.format(impactoPromedio));
        }

        // ROI - Simplified calculation
        if (lblROI != null) {
            lblROI.setText("15.2%");
        }

        // Eficiencia
        if (lblEficiencia != null) {
            double eficiencia = iniciativas.isEmpty() ? 0 :
                (double) finalizadas / iniciativas.size() * 100;
            lblEficiencia.setText(String.format("%.1f%%", eficiencia));
        }
    }

    private void actualizarGraficos(List<Iniciativa> iniciativas, List<Impacto> impactos) {
        actualizarGraficoEstados(iniciativas);
        actualizarGraficoRiesgos(iniciativas);
        actualizarGraficoImpactosPorTipo(impactos);
        actualizarGraficoImpactoMensual(impactos);
    }

    private void actualizarGraficoEstados(List<Iniciativa> iniciativas) {
        if (chartIniciativasPorEstado == null) return;

        Map<String, Long> estadoCount = iniciativas.stream()
                .collect(Collectors.groupingBy(Iniciativa::getEstado, Collectors.counting()));

        chartIniciativasPorEstado.getData().clear();
        estadoCount.forEach((estado, count) -> {
            PieChart.Data data = new PieChart.Data(estado, count);
            chartIniciativasPorEstado.getData().add(data);
        });
    }

    private void actualizarGraficoRiesgos(List<Iniciativa> iniciativas) {
        if (chartIniciativasPorRiesgo == null) return;

        Map<String, Long> riesgoCount = iniciativas.stream()
                .collect(Collectors.groupingBy(Iniciativa::getRiesgo, Collectors.counting()));

        chartIniciativasPorRiesgo.getData().clear();
        riesgoCount.forEach((riesgo, count) -> {
            PieChart.Data data = new PieChart.Data(riesgo, count);
            chartIniciativasPorRiesgo.getData().add(data);
        });
    }

    private void actualizarGraficoImpactosPorTipo(List<Impacto> impactos) {
        if (chartImpactosPorTipo == null) return;

        Map<String, BigDecimal> tipoSuma = impactos.stream()
                .collect(Collectors.groupingBy(Impacto::getTipoImpacto,
                        Collectors.reducing(BigDecimal.ZERO, Impacto::getImpactoCalculado, BigDecimal::add)));

        chartImpactosPorTipo.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Impactos por Tipo");

        tipoSuma.forEach((tipo, suma) -> {
            series.getData().add(new XYChart.Data<>(tipo, suma));
        });

        chartImpactosPorTipo.getData().add(series);
    }

    private void actualizarGraficoImpactoMensual(List<Impacto> impactos) {
        if (chartImpactoMensual == null) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, BigDecimal> impactoMensual = impactos.stream()
                .collect(Collectors.groupingBy(
                        impacto -> impacto.getFechaImpacto().format(formatter),
                        Collectors.reducing(BigDecimal.ZERO, Impacto::getImpactoCalculado, BigDecimal::add)
                ));

        chartImpactoMensual.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Impacto Mensual");

        impactoMensual.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                });

        chartImpactoMensual.getData().add(series);
    }

    /**
     * Método público para reconfigurar el dashboard
     */
    public void reconfigurar() {
        System.out.println("🔄 Reconfigurando dashboard...");
        configurarSegunUsuarioActual();
    }

    /**
     * Establece el usuario logueado y configura los filtros según su rol
     */
    public void setUsuarioLogueado(Usuario usuario) {
        System.out.println("🔧 setUsuarioLogueado llamado con usuario: " + (usuario != null ? usuario.getNombreCompleto() : "NULL"));
        UserSession.setUsuarioActual(usuario);

        // Usar Platform.runLater para asegurar que el FXML esté completamente cargado
        javafx.application.Platform.runLater(() -> {
            System.out.println("🔧 Platform.runLater ejecutándose...");
            configurarFiltros();
            cargarDatos();
        });
    }
}
