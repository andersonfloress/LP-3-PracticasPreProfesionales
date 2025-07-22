package com.syspre.models;

import com.syspre.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentoReglamentoModel {
    
    public boolean create(Map<String, Object> data) {
        String sql = """
            INSERT INTO documentos_reglamento (nombre, descripcion, especialidad, archivo_path, 
                                             tipo_documento, es_activo) 
            VALUES (?, ?, ?, ?, ?, true)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, (String) data.get("nombre"));
            stmt.setString(2, (String) data.get("descripcion"));
            stmt.setString(3, (String) data.get("especialidad"));
            stmt.setString(4, (String) data.get("archivo_path"));
            stmt.setString(5, (String) data.get("tipo_documento"));
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creando documento: " + e.getMessage());
            return false;
        }
    }
    
    public boolean update(int documentoId, Map<String, Object> data) {
        String sql = """
            UPDATE documentos_reglamento 
            SET nombre = ?, descripcion = ?, especialidad = ?, archivo_path = ?, tipo_documento = ?
            WHERE id = ?
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, (String) data.get("nombre"));
            stmt.setString(2, (String) data.get("descripcion"));
            stmt.setString(3, (String) data.get("especialidad"));
            stmt.setString(4, (String) data.get("archivo_path"));
            stmt.setString(5, (String) data.get("tipo_documento"));
            stmt.setInt(6, documentoId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error actualizando documento: " + e.getMessage());
            return false;
        }
    }
    
    public Map<String, Object> findById(int documentoId) {
        String sql = "SELECT * FROM documentos_reglamento WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, documentoId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapDocumentoFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando documento: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<Map<String, Object>> findAll() {
        String sql = "SELECT * FROM documentos_reglamento WHERE es_activo = true ORDER BY tipo_documento, nombre";
        return executeDocumentoQuery(sql);
    }
    
    public List<Map<String, Object>> findByTipo(String tipoDocumento) {
        String sql = "SELECT * FROM documentos_reglamento WHERE tipo_documento = ? AND es_activo = true ORDER BY nombre";
        return executeDocumentoQuery(sql, tipoDocumento);
    }
    
    public List<Map<String, Object>> findByEspecialidad(String especialidad) {
        String sql = """
            SELECT * FROM documentos_reglamento 
            WHERE (especialidad = ? OR especialidad IS NULL) AND es_activo = true 
            ORDER BY tipo_documento, nombre
        """;
        return executeDocumentoQuery(sql, especialidad);
    }
    
    public List<Map<String, Object>> findByEspecialidadActivos(String especialidad) {
        String sql = "SELECT * FROM documentos_reglamento WHERE especialidad = ? AND es_activo = true ORDER BY id DESC";
        List<Map<String, Object>> documentos = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, especialidad);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                documentos.add(mapDocumentoFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error buscando documentos activos: " + e.getMessage());
        }
        return documentos;
    }
    
    public List<Map<String, Object>> findReglamentos() {
        return findByTipo("reglamento");
    }
    
    public List<Map<String, Object>> findFormatos() {
        return findByTipo("formato");
    }
    
    public List<Map<String, Object>> findGuias() {
        return findByTipo("guia");
    }
    
    public List<Map<String, Object>> findNormativas() {
        return findByTipo("normativa");
    }
    
    public boolean activar(int documentoId) {
        return updateEstado(documentoId, true);
    }
    
    public boolean desactivar(int documentoId) {
        return updateEstado(documentoId, false);
    }
    
    private boolean updateEstado(int documentoId, boolean esActivo) {
        String sql = "UPDATE documentos_reglamento SET es_activo = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, esActivo);
            stmt.setInt(2, documentoId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error actualizando estado del documento: " + e.getMessage());
            return false;
        }
    }
    
    public boolean delete(int documentoId) {
        // Eliminación lógica
        return desactivar(documentoId);
    }
    
    public boolean deletePhysical(int documentoId) {
        // Eliminación física
        String sql = "DELETE FROM documentos_reglamento WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, documentoId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error eliminando documento físicamente: " + e.getMessage());
            return false;
        }
    }
    
    public List<String> getTiposDocumento() {
        List<String> tipos = new ArrayList<>();
        tipos.add("reglamento");
        tipos.add("formato");
        tipos.add("guia");
        tipos.add("normativa");
        return tipos;
    }
    
    public List<String> getEspecialidades() {
        String sql = "SELECT DISTINCT especialidad FROM usuarios WHERE especialidad IS NOT NULL ORDER BY especialidad";
        List<String> especialidades = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String especialidad = rs.getString("especialidad");
                if (especialidad != null && !especialidad.trim().isEmpty()) {
                    especialidades.add(especialidad);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo especialidades: " + e.getMessage());
        }
        
        return especialidades;
    }
    
    private List<Map<String, Object>> executeDocumentoQuery(String sql, Object... params) {
        List<Map<String, Object>> documentos = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                documentos.add(mapDocumentoFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error ejecutando consulta de documentos: " + e.getMessage());
        }
        
        return documentos;
    }
    
    private Map<String, Object> mapDocumentoFromResultSet(ResultSet rs) throws SQLException {
        Map<String, Object> documento = new HashMap<>();
        documento.put("id", rs.getInt("id"));
        documento.put("nombre", rs.getString("nombre"));
        documento.put("descripcion", rs.getString("descripcion"));
        documento.put("especialidad", rs.getString("especialidad"));
        documento.put("archivo_path", rs.getString("archivo_path"));
        documento.put("tipo_documento", rs.getString("tipo_documento"));
        documento.put("es_activo", rs.getBoolean("es_activo"));
        documento.put("created_at", rs.getTimestamp("created_at"));
        documento.put("updated_at", rs.getTimestamp("updated_at"));
        return documento;
    }
}