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
        cargarCategorias();

        tblCategorias.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                categoriaSeleccionada = newSelection;
                txtNombre.setText(newSelection.getNombre());
                chkActivo.setSelected(newSelection.isActiva());
            }
        });
    }

    private void cargarCategorias() {
        listaCategorias.clear();
        listaCategorias.addAll(categoriaDAO.listar());
    }

    // Validaciones del módulo Categoria
    private boolean validarCategoria(Integer idExcluir) {
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            mostrarError("Validación", "El nombre de la categoría es obligatorio.");
            txtNombre.requestFocus();
            return false;
        }

        try {
            boolean duplicado = (idExcluir == null)
                    ? categoriaDAO.existeNombre(nombre)
                    : categoriaDAO.existeNombreExcluyendoId(nombre, idExcluir);

            if (duplicado) {
                mostrarError("Validación", "Ya existe una categoría registrada con el nombre: " + nombre);
                txtNombre.requestFocus();
                return false;
            }
        } catch (SQLException e) {
            mostrarError("Error DB", "Error al verificar duplicados en la base de datos: " + e.getMessage());
            return false;
        }

        return true;
    }

    @FXML
    private void guardar() {
        if (validarCategoria(null)) {
            Categoria nueva = new Categoria(null, txtNombre.getText().trim(), chkActivo.isSelected());
            try {
                if (categoriaDAO.guardar(nueva)) {
                    mostrarInfo("Éxito", "Categoría guardada con éxito.");
                    cargarCategorias();
                    limpiar();
                } else {
                    mostrarError("Error", "No se pudo guardar la categoría en la base de datos.");
                }
            } catch (Exception e) {
                mostrarError("Error DB", "Error de base de datos al guardar: " + e.getMessage());
            }
        }
    }

    // 6. Validación para actualizar Categoria
    @FXML
    private void actualizar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        // Verificación de selección
        if (seleccionada == null) {
            mostrarAdvertencia(
                    "Seleccione una categoría",
                    "Debe seleccionar la categoría que desea actualizar."
            );
            return;
        }

        // Validación de nombre previa al UPDATE
        if (validarCategoria(seleccionada.getId())) {
            seleccionada.setNombre(txtNombre.getText().trim());
            seleccionada.setActiva(chkActivo.isSelected());

            try {
                if (categoriaDAO.actualizar(seleccionada)) {
                    mostrarInfo("Éxito", "Categoría actualizada con éxito.");
                    cargarCategorias();
                    limpiar();
                } else {
                    mostrarError("Error", "No se pudo actualizar la categoría.");
                }
            } catch (Exception e) {
                mostrarError("Error DB", "Error de base de datos al actualizar: " + e.getMessage());
            }
        }
    }

    // 7. Validar eliminación de Categoria
    @FXML
    private void eliminar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAdvertencia(
                    "Seleccione una categoría",
                    "Debe seleccionar la categoría que desea eliminar."
            );
            return;
        }

        try {
            // Verificar si existen productos asociados antes de proceder
            if (categoriaDAO.tieneProductos(seleccionada.getId())) {
                mostrarAdvertencia(
                        "Operación Cancelada",
                        "No se puede eliminar la categoría '" + seleccionada.getNombre() + "' porque tiene productos asociados."
                );
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar eliminación");
            confirm.setHeaderText(null);
            confirm.setContentText("¿Está seguro de eliminar la categoría '" + seleccionada.getNombre() + "'?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (categoriaDAO.eliminar(seleccionada.getId())) {
                    mostrarInfo("Éxito", "Categoría eliminada con éxito.");
                    cargarCategorias();
                    limpiar();
                } else {
                    mostrarError("Error", "No se pudo eliminar la categoría.");
                }
            }
        } catch (SQLException e) {
            mostrarError("Error DB", "Error al verificar la eliminación en la base de datos: " + e.getMessage());
        }
    }

    @FXML
    private void limpiar() {
        txtNombre.clear();
        chkActivo.setSelected(true);
        categoriaSeleccionada = null;
        tblCategorias.getSelectionModel().clearSelection();
    }

    private void mostrarAdvertencia(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}