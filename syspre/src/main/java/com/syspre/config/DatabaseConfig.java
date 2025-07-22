package com.syspre.config;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String DB_HOST = "www.createxpro.net.pe";
    private static final String DB_NAME = "bpuma_userMysql11";
    private static final String DB_USER = "bpuma_userMysql11";
    private static final String DB_PASSWORD = "4*=K45&KiioEi23";
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":3306/" + DB_NAME + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    
    private static Jdbi jdbi;
    
    public static void initDatabase() {
        try {
            jdbi = Jdbi.create(DB_URL, DB_USER, DB_PASSWORD);
            jdbi.installPlugin(new SqlObjectPlugin());
            
            System.out.println("✅ Conexión a base de datos establecida");
            
            // Opcional: limpiar base de datos solo si es necesario
            // clearDatabase();
        } catch (Exception e) {
            System.err.println("❌ Error conectando a la base de datos: " + e.getMessage());
            e.printStackTrace();
            // Continuar sin base de datos para permitir que el servidor se inicie
            System.out.println("⚠️ Servidor iniciará sin conexión a base de datos");
        }
    }
    
    private static void clearDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            // Desactivar verificación de llaves foráneas
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // Obtener todas las tablas
            var rs = stmt.executeQuery("SHOW TABLES");
            while (rs.next()) {
                String tableName = rs.getString(1);
                stmt.execute("DROP TABLE IF EXISTS " + tableName);
                System.out.println("🗑️ Tabla eliminada: " + tableName);
            }
            
            // Reactivar verificación de llaves foráneas
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            
            System.out.println("🧹 Base de datos limpiada completamente");
            
        } catch (SQLException e) {
            System.err.println("Error limpiando la base de datos: " + e.getMessage());
        }
    }
    
    public static Jdbi getJdbi() {
        return jdbi;
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}