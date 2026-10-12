
package ni.edu.uam.practicahilosjavafx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class ProcesadorController {

    // Controles de clientes
    @FXML private Label lblEstadoClientes;
    @FXML private Label lblPorcentajeClientes;
    @FXML private ProgressBar barraClientes;
    @FXML private Button btnIniciarClientes;
    @FXML private Button btnDetenerClientes;

    // Controles de productos
    @FXML private Label lblEstadoProductos;
    @FXML private Label lblPorcentajeProductos;
    @FXML private ProgressBar barraProductos;
    @FXML private Button btnIniciarProductos;
    @FXML private Button btnDetenerProductos;

    // Controles generales
    @FXML private Button btnIniciarAmbos;
    @FXML private Button btnReiniciarTodo;

    // Hilos independientes
    private Thread hiloClientes;
    private Thread hiloProductos;

    // Estados de ejecución
    private boolean clientesActivos = false;
    private boolean productosActivos = false;

    private boolean clientesDeteniendose = false;
    private boolean productosDeteniendose = false;

    @FXML
    private void initialize() {
        actualizarBotones();
    }

    // INICIAR CLIENTES
    @FXML
    private void iniciarClientes() {

        if (clientesActivos) {
            return;
        }

        clientesActivos = true;
        clientesDeteniendose = false;

        prepararInicio(
                barraClientes,
                lblPorcentajeClientes,
                lblEstadoClientes,
                "Procesando clientes..."
        );

        actualizarBotones();

        Runnable tarea = () -> procesarRegistros(
                100,
                100,
                barraClientes,
                lblPorcentajeClientes,
                lblEstadoClientes,
                () -> {
                    clientesActivos = false;
                    clientesDeteniendose = false;
                    actualizarBotones();
                }
        );

        hiloClientes = new Thread(tarea, "Hilo-Clientes");
        hiloClientes.setDaemon(true);
        hiloClientes.start();
    }

    // INICIAR PRODUCTOS
    @FXML
    private void iniciarProductos() {

        if (productosActivos) {
            return;
        }

        productosActivos = true;
        productosDeteniendose = false;

        prepararInicio(
                barraProductos,
                lblPorcentajeProductos,
                lblEstadoProductos,
                "Procesando productos..."
        );

        actualizarBotones();

        Runnable tarea = () -> procesarRegistros(
                50,
                150,
                barraProductos,
                lblPorcentajeProductos,
                lblEstadoProductos,
                () -> {
                    productosActivos = false;
                    productosDeteniendose = false;
                    actualizarBotones();
                }
        );

        hiloProductos = new Thread(tarea, "Hilo-Productos");
        hiloProductos.setDaemon(true);
        hiloProductos.start();
    }

    // PREPARAR LA INTERFAZ
    private void prepararInicio(
            ProgressBar barra,
            Label porcentaje,
            Label estado,
            String mensaje) {

        barra.setProgress(0);
        porcentaje.setText("0%");
        estado.setText(mensaje);
    }

    // PROCESAMIENTO COMPARTIDO
    private void procesarRegistros(
            int total,
            int pausaMs,
            ProgressBar barra,
            Label porcentaje,
            Label estado,
            Runnable alFinalizar) {

        boolean detenido = false;

        try {

            for (int i = 1; i <= total; i++) {

                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }

                Thread.sleep(pausaMs);

                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }

                final int avance = i;

                // Actualizar controles desde el hilo JavaFX
                Platform.runLater(() -> {

                    double progreso = (double) avance / total;

                    barra.setProgress(progreso);

                    int porcentajeActual =
                            (int) Math.round(progreso * 100);

                    porcentaje.setText(porcentajeActual + "%");
                });
            }

        } catch (InterruptedException e) {

            detenido = true;
            Thread.currentThread().interrupt();
        }

        final boolean fueDetenido = detenido;

        Platform.runLater(() -> {

            if (fueDetenido) {
                estado.setText("Proceso detenido");
            } else {
                estado.setText("Proceso finalizado");
            }

            alFinalizar.run();
        });
    }

    // DETENER CLIENTES
    @FXML
    private void detenerClientes() {

        if (clientesActivos
                && !clientesDeteniendose
                && hiloClientes != null) {

            clientesDeteniendose = true;

            lblEstadoClientes.setText(
                    "Solicitando detención..."
            );

            actualizarBotones();
            hiloClientes.interrupt();
        }
    }

    // DETENER PRODUCTOS
    @FXML
    private void detenerProductos() {

        if (productosActivos
                && !productosDeteniendose
                && hiloProductos != null) {

            productosDeteniendose = true;

            lblEstadoProductos.setText(
                    "Solicitando detención..."
            );

            actualizarBotones();
            hiloProductos.interrupt();
        }
    }

    // INICIAR AMBOS PROCESOS
    @FXML
    private void iniciarAmbos() {

        if (!clientesActivos) {
            iniciarClientes();
        }

        if (!productosActivos) {
            iniciarProductos();
        }
    }

    // REINICIAR LA INTERFAZ
    @FXML
    private void reiniciarTodo() {

        if (clientesActivos || productosActivos) {
            return;
        }

        barraClientes.setProgress(0);
        barraProductos.setProgress(0);

        lblPorcentajeClientes.setText("0%");
        lblPorcentajeProductos.setText("0%");

        lblEstadoClientes.setText("Esperando inicio");
        lblEstadoProductos.setText("Esperando inicio");

        actualizarBotones();
    }

    // ACTUALIZAR BOTONES
    private void actualizarBotones() {

        btnIniciarClientes.setDisable(clientesActivos);

        btnDetenerClientes.setDisable(
                !clientesActivos || clientesDeteniendose
        );

        btnIniciarProductos.setDisable(productosActivos);

        btnDetenerProductos.setDisable(
                !productosActivos || productosDeteniendose
        );

        btnIniciarAmbos.setDisable(
                clientesActivos && productosActivos
        );

        btnReiniciarTodo.setDisable(
                clientesActivos || productosActivos
        );
    }

    // DETENER LOS HILOS AL CERRAR
    public void detenerAlCerrar() {

        if (hiloClientes != null && hiloClientes.isAlive()) {
            hiloClientes.interrupt();
        }

        if (hiloProductos != null && hiloProductos.isAlive()) {
            hiloProductos.interrupt();
        }
    }
}
