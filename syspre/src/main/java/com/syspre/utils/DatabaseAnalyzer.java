package com.syspre.utils;

import com.syspre.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAnalyzer {
    
    public static void analyzeAndCreateMissingTables() {
        System.out.println("🔍 ANÁLISIS COMPLETO DE BASE DE DATOS SYSPRE");
        System.out.println("============================================");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // 1. Verificar tablas existentes
            showExistingTables(conn);
            
            // 2. Verificar estructura de tablas críticas
            analyzeTableStructure(conn, "usuarios");
            analyzeTableStructure(conn, "planes_practica");
            analyzeTableStructure(conn, "reportes_semanales");
            analyzeTableStructure(conn, "informes_finales");
            analyzeTableStructure(conn, "asignaciones_docente");
            analyzeTableStructure(conn, "sustentaciones");
            analyzeTableStructure(conn, "documentos_reglamento");
            
            // 3. Verificar usuarios de prueba
            checkTestUsers(conn);
            
            // 4. Crear tablas faltantes si es necesario
            createMissingTables(conn);
            
        } catch (SQLException e) {
            System.err.println("❌ Error analizando base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void showExistingTables(Connection conn) throws SQLException {
        System.out.println("\n📋 TABLAS EXISTENTES:");
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        
        List<String> tableNames = new ArrayList<>();
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            tableNames.add(tableName);
            System.out.println("  ✅ " + tableName);
        }
        
        System.out.println("Total de tablas: " + tableNames.size());
    }
    
    private static void analyzeTableStructure(Connection conn, String tableName) throws SQLException {
        System.out.println("\n🔎 ESTRUCTURA DE TABLA: " + tableName.toUpperCase());
        
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, "%");
            
            boolean tableExists = false;
            while (columns.next()) {
                tableExists = true;
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                String isNullable = columns.getString("IS_NULLABLE");
                String defaultValue = columns.getString("COLUMN_DEF");
                
                System.out.printf("  - %-25s %s(%d) %s %s%n", 
                    columnName, 
                    dataType, 
                    columnSize,
                    "YES".equals(isNullable) ? "NULL" : "NOT NULL",
                    defaultValue != null ? "DEFAULT " + defaultValue : ""
                );
            }
            
            if (!tableExists) {
                System.out.println("  ⚠️ TABLA NO EXISTE - NECESITA SER CREADA");
            }
            
        } catch (SQLException e) {
            System.out.println("  ⚠️ TABLA NO EXISTE O ERROR: " + e.getMessage());
        }
    }
    
    private static void checkTestUsers(Connection conn) throws SQLException {
        System.out.println("\n👥 USUARIOS DE PRUEBA:");
        
        String sql = "SELECT email, tipo FROM usuarios WHERE email LIKE '%@syspre.com' OR email LIKE '%@test.com'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                System.out.println("  ✅ " + rs.getString("email") + " (" + rs.getString("tipo") + ")");
            }
        }
    }
    
    private static void createMissingTables(Connection conn) throws SQLException {
        System.out.println("\n🛠️ CREANDO/VERIFICANDO TABLAS CRÍTICAS:");
        
        // Verificar y crear tabla de evaluaciones si no existe
        createEvaluacionesTable(conn);
        
        // Verificar y crear tabla de configuracion del sistema
        createConfiguracionTable(conn);
        
        // Verificar y crear tabla de notificaciones
        createNotificacionesTable(conn);
        
        // Verificar y crear tabla de historial de cambios
        createHistorialCambiosTable(conn);
        
        System.out.println("✅ Análisis completo finalizado");
    }
    
    private static void createEvaluacionesTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS evaluaciones (
                id INT AUTO_INCREMENT PRIMARY KEY,
                tipo ENUM('plan', 'reporte', 'informe', 'sustentacion') NOT NULL,
                referencia_id INT NOT NULL,
                evaluador_id INT NOT NULL,
                calificacion DECIMAL(4,2),
                comentarios TEXT,
                criterios JSON,
                fecha_evaluacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_tipo_referencia (tipo, referencia_id),
                INDEX idx_evaluador (evaluador_id),
                FOREIGN KEY (evaluador_id) REFERENCES usuarios(id) ON DELETE CASCADE
            )
        """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("  ✅ Tabla evaluaciones verificada/creada");
        }
    }
    
    private static void createConfiguracionTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS configuracion_sistema (
                id INT AUTO_INCREMENT PRIMARY KEY,
                clave VARCHAR(100) UNIQUE NOT NULL,
                valor TEXT,
                descripcion TEXT,
                tipo ENUM('string', 'number', 'boolean', 'json') DEFAULT 'string',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("  ✅ Tabla configuracion_sistema verificada/creada");
            
            // Insertar configuraciones por defecto
            insertDefaultConfigurations(conn);
        }
    }
    
    private static void createNotificacionesTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS notificaciones (
                id INT AUTO_INCREMENT PRIMARY KEY,
                usuario_id INT NOT NULL,
                titulo VARCHAR(200) NOT NULL,
                mensaje TEXT NOT NULL,
                tipo ENUM('info', 'warning', 'success', 'error') DEFAULT 'info',
                leida BOOLEAN DEFAULT FALSE,
                url_accion VARCHAR(500),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_usuario_leida (usuario_id, leida),
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
            )
        """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("  ✅ Tabla notificaciones verificada/creada");
        }
    }
    
    private static void createHistorialCambiosTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS historial_cambios (
                id INT AUTO_INCREMENT PRIMARY KEY,
                tabla VARCHAR(50) NOT NULL,
                registro_id INT NOT NULL,
                accion ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
                usuario_id INT,
                datos_anteriores JSON,
                datos_nuevos JSON,
                ip_address VARCHAR(45),
                user_agent TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_tabla_registro (tabla, registro_id),
                INDEX idx_usuario_fecha (usuario_id, created_at),
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
            )
        """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("  ✅ Tabla historial_cambios verificada/creada");
        }
    }
    
    private static void insertDefaultConfigurations(Connection conn) throws SQLException {
        String[] configs = {
            "('min_reportes_semanales', '8', 'Número mínimo de reportes semanales requeridos', 'number')",
            "('horas_practica_minimas', '240', 'Horas mínimas de práctica requeridas', 'number')",
            "('calificacion_aprobatoria', '11.0', 'Calificación mínima para aprobar', 'number')",
            "('periodo_academico_actual', '2025-I', 'Periodo académico actual', 'string')",
            "('sistema_activo', 'true', 'Estado del sistema', 'boolean')"
        };
        
        for (String config : configs) {
            String sql = "INSERT IGNORE INTO configuracion_sistema (clave, valor, descripcion, tipo) VALUES " + config;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        }
        System.out.println("  ✅ Configuraciones por defecto insertadas");
    }
    
    public static void main(String[] args) {
        analyzeAndCreateMissingTables();
    }
}