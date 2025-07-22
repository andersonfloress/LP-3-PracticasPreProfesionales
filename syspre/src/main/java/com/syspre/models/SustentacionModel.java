package com.syspre.models;

import com.syspre.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SustentacionModel {
    
    public boolean programar(Map<String, Object> data) {
        String sql = """
            INSERT INTO sustentaciones (estudiante_id, fecha_programada, lugar, presidente_jurado_id, 
                                      vocal_jurado_id, secretario_jurado_id, estado) 
            VALUES (?, ?, ?, ?, ?, ?, 'programada')
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, (Integer) data.get("estudiante_id"));
            stmt.setTimestamp(2, Timestamp.valueOf((LocalDateTime) data.get("fecha_programada")));
            stmt.setString(3, (String) data.get("lugar"));
            stmt.setInt(4, (Integer) data.get("presidente_jurado_id"));
            stmt.setInt(5, (Integer) data.get("vocal_jurado_id"));
            stmt.setInt(6, (Integer) data.get("secretario_jurado_id"));
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error programando sustentación: " + e.getMessage());
            return false;
        }
    }

    public boolean create(Map<String, Object> data) {
        String sql = """
            INSERT INTO sustentaciones (estudiante_id, fecha_programada, lugar, presidente_jurado_id, 
                                      vocal_jurado_id, secretario_jurado_id, estado) 
            VALUES (?, ?, ?, ?, ?, ?, 'programada')
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, (Integer) data.get("estudiante_id"));
            stmt.setString(2, (String) data.get("fecha_programada"));
            stmt.setString(3, (String) data.get("lugar"));
            stmt.setInt(4, (Integer) data.get("presidente_jurado_id"));
            stmt.setInt(5, (Integer) data.get("vocal_jurado_id"));
            stmt.setInt(6, (Integer) data.get("secretario_jurado_id"));
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creando sustentación: " + e.getMessage());
            return false;
        }
    }
    
    public List<Map<String, Object>> findEstudiantesListosParaSustentacion() {
        String sql = """
            SELECT u.id, u.nombres, u.apellidos, u.email, u.especialidad
            FROM usuarios u
            JOIN informes_finales i ON u.id = i.estudiante_id
            WHERE u.tipo = 'estudiante' 
            AND i.estado = 'aprobado_coordinador'
            AND u.id NOT IN (SELECT estudiante_id FROM sustentaciones)
            ORDER BY u.especialidad, u.nombres, u.apellidos
        """;
        
        List<Map<String, Object>> estudiantes = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> estudiante = new HashMap<>();
                estudiante.put("id", rs.getInt("id"));
                estudiante.put("nombres", rs.getString("nombres"));
                estudiante.put("apellidos", rs.getString("apellidos"));
                estudiante.put("email", rs.getString("email"));
                estudiante.put("especialidad", rs.getString("especialidad"));
                estudiantes.add(estudiante);
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando estudiantes listos para sustentación: " + e.getMessage());
        }
        
        return estudiantes;
    }
    
    public List<Map<String, Object>> findSustentacionesProgramadas() {
        String sql = """
            SELECT s.*, e.nombres as estudiante_nombres, e.apellidos as estudiante_apellidos,
                   p.nombres as presidente_nombres, p.apellidos as presidente_apellidos,
                   v.nombres as vocal_nombres, v.apellidos as vocal_apellidos,
                   sec.nombres as secretario_nombres, sec.apellidos as secretario_apellidos
            FROM sustentaciones s
            JOIN usuarios e ON s.estudiante_id = e.id
            JOIN usuarios p ON s.presidente_jurado_id = p.id
            JOIN usuarios v ON s.vocal_jurado_id = v.id
            JOIN usuarios sec ON s.secretario_jurado_id = sec.id
            WHERE s.estado = 'programada'
            ORDER BY s.fecha_programada ASC
        """;
        
        return executeSustentacionQuery(sql);
    }
    
    public List<Map<String, Object>> findByDocenteJurado(int docenteId) {
        String sql = """
            SELECT s.*, e.nombres as estudiante_nombres, e.apellidos as estudiante_apellidos,
                   p.nombres as presidente_nombres, p.apellidos as presidente_apellidos,
                   v.nombres as vocal_nombres, v.apellidos as vocal_apellidos,
                   sec.nombres as secretario_nombres, sec.apellidos as secretario_apellidos
            FROM sustentaciones s
            JOIN usuarios e ON s.estudiante_id = e.id
            JOIN usuarios p ON s.presidente_jurado_id = p.id
            JOIN usuarios v ON s.vocal_jurado_id = v.id
            JOIN usuarios sec ON s.secretario_jurado_id = sec.id
            WHERE (s.presidente_jurado_id = ? OR s.vocal_jurado_id = ? OR s.secretario_jurado_id = ?)
            AND s.estado IN ('programada', 'realizada')
            ORDER BY s.fecha_programada ASC
        """;
        
        return executeSustentacionQuery(sql, docenteId, docenteId, docenteId);
    }
    
    public Map<String, Object> findById(int sustentacionId) {
        String sql = """
            SELECT s.*, e.nombres as estudiante_nombres, e.apellidos as estudiante_apellidos,
                   p.nombres as presidente_nombres, p.apellidos as presidente_apellidos,
                   v.nombres as vocal_nombres, v.apellidos as vocal_apellidos,
                   sec.nombres as secretario_nombres, sec.apellidos as secretario_apellidos
            FROM sustentaciones s
            JOIN usuarios e ON s.estudiante_id = e.id
            JOIN usuarios p ON s.presidente_jurado_id = p.id
            JOIN usuarios v ON s.vocal_jurado_id = v.id
            JOIN usuarios sec ON s.secretario_jurado_id = sec.id
            WHERE s.id = ?
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sustentacionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapSustentacionFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando sustentación por ID: " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean isDocenteAsignado(int sustentacionId, int docenteId) {
        String sql = """
            SELECT COUNT(*) as total FROM sustentaciones 
            WHERE id = ? AND (presidente_jurado_id = ? OR vocal_jurado_id = ? OR secretario_jurado_id = ?)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sustentacionId);
            stmt.setInt(2, docenteId);
            stmt.setInt(3, docenteId);
            stmt.setInt(4, docenteId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error verificando asignación de docente: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean calificar(int sustentacionId, double calificacionFinal, String observaciones) {
        String sql = """
            UPDATE sustentaciones 
            SET calificacion_final = ?, observaciones = ?, estado = 'realizada' 
            WHERE id = ?
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, calificacionFinal);
            stmt.setString(2, observaciones);
            stmt.setInt(3, sustentacionId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error calificando sustentación: " + e.getMessage());
            return false;
        }
    }
    
    private List<Map<String, Object>> executeSustentacionQuery(String sql, Object... params) {
        List<Map<String, Object>> sustentaciones = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                sustentaciones.add(mapSustentacionFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error ejecutando consulta de sustentaciones: " + e.getMessage());
        }
        
        return sustentaciones;
    }
    
    private Map<String, Object> mapSustentacionFromResultSet(ResultSet rs) throws SQLException {
        Map<String, Object> sustentacion = new HashMap<>();
        sustentacion.put("id", rs.getInt("id"));
        sustentacion.put("estudiante_id", rs.getInt("estudiante_id"));
        sustentacion.put("fecha_programada", rs.getTimestamp("fecha_programada"));
        sustentacion.put("lugar", rs.getString("lugar"));
        sustentacion.put("presidente_jurado_id", rs.getInt("presidente_jurado_id"));
        sustentacion.put("vocal_jurado_id", rs.getInt("vocal_jurado_id"));
        sustentacion.put("secretario_jurado_id", rs.getInt("secretario_jurado_id"));
        sustentacion.put("calificacion_final", rs.getObject("calificacion_final"));
        sustentacion.put("observaciones", rs.getString("observaciones"));
        sustentacion.put("estado", rs.getString("estado"));
        sustentacion.put("created_at", rs.getTimestamp("created_at"));
        
        // Datos de estudiante
        try {
            sustentacion.put("estudiante_nombres", rs.getString("estudiante_nombres"));
            sustentacion.put("estudiante_apellidos", rs.getString("estudiante_apellidos"));
        } catch (SQLException ignored) {}
        
        // Datos del jurado
        try {
            sustentacion.put("presidente_nombres", rs.getString("presidente_nombres"));
            sustentacion.put("presidente_apellidos", rs.getString("presidente_apellidos"));
            sustentacion.put("vocal_nombres", rs.getString("vocal_nombres"));
            sustentacion.put("vocal_apellidos", rs.getString("vocal_apellidos"));
            sustentacion.put("secretario_nombres", rs.getString("secretario_nombres"));
            sustentacion.put("secretario_apellidos", rs.getString("secretario_apellidos"));
        } catch (SQLException ignored) {}
        
        return sustentacion;
    }
}