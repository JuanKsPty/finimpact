package com.juank.utp.finimpact.utils;

import com.juank.utp.finimpact.model.Usuario;
import com.juank.utp.finimpact.model.Iniciativa;
import com.juank.utp.finimpact.repository.UsuarioRepository;
import com.juank.utp.finimpact.repository.IniciativaRepository;
import com.juank.utp.finimpact.utils.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class AsignarIniciativasAnalista {
    public static void main(String[] args) {
        try {
            UsuarioRepository usuarioRepo = new UsuarioRepository();
            IniciativaRepository iniciativaRepo = new IniciativaRepository();

            System.out.println("=== ASIGNANDO INICIATIVAS AL ANALISTA ===");

            // Buscar usuario analista
            List<Usuario> usuarios = usuarioRepo.findAll();
            Usuario analista = null;

            for (Usuario usuario : usuarios) {
                if ("analista".equals(usuario.getRol()) && usuario.getNombreCompleto().contains("Analista")) {
                    analista = usuario;
                    break;
                }
            }

            if (analista == null) {
                System.out.println("❌ No se encontró usuario analista!");
                return;
            }

            System.out.println("✅ Usuario analista encontrado: " + analista.getNombreCompleto() + " (ID: " + analista.getIdUsuario() + ")");

            // Asignar algunas iniciativas al analista
            String[] iniciativasParaAnalista = {
                "Expansión Mercado Internacional",
                "Optimización Procesos Logísticos",
                "Optimización Línea A"
            };

            Connection conn = DatabaseConfig.getConnection();
            String updateSQL = "UPDATE iniciativas SET id_owner = ? WHERE nombre = ?";
            PreparedStatement stmt = conn.prepareStatement(updateSQL);

            int asignadas = 0;
            for (String nombreIniciativa : iniciativasParaAnalista) {
                stmt.setInt(1, analista.getIdUsuario());
                stmt.setString(2, nombreIniciativa);
                int result = stmt.executeUpdate();
                if (result > 0) {
                    System.out.println("✅ Iniciativa asignada: " + nombreIniciativa);
                    asignadas++;
                } else {
                    System.out.println("⚠️ No se pudo asignar: " + nombreIniciativa);
                }
            }

            stmt.close();
            conn.close();

            System.out.println("\n📈 RESULTADO:");
            System.out.println("  - Iniciativas asignadas al analista: " + asignadas);
            System.out.println("  - El analista ahora debería ver " + asignadas + " iniciativas en el dashboard");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
