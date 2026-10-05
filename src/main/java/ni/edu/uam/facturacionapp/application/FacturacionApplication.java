package ni.edu.uam.facturacionapp.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FacturacionApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ni/edu/uam/facturacionapp/fxml/menu-principal.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("Sistema de Facturación");
        stage.setMaximized(true); // <-- Hace que ocupe toda la pantalla
        stage.setScene(scene);
        stage.show();
    }
}