package com.syspre.controllers;

import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

public class ProfileController {
    
    public void perfilCoordinador(Context ctx) {
        if (!isAuthenticated(ctx, "coordinador")) {
            ctx.redirect("/login/coordinador");
            return;
        }
        
        Map<String, Object> user = getUserFromSession(ctx);
        Map<String, Object> model = new HashMap<>();
        model.put("user", user);
        
        ctx.render("perfil_coordinador", model);
    }
    
    public void perfilDocente(Context ctx) {
        if (!isAuthenticated(ctx, "docente")) {
            ctx.redirect("/login/docente");
            return;
        }
        
        Map<String, Object> user = getUserFromSession(ctx);
        Map<String, Object> model = new HashMap<>();
        model.put("user", user);
        
        ctx.render("perfil_docente", model);
    }
    
    public void perfilEstudiante(Context ctx) {
        if (!isAuthenticated(ctx, "estudiante")) {
            ctx.redirect("/login/estudiante");
            return;
        }
        
        Map<String, Object> user = getUserFromSession(ctx);
        Map<String, Object> model = new HashMap<>();
        model.put("user", user);
        
        ctx.render("perfil_estudiante", model);
    }
    
    public void perfilAdmin(Context ctx) {
        if (!isAuthenticated(ctx, "admin")) {
            ctx.redirect("/login/admin");
            return;
        }
        
        Map<String, Object> user = getUserFromSession(ctx);
        Map<String, Object> model = new HashMap<>();
        model.put("user", user);
        
        ctx.render("perfil_admin", model);
    }
    
    private boolean isAuthenticated(Context ctx, String requiredType) {
        var session = ctx.req().getSession(false);
        if (session == null) return false;
        
        String userType = (String) session.getAttribute("user_type");
        return requiredType.equals(userType);
    }
    
    private Map<String, Object> getUserFromSession(Context ctx) {
        var session = ctx.req().getSession();
        return (Map<String, Object>) session.getAttribute("user_data");
    }
}