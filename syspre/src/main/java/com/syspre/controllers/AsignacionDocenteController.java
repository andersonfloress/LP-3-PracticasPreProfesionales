package com.syspre.controllers;

import com.syspre.config.DatabaseConfig;
import com.syspre.models.AsignacionDocenteModel;
import com.syspre.utils.DatabaseQuery;
import io.javalin.http.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsignacionDocenteController {
    private final AsignacionDocenteModel asignacionModel = new AsignacionDocenteModel();
    
    public void mostrarAsignarDocente(Context ctx) {
        if (!isAuthenticated(ctx, "coordinador")) {
            ctx.redirect("/login/coordinador");
            return;
        }
        
        String estudianteIdStr = ctx.pathParam("id");
        Map<String, Object> user = getUserFromSession(ctx);
        String especialidad = (String) user.get("especialidad");
        
        try {
            int estudianteId = Integer.parseInt(estudianteIdStr);
            
            // Obtener datos del estudiante
            Map<String, Object> estudiante = obtenerDatosEstudiante(estudianteId);
            
            // Obtener lista de docentes de la misma especialidad
            List<Map<String, Object>> docentes = DatabaseQuery.getDocentesPorEspecialidad(especialidad);
            
            Map<String, Object> model = new HashMap<>();
            model.put("user", user);
            model.put("estudiante", estudiante);
            model.put("docentes", docentes);
            
            ctx.render("asignar_docente", model);
            
        } catch (NumberFormatException e) {
            ctx.redirect("/coordinador/dashboard?error=ID+inválido");
        }
    }
    
    public void procesarAsignacion(Context ctx) {
        if (!isAuthenticated(ctx, "coordinador")) {
            ctx.redirect("/login/coordinador");
            return;
        }
        
        try {
            String estudianteIdStr = ctx.formParam("estudiante_id");
            String docenteIdStr = ctx.formParam("docente_id");
            String comentarios = ctx.formParam("comentarios");
            
            int estudianteId = Integer.parseInt(estudianteIdStr);
            int docenteId = Integer.parseInt(docenteIdStr);
            
            // Obtener ID del coordinador de la sesión
            Map<String, Object> user = getUserFromSession(ctx);
            int coordinadorId = (Integer) user.get("id");
            
            Map<String, Object> asignacionData = new HashMap<>();
            asignacionData.put("estudiante_id", estudianteId);
            asignacionData.put("docente_id", docenteId);
            asignacionData.put("coordinador_id", coordinadorId);
            asignacionData.put("tipo_asignacion", "asesor");
            asignacionData.put("comentarios", comentarios);
            
            boolean success = asignacionModel.create(asignacionData);
            
            if (success) {
                ctx.redirect("/coordinador/dashboard?success=Docente+asignado+exitosamente");
            } else {
                ctx.redirect("/coordinador/asignar-docente/" + estudianteId + "?error=Error+al+asignar+docente");
            }
            
        } catch (Exception e) {
            ctx.redirect("/coordinador/dashboard?error=Error+procesando+asignación");
        }
    }
    
    public void asignarDocentePage(Context ctx) {
        // Página general de asignación - redirigir al dashboard
        ctx.redirect("/coordinador/dashboard");
    }
    
    public void asignarJurado(Context ctx) {
        // Funcionalidad de asignar jurado - implementar después
        ctx.redirect("/coordinador/dashboard?info=Funcionalidad+de+jurado+en+desarrollo");
    }
    
    private Map<String, Object> obtenerDatosEstudiante(int estudianteId) {
        Map<String, Object> estudiante = new HashMap<>();
        String sql = "SELECT id, nombres, apellidos, codigo, email, especialidad FROM usuarios WHERE id = ? AND tipo = 'estudiante'";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, estudianteId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                estudiante.put("id", rs.getInt("id"));
                estudiante.put("nombres", rs.getString("nombres"));
                estudiante.put("apellidos", rs.getString("apellidos"));
                estudiante.put("codigo", rs.getString("codigo"));
                estudiante.put("email", rs.getString("email"));
                estudiante.put("especialidad", rs.getString("especialidad"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo datos del estudiante: " + e.getMessage());
            // Datos por defecto en caso de error
            estudiante.put("id", estudianteId);
            estudiante.put("nombres", "Estudiante");
            estudiante.put("apellidos", "No encontrado");
            estudiante.put("codigo", "N/A");
            estudiante.put("email", "N/A");
            estudiante.put("especialidad", "N/A");
        }
        
        return estudiante;
    }
    
    private boolean isAuthenticated(Context ctx, String requiredType) {
        var session = ctx.req().getSession(false);
        if (session == null) return false;
        
        String userType = (String) session.getAttribute("user_type");
        return requiredType.equals(userType);
    }
    
    private Map<String, Object> getUserFromSession(Context ctx) {
        var session = ctx.req().getSession();
        return (Map<String, Object>) session.getAttribute("user_data");
    }
}