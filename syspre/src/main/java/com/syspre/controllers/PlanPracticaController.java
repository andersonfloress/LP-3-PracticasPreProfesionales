package com.syspre.controllers;

import com.syspre.models.PlanPracticaModel;
import com.syspre.models.UserModel;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanPracticaController {
    private final PlanPracticaModel planModel = new PlanPracticaModel();
    private final UserModel userModel = new UserModel();
    
    // Estudiante - Ver plan
    public void estudianteVerPlan(Context ctx) {
        try {
            Map<String, Object> userData = getUserFromSession(ctx);
            if (userData == null || !"estudiante".equals(userData.get("tipo"))) {
                ctx.redirect("/login/estudiante");
                return;
            }
            
            int estudianteId = (Integer) userData.get("id");
            Map<String, Object> plan = planModel.findByEstudianteId(estudianteId);
            
            Map<String, Object> model = new HashMap<>();
            model.put("user", userData);
            model.put("plan", plan);
            
            ctx.render("estudiante_ver_plan", model);
        } catch (Exception e) {
            System.err.println("❌ Error en estudianteVerPlan: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).html("<h1>Error en el controlador</h1><p>" + e.getMessage() + "</p>");
        }
    }
    
    // Estudiante - Registro de plan (formulario)
    public void estudianteRegistroPlanPage(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"estudiante".equals(userData.get("tipo"))) {
            ctx.redirect("/login/estudiante");
            return;
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        
        ctx.render("estudiante_plan_crear", model);
    }
    
    // Estudiante - Crear plan (submit)
    public void estudianteCrearPlan(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"estudiante".equals(userData.get("tipo"))) {
            ctx.redirect("/login/estudiante");
            return;
        }
        
        try {
            String accion = ctx.formParam("accion");
            
            // Validar datos requeridos para envío a revisión
            if ("enviar_revision".equals(accion)) {
                String empresa = ctx.formParam("empresa");
                String ruc = ctx.formParam("ruc");
                String direccionEmpresa = ctx.formParam("direccion_empresa");
                String telefonoEmpresa = ctx.formParam("telefono_empresa");
                String supervisor = ctx.formParam("supervisor");
                String cargoSupervisor = ctx.formParam("cargo_supervisor");
                String fechaInicio = ctx.formParam("fecha_inicio");
                String fechaFin = ctx.formParam("fecha_fin");
                String horario = ctx.formParam("horario");
                String totalHoras = ctx.formParam("total_horas");
                String actividades = ctx.formParam("actividades");
                String objetivos = ctx.formParam("objetivos");
                
                if (empresa == null || empresa.trim().isEmpty() ||
                    ruc == null || ruc.trim().isEmpty() ||
                    direccionEmpresa == null || direccionEmpresa.trim().isEmpty() ||
                    telefonoEmpresa == null || telefonoEmpresa.trim().isEmpty() ||
                    supervisor == null || supervisor.trim().isEmpty() ||
                    cargoSupervisor == null || cargoSupervisor.trim().isEmpty() ||
                    fechaInicio == null || fechaInicio.trim().isEmpty() ||
                    fechaFin == null || fechaFin.trim().isEmpty() ||
                    horario == null || horario.trim().isEmpty() ||
                    totalHoras == null || totalHoras.trim().isEmpty() ||
                    actividades == null || actividades.trim().isEmpty() ||
                    objetivos == null || objetivos.trim().isEmpty()) {
                    
                    Map<String, Object> model = new HashMap<>();
                    model.put("user", userData);
                    model.put("error", "Todos los campos marcados con (*) son obligatorios para enviar a revisión");
                    ctx.render("estudiante_plan_crear", model);
                    return;
                }
            }
            
            // Debug: Verificar datos de usuario
            System.out.println("🔍 DATOS DE USUARIO:");
            userData.forEach((key, value) -> System.out.println("  " + key + " = " + value));
            
            // Debug: Verificar parámetros del formulario
            System.out.println("🔍 PARÁMETROS DEL FORMULARIO:");
            ctx.formParamMap().forEach((key, values) -> 
                System.out.println("  " + key + " = " + (values.isEmpty() ? "VACÍO" : values.get(0))));
            
            Map<String, Object> planData = new HashMap<>();
            planData.put("estudiante_id", userData.get("id"));
            
            // Verificar que los datos del usuario no sean null
            planData.put("nombres", userData.get("nombres") != null ? userData.get("nombres") : "N/A");
            planData.put("apellidos", userData.get("apellidos") != null ? userData.get("apellidos") : "N/A");
            planData.put("codigo", userData.get("codigo") != null ? userData.get("codigo") : "N/A");
            planData.put("especialidad", userData.get("especialidad") != null ? userData.get("especialidad") : "N/A");
            planData.put("email", userData.get("email") != null ? userData.get("email") : "N/A");
            planData.put("telefono", userData.get("telefono") != null ? userData.get("telefono") : "N/A");
            
            // Mapear todos los parámetros del formulario correctamente
            planData.put("empresa", ctx.formParam("empresa") != null ? ctx.formParam("empresa") : "");
            planData.put("ruc", ctx.formParam("ruc") != null ? ctx.formParam("ruc") : "");
            planData.put("direccion_empresa", ctx.formParam("direccion_empresa") != null ? ctx.formParam("direccion_empresa") : "");
            planData.put("telefono_empresa", ctx.formParam("telefono_empresa") != null ? ctx.formParam("telefono_empresa") : "");
            planData.put("supervisor", ctx.formParam("supervisor") != null ? ctx.formParam("supervisor") : "");
            planData.put("cargo_supervisor", ctx.formParam("cargo_supervisor") != null ? ctx.formParam("cargo_supervisor") : "");
            
            // Fechas y horarios
            planData.put("fecha_inicio", ctx.formParam("fecha_inicio") != null ? ctx.formParam("fecha_inicio") : "");
            planData.put("fecha_fin", ctx.formParam("fecha_fin") != null ? ctx.formParam("fecha_fin") : "");
            planData.put("horario", ctx.formParam("horario") != null ? ctx.formParam("horario") : "");
            planData.put("fecha_inicio_plan", ctx.formParam("fecha_inicio_plan")); // Puede ser null
            
            // Total de horas (convertir a integer)
            try {
                String totalHorasStr = ctx.formParam("total_horas");
                Integer totalHoras = totalHorasStr != null && !totalHorasStr.trim().isEmpty() 
                    ? Integer.parseInt(totalHorasStr) : 240;
                planData.put("total_horas", totalHoras);
            } catch (NumberFormatException e) {
                planData.put("total_horas", 240); // valor por defecto
            }
            
            // Actividades y objetivos
            planData.put("actividades", ctx.formParam("actividades") != null ? ctx.formParam("actividades") : "");
            planData.put("objetivos", ctx.formParam("objetivos") != null ? ctx.formParam("objetivos") : "");
            
            boolean success = planModel.create(planData);
            
            if (success) {
                // Si la acción es enviar para revisión, cambiar estado
                if ("enviar_revision".equals(accion)) {
                    // Obtener el ID del plan recién creado
                    Map<String, Object> planCreado = planModel.findByEstudianteId((Integer) userData.get("id"));
                    if (planCreado != null) {
                        planModel.enviarPlan((Integer) planCreado.get("id"));
                        ctx.redirect("/estudiante/plan/ver?success=true&enviado=true");
                    } else {
                        ctx.redirect("/estudiante/plan/ver?success=true");
                    }
                } else {
                    ctx.redirect("/estudiante/plan/ver?success=true&borrador=true");
                }
            } else {
                Map<String, Object> model = new HashMap<>();
                model.put("user", userData);
                model.put("error", "Error al crear el plan de práctica");
                model.put("formData", planData);
                ctx.render("estudiante_plan_crear", model);
            }
            
        } catch (Exception e) {
            Map<String, Object> model = new HashMap<>();
            model.put("user", userData);
            model.put("error", "Error en los datos del formulario: " + e.getMessage());
            e.printStackTrace();
            ctx.render("estudiante_plan_crear", model);
        }
    }
    
    // Docente - Ver planes pendientes
    public void docenteVerPlanesPendientes(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"docente".equals(userData.get("tipo"))) {
            ctx.redirect("/login/docente");
            return;
        }
        
        List<Map<String, Object>> planesPendientes = planModel.findPendientesParaDocente();
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("planes", planesPendientes);
        
        ctx.render("docente_planes_pendientes", model);
    }
    
    // Docente - Aprobar plan (formulario)
    public void docenteAprobarPlanPage(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"docente".equals(userData.get("tipo"))) {
            ctx.redirect("/login/docente");
            return;
        }
        
        int planId = Integer.parseInt(ctx.pathParam("id"));
        Map<String, Object> plan = planModel.findByEstudianteId(planId); // TODO: Should be findById
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("plan", plan);
        
        ctx.render("aprobar_plan", model);
    }
    
    // Docente - Procesar aprobación
    public void docenteAprobarPlan(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"docente".equals(userData.get("tipo"))) {
            ctx.redirect("/login/docente");
            return;
        }
        
        try {
            int planId = Integer.parseInt(ctx.pathParam("id"));
            String accion = ctx.formParam("accion");
            String comentarios = ctx.formParam("comentarios");
            
            boolean success;
            if ("aprobar".equals(accion)) {
                success = planModel.updateEstado(planId, "aprobado_docente", comentarios, "docente");
            } else {
                success = planModel.updateEstado(planId, "rechazado", comentarios, "docente");
            }
            
            if (success) {
                ctx.redirect("/docente/planes?success=true");
            } else {
                ctx.redirect("/docente/plan/" + planId + "/aprobar?error=true");
            }
            
        } catch (Exception e) {
            ctx.redirect("/docente/planes?error=true");
        }
    }
    
    // Coordinador - Ver planes pendientes
    public void coordinadorVerPlanesPendientes(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"coordinador".equals(userData.get("tipo"))) {
            ctx.redirect("/login/coordinador");
            return;
        }
        
        List<Map<String, Object>> planesPendientes = planModel.findPendientesParaCoordinador();
        
        // Validar datos para evitar errores Thymeleaf
        if (planesPendientes != null) {
            for (Map<String, Object> plan : planesPendientes) {
                if (plan.get("empresa") == null) plan.put("empresa", "No definida");
                if (plan.get("estudiante_nombres") == null) plan.put("estudiante_nombres", "");
                if (plan.get("estudiante_apellidos") == null) plan.put("estudiante_apellidos", "");
                if (plan.get("fecha_creacion") == null) plan.put("fecha_creacion", "No disponible");
                if (plan.get("estado") == null) plan.put("estado", "pendiente");
            }
        } else {
            planesPendientes = new ArrayList<>();
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("planes", planesPendientes);
        
        ctx.render("gestion_practicas", model);
    }
    
    // Coordinador - Revisar plan (formulario)
    public void coordinadorRevisarPlanPage(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"coordinador".equals(userData.get("tipo"))) {
            ctx.redirect("/login/coordinador");
            return;
        }
        
        int planId = Integer.parseInt(ctx.pathParam("id"));
        Map<String, Object> plan = planModel.findByEstudianteId(planId); // TODO: Should be findById
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("plan", plan);
        
        ctx.render("coordinador_revisar_plan", model);
    }
    
    // Coordinador - Procesar revisión
    public void coordinadorRevisarPlan(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"coordinador".equals(userData.get("tipo"))) {
            ctx.redirect("/login/coordinador");
            return;
        }
        
        try {
            int planId = Integer.parseInt(ctx.pathParam("id"));
            String accion = ctx.formParam("accion");
            String comentarios = ctx.formParam("comentarios");
            
            boolean success;
            if ("aprobar".equals(accion)) {
                success = planModel.updateEstado(planId, "aprobado_coordinador", comentarios, "coordinador");
            } else {
                success = planModel.updateEstado(planId, "rechazado", comentarios, "coordinador");
            }
            
            if (success) {
                ctx.redirect("/coordinador/planes?success=true");
            } else {
                ctx.redirect("/coordinador/plan/" + planId + "/revisar?error=true");
            }
            
        } catch (Exception e) {
            ctx.redirect("/coordinador/planes?error=true");
        }
    }
    
    private Map<String, Object> getUserFromSession(Context ctx) {
        var session = ctx.req().getSession(false);
        if (session == null) return null;
        
        Integer userId = (Integer) session.getAttribute("user_id");
        String userType = (String) session.getAttribute("user_type");
        String userName = (String) session.getAttribute("user_name");
        
        if (userId == null || userType == null) return null;
        
        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("tipo", userType);
        user.put("nombres", userName != null ? userName : "Usuario");
        user.put("apellidos", "");
        
        return user;
    }
}