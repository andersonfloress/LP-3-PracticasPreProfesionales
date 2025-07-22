package com.syspre.models;

import com.syspre.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsignacionDocenteModel {
    
    public boolean asignarAsesor(int estudianteId, int docenteId, int coordinadorId) {
        return createAsignacion(estudianteId, docenteId, coordinadorId, "asesor");
    }
    
    public boolean asignarJurado(int estudianteId, int presidenteId, int vocalId, int secretarioId, int coordinadorId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Eliminar asignaciones previas de jurado
                String deleteSql = "DELETE FROM asignaciones_docente WHERE estudiante_id = ? AND tipo_asignacion LIKE 'jurado_%'";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, estudianteId);
                    deleteStmt.executeUpdate();
                }
                
                // Crear nuevas asignaciones
                String insertSql = "INSERT INTO asignaciones_docente (estudiante_id, docente_id, coordinador_id, tipo_asignacion) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    // Presidente
                    insertStmt.setInt(1, estudianteId);
                    insertStmt.setInt(2, presidenteId);
                    insertStmt.setInt(3, coordinadorId);
                    insertStmt.setString(4, "jurado_presidente");
                    insertStmt.executeUpdate();
                    
                    // Vocal
                    insertStmt.setInt(2, vocalId);
                    insertStmt.setString(4, "jurado_vocal");
                    insertStmt.executeUpdate();
                    
                    // Secretario
                    insertStmt.setInt(2, secretarioId);
                    insertStmt.setString(4, "jurado_secretario");
                    insertStmt.executeUpdate();
                }
                
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error asignando jurado: " + e.getMessage());
            return false;
        }
    }
    
    // Obtener estudiantes sin asesor asignado
    public List<Map<String, Object>> getEstudiantesSinAsesor() {
        List<Map<String, Object>> estudiantes = new ArrayList<>();
        String sql = """
            SELECT DISTINCT u.id, u.nombres, u.apellidos, u.codigo, u.especialidad, u.email
            FROM usuarios u 
            LEFT JOIN planes_practica p ON u.id = p.estudiante_id
            LEFT JOIN asignaciones_docente a ON u.id = a.estudiante_id AND a.tipo_asignacion = 'asesor'
            WHERE u.tipo = 'estudiante' 
            AND p.id IS NOT NULL 
            AND a.id IS NULL
            ORDER BY u.apellidos, u.nombres
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> estudiante = new HashMap<>();
                estudiante.put("id", rs.getInt("id"));
                estudiante.put("nombres", rs.getString("nombres"));
                estudiante.put("apellidos", rs.getString("apellidos"));
                estudiante.put("codigo", rs.getString("codigo"));
                estudiante.put("especialidad", rs.getString("especialidad"));
                estudiante.put("email", rs.getString("email"));
                estudiantes.add(estudiante);
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo estudiantes sin asesor: " + e.getMessage());
        }
        
        return estudiantes;
    }
    
    // Obtener asignaciones por coordinador
    public List<Map<String, Object>> getAsignacionesByCoordinador(int coordinadorId) {
        List<Map<String, Object>> asignaciones = new ArrayList<>();
        String sql = """
            SELECT a.id, a.tipo_asignacion,
                   u_est.nombres as estudiante_nombres, u_est.apellidos as estudiante_apellidos, u_est.codigo as estudiante_codigo,
                   u_doc.nombres as docente_nombres, u_doc.apellidos as docente_apellidos
            FROM asignaciones_docente a
            JOIN usuarios u_est ON a.estudiante_id = u_est.id
            JOIN usuarios u_doc ON a.docente_id = u_doc.id
            WHERE a.coordinador_id = ?
            ORDER BY a.id DESC
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, coordinadorId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> asignacion = new HashMap<>();
                    asignacion.put("id", rs.getInt("id"));
                    asignacion.put("tipo_asignacion", rs.getString("tipo_asignacion"));
                    asignacion.put("estudiante_nombres", rs.getString("estudiante_nombres"));
                    asignacion.put("estudiante_apellidos", rs.getString("estudiante_apellidos"));
                    asignacion.put("estudiante_codigo", rs.getString("estudiante_codigo"));
                    asignacion.put("docente_nombres", rs.getString("docente_nombres"));
                    asignacion.put("docente_apellidos", rs.getString("docente_apellidos"));
                    asignaciones.add(asignacion);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo asignaciones: " + e.getMessage());
        }
        
        return asignaciones;
    }
    
    // Obtener planes asignados a un docente
    public List<Map<String, Object>> getPlanesByDocente(int docenteId) {
        List<Map<String, Object>> planes = new ArrayList<>();
        String sql = """
            SELECT p.id, p.empresa, p.ruc, p.estado, p.fecha_creacion,
                   u.nombres as estudiante_nombres, u.apellidos as estudiante_apellidos, u.codigo as estudiante_codigo,
                   a.tipo_asignacion
            FROM planes_practica p
            JOIN asignaciones_docente a ON p.estudiante_id = a.estudiante_id
            JOIN usuarios u ON p.estudiante_id = u.id
            WHERE a.docente_id = ? AND a.tipo_asignacion = 'asesor'
            ORDER BY p.fecha_creacion DESC
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, docenteId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> plan = new HashMap<>();
                    plan.put("id", rs.getInt("id"));
                    plan.put("empresa", rs.getString("empresa"));
                    plan.put("ruc", rs.getString("ruc"));
                    plan.put("estado", rs.getString("estado"));
                    plan.put("fecha_creacion", rs.getTimestamp("fecha_creacion"));
                    plan.put("estudiante_nombres", rs.getString("estudiante_nombres"));
                    plan.put("estudiante_apellidos", rs.getString("estudiante_apellidos"));
                    plan.put("estudiante_codigo", rs.getString("estudiante_codigo"));
                    plan.put("tipo_asignacion", rs.getString("tipo_asignacion"));
                    planes.add(plan);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo planes por docente: " + e.getMessage());
        }
        
        return planes;
    }
    
    // Verificar si docente está asignado a un plan
    public boolean docenteAsignadoAPlan(int docenteId, int planId) {
        String sql = """
            SELECT COUNT(*) as count
            FROM asignaciones_docente a
            JOIN planes_practica p ON a.estudiante_id = p.estudiante_id
            WHERE a.docente_id = ? AND p.id = ? AND a.tipo_asignacion = 'asesor'
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, docenteId);
            stmt.setInt(2, planId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error verificando asignación docente-plan: " + e.getMessage());
        }
        
        return false;
    }
    
    private boolean createAsignacion(int estudianteId, int docenteId, int coordinadorId, String tipoAsignacion) {
        String sql = "INSERT INTO asignaciones_docente (estudiante_id, docente_id, coordinador_id, tipo_asignacion) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, estudianteId);
            stmt.setInt(2, docenteId);
            stmt.setInt(3, coordinadorId);
            stmt.setString(4, tipoAsignacion);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creando asignación: " + e.getMessage());
            return false;
        }
    }
    
    public Map<String, Object> findAsesorByEstudianteId(int estudianteId) {
        String sql = """
            SELECT a.*, d.nombres as docente_nombres, d.apellidos as docente_apellidos, d.email as docente_email
            FROM asignaciones_docente a
            JOIN usuarios d ON a.docente_id = d.id
            WHERE a.estudiante_id = ? AND a.tipo_asignacion = 'asesor'
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, estudianteId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, Object> asignacion = new HashMap<>();
                asignacion.put("id", rs.getInt("id"));
                asignacion.put("estudiante_id", rs.getInt("estudiante_id"));
                asignacion.put("docente_id", rs.getInt("docente_id"));
                asignacion.put("coordinador_id", rs.getInt("coordinador_id"));
                asignacion.put("tipo_asignacion", rs.getString("tipo_asignacion"));
                asignacion.put("docente_nombres", rs.getString("docente_nombres"));
                asignacion.put("docente_apellidos", rs.getString("docente_apellidos"));
                asignacion.put("docente_email", rs.getString("docente_email"));
                asignacion.put("created_at", rs.getTimestamp("created_at"));
                return asignacion;
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando asesor: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<Map<String, Object>> findEstudiantesByDocenteId(int docenteId) {
        String sql = """
            SELECT a.*, e.nombres as estudiante_nombres, e.apellidos as estudiante_apellidos, 
                   e.email as estudiante_email, e.especialidad, e.semestre
            FROM asignaciones_docente a
            JOIN usuarios e ON a.estudiante_id = e.id
            WHERE a.docente_id = ? AND a.tipo_asignacion = 'asesor'
            ORDER BY e.nombres, e.apellidos
        """;
        
        return executeAsignacionQuery(sql, docenteId);
    }
    
    public List<Map<String, Object>> findJuradoByEstudianteId(int estudianteId) {
        String sql = """
            SELECT a.*, d.nombres as docente_nombres, d.apellidos as docente_apellidos, d.email as docente_email
            FROM asignaciones_docente a
            JOIN usuarios d ON a.docente_id = d.id
            WHERE a.estudiante_id = ? AND a.tipo_asignacion LIKE 'jurado_%'
            ORDER BY a.tipo_asignacion
        """;
        
        return executeAsignacionQuery(sql, estudianteId);
    }
    
    public List<Map<String, Object>> findEstudiantesSinAsesor() {
        String sql = """
            SELECT u.id, u.nombres, u.apellidos, u.email, u.especialidad, u.semestre
            FROM usuarios u
            WHERE u.tipo = 'estudiante' 
            AND u.id NOT IN (
                SELECT estudiante_id FROM asignaciones_docente WHERE tipo_asignacion = 'asesor'
            )
            ORDER BY u.especialidad, u.nombres, u.apellidos
        """;
        
        return executeEstudianteQuery(sql);
    }
    
    public List<Map<String, Object>> findDocentesDisponibles() {
        String sql = """
            SELECT u.id, u.nombres, u.apellidos, u.email, u.especialidad,
                   COUNT(a.id) as estudiantes_asignados
            FROM usuarios u
            LEFT JOIN asignaciones_docente a ON u.id = a.docente_id AND a.tipo_asignacion = 'asesor'
            WHERE u.tipo IN ('docente', 'coordinador')
            GROUP BY u.id, u.nombres, u.apellidos, u.email, u.especialidad
            ORDER BY estudiantes_asignados ASC, u.nombres, u.apellidos
        """;
        
        return executeDocenteQuery(sql);
    }
    
    private List<Map<String, Object>> executeAsignacionQuery(String sql, Object... params) {
        List<Map<String, Object>> asignaciones = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> asignacion = new HashMap<>();
                asignacion.put("id", rs.getInt("id"));
                asignacion.put("estudiante_id", rs.getInt("estudiante_id"));
                asignacion.put("docente_id", rs.getInt("docente_id"));
                asignacion.put("coordinador_id", rs.getInt("coordinador_id"));
                asignacion.put("tipo_asignacion", rs.getString("tipo_asignacion"));
                asignacion.put("created_at", rs.getTimestamp("created_at"));
                
                // Datos adicionales según la consulta
                try {
                    asignacion.put("docente_nombres", rs.getString("docente_nombres"));
                    asignacion.put("docente_apellidos", rs.getString("docente_apellidos"));
                    asignacion.put("docente_email", rs.getString("docente_email"));
                } catch (SQLException ignored) {}
                
                try {
                    asignacion.put("estudiante_nombres", rs.getString("estudiante_nombres"));
                    asignacion.put("estudiante_apellidos", rs.getString("estudiante_apellidos"));
                    asignacion.put("estudiante_email", rs.getString("estudiante_email"));
                    asignacion.put("especialidad", rs.getString("especialidad"));
                    asignacion.put("semestre", rs.getObject("semestre"));
                } catch (SQLException ignored) {}
                
                asignaciones.add(asignacion);
            }
            
        } catch (SQLException e) {
            System.err.println("Error ejecutando consulta de asignaciones: " + e.getMessage());
        }
        
        return asignaciones;
    }
    
    private List<Map<String, Object>> executeEstudianteQuery(String sql) {
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
                estudiante.put("semestre", rs.getObject("semestre"));
                estudiantes.add(estudiante);
            }
            
        } catch (SQLException e) {
            System.err.println("Error ejecutando consulta de estudiantes: " + e.getMessage());
        }
        
        return estudiantes;
    }
    
    private List<Map<String, Object>> executeDocenteQuery(String sql) {
        List<Map<String, Object>> docentes = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> docente = new HashMap<>();
                docente.put("id", rs.getInt("id"));
                docente.put("nombres", rs.getString("nombres"));
                docente.put("apellidos", rs.getString("apellidos"));
                docente.put("email", rs.getString("email"));
                docente.put("especialidad", rs.getString("especialidad"));
                docente.put("estudiantes_asignados", rs.getInt("estudiantes_asignados"));
                docentes.add(docente);
            }
            
        } catch (SQLException e) {
            System.err.println("Error ejecutando consulta de docentes: " + e.getMessage());
        }
        
        return docentes;
    }
    
    public boolean create(Map<String, Object> data) {
        String sql = """
            INSERT INTO asignaciones_docente (estudiante_id, docente_id, coordinador_id, tipo_asignacion) 
            VALUES (?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, (Integer) data.get("estudiante_id"));
            stmt.setInt(2, (Integer) data.get("docente_id"));
            stmt.setInt(3, (Integer) data.get("coordinador_id"));
            stmt.setString(4, (String) data.get("tipo_asignacion"));
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creando asignación docente: " + e.getMessage());
            return false;
        }
    }
    
    public boolean createJurado(int estudianteId, int presidenteId, int vocalId, int secretarioId, int coordinadorId) {
        String[] tiposJurado = {"jurado_presidente", "jurado_vocal", "jurado_secretario"};
        int[] docenteIds = {presidenteId, vocalId, secretarioId};
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            String sql = """
                INSERT INTO asignaciones_docente (estudiante_id, docente_id, coordinador_id, tipo_asignacion) 
                VALUES (?, ?, ?, ?)
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < tiposJurado.length; i++) {
                    stmt.setInt(1, estudianteId);
                    stmt.setInt(2, docenteIds[i]);
                    stmt.setInt(3, coordinadorId);
                    stmt.setString(4, tiposJurado[i]);
                    stmt.addBatch();
                }
                
                stmt.executeBatch();
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creando asignaciones de jurado: " + e.getMessage());
            return false;
        }
    }

}