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

    // Paso 17: Método para construir y validar el Producto
    private Producto obtenerProductoFormulario() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();

        if (codigo.isEmpty()) {
            throw new IllegalArgumentException("El código es obligatorio.");
        }

        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        Categoria categoria = cmbCategoria.getSelectionModel().getSelectedItem();

        if (categoria == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoría.");
        }

        BigDecimal precio;

        try {
            precio = new BigDecimal(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio debe ser numérico.");
        }

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }

        int existencia;

        try {
            existencia = Integer.parseInt(txtExistencia.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La existencia debe ser un número entero.");
        }

        if (existencia < 0) {
            throw new IllegalArgumentException("La existencia no puede ser negativa.");
        }

        return new Producto(
                null,
                codigo,
                nombre,
                categoria,
                precio,
                existencia,
                rutaImagenSeleccionada,
                chkActivo.isSelected()
        );
    }

    // Paso 18: Controlar excepciones al guardar Producto
    @FXML
    private void guardarProducto() {
        try {
            Producto producto = obtenerProductoFormulario();

            if (productoDAO.existeCodigo(producto.getCodigo())) {
                mostrarAdvertencia(
                        "Código duplicado",
                        "Ya existe un producto con ese código."
                );
                return;
            }

            productoDAO.guardar(producto);

            mostrarExito(
                    "Producto registrado",
                    "La información fue almacenada correctamente."
            );

            cargarProductos();
            limpiarFormulario();

        } catch (IllegalArgumentException e) {
            mostrarAdvertencia(
                    "Validación",
                    e.getMessage()
            );

        } catch (SQLException e) {
            mostrarError(
                    "Error de base de datos",
                    "No fue posible registrar el producto."
            );
        }
    }

    // Alias para el evento FXML si tu archivo FXML llama a "guardar"
    @FXML
    private void guardar() {
        guardarProducto();
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
    private void actualizar() {
        if (productoSeleccionado == null) {
            mostrarAdvertencia("Validación", "Debe seleccionar un producto de la tabla para actualizar.");
            return;
        }

        try {
            Producto datosNuevo = obtenerProductoFormulario();

            if (productoDAO.existeCodigoExcluyendoId(datosNuevo.getCodigo(), productoSeleccionado.getId())) {
                mostrarAdvertencia("Código duplicado", "El código '" + datosNuevo.getCodigo() + "' ya pertenece a otro producto.");
                return;
            }

            productoSeleccionado.setCodigo(datosNuevo.getCodigo());
            productoSeleccionado.setNombre(datosNuevo.getNombre());
            productoSeleccionado.setCategoria(datosNuevo.getCategoria());
            productoSeleccionado.setPrecioVenta(datosNuevo.getPrecioVenta());
            productoSeleccionado.setExistencia(datosNuevo.getExistencia());
            productoSeleccionado.setRutaImagen(datosNuevo.getRutaImagen());
            productoSeleccionado.setActivo(datosNuevo.isActivo());

            if (productoDAO.actualizar(productoSeleccionado)) {
                mostrarExito("Producto actualizado", "La información fue actualizada correctamente.");
                cargarProductos();
                limpiarFormulario();
            } else {
                mostrarError("Error de base de datos", "No fue posible actualizar el producto.");
            }
        } catch (IllegalArgumentException e) {
            mostrarAdvertencia("Validación", e.getMessage());
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible actualizar el producto.");
        }
    }

    @FXML
    private void eliminar() {
        if (productoSeleccionado == null) {
            mostrarAdvertencia("Validación", "Debe seleccionar un producto de la tabla para eliminar.");
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
                    mostrarExito("Producto eliminado", "El producto se eliminó correctamente.");
                    cargarProductos();
                    limpiarFormulario();
                } else {
                    mostrarError("Error de base de datos", "No fue posible eliminar el producto.");
                }
            } catch (SQLException e) {
                mostrarError("Error de base de datos", "No fue posible eliminar el producto debido a restricciones en la BD.");
            }
        }
    }

    @FXML
    private void limpiarFormulario() {
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