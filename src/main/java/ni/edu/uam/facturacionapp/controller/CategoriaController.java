package ni.edu.uam.facturacionapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.facturacionapp.dao.CategoriaDAO;
import ni.edu.uam.facturacionapp.model.Categoria;

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

    // 4. Validaciones del módulo Categoria
    private boolean validarCategoria(Integer idExcluir) {
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();

        // El nombre no puede estar vacío ni contener únicamente espacios
        if (nombre.isEmpty()) {
            mostrarError("Validación", "El nombre de la categoría es obligatorio.");
            txtNombre.requestFocus();
            return false;
        }

        // No deberán existir categorías con el mismo nombre
        boolean duplicado = listaCategorias.stream()
                .anyMatch(c -> !c.getId().equals(idExcluir) && c.getNombre().equalsIgnoreCase(nombre));

        if (duplicado) {
            mostrarError("Validación", "Ya existe una categoría registrada con el nombre: " + nombre);
            txtNombre.requestFocus();
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

    @FXML
    private void actualizar() {
        // Para actualizar debe existir una categoría seleccionada
        if (categoriaSeleccionada == null) {
            mostrarError("Validación", "Debe seleccionar una categoría de la tabla para actualizar.");
            return;
        }

        if (validarCategoria(categoriaSeleccionada.getId())) {
            categoriaSeleccionada.setNombre(txtNombre.getText().trim());
            categoriaSeleccionada.setActiva(chkActivo.isSelected());

            try {
                if (categoriaDAO.actualizar(categoriaSeleccionada)) {
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

    @FXML
    private void eliminar() {
        // Para eliminar debe existir una categoría seleccionada
        if (categoriaSeleccionada == null) {
            mostrarError("Validación", "Debe seleccionar una categoría de la tabla para eliminar.");
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
                    mostrarInfo("Éxito", "Categoría eliminada con éxito.");
                    cargarCategorias();
                    limpiar();
                } else {
                    mostrarError("Error", "No se pudo eliminar la categoría.");
                }
            } catch (Exception e) {
                mostrarError("Integridad Referencial", "No se puede eliminar la categoría porque está asociada a uno o más productos.");
            }
        }
    }

    @FXML
    private void limpiar() {
        txtNombre.clear();
        chkActivo.setSelected(true);
        categoriaSeleccionada = null;
        tblCategorias.getSelectionModel().clearSelection();
    }

    private void mostrarError(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
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