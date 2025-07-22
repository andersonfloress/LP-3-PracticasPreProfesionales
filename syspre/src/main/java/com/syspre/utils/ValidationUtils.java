package com.syspre.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ValidationUtils {
    
    // Patrones de validación
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^\\+?[1-9]\\d{8,14}$");
    private static final Pattern CODIGO_ESTUDIANTE_PATTERN = 
        Pattern.compile("^\\d{10}$"); // 10 dígitos para código de estudiante
    private static final Pattern DNI_PATTERN = 
        Pattern.compile("^\\d{8}$");
    
    /**
     * Valida un email
     */
    public static ValidationResult validateEmail(String email) {
        ValidationResult result = new ValidationResult();
        
        if (email == null || email.trim().isEmpty()) {
            result.addError("El email es requerido");
            return result;
        }
        
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            result.addError("Formato de email inválido");
        }
        
        return result;
    }
    
    /**
     * Valida un teléfono
     */
    public static ValidationResult validatePhone(String phone) {
        ValidationResult result = new ValidationResult();
        
        if (phone != null && !phone.trim().isEmpty()) {
            String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");
            if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
                result.addError("Formato de teléfono inválido");
            }
        }
        
        return result;
    }
    
    /**
     * Valida código de estudiante
     */
    public static ValidationResult validateCodigoEstudiante(String codigo) {
        ValidationResult result = new ValidationResult();
        
        if (codigo == null || codigo.trim().isEmpty()) {
            result.addError("El código de estudiante es requerido");
            return result;
        }
        
        if (!CODIGO_ESTUDIANTE_PATTERN.matcher(codigo.trim()).matches()) {
            result.addError("El código debe tener exactamente 10 dígitos");
        }
        
        return result;
    }
    
    /**
     * Valida DNI
     */
    public static ValidationResult validateDni(String dni) {
        ValidationResult result = new ValidationResult();
        
        if (dni == null || dni.trim().isEmpty()) {
            result.addError("El DNI es requerido");
            return result;
        }
        
        if (!DNI_PATTERN.matcher(dni.trim()).matches()) {
            result.addError("El DNI debe tener exactamente 8 dígitos");
        }
        
        return result;
    }
    
    /**
     * Valida nombre completo
     */
    public static ValidationResult validateNombreCompleto(String nombre) {
        ValidationResult result = new ValidationResult();
        
        if (nombre == null || nombre.trim().isEmpty()) {
            result.addError("El nombre es requerido");
            return result;
        }
        
        if (nombre.trim().length() < 2) {
            result.addError("El nombre debe tener al menos 2 caracteres");
        }
        
        if (nombre.trim().length() > 100) {
            result.addError("El nombre no puede exceder 100 caracteres");
        }
        
        // Validar que solo contenga letras, espacios, acentos y apostrofes
        if (!nombre.matches("^[a-zA-ZÀ-ÿ\\s']+$")) {
            result.addError("El nombre solo puede contener letras, espacios y acentos");
        }
        
        return result;
    }
    
    /**
     * Valida contraseña
     */
    public static ValidationResult validatePassword(String password) {
        ValidationResult result = new ValidationResult();
        
        if (password == null || password.isEmpty()) {
            result.addError("La contraseña es requerida");
            return result;
        }
        
        if (password.length() < 6) {
            result.addError("La contraseña debe tener al menos 6 caracteres");
        }
        
        if (password.length() > 50) {
            result.addError("La contraseña no puede exceder 50 caracteres");
        }
        
        return result;
    }
    
    /**
     * Valida que las contraseñas coincidan
     */
    public static ValidationResult validatePasswordConfirmation(String password, String confirmPassword) {
        ValidationResult result = new ValidationResult();
        
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            result.addError("La confirmación de contraseña es requerida");
            return result;
        }
        
        if (!password.equals(confirmPassword)) {
            result.addError("Las contraseñas no coinciden");
        }
        
        return result;
    }
    
    /**
     * Valida especialidad
     */
    public static ValidationResult validateEspecialidad(String especialidad) {
        ValidationResult result = new ValidationResult();
        
        if (especialidad == null || especialidad.trim().isEmpty()) {
            result.addError("La especialidad es requerida");
            return result;
        }
        
        List<String> especialidadesValidas = List.of(
            "Ingeniería de Sistemas",
            "Ingeniería Civil", 
            "Ingeniería Industrial",
            "Ingeniería Electrónica",
            "Ingeniería Mecánica",
            "Ingeniería Química",
            "Arquitectura",
            "Contabilidad",
            "Administración"
        );
        
        if (!especialidadesValidas.contains(especialidad.trim())) {
            result.addError("Especialidad no válida");
        }
        
        return result;
    }
    
    /**
     * Valida tipo de usuario
     */
    public static ValidationResult validateTipoUsuario(String tipoUsuario) {
        ValidationResult result = new ValidationResult();
        
        if (tipoUsuario == null || tipoUsuario.trim().isEmpty()) {
            result.addError("El tipo de usuario es requerido");
            return result;
        }
        
        List<String> tiposValidos = List.of("estudiante", "docente", "coordinador", "admin");
        
        if (!tiposValidos.contains(tipoUsuario.trim())) {
            result.addError("Tipo de usuario no válido");
        }
        
        return result;
    }
    
    /**
     * Valida archivo subido
     */
    public static ValidationResult validateFile(String fileName, long fileSize, List<String> allowedExtensions, long maxSize) {
        ValidationResult result = new ValidationResult();
        
        if (fileName == null || fileName.trim().isEmpty()) {
            result.addError("Debe seleccionar un archivo");
            return result;
        }
        
        // Validar extensión
        String extension = getFileExtension(fileName).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            result.addError("Tipo de archivo no permitido. Extensiones permitidas: " + String.join(", ", allowedExtensions));
        }
        
        // Validar tamaño
        if (fileSize > maxSize) {
            result.addError("El archivo excede el tamaño máximo permitido (" + formatFileSize(maxSize) + ")");
        }
        
        if (fileSize == 0) {
            result.addError("El archivo está vacío");
        }
        
        return result;
    }
    
    /**
     * Obtiene la extensión de un archivo
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        
        return fileName.substring(lastDot + 1);
    }
    
    /**
     * Formatea el tamaño de archivo
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
    
    /**
     * Combina múltiples resultados de validación
     */
    public static ValidationResult combineValidations(ValidationResult... results) {
        ValidationResult combined = new ValidationResult();
        
        for (ValidationResult result : results) {
            combined.addErrors(result.getErrors());
        }
        
        return combined;
    }
    
    /**
     * Clase para encapsular resultados de validación
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addErrors(List<String> errors) {
            this.errors.addAll(errors);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public String getFirstError() {
            return errors.isEmpty() ? null : errors.get(0);
        }
        
        public String getAllErrors() {
            return String.join("; ", errors);
        }
    }
}