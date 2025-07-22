package com.syspre;

import com.syspre.config.DatabaseConfig;
import com.syspre.controllers.*;
import com.syspre.middleware.AuthMiddleware;
import com.syspre.models.UserModel;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Configurar Thymeleaf
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML");
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateEngine.setTemplateResolver(templateResolver);
        
        // Inicializar base de datos
        DatabaseConfig.initDatabase();
        
        // Crear tablas
        UserModel userModel = new UserModel();
        userModel.createTables();
        
        // Inicializar directorios de archivos
        FileController.initializeDirectories();
        
        // ANÁLISIS COMPLETO DE BASE DE DATOS
        analyzeDatabase();
        
        // DEBUG: Estructura de planes_practica
        com.syspre.utils.DatabaseDebugger.analyzeTableStructure();
        
        // CONSULTAR DATOS REALES DE USUARIOS
        com.syspre.utils.DatabaseQuery.testUserData();
        
        // Crear aplicación Javalin
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/static", Location.CLASSPATH);
            config.fileRenderer(new JavalinThymeleaf(templateEngine));
        });
        
        // Configurar rutas ANTES de iniciar el servidor
        setupRoutes(app);
        
        // Iniciar servidor  
        app.start("0.0.0.0", 5000);
        
        System.out.println("🚀 SYSPRE Java ejecutándose en http://localhost:5000");
    }
    
    private static void analyzeDatabase() {
        System.out.println("\n🔍 ANÁLISIS COMPLETO DE BASE DE DATOS SYSPRE");
        System.out.println("============================================");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // 1. Verificar tablas existentes
            showExistingTables(conn);
            
            // 2. Verificar estructura específica de planes_practica
            analyzeTableStructure(conn, "planes_practica");
            
            // 3. Verificar usuarios de prueba
            checkTestUsers(conn);
            
            // 4. Crear tablas adicionales necesarias
            createAdditionalTables(conn);
            
            // 5. Agregar columna fecha_inicio_plan si no existe
            addFechaInicioPlanColumn(conn);
            
        } catch (SQLException e) {
            System.err.println("❌ Error analizando base de datos: " + e.getMessage());
        }
    }
    
    private static void addFechaInicioPlanColumn(Connection conn) throws SQLException {
        System.out.println("\n🔧 AGREGANDO COLUMNA FECHA_INICIO_PLAN:");
        
        // Verificar si la columna ya existe
        String checkColumnSQL = """
            SELECT COUNT(*) as column_count 
            FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_NAME = 'planes_practica' 
            AND COLUMN_NAME = 'fecha_inicio_plan'
            AND TABLE_SCHEMA = 'bpuma_userMysql11'
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(checkColumnSQL)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt("column_count") == 0) {
                // Columna no existe, agregarla
                String addColumnSQL = """
                    ALTER TABLE planes_practica 
                    ADD COLUMN fecha_inicio_plan DATE NULL 
                    COMMENT 'Fecha programada para el inicio de la ejecución del plan de prácticas'
                """;
                
                try (PreparedStatement addStmt = conn.prepareStatement(addColumnSQL)) {
                    addStmt.executeUpdate();
                    System.out.println("  ✅ Columna fecha_inicio_plan agregada exitosamente");
                }
            } else {
                System.out.println("  ✅ Columna fecha_inicio_plan ya existe");
            }
        }
    }
    
    private static void showExistingTables(Connection conn) throws SQLException {
        System.out.println("\n📋 TABLAS EXISTENTES:");
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        
        int count = 0;
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            System.out.println("  ✅ " + tableName);
            count++;
        }
        System.out.println("Total de tablas: " + count);
    }
    
    private static void analyzeTableStructure(Connection conn, String tableName) throws SQLException {
        System.out.println("\n🔎 ESTRUCTURA DE TABLA: " + tableName.toUpperCase());
        
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, "%");
            
            boolean tableExists = false;
            while (columns.next()) {
                tableExists = true;
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");
                System.out.println("  - " + columnName + " (" + dataType + ")");
            }
            
            if (!tableExists) {
                System.out.println("  ⚠️ TABLA NO EXISTE");
            }
            
        } catch (SQLException e) {
            System.out.println("  ⚠️ ERROR: " + e.getMessage());
        }
    }
    
    private static void checkTestUsers(Connection conn) throws SQLException {
        System.out.println("\n👥 USUARIOS DE PRUEBA:");
        
        String sql = "SELECT email, tipo FROM usuarios WHERE email LIKE '%@syspre.com' OR email LIKE '%@test.com'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                System.out.println("  ✅ " + rs.getString("email") + " (" + rs.getString("tipo") + ")");
            }
        }
    }
    
    private static void createAdditionalTables(Connection conn) throws SQLException {
        System.out.println("\n🛠️ CREANDO TABLAS ADICIONALES:");
        
        // Tabla de evaluaciones detalladas
        String evaluacionesSQL = """
            CREATE TABLE IF NOT EXISTS evaluaciones (
                id INT AUTO_INCREMENT PRIMARY KEY,
                tipo ENUM('plan', 'reporte', 'informe', 'sustentacion') NOT NULL,
                referencia_id INT NOT NULL,
                evaluador_id INT NOT NULL,
                calificacion DECIMAL(4,2),
                comentarios TEXT,
                fecha_evaluacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_tipo_referencia (tipo, referencia_id),
                INDEX idx_evaluador (evaluador_id),
                FOREIGN KEY (evaluador_id) REFERENCES usuarios(id) ON DELETE CASCADE
            )
        """;
        
        // Tabla de configuración del sistema
        String configuracionSQL = """
            CREATE TABLE IF NOT EXISTS configuracion_sistema (
                id INT AUTO_INCREMENT PRIMARY KEY,
                clave VARCHAR(100) UNIQUE NOT NULL,
                valor TEXT,
                descripcion TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;
        
        // Tabla de notificaciones
        String notificacionesSQL = """
            CREATE TABLE IF NOT EXISTS notificaciones (
                id INT AUTO_INCREMENT PRIMARY KEY,
                usuario_id INT NOT NULL,
                titulo VARCHAR(200) NOT NULL,
                mensaje TEXT NOT NULL,
                tipo ENUM('info', 'warning', 'success', 'error') DEFAULT 'info',
                leida BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
            )
        """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(evaluacionesSQL);
            System.out.println("  ✅ Tabla evaluaciones creada");
            
            stmt.execute(configuracionSQL);
            System.out.println("  ✅ Tabla configuracion_sistema creada");
            
            stmt.execute(notificacionesSQL);
            System.out.println("  ✅ Tabla notificaciones creada");
            
            // Tabla para documentos de especialidad
            String documentosEspecialidadSQL = """
                CREATE TABLE IF NOT EXISTS documentos_especialidad (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    coordinador_id INT NOT NULL,
                    especialidad VARCHAR(100) NOT NULL,
                    titulo VARCHAR(200) NOT NULL,
                    descripcion TEXT,
                    archivo_path VARCHAR(500) NOT NULL,
                    archivo_nombre VARCHAR(200) NOT NULL,
                    archivo_size BIGINT,
                    tipo ENUM('principal', 'secundario') DEFAULT 'principal',
                    activo BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (coordinador_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                    INDEX idx_especialidad (especialidad),
                    INDEX idx_coordinador (coordinador_id),
                    INDEX idx_activo (activo)
                )
            """;
            stmt.execute(documentosEspecialidadSQL);
            System.out.println("  ✅ Tabla documentos_especialidad creada");
            
            // Insertar configuraciones por defecto
            String configInsert = """
                INSERT IGNORE INTO configuracion_sistema (clave, valor, descripcion) VALUES 
                ('min_reportes_semanales', '8', 'Número mínimo de reportes semanales'),
                ('horas_practica_minimas', '240', 'Horas mínimas de práctica'),
                ('calificacion_aprobatoria', '11.0', 'Calificación mínima para aprobar'),
                ('periodo_academico_actual', '2025-I', 'Periodo académico actual')
            """;
            stmt.execute(configInsert);
            System.out.println("  ✅ Configuraciones por defecto insertadas");
            
        } catch (SQLException e) {
            System.err.println("  ⚠️ Error creando tablas adicionales: " + e.getMessage());
        }
    }
    
    private static void setupRoutes(Javalin app) {
        IndexController indexController = new IndexController();
        AuthController authController = new AuthController();
        DashboardController dashboardController = new DashboardController();
        ProfileController profileController = new ProfileController();
        PlanPracticaController planController = new PlanPracticaController();
        ReporteController reporteController = new ReporteController();
        InformeController informeController = new InformeController();
        AsignacionDocenteController asignacionDocenteController = new AsignacionDocenteController();
        AsignacionController asignacionController = new AsignacionController();
        SustentacionController sustentacionController = new SustentacionController();
        AdminController adminController = new AdminController();
        FileController fileController = new FileController();
        DocumentoController documentoController = new DocumentoController();
        DocumentosEspecialidadController documentosEspecialidadController = new DocumentosEspecialidadController();
        
        // Ruta principal
        app.get("/", IndexController::index);
        
        // Rutas de autenticación
        app.get("/login/admin", authController::loginAdminPage);
        app.post("/login/admin", authController::loginAdmin);
        
        app.get("/login/coordinador", authController::loginCoordinadorPage);
        app.post("/login/coordinador", authController::loginCoordinador);
        
        app.get("/login/docente", authController::loginDocentePage);
        app.post("/login/docente", authController::loginDocente);
        
        app.get("/login/estudiante", authController::loginEstudiantePage);
        app.post("/login/estudiante", authController::loginEstudiante);
        
        // Rutas de registro
        app.get("/registro/estudiante", authController::registroEstudiantePage);
        app.post("/registro/estudiante", authController::registroEstudiante);
        
        app.get("/registro/docente-coordinador", authController::registroDocenteCoordinadorPage);
        app.post("/registro/docente-coordinador", authController::registroDocenteCoordinador);
        
        // Rutas de dashboards
        app.get("/admin/dashboard", dashboardController::adminDashboard);
        app.get("/coordinador/dashboard", dashboardController::coordinadorDashboard);
        app.get("/docente/dashboard", dashboardController::docenteDashboard);
        app.get("/estudiante/dashboard", dashboardController::estudianteDashboard);
        
        // Dashboard completo del docente con las 4 secciones
        app.get("/docente/dashboard-completo", dashboardController::docenteDashboardCompleto);
        
        // Rutas de perfiles
        app.get("/perfil/coordinador", profileController::perfilCoordinador);
        app.get("/perfil/docente", profileController::perfilDocente);
        app.get("/perfil/estudiante", profileController::perfilEstudiante);
        app.get("/perfil/admin", profileController::perfilAdmin);
        
        // === RUTAS DE PLANES DE PRÁCTICA ===
        // Estudiante
        app.get("/estudiante/plan/ver", planController::estudianteVerPlan);
        app.get("/estudiante/plan/crear", planController::estudianteRegistroPlanPage);
        app.post("/estudiante/plan/crear", planController::estudianteCrearPlan);
        
        // Docente - Planes asignados
        app.get("/docente/planes-asignados", asignacionController::docentePlanesAsignados);
        app.get("/docente/evaluar-plan/{planId}", asignacionController::evaluarPlan);
        app.post("/docente/evaluar-plan/{planId}", asignacionController::procesarEvaluacion);
        
        // Coordinador - Asignación de docentes (rutas corregidas)
        app.get("/coordinador/asignar-docente", asignacionController::asignarDocentePage);
        app.get("/coordinador/asignar-docente/{id}", asignacionController::asignarDocenteEstudiantePage);
        app.post("/coordinador/asignar-docente", asignacionController::procesarAsignacion);
        app.post("/coordinador/asignar-docente/{id}", asignacionController::procesarAsignacionEstudiante);
        
        // Coordinador - Revisión de planes
        app.get("/coordinador/planes", planController::coordinadorVerPlanesPendientes);
        app.get("/coordinador/plan/{id}/revisar", planController::coordinadorRevisarPlanPage);
        app.post("/coordinador/plan/{id}/revisar", planController::coordinadorRevisarPlan);
        
        // === RUTAS DE REPORTES SEMANALES ===
        // Estudiante
        app.get("/estudiante/reportes", reporteController::estudianteVerReportes);
        app.get("/estudiante/reporte/crear", reporteController::estudianteCrearReportePage);
        app.post("/estudiante/reporte/crear", reporteController::estudianteCrearReporte);
        app.get("/estudiante/reporte/{id}/editar", reporteController::estudianteEditarReportePage);
        app.post("/estudiante/reporte/{id}/editar", reporteController::estudianteActualizarReporte);
        
        // Docente
        app.get("/docente/reportes", reporteController::docenteVerReportes);
        app.get("/docente/reporte/{id}/calificar", reporteController::docenteCalificarReportePage);
        app.post("/docente/reporte/{id}/calificar", reporteController::docenteCalificarReporte);
        
        // Coordinador
        app.get("/coordinador/reporte/{id}/ver", reporteController::coordinadorVerReporte);
        
        // === RUTAS DE INFORMES FINALES ===
        // Estudiante
        app.get("/estudiante/informe/ver", informeController::estudianteVerInforme);
        app.get("/estudiante/informe/crear", informeController::estudianteRegistroInformePage);
        app.post("/estudiante/informe/crear", informeController::estudianteCrearInforme);
        app.post("/estudiante/informe/{id}/enviar", informeController::estudianteEnviarInforme);
        
        // Docente
        app.get("/docente/informes", informeController::docenteVerInformesPendientes);
        app.get("/docente/informe/{id}/calificar", informeController::docenteCalificarInformePage);
        app.post("/docente/informe/{id}/calificar", informeController::docenteCalificarInforme);
        
        // Coordinador
        app.get("/coordinador/informes", informeController::coordinadorVerInformesPendientes);
        app.get("/coordinador/informe/{id}/revisar", informeController::coordinadorRevisarInformePage);
        app.post("/coordinador/informe/{id}/revisar", informeController::coordinadorRevisarInforme);
        
        // === RUTAS DE ASIGNACIÓN DOCENTE (eliminadas para evitar duplicados) ===
        
        // === RUTAS DE SUSTENTACIONES ===
        // Coordinador
        app.get("/coordinador/sustentacion/programar", sustentacionController::mostrarProgramarSustentacion);
        app.post("/coordinador/sustentacion/programar", sustentacionController::programarSustentacion);
        app.get("/coordinador/sustentaciones", sustentacionController::listarSustentaciones);
        
        // Docente - Sustentaciones  
        app.get("/docente/sustentacion/{id}/calificar", sustentacionController::mostrarCalificarSustentacion);
        app.post("/docente/sustentacion/{id}/calificar", sustentacionController::calificarSustentacion);
        
        // === RUTAS DE ADMINISTRACIÓN ===
        app.get("/admin/usuarios", adminController::gestionarUsuarios);
        app.post("/admin/usuario/{id}/cambiar-estado", adminController::cambiarEstadoUsuario);
        app.get("/admin/registro-personal", adminController::mostrarRegistroPersonal);
        app.post("/admin/registro-personal", adminController::registrarPersonal);
        app.get("/admin/reportes", adminController::mostrarReportes);
        app.get("/admin/usuario/{id}/ver", adminController::verUsuario);
        
        // Rutas adicionales del sistema
        app.get("/gestion/usuarios", ctx -> ctx.render("gestion_usuarios.html"));
        app.get("/gestion/practicas", ctx -> ctx.render("gestion_practicas.html"));
        app.get("/gestion/reportes", ctx -> ctx.render("gestion_reportes.html"));
        app.get("/gestion/empresas", ctx -> ctx.render("gestion_empresas.html"));
        app.get("/mis-practicas", ctx -> ctx.render("mis_practicas.html"));
        app.get("/evaluaciones", ctx -> ctx.render("evaluaciones.html"));
        app.get("/notificaciones", ctx -> ctx.render("notificaciones.html"));
        app.get("/configuracion", ctx -> ctx.render("configuracion_sistema.html"));
        
        // === RUTAS DE ARCHIVOS ===
        // Upload de archivos
        app.post("/upload/plan", fileController::uploadPlanFile);
        app.post("/upload/reporte", fileController::uploadReporteFile);
        app.post("/upload/informe", fileController::uploadInformeFile);
        app.post("/upload/documento", fileController::uploadDocumento);
        
        // Descarga de archivos
        app.get("/download/{tipo}/{fileName}", fileController::downloadFile);
        
        // === RUTAS DE DOCUMENTOS ===
        app.get("/estudiante/documentos", documentoController::estudianteDocumentos);
        app.get("/coordinador/documentos/gestionar", documentoController::coordinadorGestionarDocumentos);
        app.post("/coordinador/documentos/gestionar", documentoController::coordinadorGestionarDocumentos);
        app.get("/documentos/especialidad", documentoController::listarDocumentosEspecialidad);
        app.post("/documento/{id}/desactivar", documentoController::desactivarDocumento);
        
        // === RUTAS DE DOCUMENTOS DE ESPECIALIDAD ===
        app.post("/coordinador/documentos/subir", documentosEspecialidadController::subirDocumentos);
        app.get("/coordinador/documento/{id}/descargar", documentosEspecialidadController::descargarDocumento);
        app.get("/coordinador/documento/{id}/eliminar", documentosEspecialidadController::eliminarDocumento);
        app.get("/estudiante/documento/{id}/descargar", documentosEspecialidadController::descargarDocumento);
        
        // Logout
        app.post("/logout", authController::logout);
    }
}