package ni.edu.uam.facturacionapp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MenuPrincipalController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Button btnProductos;

    @FXML
    private Button btnCategorias;

    @FXML
    private Button btnSalir;

    @FXML
    private void abrirProductos() {
        cargarVistaCentral("/ni/edu/uam/facturacionapp/fxml/producto-view.fxml");
    }

    @FXML
    private void abrirCategorias() {
        cargarVistaCentral("/ni/edu/uam/facturacionapp/fxml/categoria-view.fxml");
    }

    /**
     * Carga dinámicamente cualquier vista dentro del panel central
     */
    private void cargarVistaCentral(String rutaFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            Node vista = loader.load();
            mainBorderPane.setCenter(vista);
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al cargar la vista: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void salir() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea salir del sistema?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle("Confirmar salida");

        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            Platform.exit();
        }
    }
}