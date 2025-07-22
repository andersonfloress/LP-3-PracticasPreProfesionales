package com.syspre.controllers;

import com.syspre.models.AsignacionDocenteModel;
import com.syspre.models.UserModel;
import com.syspre.models.PlanPracticaModel;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsignacionController {
    private final AsignacionDocenteModel asignacionModel = new AsignacionDocenteModel();
    private final UserModel userModel = new UserModel();
    private final PlanPracticaModel planModel = new PlanPracticaModel();
    
    // Página para asignar docentes (coordinadores)
    public void asignarDocentePage(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"coordinador".equals(userData.get("tipo"))) {
            ctx.redirect("/login/coordinador");
            return;
        }
        
        // Obtener estudiantes sin docente asesor asignado
        List<Map<String, Object>> estudiantesSinAsesor = asignacionModel.getEstudiantesSinAsesor();
        
        // Obtener docentes disponibles
        List<Map<String, Object>> docentesDisponibles = userModel.getDocentesByEspecialidad((String) userData.get("especialidad"));
        
        // Obtener asignaciones existentes
        List<Map<String, Object>> asignacionesExistentes = asignacionModel.getAsignacionesByCoordinador((Integer) userData.get("id"));
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("estudiantesSinAsesor", estudiantesSinAsesor);
        model.put("docentesDisponibles", docentesDisponibles);
        model.put("asignacionesExistentes", asignacionesExistentes);
        
        ctx.render("asignar_docente", model);
    }
    
    // Procesar asignación de docente asesor
    public void procesarAsignacion(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"coordinador".equals(userData.get("tipo"))) {
            ctx.redirect("/login/coordinador");
            return;
        }
        
        try {
            int estudianteId = Integer.parseInt(ctx.formParam("estudiante_id"));
            int docenteId = Integer.parseInt(ctx.formParam("docente_id"));
            int coordinadorId = (Integer) userData.get("id");
            
            boolean exito = asignacionModel.asignarAsesor(estudianteId, docenteId, coordinadorId);
            
            if (exito) {
                ctx.redirect("/coordinador/asignar-docente?success=true");
            } else {
                ctx.redirect("/coordinador/asignar-docente?error=true");
            }
            
        } catch (Exception e) {
            System.err.println("Error procesando asignación: " + e.getMessage());
            ctx.redirect("/coordinador/asignar-docente?error=true");
        }
    }
    
    // Página para asignar docente a estudiante específico
    public void asignarDocenteEstudiantePage(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"coordinador".equals(userData.get("tipo"))) {
            ctx.redirect("/login/coordinador");
            return;
        }
        
        try {
            int estudianteId = Integer.parseInt(ctx.pathParam("id"));
            
            // Obtener datos del estudiante
            Map<String, Object> estudiante = userModel.getUserById(estudianteId);
            if (estudiante == null) {
                ctx.redirect("/coordinador/dashboard?error=estudiante_no_encontrado");
                return;
            }
            
            // Obtener docentes disponibles de la misma especialidad
            List<Map<String, Object>> docentesDisponibles = userModel.getDocentesByEspecialidad((String) userData.get("especialidad"));
            
            Map<String, Object> model = new HashMap<>();
            model.put("user", userData);
            model.put("estudiante", estudiante);
            model.put("docentesDisponibles", docentesDisponibles);
            
            ctx.render("asignar_docente_estudiante", model);
            
        } catch (NumberFormatException e) {
            ctx.redirect("/coordinador/dashboard?error=id_invalido");
        }
    }
    
    // Procesar asignación de docente a estudiante específico
    public void procesarAsignacionEstudiante(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"coordinador".equals(userData.get("tipo"))) {
            ctx.redirect("/login/coordinador");
            return;
        }
        
        try {
            int estudianteId = Integer.parseInt(ctx.pathParam("id"));
            int docenteId = Integer.parseInt(ctx.formParam("docente_id"));
            int coordinadorId = (Integer) userData.get("id");
            
            boolean exito = asignacionModel.asignarAsesor(estudianteId, docenteId, coordinadorId);
            
            if (exito) {
                ctx.redirect("/coordinador/dashboard?success=asignacion_completada");
            } else {
                ctx.redirect("/coordinador/asignar-docente/" + estudianteId + "?error=asignacion_fallida");
            }
            
        } catch (Exception e) {
            System.err.println("Error procesando asignación específica: " + e.getMessage());
            ctx.redirect("/coordinador/dashboard?error=error_servidor");
        }
    }

    // Dashboard del docente con planes asignados
    public void docentePlanesAsignados(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"docente".equals(userData.get("tipo"))) {
            ctx.redirect("/login/docente");
            return;
        }
        
        int docenteId = (Integer) userData.get("id");
        
        // Obtener estudiantes asignados y sus planes
        List<Map<String, Object>> planesAsignados = asignacionModel.getPlanesByDocente(docenteId);
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("planesAsignados", planesAsignados);
        
        ctx.render("docente_planes_asignados", model);
    }
    
    // Página para evaluar plan específico
    public void evaluarPlan(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"docente".equals(userData.get("tipo"))) {
            ctx.redirect("/login/docente");
            return;
        }
        
        int planId = Integer.parseInt(ctx.pathParam("planId"));
        int docenteId = (Integer) userData.get("id");
        
        // Verificar que el docente está asignado a este plan
        if (!asignacionModel.docenteAsignadoAPlan(docenteId, planId)) {
            ctx.redirect("/docente/planes-asignados?error=no_authorized");
            return;
        }
        
        Map<String, Object> plan = planModel.findById(planId);
        Map<String, Object> estudiante = userModel.findById((Integer) plan.get("estudiante_id"));
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("plan", plan);
        model.put("estudiante", estudiante);
        
        ctx.render("docente_evaluar_plan", model);
    }
    
    // Procesar evaluación del plan
    public void procesarEvaluacion(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"docente".equals(userData.get("tipo"))) {
            ctx.redirect("/login/docente");
            return;
        }
        
        try {
            int planId = Integer.parseInt(ctx.pathParam("planId"));
            int docenteId = (Integer) userData.get("id");
            
            // Verificar autorización
            if (!asignacionModel.docenteAsignadoAPlan(docenteId, planId)) {
                ctx.redirect("/docente/planes-asignados?error=no_authorized");
                return;
            }
            
            String accion = ctx.formParam("accion");
            String comentarios = ctx.formParam("comentarios");
            String notaStr = ctx.formParam("nota");
            
            boolean exito = false;
            
            if ("aprobar".equals(accion)) {
                double nota = Double.parseDouble(notaStr);
                exito = planModel.aprobarPorDocente(planId, docenteId, comentarios, nota);
            } else if ("rechazar".equals(accion)) {
                exito = planModel.rechazarPorDocente(planId, docenteId, comentarios);
            }
            
            if (exito) {
                ctx.redirect("/docente/planes-asignados?success=true");
            } else {
                ctx.redirect("/docente/evaluar-plan/" + planId + "?error=true");
            }
            
        } catch (Exception e) {
            System.err.println("Error procesando evaluación: " + e.getMessage());
            ctx.redirect("/docente/planes-asignados?error=true");
        }
    }
    
    private Map<String, Object> getUserFromSession(Context ctx) {
        return ctx.sessionAttribute("user");
    }
}