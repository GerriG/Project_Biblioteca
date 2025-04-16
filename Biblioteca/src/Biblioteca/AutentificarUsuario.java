package Biblioteca;

// Dependencias
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AutentificarUsuario {
    public boolean validarUsuario(String correo, String contraseña) {
        String sql = "SELECT * FROM Usuarios WHERE correo = ? AND contrasenia = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, correo);
            pstmt.setString(2, contraseña);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // Si hay resultados, el usuario existe y la contraseña es correcta.

        } catch (SQLException e) {
            System.out.println("❌ Error al validar usuario: " + e.getMessage());
            return false;
        }
    }
}

