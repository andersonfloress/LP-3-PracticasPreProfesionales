package com.syspre.utils;

import com.syspre.config.DatabaseConfig;
import java.sql.*;

public class DatabaseDebugger {
    
    public static void analyzeTableStructure() {
        System.out.println("\n🔍 ANALIZANDO ESTRUCTURA DE PLANES_PRACTICA:");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Verificar estructura de tabla
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "planes_practica", "%");
            
            System.out.println("📋 COLUMNAS EXISTENTES:");
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");
                String isNullable = columns.getString("IS_NULLABLE");
                System.out.println("  - " + columnName + " (" + dataType + ", nullable: " + isNullable + ")");
            }
            
            // Verificar datos existentes
            String sql = "SELECT * FROM planes_practica LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("\n📊 EJEMPLO DE REGISTRO:");
                    ResultSetMetaData rsmd = rs.getMetaData();
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        String columnName = rsmd.getColumnName(i);
                        Object value = rs.getObject(i);
                        System.out.println("  " + columnName + " = " + value);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error analizando estructura: " + e.getMessage());
        }
    }
}