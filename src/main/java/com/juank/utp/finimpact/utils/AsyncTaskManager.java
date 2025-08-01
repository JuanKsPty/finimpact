package com.juank.utp.finimpact.utils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Label;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Gestor de tareas asíncronas para mejorar el rendimiento de la aplicación
 */
public class AsyncTaskManager {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    /**
     * Ejecuta una tarea en background con indicador de carga
     */
    public static <T> void executeAsync(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Exception> onError,
            ProgressIndicator progressIndicator,
            Label statusLabel) {

        // Mostrar indicador de carga
        Platform.runLater(() -> {
            if (progressIndicator != null) {
                progressIndicator.setVisible(true);
            }
            if (statusLabel != null) {
                statusLabel.setText("Cargando...");
            }
        });

        Task<T> task = new Task<T>() {
            @Override
            protected T call() throws Exception {
                return backgroundTask.get();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    if (statusLabel != null) {
                        statusLabel.setText("Datos cargados");
                    }
                    onSuccess.accept(getValue());
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    if (statusLabel != null) {
                        statusLabel.setText("Error al cargar datos");
                    }
                    onError.accept(new Exception(getException()));
                });
            }
        };

        executor.submit(task);
    }

    /**
     * Ejecuta una tarea simple en background sin indicadores
     */
    public static <T> void executeAsync(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Exception> onError) {

        executeAsync(backgroundTask, onSuccess, onError, null, null);
    }

    /**
     * Ejecuta una tarea en background con mensaje personalizado
     */
    public static <T> void executeAsyncWithMessage(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Exception> onError,
            ProgressIndicator progressIndicator,
            Label statusLabel,
            String loadingMessage,
            String successMessage) {

        // Mostrar indicador de carga
        Platform.runLater(() -> {
            if (progressIndicator != null) {
                progressIndicator.setVisible(true);
            }
            if (statusLabel != null) {
                statusLabel.setText(loadingMessage);
            }
        });

        Task<T> task = new Task<T>() {
            @Override
            protected T call() throws Exception {
                return backgroundTask.get();
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    if (statusLabel != null) {
                        statusLabel.setText(successMessage);
                    }
                    onSuccess.accept(getValue());
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    if (statusLabel != null) {
                        statusLabel.setText("Error al cargar datos");
                    }
                    onError.accept(new Exception(getException()));
                });
            }
        };

        executor.submit(task);
    }

    /**
     * Cierra el executor al cerrar la aplicación
     */
    public static void shutdown() {
        executor.shutdown();
    }
}
