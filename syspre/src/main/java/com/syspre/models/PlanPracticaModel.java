package com.syspre.models;

import com.syspre.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanPracticaModel {
    
    public boolean create(Map<String, Object> data) {
        // VERSIÓN CORREGIDA: Incluir TODOS los campos NOT NULL con valores ENUM correctos + fecha_inicio_plan
        String sql = """
            INSERT INTO planes_practica (
                estudiante_id, nombres, apellidos, codigo, especialidad, email, telefono,
                empresa, ruc, direccion_empresa, telefono_empresa, supervisor, cargo_supervisor,
                fecha_inicio, fecha_fin, horario, total_horas, actividades, objetivos, estado, fecha_inicio_plan
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pendiente', ?)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Debug: mostrar datos recibidos
            System.out.println("🔍 DATOS RECIBIDOS PARA CREAR PLAN:");
            data.forEach((key, value) -> System.out.println("  " + key + " = " + value));
            
            // Debug: mostrar SQL preparado
            System.out.println("🔍 SQL A EJECUTAR:");
            System.out.println(sql);
            
            // Asignar TODOS los campos obligatorios con valores por defecto si es necesario
            stmt.setInt(1, (Integer) data.get("estudiante_id"));
            stmt.setString(2, (String) data.getOrDefault("nombres", "N/A"));
            stmt.setString(3, (String) data.getOrDefault("apellidos", "N/A"));
            stmt.setString(4, (String) data.getOrDefault("codigo", "N/A"));
            stmt.setString(5, (String) data.getOrDefault("especialidad", "N/A"));
            stmt.setString(6, (String) data.getOrDefault("email", "N/A"));
            stmt.setString(7, (String) data.getOrDefault("telefono", "N/A"));
            stmt.setString(8, (String) data.getOrDefault("empresa", ""));
            stmt.setString(9, (String) data.getOrDefault("ruc", ""));
            stmt.setString(10, (String) data.getOrDefault("direccion_empresa", ""));
            stmt.setString(11, (String) data.getOrDefault("telefono_empresa", ""));
            stmt.setString(12, (String) data.getOrDefault("supervisor", ""));
            stmt.setString(13, (String) data.getOrDefault("cargo_supervisor", ""));
            
            // Fechas por defecto (período académico típico)
            stmt.setString(14, (String) data.getOrDefault("fecha_inicio", "2025-03-01"));
            stmt.setString(15, (String) data.getOrDefault("fecha_fin", "2025-07-31"));
            stmt.setString(16, (String) data.getOrDefault("horario", "Lunes a Viernes 8:00-17:00"));
            
            // Debug: Verificar el tipo de total_horas
            Object totalHoras = data.getOrDefault("total_horas", 240);
            System.out.println("🔍 TOTAL_HORAS: " + totalHoras + " (tipo: " + totalHoras.getClass().getSimpleName() + ")");
            if (totalHoras instanceof Integer) {
                stmt.setInt(17, (Integer) totalHoras);
            } else {
                stmt.setInt(17, 240); // valor por defecto
            }
            
            stmt.setString(18, (String) data.getOrDefault("actividades", ""));
            stmt.setString(19, (String) data.getOrDefault("objetivos", ""));
            
            // Fecha de inicio del plan (puede ser null)
            String fechaInicioPlan = (String) data.get("fecha_inicio_plan");
            if (fechaInicioPlan != null && !fechaInicioPlan.trim().isEmpty()) {
                stmt.setString(20, fechaInicioPlan);
            } else {
                stmt.setNull(20, java.sql.Types.DATE);
            }
            
            // Debug: Mostrar todos los parámetros asignados
            System.out.println("🔍 PARÁMETROS ASIGNADOS AL PREPARED STATEMENT");
            
            System.out.println("✅ Ejecutando INSERT...");
            int rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Plan creado exitosamente con todos los campos, filas afectadas: " + rowsAffected);
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error creando plan de práctica: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public Map<String, Object> findByEstudianteId(int estudianteId) {
        String sql = "SELECT * FROM planes_practica WHERE estudiante_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, estudianteId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, Object> plan = new HashMap<>();
                plan.put("id", rs.getInt("id"));
                plan.put("estudiante_id", rs.getInt("estudiante_id"));
                plan.put("nombres", rs.getString("nombres"));
                plan.put("apellidos", rs.getString("apellidos"));
                plan.put("codigo", rs.getString("codigo"));
                plan.put("especialidad", rs.getString("especialidad"));
                plan.put("email", rs.getString("email"));
                plan.put("telefono", rs.getString("telefono"));
                plan.put("empresa", rs.getString("empresa"));
                plan.put("ruc", rs.getString("ruc"));
                plan.put("direccion_empresa", rs.getString("direccion_empresa"));
                plan.put("telefono_empresa", rs.getString("telefono_empresa"));
                plan.put("supervisor", rs.getString("supervisor"));
                plan.put("cargo_supervisor", rs.getString("cargo_supervisor"));
                plan.put("fecha_inicio", rs.getDate("fecha_inicio"));
                plan.put("fecha_fin", rs.getDate("fecha_fin"));
                plan.put("horario", rs.getString("horario"));
                plan.put("total_horas", rs.getInt("total_horas"));
                plan.put("actividades", rs.getString("actividades"));
                plan.put("objetivos", rs.getString("objetivos"));
                plan.put("estado", rs.getString("estado"));
                plan.put("comentarios_docente", rs.getString("comentarios_docente"));
                plan.put("comentarios_coordinador", rs.getString("comentarios_coordinador"));
                plan.put("nota_docente", rs.getBigDecimal("nota_docente"));
                plan.put("archivo_plan", rs.getString("archivo_plan"));
                plan.put("archivo_documento1", rs.getString("archivo_documento1"));
                plan.put("archivo_documento2", rs.getString("archivo_documento2"));
                plan.put("archivo_documento3", rs.getString("archivo_documento3"));
                plan.put("fecha_creacion", rs.getTimestamp("fecha_creacion"));
                plan.put("fecha_actualizacion", rs.getTimestamp("fecha_actualizacion"));
                
                // Agregar fecha_inicio_plan si existe
                try {
                    plan.put("fecha_inicio_plan", rs.getDate("fecha_inicio_plan"));
                } catch (SQLException e) {
                    plan.put("fecha_inicio_plan", null);
                }
                
                return plan;
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando plan: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<Map<String, Object>> findPendientesParaDocente() {
        String sql = """
            SELECT p.*, u.nombres, u.apellidos, u.email 
            FROM planes_practica p 
            JOIN usuarios u ON p.estudiante_id = u.id 
            WHERE p.estado = 'enviado'
            ORDER BY p.fecha_creacion ASC
        """;
        
        return executePlanQuery(sql);
    }
    
    public List<Map<String, Object>> findPendientesParaCoordinador() {
        String sql = """
            SELECT p.*, u.nombres, u.apellidos, u.email 
            FROM planes_practica p 
            JOIN usuarios u ON p.estudiante_id = u.id 
            WHERE p.estado = 'aprobado_docente'
            ORDER BY p.fecha_creacion ASC
        """;
        
        return executePlanQuery(sql);
    }
    
    public boolean updateEstado(int planId, String nuevoEstado, String comentarios, String tipoComentario) {
        String sql;
        if ("docente".equals(tipoComentario)) {
            sql = "UPDATE planes_practica SET estado = ?, comentarios_docente = ? WHERE id = ?";
        } else {
            sql = "UPDATE planes_practica SET estado = ?, comentarios_coordinador = ? WHERE id = ?";
        }
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nuevoEstado);
            stmt.setString(2, comentarios);
            stmt.setInt(3, planId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error actualizando estado del plan: " + e.getMessage());
            return false;
        }
    }
    
    public boolean enviarPlan(int planId) {
        String sql = "UPDATE planes_practica SET estado = 'enviado' WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, planId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Plan ID " + planId + " enviado para revisión");
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error enviando plan para revisión: " + e.getMessage());
            return false;
        }
    }
    
    private List<Map<String, Object>> executePlanQuery(String sql) {
        List<Map<String, Object>> planes = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> plan = new HashMap<>();
                plan.put("id", rs.getInt("id"));
                plan.put("estudiante_id", rs.getInt("estudiante_id"));
                plan.put("empresa_nombre", rs.getString("empresa_nombre"));
                plan.put("empresa_direccion", rs.getString("empresa_direccion"));
                plan.put("empresa_supervisor", rs.getString("empresa_supervisor"));
                plan.put("actividades_planificadas", rs.getString("actividades_planificadas"));
                plan.put("objetivos_generales", rs.getString("objetivos_generales"));
                plan.put("objetivos_especificos", rs.getString("objetivos_especificos"));
                plan.put("cronograma", rs.getString("cronograma"));
                plan.put("archivo_plan", rs.getString("archivo_plan"));
                plan.put("estado", rs.getString("estado"));
                plan.put("comentarios_docente", rs.getString("comentarios_docente"));
                plan.put("comentarios_coordinador", rs.getString("comentarios_coordinador"));
                
                // Manejar campos de fecha de manera segura
                try {
                    plan.put("created_at", rs.getTimestamp("fecha_creacion"));
                } catch (SQLException e) {
                    plan.put("created_at", null);
                }
                
                try {
                    plan.put("updated_at", rs.getTimestamp("fecha_actualizacion"));
                } catch (SQLException e) {
                    plan.put("updated_at", null);
                }
                
                // Datos del estudiante
                try {
                    plan.put("estudiante_nombres", rs.getString("nombres"));
                    plan.put("estudiante_apellidos", rs.getString("apellidos"));
                    plan.put("estudiante_email", rs.getString("email"));
                } catch (SQLException ignored) {
                    // Campos no disponibles en todas las consultas
                }
                
                planes.add(plan);
            }
            
        } catch (SQLException e) {
            System.err.println("Error ejecutando consulta de planes: " + e.getMessage());
        }
        
        return planes;
    }
    
    // Buscar plan por ID
    public Map<String, Object> findById(int id) {
        String sql = "SELECT * FROM planes_practica WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> plan = new HashMap<>();
                    plan.put("id", rs.getInt("id"));
                    plan.put("estudiante_id", rs.getInt("estudiante_id"));
                    plan.put("empresa", rs.getString("empresa"));
                    plan.put("ruc", rs.getString("ruc"));
                    plan.put("direccion_empresa", rs.getString("direccion_empresa"));
                    plan.put("supervisor", rs.getString("supervisor"));
                    plan.put("fecha_inicio", rs.getDate("fecha_inicio"));
                    plan.put("fecha_fin", rs.getDate("fecha_fin"));
                    plan.put("total_horas", rs.getInt("total_horas"));
                    plan.put("horario", rs.getString("horario"));
                    plan.put("actividades", rs.getString("actividades"));
                    plan.put("objetivos", rs.getString("objetivos"));
                    plan.put("estado", rs.getString("estado"));
                    plan.put("comentarios_docente", rs.getString("comentarios_docente"));
                    plan.put("nota_docente", rs.getObject("nota_docente"));
                    plan.put("comentarios_coordinador", rs.getString("comentarios_coordinador"));
                    plan.put("fecha_creacion", rs.getTimestamp("fecha_creacion"));
                    plan.put("fecha_actualizacion", rs.getTimestamp("fecha_actualizacion"));
                    return plan;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando plan por ID: " + e.getMessage());
        }
        
        return null;
    }
    
    // Aprobar plan por docente
    public boolean aprobarPorDocente(int planId, int docenteId, String comentarios, double nota) {
        String sql = "UPDATE planes_practica SET estado = 'aprobado_docente', comentarios_docente = ?, nota_docente = ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, comentarios);
            stmt.setDouble(2, nota);
            stmt.setInt(3, planId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error aprobando plan por docente: " + e.getMessage());
            return false;
        }
    }
    
    // Rechazar plan por docente
    public boolean rechazarPorDocente(int planId, int docenteId, String comentarios) {
        String sql = "UPDATE planes_practica SET estado = 'rechazado', comentarios_docente = ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, comentarios);
            stmt.setInt(2, planId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error rechazando plan por docente: " + e.getMessage());
            return false;
        }
    }
    
    public List<Map<String, Object>> findPlanesAsignadosDocente(int docenteId) {
        List<Map<String, Object>> planes = new ArrayList<>();
        String sql = """
            SELECT p.*, u.nombres as estudiante_nombres, u.apellidos as estudiante_apellidos,
                   u.email as estudiante_email
            FROM planes_practica p 
            JOIN usuarios u ON p.estudiante_id = u.id 
            JOIN asignaciones_docente a ON u.id = a.estudiante_id 
            WHERE a.docente_id = ?
            ORDER BY p.fecha_creacion DESC
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, docenteId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> plan = new HashMap<>();
                plan.put("id", rs.getInt("id"));
                plan.put("empresa", rs.getString("empresa"));
                plan.put("estudiante_nombres", rs.getString("estudiante_nombres"));
                plan.put("estudiante_apellidos", rs.getString("estudiante_apellidos"));
                plan.put("estudiante_email", rs.getString("estudiante_email"));
                plan.put("estado", rs.getString("estado"));
                try {
                    plan.put("created_at", rs.getTimestamp("fecha_creacion"));
                } catch (SQLException e) {
                    plan.put("created_at", null);
                }
                plan.put("comentarios_docente", rs.getString("comentarios_docente"));
                plan.put("actividades", rs.getString("actividades"));
                plan.put("objetivos", rs.getString("objetivos"));
                planes.add(plan);
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo planes asignados al docente: " + e.getMessage());
        }
        
        return planes;
    }
    
    // Coordinador - Ver todos los planes de su especialidad
    public List<Map<String, Object>> findPlanesPorEspecialidad(String especialidad) {
        List<Map<String, Object>> planes = new ArrayList<>();
        String sql = """
            SELECT p.*, u.nombres as estudiante_nombres, u.apellidos as estudiante_apellidos,
                   u.email as estudiante_email, u.codigo as estudiante_codigo
            FROM planes_practica p 
            JOIN usuarios u ON p.estudiante_id = u.id 
            WHERE u.especialidad = ?
            ORDER BY p.fecha_creacion DESC
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, especialidad);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> plan = new HashMap<>();
                plan.put("id", rs.getInt("id"));
                plan.put("empresa", rs.getString("empresa"));
                plan.put("estudiante_nombres", rs.getString("estudiante_nombres"));
                plan.put("estudiante_apellidos", rs.getString("estudiante_apellidos"));
                plan.put("estudiante_email", rs.getString("estudiante_email"));
                plan.put("estudiante_codigo", rs.getString("estudiante_codigo"));
                plan.put("estado", rs.getString("estado"));
                try {
                    plan.put("created_at", rs.getTimestamp("fecha_creacion"));
                } catch (SQLException e) {
                    plan.put("created_at", null);
                }
                plan.put("comentarios_docente", rs.getString("comentarios_docente"));
                plan.put("comentarios_coordinador", rs.getString("comentarios_coordinador"));
                plan.put("actividades", rs.getString("actividades"));
                plan.put("objetivos", rs.getString("objetivos"));
                plan.put("fecha_inicio", rs.getDate("fecha_inicio"));
                plan.put("fecha_fin", rs.getDate("fecha_fin"));
                plan.put("total_horas", rs.getInt("total_horas"));
                planes.add(plan);
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo planes por especialidad: " + e.getMessage());
        }
        
        return planes;
    }
}