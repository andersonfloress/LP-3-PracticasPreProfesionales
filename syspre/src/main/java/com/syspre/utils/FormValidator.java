package com.syspre.utils;

import com.syspre.utils.ValidationUtils.ValidationResult;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormValidator {
    
    /**
     * Valida formulario de registro de estudiante
     */
    public static ValidationResult validateEstudianteRegistration(Context ctx) {
        ValidationResult result = new ValidationResult();
        
        String codigo = ctx.formParam("codigo");
        String nombres = ctx.formParam("nombres");
        String apellidos = ctx.formParam("apellidos");
        String email = ctx.formParam("email");
        String telefono = ctx.formParam("telefono");
        String dni = ctx.formParam("dni");
        String especialidad = ctx.formParam("especialidad");
        String password = ctx.formParam("password");
        String confirmPassword = ctx.formParam("confirm_password");
        
        // Validar campos individuales
        result = ValidationUtils.combineValidations(
            result,
            ValidationUtils.validateCodigoEstudiante(codigo),
            ValidationUtils.validateNombreCompleto(nombres),
            ValidationUtils.validateNombreCompleto(apellidos),
            ValidationUtils.validateEmail(email),
            ValidationUtils.validatePhone(telefono),
            ValidationUtils.validateDni(dni),
            ValidationUtils.validateEspecialidad(especialidad),
            ValidationUtils.validatePassword(password),
            ValidationUtils.validatePasswordConfirmation(password, confirmPassword)
        );
        
        return result;
    }
    
    /**
     * Valida formulario de registro de personal (docente/coordinador)
     */
    public static ValidationResult validatePersonalRegistration(Context ctx) {
        ValidationResult result = new ValidationResult();
        
        String nombres = ctx.formParam("nombres");
        String apellidos = ctx.formParam("apellidos");
        String email = ctx.formParam("email");
        String telefono = ctx.formParam("telefono");
        String dni = ctx.formParam("dni");
        String especialidad = ctx.formParam("especialidad");
        String tipoUsuario = ctx.formParam("tipo_usuario");
        String password = ctx.formParam("password");
        String confirmPassword = ctx.formParam("confirm_password");
        
        // Validar campos individuales
        result = ValidationUtils.combineValidations(
            result,
            ValidationUtils.validateNombreCompleto(nombres),
            ValidationUtils.validateNombreCompleto(apellidos),
            ValidationUtils.validateEmail(email),
            ValidationUtils.validatePhone(telefono),
            ValidationUtils.validateDni(dni),
            ValidationUtils.validateEspecialidad(especialidad),
            validateTipoUsuarioPersonal(tipoUsuario),
            ValidationUtils.validatePassword(password),
            ValidationUtils.validatePasswordConfirmation(password, confirmPassword)
        );
        
        return result;
    }
    
    /**
     * Valida formulario de login
     */
    public static ValidationResult validateLogin(Context ctx) {
        ValidationResult result = new ValidationResult();
        
        String identifier = ctx.formParam("identifier"); // puede ser email o código
        String password = ctx.formParam("password");
        
        if (identifier == null || identifier.trim().isEmpty()) {
            result.addError("El email o código es requerido");
        }
        
        if (password == null || password.trim().isEmpty()) {
            result.addError("La contraseña es requerida");
        }
        
        return result;
    }
    
    /**
     * Valida formulario de plan de prácticas
     */
    public static ValidationResult validatePlanPractica(Context ctx) {
        ValidationResult result = new ValidationResult();
        
        String empresaNombre = ctx.formParam("empresa_nombre");
        String empresaRuc = ctx.formParam("empresa_ruc");
        String empresaDireccion = ctx.formParam("empresa_direccion");
        String supervisorNombre = ctx.formParam("supervisor_nombre");
        String supervisorCargo = ctx.formParam("supervisor_cargo");
        String supervisorEmail = ctx.formParam("supervisor_email");
        String descripcionActividades = ctx.formParam("descripcion_actividades");
        String objetivos = ctx.formParam("objetivos");
        String fechaInicio = ctx.formParam("fecha_inicio");
        String fechaFin = ctx.formParam("fecha_fin");
        
        // Validar campos requeridos
        if (empresaNombre == null || empresaNombre.trim().isEmpty()) {
            result.addError("El nombre de la empresa es requerido");
        }
        if (empresaRuc == null || empresaRuc.trim().isEmpty()) {
            result.addError("El RUC de la empresa es requerido");
        }
        if (empresaDireccion == null || empresaDireccion.trim().isEmpty()) {
            result.addError("La dirección de la empresa es requerida");
        }
        if (supervisorNombre == null || supervisorNombre.trim().isEmpty()) {
            result.addError("El nombre del supervisor es requerido");
        }
        if (supervisorCargo == null || supervisorCargo.trim().isEmpty()) {
            result.addError("El cargo del supervisor es requerido");
        }
        if (descripcionActividades == null || descripcionActividades.trim().isEmpty()) {
            result.addError("La descripción de actividades es requerida");
        }
        if (objetivos == null || objetivos.trim().isEmpty()) {
            result.addError("Los objetivos son requeridos");
        }
        if (fechaInicio == null || fechaInicio.trim().isEmpty()) {
            result.addError("La fecha de inicio es requerida");
        }
        if (fechaFin == null || fechaFin.trim().isEmpty()) {
            result.addError("La fecha de fin es requerida");
        }
        
        // Validar email del supervisor
        if (supervisorEmail != null && !supervisorEmail.trim().isEmpty()) {
            ValidationResult emailResult = ValidationUtils.validateEmail(supervisorEmail);
            result.addErrors(emailResult.getErrors());
        }
        
        // Validar RUC (11 dígitos)
        if (empresaRuc != null && !empresaRuc.trim().matches("^\\d{11}$")) {
            result.addError("El RUC debe tener exactamente 11 dígitos");
        }
        
        return result;
    }
    
    /**
     * Valida formulario de reporte semanal
     */
    public static ValidationResult validateReporteSemanal(Context ctx) {
        ValidationResult result = new ValidationResult();
        
        String semana = ctx.formParam("semana");
        String fechaInicio = ctx.formParam("fecha_inicio");
        String fechaFin = ctx.formParam("fecha_fin");
        String actividades = ctx.formParam("actividades_realizadas");
        String logros = ctx.formParam("logros_obtenidos");
        String dificultades = ctx.formParam("dificultades_encontradas");
        String observaciones = ctx.formParam("observaciones");
        
        // Validar campos requeridos
        if (semana == null || semana.trim().isEmpty()) {
            result.addError("El número de semana es requerido");
        }
        if (fechaInicio == null || fechaInicio.trim().isEmpty()) {
            result.addError("La fecha de inicio es requerida");
        }
        if (fechaFin == null || fechaFin.trim().isEmpty()) {
            result.addError("La fecha de fin es requerida");
        }
        if (actividades == null || actividades.trim().isEmpty()) {
            result.addError("Las actividades realizadas son requeridas");
        }
        if (logros == null || logros.trim().isEmpty()) {
            result.addError("Los logros obtenidos son requeridos");
        }
        
        // Validar que la semana sea un número positivo
        try {
            int semanaNum = Integer.parseInt(semana);
            if (semanaNum <= 0 || semanaNum > 52) {
                result.addError("La semana debe estar entre 1 y 52");
            }
        } catch (NumberFormatException e) {
            result.addError("El número de semana debe ser válido");
        }
        
        return result;
    }
    
    /**
     * Valida archivo subido
     */
    public static ValidationResult validateUploadedFile(UploadedFile file, String fileType) {
        ValidationResult result = new ValidationResult();
        
        if (file == null) {
            result.addError("Debe seleccionar un archivo");
            return result;
        }
        
        List<String> allowedExtensions;
        long maxSize = 10 * 1024 * 1024; // 10MB por defecto
        
        switch (fileType.toLowerCase()) {
            case "plan":
            case "informe":
            case "documento":
                allowedExtensions = List.of("pdf", "doc", "docx");
                break;
            case "reporte":
                allowedExtensions = List.of("pdf", "doc", "docx");
                break;
            case "imagen":
                allowedExtensions = List.of("jpg", "jpeg", "png", "gif");
                maxSize = 5 * 1024 * 1024; // 5MB para imágenes
                break;
            default:
                allowedExtensions = List.of("pdf");
                break;
        }
        
        return ValidationUtils.validateFile(file.filename(), file.size(), allowedExtensions, maxSize);
    }
    
    /**
     * Crea un mapa con los errores de validación para ser usado en las plantillas
     */
    public static Map<String, Object> createErrorModel(ValidationResult validation) {
        Map<String, Object> model = new HashMap<>();
        model.put("hasErrors", !validation.isValid());
        model.put("errors", validation.getErrors());
        model.put("errorMessage", validation.getAllErrors());
        return model;
    }
    
    /**
     * Valida tipo de usuario para personal (no estudiantes)
     */
    private static ValidationResult validateTipoUsuarioPersonal(String tipoUsuario) {
        ValidationResult result = new ValidationResult();
        
        if (tipoUsuario == null || tipoUsuario.trim().isEmpty()) {
            result.addError("El tipo de usuario es requerido");
            return result;
        }
        
        List<String> tiposValidos = List.of("docente", "coordinador");
        
        if (!tiposValidos.contains(tipoUsuario.trim())) {
            result.addError("Tipo de usuario no válido");
        }
        
        return result;
    }
    
    /**
     * Extrae valores del formulario y los devuelve en un mapa
     */
    public static Map<String, String> extractFormData(Context ctx, String... fields) {
        Map<String, String> data = new HashMap<>();
        
        for (String field : fields) {
            String value = ctx.formParam(field);
            data.put(field, value != null ? value.trim() : "");
        }
        
        return data;
    }
}