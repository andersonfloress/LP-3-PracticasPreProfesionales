package com.syspre.utils;

import com.syspre.config.DatabaseConfig;

import java.sql.*;

public class PlanTester {
    
    public static void testMinimalInsert() {
        System.out.println("🧪 PRUEBA MÍNIMA DE INSERT EN PLANES_PRACTICA");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            
            // Obtener un estudiante válido
            int estudianteId = 0;
            String findStudentSQL = "SELECT id FROM usuarios WHERE tipo = 'estudiante' LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(findStudentSQL)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    estudianteId = rs.getInt("id");
                }
            }
            
            if (estudianteId == 0) {
                System.out.println("❌ No se encontró estudiante");
                return;
            }
            
            System.out.println("✅ Estudiante ID: " + estudianteId);
            
            // Verificar valores válidos del ENUM estado
            String enumSQL = "SHOW COLUMNS FROM planes_practica LIKE 'estado'";
            try (PreparedStatement stmt = conn.prepareStatement(enumSQL)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String type = rs.getString("Type");
                    System.out.println("🔍 CAMPO ESTADO: " + type);
                }
            }
            
            // INSERT con TODOS los campos NOT NULL identificados
            String sql = """
                INSERT INTO planes_practica (
                    estudiante_id, nombres, apellidos, codigo, especialidad, email, telefono,
                    empresa, ruc, direccion_empresa, telefono_empresa, supervisor, cargo_supervisor,
                    fecha_inicio, fecha_fin, total_horas, actividades, objetivos, estado
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, estudianteId);
                stmt.setString(2, "Juan");
                stmt.setString(3, "Perez");
                stmt.setString(4, "TEST001");
                stmt.setString(5, "Ingeniería de Sistemas");
                stmt.setString(6, "test@test.com");
                stmt.setString(7, "123456789");
                // AGREGAR campos empresa obligatorios
                stmt.setString(8, "Empresa Test");
                stmt.setString(9, "20123456789");
                stmt.setString(10, "Lima, Perú");
                stmt.setString(11, "987654321");
                stmt.setString(12, "Supervisor Test");
                stmt.setString(13, "Jefe de Área");
                // Fechas y demás
                stmt.setString(14, "2025-03-01");
                stmt.setString(15, "2025-07-31");
                stmt.setInt(16, 240);
                stmt.setString(17, "Actividades de prueba");
                stmt.setString(18, "Objetivos de prueba");
                stmt.setString(19, "pendiente"); // Usar valor por defecto del ENUM
                
                System.out.println("📤 Ejecutando INSERT...");
                int rows = stmt.executeUpdate();
                System.out.println("✅ INSERT exitoso. Filas afectadas: " + rows);
                
                // Limpiar registro de prueba
                String deleteSQL = "DELETE FROM planes_practica WHERE estudiante_id = ? AND codigo = 'TEST001'";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {
                    deleteStmt.setInt(1, estudianteId);
                    deleteStmt.executeUpdate();
                    System.out.println("🧹 Registro de prueba eliminado");
                }
                
            } catch (SQLException e) {
                System.err.println("❌ Error en INSERT: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error general: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        testMinimalInsert();
    }
}