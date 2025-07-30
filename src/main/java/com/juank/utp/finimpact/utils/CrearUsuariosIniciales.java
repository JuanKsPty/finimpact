package com.juank.utp.finimpact.utils;

import com.juank.utp.finimpact.model.Usuario;
import com.juank.utp.finimpact.repository.UsuarioRepository;

/**
 * Utilidad para crear usuarios iniciales del sistema
 */
public class CrearUsuariosIniciales {

    public static void main(String[] args) {
        UsuarioRepository usuarioRepository = new UsuarioRepository();

        try {
            System.out.println("🚀 Iniciando creación de usuarios iniciales...\n");

            // Crear usuario administrador
            Usuario admin = new Usuario();
            admin.setNombreCompleto("Administrador del Sistema");
            admin.setEmail("admin@finimpact.com");
            admin.setPassword("admin123"); // Se hasheará automáticamente en el repositorio
            admin.setRol("admin");
            admin.setEstado(true);

            if (usuarioRepository.save(admin)) {
                System.out.println("✅ Usuario administrador creado exitosamente");
                System.out.println("   Email: admin@finimpact.com");
                System.out.println("   Password: admin123");
                System.out.println("   ID generado: " + admin.getIdUsuario());
            } else {
                System.out.println("❌ Error al crear usuario administrador");
            }

            // Crear usuario analista
            Usuario analista = new Usuario();
            analista.setNombreCompleto("Juan Analista");
            analista.setEmail("analista@finimpact.com");
            analista.setPassword("analista123"); // Se hasheará automáticamente en el repositorio
            analista.setRol("analista");
            analista.setEstado(true);

            if (usuarioRepository.save(analista)) {
                System.out.println("✅ Usuario analista creado exitosamente");
                System.out.println("   Email: analista@finimpact.com");
                System.out.println("   Password: analista123");
                System.out.println("   ID generado: " + analista.getIdUsuario());
            } else {
                System.out.println("❌ Error al crear usuario analista");
            }

            // Crear usuario viewer
            Usuario viewer = new Usuario();
            viewer.setNombreCompleto("Maria Viewer");
            viewer.setEmail("viewer@finimpact.com");
            viewer.setPassword("viewer123"); // Se hasheará automáticamente en el repositorio
            viewer.setRol("viewer");
            viewer.setEstado(true);

            if (usuarioRepository.save(viewer)) {
                System.out.println("✅ Usuario viewer creado exitosamente");
                System.out.println("   Email: viewer@finimpact.com");
                System.out.println("   Password: viewer123");
                System.out.println("   ID generado: " + viewer.getIdUsuario());
            } else {
                System.out.println("❌ Error al crear usuario viewer");
            }

            System.out.println("\n🎉 Proceso de creación de usuarios completado!");
            System.out.println("Puedes usar cualquiera de estos usuarios para hacer login en la aplicación.");

        } catch (Exception e) {
            System.err.println("❌ Error durante la creación de usuarios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método utilitario para crear un usuario específico
     */
    public static boolean crearUsuario(String nombreCompleto, String email, String password, String rol, boolean estado) {
        UsuarioRepository usuarioRepository = new UsuarioRepository();

        try {
            Usuario usuario = new Usuario();
            usuario.setNombreCompleto(nombreCompleto);
            usuario.setEmail(email);
            usuario.setPassword(password); // Se hasheará automáticamente
            usuario.setRol(rol);
            usuario.setEstado(estado);

            boolean resultado = usuarioRepository.save(usuario);

            if (resultado) {
                System.out.println("✅ Usuario creado: " + email + " (ID: " + usuario.getIdUsuario() + ")");
            } else {
                System.out.println("❌ Error al crear usuario: " + email);
            }

            return resultado;
        } catch (Exception e) {
            System.err.println("❌ Error al crear usuario " + email + ": " + e.getMessage());
            return false;
        }
    }
}
