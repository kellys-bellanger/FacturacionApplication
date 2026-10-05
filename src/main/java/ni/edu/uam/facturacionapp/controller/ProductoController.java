package ni.edu.uam.facturacionapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import ni.edu.uam.facturacionapp.dao.CategoriaDAO;
import ni.edu.uam.facturacionapp.dao.ProductoDAO;
import ni.edu.uam.facturacionapp.model.Categoria;
import ni.edu.uam.facturacionapp.model.Producto;

import java.io.File;
import java.math.BigDecimal;
import java.util.Optional;

public class ProductoController {

    @FXML private TextField txtCodigo, txtNombre, txtPrecio, txtExistencia, txtBuscar;
    @FXML private ComboBox<Categoria> cmbCategoria, cmbFiltroCategoria;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private CheckBox chkActivo;
    @FXML private ImageView imgProducto;
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
    private String rutaImagen;

    @FXML
    private void initialize() {
        // 1. Configurar Columnas
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // 2. Cargar combos de categorías y estados
        cargarCategorias();
        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Todos", "Activos", "Inactivos"));
        cmbFiltroEstado.setValue("Todos");

        // 3. Configurar FilteredList para Búsqueda y Filtros
        productosFiltrados = new FilteredList<>(listaProductos, p -> true);
        tblProductos.setItems(productosFiltrados);

        // 4. Listeners para filtros de búsqueda dinámica
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cmbFiltroEstado.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cmbFiltroCategoria.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        // 5. Listener de selección en el TableView
        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                productoSeleccionado = newVal;
                txtCodigo.setText(newVal.getCodigo());
                txtNombre.setText(newVal.getNombre());
                txtPrecio.setText(newVal.getPrecioVenta().toString());
                txtExistencia.setText(String.valueOf(newVal.getExistencia()));
                chkActivo.setSelected(newVal.isActivo());
                cmbCategoria.setValue(newVal.getCategoria());

                rutaImagen = newVal.getRutaImagen();
                if (rutaImagen != null && !rutaImagen.isBlank()) {
                    try {
                        imgProducto.setImage(new Image(rutaImagen));
                    } catch (Exception e) {
                        imgProducto.setImage(null);
                    }
                } else {
                    imgProducto.setImage(null);
                }
            }
        });

        cargarProductos();
        chkActivo.setSelected(true);
    }

    private void cargarCategorias() {
        ObservableList<Categoria> cats = FXCollections.observableArrayList(categoriaDAO.listar());
        cmbCategoria.setItems(cats);

        // Agregar opción de "Todas las categorías" para el filtro
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
            // Filtro por Búsqueda (Código, Nombre o Categoría)
            boolean coincideTexto = textoBusqueda.isEmpty() ||
                    producto.getCodigo().toLowerCase().contains(textoBusqueda) ||
                    producto.getNombre().toLowerCase().contains(textoBusqueda) ||
                    (producto.getCategoria() != null && producto.getCategoria().getNombre().toLowerCase().contains(textoBusqueda));

            // Filtro por Estado
            boolean coincideEstado = true;
            if ("Activos".equals(estadoFiltro)) coincideEstado = producto.isActivo();
            else if ("Inactivos".equals(estadoFiltro)) coincideEstado = !producto.isActivo();

            // Filtro por Categoría
            boolean coincideCategoria = true;
            if (catFiltro != null && catFiltro.getId() != 0) {
                coincideCategoria = producto.getCategoria() != null && producto.getCategoria().getId().equals(catFiltro.getId());
            }

            return coincideTexto && coincideEstado && coincideCategoria;
        });
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = chooser.showOpenDialog(txtCodigo.getScene().getWindow());
        if (archivo != null) {
            rutaImagen = archivo.toURI().toString();
            imgProducto.setImage(new Image(rutaImagen));
        }
    }

    @FXML
    private void guardar() {
        if (!validarFormulario()) return;

        String codigo = txtCodigo.getText().trim();

        // Validar código duplicado
        boolean existeCodigo = listaProductos.stream()
                .anyMatch(p -> p.getCodigo().equalsIgnoreCase(codigo));
        if (existeCodigo) {
            mensaje(Alert.AlertType.WARNING, "El código del producto ya existe. Utilice uno diferente.");
            return;
        }

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int existencia = Integer.parseInt(txtExistencia.getText().trim());

            Producto nuevo = new Producto(
                    null,
                    codigo,
                    txtNombre.getText().trim(),
                    cmbCategoria.getValue(),
                    precio,
                    existencia,
                    rutaImagen,
                    chkActivo.isSelected()
            );

            if (productoDAO.guardar(nuevo)) {
                mensaje(Alert.AlertType.INFORMATION, "Producto registrado con éxito.");
                cargarProductos();
                limpiar();
            } else {
                mensaje(Alert.AlertType.ERROR, "No se pudo guardar el producto.");
            }
        } catch (Exception e) {
            mensaje(Alert.AlertType.ERROR, "Error al procesar los datos: " + e.getMessage());
        }
    }

    @FXML
    private void actualizar() {
        if (productoSeleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto de la tabla para modificar.");
            return;
        }

        if (!validarFormulario()) return;

        String codigo = txtCodigo.getText().trim();

        // Validar que no duplique código de OTRO producto diferente
        boolean existeCodigo = listaProductos.stream()
                .anyMatch(p -> !p.getId().equals(productoSeleccionado.getId()) && p.getCodigo().equalsIgnoreCase(codigo));
        if (existeCodigo) {
            mensaje(Alert.AlertType.WARNING, "El código ingresado pertenece a otro producto.");
            return;
        }

        try {
            productoSeleccionado.setCodigo(codigo);
            productoSeleccionado.setNombre(txtNombre.getText().trim());
            productoSeleccionado.setCategoria(cmbCategoria.getValue());
            productoSeleccionado.setPrecioVenta(new BigDecimal(txtPrecio.getText().trim()));
            productoSeleccionado.setExistencia(Integer.parseInt(txtExistencia.getText().trim()));
            productoSeleccionado.setRutaImagen(rutaImagen);
            productoSeleccionado.setActivo(chkActivo.isSelected());

            if (productoDAO.actualizar(productoSeleccionado)) {
                mensaje(Alert.AlertType.INFORMATION, "Producto actualizado con éxito.");
                cargarProductos();
                limpiar();
            } else {
                mensaje(Alert.AlertType.ERROR, "No se pudo actualizar el producto.");
            }
        } catch (Exception e) {
            mensaje(Alert.AlertType.ERROR, "Error al actualizar los datos: " + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        if (productoSeleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de eliminar el producto '" + productoSeleccionado.getNombre() + "'?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (productoDAO.eliminar(productoSeleccionado.getId())) {
                mensaje(Alert.AlertType.INFORMATION, "Producto eliminado correctamente.");
                cargarProductos();
                limpiar();
            } else {
                mensaje(Alert.AlertType.ERROR, "Error al eliminar el producto.");
            }
        }
    }

    private boolean validarFormulario() {
        if (txtCodigo.getText().isBlank() || txtNombre.getText().isBlank() ||
                txtPrecio.getText().isBlank() || txtExistencia.getText().isBlank() ||
                cmbCategoria.getValue() == null) {
            mensaje(Alert.AlertType.WARNING, "Todos los campos obligatorios deben estar completos.");
            return false;
        }

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                mensaje(Alert.AlertType.WARNING, "El precio de venta debe ser un número mayor a cero.");
                return false;
            }
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.WARNING, "El precio ingresado no es válido.");
            return false;
        }

        try {
            int existencia = Integer.parseInt(txtExistencia.getText().trim());
            if (existencia < 0) {
                mensaje(Alert.AlertType.WARNING, "La existencia no puede ser negativa.");
                return false;
            }
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.WARNING, "La existencia debe ser un número entero válido.");
            return false;
        }

        return true;
    }

    @FXML
    private void limpiar() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtExistencia.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);
        imgProducto.setImage(null);
        rutaImagen = null;
        productoSeleccionado = null;
        tblProductos.getSelectionModel().clearSelection();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}