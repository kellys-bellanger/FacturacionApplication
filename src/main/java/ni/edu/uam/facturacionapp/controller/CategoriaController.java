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

    private boolean esFormularioValido(Integer idExcluir) {
        if (txtNombre.getText() == null || txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "El campo Nombre es obligatorio.");
            return false;
        }

        String nombreIngresado = txtNombre.getText().trim();
        boolean duplicado = listaCategorias.stream()
                .anyMatch(c -> !c.getId().equals(idExcluir) && c.getNombre().equalsIgnoreCase(nombreIngresado));

        if (duplicado) {
            mensaje(Alert.AlertType.WARNING, "Ya existe una categoría registrada con el nombre: " + nombreIngresado);
            return false;
        }

        return true;
    }

    @FXML
    private void guardar() {
        if (esFormularioValido(null)) {
            Categoria nueva = new Categoria(null, txtNombre.getText().trim(), chkActivo.isSelected());
            try {
                if (categoriaDAO.guardar(nueva)) {
                    mensaje(Alert.AlertType.INFORMATION, "Categoría guardada con éxito.");
                    cargarCategorias();
                    limpiar();
                } else {
                    mensaje(Alert.AlertType.ERROR, "No se pudo guardar la categoría en la base de datos.");
                }
            } catch (Exception e) {
                mensaje(Alert.AlertType.ERROR, "Error de base de datos al guardar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void actualizar() {
        if (categoriaSeleccionada == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione una categoría de la tabla.");
            return;
        }

        if (esFormularioValido(categoriaSeleccionada.getId())) {
            categoriaSeleccionada.setNombre(txtNombre.getText().trim());
            categoriaSeleccionada.setActiva(chkActivo.isSelected());

            try {
                if (categoriaDAO.actualizar(categoriaSeleccionada)) {
                    mensaje(Alert.AlertType.INFORMATION, "Categoría actualizada con éxito.");
                    cargarCategorias();
                    limpiar();
                } else {
                    mensaje(Alert.AlertType.ERROR, "No se pudo actualizar la categoría.");
                }
            } catch (Exception e) {
                mensaje(Alert.AlertType.ERROR, "Error de base de datos al actualizar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void eliminar() {
        if (categoriaSeleccionada == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione una categoría de la tabla.");
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
                    mensaje(Alert.AlertType.INFORMATION, "Categoría eliminada con éxito.");
                    cargarCategorias();
                    limpiar();
                } else {
                    mensaje(Alert.AlertType.ERROR, "No se pudo eliminar la categoría.");
                }
            } catch (Exception e) {
                mensaje(Alert.AlertType.ERROR, "No se puede eliminar la categoría porque está asociada a uno o más productos.");
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

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}