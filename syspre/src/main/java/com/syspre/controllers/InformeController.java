package com.syspre.controllers;

import com.syspre.models.InformeFinalModel;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InformeController {
    private final InformeFinalModel informeModel = new InformeFinalModel();
    
    // Estudiante - Ver informe final
    public void estudianteVerInforme(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"estudiante".equals(userData.get("tipo"))) {
            ctx.redirect("/login/estudiante");
            return;
        }
        
        int estudianteId = (Integer) userData.get("id");
        Map<String, Object> informe = informeModel.findByEstudianteId(estudianteId);
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("informe", informe);
        
        ctx.render("estudiante_ver_informe_final", model);
    }
    
    // Estudiante - Registro de informe (formulario)
    public void estudianteRegistroInformePage(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"estudiante".equals(userData.get("tipo"))) {
            ctx.redirect("/login/estudiante");
            return;
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        
        ctx.render("registro_informe_final", model);
    }
    
    // Estudiante - Crear informe (submit)
    public void estudianteCrearInforme(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"estudiante".equals(userData.get("tipo"))) {
            ctx.redirect("/login/estudiante");
            return;
        }
        
        try {
            Map<String, Object> informeData = new HashMap<>();
            informeData.put("estudiante_id", userData.get("id"));
            informeData.put("resumen_ejecutivo", ctx.formParam("resumen_ejecutivo"));
            informeData.put("metodologia", ctx.formParam("metodologia"));
            informeData.put("resultados", ctx.formParam("resultados"));
            informeData.put("conclusiones", ctx.formParam("conclusiones"));
            informeData.put("recomendaciones", ctx.formParam("recomendaciones"));
            informeData.put("archivo_informe", ctx.formParam("archivo_informe")); // TODO: File upload
            
            boolean success = informeModel.create(informeData);
            
            if (success) {
                ctx.redirect("/estudiante/informe/ver?success=true");
            } else {
                Map<String, Object> model = new HashMap<>();
                model.put("user", userData);
                model.put("error", "Error al crear el informe final");
                model.put("formData", informeData);
                ctx.render("registro_informe_final", model);
            }
            
        } catch (Exception e) {
            Map<String, Object> model = new HashMap<>();
            model.put("user", userData);
            model.put("error", "Error en los datos del formulario: " + e.getMessage());
            ctx.render("registro_informe_final", model);
        }
    }
    
    // Estudiante - Enviar informe para calificación
    public void estudianteEnviarInforme(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"estudiante".equals(userData.get("tipo"))) {
            ctx.redirect("/login/estudiante");
            return;
        }
        
        try {
            int informeId = Integer.parseInt(ctx.pathParam("id"));
            boolean success = informeModel.enviarInforme(informeId);
            
            if (success) {
                ctx.redirect("/estudiante/informe/ver?success=sent");
            } else {
                ctx.redirect("/estudiante/informe/ver?error=true");
            }
            
        } catch (Exception e) {
            ctx.redirect("/estudiante/informe/ver?error=true");
        }
    }
    
    // Docente - Ver informes pendientes para calificar
    public void docenteVerInformesPendientes(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"docente".equals(userData.get("tipo"))) {
            ctx.redirect("/login/docente");
            return;
        }
        
        List<Map<String, Object>> informesPendientes = informeModel.findPendientesParaDocente();
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("informes", informesPendientes);
        
        ctx.render("docente_informes_pendientes", model);
    }
    
    // Docente - Calificar informe (formulario)
    public void docenteCalificarInformePage(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"docente".equals(userData.get("tipo"))) {
            ctx.redirect("/login/docente");
            return;
        }
        
        int informeId = Integer.parseInt(ctx.pathParam("id"));
        Map<String, Object> informe = informeModel.findById(informeId);
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("informe", informe);
        
        ctx.render("docente_calificar_informe", model);
    }
    
    // Docente - Procesar calificación de informe
    public void docenteCalificarInforme(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"docente".equals(userData.get("tipo"))) {
            ctx.redirect("/login/docente");
            return;
        }
        
        try {
            int informeId = Integer.parseInt(ctx.pathParam("id"));
            double calificacion = Double.parseDouble(ctx.formParam("calificacion"));
            String comentarios = ctx.formParam("comentarios");
            
            boolean success = informeModel.calificarPorDocente(informeId, calificacion, comentarios);
            
            if (success) {
                ctx.redirect("/docente/informes?success=true");
            } else {
                ctx.redirect("/docente/informe/" + informeId + "/calificar?error=true");
            }
            
        } catch (Exception e) {
            ctx.redirect("/docente/informes?error=true");
        }
    }
    
    // Coordinador - Ver informes pendientes
    public void coordinadorVerInformesPendientes(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"coordinador".equals(userData.get("tipo"))) {
            ctx.redirect("/login/coordinador");
            return;
        }
        
        List<Map<String, Object>> informesPendientes = informeModel.findPendientesParaCoordinador();
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("informes", informesPendientes);
        
        ctx.render("coordinador_informe", model);
    }
    
    // Coordinador - Revisar informe (formulario)
    public void coordinadorRevisarInformePage(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"coordinador".equals(userData.get("tipo"))) {
            ctx.redirect("/login/coordinador");
            return;
        }
        
        int informeId = Integer.parseInt(ctx.pathParam("id"));
        Map<String, Object> informe = informeModel.findById(informeId);
        
        Map<String, Object> model = new HashMap<>();
        model.put("user", userData);
        model.put("informe", informe);
        
        ctx.render("revisar_informe", model);
    }
    
    // Coordinador - Procesar revisión de informe
    public void coordinadorRevisarInforme(Context ctx) {
        Map<String, Object> userData = getUserFromSession(ctx);
        if (userData == null || !"coordinador".equals(userData.get("tipo"))) {
            ctx.redirect("/login/coordinador");
            return;
        }
        
        try {
            int informeId = Integer.parseInt(ctx.pathParam("id"));
            double calificacion = Double.parseDouble(ctx.formParam("calificacion"));
            String comentarios = ctx.formParam("comentarios");
            
            boolean success = informeModel.aprobarPorCoordinador(informeId, calificacion, comentarios);
            
            if (success) {
                ctx.redirect("/coordinador/informes?success=true");
            } else {
                ctx.redirect("/coordinador/informe/" + informeId + "/revisar?error=true");
            }
            
        } catch (Exception e) {
            ctx.redirect("/coordinador/informes?error=true");
        }
    }
    
    private Map<String, Object> getUserFromSession(Context ctx) {
        return ctx.sessionAttribute("user");
    }
}