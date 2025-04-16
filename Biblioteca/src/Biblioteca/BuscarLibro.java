package Biblioteca;

import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class BuscarLibro {

    public static void buscarPorTitulo(String titulo, DefaultTableModel modeloTabla) {
        modeloTabla.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Libros WHERE titulo LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "%" + titulo + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String estado = rs.getBoolean("disponible") ? "Disponible" : "Agotado";
                modeloTabla.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("titulo"),
                    rs.getString("autor"),
                    rs.getInt("anio"),
                    estado,
                    rs.getInt("stock")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}