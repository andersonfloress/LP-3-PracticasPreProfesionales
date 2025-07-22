package com.syspre.controllers;

import com.syspre.models.DocumentoReglamentoModel;
import com.syspre.models.UserModel;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentoController {
    private final DocumentoReglamentoModel documentoModel = new DocumentoReglamentoModel();
    private final UserModel userModel = new UserModel();

    public void estudianteDocumentos(Context ctx) {
        try {
            String userId = ctx.sessionAttribute("userId");
            if (userId == null) {
                ctx.redirect("/login/estudiante");
                return;
            }

            // Obtener información del usuario
            Map<String, Object> usuario = userModel.findById(Integer.parseInt(userId));
            if (usuario == null) {
                ctx.redirect("/login/estudiante");
                return;
            }

            // Preparar datos para la vista
            Map<String, Object> model = new HashMap<>();
            model.put("userName", usuario.get("nombre_completo"));
            model.put("planStatus", "Aprobado"); // TODO: obtener estado real
            model.put("reportesCount", "3"); // TODO: obtener conteo real
            model.put("informeStatus", "Calificado"); // TODO: obtener estado real
            model.put("sustentacionStatus", "Programada"); // TODO: obtener estado real
            model.put("planFechaAprobacion", "15/07/2025"); // TODO: obtener fecha real
            model.put("informeCalificacion", "18/20"); // TODO: obtener calificación real
            model.put("certificadoStatus", "En proceso");

            ctx.render("estudiante_documentos.html", model);

        } catch (Exception e) {
            System.err.println("Error en estudianteDocumentos: " + e.getMessage());
            ctx.status(500).result("Error interno del servidor");
        }
    }

    public void coordinadorGestionarDocumentos(Context ctx) {
        try {
            String userId = ctx.sessionAttribute("userId");
            if (userId == null) {
                ctx.redirect("/login/coordinador");
                return;
            }

            if ("GET".equals(ctx.method().name())) {
                // Mostrar página de gestión
                mostrarGestionDocumentos(ctx, userId);
            } else if ("POST".equals(ctx.method().name())) {
                // Procesar subida de documento
                procesarSubidaDocumento(ctx, userId);
            }

        } catch (Exception e) {
            System.err.println("Error en coordinadorGestionarDocumentos: " + e.getMessage());
            ctx.status(500).result("Error interno del servidor");
        }
    }

    private void mostrarGestionDocumentos(Context ctx, String userId) {
        try {
            // Obtener información del coordinador
            Map<String, Object> usuario = userModel.findById(Integer.parseInt(userId));
            if (usuario == null) {
                ctx.redirect("/login/coordinador");
                return;
            }

            // Obtener documentos de la especialidad del coordinador
            String especialidad = (String) usuario.get("especialidad");
            List<Map<String, Object>> documentos = documentoModel.findByEspecialidad(especialidad);

            // Preparar modelo para la vista
            Map<String, Object> model = new HashMap<>();
            model.put("userName", usuario.get("nombre_completo"));
            model.put("especialidad", especialidad);
            model.put("documentos", documentos);

            ctx.render("coordinador_gestionar_documentos.html", model);

        } catch (Exception e) {
            System.err.println("Error en mostrarGestionDocumentos: " + e.getMessage());
            ctx.status(500).result("Error interno del servidor");
        }
    }

    private void procesarSubidaDocumento(Context ctx, String userId) {
        try {
            // Obtener datos del formulario
            String nombre = ctx.formParam("nombre");
            String descripcion = ctx.formParam("descripcion");
            String tipoDocumento = ctx.formParam("tipo_documento");
            
            // Obtener especialidad del coordinador
            Map<String, Object> usuario = userModel.findById(Integer.parseInt(userId));
            String especialidad = (String) usuario.get("especialidad");

            // TODO: Procesar archivo subido con FileController
            String archivoPath = "documentos/temp_" + System.currentTimeMillis() + ".pdf";

            // Crear documento en la base de datos
            Map<String, Object> nuevoDocumento = new HashMap<>();
            nuevoDocumento.put("nombre", nombre);
            nuevoDocumento.put("descripcion", descripcion);
            nuevoDocumento.put("especialidad", especialidad);
            nuevoDocumento.put("archivo_path", archivoPath);
            nuevoDocumento.put("tipo_documento", tipoDocumento);
            nuevoDocumento.put("es_activo", true);

            boolean exito = documentoModel.create(nuevoDocumento);

            if (exito) {
                // Redirect con mensaje de éxito
                ctx.redirect("/coordinador/documentos/gestionar?success=true");
            } else {
                // Redirect con mensaje de error
                ctx.redirect("/coordinador/documentos/gestionar?error=true");
            }

        } catch (Exception e) {
            System.err.println("Error procesando subida de documento: " + e.getMessage());
            ctx.redirect("/coordinador/documentos/gestionar?error=true");
        }
    }

    public void listarDocumentosEspecialidad(Context ctx) {
        try {
            String especialidad = ctx.queryParam("especialidad");
            if (especialidad == null) {
                especialidad = "Ingeniería de Sistemas"; // Default
            }

            List<Map<String, Object>> documentos = documentoModel.findByEspecialidadActivos(especialidad);

            Map<String, Object> model = new HashMap<>();
            model.put("documentos", documentos);
            model.put("especialidad", especialidad);

            ctx.json(model);

        } catch (Exception e) {
            System.err.println("Error listando documentos: " + e.getMessage());
            ctx.status(500).json("{\"error\": \"Error interno del servidor\"}");
        }
    }

    public void desactivarDocumento(Context ctx) {
        try {
            String documentoId = ctx.pathParam("id");
            String userId = ctx.sessionAttribute("userId");
            
            if (userId == null) {
                ctx.status(401).json("{\"error\": \"No autenticado\"}");
                return;
            }

            // Verificar que es coordinador
            Map<String, Object> usuario = userModel.findById(Integer.parseInt(userId));
            if (!"coordinador".equals(usuario.get("tipo_usuario"))) {
                ctx.status(403).json("{\"error\": \"Sin permisos\"}");
                return;
            }

            boolean exito = documentoModel.desactivar(Integer.parseInt(documentoId));

            if (exito) {
                ctx.json("{\"success\": true}");
            } else {
                ctx.status(400).json("{\"error\": \"Error desactivando documento\"}");
            }

        } catch (Exception e) {
            System.err.println("Error desactivando documento: " + e.getMessage());
            ctx.status(500).json("{\"error\": \"Error interno del servidor\"}");
        }
    }
}