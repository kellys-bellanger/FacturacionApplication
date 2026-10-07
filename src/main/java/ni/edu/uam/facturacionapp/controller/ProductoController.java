package ni.edu.uam.facturacionapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import ni.edu.uam.facturacionapp.dao.CategoriaDAO;
import ni.edu.uam.facturacionapp.dao.ProductoDAO;
import ni.edu.uam.facturacionapp.model.Categoria;
import ni.edu.uam.facturacionapp.model.Producto;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public class ProductoController {

    @FXML private TextField txtCodigo, txtNombre, txtPrecio, txtExistencia, txtBuscar;
    @FXML private ComboBox<Categoria> cmbCategoria, cmbFiltroCategoria;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private CheckBox chkActivo;
    @FXML private TableView<Producto> tblProductos;

    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Categoria> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, Boolean> colActivo;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private FilteredList<Producto> productosFiltrados;
    private Producto productoSeleccionado;
    private String rutaImagenSeleccionada = null;

    @FXML
    private void initialize() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        cargarCategorias();
        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Todos", "Activos", "Inactivos"));
        cmbFiltroEstado.setValue("Todos");

        productosFiltrados = new FilteredList<>(listaProductos, p -> true);
        tblProductos.setItems(productosFiltrados);

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cmbFiltroEstado.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cmbFiltroCategoria.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                productoSeleccionado = newVal;
                txtCodigo.setText(newVal.getCodigo());
                txtNombre.setText(newVal.getNombre());
                txtPrecio.setText(newVal.getPrecioVenta().toString());
                txtExistencia.setText(String.valueOf(newVal.getExistencia()));
                chkActivo.setSelected(newVal.isActivo());
                cmbCategoria.setValue(newVal.getCategoria());
                rutaImagenSeleccionada = newVal.getRutaImagen();
            }
        });

        cargarProductos();
        chkActivo.setSelected(true);
    }

    private void cargarCategorias() {
        ObservableList<Categoria> cats = FXCollections.observableArrayList(categoriaDAO.listar());
        cmbCategoria.setItems(cats);

        ObservableList<Categoria> catsFiltro = FXCollections.observableArrayList(cats);
        Categoria catTodas = new Categoria(0, "Todas", true);
        catsFiltro.add(0, catTodas);
        cmbFiltroCategoria.setItems(catsFiltro);
        cmbFiltroCategoria.setValue(catTodas);
    }

    private void cargarProductos() {
        listaProductos.clear();
        listaProductos.addAll(productoDAO.listar());
    }

    private void aplicarFiltros() {
        String textoBusqueda = txtBuscar.getText() == null ? "" : txtBuscar.getText().toLowerCase().trim();
        String estadoFiltro = cmbFiltroEstado.getValue();
        Categoria catFiltro = cmbFiltroCategoria.getValue();

        productosFiltrados.setPredicate(producto -> {
            boolean coincideTexto = textoBusqueda.isEmpty() ||
                    producto.getCodigo().toLowerCase().contains(textoBusqueda) ||
                    producto.getNombre().toLowerCase().contains(textoBusqueda) ||
                    (producto.getCategoria() != null && producto.getCategoria().getNombre().toLowerCase().contains(textoBusqueda));

            boolean coincideEstado = true;
            if ("Activos".equals(estadoFiltro)) coincideEstado = producto.isActivo();
            else if ("Inactivos".equals(estadoFiltro)) coincideEstado = !producto.isActivo();

            boolean coincideCategoria = true;
            if (catFiltro != null && catFiltro.getId() != 0) {
                coincideCategoria = producto.getCategoria() != null && producto.getCategoria().getId().equals(catFiltro.getId());
            }

            return coincideTexto && coincideEstado && coincideCategoria;
        });
    }

    // Validaciones del módulo Producto
    private boolean esFormularioValido(Integer idExcluir) {
        String codigo = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();

        // Paso 9: Validar código obligatorio
        if (codigo.isEmpty()) {
            mostrarError(
                    "Validación",
                    "El código del producto es obligatorio."
            );
            txtCodigo.requestFocus();
            return false;
        }

        // Paso 14: Controlar códigos duplicados desde la base de datos
        try {
            boolean duplicado = (idExcluir == null)
                    ? productoDAO.existeCodigo(codigo)
                    : productoDAO.existeCodigoExcluyendoId(codigo, idExcluir);

            if (duplicado) {
                mostrarError(
                        "Código duplicado",
                        "El código del producto '" + codigo + "' ya está registrado. Ingrese uno diferente."
                );
                txtCodigo.requestFocus();
                return false;
            }
        } catch (SQLException e) {
            mostrarError("Error de BD", "Error al verificar el código en la base de datos: " + e.getMessage());
            return false;
        }

        // Paso 9: Validar nombre obligatorio
        if (nombre.isEmpty()) {
            mostrarError(
                    "Validación",
                    "El nombre del producto es obligatorio."
            );
            txtNombre.requestFocus();
            return false;
        }

        // Paso 10: Validar la categoría del Producto
        Categoria categoria = cmbCategoria.getSelectionModel().getSelectedItem();
        if (categoria == null) {
            mostrarError(
                    "Validación",
                    "Debe seleccionar una categoría."
            );
            cmbCategoria.requestFocus();
            return false;
        }

        // Pasos 11 y 12: Validar precio de venta
        BigDecimal precio;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            mostrarError(
                    "Precio incorrecto",
                    "El precio debe contener únicamente valores numéricos."
            );
            txtPrecio.requestFocus();
            return false;
        }

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            mostrarError(
                    "Precio incorrecto",
                    "El precio de venta debe ser mayor que cero."
            );
            txtPrecio.requestFocus();
            return false;
        }

        // Paso 13: Validar existencia (número entero y no negativo)
        try {
            int existencia = Integer.parseInt(txtExistencia.getText().trim());

            if (existencia < 0) {
                mostrarError(
                        "Existencia incorrecta",
                        "La existencia no puede ser negativa."
                );
                txtExistencia.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarError(
                    "Existencia incorrecta",
                    "La existencia debe ser un número entero."
            );
            txtExistencia.requestFocus();
            return false;
        }

        return true;
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen del Producto");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de Imagen", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File archivoSeleccionado = fileChooser.showOpenDialog(txtCodigo.getScene().getWindow());

        if (archivoSeleccionado != null) {
            rutaImagenSeleccionada = archivoSeleccionado.getAbsolutePath();
        }
    }

    @FXML
    private void guardar() {
        if (esFormularioValido(null)) {
            try {
                BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
                int existencia = Integer.parseInt(txtExistencia.getText().trim());

                Producto nuevo = new Producto(
                        null,
                        txtCodigo.getText().trim(),
                        txtNombre.getText().trim(),
                        cmbCategoria.getSelectionModel().getSelectedItem(),
                        precio,
                        existencia,
                        rutaImagenSeleccionada,
                        chkActivo.isSelected()
                );

                if (productoDAO.guardar(nuevo)) {
                    mensaje(Alert.AlertType.INFORMATION, "Producto registrado con éxito.");
                    cargarProductos();
                    limpiar();
                } else {
                    mostrarError("Error", "No se pudo registrar el producto en la base de datos.");
                }
            } catch (Exception e) {
                mostrarError("Error", "Error de base de datos al guardar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void actualizar() {
        if (productoSeleccionado == null) {
            mostrarError("Validación", "Debe seleccionar un producto de la tabla para actualizar.");
            return;
        }

        if (esFormularioValido(productoSeleccionado.getId())) {
            try {
                productoSeleccionado.setCodigo(txtCodigo.getText().trim());
                productoSeleccionado.setNombre(txtNombre.getText().trim());
                productoSeleccionado.setCategoria(cmbCategoria.getSelectionModel().getSelectedItem());
                productoSeleccionado.setPrecioVenta(new BigDecimal(txtPrecio.getText().trim()));
                productoSeleccionado.setExistencia(Integer.parseInt(txtExistencia.getText().trim()));
                productoSeleccionado.setRutaImagen(rutaImagenSeleccionada);
                productoSeleccionado.setActivo(chkActivo.isSelected());

                if (productoDAO.actualizar(productoSeleccionado)) {
                    mensaje(Alert.AlertType.INFORMATION, "Producto actualizado con éxito.");
                    cargarProductos();
                    limpiar();
                } else {
                    mostrarError("Error", "No se pudo actualizar el producto.");
                }
            } catch (Exception e) {
                mostrarError("Error", "Error de base de datos al actualizar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void eliminar() {
        if (productoSeleccionado == null) {
            mostrarError("Validación", "Debe seleccionar un producto de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de eliminar el producto '" + productoSeleccionado.getNombre() + "'?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (productoDAO.eliminar(productoSeleccionado.getId())) {
                    mensaje(Alert.AlertType.INFORMATION, "Producto eliminado correctamente.");
                    cargarProductos();
                    limpiar();
                } else {
                    mostrarError("Error", "No se pudo eliminar el producto.");
                }
            } catch (Exception e) {
                mostrarError("Error", "No se puede eliminar el producto debido a restricciones en la base de datos.");
            }
        }
    }

    @FXML
    private void limpiar() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtExistencia.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);
        productoSeleccionado = null;
        rutaImagenSeleccionada = null;
        tblProductos.getSelectionModel().clearSelection();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Módulo de Productos");
        alert.setHeaderText(null);
        alert.setContentText(texto);
        alert.showAndWait();
    }
}