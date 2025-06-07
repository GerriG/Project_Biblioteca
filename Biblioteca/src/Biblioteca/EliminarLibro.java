package Biblioteca;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EliminarLibro {

    //Metodo para eliminar el libro en la BD
    public static void eliminar(JFrame parent, int libroId) {
        int confirmacion = JOptionPane.showConfirmDialog(
                parent,
                "¿Estás seguro de que deseas eliminar este libro?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM Libros WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, libroId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(parent, "Libro eliminado correctamente");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(parent, "Error al eliminar libro: " + ex.getMessage());
            }
        }
    }
}
