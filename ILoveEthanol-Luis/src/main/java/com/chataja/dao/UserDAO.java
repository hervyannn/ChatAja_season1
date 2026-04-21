package com.chataja.dao;

import com.chataja.db.DatabaseManager;
import com.chataja.model.Admin;
import com.chataja.model.Majelis;
import com.chataja.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk tabel users.
 */
public class UserDAO {

    /**
     * Autentikasi login berdasarkan username, password, dan role.
     * @return User object jika berhasil, null jika gagal.
     */
    public User login(String username, String password, String role) {
        String sql = "SELECT * FROM users WHERE username=? AND password=? AND role=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] login error: " + e.getMessage());
        }
        return null;
    }





    // ── Private helpers ────────────────────────────────────────────────────

    private User mapUser(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        User u;
        if ("admin".equals(role)) {
            u = new Admin();
        } else if ("majelis".equals(role)) {
            u = new Majelis();
        } else {
            u = new Admin(); // fallback – treat unknown as admin object
        }
        u.setIdUser(rs.getString("id_user"));
        u.setNama(rs.getString("nama"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRole(role);
        return u;
    }


}
