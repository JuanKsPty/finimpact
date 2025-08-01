package com.juank.utp.finimpact.utils;

import com.juank.utp.finimpact.model.Usuario;
import com.juank.utp.finimpact.repository.UsuarioRepository;

/**
 * Utilidad para probar la creación y eliminación de usuarios con contraseñas
 */
public class TestPasswordCreation {

    public static void main(String[] args) {
        System.out.println("🧪 Probando creación y eliminación de usuarios con contraseñas...\n");

        UsuarioRepository usuarioRepository = new UsuarioRepository();

        // Crear usuarios de prueba
        Usuario[] usuariosPrueba = {
            new Usuario(0, "Test Admin", "test.admin@finimpact.com", "testpass123", "admin", true),
            new Usuario(0, "Test Analista", "test.analista@finimpact.com", "testpass456", "analista", true),
            new Usuario(0, "Test Viewer", "test.viewer@finimpact.com", "testpass789", "viewer", true)
        };

        System.out.println("📝 Creando usuarios de prueba...");
        for (Usuario usuario : usuariosPrueba) {
            try {
                boolean creado = usuarioRepository.save(usuario);
                if (creado) {
                    System.out.println("✅ Usuario creado: " + usuario.getEmail() + " (ID: " + usuario.getId() + ")");

                    // Probar login inmediatamente después de crear
                    if (VerificarLogin.verificarLoginUsuario(usuario.getEmail(),
                            usuario.getEmail().contains("admin") ? "testpass123" :
                            usuario.getEmail().contains("analista") ? "testpass456" : "testpass789")) {
                        System.out.println("   ✅ Login verificado correctamente");
                    } else {
                        System.out.println("   ❌ Error en login después de crear");
                    }
                } else {
                    System.out.println("❌ Error al crear usuario: " + usuario.getEmail());
                }
            } catch (Exception e) {
                System.out.println("❌ Excepción al crear usuario " + usuario.getEmail() + ": " + e.getMessage());
            }
            System.out.println();
        }

        // Esperar un momento
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("🗑️ Eliminando usuarios de prueba...");
        for (Usuario usuario : usuariosPrueba) {
            try {
                // Buscar el usuario por email para obtener su ID
                var usuarioEncontrado = usuarioRepository.findByEmail(usuario.getEmail());
                if (usuarioEncontrado.isPresent()) {
                    boolean eliminado = usuarioRepository.delete(usuarioEncontrado.get().getId());
                    if (eliminado) {
                        System.out.println("✅ Usuario eliminado: " + usuario.getEmail());
                    } else {
                        System.out.println("❌ Error al eliminar usuario: " + usuario.getEmail());
                    }
                } else {
                    System.out.println("⚠️ Usuario no encontrado para eliminar: " + usuario.getEmail());
                }
            } catch (Exception e) {
                System.out.println("❌ Excepción al eliminar usuario " + usuario.getEmail() + ": " + e.getMessage());
            }
        }

        System.out.println("\n🎯 Test de creación de contraseñas completado.");
    }
}
