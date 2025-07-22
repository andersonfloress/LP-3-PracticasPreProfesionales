package com.syspre.controllers;

import com.syspre.models.UserModel;
import com.syspre.models.PlanPracticaModel;
import com.syspre.models.EstadoPlan;
import com.syspre.utils.DatabaseQuery;
import io.javalin.http.Context;

import java.time.LocalDate;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardController {
    private final UserModel userModel = new UserModel();
    private final PlanPracticaModel planModel = new PlanPracticaModel();
    
    public void adminDashboard(Context ctx) {
        if (!isAuthenticated(ctx, "admin")) {
            ctx.redirect("/login/admin");
            return;
        }
        
        Map<String, Object> user = getUserFromSession(ctx);
        String success = ctx.queryParam("success");
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", user);
        if (success != null) {
            model.put("success", success);
        }
        
        // Agregar estadísticas por defecto para evitar errores de Thymeleaf
        model.put("totalUsuarios", 0);
        model.put("usuariosActivos", 0);
        model.put("totalEstudiantes", 0);
        model.put("estudiantesEnPracticas", 0);
        model.put("totalDocentes", 0);
        model.put("totalCoordinadores", 0);
        model.put("practicasActivas", 0);
        
        ctx.render("admin_dashboard", model);
    }
    
    public void coordinadorDashboard(Context ctx) {
        if (!isAuthenticated(ctx, "coordinador")) {
            ctx.redirect("/login/coordinador");
            return;
        }
        
        Map<String, Object> user = getUserFromSession(ctx);
        Map<String, Object> model = new HashMap<>();
        model.put("user", user);
        
        // Obtener especialidad del coordinador
        String especialidad = (String) user.get("especialidad");
        if (especialidad == null) especialidad = "Ingeniería de Sistemas"; // fallback
        
        // Obtener estadísticas reales
        Map<String, Integer> stats = DatabaseQuery.getEstadisticasCoordinador(especialidad);
        model.put("estudiantesActivos", stats.get("estudiantesActivos"));
        model.put("planesAprobados", stats.get("planesAprobados"));
        model.put("asignacionesPendientes", stats.get("asignacionesPendientes"));
        
        // Obtener estudiantes sin asignar
        List<Map<String, Object>> estudiantesSinAsignar = DatabaseQuery.getEstudiantesSinAsignar(especialidad);
        // Asegurar que la lista no sea null y tenga valores por defecto
        if (estudiantesSinAsignar != null) {
            for (Map<String, Object> estudiante : estudiantesSinAsignar) {
                if (estudiante.get("nombres") == null) estudiante.put("nombres", "");
                if (estudiante.get("apellidos") == null) estudiante.put("apellidos", "");
            }
        } else {
            estudiantesSinAsignar = java.util.Collections.emptyList();
        }
        model.put("estudiantesSinAsignar", estudiantesSinAsignar);
        
        // Obtener estudiantes con docente asignado
        List<Map<String, Object>> estudiantesConAsignacion = new ArrayList<>();
        try {
            estudiantesConAsignacion = DatabaseQuery.getEstudiantesConAsignacion(especialidad);
            // Validar y corregir datos nulos
            if (estudiantesConAsignacion != null) {
                for (Map<String, Object> estudiante : estudiantesConAsignacion) {
                    if (estudiante.get("nombres") == null) estudiante.put("nombres", "");
                    if (estudiante.get("apellidos") == null) estudiante.put("apellidos", "");
                    if (estudiante.get("docente_nombres") == null) estudiante.put("docente_nombres", "Sin asignar");
                    if (estudiante.get("docente_apellidos") == null) estudiante.put("docente_apellidos", "");
                    if (estudiante.get("fecha_asignacion") == null) estudiante.put("fecha_asignacion", "No definida");
                }
            } else {
                estudiantesConAsignacion = new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error consultando estudiantes con asignación: " + e.getMessage());
            estudiantesConAsignacion = new ArrayList<>();
        }
        model.put("estudiantesConAsignacion", estudiantesConAsignacion);
        
        // Obtener docentes de la misma especialidad
        List<Map<String, Object>> docentes = new ArrayList<>();
        try {
            docentes = DatabaseQuery.getDocentesPorEspecialidad(especialidad);
            if (docentes != null) {
                for (Map<String, Object> docente : docentes) {
                    if (docente.get("nombres") == null) docente.put("nombres", "");
                    if (docente.get("apellidos") == null) docente.put("apellidos", "");
                }
            } else {
                docentes = new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error consultando docentes: " + e.getMessage());
            docentes = new ArrayList<>();
        }
        model.put("docentes", docentes);
        
        // Obtener documentos de la especialidad
        List<Map<String, Object>> documentosEspecialidad = new ArrayList<>();
        try {
            documentosEspecialidad = DatabaseQuery.getDocumentosPorEspecialidad(especialidad);
            if (documentosEspecialidad == null) {
                documentosEspecialidad = new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error consultando documentos especialidad: " + e.getMessage());
            documentosEspecialidad = new ArrayList<>();
        }
        model.put("documentosEspecialidad", documentosEspecialidad);
        
        // Obtener todos los planes de la especialidad
        List<Map<String, Object>> todosLosPlanes = planModel.findPlanesPorEspecialidad(especialidad);
        if (todosLosPlanes == null) todosLosPlanes = new ArrayList<>();
        
        // Filtrar planes pendientes de revisión
        List<Map<String, Object>> planesParaRevisar = todosLosPlanes.stream()
            .filter(p -> "pendiente".equals(p.get("estado")) || "enviado".equals(p.get("estado")) || "aprobado_docente".equals(p.get("estado")))
            .collect(java.util.stream.Collectors.toList());
        
        // Variables adicionales
        model.put("todosLosPlanes", todosLosPlanes);
        model.put("informesPendientes", 0);
        model.put("sustentacionesProgramadas", 0);
        model.put("procesosFinalizados", (int) todosLosPlanes.stream().filter(p -> "aprobado_coordinador".equals(p.get("estado"))).count());
        model.put("planesParaRevisar", planesParaRevisar);
        
        ctx.render("coordinador_dashboard", model);
    }
    
    public void docenteDashboard(Context ctx) {
        if (!isAuthenticated(ctx, "docente")) {
            ctx.redirect("/login/docente");
            return;
        }
        
        Map<String, Object> user = getUserFromSession(ctx);
        int docenteId = (Integer) user.get("id");
        
        // Obtener planes asignados al docente
        List<Map<String, Object>> planesAsignados = planModel.findPlanesAsignadosDocente(docenteId);
        if (planesAsignados == null) planesAsignados = new ArrayList<>();
        
        // Obtener reportes pendientes de calificar
        List<Map<String, Object>> reportesPendientes = new ArrayList<>();
        try {
            reportesPendientes = DatabaseQuery.getReportesPendientesDocente(docenteId);
            if (reportesPendientes == null) reportesPendientes = new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error obteniendo reportes pendientes: " + e.getMessage());
        }
        
        // Obtener informes pendientes de calificar
        List<Map<String, Object>> informesPendientes = new ArrayList<>();
        try {
            informesPendientes = DatabaseQuery.getInformesPendientesDocente(docenteId);
            if (informesPendientes == null) informesPendientes = new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error obteniendo informes pendientes: " + e.getMessage());
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", user);
        model.put("planesAsignados", planesAsignados);
        model.put("reportesPendientes", reportesPendientes);
        model.put("informesPendientes", informesPendientes);
        
        // Estadísticas
        model.put("estudiantesAsignados", planesAsignados.size());
        model.put("planesPendientes", (int) planesAsignados.stream().filter(p -> 
            "enviado".equals(p.get("estado")) || "pendiente".equals(p.get("estado"))).count());
        model.put("reportesPendientesCount", reportesPendientes.size());
        model.put("informesPendientesCount", informesPendientes.size());
        model.put("informesPendientes", 0);
        
        // Variables para listas vacías
        model.put("planesParaAprobar", java.util.Collections.emptyList());
        model.put("reportesParaCalificar", java.util.Collections.emptyList());
        
        ctx.render("docente_dashboard", model);
    }
    
    // Dashboard completo del docente con las 4 secciones principales
    public void docenteDashboardCompleto(Context ctx) {
        if (!isAuthenticated(ctx, "docente")) {
            ctx.redirect("/login/docente");
            return;
        }
        
        Map<String, Object> user = getUserFromSession(ctx);
        int docenteId = (Integer) user.get("id");
        
        // Obtener planes asignados al docente
        List<Map<String, Object>> planesAsignados = planModel.findPlanesAsignadosDocente(docenteId);
        if (planesAsignados == null) planesAsignados = new ArrayList<>();
        
        // Obtener reportes pendientes de calificar
        List<Map<String, Object>> reportesPendientes = new ArrayList<>();
        try {
            reportesPendientes = DatabaseQuery.getReportesPendientesDocente(docenteId);
            if (reportesPendientes == null) reportesPendientes = new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error obteniendo reportes pendientes: " + e.getMessage());
        }
        
        // Obtener informes pendientes de calificar
        List<Map<String, Object>> informesPendientes = new ArrayList<>();
        try {
            informesPendientes = DatabaseQuery.getInformesPendientesDocente(docenteId);
            if (informesPendientes == null) informesPendientes = new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error obteniendo informes pendientes: " + e.getMessage());
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", user);
        model.put("planesAsignados", planesAsignados);
        model.put("reportesPendientes", reportesPendientes);
        model.put("informesPendientes", informesPendientes);
        
        // Estadísticas para las 4 secciones
        model.put("estudiantesAsignados", planesAsignados.size());
        model.put("planesPendientes", (int) planesAsignados.stream().filter(p -> 
            "enviado".equals(p.get("estado")) || "pendiente".equals(p.get("estado"))).count());
        model.put("reportesPendientesCount", reportesPendientes.size());
        model.put("informesPendientesCount", informesPendientes.size());
        
        ctx.render("docente_dashboard_completo", model);
    }
    
    public void estudianteDashboard(Context ctx) {
        if (!isAuthenticated(ctx, "estudiante")) {
            ctx.redirect("/login/estudiante");
            return;
        }
        
        Map<String, Object> user = getUserFromSession(ctx);
        Map<String, Object> model = new HashMap<>();
        model.put("user", user);
        
        // Verificar si el estudiante ya tiene un plan de práctica
        int estudianteId = (Integer) user.get("id");
        Map<String, Object> plan = planModel.findByEstudianteId(estudianteId);
        
        // Sistema de estados del flujo de prácticas
        EstadoPlan estadoPlan = determinarEstadoPlan(plan);
        model.put("estadoPlan", estadoPlan);
        model.put("plan", plan);
        
        // Agregar información específica según el estado (7 etapas del proceso)
        switch (estadoPlan) {
            case SIN_PLAN:
                model.put("mostrarBotonCrearPlan", true);
                model.put("mostrarProgreso", false);
                model.put("progreso", 0);
                model.put("siguientePaso", "Crear plan de prácticas");
                break;
            case PLAN_CREADO:
                model.put("mostrarBotonCrearPlan", false);
                model.put("mostrarProgreso", true);
                model.put("progreso", 14); // 1/7 * 100
                model.put("siguientePaso", "Esperando evaluación del docente");
                break;
            case EVALUADO_DOCENTE:
                model.put("mostrarBotonCrearPlan", false);
                model.put("mostrarProgreso", true);
                model.put("progreso", 28); // 2/7 * 100
                model.put("siguientePaso", "Esperando aprobación del coordinador");
                break;
            case APROBADO_COORDINADOR:
                model.put("mostrarBotonCrearPlan", false);
                model.put("mostrarProgreso", true);
                model.put("progreso", 42); // 3/7 * 100
                model.put("siguientePaso", "Esperando fecha de inicio de prácticas");
                model.put("fechaInicioDisponible", plan != null && plan.get("fecha_inicio_plan") != null);
                break;
            case EN_EJECUCION:
                model.put("mostrarBotonCrearPlan", false);
                model.put("mostrarProgreso", true);
                model.put("progreso", 57); // 4/7 * 100
                model.put("siguientePaso", "Prácticas en ejecución - Crear reportes semanales");
                model.put("puedeCrearReportes", true);
                break;
            case REPORTES_ACTIVOS:
                model.put("mostrarBotonCrearPlan", false);
                model.put("mostrarProgreso", true);
                model.put("progreso", 71); // 5/7 * 100
                model.put("siguientePaso", "Entregando reportes semanales");
                model.put("puedeCrearReportes", true);
                model.put("puedeCrearInformes", false);
                break;
            case INFORME_FINAL:
                model.put("mostrarBotonCrearPlan", false);
                model.put("mostrarProgreso", true);
                model.put("progreso", 85); // 6/7 * 100
                model.put("siguientePaso", "Crear informe final de prácticas");
                model.put("puedeCrearReportes", true);
                model.put("puedeCrearInformes", true);
                break;
            case SUSTENTACION:
                model.put("mostrarBotonCrearPlan", false);
                model.put("mostrarProgreso", true);
                model.put("progreso", 100); // 7/7 * 100
                model.put("siguientePaso", "Esperando fecha de sustentación");
                model.put("procesoCompletado", true);
                break;
        }
        model.put("tienePlan", plan != null);
        
        // Obtener documentos de la especialidad
        String especialidad = (String) user.get("especialidad");
        List<Map<String, Object>> documentosEspecialidad = DatabaseQuery.getDocumentosPorEspecialidad(especialidad);
        model.put("documentosEspecialidad", documentosEspecialidad);
        
        ctx.render("estudiante_dashboard", model);
    }
    
    private EstadoPlan determinarEstadoPlan(Map<String, Object> plan) {
        if (plan == null) {
            return EstadoPlan.SIN_PLAN;
        }
        
        String estado = (String) plan.get("estado");
        Date fechaInicioPlan = (Date) plan.get("fecha_inicio_plan");
        Date fechaFin = (Date) plan.get("fecha_fin");
        LocalDate hoy = LocalDate.now();
        
        // Verificar si existe un informe final aprobado por coordinador
        int estudianteId = (Integer) plan.get("estudiante_id");
        boolean tieneInformeFinalAprobado = verificarInformeFinalAprobado(estudianteId);
        
        switch (estado) {
            case "pendiente":
            case "borrador":
                return EstadoPlan.PLAN_CREADO;
                
            case "evaluado_docente":
            case "aprobado_docente":
                return EstadoPlan.EVALUADO_DOCENTE;
                
            case "aprobado_coordinador":
            case "aprobado":
                // Si tiene informe final aprobado, espera sustentación
                if (tieneInformeFinalAprobado) {
                    return EstadoPlan.SUSTENTACION;
                }
                
                // Si ya pasó la fecha de fin, puede crear informe final
                if (fechaFin != null && !fechaFin.toLocalDate().isAfter(hoy)) {
                    return EstadoPlan.INFORME_FINAL;
                }
                
                // Si ya comenzó y no terminó, está en reportes activos
                if (fechaInicioPlan != null && !fechaInicioPlan.toLocalDate().isAfter(hoy)) {
                    return EstadoPlan.REPORTES_ACTIVOS;
                }
                
                // Todavía no empieza, esperando fecha
                return EstadoPlan.APROBADO_COORDINADOR;
                
            case "en_ejecucion":
                // Si tiene informe final aprobado, espera sustentación
                if (tieneInformeFinalAprobado) {
                    return EstadoPlan.SUSTENTACION;
                }
                
                // Si ya pasó la fecha de fin, puede crear informe final
                if (fechaFin != null && !fechaFin.toLocalDate().isAfter(hoy)) {
                    return EstadoPlan.INFORME_FINAL;
                }
                
                // Está en período de prácticas, puede crear reportes
                return EstadoPlan.REPORTES_ACTIVOS;
                
            case "finalizado":
            case "completado":
                return EstadoPlan.SUSTENTACION;
                
            default:
                return EstadoPlan.PLAN_CREADO;
        }
    }
    
    private boolean verificarInformeFinalAprobado(int estudianteId) {
        try {
            // Consulta SQL para verificar si existe un informe final aprobado por coordinador
            String sql = "SELECT COUNT(*) as count FROM informes_finales WHERE estudiante_id = ? AND estado = 'aprobado_coordinador'";
            // Implementar consulta a base de datos
            // Por ahora, retornamos false hasta implementar la consulta completa
            return false;
        } catch (Exception e) {
            System.err.println("Error verificando informe final: " + e.getMessage());
            return false;
        }
    }
    
    private boolean isAuthenticated(Context ctx, String requiredType) {
        var session = ctx.req().getSession(false);
        if (session == null) return false;
        
        String userType = (String) session.getAttribute("user_type");
        return requiredType.equals(userType);
    }
    
    private Map<String, Object> getUserFromSession(Context ctx) {
        // Primero intentar con el nuevo sistema de sesiones
        Map<String, Object> user = ctx.sessionAttribute("user");
        if (user != null) {
            return user;
        }
        
        // Fallback al sistema anterior si existe
        var session = ctx.req().getSession();
        return (Map<String, Object>) session.getAttribute("user_data");
    }
}