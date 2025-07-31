package com.juank.utp.finimpact.repository;

import com.juank.utp.finimpact.model.Iniciativa;
import com.juank.utp.finimpact.utils.DatabaseConfig;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de iniciativas
 */
public class IniciativaRepository {

    /**
     * Obtiene todas las iniciativas
     */
    public List<Iniciativa> findAll() {
        List<Iniciativa> iniciativas = new ArrayList<>();
        String sql = "SELECT id_iniciativa, nombre, descripcion, fecha_inicio, fecha_fin, tipo, estado, riesgo, id_owner, fecha_registro FROM iniciativas ORDER BY fecha_registro DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Iniciativa iniciativa = createIniciativaFromResultSet(rs);
                iniciativas.add(iniciativa);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener iniciativas: " + e.getMessage());
        }

        return iniciativas;
    }

    /**
     * Busca una iniciativa por ID
     */
    public Optional<Iniciativa> findById(int id) {
        String sql = "SELECT id_iniciativa, nombre, descripcion, fecha_inicio, fecha_fin, tipo, estado, riesgo, id_owner, fecha_registro FROM iniciativas WHERE id_iniciativa = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Iniciativa iniciativa = createIniciativaFromResultSet(rs);
                return Optional.of(iniciativa);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar iniciativa por ID: " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Obtiene iniciativas por owner
     */
    public List<Iniciativa> findByOwner(int idOwner) {
        List<Iniciativa> iniciativas = new ArrayList<>();
        String sql = "SELECT id_iniciativa, nombre, descripcion, fecha_inicio, fecha_fin, tipo, estado, riesgo, id_owner, fecha_registro FROM iniciativas WHERE id_owner = ? ORDER BY fecha_registro DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idOwner);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Iniciativa iniciativa = createIniciativaFromResultSet(rs);
                iniciativas.add(iniciativa);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener iniciativas por owner: " + e.getMessage());
        }

        return iniciativas;
    }

    /**
     * Obtiene iniciativas por estado
     */
    public List<Iniciativa> findByEstado(String estado) {
        List<Iniciativa> iniciativas = new ArrayList<>();
        String sql = "SELECT id_iniciativa, nombre, descripcion, fecha_inicio, fecha_fin, tipo, estado, riesgo, id_owner, fecha_registro FROM iniciativas WHERE estado = ? ORDER BY fecha_registro DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Iniciativa iniciativa = createIniciativaFromResultSet(rs);
                iniciativas.add(iniciativa);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener iniciativas por estado: " + e.getMessage());
        }

        return iniciativas;
    }

    /**
     * Obtiene iniciativas por múltiples filtros
     */
    public List<Iniciativa> findByFilters(String estado, String tipo, String riesgo, Integer idOwner) {
        List<Iniciativa> iniciativas = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id_iniciativa, nombre, descripcion, fecha_inicio, fecha_fin, tipo, estado, riesgo, id_owner, fecha_registro FROM iniciativas WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (estado != null && !estado.isEmpty()) {
            sql.append(" AND estado = ?");
            parameters.add(estado);
        }

        if (tipo != null && !tipo.isEmpty()) {
            sql.append(" AND tipo = ?");
            parameters.add(tipo);
        }

        if (riesgo != null && !riesgo.isEmpty()) {
            sql.append(" AND riesgo = ?");
            parameters.add(riesgo);
        }

        if (idOwner != null) {
            sql.append(" AND id_owner = ?");
            parameters.add(idOwner);
        }

        sql.append(" ORDER BY fecha_registro DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Iniciativa iniciativa = createIniciativaFromResultSet(rs);
                iniciativas.add(iniciativa);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener iniciativas por filtros: " + e.getMessage());
        }

        return iniciativas;
    }

    /**
     * Crea una nueva iniciativa
     */
    public boolean save(Iniciativa iniciativa) {
        String sql = "INSERT INTO iniciativas (nombre, descripcion, fecha_inicio, fecha_fin, tipo, estado, riesgo, id_owner) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, iniciativa.getNombre());
            stmt.setString(2, iniciativa.getDescripcion());
            stmt.setDate(3, iniciativa.getFechaInicio() != null ? Date.valueOf(iniciativa.getFechaInicio()) : null);
            stmt.setDate(4, iniciativa.getFechaFin() != null ? Date.valueOf(iniciativa.getFechaFin()) : null);
            stmt.setString(5, iniciativa.getTipo());
            stmt.setString(6, iniciativa.getEstado());
            stmt.setString(7, iniciativa.getRiesgo());
            stmt.setInt(8, iniciativa.getIdOwner());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    iniciativa.setIdIniciativa(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al crear iniciativa: " + e.getMessage());
        }

        return false;
    }

    /**
     * Actualiza una iniciativa existente
     */
    public boolean update(Iniciativa iniciativa) {
        String sql = "UPDATE iniciativas SET nombre = ?, descripcion = ?, fecha_inicio = ?, fecha_fin = ?, tipo = ?, estado = ?, riesgo = ?, id_owner = ? WHERE id_iniciativa = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, iniciativa.getNombre());
            stmt.setString(2, iniciativa.getDescripcion());
            stmt.setDate(3, iniciativa.getFechaInicio() != null ? Date.valueOf(iniciativa.getFechaInicio()) : null);
            stmt.setDate(4, iniciativa.getFechaFin() != null ? Date.valueOf(iniciativa.getFechaFin()) : null);
            stmt.setString(5, iniciativa.getTipo());
            stmt.setString(6, iniciativa.getEstado());
            stmt.setString(7, iniciativa.getRiesgo());
            stmt.setInt(8, iniciativa.getIdOwner());
            stmt.setInt(9, iniciativa.getIdIniciativa());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar iniciativa: " + e.getMessage());
        }

        return false;
    }

    /**
     * Elimina una iniciativa
     */
    public boolean delete(int idIniciativa) {
        String sql = "DELETE FROM iniciativas WHERE id_iniciativa = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idIniciativa);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar iniciativa: " + e.getMessage());
        }

        return false;
    }

    /**
     * Cuenta el número de iniciativas activas (en curso)
     */
    public int countIniciativasActivas() {
        String sql = "SELECT COUNT(*) FROM iniciativas WHERE estado = 'en curso'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar iniciativas activas: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Método auxiliar para crear un objeto Iniciativa desde ResultSet
     */
    private Iniciativa createIniciativaFromResultSet(ResultSet rs) throws SQLException {
        Iniciativa iniciativa = new Iniciativa();
        iniciativa.setIdIniciativa(rs.getInt("id_iniciativa"));
        iniciativa.setNombre(rs.getString("nombre"));
        iniciativa.setDescripcion(rs.getString("descripcion"));

        Date fechaInicio = rs.getDate("fecha_inicio");
        if (fechaInicio != null) {
            iniciativa.setFechaInicio(fechaInicio.toLocalDate());
        }

        Date fechaFin = rs.getDate("fecha_fin");
        if (fechaFin != null) {
            iniciativa.setFechaFin(fechaFin.toLocalDate());
        }

        iniciativa.setTipo(rs.getString("tipo"));
        iniciativa.setEstado(rs.getString("estado"));
        iniciativa.setRiesgo(rs.getString("riesgo"));
        iniciativa.setIdOwner(rs.getInt("id_owner"));

        Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            iniciativa.setFechaRegistro(fechaRegistro.toLocalDateTime());
        }

        return iniciativa;
    }
}
