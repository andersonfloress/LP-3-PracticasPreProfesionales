package com.syspre.utils;

import com.syspre.config.DatabaseConfig;

import java.sql.*;

public class DatabaseTester {
    
    public static void testPlanInsert() {
        System.out.println("🧪 PRUEBA DIRECTA DE INSERT EN PLANES_PRACTICA");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            
            // 1. Primero verificar la estructura EXACTA de la tabla
            System.out.println("📋 COLUMNAS DE LA TABLA:");
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "planes_practica", "%");
            
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");
                String isNullable = columns.getString("IS_NULLABLE");
                String defaultValue = columns.getString("COLUMN_DEF");
                
                System.out.printf("  - %-25s %s %s %s%n", 
                    columnName, 
                    dataType,
                    "YES".equals(isNullable) ? "NULL" : "NOT NULL",
                    defaultValue != null ? "DEFAULT " + defaultValue : ""
                );
            }
            
            // 2. Probar INSERT manual paso a paso
            System.out.println("\n🔍 INTENTANDO INSERT MANUAL:");
            
            // Buscar estudiante válido
            String findStudentSQL = "SELECT id FROM usuarios WHERE tipo = 'estudiante' LIMIT 1";
            int estudianteId = 0;
            try (PreparedStatement stmt = conn.prepareStatement(findStudentSQL)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    estudianteId = rs.getInt("id");
                    System.out.println("  Estudiante encontrado ID: " + estudianteId);
                }
            }
            
            if (estudianteId == 0) {
                System.out.println("  ❌ No se encontró ningún estudiante");
                return;
            }
            
            // 3. INSERT ultra-mínimo para verificar
            String insertSQL = "INSERT INTO planes_practica (estudiante_id) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
                stmt.setInt(1, estudianteId);
                int rows = stmt.executeUpdate();
                System.out.println("  ✅ INSERT mínimo exitoso, filas: " + rows);
                
                // Limpiar el registro de prueba
                String deleteSQL = "DELETE FROM planes_practica WHERE estudiante_id = ? AND empresa IS NULL";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {
                    deleteStmt.setInt(1, estudianteId);
                    deleteStmt.executeUpdate();
                    System.out.println("  🧹 Registro de prueba eliminado");
                }
                
            } catch (SQLException e) {
                System.err.println("  ❌ Error en INSERT mínimo: " + e.getMessage());
            }
            
            // 4. Probar con campos básicos
            System.out.println("\n🔍 INTENTANDO INSERT CON CAMPOS BÁSICOS:");
            String insertFullSQL = """
                INSERT INTO planes_practica (estudiante_id, empresa, actividades, objetivos, estado) 
                VALUES (?, ?, ?, ?, ?)
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(insertFullSQL)) {
                stmt.setInt(1, estudianteId);
                stmt.setString(2, "Empresa de Prueba");
                stmt.setString(3, "Actividades de prueba");
                stmt.setString(4, "Objetivos de prueba");
                stmt.setString(5, "borrador");
                
                int rows = stmt.executeUpdate();
                System.out.println("  ✅ INSERT completo exitoso, filas: " + rows);
                
                // Limpiar
                String deleteSQL = "DELETE FROM planes_practica WHERE estudiante_id = ? AND empresa = 'Empresa de Prueba'";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {
                    deleteStmt.setInt(1, estudianteId);
                    deleteStmt.executeUpdate();
                    System.out.println("  🧹 Registro de prueba eliminado");
                }
                
            } catch (SQLException e) {
                System.err.println("  ❌ Error en INSERT completo: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error general: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        testPlanInsert();
    }
}