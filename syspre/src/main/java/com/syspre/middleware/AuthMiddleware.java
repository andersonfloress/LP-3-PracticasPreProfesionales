package com.syspre.middleware;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.Set;

public class AuthMiddleware {
    
    // Rutas públicas que no requieren autenticación
    private static final Set<String> PUBLIC_ROUTES = Set.of(
        "/", "/login/admin", "/login/coordinador", "/login/docente", "/login/estudiante",
        "/registro/estudiante", "/registro/docente-coordinador", "/logout"
    );
    
    // Rutas de archivos estáticos
    private static final Set<String> STATIC_ROUTES = Set.of(
        "/css", "/js", "/images", "/static", "/favicon.ico"
    );
    
    public static Handler requireAuth = ctx -> {
        String userId = ctx.sessionAttribute("userId");
        String userType = ctx.sessionAttribute("userType");
        
        if (userId == null || userType == null) {
            // Redirigir a página de selección de roles
            ctx.redirect("/");
            return;
        }
    };
    
    public static Handler requireEstudiante = ctx -> {
        String userType = ctx.sessionAttribute("userType");
        
        if (!"estudiante".equals(userType)) {
            ctx.status(403).html("Acceso denegado. Se requiere rol de estudiante.");
            return;
        }
    };
    
    public static Handler requireDocente = ctx -> {
        String userType = ctx.sessionAttribute("userType");
        
        if (!"docente".equals(userType)) {
            ctx.status(403).html("Acceso denegado. Se requiere rol de docente.");
            return;
        }
    };
    
    public static Handler requireCoordinador = ctx -> {
        String userType = ctx.sessionAttribute("userType");
        
        if (!"coordinador".equals(userType)) {
            ctx.status(403).html("Acceso denegado. Se requiere rol de coordinador.");
            return;
        }
    };
    
    public static Handler requireAdmin = ctx -> {
        String userType = ctx.sessionAttribute("userType");
        
        if (!"admin".equals(userType)) {
            ctx.status(403).html("Acceso denegado. Se requiere rol de administrador.");
            return;
        }
    };
    
    public static Handler requireDocenteOrCoordinador = ctx -> {
        String userType = ctx.sessionAttribute("userType");
        
        if (!"docente".equals(userType) && !"coordinador".equals(userType)) {
            ctx.status(403).html("Acceso denegado. Se requiere rol de docente o coordinador.");
            return;
        }
    };
    
    public static Handler requireCoordinadorOrAdmin = ctx -> {
        String userType = ctx.sessionAttribute("userType");
        
        if (!"coordinador".equals(userType) && !"admin".equals(userType)) {
            ctx.status(403).html("Acceso denegado. Se requiere rol de coordinador o administrador.");
            return;
        }
    };
    
    // Métodos auxiliares de validación
    private static boolean isPublicRoute(String path) {
        return PUBLIC_ROUTES.contains(path) || path.startsWith("/css") || path.startsWith("/js") || 
               path.startsWith("/images") || path.startsWith("/static") || path.endsWith(".ico");
    }
    
    private static boolean isStaticRoute(String path) {
        return STATIC_ROUTES.stream().anyMatch(route -> path.startsWith(route));
    }
    
    private static boolean isValidUserType(String userType) {
        return userType != null && Set.of("estudiante", "docente", "coordinador", "admin").contains(userType);
    }
    
    private static void clearSession(Context ctx) {
        ctx.sessionAttribute("userId", null);
        ctx.sessionAttribute("userType", null);
        ctx.sessionAttribute("userName", null);
    }
    
    private static void handleUnauthorized(Context ctx) {
        if (ctx.path().startsWith("/api/")) {
            ctx.status(401).json("{\"error\": \"No autorizado\"}");
        } else {
            ctx.redirect("/");
        }
    }
    
    public static Handler logRequest = ctx -> {
        String method = ctx.method().toString();
        String path = ctx.path();
        String userType = ctx.sessionAttribute("userType");
        String userId = ctx.sessionAttribute("userId");
        
        System.out.println("[" + method + "] " + path + " - User: " + userId + " (" + userType + ")");
    };
}