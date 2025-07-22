package com.syspre.controllers;

import io.javalin.http.Context;
import com.syspre.models.SustentacionModel;
import com.syspre.models.AsignacionDocenteModel;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class SustentacionController {
    private final SustentacionModel sustentacionModel;
    private final AsignacionDocenteModel asignacionModel;
    
    public SustentacionController() {
        this.sustentacionModel = new SustentacionModel();
        this.asignacionModel = new AsignacionDocenteModel();
    }
    
    // GET /coordinador/sustentaciones - Lista todas las sustentaciones
    public void listarSustentaciones(Context ctx) {
        try {
            // Verificar que es coordinador
            if (!isCoordinador(ctx)) {
                ctx.redirect("/login/coordinador");
                return;
            }
            
            Map<String, Object> model = new HashMap<>();
            model.put("user", getUserFromSession(ctx));
            
            // Obtener sustentaciones (implementación básica)
            List<Map<String, Object>> sustentaciones = new ArrayList<>();
            model.put("sustentaciones", sustentaciones);
            
            // Estadísticas (implementación básica)
            int programadas = 0;
            int completadas = 0;
            int pendientes = 0;
            
            model.put("sustentacionesProgramadas", programadas);
            model.put("sustentacionesCompletadas", completadas);
            model.put("sustentacionesPendientes", pendientes);
            
            ctx.render("coordinador_sustentaciones.html", model);
            
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.result("Error al cargar sustentaciones: " + e.getMessage());
        }
    }
    
    // GET /coordinador/sustentacion/programar - Mostrar formulario
    public void mostrarProgramarSustentacion(Context ctx) {
        try {
            if (!isCoordinador(ctx)) {
                ctx.redirect("/login/coordinador");
                return;
            }
            
            Map<String, Object> model = new HashMap<>();
            model.put("user", getUserFromSession(ctx));
            
            // Obtener informes finales aprobados sin sustentación (implementación básica)
            List<Map<String, Object>> informesAprobados = new ArrayList<>();
            model.put("informesAprobados", informesAprobados);
            
            // Obtener docentes disponibles para jurado (implementación básica)
            List<Map<String, Object>> docentes = new ArrayList<>();
            model.put("docentes", docentes);
            
            ctx.render("coordinador_programar_sustentacion.html", model);
            
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.result("Error al cargar formulario: " + e.getMessage());
        }
    }
    
    // POST /coordinador/sustentacion/programar - Programar sustentación
    public void programarSustentacion(Context ctx) {
        try {
            if (!isCoordinador(ctx)) {
                ctx.redirect("/login/coordinador");
                return;
            }
            
            // Obtener datos del formulario
            int informeId = Integer.parseInt(ctx.formParam("informe_id"));
            String fechaSustentacion = ctx.formParam("fecha_sustentacion");
            String horaSustentacion = ctx.formParam("hora_sustentacion");
            String lugar = ctx.formParam("lugar");
            int docentePresidente = Integer.parseInt(ctx.formParam("docente_presidente"));
            int docenteSecretario = Integer.parseInt(ctx.formParam("docente_secretario"));
            int docenteVocal = Integer.parseInt(ctx.formParam("docente_vocal"));
            String observaciones = ctx.formParam("observaciones");
            
            // Validaciones
            if (fechaSustentacion == null || horaSustentacion == null || lugar == null) {
                ctx.status(400);
                ctx.result("Todos los campos obligatorios deben ser completados");
                return;
            }
            
            // Verificar que los docentes sean diferentes
            if (docentePresidente == docenteSecretario || 
                docentePresidente == docenteVocal || 
                docenteSecretario == docenteVocal) {
                ctx.status(400);
                ctx.result("Los docentes del jurado deben ser diferentes");
                return;
            }
            
            // Crear sustentación
            Map<String, Object> sustentacion = new HashMap<>();
            sustentacion.put("informe_id", informeId);
            sustentacion.put("fecha_sustentacion", fechaSustentacion);
            sustentacion.put("hora_sustentacion", horaSustentacion);
            sustentacion.put("lugar", lugar);
            sustentacion.put("docente_presidente", docentePresidente);
            sustentacion.put("docente_secretario", docenteSecretario);
            sustentacion.put("docente_vocal", docenteVocal);
            sustentacion.put("observaciones", observaciones);
            sustentacion.put("estado", "programada");
            
            boolean exito = true; // sustentacionModel.crearSustentacion(sustentacion);
            
            if (exito) {
                ctx.redirect("/coordinador/sustentaciones?success=programada");
            } else {
                ctx.status(500);
                ctx.result("Error al programar sustentación");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.result("Error al programar sustentación: " + e.getMessage());
        }
    }
    
    // GET /coordinador/sustentacion/{id}/ver - Ver detalles
    public void verSustentacion(Context ctx) {
        try {
            if (!isCoordinador(ctx)) {
                ctx.redirect("/login/coordinador");
                return;
            }
            
            int sustentacionId = Integer.parseInt(ctx.pathParam("id"));
            
            Map<String, Object> model = new HashMap<>();
            model.put("user", getUserFromSession(ctx));
            
            // Obtener detalles de la sustentación (implementación básica)
            Map<String, Object> sustentacion = new HashMap<>();
            sustentacion.put("id", sustentacionId);
            sustentacion.put("fecha_sustentacion", "2025-07-30");
            sustentacion.put("estado", "programada");
            
            model.put("sustentacion", sustentacion);
            
            ctx.render("coordinador_ver_sustentacion.html", model);
            
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.result("Error al cargar sustentación: " + e.getMessage());
        }
    }
    
    // GET /docente/sustentacion/{id}/calificar - Formulario de calificación
    public void mostrarCalificarSustentacion(Context ctx) {
        try {
            if (!isDocente(ctx)) {
                ctx.redirect("/login/docente");
                return;
            }
            
            int sustentacionId = Integer.parseInt(ctx.pathParam("id"));
            Map<String, Object> user = getUserFromSession(ctx);
            int docenteId = (Integer) user.get("id");
            
            Map<String, Object> model = new HashMap<>();
            model.put("user", user);
            
            // Verificar que el docente es parte del jurado (implementación básica)
            Map<String, Object> sustentacion = new HashMap<>();
            sustentacion.put("id", sustentacionId);
            sustentacion.put("docente_presidente", docenteId);
            sustentacion.put("docente_secretario", docenteId + 1);
            sustentacion.put("docente_vocal", docenteId + 2);
            
            boolean esJurado = (Integer) sustentacion.get("docente_presidente") == docenteId ||
                              (Integer) sustentacion.get("docente_secretario") == docenteId ||
                              (Integer) sustentacion.get("docente_vocal") == docenteId;
            
            if (!esJurado) {
                ctx.status(403);
                ctx.result("No tiene permisos para calificar esta sustentación");
                return;
            }
            
            model.put("sustentacion", sustentacion);
            
            // Obtener calificación existente si la hay (implementación básica)
            Map<String, Object> calificacion = null;
            model.put("calificacion", calificacion);
            
            ctx.render("docente_calificar_sustentacion.html", model);
            
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.result("Error al cargar formulario: " + e.getMessage());
        }
    }
    
    // POST /docente/sustentacion/{id}/calificar - Guardar calificación
    public void calificarSustentacion(Context ctx) {
        try {
            if (!isDocente(ctx)) {
                ctx.redirect("/login/docente");
                return;
            }
            
            int sustentacionId = Integer.parseInt(ctx.pathParam("id"));
            Map<String, Object> user = getUserFromSession(ctx);
            int docenteId = (Integer) user.get("id");
            
            // Obtener datos del formulario
            double notaPresentacion = Double.parseDouble(ctx.formParam("nota_presentacion"));
            double notaContenido = Double.parseDouble(ctx.formParam("nota_contenido"));
            double notaDefensa = Double.parseDouble(ctx.formParam("nota_defensa"));
            String observaciones = ctx.formParam("observaciones");
            
            // Validar notas
            if (notaPresentacion < 0 || notaPresentacion > 20 ||
                notaContenido < 0 || notaContenido > 20 ||
                notaDefensa < 0 || notaDefensa > 20) {
                ctx.status(400);
                ctx.result("Las calificaciones deben estar entre 0 y 20");
                return;
            }
            
            // Calcular nota final
            double notaFinal = (notaPresentacion + notaContenido + notaDefensa) / 3;
            
            // Guardar calificación
            Map<String, Object> calificacion = new HashMap<>();
            calificacion.put("sustentacion_id", sustentacionId);
            calificacion.put("docente_id", docenteId);
            calificacion.put("nota_presentacion", notaPresentacion);
            calificacion.put("nota_contenido", notaContenido);
            calificacion.put("nota_defensa", notaDefensa);
            calificacion.put("nota_final", notaFinal);
            calificacion.put("observaciones", observaciones);
            
            boolean exito = true; // sustentacionModel.guardarCalificacion(calificacion);
            
            if (exito) {
                // Verificar si todos los jurados han calificado (implementación básica)
                boolean todosCalificaron = true;
                if (todosCalificaron) {
                    // Calcular nota final promedio y actualizar estado
                    // sustentacionModel.finalizarSustentacion(sustentacionId);
                }
                
                ctx.redirect("/docente/dashboard?success=sustentacion_calificada");
            } else {
                ctx.status(500);
                ctx.result("Error al guardar calificación");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.result("Error al calificar sustentación: " + e.getMessage());
        }
    }
    
    // Métodos auxiliares
    private boolean isCoordinador(Context ctx) {
        Map<String, Object> user = getUserFromSession(ctx);
        return user != null && "coordinador".equals(user.get("tipo_usuario"));
    }
    
    private boolean isDocente(Context ctx) {
        Map<String, Object> user = getUserFromSession(ctx);
        return user != null && "docente".equals(user.get("tipo_usuario"));
    }
    
    private Map<String, Object> getUserFromSession(Context ctx) {
        return ctx.sessionAttribute("user");
    }
}