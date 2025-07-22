package com.syspre.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;

public class IndexController {
    
    public static void setupRoutes(Javalin app) {
        app.get("/", IndexController::index);
        app.get("/index", IndexController::index);
    }
    
    public static void index(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "SYSPRE 2025 - Sistema de Gestión de Prácticas Pre-Profesionales");
        ctx.render("index.html", model);
    }
}