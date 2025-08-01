package com.juank.utp.finimpact.repository;

import com.juank.utp.finimpact.model.Usuario;
import com.juank.utp.finimpact.utils.DatabaseConfig;
import com.juank.utp.finimpact.utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de usuarios
 */
public class UsuarioRepository {

    /**
     * Busca un usuario por email para autenticación
     */
    public Optional<Usuario> findByEmail(String email) {
        String sql = "SELECT id_usuario, nombre_completo, email, password, rol, estado FROM usuarios WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Usuario usuario = new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("nombre_completo"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("rol"),
                    rs.getBoolean("estado")
                );
                return Optional.of(usuario);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por email: " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Autentica un usuario con email y contraseña
     */
    public Optional<Usuario> authenticate(String email, String password) {
        Optional<Usuario> usuarioOpt = findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.isEstado() && PasswordUtils.verifyPassword(password, usuario.getPassword())) {
                return usuarioOpt;
            }
        }

        return Optional.empty();
    }

    /**
     * Obtiene todos los usuarios activos
     */
    public List<Usuario> findAll() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id_usuario, nombre_completo, email, password, rol, estado FROM usuarios WHERE estado = 1 ORDER BY nombre_completo";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Usuario usuario = new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("nombre_completo"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("rol"),
                    rs.getBoolean("estado")
                );
                usuarios.add(usuario);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios: " + e.getMessage());
        }

        return usuarios;
    }

    /**
     * Busca un usuario por ID
     */
    public Optional<Usuario> findById(int id) {
        String sql = "SELECT id_usuario, nombre_completo, email, password, rol, estado FROM usuarios WHERE id_usuario = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Usuario usuario = new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("nombre_completo"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("rol"),
                    rs.getBoolean("estado")
                );
                return Optional.of(usuario);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por ID: " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Crea un nuevo usuario
     */
    public boolean save(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre_completo, email, password, rol, estado) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNombreCompleto());
            stmt.setString(2, usuario.getEmail());
            // Hash la contraseña solo si no está ya hasheada
            String passwordToSave = usuario.getPassword();
            if (!passwordToSave.startsWith("$2a$")) { // BCrypt hash starts with $2a$
                passwordToSave = PasswordUtils.hashPassword(passwordToSave);
            }
            stmt.setString(3, passwordToSave);
            stmt.setString(4, usuario.getRol());
            stmt.setBoolean(5, usuario.isEstado());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    usuario.setIdUsuario(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
        }

        return false;
    }

    /**
     * Actualiza un usuario existente
     */
    public boolean update(Usuario usuario) {
        // Si el usuario tiene una nueva contraseña, incluirla en la actualización
        String sql;
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            sql = "UPDATE usuarios SET nombre_completo = ?, email = ?, password = ?, rol = ?, estado = ? WHERE id_usuario = ?";
        } else {
            sql = "UPDATE usuarios SET nombre_completo = ?, email = ?, rol = ?, estado = ? WHERE id_usuario = ?";
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNombreCompleto());
            stmt.setString(2, usuario.getEmail());

            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                // Hash la contraseña solo si no está ya hasheada
                String passwordToSave = usuario.getPassword();
                if (!passwordToSave.startsWith("$2a$")) { // BCrypt hash starts with $2a$
                    passwordToSave = PasswordUtils.hashPassword(passwordToSave);
                }
                stmt.setString(3, passwordToSave);
                stmt.setString(4, usuario.getRol());
                stmt.setBoolean(5, usuario.isEstado());
                stmt.setInt(6, usuario.getIdUsuario());
            } else {
                stmt.setString(3, usuario.getRol());
                stmt.setBoolean(4, usuario.isEstado());
                stmt.setInt(5, usuario.getIdUsuario());
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
        }

        return false;
    }

    /**
     * Actualiza la contraseña de un usuario
     */
    public boolean updatePassword(int idUsuario, String newPassword) {
        String sql = "UPDATE usuarios SET password = ? WHERE id_usuario = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, PasswordUtils.hashPassword(newPassword));
            stmt.setInt(2, idUsuario);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar contraseña: " + e.getMessage());
        }

        return false;
    }

    /**
     * Elimina un usuario (soft delete - cambiar estado a inactivo)
     */
    public boolean delete(int idUsuario) {
        String sql = "UPDATE usuarios SET estado = 0 WHERE id_usuario = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
        }

        return false;
    }

    /**
     * Obtiene usuarios activos por rol
     */
    public List<Usuario> findByRolAndEstado(String rol, boolean estado) {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id_usuario, nombre_completo, email, password, rol, estado FROM usuarios WHERE rol = ? AND estado = ? ORDER BY nombre_completo";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rol);
            stmt.setBoolean(2, estado);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Usuario usuario = new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("nombre_completo"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("rol"),
                    rs.getBoolean("estado")
                );
                usuarios.add(usuario);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar usuarios por rol y estado: " + e.getMessage());
        }

        return usuarios;
    }

    /**
     * Actualiza solo el estado de un usuario
     */
    public boolean updateEstado(int idUsuario, boolean estado) {
        String sql = "UPDATE usuarios SET estado = ? WHERE id_usuario = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, estado);
            stmt.setInt(2, idUsuario);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar estado del usuario: " + e.getMessage());
        }

        return false;
    }
}
