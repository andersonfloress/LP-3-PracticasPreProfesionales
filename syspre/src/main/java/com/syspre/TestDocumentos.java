package com.syspre;

import com.syspre.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestDocumentos {
    public static void main(String[] args) {
        try {
            // Obtener ID del coordinador
            String sqlCoordinador = "SELECT id FROM usuarios WHERE email = 'coordinador@syspre.com'";
            int coordinadorId = 0;
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sqlCoordinador)) {
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    coordinadorId = rs.getInt("id");
                    System.out.println("✅ Coordinador ID encontrado: " + coordinadorId);
                }
            }
            
            if (coordinadorId > 0) {
                // Insertar documentos de prueba
                String sqlInsert = """
                    INSERT INTO documentos_especialidad 
                    (coordinador_id, especialidad, titulo, descripcion, archivo_path, archivo_nombre, archivo_size, tipo, activo)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                    
                    // Documento 1
                    stmt.setInt(1, coordinadorId);
                    stmt.setString(2, "Ingeniería de Sistemas");
                    stmt.setString(3, "Reglamento de Prácticas Pre-Profesionales");
                    stmt.setString(4, "Documento oficial con normativas y procedimientos para las prácticas pre-profesionales");
                    stmt.setString(5, "uploads/documentos_especialidad/ingenieria_sistemas/reglamento_practicas.pdf");
                    stmt.setString(6, "reglamento_practicas.pdf");
                    stmt.setLong(7, 245760);
                    stmt.setString(8, "principal");
                    stmt.setBoolean(9, true);
                    stmt.executeUpdate();
                    System.out.println("✅ Documento 1 insertado");
                    
                    // Documento 2
                    stmt.setInt(1, coordinadorId);
                    stmt.setString(2, "Ingeniería de Sistemas");
                    stmt.setString(3, "Formato de Evaluación de Prácticas");
                    stmt.setString(4, "Criterios de evaluación, rúbricas y formatos oficiales para calificar el desempeño estudiantil");
                    stmt.setString(5, "uploads/documentos_especialidad/ingenieria_sistemas/formato_evaluacion.pdf");
                    stmt.setString(6, "formato_evaluacion.pdf");
                    stmt.setLong(7, 189440);
                    stmt.setString(8, "secundario");
                    stmt.setBoolean(9, true);
                    stmt.executeUpdate();
                    System.out.println("✅ Documento 2 insertado");
                }
                
                // Verificar inserción
                String sqlVerificar = """
                    SELECT d.id, d.titulo, d.especialidad, u.nombres, u.apellidos 
                    FROM documentos_especialidad d 
                    JOIN usuarios u ON d.coordinador_id = u.id 
                    WHERE d.especialidad = 'Ingeniería de Sistemas'
                """;
                
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sqlVerificar)) {
                    
                    ResultSet rs = stmt.executeQuery();
                    System.out.println("📋 DOCUMENTOS INSERTADOS:");
                    while (rs.next()) {
                        System.out.printf("  ID: %d - %s (por %s %s)%n",
                            rs.getInt("id"),
                            rs.getString("titulo"),
                            rs.getString("nombres"),
                            rs.getString("apellidos")
                        );
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}