package ni.edu.uam.facturacionapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.facturacionapp.dao.CategoriaDAO;
import ni.edu.uam.facturacionapp.model.Categoria;

import java.sql.SQLException;
import java.util.Optional;

public class CategoriaController {

    @FXML private TextField txtNombre;
    @FXML private CheckBox chkActivo;
    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, Boolean> colActivo;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();
    private Categoria categoriaSeleccionada;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activa"));

        tblCategorias.setItems(listaCategorias);

        tblCategorias.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                categoriaSeleccionada = newVal;
                txtNombre.setText(newVal.getNombre());
                chkActivo.setSelected(newVal.isActiva());
            }
        });

        cargarCategorias();
        chkActivo.setSelected(true);
    }

    private void cargarCategorias() {
        listaCategorias.clear();
        listaCategorias.addAll(categoriaDAO.listar());
    }

    private Categoria obtenerCategoriaFormulario() {
        String nombre = txtNombre.getText() != null ? txtNombre.getText().trim() : "";

        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        return new Categoria(null, nombre, chkActivo.isSelected());
    }

    @FXML
    private void guardarCategoria() {
        try {
            Categoria categoria = obtenerCategoriaFormulario();

            if (categoriaDAO.existeNombre(categoria.getNombre())) {
                mostrarAdvertencia("Advertencia", "Ya existe una categoría con ese nombre.");
                return;
            }

            categoriaDAO.guardar(categoria);
            mostrarExito("Éxito", "Categoría registrada correctamente.");
            cargarCategorias();
            limpiarFormulario();

        } catch (IllegalArgumentException e) {
            mostrarAdvertencia("Validación", e.getMessage());
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible completar la operación.");
        }
    }

    @FXML
    private void guardar() {
        guardarCategoria();
    }

    @FXML
    private void actualizarCategoria() {
        if (categoriaSeleccionada == null || categoriaSeleccionada.getId() == null) {
            mostrarAdvertencia("Selección requerida", "Debe seleccionar una categoría de la tabla para actualizar.");
            return;
        }

        try {
            Categoria datosNuevos = obtenerCategoriaFormulario();

            if (categoriaDAO.existeNombreExcluyendoId(datosNuevos.getNombre(), categoriaSeleccionada.getId())) {
                mostrarAdvertencia("Advertencia", "Ya existe otra categoría con el nombre '" + datosNuevos.getNombre() + "'.");
                return;
            }

            datosNuevos.setId(categoriaSeleccionada.getId());

            if (categoriaDAO.actualizar(datosNuevos)) {
                mostrarExito("Éxito", "Categoría actualizada correctamente.");
                cargarCategorias();
                limpiarFormulario();
            } else {
                mostrarError("Error de base de datos", "No fue posible completar la operación.");
            }

        } catch (IllegalArgumentException e) {
            mostrarAdvertencia("Validación", e.getMessage());
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible completar la operación.");
        }
    }

    @FXML
    private void actualizar() {
        actualizarCategoria();
    }

    @FXML
    private void eliminarCategoria() {
        if (categoriaSeleccionada == null || categoriaSeleccionada.getId() == null) {
            mostrarAdvertencia("Selección requerida", "Debe seleccionar una categoría para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de eliminar la categoría '" + categoriaSeleccionada.getNombre() + "'?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (categoriaDAO.eliminar(categoriaSeleccionada.getId())) {
                    mostrarExito("Éxito", "Categoría eliminada correctamente.");
                    cargarCategorias();
                    limpiarFormulario();
                } else {
                    mostrarError("Error de base de datos", "No fue posible completar la operación.");
                }
            } catch (SQLException e) {
                if (e.getErrorCode() == 547) {
                    mostrarError("Restricción de integridad", "No puede eliminar la categoría porque tiene productos asociados.");
                } else {
                    mostrarError("Error de base de datos", "No fue posible completar la operación.");
                }
            }
        }
    }

    @FXML
    private void eliminar() {
        eliminarCategoria();
    }

    @FXML
    private void limpiarFormulario() {
        txtNombre.clear();
        chkActivo.setSelected(true);
        categoriaSeleccionada = null;
        tblCategorias.getSelectionModel().clearSelection();
    }

    @FXML
    private void limpiar() {
        limpiarFormulario();
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}