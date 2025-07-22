package com.syspre.utils;

import com.syspre.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseHelper {
    
    /**
     * Crear usuario administrador por defecto si no existe
     */
    public static void ensureAdminUserExists() {
        String email = "admin@syspre.com";
        String password = "test123";
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Verificar si ya existe admin
            String checkSql = "SELECT COUNT(*) FROM usuarios WHERE email = ? AND tipo = 'admin'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("✅ Usuario admin ya existe: " + email);
                    return;
                }
            }
            
            // Crear admin
            String sql = """
                INSERT INTO usuarios (nombres, apellidos, email, password, tipo) 
                VALUES (?, ?, ?, ?, ?)
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "Administrador");
                stmt.setString(2, "Sistema");
                stmt.setString(3, email);
                stmt.setString(4, BCrypt.hashpw(password, BCrypt.gensalt()));
                stmt.setString(5, "admin");
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("✅ Usuario admin creado exitosamente: " + email + " / " + password);
                } else {
                    System.err.println("⚠️ No se pudo crear el usuario admin");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error creando usuario admin: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Crear usuarios de prueba para desarrollo
     */
    public static void createTestUsers() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            createTestUser(conn, "coordinador@syspre.com", "test123", "coordinador", "Coordinador", "Prueba");
            createTestUser(conn, "docente@syspre.com", "test123", "docente", "Docente", "Prueba");  
            createTestUser(conn, "estudiante@syspre.com", "test123", "estudiante", "Estudiante", "Prueba");
            
        } catch (SQLException e) {
            System.err.println("Error creando usuarios de prueba: " + e.getMessage());
        }
    }
    
    private static void createTestUser(Connection conn, String email, String password, String tipo, String nombres, String apellidos) {
        try {
            // Verificar si ya existe
            String checkSql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return; // Ya existe
                }
            }
            
            // Crear usuario
            String sql;
            if ("estudiante".equals(tipo)) {
                sql = """
                    INSERT INTO usuarios (codigo, nombres, apellidos, email, password, tipo, especialidad, semestre) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
            } else {
                sql = """
                    INSERT INTO usuarios (nombres, apellidos, email, password, tipo, especialidad) 
                    VALUES (?, ?, ?, ?, ?, ?)
                """;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if ("estudiante".equals(tipo)) {
                    stmt.setString(1, "2020123456");
                    stmt.setString(2, nombres);
                    stmt.setString(3, apellidos);
                    stmt.setString(4, email);
                    stmt.setString(5, BCrypt.hashpw(password, BCrypt.gensalt()));
                    stmt.setString(6, tipo);
                    stmt.setString(7, "Ingeniería de Sistemas");
                    stmt.setInt(8, 8);
                } else {
                    stmt.setString(1, nombres);
                    stmt.setString(2, apellidos);
                    stmt.setString(3, email);
                    stmt.setString(4, BCrypt.hashpw(password, BCrypt.gensalt()));
                    stmt.setString(5, tipo);
                    stmt.setString(6, "Ingeniería de Sistemas");
                }
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("✅ Usuario " + tipo + " creado: " + email);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error creando usuario " + tipo + ": " + e.getMessage());
        }
    }
    
    /**
     * Verificar conexión a base de datos
     */
    public static boolean testConnection() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Error verificando conexión DB: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Limpiar datos de prueba (opcional para testing)
     */
    public static void clearTestData() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String[] testEmails = {
                "coordinador@test.com", "docente@test.com", "estudiante@test.com"
            };
            
            String sql = "DELETE FROM usuarios WHERE email = ?";
            for (String email : testEmails) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, email);
                    stmt.executeUpdate();
                }
            }
            System.out.println("🧹 Datos de prueba eliminados");
            
        } catch (SQLException e) {
            System.err.println("Error limpiando datos de prueba: " + e.getMessage());
        }
    }
}