package com.juank.utp.finimpact.repository;

import com.juank.utp.finimpact.model.Impacto;
import com.juank.utp.finimpact.utils.DatabaseConfig;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de impactos financieros
 */
public class ImpactoRepository {

    /**
     * Obtiene todos los impactos
     */
    public List<Impacto> findAll() {
        List<Impacto> impactos = new ArrayList<>();
        String sql = "SELECT id_impacto, id_iniciativa, fecha_creacion, tipo_impacto, multiplicador, atributo_impacto, fecha_impacto, impacto FROM impactos ORDER BY fecha_creacion DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Impacto impacto = createImpactoFromResultSet(rs);
                impactos.add(impacto);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener impactos: " + e.getMessage());
        }

        return impactos;
    }

    /**
     * Busca un impacto por ID
     */
    public Optional<Impacto> findById(int id) {
        String sql = "SELECT id_impacto, id_iniciativa, fecha_creacion, tipo_impacto, multiplicador, atributo_impacto, fecha_impacto, impacto FROM impactos WHERE id_impacto = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Impacto impacto = createImpactoFromResultSet(rs);
                return Optional.of(impacto);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar impacto por ID: " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Obtiene impactos por iniciativa
     */
    public List<Impacto> findByIniciativa(int idIniciativa) {
        List<Impacto> impactos = new ArrayList<>();
        String sql = "SELECT id_impacto, id_iniciativa, fecha_creacion, tipo_impacto, multiplicador, atributo_impacto, fecha_impacto, impacto FROM impactos WHERE id_iniciativa = ? ORDER BY fecha_impacto DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idIniciativa);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Impacto impacto = createImpactoFromResultSet(rs);
                impactos.add(impacto);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener impactos por iniciativa: " + e.getMessage());
        }

        return impactos;
    }

    /**
     * Obtiene impactos por tipo de impacto
     */
    public List<Impacto> findByTipoImpacto(String tipoImpacto) {
        List<Impacto> impactos = new ArrayList<>();
        String sql = "SELECT id_impacto, id_iniciativa, fecha_creacion, tipo_impacto, multiplicador, atributo_impacto, fecha_impacto, impacto FROM impactos WHERE tipo_impacto = ? ORDER BY fecha_impacto DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipoImpacto);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Impacto impacto = createImpactoFromResultSet(rs);
                impactos.add(impacto);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener impactos por tipo: " + e.getMessage());
        }

        return impactos;
    }

    /**
     * Obtiene impactos por atributo (Planeado, Estimado, Real)
     */
    public List<Impacto> findByAtributoImpacto(String atributoImpacto) {
        List<Impacto> impactos = new ArrayList<>();
        String sql = "SELECT id_impacto, id_iniciativa, fecha_creacion, tipo_impacto, multiplicador, atributo_impacto, fecha_impacto, impacto FROM impactos WHERE atributo_impacto = ? ORDER BY fecha_impacto DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, atributoImpacto);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Impacto impacto = createImpactoFromResultSet(rs);
                impactos.add(impacto);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener impactos por atributo: " + e.getMessage());
        }

        return impactos;
    }

    /**
     * Obtiene impactos por rango de fechas
     */
    public List<Impacto> findByFechaRange(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Impacto> impactos = new ArrayList<>();
        String sql = "SELECT id_impacto, id_iniciativa, fecha_creacion, tipo_impacto, multiplicador, atributo_impacto, fecha_impacto, impacto FROM impactos WHERE fecha_impacto BETWEEN ? AND ? ORDER BY fecha_impacto DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fechaInicio));
            stmt.setDate(2, Date.valueOf(fechaFin));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Impacto impacto = createImpactoFromResultSet(rs);
                impactos.add(impacto);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener impactos por rango de fechas: " + e.getMessage());
        }

        return impactos;
    }

    /**
     * Obtiene impactos con filtros múltiples
     */
    public List<Impacto> findByFilters(String tipoImpacto, String atributoImpacto, Integer idIniciativa, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Impacto> impactos = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id_impacto, id_iniciativa, fecha_creacion, tipo_impacto, multiplicador, atributo_impacto, fecha_impacto, impacto FROM impactos WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (tipoImpacto != null && !tipoImpacto.isEmpty()) {
            sql.append(" AND tipo_impacto = ?");
            parameters.add(tipoImpacto);
        }

        if (atributoImpacto != null && !atributoImpacto.isEmpty()) {
            sql.append(" AND atributo_impacto = ?");
            parameters.add(atributoImpacto);
        }

        if (idIniciativa != null) {
            sql.append(" AND id_iniciativa = ?");
            parameters.add(idIniciativa);
        }

        if (fechaInicio != null) {
            sql.append(" AND fecha_impacto >= ?");
            parameters.add(Date.valueOf(fechaInicio));
        }

        if (fechaFin != null) {
            sql.append(" AND fecha_impacto <= ?");
            parameters.add(Date.valueOf(fechaFin));
        }

        sql.append(" ORDER BY fecha_impacto DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Impacto impacto = createImpactoFromResultSet(rs);
                impactos.add(impacto);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener impactos por filtros: " + e.getMessage());
        }

        return impactos;
    }

    /**
     * Crea un nuevo impacto
     */
    public boolean save(Impacto impacto) {
        String sql = "INSERT INTO impactos (id_iniciativa, fecha_creacion, tipo_impacto, multiplicador, atributo_impacto, fecha_impacto, impacto) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, impacto.getIdIniciativa());
            stmt.setDate(2, Date.valueOf(impacto.getFechaCreacion()));
            stmt.setString(3, impacto.getTipoImpacto());
            stmt.setInt(4, impacto.getMultiplicador());
            stmt.setString(5, impacto.getAtributoImpacto());
            stmt.setDate(6, Date.valueOf(impacto.getFechaImpacto()));
            stmt.setBigDecimal(7, impacto.getImpacto());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    impacto.setIdImpacto(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al crear impacto: " + e.getMessage());
        }

        return false;
    }

    /**
     * Actualiza un impacto existente
     */
    public boolean update(Impacto impacto) {
        String sql = "UPDATE impactos SET id_iniciativa = ?, fecha_creacion = ?, tipo_impacto = ?, multiplicador = ?, atributo_impacto = ?, fecha_impacto = ?, impacto = ? WHERE id_impacto = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, impacto.getIdIniciativa());
            stmt.setDate(2, Date.valueOf(impacto.getFechaCreacion()));
            stmt.setString(3, impacto.getTipoImpacto());
            stmt.setInt(4, impacto.getMultiplicador());
            stmt.setString(5, impacto.getAtributoImpacto());
            stmt.setDate(6, Date.valueOf(impacto.getFechaImpacto()));
            stmt.setBigDecimal(7, impacto.getImpacto());
            stmt.setInt(8, impacto.getIdImpacto());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar impacto: " + e.getMessage());
        }

        return false;
    }

    /**
     * Elimina un impacto
     */
    public boolean delete(int idImpacto) {
        String sql = "DELETE FROM impactos WHERE id_impacto = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idImpacto);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar impacto: " + e.getMessage());
        }

        return false;
    }

    /**
     * Calcula la suma de impactos por atributo en un rango de fechas
     */
    public BigDecimal sumImpactosByAtributoAndFecha(String atributoImpacto, LocalDate fechaInicio, LocalDate fechaFin) {
        String sql = "SELECT COALESCE(SUM(impacto * multiplicador), 0) FROM impactos WHERE atributo_impacto = ? AND fecha_impacto BETWEEN ? AND ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, atributoImpacto);
            stmt.setDate(2, Date.valueOf(fechaInicio));
            stmt.setDate(3, Date.valueOf(fechaFin));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal(1);
            }

        } catch (SQLException e) {
            System.err.println("Error al calcular suma de impactos: " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }

    /**
     * Obtiene estadísticas de impactos por tipo
     */
    public List<Object[]> getEstadisticasPorTipo() {
        List<Object[]> estadisticas = new ArrayList<>();
        String sql = "SELECT tipo_impacto, COUNT(*) as cantidad, SUM(impacto * multiplicador) as total FROM impactos GROUP BY tipo_impacto ORDER BY total DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Object[] fila = new Object[3];
                fila[0] = rs.getString("tipo_impacto");
                fila[1] = rs.getInt("cantidad");
                fila[2] = rs.getBigDecimal("total");
                estadisticas.add(fila);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas por tipo: " + e.getMessage());
        }

        return estadisticas;
    }

    /**
     * Método auxiliar para crear un objeto Impacto desde ResultSet
     */
    private Impacto createImpactoFromResultSet(ResultSet rs) throws SQLException {
        Impacto impacto = new Impacto();
        impacto.setIdImpacto(rs.getInt("id_impacto"));
        impacto.setIdIniciativa(rs.getInt("id_iniciativa"));
        impacto.setFechaCreacion(rs.getDate("fecha_creacion").toLocalDate());
        impacto.setTipoImpacto(rs.getString("tipo_impacto"));
        impacto.setMultiplicador(rs.getInt("multiplicador"));
        impacto.setAtributoImpacto(rs.getString("atributo_impacto"));
        impacto.setFechaImpacto(rs.getDate("fecha_impacto").toLocalDate());
        impacto.setImpacto(rs.getBigDecimal("impacto"));

        return impacto;
    }
}
