package com.syspre.models;

import com.syspre.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserModel {
    
    public void createTables() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 1. Crear tabla usuarios (base del sistema)
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    codigo VARCHAR(20) UNIQUE,
                    nombres VARCHAR(100) NOT NULL,
                    apellidos VARCHAR(100) NOT NULL,
                    dni VARCHAR(8) UNIQUE,
                    telefono VARCHAR(15),
                    direccion TEXT,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    especialidad VARCHAR(100),
                    semestre INT,
                    ciclo_academico VARCHAR(20),
                    tipo ENUM('estudiante', 'docente', 'coordinador', 'admin') NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
            """;
            stmt.execute(createUsersTable);
            System.out.println("✅ Tabla usuarios creada");
            
            // 2. Crear tabla planes_practica
            String createPlanesTable = """
                CREATE TABLE IF NOT EXISTS planes_practica (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    estudiante_id INT NOT NULL,
                    empresa_nombre VARCHAR(200) NOT NULL,
                    empresa_direccion TEXT,
                    empresa_supervisor VARCHAR(200),
                    actividades_planificadas TEXT NOT NULL,
                    objetivos_generales TEXT NOT NULL,
                    objetivos_especificos TEXT NOT NULL,
                    cronograma TEXT,
                    archivo_plan VARCHAR(255),
                    estado ENUM('borrador', 'enviado', 'aprobado_docente', 'aprobado_coordinador', 'rechazado') DEFAULT 'borrador',
                    comentarios_docente TEXT,
                    comentarios_coordinador TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (estudiante_id) REFERENCES usuarios(id) ON DELETE CASCADE
                )
            """;
            stmt.execute(createPlanesTable);
            System.out.println("✅ Tabla planes_practica creada");
            
            // 3. Crear tabla reportes_semanales
            String createReportesTable = """
                CREATE TABLE IF NOT EXISTS reportes_semanales (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    estudiante_id INT NOT NULL,
                    semana_numero INT NOT NULL,
                    fecha_inicio DATE NOT NULL,
                    fecha_fin DATE NOT NULL,
                    actividades_realizadas TEXT NOT NULL,
                    horas_trabajadas INT NOT NULL,
                    aprendizajes TEXT,
                    dificultades TEXT,
                    archivo_reporte VARCHAR(255),
                    calificacion DECIMAL(3,1),
                    comentarios_docente TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (estudiante_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                    UNIQUE KEY unique_semana_estudiante (estudiante_id, semana_numero)
                )
            """;
            stmt.execute(createReportesTable);
            System.out.println("✅ Tabla reportes_semanales creada");
            
            // 4. Crear tabla informes_finales
            String createInformesTable = """
                CREATE TABLE IF NOT EXISTS informes_finales (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    estudiante_id INT NOT NULL,
                    resumen_ejecutivo TEXT NOT NULL,
                    metodologia TEXT NOT NULL,
                    resultados TEXT NOT NULL,
                    conclusiones TEXT NOT NULL,
                    recomendaciones TEXT,
                    archivo_informe VARCHAR(255),
                    calificacion_docente DECIMAL(3,1),
                    calificacion_coordinador DECIMAL(3,1),
                    comentarios_docente TEXT,
                    comentarios_coordinador TEXT,
                    estado ENUM('borrador', 'enviado', 'calificado_docente', 'aprobado_coordinador') DEFAULT 'borrador',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (estudiante_id) REFERENCES usuarios(id) ON DELETE CASCADE
                )
            """;
            stmt.execute(createInformesTable);
            System.out.println("✅ Tabla informes_finales creada");
            
            // 5. Crear tabla asignaciones_docente
            String createAsignacionesTable = """
                CREATE TABLE IF NOT EXISTS asignaciones_docente (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    estudiante_id INT NOT NULL,
                    docente_id INT NOT NULL,
                    coordinador_id INT NOT NULL,
                    tipo_asignacion ENUM('asesor', 'jurado_presidente', 'jurado_vocal', 'jurado_secretario') NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (estudiante_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                    FOREIGN KEY (docente_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                    FOREIGN KEY (coordinador_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                    UNIQUE KEY unique_estudiante_tipo (estudiante_id, tipo_asignacion)
                )
            """;
            stmt.execute(createAsignacionesTable);
            System.out.println("✅ Tabla asignaciones_docente creada");
            
            // 6. Crear tabla sustentaciones
            String createSustentacionesTable = """
                CREATE TABLE IF NOT EXISTS sustentaciones (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    estudiante_id INT NOT NULL,
                    fecha_programada DATETIME NOT NULL,
                    lugar VARCHAR(200),
                    presidente_jurado_id INT NOT NULL,
                    vocal_jurado_id INT NOT NULL,
                    secretario_jurado_id INT NOT NULL,
                    calificacion_final DECIMAL(3,1),
                    observaciones TEXT,
                    estado ENUM('programada', 'realizada', 'cancelada') DEFAULT 'programada',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (estudiante_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                    FOREIGN KEY (presidente_jurado_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                    FOREIGN KEY (vocal_jurado_id) REFERENCES usuarios(id) ON DELETE CASCADE,
                    FOREIGN KEY (secretario_jurado_id) REFERENCES usuarios(id) ON DELETE CASCADE
                )
            """;
            stmt.execute(createSustentacionesTable);
            System.out.println("✅ Tabla sustentaciones creada");
            
            // 7. Crear tabla documentos_reglamento
            String createDocumentosTable = """
                CREATE TABLE IF NOT EXISTS documentos_reglamento (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    nombre VARCHAR(200) NOT NULL,
                    descripcion TEXT,
                    especialidad VARCHAR(100),
                    archivo_path VARCHAR(255) NOT NULL,
                    tipo_documento ENUM('reglamento', 'formato', 'guia', 'normativa') NOT NULL,
                    es_activo BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
            """;
            stmt.execute(createDocumentosTable);
            System.out.println("✅ Tabla documentos_reglamento creada");
            
            // Crear usuario administrador por defecto
            com.syspre.utils.DatabaseHelper.ensureAdminUserExists();
            
            // Crear usuarios de prueba para desarrollo (opcional)
            com.syspre.utils.DatabaseHelper.createTestUsers();
            
            System.out.println("🎉 ESQUEMA COMPLETO: 7 tablas creadas exitosamente");
            
        } catch (SQLException e) {
            System.err.println("⚠️ Error creando tablas (servidor funcionará sin DB): " + e.getMessage());
            e.printStackTrace();
            // No propagar el error para permitir que el servidor se inicie
        }
    }
    
    // Método eliminado - ahora se maneja en DatabaseHelper
    
    public Map<String, Object> authenticate(String email, String password) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (BCrypt.checkpw(password, hashedPassword)) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", rs.getInt("id"));
                    userData.put("codigo", rs.getString("codigo"));
                    userData.put("nombres", rs.getString("nombres"));
                    userData.put("apellidos", rs.getString("apellidos"));
                    userData.put("dni", rs.getString("dni"));
                    userData.put("telefono", rs.getString("telefono"));
                    userData.put("direccion", rs.getString("direccion"));
                    userData.put("email", rs.getString("email"));
                    userData.put("especialidad", rs.getString("especialidad"));
                    userData.put("semestre", rs.getObject("semestre"));
                    userData.put("ciclo_academico", rs.getString("ciclo_academico"));
                    userData.put("tipo", rs.getString("tipo"));
                    return userData;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error en autenticación: " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean create(Map<String, Object> data) {
        String sql = """
            INSERT INTO usuarios (codigo, nombres, apellidos, dni, telefono, direccion, 
                                email, password, especialidad, semestre, ciclo_academico, tipo) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, (String) data.get("codigo"));
            stmt.setString(2, (String) data.get("nombres"));
            stmt.setString(3, (String) data.get("apellidos"));
            stmt.setString(4, (String) data.get("dni"));
            stmt.setString(5, (String) data.get("telefono"));
            stmt.setString(6, (String) data.get("direccion"));
            stmt.setString(7, (String) data.get("email"));
            
            // Hash password
            String password = (String) data.get("password");
            stmt.setString(8, BCrypt.hashpw(password, BCrypt.gensalt()));
            
            stmt.setString(9, (String) data.get("especialidad"));
            stmt.setObject(10, data.get("semestre"));
            stmt.setString(11, (String) data.get("ciclo_academico"));
            stmt.setString(12, (String) data.get("tipo"));
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creando usuario: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error verificando email: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean existsByCodigo(String codigo) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE codigo = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error verificando código: " + e.getMessage());
        }
        
        return false;
    }
    
    public Map<String, Object> getUserById(int userId) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("codigo", rs.getString("codigo"));
                user.put("nombres", rs.getString("nombres"));
                user.put("apellidos", rs.getString("apellidos"));
                user.put("dni", rs.getString("dni"));
                user.put("telefono", rs.getString("telefono"));
                user.put("direccion", rs.getString("direccion"));
                user.put("email", rs.getString("email"));
                user.put("especialidad", rs.getString("especialidad"));
                user.put("semestre", rs.getInt("semestre"));
                user.put("ciclo_academico", rs.getString("ciclo_academico"));
                user.put("tipo", rs.getString("tipo"));
                user.put("created_at", rs.getTimestamp("created_at"));
                user.put("updated_at", rs.getTimestamp("updated_at"));
                return user;
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo usuario por ID: " + e.getMessage());
        }
        
        return null;
    }
    
    public Map<String, Object> findById(int id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", rs.getInt("id"));
                userData.put("codigo", rs.getString("codigo"));
                userData.put("nombres", rs.getString("nombres"));
                userData.put("apellidos", rs.getString("apellidos"));
                userData.put("dni", rs.getString("dni"));
                userData.put("telefono", rs.getString("telefono"));
                userData.put("direccion", rs.getString("direccion"));
                userData.put("email", rs.getString("email"));
                userData.put("especialidad", rs.getString("especialidad"));
                userData.put("semestre", rs.getObject("semestre"));
                userData.put("ciclo_academico", rs.getString("ciclo_academico"));
                userData.put("tipo", rs.getString("tipo"));
                return userData;
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando usuario: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<Map<String, Object>> findAll() {
        String sql = "SELECT * FROM usuarios ORDER BY tipo, nombres, apellidos";
        List<Map<String, Object>> usuarios = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", rs.getInt("id"));
                userData.put("codigo", rs.getString("codigo"));
                userData.put("nombres", rs.getString("nombres"));
                userData.put("apellidos", rs.getString("apellidos"));
                userData.put("dni", rs.getString("dni"));
                userData.put("email", rs.getString("email"));
                userData.put("especialidad", rs.getString("especialidad"));
                userData.put("tipo", rs.getString("tipo"));
                userData.put("activo", rs.getBoolean("activo"));
                usuarios.add(userData);
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo todos los usuarios: " + e.getMessage());
        }
        
        return usuarios;
    }
    
    public List<Map<String, Object>> findByType(String tipo) {
        String sql = "SELECT * FROM usuarios WHERE tipo = ? ORDER BY nombres, apellidos";
        List<Map<String, Object>> usuarios = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tipo);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", rs.getInt("id"));
                userData.put("nombres", rs.getString("nombres"));
                userData.put("apellidos", rs.getString("apellidos"));
                userData.put("email", rs.getString("email"));
                userData.put("especialidad", rs.getString("especialidad"));
                usuarios.add(userData);
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando usuarios por tipo: " + e.getMessage());
        }
        
        return usuarios;
    }
    
    public boolean activarUsuario(int userId) {
        String sql = "UPDATE usuarios SET activo = TRUE WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error activando usuario: " + e.getMessage());
            return false;
        }
    }
    
    public boolean desactivarUsuario(int userId) {
        String sql = "UPDATE usuarios SET activo = FALSE WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error desactivando usuario: " + e.getMessage());
            return false;
        }
    }
    
    public Map<String, Object> getEstadisticasGenerales() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Contar usuarios por tipo
            String sql = "SELECT tipo, COUNT(*) as total FROM usuarios WHERE activo = TRUE GROUP BY tipo";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                estadisticas.put("total_" + rs.getString("tipo"), rs.getInt("total"));
            }
            
            // Contar planes por estado
            sql = "SELECT estado, COUNT(*) as total FROM planes_practica GROUP BY estado";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                estadisticas.put("planes_" + rs.getString("estado"), rs.getInt("total"));
            }
            
            // Contar sustentaciones programadas
            sql = "SELECT COUNT(*) as total FROM sustentaciones WHERE estado = 'programada'";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) {
                estadisticas.put("sustentaciones_programadas", rs.getInt("total"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo estadísticas: " + e.getMessage());
        }
        
        return estadisticas;
    }
    
    // Obtener docentes por especialidad
    public List<Map<String, Object>> getDocentesByEspecialidad(String especialidad) {
        List<Map<String, Object>> docentes = new ArrayList<>();
        String sql = "SELECT id, nombres, apellidos, email, especialidad FROM usuarios WHERE tipo = 'docente' AND especialidad = ? ORDER BY apellidos, nombres";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, especialidad);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> docente = new HashMap<>();
                    docente.put("id", rs.getInt("id"));
                    docente.put("nombres", rs.getString("nombres"));
                    docente.put("apellidos", rs.getString("apellidos"));
                    docente.put("email", rs.getString("email"));
                    docente.put("especialidad", rs.getString("especialidad"));
                    docentes.add(docente);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo docentes por especialidad: " + e.getMessage());
        }
        
        return docentes;
    }
}