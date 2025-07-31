package com.juank.utp.finimpact.controller;

import com.juank.utp.finimpact.model.Impacto;
import com.juank.utp.finimpact.model.Iniciativa;
import com.juank.utp.finimpact.repository.ImpactoRepository;
import com.juank.utp.finimpact.repository.IniciativaRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

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
    private NumberFormat currencyFormat;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        iniciativaRepository = new IniciativaRepository();
        impactoRepository = new ImpactoRepository();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

        cargarDatos();
    }

    @FXML
    private void actualizarDashboard() {
        cargarDatos();
    }

    private void cargarDatos() {
        try {
            List<Iniciativa> iniciativas = iniciativaRepository.findAll();
            List<Impacto> impactos = impactoRepository.findAll();

            cargarChartIniciativasPorEstado(iniciativas);
            cargarChartImpactosPorTipo(impactos);
            cargarChartImpactoMensual(impactos);
            cargarChartIniciativasPorRiesgo(iniciativas);
            cargarKPIs(iniciativas, impactos);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarChartIniciativasPorEstado(List<Iniciativa> iniciativas) {
        Map<String, Long> estadoCount = iniciativas.stream()
                .collect(Collectors.groupingBy(Iniciativa::getEstado, Collectors.counting()));

        chartIniciativasPorEstado.setData(FXCollections.observableArrayList(
                estadoCount.entrySet().stream()
                        .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())
        ));
    }

    private void cargarChartImpactosPorTipo(List<Impacto> impactos) {
        Map<String, BigDecimal> tipoSum = impactos.stream()
                .collect(Collectors.groupingBy(
                        Impacto::getTipoImpacto,
                        Collectors.reducing(BigDecimal.ZERO,
                                impacto -> impacto.getImpacto().multiply(BigDecimal.valueOf(impacto.getMultiplicador())),
                                BigDecimal::add)
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Impacto por Tipo");

        tipoSum.forEach((tipo, suma) ->
                series.getData().add(new XYChart.Data<>(tipo, suma.doubleValue()))
        );

        chartImpactosPorTipo.setData(FXCollections.observableArrayList(series));
    }

    private void cargarChartImpactoMensual(List<Impacto> impactos) {
        Map<String, BigDecimal> impactoMensual = impactos.stream()
                .collect(Collectors.groupingBy(
                        impacto -> impacto.getFechaImpacto().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.reducing(BigDecimal.ZERO,
                                impacto -> impacto.getImpacto().multiply(BigDecimal.valueOf(impacto.getMultiplicador())),
                                BigDecimal::add)
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Impacto Mensual");

        // Ordenar por fecha
        impactoMensual.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry ->
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue().doubleValue()))
                );

        chartImpactoMensual.setData(FXCollections.observableArrayList(series));
    }

    private void cargarChartIniciativasPorRiesgo(List<Iniciativa> iniciativas) {
        Map<String, Long> riesgoCount = iniciativas.stream()
                .collect(Collectors.groupingBy(Iniciativa::getRiesgo, Collectors.counting()));

        chartIniciativasPorRiesgo.setData(FXCollections.observableArrayList(
                riesgoCount.entrySet().stream()
                        .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())
        ));
    }

    private void cargarKPIs(List<Iniciativa> iniciativas, List<Impacto> impactos) {
        // Total Iniciativas
        lblTotalIniciativas.setText(String.valueOf(iniciativas.size()));

        // Iniciativas Activas (en curso)
        long activas = iniciativas.stream()
                .filter(i -> "en curso".equalsIgnoreCase(i.getEstado()))
                .count();
        lblIniciativasActivas.setText(String.valueOf(activas));

        // Iniciativas Finalizadas
        long finalizadas = iniciativas.stream()
                .filter(i -> "finalizado".equalsIgnoreCase(i.getEstado()))
                .count();
        lblIniciativasFinalizadas.setText(String.valueOf(finalizadas));

        // Riesgo Alto
        long riesgoAlto = iniciativas.stream()
                .filter(i -> "alto".equalsIgnoreCase(i.getRiesgo()))
                .count();
        lblRiesgoAlto.setText(String.valueOf(riesgoAlto));

        // Impacto Total
        BigDecimal impactoTotal = impactos.stream()
                .map(i -> i.getImpacto().multiply(BigDecimal.valueOf(i.getMultiplicador())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblImpactoTotal.setText(currencyFormat.format(impactoTotal));

        // Impacto Promedio
        if (!impactos.isEmpty()) {
            BigDecimal impactoPromedio = impactoTotal.divide(BigDecimal.valueOf(impactos.size()), 2, BigDecimal.ROUND_HALF_UP);
            lblImpactoPromedio.setText(currencyFormat.format(impactoPromedio));
        } else {
            lblImpactoPromedio.setText("$0");
        }

        // ROI Proyectado (calculado como porcentaje de impactos positivos vs negativos)
        long positivos = impactos.stream()
                .filter(i -> i.getMultiplicador() > 0)
                .count();
        double roi = impactos.isEmpty() ? 0 : (positivos * 100.0) / impactos.size();
        lblROI.setText(String.format("%.1f%%", roi));

        // Eficiencia (porcentaje de iniciativas finalizadas)
        double eficiencia = iniciativas.isEmpty() ? 0 : (finalizadas * 100.0) / iniciativas.size();
        lblEficiencia.setText(String.format("%.1f%%", eficiencia));
    }
}
