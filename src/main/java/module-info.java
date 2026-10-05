module ni.edu.uam.facturacionapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires static lombok;

    // Exporta el paquete de la aplicación para que JavaFX pueda ejecutarla
    exports ni.edu.uam.facturacionapp.application;

    // Permisos para controladores y modelos
    opens ni.edu.uam.facturacionapp.controller to javafx.fxml;
    opens ni.edu.uam.facturacionapp.model to javafx.base;

    exports ni.edu.uam.facturacionapp;
}