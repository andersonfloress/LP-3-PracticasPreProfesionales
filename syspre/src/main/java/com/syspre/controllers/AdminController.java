package com.syspre.controllers;

import io.javalin.http.Context;
import com.syspre.models.UserModel;
import com.syspre.models.PlanPracticaModel;
import com.syspre.models.ReporteSemanalModel;
import com.syspre.models.InformeFinalModel;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class AdminController {
    private final UserModel userModel;
    private final PlanPracticaModel planModel;
    private final ReporteSemanalModel reporteModel;
    private final InformeFinalModel informeModel;
    
    public AdminController() {
        this.userModel = new UserModel();
        this.planModel = new PlanPracticaModel();
        this.reporteModel = new ReporteSemanalModel();
        this.informeModel = new InformeFinalModel();
    }
    
    // GET /admin/usuarios - Gestión de usuarios
    public void gestionarUsuarios(Context ctx) {
        try {
            if (!isAdmin(ctx)) {
                ctx.redirect("/login/admin");
                return;
            }
            
            Map<String, Object> model = new HashMap<>();
            model.put("user", getUserFromSession(ctx));
            
            // Obtener todos los usuarios (implementación básica)
            List<Map<String, Object>> usuarios = new ArrayList<>();
            model.put("usuarios", usuarios);
            
            // Estadísticas por tipo (implementación básica)
            int totalEstudiantes = 0;
            int totalDocentes = 0;
            int totalCoordinadores = 0;
            int totalAdmins = 0;
            
            model.put("totalEstudiantes", totalEstudiantes);
            model.put("totalDocentes", totalDocentes);
            model.put("totalCoordinadores", totalCoordinadores);
            model.put("totalAdmins", totalAdmins);
            
            // Filtros aplicados
            String tipoFiltro = ctx.queryParam("tipo");
            String estadoFiltro = ctx.queryParam("estado");
            
            if (tipoFiltro != null && !tipoFiltro.isEmpty()) {
                usuarios = usuarios.stream()
                    .filter(u -> tipoFiltro.equals(u.get("tipo_usuario")))
                    .collect(java.util.stream.Collectors.toList());
                model.put("tipoFiltro", tipoFiltro);
            }
            
            if (estadoFiltro != null && !estadoFiltro.isEmpty()) {
                usuarios = usuarios.stream()
                    .filter(u -> estadoFiltro.equals(u.get("estado")))
                    .collect(java.util.stream.Collectors.toList());
                model.put("estadoFiltro", estadoFiltro);
            }
            
            model.put("usuariosFiltrados", usuarios);
            
            ctx.render("admin_gestionar_usuarios.html", model);
            
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.result("Error al cargar usuarios: " + e.getMessage());
        }
    }
    
    // POST /admin/usuario/{id}/cambiar-estado - Cambiar estado de usuario
    public void cambiarEstadoUsuario(Context ctx) {
        try {
            if (!isAdmin(ctx)) {
                ctx.redirect("/login/admin");
                return;
            }
            
            int usuarioId = Integer.parseInt(ctx.pathParam("id"));
            String nuevoEstado = ctx.formParam("estado");
            
            if (nuevoEstado == null || (!nuevoEstado.equals("activo") && !nuevoEstado.equals("inactivo"))) {
                ctx.status(400);
                ctx.result("Estado inválido");
                return;
            }
            
            boolean exito = true; // userModel.cambiarEstadoUsuario(usuarioId, nuevoEstado);
            
            if (exito) {
                ctx.redirect("/admin/usuarios?success=estado_cambiado");
            } else {
                ctx.status(500);
                ctx.result("Error al cambiar estado");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.result("Error al cambiar estado: " + e.getMessage());
        }
    }
    
    // GET /admin/registro-personal - Formulario de registro de personal
    public void mostrarRegistroPersonal(Context ctx) {
        try {
            if (!isAdmin(ctx)) {
                ctx.redirect("/login/admin");
                return;
            }
            
            Map<String, Object> model = new HashMap<>();
            model.put("user", getUserFromSession(ctx));
            
            ctx.render("admin_registro_personal.html", model);
            
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.result("Error al cargar formulario: " + e.getMessage());
        }
    }
    
    // POST /admin/registro-personal - Registrar personal
    public void registrarPersonal(Context ctx) {
        try {
            if (!isAdmin(ctx)) {
                ctx.redirect("/login/admin");
                return;
            }
            
            // Obtener datos del formulario
            String tipoUsuario = ctx.formParam("tipo_usuario");
            String nombres = ctx.formParam("nombres");
            String apellidos = ctx.formParam("apellidos");
            String email = ctx.formParam("email");
            String dni = ctx.formParam("dni");
            String telefono = ctx.formParam("telefono");
            String especialidad = ctx.formParam("especialidad");
            String password = ctx.formParam("password");
            String confirmPassword = ctx.formParam("confirm_password");
            
            // Validaciones
            if (tipoUsuario == null || nombres == null || apellidos == null || 
                email == null || dni == null || password == null) {
                ctx.status(400);
                ctx.result("Todos los campos obligatorios deben ser completados");
                return;
            }
            
            if (!tipoUsuario.equals("docente") && !tipoUsuario.equals("coordinador") && !tipoUsuario.equals("admin")) {
                ctx.status(400);
                ctx.result("Tipo de usuario inválido");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                ctx.status(400);
                ctx.result("Las contraseñas no coinciden");
                return;
            }
            
            // Verificar que el email no exista (implementación básica)
            // if (userModel.existeEmail(email)) {
            //     ctx.status(400);
            //     ctx.result("El email ya está registrado");
            //     return;
            // }
            
            // Verificar que el DNI no exista (implementación básica)
            // if (userModel.existeDni(dni)) {
            //     ctx.status(400);
            //     ctx.result("El DNI ya está registrado");
            //     return;
            // }
            
            // Crear usuario
            Map<String, Object> nuevoUsuario = new HashMap<>();
            nuevoUsuario.put("tipo_usuario", tipoUsuario);
            nuevoUsuario.put("nombres", nombres);
            nuevoUsuario.put("apellidos", apellidos);
            nuevoUsuario.put("email", email);
            nuevoUsuario.put("dni", dni);
            nuevoUsuario.put("telefono", telefono);
            nuevoUsuario.put("especialidad", especialidad);
            nuevoUsuario.put("password", password);
            nuevoUsuario.put("estado", "activo");
            
            boolean exito = true; // userModel.crearUsuario(nuevoUsuario);
            
            if (exito) {
                ctx.redirect("/admin/usuarios?success=personal_registrado");
            } else {
                ctx.status(500);
                ctx.result("Error al registrar personal");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.result("Error al registrar personal: " + e.getMessage());
        }
    }
    
    // GET /admin/reportes - Estadísticas y reportes globales
    public void mostrarReportes(Context ctx) {
        try {
            if (!isAdmin(ctx)) {
                ctx.redirect("/login/admin");
                return;
            }
            
            Map<String, Object> model = new HashMap<>();
            model.put("user", getUserFromSession(ctx));
            
            // Estadísticas generales (implementación básica)
            int totalUsuarios = 0; // userModel.contarUsuarios();
            int totalPlanes = 0; // planModel.contarPlanes();
            int totalReportes = 0; // reporteModel.contarReportes();
            int totalInformes = 0; // informeModel.contarInformes();
            
            model.put("totalUsuarios", totalUsuarios);
            model.put("totalPlanes", totalPlanes);
            model.put("totalReportes", totalReportes);
            model.put("totalInformes", totalInformes);
            
            // Estadísticas por estado (implementación básica)
            Map<String, Integer> planesPorEstado = new HashMap<>();
            Map<String, Integer> reportesPorEstado = new HashMap<>();
            Map<String, Integer> informesPorEstado = new HashMap<>();
            
            model.put("planesPorEstado", planesPorEstado);
            model.put("reportesPorEstado", reportesPorEstado);
            model.put("informesPorEstado", informesPorEstado);
            
            // Estadísticas por mes
            List<Map<String, Object>> estadisticasMensuales = obtenerEstadisticasMensuales();
            model.put("estadisticasMensuales", estadisticasMensuales);
            
            // Top estudiantes por calificaciones (implementación básica)
            List<Map<String, Object>> topEstudiantes = new ArrayList<>();
            model.put("topEstudiantes", topEstudiantes);
            
            // Empresas más utilizadas (implementación básica)
            List<Map<String, Object>> topEmpresas = new ArrayList<>();
            model.put("topEmpresas", topEmpresas);
            
            ctx.render("admin_reportes.html", model);
            
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.result("Error al cargar reportes: " + e.getMessage());
        }
    }
    
    // GET /admin/usuario/{id}/ver - Ver detalles de usuario
    public void verUsuario(Context ctx) {
        try {
            if (!isAdmin(ctx)) {
                ctx.redirect("/login/admin");
                return;
            }
            
            int usuarioId = Integer.parseInt(ctx.pathParam("id"));
            
            Map<String, Object> model = new HashMap<>();
            model.put("user", getUserFromSession(ctx));
            
            // Obtener usuario (implementación básica)
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("id", usuarioId);
            usuario.put("nombres", "Usuario");
            usuario.put("apellidos", "Ejemplo");
            usuario.put("tipo_usuario", "estudiante");
            
            model.put("usuario", usuario);
            
            // Obtener actividad según el tipo de usuario
            String tipoUsuario = (String) usuario.get("tipo_usuario");
            
            if ("estudiante".equals(tipoUsuario)) {
                // Obtener planes, reportes e informes del estudiante (implementación básica)
                List<Map<String, Object>> planes = new ArrayList<>();
                List<Map<String, Object>> reportes = new ArrayList<>();
                List<Map<String, Object>> informes = new ArrayList<>();
                
                model.put("planes", planes);
                model.put("reportes", reportes);
                model.put("informes", informes);
            } else if ("docente".equals(tipoUsuario)) {
                // Obtener asignaciones y calificaciones del docente (implementación básica)
                List<Map<String, Object>> asignaciones = new ArrayList<>();
                List<Map<String, Object>> calificaciones = new ArrayList<>();
                
                model.put("asignaciones", asignaciones);
                model.put("calificaciones", calificaciones);
            }
            
            ctx.render("admin_ver_usuario.html", model);
            
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.result("Error al cargar usuario: " + e.getMessage());
        }
    }
    
    // Métodos auxiliares
    private boolean isAdmin(Context ctx) {
        Map<String, Object> user = getUserFromSession(ctx);
        return user != null && "admin".equals(user.get("tipo_usuario"));
    }
    
    private Map<String, Object> getUserFromSession(Context ctx) {
        return ctx.sessionAttribute("user");
    }
    
    private List<Map<String, Object>> obtenerEstadisticasMensuales() {
        // Implementar estadísticas mensuales
        List<Map<String, Object>> estadisticas = new java.util.ArrayList<>();
        
        // Por ahora retornar lista vacía, se puede implementar después
        return estadisticas;
    }
}