package com.syspre.controllers;

import com.syspre.config.DatabaseConfig;
import com.syspre.middleware.AuthMiddleware;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class DocumentosEspecialidadController {

    public void subirDocumentos(Context ctx) {
        Map<String, Object> user = ctx.sessionAttribute("user");
        if (user == null || !"coordinador".equals(user.get("tipo"))) {
            ctx.redirect("/");
            return;
        }

        int coordinadorId = (int) user.get("id");
        String especialidad = (String) user.get("especialidad");

        try {
            // Procesar documento 1
            UploadedFile documento1 = ctx.uploadedFile("documento1");
            String titulo1 = ctx.formParam("titulo1");
            String descripcion1 = ctx.formParam("descripcion1");

            if (documento1 != null && titulo1 != null && !titulo1.trim().isEmpty()) {
                guardarDocumento(coordinadorId, especialidad, documento1, titulo1, descripcion1, "principal");
            }

            // Procesar documento 2
            UploadedFile documento2 = ctx.uploadedFile("documento2");
            String titulo2 = ctx.formParam("titulo2");
            String descripcion2 = ctx.formParam("descripcion2");

            if (documento2 != null && titulo2 != null && !titulo2.trim().isEmpty()) {
                guardarDocumento(coordinadorId, especialidad, documento2, titulo2, descripcion2, "secundario");
            }

            ctx.sessionAttribute("mensaje", "Documentos subidos exitosamente");
            ctx.sessionAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            System.err.println("Error subiendo documentos: " + e.getMessage());
            ctx.sessionAttribute("mensaje", "Error al subir documentos: " + e.getMessage());
            ctx.sessionAttribute("tipoMensaje", "error");
        }

        ctx.redirect("/coordinador/dashboard");
    }

    private void guardarDocumento(int coordinadorId, String especialidad, UploadedFile archivo, 
                                 String titulo, String descripcion, String tipo) throws IOException, SQLException {
        
        // Crear directorio si no existe
        String uploadDir = "uploads/documentos_especialidad/" + especialidad;
        Path dirPath = Paths.get(uploadDir);
        Files.createDirectories(dirPath);

        // Generar nombre único para el archivo
        String extension = getFileExtension(archivo.filename());
        String nombreArchivo = System.currentTimeMillis() + "_" + coordinadorId + extension;
        String rutaArchivo = uploadDir + "/" + nombreArchivo;

        // Guardar archivo físico
        try (FileOutputStream fos = new FileOutputStream(rutaArchivo)) {
            fos.write(archivo.content().readAllBytes());
        }

        // Guardar en base de datos
        String sql = """
            INSERT INTO documentos_especialidad 
            (coordinador_id, especialidad, titulo, descripcion, archivo_path, archivo_nombre, archivo_size, tipo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, coordinadorId);
            stmt.setString(2, especialidad);
            stmt.setString(3, titulo);
            stmt.setString(4, descripcion);
            stmt.setString(5, rutaArchivo);
            stmt.setString(6, archivo.filename());
            stmt.setLong(7, archivo.size());
            stmt.setString(8, tipo);
            
            stmt.executeUpdate();
            
            System.out.println("✅ Documento guardado: " + titulo + " para especialidad " + especialidad);
        }
    }

    public void descargarDocumento(Context ctx) {
        // Intentar múltiples formas de obtener el usuario de la sesión
        Map<String, Object> user = ctx.sessionAttribute("user");
        
        // Debug básico de sesión
        System.out.println("🔍 Descarga - Usuario: " + (user != null ? user.get("email") : "NO AUTH"));
        
        if (user == null) {
            // Intentar reconstruir el objeto user desde otras variables de sesión
            String userId = ctx.sessionAttribute("userId");
            String userType = ctx.sessionAttribute("userType");
            String userEspecialidad = ctx.sessionAttribute("user_especialidad");
            String userEmail = ctx.sessionAttribute("user_email");
            
            if (userId != null && userType != null) {
                user = new java.util.HashMap<>();
                user.put("id", Integer.parseInt(userId.toString()));
                user.put("tipo", userType);
                user.put("especialidad", userEspecialidad);
                user.put("email", userEmail);
                System.out.println("✅ Usuario reconstruido desde variables de sesión");
            } else {
                System.out.println("🚫 Usuario no autenticado para descarga - no hay variables de sesión");
                ctx.status(401).result("No autenticado. Por favor, inicia sesión nuevamente.");
                return;
            }
        }

        int documentoId = Integer.parseInt(ctx.pathParam("id"));
        String especialidadUsuario = (String) user.get("especialidad");
        String tipoUsuario = (String) user.get("tipo");
        
        System.out.println("📥 Descarga solicitada - ID: " + documentoId + ", Usuario: " + user.get("email") + ", Especialidad: " + especialidadUsuario);

        String sql = """
            SELECT archivo_path, archivo_nombre, especialidad
            FROM documentos_especialidad 
            WHERE id = ? AND especialidad = ? AND activo = TRUE
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, documentoId);
            stmt.setString(2, especialidadUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String rutaArchivo = rs.getString("archivo_path");
                String nombreArchivo = rs.getString("archivo_nombre");
                
                System.out.println("📁 Archivo encontrado: " + rutaArchivo);

                File archivo = new File(rutaArchivo);
                if (archivo.exists()) {
                    // Detectar tipo de contenido basado en extensión
                    String contentType = "application/octet-stream";
                    if (nombreArchivo.toLowerCase().endsWith(".pdf")) {
                        contentType = "application/pdf";
                    } else if (nombreArchivo.toLowerCase().endsWith(".txt")) {
                        contentType = "text/plain";
                    } else if (nombreArchivo.toLowerCase().endsWith(".doc") || nombreArchivo.toLowerCase().endsWith(".docx")) {
                        contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                    }
                    
                    System.out.println("📤 Enviando archivo: " + nombreArchivo + " (" + contentType + ")");
                    
                    ctx.header("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"");
                    ctx.header("Content-Type", contentType);
                    ctx.result(Files.readAllBytes(archivo.toPath()));
                } else {
                    System.out.println("❌ Archivo no existe físicamente: " + rutaArchivo);
                    ctx.status(404).result("Archivo no encontrado");
                }
            } else {
                System.out.println("🚫 Sin acceso al documento ID " + documentoId + " para especialidad " + especialidadUsuario);
                ctx.status(403).result("No tienes acceso a este documento");
            }

        } catch (Exception e) {
            System.err.println("❌ Error descargando documento: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).result("Error interno del servidor");
        }
    }

    public void eliminarDocumento(Context ctx) {
        Map<String, Object> user = ctx.sessionAttribute("user");
        if (user == null || !"coordinador".equals(user.get("tipo"))) {
            ctx.redirect("/");
            return;
        }

        int documentoId = Integer.parseInt(ctx.pathParam("id"));
        int coordinadorId = (int) user.get("id");

        String sql = """
            UPDATE documentos_especialidad 
            SET activo = FALSE 
            WHERE id = ? AND coordinador_id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, documentoId);
            stmt.setInt(2, coordinadorId);
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                ctx.sessionAttribute("mensaje", "Documento eliminado exitosamente");
                ctx.sessionAttribute("tipoMensaje", "success");
            } else {
                ctx.sessionAttribute("mensaje", "No se pudo eliminar el documento");
                ctx.sessionAttribute("tipoMensaje", "error");
            }

        } catch (SQLException e) {
            System.err.println("Error eliminando documento: " + e.getMessage());
            ctx.sessionAttribute("mensaje", "Error al eliminar documento");
            ctx.sessionAttribute("tipoMensaje", "error");
        }

        ctx.redirect("/coordinador/dashboard");
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }
}