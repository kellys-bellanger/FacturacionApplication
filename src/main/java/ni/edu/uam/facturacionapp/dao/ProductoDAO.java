package ni.edu.uam.facturacionapp.dao;

import ni.edu.uam.facturacionapp.connection.DatabaseConnection;
import ni.edu.uam.facturacionapp.model.Categoria;
import ni.edu.uam.facturacionapp.model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // Paso 16: Operación con try-with-resources y propagación de SQLException
    public boolean guardar(Producto producto) throws SQLException {
        String sql = """
            INSERT INTO producto (
                codigo, nombre, categoria_id, precio_venta, existencia, ruta_imagen, activo
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getRutaImagen());
            ps.setBoolean(7, producto.isActivo());

            return ps.executeUpdate() > 0;
        }
    }

    // Paso 16: Operación con try-with-resources y propagación de SQLException
    public boolean actualizar(Producto producto) throws SQLException {
        String sql = """
            UPDATE producto SET 
                codigo = ?, nombre = ?, categoria_id = ?, precio_venta = ?, 
                existencia = ?, ruta_imagen = ?, activo = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getRutaImagen());
            ps.setBoolean(7, producto.isActivo());
            ps.setInt(8, producto.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // Paso 16: Operación con try-with-resources y propagación de SQLException
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Paso 16: try-with-resources para Connection, PreparedStatement y ResultSet
    public List<Producto> listar() {
        List<Producto> lista = new ArrayList<>();
        String sql = """
            SELECT p.id, p.codigo, p.nombre, p.precio_venta, p.existencia, p.ruta_imagen, p.activo,
                   c.id AS cat_id, c.nombre AS cat_nombre, c.activa AS cat_activa
            FROM producto p
            INNER JOIN categoria c ON p.categoria_id = c.id
            ORDER BY p.id DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Producto prod = new Producto();
                prod.setId(rs.getInt("id"));
                prod.setCodigo(rs.getString("codigo"));
                prod.setNombre(rs.getString("nombre"));
                prod.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                prod.setExistencia(rs.getInt("existencia"));
                prod.setRutaImagen(rs.getString("ruta_imagen"));
                prod.setActivo(rs.getBoolean("activo"));

                Categoria cat = new Categoria();
                cat.setId(rs.getInt("cat_id"));
                cat.setNombre(rs.getString("cat_nombre"));
                cat.setActiva(rs.getBoolean("cat_activa"));
                prod.setCategoria(cat);

                lista.add(prod);
            }
        } catch (SQLException e) {
            System.err.println("Error de base de datos al listar productos: " + e.getMessage());
        }
        return lista;
    }

    // Paso 14 & 16: try-with-resources completo
    public boolean existeCodigo(String codigo) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM Producto
            WHERE codigo = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método auxiliar para evitar duplicados al actualizar
    public boolean existeCodigoExcluyendoId(String codigo, int idExcluir) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM Producto
            WHERE codigo = ? AND id <> ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.setInt(2, idExcluir);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}