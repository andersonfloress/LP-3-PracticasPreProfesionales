package com.syspre.utils;

import com.syspre.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseQuery {
    
    public static void testUserData() {
        System.out.println("🔍 CONSULTANDO DATOS REALES DE USUARIOS");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            
            // Consultar usuarios por tipo
            String sql = "SELECT id, nombres, apellidos, email, tipo, especialidad FROM usuarios WHERE tipo IN ('coordinador', 'docente', 'estudiante')";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                System.out.println("👥 USUARIOS EN EL SISTEMA:");
                while (rs.next()) {
                    System.out.printf("  %s - %s %s (%s) - %s%n", 
                        rs.getString("tipo").toUpperCase(),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("email"),
                        rs.getString("especialidad")
                    );
                }
            }
            
            // Consultar planes de práctica
            String planSQL = "SELECT p.id, u.nombres, u.apellidos, p.empresa, p.estado FROM planes_practica p JOIN usuarios u ON p.estudiante_id = u.id";
            try (PreparedStatement stmt = conn.prepareStatement(planSQL)) {
                ResultSet rs = stmt.executeQuery();
                
                System.out.println("\n📋 PLANES DE PRÁCTICA:");
                while (rs.next()) {
                    System.out.printf("  Plan #%d - %s %s - %s (%s)%n",
                        rs.getInt("id"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("empresa"),
                        rs.getString("estado")
                    );
                }
            }
            
            // Consultar asignaciones
            String asignSQL = "SELECT a.*, u1.nombres as estudiante_nombre, u2.nombres as docente_nombre FROM asignaciones_docente a LEFT JOIN usuarios u1 ON a.estudiante_id = u1.id LEFT JOIN usuarios u2 ON a.docente_id = u2.id";
            try (PreparedStatement stmt = conn.prepareStatement(asignSQL)) {
                ResultSet rs = stmt.executeQuery();
                
                System.out.println("\n👨‍🏫 ASIGNACIONES DOCENTE:");
                while (rs.next()) {
                    System.out.printf("  %s -> %s%n",
                        rs.getString("estudiante_nombre"),
                        rs.getString("docente_nombre")
                    );
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error consultando datos: " + e.getMessage());
        }
    }
    
    public static List<Map<String, Object>> getEstudiantesSinAsignar(String especialidad) {
        List<Map<String, Object>> estudiantes = new ArrayList<>();
        
        String sql = """
            SELECT u.id, u.nombres, u.apellidos, u.codigo, u.email, u.especialidad
            FROM usuarios u 
            WHERE u.tipo = 'estudiante' 
            AND u.especialidad = ?
            AND u.id NOT IN (SELECT estudiante_id FROM asignaciones_docente WHERE estudiante_id IS NOT NULL)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, especialidad);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> estudiante = new HashMap<>();
                estudiante.put("id", rs.getInt("id"));
                estudiante.put("nombres", rs.getString("nombres"));
                estudiante.put("apellidos", rs.getString("apellidos"));
                estudiante.put("codigo", rs.getString("codigo"));
                estudiante.put("email", rs.getString("email"));
                estudiante.put("especialidad", rs.getString("especialidad"));
                estudiantes.add(estudiante);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error consultando estudiantes sin asignar: " + e.getMessage());
        }
        
        return estudiantes;
    }
    
    public static List<Map<String, Object>> getEstudiantesConAsignacion(String especialidad) {
        List<Map<String, Object>> estudiantes = new ArrayList<>();
        
        String sql = """
            SELECT u.id, u.nombres, u.apellidos, u.codigo, u.email, u.especialidad,
                   d.nombres as docente_nombres, d.apellidos as docente_apellidos, d.email as docente_email,
                   a.created_at as fecha_asignacion
            FROM usuarios u 
            JOIN asignaciones_docente a ON u.id = a.estudiante_id
            JOIN usuarios d ON a.docente_id = d.id
            WHERE u.tipo = 'estudiante' 
            AND u.especialidad = ?
            AND a.tipo_asignacion = 'asesor'
            ORDER BY u.apellidos, u.nombres
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, especialidad);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> estudiante = new HashMap<>();
                estudiante.put("id", rs.getInt("id"));
                estudiante.put("nombres", rs.getString("nombres"));
                estudiante.put("apellidos", rs.getString("apellidos"));
                estudiante.put("codigo", rs.getString("codigo"));
                estudiante.put("email", rs.getString("email"));
                estudiante.put("especialidad", rs.getString("especialidad"));
                estudiante.put("docente_nombres", rs.getString("docente_nombres"));
                estudiante.put("docente_apellidos", rs.getString("docente_apellidos"));
                estudiante.put("docente_email", rs.getString("docente_email"));
                estudiante.put("fecha_asignacion", rs.getTimestamp("fecha_asignacion"));
                estudiantes.add(estudiante);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error consultando estudiantes con asignación: " + e.getMessage());
        }
        
        return estudiantes;
    }
    
    public static List<Map<String, Object>> getDocentesPorEspecialidad(String especialidad) {
        List<Map<String, Object>> docentes = new ArrayList<>();
        
        String sql = """
            SELECT id, nombres, apellidos, email, especialidad
            FROM usuarios 
            WHERE tipo = 'docente' 
            AND especialidad = ?
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, especialidad);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> docente = new HashMap<>();
                docente.put("id", rs.getInt("id"));
                docente.put("nombres", rs.getString("nombres"));
                docente.put("apellidos", rs.getString("apellidos"));
                docente.put("email", rs.getString("email"));
                docente.put("especialidad", rs.getString("especialidad"));
                docentes.add(docente);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error consultando docentes: " + e.getMessage());
        }
        
        return docentes;
    }
    
    public static List<Map<String, Object>> getDocumentosPorEspecialidad(String especialidad) {
        List<Map<String, Object>> documentos = new ArrayList<>();
        
        String sql = """
            SELECT d.id, d.titulo, d.descripcion, d.archivo_path, d.especialidad, d.created_at,
                   u.nombres as coordinador_nombres, u.apellidos as coordinador_apellidos
            FROM documentos_especialidad d
            JOIN usuarios u ON d.coordinador_id = u.id
            WHERE d.especialidad = ? AND d.activo = TRUE
            ORDER BY d.id DESC
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, especialidad);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> documento = new HashMap<>();
                documento.put("id", rs.getInt("id"));
                documento.put("titulo", rs.getString("titulo"));
                documento.put("descripcion", rs.getString("descripcion"));
                documento.put("archivo_path", rs.getString("archivo_path"));
                documento.put("especialidad", rs.getString("especialidad"));
                documento.put("created_at", rs.getTimestamp("created_at"));
                documento.put("coordinador_nombres", rs.getString("coordinador_nombres"));
                documento.put("coordinador_apellidos", rs.getString("coordinador_apellidos"));
                documentos.add(documento);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error consultando documentos por especialidad: " + e.getMessage());
        }
        
        return documentos;
    }
    
    public static Map<String, Integer> getEstadisticasCoordinador(String especialidad) {
        Map<String, Integer> stats = new HashMap<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            
            // Estudiantes activos
            String sql1 = "SELECT COUNT(*) as total FROM usuarios WHERE tipo = 'estudiante' AND especialidad = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                stmt.setString(1, especialidad);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.put("estudiantesActivos", rs.getInt("total"));
                }
            }
            
            // Planes aprobados
            String sql2 = "SELECT COUNT(*) as total FROM planes_practica p JOIN usuarios u ON p.estudiante_id = u.id WHERE p.estado = 'aprobado_coordinador' AND u.especialidad = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                stmt.setString(1, especialidad);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.put("planesAprobados", rs.getInt("total"));
                }
            }
            
            // Asignaciones pendientes
            String sql3 = "SELECT COUNT(*) as total FROM usuarios u WHERE u.tipo = 'estudiante' AND u.especialidad = ? AND u.id NOT IN (SELECT estudiante_id FROM asignaciones_docente WHERE estudiante_id IS NOT NULL)";
            try (PreparedStatement stmt = conn.prepareStatement(sql3)) {
                stmt.setString(1, especialidad);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.put("asignacionesPendientes", rs.getInt("total"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo estadísticas: " + e.getMessage());
            stats.put("estudiantesActivos", 0);
            stats.put("planesAprobados", 0);
            stats.put("asignacionesPendientes", 0);
        }
        
        return stats;
    }
    
    public static List<Map<String, Object>> getReportesPendientesDocente(int docenteId) {
        List<Map<String, Object>> reportes = new ArrayList<>();
        String sql = """
            SELECT r.*, u.nombres as estudiante_nombres, u.apellidos as estudiante_apellidos,
                   p.empresa as empresa
            FROM reportes_semanales r 
            JOIN usuarios u ON r.estudiante_id = u.id 
            JOIN asignaciones_docente a ON u.id = a.estudiante_id 
            LEFT JOIN planes_practica p ON u.id = p.estudiante_id
            WHERE a.docente_id = ? AND (r.estado = 'enviado' OR r.estado IS NULL)
            ORDER BY r.id DESC
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, docenteId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> reporte = new HashMap<>();
                reporte.put("id", rs.getInt("id"));
                reporte.put("semana_numero", rs.getInt("id")); // Usar id como número de semana temporal
                reporte.put("fecha_inicio", rs.getDate("fecha_inicio"));
                reporte.put("fecha_fin", rs.getDate("fecha_fin"));
                reporte.put("actividades_realizadas", rs.getString("actividades_realizadas"));
                reporte.put("horas_trabajadas", rs.getInt("horas_trabajadas"));
                reporte.put("estudiante_nombres", rs.getString("estudiante_nombres"));
                reporte.put("estudiante_apellidos", rs.getString("estudiante_apellidos"));
                reporte.put("empresa", rs.getString("empresa"));
                reporte.put("estado", rs.getString("estado"));
                
                // Manejar campos de fecha de manera segura
                try {
                    reporte.put("created_at", rs.getTimestamp("fecha_creacion"));
                } catch (SQLException e) {
                    reporte.put("created_at", null);
                }
                
                reportes.add(reporte);
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo reportes pendientes para docente: " + e.getMessage());
        }
        
        return reportes;
    }
    
    public static List<Map<String, Object>> getInformesPendientesDocente(int docenteId) {
        List<Map<String, Object>> informes = new ArrayList<>();
        String sql = """
            SELECT i.*, u.nombres as estudiante_nombres, u.apellidos as estudiante_apellidos,
                   p.empresa as empresa
            FROM informes_finales i 
            JOIN usuarios u ON i.estudiante_id = u.id 
            JOIN asignaciones_docente a ON u.id = a.estudiante_id 
            LEFT JOIN planes_practica p ON u.id = p.estudiante_id
            WHERE a.docente_id = ? AND (i.estado = 'enviado' OR i.estado IS NULL)
            ORDER BY i.id DESC
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, docenteId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> informe = new HashMap<>();
                informe.put("id", rs.getInt("id"));
                informe.put("titulo", rs.getString("titulo"));
                informe.put("resumen_actividades", rs.getString("resumen_actividades"));
                informe.put("logros_obtenidos", rs.getString("logros_obtenidos"));
                informe.put("dificultades_encontradas", rs.getString("dificultades_encontradas"));
                informe.put("estudiante_nombres", rs.getString("estudiante_nombres"));
                informe.put("estudiante_apellidos", rs.getString("estudiante_apellidos"));
                informe.put("empresa", rs.getString("empresa"));
                informe.put("estado", rs.getString("estado"));
                
                // Manejar campo de fecha de manera segura
                try {
                    informe.put("created_at", rs.getTimestamp("fecha_creacion"));
                } catch (SQLException e) {
                    informe.put("created_at", null);
                }
                
                informes.add(informe);
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo informes pendientes para docente: " + e.getMessage());
        }
        
        return informes;
    }
    
    public static void main(String[] args) {
        testUserData();
    }
}