package com.syspre.models;

import com.syspre.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InformeFinalModel {
    
    public boolean create(Map<String, Object> data) {
        String sql = """
            INSERT INTO informes_finales (estudiante_id, resumen_ejecutivo, metodologia, resultados,
                                        conclusiones, recomendaciones, archivo_informe, estado) 
            VALUES (?, ?, ?, ?, ?, ?, ?, 'borrador')
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, (Integer) data.get("estudiante_id"));
            stmt.setString(2, (String) data.get("resumen_ejecutivo"));
            stmt.setString(3, (String) data.get("metodologia"));
            stmt.setString(4, (String) data.get("resultados"));
            stmt.setString(5, (String) data.get("conclusiones"));
            stmt.setString(6, (String) data.get("recomendaciones"));
            stmt.setString(7, (String) data.get("archivo_informe"));
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creando informe final: " + e.getMessage());
            return false;
        }
    }
    
    public boolean update(int informeId, Map<String, Object> data) {
        String sql = """
            UPDATE informes_finales 
            SET resumen_ejecutivo = ?, metodologia = ?, resultados = ?, 
                conclusiones = ?, recomendaciones = ?, archivo_informe = ?
            WHERE id = ? AND estado = 'borrador'
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, (String) data.get("resumen_ejecutivo"));
            stmt.setString(2, (String) data.get("metodologia"));
            stmt.setString(3, (String) data.get("resultados"));
            stmt.setString(4, (String) data.get("conclusiones"));
            stmt.setString(5, (String) data.get("recomendaciones"));
            stmt.setString(6, (String) data.get("archivo_informe"));
            stmt.setInt(7, informeId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error actualizando informe final: " + e.getMessage());
            return false;
        }
    }
    
    public Map<String, Object> findByEstudianteId(int estudianteId) {
        String sql = "SELECT * FROM informes_finales WHERE estudiante_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, estudianteId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapInformeFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando informe: " + e.getMessage());
        }
        
        return null;
    }
    
    public Map<String, Object> findById(int informeId) {
        String sql = "SELECT * FROM informes_finales WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, informeId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapInformeFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando informe por ID: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<Map<String, Object>> findPendientesParaDocente() {
        String sql = """
            SELECT i.*, u.nombres, u.apellidos, u.email 
            FROM informes_finales i 
            JOIN usuarios u ON i.estudiante_id = u.id 
            WHERE i.estado = 'enviado'
            ORDER BY i.id ASC
        """;
        
        return executeInformeQueryWithStudent(sql);
    }
    
    public List<Map<String, Object>> findPendientesParaCoordinador() {
        String sql = """
            SELECT i.*, u.nombres, u.apellidos, u.email 
            FROM informes_finales i 
            JOIN usuarios u ON i.estudiante_id = u.id 
            WHERE i.estado = 'calificado_docente'
            ORDER BY i.id ASC
        """;
        
        return executeInformeQueryWithStudent(sql);
    }
    
    public boolean enviarInforme(int informeId) {
        return updateEstado(informeId, "enviado");
    }
    
    public boolean calificarPorDocente(int informeId, double calificacion, String comentarios) {
        String sql = """
            UPDATE informes_finales 
            SET calificacion_docente = ?, comentarios_docente = ?, estado = 'calificado_docente' 
            WHERE id = ?
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, calificacion);
            stmt.setString(2, comentarios);
            stmt.setInt(3, informeId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error calificando informe por docente: " + e.getMessage());
            return false;
        }
    }
    
    public boolean aprobarPorCoordinador(int informeId, double calificacion, String comentarios) {
        String sql = """
            UPDATE informes_finales 
            SET calificacion_coordinador = ?, comentarios_coordinador = ?, estado = 'aprobado_coordinador' 
            WHERE id = ?
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, calificacion);
            stmt.setString(2, comentarios);
            stmt.setInt(3, informeId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error aprobando informe por coordinador: " + e.getMessage());
            return false;
        }
    }
    
    private boolean updateEstado(int informeId, String nuevoEstado) {
        String sql = "UPDATE informes_finales SET estado = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, informeId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error actualizando estado del informe: " + e.getMessage());
            return false;
        }
    }
    
    private List<Map<String, Object>> executeInformeQueryWithStudent(String sql, Object... params) {
        List<Map<String, Object>> informes = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> informe = mapInformeFromResultSet(rs);
                
                // Agregar datos del estudiante
                try {
                    informe.put("estudiante_nombres", rs.getString("nombres"));
                    informe.put("estudiante_apellidos", rs.getString("apellidos"));
                    informe.put("estudiante_email", rs.getString("email"));
                } catch (SQLException ignored) {
                    // Campos no disponibles en todas las consultas
                }
                
                informes.add(informe);
            }
            
        } catch (SQLException e) {
            System.err.println("Error ejecutando consulta de informes: " + e.getMessage());
        }
        
        return informes;
    }
    
    private Map<String, Object> mapInformeFromResultSet(ResultSet rs) throws SQLException {
        Map<String, Object> informe = new HashMap<>();
        informe.put("id", rs.getInt("id"));
        informe.put("estudiante_id", rs.getInt("estudiante_id"));
        informe.put("resumen_ejecutivo", rs.getString("resumen_ejecutivo"));
        informe.put("metodologia", rs.getString("metodologia"));
        informe.put("resultados", rs.getString("resultados"));
        informe.put("conclusiones", rs.getString("conclusiones"));
        informe.put("recomendaciones", rs.getString("recomendaciones"));
        informe.put("archivo_informe", rs.getString("archivo_informe"));
        informe.put("calificacion_docente", rs.getObject("calificacion_docente"));
        informe.put("calificacion_coordinador", rs.getObject("calificacion_coordinador"));
        informe.put("comentarios_docente", rs.getString("comentarios_docente"));
        informe.put("comentarios_coordinador", rs.getString("comentarios_coordinador"));
        informe.put("estado", rs.getString("estado"));
        informe.put("created_at", rs.getTimestamp("created_at"));
        informe.put("updated_at", rs.getTimestamp("updated_at"));
        return informe;
    }
}