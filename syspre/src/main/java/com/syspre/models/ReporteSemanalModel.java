package com.syspre.models;

import com.syspre.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReporteSemanalModel {
    
    public boolean create(Map<String, Object> data) {
        String sql = """
            INSERT INTO reportes_semanales (estudiante_id, semana_numero, fecha_inicio, fecha_fin,
                                          actividades_realizadas, horas_trabajadas, aprendizajes, 
                                          dificultades, archivo_reporte) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, (Integer) data.get("estudiante_id"));
            stmt.setInt(2, (Integer) data.get("semana_numero"));
            stmt.setDate(3, Date.valueOf((String) data.get("fecha_inicio")));
            stmt.setDate(4, Date.valueOf((String) data.get("fecha_fin")));
            stmt.setString(5, (String) data.get("actividades_realizadas"));
            stmt.setInt(6, (Integer) data.get("horas_trabajadas"));
            stmt.setString(7, (String) data.get("aprendizajes"));
            stmt.setString(8, (String) data.get("dificultades"));
            stmt.setString(9, (String) data.get("archivo_reporte"));
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creando reporte semanal: " + e.getMessage());
            return false;
        }
    }
    
    public boolean update(int reporteId, Map<String, Object> data) {
        String sql = """
            UPDATE reportes_semanales 
            SET fecha_inicio = ?, fecha_fin = ?, actividades_realizadas = ?, 
                horas_trabajadas = ?, aprendizajes = ?, dificultades = ?, archivo_reporte = ?
            WHERE id = ?
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf((String) data.get("fecha_inicio")));
            stmt.setDate(2, Date.valueOf((String) data.get("fecha_fin")));
            stmt.setString(3, (String) data.get("actividades_realizadas"));
            stmt.setInt(4, (Integer) data.get("horas_trabajadas"));
            stmt.setString(5, (String) data.get("aprendizajes"));
            stmt.setString(6, (String) data.get("dificultades"));
            stmt.setString(7, (String) data.get("archivo_reporte"));
            stmt.setInt(8, reporteId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error actualizando reporte semanal: " + e.getMessage());
            return false;
        }
    }
    
    public List<Map<String, Object>> findByEstudianteId(int estudianteId) {
        String sql = """
            SELECT * FROM reportes_semanales 
            WHERE estudiante_id = ? 
            ORDER BY semana_numero ASC
        """;
        
        return executeReporteQuery(sql, estudianteId);
    }
    
    public Map<String, Object> findById(int reporteId) {
        String sql = "SELECT * FROM reportes_semanales WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, reporteId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapReporteFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando reporte: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<Map<String, Object>> findPendientesParaCalificar() {
        String sql = """
            SELECT r.*, u.nombres, u.apellidos, u.email 
            FROM reportes_semanales r 
            JOIN usuarios u ON r.estudiante_id = u.id 
            WHERE r.calificacion IS NULL
            ORDER BY r.semana_numero ASC
        """;
        
        return executeReporteQueryWithStudent(sql);
    }
    
    public List<Map<String, Object>> findByEstudianteIdForDocente(int estudianteId) {
        String sql = """
            SELECT r.*, u.nombres, u.apellidos, u.email 
            FROM reportes_semanales r 
            JOIN usuarios u ON r.estudiante_id = u.id 
            WHERE r.estudiante_id = ?
            ORDER BY r.semana_numero ASC
        """;
        
        return executeReporteQueryWithStudent(sql, estudianteId);
    }
    
    public boolean calificar(int reporteId, double calificacion, String comentarios) {
        String sql = "UPDATE reportes_semanales SET calificacion = ?, comentarios_docente = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, calificacion);
            stmt.setString(2, comentarios);
            stmt.setInt(3, reporteId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error calificando reporte: " + e.getMessage());
            return false;
        }
    }
    
    public int getNextSemanaNumber(int estudianteId) {
        String sql = "SELECT MAX(semana_numero) FROM reportes_semanales WHERE estudiante_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, estudianteId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo próximo número de semana: " + e.getMessage());
        }
        
        return 1; // Primera semana
    }
    
    private List<Map<String, Object>> executeReporteQuery(String sql, Object... params) {
        List<Map<String, Object>> reportes = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                reportes.add(mapReporteFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error ejecutando consulta de reportes: " + e.getMessage());
        }
        
        return reportes;
    }
    
    private List<Map<String, Object>> executeReporteQueryWithStudent(String sql, Object... params) {
        List<Map<String, Object>> reportes = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> reporte = mapReporteFromResultSet(rs);
                
                // Agregar datos del estudiante
                try {
                    reporte.put("estudiante_nombres", rs.getString("nombres"));
                    reporte.put("estudiante_apellidos", rs.getString("apellidos"));
                    reporte.put("estudiante_email", rs.getString("email"));
                } catch (SQLException ignored) {
                    // Campos no disponibles en todas las consultas
                }
                
                reportes.add(reporte);
            }
            
        } catch (SQLException e) {
            System.err.println("Error ejecutando consulta de reportes con estudiante: " + e.getMessage());
        }
        
        return reportes;
    }
    
    private Map<String, Object> mapReporteFromResultSet(ResultSet rs) throws SQLException {
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("id", rs.getInt("id"));
        reporte.put("estudiante_id", rs.getInt("estudiante_id"));
        reporte.put("semana_numero", rs.getInt("semana_numero"));
        reporte.put("fecha_inicio", rs.getDate("fecha_inicio"));
        reporte.put("fecha_fin", rs.getDate("fecha_fin"));
        reporte.put("actividades_realizadas", rs.getString("actividades_realizadas"));
        reporte.put("horas_trabajadas", rs.getInt("horas_trabajadas"));
        reporte.put("aprendizajes", rs.getString("aprendizajes"));
        reporte.put("dificultades", rs.getString("dificultades"));
        reporte.put("archivo_reporte", rs.getString("archivo_reporte"));
        reporte.put("calificacion", rs.getObject("calificacion"));
        reporte.put("comentarios_docente", rs.getString("comentarios_docente"));
        
        // Manejar campos de fecha de manera segura
        try {
            reporte.put("created_at", rs.getTimestamp("created_at"));
        } catch (SQLException e) {
            reporte.put("created_at", null);
        }
        
        try {
            reporte.put("updated_at", rs.getTimestamp("updated_at"));
        } catch (SQLException e) {
            reporte.put("updated_at", null);
        }
        
        return reporte;
    }
}