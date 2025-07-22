package com.syspre.controllers;

import com.syspre.models.ReporteSemanalModel;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReporteController {
    private final ReporteSemanalModel reporteModel = new ReporteSemanalModel();
    
    // Estudiante - Ver reportes semanales
    public void estudianteVerReportes(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"estudiante".equals(userData.get("tipo"))) {
            ctx.redirect("/login/estudiante");
            return;
        }
        
        int estudianteId = (Integer) userData.get("id");
        List<Map<String, Object>> reportes = reporteModel.findByEstudianteId(estudianteId);
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("reportes", reportes);
        
        ctx.render("estudiante_reportes_semanales", model);
    }
    
    // Estudiante - Crear reporte (formulario)
    public void estudianteCrearReportePage(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"estudiante".equals(userData.get("tipo"))) {
            ctx.redirect("/login/estudiante");
            return;
        }
        
        int estudianteId = (Integer) userData.get("id");
        int nextSemana = reporteModel.getNextSemanaNumber(estudianteId);
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("semana_numero", nextSemana);
        
        ctx.render("estudiante_crear_reporte", model);
    }
    
    // Estudiante - Crear reporte (submit)
    public void estudianteCrearReporte(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"estudiante".equals(userData.get("tipo"))) {
            ctx.redirect("/login/estudiante");
            return;
        }
        
        try {
            Map<String, Object> reporteData = new HashMap<>();
            reporteData.put("estudiante_id", userData.get("id"));
            reporteData.put("semana_numero", Integer.parseInt(ctx.formParam("semana_numero")));
            reporteData.put("fecha_inicio", ctx.formParam("fecha_inicio"));
            reporteData.put("fecha_fin", ctx.formParam("fecha_fin"));
            reporteData.put("actividades_realizadas", ctx.formParam("actividades_realizadas"));
            reporteData.put("horas_trabajadas", Integer.parseInt(ctx.formParam("horas_trabajadas")));
            reporteData.put("aprendizajes", ctx.formParam("aprendizajes"));
            reporteData.put("dificultades", ctx.formParam("dificultades"));
            reporteData.put("archivo_reporte", ctx.formParam("archivo_reporte")); // TODO: File upload
            
            boolean success = reporteModel.create(reporteData);
            
            if (success) {
                ctx.redirect("/estudiante/reportes?success=true");
            } else {
                Map<String, Object> model = new HashMap<>();
                model.put("user", userData);
                model.put("error", "Error al crear el reporte semanal");
                model.put("formData", reporteData);
                ctx.render("estudiante_crear_reporte", model);
            }
            
        } catch (Exception e) {
            Map<String, Object> model = new HashMap<>();
            model.put("user", userData);
            model.put("error", "Error en los datos del formulario: " + e.getMessage());
            ctx.render("estudiante_crear_reporte", model);
        }
    }
    
    // Estudiante - Editar reporte (formulario)
    public void estudianteEditarReportePage(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"estudiante".equals(userData.get("tipo"))) {
            ctx.redirect("/login/estudiante");
            return;
        }
        
        int reporteId = Integer.parseInt(ctx.pathParam("id"));
        Map<String, Object> reporte = reporteModel.findById(reporteId);
        
        // Verificar que el reporte pertenece al estudiante
        if (reporte == null || !userData.get("id").equals(reporte.get("estudiante_id"))) {
            ctx.redirect("/estudiante/reportes?error=unauthorized");
            return;
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("reporte", reporte);
        
        ctx.render("editar_reporte_semanal", model);
    }
    
    // Estudiante - Actualizar reporte (submit)
    public void estudianteActualizarReporte(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"estudiante".equals(userData.get("tipo"))) {
            ctx.redirect("/login/estudiante");
            return;
        }
        
        try {
            int reporteId = Integer.parseInt(ctx.pathParam("id"));
            
            Map<String, Object> reporteData = new HashMap<>();
            reporteData.put("fecha_inicio", ctx.formParam("fecha_inicio"));
            reporteData.put("fecha_fin", ctx.formParam("fecha_fin"));
            reporteData.put("actividades_realizadas", ctx.formParam("actividades_realizadas"));
            reporteData.put("horas_trabajadas", Integer.parseInt(ctx.formParam("horas_trabajadas")));
            reporteData.put("aprendizajes", ctx.formParam("aprendizajes"));
            reporteData.put("dificultades", ctx.formParam("dificultades"));
            reporteData.put("archivo_reporte", ctx.formParam("archivo_reporte")); // TODO: File upload
            
            boolean success = reporteModel.update(reporteId, reporteData);
            
            if (success) {
                ctx.redirect("/estudiante/reportes?success=updated");
            } else {
                ctx.redirect("/estudiante/reporte/" + reporteId + "/editar?error=true");
            }
            
        } catch (Exception e) {
            ctx.redirect("/estudiante/reportes?error=true");
        }
    }
    
    // Docente - Ver reportes para calificar
    public void docenteVerReportes(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"docente".equals(userData.get("tipo"))) {
            ctx.redirect("/login/docente");
            return;
        }
        
        List<Map<String, Object>> reportesPendientes = reporteModel.findPendientesParaCalificar();
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("reportes", reportesPendientes);
        
        ctx.render("docente_ver_reportes", model);
    }
    
    // Docente - Calificar reporte (formulario)
    public void docenteCalificarReportePage(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"docente".equals(userData.get("tipo"))) {
            ctx.redirect("/login/docente");
            return;
        }
        
        int reporteId = Integer.parseInt(ctx.pathParam("id"));
        Map<String, Object> reporte = reporteModel.findById(reporteId);
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("reporte", reporte);
        
        ctx.render("calificar_reporte", model);
    }
    
    // Docente - Procesar calificación
    public void docenteCalificarReporte(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"docente".equals(userData.get("tipo"))) {
            ctx.redirect("/login/docente");
            return;
        }
        
        try {
            int reporteId = Integer.parseInt(ctx.pathParam("id"));
            double calificacion = Double.parseDouble(ctx.formParam("calificacion"));
            String comentarios = ctx.formParam("comentarios");
            
            boolean success = reporteModel.calificar(reporteId, calificacion, comentarios);
            
            if (success) {
                ctx.redirect("/docente/reportes?success=true");
            } else {
                ctx.redirect("/docente/reporte/" + reporteId + "/calificar?error=true");
            }
            
        } catch (Exception e) {
            ctx.redirect("/docente/reportes?error=true");
        }
    }
    
    // Coordinador - Ver reporte específico
    public void coordinadorVerReporte(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"coordinador".equals(userData.get("tipo"))) {
            ctx.redirect("/login/coordinador");
            return;
        }
        
        int reporteId = Integer.parseInt(ctx.pathParam("id"));
        Map<String, Object> reporte = reporteModel.findById(reporteId);
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("reporte", reporte);
        
        ctx.render("coordinador_ver_reporte", model);
    }
    
    private Map<String, Object> getUserFromSession(Context ctx) {
        return ctx.sessionAttribute("user");
    }
}