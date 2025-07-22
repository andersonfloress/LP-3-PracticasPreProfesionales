package com.syspre.controllers;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileController {
    private static final String UPLOAD_BASE_PATH = "uploads";
    private static final String DOCUMENTOS_PATH = UPLOAD_BASE_PATH + "/documentos";
    private static final String PLANES_PATH = UPLOAD_BASE_PATH + "/planes";
    private static final String REPORTES_PATH = UPLOAD_BASE_PATH + "/reportes";
    private static final String INFORMES_PATH = UPLOAD_BASE_PATH + "/informes";
    
    // Tamaño máximo: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    // Tipos de archivo permitidos
    private static final String[] ALLOWED_EXTENSIONS = {".pdf", ".doc", ".docx", ".jpg", ".jpeg", ".png"};
    
    public static void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(DOCUMENTOS_PATH));
            Files.createDirectories(Paths.get(PLANES_PATH));
            Files.createDirectories(Paths.get(REPORTES_PATH));
            Files.createDirectories(Paths.get(INFORMES_PATH));
            System.out.println("✅ Directorios de archivos inicializados");
        } catch (IOException e) {
            System.err.println("❌ Error creando directorios: " + e.getMessage());
        }
    }
    
    public void uploadPlanFile(Context ctx) {
        try {
            UploadedFile file = ctx.uploadedFile("archivo_plan");
            if (file == null) {
                ctx.status(400).json("{\"error\": \"No se proporcionó archivo\"}");
                return;
            }
            
            String fileName = saveFile(file, PLANES_PATH);
            if (fileName != null) {
                ctx.json("{\"success\": true, \"fileName\": \"" + fileName + "\"}");
            } else {
                ctx.status(400).json("{\"error\": \"Error guardando archivo\"}");
            }
        } catch (Exception e) {
            ctx.status(500).json("{\"error\": \"Error interno del servidor\"}");
        }
    }
    
    public void uploadReporteFile(Context ctx) {
        try {
            UploadedFile file = ctx.uploadedFile("archivo_reporte");
            if (file == null) {
                ctx.status(400).json("{\"error\": \"No se proporcionó archivo\"}");
                return;
            }
            
            String fileName = saveFile(file, REPORTES_PATH);
            if (fileName != null) {
                ctx.json("{\"success\": true, \"fileName\": \"" + fileName + "\"}");
            } else {
                ctx.status(400).json("{\"error\": \"Error guardando archivo\"}");
            }
        } catch (Exception e) {
            ctx.status(500).json("{\"error\": \"Error interno del servidor\"}");
        }
    }
    
    public void uploadInformeFile(Context ctx) {
        try {
            UploadedFile file = ctx.uploadedFile("archivo_informe");
            if (file == null) {
                ctx.status(400).json("{\"error\": \"No se proporcionó archivo\"}");
                return;
            }
            
            String fileName = saveFile(file, INFORMES_PATH);
            if (fileName != null) {
                ctx.json("{\"success\": true, \"fileName\": \"" + fileName + "\"}");
            } else {
                ctx.status(400).json("{\"error\": \"Error guardando archivo\"}");
            }
        } catch (Exception e) {
            ctx.status(500).json("{\"error\": \"Error interno del servidor\"}");
        }
    }
    
    public void uploadDocumento(Context ctx) {
        try {
            UploadedFile file = ctx.uploadedFile("archivo_documento");
            if (file == null) {
                ctx.status(400).json("{\"error\": \"No se proporcionó archivo\"}");
                return;
            }
            
            String fileName = saveFile(file, DOCUMENTOS_PATH);
            if (fileName != null) {
                ctx.json("{\"success\": true, \"fileName\": \"" + fileName + "\"}");
            } else {
                ctx.status(400).json("{\"error\": \"Error guardando archivo\"}");
            }
        } catch (Exception e) {
            ctx.status(500).json("{\"error\": \"Error interno del servidor\"}");
        }
    }
    
    public void downloadFile(Context ctx) {
        try {
            String tipo = ctx.pathParam("tipo");
            String fileName = ctx.pathParam("fileName");
            
            String basePath;
            switch (tipo) {
                case "plan":
                    basePath = PLANES_PATH;
                    break;
                case "reporte":
                    basePath = REPORTES_PATH;
                    break;
                case "informe":
                    basePath = INFORMES_PATH;
                    break;
                case "documento":
                    basePath = DOCUMENTOS_PATH;
                    break;
                default:
                    ctx.status(404).result("Tipo de archivo no válido");
                    return;
            }
            
            Path filePath = Paths.get(basePath, fileName);
            File file = filePath.toFile();
            
            if (!file.exists()) {
                ctx.status(404).result("Archivo no encontrado");
                return;
            }
            
            // Configurar headers para descarga
            ctx.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            ctx.header("Content-Type", getContentType(fileName));
            
            // Enviar archivo
            try (FileInputStream fis = new FileInputStream(file)) {
                ctx.result(fis);
            }
            
        } catch (Exception e) {
            ctx.status(500).result("Error descargando archivo");
        }
    }
    
    private String saveFile(UploadedFile file, String targetPath) {
        try {
            // Validar archivo
            if (!isValidFile(file)) {
                return null;
            }
            
            // Generar nombre único
            String originalName = file.filename();
            String extension = getFileExtension(originalName);
            String uniqueName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + extension;
            
            // Guardar archivo
            Path targetFilePath = Paths.get(targetPath, uniqueName);
            Files.copy(file.content(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);
            
            return uniqueName;
            
        } catch (IOException e) {
            System.err.println("Error guardando archivo: " + e.getMessage());
            return null;
        }
    }
    
    private boolean isValidFile(UploadedFile file) {
        // Validar tamaño
        if (file.size() > MAX_FILE_SIZE) {
            return false;
        }
        
        // Validar extensión
        String fileName = file.filename();
        if (fileName == null) {
            return false;
        }
        
        String extension = getFileExtension(fileName).toLowerCase();
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (extension.equals(allowed)) {
                return true;
            }
        }
        
        return false;
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot);
        }
        return "";
    }
    
    private String getContentType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case ".pdf":
                return "application/pdf";
            case ".doc":
                return "application/msword";
            case ".docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            default:
                return "application/octet-stream";
        }
    }
}