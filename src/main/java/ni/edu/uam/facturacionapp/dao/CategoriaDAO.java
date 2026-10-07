package ni.edu.uam.facturacionapp.dao;

import ni.edu.uam.facturacionapp.connection.DatabaseConnection;
import ni.edu.uam.facturacionapp.model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public List<Categoria> listar() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, activa FROM categoria ORDER BY id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Categoria(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getBoolean("activa")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar categorías: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    public boolean guardar(Categoria c) throws SQLException {
        String sql = "INSERT INTO categoria (nombre, activa) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setBoolean(2, c.isActiva());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Categoria c) throws SQLException {
        String sql = "UPDATE categoria SET nombre = ?, activa = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setBoolean(2, c.isActiva());
            ps.setInt(3, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Paso 5: Verificar existencia por nombre (Guardar / Inserción)
    public boolean existeNombre(String nombre) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM categoria
            WHERE LOWER(nombre) = LOWER(?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Paso 5: Verificar existencia excluyendo un ID (Actualizar / UPDATE)
    public boolean existeNombreExcluyendoId(String nombre, int idExcluir) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM categoria
            WHERE LOWER(nombre) = LOWER(?) AND id <> ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
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