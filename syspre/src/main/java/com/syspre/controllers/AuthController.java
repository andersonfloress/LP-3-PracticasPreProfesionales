package com.syspre.controllers;

import com.syspre.models.UserModel;
import com.syspre.utils.FormValidator;
import com.syspre.utils.ValidationUtils.ValidationResult;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

public class AuthController {
    private final UserModel userModel = new UserModel();
    
    // Login Admin
    public void loginAdminPage(Context ctx) {
        ctx.render("login_admin");
    }
    
    public void loginAdmin(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        
        Map<String, Object> userData = userModel.authenticate(email, password);
        
        if (userData != null && "admin".equals(userData.get("tipo"))) {
            // Establecer sesión
            setUserSession(ctx, userData);
            ctx.redirect("/admin/dashboard");
        } else {
            Map<String, Object> model = new HashMap<>();
            model.put("error", "Credenciales inválidas o no tiene permisos de administrador");
            ctx.render("login_admin", model);
        }
    }
    
    // Login Coordinador
    public void loginCoordinadorPage(Context ctx) {
        ctx.render("login_coordinador");
    }
    
    public void loginCoordinador(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        
        Map<String, Object> userData = userModel.authenticate(email, password);
        
        if (userData != null && "coordinador".equals(userData.get("tipo"))) {
            setUserSession(ctx, userData);
            ctx.redirect("/coordinador/dashboard");
        } else {
            Map<String, Object> model = new HashMap<>();
            model.put("error", "Credenciales inválidas o no tiene permisos de coordinador");
            ctx.render("login_coordinador", model);
        }
    }
    
    // Login Docente
    public void loginDocentePage(Context ctx) {
        ctx.render("login_docente");
    }
    
    public void loginDocente(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        
        Map<String, Object> userData = userModel.authenticate(email, password);
        
        if (userData != null && "docente".equals(userData.get("tipo"))) {
            setUserSession(ctx, userData);
            ctx.redirect("/docente/dashboard");
        } else {
            Map<String, Object> model = new HashMap<>();
            model.put("error", "Credenciales inválidas o no tiene permisos de docente");
            ctx.render("login_docente", model);
        }
    }
    
    // Login Estudiante
    public void loginEstudiantePage(Context ctx) {
        String success = ctx.queryParam("success");
        Map<String, Object> model = new HashMap<>();
        if ("1".equals(success)) {
            model.put("success", "Registro exitoso. Ahora puedes iniciar sesión.");
        }
        ctx.render("login_estudiante", model);
    }
    
    public void loginEstudiante(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        
        Map<String, Object> userData = userModel.authenticate(email, password);
        
        if (userData != null && "estudiante".equals(userData.get("tipo"))) {
            setUserSession(ctx, userData);
            ctx.redirect("/estudiante/dashboard");
        } else {
            Map<String, Object> model = new HashMap<>();
            model.put("error", "Credenciales inválidas o no tiene permisos de estudiante");
            ctx.render("login_estudiante", model);
        }
    }
    
    // Registro Estudiante
    public void registroEstudiantePage(Context ctx) {
        ctx.render("registro_estudiante");
    }
    
    public void registroEstudiante(Context ctx) {
        Map<String, Object> data = new HashMap<>();
        data.put("codigo", ctx.formParam("codigo_estudiante"));
        data.put("nombres", ctx.formParam("nombres"));
        data.put("apellidos", ctx.formParam("apellidos"));
        data.put("dni", ctx.formParam("dni"));
        data.put("telefono", ctx.formParam("telefono"));
        data.put("direccion", ctx.formParam("direccion"));
        data.put("email", ctx.formParam("correo"));
        data.put("password", ctx.formParam("password"));
        data.put("especialidad", ctx.formParam("escuela_profesional"));
        data.put("semestre", Integer.parseInt(ctx.formParam("semestre")));
        data.put("ciclo_academico", ctx.formParam("ciclo_academico"));
        data.put("tipo", "estudiante");
        
        String confirmPassword = ctx.formParam("confirm_password");
        
        if (!ctx.formParam("password").equals(confirmPassword)) {
            Map<String, Object> model = new HashMap<>();
            model.put("error", "Las contraseñas no coinciden");
            ctx.render("registro_estudiante", model);
            return;
        }
        
        if (userModel.create(data)) {
            ctx.redirect("/login/estudiante?success=1");
        } else {
            Map<String, Object> model = new HashMap<>();
            model.put("error", "Error al registrar el estudiante");
            ctx.render("registro_estudiante", model);
        }
    }
    
    // Registro Docente/Coordinador
    public void registroDocenteCoordinadorPage(Context ctx) {
        ctx.render("registro_docente_coordinador");
    }
    
    public void registroDocenteCoordinador(Context ctx) {
        Map<String, Object> data = new HashMap<>();
        data.put("codigo", ctx.formParam("codigo"));
        data.put("nombres", ctx.formParam("nombres"));
        data.put("apellidos", ctx.formParam("apellidos"));
        data.put("dni", ctx.formParam("dni"));
        data.put("telefono", ctx.formParam("telefono"));
        data.put("direccion", ctx.formParam("direccion"));
        data.put("email", ctx.formParam("email"));
        data.put("password", ctx.formParam("password"));
        data.put("especialidad", ctx.formParam("especialidad"));
        data.put("tipo", ctx.formParam("tipo"));
        
        String confirmPassword = ctx.formParam("confirm_password");
        
        if (!ctx.formParam("password").equals(confirmPassword)) {
            Map<String, Object> model = new HashMap<>();
            model.put("error", "Las contraseñas no coinciden");
            ctx.render("registro_docente_coordinador", model);
            return;
        }
        
        if (userModel.create(data)) {
            ctx.redirect("/dashboard/admin?success=Usuario registrado exitosamente");
        } else {
            Map<String, Object> model = new HashMap<>();
            model.put("error", "Error al registrar el usuario");
            ctx.render("registro_docente_coordinador", model);
        }
    }
    
    // Logout
    public void logout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }
    
    private void setUserSession(Context ctx, Map<String, Object> userData) {
        // Variables de sesión para AuthMiddleware
        ctx.sessionAttribute("userId", userData.get("id"));
        ctx.sessionAttribute("userType", userData.get("tipo"));
        
        // Variables de sesión para controladores específicos
        ctx.sessionAttribute("user_id", userData.get("id"));
        ctx.sessionAttribute("user_email", userData.get("email"));
        ctx.sessionAttribute("user_nombres", userData.get("nombres"));
        ctx.sessionAttribute("user_apellidos", userData.get("apellidos"));
        ctx.sessionAttribute("user_codigo", userData.get("codigo"));
        ctx.sessionAttribute("user_type", userData.get("tipo"));
        ctx.sessionAttribute("user_especialidad", userData.get("especialidad"));
        
        // Objeto completo para controladores como DocumentosEspecialidadController
        ctx.sessionAttribute("user", userData);
    }
}