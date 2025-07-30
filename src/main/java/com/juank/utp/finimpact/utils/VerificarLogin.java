package com.juank.utp.finimpact.utils;

import com.juank.utp.finimpact.model.Usuario;
import com.juank.utp.finimpact.repository.UsuarioRepository;
import java.util.Optional;

/**
 * Utilidad para verificar que el sistema de hashing y autenticación funciona correctamente
 */
public class VerificarLogin {

    public static void main(String[] args) {
        System.out.println("🔐 Iniciando verificación del sistema de login...\n");

        // Datos de prueba de los usuarios que ya existen en tu BD
        String[][] usuariosPrueba = {
            {"admin@finimpact.com", "admin123", "admin"},
            {"analista@finimpact.com", "analista123", "analista"},
            {"viewer@finimpact.com", "viewer123", "viewer"}
        };

        UsuarioRepository usuarioRepository = new UsuarioRepository();
        int exitosos = 0;
        int fallidos = 0;

        for (String[] datosUsuario : usuariosPrueba) {
            String email = datosUsuario[0];
            String password = datosUsuario[1];
            String rolEsperado = datosUsuario[2];

            System.out.println("🧪 Probando login para: " + email);

            if (verificarUsuario(usuarioRepository, email, password, rolEsperado)) {
                exitosos++;
            } else {
                fallidos++;
            }
            System.out.println(); // Línea en blanco para separar
        }

        // Probar login con credenciales incorrectas
        System.out.println("🧪 Probando login con credenciales incorrectas...");
        if (probarCredencialesIncorrectas(usuarioRepository)) {
            System.out.println("✅ Correctamente rechazó credenciales incorrectas");
        } else {
            System.out.println("❌ Error: aceptó credenciales incorrectas");
            fallidos++;
        }

        // Resumen
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📊 RESUMEN DE VERIFICACIÓN:");
        System.out.println("✅ Logins exitosos: " + exitosos);
        System.out.println("❌ Logins fallidos: " + fallidos);

        if (fallidos == 0) {
            System.out.println("🎉 ¡Todos los tests pasaron! El sistema de login funciona correctamente.");
        } else {
            System.out.println("⚠️  Algunos tests fallaron. Revisa la configuración de la base de datos.");
        }
    }

    /**
     * Verifica que un usuario específico pueda hacer login correctamente
     */
    private static boolean verificarUsuario(UsuarioRepository repo, String email, String password, String rolEsperado) {
        try {
            // Paso 1: Verificar que el usuario existe en la BD
            Optional<Usuario> usuarioOpt = repo.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                System.out.println("❌ Usuario no encontrado en la base de datos");
                return false;
            }

            Usuario usuario = usuarioOpt.get();
            System.out.println("   📧 Usuario encontrado: " + usuario.getNombreCompleto());
            System.out.println("   🏷️  Rol: " + usuario.getRol());
            System.out.println("   🟢 Estado: " + (usuario.isEstado() ? "Activo" : "Inactivo"));

            // Paso 2: Verificar que el rol sea correcto
            if (!usuario.getRol().equals(rolEsperado)) {
                System.out.println("❌ Rol incorrecto. Esperado: " + rolEsperado + ", Actual: " + usuario.getRol());
                return false;
            }

            // Paso 3: Probar autenticación con contraseña
            Optional<Usuario> authResult = repo.authenticate(email, password);
            if (authResult.isEmpty()) {
                System.out.println("❌ Autenticación fallida - contraseña incorrecta o usuario inactivo");
                return false;
            }

            // Paso 4: Verificar que el hash funciona manualmente
            String hashGenerado = PasswordUtils.hashPassword(password);
            boolean hashValido = PasswordUtils.verifyPassword(password, usuario.getPassword());

            System.out.println("   🔑 Hash en BD: " + usuario.getPassword().substring(0, 20) + "...");
            System.out.println("   🔑 Hash generado: " + hashGenerado.substring(0, 20) + "...");
            System.out.println("   ✅ Verificación de hash: " + (hashValido ? "VÁLIDA" : "INVÁLIDA"));

            if (!hashValido) {
                System.out.println("❌ El hash no coincide con la contraseña");
                return false;
            }

            System.out.println("✅ LOGIN EXITOSO para " + email);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error durante la verificación: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Prueba que el sistema rechace credenciales incorrectas
     */
    private static boolean probarCredencialesIncorrectas(UsuarioRepository repo) {
        try {
            // Probar con email que no existe
            Optional<Usuario> result1 = repo.authenticate("noexiste@test.com", "cualquierpassword");
            if (result1.isPresent()) {
                System.out.println("❌ Error: aceptó email inexistente");
                return false;
            }

            // Probar con email válido pero contraseña incorrecta
            Optional<Usuario> result2 = repo.authenticate("admin@finimpact.com", "passwordincorrecto");
            if (result2.isPresent()) {
                System.out.println("❌ Error: aceptó contraseña incorrecta");
                return false;
            }

            System.out.println("   ✅ Rechazó email inexistente");
            System.out.println("   ✅ Rechazó contraseña incorrecta");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error durante prueba de credenciales incorrectas: " + e.getMessage());
            return false;
        }
    }

    /**
     * Método utilitario para verificar un usuario específico desde código
     */
    public static boolean verificarLoginUsuario(String email, String password) {
        UsuarioRepository repo = new UsuarioRepository();
        Optional<Usuario> result = repo.authenticate(email, password);

        if (result.isPresent()) {
            Usuario usuario = result.get();
            System.out.println("✅ Login exitoso: " + usuario.getNombreCompleto() + " (" + usuario.getRol() + ")");
            return true;
        } else {
            System.out.println("❌ Login fallido para: " + email);
            return false;
        }
    }

    /**
     * Método para probar el hashing de contraseñas manualmente
     */
    public static void probarHashing() {
        System.out.println("🔧 Probando sistema de hashing...\n");

        String[] passwordsPrueba = {"admin123", "analista123", "viewer123", "password123"};

        for (String password : passwordsPrueba) {
            String hash1 = PasswordUtils.hashPassword(password);
            String hash2 = PasswordUtils.hashPassword(password);

            boolean verificacion1 = PasswordUtils.verifyPassword(password, hash1);
            boolean verificacion2 = PasswordUtils.verifyPassword(password, hash2);
            boolean hashesIguales = hash1.equals(hash2);

            System.out.println("Password: " + password);
            System.out.println("  Hash 1: " + hash1);
            System.out.println("  Hash 2: " + hash2);
            System.out.println("  Hashes iguales: " + hashesIguales);
            System.out.println("  Verificación 1: " + verificacion1);
            System.out.println("  Verificación 2: " + verificacion2);
            System.out.println("  ✅ Estado: " + (hashesIguales && verificacion1 && verificacion2 ? "OK" : "ERROR"));
            System.out.println();
        }
    }
}
